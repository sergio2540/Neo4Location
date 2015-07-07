package org.neo4location.server.unit.processing;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.neo4location.domain.trajectory.Trajectory;
import org.neo4location.processing.Annotation;
import org.neo4location.processing.annotation.ElevationAnnotation;
import org.neo4location.processing.annotation.GeoCodingAnnotation;
import org.neo4location.processing.annotation.PlacesAnnotation;
import org.neo4location.processing.annotation.SnapToRoadsAnnotation;
import org.neo4location.processing.identification.PredefinedTimeIntervalIdentification;
import org.neo4location.processing.identification.RawGPSGapIdentification;
import org.neo4location.utils.Neo4LocationTestsUtils;



@RunWith(Parameterized.class)
public class TestTrajectoryAnnotation {
  
  private static final int ITERATIONS = 1;
  
  @Parameters(name = "{index}:")
  public static Collection<Object[]> data() {

    final Collection<Object[]> col = new ConcurrentLinkedQueue<>();

    for(int i=0; i < ITERATIONS; i++){

      Collection<Object> objs = new ConcurrentLinkedQueue<>();
      objs.add(i);
      col.add(objs.toArray());
   
    }

    return col;

  }
  
  @Parameter // first data value (0) is default
  public /* NOT private */ int mTest;
  
  public TestTrajectoryAnnotation() {
    
  }
  
  //@Test
  public void testGeoCodingAnnotation() throws Exception { 
  
    Annotation ri = new GeoCodingAnnotation();
    int movesPerTrajectory = 4;
    int numberOfUsers = 1;
    int trajectoriesPerUser = 1;
    Trajectory[] trajectories = Neo4LocationTestsUtils.createTrajectory(numberOfUsers, trajectoriesPerUser , movesPerTrajectory);    
    Collection<Trajectory> postProcess = ri.process(Arrays.asList(trajectories));
    System.out.println(postProcess);
  
  }
  
  //@Test
  public void testElevationAnnotation() throws Exception { 
  
    Annotation ri = new ElevationAnnotation();
    int movesPerTrajectory = 4;
    int numberOfUsers = 1;
    int trajectoriesPerUser = 1;
    Trajectory[] trajectories = Neo4LocationTestsUtils.createTrajectory(numberOfUsers, trajectoriesPerUser , movesPerTrajectory);    
    Collection<Trajectory> postProcess = ri.process(Arrays.asList(trajectories));
    System.out.println(postProcess);
  
  }
  
  @Test
  public void testPlacesAnnotation() throws Exception { 
  
    Annotation ri = new PlacesAnnotation();
    int movesPerTrajectory = 4;
    int numberOfUsers = 1;
    int trajectoriesPerUser = 1;
    Trajectory[] trajectories = Neo4LocationTestsUtils.createTrajectory(numberOfUsers, trajectoriesPerUser , movesPerTrajectory);    
    Collection<Trajectory> postProcess = ri.process(Arrays.asList(trajectories));
    System.out.println(postProcess);
  
  }
  
  //@Test
  public void testSnapToRoadAnnotation() throws Exception { 
  
    boolean interpolate = true;
    Annotation ri = new SnapToRoadsAnnotation(interpolate);
    int movesPerTrajectory = 4;
    int numberOfUsers = 1;
    int trajectoriesPerUser = 1;
    Trajectory[] trajectories = Neo4LocationTestsUtils.createTrajectory(numberOfUsers, trajectoriesPerUser , movesPerTrajectory);    
    Collection<Trajectory> postProcess = ri.process(Arrays.asList(trajectories));
    System.out.println(postProcess);
  
  }
  
}