package hays.emailServiceClient;
import static intradoc.shared.SharedObjects.getEnvironmentValue;

import java.util.Date;
import java.util.GregorianCalendar;

import hays.co.uk.HaysUtil;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.BindingProvider;

import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.data.DataException;
import intradoc.server.ServiceHandler;
import intradoc.shared.SharedObjects;
import intradoc.common.ExecutionContext;
import intradoc.common.ExecutionContextAdaptor;
import intradoc.common.IdcLocale;
import intradoc.common.LocaleResources;

// added to accomodate multiple WSDLs 
public class CallReqCandInfoEmailService extends ServiceHandler
{

	ExecutionContext ctx = new ExecutionContextAdaptor();
	public static ObjectFactory objectFactory = new ObjectFactory();
	public void handleCandInfoReqEmailWS() throws DataException, ServiceException 
	{		
		
		System.setProperty("http.proxyUser", "");
		System.setProperty("http.proxyPassword", "");
		System.setProperty("http.proxyHost", "");
		System.setProperty("http.proxyPort", "");
		SystemUtils.trace("hays_req_cand_info_Email_WS","inside handlecandinforeqemailws().....");
		String firstName=escapeHTML(super.m_binder.getLocal("firstName"));
		String lastName=escapeHTML(super.m_binder.getLocal("lastName"));
		String country=escapeHTML(super.m_binder.getLocal("country"));
		String apacCheck=getEnvironmentValue("IS_APAC_ENV");
		// for Hungary, it will be last name+first name
		// for all other sites, it will be first name + last name
		String []countryFromLocale = country.split("_");
		String employerFullName ="";
        String countryCode = countryFromLocale[1];
        if("HU".equalsIgnoreCase(countryCode))
        {
        	 employerFullName=lastName+" "+firstName;
        }
        else if("true".equalsIgnoreCase(apacCheck))
        {
        	 employerFullName=firstName; 
        }
         else
        {
        	employerFullName=firstName+" "+lastName;
        }
		
		String empEmail=escapeHTML(super.m_binder.getLocal("empEmail"));
		String emplPhone=escapeHTML(super.m_binder.getLocal("emplPhone"));
		String emplOrg=escapeHTML(super.m_binder.getLocal("emplOrg"));
		String emplJobTitle=escapeHTML(super.m_binder.getLocal("emplJobTitle"));
		String notes=escapeHTML(super.m_binder.getLocal("notes"));
		String refID=escapeHTML(super.m_binder.getLocal("refID"));
		String jobTitle=escapeHTML(super.m_binder.getLocal("jobTitle"));
		String haysConsultant=escapeHTML(super.m_binder.getLocal("haysConsultant"));
		String consEmail=escapeHTML(super.m_binder.getLocal("consEmail"));
		String consPhone=escapeHTML(super.m_binder.getLocal("consPhone"));
		String candidateAvailFrom=escapeHTML(super.m_binder.getLocal("candidateAvailFrom"));
		String candDescLink=escapeHTML(super.m_binder.getLocal("candDescLink"));
		String consOffice=escapeHTML(super.m_binder.getLocal("consOffice"));
		String consName=escapeHTML(super.m_binder.getLocal("consName"));
		//String captchaValue = super.m_binder.getLocal("captcha");
		String gRecaptchaResponse = super.m_binder.getLocal("captchaResponse");
		SystemUtils.trace("hays_req_cand_info_Email_WS", "gRecaptchaResponse is =" + gRecaptchaResponse);
    	String apacCheck1=  SharedObjects.getEnvironmentValue("IS_APAC_ENV");;
    	SystemUtils.trace("hays_req_cand_info_Email_WS", "apac check =" + apacCheck1);
    //	SystemUtils.trace("hays_req_cand_info_Email_WS", "captcha value =" + captchaValue);
		SharedObjects.putEnvironmentValue("country",country); // added to accomodate multiple WSDLs
		String fromAddress=escapeHTML(super.m_binder.getLocal("fromAddress"));
		String fromName=escapeHTML(super.m_binder.getLocal("fromName"));
		String HAYS_DOMAIN=escapeHTML(super.m_binder.getLocal("HAYS_DOMAIN"));
		String HAYS_LOGO=escapeHTML(super.m_binder.getLocal("HAYS_LOGO"));
		String captchaAddCountry= SharedObjects.getEnvironmentValue("CaptchaEnabled");
		//String CandInfoEmpEmailSubject=(super.m_binder.getLocal("CandInfoEmpEmailSubject")); //added for localized subject string
		//String CandInfoConsEmailSubject=(super.m_binder.getLocal("CandInfoConsEmailSubject"));//added for localized subject string
		String localeName=super.m_binder.getLocal("locale");
		SystemUtils.trace("hays_req_cand_info_Email_WS","apacCheck:::"+apacCheck); 
		setLocale(localeName);
		
		String CandInfoEmpEmailSubject=LocaleResources.getString("wwCandInfoEmpEmailSubject", ctx); //added for localized subject string
		String CandInfoConsEmailSubject=LocaleResources.getString("wwCandInfoConsEmailSubject", ctx);//added for localized subject string
		
		
		SystemUtils.trace("hays_req_cand_info_Email_WS","firstName:::"+firstName); 
		SystemUtils.trace("hays_req_cand_info_Email_WS","lastName:::"+lastName); 
		SystemUtils.trace("hays_req_cand_info_Email_WS","employerFullName:::"+employerFullName); 
		SystemUtils.trace("hays_req_cand_info_Email_WS","empEmail:::"+empEmail); 
		SystemUtils.trace("hays_req_cand_info_Email_WS","emplPhone:::"+emplPhone); 
		SystemUtils.trace("hays_req_cand_info_Email_WS","emplOrg:::"+emplOrg); 
		SystemUtils.trace("hays_req_cand_info_Email_WS","emplJobTitle:::"+emplJobTitle); 
		SystemUtils.trace("hays_req_cand_info_Email_WS","notes:::"+notes); 
		SystemUtils.trace("hays_req_cand_info_Email_WS","refID:::"+refID); 
		SystemUtils.trace("hays_req_cand_info_Email_WS","jobTitle:::"+jobTitle); 
		SystemUtils.trace("hays_req_cand_info_Email_WS","haysConsultant:::"+haysConsultant); 
		SystemUtils.trace("hays_req_cand_info_Email_WS","consEmail:::"+consEmail); 
		SystemUtils.trace("hays_req_cand_info_Email_WS","consPhone:::"+consPhone); 
		SystemUtils.trace("hays_req_cand_info_Email_WS","candidateAvailFrom:::"+candidateAvailFrom); 
		SystemUtils.trace("hays_req_cand_info_Email_WS","candDescLink:::"+candDescLink); 
		SystemUtils.trace("hays_req_cand_info_Email_WS","consOffice:::"+consOffice); 
		SystemUtils.trace("hays_req_cand_info_Email_WS","consName:::"+consName); 
		SystemUtils.trace("hays_req_cand_info_Email_WS","country:::"+country); 
		SystemUtils.trace("hays_req_cand_info_Email_WS","fromAddress:::"+fromAddress); 
		SystemUtils.trace("hays_req_cand_info_Email_WS","fromName:::"+fromName); 
		SystemUtils.trace("hays_req_cand_info_Email_WS","HAYS_DOMAIN:::"+HAYS_DOMAIN); 
		SystemUtils.trace("hays_req_cand_info_Email_WS","HAYS_LOGO:::"+HAYS_LOGO); 
		SystemUtils.trace("hays_req_cand_info_Email_WS","CandInfoEmpEmailSubject:::"+CandInfoEmpEmailSubject); //added for localized subject string
		SystemUtils.trace("hays_req_cand_info_Email_WS","CandInfoConsEmailSubject:::"+CandInfoConsEmailSubject); //added for localized subject string
		boolean isCaptchaCheck=false;
		String[] captchaCoutriesList = captchaAddCountry.split(",");
    	String captchaValue = "N";
    	for (String fromCountry : captchaCoutriesList){
    		if(fromCountry.equals(m_binder.getLocal("domainId")))
    			captchaValue = "Y";
    	}
    	SystemUtils.trace("hays_req_cand_info_Email_WS", "Captcha value in sendMail:" + captchaValue);
		if("Y".equalsIgnoreCase(captchaValue))
		{
		isCaptchaCheck=checkCaptcha(captchaValue,gRecaptchaResponse,apacCheck1);
		}
		
		SystemUtils.trace("hays_req_cand_info_Email_WS","isCaptchaCheck:::"+isCaptchaCheck);
		boolean isMailSendSuccessEmp =false;
		boolean isMailSendSuccessCons =false;
		String result="";
		isMailSendSuccessEmp = sendEmailToEmployer(refID,jobTitle,haysConsultant,employerFullName,consEmail,
				consPhone,consOffice,empEmail,candidateAvailFrom,candDescLink,country,fromAddress,fromName,HAYS_DOMAIN,HAYS_LOGO,CandInfoEmpEmailSubject);
		//SystemUtils.trace("hays_req_cand_info_Email_WS","isMailSendSuccessEmp to employer:::"+isMailSendSuccessEmp); 
		   
		
		isMailSendSuccessCons = sendEmailToConsultant(refID,jobTitle,employerFullName,emplJobTitle,emplOrg,
				consName,consEmail,emplPhone,empEmail,candidateAvailFrom,notes,country,fromAddress,fromName,HAYS_DOMAIN,HAYS_LOGO,CandInfoConsEmailSubject);
		//SystemUtils.trace("hays_req_cand_info_Email_WS","isMailSendSuccessCons to cons:::"+isMailSendSuccessCons);
		result = "isMailSendSuccessEmp="+isMailSendSuccessEmp+",isMailSendSuccessCons="+isMailSendSuccessCons;
		SystemUtils.trace("hays_req_cand_info_Email_WS","result:::"+result);
		m_binder.putLocal("result", result);
	}
	
	
	
