package org.neo4location.processing.strucuture;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.neo4location.domain.Neo4LocationLabels;
import org.neo4location.domain.Neo4LocationProperties;
import org.neo4location.domain.Neo4LocationRelationships;
import org.neo4location.domain.trajectory.Move;
import org.neo4location.domain.trajectory.Point;
import org.neo4location.domain.trajectory.RawData;
import org.neo4location.domain.trajectory.Trajectory;
import org.neo4location.processing.Structure;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.distance.GeodesicSphereDistCalc;
import com.spatial4j.core.distance.GeodesicSphereDistCalc.Haversine;
import com.spatial4j.core.shape.impl.PointImpl;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import com.vividsolutions.jts.io.WKTWriter;


public class DensityBasedStructure implements Structure, DistanceMeasure {

  //interface -> Clusterable 
  

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  
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
    
    DBSCANClusterer<Point> dbscan = new DBSCANClusterer<Point>(mMaxDist, 1, this);
    
    Iterable<Move> moves = trajectory.getMoves();
    
    List<Point> points = new ArrayList<Point>();
    
    for(Move move : moves){
      
//      double[] point = new double[3];    
//      //TODO: Null checks
//      point[0] = move.getFrom().getRawData().getLatitude();
//      point[1] = move.getFrom().getRawData().getLongitude();
//      DoublePoint dp = new DoublePoint(point );

      points.add(move.getFrom());
    
    }
    
    List<Cluster<Point>> cluster = dbscan.cluster(points);

    Collection<Move> newMoves = new ArrayList<Move>();
    Point to = null;  
    Point from = null; 
    
    for(Cluster<Point> c : cluster){
      //TODO:!!!!!!!!!!!!!!!CHECK NULLS!!!!!!!!!!!!!
      
      List<Point> clusteredPoints = c.getPoints(); 
      int clusteredPointsSize = clusteredPoints.size();
      
      //Create a POINT (ver velocityBased)
      Map<String, Object> semanticData = new HashMap<String, Object>();
      //Sort per time cluster
      Arrays.parallelSort(clusteredPoints.toArray(new Point[0]),new Comparator<Point>(){

        @Override
        public int compare(Point p1, Point p2) {
          
          RawData rd1 = p1.getRawData();
          RawData rd2 = p2.getRawData();
          
          if(rd1 == null && rd2 == null){
            return 0;
          }
          else if(rd1 == null){
            return 1;
          }
          else if(rd2 == null){
            return -1;
          }
          else {
            
            return Long.compare(rd1.getTime(), rd2.getTime());
          
          }
        }
        
      });
      
      //Start time
      long startInstant = clusteredPoints.get(0).getRawData().getTime();
      semanticData.put(Neo4LocationProperties.START_INSTANT, startInstant);
      //End time/Duration
      long endInstant = clusteredPoints.get(clusteredPointsSize-1).getRawData().getTime();
      semanticData.put(Neo4LocationProperties.END_INSTANT, endInstant);
      
      //WKT
      Coordinate[] coordinateArray = new Coordinate[clusteredPointsSize];
      int i=0;
      for(Point p : clusteredPoints){
        double[] point = p.getPoint();
        Coordinate co = new Coordinate(point[0], point[1]);
        coordinateArray[i++] = co;
      }
      CoordinateSequence seqPoints = new CoordinateArraySequence(coordinateArray);
      String wkt = WKTWriter.toLineString(seqPoints);
      semanticData.put(Neo4LocationProperties.WKT, wkt);
      
      Collection<Neo4LocationLabels> labels = new ArrayList<Neo4LocationLabels>();
      labels.add(Neo4LocationLabels.EPISODE);
      
      Point p = new Point(null, semanticData, labels);
      
      //MOVE entre clusters      
      if(from == null && to == null)
        from = p;
      else if(from != null && to == null){
        to = p;
      }
      else if(from != null && to != null){
        
        Map<String,Object> moveSemanticData = new HashMap<String, Object>();
        Move m = new Move(Neo4LocationRelationships.MOVE, from, to, moveSemanticData);
        newMoves.add(m);
        
        from = to;
        to = null;
      
      }
      
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
  public double compute(double[] p1, double[] p2) {
    
    double lat1 = p1[0];
    double lon1 = p1[1];
    
    double time1 = p1[2];
    
    double lat2 = p2[0];
    double lon2 = p2[1];
    
    double time2 = p2[2];
    
    double distance;
    
    PointImpl pImpl1 = new PointImpl(lat1, lon1, SpatialContext.GEO);
    PointImpl pImpl2 = new PointImpl(lat2, lon2, SpatialContext.GEO);
    Haversine haversine = new GeodesicSphereDistCalc.Haversine();
    distance = haversine.distance(pImpl1, pImpl2);
    
    return distance;
  
  }


  //1. ∥tib −tia∥ ≤ τ e ∥⟨xia, yia⟩ − ⟨xib, yib⟩∥ ≤ σ

  //Ultimo elemento da trajetoria dista de mais de σ e menos τ => ∥ta − t∥ ≤ τ and ∥⟨xa, ya⟩ − ⟨x, y⟩∥ > σ
}