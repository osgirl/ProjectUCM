package hays.custom;

import hays.co.uk.HaysUtil;
import intradoc.common.DynamicHtml;
import intradoc.common.ExecutionContext;
import intradoc.common.ExecutionContextAdaptor;
import intradoc.common.IdcLocale;
import intradoc.common.LocaleResources;
import intradoc.common.LocaleUtils;
import intradoc.common.ServiceException;
import intradoc.common.StringUtils;
import intradoc.common.SystemUtils;
import intradoc.data.DataException;
import intradoc.data.DataSerializeUtils;
import intradoc.serialize.DataBinderLocalizer;
import intradoc.server.DataLoader;
import intradoc.server.PageMerger;
import intradoc.server.PageMergerData;
import intradoc.server.ServiceHandler;
import intradoc.shared.FieldDef;
import intradoc.shared.SharedObjects;
import intradoc.shared.ViewFields;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Properties;
import java.util.Vector;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.tika.config.TikaConfig;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;


/**
 * When this ServiceHandler is properly merged into the Content Server, this 
 * code can be called by any service type specified in the merged table
 */
public class HaysEmailHandlerAPAC extends ServiceHandler
{
	ExecutionContext ctx = new ExecutionContextAdaptor();
	String EMAIL_TEMPLATE_APAC = "JobApplyApac_EmailTemplate"; 

	/**
	 * Default constructor 
	 */
	public HaysEmailHandlerAPAC()

	{
	}

	/**
	 * This code will create a html page based on the data in the databinder,
	 * and email it to the list of addresses specified by the user.
	 */
	public void HaysMailApac() throws DataException, ServiceException
	{
		String fileLocation = m_binder.getLocal("AttachedDocument:path");
		String fileName = m_binder.getLocal("AttachedDocument");
		String emailConsultant = m_binder.getLocal("EmailConsultant");
		String jobTitle = m_binder.getLocal("JobTitle");
		String jobReference = m_binder.getLocal("JobReference");
		// obtain the list of addresses from the local data that the user specified
		String userEmail = m_binder.getLocal("Email");
		
		SystemUtils.trace("HaysMailApac", "fileLocation is" + fileLocation);
		SystemUtils.trace("HaysMailApac", "fileName is" + fileName);
		SystemUtils.trace("HaysMailApac", "emailConsultant is" + emailConsultant);
		SystemUtils.trace("HaysMailApac", "userEmail is" + userEmail);

		//int domainId = Integer.parseInt(m_binder.getLocal("domainId"));

		
		
		try
		{
			/* Pick up the email body from template page  Start*/
			String htmlBody = null;
			String templateName = EMAIL_TEMPLATE_APAC;
			String subject_ReferAFriend = "URGENT: A candidate is interested in your job";
			setLocale();
			boolean response = false;
			if (templateName != null && templateName.length() > 0 )
			{
				SystemUtils.trace("HaysMailApac", "emailTemplate" + templateName);
				htmlBody = createEmailMessage(templateName, userEmail, jobTitle, jobReference);

				//subject_ReferAFriend = LocaleResources.getString("wwRefereeSubject", ctx);
				response = sendMail(emailConsultant, fileLocation, fileName, htmlBody, subject_ReferAFriend);
			}
			m_binder.putLocal("mailResponse", new Boolean(response).toString());
			/* Pick up the email body from template page  End*/
		}
		catch (Exception e)
		{
			e.printStackTrace();
			m_binder.putLocal("mailResponse", "false");
		}
	}

	/**
	 * Utility function to add a new string to a vector, if it doesn't already
	 * exist in the vector. This is a case insensitive function.
	 */
	protected void addUniqueStringToVector(Vector vect, String str)
	{
		int num = vect.size();
		for (int i = 0; i < num; ++i)
		{
			String curStr = (String) vect.elementAt(i);
			if (curStr.equalsIgnoreCase(str))
			{
				return;
			}
		}
		vect.addElement(str);
	}

