package law.webgraph.generator;

import it.unimi.dsi.webgraph.AbstractLazyIntIterator;
import it.unimi.dsi.webgraph.BVGraph;
import it.unimi.dsi.webgraph.GraphClassParser;
import it.unimi.dsi.webgraph.NodeIterator;
import it.unimi.dsi.webgraph.examples.IntegerTriplesArcLabelledImmutableGraph;
import it.unimi.dsi.webgraph.labelling.ArcLabelledImmutableGraph;
import it.unimi.dsi.webgraph.labelling.ArcLabelledImmutableSequentialGraph;
import it.unimi.dsi.webgraph.labelling.ArcLabelledNodeIterator;
import it.unimi.dsi.webgraph.labelling.ArcLabelledNodeIterator.LabelledArcIterator;
import it.unimi.dsi.webgraph.labelling.BitStreamArcLabelledImmutableGraph;
import it.unimi.dsi.webgraph.labelling.GammaCodedIntLabel;
import it.unimi.dsi.webgraph.labelling.Label;
import it.unimi.dsi.fastutil.longs.LongBigArrays;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.law.webgraph.CompressedIntLabel;
import it.unimi.dsi.logging.ProgressLogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.NoSuchElementException;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;
import com.martiansoftware.jsap.SimpleJSAP;
import com.martiansoftware.jsap.UnflaggedOption;

import it.unimi.dsi.webgraph.GraphClassParser;
import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.LazyIntIterator;
import it.unimi.dsi.webgraph.NodeIterator;
import it.unimi.dsi.webgraph.examples.IntegerTriplesArcLabelledImmutableGraph;
import it.unimi.dsi.webgraph.labelling.ArcLabelledImmutableGraph;
import it.unimi.dsi.webgraph.labelling.ArcLabelledNodeIterator;
import it.unimi.dsi.webgraph.labelling.Label;
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

public class EdgeListGenerator {
private EdgeListGenerator() {}
	
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
		
//		final ArcLabelledImmutableGraph graph; 
//		if ( graphClass != null ) graph = (IntegerTriplesArcLabelledImmutableGraph)graphClass.getMethod( "loadOffline", CharSequence.class ).invoke( null, basename );
//		else graph = ArcLabelledImmutableGraph.load( basename ); //ArcLabelledImmutableGraph.loadOffline( basename , pl );
		
		final ImmutableGraph graph;
		// We fetch by reflection the class specified by the user
		if ( graphClass != null ) graph = (ImmutableGraph)graphClass.getMethod( "loadOffline", CharSequence.class ).invoke( null, basename );
		else graph = ImmutableGraph.loadOffline( basename, pl );

		final NodeIterator nodeIterator = graph.nodeIterator();
		long[][] count = LongBigArrays.EMPTY_BIG_ARRAY;
		long d, curr, maxd = 0, maxNode = 0, mind = Integer.MAX_VALUE, minNode = 0;;
		long totd = 0;
		
		System.out.println("Node count: " + graph.numNodes());
		System.out.println("Edge count: " + graph.numArcs());
		
		File edgeListFile = new File(basename + ".edgelist");
		edgeListFile.createNewFile();
		
		File nodeDegreeFile = new File(basename + ".nodedegree");
		nodeDegreeFile.createNewFile();
		
		FileWriter edgeListFileWriter = new FileWriter(edgeListFile);
		FileWriter nodeDegreeFileWriter = new FileWriter(nodeDegreeFile); 
		
		long maxNodeDegree = 0;
		
		final NodeIterator nodeIter = graph.nodeIterator();
//		final ArcLabelledNodeIterator nodeIter = graph.nodeIterator();
		
/*		long tmpNodeCounter = 0;
		//while(nodeIter.hasNext() && tmpNodeCounter++ < 10) {
		while(nodeIter.hasNext()) {			
			long nodeID = nodeIter.nextInt();
			long neighborCount = nodeIter.outdegree();
			LazyIntIterator edgeIter = nodeIter.successors();
//--			ArcLabelledNodeIterator.LabelledArcIterator edgeIter = nodeIter.successors();
			
			//System.out.println(nodeID + "," + neighborCount + ",");
			for (long e = 0; e < neighborCount; e++) {
				//System.out.println(nodeID + "," + edgeIter.nextLong() + "," + neighborCount);
				//edgeListFileWriter.write(nodeID + "," + edgeIter.nextLong() + "," + neighborCount + "\n");
				long sourceLabel = 555;
				long edgeLabel = 555;
				edgeListFileWriter.write(nodeID + "," + edgeIter.nextInt() + "," + sourceLabel + "," + edgeLabel + "\n");
			}
		}*/
		
		System.out.println("Generating edgelist ... ");
		
		// using LazyIterator		
		while(nodeIter.hasNext()) {
			long nodeID = nodeIter.nextInt();
			long neighborCount = nodeIter.outdegree();			
			
			if (neighborCount > maxNodeDegree) {
				maxNodeDegree = neighborCount; 
			}
			
//			ArcLabelledNodeIterator.LabelledArcIterator	edgeIter = graph.successors((int)nodeID); 
			// Exception: Random access to successor lists is not possible with sequential or offline graphs
			
			//ArcLabelledNodeIterator.LabelledArcIterator edgeIter = nodeIter.successors();
			LazyIntIterator edgeIter = nodeIter.successors();
			
//			Label[][] labelArray = graph.labelBigArray(nodeID);
//			Label[] labelArray = graph.labelArray((int)nodeID);
			for (long e = 0; e < neighborCount; e++) {
				long targetID = edgeIter.nextInt();
				long sourceLabel = 0;
//				Label edgeLabel = edgeIter.label();
//				CompressedIntLabel edgeLabel =  (CompressedIntLabel)edgeIter.label();
//				Label edgeLabel = labelArray[(int)e][0];
//				edgeListFileWriter.write(nodeID + "," + targetID + "," + sourceLabel + "," + 
//					edgeLabel.getInt() + "," + labelArray[(int)e].wellKnownAttributeKey() + 
//					"," + labelArray[(int)e].attributeKeys()[0] +
//					"," + labelArray[(int)e].get().toString() + "\n");
				edgeListFileWriter.write(nodeID + " " + targetID + "\n");
				
			}	
			
			nodeDegreeFileWriter.write(nodeID + " " + neighborCount + "\n");
		}
				
		edgeListFileWriter.flush();
		edgeListFileWriter.close();
		
		nodeDegreeFileWriter.flush();
		nodeDegreeFileWriter.close();
		
		System.out.println("Max node degree: " + maxNodeDegree);
		
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
