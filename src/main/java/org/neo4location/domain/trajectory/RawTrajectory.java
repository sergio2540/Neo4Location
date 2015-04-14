package org.neo4location.domain.trajectory;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4location.domain.Neo4LocationLabels;
import org.neo4location.domain.Neo4LocationRelationships;

public class RawTrajectory implements Trajectory {

	private GraphDatabaseService db;
	private Node mRawTrajectoryNode;

	private Point mFrom;
	private Point mTo;
	private Iterable<Point> mPoints;


	private RawTrajectory(Node rawTrajectoryNode){
		this.mRawTrajectoryNode =  rawTrajectoryNode;
		this.db = mRawTrajectoryNode.getGraphDatabase();
	}

	@Override
	public Iterable<Point> getPoints() {
		
		Node startNode = getNode(Neo4LocationRelationships.FROM);
		int numberOfPaths = 0;
		Iterable<Node> rawPointNodes = null;
		
		for(Path path : db.traversalDescription().
				relationships(Neo4LocationRelationships.MOVE, Direction.OUTGOING).
				traverse(startNode)){

			rawPointNodes = path.nodes();
			numberOfPaths++;
		}

		if(numberOfPaths != 1){
			throw new IllegalStateException("A trajectory has "+numberOfPaths+" paths, must have only one path.");
		}
		
		Iterable<Point> points = Point.create(rawPointNodes);
		
		return points;

	}

	private Node getNode(Neo4LocationRelationships fromOrTo){

		//getSingleRelationship
		final Iterable<Relationship> relsFrom = mRawTrajectoryNode.getRelationships(fromOrTo, Direction.OUTGOING);
		int numberOfFromRel = 0;
		Node node = null;

		for (Relationship relFrom: relsFrom) {
			node = relFrom.getEndNode();
			numberOfFromRel++;
		}

		if(numberOfFromRel != 1){
			throw new IllegalStateException("A trajectory has "+numberOfFromRel+" "+fromOrTo+" relationships, must have one relationship "+fromOrTo+".");
		}
		
		return node;
	}

	@Override
	public Point getFrom() {	
		Node from = this.getNode(Neo4LocationRelationships.FROM);
		return new RawPoint(from);
	}

	@Override
	public Point getTo() {
		Node to = this.getNode(Neo4LocationRelationships.TO);
		return new RawPoint(to);
	}
	
	public Iterable<Point> getRawPoints(){
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