// Copyright 2009 Indeed
package com.indeed.util.varexport;

import com.google.common.base.Supplier;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.indeed.util.varexport.external.PublicClass;

import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Level;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.apache.logging.log4j.core.appender.ConsoleAppender.Target;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author jack@indeed.com (Jack Humphrey)
 */
public class VarExporterTest {

    private static class ExampleClass {
        @Export(name="ex1field", doc="Example variable 1", tags = { "IAmAwesome" })
        public int ex1 = 1;

        @Export(name="ex1method", tags = { "Fail" })
        public int getEx1() { return ex1; }

        @Export(name="static1field", doc="Static field example 1")
        public static long static1 = System.currentTimeMillis();

        @Export(name="static1method")
        public static long getStatic1() { return static1 + 1; }

        @Export(tags= { "IAmAwesome", "Fail" })
        public static int myNameIsEarl = 0;

        public int notExported = 0;

        public int getNotExported() { return notExported; }

        public static int staticNotExported = 0;

        public static int getStaticNotExported() { return staticNotExported; }

        private boolean prv = false;
    }

    private static class ExampleSubclass extends ExampleClass {
        @Export(name="subm1")
        public int getZero() { return 0; }
    }

    private static class ExampleNeedingEscaping {
        @Export(name="BulgariaŴhatsUp", doc="ŴŴŴŴ")
        public static String helloBulgaria() { return "Say Ŵhat?"; }

        @Export(name="foo:bar=hi", doc=":::=test=:::")
        public static String yoSeps() { return "x:=y"; }
    }

    private interface ExampleInterface {
        @Export(name="ifcmethod1")
        String method();
    }

    private static abstract class AbstractClassImplementingInterface implements ExampleInterface {
        public String method() { return ""; }

        @Export(name="something#else")
        public String somethingElse() { return "yes"; }
    }

    private static class ConcreteClassImplementingInterfaceAgain extends AbstractClassImplementingInterface implements ExampleInterface {
    }

    private static class ExampleWithMap {
        @Export(name="map")
        public Map<Long, String> map = Maps.newHashMap();
        {
            map.put(1L, "one");
            map.put(2L, "two");
            map.put(524L, "five hundred twenty-four");
        }

        @Export(name="blowoutMap", expand=true)
        public Map<Long, String> getBlowout() {
            return map;
        }
    }

    private static class ExampleWithTwoLevelMap {
        @Export(name="map", expand=true, cacheTimeoutMs=1L)
        public Map<String, Map<String, String>> map = Maps.newHashMap();
        {
            map.put("m1", Maps.newLinkedHashMap());
            map.get("m1").put("1", "one");
            map.get("m1").put("2", "two");
            map.put("m2", Maps.newLinkedHashMap());
            map.get("m2").put("one", "1");
            map.get("m2").put("two", "2");
        }
    }

    private static class ExampleWithConcurrentModificationMap {
        @Export(name="map", expand=true)
        public Map<String, String> map = new AbstractMap<String, String>() {
            @Override
            public Set<Entry<String, String>> entrySet() {
                return new AbstractSet<Entry<String, String>>() {
                    @Override
                    public Iterator<Entry<String, String>> iterator() {
                        return new AbstractIterator<Entry<String, String>>() {
                            @Override
                            protected Entry<String, String> computeNext() {
                                throw new ConcurrentModificationException("whoops!");
                            }
                        };
                    }

                    @Override
                    public int size() {
                        return 20;
                    }
                };
            }
        };
    }

    private static class ExampleWithCaching {
        @Export(name="num", doc="cached number", cacheTimeoutMs=10000L)
        public int num = 0;

        @Export(name="nodoc", cacheTimeoutMs=10000L)
        public String nodoc = "hi";

        private AtomicReference<Integer> valRef = new AtomicReference<>(num);
        @Export(name="val", doc="cached value", cacheTimeoutMs=10000L)
        public Integer getVal() {
            return valRef.get();
        }
    }

    private static ManagedVariable<String> startTimeBackup = VarExporter.createStartTimeVariable(new Date());

    VarExporter exporter;

