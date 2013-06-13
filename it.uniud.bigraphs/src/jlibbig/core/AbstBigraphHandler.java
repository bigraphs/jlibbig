package jlibbig.core;

import java.util.*;

public interface AbstBigraphHandler extends Owner{

		public abstract Signature getSignature();
		
		public abstract boolean isEmpty();
		
		public abstract boolean isGround();

		public abstract List<? extends Root> getRoots();

		public abstract List<? extends Site> getSites();

		public abstract Set<? extends OuterName> getOuterNames();

		public abstract Set<? extends InnerName> getInnerNames();

		public abstract Set<? extends Node> getNodes();

		public abstract Set<? extends Edge> getEdges();
}
