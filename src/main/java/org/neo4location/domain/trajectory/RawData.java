package org.neo4location.domain.trajectory;

import java.text.DecimalFormat;
import java.util.StringTokenizer;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.distance.GeodesicSphereDistCalc;
import com.spatial4j.core.shape.impl.PointImpl;

public class RawData { 
	

	
	private long mTime = 0;
	private double mLatitude = 0.0;
	private double mLongitude = 0.0;
	
	private boolean mHasAltitude = false;
	private double mAltitude = 0.0f;
	
	private boolean mHasSpeed = false;
	private float mSpeed = 0.0f;
	
	private boolean mHasBearing = false;
	private float mBearing = 0.0f;
	
	private boolean mHasAccuracy = false;
	private float mAccuracy = 0.0f;

	public enum FORMAT { 

		/**
		 * Constant used to specify formatting of a latitude or longitude
		 * in the form "[+-]DDD.DDDDD where D indicates degrees.
		 */
		DEGREES, 

		/**
		 * Constant used to specify formatting of a latitude or longitude
		 * in the form "[+-]DDD:MM.MMMMM" where D indicates degrees and
		 * M indicates minutes of arc (1 minute = 1/60th of a degree).
		 */
		MINUTES, 

		/**
		 * Constant used to specify formatting of a latitude or longitude
		 * in the form "DDD:MM:SS.SSSSS" where D indicates degrees, M
		 * indicates minutes of arc, and S indicates seconds of arc (1
		 * minute = 1/60th of a degree, 1 second = 1/3600th of a degree).
		 */
		SECONDS

	}

	public RawData(double latitude, double longitude, double altitude, float accuracy, float speed, long timestamp){
		
		mLatitude = latitude;
		mLongitude = longitude;
		mTime = timestamp;
		
		if(altitude != -1.0){
			mHasAltitude = true; 
			mAltitude = altitude;
		}
		
		if(accuracy != -1.0){
			mHasAccuracy = false;
			mAccuracy = 0.0f;
		}
		
		if(speed != -1.0){
			mHasSpeed = true;
			mSpeed = speed;
		}
		
		if(speed != -1.0){
			mHasSpeed = true;
			mSpeed = speed;
		}
		
		
		
		
	
		
	}

	/**
	 * Converts a String in one of the formats described by FORMAT_DEGREES, FORMAT_MINUTES, or FORMAT_SECONDS into a double.
	 * @param coordinate
	 * @return
	 * 
	 * @throws NullPointerException if coordinate is null
	 * @throws IllegalArgumentException if the coordinate is not
	 * in one of the valid formats.
	 */
	public static double convert(String coordinate){

		if (coordinate == null) {
			throw new NullPointerException("coordinate");
		}

		boolean negative = false;
		if (coordinate.charAt(0) == '-') {
			coordinate = coordinate.substring(1);
			negative = true;
		}

		StringTokenizer st = new StringTokenizer(coordinate, ":");
		int tokens = st.countTokens();
		if (tokens < 1) {
			throw new IllegalArgumentException("coordinate=" + coordinate);
		}
		try {
			String degrees = st.nextToken();
			double val;
			if (tokens == 1) {
				val = Double.parseDouble(degrees);
				return negative ? -val : val;
			}

			String minutes = st.nextToken();
			int deg = Integer.parseInt(degrees);
			double min;
			double sec = 0.0;

			if (st.hasMoreTokens()) {
				min = Integer.parseInt(minutes);
				String seconds = st.nextToken();
				sec = Double.parseDouble(seconds);
			} else {
				min = Double.parseDouble(minutes);
			}

			boolean isNegative180 = negative && (deg == 180) &&
					(min == 0) && (sec == 0);

			// deg must be in [0, 179] except for the case of -180 degrees
			if ((deg < 0.0) || (deg > 179 && !isNegative180)) {
				throw new IllegalArgumentException("coordinate=" + coordinate);
			}
			if (min < 0 || min > 59) {
				throw new IllegalArgumentException("coordinate=" +
						coordinate);
			}
			if (sec < 0 || sec > 59) {
				throw new IllegalArgumentException("coordinate=" +
						coordinate);
			}

			val = deg*3600.0 + min*60.0 + sec;
			val /= 3600.0;
			return negative ? -val : val;
		} catch (NumberFormatException nfe) {
			throw new IllegalArgumentException("coordinate=" + coordinate);
		}
	}

