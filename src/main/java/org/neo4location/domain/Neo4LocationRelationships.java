package org.neo4location.domain;

import java.io.Serializable;

import org.neo4j.graphdb.RelationshipType;

public enum Neo4LocationRelationships implements RelationshipType, Serializable, Comparable<Neo4LocationRelationships> {
	
	//User START_A TRAJECTORY
	START_A,
	//FROM UK TO LISBON
	FROM,
	TO,
	//TIME
	START,
	END,
	
	//RAW DATA NEXT
	MOVE,
	
	//ROUTE
	UKNOWN_WAY, //or WAY
	FOOTWAY, 
	CYCLEWAY, 
	HIGHWAY, 
	RAILWAY, 
	WATERWAY, 
	AERIALWAY, 
	SUBWAY,

	//TRAJ
	//MOVE
	//MODES OF TRANSPORT
	UNKNOW_MODE, //or MOVE
	CAR,
	MOTORBIKE,
	BUS, 
	TAXI,
	TRUCK,
	TRAIN, 
	WALK,
	BYCYCLE,
	PLANE,
	HELICOPTER,
	BOAT
	
}