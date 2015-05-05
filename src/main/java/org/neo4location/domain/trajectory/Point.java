package org.neo4location.domain.trajectory;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.graphdb.Label;
import org.neo4location.domain.Neo4LocationLabels;

import com.esotericsoftware.kryo.serializers.BeanSerializer;
import com.esotericsoftware.kryo.serializers.CollectionSerializer;
import com.esotericsoftware.kryo.serializers.CollectionSerializer.BindCollection;
import com.esotericsoftware.kryo.serializers.DefaultSerializers.EnumSerializer;
import com.esotericsoftware.kryo.serializers.DefaultSerializers.StringSerializer;
import com.esotericsoftware.kryo.serializers.FieldSerializer;
import com.esotericsoftware.kryo.serializers.FieldSerializer.Bind;
import com.esotericsoftware.kryo.serializers.MapSerializer.BindMap;


public class Point implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	//@Bind(FieldSerializer.class)
	private RawData mRawData;

//	@BindMap(valueSerializer = FieldSerializer.class, 
//			keySerializer = StringSerializer.class, 
//			valueClass = Object.class, 
//			keyClass = String.class, 
//			keysCanBeNull = true)
	private Map<String,Object> mSemanticData = new HashMap<>();
	
//	@BindCollection(
//			elementSerializer = EnumSerializer.class,
//			elementClass = ArrayList.class, 
//			elementsCanBeNull = true) 
	private Collection<Neo4LocationLabels> mLabels = new ArrayList<>();


	public Point(){
	}

	public Point(RawData rawData, Map<String,Object> semanticData, Collection<Neo4LocationLabels> labels){

		mRawData = rawData;
		mSemanticData = semanticData;
		mLabels = labels;

		//Neo4JPoint p = new Neo4JPoint(rawData,semanticData, labels); 

	}

	public Collection<Neo4LocationLabels> getLabels() {
		return mLabels;
	}

	public RawData getRawData() {
		return mRawData;
	}


	public Map<String,Object>  getSemanticData() {
		return mSemanticData;
	}

	public void setLabels(Collection<Neo4LocationLabels> labels) {
		mLabels = labels;

	}

	public void setRawData(RawData rd) {
		mRawData = rd;
	}

	public void setSemanticData(Map<String,Object> sd) {
		mSemanticData = sd;
	}

	//	public Map<String,String> toMap(){
	//		
	//		Map<String,String> rawMap = getRawData().toMap();
	//		Map<String,String> semanticMap = getSemanticData().toMap();
	//		
	//		Map<String,String> map = new HashMap<String, String>();
	//		
	//		map.putAll(rawMap);
	//		map.putAll(semanticMap);
	//		
	//		return map;
	//	}

	@Override
	public String toString() {
		return String.format("[rawData= %s SemanticData= %s", getRawData().toString(), getSemanticData().toString());
	}
}