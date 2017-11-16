/*	SWFObject v2.2 <http://code.google.com/p/swfobject/> 
	is released under the MIT License <http://www.opensource.org/licenses/mit-license.php> 
*/
var swfobject=function(){var D="undefined",r="object",S="Shockwave Flash",W="ShockwaveFlash.ShockwaveFlash",q="application/x-shockwave-flash",R="SWFObjectExprInst",x="onreadystatechange",O=window,j=document,t=navigator,T=false,U=[h],o=[],N=[],I=[],l,Q,E,B,J=false,a=false,n,G,m=true,M=function(){var aa=typeof j.getElementById!=D&&typeof j.getElementsByTagName!=D&&typeof j.createElement!=D,ah=t.userAgent.toLowerCase(),Y=t.platform.toLowerCase(),ae=Y?/win/.test(Y):/win/.test(ah),ac=Y?/mac/.test(Y):/mac/.test(ah),af=/webkit/.test(ah)?parseFloat(ah.replace(/^.*webkit\/(\d+(\.\d+)?).*$/,"$1")):false,X=!+"\v1",ag=[0,0,0],ab=null;if(typeof t.plugins!=D&&typeof t.plugins[S]==r){ab=t.plugins[S].description;if(ab&&!(typeof t.mimeTypes!=D&&t.mimeTypes[q]&&!t.mimeTypes[q].enabledPlugin)){T=true;X=false;ab=ab.replace(/^.*\s+(\S+\s+\S+$)/,"$1");ag[0]=parseInt(ab.replace(/^(.*)\..*$/,"$1"),10);ag[1]=parseInt(ab.replace(/^.*\.(.*)\s.*$/,"$1"),10);ag[2]=/[a-zA-Z]/.test(ab)?parseInt(ab.replace(/^.*[a-zA-Z]+(.*)$/,"$1"),10):0}}else{if(typeof O.ActiveXObject!=D){try{var ad=new ActiveXObject(W);if(ad){ab=ad.GetVariable("$version");if(ab){X=true;ab=ab.split(" ")[1].split(",");ag=[parseInt(ab[0],10),parseInt(ab[1],10),parseInt(ab[2],10)]}}}catch(Z){}}}return{w3:aa,pv:ag,wk:af,ie:X,win:ae,mac:ac}}(),k=function(){if(!M.w3){return}if((typeof j.readyState!=D&&j.readyState=="complete")||(typeof j.readyState==D&&(j.getElementsByTagName("body")[0]||j.body))){f()}if(!J){if(typeof j.addEventListener!=D){j.addEventListener("DOMContentLoaded",f,false)}if(M.ie&&M.win){j.attachEvent(x,function(){if(j.readyState=="complete"){j.detachEvent(x,arguments.callee);f()}});if(O==top){(function(){if(J){return}try{j.documentElement.doScroll("left")}catch(X){setTimeout(arguments.callee,0);return}f()})()}}if(M.wk){(function(){if(J){return}if(!/loaded|complete/.test(j.readyState)){setTimeout(arguments.callee,0);return}f()})()}s(f)}}();function f(){if(J){return}try{var Z=j.getElementsByTagName("body")[0].appendChild(C("span"));Z.parentNode.removeChild(Z)}catch(aa){return}J=true;var X=U.length;for(var Y=0;Y<X;Y++){U[Y]()}}function K(X){if(J){X()}else{U[U.length]=X}}function s(Y){if(typeof O.addEventListener!=D){O.addEventListener("load",Y,false)}else{if(typeof j.addEventListener!=D){j.addEventListener("load",Y,false)}else{if(typeof O.attachEvent!=D){i(O,"onload",Y)}else{if(typeof O.onload=="function"){var X=O.onload;O.onload=function(){X();Y()}}else{O.onload=Y}}}}}function h(){if(T){V()}else{H()}}function V(){var X=j.getElementsByTagName("body")[0];var aa=C(r);aa.setAttribute("type",q);var Z=X.appendChild(aa);if(Z){var Y=0;(function(){if(typeof Z.GetVariable!=D){var ab=Z.GetVariable("$version");if(ab){ab=ab.split(" ")[1].split(",");M.pv=[parseInt(ab[0],10),parseInt(ab[1],10),parseInt(ab[2],10)]}}else{if(Y<10){Y++;setTimeout(arguments.callee,10);return}}X.removeChild(aa);Z=null;H()})()}else{H()}}function H(){var ag=o.length;if(ag>0){for(var af=0;af<ag;af++){var Y=o[af].id;var ab=o[af].callbackFn;var aa={success:false,id:Y};if(M.pv[0]>0){var ae=c(Y);if(ae){if(F(o[af].swfVersion)&&!(M.wk&&M.wk<312)){w(Y,true);if(ab){aa.success=true;aa.ref=z(Y);ab(aa)}}else{if(o[af].expressInstall&&A()){var ai={};ai.data=o[af].expressInstall;ai.width=ae.getAttribute("width")||"0";ai.height=ae.getAttribute("height")||"0";if(ae.getAttribute("class")){ai.styleclass=ae.getAttribute("class")}if(ae.getAttribute("align")){ai.align=ae.getAttribute("align")}var ah={};var X=ae.getElementsByTagName("param");var ac=X.length;for(var ad=0;ad<ac;ad++){if(X[ad].getAttribute("name").toLowerCase()!="movie"){ah[X[ad].getAttribute("name")]=X[ad].getAttribute("value")}}P(ai,ah,Y,ab)}else{p(ae);if(ab){ab(aa)}}}}}else{w(Y,true);if(ab){var Z=z(Y);if(Z&&typeof Z.SetVariable!=D){aa.success=true;aa.ref=Z}ab(aa)}}}}}function z(aa){var X=null;var Y=c(aa);if(Y&&Y.nodeName=="OBJECT"){if(typeof Y.SetVariable!=D){X=Y}else{var Z=Y.getElementsByTagName(r)[0];if(Z){X=Z}}}return X}function A(){return !a&&F("6.0.65")&&(M.win||M.mac)&&!(M.wk&&M.wk<312)}function P(aa,ab,X,Z){a=true;E=Z||null;B={success:false,id:X};var ae=c(X);if(ae){if(ae.nodeName=="OBJECT"){l=g(ae);Q=null}else{l=ae;Q=X}aa.id=R;if(typeof aa.width==D||(!/%$/.test(aa.width)&&parseInt(aa.width,10)<310)){aa.width="310"}if(typeof aa.height==D||(!/%$/.test(aa.height)&&parseInt(aa.height,10)<137)){aa.height="137"}j.title=j.title.slice(0,47)+" - Flash Player Installation";var ad=M.ie&&M.win?"ActiveX":"PlugIn",ac="MMredirectURL="+O.location.toString().replace(/&/g,"%26")+"&MMplayerType="+ad+"&MMdoctitle="+j.title;if(typeof ab.flashvars!=D){ab.flashvars+="&"+ac}else{ab.flashvars=ac}if(M.ie&&M.win&&ae.readyState!=4){var Y=C("div");X+="SWFObjectNew";Y.setAttribute("id",X);ae.parentNode.insertBefore(Y,ae);ae.style.display="none";(function(){if(ae.readyState==4){ae.parentNode.removeChild(ae)}else{setTimeout(arguments.callee,10)}})()}u(aa,ab,X)}}function p(Y){if(M.ie&&M.win&&Y.readyState!=4){var X=C("div");Y.parentNode.insertBefore(X,Y);X.parentNode.replaceChild(g(Y),X);Y.style.display="none";(function(){if(Y.readyState==4){Y.parentNode.removeChild(Y)}else{setTimeout(arguments.callee,10)}})()}else{Y.parentNode.replaceChild(g(Y),Y)}}function g(ab){var aa=C("div");if(M.win&&M.ie){aa.innerHTML=ab.innerHTML}else{var Y=ab.getElementsByTagName(r)[0];if(Y){var ad=Y.childNodes;if(ad){var X=ad.length;for(var Z=0;Z<X;Z++){if(!(ad[Z].nodeType==1&&ad[Z].nodeName=="PARAM")&&!(ad[Z].nodeType==8)){aa.appendChild(ad[Z].cloneNode(true))}}}}}return aa}function u(ai,ag,Y){var X,aa=c(Y);if(M.wk&&M.wk<312){return X}if(aa){if(typeof ai.id==D){ai.id=Y}if(M.ie&&M.win){var ah="";for(var ae in ai){if(ai[ae]!=Object.prototype[ae]){if(ae.toLowerCase()=="data"){ag.movie=ai[ae]}else{if(ae.toLowerCase()=="styleclass"){ah+=' class="'+ai[ae]+'"'}else{if(ae.toLowerCase()!="classid"){ah+=" "+ae+'="'+ai[ae]+'"'}}}}}var af="";for(var ad in ag){if(ag[ad]!=Object.prototype[ad]){af+='<param name="'+ad+'" value="'+ag[ad]+'" />'}}aa.outerHTML='<object classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000"'+ah+">"+af+"</object>";N[N.length]=ai.id;X=c(ai.id)}else{var Z=C(r);Z.setAttribute("type",q);for(var ac in ai){if(ai[ac]!=Object.prototype[ac]){if(ac.toLowerCase()=="styleclass"){Z.setAttribute("class",ai[ac])}else{if(ac.toLowerCase()!="classid"){Z.setAttribute(ac,ai[ac])}}}}for(var ab in ag){if(ag[ab]!=Object.prototype[ab]&&ab.toLowerCase()!="movie"){e(Z,ab,ag[ab])}}aa.parentNode.replaceChild(Z,aa);X=Z}}return X}function e(Z,X,Y){var aa=C("param");aa.setAttribute("name",X);aa.setAttribute("value",Y);Z.appendChild(aa)}function y(Y){var X=c(Y);if(X&&X.nodeName=="OBJECT"){if(M.ie&&M.win){X.style.display="none";(function(){if(X.readyState==4){b(Y)}else{setTimeout(arguments.callee,10)}})()}else{X.parentNode.removeChild(X)}}}function b(Z){var Y=c(Z);if(Y){for(var X in Y){if(typeof Y[X]=="function"){Y[X]=null}}Y.parentNode.removeChild(Y)}}function c(Z){var X=null;try{X=j.getElementById(Z)}catch(Y){}return X}function C(X){return j.createElement(X)}function i(Z,X,Y){Z.attachEvent(X,Y);I[I.length]=[Z,X,Y]}function F(Z){var Y=M.pv,X=Z.split(".");X[0]=parseInt(X[0],10);X[1]=parseInt(X[1],10)||0;X[2]=parseInt(X[2],10)||0;return(Y[0]>X[0]||(Y[0]==X[0]&&Y[1]>X[1])||(Y[0]==X[0]&&Y[1]==X[1]&&Y[2]>=X[2]))?true:false}function v(ac,Y,ad,ab){if(M.ie&&M.mac){return}var aa=j.getElementsByTagName("head")[0];if(!aa){return}var X=(ad&&typeof ad=="string")?ad:"screen";if(ab){n=null;G=null}if(!n||G!=X){var Z=C("style");Z.setAttribute("type","text/css");Z.setAttribute("media",X);n=aa.appendChild(Z);if(M.ie&&M.win&&typeof j.styleSheets!=D&&j.styleSheets.length>0){n=j.styleSheets[j.styleSheets.length-1]}G=X}if(M.ie&&M.win){if(n&&typeof n.addRule==r){n.addRule(ac,Y)}}else{if(n&&typeof j.createTextNode!=D){n.appendChild(j.createTextNode(ac+" {"+Y+"}"))}}}function w(Z,X){if(!m){return}var Y=X?"visible":"hidden";if(J&&c(Z)){c(Z).style.visibility=Y}else{v("#"+Z,"visibility:"+Y)}}function L(Y){var Z=/[\\\"<>\.;]/;var X=Z.exec(Y)!=null;return X&&typeof encodeURIComponent!=D?encodeURIComponent(Y):Y}var d=function(){if(M.ie&&M.win){window.attachEvent("onunload",function(){var ac=I.length;for(var ab=0;ab<ac;ab++){I[ab][0].detachEvent(I[ab][1],I[ab][2])}var Z=N.length;for(var aa=0;aa<Z;aa++){y(N[aa])}for(var Y in M){M[Y]=null}M=null;for(var X in swfobject){swfobject[X]=null}swfobject=null})}}();return{registerObject:function(ab,X,aa,Z){if(M.w3&&ab&&X){var Y={};Y.id=ab;Y.swfVersion=X;Y.expressInstall=aa;Y.callbackFn=Z;o[o.length]=Y;w(ab,false)}else{if(Z){Z({success:false,id:ab})}}},getObjectById:function(X){if(M.w3){return z(X)}},embedSWF:function(ab,ah,ae,ag,Y,aa,Z,ad,af,ac){var X={success:false,id:ah};if(M.w3&&!(M.wk&&M.wk<312)&&ab&&ah&&ae&&ag&&Y){w(ah,false);K(function(){ae+="";ag+="";var aj={};if(af&&typeof af===r){for(var al in af){aj[al]=af[al]}}aj.data=ab;aj.width=ae;aj.height=ag;var am={};if(ad&&typeof ad===r){for(var ak in ad){am[ak]=ad[ak]}}if(Z&&typeof Z===r){for(var ai in Z){if(typeof am.flashvars!=D){am.flashvars+="&"+ai+"="+Z[ai]}else{am.flashvars=ai+"="+Z[ai]}}}if(F(Y)){var an=u(aj,am,ah);if(aj.id==ah){w(ah,true)}X.success=true;X.ref=an}else{if(aa&&A()){aj.data=aa;P(aj,am,ah,ac);return}else{w(ah,true)}}if(ac){ac(X)}})}else{if(ac){ac(X)}}},switchOffAutoHideShow:function(){m=false},ua:M,getFlashPlayerVersion:function(){return{major:M.pv[0],minor:M.pv[1],release:M.pv[2]}},hasFlashPlayerVersion:F,createSWF:function(Z,Y,X){if(M.w3){return u(Z,Y,X)}else{return undefined}},showExpressInstall:function(Z,aa,X,Y){if(M.w3&&A()){P(Z,aa,X,Y)}},removeSWF:function(X){if(M.w3){y(X)}},createCSS:function(aa,Z,Y,X){if(M.w3){v(aa,Z,Y,X)}},addDomLoadEvent:K,addLoadEvent:s,getQueryParamValue:function(aa){var Z=j.location.search||j.location.hash;if(Z){if(/\?/.test(Z)){Z=Z.split("?")[1]}if(aa==null){return L(Z)}var Y=Z.split("&");for(var X=0;X<Y.length;X++){if(Y[X].substring(0,Y[X].indexOf("="))==aa){return L(Y[X].substring((Y[X].indexOf("=")+1)))}}}return""},expressInstallCallback:function(){if(a){var X=c(R);if(X&&l){X.parentNode.replaceChild(l,X);if(Q){w(Q,true);if(M.ie&&M.win){l.style.display="block"}}if(E){E(B)}}a=false}}}}();
/*!
 * jQuery UI 1.8.2
 *
 * Copyright (c) 2010 AUTHORS.txt (http://jqueryui.com/about)
 * Dual licensed under the MIT (MIT-LICENSE.txt)
 * and GPL (GPL-LICENSE.txt) licenses.
 *
 * http://docs.jquery.com/UI
 */
(function(c){c.ui=c.ui||{};if(!c.ui.version){c.extend(c.ui,{version:"1.8.2",plugin:{add:function(a,b,d){a=c.ui[a].prototype;for(var e in d){a.plugins[e]=a.plugins[e]||[];a.plugins[e].push([b,d[e]])}},call:function(a,b,d){if((b=a.plugins[b])&&a.element[0].parentNode)for(var e=0;e<b.length;e++)a.options[b[e][0]]&&b[e][1].apply(a.element,d)}},contains:function(a,b){return document.compareDocumentPosition?a.compareDocumentPosition(b)&16:a!==b&&a.contains(b)},hasScroll:function(a,b){if(c(a).css("overflow")==
"hidden")return false;b=b&&b=="left"?"scrollLeft":"scrollTop";var d=false;if(a[b]>0)return true;a[b]=1;d=a[b]>0;a[b]=0;return d},isOverAxis:function(a,b,d){return a>b&&a<b+d},isOver:function(a,b,d,e,f,g){return c.ui.isOverAxis(a,d,f)&&c.ui.isOverAxis(b,e,g)},keyCode:{ALT:18,BACKSPACE:8,CAPS_LOCK:20,COMMA:188,COMMAND:91,COMMAND_LEFT:91,COMMAND_RIGHT:93,CONTROL:17,DELETE:46,DOWN:40,END:35,ENTER:13,ESCAPE:27,HOME:36,INSERT:45,LEFT:37,MENU:93,NUMPAD_ADD:107,NUMPAD_DECIMAL:110,NUMPAD_DIVIDE:111,NUMPAD_ENTER:108,
NUMPAD_MULTIPLY:106,NUMPAD_SUBTRACT:109,PAGE_DOWN:34,PAGE_UP:33,PERIOD:190,RIGHT:39,SHIFT:16,SPACE:32,TAB:9,UP:38,WINDOWS:91}});c.fn.extend({_focus:c.fn.focus,focus:function(a,b){return typeof a==="number"?this.each(function(){var d=this;setTimeout(function(){c(d).focus();b&&b.call(d)},a)}):this._focus.apply(this,arguments)},enableSelection:function(){return this.attr("unselectable","off").css("MozUserSelect","")},disableSelection:function(){return this.attr("unselectable","on").css("MozUserSelect",
"none")},scrollParent:function(){var a;a=c.browser.msie&&/(static|relative)/.test(this.css("position"))||/absolute/.test(this.css("position"))?this.parents().filter(function(){return/(relative|absolute|fixed)/.test(c.curCSS(this,"position",1))&&/(auto|scroll)/.test(c.curCSS(this,"overflow",1)+c.curCSS(this,"overflow-y",1)+c.curCSS(this,"overflow-x",1))}).eq(0):this.parents().filter(function(){return/(auto|scroll)/.test(c.curCSS(this,"overflow",1)+c.curCSS(this,"overflow-y",1)+c.curCSS(this,"overflow-x",
1))}).eq(0);return/fixed/.test(this.css("position"))||!a.length?c(document):a},zIndex:function(a){if(a!==undefined)return this.css("zIndex",a);if(this.length){a=c(this[0]);for(var b;a.length&&a[0]!==document;){b=a.css("position");if(b=="absolute"||b=="relative"||b=="fixed"){b=parseInt(a.css("zIndex"));if(!isNaN(b)&&b!=0)return b}a=a.parent()}}return 0}});c.extend(c.expr[":"],{data:function(a,b,d){return!!c.data(a,d[3])},focusable:function(a){var b=a.nodeName.toLowerCase(),d=c.attr(a,"tabindex");return(/input|select|textarea|button|object/.test(b)?
!a.disabled:"a"==b||"area"==b?a.href||!isNaN(d):!isNaN(d))&&!c(a)["area"==b?"parents":"closest"](":hidden").length},tabbable:function(a){var b=c.attr(a,"tabindex");return(isNaN(b)||b>=0)&&c(a).is(":focusable")}})}})(jQuery);
;/*!
 * jQuery UI Widget 1.8.2
 *
 * Copyright (c) 2010 AUTHORS.txt (http://jqueryui.com/about)
 * Dual licensed under the MIT (MIT-LICENSE.txt)
 * and GPL (GPL-LICENSE.txt) licenses.
 *
 * http://docs.jquery.com/UI/Widget
 */
(function(b){var j=b.fn.remove;b.fn.remove=function(a,c){return this.each(function(){if(!c)if(!a||b.filter(a,[this]).length)b("*",this).add(this).each(function(){b(this).triggerHandler("remove")});return j.call(b(this),a,c)})};b.widget=function(a,c,d){var e=a.split(".")[0],f;a=a.split(".")[1];f=e+"-"+a;if(!d){d=c;c=b.Widget}b.expr[":"][f]=function(h){return!!b.data(h,a)};b[e]=b[e]||{};b[e][a]=function(h,g){arguments.length&&this._createWidget(h,g)};c=new c;c.options=b.extend({},c.options);b[e][a].prototype=
b.extend(true,c,{namespace:e,widgetName:a,widgetEventPrefix:b[e][a].prototype.widgetEventPrefix||a,widgetBaseClass:f},d);b.widget.bridge(a,b[e][a])};b.widget.bridge=function(a,c){b.fn[a]=function(d){var e=typeof d==="string",f=Array.prototype.slice.call(arguments,1),h=this;d=!e&&f.length?b.extend.apply(null,[true,d].concat(f)):d;if(e&&d.substring(0,1)==="_")return h;e?this.each(function(){var g=b.data(this,a),i=g&&b.isFunction(g[d])?g[d].apply(g,f):g;if(i!==g&&i!==undefined){h=i;return false}}):this.each(function(){var g=
b.data(this,a);if(g){d&&g.option(d);g._init()}else b.data(this,a,new c(d,this))});return h}};b.Widget=function(a,c){arguments.length&&this._createWidget(a,c)};b.Widget.prototype={widgetName:"widget",widgetEventPrefix:"",options:{disabled:false},_createWidget:function(a,c){this.element=b(c).data(this.widgetName,this);this.options=b.extend(true,{},this.options,b.metadata&&b.metadata.get(c)[this.widgetName],a);var d=this;this.element.bind("remove."+this.widgetName,function(){d.destroy()});this._create();
this._init()},_create:function(){},_init:function(){},destroy:function(){this.element.unbind("."+this.widgetName).removeData(this.widgetName);this.widget().unbind("."+this.widgetName).removeAttr("aria-disabled").removeClass(this.widgetBaseClass+"-disabled ui-state-disabled")},widget:function(){return this.element},option:function(a,c){var d=a,e=this;if(arguments.length===0)return b.extend({},e.options);if(typeof a==="string"){if(c===undefined)return this.options[a];d={};d[a]=c}b.each(d,function(f,
h){e._setOption(f,h)});return e},_setOption:function(a,c){this.options[a]=c;if(a==="disabled")this.widget()[c?"addClass":"removeClass"](this.widgetBaseClass+"-disabled ui-state-disabled").attr("aria-disabled",c);return this},enable:function(){return this._setOption("disabled",false)},disable:function(){return this._setOption("disabled",true)},_trigger:function(a,c,d){var e=this.options[a];c=b.Event(c);c.type=(a===this.widgetEventPrefix?a:this.widgetEventPrefix+a).toLowerCase();d=d||{};if(c.originalEvent){a=
b.event.props.length;for(var f;a;){f=b.event.props[--a];c[f]=c.originalEvent[f]}}this.element.trigger(c,d);return!(b.isFunction(e)&&e.call(this.element[0],c,d)===false||c.isDefaultPrevented())}}})(jQuery);
;/*
 * jQuery UI Position 1.8.2
 *
 * Copyright (c) 2010 AUTHORS.txt (http://jqueryui.com/about)
 * Dual licensed under the MIT (MIT-LICENSE.txt)
 * and GPL (GPL-LICENSE.txt) licenses.
 *
 * http://docs.jquery.com/UI/Position
 */
(function(c){c.ui=c.ui||{};var m=/left|center|right/,n=/top|center|bottom/,p=c.fn.position,q=c.fn.offset;c.fn.position=function(a){if(!a||!a.of)return p.apply(this,arguments);a=c.extend({},a);var b=c(a.of),d=(a.collision||"flip").split(" "),e=a.offset?a.offset.split(" "):[0,0],g,h,i;if(a.of.nodeType===9){g=b.width();h=b.height();i={top:0,left:0}}else if(a.of.scrollTo&&a.of.document){g=b.width();h=b.height();i={top:b.scrollTop(),left:b.scrollLeft()}}else if(a.of.preventDefault){a.at="left top";g=h=
0;i={top:a.of.pageY,left:a.of.pageX}}else{g=b.outerWidth();h=b.outerHeight();i=b.offset()}c.each(["my","at"],function(){var f=(a[this]||"").split(" ");if(f.length===1)f=m.test(f[0])?f.concat(["center"]):n.test(f[0])?["center"].concat(f):["center","center"];f[0]=m.test(f[0])?f[0]:"center";f[1]=n.test(f[1])?f[1]:"center";a[this]=f});if(d.length===1)d[1]=d[0];e[0]=parseInt(e[0],10)||0;if(e.length===1)e[1]=e[0];e[1]=parseInt(e[1],10)||0;if(a.at[0]==="right")i.left+=g;else if(a.at[0]==="center")i.left+=
g/2;if(a.at[1]==="bottom")i.top+=h;else if(a.at[1]==="center")i.top+=h/2;i.left+=e[0];i.top+=e[1];return this.each(function(){var f=c(this),k=f.outerWidth(),l=f.outerHeight(),j=c.extend({},i);if(a.my[0]==="right")j.left-=k;else if(a.my[0]==="center")j.left-=k/2;if(a.my[1]==="bottom")j.top-=l;else if(a.my[1]==="center")j.top-=l/2;j.left=parseInt(j.left);j.top=parseInt(j.top);c.each(["left","top"],function(o,r){c.ui.position[d[o]]&&c.ui.position[d[o]][r](j,{targetWidth:g,targetHeight:h,elemWidth:k,
elemHeight:l,offset:e,my:a.my,at:a.at})});c.fn.bgiframe&&f.bgiframe();f.offset(c.extend(j,{using:a.using}))})};c.ui.position={fit:{left:function(a,b){var d=c(window);b=a.left+b.elemWidth-d.width()-d.scrollLeft();a.left=b>0?a.left-b:Math.max(0,a.left)},top:function(a,b){var d=c(window);b=a.top+b.elemHeight-d.height()-d.scrollTop();a.top=b>0?a.top-b:Math.max(0,a.top)}},flip:{left:function(a,b){if(b.at[0]!=="center"){var d=c(window);d=a.left+b.elemWidth-d.width()-d.scrollLeft();var e=b.my[0]==="left"?
-b.elemWidth:b.my[0]==="right"?b.elemWidth:0,g=-2*b.offset[0];a.left+=a.left<0?e+b.targetWidth+g:d>0?e-b.targetWidth+g:0}},top:function(a,b){if(b.at[1]!=="center"){var d=c(window);d=a.top+b.elemHeight-d.height()-d.scrollTop();var e=b.my[1]==="top"?-b.elemHeight:b.my[1]==="bottom"?b.elemHeight:0,g=b.at[1]==="top"?b.targetHeight:-b.targetHeight,h=-2*b.offset[1];a.top+=a.top<0?e+b.targetHeight+h:d>0?e+g+h:0}}}};if(!c.offset.setOffset){c.offset.setOffset=function(a,b){if(/static/.test(c.curCSS(a,"position")))a.style.position=
"relative";var d=c(a),e=d.offset(),g=parseInt(c.curCSS(a,"top",true),10)||0,h=parseInt(c.curCSS(a,"left",true),10)||0;e={top:b.top-e.top+g,left:b.left-e.left+h};"using"in b?b.using.call(a,e):d.css(e)};c.fn.offset=function(a){var b=this[0];if(!b||!b.ownerDocument)return null;if(a)return this.each(function(){c.offset.setOffset(this,a)});return q.call(this)}}})(jQuery);
;/*
 * jQuery UI Autocomplete 1.8.2
 *
 * Copyright (c) 2010 AUTHORS.txt (http://jqueryui.com/about)
 * Dual licensed under the MIT (MIT-LICENSE.txt)
 * and GPL (GPL-LICENSE.txt) licenses.
 *
 * http://docs.jquery.com/UI/Autocomplete
 *
 * Depends:
 *	jquery.ui.core.js
 *	jquery.ui.widget.js
 *	jquery.ui.position.js
 */
(function(e){e.widget("ui.autocomplete",{options:{minLength:1,delay:300},_create:function(){var a=this,c=this.element[0].ownerDocument;this.element.addClass("ui-autocomplete-input").attr("autocomplete","off").attr({role:"textbox","aria-autocomplete":"list","aria-haspopup":"true"}).bind("keydown.autocomplete",function(d){var b=e.ui.keyCode;switch(d.keyCode){case b.PAGE_UP:a._move("previousPage",d);break;case b.PAGE_DOWN:a._move("nextPage",d);break;case b.UP:a._move("previous",d);d.preventDefault();
break;case b.DOWN:a._move("next",d);d.preventDefault();break;case b.ENTER:case b.NUMPAD_ENTER:a.menu.active&&d.preventDefault();case b.TAB:if(!a.menu.active)return;a.menu.select(d);break;case b.ESCAPE:a.element.val(a.term);a.close(d);break;case b.LEFT:case b.RIGHT:case b.SHIFT:case b.CONTROL:case b.ALT:case b.COMMAND:case b.COMMAND_RIGHT:case b.INSERT:case b.CAPS_LOCK:case b.END:case b.HOME:break;default:clearTimeout(a.searching);a.searching=setTimeout(function(){a.search(null,d)},a.options.delay);
break}}).bind("focus.autocomplete",function(){a.selectedItem=null;a.previous=a.element.val()}).bind("blur.autocomplete",function(d){clearTimeout(a.searching);a.closing=setTimeout(function(){a.close(d);a._change(d)},150)});this._initSource();this.response=function(){return a._response.apply(a,arguments)};this.menu=e("<ul></ul>").addClass("ui-autocomplete").appendTo("body",c).mousedown(function(){setTimeout(function(){clearTimeout(a.closing)},13)}).menu({focus:function(d,b){b=b.item.data("item.autocomplete");
false!==a._trigger("focus",null,{item:b})&&/^key/.test(d.originalEvent.type)&&a.element.val(b.value)},selected:function(d,b){b=b.item.data("item.autocomplete");false!==a._trigger("select",d,{item:b})&&a.element.val(b.value);a.close(d);d=a.previous;if(a.element[0]!==c.activeElement){a.element.focus();a.previous=d}a.selectedItem=b},blur:function(){a.menu.element.is(":visible")&&a.element.val(a.term)}}).zIndex(this.element.zIndex()+1).css({top:0,left:0}).hide().data("menu");e.fn.bgiframe&&this.menu.element.bgiframe()},
destroy:function(){this.element.removeClass("ui-autocomplete-input").removeAttr("autocomplete").removeAttr("role").removeAttr("aria-autocomplete").removeAttr("aria-haspopup");this.menu.element.remove();e.Widget.prototype.destroy.call(this)},_setOption:function(a){e.Widget.prototype._setOption.apply(this,arguments);a==="source"&&this._initSource()},_initSource:function(){var a,c;if(e.isArray(this.options.source)){a=this.options.source;this.source=function(d,b){b(e.ui.autocomplete.filter(a,d.term))}}else if(typeof this.options.source===
"string"){c=this.options.source;this.source=function(d,b){e.getJSON(c,d,b)}}else this.source=this.options.source},search:function(a,c){a=a!=null?a:this.element.val();if(a.length<this.options.minLength)return this.close(c);clearTimeout(this.closing);if(this._trigger("search")!==false)return this._search(a)},_search:function(a){this.term=this.element.addClass("ui-autocomplete-loading").val();this.source({term:a},this.response)},_response:function(a){if(a.length){a=this._normalize(a);this._suggest(a);
this._trigger("open")}else this.close();this.element.removeClass("ui-autocomplete-loading")},close:function(a){clearTimeout(this.closing);if(this.menu.element.is(":visible")){this._trigger("close",a);this.menu.element.hide();this.menu.deactivate()}},_change:function(a){this.previous!==this.element.val()&&this._trigger("change",a,{item:this.selectedItem})},_normalize:function(a){if(a.length&&a[0].label&&a[0].value)return a;return e.map(a,function(c){if(typeof c==="string")return{label:c,value:c};return e.extend({label:c.label||
c.value,value:c.value||c.label},c)})},_suggest:function(a){var c=this.menu.element.empty().zIndex(this.element.zIndex()+1),d;this._renderMenu(c,a);this.menu.deactivate();this.menu.refresh();this.menu.element.show().position({my:"left top",at:"left bottom",of:this.element,collision:"none"});a=c.width("").width();d=this.element.width();c.width(Math.max(a,d))},_renderMenu:function(a,c){var d=this;e.each(c,function(b,f){d._renderItem(a,f)})},_renderItem:function(a,c){return e("<li></li>").data("item.autocomplete",
c).append("<a>"+c.label+"</a>").appendTo(a)},_move:function(a,c){if(this.menu.element.is(":visible"))if(this.menu.first()&&/^previous/.test(a)||this.menu.last()&&/^next/.test(a)){this.element.val(this.term);this.menu.deactivate()}else this.menu[a](c);else this.search(null,c)},widget:function(){return this.menu.element}});e.extend(e.ui.autocomplete,{escapeRegex:function(a){return a.replace(/([\^\$\(\)\[\]\{\}\*\.\+\?\|\\])/gi,"\\$1")},filter:function(a,c){var d=new RegExp(e.ui.autocomplete.escapeRegex(c),
"i");return e.grep(a,function(b){return d.test(b.label||b.value||b)})}})})(jQuery);
(function(e){e.widget("ui.menu",{_create:function(){var a=this;this.element.addClass("ui-menu ui-widget ui-widget-content ui-corner-all").attr({role:"listbox","aria-activedescendant":"ui-active-menuitem"}).click(function(c){if(e(c.target).closest(".ui-menu-item a").length){c.preventDefault();a.select(c)}});this.refresh()},refresh:function(){var a=this;this.element.children("li:not(.ui-menu-item):has(a)").addClass("ui-menu-item").attr("role","menuitem").children("a").addClass("ui-corner-all").attr("tabindex",
-1).mouseenter(function(c){a.activate(c,e(this).parent())}).mouseleave(function(){a.deactivate()})},activate:function(a,c){this.deactivate();if(this.hasScroll()){var d=c.offset().top-this.element.offset().top,b=this.element.attr("scrollTop"),f=this.element.height();if(d<0)this.element.attr("scrollTop",b+d);else d>f&&this.element.attr("scrollTop",b+d-f+c.height())}this.active=c.eq(0).children("a").addClass("ui-state-hover").attr("id","ui-active-menuitem").end();this._trigger("focus",a,{item:c})},deactivate:function(){if(this.active){this.active.children("a").removeClass("ui-state-hover").removeAttr("id");
this._trigger("blur");this.active=null}},next:function(a){this.move("next",".ui-menu-item:first",a)},previous:function(a){this.move("prev",".ui-menu-item:last",a)},first:function(){return this.active&&!this.active.prev().length},last:function(){return this.active&&!this.active.next().length},move:function(a,c,d){if(this.active){a=this.active[a+"All"](".ui-menu-item").eq(0);a.length?this.activate(d,a):this.activate(d,this.element.children(c))}else this.activate(d,this.element.children(c))},nextPage:function(a){if(this.hasScroll())if(!this.active||
this.last())this.activate(a,this.element.children(":first"));else{var c=this.active.offset().top,d=this.element.height(),b=this.element.children("li").filter(function(){var f=e(this).offset().top-c-d+e(this).height();return f<10&&f>-10});b.length||(b=this.element.children(":last"));this.activate(a,b)}else this.activate(a,this.element.children(!this.active||this.last()?":first":":last"))},previousPage:function(a){if(this.hasScroll())if(!this.active||this.first())this.activate(a,this.element.children(":last"));
else{var c=this.active.offset().top,d=this.element.height();result=this.element.children("li").filter(function(){var b=e(this).offset().top-c+d-e(this).height();return b<10&&b>-10});result.length||(result=this.element.children(":first"));this.activate(a,result)}else this.activate(a,this.element.children(!this.active||this.first()?":last":":first"))},hasScroll:function(){return this.element.height()<this.element.attr("scrollHeight")},select:function(a){this._trigger("selected",a,{item:this.active})}})})(jQuery);
;/*
 * jQuery UI Dialog 1.8.2
 *
 * Copyright (c) 2010 AUTHORS.txt (http://jqueryui.com/about)
 * Dual licensed under the MIT (MIT-LICENSE.txt)
 * and GPL (GPL-LICENSE.txt) licenses.
 *
 * http://docs.jquery.com/UI/Dialog
 *
 * Depends:
 *	jquery.ui.core.js
 *	jquery.ui.widget.js
 *  jquery.ui.button.js
 *	jquery.ui.draggable.js
 *	jquery.ui.mouse.js
 *	jquery.ui.position.js
 *	jquery.ui.resizable.js
 */
(function(c){c.widget("ui.dialog",{options:{autoOpen:true,buttons:{},closeOnEscape:true,closeText:"close",dialogClass:"",draggable:true,hide:null,height:"auto",maxHeight:false,maxWidth:false,minHeight:150,minWidth:150,modal:false,position:"center",resizable:true,show:null,stack:true,title:"",width:300,zIndex:1E3},_create:function(){this.originalTitle=this.element.attr("title");var a=this,b=a.options,d=b.title||a.originalTitle||"&#160;",e=c.ui.dialog.getTitleId(a.element),g=(a.uiDialog=c("<div></div>")).appendTo(document.body).hide().addClass("ui-dialog ui-widget ui-widget-content ui-corner-all "+
b.dialogClass).css({zIndex:b.zIndex}).attr("tabIndex",-1).css("outline",0).keydown(function(i){if(b.closeOnEscape&&i.keyCode&&i.keyCode===c.ui.keyCode.ESCAPE){a.close(i);i.preventDefault()}}).attr({role:"dialog","aria-labelledby":e}).mousedown(function(i){a.moveToTop(false,i)});a.element.show().removeAttr("title").addClass("ui-dialog-content ui-widget-content").appendTo(g);var f=(a.uiDialogTitlebar=c("<div></div>")).addClass("ui-dialog-titlebar ui-widget-header ui-corner-all ui-helper-clearfix").prependTo(g),
h=c('<a href="#"></a>').addClass("ui-dialog-titlebar-close ui-corner-all").attr("role","button").hover(function(){h.addClass("ui-state-hover")},function(){h.removeClass("ui-state-hover")}).focus(function(){h.addClass("ui-state-focus")}).blur(function(){h.removeClass("ui-state-focus")}).click(function(i){a.close(i);return false}).appendTo(f);(a.uiDialogTitlebarCloseText=c("<span></span>")).addClass("ui-icon ui-icon-closethick").text(b.closeText).appendTo(h);c("<span></span>").addClass("ui-dialog-title").attr("id",
e).html(d).prependTo(f);if(c.isFunction(b.beforeclose)&&!c.isFunction(b.beforeClose))b.beforeClose=b.beforeclose;f.find("*").add(f).disableSelection();b.draggable&&c.fn.draggable&&a._makeDraggable();b.resizable&&c.fn.resizable&&a._makeResizable();a._createButtons(b.buttons);a._isOpen=false;c.fn.bgiframe&&g.bgiframe()},_init:function(){this.options.autoOpen&&this.open()},destroy:function(){var a=this;a.overlay&&a.overlay.destroy();a.uiDialog.hide();a.element.unbind(".dialog").removeData("dialog").removeClass("ui-dialog-content ui-widget-content").hide().appendTo("body");
a.uiDialog.remove();a.originalTitle&&a.element.attr("title",a.originalTitle);return a},widget:function(){return this.uiDialog},close:function(a){var b=this,d;if(false!==b._trigger("beforeClose",a)){b.overlay&&b.overlay.destroy();b.uiDialog.unbind("keypress.ui-dialog");b._isOpen=false;if(b.options.hide)b.uiDialog.hide(b.options.hide,function(){b._trigger("close",a)});else{b.uiDialog.hide();b._trigger("close",a)}c.ui.dialog.overlay.resize();if(b.options.modal){d=0;c(".ui-dialog").each(function(){if(this!==
b.uiDialog[0])d=Math.max(d,c(this).css("z-index"))});c.ui.dialog.maxZ=d}return b}},isOpen:function(){return this._isOpen},moveToTop:function(a,b){var d=this,e=d.options;if(e.modal&&!a||!e.stack&&!e.modal)return d._trigger("focus",b);if(e.zIndex>c.ui.dialog.maxZ)c.ui.dialog.maxZ=e.zIndex;if(d.overlay){c.ui.dialog.maxZ+=1;d.overlay.$el.css("z-index",c.ui.dialog.overlay.maxZ=c.ui.dialog.maxZ)}a={scrollTop:d.element.attr("scrollTop"),scrollLeft:d.element.attr("scrollLeft")};c.ui.dialog.maxZ+=1;d.uiDialog.css("z-index",
c.ui.dialog.maxZ);d.element.attr(a);d._trigger("focus",b);return d},open:function(){if(!this._isOpen){var a=this,b=a.options,d=a.uiDialog;a.overlay=b.modal?new c.ui.dialog.overlay(a):null;d.next().length&&d.appendTo("body");a._size();a._position(b.position);d.show(b.show);a.moveToTop(true);b.modal&&d.bind("keypress.ui-dialog",function(e){if(e.keyCode===c.ui.keyCode.TAB){var g=c(":tabbable",this),f=g.filter(":first");g=g.filter(":last");if(e.target===g[0]&&!e.shiftKey){f.focus(1);return false}else if(e.target===
f[0]&&e.shiftKey){g.focus(1);return false}}});c([]).add(d.find(".ui-dialog-content :tabbable:first")).add(d.find(".ui-dialog-buttonpane :tabbable:first")).add(d).filter(":first").focus();a._trigger("open");a._isOpen=true;return a}},_createButtons:function(a){var b=this,d=false,e=c("<div></div>").addClass("ui-dialog-buttonpane ui-widget-content ui-helper-clearfix");b.uiDialog.find(".ui-dialog-buttonpane").remove();typeof a==="object"&&a!==null&&c.each(a,function(){return!(d=true)});if(d){c.each(a,
function(g,f){g=c('<button type="button"></button>').text(g).click(function(){f.apply(b.element[0],arguments)}).appendTo(e);c.fn.button&&g.button()});e.appendTo(b.uiDialog)}},_makeDraggable:function(){function a(f){return{position:f.position,offset:f.offset}}var b=this,d=b.options,e=c(document),g;b.uiDialog.draggable({cancel:".ui-dialog-content, .ui-dialog-titlebar-close",handle:".ui-dialog-titlebar",containment:"document",start:function(f,h){g=d.height==="auto"?"auto":c(this).height();c(this).height(c(this).height()).addClass("ui-dialog-dragging");
b._trigger("dragStart",f,a(h))},drag:function(f,h){b._trigger("drag",f,a(h))},stop:function(f,h){d.position=[h.position.left-e.scrollLeft(),h.position.top-e.scrollTop()];c(this).removeClass("ui-dialog-dragging").height(g);b._trigger("dragStop",f,a(h));c.ui.dialog.overlay.resize()}})},_makeResizable:function(a){function b(f){return{originalPosition:f.originalPosition,originalSize:f.originalSize,position:f.position,size:f.size}}a=a===undefined?this.options.resizable:a;var d=this,e=d.options,g=d.uiDialog.css("position");
a=typeof a==="string"?a:"n,e,s,w,se,sw,ne,nw";d.uiDialog.resizable({cancel:".ui-dialog-content",containment:"document",alsoResize:d.element,maxWidth:e.maxWidth,maxHeight:e.maxHeight,minWidth:e.minWidth,minHeight:d._minHeight(),handles:a,start:function(f,h){c(this).addClass("ui-dialog-resizing");d._trigger("resizeStart",f,b(h))},resize:function(f,h){d._trigger("resize",f,b(h))},stop:function(f,h){c(this).removeClass("ui-dialog-resizing");e.height=c(this).height();e.width=c(this).width();d._trigger("resizeStop",
f,b(h));c.ui.dialog.overlay.resize()}}).css("position",g).find(".ui-resizable-se").addClass("ui-icon ui-icon-grip-diagonal-se")},_minHeight:function(){var a=this.options;return a.height==="auto"?a.minHeight:Math.min(a.minHeight,a.height)},_position:function(a){var b=[],d=[0,0];a=a||c.ui.dialog.prototype.options.position;if(typeof a==="string"||typeof a==="object"&&"0"in a){b=a.split?a.split(" "):[a[0],a[1]];if(b.length===1)b[1]=b[0];c.each(["left","top"],function(e,g){if(+b[e]===b[e]){d[e]=b[e];b[e]=
g}})}else if(typeof a==="object"){if("left"in a){b[0]="left";d[0]=a.left}else if("right"in a){b[0]="right";d[0]=-a.right}if("top"in a){b[1]="top";d[1]=a.top}else if("bottom"in a){b[1]="bottom";d[1]=-a.bottom}}(a=this.uiDialog.is(":visible"))||this.uiDialog.show();this.uiDialog.css({top:0,left:0}).position({my:b.join(" "),at:b.join(" "),offset:d.join(" "),of:window,collision:"fit",using:function(e){var g=c(this).css(e).offset().top;g<0&&c(this).css("top",e.top-g)}});a||this.uiDialog.hide()},_setOption:function(a,
b){var d=this,e=d.uiDialog,g=e.is(":data(resizable)"),f=false;switch(a){case "beforeclose":a="beforeClose";break;case "buttons":d._createButtons(b);break;case "closeText":d.uiDialogTitlebarCloseText.text(""+b);break;case "dialogClass":e.removeClass(d.options.dialogClass).addClass("ui-dialog ui-widget ui-widget-content ui-corner-all "+b);break;case "disabled":b?e.addClass("ui-dialog-disabled"):e.removeClass("ui-dialog-disabled");break;case "draggable":b?d._makeDraggable():e.draggable("destroy");break;
case "height":f=true;break;case "maxHeight":g&&e.resizable("option","maxHeight",b);f=true;break;case "maxWidth":g&&e.resizable("option","maxWidth",b);f=true;break;case "minHeight":g&&e.resizable("option","minHeight",b);f=true;break;case "minWidth":g&&e.resizable("option","minWidth",b);f=true;break;case "position":d._position(b);break;case "resizable":g&&!b&&e.resizable("destroy");g&&typeof b==="string"&&e.resizable("option","handles",b);!g&&b!==false&&d._makeResizable(b);break;case "title":c(".ui-dialog-title",
d.uiDialogTitlebar).html(""+(b||"&#160;"));break;case "width":f=true;break}c.Widget.prototype._setOption.apply(d,arguments);f&&d._size()},_size:function(){var a=this.options,b;this.element.css({width:"auto",minHeight:0,height:0});b=this.uiDialog.css({height:"auto",width:a.width}).height();this.element.css(a.height==="auto"?{minHeight:Math.max(a.minHeight-b,0),height:"auto"}:{minHeight:0,height:Math.max(a.height-b,0)}).show();this.uiDialog.is(":data(resizable)")&&this.uiDialog.resizable("option","minHeight",
this._minHeight())}});c.extend(c.ui.dialog,{version:"1.8.2",uuid:0,maxZ:0,getTitleId:function(a){a=a.attr("id");if(!a){this.uuid+=1;a=this.uuid}return"ui-dialog-title-"+a},overlay:function(a){this.$el=c.ui.dialog.overlay.create(a)}});c.extend(c.ui.dialog.overlay,{instances:[],oldInstances:[],maxZ:0,events:c.map("focus,mousedown,mouseup,keydown,keypress,click".split(","),function(a){return a+".dialog-overlay"}).join(" "),create:function(a){if(this.instances.length===0){setTimeout(function(){c.ui.dialog.overlay.instances.length&&
c(document).bind(c.ui.dialog.overlay.events,function(d){return c(d.target).zIndex()>=c.ui.dialog.overlay.maxZ})},1);c(document).bind("keydown.dialog-overlay",function(d){if(a.options.closeOnEscape&&d.keyCode&&d.keyCode===c.ui.keyCode.ESCAPE){a.close(d);d.preventDefault()}});c(window).bind("resize.dialog-overlay",c.ui.dialog.overlay.resize)}var b=(this.oldInstances.pop()||c("<div></div>").addClass("ui-widget-overlay")).appendTo(document.body).css({width:this.width(),height:this.height()});c.fn.bgiframe&&
b.bgiframe();this.instances.push(b);return b},destroy:function(a){this.oldInstances.push(this.instances.splice(c.inArray(a,this.instances),1)[0]);this.instances.length===0&&c([document,window]).unbind(".dialog-overlay");a.remove();var b=0;c.each(this.instances,function(){b=Math.max(b,this.css("z-index"))});this.maxZ=b},height:function(){var a,b;if(c.browser.msie&&c.browser.version<7){a=Math.max(document.documentElement.scrollHeight,document.body.scrollHeight);b=Math.max(document.documentElement.offsetHeight,
document.body.offsetHeight);return a<b?c(window).height()+"px":a+"px"}else return c(document).height()+"px"},width:function(){var a,b;if(c.browser.msie&&c.browser.version<7){a=Math.max(document.documentElement.scrollWidth,document.body.scrollWidth);b=Math.max(document.documentElement.offsetWidth,document.body.offsetWidth);return a<b?c(window).width()+"px":a+"px"}else return c(document).width()+"px"},resize:function(){var a=c([]);c.each(c.ui.dialog.overlay.instances,function(){a=a.add(this)});a.css({width:0,
height:0}).css({width:c.ui.dialog.overlay.width(),height:c.ui.dialog.overlay.height()})}});c.extend(c.ui.dialog.overlay.prototype,{destroy:function(){c.ui.dialog.overlay.destroy(this.$el)}})})(jQuery);
;

/* http://keith-wood.name/datepick.html
   Date picker for jQuery v4.0.2.
   Written by Keith Wood (kbwood{at}iinet.com.au) February 2010.
   Dual licensed under the GPL (http://dev.jquery.com/browser/trunk/jquery/GPL-LICENSE.txt) and 
   MIT (http://dev.jquery.com/browser/trunk/jquery/MIT-LICENSE.txt) licenses. 
   Please attribute the author if you use it. */
(function($){function Datepicker(){this._defaults={pickerClass:'',showOnFocus:true,showTrigger:null,showAnim:'show',showOptions:{},showSpeed:'normal',popupContainer:null,alignment:'bottom',fixedWeeks:false,firstDay:0,calculateWeek:this.iso8601Week,monthsToShow:1,monthsOffset:0,monthsToStep:1,monthsToJump:12,changeMonth:true,yearRange:'c-10:c+10',shortYearCutoff:'+10',showOtherMonths:false,selectOtherMonths:false,defaultDate:null,selectDefaultDate:false,minDate:null,maxDate:null,dateFormat:'mm/dd/yyyy',autoSize:false,rangeSelect:false,rangeSeparator:' - ',multiSelect:0,multiSeparator:',',onDate:null,onShow:null,onChangeMonthYear:null,onSelect:null,onClose:null,altField:null,altFormat:null,constrainInput:true,commandsAsDateFormat:false,commands:this.commands};this.regional={'':{monthNames:['January','February','March','April','May','June','July','August','September','October','November','December'],monthNamesShort:['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'],dayNames:['Sunday','Monday','Tuesday','Wednesday','Thursday','Friday','Saturday'],dayNamesShort:['Sun','Mon','Tue','Wed','Thu','Fri','Sat'],dayNamesMin:['Su','Mo','Tu','We','Th','Fr','Sa'],dateFormat:'mm/dd/yyyy',firstDay:0,renderer:this.defaultRenderer,prevText:'&lt;Prev',prevStatus:'Show the previous month',prevJumpText:'&lt;&lt;',prevJumpStatus:'Show the previous year',nextText:'Next&gt;',nextStatus:'Show the next month',nextJumpText:'&gt;&gt;',nextJumpStatus:'Show the next year',currentText:'Current',currentStatus:'Show the current month',todayText:'Today',todayStatus:'Show today\'s month',clearText:'Clear',clearStatus:'Clear all the dates',closeText:'Close',closeStatus:'Close the datepicker',yearStatus:'Change the year',monthStatus:'Change the month',weekText:'Wk',weekStatus:'Week of the year',dayStatus:'Select DD, M d, yyyy',defaultStatus:'Select a date',isRTL:false}};$.extend(this._defaults,this.regional['']);this._disabled=[]}$.extend(Datepicker.prototype,{dataName:'datepick',markerClass:'hasDatepick',_popupClass:'datepick-popup',_triggerClass:'datepick-trigger',_disableClass:'datepick-disable',_coverClass:'datepick-cover',_monthYearClass:'datepick-month-year',_curMonthClass:'datepick-month-',_anyYearClass:'datepick-any-year',_curDoWClass:'datepick-dow-',commands:{prev:{text:'prevText',status:'prevStatus',keystroke:{keyCode:33},enabled:function(a){var b=a.curMinDate();return(!b||$.datepick.add($.datepick.day($.datepick.add($.datepick.newDate(a.drawDate),1-a.get('monthsToStep')-a.get('monthsOffset'),'m'),1),-1,'d').getTime()>=b.getTime())},date:function(a){return $.datepick.day($.datepick.add($.datepick.newDate(a.drawDate),-a.get('monthsToStep')-a.get('monthsOffset'),'m'),1)},action:function(a){$.datepick.changeMonth(this,-a.get('monthsToStep'))}},prevJump:{text:'prevJumpText',status:'prevJumpStatus',keystroke:{keyCode:33,ctrlKey:true},enabled:function(a){var b=a.curMinDate();return(!b||$.datepick.add($.datepick.day($.datepick.add($.datepick.newDate(a.drawDate),1-a.get('monthsToJump')-a.get('monthsOffset'),'m'),1),-1,'d').getTime()>=b.getTime())},date:function(a){return $.datepick.day($.datepick.add($.datepick.newDate(a.drawDate),-a.get('monthsToJump')-a.get('monthsOffset'),'m'),1)},action:function(a){$.datepick.changeMonth(this,-a.get('monthsToJump'))}},next:{text:'nextText',status:'nextStatus',keystroke:{keyCode:34},enabled:function(a){var b=a.get('maxDate');return(!b||$.datepick.day($.datepick.add($.datepick.newDate(a.drawDate),a.get('monthsToStep')-a.get('monthsOffset'),'m'),1).getTime()<=b.getTime())},date:function(a){return $.datepick.day($.datepick.add($.datepick.newDate(a.drawDate),a.get('monthsToStep')-a.get('monthsOffset'),'m'),1)},action:function(a){$.datepick.changeMonth(this,a.get('monthsToStep'))}},nextJump:{text:'nextJumpText',status:'nextJumpStatus',keystroke:{keyCode:34,ctrlKey:true},enabled:function(a){var b=a.get('maxDate');return(!b||$.datepick.day($.datepick.add($.datepick.newDate(a.drawDate),a.get('monthsToJump')-a.get('monthsOffset'),'m'),1).getTime()<=b.getTime())},date:function(a){return $.datepick.day($.datepick.add($.datepick.newDate(a.drawDate),a.get('monthsToJump')-a.get('monthsOffset'),'m'),1)},action:function(a){$.datepick.changeMonth(this,a.get('monthsToJump'))}},current:{text:'currentText',status:'currentStatus',keystroke:{keyCode:36,ctrlKey:true},enabled:function(a){var b=a.curMinDate();var c=a.get('maxDate');var d=a.selectedDates[0]||$.datepick.today();return(!b||d.getTime()>=b.getTime())&&(!c||d.getTime()<=c.getTime())},date:function(a){return a.selectedDates[0]||$.datepick.today()},action:function(a){var b=a.selectedDates[0]||$.datepick.today();$.datepick.showMonth(this,b.getFullYear(),b.getMonth()+1)}},today:{text:'todayText',status:'todayStatus',keystroke:{keyCode:36,ctrlKey:true},enabled:function(a){var b=a.curMinDate();var c=a.get('maxDate');return(!b||$.datepick.today().getTime()>=b.getTime())&&(!c||$.datepick.today().getTime()<=c.getTime())},date:function(a){return $.datepick.today()},action:function(a){$.datepick.showMonth(this)}},clear:{text:'clearText',status:'clearStatus',keystroke:{keyCode:35,ctrlKey:true},enabled:function(a){return true},date:function(a){return null},action:function(a){$.datepick.clear(this)}},close:{text:'closeText',status:'closeStatus',keystroke:{keyCode:27},enabled:function(a){return true},date:function(a){return null},action:function(a){$.datepick.hide(this)}},prevWeek:{text:'prevWeekText',status:'prevWeekStatus',keystroke:{keyCode:38,ctrlKey:true},enabled:function(a){var b=a.curMinDate();return(!b||$.datepick.add($.datepick.newDate(a.drawDate),-7,'d').getTime()>=b.getTime())},date:function(a){return $.datepick.add($.datepick.newDate(a.drawDate),-7,'d')},action:function(a){$.datepick.changeDay(this,-7)}},prevDay:{text:'prevDayText',status:'prevDayStatus',keystroke:{keyCode:37,ctrlKey:true},enabled:function(a){var b=a.curMinDate();return(!b||$.datepick.add($.datepick.newDate(a.drawDate),-1,'d').getTime()>=b.getTime())},date:function(a){return $.datepick.add($.datepick.newDate(a.drawDate),-1,'d')},action:function(a){$.datepick.changeDay(this,-1)}},nextDay:{text:'nextDayText',status:'nextDayStatus',keystroke:{keyCode:39,ctrlKey:true},enabled:function(a){var b=a.get('maxDate');return(!b||$.datepick.add($.datepick.newDate(a.drawDate),1,'d').getTime()<=b.getTime())},date:function(a){return $.datepick.add($.datepick.newDate(a.drawDate),1,'d')},action:function(a){$.datepick.changeDay(this,1)}},nextWeek:{text:'nextWeekText',status:'nextWeekStatus',keystroke:{keyCode:40,ctrlKey:true},enabled:function(a){var b=a.get('maxDate');return(!b||$.datepick.add($.datepick.newDate(a.drawDate),7,'d').getTime()<=b.getTime())},date:function(a){return $.datepick.add($.datepick.newDate(a.drawDate),7,'d')},action:function(a){$.datepick.changeDay(this,7)}}},defaultRenderer:{picker:'<div class="datepick">'+'<div class="datepick-nav">{link:prev}{link:today}{link:next}</div>{months}'+'{popup:start}<div class="datepick-ctrl">{link:clear}{link:close}</div>{popup:end}'+'<div class="datepick-clear-fix"></div></div>',monthRow:'<div class="datepick-month-row">{months}</div>',month:'<div class="datepick-month"><div class="datepick-month-header">{monthHeader}</div>'+'<table><thead>{weekHeader}</thead><tbody>{weeks}</tbody></table></div>',weekHeader:'<tr>{days}</tr>',dayHeader:'<th>{day}</th>',week:'<tr>{days}</tr>',day:'<td>{day}</td>',monthSelector:'.datepick-month',daySelector:'td',rtlClass:'datepick-rtl',multiClass:'datepick-multi',defaultClass:'',selectedClass:'datepick-selected',highlightedClass:'datepick-highlight',todayClass:'datepick-today',otherMonthClass:'datepick-other-month',weekendClass:'datepick-weekend',commandClass:'datepick-cmd',commandButtonClass:'',commandLinkClass:'',disabledClass:'datepick-disabled'},setDefaults:function(a){$.extend(this._defaults,a||{});return this},_ticksTo1970:(((1970-1)*365+Math.floor(1970/4)-Math.floor(1970/100)+Math.floor(1970/400))*24*60*60*10000000),_msPerDay:24*60*60*1000,ATOM:'yyyy-mm-dd',COOKIE:'D, dd M yyyy',FULL:'DD, MM d, yyyy',ISO_8601:'yyyy-mm-dd',JULIAN:'J',RFC_822:'D, d M yy',RFC_850:'DD, dd-M-yy',RFC_1036:'D, d M yy',RFC_1123:'D, d M yyyy',RFC_2822:'D, d M yyyy',RSS:'D, d M yy',TICKS:'!',TIMESTAMP:'@',W3C:'yyyy-mm-dd',formatDate:function(f,g,h){if(typeof f!='string'){h=g;g=f;f=''}if(!g){return''}f=f||this._defaults.dateFormat;h=h||{};var i=h.dayNamesShort||this._defaults.dayNamesShort;var j=h.dayNames||this._defaults.dayNames;var k=h.monthNamesShort||this._defaults.monthNamesShort;var l=h.monthNames||this._defaults.monthNames;var m=h.calculateWeek||this._defaults.calculateWeek;var n=function(a,b){var c=1;while(s+c<f.length&&f.charAt(s+c)==a){c++}s+=c-1;return Math.floor(c/(b||1))>1};var o=function(a,b,c,d){var e=''+b;if(n(a,d)){while(e.length<c){e='0'+e}}return e};var p=function(a,b,c,d){return(n(a)?d[b]:c[b])};var q='';var r=false;for(var s=0;s<f.length;s++){if(r){if(f.charAt(s)=="'"&&!n("'")){r=false}else{q+=f.charAt(s)}}else{switch(f.charAt(s)){case'd':q+=o('d',g.getDate(),2);break;case'D':q+=p('D',g.getDay(),i,j);break;case'o':q+=o('o',this.dayOfYear(g),3);break;case'w':q+=o('w',m(g),2);break;case'm':q+=o('m',g.getMonth()+1,2);break;case'M':q+=p('M',g.getMonth(),k,l);break;case'y':q+=(n('y',2)?g.getFullYear():(g.getFullYear()%100<10?'0':'')+g.getFullYear()%100);break;case'@':q+=Math.floor(g.getTime()/1000);break;case'!':q+=g.getTime()*10000+this._ticksTo1970;break;case"'":if(n("'")){q+="'"}else{r=true}break;default:q+=f.charAt(s)}}}return q},parseDate:function(f,g,h){if(g==null){throw'Invalid arguments';}g=(typeof g=='object'?g.toString():g+'');if(g==''){return null}f=f||this._defaults.dateFormat;h=h||{};var j=h.shortYearCutoff||this._defaults.shortYearCutoff;j=(typeof j!='string'?j:this.today().getFullYear()%100+parseInt(j,10));var k=h.dayNamesShort||this._defaults.dayNamesShort;var l=h.dayNames||this._defaults.dayNames;var m=h.monthNamesShort||this._defaults.monthNamesShort;var n=h.monthNames||this._defaults.monthNames;var o=-1;var p=-1;var q=-1;var r=-1;var s=false;var t=false;var u=function(a,b){var c=1;while(z+c<f.length&&f.charAt(z+c)==a){c++}z+=c-1;return Math.floor(c/(b||1))>1};var v=function(a,b){u(a,b);var c=[2,3,4,11,20]['oy@!'.indexOf(a)+1];var d=new RegExp('^-?\\d{1,'+c+'}');var e=g.substring(y).match(d);if(!e){throw'Missing number at position {0}'.replace(/\{0\}/,y);}y+=e[0].length;return parseInt(e[0],10);};var w=function(a,b,c,d){var e=(u(a,d)?c:b);for(var i=0;i<e.length;i++){if(g.substr(y,e[i].length)==e[i]){y+=e[i].length;return i+1;}}throw'Unknown name at position {0}'.replace(/\{0\}/,y);};var x=function(){if(g.charAt(y)!=f.charAt(z)){throw'Unexpected literal at position {0}'.replace(/\{0\}/,y);}y++;};var y=0;for(var z=0;z<f.length;z++){if(t){if(f.charAt(z)=="'"&&!u("'")){t=false;}else{x();}}else{switch(f.charAt(z)){case'd':q=v('d');break;case'D':w('D',k,l);break;case'o':r=v('o');break;case'w':v('w');break;case'm':p=v('m');break;case'M':p=w('M',m,n);break;case'y':var A=z;s=!u('y',2);z=A;o=v('y',2);break;case'@':var B=this._normaliseDate(new Date(v('@')*1000));o=B.getFullYear();p=B.getMonth()+1;q=B.getDate();break;case'!':var B=this._normaliseDate(new Date((v('!')-this._ticksTo1970)/10000));o=B.getFullYear();p=B.getMonth()+1;q=B.getDate();break;case'*':y=g.length;break;case"'":if(u("'")){x();}else{t=true;}break;default:x();}}}if(y<g.length){throw'Additional text found at end';}if(o==-1){o=this.today().getFullYear();}else if(o<100&&s){o+=(j==-1?1900:this.today().getFullYear()-this.today().getFullYear()%100-(o<=j?0:100));}if(r>-1){p=1;q=r;for(var C=this.daysInMonth(o,p);q>C;C=this.daysInMonth(o,p)){p++;q-=C;}}var B=this.newDate(o,p,q);if(B.getFullYear()!=o||B.getMonth()+1!=p||B.getDate()!=q){throw'Invalid date';}return B;},determineDate:function(f,g,h,i,j){if(h&&typeof h!='object'){j=i;i=h;h=null;}if(typeof i!='string'){j=i;i='';}var k=function(a){try{return $.datepick.parseDate(i,a,j);}catch(e){}a=a.toLowerCase();var b=(a.match(/^c/)&&h?$.datepick.newDate(h):null)||$.datepick.today();var c=/([+-]?[0-9]+)\s*(d|w|m|y)?/g;var d=c.exec(a);while(d){b=$.datepick.add(b,parseInt(d[1],10),d[2]||'d');d=c.exec(a);}return b;};g=(g?$.datepick.newDate(g):null);f=(f==null?g:(typeof f=='string'?k(f):(typeof f=='number'?(isNaN(f)||f==Infinity||f==-Infinity?g:$.datepick.add($.datepick.today(),f,'d')):$.datepick._normaliseDate(f))));return f;},daysInMonth:function(a,b){var c=(a.getFullYear?a:this.newDate(a,b,1));return 32-this.newDate(c.getFullYear(),c.getMonth()+1,32).getDate();},dayOfYear:function(a,b,c){var d=(a.getFullYear?a:this.newDate(a,b,c));var e=this.newDate(d.getFullYear(),1,1);return(d.getTime()-e.getTime())/this._msPerDay+1;},iso8601Week:function(a,b,c){var d=(a.getFullYear?new Date(a.getTime()):this.newDate(a,b,c));d.setDate(d.getDate()+4-(d.getDay()||7));var e=d.getTime();d.setMonth(0);d.setDate(1);return Math.floor(Math.round((e-d)/86400000)/7)+1;},today:function(){return this._normaliseDate(new Date());},newDate:function(a,b,c){return(!a?null:this._normaliseDate(a.getFullYear?new Date(a.getTime()):new Date(a,b-1,c)));},_normaliseDate:function(a){if(!a){return a;}a.setHours(0);a.setMinutes(0);a.setSeconds(0);a.setMilliseconds(0);a.setHours(a.getHours()>12?a.getHours()+2:0);return a;},year:function(a,b){a.setFullYear(b);return this._normaliseDate(a);},month:function(a,b){a.setMonth(b-1);return this._normaliseDate(a);},day:function(a,b){a.setDate(b);return this._normaliseDate(a);},add:function(a,b,c){if(c=='d'||c=='w'){a.setDate(a.getDate()+b*(c=='w'?7:1));}else{var d=a.getFullYear()+(c=='y'?b:0);var e=a.getMonth()+(c=='m'?b:0);a.setTime(this._normaliseDate(new Date(d,e,Math.min(a.getDate(),this.daysInMonth(d,e+1)))).getTime());}return a;},_attachPicker:function(c,d){c=$(c);if(c.hasClass(this.markerClass)){return;}c.addClass(this.markerClass);var e={target:c,selectedDates:[],drawDate:null,pickingRange:false,inline:($.inArray(c[0].nodeName.toLowerCase(),['div','span'])>-1),get:function(a){var b=this.settings[a]!==undefined?this.settings[a]:$.datepick._defaults[a];if($.inArray(a,['defaultDate','minDate','maxDate'])>-1){b=$.datepick.determineDate(b,null,this.selectedDates[0],this.get('dateFormat'),e.getConfig());}return b;},curMinDate:function(){return(this.pickingRange?this.selectedDates[0]:this.get('minDate'));},getConfig:function(){return{dayNamesShort:this.get('dayNamesShort'),dayNames:this.get('dayNames'),monthNamesShort:this.get('monthNamesShort'),monthNames:this.get('monthNames'),calculateWeek:this.get('calculateWeek'),shortYearCutoff:this.get('shortYearCutoff')};}};$.data(c[0],this.dataName,e);var f=($.fn.metadata?c.metadata():{});e.settings=$.extend({},d||{},f||{});if(e.inline){this._update(c[0]);}else{this._attachments(c,e);c.bind('keydown.'+this.dataName,this._keyDown).bind('keypress.'+this.dataName,this._keyPress).bind('keyup.'+this.dataName,this._keyUp);if(c.attr('disabled')){this.disable(c[0]);}}},options:function(a,b){var c=$.data(a,this.dataName);return(c?(b?(b=='all'?c.settings:c.settings[b]):$.datepick._defaults):{});},option:function(a,b,c){a=$(a);if(!a.hasClass(this.markerClass)){return;}b=b||{};if(typeof b=='string'){var d=b;b={};b[d]=c;}var e=$.data(a[0],this.dataName);var f=e.selectedDates;extendRemove(e.settings,b);this.setDate(a[0],f,null,false,true);e.pickingRange=false;e.drawDate=$.datepick.newDate(this._checkMinMax((b.defaultDate?e.get('defaultDate'):e.drawDate)||e.get('defaultDate')||$.datepick.today(),e));if(!e.inline){this._attachments(a,e);}if(e.inline||e.div){this._update(a[0]);}},_attachments:function(a,b){a.unbind('focus.'+this.dataName);if(b.get('showOnFocus')){a.bind('focus.'+this.dataName,this.show);}if(b.trigger){b.trigger.remove();}var c=b.get('showTrigger');b.trigger=(!c?$([]):$(c).clone().removeAttr('id').addClass(this._triggerClass)[b.get('isRTL')?'insertBefore':'insertAfter'](a).click(function(){if(!$.datepick.isDisabled(a[0])){$.datepick[$.datepick.curInst==b?'hide':'show'](a[0]);}}));this._autoSize(a,b);if(b.get('selectDefaultDate')&&b.get('defaultDate')&&b.selectedDates.length==0){this.setDate(a[0],$.datepick.newDate(b.get('defaultDate')||$.datepick.today()));}},_autoSize:function(d,e){if(e.get('autoSize')&&!e.inline){var f=new Date(2009,10-1,20);var g=e.get('dateFormat');if(g.match(/[DM]/)){var h=function(a){var b=0;var c=0;for(var i=0;i<a.length;i++){if(a[i].length>b){b=a[i].length;c=i;}}return c;};f.setMonth(h(e.get(g.match(/MM/)?'monthNames':'monthNamesShort')));f.setDate(h(e.get(g.match(/DD/)?'dayNames':'dayNamesShort'))+20-f.getDay());}e.target.attr('size',$.datepick.formatDate(g,f,e.getConfig()).length);}},destroy:function(a){a=$(a);if(!a.hasClass(this.markerClass)){return;}var b=$.data(a[0],this.dataName);if(b.trigger){b.trigger.remove();}a.removeClass(this.markerClass).empty().unbind('.'+this.dataName);if(b.get('autoSize')&&!b.inline){a.removeAttr('size');}$.removeData(a[0],this.dataName);},multipleEvents:function(b){var c=arguments;return function(a){for(var i=0;i<c.length;i++){c[i].apply(this,arguments);}};},enable:function(b){var c=$(b);if(!c.hasClass(this.markerClass)){return;}var d=$.data(b,this.dataName);if(d.inline)c.children('.'+this._disableClass).remove().end().find('button,select').attr('disabled','').end().find('a').attr('href','javascript:void(0)');else{b.disabled=false;d.trigger.filter('button.'+this._triggerClass).attr('disabled','').end().filter('img.'+this._triggerClass).css({opacity:'1.0',cursor:''});}this._disabled=$.map(this._disabled,function(a){return(a==b?null:a);});},disable:function(b){var c=$(b);if(!c.hasClass(this.markerClass))return;var d=$.data(b,this.dataName);if(d.inline){var e=c.children(':last');var f=e.offset();var g={left:0,top:0};e.parents().each(function(){if($(this).css('position')=='relative'){g=$(this).offset();return false;}});var h=c.css('zIndex');h=(h=='auto'?0:parseInt(h,10))+1;c.prepend('<div class="'+this._disableClass+'" style="'+'width: '+e.outerWidth()+'px; height: '+e.outerHeight()+'px; left: '+(f.left-g.left)+'px; top: '+(f.top-g.top)+'px; z-index: '+h+'"></div>').find('button,select').attr('disabled','disabled').end().find('a').removeAttr('href');}else{b.disabled=true;d.trigger.filter('button.'+this._triggerClass).attr('disabled','disabled').end().filter('img.'+this._triggerClass).css({opacity:'0.5',cursor:'default'});}this._disabled=$.map(this._disabled,function(a){return(a==b?null:a);});this._disabled.push(b);},isDisabled:function(a){return(a&&$.inArray(a,this._disabled)>-1);},show:function(b){b=b.target||b;var c=$.data(b,$.datepick.dataName);if($.datepick.curInst==c){return;}if($.datepick.curInst){$.datepick.hide($.datepick.curInst,true);}if(c){c.lastVal=null;c.selectedDates=$.datepick._extractDates(c,$(b).val());c.pickingRange=false;c.drawDate=$.datepick._checkMinMax($.datepick.newDate(c.selectedDates[0]||c.get('defaultDate')||$.datepick.today()),c);c.prevDate=$.datepick.newDate(c.drawDate);$.datepick.curInst=c;$.datepick._update(b,true);var d=$.datepick._checkOffset(c);c.div.css({left:d.left,top:d.top});var e=c.get('showAnim');var f=c.get('showSpeed');f=(f=='normal'&&$.ui&&$.ui.version>='1.8'?'_default':f);var g=function(){var a=$.datepick._getBorders(c.div);c.div.find('.'+$.datepick._coverClass).css({left:-a[0],top:-a[1],width:c.div.outerWidth()+a[0],height:c.div.outerHeight()+a[1]});};if($.effects&&$.effects[e]){c.div.show(e,c.get('showOptions'),f,g);}else{c.div[e||'show']((e?f:''),g);}if(!e){g();}}},_extractDates:function(a,b){if(b==a.lastVal){return;}a.lastVal=b;var c=a.get('dateFormat');var d=a.get('multiSelect');var f=a.get('rangeSelect');b=b.split(d?a.get('multiSeparator'):(f?a.get('rangeSeparator'):'\x00'));var g=[];for(var i=0;i<b.length;i++){try{var h=$.datepick.parseDate(c,b[i],a.getConfig());if(h){var k=false;for(var j=0;j<g.length;j++){if(g[j].getTime()==h.getTime()){k=true;break;}}if(!k){g.push(h);}}}catch(e){}}g.splice(d||(f?2:1),g.length);if(f&&g.length==1){g[1]=g[0];}return g;},_update:function(a,b){a=$(a.target||a);var c=$.data(a[0],$.datepick.dataName);if(c){if(c.inline){a.html(this._generateContent(a[0],c));}else if($.datepick.curInst==c){if(!c.div){c.div=$('<div></div>').addClass(this._popupClass).css({display:(b?'none':'static'),position:'absolute',left:a.offset().left,top:a.offset().top+a.outerHeight()}).appendTo($(c.get('popupContainer')||'body'));}c.div.html(this._generateContent(a[0],c));a.focus();}if(c.inline||$.datepick.curInst==c){var d=c.get('onChangeMonthYear');if(d&&(!c.prevDate||c.prevDate.getFullYear()!=c.drawDate.getFullYear()||c.prevDate.getMonth()!=c.drawDate.getMonth())){d.apply(a[0],[c.drawDate.getFullYear(),c.drawDate.getMonth()+1]);}}}},_updateInput:function(a,b){var c=$.data(a,this.dataName);if(c){var d='';var e='';var f=(c.get('multiSelect')?c.get('multiSeparator'):c.get('rangeSeparator'));var g=c.get('dateFormat');var h=c.get('altFormat')||g;for(var i=0;i<c.selectedDates.length;i++){d+=(b?'':(i>0?f:'')+$.datepick.formatDate(g,c.selectedDates[i],c.getConfig()));e+=(i>0?f:'')+$.datepick.formatDate(h,c.selectedDates[i],c.getConfig());}if(!c.inline&&!b){$(a).val(d);}$(c.get('altField')).val(e);var j=c.get('onSelect');if(j&&!b&&!c.inSelect){c.inSelect=true;j.apply(a,[c.selectedDates]);c.inSelect=false;}}},_getBorders:function(c){var d=function(a){var b=($.browser.msie?1:0);return{thin:1+b,medium:3+b,thick:5+b}[a]||a;};return[parseFloat(d(c.css('border-left-width'))),parseFloat(d(c.css('border-top-width')))];},_checkOffset:function(a){var b=(a.target.is(':hidden')&&a.trigger?a.trigger:a.target);var c=b.offset();var d=false;$(a.target).parents().each(function(){d|=$(this).css('position')=='fixed';return!d;});if(d&&$.browser.opera){c.left-=document.documentElement.scrollLeft;c.top-=document.documentElement.scrollTop;}var e=(!$.browser.mozilla||document.doctype?document.documentElement.clientWidth:0)||document.body.clientWidth;var f=(!$.browser.mozilla||document.doctype?document.documentElement.clientHeight:0)||document.body.clientHeight;if(e==0){return c;}var g=a.get('alignment');var h=a.get('isRTL');var i=document.documentElement.scrollLeft||document.body.scrollLeft;var j=document.documentElement.scrollTop||document.body.scrollTop;var k=c.top-a.div.outerHeight()-(d&&$.browser.opera?document.documentElement.scrollTop:0);var l=c.top+b.outerHeight();var m=c.left;var n=c.left+b.outerWidth()-a.div.outerWidth()-(d&&$.browser.opera?document.documentElement.scrollLeft:0);var o=(c.left+a.div.outerWidth()-i)>e;var p=(c.top+a.target.outerHeight()+a.div.outerHeight()-j)>f;if(g=='topLeft'){c={left:m,top:k};}else if(g=='topRight'){c={left:n,top:k};}else if(g=='bottomLeft'){c={left:m,top:l};}else if(g=='bottomRight'){c={left:n,top:l};}else if(g=='top'){c={left:(h||o?n:m),top:k};}else{c={left:(h||o?n:m),top:(p?k:l)};}c.left=Math.max((d?0:i),c.left-(d?i:0));c.top=Math.max((d?0:j),c.top-(d?j:0));return c;},_checkExternalClick:function(a){if(!$.datepick.curInst){return;}var b=$(a.target);if(!b.parents().andSelf().hasClass($.datepick._popupClass)&&!b.hasClass($.datepick.markerClass)&&!b.parents().andSelf().hasClass($.datepick._triggerClass)){$.datepick.hide($.datepick.curInst);}},hide:function(b,c){var d=$.data(b,this.dataName)||b;if(d&&d==$.datepick.curInst){var e=(c?'':d.get('showAnim'));var f=d.get('showSpeed');f=(f=='normal'&&$.ui&&$.ui.version>='1.8'?'_default':f);var g=function(){d.div.remove();d.div=null;$.datepick.curInst=null;var a=d.get('onClose');if(a){a.apply(b,[d.selectedDates]);}};d.div.stop();if($.effects&&$.effects[e]){d.div.hide(e,d.get('showOptions'),f,g);}else{var h=(e=='slideDown'?'slideUp':(e=='fadeIn'?'fadeOut':'hide'));d.div[h]((e?f:''),g);}if(!e){g();}}},_keyDown:function(a){var b=a.target;var c=$.data(b,$.datepick.dataName);var d=false;if(c.div){if(a.keyCode==9){$.datepick.hide(b);}else if(a.keyCode==13){$.datepick.selectDate(b,$('a.'+c.get('renderer').highlightedClass,c.div)[0]);d=true;}else{var e=c.get('commands');for(var f in e){var g=e[f];if(g.keystroke.keyCode==a.keyCode&&!!g.keystroke.ctrlKey==!!(a.ctrlKey||a.metaKey)&&!!g.keystroke.altKey==a.altKey&&!!g.keystroke.shiftKey==a.shiftKey){$.datepick.performAction(b,f);d=true;break;}}}}else{var g=c.get('commands').current;if(g.keystroke.keyCode==a.keyCode&&!!g.keystroke.ctrlKey==!!(a.ctrlKey||a.metaKey)&&!!g.keystroke.altKey==a.altKey&&!!g.keystroke.shiftKey==a.shiftKey){$.datepick.show(b);d=true;}}c.ctrlKey=((a.keyCode<48&&a.keyCode!=32)||a.ctrlKey||a.metaKey);if(d){a.preventDefault();a.stopPropagation();}return!d;},_keyPress:function(a){var b=a.target;var c=$.data(b,$.datepick.dataName);if(c&&c.get('constrainInput')){var d=String.fromCharCode(a.keyCode||a.charCode);var e=$.datepick._allowedChars(c);return(a.metaKey||c.ctrlKey||d<' '||!e||e.indexOf(d)>-1);}return true;},_allowedChars:function(a){var b=a.get('dateFormat');var c=(a.get('multiSelect')?a.get('multiSeparator'):(a.get('rangeSelect')?a.get('rangeSeparator'):''));var d=false;var e=false;for(var i=0;i<b.length;i++){var f=b.charAt(i);if(d){if(f=="'"&&b.charAt(i+1)!="'"){d=false;}else{c+=f;}}else{switch(f){case'd':case'm':case'o':case'w':c+=(e?'':'0123456789');e=true;break;case'y':case'@':case'!':c+=(e?'':'0123456789')+'-';e=true;break;case'J':c+=(e?'':'0123456789')+'-.';e=true;break;case'D':case'M':case'Y':return null;case"'":if(b.charAt(i+1)=="'"){c+="'";}else{d=true;}break;default:c+=f;}}}return c;},_keyUp:function(a){var b=a.target;var c=$.data(b,$.datepick.dataName);if(c&&!c.ctrlKey&&c.lastVal!=c.target.val()){try{var d=$.datepick._extractDates(c,c.target.val());if(d.length>0){$.datepick.setDate(b,d,null,true);}}catch(a){}}return true;},clear:function(a){var b=$.data(a,this.dataName);if(b){b.selectedDates=[];this.hide(a);if(b.get('selectDefaultDate')&&b.get('defaultDate')){this.setDate(a,$.datepick.newDate(b.get('defaultDate')||$.datepick.today()));}else{this._updateInput(a);}}},getDate:function(a){var b=$.data(a,this.dataName);return(b?b.selectedDates:[]);},setDate:function(a,b,c,d,e){var f=$.data(a,this.dataName);if(f){if(!$.isArray(b)){b=[b];if(c){b.push(c);}}var g=f.get('dateFormat');var h=f.get('minDate');var k=f.get('maxDate');var l=f.selectedDates[0];f.selectedDates=[];for(var i=0;i<b.length;i++){var m=$.datepick.determineDate(b[i],null,l,g,f.getConfig());if(m){if((!h||m.getTime()>=h.getTime())&&(!k||m.getTime()<=k.getTime())){var n=false;for(var j=0;j<f.selectedDates.length;j++){if(f.selectedDates[j].getTime()==m.getTime()){n=true;break;}}if(!n){f.selectedDates.push(m);}}}}var o=f.get('rangeSelect');f.selectedDates.splice(f.get('multiSelect')||(o?2:1),f.selectedDates.length);if(o){switch(f.selectedDates.length){case 1:f.selectedDates[1]=f.selectedDates[0];break;case 2:f.selectedDates[1]=(f.selectedDates[0].getTime()>f.selectedDates[1].getTime()?f.selectedDates[0]:f.selectedDates[1]);break;}f.pickingRange=false;}f.prevDate=(f.drawDate?$.datepick.newDate(f.drawDate):null);f.drawDate=this._checkMinMax($.datepick.newDate(f.selectedDates[0]||f.get('defaultDate')||$.datepick.today()),f);if(!e){this._update(a);this._updateInput(a,d);}}},performAction:function(a,b){var c=$.data(a,this.dataName);if(c&&!this.isDisabled(a)){var d=c.get('commands');if(d[b]&&d[b].enabled.apply(a,[c])){d[b].action.apply(a,[c]);}}},showMonth:function(a,b,c,d){var e=$.data(a,this.dataName);if(e&&(d!=null||(e.drawDate.getFullYear()!=b||e.drawDate.getMonth()+1!=c))){e.prevDate=$.datepick.newDate(e.drawDate);var f=this._checkMinMax((b!=null?$.datepick.newDate(b,c,1):$.datepick.today()),e);e.drawDate=$.datepick.newDate(f.getFullYear(),f.getMonth()+1,(d!=null?d:Math.min(e.drawDate.getDate(),$.datepick.daysInMonth(f.getFullYear(),f.getMonth()+1))));this._update(a);}},changeMonth:function(a,b){var c=$.data(a,this.dataName);if(c){var d=$.datepick.add($.datepick.newDate(c.drawDate),b,'m');this.showMonth(a,d.getFullYear(),d.getMonth()+1);}},changeDay:function(a,b){var c=$.data(a,this.dataName);if(c){var d=$.datepick.add($.datepick.newDate(c.drawDate),b,'d');this.showMonth(a,d.getFullYear(),d.getMonth()+1,d.getDate());}},_checkMinMax:function(a,b){var c=b.get('minDate');var d=b.get('maxDate');a=(c&&a.getTime()<c.getTime()?$.datepick.newDate(c):a);a=(d&&a.getTime()>d.getTime()?$.datepick.newDate(d):a);return a;},retrieveDate:function(a,b){var c=$.data(a,this.dataName);return(!c?null:this._normaliseDate(new Date(parseInt(b.className.replace(/^.*dp(-?\d+).*$/,'$1'),10))));},selectDate:function(a,b){var c=$.data(a,this.dataName);if(c&&!this.isDisabled(a)){var d=this.retrieveDate(a,b);var e=c.get('multiSelect');var f=c.get('rangeSelect');if(e){var g=false;for(var i=0;i<c.selectedDates.length;i++){if(d.getTime()==c.selectedDates[i].getTime()){c.selectedDates.splice(i,1);g=true;break;}}if(!g&&c.selectedDates.length<e){c.selectedDates.push(d);}}else if(f){if(c.pickingRange){c.selectedDates[1]=d;}else{c.selectedDates=[d,d];}c.pickingRange=!c.pickingRange;}else{c.selectedDates=[d];}c.prevDate=$.datepick.newDate(d);this._updateInput(a);if(c.inline||c.pickingRange||c.selectedDates.length<(e||(f?2:1))){this._update(a);}else{this.hide(a);}}},_generateContent:function(h,i){var j=i.get('renderer');var k=i.get('monthsToShow');k=($.isArray(k)?k:[1,k]);i.drawDate=this._checkMinMax(i.drawDate||i.get('defaultDate')||$.datepick.today(),i);var l=$.datepick.add($.datepick.newDate(i.drawDate),-i.get('monthsOffset'),'m');var m='';for(var n=0;n<k[0];n++){var o='';for(var p=0;p<k[1];p++){o+=this._generateMonth(h,i,l.getFullYear(),l.getMonth()+1,j,(n==0&&p==0));$.datepick.add(l,1,'m');}m+=this._prepare(j.monthRow,i).replace(/\{months\}/,o);}var q=this._prepare(j.picker,i).replace(/\{months\}/,m).replace(/\{weekHeader\}/g,this._generateDayHeaders(i,j))+($.browser.msie&&parseInt($.browser.version,10)<7&&!i.inline?'<iframe src="javascript:void(0);" class="'+this._coverClass+'"></iframe>':'');var r=i.get('commands');var s=i.get('commandsAsDateFormat');var t=function(a,b,c,d,e){if(q.indexOf('{'+a+':'+d+'}')==-1){return;}var f=r[d];var g=(s?f.date.apply(h,[i]):null);q=q.replace(new RegExp('\\{'+a+':'+d+'\\}','g'),'<'+b+(f.status?' title="'+i.get(f.status)+'"':'')+' class="'+j.commandClass+' '+j.commandClass+'-'+d+' '+e+(f.enabled(i)?'':' '+j.disabledClass)+'">'+(g?$.datepick.formatDate(i.get(f.text),g,i.getConfig()):i.get(f.text))+'</'+c+'>');};for(var u in r){t('button','button type="button"','button',u,j.commandButtonClass);t('link','a href="javascript:void(0)"','a',u,j.commandLinkClass);}q=$(q);if(k[1]>1){var v=0;$(j.monthSelector,q).each(function(){var a=++v%k[1];$(this).addClass(a==1?'first':(a==0?'last':''));});}var w=this;q.find(j.daySelector+' a').hover(function(){$(this).addClass(j.highlightedClass);},function(){(i.inline?$(this).parents('.'+w.markerClass):i.div).find(j.daySelector+' a').removeClass(j.highlightedClass);}).click(function(){w.selectDate(h,this);}).end().find('select.'+this._monthYearClass+':not(.'+this._anyYearClass+')').change(function(){var a=$(this).val().split('/');w.showMonth(h,parseInt(a[1],10),parseInt(a[0],10));}).end().find('select.'+this._anyYearClass).click(function(){$(this).css('visibility','hidden').next('input').css({left:this.offsetLeft,top:this.offsetTop,width:this.offsetWidth,height:this.offsetHeight}).show().focus();}).end().find('input.'+w._monthYearClass).change(function(){try{var a=parseInt($(this).val(),10);a=(isNaN(a)?i.drawDate.getFullYear():a);w.showMonth(h,a,i.drawDate.getMonth()+1,i.drawDate.getDate());}catch(e){alert(e);}}).keydown(function(a){if(a.keyCode==13){$(a.target).change();}else if(a.keyCode==27){$(a.target).hide().prev('select').css('visibility','visible');i.target.focus();}});q.find('.'+j.commandClass).click(function(){if(!$(this).hasClass(j.disabledClass)){var a=this.className.replace(new RegExp('^.*'+j.commandClass+'-([^ ]+).*$'),'$1');$.datepick.performAction(h,a);}});if(i.get('isRTL')){q.addClass(j.rtlClass);}if(k[0]*k[1]>1){q.addClass(j.multiClass);}var x=i.get('pickerClass');if(x){q.addClass(x);}$('body').append(q);var y=0;q.find(j.monthSelector).each(function(){y+=$(this).outerWidth();});q.width(y/k[0]);var z=i.get('onShow');if(z){z.apply(h,[q,i]);}return q;},_generateMonth:function(a,b,c,d,e,f){var g=$.datepick.daysInMonth(c,d);var h=b.get('monthsToShow');h=($.isArray(h)?h:[1,h]);var j=b.get('fixedWeeks')||(h[0]*h[1]>1);var k=b.get('firstDay');var l=($.datepick.newDate(c,d,1).getDay()-k+7)%7;var m=(j?6:Math.ceil((l+g)/7));var n=b.get('showOtherMonths');var o=b.get('selectOtherMonths')&&n;var p=b.get('dayStatus');var q=(b.pickingRange?b.selectedDates[0]:b.get('minDate'));var r=b.get('maxDate');var s=b.get('rangeSelect');var t=b.get('onDate');var u=e.week.indexOf('{weekOfYear}')>-1;var v=b.get('calculateWeek');var w=$.datepick.today();var x=$.datepick.newDate(c,d,1);$.datepick.add(x,-l-(j&&(x.getDay()==k)?7:0),'d');var y=x.getTime();var z='';for(var A=0;A<m;A++){var B=(!u?'':'<span class="dp'+y+'">'+(v?v(x):0)+'</span>');var C='';for(var D=0;D<7;D++){var E=false;if(s&&b.selectedDates.length>0){E=(x.getTime()>=b.selectedDates[0]&&x.getTime()<=b.selectedDates[1]);}else{for(var i=0;i<b.selectedDates.length;i++){if(b.selectedDates[i].getTime()==x.getTime()){E=true;break;}}}var F=(!t?{}:t.apply(a,[x,x.getMonth()+1==d]));var G=(F.selectable!=false)&&(o||x.getMonth()+1==d)&&(!q||x.getTime()>=q.getTime())&&(!r||x.getTime()<=r.getTime());C+=this._prepare(e.day,b).replace(/\{day\}/g,(G?'<a href="javascript:void(0)"':'<span')+' class="dp'+y+' '+(F.dateClass||'')+(E&&(o||x.getMonth()+1==d)?' '+e.selectedClass:'')+(G?' '+e.defaultClass:'')+((x.getDay()||7)<6?'':' '+e.weekendClass)+(x.getMonth()+1==d?'':' '+e.otherMonthClass)+(x.getTime()==w.getTime()&&(x.getMonth()+1)==d?' '+e.todayClass:'')+(x.getTime()==b.drawDate.getTime()&&(x.getMonth()+1)==d?' '+e.highlightedClass:'')+'"'+(F.title||(p&&G)?' title="'+(F.title||$.datepick.formatDate(p,x,b.getConfig()))+'"':'')+'>'+(n||(x.getMonth()+1)==d?F.content||x.getDate():'&nbsp;')+(G?'</a>':'</span>'));$.datepick.add(x,1,'d');y=x.getTime();}z+=this._prepare(e.week,b).replace(/\{days\}/g,C).replace(/\{weekOfYear\}/g,B);}var H=this._prepare(e.month,b).match(/\{monthHeader(:[^\}]+)?\}/);H=(H[0].length<=13?'MM yyyy':H[0].substring(13,H[0].length-1));H=(f?this._generateMonthSelection(b,c,d,q,r,H,e):$.datepick.formatDate(H,$.datepick.newDate(c,d,1),b.getConfig()));var I=this._prepare(e.weekHeader,b).replace(/\{days\}/g,this._generateDayHeaders(b,e));return this._prepare(e.month,b).replace(/\{monthHeader(:[^\}]+)?\}/g,H).replace(/\{weekHeader\}/g,I).replace(/\{weeks\}/g,z);},_generateDayHeaders:function(a,b){var c=a.get('firstDay');var d=a.get('dayNames');var e=a.get('dayNamesMin');var f='';for(var g=0;g<7;g++){var h=(g+c)%7;f+=this._prepare(b.dayHeader,a).replace(/\{day\}/g,'<span class="'+this._curDoWClass+h+'" title="'+d[h]+'">'+e[h]+'</span>');}return f;},_generateMonthSelection:function(a,b,c,d,e,f){if(!a.get('changeMonth')){return $.datepick.formatDate(f,$.datepick.newDate(b,c,1),a.getConfig());}var g=a.get('monthNames'+(f.match(/mm/i)?'':'Short'));var h=f.replace(/m+/i,'\\x2E').replace(/y+/i,'\\x2F');var i='<select class="'+this._monthYearClass+'" title="'+a.get('monthStatus')+'">';for(var m=1;m<=12;m++){if((!d||$.datepick.newDate(b,m,$.datepick.daysInMonth(b,m)).getTime()>=d.getTime())&&(!e||$.datepick.newDate(b,m,1).getTime()<=e.getTime())){i+='<option value="'+m+'/'+b+'"'+(c==m?' selected="selected"':'')+'>'+g[m-1]+'</option>';}}i+='</select>';h=h.replace(/\\x2E/,i);var j=a.get('yearRange');if(j=='any'){i='<select class="'+this._monthYearClass+' '+this._anyYearClass+'" title="'+a.get('yearStatus')+'">'+'<option>'+b+'</option></select>'+'<input class="'+this._monthYearClass+' '+this._curMonthClass+c+'" value="'+b+'">';}else{j=j.split(':');var k=$.datepick.today().getFullYear();var l=(j[0].match('c[+-].*')?b+parseInt(j[0].substring(1),10):((j[0].match('[+-].*')?k:0)+parseInt(j[0],10)));var n=(j[1].match('c[+-].*')?b+parseInt(j[1].substring(1),10):((j[1].match('[+-].*')?k:0)+parseInt(j[1],10)));i='<select class="'+this._monthYearClass+'" title="'+a.get('yearStatus')+'">';var o=$.datepick.add($.datepick.newDate(l+1,1,1),-1,'d');o=(d&&d.getTime()>o.getTime()?d:o).getFullYear();var p=$.datepick.newDate(n,1,1);p=(e&&e.getTime()<p.getTime()?e:p).getFullYear();for(var y=o;y<=p;y++){if(y!=0){i+='<option value="'+c+'/'+y+'"'+(b==y?' selected="selected"':'')+'>'+y+'</option>';}}i+='</select>';}h=h.replace(/\\x2F/,i);return h;},_prepare:function(e,f){var g=function(a,b){while(true){var c=e.indexOf('{'+a+':start}');if(c==-1){return;}var d=e.substring(c).indexOf('{'+a+':end}');if(d>-1){e=e.substring(0,c)+(b?e.substr(c+a.length+8,d-a.length-8):'')+e.substring(c+d+a.length+6);}}};g('inline',f.inline);g('popup',!f.inline);var h=/\{l10n:([^\}]+)\}/;var i=null;while(i=h.exec(e)){e=e.replace(i[0],f.get(i[1]));}return e;}});function extendRemove(a,b){$.extend(a,b);for(var c in b)if(b[c]==null||b[c]==undefined)a[c]=b[c];return a;};$.fn.datepick=function(a){var b=Array.prototype.slice.call(arguments,1);if($.inArray(a,['getDate','isDisabled','options','retrieveDate'])>-1){return $.datepick[a].apply($.datepick,[this[0]].concat(b));}return this.each(function(){if(typeof a=='string'){$.datepick[a].apply($.datepick,[this].concat(b))}else{$.datepick._attachPicker(this,a||{})}})};$.datepick=new Datepicker();$(function(){$(document).mousedown($.datepick._checkExternalClick).resize(function(){$.datepick.hide($.datepick.curInst)})})})(jQuery);
/*
 * jQuery validation plug-in 1.7
 *
 * http://bassistance.de/jquery-plugins/jquery-plugin-validation/
 * http://docs.jquery.com/Plugins/Validation
 *
 * Copyright (c) 2006 - 2008 Jörn Zaefferer
 *
 * $Id: jquery.validate.js 6403 2009-06-17 14:27:16Z joern.zaefferer $
 *
 * Dual licensed under the MIT and GPL licenses:
 *   http://www.opensource.org/licenses/mit-license.php
 *   http://www.gnu.org/licenses/gpl.html
 */
