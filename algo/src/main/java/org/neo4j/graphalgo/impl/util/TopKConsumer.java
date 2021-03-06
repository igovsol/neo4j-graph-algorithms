package org.neo4j.graphalgo.impl.util;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TopKConsumer<T extends Comparable<T>> implements Consumer<T> {
    private final int topK;
    private final T[] heap;
    private int count;
    private T minValue;

    public TopKConsumer(int topK) {
        this.topK = topK;
        heap = (T[]) new Comparable[topK];
        count = 0;
        minValue = null;
    }

    public static <T extends Comparable<T>> List<T> topK(List<T> items, int topK) {
        TopKConsumer<T> consumer = new TopKConsumer<>(topK);
        items.stream().forEach(consumer);
        return consumer.list();
    }
    public static <T extends Comparable<T>> Stream<T> topK(Stream<T> items, int topK) {
        TopKConsumer<T> consumer = new TopKConsumer<>(topK);
        items.forEach(consumer);
        return consumer.stream();
    }

    @Override
    public void accept(T item) {
        if (count < topK || minValue == null || item.compareTo(minValue) < 0) {
            int idx = Arrays.binarySearch(heap, 0, count, item);
            idx = (idx < 0) ? -idx : idx + 1;
            int length = topK - idx;
            if (length > 0 && idx < topK) System.arraycopy(heap,idx-1,heap,idx, length);
            heap[idx-1]=item;
            if (count< topK) count++;
            minValue = heap[count-1];
        }
    }

    public Stream<T> stream() {
        return count< topK ? Arrays.stream(heap,0,count) : Arrays.stream(heap);
    }

    public List<T> list() {
        List<T> list = Arrays.asList(heap);
        return count< topK ? list.subList(0,count)  : list;
    }

    public void accept(TopKConsumer<T> other) {
        if (minValue == null || count < topK || other.minValue != null && other.minValue.compareTo(minValue) < 0) {
            for (int i=0;i<other.count;i++) {
                accept(other.heap[i]);
            }
        }
    }
}
