package hays.co.uk;

import java.util.Currency; 
import java.util.Locale;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern; 
import com.hp.hpl.jena.ontology.OntModel;
import hays.co.uk.search.HaysSearchLocationsHandler;
import intradoc.common.ClassHelper;
import intradoc.common.ExecutionContext;
import intradoc.common.IdcStringBuilder;
import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.data.DataBinder;
import intradoc.data.DataException;
import intradoc.data.DataResultSet;
import intradoc.data.ResultSet;
import intradoc.data.Workspace;
import intradoc.provider.Provider;
import intradoc.provider.Providers;
import intradoc.server.Service;
import intradoc.shared.FilterImplementor;
import intradoc.shared.SharedObjects;
import intradoc.util.IdcMessage;
import infomentum.ontology.*;
import infomentum.ontology.loader.OntologyFacade;
import infomentum.ontology.navigation.OntologyNavigationHandler;
import hays.custom.multilingual.HaysWebSite;
import java.util.HashMap;
import java.util.Properties; 
import java.util.Calendar;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.SimpleDateFormat; 

public class MetadataSetFilter implements FilterImplementor {
	
	static final String LOCATIONS_TBL = SharedObjects.getEnvironmentValue("LocationsTable");
	static final String LOCATION_METADATA = "xHaysLocation";
	
	private Workspace m_ws = null;
	private DataBinder m_binder = null;
	private ExecutionContext m_ctx = null;
	
	private static final String DEFAULT_METADATA_CONF = SharedObjects.getEnvironmentValue("MetadataSetForContentTypes");
	
	private static final String ONT_PROVIDER = SharedObjects.getEnvironmentValue("OntologyProvider");
	
	private static final String Mode = SharedObjects.getEnvironmentValue("Mode");
	
	private static final Pattern SALLARY_TYPE_REGEX = Pattern.compile("<jobtype>([PCTE]+)</jobtype><min>([\\d\\.]*)</min><max>([\\d\\.]*)</max><paytype>([HDWMA]*)</paytype>");
	
	private static final String DEFALT_CURRENCY_SYMBOL = "£";
	
