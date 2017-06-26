package it.unimi.dsi.big.webgraph;

/*		 
 * Copyright (C) 2007-2015 Sebastiano Vigna 
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


import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.longs.AbstractLongIterator;
import it.unimi.dsi.fastutil.longs.LongBigArrays;
import it.unimi.dsi.fastutil.longs.LongIterator;

import java.util.NoSuchElementException;

/** A class providing static methods and objects that do useful 
 * things with {@linkplain LazyLongIterator lazy integer iterators}. */

public class LazyLongIterators {

	protected LazyLongIterators() {}
	
	/** An empty lazy iterator. */
	public final static LazyLongIterator EMPTY_ITERATOR = new LazyLongIterator() {
		public long nextLong() { return -1; }
		public long skip( final long n ) { return 0; }
	};

	/** Unwraps the elements returned by a lazy iterator into an array. 
	 * 
	 * @param lazyLongIterator a lazy long iterator.
	 * @param array an array.
	 * @return the number of elements unwrapped into <code>array</code> starting from index 0.
	 */
	public static int unwrap( final LazyLongIterator lazyLongIterator, final long array[] ) {
		int j;
		long t;
		final int l = array.length;
		for( j = 0; j < l && ( t = lazyLongIterator.nextLong() ) != -1; j++ ) array[ j ] = t;
		return j;
	}

	/** Unwraps the elements returned by a lazy iterator into an array fragment.
	 * 
	 * @param lazyLongIterator a lazy long iterator.
	 * @param array an array.
	 * @param offset the index of the first element ot <code>array</code> to be used.
	 * @param length the maximum number of elements to be unwrapped.
	 * @return the number of elements unwrapped into <code>array</code> starting from index <code>offset</code>. 
	 */
	public static int unwrap( final LazyLongIterator lazyLongIterator, final long array[], final int offset, final int length ) {
		int j;
		long t;
		final int l = Math.min( length, array.length - offset );
		for( j = 0; j < l && ( t = lazyLongIterator.nextLong() ) != -1; j++ ) array[ offset + j ] = t;
		return j;
	}

	/** Unwraps the elements returned by a lazy iterator into a big array. 
	 * 
	 * @param lazyLongIterator a lazy long iterator.
	 * @param array an array.
	 * @return the number of elements unwrapped into <code>array</code> starting from index 0.
	 */
	public static long unwrap( final LazyLongIterator lazyLongIterator, final long array[][] ) {
		long j, t;
		final long l = LongBigArrays.length( array );
		for( j = 0; j < l && ( t = lazyLongIterator.nextLong() ) != -1; j++ ) LongBigArrays.set( array, j, t );
		return j;
	}

	/** Unwraps the elements returned by a lazy iterator into a big array fragment.
	 * 
	 * @param lazyLongIterator a lazy long iterator.
	 * @param array an array.
	 * @param offset the index of the first element ot <code>array</code> to be used.
	 * @param length the maximum number of elements to be unwrapped.
	 * @return the number of elements unwrapped into <code>array</code> starting from index <code>offset</code>. 
	 */
	public static long unwrap( final LazyLongIterator lazyLongIterator, final long array[][], final long offset, final long length ) {
		long j, t;
		final long l = Math.min( length, LongBigArrays.length( array ) - offset );
		for( j = 0; j < l && ( t = lazyLongIterator.nextLong() ) != -1; j++ ) LongBigArrays.set( array, offset + j, t );
		return j;
	}

	/** Unwraps the elements returned by a lazy iterator into a new array.
	 * 
	 * <p>If you need the resulting array to contain the
	 * elements returned by <code>lazyIntIterator</code>, but some more elements set to zero
	 * would cause no harm, consider using {@link #unwrapLoosely(LazyLongIterator)}, which
	 * usually avoids a final call to {@link IntArrays#trim(int[], int)}.
	 * 
	 * @param lazyLongIterator a lazy long iterator.
	 * @return an array containing the elements returned by <code>lazyIntIterator</code>.
	 * @see #unwrapLoosely(LazyLongIterator) 
	 */
	public static long[][] unwrap( final LazyLongIterator lazyLongIterator ) {
		long array[][] = LongBigArrays.newBigArray( 16 );
		int j = 0;
		long t;

		while( ( t = lazyLongIterator.nextLong() ) != -1  ) {
			if ( j == LongBigArrays.length( array ) ) array = LongBigArrays.grow( array, j + 1 );
			LongBigArrays.set( array, j++, t );
		}
        
		return LongBigArrays.trim( array, j );
	}
	
	/** Unwraps the elements returned by a lazy iterator into a new array that can contain additional entries set to zero.
	 * 
	 * <p>If you need the resulting array to contain <em>exactly</em> the
	 * elements returned by <code>lazyIntIterator</code>, consider using {@link #unwrap(LazyLongIterator)}, but this 
	 * method avoids a final call to {@link IntArrays#trim(int[], int)}.
	 * 
	 * @param lazyLongIterator a lazy long iterator.
	 * @return an array containing the elements returned by <code>lazyIntIterator</code>; note
	 * that in general it might contains some final zeroes beyond the elements returned by <code>lazyIntIterator</code>,
	 * so the number of elements actually written into <code>array</code> must be known externally.
	 * @see #unwrap(LazyLongIterator) 
	 */
	public static long[][] unwrapLoosely( final LazyLongIterator lazyLongIterator ) {
		long array[][] = LongBigArrays.newBigArray( 16 );
		int j = 0;
		long t;

		while( ( t = lazyLongIterator.nextLong() ) != -1  ) {
			if ( j == LongBigArrays.length( array ) ) array = LongBigArrays.grow( array, j + 1 );
			LongBigArrays.set( array, j++, t );
		}
        
		return array;
	}
	
