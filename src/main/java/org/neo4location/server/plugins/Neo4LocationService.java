package org.neo4location.server.plugins;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.TypeFactory;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
//import org.neo4j.cypher.ExecutionEngine;
//import org.neo4j.cypher.ExecutionResult;
import org.neo4j.gis.spatial.EditableLayer;
import org.neo4j.gis.spatial.SpatialDatabaseService;
import org.neo4j.gis.spatial.indexprovider.LayerNodeIndex;
import org.neo4j.gis.spatial.indexprovider.SpatialIndexProvider;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterable;
//import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.kernel.impl.core.NodeProxy;
import org.neo4location.domain.Neo4LocationLabels;
import org.neo4location.domain.Neo4LocationProperties;
import org.neo4location.domain.Neo4LocationRelationships;
import org.neo4location.domain.trajectory.Move;
import org.neo4location.domain.trajectory.Point;
import org.neo4location.domain.trajectory.RawData;
import org.neo4location.domain.trajectory.SemanticData;
import org.neo4location.graphdb.Neo4JMove;

@Path("")
public class Neo4LocationService {


	private final String mLayerPoints = "points";

	private final GraphDatabaseService mDb;

	private final SpatialDatabaseService mSpatialDb;

	private EditableLayer mEditableLayer; 

	private Index<Node> mIndex;

