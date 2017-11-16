package hays.com.search;

import static hays.com.commonutils.HaysWebApiUtils.getInt;
import static hays.com.commonutils.HaysWebApiUtils.isNotNull;
import static intradoc.common.LocaleResources.getString;
import static intradoc.shared.SharedObjects.getEnvironmentValue;
import hays.com.commonutils.EntityHaysWebsites;
import hays.com.commonutils.HaysWebApiUtils;
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
import java.util.Scanner;


public class GoogleSearchWrapper extends ServiceHandler
{
	public final static String TRACE_NAME = "jobs_search";
	private ArrayList<HashMap<String, String>> lGoogleResultObject = null;
	private int startIndex = 0;
	private int endIndex = 0;
	private int maxIndex = 0;
	public static final String JOB_KEYWORDSR6 = "job_keywords";
	public static final String JOB_SELECT_PERMR6 = "job_select_permanent";
	public static final String JOB_PERMR6 = "job_permanent";
	public static final String JOB_CONTRACTR6 = "job_contract";
	public static final String JOB_TEMPR6 = "job_temporary";
	public static final String JOB_SELECT_CONTRACTR6 = "job_select_contract";
	public static final String JOB_MIN_CONTRACTR6 = "job_min_contract";
	public static final String JOB_MAX_CONTRACTR6 = "job_max_contract";
	public static final String JOB_SELECT_TEMPR6 = "job_select_temporary";
	public static final String JOB_MIN_PERMR6 = "job_min_permanent";
	public static final String JOB_MAX_PERMR6 = "job_max_permanent";
	public static final String JOB_MIN_TEMPR6 = "job_min_temporary";
	public static final String JOB_MAX_TEMPR6 = "job_max_temporary";
	public static final String JOB_CATEGORYR6 = "job_category";
	public static final String JOB_INDUSTRYR6 = "job_industry";
	public static final String LOCATION_IDR6 = "location_id";
	public static final String IS_ONLY_JOB_TITLER6 = "isOnlyJobTitle";
	public static final String JOB_TITLER6 = "job_title";
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
	public boolean noResults = false;
	public String sponsoredImage="";
	
	public void fetchGoogleResults() throws ServiceException, DataException, MalformedURLException, IOException
	{
		String locale = getData("SiteLocale");
		String domainId = getData("domainId");

		String googleSearchUrl = getEnvironmentValue("Jobs_GoogleSearchAction") + "?";
		createGoogleSearchParamsString();
		String googleSearchParams = m_binder.getLocal("gParams");
		String proxystylesheetVal = getEnvironmentValue("Jobs_GoogleProxyStylesheet");
		String GSASite = getEnvironmentValue("Jobs_GoogleSite");
		String responseType = m_binder.getLocal("responseType");
		String extraParameters = getEnvironmentValue("Jobs_GoogleSearchExtraParams");
		String entspValue = getString("GoogleEntsp", null);
		if(responseType != null && responseType.equalsIgnoreCase("json")){
			proxystylesheetVal = getEnvironmentValue("GSAProxyStylesheet");
			googleSearchUrl = getEnvironmentValue("GSA_SearchUrl") + "?";
			extraParameters = getEnvironmentValue("GSA_SearchSG_ExtraParams");
			GSASite = getEnvironmentValue("GSAGoogleSite");
			entspValue = "";
		}
		
		//add specialism name to pass to google for making results page title
		String ontValue = getData("ontValue");
		if(isNotNull(ontValue)){
			googleSearchParams = googleSearchParams + "&ontValue="+ontValue;
		}
		
		String useProxy =  getEnvironmentValue("Jobs_GoogleUseProxy");
		SystemUtils.trace(TRACE_NAME, "useProxy : " + useProxy);
		boolean isProxy = false;
		if(useProxy!=null && "TRUE".equalsIgnoreCase(useProxy))
		{
			isProxy = true;
		}
		String googleSearchExtraParams = extraParameters + locale + "&siteLocale=" + locale + "&domainId="
				+ domainId + "&btnG=" + getEnvironmentValue("Jobs_GoogleBtnG") + "&site=" + GSASite + "&client="
				+ getString("GoogleClient", null) + "&entsp=" + entspValue  + "&proxystylesheet=" + proxystylesheetVal;
		googleSearchUrl = googleSearchUrl + googleSearchParams + googleSearchExtraParams;
		
		//remove xml output parameter
		googleSearchUrl = googleSearchUrl.replace("&output=xml_no_dtd",""); 

		SystemUtils.trace(TRACE_NAME, "Google Search Extra Parameters : " + googleSearchExtraParams);
		SystemUtils.trace(TRACE_NAME, "Google Search URL : " + googleSearchUrl);

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
		googleSearchUrl = googleSearchUrl.replaceAll(" ", "+");
		URL url = null;
		if(isProxy)
		{
			url = new URL(null, googleSearchUrl, new sun.net.www.protocol.http.Handler());
		}
		else
		{
			url = new URL(googleSearchUrl);
		}
		
		SystemUtils.trace(TRACE_NAME, "URL : " + url);
		URLConnection conn = null;
		if(isProxy)
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
		catch (Exception e)
		{
			HandleExceptions(e);
		}
		
		SystemUtils.trace(TRACE_NAME, "Read google output successfully.");
		input.close();
	}
	
