package org.neo4location.trajectory.identification;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.neo4location.domain.trajectory.Point;

import org.neo4location.domain.trajectory.Trajectory;
import org.neo4location.trajectory.Identification;

public class PredefinedTimeIntervalIdentification implements Identification {

	/*
	 * Divide the stream of GPS feed into several subsequences contained in given time intervals,
	 *  e.g., hourly trajectory, daily trajectory, weekly trajectory, monthly trajectory.
	 */

	private Trajectory trajectory;
	private Duration duration;

	public PredefinedTimeIntervalIdentification(Trajectory trajectory, Duration duration){

		this.trajectory = trajectory;
		this.duration = duration;

	}

	@Override
	public void process() {
		
		/*Iterator<Point> rawPoints = trajectory.getPoints().iterator();
		List<Point> tempTrajectory = new ArrayList<Point>();
		Duration sumDuration = Duration.ZERO;
		
		Point first = rawPoints.hasNext() ? rawPoints.next() : null;
		
		while(rawPoints.hasNext()){
			
			//RawPoint first = rawPoints.next();
			//RawPoint second = rawPoints.hasNext() ? rawPoints.next() : null;
			Point second = rawPoints.next();
			
			//if second is null, first is the last raw point.
//			if(second == null)	
//				break;
			
			Instant instantFirst = first.getTimeInstant();
			Instant instantSecond = second.getTimeInstant();
			
			Duration tempDuration = Duration.between(instantFirst, instantSecond);
			sumDuration.plus(tempDuration);
			
			if (sumDuration.equals(duration)){
				//end of trajectory, create temp trajectory 
				//and prepare for a new trajectory
				createSemanticTrajectory(tempTrajectory);
				tempTrajectory = new ArrayList<Point>();
			}
			tempTrajectory.add(first);
			
			first = second;
			
		}
*/


	}

	private void createSemanticTrajectory(List<Point> tempTrajectory2) {
		// TODO Auto-generated method stub
		
	}

}