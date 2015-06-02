package org.neo4location.plugins.io;


import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.neo4location.domain.trajectory.Trajectory;
import org.neo4location.domain.trajectory.Person;
import org.neo4location.graphdb.Neo4JMove;
import org.neo4location.server.plugins.Neo4LocationRESTService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xerial.snappy.SnappyFramedOutputStream;
import org.xerial.snappy.SnappyOutputStream;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.pool.KryoCallback;
import com.esotericsoftware.kryo.pool.KryoFactory;
import com.esotericsoftware.kryo.pool.KryoPool;
import com.esotericsoftware.kryo.serializers.BeanSerializer;
import com.esotericsoftware.kryo.serializers.CollectionSerializer;
import com.esotericsoftware.kryo.serializers.FieldSerializer;

public class Neo4LocationOutputStreamKryo implements StreamingOutput {

	public final static Logger logger = LoggerFactory.getLogger(Neo4LocationRESTService.class);
	
	private GraphDatabaseService mDb;
	private String mCypherQuery;
	private Map<String,Object> mParams;
	
	private final static KryoFactory factory = new KryoFactory() {
		public Kryo create () {
			Kryo kryo = new Kryo();
			// configure kryo instance, customize settings
			return kryo;
		}
	};
	
	private static final KryoPool mPool = new KryoPool.Builder(factory).softReferences().build();;
	
	public Neo4LocationOutputStreamKryo(GraphDatabaseService db, String cypherQuery, Map<String,Object> params) {

		mDb = db;
		mCypherQuery = cypherQuery;
		mParams = params;
		
	}


	@Override
	public void write(OutputStream os) throws IOException,
	WebApplicationException{


		//OutputStream snappyOs = os;
		OutputStream snappyOs = new SnappyFramedOutputStream(os);
		
		final Output out = new Output(snappyOs);

		final Collection<Trajectory> trajs = query();
		
		
		mPool.run(new KryoCallback<Void>() {
			public Void execute(Kryo kryo) {
				kryo.writeClassAndObject(out, trajs);
				return null;
			}
		});

		out.flush();
		os.flush();

		//logger.error(""+os..total());
		logger.error(""+out.total());
		out.close();
		os.close();


	}


	private Collection<Trajectory> query() {
		
		Collection<Trajectory> trajs = new ArrayList<Trajectory>();
		
		try (Transaction tx = mDb.beginTx()){

			Result result = mDb.execute(mCypherQuery, mParams);

			while (result.hasNext())
			{

				Person person = null;
				String trajName = null;
				Collection<Move> mvs = new ArrayList<>();
				Collection<Relationship> rels = new ArrayList<>();
				Map<String,Object> props = new HashMap<>();


				Map<String,Object> row = result.next();

				//jg.writeStartObject();

				for (String key : result.columns())
				{

					Object value = row.get(key);

					if(value instanceof Node){

						Node t = (Node) value;

						if(t.hasLabel(DynamicLabel.label(Neo4LocationLabels.USER.name())))
						{

							String personName = (String) t.getProperty(Neo4LocationProperties.USERNAME);
							person = new Person(personName);

							//jg.writeObjectField("user", user);

							//							BeanSerializer<Person> buser = new BeanSerializer<Person>(mKryo, Person.class);
							//
							//							buser.write(mKryo, out,  user);

						} else {
							trajName = (String) t.getProperty(Neo4LocationProperties.TRAJNAME);

							//jg.writeStringField("trajectoryName", trajName);

							//							FieldSerializer<String> someClassSerializer = new FieldSerializer<String>(mKryo, String.class);
							//							someClassSerializer.write(mKryo, out,  trajName);


						}

					}
					//							else if(value instanceof Relationship){
					//								rel.add((Relationship) value);
					//							}
					else if(value instanceof List<?>){

						//jg.writeArrayFieldStart("moves");

						rels = (List<Relationship>) value;
						for(Relationship rel :rels){
							Move m = new Neo4JMove(rel).getMove();
							mvs.add(m);
							//jg.writeObject(m);
						}

						//jg.writeEndArray();

					} else {

						props.put(key, value.toString());

						//Aglomerar todas as props de trajectoria e colocar um for
						//para iterar sobre elas
						//jg.writeStringField(key, value.toString()); 


					}

				}


				trajs.add(new Trajectory(trajName, person, mvs, props));



			}

			tx.success();


			
			
			//mKryo.writeClassAndObject(out, trajs);

			
			//throw new Exception();

		} catch (Exception e) {

			logger.error(e.toString());
			for(StackTraceElement st :e.getStackTrace()){
				logger.error(st.toString());
			}


		}
		
		return trajs;
	}




}
