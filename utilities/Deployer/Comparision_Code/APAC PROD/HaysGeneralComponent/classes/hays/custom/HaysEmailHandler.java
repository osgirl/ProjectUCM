package hays.custom;

import static intradoc.shared.SharedObjects.getEnvironmentValue;
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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.StringWriter;
import java.util.HashMap;
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

import org.apache.commons.codec.binary.Base64;

/**
 * When this ServiceHandler is properly merged into the Content Server, this 
 * code can be called by any service type specified in the merged table
 */
public class HaysEmailHandler extends ServiceHandler
{
	ExecutionContext ctx = new ExecutionContextAdaptor();
	String attachmentContent = null;

	/**
	 * Default constructor 
	 */
	public HaysEmailHandler()

	{
	}

	/**
	 * This code will create a html page based on the data in the databinder,
	 * and email it to the list of addresses specified by the user.
	 */
	public void HaysMail() throws DataException, ServiceException
	{

		String formName = m_binder.getLocal("DataFormName");
		SystemUtils.trace("HaysMail", "FormName is" + formName);
		String fileLocation = m_binder.getLocal("AttachedDocument:path");
		String fileName = m_binder.getLocal("AttachedDocument");
		attachmentContent = m_binder.getLocal("AttachedDocumentContent");
		String email = m_binder.getLocal("EmailID");
		SystemUtils.trace("HaysMail", "fileLocation is" + fileLocation);
		SystemUtils.trace("HaysMail", "fileName is" + fileName);
		SystemUtils.trace("HaysMail", "filecontent is" + attachmentContent);
		SystemUtils.trace("HaysMail", "EmailID is" + email);
		String final_emailStr = "";
		String referee_email = m_binder.getLocal("emailaddress");
		String friend_email = m_binder.getLocal("friends_email");
		int domainId = Integer.parseInt(m_binder.getLocal("domainId"));

		// obtain the list of addresses from the local data that the user specified
		String str = m_binder.getLocal("Email");
		// parse the list of comma-seperated addresses, and put it in a vector
		Vector emailAdrs = StringUtils.parseArray(str, ',', '^');
		// create a comma seprated string from the vector. This is the opposite function of 'parseArray'
		String emailStr = StringUtils.createString(emailAdrs, ',', '^');
		String emailDropdown_str = m_binder.getLocal("PreferredLocation");
		if (!(" ".equalsIgnoreCase(emailDropdown_str)) && emailDropdown_str != null && !("undefined".equalsIgnoreCase(emailDropdown_str)))
		{
			final_emailStr = emailStr + "," + emailDropdown_str;
		}
		else
		{
			final_emailStr = emailStr;
		}
		if ("CustomForm1".equalsIgnoreCase(formName) || "CustomForm2".equalsIgnoreCase(formName))
		{
			final_emailStr = email;
		}
		try
		{
			/* Pick up the email body from template page  Start*/
			String htmlBody = null;
			String templateName = m_binder.getLocal("emailTemplate");
			String subject_ReferAFriend = "";
			setLocale();
			boolean response = false;
			if (templateName != null && templateName.length() > 0 && templateName.indexOf(",") > 0)
			{
				SystemUtils.trace("HaysMail", "emailTemplate" + templateName);
				String templates[] = templateName.split(",");
				for (String singleTemplateName : templates)
				{
					SystemUtils.trace("HaysMail", "Email Template: " + singleTemplateName);
					htmlBody = createEmailMessage(singleTemplateName);

					if ("ReferAFriend_EmailTemplate_Referee".equalsIgnoreCase(singleTemplateName) && (domainId == 3 || domainId == 38 || domainId == 1005))
					{
						final_emailStr = referee_email;
						subject_ReferAFriend = LocaleResources.getString("wwRefereeSubject", ctx);
						response = sendMail(final_emailStr, fileLocation, fileName, htmlBody, subject_ReferAFriend);
					}
					else if ("ReferAFriend_EmailTemplate_Friend".equalsIgnoreCase(singleTemplateName) && (domainId == 3 || domainId == 38 || domainId == 1005))
					{
						final_emailStr = friend_email;
						subject_ReferAFriend = LocaleResources.getString("wwFriendSubject", ctx);
						response = sendMail(final_emailStr, fileLocation, fileName, htmlBody, subject_ReferAFriend);
					}
					else if ("ReferAFriend_EmailTemplate".equalsIgnoreCase(singleTemplateName))
					{
						response = sendMail(final_emailStr, fileLocation, fileName, htmlBody, subject_ReferAFriend);
					}
				}
			}
			else
			{
				SystemUtils.trace("HaysMail", "In the Else Part");
				SystemUtils.trace("HaysMail", "templateName : " + templateName);
				if (templateName.equalsIgnoreCase("webApiRYVacancyConfigurable"))
				{
					m_binder.putLocal("email_content", formContentForRegisterVacancy());
					SystemUtils.trace("HaysMail", "email content for register your vacancy : " + m_binder.getLocal("email_content"));
					htmlBody = createEmailMessage(templateName);
				}
				else if(templateName.equalsIgnoreCase("fullyCustomised"))
				{
					htmlBody = createEmailMessage("webApiRYVacancyConfigurable");
					htmlBody = htmlBody.replace(LocaleResources.getString("wwRegisterYourVacancyText1", ctx), m_binder.getLocal("text1"))
							.replace(LocaleResources.getString("wwRegisterYourVacancyText2", ctx), m_binder.getLocal("text2"));
				}
				response = sendMail(final_emailStr, fileLocation, fileName, htmlBody, subject_ReferAFriend);
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

	public boolean sendMail(String toEmailAddress, String attachedFilePath, String fileName, String htmlBody, String subject_ReferAFriend)
	{
		SystemUtils.trace("HaysMail", "sendMail method start");
		try
		{

			String fromEmailAddress = SharedObjects.getEnvironmentValue("HaysEmailAddress");
			String fromEmailCustomForm = m_binder.getLocal("email");
			String mailhost = SharedObjects.getEnvironmentValue("HaysMailHost");
			String subject = m_binder.getLocal("Subject");
			String subjectline = m_binder.getLocal("SubjectLine");
			String formName = m_binder.getLocal("DataFormName");

			if (subject_ReferAFriend != null && subject_ReferAFriend.length() > 0)
			{
				subject = subject_ReferAFriend;
			}
			File file = null;
			SystemUtils.trace("HaysMail", "attachmentContent : " + attachmentContent);
			if (attachmentContent != null && attachmentContent.length() > 0 && fileName != null && fileName.length() > 0)
			{
				SystemUtils.trace("HaysMail", "Going inside attachment block.........");
				byte[] decoded = Base64.decodeBase64(attachmentContent);
				file = new File(fileName);
				BufferedOutputStream bs = null;
				try
				{
					FileOutputStream fs = new FileOutputStream(file);
					bs = new BufferedOutputStream(fs);
					bs.write(decoded);
					bs.close();
					bs = null;
				}
				catch (Exception e)
				{
					SystemUtils.trace("HaysMail", "File content psrsing exception is" + e.toString());
				}
				if (bs != null)
					try
					{
						bs.close();
					}
					catch (Exception e)
					{
						SystemUtils.trace("HaysMail", "Stream closing exception is" + e.toString());
					}
			}
			else if (attachedFilePath != null && attachedFilePath.length() > 0)
			{
				file = new File(attachedFilePath);
			}

			SystemUtils.trace("HaysMail", "After Attachment block...............");
			if (file != null)
			{
				long length = file.length();
				SystemUtils.trace("HaysMail", "File size is" + length);
				int dotposition = fileName.lastIndexOf(".");
				String ext = fileName.substring(dotposition + 1, fileName.length());
				SystemUtils.trace("HaysMail", "File extension is" + ext);
				long maxsize = 512000; //500 KB

				if (length > maxsize) // checking the file size
				{
					return false;
				}
				if (!("csv".equalsIgnoreCase(ext) || "doc".equalsIgnoreCase(ext) || "docx".equalsIgnoreCase(ext) || "rtf".equalsIgnoreCase(ext) || "txt".equalsIgnoreCase(ext) || "pdf".equalsIgnoreCase(ext)))//checking the file extension
				{
					return false;
				}
			}

			if ("CustomForm1".equalsIgnoreCase(formName) && (subjectline != null && subjectline.length() != 0))
			{
				subject = subject.concat(" ").concat(subjectline);
			}
			if ("CustomForm1".equalsIgnoreCase(formName) || "CustomForm2".equalsIgnoreCase(formName))
			{
				SystemUtils.trace("HaysMail", "inside if fromEmailCustomForm.length()" + fromEmailCustomForm.length());
				if (fromEmailCustomForm.length() > 0 && fromEmailCustomForm != null)
				{
					fromEmailAddress = fromEmailCustomForm;
					SystemUtils.trace("HaysMail", "inside if From EmailAddress" + fromEmailAddress);
				}

			}

			SystemUtils.trace("HaysMail", "formName:" + formName);
			SystemUtils.trace("HaysMail", "To Emailaddress:" + toEmailAddress);
			SystemUtils.trace("HaysMail", "From Emailaddress:" + fromEmailAddress);
			SystemUtils.trace("HaysMail", "Email Subject:" + subject);
			SystemUtils.trace("HaysMail", "SMTP Server:" + mailhost);
			SystemUtils.trace("HaysMail", "attachedFilePath:" + attachedFilePath);
			SystemUtils.trace("HaysMail", "fileName:" + fileName);

			String EmailContent = null;
			if (htmlBody != null && htmlBody.length() > 0)
			{
				SystemUtils.trace("HaysMail", "htmlBody" + htmlBody);
				EmailContent = htmlBody;
			}
			else
			{
				SystemUtils.trace("HaysMail", "htmlBody is null");
				EmailContent = formContent(formName);
			}

			SystemUtils.trace("HaysMail", "EmailContent:" + EmailContent);

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
				SystemUtils.trace("HaysMail", "File size11 is" + l);
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
			e.printStackTrace();
		}
		return false;
	}

	public String formContent(String formName)
	{
		String str = "";
		String customForm = formName;
		setLocale();

		//SystemUtils.trace("HaysMail", "IdcLocale:"+idcl);
		if ("DataForm_1".equalsIgnoreCase(customForm))
		{
			//Refer A Friend Form
			str = LocaleResources.getString("wwReferAFriendEmailText1", ctx) + " " + m_binder.getLocal("name") + ",<br/><br/>" + LocaleResources.getString("wwReferAFriendEmailText2", ctx)
					+ "<br/><br/><br/><b>" + LocaleResources.getString("wwReferAFriendEmailText3", ctx) + "</b><br/><br/>" + LocaleResources.getString("wwReferAFriendEmailText4", ctx) + ": "
					+ m_binder.getLocal("friends_name") + "<br/><br/>" + LocaleResources.getString("wwReferAFriendEmailText5", ctx) + ": " + m_binder.getLocal("friends_location") + "<br/><br/>"
					+ LocaleResources.getString("wwReferAFriendEmailText6", ctx) + ": " + m_binder.getLocal("friends_email") + "<br/><br/>"
					+ LocaleResources.getString("wwReferAFriendEmailText7", ctx) + ": " + m_binder.getLocal("friends_phoneno") + "<br/><br/>"
					+ LocaleResources.getString("wwReferAFriendEmailText21", ctx) + ": " + m_binder.getLocal("friends_profession") + "<br/><br/>"
					+ LocaleResources.getString("wwReferAFriendEmailText8", ctx) + ": " + m_binder.getLocal("friends_jobtitle") + "<br/><br/>"
					+ LocaleResources.getString("wwReferAFriendEmailText9", ctx) + ": " + m_binder.getLocal("friends_town") + "<br/><br/><br/><b>"
					+ LocaleResources.getString("wwReferAFriendEmailText10", ctx) + "</b><br/><br/>" + LocaleResources.getString("wwReferAFriendEmailText11", ctx) + ": " + m_binder.getLocal("name")
					+ "<br/><br/>" + LocaleResources.getString("wwReferAFriendEmailText12", ctx) + ": " + m_binder.getLocal("youraddress") + "<br/><br/>"
					+ LocaleResources.getString("wwReferAFriendEmailText13", ctx) + ": " + m_binder.getLocal("postcode") + "<br/><br/>" + LocaleResources.getString("wwReferAFriendEmailText14", ctx)
					+ ": " + m_binder.getLocal("emailaddress") + "<br/><br/>" + LocaleResources.getString("wwReferAFriendEmailText15", ctx) + ": " + m_binder.getLocal("phoneno") + "<br/><br/>"
					+ LocaleResources.getString("wwReferAFriendEmailText16", ctx) + ": " + m_binder.getLocal("haysconsultant_name") + "<br/><br/>"
					+ LocaleResources.getString("wwReferAFriendEmailText17", ctx) + ": " + m_binder.getLocal("referralscheme_source") + "<br/><br/>"
					+ LocaleResources.getString("wwReferAFriendEmailText18", ctx) + ": " + m_binder.getLocal("friendsdetails_permission") + "<br/><br/>"
					+ LocaleResources.getString("wwReferAFriendEmailText22", ctx) + ": " + m_binder.getLocal("your_gift") + "<br/><br/><br/>"
					+ LocaleResources.getString("wwReferAFriendEmailText19", ctx) + "<br/>" + LocaleResources.getString("wwReferAFriendEmailText20", ctx);
			return str;
		}
		else if ("DataForm_9".equalsIgnoreCase(customForm))
		{
			//Refer A Friend Form for Belgium Hays Career 
			str = LocaleResources.getString("wwReferAFriendEmailText1", ctx) + " " + m_binder.getLocal("name") + ",<br/><br/>" + LocaleResources.getString("wwReferAFriendEmailText2", ctx)
					+ "<br/><br/><br/><b>" + LocaleResources.getString("wwReferAFriendEmailText3", ctx) + "</b><br/><br/>" + LocaleResources.getString("wwReferAFriendEmailText4", ctx) + ": "
					+ m_binder.getLocal("friends_name") + "<br/><br/>" + LocaleResources.getString("wwReferAFriendEmailText5", ctx) + ": " + m_binder.getLocal("friends_location") + "<br/><br/>"
					+ LocaleResources.getString("wwReferAFriendEmailText6", ctx) + ": " + m_binder.getLocal("friends_email") + "<br/><br/>"
					+ LocaleResources.getString("wwReferAFriendEmailText7", ctx) + ": " + m_binder.getLocal("friends_phoneno") + "<br/><br/>"
					+ LocaleResources.getString("wwReferAFriendEmailText21", ctx) + ": " + m_binder.getLocal("friends_profession") + "<br/><br/>"
					+ LocaleResources.getString("wwReferAFriendEmailText8", ctx) + ": " + m_binder.getLocal("friends_jobtitle") + "<br/><br/>"
					+ LocaleResources.getString("wwReferAFriendEmailText9", ctx) + ": " + m_binder.getLocal("friends_town") + "<br/><br/><br/><b>"
					+ LocaleResources.getString("wwReferAFriendEmailText10", ctx) + "</b><br/><br/>" + LocaleResources.getString("wwReferAFriendEmailText11", ctx) + ": " + m_binder.getLocal("name")
					+ "<br/><br/>" + LocaleResources.getString("wwReferAFriendEmailText12", ctx) + ": " + m_binder.getLocal("youraddress") + "<br/><br/>"
					+ LocaleResources.getString("wwReferAFriendEmailText13", ctx) + ": " + m_binder.getLocal("postcode") + "<br/><br/>" + LocaleResources.getString("wwReferAFriendEmailText14", ctx)
					+ ": " + m_binder.getLocal("emailaddress") + "<br/><br/>" + LocaleResources.getString("wwReferAFriendEmailText15", ctx) + ": " + m_binder.getLocal("phoneno") + "<br/><br/>"
					+ LocaleResources.getString("wwReferAFriendEmailText16", ctx) + ": " + m_binder.getLocal("haysconsultant_name") + "<br/><br/>"
					+ LocaleResources.getString("wwReferAFriendEmailText17", ctx) + ": " + m_binder.getLocal("referralscheme_source") + "<br/><br/>"
					+ LocaleResources.getString("wwReferAFriendEmailText18", ctx) + ": " + m_binder.getLocal("friendsdetails_permission") + "<br/><br/>"
					+ LocaleResources.getString("wwReferAFriendEmailText22", ctx) + ": " + m_binder.getLocal("your_gift") + "<br/><br/><br/>"
					+ LocaleResources.getString("wwReferAFriendEmailText19", ctx) + "<br/>" + LocaleResources.getString("wwReferAFriendEmailText20", ctx);
			SystemUtils.trace("HaysMail", "String Value = " + str);
			return str;
		}
		else if ("DataForm_2".equalsIgnoreCase(customForm))
		{
			//Contact US/Feedback Form
			str = LocaleResources.getString("wwFeedbackFormText1", ctx) + ": " + m_binder.getLocal("name") + "<br/><br/>" + LocaleResources.getString("wwFeedbackFormText2", ctx) + ": "
					+ m_binder.getLocal("emailaddress") + "<br/><br/>" + LocaleResources.getString("wwFeedbackFormText3", ctx) + ": " + m_binder.getLocal("telephoneno") + "<br/><br/>"
					+ LocaleResources.getString("wwFeedbackFormText4", ctx) + ": " + m_binder.getLocal("ContactPreference") + "<br/><br/>" + LocaleResources.getString("wwFeedbackFormText5", ctx)
					+ ": " + m_binder.getLocal("DataForm2_job_title") + "<br/><br/>" + LocaleResources.getString("wwFeedbackFormText6", ctx) + ": " + m_binder.getLocal("organization") + "<br/><br/>"
					+ LocaleResources.getString("wwFeedbackFormText7", ctx) + ": " + m_binder.getLocal("consultantname") + "<br/><br/>" + LocaleResources.getString("wwFeedbackFormText8", ctx) + ": "
					+ m_binder.getLocal("hays_office") + "<br/><br/>" + LocaleResources.getString("wwFeedbackFormText9", ctx) + ": " + m_binder.getLocal("CurrentlyRecruiting") + "<br/><br/>"
					+ LocaleResources.getString("wwFeedbackFormText10", ctx) + ": " + m_binder.getLocal("lookingjob") + "<br/><br/>" + LocaleResources.getString("wwFeedbackFormText11", ctx) + ": "
					+ m_binder.getLocal("comments");
			return str;
		}
		else if ("DataForm_3".equalsIgnoreCase(customForm))
		{
			//Speculative CV Submission Form
			str = LocaleResources.getString("wwSpeculativeCVSubmissionFormText1", ctx) + ": " + m_binder.getLocal("name") + "<br/><br/>"
					+ LocaleResources.getString("wwSpeculativeCVSubmissionFormText2", ctx) + ": " + m_binder.getLocal("address") + "<br/><br/>"
					+ LocaleResources.getString("wwSpeculativeCVSubmissionFormText3", ctx) + ": " + m_binder.getLocal("postcode") + "<br/><br/>"
					+ LocaleResources.getString("wwSpeculativeCVSubmissionFormText4", ctx) + ": " + m_binder.getLocal("emailaddress") + "<br/><br/>"
					+ LocaleResources.getString("wwSpeculativeCVSubmissionFormText5", ctx) + ": " + m_binder.getLocal("telephoneno") + "<br/><br/>"
					+ LocaleResources.getString("wwSpeculativeCVSubmissionFormText6", ctx) + ": " + m_binder.getLocal("current_jobtitle") + "<br/><br/>"
					+ LocaleResources.getString("wwSpeculativeCVSubmissionFormText7", ctx) + ": " + m_binder.getLocal("salary_expectation") + "<br/><br/>"
					+ LocaleResources.getString("wwSpeculativeCVSubmissionFormText8", ctx) + ": " + m_binder.getLocal("subject_specialism") + "<br/><br/>"
					+ LocaleResources.getString("wwSpeculativeCVSubmissionFormText9", ctx) + ": " + m_binder.getLocal("IntrestedRoles") + "<br/><br/>"
					+ LocaleResources.getString("wwSpeculativeCVSubmissionFormText10", ctx) + ": " + m_binder.getLocal("AttachedDocument") + "<br/><br/>"
					+ LocaleResources.getString("wwSpeculativeCVSubmissionFormText11", ctx) + ": " + m_binder.getLocal("SecurityLevels") + "<br/><br/>"
					+ LocaleResources.getString("wwSpeculativeCVSubmissionFormText12", ctx) + ": " + m_binder.getLocal("Intrestedin") + "<br/><br/>"
					+ LocaleResources.getString("wwSpeculativeCVSubmissionFormText13", ctx) + ": " + m_binder.getLocal("additional_comments");
			return str;
		}
		else if ("DataForm_4".equalsIgnoreCase(customForm))
		{
			//SEN Questionaire Form
			str = LocaleResources.getString("wwSENQuestionaireFormText1", ctx) + ": " + m_binder.getLocal("name") + "<br/><br/>" + LocaleResources.getString("wwSENQuestionaireFormText2", ctx) + ": "
					+ m_binder.getLocal("emailaddress") + "<br/><br/>" + LocaleResources.getString("wwSENQuestionaireFormText3", ctx) + ": " + m_binder.getLocal("telephoneno") + "<br/><br/>"
					+ LocaleResources.getString("wwSENQuestionaireFormText4", ctx) + ": " + m_binder.getLocal("currentsituation") + "<br/><br/>"
					+ LocaleResources.getString("wwSENQuestionaireFormText5", ctx) + ": " + m_binder.getLocal("startdate") + "<br/><br/>"
					+ LocaleResources.getString("wwSENQuestionaireFormText6", ctx) + ": " + m_binder.getLocal("availableforwork") + "<br/><br/>"
					+ LocaleResources.getString("wwSENQuestionaireFormText7", ctx) + ": " + m_binder.getLocal("travelareas") + "<br/><br/>"
					+ LocaleResources.getString("wwSENQuestionaireFormText8", ctx) + ": " + m_binder.getLocal("shoolsetting") + "<br/><br/>"
					+ LocaleResources.getString("wwSENQuestionaireFormText9", ctx) + ": " + m_binder.getLocal("senExperaince") + "<br/><br/>"
					+ LocaleResources.getString("wwSENQuestionaireFormText10", ctx) + ": " + m_binder.getLocal("additional_experiance") + "<br/><br/>"
					+ LocaleResources.getString("wwSENQuestionaireFormText11", ctx) + ": " + m_binder.getLocal("sen_qualification") + "<br/><br/>"
					+ LocaleResources.getString("wwSENQuestionaireFormText12", ctx) + ": " + m_binder.getLocal("age_range");
			return str;
		}
		else if ("DataForm_5".equalsIgnoreCase(customForm))
		{
			//P45 Request Form
			str = LocaleResources.getString("wwP45RequestFormText1", ctx) + ": " + m_binder.getLocal("name") + "<br/><br/>" + LocaleResources.getString("wwP45RequestFormText2", ctx) + ": "
					+ m_binder.getLocal("reference_no") + "<br/><br/>" + LocaleResources.getString("wwP45RequestFormText3", ctx) + ": " + m_binder.getLocal("dob") + "<br/><br/>"
					+ LocaleResources.getString("wwP45RequestFormText4", ctx) + ": " + m_binder.getLocal("ni_number") + "<br/><br/>" + LocaleResources.getString("wwP45RequestFormText5", ctx) + ": "
					+ m_binder.getLocal("address_line1") + "<br/><br/>" + LocaleResources.getString("wwP45RequestFormText6", ctx) + ": " + m_binder.getLocal("address_line2") + "<br/><br/>"
					+ LocaleResources.getString("wwP45RequestFormText7", ctx) + ": " + m_binder.getLocal("address_line3") + "<br/><br/>" + LocaleResources.getString("wwP45RequestFormText8", ctx)
					+ ": " + m_binder.getLocal("postcode") + "<br/><br/>" + LocaleResources.getString("wwP45RequestFormText9", ctx) + ": " + m_binder.getLocal("different_addr_submission")
					+ "<br/><br/>" + LocaleResources.getString("wwP45RequestFormText10", ctx) + ": " + m_binder.getLocal("home_emailaddress") + "<br/><br/>"
					+ LocaleResources.getString("wwP45RequestFormText11", ctx) + ": " + m_binder.getLocal("last_workingDate") + "<br/><br/>" + LocaleResources.getString("wwP45RequestFormText12", ctx)
					+ ": " + m_binder.getLocal("last_officename") + "<br/><br/>" + LocaleResources.getString("wwP45RequestFormText13", ctx) + ": " + m_binder.getLocal("hays_employee");
			return str;
		}
		else if ("DataForm_6".equalsIgnoreCase(customForm))
		{
			//Consultation Request Form
			str = LocaleResources.getString("wwConsultationRequestFormText1", ctx) + ": " + m_binder.getLocal("name") + "<br/><br/>" + LocaleResources.getString("wwConsultationRequestFormText2", ctx)
					+ ": " + m_binder.getLocal("contactno") + "<br/><br/>" + LocaleResources.getString("wwConsultationRequestFormText3", ctx) + ": " + m_binder.getLocal("emailaddress") + "<br/><br/>"
					+ "<b>" + LocaleResources.getString("wwConsultationRequestFormText4", ctx) + "</b>" + "<br/><br/>" + LocaleResources.getString("wwConsultationRequestFormText5", ctx) + ": "
					+ m_binder.getLocal("preferred_date1") + "<br/><br/>" + LocaleResources.getString("wwConsultationRequestFormText6", ctx) + ": " + m_binder.getLocal("preferred_time1")
					+ "<br/><br/>" + "<b>" + LocaleResources.getString("wwConsultationRequestFormText7", ctx) + "</b>" + "<br/><br/>"
					+ LocaleResources.getString("wwConsultationRequestFormText8", ctx) + ": " + m_binder.getLocal("preferred_date2") + "<br/><br/>"
					+ LocaleResources.getString("wwConsultationRequestFormText9", ctx) + ": " + m_binder.getLocal("preferred_time2") + "<br/><br/>" + "<b>"
					+ LocaleResources.getString("wwConsultationRequestFormText10", ctx) + "</b>" + "<br/><br/>" + LocaleResources.getString("wwConsultationRequestFormText11", ctx) + ": "
					+ m_binder.getLocal("preferred_date3") + "<br/><br/>" + LocaleResources.getString("wwConsultationRequestFormText12", ctx) + ": " + m_binder.getLocal("preferred_time3");
			return str;
		}
		else if ("CustomForm1".equalsIgnoreCase(customForm))
		{
			//Consultation Request Form
			str = LocaleResources.getString("wwCustomFormFirstName", ctx) + ": " + m_binder.getLocal("first_name") + ",<br/><br/>" + LocaleResources.getString("wwCustomFormLastName", ctx) + ": "
					+ m_binder.getLocal("last_name") + "<br/><br/>" + LocaleResources.getString("wwCustomFormDOB", ctx) + ": " + m_binder.getLocal("dob_day") + "/" + m_binder.getLocal("dob_month")
					+ "<br/><br/>" + LocaleResources.getString("wwCustomFormEmail", ctx) + ": " + m_binder.getLocal("email") + ",<br/><br/>" + LocaleResources.getString("wwCustomFormComments", ctx)
					+ ": " + m_binder.getLocal("message");
			return str;
		}
		else if ("CustomForm2".equalsIgnoreCase(customForm))
		{
			//Consultation Request Form
			str = LocaleResources.getString("wwRegisterYourVacancyContactEmail", ctx) + ": " + m_binder.getLocal("email") + ",<br/><br/>"
					+ LocaleResources.getString("wwRegisterYourVacancyContactPhone", ctx) + ": " + m_binder.getLocal("telephone") + "<br/><br/>"
					+ LocaleResources.getString("wwRegisterYourVacancyContactName", ctx) + ": " + m_binder.getLocal("your_name") + "<br/><br/>"
					+ LocaleResources.getString("wwRegisterYourVacancyJobTitle", ctx) + ": " + m_binder.getLocal("your_job_title") + ",<br/><br/>"
					+ LocaleResources.getString("wwRegisterYourVacancyCompanyName", ctx) + ": " + m_binder.getLocal("your_organisation") + ",<br/><br/>"
					+ LocaleResources.getString("wwRegisterYourVacancyLocationofVacancy", ctx) + ": " + m_binder.getLocal("vacancy_based") + ",<br/><br/>"
					+ LocaleResources.getString("wwRegisterYourVacancyPosition", ctx) + ": " + m_binder.getLocal("what_position") + ",<br/><br/>"
					+ LocaleResources.getString("wwRegisterYourVacancyEmploymentType", ctx) + ": " + m_binder.getLocal("employment_type") + ",<br/><br/>"
					+ LocaleResources.getString("wwRegisterYourVacancySalaryRange", ctx) + ": " + m_binder.getLocal("salary_range") + ",<br/><br/>"
					+ LocaleResources.getString("wwRegisterYourVacancyState/Country", ctx) + ": " + m_binder.getLocal("state_county") + ",<br/><br/>"
					+ LocaleResources.getString("wwRegisterYourVacancyFurtherInformation", ctx) + ": " + m_binder.getLocal("further_info");
			return str;
		}
		else
		{
			//Register A Problem Form
			str = LocaleResources.getString("wwRegisterAProblemFormText1", ctx) + ": " + m_binder.getLocal("name") + "<br/><br/>" + LocaleResources.getString("wwRegisterAProblemFormText2", ctx)
					+ ": " + m_binder.getLocal("company") + "<br/><br/>" + LocaleResources.getString("wwRegisterAProblemFormText3", ctx) + ": " + m_binder.getLocal("telephoneno") + "<br/><br/>"
					+ LocaleResources.getString("wwRegisterAProblemFormText4", ctx) + ": " + m_binder.getLocal("dateofbirth") + "<br/><br/>"
					+ LocaleResources.getString("wwRegisterAProblemFormText5", ctx) + ": " + m_binder.getLocal("emailaddress") + "<br/><br/>"
					+ LocaleResources.getString("wwRegisterAProblemFormText6", ctx) + ": " + m_binder.getLocal("address") + "<br/><br/>"
					+ LocaleResources.getString("wwRegisterAProblemFormText7", ctx) + ": " + m_binder.getLocal("hays_specialism") + "<br/><br/>"
					+ LocaleResources.getString("wwRegisterAProblemFormText8", ctx) + ": " + m_binder.getLocal("officelocation") + "<br/><br/>"
					+ LocaleResources.getString("wwRegisterAProblemFormText9", ctx) + ": " + m_binder.getLocal("concerned_person") + "<br/><br/>"
					+ LocaleResources.getString("wwRegisterAProblemFormText10", ctx) + ": " + m_binder.getLocal("problem") + "<br/><br/>"
					+ LocaleResources.getString("wwRegisterAProblemFormText11", ctx) + ": " + m_binder.getLocal("other_problem") + "<br/><br/>"
					+ LocaleResources.getString("wwRegisterAProblemFormText12", ctx) + ": " + m_binder.getLocal("problem_details") + "<br/><br/>"
					+ LocaleResources.getString("wwRegisterAProblemFormText13", ctx) + ": " + m_binder.getLocal("AttachedDocument");
			return str;
		}
	}

	public String formContentForRegisterVacancy() throws ServiceException
	{
		StringBuilder sb = new StringBuilder();
		setLocale();
		String locale = m_binder.getLocal("locale");
		if (locale == null || locale.length() <= 0)
		{
			locale = "en-GB";
		}
		SystemUtils.trace("HaysMail", "formContentForRegisterVacancy  locale: " + locale);
		
		//24664 Register Your Vacancy for mobile site - UCM API Change
		String required_fields_config = null;
		String isProjectS = getData("isExcluded");
		SystemUtils.trace("HaysMail", "formContentForRegisterVacancy  isProjectS: " + isProjectS);
		if ("Y".equalsIgnoreCase(isProjectS))
		{
			required_fields_config = getEnvironmentValue("RegisterVacancyFields_ProjectS");
		}
		else
		{
			required_fields_config = getEnvironmentValue("RegisterVacancyFields_" + locale);
		}
		//24664 Register Your Vacancy for mobile site - UCM API Change Ends.
		
		SystemUtils.trace("HaysMail", "formContentForRegisterVacancy  required_fields_config: " + required_fields_config);
		HashMap<String, String> wwStringMap = new HashMap<String, String>();
		wwStringMap.put("fromEmail", "wwRegisterYourVacancyContactEmail");
		wwStringMap.put("telephone", "wwRegisterYourVacancyContactPhone");
		wwStringMap.put("name", "wwRegisterYourVacancyContactName");
		wwStringMap.put("organisation", "wwRegisterYourVacancyCompanyName");
		wwStringMap.put("location", "wwRegisterYourVacancyLocationofVacancy");
		wwStringMap.put("position", "wwRegisterYourVacancyPosition");
		wwStringMap.put("jobType", "wwRegisterYourVacancyEmploymentType");
		wwStringMap.put("salaryRange", "wwRegisterYourVacancySalaryRange");
		wwStringMap.put("stateCountry", "wwRegisterYourVacancyState/Country");
		wwStringMap.put("addInfo", "wwRegisterYourVacancyFurtherInformation");
		wwStringMap.put("function", "wwRegisterYourVacancyFunction");
		wwStringMap.put("prefmethod", "wwRegisterYourVacancyPrefMethod");
		wwStringMap.put("preftime", "wwRegisterYourVacancyPrefTime");
		wwStringMap.put("howhere", "wwRegisterYourVacancyHDYHere");
		wwStringMap.put("startdate", "wwRegisterYourVacancyStartDate");
		wwStringMap.put("enddate", "wwRegisterYourVacancyEndDate");
		wwStringMap.put("jobdesc", "wwRegisterYourVacancyJobDesc");
		wwStringMap.put("hresponseoffice", "wwRegisterYourVacancyHaysResponseOffice");
		wwStringMap.put("arerecruiting", "wwRegisterYourVacancyAYCRecruiting");
		wwStringMap.put("arelookingforwork", "wwRegisterYourVacancyAYCLFWork");
		wwStringMap.put("division", "wwRegisterYourVacancyDivision");
		wwStringMap.put("specialisation", "wwRegisterYourVacancySpecialisation");
		wwStringMap.put("finoffice", "wwRegisterYourVacancyFinoffice");
		wwStringMap.put("cons", "wwRegisterYourVacancyCons");

		if (required_fields_config == null || required_fields_config.length() <= 0)
		{
			required_fields_config = getEnvironmentValue("RegisterVacancyFields");
		}
		SystemUtils.trace("HaysMail", "formContentForRegisterVacancy  required_fields_config: " + required_fields_config);
		String[] required_fields = required_fields_config.split(",");
		String rowToAppend = null;
		try
		{
			for (String field_name : required_fields)
			{
				rowToAppend = LocaleResources.getString(wwStringMap.get(field_name), ctx) + ": " + getData(field_name) + "<br/><br/>";
				sb.append(rowToAppend);
				SystemUtils.trace("HaysMail", "Adding -- > " + rowToAppend);
			}
		}
		catch (Exception e)
		{
			SystemUtils.trace("HaysMail", "Error: " + e.toString());
		}
		return sb.toString();
	}

	private String createEmailMessage(String emailTemplate)
	{
		SystemUtils.trace("HaysMail", "Email Template,createEmailMessage:" + emailTemplate);
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
			SystemUtils.trace("HaysMail", "error : " + ignore.toString());
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
			//SystemUtils.trace("HaysMail", "looping for field : " + def.m_name);
		}

		// Prepare binder for mail merge:
		DataBinderLocalizer localizer = new DataBinderLocalizer(m_binder, this.m_service);
		localizer.localizeBinder(DataBinderLocalizer.ALL);
		Properties oldLocalData = m_binder.getLocalData();
		Properties newLocalData = (Properties) oldLocalData.clone();

		m_binder.setLocalData(newLocalData);
		m_binder.putLocal("isAbsoluteWeb", "1");
		m_binder.putLocal("isAbsoluteCgi", "1");

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
			SystemUtils.trace("HaysMail", "error : " + e.toString());
		}
		finally
		{
			m_binder.setLocalData(oldLocalData);
		}
		//SystemUtils.trace("HaysMail","Final output html "+msg);
		SystemUtils.trace("HaysMail", "Email body created successfully.");

		return msg;
	}

	public void setLocale()
	{
		String locale = m_binder.getLocal("locale");
		if (locale == null || locale.length() <= 0)
		{
			locale = "en-GB";
		}
		//SystemUtils.trace("HaysMail", "SiteLocale:"+locale);		
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
