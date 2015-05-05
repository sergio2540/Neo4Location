package org.neo4location.domain.trajectory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.neo4location.domain.Neo4LocationLabels;

import com.esotericsoftware.kryo.serializers.BeanSerializer;
import com.esotericsoftware.kryo.serializers.CollectionSerializer;
import com.esotericsoftware.kryo.serializers.CollectionSerializer.BindCollection;
import com.esotericsoftware.kryo.serializers.CompatibleFieldSerializer;
import com.esotericsoftware.kryo.serializers.DefaultSerializers.StringSerializer;
import com.esotericsoftware.kryo.serializers.FieldSerializer;
import com.esotericsoftware.kryo.serializers.FieldSerializer.Bind;
import com.esotericsoftware.kryo.serializers.MapSerializer.BindMap;


public class Trajectory implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	//@Bind(StringSerializer.class)
	private String mTrajectoryName;

	//@Bind(BeanSerializer.class)
	private User mUser;

//	@BindCollection(
//			elementSerializer = FieldSerializer.class,
//			elementClass = ArrayList.class, 
//			elementsCanBeNull = true) 
	private Collection<Move> mMoves = new ArrayList<Move>();

	//	@BindMap(valueSerializer = StringSerializer.class, 
	//			keySerializer = FieldSerializer.class, 
	//			valueClass = Object.class, 
	//			keyClass = String.class, 
	//			keysCanBeNull = true)
	private Map<String,Object> mSemanticData = new HashMap<>();


	public Trajectory(){

	}

	public Trajectory(String trajectoryName, User user, Collection<Move> moves, Map<String,Object> semanticData){

		mTrajectoryName = trajectoryName;
		mUser = user;
		mMoves = moves;
		mSemanticData = semanticData;

	}


	public String getTrajectoryName(){
		return mTrajectoryName;	
	}

	public void setTrajectoryName(String trajectoryName){
		mTrajectoryName = trajectoryName;	
	}

	public User getUser(){
		return mUser;	
	}

	public void setUser(User user){
		mUser = user;	
	}


	public Collection<Move> getMoves(){
		return mMoves;

	}

	public void setMoves(Collection<Move> moves){
		mMoves = moves;
	}

	public Map<String, Object> getSemanticData() {
		return mSemanticData;
	}

	public void setSemanticData(Map<String, Object> semanticData) {
		this.mSemanticData = semanticData;
	}

	@Override
	public String toString(){

		StringBuilder sb = new StringBuilder();

		sb.append(String.format("[Trajectory %s %s", mTrajectoryName, mUser));

		for(Move move : mMoves){
			sb.append(String.format(" %s", move));
		}

		sb.append("]");

		return sb.toString();
	}

}