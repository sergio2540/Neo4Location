package org.neo4location.domain.trajectory;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;


public final class Trajectory implements Serializable, Comparable<Trajectory> {

	
	private static final long serialVersionUID = 1L;

	private final String mTrajectoryName;
	
	private final Person mUser;
	
	private final Iterable<Move> mMoves;
	
	private final Map<String,Object> mSemanticData;

	//	public Trajectory(){
	//
	//	}

	@JsonCreator
	public Trajectory(@JsonProperty("trajectoryName") String trajectoryName, 
					  @JsonProperty("user") Person user, 
					  @JsonProperty("moves") Iterable<Move> moves, 
					  @JsonProperty("semanticData") Map<String,Object> semanticData){

		mTrajectoryName = trajectoryName;
		mUser = user;
		mMoves = moves;
		mSemanticData = new ConcurrentHashMap<String, Object>(semanticData);

	}


	public String getTrajectoryName(){
		return mTrajectoryName;	
	}


	public Person getUser(){
		return mUser;	
	}


	public Iterable<Move> getMoves(){
		return mMoves;

	}
	
	public Map<String, Object> getSemanticData() {
		return mSemanticData;
	}
	
	/*
	public void setTrajectoryName(String trajectoryName){
		mTrajectoryName = trajectoryName;	
	}

	public void setUser(Person person){
		mUser = person;	
	}
	
	public void setMoves(Collection<Move> moves){
		mMoves = moves;
	}

	public void setSemanticData(Map<String, Object> semanticData) {
		this.mSemanticData = semanticData;
	}
	*/
	
	@Override
	public int hashCode()
	{
		//TODO: Hash Code
		return Objects.hash(mTrajectoryName,mUser, mSemanticData, mMoves);
	}


	@Override
	public boolean equals(final Object obj) {
		
		return Objects.nonNull(obj) &&
			   obj instanceof Trajectory &&
			   Objects.equals(mTrajectoryName, ((Trajectory)obj).getTrajectoryName()) &&
			   Objects.equals(mUser, ((Trajectory)obj).getUser()) &&
			   Objects.equals(mSemanticData, ((Trajectory)obj).getSemanticData()) &&
			   Objects.equals(mMoves, ((Trajectory)obj).getMoves())
			   ;	
	
	}
	
	@Override
	public String toString(){

		StringBuilder sb = new StringBuilder();

		sb.append(String.format("[%s %s", mTrajectoryName, mUser));

		for(Move move : mMoves){
			sb.append(String.format(" %s", move));
		}

		sb.append("]");

		return sb.toString();
	}


  @Override
  public int compareTo(Trajectory o) {
    
    return  mTrajectoryName.compareTo(o.getTrajectoryName());
  
  }

}