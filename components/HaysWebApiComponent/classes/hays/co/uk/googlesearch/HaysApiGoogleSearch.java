package hays.co.uk.googlesearch;

import static hays.com.commonutils.HaysWebApiUtils.HandleExceptions;
import static hays.com.commonutils.HaysWebApiUtils.createResultSetFromData;
import static hays.com.commonutils.HaysWebApiUtils.getInt;
import static hays.com.commonutils.HaysWebApiUtils.isNotNull;
import static intradoc.common.LocaleResources.getString;
import static intradoc.shared.SharedObjects.getEnvironmentValue;
import hays.co.uk.search.IHaysSearchConstants;
import hays.com.commonutils.EntityHaysWebsites;
import hays.com.commonutils.HaysWebApiUtils;
import intradoc.common.LocaleUtils;
import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.data.DataBinder;
import intradoc.data.DataException;
import intradoc.data.DataResultSet;
import intradoc.data.ResultSet;
import intradoc.data.Workspace;
import intradoc.provider.Provider;
import intradoc.provider.Providers;
import intradoc.server.ServiceHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
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

public class HaysApiGoogleSearch extends ServiceHandler
{
	public final static String TRACE_NAME = "hays_google_search";
	private ArrayList<HashMap<String, String>> lGoogleResultObject = null;
	private int startIndex = 0;
	private int endIndex = 0;
	private int maxIndex = 0;

	public static final String TITLE = "T";
	public static final String SNIPPET = "S";
	public static final String URL = "UD";
	public static final String SCORE = "RK";
	public static final String TEXT_NODE = "/text()";
	public static final String HASH = "#";
	public static final String NAME = "N";
	public static final String VALUE = "V";
	public static final String START_NUM = "SN";
	public static final String END_NUM = "EN";
	public static final String OB = "(";
	public static final String CB = ")";
	public static final String OR = "|";
	public static final String AND = ".";
	public static final String IS = ":";
	public static final String EQ = "=";
	public static final String ES = "";
	public static final String INM = "inmeta:";
	public static final String HAYS = "hays:";
	public static final String COMMA = ",";
	public boolean noResults = false;
	public String sponsoredImage = "";
	public String domainId = null;
	String locale = null;
	public String isCrossCountry = null;

