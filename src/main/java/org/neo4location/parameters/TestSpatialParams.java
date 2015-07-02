package org.neo4location.parameters;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class TestSpatialParams implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  
  
  private String mUsername;
  private String mSkip;
  private String mLimit;
  private String mOrderBy;
  private String mSum;


  private double[] mA;
  private double[] mB;

  
  public TestSpatialParams(){  
  }


  public TestSpatialParams(String username, String skip, String limit, String orderBy, String sum){

    setUsername(username);
    setSkip(skip);
    setLimit(limit);
    setOrderBy(orderBy);
    setSum(sum);
    //setBbox(bbox);

  }


  public String getUsername() {
    return mUsername;
  }


  public void setUsername(String username) {
    mUsername = username;
  }


  public String getSkip() {
    return mSkip;
  }


  public void setSkip(String skip) {
    mSkip = skip;
  }


  public String getLimit() {
    return mLimit;
  }


  public void setLimit(String limit) {
    mLimit = limit;
  }


  public String getOrderBy() {
    return mOrderBy;
  }


  public void setOrderBy(String orderBy) {
    mOrderBy = orderBy;
  }

  public String getSum() {
    return mSum;
  }
  
  public void setSum(String sum) {
    mSum = sum;
  }
  
  public double[] getA(){
    return mA;
  }
  
  public double[] getB(){
    return mB;
  }
  
  public void setA(double[] a){
    mA = a;
  }
  
  public void setB(double[] b){
    mB = b;
  }
  
}