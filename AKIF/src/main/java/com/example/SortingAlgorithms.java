package com.example;

import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Contains implementations of various sorting algorithms with visualization
 * Each algorithm updates the UI bars in real-time to show the sorting process
 */
public class SortingAlgorithms {

    private static final int DELAY_MS = 100; // Delay between steps for visualization
    private static final Color COMPARING_COLOR = Color.RED;
    private static final Color SWAPPING_COLOR = Color.YELLOW;
    private static final Color SORTED_COLOR = Color.GREEN;
    private static final Color DEFAULT_COLOR = Color.LIGHTBLUE;

    /**
     * Selection Sort Algorithm
     * Time Complexity: O(n²)
     */
    public static CompletableFuture<Void> selectionSort(int[] arr, List<Rectangle> bars) {
        return CompletableFuture.runAsync(() -> {
            int n = arr.length;

            for (int i = 0; i < n - 1; i++) {
                int minIdx = i;

                // Highlight current position
                int finalI = i;
                Platform.runLater(() -> bars.get(finalI).setFill(COMPARING_COLOR));
                delay();

                // Find minimum element
                for (int j = i + 1; j < n; j++) {
                    int finalJ = j;
                    Platform.runLater(() -> bars.get(finalJ).setFill(COMPARING_COLOR));
                    delay();

                    if (arr[j] < arr[minIdx]) {
                        if (minIdx != i) {
                            int finalMinIdx = minIdx;
                            Platform.runLater(() -> bars.get(finalMinIdx).setFill(DEFAULT_COLOR));
                        }
                        minIdx = j;
                        int finalMinIdx1 = minIdx;
                        Platform.runLater(() -> bars.get(finalMinIdx1).setFill(SWAPPING_COLOR));
                    } else {
                        int finalJ1 = j;
                        Platform.runLater(() -> bars.get(finalJ1).setFill(DEFAULT_COLOR));
                    }
                }

                // Swap elements
                if (minIdx != i) {
                    swap(arr, i, minIdx);
                    swapBars(bars, i, minIdx);
                    delay();
                }

                // Mark as sorted
                int finalI1 = i;
                int finalMinIdx2 = minIdx;
                Platform.runLater(() -> {
                    bars.get(finalI1).setFill(SORTED_COLOR);
                    if (finalMinIdx2 != finalI1) bars.get(finalMinIdx2).setFill(DEFAULT_COLOR);
                });
            }

            // Mark last element as sorted
            Platform.runLater(() -> bars.get(n-1).setFill(SORTED_COLOR));
        });
    }

    /**
     * Bubble Sort Algorithm
     * Time Complexity: O(n²)
     */
    public static CompletableFuture<Void> bubbleSort(int[] arr, List<Rectangle> bars) {
        return CompletableFuture.runAsync(() -> {
            int n = arr.length;

            for (int i = 0; i < n - 1; i++) {
                boolean swapped = false;

                for (int j = 0; j < n - i - 1; j++) {
                    // Highlight comparing elements
                    int finalJ = j;
                    Platform.runLater(() -> {
                        bars.get(finalJ).setFill(COMPARING_COLOR);
                        bars.get(finalJ + 1).setFill(COMPARING_COLOR);
                    });
                    delay();

                    if (arr[j] > arr[j + 1]) {
                        // Highlight swapping elements
                        int finalJ1 = j;
                        Platform.runLater(() -> {
                            bars.get(finalJ1).setFill(SWAPPING_COLOR);
                            bars.get(finalJ1 + 1).setFill(SWAPPING_COLOR);
                        });

                        swap(arr, j, j + 1);
                        swapBars(bars, j, j + 1);
                        swapped = true;
                        delay();
                    }

                    // Reset colors
                    int finalJ2 = j;
                    Platform.runLater(() -> {
                        bars.get(finalJ2).setFill(DEFAULT_COLOR);
                        bars.get(finalJ2 + 1).setFill(DEFAULT_COLOR);
                    });
                }

                // Mark sorted element
                int finalI = i;
                Platform.runLater(() -> bars.get(n - finalI - 1).setFill(SORTED_COLOR));

                if (!swapped) break;
            }

            // Mark first element as sorted
            Platform.runLater(() -> bars.get(0).setFill(SORTED_COLOR));
        });
    }

