package org.neo4location.graphdb;

import org.neo4j.graphdb.Node;
import org.neo4location.domain.Neo4LocationProperties;
import org.neo4location.domain.trajectory.User;

public class Neo4JPerson {
	
	User mPerson;
	
	public Neo4JPerson(Node person)
	{
		String personName = (String) person.getProperty(Neo4LocationProperties.USERNAME);
		
		mPerson = new User(personName);

	}

	public User getPerson(){
		return mPerson;
	}
	
	public void setPerson(User user){
		mPerson = user;
	}
	
	

}
