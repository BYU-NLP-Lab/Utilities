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
import java.util.AbstractSet;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * A set backed by a trie. Can lead to space savings and time savings for
 * some operations on sets of strings. Note the addition of the
 * containsPrefix and containsUnambiguousPrefix methods.
 * 
 * The empty string must be explicitly added to be returned
 * in iterators and in the contains methods. 
 * 
 * @author rah67
 *
 */
public class TrieSet extends AbstractSet<CharSequence> implements Set<CharSequence>, Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private static final Object PRESENT = new Object();
	
	private Map<CharSequence, Object> map;

	/**
	 * Constructs a new, empty set.
	 */
	public TrieSet() {
		this.map = new TrieMap<Object>();
	}

    /**
     * Constructs a new set containing the elements in the specified
     * collection.
     *
     * @param c the collection whose elements are to be placed into this set
     * @throws NullPointerException if the specified collection is null
     */
	public TrieSet(Collection<? extends CharSequence> coll) {
		this();
		addAll(coll);
	}
	
    /**
     * Constructs a new set containing the elements in the specified
     * iterable.
     *
     * @param c the iterable whose elements are to be placed into this set
     * @throws NullPointerException if the specified iterable is null
     */
	public TrieSet(Iterable<? extends CharSequence> it) {
		this(it.iterator());
	}
	
    /**
     * Constructs a new set containing the elements in the specified
     * iterator.
     *
     * @param c the iterator whose elements are to be placed into this set
     * @throws NullPointerException if the specified iterator is null
     */
	public TrieSet(Iterator<? extends CharSequence> it) {
		this();
		while(it.hasNext()) {
			add(it.next());
		}
	}
	
	/**
	 * Returns true if the provided prefix is a prefix to exactly one string
	 * contained in this set.
	 *  
	 * @param prefix the prefix to check
	 * @return true if the provided prefix is a prefix to exactly one string
	 */
	public boolean containsUnambiguousPrefix(CharSequence prefix) {
		return ((TrieMap<Object>)map).containsUnambiguousKeyPrefix(prefix);
	}
	
	/**
	 * Checks if any string in the set starts with the specified prefix.
	 * 
	 * @param prefix the prefix to check
	 * @return true if any string in the set starts with the specified prefix
	 */
	public boolean containsPrefix(CharSequence prefix) {
		return ((TrieMap<Object>)map).containsKeyPrefix(prefix);
	}

	/*
	 * FIXME : too many issues to flesh out now, but shouldn't be hard
	public TrieSet subTrieSet(CharSequence prefix) {
		return new TrieSet(((TrieMap<Object>)map).subTrie(prefix));
	}
	*/
	
	/**
	 * Save the state of this <tt>TrieSet</tt> instance to a stream (that is,
	 * serialize it).
	 */
	private void writeObject(java.io.ObjectOutputStream s)
			throws java.io.IOException {
		// Write out any hidden serialization magic
		s.defaultWriteObject();

		// Write out size
		s.writeInt(map.size());

		// Write out all elements in the proper order.
		for (@SuppressWarnings("rawtypes")
		Iterator i = map.keySet().iterator(); i.hasNext();)
			s.writeObject(i.next());
	}

	/**
	 * Reconstitute the <tt>HashSet</tt> instance from a stream (that is,
	 * deserialize it).
	 */
	private void readObject(java.io.ObjectInputStream s)
			throws java.io.IOException, ClassNotFoundException {
		// Read in any hidden serialization magic
		s.defaultReadObject();

		// Create backing HashMap
		map = new TrieMap<Object>();

		// Read in size
		int size = s.readInt();

		// Read in all elements in the proper order.
		for (int i = 0; i < size; i++) {
			CharSequence e = (CharSequence) s.readObject();
			map.put(e, PRESENT);
		}
	}
	
	/**
	 * Adds the specified element to this set if it is not already present.
	 * More formally, adds the specified element <tt>e</tt> to this set if
	 * this set contains no element <tt>e2</tt> such that
	 * <tt>(e==null&nbsp;?&nbsp;e2==null&nbsp;:&nbsp;e.equals(e2))</tt>.
	 * If this set already contains the element, the call leaves the set
	 * unchanged and returns <tt>false</tt>.
	 *
	 * @param e element to be added to this set
	 * @return <tt>true</tt> if this set did not already contain the specified
	 * element
	 */
	@Override
	public boolean add(CharSequence e) {
		return map.put(e, PRESENT) == null;
	}

	/**
	 * Removes all of the elements from this set.
	 * The set will be empty after this call returns.
	 */
	@Override
	public void clear() {
		map.clear();
	}

	/**
	 * Returns <tt>true</tt> if this set contains the specified element.
	 * More formally, returns <tt>true</tt> if and only if this set
	 * contains an element <tt>e</tt> such that
	 * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>.
	 *
	 * @param o element whose presence in this set is to be tested
	 * @return <tt>true</tt> if this set contains the specified element
	 */
	@Override
	public boolean contains(Object o) {
		return map.containsKey(o);
	}

	/**
	 * Returns <tt>true</tt> if this set contains no elements.
	 *
	 * @return <tt>true</tt> if this set contains no elements
	 */
	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	/**
	 * Returns an iterator over the elements in this set.  The elements
	 * are returned in no particular order.
	 *
	 * @return an Iterator over the elements in this set
	 * @see ConcurrentModificationException
	 */
	@Override
	public Iterator<CharSequence> iterator() {
		return map.keySet().iterator();
	}

	/**
	 * Removes the specified element from this set if it is present.
	 * More formally, removes an element <tt>e</tt> such that
	 * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>,
	 * if this set contains such an element.  Returns <tt>true</tt> if
	 * this set contained the element (or equivalently, if this set
	 * changed as a result of the call).  (This set will not contain the
	 * element once the call returns.)
	 *
	 * @param o object to be removed from this set, if present
	 * @return <tt>true</tt> if the set contained the specified element
	 */
	@Override
	public boolean remove(Object o) {
		return map.remove(o)==PRESENT;
	}

	/**
	 * Returns the number of elements in this set (its cardinality).
	 *
	 * @return the number of elements in this set (its cardinality)
	 */
	@Override
	public int size() {
		return map.size();
	}
	
	
//	public static void main(String[] args) {
//		TrieSet trie = new TrieSet();
//	
//		trie.add("JEFFERY");
//		trie.add("HOTDOG");
//		trie.add("JOSHUA");
//		trie.add("JOSHUA");
//		trie.add("YEMEN");
//	
//		approximateTest(trie, "GEOFFERY", 3);
//		approximateTest(trie, "JASH;A", 2);
//		approximateTest(trie, "Bz9LAK", 3);
//		approximateTest(trie, ";;;;;;", 6);
//		approximateTest(trie, "MANLY", 2);
//	}
//	
//	private static void approximateTest(TrieSet trie, String str, double dist) {
//		System.out.println(str + "\t" + trie.getApproximateMatches(str, dist) + "\t" + trie.containsApproximateKey(str, dist));
//	}
}
