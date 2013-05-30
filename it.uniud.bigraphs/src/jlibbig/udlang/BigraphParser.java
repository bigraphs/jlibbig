package jlibbig.udlang;

import java.util.ArrayList;
import java.io.*;
import beaver.*;
import java.util.*;
import jlibbig.core.*;

/**
 * This class is a LALR parser generated by
 * <a href="http://beaver.sourceforge.net">Beaver</a> v0.9.6.1
 * from the grammar specification "parser.grammar".
 */
public class BigraphParser extends Parser {
	static public class Terminals {
		static public final short EOF = 0;
		static public final short VARID = 1;
		static public final short INNERPLACE = 2;
		static public final short PAROPEN = 3;
		static public final short NIL = 4;
		static public final short UNLINKED = 5;
		static public final short LINKED = 6;
		static public final short SEMICOLON = 7;
		static public final short COLON = 8;
		static public final short CTRL = 9;
		static public final short NUM = 10;
		static public final short OUTERNAME = 11;
		static public final short INNERNAME = 12;
		static public final short DPIPE = 13;
		static public final short PIPE = 14;
		static public final short SQCLOSE = 15;
		static public final short INNER = 16;
		static public final short OUTER = 17;
		static public final short EDGE = 18;
		static public final short REACT = 19;
		static public final short PARCLOSE = 20;
		static public final short SQOPEN = 21;
		static public final short POINT = 22;
		static public final short COMMA = 23;
	}

	static final ParsingTables PARSING_TABLES = new ParsingTables(
		"U9o5b4bC544KXl$kdc46H41m571aaHb1L1HSK7753INnREB1enTlNhmOIOmnKIzoyUhLDMv" +
		"HuvQu6nNZJSKLLzntnVNvLqqDAPeUcCvNxzNxNtNLLDLh0BVHY18CnnWKe07v68ei4Sn5IF" +
		"vY65ovqrz74po40iRzGPZZVc8MuYX76Uem3ICHmpn44K6EJvba$06LVhFazStrZKnd5p6xC" +
		"52PIYKeFXiDqeaXcDmxUmb6C3uU4p3HYXKpLe9HkgrWg$vVBQhrKuTAar#DiHX74jA5wTIp" +
		"wTTIdwdRNFcDElabFTpX64fHXT58eWRrd7q6fc2E$CC2pCT2B48p5cC9Mh0SIx6CVZ7dBUN" +
		"ySOufuveg0AwYYcs4SrG7w4LQpppEWQlFCyoJvVvohGxt8TUdfFs1bQ1PxDo#lRxPcPHCSJ" +
		"Lpj5TDnGkueuM#cDsg$#VmBUe9yznJ#SKyD$KaUCCIr2Fq7Ue9tKQuwZ2WTpYFToB6MvdFS" +
		"rUpgnrJUbIFIb9HkvT3JztuR2gjRNeeJnUyRT2nPg9sCKdgoLwJivaSDlvASfAS9chVLfCp" +
		"vBW#Dw39vBMGGsGFsM9Y9vGr$WxIRlnzP1SvQkdxoK7Zxqp7BVqikMZyKsIjyOyOUuuSC$v" +
		"6ifwSD$rhv3gvG2wHo#IAqMwH6#GgkKawqdEAI3VfCNaX$ve2rjZeww$mzTSPky6AjGMyPv" +
		"EllpKWPzjWwx5oLncxtOgrM$wQGSRlpX0$O6mh#ltagsw$#AAVo1loMhhPlYMlvALMNiWpR" +
		"P$BOtb4#r0wvO7SbtjoLvwo$qG#ilqWNVAUjalUgIYzpzRRlyiFo$z6exw6Bd5uLrtYy4lW" +
		"4eTr3kBnMvMbxt#IjefzHJbhDiwS51vht2KUAzV69Ox1OrshEdTOsmgFDGjTjvKQZxNjMZY" +
		"Qclyc9U7X");

