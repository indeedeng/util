<html>
<head>
<title>Variable Namespaces</title>
    <style type="text/css">
        th { text-align: left; }
        td { border: 1px solid gray; font-family: monospace; }
    </style>
</head>
<body>
<h1>Variable Namespaces</h1>
<table cellpadding="4" cellspacing="4" style="border-collapse: collapse">
<tr>
    <th>Namespace</th>
    <th>Parent</th>
    <th>&nbsp;</th>
</tr>
<tr>
    <td>global</td>
    <td>&nbsp;</td>
    <td>
        <a href="${urlPath}?fmt=html">view</a>
        (<a href="${urlPath}?fmt=html&doc=1">with doc</a>,
        <a href="${urlPath}">raw</a>)
    </td>
</tr>
<#list namespaces as namespace>
<tr>
    <td>${namespace}</td>
    <td>
        <#if parents[namespace] == "none">
        <b>none</b>
        <#elseif (parents[namespace]?length > 0)>
        <a href="${urlPath}?ns=${parents[namespace]}">${parents[namespace]}</a>
        <#else>
        <i>global</i>
        </#if>
    </td>
    <td>
        <a href="${urlPath}?ns=${namespace}&fmt=html">view</a>
        (<a href="${urlPath}?ns=${namespace}&fmt=html&doc=1">with doc</a>,
        <a href="${urlPath}?ns=${namespace}">raw</a>)
    </td>
</tr>
</#list>
</table>
</body>
</html>