	private static final HashMap<String, Integer> salaryTYpeConversionRateMap = new HashMap<String, Integer>();
	static{
		salaryTYpeConversionRateMap.put("H", new Integer(1));
		salaryTYpeConversionRateMap.put("D", new Integer(10));
		salaryTYpeConversionRateMap.put("M", new Integer(100));
		salaryTYpeConversionRateMap.put("A", new Integer(1000));
	}

	
	public int doFilter(Workspace ws, DataBinder db, ExecutionContext ctx) throws DataException, ServiceException {
		this.m_ws = ws;
		this.m_binder = db;
		this.m_ctx = ctx;
		
		String filterParam = (String)ctx.getCachedObject("filterParameter");
		if("validateStandard".equals(filterParam) ) {
			
			boolean flag =checkDuplicateOffices();
			SystemUtils.trace("hays_search", "flag value" + flag);
			 if(flag)
			 {
				
				 SystemUtils.trace("hays_search","Inside the try block");	 
				 throw new ServiceException("Duplicate Offices exist in the System");
				   
				 /*catch(ServiceException ex)
				 {
					 SystemUtils.trace("hays_search", "Inside the catch block");
					 ex.setIdcMessage(new IdcMessage("Duplicate Offices exist"));
				 }*/
			 }
				 
			 
			if( DEFAULT_METADATA_CONF != null) {	
				// Sets WebSection and WebSites metadata for Jobs & candidate records
				setMetadata();
			}
			// extracts info to calculate and populate min and max salary and job type
			//populateSalaryRange_old();
			
			
			populateSalaryRange();
			// populate the currency symbol
			populateLocale();
			
			//populates Sepecialism id's 
			populateSpecialism();
			
			deleteSpecialismFromOfficeTable();
			//delete entriy from OfficeDetails table
			
			populateLocations();
			// location levels
			
			populateRegisteredDate();
			//Populate Jobs/Candidate Registered date
			
			//Populate locale_RecordID where records ID is JOb ReferenceID.
			populateLocaleWithRedordID();
			
			populateDescriptiveUrl();
			//Populate locale_RecordID for dDocType Not jobs and Candidates
			
		}
		return CONTINUE;
	}

	
	/**
	 * This method is used populate website locale with record ID into the LOCALE_RECORDID coulumn in metadata table. 
	 * 
	 */
private void populateLocaleWithRedordID(){
		
		if (("Jobs".equalsIgnoreCase(m_binder.getLocal("dDocType"))) || ("Candidates".equalsIgnoreCase(m_binder.getLocal("dDocType"))) 
				)
		{
			String recordID = m_binder.getLocal("xRecordId");
			String docTitle = m_binder.getLocal("dDocTitle").trim();	
			docTitle=docTitle.replaceAll("[;?:@&%#+$/,_\\s]+", "-").toLowerCase();
			String locationDescription = m_binder.getLocal("xLocationDescription").toLowerCase();
			locationDescription=locationDescription.replaceAll("[;?:@&%#+$/,_\\s]+", "-").toLowerCase();
			String locale = m_binder.getLocal("xLocale");
			SystemUtils.trace("hays_search", "populateLocaleWithRedordID -- recordID : " + recordID);
			SystemUtils.trace("hays_search", "populateLocaleWithRedordID -- docTitle : " + docTitle);
			SystemUtils.trace("hays_search", "populateLocaleWithRedordID -- LocationDescription : " + locationDescription);
		 	if (recordID != null && locale != null){
				if (recordID.length() >0 && locale.length() >0){
					try {					 
							m_binder.putLocal("xLocalerecordID", docTitle+"-"+locationDescription+"-"+locale+"_"+recordID);					
						}
						catch(Exception ex) {
					ex.getStackTrace();
					}				 
				}
			}
		}		
	}
//	Code Starts for Descriptive Url
	private void populateDescriptiveUrl(){
		  String dDocType = m_binder.getLocal("dDocType");
		  SystemUtils.trace("hays_search", "Content Type is : ="+dDocType);
		  
		  if ("Contrib".equalsIgnoreCase(Mode))
		  {
			 if (("AnnualReport".equalsIgnoreCase(m_binder.getLocal("dDocType").trim()))
						|| ("News".equalsIgnoreCase(m_binder.getLocal("dDocType").trim())) 
						||("PromotionalContent".equalsIgnoreCase(m_binder.getLocal("dDocType").trim()))
						||("SponsoredEmployer".equalsIgnoreCase(m_binder.getLocal("dDocType").trim()))
						||("Survey".equalsIgnoreCase(m_binder.getLocal("dDocType").trim())))
					{
				
				String RecordId=this.m_binder.getLocal("xRecordId");
  			  SystemUtils.trace("DescriptiveUrl", "RecordID is:" + RecordId+"test");
			    String urlDocTitle= m_binder.getLocal("dDocTitle");
				String pageUrl= m_binder.getLocal("xUrlPageName");
				  if(RecordId != null )
				  {
			    			  SystemUtils.trace("DescriptiveUrl", "RecordID : " + urlDocTitle+" is "+RecordId);
				  }else{
					  String RevClassID=this.m_binder.getLocal("dRevClassID");
					  m_binder.putLocal("xRecordId", RevClassID);	
					  SystemUtils.trace("DescriptiveUrl", "New Record ID : " + urlDocTitle+" is genrated "+RecordId);
				  }
				  String RevClassID=this.m_binder.getLocal("dRevClassID");
				  m_binder.putLocal("xRecordId", RevClassID);	
				  //String RecordIdN=this.m_binder.getLocal("xRecordId");
			
			  SystemUtils.trace("DescriptiveUrl", "RecordID for --  : " + dDocType+" is "+RevClassID);
			if ((pageUrl!=null && pageUrl.length()>0) ){
					pageUrl = pageUrl.replaceAll("[;?:@&%#+$/,_\\s]+", " ").toLowerCase().trim();
					pageUrl=pageUrl.replaceAll("\\s+", "-");
					SystemUtils.trace("DescriptiveUrl", "DescriptiveUrl+dRevClassID --  : " + pageUrl+"-"+RevClassID);
				try {					 
					m_binder.putLocal("xLocalerecordID", pageUrl+"-"+RevClassID);					
				}
				catch(Exception ex) {
				ex.getStackTrace();
				}	
			  }
			  else if((urlDocTitle !=null)&&(urlDocTitle.length()>0)){
					urlDocTitle = urlDocTitle.replaceAll("[;?:@&%#+$/,_\\s]+", " ").toLowerCase().trim();
					urlDocTitle=urlDocTitle.replaceAll("\\s+", "-");
					try {					 
						m_binder.putLocal("xLocalerecordID", urlDocTitle+"-"+RevClassID);					
					}
			
				catch(Exception ex) {
				ex.getStackTrace();
				 } 
			   }
			 }
		 	}
	}
	
       	
	//Code Ends for Descriptive Url
	
	
	private boolean checkDuplicateOffices()  throws ServiceException {
		
	  String dDocType = m_binder.getLocal("dDocType");
	  DataBinder param = new DataBinder();
	  SystemUtils.trace("hays_search", "Content Type is : ="+dDocType);
		
		if (dDocType != null && (dDocType.equalsIgnoreCase("Office"))) 
				{
			
			try { 
									
					String dDocTitle= m_binder.getLocal("dDocTitle");
					param.putLocal("dDocTitle", dDocTitle);
					SystemUtils.trace("hays_search", "dDocTitle value in the param object"+dDocTitle);
					String contentId= m_binder.getLocal("dDocName");
					SystemUtils.trace("hays_search", "Content Id is"+contentId);
					SystemUtils.trace("hays_search", "Document Name: " + dDocTitle);
					String addressline1= m_binder.getLocal("xAddressLine1");
					param.putLocal("addressline1", addressline1);
					SystemUtils.trace("hays_search", "addressLine 1 value in the param object"+addressline1);
					String postcode= m_binder.getLocal("xPostCode");
					//param.putLocal("addressline2", addressline2);
					SystemUtils.trace("hays_search", "Addresses: " + addressline1 + postcode );
					param.putLocal("postcode",postcode);
					String providerName = "SystemDatabase";
					Provider p = Providers.getProvider(providerName);
					if (p == null) {
						throw new ServiceException("The provider '" + providerName + "' does not exist.");
					} else if (!p.isProviderOfType("database")) {
						throw new ServiceException("The provider '" + providerName
								+ "' is not a valid provider of type 'database'.");
					}
					Workspace ws = (Workspace)p.getProvider();
					SystemUtils.trace("hays_search", "Before executing Query");
					ResultSet duplicateOffice = ws.createResultSet("QGetOffice", param);
					SystemUtils.trace("hays_search", "ResultSet duplicateOffice: " + duplicateOffice.getNumFields());
					if(duplicateOffice != null && duplicateOffice.first()) {
						//while(duplicateOffice.next())
						
							String ddocName=duplicateOffice.getStringValueByName("dDocName");
							SystemUtils.trace("hays_search","dDocName in the result set"+ddocName);
							if(contentId.equalsIgnoreCase(ddocName))
							{
								SystemUtils.trace("hays_search", "Document updation");
								return false;
							}
							else{
								m_binder.putLocal("isDuplicate", "true");
								SystemUtils.trace("hays_search", "Duplicate entries exist in the database");
								
								return true;
							}
						
					
					} 
					else
					{
						m_binder.putLocal("isDuplicate", "false");
						return false;
					}
					
				
	}
			catch(Exception ex) {
				SystemUtils.trace("hays_search", "Exception Occurred");
				ex.getStackTrace();
			}
		}
		return false;
	}
	
