package org.neo4location.domain.trajectory;

import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;


public final class Person implements Serializable, Comparable<Person> {

	
	private static final long serialVersionUID = 1L;
	
	
	//@Bind(StringSerializer.class)
	private final String mPersonName;

	//	public Person(){
	//	
	//	}

	@JsonCreator 
	public Person(@JsonProperty("personName") String personName){

		mPersonName = personName;

	}

	public String getPersonName(){
		return mPersonName;
	}

	/*
	public void setUsername(String personName){
			mPersonName = personName;
	}
	*/



	@Override
	public int hashCode()
	{
		return Objects.hashCode(mPersonName);
	}

	@Override
	public boolean equals( Object obj )
	{

	  //Objects.equals(a, b)
		return Objects.nonNull(obj) && 
				obj instanceof Person && 
				Objects.equals(mPersonName, ((Person)obj).getPersonName());
	}

	@Override
	public String toString()
	{

		return String.format("Person: %s", mPersonName);

	}

  @Override
  public int compareTo(Person o) {
   
    return mPersonName.compareTo(o.getPersonName());
  
  }

}