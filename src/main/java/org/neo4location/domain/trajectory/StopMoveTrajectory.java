package org.neo4location.domain.trajectory;

import org.neo4j.graphdb.Node;
import org.neo4location.domain.Neo4LocationLabels;
import org.neo4location.domain.Neo4LocationRelationships;

public class StopMoveTrajectory implements SemanticTrajectory {

	private Node mStopMoveTrajectoryNode;

	//private Iterable<Node> mSemanticTrajectory;
	
	public StopMoveTrajectory(Node stopMoveTrajectoryNode){

		this.mStopMoveTrajectoryNode=  stopMoveTrajectoryNode;

	}

	@Override
	public Iterable<Point> getPoints() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Point getFrom() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Point getTo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addNode(Neo4LocationLabels node) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addRelationship(Neo4LocationRelationships relationship) {
		// TODO Auto-generated method stub
		
	}


}