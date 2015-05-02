package org.neo4j.examples.server.plugins;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

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
import org.neo4location.domain.Neo4LocationIO;
import org.neo4location.domain.trajectory.Move;
import org.neo4location.domain.trajectory.Point;
import org.neo4location.domain.trajectory.RawData;
import org.neo4location.domain.trajectory.Trajectory;
import org.neo4location.server.plugins.Neo4LocationService;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.sun.jersey.api.client.ClientResponse;

@RunWith(Parameterized.class)
public class TestSpatial {

	//Max START_MOVES_PER_TRAJECTORY - 2
	private static final int ITERATIONS = 4;

	//Max 8
	private static final int START_USERS = 3;
	private static final int INC_USERS = 0;

	private static final int START_TRAJECTORIES_PER_USER = 5;
	private static final int INC_TRAJECTORIES_PER_USER = 0;

	private static final int START_MOVES_PER_TRAJECTORY = 10;
	private static final int INC_MOVES_PER_TRAJECTORY = 0;

	@Parameters(name = "{index}:")
	public static Collection<Object[]> data() {

		Collection<Object[]> col = new ArrayList<Object[]>();

		int numberOfUsers = START_USERS;
		int trajectoriesPerUser = START_TRAJECTORIES_PER_USER;
		int movesPerTrajectory =  START_MOVES_PER_TRAJECTORY;

		for(int i = 0; i < ITERATIONS;  i++){

			col.add(new Object[] { 0, numberOfUsers, trajectoriesPerUser, movesPerTrajectory, i+1});	

			numberOfUsers += INC_USERS;
			trajectoriesPerUser += INC_TRAJECTORIES_PER_USER;
			movesPerTrajectory += INC_MOVES_PER_TRAJECTORY;

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
	.withExtension("/neo4location", Neo4LocationService.class);

	@BeforeClass
	public static void init(){
		//reporter.start(1, TimeUnit.SECONDS);	
	}


	//Mudar para BeforeClass
	@Before
	public void shouldCreateTrajectory() throws Exception
	{

		System.err.println(mNumberOfUsers);
		String url = "neo4location/trajectories";
		mTrajectories = Neo4LocationIO.createTrajectory(mNumberOfUsers, mTrajectoriesPerUser, mMovesPerTrajectory);
		String json = Neo4LocationIO.trajectoriesToJson(mTrajectories);

		String filename = String.format("./create-%d-%d-%d.json", mNumberOfUsers, mTrajectoriesPerUser, mMovesPerTrajectory);
		Files.write(Paths.get(filename), json.getBytes());

		ClientResponse response = Neo4LocationIO.POST(mNeo4j.httpsURI(), url.toString(),json);

		assertEquals(201, response.getStatus());

	}



	private Collection<Trajectory> httpGET(String url) throws JsonGenerationException, JsonMappingException, IOException{

		ClientResponse response = Neo4LocationIO.GET(mNeo4j.httpsURI(), url);
		String json = Neo4LocationIO.getRawContent(response.getEntityInputStream());

		//Files.write(Paths.get("./create-%d-%d-%d.json.json"), json.getBytes());

		Collection<Trajectory> trajectories = Neo4LocationIO.JsonTotrajectories(json);

		assertThat(response.getStatus())
		.isEqualTo(Response.Status.OK.getStatusCode());

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


			Collection<Move> moves = trajectory.getMoves();
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

	

	@Test
	public void shouldReturnTrajectoryAtoB() throws JsonParseException, IOException
	{
		
		StringBuilder url = new StringBuilder("neo4location/trajectories?");
		
		url.append(getBbox(mTrajectories));
		
		Collection<Trajectory> trajectories  = httpGET(url.toString());

		assertTrajectoriesGivenABSize(trajectories);


	}


	private String getLatLonDistance(Trajectory[] trajectories) {
		
		StringBuilder sb = new StringBuilder();
		
		return sb.toString();
		
	}
	
	
	private String getBbox(Trajectory[] trajectories) {

		RawData[] ab = new RawData[2];
		StringBuilder sb = new StringBuilder();
		
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
			
			
			break;
		}
		
		double e = 0.001; 
	
		
		RawData a = ab[0];
		RawData b = ab[1];

		//A
		sb.append(String.format(Locale.ENGLISH, "lat=%f&lon=%f", a.getLatitude()-e, a.getLongitude()-e ));
		sb.append(String.format(Locale.ENGLISH, "&lat=%f&lon=%f", a.getLatitude()+e, a.getLongitude()+e ));

		//B
		sb.append(String.format(Locale.ENGLISH, "&lat=%f&lon=%f", b.getLatitude()-e, b.getLongitude()-e));
		sb.append(String.format(Locale.ENGLISH, "&lat=%f&lon=%f", b.getLatitude()+e, b.getLongitude()+e));


		return sb.toString();
	}

	private void assertTrajectoriesGivenABSize(
			Collection<Trajectory> trajectories) {

		for(Trajectory trajectory : trajectories){

			assertThat(trajectory.getTrajectoryName())
			.isNotNull()
			.isNotEmpty()
			.matches("\\d+");

			assertThat(trajectory.getUser())
			.isNotNull();

			Collection<Move> moves = trajectory.getMoves();
			assertThat(moves)
			.hasSize(mSize)
			.doesNotContainNull();

			//Falta testar retorno de latitude e longitude

		}

	}


	@Test
	public void shouldReturnMostPopularTrajectories() throws JsonParseException, IOException
	{
		//Given A = (bbox,radius, lat,lon), B = (bbox,radius, lat,lon)  ,rel + (n.indegree  return sum

		// (node) -> n.
		// (relationship) -> r.

		//Brevemente n.indegree, n.outdegree

		StringBuilder url = new StringBuilder("neo4location/trajectories?");

		url.append(getBbox(mTrajectories));
		
		//SUM=n.indegree
		String property = "n.lat";
		url.append(String.format("&sum=%s", property));

		
		
		Collection<Trajectory> trajectories  = httpGET(url.toString());
		assertTrajectoriesGivenABSize(trajectories);


	}


}
