package org.neo4location.handlers;


import java.util.Collection;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.PriorityBlockingQueue;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.event.TransactionData;
import org.neo4j.graphdb.event.TransactionEventHandler;
import org.neo4location.domain.trajectory.Trajectory;
import org.neo4location.processing.ProcessHandler;
import org.neo4location.services.Neo4LocationService;
import org.neo4location.utils.Neo4LocationProcessingUtils;

public class ProcessEventHandler implements TransactionEventHandler<Collection<Trajectory>> {

  private final SortedSet<ProcessHandler> mProcesses;
  private final Neo4LocationService mNeo4LocationService;

  private final GraphDatabaseService mGraphDatabaseService;

  public ProcessEventHandler(GraphDatabaseService graphDatabaseService) {

    mGraphDatabaseService = graphDatabaseService;

    mNeo4LocationService = new Neo4LocationService();

    SortedSet<ProcessHandler> ss = new TreeSet<ProcessHandler>(new Comparator<ProcessHandler>() {

      @Override
      public int compare(ProcessHandler ph1, ProcessHandler ph2) {
        return Integer.compare(ph1.getPriority(),ph2.getPriority());
      }

    });

    mProcesses =  ss;

  }



  @Override
  public Collection<Trajectory> beforeCommit(TransactionData data) throws Exception {
    return Neo4LocationProcessingUtils.toCollectionTrajectory(mGraphDatabaseService, data);
  }

  @Override
  public void afterCommit(final TransactionData data, final Collection<Trajectory> trajectories) {

    if(trajectories.isEmpty()){
      return;
    }

    int size = trajectories.size();

    //    Collection<Trajectory> col = mProcess.process(state);
    //    int size2 = col.size();
    //    mNeo4LocationService.write( mGraphDatabaseService,col);

    //dispatch(trajectories);
    CompletableFuture.runAsync(() -> dispatch(trajectories));


  }

  @Override
  public void afterRollback(TransactionData data, Collection<Trajectory> state) {

  }


  private void dispatch(final Collection<Trajectory> trajectories){

    for(ProcessHandler p : mProcesses){
      Collection<Trajectory> newTrajectories = p.process(trajectories);
      int size = newTrajectories.size();
      mNeo4LocationService.write(mGraphDatabaseService, newTrajectories);
    }

  }


  //TODO: THRED SAFE
  public void registerEventHandler(ProcessHandler p) {
    mProcesses.add(p);
  }


}