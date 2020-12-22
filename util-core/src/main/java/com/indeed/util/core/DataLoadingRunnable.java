// Copyright 2009 Indeed
package com.indeed.util.core;

import com.indeed.util.varexport.VarExporter;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Runnable implementation of the {@link HasDataLoadingVariables} class that
 * exports data loading variables.
 *
 * @author jack@indeed.com (Jack Humphrey)
 */
public abstract class DataLoadingRunnable extends DataLoadTimer implements HasDataLoadingVariables, Runnable {
    private static final Logger log = LogManager.getLogger(DataLoadingRunnable.class);

    // use this for your implementation of load()
    public enum ReloadState {
        RELOADED,
        NO_CHANGE,
        FAILED
    }

    private String dataVersion;
    private ReloadState reloadState;

    public DataLoadingRunnable(String namespace) {
        VarExporter.forNamespace(namespace).includeInGlobal().export(this, "");
    }

    public void setDataVersion(String version) {
        this.dataVersion = version;
    }

    public String getDataVersion() {
        return dataVersion;
    }

    public ReloadState getReloadState() {
        return reloadState;
    }

    /**
     * Instead of implementing {@link #run()}
     *
     * @return True if the load was successful. False otherwise.
     */
    public abstract boolean load();

    // optional feature, to be used in conjunction with ReloadState
    //
    // EXAMPLE USAGE of load() + ReloadState + finishLoadWithReloadState()
    //
    //    public boolean load() {
    //        final String dataVersion = Files.getCanonicalDirectoryName(binDirectory);
    //        final String path = Files.getCanonicalPath(binDirectory);
    //        if (path == null) {
    //            loadFailed();
    //            return false;
    //        }
    //        final ReloadState state = internalRefresh(path);
    //        return finishLoadWithReloadState(state, dataVersion);
    //    }
    //    private ReloadState internalRefresh(String path) {
    //        final long timestamp = readTimestamp(path);
    //        if (timestamp == -1) return DataLoadingRunnable.ReloadState.FAILED;
    //        final LocalArtifactCore current = serviceCore;
    //        if (current != null && current.timestamp == timestamp) return DataLoadingRunnable.ReloadState.NO_CHANGE;
    //        int[] stuff = (int[])Files.readObjectFromFile(Files.buildPath(binDirectory, "stuff.bin"));
    //        if (stuff == null) return DataLoadingRunnable.ReloadState.FAILED;
    //        final long newTimestamp = readTimestamp(path);
    //        if (timestamp != newTimestamp) return DataLoadingRunnable.ReloadState.FAILED;
    //        // atomically swap LocalArtifactCore object pointer in ram
    //        return DataLoadingRunnable.ReloadState.RELOADED;
    //    }
    //    private static long readTimestamp(String path) {
    //        final Long timestamp = (Long)Files.readObjectFromFile(Files.buildPath(path, "timestamp.bin"));
    //        return timestamp == null ? -1 : timestamp;
    //    }
    //
    protected boolean finishLoadWithReloadState(ReloadState state, String newDataVersion) {
        if (state == ReloadState.RELOADED) {
            loadComplete();
            setDataVersion(newDataVersion);
            return true;
        } else if (state == ReloadState.NO_CHANGE) {
            loadNotChanged();
            return false;
        } else { // FAILED
            loadFailed();
            return false;
        }
    }

    @Override
    public void loadComplete() {
        super.loadComplete();
        this.reloadState = ReloadState.RELOADED;
    }

    @Override
    public void loadFailed() {
        super.loadFailed();
        this.reloadState = ReloadState.FAILED;
    }

    public void loadNotChanged() {
        this.reloadState = ReloadState.NO_CHANGE;

        if (wasLastLoadErroring()) {
            // only touch this so that
            log.info("Last load was in error, so updating last success time anyway");
            updateLastSuccessLoadTime();
        }
    }

    public final void run() {
        try {
            updateLastLoadCheck();
            if (load()) {
                loadComplete();
            }
        } catch (RuntimeException e) {
            loadFailed();
            log.error("Ignoring RuntimeException", e);
        } catch (Error e) {
            loadFailed();
            log.error("Ignoring Error", e);
        }
    }
}