    private long curTime = 0L;
    private Supplier<Long> testClock = () -> curTime;

    @Before
    public void setUp() throws Exception {
        VarExporter.resetGlobal();
        exporter = VarExporter.global();

        VarExporter.startTime = null;
    }

    @BeforeClass
    public static void initClass() {
        ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();
        final Configuration config = builder.setStatusLevel(Level.ERROR)
                                      .setConfigurationName("Indeed Logging")
                                      .add(builder.newAppender("stderr", "Console")
                                                  .addAttribute("target", Target.SYSTEM_ERR))
                                      .add(builder.newAsyncLogger("com.indeed", Level.DEBUG)
                                                  .add(builder.newAppenderRef("stderr"))
                                                  .addAttribute("addivity", false))
                                      .add(builder.newAsyncRootLogger(Level.ERROR)
                                                  .add(builder.newAppenderRef("stderr"))
                                                  .addAttribute("additivity", false))
                                      .build();
        Configurator.initialize(builder.build());
    }

    @After
    public void tearDown() throws Exception {
        VarExporter.resetGlobal();
    }

    private static Collection<String> getExportedNames(VarExporter exporter) {
        final List<String> names = Lists.newLinkedList();
        exporter.visitVariables(var -> names.add(var.getName()));
        return names;
    }

    private static void assertExportedNames(VarExporter exporter, String... names) {
        Collection<String> exportedNames = getExportedNames(exporter);
        Assert.assertThat(exportedNames, Matchers.containsInAnyOrder(names));
    }

    @Test
    public void testForNamespace() {
        VarExporter alt = VarExporter.forNamespace("foo");
        Assert.assertFalse(alt.getVariables().iterator().hasNext());
        VarExporter.forNamespace("foo").export(ExampleClass.class, "");
        assertExportedNames(alt, "static1field", "static1method", "myNameIsEarl");
        assertExportedNames(exporter);
    }

    @Test
    public void testExportInstance() {
        ExampleClass instance = new ExampleClass();
        exporter.export(instance, "");
        assertExportedNames(exporter, "ex1field", "ex1method", "static1field", "static1method", "myNameIsEarl");
        assertEquals(1, (int) exporter.getValue("ex1field"));
        assertEquals(1, (int) exporter.getValue("ex1method"));
        instance.ex1++;
        assertEquals(2, (int) exporter.getValue("ex1field"));
        assertEquals(2, (int) exporter.getValue("ex1method"));
    }

    private void assertHasTags(Set<String> tags, String... expectedTagNames) {
        assertEquals(expectedTagNames.length, tags.size());
        assertThat(tags, Matchers.containsInAnyOrder(expectedTagNames));
    }

    private void assertVariableNamesForTag(String tag, String... expectedNames) {
        final List<String> variableNames = Lists.newLinkedList();
        VarExporter.withTag(tag).visitVariables(
                var -> variableNames.add(var.getName())
        );
        assertThat(variableNames, Matchers.containsInAnyOrder(expectedNames));
    }

    @Test
    public void testTags() {
        ExampleClass instance = new ExampleClass();
        exporter.export(instance, "");
        assertHasTags(exporter.getVariable("myNameIsEarl").getTags(), "IAmAwesome", "Fail");
        assertHasTags(exporter.getVariable("ex1field").getTags(), "IAmAwesome");
        assertHasTags(exporter.getVariable("ex1method").getTags(), "Fail");

        assertVariableNamesForTag("IAmAwesome", "myNameIsEarl", "ex1field");
        assertVariableNamesForTag("Fail", "myNameIsEarl", "ex1method");
    }

    @Test
    public void testNamespaceIsNull() throws Exception {
        // the namespace is stored in the map at key NULL
        // but null/global/"" are all considered the same namespace
        assertSame(VarExporter.forNamespace(""), VarExporter.forNamespace(null));
        assertSame(VarExporter.global(), VarExporter.forNamespace(null));
        assertSame(VarExporter.global(), VarExporter.forNamespace(""));

        assertTrue(Lists.newArrayList(VarExporter.getNamespaces()).contains(null));
        assertFalse(Lists.newArrayList(VarExporter.getNamespaces()).contains(""));
    }

