# util-varexport

## About

`VarExporter` is a utility that enables you to expose runtime variables from a running Java application. By dropping the [@Export](https://github.com/indeedeng/util/blob/master/varexport/src/main/java/com/indeed/util/varexport/Export.java) annotation into your classes and dropping in one line of code at initialization time, any program in the VM (including a provided servlet) can access all exported variables using the Visitor pattern.

## Usage

This example allows the result of `getInvocationCount` to be exported with the variable name `invocation-count`.

```java
@Export(name="invocation-count", doc="# invocations of the service interface")
public long getInvocationCount() {
      // ...
        return count;
}
```

Class-level (static) and object-level variables can be exported.

This example exports all annotated `public static` variables/methods for the class "MyClass" into the global namespace with the prefix "myclass-".

```java
VarExporter.global().export(MyClass.class, "myclass-");
```

This example exports all annotated `public static` variables/methods for the class "MyClass" into the "MyClass" namespace and the global namespace (where they will be prefixed with "MyClass-").

```java
VarExporter.forNamespace(MyClass.class.getSimpleName()).includeInGlobal().export(MyClass.class, "");
```

This example exports all `public` instance and class variables/methods for an instance of `MyClass` into the namespace specified by `instanceName` and the global namespace.

```java
public MyClass(final String instanceName) {
    // ...
    VarExporter.forNamespace(instanceName).includeInGlobal().export(this, "");
}
```

## API Documentation

### [VarExporter](https://github.com/indeedeng/util/blob/master/varexport/src/main/java/com/indeed/util/varexport/VarExporter.java)

#### Static Methods

`public static VarExporter forNamespace(String namespace)`
Load an exporter for a given namespace. For the global namespace, use global().

Returns:exporter for the namespace; will be created if never before accessed.

`public static VarExporter global()`
Returns:exporter for the global namespace, use forNamespace(String) for a specific exporter

`public static void visitNamespaceVariables(String namespace, VarExporter.Visitor visitor)`
Visit all variables in the given namespace

#### Instance Methods

`public void export(Object obj, String prefix)`
Export all public fields and methods of a given object instance, including static fields, that are annotated with Export.

Parameters:

- obj- object instance to export
- prefix- prefix for variable names (e.g. "mywidget-")

`public void export(Class c, String prefix)`
Export all public static fields and methods of a given class that are annotated with Export.

Parameters:

- c- class to export
- prefix- prefix for variable names (e.g. "mywidget-")

`public void export(Object obj, Member member, String prefix, String name)`
Export a given public Field or Method of a given object instance. The member will be exported even if it does not have the Export annotation. This is mainly useful for exporting variables for which you cannot modify the code to add an annotation.

Parameters:

- obj- object instance
- member- Field of Method to export
- prefix- prefix for variable names (e.g. "mywidget-")
- name- Name to use for export (optional, will be ignored if Export annotation used)

`public void export(Class c, Member member, String prefix, String name)`
Export a given public static Field or Method of a given class. The member will be exported even if it does not have the Export annotation. This is mainly useful for exporting variables for which you cannot modify the code to add an annotation.

Parameters:

- c- class from which to export
- member- Field of Method to export
- prefix- prefix for variable names (e.g. "mywidget-")
- name- Name to use for export (optional, will be ignored if Export annotation used)

`public <T> T getValue(String variableName)`
Load the current value of a given variable

`public <T> Variable<T> getVariable(String variableName)`
Load the dynamic variable

`public void visitVariables(VarExporter.Visitor visitor)`
Visit all the values exported by this exporter.

`public Iterable<Variable> getVariables()`
Returns an iterator over the exported variables

`public void dump(PrintWriter out, boolean includeDoc)`
Write all variables, one per line, to the given writer, in the format "name=value".

`public void reset()`
Remove all exported variables.

## [ViewExportedVariablesServlet](https://github.com/indeedeng/util/blob/master/varexport/src/main/java/com/indeed/util/varexport/servlet/ViewExportedVariablesServlet.java)

Extends `javax.servlet.http.HttpServlet`

Servlet for displaying variables exported by VarExporter. Supports URL parameters:

- `ns=` namespace, default is global namespace
- `v=` variable name, can be repeated for multiple variables
- `doc=1` to include documentation in output
- `fmt=html` to get HTML formatting
- `browse=1` (with no other params) to get a browsable view of all namespaces

### Usage

Drop in to your `web.xml`. Example (URL path /debug/v):

```xml
    <servlet>
        <servlet-name>ViewExportedVariablesServlet</servlet-name>
        <servlet-class>
            com.indeed.util.varexport.servlet.ViewExportedVariablesServlet
        </servlet-class>
        <load-on-startup>1</load-on-startup>
    </servlet>

    <servlet-mapping>
        <servlet-name>ViewExportedVariablesServlet</servlet-name>
        <url-pattern>/debug/v</url-pattern>
    </servlet-mapping>
```


## Dependencies

- guava (15 ok)
- log4j
- servlet API (optional)
- freemarker (optional for servlet)
- junit-dep (4.X
- removes bundled hamcrest-core)
- hamcrest-library (1.3)
