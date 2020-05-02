/**
 * The class acts as cache to store the outcome of some generator method.
 * The cached data can be garbage collected at any time and be re-computed
 * if needed.   
 */
package it.uniud.mads.jlibbig.core.util;

import java.lang.ref.SoftReference;

public class CachingProxy<T> {
	
	private SoftReference<T> ref;
	
	private Provider<T> provider;
	
	/**
	 * @param provider The provider to be used to populate the cache.
	 */
	public CachingProxy(Provider<T> provider) {
		super();
		this.setProvider(provider);
	}
	
	/**
	 * Returns the instance of the data provider in use.
	 * @return the provider
	 */
	public Provider<T> getProvider() {
		return provider;
	}

	/**
	 * Sets a new provider and invalidate the current cache.
	 * @param p the provider to be used henceforth.
	 */
	public void setProvider(Provider<T> p) {
		if(p==null){
			throw new IllegalArgumentException("The value cannot be null.");
		}
		this.provider = p;
		this.invalidate();
	} 
	
	/**
	 * Invalidates the cache.
	 */
	public void invalidate(){
		SoftReference<T> old = this.ref;
		this.ref = null;
		if(old!=null){
			old.clear();
		}
	}
	
	public T get() {
		T value = softGet();
		if(value==null){
			value = populateCache();
		}
		return value;
	}
	
	public T softGet(){
		T value = null;
		if(ref != null){
			value = ref.get();
		}
		return value;
	}
	
	private T populateCache(){
		T value = this.provider.get();
		this.ref = new SoftReference<T>(value);
		return value;
	}
}
