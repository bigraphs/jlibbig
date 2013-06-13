package tests;

import java.util.Map;

import jlibbig.bigmc.*;
import jlibbig.bigmc.lang.*;
import jlibbig.core.*;

public class ProvaParser{
	public static void main(String[] args) throws Exception{
		
		BigraphSystem brs = (new BigMCParser()).parse( " %active c : 2; %passive d : 1; %name m; %name n; c[x].$0 || d[y].$1 -> c[x].$0 | d[y] || nil ; c[ n , - ] | c[- , n];" );

		System.out.print( BigMCPrinter.toString( brs ) );
		
	}	
}