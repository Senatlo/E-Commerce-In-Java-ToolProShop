package com.shopping.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A custom singly linked list implementation.
 * Built from scratch to hold items without relying on standard Java Collections.
 */
public class CustomLinkedList<T> implements Iterable<T> {
    
    private Node<T> head;
    private int size;

    /**
     * Inner class representing a node in the linked list.
     */
    private static class Node<T> {
        T data;
        Node<T> next;

        Node(T data) {
            this.data = data;
            this.next = null;
        }
    }

    /**
     * Adds a new node to the end of the list.
     * Matches the 'add_node' requirement.
     * 
     * @param data the data to add
     */
    public void addNode(T data) {
        Node<T> newNode = new Node<>(data);
        if (head == null) {
            head = newNode;
        } else {
            Node<T> current = head;
            while (current.next != null) {
                current = current.next;
            }
            current.next = newNode;
        }
        size++;
    }

    /**
     * Removes the first occurrence of the specified data from the list.
     * Matches the 'remove_node' requirement.
     * 
     * @param data the data to remove
     * @return true if the node was removed, false otherwise
     */
    public boolean removeNode(T data) {
        if (head == null) {
            return false;
        }

        if (head.data.equals(data)) {
            head = head.next;
            size--;
            return true;
        }

        Node<T> current = head;
        while (current.next != null) {
            if (current.next.data.equals(data)) {
                current.next = current.next.next;
                size--;
                return true;
            }
            current = current.next;
        }
        return false;
    }

    /**
     * Returns the number of elements in the list.
     * @return the size of the list
     */
    public int size() {
        return size;
    }

    /**
     * Provides an iterator to traverse the custom linked list.
     * Matches the 'iterate' requirement.
     */
    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private Node<T> current = head;

            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                T data = current.data;
                current = current.next;
                return data;
            }
        };
    }
}
