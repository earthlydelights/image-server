<%@page import="net.aequologica.neo.geppaequo.webjars.WebJar"
%><%@   page contentType="text/html" pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="t" uri="http://net.aequologica.neo/jsp/jstl/layout"
%><t:layout jsmodules="moment handlebars">

<link rel="stylesheet" type="text/css" href="<c:url value='<%=WebJar.locate("bootstrap3/bootstrap-switch.css") %>' />" />

<h1>
  <img  style="float:left;  margin-right: 30px;" src="../assets/images/alphabet/051-16th-Century-letter-i-q90-187x200.jpg"/>
  <span style="width:100%; margin-left: -30px; width:100%;" >mage Server</span>
</h1>

<section id="attributes" style="display: table; background-color:#fafafa; padding: .5em;">
  <!--  template that will create row in the table-->
  <script id="rowsTemplate" type="text/x-handlebars-template">{{#each attributes}}<%--
  --%><div style="display: table-row;"><%--
    --%><label style="display: table-cell; white-space: nowrap; padding: 1em 1em 1em 0; white-space: nowrap; text-align:right;">{{key}}</label><%--
    --%><div id="{{key}}" style="display: table-cell;  width:100%;"><%--
       --%><input type="text" class="form-control" id="{{key}}" placeholder="{{key}}" value="{{value}}"><%--
    --%></div><%--
  --%></div>{{/each}}</script>
</section>

<div style="position: relative;width: 100%; margin-top: 1em;">
  &nbsp;
  <div style="position: absolute; left: 0; top:0;">
    <button id="loadMetadata" type="button" class="btn btn-secondary">
      <span>load metadata to application<img id="ajaxLoading_loadMetadata" src="<c:url value='/assets/images/animated_orange_refresh_22.png'/>"></span>
    </button>
  </div>
  <div style="position: absolute;right: 0; top:0;">
    <button id="reload" type="button" class="btn btn-secondary">
      <span>reset from document<img id="ajaxLoading_reload" src="<c:url value='/assets/images/animated_orange_refresh_22.png'/>"></span>
    </button>
    <button id="save" type="button" class="btn btn-secondary">
      <span>save to document service<img id="ajaxLoading_save" src="<c:url value='/assets/images/animated_orange_refresh_22.png'/>"></span>
    </button>
  </div>
</div>
<br/>
<div id="messages"></div>
<script id="alertTemplate" type="text/x-handlebars-template"><div class="alert alert-dismissable alert-{{alertClass}}"><%--
    --%><small>{{now}}</small>&nbsp;|&nbsp;<strong>{{strong}}</strong> {{message}}<%--
    --%><button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button><%--
  --%></div></script>
<%--  
<div  id="alert" class="alert alert-dismissable" style="display:none; ">
  <button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button>
</div>
--%>
</t:layout>

<script type="text/javascript" charset="utf8" src="<c:url value='<%=WebJar.locate("jolokia.js")%>'          />" ></script>
<script type="text/javascript" charset="utf8" src="<c:url value='<%=WebJar.locate("jolokia-simple.js")%>'   />" ></script>

<script type="text/javascript">

$(document).ready(function() {
  
  // handlebars
  var rowTemplate = Handlebars.compile($("#rowsTemplate").html());
  var alertTemplate = Handlebars.compile($("#alertTemplate").html());
  
  var JolokiaHelper = function (jolokiaResource, configName) {

    var _this_ = {};

    var j4p  = new Jolokia(jolokiaResource);

    var mbean = configName+":name=config";

    _this_.bindTexts = function(elements) {
        elements.each( function(index, el) {
          var ID = $(el).attr('id');
          if (ID) {
              ID = _.capitalize(ID);
              var value = j4p.getAttribute( mbean, ID );
              value = decodeURIComponent(value);
              $(el).attr('value', value);
              $(el).val(value);
              
              $(el).on('change', function(e) {
                var val = $( this ).val();
                val = encodeURIComponent(val);
                if (val) {
                  j4p.setAttribute( mbean, ID, val);
                  window.log(mbean, ID, val);
                }
              });              
          }
        });
    };

    _this_.reload = function() {
      $("#ajaxLoading_reload").attr({src :"<c:url value='/assets/images/animated_orange_refresh_22.gif'/>" });
      j4p.execute( mbean, "Configuration.reload", {
          success: function(value) {
              if (value) {
                console.log(JSON.stringify(value));
              }
              displayMessage("success", configName, "reloaded!")
              $("#ajaxLoading_reload").attr({src :"<c:url value='/assets/images/animated_orange_refresh_22.png'/>" });
              _this_.bindTexts($('input'));
          },
          error: function(response) {
              if (response.error) {
                 console.log(response.error);
              }
              displayMessage("danger", configName, response.error)
              $("#ajaxLoading_reload").attr({src :"<c:url value='/assets/images/animated_orange_refresh_22.png'/>" });
          }
       });
    };

    _this_.save = function() {
      $("#ajaxLoading_save").attr({src :"<c:url value='/assets/images/animated_orange_refresh_22.gif'/>" });
      j4p.execute( mbean, "Configuration.Application.save", {
          success: function(value) {
              console.log(JSON.stringify(value));
              displayMessage("success", configName, "saved!")
              $("#ajaxLoading_save").attr({src :"<c:url value='/assets/images/animated_orange_refresh_22.png'/>" });
          },
          error: function(response) {
              console.log();
              displayMessage("danger", configName, response.error)
              $("#ajaxLoading_save").attr({src :"<c:url value='/assets/images/animated_orange_refresh_22.png'/>" });
          }
       });
    };

    function displayMessage(alertClass, strong, message) {
        var now = moment().format('MMMM Do YYYY, h:mm:ss a');
        $("#messages").empty().html(alertTemplate({
          alertClass: alertClass,
          strong: strong,
          message: message,
          now: now,
        }));
    }

    $("button#loadMetadata").click(function( event ) {
      $("#ajaxLoading_loadMetadata").attr({src :"<c:url value='/assets/images/animated_orange_refresh_22.gif'/>" });
      $.post( "<c:url value='/api/earthly-delights-garden/image/v1/reload'/>")  
        .done(function() {
          alert('done!');
        })
        .fail(function() {
          alert('error!');
        })
        .always(function() {
          $("#ajaxLoading_loadMetadata").attr({src :"<c:url value='/assets/images/animated_orange_refresh_22.png'/>" });
      });
      event.preventDefault();
    });

    $("button#reload").click(function( event ) {
      _this_.reload();
      event.preventDefault();
    });

    $("button#save").click(function( event ) {
      _this_.save();
      event.preventDefault();
    });

    return _this_;
  };

  // /handlebars
  var $attributes = rowTemplate({ attributes : [
    { key : "title",      value : ""},
    { key : "image",      value : ""},
    { key : "app",        value : ""},
    { key : "wikipedia",  value : ""},
    { key : "randomizer", value : ""},
  ]});
  $('section#attributes').append($attributes);

  var helper = new JolokiaHelper("<c:url value='/jolokia' />", "imageserver");
  helper.bindTexts($('input'));

});

</script>
