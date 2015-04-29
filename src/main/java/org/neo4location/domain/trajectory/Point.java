package org.neo4location.domain.trajectory;


import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.graphdb.Label;
import org.neo4location.domain.Neo4LocationLabels;


public class Point {

	private RawData mRawData;
	private SemanticData mSemanticData;
	private Collection<Neo4LocationLabels> mNeo4jLabels;

	
	public Point(){
	}
	
	public Point(RawData rawData, SemanticData semanticData, Collection<Neo4LocationLabels> labels){

		mRawData = rawData;
		mSemanticData = semanticData;
		mNeo4jLabels = labels;
		
		//Neo4JPoint p = new Neo4JPoint(rawData,semanticData, labels); 

	}
	
	public Collection<Neo4LocationLabels> getLabels() {
		return mNeo4jLabels;
	}
	
	public RawData getRawData() {
		return mRawData;
	}


	public SemanticData getSemanticData() {
		return mSemanticData;
	}

	public void setLabels(Collection<Neo4LocationLabels> labels) {
		mNeo4jLabels = labels;

	}

	public void setRawData(RawData rd) {
		mRawData = rd;
	}

	public void setSemanticData(SemanticData sd) {
		mSemanticData = sd;
	}
	
	public Map<String,String> toMap(){
		
		Map<String,String> rawMap = getRawData().toMap();
		Map<String,String> semanticMap = getSemanticData().toMap();
		
		Map<String,String> map = new HashMap<String, String>();
		
		map.putAll(rawMap);
		map.putAll(semanticMap);
		
		return map;
	}
	
	@Override
	public String toString() {
		 return String.format("[rawData= %s SemanticData= %s", getRawData().toString(), getSemanticData().toString());
	}
}