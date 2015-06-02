package org.neo4location.graphdb;


import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Node;
import org.neo4location.domain.Neo4LocationLabels;
import org.neo4location.domain.Neo4LocationProperties;
import org.neo4location.domain.trajectory.Point;
import org.neo4location.domain.trajectory.RawData;


public class Neo4JPoint {
	
	private final Point mPoint;
	
	private final static Set<String> lockedKeys = new HashSet<>();
	private final static Set<Neo4LocationLabels> LABELS = EnumSet.allOf(Neo4LocationLabels.class);
	
	static {
		
		lockedKeys.add(Neo4LocationProperties.LATITUDE);
		lockedKeys.add(Neo4LocationProperties.LONGITUDE);
		lockedKeys.add(Neo4LocationProperties.ALTITUDE);
		
		lockedKeys.add(Neo4LocationProperties.ACCURACY);
		lockedKeys.add(Neo4LocationProperties.SPEED);
		lockedKeys.add(Neo4LocationProperties.TIMESTAMP);
	}
	
	
	
	public Point getPoint(){
		return mPoint;
	}

	public Neo4JPoint(Node neo4jPoint){
		
		//mPoint = new Point();

		RawData rawData = createRawData(neo4jPoint);
		Map<String,Object> semanticData = createSemanticData(neo4jPoint);
		Collection<Neo4LocationLabels> labels = createLabels(neo4jPoint);
		
		mPoint = new Point(rawData, semanticData, labels);
	}




	private Collection<Neo4LocationLabels> createLabels(Node neo4jPoint) {
		
		Collection<Neo4LocationLabels> labels = new ArrayList<>();

		for(Neo4LocationLabels label: LABELS){
		
			if(neo4jPoint.hasLabel(DynamicLabel.label(label.name()))){
				labels.add(label);
			}
		}
		
		return labels;
	}


	private Map<String,Object> createSemanticData(Node neo4jPoint) {
		
		Map<String,Object> temp = new HashMap<>();
		
		for(String k : neo4jPoint.getPropertyKeys()){
			
			if(!lockedKeys.contains(k))
				temp.put(k, neo4jPoint.getProperty(k));
		
		}

		return temp;
	}

	private RawData createRawData(Node neo4jPoint) {

		double latitude = (double) neo4jPoint.getProperty(Neo4LocationProperties.LATITUDE);
		double longitude = (double) neo4jPoint.getProperty(Neo4LocationProperties.LONGITUDE);
		long timestamp = (long) neo4jPoint.getProperty(Neo4LocationProperties.TIMESTAMP);
		
		
		double nodeAlt = (double) neo4jPoint.getProperty(Neo4LocationProperties.ALTITUDE, -1.0);
		final Double altitude = (nodeAlt  != -1.0) ? nodeAlt : null;

		
		float nodeAcc = (float) neo4jPoint.getProperty(Neo4LocationProperties.ACCURACY, -1.0f);
		final Float accuracy = (nodeAcc  != -1.0) ? nodeAcc : null;
		
		
		float nodeSpeed = (float) neo4jPoint.getProperty(Neo4LocationProperties.SPEED, -1.0f);
		final Float speed = (nodeSpeed  != -1.0) ? nodeSpeed : null;
		
		float nodeBearing = (float) neo4jPoint.getProperty(Neo4LocationProperties.BEARING, -1.0f);
		final Float bearing = (nodeBearing  != -1.0) ? nodeBearing : null;
		
		return new RawData(latitude, longitude,timestamp, altitude, accuracy, speed, bearing);

	}
	
	
	@Override
	public int hashCode()
	{
		//TODO: Hash Code
		return super.hashCode();
	}
	
	
	@Override
	public boolean equals(final Object obj) {
		
		//TODO: Verificar sincronização
		
		if(obj != null && obj instanceof Neo4JPoint){
			
			final Neo4JPoint point = (Neo4JPoint) obj;
			
			if(mPoint.equals(point.getPoint())){
				return true;
			}
			
		}
		
		return false;	
	}

	
	@Override
	public String toString() {

		return mPoint.toString();

	}

}