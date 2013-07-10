package jlibbig.udlang;

import java.io.IOException;
import beaver.Parser.Exception;
import jlibbig.udlang.*;
import jlibbig.core.lang.Compiler;

/**
 * This class is used to parse a string representing a bigraph system and generate a system (sets of bigraphs and reactions with the same signature) from it.
 * 
 * @see <a href="../../aux_doc/infoCompiler.html">Language syntax and semantic</a>
 * @see BigraphSystem
 *
 */
public class BigraphCompiler implements Compiler<BigraphSystem>{
	public BigraphCompiler(){}
	
	private static BigraphParser parser = new BigraphParser();	

	/**
	 * Return a system, carrying bigraphs and reactions with the same signature.
	 * @param str the compiler will parse this string.
	 * @return the generated system.
	 * @see BigraphSystem
	 */
	public BigraphSystem parse( String str ){
			
			BigraphSystem sys = null;
		
			try {
				sys = parser.parse( str );
			} catch (IOException | Exception e) {
				e.printStackTrace();
			}
			
			return sys;
		}
}
