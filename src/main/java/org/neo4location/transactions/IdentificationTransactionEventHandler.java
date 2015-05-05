package org.neo4location.transactions;

import java.util.concurrent.ExecutorService;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.event.TransactionData;
import org.neo4j.graphdb.event.TransactionEventHandler;
import org.neo4location.processing.Identification;

public class IdentificationTransactionEventHandler implements TransactionEventHandler<Void> {

	
	
//	public static GraphDatabaseService db;
//  private static ExecutorService ex;
    
    private Identification mIdentification;
	private static GraphDatabaseService mGraphDatabaseService;
	private ExecutorService mExecutor;
    
 
    public IdentificationTransactionEventHandler(GraphDatabaseService graphDatabaseService, ExecutorService executor, Identification identification) {
        mGraphDatabaseService = graphDatabaseService;
        mExecutor = executor;
        mIdentification = identification;
    }
 
	@Override
	public Void beforeCommit(TransactionData data) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void afterCommit(TransactionData data, Void state) {
		
		mExecutor.submit(mIdentification);
		
	}

	@Override
	public void afterRollback(TransactionData data, Void state) {
		// TODO Auto-generated method stub
		
	}
	
	
}
