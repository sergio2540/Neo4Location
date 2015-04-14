package org.neo4location.domain;

public class User {
	
	
	
	
	
	
	
	@Override
	public int hashCode()
	{
		return 0;
	    //return underlyingNode.hashCode();
	}

	@Override
	public boolean equals( Object o )
	{
		return false;
	    //return o instanceof Person && underlyingNode.equals( ( (Person)o ).getUnderlyingNode() );
	}

	@Override
	public String toString()
	{
		return null;
	    //return "User[" + getName() + "]";
	}

}
