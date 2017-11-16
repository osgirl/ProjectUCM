package hays.custom.filters;

import hays.com.commonutils.HaysWebApiUtils;
import intradoc.common.LocaleUtils;
import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.data.DataException;
import intradoc.data.DataResultSet;
import intradoc.server.ServiceHandler;
import intradoc.shared.SharedObjects;

import org.apache.commons.codec.binary.Base64;

public class HaysRegularMarketingReport extends ServiceHandler
{
	public static final String TRACE_NAME = "HaysRegularMarketingReport";
	public static final String QUOTES = "\"";
	public static final String COMMA = ",";
	public static final String NEW_LINE = "\n";

	public void sendRegularMarketingReport() throws DataException, ServiceException
	{
		//QregularMarketingReport,systemdatabase
		String searchQuery = this.m_currentAction.getParamAt(0);
		String dataSource = this.m_currentAction.getParamAt(1);

		SystemUtils.trace(TRACE_NAME, "searchQuery : " + searchQuery);
		SystemUtils.trace(TRACE_NAME, "dataSource : " + dataSource);

		DataResultSet reportContentsRS = HaysWebApiUtils.executeHaysProviderQuery(dataSource, searchQuery, m_binder);

		SystemUtils.trace(TRACE_NAME, "reportContentsRS.getNumRows() : " + reportContentsRS.getNumRows());

		if (reportContentsRS != null && reportContentsRS.getNumRows() > 0)
		{
			StringBuilder attachmentContentSB = new StringBuilder();
			StringBuilder tempSB = new StringBuilder();
			int lHeadSize = reportContentsRS.getNumFields();
			for (int i = 0; i < lHeadSize; i++)
			{
				tempSB.append(reportContentsRS.getFieldName(i) + ",");
			}
			tempSB.deleteCharAt(tempSB.length() - 1);
			tempSB.append(NEW_LINE);
			attachmentContentSB.append(tempSB);

			do
			{
				tempSB.setLength(0);
				for (int i = 0; i < lHeadSize; i++)
				{
					tempSB.append(QUOTES).append(reportContentsRS.getStringValue(i)).append(QUOTES).append(COMMA);
				}
				attachmentContentSB.append(tempSB).append(NEW_LINE);
			}
			while (reportContentsRS.next());
			SystemUtils.trace(TRACE_NAME, "built the mail contents");

			byte[] bytes = attachmentContentSB.toString().getBytes();
			String encodedString = Base64.encodeBase64String(bytes);

			SystemUtils.trace(TRACE_NAME, "encoded the mail contents");
			m_binder.putLocal("DataFormName", "CustomForm2");
			m_binder.putLocal("emailTemplate", "fullyCustomised");
			m_binder.putLocal("locale", "en-GB");
			m_binder.putLocal("domainId", "1");

			m_binder.putLocal("AttachedDocumentContent", encodedString);
			m_binder.putLocal("AttachedDocument", "MonthlyReport.csv");
			m_binder.putLocal("email", SharedObjects.getEnvironmentValue("HaysEmailAddress"));
			m_binder.putLocal("Subject", "Regular Marketing Report");
			m_binder.putLocal("EmailID", SharedObjects.getEnvironmentValue("MONTHLY_REPORT_RECIPIENTS"));
			m_binder.putLocal("text1", "Hi,");
			m_binder.putLocal("text2", "Please find the report attached.");

			SystemUtils.trace(TRACE_NAME, "Params : " + m_binder);

			m_service.executeServiceEx("HAYS_MAIL", true);
		}
		else
		{
			m_binder.putLocal("StatusMessage", "No content found.");
		}
		m_binder.clearResultSets();
		this.m_binder.putLocal("StatusMessage", LocaleUtils.encodeMessage("wwWebApiOKMsg", null));
		this.m_binder.putLocal("StatusCode", "UC000");
	}

}
