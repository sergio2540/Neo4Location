package org.neo4location.trajectory.strucuture;

import java.time.Duration;

import org.neo4location.domain.trajectory.Trajectory;


public class DensityBasedStructure {
	
	private double mMaxDist;
	private Trajectory mTrajectory;
	private Duration mDuration;

	public DensityBasedStructure(double maxDist, Duration duration){
		
		//this.mTrajectory = trajectory;
		this.mMaxDist = maxDist;
		this.mDuration = duration;
	
	}
	
	public void process(){
		
	}
	
	//1. ∥tib −tia∥ ≤ τ e ∥⟨xia, yia⟩ − ⟨xib, yib⟩∥ ≤ σ
	
	//Ultimo elemento da trajetoria dista de mais de σ e menos τ => ∥ta − t∥ ≤ τ and ∥⟨xa, ya⟩ − ⟨x, y⟩∥ > σ


}