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
	
	/**
	 *Get the estimated ACCURACY of this location, in meters.
	 */
	
	public final String SPEED = "SPEED";
	public final String DISTANCE = "DISTANCE";
	
	//public final String duration = "duration";
	//public final String INSTANT = "instant";
	
	/**
	 * Get the BEARING, in degrees.
	 * Bearing is the horizontal direction of travel of this device, 
	 * and is not related to the device orientation. 
	 * FLOAT
	 * It is guaranteed to be in the range (0.0, 360.0] if the device has a BEARING.
	 */
	public final String BEARING = "BEARING";
	
	public final String LATITUDE = "LATITUDE";
	public final String LONGITUDE = "LONGITUDE";
	public final String ALTITUDE = "ALTITUDE";


	public final String ACCURACY = "ACCURACY";
	//public final String time = "time";
	public final String TIMESTAMP = "TIMESTAMP";

}