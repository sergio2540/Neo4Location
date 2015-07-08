package org.neo4location.processing;

import java.util.Collection;

import org.neo4location.domain.trajectory.Trajectory;

public interface ProcessHandler {
  
  public int getPriority();
  
  public String getName();

  public Collection<Trajectory> process(Collection<Trajectory> trajectories);

}
