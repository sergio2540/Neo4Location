package org.neo4location.processing.identification;


import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.event.TransactionData;
import org.neo4location.domain.Neo4LocationProperties;
import org.neo4location.domain.Neo4LocationRelationships;
import org.neo4location.domain.trajectory.Move;
import org.neo4location.domain.trajectory.Point;
import org.neo4location.domain.trajectory.RawData;
import org.neo4location.domain.trajectory.Trajectory;
import org.neo4location.processing.Identification;
import org.neo4location.utils.Neo4LocationProcessingUtils;

public class RawGPSGapIdentification implements Identification {

	
	private Duration mDuration;
	private double mDistance;


	public RawGPSGapIdentification(double distance, long duration){
		
		mDuration = Duration.ofMillis(duration);
		mDistance = distance;

	}

	public Collection<Trajectory> rawGPSGapIdentification(Trajectory trajectory){

		//TODO:

		Iterable<Move> moves = trajectory.getMoves();
		if(moves == null)
			return Collections.emptyList();

		List<Move> tempMoves = new ArrayList<Move>();
		Collection<Trajectory> tempTrajectories = new ArrayList<Trajectory>();
		
		
		//Nao ha garantia de ordem no tempo
		//Devo fazer sort de moves

		for(Move m: moves){

			Duration duration;
			double distance;

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


			distance = Neo4LocationProcessingUtils.distance(m,rFrom, rTo);
			duration = Neo4LocationProcessingUtils.interval(m, rFrom, rTo);


			//duration > mDuration
			if (duration.compareTo(mDuration) == 1){

				if(mDistance > 0 && distance < mDistance){
					continue;
				}

				//SUCESS
				//end of trajectory, create temp trajectory 
				//and prepare for a new trajectory

				String newTrajectoryName = String.format("%s-%s", getName(), trajectory.getTrajectoryName());
				Trajectory tempTrajectory = new Trajectory(newTrajectoryName, trajectory.getUser(), tempMoves, trajectory.getSemanticData());
				
				
				tempTrajectories.add(tempTrajectory);

				tempMoves = new ArrayList<Move>();
			}

			tempMoves.add(m);

			//first = second;

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
				    	   .map((trajectory) -> rawGPSGapIdentification(trajectory))
						   .flatMap((col) -> col.stream())
						   .collect(Collectors.toList());
		
	}


	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

}