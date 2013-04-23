package jlibbig;

abstract class Named {
	private final String name;
	private static long _counter = 0;

	public Named() {
		this("" + _counter++);
	}

	public Named(String name) {
		if (name.isEmpty())
			throw new IllegalArgumentException("Name can not be empty.");
		this.name = name;
	}

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

	protected static String[] generateNames(int n) {
		return generateNames("",n);
	}
	
	protected static String[] generateNames(String prefix, int n) {
		if(n < 0) 
			throw new IllegalArgumentException("Can not generate a negative number of names.");
		String[] ns = new String[n];
		for(int i = 0; i < n; i++) {
			ns[i] = prefix + _counter++;
		}
		return ns;
	}
}
