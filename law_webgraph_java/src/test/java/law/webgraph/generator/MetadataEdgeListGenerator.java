package law.webgraph.generator;

/*		 
 * Copyright (C) 2003-2015 Paolo Boldi and Sebastiano Vigna 
 *
 *  This program is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation; either version 3 of the License, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses/>.
 *
 */


import it.unimi.dsi.big.webgraph.GraphClassParser;
import it.unimi.dsi.big.webgraph.ImmutableGraph;
import it.unimi.dsi.big.webgraph.LazyLongIterator;
import it.unimi.dsi.big.webgraph.NodeIterator;
import it.unimi.dsi.big.webgraph.examples.IntegerTriplesArcLabelledImmutableGraph;
import it.unimi.dsi.big.webgraph.labelling.ArcLabelledImmutableGraph;
import it.unimi.dsi.big.webgraph.labelling.ArcLabelledNodeIterator;
import it.unimi.dsi.big.webgraph.labelling.Label;
import it.unimi.dsi.fastutil.io.TextIO;
import it.unimi.dsi.fastutil.longs.LongBigArrays;
import it.unimi.dsi.law.webgraph.CompressedIntLabel;
import it.unimi.dsi.logging.ProgressLogger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;
import com.martiansoftware.jsap.SimpleJSAP;
import com.martiansoftware.jsap.UnflaggedOption;

/** The main method of this class loads an arbitrary {@link it.unimi.dsi.big.webgraph.ImmutableGraph}
 * and performs a sequential scan to establish the minimum, maximum and average outdegree.
 */

public class MetadataEdgeListGenerator {
	
	private MetadataEdgeListGenerator() {}
	
