package org.neo4location.server.plugins;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4location.domain.Neo4LocationLabels;
import org.neo4location.domain.Neo4LocationProperties;
import org.neo4location.domain.trajectory.Move;
import org.neo4location.domain.trajectory.User;
import org.neo4location.graphdb.Neo4JMove;
import org.slf4j.Logger;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Neo4LocationOutputStreamJSON implements StreamingOutput {

	public static Logger logger;
	private GraphDatabaseService mDb;
	private String mCypherQuery;
	private Map<String,Object> mParams;
	
	
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

						if(t.hasLabel(DynamicLabel.label(Neo4LocationLabels.USER.name())))
						{
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

			tx.success();
			//logger.error("end array");
			jg.writeEndArray();
			jg.flush();
			jg.close();




		} catch (Exception e) {

			logger.error(e.toString());
			for(StackTraceElement st :e.getStackTrace()){
				logger.error(st.toString());
			}


		}

	}


}
