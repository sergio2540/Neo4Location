package org.neo4location.utils;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import org.neo4location.domain.Neo4LocationProperties;
import org.neo4location.domain.trajectory.Move;
import org.neo4location.domain.trajectory.RawData;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.distance.CartesianDistCalc;
import com.spatial4j.core.distance.GeodesicSphereDistCalc;
import com.spatial4j.core.distance.GeodesicSphereDistCalc.Haversine;
import com.spatial4j.core.shape.impl.PointImpl;

public class Neo4LocationProcessingUtils {


	public static Duration interval(Move mv, RawData rFrom, RawData rTo) {

		Duration duration;
		if (rFrom == null || rTo == null){
			//Symbolic point
			//Get Distance + time
			Map<String, Object> sd = mv.getSemanticData();
			duration = Duration.ofMillis((long) sd.getOrDefault(Neo4LocationProperties.DURATION, -1));


		} else {

			Instant instantFirst = Instant.ofEpochMilli(rFrom.getTime());
			Instant instantSecond = Instant.ofEpochMilli(rTo.getTime());
			duration = Duration.between(instantFirst, instantSecond);



		}

		return duration;
	}

	public static double distance(Move mv, RawData rFrom, RawData rTo) {

		double distance = 0;

		if (rFrom == null || rTo == null){
			//Symbolic point
			//Get Distance + time
			Map<String, Object> sd = mv.getSemanticData();
			distance = (double) sd.getOrDefault(Neo4LocationProperties.DISTANCE, -1);

			if(distance == -1){
				//TODO: Lancar excepcao
			}



		} else {

			//Usar spatial 4j 
			CartesianDistCalc cd = new CartesianDistCalc();
			GeodesicSphereDistCalc gdh = new GeodesicSphereDistCalc.Haversine();
			GeodesicSphereDistCalc gdlc = new GeodesicSphereDistCalc.LawOfCosines();
			GeodesicSphereDistCalc gdv = new GeodesicSphereDistCalc.Vincenty();
			
			PointImpl from = new PointImpl(rFrom.getLongitude(), rFrom.getLatitude(), SpatialContext.GEO);
			PointImpl to = new PointImpl(rTo.getLongitude(), rTo.getLatitude(), SpatialContext.GEO);

			
			//Falta verificar se e cartesiano ou geo
			Haversine haversine = new GeodesicSphereDistCalc.Haversine();
			distance = haversine.distance(from, to);

		}		
		return distance;
	}

}