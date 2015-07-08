package org.neo4location.processing.annotation;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.joda.time.Hours;
import org.neo4location.domain.Neo4LocationProperties;
import org.neo4location.domain.trajectory.Move;
import org.neo4location.domain.trajectory.Point;
import org.neo4location.domain.trajectory.RawData;
import org.neo4location.domain.trajectory.Trajectory;
import org.neo4location.processing.Annotation;

import se.walkercrou.places.GooglePlaces;
import se.walkercrou.places.Photo;
import se.walkercrou.places.Place;
import se.walkercrou.places.Price;
import se.walkercrou.places.Review;
import se.walkercrou.places.Scope;
import se.walkercrou.places.Status;

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
public class PlacesAnnotation implements Annotation {

  private GooglePlaces mClient;

  private final static int PRIORITY = 1; 
  
  public PlacesAnnotation(){


    InputStream stream = this.getClass().getResourceAsStream("GOOGLE_API.key");
    System.out.println(stream != null);

    BufferedReader br = new BufferedReader(new InputStreamReader(stream));

    try {

      String API_KEY = br.readLine();

      mClient = new GooglePlaces(API_KEY);

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
        Map<String, Object> sdFrom = pFrom.getSemanticData();

        if(rdFrom == null){ 
          continue;
        } 

        LatLng location = null;

        double lat = rdFrom.getLatitude();
        double lng = rdFrom.getLongitude();
        //double radius = 20;
        List<Place> places = mClient.getNearbyPlaces(lat, lng, GooglePlaces.MAXIMUM_RESULTS);

        for(Place p : places){
          toPlace(p);
        }

        annotatedTrajectory = new Trajectory(trajectory.getTrajectoryName(), trajectory.getUser(), moves, trajectory.getSemanticData());

      }

    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return annotatedTrajectory;
  }


  public Map<String,Object> toPlace(Place p){

    Map<String, Object> map = new HashMap<String, Object>();

    String name = p.getName();
    if(name != null)
      map.put(Neo4LocationProperties.PLACE_NAME, name);

    int accuracy = p.getAccuracy();

    String phoneNumber = p.getPhoneNumber();
    if(phoneNumber != null)
      map.put(Neo4LocationProperties.PLACE_PHONE_NUMBER, phoneNumber);

    String internationalPhoneNumber = p.getInternationalPhoneNumber();

    String language = p.getLanguage();
    if(language != null)
      map.put(Neo4LocationProperties.LANGUAGE, language);

    //TODO: Verificar property
    se.walkercrou.places.Hours hours = p.getHours();
    if(hours != null)
      map.put(Neo4LocationProperties.TIMETABLE, hours);

    double rating = p.getRating();
    if(rating == -1)
      map.put(Neo4LocationProperties.PLACE_RATING, rating);

    Price price = p.getPrice();
    if(price != null)
      map.put(Neo4LocationProperties.PLACE_PRICE, price);

    String vicinity = p.getVicinity();
    if(vicinity != null)
      map.put(Neo4LocationProperties.PLACE_VICINITY, vicinity);

    List<Review> reviews = p.getReviews();
    if(reviews != null){

      for(Review r : reviews){

        String author = r.getAuthor();
        String lang = r.getLanguage();

        int rat = r.getRating();

        String text = r.getText();
        long time = r.getTime();

      }

    }

    Scope scope = p.getScope();
    if(scope != null)
      map.put(Neo4LocationProperties.PLACE_VICINITY, vicinity);

    List<Photo> photos = p.getPhotos();
    if(photos != null){

      for(Photo photo : photos){
        int h = photo.getHeight();
        int w = photo.getWidth();
        BufferedImage bi = photo.getImage();
      }
    }

    Status status = p.getStatus();

    if(status != null)
      map.put(Neo4LocationProperties.PLACE_STATUS, status.name());

    List<String> types = p.getTypes();
    if(types != null){
      for(String t : types){
        StringBuilder array = new StringBuilder();
        //map.put(Neo4LocationProperties.PLACE_TYPE, t);
      }
    }

    String website = p.getWebsite();
    if(website != null)
      map.put(Neo4LocationProperties.WEBSITE, website);

    return map;
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
  
  @Override
  public int getPriority() {
   
    return PRIORITY;
  }

}