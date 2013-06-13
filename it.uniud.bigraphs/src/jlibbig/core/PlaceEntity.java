package jlibbig.core;

interface PlaceEntity {
	boolean isParent();
	boolean isChild();
	boolean isRoot();
	boolean isSite();
	boolean isNode();
}