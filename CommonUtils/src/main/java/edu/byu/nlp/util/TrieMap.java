/**
 * Copyright 2011 Brigham Young University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.byu.nlp.util;

import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * A map backed by a trie. Can lead to space and time savings for
 * some operations on strings. Note the addition of getUnambiguousPrefix()
 * containsKeyPrefix() and containsUnambiguousKeyPrefix().
 * 
 * Null keys are not permitted but null values are allowed.
 *
 * The empty string must be explicitly put as a key in order to be returned
 * in iterators and in the contains methods.
 *  
 * @author rah67
 *
 */
public class TrieMap<V> extends AbstractMap<CharSequence, V> 
	implements Serializable, Map<CharSequence,V>{

	private static final long serialVersionUID = 1L;

	/**
	 * This is a single node in a trie. Each node can have children,
	 * and a value. The "present" flag is used mostly so that if 
	 * .remove is called while iterating over an entrySet(), the
	 * removed entry is guaranteed to have the correct value.
	 * It was helpful, but not necessary, to allow storage
	 * of null values.
	 * 
	 * @author rah67
	 *
	 * @param <V>
	 */
	private static final class TrieNode<V> implements Serializable {
		
		private static final long serialVersionUID = 1L;
		
		// TODO : consider using Trove's map
		private final Map<Character,TrieNode<V>> children;
		private V value;
		private boolean present;

		public TrieNode(V value, boolean present) {
			this.children = new HashMap<Character, TrieNode<V>>();
			this.value = value;
			this.present = present;
		}
		
		public TrieNode(boolean present) {
			this(null, present);
		}
		
		public final V getValue() {
			return value;
		}
		
		public final void setValue(V value) {
			this.value = value;
		}

		/**
		 * Accounts for nulls
		 * 
		 * @param value
		 * @return
		 */
		public final boolean valueEquals(V value) {
			if (this.value == null)
				return value == null;
			return value.equals(value);
		}
		
		public final boolean containsKey() {
			return present;
		}
		
		public void removeData() {
			present = false;
		}

		public final boolean hasChildren() {
			return children.size() > 0;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("{");
			sb.append(children.keySet());
			sb.append(", ");
			sb.append(value);
			sb.append(", ");
			sb.append(present);
			sb.append("}");
			return sb.toString();
		}
		
	}
	
	// Note about tree structure
	// The Trie has a root node. Each edge is labelled with
	// the character needed to reach the next node.
	// The value is stored in the node connected to the
	// "lower" end of edge of the last character
	// Consider a trie with the strings "a","abc", and "abcde".
	// the trie would look like (root)--a--(VAL_A)--b--()--c--(VAL_ABC)--d--()--e--(VAL_ABCDE)
	// where () indicate nodes by their value (if there is one, and --X-- indicates an edge
	// labeled X.
	
	private final TrieNode<V> root;		// the root of the tree
	private int size;					
	private transient int modCount;		// used to detect concurrent modification in iterators
	
	/**
	 * Constructs an empty trie-backed map
	 */
	public TrieMap() {
		this.root = new TrieNode<V>(false);
		this.size = 0;
		this.modCount = 0;
	}
	
	/**
	 * Constructs a map and fills it with the values from the provided map.
	 * The copy is shallow.
	 * 
	 * @param map the map to initialize this trie with
	 */
	public TrieMap(Map<CharSequence, ? extends V> map) {
		this();
		putAllForCreate(map);
	}
	
/*
 	// Was being used with the subTrie stuff
  	protected TrieMap(TrieNode<V> root) {
		this.root = root;
	}
*/
	
	// TODO : could implement a constructor that is passed
	// a TrieMap perhaps more efficiently than any old map
	
	/**
	 * Should avoid overhead of checking for comodifiaciton, but currently
	 * does not.
	 * 
	 * @param map the map to initialize this trie with
	 * @see TrieMap#TrieMap(Map)
	 */
	private void putAllForCreate(Map<CharSequence, ? extends V> map) {
		for( Entry<CharSequence, ? extends V> e : map.entrySet()) {
			putForCreate(e.getKey(), e.getValue());
		}
	}

	/**
	 * Shouldn't check for comodification, etc., but currently just defers to put
	 * 
	 * @param key	the key to added
	 * @param value the value to be added
	 * 
	 * @see TrieMap#putAllForCreate(Map)
	 */
	private void putForCreate(CharSequence key, V value) {
		put(key, value);
	}

    /**
     * Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for the key, the old
     * value is replaced.
     *
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with <tt>key</tt>, or
     *         <tt>null</tt> if there was no mapping for <tt>key</tt>.
     *         (A <tt>null</tt> return can also indicate that the map
     *         previously associated <tt>null</tt> with <tt>key</tt>.)
     */
	@Override
	public V put(CharSequence key, V value) {

		if (key == null) // null key
			throw new IllegalArgumentException("This map does not support null keys");
		
		V replaced = null;

		// Search the tree character by character 
		TrieNode<V> curNode = root;
		TrieNode<V> lastNode = null;
		int i = 0;
		while( i < key.length() && curNode != null ) {
			lastNode = curNode;
			Character c = key.charAt(i);
			curNode = curNode.children.get(c);
			++i;
		}
		
		if ( curNode == null ) { // not found
			curNode = lastNode;
			TrieNode<V> newNode = new TrieNode<V>(true);
			addNode(curNode, key, --i, newNode);
			modifyData(newNode, value);
		} else if ( curNode.containsKey() ) {	// string already exists, so remember existing value before overwriting
			// Since it already exists, this doesn't count as a modification and it doesn't affect the size
			replaced = curNode.getValue();
			if ( !curNode.valueEquals(value) ) {
				curNode.setValue(value);
			} 
		} else { // otherwise I am a non-existing substring of an existing entry, so my data will be added to the node
			modifyData(curNode, value);
			curNode.present = true;
		}

		return replaced;
	}

	/**
	 * Used during put to set the value of a node and update the size and modCount 
	 */
	private void modifyData(TrieNode<V> curNode, V value) {
		curNode.setValue(value);
		++modCount;
		++size;
	}

	/**
	 * Utility method to add str.substring(i,str.length) to the specified node.
	 * Also adds newNode to the end.
	 * 
	 * @param curNode the node to start with
	 * @param str the string to be added
	 * @param i the starting point within the string
	 * @param newNode the node to be associated with the string
	 */
	private void addNode(TrieNode<V> curNode, CharSequence str, int i, TrieNode<V> newNode) {
		for( ; i < str.length()-1; i++ ) {
			Character c = str.charAt(i);
			TrieNode<V> nextNode = new TrieNode<V>(false);
			curNode.children.put(c, nextNode);
			curNode = nextNode;
		}
		curNode.children.put(str.charAt(i), newNode);
	}
	
    /**
     * Returns the value to which the specified key is mapped,
     * or {@code null} if this map contains no mapping for the key.
     *
     * <p>More formally, if this map contains a mapping from a key
     * {@code k} to a value {@code v} such that {@code (key==null ? k==null :
     * key.equals(k))}, then this method returns {@code v}; otherwise
     * it returns {@code null}.  (There can be at most one such mapping.)
     *
     * <p>A return value of {@code null} does not <i>necessarily</i>
     * indicate that the map contains no mapping for the key; it's also
     * possible that the map explicitly maps the key to {@code null}.
     * The {@link #containsKey containsKey} operation may be used to
     * distinguish these two cases.
     *
     * @see #put(Object, Object)
     */
	@Override
	public V get(Object o) {
		if (!(o instanceof CharSequence))
			return null;
		CharSequence str = (CharSequence) o;
		TrieNode<V> node = findNode(str);

		return node == null ? null : node.getValue();
	}

	/**
	 * Utility method to get the node in the Trie corresponding to <tt>str</tt>
	 * 
	 * @param str the string to search for
	 * @return null if not found or the node after the edge corresponding
	 * 			to the edge of the last character in the sequence
	 * 
	 * @see #findPreviousNode(CharSequence)
	 */
	private TrieNode<V> findNode(CharSequence str) {
		TrieNode<V> curNode = root;
		
		// Iterate through each character, traversing the tree 
		for(int i = 0; i < str.length() && curNode != null; i++) {
			Character c = str.charAt(i);
			curNode = curNode.children.get(c);
		}

		return curNode;
	}

	/**
	 * If the specified prefix contains exactly one match in this trie, returns the associated entry.
	 * The returned entry is the same as those returned by <tt>entrySet()</tt>.
	 * The key of the entry is the full string.
	 * 
	 * @param prefix the prefix to search for
	 * @return null if prefix is not found or is ambiguous; the corresponding
	 * 			entry otherwise
	 * 
	 * @see #entrySet()
	 * @see #containsUnambiguousKeyPrefix(CharSequence)
	 * @see #containsKeyPrefix(CharSequence)
	 */
	public Entry<CharSequence, V> getEntryFromUnambiguousPrefix(CharSequence prefix) {
		TrieNode<V> startNode = findNode(prefix);
		
		if (startNode == null)	// no such prefix
			return null;
		
		EntryIterator it = new EntryIterator(startNode, prefix);
		
		if (!it.hasNext())	// Possible only if prefix is the null string and the Trie is empty
			return null;
		
		Entry<CharSequence, V> e = it.next();
		
		if (it.hasNext() && !e.getKey().equals(prefix))	// ambiguous!
			return null;
		
		return e;
	}
	
    /**
     * Returns <tt>true</tt> if this map contains a mapping for the
     * specified key.
     *
     * @param   key   The key whose presence in this map is to be tested
     * @return <tt>true</tt> if this map contains a mapping for the specified
     * key.
     */
	@Override
	public boolean containsKey(Object key) {
		if (!(key instanceof CharSequence))
			return false;
		TrieNode<V> node = findNode((CharSequence)key);
		return node != null && node.containsKey();
	}

    /**
     * Returns <tt>true</tt> if this map contains exactly one mapping
     * whose key begins with the specified string.
     *
     * @param   prefix   The prefix to the key whose presence in this map is to be tested
     * @return <tt>true</tt> if this map contains exactly one mapping 
     * 			whose key begins with the specified string.
     * 
     * @see #containsKeyPrefix(CharSequence)
     * @see #getEntryFromUnambiguousPrefix(CharSequence)
     */
	public boolean containsUnambiguousKeyPrefix(CharSequence prefix) {
		Entry<CharSequence, V> match = getEntryFromUnambiguousPrefix(prefix);
		return match != null;
	}

    /**
     * Returns <tt>true</tt> if this map contains at least one mapping
     * whose key begins with the specified string.
     *
     * @param   prefix   The prefix to the key whose presence in this map is to be tested
     * @return <tt>true</tt> if this map contains at least one mapping 
     * 			whose key begins with the specified string.
     */
	public boolean containsKeyPrefix(CharSequence prefix) {
		TrieNode<V> findNode = findNode(prefix);
		return findNode != null;
	}

	/**
     * Removes the mapping for the specified key from this map if present.
     *
     * @param  key key whose mapping is to be removed from the map
     * @return the previous value associated with <tt>key</tt>, or
     *         <tt>null</tt> if there was no mapping for <tt>key</tt>.
     *         (A <tt>null</tt> return can also indicate that the map
     *         previously associated <tt>null</tt> with <tt>key</tt>.)
     */
	@Override
    public V remove(Object o) {
		if (!(o instanceof CharSequence))
			return null;
		CharSequence str = (CharSequence) o;
		
		TrieNode<V> curNode = findPreviousNode(str);
		
		if (curNode == null)
			return null;
		
		TrieNode<V> dataNode = curNode.children.get(str.charAt(str.length()-1));
		
		if (dataNode == null || !dataNode.containsKey()) // not found or doesn't have data to be removed
			return null;
			
		V removed = dataNode.getValue();
		dataNode.removeData();
		--size;
		--modCount;
		
		if (!dataNode.hasChildren()) {
			eraseBackwards(str);
		} 
		return removed;
	}

	/**
	 * Finds the node touching the leading edge corresponding
	 * to the last character in the specified sequence.
	 * 
	 * @param str the sequence to search for
	 * @return the leading edge corresponding
	 * 	to the last character in the specified sequence if the 
	 *  sequence is a substring of an existing mapping; otherwise, null
	 * 
	 * @see #findNode(CharSequence)
	 */
	private TrieNode<V> findPreviousNode(CharSequence str) {
		TrieNode<V> curNode = root;
		for( int i = 0; i < str.length() - 1 && curNode != null; i++ ) {
			Character c = str.charAt(i);
			curNode = curNode.children.get(c);
		}
		return curNode;
	}

	/**
	 * Special version for entrySet that only removes if key and value match
	 * 
	 * @param o
	 * @return
	 */
    @SuppressWarnings("unchecked")
	final V removeMapping(Object o) {
    	
		if (!(o instanceof Map.Entry))
			return null;
		Entry<? extends CharSequence, V> e = (Map.Entry<? extends CharSequence, V>) o;
		if (!(e.getKey() instanceof CharSequence))
			return null;

		CharSequence str = e.getKey();
		TrieNode<V> prevNode = findPreviousNode(str);
		
		if (prevNode == null)
			return null;
		
		TrieNode<V> dataNode = prevNode.children.get(str.charAt(str.length()-1));
		
		if (dataNode == null || !dataNode.containsKey()) // not found or doesn't have data to be removed
			return null;
		
		// The extra important bit that requires we even have removeMapping method
		if (!dataNode.getValue().equals(e.getValue()))
			return null;
		
		V removed = dataNode.getValue();
		dataNode.removeData();
		--size;
		--modCount;
		
		if (!dataNode.hasChildren()) {
			eraseBackwards(str);
		} 
		return removed;
    }

    /**
     * Removes all of the mappings from this map.
     * The map will be empty after this call returns.
     */
	@Override
	public void clear() {
		root.children.clear();
		root.value = null;
		root.removeData();
		++modCount;
		size = 0;
	}

	/**
	 * After removal, it's desirable to do some cleaning up when possible.
	 * However, since we don't have a doubly-linked list, it's necessary
	 * to start from root and figure out which is the last node with data
	 * and then remove all nodes afterwards.
	 * 
	 * Although this requires traversing the Trie again, we assume that
	 * the goals are low memory usage and few removals.
	 * 
	 * This method does not ensure that it is okay to remove str.
	 * 
	 * @param str the string to be removed from the map.
	 */
	private void eraseBackwards(CharSequence str) {
		
		// Iterate through, keeping track of the previous node that has data
		TrieNode<V> curNode = root;
		TrieNode<V> lastNodeWithData = root;		// This is necessary for removing the first level
		int lastIndexWithData = 0;
		
		for(int i = 0; i < str.length() && curNode != null; i++) {
			Character c = str.charAt(i);
			
			if (curNode.containsKey()) {
				lastNodeWithData = curNode;
				lastIndexWithData = i;
			}
			
			curNode = curNode.children.get(c);
		}

		if (curNode == null)
			throw new IllegalArgumentException("Can only erase leaf nodes or node not found");
		
		// Starting from the last node with data, erase the children
		curNode = lastNodeWithData;
		TrieNode<V> nextNode;
		for( int i = lastIndexWithData; i < str.length(); i++ ) {
			nextNode = curNode.children.remove(str.charAt(i));
			nextNode.removeData();
			curNode = nextNode;
		}
	}

    /**
     * Returns the number of key-value mappings in this map.
     *
     * @return the number of key-value mappings in this map
     */
	@Override
	public int size() {
		return size;
	}

    /**
     * Returns <tt>true</tt> if this map contains no key-value mappings.
     *
     * @return <tt>true</tt> if this map contains no key-value mappings
     */
	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return super.isEmpty();
	}



	// Iterators


	/**
	 * This class facilitates <tt>KeySet</tt>, <tt>Values</tt>, and <tt>EntrySet</tt>
	 * 
	 * @author rah67
	 *
	 * @param <E>
	 */
	private abstract class TrieIterator<E> implements Iterator<E> {
		
		/**
		 * An implementation of Entry that is a view over the backing trie
		 * 
		 * @author rah67
		 *
		 */
		private final class TrieEntry implements Entry<CharSequence, V> {
			
			private final CharSequence key;
			private final TrieNode<V> node;
			
			public TrieEntry(CharSequence key, TrieNode<V> node) {
				this.key = key;
				this.node = node;
			}

			@Override
			public final CharSequence getKey() {
				return key;
			}

			@Override
			public final V getValue() {
				return node.getValue();
			}

			@Override
			public final V setValue(V value) {
				V old = node.getValue();
				node.setValue(value);
				return old;
			}

			@Override
			public boolean equals(Object o) {
			    if (!(o instanceof Map.Entry))
				return false;
			    
			    @SuppressWarnings("rawtypes")	// necessary for the cast to Entry 
				Map.Entry e = (Map.Entry)o;
			    
			    return eq(key, e.getKey()) && eq(getValue(), e.getValue());
			}

			@Override
			public int hashCode() {
				V value = getValue();
			    return (key   == null ? 0 :   key.hashCode()) ^
				   (value == null ? 0 : value.hashCode());
			}

			@Override
			public String toString() {
				return key + " => " + getValue();
			}

			private final boolean eq(Object o1, Object o2) {
			    return o1 == null ? o2 == null : o1.equals(o2);
			}
		}

		protected int expectedModCount;		// for fast-fail
		private final Deque<Pair<CharSequence,TrieNode<V>>> stack;	// for dfs
		private Entry<CharSequence,V> next;		// next entry to return
		private Entry<CharSequence,V> current;	// current entry
		
		public TrieIterator() {
			this(root,"");
		}
		
		/**
		 * @param startNode the node at which to start iteration
		 * @param prefix the prefix corresponding the the startNode
		 */
		public TrieIterator(TrieNode<V> startNode, CharSequence prefix) {
			expectedModCount = modCount;
			stack = new ArrayDeque<Pair<CharSequence,TrieNode<V>>>();
			stack.add(new Pair<CharSequence, TrieNode<V>>(prefix, startNode));
			advance();
		}
		
		private void advance() {
			next = null;
			while (next == null && !stack.isEmpty()) {
				final Pair<CharSequence, TrieNode<V>> pair = stack.removeFirst();
				
				CharSequence key = pair.getFirst();
				TrieNode<V> node = pair.getSecond();
				
				if (node.containsKey()) 
					next = new TrieEntry(key, node);
				
				for(Entry<Character, TrieNode<V>> entry : node.children.entrySet()) {
					StringBuilder sb = new StringBuilder(key);
					sb.append(entry.getKey());
					stack.addFirst(new Pair<CharSequence, TrieNode<V>>(sb.toString(), entry.getValue()));
				}
			}
		}
		
		@Override
		public boolean hasNext() {
			return next != null;
		}

		public Entry<CharSequence,V> nextEntry() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();

            current = next;
            if (current == null)
                throw new NoSuchElementException();

            advance();
            
            return current;
		}

		@Override
		public void remove() {
			if (current == null)
				throw new IllegalStateException();
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            Object k = current.getKey();
			TrieMap.this.remove(k);
			expectedModCount = modCount;
		}
		
	}
	
	private final class KeyIterator extends TrieIterator<CharSequence> {
		@Override
		public CharSequence next() {
			return nextEntry().getKey();
		}
	}
	
	private final class EntryIterator extends TrieIterator<Entry<CharSequence,V>> {
		public EntryIterator(){}
		
		public EntryIterator(TrieNode<V> startNode, CharSequence prefix) {
			super(startNode, prefix);
		}

		@Override
		public Entry<CharSequence, V> next() {
			return nextEntry();
		}
	}
	
	private final class ValueIterator extends TrieIterator<V> {
		@Override
		public V next() {
			return nextEntry().getValue();
		}
	}

	
	// Views

	// Too bad Sun declared these at package protected in AbstractMap
	transient Set<CharSequence> keySet;
	transient Collection<V> values;
	
    /**
     * Returns a {@link Set} view of the keys contained in this map.
     * The set is backed by the map, so changes to the map are
     * reflected in the set, and vice-versa.  If the map is modified
     * while an iteration over the set is in progress (except through
     * the iterator's own <tt>remove</tt> operation), the results of
     * the iteration are undefined.  The set supports element removal,
     * which removes the corresponding mapping from the map, via the
     * <tt>Iterator.remove</tt>, <tt>Set.remove</tt>,
     * <tt>removeAll</tt>, <tt>retainAll</tt>, and <tt>clear</tt>
     * operations.  It does not support the <tt>add</tt> or <tt>addAll</tt>
     * operations.
     */
	@Override
	public Set<CharSequence> keySet() {
		Set<CharSequence> ks = keySet;
		return ks != null ? ks : (keySet = new KeySet());
	}

	private final class KeySet extends AbstractSet<CharSequence> {
        
		@Override
		public Iterator<CharSequence> iterator() {
            return new KeyIterator();
        }
        
        @Override
		public int size() {
            return TrieMap.this.size();
        }
        
        @Override
		public boolean contains(Object o) {
            return containsKey(o);
        }
        
        @Override
		public boolean remove(Object o) {
            return TrieMap.this.remove(o) != null;
        }
        
        @Override
		public void clear() {
            TrieMap.this.clear();
        }
	}

	private transient Set<Entry<CharSequence, V>> entrySet;
	@Override
	public Set<Entry<CharSequence, V>> entrySet() {
		Set<Entry<CharSequence, V>> es = entrySet;
		return es != null ? es : (entrySet = new EntrySet());
	}

    /**
     * Returns a {@link Set} view of the mappings contained in this map.
     * The set is backed by the map, so changes to the map are
     * reflected in the set, and vice-versa.  If the map is modified
     * while an iteration over the set is in progress (except through
     * the iterator's own <tt>remove</tt> operation, or through the
     * <tt>setValue</tt> operation on a map entry returned by the
     * iterator) the results of the iteration are undefined.  The set
     * supports element removal, which removes the corresponding
     * mapping from the map, via the <tt>Iterator.remove</tt>,
     * <tt>Set.remove</tt>, <tt>removeAll</tt>, <tt>retainAll</tt> and
     * <tt>clear</tt> operations.  It does not support the
     * <tt>add</tt> or <tt>addAll</tt> operations.
     *
     * @return a set view of the mappings contained in this map
     */
	private final class EntrySet extends AbstractSet<Entry<CharSequence, V>> {

		@Override
		public void clear() {
			TrieMap.this.clear();
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean contains(Object o) {
            if (!(o instanceof Map.Entry))
                return false;

            Map.Entry<CharSequence,V> e = (Map.Entry<CharSequence,V>) o;
            V candidate = get(e.getKey());
            return candidate != null && candidate.equals(e.getValue());
		}

		@Override
		public Iterator<Entry<CharSequence, V>> iterator() {
			return new EntryIterator();
		}

		@Override
		public boolean remove(Object o) {
			return removeMapping(o) != null;
		}

		@Override
		public int size() {
            return TrieMap.this.size();
		}
	}
	
    /**
     * Returns a {@link Collection} view of the values contained in this map.
     * The collection is backed by the map, so changes to the map are
     * reflected in the collection, and vice-versa.  If the map is
     * modified while an iteration over the collection is in progress
     * (except through the iterator's own <tt>remove</tt> operation),
     * the results of the iteration are undefined.  The collection
     * supports element removal, which removes the corresponding
     * mapping from the map, via the <tt>Iterator.remove</tt>,
     * <tt>Collection.remove</tt>, <tt>removeAll</tt>,
     * <tt>retainAll</tt> and <tt>clear</tt> operations.  It does not
     * support the <tt>add</tt> or <tt>addAll</tt> operations.
     */
	@Override
	public Collection<V> values() {
		Collection<V> v = values;
		return v != null ? v : (values = new Values());
	}
	
	private final class Values extends AbstractCollection<V> {

		@Override
		public void clear() {
			TrieMap.this.clear();
		}

		@Override
		public boolean contains(Object o) {
			return containsValue(o);
		}

		@Override
		public Iterator<V> iterator() {
			return new ValueIterator();
		}

		@Override
		public int size() {
            return TrieMap.this.size();
		}
		
	}

	/*
	 * FIXME : too many issues to flesh out now, but shouldn't be hard
	private final class SubTrieMap extends TrieMap<V> {
		
		public SubTrieMap(final TrieNode<V> node, CharSequence prefix) {
			super(prefix.equals("") ? node : new TrieNode<V>());
			// The easiest thing to do is create a new root
			if (!prefix.equals(""))
				// And attach the "rest" of the exisiting trie to the end
				addNode(this.root,prefix,0,node);
		}

		@Override
		public int size() {
			return entrySet().size();
		}
		
	}
	 */
	
	/**
	 * Size is linear rather than constant time.
	 * If this matters, then copy the subTrie via the copy constructor of TrieMap.
	 * 
	 * Returns null if the prefix doesn't currently exist (this feature could be added)
	 * 
	 * Known Issues:
	 * 	the values(), entrySet(), and keySet() on any subTrie are NOT fail-safe
	 * 
	 * @param prefix
	 * @return
	 */
	/*
	 * FIXME : too many issues to flesh out now, but shouldn't be hard
	public TrieMap<V> subTrie(CharSequence prefix) {
		TrieNode<V> node = findNode(prefix);
		if (node == null)
			return null;
		return new SubTrieMap(node, prefix);
	}
	*/
	
}
