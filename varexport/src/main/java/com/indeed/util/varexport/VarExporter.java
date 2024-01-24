// Copyright 2009 Indeed
package com.indeed.util.varexport;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Enables exporting dynamic variables from classes (static fields/methods only) or objects
 * (instance and static fields/methods) that have been annotated with {@link Export}, as well as
 * individual fields/methods that do not have to be annotated.
 *
 * @author jack@indeed.com (Jack Humphrey)
 */
public class VarExporter implements VariableHost {

    private static Logger log = LoggerFactory.getLogger(VarExporter.class);

    @VisibleForTesting
    protected static ManagedVariable<String> startTime = createStartTimeVariable(new Date());

    private static final Map<String, VarExporter> namespaces = Maps.newHashMap();
    private static final Multimap<String, Variable> tags =
            Multimaps.newSetMultimap(
                    new HashMap<String, Collection<Variable>>(),
                    new Supplier<Set<Variable>>() {
                        @Override
                        public Set<Variable> get() {
                            return Sets.newCopyOnWriteArraySet();
                        }
                    });
    private static final ReentrantReadWriteLock tagsLock = new ReentrantReadWriteLock();

    private final Class<?> namespaceClass;
    private final boolean declaredFieldsOnly;

    public boolean isDeclaredFieldsOnly() {
        return declaredFieldsOnly;
    }

    public Class<?> getNamespaceClass() {
        return namespaceClass;
    }

    private static synchronized VarExporter getInstance(
            String namespace, final Class namespaceClass, final boolean declaredFieldsOnly) {
        if (Strings.isNullOrEmpty(namespace)) {
            namespace = null;
        }
        VarExporter exporter = namespaces.get(namespace);
        if (exporter == null) {
            exporter = new VarExporter(namespace, namespaceClass, declaredFieldsOnly);
            namespaces.put(namespace, exporter);
        }
        return exporter;
    }

    /**
     * Load an exporter for a given namespace. For the global namespace, use {@link #global()}.
     *
     * @param namespace Namespace to load exporter from; use null for the global namespace or call
     *     {@link #global()}
     * @return exporter for the namespace; will be created if never before accessed.
     */
    public static synchronized VarExporter forNamespace(String namespace) {
        return getInstance(namespace, null, false);
    }

    /**
     * Look up an exporter for the specified namespace.
     *
     * @param namespace the namespace to look up an exporter for
     * @return the exporter for the specified namespace, or {@link Optional#empty()}, if the
     *     namespace does not exist
     */
    public static synchronized Optional<VarExporter> forNamespaceIfExists(
            @NonNull final String namespace) {
        return Optional.ofNullable(namespaces.get(namespace));
    }

    /**
     * Load an exporter with a specified class.
     *
     * @param clazz Class type from which variables will be exported.
     * @param declaredFieldsOnly if true, will not export any variable belonging to superclasses of
     *     {@code clazz}.
     * @return exporter for the given class will be created if never before accessed.
     */
    public static synchronized VarExporter forNamespace(
            @NonNull final Class<?> clazz, final boolean declaredFieldsOnly) {
        return getInstance(clazz.getSimpleName(), clazz, declaredFieldsOnly);
    }

    public static synchronized List<String> getNamespaces() {
        return new ArrayList<String>(namespaces.keySet());
    }

    /**
     * @return exporter for the global namespace, use {@link #forNamespace(String)} for a specific
     *     exporter *
     */
    public static VarExporter global() {
        return forNamespace((String) null);
    }

    /**
     * Use with visitVariables methods to visit all variables in an exporter
     *
     * @deprecated use {@link VariableVisitor} instead
     */
    @Deprecated
    public static interface Visitor extends VariableVisitor {}

    /**
     * Visit all variables in the given namespace
     *
     * @param namespace namespace to visit
     * @param visitor visitor to receive callbacks
     */
    public static void visitNamespaceVariables(String namespace, VariableVisitor visitor) {
        forNamespace(namespace).visitVariables(visitor);
    }