	public static boolean sendEmailToEmployer(String refID,String jobTitle,String haysConsultant,
            String employerFullName,String consEmail,String consPhone,String consOffice,String empEmail,
            String candidateAvailFrom,String candDescLink,String country,String fromAddress,String fromName,String HAYS_DOMAIN,String HAYS_LOGO,String subject ) {
		    SystemUtils.trace("hays_req_cand_info_Email_WS","inside sendEmailToEmployer().....");
		   
			
	       // String serviceType="WEB_REGISTRATION_EMAIL"; WEB_CAND_REQ_EMAIL
		  String serviceType="WEB_CAND_REQ_EMAIL"; 
	        String sourceType ="WEBCENTER";
	        String targetSystem ="EMAIL SERVICE";
	        String uniqueMsgId = "ID_00007";
			
	        EmailContentHeader contentHeader = new EmailContentHeader();
			//EmailContentHeader contentHeader = EmailServiceClient.createHeader(serviceType,sourceSystem,targetSystem);
	        contentHeader = createEmailContentHeader(serviceType,sourceType,targetSystem,uniqueMsgId);
		  
	        //below string differs for different sites
	        //String fromAddress = LookupService.getInstance().getLookupAddress(ApplicationConstants.CANDIDATEINFO_FROM_ADDRESS);//"hays@hays.co.uk";
	        //String fromName = LookupService.getInstance().getLookupAddress(ApplicationConstants.CANDIDATEINFO_FROM_NAME);//"hays";
	        
			//String fromAddress = fromAddress;
			//String fromName = fromName;
			//String subject = "Your hays.co.uk enquiry confirmation";
			String ccName = "";
			String ccAddress="";
			String bccName= "";
			String bccAddress= ""; 

			EmailData payload = new EmailData();
		 	payload.setFromName(objectFactory.createEmailDataFromName(fromName));
	        payload.setFromAddress(objectFactory.createEmailDataFromAddress(fromAddress));
	        payload.setCCName(objectFactory.createEmailDataCCName(ccName));
	        payload.setCCAddress(objectFactory.createEmailDataCCAddress(ccAddress));
	        payload.setBCCName(objectFactory.createEmailDataBCCName(bccName));
	        payload.setBCCAddress(objectFactory.createEmailDataBCCAddress(bccAddress));
	        payload.setToAddress(objectFactory.createEmailDataToAddress(empEmail));
	        payload.setToName(objectFactory.createEmailDataToName(employerFullName));
	        payload.setSubject(objectFactory.createEmailDataSubject(subject));
	        

	        //EmailKeyValueVO[] arrEmailKeyValue = new EmailKeyValueVO[5];
	        
	        ArrayOfEmailKeyValueVO  arrayOfEmailKeyValueVO = objectFactory.createArrayOfEmailKeyValueVO();
	        
	        EmailKeyValueVO emailKeyValVo1 = new EmailKeyValueVO();
			emailKeyValVo1.setKey(objectFactory.createEmailKeyValueVOKey("EMPL_FULL_NAME"));  
			emailKeyValVo1.setValue(objectFactory.createEmailKeyValueVOValue(employerFullName));
			
			EmailKeyValueVO emailKeyValVo2 = new EmailKeyValueVO();  
			 
			// below key  differs for different sites
			//emailKeyValVo2.setValue(LookupService.getInstance().getLookupAddress(ApplicationConstants.HAYS_DOMAIN));
			emailKeyValVo2.setKey(objectFactory.createEmailKeyValueVOKey("HAYS_DOMAIN"));   
			//emailKeyValVo2.setValue(objectFactory.createEmailKeyValueVOValue("UK"));
			emailKeyValVo2.setValue(objectFactory.createEmailKeyValueVOValue(HAYS_DOMAIN));
			
			EmailKeyValueVO emailKeyValVo4 = new EmailKeyValueVO();
			emailKeyValVo4.setKey(objectFactory.createEmailKeyValueVOKey("CANDTITLE"));   
			emailKeyValVo4.setValue(objectFactory.createEmailKeyValueVOValue(jobTitle));
			
			EmailKeyValueVO emailKeyValVo5 = new EmailKeyValueVO();
			emailKeyValVo5.setKey(objectFactory.createEmailKeyValueVOKey("REFERENCE"));    
			emailKeyValVo5.setValue(objectFactory.createEmailKeyValueVOValue(refID));
			
			EmailKeyValueVO emailKeyValVo6 = new EmailKeyValueVO();
			emailKeyValVo6.setKey(objectFactory.createEmailKeyValueVOKey("CONSULTANT_NAME"));     
			emailKeyValVo6.setValue(objectFactory.createEmailKeyValueVOValue(haysConsultant));
			
			EmailKeyValueVO emailKeyValVo7 = new EmailKeyValueVO();
			emailKeyValVo7.setKey(objectFactory.createEmailKeyValueVOKey("CONSULTANT_OFFICE"));    
			emailKeyValVo7.setValue(objectFactory.createEmailKeyValueVOValue(consOffice));
			
			EmailKeyValueVO emailKeyValVo8 = new EmailKeyValueVO();
			emailKeyValVo8.setKey(objectFactory.createEmailKeyValueVOKey("CONSULTANT_EMAIL"));    
			emailKeyValVo8.setValue(objectFactory.createEmailKeyValueVOValue(consEmail));
			
			EmailKeyValueVO emailKeyValVo9 = new EmailKeyValueVO();
			emailKeyValVo9.setKey(objectFactory.createEmailKeyValueVOKey("CONSULTANT_PHONE_NO"));    
			emailKeyValVo9.setValue(objectFactory.createEmailKeyValueVOValue(consPhone));
			
			EmailKeyValueVO emailKeyValVo10 = new EmailKeyValueVO();
			emailKeyValVo10.setKey(objectFactory.createEmailKeyValueVOKey("HAYS_LOGO")); 
			//emailKeyValVo10.setValue(objectFactory.createEmailKeyValueVOValue("Hays_Logo_Url"));
			emailKeyValVo10.setValue(objectFactory.createEmailKeyValueVOValue(HAYS_LOGO));
			
			EmailKeyValueVO emailKeyValVo11 = new EmailKeyValueVO();
			emailKeyValVo11.setKey(objectFactory.createEmailKeyValueVOKey("AVAILABLE_FROM"));      
			emailKeyValVo11.setValue(objectFactory.createEmailKeyValueVOValue(candidateAvailFrom));
			
			EmailKeyValueVO emailKeyValVo12 = new EmailKeyValueVO();
			emailKeyValVo12.setKey(objectFactory.createEmailKeyValueVOKey("CANDIDATE_DESC_LINK"));      
			emailKeyValVo12.setValue(objectFactory.createEmailKeyValueVOValue(candDescLink));
			
			
			arrayOfEmailKeyValueVO.getEmailKeyValueVO().add(emailKeyValVo1);

			arrayOfEmailKeyValueVO.getEmailKeyValueVO().add(emailKeyValVo2);


			arrayOfEmailKeyValueVO.getEmailKeyValueVO().add(emailKeyValVo4);

			arrayOfEmailKeyValueVO.getEmailKeyValueVO().add(emailKeyValVo5);

			arrayOfEmailKeyValueVO.getEmailKeyValueVO().add(emailKeyValVo6);

			arrayOfEmailKeyValueVO.getEmailKeyValueVO().add(emailKeyValVo7);

			arrayOfEmailKeyValueVO.getEmailKeyValueVO().add(emailKeyValVo8);

			arrayOfEmailKeyValueVO.getEmailKeyValueVO().add(emailKeyValVo9);

			arrayOfEmailKeyValueVO.getEmailKeyValueVO().add(emailKeyValVo10);
			
			arrayOfEmailKeyValueVO.getEmailKeyValueVO().add(emailKeyValVo11);
			
			arrayOfEmailKeyValueVO.getEmailKeyValueVO().add(emailKeyValVo12);
			
	        //System.out.println("arrayOfEmailKeyValueVO size :::"+arrayOfEmailKeyValueVO.emailKeyValueVO.size());

	        
	        EmailAttachement attachment = objectFactory.createEmailAttachement();

	        
	        String stringToConvert = "Test String";     
		     byte[] theByteArray = stringToConvert.getBytes();

	        attachment.setFileContent(objectFactory.createEmailAttachementFileContent(theByteArray));
	        attachment.setFileName(objectFactory.createEmailAttachementFileName("testing"));
	        attachment.setIsCompresed(objectFactory.createEmailAttachementIsCompresed("false"));
	        attachment.setSha512Hash(objectFactory.createEmailAttachementSha512Hash("Testing"));
	        payload.setAttachment(objectFactory.createEmailDataAttachment(attachment));
	        
	        
	        payload.setEmailKeyValueList(objectFactory.createEmailDataEmailKeyValueList(arrayOfEmailKeyValueVO));
	      
		boolean isEmailSent = runEmailProcess(contentHeader, payload,country);  
		return isEmailSent;
		}
	