    /**
     * Insertion Sort Algorithm
     * Time Complexity: O(n²)
     */
    public static CompletableFuture<Void> insertionSort(int[] arr, List<Rectangle> bars) {
        return CompletableFuture.runAsync(() -> {
            int n = arr.length;

            Platform.runLater(() -> bars.get(0).setFill(SORTED_COLOR));

            for (int i = 1; i < n; i++) {
                int key = arr[i];
                int j = i - 1;

                int finalI = i;
                Platform.runLater(() -> bars.get(finalI).setFill(COMPARING_COLOR));
                delay();

                while (j >= 0 && arr[j] > key) {
                    int finalJ = j;
                    Platform.runLater(() -> bars.get(finalJ).setFill(SWAPPING_COLOR));
                    delay();

                    arr[j + 1] = arr[j];
                    updateBarHeight(bars, j + 1, arr[j]);

                    int finalJ1 = j;
                    Platform.runLater(() -> bars.get(finalJ1).setFill(DEFAULT_COLOR));
                    j--;
                }

                arr[j + 1] = key;
                updateBarHeight(bars, j + 1, key);
                int finalJ2 = j;
                Platform.runLater(() -> bars.get(finalJ2 + 1).setFill(SORTED_COLOR));
                delay();
            }
        });
    }

    /**
     * Merge Sort Algorithm
     * Time Complexity: O(n log n)
     */
    public static CompletableFuture<Void> mergeSort(int[] arr, List<Rectangle> bars) {
        return CompletableFuture.runAsync(() -> {
            mergeSortHelper(arr, bars, 0, arr.length - 1);
            // Mark all as sorted
            Platform.runLater(() -> {
                for (Rectangle bar : bars) {
                    bar.setFill(SORTED_COLOR);
                }
            });
        });
    }

    private static void mergeSortHelper(int[] arr, List<Rectangle> bars, int left, int right) {
        if (left < right) {
            int mid = left + (right - left) / 2;

            mergeSortHelper(arr, bars, left, mid);
            mergeSortHelper(arr, bars, mid + 1, right);
            merge(arr, bars, left, mid, right);
        }
    }

    private static void merge(int[] arr, List<Rectangle> bars, int left, int mid, int right) {
        int n1 = mid - left + 1;
        int n2 = right - mid;

        int[] leftArr = new int[n1];
        int[] rightArr = new int[n2];

        System.arraycopy(arr, left, leftArr, 0, n1);
        System.arraycopy(arr, mid + 1, rightArr, 0, n2);

        int i = 0, j = 0, k = left;

        while (i < n1 && j < n2) {
            int finalK = k;
            Platform.runLater(() -> {
                if (finalK < bars.size()) {
                    bars.get(finalK).setFill(COMPARING_COLOR);
                }
            });
            delay();

            if (leftArr[i] <= rightArr[j]) {
                arr[k] = leftArr[i];
                updateBarHeight(bars, k, leftArr[i]);
                i++;
            } else {
                arr[k] = rightArr[j];
                updateBarHeight(bars, k, rightArr[j]);
                j++;
            }

            int finalK1 = k;
            Platform.runLater(() -> {
                if (finalK1 < bars.size()) bars.get(finalK1).setFill(DEFAULT_COLOR);
            });
            k++;
        }

        while (i < n1) {
            arr[k] = leftArr[i];
            updateBarHeight(bars, k, leftArr[i]);
            i++;
            k++;
        }

        while (j < n2) {
            arr[k] = rightArr[j];
            updateBarHeight(bars, k, rightArr[j]);
            j++;
            k++;
        }
    }

    /**
     * Quick Sort Algorithm
     * Time Complexity: O(n log n) average, O(n²) worst case
     */
    public static CompletableFuture<Void> quickSort(int[] arr, List<Rectangle> bars) {
        return CompletableFuture.runAsync(() -> {
            quickSortHelper(arr, bars, 0, arr.length - 1);
            // Mark all as sorted
            Platform.runLater(() -> {
                for (Rectangle bar : bars) {
                    bar.setFill(SORTED_COLOR);
                }
            });
        });
    }

