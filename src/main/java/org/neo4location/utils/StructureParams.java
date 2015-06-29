package org.neo4location.utils;

import java.io.Serializable;


import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL) 
public class StructureParams implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private long mMinStopTime;
  private double mMaxDistance;
  
  //Structure
  private float mSpeedThreshold;
  private double mDelta1;
  private double mDelta2;
  
  public StructureParams(){  
  }
  
  public StructureParams(long minStopTime, double maxDistance){
    setMinStopTime(minStopTime);
    setMaxDistance(maxDistance);
  }
  
  public StructureParams(float speedThreshold, double delta1, double delta2){
    
    setSpeedThreshold(speedThreshold);
    setDelta1(delta1);
    setDelta2(delta2);
  
  }
  
  
  public long getMinStopTime() {
    return mMinStopTime;
  }
  
  public void setMinStopTime(long mMinStopTime) {
    this.mMinStopTime = mMinStopTime;
  }

  public float getSpeedThreshold() {
    return mSpeedThreshold;
  }

  public void setSpeedThreshold(float mSpeedThreshold) {
    this.mSpeedThreshold = mSpeedThreshold;
  }

  public double getDelta1() {
    return mDelta1;
  }

  public void setDelta1(double mDelta1) {
    this.mDelta1 = mDelta1;
  }

  public double getDelta2() {
    return mDelta2;
  }

  public void setDelta2(double mDelta2) {
    this.mDelta2 = mDelta2;
  }

  public double getMaxDistance() {
    return mMaxDistance;
  }

  public void setMaxDistance(double mMaxDistance) {
    this.mMaxDistance = mMaxDistance;
  }

}