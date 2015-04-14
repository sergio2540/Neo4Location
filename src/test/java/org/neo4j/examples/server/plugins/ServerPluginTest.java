/**
 * Copyright (c) 2010-2013 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.examples.server.plugins;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

//import org.neo4j.gis.spatial.server.plugin.SpatialPlugin;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;




public class ServerPluginTest extends Neo4jTestCase {

	private static final String LAYER = "layer";
	private static final String LON = "LONGITUDE";
	private static final String LAT = "LATITUDE";
	//private SpatialPlugin plugin;

	@Before
	public void setUp() throws Exception {
		super.setUp();
		//plugin = new SpatialPlugin();

	}

	@After
	public void tearDown() throws Exception {
		super.tearDown();
	}

	@Test
	public void testCreateLayer() {
		
		//SpatialDatabaseService spatialService = new SpatialDatabaseService(graphDb());
		//assertNull(spatialService.getLayer(LAYER));
		//plugin.addSimplePointLayer(graphDb(), LAYER, LAT, LON);

		//assertNotNull(spatialService.getLayer(LAYER));
	}

	@Test
	public void testSearchPoints() {
	    Transaction tx2 = graphDb().beginTx();
        Node point = graphDb().createNode();
        point.setProperty(LAT, 60.1);
        point.setProperty(LON, 15.2);
        tx2.success();
        tx2.finish();
        //plugin.addSimplePointLayer( graphDb(), LAYER, LAT, LON );
        
		//SpatialDatabaseService spatialService = new SpatialDatabaseService(graphDb());
		//Layer layer = spatialService.getLayer(LAYER);
        //debugIndexTree((RTreeIndex) layer.getIndex());
        
        //plugin.addNodeToLayer(graphDb(), point, LAYER);
        //Iterable<Node> geometries = plugin.findGeometriesInBBox( graphDb(), 15.0, 15.3, 60.0, 60.2, LAYER );
        //assertTrue( geometries.iterator().hasNext() );
        
        //geometries = plugin.findGeometriesWithinDistance(graphDb(), 15.2, 60.1, 100, LAYER);
        //assertTrue(geometries.iterator().hasNext());
        
        
//        plugin.addEditableLayer(graphDb(), LAYER);
//        plugin.addGeometryWKTToLayer(graphDb(), "POINT(15.2 60.1)", LAYER);
//        plugin.addCQLDynamicLayer(graphDb(), LAYER, "CQL1", "Geometry", "within(the_geom, POLYGON((15.1 60.0, 15.1 60.2, 15.2 60.2, 15.2 60.0, 15.1 60.0)))");
//        geometries = plugin.findGeometriesInLayer( graphDb(), 15.0, 15.3, 60.0, 60.2, "CQL1" );
//        assertTrue( geometries.iterator().hasNext() );

	}
}
