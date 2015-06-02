package org.neo4location.graphdb;

import java.util.Objects;

import org.neo4j.graphdb.Node;
import org.neo4location.domain.Neo4LocationProperties;
import org.neo4location.domain.trajectory.Person;

public class Neo4JPerson {
	
	private final Person mPerson;
	
	public Neo4JPerson(Node person)
	{
		String personName = (String) person.getProperty(Neo4LocationProperties.USERNAME);
		
		mPerson = new Person(personName);

	}

	public Person getPerson(){
		return mPerson;
	}
	
//	public void setPerson(Person person){
//		mPerson = person;
//	}
	
	@Override
	public int hashCode()
	{
		return mPerson.hashCode();
	}

	@Override
	public boolean equals( Object obj )
	{

		return  Objects.nonNull(obj) &&
				obj instanceof Neo4JPerson && 
				mPerson.equals(((Neo4JPerson) obj).getPerson());
	}

	@Override
	public String toString()
	{

		return mPerson.toString();

	}
	
}
