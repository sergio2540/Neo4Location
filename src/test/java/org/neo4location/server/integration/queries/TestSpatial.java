package org.neo4location.server.integration.queries;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.ws.rs.core.Response;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
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
import org.neo4location.domain.trajectory.Move;
import org.neo4location.domain.trajectory.RawData;
import org.neo4location.domain.trajectory.Trajectory;
import org.neo4location.parameters.TestSpatialParams;
import org.neo4location.server.plugins.Neo4LocationRESTService;
import org.neo4location.utils.Neo4LocationTestsUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(Parameterized.class)
public class TestSpatial {

  //Max START_MOVES_PER_TRAJECTORY - 2
  private static final int ITERATIONS = 2;

  //Max 8
  private static final int START_USERS = 3;
  private static final int INC_USERS = 0;

  private static final int START_TRAJECTORIES_PER_USER = 15;
  private static final int INC_TRAJECTORIES_PER_USER = 0;

  private static final int START_MOVES_PER_TRAJECTORY = 10;
  private static final int INC_MOVES_PER_TRAJECTORY = 0;

  @Parameters(name = "{index}:")
  public static Collection<Object[]> data() {

    //		Collection<Object[]> col = new ArrayList<Object[]>();
    //
    //		int numberOfUsers = START_USERS;
    //		int trajectoriesPerUser = START_TRAJECTORIES_PER_USER;
    //		int movesPerTrajectory =  START_MOVES_PER_TRAJECTORY;
    //
    //		for(int i = 0; i < ITERATIONS;  i++){
    //
    //			col.add(new Object[] { 0, numberOfUsers, trajectoriesPerUser, movesPerTrajectory, i+1});	
    //
    //			numberOfUsers += INC_USERS;
    //			trajectoriesPerUser += INC_TRAJECTORIES_PER_USER;
    //			movesPerTrajectory += INC_MOVES_PER_TRAJECTORY;
    //
    //		}

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
  @Before
  public void shouldCreateTrajectory() throws Exception
  {

    System.out.println("numberOfUsers: " + mNumberOfUsers);
    String url = "neo4location/trajectories";
    //mTrajectories = Neo4LocationTestsUtils.createTrajectory(mNumberOfUsers, mTrajectoriesPerUser, mMovesPerTrajectory);

    byte[] json;


    Path filename = Paths.get(String.format("./datasets/tests/create-spatial-%d-%d-%d.json", mNumberOfUsers, mTrajectoriesPerUser, mMovesPerTrajectory));

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

    com.squareup.okhttp.Response response = Neo4LocationTestsUtils.POST(mNeo4j.httpsURI(), url.toString(),json);
    assertEquals(Response.Status.OK.getStatusCode(), response.code());

  }



  private Iterable<Trajectory> httpGET(String url) throws JsonGenerationException, JsonMappingException, IOException{

    com.squareup.okhttp.Response response = Neo4LocationTestsUtils.GET(mNeo4j.httpsURI(), url);
    Iterable<Trajectory> trajectories = Neo4LocationTestsUtils.getStreamingCollection(response);
    return trajectories;
    
  }

  private void assertTrajectoriesGivenUser(Collection<Trajectory>  trajectories, int numberOfUsers) throws IOException{

    assertThat(trajectories)
    .hasSize(numberOfUsers*mTrajectoriesPerUser)
    .doesNotContainNull();	
  }


  private void assertTrajectoriesGivenOnlyOneUser(Collection<Trajectory> trajectories) {

    for(Trajectory trajectory : trajectories){

      assertThat(trajectory.getTrajectoryName())
      .isNotNull()
      .isNotEmpty()
      .matches("\\d+");


      assertThat(trajectory.getUser())
      .isNotNull();

      //String username = mTrajectories[mUser*mTrajectoriesPerUser].getUser().getUsername();

      //			assertThat(trajectory.getUser().getUsername())
      //			.isNotNull()
      //			.isNotEmpty()
      //			.matches("\\d+")
      //			.isEqualTo(username);


      Iterable<Move> moves = trajectory.getMoves();
      assertThat(moves)
      .hasSize(mMovesPerTrajectory)
      .doesNotContainNull();

      //Falta testar retorno de latitude e longitude

    }
  }

  //	@Test
  public void shouldReturnATrajectoryGivenRadius() throws JsonParseException
  {
    //TODO: Testa radius 

  }

  private static ObjectMapper objectMapper = new ObjectMapper();

  @Test
  public void shouldReturnTrajectoryAtoB() throws JsonParseException, IOException
  {


    StringBuilder url = new StringBuilder("neo4location/trajectories?");

    double e = 0.001; 

    RawData[] ab = getBbox(mTrajectories);

    RawData a = ab[0];
    RawData b = ab[1];

    StringBuilder sb = new StringBuilder();
    //A
    sb.append(String.format(Locale.ENGLISH, "lat=%f&lon=%f", a.getLatitude()-e, a.getLongitude()-e ));
    sb.append(String.format(Locale.ENGLISH, "&lat=%f&lon=%f", a.getLatitude()+e, a.getLongitude()+e ));
    //B
    sb.append(String.format(Locale.ENGLISH, "&lat=%f&lon=%f", b.getLatitude()-e, b.getLongitude()-e));
    sb.append(String.format(Locale.ENGLISH, "&lat=%f&lon=%f", b.getLatitude()+e, b.getLongitude()+e));
    url.append(sb);

    


    Iterable<Trajectory> trajectories  = httpGET(url.toString());
    assertTrajectoriesGivenABSize(trajectories);


  }

  @Ignore
  @Test
  public void shouldReturnMostPopularTrajectories() throws JsonParseException, IOException
  {
    //Given A = (bbox,radius, lat,lon), B = (bbox,radius, lat,lon)  ,rel + (n.indegree  return sum

    // (node) -> n.
    // (relationship) -> r.

    //Brevemente n.indegree, n.outdegree

    StringBuilder url = new StringBuilder("neo4location/trajectories?");

    double e = 0.001; 

    RawData[] ab = getBbox(mTrajectories);

    RawData a = ab[0];
    RawData b = ab[1];

    StringBuilder sb = new StringBuilder();
    //A
    sb.append(String.format(Locale.ENGLISH, "lat=%f&lon=%f", a.getLatitude()-e, a.getLongitude()-e ));
    sb.append(String.format(Locale.ENGLISH, "&lat=%f&lon=%f", a.getLatitude()+e, a.getLongitude()+e ));
    //B
    sb.append(String.format(Locale.ENGLISH, "&lat=%f&lon=%f", b.getLatitude()-e, b.getLongitude()-e));
    sb.append(String.format(Locale.ENGLISH, "&lat=%f&lon=%f", b.getLatitude()+e, b.getLongitude()+e));
    url.append(sb);


    //SUM=n.indegree
    String property = "n.lat";
    url.append(String.format("&sum=%s", property));

//    TestSpatialParams testSpatialParams = new TestSpatialParams();
//    testSpatialParams.setA(new double[]{a.getLatitude()-e, a.getLongitude()-e, a.getLatitude()+e, a.getLongitude()+e});
//    testSpatialParams.setB(new double[]{b.getLatitude()-e, b.getLongitude()-e, b.getLatitude()+e, b.getLongitude()+e});    
//    Path filename = Paths.get(String.format("./datasets/tests/shouldReturnMostPopularTrajectories-%d-%d-%d-%d.json", mTest, mNumberOfUsers, mTrajectoriesPerUser, mMovesPerTrajectory));
//    byte[] json = objectMapper.writeValueAsBytes(testSpatialParams);
//    Files.write(filename, json, StandardOpenOption.CREATE_NEW);


    Iterable<Trajectory> trajectories  = httpGET(url.toString());
    assertTrajectoriesGivenABSize(trajectories);
  
  }

  private String getLatLonDistance(Trajectory[] trajectories) {

    StringBuilder sb = new StringBuilder();
    return sb.toString();

  }


  private RawData[] getBbox(Trajectory[] trajectories) {

    RawData[] ab = new RawData[2];


    boolean first = true;

    for(Trajectory trajectory : trajectories){

      int size=0;
      for(Move mv : trajectory.getMoves()){

        if(first){
          ab[0] = mv.getTo().getRawData();
          first = false;
        }

        size++;

        if(size == mSize){
          ab[1] = mv.getFrom().getRawData();
        }

      }

      //Nao remover
      break;
    }

    return ab;

  }

  private void assertTrajectoriesGivenABSize(
      Iterable<Trajectory> trajectories) {


    for(Trajectory trajectory : trajectories) {

      assertThat(trajectory.getTrajectoryName())
      .isNotNull()
      .isNotEmpty()
      .matches("\\d+");

      assertThat(trajectory.getUser())
      .isNotNull();

      Iterable<Move> moves = trajectory.getMoves();
      assertThat(moves)
      .hasSize(mSize)
      .doesNotContainNull();



    }

    //    for(Trajectory trajectory : trajectories){
    //
    //      assertThat(trajectory.getTrajectoryName())
    //      .isNotNull()
    //      .isNotEmpty()
    //      .matches("\\d+");
    //
    //      assertThat(trajectory.getUser())
    //      .isNotNull();
    //
    //      Collection<Move> moves = trajectory.getMoves();
    //      assertThat(moves)
    //      .hasSize(mSize)
    //      .doesNotContainNull();
    //
    //      //Falta testar retorno de latitude e longitude
    //
    //    }

  }


}
