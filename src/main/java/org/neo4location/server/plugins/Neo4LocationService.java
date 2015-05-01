package org.neo4location.server.plugins;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.codehaus.jackson.map.type.TypeFactory;
import org.neo4j.gis.spatial.SpatialDatabaseService;
import org.neo4j.gis.spatial.indexprovider.SpatialIndexProvider;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Result;
//import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4location.domain.Neo4LocationLabels;
import org.neo4location.domain.Neo4LocationProperties;
import org.neo4location.domain.Neo4LocationRelationships;
import org.neo4location.domain.trajectory.Move;
import org.neo4location.domain.trajectory.Point;
import org.neo4location.domain.trajectory.RawData;
import org.neo4location.domain.trajectory.SemanticData;
import org.neo4location.domain.trajectory.Trajectory;
import org.neo4location.domain.trajectory.User;
import org.neo4location.graphdb.Neo4JMove;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import com.codahale.metrics.Slf4jReporter.LoggingLevel;
import com.codahale.metrics.Timer;

@Path("")
public class Neo4LocationService {

	private final GraphDatabaseService mDb;

	private final SpatialDatabaseService mSpatialService; 

	private Index<Node> mIndex;

	private Status status = Status.OK;

	private Logger logger = LoggerFactory.getLogger(Neo4LocationService.class);
	private MetricRegistry metrics = new MetricRegistry();
	private Timer tIndex;


	final Slf4jReporter reporter = Slf4jReporter.forRegistry(metrics)
			.outputTo(logger)
			.withLoggingLevel(LoggingLevel.ERROR)
			.build();

	private final static int GET_QUERY_PARAMS_START_DEFAULT = 0;
	private final static int GET_QUERY_PARAMS_OFFSET_DEFAULT = 10;

	private static final String GET_QUERY_PARAMS_WEIGHT_DEFAULT = "weight";
	private static final int GET_QUERY_PARAMS_LIMIT_DEFAULT = 3;

	private static final String GET_QUERY_PARAMS_REL_DEFAULT = Neo4LocationRelationships.MOVE.toString();


	//UserResource, TrajectoryResource, 
	public Neo4LocationService(@Context GraphDatabaseService db)
	{

		mDb = db;
		mSpatialService = new SpatialDatabaseService(mDb);

		//Metrics
		tIndex = metrics.timer("index");
		//reporter.start(1, TimeUnit.SECONDS);
		//mEditableLayer = mSpatialDb.getOrCreatePointLayer(mLayerPoints , Neo4LocationProperties.LATITUDE, Neo4LocationProperties.LONGITUDE);

	}


	//{pointId}/{LATITUDE}/{long}/{ALTITUDE}/{TIMESTAMP}{vehicle}
	//@Suspended final AsyncResponse asyncResponse

	//Provavelmente so havera uma trajectoria deixando de existir a diferencia entre trajetorias raw e semanticas.
	//Renomear addRawPoint ->
	//Passar de String para objetos
	@POST
	@Path("/trajectories")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addRawPoint(String _trajectories)
	{


		ObjectMapper mapper = new ObjectMapper();
		Collection<Trajectory> trajectories = new HashSet<Trajectory>();

		try {

			trajectories = mapper.readValue(_trajectories, TypeFactory.defaultInstance().constructParametricType(Collection.class, Trajectory.class));

		} catch (IOException e) {
			logger.error(e.getMessage());
		} 

		Map<String, String> config = SpatialIndexProvider.SIMPLE_POINT_CONFIG;
		IndexManager indexMan = mDb.index();

		try (Transaction tx = mDb.beginTx()){

			mIndex = indexMan.forNodes("points", config);

			//logger.error(String.valueOf(trajectories.size()));

			for(Trajectory trajectory : trajectories){

				//Devia remover trajectoryName
				String trajectoryName = trajectory.getTrajectoryName();

				String personName = trajectory.getUser().getUsername();

				List<Move> moves = new ArrayList<>(trajectory.getMoves());

				logger.error("---------------------------------");
				logger.error(String.format("user: %s", personName));
				logger.error(String.format("traj: %s", trajectoryName));
				logger.error("----------------------------------");

				//logger.error(String.format("traj size: %s", String.valueOf(moves.size())));

				Node person = getOrCreatePerson(personName);
				Node traj = getOrCreateTrajectory(trajectoryName);
				Node nLast;

				if(!traj.hasRelationship(Neo4LocationRelationships.FROM, Direction.OUTGOING)){
					//logger.error("first time");
					//FIRST TIME WE SEE THIS TRAJ
					person.createRelationshipTo(traj, Neo4LocationRelationships.START_A);	
					nLast = getOrCreateMove(moves.get(0), false);
					traj.createRelationshipTo(nLast, Neo4LocationRelationships.FROM);

				} 
				else {
					//FIRST + N TIME WE SEE THIS TRAJ
					Relationship rTo = traj.getSingleRelationship(Neo4LocationRelationships.TO, Direction.OUTGOING);		
					nLast = rTo.getEndNode();
					rTo.delete();

				}

				append(traj,nLast,moves);

			}

			tx.success();	
			tx.close();

		} catch (Exception e) {

			logger.error(e.toString());
			for(StackTraceElement st :e.getStackTrace()){
				logger.error(st.toString());
			}

		}


		//Timer.Context context = tIndex.time();

		//context.close();



		return Response.status(Response.Status.CREATED).build();

		//Falta meter end

	}


