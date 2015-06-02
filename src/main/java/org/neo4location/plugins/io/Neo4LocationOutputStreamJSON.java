package org.neo4location.plugins.io;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4location.domain.Neo4LocationLabels;
import org.neo4location.domain.Neo4LocationProperties;
import org.neo4location.domain.trajectory.Move;
import org.neo4location.domain.trajectory.Person;
import org.neo4location.domain.trajectory.Trajectory;
import org.neo4location.graphdb.Neo4JMove;
import org.neo4location.server.plugins.Neo4LocationRESTService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.type.CollectionType;

public class Neo4LocationOutputStreamJSON implements StreamingOutput {

	public final static Logger logger = LoggerFactory.getLogger(Neo4LocationRESTService.class);
	
	private GraphDatabaseService mDb;
	private String mCypherQuery;
	private Map<String,Object> mParams;
	
	private final static ObjectMapper o = new ObjectMapper();
	//private final static CollectionType ct = o.getTypeFactory().constructCollectionType(Collection.class, Trajectory.class);
	private final static ObjectWriter ow = o.writerFor(Trajectory.class);
	
	
	private final static JsonFactory jsonfactory = new JsonFactory();


	public Neo4LocationOutputStreamJSON(GraphDatabaseService db, String cypherQuery, Map<String,Object> params) {

		mDb = db;
		mCypherQuery = cypherQuery;
		mParams = params;
		
	}


	@Override
	public void write(OutputStream os) throws IOException,
	WebApplicationException{


		try (Transaction tx = mDb.beginTx()){

			Result result = mDb.execute(mCypherQuery, mParams);


			final JsonGenerator jg = jsonfactory.createGenerator(os);
			jg.setCodec(new ObjectMapper());
			jg.writeStartArray();
			
			while (result.hasNext())
			{
				
			  Map<String,Object> row = result.next();
				
			  
			  Collection<Relationship> rels = null;
	      Trajectory trajectory = null; 
	      Person person = null;
	      String trajName = null;
	      Collection<Move> moves = null;
	      Map<String,Object> sd = new HashMap<>();
			  
				for(String key : result.columns()) 
				{		

					Object value = row.get(key);

					if(value instanceof Node){

						Node t = (Node) value;

						if(t.hasLabel(DynamicLabel.label(Neo4LocationLabels.USER.name())))
						{
							
							String personName = (String) t.getProperty(Neo4LocationProperties.USERNAME);
							person = new Person(personName);
							
							
						} 
						else {
						  
							trajName = (String) t.getProperty(Neo4LocationProperties.TRAJNAME);
						
						}

					}
					else if(value instanceof List<?>){

						//jg.writeArrayFieldStart("moves");

						rels = (Collection<Relationship>) value;
						
						//Se em outra thread relationship
						moves = rels.stream().map((rel) -> {
							
							final Move m = new Neo4JMove(rel).getMove();
							return m;
						
						}).collect(Collectors.toList());
						
//						for(Relationship rel :rels){
//							Move m = new Neo4JMove(rel).getMove();
//							//mvs.add(m);
//							if(m != null)
//								moves.add(m);
//							//jg.writeObject(m);
//						}

						//jg.writeEndArray();

					} else {
						
						//jg.writeObjectFieldStart("semanticData");

						//Aglomerar todas as props de trajectoria e colocar um for
						//para iterar sobre elas
						//jg.writeStringField(key, value.toString()); 
						sd.put(key, value.toString());

						//jg.writeEndObject();
					
					}


				}
				
				trajectory = new Trajectory(trajName, person, moves, sd);
				
				jg.writeObject(trajectory);
				
				//ow.writeValue(os, trajectory);
				//trajs.add(trajectory);
				
				//jg.writeEndObject();
			}

			tx.success();
			logger.error("end array");
			
			
			//ow.writeValue(os, trajs);
			
			  jg.writeEndArray();
        jg.flush();
        jg.close();




		} catch (IOException e) {

			logger.error(e.toString());
			for(StackTraceElement st :e.getStackTrace()){
				logger.error(st.toString());
			}


		}

	}


}
