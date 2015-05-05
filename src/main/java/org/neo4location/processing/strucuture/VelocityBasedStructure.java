package org.neo4location.processing.strucuture;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.neo4location.domain.trajectory.Point;
//import org.neo4location.domain.trajectory.SemanticTrajectory;
import org.neo4location.domain.trajectory.Trajectory;
import org.neo4location.processing.Structure;

public class VelocityBasedStructure implements Structure {

	//Semantic Trajectories: Mobility Data Computation and Annotation, Yan 2012
	//4.3. Trajectory VelocityBasedStructure Layer (pag. 12)

	//We analyze sensitivity of the coefficients δ1 and δ2 (e.g., δ1 = δ2 = δ = 30%) through experiments.
	//pag. 13
	
	
	private double delta1 = 0.3;
	private double delta2 = 0.3;

	private double mSpeedThreshold;
	private Duration mMinStopTime;
	
	
	private Collection<Trajectory> mTrajectories;

	
	private List<Point> tempStopPoints;
	private List<Point> tempMovePoints;


	public VelocityBasedStructure(double speedThreshold, long minStopTime, double delta1, double delta2){

		this.mSpeedThreshold = speedThreshold;
		this.mMinStopTime = Duration.ofMillis(minStopTime);

		this.delta1 = delta1;
		this.delta2 = delta2;

	}
	
	public void setTrajectories(Collection<Trajectory> trajectories){
		
		mTrajectories = trajectories;
	
	}
	
	public Collection<Trajectory> getTrajectory(){
		
		return mTrajectories;
		
	}


	public void velocityBased(Trajectory trajectory){

		//this.trajectory = trajectory;

		/* initialize: calculate GPS instant SPEED if needed */
		if(mSpeedThreshold == 0){
			mSpeedThreshold = getDynamicSpeedThreshold(null,null,delta1,delta2);
		}


		/* episode annotation: tag each GPS point with ‘S’ or ‘M’ */


		//Collection<Point> rawPoints = trajectory.getPoints();

		Collection<Point> rawPoints = null;
		tempStopPoints = new ArrayList<Point>();
		tempMovePoints = new ArrayList<Point>();

		for(Point point : rawPoints){

			double instantSpeed = point.getRawData().getSpeed();
			if (instantSpeed < mSpeedThreshold){

				computeMovePoints(tempMovePoints);

				tempStopPoints.add(point);

			} else {

				computeStopPoints(tempStopPoints);

				tempMovePoints.add(point);
			}
		}

		return;	
	}

	private void computeStopPoints(List<Point> groupOfStopPoints) {

		//		double interval = groupOfMovePoints
		//						  .stream()
		//						  .mapToDouble(RawPoint::getInterval)
		//						  .sum();


		int last = groupOfStopPoints.size()-1;
		int first = 0;

		Instant instantFrom = Instant.ofEpochMilli(groupOfStopPoints.get(first).getRawData().getTime());
		Instant instantTo = Instant.ofEpochMilli(groupOfStopPoints.get(last).getRawData().getTime());

		//TODO:
		//minStopTime == Duration.ofSeconds(sec,nano)
		//REVER
		Duration duration = Duration.between(instantFrom, instantTo);


		if (duration.compareTo(mMinStopTime) == 1){

			//TODO: Spatial
			double center = 0;
			double mbr = 0;

			//stop : (timefrom, timeto, center, boundingRectangle);
			// add the stop episode
			Stop stop = 
					new Stop(groupOfStopPoints.get(first), 
							groupOfStopPoints.get(last) ,
							instantFrom.toEpochMilli(), 
							instantTo.toEpochMilli(), center, mbr);


		} else {

			tempMovePoints.addAll(groupOfStopPoints);

		}

	}


	private void computeMovePoints(List<Point> groupOfMovePoints) {

		int last = groupOfMovePoints.size()-1;
		int first = 0;

		Instant instantFrom = Instant.ofEpochMilli(groupOfMovePoints.get(first).getRawData().getTime());
		Instant instantTo = Instant.ofEpochMilli(groupOfMovePoints.get(last).getRawData().getTime());

		Duration duration = Duration.between(instantFrom, instantTo);

		// create a move episode
		SMove move = new SMove(instantFrom.toEpochMilli() , instantTo.toEpochMilli(), duration);

		// add the move episode
		//trajectory.setMoves(move);

	}


	/**
	 * @param point
	 * @param obj
	 * @param delta1 - 
	 * @param delta2 - 
	 * @return double
	 */

	//TODO: 
	private double getDynamicSpeedThreshold(Object p, Object obj, double delta1, double delta2){

		//p - ponto x,y,z
		//obj_id - objeto -> car, bus

		double velocityThreshold = 0;
		//objectAvgSpeed – the average SPEED of this moving object
		double objectAvgSpeed = 0;
		//positionAvgSpeed – the average SPEED of most moving objects in this position ⟨x, y⟩
		double positionAvgSpeed = 0;


		velocityThreshold = Math.min(delta1*objectAvgSpeed, delta2*positionAvgSpeed);
		return velocityThreshold;
	}


	@Override
	public Void call() throws Exception {
		
		for(Trajectory trajectory : mTrajectories){

			velocityBased(trajectory);
		}
		
		return null;
	}

}