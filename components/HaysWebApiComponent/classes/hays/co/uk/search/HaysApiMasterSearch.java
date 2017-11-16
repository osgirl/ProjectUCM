package hays.co.uk.search;

import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.data.DataException;
import intradoc.data.DataResultSet;
import intradoc.server.ServiceHandler;
import intradoc.shared.SharedObjects;

public class HaysApiMasterSearch extends ServiceHandler
{

	public final static String TRACE_NAME = "HAYS_API_MASTER_SEARCH";
	public static final String RESULTSET_HAYSLOCALEDETAILS = "LOCALE_DETAILS";

	public void selectSearchMethod() throws DataException, ServiceException
	{
		String[] GoogleSearchCountryList = SharedObjects.getEnvironmentValue("GoogleSearchFormAPI").split(",");
		boolean googleSearchCountry = false;
		boolean islinkedin = false;
		DataResultSet drsSearchResults = null;
		String linkedInParam = getData("islinkedin");
		if ("Y".equalsIgnoreCase(linkedInParam))
		{
			islinkedin = true;
		}
		if (!islinkedin)
		{
			drsSearchResults = new DataResultSet();
			drsSearchResults = (DataResultSet) super.m_binder.getResultSet(RESULTSET_HAYSLOCALEDETAILS);
			SystemUtils.trace(TRACE_NAME, "ISO Country Code: " + drsSearchResults.getStringValueByName("ISOCOUNTRYCODE").toString());
			for (String s : GoogleSearchCountryList)
			{
				if (drsSearchResults.getStringValueByName("ISOCOUNTRYCODE").equalsIgnoreCase(s))
				{
					googleSearchCountry = true;
					break;
				}
			}
		}
		String contentType = getData(IHaysSearchConstants.CONTENT_TYPE);

		if (!islinkedin && googleSearchCountry && "Jobs".equals(contentType))
		{
			String Env_CrossCountryEnabledCountries = SharedObjects.getEnvironmentValue("CrossCountryEnabledCountries_API");
			if(Env_CrossCountryEnabledCountries != null && !"".equalsIgnoreCase(Env_CrossCountryEnabledCountries.trim()))
			{
				String[] CrossCountryEnabledCountries = Env_CrossCountryEnabledCountries.split(",");
				boolean isCrossCountry = false;
				for (String s : CrossCountryEnabledCountries)
				{
					if (drsSearchResults.getStringValueByName("ISOCOUNTRYCODE").equalsIgnoreCase(s))
					{
						isCrossCountry = true;
						break;
					}
				}
				if(isCrossCountry)
				{
					m_binder.putLocal("isCrossCountry", "Y");
				}
				else
				{
					m_binder.putLocal("isCrossCountry", "N");
				}
				
			}
			m_service.executeServiceEx("HAYS_API_GOOGLE_SEARCH_RESULTS", true);
			SystemUtils.trace(TRACE_NAME, "Executed service :: HAYS_API_GOOGLE_SEARCH_RESULTS");
		}
		else
		{
			m_service.executeServiceEx("HAYS_API_GET_SEARCH_RESULTS", true);
			SystemUtils.trace(TRACE_NAME, "Executed service :: HAYS_API_GET_SEARCH_RESULTS");
		}
	}
	
	public String getData(String pParamName)
	{
		String returnString = "";
		try
		{
			returnString = m_binder.get(pParamName);
			if("null".equalsIgnoreCase(returnString))
			{
				returnString = "";
			}

		}
		catch (Exception e)
		{

		}
		return returnString.trim();
	}

}
