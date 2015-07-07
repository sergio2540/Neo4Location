package org.neo4location.processing.strucuture;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.neo4j.gis.spatial.pipes.GeoPipeline;
import org.neo4j.gis.spatial.server.plugin.SpatialPlugin;
import org.neo4location.domain.Neo4LocationLabels;
import org.neo4location.domain.Neo4LocationProperties;
import org.neo4location.domain.Neo4LocationRelationships;
import org.neo4location.domain.trajectory.Move;
import org.neo4location.domain.trajectory.Point;
import org.neo4location.domain.trajectory.RawData;
//import org.neo4location.domain.trajectory.SemanticTrajectory;
import org.neo4location.domain.trajectory.Trajectory;
import org.neo4location.processing.Structure;
import org.neo4location.utils.Neo4LocationProcessingUtils;

import com.spatial4j.core.context.SpatialContext;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import com.vividsolutions.jts.io.WKTWriter;

public class VelocityBasedStructure implements Structure {

  //Semantic Trajectories: Mobility Data Computation and Annotation, Yan 2012
  //4.3. Trajectory VelocityBasedStructure Layer (pag. 12)

  //We analyze sensitivity of the coefficients δ1 and δ2 (e.g., δ1 = δ2 = δ = 30%) through experiments.
  //pag. 13

  private float mDelta1 = 0.3f;
  private float mDelta2 = 0.3f;

  private float mSpeedThreshold;
  private long mMinStopTime;

  private Point mFrom;
  private Point mTo;


  //  private List<Move> tempStopPoints;
  //  private List<Move> tempMovePoints;


  public VelocityBasedStructure(float speedThreshold, long minStopTime, float delta1, float delta2){

    mSpeedThreshold = speedThreshold;
    mMinStopTime = minStopTime;

    mDelta1 = delta1;
    mDelta2 = delta2;

  }

  public Trajectory velocityBased(Trajectory trajectory){

    /* initialize: calculate GPS instant SPEED if needed */
    if(mSpeedThreshold == 0){
      mSpeedThreshold = getDynamicSpeedThreshold(null, null, mDelta1, mDelta2);
    }

    /* episode annotation: tag each GPS point with ‘S’ or ‘M’ */
    Iterable<Move> mvs = trajectory.getMoves();

    List<Move> stopPoints = new ArrayList<Move>();
    List<Move> movePoints = new ArrayList<Move>();
    List<Move> tempMoves = new ArrayList<Move>();

    mFrom = null;
    mTo = null;

    Move mMove = null;
    Point mStop = null;


    for(Move move : mvs){
      
      
      float instantSpeed = Neo4LocationProcessingUtils.speed(move);
      
      //Durante um periodo t a velocidade 
      //encontra-se abaixo de 10 sec

      if(instantSpeed < mSpeedThreshold){

        stopPoints.add(move);
        
        if(mStop != null || movePoints.size() > 0){
          
          mMove = computeMovePoint(movePoints);
          movePoints = new ArrayList<Move>();
          
          mStop = null;
          tempMoves.add(mMove);
        }
        
      } else {
        
        movePoints.add(move);
         
        if(mMove != null || stopPoints.size() > 0){
          
          mStop = computeStopPoint(movePoints, stopPoints);
          stopPoints = new ArrayList<Move>();
          
          mMove = null;
        }

        if(mStop != null){
          
          if(mFrom == null){
            //First time
            mFrom = mStop;
          } else if(mTo == null){
            //Second time
            mTo = mStop;
          } else {
            mFrom = mTo; 
            mFrom = mStop;
          }
        
        }
      }
    
    }

    if(stopPoints.size() != 0){
      mStop = computeStopPoint(movePoints, stopPoints);
    }
    
    if(movePoints.size() != 0){
      mMove = computeMovePoint(movePoints);
      tempMoves.add(mMove);
    }
    
    String newTrajectoryName = String.format("%s-%s", getName(), trajectory.getTrajectoryName());
    Trajectory semanticTrajectory = new Trajectory(newTrajectoryName, trajectory.getUser(), tempMoves, trajectory.getSemanticData());

    return semanticTrajectory;

  }

