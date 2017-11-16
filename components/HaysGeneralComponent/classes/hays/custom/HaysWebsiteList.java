package hays.custom;

import hays.com.commonutils.HaysWebApiUtils;
import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.data.DataException;
import intradoc.data.DataResultSet;
import intradoc.server.ServiceHandler;

public class HaysWebsiteList extends ServiceHandler
{
	public static final String TRACE_NAME = "HaysWebsiteList";

	public void getPrimaryWebsiteDetails() throws DataException, ServiceException
	{
		m_binder.putLocal("trace_name", TRACE_NAME);
		String providerName = "SystemDatabase";
		String QueryText = "SELECT COUNTRYNAMEISO AS COUNTRY,ISOCOUNTRYCODE,WEBSITEADDRESS FROM HAYSGLOBALCOUNTRYLIST ORDER BY COUNTRYNAMEISO";
		DataResultSet haysGlobalCountryListRS = null;

		haysGlobalCountryListRS = HaysWebApiUtils.executeHaysProviderSql(providerName, QueryText);
		SystemUtils.trace(TRACE_NAME, "haysWebsitesRS count : " + haysGlobalCountryListRS.getNumRows());
		m_binder.addResultSet("CountryWebsites", haysGlobalCountryListRS);
	}
}
