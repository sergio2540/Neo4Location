package org.neo4location.processing.strucuture;

import java.time.Duration;
import java.util.Collection;

import org.neo4location.domain.trajectory.Trajectory;
import org.neo4location.processing.Structure;


public class DensityBasedStructure implements Structure {

	private double mMaxDist;
	private Duration mDuration;

	private Collection<Trajectory> mTrajectories;

	public DensityBasedStructure(double maxDist, long minStopTime){

		//this.mTrajectory = trajectory;
		this.mMaxDist = maxDist;
		this.mDuration = Duration.ofMillis(minStopTime);


	}

	public void densityBased(Trajectory trajectory){

	}

	public void setTrajectories(Collection<Trajectory> trajectories){
			mTrajectories = trajectories;
	}

	public Collection<Trajectory> getTrajectories(){
		
		return mTrajectories;
	
	}


	@Override
	public Void call() throws Exception {

		if(mTrajectories == null){
			//Throw exception with text you must call setTrajectories(Collection<Trajectory> trajectories)
			return null;

		}

		for(Trajectory trajectory : mTrajectories){
			densityBased(trajectory);
		}

		return null;
	}
	
	
	

	//1. ∥tib −tia∥ ≤ τ e ∥⟨xia, yia⟩ − ⟨xib, yib⟩∥ ≤ σ

	//Ultimo elemento da trajetoria dista de mais de σ e menos τ => ∥ta − t∥ ≤ τ and ∥⟨xa, ya⟩ − ⟨x, y⟩∥ > σ


}