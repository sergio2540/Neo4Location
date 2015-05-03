package org.neo4location.server.plugins;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.sun.jersey.spi.inject.Inject;

import org.neo4j.gis.spatial.SpatialDatabaseRecord;
import org.neo4j.gis.spatial.SpatialDatabaseService;
import org.neo4j.gis.spatial.indexprovider.LayerNodeIndex;
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
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4location.domain.Compress;
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
import org.neo4location.graphdb.Neo4JPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import com.codahale.metrics.Slf4jReporter.LoggingLevel;
import com.codahale.metrics.Timer;

//@Singleton
@Path("")
public class Neo4LocationService {

	private final GraphDatabaseService mDb;
	private final SpatialDatabaseService mSpatialService; 
	private Index<Node> mIndex;
	private double mAccuracyInKm = 0.1;
	private Status status = Status.OK;
	
	
	private Logger logger = LoggerFactory.getLogger(Neo4LocationService.class);
	private MetricRegistry metrics = new MetricRegistry();
	private final Timer tIndex;
	private final Slf4jReporter reporter = Slf4jReporter.forRegistry(metrics)
			.outputTo(logger)
			.withLoggingLevel(LoggingLevel.ERROR)
			.build();

	private final static int GET_QUERY_PARAMS_START_DEFAULT = 0;
	private final static int GET_QUERY_PARAMS_OFFSET_DEFAULT = 100;


	private static final int GET_QUERY_PARAMS_LIMIT_DEFAULT = 3;

	private static final String GET_QUERY_PARAMS_REL_DEFAULT = Neo4LocationRelationships.MOVE.toString();


