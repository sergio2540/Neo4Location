package org.neo4location.transactions;

import java.util.stream.Stream;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.event.TransactionData;
import org.neo4j.graphdb.event.TransactionEventHandler;
import org.neo4location.domain.Neo4LocationLabels;
import org.neo4location.domain.trajectory.Trajectory;
import org.neo4location.trajectory.strucuture.VelocityBasedStructure;

public class StructureTransactionEventHandler implements TransactionEventHandler<Void> {

	VelocityBasedStructure str;

	public StructureTransactionEventHandler(VelocityBasedStructure str){

		this.str = str;

	}

	@Override
	public Void beforeCommit(TransactionData data) throws Exception {

		//Comparator<Node> byNodeInstantTime = (n1, n2) -> String.compare(n1.getInstantTime(), n2.getInstantTime());
		//employees.stream().sorted(byEmployeeNumber).forEach(e -> System.out.println(e));
		
		Iterable<Node> nodes = data.createdNodes();
		
//		Iterable<Trajectory> trajectories = Trajectory.extract(nodes, Neo4LocationLabels.TRAJECTORY);
//		
//		for(Trajectory trajectory: trajectories)
//			str.velocityBased(trajectory);
		
		return null;

	}

	@Override
	public void afterCommit(TransactionData data, Void state) {

		return;
	}

	@Override
	public void afterRollback(TransactionData data, Void state) {

		// TODO Auto-generated method stub

	}




}