	static final Action RETURN2 = new Action() {
		public Symbol reduce(Symbol[] _symbols, int offset) {
			return _symbols[offset + 2];
		}
	};
		
		private Set<String> _outerNames;	//set of %outer
		private Set<String> _innerNames;	//set of %outer
		private ExtendedBRS _brs;
		

		/* Parse a string, creating a BRS and a set of Bigraphs from it.
		 * It use an extension of BigMC Language. 
		 * @param str
		 * 		string, in extended-BigMC syntax.
		 * @return BigraphParser.ExtendedBRS
		 *		class that contains a signature, a set of bigraphs and a map containing all reaction rules.
		 */
		public ExtendedBRS parse( String str ) throws IOException, Parser.Exception{
			_outerNames = new HashSet<>();
			_innerNames = new HashSet<>();			
			_brs = new ExtendedBRS();

			BigraphLexer input = new BigraphLexer( new StringReader( str ) );
			parse(input);

			return _brs;
		}

		private class ParsedBigraph extends BigraphBuilder{
			
			private Map<String , OuterName> edgesNames;
			private Map<String , OuterName> outersNames;
			private Set<String> innersNames;
			private Set<Integer> sitesNames;

			ParsedBigraph( Signature sig ){
				super( sig );
				edgesNames = new HashMap<>();
				outersNames = new HashMap<>();
				innersNames = new HashSet<>();
				sitesNames = new HashSet<>();
			}
			
			//Note:	this method doesn't return a Milner's Ion (for bigraphs). It will add to the current bigraph one root with a node.
			//	This node have a site inside and, differently from Milner's definition of Ion, it can be linked to inner names.
			public void makeIon( String c , List<Name<String , Character>> list ){
				if( getSignature().getByName( c ) == null )
					throw new IllegalArgumentException( "Control " + c +" should be in the signature." );
				
				Node node = addNode( c , addRoot() );
				addSite( node );

				if( list == null ) return;

				List<? extends Port> ports = node.getPorts();

				if( ports.size() < list.size() )
					throw new IllegalArgumentException( "Control " + c +" have " + ports.size() + " ports, " + list.size() + " ports were found." );

				Iterator<? extends Port> portIt = ports.iterator();
				for( Name<String , Character> name : list ){
					switch ( name.getType() ){
           					case 'i':
							if( innersNames.contains( name.getName() ) )
								throw new RuntimeException( "Innernames (" + name.getName() + ") can't appear multiple time in a single bigraph." );
							Handle inner_edge = ((Port) portIt.next()).getHandle();
							addInnerName( name.getName() , inner_edge );
							innersNames.add( name.getName() ); 
                     					break;
						case 'o':
							OuterName outer = outersNames.get( name.getName() );
							if( outer == null )
								outersNames.put( name.getName() , outer = addOuterName() );
							relink( (Point) portIt.next() , outer );
							break;
						case 'e':
							OuterName edge = edgesNames.get( name.getName() );
							if( edge == null )
								edgesNames.put( name.getName() , edge = addOuterName() );
							relink( (Point) portIt.next() , edge );
							break;
						case 'u':
							portIt.next();
							break;
					}
				}
			}

