package hays.custom;

import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.data.DataException;
import intradoc.server.ServiceHandler;
import intradoc.shared.SharedObjects;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HaysSaveAlerts extends ServiceHandler
{
	public final static String TRACE_NAME = "HAYS_SAVE_ALERTS";

	public void saveAlertsAnonymous() throws ServiceException, DataException
	{
		SystemUtils.trace(TRACE_NAME, "MBINDER VALUES " + m_binder);
		this.m_binder.putLocal("statuscode", "WC002");
		this.m_binder.putLocal("statusmsg", "Unable to process request");
		String payLoad = null;
		URLConnection conn = null;

		try
		{
			payLoad = createPayLoad();
			String SAVEALERTURL = SharedObjects.getEnvironmentValue("WC_SAVE_ALERTS");
//			String EXTERNALPROXYHOST = SharedObjects.getEnvironmentValue("PROXY_HOST");
//			String EXTERNALPROXYPORT = SharedObjects.getEnvironmentValue("PROXY_PORT");
//			final String EXTERNALPROXYUSER = SharedObjects.getEnvironmentValue("PROXY_AUTH_USER");
//			final String EXTERNALPROXYPASS = SharedObjects.getEnvironmentValue("PROXY_AUTH_PASSWORD");
//
//			System.setProperty("https.proxyUser", EXTERNALPROXYUSER);
//			System.setProperty("https.proxyPassword", EXTERNALPROXYPASS);
//
//			System.setProperty("https.proxyHost", EXTERNALPROXYHOST);
//			System.setProperty("https.proxyPort", EXTERNALPROXYPORT);

			CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
			//URL url = new URL(null, SAVEALERTURL, new sun.net.www.protocol.https.Handler());
			URL url = new URL(SAVEALERTURL);
			conn = url.openConnection();
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			conn.setRequestProperty("Content-Type", "application/json");
			DataOutputStream printout;
			DataInputStream input;
			SystemUtils.trace(TRACE_NAME, "Point A");

			printout = new DataOutputStream(conn.getOutputStream());
			SystemUtils.trace(TRACE_NAME, "Point B");
			String content = payLoad;
			printout.writeBytes(content);
			SystemUtils.trace(TRACE_NAME, "Point B1");
			printout.flush();
			printout.close();
			SystemUtils.trace(TRACE_NAME, "Point B2");
			input = new DataInputStream(conn.getInputStream());
			SystemUtils.trace(TRACE_NAME, "Point C");
			String str;
			while (null != ((str = input.readLine())))
			{
				{
					JSONObject jsonObjOutput = null;
					jsonObjOutput = new JSONObject(str);
					SystemUtils.trace(TRACE_NAME, "RESPONSE VALUES " + jsonObjOutput.toString());
					Iterator<String> keys = jsonObjOutput.keys();
					while (keys.hasNext())
					{
						String type = (String) keys.next();
						this.m_binder.putLocal(type, jsonObjOutput.get(type).toString());
					}
				}
			}
			input.close();
		}
		catch (MalformedURLException e)
		{
			SystemUtils.trace(TRACE_NAME, "EXCEPTION " + e.toString());
		}
		catch (JSONException e)
		{
			SystemUtils.trace(TRACE_NAME, "EXCEPTION " + e.toString());
		}
		catch (IOException e)
		{
			SystemUtils.trace(TRACE_NAME, "EXCEPTION " + e.toString());
		}
	}

	public String createPayLoad() throws JSONException, DataException
	{
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("alertname", getData("alertname"));
		jsonObj.put("domainid", getInt(getData("domainid")));
		jsonObj.put("email", getData("email"));
		jsonObj.put("frequency", getInt(getData("frequency")));
		jsonObj.put("isonlyjobtitle", getData("isonlyjobtitle"));
		jsonObj.put("jobtitle", getData("jobtitle"));
		jsonObj.put("keywords", getData("keywords"));
		jsonObj.put("languageid", getInt(getData("languageid")));
		jsonObj.put("latitude", getInt(getData("latitude")));
		jsonObj.put("levelno", getInt(getData("levelno")));
		jsonObj.put("locale", getData("locale"));
		jsonObj.put("location", getData("location"));
		jsonObj.put("locationid", getData("locationid"));
		jsonObj.put("longitude", getInt(getData("longitude")));
		jsonObj.put("postcode", getData("postcode"));
		jsonObj.put("sectorids", getData("sectorids"));
		jsonObj.put("specialism", getData("specialism"));
		jsonObj.put("jobtypes", getData("jobtypes"));
		jsonObj.put("anonymousalert", "Y");

		JSONObject lSalaryTemp = new JSONObject();
		lSalaryTemp.put("id", new JSONObject().put("jobtypeid", 1));
		lSalaryTemp.put("maxsalary", getInt(getData("maxSalary_t")));
		lSalaryTemp.put("minsalary", getInt(getData("minSalary_t")));
		lSalaryTemp.put("salarycurrency", getData("salaryType_t"));
		lSalaryTemp.put("salarytype", getData("salaryType_t"));

		JSONObject lSalaryPerm = new JSONObject();
		lSalaryPerm.put("id", new JSONObject().put("jobtypeid", 2));
		lSalaryPerm.put("maxsalary", getInt(getData("maxSalary_p")));
		lSalaryPerm.put("minsalary", getInt(getData("minSalary_p")));
		lSalaryPerm.put("salarycurrency", getData("salaryType_p"));
		lSalaryPerm.put("salarytype", getData("salaryType_p"));

		JSONObject lSalaryContract = new JSONObject();
		lSalaryContract.put("id", new JSONObject().put("jobtypeid", 3));
		lSalaryContract.put("maxsalary", getInt(getData("maxSalary_c")));
		lSalaryContract.put("minsalary", getInt(getData("minSalary_c")));
		lSalaryContract.put("salarycurrency", getData("salaryType_c"));
		lSalaryContract.put("salarytype", getData("salaryType_c"));

		JSONArray lSalaryCollection = new JSONArray();
		lSalaryCollection.put(lSalaryTemp);
		lSalaryCollection.put(lSalaryPerm);
		lSalaryCollection.put(lSalaryContract);
		jsonObj.put("alertprofilesalaryCollection", lSalaryCollection);

		String payoadString = jsonObj.toString();
		SystemUtils.trace(TRACE_NAME, "PAYLOAD_STRING" + payoadString);
		return payoadString;
	}

	public Integer getInt(String pData)
	{
		Integer lReturnValue = null;
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
		return returnString;
	}
}