	static public void main( String arg[] ) throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, JSAPException, IOException {
		SimpleJSAP jsap = new SimpleJSAP( MetadataEdgeListGenerator.class.getName(), "Prints on standard error the maximum, minimum and average degree of a graph, and outputs on standard output the numerosity of each outdegree value (first line is the number of nodes with outdegree 0).",
				new Parameter[] {
						new FlaggedOption( "graphClass", GraphClassParser.getParser(), null, JSAP.NOT_REQUIRED, 'g', "graph-class", "Forces a Java class for the source graph." ),
						new FlaggedOption( "logInterval", JSAP.LONG_PARSER, Long.toString( ProgressLogger.DEFAULT_LOG_INTERVAL ), JSAP.NOT_REQUIRED, 'l', "log-interval", "The minimum time interval between activity logs in milliseconds." ),
						new UnflaggedOption( "basename", JSAP.STRING_PARSER, JSAP.NO_DEFAULT, JSAP.REQUIRED, JSAP.NOT_GREEDY, "The basename of the graph." ),
					}		
				);
		
		JSAPResult jsapResult = jsap.parse( arg );
		if ( jsap.messagePrinted() ) return;

		final Class<?> graphClass = jsapResult.getClass( "graphClass" );
		final String basename = jsapResult.getString( "basename" );

		final ProgressLogger pl = new ProgressLogger();
		pl.logInterval = jsapResult.getLong( "logInterval" );
		
		final ArcLabelledImmutableGraph graph; 
		if ( graphClass != null ) graph = (IntegerTriplesArcLabelledImmutableGraph)graphClass.getMethod( "loadOffline", CharSequence.class ).invoke( null, basename );
		else graph = ArcLabelledImmutableGraph.load( basename ); //ArcLabelledImmutableGraph.loadOffline( basename , pl );
		
//		final ImmutableGraph graph;
		// We fetch by reflection the class specified by the user
//		if ( graphClass != null ) graph = (ImmutableGraph)graphClass.getMethod( "loadOffline", CharSequence.class ).invoke( null, basename );
//		else graph = ImmutableGraph.loadOffline( basename, pl );

		final NodeIterator nodeIterator = graph.nodeIterator();
		long[][] count = LongBigArrays.EMPTY_BIG_ARRAY;
		long d, curr, maxd = 0, maxNode = 0, mind = Integer.MAX_VALUE, minNode = 0;;
		long totd = 0;
		
		System.out.println("Node count: " + graph.numNodes());
		System.out.println("Edge count: " + graph.numArcs());
		
		File edgeListFile = new File(basename + ".edgelist");
		edgeListFile.createNewFile();
		
		FileWriter edgeListFileWriter = new FileWriter(edgeListFile); 
		
//		final NodeIterator nodeIter = graph.nodeIterator();
		final ArcLabelledNodeIterator nodeIter = graph.nodeIterator();
		
/*		long tmpNodeCounter = 0;
		//while(nodeIter.hasNext() && tmpNodeCounter++ < 10) {
		while(nodeIter.hasNext()) {			
			long nodeID = nodeIter.nextLong();
			long neighborCount = nodeIter.outdegree();
//			LazyLongIterator edgeIter = nodeIter.successors();
			ArcLabelledNodeIterator.LabelledArcIterator edgeIter = nodeIter.successors();
			
			//System.out.println(nodeID + "," + neighborCount + ",");
			for (long e = 0; e < neighborCount; e++) {
				//System.out.println(nodeID + "," + edgeIter.nextLong() + "," + neighborCount);
				//edgeListFileWriter.write(nodeID + "," + edgeIter.nextLong() + "," + neighborCount + "\n");
				long sourceLabel = 0;
				long edgeLabel = 0;
				edgeListFileWriter.write(nodeID + "," + edgeIter.nextLong() + "," + sourceLabel + "," + edgeLabel + "\n");
			}
		}*/
		
		// using LazyIterator		
		while(nodeIter.hasNext()) {
			long nodeID = nodeIter.nextLong();
			long neighborCount = nodeIter.outdegree();			
			ArcLabelledNodeIterator.LabelledArcIterator	edgeIter = graph.successors(nodeID); 
			// Exception: Random access to successor lists is not possible with sequential or offline graphs
			
//			ArcLabelledNodeIterator.LabelledArcIterator edgeIter = nodeIter.successors();
			
//			Label[][] labelArray = graph.labelBigArray(nodeID);
			for (long e = 0; e < neighborCount; e++) {
				long targetID = edgeIter.nextLong();
				long sourceLabel = 0;
//				Label edgeLabel = edgeIter.label();
				CompressedIntLabel edgeLabel =  (CompressedIntLabel)edgeIter.label();
//				Label edgeLabel = labelArray[(int)e][0];
				edgeListFileWriter.write(nodeID + "," + targetID + "," + sourceLabel + "," + edgeLabel.toString() + "\n");
			}			
		}
				
		edgeListFileWriter.flush();
		edgeListFileWriter.close();
		
		System.out.println("Edgelist File: " + basename + ".edgelist");
		System.out.println("Done.");
							
		/*pl.expectedUpdates = graph.numNodes();
		pl.start("Scanning...");

		for( long i = graph.numNodes(); i-- != 0; ) {
			curr = nodeIterator.nextLong();
			d = nodeIterator.outdegree();
				
			if ( d < mind ) {
				mind = d;
				minNode = curr;
			}
			
			if ( d > maxd ){
				maxd = d;
				maxNode = curr; 
			}
			
			totd += d;
			
			if ( d >= LongBigArrays.length( count ) ) count = LongBigArrays.grow( count, d + 1 );
			LongBigArrays.incr( count, d );
			
			pl.lightUpdate();
		}
		
		pl.done();
		
		System.err.println( "The minimum outdegree is " + mind + ", attained by node " + minNode );
		System.err.println( "The maximum outdegree is " + maxd + ", attained by node " + maxNode );
		System.err.println( "The average outdegree is " + (double)totd / graph.numNodes() );*/

		//TextIO.storeLongs( count, 0, maxd + 1, System.out );
	}
}
