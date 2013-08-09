package jlibbig.core;

public class BigraphInstantiationMap implements InstantiationRule<Bigraph>{
	private int map[];
	private int dom;
	private int cod;
	
	public BigraphInstantiationMap(int... map) {
		dom = map.length;
		cod = 0;
		this.map = new int[dom];
		for(int i = 0;i< map.length;i++){
			if(map[i] < 0){
				throw new IllegalArgumentException("Invalid image");
			}else if(map[i] > cod){
				cod = map[i];
			}
			this.map[i] = map[i];
		}
	}

	public int getPlaceDomain() {
		return dom;
	}

	public int getPlaceCodomain() {
		return cod;
	}

	public int getPlaceInstance(int arg) {
		if(arg > 0 && arg < dom){
			return map[arg];
		}else{
			return -1;
		}
	}

	@Override
	public Iterable<Bigraph> instantiate(Bigraph parameters) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented yet.");
	}

}
