package org.neo4location.domain.trajectory;

import java.util.Collection;


public class Trajectory {

	String mTrajectoryName;
	User mUser;
	Collection<Move> mMoves;
	
	public Trajectory(){
		
	}
	
	public Trajectory(String trajectoryName, User user, Collection<Move> moves){
		
		mTrajectoryName = trajectoryName;
		mUser = user;
		mMoves = moves;
		
	}
	
	
	public String getTrajectoryName(){
		return mTrajectoryName;	
	}
	
	public void setTrajectoryName(String trajectoryName){
		mTrajectoryName = trajectoryName;	
	}
	
	public User getUser(){
		return mUser;	
	}
	
	public void setUser(User user){
		mUser = user;	
	}
	
	
	public Collection<Move> getMoves(){
		return mMoves;
		
	}
	
	public void setMoves(Collection<Move> moves){
		mMoves = moves;
	}
	
	@Override
	public String toString(){
		
		StringBuilder sb = new StringBuilder();
		
		sb.append(String.format("[Trajectory %s %s", mTrajectoryName, mUser));
		
		for(Move move : mMoves){
			sb.append(String.format(" %s", move));
		}
		
		sb.append("]");
		
		return sb.toString();
	}
	
}