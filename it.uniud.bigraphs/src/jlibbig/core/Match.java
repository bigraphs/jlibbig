package jlibbig.core;

import java.util.*;

public interface Match<A extends AbstBigraph> {
	A getContext();
	A getRedex();
	List<A> getParams();
}
