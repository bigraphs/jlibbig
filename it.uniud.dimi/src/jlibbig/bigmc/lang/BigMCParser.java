package jlibbig.bigmc.lang;

import java.util.ArrayList;
import java.io.*;
import beaver.*;
import jlibbig.bigmc.*;
import java.util.*;
import jlibbig.core.*;
import jlibbig.core.std.Control;
import jlibbig.core.std.OuterName;
import jlibbig.core.std.Signature;
import jlibbig.core.std.SignatureBuilder;

/**
 * This class is a LALR parser generated by <a
 * href="http://beaver.sourceforge.net">Beaver</a> v0.9.6.1 from the grammar
 * specification "parser.grammar".
 */
class BigMCParser extends Parser {
	static public class Terminals {
		static public final short EOF = 0;
		static public final short VARID = 1;
		static public final short INNERPLACE = 2;
		static public final short PAROPEN = 3;
		static public final short NIL = 4;
		static public final short SQCLOSE = 5;
		static public final short SEMICOLON = 6;
		static public final short PIPE = 7;
		static public final short CTRL = 8;
		static public final short DPIPE = 9;
		static public final short NUM = 10;
		static public final short POINT = 11;
		static public final short UNLINKED = 12;
		static public final short COLON = 13;
		static public final short SQOPEN = 14;
		static public final short OUTERNAME = 15;
		static public final short REACT = 16;
		static public final short PARCLOSE = 17;
		static public final short COMMA = 18;
	}

	static final ParsingTables PARSING_TABLES = new ParsingTables(
			"U9o5acbFKq4KXj$RsrwM5WMc8DGgY#me408nqO3B5mKYcFY9VyElbLq11KM1upEJkVM6a33"
					+ "DCsS$CtTw96rhN0qDQaGbFTI4VJLJbvpwrMStkQQAEbHMjMKtr5JMikwJxLG$KTMkr657T4"
					+ "dj07j4RNgg8GseKmzK8wSR#x5wrQD7gjkXxMjOppIcKSwUr9HcDQqP#wiwFVl96U3U3IgaX"
					+ "DkdPFtNChexUkQsh#XkMVT9NvrGtOxqtrvdJpZHSSygztFSs8LOZViwV8wlgF1pv6MyfACs"
					+ "2M$KYIOztr5OBwBqlbhK#s0swiqeXs45bc0Hti8gZCNuEo#ZFWNpKVy0o$0AFXPoylFVmoQ"
					+ "iHxiBDk0BhC4ds8BFFcx6$oKcX7Sjz9efw7DHBkG#NrE8B#TwmVVo3b$bbltwTaxqlxdBdt"
					+ "yZfr$QfLVjTzX$mIcSs7cmVy8PVBTlmJvMNBPdk#m$x29OVrHOTiqso9oL867gCgPEa34HP"
					+ "H3paOOPgHDtWJAaa4NfASLOIZVVCM4cFIaJa0IQ$m0t3MUn");

	static final Action RETURN2 = new Action() {
		public Symbol reduce(Symbol[] _symbols, int offset) {
			return _symbols[offset + 2];
		}
	};

	private BigraphRewritingSystem _brs;

	/**
	 * Parse a string, creating a BRS and a set of Bigraphs from it. Everything
	 * will be stored in a BigraphSystem.
	 * 
	 * @param str
	 *            String that will be parsed, in BigMC syntax.
	 * @return A system that contains signature, set of bigraphs and a set of
	 *         reaction rules.
	 * @see BigraphRewritingSystem
	 */
	BigraphRewritingSystem parse(String str) throws IOException, Parser.Exception {
		return parse(new StringReader(str));
	}

	/**
	 * Parse a string, creating a BRS and a set of Bigraphs from it. Everything
	 * will be stored in a BigraphSystem.
	 * 
	 * @param in
	 *            Reader that will be parsed, in BigMC syntax.
	 * @return A system that contains signature, set of bigraphs and a set of
	 *         reaction rules.
	 * @see BigraphRewritingSystem
	 */
	BigraphRewritingSystem parse(Reader in) throws IOException, Parser.Exception {
		_brs = null;
		parse(new BigMCLexer(in));
		return _brs;
	}

	/**
	 * Parse a string, creating a BRS and a set of Bigraphs from it. Everything
	 * will be stored in a BigraphSystem.
	 * 
	 * @param str
	 *            String that will be parsed, in BigMC syntax.
	 * @param sig
	 *            Signature that will be used by the BigraphSystem. Controls
	 *            defined in the parsed string must agree with the signature's
	 *            controls.
	 * @return A system that contains signature, set of bigraphs and a set of
	 *         reaction rules.
	 * @see BigraphRewritingSystem
	 */
	BigraphRewritingSystem parse(String str, Signature sig) throws IOException,
			Parser.Exception {
		return parse(new StringReader(str), sig);
	}