	public static String escapeHTML(String s) {
	    StringBuilder out = new StringBuilder(Math.max(16, s.length()));
	    for (int i = 0; i < s.length(); i++) {
	        char c = s.charAt(i);
	        if (c == '<' || c == '>' || c == '&') {
	            out.append("&#");
	            out.append((int) c);
	            out.append(';');
	        } else {
	            out.append(c);
	        }
	    }
	    return out.toString();
	}
	
	public static boolean sendEmailToConsultant(String refID,String jobTitle,String employerFullName,String emplJobTitle,String emplOrg,
            String consName,String consEmail,String emplPhone,String empEmail,
            String candidateAvailFrom,String notes,String country,String fromAddress,String fromName,String HAYS_DOMAIN,String HAYS_LOGO,String subject) {
		  SystemUtils.trace("hays_req_cand_info_Email_WS","inside sendEmailToConsultant().....");
		
		  
			
	        String serviceType="WEB_CONSULTANT_REQ_UK";
	        String sourceType ="WEBCENTER";
	        String targetSystem ="EMAIL SERVICE";
	        String uniqueMsgId = "ID_00008";
			
	        EmailContentHeader contentHeader = new EmailContentHeader();
			//EmailContentHeader contentHeader = EmailServiceClient.createHeader(serviceType,sourceSystem,targetSystem);
	        contentHeader = createEmailContentHeader(serviceType,sourceType,targetSystem,uniqueMsgId);
		  
			//String fromAddress = "hays@hays.co.uk";
			//String fromName = "hays";
			String ccName = "";
			String ccAddress="";
			String bccName= "";
			String bccAddress= ""; 
			//String subject ="*** URGENT ALERT - CLIENT SEEKS CANDIDATE ***";

			EmailData payload = new EmailData();
		 	payload.setFromName(objectFactory.createEmailDataFromName(fromName));
	        payload.setFromAddress(objectFactory.createEmailDataFromAddress(fromAddress));
	        payload.setCCName(objectFactory.createEmailDataCCName(ccName));
	        payload.setCCAddress(objectFactory.createEmailDataCCAddress(ccAddress));
	        payload.setBCCName(objectFactory.createEmailDataBCCName(bccName));
	        payload.setBCCAddress(objectFactory.createEmailDataBCCAddress(bccAddress));
	        payload.setToAddress(objectFactory.createEmailDataToAddress(consEmail));
	        payload.setToName(objectFactory.createEmailDataToName(consName));
	        payload.setSubject(objectFactory.createEmailDataSubject(subject));
	        

	        //EmailKeyValueVO[] arrEmailKeyValue = new EmailKeyValueVO[5];
	        
	        ArrayOfEmailKeyValueVO  arrayOfEmailKeyValueVO = objectFactory.createArrayOfEmailKeyValueVO();
	        
	        EmailKeyValueVO emailKeyValVo1 = new EmailKeyValueVO();
			emailKeyValVo1.setKey(objectFactory.createEmailKeyValueVOKey("EMPL_FULL_NAME"));  
			emailKeyValVo1.setValue(objectFactory.createEmailKeyValueVOValue(employerFullName));
			
			EmailKeyValueVO emailKeyValVo2 = new EmailKeyValueVO();                                                               
			emailKeyValVo2.setKey(objectFactory.createEmailKeyValueVOKey("EMPL_TITLE"));   
			emailKeyValVo2.setValue(objectFactory.createEmailKeyValueVOValue(emplJobTitle));
			
			EmailKeyValueVO emailKeyValVo4 = new EmailKeyValueVO();
			emailKeyValVo4.setKey(objectFactory.createEmailKeyValueVOKey("EMPL_ORGANISATION"));   
			emailKeyValVo4.setValue(objectFactory.createEmailKeyValueVOValue(emplOrg));
			
			EmailKeyValueVO emailKeyValVo5 = new EmailKeyValueVO();
			emailKeyValVo5.setKey(objectFactory.createEmailKeyValueVOKey("HAYS_DOMAIN"));    
			//emailKeyValVo5.setValue(objectFactory.createEmailKeyValueVOValue("UK"));
			emailKeyValVo5.setValue(objectFactory.createEmailKeyValueVOValue(HAYS_DOMAIN));
			
			EmailKeyValueVO emailKeyValVo6 = new EmailKeyValueVO();
			emailKeyValVo6.setKey(objectFactory.createEmailKeyValueVOKey("CANDTITLE"));     
			emailKeyValVo6.setValue(objectFactory.createEmailKeyValueVOValue(jobTitle));
			
			EmailKeyValueVO emailKeyValVo7 = new EmailKeyValueVO();
			emailKeyValVo7.setKey(objectFactory.createEmailKeyValueVOKey("REFERENCE"));    
			emailKeyValVo7.setValue(objectFactory.createEmailKeyValueVOValue(refID));
			
			EmailKeyValueVO emailKeyValVo8 = new EmailKeyValueVO();
			emailKeyValVo8.setKey(objectFactory.createEmailKeyValueVOKey("EMPL_EMAIL"));    
			emailKeyValVo8.setValue(objectFactory.createEmailKeyValueVOValue(empEmail));
			
			EmailKeyValueVO emailKeyValVo9 = new EmailKeyValueVO();
			emailKeyValVo9.setKey(objectFactory.createEmailKeyValueVOKey("EMPL_PHONE_NO"));    
			emailKeyValVo9.setValue(objectFactory.createEmailKeyValueVOValue(emplPhone));
			
			EmailKeyValueVO emailKeyValVo10 = new EmailKeyValueVO();
			emailKeyValVo10.setKey(objectFactory.createEmailKeyValueVOKey("HAYS_LOGO"));      
			//emailKeyValVo10.setValue(objectFactory.createEmailKeyValueVOValue("Hays_Logo_Url"));
			emailKeyValVo10.setValue(objectFactory.createEmailKeyValueVOValue(HAYS_LOGO));
			
			EmailKeyValueVO emailKeyValVo11 = new EmailKeyValueVO();
			emailKeyValVo11.setKey(objectFactory.createEmailKeyValueVOKey("AVAILABLE_FROM"));      
			emailKeyValVo11.setValue(objectFactory.createEmailKeyValueVOValue(candidateAvailFrom));
			
			EmailKeyValueVO emailKeyValVo12 = new EmailKeyValueVO();
			emailKeyValVo12.setKey(objectFactory.createEmailKeyValueVOKey("ADDITIONAL_NOTE"));      
			emailKeyValVo12.setValue(objectFactory.createEmailKeyValueVOValue(notes));
			
			
			arrayOfEmailKeyValueVO.getEmailKeyValueVO().add(emailKeyValVo1);

			arrayOfEmailKeyValueVO.getEmailKeyValueVO().add(emailKeyValVo2);


			arrayOfEmailKeyValueVO.getEmailKeyValueVO().add(emailKeyValVo4);

			arrayOfEmailKeyValueVO.getEmailKeyValueVO().add(emailKeyValVo5);

			arrayOfEmailKeyValueVO.getEmailKeyValueVO().add(emailKeyValVo6);

			arrayOfEmailKeyValueVO.getEmailKeyValueVO().add(emailKeyValVo7);

			arrayOfEmailKeyValueVO.getEmailKeyValueVO().add(emailKeyValVo8);

			arrayOfEmailKeyValueVO.getEmailKeyValueVO().add(emailKeyValVo9);

			arrayOfEmailKeyValueVO.getEmailKeyValueVO().add(emailKeyValVo10);
			
			arrayOfEmailKeyValueVO.getEmailKeyValueVO().add(emailKeyValVo11);
			
			arrayOfEmailKeyValueVO.getEmailKeyValueVO().add(emailKeyValVo12);
			
	        //System.out.println("arrayOfEmailKeyValueVO size :::"+arrayOfEmailKeyValueVO.emailKeyValueVO.size());

	        
	        EmailAttachement attachment = objectFactory.createEmailAttachement();

	        
	        String stringToConvert = "Test String";     
		     byte[] theByteArray = stringToConvert.getBytes();

	        attachment.setFileContent(objectFactory.createEmailAttachementFileContent(theByteArray));
	        attachment.setFileName(objectFactory.createEmailAttachementFileName("testing"));
	        attachment.setIsCompresed(objectFactory.createEmailAttachementIsCompresed("false"));
	        attachment.setSha512Hash(objectFactory.createEmailAttachementSha512Hash("Testing"));
	        payload.setAttachment(objectFactory.createEmailDataAttachment(attachment));
	        
	        
	        payload.setEmailKeyValueList(objectFactory.createEmailDataEmailKeyValueList(arrayOfEmailKeyValueVO));
	      
		boolean isEmailSent = runEmailProcess(contentHeader, payload,country);  
		return isEmailSent;
		}
	