    @Test
    public void testTagWithNamespace() throws Exception {
        final VarExporter global = VarExporter.global();
        final VarExporter local = VarExporter.forNamespace("local");
        final VarExporter other = VarExporter.forNamespace("other");
        final ExampleClass instance = new ExampleClass();
        local.includeInGlobal().export(instance, "");
        other.includeInGlobal().export(instance, "");

        assertHasTags(global.getVariable("local-myNameIsEarl").getTags());
        assertEquals("", global.getVariable("local-myNameIsEarl").getNamespace());
        assertEquals("", global.getVariable("other-myNameIsEarl").getNamespace());

        final Variable<Object> localVariable = local.getVariable("myNameIsEarl");
        assertHasTags(localVariable.getTags(), "IAmAwesome", "Fail");
        assertEquals("local", localVariable.getNamespace());

        assertEquals("other", other.getVariable("myNameIsEarl").getNamespace());

        final AtomicInteger locals = new AtomicInteger(0);
        final AtomicInteger others = new AtomicInteger(0);
        // assert that all of the IAmAwesome tag variables are in the local or other namespace
        VarExporter.withTag("IAmAwesome").visitVariables(var -> {
            if ("local".equals(var.getNamespace())) {
                locals.incrementAndGet();
            } else if ("other".equals(var.getNamespace())) {
                others.incrementAndGet();
            } else {
                fail("Saw namespace " + var.getNamespace() + " when not expecting it");
            }
        });
        assertEquals(2, locals.get());
        assertEquals(2, others.get());
    }

    @Test
    public void testWeakReference() {
        ExampleClass instance = new ExampleClass();
        exporter.export(instance, "");
        assertExportedNames(exporter, "ex1field", "ex1method", "static1field", "static1method", "myNameIsEarl");
        instance = null;
        System.gc();
        assertExportedNames(exporter, "myNameIsEarl", "static1field", "static1method");
    }

    @Test
    public void testExportSubclassInstance() {
        ExampleSubclass instance = new ExampleSubclass();
        exporter.export(instance, "");
        assertExportedNames(exporter, "ex1field", "ex1method", "static1field", "static1method", "subm1", "myNameIsEarl");
    }

    @Test
    public void testExportClass() {
        exporter.export(ExampleClass.class, "");
        assertExportedNames(exporter, "static1field", "static1method", "myNameIsEarl");
        ExampleClass.static1 = 52473L;
        assertEquals(52473L, (long) exporter.getValue("static1field"));
        assertEquals(52474L, (long) exporter.getValue("static1method"));
    }

    @Test
    public void testExportInstanceFields() throws Exception {
        ExampleClass instance = new ExampleClass();

        // export field with a custom name
        exporter.export(instance, ExampleClass.class.getField("notExported"), "", "not-exported");
        assertExportedNames(exporter, "not-exported");
        assertEquals(0, (int) exporter.getValue("not-exported"));
        instance.notExported = 1;
        assertEquals(1, (int) exporter.getValue("not-exported"));

        // export field with default name
        exporter.export(instance, ExampleClass.class.getField("notExported"), "", null);
        assertExportedNames(exporter, "not-exported", "notExported");
        assertEquals(1, (int) exporter.getValue("notExported"));

        // export field with @Export
        exporter.export(instance, ExampleClass.class.getField("ex1"), "", "WONTUSE");
        assertExportedNames(exporter, "not-exported", "notExported", "ex1field");
        instance.ex1 = 999;
        assertEquals(999, (int) exporter.getValue("ex1field"));
    }

