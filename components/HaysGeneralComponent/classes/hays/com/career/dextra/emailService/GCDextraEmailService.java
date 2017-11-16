package hays.com.career.dextra.emailService;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.xml.ws.BindingProvider;

import org.apache.tika.config.TikaConfig;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;

import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.data.DataException;
import intradoc.server.ServiceHandler;
import intradoc.shared.SharedObjects; // added to accomodate multiple WSDLs 

public class GCDextraEmailService extends ServiceHandler
{
 
	
	public static ObjectFactory objectFactory = new ObjectFactory();
	
	/**
	 * This is Service method which is used to call dextra email webservices.
	 * @throws DataException
	 * @throws ServiceException
	 */
	public void globalCareerDextraEmailWS() throws DataException, ServiceException 
	{	
		boolean isDesiredExtension = false;
		SystemUtils.trace("GC_EMAIL","Inside GCDextraEmailService globalCareerDextraEmailWS.....");
		String response="false";
		String endPoint=SharedObjects.getEnvironmentValue("WSDL_ENDPOINT_GC_DEXTRA");
		SystemUtils.trace("GC_EMAIL","endPoint : " +endPoint);
		String firstName=(super.m_binder.getLocal("full_name"));
		String lastName=(super.m_binder.getLocal("sur_name"));
		String email=(super.m_binder.getLocal("email"));
		String eligible=(super.m_binder.getLocal("eligible"));
		String whereHear=(super.m_binder.getLocal("where_hear"));
		String exp=(super.m_binder.getLocal("exp"));	 
		String mobile=(super.m_binder.getLocal("mobile")); 
		String fileLocation=m_binder.getLocal("AttachedDocument:path");

		String fileName=m_binder.getLocal("AttachedDocument");
				
		isDesiredExtension=getExtentionCheck(fileName);
		if(!isDesiredExtension)
		{
			SystemUtils.trace("GC_EMAILCheck","Extension not valid");
			return;
		}

		String siteLocale=(super.m_binder.getLocal("SiteLocale"));
		String jobId=(super.m_binder.getLocal("Job_ID"));
		String consultantEmailId=(super.m_binder.getLocal("consultantEmailId"));
		String JobRef=(super.m_binder.getLocal("JobRef"));
		//String SpeculativeEmail=(super.m_binder.getLocal("SpeculativeEmail"));
		String configKey = siteLocale.replace("-","")+"EmailID";
		String SpeculativeEmail=SharedObjects.getEnvironmentValue(configKey);
		String country=(super.m_binder.getLocal("country"));
		String consultantName=(super.m_binder.getLocal("consultantName"));
		String jobTitile=(super.m_binder.getLocal("jobTitile"));

		
		SystemUtils.trace("GC_EMAIL","FormName is "+firstName);
		SystemUtils.trace("GC_EMAIL","lastName is "+lastName);
		SystemUtils.trace("GC_EMAIL","email is "+email);
		SystemUtils.trace("GC_EMAIL","whereHear is "+whereHear);
		SystemUtils.trace("GC_EMAIL","exp "+exp);
		SystemUtils.trace("GC_EMAIL","mobile is "+mobile);
		SystemUtils.trace("GC_EMAIL","fileLocation is "+fileLocation);
		
		SystemUtils.trace("GC_EMAIL","fileName is "+fileName);
		SystemUtils.trace("GC_EMAIL","siteLocale is "+siteLocale);
		SystemUtils.trace("GC_EMAIL","jobId is "+jobId);
		SystemUtils.trace("GC_EMAIL","consultantEmailId is "+consultantEmailId);
		SystemUtils.trace("GC_EMAIL","SpeculativeEmail is "+SpeculativeEmail);
		SystemUtils.trace("GC_EMAIL","JobRef is "+JobRef);
		SystemUtils.trace("GC_EMAIL","country is "+country);	
		SystemUtils.trace("GC_EMAIL","consultantName is "+consultantName);	
		SystemUtils.trace("GC_EMAIL","jobTitile is "+jobTitile);	
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
		daxtraJobApplication.setJobTitle(objectFactory.createDaxtraJobApplicationJobTitle(jobTitile));
		daxtraJobApplication.setConsultantName(objectFactory.createDaxtraJobApplicationConsultantName(consultantName));
		//***************
		daxtraJobApplication.setDayTimePhone(objectFactory.createDaxtraJobApplicationDayTimePhone(""));
		daxtraJobApplication.setAdditionalInformation(objectFactory.createDaxtraJobApplicationAdditionalInformation(""));
		daxtraJobApplication.setBestWayToContact(objectFactory.createDaxtraJobApplicationBestWayToContact(""));
		
		if(null != jobId &&  jobId.length() !=0){
			SystemUtils.trace("GC_EMAIL","in jobId is not null mode ");
			daxtraJobApplication.setBestWayToContact(objectFactory.createDaxtraJobApplicationApplyMode("JobApply"));
			
		}else{
			SystemUtils.trace("GC_EMAIL","in jobId is null mode ");
			daxtraJobApplication.setBestWayToContact(objectFactory.createDaxtraJobApplicationApplyMode("CVOnly"));
		}		 
		 BinaryContent cv = new BinaryContent();
		 cv.setFileName(objectFactory.createBinaryContentFileName(fileName));	 	
		 setBinaryContent(cv, fileLocation);	
		
 
		System.setProperty("http.proxyUser", "");
		System.setProperty("http.proxyPassword", "");
		System.setProperty("http.proxyHost", "");
		System.setProperty("http.proxyPort", "");
		 
		ApplyStatus applyStatus = new ApplyStatus();
		JobService jobServiceClient = new JobService();			 
		IJobService iJobService =jobServiceClient.getBasicHttpBindingIJobService();	 
		//((BindingProvider)iJobService).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endPoint);
  
		
		 try{
		 
			 applyStatus= iJobService.applyForJobUsingDaxtra(daxtraJobApplication, cv, null);			 
			 SystemUtils.trace("GC_EMAIL","status Exception is "+applyStatus.getException().getValue());
			 SystemUtils.trace("GC_EMAIL","status is "+applyStatus.getStatus().getValue());
			 if(null != applyStatus.getStatus().getValue() && ((String) applyStatus.getStatus().getValue()).equalsIgnoreCase("Success")){
				 response= "true";
			 }
			 
			 m_binder.putLocal("mailResponse",response);	
		 }
			
	     catch(Exception e){	       
	       SystemUtils.trace("GC_EMAIL","in Catch block status Exception is 1 "+e);
	       SystemUtils.trace("GC_EMAIL","status code is "+applyStatus.getException().getValue());
		   SystemUtils.trace("GC_EMAIL","status Exception is "+applyStatus.getStatus().getValue());
         }
	     SystemUtils.trace("GC_EMAIL"," response :  "+response);
		
	}

	
	/**
	 * This method is used get binary object of file.
	 * @param cv
	 * @param fileLocation
	 */
	public  void setBinaryContent( BinaryContent cv,String fileLocation ){		
		SystemUtils.trace("GC_EMAIL"," inside setBinaryContent  ");	 
		   
	    File file = new File(fileLocation);
	    try
	    {	  
	       FileInputStream fin = new FileInputStream(file);	 
	       byte fileContent[] = new byte[(int)file.length()];  
	       fin.read(fileContent);	   
	       cv.setBinary(objectFactory.createBinaryContentBinary(fileContent));	     
	    }
	    catch(FileNotFoundException e)
	    {
	       
	      SystemUtils.trace("GC_EMAIL","File not found strFileContent " +e);
	    }
	    catch(IOException ioe)
	    {
	       
	      SystemUtils.trace("GC_EMAIL","Exception while reading the file " +ioe);
	    }
	    
	}
	
