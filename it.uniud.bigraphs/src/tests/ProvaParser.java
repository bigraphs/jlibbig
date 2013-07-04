package tests;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import jlibbig.udlang.*;
import jlibbig.core.*;

public class ProvaParser{
	public static void main(String[] args) throws Exception{
		BufferedReader br = null;
		StringBuilder b = new StringBuilder();
		String nl = System.getProperty("line.separator");
		try {
 
			String sCurrentLine;
 
			br = new BufferedReader(new FileReader( args[0] ));
 
			while ((sCurrentLine = br.readLine()) != null) {
				b.append( sCurrentLine + nl );
			}
 
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		BigraphSystem brs = (new BigraphCompiler()).parse( b.toString() );
		
	}	
}