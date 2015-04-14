package org.neo4location.graphdb;
import java.util.ArrayList;
import java.util.Collection;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4location.domain.Neo4LocationRelationships;
import org.neo4location.domain.trajectory.Move;
import org.neo4location.domain.trajectory.Point;
import org.neo4location.domain.trajectory.Trajectory;


public class Neo4JTrajectory implements Trajectory {
	
//	public Neo4JTrajectory(Iterable<Node> nodes, Neo4LocationLabels label){
//		
//		Arrays.asList(StreamSupport.stream(nodes.spliterator(), true)
//        .filter(
//        		(n) -> n.hasLabel(label)
//        		)
//        .sorted(
//        		(n1,n2) -> ((String) n1.getProperty(Neo4LocationProperties.TIMESTAMP, "")).
//        		compareTo((String) n2.getProperty(Neo4LocationProperties.TIMESTAMP, ""))
//        		)
//        .toArray(Trajectory[]::new));
//	}
	
	private Node mTrajectory;
	private Collection<Point> mPoints;
	private Collection<Move> mMoves;

	public Neo4JTrajectory(Node trajectory){
	
		Relationship from = trajectory.getSingleRelationship(Neo4LocationRelationships.FROM, Direction.OUTGOING);
		
		if(from !=null)
		createPointsAndMoves(from);
		
	}
	
	
	private void createPointsAndMoves(Relationship move) {
		
		Collection<Point> pTemp = new ArrayList<>();
		Collection<Move> mTemp = new ArrayList<>();
		
		Node start = move.getEndNode();
		pTemp.add(new Neo4JPoint(start));
		
		//Relationship move = from;
		
		while(move != null) {
		
			move  = start.getSingleRelationship(Neo4LocationRelationships.MOVE, Direction.OUTGOING);
			if(move != null){
				
				move.getEndNode();
				mTemp.add(new Neo4JMove(move));
				pTemp.add(new Neo4JPoint(move.getEndNode()));
	
			}
		}
		
		mPoints = pTemp;
		mMoves = mTemp;
	
	}


	//public Point getFrom();
	//	public Point getTo();
	//
	//	public long getStart();
	//	public long getEnd();
	//or getLocations()
	
	
	public Collection<Point> getPoints(){
		return mPoints;
	}
	

	public Collection<Move> getMoves(){
		return mMoves;
	}


	@Override
	public void setPoints(Collection<Point> points){
		mPoints = points;	
	}


	@Override
	public void setMoves(Collection<Move> moves) {
		mMoves = moves;
	}

}