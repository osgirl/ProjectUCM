package hays.co.uk.search; 

import hays.co.uk.MetadataSetFilter;
import infomentum.ontology.navigation.OntologyNavigationHandler;
import intradoc.common.ClassHelper;
import intradoc.common.LocaleUtils;
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
import intradoc.server.ServiceHandler;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Properties;



public class HaysApiSearchHandler extends ServiceHandler {
	
//public static final char[] specialCharacters = {']', '[','\\', '^', '$', ',', '.', '|', '?', '*', '+', '(', ')', '{', '}'};
	public final static String TRACE_NAME = "webapi_search";
	public void processApiSearchParameters() throws DataException, ServiceException {
		super.m_binder.putLocal("SearchQueryFormat", "universal");
		
		// EXCLUDE
		String exclude = getSuperData(IHaysSearchConstants.EXCLUDE);
		// QUERY
		StringBuffer query = new StringBuffer();
		StringBuffer queryPartforSearchPOC=new StringBuffer();
		StringBuffer queryPart = new StringBuffer();
		String level = getSuperData(IHaysSearchConstants.LEVEL);
		
		
		if( "Hays".equals(getSuperData("searchType"))) {
			SystemUtils.trace(TRACE_NAME, "\n\nStarting search process: " + m_binder.getLocalData());
			SystemUtils.trace(TRACE_NAME, "\nExclude : " + exclude);
	        // KEYWORDS
	        String keywords = getSuperData(IHaysSearchConstants.JOB_KEYWORDS);	        
	        super.m_binder.putLocal(IHaysSearchConstants.IS_FUZZY_SEARCH,"true");// Fuzzy true for all countries(set true for alert requirement)
	        String isFuzzyStr = getSuperData(IHaysSearchConstants.IS_FUZZY_SEARCH);
	        String isOnlyJobTitleStr = getSuperData(IHaysSearchConstants.IS_ONLY_JOB_TITLE);
			SystemUtils.trace(TRACE_NAME, "\nisFuzzyStr : " + isFuzzyStr+" \n isOnlyJobTitle : " + isOnlyJobTitleStr);
			String thes_locale = getData(IHaysSearchConstants.LOCALE);    
			SystemUtils.trace("hays_search", "thes_locale"+thes_locale);			
			String thes_name = thes_locale.split("-")[1];
			SystemUtils.trace("hays_search", "Thesaurus Name: " + thes_name);	
			boolean isFuzzy = (isFuzzyStr!=null &&  "true".equals(isFuzzyStr))?true:false;
        	boolean isOnlyJobTitle = (isOnlyJobTitleStr!=null &&  "Y".equals(isOnlyJobTitleStr))?true:false;
			
	        //Start POC Builder
	        if( keywords != null) {
	        		        	
		        QueryBuilderFactory queryBuilderFactory = QueryBuilderFactory.getInstance(); 
		        AbstractHaysQueryBuilder abstractHaysQueryBuilder = queryBuilderFactory.getQueryBuilder(isFuzzy, isOnlyJobTitle, thes_name,"",level,null,false,null); 
		        abstractHaysQueryBuilder.setInputString(keywords);
		        abstractHaysQueryBuilder.buildQueryPart();
		        queryPart = abstractHaysQueryBuilder.getQueryPart();
		     
		        SystemUtils.trace(TRACE_NAME, "Trace queryPart : " + queryPart.toString()+"isOnlyJobTitleStr::"+isOnlyJobTitleStr);
		        		        
		        
	        }
	       
	        String ref = getSuperData(IHaysSearchConstants.JOB_TITLE_REF);
	        query.append("((( ");
	        if( queryPart.length() > 0 )
	        {
	        	if( query.length() > 0)
        		{
        			query.append(" (idccontenttrue)*10*10 AND ");
        		}
	        query.append("(").append(queryPart); 
	        
		        	 if(ref == null || "".equals(ref)){
		        		 query.append(")");
		        	 }
	        }
	        else
        	{
        	query.append("(idccontenttrue)*10*10");
        	
        	}

	       
		
			//JOB REFERENCE
			
			SystemUtils.trace(TRACE_NAME, "JOb Title Reference"+ref);				
	        if( ref != null  && ref.length() > 0 ) {
	        	ref = ref.replaceAll(IHaysSearchConstants.specialCharacters, " ");
	        	ref = ref.trim();
	        	ref = QueryUtils.decodeHaysSpecialKeywords(ref);
	        	ref = QueryUtils.formatStringForReservedKeyWords(ref);
	        	SystemUtils.trace(TRACE_NAME, "ref length"+ref.length());
	            SystemUtils.trace(TRACE_NAME, "ref trimmed length"+ref.trim().length());
	        	
	        	if( ref.length() > 0)
	        	{
	        		ref = ref.replaceAll(IHaysSearchConstants.fuzzySpecialCharacters, " ");
	        		ref = ref.replaceAll("(?i)[&;!>]| AND ", " ");
					SystemUtils.trace(TRACE_NAME, "ref String Value"+ref);
					ref = ref.replaceAll("(?i)[|]| OR ", ",");
	        		ref = ref.replaceAll("\"", "");
	        		ref=ref.trim();
	        		ref = ref.replaceAll("[, ][ ][ ]*", " | ");
		        			        	
		        	query.append(" ACCUM (").append(ref).append(") WITHIN xRecordId)  ");
			        SystemUtils.trace(TRACE_NAME, "inside if  for home page , query::" +query);
		        		
			        	
		        	}
	        } 
	        
	        
	        SystemUtils.trace(TRACE_NAME, "processApiSearchParameters: Job Title='" + query + "'");
	        int index=query.indexOf("AND");
	        if(index>0)
	        {
	        queryPartforSearchPOC.append(query.substring(index)).append("*10*10");
	        }
	        SystemUtils.trace(TRACE_NAME, "QueryPartforSEarchPOC"+queryPartforSearchPOC);
	        
	     // INDUSTRY
	        String industriesStr = getSuperData(IHaysSearchConstants.JOB_INDUSTRY);
	        if( industriesStr == null) {
	        	StringBuffer industries = processCheckBoxes(IHaysSearchConstants.JOB_INDUSTRY); 
	        	if( industries.length() > 0) {
	        		industriesStr = industries.substring(1);	
	        	}
	        }
	     
	        if( industriesStr != null && industriesStr.trim().length() > 0 ) {
	        	if( industriesStr.endsWith(","))
	        		industriesStr = industriesStr.substring(0, industriesStr.length()-1);
	    		industriesStr = industriesStr.replaceAll(",[ ]?", " | ");
	    		if(exclude != null && exclude.contains(IHaysSearchConstants.JOB_INDUSTRY) ) {
	    			{
	    			query.append(" NOT ");
	    			queryPartforSearchPOC.append(" NOT ");
	    			}
	    			SystemUtils.trace(TRACE_NAME, "In  industries other than " + industriesStr);
	    		} else {        
		    		if( query.length() > 0)
		    		{
		    			query.append(" AND ");
		    			queryPartforSearchPOC.append(" AND ");

		    		}
		    	}
	    		query.append("((").append(industriesStr).append(") WITHIN xIndustry )*10*10"); //maxed out score
	        	queryPartforSearchPOC.append("((").append(industriesStr).append(") WITHIN xIndustry )*10*10"); 
		        SystemUtils.trace(TRACE_NAME, "processApiSearchParameters: Industry='" + query + "'");
	        }
	        
	        // CATEGORY
	        String categories = getSuperData(IHaysSearchConstants.JOB_CATEGORY);
	        if( categories != null && categories.trim().length() > 0 ) 
	        {
	        	     	
	        	// added for r7 start
	        	StringBuffer result = new StringBuffer();
	  
	        	    
		        	StringBuffer  finalsubTerms=new StringBuffer();
		        	SystemUtils.trace(TRACE_NAME, "categories 1st =" + categories);
		        	
		        	if( categories.startsWith(";"))
		        		categories = categories.substring(1);
		        	if( categories.endsWith(";"))
		        		categories = categories.substring(0, categories.length()-1);
		        	    categories = categories.replaceAll("[,;][ ]?", "#");
		        	
	        		 
	      
	        		 SystemUtils.trace(TRACE_NAME, "categories after replace all=" + categories);
	        		
	        		 
	        		 String [] categoriesArray = categories.split("#");
	        		 
	        		 for(int i=0;i<categoriesArray.length;i++)
	        		 {
	        		 
	        			 		String  subTerms = ontGetRelatedTerms(categoriesArray[i], "xCategory@hays:ParentTerm");
				        		 SystemUtils.trace(TRACE_NAME, "subTerms=" + subTerms);
				        		
				        		if("[]".equalsIgnoreCase(subTerms))
				        		{
				        			subTerms = categoriesArray[i];
				        		}
				        		 if(subTerms.length()>0)
				        		{ 
				        			subTerms = categoriesArray[i] + "," + subTerms.substring(1, subTerms.length()-1);
				        		}
				        		 SystemUtils.trace(TRACE_NAME, "for i=="+i+"  ,subTerms=" + subTerms);
				        		 if(i>0)
				        		 {
				        			 finalsubTerms.append(" | ");
				        		 }
				        		 finalsubTerms.append(subTerms);
				        		 SystemUtils.trace(TRACE_NAME, "for i=="+i+"  ,finalsubTerms=" + finalsubTerms.toString());		 
	        		 }
	        		 SystemUtils.trace(TRACE_NAME, "final  finalsubTerms=" + finalsubTerms.toString());	
	        		categories = finalsubTerms.toString();	
	        		categories = categories.replaceAll("[,;#][ ]?", " | ");	
	        		
	        	if( query.length() > 0) 
	    		{
	    			query.append(" AND ");
	    			queryPartforSearchPOC.append(" AND ");
	    		}
	        	query.append("((").append(categories).append(") WITHIN xCategory )*10*10");
	        	queryPartforSearchPOC.append("((").append(categories).append(") WITHIN xCategory )*10*10");
		        SystemUtils.trace(TRACE_NAME, "processApiSearchParameters: Category='" + query + "'");
	        	
	        }
	        
	      //POSTED DATE
	        
	        //added for R7 
	        String posteddate=getSuperData(IHaysSearchConstants.JOB_POSTED_DATE_FILTER);
	        SystemUtils.trace(TRACE_NAME, "JOB_POSTED_DATE_FILTER = ====" + posteddate );
	        if( posteddate != null && posteddate.trim().length() > 0 ) 
	        {
	        	int indexposted=posteddate.indexOf(',');
	        	SystemUtils.trace(TRACE_NAME, "Inside excluddde JOB_POSTED_DATE_FILTER = ====" + indexposted );
	        	if(indexposted == -1)
	        	{
	        		if( query.length() > 0)
	        		{
		    			query.append(" AND ");
		    			queryPartforSearchPOC.append(" AND ");
	        		}
		        	query.append(" (").append(" SDATA(dInDate > '").append(posteddate).append("'))*10*10");
		        	queryPartforSearchPOC.append(" (").append(" SDATA(dInDate > '").append(posteddate).append("'))*10*10");
	        	}
	        	else
	        	{
	        		posteddate=posteddate.substring(0,indexposted);
	        
	        		SystemUtils.trace(TRACE_NAME, "processApiSearchParameters: posteddateexclude ='"+posteddate);
	        		if( query.length() > 0)
	        		{
		    			query.append(" AND ");
		    			queryPartforSearchPOC.append(" AND ");
	        		}
			        query.append(" (").append(" SDATA(xEventDate > '").append(posteddate).append("'))");
			        queryPartforSearchPOC.append(" (").append(" SDATA(xEventDate > '").append(posteddate).append("'))");
	        		
	        	}
	        }
	        SystemUtils.trace(TRACE_NAME, "processApiSearchParameters: posteddateexclude ='"+posteddate+", " + query + "'");        
      
	        
	        //end Posted Date
    
	        // PERMANENT
	        StringBuffer salaryBuffer = new StringBuffer();
	        String type, min, max, permPayType, salary = null;
	        type = getSuperData(IHaysSearchConstants.JOB_PERM);
	        
	        SystemUtils.trace(TRACE_NAME, "value of type is : " + type);
	        if( type != null && !"".equalsIgnoreCase(type)) {
	        	if( type.length() > 0 ) {
	        		min = getSuperData(IHaysSearchConstants.JOB_MIN_PERM);
	        		max = getSuperData(IHaysSearchConstants.JOB_MAX_PERM);
	        		permPayType = getSuperData(IHaysSearchConstants.JOB_SELECT_PERM);
	        		if(permPayType == null)
	        			permPayType="A";	        		      		
	        		salary = ProcessSalary(min, max, permPayType, IHaysSearchConstants.PERMANENT);
	        		if( salary != null && salary.length() > 0 )
	        			salaryBuffer.append("(").append(salary).append(")");
		        }
	        }
	        /** Start PCR 110 - section 5.1 - 5.1	Grouping of Temporary and Contract Results*/
	        StringBuffer queryBuffer = mergeTempAndContract();
	        if(queryBuffer != null){
	        	if( salaryBuffer.length() > 0 ) {
        			salaryBuffer.append(" OR ");
        		} 
	        	salaryBuffer.append("(").append(queryBuffer).append(")");
	        }
	        
	       
	        /** END PCR 110 - section 5.1 - 5.1	Grouping of Temporary and Contract Results*/
	        // GENERAL
	        permPayType = getSuperData(IHaysSearchConstants.JOB_RATE);
	        if( permPayType != null && permPayType.length() > 0) {
	        	min = getSuperData(IHaysSearchConstants.JOB_MIN_SALARY);
	        	max = getSuperData(IHaysSearchConstants.JOB_MAX_SALARY);
	        	if( min != null || max != null) {
	        		salary = ProcessSalary(min, max, permPayType, null);
	        		if( salary != null && salary.length() > 0 ) {
		        		if( salaryBuffer.length() > 0 ) {
		        			salaryBuffer.append(" OR ");
		        		} 
		        		salaryBuffer.append("(").append(salary).append(")");
	        		}
	        	}
	        }
	        
	        if(salaryBuffer.length() > 0) {
	        	if( query.length() > 0){
        			query.append(" AND ");
        			queryPartforSearchPOC.append(" AND ");
	        	}
	        	query.append(" (").append(salaryBuffer).append(")*10*10 ");
	        	queryPartforSearchPOC.append(" (").append(salaryBuffer).append(")*10*10 ");
	        	
	        }
	        SystemUtils.trace(TRACE_NAME, "processApiSearchParameters: Salaries='" + query + "'");
	        
	        // CONTENT TYPE
	        String contentType = getSuperData(IHaysSearchConstants.CONTENT_TYPE);
	        if( "Jobs".equals(contentType) || "Candidates".equals(contentType)){
	        	if( query.length() > 0){
	        			query.append(" AND ");
	        			queryPartforSearchPOC.append(" AND ");
	        	}
	        	query.append(" (").append(contentType).append(" WITHIN dDocType)*10*10");
	        	queryPartforSearchPOC.append(" (").append(contentType).append(" WITHIN dDocType)*10*10");
	        }
	        SystemUtils.trace(TRACE_NAME, "processApiSearchParameters: Content Type ='"+contentType+", " + query + "'");
	        
	        
	        
	        // RELEASE DATE
	        String releaseDate = getSuperData(IHaysSearchConstants.RELEASE_DATE);
	        if( releaseDate != null && releaseDate.length() >0){
	        	if( query.length() > 0){
        			query.append(" AND ");
        			queryPartforSearchPOC.append(" AND ");
	        	}
	        	query.append(" (").append(" SDATA(dInDate > '").append(releaseDate).append("'))*10*10");
	        	queryPartforSearchPOC.append(" (").append(" SDATA(dInDate > '").append(releaseDate).append("'))*10*10");
	        }
	        SystemUtils.trace(TRACE_NAME, "processApiSearchParameters: Release Date ='"+releaseDate+", " + query + "'");
	        
	       
	        
	     // Registered Date
	        String registeredDate = getSuperData(IHaysSearchConstants.REGISTERED_DATE);
	        if( registeredDate != null && registeredDate.length() >0){
	        	if( query.length() > 0){
        			query.append(" AND ");
        			queryPartforSearchPOC.append(" AND ");
	        	}
	        	query.append(" (").append(" SDATA(xEventDate > '").append(registeredDate).append("'))*10*10");
	        	queryPartforSearchPOC.append(" (").append(" SDATA(xEventDate > '").append(registeredDate).append("'))*10*10");
	        }
	        SystemUtils.trace(TRACE_NAME, "processApiSearchParameters: Registered Date ='"+registeredDate+", " + query + "'");
	        
	        
	     // LOCALE
			String locale = getData(IHaysSearchConstants.LOCALE);
			if( locale != null && locale.trim().length() > 0){
				query.append(" AND ({").append(locale).append("} WITHIN xLocale)*10*10");
				queryPartforSearchPOC.append(" AND ({").append(locale).append("} WITHIN xLocale)*10*10");
			}
			
		 //	MR 219
		 // MicrositeCode
			String micrositecode = getData(IHaysSearchConstants.micrositeCode);
			if( micrositecode != null && micrositecode.trim().length() > 0){
				query.append(" AND ({").append(micrositecode).append("} WITHIN xMicroSiteCode)*10*10");
				queryPartforSearchPOC.append(" AND ({").append(micrositecode).append("} WITHIN xMicroSiteCode)*10*10");
			}
		 //end MR 219	
	        
			//added for LinkedIn Job
	        String linkedInJob = getSuperData("islinkedin");
	        if( linkedInJob != null && linkedInJob.trim().length() > 0)
	        {
		       	query.append(" AND ({").append(linkedInJob).append("} WITHIN xisLinkedIn)*10*10");
				queryPartforSearchPOC.append(" AND ({").append(linkedInJob).append("} WITHIN xisLinkedIn)*10*10");				
			 } 
	        	        
        
			// LOCATION		
			m_binder.putLocal(IHaysSearchConstants.EXCLUDE, "0");
			//String latitudeF = getSuperData(IHaysSearchConstants.NE_LATITUDE_FILTER);
			//String longitudeF = getSuperData(IHaysSearchConstants.NE_LONGITUDE_FILTER);
			//String radiusF = getSuperData(IHaysSearchConstants.RADIUS_FILTER);
			//String levelF = getSuperData(IHaysSearchConstants.LEVEL_FILTER);
			String latitude = getSuperData(IHaysSearchConstants.NE_LATITUDE);
			String longitude = getSuperData(IHaysSearchConstants.NE_LONGITUDE);
			String radius = getSuperData(IHaysSearchConstants.RADIUS);
			 level = getSuperData(IHaysSearchConstants.LEVEL);			
			int count =0;
			
			//String location_set = getSuperData("location_set");
			
			// Will be non-empty only for APAC countries.
			//SystemUtils.trace(TRACE_NAME, "location set"+location_set);
			
			String location_id = getSuperData("location_id");
			if(location_id!=null && !location_id.equals(""))
			{
				ResultSet localeResultSet = this.m_binder.getResultSet("LOCALE_DETAILS");
				String locationColumn=localeResultSet.getStringValueByName("LOCATION_COLUMN");
				String locationIds=location_id;
				DataBinder docParams = new DataBinder();
		        docParams.putLocal("locationColumn", locationColumn); 
		        docParams.putLocal("locationIds", locationIds); 
		        Provider p = Providers.getProvider("SystemDatabase");	    	    
		        Workspace databaseServerWs = (Workspace)p.getProvider();
				ResultSet locationsRset = databaseServerWs.createResultSet("LocationDetailsQuery", docParams);
				DataResultSet locationsdrset=new DataResultSet(); 
				locationsdrset.copy(locationsRset);
				
				SystemUtils.trace(TRACE_NAME, "Location column:"+locationColumn);
				SystemUtils.trace(TRACE_NAME, "LocationID's:"+locationIds);
				SystemUtils.trace(TRACE_NAME, "Location Data ResultSet:"+locationsdrset);
				
				
				if (locationsdrset.getNumRows()>0)
				{
					
					 do{
				        	
			        		int currentRowIndex =  locationsdrset.getCurrentRow();
			        		level = locationsdrset.getStringValueByName("level");
							longitude = locationsdrset.getStringValueByName("longitude");
							latitude = locationsdrset.getStringValueByName("latitude");;
							SystemUtils.trace(TRACE_NAME, "Location:"+locationsdrset.getStringValueByName("default_description"));
							SystemUtils.trace(TRACE_NAME, "level:"+level);
							SystemUtils.trace(TRACE_NAME, "Longitude:"+longitude);
							SystemUtils.trace(TRACE_NAME, "Latitude:"+latitude);     		
							if(latitude != null && latitude.trim().length() > 0 && !latitude.equals(IHaysSearchConstants.DEFAULT)
							&& longitude != null && longitude.trim().length() > 0 && !longitude.equals(IHaysSearchConstants.DEFAULT)
							&& radius != null && radius.trim().length() > 0  && !radius.equals(IHaysSearchConstants.DEFAULT))
							{
							
								processLocation(longitude, latitude, radius, "");
							}
							else
							{
								processLocation(null, null, null, "");
							}
							
							if (longitude != null && longitude.trim().length() > 0 && !longitude.equals(IHaysSearchConstants.DEFAULT)
									&& latitude != null && latitude.trim().length() > 0 && !latitude.equals(IHaysSearchConstants.DEFAULT)
									&& level!= null && level.trim().length() > 0 && !level.equals(IHaysSearchConstants.DEFAULT)) {
								SystemUtils.trace(TRACE_NAME, "Inside 3rd if condition: " + longitude + ", " + latitude + ", " + radius + ", level" + level);
								if(count>0){
									query.append(" OR ");
									queryPartforSearchPOC.append(" OR ");
								}
								else{
									query.append(" AND ( "); //Opening bracket
									queryPartforSearchPOC.append(" AND ( ");
								}
								query.append( constructLocationQuery(longitude, latitude, level) );
								queryPartforSearchPOC.append( constructLocationQuery(longitude, latitude, level) );
								SystemUtils.trace(TRACE_NAME, "Query after adding location "+query);
							}
							count++;
							
				        }while(locationsdrset.next());			
						
					 query.append(")"); //Closing bracket
					 queryPartforSearchPOC.append(")");
				}
				
			}
			
		  // INTERNATIONAL
        String jobInternational = getData(IHaysSearchConstants.JOB_INTERN,null);
        if( jobInternational != null ) {
	        if( query.length() > 0)

	        if( ("1").equals(jobInternational) || ("Y").equals(jobInternational)) {
	        	query.append(" AND (Yes WITHIN xInternational)*10*10");
	        	query.append(") )");
	        	queryPartforSearchPOC.append(" AND (Yes WITHIN xInternational)*10*10");
	        } else {
	        	query.append(" NOT (Yes WITHIN xInternational)");
	        	queryPartforSearchPOC.append(" NOT (Yes WITHIN xInternational)");
	        }
        }else {
        	query.append(") )");
        	query.append(" NOT (Yes WITHIN xInternational)");
        	queryPartforSearchPOC.append(" NOT (Yes WITHIN xInternational)");
        }
        
       	
        
        
     // Non-Nationals
        String jobNonEU = getData(IHaysSearchConstants.JOB_NONNATIONL,null);
        if( jobNonEU != null ) {
	        if( query.length() > 0)

	        if( ("1").equals(jobNonEU) || ("Y").equals(jobNonEU)) {
	        	query.append(" AND (Yes WITHIN xNonNationals)");
	        } else {
	        	query.append(" NOT (Yes WITHIN xNonNationals)");
	        }
        }
        
     // SPONSORED
        String jobSponsored = getData(IHaysSearchConstants.JOB_SPONSORED,null);
        if( jobSponsored != null) {
	        if( jobSponsored.equals("1") || jobSponsored.equalsIgnoreCase("Y")) {
	        	query.append(" NOT (idcnull WITHIN xSponsored)");
	        	queryPartforSearchPOC.append(" NOT (idcnull WITHIN xSponsored)");
	        } 
        }
        

        // FILTER
		query = processFilterSearch(query);
		queryPartforSearchPOC = processFilterSearch(queryPartforSearchPOC);
		queryPartforSearchPOC.append(")"); //Closing bracket
       

       
	}
		//Bucket Logic for Search POC change, Five buckets are made 1 day old, 3 days old, 7 days old,14 days old and more than 14 days old.
        
        Calendar currentDate = Calendar.getInstance();
        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd");
        String dateNow = formatter.format(currentDate.getTime());
        SystemUtils.trace(TRACE_NAME, "processApiSearchParameters: Current Date is ======="+ dateNow);
       
        currentDate.add(Calendar.DATE,+1);
        String datetommorow = formatter.format(currentDate.getTime());
        SystemUtils.trace(TRACE_NAME, "processApiSearchParameters: Tommorow days before date is ======="+ datetommorow);
        
        currentDate.add(Calendar.DATE,-2);
        String date24hrs = formatter.format(currentDate.getTime());
        SystemUtils.trace(TRACE_NAME, "processApiSearchParameters: 1 days before date is ======="+ date24hrs);
        
        currentDate.add(Calendar.DATE,-1);
        String date03 = formatter.format(currentDate.getTime());
        SystemUtils.trace(TRACE_NAME, "processApiSearchParameters: 3 days before date is ======="+ date03);
        
        currentDate.add(Calendar.DATE,-4);
        String date07 = formatter.format(currentDate.getTime());
        SystemUtils.trace(TRACE_NAME, "processApiSearchParameters: 7 days before date is ======="+ date07);
        
        currentDate.add(Calendar.DATE,-7);
        String date14 = formatter.format(currentDate.getTime());
        SystemUtils.trace(TRACE_NAME, "processApiSearchParameters: 14 days before date is ======="+ date14);
        
        
       
        
       query.append(" ACCUM(((").append(" SDATA(xEventDate between'").append(date24hrs).append("' and '").append(datetommorow).append(" '))*.25");
       query.append(" OR(").append(" SDATA(xEventDate between'").append(date03).append("' and '").append(date24hrs).append(" '))*.23");
       query.append(" OR(").append(" SDATA(xEventDate between'").append(date07).append("' and '").append(date03).append(" '))*.22");
       query.append(" OR(").append(" SDATA(xEventDate between'").append(date14).append("' and '").append(date07).append(" '))*.21");
       query.append(" OR(").append(" SDATA(xEventDate < '").append(date14).append("'))*.20 )").append(queryPartforSearchPOC).append(")");
       
       SystemUtils.trace(TRACE_NAME, "processApiSearchParameters: WEIGHTINGSsssssssssss ="+ query);
        
        
     //end

		
		
		SystemUtils.trace(TRACE_NAME, new StringBuffer("final locations: latitude =").append(getData(IHaysSearchConstants.NE_LATITUDE)).append(", longitude: ").append(getData(IHaysSearchConstants.NE_LONGITUDE)).append(", radius: ").append(getData(IHaysSearchConstants.RADIUS)).toString() );
		      
		 m_binder.putLocal("QueryText", query.toString());
	     SystemUtils.trace(TRACE_NAME, "\nFinal query123: =" + query );
	}
	
