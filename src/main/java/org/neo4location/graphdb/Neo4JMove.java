package org.neo4location.graphdb;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.graphdb.Relationship;
import org.neo4location.domain.trajectory.Move;
import org.neo4location.domain.trajectory.Point;


public class Neo4JMove implements Move {

	Point mFrom;
	Point mTo;

	double mDurationInSeconds;
	double mDistanceInMeters;

	Map<String,Object> mProperties;
	
	public Neo4JMove(Point from, Point to, Map<String,Object> properties)
	{
		mFrom = from;
		mTo = to;
		mProperties = properties;

	}

	public Neo4JMove(Relationship move)
	{

		mFrom = new Neo4JPoint(move.getStartNode());
		mTo = new Neo4JPoint(move.getEndNode());

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
	public Point getTo() {
		return mTo;
	}


	@Override
	public Map<String,Object> getSemanticData() {
		
		return mProperties;
	
	}

}