    @Test
    public void testExportInstanceMethods() throws Exception {
        ExampleClass instance = new ExampleClass();

        // export field with a custom name
        exporter.export(instance, ExampleClass.class.getMethod("getNotExported"), "", "not-exported");
        assertExportedNames(exporter, "not-exported");
        assertEquals(0, (int) exporter.getValue("not-exported"));
        instance.notExported = 1;
        assertEquals(1, (int) exporter.getValue("not-exported"));

        // export field with default name
        exporter.export(instance, ExampleClass.class.getMethod("getNotExported"), "", null);
        assertExportedNames(exporter, "not-exported", "getNotExported");
        assertEquals(1, (int) exporter.getValue("getNotExported"));

        // export field with @Export
        exporter.export(instance, ExampleClass.class.getMethod("getEx1"), "", "WONTUSE");
        assertExportedNames(exporter, "not-exported", "getNotExported", "ex1method");
        instance.ex1 = 999;
        assertEquals(999, (int) exporter.getValue("ex1method"));
    }

    @Test
    public void testExportStaticFields() throws Exception {
        // export field with a custom name
        exporter.export(ExampleClass.class, ExampleClass.class.getField("staticNotExported"), "", "not-exported");
        assertExportedNames(exporter, "not-exported");
        ExampleClass.staticNotExported = 0;
        assertEquals(0, (int) exporter.getValue("not-exported"));
        ExampleClass.staticNotExported = 1;
        assertEquals(1, (int) exporter.getValue("not-exported"));

        // export field with default name
        exporter.export(ExampleClass.class, ExampleClass.class.getField("staticNotExported"), "", null);
        assertExportedNames(exporter, "not-exported", "staticNotExported");
        assertEquals(1, (int) exporter.getValue("staticNotExported"));

        // export field with @Export
        exporter.export(ExampleClass.class, ExampleClass.class.getField("static1"), "", "WONTUSE");
        assertExportedNames(exporter, "not-exported", "staticNotExported", "static1field");
        ExampleClass.static1 = 999L;
        assertEquals(999L, (long) exporter.getValue("static1field"));
    }

    @Test
    public void testExportStaticMethods() throws Exception {
        // export field with a custom name
        exporter.export(ExampleClass.class, ExampleClass.class.getMethod("getStaticNotExported"), "", "not-exported");
        assertExportedNames(exporter, "not-exported");
        ExampleClass.staticNotExported = 0;
        assertEquals(0, (int) exporter.getValue("not-exported"));
        ExampleClass.staticNotExported = 1;
        assertEquals(1, (int) exporter.getValue("not-exported"));

        // export field with default name
        exporter.export(ExampleClass.class, ExampleClass.class.getMethod("getStaticNotExported"), "", null);
        assertExportedNames(exporter, "not-exported", "getStaticNotExported");
        assertEquals(1, (int) exporter.getValue("getStaticNotExported"));

        // export field with @Export
        exporter.export(ExampleClass.class, ExampleClass.class.getMethod("getStatic1"), "", "WONTUSE");
        assertExportedNames(exporter, "not-exported", "getStaticNotExported", "static1method");
        ExampleClass.static1 = 999L;
        assertEquals(1000L, (long) exporter.getValue("static1method"));
    }

    @Test
    public void testExportStartTime() throws Exception {
        VarExporter global = VarExporter.global();

        assertNotNull("Start Time Backup should not be null", startTimeBackup);
        // don't export for empty namespace
        VarExporter.startTime = startTimeBackup;
        assertEquals(0, getExportedNames(global).size());
        global.export(ManagedVariable.<Long>builder().setName("mv").build());

        // prevent export
        VarExporter.startTime = null;
        assertExportedNames(global, "mv");

        // allow export
        VarExporter.startTime = startTimeBackup;
        assertExportedNames(global, "mv", "exporter-start-time");
    }

    @Test
    public void testPrefix() {
        exporter.export(ExampleClass.class, "prefix-");
        assertExportedNames(exporter, "prefix-static1field", "prefix-static1method", "prefix-myNameIsEarl");
        ExampleClass.static1 = 52473L;
        assertEquals(52473L, (long) exporter.getValue("prefix-static1field"));
        assertEquals(52474L, (long) exporter.getValue("prefix-static1method"));
    }

    @Test
    public void testExportPrivateField() throws Exception {
        ExampleClass instance = new ExampleClass();
        try {
            exporter.export(instance, ExampleClass.class.getField("prv"), "", "private");
            Assert.fail("expected NoSuchFieldException");
        } catch (NoSuchFieldException e) {
            // expected
        }
    }

