package org.neo4location.processing;

import java.util.Collection;
import java.util.concurrent.Callable;

import org.neo4location.domain.trajectory.Trajectory;

public interface Structure {

	public String getName();
	
	public Collection<Trajectory> process(Collection<Trajectory> trajectories);
	
//	public void setTrajectories(Collection<Trajectory> trajectories);
//
//	public Collection<Trajectory> getTrajectories();
//	
//	public void createSemanticTrajectory(Trajectory trajectory);

}
