package com.indeed.util.varexport;

/**
 * @author jack@indeed.com (Jack Humphrey)
 */
public class ExplicitAnnotationExportData implements ExportData {
    private final Export export;

    public ExplicitAnnotationExportData(final Export export) {
        this.export = export;
    }

    @Override
    public String name() {
        return export.name();
    }

    @Override
    public String doc() {
        return export.doc();
    }

    @Override
    public boolean expand() {
        return export.expand();
    }

    @Override
    public long cacheTimeoutMs() {
        return export.cacheTimeoutMs();
    }
}
