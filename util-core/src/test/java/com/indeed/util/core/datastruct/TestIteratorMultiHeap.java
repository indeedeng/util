package com.indeed.util.core.datastruct;

import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import junit.framework.TestCase;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

/**
 * @author jplaisance
 */
public final class TestIteratorMultiHeap extends TestCase {

    private static final Logger log = LogManager.getLogger(TestIteratorMultiHeap.class);

    public static final class RandomIntIterator {

        Random random;
        int length;
        int value = 0;
        int i = 0;

        public RandomIntIterator(Random random, int length) {
            this.random = random;
            this.length = length;
        }

        boolean next() {
            if (i >= length) return false;
            value+=random.nextInt(4)+1;
            i++;
            return true;
        }

        int value() {
            return value;
        }

        void reset(Random random, int length) {
            value = 0;
            this.random = random;
            i = 0;
            this.length = length;
        }
    }

    public void testHeap() {
        final Random[] randoms = new Random[24];
        for (int i = 0; i < randoms.length; i++) {
            randoms[i] = new Random(i);
        }
        final List<RandomIntIterator> iterators = Lists.newArrayList();
        for (final Random random : randoms) {
            iterators.add(new RandomIntIterator(random, random.nextInt(128) + 128));
        }
        final IteratorMultiHeap<RandomIntIterator> heap = new IteratorMultiHeap<RandomIntIterator>(iterators.size(), RandomIntIterator.class) {
            @Override
            protected boolean next(final RandomIntIterator randomIntIterator) {
                return randomIntIterator.next();
            }

            @Override
            protected int compare(final RandomIntIterator a, final RandomIntIterator b) {
                return Ints.compare(a.value(), b.value());
            }
        };
        for (RandomIntIterator iterator : iterators) {
            heap.add(iterator);
        }
        doTest(heap);
        heap.clear();
        for (int i = 0; i < randoms.length; i++) {
            final Random random = new Random(i);
            final RandomIntIterator iterator = iterators.get(i);
            iterator.reset(random, random.nextInt(128) + 128);
            heap.add(iterator);
        }
        doTest(heap);
    }

    public void doTest(IteratorMultiHeap<RandomIntIterator> heap) {

        final Random[] randoms = new Random[24];
        for (int i = 0; i < randoms.length; i++) {
            randoms[i] = new Random(i);
        }
        final PriorityQueue<RandomIntIterator> queue = new PriorityQueue<RandomIntIterator>(24, new Comparator<RandomIntIterator>() {
            @Override
            public int compare(final RandomIntIterator o1, final RandomIntIterator o2) {
                return Ints.compare(o1.value(), o2.value());
            }
        });
        for (final Random random : randoms) {
            final RandomIntIterator iterator = new RandomIntIterator(random, random.nextInt(128) + 128);
            assertTrue(iterator.next());
            queue.add(iterator);
        }

        while (heap.next()) {
            assertFalse(queue.isEmpty());
            final List<RandomIntIterator> mins = Lists.newArrayList();
            final RandomIntIterator minValue = queue.peek();
            while (!queue.isEmpty() && minValue.value() == queue.peek().value()) {
                mins.add(queue.poll());
            }
            final RandomIntIterator[] min = heap.getMin();
            final int minLength = heap.getMinLength();
            assertEquals(mins.size(), minLength);
            for (int i = 0; i < minLength; i++) {
                assertEquals(minValue.value(), min[i].value());
            }
            for (RandomIntIterator iterator : mins) {
                if (iterator.next()) {
                    queue.add(iterator);
                }
            }
        }
        assertTrue(queue.isEmpty());
    }
}
