package org.neo4location.processing.identification;


import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
	private Collection<Trajectory> mTrajectories;


	public RawGPSGapIdentification(double distance, long duration){

		this.mDuration = Duration.ofMillis(duration);
		this.mDistance = distance;

	}

	public void setTrajectories(Collection<Trajectory> trajectories){

		mTrajectories = trajectories;

	}

	public Collection<Trajectory> getTrajectory(){

		return mTrajectories;

	}

	public void rawGPSGapIdentification(Trajectory trajectory){

		//TODO:

		Collection<Move> moves = trajectory.getMoves();
		if(moves == null)
			return;

		List<Point> tempTrajectory = new ArrayList<Point>();

		//Nao ha garantia de ordem no tempo
		//Devo fazer sort de moves

		for(Move m: moves){

			Duration duration;
			double distance;

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
			rawGPSGapIdentification(trajectory);
		}

		//SAVE OR SEND

		return null;
	}

}