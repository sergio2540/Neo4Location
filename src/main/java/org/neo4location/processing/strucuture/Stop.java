package org.neo4location.processing.strucuture;


import java.util.HashMap;
import java.util.Map;

import org.neo4j.graphdb.Label;
import org.neo4location.domain.trajectory.Move;
import org.neo4location.domain.trajectory.Point;
import org.neo4location.domain.trajectory.RawData;
import org.neo4location.domain.trajectory.SemanticData;



public class Stop  {
	
	//Create Point
	//Add Label STOP
	//Add Properties
	//Add array stop_points: Point[]Relationships
	
	//stop.start
	//stop.end
	//stop.center
	
//	public Stop(Point startPoint, Point endPoint,  long start, long end, double center,
//			double mbr) {
//		
//		Map<String,Object> prop = new HashMap<String,Object>();
//		
//		RawData rd = null;
//				//new RawData(latitude, longitude, altitude, accuracy, speed, timestamp);
//		
//		Map<String,Object> _sd = new HashMap<String,Object>(); 
//		
//		_sd.put("", start);
//		_sd.put("", end);
//		_sd.put("", center);
//		_sd.put("", mbr);
//		
//		SemanticData sd = new SemanticData(_sd);
//		Iterable<Label> labels = null;
//		
////		Point stop = Point.create(rd, sd, labels);
////		
////		
////		
////		Point beforeStartPoint = startPoint.getMove().getFrom();
////		Point afterStartPoint = endPoint.getMove().getTo();
////		
////		beforeStartPoint.setMove(Move.create(beforeStartPoint, stop, prop));
////		afterStartPoint.setMove(Move.create(afterStartPoint, stop, prop));
//		
//		
//		
////		setLabels(Neo4LocationLabels.EPISODE);
//		
//	}

	public Stop(String trajname, long epochMilli, long epochMilli2,
			double center, double mbr) {
		// TODO Auto-generated constructor stub
	}



	
}