	/** A lazy iterator returning the elements of a given array. */

	private static final class ArrayLazyLongIterator implements LazyLongIterator {
		/** The underlying array. */
		private final long[] a;
		/** The number of valid elements in {@link #a}, starting from 0. */
		private final int length;
		/** The next element of {@link #a} that will be returned. */
		private int pos;

		public ArrayLazyLongIterator( final long a[], final int length ) {
			this.a = a;
			this.length = length;
		}
			
		public long nextLong() {
			if ( pos == length ) return -1;
			return a[ pos++ ];
		}

		public long skip( final long n ) {
			final long toSkip = Math.min( n, length - pos );
			pos += toSkip;
			return toSkip;
		}
	}

	/** A lazy iterator returning the elements of a given big array. */

	private static final class BigArrayLazyLongIterator implements LazyLongIterator {
		/** The underlying array. */
		private final long[][] a;
		/** The number of valid elements in {@link #a}, starting from 0. */
		private final long length;
		/** The next element of {@link #a} that will be returned. */
		private long pos;

		public BigArrayLazyLongIterator( final long a[][], final long length ) {
			this.a = a;
			this.length = length;
		}
			
		public long nextLong() {
			if ( pos == length ) return -1;
			return LongBigArrays.get( a, pos++ );
		}

		public long skip( final long n ) {
			final long toSkip = Math.min( n, length - pos );
			pos += toSkip;
			return toSkip;
		}
	}

	/** Returns a lazy long iterator enumerating the given number of elements of an array.
	 * 
	 * @param array an array.
	 * @param length the number of elements to enumerate.
	 * @return a lazy integer iterator enumerating the first <code>length</code> elements of <code>array</code>.
	 */
	
	public static LazyLongIterator wrap( final long array[], final int length ) {
		if ( length == 0 ) return EMPTY_ITERATOR;
		return new ArrayLazyLongIterator( array, length );
	}
	
	/** Returns a lazy long iterator enumerating the given number of elements of a big array.
	 * 
	 * @param array an array.
	 * @param length the number of elements to enumerate.
	 * @return a lazy integer iterator enumerating the first <code>length</code> elements of <code>array</code>.
	 */
	
	public static LazyLongIterator wrap( final long array[][], final long length ) {
		if ( length == 0 ) return EMPTY_ITERATOR;
		return new BigArrayLazyLongIterator( array, length );
	}
	
	/** Returns a lazy integer iterator enumerating the elements of an array.
	 * 
	 * @param array an array.
	 * @return a lazy integer iterator enumerating the elements of <code>array</code>.
	 */

	public static LazyLongIterator wrap( final long array[] ) {
		return wrap( array, array.length );
	}
	
	/** Returns a lazy integer iterator enumerating the elements of a big array.
	 * 
	 * @param array a big array.
	 * @return a lazy integer iterator enumerating the elements of <code>array</code>.
	 */

	public static LazyLongIterator wrap( final long array[][] ) {
		return wrap( array, LongBigArrays.length( array ) );
	}
	
	/** An adapter from lazy to eager iteration. */
	private static final class LazyToEagerLongIterator extends AbstractLongIterator {
		/** The underlying lazy iterator. */
		private final LazyLongIterator lazyLongIterator;
		/** Whether this iterator has been already advanced, that is, whether {@link #next} is valid. */
		private boolean advanced;
		/** The next value to be returned, if {@link #advanced} is true. */
		private long next;

		public LazyToEagerLongIterator( final LazyLongIterator lazyLongIterator ) {
			this.lazyLongIterator = lazyLongIterator;
		}

		public boolean hasNext() {
			if ( ! advanced ) {
				advanced = true;
				next = lazyLongIterator.nextLong();
			}
			return next != -1;
		}

		public long nextLong() {
			if ( ! hasNext() ) throw new NoSuchElementException();
			advanced = false;
			return next;
		}
		
		public int skip( final int n ) {
			if ( n == 0 ) return 0;
			final int increment = advanced ? 1 : 0;
			advanced = false;
			return (int)( lazyLongIterator.skip( n - increment ) + increment );
		}
	}

	/** Returns an eager {@link IntIterator} enumerating the same elements of
	 * a given lazy integer iterator.
	 * 
	 * @param lazyLongIterator a lazy integer iterator.
	 * @return an eager {@link LongIterator} enumerating the same elements of
	 * <code>lazyLongIterator</code>.
	 */
	
	public static LongIterator eager( final LazyLongIterator lazyLongIterator ) {
		return new LazyToEagerLongIterator( lazyLongIterator );
	}
	

	private static final class EagerToLazyLongIterator implements LazyLongIterator {
		private final LongIterator underlying;
		

		public EagerToLazyLongIterator( final LongIterator underlying ) {
			this.underlying = underlying;
		}

		public long nextLong() {
			return underlying.hasNext() ? underlying.nextLong() : -1; 
		}

		public long skip( long n ) {
			long t = 0;
			int actual;
			
			while( n > 0 ) {
				t += underlying.skip( actual = (int)Math.min( n, 1 << 30 ) );
				n -= actual;
			}
			
			return t;
		}

	}

	/** Returns a {@link LazyLongIterator} enumerating the same elements of
	 * a given eager integer iterator.
	 * 
	 * @param eagerLongIterator an eager integer iterator.
	 * @return a lazy integer iterator enumerating the same elements of
	 * <code>eagerIntIterator</code>.
	 */
	
	public static LazyLongIterator lazy( final LongIterator eagerLongIterator ) {
		return new EagerToLazyLongIterator( eagerLongIterator ); 
	}
}
