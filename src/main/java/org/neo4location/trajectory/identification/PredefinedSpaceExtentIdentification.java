package org.neo4location.trajectory.identification;

import org.neo4location.domain.trajectory.Trajectory;
import org.neo4location.trajectory.Identification;

public class PredefinedSpaceExtentIdentification implements Identification {
	
	/*
	 * Divide the stream of GPS feed into several subsequences according to a spatial criteria, 
	 * e.g., fixed DISTANCE, geo-fenced regions, movement between predefined points for network 
	 * constrained trajectories.
	 */
	
	private Trajectory trajectory;
	private int spaceExtent;

	public PredefinedSpaceExtentIdentification(Trajectory trajectory, int spaceExtent){

		this.trajectory = trajectory;
		this.spaceExtent = spaceExtent;

	}

	@Override
	public void process() {
		// TODO Auto-generated method stub
		
	}
	
}