	private void deleteSpecialismFromOfficeTable()
	{
		  String dDocType = m_binder.getLocal("dDocType");
		  String officeId= m_binder.getLocal("dDocName");
		  DataBinder param = new DataBinder();
		 
		  SystemUtils.trace("hays_search", "Content Type is : ="+dDocType);
			
			if (dDocType != null && (dDocType.equalsIgnoreCase("Office")||dDocType.equalsIgnoreCase("Office-UK")||dDocType.equalsIgnoreCase("Office-Belgium")) && officeId != null && !("".equalsIgnoreCase(officeId))) //Execute only during an updation 
					{
				try {
					    
					    param.putLocal("officeId",officeId );
						String flag = "";
						String officeSpecialismIds= m_binder.getLocal("xOfficeSpecialismId");
						SystemUtils.trace("hays_search", "xOfficeSpecialismId: " + officeSpecialismIds);
						String specialisms[] = officeSpecialismIds.split(";");
						SystemUtils.trace("hays_search", "specialisms , length: " + specialisms[0]+","+specialisms.length);
						String providerName = "SystemDatabase";
						Provider p = Providers.getProvider(providerName);
						if (p == null) {
							throw new ServiceException("The provider '" + providerName + "' does not exist.");
						} else if (!p.isProviderOfType("database")) {
							throw new ServiceException("The provider '" + providerName
									+ "' is not a valid provider of type 'database'.");
						}
						Workspace ws = (Workspace)p.getProvider();
						ResultSet SpecialismIds = ws.createResultSet("QGetSpecialismIdForOffice", param);
						
						//int num = SpecialismIds.;
						DataResultSet result = null;
						if(SpecialismIds!=null && SpecialismIds.first())
						{	
						SystemUtils.trace("hays_search", "ResultSet SpecialismIds exist " );
						result = new DataResultSet();
						result.copy(SpecialismIds);
						int num = result.getNumRows();
												
						SystemUtils.trace("hays_search", "Number of Rows in the result set"+num);
						while(num>0) {
							SystemUtils.trace("hays_search", "Current Row is"+result.getCurrentRow());	
							String s=	result.getStringValueByName("SPECIALISMID");
							SystemUtils.trace("hays_search", "Specialism id from the result set: " + s);
							 for(int i = 0 ; i< specialisms.length ; i++ )
							 {
								 if(!s.equalsIgnoreCase(specialisms[i]))
								 {
									 SystemUtils.trace("hays_search","Specialism[i]"+specialisms[i]);
									 continue;
									 
								 }
								 else
								 {
									 s = "";
									 break;
								 }
								 
							 }
							 if(s.length()>0)
							 {
								 s = "'" + s + "'," ;
							 } 
							 flag = flag + s ;
							 SystemUtils.trace("hays_search","flag value is"+ flag + flag.trim().length());
							 /*if(flag.trim().length()!=0)
							 {		 
							  flag = flag + "," ;
							 }*/
							 SystemUtils.trace("hays_search","flag value is after appending comma"+ flag);
							         
							        num--;
							        result.next();
						}
						if(flag.length()>0)
				         {
							if( flag.endsWith(",")){
								flag = flag.substring(0, flag.length()-1);
							}
				            
				            SystemUtils.trace("hays_search","todeletespecialism"+flag.substring(1, flag.length()-1));
				            param.putLocal("specialismId", flag.substring(1, flag.length()-1));
						 	ws.execute("DeleteLinkingEntry1", param);
				         
				         }
						SystemUtils.trace("hays_search","Outside while");
				}	
			}
							
										
					
				  
					  
					   
				    
									
				 
			
			catch(Exception ex) {
				SystemUtils.trace("hays_search","Exception occured");
				ex.printStackTrace();
			}
			SystemUtils.trace("hays_search", "Final Registered Date: " + m_binder.getLocal("xEventDate"));
		}
		
						
						
		
	}
		