			/* Compose two ParsedBigraph. 
			 * @param pb
			 * 		ParsedBigraph to be innerComposed
			 */
			public void compose( ParsedBigraph pb ){
				//preconditions: this.getRoots().size() == 1
				if( pb.getRoots().size() != 1 )
					throw new RuntimeException( "The double-parallel operator (||) can only appear at the top level" );
				
				if( !Collections.disjoint( this.innersNames , pb.innersNames ) )
					throw new RuntimeException( "Innernames can't appear multiple time in a single bigraph." );

				BigraphBuilder inner_juxt = new BigraphBuilder( getSignature() );
				BigraphBuilder outer_juxt = new BigraphBuilder( getSignature() );
				BigraphBuilder outer_comp = new BigraphBuilder( getSignature() );

				for( String str : this.innersNames )
					inner_juxt.addInnerName( str , inner_juxt.addOuterName( str ) );
								
				for( Map.Entry<String , OuterName> o : pb.outersNames.entrySet() ){
					outer_juxt.addInnerName( o.getValue().getName() , outer_juxt.addOuterName( o.getValue().getName() ) );
					if( this.outersNames.containsKey( o.getKey() ) ){
						OuterName outer = outer_comp.addOuterName();
						outer_comp.addInnerName( o.getValue().getName() , outer );
						outer_comp.addInnerName( this.outersNames.put( o.getKey() , outer ).getName() , outer );
					}else
						outer_comp.addInnerName( o.getValue().getName() , outer_comp.addOuterName( o.getValue().getName() ) );
				}

				for( Map.Entry<String , OuterName> o : this.outersNames.entrySet() ){
					if( !pb.outersNames.containsKey( o.getKey() ) )
						outer_comp.addInnerName( o.getValue().getName() , outer_comp.addOuterName( o.getValue().getName() ) );
				}

				for( Map.Entry<String , OuterName> e : pb.edgesNames.entrySet() ){
					outer_juxt.addInnerName( e.getValue().getName() , outer_juxt.addOuterName( e.getValue().getName() ) );
					if( this.edgesNames.containsKey( e.getKey() ) ){
						OuterName edge = outer_comp.addOuterName();
						outer_comp.addInnerName( e.getValue().getName() , edge );
						outer_comp.addInnerName( this.edgesNames.put( e.getKey() , edge ).getName() , edge );
					}else
						outer_comp.addInnerName( e.getValue().getName() , outer_comp.addOuterName( e.getValue().getName() ) );
				}

				for( Map.Entry<String , OuterName> e : this.edgesNames.entrySet() ){
					if( !pb.edgesNames.containsKey( e.getKey() ) )
						outer_comp.addInnerName( e.getValue().getName() , outer_comp.addOuterName( e.getValue().getName() ) );
				}
				
				outer_comp.addSite( outer_comp.addRoot() );
				
				pb.rightJuxtapose( inner_juxt.makeBigraph() , true );
				this.leftJuxtapose( outer_juxt.makeBigraph() , true );
				this.innerCompose( pb.makeBigraph() , true );
				this.outerCompose( outer_comp.makeBigraph() , true );

				this.innersNames.addAll( pb.innersNames );
				this.sitesNames.addAll( pb.sitesNames );

			}

			/* Juxtapose two ParsedBigraph. 
			 * @param pb
			 */
			public void juxtapose( ParsedBigraph pb ){

				if( !Collections.disjoint( this.sitesNames , pb.sitesNames ) )
					throw new RuntimeException( "The same site ($num) can't appear multiple time in a single bigraph." );

				if( !Collections.disjoint( this.innersNames , pb.innersNames ) )
					throw new RuntimeException( "Innernames can't appear multiple time in a single bigraph." );
				
				BigraphBuilder outer_comp = new BigraphBuilder( getSignature() );
				
				for( Map.Entry<String , OuterName> o : pb.outersNames.entrySet() ){
					if( this.outersNames.containsKey( o.getKey() ) ){
						OuterName outer = outer_comp.addOuterName();
						outer_comp.addInnerName( o.getValue().getName() , outer );
						outer_comp.addInnerName( this.outersNames.put( o.getKey() , outer).getName() , outer );
					}else{
						OuterName out = outer_comp.addOuterName( o.getValue().getName() );
						outer_comp.addInnerName( o.getValue().getName() , out );
						this.outersNames.put( o.getKey() , out );
					}
				}
				
				for( Map.Entry<String , OuterName> o : this.outersNames.entrySet() ){
					if( !pb.outersNames.containsKey( o.getKey() ) )
						outer_comp.addInnerName( o.getValue().getName() , outer_comp.addOuterName( o.getValue().getName() ) );
				}

				for( Map.Entry<String , OuterName> e : pb.edgesNames.entrySet() ){
					if( this.edgesNames.containsKey( e.getKey() ) ){
						OuterName edge = outer_comp.addOuterName();
						outer_comp.addInnerName( e.getValue().getName() , edge );
						outer_comp.addInnerName( this.edgesNames.put( e.getKey() , edge ).getName() , edge );
					}else{
						OuterName out = outer_comp.addOuterName( e.getValue().getName() );
						outer_comp.addInnerName( e.getValue().getName() , out );
						this.edgesNames.put( e.getKey() , out );
					}
				}

				for( Map.Entry<String , OuterName> e : this.edgesNames.entrySet() ){
					if( !pb.edgesNames.containsKey( e.getKey() ) )
						outer_comp.addInnerName( e.getValue().getName() , outer_comp.addOuterName( e.getValue().getName() ) );
				}

				this.leftJuxtapose( pb.makeBigraph() );

 				for( int i = 0 ; i < this.getRoots().size() ; ++i )
					outer_comp.addSite( outer_comp.addRoot() );
				this.outerCompose( outer_comp.makeBigraph() );

				this.innersNames.addAll( pb.innersNames );
				this.sitesNames.addAll( pb.sitesNames );
			}

