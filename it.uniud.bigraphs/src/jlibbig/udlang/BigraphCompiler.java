package jlibbig.udlang;

import java.io.*;

import beaver.Parser.Exception;
import jlibbig.core.lang.Compiler;
import jlibbig.core.lang.CompilerException;

/**
 * This class is used to parse a string representing a bigraph system and
 * generate a system (sets of bigraphs and reactions with the same signature)
 * from it.
 * 
 * @see <a href="../../aux_doc/infoCompiler.html">Language syntax and
 *      semantic</a>
 * @see BigraphSystem
 * 
 */
public class BigraphCompiler implements Compiler<BigraphSystem> {
	public BigraphCompiler() {
	}

	private static BigraphParser parser = new BigraphParser();

	/**
	 * Return a system, carrying bigraphs and reactions with the same signature.
	 * 
	 * @param str
	 *            the compiler will parse this string.
	 * @return the generated system.
	 * @throws CompilerException 
	 * @see BigraphSystem
	 */
	public BigraphSystem parse(String str) throws CompilerException {
		return parse(new StringReader( str ));
	}
	public BigraphSystem parse(Reader in) throws CompilerException {
		try {
			return parser.parse(in);
		} catch (IOException | Exception e) {
			throw new CompilerException(e);
		}
	}
}