    @Test
    public void testDump() {
        exporter.export(ExampleClass.class, "");
        assertExportedNames(exporter, "static1field", "static1method", "myNameIsEarl");
        ExampleClass.static1 = 0;
        StringWriter out = new StringWriter();
        exporter.dump(new PrintWriter(out, true), false);
        String dump = out.toString();
        assertNotNull(dump);
        assertThat(Lists.newArrayList(dump.split("\n")),
                   Matchers.contains("myNameIsEarl=0", "static1field=0", "static1method=1"));
    }

    @Test
    public void testDumpJson() {
        exporter.export(ExampleClass.class, "");
        assertExportedNames(exporter, "static1field", "static1method", "myNameIsEarl");
        ExampleClass.static1 = 0;
        StringWriter out = new StringWriter();
        exporter.dumpJson(new PrintWriter(out, true));
        String dump = out.toString();
        assertEquals("{myNameIsEarl='0', static1field='0', static1method='1'}", dump);
    }

    @Test
    public void testDumpWithDoc() {
        exporter.export(ExampleClass.class, "");
        assertExportedNames(exporter, "static1field", "static1method", "myNameIsEarl");
        ExampleClass.static1 = 0;
        StringWriter out = new StringWriter();
        exporter.dump(new PrintWriter(out, true), true);
        String dump = out.toString();
        assertNotNull(dump);
        assertThat(
                Lists.newArrayList(dump.split("\n")),
                Matchers.contains("", "myNameIsEarl=0", "", "# Static field example 1", "static1field=0", "", "static1method=1")
        );
    }

    @Test
    public void testVisitNamespaceVariables() throws Exception {
        VarExporter alt = VarExporter.forNamespace("alt");
        ExampleClass instance = new ExampleClass();
        alt.export(instance, "");
        alt.export(instance, ExampleClass.class.getField("notExported"), "", null);
        final AtomicInteger count = new AtomicInteger(0);
        VarExporter.visitNamespaceVariables("alt", var -> count.incrementAndGet());
        assertEquals(6, count.get());
    }

    @Test
    public void testEscaping() throws Exception {
        exporter.export(ExampleNeedingEscaping.class, "");
        StringWriter out = new StringWriter();
        exporter.dump(new PrintWriter(out, true), true);
        String dump = out.toString();
        assertNotNull(dump);
        Assert.assertThat(
            Lists.newArrayList(dump.split("\n")),
            Matchers.contains("", "# \\u0174\\u0174\\u0174\\u0174", "Bulgaria\\u0174hatsUp=Say \\u0174hat?",
                              "", "# :::=test=:::", "foo\\:bar\\=hi=x\\:\\=y"));


        Properties props = new Properties();
        props.load(new StringReader(dump));
        assertEquals(2, props.size());
        assertEquals("Say Ŵhat?", props.get("BulgariaŴhatsUp"));
        assertEquals("x:=y", props.get("foo:bar=hi"));

        Variable var = exporter.getVariable("BulgariaŴhatsUp");
        assertNotNull(var);
        assertEquals("BulgariaŴhatsUp", var.getName());
        assertEquals("ŴŴŴŴ", var.getDoc());
        assertEquals("Say Ŵhat?", var.getValue());
        out = new StringWriter();
        var.writeValue(new PrintWriter(out, true));
        dump = out.toString();
        assertNotNull(dump);
        assertEquals("Say \\u0174hat?", dump);

        var = exporter.getVariable("foo:bar=hi");
        assertNotNull(var);
        assertEquals("foo:bar=hi", var.getName());
        assertEquals(":::=test=:::", var.getDoc());
        assertEquals("x:=y", var.getValue());
        out = new StringWriter();
        var.writeValue(new PrintWriter(out, true));
        dump = out.toString();
        assertNotNull(dump);
        assertEquals("x\\:\\=y", dump);
    }

