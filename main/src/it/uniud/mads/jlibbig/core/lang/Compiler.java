package it.uniud.mads.jlibbig.core.lang;

import java.io.*;

public interface Compiler<T> {
	T parse(String string) throws CompilerException;

	T parse(Reader in) throws CompilerException;
}