	@SuppressWarnings("null")
	private void populateSpecialism() throws ServiceException, DataException {
		SystemUtils.trace("hays_ontology",  "Inside the PopulateSpecialism Method" );

		String category=m_binder.getLocal("xCategory");
		String officecategory = m_binder.getLocal("xOfficeCategory");
		String country=m_binder.getLocal("xCountry");  //For offices
		String locale=m_binder.getLocal("xLocale");
		String languageCode = "";
		if((locale ==null  || locale.length()==0 )&& (country ==null  || country.length()==0 )){
			return; //no need to process specialism in this case
		}
		if(locale !=null)
		{
		 
		 String []temp1= locale.split("-");
		 languageCode= temp1[0];
		}
		else
		{ 
			
			String []temp1 = country.split("-");
			languageCode = temp1[0];
		}
		StringBuffer specialisms=new StringBuffer();
		
		
		
		if(category != null && category.length() != 0)
		{
			String[] temp = category.split(";");
			
			for(int i=1;i<temp.length;i++)
			{
				SystemUtils.trace("hays_ontology",  "The category Array :" +temp[i] );
				
				SystemUtils.trace("hays_ontology",  "LanguageCode" +languageCode );		
			
			//try{ 
			Boolean ontClass=Converter.isSpecialismOntClass(temp[i],OntologyFacade.getOntology("xCategory"),languageCode);
			SystemUtils.trace("hays_ontology",  "Ontology Class111" +ontClass );	
				if(ontClass!=null)
				{	
				SystemUtils.trace("hays_ontology",  "Ontology Class" +ontClass );
				//int index=Ontclass.indexOf("Specialism");
				if(!(ontClass).booleanValue())
				{
					String parentTerm=ontGetRelatedTerms(temp[i], "xCategory@hays:ParentTerm@true");
					SystemUtils.trace("hays_ontology",  "Parent Term" +parentTerm );
					parentTerm=parentTerm.substring(1, parentTerm.length()-1);
					int indexSpec=specialisms.indexOf(parentTerm+";");
					SystemUtils.trace("hays_ontology",  "INDEX Value1" +indexSpec );
					if(indexSpec< 0)
					{
					specialisms.append(parentTerm);
					specialisms.append(";");
					
					}
				}
				else
				{
					int indexSpec=specialisms.indexOf(temp[i]+";");
					SystemUtils.trace("hays_ontology",  "INDEX Value2" +indexSpec );
					if(indexSpec< 0)
					{	
					specialisms.append(temp[i]);
					specialisms.append(";");
					}
				}
				
				m_binder.putLocal("xSpecialismId", specialisms.toString());
				
				
				}
		}
		}
		
		
		if(officecategory != null && officecategory.length() != 0)
		{
			String[] temp = officecategory.split(";");
			
			for(int i=1;i<temp.length;i++)
			{
				SystemUtils.trace("hays_ontology",  "The category Array :" +temp[i] );
				
				SystemUtils.trace("hays_ontology",  "LanguageCode" +languageCode );		
			
			//try{ 
			Boolean ontClass=Converter.isSpecialismOntClass(temp[i],OntologyFacade.getOntology("xCategory"),languageCode);
			SystemUtils.trace("hays_ontology",  "Ontology Class111" +ontClass );	
				if(ontClass!=null)
				{	
				SystemUtils.trace("hays_ontology",  "Ontology Class" +ontClass );
				//int index=Ontclass.indexOf("Specialism");
				if(!(ontClass).booleanValue())
				{
					String parentTerm=ontGetRelatedTerms(temp[i], "xCategory@hays:ParentTerm@true");
					SystemUtils.trace("hays_ontology",  "Parent Term" +parentTerm );
					parentTerm=parentTerm.substring(1, parentTerm.length()-1);
					int indexSpec=specialisms.indexOf(parentTerm+";");
					SystemUtils.trace("hays_ontology",  "INDEX Value1" +indexSpec );
					if(indexSpec< 0)
					{
					specialisms.append(parentTerm);
					specialisms.append(";");
					
					}
				}
				else
				{
					int indexSpec=specialisms.indexOf(temp[i]+";");
					SystemUtils.trace("hays_ontology",  "INDEX Value2" +indexSpec );
					if(indexSpec< 0)
					{	
					specialisms.append(temp[i]);
					specialisms.append(";");
					}
				}
				
				m_binder.putLocal("xOfficeSpecialismId", specialisms.toString());
				
				}
		}
		}
	}
		
		
	

