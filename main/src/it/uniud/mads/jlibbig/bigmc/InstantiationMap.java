package it.uniud.mads.jlibbig.bigmc;

import java.util.*;

import it.uniud.mads.jlibbig.core.*;
import it.uniud.mads.jlibbig.core.std.Bigraph;
import it.uniud.mads.jlibbig.core.std.BigraphInstantiationMap;

public class InstantiationMap implements InstantiationRule<AgentBigraph> {

	private final BigraphInstantiationMap eta;

	InstantiationMap(int codomain, int... map) {
		eta = new BigraphInstantiationMap(codomain, map);
	}

	public int getPlaceDomain() {
		return eta.getPlaceDomain();
	}

	public int getPlaceCodomain() {
		return eta.getPlaceCodomain();
	}

	public int getPlaceInstance(int arg) {
		return eta.getPlaceInstance(arg);
	}

	@Override
	public Iterable<? extends AgentBigraph> instantiate(
			final AgentBigraph parameters) {
		return new Iterable<AgentBigraph>() {
			private final Iterable<Bigraph> able = eta
					.instantiate(parameters.bigraph);

			@Override
			public Iterator<AgentBigraph> iterator() {
				return new Iterator<AgentBigraph>() {
					final Iterator<Bigraph> tor = able.iterator();

					@Override
					public boolean hasNext() {
						return tor.hasNext();
					}

					@Override
					public AgentBigraph next() {
						if (hasNext()) {
							return new AgentBigraph(tor.next());
						} else {
							return null;
						}
					}

					@Override
					public void remove() {
						tor.remove();
					}

				};
			}

		};
	}

}
