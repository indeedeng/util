package com.indeed.util.varexport;

import com.google.common.base.Strings;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author jack@indeed.com (Jack Humphrey)
 */
public class LegacyAnnotationExportData implements ExportData {
    private final String name;
    private final String doc;
    private final Boolean expand;
    private long cacheTimeoutMs;

    public static ExportData fromLegacy(AnnotatedElement element, Annotation annotation) {
        ExportData data = null;
        if (annotation.annotationType().getSimpleName().endsWith("Export")) {
            try {
                data = new LegacyAnnotationExportData(element, annotation);
            } catch (InvocationTargetException e) {
                // ignore and return null
            } catch (NoSuchMethodException e) {
                // ignore and return null
            } catch (IllegalAccessException e) {
                // ignore and return null
            }
        }
        return data;
    }

    @SuppressWarnings("unchecked")
    private LegacyAnnotationExportData(AnnotatedElement element, Annotation annotation)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        final Class annotationType = annotation.annotationType();
        final Method nameMethod = annotationType.getMethod("name");
        final Method docMethod = annotationType.getMethod("doc");
        final Method expandMethod = annotationType.getMethod("expand");
        final Method cacheMethod = annotationType.getMethod("cacheTimeoutMs");

        name = Strings.nullToEmpty((String) nameMethod.invoke(annotation));
        doc = Strings.nullToEmpty((String)docMethod.invoke(annotation));
        expand = (Boolean) expandMethod.invoke(annotation);
        cacheTimeoutMs = (Long) cacheMethod.invoke(annotation);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String doc() {
        return doc;
    }

    @Override
    public boolean expand() {
        return expand;
    }

    @Override
    public long cacheTimeoutMs() {
        return cacheTimeoutMs;
    }
}