	private StringBuffer mergeTempAndContract() throws DataException{
		// CONTRACT
        String ctype = getSuperData(IHaysSearchConstants.JOB_CONTRACT);
        // TEMPORARY
        String ttype = getSuperData(IHaysSearchConstants.JOB_TEMP);
        String cmin, cmax, cpermPayType, tmin, tmax, tpermPayType;
        //initialise with request parameters
        cmin = getSuperData(IHaysSearchConstants.JOB_MIN_CONTRACT);
		cmax = getSuperData(IHaysSearchConstants.JOB_MAX_CONTRACT);
		cpermPayType = getSuperData(IHaysSearchConstants.JOB_SELECT_CONTRACT);
		tmin = getSuperData(IHaysSearchConstants.JOB_MIN_TEMP);
		tmax = getSuperData(IHaysSearchConstants.JOB_MAX_TEMP);
		tpermPayType = getSuperData(IHaysSearchConstants.JOB_SELECT_TEMP);
		
		 SystemUtils.trace(TRACE_NAME, "ctype="+ctype+",cmin: =" + cmin +",cmax:"+cmax);
		 SystemUtils.trace(TRACE_NAME, "ttype="+ttype+",tmin: =" + tmin +",tmax:"+tmax);
		//merge parameters
        if( !(ctype.isEmpty()) && ttype.isEmpty()) {
        	if( ctype.length() > 0 ) {
        		tmin = cmin;
        		tmax = cmax;
        		tpermPayType = cpermPayType; 
	        }
        }
        if( !(ttype.isEmpty()) && ctype.isEmpty()) {
        	if( ttype.length() > 0 ) {
        		cmin = tmin;
        		cmax = tmax;
        		cpermPayType = tpermPayType;
	        }
        }
        
        SystemUtils.trace(TRACE_NAME, "After Assignment cmin: =" + cmin +",cmax:"+cmax);
		SystemUtils.trace(TRACE_NAME, "After Assignment tmin: =" + tmin +",tmax:"+tmax);
		
        cpermPayType = setTempContractSalaryType(cpermPayType);
        tpermPayType = setTempContractSalaryType(tpermPayType);
        cmin = setTempContractSalaryMin(cmin);
        cmax = setTempContractSalaryMax(cmax,cpermPayType);
        tmin = setTempContractSalaryMin(tmin);
        tmax = setTempContractSalaryMax(tmax,tpermPayType);
        
        SystemUtils.trace(TRACE_NAME, "Final cmin: =" + cmin +",cmax:"+cmax);
		SystemUtils.trace(TRACE_NAME, "Final  tmin: =" + tmin +",tmax:"+tmax);
        
        StringBuffer query = null;
        if((ttype != null && ttype.length() > 0) || (ctype != null && ctype.length() >0)){
        	query = new StringBuffer();
        	if(cmin != null && cmax != null){
		        //get annualised double values
		        double cMin = MetadataSetFilter.calculateUnifiedSalary(cmin, cpermPayType);
		        double cMax = MetadataSetFilter.calculateUnifiedSalary(cmax, cpermPayType);
		        double tMin = MetadataSetFilter.calculateUnifiedSalary(tmin, tpermPayType);
		        double tMax = MetadataSetFilter.calculateUnifiedSalary(tmax, tpermPayType);
		        //check if min and max are disjoint
		        boolean isDisjoint = isDisjoint(cMin, cMax, tMin, tMax);
		        
		        //convert to upper case for use in query
		        tpermPayType = tpermPayType.toUpperCase();
			    cpermPayType = cpermPayType.toUpperCase();
		        if(isDisjoint){
				    //construct query for disjoint set of min and max
					query.append("(");
					query.append(" (SDATA(xMinSalary <= ").append(cMin).append(") AND SDATA(xMaxSalary >= ").append(cMin).append(") )");
					query.append(" OR (SDATA(xMinSalary <= ").append(cMax).append(") AND SDATA(xMaxSalary >= ").append(cMax).append(") )");
					query.append(" OR (SDATA(xMinSalary >= ").append(cMin).append(") AND SDATA(xMaxSalary <= ").append(cMax).append(") )");
					query.append(" OR (SDATA(xMinSalary <= ").append(tMin).append(") AND SDATA(xMaxSalary >= ").append(tMin).append(") )");
					query.append(" OR (SDATA(xMinSalary <= ").append(tMax).append(") AND SDATA(xMaxSalary >= ").append(tMax).append(") )");
					query.append(" OR (SDATA(xMinSalary >= ").append(tMin).append(") AND SDATA(xMaxSalary <= ").append(tMax).append(") )");
					query.append(") AND ");
		        }else{
		        	//find min and max
		        	double minArray[] = {cMin, cMax, tMin, tMax};
		        	Arrays.sort(minArray);
		        	double lowestMin = minArray[0];
		        	double highestMax = minArray[3];
		        	query.append("(");
					query.append(" (SDATA(xMinSalary <= ").append(lowestMin).append(") AND SDATA(xMaxSalary >= ").append(lowestMin).append(") )");
					query.append(" OR (SDATA(xMinSalary <= ").append(highestMax).append(") AND SDATA(xMaxSalary >= ").append(highestMax).append(") )");
					query.append(" OR (SDATA(xMinSalary >= ").append(lowestMin).append(") AND SDATA(xMaxSalary <= ").append(highestMax).append(") )");
					query.append(")AND ");
		        }
        	}

			query.append(" (({").append(IHaysSearchConstants.TEMPORARY).append("} OR {").append(IHaysSearchConstants.CONTRACT).append("}) WITHIN xJobType ) ");
        }
        return query;
	}
	
