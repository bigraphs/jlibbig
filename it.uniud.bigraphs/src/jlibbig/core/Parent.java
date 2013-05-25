package jlibbig.core;

import java.util.Set;

public interface Parent {
	Set<? extends Child> getChildren();

}
