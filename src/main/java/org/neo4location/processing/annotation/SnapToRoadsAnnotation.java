package org.neo4location.processing.annotation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.neo4location.domain.Neo4LocationProperties;
import org.neo4location.domain.trajectory.Move;
import org.neo4location.domain.trajectory.Point;
import org.neo4location.domain.trajectory.RawData;
import org.neo4location.domain.trajectory.Trajectory;
import org.neo4location.processing.Annotation;

import com.google.maps.DirectionsApi;
import com.google.maps.DistanceMatrixApi;
import com.google.maps.ElevationApi;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.RoadsApi;
import com.google.maps.model.ElevationResult;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import com.google.maps.model.SnappedPoint;

//https://github.com/googlemaps/google-maps-services-java#api-keys
//https://github.com/googlemaps/google-maps-services-java#asynchronous-or-synchronous----you-choose
//https://github.com/googlemaps/google-maps-services-java/tree/master/src/test/java/com/google/maps
public class SnapToRoadsAnnotation implements Annotation {

  private GeoApiContext mContext;
  private boolean mInterpolate;


  public SnapToRoadsAnnotation(boolean interpolate){
    
    mInterpolate = interpolate;
    
    InputStream stream = this.getClass().getResourceAsStream("GOOGLE_API.key");
    System.out.println(stream != null);

    //    stream = this.getClass().getClassLoader().getResourceAsStream("/GOOGLE_API.key");
    //    System.out.println(stream != null);

    BufferedReader br = new BufferedReader(new InputStreamReader(stream));
    mContext = null;
    try {

      String API_KEY = br.readLine();
      mContext = new GeoApiContext().setApiKey(API_KEY);

    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }




  }

  @Override
  public String getName() {
    return this.getClass().getSimpleName();
  }

  //Obter a elevação
  private Trajectory geoCodingAnnotation(Trajectory trajectory){



    Iterable<Move> moves = trajectory.getMoves();


    try {

      Collection<LatLng> points = new ArrayList<>();
      for(Move move : moves){

        Point pFrom = move.getFrom();
        RawData rdFrom = pFrom.getRawData();
     
        if(rdFrom == null)
          continue;
        
        double lat = rdFrom.getLatitude();
        double lng = rdFrom.getLongitude();
        LatLng location = new LatLng(lat, lng);
        points.add(location);  

      }
      
      SnappedPoint[] str = RoadsApi.snapToRoads(mContext, mInterpolate, points.toArray(new LatLng[points.size()])).await();
      
      for(SnappedPoint p : str){
        
        double lat = p.location.lat;
        double lng = p.location.lng;
        
        //TODO: Save in Database
      
      }

    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }


    Trajectory annotatedTrajectory = new Trajectory(trajectory.getTrajectoryName(), trajectory.getUser(), moves, trajectory.getSemanticData());

    return annotatedTrajectory;

  }

  

  @Override
  public Collection<Trajectory> process(Collection<Trajectory> trajectories) {

    if(trajectories == null){
      //Throw exception with text you must call setTrajectories(Collection<Trajectory> trajectories)
      return Collections.emptyList();

    }

    return trajectories.stream()
        .map((trajectory) -> geoCodingAnnotation(trajectory))
        .collect(Collectors.toList());

  }

}