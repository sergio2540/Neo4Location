package org.neo4location.graphdb;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4location.domain.Neo4LocationProperties;
import org.neo4location.domain.Neo4LocationRelationships;
import org.neo4location.domain.trajectory.Move;
import org.neo4location.domain.trajectory.User;
import org.neo4location.domain.trajectory.Point;
import org.neo4location.domain.trajectory.Trajectory;


public class Neo4JTrajectory {
	

	
	private Node mNode;
	private Trajectory mTrajectory;
	
//	private Collection<Point> mPoints;
//	private Collection<Move> mMoves;

	public Neo4JTrajectory(Node trajectory){
		
				
		String trajectoryName  = (String) trajectory.getProperty(Neo4LocationProperties.TRAJNAME);
		
		Relationship startA = trajectory.getSingleRelationship(Neo4LocationRelationships.START_A, Direction.INCOMING);
		
		User user = null;
		
		if(startA != null){
			user = new Neo4JPerson(startA.getStartNode()).getPerson();
		}
	
		Relationship from = trajectory.getSingleRelationship(Neo4LocationRelationships.FROM, Direction.OUTGOING);
		
		Collection<Move> moves = null;
		if(from !=null)
			moves = createPointsAndMoves(from);
		
		
		Map<String,Object> props = new HashMap<String, Object>(); 
		mTrajectory = new Trajectory(trajectoryName, user, moves, props);
		
	}
	
	
	private Collection<Move> createPointsAndMoves(Relationship move) {
		
		//Collection<Point> pTemp = new ArrayList<>();
		Collection<Move> mTemp = new ArrayList<>();
		
		Node start = move.getEndNode();
		//pTemp.add(new Neo4JPoint(start).getPoint());
		
		//Relationship move = from;
		
		while(move != null) {
		
			move  = start.getSingleRelationship(Neo4LocationRelationships.MOVE, Direction.OUTGOING);
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
	
	
	public void setTrajectory(Trajectory trajectory){
		mTrajectory = trajectory;
	}


	//public Point getFrom();
	//	public Point getTo();
	//
	//	public long getStart();
	//	public long getEnd();
	//or getLocations()
	
	
//	public Collection<Point> getPoints(){
//		return mPoints;
//	}
//	
//
//	public Collection<Move> getMoves(){
//		return mMoves;
//	}
	
}