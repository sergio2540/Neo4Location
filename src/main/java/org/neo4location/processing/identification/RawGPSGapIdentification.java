package org.neo4location.processing.identification;


import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.neo4location.domain.Neo4LocationProperties;
import org.neo4location.domain.trajectory.Move;
import org.neo4location.domain.trajectory.Point;
import org.neo4location.domain.trajectory.RawData;
import org.neo4location.domain.trajectory.Trajectory;
import org.neo4location.processing.Identification;
import org.neo4location.utils.Neo4LocationProcessingUtils;

//TODO: Deve dar tb para quando só tem tempo!!!!
public class RawGPSGapIdentification implements Identification {

  private Duration mDuration;
  private Double mDistance;

  private final static int PRIORITY = 1; 

  public RawGPSGapIdentification(long duration){

    mDuration = Duration.ofMillis(duration);
   

  }

  public RawGPSGapIdentification(long duration, double distance){

    mDuration = Duration.ofMillis(duration);
    mDistance = distance;

  }

  private Collection<Trajectory> rawGPSGapIdentification(Trajectory trajectory){


    //TODO:
    Iterable<Move> moves = trajectory.getMoves();
    if(moves == null)
      return Collections.emptyList();

    List<Move> tempMoves = new ArrayList<Move>();
    Collection<Trajectory> tempTrajectories = new ArrayList<Trajectory>();


    //Nao ha garantia de ordem no tempo
    //Devo fazer sort de moves

    for(Move m: moves){

      Duration duration;
      double distance;

      Point pFrom = m.getFrom();

      if(pFrom == null){
        return Collections.emptyList();
      }

      Point pTo = m.getTo();

      if(pTo == null){
        return Collections.emptyList();
      }

      RawData rFrom = pFrom.getRawData();
      RawData rTo = pTo.getRawData();

      duration = Neo4LocationProcessingUtils.interval(m);
      distance = Neo4LocationProcessingUtils.distance(m);


      //duration > mDuration
      if (duration.compareTo(mDuration) == 1 && mDistance > 0 && distance < mDistance){

        //SUCESS
        //end of trajectory, create temp trajectory 
        //and prepare for a new trajectory
        
//        long startTrajectoryTime = (long) trajectory.getSemanticData().get(Neo4LocationProperties.START_INSTANT);
//        String newTrajectoryName = String.format("%s-%s-%d", getName(), trajectory.getTrajectoryName(), startTrajectoryTime);
       
        String newTrajectoryName = String.format("%s-%s", getName(), trajectory.getTrajectoryName());
       
        Trajectory tempTrajectory = new Trajectory(newTrajectoryName, trajectory.getUser(), tempMoves, trajectory.getSemanticData());


        tempTrajectories.add(tempTrajectory);

        tempMoves = new ArrayList<Move>();

        //TODO:
        //DEVO METER CONTINUE????????

      }

      tempMoves.add(m);



    }

    return tempTrajectories;

  }





  @Override
  public Collection<Trajectory> process(Collection<Trajectory> trajectories) {


    if(trajectories == null){
      //Throw exception with text you must call setTrajectories(Collection<Trajectory> trajectories)
      return Collections.emptyList();

    }


    return trajectories.stream()
        .map((trajectory) -> rawGPSGapIdentification(trajectory))
        .flatMap((col) -> col.stream())
        .collect(Collectors.toList());

  }

  @Override
  public String getName() {

    //Falta UID

    return this.getClass().getSimpleName();
  }

  @Override
  public int getPriority() {
   
    return PRIORITY;
  }
  
}