    public static VariableHost withTag(final String tag) {
        return new VariableHost() {
            @Override
            public void visitVariables(VariableVisitor visitor) {
                final Collection<Variable> matched;
                tagsLock.readLock().lock();
                try {
                    matched = tags.get(tag);
                } finally {
                    tagsLock.readLock().unlock();
                }
                if (matched != null) {
                    for (final Variable v : matched) {
                        visitor.visit(v);
                    }
                }
            }

            @Override
            @SuppressWarnings("unchecked")
            public <T> Variable<T> getVariable(final String variableName) {
                final Variable[] foundVariable = new Variable[] {null};
                visitVariables(
                        new VariableVisitor() {
                            public void visit(Variable var) {
                                if (variableName.equals(var.getName())) {
                                    foundVariable[0] = var;
                                    return;
                                }
                            }
                        });
                return foundVariable[0];
            }
        };
    }

    private final String namespace;

    private final Map<String, Variable> variables = Maps.newTreeMap();
    private final Set<String> childVariables = Sets.<String>newTreeSet();

    private VarExporter parent = null;

    private VarExporter(
            @Nullable final String namespace,
            @Nullable final Class namespaceClass,
            final boolean declaredFieldsOnly) {
        this.namespace = namespace == null ? "" : namespace;
        this.namespaceClass = namespaceClass;
        this.declaredFieldsOnly = declaredFieldsOnly;
    }

    /**
     * Export variables subsequently exported into this namespace into the global namespace using
     * "NAMESPACE-" as a prefix.
     *
     * @return the current namespace (not the parent)
     */
    public VarExporter includeInGlobal() {
        if (Strings.isNullOrEmpty(namespace)) {
            // already global
            return this;
        }
        return setParentNamespace(global());
    }

    /**
     * Export variables subsequently exported into this namespace into the given parent namespace
     * using "NAMESPACE-" as a prefix.
     *
     * @param namespace parent namespace
     * @return the current namespace (not the parent)
     */
    public VarExporter setParentNamespace(VarExporter namespace) {
        if (namespace != this) {
            parent = namespace;
        }
        return this;
    }

    public String getNamespace() {
        return namespace;
    }

    public VarExporter getParentNamespace() {
        return parent;
    }

    /**
     * Export all public fields and methods of a given object instance, including static fields,
     * that are annotated with {@link com.indeed.util.varexport.Export}. Also finds annotation on
     * interfaces.
     *
     * @param obj object instance to export
     * @param prefix prefix for variable names (e.g. "mywidget-")
     */
    public void export(Object obj, String prefix) {
        checkTypeCompatibility(obj.getClass());
        final boolean isNamespaceClassSet = namespaceClass != null;
        final Class c = isNamespaceClassSet ? namespaceClass : obj.getClass();
        for (final Field field : (declaredFieldsOnly ? c.getDeclaredFields() : c.getFields())) {
            Export export = field.getAnnotation(Export.class);
            if (Modifier.isStatic(field.getModifiers())) {
                loadMemberVariable(field, export, c, true, prefix, null);
            } else {
                loadMemberVariable(field, export, obj, true, prefix, null);
            }
        }
        Set<Class<?>> classAndInterfaces = Sets.newHashSet();
        getAllInterfaces(c, classAndInterfaces);
        classAndInterfaces.add(c);
        for (Class<?> cls : classAndInterfaces) {
            for (final Method method :
                    (declaredFieldsOnly ? cls.getDeclaredMethods() : cls.getMethods())) {
                Export export = method.getAnnotation(Export.class);
                if (Modifier.isStatic(method.getModifiers())) {
                    loadMemberVariable(method, export, c, true, prefix, null);
                } else {
                    loadMemberVariable(method, export, obj, true, prefix, null);
                }
            }
        }
    }

    private void checkTypeCompatibility(final Class<?> clazz) {
        final boolean isNamespaceClassSet = namespaceClass != null;
        if (isNamespaceClassSet) {
            Preconditions.checkArgument(
                    namespaceClass.isAssignableFrom(clazz),
                    "%s is not compatible with %s",
                    clazz.getCanonicalName(),
                    namespaceClass.getCanonicalName());
        }
    }

    private void getAllInterfaces(Class<?> c, Set<Class<?>> alreadySeen) {
        for (Class<?> i : c.getInterfaces()) {
            alreadySeen.add(i);
        }
        if (c.getSuperclass() != null) {
            getAllInterfaces(c.getSuperclass(), alreadySeen);
        }
    }

