package org.neo4location.graphdb;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4location.domain.Neo4LocationProperties;
import org.neo4location.domain.Neo4LocationRelationships;
import org.neo4location.domain.trajectory.Move;
import org.neo4location.domain.trajectory.Person;
import org.neo4location.domain.trajectory.Trajectory;


public class Neo4JTrajectory {
	
	
	private final Trajectory mTrajectory;
	
	
	public Neo4JTrajectory(Node trajectory){
		
				
		String trajectoryName  = (String) trajectory.getProperty(Neo4LocationProperties.TRAJNAME);
		
		Relationship startA = trajectory.getSingleRelationship(DynamicRelationshipType.withName(Neo4LocationRelationships.START_A.name()), Direction.INCOMING);
		
		Person person = null;
		
		if(startA != null){
			person = new Neo4JPerson(startA.getStartNode()).getPerson();
		}
	
		Relationship from = trajectory.getSingleRelationship(DynamicRelationshipType.withName(Neo4LocationRelationships.FROM.name()), Direction.OUTGOING);
		
		Collection<Move> moves = new ArrayList<>();
		if(from != null)
			moves = createPointsAndMoves(from);
		
		
		Map<String,Object> props = new HashMap<String, Object>(); 
		
		mTrajectory = new Trajectory(trajectoryName, person, moves, props);
		
	}
	
	
	private Collection<Move> createPointsAndMoves(Relationship move) {
		
		//Collection<Point> pTemp = new ArrayList<>();
		Collection<Move> mTemp = new ArrayList<>();
		
		Node start = move.getEndNode();
		//pTemp.add(new Neo4JPoint(start).getPoint());
		
		//Relationship move = from;
		
		while(move != null) {
		
			move  = start.getSingleRelationship(DynamicRelationshipType.withName(Neo4LocationRelationships.MOVE.name()), Direction.OUTGOING);
			if(move != null){
				
				//move.getEndNode();
				mTemp.add(new Neo4JMove(move).getMove());
				//pTemp.add(new Neo4JPoint(move.getEndNode()).getPoint());
	
			}
		}
		
		return mTemp;
	
	}
	
	
	public Trajectory getTrajectory(){
		return mTrajectory;
	}
	
}