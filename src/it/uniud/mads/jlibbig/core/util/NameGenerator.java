/**
 * 
 */
package it.uniud.mads.jlibbig.core.util;

import java.math.BigInteger;

public class NameGenerator {

	private final static boolean DEBUG_NAME_GEN = true; //Boolean.getBoolean("it.uniud.mads.jlibbig.namegeneration");
	
	public static final NameGenerator DEFAULT = new NameGenerator();

	protected static final long MIN_BLOCK_SIZE = 1000L;
	protected static final long MAX_BLOCK_SIZE = 1000000L;
	
	private BigInteger _sharedCounter = BigInteger.ZERO; 
	
	private final ThreadLocal<Block> _localBlock = new ThreadLocal<Block>(){
		@Override
		protected Block initialValue(){
			return createLocalBlock();
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
	
	protected Block createLocalBlock(){
		return new Block();
	}
	
	protected class Block{
		
		private static final long SHRINK_FACTOR = 2;
		private static final long GROW_FACTOR = 2;
		
		private long _rem = 0;
		private long _currentSize = MIN_BLOCK_SIZE*2;
		private BigInteger _blockSize = BigInteger.valueOf(_currentSize);
		private long _lastGenT = 0;
		private int _genSinceLastSizeCng = -1;
		private BigInteger _next = null;
		
		Block(){}
		
		public long getSize(){
			return this._currentSize;
		}
		
		protected long onRefilling(long oldBlockSize){
			long now = System.currentTimeMillis();
			long newBlockSize = oldBlockSize;
			if(now - _lastGenT < 1000 && newBlockSize * GROW_FACTOR <= MAX_BLOCK_SIZE){
				newBlockSize *= GROW_FACTOR;
			}else if(_genSinceLastSizeCng > 5 && newBlockSize / SHRINK_FACTOR >= MIN_BLOCK_SIZE){
				newBlockSize /= SHRINK_FACTOR;
			}
			return newBlockSize;
		}
				
		private void refill(){
			long newBlockSize = onRefilling(this._currentSize);
			if(newBlockSize != _currentSize && newBlockSize >= MIN_BLOCK_SIZE){// && newBlockSize <= MAX_BLOCK_SIZE ){
				_currentSize = newBlockSize;
				_genSinceLastSizeCng = 0;
				_blockSize = BigInteger.valueOf(_currentSize);
			}else{
				_lastGenT = System.currentTimeMillis();
				_genSinceLastSizeCng += 1;
			}
			_next = getNewBlock(_blockSize);
			_rem = _currentSize;
		}
		
		public String next(){
			if(_rem < 1){
				refill();
			}
			_rem -= 1;
			String name = this._next.toString(16).toUpperCase();
			this._next = this._next.add(BigInteger.ONE);
			return name;
		}
	}
}