	/**
	 * Parse a string, creating a BRS and a set of Bigraphs from it. Everything
	 * will be stored in a BigraphSystem.
	 * 
	 * @param in
	 *            Reader that will be parsed, in BigMC syntax.
	 * @param sig
	 *            Signature that will be used by the BigraphSystem. Controls
	 *            defined in the parsed string must agree with the signature's
	 *            controls.
	 * @return A system that contains signature, set of bigraphs and a set of
	 *         reaction rules.
	 * @see BigraphRewritingSystem
	 */
	BigraphRewritingSystem parse(Reader in, Signature sig) throws IOException,
			Parser.Exception {
		_brs = new BigraphRewritingSystem(sig);
		parse(new BigMCLexer(in));
		return _brs;
	}

	/**
	 * Override default recoverFromError method. Policy: never recover.
	 */
	protected void recoverFromError(Symbol token, TokenStream in)
			throws IOException, Parser.Exception {
		throw new IOException("Syntax Error.");
	}

	private final Action[] actions;

	public BigMCParser() {
		super(PARSING_TABLES);
		actions = new Action[] { RETURN2, // [0] start = definitionlist models;
											// returns 'models' although none is
											// marked
				Action.RETURN, // [1] start = models
				new Action() { // [2] definitionlist = definitions.sb CTRL.b
					// VARID.v COLON NUM.n SEMICOLON
					public Symbol reduce(Symbol[] _symbols, int offset) {
						final Symbol _symbol_sb = _symbols[offset + 1];
						final SignatureBuilder sb = (SignatureBuilder) _symbol_sb.value;
						final Symbol _symbol_b = _symbols[offset + 2];
						final Boolean b = (Boolean) _symbol_b.value;
						final Symbol _symbol_v = _symbols[offset + 3];
						final String v = (String) _symbol_v.value;
						final Symbol _symbol_n = _symbols[offset + 5];
						final Integer n = (Integer) _symbol_n.value;

						if (_brs == null) {
							sb.put(v, b, n);
							_brs = new BigraphRewritingSystem(sb.makeSignature());
						} else {
							Control c = null;
							if ((c = _brs.getSignature().getByName(v)) == null
									|| c.getArity() != n || c.isActive() != b)
								throw new RuntimeException(
										"Line: "
												+ Symbol.getLine(_symbol_v
														.getStart())
												+ " - Control "
												+ v
												+ ", "
												+ (b == true ? "active"
														: "passive")
												+ " and with arity "
												+ n
												+ ", not found in the input's signature");
						}
						return new Symbol(null);
					}
				}, new Action() { // [3] definitions =
					public Symbol reduce(Symbol[] _symbols, int offset) {
						return new Symbol(new SignatureBuilder());
					}
				}, new Action() { // [4] definitions = definitions.sb CTRL.b
									// VARID.v COLON NUM.n SEMICOLON
					public Symbol reduce(Symbol[] _symbols, int offset) {
						final Symbol _symbol_sb = _symbols[offset + 1];
						final SignatureBuilder sb = (SignatureBuilder) _symbol_sb.value;
						final Symbol _symbol_b = _symbols[offset + 2];
						final Boolean b = (Boolean) _symbol_b.value;
						final Symbol _symbol_v = _symbols[offset + 3];
						final String v = (String) _symbol_v.value;
						final Symbol _symbol_n = _symbols[offset + 5];
						final Integer n = (Integer) _symbol_n.value;

						if (_brs == null) {
							if (sb.contains(v))
								throw new RuntimeException("Line: "
										+ Symbol.getLine(_symbol_v.getStart())
										+ " - Control already defined: " + v);
							sb.put(v, b, n);
							return new Symbol(sb);
						}
						Control c = null;
						if ((c = _brs.getSignature().getByName(v)) == null
								|| c.getArity() != n || c.isActive() != b)
							throw new RuntimeException("Line: "
									+ Symbol.getLine(_symbol_v.getStart())
									+ " - Control " + v + ", "
									+ (b == true ? "active" : "passive")
									+ " and with arity " + n
									+ ", not found in the input's signature");
						return new Symbol(null);
					}
				}, new Action() { // [5] names = names OUTERNAME VARID.v
									// SEMICOLON
					public Symbol reduce(Symbol[] _symbols, int offset) {
						final Symbol _symbol_v = _symbols[offset + 3];
						final String v = (String) _symbol_v.value;
						_brs.addName(v);
						return new Symbol(null);
					}
				}, new Action() { // [6] names =
					public Symbol reduce(Symbol[] _symbols, int offset) {
						if (_brs == null)
							_brs = new BigraphRewritingSystem(
									new SignatureBuilder().makeSignature());
						return new Symbol(null);
					}
				}, new Action() { // [7] models = models big.b SEMICOLON
					public Symbol reduce(Symbol[] _symbols, int offset) {
						final Symbol _symbol_b = _symbols[offset + 2];
						final ReactionBigraphBuilder b = (ReactionBigraphBuilder) _symbol_b.value;

						for (OuterName out : b.getOuterNames()) {
							if (!_brs.getOuterNames().contains(out.getName()))
								throw new RuntimeException(
										"Line: "
												+ Symbol.getLine(_symbol_b
														.getStart())
												+ " - Agents' outernames can't be free names. Eachone of them must be declared (using %outer or %name). Undeclared name: "
												+ out.getName());
						}
						if (b.getRoots().size() != 1)
							throw new RuntimeException(
									"Line: "
											+ Symbol.getLine(_symbol_b
													.getStart())
											+ " - BigMC's agent can't have more than one top level region (root). The double-parallel operator (||) can only appear in reaction rules.");

						try {
							_brs.addBigraph(new AgentBigraph(b.makeBigraph()));
						} catch (IllegalArgumentException e) {
							throw new RuntimeException(
									"Line: "
											+ Symbol.getLine(_symbol_b
													.getStart())
											+ " - Sites can only appear in reaction rules.");
						}
						return new Symbol(null);
					}
				}, new Action() { // [8] models = models big.b1 REACT big.b2
									// SEMICOLON
					public Symbol reduce(Symbol[] _symbols, int offset) {
						final Symbol _symbol_b1 = _symbols[offset + 2];
						final ReactionBigraphBuilder b1 = (ReactionBigraphBuilder) _symbol_b1.value;
						final Symbol _symbol_b2 = _symbols[offset + 4];
						final ReactionBigraphBuilder b2 = (ReactionBigraphBuilder) _symbol_b2.value;

						_brs.addReaction(b1.makeReactionBigraph(),
								b2.makeReactionBigraph());
						return new Symbol(null);
					}
				}, Action.RETURN, // [9] models = names
				new Action() { // [10] big = VARID.v
					public Symbol reduce(Symbol[] _symbols, int offset) {
						final Symbol _symbol_v = _symbols[offset + 1];
						final String v = (String) _symbol_v.value;

						ReactionBigraphBuilder rbb = new ReactionBigraphBuilder(
								_brs.getSignature());
						rbb.addNode(v, rbb.addRoot());
						return new Symbol(rbb);
					}
				}, new Action() { // [11] big = VARID.v SQOPEN nms.l SQCLOSE
					public Symbol reduce(Symbol[] _symbols, int offset) {
						final Symbol _symbol_v = _symbols[offset + 1];
						final String v = (String) _symbol_v.value;
						final Symbol _symbol_l = _symbols[offset + 3];
						final LinkedList<String> l = (LinkedList<String>) _symbol_l.value;

						ReactionBigraphBuilder rbb = new ReactionBigraphBuilder(
								_brs.getSignature());
						rbb.addNode(v, rbb.addRoot(), rbb.addOuterNames(l));
						return new Symbol(rbb);
					}
				}, new Action() { // [12] big = VARID.v POINT big.b
					public Symbol reduce(Symbol[] _symbols, int offset) {
						final Symbol _symbol_v = _symbols[offset + 1];
						final String v = (String) _symbol_v.value;
						final Symbol _symbol_b = _symbols[offset + 3];
						final ReactionBigraphBuilder b = (ReactionBigraphBuilder) _symbol_b.value;

						if (b.getRoots().size() != 1)
							throw new RuntimeException(
									"Line: "
											+ Symbol.getLine(_symbol_b
													.getStart())
											+ " - Juxtaposition (||) can only appear at top level.");
						b.outerNestNode(v);
						return new Symbol(b);
					}
				}, new Action() { // [13] big = VARID.v SQOPEN nms.l SQCLOSE
									// POINT big.b
					public Symbol reduce(Symbol[] _symbols, int offset) {
						final Symbol _symbol_v = _symbols[offset + 1];
						final String v = (String) _symbol_v.value;
						final Symbol _symbol_l = _symbols[offset + 3];
						final LinkedList<String> l = (LinkedList<String>) _symbol_l.value;
						final Symbol _symbol_b = _symbols[offset + 6];
						final ReactionBigraphBuilder b = (ReactionBigraphBuilder) _symbol_b.value;

						if (b.getRoots().size() != 1)
							throw new RuntimeException(
									"Line: "
											+ Symbol.getLine(_symbol_b
													.getStart())
											+ " - Juxtaposition (||) can only appear at top level.");
						b.outerNestNode(v, b.addOuterNames(l));
						return new Symbol(b);
					}
				}, new Action() { // [14] big = big.b1 PIPE big.b2
					public Symbol reduce(Symbol[] _symbols, int offset) {
						final Symbol _symbol_b1 = _symbols[offset + 1];
						final ReactionBigraphBuilder b1 = (ReactionBigraphBuilder) _symbol_b1.value;
						final Symbol _symbol_b2 = _symbols[offset + 3];
						final ReactionBigraphBuilder b2 = (ReactionBigraphBuilder) _symbol_b2.value;

						if (b1.getRoots().size() != 1
								|| b2.getRoots().size() != 1)
							throw new RuntimeException(
									"Line: "
											+ Symbol.getLine(_symbol_b2
													.getStart())
											+ " - Juxtaposition (||) can only appear at top level.");
						b1.rightParallelProduct(b2.makeReactionBigraph());
						b1.merge();
						return new Symbol(b1);
					}
				}, new Action() { // [15] big = big.b1 DPIPE big.b2
					public Symbol reduce(Symbol[] _symbols, int offset) {
						final Symbol _symbol_b1 = _symbols[offset + 1];
						final ReactionBigraphBuilder b1 = (ReactionBigraphBuilder) _symbol_b1.value;
						final Symbol _symbol_b2 = _symbols[offset + 3];
						final ReactionBigraphBuilder b2 = (ReactionBigraphBuilder) _symbol_b2.value;

						b1.rightParallelProduct(b2.makeReactionBigraph());
						return new Symbol(b1);
					}
				}, new Action() { // [16] big = INNERPLACE NUM.n
					public Symbol reduce(Symbol[] _symbols, int offset) {
						final Symbol _symbol_n = _symbols[offset + 2];
						final Integer n = (Integer) _symbol_n.value;

						ReactionBigraphBuilder rbb = new ReactionBigraphBuilder(
								_brs.getSignature());
						rbb.addSite(rbb.addRoot(), n);
						return new Symbol(rbb);
					}
				}, new Action() { // [17] big = NIL
					public Symbol reduce(Symbol[] _symbols, int offset) {

						ReactionBigraphBuilder rbb = new ReactionBigraphBuilder(
								_brs.getSignature());
						rbb.addRoot();
						return new Symbol(rbb);
					}
				}, new Action() { // [18] big = PAROPEN big.b PARCLOSE
					public Symbol reduce(Symbol[] _symbols, int offset) {
						final Symbol _symbol_b = _symbols[offset + 2];
						final ReactionBigraphBuilder b = (ReactionBigraphBuilder) _symbol_b.value;
						return new Symbol(b);
					}
				}, new Action() { // [19] nms =
					public Symbol reduce(Symbol[] _symbols, int offset) {
						return new Symbol(new LinkedList<>());
					}
				}, new Action() { // [20] nms = nameli.l
					public Symbol reduce(Symbol[] _symbols, int offset) {
						final Symbol _symbol_l = _symbols[offset + 1];
						final LinkedList<String> l = (LinkedList<String>) _symbol_l.value;
						return new Symbol(l);
					}
				}, new Action() { // [21] nameli = name.v
					public Symbol reduce(Symbol[] _symbols, int offset) {
						final Symbol _symbol_v = _symbols[offset + 1];
						final String v = (String) _symbol_v.value;

						List<String> list = new LinkedList<>();
						list.add(v);
						return new Symbol(list);
					}
				}, new Action() { // [22] nameli = name.v COMMA nameli.l
					public Symbol reduce(Symbol[] _symbols, int offset) {
						final Symbol _symbol_v = _symbols[offset + 1];
						final String v = (String) _symbol_v.value;
						final Symbol _symbol_l = _symbols[offset + 3];
						final LinkedList<String> l = (LinkedList<String>) _symbol_l.value;
						l.addFirst(v);
						return new Symbol(l);
					}
				}, new Action() { // [23] name = VARID.v
					public Symbol reduce(Symbol[] _symbols, int offset) {
						final Symbol _symbol_v = _symbols[offset + 1];
						final String v = (String) _symbol_v.value;
						return new Symbol(v);
					}
				}, new Action() { // [24] name = UNLINKED
					public Symbol reduce(Symbol[] _symbols, int offset) {
						return new Symbol(null);
					}
				} };

		_brs = null;
	}

	protected Symbol invokeReduceAction(int rule_num, int offset) {
		return actions[rule_num].reduce(_symbols, offset);
	}
}
