package jlibbig;

import java.util.*;

public interface Node extends Parent, Child {
	public List<Port> getPorts();

	public Port getPort(int index);

	public BigraphControl getControl();

	public interface Port extends Point {

		public Node getNode();

		public int getNumber();
	}
}
