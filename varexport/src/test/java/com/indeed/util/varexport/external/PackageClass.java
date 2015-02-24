package com.indeed.util.varexport.external;

import com.indeed.util.varexport.Export;
import com.indeed.util.varexport.VarExporter;

class PackageClass {

    PackageClass() {
        VarExporter.forNamespace("external").export(this, "");
    }

    @Export(name = "status")
    public String getStatus() {
        return "ok!";
    }
}