    /**
     * Export all public static fields and methods of a given class that are annotated with {@link
     * com.indeed.util.varexport.Export}.
     *
     * @param c class to export
     * @param prefix prefix for variable names (e.g. "mywidget-")
     */
    public void export(Class c, String prefix) {
        checkTypeCompatibility(c);
        for (final Field field : (declaredFieldsOnly ? c.getDeclaredFields() : c.getFields())) {
            if (Modifier.isStatic(field.getModifiers())) {
                Export export = field.getAnnotation(Export.class);
                loadMemberVariable(field, export, c, true, prefix, null);
            }
        }
        for (final Method method : (declaredFieldsOnly ? c.getDeclaredMethods() : c.getMethods())) {
            if (Modifier.isStatic(method.getModifiers())) {
                Export export = method.getAnnotation(Export.class);
                loadMemberVariable(method, export, c, true, prefix, null);
            }
        }
    }

    /**
     * Export a given public {@link Field} or {@link Method} of a given object instance. The member
     * will be exported even if it does not have the {@link Export} annotation. This is mainly
     * useful for exporting variables for which you cannot modify the code to add an annotation.
     *
     * @param obj object instance
     * @param member Field of Method to export
     * @param prefix prefix for variable names (e.g. "mywidget-")
     * @param name Name to use for export (optional, will be ignored if Export annotation used)
     */
    public void export(Object obj, Member member, String prefix, String name) {
        checkTypeCompatibility(obj.getClass());
        Export export = null;
        if (member instanceof AnnotatedElement) {
            export = ((AnnotatedElement) member).getAnnotation(Export.class);
        }
        loadMemberVariable(member, export, obj, false, prefix, name);
    }

    /**
     * Export a given public static {@link Field} or {@link Method} of a given class. The member
     * will be exported even if it does not have the {@link Export} annotation. This is mainly
     * useful for exporting variables for which you cannot modify the code to add an annotation.
     *
     * @param c class from which to export
     * @param member Field of Method to export
     * @param prefix prefix for variable names (e.g. "mywidget-")
     * @param name Name to use for export (optional, will be ignored if Export annotation used)
     */
    public void export(Class c, Member member, String prefix, String name) {
        if (!Modifier.isStatic(member.getModifiers())) {
            throw new UnsupportedOperationException(member + " is not static in " + c.getName());
        }
        export((Object) c, member, prefix, name);
    }

    public void export(LazilyManagedVariable lazilyManagedVariable) {
        addVariable(lazilyManagedVariable);
        loadTagsForVariable(lazilyManagedVariable);
    }

    /**
     * Export a manually-managed variable.
     *
     * @param managedVariable manually-managed variable
     */
    public void export(ManagedVariable managedVariable) {
        addVariable(managedVariable);
        loadTagsForVariable(managedVariable);
    }

    /**
     * Load the current value of a given variable.
     *
     * @param variableName The name of the variable we are looking up.
     * @param <T> The data type of the exported variable.
     * @return Null if the variable was not found. The value otherwise.
     */
    @SuppressWarnings("unchecked")
    public <T> T getValue(final String variableName) {
        final Variable variable = getVariable(variableName);
        return (variable == null) ? null : (T) variable.getValue();
    }

    /**
     * Load the dynamic variable object.
     *
     * @param variableName name of variable
     * @param <T> The data type of the exported variable.
     * @return Null if the variable was not found. The value otherwise.
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> Variable<T> getVariable(final String variableName) {
        final String[] subTokens = getSubVariableTokens(variableName);
        if (subTokens != null) {
            final Variable<T> sub = getSubVariable(subTokens[0], subTokens[1]);
            if (sub != null) {
                return sub;
            }
        }
        final Variable<T> v;
        synchronized (variables) {
            v = variables.get(variableName);
        }
        return v;
    }

    /**
     * Visit all the values exported by this exporter.
     *
     * @param visitor visitor to receive visit callbacks
     */
    @Override
    public void visitVariables(VariableVisitor visitor) {
        // build a collection of live variables in a synchronized block
        final List<Variable> variablesCopy;
        synchronized (variables) {
            variablesCopy = Lists.newArrayListWithExpectedSize(variables.size());
            final Iterator<Variable> iterator = variables.values().iterator();
            while (iterator.hasNext()) {
                final Variable v = iterator.next();
                if (v.isLive()) {
                    variablesCopy.add(v);
                } else {
                    // remove-on-read of variables that are no longer live
                    // javadoc says this removes from the mapping from the map
                    iterator.remove();
                }
            }
        }

        for (Variable v : variablesCopy) {
            if (v.isExpandable()) {
                Map<?, ?> map = v.expand();
                try {
                    for (final Map.Entry entry : map.entrySet()) {
                        visitor.visit(new EntryVariable(entry, v, namespace));
                    }
                } catch (ConcurrentModificationException e) {
                    log.warn("Failed to iterate map entry set for variable " + v.getName(), e);
                    Map.Entry<String, String> errorEntry =
                            new AbstractMap.SimpleEntry<String, String>("error", e.getMessage());
                    visitor.visit(new EntryVariable(errorEntry, v, namespace));
                }
            } else {
                visitor.visit(v);
            }
        }
        if (variablesCopy.size() > 0 && startTime != null) {
            visitor.visit(startTime);
        }
    }