	private org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Neo4LocationService.class);

	private final static int GET_QUERY_PARAMS_START_DEFAULT = 0;
	private final static int GET_QUERY_PARAMS_OFFSET_DEFAULT = 10;
	private static final String GET_QUERY_PARAMS_WEIGHT_DEFAULT = "weight";
	private static final int GET_QUERY_PARAMS_LIMIT_DEFAULT = 3;

	//UserResource, TrajectoryResource, 
	public Neo4LocationService( @Context GraphDatabaseService db)
	{

		mDb = db;
		mSpatialDb = new SpatialDatabaseService(db);

		try {

			//			mEditableLayer = mSpatialDb.getOrCreatePointLayer(mLayerPoints , Neo4LocationProperties.LATITUDE, Neo4LocationProperties.LONGITUDE);

		} catch(Exception e){
			logger.error("Construtor");
			logger.error(e.toString());
		}

	}


	@GET
	@Path("/ping")
	@Produces(MediaType.APPLICATION_JSON)
	public Response ping()
	{

		String pong = "pong";

		Map<String, String> config = SpatialIndexProvider.SIMPLE_POINT_CONFIG;
		IndexManager indexMan = mDb.index();
		Index<Node> index;


		Transaction tx = mDb.beginTx();
		index = indexMan.forNodes("layer1", config);

		ExecutionEngine engine = new ExecutionEngine(mDb);
		ExecutionResult result1 = engine.execute("create (malmo {name:'Malmö',lat:56.2, lon:15.3})-[:TRAIN]->(stockholm {name:'Stockholm',lat:59.3,lon:18.0}) return malmo");
		Node malmo = (Node) result1.iterator().next().get("malmo");
		index.add(malmo, "dummy", "value");
		tx.success();
		tx.close();

		tx = mDb.beginTx();

		// within BBOX
		IndexHits<Node> hits = index.query(LayerNodeIndex.BBOX_QUERY,
				"[15.0, 16.0, 56.0, 57.0]");
		logger.error("INDEX PONG - " + String.valueOf(hits.hasNext()));

		hits.close();

		//ExecutionResult result1 = 
		ExecutionResult result2 = engine.execute("start malmo=node:layer1('bbox:[15.0, 16.0, 56.0, 57.0]') match p=malmo--other return malmo, other");
		logger.error("INDEX PONG - " + result2.dumpToString());
		result2 = engine.execute("start malmo=node:layer1('withinDistance:[56.0, 15.0,1000.0]') match p=malmo--other return malmo, other");
		logger.error("INDEX PONG - " + result2.dumpToString());

		tx.success();
		tx.close();

		return Response.status(Response.Status.OK).entity(pong).build();

	}


	//{pointId}/{LATITUDE}/{long}/{ALTITUDE}/{TIMESTAMP}{vehicle}
	//@Suspended final AsyncResponse asyncResponse

	//Provavelmente so havera uma trajectoria deixando de existir a diferencia entre trajetorias raw e semanticas.
	//Renomear addRawPoint ->
	@POST
	@Path("/users/{personName}/trajectories/{trajectoryName}/raw/points")
	@Consumes(MediaType.TEXT_PLAIN)
	public Response addRawPoint(String _moves, @PathParam("personName") String personName, @PathParam("trajectoryName") String trajectoryName)
	{


		ObjectMapper mapper = new ObjectMapper();
		List<Move> moves = new ArrayList<Move>();
		try {

			moves = mapper.readValue(_moves, TypeFactory.defaultInstance().constructParametricType(List.class, Neo4JMove.class));

		} catch (IOException e) {
			logger.error(e.getMessage());
		} 

		Map<String, String> config = SpatialIndexProvider.SIMPLE_POINT_CONFIG;
		IndexManager indexMan = mDb.index();

		try (Transaction tx = mDb.beginTx()){

			mIndex = indexMan.forNodes("points", config);

			Node person = getOrCreatePerson(personName);
			Node traj = getOrCreateTrajectory(trajectoryName);
			Node nLast;

			if(!person.hasRelationship(Neo4LocationRelationships.START_A, Direction.OUTGOING)){

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


			tx.success();	
			tx.close();

		} catch (Exception e) {

			logger.error(e.toString());
			for(StackTraceElement st :e.getStackTrace()){
				logger.error(st.toString());
			}

		}

		Transaction tx = mDb.beginTx();

		for(String ind: indexMan.nodeIndexNames()){
			logger.error("INDN - " + ind);
		}


		IndexHits<Node> hits = mIndex.query(LayerNodeIndex.BBOX_QUERY, "[0.0, 180.0, 0.0, 90.0]");
		logger.error("INDEX - " + String.valueOf(hits.hasNext()));
		hits.close();

		tx.success();
		tx.close();


		String r = "";
		return Response.status(Response.Status.OK).entity(r).build();

		//Falta meter end

	}


	private void append(final Node traj, final Node nLast, final List<Move> moves) {

		//moves.parallelStream().forEach(

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
		//TODO:
		//IF EXISTS NODE IN X KM DONT CREATE
		//UPDATE INFO

		Node point = mDb.createNode(p.getLabels().toArray(new Label[0]));
		//RawData rd = new RawData(lat, lon, alt, accuracy, speed, timestamp);

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

			for(Entry<String, Object> kv : sd.getKeysAndValues().entrySet()){

				point.setProperty(kv.getKey(), kv.getValue());

			}

			//.forEach((k,v) -> point.setProperty(k, v));
		}

		//mEditableLayer.add(point);

		mIndex.add(point, String.valueOf(point.getId()), String.valueOf(point.getId()));

		return point;
	}

	private Node getOrCreateTrajectory(String trajectoryName) {

		ResourceIterable<Node> trajs =  mDb.findNodesByLabelAndProperty(Neo4LocationLabels.TRAJECTORY, Neo4LocationProperties.TRAJNAME, trajectoryName);

		Node traj;

		if(trajs != null){
			for(Node t: trajs){
				return t;
			}
		}


		traj = mDb.createNode(Neo4LocationLabels.TRAJECTORY);
		traj.setProperty(Neo4LocationProperties.TRAJNAME, trajectoryName);

		return traj;

	}


	private Node getOrCreatePerson(String personName) {


		ResourceIterable<Node> persons =  mDb.findNodesByLabelAndProperty(Neo4LocationLabels.USER, Neo4LocationProperties.USERNAME, personName);

		Node person;

		if(persons != null){
			for(Node p: persons){
				return p;
			}
		}
		person = mDb.createNode(Neo4LocationLabels.USER);
		person.setProperty(Neo4LocationProperties.USERNAME, personName);

		return person;
	}




	@POST
	@Path("/shortest")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getShortestTrajectory(String _start, String _end, @Context UriInfo ui){

		//Dado start, end, prop, numberOfPaths 
		//post /shortest?weight=distance&weight=time&limit=2

		MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
		Map<String, Object> params = new HashMap<String, Object>();

		ObjectMapper mapper = new ObjectMapper();
		List<Move> moves = new ArrayList<Move>();
		try {
			moves = mapper.readValue(_start, TypeFactory.defaultInstance().constructParametricType(List.class, Neo4JMove.class));
		} catch (IOException e) {
			logger.error(e.getMessage());
		} 

		//TODO: Futuro suportar mais de 1 weight
		List<String> lWeight = queryParams.get("weight");
		String weight = (lWeight==null || lWeight.size() > 1) ? Neo4LocationService.GET_QUERY_PARAMS_WEIGHT_DEFAULT :  lWeight.get(0);

		List<String> lLimit = queryParams.get("limit");
		int limit = (lLimit ==null || lLimit.size() > 1) ? Neo4LocationService.GET_QUERY_PARAMS_LIMIT_DEFAULT : Integer.parseInt(lLimit.get(0));

		//		params.put("startProps", limit);
		//		params.put("endProps", limit);
		params.put("limit", limit);

		StringBuilder cypherQuery = 
				new StringBuilder("MATCH p=(start {startProps})-[*]->(end {endProps}) RETURN p as shortestPath, "
						+ String.format("REDUCE(weight=0, r in relationships(p) | weight + r.%s) AS totalWeight ", weight)
						+ "ORDER BY totalWeight ASC LIMIT {limit}");



		logger.error(cypherQuery.toString());

		String r = "";


		return Response.status(Response.Status.OK).entity(r).build();
	}



	@GET
	//@Timed
	@Path("/users/{personName}/trajectories/{trajectoryName}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getRawTrajectory(@Context UriInfo ui){

		MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
		MultivaluedMap<String, String> pathParams = ui.getPathParameters();
		Map<String, Object> params = new HashMap<String, Object>();
		StringBuilder cypherQuery = new StringBuilder();

		try {
			List<String> username = pathParams.get("personName");	
			int ret = putInParameters("personName", username, params, 1, 1);

			List<String> trajectoryName = pathParams.get("trajectoryName");
			ret = putInParameters("trajectoryName", trajectoryName, params, 1, 1);


			//################################START##############################
			List<Float> withinDistance = new ArrayList<Float>();
			List<Float> bbox = new ArrayList<Float>();

			List<String> lLat = queryParams.get("lat");	//LATITUDE=2.8989,3.099090&LONGITUDE=2.8989,3.099090&ALTITUDE=2.8989,3.099090&time=2,3&points=-1,5&fields=LATITUDE,LONGITUDE
			int latSize = (lLat==null) ? 0 : lLat.size();

			List<String> lLon = queryParams.get("lon");	
			int lonSize = (lLon==null) ? 0 : lLon.size();

			List<String> lRadius = queryParams.get("radius");	
			int radiusSize = (lRadius==null) ? 0 : lRadius.size();

			logger.error(String.valueOf(latSize));
			logger.error(String.valueOf(lonSize));

			for(int i=0; i < lonSize;i++){

				if(radiusSize >= 1){

					withinDistance.add(Float.parseFloat(lLon.get(i)));
					logger.error("add withinDistance");

				} else {

					bbox.add(Float.parseFloat(lLon.get(i)));
					logger.error("add bbox");

				}

			}

			for(int i=0; i < latSize;i++){

				if(radiusSize >= 1){

					withinDistance.add(Float.parseFloat(lLat.get(i)));			
					logger.error("add withinDistance");

				} else {

					bbox.add(Float.parseFloat(lLat.get(i)));
					logger.error("add bbox");

				}

			}


			for(int i=0; i < radiusSize; i++){
				withinDistance.add(Float.parseFloat(lRadius.get(i)));
			}


			//		for(int i=0; i < lonSize; i++){
			//
			//			if(radiusSize >= 1){
			//				withinDistance.add(Float.parseFloat(lLon.get(i)));
			//			} else {
			//				bbox.add(Float.parseFloat(lLon.get(i)));
			//			}
			//		}



			//Entre 1 e 2, 1 e 2, 1 e 2
			if(latSize >= 1 && lonSize >= 1 && radiusSize >= 1){
				logger.error("points - dist - 1");
				cypherQuery = new StringBuilder(String.format(Locale.ENGLISH,"START start=node:points('withinDistance:[%f, %f, %f]') ", withinDistance.get(0), withinDistance.get(1), withinDistance.get(2)));
			}

			if(latSize == 2 && lonSize == 2 && radiusSize == 2){
				logger.error("points - dist - 2");
				cypherQuery.append(String.format(Locale.ENGLISH,", end=node:points('withinDistance:[%f, %f, %f]') ", withinDistance.get(0), withinDistance.get(1), withinDistance.get(2)));

			} 

			if (latSize >= 2 && lonSize >= 2  && radiusSize == 0){
				logger.error("points - 1");
				cypherQuery = new StringBuilder(String.format(Locale.ENGLISH,"START start=node:points('bbox:[%f, %f, %f, %f]') ", bbox.get(0), bbox.get(1), bbox.get(2), bbox.get(3)));

			} 

			if (latSize == 4 && lonSize == 4  && radiusSize == 0) {
				logger.error("points - 2");
				cypherQuery.append(String.format(Locale.ENGLISH,", end=node:points('bbox:[%f, %f, %f, %f]') ",   bbox.get(4), bbox.get(5), bbox.get(6), bbox.get(7)));

			}

			cypherQuery.append("WITH start ");

			//################################MATCH##############################

			cypherQuery.append(String.format("MATCH p = ({ %s : {personName}})-[:%s]->(traj { %s : {trajectoryName}})",
					Neo4LocationProperties.USERNAME, Neo4LocationRelationships.START_A, Neo4LocationProperties.TRAJNAME));

			List<String> lStart = queryParams.get("start");
			int start = (lStart==null || lStart.size() > 1) ? Neo4LocationService.GET_QUERY_PARAMS_START_DEFAULT :  Integer.parseInt(lStart.get(0));

			List<String> lOffset = queryParams.get("offset");
			int offset = (lOffset==null || lStart.size() > 1) ? Neo4LocationService.GET_QUERY_PARAMS_OFFSET_DEFAULT : Integer.parseInt(lOffset.get(0));

			List<String> lRel = queryParams.get("rel");
			int lRelSize = (lRel==null) ? 0 : lRel.size();

			if(lRelSize != 0){

				String rel = lRel.get(0);

				if(start >= 0){

					cypherQuery.append(String.format("-[:%s]->()", Neo4LocationRelationships.FROM));

					if(offset == 0)
						cypherQuery.append(String.format("-[:%s*%d]->",rel,start));
					else 
						cypherQuery.append(String.format("-[:%s*%d..%d]->",rel,start,offset));

					cypherQuery.append(String.format("()<-[:%s]-(traj)", Neo4LocationRelationships.TO));

				}
				else {

					start = Math.abs(start);
					cypherQuery.append(String.format("-[:%s]->()", Neo4LocationRelationships.TO));

					if(offset == 0)
						cypherQuery.append(String.format("<-[:%s*%d]-",rel,start));
					else 
						cypherQuery.append(String.format("<-[:%s*%d..%d]-",rel,start,offset));

					cypherQuery.append(String.format("()<-[:%s]-(traj)", Neo4LocationRelationships.FROM));

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

				List<String> time = queryParams.get(kv.getKey());	
				ret = (time==null) ? 0 : time.size();

				if(ret >= 1){

					cypherQuery.append(String.format(Locale.ENGLISH,"AND %s >= %s ", kv.getValue(), time.get(0)));

				}
				if (ret == 2){

					cypherQuery.append(String.format(Locale.ENGLISH,"AND %s <= %s ",kv.getValue(), time.get(1)));

				}

			}

			//TODO: Create String Point.parse(String property, String value)

			//		List<String> altitude = queryParams.get("alt");	//ALTITUDE=2.8989,3.099090
			//		
			//		List<String> speed = queryParams.get("speed"); //SPEED=20,20
			//		
			//		List<String> accuracy = queryParams.get("accuracy");

			//#########################RETURN##########################
			cypherQuery.append(" RETURN start");

		}
		catch(Exception e){

			logger.error(e.toString());
			for(StackTraceElement st :e.getStackTrace()){
				logger.error(st.toString());
			}

		}

		logger.error(cypherQuery.toString());

		//		Map<String, String> config = SpatialIndexProvider.SIMPLE_POINT_CONFIG;
		//		IndexManager indexMan = mDb.index();

		//		try (Transaction tx2 = mDb.beginTx()){
		//			
		//			mIndex = indexMan.forNodes("points", config);
		//			IndexHits<Node> hits = mIndex.query(LayerNodeIndex.BBOX_QUERY, "[-180.0, 180.0, -90.0, 90.0]");
		//			logger.error("index - " + String.valueOf(hits.hasNext()));
		//
		//			tx2.success();	
		//			tx2.close();
		//
		//		} catch(Exception e){
		//
		//			logger.error(e.toString());
		//			for(StackTraceElement st :e.getStackTrace()){
		//				logger.error(st.toString());
		//			}
		//
		//		}


		String r = "";

		try (Transaction tx = mDb.beginTx())
		{

			ExecutionEngine engine = new ExecutionEngine(mDb);
			ExecutionResult result = engine.execute(cypherQuery.toString(),params);

			//IndexHits<Node> hits = mIndex.query(LayerNodeIndex.BBOX_QUERY, "[0.0, 180.0, 0.0, 90.0]");
			//logger.error("index - " + String.valueOf(hits.hasNext()));

			//r += result.dumpToString();



			for (Map<String, Object> mp : result)
			{


				for(Entry<String, Object> kv: mp.entrySet()){

					String key = kv.getKey();

					r += String.format("node: %s ",key); //person - nome da var

					org.neo4j.kernel.impl.core.NodeProxy t =  (NodeProxy) kv.getValue();

					/*if(isNode(key)){
									t = (Node) kv.getValue(); 
								}
								else {
									t = (Relationship) kv.getValue(); 
								}*/

					List<String> fields = queryParams.get("field");

					if(fields!=null){
						
						
						for(String field : fields){

							if(t.hasProperty(field)){
								r += String.format("%s: %s",field,t.getProperty(field).toString()); 
								r += " ";
							}
						}
						
						r += "\n";


					}



				}

			}

			tx.success();

		} catch (Exception e) {

			logger.error(e.toString());

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