	/**
	 * Converts a coordinate to a String representation.
	 * @param coordinate
	 * @param outputType
	 * @return
	 * 
	 * @throws IllegalArgumentException if coordinate is less than
	 * -180.0, greater than 180.0, or is not a number.
	 * @throws NullPointerException if outputType is null
	 */
	public static String convert(double coordinate, RawData.FORMAT outputType){

		if (outputType == null) {
			throw new NullPointerException("outputType");
		}

		if (coordinate < -180.0 || coordinate > 180.0 || Double.isNaN(coordinate)) {
			throw new IllegalArgumentException("coordinate=" + coordinate);
		}

		StringBuilder sb = new StringBuilder();

		// Handle negative values
		if (coordinate < 0) {
			sb.append('-');
			coordinate = -coordinate;
		}

		DecimalFormat df = new DecimalFormat("###.#####");
		if (outputType == FORMAT.MINUTES || outputType == FORMAT.SECONDS) {
			int degrees = (int) Math.floor(coordinate);
			sb.append(degrees);
			sb.append(':');
			coordinate -= degrees;
			coordinate *= 60.0;
			if (outputType == FORMAT.SECONDS) {
				int minutes = (int) Math.floor(coordinate);
				sb.append(minutes);
				sb.append(':');
				coordinate -= minutes;
				coordinate *= 60.0;
			}
		}
		sb.append(df.format(coordinate));
		return sb.toString();
	}

	/**
	 * Computes the approximate DISTANCE in meters between two locations, 
	 * and optionally the initial and final bearings of the shortest path between them.
	 * @param startLatitude
	 * @param startLongitude
	 * @param endLatitude
	 * @param endLongitude
	 * @param results
	 */
	public static void distanceBetween(double startLatitude, double startLongitude, double endLatitude, double endLongitude, float[] results){
		//TODO:
	}

	/**
	 * Returns the approximate DISTANCE in meters between this
	 * location and the given location.  Distance is defined using
	 * the WGS84 ellipsoid.
	 *
	 * @param dest the destination location
	 * @return the approximate DISTANCE in meters
	 */
	public double distanceTo(Point dest){

		//		CartesianDistCalc cd = new CartesianDistCalc();
		//		GeodesicSphereDistCalc gdh = new GeodesicSphereDistCalc.Haversine();
		//		GeodesicSphereDistCalc gdlc = new GeodesicSphereDistCalc.LawOfCosines();

		RawData raw = dest.getRawData();
		
		GeodesicSphereDistCalc gdv = new GeodesicSphereDistCalc.Vincenty();
		PointImpl p = new PointImpl(raw.getLongitude(), raw.getLatitude(), SpatialContext.GEO);

		return gdv.distance(p, raw.getLongitude(), raw.getLatitude());
		
	}

	/**
	 * Returns the approximate initial BEARING in degrees East of true
	 * North when traveling along the shortest path between this
	 * location and the given location.  The shortest path is defined
	 * using the WGS84 ellipsoid.  Locations that are (nearly)
	 * antipodal may produce meaningless results.
	 *
	 * @param dest the destination location
	 * @return the initial BEARING in degrees
	 */
	public float bearingTo(Point dest) {

		//		synchronized (mResults) {
		//			// See if we already have the result
		//			if (mLatitude != mLat1 || mLongitude != mLon1 ||
		//					dest.mLatitude != mLat2 || dest.mLongitude != mLon2) {
		//				
		//				
		//				computeDistanceAndBearing(mLatitude, mLongitude,
		//						dest.mLatitude, dest.mLongitude, mResults);
		//				
		//				mLat1 = mLatitude;
		//				mLon1 = mLongitude;
		//				mLat2 = dest.mLatitude;
		//				mLon2 = dest.mLongitude;
		//				mDistance = mResults[0];
		//				mInitialBearing = mResults[1];
		//			
		//			}
		//			return mInitialBearing;

		return 0;

	}
	
	 /**
     * Return the UTC time of this fix, in milliseconds since January 1, 1970.
     *
     * <p>Note that the UTC time on a device is not monotonic: it
     * can jump forwards or backwards unpredictably. So always use
     * {@link #getElapsedRealtimeNanos} when calculating time deltas.
     *
     * <p>On the other hand, {@link #getTime} is useful for presenting
     * a human readable time to the user, or for carefully comparing
     * location fixes across reboot or across devices.
     *
     * <p>All locations generated by the {@link LocationManager}
     * are guaranteed to have a valid UTC time, however remember that
     * the system time may have changed since the location was generated.
     *
     * @return time of fix, in milliseconds since January 1, 1970.
     */
    public long getTime() {
        return mTime;
    }

    /**
     * Set the UTC time of this fix, in milliseconds since January 1,
     * 1970.
     *
     * @param time UTC time of this fix, in milliseconds since January 1, 1970
     */
    public void setTime(long time) {
        mTime = time;
    }

    

    /**
     * Get the latitude, in degrees.
     *
     * <p>All locations generated by the {@link LocationManager}
     * will have a valid latitude.
     */
    public double getLatitude() {
        return mLatitude;
    }

    /**
     * Set the latitude, in degrees.
     */
    public void setLatitude(double latitude) {
        mLatitude = latitude;
    }

    /**
     * Get the longitude, in degrees.
     *
     * <p>All locations generated by the {@link LocationManager}
     * will have a valid longitude.
     */
    public double getLongitude() {
        return mLongitude;
    }

    /**
     * Set the longitude, in degrees.
     */
    public void setLongitude(double longitude) {
        mLongitude = longitude;
    }

    /**
     * True if this location has an altitude.
     */
    public boolean hasAltitude() {
        return mHasAltitude;
    }