	//UserResource, TrajectoryResource, 
	public Neo4LocationService(@Context GraphDatabaseService db)
	{

		mDb = db;
		mSpatialService = new SpatialDatabaseService(mDb);

		//Metrics
		tIndex = metrics.timer("index");
		//reporter.start(2, TimeUnit.SECONDS);
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
	public Response addRawPoint(String  _trajectories) throws Exception
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

				mAccuracyInKm = 0.1;

				//Devia remover trajectoryName
				String trajectoryName = trajectory.getTrajectoryName();
				Map<String, Object> props = trajectory.getSemanticData();

				String personName = trajectory.getUser().getUsername();

				List<Move> moves = new ArrayList<>(trajectory.getMoves());

				//				logger.error("---------------------------------");
				//				logger.error(String.format("user: %s", personName));
				//				logger.error(String.format("traj: %s", trajectoryName));
				//				logger.error("----------------------------------");

				//logger.error(String.format("traj size: %s", String.valueOf(moves.size())));

				Node person = getOrCreatePerson(personName);
				Node traj = getOrCreateTrajectory(trajectoryName,props);
				Node nLast;

				if(!traj.hasRelationship(Neo4LocationRelationships.FROM, Direction.OUTGOING)){
					//logger.error("first time");
					//FIRST TIME WE SEE THIS TRAJ
					person.createRelationshipTo(traj, Neo4LocationRelationships.START_A);	
					nLast = getOrCreatePoint(moves.get(0), false);
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



		if(status.equals(Status.CREATED)){
			return Response.status(Response.Status.CREATED).build();
		}
		else {
			status = Status.OK;
			return Response.status(Response.Status.OK).build();
		}

	}


	private void append(final Node traj, final Node nLast, final List<Move> moves) {

		Node cursor = nLast;
		for(Move m : moves){

			Node nTo = getOrCreatePoint(m, true);

			cursor.createRelationshipTo(nTo, m.getRelationship());
			status = Status.CREATED;

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


	private Node getOrCreatePoint(Move move, boolean to) {

		Point ret;
		if(to) {
			ret = move.getTo();
		}
		else {

			ret = move.getFrom();
		}

		return toNode(ret);
	}


	private Node toNode(Point p) {

		//Adicionar suporte a Z.
		//Timestamp nao deixa fazer merge

		double lat = p.getRawData().getLatitude(); 
		double lon =  p.getRawData().getLongitude();

		Map<String, Object> params = new HashMap<String, Object>();
		params.put(LayerNodeIndex.DISTANCE_IN_KM_PARAMETER, mAccuracyInKm);
		params.put(LayerNodeIndex.POINT_PARAMETER, new Double[] { lat, lon });

		IndexHits<Node> results = mIndex.query(LayerNodeIndex.WITHIN_DISTANCE_QUERY, params);
		Node point = null;

		int degree = 1;

		if (results.hasNext()) {

			//Retorna primeiro que encontra
			//Estava tao proximo que a db considera igual
			//Nao e clustering
			point = results.next();

			//degree
			degree = (int) point.getProperty("degree") + 1;
			point.setProperty("degree", degree);


		}

		//por enquanto cria sempre

		point = mDb.createNode(p.getLabels().toArray(new Label[0]));
		status = Status.CREATED;


		RawData rd = p.getRawData();

		if(rd != null){

			point.setProperty(Neo4LocationProperties.LATITUDE, rd.getLatitude());
			point.setProperty(Neo4LocationProperties.LONGITUDE, rd.getLongitude());
			point.setProperty(Neo4LocationProperties.ALTITUDE, rd.getAltitude());
			point.setProperty(Neo4LocationProperties.ACCURACY, rd.getAccuracy());

			point.setProperty(Neo4LocationProperties.SPEED, rd.getSpeed());
			point.setProperty(Neo4LocationProperties.TIMESTAMP, rd.getTime());

		}

		Map<String,Object> sd = p.getSemanticData();

		if(sd != null){

			for(Entry<String, Object> kv : sd.entrySet()){

				point.setProperty(kv.getKey(), kv.getValue());

			}


		}


		point.setProperty("degree", degree);


		mIndex.add(point, String.valueOf(point.getId()), String.valueOf(point.getId()));

		return point;
	}

	private Node getOrCreateTrajectory(String trajectoryName, Map<String,Object> props) {
		//logger.error("getTraj");
		Node traj =  mDb.findNode(Neo4LocationLabels.TRAJECTORY, Neo4LocationProperties.TRAJNAME, trajectoryName);


		if(traj == null){
			status = Status.CREATED;
			traj = mDb.createNode(Neo4LocationLabels.TRAJECTORY);
		}

		traj.setProperty(Neo4LocationProperties.TRAJNAME, trajectoryName);

		for(Entry<String, Object> prop: props.entrySet()){

			traj.setProperty(prop.getKey(),prop.getValue());

			if(prop.getKey().equals("error")){
				mAccuracyInKm = (double) prop.getValue();
			}

		}

		return traj;

	}


	private Node getOrCreatePerson(String personName) {

		Node person =  mDb.findNode(Neo4LocationLabels.USER, Neo4LocationProperties.USERNAME, personName);

		if(person == null){
			status = Status.CREATED;
			person = mDb.createNode(Neo4LocationLabels.USER);

		}

		person.setProperty(Neo4LocationProperties.USERNAME, personName);
		return person;

	}


	@GET
	@Compress
	@Path("/trajectories")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getRawTrajectory(@Context UriInfo ui){

		
		MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
		//MultivaluedMap<String, String> pathParams = ui.getPathParameters();

		Map<String, Object> params = new HashMap<String, Object>();
		final StringBuilder cypherQuery = new StringBuilder();

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
				cypherQuery.append(String.format(Locale.ENGLISH,"START start=node:points('withinDistance:[%f, %f, %f]') ", withinDistance.get(0), withinDistance.get(1), withinDistance.get(2)));
			}

			if(latSize == 2 && lonSize == 2 && radiusSize == 2){

				cypherQuery.append(String.format(Locale.ENGLISH,", end=node:points('withinDistance:[%f, %f, %f]') ", withinDistance.get(0), withinDistance.get(1), withinDistance.get(2)));

			} 

			if (latSize >= 2 && lonSize >= 2  && radiusSize == 0){

				cypherQuery.append(String.format(Locale.ENGLISH,"START start=node:points('bbox:[%f, %f, %f, %f]') ", bbox.get(0), bbox.get(1), bbox.get(2), bbox.get(3)));

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

			lWhere.put("lat", Neo4LocationProperties.LATITUDE);
			lWhere.put("lon", Neo4LocationProperties.LONGITUDE);
			lWhere.put("alt", Neo4LocationProperties.ALTITUDE);
			lWhere.put("timestamp", Neo4LocationProperties.TIMESTAMP);
			lWhere.put("speed", Neo4LocationProperties.SPEED);
			lWhere.put("accuracy", Neo4LocationProperties.ACCURACY);

			cypherQuery.append(" WHERE true ");
			for(Entry<String, String> kv : lWhere.entrySet()){

				List<String> t = queryParams.get(kv.getKey());

				int it = (t==null) ? 0 : t.size();

				//				if(it == 1){
				//					
				//				}

				if(it >= 1){

					cypherQuery.append(String.format(Locale.ENGLISH,"AND start.%s >= %s ", kv.getValue(), t.get(0)));

				}

				if (it >= 2){

					cypherQuery.append(String.format(Locale.ENGLISH,"AND start.%s <= %s ",kv.getValue(), t.get(1)));

				}

				if(it >= 3){

					cypherQuery.append(String.format(Locale.ENGLISH,"AND end.%s >= %s ", kv.getValue(), t.get(0)));

				}

				if (it == 4){

					cypherQuery.append(String.format(Locale.ENGLISH,"AND end.%s <= %s ",kv.getValue(), t.get(1)));

				}

			}

			//TODO: Create String Point.parse(String property, String value)

			//#########################RETURN##########################

			cypherQuery.append(" RETURN user, trajectory, relationships(p) AS rels ");
			
			//#########################SUM##########################


			List<String> lSum = queryParams.get("sum");

			if(lSum != null){

				for(String sum : lSum){

					if(sum.startsWith("n.")){
						sum = sum.substring(2);
						cypherQuery.append(String.format(", reduce(c = 0, n IN nodes(p) | c + n.%s) AS %s ", sum, sum));
					}
					else if(sum.startsWith("r.")){
						sum = sum.substring(2);
						cypherQuery.append(String.format(", reduce(c = 0, r IN relationships(p) | c + r.%s) AS %s ", sum, sum));
					}
					else {
						//TODO: Throw
					}

				}

			}

			List<String> lOrderBy = queryParams.get("orderBy");
			int lOrderBySize = (lOrderBy==null) ? 0 : lOrderBy.size();

			if(lOrderBySize > 0){	

				cypherQuery.append(String.format(" ORDER BY %s ", lOrderBy.remove(0).substring(2)));

				for(String orderBy : lOrderBy){
					orderBy = orderBy.substring(2);
					cypherQuery.append(String.format(", %s",orderBy));

				}

			}

			List<String> lSkip = queryParams.get("skip");
			int lSkipSize = (lSkip==null) ? 0 : lSkip.size();

			if(lSkipSize == 1){	

				cypherQuery.append(String.format(" SKIP %s ",lSkip.get(0)));

			}

			List<String> lLimit = queryParams.get("limit");
			int lLimitSize = (lLimit==null) ? 0 : lLimit.size();

			if(lLimitSize == 1){	

				cypherQuery.append(String.format(" LIMIT %s ", lLimit.get(0)));

			}



		} catch(Exception e){

			logger.error(e.toString());
			for(StackTraceElement st :e.getStackTrace()){
				logger.error(st.toString());
			}

		}

		//logger.error(cypherQuery.toString());

		//String r = "";

		final StreamingOutput stream = new StreamingOutput(){
			@Override
			public void write(OutputStream os) throws IOException, WebApplicationException
			{
				try (Transaction tx = mDb.beginTx()){

					Result result = mDb.execute(cypherQuery.toString(), params);

					JsonFactory jsonfactory = new JsonFactory();
					JsonGenerator jg = jsonfactory.createGenerator(os);
					jg.setCodec(new ObjectMapper());

					jg.writeStartArray();
					//logger.error("start array");
					
					while (result.hasNext())
					{

						User user = null;
						String trajName = null;
						//Collection<Move> mvs = new ArrayList<>();
						Collection<Relationship> rels = new ArrayList<>();
						//Map<String,Object> props = new HashMap<>();

						Map<String,Object> row = result.next();
						
						jg.writeStartObject();
						for (String key : result.columns())
						{

							Object value = row.get(key);

							if(value instanceof Node){

								Node t = (Node) value;

								if(t.hasLabel(Neo4LocationLabels.USER)){
									String personName = (String) t.getProperty(Neo4LocationProperties.USERNAME);
									user = new User(personName);
									jg.writeObjectField("user", user);

								} else {
									trajName = (String) t.getProperty(Neo4LocationProperties.TRAJNAME);

									jg.writeStringField("trajectoryName", trajName);
									
									
								}

							}
							//							else if(value instanceof Relationship){
							//								rel.add((Relationship) value);
							//							}
							else if(value instanceof List<?>){

								jg.writeArrayFieldStart("moves");

								rels = (List<Relationship>) value;
								for(Relationship rel :rels){
									Move m = new Neo4JMove(rel).getMove();
									//mvs.add(m);
									jg.writeObject(m);
								}

								jg.writeEndArray();

							} else {
								//props.put(key, value.toString());
								
								
								jg.writeObjectFieldStart("semanticData");
								
								//Aglomerar todas as props de trajectoria e colocar um for
								//para iterar sobre elas
								jg.writeStringField(key, value.toString()); 
								
								jg.writeEndObject();
								///logger.error(key,value.toString());
							}

						}

						jg.writeEndObject();
						//jg.writeObject(new Trajectory(trajName, user, mvs, props));

					}

					//logger.error("end array");
					jg.writeEndArray();
					jg.flush();
					jg.close();

					tx.success();


				} catch (Exception e) {

					logger.error(e.toString());
					for(StackTraceElement st :e.getStackTrace()){
						logger.error(st.toString());
					}


				}

			}
		};
		
		
		//			ObjectWriter mapper = new ObjectMapper().writerWithDefaultPrettyPrinter();
		//			Collection<Trajectory> trajectories = toTrajectories(result);
		//			r = mapper.writeValueAsString(trajectories);

		
		return Response.status(Response.Status.OK).entity(stream).type(MediaType.APPLICATION_JSON).build();
	}

	private Collection<Trajectory> toTrajectories(Result result){

		//		Collection<Trajectory> trajs = new HashSet<>();
		//
		//		
		//
		//
		//			for(Relationship t : rel){
		//
		//				Move m = new Neo4JMove(t).getMove();
		//				mvs.add(m);
		//			}	
		//
		//			trajs.add(new Trajectory(trajName, person, mvs, props));
		//
		//		}
		//
		//
		//		return trajs;
		return null;

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