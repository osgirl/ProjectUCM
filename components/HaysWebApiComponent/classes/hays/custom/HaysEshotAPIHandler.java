package hays.custom;

import static hays.com.commonutils.HaysWebApiUtils.HandleExceptions;
import static intradoc.shared.SharedObjects.getEnvironmentValue;
import intradoc.common.LocaleUtils;
import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.data.DataException;
import intradoc.server.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.json.JSONObject;
import org.xml.sax.SAXException;

public class HaysEshotAPIHandler extends Service
{
	public final static String TRACE_NAME = "hays_eshot_api_call";
	

	public void callEshotAPI() throws ServiceException, DataException, MalformedURLException, IOException
	{
		String eshotServiceUrl = getEnvironmentValue("EshotServiceUrl");
		String suffix = m_binder.getLocal("suffix");
		String EshotWhiteImageUrl = getEnvironmentValue("EshotWhiteImageUrl");
		String useProxy = getEnvironmentValue("EshotUseProxy");
		SystemUtils.trace(TRACE_NAME, "useProxy : " + useProxy + ", eshotServiceUrl " + eshotServiceUrl);
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
			SystemUtils.trace(TRACE_NAME, "http.proxyUser : " + authUser + " http.proxyPassword : " + authPassword + " http.proxyHost : " + PROXY_HOST + " http.proxyPort : " + PROXY_PORT);
			
		}
		else
		{
			
			SystemUtils.trace(TRACE_NAME, "http.proxyUser before remove: " + System.getProperty("http.proxyUser") + " proxyPassword : " + System.getProperty("http.proxyPassword") +" proxyHost : "+System.getProperty("http.proxyHost") + " proxyPort : " +System.getProperty("http.proxyPort"));
			System.getProperties().remove("http.proxyUser");
			System.getProperties().remove("http.proxyPassword");
			System.getProperties().remove("http.proxyHost");
			System.getProperties().remove("http.proxyPort");
			
		}
		eshotServiceUrl = eshotServiceUrl + suffix;
//		eshotServiceUrl = eshotServiceUrl.replaceAll(" ", "+");
		URL url = null;
		if (isProxy)
		{
			url = new URL(null, eshotServiceUrl, new sun.net.www.protocol.http.Handler());
		}
		else
		{
			url = new URL(eshotServiceUrl);
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
		BufferedReader streamReader=null;
		try
		{
			InputStream input = conn.getInputStream();
			streamReader = new BufferedReader(new InputStreamReader(input, "UTF-8"));
		    StringBuilder responseStrBuilder = new StringBuilder();

		    String inputStr;
		     while ((inputStr = streamReader.readLine()) != null)
		      responseStrBuilder.append(inputStr);
		    JSONObject jsonObject = new JSONObject(responseStrBuilder.toString());
		    SystemUtils.trace(TRACE_NAME, "Response String : " + jsonObject.toString());
		   // int respCode = ((HttpURLConnection)conn).getResponseCode();
//			String responseMessage=((HttpURLConnection)conn).getResponseMessage();	    
//		    SystemUtils.trace(TRACE_NAME, "Response String : " + responseMessage);
		
		}
		catch (IOException e)
		{
			HandleExceptions(m_binder, "UC011", "wwFileNotFound");
		}
		catch (Exception e)
		{
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
		SystemUtils.trace(TRACE_NAME, "Eshot API Calling complete");
		this.m_binder.putLocal("StatusMessage", LocaleUtils.encodeMessage("wwWebApiOKMsg", null));
		this.m_binder.putLocal("StatusCode", "UC000");
		this.setRedirectUrl(EshotWhiteImageUrl);
	}


	public void readGoogleOutput(InputStream pIN) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException
	{

	}

}