	public void readGoogleOutput(InputStream pIN) 
	{
		String fEncoding = "UTF8";
		StringBuilder text = new StringBuilder();
	    String NL = System.getProperty("line.separator");
	    Scanner scanner = new Scanner(pIN, fEncoding);
	      while (scanner.hasNextLine()){
	        text.append(scanner.nextLine() + NL);
	      }
	     scanner.close();
	    //log("Text read in: " + text);
	     this.m_binder.putLocal("Output", text.toString());
		
	}
	
	private void HandleExceptions(Exception e){
		SystemUtils.dumpException(TRACE_NAME, e);
	}

	public void createGoogleSearchParamsString() throws ServiceException, DataException
	{

		StringBuffer parameters = new StringBuffer();
		StringBuffer partial = new StringBuffer();
		StringBuffer inmeta = new StringBuffer();
		String q = getData(JOB_KEYWORDSR6);
		if(q.contains(","))
		{
			q = q.replaceAll(",", " OR ");
		}
		parameters.append("q" + EQ + q);
		
		String typeP = getData(JOB_SELECT_PERMR6);
		String jobTypeP = getData(JOB_PERMR6);
		String minP = getData(JOB_MIN_PERMR6);
		String maxP = getData(JOB_MAX_PERMR6);
		String jobTypeC = getData(JOB_CONTRACTR6);
		String typeC = getData(JOB_SELECT_CONTRACTR6);
		String minC = getData(JOB_MIN_CONTRACTR6);
		String maxC = getData(JOB_MAX_CONTRACTR6);
		String jobTypeT = getData(JOB_TEMPR6);
		String typeT = getData(JOB_SELECT_TEMPR6);
		String minT = getData(JOB_MIN_TEMPR6);
		String maxT = getData(JOB_MAX_TEMPR6);

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
			int start = Integer.parseInt(getData("StartRow").trim())-1; //google starts with 0
			parameters.append("&start=" + start);
		}
		if (isNotNull(getData("ResultCount")))
		{
			parameters.append("&num=" + getData("ResultCount").trim());
		}
		if (isNotNull(getData("SortField")))
		{
			if(getData("SortField").endsWith("Date"))
			{
				if(isNotNull(getData("SortOrder")))
				{
					if(getData("SortOrder").equalsIgnoreCase("DESC"))
					{
						parameters.append("&sort=date:D:S:d1");
					}
					else if(getData("SortOrder").equalsIgnoreCase("ASC"))
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
		String job_category = getData(JOB_CATEGORYR6);
		if (job_category != null && !ES.equals(job_category))
		{
			String[] jobCatagories = job_category.split(",");
			partial.append(OB);
			for (String temp : jobCatagories)
			{
				if(temp.startsWith(HAYS))
				{
					temp = temp.replace(HAYS, ES);
				}
				partial.append("xSpecialism" + IS + temp + OR);
			}
			partial.setCharAt(partial.length() - 1, ')');
		}

		String industry = getData(JOB_INDUSTRYR6);

		if (isNotNull(industry))
		{
			if (partial.length() > 0)
				partial.append(AND);
			String[] industries = industry.split(",");
			partial.append(OB);
			for (String temp : industries)
			{
				partial.append("xIndustryId" + IS + temp + OR);
			}
			partial.setCharAt(partial.length() - 1, ')');
		}

		String location_id = getData(LOCATION_IDR6);
		String level = getData("location_level");
		String locationName = getData("location_name");
		if (isNotNull(location_id))
		{
			partial.append(OB);
			partial.append("xHayslocation" + level + IS + location_id + CB);
			parameters.append("&locationToSearch=" + locationName.trim());
		}
		String isOnlyJobTitle = getData(IS_ONLY_JOB_TITLER6);
		String jobTitle = getData(JOB_TITLER6);
		if (isOnlyJobTitle != null && "Y".equalsIgnoreCase(isOnlyJobTitle))
		{
			if (partial.length() > 0)
				partial.append(AND);
			partial.append(OB + "JobTitle" + IS + jobTitle + CB);
		}
		if (isNotNull(partial.toString()))
		{
			parameters.append("&partialfields=" + partial.toString().trim());
		}

		

		m_binder.putLocal("gParams", parameters.toString().trim());
		SystemUtils.trace(TRACE_NAME, "getGoogleSearchParams() binder After:  " + parameters);
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
		}
		catch (Exception e)
		{

		}
		return returnString.trim();
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
