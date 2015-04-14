package org.neo4location.domain.trajectory;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class SemanticData {
	
	Map<String,Object> mSemanticData;
	
	public SemanticData(Map<String,Object> semanticData){
		mSemanticData = semanticData;
	}
	
	public  Set<Entry<String, Object>> getKeysAndValues(){
		return mSemanticData.entrySet();
	}
	
	public Object getSemanticValue(String key){
		return mSemanticData.get(key);
	}
	
	public void setSemanticValue(String key, Object semanticValue){
		mSemanticData.put(key, semanticValue);
	}

}
