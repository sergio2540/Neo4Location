package org.neo4location.processing.identification;

import java.util.Collection;

import org.neo4location.domain.trajectory.Trajectory;
import org.neo4location.processing.Identification;

public class PredefinedSpaceExtentIdentification implements Identification {
	
	/*
	 * Divide the stream of GPS feed into several subsequences according to a spatial criteria, 
	 * e.g., fixed DISTANCE, geo-fenced regions, movement between predefined points for network 
	 * constrained trajectories.
	 */
	
	//String wkt
	//Alterar spaceExtent para wkt em free space
	//Em network: dados dois pontos a e b devolve
	
	private double spaceExtent;
	private Collection<Trajectory> mTrajectories;

	public PredefinedSpaceExtentIdentification(double spaceExtent){

		this.spaceExtent = spaceExtent;

	}
	
	private void predefinedSpaceExtentIdentification(Trajectory trajectory) {
		// TODO Auto-generated method stub
		
	}
	
	public void setTrajectories(Collection<Trajectory> trajectories){
		
		mTrajectories = trajectories;
	
	}
	
	public Collection<Trajectory> getTrajectory(){
		
		return mTrajectories;
		
	}

	@Override
	public Void call() throws Exception {
		
		for(Trajectory trajectory : mTrajectories){
			predefinedSpaceExtentIdentification(trajectory);
		}
		
		return null;
	}
	
}