	/**
	 * Sets WebSection and WebSites metadata for Jobs & candidate records
	 */
	private void setMetadata() throws DataException{
		String section, website = null;
		String dDocType = m_binder.getLocal("dDocType");
		section = m_binder.getLocal("xWebsiteSection");
		website = m_binder.getLocal("xWebsites");
		//System.out.println("Updated metadata: " + dDocType + ", " + section);
		if (dDocType != null && (section == null || section.length() == 0)){
			try {
				int index = DEFAULT_METADATA_CONF.indexOf(dDocType);
				if( index < 0)
					return;
				section = DEFAULT_METADATA_CONF.substring(index + dDocType.length()+1);
				index = section.indexOf(",");
				if( index > 0) {
					section = section.substring(0, index);
				}
				// extract website ID
				index = section.indexOf(":");
				if( index > 0 ) {
					website = section.substring(0, index);
				} 
				
		//		System.out.println("Updated metadata: retrieved section " + section + ", " + website);
				// update doc info
				if( section != null ) {
					m_binder.putLocal("xWebsiteSection", section);
					m_binder.putLocal("xWebsites", website);
				}
			}catch(Exception ex) {
				ex.getStackTrace();
			}
		}
	}
	
	
	/**
	 * Extracts info from passed XML to calculate Min and Max payment, bring them to 
	 * unified level and populate xMinSalary, xMaxSalary and xJobType metadata fields
	 * 
	 * @throws DataException
	 */
	/*private void populateSalaryRange_old() throws DataException {
		String salaryType = m_binder.getLocal("xSalaryType");
		if( salaryType != null && salaryType.length() > 0) {
			Matcher matcher = null;
			String jobType, minSalary, maxSalary, paymentType = null;
			StringBuffer type = new StringBuffer(";");
			double min = 10000000;
			double max = -1;
			double temp = -1;
			try {
				String[] array = salaryType.split("<.payrange>");
				for(int i = 0; i < array.length; i++) {
					matcher = SALLARY_TYPE_REGEX.matcher(array[i]);
					if( matcher.find()) {
						jobType = matcher.group(1);
						type.append(jobType).append(";");
						minSalary = matcher.group(2);
						maxSalary = matcher.group(3);
						paymentType = matcher.group(4);
						
						if(minSalary != null && minSalary.length() > 0){
							temp = calculateUnifiedSalary(minSalary, paymentType);
							if( temp < min) {
								min = temp;
							}
						}
						
						if(maxSalary != null && maxSalary.length() > 0){
							temp = calculateUnifiedSalary(maxSalary, paymentType);
							if( temp > max) {
								max = temp;
							}
						}
					}
				}
				if( min <= max) {
					
					double average = (max+min)/2;
					
					java.math.BigDecimal minsalary = new java.math.BigDecimal( min );
					java.math.BigDecimal maxsalary = new java.math.BigDecimal( max );
					java.math.BigDecimal averagesalary = new java.math.BigDecimal( average );
					
					m_binder.putLocal("xMinSalary", String.valueOf(min));
					m_binder.putLocal("xMaxSalary", String.valueOf(max));
					m_binder.putLocal("xAverageSalary", String.valueOf((max+min)/2));
				}
				
				if( type.length() > 1) {
					m_binder.putLocal("xJobType", type.toString());
				}
			} catch(Exception ex) {
				throw new DataException("Salary range parsing failed: " + ex);
			}
		}
	}*/
	
	
	
	
	/**
	 * Extracts info from passed XML to calculate Min and Max payment, bring them to 
	 * unified level and populate xMinSalary, xMaxSalary and xJobType metadata fields
	 * 
	 * @throws DataException
	 */
	private void populateSalaryRange() throws DataException {
		String salaryType = m_binder.getLocal("xSalaryType");		
		String siteId = m_binder.getLocal("xWebsiteSection");
		HaysWebSite website=null;
		if(siteId !=null && siteId.length()>0)
		{
			String[] siteId_arry=siteId.split(":");
			siteId=siteId_arry[0];
			SystemUtils.trace("hays_search", "Site ID=" +siteId);
			HashMap<String, HaysWebSite> websitesMap = (HashMap<String, HaysWebSite>)SharedObjects.getObject("Multiling", "WebsitesMap");
	        website = websitesMap.get(siteId);
		}		
		@SuppressWarnings("unchecked")		
        String jobtype_permanent = null;
        String jobtype_temporary = null;
        String jobtype_contract = null;
        if( salaryType != null && salaryType.length() > 0)        
	        {        	
	        if(website != null)
	        {
	        	jobtype_permanent= website.jobtype_permanent;
	        	jobtype_temporary= website.jobtype_temporary;
	        	jobtype_contract= website.jobtype_contract;
	        
	        SystemUtils.trace("hays_search", "Correct JobTypes: Permanent=" +jobtype_permanent +",Temporary="+jobtype_temporary+",Contract="+jobtype_contract);
			
			
				Matcher matcher = null;
				String jobType, minSalary, maxSalary, paymentType,targetPayType = null;
				StringBuffer type = new StringBuffer(";");
				double min = 10000000;
				double max = -1;
				double temp = -1;
				double minmaxTemp=-1;                  
				double minSal = 10000000;
				double maxSal = -1;
				NumberFormat f = NumberFormat.getInstance();
				f.setGroupingUsed(false);
				f.setMaximumFractionDigits(5);  // For seting the maximum places after decimal to 5
				
				String salType=null;
				try {
					String[] array = salaryType.split("<.payrange>");
					for(int i = 0; i < array.length; i++) {
						min = 10000000;
						max = -1;
						temp = -1;
						minmaxTemp=-1;                  
						minSal = 10000000;
						maxSal = -1;
						matcher = SALLARY_TYPE_REGEX.matcher(array[i]);
						if( matcher.find()) {
							jobType = matcher.group(1);
							type.append(jobType).append(";");
							minSalary = matcher.group(2);
							maxSalary = matcher.group(3);
							paymentType = matcher.group(4);
							
							
								if (jobType.equalsIgnoreCase("P")){
									targetPayType=jobtype_permanent;
								}
								else if (jobType.equalsIgnoreCase("T") || jobType.equalsIgnoreCase("TE")){
									targetPayType=jobtype_temporary;
								}
								else{
									targetPayType=jobtype_contract;
								}
									
								
								SystemUtils.trace("hays_search", "Target Patype:"+targetPayType );
								SystemUtils.trace("hays_search", "JobType:"+jobType );
								SystemUtils.trace("hays_search", "Min Salary:"+minSalary );
								SystemUtils.trace("hays_search", "Max Salary:"+maxSalary );
								SystemUtils.trace("hays_search", "Pay Type:"+paymentType );
									
								if(minSalary != null && minSalary.length() > 0){
									salType="min";
									temp = calculateUnifiedSalaryForFilter(minSalary, paymentType, targetPayType, salType);
									minmaxTemp = calculateUnifiedSalary(minSalary, paymentType);
									if( temp < min) {
										min = temp;								    
									}
									if( minmaxTemp < minSal) {
										minSal = minmaxTemp;								    
									}
								}
								
								if(maxSalary != null && maxSalary.length() > 0){
									salType="max";
									temp = calculateUnifiedSalaryForFilter(maxSalary, paymentType, targetPayType, salType);
									minmaxTemp = calculateUnifiedSalary(maxSalary, paymentType);
									if( temp > max) {
										max = temp;								   	
									}
									if( minmaxTemp > maxSal) {
										maxSal = minmaxTemp;								    
									}
								}
								SystemUtils.trace("hays_search", "Document Name: " +m_binder.getLocal("dDocName") +",Document Title="+m_binder.getLocal("dDocTitle"));
								SystemUtils.trace("hays_search", "Min-Max Salary: Min=" +min +",Max="+max); 								
								//SystemUtils.trace("hays_search", "minsalary big decimal value is"+minsalary);
								//maxsalary = new java.math.BigDecimal( max );
								
							    if (min<=max)
							    {
								    	if (jobType.equalsIgnoreCase("P"))
									    {
										     m_binder.putLocal("xPermMin", f.format(new Double(min)));
										     m_binder.putLocal("xPermMax",f.format(new Double(max)) );
									    }					    
									    else if (jobType.equalsIgnoreCase("T") || jobType.equalsIgnoreCase("TE"))
									    {
										     m_binder.putLocal("xTempMin", f.format(new Double(min)));
										     m_binder.putLocal("xTempMax", f.format(new Double(max)) );
									    }					    		    
									    else if (jobType.equalsIgnoreCase("C"))
										{
											 m_binder.putLocal("xContMin", f.format(new Double(min)) );	
										     m_binder.putLocal("xContMax", f.format(new Double(max)) );	
										}	
							    }				    
						  
						}
					}
					
					SystemUtils.trace("hays_search", "Permanent JobType: Min=" +m_binder.getLocal("xPermMin") +",Max="+m_binder.getLocal("xPermMax"));
					SystemUtils.trace("hays_search", "Temporary JobType: Min=" +m_binder.getLocal("xTempMin") +",Max="+m_binder.getLocal("xTempMax"));
					SystemUtils.trace("hays_search", "Contract JobType: Min=" +m_binder.getLocal("xContMin") +",Max="+m_binder.getLocal("xContMax"));
					
					if( minSal <= maxSal) {
						double average = minSal+maxSal/2;
						SystemUtils.trace("hays_search", "MinSalary less than Max Salary:Min=" +minSal+",Max="+maxSal);
						//minsalary = new java.math.BigDecimal( minSal ).setScale(5, RoundingMode.HALF_EVEN).stripTrailingZeros();
						//maxsalary = new java.math.BigDecimal( maxSal ).setScale(5, RoundingMode.HALF_EVEN).stripTrailingZeros();						
						//java.math.BigDecimal averagesalary = new java.math.BigDecimal( average ).setScale(5, RoundingMode.HALF_EVEN).stripTrailingZeros();
						
						
						m_binder.putLocal("xMinSalary", f.format(new Double(minSal)) );	
						m_binder.putLocal("xMaxSalary", f.format(new Double(maxSal)) );	
						m_binder.putLocal("xAverageSalary", f.format(new Double(average)) );	
						SystemUtils.trace("hays_search", "Min is =" +m_binder.getLocal("xMinSalary")+",Max is ="+m_binder.getLocal("xMaxSalary")+"average is :"+m_binder.getLocal("xAverageSalary"));
						/*m_binder.putLocal("xMinSalary", String.valueOf(minSal));
						m_binder.putLocal("xMaxSalary", String.valueOf(maxSal));
						m_binder.putLocal("xAverageSalary", String.valueOf((maxSal+minSal)/2));					
						*/
					}
					
					if( type.length() > 1) {
						m_binder.putLocal("xJobType", type.toString());
					}
				}catch(Exception ex) {
					throw new DataException("Salary range parsing failed: " + ex);
				}
			 }
		  } 	
  }
	
	
	
