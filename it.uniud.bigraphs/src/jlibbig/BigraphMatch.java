package jlibbig;

import java.util.*;

public class BigraphMatch {
	// flags for copy on write behaviour
	//private final boolean _copyOnWrite;
	//private boolean _cloned = false;

	private Bigraph _ctx;
	private Bigraph _rdx;
	private final List<Bigraph> _args = new LinkedList<>();

	private BigraphView _ctx_view;
	private BigraphView _rdx_view;
	private List<BigraphView> _args_view;

	/**
	 * Construct a match for the given context, redex and arguments. These must
	 * be composable into a single bigraph otherwise an exception is thrown.
	 * Arguments are not cloned for efficiency (operations on them made somewere
	 * else can introduce inconsistencies).
	 * @throws IllegalArgumentException
	 *             if arguments are null or not composable.
	 * @param ctx
	 *            the context
	 * @param rdx
	 *            the redex
	 * @param args
	 *            the arguments
	 */
	public BigraphMatch(Bigraph ctx, Bigraph rdx, List<Bigraph> args) {
		this(ctx, rdx, args, false); //, true);
	}

	/**
	 * Construct a match for the given context, redex and arguments. These must
	 * be composable into a single bigraph otherwise an exception is thrown.
	 * 
	 * @throws IllegalArgumentException
	 *             if arguments are null or not composable.
	 * @param ctx
	 *            the context
	 * @param rdx
	 *            the redex
	 * @param args
	 *            the arguments
	 * @param skipChecks
	 *            if {@literal} composability checks are skipped
	 */
	
	//	 * @param copyOnWrite clone bigraphs at the first write access

	protected BigraphMatch(Bigraph ctx, Bigraph rdx, List<Bigraph> args,
			boolean skipChecks){
		//, boolean copyOnWrite) {
		if (ctx == null || rdx == null)
			throw new IllegalArgumentException("Context, redex can not be null");
		if (args == null)
			args = new LinkedList<>();
		for (Bigraph arg : args) {
			if (arg == null)
				throw new IllegalArgumentException("Arguments can not be null");
		}
		if (!skipChecks) {
			boolean ok = true;
			ok &= ctx.isComposable(rdx);

			Set<BigraphNode> bn = new HashSet<>();
			Set<Edge> ed = new HashSet<>();

			bn.addAll(rdx.getNodes());
			bn.addAll(ctx.getNodes());
			ed.addAll(rdx.getEdges());
			ed.addAll(ctx.getEdges());

			Set<LinkGraphFacet> lf = new HashSet<>();

			if (ok) {
				for (Bigraph arg : args) {
					if (!ok)
						break;
					// check signatures
					ok &=  rdx.getSignature().equals(arg.getSignature());
					// check support disjointness
					ok &= Collections.disjoint(bn, arg.getNodes());
					ok &= Collections.disjoint(ed, arg.getEdges());
					if (!ok)
						break;
					bn.addAll(arg.getNodes());
					ed.addAll(arg.getEdges());
					// check disjointness of outer faces
					for (LinkGraphFacet f : arg.getOuterFace().getNames()) {
						if (!ok)
							break;
						ok &= lf.add(f);
					}
				}
			}
			if (ok)
				ok &= rdx.getInnerFace().getNames().equals(lf);
			if (ok) {
				lf.clear();
				for (Bigraph arg : args) {
					if (!ok)
						break;
					for (LinkGraphFacet f : arg.getInnerFace().getNames()) {
						// check disjointness of inner faces
						if (!ok)
							break;
						ok &= lf.add(f);
					}
				}
			}
			if (!ok)
				throw new IllegalArgumentException(
						"Context, redex and parameters should yeld a bigraph if composed");
		}
		//this._copyOnWrite = copyOnWrite;
		this._ctx = ctx;
		this._rdx = rdx;
		this._args.addAll(args);
	}

	/*
	private void copyOnWrite() {
		if (!_copyOnWrite || _cloned)
			return;
		_ctx = _ctx.clone();
		_rdx = _rdx.clone();
		List<Bigraph> bs = new LinkedList<>();
		for (Bigraph arg : _args) {
			bs.add(arg.clone());
		}
		_args.clear();
		_args.addAll(bs);
		_ctx_view = null;
		_rdx_view = null;
		_args_view = null;
	}

	protected List<Bigraph> getArgs() {
		copyOnWrite();
		return Collections.unmodifiableList(_args);
	}
	*/

	protected Bigraph getArg(int index) {
		//copyOnWrite();
		return _args.get(index);
	}
	
	protected Bigraph getContext() {
		//copyOnWrite();
		return _ctx;
	}

	protected Bigraph getRedex() {
		//copyOnWrite();
		return _rdx;
	}

	public BigraphView getContextView() {
		if (_ctx_view == null)
			_ctx_view = new BigraphView(_ctx);
		return _ctx_view;
	}

	public BigraphView getRedexView() {
		if (_rdx_view == null)
			_rdx_view = new BigraphView(_rdx);
		return _rdx_view;
	}

	public List<BigraphView> getArgViews() {
		if (_args_view == null) {
			_args_view = new LinkedList<>();
			for (Bigraph arg : _args) {
				_args_view.add(new BigraphView(arg));
			}
			_args_view = Collections.unmodifiableList(_args_view);
		}
		return _args_view;
	}

	public BigraphView getArgView(int index) {
		return getArgViews().get(index);
	}

}
