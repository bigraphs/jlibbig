package jlibbig.udlang;

import java.io.*;

import beaver.Parser.Exception;
import jlibbig.core.Signature;
import jlibbig.core.lang.Compiler;
import jlibbig.core.lang.CompilerException;

/**
 * This class is used to parse a string representing a bigraph system and
 * generate a system (sets of bigraphs and reactions with the same signature)
 * from it.
 * 
 * @see <a href="../../aux_doc/infoCompiler.html">Language syntax and
 *      semantic</a>
 * @see BigraphReactiveSystem
 * 
 */
public class BRSCompiler implements Compiler<BigraphReactiveSystem> {

	private static final BRSParser parser = new BRSParser();
	private static final BRSCompiler instance = new BRSCompiler();
	
	public static BRSCompiler getInstance(){
		return instance;
	}

	/**
	 * Return a system, carrying bigraphs and reactions with the same signature.
	 * 
	 * @param str
	 *            the compiler will parse this string.
	 * @return the generated system.
	 * @throws CompilerException 
	 * @see BigraphReactiveSystem
	 */
    @Override
	public BigraphReactiveSystem parse(String str) throws CompilerException {
		return parse(new StringReader( str ));
	}

    @Override
	public BigraphReactiveSystem parse(Reader in) throws CompilerException {
		try {
			return parser.parse(in);
		} catch (IOException | Exception e) {
			throw new CompilerException(e);
		}
	}
	
	public BigraphReactiveSystem parse(String str , Signature s) throws CompilerException {
		return parse(new StringReader( str ) , s);
	}
	public BigraphReactiveSystem parse(Reader in , Signature s) throws CompilerException {
		try {
			return parser.parse(in , s);
		} catch (IOException | Exception e) {
			throw new CompilerException(e);
		}
	}
	
}
