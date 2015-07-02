package org.neo4location.server.unit.processing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.neo4location.domain.trajectory.Move;
import org.neo4location.domain.trajectory.Person;
import org.neo4location.domain.trajectory.Trajectory;
import org.neo4location.processing.identification.PredefinedTimeIntervalIdentification;
import org.neo4location.processing.identification.RawGPSGapIdentification;
import org.neo4location.processing.strucuture.DensityBasedStructure;
import org.neo4location.utils.Neo4LocationTestsUtils;




@RunWith(Parameterized.class)
public class TestTrajectoryIndentification {
  
  //Max START_MOVES_PER_TRAJECTORY - 2
  private static final int ITERATIONS = 7;
  //Max 8
  private static final int START_USERS = 3;
  private static final int INC_USERS = 0;
  private static final int START_TRAJECTORIES_PER_USER = 15;
  private static final int INC_TRAJECTORIES_PER_USER = 0;
  private static final int START_MOVES_PER_TRAJECTORY = 10;
  private static final int INC_MOVES_PER_TRAJECTORY = 0;
  
  
  @Parameters(name = "{index}:")
  public static Collection<Object[]> data() {

    final Collection<Object[]> col = new ConcurrentLinkedQueue<>();

    for(int i=0; i < ITERATIONS; i++){

      Collection<Object> objs = new ConcurrentLinkedQueue<>();

      objs.add(i);
//      objs.add((START_USERS + INC_USERS*i));
//      objs.add((START_TRAJECTORIES_PER_USER + INC_TRAJECTORIES_PER_USER*i));
//      objs.add((START_MOVES_PER_TRAJECTORY + INC_MOVES_PER_TRAJECTORY*i));
//      objs.add(i+1);

      col.add(objs.toArray());
      System.out.println(i);
    }

    return col;

  }
  
  @Parameter // first data value (0) is default
  public /* NOT private */ int mTest;

//  @Parameter(value = 1)
//  public /* NOT private */ long mMinStopTime;

//  @Parameter(value = 2)
//  public /* NOT private */ double mMaxDistance;


  
  public TestTrajectoryIndentification() {
    
  }
  
  @Test
  public void testRawGPSGapIdentification() throws Exception { 

    
    //meters
    double mMaxDistance = 20;
    
    //unidade = ms
    long mMinStopTime = 10;

    RawGPSGapIdentification ri = new RawGPSGapIdentification(mMaxDistance,  mMinStopTime);

    int movesPerTrajectory = 4;
    int numberOfUsers = 2;
    int trajectoriesPerUser = 1;

    Trajectory[] trajectories = Neo4LocationTestsUtils.createTrajectory(numberOfUsers, trajectoriesPerUser , movesPerTrajectory);    


    Collection<Trajectory> postProcess = ri.process(Arrays.asList(trajectories));


    System.out.println(postProcess);


  }

  @Test
  public void testPredefinedTimeIntervalIdentification() throws Exception { 

    //unidade = ms
    long mMinStopTime = 10*1000;

    PredefinedTimeIntervalIdentification pi = new PredefinedTimeIntervalIdentification(mMinStopTime);

    int movesPerTrajectory = 10;
    int numberOfUsers = 2;
    int trajectoriesPerUser = 10;

    Trajectory[] trajectories = Neo4LocationTestsUtils.createTrajectory(numberOfUsers, trajectoriesPerUser , movesPerTrajectory);    


    Collection<Trajectory> postProcess = pi.process(Arrays.asList(trajectories));


    System.out.println(postProcess);


  }
  
}