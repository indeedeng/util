// Copyright 2009 Indeed
package com.indeed.util.varexport;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableSet;

import java.util.Map;
import java.util.Set;

/**
 * To be used instead of {@link Export} or the introspection methods
 * of {@link VarExporter} to export a manually updated variable.
 * <p>
 * Example usage:
 * <pre>
 *   ManagedVariable&lt;Integer&gt; var = ManagedVariable.&lt;Integer&gt;builder().setName("myvar").setValue(524).build();
 *   // ...
 *   var.set(52473);
 * </pre>
 *
 * @author jack@indeed.com (Jack Humphrey)
 */
public class ManagedVariable<T> extends Variable<T> {

    public static <T> Builder<T> builder() {
        return new Builder<T>("");
    }

    public static <T> Builder<T> builder(String namespace) {
        return new Builder<T>(namespace);
    }
    
    public static class Builder<T> {
        private final String namespace;
        private String name = null;
        private String doc = "";
        private boolean expand = false;
        private Set<String> tags = ImmutableSet.of();
        private T value = null;

        private Builder(String namespace) {
            this.namespace = namespace;
        }

        public Builder<T> setName(String name) {
            this.name = name;
            return this;
        }

        public Builder<T> setDoc(String doc) {
            this.doc = doc;
            return this;
        }

        public Builder<T> setExpand(boolean expand) {
            this.expand = expand;
            return this;
        }

        public Builder<T> setValue(T value) {
            this.value = value;
            return this;
        }

        public Builder<T> setTags(Set<String> tags) {
            this.tags = tags;
            return this;
        }

        public ManagedVariable<T> build() {
            if (name == null) {
                throw new RuntimeException("name must not be null for ManagedVariable");
            }
            if (tags == null) {
                throw new RuntimeException("tags must not be null for ManagedVariable");
            }
            return new ManagedVariable<T>(name, tags, doc, expand, value, namespace);
        }
    }

    @VisibleForTesting
    protected Supplier<Long> clock = new Supplier<Long>() {
        public Long get() {
            return System.currentTimeMillis();
        }
    };

    private T value;
    private Long lastUpdated = clock.get();

    private ManagedVariable(String name, Set<String> tags, String doc, boolean expand, T value, String namespace) {
        super(name, tags, doc, expand, namespace);
        this.value = value;
    }

    public void set(T value) {
        this.value = value;
        lastUpdated = clock.get();
    }

    @Override
    protected boolean isLive() {
        return true;
    }

    protected boolean canExpand() {
        return value != null && Map.class.isAssignableFrom(value.getClass());
    }

    @Override
    public Long getLastUpdated() {
        return lastUpdated;
    }

    public T getValue() {
        return value;
    }
}
