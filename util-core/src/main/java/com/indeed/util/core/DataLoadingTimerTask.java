// Copyright 2009 Indeed
package com.indeed.util.core;

import com.indeed.util.varexport.VarExporter;
import org.apache.log4j.Logger;

import java.util.TimerTask;

/**
 * TimerTask implementation that exports data loading variables ({@link HasDataLoadingVariables})
 *
 * @author jack@indeed.com (Jack Humphrey)
 */
public abstract class DataLoadingTimerTask extends TimerTask implements HasDataLoadingVariables {
    private static final Logger log = Logger.getLogger(DataLoadingTimerTask.class);

    protected DataLoadTimer dataLoadTimer = new DataLoadTimer();
    private String dataVersion;

    public DataLoadingTimerTask(String namespace) {
        VarExporter.forNamespace(namespace).includeInGlobal().export(this, "");
    }

    public void setDataVersion(String version) {
        this.dataVersion = version;
    }

    public String getDataVersion() {
        return dataVersion;
    }

    protected void loadFailed() {
        dataLoadTimer.loadFailed();
    }

    protected void loadComplete() {
        dataLoadTimer.loadComplete();
    }

    /**
     * Instead of implementing {@link #run()}
     *
     * @return True if the load was successful. False otherwise.
     */
    public abstract boolean load();

    public Integer getSecondsSinceLastLoad() {
        return dataLoadTimer.getSecondsSinceLastLoad();
    }

    public Integer getSecondsSinceLastFailedLoad() {
        return dataLoadTimer.getSecondsSinceLastFailedLoad();
    }

    public Integer getSecondsSinceLastLoadCheck(){
        return dataLoadTimer.getSecondsSinceLastLoadCheck();
    }

    @Override
    public final void run() {
        try {
            dataLoadTimer.updateLastLoadCheck();
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
