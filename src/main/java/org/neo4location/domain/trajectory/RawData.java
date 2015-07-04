package org.neo4location.domain.trajectory;

import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;


@JsonInclude(JsonInclude.Include.NON_NULL)
public final class RawData implements Serializable, Comparable<RawData> { 

  private static final long serialVersionUID = 1L;

  private double mLatitude;
  private double mLongitude;
  private long mTime;

  //private final boolean mHasAltitude;
  private Double mAltitude;

  //private final boolean mHasSpeed;
  private Float mSpeed;

  //private final boolean mHasBearing;
  private Float mBearing;

  //private final boolean mHasAccuracy;
  private Float mAccuracy;


  //public RawData(){
  //
  //	}


  @JsonCreator
  public RawData(@JsonProperty("latitude") double latitude, 
      @JsonProperty("longitude")double longitude, 
      @JsonProperty("time") long time, 
      @JsonProperty("altitude") Double altitude, 
      @JsonProperty("accuracy") Float accuracy, 
      @JsonProperty("speed") Float speed, 
      @JsonProperty("bearing") Float bearing){

    mLatitude = latitude;
    mLongitude = longitude;
    mTime = time;

    //		if(altitude.isPresent()){
    //			mHasAltitude = true; 
    //			mAltitude = altitude.get();
    //		} else {
    //			mHasAltitude = false;
    //			mAltitude = 0.0f;
    //		}

    mAltitude = altitude;

    //		if(speed.isPresent()){
    //			mHasSpeed = true;
    //			mSpeed = speed.get();
    //		} else {
    //			mHasSpeed = false;
    //			mSpeed = speed;
    //		}

    mSpeed = speed;

    if(bearing != null){

      float _bearing = bearing;

      while (_bearing < 0.0f) {
        _bearing += 360.0f;
      }
      while (_bearing >= 360.0f) {
        _bearing -= 360.0f;
      }

      mBearing = _bearing;
      //mHasBearing = true;

    } else {

      //mHasBearing = false;
      mBearing = null;

    }

    //		if(accuracy.isPresent()){
    //			mHasAccuracy = true;
    //			mAccuracy = accuracy.get();
    //		} else {
    //			mHasAccuracy = false;
    //			mAccuracy = 0.0f;
    //		}

    mAccuracy = accuracy;

  }

  /**
   * Get the latitude, in degrees.
   *
   */
  public double getLatitude() {
    return mLatitude;
  }

  /**
   * Get the longitude, in degrees.
   *
   */
  public double getLongitude() {
    return mLongitude;
  }

  /**
   * Return the UTC time of this fix, in milliseconds since January 1, 1970.
   * @return time of fix, in milliseconds since January 1, 1970.
   */
  public long getTime() {
    return mTime;
  }


  //	/**
  //	 * True if this location has an altitude.
  //	 */
  //	public boolean hasAltitude() {
  //		return mHasAltitude;
  //	}

  /**
   * Get the altitude if available, in meters above the WGS 84 reference
   * ellipsoid.
   *
   * <p>If this location does not have an altitude then null is returned.
   */
  public Double getAltitude() {
    return mAltitude;
  }

  //	/**
  //	 * True if this location has a speed.
  //	 */
  //	public boolean hasSpeed() {
  //		return mHasSpeed;
  //	}

  /**
   * Get the speed if it is available, in meters/second over ground.
   *
   * <p>If this location does not have a speed then 0.0 is returned.
   */
  public Float getSpeed() {
    return mSpeed;
  }

  //	/**
  //	 * Set the UTC time of this fix, in milliseconds since January 1,
  //	 * 1970.
  //	 *
  //	 * @param time UTC time of this fix, in milliseconds since January 1, 1970
  //	 */
  	public void setTime(long time) {
  		mTime = time;
  	}


  //	/**
  //	 * Set the latitude, in degrees.
  //	 */
  public void setLatitude(double latitude) {
  		mLatitude = latitude;
  	}



  //	/**
  //	 * Set the longitude, in degrees.
  //	 */
  	public void setLongitude(double longitude) {
  		mLongitude = longitude;
  	}