	private void append(final Node traj, final Node nLast, final List<Move> moves) {

		Node cursor = nLast;
		for(Move m : moves){

			Node nTo = getOrCreateMove(m, true);
			cursor.createRelationshipTo(nTo, m.getRelationship());
			Relationship r = nLast.getSingleRelationship(m.getRelationship(), Direction.OUTGOING);

			//Devia retornar SemanticData ou Map
			Map<String, Object> sd = m.getSemanticData();

			if(sd != null){

				for(Entry<String, Object> kv : sd.entrySet()){

					r.setProperty(kv.getKey(), kv.getValue());

				}

			}

			cursor = nTo;


		}

		traj.createRelationshipTo(cursor, Neo4LocationRelationships.TO);

	}


	private Node getOrCreateMove(Move move, boolean to) {

		Point ret;
		if(to) {
			ret = move.getTo();
		}
		else {

			ret = move.getFrom();
		}

		return toPointNode(ret);
	}


	private Node toPointNode(Point p) {

		//TODO: Merge Similar Points.
		//Adicionar suporte a Z.


		//		double lat = 10; 
		//		double lon =  10;

		//TODO: Ver Envelope

		//		Envelope maxDistanceInKm = new Envelope

		//		List<SpatialDatabaseRecord> results = GeoPipeline.
		//					startNearestNeighborLatLonSearch(mSpatialService.getLayer("points"), new Coordinate(lat, lon), maxDistanceInKm)
		//		        	.toSpatialDatabaseRecordList();
		//		   

		//IF EXISTS NODE IN X KM DONT CREATE
		//UPDATE INFO


		status = Status.CREATED;
		Node point = mDb.createNode(p.getLabels().toArray(new Label[0]));

		RawData rd = p.getRawData();

		if(rd != null){

			point.setProperty(Neo4LocationProperties.LATITUDE, rd.getLatitude());
			point.setProperty(Neo4LocationProperties.LONGITUDE, rd.getLongitude());
			point.setProperty(Neo4LocationProperties.ALTITUDE, rd.getAltitude());
			point.setProperty(Neo4LocationProperties.ACCURACY, rd.getAccuracy());

			point.setProperty(Neo4LocationProperties.SPEED, rd.getSpeed());
			point.setProperty(Neo4LocationProperties.TIMESTAMP, rd.getTime());

		}

		SemanticData sd = p.getSemanticData();

		if(sd != null){

			for(Entry<String, Object> kv : sd.getSemanticData().entrySet()){

				point.setProperty(kv.getKey(), kv.getValue());

			}


		}

		//mEditableLayer.add(point);

		mIndex.add(point, String.valueOf(point.getId()), String.valueOf(point.getId()));

		return point;
	}

