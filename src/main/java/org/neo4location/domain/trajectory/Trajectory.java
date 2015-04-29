package org.neo4location.domain.trajectory;

import java.util.Collection;



public class Trajectory {

	String mTrajectoryName;
	Person mPerson;
	Collection<Move> mMoves;
	
	public Trajectory(){
		
	}
	
	public Trajectory(String trajectoryName, Person person, Collection<Move> moves){
		
		mTrajectoryName = trajectoryName;
		mPerson = person;
		mMoves = moves;
		
	}
	
	
	public String getTrajectoryName(){
		return mTrajectoryName;	
	}
	
	public void setTrajectoryName(String trajectoryName){
		mTrajectoryName = trajectoryName;	
	}
	
	public Person getPerson(){
		return mPerson;	
	}
	
	public void setPerson(Person person){
		mPerson = person;	
	}
	
	
	public Collection<Move> getMoves(){
		return mMoves;
		
	}
	
	public void setMoves(Collection<Move> moves){
		mMoves = moves;
	}
	
}