<html>
<head>
    <title>${name} variables at ${date}</title>
    <style type="text/css">
        th { text-align: left; }
        td { border: 1px solid gray; font-family: monospace; }
    </style>
</head>
<body>
<h1>${name} variables at ${date}</h1>
<table cellpadding="2" cellspacing="2" style="border-collapse: collapse">
    <tr><th>Name</th><th>Value</th><#if includeDoc><th>Doc</th><th>Last Updated</th></#if></tr>
<#list vars as var>
    <tr>
        <td style="white-space: nowrap;">${var.name}</td>
        <td>${var.valueString}</td>
        <#if includeDoc>
        <td>${var.doc}</td>
        <td><#if var.lastUpdated??>${var.lastUpdated?c}</#if></td>
        </#if>
    </tr>
</#list>
</table>
<p><a href="${urlPath}?browse=1">View all namespaces</a></p>
</body>
</html>