			/* Add to the ParsedBigraph relations between outer (or edge) and inner interface
			 * Es: outer <- inner1 , inner2
			 * @param o
			 * 		Handler (outer or edge) used
			 * @param list
			 *		List of inner names linked with the handler o (first parameter)
			 */
			public void addLinks( Name<String , Character> o , LinkedList<Name<String , Character>> list ){

				OuterName out;
				if( list == null && ( o == null || o.getType() == 'e' || o.getType() == 'u' ) ) return;

				if( o == null ){
					out = addOuterName();
					edgesNames.put( out.getName() , out );
				}else{ 
					switch( o.getType() ){
						case 'o':
							out = addOuterName();
							outersNames.put( o.getName() , out );
							break;
						case 'e':
							out = addOuterName();
							edgesNames.put( o.getName() , out );
							break;
						case 'u':
							out = addOuterName();
							edgesNames.put( out.getName() , out );
							break;
						default:
							throw new IllegalArgumentException( "In <- operator, innernames can't appear as a prefix (" + o.getName() + ":i)" );
					}
				}

				for( Name<String , Character> name : list ){
					if( name.getType() != 'i' )
						throw new IllegalArgumentException( "In <- operator, only innernames can appear as a suffix (" + name.getName() + ":" + name.getType() + ")" );
					if( innersNames.contains( name.getName() ) )
						throw new RuntimeException( "Innernames (" + name.getName() + ") can't appear multiple time in a single bigraph." );
					addInnerName( name.getName() , out );
					innersNames.add( name.getName() );
				}	
			}	


			/* Make a Bigraph out of a ParsedBigraph. 
			 * Edges must be managed before calling BigraphBuilder::makeBigraph()
			 *
			 * @return Bigraph
			 */
			public Bigraph switchToBigraph(){
				BigraphBuilder outer_comp = new BigraphBuilder( getSignature() );
				
				for( Map.Entry<String , OuterName> o : this.outersNames.entrySet() )
					outer_comp.addInnerName( o.getValue().getName() , outer_comp.addOuterName( o.getKey() ) );
				for( OuterName o : this.edgesNames.values() )
					outer_comp.addInnerName( o.getName() );
				for( int i = 0; i < this.getRoots().size() ; ++i )
					outer_comp.addSite( outer_comp.addRoot() );

				this.outerCompose( outer_comp.makeBigraph() );

				return this.makeBigraph();
			}

			/* Close all sites of a ParsedBigraph 
			 */
			public void groundPlaceGraph(){
				BigraphBuilder ground = new BigraphBuilder( getSignature() );
				for( int i = 0; i < getSites().size() ; ++i )
					ground.addRoot();
				for( String str : innersNames )
					ground.addInnerName( str , ground.addOuterName( str ) );
				innerCompose( ground.makeBigraph() );
			}

