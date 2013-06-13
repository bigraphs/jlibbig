package jlibbig.core;

import java.util.Set;

public interface Parent extends PlaceEntity{
	Set<? extends Child> getChildren();
}
