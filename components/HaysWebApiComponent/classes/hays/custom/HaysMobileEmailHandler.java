package hays.custom;

import hays.com.commonutils.EntityHaysWebsites;
import hays.com.commonutils.HaysWebApiUtils;
import intradoc.common.DynamicHtml;
import intradoc.common.ExecutionContext;
import intradoc.common.ExecutionContextAdaptor;
import intradoc.common.IdcLocale;
import intradoc.common.LocaleResources;
import intradoc.common.LocaleUtils;
import intradoc.common.ParseSyntaxException;
import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.data.DataException;
import intradoc.data.DataResultSet;
import intradoc.server.DataLoader;
import intradoc.server.PageMerger;
import intradoc.server.PageMergerData;
import intradoc.server.ServiceHandler;
import intradoc.shared.SharedObjects;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class HaysMobileEmailHandler extends ServiceHandler
{
	String TRACE_NAME = "webAPI_EMail";
	ExecutionContext ctx = new ExecutionContextAdaptor();

	public void sendEmail() throws DataException, ServiceException
	{
		Map<String, String> params = new HashMap<String, String>();
		params.put("statusCode", "UC000");
		params.put("statusMessage", "wwWebApiOKMsg");
		params.put("response", "true");

		String templateName = m_binder.getLocal("action");
		String email = m_binder.getLocal("toemail");
		setLocale();
		validateParameters(email, templateName, params);
		if (params.get("response") != null && params.get("response").equals("true"))
		{
			SystemUtils.trace(TRACE_NAME, "Send Email parameters passed");
			if (templateName.indexOf(",") > 0)
			{
				String templates[] = templateName.split(",");
				for (int i = 0; i < templates.length; i++)
				{
					createEmailMessage(templates[i], params);

				}
			}
			else
			{
				SystemUtils.trace(TRACE_NAME, "Function createEmailMessage called in else condition");
				createEmailMessage(templateName, params);
			}
		}
		m_binder.putLocal("sendEmailStatus", "" + params.get("response"));
		this.m_binder.putLocal("StatusMessage", LocaleUtils.encodeMessage(params.get("statusMessage"), null));
		this.m_binder.putLocal("StatusCode", params.get("statusCode"));

		this.m_binder.removeLocal("TemplateFilePath");
		this.m_binder.removeLocal("TemplateClass");
		this.m_binder.removeLocal("TemplateType");
		this.m_binder.removeLocal("TemplateName");

		this.m_binder.removeLocal("charset");
		this.m_binder.removeLocal("siteLabel");
		this.m_binder.removeLocal("isAbsoluteCgi");
		this.m_binder.removeLocal("text");
		this.m_binder.removeLocal("site");
		this.m_binder.removeLocal("action");
		this.m_binder.removeLocal("locale");
		this.m_binder.removeLocal("toemail");
		this.m_binder.removeResultSet("LOCALE_DETAILS");
		this.m_binder.removeResultSet("DomainMap");
	}

	private void validateParameters(String email, String templateName, Map<String, String> params)
	{
		EntityHaysWebsites lEntityHaysWebsites = null;
		try
		{
			String lLocale = m_binder.getLocal("locale");
			SystemUtils.trace(TRACE_NAME, "lLocale: " + lLocale);
			SystemUtils.trace(TRACE_NAME, "Template Name: " + templateName);

			lEntityHaysWebsites = HaysWebApiUtils.getHaysWebsitesData((DataResultSet) this.m_binder.getResultSet("LOCALE_DETAILS"));

			SystemUtils.trace(TRACE_NAME, "Website ID: " + lEntityHaysWebsites.getlSiteId());
			m_binder.putLocal("siteId", lEntityHaysWebsites.getlSiteId());
			m_binder.putLocal("domainId", lEntityHaysWebsites.getlDomainId());
			String emailRegex = "^[a-z][a-z|0-9|]*([_][a-z|0-9]+)*([.][a-z|0-9]+([_]"
					+ "[a-z|0-9]+)*)?@[a-z][a-z|0-9|]*\\.([a-z][a-z|0-9]*(\\.[a-z][a-z|0-9]*)?)$";
			if (lEntityHaysWebsites == null || lEntityHaysWebsites.getlDomainId() == null)
			{
				params.put("statusCode", "UC001");
				params.put("statusMessage", "wwInvalidSiteLocale");
				params.put("response", "false");
			}
			else if (email == null || email.isEmpty())
			{
				params.put("statusCode", "UC009");
				params.put("statusMessage", "wwInvalidEmailID");
				params.put("response", "false");
			}
			else if (email != null && !email.matches(emailRegex))
			{
				params.put("statusCode", "UC009");
				params.put("statusMessage", "wwInvalidEmailID");
				params.put("response", "false");
			}
			else if (templateName == null || templateName.isEmpty())
			{
				params.put("statusCode", "UC010");
				params.put("statusMessage", "wwInvalidAction");
				params.put("response", "false");
			}
		}
		catch (Exception e)
		{
			SystemUtils.trace(TRACE_NAME, e.getMessage());
		}
	}

	private void createEmailMessage(String action, Map<String, String> params)
	{
		String msg = null;
		try
		{

			String encoding = (String) this.m_service.getLocaleResource(LocaleUtils.ENCODING);
			if (encoding == null || encoding.length() == 0)
			{
				encoding = "utf-8";
			}
			m_binder.putLocal("charset", encoding);
			SystemUtils.trace(TRACE_NAME, "Email encoding:" + encoding);
			// Get the dynamic html for the mail page.
			PageMergerData.loadTemplateData(action, m_binder.getLocalData());
			SystemUtils.trace(TRACE_NAME, "LOCAL DATA :" + m_binder.getLocalData() + "*****************" + action);
			DataLoader.checkCachedPage(action, this.m_service);
			DynamicHtml html = SharedObjects.getHtmlPage(action);
			SystemUtils.trace(TRACE_NAME, "HTML :" + html);
			StringWriter sw = new StringWriter();
			html.outputHtml(sw, (PageMerger) this.m_service.getCachedObject("PageMerger"));
			msg = sw.toString();
			SystemUtils.trace(TRACE_NAME, "MESSAGE :" + msg);
			sw.close();

			sendMail(msg, params);
			SystemUtils.trace(TRACE_NAME, "LAST LINE .");
		}
		catch (IOException e)
		{
			params.put("statusCode", "UC010");
			params.put("statusMessage", "wwInvalidAction");
			params.put("response", "false");
			SystemUtils.trace(TRACE_NAME, e.getMessage().toString());
		}
		catch (ParseSyntaxException e)
		{
			params.put("statusCode", "UC010");
			params.put("statusMessage", "wwInvalidAction");
			params.put("response", "false");
			SystemUtils.trace(TRACE_NAME, e.getMessage().toString());
		}
		catch (Exception e)
		{
			params.put("statusCode", "UC010");
			params.put("statusMessage", "wwInvalidAction");
			params.put("response", "false");
			SystemUtils.trace(TRACE_NAME, e.getMessage().toString());
		}
	}

	public void sendMail(String htmlBody, Map<String, String> params)
	{
		try
		{
			String email = m_binder.getLocal("toemail");
			String action = m_binder.getLocal("action");
			String subject = LocaleResources.getString("wwEmail" + action.toLowerCase() + "Subject", ctx);
			String fromEmailAddress = SharedObjects.getEnvironmentValue("HaysEmailAddress");
			String mailhost = SharedObjects.getEnvironmentValue("HaysMailHost");

			Properties props = new Properties();
			props.setProperty("mail.transport.protocol", "smtp");
			props.setProperty("mail.host", mailhost);
			Session mailSession = Session.getDefaultInstance(props, null);
			Transport transport = null;
			transport = mailSession.getTransport();
			MimeBodyPart textPart = new MimeBodyPart();
			textPart.setContent(htmlBody, "text/html; charset=UTF-8");
			Multipart mp = new MimeMultipart();
			mp.addBodyPart(textPart);

			MimeMessage message = new MimeMessage(mailSession);
			message.setSubject(subject, "UTF-8");
			message.setContent(mp);
			message.setFrom(new InternetAddress(fromEmailAddress));
			InternetAddress[] TheAddresses = InternetAddress.parse(email);
			transport.connect();
			message.addRecipients(Message.RecipientType.TO, TheAddresses);
			transport.sendMessage(message, message.getRecipients(Message.RecipientType.TO));
			transport.close();
		}
		catch (MessagingException e)
		{
			params.put("statusCode", "UC008");
			params.put("statusMessage", "wwEmailSendingFailed");
			params.put("response", "false");
			SystemUtils.trace(TRACE_NAME, e.getMessage());
		}

	}

	public void setLocale()
	{
		String locale = m_binder.getLocal("locale");
		if (locale == null || locale.length() <= 0)
		{
			locale = "en-GB";
		}
		IdcLocale idcl = LocaleResources.getLocale(locale);
		ctx.setCachedObject("UserLocale", idcl);
	}
}
