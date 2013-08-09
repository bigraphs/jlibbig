package tests;

import java.io.*;

import jlibbig.udlang.*;

public class ProvaParser{
	public static void main(String[] args) throws Exception{
		//System.out.println(System.getProperty("user.dir"));
		String file = (args.length > 0) ? args[0] : "src/tests/ProvaParser.txt";
 
		BigraphReactiveSystem brs = (new BRSCompiler()).parse(new FileReader(file));
		System.out.println(brs.toString());
	}	
}
