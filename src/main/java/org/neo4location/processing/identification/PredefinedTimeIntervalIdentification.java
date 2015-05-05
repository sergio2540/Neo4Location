package org.neo4location.processing.identification;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

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

	private Collection<Trajectory> mTrajectories;
	private Duration mDuration;
	

	public PredefinedTimeIntervalIdentification(long minStopTime) {
		
		mDuration = Duration.ofMillis(minStopTime);
	
	}
	
	public void setTrajectories(Collection<Trajectory> trajectories){
		
		mTrajectories = trajectories;
	
	}
	
	public Collection<Trajectory> getTrajectory(){
		
		return mTrajectories;
		
	}


	public void predefinedTimeIntervalIdentification(Trajectory trajectory) {
		
		Collection<Move> moves = trajectory.getMoves();
		if(moves == null)
			return;

		List<Point> tempTrajectory = new ArrayList<Point>();

		//Nao ha garantia de ordem no tempo
		//Devo fazer sort de moves
		
		Duration sumDuration = Duration.ZERO;

		for(Move m: moves){

			Duration duration;

			Point pFrom = m.getFrom();

			if(pFrom == null){
				return;
			}

			Point pTo = m.getTo();

			if(pTo == null){
				return;
			}

			RawData rFrom = pFrom.getRawData();
			RawData rTo = pFrom.getRawData();
			
			
			duration = Neo4LocationProcessingUtils.interval(m, rFrom, rTo);
			sumDuration.plus(duration);

			
			if (sumDuration.equals(duration)){
				//end of trajectory, create temp trajectory 
				//and prepare for a new trajectory
				createSemanticTrajectory(tempTrajectory);
				tempTrajectory = new ArrayList<Point>();
			}

			tempTrajectory.add(pFrom);

			//first = second;

		}
		
	}	
	

	private void createSemanticTrajectory(List<Point> tempTrajectory2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Void call() throws Exception {

		for(Trajectory trajectory : mTrajectories){
			predefinedTimeIntervalIdentification(trajectory);
		}
		
		//SAVE OR SEND
		return null;
	}

}