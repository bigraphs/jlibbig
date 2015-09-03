/**
 * 
 */
package it.uniud.mads.jlibbig.core.util;

import java.math.BigInteger;
import java.util.*;

public class NameHelper {

	// internal state for name generation
	// private static long _quick_counter = 0; // used to reduce the number of
	// operations on _counter
	private static BigInteger _counter = BigInteger.ZERO; // arbitrary dimension
															// integer counter

	// private static String _quick_prefix = _counter.toString(16); // used to
	// avoid frequent string conversion of _counter
	
	/**
	 * Generates names unique with respect to the internal shared state of the
	 * class.
	 * 
	 * @param n
	 *            the number of names to be generated
	 * @return an array of names
	 * @throws IllegalArgumentException
	 *             for negative integers
	 */
	protected static String[] generateNames(int n) {
		return generateNames("", n);
	}

	/**
	 * Generates names unique with respect to the internal shared state of the
	 * class.
	 * 
	 * @param prefix
	 *            a prefix to be used
	 * @param n
	 *            the number of names to be generated
	 * @return an array of names
	 * @throws IllegalArgumentException
	 *             for negative integers
	 */
	public static String[] generateNames(String prefix, int n) {
		return generateNames(prefix, n, new LinkedList<String>());
	}

	public static String[] generateNames(String prefix, int n,
			List<String> filter) {
		if (n < 0)
			throw new IllegalArgumentException(
					"Can not generate a negative number of names.");
		String[] ns = new String[n];
		synchronized (_counter) {
			int i = 0;
			while (i < n) {
				String name = prefix + unsafeGenerateName();
				if (!filter.contains(name)) {
					ns[i++] = name;
				}
			}
		}
		return ns;
	}

	/**
	 * Generate a name unique with respect to the internal shared state of the
	 * class.
	 * 
	 * @return a name
	 */
	public static String generateName() {
		String name;
		synchronized (_counter) {
			name = unsafeGenerateName();
		}
		return name;
	}

	/**
	 * Generates a name using the shared internal state of the class. This
	 * method is not thread safe, use {@link #generateName()} .
	 * 
	 * @return a name
	 */
	private static String unsafeGenerateName() {
		/*
		 * if(_quick_counter == Long.MAX_VALUE){ _counter =
		 * _counter.add(BigInteger.ONE); //.valueOf(_quick_counter));
		 * _quick_prefix = _counter.toString(16); _quick_counter = 0; } return
		 * _quick_prefix + "-" + _quick_counter++;
		 */
		String r = _counter.toString(16).toUpperCase();
		_counter = _counter.add(BigInteger.ONE);
		return r;
	}
}
