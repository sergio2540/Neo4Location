package org.neo4location.processing.strucuture;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4location.domain.trajectory.Trajectory;
import org.neo4location.processing.Structure;


public class DensityBasedStructureF implements Structure {

	//private GraphDatabaseService mGraphDatabaseService;
	private double mMaxDist;
	private long mMinStopTime;

	

	public DensityBasedStructureF(double maxDist, long minStopTime){

		//mGraphDatabaseService = graphDatabaseService;
		mMaxDist = maxDist;
		mMinStopTime = minStopTime;


	}

	public Collection<Trajectory> densityBased(Trajectory trajectory){
		
		return new ArrayList<Trajectory>();

	}


	@Override
	public Collection<Trajectory> process(Collection<Trajectory> trajectories) {


		if(trajectories == null){
			//Throw exception with text you must call setTrajectories(Collection<Trajectory> trajectories)
			return Collections.emptyList();

		}
		
		
		return trajectories.stream()
				    	   .map((trajectory) -> densityBased(trajectory))
						   .flatMap((col) -> col.stream())
						   .collect(Collectors.toList());
		
	}

	@Override
	public String getName() {
		
		return this.getClass().getSimpleName();

	}
	

	//1. ∥tib −tia∥ ≤ τ e ∥⟨xia, yia⟩ − ⟨xib, yib⟩∥ ≤ σ

	//Ultimo elemento da trajetoria dista de mais de σ e menos τ => ∥ta − t∥ ≤ τ and ∥⟨xa, ya⟩ − ⟨x, y⟩∥ > σ
}