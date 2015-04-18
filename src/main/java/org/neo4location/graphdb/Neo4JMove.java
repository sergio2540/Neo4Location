package org.neo4location.graphdb;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4location.domain.Neo4LocationRelationships;
import org.neo4location.domain.trajectory.Move;
import org.neo4location.domain.trajectory.Point;


public class Neo4JMove implements Move {

	Neo4LocationRelationships mRel;
	Point mFrom;
	Point mTo;
	
//	double mDurationInSeconds;
//	double mDistanceInMeters;

	Map<String,Object> mProperties;

	public Neo4JMove(){
		
	}
	
	public Neo4JMove(Neo4LocationRelationships rel, Point from, Point to, Map<String,Object> properties)
	{
		if(from == null && to == null && properties == null)
			throw new IllegalArgumentException();

		mRel = rel;
		mFrom = from;
		mTo = to;
		mProperties = properties;

	}

	public Neo4JMove(Relationship move)
	{

		mFrom = new Neo4JPoint(move.getStartNode()).getPoint();
		mTo = new Neo4JPoint(move.getEndNode()).getPoint();

		//		mDurationInSeconds = durationInSeconds;
		//		mDistanceInMeters = distanceInMeters;

		Map<String,Object> temp = new HashMap<>();

		for(String k : move.getPropertyKeys()){
			temp.put(k, move.getProperty(k));
		}

		mProperties = temp;

	}


	@Override
	public Point getFrom() {
		return mFrom;
	}
	
	@Override
	public void setFrom(Point from) {
		mFrom = from;
	}
	


	@Override
	public Point getTo() {
		return mTo;
	}
	
	@Override
	public void setTo(Point to) {
		mTo = to;
	}

	@Override
	public Map<String,Object> getSemanticData() {

		return mProperties;

	}
	
	@Override
	public void setSemanticData(Map<String,Object> sd) {

		mProperties = sd;

	}

	@Override
	public String toString(){
		
		return String.format("[from= %s to= %s]", mFrom, mTo);
	
	}

	@Override
	public Neo4LocationRelationships getRelationship() {
		return mRel;
	}

	@Override
	public void setRelationship(Neo4LocationRelationships rt) {
		mRel = rt;
		
	}

}