	public boolean getExtentionCheck(String fileName){
		 SystemUtils.trace("GC_EMAIL","check for get extension ");
		boolean bCheck=false;
		int dotposition = fileName.lastIndexOf(".");
		String ext = fileName.substring(dotposition + 1, fileName.length());
		long maxsize = 512000; //500 KB
		if (fileName != null)
		{   
			String fileLocation = m_binder.getLocal("AttachedDocument:path");
			String formName = m_binder.getLocal("DataFormName");
			SystemUtils.trace("GC_EMAIL", "FormName is " + formName);
			
			long length = fileName.length();
			if (length > maxsize) // checking the file size
			{ SystemUtils.trace("GC_EMAIL","check for file size " );
				bCheck=false;
			}
			if(formName.equalsIgnoreCase("CustomForm1"))
			{
			      if (!( "doc".equalsIgnoreCase(ext) || "docx".equalsIgnoreCase(ext) || "rtf".equalsIgnoreCase(ext) || "txt".equalsIgnoreCase(ext)))//checking the file extension
			      {   SystemUtils.trace("GC_EMAIL", "Custom form1 for ext chek false value...............");
			      bCheck=false;
			      } else {   SystemUtils.trace("GC_EMAIL", "Custom form1 for ext chek true value...............");
				  bCheck=true;
			      }
			}
			else
			{
				if (!( "doc".equalsIgnoreCase(ext) || "docx".equalsIgnoreCase(ext)))//checking the file extension
				{   SystemUtils.trace("GC_EMAIL", "Custom form2 for ext chek false value...............");
				bCheck=false;
				}else{  SystemUtils.trace("GC_EMAIL", "Custom form2 for ext chek false value...............");
				bCheck=true;
				}
			}
			if(fileLocation!=null && bCheck)
			{
				bCheck=getTikaCode(fileLocation);
				SystemUtils.trace("GC_EMAIL", "getTikaCode value1"+bCheck);	
				
			}
		
		}
		return bCheck;
	}
	