	/**
	 * Using passed locale to populate the currency symbol
	 */
	private void populateLocale() {
		String locale = m_binder.getLocal("xLocale");
		String currencySymbol = DEFALT_CURRENCY_SYMBOL;
		try {
			if(locale != null && locale.length() > 0) {
				Locale aLocale = new Locale(locale);
				currencySymbol = Currency.getInstance(aLocale).getSymbol();
			}
		} catch(Exception ex) {
			currencySymbol = DEFALT_CURRENCY_SYMBOL;
		}
		m_binder.putLocal("xSalaryCurrency", currencySymbol);
	}
	
	static public double calculateUnifiedSalary(String salary, String payType) throws NumberFormatException, DataException {
		double asalary = Math.abs( Double.parseDouble(salary) );
		double unified = -1;
		if (payType.equalsIgnoreCase("H") ) {
			unified = asalary * 2080;		
		}
		else if(payType.equalsIgnoreCase("D") ) {
			unified = asalary * 260;
		}
		else if( payType.equalsIgnoreCase("W") ) {
			unified = asalary * 52;
		}
		else if( payType.equalsIgnoreCase("M")) {
			unified = asalary * 12;		
		}
		else if (payType.equalsIgnoreCase("A")) {
			unified = asalary;
		}
		else throw new DataException("Payment type is incorrect: " + payType);
		return unified;
	}
	
	
   static public double calculateUnifiedSalaryForFilter(String salary, String payType, String targetPayType, String salType) throws NumberFormatException, DataException,ServiceException {
		
		double asalary = Math.abs( Double.parseDouble(salary) );
		double unified = -1;		
		char targetPaymentType=targetPayType.charAt(0);
		if (payType.equalsIgnoreCase(targetPayType))
			{
			  unified = asalary;
			  unified=calculateUnifiedSalary_roundoff(unified,targetPayType,salType);
			}		
	
		else
		{
			
			switch (targetPaymentType){
			
			case 'H' :	
				unified=calculateUnifiedSalary_Payment(payType,asalary);
				unified=unified /2080;				
				unified=calculateUnifiedSalary_roundoff(unified,targetPayType,salType);									
			    break;
			    
			   
			case 'D' :	
				unified=calculateUnifiedSalary_Payment(payType,asalary);
				unified=unified /260;					
				unified=calculateUnifiedSalary_roundoff(unified,targetPayType,salType);				
			    break;		
			    
			    
			case 'M' :		
				unified=calculateUnifiedSalary_Payment(payType,asalary);
				unified=unified /12;				
				unified=calculateUnifiedSalary_roundoff(unified,targetPayType,salType);				
			    break;
			    
			    
			case 'A' : 			
				unified=calculateUnifiedSalary_Payment(payType,asalary);					
				unified=calculateUnifiedSalary_roundoff(unified,targetPayType,salType);				
			    break;
			
		 }
			
		}		
		return unified;
	}
   
   
   static public double calculateUnifiedSalary_Payment(String payType,double asalary) throws NumberFormatException, DataException,ServiceException {

	   
	   double calculatedPayment = asalary;
	   char payment_Type=payType.charAt(0);
	   
	   switch(payment_Type){
	   
	   case 'H' :
		   calculatedPayment=calculatedPayment * 2080;
		   break;
		   
	   case 'D' :
		   calculatedPayment=calculatedPayment * 260;
		   break;		   
	  
	   case 'M' :
		   calculatedPayment=calculatedPayment * 12;
		   break;
		   
	   case 'A' :		   
		   break;	   
	   
	   }
	  
	   return calculatedPayment;
	   
	   
   }
  
