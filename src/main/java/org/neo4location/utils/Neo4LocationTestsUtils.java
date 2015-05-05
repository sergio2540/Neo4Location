package org.neo4location.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;


















import org.neo4location.domain.Neo4LocationLabels;
import org.neo4location.domain.Neo4LocationRelationships;
import org.neo4location.domain.trajectory.Move;
import org.neo4location.domain.trajectory.Point;
import org.neo4location.domain.trajectory.RawData;
import org.neo4location.domain.trajectory.SemanticData;
import org.neo4location.domain.trajectory.Trajectory;
import org.neo4location.domain.trajectory.User;
import org.supercsv.cellprocessor.ParseDate;
import org.supercsv.cellprocessor.ParseDouble;
import org.supercsv.cellprocessor.constraint.DMinMax;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.comment.CommentMatcher;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;
import org.xerial.snappy.SnappyFramedInputStream;
import org.xerial.snappy.SnappyInputStream;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.pool.KryoCallback;
import com.esotericsoftware.kryo.pool.KryoFactory;
import com.esotericsoftware.kryo.pool.KryoPool;
import com.esotericsoftware.kryo.serializers.BeanSerializer;
import com.esotericsoftware.kryo.serializers.CollectionSerializer;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;





public class Neo4LocationTestsUtils {


	public static final String DATA_DIR = "./Data/";
	public static final String TRAJECTORY_DIR = "/Trajectory/";


	private static CellProcessor[] getProcessors() {

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

	private static Collection<Move> csvListPointReader(String trajectory, String user, int numberOfMoves) throws Exception {

		//TODO: Skyp first 6 lines
		CommentMatcher commentMatcher = new CommentMatcher(){

			@Override
			public boolean isComment(String line) {

				boolean skip = false;


				if(line.startsWith("G") || 
						line.startsWith("W") || 
						line.startsWith("A") ||
						line.startsWith("R") ||
						//line.startsWith("0,2,255,My") ||
						line.startsWith("0")){

					skip = true;
					//System.out.println("matched line: " + line);
				}


				return skip;
			}


		};

		final CsvPreference STANDARD_SKIP_COMMENTS = new CsvPreference.Builder(CsvPreference.STANDARD_PREFERENCE).skipComments(commentMatcher).build();


		List<Move> moves = new ArrayList<>();
		ICsvListReader beanReader = null;

		String ext = ".plt";
		String filename = Paths.get(DATA_DIR, user, TRAJECTORY_DIR, trajectory).toAbsolutePath().toString();


		beanReader  = new CsvListReader(new FileReader(filename + ext), STANDARD_SKIP_COMMENTS);
		final CellProcessor[] processors = getProcessors();

		Move move = null;
		Point from;
		Point to = null;
		List<Object> tp;
		boolean first = true;
		//boolean _first = true;

		while( (tp = beanReader.read(processors)) != null && moves.size() != numberOfMoves ) {

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
			//SemanticData sd = new SemanticData(props);
			List<Neo4LocationLabels> labels = new ArrayList<>();

			Neo4LocationRelationships rel = Neo4LocationRelationships.MOVE;

			if(first){
				to = new Point(rd, props, labels);
				first = false;
			}
			else {

				from = to;
				to = new Point(rd, props, labels);


				move = new Move(rel, from, to, props);
				moves.add(move);

			}






		}

		return moves;

	}


	//	public static String createUser(int u){
	//
	//		StringBuilder sb = new StringBuilder();
	//		if(u < 10){
	//			sb.append("00");
	//		}else if(u < 100){
	//			sb.append("0");
	//		} 
	//
	//		sb.append(u);
	//
	//		return sb.toString();
	//
	//	}

	private static String[] getTrajectories(final String  trajDir, final int trajectoriesNumber) throws IOException{

		String [] trajectories =  new String[trajectoriesNumber];

		//GET LIST OF ALL FILES IN

		//System.out.println(trajDir);

		DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(trajDir).toAbsolutePath());

		int i= 0;
		for (Path path : directoryStream) {

			if(i >= trajectoriesNumber){
				break;
			}

			String trajectory = path.getFileName().toString();
			trajectories[i] = trajectory.substring(0, trajectory.length()- 4);



			i++;
		}




		return trajectories;

	}

	private static String[] getUsers(String dataFile, int numberOfUsers) throws IOException {

		String [] usernames =  new String[numberOfUsers];

		//GET LIST OF ALL FILES IN

		DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(dataFile).toAbsolutePath());

