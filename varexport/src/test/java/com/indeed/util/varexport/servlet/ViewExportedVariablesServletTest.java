package com.indeed.util.varexport.servlet;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.TreeMultimap;
import com.indeed.util.varexport.Export;
import com.indeed.util.varexport.ManagedVariable;
import com.indeed.util.varexport.VarExporter;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

/**
 * @author jack@indeed.com (Jack Humphrey)
 */
public class ViewExportedVariablesServletTest {

    private ViewExportedVariablesServlet setupServlet() throws IOException {
        final Configuration config = new Configuration();
        config.setObjectWrapper(new DefaultObjectWrapper());
        config.setClassForTemplateLoading(this.getClass(), "/");

        final ViewExportedVariablesServlet servlet = new ViewExportedVariablesServlet();
        servlet.setVarTextTemplate(config.getTemplate("vars-text.ftl"));
        servlet.setVarHtmlTemplate(config.getTemplate("vars-html.ftl"));
        servlet.setBrowseNamespaceTemplate(config.getTemplate("browsens.ftl"));

        return servlet;
    }

    private String getOutput(final ViewExportedVariablesServlet servlet, String namespace, String tag) throws IOException {
        final StringWriter output = new StringWriter();
        HttpServletResponse response = createMock(HttpServletResponse.class);
        response.setContentType("text/plain");
        expectLastCall().once();
        expect(response.getWriter()).andReturn(new PrintWriter(output)).once();

        replay(response);
        servlet.showVariables("", response, namespace, tag, false, ViewExportedVariablesServlet.DisplayType.PLAINTEXT);

        return output.toString();
    }

    private void assertLines(final String output, String... lines) {
        String[] rawLines = output.split("\n");
        List<String> actualLines = Lists.newArrayListWithCapacity(rawLines.length);
        for (String rawLine : rawLines) {
            rawLine = rawLine.trim();
            if (!Strings.isNullOrEmpty(rawLine) &&
                !rawLine.startsWith("#") &&
                !rawLine.startsWith("exporter-start-time=")) {
                actualLines.add(rawLine);
            }
        }
        assertThat(actualLines, Matchers.containsInAnyOrder(lines));
    }

    public static class TagExamples {
        @Export(name="ex1field", doc="Example variable 1", tags = { "VEVST1" })
        public int ex1 = 1;

        @Export(name="ex2field", doc="Example variable 2", tags = { "VEVST2" })
        public int ex2 = 2;

        @Export(name="ex3field", doc="Example variable 3", tags = { "VEVST3" })
        public int ex3 = 3;

        @Export(name="ex4field", doc="Example variable 4", tags = { "VEVST2" })
        public String ex4 = "four";

        @Export(name="ex1method", tags = { "VEVST3" })
        public int getEx1() { return ex1; }

        @Export(name="ex2method", tags = { "VEVST1" })
        public int getEx2() { return ex2; }
    }

    @Test
    public void testManagedVariables() throws IOException {
        final ManagedVariable<String> var1 = ManagedVariable.<String>builder().setName("test1").setValue("1").build();
        final ManagedVariable<String> var2 = ManagedVariable.<String>builder().setName("test2").setValue("2").build();
        VarExporter.global().export(var1);
        VarExporter.global().export(var2);
        final ViewExportedVariablesServlet servlet = setupServlet();
        assertLines(getOutput(servlet, "", ""), "test1=1", "test2=2");
    }

    @Test
    public void testTags() throws IOException {
        VarExporter.global().export(new TagExamples(), "");
        final ViewExportedVariablesServlet servlet = setupServlet();
        assertLines(getOutput(servlet, "", "VEVST1"), "ex1field=1", "ex2method=2");
        assertLines(getOutput(servlet, "", "VEVST2"), "ex2field=2", "ex4field=four");
        assertLines(getOutput(servlet, "", "VEVST3"), "ex3field=3", "ex1method=1");
        assertLines(getOutput(servlet, "", "nothing"), "null");
    }

