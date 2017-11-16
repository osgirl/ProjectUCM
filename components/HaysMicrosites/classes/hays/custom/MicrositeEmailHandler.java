package hays.custom;

import hays.co.uk.HaysUtil;
import hays.com.career.dextra.emailService.ApplyStatus;
import hays.com.career.dextra.emailService.BinaryContent;
import hays.com.career.dextra.emailService.DaxtraJobApplication;
import hays.com.career.dextra.emailService.IJobService;
import hays.com.career.dextra.emailService.JobService;
import hays.com.career.dextra.emailService.ObjectFactory;
import intradoc.common.DynamicHtml;
import intradoc.common.LocaleUtils;
import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.data.DataException;
import intradoc.data.DataResultSet;
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
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
import javax.xml.namespace.QName;
//Import removed for automation
import org.jsoup.Jsoup;

import sitestudio.SSCommon;

/**
 * When this ServiceHandler is properly merged into the Content Server, this
 * code can be called by any service type specified in the merged table
 */
public class MicrositeEmailHandler extends ServiceHandler
{
	public static ObjectFactory objectFactory = new ObjectFactory();
	public static final String TRACE_NAME = "microsite_email";
	public final String EMAIL_DELEGATION_DEXTRA = "haysmicro";
	
	public MicrositeEmailHandler()
	{
	}
	
	public void delegateEmailAction() throws DataException, ServiceException
	{
		String delegationPointer = m_binder.getLocal("xPostingType");
		String xIsAdaptRecord = m_binder.getLocal("xIsAdaptRecord");
		String subject = "Job Application " + m_binder.getLocal("jobTitle")+" "+m_binder.getLocal("jobReference");
		String applicationEmail = m_binder.getLocal("applicationEmail");
		String employerEmailID = applicationEmail;
		SystemUtils.trace(TRACE_NAME, "Job posting type is : " + delegationPointer);
		SystemUtils.trace(TRACE_NAME, "applicationEmail, employerEmailID, subject: " + applicationEmail+","+employerEmailID+","+subject);
		if(EMAIL_DELEGATION_DEXTRA.equalsIgnoreCase(delegationPointer) && "Y".equalsIgnoreCase(xIsAdaptRecord))
		{
			micrositeDextraEmailWS(applicationEmail, employerEmailID, subject);
			SystemUtils.trace("TRACE_NAME", "Delegating the mail process to Dextra Handler.");
		}
		else
		{
			micrositeEmail(applicationEmail, employerEmailID, subject);
			SystemUtils.trace("TRACE_NAME", "Delegating the mail process to WebForm Handler.");
		}
	}
	
