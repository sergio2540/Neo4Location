package org.neo4location.processing.annotation;

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

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;

//https://github.com/googlemaps/google-maps-services-java#api-keys
//https://github.com/googlemaps/google-maps-services-java#asynchronous-or-synchronous----you-choose
//https://github.com/googlemaps/google-maps-services-java/tree/master/src/test/java/com/google/maps
public class GeoCodingAnnotation implements Annotation {

  private final GeoApiContext mContext;


  public GeoCodingAnnotation(){

    mContext = new GeoApiContext().setApiKey("");

  }

  @Override
  public String getName() {
    return this.getClass().getSimpleName();
  }

  //Obter a elevação
  private Trajectory geoCodingAnnotation(Trajectory trajectory){


    //Collection<Move> moves = new ArrayList<>();

    Iterable<Move> moves = trajectory.getMoves();

    GeocodingResult[] results = new GeocodingResult[1];
    String semanticLocation = "";
    LatLng location = new LatLng(10, 11);

    try {

      for(Move move : moves){

        Point pFrom = move.getFrom();

        RawData rdFrom = pFrom.getRawData();
        Map<String, Object> sdFrom = pFrom.getSemanticData();

        if(rdFrom == null && sdFrom == null)
          continue;


        if(rdFrom == null && sdFrom != null){ 
          rdFrom = new RawData(-1.0, -1.0, -1L,null,null,null,null);
        } 

        if(rdFrom.getLatitude() >= -90 && rdFrom.getLatitude() <= 90
            &&  rdFrom.getLongitude() >= -180  && rdFrom.getLongitude() <= 180
            && sdFrom.containsKey(Neo4LocationProperties.ADDRESS)){

          String address = (String) sdFrom.get(Neo4LocationProperties.ADDRESS);
          results = geocode(address);
          LatLng latLng = results[0].geometry.location;
          
          //rdFrom = new RawData(latitude, longitude, time, altitude, accuracy, speed, bearing)
          continue;
        }


        if(sdFrom == null && rdFrom != null){
          sdFrom = new HashMap<String, Object>();
        }

        //Se nao contem chave address
        if((!sdFrom.containsKey(Neo4LocationProperties.ADDRESS)) && rdFrom != null){
          results = geocode(semanticLocation);
        }



      }
      results = geocode(semanticLocation); 
      results = reverseGeocode(location);

    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    System.out.println(results[0].formattedAddress);


    Trajectory annotatedTrajectory = new Trajectory(trajectory.getTrajectoryName(), trajectory.getUser(), moves, trajectory.getSemanticData());

    return annotatedTrajectory;
  }

  private  GeocodingResult[] geocode(String location) throws Exception{

    //Dada localizacao simbolica retorna lat,lon
    return GeocodingApi.geocode(mContext, location).await();

  }

  private  GeocodingResult[] reverseGeocode(LatLng location) throws Exception{

    //Dada localizacao fisica retorna localizacao simbolica
    return GeocodingApi.reverseGeocode(mContext, location).await();

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
