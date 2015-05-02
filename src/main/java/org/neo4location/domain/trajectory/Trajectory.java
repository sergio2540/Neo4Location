package org.neo4location.domain.trajectory;

import java.util.Collection;
import java.util.Map;


public class Trajectory {

	private String mTrajectoryName;
	private User mUser;
	private Collection<Move> mMoves;
	private Map<String,Object> mSemanticData;
	
	
	public Trajectory(){
		
	}
	
	public Trajectory(String trajectoryName, User user, Collection<Move> moves, Map<String,Object> semanticData){
		
		mTrajectoryName = trajectoryName;
		mUser = user;
		mMoves = moves;
		mSemanticData = semanticData;
		
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
	
	public Map<String, Object> getSemanticData() {
		return mSemanticData;
	}

	public void setSemanticData(Map<String, Object> semanticData) {
		this.mSemanticData = semanticData;
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