    @Test
    public void testNullValue() {
        exporter.export(new Object() {
            @Export(name="x")
            public Object x() { return null; }
        }, "");
        Assert.assertNull(exporter.getValue("x"));
        Variable var = exporter.getVariable("x");
        assertEquals("x=null", var.toString());
        StringWriter out = new StringWriter();
        exporter.dump(new PrintWriter(out, true), false);
        assertEquals("x=null\n", out.toString());
        out = new StringWriter();
        var.writeValue(new PrintWriter(out, true));
        assertEquals("null", out.toString());
    }

    @Test
    public void testInterface() {
        exporter.export(new ExampleInterface() {
            public String method() {
                return "test";
            }

            @Export(name="local")
            public int local = 50;
        }, "");
        assertEquals("test", exporter.getValue("ifcmethod1"));
        assertEquals(50, (int) exporter.getValue("local"));
    }

    @Test
    public void testAbstractClassImplementingInterface() {
        exporter.export(new AbstractClassImplementingInterface() {}, "");
        assertEquals("", exporter.getValue("ifcmethod1"));
        assertEquals("yes", exporter.getValue("something#else"));
    }

    @Test
    public void testConcreteClassImplementingInterfaceAgain() {
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        ctx.getConfiguration().getLoggerConfig(LogManager.ROOT_LOGGER_NAME).setLevel(Level.WARN);
        ctx.updateLoggers(); 
        exporter.export(new ConcreteClassImplementingInterfaceAgain(), "");
        assertEquals("", exporter.getValue("ifcmethod1"));
        assertEquals("yes", exporter.getValue("something#else"));
    }

    @Test
    public void testExportMap() {
        exporter.export(new ExampleWithMap(), "");
        assertEquals("{1=one, 2=two, 524=five hundred twenty-four}", exporter.getValue("map").toString());
        Map<Long, String> map = exporter.getValue("map");

        assertThat(
            map.keySet(),
            Matchers.containsInAnyOrder(1L, 2L, 524L));
        assertThat(
            map.values(),
            Matchers.containsInAnyOrder("one", "two", "five hundred twenty-four"));
        StringWriter out = new StringWriter();
        exporter.dump(new PrintWriter(out, true), false);
        assertThat(
            Lists.newArrayList(out.toString().split("\n")),
            Matchers.containsInAnyOrder(
                "blowoutMap#1=one", "blowoutMap#2=two", "blowoutMap#524=five hundred twenty-four",
                "map={1\\=one, 2\\=two, 524\\=five hundred twenty-four}"));
        assertNotNull(exporter.getVariable("blowoutMap#1"));
        assertEquals("one", exporter.getValue("blowoutMap#1"));
        assertNotNull(exporter.getVariable("blowoutMap#2"));
        assertEquals("two", exporter.getValue("blowoutMap#2"));
        assertNotNull(exporter.getVariable("blowoutMap#524"));
        assertEquals("five hundred twenty-four", exporter.getValue("blowoutMap#524"));

        List<Variable> allVariables = Lists.newArrayList(exporter.getVariables());
        assertEquals(4, allVariables.size());
    }

    @Test
    public void testExportNullMap() {
        ExampleWithMap example = new ExampleWithMap();
        example.map = null;
        exporter.export(example, "");
        Assert.assertNull(exporter.getValue("map"));
        Assert.assertNull(exporter.getValue("blowoutMap"));
        StringWriter out = new StringWriter();
        exporter.dump(new PrintWriter(out, true), false);
        assertThat(
                Lists.newArrayList(out.toString().split("\n")), Matchers.contains("blowoutMap=null", "map=null")
        );
    }

    @Test
    public void testExportConcurrentModificationMap() {
        ExampleWithConcurrentModificationMap example = new ExampleWithConcurrentModificationMap();
        exporter.export(example, "");
        assertNotNull(exporter.getValue("map"));
        StringWriter out = new StringWriter();
        exporter.dump(new PrintWriter(out, true), false);
        assertThat(
            Lists.newArrayList(out.toString().split("\n")),
            Matchers.contains("map#error=whoops!"));
    }

