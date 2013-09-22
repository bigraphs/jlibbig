package it.uniud.mads.jlibbig.bigmc.lang;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import beaver.Parser.Exception;
import it.uniud.mads.jlibbig.bigmc.*;
import it.uniud.mads.jlibbig.core.lang.Compiler;
import it.uniud.mads.jlibbig.core.lang.CompilerException;
import it.uniud.mads.jlibbig.core.std.Signature;

/**
 * This class is used to parse strings in bigMC syntax and generate a system
 * (sets of bigraphs and reactions with the same signature) from them.
 * 
 * @see Compiler
 * @see BigraphRewritingSystem
 * @see <a href="http://bigraph.org/bigmc/">bigraph.org/bigmc</a>
 * 
 */
public class BigMCCompiler implements Compiler<BigraphRewritingSystem> {
	public BigMCCompiler() {
	}

	private static BigMCParser parser = new BigMCParser();

	/**
	 * Return a system, carrying bigraphs and reactions with the same signature.
	 * 
	 * @param str
	 *            the compiler will parse this string.
	 * @return the generated system.
	 * @see BigraphRewritingSystem
	 */
	@Override
	public BigraphRewritingSystem parse(String str) throws CompilerException {
		return parse(new StringReader(str));
	}

	@Override
	public BigraphRewritingSystem parse(Reader in) throws CompilerException {
		try {
			return parser.parse(in);
		} catch (IOException | Exception e) {
			throw new CompilerException(e);
		}
	}

	public BigraphRewritingSystem parse(String str, Signature s)
			throws CompilerException {
		return parse(new StringReader(str), s);
	}

	public BigraphRewritingSystem parse(Reader in, Signature s) throws CompilerException {
		try {
			return parser.parse(in, s);
		} catch (IOException | Exception e) {
			throw new CompilerException(e);
		}
	}
}
