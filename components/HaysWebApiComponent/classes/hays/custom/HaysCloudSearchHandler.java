package hays.custom;

import static hays.com.commonutils.HaysWebApiUtils.HandleExceptions;
import static intradoc.shared.SharedObjects.getEnvironmentValue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.*;
import org.json.*;
import hays.co.uk.HaysUtil;
import hays.com.commonutils.HaysWebApiUtils;
import intradoc.server.Service;
import intradoc.common.LocaleUtils;
import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.data.DataException;
import intradoc.data.DataResultSet;

public class HaysCloudSearchHandler extends Service{
	
	public final static String TRACE_NAME = "Google_cloud_search";
	
	public void getGoogleSearchKeywords() throws ServiceException, DataException, MalformedURLException, IOException{
		String searchApiBaseUrl = getEnvironmentValue("searchApiBaseUrl");
		String searchApiBaseUrlHMac = getEnvironmentValue("hmacDigestBaseApi");
		String searchApiMethod = getEnvironmentValue("searchApiMethod");
		String keyword = URLEncoder.encode(m_binder.getLocal("keyword"),"UTF-8");
		DataResultSet lReturnResultSet = null;
		JSONObject cloudResponse = null;
		String siteLocale = m_binder.getLocal("locale");
		String pageSize  = m_binder.getLocal("limit");
		String completionType  = m_binder.getLocal("completionType");
		String useProxy = getEnvironmentValue("GCSUseProxy");
		String additionalParam = "?query="+keyword + "&languageCode="+ siteLocale + "&completionType="+ completionType + "&pageSize="+ pageSize;
		SystemUtils.trace(TRACE_NAME, "useProxy : " + useProxy + ", searchApiBaseUrl: " + searchApiBaseUrl + ", searchApiBaseUrlHMac: " + searchApiBaseUrlHMac + ", searchApiMethod: " + searchApiMethod);
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
			if(searchApiBaseUrl.startsWith("https")){
				System.setProperty("https.proxyUser", authUser);
				System.setProperty("https.proxyPassword", authPassword);
				System.setProperty("https.proxyHost", PROXY_HOST);
				System.setProperty("https.proxyPort", PROXY_PORT);
				System.setProperty("https.protocols", "TLSv1.0");
			}
				 
			System.setProperty("http.agent", "Mozilla/5.0 (Windows NT 5.1; rv:12.0) Gecko/20100101 Firefox/12.0");
			SystemUtils.trace(TRACE_NAME, "http.proxyUser : " + authUser + " http.proxyPassword : " + authPassword + " http.proxyHost : " + PROXY_HOST + " http.proxyPort : " + PROXY_PORT);
			
		}
		else
		{
			
			SystemUtils.trace(TRACE_NAME, "http.proxyUser before remove: " + System.getProperty("http.proxyUser") + " proxyPassword : " + System.getProperty("http.proxyPassword") +" proxyHost : "+System.getProperty("http.proxyHost") + " proxyPort : " +System.getProperty("http.proxyPort"));
			System.getProperties().remove("http.proxyUser");
			System.getProperties().remove("http.proxyPassword");
			System.getProperties().remove("http.proxyHost");
			System.getProperties().remove("http.proxyPort");
			if(searchApiBaseUrl.startsWith("https")){
				System.getProperties().remove("https.proxyUser");
				System.getProperties().remove("https.proxyPassword");
				System.getProperties().remove("https.proxyHost");
				System.getProperties().remove("https.proxyPort");
				System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
			}
			
		}
		searchApiBaseUrlHMac = searchApiBaseUrlHMac + searchApiMethod;
		searchApiBaseUrl = searchApiBaseUrl + searchApiMethod + additionalParam;
		 
