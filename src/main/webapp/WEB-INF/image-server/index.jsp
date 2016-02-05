<%@page import="org.webjars.WebJarAssetLocator"
%><%@page contentType="text/html" pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="t" uri="http://net.aequologica.neo/jsp/jstl/layout"
%><%!

WebJarAssetLocator  LOCATOR                      = new WebJarAssetLocator();
int                 META_INF_RESOUCE_PATH_LENGTH = "META-INF/resources/".length();

%><%
  Boolean isWizard = request.isUserInRole("wizard") || request.isUserInRole("BUILTIN\\Administrators");
  request.setAttribute("isGeppaequoWizard", isWizard);
%><%

request.setAttribute("plotly_min_js", LOCATOR.getFullPath("plotly.min.js").substring(META_INF_RESOUCE_PATH_LENGTH) );

%><t:layout module="image-server">

<h1 id="title"></h1>

<a id="image"     class="btn btn-secondary btn-lg" href="#" role="button">image <span id="width"></span>&nbsp;x&nbsp;<span id="height"></span></a>

<a id="wikipedia" class="btn btn-secondary btn-lg" href="#" role="button">wikipedia article</a>

<hr/>

<h2><span id="clickcount"></span> clicks</h2>

<div id="tester" style="width:100%;height:auto;"></div>

<c:if test='${isGeppaequoWizard}' >
<button id="reset" class="btn btn-secondary btn-sm btn-danger" style="float:right;">delete all clicks</button>
</c:if>

</t:layout>

<script type="text/javascript" charset="utf8" src="<c:url value='/${plotly_min_js}' />" ></script>

<script type="text/javascript">

$(document).ready(function() {
  
  $('#clickcount').text("${model.size()}");

  $.getJSON( "<c:url value='/earthly-delights-garden-api/image/v1/metadata' />", function( data ) {
    $('#title').text(data.title);
    $('#width').text(data.width);
    $('#height').text(data.height);
    $('#image').attr("href", "${pageContext.request.contextPath}" + data.image);
    $('#wikipedia').attr("href", data.wikipedia);
  });

  TESTER = document.getElementById('tester');
  Plotly.plot( TESTER, [{
      x: [<c:forEach var="p" items="${model}">${p.x},</c:forEach>],
      y: [<c:forEach var="p" items="${model}">${p.y},</c:forEach>],
  mode: 'markers', }], 
  { margin: { b: 50, l: 50, r: 0, t: 20 } },
  { displayModeBar: false } );

  $('#reset').on('click', function(){
    $.post( "<c:url value='/earthly-delights-garden-api/image/v1/points/reset' />", function( data ) {
      $('#clickcount').text("0");
      TESTER.data = [{
        x: [],
        y: [],
        mode: 'markers', }];
      Plotly.redraw( TESTER );
    });
  });

});

</script>
