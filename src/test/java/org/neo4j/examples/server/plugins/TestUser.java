package org.neo4j.examples.server.plugins;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Comparator;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.ws.rs.core.Response;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.server.rest.domain.JsonParseException;
import org.neo4j.test.Mute;
import org.neo4location.domain.trajectory.Move;
import org.neo4location.domain.trajectory.Trajectory;
import org.neo4location.server.plugins.Neo4LocationRESTService;
import org.neo4location.utils.Neo4LocationTestsUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

@RunWith(Parameterized.class)
public class TestUser {

  //Max Person = 7
  private static final int ITERATIONS = 3;

  //Max 8
  private static final int START_USERS = 8;
  private static final int INC_USERS = 0;

  private static final int START_TRAJECTORIES_PER_USER = 5;
  private static final int INC_TRAJECTORIES_PER_USER = 0;

  private static final int START_MOVES_PER_TRAJECTORY = 6;
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
      System.out.println(i);
    }

    return col;
  }

  @Parameter // first data value (0) is default
  public static /* NOT private */ int mTest;

  @Parameter(value = 1)
  public static /* NOT private */ int mNumberOfUsers;

  @Parameter(value = 2)
  public static /* NOT private */ int mTrajectoriesPerUser;

  @Parameter(value = 3)
  public static /* NOT private */ int mMovesPerTrajectory;

  @Parameter(value = 4)
  public static /* NOT private */ int mUser;

  private static Trajectory[] mTrajectories;


  @Rule public Mute mMute = Mute.muteAll();

  @Rule
  public Neo4jRule mNeo4j = new Neo4jRule()
  .withExtension("/neo4location", Neo4LocationRESTService.class); 

  //Mudar para BeforeClass
  @Before
  public void shouldCreateTrajectory() throws Exception
  {

    String  url = "neo4location/trajectories";

    byte[] json;

    Path filename = Paths.get(String.format("./datasets/tests/create-user-%d-%d-%d.json", mNumberOfUsers, mTrajectoriesPerUser, mMovesPerTrajectory));

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


    com.squareup.okhttp.Response response = Neo4LocationTestsUtils.POST(mNeo4j.httpURI(), url, json);

    //Testar
    //assertEquals(201, response.code());

  }

  private static ObjectMapper objectMapper = new ObjectMapper();
  //private static ObjectWriter mObjectWriter = objectMapper.writerFor(TestParams.class);
  
  @Test
  public void shouldReturnTrajectories() throws JsonParseException, IOException
  {
    StringBuilder url = new StringBuilder("neo4location/trajectories?");

    String username = mTrajectories[mUser*mTrajectoriesPerUser].getUser().getPersonName();

    
    url.append(String.format("username=%s", username));

    
    TestParams testParams = new TestParams();
    testParams.setUsername(username);
    Path filename = Paths.get(String.format("./datasets/tests/shouldReturnTrajectories-%d-%d-%d-%d.json", mTest, mNumberOfUsers, mTrajectoriesPerUser, mMovesPerTrajectory));
    byte[] json = objectMapper.writeValueAsBytes(testParams);
    Files.write(filename, json, StandardOpenOption.CREATE_NEW);

    
    Iterable<Trajectory> trajectories = httpGET(url.toString());
    assertTrajectoriesGivenUser(trajectories, 1);
    assertTrajectoriesGivenOnlyOneUser(trajectories);

  }


  @Test
  public void shouldReturnAllTrajectories() throws JsonParseException, IOException
  {

    StringBuilder url = new StringBuilder("neo4location/trajectories");

    Iterable<Trajectory> trajectories = httpGET(url.toString());
    
    TestParams testParams = new TestParams();
    Path filename = Paths.get(String.format("./datasets/tests/shouldReturnAllTrajectories-%d-%d-%d-%d.json", mTest, mNumberOfUsers, mTrajectoriesPerUser, mMovesPerTrajectory));
    byte[] json = objectMapper.writeValueAsBytes(testParams);
    Files.write(filename, json, StandardOpenOption.CREATE_NEW);

    assertTrajectoriesGivenUser(trajectories, mNumberOfUsers);

  }

  //Testar melhor skip, limit, orderBy
  @Test
  public void shouldSkipTrajectories() throws JsonParseException, IOException
  {
    int skip = 2;

    StringBuilder url = new StringBuilder("neo4location/trajectories?");

    String username = mTrajectories[mUser*mTrajectoriesPerUser].getUser().getPersonName();
    url.append(String.format("username=%s", username));

    url.append(String.format("&skip=%d", skip));
    Iterable<Trajectory> trajectories = httpGET(url.toString());
    
    TestParams testParams = new TestParams();
    testParams.setUsername(username);
    testParams.setSkip(String.valueOf(skip));
    
    Path filename = Paths.get(String.format("./datasets/tests/shouldSkipTrajectories-%d-%d-%d-%d.json", mTest, mNumberOfUsers, mTrajectoriesPerUser, mMovesPerTrajectory));
    byte[] json = objectMapper.writeValueAsBytes(testParams);
    Files.write(filename, json, StandardOpenOption.CREATE_NEW);

    assertThat(trajectories)
    .hasSize(mTrajectoriesPerUser-skip)
    .doesNotContainNull();

  }

  @Test
  public void shouldSkipAndLimitTrajectories() throws JsonParseException, IOException
  {
    
    int skip =  1;
    int limit = 2;

    StringBuilder url = new StringBuilder("neo4location/trajectories?");
    url.append(String.format("skip=%d&limit=%d",skip,limit));
    Iterable<Trajectory> trajectories = httpGET(url.toString());
    
    
    TestParams testParams = new TestParams();
    testParams.setSkip(String.valueOf(skip));
    testParams.setLimit(String.valueOf(limit));
    Path filename = Paths.get(String.format("./datasets/tests/shouldSkipAndLimitTrajectories-%d-%d-%d-%d.json", mTest, mNumberOfUsers, mTrajectoriesPerUser, mMovesPerTrajectory));
    byte[] json = objectMapper.writeValueAsBytes(testParams);
    Files.write(filename, json, StandardOpenOption.CREATE_NEW);

    assertThat(trajectories)
    .hasSize(limit)
    .doesNotContainNull();

  }

  

  @Test
  public void shouldSkipLimitAndOrderByTrajectories() throws JsonParseException, IOException
  {

    int skip = 1;
    int limit = 2;

    String orderBy = "n.degree";

    StringBuilder url = new StringBuilder("neo4location/trajectories?");
    url.append(String.format("skip=%d&limit=%d",skip,limit));
    url.append(String.format("&orderBy=%s", orderBy));
    url.append(String.format("&sum=%s", orderBy));

    Iterable<Trajectory> trajectories = httpGET(url.toString());

    TestParams testParams = new TestParams();
    testParams.setSkip(String.valueOf(skip));
    testParams.setLimit(String.valueOf(limit));
    testParams.setOrderBy(orderBy);
    testParams.setSum(orderBy);
    
    Path filename = Paths.get(String.format("./datasets/tests/shouldSkipLimitAndOrderByTrajectories-%d-%d-%d-%d.json", mTest, mNumberOfUsers, mTrajectoriesPerUser, mMovesPerTrajectory));
    byte[] json = objectMapper.writeValueAsBytes(testParams);
    Files.write(filename, json, StandardOpenOption.CREATE_NEW);
    

    assertThat(trajectories)
    .hasSize(limit)
    .doesNotContainNull();


    Trajectory prevTraj = null;  
    boolean first = true;



    //    trajectories.stream().parallel().max(new Comparator<Trajectory>() {
    //
    //      @Override
    //      public int compare(Trajectory t1, Trajectory t2) {
    //
    //        Integer it1 = Integer.valueOf(((String) t1.getSemanticData().get("degree")));
    //        Integer it2 = Integer.valueOf(((String) t2.getSemanticData().get("degree")));
    //
    //        return it1.compareTo(it2);
    //      }
    //
    //    });


    for(Trajectory traj : trajectories){

      if(first){
        prevTraj = traj;
        first = false;
        continue;
      }

      
      int prev = Integer.parseInt(((String) prevTraj.getSemanticData().get("degree")));
      int current = Integer.parseInt(((String) traj.getSemanticData().get("degree")));


      assertThat(prev).isGreaterThanOrEqualTo(current);
      //new prev
      prevTraj = traj;
    }





  }

  private Iterable<Trajectory> httpGET(String url) throws JsonGenerationException, JsonMappingException, IOException{

    com.squareup.okhttp.Response response = Neo4LocationTestsUtils.GET(mNeo4j.httpURI(), url);



    Iterable<Trajectory> trajectories = Neo4LocationTestsUtils.getStreamingCollection(response);

    
    
    Path filename = Paths.get(String.format("./datasets/tests/get-user-%d-%d-%d.json", mNumberOfUsers, mTrajectoriesPerUser, mMovesPerTrajectory));
    
    if(!Files.exists(filename))
      Files.write(filename,"".getBytes(), StandardOpenOption.CREATE_NEW);

    Files.write(filename, (url + "\n").getBytes(), StandardOpenOption.APPEND);


    //		assertThat(response.headers().toString())
    //		.isEqualTo(Response.Status.OK.getStatusCode());

    return trajectories;
  }

  private void assertTrajectoriesGivenUser(Iterable<Trajectory>  trajectories, int numberOfUsers) throws IOException{

    assertThat(trajectories)
    .hasSize(numberOfUsers*mTrajectoriesPerUser)
    .doesNotContainNull();	
  }


  private void assertTrajectoriesGivenOnlyOneUser(Iterable<Trajectory> trajectories) {

    for(Trajectory trajectory : trajectories){

      assertThat(trajectory.getTrajectoryName())
      .isNotNull()
      .isNotEmpty()
      .matches("\\d+");


      assertThat(trajectory.getUser())
      .isNotNull();

      String username = mTrajectories[mUser*mTrajectoriesPerUser].getUser().getPersonName();

      assertThat(trajectory.getUser().getPersonName())
      .isNotNull()
      .isNotEmpty()
      .matches("\\d+")
      .isEqualTo(username);


      Iterable<Move> moves = trajectory.getMoves();
      assertThat(moves)
      .hasSize(mMovesPerTrajectory)
      .doesNotContainNull();



    }

   
  }
}