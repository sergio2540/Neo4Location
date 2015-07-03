package org.neo4location.domain.trajectory;


import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.math3.ml.clustering.Clusterable;
import org.neo4location.domain.Neo4LocationLabels;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;




public final class Point implements Serializable, Comparable<Point>, Clusterable {


  private static final long serialVersionUID = 1L;

  private final RawData mRawData;

  private final Map<String,Object> mSemanticData;

  private final Iterable<Neo4LocationLabels> mLabels;


  //	public Point(){
  //	}

  @JsonCreator
  public Point(@JsonProperty("rawData") final RawData rawData, 
      @JsonProperty("semanticData") final Map<String,Object> semanticData, 
      @JsonProperty("labels") final Iterable<Neo4LocationLabels> labels){

    mRawData = rawData;
    mSemanticData = new ConcurrentHashMap<String, Object>(semanticData);
    mLabels = labels;
  }

  public Iterable<Neo4LocationLabels> getLabels() {
    return mLabels;
  }

  public RawData getRawData() {
    return mRawData;
  }


  public Map<String,Object>  getSemanticData() {
    return mSemanticData;
  }

  /*
	public void setLabels(Collection<Neo4LocationLabels> labels) {
		mLabels = labels;

	}

	public void setRawData(RawData rd) {
		mRawData = rd;
	}

	public void setSemanticData(Map<String,Object> sd) {
		mSemanticData = sd;
	}
   */

  @Override
  public int hashCode()
  { 
    return Objects.hashCode(mRawData);
  }

  @Override
  public boolean equals(final Object obj) {

    //Basta verificar raw data	
    return Objects.nonNull(obj) &&
        obj instanceof Point &&
        Objects.equals(mRawData, ((Point) obj).getRawData());	

  }

  @Override
  public String toString() {
    
    return String.format("[rawData= %s SemanticData= %s", getRawData(), getSemanticData());
  
  }

  //Define a natural ordering
  @Override
  public int compareTo(Point o) {
    return 0;
  }

  @Override
  public double[] getPoint() {
    
    double[] point = new double[4];
    if(mRawData != null){
      point[0] = mRawData.getLatitude();
      point[1] = mRawData.getLongitude();
      point[2] = mRawData.getTime();
      
      if(mRawData.getAltitude() != null)
        point[3] = mRawData.getAltitude();
      
      return point;
    }
    
    return null;
  }

}