	public void micrositeDextraEmailWS(String applicationEmail, String employerEmailID, String subject) throws DataException, ServiceException
	{
		DataResultSet drsSearchResults =new DataResultSet();
		drsSearchResults=(DataResultSet)super.m_binder.getResultSet("LOCALE_DETAILS");
		
		//Hardcoded values
		final String HC_WHERE_HEAR = "";
		final String HC_EXP = "";
		final String HC_ELIDGIBLE = "Yes";
		
		
		String firstName = (super.m_binder.getLocal("full_name"));
		String lastName = (super.m_binder.getLocal("sur_name"));
		String email = (super.m_binder.getLocal("email"));
		String eligible = (HC_ELIDGIBLE);
		String whereHear = HC_WHERE_HEAR;
		String exp = HC_EXP;
		String mobile = (super.m_binder.getLocal("mobile"));
		String fileLocation = m_binder.getLocal("AttachedDocument:path");
		String fileName = m_binder.getLocal("AttachedDocument");
		
		String siteLocale = (super.m_binder.getLocal("siteLocale"));
		if(siteLocale.equalsIgnoreCase("it-ITL")){
			siteLocale = "it-IT";
		}
		String jobId = (super.m_binder.getLocal("job_ID"));
		String consultantEmailId = employerEmailID;//(super.m_binder.getLocal("employerEmailID"));
		String JobRef = (super.m_binder.getLocal("xRecordId"));
		String SpeculativeEmail = applicationEmail;//(super.m_binder.getLocal("xApplicationEmail"));
		String country = drsSearchResults.getStringValueByName("COUNTRY");
		String bestWayToContact = (super.m_binder.getLocal("waytocontact"));

		SystemUtils.trace(TRACE_NAME, "FormName is " + firstName);
		SystemUtils.trace(TRACE_NAME, "lastName is " + lastName);
		SystemUtils.trace(TRACE_NAME, "email is " + email);
		SystemUtils.trace(TRACE_NAME, "whereHear is " + whereHear);
		SystemUtils.trace(TRACE_NAME, "exp " + exp);
		SystemUtils.trace(TRACE_NAME, "mobile is " + mobile);
		SystemUtils.trace(TRACE_NAME, "fileLocation is " + fileLocation);
		SystemUtils.trace(TRACE_NAME, "eligible " + eligible);
		SystemUtils.trace(TRACE_NAME, "fileName is " + fileName);
		SystemUtils.trace(TRACE_NAME, "siteLocale is " + siteLocale);
		SystemUtils.trace(TRACE_NAME, "jobId is " + jobId);
		SystemUtils.trace(TRACE_NAME, "consultantEmailId is " + consultantEmailId);
		SystemUtils.trace(TRACE_NAME, "SpeculativeEmail is " + SpeculativeEmail);
		SystemUtils.trace(TRACE_NAME, "JobRef is " + JobRef);
		SystemUtils.trace(TRACE_NAME, "country is " + country);
		SystemUtils.trace(TRACE_NAME, "waytocontact is " + bestWayToContact);
		
		String endPoint = resolveEndPoint(siteLocale);		
        SystemUtils.trace(TRACE_NAME, "endPoint : " + endPoint);
		
		DaxtraJobApplication daxtraJobApplication = new DaxtraJobApplication();

		daxtraJobApplication.setMobilePhone(objectFactory.createDaxtraJobApplicationMobilePhone(mobile));
		daxtraJobApplication.setForeName(objectFactory.createDaxtraJobApplicationForeName(firstName));
		daxtraJobApplication.setExpInRecruitment(objectFactory.createDaxtraJobApplicationExpInRecruitment(exp));
		daxtraJobApplication.setCountry(objectFactory.createDaxtraJobApplicationCountry(country));
		daxtraJobApplication.setEmailAddress(objectFactory.createDaxtraJobApplicationEmailAddress(email));
		daxtraJobApplication.setAboutUsSource(objectFactory.createDaxtraJobApplicationAboutUsSource(whereHear));
		daxtraJobApplication.setIsEligible(objectFactory.createDaxtraJobApplicationIsEligible(eligible));
		daxtraJobApplication.setJobId(objectFactory.createDaxtraJobApplicationJobId(jobId));
		daxtraJobApplication.setLocale(objectFactory.createDaxtraJobApplicationLocale(siteLocale));
		daxtraJobApplication.setSurName(objectFactory.createDaxtraJobApplicationSurName(lastName));
		daxtraJobApplication.setSpeculativeEmailAddress(objectFactory.createDaxtraJobApplicationSpeculativeEmailAddress(SpeculativeEmail));
		daxtraJobApplication.setConsultantEmailId(objectFactory.createDaxtraJobApplicationConsultantEmailId(consultantEmailId));
		
		daxtraJobApplication.setJobRef(objectFactory.createDaxtraJobApplicationJobRef(JobRef));
		daxtraJobApplication.setDayTimePhone(objectFactory.createDaxtraJobApplicationDayTimePhone(""));
		daxtraJobApplication.setAdditionalInformation(objectFactory.createDaxtraJobApplicationAdditionalInformation(""));
		daxtraJobApplication.setBestWayToContact(objectFactory.createDaxtraJobApplicationBestWayToContact(bestWayToContact));

		if (null != jobId &&  jobId.length() !=0 && !jobId.equalsIgnoreCase(""))
		{
			daxtraJobApplication.setApplyMode(objectFactory.createDaxtraJobApplicationApplyMode("JobApply"));

		}
		else
		{
			daxtraJobApplication.setApplyMode(objectFactory.createDaxtraJobApplicationApplyMode("CVOnly"));
		}
		SystemUtils.trace(TRACE_NAME, "Apply Mode " + daxtraJobApplication.getApplyMode());
		SystemUtils.trace(TRACE_NAME, "daxtraJobApplication is " + daxtraJobApplication);
		BinaryContent cv = new BinaryContent();
		if (fileName != null && fileLocation != null && fileLocation.length() > 0)
		{
			cv.setFileName(objectFactory.createBinaryContentFileName(fileName));
			setBinaryContent(cv, fileLocation);
		}
		
		ApplyStatus applyStatus = new ApplyStatus();
		try
		{
			System.setProperty("http.proxyUser", "");
			System.setProperty("http.proxyPassword", "");
			System.setProperty("http.proxyHost", "");
			System.setProperty("http.proxyPort", "");
			
			//Modifying the URL as per site locale
			URL endpoint_new = new URL(endPoint);
			QName qname = new QName("http://tempuri.org/","JobService");
			
			JobService jobServiceClient = new JobService(endpoint_new,qname);
			IJobService iJobService = jobServiceClient.getBasicHttpBindingIJobService();

			applyStatus = iJobService.applyForJobUsingDaxtra(daxtraJobApplication, cv, null);
			SystemUtils.trace(TRACE_NAME, "status code is " + applyStatus.getStatus().getValue());
			SystemUtils.trace(TRACE_NAME, "status Exception is " + applyStatus.getException().getValue());
			if(!"FAILED".equalsIgnoreCase(applyStatus.getStatus().getValue()))
				m_binder.putLocal("mailResponse", new Boolean(true).toString());
			else
				m_binder.putLocal("mailResponse", new Boolean(false).toString());
		}
		catch (Exception e)
		{
			SystemUtils.trace(TRACE_NAME, "in Catch block status Exception is 1 " + e);
			SystemUtils.trace(TRACE_NAME, "status code is " + applyStatus.getStatus().getValue());
			SystemUtils.trace(TRACE_NAME, "status Exception is " + applyStatus.getException().getValue());
		}
	}
	/**
	 * This code will create a html page based on the data in the databinder,
	 * and email it to the list of addresses specified by the user.
	 */
	public void micrositeEmail(String applicationEmail, String employerEmailID, String subject) throws DataException, ServiceException
	{

		try
		{
						
			String fileLocation = m_binder.getLocal("AttachedDocument:path");
			String fileName = m_binder.getLocal("AttachedDocument");
			String employerEmail = employerEmailID;//m_binder.getLocal("xApplicationEmail");
			String jobSeekerEmail = m_binder.getLocal("email");		
			SystemUtils.trace(TRACE_NAME, "fileLocation: " + fileLocation);
			SystemUtils.trace(TRACE_NAME, "fileName: " + fileName);
			SystemUtils.trace(TRACE_NAME, "Employer EmailID: " + employerEmail);
			SystemUtils.trace(TRACE_NAME, "Jobseeker EmailID: " + jobSeekerEmail);
				
			DataResultSet drsSearchResults =new DataResultSet();
			drsSearchResults=(DataResultSet)super.m_binder.getResultSet("LOCALE_DETAILS");
			
			SystemUtils.trace(TRACE_NAME, "Locale_Details ResultSet Details: " + drsSearchResults);
			SystemUtils.trace(TRACE_NAME, "Locale_Details ResultSet numRows: " + drsSearchResults.getNumRows());
			SystemUtils.trace(TRACE_NAME, "Locale specific SiteId: " + drsSearchResults.getStringValueByName("SITEID"));
			
			m_binder.putLocal("siteId", drsSearchResults.getStringValueByName("SITEID"));

			/* Pick up the email body from template page Start */
			String htmlBody = null;
			String templateName = m_binder.getLocal("emailTemplate");
			boolean response = false;

			if (templateName != null && templateName.length() > 0 && templateName.indexOf(",") > 0)
			{
				SystemUtils.trace(TRACE_NAME, "Email Templates: " + templateName);
				String templates[] = templateName.split(",");
				String emailaddress = "";

				for (String singleTemplateName : templates)
				{
					htmlBody = createEmailMessageMicrosite(singleTemplateName);

					if ("microsite_jobapply_jobseeker".equalsIgnoreCase(singleTemplateName))
						emailaddress = jobSeekerEmail;
					else
						emailaddress = employerEmail;

					response = sendEmailMicrosite(emailaddress, fileLocation, fileName, htmlBody, subject);

				}
			}

			m_binder.putLocal("mailResponse", new Boolean(response).toString());

		}
		catch (Exception e)
		{
			// e.printStackTrace();
			m_binder.putLocal("mailResponse", "false");
		}

	}
	