    private static void quickSortHelper(int[] arr, List<Rectangle> bars, int low, int high) {
        if (low < high) {
            int pi = partition(arr, bars, low, high);

            quickSortHelper(arr, bars, low, pi - 1);
            quickSortHelper(arr, bars, pi + 1, high);
        }
    }

    private static int partition(int[] arr, List<Rectangle> bars, int low, int high) {
        int pivot = arr[high];
        Platform.runLater(() -> bars.get(high).setFill(Color.PURPLE));

        int i = low - 1;

        for (int j = low; j < high; j++) {
            int finalJ = j;
            Platform.runLater(() -> bars.get(finalJ).setFill(COMPARING_COLOR));
            delay();

            if (arr[j] < pivot) {
                i++;
                swap(arr, i, j);
                swapBars(bars, i, j);
                int finalI = i;
                int finalJ1 = j;
                Platform.runLater(() -> {
                    bars.get(finalI).setFill(SWAPPING_COLOR);
                    bars.get(finalJ1).setFill(SWAPPING_COLOR);
                });
                delay();
            }

            int finalJ2 = j;
            Platform.runLater(() -> bars.get(finalJ2).setFill(DEFAULT_COLOR));
        }

        swap(arr, i + 1, high);
        swapBars(bars, i + 1, high);

        int finalI1 = i;
        Platform.runLater(() -> {
            bars.get(finalI1 + 1).setFill(DEFAULT_COLOR);
            bars.get(high).setFill(DEFAULT_COLOR);
        });

        return i + 1;
    }

    /**
     * Heap Sort Algorithm
     * Time Complexity: O(n log n)
     */
    public static CompletableFuture<Void> heapSort(int[] arr, List<Rectangle> bars) {
        return CompletableFuture.runAsync(() -> {
            int n = arr.length;

            // Build max heap
            for (int i = n / 2 - 1; i >= 0; i--) {
                heapify(arr, bars, n, i);
            }

            // Extract elements from heap
            for (int i = n - 1; i > 0; i--) {
                swap(arr, 0, i);
                swapBars(bars, 0, i);

                int finalI = i;
                Platform.runLater(() -> {
                    bars.get(0).setFill(SWAPPING_COLOR);
                    bars.get(finalI).setFill(SORTED_COLOR);
                });
                delay();

                Platform.runLater(() -> bars.get(0).setFill(DEFAULT_COLOR));

                heapify(arr, bars, i, 0);
            }

            Platform.runLater(() -> bars.get(0).setFill(SORTED_COLOR));
        });
    }

    private static void heapify(int[] arr, List<Rectangle> bars, int n, int i) {
        int largest = i;
        int left = 2 * i + 1;
        int right = 2 * i + 2;

        if (left < n && arr[left] > arr[largest]) {
            largest = left;
        }

        if (right < n && arr[right] > arr[largest]) {
            largest = right;
        }

        if (largest != i) {
            int finalLargest = largest;
            Platform.runLater(() -> {
                bars.get(i).setFill(COMPARING_COLOR);
                bars.get(finalLargest).setFill(COMPARING_COLOR);
            });
            delay();

            swap(arr, i, largest);
            swapBars(bars, i, largest);

            int finalLargest1 = largest;
            Platform.runLater(() -> {
                bars.get(i).setFill(DEFAULT_COLOR);
                bars.get(finalLargest1).setFill(DEFAULT_COLOR);
            });

            heapify(arr, bars, n, largest);
        }
    }

    // Helper methods
    private static void swap(int[] arr, int i, int j) {
        int temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }

    private static void swapBars(List<Rectangle> bars, int i, int j) {
        Platform.runLater(() -> {
            double tempHeight = bars.get(i).getHeight();
            bars.get(i).setHeight(bars.get(j).getHeight());
            bars.get(j).setHeight(tempHeight);
        });
    }

    private static void updateBarHeight(List<Rectangle> bars, int index, int value) {
        Platform.runLater(() -> {
            if (index < bars.size()) {
                bars.get(index).setHeight(value * 4); // Scale factor for visualization
            }
        });
    }

    private static void delay() {
        try {
            Thread.sleep(DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}