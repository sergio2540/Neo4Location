package org.neo4location.domain.trajectory;

import com.esotericsoftware.kryo.serializers.DefaultSerializers.StringSerializer;
import com.esotericsoftware.kryo.serializers.FieldSerializer.Bind;

public class User {

	@Bind(StringSerializer.class)
	private String mUsername;

	public User(){
	
	}

	public User(String personName){

		mUsername = personName;

	}

	public void setUsername(String personName){
		mUsername = personName;
	}

	public String getUsername(){
		return mUsername;
	}

//	@Override
//	public int hashCode()
//	{
//		return mUsername.hashCode();
//	}
//
//	@Override
//	public boolean equals( Object o )
//	{
//
//		return o instanceof User && mUsername.equals( ( (User)o ).getUsername() );
//	}
//
//	@Override
//	public String toString()
//	{
//	
//		return String.format("User: %s", mUsername);
//	
//	}

}