			public void addSite( int n ){
				if( sitesNames.contains( n ) )
					throw new IllegalArgumentException( "The same site ($" + n + ") can't appear multiple time in a single bigraph." );
				addSite( addRoot() );
				sitesNames.add( n );
			}
				
		}

		public class ExtendedBRS{
			private Signature signature;
			private Set<Bigraph> bigraphs;
			private Map<Bigraph , Bigraph> reactionRules;
			
			ExtendedBRS(){
				signature = null;
				bigraphs = new HashSet<>();
				reactionRules = new HashMap<>();
			}
			
			public void setSignature( Signature sig ){ 
				signature = sig; 
			}
			
			public Signature getSignature(){ 
				return signature; 
			}

			public void addBigraph( Bigraph b ){
				bigraphs.add( b );
			}
			
			public void addReaction( Bigraph redex , Bigraph reactum ){
				if(	redex.getRoots().size() != reactum.getRoots().size() || 
					!redex.getOuterNames().containsAll( reactum.getOuterNames() ) ||
					!redex.getInnerNames().containsAll( reactum.getInnerNames() ) )
						throw new RuntimeException("The interface of a reactum must be the same of its redex (both inner and outer faces)");

				reactionRules.put( redex , reactum );
			}
			
			public Set<Bigraph> getBigraphs(){
				return bigraphs;
			}
			
			public Map<Bigraph , Bigraph> getReactions(){
				return reactionRules;
			}
		}

		public class Name<K,V>{
			private final K name;
			private final V type;

			public Name( K name , V type ){
				this.name = name;
				this.type = type;
			}
			public K getName(){ return name; }
			public V getType(){ return type; }
		}

	private final Action[] actions;

