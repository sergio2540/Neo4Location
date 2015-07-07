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
  
  //Semantic Information (Location)
  public final String ADDRESS = "ADDRESS";
  public final String PLACE_NAME = "PLACE_NAME";
  public final String PLACE_PHONE_NUMBER = "PLACE_PHONE_NUMBER";
  public final String LANGUAGE = "LANGUAGE";
  public final String TIMETABLE = "TIMETABLE";
  
  public final String PLACE_RATING = "PLACE_RATING";
  public final String PLACE_PRICE = "PLACE_PRICE";
  public final String PLACE_VICINITY = "PLACE_VICINITY";
  public final String PLACE_STATUS = "PLACE_STATUS";
  public final String WEBSITE = "WEBSITE";
  public final String PLACE_TYPE = "PLACE_TYPE";
  
}