// Copyright 2009 Indeed
package com.indeed.util.varexport.servlet;

import com.indeed.util.varexport.VarExporter;
import com.indeed.util.varexport.Variable;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.log4j.Logger;

/**
 * Servlet for displaying variables exported by {@link com.indeed.util.varexport.VarExporter}.
 * Will escape values for compatibility with loading into {@link java.util.Properties}.
 *
 * @author jack@indeed.com (Jack Humphrey)
 */
public class ViewExportedVariablesServlet extends HttpServlet {

    private static Logger log = Logger.getLogger(ViewExportedVariablesServlet.class);

    private Template varTextTemplate;
    private Template varHtmlTemplate;
    private Template browseNamespaceTemplate;

    public enum DisplayType {
        PLAINTEXT("text", "text/plain"),
        JSON("json", "application/json"),
        HTML("html", "text/html");

        private final String paramValue;
        private final String mimeType;

        private DisplayType(final String paramValue, final String mimeType) {
            this.paramValue = paramValue;
            this.mimeType = mimeType;
        }

    }

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);

        Configuration config = new Configuration();
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
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
            showVariables(request.getRequestURI(), response, request.getParameter("ns"), doc, displayType, vars);
        }
    }

    private void showNamespaces(String uri, HttpServletResponse response) throws IOException {
        List<String> namespaces = VarExporter.getNamespaces();
        namespaces.remove(null);
        Collections.sort(namespaces);

        Map<String, String> parents = Maps.newHashMapWithExpectedSize(namespaces.size());
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
        Map<String, Object> root = new HashMap<String, Object>();
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

    /** @deprecated use version that takes DisplayType enum */
    @Deprecated
    protected void showVariables(String uri, HttpServletResponse response, String namespace, boolean includeDoc, boolean html,
                                 String... vars)
            throws IOException {
        final DisplayType displayType = html ? DisplayType.HTML : DisplayType.PLAINTEXT;
        showVariables(uri, response, namespace, includeDoc, displayType, vars);
    }

    protected void showVariables(String uri, HttpServletResponse response, String namespace, boolean includeDoc, DisplayType displayType,
                                 String... vars)
            throws IOException {

        // null ns will result in loading the global namespace:
        if (Strings.isNullOrEmpty(namespace)) {
            namespace = null;
        }
        VarExporter exporter = VarExporter.forNamespace(namespace);
        final PrintWriter out = response.getWriter();
        response.setContentType(displayType.mimeType);

        switch(displayType) {
            case HTML:
                showUsingTemplate(exporter, uri, namespace, includeDoc, varHtmlTemplate, out, vars);
                break;
            case PLAINTEXT:
                showUsingTemplate(exporter, uri, namespace, includeDoc, varTextTemplate, out, vars);
                break;
            // TODO: support json -- exporter JSON is currently broken
        }

        out.flush();
        out.close();
    }

    private void showUsingTemplate(VarExporter exporter, String uri, String namespace, boolean includeDoc, Template template,
                                   PrintWriter out, String... vars) throws IOException {
        String name = (namespace == null ? "Global" : namespace);

        Map<String, Object> root = new HashMap<String, Object>();
        DateFormat df = SimpleDateFormat.getDateTimeInstance();
        root.put("urlPath", uri);
        root.put("name", name);
        root.put("date", df.format(new Date()));
        root.put("includeDoc", includeDoc);

        final List<Variable> varList;
        if (vars != null && vars.length == 1) {
            Variable v = exporter.getVariable(vars[0]);
            if (v != null) {
                varList = Lists.newArrayListWithExpectedSize(1);
                addVariable(v, varList);
            } else {
                varList = ImmutableList.of();
            }
        } else {
            varList = Lists.newArrayListWithExpectedSize(vars != null ? vars.length : 256);
            if (vars == null || vars.length == 0) {
                exporter.visitVariables(new VarExporter.Visitor() {
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

        try {
            template.process(root, out);
        } catch (Exception e) {
            throw new IOException("template failure", e);
        }
    }

    private void addVariable(Variable v, List<Variable> out) {
        try {
            v.toString();
            out.add(v);
        } catch (Throwable t) {
            // skip variables that cannot render due to an exception
            log.warn("Cannot resolve variable " + v.getName(), t);
        }

    }
}