		int i= 0;
		for (Path path : directoryStream) {

			if(i >= numberOfUsers){
				break;
			}

			String username = path.toString();
			usernames[i] = path.getFileName().toString();
			//;.substring(username.length()-3);
			i++;
		}

		return usernames;
	}



	public static Trajectory [] createTrajectory(int numberOfUsers, int trajectoriesPerUser, int movesPerTrajectory) throws Exception {

		Trajectory [] trajectories = new Trajectory[numberOfUsers*trajectoriesPerUser];
		int index = 0;

		String [] usernames = getUsers(DATA_DIR,numberOfUsers);

		for(int u=0; u < numberOfUsers; u++){

			String username = usernames[u];

			//System.out.println("username: " + username);

			String trajectoriesDirectory = DATA_DIR + username + TRAJECTORY_DIR;
			String[] trajectoryNames = getTrajectories(trajectoriesDirectory, trajectoriesPerUser);

			User user = new User(username);

			for(int t=0; t < trajectoriesPerUser; t++){

				//System.out.println("trajectory: " + trajectoryNames[t]);

				Collection<Move> moves = csvListPointReader(trajectoryNames[t], username, movesPerTrajectory);
				//				System.out.println(index);
				//				System.out.println(moves);

				Map<String,Object> props = new HashMap<String,Object>();
				props.put("error", 0.1);
				trajectories[index++] = new Trajectory(trajectoryNames[t], user, moves, props);	

			}

		}

		return trajectories;
	}



	public static String trajectoriesToJson(Trajectory [] trajectories) throws JsonGenerationException, JsonMappingException, IOException{

		ObjectWriter mapper = new ObjectMapper().writerWithDefaultPrettyPrinter();

		return mapper.writeValueAsString(trajectories);
	}


	public static Collection<Trajectory> JsonTotrajectories(String json) throws JsonGenerationException, JsonMappingException, IOException{

		ObjectMapper mapper = new ObjectMapper();
		Collection<Trajectory> trajectories = new HashSet<Trajectory>();

		trajectories = mapper.readValue(json, new TypeReference<Collection<Trajectory>>(){});

		return trajectories;
	}



	//	String post(String url, String json) throws IOException {
	//
	//	}

	public static OkHttpClient client = new OkHttpClient();

	public static Response POST(URI serverURI, String url, String json) throws IOException{

		final MediaType JSON = MediaType.parse("application/json; charset=utf-8");



		RequestBody body = RequestBody.create(JSON, json);

		Request request = new Request.Builder()
		.url(serverURI.resolve(url).toString())
		.post(body)
		.build();

		Response response = client.newCall(request).execute();

		return response;

	}

	public static Response GET(URI serverURI, String url) throws IOException{

		Request request = new Request.Builder()
		.url(serverURI.resolve(url).toString())
		.build();

		Response response = client.newCall(request).execute();

		return response;
	}

	//Registo e iniciação so precisa de ser feita uma vez
	private final static KryoFactory mFactory = new KryoFactory() {
		public Kryo create () {
			Kryo kryo = new Kryo();
			// configure kryo instance, customize settings
			return kryo;
		}
	}; 

	// Build pool with SoftReferences enabled (optional)
	private final static KryoPool mPool = new KryoPool.Builder(mFactory).softReferences().build();


	public static Collection<Trajectory> getStreamingCollection(Response res) throws JsonParseException, JsonMappingException, IOException{

	
		InputStream in = res.body().byteStream();
		Collection<Trajectory> trajectories;
		
		//TODO: Olhar para o header para perceber que formata esta a ser enviado
		boolean isKryo = false;

		if(isKryo){
			
			InputStream snappyIn = new SnappyFramedInputStream(in);
			Input i = new Input(snappyIn);
			trajectories = mPool.run(new KryoCallback<Collection<Trajectory>>() {
				public Collection<Trajectory> execute(Kryo kryo) {
					return (Collection<Trajectory>) kryo.readClassAndObject(i);
				}
			});
			
		} else {

			ObjectMapper mapper = new ObjectMapper();
			trajectories = mapper.readValue(in, new TypeReference<Collection<Trajectory>>(){});
		
		}
		
		return trajectories;

	}

	public static String getRawContent(Response res) throws IOException{



		StringBuilder sb = new StringBuilder();

		BufferedReader br = new BufferedReader(new InputStreamReader(res.body().byteStream()));


		br.lines().forEach((l) -> sb.append(l + "\n"));

		return sb.toString();
	}

}
