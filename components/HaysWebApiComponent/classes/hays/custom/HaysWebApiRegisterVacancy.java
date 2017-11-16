package hays.custom;

import static hays.com.commonutils.HaysWebApiUtils.HandleExceptions;
import hays.com.commonutils.EntityHaysWebsites;
import hays.com.commonutils.HaysWebApiUtils;
import intradoc.common.LocaleUtils;
import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.data.DataException;
import intradoc.data.DataResultSet;
import intradoc.server.ServiceHandler;

public class HaysWebApiRegisterVacancy  extends ServiceHandler
{
	public final static String TRACE_NAME = "webAPI_webApiRegisterVacancy";

	public void webApiRegisterVacancy() throws ServiceException, DataException
	{
		
			try
			{
				String lLocale = m_binder.getLocal("locale");
				SystemUtils.trace(TRACE_NAME, "lLocale " + lLocale);
				EntityHaysWebsites lEntityHaysWebsites = HaysWebApiUtils.getHaysWebsitesData((DataResultSet) this.m_binder
						.getResultSet("LOCALE_DETAILS"));
				m_binder.putLocal("DataFormName","CustomForm2");
				m_binder.putLocal("emailTemplate","webApiRYVacancyConfigurable");
				m_binder.putLocal("domainId",lEntityHaysWebsites.getlDomainId());
				
				m_binder.putLocal("AttachedDocumentContent",getData("attachmentcontent"));
				m_binder.putLocal("AttachedDocument",getData("attachmentfilename"));
				m_binder.putLocal("email",getData("fromEmail"));
				m_binder.putLocal("Subject",getData("subject"));
				m_binder.putLocal("EmailID",getData("toEmail"));
				
				SystemUtils.trace(TRACE_NAME, "Params : " + m_binder);
				
				m_service.executeServiceEx("HAYS_MAIL", true);
				
				this.m_binder.putLocal("StatusMessage", LocaleUtils.encodeMessage("wwWebApiOKMsg", null));
				this.m_binder.putLocal("StatusCode", "UC000");
			}
			catch (Exception e)
			{
				HandleExceptions(m_binder, "UC015", "wwMandatoryParams");
			}
		
		
	}
	
	public String getData(String pParamName) throws ServiceException
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
			throw new ServiceException("You must specify " + pParamName);
		}
		return returnString;
	}
}