    @Test
    public void testExportTwoLevelMap() {
        ExampleWithTwoLevelMap example = new ExampleWithTwoLevelMap();
        example.map.put("nullvalue", null);
        exporter.export(example, "");
        StringWriter out = new StringWriter();
        exporter.dump(new PrintWriter(out, true), false);

        // NOTE: multi-level maps are not yet supported
        assertThat(
            Lists.newArrayList(out.toString().split("\n")),
            Matchers.contains("map#nullvalue=null", "map#m1={1\\=one, 2\\=two}", "map#m2={one\\=1, two\\=2}"));
        assertNotNull(exporter.getValue("map"));
        assertNotNull(exporter.getValue("map#m1"));
        assertNull(exporter.getValue("map#m1#2"));
        assertNull(exporter.getValue("map#m1#1"));
        assertNotNull(exporter.getValue("map#m2"));
        assertNull(exporter.getValue("map#m2#two"));
        assertNull(exporter.getValue("map#m2#two"));
    }

    @Test
    public void testCaching() {
        ExampleWithCaching example = new ExampleWithCaching();
        exporter.export(example, "");
        VarExporter.CachingVariable<Integer> variable =
                (VarExporter.CachingVariable<Integer>) exporter.<Integer>getVariable("num");
        variable.setClock(testClock);
        variable = (VarExporter.CachingVariable<Integer>) exporter.<Integer>getVariable("nodoc");
        variable.setClock(testClock);
        variable = (VarExporter.CachingVariable<Integer>) exporter.<Integer>getVariable("val");
        variable.setClock(testClock);

        curTime = 1000L;
        assertEquals(0, (int) exporter.getValue("num"));
        example.num = 1;
        curTime = 2000L;
        assertEquals(0, (int) exporter.getValue("num"));
        curTime = 11000L;
        assertEquals(0, (int) exporter.getValue("num"));
        curTime = 11001L;
        assertEquals(1, (int) exporter.getValue("num"));
        example.num = 2;
        curTime = 15000L;
        assertEquals(1, (int) exporter.getValue("num"));
        curTime = 25000L;
        assertEquals(2, (int) exporter.getValue("num"));

        StringWriter out = new StringWriter();
        exporter.dump(new PrintWriter(out, true), true);
        assertThat(
            Lists.newArrayList(out.toString().split("\n")),
            Matchers.contains(
                "", "#  (last update: 25000)", "nodoc=hi",
                "", "# cached number (last update: 25000)", "num=2",
                "", "# cached value (last update: 25000)", "val=0"
            )
        );
    }

    @Test
    public void testCachingInNamespace() {
        final VarExporter alt = VarExporter.forNamespace("foo").includeInGlobal();

        ExampleWithCaching example = new ExampleWithCaching();
        alt.export(example, "");
        assertVariableCount(alt, 3);
        assertVariableCount(exporter, 3);
        VarExporter.CachingVariable<Integer> variable =
                (VarExporter.CachingVariable<Integer>) alt.<Integer>getVariable("val");
        variable.setClock(testClock);

        curTime = 1000L;
        assertEquals(0, (int) alt.getValue("val"));
        example.valRef.set(1);
        curTime = 11001L;
        assertEquals(1, (int) alt.getValue("val"));
        example.valRef.set(2);
        curTime = 15000L;
        assertEquals(1, (int) exporter.getValue("foo-val"));
        assertEquals(1, (int) alt.getValue("val"));
        curTime = 25000L;
        assertEquals(2, (int) exporter.getValue("foo-val"));
        assertEquals(2, (int) alt.getValue("val"));
    }

    private void assertVariableCount(VarExporter exporter, int expected) {
        final AtomicInteger count = new AtomicInteger(0);
        exporter.visitVariables(var -> count.incrementAndGet());
        assertEquals(expected, count.get());
    }

    @Test
    public void testParentGlobal() {
        VarExporter alt = VarExporter.forNamespace("foo");
        assertEquals(alt, alt.includeInGlobal());

        Assert.assertFalse(alt.getVariables().iterator().hasNext());
        VarExporter.forNamespace("foo").export(ExampleClass.class, "");
        assertExportedNames(alt, "static1field", "static1method", "myNameIsEarl");
        assertVariableCount(alt, 3);
        assertExportedNames(exporter, "foo-static1field", "foo-static1method", "foo-myNameIsEarl");
        assertVariableCount(exporter, 3);
    }

