package org.neo4location.domain.trajectory;

public class Person {

	String mPersonName;

	public Person(){
	}

	public Person(String personName){

		mPersonName = personName;

	}

	public void setPersonName(String personName){
		mPersonName = personName;
	}

	public String getPersonName(){
		return mPersonName;
	}

	@Override
	public int hashCode()
	{
		return mPersonName.hashCode();
	}

	@Override
	public boolean equals( Object o )
	{

		return o instanceof Person && mPersonName.equals( ( (Person)o ).getPersonName() );
	}

	@Override
	public String toString()
	{

		return "Person[" + getPersonName() + "]";
	}

}
