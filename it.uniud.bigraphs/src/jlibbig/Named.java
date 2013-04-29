package jlibbig;

import java.math.BigInteger;

/**
 * Describes a name identified entity (e.g. edges, nodes, controls).
 */
abstract class Named {
	// the immutable name of the instance
	private final String name;
	
	// internal state for name generation
	private static long _quick_counter = 0; // used to reduce the number of operations on _counter
	private static BigInteger _counter = BigInteger.ZERO; // arbitrary dimension integer counter
	private static String _quick_prefix = ""; // used to avoid frequent string conversion of _counter
	
	/**
	 * Use an automatically generated name.
	 * @see generateName
	 */
	public Named() {
		this(generateName());
	}

	public Named(String name) {
		if (name.isEmpty())
			throw new IllegalArgumentException("Name can not be empty.");
		this.name = name;
	}

	
	/**
	 * @see toString()
	 * @return the textual name
	 */
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	/** Comparison is based on names also for inherited classes.
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		Named other = null;
		try {
			other = (Named) obj;
		} catch (ClassCastException e) {
			return false;
		}
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
	
	/** Generates names unique with respect to the internal shared state of the class.
	 * @param n the number of names to be generated
	 * @return an array of names
	 * @throws IllegalArgumentException for negative integers
	 */
	protected static String[] generateNames(int n) {
		return generateNames("",n);
	}
	
	/** Generates names unique with respect to the internal shared state of the class.
	 * @param prefix a prefix to be used
	 * @param n the number of names to be generated
	 * @return an array of names
	 * @throws IllegalArgumentException for negative integers
	 */
	protected static String[] generateNames(String prefix, int n) {
		if(n < 0) 
			throw new IllegalArgumentException("Can not generate a negative number of names.");
		String[] ns = new String[n];
		synchronized(_counter){
			for(int i = 0; i < n; i++) {
				ns[i] = prefix + unsafeGenerateName();
			}
		}
		return ns;
	}
	
	/** Generate a  name unique with respect to the internal shared state of the class.
	 * @return a name
	 */
	protected static String generateName(){
		String name;
		synchronized(_counter){
			name = unsafeGenerateName();
		}
		return name;
	}

	/** Generates a name using the shared internal state of the class.
	 * This method is not thread safe, use {@link generateName()}}.
	 * @return a name
	 * @deprecated
	 */
	private static String unsafeGenerateName(){
		if(_quick_counter == Long.MAX_VALUE){
			_counter.add(BigInteger.ONE); //.valueOf(_quick_counter));
			_quick_prefix = _counter.toString(16);
			_quick_counter = 0;
		}
		return _quick_prefix + "-" +  _quick_counter++;
	}
	
}
