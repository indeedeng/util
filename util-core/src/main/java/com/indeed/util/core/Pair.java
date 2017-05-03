package com.indeed.util.core;

import com.google.common.base.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Comparator;

/**
 * @author ahudson
 */
public class Pair<A, B> implements Serializable {
    
    private static final long serialVersionUID = -7675178028057823346L;
    
    private final A a;
    private final B b;
    
    public Pair(A a, B b) {
        this.a = a;
        this.b = b;
    }
    
    public A getFirst() {
        return a;
    }
    
    public B getSecond() {
        return b;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Pair) {
            Pair p = (Pair)obj;
            Object a = p.getFirst();
            Object b = p.getSecond();
            return (a == null ? this.a == null : a.equals(this.a))
                    && (b == null ? this.b == null : b.equals(this.b));
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result;
        result = (a != null ? a.hashCode() : 0);
        result = 31 * result + (b != null ? b.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "("+a+", "+b+")";
    }

    public static <T1, T2> Pair<T1, T2> of(T1 first, T2 second) {
        return new Pair<T1, T2>(first, second);
    }

    public static class FullPairComparator implements Comparator<Pair> {
        public int compare(Pair o1, Pair o2) {
            int ret = ((Comparable)o1.a).compareTo(o2.a);
            if (ret == 0) return ((Comparable)o1.b).compareTo(o2.b);
            return ret;
        }
    }

    public static class HalfPairComparator implements Comparator<Pair> {
        public int compare(Pair o1, Pair o2) {
            return ((Comparable)o1.a).compareTo(o2.a);
        }
    }

    /**
     * Makes grabbing the first value out of a collection of Pairs a bit easier, e.g.
     * given an iterable over Pair.of(String, ?) objects:
     *
     * <br>
     * <code>... = Iterables.transform(iterable, Pair.fst(String.class))</code>
     *
     * @param clazz The class type for a.
     * @param <T1> Expected data type for {@link Pair#a}
     * @param <T2> Expected data type for {@link Pair#b}
     * @return a Function that returns null or the first value of the Pair, which also may be null
     * @deprecated use {@link #fst()}.
     */
    @Nonnull
    @Deprecated
    public static <T1, T2> Function<Pair<? extends T1, ? extends T2>, ? extends T1> fst(@Nonnull final Class<T1> clazz) {
        return new First<>();
    }
    private static final class First<T1, T2> implements Function<Pair<? extends T1, ? extends T2>, T1> {
        @Nullable
        @Override
        public T1 apply(@Nullable Pair<? extends T1, ? extends T2> input) {
            return (input == null) ? null : input.getFirst();
        }
    }

    /**
     * Makes grabbing the first value out of a collection of Pairs a bit easier, e.g.
     * given an iterable over Pair.of(String, ?) objects:
     *
     * <br>
     * <code>
     *     ... = Iterables.transform(iterable, Pair.&lt;String&gt;fst())
     * </code>
     *
     * @param <T> The expected data type of {@link Pair#a}
     * @return a Function that returns null or the first value of the Pair, which also may be null
     */
    @Nonnull
    public static <T> Function<Pair<? extends T, ?>, ? extends T> fst() {
        return new Function<Pair<? extends T, ?>, T>() {
            @Nullable
            @Override
            public T apply(@Nullable Pair<? extends T, ?> input) {
                return (input == null) ? null : input.getFirst();
            }
        };
    }

    /**
     * Makes grabbing the second value out of a collection of Pairs a bit easier, e.g.
     * given an iterable over Pair.of(?, String) objects:
     *
     * <br>
     * <code>
     *     ... = Iterables.transform(iterable, Pair.snd(String.class))
     * </code>
     *
     * @param clazz The class type for b.
     * @param <T1> Expected data type for {@link Pair#a}
     * @param <T2> Expected data type for {@link Pair#b}
     * @return A function that returns null or the second value of the Pair, which also may be null
     */
    @Nonnull
    @Deprecated
    public static <T1, T2> Function<Pair<? extends T1, ? extends T2>, ? extends T2> snd(@Nonnull final Class<T2> clazz) {
        return new Second<>();
    }
    private static final class Second<T1, T2> implements Function<Pair<? extends T1, ? extends T2>, T2> {
        @Nullable
        @Override
        public T2 apply(@Nullable Pair<? extends T1, ? extends T2> input) {
            return input == null ? null : input.getSecond();
        }
    }

    /**
     * Makes grabbing the second value out of a collection of Pairs a bit easier, e.g.
     * given an iterable over Pair.of(?, String) objects:
     *
     * <br>
     * <code>
     *     ... = Iterables.transform(iterable, Pair.&lt;String&gt;snd())
     * </code>
     *
     * @param <T> The expected data type for {@link Pair#b}.
     * @return a Function that returns null or the second value of the Pair, which also may be null
     */
    @Nonnull
    public static <T> Function<Pair<?, ? extends T>, ? extends T> snd() {
        return new Function<Pair<?, ? extends T>, T>() {
            @Nullable
            @Override
            public T apply(@Nullable final Pair<?, ? extends T> input) {
                return (input == null) ? null : input.getSecond();
            }
        };
    }

}
