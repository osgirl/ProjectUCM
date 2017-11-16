package hays.com.specialisms;

import intradoc.common.LocaleUtils;
import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.data.DataException;
import intradoc.data.DataResultSet;
import intradoc.server.ServiceHandler;

public class Specialisms extends ServiceHandler
{
	public final static String TRACE_NAME = "webAPI_getSpecialismList";

	public void getSpecialismList() throws ServiceException, DataException
	{
		String resultSetName = this.m_currentAction.getParamAt(0);
		SystemUtils.trace(TRACE_NAME, "resultSetName " + resultSetName);

		String lLocale = m_binder.getLocal("locale");
		SystemUtils.trace(TRACE_NAME, "lLocale " + lLocale);

		m_binder.putLocal("metadata", "xCategory");
		m_binder.putLocal("ontClass", "hays:Specialism");
		m_binder.putLocal("language", lLocale.split("-")[0].trim());
		m_binder.putLocal("country", lLocale.split("-")[1].trim());
		SystemUtils.trace(TRACE_NAME, "metadata = " + m_binder.getLocal("metadata") + "   " + "ontClass = " + m_binder.getLocal("ontClass")
				+ "   " + "language = " + m_binder.getLocal("language") + "   " + "country = " + m_binder.getLocal("country"));
		m_service.executeServiceEx("ONT_GET_INDIVIDUALS", true);

		DataResultSet lDataResultSet = (DataResultSet) m_binder.getResultSet("IndividualsList");
		m_binder.clearResultSets();
		m_binder.addResultSet(resultSetName, lDataResultSet);
		m_binder.removeLocal("sitelocale");
		this.m_binder.putLocal("StatusMessage", LocaleUtils.encodeMessage("wwWebApiOKMsg", null));
		this.m_binder.putLocal("StatusCode", "UC000");
	}
}
