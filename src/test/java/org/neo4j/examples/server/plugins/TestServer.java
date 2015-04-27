package org.neo4j.examples.server.plugins;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

import javax.ws.rs.core.MediaType;

import org.junit.Before;
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
import org.neo4location.server.plugins.Neo4LocationService;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;


@RunWith(Parameterized.class)
public class TestServer {

	private static final String FILENAME1 = "/home/sergio/Área de Trabalho/neo-server-ext/datasets/000/Trajectory/20081023025304.plt";
	private static final String FILENAME2 = "/home/sergio/Área de Trabalho/neo-server-ext/datasets/000/Trajectory/20081024020959.plt";

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

	@Before
	public void shouldCreateRawTrajectory() throws JsonParseException
	{

		//		try {
		//neo4j.withExtension("/neo4location", Neo4LocationService.class);
		//		} catch(Exception e){
		//			e.printStackTrace();
		//		}
		// Given
		URI serverURI = neo4j.httpURI();


		// When I access the server
		String personName = "s";
		String trajName = "s";

		String url = String.format("neo4location/users/%s/trajectories/%s/raw/points",personName,trajName);

		try {
			String json1 = Neo4LocationIO.PltToJson(TestServer.FILENAME1);

			String json2 = Neo4LocationIO.PltToJson(TestServer.FILENAME2);

			//HTTP.Response response = HTTP.POST(serverURI.resolve(url).toString(), json);

			Client client = Client.create();

			WebResource webResource = client.resource(serverURI.resolve(url).toString());

			// POST method
			ClientResponse response = webResource.accept(MediaType.TEXT_PLAIN)
					.type(MediaType.TEXT_PLAIN).post(ClientResponse.class, json1);

			ClientResponse response2 = webResource.accept(MediaType.TEXT_PLAIN)
					.type(MediaType.TEXT_PLAIN).post(ClientResponse.class, json2);


			assertEquals(200, response.getStatus());
		} catch (Exception e) {

			System.out.println(e.getMessage());
		}


		//TODO: 

	}


	@Test
	public void shouldReturnATrajectoryGivenLatLon() throws JsonParseException
	{
		//TODO: Falta testar resultado returnado
		// Given
		URI serverURI = neo4j.httpURI();
		//String expectedContent = "pong";

		// When I access the server
		String personName = "s";
		String trajName = "s";


		StringBuilder url = new StringBuilder(String.format("neo4location/users/%s/trajectories/%s?",personName,trajName));
		url.append("field=USERNAME&field=TRAJNAME&field=TIMESTAMP&field=LATITUDE&field=LONGITUDE");
		url.append("&rel=MOVE");

		//Start Node
		url.append(String.format(Locale.ENGLISH, "&lat=%f&lon=%f", 0.0f, 0.320f));
		url.append(String.format(Locale.ENGLISH, "&lat=%f&lon=%f", 50.0f, 120.310f));

		//End Node
		//		url.append(String.format(Locale.ENGLISH, "&lat=%f&lon=%f", 39.0f, 116.320f));
		//		url.append(String.format(Locale.ENGLISH, "&lat=%f&lon=%f", 40.0f, 116.310f));	

		//HTTP.Response response = HTTP.GET(serverURI.resolve(url.toString()).toString());


		Client client = Client.create();
		WebResource webResource = client.resource(serverURI.resolve(url.toString()).toString());
		ClientResponse response = webResource.accept(MediaType.APPLICATION_JSON)
				.type(MediaType.APPLICATION_JSON).get(ClientResponse.class);




		assertEquals(200, response.getStatus());
		assertEquals(200, getRawContent(response.getEntityInputStream()));
	}


	//	@Test
	//	public void shouldReturnATrajectoryGivenRadius() throws JsonParseException
	//	{
	//		//TODO: Testa radius 
	//		
	//	}


	private String getRawContent(InputStream in){

		StringBuilder sb = new StringBuilder();

		BufferedReader br = new BufferedReader(new InputStreamReader(in));

		br.lines().forEach((l) -> sb.append(l + "\n"));

		return sb.toString();
	}


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

}