package org.neo4location.utils;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.event.LabelEntry;
import org.neo4j.graphdb.event.TransactionData;
import org.neo4location.domain.Neo4LocationProperties;
import org.neo4location.domain.Neo4LocationRelationships;
import org.neo4location.domain.trajectory.Move;
import org.neo4location.domain.trajectory.Person;
import org.neo4location.domain.trajectory.Point;
import org.neo4location.domain.trajectory.RawData;
import org.neo4location.domain.trajectory.Trajectory;
import org.neo4location.graphdb.Neo4JMove;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.distance.DistanceUtils;
import com.spatial4j.core.distance.GeodesicSphereDistCalc;
import com.spatial4j.core.distance.GeodesicSphereDistCalc.Haversine;
import com.spatial4j.core.shape.impl.PointImpl;

public class Neo4LocationProcessingUtils {


  public static Duration interval(Move mv) {

    Duration duration;

    Point pFrom = mv.getFrom();
    Point pTo = mv.getTo();

    RawData rFrom = pFrom.getRawData();
    RawData rTo = pTo.getRawData();

    if (rFrom == null || rTo == null){

      //Symbolic point
      //Get Distance + time
      Map<String, Object> sd = mv.getSemanticData();
      duration = Duration.ofMillis((long) sd.getOrDefault(Neo4LocationProperties.DURATION, -1));


    } else {

      long tFrom = rFrom.getTime(); 
      long tTo = rTo.getTime();

      Instant instantFirst = Instant.ofEpochMilli(tFrom);
      Instant instantSecond = Instant.ofEpochMilli(tTo);

      duration = Duration.between(instantFirst, instantSecond);



    }

    return duration;
  }

  //in meters
  public static double distance(Move mv) {

    double distance = 0;

    RawData rFrom = mv.getFrom().getRawData();
    RawData rTo = mv.getTo().getRawData();

    if (rFrom == null || rTo == null){
      //Symbolic point
      //Get Distance + time
      Map<String, Object> sd = mv.getSemanticData();
      distance = (double) sd.getOrDefault(Neo4LocationProperties.DISTANCE, -1);

      if(distance == -1){
        //TODO: Lancar excepcao
      }



    } else {

      //Usar spatial 4j 
      //			CartesianDistCalc cd = new CartesianDistCalc();
      //			GeodesicSphereDistCalc gdh = new GeodesicSphereDistCalc.Haversine();
      //			GeodesicSphereDistCalc gdlc = new GeodesicSphereDistCalc.LawOfCosines();
      //			GeodesicSphereDistCalc gdv = new GeodesicSphereDistCalc.Vincenty();

      //https://github.com/spatial4j/spatial4j/blob/master/src/main/java/com/spatial4j/core/shape/impl/PointImpl.java

      PointImpl from = new PointImpl(rFrom.getLongitude(), rFrom.getLatitude(), SpatialContext.GEO);
      PointImpl to = new PointImpl(rTo.getLongitude(), rTo.getLatitude(), SpatialContext.GEO);


      //https://github.com/spatial4j/spatial4j/blob/7b28c125e979fa93a0a9ef0de5d42e0c49584dd5/src/test/java/com/spatial4j/core/distance/TestDistances.java

      //Falta verificar se e cartesiano ou geo
      Haversine haversine = new GeodesicSphereDistCalc.Haversine();

      distance = haversine.distance(from, to);

      distance *= DistanceUtils.DEG_TO_KM*1000;

      //distance = rad/degree

    }		

    return distance;

  }

  //TODO: Criar Classe Distance.

  //unidade m/s
  public static float speed(Move mv){


    //TODO: Rever e fazer calculo com info semantica dos arcos. 
    //Calcula com base em rawData dos nodes.
    float speed = speedBasedOnPointInfo(mv);

    if(speed == -1){
      return speedBasedOnMoveInfo(mv);
    }


    return speed;


  }