    /** @return an iterator over the exported variables */
    public Iterable<Variable> getVariables() {
        final ImmutableList.Builder<Variable> builder = ImmutableList.builder();
        visitVariables(
                new VariableVisitor() {
                    public void visit(Variable var) {
                        builder.add(var);
                    }
                });
        return builder.build();
    }

    /**
     * Write all variables, one per line, to the given writer, in the format "name=value". Will
     * escape values for compatibility with loading into {@link java.util.Properties}.
     *
     * @param out writer
     * @param includeDoc true if documentation comments should be included
     */
    public void dump(final PrintWriter out, final boolean includeDoc) {
        visitVariables(
                new Visitor() {
                    public void visit(Variable var) {
                        var.write(out, includeDoc);
                    }
                });
    }

    /**
     * Write all variables as a JSON object. Will not escape names or values. All values are written
     * as Strings.
     *
     * @param out writer
     */
    public void dumpJson(final PrintWriter out) {
        out.append("{");
        visitVariables(
                new Visitor() {
                    int count = 0;

                    public void visit(Variable var) {
                        if (count++ > 0) {
                            out.append(", ");
                        }
                        out.append(var.getName())
                                .append("='")
                                .append(String.valueOf(var.getValue()))
                                .append("'");
                    }
                });
        out.append("}");
    }

    /**
     * Remove all exported variables (including those from child namespaces). Also removes variables
     * exported to a parent namespace. Not recommended for use outside of tests.
     */
    @SuppressWarnings("unchecked")
    @VisibleForTesting
    public static void resetGlobal() {
        tagsLock.writeLock().lock();
        try {
            tags.clear();
        } finally {
            tagsLock.writeLock().unlock();
        }

        resetNamespaces();
    }

    private static synchronized void resetNamespaces() {
        namespaces.clear();
    }

    /**
     * @deprecated please just use {@link #resetGlobal()} instead to reset the entirety of
     *     VarExporter
     */
    @SuppressWarnings("unchecked")
    @Deprecated
    public void reset() {
        // remove tag mappings
        tagsLock.writeLock().lock();
        try {
            for (Variable v : variables.values()) {
                final Set<String> varTags = v.getTags();
                for (String tag : varTags) {
                    tags.remove(tag, v);
                }
            }
        } finally {
            tagsLock.writeLock().unlock();
        }

        synchronized (variables) {
            variables.clear();
        }

        if (parent != null) {
            parent.removeChildVariables(namespace);
        }
    }

    protected void removeChildVariables(String namespacePrefix) {
        synchronized (childVariables) {
            final Iterator<String> childIterator = childVariables.iterator();
            while (childIterator.hasNext()) {
                final String childVariableName = childIterator.next();
                if (childVariableName.startsWith(namespacePrefix + "-")) {
                    variables.remove(childVariableName);
                    childIterator.remove();
                }
            }
        }
    }

    private String[] getSubVariableTokens(String variableName) {
        String[] tokens = variableName.split("#", 2);
        if (tokens.length > 1) {
            return tokens;
        }
        return null;
    }