	public static boolean runEmailProcess(EmailContentHeader header,EmailData payLoad,String country)
	{
		
		//SystemUtils.trace("hays_req_cand_info_Email_WS","inside runEmailProcess..."); 
		 boolean isEmailSent = false;
		 try {
			 SystemUtils.trace("hays_req_cand_info_Email_WS","inside runEmailProcess().....country:"+country);
			 
				//String endPoint = "http://sasoat1.sguk.hays.loc:4047/Services/EmailPublisherService.svc?wsdl"; commetned to accomodate multiple WSDLs	
			
			 // added for multiple WSDLs start
			 
			 
			
	            String []countryFromLocale = country.split("_");
	            String countryCode = countryFromLocale[1];
	            SystemUtils.trace("hays_req_cand_info_Email_WS","countryCode in CallReqCandInfoEmailService :::"+countryCode); 
			 
				
			 String endPoint=SharedObjects.getEnvironmentValue("WSDL_ENDPOINT_"+countryCode);
			 
			 
			   
				SystemUtils.trace("hays_req_cand_info_Email_WS","WSDL endpoint :"+endPoint);
				
				EmailMessage emailMessage = new EmailMessage();
				emailMessage.setHeader(objectFactory.createEmailMessageHeader(header));
				emailMessage.setPayLoad(objectFactory.createEmailMessagePayLoad(payLoad));
				emailMessage.setCountry(objectFactory.createEmailMessageCountry(country));
				
				
				EmailPublisherService client =new EmailPublisherService();
				IEmailPublisherService iEmailPublisherService=  client.getBasicHttpBindingIEmailPublisherService();
				((BindingProvider)iEmailPublisherService).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endPoint);
				iEmailPublisherService.processEmail(emailMessage);
				isEmailSent =true;
				
		 }
		 catch( Exception e)
		 {
			 isEmailSent = false;
              e.printStackTrace();
		 }
		 