  private static float speedBasedOnMoveInfo(Move mv) {

    Map<String, Object> semanticData = mv.getSemanticData();

    if(semanticData != null){

      if(semanticData.get(Neo4LocationProperties.SPEED) != null) {
        return (float) semanticData.get(Neo4LocationProperties.SPEED);
      }


      //Return meters
      float distance = (float)  semanticData.get(Neo4LocationProperties.DISTANCE);
      //Return seconds
      float duration = (float) semanticData.get(Neo4LocationProperties.DURATION);

      return distance/duration;
    } 

    return -1;

  }

  private static float speedBasedOnPointInfo(Move mv) {

    Point pFrom = mv.getFrom();
    Point pTo = mv.getTo();

    RawData rFrom = pFrom.getRawData();
    RawData rTo = pTo.getRawData();

    if(rFrom != null  && rTo != null ){

      if(rFrom.getSpeed() != null || rTo.getSpeed() != null){

        if(rFrom.getSpeed() != null)
          return rFrom.getSpeed();

        if(rTo.getSpeed() != null)
          return rTo.getSpeed();  

      }


      //Return meters
      float distance = (float) distance(mv);
      //Return seconds
      float interval = (float) interval(mv).toMillis()*(0.001f);

      return distance/interval;
    } 

    return -1;

  }


  public static Duration totalTime(List<Move> groupOfStopPoints){

    int first = 0;
    int last = groupOfStopPoints.size()-1;
    Move mFirst = groupOfStopPoints.get(first);
    Move mLast = groupOfStopPoints.get(last);

    Point pFrom = mFirst.getFrom();

    Point pTo = mLast.getTo();

    Instant instantFrom = Instant.ofEpochMilli(pFrom.getRawData().getTime());
    Instant instantTo = Instant.ofEpochMilli(pTo.getRawData().getTime());

    return Duration.between(instantFrom,  instantTo);

  }


  public static Collection<Trajectory> toCollectionTrajectory(GraphDatabaseService db, TransactionData data){

    Collection<Trajectory> trajs = new ArrayList<>();

    //adicionar property to all relationships with Neo4LocationProperties.TRAJNAME
    Iterable<Node> nodes = data.createdNodes();
    Iterable<Relationship> rels = data.createdRelationships();

    String trajectoryName;
    String lastTrajectoryName = null;
    boolean first = true;

    Person user = new Person("");
    Map<String,Object> semanticData = new HashMap<String, Object>();
    Collection<Move> mvs = new ArrayList<>();

    //try (Transaction tx = db.beginTx()){
    try{

      for(Relationship rel : rels){

        String relationshipType = rel.getType().name();
        
        if(Neo4LocationRelationships.START_A.name().equals(relationshipType)){
          
            Node userNode = rel.getStartNode();
            String username  = (String) userNode.getProperty(Neo4LocationProperties.USERNAME);
            user = new Person(username);
          
          Node trajectory = rel.getEndNode();
          for(String key : rel.getPropertyKeys()){
            
            Object property = trajectory.getProperty(key);
            semanticData.put(key, property);
          
          }
        
        }
        
        if(!Neo4LocationRelationships.MOVE.name().equals(relationshipType)){
          continue;
        }
        
        trajectoryName = (String) rel.getProperty(Neo4LocationProperties.TRAJNAME);

        if(lastTrajectoryName == null){
          lastTrajectoryName = trajectoryName;
          //first = false;
        }

        if(trajectoryName.equals(lastTrajectoryName)){
          Move mv = new Neo4JMove(rel).getMove();
          mvs.add(mv);
        } else {
          
          Trajectory traj = new Trajectory(lastTrajectoryName, user, mvs, semanticData);
          trajs.add(traj);
          
          user = new Person("");
          semanticData = new HashMap<String, Object>();
          mvs = new ArrayList<>();

        }

        lastTrajectoryName = trajectoryName;
      }

      //tx.success();  
    } catch(Exception e){
      //TODO:
      System.out.println(e.getMessage());
    }

    return trajs;

  }


}