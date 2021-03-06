package org.neo4location.handlers;


import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.event.TransactionData;
import org.neo4j.graphdb.event.TransactionEventHandler;
import org.neo4location.domain.trajectory.Trajectory;
import org.neo4location.processing.Structure;
import org.neo4location.services.Neo4LocationService;
import org.neo4location.utils.Neo4LocationProcessingUtils;

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

    int size = state.size();
    //  Collection<Trajectory> col =  mStructure.process(state);
    //  mNeo4LocationService.write( mGraphDatabaseService,col);

    CompletableFuture.supplyAsync(() ->  mStructure.process(state))
    .thenApplyAsync((ts) -> mNeo4LocationService.write(mGraphDatabaseService, ts));

  }


  @Override
  public void afterRollback(TransactionData data, Collection<Trajectory> state) {

  }

}