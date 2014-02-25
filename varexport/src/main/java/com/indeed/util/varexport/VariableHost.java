package com.indeed.util.varexport;

/**
 * @author jack@indeed.com (Jack Humphrey)
 */
public interface VariableHost {

    void visitVariables(VariableVisitor visitor);

    <T> Variable<T> getVariable(String variableName);

}
