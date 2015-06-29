package org.neo4location.domain.trajectory;


import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.neo4location.domain.Neo4LocationRelationships;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;



//@JsonTypeInfo(
//use = JsonTypeInfo.Id.NAME, 
//include = JsonTypeInfo.As.EXTERNAL_PROPERTY, 
//property = "type"
//)
//@JsonSubTypes({
//	@JsonSubTypes.Type(value = Neo4JMove.class, name = "Neo4JMove"),
//})

//Threadsafe
public final class Move implements Serializable, Comparable<Move>  {

	private static final long serialVersionUID = 1L;

	//@Bind(JavaSerializer.class)
	private final Neo4LocationRelationships mRelationship;

	//@Bind(BeanSerializer.class)
	private final Point mFrom;

	//@Bind(BeanSerializer.class)
	private final Point mTo;

	//	@BindMap(valueSerializer = FieldSerializer.class, 
	//			keySerializer = StringSerializer.class, 
	//			valueClass = Object.class, 
	//			keyClass = String.class, 
	//			keysCanBeNull = true)
	private final Map<String, Object> mSemanticData;

//	public Move(){
//	}

	@JsonCreator
	public Move(@JsonProperty("relationship") final Neo4LocationRelationships relationship, 
			@JsonProperty("from") final Point from, 
			@JsonProperty("to") final Point to, 
			@JsonProperty("semanticData") final Map<String, Object> semanticData){

		mRelationship = relationship;
		mFrom = from;
		mTo = to;
		mSemanticData = new ConcurrentHashMap<String, Object>(semanticData);
	}

	public Neo4LocationRelationships getRelationship() {
		//TODO:retornar mRelationship
		//return Neo4LocationRelationships.MOVE;
		return mRelationship;
	}

	public Point getFrom() {
		return mFrom;
	}

	public Point getTo() {
		return mTo;
	}

	public Map<String,Object> getSemanticData() {
		return mSemanticData;
	}
	
 /*
	public void setTo(Point to) {
			mTo = to;
	}

	public void setFrom(Point from) {
		mFrom = from;
	}

	public void setRelationship(Neo4LocationRelationships rel) {

		//Se descomentado causa erro
		//mRelationship = rel;

	}

	public void setSemanticData(Map<String,Object> sd) {
		mSemanticData = sd;
	}
*/
	
	@Override
	public int hashCode()
	{
		//TODO:
		return Objects.hash(mRelationship,mFrom, mTo, mSemanticData);
	}

	@Override
	public boolean equals(Object obj )
	{
		return Objects.nonNull(obj) &&
			   obj instanceof Move && 
			   
			   Objects.equals(mRelationship, ((Move)obj).getRelationship()) &&
			   Objects.equals(mFrom, ((Move)obj).getFrom()) &&
			   Objects.equals(mTo, ((Move)obj).getTo()) &&
			   Objects.equals(mSemanticData,  ((Move)obj).getSemanticData());
	
	}


	@Override
	public String toString(){

		StringBuilder sb = new StringBuilder();

		sb.append(String.format("[:%s] from: %s to: %s", mRelationship, mFrom, mTo));

		for(Entry<String, Object> kv : mSemanticData.entrySet())
			sb.append(String.format("%s: %s", kv.getKey(), kv.getValue()));

		return sb.toString();

	}

  @Override
  public int compareTo(Move o) {
    // TODO Auto-generated method stub
    
//    mSemanticData
//    mFrom.getRawData()
//    mTo.getRawData()
    
    return 0;
  }

}