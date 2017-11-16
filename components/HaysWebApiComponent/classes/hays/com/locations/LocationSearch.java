package hays.com.locations;

import static hays.com.commonutils.HaysWebApiUtils.HandleExceptions;

import java.io.IOException;

import intradoc.common.LocaleUtils;
import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.data.DataException;
import intradoc.data.DataResultSet;
import intradoc.server.ServiceHandler;

public class LocationSearch extends ServiceHandler
{
	public final static String TRACE_NAME = "webAPI_searchLocations";
	public final static String NL = "\n";

	public void searchLocations() throws ServiceException, DataException
	{
		String lServiceName = "";
		String resultSetName = this.m_currentAction.getParamAt(0);
		String lLocale = getData("locale");
		String level = getData("level");
		String domainId = getData("domainId");
		m_binder.putLocal("loc_descr", "default_description");
		try {
			m_service.computeFunction("setLocale", new String[]{lLocale});
		} catch (IOException e) {
			SystemUtils.dumpException(TRACE_NAME, e);
		}
		
		SystemUtils.trace(TRACE_NAME, 
				"resultSetName " + resultSetName + NL 
				+ "lLocale " + lLocale + NL
				+ "level = " + level + NL
				+ "domainId = " + domainId + NL
				+ "loc_descr = " + m_binder.getLocal("loc_descr"));

		if (domainId != null && "".equalsIgnoreCase(domainId))
		{
			HandleExceptions(m_binder, "UC005", "wwInvalidDomainID");
		}

		if (level != null && !"".equalsIgnoreCase(level))
		{
			SystemUtils.trace(TRACE_NAME, "EXECUTING THE WITH LEVEL BLOCK");
			m_binder.putLocal("level", level);
			lServiceName = "SEARCH_LOCATIONS_OLD_APAC_WITHLEVEL";
		}
		else
		{
			SystemUtils.trace(TRACE_NAME, "EXECUTING THE NON LEVEL BLOCK");
			lServiceName = "SEARCH_LOCATIONS_OLD_APAC";
		}

		m_service.executeServiceEx(lServiceName, true);

		DataResultSet lDataResultSet = (DataResultSet) m_binder.getResultSet("LOCATION_RESULT_LIST_APAC");
		m_binder.clearResultSets();
		m_binder.addResultSet(resultSetName, lDataResultSet);
		m_binder.removeResultSet("LOCATION_RESULT_LIST_APAC");

		m_binder.removeLocal("sitelocale");
		this.m_binder.putLocal("StatusMessage", LocaleUtils.encodeMessage("wwWebApiOKMsg", null));
		this.m_binder.putLocal("StatusCode", "UC000");
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
          return returnString;

    }

    



}
