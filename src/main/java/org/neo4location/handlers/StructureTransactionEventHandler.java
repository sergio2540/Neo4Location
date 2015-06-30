package org.neo4location.handlers;


import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.event.TransactionData;
import org.neo4j.graphdb.event.TransactionEventHandler;
import org.neo4location.domain.trajectory.Trajectory;
import org.neo4location.processing.Structure;
import org.neo4location.utils.Neo4LocationProcessingUtils;
import org.neo4location.utils.Neo4LocationService;

public class StructureTransactionEventHandler implements TransactionEventHandler<Collection<Trajectory>> {

	private final Structure mStructure;
	private final Neo4LocationService mNeo4LocationService;
	private final GraphDatabaseService mGraphDatabaseService;
	

	public StructureTransactionEventHandler(GraphDatabaseService graphDatabaseService, Structure structure){

		mNeo4LocationService = new Neo4LocationService();
		mStructure = structure;
		mGraphDatabaseService = graphDatabaseService;
		
	}

	@Override
	public Collection<Trajectory> beforeCommit(TransactionData data) throws Exception {
		return Neo4LocationProcessingUtils.toCollectionTrajectory(mGraphDatabaseService, data);
	}

	@Override
	public void afterCommit(final TransactionData data, Collection<Trajectory> state) {
	  
	  if(state.isEmpty()){
      return;
    }
	  
//		CompletableFuture.supplyAsync(() -> mStructure.process(ts1))
//		 .thenAcceptAsync((ts2) -> mNeo4LocationService.write(ts2));

	  
	}
	
	
	@Override
	public void afterRollback(TransactionData data, Collection<Trajectory> state) {

	}

}