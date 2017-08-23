<%@page import="net.aequologica.neo.geppaequo.webjars.WebJar"
%><%@page contentType="text/html" pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="t" uri="http://net.aequologica.neo/jsp/jstl/layout"
%><%
  Boolean isWizard = request.isUserInRole("wizard") || request.isUserInRole("BUILTIN\\Administrators");
  request.setAttribute("isGeppaequoWizard", isWizard);
%><t:layout>
<style>
#app > img{
opacity:.666;
}
#app > img:hover {
opacity:1;
}
</style>
<h1><span id="title"></span></h1>

<a id="reload"    class="btn btn-outline-secondary btn-sm" href="#" role="button" title="reload page (F5)"        ><img src="<c:url value='/assets/images/animated_orange_refresh_22.gif'/>"></a>
<a id="wikipedia" class="btn btn-outline-secondary btn-sm" href="#" role="button" title="read wikipedia article"  ><i class="fa fa-wikipedia-w"></i></a>
<a id="image"     class="btn btn-outline-secondary btn-sm" href="#" role="button" title="display image"           ><i class="fa fa-picture-o"></i> <span id="width"></span>&nbsp;x&nbsp;<span id="height"></span>&nbsp;<span id="bytes"></span></a>
<a id="app"                                                               title="Earthly Delights Garden "><img alt="" class="img-circle" src="<c:url value='/images/earthly.delights.garden-32x32.png'/>"></a>

<c:if test='${not empty model.exception}' >
<div class="alert alert-warning" style="margin-top:1em;">Exception raised: ${model.exception.getClass()} - [ ${model.exception.message} ]<br/>Caused by: ${model.exception.cause.getClass()} - [ ${model.exception.cause.message} ]</div>
</c:if>

<hr/>

<h2><span id="clickcount"></span> clicks</h2>


<div id="thePlot" style="width:100%;height:auto;"></div>

<c:if test='${isGeppaequoWizard}' >
<button id="reset" class="btn btn-outline-secondary btn-sm btn-danger" style="float:right;">delete all clicks</button>
</c:if>



</t:layout>

<script type="text/javascript" charset="utf8" src="<c:url value='<%=WebJar.locate("plotly.min.js") %>' />" ></script>

<script type="text/javascript">

$(document).ready(function() {
  
  $('#clickcount').text("${model.points.size()}");

  $.getJSON( "<c:url value='/api/earthly-delights-garden/image/v1/metadata' />", function( data ) {
    $('#title').empty().text(decodeURIComponent(data.title));
    $('#width').text(data.width);
    $('#height').text(data.height);
    $('#bytes').text("("+new Intl.NumberFormat().format(Math.round(data.bytes/1000.))+" Kb)");
    $('#image').attr("href", "${pageContext.request.contextPath}" + decodeURIComponent(data.image));
    $('#wikipedia').attr("href", decodeURIComponent(data.wikipedia));
    $('#app').attr("href", decodeURIComponent(data.app));
    $('#reload img').attr("src", "<c:url value='/assets/images/animated_orange_refresh_22.png'/>");
  });

  var thePlot = document.getElementById('thePlot');
  Plotly.plot( 
    thePlot, 
    [{ x    : [<c:forEach var="p" items="${model.points}">${p.x},</c:forEach>],
       y    : [<c:forEach var="p" items="${model.points}">${p.y},</c:forEach>],
       mode : 'markers' }], 
    { margin: { b: 50, l: 50, r: 0, t: 20 } },
    { displayModeBar: false } 
  );

  $('#reload').on('click', function(){
    location.reload();
  });

  $('#reset').on('click', function(){
    
    $.ajax({
      type : "DELETE",
      url  : "<c:url value='/api/earthly-delights-garden/image/v1/points' />",
    }).done(function( data ) {
      $('#clickcount').text("0");
      thePlot.data = [{
        x: [],
        y: [],
        mode: 'markers', }];
      Plotly.redraw( thePlot );
    });
  });

});

</script>
