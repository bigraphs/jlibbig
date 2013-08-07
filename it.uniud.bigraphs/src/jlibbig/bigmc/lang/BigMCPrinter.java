package jlibbig.bigmc.lang;

import java.util.*;

import jlibbig.bigmc.*;
import jlibbig.core.*;
import jlibbig.core.lang.*;

/**
 * BigMC's syntax pretty printer.
 * Convert into Strings all the bigraphs or systems that can be represented with the BigMC's Language
 * @see <a href="http://bigraph.org/bigmc/">bigraph.org/bigmc</a>
 *
 */
public class BigMCPrinter implements PrettyPrinter<BigraphSystem>{
	public BigMCPrinter(){}
	public static final String ln = System.getProperty( "line.separator" );
	
	/**
	 * Convert a BigraphSystem into a string in BigMC's syntax
	 * @param brs the system that will be converted into a string.
	 * @see BigraphSystem
	 * @return the resulting string
	 */
	public String toString( BigraphSystem brs ){
		StringBuilder s = new StringBuilder();
		
		s.append( toString( brs.getSignature() ) );

		s.append( ln );
		
		for( String str : brs.getOuterNames() )
			s.append( "%name " + str + ";" + ln );
		
		s.append( ln );
		
		s.append( toString( brs.getReactions() ) );
		
		s.append( ln );
		
		for( AgentBigraph big : brs.getBigraphs() )
			s.append( toString( big.asBigraph() ) + " ;" + ln );
		
		s.append( ln );
		
		return s.toString();
	}

	/**
	 * Convert a Signature into a string in BigMC's syntax
	 * @param sig 
	 * 			The Signature that will be converted into a string.
	 * @see Signature
	 * @return 
	 * 			The String representing the element in input.
	 */
	public static String toString( Signature sig ){
		StringBuilder s = new StringBuilder();

		for( Control ctrl : sig ){
				s.append( (ctrl.isActive() ? "%active " : "%passive ")
						+ ctrl.getName() + " : " + ctrl.getArity() +";" + ln );
		}

		return s.toString();
	}
	
	/**
	 * Convert a Collection of reaction rules into a string in BigMC's syntax.
	 * @param reaction_rules
	 * 			Collection of Reaction<ReactionBigraph> that will be converted into a string.
	 * @return
	 * 			The String representing the element in input.
	 */
	public static String toString( Collection<Reaction<ReactionBigraph>> reaction_rules ){
		StringBuilder s = new StringBuilder();
		for( Reaction<ReactionBigraph> reaction : reaction_rules ){
			s.append( toString( reaction ) + ";" + ln );
		}
		return s.toString();
	}
	
	/**
	 * Convert a reaction rule into the corresponding String in BigMC's syntax.
	 * @param reaction
	 * @return the resulting string
	 */
	public static String toString( Reaction<ReactionBigraph> reaction ){
		return toString( reaction.getRedex() ) + " -> " + toString( reaction.getReactum() );
	}
		
	/**
	 * Translate a bigraph ( {@link jlibbig.core.AbstBigraphHandler} ) to a string with BigMC's syntax
	 * @param big the bigraph that will be converted into a string.
	 * @return the resulting string
	 */
	public static String toString( AbstBigraphHandler big ){
		StringBuilder s = new StringBuilder();
		
		Iterator<? extends Root> it = big.getRoots().iterator();
		while( it.hasNext() ){
			
			Set<? extends Child> childs = it.next().getChildren();
			if( !childs.isEmpty() ){
				Iterator<? extends Child> childIt = childs.iterator();
				while( childIt.hasNext() ){
					if( big instanceof ReactionBigraph )
						s.append( toString( childIt.next() , big.getOuterNames() , ((ReactionBigraph) big).getSitesMap() ) + (childIt.hasNext() ? " | " : "") );
					else if( big instanceof ReactionBigraphBuilder )
						s.append( toString( childIt.next() , big.getOuterNames() , ((ReactionBigraphBuilder) big).getSitesMap() ) + (childIt.hasNext() ? " | " : "") );
					else
						s.append( toString( childIt.next() , big.getOuterNames() , big.getSites() ) + (childIt.hasNext() ? " | " : "") );
				}
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
	 * @see ReactionBigraph
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
