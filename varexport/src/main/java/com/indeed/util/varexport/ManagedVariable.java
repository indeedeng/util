// Copyright 2009 Indeed
package com.indeed.util.varexport;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Supplier;

import java.util.Map;

/**
 * To be used instead of {@link com.indeed.util.varexport.Export} or the introspection methods
 * of {@link com.indeed.util.varexport.VarExporter} to export a manually updated variable.
 * <p>
 * Example usage:
 * <pre>
 *   ManagedVariable<Integer> var = ManagedVariable.<Integer>builder().setName("myvar").setValue(524).build();
 *   // ...
 *   var.set(52473);
 * </pre>
 *
 * @author jack@indeed.com (Jack Humphrey)
 */
public class ManagedVariable<T> extends Variable<T> {

    public static <T> Builder<T> builder() {
        return new Builder<T>();
    }
    
    public static class Builder<T> {
        private String name = null;
        private String doc = "";
        private boolean expand = false;
        private T value = null;

        private Builder() {
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

        public ManagedVariable<T> build() {
            if (name == null) {
                throw new RuntimeException("name must not be null for ManagedVariable");
            }
            return new ManagedVariable<T>(name, doc, expand, value);
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

    private ManagedVariable(String name, String doc, boolean expand, T value) {
        super(name, doc, expand);
        this.value = value;
    }

    public void set(T value) {
        this.value = value;
        lastUpdated = clock.get();
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
