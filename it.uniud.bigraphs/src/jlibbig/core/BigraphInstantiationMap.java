package jlibbig.core;

public class BigraphInstantiationMap implements InstantiationRule<Bigraph>{
	final private int map[];
	final private int dom;
	final private int cod;
	
	final private boolean[] neededParam;
	
	public BigraphInstantiationMap(int codomain, int... map) {
		dom = map.length;
		cod = codomain--;
		this.map = new int[dom];
		neededParam = new boolean[cod];
		for(int i = 0;i< map.length;i++){
			if(map[i] < 0 || map[i] > codomain){
				throw new IllegalArgumentException("Invalid image");
			}
			this.map[i] = map[i];
			neededParam[map[i]] = true;
		}
	}

	public int getPlaceDomain() {
		return dom;
	}

	public int getPlaceCodomain() {
		return cod;
	}

	public int getPlaceInstance(int arg) {
		if(-1 < arg && arg < dom){
			return map[arg];
		}else{
			return -1;
		}
	}
	
	public boolean isNeeded(int prm){
		return -1 < prm  && prm < cod && neededParam[prm];
	}

	@Override
	public Iterable<Bigraph> instantiate(Bigraph parameters) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented yet.");
	}

}
