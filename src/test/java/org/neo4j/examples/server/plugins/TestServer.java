package org.neo4j.examples.server.plugins;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.MediaType;

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
import org.neo4location.domain.trajectory.Trajectory;
import org.neo4location.server.plugins.Neo4LocationService;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;


@RunWith(Parameterized.class)
public class TestServer {

	private static final String FILENAME1 = "/home/sergio/Área de Trabalho/neo-server-ext/datasets/000/Trajectory/20081023025304.plt";
	private static final String FILENAME2 = "/home/sergio/Área de Trabalho/neo-server-ext/datasets/000/Trajectory/20081024020959.plt";

	private static  final MetricRegistry metrics = new MetricRegistry();

	private static final Timer responses = metrics.timer("responses");

	private static final ConsoleReporter reporter = ConsoleReporter.forRegistry(metrics)
			.convertRatesTo(TimeUnit.SECONDS)
			.convertDurationsTo(TimeUnit.MILLISECONDS)
			.build();


	@Parameters(name = "{index}: METHOD {1} : {2}")
	public static Collection<Object[]> data() {

		return Arrays.asList(new Object[][] {  

				{ 0, "GET", "/neo4location/ping", "" } 

		});

	}

	@Parameter // first data value (0) is default
	public /* NOT private */ int test;

	@Parameter(value = 1)
	public /* NOT private */ String method;

	@Parameter(value = 2)
	public /* NOT private */ String uri;

	@Parameter(value = 3)
	public /* NOT private */ String payload;


	@Rule public Mute mute = Mute.muteAll();

	@Rule
	public final Neo4jRule neo4j = new Neo4jRule()
	.withExtension("/neo4location", Neo4LocationService.class);

	@BeforeClass
	public static void init(){
		//reporter.start(1, TimeUnit.SECONDS);	
	}


	private ClientResponse createRawTrajectory(WebResource webResource, String json){

		// POST method
		Timer.Context context = responses.time();
		ClientResponse response = webResource.accept(MediaType.APPLICATION_JSON)
				.type(MediaType.APPLICATION_JSON).post(ClientResponse.class, json);
		context.stop();

		return response;
	}


	@Before
	public void shouldCreateRawTrajectory() throws JsonParseException
	{

		String url = "neo4location/trajectories";
		Neo4LocationIO nio = new Neo4LocationIO();
		String [] pltFilenames = {TestServer.FILENAME1, TestServer.FILENAME2};
		
		// When I access the server
		String personName = "s";
		String trajName = "s";

		try {

			Collection<Trajectory> trajectories = nio.PltToTrajectory(trajName, personName, pltFilenames);

			String json = nio.TrajectoriesToJson(trajectories);

			ClientResponse response = POST(url.toString(),json);
			
			assertEquals(200, response.getStatus());

		} catch (Exception e) {

			System.out.println(e.getMessage());
		} 

	}


	@Test
	public void shouldReturnUsers() throws JsonParseException, IOException
	{
		//Given bbox + time return users
		
		// When I access the server
		String personName = "s";
		String trajName = "s";


		StringBuilder url = new StringBuilder("neo4location/trajectories?");

		url.append("field=USERNAME&field=TRAJNAME&field=TIMESTAMP&field=lat&field=lon");

		url.append("&rel=MOVE&nw=lat");

		//Start Node
		url.append(String.format(Locale.ENGLISH, "&lat=%f&lon=%f", 0.0f, 0.320f));
		url.append(String.format(Locale.ENGLISH, "&lat=%f&lon=%f", 45.99f, 120.310f));

		//End Node
		//		url.append(String.format(Locale.ENGLISH, "&lat=%f&lon=%f", 39.0f, 116.320f));
		//		url.append(String.format(Locale.ENGLISH, "&lat=%f&lon=%f", 40.0f, 116.310f));	


		ClientResponse response = GET(url.toString());

		
		String outJson = getRawContent(response.getEntityInputStream());
		Files.write(Paths.get("./shouldReturnUsers.out.json"), outJson.getBytes());

		assertEquals(200, response.getStatus());

	}


	//	@Test
	//	public void shouldReturnATrajectoryGivenRadius() throws JsonParseException
	//	{
	//		//TODO: Testa radius 
	//		
	//	}

	@Test
	public void shouldReturnKPositions() throws JsonParseException, IOException
	{

		//Given user+traj+k return list with 1 traj with k last trajs
		
		String personId = "s";
		String trajectoryId = "s";

		StringBuilder url = new StringBuilder("neo4location/trajectories?");
		url.append(String.format("&trajectory=s&person=s&nw=lat",personId,trajectoryId));

		
		ClientResponse response = GET(url.toString());
		
		
		String outJson = getRawContent(response.getEntityInputStream());
		Files.write(Paths.get("./shouldReturnKPositions.out.json"), outJson.getBytes());

		
		assertEquals(200, response.getStatus());




	}

	
	@Test
	public void shouldReturnMostPopularTrajectories() throws JsonParseException, IOException
	{
		//Given rel+prop return sum
		
		String relation = "MOVE";
		String property = "n.lat";
		
		// (node) -> n.
		//(relationship) -> r.
		
		//Brevemente n.degree
		
		StringBuilder url = new StringBuilder("neo4location/trajectories?");
		url.append(String.format("&rel=%s&sum=%s", relation, property));

		
		ClientResponse response = GET(url.toString());
		String outJson = getRawContent(response.getEntityInputStream());
		Files.write(Paths.get("./shouldReturnMostPopularTrajectories.out.json"), outJson.getBytes());
		assertEquals(200, response.getStatus());
	
	}


	private ClientResponse POST(String url, String json){

		URI serverURI = neo4j.httpURI();
		Client client = Client.create();
		WebResource webResource = client.resource(serverURI.resolve(url.toString()).toString());
		ClientResponse response = webResource.accept(MediaType.APPLICATION_JSON)
				.type(MediaType.APPLICATION_JSON).post(ClientResponse.class, json);
		
		return response;

	}

	private ClientResponse GET(String url){

		URI serverURI = neo4j.httpURI();
		Client client = Client.create();
		WebResource webResource = client.resource(serverURI.resolve(url.toString()).toString());
		return webResource.accept(MediaType.APPLICATION_JSON)
				.type(MediaType.APPLICATION_JSON).get(ClientResponse.class);

	}

	private String getRawContent(InputStream in){

		StringBuilder sb = new StringBuilder();

		BufferedReader br = new BufferedReader(new InputStreamReader(in));

		br.lines().forEach((l) -> sb.append(l + "\n"));

		return sb.toString();
	}

	/*  
    @Test
	public void shouldReturnPong() throws JsonParseException
	{
		// Given
		URI serverURI = neo4j.httpURI();
		String expectedContent = "pong\n";
		// When I access the server
		//HTTP.Response response = HTTP.GET(serverURI.resolve("neo4location/ping").toString());

		Client client = Client.create();
		WebResource webResource = client.resource(serverURI.resolve("neo4location/ping").toString());
		ClientResponse response = webResource.accept(MediaType.APPLICATION_JSON)
				.type(MediaType.APPLICATION_JSON).get(ClientResponse.class);

		assertEquals(200, response.getStatus());

		String content = getRawContent(response.getEntityInputStream());
		assertEquals(expectedContent, content);




	}
	 */

}