package jlibbig.bigmc.lang;

import java.io.IOException;
import beaver.Parser.Exception;
import jlibbig.bigmc.*;
import jlibbig.core.lang.Compiler;

public class BigMCCompiler implements Compiler<BigraphSystem>{
	public BigMCCompiler(){}
	
	private static BigMCParser parser = new BigMCParser();	

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
