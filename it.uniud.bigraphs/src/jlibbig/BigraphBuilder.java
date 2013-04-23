package jlibbig;

public class BigraphBuilder {
	//TODO everything!
	
	
	public static Bigraph fromString(){
		//TODO parse BigMC language
		throw new UnsupportedOperationException("Not implemented yet");
	}
	
	public static Bigraph fromString(Signature<BigraphControl> s){
		//TODO parse BigMC language
		throw new UnsupportedOperationException("Not implemented yet");
	}
	
	public static BigraphControl makeControl(int arity){
		return new BGControl(arity);
	}
	public static BigraphControl makeControl(String name, int arity){
		return new BGControl(name,arity);
	}
	
	private static class BGControl extends Named implements BigraphControl{

		private final int arity;
		
		@Override
		public String toString() {
			return getName() + ":" + arity;
		}

		protected BGControl(int arity){
			super();
			this.arity = arity;
		}
		
		protected BGControl(String name, int arity){
			super(name);
			this.arity = arity;
		}

		@Override
		public int getArity() {
			return arity;
		}
		
	}
}