		return isEmailSent;
	}
				
	private static EmailContentHeader createEmailContentHeader(String serviceType,String sourceSystem, String targetSystem,String uniqueMsgId)
	{
		SystemUtils.trace("hays_req_cand_info_Email_WS","inside createEmailContentHeader..."); 
		EmailContentHeader header = new EmailContentHeader();
		XMLGregorianCalendar xcal=null;;
        Date date =new Date();
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTimeInMillis(date.getTime());
        try 
        {
			xcal = DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);
		} 
        catch (DatatypeConfigurationException e1) 
        {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

        
		//System.out.println("xcal:::"+xcal.getDay());
        header.setMailDate(xcal);
        header.setServiceType(objectFactory.createEmailContentHeaderServiceType(serviceType));
        header.setSourceSystem(objectFactory.createEmailContentHeaderSourceSystem(sourceSystem));
        header.setTargetSystem(objectFactory.createEmailContentHeaderTargetSystem(targetSystem));
        header.setUniqueMsgId(objectFactory.createEmailContentHeaderUniqueMsgId(uniqueMsgId));
        return header;
	}
	
public String setLocale(String locale)
{
	if (locale== null || locale.length() <= 0)
	{
		locale = "en-GB";
	}
	SystemUtils.trace("hays_req_cand_info_Email_WS", "SiteLocale:"+locale);		
	IdcLocale idcl = LocaleResources.getLocale(locale);
	ctx.setCachedObject("UserLocale", idcl);
	String siteLocale=locale;
	return  siteLocale;
}