	public BigraphParser() {
		super(PARSING_TABLES);
		actions = new Action[] {
			RETURN2,	// [0] start = definitionlist names; returns 'names' although none is marked
			Action.RETURN,	// [1] start = names
			RETURN2,	// [2] names = namedef names; returns 'names' although none is marked
			Action.RETURN,	// [3] names = reactions
			RETURN2,	// [4] reactions = reaction reactions; returns 'reactions' although none is marked
			Action.RETURN,	// [5] reactions = models
			Action.NONE,  	// [6] models = 
			new Action() {	// [7] models = model.b SEMICOLON models
				public Symbol reduce(Symbol[] _symbols, int offset) {
					final Symbol _symbol_b = _symbols[offset + 1];
					final ParsedBigraph b = (ParsedBigraph) _symbol_b.value;
					 _brs.addBigraph( b.switchToBigraph() ); return new Symbol( null );
				}
			},
			new Action() {	// [8] definitionlist = CTRL.b VARID.v COLON NUM.n SEMICOLON definitions.sb
				public Symbol reduce(Symbol[] _symbols, int offset) {
					final Symbol _symbol_b = _symbols[offset + 1];
					final Boolean b = (Boolean) _symbol_b.value;
					final Symbol _symbol_v = _symbols[offset + 2];
					final String v = (String) _symbol_v.value;
					final Symbol _symbol_n = _symbols[offset + 4];
					final Integer n = (Integer) _symbol_n.value;
					final Symbol _symbol_sb = _symbols[offset + 6];
					final SignatureBuilder sb = (SignatureBuilder) _symbol_sb.value;
					
				sb.put( v , b , n ); _brs.setSignature( sb.makeSignature() );
				return new Symbol( null );
				}
			},
			new Action() {	// [9] definitions = 
				public Symbol reduce(Symbol[] _symbols, int offset) {
					 return new Symbol( new SignatureBuilder() );
				}
			},
			new Action() {	// [10] definitions = CTRL.b VARID.v COLON NUM.n SEMICOLON definitions.sb
				public Symbol reduce(Symbol[] _symbols, int offset) {
					final Symbol _symbol_b = _symbols[offset + 1];
					final Boolean b = (Boolean) _symbol_b.value;
					final Symbol _symbol_v = _symbols[offset + 2];
					final String v = (String) _symbol_v.value;
					final Symbol _symbol_n = _symbols[offset + 4];
					final Integer n = (Integer) _symbol_n.value;
					final Symbol _symbol_sb = _symbols[offset + 6];
					final SignatureBuilder sb = (SignatureBuilder) _symbol_sb.value;
					 sb.put( v , b , n ); return new Symbol( sb );
				}
			},
			new Action() {	// [11] namedef = OUTERNAME VARID.v SEMICOLON
				public Symbol reduce(Symbol[] _symbols, int offset) {
					final Symbol _symbol_v = _symbols[offset + 2];
					final String v = (String) _symbol_v.value;
					 _outerNames.add( v ); return new Symbol( null );
				}
			},
			new Action() {	// [12] namedef = INNERNAME VARID.v SEMICOLON
				public Symbol reduce(Symbol[] _symbols, int offset) {
					final Symbol _symbol_v = _symbols[offset + 2];
					final String v = (String) _symbol_v.value;
					 _innerNames.add( v ); return new Symbol( null );
				}
			},
			new Action() {	// [13] reaction = model.b1 REACT model.b2 SEMICOLON
				public Symbol reduce(Symbol[] _symbols, int offset) {
					final Symbol _symbol_b1 = _symbols[offset + 1];
					final ParsedBigraph b1 = (ParsedBigraph) _symbol_b1.value;
					final Symbol _symbol_b2 = _symbols[offset + 3];
					final ParsedBigraph b2 = (ParsedBigraph) _symbol_b2.value;
					 
							if( !b1.sitesNames.containsAll( b2.sitesNames ) )
								throw new RuntimeException("Sites ($num) in a reactum must be the same of its redex.");
							_brs.addReaction( b1.switchToBigraph() , b2.switchToBigraph() ); return new Symbol( null );
				}
			},
			new Action() {	// [14] model = t.b
				public Symbol reduce(Symbol[] _symbols, int offset) {
					final Symbol _symbol_b = _symbols[offset + 1];
					final ParsedBigraph b = (ParsedBigraph) _symbol_b.value;
					 return new Symbol( b );
				}
			},
			new Action() {	// [15] model = links.l
				public Symbol reduce(Symbol[] _symbols, int offset) {
					final Symbol _symbol_l = _symbols[offset + 1];
					final ParsedBigraph l = (ParsedBigraph) _symbol_l.value;
					 return new Symbol( l );
				}
			},
			new Action() {	// [16] model = links.l DPIPE model.b
				public Symbol reduce(Symbol[] _symbols, int offset) {
					final Symbol _symbol_l = _symbols[offset + 1];
					final ParsedBigraph l = (ParsedBigraph) _symbol_l.value;
					final Symbol _symbol_b = _symbols[offset + 3];
					final ParsedBigraph b = (ParsedBigraph) _symbol_b.value;
					 b.juxtapose( l ); return new Symbol( b );
				}
			},
			new Action() {	// [17] links = LINKED nms.l
				public Symbol reduce(Symbol[] _symbols, int offset) {
					final Symbol _symbol_l = _symbols[offset + 2];
					final LinkedList<Name<String , Character>> l = (LinkedList<Name<String , Character>>) _symbol_l.value;
					 ParsedBigraph b = new ParsedBigraph( _brs.getSignature() );  b.addLinks( null , l ); return new Symbol( b );
				}
			},
			new Action() {	// [18] links = name.n LINKED nms.l
				public Symbol reduce(Symbol[] _symbols, int offset) {
					final Symbol _symbol_n = _symbols[offset + 1];
					final Name<String , Character> n = (Name<String , Character>) _symbol_n.value;
					final Symbol _symbol_l = _symbols[offset + 3];
					final LinkedList<Name<String , Character>> l = (LinkedList<Name<String , Character>>) _symbol_l.value;
					 ParsedBigraph b = new ParsedBigraph( _brs.getSignature() ); b.addLinks( n , l ); return new Symbol( b );
				}
			},
			new Action() {	// [19] t = k.b1 POINT t.b2
				public Symbol reduce(Symbol[] _symbols, int offset) {
					final Symbol _symbol_b1 = _symbols[offset + 1];
					final ParsedBigraph b1 = (ParsedBigraph) _symbol_b1.value;
					final Symbol _symbol_b2 = _symbols[offset + 3];
					final ParsedBigraph b2 = (ParsedBigraph) _symbol_b2.value;
					 b1.compose( b2 ); return new Symbol( b1 );
				}
			},
			new Action() {	// [20] t = t.b1 PIPE t.b2
				public Symbol reduce(Symbol[] _symbols, int offset) {
					final Symbol _symbol_b1 = _symbols[offset + 1];
					final ParsedBigraph b1 = (ParsedBigraph) _symbol_b1.value;
					final Symbol _symbol_b2 = _symbols[offset + 3];
					final ParsedBigraph b2 = (ParsedBigraph) _symbol_b2.value;
					 b1.juxtapose( b2 ); b1.merge(); return new Symbol( b1 );
				}
			},
			new Action() {	// [21] t = t.b1 DPIPE t.b2
				public Symbol reduce(Symbol[] _symbols, int offset) {
					final Symbol _symbol_b1 = _symbols[offset + 1];
					final ParsedBigraph b1 = (ParsedBigraph) _symbol_b1.value;
					final Symbol _symbol_b2 = _symbols[offset + 3];
					final ParsedBigraph b2 = (ParsedBigraph) _symbol_b2.value;
					 b1.juxtapose( b2 ); return new Symbol( b1 );
				}
			},
			new Action() {	// [22] t = INNERPLACE NUM.n
				public Symbol reduce(Symbol[] _symbols, int offset) {
					final Symbol _symbol_n = _symbols[offset + 2];
					final Integer n = (Integer) _symbol_n.value;
					 ParsedBigraph b = new ParsedBigraph( _brs.getSignature() ); b.addSite( n ); return new Symbol( b );
				}
			},
			new Action() {	// [23] t = k.b
				public Symbol reduce(Symbol[] _symbols, int offset) {
					final Symbol _symbol_b = _symbols[offset + 1];
					final ParsedBigraph b = (ParsedBigraph) _symbol_b.value;
					 b.groundPlaceGraph(); return new Symbol( b );
				}
			},
			new Action() {	// [24] t = NIL
				public Symbol reduce(Symbol[] _symbols, int offset) {
					 ParsedBigraph b = new ParsedBigraph( _brs.getSignature() ); b.addRoot(); return new Symbol( b );
				}
			},
			new Action() {	// [25] t = PAROPEN t.b PARCLOSE
				public Symbol reduce(Symbol[] _symbols, int offset) {
					final Symbol _symbol_b = _symbols[offset + 2];
					final ParsedBigraph b = (ParsedBigraph) _symbol_b.value;
					 return new Symbol( b );
				}
			},
			new Action() {	// [26] k = node.c SQOPEN nms.l SQCLOSE
				public Symbol reduce(Symbol[] _symbols, int offset) {
					final Symbol _symbol_c = _symbols[offset + 1];
					final String c = (String) _symbol_c.value;
					final Symbol _symbol_l = _symbols[offset + 3];
					final LinkedList<Name<String , Character>> l = (LinkedList<Name<String , Character>>) _symbol_l.value;
					 ParsedBigraph b = new ParsedBigraph( _brs.getSignature() ); b.makeIon( c , l ); return new Symbol( b );
				}
			},
			new Action() {	// [27] k = node.c
				public Symbol reduce(Symbol[] _symbols, int offset) {
					final Symbol _symbol_c = _symbols[offset + 1];
					final String c = (String) _symbol_c.value;
					 ParsedBigraph b = new ParsedBigraph( _brs.getSignature() ); b.makeIon( c , null ); return new Symbol( b );
				}
			},
			new Action() {	// [28] node = VARID.v
				public Symbol reduce(Symbol[] _symbols, int offset) {
					final Symbol _symbol_v = _symbols[offset + 1];
					final String v = (String) _symbol_v.value;
					 return new Symbol( v );
				}
			},
			new Action() {	// [29] node = VARID.v1 COLON VARID.v2
				public Symbol reduce(Symbol[] _symbols, int offset) {
					final Symbol _symbol_v1 = _symbols[offset + 1];
					final String v1 = (String) _symbol_v1.value;
					final Symbol _symbol_v2 = _symbols[offset + 3];
					final String v2 = (String) _symbol_v2.value;
					 return new Symbol( v2 );
				}
			},
			new Action() {	// [30] nms = 
				public Symbol reduce(Symbol[] _symbols, int offset) {
					 return new Symbol( null );
				}
			},
			new Action() {	// [31] nms = nameli.l
				public Symbol reduce(Symbol[] _symbols, int offset) {
					final Symbol _symbol_l = _symbols[offset + 1];
					final LinkedList<Name<String , Character>> l = (LinkedList<Name<String , Character>>) _symbol_l.value;
					 return new Symbol( l );
				}
			},
			new Action() {	// [32] nameli = name.s
				public Symbol reduce(Symbol[] _symbols, int offset) {
					final Symbol _symbol_s = _symbols[offset + 1];
					final Name<String , Character> s = (Name<String , Character>) _symbol_s.value;
					 List<Name<String , Character>> list = new LinkedList<>(); list.add( s ); return new Symbol( list );
				}
			},
			new Action() {	// [33] nameli = name.s COMMA nameli.l
				public Symbol reduce(Symbol[] _symbols, int offset) {
					final Symbol _symbol_s = _symbols[offset + 1];
					final Name<String , Character> s = (Name<String , Character>) _symbol_s.value;
					final Symbol _symbol_l = _symbols[offset + 3];
					final LinkedList<Name<String , Character>> l = (LinkedList<Name<String , Character>>) _symbol_l.value;
					 l.addFirst( s ); return new Symbol( l );
				}
			},
			new Action() {	// [34] name = VARID.v INNER
				public Symbol reduce(Symbol[] _symbols, int offset) {
					final Symbol _symbol_v = _symbols[offset + 1];
					final String v = (String) _symbol_v.value;
					 return new Symbol( new Name<String , Character>( v , 'i' ) );
				}
			},
			new Action() {	// [35] name = VARID.v OUTER
				public Symbol reduce(Symbol[] _symbols, int offset) {
					final Symbol _symbol_v = _symbols[offset + 1];
					final String v = (String) _symbol_v.value;
					 return new Symbol( new Name<String , Character>( v , 'o' ) );
				}
			},
			new Action() {	// [36] name = VARID.v EDGE
				public Symbol reduce(Symbol[] _symbols, int offset) {
					final Symbol _symbol_v = _symbols[offset + 1];
					final String v = (String) _symbol_v.value;
					 return new Symbol( new Name<String , Character>( v , 'e' ) );
				}
			},
			new Action() {	// [37] name = VARID.v
				public Symbol reduce(Symbol[] _symbols, int offset) {
					final Symbol _symbol_v = _symbols[offset + 1];
					final String v = (String) _symbol_v.value;
					 return new Symbol( new Name<String , Character>( v , _outerNames.contains( v ) ? 'o' : 
										_innerNames.contains( v ) ? 'i' : 'e' ) );
				}
			},
			new Action() {	// [38] name = UNLINKED
				public Symbol reduce(Symbol[] _symbols, int offset) {
					 return new Symbol( new Name<String , Character>( null , 'u' ) );
				}
			}
		};

 	
		_outerNames = new HashSet<>();
		_innerNames = new HashSet<>();
		_brs = new ExtendedBRS();
	}

	protected Symbol invokeReduceAction(int rule_num, int offset) {
		return actions[rule_num].reduce(_symbols, offset);
	}
}
