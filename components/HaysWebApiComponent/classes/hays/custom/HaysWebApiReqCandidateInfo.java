package hays.custom;

import static hays.com.commonutils.HaysWebApiUtils.HandleExceptions;
import static intradoc.common.LocaleResources.getString;
import hays.emailServiceClient.ArrayOfEmailKeyValueVO;
import hays.emailServiceClient.EmailContentHeader;
import hays.emailServiceClient.EmailData;
import hays.emailServiceClient.EmailKeyValueVO;
import hays.emailServiceClient.EmailMessage;
import hays.emailServiceClient.EmailPublisherService;
import hays.emailServiceClient.IEmailPublisherService;
import hays.emailServiceClient.ObjectFactory;
import intradoc.common.ExecutionContext;
import intradoc.common.ExecutionContextAdaptor;
import intradoc.common.IdcLocale;
import intradoc.common.LocaleResources;
import intradoc.common.LocaleUtils;
import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.data.DataException;
import intradoc.data.ResultSet;
import intradoc.server.ServiceHandler;
import intradoc.shared.SharedObjects;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.ws.BindingProvider;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class HaysWebApiReqCandidateInfo extends ServiceHandler
{
	private final static String TRACE_NAME = "HAYS_API_REQ_CAN_INFO";
	private final static String SOURCE_NAME = "WEBCENTER";
	private final static String TARGET_SYSTEM = "EMAIL SERVICE";
	private final static String BLANK = "";
	private final static String COLON_SEPERATOR = " : ";
	private final static String X = "x";
	private final static String D = "d";
	private static ObjectFactory OBJECT_FACTORY = new ObjectFactory();
	private HashMap<String, String> gParamsMap = new HashMap<String, String>();

	ExecutionContext ctx = new ExecutionContextAdaptor();

	public void requestCandidateInformation() throws DataException, ServiceException
	{

		// Get the data from user
		getData("firstName").getData("lastName").getData("empEmail").getData("emplPhone").getData("emplOrg").getData("emplJobTitle")
				.getData("notes").getData("candDescLink").getData("locale").getData("candidateId");
		String isoCountryCode = "GB";
		if (gParamsMap.get("locale") != "en-GB")
		{
			ResultSet localeResultSet = this.m_binder.getResultSet("LOCALE_DETAILS");
			isoCountryCode = localeResultSet.getStringValueByName("ISOCOUNTRYCODE").trim();
		}
		setLocale();
		// Validate user provided data
		validateParameters();
		// Get data from localisation files
		gParamsMap.put("HAYS_DOMAIN", getString("wwHAYS_DOMAIN", ctx));
		gParamsMap.put("HAYS_LOGO", getString("wwHays_Logo_Url", ctx));
		gParamsMap.put("CandInfoEmpEmailSubject", getString("wwCandInfoEmpEmailSubject", ctx));
		gParamsMap.put("CandInfoConsEmailSubject", getString("wwCandInfoConsEmailSubject", ctx));

		gParamsMap.put("country", isoCountryCode);
		gParamsMap.put("webcenterlocale", gParamsMap.get("locale").split("-")[0] + "_" + isoCountryCode);
		// EmailPublisherService uses country to decide upon WSDL URL to call.
		SharedObjects.putEnvironmentValue("country", gParamsMap.get("webcenterlocale"));

		if ("HU".equalsIgnoreCase(gParamsMap.get("country")))
		{
			gParamsMap.put("employerFullName", gParamsMap.get("lastName") + " " + gParamsMap.get("firstName"));// Hungary
		}
		else
		{
			gParamsMap.put("employerFullName", gParamsMap.get("firstName") + " " + gParamsMap.get("lastName"));
		}

		// Fetch doc meta data
		m_binder.putLocal("dDocName", gParamsMap.get("candidateId"));
		try
		{
			m_service.executeServiceEx("DOC_INFO_BY_NAME", true);
		}
		catch (Exception e)
		{
			HandleExceptions(m_binder, "UC011", "wwFileNotFound");
		}
		ResultSet lDocInfoRS = m_binder.getResultSet("DOC_INFO");
		if (lDocInfoRS != null && lDocInfoRS.first())
		{
			gParamsMap.put("refID", lDocInfoRS.getStringValueByName("xRecordId"));
			gParamsMap.put("jobTitle", lDocInfoRS.getStringValueByName("dDocTitle"));
			gParamsMap.put("candidateAvailFrom", lDocInfoRS.getStringValueByName("xAvailableFrom"));
			gParamsMap.put("xJobOwner", lDocInfoRS.getStringValueByName("xJobOwner"));
		}
		extractJobOwnerVaues(gParamsMap.get("xJobOwner"));

		String fromAddress = getString("wwCandidateInfo_Request_From_Email_Address", ctx);
		fromAddress = fromAddress.replace("(Err)", "@");
		gParamsMap.put("fromAddress", fromAddress);
		gParamsMap.put("fromName", getString("wwCandidateInfo_Request_From_Name", ctx));
		for (String lKey : gParamsMap.keySet())
		{
			SystemUtils.trace(TRACE_NAME, lKey + COLON_SEPERATOR + gParamsMap.get(lKey));
		}
		m_binder.clearResultSets();

//		System.setProperty("https.proxyUser", "");
//		System.setProperty("https.proxyPassword", "");
//		System.setProperty("https.proxyHost", "");
//		System.setProperty("https.proxyPort", "");
		
		System.setProperty("http.proxyUser", "");
		System.setProperty("http.proxyPassword", "");
		System.setProperty("http.proxyHost", "");
		System.setProperty("http.proxyPort", "");

		boolean lEmployerMailStatus = sendEmailToEmployer();
		boolean lConsultantMailStatus = false;
		if (lEmployerMailStatus)
		{
			lConsultantMailStatus = sendEmailToConsultant();
		}
		if (!lEmployerMailStatus && !lConsultantMailStatus)
		{
			HandleExceptions(m_binder, "UC008", "wwEmailSendingFailed");
		}
		else if (lEmployerMailStatus && !lConsultantMailStatus)
		{
			HandleExceptions(m_binder, "UC008", "wwOneOfEmailSendingFailed");
		}
		else
		{
			m_binder.putLocal("StatusMessage", LocaleUtils.encodeMessage("wwWebApiOKMsg", null));
			m_binder.putLocal("StatusCode", "UC000");
		}

		ArrayList<String> lListOfKeys = new ArrayList<String>();
		String lStringKey = null;
		for (Object lKey : m_binder.getLocalData().keySet())
		{
			lStringKey = lKey.toString();
			if (lStringKey.startsWith(X) || lStringKey.startsWith(D))
			{
				lListOfKeys.add(lKey.toString());
			}
		}
		for (String lkey : lListOfKeys)
			m_binder.removeLocal(lkey);
	}

	private boolean sendEmailToEmployer() throws ServiceException
	{
		SystemUtils.trace(TRACE_NAME, "inside sendEmailToEmployer().....");
		String lServiceType = "WEB_CAND_REQ_EMAIL";
		String lUniqueMsgId = "ID_00007";

		EmailContentHeader lEmailContentHeader = new EmailContentHeader();
		lEmailContentHeader = createEmailContentHeader(lServiceType, lUniqueMsgId);

		EmailData lEmailData = intiatePayload();
		lEmailData.setToAddress(OBJECT_FACTORY.createEmailDataToAddress(gParamsMap.get("empEmail")));
		lEmailData.setToName(OBJECT_FACTORY.createEmailDataToName(gParamsMap.get("employerFullName")));
		lEmailData.setSubject(OBJECT_FACTORY.createEmailDataSubject(gParamsMap.get("CandInfoEmpEmailSubject")));

		ArrayOfEmailKeyValueVO lArrayOfEmailKeyValueVO = OBJECT_FACTORY.createArrayOfEmailKeyValueVO();
		List<EmailKeyValueVO> lEmailKeyValueVOList = lArrayOfEmailKeyValueVO.getEmailKeyValueVO();
		lEmailKeyValueVOList.add(createMailVOInstance("EMPL_FULL_NAME", gParamsMap.get("employerFullName")));
		lEmailKeyValueVOList.add(createMailVOInstance("HAYS_DOMAIN", gParamsMap.get("HAYS_DOMAIN")));
		lEmailKeyValueVOList.add(createMailVOInstance("CANDTITLE", gParamsMap.get("jobTitle")));
		lEmailKeyValueVOList.add(createMailVOInstance("REFERENCE", gParamsMap.get("refID")));
		lEmailKeyValueVOList.add(createMailVOInstance("CONSULTANT_NAME", gParamsMap.get("haysConsultant")));
		lEmailKeyValueVOList.add(createMailVOInstance("CONSULTANT_OFFICE", gParamsMap.get("consOffice")));
		lEmailKeyValueVOList.add(createMailVOInstance("CONSULTANT_EMAIL", gParamsMap.get("consEmail")));
		lEmailKeyValueVOList.add(createMailVOInstance("CONSULTANT_PHONE_NO", gParamsMap.get("consPhone")));
		lEmailKeyValueVOList.add(createMailVOInstance("HAYS_LOGO", gParamsMap.get("HAYS_LOGO")));
		lEmailKeyValueVOList.add(createMailVOInstance("AVAILABLE_FROM", gParamsMap.get("candidateAvailFrom")));
		lEmailKeyValueVOList.add(createMailVOInstance("CANDIDATE_DESC_LINK", gParamsMap.get("candDescLink")));

		lEmailData.setEmailKeyValueList(OBJECT_FACTORY.createEmailDataEmailKeyValueList(lArrayOfEmailKeyValueVO));

		boolean isEmailSent = false;
		try
		{
			isEmailSent = runEmailProcess(lEmailContentHeader, lEmailData);
		}
		catch (ServiceException e)
		{
			HandleExceptions(m_binder, "UC008", "wwEmailSendingFailed");
		}
		return isEmailSent;
	}

	private boolean sendEmailToConsultant() throws ServiceException
	{
		SystemUtils.trace(TRACE_NAME, "inside sendEmailToConsultant().....");
		String lServiceType = "WEB_CONSULTANT_REQ_UK";
		String lUniqueMsgId = "ID_00008";

		EmailContentHeader lEmailContentHeader = new EmailContentHeader();
		lEmailContentHeader = createEmailContentHeader(lServiceType, lUniqueMsgId);

		EmailData lEmailData = intiatePayload();

		lEmailData.setToAddress(OBJECT_FACTORY.createEmailDataToAddress(gParamsMap.get("consEmail")));
		lEmailData.setToName(OBJECT_FACTORY.createEmailDataToName(gParamsMap.get("consName")));
		lEmailData.setSubject(OBJECT_FACTORY.createEmailDataSubject(gParamsMap.get("CandInfoConsEmailSubject")));

		ArrayOfEmailKeyValueVO lArrayOfEmailKeyValueVO = OBJECT_FACTORY.createArrayOfEmailKeyValueVO();
		List<EmailKeyValueVO> lEmailKeyValueVOList = lArrayOfEmailKeyValueVO.getEmailKeyValueVO();
		lEmailKeyValueVOList.add(createMailVOInstance("EMPL_FULL_NAME", gParamsMap.get("employerFullName")));
		lEmailKeyValueVOList.add(createMailVOInstance("HAYS_DOMAIN", gParamsMap.get("HAYS_DOMAIN")));
		lEmailKeyValueVOList.add(createMailVOInstance("CANDTITLE", gParamsMap.get("jobTitle")));
		lEmailKeyValueVOList.add(createMailVOInstance("REFERENCE", gParamsMap.get("refID")));
		lEmailKeyValueVOList.add(createMailVOInstance("EMPL_TITLE", gParamsMap.get("emplJobTitle")));
		lEmailKeyValueVOList.add(createMailVOInstance("EMPL_ORGANISATION", gParamsMap.get("emplOrg")));
		lEmailKeyValueVOList.add(createMailVOInstance("EMPL_EMAIL", gParamsMap.get("empEmail")));
		lEmailKeyValueVOList.add(createMailVOInstance("EMPL_PHONE_NO", gParamsMap.get("emplPhone")));
		lEmailKeyValueVOList.add(createMailVOInstance("HAYS_LOGO", gParamsMap.get("HAYS_LOGO")));
		lEmailKeyValueVOList.add(createMailVOInstance("AVAILABLE_FROM", gParamsMap.get("candidateAvailFrom")));
		lEmailKeyValueVOList.add(createMailVOInstance("ADDITIONAL_NOTE", gParamsMap.get("notes")));

		lEmailData.setEmailKeyValueList(OBJECT_FACTORY.createEmailDataEmailKeyValueList(lArrayOfEmailKeyValueVO));

		boolean isEmailSent = false;
		try
		{
			isEmailSent = runEmailProcess(lEmailContentHeader, lEmailData);
		}
		catch (ServiceException e)
		{
			HandleExceptions(m_binder, "UC008", "wwEmailSendingFailed");
		}
		return isEmailSent;
	}

	private boolean runEmailProcess(EmailContentHeader header, EmailData payLoad) throws ServiceException
	{
		boolean isEmailSent = false;
		try
		{
			String countryCode = gParamsMap.get("country");
			SystemUtils.trace(TRACE_NAME, "inside runEmailProcess().....country:" + countryCode);
			String endPoint = SharedObjects.getEnvironmentValue("WSDL_ENDPOINT_" + countryCode);
			// added for multiple WSDLs end
			SystemUtils.trace(TRACE_NAME, "WSDL endpoint :" + endPoint);
			EmailMessage emailMessage = new EmailMessage();
			emailMessage.setHeader(OBJECT_FACTORY.createEmailMessageHeader(header));
			emailMessage.setPayLoad(OBJECT_FACTORY.createEmailMessagePayLoad(payLoad));
			emailMessage.setCountry(OBJECT_FACTORY.createEmailMessageCountry(gParamsMap.get("webcenterlocale")));

			EmailPublisherService client = new EmailPublisherService(new URL(endPoint), new QName("http://tempuri.org/",
					"EmailPublisherService"));
			IEmailPublisherService iEmailPublisherService = client.getBasicHttpBindingIEmailPublisherService();
			((BindingProvider) iEmailPublisherService).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endPoint);
			iEmailPublisherService.processEmail(emailMessage);
			isEmailSent = true;
		}
		catch (Exception e)
		{
			isEmailSent = false;
			SystemUtils.dumpException(TRACE_NAME, e);
			HandleExceptions(m_binder, "UC008", "wwEmailSendingFailed");
		}
		return isEmailSent;
	}

	private EmailContentHeader createEmailContentHeader(String pServiceType, String pUniqueMsgId)
	{
		SystemUtils.trace(TRACE_NAME, "inside createEmailContentHeader...");
		EmailContentHeader header = new EmailContentHeader();
		XMLGregorianCalendar xcal = null;
		Date date = new Date();
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTimeInMillis(date.getTime());
		try
		{
			xcal = DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);
		}
		catch (DatatypeConfigurationException e1)
		{
			SystemUtils.trace(TRACE_NAME, e1.toString());
		}

		header.setMailDate(xcal);
		header.setServiceType(OBJECT_FACTORY.createEmailContentHeaderServiceType(pServiceType));
		header.setSourceSystem(OBJECT_FACTORY.createEmailContentHeaderSourceSystem(SOURCE_NAME));
		header.setTargetSystem(OBJECT_FACTORY.createEmailContentHeaderTargetSystem(TARGET_SYSTEM));
		header.setUniqueMsgId(OBJECT_FACTORY.createEmailContentHeaderUniqueMsgId(pUniqueMsgId));
		return header;
	}

	private EmailData intiatePayload()
	{
		EmailData payload = new EmailData();
		payload.setFromName(OBJECT_FACTORY.createEmailDataFromName(gParamsMap.get("fromName")));
		payload.setFromAddress(OBJECT_FACTORY.createEmailDataFromAddress(gParamsMap.get("fromAddress")));
		payload.setCCName(OBJECT_FACTORY.createEmailDataCCName(BLANK));
		payload.setCCAddress(OBJECT_FACTORY.createEmailDataCCAddress(BLANK));
		payload.setBCCName(OBJECT_FACTORY.createEmailDataBCCName(BLANK));
		payload.setBCCAddress(OBJECT_FACTORY.createEmailDataBCCAddress(BLANK));
		return payload;
	}

	private EmailKeyValueVO createMailVOInstance(String pParamName, String pParamValue)
	{
		EmailKeyValueVO lEmailKeyValueVO = new EmailKeyValueVO();
		lEmailKeyValueVO.setKey(OBJECT_FACTORY.createEmailKeyValueVOKey(pParamName));
		lEmailKeyValueVO.setValue(OBJECT_FACTORY.createEmailKeyValueVOValue(pParamValue));
		return lEmailKeyValueVO;
	}

	private void extractJobOwnerVaues(String pJobOwnerXMLString) throws ServiceException
	{
		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		domFactory.setNamespaceAware(true);
		XPath xpath = null;
		try
		{
			DocumentBuilder builder = domFactory.newDocumentBuilder();
			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(pJobOwnerXMLString));
			Document lDocument = builder.parse(is);
			xpath = XPathFactory.newInstance().newXPath();
			XPathExpression expr = xpath.compile("//JobOwner/name/text()");
			gParamsMap.put("haysConsultant", expr.evaluate(lDocument, XPathConstants.STRING).toString());
			gParamsMap.put("consName", gParamsMap.get("haysConsultant"));
			expr = xpath.compile("//JobOwner/address/text()");
			gParamsMap.put("consOffice", expr.evaluate(lDocument, XPathConstants.STRING).toString());
			expr = xpath.compile("//JobOwner/tel/text()");
			gParamsMap.put("consPhone", expr.evaluate(lDocument, XPathConstants.STRING).toString());
			expr = xpath.compile("//JobOwner/email/text()");
			gParamsMap.put("consEmail", expr.evaluate(lDocument, XPathConstants.STRING).toString());
		}
		catch (XPathExpressionException e)
		{
			HandleExceptions(m_binder, "UC011", "wwFileNotFound");
		}
		catch (ParserConfigurationException e)
		{
			HandleExceptions(m_binder, "UC013", "wwFileNotParse");
		}
		catch (SAXException e)
		{
			HandleExceptions(m_binder, "UC012", "wwFileNotFormedProperly");
		}
		catch (IOException e)
		{
			HandleExceptions(m_binder, "UC011", "wwFileNotFound");
		}
	}

	private HaysWebApiReqCandidateInfo getData(String pParamName)
	{
		return getData(pParamName, pParamName);
	}

	private HaysWebApiReqCandidateInfo getData(String pParamKey, String pParamValue)
	{
		try
		{
			gParamsMap.put(pParamKey, m_binder.get(pParamValue));
		}
		catch (Exception e)
		{

		}
		return this;
	}

	private void validateParameters() throws ServiceException
	{
		String[] mandatoryParameters = { "firstName", "lastName", "empEmail", "emplPhone", "candidateId" };
		for (String key : mandatoryParameters)
		{
			if (gParamsMap.get(key) == null || gParamsMap.get(key).isEmpty())
			{
				HandleExceptions(m_binder, "UC0015", LocaleUtils.encodeMessage("wwMandatoryParams", key));
			}
		}
		try
		{
			String emailRegex = "^[a-z][a-z|0-9|]*([_][a-z|0-9]+)*([.][a-z|0-9]+([_]"
					+ "[a-z|0-9]+)*)?@[a-z][a-z|0-9|]*\\.([a-z][a-z|0-9]*(\\.[a-z][a-z|0-9]*)?)$";
			String email = gParamsMap.get("empEmail");
			if (email != null && !email.matches(emailRegex))
			{
				HandleExceptions(m_binder, "UC009", "wwInvalidEmailID");
			}
		}
		catch (Exception e)
		{
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
