package org.neo4location.utils;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
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
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import java.util.zip.GZIPOutputStream;

import org.neo4location.domain.Neo4LocationLabels;
import org.neo4location.domain.Neo4LocationRelationships;
import org.neo4location.domain.trajectory.Move;
import org.neo4location.domain.trajectory.Person;
import org.neo4location.domain.trajectory.Point;
import org.neo4location.domain.trajectory.RawData;
import org.neo4location.domain.trajectory.Trajectory;
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

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.pool.KryoCallback;
import com.esotericsoftware.kryo.pool.KryoFactory;
import com.esotericsoftware.kryo.pool.KryoPool;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;





public class Neo4LocationTestsUtils {


	private static final String DATA_DIR = "./Data/";
	private static final String TRAJECTORY_DIR = "/Trajectory/";

	//Registo e iniciação so precisa de ser feita uma vez
	private final static KryoFactory mFactory = new KryoFactory() {
		public Kryo create () {
			Kryo kryo = new Kryo();
			// configure kryo instance, customize settings
			return kryo;
		}
	}; 
	
	private static ObjectMapper objectMapper = new ObjectMapper();
	private static ObjectWriter mObjectWriter = objectMapper.writerFor(Trajectory[].class);
	
	private static CollectionType ct = objectMapper.getTypeFactory().constructCollectionType(Collection.class, Trajectory.class);
	private static ObjectReader mObjectReader = objectMapper.reader(ct);
	
	
	

	// Build pool with SoftReferences enabled (optional)
	private final static KryoPool mPool = new KryoPool.Builder(mFactory).softReferences().build();



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
			
			final Double alt = (tp.get(3) == null) ? null : (double)tp.get(3);
			
			tp.get(4); //IGNORE

			Date ymd = (Date) tp.get(5);
			Date hms = (Date) tp.get(6);


			long timestamp =  Instant.ofEpochMilli(ymd.getTime()).plusMillis(Instant.ofEpochMilli(hms.getTime()).toEpochMilli()).toEpochMilli();

			Float accuracy = null;
			Float speed = null;
			Float bearing = null;

			RawData rd = new RawData(lat, lon, timestamp, alt, accuracy, speed, bearing);

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
		
		beanReader.close();

		return moves;

	}

	private static Collection<String> getTrajectories(final String  trajDir, final int numberOfTrajectories) throws IOException{

		DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(trajDir).toAbsolutePath());

		return StreamSupport.stream(directoryStream.spliterator(), false)
				.limit(numberOfTrajectories)
				.parallel()
				.map((path) -> {

					String trajectory = path.getFileName().toString();
					return trajectory.substring(0, trajectory.length()- 4);

				}).collect(Collectors.toList());

	}

	private static Collection<String> getUsers(String dataFile, int numberOfUsers) throws IOException {


		//GET LIST OF ALL FILES IN

		DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(dataFile).toAbsolutePath());

		return StreamSupport.stream(directoryStream.spliterator(), false)
				.limit(numberOfUsers)
				.parallel()
				.map((path) -> {

					return path.getFileName().toString();

				}).collect(Collectors.toList());
	}



	public static Trajectory [] createTrajectory(int numberOfUsers, int trajectoriesPerUser, int movesPerTrajectory) throws Exception {

		//Trajectory [] trajectories = new Trajectory[numberOfUsers*trajectoriesPerUser];
		//int index = 0;

		Collection<String> usernames = getUsers(DATA_DIR,numberOfUsers);

		Collection<Object> trajectories = usernames.stream()
				.parallel()
				.map((username)-> {

					Person person = new Person(username);

					Collection<Trajectory> trajs = new ArrayList<Trajectory>();

					String trajectoriesDirectory = DATA_DIR + username + TRAJECTORY_DIR;



					try {

						Collection<String> trajectoryNames = getTrajectories(trajectoriesDirectory, trajectoriesPerUser);

						trajs = trajectoryNames.stream()
						.parallel()
						.map((trajectoryName) -> {

							Collection<Move> moves = null;

							try {
								moves = csvListPointReader(trajectoryName, username, movesPerTrajectory);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}


							Map<String,Object> props = new HashMap<String,Object>();
							props.put("error", 0.1);	   
							
							return new Trajectory(trajectoryName, person, moves, props);

						}).collect(Collectors.toList());
						
						
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					return trajs;	



					//return username;


				})
				.flatMap((trajs)-> trajs.stream())
				.collect(Collectors.toList());

		return trajectories.toArray(new Trajectory[0]);



		//		for(int u=0; u < numberOfUsers; u++){
		//
		//			String username = usernames[u];
		//
		//			//System.out.println("username: " + username);
		//
		//			String trajectoriesDirectory = DATA_DIR + username + TRAJECTORY_DIR;
		//			String[] trajectoryNames = getTrajectories(trajectoriesDirectory, trajectoriesPerUser);
		//
		//			Person user = new Person(username);
		//
		//			for(int t=0; t < trajectoriesPerUser; t++){
		//
		//				//System.out.println("trajectory: " + trajectoryNames[t]);
		//
		//				Collection<Move> moves = csvListPointReader(trajectoryNames[t], username, movesPerTrajectory);
		//				//				System.out.println(index);
		//				//				System.out.println(moves);
		//
		//				Map<String,Object> props = new HashMap<String,Object>();
		//				props.put("error", 0.1);
		//				trajectories[index++] = new Trajectory(trajectoryNames[t], user, moves, props);	
		//
		//			}
		//
		//		}

		//return trajectories;
	}



	public static byte[] trajectoriesToJson(Trajectory [] trajectories) throws JsonGenerationException, JsonMappingException, IOException{
	
		return mObjectWriter.writeValueAsBytes(trajectories);
	}

	private final static OkHttpClient CLIENT = new OkHttpClient();
	
	private static MediaType MEDIA_TYPE;
	    
	public static Response POST(URI serverURI, String url, byte[] json) throws IOException{

	  
		final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
	
		RequestBody body = RequestBody.create(JSON, json);

		Request request = new Request.Builder()
		.url(serverURI.resolve(url).toString())
		//.header("Content-Encoding", "gzip")
		.post(body)
		.build();

		Response response = CLIENT.newCall(request).execute();

		return response;

	}

	public static Response GET(URI serverURI, String url) throws IOException{

		Request request = new Request.Builder()
		.url(serverURI.resolve(url).toString())
		.header("Accept-Encoding", " gzip,deflate")
		.build();

		Response response = CLIENT.newCall(request).execute();

		return response;
	}

	public static Iterable<Trajectory> getStreamingCollection(Response res) throws JsonParseException, JsonMappingException, IOException{

	  
		InputStream in = res.body().byteStream();
		Collection<Trajectory> trajectories;

		//TODO: Olhar para o header para perceber que formata esta a ser enviado
		boolean isKryo = false;

		if(isKryo){

			InputStream snappyIn = new SnappyFramedInputStream(in);
			final Input i = new Input(snappyIn);
			trajectories = mPool.run(new KryoCallback<Collection<Trajectory>>() {
				@SuppressWarnings("unchecked")
				public Collection<Trajectory> execute(Kryo kryo) {
					return (Collection<Trajectory>) kryo.readClassAndObject(i);
				}
			});

		} else {

			
			 trajectories =  mObjectReader.readValue(in);

		}

		return trajectories;

	}

  public static Trajectory[] createTrajectory(byte[] json) throws JsonProcessingException, IOException {
   
    return  ((Collection<Trajectory>) mObjectReader.readValue(json)).toArray(new Trajectory[0]);
    
  }

}