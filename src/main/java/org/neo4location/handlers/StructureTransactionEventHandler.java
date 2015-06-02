package org.neo4location.handlers;


import java.util.concurrent.CompletableFuture;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.event.TransactionData;
import org.neo4j.graphdb.event.TransactionEventHandler;
import org.neo4location.processing.Structure;
import org.neo4location.utils.Neo4LocationProcessingUtils;
import org.neo4location.utils.Neo4LocationService;

public class StructureTransactionEventHandler implements TransactionEventHandler<Void> {

	private final Structure mStructure;
	private final Neo4LocationService mNeo4LocationService;

	public StructureTransactionEventHandler(GraphDatabaseService graphDatabaseService, Structure structure){

		mNeo4LocationService = new Neo4LocationService();
		mStructure = structure;
		

	}

	@Override
	public Void beforeCommit(TransactionData data) throws Exception {
		return null;
	}

	@Override
	public void afterCommit(final TransactionData data, Void state) {

		//final Collection<Trajectory> trajs = Neo4LocationProcessingUtils.toCollectionTrajectory(data);

		CompletableFuture.supplyAsync(() -> Neo4LocationProcessingUtils.toCollectionTrajectory(data))
		 .thenApplyAsync((ts1) -> mStructure.process(ts1))
		 .thenAcceptAsync((ts2) -> mNeo4LocationService.write(ts2));

	}
	
	
	@Override
	public void afterRollback(TransactionData data, Void state) {

	}

}