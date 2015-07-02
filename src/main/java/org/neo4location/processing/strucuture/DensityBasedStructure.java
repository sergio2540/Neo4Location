package org.neo4location.processing.strucuture;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.neo4location.domain.Neo4LocationLabels;
import org.neo4location.domain.trajectory.Move;
import org.neo4location.domain.trajectory.Point;
import org.neo4location.domain.trajectory.RawData;
import org.neo4location.domain.trajectory.Trajectory;
import org.neo4location.processing.Structure;


public class DensityBasedStructure implements Structure, DistanceMeasure {

  //private GraphDatabaseService mGraphDatabaseService;
  private double mMaxDist;
  private long mMinStopTime;



  public DensityBasedStructure(double maxDist, long minStopTime){

    //mGraphDatabaseService = graphDatabaseService;
    mMaxDist = maxDist;
    mMinStopTime = minStopTime;

  }

  public Trajectory densityBased(Trajectory trajectory){
    
    //DistanceMeasure dm = new 
    
    DBSCANClusterer<DoublePoint> dbscan = new DBSCANClusterer<DoublePoint>(.05, 50, this);
    
    Iterable<Move> moves = trajectory.getMoves();
    
    List<DoublePoint> points = new ArrayList<DoublePoint>();
    
    for(Move move: moves){
      
      double[] point = new double[3];
      
      //TODO: Null checks
      point[0] = move.getFrom().getRawData().getLatitude();
      point[1] = move.getFrom().getRawData().getLongitude();
      
      DoublePoint dp = new DoublePoint(point );
      
      points.add(dp);
    
    }
    
    List<Cluster<DoublePoint>> cluster = dbscan.cluster(points);

    Collection<Move> newMoves = new ArrayList<Move>();
        
    for(Cluster<DoublePoint> c: cluster){
      
      //TODO:
      
      //Create a node (ver velocityBased)
      RawData rawData = null;
      Map<String, Object> semanticData = null;
      Collection<Neo4LocationLabels> labels = null;
      
      Point p = new Point(rawData, semanticData, labels);
      
      //move entre clusters
      System.out.println(c.getPoints().get(0));
    
    }  
    
    String newTrajectoryName = String.format("%s-%s", getName(), trajectory.getTrajectoryName());
    Trajectory semanticTrajectory = new Trajectory(newTrajectoryName, trajectory.getUser(), newMoves, trajectory.getSemanticData());

    return semanticTrajectory;

  }


  @Override
  public Collection<Trajectory> process(Collection<Trajectory> trajectories) {


    if(trajectories == null){
      //Throw exception with text you must call setTrajectories(Collection<Trajectory> trajectories)
      return Collections.emptyList();

    }


    return trajectories.stream()
        .map((trajectory) -> densityBased(trajectory))
        .collect(Collectors.toList());

  }

  @Override
  public String getName() {

    return this.getClass().getSimpleName();

  }

  @Override
  public double compute(double[] arg0, double[] arg1) {
    // TODO Auto-generated method stub
    return 0;
  }


  //1. ∥tib −tia∥ ≤ τ e ∥⟨xia, yia⟩ − ⟨xib, yib⟩∥ ≤ σ

  //Ultimo elemento da trajetoria dista de mais de σ e menos τ => ∥ta − t∥ ≤ τ and ∥⟨xa, ya⟩ − ⟨x, y⟩∥ > σ
}