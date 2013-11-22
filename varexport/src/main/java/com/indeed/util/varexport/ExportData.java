package com.indeed.util.varexport;

/**
 * @author jack@indeed.com (Jack Humphrey)
 */
public interface ExportData {
    public String name();
    public String doc();
    public boolean expand();
    public long cacheTimeoutMs();
}
