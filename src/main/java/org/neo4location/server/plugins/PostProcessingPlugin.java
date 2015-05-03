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
import org.neo4location.domain.trajectory.Trajectory;
import org.neo4location.transactions.RawGPSGapIdentificationEventHandler;


@Description("A set of extensions that perform operations using the neo4location-post processing component")
public class PostProcessingPlugin extends ServerPlugin {
	
	
	@PluginTarget(GraphDatabaseService.class)
	@Description("Add a new structure")
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

		VelocityBasedStructure str = new VelocityBasedStructure(speedThreshold, _minStopTime, delta1, delta2);
		graphdb.registerTransactionEventHandler(new StructureTransactionEventHandler(graphdb, executor, str));
		
		return;
	
	}
	
	@PluginTarget(GraphDatabaseService.class)
	@Description("Add a new structure")
	public void addDensityBasedStructure(
			@Source GraphDatabaseService graphdb,
			@Description("Maximum distance.") @Parameter(name = "maxDistance", optional = true) double maxDistance,
			@Description("Min stop time. Default is ?") @Parameter(name = "minStopTime", optional = true) long minStopTime
			) 
	{
		
		DensityBasedStructure str = new DensityBasedStructure(maxDistance, _minStopTime);
		graphdb.registerTransactionEventHandler(new StructureTransactionEventHandler(graphdb, executor, str));
		
	
	}
	
	@PluginTarget(GraphDatabaseService.class)
	@Description("Add a new identification")
	public void addRawGPSGapIdentification(
			@Source GraphDatabaseService graphdb,
			@Description("Maximum distance.") @Parameter(name = "maxDistance", optional = true) double maxDistance,
			@Description("Min stop time. Default is ?") @Parameter(name = "minStopTime", optional = true) long minStopTime
			) 
	{
		
		RawGPSGapIdentification id = new RawGPSGapIdentification(distance, minStopTime);
		graphdb.registerTransactionEventHandler(new IdentificationEventHandler(graphdb, executor, id)) ;
	
	}
	
	@PluginTarget(GraphDatabaseService.class)
	@Description("Add a new identification")
	public void addPredefinedTimeInterval(
			@Source GraphDatabaseService graphdb,
			@Description("Min stop time. Default is ?") @Parameter(name = "minStopTime", optional = true) long minStopTime
			) 
	{
		PredefinedTimeIntervalIdentification id = new PredefinedTimeIntervalIdentification(maxDistance, minStopTime);
		graphdb.registerTransactionEventHandler(new StructureTransactionEventHandler(graphdb, executor, id));
	
	}
	
	

	/*
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
	*/

}