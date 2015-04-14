package org.neo4location.server.plugins;

import java.time.Duration;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.server.plugins.Description;
import org.neo4j.server.plugins.Parameter;
import org.neo4j.server.plugins.PluginTarget;
import org.neo4j.server.plugins.ServerPlugin;
import org.neo4j.server.plugins.Source;
//import org.neo4location.trajectory.strucuture.VelocityBasedStructure;
//import org.neo4location.transactions.StructureTransactionEventHandler;


@Description("A set of extensions that perform operations using the neo4location-post processing component")
public class PostProcessingPlugin extends ServerPlugin {

	private GraphDatabaseService db;

	//Neo4LocationDatabaseService
	//private SpatialDatabaseService neo4LocationDatabaseService;

	@PluginTarget(GraphDatabaseService.class)
	@Description("add a new structure")
	public void addVelocityBasedStructure(
			@Source GraphDatabaseService graphdb,
			@Description("Speed property.") @Parameter(name = "speedThreshold", optional = true) double speedThreshold,
			@Description("Min stop time. Default is ?") @Parameter(name = "minStopTime", optional = true) String minStopTime,
			@Description("Delta 1. Default is 0.3") @Parameter(name = "delta1", optional = true) double delta1,
			@Description("Delta 2. Default is 0.3") @Parameter(name = "delta2", optional = true) double delta2
			) 
	{

		//System.out.println("Creating new layer '" + layer + "' unless it already exists");
		Duration _minStopTime = Duration.parse(minStopTime);

//		VelocityBasedStructure str = new VelocityBasedStructure(speedThreshold, _minStopTime, delta1, delta2);
//		graphdb.registerTransactionEventHandler(new StructureTransactionEventHandler(str));
		return;
	}


	@PluginTarget(GraphDatabaseService.class)
	@Description("add a new structure")
	public void addRawTrajectory(
			@Source GraphDatabaseService graphdb,
			@Description("Unique user identifier.") @Parameter(name = "userId", optional = true) String userId,
			@Description(".Default 1") @Parameter(name = "LATITUDE", optional = true) String lat,
			@Description("") @Parameter(name = "LONGITUDE", optional = true) double lon,
			@Description("") @Parameter(name = "ALTITUDE", optional = true) double alt,
			@Description("") @Parameter(name = "ACCURACY", optional = true) double accuracy,

			@Description("") @Parameter(name = "transport", optional = true) String transport,
			@Description("") @Parameter(name = "confidence", optional = true) double confidence

			) 
	{
		return;
	}


	@PluginTarget(GraphDatabaseService.class)
	@Description("add a new structure")
	public void getUsers(
			@Source GraphDatabaseService graphdb,

			@Description("Speed property.") @Parameter(name = "speedThreshold", optional = true) double lat,
			@Description("Min stop time. Default is ?") @Parameter(name = "minStopTime", optional = true) double lon,
			@Description("Delta 1. Default is 0.3") @Parameter(name = "delta1", optional = true) double alt,
			
			
			@Description("Delta 1. Default is 0.3") @Parameter(name = "delta1", optional = true) double accuracy,


			@Description("Delta 1. Default is 0.3") @Parameter(name = "delta1", optional = true) String interval
			) 
	{

		//System.out.println("Creating new layer '" + layer + "' unless it already exists");

		return;
	}


	@PluginTarget(GraphDatabaseService.class)
	@Description("add a new structure")
	public void getMostPopularPath(
			@Source GraphDatabaseService graphdb,

			@Description("Speed property.") @Parameter(name = "source_lat", optional = true) double sourceLat,
			@Description("Min stop time. Default is ?") @Parameter(name = "source_lon", optional = true) double sourceLon,
			@Description("Delta 1. Default is 0.3") @Parameter(name = "source_alt", optional = true) double sourceAlt,
			
			@Description("Speed property.") @Parameter(name = "target_lat", optional = true) double targetLat,
			@Description("Min stop time. Default is ?") @Parameter(name = "target_lon", optional = true) double targetLon,
			@Description("Delta 1. Default is 0.3") @Parameter(name = "target_alt", optional = true) double targetAlt,

			
			@Description("Delta 1. Default is 0.3") @Parameter(name = "ACCURACY", optional = true) double accuracy,


			@Description( "The relationship types to follow when searching for the shortest path(s). " +
					"Order is insignificant, if omitted all types are followed." )
			@Parameter( name = "relTypes", optional = true ) String[] relTypes,
			
			
			@Description( "The maximum path length to search for, default value (if omitted) is 4." )
			@Parameter( name = "depth", optional = true ) Integer depth

			) 
	{

		//System.out.println("Creating new layer '" + layer + "' unless it already exists");

		return;
	}

	@PluginTarget(GraphDatabaseService.class)
	@Description("add a new structure")
	public Iterable<Node> getKLastPositions(
			@Source GraphDatabaseService graphdb,

			@Description("User identifier.") @Parameter(name = "userId", optional = true) String userId,
			@Description("Trajectory.") @Parameter(name = "trajectory", optional = true) String trajectory,

			@Description("Number of last positions returned. Default is 0") @Parameter(name = "k", optional = true) int k
			) 
			{

		//System.out.println("Creating new layer '" + layer + "' unless it already exists");

		return null;
			}



}