package org.neo4location.trajectory.strucuture;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.graphdb.Label;
import org.neo4location.domain.Neo4LocationRelationships;
import org.neo4location.domain.trajectory.Move;
import org.neo4location.domain.trajectory.Point;
import org.neo4location.domain.trajectory.RawData;
import org.neo4location.domain.trajectory.SemanticData;

public class SMove implements Move {

	public SMove(long instantFrom, long instantTo, Duration duration) {
		
		
		Map<String,Object> prop = new HashMap<String,Object>();

		RawData rd = null;
		//new RawData(latitude, longitude, altitude, accuracy, speed, timestamp);

		Map<String,Object> _sd = new HashMap<String,Object>(); 

		_sd.put("", duration);
		
		SemanticData sd = new SemanticData(_sd);
		Iterable<Label> labels = null;

		//Codigo Move
		
//		Move stop = Move.create(rd, sd, labels);
//		
//		Point beforeStartPoint = startPoint.getMove().getFrom();
//		Point afterStartPoint = endPoint.getMove().getTo();
//
//		beforeStartPoint.setMove(Move.create(beforeStartPoint, stop, prop));
//		afterStartPoint.setMove(Move.create(afterStartPoint, stop, prop));
	
	}

	@Override
	public Point getFrom() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Point getTo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> getSemanticData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Neo4LocationRelationships getRelationship() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setRelationship(Neo4LocationRelationships rt) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setFrom(Point from) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setTo(Point to) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setSemanticData(Map<String, Object> sd) {
		// TODO Auto-generated method stub
		
	}

}
