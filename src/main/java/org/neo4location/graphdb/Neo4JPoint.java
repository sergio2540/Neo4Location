package org.neo4location.graphdb;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4location.domain.Neo4LocationProperties;
import org.neo4location.domain.Neo4LocationRelationships;
import org.neo4location.domain.trajectory.Move;
import org.neo4location.domain.trajectory.Point;
import org.neo4location.domain.trajectory.RawData;
import org.neo4location.domain.trajectory.SemanticData;


public class Neo4JPoint {


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

		SemanticData sd = p.getSemanticData();
		
		if(sd != null){
			sd.getKeysAndValues().forEach((k,v) -> mNeo4JNode.setProperty(k, v));
		}
		
		mNeo4JNode = n;
	}




	private void createLabels(Node neo4jPoint) {
		Collection<Label> list = new ArrayList<Label>();
		Iterable<Label> iter = neo4jPoint.getLabels();
		for (Label item : iter) {
	        list.add(item);
	    }
		
		mPoint.setLabels(list);
	}


	private void createSemanticData(Node neo4jPoint) {
		Map<String,Object> temp = new HashMap<>();

		for(String k : neo4jPoint.getPropertyKeys()){
			temp.put(k, neo4jPoint.getProperty(k));
		}

		mPoint.setSemanticData(new SemanticData(temp));

	}


	private void createRawData(Node neo4jPoint) {

		double latitude = (double) neo4jPoint.getProperty(Neo4LocationProperties.LATITUDE,-1.0);
		double longitude = (double) neo4jPoint.getProperty(Neo4LocationProperties.LONGITUDE,-1.0);
		double altitude = (double) neo4jPoint.getProperty(Neo4LocationProperties.ALTITUDE, -1.0);

		float accuracy = (float) neo4jPoint.getProperty(Neo4LocationProperties.ACCURACY, -1.0);

		float speed = (float) neo4jPoint.getProperty(Neo4LocationProperties.SPEED, -1.0);
		long timestamp = (long) neo4jPoint.getProperty(Neo4LocationProperties.TIMESTAMP, -1);

		mPoint.setRawData(new RawData(latitude, longitude, altitude, accuracy, speed, timestamp));

	}
	
	
	public Collection<Label> getLabels() {

		return mPoint.getLabels();

	}
	
	public RawData getRawData() {
		return mPoint.getRawData();
	}


	public SemanticData getSemanticData() {
		return mPoint.getSemanticData();
	}


	
	public void setLabels(Collection<Label> labels) {
		
		for(Label l: labels){
			if(!mNeo4JNode.hasLabel(l)){
				mNeo4JNode.addLabel(l);
			}
		}
		
		mPoint.setLabels(labels);
	}
	
	public void setRawData(RawData rd) {
		
		mNeo4JNode.setProperty(Neo4LocationProperties.LATITUDE,rd.getLatitude());
		mNeo4JNode.setProperty(Neo4LocationProperties.LONGITUDE,rd.getLongitude());
		mNeo4JNode.setProperty(Neo4LocationProperties.ALTITUDE, rd.getAltitude());

		mNeo4JNode.setProperty(Neo4LocationProperties.ACCURACY, rd.getAccuracy());

		mNeo4JNode.setProperty(Neo4LocationProperties.SPEED, rd.getSpeed());
		mNeo4JNode.setProperty(Neo4LocationProperties.TIMESTAMP, rd.getTime());

		mPoint.setRawData(rd);

	}
	
	public void setSemanticData(SemanticData sd) {
		
		sd.getKeysAndValues().forEach( (k,v) -> mNeo4JNode.setProperty(k, v));
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