	private Node getOrCreateTrajectory(String trajectoryName) {
		//logger.error("getTraj");
		Node traj =  mDb.findNode(Neo4LocationLabels.TRAJECTORY, Neo4LocationProperties.TRAJNAME, trajectoryName);


		if(traj != null)
			return traj;

		status = Status.CREATED;
		traj = mDb.createNode(Neo4LocationLabels.TRAJECTORY);
		traj.setProperty(Neo4LocationProperties.TRAJNAME, trajectoryName);

		return traj;

	}


	private Node getOrCreatePerson(String personName) {

		Node person =  mDb.findNode(Neo4LocationLabels.USER, Neo4LocationProperties.USERNAME, personName);

		if(person != null)
			return person;


		status = Status.CREATED;
		person = mDb.createNode(Neo4LocationLabels.USER);
		person.setProperty(Neo4LocationProperties.USERNAME, personName);
		return person;


	}


	@GET
	@Path("/trajectories")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getRawTrajectory(@Context UriInfo ui){

		MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
		//MultivaluedMap<String, String> pathParams = ui.getPathParameters();

		Map<String, Object> params = new HashMap<String, Object>();
		StringBuilder cypherQuery = new StringBuilder();

		try {

			//################################START##############################
			List<Float> withinDistance = new ArrayList<Float>();
			List<Float> bbox = new ArrayList<Float>();

			List<String> lLat = queryParams.get("lat");	//LATITUDE=2.8989,3.099090&LONGITUDE=2.8989,3.099090&ALTITUDE=2.8989,3.099090&time=2,3&points=-1,5&fields=LATITUDE,LONGITUDE
			int latSize = (lLat==null) ? 0 : lLat.size();

			List<String> lLon = queryParams.get("lon");	
			int lonSize = (lLon==null) ? 0 : lLon.size();

			List<String> lRadius = queryParams.get("radius");	
			int radiusSize = (lRadius==null) ? 0 : lRadius.size();


			//logger.error((String)params.get("trajectoryName"));


			for(int i=0; i < lonSize;i++){

				if(radiusSize >= 1){

					withinDistance.add(Float.parseFloat(lLon.get(i)));
					//logger.error("add withinDistance");

				} else {

					bbox.add(Float.parseFloat(lLon.get(i)));
					//logger.error("add bbox");

				}

			}

			for(int i=0; i < latSize;i++){

				if(radiusSize >= 1){

					withinDistance.add(Float.parseFloat(lLat.get(i)));			
					//logger.error("add withinDistance");

				} else {

					bbox.add(Float.parseFloat(lLat.get(i)));
					//logger.error("add bbox");

				}

			}


			for(int i=0; i < radiusSize; i++){
				withinDistance.add(Float.parseFloat(lRadius.get(i)));
			}


			//Entre 1 e 2, 1 e 2, 1 e 2
			if(latSize >= 1 && lonSize >= 1 && radiusSize >= 1){
				cypherQuery = new StringBuilder(String.format(Locale.ENGLISH,"START start=node:points('withinDistance:[%f, %f, %f]') ", withinDistance.get(0), withinDistance.get(1), withinDistance.get(2)));
			}

			if(latSize == 2 && lonSize == 2 && radiusSize == 2){

				cypherQuery.append(String.format(Locale.ENGLISH,", end=node:points('withinDistance:[%f, %f, %f]') ", withinDistance.get(0), withinDistance.get(1), withinDistance.get(2)));

			} 

			if (latSize >= 2 && lonSize >= 2  && radiusSize == 0){

				cypherQuery = new StringBuilder(String.format(Locale.ENGLISH,"START start=node:points('bbox:[%f, %f, %f, %f]') ", bbox.get(0), bbox.get(1), bbox.get(2), bbox.get(3)));

			} 

			if (latSize == 4 && lonSize == 4  && radiusSize == 0) {

				cypherQuery.append(String.format(Locale.ENGLISH,", end=node:points('bbox:[%f, %f, %f, %f]') ",   bbox.get(4), bbox.get(5), bbox.get(6), bbox.get(7)));

			}

			boolean hasStart = false;
			boolean hasEnd = false;

			if((latSize >= 1 && lonSize >= 1 && radiusSize >= 1) || (latSize >= 2 && lonSize >= 2  && radiusSize == 0)){
				cypherQuery.append("WITH start ");
				hasStart = true;
			}


			if ((latSize == 4 && lonSize == 4  && radiusSize == 0) || (latSize == 2 && lonSize == 2 && radiusSize == 2)){
				cypherQuery.append(", end  ");
				hasEnd = true;
			}


			List<String> username = queryParams.get("username");	
			int iUser = putInParameters("username", username, params, 1, 1);

			//################################MATCH##############################

			if(iUser == 0){

				cypherQuery.append("MATCH (user)");

			} else if (iUser == 1){

				cypherQuery.append(String.format("MATCH (user { %s : {username}})",Neo4LocationProperties.USERNAME));

			}

			cypherQuery.append(String.format("-[:%s]->", Neo4LocationRelationships.START_A));

			List<String> trajectoryName = queryParams.get("trajectory");
			int iTraj = putInParameters("trajectoryname", trajectoryName, params, 1, 1);

			if(iTraj == 0){

				cypherQuery.append("(trajectory)");

			} else if (iTraj == 1){

				cypherQuery.append(String.format("(trajectory { %s : {trajectoryname}})",Neo4LocationProperties.TRAJNAME));

			}

			List<String> lStart = queryParams.get("start");
			int iStart = (lStart==null || lStart.size() > 1) ? Neo4LocationService.GET_QUERY_PARAMS_START_DEFAULT :  Integer.parseInt(lStart.get(0));

			List<String> lOffset = queryParams.get("offset");
			int iOffset = (lOffset==null || lStart.size() > 1) ? Neo4LocationService.GET_QUERY_PARAMS_OFFSET_DEFAULT : Integer.parseInt(lOffset.get(0));


			List<String> lRel = queryParams.get("rel");
			int lRelSize = (lRel==null) ? 0 : lRel.size();

			if(lRelSize == 0){
				lRel = new ArrayList<String>();
				lRel.add(Neo4LocationService.GET_QUERY_PARAMS_REL_DEFAULT);
			}

			String rel = lRel.get(0);

			if(iStart >= 0){

				if(!hasStart){
					//Nao tem start usamos o FROM como start
					cypherQuery.append(String.format("-[:%s]->(start)", Neo4LocationRelationships.FROM));
				}

				
				cypherQuery.append(",p=(start)");

				if(iOffset == 0)
					cypherQuery.append(String.format("-[:%s*%d]->(end)", rel, iStart));
				else 
					cypherQuery.append(String.format("-[:%s*%d..%d]->(end)", rel, iStart, iOffset));

				
				if(!hasEnd){
					//Nao tem end usamos o TO como end
					cypherQuery.append(String.format(",(end)<-[:%s]-(traj)", Neo4LocationRelationships.TO));
				}
				

			}
			else {

				iStart = Math.abs(iStart);
				
				if(!hasStart){
					//Nao tem start usamos o TO como start
					cypherQuery.append(String.format("-[:%s]->(start)", Neo4LocationRelationships.TO));
				}
				
				cypherQuery.append(",p=(start)");

				if(iOffset == 0)
					cypherQuery.append(String.format("<-[:%s*%d]-(end)", rel, iStart));
				else 
					cypherQuery.append(String.format("<-[:%s*%d..%d]-(end)", rel, iStart, iOffset));


				if(!hasEnd){
					//Nao tem end usamos o FROM como end
					cypherQuery.append(String.format(",(end)<-[:%s]-(traj)", Neo4LocationRelationships.FROM));
				}
				
				

			}



			//################################WHERE##############################


			final Map<String,String> lWhere = new HashMap<String, String>();

			lWhere.put("alt", Neo4LocationProperties.ALTITUDE);
			lWhere.put("timestamp", Neo4LocationProperties.TIMESTAMP);
			lWhere.put("speed", Neo4LocationProperties.SPEED);
			lWhere.put("accuracy", Neo4LocationProperties.ACCURACY);

			//cypherQuery.append(" WHERE TRUE ");
			for(Entry<String, String> kv : lWhere.entrySet()){

				List<String> t = queryParams.get(kv.getKey());	
				int it = (t==null) ? 0 : t.size();

				if(it >= 1){

					cypherQuery.append(String.format(Locale.ENGLISH,"AND %s >= %s ", kv.getValue(), t.get(0)));

				}
				if (it == 2){

					cypherQuery.append(String.format(Locale.ENGLISH,"AND %s <= %s ",kv.getValue(), t.get(1)));

				}

			}

			//TODO: Create String Point.parse(String property, String value)

			//		List<String> altitude = queryParams.get("alt");	//ALTITUDE=2.8989,3.099090
			//		
			//		List<String> speed = queryParams.get("speed"); //SPEED=20,20
			//		
			//		List<String> accuracy = queryParams.get("accuracy");

			//#########################RETURN##########################

			cypherQuery.append(" RETURN user, trajectory, relationships(p) AS rels");
			//			cypherQuery.append(", length(p) AS len ");

			//#########################STATICS##########################


			List<String> lSum = queryParams.get("sum");

			if(lSum != null){

				for(String sum : lSum){

					if(sum.startsWith("n.")){
						cypherQuery.append(String.format(", reduce(c = 0, n IN nodes(p) | c + %s) AS %s_total", sum, sum));
					}
					else if(sum.startsWith("r.")){
						cypherQuery.append(String.format(", reduce(c = 0, r IN relationships(p) | c + %s) AS %s_total", sum, sum));
					}
					else {
						//TODO: Throw
					}

				}

			}

		}
		catch(Exception e){

			logger.error(e.toString());
			for(StackTraceElement st :e.getStackTrace()){
				logger.error(st.toString());
			}

		}

		logger.error(cypherQuery.toString());

		String r = "";

		try (Transaction tx = mDb.beginTx())
		{

			Result result = mDb.execute(cypherQuery.toString(), params);

			ObjectWriter mapper = new ObjectMapper().writerWithDefaultPrettyPrinter();
			Collection<Trajectory> trajectories = toTrajectories(result);
			r = mapper.writeValueAsString(trajectories);

			tx.success();

		} catch (Exception e) {

			logger.error(e.toString());
			for(StackTraceElement st :e.getStackTrace()){
				logger.error(st.toString());
			}


		}

		return Response.status(Response.Status.OK).entity(r).build();

		//Filtering
		//page=1,-2&per_page=100&sort=

		//Select
		//fields=LATITUDE,LONGITUDE,TIMESTAMP&

		//Where
		//LATITUDE=10

		//last k points de uma uma trajectoria ok
		//page=-1&per_page=k&fields=LATITUDE,LONGITUDE&sort=-time&LATITUDE=10


	}

