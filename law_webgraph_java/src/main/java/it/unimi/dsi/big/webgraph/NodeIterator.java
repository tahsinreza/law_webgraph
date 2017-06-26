package it.unimi.dsi.big.webgraph;

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

import it.unimi.dsi.fastutil.longs.AbstractLongIterator;
import it.unimi.dsi.fastutil.longs.LongBigArrays;
import it.unimi.dsi.fastutil.longs.LongIterator;

/** This interface extends {@link LongIterator} and is used to scan a graph, that is, to read its nodes and their successor lists
 *  sequentially. The {@link #nextLong()} method returns the node that will be scanned. After a call to this method,  calling
 *  {@link #successors()} or {@link #successorBigArray()} will return the list of successors.
 *  
 *  <p>Implementing subclasses can override either {@link #successors()} or 
 *  {@link #successorBigArray()}, but at least one of them <strong>must</strong> be implemented.
 */

public abstract class NodeIterator extends AbstractLongIterator {
	
	/** Returns the outdegree of the current node.
	 *
	 *  @return the outdegree of the current node.
	 */
	public abstract long outdegree();

	/** Returns a lazy iterator over the successors of the current node.  The iteration terminates
	 * when -1 is returned.
	 * 
	 * <P>This implementation just wraps the array returned by {@link #successorBigArray()}.
	 * 
	 *  @return a lazy iterator over the successors of the current node.
	 */
	public LazyLongIterator successors() {
		return LazyLongIterators.wrap( successorBigArray(), outdegree() );
	}

	/** Returns a reference to an array containing the successors of the current node.
	 * 
	 * <P>The returned array may contain more entries than the outdegree of the current node.
	 * However, only those with indices from 0 (inclusive) to the outdegree of the current node (exclusive)
	 * contain valid data.
	 * 
	 * <P>This implementation just unwrap the iterator returned by {@link #successors()}.
	 * 
	 * @return an array whose first elements are the successors of the current node; the array must not
	 * be modified by the caller.
	 */
	public long[][] successorBigArray() {
		final long[][] successor = LongBigArrays.newBigArray( outdegree() );
		LazyLongIterators.unwrap( successors(), successor );
		return successor;
	}
	
	/** Skips the given number of elements.
	 * 
	 * <P>This implementation calls {@link #next()} for
	 * <code>n</code> times (possibly stopping if {@link #hasNext()} becomes false).
	 * 
	 * @param n the number of elements to skip.
	 * @return the number of elements actually skipped.
	 * @see LongIterator#next()
	 */

	public long skip( long n ) {
		long i = n;
		while( i-- != 0 && hasNext() ) nextLong();
		return n - i - 1;
	}
}
