package org.neo4location.server.plugins;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4location.domain.Neo4LocationProperties;
import org.neo4location.domain.Neo4LocationRelationships;
import org.neo4location.domain.trajectory.Trajectory;
import org.neo4location.handlers.IdentificationTransactionEventHandler;
import org.neo4location.handlers.StructureTransactionEventHandler;
import org.neo4location.plugins.io.Neo4LocationOutputStreamJSON;
import org.neo4location.plugins.io.Neo4LocationOutputStreamKryo;
import org.neo4location.processing.identification.PredefinedTimeIntervalIdentification;
import org.neo4location.processing.identification.RawGPSGapIdentification;
import org.neo4location.processing.strucuture.DensityBasedStructureF;
import org.neo4location.processing.strucuture.VelocityBasedStructure;
import org.neo4location.utils.IntegrationParams;
import org.neo4location.utils.Neo4LocationService;
import org.neo4location.utils.StructureParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

//@Singleton
@Path("")
public class Neo4LocationRESTService {

  //private GraphDatabaseService mDb;
  private static final Neo4LocationService mNeo4LocationService = new Neo4LocationService();;

  private static final ObjectReader OBJECT_READER_TRAJECTORY = new ObjectMapper().reader(Trajectory.class);
  private static final ObjectReader OBJECT_READER_INTEGRATION = new ObjectMapper().reader(IntegrationParams.class);
  private static final ObjectReader OBJECT_READER_STRUCTURE = new ObjectMapper().reader(StructureParams.class);

  private static final JsonFactory JSON_FACTORY =  new JsonFactory();

  private static final String MEDIATYPE_KRYO = "application/x-kryo";
  private static final int GET_QUERY_PARAMS_START_DEFAULT = 0;
  private static final int GET_QUERY_PARAMS_OFFSET_DEFAULT = 100;
  private static final int GET_QUERY_PARAMS_LIMIT_DEFAULT = 3;
  private static final String GET_QUERY_PARAMS_REL_DEFAULT = Neo4LocationRelationships.MOVE.toString();

  private static MetricRegistry metrics = new MetricRegistry();
  private static final Logger logger = LoggerFactory.getLogger(Neo4LocationRESTService.class);



  //UserResource, TrajectoryResource, 
  public Neo4LocationRESTService(@Context GraphDatabaseService db)
  {

    //Metrics
    //reporter.start();
  }

  //Talvez PUT
  //Em vez de usar queries parameteres usar JSON

  @POST
  @Path("/processing/velocityBasedStructure")
  @Produces(MediaType.APPLICATION_JSON)
  public Response addVelocityBasedStructure(final InputStream stream,  @Context GraphDatabaseService db) throws JsonProcessingException, IOException{

    //    final MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
    //    double delta1 = Double.parseDouble(queryParams.getFirst("delta1"));
    //    double delta2 =  Double.parseDouble(queryParams.getFirst("delta2"));
    //    float speedThreshold =  Float.parseFloat(queryParams.getFirst("speedThreshold"));
    //    long minStopTime =  Long.parseLong(queryParams.getFirst("minStopTime"));
    try{
    StructureParams structure = OBJECT_READER_STRUCTURE.readValue(stream); 

    long minStopTime = structure.getMinStopTime();
    double delta1 = structure.getDelta1();
    double delta2 = structure.getDelta2();
    float speedThreshold = structure.getSpeedThreshold();
   


    VelocityBasedStructure str = new VelocityBasedStructure(speedThreshold, minStopTime, delta1, delta2);
    db.registerTransactionEventHandler(new StructureTransactionEventHandler(db, str));
  }catch(Exception e){
    logger.error(e.getMessage());
  }
    return Response.status(Response.Status.CREATED).build();

  }

  @POST
  @Path("/processing/densityBasedStructure")
  @Produces(MediaType.APPLICATION_JSON)
  public Response addDensityBasedStructure(final InputStream stream, @Context GraphDatabaseService db) throws JsonProcessingException, IOException{

    //    final MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
    //    long minStopTime =  Long.parseLong(queryParams.getFirst("minStopTime"));
    //    double maxDistance = Double.parseDouble(queryParams.getFirst("maxDistance"));
try{
    StructureParams structure = OBJECT_READER_STRUCTURE.readValue(stream); 

    long minStopTime = structure.getMinStopTime();
    double maxDistance = structure.getMaxDistance();


    DensityBasedStructureF str = new DensityBasedStructureF(maxDistance, minStopTime);
    db.registerTransactionEventHandler(new StructureTransactionEventHandler(db, str));
  }catch(Exception e){
    logger.error(e.getMessage());
  }
    return Response.status(Response.Status.CREATED).build();

  }

  @POST
  @Path("/processing/predefinedTimeInterval")
  @Produces(MediaType.APPLICATION_JSON)
  public Response addPredefinedTimeInterval(final InputStream stream, @Context GraphDatabaseService db) throws JsonProcessingException, IOException{

    //    final MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
    //    long minStopTime =  Long.parseLong(queryParams.getFirst("minStopTime"));

    try{
    IntegrationParams integration = OBJECT_READER_INTEGRATION.readValue(stream); 
    
    long minStopTime = integration.getMinStopTime();

    PredefinedTimeIntervalIdentification id = new PredefinedTimeIntervalIdentification(minStopTime);
    db.registerTransactionEventHandler(new IdentificationTransactionEventHandler(db, id));
    
    }catch(Exception e){
      logger.error(e.getMessage());
    }
    return Response.status(Response.Status.CREATED).build();

  }


