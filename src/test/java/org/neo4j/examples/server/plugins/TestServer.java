package org.neo4j.examples.server.plugins;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.neo4j.graphdb.Label;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.server.rest.domain.JsonParseException;
import org.neo4j.test.Mute;
import org.neo4j.test.server.HTTP;
import org.neo4location.domain.Neo4LocationRelationships;
import org.neo4location.domain.trajectory.Move;
import org.neo4location.domain.trajectory.Point;
import org.neo4location.domain.trajectory.RawData;
import org.neo4location.domain.trajectory.SemanticData;
import org.neo4location.server.plugins.Neo4LocationService;
import org.supercsv.cellprocessor.ParseDate;
import org.supercsv.cellprocessor.ParseDouble;
import org.supercsv.cellprocessor.constraint.DMinMax;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;


@RunWith(Parameterized.class)
public class TestServer {

	private static final String FILENAME = "/home/sergio/Área de Trabalho/neo-server-ext/datasets/000/Trajectory/20081023025304.plt";

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

	//private String fixtureFile= "/home/sergio/Área de Trabalho/neo-server-ext/fixtures/fixture";

	@Rule
	public final Neo4jRule neo4j = new Neo4jRule()
	//.withFixture(new File(fixtureFile))
	.withExtension("/neo4location", Neo4LocationService.class);



	/**
	 * Sets up the processors used for the examples. There are 10 CSV columns, so 10 processors are defined. Empty
	 * columns are read as null (hence the NotNull() for mandatory columns).
	 * 
	 * @return the cell processors
	 */
	private static CellProcessor[] getProcessors() {
 
		 //		final String emailRegex = "[a-z0-9\\._]+@[a-z0-9\\.]+"; // just an example, not very robust!
		//		StrRegEx.registerMessage(emailRegex, "must be a valid email address");
		//Line 1…6 are useless in this dataset, and can be ignored. Points are described in following lines, one for each line.
		
		final CellProcessor[] processors = new CellProcessor[] { 
				new NotNull(new DMinMax(-90.0, 90.0)), // lat
				new NotNull(new DMinMax(-180.0, 180.0)), // lon
				new NotNull(new ParseDouble()), // set to 0
				new NotNull(new ParseDouble()), // alt
				new NotNull(new ParseDouble()), // date - number of days (with fractional part) that have passed since 12/30/1899.

				new ParseDate("yyyy-MM-dd"), // date as string
				new ParseDate("HH:mm:ss"), // time as string

		};

		return processors;
	}

	private static List<Move> CsvListPointReader(String filename) throws Exception {

		//TODO: Skyp first 6 lines
		//CommentMatcher commentMatcher = new CommentStartsWith("s");
		//final CsvPreference STANDARD_SKIP_COMMENTS = 
		//new CsvPreference.Builder(CsvPreference.STANDARD_PREFERENCE).skipComments(commentMatcher).build();


		List<Move> moves = new ArrayList<>();
		ICsvListReader beanReader = null;
		try  {


			beanReader  = new CsvListReader(new FileReader(filename), CsvPreference.STANDARD_PREFERENCE);
			final CellProcessor[] processors = getProcessors();

			Move move = null;
			Point from;
			Point to = null;
			List<Object> tp;
			boolean first = true;
			boolean _first = true;
			
			while( (tp = beanReader.read(processors)) != null ) {

				//				System.out.println(String.format("lineNo=%s, rowNo=%s, customer=%s", beanReader.getLineNumber(),
				//						beanReader.getRowNumber(), tp));

				double lat = (double) tp.get(0);
				double lon = (double) tp.get(1);
				tp.get(2); //IGNORE
				double alt = (double) tp.get(3);
				tp.get(4); //IGNORE

				Date ymd = (Date) tp.get(5);
				Date hms = (Date) tp.get(6);


				long timestamp =  Instant.ofEpochMilli(ymd.getTime()).plusMillis(Instant.ofEpochMilli(hms.getTime()).toEpochMilli()).toEpochMilli();

				float accuracy = 0;
				float speed = 0;

				RawData rd = new RawData(lat, lon, alt, accuracy, speed, timestamp);

				Map<String, Object> props = new HashMap<>();
				SemanticData sd = new SemanticData(props);
				List<Label> labels = new ArrayList<>();
				
				Neo4LocationRelationships rel = Neo4LocationRelationships.MOVE;
				
				if(first){
					to = new Point(rd, sd, labels);
					first = false;
				}
				else {
					from = to;
					to = new Point(rd, sd, labels);
					
					if(_first){
						rel = Neo4LocationRelationships.FROM;
						_first = false;
					}
					
					move = Move.create(rel, from, to, props);
					moves.add(move);
				}



			}

		} finally {

			if (beanReader != null) beanReader.close();

		}

		return moves;

	}

	@Test
	public void shouldReturnRawTrajectory() throws JsonParseException
	{
		//TODO: Falta testar resultado returnado
		// Given
		URI serverURI = neo4j.httpURI();
		//String expectedContent = "pong";

		// When I access the server
		String personName = "s";
		String trajName = "s";

		String url = String.format("neo4location/users/%s/trajectories/%s?field=personName&field=lat",personName,trajName);
		HTTP.Response response = HTTP.GET(serverURI.resolve(url).toString());

		assertEquals(200, response.status());
		assertEquals(200, response.rawContent());
	}

	@Before
	public void shouldCreateRawTrajectory() throws JsonParseException
	{

		// Given
		URI serverURI = neo4j.httpURI();


		// When I access the server
		String personName = "s";
		String trajName = "s";

		String url = String.format("neo4location/users/%s/trajectories/%s/raw/points",personName,trajName);

		List<Move> mvs = new ArrayList<Move>();

		try {
			mvs = TestServer.CsvListPointReader(FILENAME);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String json = "";
		ObjectWriter mapper = new ObjectMapper().writerWithDefaultPrettyPrinter();
		try {
			
			mapper.writeValue(new File("test.json"), mvs);
			json = mapper.writeValueAsString(mvs);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		


		//HTTP.Response response = HTTP.POST(serverURI.resolve(url).toString(), json);

		
		Client client = Client.create();

		WebResource webResource = client.resource(serverURI.resolve(url).toString());
		
		// POST method
		ClientResponse response = webResource.accept(MediaType.TEXT_PLAIN)
				.type(MediaType.TEXT_PLAIN).post(ClientResponse.class, json);

		
		assertEquals(200, response.getStatus());
		//TODO: 

	}

//	@Test
//	public void shouldCreateSemanticTrajectory() throws JsonParseException
//	{
//		//TODO: 
//
//	}

//	@Test
//	//@Documented
//	public void shouldReturnPong() throws JsonParseException
//	{
//		// Given
//		URI serverURI = neo4j.httpURI();
//		String expectedContent = "pong";
//		// When I access the server
//		HTTP.Response response = HTTP.GET(serverURI.resolve("neo4location/ping").toString());
//
//		assertEquals(200, response.status());
//		assertEquals(expectedContent, response.rawContent());
//
//	}
}