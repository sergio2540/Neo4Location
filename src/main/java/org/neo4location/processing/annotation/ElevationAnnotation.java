package org.neo4location.processing.annotation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
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

import com.google.maps.DistanceMatrixApi;
import com.google.maps.ElevationApi;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.RoadsApi;
import com.google.maps.model.ElevationResult;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;

//https://github.com/googlemaps/google-maps-services-java#api-keys
//https://github.com/googlemaps/google-maps-services-java#asynchronous-or-synchronous----you-choose
//https://github.com/googlemaps/google-maps-services-java/tree/master/src/test/java/com/google/maps
public class ElevationAnnotation implements Annotation {

  private GeoApiContext mContext;


  public ElevationAnnotation(){
    //TODO: Set API KEY


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
  private Trajectory elevationAnnotation(Trajectory trajectory){


    Trajectory annotatedTrajectory = null;
    Iterable<Move> moves = trajectory.getMoves();

    try {

      for(Move m : moves){

        Point pFrom = m.getFrom();

        RawData rdFrom = pFrom.getRawData();



        if(rdFrom == null){ 
          continue;
        } 
        
        double lat = rdFrom.getLatitude();
        double lng = rdFrom.getLongitude();
        LatLng location = new LatLng(lat, lng);

        Double alt = rdFrom.getAltitude();

        if(alt == null){
          ElevationResult el = ElevationApi.getByPoint(mContext, location).await();
          double newAlt = el.elevation;
          rdFrom.setAltitude(newAlt);
        }

        //RoadsApi.snapToRoads(context, path)

        annotatedTrajectory = new Trajectory(trajectory.getTrajectoryName(), trajectory.getUser(), moves, trajectory.getSemanticData());



      }

    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return annotatedTrajectory;


  }

  @Override
  public Collection<Trajectory> process(Collection<Trajectory> trajectories) {

    if(trajectories == null){
      //Throw exception with text you must call setTrajectories(Collection<Trajectory> trajectories)
      return Collections.emptyList();

    }

    return trajectories.stream()
        .map((trajectory) -> elevationAnnotation(trajectory))
        .collect(Collectors.toList());

  }

}