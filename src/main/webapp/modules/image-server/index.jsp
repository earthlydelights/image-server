<%@page import="org.webjars.WebJarAssetLocator"
%><%@page contentType="text/html" pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="t" uri="http://net.aequologica.neo/jsp/jstl/layout"
%><%!

WebJarAssetLocator  LOCATOR                      = new WebJarAssetLocator();
int                 META_INF_RESOUCE_PATH_LENGTH = "META-INF/resources/".length();

%><%

request.setAttribute("plotly_min_js", LOCATOR.getFullPath("plotly.min.js").substring(META_INF_RESOUCE_PATH_LENGTH) );

%><t:layout module="image-server">

<h1>image-server</h1>

<div id="tester" style="width:100%;height:auto;"></div>

${model}

</t:layout>

<script type="text/javascript" charset="utf8" src="<c:url value='/${plotly_min_js}' />" ></script>

<script type="text/javascript">

$(document).ready(function() {

    TESTER = document.getElementById('tester');
    Plotly.plot( TESTER, [{
    x: [1, 2, 3, 4, 5],
    y: [1, 2, 4, 8, 16],
    mode: 'markers', }], {
    margin: { t: 0 } } );

});

</script>
