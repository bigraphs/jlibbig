package jlibbig.core;

public class StandardNodeEquivalence implements NodeEquivalence {

	public static final StandardNodeEquivalence DEFAULT = new StandardNodeEquivalence();
	
	@Override
	public boolean areEquiv(Node n1, Node n2) {
		return (n1 != null) && (n2 != null) && n1.getControl().equals(n2.getControl());
	}

}
