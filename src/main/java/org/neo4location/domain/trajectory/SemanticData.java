package org.neo4location.domain.trajectory;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class SemanticData {

	Map<String,Object> mSemanticData = new HashMap<>();

	public SemanticData(){
	}
	
	public SemanticData(Map<String,Object> semanticData){
		mSemanticData = semanticData;
	}
	
	
	public  Map<String,Object> getSemanticData(){
		return mSemanticData;
	}
	
	public void setSemanticData(Map<String,Object> semanticData){
		
		mSemanticData = semanticData;
		//for(Entry<String, Object> kv: kvs){
			//mSemanticData.put(kv.getKey(), kv.getValue());
		//}
	}

//	public Object getSemanticValue(String key){
//		return mSemanticData.get(key);
//	}
//
//	public void setSemanticValue(String key, Object semanticValue){
//		mSemanticData.put(key, semanticValue);
//	}

	@Override
	public String toString() {
		
		StringBuilder s = new StringBuilder();
	
		getSemanticData()
		.forEach((k,v) -> s.append(String.format("%s= %s ", k, v.toString())));
		
		return s.toString();
	}

	public Map<String, String> toMap() {
		
		final Map<String,String> map = new HashMap<String, String>();
		
		getSemanticData().forEach((k,v) -> map.put(k, v.toString()));
		
		return map;
	}

}
