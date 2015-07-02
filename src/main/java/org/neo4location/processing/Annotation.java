package org.neo4location.processing;

import java.util.Collection;

import org.neo4location.domain.trajectory.Trajectory;

//https://code.google.com/p/geocoder-java/

public interface Annotation {

//	public void fill_missing_props(String ... properties){
//	}

	public String getName();

  public Collection<Trajectory> process(Collection<Trajectory> trajectories);
  
	
}
