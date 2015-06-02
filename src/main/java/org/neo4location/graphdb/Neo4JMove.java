package org.neo4location.graphdb;


import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.graphdb.Relationship;
import org.neo4location.domain.Neo4LocationRelationships;
import org.neo4location.domain.trajectory.Move;
import org.neo4location.domain.trajectory.Point;


public class Neo4JMove {
	
	//Imutable
	private final Move mMove;
	
	//Imutable
	private final static EnumSet<Neo4LocationRelationships> rels = EnumSet.allOf(Neo4LocationRelationships.class);
	
	public Neo4JMove(final Relationship move)
	{
		
		//TODO: Verificar
		final Neo4LocationRelationships rel = Neo4LocationRelationships.MOVE;
		
		//Ineficiente
//		for(Neo4LocationRelationships tempRelationship: rels){
//			if(move.getType().name().equals(tempRelationship.name())){
//				rel = tempRelationship;
//				break;
//			}
//		}
		
		
		final Point from = new Neo4JPoint(move.getStartNode()).getPoint();
		final Point to = new Neo4JPoint(move.getEndNode()).getPoint();
		
		
		final Map<String,Object> sd = new HashMap<>();
		for(String k : move.getPropertyKeys()){
			sd.put(k, move.getProperty(k));
		}	
		
		mMove = new Move(rel, from, to, sd);
	}



	public Move getMove() {
		return mMove;
	}

	
	@Override
	public String toString(){

		return mMove.toString();

	}
	
}