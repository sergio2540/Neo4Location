package org.neo4j.examples.server.plugins;

import static org.junit.Assert.assertEquals;
import static org.assertj.core.api.Assertions.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
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
import org.neo4j.harness.ServerControls;
import org.neo4j.harness.TestServerBuilders;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.server.rest.domain.JsonParseException;
import org.neo4j.test.Mute;
import org.neo4location.domain.trajectory.Move;
import org.neo4location.domain.trajectory.Trajectory;
import org.neo4location.server.plugins.Neo4LocationService;
import org.neo4location.utils.Neo4LocationTestsUtils;
import org.supercsv.cellprocessor.ParseBigDecimal;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;


@RunWith(Parameterized.class)
public class TestUser {

	private static final int ITERATIONS = 7;

	//Max 8
	private static final int START_USERS = 8;
	private static final int INC_USERS = 0;

	private static final int START_TRAJECTORIES_PER_USER = 5;
	private static final int INC_TRAJECTORIES_PER_USER = 0;

	private static final int START_MOVES_PER_TRAJECTORY = 6;
	private static final int INC_MOVES_PER_TRAJECTORY = 0;

	@Parameters(name = "{index}:")
	public static Collection<Object[]> data() {

		Collection<Object[]> col = new ArrayList<Object[]>();

		int numberOfUsers = START_USERS;
		int trajectoriesPerUser = START_TRAJECTORIES_PER_USER;
		int movesPerTrajectory =  START_MOVES_PER_TRAJECTORY;

		for(int i = 0; i < ITERATIONS;  i++){

			col.add(new Object[] { 0, numberOfUsers, trajectoriesPerUser, movesPerTrajectory, i});	

			numberOfUsers += INC_USERS;
			trajectoriesPerUser += INC_TRAJECTORIES_PER_USER;
			movesPerTrajectory += INC_MOVES_PER_TRAJECTORY;

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
		.withExtension("/neo4location", Neo4LocationService.class);

	//public static ServerControls mNeo4j;


	//Mudar para BeforeClass
	@Before
	public void shouldCreateTrajectory() throws Exception
	{
		
		//reporter.start(1, TimeUnit.SECONDS);	
		
//		mNeo4j = TestServerBuilders.newInProcessBuilder()
//				.withExtension("/neo4location", Neo4LocationService.class)
//				//.withConfig("dbms.security.auth_enabled", "false")
//				.newServer();

		
		String url = "neo4location/trajectories";
		mTrajectories = Neo4LocationTestsUtils.createTrajectory(mNumberOfUsers, mTrajectoriesPerUser, mMovesPerTrajectory);
		String json = Neo4LocationTestsUtils.trajectoriesToJson(mTrajectories);

		//String filename = String.format("./create-%d-%d-%d.json", mNumberOfUsers, mTrajectoriesPerUser, mMovesPerTrajectory);
		//Files.write(Paths.get(filename), json.getBytes());

		com.squareup.okhttp.Response response = Neo4LocationTestsUtils.POST(mNeo4j.httpURI(), url.toString(),json);

		//assertEquals(201, response.getStatus());

	}


	@Test
	public void shouldReturnTrajectories() throws JsonParseException, IOException
	{
		StringBuilder url = new StringBuilder("neo4location/trajectories?");

		String username = mTrajectories[mUser*mTrajectoriesPerUser].getUser().getUsername();



		url.append(String.format("username=%s", username));
		url.append(String.format("&timestamp=0", username));


		Collection<Trajectory> trajectories = httpGET(url.toString());
		assertTrajectoriesGivenUser(trajectories, 1);
		assertTrajectoriesGivenOnlyOneUser(trajectories);

	}


	@Test
	public void shouldReturnAllTrajectories() throws JsonParseException, IOException
	{

		StringBuilder url = new StringBuilder("neo4location/trajectories");

		Collection<Trajectory> trajectories = httpGET(url.toString());

		assertTrajectoriesGivenUser(trajectories, mNumberOfUsers);

	}

	//Testar melhor skip, limit, orderBy
	@Test
	public void shouldSkipTrajectories() throws JsonParseException, IOException
	{
		int skip = 2;

		StringBuilder url = new StringBuilder("neo4location/trajectories?");

		String username = mTrajectories[mUser*mTrajectoriesPerUser].getUser().getUsername();
		url.append(String.format("username=%s", username));

		url.append(String.format("&skip=%d", skip));
		Collection<Trajectory> trajectories = httpGET(url.toString());

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
		Collection<Trajectory> trajectories = httpGET(url.toString());

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

		Collection<Trajectory> trajectories = httpGET(url.toString());


		assertThat(trajectories)
		.hasSize(limit)
		.doesNotContainNull();


		Trajectory prevTraj = null;  
		boolean first = true;

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

	private Collection<Trajectory> httpGET(String url) throws JsonGenerationException, JsonMappingException, IOException{

		com.squareup.okhttp.Response response = Neo4LocationTestsUtils.GET(mNeo4j.httpURI(), url);

		//MultivaluedMap<String, String> s = response.getHeaders();

		//assertThat(s).containsKey("application/x-gzip");

		Collection<Trajectory> trajectories = Neo4LocationTestsUtils.getStreamingCollection(response);

		//Files.write(Paths.get("./get-%d-%d-%d.json"), json.getBytes());

		assertThat(response.code())
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

			String username = mTrajectories[mUser*mTrajectoriesPerUser].getUser().getUsername();

			assertThat(trajectory.getUser().getUsername())
			.isNotNull()
			.isNotEmpty()
			.matches("\\d+")
			.isEqualTo(username);


			Collection<Move> moves = trajectory.getMoves();
			assertThat(moves)
			.hasSize(mMovesPerTrajectory)
			.doesNotContainNull();

			//Falta testar retorno de latitude e longitude

		}
	}
}