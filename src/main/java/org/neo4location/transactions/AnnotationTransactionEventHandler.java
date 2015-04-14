package org.neo4location.transactions;

import org.neo4j.graphdb.event.TransactionData;
import org.neo4j.graphdb.event.TransactionEventHandler;

public class AnnotationTransactionEventHandler implements TransactionEventHandler<Void> {

	
	@Override
	public Void beforeCommit(TransactionData data) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void afterCommit(TransactionData data, Void state) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterRollback(TransactionData data, Void state) {
		// TODO Auto-generated method stub
		
	}
	
	
}
