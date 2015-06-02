package org.neo4location.processing.strucuture;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.neo4location.domain.Neo4LocationProperties;
import org.neo4location.domain.trajectory.Move;
import org.neo4location.domain.trajectory.Point;
import org.neo4location.domain.trajectory.RawData;
//import org.neo4location.domain.trajectory.SemanticTrajectory;
import org.neo4location.domain.trajectory.Trajectory;
import org.neo4location.processing.Structure;

public class VelocityBasedStructure implements Structure {

	//Semantic Trajectories: Mobility Data Computation and Annotation, Yan 2012
	//4.3. Trajectory VelocityBasedStructure Layer (pag. 12)

	//We analyze sensitivity of the coefficients δ1 and δ2 (e.g., δ1 = δ2 = δ = 30%) through experiments.
	//pag. 13

	

	private double mDelta1 = 0.3;
	private double mDelta2 = 0.3;

	private float mSpeedThreshold;
	private long mMinStopTime;


	private List<Move> tempStopPoints;
	private List<Move> tempMovePoints;


	public VelocityBasedStructure(float speedThreshold, long minStopTime, double delta1, double delta2){

		

		mSpeedThreshold = speedThreshold;
		mMinStopTime = minStopTime;

		mDelta1 = delta1;
		mDelta2 = delta2;

	}

	public Collection<Trajectory> velocityBased(Trajectory trajectory){

		//this.trajectory = trajectory;

		/* initialize: calculate GPS instant SPEED if needed */
		if(mSpeedThreshold == 0){
			mSpeedThreshold = getDynamicSpeedThreshold(null,null,mDelta1,mDelta2);
		}


		/* episode annotation: tag each GPS point with ‘S’ or ‘M’ */


		//Collection<Point> rawPoints = trajectory.getPoints();

		Iterable<Move> mvs = trajectory.getMoves();

		tempStopPoints = new ArrayList<Move>();
		tempMovePoints = new ArrayList<Move>();

		for(Move move : mvs){

			Point pFrom = move.getFrom();
			Point pTo = move.getTo();

			RawData rdFrom = pFrom.getRawData();
			
			//TODO: So From na velocity???
			if(rdFrom == null || rdFrom.getSpeed() == null){
				//Nao tem informação raw ou velocidade
				continue;
			}
				
			float instantSpeed = rdFrom.getSpeed();

			if (instantSpeed < mSpeedThreshold){

				SMove smove = computeMovePoints(tempMovePoints);

				if(smove != null){

				}

				tempStopPoints.add(move);

			} else {

				Stop stop = computeStopPoints(tempStopPoints);
				
				if(stop != null){

				}


				tempMovePoints.add(move);
			}
		}
		
		
		//Return trajectorias criadas
		return null;
	}

	private Stop computeStopPoints(List<Move> groupOfStopPoints) {


		int last = groupOfStopPoints.size()-1;
		int first = 0;

		Move mFirst = groupOfStopPoints.get(first);
		Move mLast = groupOfStopPoints.get(last);

		String trajname = (String) mFirst.getFrom().getSemanticData().get(Neo4LocationProperties.TRAJNAME);

		Point pFrom = mFirst.getFrom();
		Point pTo = mLast.getTo();

		Instant instantFrom = Instant.ofEpochMilli(pFrom.getRawData().getTime());

		Instant instantTo = Instant.ofEpochMilli(pTo.getRawData().getTime());

		//TODO:
		//minStopTime == Duration.ofSeconds(sec,nano)
		//REVER
		Duration duration = Duration.between(instantFrom, instantTo);


		if (duration.compareTo(Duration.ofMillis(mMinStopTime)) == 1){

			//TODO: Spatial
			double center = 0;
			double mbr = 0;

			//stop : (timefrom, timeto, center, boundingRectangle);
			// add the stop episode
			return new Stop(trajname,
					instantFrom.toEpochMilli(), 
					instantTo.toEpochMilli(), center, mbr);


		} else {

			tempMovePoints.addAll(groupOfStopPoints);
			return null;

		}

	}


	private SMove computeMovePoints(List<Move> groupOfMovePoints) {

		int last = groupOfMovePoints.size()-1;
		int first = 0;


		Move mFirst = groupOfMovePoints.get(first);
		Move mLast = groupOfMovePoints.get(last);

		String trajname = (String) mFirst.getFrom().getSemanticData().get(Neo4LocationProperties.TRAJNAME);

		Point pFrom = mFirst.getFrom();
		Point pTo = mLast.getTo();

		Instant instantFrom = Instant.ofEpochMilli(pFrom.getRawData().getTime());

		Instant instantTo = Instant.ofEpochMilli(pTo.getRawData().getTime());

		//Duration duration = Duration.between(instantFrom, instantTo);

		// create a move episode
		SMove move = new SMove(trajname, instantFrom.toEpochMilli() , instantTo.toEpochMilli());

		return move;

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
	private float getDynamicSpeedThreshold(Object p, Object obj, double delta1, double delta2){

		//p - ponto x,y,z
		//obj_id - objeto -> car, bus

		float velocityThreshold = 0;
		//objectAvgSpeed – the average SPEED of this moving object
		float objectAvgSpeed = 0;
		//positionAvgSpeed – the average SPEED of most moving objects in this position ⟨x, y⟩
		float positionAvgSpeed = 0;


		velocityThreshold = (float) Math.min(delta1*objectAvgSpeed, delta2*positionAvgSpeed);
		return velocityThreshold;
	}


	@Override
	public Collection<Trajectory> process(Collection<Trajectory> trajectories) {
		
		
		if(trajectories == null){
			//Throw exception with text you must call setTrajectories(Collection<Trajectory> trajectories)
			return Collections.emptyList();

		}
		
		
		return trajectories.stream()
				    	   .map((trajectory) -> velocityBased(trajectory))
						   .flatMap((col) -> col.stream())
						   .collect(Collectors.toList());
	
	}

	@Override
	public String getName() {

		return "VelocityBased";

	}

}