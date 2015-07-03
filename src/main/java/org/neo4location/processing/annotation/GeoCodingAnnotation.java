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

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;

//https://github.com/googlemaps/google-maps-services-java#api-keys
//https://github.com/googlemaps/google-maps-services-java#asynchronous-or-synchronous----you-choose
//https://github.com/googlemaps/google-maps-services-java/tree/master/src/test/java/com/google/maps
public class GeoCodingAnnotation implements Annotation {

  private GeoApiContext mContext;


  public GeoCodingAnnotation(){
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
  private Trajectory geoCodingAnnotation(Trajectory trajectory){
    
    //Collection<Move> moves = new ArrayList<>();

    Iterable<Move> moves = trajectory.getMoves();
    GeocodingResult[] results = new GeocodingResult[1];

    try {

      for(Move move : moves){

        Point pFrom = move.getFrom();

        RawData rdFrom = pFrom.getRawData();
        Map<String, Object> sdFrom = pFrom.getSemanticData();

        if(rdFrom == null && sdFrom == null)
          continue;


        if(rdFrom == null && sdFrom != null){ 
          rdFrom = new RawData(Double.MIN_VALUE, Double.MIN_VALUE, Long.MIN_VALUE, null, null, null, null);
        } 

        if(rdFrom.getLatitude() < -90 && rdFrom.getLatitude() > 90
            &&  rdFrom.getLongitude() < -180  && rdFrom.getLongitude() > 180
            && sdFrom.containsKey(Neo4LocationProperties.ADDRESS)){

          String address = (String) sdFrom.get(Neo4LocationProperties.ADDRESS);
          results = geocode(address);
          
          LatLng latLng = results[0].geometry.location;
          
          rdFrom.setLatitude(latLng.lat);
          rdFrom.setLongitude(latLng.lng);
          
          continue;
        }


        if(sdFrom == null && rdFrom != null){
          sdFrom = new HashMap<String, Object>();
        }

        //Se nao contem chave address
        if((!sdFrom.containsKey(Neo4LocationProperties.ADDRESS)) && rdFrom != null){
          
          double lat = rdFrom.getLatitude();
          double lng = rdFrom.getLongitude();
          
          LatLng location = new LatLng(lat, lng);
          
          results = reverseGeocode(location);
          String address = results[0].formattedAddress;
          sdFrom.put(Neo4LocationProperties.ADDRESS, address);
        }
      
      }

    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }


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