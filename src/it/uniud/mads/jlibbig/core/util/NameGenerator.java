/**
 * 
 */
package it.uniud.mads.jlibbig.core.util;
import java.lang.ref.SoftReference;

import java.math.BigInteger;

public class NameGenerator {

	private final static boolean DEBUG = true; //Boolean.getBoolean("it.uniud.mads.jlibbig.namegeneration");
	
	public static final NameGenerator DEFAULT = new NameGenerator();

	private BigInteger _sharedCounter = BigInteger.ZERO; 
	
	private final ThreadLocal<SoftReference<Block>> _localBlock = new ThreadLocal<SoftReference<Block>>(){
		@Override
		protected SoftReference<Block> initialValue(){
			return null;
		}
	};
	
	NameGenerator(){}
	
	public String generate(){
		SoftReference<Block> ref = this._localBlock.get();
		if(ref == null){
			if(DEBUG){
				System.out.println("New local block for thread " + Thread.currentThread().getId() + "-" + Thread.currentThread().getName());
			}
			ref = new SoftReference<>(createLocalBlock());
			this._localBlock.set(ref);
		}
		return ref.get().next();
	}
		
	private BigInteger getNewBlock(BigInteger size){
		BigInteger block;
		synchronized(this){
			if(DEBUG){
				System.out.println("Allocating a new block of size " + size.toString() + " for thread " + Thread.currentThread().getId() + "-" + Thread.currentThread().getName());
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
		
		private static final long MIN_BLOCK_SIZE = 1000L;
		private static final long MAX_BLOCK_SIZE = 100000L;
			
		private static final long SHRINK_FACTOR = 2;
		private static final long GROW_FACTOR = 2;
		private static final long TIME_THRESHOLD = 1000; //milliseconds between block refills, grow if below
		private static final long FREQ_THRESHOLD = 5; //number of refills since last size change, shrink if above
		
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
			if(now - _lastGenT < TIME_THRESHOLD && newBlockSize * GROW_FACTOR <= MAX_BLOCK_SIZE){
				newBlockSize *= GROW_FACTOR;
			}else if(_genSinceLastSizeCng > FREQ_THRESHOLD && newBlockSize / SHRINK_FACTOR >= MIN_BLOCK_SIZE){
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