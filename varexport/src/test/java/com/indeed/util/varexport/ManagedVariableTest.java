// Copyright 2009 Indeed
package com.indeed.util.varexport;

import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

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
        exporter = VarExporter.global();
        exporter.reset();
    }

    @After
    public void tearDown() throws Exception {
        exporter.reset();
    }

    @Test
    public void testManagedVariable() {
        ManagedVariable<Integer> var1 = ManagedVariable.<Integer>builder().setName("var1").setValue(524).build();
        Assert.assertEquals(524, (int) var1.getValue());
        var1.clock = testClock;
        var1.set(524);
        exporter.export(var1);
        Assert.assertEquals(524, exporter.getValue("var1"));
        Assert.assertEquals(0L, (long) var1.getLastUpdated());
        curTime = 100L;
        var1.set(999);
        Assert.assertEquals(999, exporter.getValue("var1"));
        Assert.assertEquals(100L, (long) var1.getLastUpdated());
        Assert.assertEquals("", exporter.getVariable("var1").getDoc());
        Assert.assertEquals(false, exporter.getVariable("var1").isExpandable());

        StringWriter out = new StringWriter();
        exporter.dump(new PrintWriter(out, true), true);
        Assert.assertThat(Lists.newArrayList(out.toString().split("\n")),
                          Matchers.containsInAnyOrder("", "#  (last update: 100)", "var1=999")
        );
    }

    @Test
    public void testManagedVariable_hasDoc() {
        ManagedVariable<Integer> var1 = ManagedVariable.<Integer>builder()
                .setDoc("rtfm").setName("var1").build();
        var1.clock = testClock;
        var1.set(524);
        exporter.export(var1);
        Assert.assertEquals(524, exporter.getValue("var1"));
        Assert.assertEquals(0L, (long) var1.getLastUpdated());
        curTime = 100L;
        var1.set(999);
        Assert.assertEquals(999, exporter.getValue("var1"));
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
        Assert.assertEquals(524, exporter.getValue("var1"));
        Assert.assertEquals(0L, (long) var1.getLastUpdated());
        curTime = 100L;
        var1.set(999);
        Assert.assertEquals(999, exporter.getValue("var1"));
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
        Assert.assertEquals(100L, exporter.getValue("var1#1"));
        Assert.assertEquals(200L, exporter.getValue("var1#2"));
        Assert.assertEquals(0L, (long) var1.getLastUpdated());
        curTime = 100L;
        map.put(1L, 1000L);
        Assert.assertEquals(1000L, exporter.getValue("var1#1"));
        Assert.assertEquals(200L, exporter.getValue("var1#2"));
        Assert.assertEquals(0L, (long) var1.getLastUpdated());
        var1.set(map);
        Assert.assertEquals(100L, (long) var1.getLastUpdated());
        Assert.assertEquals("", exporter.getVariable("var1").getDoc());
        Assert.assertEquals(true, exporter.getVariable("var1").isExpandable());
    }
}
