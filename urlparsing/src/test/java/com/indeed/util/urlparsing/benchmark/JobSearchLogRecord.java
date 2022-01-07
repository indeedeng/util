package com.indeed.util.urlparsing.benchmark;

/** @author: preetha */
public class JobSearchLogRecord {

    private StringBuilder uid = new StringBuilder(16);
    private StringBuilder query = new StringBuilder(12);
    private StringBuilder location = new StringBuilder(12);
    private long timestamp;
    private int numResults;

    public StringBuilder getUid() {
        return uid;
    }

    public StringBuilder getQuery() {
        return query;
    }

    public StringBuilder getLocation() {
        return location;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getNumResults() {
        return numResults;
    }

    public void setNumResults(int numResults) {
        this.numResults = numResults;
    }

    @Override
    public String toString() {
        return "JobSearchLogRecord{"
                + "uid="
                + uid
                + ", query="
                + query
                + ", location="
                + location
                + ", timestamp="
                + timestamp
                + ", numResults="
                + numResults
                + '}';
    }
}
