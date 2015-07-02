package org.neo4location.domain;


public interface Neo4LocationProperties {

  //public interface Point {
  //}
  
  //public interface Move {
  //}
  
  public final String USERNAME = "USERNAME";
  public final String TRAJNAME = "TRAJNAME";

  public final String LATITUDE = "lat";
  public final String LONGITUDE = "lon";
  public final String ALTITUDE = "alt";

  /**
   *Get the estimated ACCURACY of this location, in meters.
   */
  public final String ACCURACY = "ACCURACY";
  public final String TIMESTAMP = "TIMESTAMP";
  public final String SPEED = "SPEED";

  /**
   * Get the BEARING, in degrees.
   * Bearing is the horizontal direction of travel of this device, 
   * and is not related to the device orientation. 
   * FLOAT
   * It is guaranteed to be in the range (0.0, 360.0] if the device has a BEARING.
   */
  public final String BEARING = "BEARING";


  public final String CONFIDENCE = "CONFIDENCE";

  public final String DISTANCE = "d";
  public final String DURATION = "t";

  public final String CENTER = "CENTER";
  public final String MBR = "MBR";
  
  public final String START_INSTANT = "START_INSTANT";
  public final String END_INSTANT = "END_INSTANT";
  public final String WKT = "WKT";
  
  public final Object ADDRESS = "ADDRESS";
  
}