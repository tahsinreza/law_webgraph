package it.unimi.dsi.big.webgraph;

/*		 
 * Copyright (C) 2013-2015 Sebastiano Vigna 
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

/** A skippable {@linkplain LazyLongIterator lazy iterator over longs}.
 * 
 * <p>An instance of this class represent an iterator over longs
 * that returns elements in increasing order. The iterator makes it possible to {@linkplain #skip(long) skip elements
 * by <em>value</em>}.
 */

public interface LazyLongSkippableIterator extends LazyLongIterator {
	public static final long END_OF_LIST = Long.MAX_VALUE;
	
	/** Skips to a given element.
	 * 
	 * <p>Note that this interface is <em>fragile</em>: after {@link #END_OF_LIST}
	 * has been returned, the behavour of further calls to this method will be
	 * unpredictable. 
	 * 
	 * @param lowerBound a lower bound to the returned element.
	 * @return if the last returned element is greater than or equal to
	 * {@code lowerBound}, the last returned element; otherwise, 
	 * the smallest element greater
	 * than or equal to <code>lowerBound</code> that would be 
	 * returned by this iterator, or {@link #END_OF_LIST}
	 * if no such element exists. 
	 */
	public long skipTo( long lowerBound );
}
