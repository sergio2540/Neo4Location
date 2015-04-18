package org.neo4location.domain.trajectory;

import java.util.Map;





import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;
import org.neo4location.domain.Neo4LocationRelationships;
import org.neo4location.graphdb.Neo4JMove;

//@JsonTypeInfo(
//use = JsonTypeInfo.Id.NAME, 
//include = JsonTypeInfo.As.EXTERNAL_PROPERTY, 
//property = "type"
//)
//@JsonSubTypes({
//	@JsonSubTypes.Type(value = Neo4JMove.class, name = "Neo4JMove"),
//})
public interface Move {

	public static Move create(Neo4LocationRelationships rel, Point from, Point to, Map<String, Object> properties){

		Move mv = new Neo4JMove(rel, from,to,properties); 

		return mv;

	}

	public Neo4LocationRelationships getRelationship();
	public void setRelationship(Neo4LocationRelationships rt);
	
	public Point getFrom();
	public void setFrom(Point from);
	public Point getTo();
	public void setTo(Point to);


	//	public double getDuration();
	//	public double getDistance();

	public Map<String,Object> getSemanticData();
	public void setSemanticData(Map<String,Object>  sd);

//	public Label getLabel();
//	public void setLabel(Label label);



	//	public Move(Point from, Point to, double durationInSeconds, double distanceInMeters, ){
	//	
	//		mFrom = from;
	//		mTo = to;
	//		
	//		mDurationInSeconds = durationInSeconds;
	//		mDistanceInMeters = distanceInMeters;
	//		
	//		mProperties = properties;
	//		
	//	}


}