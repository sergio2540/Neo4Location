package org.neo4location.processing.identification;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4location.domain.trajectory.Move;
import org.neo4location.domain.trajectory.Point;
import org.neo4location.domain.trajectory.RawData;
import org.neo4location.domain.trajectory.Trajectory;
import org.neo4location.processing.Identification;
import org.neo4location.utils.Neo4LocationProcessingUtils;

public class PredefinedTimeIntervalIdentification implements Identification {

	/*
	 * Divide the stream of GPS feed into several subsequences contained in given time intervals,
	 *  e.g., hourly trajectory, daily trajectory, weekly trajectory, monthly trajectory.
	 */
	
	
	private long mMinStopTime;


	public PredefinedTimeIntervalIdentification(long minStopTime) {

		
		mMinStopTime = minStopTime;

	}

	public Collection<Trajectory> predefinedTimeIntervalIdentification(Trajectory trajectory) {

		Iterable<Move> moves = trajectory.getMoves();
		if(moves == null)
			return Collections.emptyList();

		List<Move> tempMoves = new ArrayList<Move>();

		//Nao ha garantia de ordem no tempo
		//Devo fazer sort de moves

		Duration sumDuration = Duration.ZERO;
		
		Collection<Trajectory> tempTrajectories = new ArrayList<Trajectory>();
		
		for(Move m: moves){

			Duration duration;

			Point pFrom = m.getFrom();

			if(pFrom == null){
				return Collections.emptyList();
			}

			Point pTo = m.getTo();

			if(pTo == null){
				return Collections.emptyList();
			}

			RawData rFrom = pFrom.getRawData();
			RawData rTo = pFrom.getRawData();


			duration = Neo4LocationProcessingUtils.interval(m, rFrom, rTo);
			sumDuration.plus(duration);


			if (sumDuration.equals(Duration.ofMillis(mMinStopTime))){
				//end of trajectory, create temp trajectory 
				//and prepare for a new trajectory
				
				
				String newTrajectoryName = String.format("%s-%s", getName(), trajectory.getTrajectoryName());
				Trajectory tempTrajectory = new Trajectory(newTrajectoryName, trajectory.getUser(), tempMoves, trajectory.getSemanticData());
				
		
				tempTrajectories.add(tempTrajectory);

			
				
				tempMoves = new ArrayList<Move>();
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
				    	   .map((trajectory) -> predefinedTimeIntervalIdentification(trajectory))
						   .flatMap((col) -> col.stream())
						   .collect(Collectors.toList());
		
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

}