  @POST
  @Path("/processing/rawGPSGapIdentification")
  @Produces(MediaType.APPLICATION_JSON)
  public Response addRawGPSGapIdentification(final InputStream stream, @Context GraphDatabaseService db) throws JsonProcessingException, IOException{

    //    final MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
    //    long minStopTime =  Long.parseLong(queryParams.getFirst("minStopTime"));
    //    double maxDistance = Double.parseDouble(queryParams.getFirst("maxDistance"));  

    try{
    IntegrationParams integration = OBJECT_READER_INTEGRATION.readValue(stream); 
    long minStopTime = integration.getMinStopTime();
    double maxDistance = integration.getMaxDistance();

    RawGPSGapIdentification id = new RawGPSGapIdentification(maxDistance, minStopTime);
    db.registerTransactionEventHandler(new IdentificationTransactionEventHandler(db, id));
  
    }catch(Exception e){
    logger.error(e.getMessage());
  }
    return Response.status(Response.Status.CREATED).build();

  }




  private final Timer timerAppend = metrics.timer("append");
  private Timer timerReadValue = metrics.timer("readValue");


  @POST
  //@Decompress
  @Path("/trajectories")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response addTrajectories(final InputStream stream, @Context GraphDatabaseService db)
  {

    boolean created = false;

    //Processa Collection Trajectory
    Collection<Trajectory> trajectories = new ArrayList<>();

    try {

      JsonParser jParser = JSON_FACTORY.createParser(stream);


      while (jParser.nextToken() != JsonToken.END_ARRAY){


        jParser.nextToken();

        Trajectory trajectory = OBJECT_READER_TRAJECTORY.readValue(jParser);

        //mNeo4LocationService.appendTrajectory(trajectory, db);
        trajectories.add(trajectory);

      }

      jParser.close();

    } catch (IOException e) {
      logger.error(e.toString());
      for(StackTraceElement st :e.getStackTrace()){
        logger.error(st.toString());
      }
    }


    //final Timer.Context appendContext = timerAppend.time();
    created = mNeo4LocationService.append(trajectories,db);
    //appendContext.stop();

    if(created){
      return Response.status(Response.Status.CREATED).build();
    }
    else {

      return Response.status(Response.Status.OK).build();
    }

  }





  //  	@GET
  //  	@Path("/trajectories")
  //  	@Produces(MEDIATYPE_KRYO)
  //  	public Response getTrajectoryKryo(@Context UriInfo ui){
  //  		
  //  		final MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
  //  		//MultivaluedMap<String, String> pathParams = ui.getPathParameters();
  //  	
  //  		final Map<String, Object> params = new HashMap<String, Object>();		
  //  		final String cypherQuery  =  buildCypherQuery(queryParams, params);
  //  		
  //  		
  //  		final StreamingOutput so = new Neo4LocationOutputStreamKryo(mDb, cypherQuery, params);
  //  		
  //  		Response response = Response.status(Response.Status.OK).entity(so).build();
  //  		
  //  		return response;
  //  	}



  @GET
  //@Compress
  @Path("/trajectories")
  //@Produces(MediaType.APPLICATION_JSON)
  public Response getTrajectoryJSON(@Context UriInfo ui, @Context GraphDatabaseService db) throws Exception {


    final MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
    //MultivaluedMap<String, String> pathParams = ui.getPathParameters();

    final Map<String, Object> params = new HashMap<String, Object>();		
    final String cypherQuery  =  buildCypherQuery(queryParams, params);



    final StreamingOutput so = new Neo4LocationOutputStreamJSON(db, cypherQuery, params);

    Response response = Response.status(Response.Status.OK).entity(so).build();



    return response;
  }





  private String buildCypherQuery(final MultivaluedMap<String, String> queryParams, final Map<String, Object> params) {

    //final Map<String, Object> params = new HashMap<String, Object>();

    final StringBuilder cypherQuery = new StringBuilder(100);

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
      int iStart = (lStart==null || lStart.size() > 1) ? Neo4LocationRESTService.GET_QUERY_PARAMS_START_DEFAULT :  Integer.parseInt(lStart.get(0));

      List<String> lOffset = queryParams.get("offset");
      int iOffset = (lOffset==null || lStart.size() > 1) ? Neo4LocationRESTService.GET_QUERY_PARAMS_OFFSET_DEFAULT : Integer.parseInt(lOffset.get(0));


      List<String> lRel = queryParams.get("rel");
      int lRelSize = (lRel==null) ? 0 : lRel.size();

      if(lRelSize == 0){
        lRel = new ArrayList<String>();
        lRel.add(Neo4LocationRESTService.GET_QUERY_PARAMS_REL_DEFAULT);
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


    return cypherQuery.toString();

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