package org.example.dsa_simulator.heap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

public class MinHeap {
    private final List<Integer> heap;

    public MinHeap() {
        heap = new ArrayList<>();
    }
    public MinHeap(List<Integer> list) {
        this.heap = new ArrayList<>(list);
        for (int i = (heap.size() / 2) - 1; i >= 0; i--) {
            heapify(i);
        }
    }

    private void heapify(int i) {
        int smallest = i;
        int left = 2 * i + 1;
        int right = 2 * i + 2;
        int size = heap.size();

        if (left < size && heap.get(left) < heap.get(smallest))
            smallest = left;
        if (right < size && heap.get(right) < heap.get(smallest))
            smallest = right;

        if (smallest != i) {
            Collections.swap(heap, i, smallest);
            heapify(smallest);
        }
    }

    public void insert(int key) {
        heap.add(key);
        int i = heap.size() - 1;

        while (i != 0 && heap.get((i - 1) / 2) > heap.get(i)) {
            Collections.swap(heap, i, (i - 1) / 2);
            i = (i - 1) / 2;
        }
    }

    public int extractMin() {
        if (heap.isEmpty()) {
            throw new NoSuchElementException("Heap is empty, cannot extract min.");
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

    public int getMin() {
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
        return "MinHeap{" +
                "heap=" + heap +
                '}';
    }
}