  //	/**
  //	 * Set the altitude, in meters above the WGS 84 reference ellipsoid.
  //	 *
  //	 * <p>Following this call {@link #hasAltitude} will return true.
  //	 */
  	public void setAltitude(double altitude) {
  		mAltitude = altitude;
  
  	}


  //	/**
  //	 * Set the speed, in meters/second over ground.
  //	 *
  //	 * <p>Following this call {@link #hasSpeed} will return true.
  //	 */
  public void setSpeed(float speed) {
    mSpeed = speed;

  }




  //	/**
  //	 * Set the bearing, in degrees.
  //	 *
  //	 * <p>Bearing is the horizontal direction of travel of this device,
  //	 * and is not related to the device orientation.
  //	 *
  //	 * <p>The input will be wrapped into the range (0.0, 360.0].
  //	 */
  public void setBearing(float bearing) {
    while (bearing < 0.0f) {
      bearing += 360.0f;
    }
    while (bearing >= 360.0f) {
      bearing -= 360.0f;
    }
    mBearing = bearing;
  }



  /**
   * Get the bearing, in degrees.
   *
   * <p>Bearing is the horizontal direction of travel of this device,
   * and is not related to the device orientation. It is guaranteed to
   * be in the range (0.0, 360.0] if the device has a bearing.
   *
   * <p>If this location does not have a bearing then 0.0 is returned.
   */
  public Float getBearing() {
    return mBearing;
  }

  /**
   * Get the estimated accuracy of this location, in meters.
   *
   * <p>We define accuracy as the radius of 68% confidence. In other
   * words, if you draw a circle centered at this location's
   * latitude and longitude, and with a radius equal to the accuracy,
   * then there is a 68% probability that the true location is inside
   * the circle.
   *
   * <p>In statistical terms, it is assumed that location errors
   * are random with a normal distribution, so the 68% confidence circle
   * represents one standard deviation. Note that in practice, location
   * errors do not always follow such a simple distribution.
   *
   * <p>This accuracy estimation is only concerned with horizontal
   * accuracy, and does not indicate the accuracy of bearing,
   * velocity or altitude if those are included in this Location.
   *
   * <p>If this location does not have an accuracy, then 0.0 is returned.
   */
  public Float getAccuracy() {
    return mAccuracy;
  }

  //	/**
  //	 * Set the estimated accuracy of this location, meters.
  //	 *
  //	 * <p>See {@link #getAccuracy} for the definition of accuracy.
  //	 *
  //	 * <p>Following this call {@link #hasAccuracy} will return true.
  //	 */
  public void setAccuracy(float accuracy) {
    mAccuracy = accuracy;

  }

  @Override
  public int hashCode()
  {
    //TODO: Hash Code
    // ,mAltitude,mSpeed, mAccuracy

    //return super.hashCode();

    return Objects.hash(Double.valueOf(mLatitude),Double.valueOf(mLongitude),Long.valueOf(mTime));
  }

  @Override
  public boolean equals(final Object obj) {

    return Objects.nonNull(obj) &&
        obj instanceof RawData &&
        Objects.equals(mLatitude, ((RawData) obj).getLatitude()) &&
        Objects.equals(mLongitude, ((RawData) obj).getLongitude()) &&
        Objects.equals(mTime, ((RawData) obj).getTime());	

  }

  @Override
  public String toString() {
    StringBuilder s = new StringBuilder();

    s.append(String.format("lat=%f lon=%f time=%d", mLatitude, mLongitude, mTime));


    if (mAltitude != null) 
      s.append(" alt=").append(mAltitude);
    else
      s.append(" alt=?");	


    if (mAccuracy != null) 
      s.append(String.format(" acc=%.0f", mAccuracy));
    else 
      s.append(" acc=?");	

    if (mSpeed != null) 
      s.append(" vel=").append(mSpeed);
    else 
      s.append(" vel=?");	


    if (mBearing != null) 
      s.append(" bear=").append(mBearing);
    else 
      s.append(" bear=?");


    return s.toString();
  }


  @Override
  public int compareTo(RawData o) {

    //Natural ordering time only
    return Long.compare(mTime, o.getTime());

  }


}