    @Test
    public void notFoundIsServedOnMissingNamespace() throws IOException {
        final String nonExistentNamespace = "a-nonexistent-namespace";

        assertFalse(VarExporter.getNamespaces().contains(nonExistentNamespace));

        final HttpServletResponse response = createMock(HttpServletResponse.class);
        response.sendError(HttpServletResponse.SC_NOT_FOUND, "The specified namespace not found");
        replay(response);

        setupServlet().showVariables("/private/v", response, nonExistentNamespace, "", true, ViewExportedVariablesServlet.DisplayType.HTML);
        verify(response);

        assertFalse(VarExporter.getNamespaces().contains(nonExistentNamespace));
    }

    @Test
    public void escapesHtmlInVariableNamesAndValues() throws IOException {
        final StringWriter bodyWriter = new StringWriter();

        final HttpServletResponse response = createMock(HttpServletResponse.class);
        response.setContentType("text/html");
        expectLastCall().once();
        expect(response.getWriter()).andReturn(new PrintWriter(bodyWriter)).once();
        replay(response);

        final String unescaped = "<img src='https://goo.gl/1nzcxx'/>";
        VarExporter.global().export(
                ManagedVariable
                        .builder()
                        .setName(unescaped)
                        .setValue(unescaped)
                        .build()
        );

        setupServlet()
                .showVariables(
                        "/private/v",
                        response,
                        "",
                        "",
                        false,
                        ViewExportedVariablesServlet.DisplayType.HTML
                        );

        verify(response);

        assertFalse(bodyWriter.toString().contains(unescaped));
    }

    @Test
    public void buildAlphaNumericNGramIndex() {
        final TreeMultimap<String, Integer> expectedTriGramIndex = TreeMultimap.create();
        expectedTriGramIndex.put("abc", 0);
        expectedTriGramIndex.put("bcd", 0);
        expectedTriGramIndex.put("bcd", 1);
        expectedTriGramIndex.put("zzz", 2);

        final SetMultimap<String, Integer> actualTriGramIndex = ViewExportedVariablesServlet.buildAlphanumericNGramIndex(
                Arrays.asList(
                        ManagedVariable.builder().setName("abcd").build(),
                        ManagedVariable.builder().setName("bcd").build(),
                        ManagedVariable.builder().setName("zzz").build()
                ),
                3
        );

        assertEquals(expectedTriGramIndex, actualTriGramIndex);
    }

    @Test
    public void alphanumericNGrams() {
        assertEquals(0, ViewExportedVariablesServlet.alphanumericNGrams(null, 1).count());
        assertEquals(0, ViewExportedVariablesServlet.alphanumericNGrams("zzz", 42).count());

        assertEquals(
                Arrays.asList("m", "1", "3", "b", "i", "t", "r", "i"),
                ViewExportedVariablesServlet
                        .alphanumericNGrams("m1-3-/bi<</tri", 1)
                        .collect(Collectors.toList())
        );

        assertEquals(
                Arrays.asList("m1", "bi", "tr", "ri"),
                ViewExportedVariablesServlet
                        .alphanumericNGrams("m1-3-/bi<</tri", 2)
                        .collect(Collectors.toList())
        );

        assertEquals(
                Arrays.asList(
                        "m1x",
                        "1x3",
                        "x3d",
                        "3dc",
                        "dca",
                        "cas",
                        "ase",
                        "tri",
                        "tri"
                ),
                ViewExportedVariablesServlet
                        .alphanumericNGrams("m1x3dCaSe-1-/bi<</tri?tri", 3)
                        .collect(Collectors.toList())
        );
    }

    @Before
    public void setUp() throws Exception {
        VarExporter.resetGlobal();
    }

    @BeforeClass
    public static void initClass() {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.ERROR);
        Logger.getLogger("com.indeed").setLevel(Level.ERROR);
    }
}
