package org.neo4location.parameters;

import java.io.Serializable;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class IntegrationParams implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  //Identification
  private long mMinStopTime;
  private double mMaxDistance;
  
  
  public IntegrationParams(){  
  
  }

  public IntegrationParams(long minStopTime){
    setMinStopTime(minStopTime);
  }

  public IntegrationParams(long minStopTime, double maxDistance){
    setMinStopTime(minStopTime);
    setMaxDistance(maxDistance);
  }

  public long getMinStopTime() {
    return mMinStopTime;
  }

  public void setMinStopTime(long mMinStopTime) {
    this.mMinStopTime = mMinStopTime;
  }

  public double getMaxDistance() {
    return mMaxDistance;
  }

  public void setMaxDistance(double mMaxDistance) {
    this.mMaxDistance = mMaxDistance;
  }
  
}