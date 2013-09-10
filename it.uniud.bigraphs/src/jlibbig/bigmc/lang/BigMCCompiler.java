package jlibbig.bigmc.lang;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import beaver.Parser.Exception;
import jlibbig.bigmc.*;
import jlibbig.core.Signature;
import jlibbig.core.lang.Compiler;
import jlibbig.core.lang.CompilerException;

/**
 * This class is used to parse strings in bigMC syntax and generate a system
 * (sets of bigraphs and reactions with the same signature) from them.
 * 
 * @see Compiler
 * @see BigraphSystem
 * @see <a href="http://bigraph.org/bigmc/">bigraph.org/bigmc</a>
 * 
 */
public class BigMCCompiler implements Compiler<BigraphSystem> {
	public BigMCCompiler() {
	}

	private static BigMCParser parser = new BigMCParser();

	/**
	 * Return a system, carrying bigraphs and reactions with the same signature.
	 * 
	 * @param str
	 *            the compiler will parse this string.
	 * @return the generated system.
	 * @see BigraphSystem
	 */
	@Override
	public BigraphSystem parse(String str) throws CompilerException {
		return parse(new StringReader(str));
	}

	@Override
	public BigraphSystem parse(Reader in) throws CompilerException {
		try {
			return parser.parse(in);
		} catch (IOException | Exception e) {
			throw new CompilerException(e);
		}
	}

	public BigraphSystem parse(String str, Signature s)
			throws CompilerException {
		return parse(new StringReader(str), s);
	}

	public BigraphSystem parse(Reader in, Signature s) throws CompilerException {
		try {
			return parser.parse(in, s);
		} catch (IOException | Exception e) {
			throw new CompilerException(e);
		}
	}
}
