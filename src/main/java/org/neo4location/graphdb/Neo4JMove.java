package org.neo4location.graphdb;


import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.neo4j.graphdb.Relationship;
import org.neo4location.domain.Neo4LocationRelationships;
import org.neo4location.domain.trajectory.Move;


public class Neo4JMove {

//	Neo4LocationRelationships mRel;
//
//	Point mFrom;
//	Point mTo;

	Neo4LocationRelationships mRel;
	Move mMove;
	
	//Map<String,Object> mProperties;

	public Neo4JMove(){

	}

//	private Neo4JMove(Neo4LocationRelationships rel, Point from, Point to, Map<String,Object> properties)
//	{
//		if(from == null && to == null && properties == null)
//			throw new IllegalArgumentException();
//
//  		mRel = rel;
//  		mFrom = from;
//  		mTo = to;
//	    	mProperties = properties;
//
//	}

	public Neo4JMove(Relationship move)
	{
		mMove = new Move();
		
		Set<Neo4LocationRelationships> rels = EnumSet.allOf(Neo4LocationRelationships.class);

		for(Neo4LocationRelationships rel: rels){

			if(move.getType().equals(rel)){
				mMove.setRelationship(rel);
				break;
			}
		}

		mMove.setFrom(new Neo4JPoint(move.getStartNode()).getPoint());
		mMove.setTo(new Neo4JPoint(move.getEndNode()).getPoint());

		Map<String,Object> temp = new HashMap<>();

		for(String k : move.getPropertyKeys()){
			temp.put(k, move.getProperty(k));
		}

		mMove.setSemanticData(temp);


		//mMove = Move.create(mRel, mFrom, mTo, mProperties);

	}



	public Move getMove() {
		return mMove;
	}

	//Remover manter apenas getMove


	@Override
	public String toString(){

		return mMove.toString();

	}

	

}