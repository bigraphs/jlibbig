package jlibbig.udlang;

import java.io.IOException;
import beaver.Parser.Exception;
import jlibbig.udlang.*;
import jlibbig.core.lang.Compiler;

public class BigraphCompiler implements Compiler<BigraphSystem>{
	public BigraphCompiler(){}
	
	private static BigraphParser parser = new BigraphParser();	

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
