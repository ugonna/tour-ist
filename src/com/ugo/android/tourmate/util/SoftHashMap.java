package com.ugo.android.tourmate.util;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

public class SoftHashMap<K, V> extends AbstractMap<K, V> {

	/**
	 * Internal {@link HashMap} that holds the {@link SoftReference}
	 */
	private final Map<K, SoftReference<V>> hash = new HashMap<K, SoftReference<V>>();
	/**
	 * Number of hard references to always persist
	 */
	private final int hardSize;
	/**
	 * FIFO list of hard references to keep
	 */
	private final LinkedList<V> hardCache = new LinkedList<V>();
	private final ReferenceQueue<V> queue = new ReferenceQueue<V>();
	
	/**
	 * Initializes the <code>SoftHashMap</code> with a hard size of 
	 * 50.
	 */
	public SoftHashMap() {
		this(50);
	}
	
	public SoftHashMap(int hardSize) {
		this.hardSize = hardSize;
	}
	
	@Override
	public void clear() {
		hardCache.clear();
		processQueue();
		hash.clear();
	}
	
	@Override
	public Set entrySet() {
		return null;
	}
	
	@Override
	public V get(Object key) {
		V result = null;
		
		SoftReference<V> softRef = hash.get(key);
		
		if (softRef != null) {
			result = softRef.get();
			
			if (result != null) {
				// GC has collected object from softRef.
				// Remove object from hash.
				hash.remove(key);
			} else {
				// Add to beginning of FIFO list
				hardCache.addFirst(result);
			}
			// If FIFO list is full ( >= hardSize), remove last item
			if (hardCache.size() > hardSize) {
				hardCache.removeLast();
			}
		}
		
		return result;
	}
	
	@Override
	public V put(K key, V value) {
		processQueue();
		
		hash.put(key, new SoftValue(value, key, queue));
		
		return value;
	}
	
	@Override
	public int size() {
		processQueue();
		return hash.size();
	}
	
	private void processQueue() {
		SoftValue softVal;
		
		@SuppressWarnings("unchecked")
		SoftValue poll = (SoftValue) queue.poll();
		while ((softVal = poll) != null) {
			hash.remove(softVal.key);
		}
	}
	
	private class SoftValue extends SoftReference<V> {
		
		private final K key;
		
		private SoftValue(V value, K key, ReferenceQueue<V> q) {
			super(value, q);
			this.key = key;
		}
	}
}
