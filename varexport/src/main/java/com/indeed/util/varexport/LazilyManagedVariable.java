// Copyright 2009 Indeed
package com.indeed.util.varexport;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableSet;

import java.util.Map;
import java.util.Set;

/**
 * To be used instead of {@link com.indeed.util.varexport.Export} or the introspection methods of
 * {@link com.indeed.util.varexport.VarExporter} to export a manually updated variable.
 *
 * <p>Example usage:
 *
 * <pre>
 *   ManagedVariable&lt;Integer&gt; var = ManagedVariable.&lt;Integer&gt;builder().setName("myvar").setValue(524).build();
 *   // ...
 *   var.set(52473);
 * </pre>
 *
 * @author jack@indeed.com (Jack Humphrey)
 */
public class LazilyManagedVariable<T> extends Variable<T> {

    public static <T> Builder<T> builder(final Class<T> c) {
        return new Builder<T>(c, "");
    }

    public static <T> Builder<T> builder(final Class<T> c, String namespace) {
        return new Builder<T>(c, namespace);
    }

    public static class Builder<T> {
        private final Class<T> c;
        private final String namespace;
        private String name = null;
        private String doc = "";
        private boolean expand = false;
        private Set<String> tags = ImmutableSet.of();
        private Supplier<T> valueSupplier = null;

        private Builder(final Class<T> c, final String namespace) {
            this.c = c;
            this.namespace = namespace;
        }

        public Builder<T> setName(final String name) {
            this.name = name;
            return this;
        }

        public Builder<T> setDoc(final String doc) {
            this.doc = doc;
            return this;
        }

        public Builder<T> setExpand(final boolean expand) {
            this.expand = expand;
            return this;
        }

        public Builder<T> setValue(final Supplier<T> valueSupplier) {
            this.valueSupplier = valueSupplier;
            return this;
        }

        public Builder<T> setTags(Set<String> tags) {
            this.tags = tags;
            return this;
        }

        public LazilyManagedVariable<T> build() {
            if (name == null) {
                throw new RuntimeException("name must not be null for ManagedVariable");
            }
            if (tags == null) {
                throw new RuntimeException("tags must not be null for ManagedVariable");
            }
            return new LazilyManagedVariable<T>(
                    name, tags, doc, expand, c, valueSupplier, namespace);
        }
    }

    @VisibleForTesting
    protected Supplier<Long> clock =
            new Supplier<Long>() {
                public Long get() {
                    return System.currentTimeMillis();
                }
            };

    private final Class<T> c;
    private final Supplier<T> valueSupplier;
    private Long lastUpdated = clock.get();

    private LazilyManagedVariable(
            final String name,
            final Set<String> tags,
            final String doc,
            final boolean expand,
            final Class<T> c,
            final Supplier<T> valueSupplier,
            final String namespace) {
        super(name, tags, doc, expand, namespace);
        this.c = c;
        this.valueSupplier = valueSupplier;
    }

    public void update() {
        lastUpdated = clock.get();
    }

    /** Managed variables are always considered "live" */
    @Override
    protected boolean isLive() {
        return true;
    }

    protected boolean canExpand() {
        return Map.class.isAssignableFrom(c);
    }

    @Override
    public Long getLastUpdated() {
        return lastUpdated;
    }

    public T getValue() {
        return valueSupplier.get();
    }
}
