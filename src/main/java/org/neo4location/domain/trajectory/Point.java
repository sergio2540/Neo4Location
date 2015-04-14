package org.neo4location.domain.trajectory;

import org.neo4j.graphdb.Label;
import org.neo4location.graphdb.Neo4JPoint;

public interface Point {

	//	static RawData mRawData;
	//	static SemanticData mSemanticData;
	//	static Iterable<Label> mNeo4jLabels;

	public static Point create(RawData rawData, SemanticData semanticData, Iterable<Label> labels){
			
		Point p = new Neo4JPoint(rawData,semanticData, labels); 
		
		return p;
	
	}
	
	//public Iterable<Point> create(Iterable<Object> nodePoints);

	//Meta informacao
	public Iterable<Label> getLabels();

	//Informacao
	public RawData getRawData();
	public SemanticData getSemanticData();

	public Move getMove();

	public void setLabels(Label label);

	public void setRawData(RawData rd);
	public void setSemanticData(SemanticData sd);

	public void setMove(Move mv);

}
