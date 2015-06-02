package org.neo4location.domain.trajectory;


import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;



public final class SemanticData implements Serializable {

	
  private static final long serialVersionUID = 1L;
  
  final Map<String,Object> mSemanticData;

	//	public SemanticData(){
	//	}

	@JsonCreator
	public SemanticData(@JsonProperty("semanticData") Map<String,Object> semanticData){
		mSemanticData = new ConcurrentHashMap<String, Object>(semanticData);
	}


	public  Map<String,Object> getSemanticData(){
		return mSemanticData;
	}

	/*
	public void setSemanticData(Map<String,Object> semanticData){

		mSemanticData = semanticData;

	}
	 */


	@Override
	public int hashCode()
	{
		//TODO: Hash Code
		return Objects.hashCode(mSemanticData);
	}


	@Override
	public boolean equals(final Object obj) {

		return Objects.nonNull(obj) &&
				obj instanceof SemanticData &&
				Objects.equals(mSemanticData, ((SemanticData) obj).getSemanticData());	

	}

	@Override
	public String toString() {

		StringBuilder s = new StringBuilder();

		for(Entry<String, Object> kv: mSemanticData.entrySet()){
			
			s.append(String.format("%s= %s ", kv.getKey(), kv.getValue().toString()));
		
		}

		return s.toString();
	}


 

}