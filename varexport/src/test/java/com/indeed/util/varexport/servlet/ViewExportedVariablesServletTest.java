package com.indeed.util.varexport.servlet;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.indeed.util.varexport.Export;
import com.indeed.util.varexport.ManagedVariable;
import com.indeed.util.varexport.VarExporter;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Level;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import static org.apache.logging.log4j.core.appender.ConsoleAppender.Target;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
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

    @Before
    public void setUp() throws Exception {
        VarExporter.resetGlobal();
    }

    @BeforeClass
    public static void initClass() {
        ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();
        final BuiltConfiguration config = builder.setStatusLevel(Level.ERROR)
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
        Configurator.initialize(config);
    }
}