	public boolean getTikaCode(String fileLocation){
		String formName = m_binder.getLocal("DataFormName");
		boolean isValid=false;		
		String fileName = m_binder.getLocal("AttachedDocument");
		if ( fileLocation != null )
			SystemUtils.trace("GC_EMAIL", "filelocation check...............");	
		{
            File fileLoc = new File(fileLocation);
	        try {
	        	SystemUtils.trace("GC_EMAIL","fileName : " + fileName);
	            TikaConfig config = TikaConfig.getDefaultConfig();
	            TikaInputStream tikaIS = null;
             	    
	            try {
	            	SystemUtils.trace("GC_EMAIL", "inside try...............");	
	                tikaIS = TikaInputStream.get(fileLoc);

	                final Metadata metadata = new Metadata();
	                MediaType mimetype = config.getDetector().detect(TikaInputStream.get(fileLoc), metadata);
	                SystemUtils.trace("GC_EMAIL","mimeType : " +mimetype);
	                if(formName.equalsIgnoreCase("CustomForm1"))
	                {
	                     if(!((mimetype.toString().equalsIgnoreCase("application/rtf")) || (mimetype.toString().equalsIgnoreCase("application/octet-stream")) || (mimetype.toString().equalsIgnoreCase("application/x-tika-msoffice")) || (mimetype.toString().equalsIgnoreCase("application/x-tika-ooxml"))) )
	                     {
	                    	 SystemUtils.trace("GC_EMAIL", "Custom form1 for false value...............");
	                	 isValid=false;
                         }else{
                        	 SystemUtils.trace("GC_EMAIL", "Custom form1 for true value...............");
                    	 isValid=true;                    	
                         }
	                }
	                else
	                {
	                	 if(!((mimetype.toString().equalsIgnoreCase("application/x-tika-msoffice")) || (mimetype.toString().equalsIgnoreCase("application/x-tika-ooxml"))) )
	                	 {
	                		 SystemUtils.trace("GC_EMAIL", "Custom form2 for false value...............");
	 	                	isValid=false;
	                     }else{
	                    	 SystemUtils.trace("GC_EMAIL", "Custom form2 for true value...............");
	                     	isValid=true;                    	
	                     }
	                }
	            } finally {

	                if (tikaIS != null) {
	                	SystemUtils.trace("GC_EMAIL", "mimeType close");
	                    tikaIS.close();

	                }

	            }

	        }catch(IOException e){
	        	
	            e.printStackTrace();

	        }

	    }
		return isValid; 
	}
}
