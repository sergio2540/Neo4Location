package org.neo4location.processing.strucuture;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.graphdb.Label;
import org.neo4location.domain.trajectory.RawData;
import org.neo4location.domain.trajectory.SemanticData;

public class SMove  {

	public SMove(String trajname, long instantFrom, long instantTo) {
		
		
		Map<String,Object> prop = new HashMap<String,Object>();

		RawData rd = null;
		//new RawData(latitude, longitude, altitude, accuracy, speed, timestamp);

		Map<String,Object> _sd = new HashMap<String,Object>(); 

		//_sd.put("", duration);
		
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

	

	

}
