package hays.custom;

import static intradoc.shared.SharedObjects.getEnvironmentValue;
import hays.com.commonutils.HaysWebApiUtils;
import intradoc.common.SystemUtils;
import intradoc.data.DataException;
import intradoc.data.DataResultSet;
import intradoc.data.ResultSet;
import intradoc.server.ServiceHandler;
import intradoc.shared.SharedObjects;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HaysSocialMediaBlogs extends ServiceHandler
{

	public final static String TRACE_NAME = "SocialMediaBlogs";
	public boolean noResults = false;
	public static final String TITLE = "title";
	public static final String THUMBNAIL = SharedObjects.getEnvironmentValue("ThumbNailTag");
	public static final String URL = "url";
	public static final String POSTS = "posts";
	public static final String THUMBNAIL_IMAGES = "thumbnail_images";
	public ArrayList<String> gContentList = new ArrayList<String>();

	public void getSocialMediaBlogs() throws MalformedURLException, IOException, DataException
	{
		SystemUtils.trace(TRACE_NAME, "Social Media service is runnig : ");
		String resultSetName = m_currentAction.getParamAt(0);
		String socialMediaURL = SharedObjects.getEnvironmentValue("SocialMediaUrl");

		SystemUtils.trace(TRACE_NAME, "Social Blogs Resultset: " + resultSetName);
		SystemUtils.trace(TRACE_NAME, "Social Media URL: " + socialMediaURL);

		if ((socialMediaURL) != null)
		{
			socialMediaURL = socialMediaURL.replace("REPLACE_JSON", "get_category_posts");
			socialMediaURL = socialMediaURL.replace("REPLACE_COUNT", "3");
			socialMediaURL = socialMediaURL.replace("REPLACE_SLUG", "current");
		}
		SystemUtils.trace(TRACE_NAME, "Final Social Media URL: " + socialMediaURL);
		gContentList.clear();
		initializeProxyEnvironment();
		parseJSONObject(executeURL(socialMediaURL));
		ArrayList<String> lHeaderList = new ArrayList<String>();
		lHeaderList.add("Title");
		lHeaderList.add("Thumbnail");
		lHeaderList.add("Url");

		ResultSet outputResultSet = HaysWebApiUtils.createResultSetFromData(lHeaderList, gContentList);
		m_binder.addResultSet(resultSetName, outputResultSet);

	}

	public void getSocialMediaBlogsByTagName() throws MalformedURLException, IOException, DataException
	{
		SystemUtils.trace(TRACE_NAME, "Social Media By TagName service is runnig : ");
		String socialMediaTags = null;
		String resultSetName = m_currentAction.getParamAt(0);
		String socialMediaURL = SharedObjects.getEnvironmentValue("SocialMediaUrl");
		String socialMediaTagArray[]=null;
		socialMediaTags = m_binder.getLocal("SocialMediaTags");
		String existingSocialMediaURL=null;
		if(socialMediaTags!=null && !("".equalsIgnoreCase(socialMediaTags)))
		{
			socialMediaTagArray = socialMediaTags.split(",");
		}
		else
		{
			DataResultSet lEmptyResultset = new DataResultSet();
			m_binder.addResultSet(resultSetName, lEmptyResultset);
		}
		
		
		ArrayList<String> lHeaderList = new ArrayList<String>();
		lHeaderList.add("Title");
		lHeaderList.add("Thumbnail");
		lHeaderList.add("Url");

		SystemUtils.trace(TRACE_NAME, "Social Blogs Resultset: " + resultSetName);
		SystemUtils.trace(TRACE_NAME, "Social Media URL: " + socialMediaURL);
		SystemUtils.trace(TRACE_NAME, "Social Media Tags: " + socialMediaTags);

		if ((socialMediaURL) != null)
		{
			socialMediaURL = socialMediaURL.replace("REPLACE_JSON", "get_tag_posts");
			existingSocialMediaURL = socialMediaURL;
		}
		SystemUtils.trace(TRACE_NAME, "Final Social Media URL: " + socialMediaURL);
		int count = 0;
		initializeProxyEnvironment();
		gContentList.clear();
		SystemUtils.trace(TRACE_NAME, "Social Media Tags Count: " + socialMediaTagArray.length);
		while(gContentList.size()<9 && count < socialMediaTagArray.length)
		{
			socialMediaURL=existingSocialMediaURL;
			socialMediaURL = socialMediaURL.replace("REPLACE_COUNT", Integer.toString(3-(gContentList.size()/3)));
			socialMediaURL = socialMediaURL.replace("REPLACE_SLUG", socialMediaTagArray[count]);
			SystemUtils.trace(TRACE_NAME, "Social Media Blog URL for "+socialMediaTagArray[count]+": " + socialMediaURL);
			SystemUtils.trace(TRACE_NAME, "REPLACE_COUNT: " + Integer.toString(3-(gContentList.size()/3)));
			SystemUtils.trace(TRACE_NAME, "REPLACE_SLUG: " + socialMediaTagArray[count]);
			parseJSONObject(executeURL(socialMediaURL));
			count++;
		}
		ResultSet outputResultSet = HaysWebApiUtils.createResultSetFromData(lHeaderList, gContentList);
		m_binder.addResultSet(resultSetName, outputResultSet);

	}

	public void parseJSONObject(String pResponse)
	{
		JSONObject jsonObject = null;
		JSONArray posts = null;
		JSONArray thumbnailsArr = null;
		
		try
		{
			jsonObject = new JSONObject(pResponse);
			posts = (JSONArray) jsonObject.get(POSTS);
			
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		
		for (int i = 0; i < posts.length(); i++)
		{
			try
			{
				gContentList.add(posts.getJSONObject(i).get(TITLE).toString());
			}
			catch (Exception e)
			{
				gContentList.add("");
			}
			try
			{
				
				gContentList.add(posts.getJSONObject(i).getJSONObject(THUMBNAIL_IMAGES)
						.getJSONObject(THUMBNAIL).get(URL).toString());
				
			}
			catch (Exception e)
			{
				gContentList.add("");
			}
			try
			{
				gContentList.add(posts.getJSONObject(i).get(URL).toString());
			}
			catch (Exception e)
			{
				gContentList.add("");
			}			
		}
		SystemUtils.trace(TRACE_NAME, "Social Blogs: " + gContentList.toString());
	}

	public String executeURL(String pUrl) throws IOException
	{
		URL url = null;
		url = new URL(null, pUrl, new sun.net.www.protocol.http.Handler());
		HttpURLConnection conn = null;
		conn = (HttpURLConnection)url.openConnection();

		conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.setUseCaches(false);
		conn.setRequestProperty("Content-Type", "application/json");
		DataInputStream input;
		input = new DataInputStream(conn.getInputStream());
		SystemUtils.trace(TRACE_NAME, "Point A");
		String str;
		StringBuilder sb = new StringBuilder();
		String ls = System.getProperty("line.separator");
		while (null != ((str = input.readLine())))
		{
			sb.append(str);
			sb.append(ls);
		}
		input.close();
		SystemUtils.trace(TRACE_NAME, "Social Blog Title: " + sb.toString());
		return sb.toString();
	}

	public void initializeProxyEnvironment()
	{
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

}