(function($){$.extend($.fn,{validate:function(options){if(!this.length){options&&options.debug&&window.console&&console.warn("nothing selected, can't validate, returning nothing");return;}var validator=$.data(this[0],'validator');if(validator){return validator;}validator=new $.validator(options,this[0]);$.data(this[0],'validator',validator);if(validator.settings.onsubmit){this.find("input, button").filter(".cancel").click(function(){validator.cancelSubmit=true;});if(validator.settings.submitHandler){this.find("input, button").filter(":submit").click(function(){validator.submitButton=this;});}this.submit(function(event){if(validator.settings.debug)event.preventDefault();function handle(){if(validator.settings.submitHandler){if(validator.submitButton){var hidden=$("<input type='hidden'/>").attr("name",validator.submitButton.name).val(validator.submitButton.value).appendTo(validator.currentForm);}validator.settings.submitHandler.call(validator,validator.currentForm);if(validator.submitButton){hidden.remove();}return false;}return true;}if(validator.cancelSubmit){validator.cancelSubmit=false;return handle();}if(validator.form()){if(validator.pendingRequest){validator.formSubmitted=true;return false;}return handle();}else{validator.focusInvalid();return false;}});}return validator;},valid:function(){if($(this[0]).is('form')){return this.validate().form();}else{var valid=true;var validator=$(this[0].form).validate();this.each(function(){valid&=validator.element(this);});return valid;}},removeAttrs:function(attributes){var result={},$element=this;$.each(attributes.split(/\s/),function(index,value){result[value]=$element.attr(value);$element.removeAttr(value);});return result;},rules:function(command,argument){var element=this[0];if(command){var settings=$.data(element.form,'validator').settings;var staticRules=settings.rules;var existingRules=$.validator.staticRules(element);switch(command){case"add":$.extend(existingRules,$.validator.normalizeRule(argument));staticRules[element.name]=existingRules;if(argument.messages)settings.messages[element.name]=$.extend(settings.messages[element.name],argument.messages);break;case"remove":if(!argument){delete staticRules[element.name];return existingRules;}var filtered={};$.each(argument.split(/\s/),function(index,method){filtered[method]=existingRules[method];delete existingRules[method];});return filtered;}}var data=$.validator.normalizeRules($.extend({},$.validator.metadataRules(element),$.validator.classRules(element),$.validator.attributeRules(element),$.validator.staticRules(element)),element);if(data.required){var param=data.required;delete data.required;data=$.extend({required:param},data);}return data;}});$.extend($.expr[":"],{blank:function(a){return!$.trim(""+a.value);},filled:function(a){return!!$.trim(""+a.value);},unchecked:function(a){return!a.checked;}});$.validator=function(options,form){this.settings=$.extend(true,{},$.validator.defaults,options);this.currentForm=form;this.init();};$.validator.format=function(source,params){if(arguments.length==1)return function(){var args=$.makeArray(arguments);args.unshift(source);return $.validator.format.apply(this,args);};if(arguments.length>2&&params.constructor!=Array){params=$.makeArray(arguments).slice(1);}if(params.constructor!=Array){params=[params];}$.each(params,function(i,n){source=source.replace(new RegExp("\\{"+i+"\\}","g"),n);});return source;};$.extend($.validator,{defaults:{messages:{},groups:{},rules:{},errorClass:"error",validClass:"valid",errorElement:"label",focusInvalid:true,errorContainer:$([]),errorLabelContainer:$([]),onsubmit:true,ignore:[],ignoreTitle:false,onfocusin:function(element){this.lastActive=element;if(this.settings.focusCleanup&&!this.blockFocusCleanup){this.settings.unhighlight&&this.settings.unhighlight.call(this,element,this.settings.errorClass,this.settings.validClass);this.errorsFor(element).hide();}},onfocusout:function(element){if(!this.checkable(element)&&(element.name in this.submitted||!this.optional(element))){this.element(element);}},onkeyup:function(element){if(element.name in this.submitted||element==this.lastElement){this.element(element);}},onclick:function(element){if(element.name in this.submitted)this.element(element);else if(element.parentNode.name in this.submitted)this.element(element.parentNode);},highlight:function(element,errorClass,validClass){$(element).addClass(errorClass).removeClass(validClass);},unhighlight:function(element,errorClass,validClass){$(element).removeClass(errorClass).addClass(validClass);}},setDefaults:function(settings){$.extend($.validator.defaults,settings);},messages:{required:"This field is required.",remote:"Please fix this field.",email:"Please enter a valid email address.",url:"Please enter a valid URL.",date:"Please enter a valid date.",dateISO:"Please enter a valid date (ISO).",number:"Please enter a valid number.",digits:"Please enter only digits.",creditcard:"Please enter a valid credit card number.",equalTo:"Please enter the same value again.",accept:"Please enter a value with a valid extension.",maxlength:$.validator.format("Please enter no more than {0} characters."),minlength:$.validator.format("Please enter at least {0} characters."),rangelength:$.validator.format("Please enter a value between {0} and {1} characters long."),range:$.validator.format("Please enter a value between {0} and {1}."),max:$.validator.format("Please enter a value less than or equal to {0}."),min:$.validator.format("Please enter a value greater than or equal to {0}.")},autoCreateRanges:false,prototype:{init:function(){this.labelContainer=$(this.settings.errorLabelContainer);this.errorContext=this.labelContainer.length&&this.labelContainer||$(this.currentForm);this.containers=$(this.settings.errorContainer).add(this.settings.errorLabelContainer);this.submitted={};this.valueCache={};this.pendingRequest=0;this.pending={};this.invalid={};this.reset();var groups=(this.groups={});$.each(this.settings.groups,function(key,value){$.each(value.split(/\s/),function(index,name){groups[name]=key;});});var rules=this.settings.rules;$.each(rules,function(key,value){rules[key]=$.validator.normalizeRule(value);});function delegate(event){var validator=$.data(this[0].form,"validator"),eventType="on"+event.type.replace(/^validate/,"");validator.settings[eventType]&&validator.settings[eventType].call(validator,this[0]);}$(this.currentForm).validateDelegate(":text, :password, :file, select, textarea","focusin focusout keyup",delegate).validateDelegate(":radio, :checkbox, select, option","click",delegate);if(this.settings.invalidHandler)$(this.currentForm).bind("invalid-form.validate",this.settings.invalidHandler);},form:function(){this.checkForm();$.extend(this.submitted,this.errorMap);this.invalid=$.extend({},this.errorMap);if(!this.valid())$(this.currentForm).triggerHandler("invalid-form",[this]);this.showErrors();return this.valid();},checkForm:function(){this.prepareForm();for(var i=0,elements=(this.currentElements=this.elements());elements[i];i++){this.check(elements[i]);}return this.valid();},element:function(element){element=this.clean(element);this.lastElement=element;this.prepareElement(element);this.currentElements=$(element);var result=this.check(element);if(result){delete this.invalid[element.name];}else{this.invalid[element.name]=true;}if(!this.numberOfInvalids()){this.toHide=this.toHide.add(this.containers);}this.showErrors();return result;},showErrors:function(errors){if(errors){$.extend(this.errorMap,errors);this.errorList=[];for(var name in errors){this.errorList.push({message:errors[name],element:this.findByName(name)[0]});}this.successList=$.grep(this.successList,function(element){return!(element.name in errors);});}this.settings.showErrors?this.settings.showErrors.call(this,this.errorMap,this.errorList):this.defaultShowErrors();},resetForm:function(){if($.fn.resetForm)$(this.currentForm).resetForm();this.submitted={};this.prepareForm();this.hideErrors();this.elements().removeClass(this.settings.errorClass);},numberOfInvalids:function(){return this.objectLength(this.invalid);},objectLength:function(obj){var count=0;for(var i in obj)count++;return count;},hideErrors:function(){this.addWrapper(this.toHide).hide();},valid:function(){return this.size()==0;},size:function(){return this.errorList.length;},focusInvalid:function(){if(this.settings.focusInvalid){try{$(this.findLastActive()||this.errorList.length&&this.errorList[0].element||[]).filter(":visible").focus().trigger("focusin");}catch(e){}}},findLastActive:function(){var lastActive=this.lastActive;return lastActive&&$.grep(this.errorList,function(n){return n.element.name==lastActive.name;}).length==1&&lastActive;},elements:function(){var validator=this,rulesCache={};return $([]).add(this.currentForm.elements).filter(":input").not(":submit, :reset, :image, [disabled]").not(this.settings.ignore).filter(function(){!this.name&&validator.settings.debug&&window.console&&console.error("%o has no name assigned",this);if(this.name in rulesCache||!validator.objectLength($(this).rules()))return false;rulesCache[this.name]=true;return true;});},clean:function(selector){return $(selector)[0];},errors:function(){return $(this.settings.errorElement+"."+this.settings.errorClass,this.errorContext);},reset:function(){this.successList=[];this.errorList=[];this.errorMap={};this.toShow=$([]);this.toHide=$([]);this.currentElements=$([]);},prepareForm:function(){this.reset();this.toHide=this.errors().add(this.containers);},prepareElement:function(element){this.reset();this.toHide=this.errorsFor(element);},check:function(element){element=this.clean(element);if(this.checkable(element)){element=this.findByName(element.name)[0];}var rules=$(element).rules();var dependencyMismatch=false;for(method in rules){var rule={method:method,parameters:rules[method]};try{var result=$.validator.methods[method].call(this,element.value.replace(/\r/g,""),element,rule.parameters);if(result=="dependency-mismatch"){dependencyMismatch=true;continue;}dependencyMismatch=false;if(result=="pending"){this.toHide=this.toHide.not(this.errorsFor(element));return;}if(!result){this.formatAndAdd(element,rule);return false;}}catch(e){this.settings.debug&&window.console&&console.log("exception occured when checking element "+element.id
+", check the '"+rule.method+"' method",e);throw e;}}if(dependencyMismatch)return;if(this.objectLength(rules))this.successList.push(element);return true;},customMetaMessage:function(element,method){if(!$.metadata)return;var meta=this.settings.meta?$(element).metadata()[this.settings.meta]:$(element).metadata();return meta&&meta.messages&&meta.messages[method];},customMessage:function(name,method){var m=this.settings.messages[name];return m&&(m.constructor==String?m:m[method]);},findDefined:function(){for(var i=0;i<arguments.length;i++){if(arguments[i]!==undefined)return arguments[i];}return undefined;},defaultMessage:function(element,method){return this.findDefined(this.customMessage(element.name,method),this.customMetaMessage(element,method),!this.settings.ignoreTitle&&element.title||undefined,$.validator.messages[method],"<strong>Warning: No message defined for "+element.name+"</strong>");},formatAndAdd:function(element,rule){var message=this.defaultMessage(element,rule.method),theregex=/\$?\{(\d+)\}/g;if(typeof message=="function"){message=message.call(this,rule.parameters,element);}else if(theregex.test(message)){message=jQuery.format(message.replace(theregex,'{$1}'),rule.parameters);}this.errorList.push({message:message,element:element});this.errorMap[element.name]=message;this.submitted[element.name]=message;},addWrapper:function(toToggle){if(this.settings.wrapper)toToggle=toToggle.add(toToggle.parent(this.settings.wrapper));return toToggle;},defaultShowErrors:function(){for(var i=0;this.errorList[i];i++){var error=this.errorList[i];this.settings.highlight&&this.settings.highlight.call(this,error.element,this.settings.errorClass,this.settings.validClass);this.showLabel(error.element,error.message);}if(this.errorList.length){this.toShow=this.toShow.add(this.containers);}if(this.settings.success){for(var i=0;this.successList[i];i++){this.showLabel(this.successList[i]);}}if(this.settings.unhighlight){for(var i=0,elements=this.validElements();elements[i];i++){this.settings.unhighlight.call(this,elements[i],this.settings.errorClass,this.settings.validClass);}}this.toHide=this.toHide.not(this.toShow);this.hideErrors();this.addWrapper(this.toShow).show();},validElements:function(){return this.currentElements.not(this.invalidElements());},invalidElements:function(){return $(this.errorList).map(function(){return this.element;});},showLabel:function(element,message){var label=this.errorsFor(element);if(label.length){label.removeClass().addClass(this.settings.errorClass);label.attr("generated")&&label.html(message);}else{label=$("<"+this.settings.errorElement+"/>").attr({"for":this.idOrName(element),generated:true}).addClass(this.settings.errorClass).html(message||"");if(this.settings.wrapper){label=label.hide().show().wrap("<"+this.settings.wrapper+"/>").parent();}if(!this.labelContainer.append(label).length)this.settings.errorPlacement?this.settings.errorPlacement(label,$(element)):label.insertAfter(element);}if(!message&&this.settings.success){label.text("");typeof this.settings.success=="string"?label.addClass(this.settings.success):this.settings.success(label);}this.toShow=this.toShow.add(label);},errorsFor:function(element){var name=this.idOrName(element);return this.errors().filter(function(){return $(this).attr('for')==name;});},idOrName:function(element){return this.groups[element.name]||(this.checkable(element)?element.name:element.id||element.name);},checkable:function(element){return/radio|checkbox/i.test(element.type);},findByName:function(name){var form=this.currentForm;return $(document.getElementsByName(name)).map(function(index,element){return element.form==form&&element.name==name&&element||null;});},getLength:function(value,element){switch(element.nodeName.toLowerCase()){case'select':return $("option:selected",element).length;case'input':if(this.checkable(element))return this.findByName(element.name).filter(':checked').length;}return value.length;},depend:function(param,element){return this.dependTypes[typeof param]?this.dependTypes[typeof param](param,element):true;},dependTypes:{"boolean":function(param,element){return param;},"string":function(param,element){return!!$(param,element.form).length;},"function":function(param,element){return param(element);}},optional:function(element){return!$.validator.methods.required.call(this,$.trim(element.value),element)&&"dependency-mismatch";},startRequest:function(element){if(!this.pending[element.name]){this.pendingRequest++;this.pending[element.name]=true;}},stopRequest:function(element,valid){this.pendingRequest--;if(this.pendingRequest<0)this.pendingRequest=0;delete this.pending[element.name];if(valid&&this.pendingRequest==0&&this.formSubmitted&&this.form()){$(this.currentForm).submit();this.formSubmitted=false;}else if(!valid&&this.pendingRequest==0&&this.formSubmitted){$(this.currentForm).triggerHandler("invalid-form",[this]);this.formSubmitted=false;}},previousValue:function(element){return $.data(element,"previousValue")||$.data(element,"previousValue",{old:null,valid:true,message:this.defaultMessage(element,"remote")});}},classRuleSettings:{required:{required:true},email:{email:true},url:{url:true},date:{date:true},dateISO:{dateISO:true},dateDE:{dateDE:true},number:{number:true},numberDE:{numberDE:true},digits:{digits:true},creditcard:{creditcard:true}},addClassRules:function(className,rules){className.constructor==String?this.classRuleSettings[className]=rules:$.extend(this.classRuleSettings,className);},classRules:function(element){var rules={};var classes=$(element).attr('class');classes&&$.each(classes.split(' '),function(){if(this in $.validator.classRuleSettings){$.extend(rules,$.validator.classRuleSettings[this]);}});return rules;},attributeRules:function(element){var rules={};var $element=$(element);for(method in $.validator.methods){var value=$element.attr(method);if(value){rules[method]=value;}}if(rules.maxlength&&/-1|2147483647|524288/.test(rules.maxlength)){delete rules.maxlength;}return rules;},metadataRules:function(element){if(!$.metadata)return{};var meta=$.data(element.form,'validator').settings.meta;return meta?$(element).metadata()[meta]:$(element).metadata();},staticRules:function(element){var rules={};var validator=$.data(element.form,'validator');if(validator.settings.rules){rules=$.validator.normalizeRule(validator.settings.rules[element.name])||{};}return rules;},normalizeRules:function(rules,element){$.each(rules,function(prop,val){if(val===false){delete rules[prop];return;}if(val.param||val.depends){var keepRule=true;switch(typeof val.depends){case"string":keepRule=!!$(val.depends,element.form).length;break;case"function":keepRule=val.depends.call(element,element);break;}if(keepRule){rules[prop]=val.param!==undefined?val.param:true;}else{delete rules[prop];}}});$.each(rules,function(rule,parameter){rules[rule]=$.isFunction(parameter)?parameter(element):parameter;});$.each(['minlength','maxlength','min','max'],function(){if(rules[this]){rules[this]=Number(rules[this]);}});$.each(['rangelength','range'],function(){if(rules[this]){rules[this]=[Number(rules[this][0]),Number(rules[this][1])];}});if($.validator.autoCreateRanges){if(rules.min&&rules.max){rules.range=[rules.min,rules.max];delete rules.min;delete rules.max;}if(rules.minlength&&rules.maxlength){rules.rangelength=[rules.minlength,rules.maxlength];delete rules.minlength;delete rules.maxlength;}}if(rules.messages){delete rules.messages;}return rules;},normalizeRule:function(data){if(typeof data=="string"){var transformed={};$.each(data.split(/\s/),function(){transformed[this]=true;});data=transformed;}return data;},addMethod:function(name,method,message){$.validator.methods[name]=method;$.validator.messages[name]=message!=undefined?message:$.validator.messages[name];if(method.length<3){$.validator.addClassRules(name,$.validator.normalizeRule(name));}},methods:{required:function(value,element,param){if(!this.depend(param,element))return"dependency-mismatch";switch(element.nodeName.toLowerCase()){case'select':var val=$(element).val();return val&&val.length>0;case'input':if(this.checkable(element))return this.getLength(value,element)>0;default:return $.trim(value).length>0;}},remote:function(value,element,param){if(this.optional(element))return"dependency-mismatch";var previous=this.previousValue(element);if(!this.settings.messages[element.name])this.settings.messages[element.name]={};previous.originalMessage=this.settings.messages[element.name].remote;this.settings.messages[element.name].remote=previous.message;param=typeof param=="string"&&{url:param}||param;if(previous.old!==value){previous.old=value;var validator=this;this.startRequest(element);var data={};data[element.name]=value;$.ajax($.extend(true,{url:param,mode:"abort",port:"validate"+element.name,dataType:"json",data:data,success:function(response){validator.settings.messages[element.name].remote=previous.originalMessage;var valid=response===true;if(valid){var submitted=validator.formSubmitted;validator.prepareElement(element);validator.formSubmitted=submitted;validator.successList.push(element);validator.showErrors();}else{var errors={};var message=(previous.message=response||validator.defaultMessage(element,"remote"));errors[element.name]=$.isFunction(message)?message(value):message;validator.showErrors(errors);}previous.valid=valid;validator.stopRequest(element,valid);}},param));return"pending";}else if(this.pending[element.name]){return"pending";}return previous.valid;},minlength:function(value,element,param){return this.optional(element)||this.getLength($.trim(value),element)>=param;},maxlength:function(value,element,param){return this.optional(element)||this.getLength($.trim(value),element)<=param;},rangelength:function(value,element,param){var length=this.getLength($.trim(value),element);return this.optional(element)||(length>=param[0]&&length<=param[1]);},min:function(value,element,param){return this.optional(element)||value>=param;},max:function(value,element,param){return this.optional(element)||value<=param;},range:function(value,element,param){return this.optional(element)||(value>=param[0]&&value<=param[1]);},email:function(value,element){return this.optional(element)||/^((([a-z]|\d|[!#\$%&'\*\+\-\/=\?\^_`{\|}~]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])+(\.([a-z]|\d|[!#\$%&'\*\+\-\/=\?\^_`{\|}~]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])+)*)|((\x22)((((\x20|\x09)*(\x0d\x0a))?(\x20|\x09)+)?(([\x01-\x08\x0b\x0c\x0e-\x1f\x7f]|\x21|[\x23-\x5b]|[\x5d-\x7e]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(\\([\x01-\x09\x0b\x0c\x0d-\x7f]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]))))*(((\x20|\x09)*(\x0d\x0a))?(\x20|\x09)+)?(\x22)))@((([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.)+(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.?$/i.test(value);},url:function(value,element){return this.optional(element)||/^(https?|ftp):\/\/(((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:)*@)?(((\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5]))|((([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.)+(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.?)(:\d*)?)(\/((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)+(\/(([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)*)*)?)?(\?((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)|[\uE000-\uF8FF]|\/|\?)*)?(\#((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)|\/|\?)*)?$/i.test(value);},date:function(value,element){return this.optional(element)||!/Invalid|NaN/.test(new Date(value));},dateISO:function(value,element){return this.optional(element)||/^\d{4}[\/-]\d{1,2}[\/-]\d{1,2}$/.test(value);},number:function(value,element){return this.optional(element)||/^-?(?:\d+|\d{1,3}(?:,\d{3})+)(?:\.\d+)?$/.test(value);},digits:function(value,element){return this.optional(element)||/^\d+$/.test(value);},creditcard:function(value,element){if(this.optional(element))return"dependency-mismatch";if(/[^0-9-]+/.test(value))return false;var nCheck=0,nDigit=0,bEven=false;value=value.replace(/\D/g,"");for(var n=value.length-1;n>=0;n--){var cDigit=value.charAt(n);var nDigit=parseInt(cDigit,10);if(bEven){if((nDigit*=2)>9)nDigit-=9;}nCheck+=nDigit;bEven=!bEven;}return(nCheck%10)==0;},accept:function(value,element,param){param=typeof param=="string"?param.replace(/,/g,'|'):"png|jpe?g|gif";return this.optional(element)||value.match(new RegExp(".("+param+")$","i"));},equalTo:function(value,element,param){var target=$(param).unbind(".validate-equalTo").bind("blur.validate-equalTo",function(){$(element).valid();});return value==target.val();}}});$.format=$.validator.format;})(jQuery);;(function($){var ajax=$.ajax;var pendingRequests={};$.ajax=function(settings){settings=$.extend(settings,$.extend({},$.ajaxSettings,settings));var port=settings.port;if(settings.mode=="abort"){if(pendingRequests[port]){pendingRequests[port].abort();}return(pendingRequests[port]=ajax.apply(this,arguments));}return ajax.apply(this,arguments);};})(jQuery);;(function($){if(!jQuery.event.special.focusin&&!jQuery.event.special.focusout&&document.addEventListener){$.each({focus:'focusin',blur:'focusout'},function(original,fix){$.event.special[fix]={setup:function(){this.addEventListener(original,handler,true);},teardown:function(){this.removeEventListener(original,handler,true);},handler:function(e){arguments[0]=$.event.fix(e);arguments[0].type=fix;return $.event.handle.apply(this,arguments);}};function handler(e){e=$.event.fix(e);e.type=fix;return $.event.handle.call(this,e);}});};$.extend($.fn,{validateDelegate:function(delegate,type,handler){return this.bind(type,function(event){var target=$(event.target);if(target.is(delegate)){return handler.apply(target,arguments);}});}});})(jQuery);

(function($){var
undefined,dataFlag="watermark",dataClass="watermarkClass",dataFocus="watermarkFocus",dataFormSubmit="watermarkSubmit",dataMaxLen="watermarkMaxLength",dataPassword="watermarkPassword",dataText="watermarkText",selWatermarkDefined=":data("+dataFlag+")",selWatermarkAble=":text,:password,:search,textarea",triggerFns=["Page_ClientValidate"],pageDirty=false;$.extend($.expr[":"],{"search":function(elem){return"search"===elem.type;},"data":function(element,index,matches,set){var data,parts=/^((?:[^=!^$*]|[!^$*](?!=))+)(?:([!^$*]?=)(.*))?$/.exec(matches[3]);if(parts){data=$(element).data(parts[1]);if(data!==undefined){if(parts[2]){data=""+data;switch(parts[2]){case"=":return(data==parts[3]);case"!=":return(data!=parts[3]);case"^=":return(data.slice(0,parts[3].length)==parts[3]);case"$=":return(data.slice(-parts[3].length)==parts[3]);case"*=":return(data.indexOf(parts[3])!==-1);}}
return true;}}
return false;}});$.watermark={version:"3.0.5",options:{className:"watermark",useNative:true},hide:function(selector){$(selector).filter(selWatermarkDefined).each(function(){$.watermark._hide($(this));});},_hide:function($input,focus){if($input.val()==$input.data(dataText)){$input.val("");if($input.data(dataPassword)){if($input.attr("type")==="text"){var $pwd=$input.data(dataPassword),$wrap=$input.parent();$wrap[0].removeChild($input[0]);$wrap[0].appendChild($pwd[0]);$input=$pwd;}}
if($input.data(dataMaxLen)){$input.attr("maxLength",$input.data(dataMaxLen));$input.removeData(dataMaxLen);}
if(focus){$input.attr("autocomplete","off");window.setTimeout(function(){$input.select();},0);}}
$input.removeClass($input.data(dataClass));},show:function(selector){$(selector).filter(selWatermarkDefined).each(function(){$.watermark._show($(this));});},_show:function($input){var val=$input.val(),text=$input.data(dataText),type=$input.attr("type");if(((val.length==0)||(val==text))&&(!$input.data(dataFocus))){pageDirty=true;if($input.data(dataPassword)){if(type==="password"){var $wm=$input.data(dataPassword),$wrap=$input.parent();$wrap[0].removeChild($input[0]);$wrap[0].appendChild($wm[0]);$input=$wm;$input.attr("maxLength",text.length);}}
if((type==="text")||(type==="search")){var maxLen=$input.attr("maxLength");if((maxLen>0)&&(text.length>maxLen)){$input.data(dataMaxLen,maxLen);$input.attr("maxLength",text.length);}}
$input.addClass($input.data(dataClass));$input.val(text);}
else{$.watermark._hide($input);}},hideAll:function(){if(pageDirty){$.watermark.hide(selWatermarkAble);pageDirty=false;}},showAll:function(){$.watermark.show(selWatermarkAble);}};$.fn.watermark=function(text,options){var hasText=(typeof(text)==="string"),hasClass;if(typeof(options)==="object"){hasClass=(typeof(options.className)==="string");options=$.extend({},$.watermark.options,options);}
else if(typeof(options)==="string"){hasClass=true;options=$.extend({},$.watermark.options,{className:options});}
else{options=$.watermark.options;}
if(typeof(options.useNative)!=="function"){options.useNative=options.useNative?function(){return true;}:function(){return false;};}
return this.each(function(){var $input=$(this);if(!$input.is(selWatermarkAble)){return;}
if($input.data(dataFlag)){if(hasText||hasClass){$.watermark._hide($input);if(hasText){$input.data(dataText,text);}
if(hasClass){$input.data(dataClass,options.className);}}}
else{if(options.useNative.call(this,$input)){if(((""+$input.css("-webkit-appearance")).replace("undefined","")!=="")&&($input.attr("tagName")!=="TEXTAREA")){if(hasText){$input.attr("placeholder",text);}
return;}}
$input.data(dataText,hasText?text:"");$input.data(dataClass,options.className);$input.data(dataFlag,1);if($input.attr("type")==="password"){var $wrap=$input.wrap("<span>").parent();var $wm=$($wrap.html().replace(/type=["']?password["']?/i,'type="text"'));$wm.data(dataText,$input.data(dataText));$wm.data(dataClass,$input.data(dataClass));$wm.data(dataFlag,1);$wm.attr("maxLength",text.length);$wm.focus(function(){$.watermark._hide($wm,true);}).bind("dragenter",function(){$.watermark._hide($wm);}).bind("dragend",function(){window.setTimeout(function(){$wm.blur();},1);});$input.blur(function(){$.watermark._show($input);}).bind("dragleave",function(){$.watermark._show($input);});$wm.data(dataPassword,$input);$input.data(dataPassword,$wm);}
else{$input.focus(function(){$input.data(dataFocus,1);$.watermark._hide($input,true);}).blur(function(){$input.data(dataFocus,0);$.watermark._show($input);}).bind("dragenter",function(){$.watermark._hide($input);}).bind("dragleave",function(){$.watermark._show($input);}).bind("dragend",function(){window.setTimeout(function(){$.watermark._show($input);},1);}).bind("drop",function(evt){var dropText=evt.originalEvent.dataTransfer.getData("Text");if($input.val().replace(dropText,"")===$input.data(dataText)){$input.val(dropText);}
$input.focus();});}}
$.watermark._show($input);}).end();};if(triggerFns.length){$(function(){var i,name,fn;for(i=triggerFns.length-1;i>=0;i--){name=triggerFns[i];fn=window[name];if(typeof(fn)==="function"){window[name]=(function(origFn){return function(){$.watermark.hideAll();return origFn.apply(null,Array.prototype.slice.call(arguments));};})(fn);}}});}})(jQuery);

/*
 * http://code.google.com/p/jquery-showhide*/
(function($){$.fn.showhide=function(options){var opts=$.extend({},$.fn.showhide.defaults,options);return $(this).each(function(){var obj=$(this);obj.o=$.meta?$.extend({},opts,$this.data()):opts;if(obj.o.target_obj){obj.o.target=obj.o.target_obj;}else{obj.o.target=obj.next();};show=function(object){object.removeClass(object.o.plus_class);object.addClass(object.o.minus_class);if(object.o.minus_text){object.text(object.o.minus_text);};object.o.target.removeClass(object.o.hide_class);object.o.target.addClass(object.o.show_class);if(obj.o.focus_target){obj.o.focus_target.focus();};};hide=function(object){object.removeClass(object.o.minus_class);object.addClass(object.o.plus_class);if(object.o.plus_text){object.text(object.o.plus_text);};object.o.target.removeClass(object.o.show_class);object.o.target.addClass(object.o.hide_class);};if(obj.o.default_open){show(obj);}
else{hide(obj);}
obj.click(function(){if(obj.o.target.hasClass(obj.o.hide_class)){show(obj);return false;}
else if(obj.o.target.hasClass(obj.o.show_class)){hide(obj);return false;};});});};$.fn.showhide.defaults={target_obj:null,focus_target:null,default_open:true,show_class:'show',hide_class:'hide',plus_class:'plus',plus_text:null,minus_class:'minus',minus_text:null};})(jQuery);

/*
 * jQuery doTimeout: Like setTimeout, but better! - v1.0 - 3/3/2010
 * http://benalman.com/projects/jquery-dotimeout-plugin/
 * 
 * Copyright (c) 2010 "Cowboy" Ben Alman
 * Dual licensed under the MIT and GPL licenses.
 * http://benalman.com/about/license/
 */
(function($){var a={},c="doTimeout",d=Array.prototype.slice;$[c]=function(){return b.apply(window,[0].concat(d.call(arguments)))};$.fn[c]=function(){var f=d.call(arguments),e=b.apply(this,[c+f[0]].concat(f));return typeof f[0]==="number"||typeof f[1]==="number"?this:e};function b(l){var m=this,h,k={},g=l?$.fn:$,n=arguments,i=4,f=n[1],j=n[2],p=n[3];if(typeof f!=="string"){i--;f=l=0;j=n[1];p=n[2]}if(l){h=m.eq(0);h.data(l,k=h.data(l)||{})}else{if(f){k=a[f]||(a[f]={})}}k.id&&clearTimeout(k.id);delete k.id;function e(){if(l){h.removeData(l)}else{if(f){delete a[f]}}}function o(){k.id=setTimeout(function(){k.fn()},j)}if(p){k.fn=function(q){if(typeof p==="string"){p=g[p]}p.apply(m,d.call(n,i))===true&&!q?o():e()};o()}else{if(k.fn){j===undefined?e():k.fn(j===false);return true}else{e()}}}})(jQuery);

var DDDEV = new Object();

if ($("#pl_main").length > 0) {
	DDDEV.ucm = true;	
}
else {
	DDDEV.ucm = false;
}

// Tabs object, currently only present on the sitemap.
DDDEV.tabs = {
		onload 	: function(){
								$('#tabs .tabs a').each(function(i) {
										var e = $(this);
										var target = $(e.attr('href'));
										i == 0 ? e.addClass('active') : target.hide();
										e.bind('click', function(){
											DDDEV.tabs.onclick(e, target);
											return false;
										});
								});
		},
		onclick 	: function(e, target){
								$('#tabs .tabs a').removeClass('active');
								e.addClass('active');
								$('#tabs > div').hide();
								target.show();
		}
}


DDDEV.equalHeight = function(e, start, finish) {
	var tallest = 0;
	start--;
	$(e).slice(start,finish).each(function() {
		if ($(this).height() > tallest) {
			tallest = $(this).height();
		}
	});
	$(e).slice(start,finish).css('min-height',tallest+'px');
}

$(document).ready(function(){
	$('body').addClass('js');

	//hover intent
	var firstTimer;
	var secondTimer;

	$('.suckerfishworld li').removeClass('popout');

	$('.suckerfishworld li.worldbutton').hover(function() {
		if(!$(this).hasClass('fly-outdd')){
			$(this).addClass('waiting wcdeactive');
			firstTimer = setTimeout(function() {
			   $('.suckerfishworld li.waiting').removeClass('wcdeactive').addClass('fly-outdd wcactive');
			}, 400);
		}else{
			clearTimeout(secondTimer);
		}
	},function(){
		if(!$(this).hasClass('fly-outdd')){
			clearTimeout(firstTimer)
			$('.suckerfishworld li.waiting').removeClass('waiting');
		}else{
			secondTimer = setTimeout(function() {
				$('.suckerfishworld li.fly-outdd').removeClass('fly-outdd wcactive waiting');
			}, 400);
		}
	});
	
	$('.homesuckerfish > li').removeClass('popout');
	
	$('.homesuckerfish > li').hover(function() {
		if(!$(this).hasClass('fly-outdd')){
			$(this).addClass('waiting');
			firstTimer = setTimeout(function() {
			   $('.homesuckerfish > li.waiting').addClass('fly-outdd');
			}, 400);
		}else{
			clearTimeout(secondTimer);
		}
	},function(){
		if(!$(this).hasClass('fly-outdd')){
			clearTimeout(firstTimer)
			$('.homesuckerfish > li.waiting').removeClass('waiting');
		}else{
			secondTimer = setTimeout(function() {
				$('.homesuckerfish > li.fly-outdd').removeClass('fly-outdd waiting');
			}, 400);
		}
	});
	
	$('.content_boxes > ul.featured_boxes > li:even').css({
		clear: 'left'
	});
	
	
	

	//Featured boxes
	$('.partners_box ul').each(function() {	
		var pb_items = $('li',this).length;		
		if(pb_items == 6) {$(this).addClass('custom');} 
		else if (pb_items == 5) {$(this).addClass('custom');}
	});
	
	// Checkbox tables for multilang
	$(".checkbox_results th.col_sml").each(function() {
		if ($("span",this).width() > 50) {
			$(this).width($("span",this).width());
		}		
	});
	
	// Tabs
	$('#tabs').length > 0 ? DDDEV.tabs.onload() : 0;
	
	//External links
	$('.featured_boxes a, .side_box li a,.content_boxes a, .info_box a,.main_box a').filter(function(){
		return this.hostname !== location.hostname
	}).each(function() {
		if (!$(this).children().is('img')){
			$(this).addClass("external");
		}
		$(this).attr('target','_blank').append('<span class="access"> ('+ HAYS.ww_newWindow +')</span>');
	});
	

	/*Multiselect*/
	DDDEV.multiselects = $('.multiselect');
	if (DDDEV.multiselects.length > 0 && DDDEV.ucm) {
		$(DDDEV.multiselects).each(function() {
			var multi_object = $(this).get();		
			$(multi_object).prepend('<div class="jaccess multitoggle" tabindex="0"></div>');
			if ($(multi_object).find('input').is(':checked')) {	
				// This function needs to be combined with other to form one that will rule them all. 
				var ms_selected  = '';
				var ms_value ='';
				$(multi_object).find('input:checked').each (function(i) {
					if ($(this).attr('name') != 'check_all') {
						if (i > 0){
							ms_selected = HAYS.ww_multiOptions;
						}
						else {
							ms_selected = ms_selected + $('label[for='+$(this).attr('id')+']',multi_object).text();
						}
						ms_value = ms_value + $(this).attr('value') + ', ';
					}
				});
				$(multi_object).find('.multitoggle').text(ms_selected);
				
				$(multi_object).find('input[type=hidden]').val(ms_value.substring(0, ms_value.length - 2));
					
					}
			else {
				$(multi_object).find('.multitoggle').text($(multi_object).attr('title'));
			}
			/*//commented by devendra for the release 7 requirement start
			if ($(this).parents('form').hasClass('hays_form') && !$(this).find('input').is(':radio')) {
				$(this).children('ol').prepend('<li class="checkbox_row"><input type="checkbox" name="check_all" id="option" value="'+ HAYS.ww_checkAll +'"/><label for="option">'+ HAYS.ww_all +'<span class="watermark">('+ HAYS.ww_egIndustries +')</span></label></li>');	
			}
			//commented by devendra for the release 7 requirement end	*/		
			$('.multitoggle',this).click (function() {			
				$(this).hasClass('ms_hover') ? $(this).removeClass('ms_hover').next('ol').removeAttr('style') : $(this).addClass('ms_hover').next('ol').css('visibility', 'visible');
			});	
			$('.multitoggle',this).keypress (function(e) {
				e.keyCode == 13 ? $(this).trigger('click') : 0;
			});
			$('input',this).click(function() {
				var ms_selected = '';
				var ms_value = '';
				var ms_valuelocationid = '';
				if ($(this).attr('name') == 'check_all') {
					$(this).hasClass('checked') ? $(this).removeClass('checked').next('label').html(HAYS.ww_all +' <span class="watermark">('+ HAYS.ww_egIndustries +')</span>') : $(this).addClass('checked').next('label').text(HAYS.ww_unSelAll);
				 	$(this).parents('fieldset:eq(0)').find(':checkbox').attr('checked', this.checked);	
				}
				// Function repeat, needs consolidating with above line 236
				$(':checked',multi_object).each (function(i) {
					if ($(this).attr('name') != 'check_all') {
						if (i > 0){
							ms_selected = 'Multiple options selected';
						}
						else {
							ms_selected = ms_selected + $('label[for='+$(this).attr('id')+']',multi_object).text();
						}
						ms_value = ms_value + $(this).attr('value') + ', ';
						ms_valuelocationid = ms_valuelocationid + $(this).attr('value').split("&",1) + ', ';
					}
				});	
				$('.multitoggle',multi_object).text(ms_selected);
				$('.require_group',multi_object).val(ms_value.substring(0, ms_value.length - 2));
				$('input[id=locationid]',multi_object).val(ms_valuelocationid.substring(0, ms_valuelocationid.length - 2));
			});
		});
		$(document).bind('click keyup', function(e) {
			!$(e.target).is('.multiselect *') ? $('.multitoggle').removeClass('ms_hover').next('ol').removeAttr('style') : 0;
		});
	}
	
	
	
	
/*-------New homepage release 6 equal height of boxes-------*/
	/* .grid_4col Equal heights */
	$('.grid_4col').each (function() { // For each grid_4col on-screen
		HAYS.grid_4col = $('.promo_box',this);
		if ($(this).parent().attr('id') == 'pg_home') { 
			for (i=1;i<HAYS.grid_4col.length;i=i+2) {
				DDDEV.equalHeight(HAYS.grid_4col,i,i+1);
			}
		}		
		else {
			for (i=1;i<HAYS.grid_4col.length;i=i+4) {
				DDDEV.equalHeight(HAYS.grid_4col,i,i+3);
			}
		}
	});
	
	/* .grid_3col Equal heights */
	$('.grid_3col').each (function() { // For each grid_3col on-screen
		HAYS.grid_3col = $('.promo_box',this);
		if ($(this).parent().attr('id') == 'pg_home') { 
			for (i=1;i<HAYS.grid_3col.length;i=i+2) {
				DDDEV.equalHeight(HAYS.grid_3col,i,i+1);
			}
		}		
		else {
			for (i=1;i<HAYS.grid_3col.length;i=i+3) {
				DDDEV.equalHeight(HAYS.grid_3col,i,i+2);
			}
		}
	});
	
	/* .grid_2col Equal heights */
	$('.grid_2col').each (function() { // For each grid_2col on-screen
		HAYS.grid_2col = $('.promo_box',this);
		if ($(this).parent().attr('id') == 'pg_home') { 
			for (i=1;i<HAYS.grid_2col.length;i=i+2) {
				DDDEV.equalHeight(HAYS.grid_2col,i,i+1);
			}
		}		
		else {
			for (i=1;i<HAYS.grid_2col.length;i=i+2) {
				DDDEV.equalHeight(HAYS.grid_2col,i,i+1);
			}
		}
	});
	

	/* grid_33 Equal heights */
	$('.grid_33').each (function() { // For each grid_33 on-screen
		HAYS.grid_33 = $('.promo_box',this);
		if ($(this).parent().attr('id') == 'Expertise') { 
			for (i=1;i<HAYS.grid_33.length;i=i+2) {
				DDDEV.equalHeight(HAYS.grid_33,i,i+1);
			}
		}
		else {
			for (i=1;i<HAYS.grid_33.length;i=i+3) {
				DDDEV.equalHeight(HAYS.grid_33,i,i+2);
			}
		}
		
		HAYS.nav_33 = $('> li > a',this);
		for (i=1;i<HAYS.nav_33.length;i=i+3) {
			DDDEV.equalHeight(HAYS.nav_33,i,i+2);
		}		
	});
	
	/* grid_50 Equal heights*/
	$('.grid_50').each (function() {
		HAYS.grid_50 = $('.promo_box',this);
		for (i=1;i<HAYS.grid_50.length;i=i+2) {
			DDDEV.equalHeight(HAYS.grid_50,i,i+1);
		}
		HAYS.nav_50 = $('> li > a',this);
		for (i=1;i<HAYS.nav_50.length;i=i+2) {
			DDDEV.equalHeight(HAYS.nav_50,i,i+1);
		}
	});
	
	/* grid_25 Equal heights*/
	$('.grid_25').each (function() {
		HAYS.grid_25 = $('.promo_box',this);
		for (i=1;i<HAYS.grid_25.length;i=i+3) {
			DDDEV.equalHeight(HAYS.grid_25,i,i+3);
		}
	});
	
	$('.grid_10').each (function() {
		HAYS.jf_inner = $('.jf_inner',this);
		for (i=1;i<HAYS.jf_inner.length;i=i+2) {
			DDDEV.equalHeight(HAYS.jf_inner,i,i+1);
		}
	});
	
	/* Toggle*/
	DDDEV.toggle = $('.toggle');
	if (DDDEV.toggle.length > 0 && DDDEV.ucm) {
		$(DDDEV.toggle).each(function(t) {									  
			$('.thead',this).each(function(i) {
				var state = $(this).hasClass('show') ? HAYS.ww_hideText : HAYS.ww_showText;
				!$(this).hasClass('show') ? $(this).next().addClass('access') : 0;
				$(this).append('<a href="#toggle'+t+'_pane'+i+'"><span>'+state+'<span class="access"> '+$(this).text()+'</span></span></a>').next().attr('id',('toggle'+t+'_pane'+i));
			});
		});
		$('.toggle .thead a').click (function() {
			var target = $(this).attr('href').slice($(this).attr('href').indexOf('#'));
			var accessText = '<span class="access"> '+$(this).prev().text()+'</span>';
			$(this).parent().hasClass('show') ? $(target).addClass('access') && $(this).html('<span>'+ HAYS.ww_showText+accessText+'</span>').parent().removeClass('show') : $(target).removeClass('access') && $(this).html('<span>'+HAYS.ww_hideText+accessText+'</span>').parent().addClass('show');
			return false;
		});
	}	
	
	/*Search filters*/
	DDDEV.search_filters = $('.ehc_box form');
	if (DDDEV.search_filters.length > 0) {
		$(DDDEV.search_filters).each(function() {
			var ehc_delivered = $('input.ehc',this).val();
			if (ehc_delivered !== '') {
				ehc_delivered = ehc_delivered + ', ';
			}
			var ehc_object = $(this).get();
			$('input[type=checkbox]',this).click(function() {
				var ehc_selected = ehc_delivered;	
				$(':checked',ehc_object).each(function() {
					ehc_selected = ehc_selected + $(this).attr('value') + ', ';
					
				});
				$('input.ehc',ehc_object).val(ehc_selected.substring(0, ehc_selected.length - 2));
			});
			$('.clear_ehc',this).click(function() {
				$('input.ehc',ehc_object).val('clear');
				$(ehc_object).submit();
				return false;
			});
			if ($(this).is('.select')) {
				$('input[type=radio]',this).click(function() {
					var location_coords = $(this).val().split(',');
					$('input[name=level_filter]',ehc_object).val(location_coords[0]);
					$('input[name=ne_longitude_filter]',ehc_object).val(location_coords[1]);
					$('input[name=ne_latitude_filter]',ehc_object).val(location_coords[2]);
					$(ehc_object).submit();
				});
			}
		});
	}
	DDDEV.suckerfish = $('#suckerfish');
	if (DDDEV.suckerfish.length > 0) {
		var sfEls = document.getElementById("suckerfish").getElementsByTagName("LI");
		for (var i=0; i<sfEls.length; i++) {
			sfEls[i].onmouseover=function() {
				this.className+=" sfhover";
			}
			sfEls[i].onmouseout=function() {
				this.className=this.className.replace(new RegExp(" sfhover\\b"), "");
			}
		}
	}
	
	//Top jobs
	DDDEV.scrolllist = $('.scrolllist');
	if (DDDEV.scrolllist.length > 0) {
		DDDEV.scrolllist.each(function(){
			$(this).children('li').length > 3 ? $(this).addClass('scrollcss') : 0;
		})
	}
	
	//Add this plugin
	$(".addthis_toolbox").append('<script type="text/javascript" src="/haysassets/HaysGeneralComponent/assets/js/addthis_widget.js"></script>');
	
	//IE 7.0 dialog button fix wrapping
	$("#dialog,#dialog_1,#dialog_2").bind("dialogopen", function(event,ui) {
		$(this).next().children("button").each(function() {
			if ($.browser.msie && (parseInt($.browser.version, 10)===7) && ($(this).width() > 83)) {
				$(this).wrapInner("<div class=\"ie7\" />").width("100%");
			}
		});
	});
	
 });

jQuery.validator.addMethod("require_from_group", function(value, element, options) {
    numberRequired = options[0];
    selector = options[1];
    //Look for our selector within the parent form
	
    var validOrNot = $(selector, element.form).filter(function() {
         // Each field is kept if it has a value
		return $(this).not('.watermark,[type=checkbox]').val();
         // Set to true if there are enough, else to false
    }).length >= numberRequired;
	
    return validOrNot;
    // {0} below is the 0th item in the options field
    }, jQuery.format("Please fill out at least {0} of these fields.")
);

$.validator.addMethod("nowatermark", function(value,element) {
	return $(element).is(':not(.watermark)'); //Booommmmmmmmmmmmmmmmmmm
});

$.validator.addMethod("ukpostcode", function(value) {
	return /^([a-z][a-z]?\d\d?[a-z]?\u0020?\d[a-z][a-z])$/i.test(value);
});

// custom code for decimal
$.validator.addMethod("decimal", function(value) {
  //return /^\d*\.?([0-9][0-9])?$/i.test(value);
  ///^\d*\.?([0-9]{1,2})?$/i.test(value); -- working latest
  if (value.length > 0) {
    return /^\d*(\.[0-9]{1,2})?$/i.test(value);
  } else {
    return true;
  }
});


// custom code for custompassword
$.validator.addMethod("custompassword", function(value) { 
  if (value.length == 0) {
      return true;
  }
  else if (value.length < 6 || value.length > 20) {
      return false;
  }
  else {
	//check for having atleast one number
    return /[0-9]+/i.test(value);
  }
});

//Custom code for telephone
$.validator.addMethod("telnumber", function(value) {
	  if (value.length > 0) {
	return /^([\+][0-9]{1,3}([ \.\-])?)?([\(]{1}[0-9]{3}[\)])?([0-9A-Z \.\-]{1,32}([ \s\.\-])?)((x|ext|extension)?[0-9]{1,4}?)$/i.test(value);
	  } else {
    return true;
  }
});


if ($("#pl_main").length > 0) {
	jQuery.extend(jQuery.validator.messages, {
		nowatermark : 'This field is required.',
		//ukpostcode: 'Please enter a valid UK postcode.',
		decimal: 'Please enter numbers only.',
		//custompassword : 'Password must be between 6 and 20 digits long and include at least one numeric digit.',
		//telnumber : 'Please enter a valid number.',
		digits : 'Please enter only numbers with no spaces.',
		min : 'Max should be higher than Min.',
		max : 'Max should be higher than Min.'
	});
}

/*Add to homepage to stop it refreshing*/
$('.worldbutton a').click(function() {
	$(this).css('outline','none');
	return false;
});

if ($('.subspecialbox').length > 0){
	//console.log('subspecialbox exists');
	$('.subspecialbox').css('height', ($('.home_panel2_inner').height()-2));

	$('.subspecialbox').each(function(){
		
		if ($('.specialcol',this).length > 1	)
			{	
				$(this).css('width', '380px')
			}
		if ($('.specialcol',this).length > 2	)
		{	
			$(this).css('width', '570px')
		}
	});
}

var checkheight = $('.half_intro').height();
var checkimgheight = $('.half_intro img').height();


/**
 * Cookie plugin
 *
 * Copyright (c) 2006 Klaus Hartl (stilbuero.de)
 * Dual licensed under the MIT and GPL licenses:
 * http://www.opensource.org/licenses/mit-license.php
 * http://www.gnu.org/licenses/gpl.html
 *
 */

/**
 * Create a cookie with the given name and value and other optional parameters.
 *
 * @example $.cookie('the_cookie', 'the_value');
 * @desc Set the value of a cookie.
 * @example $.cookie('the_cookie', 'the_value', { expires: 7, path: '/', domain: 'jquery.com', secure: true });
 * @desc Create a cookie with all available options.
 * @example $.cookie('the_cookie', 'the_value');
 * @desc Create a session cookie.
 * @example $.cookie('the_cookie', null);
 * @desc Delete a cookie by passing null as value. Keep in mind that you have to use the same path and domain
 *       used when the cookie was set.
 *
 * @param String name The name of the cookie.
 * @param String value The value of the cookie.
 * @param Object options An object literal containing key/value pairs to provide optional cookie attributes.
 * @option Number|Date expires Either an integer specifying the expiration date from now on in days or a Date object.
 *                             If a negative value is specified (e.g. a date in the past), the cookie will be deleted.
 *                             If set to null or omitted, the cookie will be a session cookie and will not be retained
 *                             when the the browser exits.
 * @option String path The value of the path atribute of the cookie (default: path of page that created the cookie).
 * @option String domain The value of the domain attribute of the cookie (default: domain of page that created the cookie).
 * @option Boolean secure If true, the secure attribute of the cookie will be set and the cookie transmission will
 *                        require a secure protocol (like HTTPS).
 * @type undefined
 *
 * @name $.cookie
 * @cat Plugins/Cookie
 * @author Klaus Hartl/klaus.hartl@stilbuero.de
 */

/**
 * Get the value of a cookie with the given name.
 *
 * @example $.cookie('the_cookie');
 * @desc Get the value of a cookie.
 *
 * @param String name The name of the cookie.
 * @return The value of the cookie.
 * @type String
 *
 * @name $.cookie
 * @cat Plugins/Cookie
 * @author Klaus Hartl/klaus.hartl@stilbuero.de
 */
jQuery.cookie = function(name, value, options) {
    if (typeof value != 'undefined') { // name and value given, set cookie
        options = options || {};
        if (value === null) {
            value = '';
            options.expires = -1;
        }
        var expires = '';
        if (options.expires && (typeof options.expires == 'number' || options.expires.toUTCString)) {
            var date;
            if (typeof options.expires == 'number') {
                date = new Date();
                date.setTime(date.getTime() + (options.expires * 24 * 60 * 60 * 1000));
            } else {
                date = options.expires;
            }
            expires = '; expires=' + date.toUTCString(); // use expires attribute, max-age is not supported by IE
        }
        // CAUTION: Needed to parenthesize options.path and options.domain
        // in the following expressions, otherwise they evaluate to undefined
        // in the packed version for some reason...
        var path = options.path ? '; path=' + (options.path) : '';
        var domain = options.domain ? '; domain=' + (options.domain) : '';
        var secure = options.secure ? '; secure' : '';
        document.cookie = [name, '=', encodeURIComponent(value), expires, path, domain, secure].join('');
    } else { // only name given, get cookie
        var cookieValue = null;
        if (document.cookie && document.cookie != '') {
            var cookies = document.cookie.split(';');
            for (var i = 0; i < cookies.length; i++) {
                var cookie = jQuery.trim(cookies[i]);
                // Does this cookie string begin with the name we want?
                if (cookie.substring(0, name.length + 1) == (name + '=')) {
                    cookieValue = decodeURIComponent(cookie.substring(name.length + 1));
                    break;
                }
            }
        }
        return cookieValue;
    }
};