<#if (vars?size > 1)>
# ${name} variables at ${date}
<#list vars as var>
<#if (includeDoc && (var.doc?length > 0 || var.lastUpdated??))>

# ${var.doc}<#if var.lastUpdated??> (last update: ${var.lastUpdated?c})</#if>
${var}
<#else>
${var}
</#if>
</#list>
<#elseif (vars?size == 1)>
${vars?first.safeValue}
<#else>
null
</#if>

