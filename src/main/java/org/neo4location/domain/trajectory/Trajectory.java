package org.neo4location.domain.trajectory;

import java.util.Collection;



public interface Trajectory {
	
	
	//public Point getFrom();
	//	public Point getTo();
	//
	//	public long getStart();
	//	public long getEnd();

	
	//or getLocations()
	
	public Collection<Point> getPoints();
	public Collection<Move> getMoves();
	

	public void setPoints(Collection<Point> points);
	public void setMoves(Collection<Move> moves);
	
	
}