  private Point computeStopPoint(List<Move> tempMovePoints, List<Move> groupOfStopPoints) {

    //TODO:
    //minStopTime == Duration.ofSeconds(sec,nano)
    //REVER
    int groupSize = groupOfStopPoints.size();
    
    if(groupSize == 0)
      return null;
          
    Duration totalTime = Neo4LocationProcessingUtils.totalTime(groupOfStopPoints);


    if(totalTime.compareTo(Duration.ofMillis(mMinStopTime)) == 1){
      
      
      Coordinate[] coordinateArray = new Coordinate[groupSize];
      int i=0;
      for(Move m : groupOfStopPoints){
        Point p = m.getFrom();
        RawData rd = p.getRawData();
        Coordinate co = new Coordinate(rd.getLatitude(),rd.getLongitude(),rd.getAltitude());
        coordinateArray[i++] = co;
      }
      CoordinateSequence points = new CoordinateArraySequence(coordinateArray, 3);
  
      String wkt = WKTWriter.toLineString(points);
      
      long startInstant = groupOfStopPoints.get(0).getFrom().getRawData().getTime();
      long endInstant = groupOfStopPoints.get(groupOfStopPoints.size()-1).getFrom().getRawData().getTime();

      //stop : (timefrom, timeto, center, boundingRectangle);
      // add the stop episode

      RawData rawData = null;
      
      Map<String, Object> semanticData = new HashMap<String, Object>();
      
      semanticData.put(Neo4LocationProperties.START_INSTANT, startInstant);
      semanticData.put(Neo4LocationProperties.END_INSTANT, endInstant);
      
//      semanticData.put(Neo4LocationProperties.CENTER, center);
//      semanticData.put(Neo4LocationProperties.MBR, mbr);
      semanticData.put(Neo4LocationProperties.WKT, wkt);
      
      Collection<Neo4LocationLabels> labels = new ArrayList<Neo4LocationLabels>();
      labels.add(Neo4LocationLabels.EPISODE);

      
      Point p = new Point(rawData, semanticData, labels);

      
      return p;
      
      
    } else {

      tempMovePoints.addAll(groupOfStopPoints);
      return null;

    }

  }


  private Move computeMovePoint(List<Move> groupOfMovePoints) {

    int first = 0;
    int last = groupOfMovePoints.size()-1;

    Move mFirst = groupOfMovePoints.get(first);
    Move mLast = groupOfMovePoints.get(last);

    //String trajname = (String) mFirst.getFrom().getSemanticData().get(Neo4LocationProperties.TRAJNAME);

    Point pFrom = mFirst.getFrom();
    Point pTo = mLast.getTo();

    Instant instantFrom = Instant.ofEpochMilli(pFrom.getRawData().getTime());

    Instant instantTo = Instant.ofEpochMilli(pTo.getRawData().getTime());

    // create a move episode
    Point from = (mFrom != null) ? mFrom : pFrom;
    Point to = (mTo != null) ? mTo : pTo;

    Map<String,Object> props = new HashMap<String,Object>();
    long delta_t = Duration.between(instantFrom,instantTo).toMillis();
    props.put("delta_t", delta_t);

    Move move = new Move(Neo4LocationRelationships.MOVE, from, to, props);

    return move;

  }


  /**
   * @param point
   * @param obj
   * @param delta1 - 
   * @param delta2 - 
   * @return double
   */

  //TODO: 
  
  /*
  DYNAMIC VELOCITY THRESHOLD 
  
  For each GPS point P(lat,lon, t) of a given moving object, the speed is dynamically 
  determined by the moving object (by using objectAvgSpeed – the average speed of this moving 
  object) and the underlying context (by positionAvgSpeed – the average speed of most moving 
  objects in this position. 
  
  Δspeed = min(delta1*objectAvgSpeed, delta2*positionAvgSpeedg, where delta1 and delta2 
  are coefficients.
  */
  
  private float getDynamicSpeedThreshold(List<Move> traj, Point obj, float delta1, float delta2){

    //p - ponto x,y,z
    //obj_id - objeto -> car, bus

    float velocityThreshold = 0;
    
    //objectAvgSpeed – the average SPEED of this moving object
    //speed media do objecto na trajectoria
    float objectAvgSpeed = Neo4LocationProcessingUtils.getAverageSpeed(traj);
    
    //positionAvgSpeed – the average SPEED of most moving objects in this position ⟨x, y⟩
    RawData rd = obj.getRawData();
    if(rd == null || rd.getSpeed() == null){
      return delta1*objectAvgSpeed;
    }
    
    float positionAvgSpeed = rd.getSpeed();

    velocityThreshold = (float) Math.min(delta1*objectAvgSpeed, delta2*positionAvgSpeed);
    
    return velocityThreshold;
  }


  @Override
  public Collection<Trajectory> process(Collection<Trajectory> trajectories) {
    
    if(trajectories == null){
      //Throw exception with text you must call setTrajectories(Collection<Trajectory> trajectories)
      return Collections.emptyList();

    }
    
    
    return trajectories.stream()
        .map((trajectory) -> velocityBased(trajectory))
        .collect(Collectors.toList());

  }


  @Override
  public String getName() {
    return this.getClass().getSimpleName();
  }


}