	private Collection<Trajectory> toTrajectories(Result result){

		Collection<Trajectory> trajs = new HashSet<>();

		while (result.hasNext())
		{
			User person = null;
			String trajName = null;
			Collection<Move> mvs = new ArrayList<>();
			Set<Relationship> rel = new HashSet<>();
			Map<String,Object> props = new HashMap<>();

			Map<String,Object> row = result.next();

			for ( String key : result.columns() )
			{

				Object value = row.get(key);

				if(value instanceof Node){

					Node t = (Node) value;

					if(t.hasLabel(Neo4LocationLabels.USER)){
						String personName = (String) t.getProperty(Neo4LocationProperties.USERNAME);
						person = new User(personName);
					} else {
						trajName = (String) t.getProperty(Neo4LocationProperties.TRAJNAME);
					}
				}
				if(value instanceof Relationship){
					rel.add((Relationship) value);
				}
				if(value instanceof List<?>){
					rel.addAll((List<Relationship>) value);
				} else {
					props.put(key, value.toString());
					//logger.error(key,value.toString());
				}

			}


			for(Relationship t : rel){

				Move m = new Neo4JMove(t).getMove();

				//				for(Entry<String, Object> prop: props.entrySet()){
				//					
				//				}

				mvs.add(m);
			}


			trajs.add(new Trajectory(trajName, person, mvs));

		}


		return trajs;

	}

	private int putInParameters(String key, List<String> values, Map<String,Object> params, int minSize, int maxSize){

		int i=0;

		if(values != null){
			if(values.size() >= minSize && values.size() <= maxSize ){

				for(String value : values){
					params.put(key, value);
					i++;
				}
			}
			else {

				//ERRO
				//throw
			}
		}
		else {

			if(minSize == 0){
				//params.put("username","");
			}
			else {
				//ERRO
			}

		}

		return i;

	}

}