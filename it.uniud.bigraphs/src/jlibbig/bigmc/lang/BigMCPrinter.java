package jlibbig.bigmc.lang;

import java.util.*;

import jlibbig.bigmc.*;
import jlibbig.core.*;
import jlibbig.core.lang.*;

/**
 * BigMC's syntax pretty printer.
 * Print all the bigraph that can be represented with BigMC's Language
 * @see <a href="http://bigraph.org/bigmc/">bigraph.org/bigmc</a>
 *
 */
public class BigMCPrinter implements PrettyPrinter<BigraphSystem>{
	public BigMCPrinter(){}
	
	/**
	 * Translate a BigraphSystem to a string with BigMC's syntax
	 * @param brs the system that will be printed.
	 * @see BigraphSystem
	 * @return the resulting string
	 */
	public String toString( BigraphSystem brs ){
		String ln = System.getProperty("line.separator");
		StringBuilder s = new StringBuilder();
		
		s.append( toString( brs.getSignature() ) );

		s.append( ln );
		
		for( String str : brs.getOuterNames() )
			s.append( "%name " + str + ";" + ln );
		
		s.append( ln );
		
		for(Map.Entry<RedexBigraph, RedexBigraph> reaction : brs.getReactions().entrySet() )
			s.append( toString( reaction.getKey() ) + " -> " + toString( reaction.getValue() ) + " ;" + ln );
		
		s.append( ln );
		
		for( Bigraph big : brs.getBigraphs() )
			s.append( toString( big ) + " ;" + ln );
		
		s.append( ln );
		
		return s.toString();
	}

	/**
	 * Translate a Signature to a string with BigMC's syntax
	 * @param sig the signature that will be printed.
	 * @see Signature
	 * @return the resulting string
	 */
	public String toString( Signature sig ){
		String ln = System.getProperty("line.separator");
		StringBuilder s = new StringBuilder();

		for( Control ctrl : sig ){
				s.append( (ctrl.isActive() ? "%active " : "%passive ")
						+ ctrl.getName() + " : " + ctrl.getArity() +";" + ln );
		}

		return s.toString();
	}
	
	/**
	 * Translate a RedexBigraph to a string with BigMC's syntax
	 * @param big the redex bigraph that will be printed.
	 * @see RedexBigraph
	 * @return the resulting string
	 */
	public static String toString( RedexBigraph big ){
		StringBuilder s = new StringBuilder();
		Iterator<? extends Root> it = big.getRoots().iterator();
		while( it.hasNext() ){
			
			Set<? extends Child> childs = it.next().getChildren();
			if( !childs.isEmpty() ){
				Iterator<? extends Child> childIt = childs.iterator();
				while( childIt.hasNext() )
					s.append( toString( childIt.next() , big.getOuterNames() , big.getSitesMap() ) + (childIt.hasNext() ? " | " : "") );
			}else s.append( "nil" );
			s.append( it.hasNext() ? " || " : "" );
		}
		return s.toString();
	}
	
	/**
	 * Translate a Bigraph to a string with BigMC's syntax
	 * @param big the bigraph that will be printed.
	 * @see Bigraph
	 * @return the resulting string
	 */
	public static String toString( Bigraph big ){
		StringBuilder s = new StringBuilder();
		
		Iterator<? extends Root> it = big.getRoots().iterator();
		while( it.hasNext() ){
			
			Set<? extends Child> childs = it.next().getChildren();
			if( !childs.isEmpty() ){
				Iterator<? extends Child> childIt = childs.iterator();
				while( childIt.hasNext() )
					s.append( toString( childIt.next() , big.getOuterNames() , big.getSites() ) + (childIt.hasNext() ? " | " : "") );
			}else s.append( "nil" );
			s.append( it.hasNext() ? " || " : "" );
		}
		return s.toString();
	}
	
	/**
	 * Auxiliary procedure, used by toString( RedexBigraph ) 
	 * @param d control or site handler
	 * @param outrnms set of outernames
	 * @param sitenum sites' enumeration, used to retrieve the right number of the site
	 * @see RedexBigraph
	 * @return the resulting string
	 */
	private static String toString( Child d , Set<? extends OuterName> outrnms , Map<Site , Integer> sitenum ){
		StringBuilder s = new StringBuilder();
		
		if( d instanceof Site ){
			s.append( "$" + sitenum.get( d ) );
		}else{
			s.append( ((Node) d).getControl().getName() );
			
			StringBuilder ns = new StringBuilder();
			int unlinked = 0;
			
			Iterator<? extends Port> portIt = ((Node) d).getPorts().iterator();
			while( portIt.hasNext() ){
				Handle handle = portIt.next().getHandle();
				if( outrnms.contains( handle ) ){
					for( int i = 0; i < unlinked; ++i )
						ns.append( " - , " );
					ns.append( " " + ((OuterName) handle ).getName() + " , " );
				}else
					++unlinked;
			}
			
			if(ns.length() > 0 )	//note: if length > 0 --> at least 1 append has been done --> length > 3
				s.append( "[" + ns.substring(0 , ns.length()-2 ) + "]" );
			
			Set<? extends Child> childs = ((Node) d ).getChildren();
			if(! childs.isEmpty() ){
				s.append(".");
				if( childs.size() > 1 ) s.append("( ");
				
				Iterator<? extends Child> childIt = childs.iterator();
				while( childIt.hasNext() )
					s.append( toString( childIt.next() , outrnms , sitenum ) + (childIt.hasNext() ? " | " : "") );
				
				if( childs.size() > 1 ) s.append(" )");
			}
		}
		
		return s.toString();
	}
	
	/**
	 * Auxiliary procedure, used by toString( Bigraph ) 
	 * @param c control or site handler
	 * @param outerNames set of outernames
	 * @param sites list of sites
	 * @see Bigraph
	 * @return the resulting string
	 */
	private static String toString( Child c , Set<? extends OuterName> outerNames , List<? extends Site> sites ){
		StringBuilder s = new StringBuilder();
		
		if( c instanceof Site ){
			s.append( "$" + sites.indexOf(c) );
		}else{
			s.append( ((Node) c).getControl().getName() );
			
			StringBuilder ns = new StringBuilder();
			int unlinked = 0;
			
			Iterator<? extends Port> portIt = ((Node) c).getPorts().iterator();
			while( portIt.hasNext() ){
				Handle handle = portIt.next().getHandle();
				if( outerNames.contains( handle ) ){
					for( int i = 0; i < unlinked; ++i )
						ns.append( " - , " );
					ns.append( " " + ((OuterName) handle ).getName() + " , " );
				}else
					++unlinked;
			}
			
			if(ns.length() > 0 )	//note: if length > 0 --> at least 1 append has been done --> length > 3
				s.append( "[" + ns.substring(0 , ns.length()-2 ) + "]" );
			
			Set<? extends Child> childs = ((Node) c ).getChildren();
			if(! childs.isEmpty() ){
				s.append(".");
				if( childs.size() > 1 ) s.append("( ");
				
				Iterator<? extends Child> childIt = childs.iterator();
				while( childIt.hasNext() )
					s.append( toString( childIt.next() , outerNames , sites ) + (childIt.hasNext() ? " | " : "") );
				
				if( childs.size() > 1 ) s.append(" )");
			}
		}
		
		return s.toString();
	}
	
}
