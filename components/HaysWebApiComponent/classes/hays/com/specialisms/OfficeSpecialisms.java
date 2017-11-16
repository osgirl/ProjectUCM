package hays.com.specialisms;

import hays.com.commonutils.EntityHaysWebsites;
import hays.com.commonutils.HaysWebApiUtils;
import intradoc.common.LocaleUtils;
import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.data.DataException;
import intradoc.data.DataResultSet;
import intradoc.server.ServiceHandler;
import intradoc.shared.SharedObjects;

public class OfficeSpecialisms extends ServiceHandler
{
	public final static String TRACE_NAME = "webAPI_getOfficeSpecialismList";

	public void getOfficeSpecialismList() throws ServiceException, DataException
	{
		String sqlServerproviderName = this.m_currentAction.getParamAt(0);
		String resultSetName = this.m_currentAction.getParamAt(1);
		String queryName = this.m_currentAction.getParamAt(2);
		SystemUtils.trace(TRACE_NAME, "resultSetName " + resultSetName);

		String lLocale = m_binder.getLocal("locale");
		String specialismid = m_binder.getLocal("specialismid");
		SystemUtils.trace(TRACE_NAME, "lLocale " + lLocale);

		EntityHaysWebsites lEntityHaysWebsites = HaysWebApiUtils.getHaysWebsitesData((DataResultSet) this.m_binder
				.getResultSet("LOCALE_DETAILS"));
		SystemUtils.trace(TRACE_NAME,
				"lCountryRegion=" + lEntityHaysWebsites.getlCountryRegion() + "  lDomainId=" + lEntityHaysWebsites.getlDomainId()
						+ "  lLanguageId=" + lEntityHaysWebsites.getlLanguageId());

		String[] OfficeLocatorUKCerowList = SharedObjects.getEnvironmentValue("OfficeLocatorUKCerowList").split(",");

		boolean officeLocatorCountry = false;
		if (!SharedObjects.getEnvironmentValue("OfficeLocatorUKCerowList").trim().equalsIgnoreCase(""))
		{
			for (String s : OfficeLocatorUKCerowList)
			{
				if (lLocale.endsWith(s))
					officeLocatorCountry = true;
			}
		}

		if ("apac".equalsIgnoreCase(lEntityHaysWebsites.getlCountryRegion()) || (officeLocatorCountry))
		{
			SystemUtils.trace(TRACE_NAME, "EXECUTING THE APAC BLOCK");

			m_binder.putLocal("metadata", "xCategory");
			
			if(specialismid == null || "null".equalsIgnoreCase(specialismid) || specialismid.trim().length() == 0){
				m_binder.putLocal("ontClass", "hays:Specialism");
			}
			else{
				SystemUtils.trace(TRACE_NAME, "specialismid :  " + specialismid);
				m_binder.putLocal("ontClass", "hays:Subspecialism");
				m_binder.putLocal("parentTerm", specialismid);
			}
			m_binder.putLocal("language", lLocale.split("-")[0].trim());
			m_binder.putLocal("country", lLocale.split("-")[1].trim());
			SystemUtils.trace(TRACE_NAME,
					"metadata = " + m_binder.getLocal("metadata") + "   " + "ontClass = " + m_binder.getLocal("ontClass") + "   "
							+ "language = " + m_binder.getLocal("language") + "   " + "country = " + m_binder.getLocal("country"));
			m_service.executeServiceEx("ONT_GET_INDIVIDUALS", true);

			DataResultSet lDataResultSet = (DataResultSet) m_binder.getResultSet("IndividualsList");
			m_binder.clearResultSets();
			m_binder.addResultSet(resultSetName, lDataResultSet);
			SystemUtils.trace(TRACE_NAME, "EXECUTING THE APAC BLOCK");
		}
		else
		{
			SystemUtils.trace(TRACE_NAME, "EXECUTING THE NON-APAC BLOCK");
			sqlServerproviderName = "SqlServer";
			m_binder.putLocal("DomainId", lEntityHaysWebsites.getlDomainId());
			m_binder.putLocal("LanguageId", lEntityHaysWebsites.getlLanguageId());
			DataResultSet lDataResultSet = HaysWebApiUtils.executeHaysProviderQuery(sqlServerproviderName, queryName, m_binder);
			m_binder.clearResultSets();
			m_binder.addResultSet(resultSetName, lDataResultSet);
		}
		m_binder.removeLocal("sitelocale");
		this.m_binder.putLocal("StatusMessage", LocaleUtils.encodeMessage("wwWebApiOKMsg", null));
		this.m_binder.putLocal("StatusCode", "UC000");
	}

}
