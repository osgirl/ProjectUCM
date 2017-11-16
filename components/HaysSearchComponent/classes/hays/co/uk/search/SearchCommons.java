package hays.co.uk.search;

import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import static intradoc.shared.SharedObjects.getEnvironmentValue;

import intradoc.common.SystemUtils;
import intradoc.data.DataException;
import intradoc.shared.SharedObjects;
//Create for automation
public class SearchCommons {
	public static final String UTIL_TRACE = "hays_search_commons";
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
	
	static public double calculateUnifiedSalary(String salary, String payType) throws NumberFormatException, DataException {
		double asalary = Math.abs( Double.parseDouble(salary) );
		double unified = -1;
		if (payType.equalsIgnoreCase("H") ) {
			unified = asalary * 2080;		
		}
		else if(payType.equalsIgnoreCase("D") ) {
			unified = asalary * 260;
		}
		else if( payType.equalsIgnoreCase("W") ) {
			unified = asalary * 52;
		}
		else if( payType.equalsIgnoreCase("M")) {
			unified = asalary * 12;		
		}
		else if (payType.equalsIgnoreCase("A")) {
			unified = asalary;
		}
		else throw new DataException("Payment type is incorrect: " + payType);
		return unified;
	}

}
