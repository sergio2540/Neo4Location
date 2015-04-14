package org.neo4j.examples.server.plugins;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.neo4j.harness.junit.Neo4jRule;

import org.neo4j.server.rest.domain.JsonParseException;
import org.neo4j.test.Mute;
import org.neo4j.test.server.HTTP;
import org.neo4location.server.plugins.Neo4LocationService;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;


@RunWith(Parameterized.class)
public class TestIT {
	
	
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

	private String fixtureFile= "./fixture";
	
	@Rule
	public final Neo4jRule neo4j = new Neo4jRule()
	//.withFixture(new File(fixtureFile))
	.withExtension("/neo4location", Neo4LocationService.class);
	
	@Test
	public void shouldReturnRawTrajectory() throws JsonParseException
	{
		//TODO: 
		// Given
	    URI serverURI = neo4j.httpURI();
	    String expectedContent = "pong";
	    
	    // When I access the server
	    String personName = "s";
	    String trajName = "s";
	    
	    String url = String.format("neo4location/users/%s/trajectories/%s/raw",personName,trajName);
	    HTTP.Response response = HTTP.GET(serverURI.resolve(url).toString());

	    assertEquals(200, response.status());
	    
	}
	
	@Test
	public void shouldCreateRawTrajectory() throws JsonParseException
	{
		 
	   //TODO: 
	    
	}
	
	@Test
	public void shouldCreateSemanticTrajectory() throws JsonParseException
	{
		//TODO: 
	    
	}
	
	@Test
	//@Documented
	public void shouldReturnPong() throws JsonParseException
	{
	    // Given
	    URI serverURI = neo4j.httpURI();
	    String expectedContent = "pong";
	    // When I access the server
	    HTTP.Response response = HTTP.GET(serverURI.resolve("neo4location/ping").toString());

	    assertEquals(200, response.status());
	    assertEquals(expectedContent, response.rawContent());
	    
	}
}