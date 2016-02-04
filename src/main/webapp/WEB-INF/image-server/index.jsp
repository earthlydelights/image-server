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

<h1 id="title"></h1>

<button type="button" class="btn btn-secondary btn-sm"><a id="image" href="#"><span id="width"></span>&nbsp;x&nbsp;<span id="height"></span></a></button>

<button type="button" class="btn btn-secondary btn-sm"><a id="wikipedia" href="#">wikipedia article</a></button>

<hr/>

<h2>Clicks</h2>

<div id="tester" style="width:100%;height:auto;"></div>

</t:layout>

<script type="text/javascript" charset="utf8" src="<c:url value='/${plotly_min_js}' />" ></script>

<script type="text/javascript">

$(document).ready(function() {

  $.getJSON( "<c:url value='/earthly-delights-garden-api/image/v1/metadata' />", function( data ) {
    $('#title').text(data.title);
    $('#width').text(data.width);
    $('#height').text(data.height);
    $('#image').attr("href", "${pageContext.request.contextPath}" + data.image);
    $('#wikipedia').attr("href", data.wikipedia);
  });
  

  TESTER = document.getElementById('tester');
  Plotly.plot( TESTER, [{
      x: [
<c:forEach var="p" items="${model}">
      ${p.x},
</c:forEach>
      ],
      y: [
<c:forEach var="p" items="${model}">
      ${p.y},
</c:forEach>
  ],
  mode: 'markers', }], {
  margin: { t: 0 } } );

});

</script>
