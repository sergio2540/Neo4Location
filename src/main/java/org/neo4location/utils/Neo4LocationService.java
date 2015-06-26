package org.neo4location.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.neo4j.gis.spatial.indexprovider.LayerNodeIndex;
import org.neo4j.gis.spatial.indexprovider.SpatialIndexProvider;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4location.domain.Neo4LocationLabels;
import org.neo4location.domain.Neo4LocationProperties;
import org.neo4location.domain.Neo4LocationRelationships;
import org.neo4location.domain.trajectory.Move;
import org.neo4location.domain.trajectory.Person;
import org.neo4location.domain.trajectory.Point;
import org.neo4location.domain.trajectory.RawData;
import org.neo4location.domain.trajectory.Trajectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Neo4LocationService {
  
  
  private Logger logger = LoggerFactory.getLogger(Neo4LocationService.class);


  public synchronized void writeTrajectory(final Trajectory trajectory){

    //REMOVER
//    final Iterable<Move> moves = trajectory.getMoves();
//
//    final Node person = getOrCreatePerson(trajectory);
//
//    final Node traj = createTrajectory(trajectory);
//    person.createRelationshipTo(traj, DynamicRelationshipType.withName(Neo4LocationRelationships.START_A.name()));	
//
//    //first move
//    final Move m = moves.iterator().next();
//
//    final Node nLast = createPoint(m.getFrom());
//    traj.createRelationshipTo(nLast, DynamicRelationshipType.withName(Neo4LocationRelationships.FROM.name()));
//    append(traj,nLast,moves);
    
  }



  private Node getOrCreatePerson(Trajectory trajectory) {
    // TODO Auto-generated method stub
    return null;
  }


  //PUT TRAJECTORY
  public void write(final Collection<Trajectory> trajectories){

    for(Trajectory trajectory : trajectories){

      writeTrajectory(trajectory);
    
    }

  }




  //Pode ser executado por varias threads 
  //com diferentes objectos trajectory
  //sync trajectory com lock
  public boolean appendTrajectory(final Trajectory trajectory, GraphDatabaseService db) {

    //Sync mGraphDatabaseService
    //Transaction tx = mGraphDatabaseService.beginTx()


    final AtomicBoolean created = new AtomicBoolean(true);
    final Iterable<Move> moves = trajectory.getMoves();

    try (Transaction tx = db.beginTx()){

      final Node optionalPerson = getPerson(trajectory,db);
      final Node optionalTraj = getTrajectory(trajectory,db);


      if(optionalPerson != null) {
        created.set(false);
        final String personName = trajectory.getUser().getPersonName();
        //Update personName
        optionalPerson.setProperty(Neo4LocationProperties.USERNAME, personName);
      }

      final Node person = (optionalPerson != null) ? optionalPerson : createPerson(trajectory,db);

      final String trajectoryName = trajectory.getTrajectoryName();
      
      if(optionalTraj != null) {

        created.set(false);

       
        final Map<String,Object> props = trajectory.getSemanticData();

        optionalTraj.setProperty(Neo4LocationProperties.TRAJNAME, trajectoryName);

        for(Entry<String, Object> prop: props.entrySet()){

          optionalTraj.setProperty(prop.getKey(),prop.getValue());

          //			if(prop.getKey().equals("error")){
          //				mAccuracyInKm = (double) prop.getValue();
          //			}

        }


      }

      final Node traj = (optionalTraj != null) ? optionalTraj : createTrajectory(trajectory,db);


      final Node nLast = getLastPosition(trajectoryName, moves, person,traj,db);

      append(trajectoryName,traj,nLast,moves,db);


      tx.success();	
      tx.close();

    } catch(Exception e){
      //REVER TRANSACTIONS NEO4J
      logger.error("APPEND: " + e.toString());
      for(StackTraceElement st :e.getStackTrace()){
        logger.error("APPEND: " + st.toString());
      }
    }

    return created.get();

  }

  //POST
  public boolean append(final Collection<Trajectory> trajectories, GraphDatabaseService db){

    //TODO: created
    boolean created = false;


    trajectories.stream().forEach((trajectory) -> {

      appendTrajectory(trajectory,db);

    });



    return created;
  }


  private static DynamicRelationshipType FROM = DynamicRelationshipType.withName(Neo4LocationRelationships.FROM.name());
  private static DynamicRelationshipType TO = DynamicRelationshipType.withName(Neo4LocationRelationships.TO.name());
  private static DynamicRelationshipType START_A = DynamicRelationshipType.withName(Neo4LocationRelationships.START_A.name());

  private Node getLastPosition(final String trajName, Iterable<Move> moves, Node person, Node traj, GraphDatabaseService db) {

    if(!traj.hasRelationship(FROM, Direction.OUTGOING)){
      //logger.error("first time");
      //FIRST TIME WE SEE THIS TRAJ
      Relationship startA = person.createRelationshipTo(traj, START_A);	

      Move m = moves.iterator().next();
      final Node nLast = createPoint(trajName, m.getFrom(),db);
      traj.createRelationshipTo(nLast, FROM);

      return nLast;

    } 
    else {
      //FIRST + N TIME WE SEE THIS TRAJ
      Relationship rTo = traj.getSingleRelationship(TO, Direction.OUTGOING);		
      final Node nLast = rTo.getEndNode();
      rTo.delete();
      return nLast;

    }

  }

  private void append(final String trajName, final Node traj, final Node nLast, final Iterable<Move> moves, GraphDatabaseService db) {

    final AtomicReference<Node> from =  new AtomicReference<Node>(nLast);

    for(Move m : moves) {

      final AtomicReference<Point> pTo = new AtomicReference<>(m.getTo());

      final AtomicReference<Node> to = new AtomicReference<>(createPoint(trajName, pTo.get(),db));

      final Relationship r = from.getAndSet(to.get()).createRelationshipTo(to.get(), DynamicRelationshipType.withName(m.getRelationship().name()));


      //Relationship r = nLast.getSingleRelationship(DynamicRelationshipType.withName(m.getRelationship().name()), Direction.OUTGOING);

      //Devia retornar SemanticData ou Map
      Map<String, Object> sd = m.getSemanticData();

      if(sd != null){

        for(Entry<String, Object> kv : sd.entrySet()){

          r.setProperty(kv.getKey(), kv.getValue());

        }

      }

      //from = to;


    }

    traj.createRelationshipTo(from.get(), TO);

  }

  /////////
  //POINT//
  /////////
  private Node getPoint(final Point point, GraphDatabaseService db) {

    //Adicionar suporte a Z.
    //Timestamp nao deixa fazer merge

    double lat = point.getRawData().getLatitude(); 
    double lon =  point.getRawData().getLongitude();

    Map<String, Object> params = new HashMap<String, Object>();

    double accuracyInKm;

    Map<String,Object> sd = point.getSemanticData();

    if(point.getSemanticData() == null){

      accuracyInKm = 0.1;

    }
    else {

      accuracyInKm = (double) sd.get("error");

    }

    params.put(LayerNodeIndex.DISTANCE_IN_KM_PARAMETER, accuracyInKm);
    params.put(LayerNodeIndex.POINT_PARAMETER, new Double[] { lat, lon });

    final Index<Node> index =  db.index().forNodes("points", SpatialIndexProvider.SIMPLE_POINT_CONFIG);

    final IndexHits<Node> results = index.query(LayerNodeIndex.WITHIN_DISTANCE_QUERY, params);


    int degree = 1;

    if (results.hasNext()) {

      //Retorna primeiro que encontra
      //Estava tao proximo que a db considera igual
      //Nao e clustering
      final Node tp = results.next();

      //degree
      degree = (int) tp.getProperty("degree") + 1;
      tp.setProperty("degree", degree);
      return tp;


    }
    else {

      return null;

    }

  }

  private synchronized Node createPoint(final String trajName, final Point point, GraphDatabaseService db) {

    //por enquanto cria sempre
    //TODO: Rever .toArray

    final Node p = db.createNode();


    point.getLabels().forEach((label) -> {
    
      p.addLabel(DynamicLabel.label(label.name()));
    
    });


    RawData rd = point.getRawData();

    if(rd != null){

      p.setProperty(Neo4LocationProperties.LATITUDE, rd.getLatitude());
      p.setProperty(Neo4LocationProperties.LONGITUDE, rd.getLongitude());
      p.setProperty(Neo4LocationProperties.TIMESTAMP, rd.getTime());

      Double alt = rd.getAltitude();

      if(alt != null)
        p.setProperty(Neo4LocationProperties.ALTITUDE, alt.doubleValue());

      Float acc = rd.getAccuracy();

      if(acc != null)
        p.setProperty(Neo4LocationProperties.ACCURACY, acc.floatValue());

      Float speed = rd.getSpeed();
      if(speed!= null)
        p.setProperty(Neo4LocationProperties.SPEED, speed.floatValue());


    }

    Map<String,Object> sd = point.getSemanticData();

    if(sd != null){

      for(Entry<String, Object> kv : sd.entrySet()){

        p.setProperty(kv.getKey(), kv.getValue());

      }


    }

    //Set TrajName
    p.setProperty(Neo4LocationProperties.TRAJNAME, trajName);

    //Set Degree
    p.setProperty("degree", 1);

    final Index<Node> index =  db.index().forNodes("points", SpatialIndexProvider.SIMPLE_POINT_CONFIG);
    index.add(p, String.valueOf(p.getId()), String.valueOf(p.getId()));

    return p;
  }

  private Node getOrCreatePoint(final Point p, GraphDatabaseService db) {

    //Node point = getPoint(p);
    Node point = null;
    if(point == null) {
      return createPoint("",p,db);
    }


    return point;
  }


  //////////////
  //TRAJECTORY//
  //////////////
  private static Label TRAJECTORY = DynamicLabel.label(Neo4LocationLabels.TRAJECTORY.name());

  private synchronized Node getTrajectory(final Trajectory trajectory, GraphDatabaseService db) {

    final String trajectoryName = trajectory.getTrajectoryName();
    final Node traj =  db.findNode(TRAJECTORY, Neo4LocationProperties.TRAJNAME, trajectoryName);

    return traj;
    //		if(traj == null){
    //			return Optional.ofNullable(traj);
    //		}else {
    //			return Optional.of(traj);
    //		}
  }

  private synchronized Node createTrajectory(final Trajectory trajectory, GraphDatabaseService db) {


    final String trajectoryName = trajectory.getTrajectoryName();
    final Map<String,Object> props = trajectory.getSemanticData();



    final Node traj = db.createNode(TRAJECTORY);
    traj.setProperty(Neo4LocationProperties.TRAJNAME, trajectoryName);
    for(Entry<String, Object> prop: props.entrySet()){

      traj.setProperty(prop.getKey(),prop.getValue());

      //			if(prop.getKey().equals("error")){
      //				mAccuracyInKm = (double) prop.getValue();
      //			}

    }

    return traj;

  }

  //	private Node getOrCreateTrajectory(final Trajectory trajectory) {
  //
  //
  //		final Node traj = getTrajectory(trajectory);
  //
  //		if(traj == null){
  //			return createTrajectory(trajectory);
  //		}
  //
  //		final String trajectoryName = trajectory.getTrajectoryName();
  //		final Map<String,Object> props = trajectory.getSemanticData();
  //
  //		traj.setProperty(Neo4LocationProperties.TRAJNAME, trajectoryName);
  //
  //		for(Entry<String, Object> prop: props.entrySet()){
  //
  //			traj.setProperty(prop.getKey(),prop.getValue());
  //
  //			//			if(prop.getKey().equals("error")){
  //			//				mAccuracyInKm = (double) prop.getValue();
  //			//			}
  //
  //		}
  //
  //		return traj;
  //
  //	}

  ////////////////////
  ////PERSON/USER/////
  ///////////////////

  private static final Label USER = DynamicLabel.label(Neo4LocationLabels.USER.name());

  //Colocar em Neo4jPerson
  private Node createPerson(Trajectory trajectory, GraphDatabaseService db) {

    final Person user = trajectory.getUser();

    if(user == null){
      return db.createNode(USER);
    }

    final String personName = user.getPersonName();

    final Node person = db.createNode(USER);
    person.setProperty(Neo4LocationProperties.USERNAME, personName);

    return person;

  }

  private Node getPerson(Trajectory trajectory, GraphDatabaseService db) {

    String personName = trajectory.getUser().getPersonName();
    final Node person =  db.findNode(USER, Neo4LocationProperties.USERNAME, personName);
    return person;

  }

  

  //DELETE
  //	public void delete(Collection<Trajectory> trajectories){
  //
  //	}


  //GET
  //	public Collection<Trajectory> get(String query){
  //
  //		return null;
  //
  //	}

}