	public void micrositeEmailForms() throws DataException, ServiceException
	{

		try
		{
			String fileLocation = m_binder.getLocal("AttachedDocument:path");
			String fileName = m_binder.getLocal("AttachedDocument");
			//String emailaddress = m_binder.getLocal("EmailID");
			String dataId = m_binder.getLocal("DataId");
			String emailaddress = SSCommon.doIncludeXml(this.m_service, dataId, "wcm:root/wcm:element[@name='ToEmailField']/text()");
			emailaddress  = Jsoup.parse(emailaddress).text();
			String subject = SSCommon.doIncludeXml(this.m_service, dataId, "wcm:root/wcm:element[@name='Heading']/text()");
			subject  = Jsoup.parse(subject).text();
			String htmlBody = null;
			String templateName = m_binder.getLocal("emailTemplate");
			boolean response = false;
			
			SystemUtils.trace(TRACE_NAME, "fileLocation: " + fileLocation);
			SystemUtils.trace(TRACE_NAME, "fileName: " + fileName);
			SystemUtils.trace(TRACE_NAME, "ToEmailID: " + emailaddress);
			SystemUtils.trace(TRACE_NAME, "Template Name: " + templateName);
			
			htmlBody = createEmailMessageMicrosite(templateName);
			response = sendEmailMicrosite(emailaddress, fileLocation, fileName, htmlBody, subject);				

			m_binder.putLocal("mailResponse", new Boolean(response).toString());
				
		}
		catch (Exception e)
		{
			// e.printStackTrace();
			m_binder.putLocal("mailResponse", "false");
		}

	}

	
	public boolean sendEmailMicrosite(String toEmailAddress, String attachedFilePath, String fileName, String htmlBody, String subject)
	{

		try
		{

			String fromEmailAddress = SharedObjects.getEnvironmentValue("MicrositeJobApplyEmail");
			String mailhost = SharedObjects.getEnvironmentValue("HaysMailHost");

			if (attachedFilePath != null && attachedFilePath.length() > 0)
			{
				String fileLocation = m_binder.getLocal("AttachedDocument:path");
				boolean isDesiredExtension = false;
				File file = new File(attachedFilePath);
				long length = file.length();
				SystemUtils.trace(TRACE_NAME, "File size: " + length);
				int dotposition = fileName.lastIndexOf(".");
				String ext = fileName.substring(dotposition + 1, fileName.length());
				SystemUtils.trace(TRACE_NAME, "File extension: " + ext);
				long maxsize = 512000; // 500 KB

				if (length > maxsize) // checking the file size
				{
					return false;
				}
				if (!("doc".equalsIgnoreCase(ext) || "docx".equalsIgnoreCase(ext) || "rtf".equalsIgnoreCase(ext)
						|| "txt".equalsIgnoreCase(ext) || "pdf".equalsIgnoreCase(ext)))// checking
																						// the
																						// file
																						// extension
				{
					return false;
				}
                if(fileLocation!=null){
				isDesiredExtension=HaysUtil.getTikaCode(fileLocation);
				SystemUtils.trace("TRACE_NAME", "getTikaCode value1"+isDesiredExtension);	
				if(!isDesiredExtension)
				{
				SystemUtils.trace("TRACE_NAME","checking for ext");
				return isDesiredExtension ;
				}
									
				}
			}

			SystemUtils.trace(TRACE_NAME, "To Emailaddress: " + toEmailAddress);
			SystemUtils.trace(TRACE_NAME, "From Emailaddress: " + fromEmailAddress);
			SystemUtils.trace(TRACE_NAME, "Email Subject: " + subject);
			SystemUtils.trace(TRACE_NAME, "SMTP Server: " + mailhost);
			SystemUtils.trace(TRACE_NAME, "attachedFilePath: " + attachedFilePath);
			SystemUtils.trace(TRACE_NAME, "fileName: " + fileName);

			Properties props = new Properties();
			props.setProperty("mail.transport.protocol", "smtp");
			props.setProperty("mail.host", mailhost);
			Session mailSession = Session.getDefaultInstance(props, null);
			Transport transport = mailSession.getTransport();

			MimeMessage message = new MimeMessage(mailSession);
			message.setSubject(subject, "UTF-8");

			MimeBodyPart textPart = new MimeBodyPart();
			textPart.setContent(htmlBody, "text/html; charset=UTF-8");

			Multipart mp = new MimeMultipart();

			mp.addBodyPart(textPart);

			if (!(" ".equalsIgnoreCase(attachedFilePath)) && attachedFilePath != null)
			{
				MimeBodyPart attachFilePart = new MimeBodyPart();
				FileDataSource fds = new FileDataSource(attachedFilePath);
				attachFilePart.setDataHandler(new DataHandler(fds));
				int l = attachFilePart.getSize();
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

			return true;
		}

		catch (Exception e)
		{

		}
		return false;
	}

	
	private String createEmailMessageMicrosite(String emailTemplate)
	{
		Properties oldLocalData = null;
		Properties newLocalData = null;
		String msg = null;
		try
		{

			SystemUtils.trace(TRACE_NAME, "Email Template Name: " + emailTemplate);

			ViewFields fields = new ViewFields(this.m_service);

			fields.addStandardDocFields();
			fields.addDocDateFields(true, true);
			try
			{
				fields.addMetaFields(SharedObjects.getTable("DocMetaDefinition"));
			}
			catch (DataException ignore)
			{
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
			}

			// Prepare binder for mail merge:
			DataBinderLocalizer localizer = new DataBinderLocalizer(m_binder, this.m_service);
			localizer.localizeBinder(DataBinderLocalizer.ALL);
			oldLocalData = m_binder.getLocalData();
			newLocalData = (Properties) oldLocalData.clone();

			m_binder.setLocalData(newLocalData);
			m_binder.putLocal("isAbsoluteWeb", "1");
			m_binder.putLocal("isAbsoluteCgi", "1");

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

		}
		finally
		{
			m_binder.setLocalData(oldLocalData);
		}

		return msg;
	}
	
	
	
	public void setBinaryContent(BinaryContent cv, String fileLocation)
	{
		SystemUtils.trace(TRACE_NAME, " inside setBinaryContent  ");

		File file = new File(fileLocation);
		try
		{
			FileInputStream fin = new FileInputStream(file);
			byte fileContent[] = new byte[(int) file.length()];
			fin.read(fileContent);
			cv.setBinary(objectFactory.createBinaryContentBinary(fileContent));
		}
		catch (FileNotFoundException e)
		{

			SystemUtils.trace(TRACE_NAME, "File not found strFileContent " + e);
		}
		catch (IOException ioe)
		{

			SystemUtils.trace(TRACE_NAME, "Exception while reading the file " + ioe);
		}

	}
	
	public String resolveEndPoint(String pSiteLocale)
	{
		String returnString  = "";
		String []countryFromLocale = null;
		String countryCode = "DEFAULT";
		if(pSiteLocale != null && !"".equals(pSiteLocale))
		{
			countryFromLocale = pSiteLocale.split("-");
			if(countryFromLocale.length > 1)
			{
				countryCode = countryFromLocale[1];
			}
		}
        SystemUtils.trace(TRACE_NAME,"countryCode is :::"+countryCode);	 
        returnString = SharedObjects.getEnvironmentValue("WSDL_ENDPOINT_MICROSITE_"+countryCode);
        return returnString;
	}
	
}