	public void processGoogleSearch() throws ServiceException, DataException, MalformedURLException, IOException
	{
		locale = getData("SiteLocale");
		EntityHaysWebsites lEntityHaysWebsites = HaysWebApiUtils.getHaysWebsitesData((DataResultSet) this.m_binder.getResultSet("LOCALE_DETAILS"));
		domainId = lEntityHaysWebsites.getlDomainId();
		isCrossCountry = getData("isCrossCountry");
		
		String googleSearchUrl = getEnvironmentValue("GoogleSearchAction") + "?";
		createGoogleSearchParamsString();
		String googleSearchParams = m_binder.getLocal("gParams");

		String useProxy = getEnvironmentValue("GoogleUseProxy");
		SystemUtils.trace(TRACE_NAME, "useProxy : " + useProxy);
		boolean isProxy = false;
		if (useProxy != null && "TRUE".equalsIgnoreCase(useProxy))
		{
			isProxy = true;
			
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
			System.setProperty("http.agent", "Mozilla/5.0 (Windows NT 5.1; rv:12.0) Gecko/20100101 Firefox/12.0");
			SystemUtils.trace(TRACE_NAME, "http.proxyUser : " + authUser);
			SystemUtils.trace(TRACE_NAME, "http.proxyPassword : " + authPassword);
			SystemUtils.trace(TRACE_NAME, "http.proxyHost : " + PROXY_HOST);
			SystemUtils.trace(TRACE_NAME, "http.proxyPort : " + PROXY_PORT);
		}
		else
		{
			
			SystemUtils.trace(TRACE_NAME, "http.proxyUser before remove: " + System.getProperty("http.proxyUser"));
			SystemUtils.trace(TRACE_NAME, "http.proxyPassword before remove: " + System.getProperty("http.proxyPassword"));
			SystemUtils.trace(TRACE_NAME, "http.proxyHost before remove: " + System.getProperty("http.proxyHost"));
			SystemUtils.trace(TRACE_NAME, "http.proxyPort before remove: " + System.getProperty("http.proxyPort"));

			
			System.getProperties().remove("http.proxyUser");
			System.getProperties().remove("http.proxyPassword");
			System.getProperties().remove("http.proxyHost");
			System.getProperties().remove("http.proxyPort");
			
		}
		
		SystemUtils.trace(TRACE_NAME, "isProxy : " + isProxy);
		
		String googleSearchExtraParams = null;
		if (isCrossCountry != null && "Y".equalsIgnoreCase(isCrossCountry))
		{
			//removing &requiredfields=xlocale:<locale> from url in case of cross country
			googleSearchExtraParams = getEnvironmentValue("GoogleSearchExtraParams").replaceAll("&requiredfields=xlocale:", "");
		}
		else
		{
			googleSearchExtraParams = getEnvironmentValue("GoogleSearchExtraParams") + locale;
		}
		
		googleSearchExtraParams = googleSearchExtraParams + "&siteLocale=" + locale + "&domainId=" + domainId + 
				"&btnG=" + getEnvironmentValue("GoogleBtnG") + "&site=" + getEnvironmentValue("GoogleSite") + 
				"&entsp=" + getString("GoogleEntsp", null);
		
		googleSearchUrl = googleSearchUrl + googleSearchParams + googleSearchExtraParams;

		SystemUtils.trace(TRACE_NAME, "Google Search Extra Parameters : " + googleSearchExtraParams);
		SystemUtils.trace(TRACE_NAME, "Google Search URL : " + googleSearchUrl);

		
		googleSearchUrl = googleSearchUrl.replaceAll(" ", "+");
		URL url = null;
		if (isProxy)
		{
			url = new URL(null, googleSearchUrl, new sun.net.www.protocol.http.Handler());
		}
		else
		{
			url = new URL(googleSearchUrl);
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
		conn.setRequestProperty("Content-Type", "application/xml");
		conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 5.1; rv:12.0) Gecko/20100101 Firefox/12.0");

		InputStream input = conn.getInputStream();
		SystemUtils.trace(TRACE_NAME, "GotInputStream.");
		try
		{
			readGoogleOutput(input);
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
		SystemUtils.trace(TRACE_NAME, "Read google output successfully.");
		convertToUCMFormat();
		SystemUtils.trace(TRACE_NAME, "Converted the output to UCM Format.");
		input.close();
		this.m_binder.putLocal("StatusMessage", LocaleUtils.encodeMessage("wwWebApiOKMsg", null));
		this.m_binder.putLocal("StatusCode", "UC000");
	}

	public void createGoogleSearchParamsString() throws ServiceException, DataException
	{

		StringBuffer parameters = new StringBuffer();
		StringBuffer partial = new StringBuffer();
		StringBuffer inmeta = new StringBuffer();
		String q = getData(IHaysSearchConstants.JOB_KEYWORDS);
		if (q.contains(COMMA))
		{
			q = q.replaceAll(COMMA, " OR ");
		}
		try
		{
			SystemUtils.trace(TRACE_NAME, "qs value before encode : " + q);
			q = URLEncoder.encode(q, "UTF-8");
			SystemUtils.trace(TRACE_NAME, "qs value after encode : " + q);
		}
		catch (UnsupportedEncodingException ex)
		{
			throw new ServiceException(ex.toString());
		}
		parameters.append("q" + EQ + q);

		String typeP = getData(IHaysSearchConstants.JOB_SELECT_PERM);
		String jobTypeP = getData(IHaysSearchConstants.JOB_PERM);
		String minP = getData(IHaysSearchConstants.JOB_MIN_PERM);
		String maxP = getData(IHaysSearchConstants.JOB_MAX_PERM);
		String jobTypeC = getData(IHaysSearchConstants.JOB_CONTRACT);
		String typeC = getData(IHaysSearchConstants.JOB_SELECT_CONTRACT);
		String minC = getData(IHaysSearchConstants.JOB_MIN_CONTRACT);
		String maxC = getData(IHaysSearchConstants.JOB_MAX_CONTRACT);
		String jobTypeT = getData(IHaysSearchConstants.JOB_TEMP);
		String typeT = getData(IHaysSearchConstants.JOB_SELECT_TEMP);
		String minT = getData(IHaysSearchConstants.JOB_MIN_TEMP);
		String maxT = getData(IHaysSearchConstants.JOB_MAX_TEMP);

		if (isNotNull(jobTypeP))
		{
			inmeta.append(INM + "xjobType" + EQ + "P OR ");
		}
		if (isNotNull(jobTypeT))
		{
			inmeta.append(INM + "xjobType" + EQ + "T OR ");
		}
		if (isNotNull(jobTypeC))
		{
			inmeta.append(INM + "xjobType" + EQ + "C OR ");
		}
		if (inmeta.lastIndexOf("OR") > 0)
			inmeta.replace(inmeta.lastIndexOf("OR"), inmeta.length(), "");

		if (isNotNull(minP) && isNotNull(maxP))
		{
			if (typeP.equals(ES))
			{
				typeP = "A";
			}
			inmeta.append(INM + "xminSalary:0.." + calculateDailySalary(maxP, typeP) + " ");
			inmeta.append(INM + "xmaxSalary:" + calculateDailySalary(minP, typeP) + "..999999 ");
		}
		else if (isNotNull(minT) && isNotNull(maxT))
		{
			if (typeT.equals(ES))
			{
				typeT = "A";
			}
			inmeta.append(INM + "xminSalary:0.." + calculateDailySalary(maxT, typeT) + " ");
			inmeta.append(INM + "xmaxSalary:" + calculateDailySalary(minT, typeT) + "..999999 ");
		}
		else if (isNotNull(minC) && isNotNull(maxC))
		{
			if (typeC.equals(ES))
			{
				typeC = "A";
			}
			inmeta.append(INM + "xminSalary:0.." + calculateDailySalary(maxC, typeC) + " ");
			inmeta.append(INM + "xmaxSalary:" + calculateDailySalary(minC, typeC) + "..999999 ");
		}

		if (isNotNull(inmeta.toString()))
		{
			parameters.append(" " + inmeta.toString().trim());
		}
		if (isNotNull(getData("StartRow")))
		{
			int start = Integer.parseInt(getData("StartRow").trim()) - 1; //google starts with 0
			parameters.append("&start=" + start);
		}
		if (isNotNull(getData("ResultCount")))
		{
			parameters.append("&num=" + getData("ResultCount").trim());
		}
		if (isNotNull(getData("SortField")))
		{
			if (getData("SortField").endsWith("Date"))
			{
				if (isNotNull(getData("SortOrder")))
				{
					if (getData("SortOrder").equalsIgnoreCase("DESC"))
					{
						parameters.append("&sort=date:D:S:d1");
					}
					else if (getData("SortOrder").equalsIgnoreCase("ASC"))
					{
						parameters.append("&sort=date:A:S:d1");
					}
				}
				else
				{
					parameters.append("&sort=date:D:S:d1");
				}
			}
		}
		String job_category = getData(IHaysSearchConstants.JOB_CATEGORY);
		if (job_category != null && !ES.equals(job_category))
		{
			String[] jobCatagories = job_category.split(COMMA);
			partial.append(OB);
			for (String temp : jobCatagories)
			{
				if (temp.startsWith(HAYS))
				{
					temp = temp.replace(HAYS, ES);
				}
				partial.append("xSpecialism" + IS + temp + OR);
			}
			partial.setCharAt(partial.length() - 1, ')');
		}

		String industry = getData(IHaysSearchConstants.JOB_INDUSTRY);

		if (isNotNull(industry))
		{
			if (partial.length() > 0)
				partial.append(AND);
			String[] industries = industry.split(COMMA);
			partial.append(OB);
			for (String temp : industries)
			{
				partial.append("xIndustryId" + IS + temp + OR);
			}
			partial.setCharAt(partial.length() - 1, ')');
		}
		String postcode = getData("postcode");
		String location_id = getData(IHaysSearchConstants.LOCATION_ID);
		if (!isNotNull(location_id) && isNotNull(postcode))
		{
			m_binder.putLocal("searchPostcode", "1");
			m_binder.putLocal("domainId", domainId);
			m_binder.putLocal("location", postcode);
			m_binder.putLocal("locationColumn", "default_description");
			m_binder.putLocal("ResultCount", "10");
			SystemUtils.trace(TRACE_NAME, "Executing SEARCH_LOCATIONS with postcode : " + postcode + " and domain id : " + getData("domainId") + " and searchPostcode  : " + getData("searchPostcode"));
			m_service.executeServiceEx("SEARCH_LOCATIONS", true);
			ResultSet locationResultSet = m_binder.getResultSet("LOCATION_RESULT_LIST");
			SystemUtils.trace(TRACE_NAME, "locationResultSet:" + locationResultSet);
			StringBuilder locationIdList = new StringBuilder();
			if (locationResultSet != null)
			{
				do
				{
					location_id = locationResultSet.getStringValueByName("LOCATION_ID");
					if (locationIdList.length() > 0)
					{
						locationIdList.append(COMMA).append(location_id);
					}
					else
					{
						locationIdList.append(location_id);
					}
				}
				while (locationResultSet.next());
				location_id = locationIdList.toString();
				SystemUtils.trace(TRACE_NAME, "Location Id value from postcode is : " + locationIdList.toString());
			}
		}
		if (isNotNull(location_id))
		{
			ResultSet localeResultSet = this.m_binder.getResultSet("LOCALE_DETAILS");
			String locationColumn = localeResultSet.getStringValueByName("LOCATION_COLUMN");
			String locationIds = location_id;
			DataBinder docParams = new DataBinder();
			docParams.putLocal("locationColumn", locationColumn);
			docParams.putLocal("locationIds", locationIds);
			Provider p = Providers.getProvider("SystemDatabase");
			Workspace databaseServerWs = (Workspace) p.getProvider();
			ResultSet locationsRset = databaseServerWs.createResultSet("LocationDetailsQuery", docParams);
			DataResultSet locationsdrset = new DataResultSet();
			locationsdrset.copy(locationsRset);

			SystemUtils.trace(TRACE_NAME, "Location column:" + locationColumn);
			SystemUtils.trace(TRACE_NAME, "LocationID's:" + locationIds);
			SystemUtils.trace(TRACE_NAME, "Location Data ResultSet:" + locationsdrset);
			if (locationsdrset.getNumRows() > 0)
			{
				if (partial.length() > 0)
					partial.append(AND);
				String level = null;
				String locationId = null;
				do
				{
					level = locationsdrset.getStringValueByName("level");
					locationId = locationsdrset.getStringValueByName("location_id");
					if (locationId == null)
					{
						locationId = locationsdrset.getStringValueByName("LOCATION_ID");
					}
					SystemUtils.trace(TRACE_NAME, "Location:" + locationsdrset.getStringValueByName("default_description"));
					SystemUtils.trace(TRACE_NAME, "level:" + level);
					partial.append(OB);
					partial.append("xHayslocation" + level + IS + locationId + CB + OR);
				}
				while (locationsdrset.next());
				//partial.setCharAt(partial.length() - 1, ')');
				partial = partial.deleteCharAt(partial.length() - 1);
			}
		}
		String isOnlyJobTitle = getData(IHaysSearchConstants.IS_ONLY_JOB_TITLE);
		String jobTitle = getData(IHaysSearchConstants.JOB_KEYWORDS);
		if (isOnlyJobTitle != null && "Y".equalsIgnoreCase(isOnlyJobTitle))
		{
			if (partial.length() > 0)
				partial.append(AND);
			partial.append(OB + "JobTitle" + IS + jobTitle + CB);
		}
		if (isCrossCountry != null && "Y".equalsIgnoreCase(isCrossCountry))
		{
			if (partial.length() > 0)
				partial.append(AND);
			partial.append(OB + "CrossCountry" + IS + locale + CB);
		}
		if (isNotNull(partial.toString()))
		{
			parameters.append("&partialfields=" + partial.toString().trim());
		}

		m_binder.putLocal("gParams", parameters.toString().trim());
		SystemUtils.trace(TRACE_NAME, "getGoogleSearchParams() binder After:  " + parameters);

	}

	private void convertToUCMFormat()
	{
		if (!noResults)
		{
			ArrayList<String> lHeaderList = new ArrayList<String>();
			lHeaderList.add("dDocTitle");
			lHeaderList.add("dDocName");
			lHeaderList.add("dID");
			lHeaderList.add("dRevLabel");
			lHeaderList.add("xLocationDescription");
			lHeaderList.add("xJobType");
			lHeaderList.add("dInDate");
			lHeaderList.add("SCORE");
			lHeaderList.add("srfDocSnippet");
			lHeaderList.add("xlinks");
			lHeaderList.add("xDescription");
			lHeaderList.add("xSalaryDescription");
			lHeaderList.add("dStatus");
			lHeaderList.add("xEmailMeOnApplication");
			lHeaderList.add("SponsoredJobsImage");
			lHeaderList.add("HaysLocations");
			ArrayList<String> lAttrList = new ArrayList<String>();
			for (HashMap<String, String> lRMap : getlGoogleResultObject())
			{
				lAttrList.add(lRMap.get("JobTitle"));
				lAttrList.add(lRMap.get("JobId"));
				// lAttrList.add(lRMap.get("dID"));
				lAttrList.add("1");
				lAttrList.add("1");
				lAttrList.add(lRMap.get("xLocationDescription"));
				lAttrList.add(lRMap.get("xjobType"));
				lAttrList.add(lRMap.get("xReleaseDate"));
				lAttrList.add(lRMap.get(SCORE));
				lAttrList.add(lRMap.get(SNIPPET));
				lAttrList.add(lRMap.get(URL));
				lAttrList.add(lRMap.get("xDescription"));
				lAttrList.add(lRMap.get("xSalaryDescription"));
				lAttrList.add(lRMap.get("RELEASED"));
				if ("True".equalsIgnoreCase(lRMap.get("EmailMeOnApplication")))
					lAttrList.add("Y");
				else
					lAttrList.add("N");
				sponsoredImage = lRMap.get("ClientLogoUrl");
				if (sponsoredImage != null)
				{
					sponsoredImage = sponsoredImage.substring(sponsoredImage.indexOf("groups"));
				}
				else
				{
					sponsoredImage = "";
				}
				lAttrList.add(sponsoredImage);
				lAttrList.add(lRMap.get("xLongitude") + "#" + lRMap.get("xLatitude"));
			}
			DataResultSet lSearchResult = (DataResultSet) createResultSetFromData(lHeaderList, lAttrList);
			m_binder.addResultSet("SearchResults", lSearchResult);
			lHeaderList.clear();
			lAttrList.clear();
			lHeaderList.add("HeaderPageNumber");
			lHeaderList.add("PageReference");
			lHeaderList.add("PageNumber");
			lHeaderList.add("StartRow");
			lHeaderList.add("EndRow");
			int diff = getEndIndex() - getStartIndex() + 1;
			int pageNum = 1;
			for (int i = getStartIndex(); i <= getMaxIndex(); i += diff)
			{
				lAttrList.add(String.valueOf(pageNum));
				lAttrList.add(String.valueOf(pageNum));
				lAttrList.add(String.valueOf(pageNum));
				lAttrList.add(String.valueOf(i));
				lAttrList.add(String.valueOf(i + diff));
			}
			lSearchResult = (DataResultSet) createResultSetFromData(lHeaderList, lAttrList);
			m_binder.addResultSet("NavigationPages", lSearchResult);
			m_binder.putLocal("TotalRows", Integer.toString(getMaxIndex()));
		}
		else
		{
			DataResultSet lEmptyResultset = new DataResultSet();
			m_binder.addResultSet("SearchResults", lEmptyResultset);
			m_binder.addResultSet("NavigationPages", lEmptyResultset);
			m_binder.putLocal("TotalRows", "0");
		}
	}

	public void readGoogleOutput(InputStream pIN) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException
	{
		XPath xpath = null;
		XPathExpression expr = null;
		XPathExpression exprT = null;
		XPathExpression exprS = null;
		XPathExpression exprU = null;
		XPathExpression exprRK = null;
		Node nR = null;
		Node nMT = null;
		Node nRES = null;
		Element eR = null;
		Element eMT = null;
		Element eRES = null;
		NodeList lMTs = null;
		NodeList lRs = null;
		ArrayList<HashMap<String, String>> lGoogleResultObject = new ArrayList<HashMap<String, String>>();
		HashMap<String, String> lRMap = null;

		Document lDocument = HaysWebApiUtils.parseXML(pIN);
		xpath = XPathFactory.newInstance().newXPath();
		expr = xpath.compile("//GSP/RES");
		nRES = (Node) expr.evaluate(lDocument, XPathConstants.NODE);
		eRES = (Element) nRES;
		setStartIndex(getInt(eRES.getAttribute(START_NUM)));
		setEndIndex(getInt(eRES.getAttribute(END_NUM)));

		expr = xpath.compile("//GSP/RES/M" + TEXT_NODE);
		setMaxIndex(getInt(expr.evaluate(lDocument, XPathConstants.STRING).toString()));

		expr = xpath.compile("//GSP/RES/R");
		Object oRs = expr.evaluate(lDocument, XPathConstants.NODESET);
		expr = xpath.compile("MT");
		exprT = xpath.compile(TITLE + TEXT_NODE);
		exprS = xpath.compile(SNIPPET + TEXT_NODE);
		exprU = xpath.compile(URL + TEXT_NODE);
		exprRK = xpath.compile(SCORE + TEXT_NODE);
		lRs = (NodeList) oRs;
		for (int i = 0; i < lRs.getLength(); i++)
		{
			nR = lRs.item(i);
			if (nR != null && nR.getNodeType() == Node.ELEMENT_NODE)
			{
				lRMap = new HashMap<String, String>();
				eR = (Element) nR;
				lMTs = (NodeList) expr.evaluate(eR, XPathConstants.NODESET);
				for (int j = 0; j < lMTs.getLength(); j++)
				{
					nMT = lMTs.item(j);
					if (nMT != null && nMT.getNodeType() == Node.ELEMENT_NODE)
					{
						eMT = (Element) nMT;
						lRMap.put(eMT.getAttribute(NAME), eMT.getAttribute(VALUE));
					}
				}
				lRMap.put(SNIPPET, exprS.evaluate(eR, XPathConstants.STRING).toString());
				lRMap.put(TITLE, exprT.evaluate(eR, XPathConstants.STRING).toString());
				lRMap.put(URL, exprU.evaluate(eR, XPathConstants.STRING).toString());
				lRMap.put(SCORE, exprRK.evaluate(eR, XPathConstants.STRING).toString());
			}
			lGoogleResultObject.add(lRMap);
		}

		setlGoogleResultObject(lGoogleResultObject);

	}

	public String calculateDailySalary(String pSalary, String payType)
	{
		String res = "0";
		int salary = getInt(pSalary);
		if ("A".equalsIgnoreCase(payType))
		{
			res = Integer.toString(salary / 260);
		}
		else if ("M".equalsIgnoreCase(payType))
		{
			res = Integer.toString(salary / 220);
		}
		else if ("W".equalsIgnoreCase(payType))
		{
			res = Integer.toString(salary / 5);
		}
		else if ("D".equalsIgnoreCase(payType))
		{
			res = pSalary;
		}
		else if ("H".equalsIgnoreCase(payType))
		{
			res = Integer.toString(salary * 8);
		}
		return res;
	}

	public String getData(String pParamName)
	{
		String returnString = "";
		try
		{
			returnString = m_binder.get(pParamName);
			if ("null".equalsIgnoreCase(returnString))
			{
				returnString = "";
			}
			returnString = returnString.trim();
		}
		catch (Exception e)
		{

		}
		return returnString;
	}

	public ArrayList<HashMap<String, String>> getlGoogleResultObject()
	{
		return lGoogleResultObject;
	}

	public void setlGoogleResultObject(ArrayList<HashMap<String, String>> lGoogleResultObject)
	{
		this.lGoogleResultObject = lGoogleResultObject;
	}

	public int getStartIndex()
	{
		return startIndex;
	}

	public void setStartIndex(int startIndex)
	{
		this.startIndex = startIndex;
	}

	public int getEndIndex()
	{
		return endIndex;
	}

	public void setEndIndex(int endIndex)
	{
		this.endIndex = endIndex;
	}

	public int getMaxIndex()
	{
		return maxIndex;
	}

	public void setMaxIndex(int maxIndex)
	{
		this.maxIndex = maxIndex;
	}
}
