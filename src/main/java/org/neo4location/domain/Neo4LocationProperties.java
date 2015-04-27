package org.neo4location.domain;

public interface Neo4LocationProperties {

	public Neo4LocationProperties create();

	/**
	 * Get Key from this property.
	 *
	 */
	public String getKey();

	/**
	 * Get Value from this property.
	 *
	 */

	public String getValue();
	
	public final String USERNAME = "USERNAME";
	public final String TRAJNAME = "TRAJNAME";
	
	//public final String DISTANCE = "DISTANCE";
	//public final String duration = "duration";
	//public final String INSTANT = "instant";
	
	public final String LATITUDE = "lat";
	public final String LONGITUDE = "lon";
	public final String ALTITUDE = "ALTITUDE";
	
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
	
}