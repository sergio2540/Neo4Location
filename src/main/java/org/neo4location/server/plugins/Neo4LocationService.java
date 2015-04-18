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
import org.codehaus.jackson.map.ObjectReader;
import org.codehaus.jackson.map.type.TypeFactory;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4location.domain.trajectory.Move;
import org.neo4location.domain.trajectory.Point;
import org.neo4location.domain.trajectory.RawData;
import org.neo4location.domain.trajectory.SemanticData;
import org.neo4location.graphdb.Neo4JMove;


@Path("")
public class Neo4LocationService {


	private final GraphDatabaseService mDb;
	private org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Neo4LocationService.class);

	//UserResource, TrajectoryResource, 
	public Neo4LocationService( @Context GraphDatabaseService db)
	{
		mDb = db;
		
	}


	@GET
	@Path("/ping")
	@Produces(MediaType.APPLICATION_JSON)
	public Response ping()
	{
		String pong = "pong";
		//logger.error(pong);
		return Response.status(Response.Status.OK).entity(pong).build();

	}


	//{pointId}/{LATITUDE}/{long}/{ALTITUDE}/{TIMESTAMP}{vehicle}
	//last
	//
	//@Suspended final AsyncResponse asyncResponse

	//Provavelmente so havera uma trajectoria deixando de existir a diferencia entre trajetorias raw e semanticas.
	@POST
	@Path("/users/{personName}/trajectories/{trajectoryName}/raw/points")
	@Consumes(MediaType.TEXT_PLAIN)
	//@Produces(MediaType.APPLICATION_JSON) 
	public Response addRawPoint(String _moves, @PathParam("username") String personName, @PathParam("trajectoryName") String trajectoryName)
	{


		ObjectMapper mapper = new ObjectMapper();
		List<Move> moves = new ArrayList<Move>();
		try {
			moves = mapper.readValue(_moves, TypeFactory.defaultInstance().constructParametricType(List.class, Neo4JMove.class));
		} catch (IOException e) {
			logger.error(e.getMessage());
		} 

		//		for(Move m: moves){
		//			logger.error(m.toString());
		//		}

		boolean noTraj = true;
		StringBuilder cypherQuery = new StringBuilder();
		//Map<String, Object> params = new HashMap<String, Object>();

		if(noTraj){
			//Se nao existir trajectory com o mesmo nome cria uma nova trajectoria.
			cypherQuery.append("CREATE (person:Person {personName: {personName}})-[:START_A]->(traj {trajName: {trajectoryName} })");
		}

		if(moves != null){

			boolean isFirst=true;
			Point p;
			for(Move m : moves){

				//adicionar if(m.getLabel() == FROM) 
				if(isFirst){
					p = m.getFrom();
					isFirst=false;
				} 
				else p = m.getTo();


				RelationshipType l = m.getRelationship();
				cypherQuery.append(String.format("-[:%s]->({",l));

				if(p.getRawData() != null){

					RawData	rd = p.getRawData();
					//Falta meter outras raw properties
					cypherQuery.append(String.format(Locale.ENGLISH,"lat: %f, lon: %f, time: %d, accuracy: %f", rd.getLatitude(), rd.getLongitude(), rd.getTime(), rd.getAccuracy()));
				}

				if(p.getSemanticData() != null){
					SemanticData sd = p.getSemanticData();
					//%f em baixo esta ERRADO!!!!!!!!
					for(Entry<String, Object> kv : sd.getKeysAndValues()){
						cypherQuery.append(String.format("%s: %f,",kv.getKey(),kv.getValue()));	
					}
				}
				cypherQuery.append("})");

				//params.put(, value);

			}


		}

		logger.error(cypherQuery.toString());
		
		Map<String,Object>  params = new HashMap();
		
		params.put("personName", personName);
		params.put("trajectoryName", trajectoryName);
		
		try (Transaction tx = mDb.beginTx(); Result result = mDb.execute(cypherQuery.toString(), params))
		{

			//			while ( result.hasNext() )
			//			{

//			r = result.resultAsString();
//			logger.error(r);
			//Map<String,Object> row = result.next();
			//jg.writeString( ((Node) row.get( "colleague" )).getProperty( "name" ).toString() );
			//			}

			tx.success();

		} catch (Exception e) {

			logger.error(e.toString());

		}
		
		
		return Response.status(Response.Status.OK).build();

		//Falta meter end


	}

	@POST
	@Path("/users/{personName}/trajectories/{trajectoryName}/semantic/points/")
	@Consumes(MediaType.APPLICATION_JSON)
	//@Produces(MediaType.APPLICATION_JSON)
	public Response addSemanticPoint(Point rp, @PathParam("personName") String username, @PathParam("trajectoryName") String trajectory_name)
	{
		return null;

	}

	//trajectory_name = index start_timestamp/raw md5(server-id:ISO-8061)
	//user_name = index
	@GET
	@Path("/users/{personName}/trajectories/{trajectoryName}/raw")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getRawTrajectory(@Context UriInfo ui){

		MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
		MultivaluedMap<String, String> pathParams = ui.getPathParameters();
		Map<String, Object> params = new HashMap<String, Object>();

		List<String> username = pathParams.get("personName");	
		int ret = putInParameters("personName", username, params, 1, 1);

		List<String> trajectoryName = pathParams.get("trajectoryName");
		ret = putInParameters("trajectoryName", trajectoryName, params, 1, 1);

		//################################MATCH##############################
		StringBuilder cypherQuery = 
				new StringBuilder("MATCH (person:Person {name: {personName} })-[:START_A]->(trajectory:RawTrajectory {trajName: {trajName} })-[:FROM]->");


		int start = 0;
		int offset = 2;
		String rel = "MOVE_TO";

		if(start > 0){
			cypherQuery.append(String.format("(from)-[:%s*%d..%d]->(to)",rel,start,offset));
		}
		else {
			cypherQuery.append(String.format("(from)<-[:%s*%d..%d]-(to)",rel, start, offset));
		}

		//		List<String> points = queryParams.get("positions");
		//		ret = putInParameters("points", points, params, 0, 2);
		//		//points=<start>-1,<inc>1,<max>
		//
		//		//SKIP
		//		//LIMIT
		//		if(ret == 1){
		//			cypherQuery.append("");
		//		}
		//		if (ret == 2){
		//			cypherQuery.append("");
		//		}

		//################################WHERE##############################

		//		cypherQuery.append("WHERE");
		//
		//		List<String> LATITUDE = queryParams.get("LATITUDE");	//LATITUDE=2.8989,3.099090&LONGITUDE=2.8989,3.099090&ALTITUDE=2.8989,3.099090&time=2,3&points=-1,5&fields=LATITUDE,LONGITUDE
		//		ret = putInParameters("LATITUDE", LATITUDE, params, 0, 2);
		//
		//		if(ret >= 1){
		//			cypherQuery.append("LATITUDE >= {LATITUDE}");
		//		}
		//		if (ret == 2){
		//			cypherQuery.append("LATITUDE ");
		//		}
		//
		//		List<String> LONGITUDE = queryParams.get("LONGITUDE");	//LONGITUDE=2.8989,3.099090
		//
		//		//[LATITUDE, LONGITUDE, ]
		//
		//
		//		List<String> ALTITUDE = queryParams.get("ALTITUDE");	//ALTITUDE=2.8989,3.099090
		//		List<String> time = queryParams.get("time");	//time=2,3
		//


		//List<String> SPEED = queryParams.get("SPEED"); //SPEED=20,20

		//#########################RETURN##########################
		cypherQuery.append("RETURN *");
		List<String> fields = queryParams.get("fields");
		ret = putInParameters("fields",fields, params, 0, Integer.MAX_VALUE);

		//		for(String field: fields){
		//			cypherQuery.append("{field}");
		//		}

		//RETURN DISTINCT {field1}, {field2}

		//		cypherQuery.append("ORDER BY n");
		//		List<String> orderBy = queryParams.get("orderBy");
		//		ret = putInParameters("orderBy",orderBy, params,0, Integer.MAX_VALUE);
		//
		//		for(String ob :orderBy){
		//
		//		}

		logger.error(cypherQuery.toString());

		String r = "";
		try (Transaction tx = mDb.beginTx(); Result result = mDb.execute(cypherQuery.toString(), params))
		{

			//			while ( result.hasNext() )
			//			{

			r = result.resultAsString();
			logger.error(r);
			//Map<String,Object> row = result.next();
			//jg.writeString( ((Node) row.get( "colleague" )).getProperty( "name" ).toString() );
			//			}

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


	private String getRawTrajectoryQuery() {
		// TODO Auto-generated method stub
		return null;
	}

	//	List<String> rels = queryParams.get("rels");
	//	List<String> labels = queryParams.get("labels");

	//	StreamingOutput stream = new StreamingOutput() {
	//
	//		@Override
	//		public void write(OutputStream os) throws IOException, WebApplicationException {
	//			Writer writer = new BufferedWriter(new OutputStreamWriter(os));
	//			for (org.neo4j.graphdb.Path path : paths) {
	//				writer.write(path.toString() + "\n");
	//			}
	//			writer.flush();
	//		}
	//	};


	//	@Override
	//	public String toString() {
	//		return "Neo4LocationService";
	//	}

}