    /**
     * Get the altitude if available, in meters above the WGS 84 reference
     * ellipsoid.
     *
     * <p>If this location does not have an altitude then 0.0 is returned.
     */
    public double getAltitude() {
        return mAltitude;
    }

    /**
     * Set the altitude, in meters above the WGS 84 reference ellipsoid.
     *
     * <p>Following this call {@link #hasAltitude} will return true.
     */
    public void setAltitude(double altitude) {
        mAltitude = altitude;
        mHasAltitude = true;
    }

    /**
     * Remove the altitude from this location.
     *
     * <p>Following this call {@link #hasAltitude} will return false,
     * and {@link #getAltitude} will return 0.0.
     */
    public void removeAltitude() {
        mAltitude = 0.0f;
        mHasAltitude = false;
    }

    /**
     * True if this location has a speed.
     */
    public boolean hasSpeed() {
        return mHasSpeed;
    }

    /**
     * Get the speed if it is available, in meters/second over ground.
     *
     * <p>If this location does not have a speed then 0.0 is returned.
     */
    public float getSpeed() {
        return mSpeed;
    }

    /**
     * Set the speed, in meters/second over ground.
     *
     * <p>Following this call {@link #hasSpeed} will return true.
     */
    public void setSpeed(float speed) {
        mSpeed = speed;
        mHasSpeed = true;
    }

    /**
     * Remove the speed from this location.
     *
     * <p>Following this call {@link #hasSpeed} will return false,
     * and {@link #getSpeed} will return 0.0.
     */
    public void removeSpeed() {
        mSpeed = 0.0f;
        mHasSpeed = false;
    }

    /**
     * True if this location has a bearing.
     */
    public boolean hasBearing() {
        return mHasBearing;
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
    public float getBearing() {
        return mBearing;
    }

    /**
     * Set the bearing, in degrees.
     *
     * <p>Bearing is the horizontal direction of travel of this device,
     * and is not related to the device orientation.
     *
     * <p>The input will be wrapped into the range (0.0, 360.0].
     */
    public void setBearing(float bearing) {
        while (bearing < 0.0f) {
            bearing += 360.0f;
        }
        while (bearing >= 360.0f) {
            bearing -= 360.0f;
        }
        mBearing = bearing;
        mHasBearing = true;
    }

    /**
     * Remove the bearing from this location.
     *
     * <p>Following this call {@link #hasBearing} will return false,
     * and {@link #getBearing} will return 0.0.
     */
    public void removeBearing() {
        mBearing = 0.0f;
        mHasBearing = false;
    }

    /**
     * True if this location has an accuracy.
     *
     * <p>All locations generated by the {@link LocationManager} have an
     * accuracy.
     */
    public boolean hasAccuracy() {
        return mHasAccuracy;
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
     * All locations generated by the {@link LocationManager} include
     * an accuracy.
     */
    public float getAccuracy() {
        return mAccuracy;
    }

    /**
     * Set the estimated accuracy of this location, meters.
     *
     * <p>See {@link #getAccuracy} for the definition of accuracy.
     *
     * <p>Following this call {@link #hasAccuracy} will return true.
     */
    public void setAccuracy(float accuracy) {
        mAccuracy = accuracy;
        mHasAccuracy = true;
    }

    /**
     * Remove the accuracy from this location.
     *
     * <p>Following this call {@link #hasAccuracy} will return false, and
     * {@link #getAccuracy} will return 0.0.
     */
    public void removeAccuracy() {
        mAccuracy = 0.0f;
        mHasAccuracy = false;
    }

    /**
     * Return true if this Location object is complete.
     *
     * <p>A location object is currently considered complete if it has
     * a valid provider, accuracy, wall-clock time and elapsed real-time.
     *
     * <p>All locations supplied by the {@link LocationManager} to
     * applications must be complete.
     *
     * @see #makeComplete
     * @hide
     */
    public boolean isComplete() {
        if (!mHasAccuracy) return false;
        if (mTime == 0) return false;
        return true;
    }

    /**
     * Helper to fill incomplete fields.
     *
     * <p>Used to assist in backwards compatibility with
     * Location objects received from applications.
     *
     * @see #isComplete
     * @hide
     */
    public void makeComplete() {
        if (!mHasAccuracy) {
            mHasAccuracy = true;
            mAccuracy = 100.0f;
        }
        if (mTime == 0) mTime = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("Location[");
        s.append(String.format(" %.6f,%.6f", mLatitude, mLongitude));
        if (mHasAccuracy) s.append(String.format(" acc=%.0f", mAccuracy));
        else s.append(" acc=???");
        if (mTime == 0) {
            s.append(" t=?!?");
        }
        if (mHasAltitude) s.append(" alt=").append(mAltitude);
        if (mHasSpeed) s.append(" vel=").append(mSpeed);
        if (mHasBearing) s.append(" bear=").append(mBearing);
        s.append(']');
        return s.toString();
    }

	
	

//	public Instant getTimeInstant(){
//
//		//TODO: Rever default
//		String instant = (String) mNode.getProperty(Neo4LocationProperties.INSTANT, "");
//		Instant instantParsed = Instant.parse(instant);
//
//		return instantParsed;
//	}
}