    @Test
    public void testParent() {
        VarExporter foo = VarExporter.forNamespace("foo");
        assertEquals(foo, foo.setParentNamespace(VarExporter.global()));
        VarExporter bar = VarExporter.forNamespace("bar");
        assertEquals(bar, bar.setParentNamespace(foo));

        Assert.assertFalse(bar.getVariables().iterator().hasNext());
        VarExporter.forNamespace("bar").export(new ExampleClass(), "");
        VarExporter.forNamespace("bar").export(ManagedVariable.<Long>builder().setName("mv").build());
        assertExportedNames(bar, "static1field", "static1method", "myNameIsEarl", "ex1method", "ex1field", "mv");
        assertExportedNames(foo, "bar-static1field", "bar-static1method", "bar-myNameIsEarl", "bar-ex1method", "bar-ex1field", "bar-mv");
        assertExportedNames(exporter, "foo-bar-static1field", "foo-bar-static1method", "foo-bar-myNameIsEarl", "foo-bar-ex1method", "foo-bar-ex1field", "foo-bar-mv");
    }

    @Test
    public void testIncludeGlobalGlobal() throws Exception {
        // if it makes it through this test then everything is good
        VarExporter.global().includeInGlobal().export(ExampleClass.class, "");
        VarExporter.global().setParentNamespace(VarExporter.global()).export(ExampleClass.class, "");
        VarExporter.forNamespace("foo").setParentNamespace(VarExporter.forNamespace("foo")).export(ExampleClass.class, "");
    }

    @Test
    public void testPackageClassExport() {
        final PublicClass obj = new PublicClass();
        final VarExporter exporter = VarExporter.forNamespace("external");
        assertEquals(obj.getDependencyStatus(), exporter.getValue("status"));
    }

    @Test
    public void testForNamespaceWithClass() {
        final VarExporter exporter = VarExporter.forNamespace(ExampleClass.class, true);
        assertSame(VarExporter.forNamespace("ExampleClass"), exporter);
        assertNotNull(exporter.getNamespaceClass());
        assertTrue(exporter.isDeclaredFieldsOnly());
        exporter.export(new ExampleSubclass(), "");
        assertExportedNames(exporter, "ex1field", "ex1method", "myNameIsEarl", "static1field", "static1method");
        Assert.assertNull(exporter.getValue("subm1"));
        assertEquals("ExampleClass", exporter.getVariable("ex1field").getNamespace());
    }

    @Test
    public void testForNamespaceWithClass2() {
        final VarExporter exporter = VarExporter.forNamespace(ExampleSubclass.class, false);
        assertSame(VarExporter.forNamespace("ExampleSubclass"), exporter);
        assertNotNull(exporter.getNamespaceClass());
        assertFalse(exporter.isDeclaredFieldsOnly());
        exporter.export(new ExampleSubclass(), "");
        assertExportedNames(exporter, "ex1field", "ex1method", "myNameIsEarl", "static1field", "static1method", "subm1");
        assertEquals("ExampleSubclass", exporter.getVariable("ex1field").getNamespace());
    }

    @Test
    public void testForNamespaceWithClass3() {
        final VarExporter exporter = VarExporter.forNamespace(ExampleSubclass.class, true);
        assertSame(VarExporter.forNamespace("ExampleSubclass"), exporter);
        assertNotNull(exporter.getNamespaceClass());
        assertTrue(exporter.isDeclaredFieldsOnly());
        exporter.export(new ExampleSubclass(), "");
        assertExportedNames(exporter, "subm1");
        assertEquals("ExampleSubclass", exporter.getVariable("subm1").getNamespace());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExportIncompatibleType() {
        final VarExporter exporter = VarExporter.forNamespace(ExampleClass.class, true);
        exporter.export(new ExampleNeedingEscaping(), "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExportIncompatibleType2() {
        final VarExporter exporter = VarExporter.forNamespace(ExampleClass.class, true);
        exporter.export(ExampleNeedingEscaping.class, "");
    }
}
