package org.neo4location.domain;

import java.io.Serializable;

import org.neo4j.graphdb.Label;

public enum Neo4LocationLabels implements Label, Serializable, Comparable<Neo4LocationLabels> {
	
	//TIME
	INSTANT,
	INTERVAL,
	
	//TRAJECTORY
	TRAJECTORY,
	ROUTE,
	
	//LOCATION
	POINT,
	EPISODE,
	
	//STOP
	//ACTIVITY
	
	USER,
	

}
