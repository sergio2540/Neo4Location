package org.neo4location.transactions;

import java.util.concurrent.ExecutorService;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.event.TransactionData;
import org.neo4j.graphdb.event.TransactionEventHandler;
import org.neo4location.trajectory.identification.RawGPSGapIdentification;


public class RawGPSGapIdentificationEventHandler implements TransactionEventHandler<Void> {

	
	public static GraphDatabaseService db;
    private static ExecutorService ex;
 
    public RawGPSGapIdentificationEventHandler(GraphDatabaseService graphDatabaseService, ExecutorService executor) {
        db = graphDatabaseService;
        ex = executor;
    }
 
    @Override
    public Void beforeCommit(TransactionData transactionData) throws Exception {
        return null;
    }
 
    @Override
    public void afterCommit(TransactionData transactionData, Void o) {
    
        ex.submit(new RawGPSGapIdentification(transactionData, db));
    
    }
 
    @Override
    public void afterRollback(TransactionData transactionData, Void o) {
 
    }
	

}