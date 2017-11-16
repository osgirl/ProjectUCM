package hays.co.uk.solar;

import static hays.com.commonutils.HaysWebApiUtils.HandleExceptions;
import static hays.com.commonutils.HaysWebApiUtils.createResultSetFromData;
import static intradoc.shared.SharedObjects.getEnvironmentValue;
import hays.com.commonutils.HaysWebApiUtils;
import intradoc.common.LocaleUtils;
import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.data.DataException;
import intradoc.data.DataResultSet;
import intradoc.server.ServiceHandler;

import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document; 
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class HaysApiSolarSearch extends ServiceHandler {
	public final static String TRACE_NAME = "hays_solar_search";
	private ArrayList<HashMap<String, String>> lSolarResultObject = null;

	public static final String STR_TAG = "str";
	public static final String NAME = "name";
	public boolean noResults = false;
	String parmalink = null;
	
	
	public void processSolrSearch() throws ServiceException, DataException, MalformedURLException, IOException 
	{
		
		parmalink = getData("path");
		SystemUtils.trace(TRACE_NAME, " parmalink: "+parmalink);
		
		//1/HAYS_10001/privacy-policy
		
		parmalink = "\"/"+parmalink+"\"";
		SystemUtils.trace(TRACE_NAME, " parmalink: "+parmalink);
		String solrSearchUrl = getEnvironmentValue("SolrSearchAction");
		SystemUtils.trace(TRACE_NAME, " solrSearchUrl: "+solrSearchUrl);

		String useProxy = getEnvironmentValue("SolrUseProxy");
		SystemUtils.trace(TRACE_NAME, "useProxy : " + useProxy);
		boolean isProxy = false;
		if (useProxy != null && "TRUE".equalsIgnoreCase(useProxy)) {
			isProxy = true;
		}
		SystemUtils.trace(TRACE_NAME, "isProxy : " + isProxy);
		String solrSearchExtraParams = null;

		solrSearchUrl = solrSearchUrl + parmalink;

		SystemUtils.trace(TRACE_NAME, "Solr Search URL after appending parmalink : " + solrSearchUrl);

		if (isProxy) 
		{
			SystemUtils.trace(TRACE_NAME, "cookieHandler" );
			CookieHandler.setDefault(new CookieManager(null,CookiePolicy.ACCEPT_ALL));

			String PROXY_HOST = getEnvironmentValue("PROXY_HOST");
			String PROXY_PORT = getEnvironmentValue("PROXY_PORT");

			final String authUser = getEnvironmentValue("PROXY_AUTH_USER");
			final String authPassword = getEnvironmentValue("PROXY_AUTH_PASSWORD");

			Authenticator.setDefault(new Authenticator() {
				public PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(authUser, authPassword
						.toCharArray());
				}
			});

			System.setProperty("http.proxyUser", authUser);
			System.setProperty("http.proxyPassword", authPassword);

			System.setProperty("http.proxyHost", PROXY_HOST);
			System.setProperty("http.proxyPort", PROXY_PORT);
			System.setProperty("http.agent",
				"Mozilla/5.0 (Windows NT 5.1; rv:12.0) Gecko/20100101 Firefox/12.0");
			SystemUtils.trace(TRACE_NAME, "http.proxyUser : " + authUser);
			SystemUtils.trace(TRACE_NAME, "http.proxyPassword : " + authPassword);
			SystemUtils.trace(TRACE_NAME, "http.proxyHost : " + PROXY_HOST);
			SystemUtils.trace(TRACE_NAME, "http.proxyPort : " + PROXY_PORT);
		}
		
		solrSearchUrl = solrSearchUrl.replaceAll(" ", "+");
		URL url = null;
		if (isProxy) {
			url = new URL(null, solrSearchUrl,
					new sun.net.www.protocol.http.Handler());
		} else {
			url = new URL(solrSearchUrl);			
		}

		SystemUtils.trace(TRACE_NAME, "URL : " + url);
		URLConnection conn = null;
		if (isProxy) {
			conn = url.openConnection();
		} else {
			conn = url.openConnection(Proxy.NO_PROXY);
		}
		SystemUtils.trace(TRACE_NAME, "Connection : " + conn);

		conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.setUseCaches(false);
		conn.setRequestProperty("Content-Type", "application/xml");
		conn.setRequestProperty("User-Agent",
				"Mozilla/5.0 (Windows NT 5.1; rv:12.0) Gecko/20100101 Firefox/12.0");
		
		InputStream input = conn.getInputStream();
		SystemUtils.trace(TRACE_NAME, "GotInputStream.");
		try {
			readSolarOutput(input);
		} catch (ParserConfigurationException e) {
			HandleExceptions(m_binder, "UC013", "wwFileNotParse");
		} catch (SAXException e) {
			HandleExceptions(m_binder, "UC012", "wwFileNotFormedProperly");
		} catch (IOException e) {
			HandleExceptions(m_binder, "UC011", "wwFileNotFound");
		} catch (Exception e) {
			noResults = true;
		}
		SystemUtils.trace(TRACE_NAME, "Read solr output successfully.");
		convertToUCMFormat();
		SystemUtils.trace(TRACE_NAME, "Converted the output to UCM Format.");
		input.close();
		this.m_binder.putLocal("StatusMessage",
				LocaleUtils.encodeMessage("wwWebApiOKMsg", null));
		this.m_binder.putLocal("StatusCode", "UC000");
	}

	private void convertToUCMFormat() {
		if (!noResults) {
			ArrayList<String> lHeaderList = new ArrayList<String>();
			lHeaderList.add("id");
			lHeaderList.add("source");
			lHeaderList.add("parma-link");
			lHeaderList.add("url");
			lHeaderList.add("domain");
			lHeaderList.add("locale");
			lHeaderList.add("content_type");
			lHeaderList.add("client");
			lHeaderList.add("author");
			lHeaderList.add("title");
			lHeaderList.add("unFormattedContent");

			ArrayList<String> lAttrList = new ArrayList<String>();
			for (HashMap<String, String> lRMap : getlSolrResultObject()) {
				lAttrList.add(lRMap.get("id"));
				lAttrList.add(lRMap.get("source"));
				lAttrList.add(lRMap.get("parma-link"));
				lAttrList.add(lRMap.get("url"));
				lAttrList.add(lRMap.get("domain"));
				lAttrList.add(lRMap.get("locale"));
				lAttrList.add(lRMap.get("content_type"));
				lAttrList.add(lRMap.get("client"));
				lAttrList.add(lRMap.get("author"));
				lAttrList.add(lRMap.get("title"));
				lAttrList.add(lRMap.get("unFormattedContent"));
			}
			DataResultSet lSearchResult = (DataResultSet) createResultSetFromData(
					lHeaderList, lAttrList);
			m_binder.addResultSet("SearchResults", lSearchResult);
			m_binder.putLocal("TotalRows",
					Integer.toString(lSearchResult.getNumRows()));
		} else {
			DataResultSet lEmptyResultset = new DataResultSet();
			m_binder.addResultSet("SearchResults", lEmptyResultset);
			m_binder.addResultSet("NavigationPages", lEmptyResultset);
			m_binder.putLocal("TotalRows", "0");
		}
	}

	public void readSolarOutput(InputStream pIN)
			throws ParserConfigurationException, SAXException, IOException,
			XPathExpressionException {
		XPath xpath = null;
		XPathExpression expr = null;
		Node docNode = null;
		Node strNode = null;
		Element docElement = null;
		Element strElement = null;
		NodeList strNodeList = null;
		NodeList docNodeList = null;
		ArrayList<HashMap<String, String>> lSolrResultObject = new ArrayList<HashMap<String, String>>();
		HashMap<String, String> lRMap = null;

		Document lDocument = HaysWebApiUtils.parseXML(pIN);
		xpath = XPathFactory.newInstance().newXPath();

		expr = xpath.compile("//response/result/doc");
		Object docNodeSet = expr.evaluate(lDocument, XPathConstants.NODESET);
		expr = xpath.compile(STR_TAG);
		docNodeList = (NodeList) docNodeSet;
		for (int i = 0; i < docNodeList.getLength(); i++) {
			docNode = docNodeList.item(i);
			if (docNode != null && docNode.getNodeType() == Node.ELEMENT_NODE) {
				lRMap = new HashMap<String, String>();
				docElement = (Element) docNode;
				strNodeList = (NodeList) expr.evaluate(docElement,
						XPathConstants.NODESET);
				for (int j = 0; j < strNodeList.getLength(); j++) {
					strNode = strNodeList.item(j);
					if (strNode != null
							&& strNode.getNodeType() == Node.ELEMENT_NODE) {
						strElement = (Element) strNode;
						lRMap.put(strElement.getAttribute(NAME),
								strElement.getTextContent());
					}
				}
			}
			lSolrResultObject.add(lRMap);
		}
		setlSolrResultObject(lSolrResultObject);

	}

	public String getData(String pParamName) {
		String returnString = "";
		try {
			returnString = m_binder.get(pParamName);
			if ("null".equalsIgnoreCase(returnString)) {
				returnString = "";
			}
			returnString = returnString.trim();
		} catch (Exception e) {

		}
		return returnString;
	}

	public ArrayList<HashMap<String, String>> getlSolrResultObject() {
		return lSolarResultObject;
	}

	public void setlSolrResultObject(
			ArrayList<HashMap<String, String>> lSolrResultObject) {
		this.lSolarResultObject = lSolrResultObject;
	}

}