    private Variable getSubVariable(String variableName, String subVariableName) {
        final Variable container;
        synchronized (variables) {
            container = variables.get(variableName);
        }
        if (container != null && container.isExpandable()) {
            Map<?, ?> map = container.expand();
            try {
                for (Map.Entry entry : map.entrySet()) {
                    Object key = entry.getKey();
                    if (String.valueOf(key).equals(subVariableName)) {
                        return new EntryVariable(entry, container, namespace);
                    }
                }
            } catch (ConcurrentModificationException e) {
                log.warn("Failed to iterate map entry set for variable " + variableName, e);
                return null;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private void addChildVariable(String childNamespace, Variable variable) {
        // there is no need to propagate tags from the child
        final Set<String> noTags = ImmutableSet.of();
        final ProxyVariable v =
                new ProxyVariable(
                        childNamespace + "-" + variable.getName(), variable, noTags, namespace);
        addVariable(v);
        synchronized (childVariables) {
            childVariables.add(v.getName());
        }
    }

    @SuppressWarnings("unchecked")
    private void addVariable(Variable variable) {
        final Variable prev;
        synchronized (variables) {
            prev = variables.put(variable.getName(), variable);
        }
        if (prev != null) {
            log.warn(
                    "In namespace '"
                            + namespace
                            + "': Exporting variable named "
                            + variable.getName()
                            + " hides a previously exported variable");
        } else {
            if (log.isDebugEnabled()) {
                log.debug("In namespace '" + namespace + "': Added variable " + variable.getName());
            }
        }
        if (parent != null && parent != this) {
            parent.addChildVariable(namespace, variable);
        }
    }

    @SuppressWarnings("unchecked")
    private void loadTagsForVariable(Variable variable) {
        tagsLock.writeLock().lock();
        try {
            final Set<String> variableTags = variable.getTags();
            for (String tag : variableTags) {
                tags.put(tag, variable);
            }
        } finally {
            tagsLock.writeLock().unlock();
        }
    }

    @SuppressWarnings("unchecked")
    private void loadMemberVariable(
            Member member,
            Export export,
            Object obj,
            boolean requireAnnotation,
            String prefix,
            String name) {
        if (!requireAnnotation || export != null) {
            Variable variable = variableFromMember(export, prefix, name, member, obj);
            if (export != null && export.cacheTimeoutMs() > 0) {
                variable = new CachingVariable(variable, export.cacheTimeoutMs(), namespace);
            }
            loadTagsForVariable(variable);
            addVariable(variable);
        }
    }

    private String getVarName(Export export, String supplied, Member member) {
        if (export != null && export.name() != null && export.name().length() > 0) {
            return export.name();
        } else if (supplied != null && supplied.length() > 0) {
            return supplied;
        } else {
            return member.getName();
        }
    }

    @SuppressWarnings("unchecked")
    private Variable variableFromMember(
            Export export, String prefix, String suppliedName, Member member, Object obj) {
        final String name =
                (prefix != null ? prefix : "") + getVarName(export, suppliedName, member);
        final String doc = export != null ? export.doc() : "";
        final Set<String> tags =
                ImmutableSet.copyOf(export != null ? export.tags() : new String[0]);
        final boolean expand = export != null ? export.expand() : false;
        if (member instanceof Field) {
            return new FieldVariable(name, doc, tags, expand, (Field) member, obj, namespace);
        } else if (member instanceof Method) {
            return new MethodVariable(name, doc, tags, expand, (Method) member, obj, namespace);
        } else {
            throw new UnsupportedOperationException(member.getClass() + " not supported by export");
        }
    }

    @VisibleForTesting
    protected static ManagedVariable<String> createStartTimeVariable(final Date date) {
        final SimpleDateFormat timestampFormat =
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz"); // ISO 8601
        return ManagedVariable.<String>builder()
                .setName("exporter-start-time")
                .setDoc("global start time of variable exporter")
                .setValue(timestampFormat.format(date))
                .build();
    }

    private static class FieldVariable<T> extends Variable<T> {
        private final Field field;
        private final WeakReference<Object> objectRef;

        public FieldVariable(
                String name,
                String doc,
                Set<String> tags,
                boolean expand,
                Field field,
                Object object,
                String namespace) {
            super(name, tags, doc, expand, namespace);
            this.field = field;
            field.setAccessible(true);
            this.objectRef = new WeakReference<Object>(object);
            if (Map.class.isAssignableFrom(field.getType())
                    && !ImmutableMap.class.isAssignableFrom(field.getType())) {
                log.warn(
                        "Variable "
                                + getNamespaceAndName()
                                + " is not an ImmutableMap, which may result in sporadic errors");
            }
        }

        @Override
        protected boolean isLive() {
            return objectRef.get() != null; // true if object has not been GC'd
        }

        protected boolean canExpand() {
            return Map.class.isAssignableFrom(field.getType()) && getValue() != null;
        }

        @SuppressWarnings("unchecked")
        public T getValue() {
            final Object object = objectRef.get();
            if (object == null) {
                return null;
            }
            try {
                return (T) field.get(object);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class MethodVariable<T> extends Variable<T> {
        private final Method method;
        private final WeakReference<Object> objectRef;

        public MethodVariable(
                String name,
                String doc,
                Set<String> tags,
                boolean expand,
                Method method,
                Object object,
                String namespace) {
            super(name, tags, doc, expand, namespace);
            this.method = method;
            method.setAccessible(true);
            this.objectRef = new WeakReference<Object>(object);
            if (Map.class.isAssignableFrom(method.getReturnType())
                    && !ImmutableMap.class.isAssignableFrom(method.getReturnType())) {
                log.warn(
                        "Variable "
                                + getNamespaceAndName()
                                + " is not an ImmutableMap, which may result in sporadic errors");
            }
        }

        @Override
        protected boolean isLive() {
            return objectRef.get() != null; // true if object has not been GC'd
        }

        protected boolean canExpand() {
            return Map.class.isAssignableFrom(method.getReturnType()) && getValue() != null;
        }

        @SuppressWarnings("unchecked")
        public T getValue() {
            final Object object = objectRef.get();
            if (object == null) {
                return null;
            }
            try {
                return (T) method.invoke(object);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class EntryVariable extends Variable {
        private final WeakReference<Object> valueRef;
        private final Variable parent;

        @SuppressWarnings("unchecked")
        public EntryVariable(Map.Entry entry, Variable parent, String namespace) {
            super(
                    parent.getName() + "#" + entry.getKey(),
                    parent.getTags(),
                    null,
                    false,
                    namespace);
            this.valueRef = new WeakReference<Object>(entry.getValue());
            this.parent = parent;
            final Object value = valueRef.get();
            if (value != null
                    && Map.class.isAssignableFrom(value.getClass())
                    && !ImmutableMap.class.isAssignableFrom(value.getClass())) {
                log.warn(
                        "Variable "
                                + getNamespaceAndName()
                                + "#"
                                + entry.getKey()
                                + " is not an ImmutableMap, which may result in sporadic errors");
            }
        }

        @Override
        protected boolean isLive() {
            return valueRef.get() != null;
        }

        protected boolean canExpand() {
            final Object value = valueRef.get();
            if (value != null) {
                return Map.class.isAssignableFrom(value.getClass()) && getValue() != null;
            } else {
                return false;
            }
        }

        @Override
        public String getDoc() {
            return parent.getDoc();
        }

        @Override
        public Long getLastUpdated() {
            return parent.getLastUpdated();
        }

        public Object getValue() {
            return valueRef.get();
        }
    }

    protected static class CachingVariable<T> extends ProxyVariable<T> {

        private final long timeout;

        private T cachedValue = null;
        private long lastCached = 0;

        private Supplier<Long> clock =
                new Supplier<Long>() {
                    public Long get() {
                        return System.currentTimeMillis();
                    }
                };

        public CachingVariable(Variable<T> variable, long timeout, String namespace) {
            super(variable.getName(), variable, variable.getTags(), namespace);
            this.timeout = timeout;
        }

        @VisibleForTesting
        protected void setClock(Supplier<Long> clock) {
            this.clock = clock;
        }

        private boolean isCacheExpired() {
            return lastCached == 0 || clock.get() - lastCached > timeout;
        }

        @Override
        public Long getLastUpdated() {
            return lastCached;
        }

        @Override
        public T getValue() {
            if (isCacheExpired()) {
                cachedValue = super.getValue();
                lastCached = clock.get();
            }
            return cachedValue;
        }
    }

    protected static class ProxyVariable<T> extends Variable<T> {
        private final Variable<T> variable;

        public ProxyVariable(
                String name, Variable<T> variable, Set<String> tags, String namespace) {
            super(name, tags, variable.getDoc(), variable.isExpandable(), namespace);
            this.variable = variable;
        }

        @Override
        protected boolean isLive() {
            return variable.isLive();
        }

        @Override
        protected boolean canExpand() {
            return variable.canExpand();
        }

        public T getValue() {
            return variable.getValue();
        }

        @Override
        public Long getLastUpdated() {
            return variable.getLastUpdated();
        }
    }
}
