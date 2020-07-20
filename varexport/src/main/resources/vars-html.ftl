<html>
<head>
<title>${name?html} variables at ${date?html}</title>
<style type="text/css">
    input {
        width: 300px;
    }
    body {
        margin: 0;
        padding: 10px;
    }
    table {
        margin-top: 1em;
        box-sizing: border-box;
        width: 100%;
        table-layout: fixed;
        border-collapse: collapse;
    }
    caption {
        text-align: left;
        font-family: monospace;
        font-size: 120%;
        font-weight: bold;
    }
    tr:hover {
        background-color: #eee;
    }
    th, td {
        padding: 2px 10px;
        border: solid #666;
        border-width: 1px 0;
        overflow: hidden;
        text-overflow: ellipsis;
        font-family: monospace;
        text-align: left;
        white-space: nowrap;
        word-wrap: normal;
    }
    tr:hover td {
        white-space: normal;
        word-wrap: break-word;
    }
    b {
        font-weight: normal;
        background-color: #ccc;
    }
    .q-0 {
        background-color: #fcc;
    }
    .q-1 {
        background-color: #ffc;
    }
    .q-2 {
        background-color: #cfc;
    }
    .q-3 {
        background-color: #cff;
    }
    .q-4 {
        background-color: #ccf;
    }
    .q-5 {
        background-color: #fcf;
    }
</style>
</head>
<body>
<input id="input" placeholder="You can query with multiple words" disabled>
<table>
    <caption>${name?html} variables at ${date?html}</caption>
    <thead>
        <tr><th>Name</th><th>Value</th><#if includeDoc><th>Doc</th><th>Last Updated</th></#if></tr>
    </thead>
    <tbody id="tbody">
    <#list vars as var>
        <tr>
            <td data-key="${var.name?html}">${var.name?html}</td>
            <td>${var.valueString?html}</td>
            <#if includeDoc>
            <td>${var.doc?html}</td>
            <td><#if var.lastUpdated??>${var.lastUpdated?html}</#if></td>
            </#if>
        </tr>
    </#list>
    </tbody>
</table>
<p><a href="${urlPath}?browse=1">View all namespaces</a></p>
<script>
    var varsIndex = ${varsIndex};
    var input = document.getElementById('input');
    var tbody = document.getElementById('tbody');

    var parseQuery = function(query) {
        query = query.trim();
        if (query.length === 0) {
            return [];
        }
        return query
                .split(/[^a-z0-9]+/i).join(' ') // remove repeated non-query-able characters
                .split(' ');    // split to array
    };

    var isSameQueries = function(left, right) {
        if (left.length !== right.length) {
            return false;
        }
        for (var i = 0; i < left.length; i++) {
            if (left[i] !== right[i]) {
                return false;
            }
        }
        return true;
    };

    var highlightTr = function(tr, queries) {
        var keyTd = tr.getElementsByTagName('td')[0];
        var key = keyTd.getAttribute('data-key');
        if (!queries || queries.length === 0) {
            keyTd.innerHTML = key;
            return;
        }
        var lowerKey = key.toLowerCase();
        var indexes = [];
        for (var i = 0; i < lowerKey.length + 1; i++) {
            indexes.push({s:[], e:[]});
        }
        for (var i = 0; i < queries.length; i++) {
            var query = queries[i];
            var index = lowerKey.indexOf(query);
            while (index >= 0) {
                indexes[index].s.push(i);
                indexes[index + query.length].e.push(i);
                index = lowerKey.indexOf(query, index + 1);
            }
        }
        var result = '';
        for (var i = 0; i < key.length + 1; i++) {
            var index = indexes[i];
            for (var e = 0; e < index.e.length; e++) {
                result += '</b>';
            }
            for (var s = 0; s < index.s.length; s++) {
                result += '<b class="q-' + index.s[s] + '">';
            }
            if (i === key.length) {
                break;
            }
            result += key.charAt(i);
        }
        keyTd.innerHTML = result;
    };

    /**
     * Search query in the index
     * @returns array of indexes which contain query in name
     */
    var search = function(query) {
        if (query.length <= 1) {
            var result = varsIndex.uniGram[query];
            return result ? [result] : [[]];
        }
        if (query.length <= 2) {
            var result = varsIndex.biGram[query];
            return result ? [result] : [[]];
        }
        var results = [];
        for (var i = 0; i < query.length - 2; i++) {
            var key = query.substr(i, 3);
            var result = varsIndex.triGram[key];
            results.push(result ? result : []);
        }
        return results;
    };

    /**
     * Merge multiple sorted integer arrays. The result must have values which all input arrays have.
     * Example
     * input: [[0,1,2,4,8,16], [1,2,3,5,8,13], [2,4,6,8,10]]
     * output: [2,8]
     * @returns Single integer array
     */
    var innerJoin = function(results) {
        if (results.length === 0) {
            return [];
        }
        results.sort(function(arr1, arr2) {
            var diff = arr1.length - arr2.length;
            if (diff !== 0) {
                return diff;
            }
            for (var i = 0; i < arr1.length; i++) {
                var diff = arr1[i] - arr2[i];
                if (diff !== 0) {
                    return diff;
                }
            }
            return -1;
        });
        var current = results[0].slice(0);
        for (var i = 1; i < results.length; i++) {
            var result = results[i];
            var ri = 0;
            var next = [];
            var j = k = 0;
            while (j < current.length && k < result.length) {
                var c = current[j];
                var r = result[k];
                if (c === r) {
                    next.push(current[j]);
                    j++;
                    k++;
                } else if (c < r) {
                    j++;
                } else {
                    k++;
                }
            }
            current = next;
            if (current.length === 0) {
                break;
            }
        }
        return current;
    };

    var prevQueries = parseQuery(input.value);
    input.onkeyup = function(e) {
        var queries = parseQuery(input.value);
        if (isSameQueries(prevQueries, queries)) {
            return;
        }
        prevQueries = queries;
        var trs = tbody.getElementsByTagName('tr');
        if (queries.length === 0) {
            for (var i = 0; i < trs.length; i++) {
                var tr = trs[i];
                highlightTr(tr);
                tr.style.display = 'table-row';
            }
            return;
        }
        var results = [];
        for (var i = 0; i < queries.length; i++) {
            var query = queries[i];
            results = results.concat(search(query));
        }
        var result = innerJoin(results);
        var tooManyMatch = result.length > 256;
        var ri = 0;
        for (var i = 0; i < trs.length; i++) {
            var tr = trs[i];
            var next = result.length > ri ? result[ri] : -1;
            var visible = (next === i);
            if (visible) {
                ri++;
                highlightTr(tr, tooManyMatch ? undefined : queries);
                tr.style.display = 'table-row';
            } else {
                tr.style.display = 'none';
            }
        }
    };
    input.disabled = false;
    input.focus();
</script>
</body>
</html>