	private String setTempContractSalaryType(String salaryType){
		if(salaryType == null || salaryType.length() ==0){
			salaryType = "H";
		}
		return salaryType;
	}
	
	private String setTempContractSalaryMin(String salaryMin){
		if(salaryMin ==null || salaryMin.length() ==0){
			salaryMin = "0";
		}
		return salaryMin;
	}
	
	private String setTempContractSalaryMax(String salaryMax,String salaryType){
		if(salaryMax ==null || salaryMax.length() ==0){
			if("D".equalsIgnoreCase(salaryType)){
				salaryMax  = "9999";
			}else{
				salaryMax  = "999";
			}
			
		}
		return salaryMax;
	}
	
	private boolean isDisjoint(double cMin,double cMax,double tMin,double tMax){
		if(tMin > cMax || cMin > tMax){
			return true;
		}
		return false;
	}
	private StringBuffer constructLocationQuery(String x, String y, String level) {
		String[] arrayLongitude = x.split(";");
		String[] arrayLatitude = y.split(";");
		String[] arrayLevel = level.split(";");
		StringBuffer rez = new StringBuffer();
		for( int i = 0; i < arrayLongitude.length; i++) {
			if( rez.length() > 0) 
				rez.append(" AND ");
			rez.append("{").append(arrayLongitude[i]).append("#").append(arrayLatitude[i]).append("}");
			rez.append(" WITHIN xHaysLocation").append(arrayLevel[i]);
		}
		if( rez.length() > 0) {
			rez.append(")*10*10");
			rez.insert(0, "(");
		}
		return rez;
	}
	
