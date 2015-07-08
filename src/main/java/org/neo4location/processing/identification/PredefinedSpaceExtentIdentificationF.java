package org.neo4location.processing.identification;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import org.neo4location.domain.trajectory.Trajectory;
import org.neo4location.processing.Identification;

public class PredefinedSpaceExtentIdentificationF implements Identification {

  /*
   * Divide the stream of GPS feed into several subsequences according to a spatial criteria, 
   * e.g., fixed DISTANCE, geo-fenced regions, movement between predefined points for network 
   * constrained trajectories.
   */

  //String wkt
  //Alterar spaceExtent para wkt em free space
  //Em network: dados dois pontos a e b devolve


  private double mSpaceExtent;

  private final static int PRIORITY = 1; 

  public PredefinedSpaceExtentIdentificationF(double spaceExtent){

    mSpaceExtent = spaceExtent;

  }

  private Collection<Trajectory> predefinedSpaceExtentIdentification(Trajectory trajectory) {
    return null;
    // TODO Auto-generated method stub

  }

  @Override
  public Collection<Trajectory> process(Collection<Trajectory> trajectories) {


    if(trajectories == null){
      //Throw exception with text you must call setTrajectories(Collection<Trajectory> trajectories)
      return Collections.emptyList();

    }


    return trajectories.stream()
        .map((trajectory) -> predefinedSpaceExtentIdentification(trajectory))
        .flatMap((col) -> col.stream())
        .collect(Collectors.toList());

  }

  @Override
  public String getName() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int getPriority() {
   
    return PRIORITY;
  }

}