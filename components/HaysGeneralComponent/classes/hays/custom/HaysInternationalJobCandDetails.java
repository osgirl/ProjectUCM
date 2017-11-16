package hays.custom;

import static hays.com.commonutils.HaysWebApiUtils.HandleExceptions;
import static hays.com.commonutils.HaysWebApiUtils.isNotNull;
import static intradoc.shared.SharedObjects.getEnvironmentValue;
import hays.com.commonutils.HaysWebApiUtils;
import hays.com.commonutils.NamespaceResolver;
import intradoc.common.LocaleUtils;
import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.data.DataException;
import intradoc.data.DataResultSet;
import intradoc.data.ResultSet;
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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class HaysInternationalJobCandDetails extends ServiceHandler
{
	public final static String TRACE_NAME = "HIJCDetails";
	private static HashMap<String, HashMap<String, String>> lAPIResultObject = null;
	public boolean noResults = false;

	public void getIntJobCandDetails() throws ServiceException, DataException, MalformedURLException, IOException
	{
		String restJobDetailURL = null;
		boolean isProxy = false;
		String useProxy = null;
		String localeRecordId = getData("localeRecordId");
		String urlToPick = null;
		String derivedLocale = null;
		if (isNotNull(localeRecordId))
		{
			int start = localeRecordId.substring(0, localeRecordId.lastIndexOf("-")).lastIndexOf("-");
			int stop = localeRecordId.indexOf("_");
			if (!(start >= 0))
			{
				start = 0;
			}
			else
			{
				start += 1;
			}
			derivedLocale = localeRecordId.substring(start, stop);
			urlToPick = getDerivedLocaleDetails(derivedLocale);
		}
		SystemUtils.trace(TRACE_NAME, "localeRecordId : " + localeRecordId);
		SystemUtils.trace(TRACE_NAME, "derivedLocale : " + derivedLocale);
		SystemUtils.trace(TRACE_NAME, "urlToPick : " + urlToPick);
		m_binder.putLocal("urlToPick", urlToPick);
		if (isNotNull(urlToPick) && urlToPick.equalsIgnoreCase("apac"))
		{			
			restJobDetailURL = getEnvironmentValue("INT_JD_REST_URL_APAC");
			useProxy = getEnvironmentValue("INT_USE_PROXY_APAC");
		}
		else
		{
			restJobDetailURL = getEnvironmentValue("INT_JD_REST_URL_UK");
			useProxy = getEnvironmentValue("INT_USE_PROXY_UK");
		}
		restJobDetailURL = restJobDetailURL.replace("JOB_ID", getData("JOB_ID"));
		SystemUtils.trace(TRACE_NAME, "useProxy : " + useProxy);
		if (useProxy != null && "TRUE".equalsIgnoreCase(useProxy))
		{
			isProxy = true;
		}

		SystemUtils.trace(TRACE_NAME, "Rest Job Detail URL : " + restJobDetailURL);

		CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
		String PROXY_HOST = getEnvironmentValue("PROXY_HOST");
		String PROXY_PORT = getEnvironmentValue("PROXY_PORT");
		final String authUser = getEnvironmentValue("PROXY_AUTH_USER");
		final String authPassword = getEnvironmentValue("PROXY_AUTH_PASSWORD");
		Authenticator.setDefault(new Authenticator()
		{
			public PasswordAuthentication getPasswordAuthentication()
			{
				return new PasswordAuthentication(authUser, authPassword.toCharArray());
			}
		});

		System.setProperty("http.proxyUser", authUser);
		System.setProperty("http.proxyPassword", authPassword);
		System.setProperty("http.proxyHost", PROXY_HOST);
		System.setProperty("http.proxyPort", PROXY_PORT);
		SystemUtils.trace(TRACE_NAME, "http.proxyUser : " + authUser);
		SystemUtils.trace(TRACE_NAME, "http.proxyPassword : " + authPassword);
		SystemUtils.trace(TRACE_NAME, "http.proxyHost : " + PROXY_HOST);
		SystemUtils.trace(TRACE_NAME, "http.proxyPort : " + PROXY_PORT);
		restJobDetailURL = restJobDetailURL.replaceAll(" ", "+");
		URL url = null;
		if (isProxy)
		{
			url = new URL(null, restJobDetailURL, new sun.net.www.protocol.http.Handler());
		}
		else
		{
			url = new URL(restJobDetailURL);
		}

		SystemUtils.trace(TRACE_NAME, "URL : " + url);
		URLConnection conn = null;
		if (isProxy)
		{
			conn = url.openConnection();
		}
		else
		{
			conn = url.openConnection(Proxy.NO_PROXY);
		}
		SystemUtils.trace(TRACE_NAME, "Connection : " + conn);

		conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.setUseCaches(false);
		// conn.setRequestProperty("Content-Type", "application/xml");
		// conn.setRequestProperty("User-Agent",
		// "Mozilla/5.0 (Windows NT 5.1; rv:12.0) Gecko/20100101 Firefox/12.0");

		InputStream input = conn.getInputStream();
		SystemUtils.trace(TRACE_NAME, "GotInputStream.");
		try
		{
			readAPIOutput(input);
		}
		catch (ParserConfigurationException e)
		{
			HandleExceptions(m_binder, "UC013", "wwFileNotParse");
		}
		catch (SAXException e)
		{
			HandleExceptions(m_binder, "UC012", "wwFileNotFormedProperly");
		}
		catch (IOException e)
		{
			HandleExceptions(m_binder, "UC011", "wwFileNotFound");
		}
		catch (Exception e)
		{
			noResults = true;
		}
		SystemUtils.trace(TRACE_NAME, "Read api output successfully.");
		convertToResultset();
		SystemUtils.trace(TRACE_NAME, "Converted the output to resultset.");
		input.close();
		this.m_binder.putLocal("StatusMessage", LocaleUtils.encodeMessage("wwWebApiOKMsg", null));
		this.m_binder.putLocal("StatusCode", "UC000");
	}

	public static void readAPIOutput(InputStream pIN) throws ParserConfigurationException, SAXException, IOException,
			XPathExpressionException
	{
		XPath xpath = null;
		XPathExpression exprResultSets = null;
		XPathExpression exprAttributes = null;
		Node nodeTemp = null;
		Node nodeAttribs = null;
		Node nodeResultSet = null;
		NodeList nodeListResultset = null;
		NodeList nodeListAttribs = null;
		HashMap<String, HashMap<String, String>> lGoogleResultObject = new HashMap<String, HashMap<String, String>>();
		HashMap<String, String> lRMap = null;
		String resultSetName = null;
		Document lDocument = HaysWebApiUtils.parseXML(pIN);
		xpath = XPathFactory.newInstance().newXPath();
		xpath.setNamespaceContext(new NamespaceResolver());
		exprResultSets = xpath.compile("//idc:service/idc:result/idc:resultsets");
		nodeResultSet = (Node) exprResultSets.evaluate(lDocument, XPathConstants.NODE);
		nodeListResultset = nodeResultSet.getChildNodes();
		for (int i = 0; i < nodeListResultset.getLength(); i++)
		{
			lRMap = new HashMap<String, String>();
			nodeTemp = nodeListResultset.item(i);
			if (nodeTemp != null && nodeTemp.getNodeType() == Node.ELEMENT_NODE)
			{
				resultSetName = nodeTemp.getNodeName().trim();
				exprAttributes = xpath.compile(resultSetName + "/idc:row");
				nodeAttribs = (Node) exprAttributes.evaluate(nodeResultSet, XPathConstants.NODE);
				nodeListAttribs = nodeAttribs.getChildNodes();
				for (int j = 0; j < nodeListAttribs.getLength(); j++)
				{
					nodeTemp = nodeListAttribs.item(j);
					if (nodeTemp != null && nodeTemp.getNodeType() == Node.ELEMENT_NODE)
					{
						lRMap.put(nodeTemp.getNodeName().replace("idc:", ""), nodeTemp.getFirstChild().getNodeValue());
					}
				}
				lGoogleResultObject.put(resultSetName.replace("idc:", ""), lRMap);
			}
		}
		setlGoogleResultObject(lGoogleResultObject);
	}

	private void convertToResultset()
	{
		HashMap<String, String> lRMap = null;
		ArrayList<String> lHeaderList = null;
		ArrayList<String> lAttrList = null;

		for (String lKey : getlGoogleResultObject().keySet())
		{
			lRMap = getlGoogleResultObject().get(lKey);
			lHeaderList = new ArrayList<String>();
			lAttrList = new ArrayList<String>();
			for (String attributeName : lRMap.keySet())
			{
				lHeaderList.add(attributeName);
				lAttrList.add(lRMap.get(attributeName).trim());
			}
			ResultSet lApiResultset = HaysWebApiUtils.createResultSetFromData(lHeaderList, lAttrList);
			m_binder.addResultSet(lKey, lApiResultset);
		}
	}

	private String getDerivedLocaleDetails(String pDerivedLocale) throws ServiceException, DataException
	{
		String providerName = "SystemDatabase";
		String lCountryRegion = "";
		DataResultSet lLocaleDetails = null;
		if (isNotNull(pDerivedLocale))
		{
			m_binder.putLocal("sitelocale", pDerivedLocale.trim());
			lLocaleDetails = HaysWebApiUtils.executeHaysProviderQuery(providerName, "QGetLocaleDetails", m_binder);
			if (lLocaleDetails != null && lLocaleDetails.getNumRows() > 0)
			{
				lCountryRegion = lLocaleDetails.getStringValueByName("CONTRY_REGION");
				SystemUtils.trace(TRACE_NAME, "Country region is : " + lCountryRegion);
			}
		}
		return lCountryRegion.trim();
	}

	public String getData(String pParamName)
	{
		String returnString = "";
		try
		{
			returnString = m_binder.get(pParamName);
		}
		catch (Exception e)
		{

		}
		return returnString.trim();
	}

	public static HashMap<String, HashMap<String, String>> getlGoogleResultObject()
	{
		return lAPIResultObject;
	}

	public static void setlGoogleResultObject(HashMap<String, HashMap<String, String>> pGoogleResultObject)
	{
		lAPIResultObject = pGoogleResultObject;
	}
}
