package org.neo4location.graphdb;
import java.util.ArrayList;
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


public class Neo4JPoint implements Point {


	Node mNeo4JNode;
	RawData mRawData;
	SemanticData mSemanticData;
	//Remover
	Iterable<Label> mNeo4jLabels;

	public Neo4JPoint(Node neo4jPoint){

		mNeo4JNode = neo4jPoint;

		createRawData(neo4jPoint);
		createSemanticData(neo4jPoint);
		createLabels(neo4jPoint);
	}


	public Neo4JPoint(RawData rawData, SemanticData semanticData,
			Iterable<Label> labels) {
		
		mRawData = rawData;
		mSemanticData = semanticData;
		mNeo4jLabels = labels;
		
	}


	private void createLabels(Node neo4jPoint) {
		
		mNeo4jLabels = neo4jPoint.getLabels();

	}


	private void createSemanticData(Node neo4jPoint) {
		Map<String,Object> temp = new HashMap<>();

		for(String k : neo4jPoint.getPropertyKeys()){
			temp.put(k, neo4jPoint.getProperty(k));
		}

		mSemanticData = new SemanticData(temp);

	}


	private void createRawData(Node neo4jPoint) {

		double latitude = (double) neo4jPoint.getProperty(Neo4LocationProperties.LATITUDE,-1.0);
		double longitude = (double) neo4jPoint.getProperty(Neo4LocationProperties.LONGITUDE,-1.0);
		double altitude = (double) neo4jPoint.getProperty(Neo4LocationProperties.ALTITUDE, -1.0);

		float accuracy = (float) neo4jPoint.getProperty(Neo4LocationProperties.ACCURACY, -1.0);

		float speed = (float) neo4jPoint.getProperty(Neo4LocationProperties.SPEED, -1.0);
		long timestamp = (long) neo4jPoint.getProperty(Neo4LocationProperties.TIMESTAMP, -1);

		mRawData = new RawData(latitude, longitude, altitude, accuracy, speed, timestamp);

	}


	@Override
	public Iterable<Label> getLabels() {

		return mNeo4jLabels;

	}

	@Override
	public RawData getRawData() {
		return mRawData;
	}

	@Override
	public SemanticData getSemanticData() {
		return mSemanticData;
	}


	@Override
	public void setLabels(Label label) {
		mNeo4JNode.addLabel(label);

	}


	@Override
	public void setRawData(RawData rd) {


		mNeo4JNode.setProperty(Neo4LocationProperties.LATITUDE,rd.getLatitude());
		mNeo4JNode.setProperty(Neo4LocationProperties.LONGITUDE,rd.getLongitude());
		mNeo4JNode.setProperty(Neo4LocationProperties.ALTITUDE, rd.getAltitude());

		mNeo4JNode.setProperty(Neo4LocationProperties.ACCURACY, rd.getAccuracy());

		mNeo4JNode.setProperty(Neo4LocationProperties.SPEED, rd.getSpeed());
		mNeo4JNode.setProperty(Neo4LocationProperties.TIMESTAMP, rd.getTime());

		//mNeo4JNode.setProperty(key, value);

	}


	@Override
	public void setSemanticData(SemanticData sd) {
		Set<Entry<String, Object>> temp = sd.getKeysAndValues();


		for (Entry<String, Object> entry : temp) {
			mNeo4JNode.setProperty(entry.getKey(), entry.getValue());
		}


		//		Map<String,Object> temp = new HashMap<>();
		//		
		//		for(String k : neo4jPoint.getPropertyKeys()){
		//			temp.put(k, neo4jPoint.getProperty(k));
		//		}

	}


	@Override
	public Move getMove() {
		Move m = new Neo4JMove(mNeo4JNode.getSingleRelationship(Neo4LocationRelationships.MOVE, Direction.OUTGOING));
		return m;
	}


	@Override
	public void setMove(Move mv) {
		mNeo4JNode.createRelationshipTo(toNode(mv.getTo()), Neo4LocationRelationships.MOVE);
	}


	private Node toNode(Point p){

		Node n = mNeo4JNode.getGraphDatabase().createNode();
		
		
		for(Label l : p.getLabels()){
			n.addLabel(l);
		}

		n.setProperty(Neo4LocationProperties.LATITUDE, p.getRawData().getLatitude());
		n.setProperty(Neo4LocationProperties.LONGITUDE,p.getRawData().getLongitude());
		n.setProperty(Neo4LocationProperties.ALTITUDE, p.getRawData().getAltitude());

		n.setProperty(Neo4LocationProperties.ACCURACY, p.getRawData().getAccuracy());

		n.setProperty(Neo4LocationProperties.SPEED, p.getRawData().getSpeed());
		n.setProperty(Neo4LocationProperties.TIMESTAMP, p.getRawData().getTime());

		Set<Entry<String, Object>> temp = p.getSemanticData().getKeysAndValues();
		for (Entry<String, Object> entry : temp) {
			mNeo4JNode.setProperty(entry.getKey(), entry.getValue());
		}


		return n;
	}
}