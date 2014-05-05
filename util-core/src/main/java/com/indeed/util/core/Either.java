package com.indeed.util.core;

import com.google.common.base.Function;

/**
* @author jplaisance
*/
public interface Either<A extends Throwable,B> {

    public <Z> Z match(Matcher<A, B, Z> matcher) throws A;

    public <C> Either<A, C> map(Function<B, C> f);

    public <C> Either<A, C> flatMap(Function<B, Either<A, C>> f);

    public B get() throws A;

    public static abstract class Matcher<A extends Throwable,B,Z> {

        protected Z left(A a) throws A {
            throw a;
        }

        protected Z right(B b) {
            throw new UnsupportedOperationException();
        }
    }

    public static final class Left<A extends Throwable,B> implements Either<A,B> {

        private final A a;

        public static <A extends Throwable,B> Either<A,B> of(A a) {
            return new Left<A, B>(a);
        }

        private Left(final A a) {
            this.a = a;
        }

        public <Z> Z match(final Matcher<A, B, Z> matcher) throws A {
            return matcher.left(a);
        }

        @Override
        public <C> Either<A, C> map(final Function<B, C> f) {
            return (Either<A, C>)this;
        }

        @Override
        public <C> Either<A, C> flatMap(final Function<B, Either<A, C>> f) {
            return (Either<A, C>)this;
        }

        @Override
        public B get() throws A {
            throw a;
        }
    }

    public static final class Right<A extends Throwable,B> implements Either<A,B> {

        private final B b;

        public static <A extends Throwable,B> Either<A,B> of(B b) {
            return new Right<A, B>(b);
        }

        private Right(final B b) {
            this.b = b;
        }

        public <Z> Z match(final Matcher<A, B, Z> matcher) {
            return matcher.right(b);
        }

        @Override
        public <C> Either<A, C> map(final Function<B, C> f) {
            return Right.of(f.apply(b));
        }

        @Override
        public <C> Either<A, C> flatMap(final Function<B, Either<A, C>> f) {
            return f.apply(b);
        }

        @Override
        public B get() throws A {
            return b;
        }
    }
}
