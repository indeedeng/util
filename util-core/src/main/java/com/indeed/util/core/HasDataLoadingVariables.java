// Copyright 2009 Indeed
package com.indeed.util.core;

import com.indeed.util.varexport.Export;

/**
 * Shared interface for exposing variables related to data loading.
 *
 * @author jack@indeed.com (Jack Humphrey)
 */
public interface HasDataLoadingVariables {
    @Export(name="data-version", doc="Version of data being provided")
    public String getDataVersion();

    @Export(name="data-since-last-load", doc="Seconds since last successful data load")
    public Integer getSecondsSinceLastLoad();

    @Export(name="data-since-last-load-failed", doc="Seconds since last failed data load attempt")
    public Integer getSecondsSinceLastFailedLoad();

    @Export(name="data-since-last-check", doc="Seconds since last check for reload")
    public Integer getSecondsSinceLastLoadCheck();
}
