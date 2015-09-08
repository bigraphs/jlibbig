/**
 * 
 */
package it.uniud.mads.jlibbig.core.util;

import java.math.BigInteger;

public class NameGenerator {

	private final static boolean DEBUG_NAME_GEN = Boolean.getBoolean("it.uniud.mads.jlibbig.namegeneration");
	
	public static final NameGenerator DEFAULT = new NameGenerator();
	
	private BigInteger _sharedCounter = BigInteger.ZERO; 
	
	private final ThreadLocal<Block> _localBlock = new ThreadLocal<Block>(){
		@Override
		protected Block initialValue(){
			return new Block();
		}
	};
	
	NameGenerator(){}
	
	public String generate(){
		return this._localBlock.get().next();
	}
		
	private BigInteger getNewBlock(BigInteger size){
		BigInteger block;
		synchronized(this){
			if(DEBUG_NAME_GEN){
				System.out.println("Allocating a new block of size " + size.toString());
			}
			block = this._sharedCounter;
			this._sharedCounter = block.add(size);
		}
		return block;
	}
	
	private class Block{
		private long _rem = 0;
		private long _blockSizeL = 2000;
		private BigInteger _blockSize = BigInteger.valueOf(_blockSizeL);
//		private long _lastGen = 0;
		private BigInteger _next = null;
		
		Block(){}
				
		public String next(){
			if(_rem < 1){
//				long n = System.currentTimeMillis();
				_next = getNewBlock(_blockSize);
				_rem = _blockSizeL;
//				_lastGen = n;
			}
			_rem -= 1;
			String name = this._next.toString(16).toUpperCase();
			this._next = this._next.add(BigInteger.ONE);
			return name;
		}
		
	}
}
