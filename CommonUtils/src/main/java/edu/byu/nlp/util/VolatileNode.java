/**
 * Copyright 2012 Brigham Young University
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


/**
 * A class representing a Node in a linked list. The only difference between this class and {@link Node} is that this 
 * class defines its previous and next pointers, as well as its value, as volatile--thereby making it suitable for
 * multi-threaded environments. This class assumes that all nodes always have a previous and a next neighbor and
 * therefore perform no validation. As long as (1) there is a dummy node that represents a terminator for both the
 * head AND tail of the list and (2) all nodes are created using the methods in this class. This is illustrated in the
 * following example which copies the values of an Iterable into a linked list.<p>
 * <pre>
 *   Node<V> dummyNode = Node.newDummyNode();
 *   for (V val : iterable) {
 *     dummyNode.createNodeAndInsertBefore(val);
 *   }
 *   // dummyNode is now the head and tail of the linked list.
 *   if (dummyNode.getNext() != dummyNode) {
 *     System.out.println("First value = " + dummyNode.getNext());
 *   }
 *   if (dummyNode.getPrevious() != dummyNode) {
 *     System.out.println("Last value = " + dummyNode.getPrevious());
 *   }
 * </pre>
 * 
 * @author rah67
 * 
 * @see Node
 */
/*
 * For efficiency reasons, we copy the entire Node class rather than attempting abstraction.
 */
public class VolatileNode<V> {
	
	private volatile V value;
	private volatile VolatileNode<V> previous, next;
	
	public VolatileNode(V value, VolatileNode<V> previous, VolatileNode<V> next) {
		this.value = value;
		this.previous = previous;
		this.next = next;
	}
	
	/**
	 * Creates a new node using the specified value and inserts it before this node in the linked list.
	 */
	public void createNodeAndInsertBefore(V val) {
		VolatileNode<V> newNode = new VolatileNode<V>(val, previous, this);
		previous.next = newNode;
		this.previous = newNode;
		
	}
	
	/**
	 * Creates a new node using the specified value and inserts it after this node in the linked list.
	 */
	public void createNodeAndInsertAfter(V val) {
		VolatileNode<V> newNode = new VolatileNode<V>(val, this, next);
		next.previous = newNode;
		this.next = newNode;
	}
	
	/**
	 * Swaps this node with its next neighbor.
	 */
	public void swapWithNext() {
		assert next != null;

		VolatileNode<V> nextNext = next.getNext();
		if (nextNext != null) {
			nextNext.previous = this;
		}
		assert previous != null;
		previous.next = next;
		next.previous = previous;
		next.next = this;
		previous = next;
		next = nextNext;
		
		assert previous.next == this;
		assert next == null || next.previous == this;
		assert previous.previous.next == previous;
	}
	
	public V getValue() {
		return value;
	}
	
	public void setValue(V value) {
		this.value = value;
	}

	public VolatileNode<V> getPrevious() {
		return previous;
	}

	public VolatileNode<V> getNext() {
		return next;
	}

	/**
	 * Removes this node from the list. Connects the next node and previous node to each other. This nodes' pointers are
	 * left as is so that the iterators can work as expected in the face of removals. Since nothing should point to the
	 * current node any longer, there should be no memory leaks, but the effect on the performance of the garbage
	 * collector is unknown.
	 */
	public void remove() {
		previous.next = next;
		next.previous = previous;
	}
	
	/**
	 * Creates a dummy node that represents the head and tail of a linked list, as described above. The next and
	 * previous pointers of this node point to itself.
	 */
	public static <V> VolatileNode<V> newDummyNode() {
		VolatileNode<V> dummy = new VolatileNode<V>(null, null, null);
		dummy.next = dummy;
		dummy.previous = dummy;
		return dummy;
	}
}