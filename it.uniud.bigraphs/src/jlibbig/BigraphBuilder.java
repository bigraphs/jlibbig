package jlibbig;

public class BigraphBuilder {
	//TODO everything!
	
	
	public static BigraphAbst fromString(){
		//TODO parse BigMC language
		throw new UnsupportedOperationException("Not implemented yet");
	}
	
	public static BigraphAbst fromString(Signature<BigraphControl> s){
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
		
		protected BGControl(int arity){
			super("C_" + generateName());
			this.arity = arity;
		}
		
		protected BGControl(String name, int arity){
			super(name);
			this.arity = arity;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + arity;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			BGControl other = (BGControl) obj;
			if (arity != other.arity || super.getName() != other.getName())
				return false;
			return true;
		}

		private final int arity;
		
		@Override
		public String toString() {
			return getName() + ":" + arity;
		}

		@Override
		public int getArity() {
			return arity;
		}
		
	}
}
