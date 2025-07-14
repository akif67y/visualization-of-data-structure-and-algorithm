package org.example.dsa_simulator.heap; // Assuming a 'heap' package

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

public class MaxHeap {
    private final List<Integer> heap;

    public MaxHeap() {
        heap = new ArrayList<>();
    }

    public MaxHeap(List<Integer> list) {
        this.heap = new ArrayList<>(list);
        for (int i = (heap.size() / 2) - 1; i >= 0; i--) {
            heapify(i);
        }
    }

    private void heapify(int i) {
        int largest = i;
        int left = 2 * i + 1;
        int right = 2 * i + 2;
        int size = heap.size();

        if (left < size && heap.get(left) > heap.get(largest))
            largest = left;
        if (right < size && heap.get(right) > heap.get(largest))
            largest = right;

        if (largest != i) {
            Collections.swap(heap, i, largest);
            heapify(largest);
        }
    }

    public void insert(int key) {
        heap.add(key);
        int i = heap.size() - 1;
        while (i != 0 && heap.get((i - 1) / 2) < heap.get(i)) {
            Collections.swap(heap, i, (i - 1) / 2);
            i = (i - 1) / 2;
        }
    }

    public int extractMax() {
        if (heap.isEmpty()) {
            // will do something
        }
        int root = heap.getFirst();

        if (heap.size() > 1) {
            heap.set(0, heap.getLast());
            heap.removeLast();
            heapify(0);
        } else {
            heap.removeFirst();
        }

        return root;
    }

    public int getMax() {
        if (heap.isEmpty()) {
            throw new NoSuchElementException("Heap is empty.");
        }
        return heap.getFirst();
    }

    public List<Integer> getAsList() {
        return new ArrayList<>(heap);
    }

    @Override
    public String toString() {
        return "MaxHeap{" +
                "heap=" + heap +
                '}';
    }
}