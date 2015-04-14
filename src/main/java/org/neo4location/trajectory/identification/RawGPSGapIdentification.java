package org.neo4location.trajectory.identification;


import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.neo4location.domain.trajectory.Point;
import org.neo4location.domain.trajectory.Trajectory;
import org.neo4location.trajectory.Identification;

public class RawGPSGapIdentification implements Identification {

	private Duration mDuration;
	private double mDistance;
	private Trajectory mTrajectory;


	public RawGPSGapIdentification(Trajectory trajectory, Duration duration, double distance){

		this.mTrajectory = trajectory;	
		this.mDuration = duration;
		this.mDistance = distance;
		

	}

	public void process(){


		Iterator<Point> rawPoints = mTrajectory.getPoints().iterator();
		List<Point> tempTrajectory = new ArrayList<Point>();
		
		Point first = rawPoints.hasNext() ? rawPoints.next() : null;
		
		while(rawPoints.hasNext()){
			
			//RawPoint first = rawPoints.next();
			//RawPoint second = rawPoints.hasNext() ? rawPoints.next() : null;
			Point second = rawPoints.next();
			
			//if second is null, first is the last raw point.
//			if(second == null)	
//				break;
			
			Instant instantFirst = Instant.ofEpochMilli(first.getRawData().getTime());
			Instant instantSecond = Instant.ofEpochMilli(second.getRawData().getTime());
			
			Duration duration = Duration.between(instantFirst, instantSecond);
			
			if (duration.compareTo(mDuration) == 1){
				
				if(mDistance > 0){
					//One more verification
					//Point devia ter distanceTo?????
					double distance = first.getRawData().distanceTo(second);
					if(distance < mDistance){
						continue;
					}
				}
				
				//SUCESS
				//end of trajectory, create temp trajectory 
				//and prepare for a new trajectory
				createSemanticTrajectory(tempTrajectory);
				tempTrajectory = new ArrayList<Point>();
			}
			
			tempTrajectory.add(first);
			
			first = second;
			
		}





	}

	private void createSemanticTrajectory(List<Point> tempTrajectory2) {
		// TODO Auto-generated method stub
		
	}

}