	public boolean sendMail(String toEmailAddress, String attachedFilePath, String fileName, String htmlBody, String subject)
	{
		SystemUtils.trace("HaysMailApac", "sendMail method start");
		try
		{
			String fromEmailAddress = SharedObjects.getEnvironmentValue("HaysEmailAddress");
			String mailhost = SharedObjects.getEnvironmentValue("HaysMailHost");

			File file = null;
			if (attachedFilePath != null && attachedFilePath.length() > 0)
			{
				file = new File(attachedFilePath);
			}

			SystemUtils.trace("HaysMailApac", "After Attachment block...............");
			if (file != null)
			{
				String fileLocation = m_binder.getLocal("AttachedDocument:path");
				boolean isDesiredExtension = false;
				long length = file.length();
				SystemUtils.trace("HaysMailApac", "File size is" + length);
				int dotposition = fileName.lastIndexOf(".");
				String ext = fileName.substring(dotposition + 1, fileName.length());
				SystemUtils.trace("HaysMailApac", "File extension is" + ext);
				long maxsize = 512000; //500 KB

				if (length > maxsize) // checking the file size
				{
					return false;
				}
				if (!("doc".equalsIgnoreCase(ext) || "docx".equalsIgnoreCase(ext) || "rtf".equalsIgnoreCase(ext) || "txt".equalsIgnoreCase(ext) || "pdf".equalsIgnoreCase(ext)))//checking the file extension
				{
					return false;
				}
                if(fileLocation!=null){
				isDesiredExtension=HaysUtil.getTikaCode(fileLocation);
				SystemUtils.trace("HaysMailApac", "getTikaCode value1"+isDesiredExtension);	
				if(!isDesiredExtension)
				{
				SystemUtils.trace("HaysMailApac","checking for ext");
				return isDesiredExtension ;
				}
				
					
				}
			}

			SystemUtils.trace("HaysMailApac", "To Emailaddress:" + toEmailAddress);
			SystemUtils.trace("HaysMailApac", "From Emailaddress:" + fromEmailAddress);
			SystemUtils.trace("HaysMailApac", "Email Subject:" + subject);
			SystemUtils.trace("HaysMailApac", "SMTP Server:" + mailhost);
			SystemUtils.trace("HaysMailApac", "attachedFilePath:" + attachedFilePath);
			SystemUtils.trace("HaysMailApac", "fileName:" + fileName);

			String EmailContent = htmlBody;
			SystemUtils.trace("HaysMailApac", "EmailContent:" + EmailContent);

			boolean debug = true;
			Properties props = new Properties();
			props.setProperty("mail.transport.protocol", "smtp");
			props.setProperty("mail.host", mailhost);
			Session mailSession = Session.getDefaultInstance(props, null);
			Transport transport = mailSession.getTransport();

			MimeMessage message = new MimeMessage(mailSession);
			message.setSubject(subject, "UTF-8");

			MimeBodyPart textPart = new MimeBodyPart();
			textPart.setContent(EmailContent, "text/html; charset=UTF-8");

			Multipart mp = new MimeMultipart();

			mp.addBodyPart(textPart);

			if (file != null && file.length() > 0)
			{
				MimeBodyPart attachFilePart = new MimeBodyPart();
				FileDataSource fds = new FileDataSource(file);
				attachFilePart.setDataHandler(new DataHandler(fds));
				int l = attachFilePart.getSize();
				SystemUtils.trace("HaysMailApac", "File size11 is" + l);
				attachFilePart.setFileName(fileName);
				mp.addBodyPart(attachFilePart);
			}

			message.setContent(mp);
			message.setFrom(new InternetAddress(fromEmailAddress));
			InternetAddress[] TheAddresses = InternetAddress.parse(toEmailAddress);
			transport.connect();
			message.addRecipients(Message.RecipientType.TO, TheAddresses);
			transport.sendMessage(message, message.getRecipients(Message.RecipientType.TO));
			transport.close();
			SystemUtils.trace("HaysMailApac", "sendMail method end");
			return true;
		}

		catch (Exception e)
		{
			SystemUtils.trace("HaysMailApac", "error:" + e.getMessage());
			e.printStackTrace();
		}
		return false;
	}


	private String createEmailMessage(String emailTemplate, String emailUser, String jobTitle, String jobReference)
	{
		SystemUtils.trace("HaysMailApac", "Email Template,createEmailMessage:" + emailTemplate);
		String msg = null;

		ViewFields fields = new ViewFields(this.m_service);

		fields.addStandardDocFields();
		fields.addDocDateFields(true, true);
		try
		{
			fields.addMetaFields(SharedObjects.getTable("DocMetaDefinition"));
		}
		catch (DataException ignore)
		{
			SystemUtils.trace("HaysMailApac", "error : " + ignore.toString());
		}

		Vector v = fields.m_viewFields;
		int size = v.size();
		for (int i = 0; i < size; i++)
		{
			FieldDef def = (FieldDef) v.elementAt(i);
			if (def.m_type != null && !def.m_type.equalsIgnoreCase("text"))
			{
				m_binder.m_blFieldTypes.put(def.m_name, def.m_type);
			}
			//SystemUtils.trace("HaysMailApac", "looping for field : " + def.m_name);
		}

		// Prepare binder for mail merge:
		DataBinderLocalizer localizer = new DataBinderLocalizer(m_binder, this.m_service);
		localizer.localizeBinder(DataBinderLocalizer.ALL);
		Properties oldLocalData = m_binder.getLocalData();
		Properties newLocalData = (Properties) oldLocalData.clone();

		m_binder.setLocalData(newLocalData);
		m_binder.putLocal("isAbsoluteWeb", "1");
		m_binder.putLocal("isAbsoluteCgi", "1");
		m_binder.putLocal("FULL_NAME", emailUser);
		m_binder.putLocal("JOBTITLE", jobTitle);
		m_binder.putLocal("REFERENCE", jobReference);
		try
		{

			String encoding = (String) this.m_service.getLocaleResource(LocaleUtils.ENCODING);
			if (encoding == null || encoding.length() == 0)
			{
				encoding = "utf-8";
			}
			String javaEncoding = DataSerializeUtils.getJavaEncoding(encoding.toLowerCase());
			if (javaEncoding == null)
			{
				javaEncoding = encoding;
			}
			m_binder.putLocal("charset", encoding);

			// Get the dynamic html for the mail page.
			PageMergerData.loadTemplateData(emailTemplate, m_binder.getLocalData());
			DataLoader.checkCachedPage(emailTemplate, this.m_service);
			DynamicHtml html = SharedObjects.getHtmlPage(emailTemplate);

			StringWriter sw = new StringWriter();
			html.outputHtml(sw, (PageMerger) this.m_service.getCachedObject("PageMerger"));
			sw.close();
			msg = sw.toString();
		}
		catch (Exception e)
		{
			SystemUtils.trace("HaysMailApac", "error : " + e.toString());
		}
		finally
		{
			m_binder.setLocalData(oldLocalData);
		}
		//SystemUtils.trace("HaysMailApac","Final output html "+msg);
		SystemUtils.trace("HaysMailApac", "Email body created successfully.");

		return msg;
	}

	public void setLocale()
	{
		String locale = m_binder.getLocal("locale");
		if (locale == null || locale.length() <= 0)
		{
			locale = "en-GB";
		}
		//SystemUtils.trace("HaysMailApac", "SiteLocale:"+locale);		
		IdcLocale idcl = LocaleResources.getLocale(locale);
		ctx.setCachedObject("UserLocale", idcl);
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
