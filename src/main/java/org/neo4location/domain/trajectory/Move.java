package org.neo4location.domain.trajectory;

import java.util.Map;

import org.neo4location.graphdb.Neo4JMove;


public interface Move {
	
	public static Move create(Point from, Point to, Map<String, Object> properties){
		
		Move mv = new Neo4JMove(from,to,properties); 
		
		return mv;
	
	}
	
	public Point getFrom();
	public Point getTo();

	
//	public double getDuration();
//	public double getDistance();
	
	public Map<String,Object> getSemanticData();
	
	
//	public Move(Point from, Point to, double durationInSeconds, double distanceInMeters, ){
//	
//		mFrom = from;
//		mTo = to;
//		
//		mDurationInSeconds = durationInSeconds;
//		mDistanceInMeters = distanceInMeters;
//		
//		mProperties = properties;
//		
//	}
	
	
}