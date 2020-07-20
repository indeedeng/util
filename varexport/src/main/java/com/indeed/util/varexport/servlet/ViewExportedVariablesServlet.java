// Copyright 2009 Indeed
package com.indeed.util.varexport.servlet;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.TreeMultimap;
import com.indeed.util.varexport.VarExporter;
import com.indeed.util.varexport.Variable;
import com.indeed.util.varexport.VariableHost;
import com.indeed.util.varexport.VariableVisitor;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import org.apache.log4j.Logger;

import javax.annotation.Nullable;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Servlet for displaying variables exported by {@link VarExporter}.
 * Will escape values for compatibility with loading into {@link Properties}.
 *
 * @author jack@indeed.com (Jack Humphrey)
 */
public class ViewExportedVariablesServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(ViewExportedVariablesServlet.class);

    private static final Joiner COMMA_JOINER = Joiner.on(',');

    private Template varTextTemplate;
    private Template varHtmlTemplate;
    private Template browseNamespaceTemplate;

    public enum DisplayType {
        PLAINTEXT("text", "text/plain"),
        JSON("json", "application/json"),
        HTML("html", "text/html");

        private final String paramValue;
        private final String mimeType;

        DisplayType(final String paramValue, final String mimeType) {
            this.paramValue = paramValue;
            this.mimeType = mimeType;
        }

    }

    @VisibleForTesting
    protected void setVarTextTemplate(final Template varTextTemplate) {
        this.varTextTemplate = varTextTemplate;
    }

    @VisibleForTesting
    protected void setVarHtmlTemplate(final Template varHtmlTemplate) {
        this.varHtmlTemplate = varHtmlTemplate;
    }

    @VisibleForTesting
    protected void setBrowseNamespaceTemplate(final Template browseNamespaceTemplate) {
        this.browseNamespaceTemplate = browseNamespaceTemplate;
    }

    @Override
    public void init(final ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);

        final Configuration config = new Configuration();
        config.setObjectWrapper(new DefaultObjectWrapper());
        final String templateLoadPath = servletConfig.getInitParameter("templateLoadPath");
        if (templateLoadPath != null && new File(templateLoadPath).isDirectory()) {
            try {
                config.setDirectoryForTemplateLoading(new File(templateLoadPath));
            } catch (IOException e) {
                throw new ServletException(e);
            }
        } else {
            final String contextLoadPath = servletConfig.getInitParameter("contextLoadPath");
            if (contextLoadPath != null) {
                final ServletContext ctx = servletConfig.getServletContext();
                config.setServletContextForTemplateLoading(ctx, contextLoadPath);
            } else {
                config.setClassForTemplateLoading(getClass(), "/");
            }
        }
        try {
            varTextTemplate = config.getTemplate("vars-text.ftl");
            varHtmlTemplate = config.getTemplate("vars-html.ftl");
            browseNamespaceTemplate = config.getTemplate("browsens.ftl");
        } catch (IOException e) {
            throw new ServletException("Failed to load template", e);
        }
    }

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        response.setCharacterEncoding("UTF-8");

        if ("1".equals(request.getParameter("browse"))) {
            showNamespaces(request.getRequestURI(), response);
        } else {
            final String[] vars = request.getParameterValues("v");
            final boolean doc = "1".equals(request.getParameter("doc"));
            final String fmtParam = request.getParameter("fmt");
            final DisplayType displayType;
            if ("html".equals(fmtParam)) {
                displayType = DisplayType.HTML;
            } else if ("json".equals(fmtParam)) {
                displayType = DisplayType.JSON;
            } else {
                displayType = DisplayType.PLAINTEXT;
            }
            showVariables(request.getRequestURI(), response, request.getParameter("ns"), request.getParameter("tag"), doc, displayType, vars);
        }
    }

    private void showNamespaces(final String uri, final HttpServletResponse response) throws IOException {
        final List<String> namespaces = VarExporter.getNamespaces();
        namespaces.remove(null);
        Collections.sort(namespaces);

        final Map<String, String> parents = Maps.newHashMapWithExpectedSize(namespaces.size());
        for (String namespace : namespaces) {
            VarExporter parent = VarExporter.forNamespace(namespace).getParentNamespace();
            if (parent == null) {
                parents.put(namespace, "none");
            } else {
                parents.put(namespace, parent.getNamespace());
            }
        }

        response.setContentType("text/html");
        final PrintWriter out = response.getWriter();
        final Map<String, Object> root = new HashMap<String, Object>();
        root.put("urlPath", uri);
        root.put("namespaces", namespaces);
        root.put("parents", parents);
        try {
            browseNamespaceTemplate.process(root, out);
        } catch (Exception e) {
            throw new IOException("template failure", e);
        }
        out.flush();
        out.close();
    }

    /**
     * @param uri The request URI.
     * @param response The response we will stream the report to.
     * @param namespace The namespace which we should be inspecting
     * @param includeDoc True if we should include docs. False, otherwise.
     * @param html True to display the report in HTML. False for PLAINTEXT.
     * @param vars The variables that we want to view.
     * @throws IOException If we cannot write to the response stream.
     * @deprecated use version that takes DisplayType enum
     */
    @Deprecated
    protected void showVariables(
            final String uri,
            final HttpServletResponse response,
            final String namespace,
            final boolean includeDoc,
            final boolean html,
            final String... vars
    ) throws IOException {
        final DisplayType displayType = html ? DisplayType.HTML : DisplayType.PLAINTEXT;
        showVariables(uri, response, namespace, includeDoc, displayType, vars);
    }

    /**
     * @param uri The request URI.
     * @param response The response we will stream the report to.
     * @param namespace The namespace which we should be inspecting
     * @param includeDoc True if we should include docs. False, otherwise.
     * @param displayType The type of display to produce.
     * @param vars The variables that we want to view.
     * @throws IOException If we cannot write to the response stream.
     * @deprecated use version that takes tag
     */
    @Deprecated
    protected void showVariables(
            final String uri,
            final HttpServletResponse response,
            final String namespace,
            final boolean includeDoc,
            final DisplayType displayType,
            final String... vars
    ) throws IOException {
        showVariables(uri, response, namespace, null, includeDoc, displayType, vars);
    }

    protected void showVariables(
            final String uri,
            final HttpServletResponse response,
            final String namespace,
            final String tag,
            final boolean includeDoc,
            final DisplayType displayType,
            final String... vars
    ) throws IOException {
        final VariableHost exporter;
        if (!Strings.isNullOrEmpty(tag)) {
            exporter = VarExporter.withTag(tag);
        } else if (!Strings.isNullOrEmpty(namespace)) {
            final Optional<VarExporter> maybeExporter = VarExporter.forNamespaceIfExists(namespace);
            if (!maybeExporter.isPresent()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "The specified namespace not found");
                return;
            }
            exporter = maybeExporter.get();
        } else {
            exporter = VarExporter.global();
        }

        final PrintWriter out = response.getWriter();
        response.setContentType(displayType.mimeType);
        switch(displayType) {
            case HTML:
                showUsingTemplate(exporter, uri, namespace, includeDoc, varHtmlTemplate, true, out, vars);
                break;
            case PLAINTEXT:
                showUsingTemplate(exporter, uri, namespace, includeDoc, varTextTemplate, false, out, vars);
                break;
            // TODO: support json -- exporter JSON is currently broken
        }

        out.flush();
        out.close();
    }

    private void showUsingTemplate(
            final VariableHost exporter,
            final String uri,
            final String namespace,
            final boolean includeDoc,
            final Template template,
            final boolean withIndex,
            final PrintWriter out,
            final String... vars
    ) throws IOException {
        final String name = (namespace == null ? "Global" : namespace);

        final Map<String, Object> root = new HashMap<String, Object>();
        final DateFormat df = SimpleDateFormat.getDateTimeInstance();
        root.put("urlPath", uri);
        root.put("name", name);
        root.put("date", df.format(new Date()));
        root.put("includeDoc", includeDoc);

        final List<Variable> varList;
        if (vars != null && vars.length == 1) {
            final Variable v = exporter.getVariable(vars[0]);
            if (v != null) {
                varList = Lists.newArrayListWithExpectedSize(1);
                addVariable(v, varList);
            } else {
                varList = ImmutableList.of();
            }
        } else {
            varList = Lists.newArrayListWithExpectedSize(vars != null ? vars.length : 256);
            if (vars == null || vars.length == 0) {
                exporter.visitVariables(new VariableVisitor() {
                    public void visit(Variable var) {
                        addVariable(var, varList);
                    }
                });
            } else {
                for (String var : vars) {
                    Variable v = exporter.getVariable(var);
                    if (v != null) {
                        addVariable(v, varList);
                    }
                }
            }
        }
        root.put("vars", varList);
        if (withIndex) {
            root.put("varsIndex", alphanumericNGramIndexesJSON(varList));
        }

        try {
            template.process(root, out);
        } catch (Exception e) {
            throw new IOException("template failure", e);
        }
    }

    private String alphanumericNGramIndexesJSON(final List<Variable> varList) {
        // The JSON we build here is valid as we only use alphanumeric n-grams

        final StringBuilder json = new StringBuilder();

        json.append('{').append('\n');

        json.append("\n\"uniGram\":").append('\n');
        appendTo(json, buildAlphanumericNGramIndex(varList, 1)).append(',').append('\n');

        json.append("\"biGram\":").append('\n');
        appendTo(json, buildAlphanumericNGramIndex(varList, 2)).append(',').append('\n');;

        json.append("\"triGram\":").append('\n');
        appendTo(json, buildAlphanumericNGramIndex(varList, 3));

        json.append('}');

        return json.toString();
    }

    private <K> StringBuilder appendTo(final StringBuilder json, final SetMultimap<K, Integer> map) {
        json.append('{').append('\n');
        boolean isFirst = true;
        for (final K key : map.keySet()) {
            final Set<Integer> values = map.get(key);
            if (isFirst) {
                isFirst = false;
            } else {
                json.append(',').append('\n');
            }
            json.append('"').append(key.toString()).append('"').append(':');
            json.append('[');
            COMMA_JOINER.appendTo(json, values);
            json.append(']');
        }
        json.append('\n').append('}');
        return json;
    }

    @VisibleForTesting
    static SetMultimap<String, Integer> buildAlphanumericNGramIndex(final List<Variable> varList, final int n) {
        final TreeMultimap<String, Integer> nGramIndex = TreeMultimap.create();
        IntStream
                .range(0, varList.size())
                .forEach(i ->
                        alphanumericNGrams(varList.get(i).getName(), n)
                                .forEach(ngram -> nGramIndex.put(ngram, i))
                );
        return nGramIndex;
    }

    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^a-z0-9]+");

    @VisibleForTesting
    static Stream<String> alphanumericNGrams(@Nullable final String in, final int n) {
        if (in == null) {
            return Stream.empty();
        }
        return NON_ALPHANUMERIC
                .splitAsStream(in.toLowerCase(Locale.US))
                .flatMap(alnumSubString ->
                        IntStream
                                .rangeClosed(0, alnumSubString.length() - n)
                                .mapToObj(i -> alnumSubString.substring(i, i + n))
                );
    }

    private void addVariable(final Variable v, final List<Variable> out) {
        try {
            v.toString();
            out.add(v);
        } catch (Throwable t) {
            // skip variables that cannot render due to an exception
            log.warn("Cannot resolve variable " + v.getName(), t);
        }

    }
}