	private StringBuffer processCheckBoxes(String name) {
		StringBuffer values = new StringBuffer();
		Properties prop = super.m_binder.getLocalData();
        String key, value;
        for( Enumeration en = prop.keys(); en.hasMoreElements();) {
        	key = (String)en.nextElement();
        	if( key.indexOf(name) == 0 && key.indexOf("filter") < 0) {
        		value = getSuperData(key);
		        if( value != null ) {
		        	value = value.trim();
		        	if( value.length() > 0 ) {
		        		values.append(", ").append(value);
			        }
		        }
        	}
        }
        return values;
	}
	
	private void processLocation(String longitude, String latitude, String radius, String suffix){
		 // LOCATION
        String swLatitude = getSuperData(IHaysSearchConstants.SW_LATITUDE + suffix);
        String swLongitude = getSuperData(IHaysSearchConstants.SW_LONGITUDE + suffix);
        
        // convert radius to miles
        if( radius != null) {
        	String distUnit = getSuperData(IHaysSearchConstants.DISTANCE_UNIT);
        	if(distUnit != null && IHaysSearchConstants.DISTANCE_UNITS.contains(distUnit) ) {
        		if ( distUnit.equals("km")) {
        			radius = String.valueOf( (int)(Integer.parseInt(radius) * IHaysSearchConstants.KM_MILES_CONVERT));
        		}
        	}
        }
        
        SystemUtils.trace(TRACE_NAME, new StringBuffer("latitude: ").append(latitude).append(", long: ").append(longitude).append(", radius: ").append(radius).toString());
        
        if( latitude == null || longitude == null) {
        	latitude = IHaysSearchConstants.DEFAULT;
        	longitude = IHaysSearchConstants.DEFAULT;
        	swLatitude = IHaysSearchConstants.DEFAULT;
        	swLongitude = IHaysSearchConstants.DEFAULT;
        	radius = IHaysSearchConstants.DEFAULT;
        } else {
        	if( swLatitude == null || swLongitude == null){
            	swLatitude = IHaysSearchConstants.DEFAULT;
            	swLongitude = IHaysSearchConstants.DEFAULT;        		
        	}   
        	if( radius == null){
            	radius = IHaysSearchConstants.DEFAULT_RADIUS;        		
        	}
        	if( longitude != null) {
        		int index = longitude.lastIndexOf(";");
        		if( index > 0)
        			longitude = longitude.substring(index+1);
        	}
        	if( latitude != null) {
        		int index = latitude.lastIndexOf(";");
        		if( index > 0)
        			latitude = latitude.substring(index+1);
        	}
        }
        m_binder.putLocal(IHaysSearchConstants.NE_LATITUDE, latitude);
        m_binder.putLocal(IHaysSearchConstants.NE_LONGITUDE, longitude);
        m_binder.putLocal(IHaysSearchConstants.SW_LATITUDE, swLatitude);
        m_binder.putLocal(IHaysSearchConstants.SW_LONGITUDE, swLongitude);
        m_binder.putLocal(IHaysSearchConstants.RADIUS, radius);
	}
	
	
	private StringBuffer processFilterSearch(StringBuffer origQuery) throws ServiceException, DataException {
		StringBuffer query = new StringBuffer(origQuery);
		StringBuffer queryPart = null;		

		// EXCLUDE
		String exclude = getSuperData(IHaysSearchConstants.EXCLUDE);
		
		String jobTitle = getSuperData(IHaysSearchConstants.JOB_TITLE_FILTER);
		if( jobTitle != null && jobTitle.trim().length() > 0) {
			String[] jobTitleArray  = jobTitle.split(";");
			String aFilter = null;
			queryPart = new StringBuffer("((");
			for( int i = 0; i < jobTitleArray.length; i++){
				aFilter = jobTitleArray[i].trim();
				if( aFilter.startsWith(",") ) {
					aFilter = aFilter.substring(1);
				}
				aFilter = aFilter.replaceAll(",[ ]?", " | ");
				if( queryPart.length() > 2)
					queryPart.append(" and ");
				queryPart.append("(").append(aFilter).append(")");
			}
	        if( queryPart.length() > 1){
	        	queryPart.append(") WITHIN xAutomatedTitle) ");
	        	if( query.length() > 0)
	    			query.append(" AND ");
	        	query.append(queryPart);
	        }
		}
        
     // INDUSTRY        
        String industry = getSuperData(IHaysSearchConstants.JOB_INDUSTRY_FILTER);
        
        if( industry != null && industry.trim().length() > 0) {
        	queryPart = new StringBuffer("(");
			String[] industryArray  = industry.split(";");
			String aFilter = null;
			for( int i = 0; i < industryArray.length; i++){
				aFilter = industryArray[i].trim();
				if( aFilter.startsWith(",") ) {
					aFilter = aFilter.substring(1);
				}
				aFilter = aFilter.replaceAll(",[ ]?", " | ");
				if( queryPart.length() > 1)
					queryPart.append(" and ");
				queryPart.append("(").append(aFilter).append(")");
			}
			 if( queryPart.length() > 1){
	        	queryPart.append(") WITHIN xIndustry ");
        		if(exclude != null && exclude.contains(IHaysSearchConstants.JOB_INDUSTRY) ) {
	    			query.append(" NOT ");
	    			SystemUtils.trace(TRACE_NAME, "In  industries other than " + aFilter);
	    		} else if( query.length() > 0) {
	    			query.append(" AND ");
	        	}
	        	query.append(" (").append(queryPart).append(" )*10*10");
	        	
			 }
		}
        
       //CATEGORY
         String categoryf=getSuperData(IHaysSearchConstants.JOB_CATEGORY_FILTER);
         SystemUtils.trace(TRACE_NAME, "JOB_CATEGORY_FILTER =" + categoryf );
         if( categoryf != null && categoryf.trim().length() > 0) {
        	 
        	 StringBuffer  finalsubTerms=new StringBuffer();
	        	SystemUtils.trace(TRACE_NAME, "categories 1st =" + categoryf);
	        	 String [] categoriesArray = categoryf.split("&");
	        	 
	            for(int x=0;x<categoriesArray.length;x++)   
	        	 {
	        		 SystemUtils.trace(TRACE_NAME, "CategoriesArray Value"+categoriesArray[x]);
                    if(categoriesArray[x].startsWith(",")) 
                    {
                    	finalsubTerms.append(")AND(");
                    	SystemUtils.trace(TRACE_NAME, "Starts With");
                    categoriesArray[x]=categoriesArray[x].substring(1);
                    }
                    categoriesArray[x] =  categoriesArray[x].replaceAll("[,][ ]?", "#");
	        		 SystemUtils.trace(TRACE_NAME, "categoryffffff"+categoriesArray[x]);
	             		   
     		 SystemUtils.trace(TRACE_NAME, "categories after replace all=" + categoryf);
     	     String [] categoriesfArray =  categoriesArray[x].split("#");
     		 SystemUtils.trace(TRACE_NAME, "CategoriesfArray length"+categoriesfArray.length);
     		   for(int i=0;i<categoriesfArray.length;i++)
     		   {
     		 
     			 		String  subTerms = ontGetRelatedTerms(categoriesfArray[i], "xCategory@hays:ParentTerm");
			        		 SystemUtils.trace(TRACE_NAME, "subTerms=" + subTerms);
			        		
			        		if("[]".equalsIgnoreCase(subTerms))
			        		{
			        			subTerms = categoriesfArray[i];
			        		}
			        		else
			        		{ 
			        			subTerms = categoriesfArray[i] + "," + subTerms.substring(1, subTerms.length()-1);
			        		}
			        		 SystemUtils.trace(TRACE_NAME, "for i=="+i+"  ,subTerms=" + subTerms);
			        		 if(i>0)
			        		 {
			        			 finalsubTerms.append(" | ");
			        		 }
			        		 finalsubTerms.append(subTerms);
			        		 SystemUtils.trace(TRACE_NAME, "for i=="+i+"  ,finalsubTerms=" + finalsubTerms.toString());		 
     		 }
     		      		 
	        	 }
     		 SystemUtils.trace(TRACE_NAME, "final  finalsubTerms=" + finalsubTerms.toString());	
     		categoryf = finalsubTerms.toString();	
     		categoryf = categoryf.replaceAll("[,;#][ ]?", " | ");	
     		if( query.length() > 0) 
    		{
    			query.append(" AND ");
    		}
        	query.append("((").append(categoryf).append(") WITHIN xCategory )*10*10");
	        SystemUtils.trace(TRACE_NAME, "processApiSearchParameters: Category='" + query + "'");
        	
     		// added for r7 end
         }
         
        
        
        // KEYWORDS
        String keywords = getSuperData(IHaysSearchConstants.JOB_KEYWORDS_FILTER);
        
       
        
        if( keywords != null && keywords.trim().length() > 0) {
        	SystemUtils.trace(TRACE_NAME, "JOB_KEYWORDS_FILTER =" + keywords );
        	queryPart = new StringBuffer("");
			String[] keywordsArray  = keywords.split(";");
			String aFilter = null;
			for( int i = 0; i < keywordsArray.length; i++){
				aFilter = keywordsArray[i].trim();
				if( aFilter.startsWith(",") ) {
					aFilter = aFilter.substring(1);
				}
				aFilter = aFilter.replaceAll(",[ ]?", " | ");
				if( queryPart.length() > 1)
					queryPart.append(" and ");
				queryPart.append("(").append(aFilter).append(")");
			}
	        if( queryPart.length() > 1){
	        	queryPart.append(" OR "+queryPart+ " WITHIN xKeywords OR "+queryPart+" WITHIN dDocTitle");
	        	if( query.length() > 0)
	    			query.append(" AND ");
	        	query.append("( "+queryPart+ " )*10*10");
	        }
		}
        
        // JOB TYPES
        String type = getSuperData(IHaysSearchConstants.JOB_TYPE_FILTER);
        String[] jobTypeArray = null;
        if( type != null && type.trim().length() > 0) {
        	queryPart = new StringBuffer("(");
        	jobTypeArray  = type.split(";");
        	String aFilter = null;
			for( int i = 0; i < jobTypeArray.length; i++){
				aFilter = jobTypeArray[i].trim();
				if( aFilter.startsWith(",") ) {
					aFilter = aFilter.substring(1);
				}
				aFilter = aFilter.replaceAll(",[ ]?", " | ");
				if( queryPart.length() > 1)
					queryPart.append(" and ");
				queryPart.append("(").append(aFilter).append(")");
			}
	        if( queryPart.length() > 1){
	        	queryPart.append(") WITHIN xJobType ");
	        	if( query.length() > 0)
	    			query.append(" AND ");
	        	query.append(queryPart);
	        }        	
        }
        
     // PERMANENT Job Type Filter
        StringBuffer jobTypeBuffer = new StringBuffer();
        String permtype, min, max, permPayType, perm_salary = null;
        permtype = getSuperData(IHaysSearchConstants.JOB_PERM_SLIDER);
        if( permtype != null ) {
        	if( permtype.length() > 0 ) {
        		min = getSuperData(IHaysSearchConstants.JOB_MIN_PERM_SLIIDER);
        		max = getSuperData(IHaysSearchConstants.JOB_MAX_PERM_SLIIDER);
        		permPayType = getSuperData(IHaysSearchConstants.JOB_SELECT_PERM_SLIDER);
        		perm_salary = ProcessSalary(min, max, permPayType, IHaysSearchConstants.PERMANENT);
        		if( perm_salary != null && perm_salary.length() > 0 )
        		{
        			jobTypeBuffer.append("(").append(perm_salary).append(")");
        		}
        			
	        }
        }
        
     // TEMPORARY Job Type Filter
        //StringBuffer salaryBuffer = new StringBuffer();
        String temptype,temp_salary = null;
        temptype = getSuperData(IHaysSearchConstants.JOB_TEMP_SLIDER);
        if( temptype != null ) {
        	if( temptype.length() > 0 ) {
        		min = getSuperData(IHaysSearchConstants.JOB_MIN_TEMP_SLIIDER);
        		max = getSuperData(IHaysSearchConstants.JOB_MAX_TEMP_SLIIDER);
        		permPayType = getSuperData(IHaysSearchConstants.JOB_SELECT_TEMP_SLIDER);
        		temp_salary = ProcessSalary(min, max, permPayType, IHaysSearchConstants.TEMPORARY);
        		if( temp_salary != null && temp_salary.length() > 0 )
        		{
        			if(permtype!=null && permtype.equalsIgnoreCase("on"))
        			{
        				jobTypeBuffer.append(" OR ");
        			}        			
        			jobTypeBuffer.append("(").append(temp_salary).append(")");
        		}
	        }
        }
        
     // CONTRACT Job Type Filter
        
        String conttype, cont_salary = null;
        conttype = getSuperData(IHaysSearchConstants.JOB_CONT_SLIDER);
        if( conttype != null ) {
        	if( conttype.length() > 0 ) {
        		min = getSuperData(IHaysSearchConstants.JOB_MIN_CONT_SLIIDER);
        		max = getSuperData(IHaysSearchConstants.JOB_MAX_CONT_SLIIDER);
        		permPayType = getSuperData(IHaysSearchConstants.JOB_SELECT_CONT_SLIDER);
        		cont_salary = ProcessSalary(min, max, permPayType, IHaysSearchConstants.CONTRACT);
        		if( cont_salary != null && cont_salary.length() > 0 )
        		{
        			if((permtype!=null && permtype.equalsIgnoreCase("on"))  || (temptype!=null && temptype.equalsIgnoreCase("on")))
        			{
        				jobTypeBuffer.append(" OR ");
        			}        			
        			jobTypeBuffer.append("(").append(cont_salary).append(")");
        		}
        			
        			
	        }
        }
        
        
        if (jobTypeBuffer!=null && jobTypeBuffer.length()>0)
        {
        	query.append(" AND ( ").append(jobTypeBuffer).append(" )*10*10");
        }
        
        
        SystemUtils.trace(TRACE_NAME, "Filter Job Type Query ='" + query + "'");
		return query;
	}
	
	
	private boolean isTitleSet(){
		
		String ref = getSuperData(IHaysSearchConstants.JOB_TITLE_REF);
		
		if(ref != null && ref.trim().length() > 0)
			return true;
		return false;
	}
	