    static public double calculateUnifiedSalary_roundoff(double unified,String targetPayType, String salType) throws NumberFormatException, DataException,ServiceException {
	if(salType.equalsIgnoreCase("min"))
	   {
		return calculateFloor(unified, salaryTYpeConversionRateMap.get(targetPayType).intValue());
	   }
	else{
		return calculateCeil(unified, salaryTYpeConversionRateMap.get(targetPayType).intValue());
	   }
    }

	private static double calculateFloor(double salary, int conversionFactor){
		double unifiedsalary=salary/conversionFactor;
		unifiedsalary=(Math.floor(unifiedsalary))*conversionFactor;
		return unifiedsalary;
	}

	private static double calculateCeil(double salary, int conversionFactor){
		double unifiedsalary=salary/conversionFactor;
		unifiedsalary=(Math.ceil(unifiedsalary))*conversionFactor;
		return unifiedsalary;
	}
	

	
	private void populateLocations()  {
		String location = m_binder.getLocal("xLocation");
		if( location != null && location.length() > 0  ) {
			location = location.replaceAll(";", ",");
			if( location.startsWith(",")){
				location = location.substring(1);
			}
			if( location.endsWith(",")){
				location = location.substring(0, location.length()-1);
			}
			// remove existing locations
			clearLocations();
			
			SystemUtils.trace("hays_search", "populate Locations: " + location);
			SystemUtils.trace("hays_search", "Binder: " + m_binder.getLocalData());
			
			DataBinder param = new DataBinder();
			param.putLocal("tblName", LOCATIONS_TBL);
			param.putLocal("locationId", location);
			try {
				Workspace ws = getProviderConnection();
				ResultSet locations = ws.createResultSet("QparentLocations", param);
				SystemUtils.trace("hays_search", "Result locations: " + locations);
				String level, aLocation, temp = null;
				if(locations != null && locations.first()) {
					do {
						level = locations.getStringValueByName("ZOOM");
						aLocation = locations.getStringValueByName("COORDINATE");
						SystemUtils.trace("hays_search", "level: " + level + ", location = " + aLocation);
						temp = m_binder.getLocal(LOCATION_METADATA + level);
						if( temp != null) {
							if( temp.indexOf(aLocation) < 0)
								temp = temp + aLocation  + ";";
						} else {
							temp = ";" + aLocation + ";";
						}
						this.m_binder.putLocal((LOCATION_METADATA + level), temp);
						SystemUtils.trace("hays_search", "Populate locations: " + level + ", " + temp);
					} while (locations.next());
				}
				if(ws != null){
					ws.releaseConnection();
				}
			} catch(Exception ex) {
				SystemUtils.trace("hays_search", "\nFailed to retrieve locations for zooming: "+ ex);
				ex.getStackTrace();
			}
			SystemUtils.trace("hays_search", "databinder after locations are processed: " + m_binder.getLocalData());
		}		
	 }
	
	
	private void clearLocations() {
		for(int i = HaysSearchLocationsHandler.BOTTOM_LEVEL; i >= HaysSearchLocationsHandler.TOP_LEVEL; i--){
			m_binder.putLocal(LOCATION_METADATA + i,"");
			SystemUtils.trace("hays_search",LOCATION_METADATA+i+m_binder.getLocal(LOCATION_METADATA + i) );
		}
	}

