package hays.com.commonutils;

import static intradoc.shared.SharedObjects.getEnvironmentValue;
import intradoc.common.DataStreamWrapper;
import intradoc.common.ExecutionContext;
import intradoc.common.LocaleUtils;
import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.data.DataBinder;
import intradoc.data.DataException;
import intradoc.data.DataResultSet;
import intradoc.data.ResultSet;
import intradoc.data.Workspace;
import intradoc.filestore.FileStoreUtils;
import intradoc.filestore.IdcFileDescriptor;
import intradoc.provider.Provider;
import intradoc.provider.Providers;
import intradoc.server.Action;
import intradoc.server.Service;
import intradoc.server.ServiceHandler;
import intradoc.shared.SharedObjects;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class HaysWebApiUtils extends ServiceHandler
{
	public static final String UTIL_TRACE = "hays_util";
	private static HashMap<String, String> htmlEntities;
	static
	{
		htmlEntities = new HashMap<String, String>();
		htmlEntities.put("&lt;", "<");
		htmlEntities.put("&gt;", ">");
		htmlEntities.put("&amp;", "&");
		htmlEntities.put("&quot;", "\"");
		htmlEntities.put("&agrave;", "à");
		htmlEntities.put("&Agrave;", "À");
		htmlEntities.put("&acirc;", "â");
		htmlEntities.put("&auml;", "ä");
		htmlEntities.put("&Auml;", "Ä");
		htmlEntities.put("&Acirc;", "Â");
		htmlEntities.put("&aring;", "å");
		htmlEntities.put("&Aring;", "Å");
		htmlEntities.put("&aelig;", "æ");
		htmlEntities.put("&AElig;", "Æ");
		htmlEntities.put("&ccedil;", "ç");
		htmlEntities.put("&Ccedil;", "Ç");
		htmlEntities.put("&eacute;", "é");
		htmlEntities.put("&Eacute;", "É");
		htmlEntities.put("&egrave;", "è");
		htmlEntities.put("&Egrave;", "È");
		htmlEntities.put("&ecirc;", "ê");
		htmlEntities.put("&Ecirc;", "Ê");
		htmlEntities.put("&euml;", "ë");
		htmlEntities.put("&Euml;", "Ë");
		htmlEntities.put("&iuml;", "ï");
		htmlEntities.put("&Iuml;", "Ï");
		htmlEntities.put("&ocirc;", "ô");
		htmlEntities.put("&Ocirc;", "Ô");
		htmlEntities.put("&ouml;", "ö");
		htmlEntities.put("&Ouml;", "Ö");
		htmlEntities.put("&oslash;", "ø");
		htmlEntities.put("&Oslash;", "Ø");
		htmlEntities.put("&szlig;", "ß");
		htmlEntities.put("&ugrave;", "ù");
		htmlEntities.put("&Ugrave;", "Ù");
		htmlEntities.put("&ucirc;", "û");
		htmlEntities.put("&Ucirc;", "Û");
		htmlEntities.put("&uuml;", "ü");
		htmlEntities.put("&Uuml;", "Ü");
		htmlEntities.put("&nbsp;", " ");
		htmlEntities.put("&copy;", "\u00a9");
		htmlEntities.put("&reg;", "\u00ae");
		htmlEntities.put("&euro;", "\u20a0");
	}

	public static Workspace getDatabaseProvider(String pProviderName) throws ServiceException
	{
		Provider p = Providers.getProvider(pProviderName);
		if ((p == null) || (!p.isProviderOfType("database")))
		{
			throw new ServiceException("You the provider '" + pProviderName + "' is not a valid provider of type 'database'.");
		}
		Workspace ws = null;
		ws = (Workspace) p.getProvider();
		return ws;
	}

	public static DataResultSet executeHaysProviderSql(String pProviderName, String pRawSql) throws ServiceException, DataException
	{
		if ((pRawSql == null) || (pRawSql.length() == 0))
		{
			throw new ServiceException("You must specify a value for 'pRawSql'.");
		}
		Workspace ws = getDatabaseProvider(pProviderName);
		ResultSet temp = ws.createResultSetSQL(pRawSql);
		DataResultSet result = new DataResultSet();
		result.copy(temp);
		ws.releaseConnection();
		return result;
	}

	public static DataResultSet executeHaysProviderQuery(String pProviderName, String pQueryName, DataBinder pBinder)
			throws ServiceException, DataException
	{
		if (pQueryName != null && !(pQueryName.trim().length() > 0))
		{
			throw new ServiceException("You must specify a value for 'pQueryName'.");
		}
		Workspace ws = getDatabaseProvider(pProviderName);
		ResultSet temp = ws.createResultSet(pQueryName, pBinder);
		DataResultSet result = new DataResultSet();
		result.copy(temp);
		ws.releaseConnection();
		return result;
	}

	public static EntityHaysWebsites getHaysWebsitesData(DataResultSet pRsLocaleDetails) throws ServiceException, DataException
	{
		Vector vecLocaleDetails = new Vector();
		EntityHaysWebsites lEntityHaysWebsites = new EntityHaysWebsites();
		if (pRsLocaleDetails != null && pRsLocaleDetails.getNumRows() > 0)
		{
			vecLocaleDetails = pRsLocaleDetails.getRowValues(0);
			lEntityHaysWebsites.setlCountryRegion(vecLocaleDetails.elementAt(0).toString());
			lEntityHaysWebsites.setlDomainId(vecLocaleDetails.elementAt(1).toString());
			lEntityHaysWebsites.setlLanguageId(vecLocaleDetails.elementAt(2).toString());
			lEntityHaysWebsites.setlSiteId(vecLocaleDetails.elementAt(6).toString());
			lEntityHaysWebsites.setlDataFilePrefix(vecLocaleDetails.elementAt(7).toString());
		}
		return lEntityHaysWebsites;
	}

	public static Document parseXML(String pFilePath) throws ParserConfigurationException, SAXException, IOException
	{
		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		domFactory.setNamespaceAware(true);
		DocumentBuilder builder = domFactory.newDocumentBuilder();
		Document document = builder.parse(pFilePath);
		return document;
	}

	public static Document parseXML(InputStream paramInputStream) throws ParserConfigurationException, SAXException, IOException
	{
		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		domFactory.setNamespaceAware(true);
		DocumentBuilder builder = domFactory.newDocumentBuilder();
		builder.setEntityResolver(null);
		Document document = builder.parse(paramInputStream);
		return document;
	}

	public static ResultSet createResultSetFromData(ArrayList<String> pHeaderList, ArrayList<String> pAttrList)
	{
		DataResultSet rs = new DataResultSet(pHeaderList.toArray(new String[pHeaderList.size()]));
		int lSplitIndex = pHeaderList.size();

		List<List<String>> parts = chopList(pAttrList, lSplitIndex);
		for (List<String> lListOfData : parts)
		{
			rs.addRowWithList(lListOfData);
		}
		return rs;
	}

	public static <T> List<List<T>> chopList(List<T> list, final int L)
	{
		List<List<T>> parts = new ArrayList<List<T>>();
		final int N = list.size();
		for (int i = 0; i < N; i += L)
		{
			parts.add(new ArrayList<T>(list.subList(i, Math.min(N, i + L))));
		}
		return parts;
	}

	public static ArrayList<String> grabHTMLLinks(final String html)
	{
		Pattern patternTag = null;
		Pattern patternLink = null;
		String link = null;
		Matcher matcherTag, matcherLink;
		final String HTML_A_TAG_PATTERN = "(?i)<a([^>]+)>(.+?)</a>";
		final String HTML_A_HREF_TAG_PATTERN = "\\s*(?i)href\\s*=\\s*(\"([^\"]*\")|'[^']*'|([^'\">\\s]+))";
		patternTag = Pattern.compile(HTML_A_TAG_PATTERN);
		patternLink = Pattern.compile(HTML_A_HREF_TAG_PATTERN);
		ArrayList<String> result = new ArrayList<String>();
		matcherTag = patternTag.matcher(html);
		while (matcherTag.find())
		{
			String href = matcherTag.group(1); // href
			String linkText = matcherTag.group(2); // link text
			matcherLink = patternLink.matcher(href);
			while (matcherLink.find())
			{
				link = matcherLink.group(1); // link
				if (!("".equalsIgnoreCase(linkText) && "".equalsIgnoreCase(link)))
				{
					result.add(linkText);
					result.add(link.replaceAll("\"", ""));
				}
			}
		}
		return result;
	}

	public static void HandleExceptions(DataBinder pBinder, String pErrorCode, String pErrorStringKey) throws ServiceException
	{
		String msg = LocaleUtils.encodeMessage(pErrorStringKey, null);
		pBinder.putLocal("StatusMessage", LocaleUtils.encodeMessage(pErrorStringKey, null));
		pBinder.putLocal("StatusCode", pErrorCode);
		pBinder.removeResultSet("error");
		pBinder.m_resultSets.remove("error");
		throw new ServiceException(msg);
	}
	
	public static Integer getInt(String pData)
	{
		Integer lReturnValue = 0;
		try
		{
			if (pData != null)
			{
				lReturnValue = Integer.parseInt(pData);
			}
		}
		catch (Exception e)
		{

		}
		return lReturnValue;
	}
	
	public static boolean isNotNull(String pParamName)
	{
		if (pParamName != null && !"".equalsIgnoreCase(pParamName))
			return true;
		else
			return false;
	}

	public static InputStream getExternalURLStream(String pProxyConfig, String pURL, String contentType) throws IOException
	{
		String useProxy = getEnvironmentValue(pProxyConfig);
		boolean isProxy = false;
		String apacEnvCheck=getEnvironmentValue("IS_APAC_ENV_CERT");
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
			if(pURL.startsWith("https")){
				System.setProperty("https.proxyUser", authUser);
				System.setProperty("https.proxyPassword", authPassword);
				
				System.setProperty("https.proxyHost", PROXY_HOST);
				System.setProperty("https.proxyPort", PROXY_PORT);
				
				System.setProperty("http.agent", "Mozilla/5.0 (Windows NT 5.1; rv:12.0) Gecko/20100101 Firefox/12.0");
				SystemUtils.trace(UTIL_TRACE, "https.proxyUser : " + authUser);
				SystemUtils.trace(UTIL_TRACE, "https.proxyPassword : " + authPassword);
				SystemUtils.trace(UTIL_TRACE, "https.proxyHost : " + PROXY_HOST);
				SystemUtils.trace(UTIL_TRACE, "https.proxyPort : " + PROXY_PORT);
				if(pURL.indexOf("evolve") >-1 && ("TRUE".equalsIgnoreCase(apacEnvCheck))){
					SystemUtils.trace(UTIL_TRACE, "Inside evolve request ");
					String sslCaptchaURL= SharedObjects.getEnvironmentValue("sslCaptcha");	
			        SystemUtils.trace(UTIL_TRACE, "check ssl value "+sslCaptchaURL);
			        System.setProperty("javax.net.ssl.trustStore",sslCaptchaURL);
				}
			}else{
			System.setProperty("http.proxyUser", authUser);
			System.setProperty("http.proxyPassword", authPassword);
			
			System.setProperty("http.proxyHost", PROXY_HOST);
			System.setProperty("http.proxyPort", PROXY_PORT);
			
			System.setProperty("http.agent", "Mozilla/5.0 (Windows NT 5.1; rv:12.0) Gecko/20100101 Firefox/12.0");
			SystemUtils.trace(UTIL_TRACE, "http.proxyUser : " + authUser);
			SystemUtils.trace(UTIL_TRACE, "http.proxyPassword : " + authPassword);
			SystemUtils.trace(UTIL_TRACE, "http.proxyHost : " + PROXY_HOST);
			SystemUtils.trace(UTIL_TRACE, "http.proxyPort : " + PROXY_PORT);
			}
		}
		else
		{
			if(pURL.startsWith("https")){
				SystemUtils.trace(UTIL_TRACE, "https.proxyUser before remove: " + System.getProperty("https.proxyUser"));
				SystemUtils.trace(UTIL_TRACE, "https.proxyPassword before remove: " + System.getProperty("https.proxyPassword"));
				SystemUtils.trace(UTIL_TRACE, "https.proxyHost before remove: " + System.getProperty("https.proxyHost"));
				SystemUtils.trace(UTIL_TRACE, "https.proxyPort before remove: " + System.getProperty("https.proxyPort"));

				
				System.getProperties().remove("https.proxyUser");
				System.getProperties().remove("https.proxyPassword");
				System.getProperties().remove("https.proxyHost");
				System.getProperties().remove("https.proxyPort");
			}else{
			SystemUtils.trace(UTIL_TRACE, "http.proxyUser before remove: " + System.getProperty("http.proxyUser"));
			SystemUtils.trace(UTIL_TRACE, "http.proxyPassword before remove: " + System.getProperty("http.proxyPassword"));
			SystemUtils.trace(UTIL_TRACE, "http.proxyHost before remove: " + System.getProperty("http.proxyHost"));
			SystemUtils.trace(UTIL_TRACE, "http.proxyPort before remove: " + System.getProperty("http.proxyPort"));

			
			System.getProperties().remove("http.proxyUser");
			System.getProperties().remove("http.proxyPassword");
			System.getProperties().remove("http.proxyHost");
			System.getProperties().remove("http.proxyPort");
			}
			
		}
		
		SystemUtils.trace(UTIL_TRACE, "isProxy : " + isProxy);
		URL url = null;
		if (isProxy)
		{
			if(pURL.startsWith("https")){
				url = new URL(null, pURL, new sun.net.www.protocol.https.Handler());
			}else{
				url = new URL(null, pURL, new sun.net.www.protocol.http.Handler());
			}
		}
		else
		{
			url = new URL(pURL);
		}
		SystemUtils.trace(UTIL_TRACE, "URL : " + url);
		URLConnection conn = null;
		if (isProxy)
		{
			conn = url.openConnection();
		}
		else
		{
			conn = url.openConnection(Proxy.NO_PROXY);
		}
		SystemUtils.trace(UTIL_TRACE, "Connection : " + conn);

		conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.setUseCaches(false);
		if(contentType!=null && !contentType.equalsIgnoreCase("")){
		conn.setRequestProperty("Content-Type", contentType);
		}
		conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 5.1; rv:12.0) Gecko/20100101 Firefox/12.0");

		InputStream input = null;
		input = conn.getInputStream();
		SystemUtils.trace(UTIL_TRACE, "GotInputStream.");
		//String str = getStringFromInputStream(input);
		//SystemUtils.trace(UTIL_TRACE, "GotInputStream as TEXT "+str);
		return input;
	}
	
	// convert InputStream to String
	private static String getStringFromInputStream(InputStream is) {

		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();

		String line;
		try {

			br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return sb.toString();

	}
	
	public static String getFormattedDateForDB()
	{
		Calendar currentDate = Calendar.getInstance();
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy HH:mm:ss a");
		String currentFormattedDate = formatter.format(currentDate.getTime());
		return currentFormattedDate;
	}

	public static final String unescapeHTML(String source)
	{
		int i, j;
		boolean continueLoop;
		int skip = 0;
		do
		{
			continueLoop = false;
			i = source.indexOf("&", skip);
			if (i > -1)
			{
				j = source.indexOf(";", i);
				if (j > i)
				{
					String entityToLookFor = source.substring(i, j + 1);
					String value = (String) htmlEntities.get(entityToLookFor);
					if (value != null)
					{
						source = source.substring(0, i) + value + source.substring(j + 1);
						continueLoop = true;
					}
					else if (value == null)
					{
						skip = i + 1;
						continueLoop = true;
					}
				}
			}
		}
		while (continueLoop);
		return source;
	}

	public static void cleanUpDataRS(DataResultSet pResultsetToClean, String pRetainFieldsConfig)
	{
		String resultSetContentAPI = SharedObjects.getEnvironmentValue(pRetainFieldsConfig);
		List<String> resultSetContentAPIReturnList = Arrays.asList(resultSetContentAPI.split(","));
		List<String> fieldsToBeRemoved = new ArrayList<String>();
		String fieldName = null;
		int count = pResultsetToClean.getNumFields();
		for (int i = 0; i < count; i++)
		{
			fieldName = pResultsetToClean.getFieldName(i);
			if (!(resultSetContentAPIReturnList.contains(fieldName)))
			{
				fieldsToBeRemoved.add(fieldName);
			}
		}
		pResultsetToClean.removeFields(fieldsToBeRemoved.toArray(new String[0]));
	}

	public static void removeResultSets(DataBinder pBinder, String pCleanResultSetConfig)
	{
		String removeResultSetsForSolr = SharedObjects.getEnvironmentValue(pCleanResultSetConfig);
		String[] removeResultSetsArr = removeResultSetsForSolr.split(",");
		for (int i = 0; i < removeResultSetsArr.length; i++)
		{
			SystemUtils.trace(UTIL_TRACE, "Removing ResultSet: " + removeResultSetsArr[i]);
			pBinder.removeResultSet(removeResultSetsArr[i]);
		}
	}

	public static void removeLocalData(DataBinder pBinder, String pCleanLocalDataConfig)
	{
		String removeLocalData = SharedObjects.getEnvironmentValue(pCleanLocalDataConfig);
		String removeLocalDataArr[] = null;
		removeLocalDataArr = removeLocalData.split(",");
		for (int i = 0; i < removeLocalDataArr.length; i++)
		{
			SystemUtils.trace(UTIL_TRACE, "Removing LocalData: " + removeLocalDataArr[i]);
			pBinder.removeLocal(removeLocalDataArr[i]);
		}
	}

	public static String getFilePath(String contentid,DataBinder m_binder,Service m_service, Action pAction) throws DataException, ServiceException
	{
		m_binder.putLocal("dID", contentid);
		m_binder.putLocal("allowInterrupt", "1");
		m_binder.putLocal("RevisionSelectionMethod", "Specific");
		SystemUtils.trace(UTIL_TRACE, "dID "
				+ m_binder.getLocal("dID"));

		
		m_binder.removeLocal("IsSoap");
		m_service.executeServiceEx("DOC_INFO_BY_NAME", true);

		String path = getFile(m_binder,m_service);
		SystemUtils.trace(UTIL_TRACE, path);
		File lFile = new File(path);
		if (lFile.exists())
			SystemUtils.trace(UTIL_TRACE, "FILE EXISTS");
		getSiteId(m_binder, pAction);
		String lDocURL = path;
		SystemUtils.trace(UTIL_TRACE, "DocUrl : " + lDocURL);
		return lDocURL;
	}
	
	public static String getFile(DataBinder pBinder,Service m_service)
	{
		DataStreamWrapper localDataStreamWrapper = m_service.getDownloadStream(true);
		try
		{
			String str2 = pBinder.getAllowMissing("Rendition");

			if ((str2 == null) || (str2.equalsIgnoreCase("primary")))
			{
				str2 = "primaryFile";
			}
			pBinder.putLocal("RenditionId", str2);
			IdcFileDescriptor localIdcFileDescriptor = null;
			//SystemUtils.trace(UTIL_TRACE, "binder " + m_binder);
			localIdcFileDescriptor = m_service.m_fileStore.createDescriptor(pBinder, null, (ExecutionContext) m_service);
			SystemUtils.trace(UTIL_TRACE, localIdcFileDescriptor.toString());
			localDataStreamWrapper.m_descriptor = localIdcFileDescriptor;
			FileStoreUtils.forceDownloadStreamToFilePath(localDataStreamWrapper, m_service.m_fileStore, (ExecutionContext) m_service);
		}
		catch (Exception localException)
		{
			localException.printStackTrace();
		}
		return localDataStreamWrapper.m_filePath;
	}
	
	public static void getSiteId(DataBinder pBinder,Action m_currentAction) throws ServiceException, DataException
	{
		ResultSet lResultSet = pBinder.getResultSet("DOC_INFO");
		String siteId = lResultSet.getStringValueByName("xWebsites");
		String QwebsiteAddressQuery = m_currentAction.getParamAt(0);
		String dataSource = m_currentAction.getParamAt(2);
		String websiteAddress = "";
		String dSiteId = siteId.trim();
		if (dSiteId.indexOf(",") > 0)
		{
			dSiteId = dSiteId.substring(0, dSiteId.indexOf(","));
		}
		pBinder.putLocal("siteId", dSiteId);
		pBinder.putLocal("dSiteId", dSiteId);

		SystemUtils.trace(UTIL_TRACE, "Website Address Query:  " + QwebsiteAddressQuery);		
		DataResultSet drsSiteAddress = HaysWebApiUtils.executeHaysProviderQuery(dataSource, QwebsiteAddressQuery, pBinder);
		if (drsSiteAddress != null && drsSiteAddress.getNumRows() > 0)
		{
			SystemUtils.trace(UTIL_TRACE, "Website Address DataResultSet:  " + drsSiteAddress.toString());
			do
			{
				if (("1").equals(drsSiteAddress.getStringValueByName("dIsDefault")))
				{
					websiteAddress = drsSiteAddress.getStringValueByName("dAddress");
				}

			}
			while (drsSiteAddress.next());
		}
		SystemUtils.trace(UTIL_TRACE, "Website Address:  " + websiteAddress);
		pBinder.putLocal("websiteAddress", websiteAddress);
	}
	
	public static String resolveIDOCFunctions(String pStringToEvaluate,DataBinder pBinder,Service m_service)
	{
		String websiteAddress = pBinder.getLocal("websiteAddress");//commented for TS
		String returnString = null;
		String localization = null;

		if (!websiteAddress.startsWith("http"))
		{
			websiteAddress = "http://" + websiteAddress;
		}
		String websiteAddress1 = "";
		int last_slash = websiteAddress.lastIndexOf("/");
		if (last_slash > 10)
		{
			websiteAddress1 = websiteAddress.substring(0, last_slash);
			localization = websiteAddress.substring(last_slash + 1);

		}
		else
			websiteAddress1 = websiteAddress;

		SystemUtils.trace(UTIL_TRACE, "websiteAddress " + websiteAddress);
		SystemUtils.trace(UTIL_TRACE, "websiteAddress1 " + websiteAddress1);

		pStringToEvaluate = pStringToEvaluate.replaceAll("src=\"\\[!--", "src=\"" + websiteAddress1 + "<");
		pStringToEvaluate = pStringToEvaluate.replaceAll("href=\"\\[!--", "href=\"" + websiteAddress + "<");
		pStringToEvaluate = pStringToEvaluate.replaceAll("--]", "\\$>");
		pStringToEvaluate = pStringToEvaluate.replaceAll("\\?ssSourceSiteId=null", "");

		SystemUtils.trace(UTIL_TRACE, "pStringToEvaluate " + pStringToEvaluate);
		try
		{
			String siteId = pBinder.getLocal("siteId");
			SystemUtils.trace(UTIL_TRACE, "siteId " + siteId);
			returnString = m_service.getPageMerger().evaluateScript(pStringToEvaluate);
			returnString = returnString.replaceAll(websiteAddress + websiteAddress, websiteAddress);
			if (last_slash > 10)
				returnString = returnString.replaceAll(localization + "/" + localization, localization);
			return returnString;
		}
		catch (IllegalArgumentException e)
		{
			return pStringToEvaluate;
		}
		catch (IOException e)
		{
			return pStringToEvaluate;
		}
	}
}
