package jlibbig.core.lang;

import jlibbig.core.*;

public interface Compiler<T> {
	T parse( String string );
}
