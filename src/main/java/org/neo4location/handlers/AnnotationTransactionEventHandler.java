package org.neo4location.handlers;


import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.event.TransactionData;
import org.neo4j.graphdb.event.TransactionEventHandler;
import org.neo4location.domain.trajectory.Trajectory;
import org.neo4location.processing.Annotation;
import org.neo4location.processing.Identification;
import org.neo4location.services.Neo4LocationService;
import org.neo4location.utils.Neo4LocationProcessingUtils;

public class AnnotationTransactionEventHandler implements TransactionEventHandler<Collection<Trajectory>> {

  private final Annotation mAnnotation;
  private final Neo4LocationService mNeo4LocationService;

  private final GraphDatabaseService mGraphDatabaseService;

  public AnnotationTransactionEventHandler(GraphDatabaseService graphDatabaseService, Annotation annotation) {

    mNeo4LocationService = new Neo4LocationService();
    mAnnotation = annotation;
    mGraphDatabaseService = graphDatabaseService;

  }

  @Override
  public Collection<Trajectory> beforeCommit(TransactionData data) throws Exception {
    return Neo4LocationProcessingUtils.toCollectionTrajectory(mGraphDatabaseService, data);
  }

  @Override
  public void afterCommit(final TransactionData data, final Collection<Trajectory> state) {

    if(state.isEmpty()){
      return;
    }
    
    int size = state.size();

//    Collection<Trajectory> col = mAnnotation.process(state);
//    int size2 = col.size();
//    mNeo4LocationService.write( mGraphDatabaseService,col);

    CompletableFuture.supplyAsync(() -> mAnnotation.process(state))
    .thenApplyAsync((ts2) -> mNeo4LocationService.write(mGraphDatabaseService, ts2));

  }

  @Override
  public void afterRollback(TransactionData data, Collection<Trajectory> state) {

  }


}