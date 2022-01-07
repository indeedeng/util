package com.indeed.util.core; // Copyright 2009 Indeed

import com.google.common.annotations.VisibleForTesting;

/**
 * Helper or base class that stores timestamps for data loading processes.
 *
 * @author jack@indeed.com (Jack Humphrey)
 */
public class DataLoadTimer {
    private Long lastSuccessLoad = null;
    private Long lastFailLoad = null;
    private Long lastLoadCheck = null;

    // right at startup, this boolean may report incorrectly, so always use
    // isLoadedDataSuccessfullyRecently() instead
    @VisibleForTesting protected boolean lastLoadWasSuccessful = false;

    /** Updates the timestamp of when the last load was attempted */
    public void updateLastLoadCheck() {
        lastLoadCheck = System.currentTimeMillis();
    }

    /** Updates the last successful load time without setting lastLoadWasSuccessful to true */
    protected void updateLastSuccessLoadTime() {
        lastSuccessLoad = System.currentTimeMillis();
        lastLoadWasSuccessful = true;
    }

    public void loadComplete() {
        lastSuccessLoad = System.currentTimeMillis();
        lastLoadWasSuccessful = true;
    }

    public void loadFailed() {
        lastFailLoad = System.currentTimeMillis();
        lastLoadWasSuccessful = false;
    }

    public Integer getSecondsSinceLastLoad() {
        if (lastSuccessLoad == null) return null;
        return (int) (System.currentTimeMillis() - lastSuccessLoad) / 1000;
    }

    public Integer getSecondsSinceLastFailedLoad() {
        if (lastFailLoad == null) return null;
        return (int) (System.currentTimeMillis() - lastFailLoad) / 1000;
    }

    public Integer getSecondsSinceLastLoadCheck() {
        if (lastLoadCheck == null) return null;
        return (int) (System.currentTimeMillis() - lastLoadCheck) / 1000;
    }

    // useful for artifact-based healthchecks
    public boolean wasLastLoadErroring() {
        return !isLoadedDataSuccessfullyRecently();
    }

    // useful for artifact-based healthchecks
    public boolean isLoadedDataSuccessfullyRecently() {
        final Integer timeSinceLastError = getSecondsSinceLastFailedLoad();
        final Integer timeSinceLastSuccess = getSecondsSinceLastLoad();
        if (timeSinceLastSuccess == null) {
            return false; // never loaded data, so must be FAIL
        }
        if (timeSinceLastError == null) {
            return true; // no errors ever, so must be OKAY
        }
        return lastLoadWasSuccessful;
    }
}
