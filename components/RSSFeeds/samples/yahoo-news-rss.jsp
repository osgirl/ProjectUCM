<%@ page import="idcserver.*" %>
<jsp:useBean id = "sb" scope="page" class="idcserver.ServerBean" />
<%
	sb.init(request);
	String feedUrl = "http://rss.news.yahoo.com/rss/topstories";
	sb.putLocal("feedUrl", feedUrl);
	sb.putLocal("isAbsoluteWeb", "1");
	sb.putLocal("xslUrl", sb.evalIdcScp("HttpWebRoot") +
		"resources/xsl/yahoo-news-rss.xsl");
	sb.putLocal("parseXml", "0");
	String cgiPath = sb.evalIdcScp("HttpCgiPath");
%>
<html>
<head>
<title>Stellent RSS Feed Samples</title>
</head>
<body>

<h1>Stellent RSS Feed Samples</h1>

<h3>Links to Feeds</h3>
<p><a href="<%=feedUrl%>"><%=feedUrl%></a></p>

<p><a href="<%=cgiPath%>?IdcService=GET_REMOTE_FEED&parseXml=0&feedUrl=<%=feedUrl%>">
Remote RSS feed, cached in Stellent</a></p>

<p><a href="<%=cgiPath%>?IdcService=GET_REMOTE_FEED&parseXml=1&feedUrl=<%=feedUrl%>">
RSS Feed with simplified Stellent template</a></p>

<p><a href="<%=cgiPath%>?IdcService=GET_REMOTE_FEED&parseXml=1&IsJava=1&feedUrl=<%=feedUrl%>">
Data from the RSS feed, parsed and displayed in HDA format</a></p>

<p><a href="<%=cgiPath%>?IdcService=GET_REMOTE_FEED&parseXml=1&IsSoap=1&feedUrl=<%=feedUrl%>">
Data from the RSS feed, parsed and displayed in SOAP format</a></p>


<h3>RSS feed transformed into HTML with the XSL stylesheet</h3>
<div style="border:1px solid black; height:300px; overflow:scroll">
<%
	String htmlData = sb.evalIdcScp("rssTransform(feedUrl, xslUrl)");
	out.println(htmlData);
%>
</div>


<h3>RSS feed parsed, and displayed with IdocScript</h3>
<div style="border:1px solid black; height:300px; overflow:scroll">
<%
	sb.putLocal("parseXml", "1");
	sb.evalIdcScp("executeService('GET_REMOTE_FEED')");
%>

<a href="<%=sb.getLocal("channelLink")%>">
	<h1><%=sb.getLocal("channelTitle")%></h1></a>
<%
	ServerResultSet entries = sb.getResultSet("RSS_ENTRIES");
	for (entries.first(); entries.isRowPresent(); entries.next())
	{
		out.println("<h3><a href=\"" + entries.getStringValue("link") +
			"\">" + entries.getStringValue("title") + "</a></h3>" +
			entries.getStringValue("description"));
	}
%>
</div>

</body>
</html>