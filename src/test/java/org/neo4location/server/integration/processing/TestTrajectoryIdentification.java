package org.neo4location.server.integration.processing;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.server.rest.domain.JsonParseException;
import org.neo4j.test.Mute;
import org.neo4location.domain.trajectory.Trajectory;
import org.neo4location.parameters.IntegrationParams;
import org.neo4location.server.plugins.Neo4LocationRESTService;
import org.neo4location.utils.Neo4LocationTestsUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(Parameterized.class)
public class TestTrajectoryIdentification {

  //Max START_MOVES_PER_TRAJECTORY - 2
  private static final int ITERATIONS = 2;
  //Max 8
  private static final int START_USERS = 3;
  private static final int INC_USERS = 0;
  private static final int START_TRAJECTORIES_PER_USER = 5;
  private static final int INC_TRAJECTORIES_PER_USER = 0;
  private static final int START_MOVES_PER_TRAJECTORY = 5;
  private static final int INC_MOVES_PER_TRAJECTORY = 0;


  @Parameters(name = "{index}:")
  public static Collection<Object[]> data() {

    final Collection<Object[]> col = new ConcurrentLinkedQueue<>();

    for(int i=0; i < ITERATIONS; i++){

      Collection<Object> objs = new ConcurrentLinkedQueue<>();

      objs.add(i);
      objs.add((START_USERS + INC_USERS*i));
      objs.add((START_TRAJECTORIES_PER_USER + INC_TRAJECTORIES_PER_USER*i));
      objs.add((START_MOVES_PER_TRAJECTORY + INC_MOVES_PER_TRAJECTORY*i));
      objs.add(i+1);

      col.add(objs.toArray());
      
    }

    return col;

  }

  @Parameter // first data value (0) is default
  public /* NOT private */ int mTest;

  @Parameter(value = 1)
  public /* NOT private */ int mNumberOfUsers;

  @Parameter(value = 2)
  public /* NOT private */ int mTrajectoriesPerUser;

  @Parameter(value = 3)
  public /* NOT private */ int mMovesPerTrajectory;

  @Parameter(value = 4)
  public /* NOT private */ int mSize;

  private Trajectory[] mTrajectories;


  @Rule public Mute mMute = Mute.muteAll();

  @Rule
  public final Neo4jRule mNeo4j = new Neo4jRule()
  .withExtension("/neo4location", Neo4LocationRESTService.class);

  //Mudar para BeforeClass
  //@Before
  
  public void shouldCreateTrajectory() throws Exception
  {

    String url = "neo4location/trajectories";
    byte[] json;

    Path filename = Paths.get(String.format("./examples/create-spatial-%d-%d-%d.json", mNumberOfUsers, mTrajectoriesPerUser, mMovesPerTrajectory));

    if(Files.exists(filename)){
      json = Files.readAllBytes(filename);
      mTrajectories = Neo4LocationTestsUtils.createTrajectory(json);
    }
    else {

      //Criar varios ficheiros com json
      mTrajectories = Neo4LocationTestsUtils.createTrajectory(mNumberOfUsers, mTrajectoriesPerUser, mMovesPerTrajectory);
      //Aqui devia ler de um ficheiro
      json = Neo4LocationTestsUtils.trajectoriesToJson(mTrajectories);
      Files.write(filename, json, StandardOpenOption.CREATE_NEW);

    }

    com.squareup.okhttp.Response response = Neo4LocationTestsUtils.POST(mNeo4j.httpsURI(), url, json);
    assertEquals(Response.Status.OK.getStatusCode(), response.code());

  }

  private static ObjectMapper objectMapper = new ObjectMapper();
  
  @Test
  public void shouldProcessRawGPSGapIdentification() throws Exception
  {

    StringBuilder url = new StringBuilder("neo4location/processing/identification/rawGPSGap");  

    long minStopTimeInMiliseconds = 1000;
    double maxDistance = 100;

//    url.append(String.format("minStopTime=%d",minStopTime));
//    url.append(String.format("&maxDistance=%f", maxDistance));
    
    IntegrationParams ip = new IntegrationParams(minStopTimeInMiliseconds,maxDistance);
    byte[] json = objectMapper.writeValueAsBytes(ip);
    

    com.squareup.okhttp.Response response = 
        Neo4LocationTestsUtils.POST(mNeo4j.httpsURI(), url.toString(), json);

    assertEquals(Response.Status.CREATED.getStatusCode(), response.code());
    
    shouldCreateTrajectory();
    //wait 5 seconds
    Thread.sleep(5000);

  }

  @Ignore
  @Test
  public void shouldProcessPredefinedTimeInterval() throws Exception
  {

    StringBuilder url = new StringBuilder("neo4location/processing/indentification/predefinedTimeInterval");  

    long minStopTimeInMiliseconds = 1000;

//    url.append(String.format("minStopTime=%d",minStopTime));
    
    IntegrationParams ip = new IntegrationParams(minStopTimeInMiliseconds);
    byte[] json = objectMapper.writeValueAsBytes(ip);
    
  
    com.squareup.okhttp.Response response = 
        Neo4LocationTestsUtils.POST(mNeo4j.httpsURI(), url.toString(), json);

    assertEquals(Response.Status.CREATED.getStatusCode(), response.code());
    
    shouldCreateTrajectory();
    //wait 5 seconds
    Thread.sleep(5000);

  }

}