package jlibbig.bigmc.lang;

import java.io.IOException;
import beaver.Parser.Exception;
import jlibbig.bigmc.*;
import jlibbig.core.lang.Compiler;

/**
 * This class is used to parse strings in bigMC syntax and generate a system (sets of bigraphs and reactions with the same signature) from them.
 * @see Compiler
 * @see BigraphSystem
 * @see <a href="http://bigraph.org/bigmc/">bigraph.org/bigmc</a>
 *
 */
public class BigMCCompiler implements Compiler<BigraphSystem>{
	public BigMCCompiler(){}
	
	private static BigMCParser parser = new BigMCParser();	

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