	private boolean isValid(String salaryMin, String salaryMax, String paymentType) throws NumberFormatException, DataException {
		SystemUtils.trace(TRACE_NAME, "validate salary: " + new StringBuffer("min = ").append(salaryMin).append(", max = ").append(salaryMax).append(" payment type = ").append(paymentType));
		boolean isValid = false;
		if( paymentType != null)
			paymentType = paymentType.toUpperCase();
		
		//start pcr 110 - 2.1
		if(salaryMin ==null || salaryMin.length() ==0){
			salaryMin = "0";
		}
		if(salaryMax ==null || salaryMax.length() ==0){
			salaryMax  = "999999";
		}
		//end pcr 110 - 2.1
		if( salaryMin != null && salaryMax != null) {
			double min = MetadataSetFilter.calculateUnifiedSalary(salaryMin, paymentType);
            double max = MetadataSetFilter.calculateUnifiedSalary(salaryMax, paymentType);
            SystemUtils.trace(TRACE_NAME, "validate salary range: " + min + ", " + max);
            if( min <= max && IHaysSearchConstants.PAYMENT_TYPES.indexOf(paymentType) >= 0) {
            	return true;
            }
		}
		
		return isValid;
	}
	
	private String ProcessSalary(String salaryMin, String salaryMax, String paymentType, String jobType) throws DataException {
		SystemUtils.trace(TRACE_NAME, "validate salary: " + new StringBuffer("min = ").append(salaryMin).append(", max = ").append(salaryMax).append(" payment type = ").append(paymentType).append(", jobType = ").append(jobType));
		
		StringBuffer query = new StringBuffer();
		if( paymentType != null)
			paymentType = paymentType.toUpperCase();
		
		//start pcr 110 - 2.1
		if(salaryMin ==null || salaryMin.length() ==0){
			salaryMin = "0";
		}
		if(salaryMax ==null || salaryMax.length() ==0){
			salaryMax  = "999999";
		}
		//end pcr 110 - 2.1
		
		if( salaryMin != null && salaryMax != null) {
    		try {
                double min = MetadataSetFilter.calculateUnifiedSalary(salaryMin, paymentType);
                double max = MetadataSetFilter.calculateUnifiedSalary(salaryMax, paymentType);
                SystemUtils.trace(TRACE_NAME, "Min-Max:" + min+","+max+"Payment Type:"+paymentType);
        		
        		if( min <= max && IHaysSearchConstants.PAYMENT_TYPES.indexOf(paymentType) >= 0) {
        			 SystemUtils.trace(TRACE_NAME, "Min-Max in If condition:" + min+","+max+"Payment Type:"+paymentType);
        			query.append("(");
	        		query.append(" (SDATA(xMinSalary <= ").append(min).append(") AND SDATA(xMaxSalary >= ").append(min).append(") )");
	        		query.append(" OR (SDATA(xMinSalary <= ").append(max).append(") AND SDATA(xMaxSalary >= ").append(max).append(") )");
	        		query.append(" OR (SDATA(xMinSalary >= ").append(min).append(") AND SDATA(xMaxSalary <= ").append(max).append(") )");
	        		query.append(")");
	        		if( jobType != null)
	        			query.append(" AND (({").append(jobType).append("}) WITHIN xJobType ) ");
        		}
        		 SystemUtils.trace(TRACE_NAME, "Query in processSalary function:" + query);
    		}catch(Exception ex) {
    			SystemUtils.trace(TRACE_NAME, "exception while generating query: " + ex);
    			String er = "csInvalidSalaryParameters";
    			throw new DataException(er);
    		}
		}
		return query.toString();
		
	}
	

	
	String ontGetRelatedTerms(String sArg1,String sArg2) throws ServiceException
	{
		SystemUtils.trace(TRACE_NAME, "inside ontGetRelatedTerms, sArg1::::=" + sArg1+" ,sArg2:"+sArg2);
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
			    SystemUtils.trace(TRACE_NAME, "before calling  callServiceHandlerSpecialism, metadata:" + metadata+" ,relation:"+relation+" ,isObject:"+isObject+" ,terms:"+terms);
			    result = callServiceHandlerSpecialism(m_service, "getRelatedTerms", properties, relation+metadata);
			    SystemUtils.trace(TRACE_NAME, "result after::::='" + result);
			   
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
	
	public void apiValidateSearchParams() throws ServiceException, DataException {{
		SystemUtils.trace(TRACE_NAME, "apiValidateSearchParams() binder Before:  " + super.m_binder.getLocalData());
		String jobTitle = getSuperData(IHaysSearchConstants.JOB_TITLE);
		String jobTitleFull = getSuperData(IHaysSearchConstants.JOB_TITLE_FULL);
		String jobTitleFullDescr = getSuperData(IHaysSearchConstants.JOB_TITLE_FULL_DECR);
		String ref = getSuperData(IHaysSearchConstants.JOB_TITLE_REF);
		String industry = getSuperData(IHaysSearchConstants.JOB_INDUSTRY);
        if( industry == null) {
        	StringBuffer industries = processCheckBoxes(IHaysSearchConstants.JOB_INDUSTRY);
        	if( industries.length() > 0) {
        		industry = industries.substring(1);	
        	}
        }
		String keywords = getSuperData(IHaysSearchConstants.JOB_KEYWORDS);
		//String typeP = "A";//getSuperData("job_permanent"); // commented for release 8.0
		String typeP =getSuperData(IHaysSearchConstants.JOB_SELECT_PERM);//added for release 8.0
		String jobTypeP = getSuperData(IHaysSearchConstants.JOB_PERM);
		String minP = getSuperData(IHaysSearchConstants.JOB_MIN_PERM);
		String maxP = getSuperData(IHaysSearchConstants.JOB_MAX_PERM);
		String jobTypeC = getSuperData(IHaysSearchConstants.JOB_CONTRACT);
		String typeC = getSuperData(IHaysSearchConstants.JOB_SELECT_CONTRACT);
		String minC = getSuperData(IHaysSearchConstants.JOB_MIN_CONTRACT);
		String maxC = getSuperData(IHaysSearchConstants.JOB_MAX_CONTRACT);
		String jobTypeT = getSuperData(IHaysSearchConstants.JOB_TEMP);
		String typeT = getSuperData(IHaysSearchConstants.JOB_SELECT_TEMP);
		String minT = getSuperData(IHaysSearchConstants.JOB_MIN_TEMP);
		String maxT = getSuperData(IHaysSearchConstants.JOB_MAX_TEMP);
		String location = getSuperData(IHaysSearchConstants.JOB_LOCATION);
		String neLatitude = getSuperData(IHaysSearchConstants.NE_LATITUDE);
        String neLongitude = getSuperData(IHaysSearchConstants.NE_LONGITUDE);
        String level = getSuperData(IHaysSearchConstants.LEVEL);
        String radius = getSuperData(IHaysSearchConstants.RADIUS);
        String contentType = getSuperData(IHaysSearchConstants.CONTENT_TYPE);
        String sortfield = getSuperData(IHaysSearchConstants.SORTFIELD);
        String sortorder = getSuperData(IHaysSearchConstants.SORTORDER);
        String locale = getSuperData(IHaysSearchConstants.LOCALE);
        String jobInternational = getSuperData(IHaysSearchConstants.JOB_INTERN);
        String jobNonNational = getSuperData(IHaysSearchConstants.JOB_NONNATIONL);
        String isOnlyJobTitle = getSuperData(IHaysSearchConstants.IS_ONLY_JOB_TITLE);
        String isFuzzy = getSuperData(IHaysSearchConstants.IS_FUZZY_SEARCH);
        String location_id=getSuperData(IHaysSearchConstants.LOCATION_ID);
        //String isHomePageSearch = getSuperData("isHomePageSearch");
        String job_category = getSuperData(IHaysSearchConstants.JOB_CATEGORY); 
        String isAdvanceSearch = getSuperData(IHaysSearchConstants.IS_ADVANCE_SEARCH);// added  for r7
        String isHomePageSearch = getSuperData(IHaysSearchConstants.IS_HOME_SEARCH);
        String jobPostCode = getSuperData(IHaysSearchConstants.JOB_POST_CODE);// added for alert form requirement for r7
        String locationSet = getSuperData("location_set");
        String linkedInJob = getSuperData("islinkedin");
        String jobExperienceLevel = getSuperData("experience_level");
        String er = null;
        StringBuffer parameters = new StringBuffer();
        
        SystemUtils.trace(TRACE_NAME, "Sort Field:  " + sortfield);
        SystemUtils.trace(TRACE_NAME, "Sort Order:  " + sortorder);
        
        if("".equalsIgnoreCase(sortorder))
        	m_binder.removeLocal(IHaysSearchConstants.SORTORDER);
        if("".equalsIgnoreCase(sortfield))
        	m_binder.removeLocal(IHaysSearchConstants.SORTFIELD);
        
        if( jobTitle != null && !jobTitle.equalsIgnoreCase("")){
        	jobTitle = QueryUtils.encodeForHaysSpecialKeywords(jobTitle);
        	parameters.append("&").append(IHaysSearchConstants.JOB_TITLE).append("=").append(jobTitle.replaceAll(IHaysSearchConstants.fuzzySpecialCharacters, " "));
        }
        if( jobTitleFull != null && !jobTitleFull.equalsIgnoreCase("")){
        	jobTitleFull = QueryUtils.encodeForHaysSpecialKeywords(jobTitleFull);
        	parameters.append("&").append(IHaysSearchConstants.JOB_TITLE_FULL).append("=").append(jobTitleFull);
        }
        if( jobTitleFullDescr != null && !jobTitleFullDescr.equalsIgnoreCase("")){
        	jobTitleFullDescr = QueryUtils.encodeForHaysSpecialKeywords(jobTitleFullDescr);
        	parameters.append("&").append(IHaysSearchConstants.JOB_TITLE_FULL_DECR).append("=").append(jobTitleFullDescr);
        }
        if( ref != null && !ref.equalsIgnoreCase("")){
        	ref = QueryUtils.encodeForHaysSpecialKeywords(ref);
        	parameters.append("&").append(IHaysSearchConstants.JOB_TITLE_REF).append("=").append(ref);
        }
        if( industry != null && !industry.equalsIgnoreCase(""))
        	parameters.append("&").append(IHaysSearchConstants.JOB_INDUSTRY).append("=").append(industry);
        if( keywords != null && !keywords.equalsIgnoreCase("")){
        	keywords = QueryUtils.encodeForHaysSpecialKeywords(keywords);
        	parameters.append("&").append(IHaysSearchConstants.JOB_KEYWORDS).append("=").append(keywords.replaceAll(IHaysSearchConstants.specialCharactersForKeywords, " "));
        }
        if( jobTypeP != null && !jobTypeP.equalsIgnoreCase("")) {
        	parameters.append("&").append(IHaysSearchConstants.JOB_PERM).append("=").append(jobTypeP).append("&").append(IHaysSearchConstants.JOB_MIN_PERM).append("=").append(minP);
        	
        	if(typeP == null){
        		typeP = "A"; //for uk and cerow
        		parameters.append("&").append(IHaysSearchConstants.JOB_MAX_PERM).append("=").append(maxP).append("&").append(IHaysSearchConstants.JOB_SELECT_PERM).append("=A");// commented for release 8.0
        	}else{
        		parameters.append("&").append(IHaysSearchConstants.JOB_MAX_PERM).append("=").append(maxP).append("&").append(IHaysSearchConstants.JOB_SELECT_PERM).append("="+typeP);// added for release 8.0
        	}
         }
        if( jobTypeC != null && !jobTypeC.equalsIgnoreCase("")) {
        	parameters.append("&").append(IHaysSearchConstants.JOB_CONTRACT).append("=").append(jobTypeC).append("&").append(IHaysSearchConstants.JOB_MIN_CONTRACT).append("=").append(minC);
        	parameters.append("&").append(IHaysSearchConstants.JOB_MAX_CONTRACT).append("=").append(maxC).append("&").append(IHaysSearchConstants.JOB_SELECT_CONTRACT).append("=").append(typeC);
        }
        if( jobTypeT != null && !jobTypeT.equalsIgnoreCase("")) {
        	parameters.append("&").append(IHaysSearchConstants.JOB_TEMP).append("=").append(jobTypeT).append("&").append(IHaysSearchConstants.JOB_MIN_TEMP).append("=").append(minT);
        	parameters.append("&").append(IHaysSearchConstants.JOB_MAX_TEMP).append("=").append(maxT).append("&").append(IHaysSearchConstants.JOB_SELECT_TEMP).append("=").append(typeT);
        }
        if( contentType != null && !contentType.equalsIgnoreCase(""))
        	parameters.append("&").append(IHaysSearchConstants.CONTENT_TYPE).append("=").append(contentType);
   		
        if( locale != null && !locale.equalsIgnoreCase(""))
        	parameters.append("&").append(IHaysSearchConstants.LOCALE).append("=").append(locale);
        
        if( location != null && !location.equalsIgnoreCase(""))
        	parameters.append("&").append(IHaysSearchConstants.JOB_LOCATION).append("=").append(location);
        if( neLongitude != null && neLatitude != null && !neLongitude.equalsIgnoreCase("") && !neLatitude.equalsIgnoreCase("")) {
        	parameters.append("&").append(IHaysSearchConstants.NE_LONGITUDE).append("=").append(neLongitude);
        	parameters.append("&").append(IHaysSearchConstants.NE_LATITUDE).append("=").append(neLatitude);
        }
        if( level != null && !level.equalsIgnoreCase(""))
        	parameters.append("&").append(IHaysSearchConstants.LEVEL).append("=").append(level);
        if( radius != null && !radius.equalsIgnoreCase(""))
        	parameters.append("&").append(IHaysSearchConstants.RADIUS).append("=").append(radius);
        if( sortfield != null && !sortfield.equalsIgnoreCase(""))
        	parameters.append("&").append(IHaysSearchConstants.SORTFIELD).append("=").append(sortfield);
        if( sortorder != null && !sortorder.equalsIgnoreCase(""))
        	parameters.append("&").append(IHaysSearchConstants.SORTORDER).append("=").append(sortorder);
        if( jobInternational != null && !jobInternational.equalsIgnoreCase(""))
        	parameters.append("&").append(IHaysSearchConstants.JOB_INTERN).append("=").append(jobInternational);
        if( jobNonNational != null && !jobNonNational.equalsIgnoreCase(""))
        	parameters.append("&").append(IHaysSearchConstants.JOB_NONNATIONL).append("=").append(jobNonNational);
        if( isOnlyJobTitle != null && !isOnlyJobTitle.equalsIgnoreCase(""))
        	parameters.append("&").append(IHaysSearchConstants.IS_ONLY_JOB_TITLE).append("=").append(isOnlyJobTitle);
        if( isFuzzy != null && !isFuzzy.equalsIgnoreCase(""))
        	parameters.append("&").append(IHaysSearchConstants.IS_FUZZY_SEARCH).append("=").append(isFuzzy);
        if( location_id != null && !location_id.equalsIgnoreCase(""))
        	parameters.append("&").append(IHaysSearchConstants.LOCATION_ID).append("=").append(location_id);
       
        if( isHomePageSearch != null && isHomePageSearch.equalsIgnoreCase("1")){
        	parameters.append("&").append(IHaysSearchConstants.IS_HOME_SEARCH).append("=").append(isHomePageSearch);
        }
        if( job_category != null && !job_category.equalsIgnoreCase("")){
        	parameters.append("&").append(IHaysSearchConstants.JOB_CATEGORY).append("=").append(job_category);
        }
        // added for r7
        if( isAdvanceSearch != null && !isAdvanceSearch.equalsIgnoreCase("")){
        	parameters.append("&").append(IHaysSearchConstants.IS_ADVANCE_SEARCH).append("=").append(isAdvanceSearch);
        }
        //added for r7 alert form requirement
        if( jobPostCode != null && !jobPostCode.equalsIgnoreCase("")){
        	parameters.append("&").append(IHaysSearchConstants.JOB_POST_CODE).append("=").append(jobPostCode);
        }
        
      //added for r8 
        if( locationSet != null && !locationSet.equalsIgnoreCase("")){
        	parameters.append("&").append("location_set").append("=").append(locationSet);
        }
    		
        SystemUtils.trace(TRACE_NAME, "apiValidateSearchParams() binder After:  " + parameters); 
        // check mandatory parameters
        if( !isTitleSet()  && (industry == null || industry.trim().length() == 0)
        		&& (keywords == null || keywords.trim().length() == 0)        		
        		&&   (job_category == null || job_category.trim().length() == 0) 
        		&&   (location_id == null || location_id.trim().length() == 0)) 
        	{
        	
        	
			this.m_binder.putLocal("StatusMessage", LocaleUtils.encodeMessage("wwInvalidSearchParameters", null));
			this.m_binder.putLocal("StatusCode", "UC0015");		
			throw new ServiceException(LocaleUtils.encodeMessage("wwInvalidSearchParameters", null));
			
       	} /*else { // validate salary
	        
	        	if( (jobTypeP != null && !isValid(minP, maxP, typeP)) || 
	        			(jobTypeC != null && !isValid(minC, maxC, typeC)) ||
	        			( jobTypeT != null && !isValid(minT, maxT, typeT) ) )
	        	{
	        		
	    			this.m_binder.putLocal("StatusMessage", LocaleUtils.encodeMessage("wwInvalidSalaryParameters", null));
	    			this.m_binder.putLocal("StatusCode", "UC0016");		 
	    			throw new ServiceException(LocaleUtils.encodeMessage("wwInvalidSalaryParameters", null));
	        		
	        	}
	         
        }*/
        
     
        if( linkedInJob != null && !linkedInJob.equalsIgnoreCase("")){
        	parameters.append("&").append("linkedIn_job").append("=").append(linkedInJob);
        }
        
        if( jobExperienceLevel != null && !jobExperienceLevel.equalsIgnoreCase("")){
        	parameters.append("&").append("experience_Level").append("=").append(jobExperienceLevel);
        }
    		
        this.m_binder.putLocal("SearchQueryParamas", parameters.toString());		
	}	
}

	public String getData(String pParamName)
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

		}
		return returnString;
	}
	
	public String getSuperData(String pParamName)
	{
		String returnString = "";
		try
		{
			returnString = super.m_binder.get(pParamName);
			if("null".equalsIgnoreCase(returnString))
			{
				returnString = "";
			}
			
		}
		catch (Exception e)
		{

		}
		return returnString;
	}
	
	public String getData(String pParamName, String nullReturn)
	{
		String returnString = nullReturn;
		try
		{
			returnString = m_binder.get(pParamName);
			if("null".equalsIgnoreCase(returnString))
			{
				returnString = nullReturn;
			}
			
		}
		catch (Exception e)
		{

		}
		return returnString;
	}
	
}

