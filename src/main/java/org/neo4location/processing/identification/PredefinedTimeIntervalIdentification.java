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
import org.neo4location.domain.Neo4LocationProperties;
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
  
  private final static int PRIORITY = 1; 
	
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
		Duration minStop = Duration.ofMillis(mMinStopTime);
		
		Collection<Trajectory> tempTrajectories = new ArrayList<Trajectory>();
		
		for(Move m: moves){

			Point pFrom = m.getFrom();

			if(pFrom == null){
				return Collections.emptyList();
			}

			Point pTo = m.getTo();

			if(pTo == null){
				return Collections.emptyList();
			}

			RawData rFrom = pFrom.getRawData();
			RawData rTo = pTo.getRawData();


			Duration  delta_t = Neo4LocationProcessingUtils.interval(m);
			sumDuration = sumDuration.plus(delta_t);

			Duration t = minStop.minus(sumDuration);
			
			if (t.isZero() || t.isNegative()){
				//end of trajectory, create temp trajectory 
				//and prepare for a new trajectory
				
			
			  long startTrajectoryTime = (long) trajectory.getSemanticData().get(Neo4LocationProperties.START_INSTANT);
        String newTrajectoryName = String.format("%s-%s-%d", getName(), trajectory.getTrajectoryName(), startTrajectoryTime);
        
				Trajectory tempTrajectory = new Trajectory(newTrajectoryName, trajectory.getUser(), tempMoves, trajectory.getSemanticData());
				tempTrajectories.add(tempTrajectory);

			
				tempMoves = new ArrayList<Move>();
				sumDuration = Duration.ZERO;
				
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
	  
		return this.getClass().getSimpleName();
	
	}
	
	@Override
  public int getPriority() {
   
    return PRIORITY;
  }

}