	private Workspace getProviderConnection() throws ServiceException, DataException {
		String providerName = ONT_PROVIDER;
		
		SystemUtils.trace("hays_search", "provider name to be used =" + providerName);
		// validate the provider name
		if (providerName == null || providerName.length() == 0) {
			throw new ServiceException("You must specify a provider name.");
		}
		// validate that the provider is a valid database provider
		Provider p = Providers.getProvider(providerName);
		if (p == null) {
			throw new ServiceException("The provider '" + providerName + "' does not exist.");
		} else if (!p.isProviderOfType("database")) {
			throw new ServiceException("The provider '" + providerName
					+ "' is not a valid provider of type 'database'.");
		}

		Workspace ws = (Workspace) p.getProvider();
		
		return ws;
	}

	
	public static void main(String[] args) throws Exception {
		String salaryType = "<payrange><jobtype>P</jobtype><min></min><max></max><paytype></paytype></payrange>";//"<payrange><jobtype>TE</jobtype><min>50000</min><max>60000</max><paytype>A</paytype></payrange><payrange><jobtype>C</jobtype><min>300</min><max>500</max><paytype>D</paytype></payrange>";
		
		if( salaryType != null && salaryType.length() > 0) {
			Matcher matcher = null;
			String jobType, minSalary, maxSalary, paymentType = null;
			StringBuffer type = new StringBuffer(";");
			double min = 10000000;
			double max = -1;
			double temp = -1;
			try {
				String[] array = salaryType.split("<.payrange>");
				for(int i = 0; i < array.length; i++) {
					matcher = SALLARY_TYPE_REGEX.matcher(array[i]);
					if( matcher.find()) {
						jobType = matcher.group(1);
						type.append(jobType).append(";");
						minSalary = matcher.group(2);
						maxSalary = matcher.group(3);
						paymentType = matcher.group(4);
						
						if(minSalary != null && minSalary.length() > 0){
							//temp = calculateUnifiedSalary(minSalary, paymentType);
							if( temp < min) {
								min = temp;
							}
						}
						
						if(maxSalary != null && maxSalary.length() > 0){
							//temp = calculateUnifiedSalary(maxSalary, paymentType);
							if( temp > max) {
								max = temp;
							}
						}
					}
				}
				if( min <= max) {
					System.out.println("xMinSalary"+ String.valueOf(min));
					System.out.println("xMaxSalary"+ String.valueOf(max));
					System.out.println("xAverageSalary"+ String.valueOf((max+min)/2));
				}
				
				if( type.length() > 1) {
					System.out.println("xJobType"+ type.toString());
				}
			} catch(Exception ex) {
				throw new DataException("Salary range parsing failed: " + ex);
			}
		}	
	}
	
	
	/**
	 * Sets contents first Revision Releasedate in MetaDataField: xRegisteredDate
	 */
	private void populateRegisteredDate(){
		String dDocType = m_binder.getLocal("dDocType");
		String RegisteredDate = m_binder.getLocal("xEventDate");
		String ReleaseDate= m_binder.getLocal("dInDate");
		Calendar currentDate = Calendar.getInstance();
		SimpleDateFormat formatter= new SimpleDateFormat("dd/MM/yy HH:mm:ss a");		
		String currentRevisionDate=formatter.format(currentDate.getTime());		
		int Revision = Integer.parseInt(m_binder.getLocal("dRevLabel"));		
		DataBinder param = new DataBinder();
		SystemUtils.trace("hays_search", "Document Revision: " + Revision);
		SystemUtils.trace("hays_search", "Document Current Revision Date: " + currentRevisionDate);
		SystemUtils.trace("hays_search", "Document Type: " + dDocType);
		SystemUtils.trace("hays_search", "Registered Date: " + RegisteredDate);
		
		if (dDocType != null && (dDocType.equalsIgnoreCase("Jobs") || dDocType.equalsIgnoreCase("Candidates")) && (RegisteredDate == null || RegisteredDate.length() == 0)){
			
				try {
					if(Revision>1){
						
						String dDocName= m_binder.getLocal("dDocName");
						SystemUtils.trace("hays_search", "Document Name: " + dDocName);
						param.putLocal("dDocName", dDocName);
						String providerName = "SystemDatabase";
						Provider p = Providers.getProvider(providerName);
						if (p == null) {
							throw new ServiceException("The provider '" + providerName + "' does not exist.");
						} else if (!p.isProviderOfType("database")) {
							throw new ServiceException("The provider '" + providerName
									+ "' is not a valid provider of type 'database'.");
						}
						Workspace ws = (Workspace)p.getProvider();
						ResultSet docFirstRevision = ws.createResultSet("QGetFirstRevision", param);
						SystemUtils.trace("hays_search", "ResultSet Doc_First_revision: " + docFirstRevision);
						if(docFirstRevision != null && docFirstRevision.first()) {
						String createDate=	docFirstRevision.getStringValueByName("dInDate");
						SystemUtils.trace("hays_search", "Create Date: " + createDate);
						m_binder.putLocal("xEventDate", createDate);	
						}
					}
					
				   else
				    {
					   if (!(" ".equalsIgnoreCase(ReleaseDate)) && ReleaseDate !=null)
					   {
						   SystemUtils.trace("hays_search", "Current Release Date: " + ReleaseDate);
						   m_binder.putLocal("xEventDate", ReleaseDate);
					   }
					   else
					   {
						   SystemUtils.trace("hays_search", "Current Create Date: " + currentRevisionDate);
						   m_binder.putLocal("xEventDate", currentRevisionDate);
					   }	  
					   
				    }
									
				  }
			
			catch(Exception ex) {
				ex.getStackTrace();
			}
			SystemUtils.trace("hays_search", "Final Registered Date: " + m_binder.getLocal("xEventDate"));
		}
		
	}
	
//////////////added for r7 start///////////////////////////////////////////////////////
	
	String ontGetRelatedTerms(String sArg1,String sArg2) throws ServiceException
	{
		SystemUtils.trace("hays_search", "inside ontGetRelatedTerms, sArg1::::=" + sArg1+" ,sArg2:"+sArg2);
		String result ="";
		String terms = sArg1.trim();
		String meta_relation = sArg2;
		String isObject = "false";
		if( terms != null && meta_relation != null) 
		{
		    int index = meta_relation.indexOf("@");
		    if( index > 0)
		    {
			    String metadata = meta_relation.substring(0, index);
			    String relation = meta_relation.substring(index + 1);
			    index = relation.indexOf("@");
			    if( index > 0 )
			    {
				    isObject = relation.substring(index+1 );
				    relation = relation.substring(0, index );
			    }
			     Properties properties = new Properties();
			    properties.put("ont_metadata", metadata);
			    properties.put("relation", relation);
			    properties.put("isObject", isObject);
			    properties.put("terms", terms);
			    SystemUtils.trace("hays_search", "before calling  callServiceHandlerSpecialism, metadata:" + metadata+" ,relation:"+relation+" ,isObject:"+isObject+" ,terms:"+terms);
			    result = callServiceHandlerSpecialism((Service)m_ctx, "getRelatedTerms", properties, relation+metadata);
			    SystemUtils.trace("hays_search", "result after::::='" + result);
			   
		    }
		}
		 return result;
	}
	
	
	
	String callServiceHandlerSpecialism(Service service, String s, Properties properties, String s1) throws ServiceException {
        String resultStr = null;
        DataBinder databinder;
        Properties properties1;
        databinder = service.getBinder();
        if (databinder == null) {
            return null;
        }
        properties1 = databinder.getLocalData();
        databinder.setLocalData(properties);
        OntologyNavigationHandler ssservicehandler = (OntologyNavigationHandler)service.getHandler("infomentum.ontology.navigation.OntologyNavigationHandler");
        if (ssservicehandler != null) {
            ClassHelper classhelper = new ClassHelper();
            classhelper.m_class = ssservicehandler.getClass();
            classhelper.m_obj = ssservicehandler;
            classhelper.invoke(s);
            resultStr = databinder.getLocal(s1);
        }
        databinder.setLocalData(properties1);
        return resultStr;
        
	}
	
	
	
	
}
