// Copyright 2009 Indeed
package com.indeed.util.varexport;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author jack@indeed.com (Jack Humphrey)
 */
public class ManagedVariableTest {
    private VarExporter exporter;
    private long curTime = 0;
    private Supplier<Long> testClock = new Supplier<Long>() {
        public Long get() {
            return curTime;
        }
    };

    @Before
    public void setUp() throws Exception {
        VarExporter.startTime = null;
        VarExporter.resetGlobal();
        exporter = VarExporter.global();
    }

    @BeforeClass
    public static void initClass() {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.ERROR);
        Logger.getLogger("com.indeed").setLevel(Level.ERROR);
    }

    @After
    public void tearDown() throws Exception {
        VarExporter.resetGlobal();
    }

    @Test
    public void testManagedVariable() {
        ManagedVariable<Integer> var1 = ManagedVariable.<Integer>builder().setName("var1").setValue(524).build();
        Assert.assertEquals(524, (int) var1.getValue());
        var1.clock = testClock;
        var1.set(524);
        exporter.export(var1);
        Assert.assertEquals(524, (int) exporter.getValue("var1"));
        Assert.assertEquals(0L, (long) var1.getLastUpdated());
        curTime = 100L;
        var1.set(999);
        Assert.assertEquals(999, (int) exporter.getValue("var1"));
        Assert.assertEquals(100L, (long) var1.getLastUpdated());
        Assert.assertEquals("", exporter.getVariable("var1").getDoc());
        Assert.assertEquals(false, exporter.getVariable("var1").isExpandable());

        StringWriter out = new StringWriter();
        exporter.dump(new PrintWriter(out, true), true);
        Assert.assertThat(Lists.newArrayList(out.toString().split("\n")),
                          Matchers.containsInAnyOrder("", "#  (last update: 100)", "var1=999")
        );
    }

    final Function<VariableHost, Integer> countVars = new Function<VariableHost, Integer>() {
        @Override
        public Integer apply(VariableHost variableHost) {
            final AtomicInteger count = new AtomicInteger(0);
            variableHost.visitVariables(new VariableVisitor() {
                @Override
                public void visit(Variable var) {
                    count.incrementAndGet();
                }
            });
            return count.intValue();
        }
    };

    @Test
    public void testManagedVariable_tags() {
        ManagedVariable<Integer> var1 = ManagedVariable.<Integer>builder()
                .setName("var1").setValue(524).setTags(ImmutableSet.of("MVT1", "MVT2")).build();
        ManagedVariable<Integer> var2 = ManagedVariable.<Integer>builder()
                .setName("var2").setValue(207).setTags(ImmutableSet.of("MVT2", "MVT3")).build();
        Assert.assertEquals(524, (int) var1.getValue());
        exporter.export(var1);
        exporter.export(var2);

        VariableHost tagged1 = VarExporter.withTag("MVT1");
        VariableHost tagged2 = VarExporter.withTag("MVT2");
        VariableHost tagged3 = VarExporter.withTag("MVT3");

        Assert.assertEquals((Integer) 1, countVars.apply(tagged1));
        Assert.assertEquals((Integer) 2, countVars.apply(tagged2));
        Assert.assertEquals((Integer) 1, countVars.apply(tagged3));

        Assert.assertEquals(var1, tagged1.getVariable("var1"));
        Assert.assertNull(tagged1.getVariable("var2"));

        Assert.assertEquals(var1, tagged2.getVariable("var1"));
        Assert.assertEquals(var2, tagged2.getVariable("var2"));

        Assert.assertNull(tagged3.getVariable("var1"));
        Assert.assertEquals(var2, tagged3.getVariable("var2"));
    }

    @Test
    public void testLazilyManagedVariable_tags() {
        LazilyManagedVariable<Integer> var1 = LazilyManagedVariable.<Integer>builder(Integer.class)
                .setName("var1").setValue(Suppliers.ofInstance(524)).setTags(ImmutableSet.of("LMVT1", "LMVT2")).build();
        LazilyManagedVariable<Integer> var2 = LazilyManagedVariable.<Integer>builder(Integer.class)
                .setName("var2").setValue(Suppliers.ofInstance(207)).setTags(ImmutableSet.of("LMVT2", "LMVT3")).build();
        Assert.assertEquals(524, (int) var1.getValue());
        exporter.export(var1);
        exporter.export(var2);

        VariableHost tagged1 = VarExporter.withTag("LMVT1");
        VariableHost tagged2 = VarExporter.withTag("LMVT2");
        VariableHost tagged3 = VarExporter.withTag("LMVT3");

        Assert.assertEquals((Integer) 1, countVars.apply(tagged1));
        Assert.assertEquals((Integer) 2, countVars.apply(tagged2));
        Assert.assertEquals((Integer) 1, countVars.apply(tagged3));

        Assert.assertEquals(var1, tagged1.getVariable("var1"));
        Assert.assertNull(tagged1.getVariable("var2"));

        Assert.assertEquals(var1, tagged2.getVariable("var1"));
        Assert.assertEquals(var2, tagged2.getVariable("var2"));

        Assert.assertNull(tagged3.getVariable("var1"));
        Assert.assertEquals(var2, tagged3.getVariable("var2"));
    }

    @Test
    public void testManagedVariable_hasDoc() {
        ManagedVariable<Integer> var1 = ManagedVariable.<Integer>builder()
                .setDoc("rtfm").setName("var1").build();
        var1.clock = testClock;
        var1.set(524);
        exporter.export(var1);
        Assert.assertEquals(524, (int) exporter.getValue("var1"));
        Assert.assertEquals(0L, (long) var1.getLastUpdated());
        curTime = 100L;
        var1.set(999);
        Assert.assertEquals(999, (int) exporter.getValue("var1"));
        Assert.assertEquals(100L, (long) var1.getLastUpdated());
        Assert.assertEquals("rtfm", exporter.getVariable("var1").getDoc());
        Assert.assertEquals(false, exporter.getVariable("var1").isExpandable());

        StringWriter out = new StringWriter();
        exporter.dump(new PrintWriter(out, true), true);
        Assert.assertThat(Lists.newArrayList(out.toString().split("\n")),
                          Matchers.contains("", "# rtfm (last update: 100)", "var1=999")
        );
    }

    @Test
    public void testManagedVariable_notExpandable() {
        ManagedVariable<Integer> var1 = ManagedVariable.<Integer>builder()
                .setExpand(true).setName("var1").build();
        var1.clock = testClock;
        var1.set(524);
        exporter.export(var1);
        Assert.assertEquals(524, (int) exporter.getValue("var1"));
        Assert.assertEquals(0L, (long) var1.getLastUpdated());
        curTime = 100L;
        var1.set(999);
        Assert.assertEquals(999, (int) exporter.getValue("var1"));
        Assert.assertEquals(100L, (long) var1.getLastUpdated());
        Assert.assertEquals("", exporter.getVariable("var1").getDoc());
        Assert.assertEquals(false, exporter.getVariable("var1").isExpandable());
    }

    @Test
    public void testManagedVariable_expandable() {
        Map<Long,Long> map = Maps.newHashMap();
        map.put(1L, 100L);
        map.put(2L, 200L);
        ManagedVariable<Map<Long,Long>> var1 = ManagedVariable.<Map<Long,Long>>builder()
                .setExpand(true).setName("var1").build();
        var1.clock = testClock;
        var1.set(map);
        exporter.export(var1);
        Assert.assertEquals(100L, (long) exporter.getValue("var1#1"));
        Assert.assertEquals(200L, (long) exporter.getValue("var1#2"));
        Assert.assertEquals(0L, (long) var1.getLastUpdated());
        curTime = 100L;
        map.put(1L, 1000L);
        Assert.assertEquals(1000L, (long) exporter.getValue("var1#1"));
        Assert.assertEquals(200L, (long) exporter.getValue("var1#2"));
        Assert.assertEquals(0L, (long) var1.getLastUpdated());
        var1.set(map);
        Assert.assertEquals(100L, (long) var1.getLastUpdated());
        Assert.assertEquals("", exporter.getVariable("var1").getDoc());
        Assert.assertEquals(true, exporter.getVariable("var1").isExpandable());
    }
}
