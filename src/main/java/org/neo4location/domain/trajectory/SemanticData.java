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

	public  Set<Entry<String, Object>> getKeysAndValues(){
		return mSemanticData.entrySet();
	}
	
	public void setKeysAndValues(Set<Entry<String, Object>> kvs){
		
		for(Entry<String, Object> kv: kvs){
			mSemanticData.put(kv.getKey(), kv.getValue());
		}
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
	
		for(Entry<String, Object> kv : getKeysAndValues()){
			s.append(String.format("%s= %s ", kv.getKey(), kv.getValue().toString()));
		}

		

		return s.toString();
	}

}
