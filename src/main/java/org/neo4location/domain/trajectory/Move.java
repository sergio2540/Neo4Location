package org.neo4location.domain.trajectory;


import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;

import org.neo4location.domain.Neo4LocationRelationships;

import com.esotericsoftware.kryo.serializers.BeanSerializer;
import com.esotericsoftware.kryo.serializers.DefaultSerializers.EnumSerializer;
import com.esotericsoftware.kryo.serializers.DefaultSerializers.StringSerializer;
import com.esotericsoftware.kryo.serializers.FieldSerializer;
import com.esotericsoftware.kryo.serializers.JavaSerializer;
import com.esotericsoftware.kryo.serializers.FieldSerializer.Bind;
import com.esotericsoftware.kryo.serializers.MapSerializer.BindMap;


//@JsonTypeInfo(
//use = JsonTypeInfo.Id.NAME, 
//include = JsonTypeInfo.As.EXTERNAL_PROPERTY, 
//property = "type"
//)
//@JsonSubTypes({
//	@JsonSubTypes.Type(value = Neo4JMove.class, name = "Neo4JMove"),
//})
public class Move implements Serializable  {
	
//	/**
//	 * 
//	 */

private static final long serialVersionUID = 1L;

	//@Bind(JavaSerializer.class)
	private Neo4LocationRelationships mRelationship;
	
	//@Bind(BeanSerializer.class)
	private Point mFrom;
	
	//@Bind(BeanSerializer.class)
	private Point mTo;
	
//	@BindMap(valueSerializer = FieldSerializer.class, 
//			keySerializer = StringSerializer.class, 
//			valueClass = Object.class, 
//			keyClass = String.class, 
//			keysCanBeNull = true)
	private Map<String, Object> mSemanticData;
	
	
	
	public Move(){
	}
	
	public Move(Neo4LocationRelationships rel, Point from, Point to, Map<String, Object> sd){

		mRelationship = rel;
		mFrom = from;
		mTo = to;
		mSemanticData = sd;
	}
	
	public Neo4LocationRelationships getRelationship() {
		return Neo4LocationRelationships.MOVE;
	}


	public void setRelationship(Neo4LocationRelationships rel) {
		
		//Se descomentado causa erro
		//mRelationship = rel;

	}
	
	
	public Point getFrom() {
		return mFrom;
	}

	public void setFrom(Point from) {
		mFrom = from;
	}

	public Point getTo() {
		return mTo;
	}

	public void setTo(Point to) {
		mTo = to;
	}


	public Map<String,Object> getSemanticData() {

		return mSemanticData;

	}


	public void setSemanticData(Map<String,Object> sd) {

		mSemanticData = sd;
	}
	
	@Override
	public String toString(){
		
		StringBuilder sb = new StringBuilder();
		
		sb.append(String.format("Move:  rel: %s from: %s to: %s", mRelationship, mFrom, mTo));
		
		for(Entry<String, Object> kv : mSemanticData.entrySet())
			sb.append(String.format("%s: %s", kv.getKey(), kv.getValue()));
		
		return sb.toString();
		
	}



	//	public Move(Point from, Point to, double durationInSeconds, double distanceInMeters, ){
	//	
	//		mFrom = from;
	//		mTo = to;
	//		
	//		mDurationInSeconds = durationInSeconds;
	//		mDistanceInMeters = distanceInMeters;
	//		
	//		mSemanticData = properties;
	//		
	//	}


}