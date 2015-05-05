package org.neo4location.graphdb;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4location.domain.Neo4LocationLabels;
import org.neo4location.domain.Neo4LocationProperties;
import org.neo4location.domain.Neo4LocationRelationships;
import org.neo4location.domain.trajectory.Move;
import org.neo4location.domain.trajectory.Point;
import org.neo4location.domain.trajectory.RawData;
import org.neo4location.domain.trajectory.SemanticData;


public class Neo4JPoint {


	//private String Neo4LocationLabels;
	
	private Node mNeo4JNode;
	private Point mPoint;

	public Neo4JPoint(){

	}
	
	public Point getPoint(){
		return mPoint;
	}

	public Neo4JPoint(Node neo4jPoint){
		mNeo4JNode = neo4jPoint;
		
		if(mPoint == null) mPoint = new Point();

		createRawData(neo4jPoint);
		createSemanticData(neo4jPoint);
		createLabels(neo4jPoint);
	}


	private Neo4JPoint(Point p) {
		
		mPoint = p;
		
		Node n = mNeo4JNode.getGraphDatabase().createNode(p.getLabels().toArray(new Label[0]));

		RawData rd = p.getRawData();
		
		if(rd != null){
			n.setProperty(Neo4LocationProperties.LATITUDE, rd.getLatitude());
			n.setProperty(Neo4LocationProperties.LONGITUDE, rd.getLongitude());
			n.setProperty(Neo4LocationProperties.ALTITUDE, rd.getAltitude());
	
			n.setProperty(Neo4LocationProperties.ACCURACY, rd.getAccuracy());
	
			n.setProperty(Neo4LocationProperties.SPEED, rd.getSpeed());
			n.setProperty(Neo4LocationProperties.TIMESTAMP, rd.getTime());
		}

		Map<String,Object> sd = p.getSemanticData();
		
		if(sd != null){
			sd.forEach((k,v) -> mNeo4JNode.setProperty(k, v));
		}
		
		mNeo4JNode = n;
	}




	private void createLabels(Node neo4jPoint) {
		
		Collection<Neo4LocationLabels> list = new ArrayList<>();
		Set<Neo4LocationLabels> labels = EnumSet.allOf(Neo4LocationLabels.class);
		
		for(Neo4LocationLabels label: labels){
		
			if(neo4jPoint.hasLabel(DynamicLabel.label(label.name()))){
				list.add(label);
			}
		}
		
		mPoint.setLabels(list);
	}


	private void createSemanticData(Node neo4jPoint) {
		
		Map<String,Object> temp = new HashMap<>();

		Set<String> lockedKeys = new HashSet<>();
		lockedKeys.add(Neo4LocationProperties.LATITUDE);
		lockedKeys.add(Neo4LocationProperties.LONGITUDE);
		lockedKeys.add(Neo4LocationProperties.ALTITUDE);
		
		lockedKeys.add(Neo4LocationProperties.ACCURACY);
		lockedKeys.add(Neo4LocationProperties.SPEED);
		lockedKeys.add(Neo4LocationProperties.TIMESTAMP);
		
		for(String k : neo4jPoint.getPropertyKeys()){
			
			if(!lockedKeys.contains(k))
				temp.put(k, neo4jPoint.getProperty(k));
		
		}

		mPoint.setSemanticData(temp);

	}


	private void createRawData(Node neo4jPoint) {

		double latitude = (double) neo4jPoint.getProperty(Neo4LocationProperties.LATITUDE,-1.0);
		double longitude = (double) neo4jPoint.getProperty(Neo4LocationProperties.LONGITUDE,-1.0);
		double altitude = (double) neo4jPoint.getProperty(Neo4LocationProperties.ALTITUDE, -1.0);

		float accuracy = (float) neo4jPoint.getProperty(Neo4LocationProperties.ACCURACY, -1.0f);

		float speed = (float) neo4jPoint.getProperty(Neo4LocationProperties.SPEED, -1.0f);
		long timestamp = (long) neo4jPoint.getProperty(Neo4LocationProperties.TIMESTAMP, -1L);

		mPoint.setRawData(new RawData(latitude, longitude, altitude, accuracy, speed, timestamp));

	}
	
	
	private Collection<Neo4LocationLabels> getLabels() {

		return mPoint.getLabels();

	}
	
	private RawData getRawData() {
		return mPoint.getRawData();
	}


	private Map<String,Object> getSemanticData() {
		return mPoint.getSemanticData();
	}


	
	private void setLabels(Collection<Neo4LocationLabels> labels) {
		
		for(Neo4LocationLabels l: labels){
			
			Label neo4JLabel = DynamicLabel.label(l.name());
			
 			if(!mNeo4JNode.hasLabel(neo4JLabel)){
				mNeo4JNode.addLabel(neo4JLabel);
			}
		}
		
		mPoint.setLabels(labels);
	}
	
	private void setRawData(RawData rd) {
		
		mNeo4JNode.setProperty(Neo4LocationProperties.LATITUDE,rd.getLatitude());
		mNeo4JNode.setProperty(Neo4LocationProperties.LONGITUDE,rd.getLongitude());
		mNeo4JNode.setProperty(Neo4LocationProperties.ALTITUDE, rd.getAltitude());

		mNeo4JNode.setProperty(Neo4LocationProperties.ACCURACY, rd.getAccuracy());

		mNeo4JNode.setProperty(Neo4LocationProperties.SPEED, rd.getSpeed());
		mNeo4JNode.setProperty(Neo4LocationProperties.TIMESTAMP, rd.getTime());

		mPoint.setRawData(rd);

	}
	
	private void setSemanticData(Map<String,Object> sd) {
		
		sd.forEach( (k,v) -> mNeo4JNode.setProperty(k, v));
		mPoint.setSemanticData(sd);
	
	}


	//	@Override
	//	public Move getMove() {
	//		Move m = new Neo4JMove(mNeo4JNode.getSingleRelationship(Neo4LocationRelationships.MOVE, Direction.OUTGOING));
	//		return m;
	//	}
	//
	//
	//	@Override
	//	public void setMove(Move mv) {
	//		mNeo4JNode.createRelationshipTo(toNode(mv.getTo()), Neo4LocationRelationships.MOVE);
	//	}


	



	@Override
	public String toString() {

		return mPoint.toString();

	}
}