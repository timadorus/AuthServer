/* -*- java -*- */
/*
 * Filename:          org.timadorus.auth.cache.MaxSizeCacheMap.java
 *                                                                       *
 * Project:           TimadorusAuthServer
 *
 * This file is distributed under the GNU Public License 2.0
 * See the file Copying for more information
 *
 * copyright (c) 2010 Lutz Behnke <lutz.behnke@gmx.de>
 *
 * THE AUTHOR MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE
 * SUITABILITY OF THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR
 * A PARTICULAR PURPOSE, OR NON-INFRINGEMENT. THE AUTHOR SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING,
 * MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */

package org.timadorus.auth.cache;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * @author sage
 *
 */
public class MaxSizeCacheMap<K, V> implements Map<K, V> {

  private HashMap<K, V> map;
  private LinkedList<K> order;
  int maxSize;
  
  public MaxSizeCacheMap(int size) {
    map = new HashMap<K, V>(size);
    order = new LinkedList<K>();
    maxSize = size;
  }

  @Override
  public void clear() {
    map.clear();
    order.clear();
  }

  @Override
  public boolean containsKey(Object key) {
    return map.containsKey(key);
  }

  @Override
  public boolean containsValue(Object value) {
    return map.containsValue(value);
  }

  @Override
  public Set<java.util.Map.Entry<K, V>> entrySet() {
    return map.entrySet();
  }

  @Override
  public V get(Object key) {
    return map.get(key);
  }

  @Override
  public boolean isEmpty() {
    return map.isEmpty();
  }

  @Override
  public Set<K> keySet() {
    return map.keySet();
  }

  @Override
  public V put(K key, V value) {

    if (map.containsKey(key)) {
      return map.put(key, value);
    }
    
    if (order.size() <= maxSize) { 
      order.add(key);
      return map.put(key, value);
    }
    
    K oldKey = order.pollFirst();
    V oldVal = map.remove(oldKey);
    order.add(key);
    map.put(key, value);
    return oldVal;
  }

  @Override
  public void putAll(Map<? extends K, ? extends V> m) {
    for (Entry<? extends K, ? extends V> entry : m.entrySet()) {
      put(entry.getKey(), entry.getValue());
    }
  }

  @Override
  public V remove(Object key) {
    int pos = order.indexOf(key);
    if (pos == -1) { return null; }
    order.remove(pos);
    return map.remove(key); 
  }

  @Override
  public int size() {
    return map.size();
  }

  @Override
  public Collection<V> values() {
    return map.values();
  }
}