		URL url = null;
		if (isProxy)
		{
			
			url = new URL(null, searchApiBaseUrl, new sun.net.www.protocol.http.Handler());
			if(searchApiBaseUrl.startsWith("https"))
				url = new URL(null, searchApiBaseUrl, new sun.net.www.protocol.https.Handler());
		}
		else
		{
			url = new URL(searchApiBaseUrl);
		}
		SystemUtils.trace(TRACE_NAME, "URL : " + url);
		CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
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
		String date = null;
		String HMAC_GoogleSearch=getEnvironmentValue("HMAC_GoogleSearchEnable");
		SystemUtils.trace("hays_util_Hmac",  "String sent = "+HMAC_GoogleSearch);
		if(HMAC_GoogleSearch.equalsIgnoreCase("true")){
			 //logger.debug("In isGoogleSearchHmac enabled");
			date = HaysUtil.getRequestTime();
			 String hmacHash=null;
			try {
				hmacHash = HaysUtil.getDigest(searchApiBaseUrlHMac,date,"GET",null,null);
				SystemUtils.trace(TRACE_NAME, "hmacHash code : " + hmacHash);
			} catch (Exception e3) {
				// TODO Auto-generated catch block
				e3.printStackTrace();
			}
			//String key = getEnvironmentValue("API_KEY");
			//logger.info("HASH : {} , KEY : {} , date : {} ",hmacHash,key,date);
			conn.setRequestProperty("Authorization",getEnvironmentValue("HMAC_API_KEY")+":"+hmacHash);
			conn.setRequestProperty("sDate",date );
		}
			 
		//conn.setRequestProperty("Content-Type", null);
		conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 5.1; rv:12.0) Gecko/20100101 Firefox/12.0");
		BufferedReader streamReader=null;
		try
		{
			InputStream input = conn.getInputStream();
			streamReader = new BufferedReader(new InputStreamReader(input, "UTF-8"));
		    StringBuilder responseStrBuilder = new StringBuilder();
		    int respCode = ((HttpURLConnection)conn).getResponseCode();
		    SystemUtils.trace(TRACE_NAME, "Response code : " + respCode);
		    if(respCode == 200){
		    String inputStr;
		     while ((inputStr = streamReader.readLine()) != null)
		      responseStrBuilder.append(inputStr);
		     try {
		 		cloudResponse = new JSONObject(responseStrBuilder.toString());
		 		JSONArray result = cloudResponse.getJSONArray("result");
		 		System.out.println("JsonArray : " + result);
		 		ArrayList<String> lHeaderList = new ArrayList<String>();
				List<List<String>> lValueDataList = new ArrayList<List<String>>();
				lHeaderList.add("Suggestion");
				lHeaderList.add("type");
				if (result!= null) { 
				  int len = result.length();
				  System.out.println("length : " + len);
				  for (int i=0; i<len; i++) { 
				    JSONObject o = (JSONObject) result.get(i);
				    ArrayList<String> lValueData = new ArrayList<String>();
				    lValueData.add(o.getString("suggestion"));
				    lValueData.add(o.getString("type"));
				    lValueDataList.add(lValueData);
				  } 
				} 
				lReturnResultSet = new DataResultSet(lHeaderList.toArray(new String[lHeaderList.size()]));
				for (List<String> lListOfData : lValueDataList)
				{
					System.out.println("test : " + lListOfData );
					lReturnResultSet.addRowWithList(lListOfData);
					
				}
		 		} catch (JSONException e) {
		 			// TODO Auto-generated catch block
		 			e.printStackTrace();
		 		}
		 		
		 		System.out.println("Objects. " + lReturnResultSet);
		 		this.m_binder.addResultSet("Suggestions", lReturnResultSet);
		    }
		   }
		catch (IOException e)
		{
			SystemUtils.trace(TRACE_NAME, "Exception : " + e.getMessage());
			HandleExceptions(m_binder, "UC011", "wwFileNotFound");
		}
		catch (Exception e)
		{
			SystemUtils.trace(TRACE_NAME, "Exception : " + e.getMessage());
			HandleExceptions(m_binder, "UC011", "wwFileNotFound");
		}
		finally {
			  try {
				  if( streamReader != null)
					  streamReader.close(); 
			      } catch(Exception e)
			       {
					e.printStackTrace();
				   }
			    }
		SystemUtils.trace(TRACE_NAME, "Google Search API Calling complete");
		this.m_binder.putLocal("StatusMessage", LocaleUtils.encodeMessage("wwWebApiOKMsg", null));
		this.m_binder.putLocal("StatusCode", "UC000");
	}
}