public static boolean checkCaptcha(String captchaValue, String gRecaptchaResponse, String apacCheck1)
{
	String url = "https://www.google.com/recaptcha/api/siteverify";
	String secret = SharedObjects.getEnvironmentValue("PrivateKey");
	String USER_AGENT = "Mozilla/5.0";
	boolean verify=false;
	
	 try
	 {
			
				 SystemUtils.trace("hays_req_cand_info_Email_WS", "inside if =" + captchaValue);
		
			   if((gRecaptchaResponse!=null) && ("Y".equalsIgnoreCase(captchaValue))){
			    	 verify = HaysUtil.verify(gRecaptchaResponse, url, USER_AGENT, secret,apacCheck1);
			    	 		    	 
			    	 SystemUtils.trace("hays_req_cand_info_Email_WS", "verify is =" + verify);
			    	 if(!verify)
						{
							SystemUtils.trace("hays_req_cand_info_Email_WS","checking for verification");
												
							return verify;
							
						}
			    }
			 else if (!(gRecaptchaResponse!=null) && ("Y".equalsIgnoreCase(captchaValue)))      
			   {
			     SystemUtils.trace("hays_req_cand_info_Email_WS", "inside else condition =" + captchaValue);
			      return verify;
			    }
					
	 }
	   catch (Exception e)
		{
			SystemUtils.trace("hays_req_cand_info_Email_WS", "Error: " + e.toString());
		}
	 return verify;	
}

}
