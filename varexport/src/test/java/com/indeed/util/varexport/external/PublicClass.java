package com.indeed.util.varexport.external;

public class PublicClass {

    private final PackageClass dependency = new PackageClass();

    public String getDependencyStatus() {
        return dependency.getStatus();
    }
}
