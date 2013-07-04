package tests;

import java.util.*;

import jlibbig.bigmc.*;
import jlibbig.bigmc.lang.*;
import jlibbig.core.*;

public class ProvaParserBigMc{
	public static void main(String[] args) throws Exception{
		
		BigraphSystem brs = (new BigMCCompiler()).parse( " %active c : 2; %passive d : 1; %name m; %name n; c[x].$0 || d[y].($0 | $2 ) -> c[x].$1 | d[y] || nil ; c[ n , - ] | c[- , n];" );
		System.out.print( (new BigMCPrinter()).toString( brs ) );
		
	}	
}
