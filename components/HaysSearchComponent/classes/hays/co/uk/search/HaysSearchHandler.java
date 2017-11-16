package hays.co.uk.search;



import intradoc.common.ClassHelper;
import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.data.DataBinder;
import intradoc.data.DataException;
import intradoc.data.ResultSet;
import intradoc.server.Service; 
import intradoc.server.ServiceHandler;
import intradoc.shared.SharedObjects;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import infomentum.ontology.navigation.OntologyNavigationHandler;
import hays.co.uk.search.IHaysSearchConstants.*;

public class HaysSearchHandler extends ServiceHandler {
	
	
	//public static final char[] specialCharacters = {']', '[','\\', '^', '$', ',', '.', '|', '?', '*', '+', '(', ')', '{', '}'};
	
	public void processSearchParameters() throws DataException, ServiceException {
		super.m_binder.putLocal("SearchQueryFormat", "universal");
		
		//fix for google bots etc still using sortfield in init case
		String sortfield = super.m_binder.getLocal(IHaysSearchConstants.SORTFIELD);
		if("Score".equals(sortfield)){
			super.m_binder.putLocal(IHaysSearchConstants.SORTFIELD, "SCORE");
		}
		//end fix
		
		// EXCLUDE
		String exclude = super.m_binder.getLocal(IHaysSearchConstants.EXCLUDE);
		// QUERY
		StringBuffer query = new StringBuffer();
		StringBuffer queryPartforSearchPOC=new StringBuffer();
		StringBuffer queryPart = new StringBuffer();
		
		//added by devendra  start
		String isHomePageSearch = super.m_binder.getLocal(IHaysSearchConstants.IS_HOME_SEARCH);
		SystemUtils.trace("hays_search","isHomePageSearch:::"+isHomePageSearch); 
		// added by denvendra  end
		String isAdvanceSearch = super.m_binder.getLocal(IHaysSearchConstants.IS_ADVANCE_SEARCH); // added for r7
		SystemUtils.trace("hays_search","isAdvanceSearch:::"+isAdvanceSearch); 
		String alertprofileid=super.m_binder.getLocal("AlertProfileID");
		SystemUtils.trace("hays_search","alertprofileid is:::"+alertprofileid); 
		String level = super.m_binder.getLocal(IHaysSearchConstants.LEVEL);
		String[] RelaxFullTextCountryList = SharedObjects.getEnvironmentValue("RelaxFullTextCountryList").split(",");
		String DefaultRadius = SharedObjects.getEnvironmentValue("DefaultRadius");
		String siteLocale = m_binder.getLocal(IHaysSearchConstants.LOCALE);
		String radius = super.m_binder.getLocal(IHaysSearchConstants.RADIUS);
        boolean relaxFullTextCountry = false;
        for(String s:RelaxFullTextCountryList)
        {
        	if(siteLocale.endsWith(s))
        		relaxFullTextCountry = true;
        }	
        
		if( "Hays".equals(super.m_binder.getLocal("searchType"))) {
			SystemUtils.trace("hays_search", "\n\nStarting search process: " + m_binder.getLocalData());
			SystemUtils.trace("hays_search", "\nExclude : " + exclude);
	        // KEYWORDS
	        String keywords = super.m_binder.getLocal(IHaysSearchConstants.JOB_KEYWORDS);	        
	        super.m_binder.putLocal(IHaysSearchConstants.IS_FUZZY_SEARCH,"true");// Fuzzy true for all countries(set true for alert requirement)
	        String isFuzzyStr = super.m_binder.getLocal(IHaysSearchConstants.IS_FUZZY_SEARCH);
	        String isOnlyJobTitleStr = super.m_binder.getLocal(IHaysSearchConstants.IS_ONLY_JOB_TITLE);
			SystemUtils.trace("hays_search", "\nisFuzzyStr : " + isFuzzyStr+" \n isOnlyJobTitle : " + isOnlyJobTitleStr);
			String thes_locale = m_binder.getLocal(IHaysSearchConstants.LOCALE);    
			SystemUtils.trace("hays_search", "thes_locale"+thes_locale);
			int localeIndex=thes_locale.indexOf('-');  
			String thes_name = thes_locale.substring(localeIndex+1, thes_locale.length());
			SystemUtils.trace("hays_search", "Thesaurus Name: " + thes_name);			
			
			boolean isFuzzy = (isFuzzyStr!=null &&  "true".equals(isFuzzyStr))?true:false;
        	boolean isOnlyJobTitle = (isOnlyJobTitleStr!=null &&  "Y".equals(isOnlyJobTitleStr))?true:false;
			
	        //Start POC Builder
	        if( keywords != null) {
	        		        	
		        QueryBuilderFactory queryBuilderFactory = QueryBuilderFactory.getInstance(); 
		        
				
		        AbstractHaysQueryBuilder abstractHaysQueryBuilder = queryBuilderFactory.getQueryBuilder(isFuzzy, isOnlyJobTitle, thes_name,
		        		alertprofileid,level,isAdvanceSearch,relaxFullTextCountry,radius); 
		        abstractHaysQueryBuilder.setInputString(keywords);
		        abstractHaysQueryBuilder.buildQueryPart();
		        queryPart = abstractHaysQueryBuilder.getQueryPart();
		        // added by devendra start
		        SystemUtils.trace("hays_search", "Trace queryPart : " + queryPart.toString()+"isOnlyJobTitleStr::"+isOnlyJobTitleStr+"ishomepagesearch=="+isHomePageSearch);
		        SystemUtils.trace("hays_search", "zzzzzzzzzzzTrace queryPart final : " + queryPart.toString()+isOnlyJobTitleStr);
		        //added for r7 end
		        
		        
	        }
	        // added by devendra end
	        String ref = super.m_binder.getLocal(IHaysSearchConstants.JOB_TITLE_REF);
	        query.append("((( ");
	        if( queryPart.length() > 0 )
	        {
	        	if( query.length() > 0)
        		{
        			query.append(" (idccontenttrue)*10*10 AND ");
        		}
	        query.append("(").append(queryPart);
//		         if(isOnlyJobTitle) 
	        
		        	 if(ref == null || "".equals(ref) || ((alertprofileid!=null && alertprofileid.length()!=0))){
		        		 query.append(")");
		        	 }
//		         if((!isOnlyJobTitle())&& ((alertprofileid!=null && alertprofileid.length()!=0)))  //When alert request is sent
//				   {
//					  fuzzyJobTitleQueryBuffer.append(")");
//				   }
	        }
	        else
        	{
        	query.append("(idccontenttrue)*10*10");
        	
        	}

	        //End POC Builder
	        
	        
	        
//	        if( keywords != null ) {
//	        	
//	        	SystemUtils.trace("hays_search", "First time keywords=" + keywords );
//
//	        	keywords = decodeHaysSpecialKeywords(keywords);
//		        
//	        	keywords = keywords.trim();
//	        	keywords = formatStringForReservedKeyWords(keywords);
//	        	if( keywords.length() > 0 ) {
//        			StringBuffer keyBuffer = new StringBuffer();
//        			String[] arr = keywords.split("[ ][ ]*");
//        			for(int i = 0; i< arr.length; i++){
//        				keyBuffer.append(arr[i].trim()).append("&");
//        			}
//        			keywords = keyBuffer.toString();
//        			keywords = keywords.replaceAll("[&]?[,][&]?", "|");
//        			if(keywords.endsWith("&"))
//        				keywords = keywords.substring(0, keywords.length()-1);
//        			if(keywords.endsWith("|"))
//        				keywords = keywords.substring(0, keywords.length()-1);
//	        	//	keywords = keywords.replaceAll("[ ][ ]?", "&");
//	        	//	keywords = keywords.replaceAll("[,][ ]?", "|");
//	        		queryPart.append("( ").append(keywords).append(" )");
//	        		//queryPart.append(" OR (").append(keywords).append(") OR ");
//	        		queryPart.append(" ACCUM (").append(keywords).append(") WITHIN xKeywords ACCUM ");
//	        		queryPart.append(" (").append(keywords).append(") WITHIN dDocTitle");
//		        }
//	        	else {
//	        		query.append("idccontenttrue ");
//	        	}
//	        }
//        	else {
//        		query.append("idccontenttrue ");
//        	}
//	        if( queryPart.length() > 0 ){
//	        	if( query.length() > 0)
//	    			query.append(" AND ");
//	        	query.append("(").append(queryPart).append(")");
//	        }
//	        
		
			//JOB REFERENCE
			
			SystemUtils.trace("hays_search", "JOb Title Reference"+ref);				
	        if( ref != null  && ref.length() > 0 ) {
	        	ref = ref.replaceAll(IHaysSearchConstants.specialCharacters, " ");
	        	ref = ref.trim();
	        	ref = QueryUtils.decodeHaysSpecialKeywords(ref);
	        	ref = QueryUtils.formatStringForReservedKeyWords(ref);
	        	SystemUtils.trace("hays_search", "ref length"+ref.length());
	            SystemUtils.trace("hays_search", "ref trimmed length"+ref.trim().length());
	        	
	        	if( ref.length() > 0)
	        	{
	        		ref = ref.replaceAll(IHaysSearchConstants.fuzzySpecialCharacters, " ");
	        		ref = ref.replaceAll("(?i)[&;!>]| AND ", " ");
					SystemUtils.trace("hays_search", "ref String Value"+ref);
					ref = ref.replaceAll("(?i)[|]| OR ", ",");
	        		ref = ref.replaceAll("\"", "");
	        		ref=ref.trim();
	        		ref = ref.replaceAll("[, ][ ][ ]*", " | ");
		        			        	
		        	// added by devendra  start	
			        	if(alertprofileid==null )
		        		{
			        		query.append(" ACCUM (").append(ref).append(") WITHIN xRecordId)  ");
			        		SystemUtils.trace("hays_search", "inside if  for home page , query::" +query);
		        		}
			        	
			        		        	  	
			        	else{ //alert scenario
			        		// added by devendra  end
		        			query.append(" AND ((").append(ref).append(") WITHIN xRecordId)  ");
		        		 // added by devendra
			        	}
		        	}
	        } 
	        else 
	        {
			
				// JOB TITLE 
		        
		        // Job Function ID
		       /* String jobTitle = super.m_binder.getLocal(IHaysSearchConstants.JOB_TITLE_FULL);
		        StringBuffer jobTitlePart = new StringBuffer();
		        if( jobTitle != null && jobTitle.trim().length() > 0) {
		        	jobTitle = jobTitle.trim();
		        	jobTitle = QueryUtils.decodeHaysSpecialKeywords(jobTitle);
		        	jobTitle = QueryUtils.formatStringForReservedKeyWords(jobTitle);
	        		jobTitle = jobTitle.replaceAll(",[ ]?", " | ");
	        		jobTitlePart.append("((").append(jobTitle).append(") WITHIN xAutomatedTitle)  ");
			      
		        } As xAutomatedTitle has been removed from the SDATA section*/
		        //Textual Description
		       String jobTitle = super.m_binder.getLocal(IHaysSearchConstants.JOB_TITLE);
		       StringBuffer jobTitlePart = new StringBuffer();
		        if( jobTitle != null && jobTitle.trim().length() > 0) 
		        {
			        String fuzzyJobTitle = new String(jobTitle);
		        	jobTitle = jobTitle.replaceAll(IHaysSearchConstants.specialCharacters, " ");	
		        	fuzzyJobTitle = fuzzyJobTitle.replaceAll(IHaysSearchConstants.fuzzySpecialCharacters, " ");
			        SystemUtils.trace("hays_search", "processSearchParameters: fuzzyJobTitle 2 ='" + fuzzyJobTitle + "'");
		        	
		        	jobTitle = jobTitle.trim();
		        	fuzzyJobTitle = fuzzyJobTitle.trim();
			        SystemUtils.trace("hays_search", "processSearchParameters: fuzzyJobTitle 3 ='" + fuzzyJobTitle + "'");
		        	
		        	jobTitle = QueryUtils.decodeHaysSpecialKeywords(jobTitle);
		        	fuzzyJobTitle = QueryUtils.decodeHaysSpecialKeywords(fuzzyJobTitle);
			        SystemUtils.trace("hays_search", "processSearchParameters: fuzzyJobTitle 4 ='" + fuzzyJobTitle + "'");
		        	
		        	jobTitle = QueryUtils.formatStringForReservedKeyWords(jobTitle);
		        	//fuzzyJobTitle = QueryUtils.formatStringForReservedKeyWords(fuzzyJobTitle);
			        //SystemUtils.trace("hays_search", "processSearchParameters: fuzzyJobTitle 5 ='" + fuzzyJobTitle + "'");
		        	
	        		if( jobTitlePart.length() > 0 )
	        			jobTitlePart.append(" ACCUM ");
	        		
	        		//StringBuffer fuzzyJobTitleBuffer = new StringBuffer();
	        		String fuzzyJobTitleQuotes=fuzzyJobTitle;
	        		/*updated for MR 209*/
	        			int fuzzyJobTitleLenght = fuzzyJobTitle.length();
	        			if(fuzzyJobTitle.charAt(0) =='\"' && fuzzyJobTitle.charAt(fuzzyJobTitleLenght-1) =='\"' ){
	        				fuzzyJobTitle = fuzzyJobTitle.replaceAll("\"", "");
	        				//fuzzyJobTitle = fuzzyJobTitle.substring(1, fuzzyJobTitleLenght-1);
	        			}
	        			fuzzyJobTitle = fuzzyJobTitle.replaceAll("(?i)[&;!>]| AND ", " ");
	        			fuzzyJobTitle = fuzzyJobTitle.replaceAll("(?i)[|]| OR ", ",");
	        			
		        			StringTokenizer commaTokenizer = new StringTokenizer(fuzzyJobTitle,",");
		        			int count=0;
		        			while(commaTokenizer.hasMoreTokens()){
		        				if(count>0)
		        				{
		        				    SystemUtils.trace("hays_search", "Count is greater than 0");
		        				    jobTitlePart.append("ACCUM");
		        				  	count=count-1;
		        				}
		        				String token = commaTokenizer.nextToken().trim();
		    			        SystemUtils.trace("hays_search", "processSearchParameters: fuzzyJobTitle Inside Token   ='" + token + "'");
		    			        token = token.replaceAll("-", "\\\\-");
		    			        token = token.replaceAll("_", "\\\\_");
		    			        token = token.replaceAll("\"", "");
		    			       
		    			        jobTitlePart
		        				.append("(fuzzy(")
		        				.append(token.replaceAll("[ ][ ]*","+"))
		        				.append(",,6,N) WITHIN dDocTitle) ACCUM ");
		    			        
		    			                    				            					            			            				
		            				
		    			        	SystemUtils.trace("hays_search", "Job Title part=" + jobTitlePart.toString());
		            			/*end updated for MR 209*/

		    			        jobTitle = token.replaceAll("[ ]+", " ");	
		    			        SystemUtils.trace("hays_search","JObTitle Value"+jobTitle);
		    		        	//jobTitlePart.append(" (").append(jobTitle).append(") WITHIN dDocTitle ");
		    		        	jobTitlePart.append(" ($(SYN(").append(jobTitle).append(", ").append(thes_name).append(") WITHIN dDocTitle ))*4 ACCUM (SYN (").append(jobTitle).append(", ").append(thes_name).append(") WITHIN dDocTitle )");
		    		        	// added for stemming
		    		        	if(	fuzzyJobTitleQuotes.charAt(0) =='\"' && fuzzyJobTitleQuotes.charAt(fuzzyJobTitleQuotes.length()-1) =='\"' )
		    	        		{
		    		        		jobTitlePart.append(" ACCUM (($(").append(jobTitle).append(")) WITHIN dDocTitle )*2 ");
		    		        		SystemUtils.trace("hays_search","JobtitlePart"+jobTitlePart);
		    			            
		    	        		}
		    		        	else{
		    		            jobTitle = jobTitle.replaceAll(" ", " and \\$ ");
		    		        	jobTitlePart.append(" ACCUM (($").append(jobTitle).append(") WITHIN dDocTitle )*2 ");
		    		        	SystemUtils.trace("hays_search","JobtitlePart"+jobTitlePart);
		    		        	}
		    		        	count=count+1;

		        			}
		        			//jobTitlePart.append(fuzzyJobTitleBuffer.toString());
	        			
	        			
	        			
	        			
	        		
	        		
		        }
		        if( jobTitlePart.length() > 0 ){
		        	if( query.length() > 0)
		        			query.append(" AND ");	
		        	query.append(" ( ").append(jobTitlePart).append(")");
		        	SystemUtils.trace("hays_search", "QUery After aapending job title prt"+query);
		        }
		        SystemUtils.trace("hays_search", "Job Title part after appending brackets=" + jobTitlePart.toString());
		      /* if( ((isOnlyJobTitleStr!=null)&& (isFuzzyStr==null || "".equalsIgnoreCase(isFuzzyStr) || "false".equalsIgnoreCase(isFuzzyStr))))
		          {
		        	SystemUtils.trace("hays_search", "Inside Non-Fuzzy");
		        	// condition added to avoid adding ")" to a query string containing only "idccontenttrue"
		        	if(!("idccontenttrue").equalsIgnoreCase(query.toString().trim()))
		        	{
		        		SystemUtils.trace("hays_search", "adding )");
		        		query.append(")");
		        	}
		        	//query.append(")"); moved this statement in the above condition
		          }*/
		        jobTitlePart = null;
	        }
	        
	        SystemUtils.trace("hays_search", "processSearchParameters: Job Title='" + query + "'");
	        int index=query.indexOf("AND");
	        if(index>0)
	        {
	        queryPartforSearchPOC.append(query.substring(index)).append("*10*10");
	        }
	        SystemUtils.trace("hays_search", "QueryPartforSEarchPOC"+queryPartforSearchPOC);
	        
	     // INDUSTRY
	        String industriesStr = super.m_binder.getLocal(IHaysSearchConstants.JOB_INDUSTRY);
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
	    			SystemUtils.trace("hays_search", "In  industries other than " + industriesStr);
	    		} else {        
		    		if( query.length() > 0)
		    		{
		    			query.append(" AND ");
		    			queryPartforSearchPOC.append(" AND ");

		    		}
		    	}
	    		query.append("((").append(industriesStr).append(") WITHIN xIndustry )*10*10"); //maxed out score
	        	queryPartforSearchPOC.append("((").append(industriesStr).append(") WITHIN xIndustry )*10*10"); 
		        SystemUtils.trace("hays_search", "processSearchParameters: Industry='" + query + "'");
	        }
	        
	        // CATEGORY
	        String categories = super.m_binder.getLocal(IHaysSearchConstants.JOB_CATEGORY);
	        if( categories != null && categories.trim().length() > 0 ) 
	        {
	        	     	
	        	// added for r7 start
	        	StringBuffer result = new StringBuffer();
	        	//if(isAdvanceSearch!=null && isAdvanceSearch.equalsIgnoreCase("1"))
	        	if((isAdvanceSearch!=null && isAdvanceSearch.equalsIgnoreCase("1")) || (IHaysSearchConstants.AlertProfileID!=null && !(IHaysSearchConstants.AlertProfileID.equalsIgnoreCase(" "))))
        		{		    
		        	StringBuffer  finalsubTerms=new StringBuffer();
		        	SystemUtils.trace("hays_search", "categories 1st =" + categories);
		        	
		        	if( categories.startsWith(";"))
		        		categories = categories.substring(1);
		        	if( categories.endsWith(";"))
		        		categories = categories.substring(0, categories.length()-1);
		        	    categories = categories.replaceAll("[,;][ ]?", "#");
		        	
	        		 //categories = categories.substring(0, categories.length());
	      
	        		 SystemUtils.trace("hays_search", "categories after replace all=" + categories);
	        		 //categories = categories.replaceAll(";", ",");
	        		 //SystemUtils.trace("hays_search", "categories after replace all=" + categories);
	        		 
	        		 String [] categoriesArray = categories.split("#");
	        		 
	        		 for(int i=0;i<categoriesArray.length;i++)
	        		 {
	        		 
	        			 		String  subTerms = ontGetRelatedTerms(categoriesArray[i], "xCategory@hays:ParentTerm");
				        		 SystemUtils.trace("hays_search", "subTerms=" + subTerms);
				        		
				        		if("[]".equalsIgnoreCase(subTerms))
				        		{
				        			subTerms = categoriesArray[i];
				        		}
				        		 if(subTerms.length()>0)
				        		{ 
				        			subTerms = categoriesArray[i] + "," + subTerms.substring(1, subTerms.length()-1);
				        		}
				        		 SystemUtils.trace("hays_search", "for i=="+i+"  ,subTerms=" + subTerms);
				        		 if(i>0)
				        		 {
				        			 finalsubTerms.append(" | ");
				        		 }
				        		 finalsubTerms.append(subTerms);
				        		 SystemUtils.trace("hays_search", "for i=="+i+"  ,finalsubTerms=" + finalsubTerms.toString());		 
	        		 }
	        		 SystemUtils.trace("hays_search", "final  finalsubTerms=" + finalsubTerms.toString());	
	        		categories = finalsubTerms.toString();	
	        		categories = categories.replaceAll("[,;#][ ]?", " | ");	
	        		// added for r7 end
		        		
	        	}
	        	else
	        	{
	        		if( categories.startsWith(";"))
		        		categories = categories.substring(1);
		        	if( categories.endsWith(";"))
		        		categories = categories.substring(0, categories.length()-1);
		        	categories = categories.replaceAll("[,;][ ]?", " | ");
			        	
	        	}
	        	if( query.length() > 0) 
	    		{
	    			query.append(" AND ");
	    			queryPartforSearchPOC.append(" AND ");
	    		}
	        	query.append("((").append(categories).append(") WITHIN xCategory )*10*10");
	        	queryPartforSearchPOC.append("((").append(categories).append(") WITHIN xCategory )*10*10");
		        SystemUtils.trace("hays_search", "processSearchParameters: Category='" + query + "'");
	        	
	        }
	        
	      //POSTED DATE
	        
	        //added for R7 
	        String posteddate=super.m_binder.getLocal(IHaysSearchConstants.JOB_POSTED_DATE_FILTER);
	       // String posteddateexclude = super.m_binder.getLocal(IHaysSearchConstants.JOB_POSTED_DATE_EXCLUDE);
	        SystemUtils.trace("hays_search", "JOB_POSTED_DATE_FILTER = ====" + posteddate );
	        if( posteddate != null && posteddate.trim().length() > 0 ) 
	        {
	        	int indexposted=posteddate.indexOf(',');
	        	SystemUtils.trace("hays_search", "Inside excluddde JOB_POSTED_DATE_FILTER = ====" + indexposted );
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
	        		//String posteddateexclude=posteddate.substring(index,posteddate.length());
	        		SystemUtils.trace("hays_search", "processSearchParameters: posteddateexclude ='"+posteddate);
	        		if( query.length() > 0)
	        		{
		    			query.append(" AND ");
		    			queryPartforSearchPOC.append(" AND ");
	        		}
			        query.append(" (").append(" SDATA(xEventDate > '").append(posteddate).append("'))");
			        queryPartforSearchPOC.append(" (").append(" SDATA(xEventDate > '").append(posteddate).append("'))");
	        		
	        	}
	        }
	        SystemUtils.trace("hays_search", "processSearchParameters: posteddateexclude ='"+posteddate+", " + query + "'");        
      
	        
	        //end Posted Date
    
	        // PERMANENT
	        StringBuffer salaryBuffer = new StringBuffer();
	        String type, min, max, permPayType, salary = null;
	        type = super.m_binder.getLocal(IHaysSearchConstants.JOB_PERM);
	        if( type != null ) {
	        	if( type.length() > 0 ) {
	        		min = super.m_binder.getLocal(IHaysSearchConstants.JOB_MIN_PERM);
	        		max = super.m_binder.getLocal(IHaysSearchConstants.JOB_MAX_PERM);
	        		permPayType = super.m_binder.getLocal(IHaysSearchConstants.JOB_SELECT_PERM);
	        		if(permPayType == null)
	        			permPayType="A";	        		      		
	        		salary = processSalary(min, max, permPayType, IHaysSearchConstants.PERMANENT);
	        		if( salary != null && salary.length() > 0 )
	        			salaryBuffer.append("(").append(salary).append(")");
		        }
	        }
	        
	        boolean runAsUsual = true;
	        
	        String[] OldFlowCountryList = SharedObjects.getEnvironmentValue("OldFlowCountryList").split(",");
	        SystemUtils.trace("hays_search", "siteLocale :: " + siteLocale);
	        SystemUtils.trace("hays_search", "OldFlowCountryList :: " + OldFlowCountryList);
	        for(String s:OldFlowCountryList)
	        {
	        	if(siteLocale.endsWith(s))
	        		runAsUsual = false;
	        }
	        if (runAsUsual)
			{
				/** Start PCR 110 - section 5.1 - 5.1	Grouping of Temporary and Contract Results*/
				StringBuffer queryBuffer = mergeTempAndContract();
				if (queryBuffer != null)
				{
					if (salaryBuffer.length() > 0)
					{
						salaryBuffer.append(" OR ");
					}
					salaryBuffer.append("(").append(queryBuffer).append(")");
				}
				SystemUtils.trace("hays_search", "EXISTING SALARY BUFFER :: " + salaryBuffer);
			}
	        else
	        {	        	
	        	// CONTRACT
	        	type = super.m_binder.getLocal(IHaysSearchConstants.JOB_CONTRACT);
	        	if( type != null ) {
	        		if( type.length() > 0 ) {
	        			min = super.m_binder.getLocal(IHaysSearchConstants.JOB_MIN_CONTRACT);
	        			max = super.m_binder.getLocal(IHaysSearchConstants.JOB_MAX_CONTRACT);
	        			permPayType = super.m_binder.getLocal(IHaysSearchConstants.JOB_SELECT_CONTRACT);
	        			if(permPayType == null){
	        				permPayType= setTempContractSalaryType(permPayType);
	        			}
	        			salary = processSalary(min, max, permPayType, IHaysSearchConstants.CONTRACT);
	        			if( salary != null && salary.length() > 0 ) {
	        				if( salaryBuffer.length() > 0 ) {
	        					salaryBuffer.append(" OR ");
	        				} 
	        				salaryBuffer.append("(").append(salary).append(")");
	        			}
	        		}
	        	}
	        	// TEMPORARY
	        	type = super.m_binder.getLocal(IHaysSearchConstants.JOB_TEMP);
	        	if( type != null ) {
	        		if( type.length() > 0 ) {
	        			min = super.m_binder.getLocal(IHaysSearchConstants.JOB_MIN_TEMP);
	        			max = super.m_binder.getLocal(IHaysSearchConstants.JOB_MAX_TEMP);
	        			permPayType = super.m_binder.getLocal(IHaysSearchConstants.JOB_SELECT_TEMP);
	        			if(permPayType == null){
	        				permPayType= setTempContractSalaryType(permPayType);
	        			}
	        			salary = processSalary(min, max, permPayType, IHaysSearchConstants.TEMPORARY);
	        			if( salary != null && salary.length() > 0 ) {
	        				if( salaryBuffer.length() > 0 ) {
	        					salaryBuffer.append(" OR ");
	        				} 
	        				salaryBuffer.append("(").append(salary).append(")");
	        			}
	        		}
	        	}
	        	SystemUtils.trace("hays_search", "BELGIUM SALARY BUFFER :: " + salaryBuffer);
	        }
	        
	        /** END PCR 110 - section 5.1 - 5.1	Grouping of Temporary and Contract Results*/
	        // GENERAL
	        permPayType = super.m_binder.getLocal(IHaysSearchConstants.JOB_RATE);
	        if( permPayType != null && permPayType.length() > 0) {
	        	min = super.m_binder.getLocal(IHaysSearchConstants.JOB_MIN_SALARY);
	        	max = super.m_binder.getLocal(IHaysSearchConstants.JOB_MAX_SALARY);
	        	if( min != null || max != null) {
	        		salary = processSalary(min, max, permPayType, null);
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
	        SystemUtils.trace("hays_search", "processSearchParameters: Salaries='" + query + "'");
	        
	        // CONTENT TYPE
	        String contentType = super.m_binder.getLocal(IHaysSearchConstants.CONTENT_TYPE);
	        if( "Jobs".equals(contentType) || "Candidates".equals(contentType)){
	        	if( query.length() > 0){
	        			query.append(" AND ");
	        			queryPartforSearchPOC.append(" AND ");
	        	}
	        	query.append(" (").append(contentType).append(" WITHIN dDocType)*10*10");
	        	queryPartforSearchPOC.append(" (").append(contentType).append(" WITHIN dDocType)*10*10");
	        }
	        SystemUtils.trace("hays_search", "processSearchParameters: Content Type ='"+contentType+", " + query + "'");
	        
	        
	        
	        // RELEASE DATE
	        String releaseDate = super.m_binder.getLocal(IHaysSearchConstants.RELEASE_DATE);
	        if( releaseDate != null && releaseDate.length() >0){
	        	if( query.length() > 0){
        			query.append(" AND ");
        			queryPartforSearchPOC.append(" AND ");
	        	}
	        	query.append(" (").append(" SDATA(dInDate > '").append(releaseDate).append("'))*10*10");
	        	queryPartforSearchPOC.append(" (").append(" SDATA(dInDate > '").append(releaseDate).append("'))*10*10");
	        }
	        SystemUtils.trace("hays_search", "processSearchParameters: Release Date ='"+releaseDate+", " + query + "'");
	        
	       
	        
	     // Registered Date
	        String registeredDate = super.m_binder.getLocal(IHaysSearchConstants.REGISTERED_DATE);
	        if( registeredDate != null && registeredDate.length() >0){
	        	if( query.length() > 0){
        			query.append(" AND ");
        			queryPartforSearchPOC.append(" AND ");
	        	}
	        	query.append(" (").append(" SDATA(dInDate > '").append(registeredDate).append("'))*10*10");
	        	queryPartforSearchPOC.append(" (").append(" SDATA(dInDate > '").append(registeredDate).append("'))*10*10");
	        }
	        SystemUtils.trace("hays_search", "processSearchParameters: Registered Date ='"+registeredDate+", " + query + "'");
	        
	        
	     // LOCALE
			String locale = m_binder.getLocal(IHaysSearchConstants.LOCALE);
			if( locale != null && locale.trim().length() > 0){
				query.append(" AND ({").append(locale).append("} WITHIN xLocale)*10*10");
				queryPartforSearchPOC.append(" AND ({").append(locale).append("} WITHIN xLocale)*10*10");
			}
			
		 //	MR 219
		 // MicrositeCode
			String micrositecode = m_binder.getLocal(IHaysSearchConstants.micrositeCode);
			if( micrositecode != null && micrositecode.trim().length() > 0){
				query.append(" AND ({").append(micrositecode).append("} WITHIN xMicroSiteCode)*10*10");
				queryPartforSearchPOC.append(" AND ({").append(micrositecode).append("} WITHIN xMicroSiteCode)*10*10");
			}
		 //end MR 219	
	        
	   
        
			// LOCATION		
			m_binder.putLocal(IHaysSearchConstants.EXCLUDE, "0");
			String latitudeF = super.m_binder.getLocal(IHaysSearchConstants.NE_LATITUDE_FILTER);
			String longitudeF = super.m_binder.getLocal(IHaysSearchConstants.NE_LONGITUDE_FILTER);
			String radiusF = super.m_binder.getLocal(IHaysSearchConstants.RADIUS_FILTER);
			String levelF = super.m_binder.getLocal(IHaysSearchConstants.LEVEL_FILTER);
			
			//String locationids = super.m_binder.getLocal("locationid");
			//SystemUtils.trace("hays_search","Location id values is"+locationids);
			String latitude = super.m_binder.getLocal(IHaysSearchConstants.NE_LATITUDE);
			String longitude = super.m_binder.getLocal(IHaysSearchConstants.NE_LONGITUDE);
			
			level = super.m_binder.getLocal(IHaysSearchConstants.LEVEL);
			int count =0;
			SystemUtils.trace("hays_search", "relaxFullTextCountry. " +relaxFullTextCountry);
			SystemUtils.trace("hays_search", "level. " +level);
			SystemUtils.trace("hays_search", "alertprofileid. " +alertprofileid);
			SystemUtils.trace("hays_search", "isAdvanceSearch. " +isAdvanceSearch);
			SystemUtils.trace("hays_search", "radius. " +radius);
	        if (relaxFullTextCountry && level !=null && "6".equalsIgnoreCase(level.trim()))
			{
				if ((alertprofileid!=null && (radius.equals("0") || radius.equals(DefaultRadius))) ||
						(isAdvanceSearch!=null && radius.equals("0")) ||
						(isAdvanceSearch==null))
				{
					radius = "0";
					SystemUtils.trace("hays_search", "RADIAL 0 EXECUTED.");
				}
			}
	        
			String location_set = super.m_binder.getLocal("location_set"); // Will be non-empty only for APAC countries.
			SystemUtils.trace("hays_search", "location set"+location_set);
			if(location_set!=null && !location_set.equals(""))
			{	
				String location_details [] = location_set.split("@");
				SystemUtils.trace("hays_search", "Inside if condition");
				for(int i = 0 ;i<location_details.length; i++)
				{
					SystemUtils.trace("hays_search","location_set.trim().length()"+location_set.trim().length());
					
					SystemUtils.trace("hays_search", "Location details array"+location_details[0]);
					//for(int j=0 ; j< location_details.length; j++)
					
					@SuppressWarnings("unused")
					String location1 [] = location_details[i].split(",");
					SystemUtils.trace("hays_search", "location1[0]"+location1[0]+location1.length);
					level = location1[2];
					longitude = location1[3];
					latitude = location1[4];
						 
					SystemUtils.trace("hays_search", "level,longitude and latitude" + level + "," +longitude +" , " + latitude);
					
					/**if( exclude != null && exclude.contains( IHaysSearchConstants.JOB_LOCATION)) {
						m_binder.putLocal(IHaysSearchConstants.EXCLUDE, "1");
						SystemUtils.trace("hays_search", "exclude Value"+exclude);
						if( longitudeF != null && !longitudeF.equals(IHaysSearchConstants.DEFAULT))
							processLocation(longitudeF, latitudeF, radiusF, "_filter");
						else{
							SystemUtils.trace("hays_search", "Inside the else condition");
							processLocation(longitude, latitude, radius, "");
						}
					}else {**/
					SystemUtils.trace("hays_search", "Inside the first else condition");
					// if filter coordinates and level are known then build a query
					if( latitudeF != null && latitudeF.trim().length() > 0 && !latitudeF.equals(IHaysSearchConstants.DEFAULT) &&
								longitudeF != null && longitudeF.trim().length() > 0 && !longitudeF.equals(IHaysSearchConstants.DEFAULT)
								&& levelF != null && levelF.trim().length() > 0){
						SystemUtils.trace("hays_search", "Inside second if condidtion");
						processLocation(null, null, null, "");
						query.append(" AND ").append( constructLocationQuery(longitudeF, latitudeF, levelF) );	
						queryPartforSearchPOC.append(" AND ").append( constructLocationQuery(longitudeF, latitudeF, levelF) );
						SystemUtils.trace("hays_search", "append location filter to query: " + longitudeF + ", " + latitudeF + ", " + levelF);
						
					} 
						
					SystemUtils.trace("hays_search", "Inside the second else condition");
					processLocation(null, null, null, "");
					
					if (longitude != null && longitude.trim().length() > 0 && !longitude.equals(IHaysSearchConstants.DEFAULT)
							&& latitude != null && latitude.trim().length() > 0 && !latitude.equals(IHaysSearchConstants.DEFAULT)
							&& level!= null && level.trim().length() > 0 && !level.equals(IHaysSearchConstants.DEFAULT)) {
						SystemUtils.trace("hays_search", "Inside 4th if condition: " + longitude + ", " + latitude + ", " + radius + ", level" + level);
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
						SystemUtils.trace("hays_search", "Query after adding location "+query);
					}
					count++;	
				}
				query.append(")"); //Closing bracket
				queryPartforSearchPOC.append(")");
			}
			else
			{
				if( exclude != null && exclude.contains( IHaysSearchConstants.JOB_LOCATION)) {
					m_binder.putLocal(IHaysSearchConstants.EXCLUDE, "1");
					SystemUtils.trace("hays_search", "exclude Value"+exclude);
					if( longitudeF != null && !longitudeF.equals(IHaysSearchConstants.DEFAULT))
						processLocation(longitudeF, latitudeF, radiusF, "_filter");
					else
						processLocation(longitude, latitude, radius, "");
				}else {
					// if filter coordinates and level are known then build a query
					if( latitudeF != null && latitudeF.trim().length() > 0 && !latitudeF.equals(IHaysSearchConstants.DEFAULT) &&
							longitudeF != null && longitudeF.trim().length() > 0 && !longitudeF.equals(IHaysSearchConstants.DEFAULT)
							&& levelF != null && levelF.trim().length() > 0){
						processLocation(null, null, null, "");
						query.append(" AND ").append( constructLocationQuery(longitudeF, latitudeF, levelF) );	
						queryPartforSearchPOC.append(" AND ").append( constructLocationQuery(longitudeF, latitudeF, levelF) );
						SystemUtils.trace("hays_search", "append location filter to query: " + longitudeF + ", " + latitudeF + ", " + levelF);
						
					} 
					if(latitude != null && latitude.trim().length() > 0 && !latitude.equals(IHaysSearchConstants.DEFAULT)
							&& longitude != null && longitude.trim().length() > 0 && !longitude.equals(IHaysSearchConstants.DEFAULT)
							&& radius != null && radius.trim().length() > 0  && !radius.equals(IHaysSearchConstants.DEFAULT)){
						SystemUtils.trace("hays_search", "Inside third If condition " + longitude + ", " + latitude + ", " + radius);
						processLocation(longitude, latitude, radius, "");
					} else {	
						SystemUtils.trace("hays_Search","Insisde this");
						processLocation(null, null, null, "");
						
						if (longitude != null && longitude.trim().length() > 0 && !longitude.equals(IHaysSearchConstants.DEFAULT)
								&& latitude != null && latitude.trim().length() > 0 && !latitude.equals(IHaysSearchConstants.DEFAULT)
								&& level!= null && level.trim().length() > 0 && !level.equals(IHaysSearchConstants.DEFAULT)) {
							SystemUtils.trace("hays_search", "append location to query: " + longitude + ", " + latitude + ", " + radius + ", level" + level);
							query.append(" AND ").append( constructLocationQuery(longitude, latitude, level) );
							queryPartforSearchPOC.append(" AND ").append( constructLocationQuery(longitude, latitude, level) );
							SystemUtils.trace("hays_search", "Query after adding location "+query);
							
						}else {
							String location = super.m_binder.getLocal(IHaysSearchConstants.JOB_LOCATION);
							SystemUtils.trace("hays_search", "location value which has id's"+location);
							
							if( location != null ){
								location = location.trim();
								if( location.length() > 0 ) {
									SystemUtils.trace("hays_search","location parameter: " + location);
									location = location.replaceAll(",[ ]?", " | ");
									DataBinder params = new DataBinder();
									params.putLocal("locationIds", location);
									ResultSet rez = null;
									try {
										rez = this.m_workspace.createResultSet("LocationDetailsQuery", params);
										SystemUtils.trace("hays_search","search for locations: " + location);
									} catch(DataException ex){
										//
										SystemUtils.trace("hays_search","check GoogleMap for this location is missing. Location is ignored");
									}
									if( rez != null && rez.first()) {
										String x = rez.getStringValueByName("longitude");
										String y = rez.getStringValueByName("latitude");
										String lev = rez.getStringValueByName("level");
										query.append(" AND ").append( constructLocationQuery(x, y, lev) );
										queryPartforSearchPOC.append(" AND ").append( constructLocationQuery(x, y, lev) );
									}
								}
							} 
						} 
					}		
				}
			}
		  // INTERNATIONAL
        String jobInternational = super.m_binder.getLocal(IHaysSearchConstants.JOB_INTERN);
        if( jobInternational != null ) {
	        if( query.length() > 0)
//        		query.append("  ");
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
        String jobNonEU = super.m_binder.getLocal(IHaysSearchConstants.JOB_NONNATIONL);
        if( jobNonEU != null ) {
	        if( query.length() > 0)
//        		query.append(" AND ");
	        if( ("1").equals(jobNonEU) || ("Y").equals(jobNonEU)) {
	        	query.append(" AND (Yes WITHIN xNonNationals)");
	        } else {
	        	query.append(" NOT (Yes WITHIN xNonNationals)");
	        }
        }
        
     // SPONSORED
        String jobSponsored = super.m_binder.getLocal(IHaysSearchConstants.JOB_SPONSORED);
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
       
        //if( query.length() == 0){
        //	query.append("idccontenttrue ");
        //}
       
	}
		//Bucket Logic for Search POC change, Five buckets are made 1 day old, 3 days old, 7 days old,14 days old and more than 14 days old.
        
        Calendar currentDate = Calendar.getInstance();
        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd");
        String dateNow = formatter.format(currentDate.getTime());
        SystemUtils.trace("hays_search", "processSearchParameters: Current Date is ======="+ dateNow);
       
        currentDate.add(Calendar.DATE,+1);
        String datetommorow = formatter.format(currentDate.getTime());
        SystemUtils.trace("hays_search", "processSearchParameters: Tommorow days before date is ======="+ datetommorow);
        
        currentDate.add(Calendar.DATE,-2);
        String date24hrs = formatter.format(currentDate.getTime());
        SystemUtils.trace("hays_search", "processSearchParameters: 1 days before date is ======="+ date24hrs);
        
        currentDate.add(Calendar.DATE,-1);
        String date03 = formatter.format(currentDate.getTime());
        SystemUtils.trace("hays_search", "processSearchParameters: 3 days before date is ======="+ date03);
        
        currentDate.add(Calendar.DATE,-4);
        String date07 = formatter.format(currentDate.getTime());
        SystemUtils.trace("hays_search", "processSearchParameters: 7 days before date is ======="+ date07);
        
        currentDate.add(Calendar.DATE,-7);
        String date14 = formatter.format(currentDate.getTime());
        SystemUtils.trace("hays_search", "processSearchParameters: 14 days before date is ======="+ date14);
        
        
       
        
       query.append(" ACCUM(((").append(" SDATA(xEventDate between'").append(date24hrs).append("' and '").append(datetommorow).append(" '))*.25");
       query.append(" OR(").append(" SDATA(xEventDate between'").append(date03).append("' and '").append(date24hrs).append(" '))*.23");
       query.append(" OR(").append(" SDATA(xEventDate between'").append(date07).append("' and '").append(date03).append(" '))*.22");
       query.append(" OR(").append(" SDATA(xEventDate between'").append(date14).append("' and '").append(date07).append(" '))*.21");
       query.append(" OR(").append(" SDATA(xEventDate < '").append(date14).append("'))*.20 )").append(queryPartforSearchPOC).append(")");
       
       SystemUtils.trace("hays_search", "processSearchParameters: WEIGHTINGSsssssssssss ="+ query);
        
        
     //end

		
		
		SystemUtils.trace("hays_search", new StringBuffer("final locations: latitude =").append(m_binder.getLocal(IHaysSearchConstants.NE_LATITUDE)).append(", longitude: ").append(m_binder.getLocal(IHaysSearchConstants.NE_LONGITUDE)).append(", radius: ").append(m_binder.getLocal(IHaysSearchConstants.RADIUS)).toString() );
		      
		 m_binder.putLocal("QueryText", query.toString());
	     SystemUtils.trace("hays_search", "\nFinal query123: =" + query );
	}
	
	private StringBuffer mergeTempAndContract() throws DataException{
		// CONTRACT
        String ctype = super.m_binder.getLocal(IHaysSearchConstants.JOB_CONTRACT);
        // TEMPORARY
        String ttype = super.m_binder.getLocal(IHaysSearchConstants.JOB_TEMP);
        String cmin, cmax, cpermPayType, tmin, tmax, tpermPayType;
        //initialise with request parameters
        cmin = super.m_binder.getLocal(IHaysSearchConstants.JOB_MIN_CONTRACT);
		cmax = super.m_binder.getLocal(IHaysSearchConstants.JOB_MAX_CONTRACT);
		cpermPayType = super.m_binder.getLocal(IHaysSearchConstants.JOB_SELECT_CONTRACT);
		tmin = super.m_binder.getLocal(IHaysSearchConstants.JOB_MIN_TEMP);
		tmax = super.m_binder.getLocal(IHaysSearchConstants.JOB_MAX_TEMP);
		tpermPayType = super.m_binder.getLocal(IHaysSearchConstants.JOB_SELECT_TEMP);
		
		 SystemUtils.trace("hays_search", "ctype="+ctype+",cmin: =" + cmin +",cmax:"+cmax);
		 SystemUtils.trace("hays_search", "ttype="+ttype+",tmin: =" + tmin +",tmax:"+tmax);
		//merge parameters
        if( ctype != null && ttype == null) {
        	if( ctype.length() > 0 ) {
        		tmin = cmin;
        		tmax = cmax;
        		tpermPayType = cpermPayType; 
	        }
        }
        if( ttype != null && ctype == null) {
        	if( ttype.length() > 0 ) {
        		cmin = tmin;
        		cmax = tmax;
        		cpermPayType = tpermPayType;
	        }
        }
        
        SystemUtils.trace("hays_search", "After Assignment cmin: =" + cmin +",cmax:"+cmax);
		SystemUtils.trace("hays_search", "After Assignment tmin: =" + tmin +",tmax:"+tmax);
		
        cpermPayType = setTempContractSalaryType(cpermPayType);
        tpermPayType = setTempContractSalaryType(tpermPayType);
        cmin = setTempContractSalaryMin(cmin);
        cmax = setTempContractSalaryMax(cmax,cpermPayType);
        tmin = setTempContractSalaryMin(tmin);
        tmax = setTempContractSalaryMax(tmax,tpermPayType);
        
        SystemUtils.trace("hays_search", "Final cmin: =" + cmin +",cmax:"+cmax);
		SystemUtils.trace("hays_search", "Final  tmin: =" + tmin +",tmax:"+tmax);
        
        StringBuffer query = null;
        if((ttype != null && ttype.length() > 0) || (ctype != null && ctype.length() >0)){
        	query = new StringBuffer();
        	if(cmin != null && cmax != null){
		        //get annualised double values
		        double cMin = SearchCommons.calculateUnifiedSalary(cmin, cpermPayType);
		        double cMax = SearchCommons.calculateUnifiedSalary(cmax, cpermPayType);
		        double tMin = SearchCommons.calculateUnifiedSalary(tmin, tpermPayType);
		        double tMax = SearchCommons.calculateUnifiedSalary(tmax, tpermPayType);
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
	        //AND (({T} OR {C}) WITHIN xJobType ) )
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
        		value = super.m_binder.getLocal(key);
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
        String swLatitude = super.m_binder.getLocal(IHaysSearchConstants.SW_LATITUDE + suffix);
        String swLongitude = super.m_binder.getLocal(IHaysSearchConstants.SW_LONGITUDE + suffix);
        
        // convert radius to miles
        if( radius != null) {
        	String distUnit = super.m_binder.getLocal(IHaysSearchConstants.DISTANCE_UNIT);
        	if(distUnit != null && IHaysSearchConstants.DISTANCE_UNITS.contains(distUnit) ) {
        		if ( distUnit.equals("km")) {
        			radius = String.valueOf( (int)(Integer.parseInt(radius) * IHaysSearchConstants.KM_MILES_CONVERT));
        		}
        	}
        }
        
        SystemUtils.trace("hays_search", new StringBuffer("latitude: ").append(latitude).append(", long: ").append(longitude).append(", radius: ").append(radius).toString());
        
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
		String exclude = super.m_binder.getLocal(IHaysSearchConstants.EXCLUDE);
		
		String jobTitle = super.m_binder.getLocal(IHaysSearchConstants.JOB_TITLE_FILTER);
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
        String industry = super.m_binder.getLocal(IHaysSearchConstants.JOB_INDUSTRY_FILTER);
        SystemUtils.trace("hays_search", "JOB_INDUSTRY_FILTER =" + industry );
        if( industry != null && industry.trim().length() > 0) {
        	queryPart = new StringBuffer("(");
			String[] industryArray  = industry.split(";");
			String aFilter = null;
			for( int i = 0; i < industryArray.length; i++){
				aFilter = industryArray[i].trim();
				/*if( aFilter.startsWith(",") ) {
					aFilter = aFilter.substring(1);
				}*/
				aFilter = aFilter.replaceAll(",[ ]?", "#");
				String [] industryfArray =  aFilter.split("#");
				aFilter = "";
	     		SystemUtils.trace("hays_search", "IndustryfArray length"+industryfArray.length);
	     		   for(int j=0;j<industryfArray.length;j++)
	     		   {
	     			   if(industryfArray[j].length() >0){
	     				  aFilter = aFilter + " | "+industryfArray[j];
	     			   }
	     		   }
	     		  if( aFilter.startsWith(" | ") ) {
						aFilter = aFilter.substring(3);
					}
	     		  
				if( queryPart.length() > 1)
					queryPart.append(" and ");
				queryPart.append("(").append(aFilter).append(")");
			}
			 if( queryPart.length() > 1){
	        	queryPart.append(") WITHIN xIndustry ");
        		if(exclude != null && exclude.contains(IHaysSearchConstants.JOB_INDUSTRY) ) {
	    			query.append(" NOT ");
	    			SystemUtils.trace("hays_search", "In  industries other than " + aFilter);
	    		} else if( query.length() > 0) {
	    			query.append(" AND ");
	        	}
	        	query.append(" (").append(queryPart).append(" )*10*10");
	        	
			 }
		}
        
       //CATEGORY
         String categoryf=super.m_binder.getLocal(IHaysSearchConstants.JOB_CATEGORY_FILTER);
         SystemUtils.trace("hays_search", "JOB_CATEGORY_FILTER =" + categoryf );
         if( categoryf != null && categoryf.trim().length() > 0) {
        	 
        	 StringBuffer  finalsubTerms=new StringBuffer();
	        	SystemUtils.trace("hays_search", "categories 1st =" + categoryf);
	        	 String [] categoriesArray = categoryf.split("&");
	        	 
	            for(int x=0;x<categoriesArray.length;x++)   
	        	 {
	        		 SystemUtils.trace("hays_search", "CategoriesArray Value"+categoriesArray[x]);
                    if(categoriesArray[x].startsWith(",")) 
                    {
                    	finalsubTerms.append(")AND(");
                    	SystemUtils.trace("hays_search", "Starts With");
                    categoriesArray[x]=categoriesArray[x].substring(1);
                    }
                    categoriesArray[x] =  categoriesArray[x].replaceAll("[,][ ]?", "#");
	        		 SystemUtils.trace("hays_search", "categoryffffff"+categoriesArray[x]);
	             		   
     		 SystemUtils.trace("hays_search", "categories after replace all=" + categoryf);
     	     String [] categoriesfArray =  categoriesArray[x].split("#");
     		 SystemUtils.trace("hays_search", "CategoriesfArray length"+categoriesfArray.length);
     		   for(int i=0;i<categoriesfArray.length;i++)
     		   {
     		 
     			 		String  subTerms = ontGetRelatedTerms(categoriesfArray[i], "xCategory@hays:ParentTerm");
			        		 SystemUtils.trace("hays_search", "subTerms=" + subTerms);
			        		
			        		if("[]".equalsIgnoreCase(subTerms))
			        		{
			        			subTerms = categoriesfArray[i];
			        		}
			        		else
			        		{ 
			        			subTerms = categoriesfArray[i] + "," + subTerms.substring(1, subTerms.length()-1);
			        		}
			        		 SystemUtils.trace("hays_search", "for i=="+i+"  ,subTerms=" + subTerms);
			        		 if(i>0)
			        		 {
			        			 finalsubTerms.append(" | ");
			        		 }
			        		 finalsubTerms.append(subTerms);
			        		 SystemUtils.trace("hays_search", "for i=="+i+"  ,finalsubTerms=" + finalsubTerms.toString());		 
     		 }
     		      		 
	        	 }
     		 SystemUtils.trace("hays_search", "final  finalsubTerms=" + finalsubTerms.toString());	
     		categoryf = finalsubTerms.toString();	
     		categoryf = categoryf.replaceAll("[,;#][ ]?", " | ");	
     		if( query.length() > 0) 
    		{
    			query.append(" AND ");
    		}
        	query.append("((").append(categoryf).append(") WITHIN xCategory )*10*10");
	        SystemUtils.trace("hays_search", "processSearchParameters: Category='" + query + "'");
        	
     		// added for r7 end
         }
         
        
        
        // KEYWORDS
        String keywords = super.m_binder.getLocal(IHaysSearchConstants.JOB_KEYWORDS_FILTER);
        
       
        
        if( keywords != null && keywords.trim().length() > 0) {
        	SystemUtils.trace("hays_search", "JOB_KEYWORDS_FILTER =" + keywords );
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
        String type = super.m_binder.getLocal(IHaysSearchConstants.JOB_TYPE_FILTER);
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
        permtype = super.m_binder.getLocal(IHaysSearchConstants.JOB_PERM_SLIDER);
        if( permtype != null ) {
        	if( permtype.length() > 0 ) {
        		min = super.m_binder.getLocal(IHaysSearchConstants.JOB_MIN_PERM_SLIIDER);
        		max = super.m_binder.getLocal(IHaysSearchConstants.JOB_MAX_PERM_SLIIDER);
        		permPayType = super.m_binder.getLocal(IHaysSearchConstants.JOB_SELECT_PERM_SLIDER);
        		perm_salary = processSalary(min, max, permPayType, IHaysSearchConstants.PERMANENT);
        		if( perm_salary != null && perm_salary.length() > 0 )
        		{
        			jobTypeBuffer.append("(").append(perm_salary).append(")");
        		}
        			
	        }
        }
        
     // TEMPORARY Job Type Filter
        //StringBuffer salaryBuffer = new StringBuffer();
        String temptype,temp_salary = null;
        temptype = super.m_binder.getLocal(IHaysSearchConstants.JOB_TEMP_SLIDER);
        if( temptype != null ) {
        	if( temptype.length() > 0 ) {
        		min = super.m_binder.getLocal(IHaysSearchConstants.JOB_MIN_TEMP_SLIIDER);
        		max = super.m_binder.getLocal(IHaysSearchConstants.JOB_MAX_TEMP_SLIIDER);
        		permPayType = super.m_binder.getLocal(IHaysSearchConstants.JOB_SELECT_TEMP_SLIDER);
        		temp_salary = processSalary(min, max, permPayType, IHaysSearchConstants.TEMPORARY);
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
        //StringBuffer salaryBuffer = new StringBuffer();
        String conttype, cont_salary = null;
        conttype = super.m_binder.getLocal(IHaysSearchConstants.JOB_CONT_SLIDER);
        if( conttype != null ) {
        	if( conttype.length() > 0 ) {
        		min = super.m_binder.getLocal(IHaysSearchConstants.JOB_MIN_CONT_SLIIDER);
        		max = super.m_binder.getLocal(IHaysSearchConstants.JOB_MAX_CONT_SLIIDER);
        		permPayType = super.m_binder.getLocal(IHaysSearchConstants.JOB_SELECT_CONT_SLIDER);
        		cont_salary = processSalary(min, max, permPayType, IHaysSearchConstants.CONTRACT);
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
        
        
        SystemUtils.trace("hays_search", "Filter Job Type Query ='" + query + "'");
		return query;
	}
	
	
	
	public void validateSearchParams() {
		SystemUtils.trace("hays_search", "validateSearchParams() binder:  " + super.m_binder.getLocalData());
		String jobTitle = super.m_binder.getLocal(IHaysSearchConstants.JOB_TITLE);
		String jobTitleFull = super.m_binder.getLocal(IHaysSearchConstants.JOB_TITLE_FULL);
		String jobTitleFullDescr = super.m_binder.getLocal(IHaysSearchConstants.JOB_TITLE_FULL_DECR);
		String ref = super.m_binder.getLocal(IHaysSearchConstants.JOB_TITLE_REF);
		String industry = super.m_binder.getLocal(IHaysSearchConstants.JOB_INDUSTRY);
        if( industry == null) {
        	StringBuffer industries = processCheckBoxes(IHaysSearchConstants.JOB_INDUSTRY);
        	if( industries.length() > 0) {
        		industry = industries.substring(1);	
        	}
        }
		String keywords = super.m_binder.getLocal(IHaysSearchConstants.JOB_KEYWORDS);
		//String typeP = "A";//super.m_binder.getLocal("job_permanent"); // commented for release 8.0
		String typeP =super.m_binder.getLocal(IHaysSearchConstants.JOB_SELECT_PERM);//added for release 8.0
		String jobTypeP = super.m_binder.getLocal(IHaysSearchConstants.JOB_PERM);
		String minP = super.m_binder.getLocal(IHaysSearchConstants.JOB_MIN_PERM);
		String maxP = super.m_binder.getLocal(IHaysSearchConstants.JOB_MAX_PERM);
		String jobTypeC = super.m_binder.getLocal(IHaysSearchConstants.JOB_CONTRACT);
		String typeC = super.m_binder.getLocal(IHaysSearchConstants.JOB_SELECT_CONTRACT);
		String minC = super.m_binder.getLocal(IHaysSearchConstants.JOB_MIN_CONTRACT);
		String maxC = super.m_binder.getLocal(IHaysSearchConstants.JOB_MAX_CONTRACT);
		String jobTypeT = super.m_binder.getLocal(IHaysSearchConstants.JOB_TEMP);
		String typeT = super.m_binder.getLocal(IHaysSearchConstants.JOB_SELECT_TEMP);
		String minT = super.m_binder.getLocal(IHaysSearchConstants.JOB_MIN_TEMP);
		String maxT = super.m_binder.getLocal(IHaysSearchConstants.JOB_MAX_TEMP);
		String location = super.m_binder.getLocal(IHaysSearchConstants.JOB_LOCATION);
		String neLatitude = super.m_binder.getLocal(IHaysSearchConstants.NE_LATITUDE);
        String neLongitude = super.m_binder.getLocal(IHaysSearchConstants.NE_LONGITUDE);
        String level = super.m_binder.getLocal(IHaysSearchConstants.LEVEL);
        String radius = super.m_binder.getLocal(IHaysSearchConstants.RADIUS);
        String contentType = super.m_binder.getLocal(IHaysSearchConstants.CONTENT_TYPE);
        String sortfield = super.m_binder.getLocal(IHaysSearchConstants.SORTFIELD);
        String sortorder = super.m_binder.getLocal(IHaysSearchConstants.SORTORDER);
        String locale = super.m_binder.getLocal(IHaysSearchConstants.LOCALE);
        String jobInternational = super.m_binder.getLocal(IHaysSearchConstants.JOB_INTERN);
        String jobNonNational = super.m_binder.getLocal(IHaysSearchConstants.JOB_NONNATIONL);
        String isOnlyJobTitle = super.m_binder.getLocal(IHaysSearchConstants.IS_ONLY_JOB_TITLE);
        String isFuzzy = super.m_binder.getLocal(IHaysSearchConstants.IS_FUZZY_SEARCH);
        String location_id=super.m_binder.getLocal(IHaysSearchConstants.LOCATION_ID);
        //String isHomePageSearch = super.m_binder.getLocal("isHomePageSearch");
        String job_category = super.m_binder.getLocal(IHaysSearchConstants.JOB_CATEGORY); 
        String isAdvanceSearch = super.m_binder.getLocal(IHaysSearchConstants.IS_ADVANCE_SEARCH);// added  for r7
        String isHomePageSearch = super.m_binder.getLocal(IHaysSearchConstants.IS_HOME_SEARCH);
        String jobPostCode = super.m_binder.getLocal(IHaysSearchConstants.JOB_POST_CODE);// added for alert form requirement for r7
        String locationSet = super.m_binder.getLocal("location_set");
        String er = null;
        StringBuffer parameters = new StringBuffer();
        if( jobTitle != null){
        	jobTitle = QueryUtils.encodeForHaysSpecialKeywords(jobTitle);
        	parameters.append("&").append(IHaysSearchConstants.JOB_TITLE).append("=").append(jobTitle.replaceAll(IHaysSearchConstants.fuzzySpecialCharacters, " "));
        }
        if( jobTitleFull != null){
        	jobTitleFull = QueryUtils.encodeForHaysSpecialKeywords(jobTitleFull);
        	parameters.append("&").append(IHaysSearchConstants.JOB_TITLE_FULL).append("=").append(jobTitleFull);
        }
        if( jobTitleFullDescr != null){
        	jobTitleFullDescr = QueryUtils.encodeForHaysSpecialKeywords(jobTitleFullDescr);
        	parameters.append("&").append(IHaysSearchConstants.JOB_TITLE_FULL_DECR).append("=").append(jobTitleFullDescr);
        }
        if( ref != null){
        	ref = QueryUtils.encodeForHaysSpecialKeywords(ref);
        	parameters.append("&").append(IHaysSearchConstants.JOB_TITLE_REF).append("=").append(ref);
        }
        if( industry != null)
        	parameters.append("&").append(IHaysSearchConstants.JOB_INDUSTRY).append("=").append(industry);
        if( keywords != null ){
        	keywords = QueryUtils.encodeForHaysSpecialKeywords(keywords);
        	parameters.append("&").append(IHaysSearchConstants.JOB_KEYWORDS).append("=").append(keywords.replaceAll(IHaysSearchConstants.specialCharactersForKeywords, " "));
        }
        if( jobTypeP != null) {
        	parameters.append("&").append(IHaysSearchConstants.JOB_PERM).append("=").append(jobTypeP).append("&").append(IHaysSearchConstants.JOB_MIN_PERM).append("=").append(minP);
        	SystemUtils.trace("hays_search", "validateSearchParams() jobTypeP:  " + jobTypeP+" typeP "+typeP);
        	if(typeP == null){
        		typeP = "A"; //for uk and cerow
        		parameters.append("&").append(IHaysSearchConstants.JOB_MAX_PERM).append("=").append(maxP).append("&").append(IHaysSearchConstants.JOB_SELECT_PERM).append("=A");// commented for release 8.0
        	}else{
        		parameters.append("&").append(IHaysSearchConstants.JOB_MAX_PERM).append("=").append(maxP).append("&").append(IHaysSearchConstants.JOB_SELECT_PERM).append("="+typeP);// added for release 8.0
        	}
         }
        if( jobTypeC != null ) {
        	parameters.append("&").append(IHaysSearchConstants.JOB_CONTRACT).append("=").append(jobTypeC).append("&").append(IHaysSearchConstants.JOB_MIN_CONTRACT).append("=").append(minC);
        	parameters.append("&").append(IHaysSearchConstants.JOB_MAX_CONTRACT).append("=").append(maxC).append("&").append(IHaysSearchConstants.JOB_SELECT_CONTRACT).append("=").append(typeC);
        }
        if( jobTypeT != null) {
        	parameters.append("&").append(IHaysSearchConstants.JOB_TEMP).append("=").append(jobTypeT).append("&").append(IHaysSearchConstants.JOB_MIN_TEMP).append("=").append(minT);
        	parameters.append("&").append(IHaysSearchConstants.JOB_MAX_TEMP).append("=").append(maxT).append("&").append(IHaysSearchConstants.JOB_SELECT_TEMP).append("=").append(typeT);
        }
        if( contentType != null)
        	parameters.append("&").append(IHaysSearchConstants.CONTENT_TYPE).append("=").append(contentType);
     //   parameters.append("&").append(JOB_INTERN).append("=").append(jobInternational);
    //	parameters.append("&").append(JOB_SPONSORED).append("=").append(jobSponsored);
		
        if( locale != null)
        	parameters.append("&").append(IHaysSearchConstants.LOCALE).append("=").append(locale);
        
        if( location != null)
        	parameters.append("&").append(IHaysSearchConstants.JOB_LOCATION).append("=").append(location);
        if( neLongitude != null && neLatitude != null) {
        	parameters.append("&").append(IHaysSearchConstants.NE_LONGITUDE).append("=").append(neLongitude);
        	parameters.append("&").append(IHaysSearchConstants.NE_LATITUDE).append("=").append(neLatitude);
        }
        if( level != null)
        	parameters.append("&").append(IHaysSearchConstants.LEVEL).append("=").append(level);
        if( radius != null)
        	parameters.append("&").append(IHaysSearchConstants.RADIUS).append("=").append(radius);
        if( sortfield != null)
        	parameters.append("&").append(IHaysSearchConstants.SORTFIELD).append("=").append(sortfield);
        if( sortorder != null)
        	parameters.append("&").append(IHaysSearchConstants.SORTORDER).append("=").append(sortorder);
        if( jobInternational != null)
        	parameters.append("&").append(IHaysSearchConstants.JOB_INTERN).append("=").append(jobInternational);
        if( jobNonNational != null)
        	parameters.append("&").append(IHaysSearchConstants.JOB_NONNATIONL).append("=").append(jobNonNational);
        if( isOnlyJobTitle != null)
        	parameters.append("&").append(IHaysSearchConstants.IS_ONLY_JOB_TITLE).append("=").append(isOnlyJobTitle);
        if( isFuzzy != null)
        	parameters.append("&").append(IHaysSearchConstants.IS_FUZZY_SEARCH).append("=").append(isFuzzy);
        if( location_id != null)
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
    
		
		 m_binder.putLocal("QueryParameters", parameters.toString());
		 SystemUtils.trace("hays_search", "validateSearchParams() QueryParameters:  " + parameters.toString());
    
        // check mandatory parameters
        if( !isTitleSet()  && (industry == null || industry.trim().length() == 0)
        		&& (keywords == null || keywords.trim().length() == 0)
        		&& jobTypeP == null 
        		&& jobTypeT == null 
        		&& jobTypeC == null 
        		&& (neLatitude == null || neLatitude.equals("0")) && (neLongitude == null || neLongitude.equals("0")) && (locationSet == null || locationSet.trim().length() == 0) && (job_category == null || job_category.trim().length() == 0) ) {
        	er = "csInvalidSearchParametrs";
			
        } else { // validate salary
	        try {
	        	if( (jobTypeP != null && !isValid(minP, maxP, typeP)) || 
	        			(jobTypeC != null && !isValid(minC, maxC, typeC)) ||
	        			( jobTypeT != null && !isValid(minT, maxT, typeT) ) ) {
	        		er = "csInvalidSalaryParameters";
	        		
	        	}
	        } catch(Exception ex) {
	        	er = "csInvalidSalaryParameters";
	        }
        }
        SystemUtils.trace("hays_search", "validation error: " + er);
        if( er != null)
        	m_binder.putLocal("STATUS", er);
	}
	
	public void validateSearchParamsNew() {
		SystemUtils.trace("hays_search", "validateSearchParamsNew() binder:  " + super.m_binder.getLocalData());
		String jobTitle = super.m_binder.getLocal(IHaysSearchConstants.JOB_TITLE);
		String jobTitleFull = super.m_binder.getLocal(IHaysSearchConstants.JOB_TITLE_FULL);
		String jobTitleFullDescr = super.m_binder.getLocal(IHaysSearchConstants.JOB_TITLE_FULL_DECR);
		String ref = super.m_binder.getLocal(IHaysSearchConstants.JOB_TITLE_REF);
		String industry = super.m_binder.getLocal(IHaysSearchConstants.JOB_INDUSTRY);
        if( industry == null) {
        	StringBuffer industries = processCheckBoxes(IHaysSearchConstants.JOB_INDUSTRY);
        	if( industries.length() > 0) {
        		industry = industries.substring(1);	
        	}
        }
		String keywords = super.m_binder.getLocal(IHaysSearchConstants.JOB_KEYWORDS);
		//String typeP = "A";//super.m_binder.getLocal("job_permanent"); // commented for release 8.0
		String typeP =super.m_binder.getLocal(IHaysSearchConstants.JOB_SELECT_PERM);//added for release 8.0
		String jobTypeP = super.m_binder.getLocal(IHaysSearchConstants.JOB_PERM);
		String minP = super.m_binder.getLocal(IHaysSearchConstants.JOB_MIN_PERM);
		String maxP = super.m_binder.getLocal(IHaysSearchConstants.JOB_MAX_PERM);
		String jobTypeC = super.m_binder.getLocal(IHaysSearchConstants.JOB_CONTRACT);
		String typeC = super.m_binder.getLocal(IHaysSearchConstants.JOB_SELECT_CONTRACT);
		String minC = super.m_binder.getLocal(IHaysSearchConstants.JOB_MIN_CONTRACT);
		String maxC = super.m_binder.getLocal(IHaysSearchConstants.JOB_MAX_CONTRACT);
		String jobTypeT = super.m_binder.getLocal(IHaysSearchConstants.JOB_TEMP);
		String typeT = super.m_binder.getLocal(IHaysSearchConstants.JOB_SELECT_TEMP);
		String minT = super.m_binder.getLocal(IHaysSearchConstants.JOB_MIN_TEMP);
		String maxT = super.m_binder.getLocal(IHaysSearchConstants.JOB_MAX_TEMP);
		String location = super.m_binder.getLocal(IHaysSearchConstants.JOB_LOCATION);
		String neLatitude = super.m_binder.getLocal(IHaysSearchConstants.NE_LATITUDE);
        String neLongitude = super.m_binder.getLocal(IHaysSearchConstants.NE_LONGITUDE);
        String level = super.m_binder.getLocal(IHaysSearchConstants.LEVEL);
        String radius = super.m_binder.getLocal(IHaysSearchConstants.RADIUS);
        String contentType = super.m_binder.getLocal(IHaysSearchConstants.CONTENT_TYPE);
        String sortfield = super.m_binder.getLocal(IHaysSearchConstants.SORTFIELD);
        String sortorder = super.m_binder.getLocal(IHaysSearchConstants.SORTORDER);
        String locale = super.m_binder.getLocal(IHaysSearchConstants.LOCALE);
        String jobInternational = super.m_binder.getLocal(IHaysSearchConstants.JOB_INTERN);
        String jobNonNational = super.m_binder.getLocal(IHaysSearchConstants.JOB_NONNATIONL);
        String isOnlyJobTitle = super.m_binder.getLocal(IHaysSearchConstants.IS_ONLY_JOB_TITLE);
        String isFuzzy = super.m_binder.getLocal(IHaysSearchConstants.IS_FUZZY_SEARCH);
        String location_id=super.m_binder.getLocal(IHaysSearchConstants.LOCATION_ID);
        //String isHomePageSearch = super.m_binder.getLocal("isHomePageSearch");
        String job_category = super.m_binder.getLocal(IHaysSearchConstants.JOB_CATEGORY); 
        String isAdvanceSearch = super.m_binder.getLocal(IHaysSearchConstants.IS_ADVANCE_SEARCH);// added  for r7
        String isHomePageSearch = super.m_binder.getLocal(IHaysSearchConstants.IS_HOME_SEARCH);
        String jobPostCode = super.m_binder.getLocal(IHaysSearchConstants.JOB_POST_CODE);// added for alert form requirement for r7
        String locationSet = super.m_binder.getLocal("location_set");
        String er = null;
        StringBuffer parameters = new StringBuffer();
        if( jobTitle != null){
        	jobTitle = QueryUtils.encodeForHaysSpecialKeywords(jobTitle);
        	m_binder.putLocal(IHaysSearchConstants.JOB_TITLE, jobTitle.replaceAll(IHaysSearchConstants.fuzzySpecialCharacters, " "));
        	//parameters.append("&").append(IHaysSearchConstants.JOB_TITLE).append("=").append(jobTitle.replaceAll(IHaysSearchConstants.fuzzySpecialCharacters, " "));
        }
        if( jobTitleFull != null){
        	jobTitleFull = QueryUtils.encodeForHaysSpecialKeywords(jobTitleFull);
        	m_binder.putLocal(IHaysSearchConstants.JOB_TITLE_FULL, jobTitleFull);
        	//parameters.append("&").append(IHaysSearchConstants.JOB_TITLE_FULL).append("=").append(jobTitleFull);
        }
        if( jobTitleFullDescr != null){
        	jobTitleFullDescr = QueryUtils.encodeForHaysSpecialKeywords(jobTitleFullDescr);
        	m_binder.putLocal(IHaysSearchConstants.JOB_TITLE_FULL_DECR, jobTitleFullDescr);
        	//parameters.append("&").append(IHaysSearchConstants.JOB_TITLE_FULL_DECR).append("=").append(jobTitleFullDescr);
        }
        if( ref != null){
        	ref = QueryUtils.encodeForHaysSpecialKeywords(ref);
        	m_binder.putLocal(IHaysSearchConstants.JOB_TITLE_REF, ref);
        	//parameters.append("&").append(IHaysSearchConstants.JOB_TITLE_REF).append("=").append(ref);
        }
        if( industry != null)
        	m_binder.putLocal(IHaysSearchConstants.JOB_INDUSTRY, industry);
        	//parameters.append("&").append(IHaysSearchConstants.JOB_INDUSTRY).append("=").append(industry);
        if( keywords != null ){
        	keywords = QueryUtils.encodeForHaysSpecialKeywords(keywords);
        	m_binder.putLocal(IHaysSearchConstants.JOB_KEYWORDS, keywords.replaceAll(IHaysSearchConstants.specialCharactersForKeywords, " "));
        	//parameters.append("&").append(IHaysSearchConstants.JOB_KEYWORDS).append("=").append(keywords.replaceAll(IHaysSearchConstants.specialCharactersForKeywords, " "));
        }
        if( jobTypeP != null) {
        	m_binder.putLocal(IHaysSearchConstants.JOB_PERM, jobTypeP);
        	m_binder.putLocal(IHaysSearchConstants.JOB_MIN_PERM, minP);
        	//parameters.append("&").append(IHaysSearchConstants.JOB_PERM).append("=").append(jobTypeP).append("&").append(IHaysSearchConstants.JOB_MIN_PERM).append("=").append(minP);
        	
        	SystemUtils.trace("hays_search", "validateSearchParams() jobTypeP:  " + jobTypeP+" typeP "+typeP);
        	if(typeP == null){
        		typeP = "A"; //for uk and cerow
        		m_binder.putLocal(IHaysSearchConstants.JOB_MAX_PERM, maxP);
        		m_binder.putLocal(IHaysSearchConstants.JOB_SELECT_PERM, "A");
        		//parameters.append("&").append(IHaysSearchConstants.JOB_MAX_PERM).append("=").append(maxP).append("&").append(IHaysSearchConstants.JOB_SELECT_PERM).append("=A");// commented for release 8.0
        	}else{
        		m_binder.putLocal(IHaysSearchConstants.JOB_MAX_PERM, maxP);
        		m_binder.putLocal(IHaysSearchConstants.JOB_SELECT_PERM, typeP);
        		//parameters.append("&").append(IHaysSearchConstants.JOB_MAX_PERM).append("=").append(maxP).append("&").append(IHaysSearchConstants.JOB_SELECT_PERM).append("="+typeP);// added for release 8.0
        	}
         }
        if( jobTypeC != null ) {
        	m_binder.putLocal(IHaysSearchConstants.JOB_CONTRACT, jobTypeC);
        	m_binder.putLocal(IHaysSearchConstants.JOB_MIN_CONTRACT, minC);
        	m_binder.putLocal(IHaysSearchConstants.JOB_MAX_CONTRACT, maxC);
        	m_binder.putLocal(IHaysSearchConstants.JOB_SELECT_CONTRACT, typeC);
        	///parameters.append("&").append(IHaysSearchConstants.JOB_CONTRACT).append("=").append(jobTypeC).append("&").append(IHaysSearchConstants.JOB_MIN_CONTRACT).append("=").append(minC);
       // 	parameters.append("&").append(IHaysSearchConstants.JOB_MAX_CONTRACT).append("=").append(maxC).append("&").append(IHaysSearchConstants.JOB_SELECT_CONTRACT).append("=").append(typeC);
        }
        if( jobTypeT != null) {
        	m_binder.putLocal(IHaysSearchConstants.JOB_TEMP, jobTypeT);
        	m_binder.putLocal(IHaysSearchConstants.JOB_MIN_TEMP, minT);
        	m_binder.putLocal(IHaysSearchConstants.JOB_MAX_TEMP, maxT);
        	m_binder.putLocal(IHaysSearchConstants.JOB_SELECT_TEMP, typeT);
        	//parameters.append("&").append(IHaysSearchConstants.JOB_TEMP).append("=").append(jobTypeT).append("&").append(IHaysSearchConstants.JOB_MIN_TEMP).append("=").append(minT);
        	//parameters.append("&").append(IHaysSearchConstants.JOB_MAX_TEMP).append("=").append(maxT).append("&").append(IHaysSearchConstants.JOB_SELECT_TEMP).append("=").append(typeT);
        }
        if( contentType != null)
        	m_binder.putLocal(IHaysSearchConstants.CONTENT_TYPE, contentType);
        	//parameters.append("&").append(IHaysSearchConstants.CONTENT_TYPE).append("=").append(contentType);
     //   parameters.append("&").append(JOB_INTERN).append("=").append(jobInternational);
    //	parameters.append("&").append(JOB_SPONSORED).append("=").append(jobSponsored);
		
        if( locale != null)
        	m_binder.putLocal(IHaysSearchConstants.LOCALE, locale);
        	//parameters.append("&").append(IHaysSearchConstants.LOCALE).append("=").append(locale);
        
        if( location != null)
        	m_binder.putLocal(IHaysSearchConstants.JOB_LOCATION, location);
        	//parameters.append("&").append(IHaysSearchConstants.JOB_LOCATION).append("=").append(location);
        if( neLongitude != null && neLatitude != null) {
        	m_binder.putLocal(IHaysSearchConstants.NE_LONGITUDE, neLongitude);
        	m_binder.putLocal(IHaysSearchConstants.NE_LATITUDE, neLatitude);
        	//parameters.append("&").append(IHaysSearchConstants.NE_LONGITUDE).append("=").append(neLongitude);
        //	parameters.append("&").append(IHaysSearchConstants.NE_LATITUDE).append("=").append(neLatitude);
        }
        if( level != null)
        	m_binder.putLocal(IHaysSearchConstants.LEVEL, level);
        	//parameters.append("&").append(IHaysSearchConstants.LEVEL).append("=").append(level);
        if( radius != null)
         	m_binder.putLocal(IHaysSearchConstants.RADIUS, radius);
        	//parameters.append("&").append(IHaysSearchConstants.RADIUS).append("=").append(radius);
        if( sortfield != null)
        	m_binder.putLocal(IHaysSearchConstants.SORTFIELD, sortfield);
        	//parameters.append("&").append(IHaysSearchConstants.SORTFIELD).append("=").append(sortfield);
        if( sortorder != null)
        	m_binder.putLocal(IHaysSearchConstants.SORTORDER, sortorder);
        	//parameters.append("&").append(IHaysSearchConstants.SORTORDER).append("=").append(sortorder);
        if( jobInternational != null)
        	m_binder.putLocal(IHaysSearchConstants.JOB_INTERN, jobInternational);
        	//parameters.append("&").append(IHaysSearchConstants.JOB_INTERN).append("=").append(jobInternational);
        if( jobNonNational != null)
        	m_binder.putLocal(IHaysSearchConstants.JOB_NONNATIONL, jobNonNational);
        	//parameters.append("&").append(IHaysSearchConstants.JOB_NONNATIONL).append("=").append(jobNonNational);
        if( isOnlyJobTitle != null)
        	m_binder.putLocal(IHaysSearchConstants.IS_ONLY_JOB_TITLE, isOnlyJobTitle);
        	//parameters.append("&").append(IHaysSearchConstants.IS_ONLY_JOB_TITLE).append("=").append(isOnlyJobTitle);
        if( isFuzzy != null)
        	m_binder.putLocal(IHaysSearchConstants.IS_FUZZY_SEARCH, isFuzzy);
        	//parameters.append("&").append(IHaysSearchConstants.IS_FUZZY_SEARCH).append("=").append(isFuzzy);
        if( location_id != null)
        	m_binder.putLocal(IHaysSearchConstants.LOCATION_ID, location_id);
        	parameters.append("&").append(IHaysSearchConstants.LOCATION_ID).append("=").append(location_id);
       
        if( isHomePageSearch != null && isHomePageSearch.equalsIgnoreCase("1")){
        	m_binder.putLocal(IHaysSearchConstants.IS_HOME_SEARCH, isHomePageSearch);
        	//parameters.append("&").append(IHaysSearchConstants.IS_HOME_SEARCH).append("=").append(isHomePageSearch);
        }
        if( job_category != null && !job_category.equalsIgnoreCase("")){
        	m_binder.putLocal(IHaysSearchConstants.JOB_CATEGORY, job_category);
        	//parameters.append("&").append(IHaysSearchConstants.JOB_CATEGORY).append("=").append(job_category);
        }
        // added for r7
        if( isAdvanceSearch != null && !isAdvanceSearch.equalsIgnoreCase("")){
        	m_binder.putLocal(IHaysSearchConstants.IS_ADVANCE_SEARCH, isAdvanceSearch);
        	//parameters.append("&").append(IHaysSearchConstants.IS_ADVANCE_SEARCH).append("=").append(isAdvanceSearch);
        }
        //added for r7 alert form requirement
        if( jobPostCode != null && !jobPostCode.equalsIgnoreCase("")){
        	m_binder.putLocal(IHaysSearchConstants.JOB_POST_CODE, jobPostCode);
        	//parameters.append("&").append(IHaysSearchConstants.JOB_POST_CODE).append("=").append(jobPostCode);
        }
        
      //added for r8 
        if( locationSet != null && !locationSet.equalsIgnoreCase("")){
        	m_binder.putLocal("location_set", locationSet);
        	//parameters.append("&").append("location_set").append("=").append(locationSet);
        }
    
		
		// m_binder.putLocal("QueryParameters", parameters.toString());
		 SystemUtils.trace("hays_search", "validateSearchParams() QueryParameters:  " + parameters.toString());
		 SystemUtils.trace("hays_search", "validateSearchParamsNew() binder after validating:  " + super.m_binder.getLocalData());
		 SystemUtils.trace("hays_search", "mandatory parameters()   " + industry+","+keywords+","+jobTypeP+","+jobTypeT+","+jobTypeC+","+neLatitude+","+neLongitude+","+locationSet+","+job_category+",");
    
        // check mandatory parameters
        if( !isTitleSet()  && (industry == null || industry.trim().length() == 0)
        		&& (keywords == null || keywords.trim().length() == 0)
        		&& jobTypeP == null 
        		&& jobTypeT == null 
        		&& jobTypeC == null 
        		&& (neLatitude == null || neLatitude.equals("0") || neLatitude.trim().length()== 0) && (neLongitude == null || neLongitude.equals("0") || neLatitude.trim().length()== 0) && (locationSet == null || locationSet.trim().length() == 0) && (job_category == null || job_category.trim().length() == 0) ) {
        	
        	 SystemUtils.trace("hays_search", "validation error111: " + er);
        	er = "csInvalidSearchParametrs";
			
        } else { // validate salary
	        try {
	        	if( (jobTypeP != null && !isValid(minP, maxP, typeP)) || 
	        			(jobTypeC != null && !isValid(minC, maxC, typeC)) ||
	        			( jobTypeT != null && !isValid(minT, maxT, typeT) ) ) {
	        		er = "csInvalidSalaryParameters";
	        		
	        	}
	        } catch(Exception ex) {
	        	er = "csInvalidSalaryParameters";
	        }
        }
        SystemUtils.trace("hays_search", "validation error: " + er);
        if( er != null)
        	m_binder.putLocal("STATUS", er);
	}
	private boolean isTitleSet(){
		SystemUtils.trace("hays_search", "isTitleSet ");
		String jobTitle = super.m_binder.getLocal(IHaysSearchConstants.JOB_TITLE);
		String jobTitleFull = super.m_binder.getLocal(IHaysSearchConstants.JOB_TITLE_FULL);
		String ref = super.m_binder.getLocal(IHaysSearchConstants.JOB_TITLE_REF);
		if(jobTitle != null && jobTitle.trim().length() > 0)
		{
			SystemUtils.trace("hays_search", "First if");
			return true;
		}
		if(jobTitleFull != null && jobTitleFull.trim().length() > 0)
		{	
			SystemUtils.trace("hays_search", "Second if");
			return true;
		
		}
		if(ref != null && ref.trim().length() > 0)
		{
			SystemUtils.trace("hays_search", "Third if");
			return true;
		}
			return false;
	}
	
	private boolean isValid(String salaryMin, String salaryMax, String paymentType) throws NumberFormatException, DataException {
		SystemUtils.trace("hays_search", "validate salary: " + new StringBuffer("min = ").append(salaryMin).append(", max = ").append(salaryMax).append(" payment type = ").append(paymentType));
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
			double min = SearchCommons.calculateUnifiedSalary(salaryMin, paymentType);
            double max = SearchCommons.calculateUnifiedSalary(salaryMax, paymentType);
            SystemUtils.trace("hays_search", "validate salary range: " + min + ", " + max);
            if( min <= max && IHaysSearchConstants.PAYMENT_TYPES.indexOf(paymentType) >= 0) {
            	return true;
            }
		}
		
		return isValid;
	}
	
	private String processSalary(String salaryMin, String salaryMax, String paymentType, String jobType) throws DataException {
		SystemUtils.trace("hays_search", "validate salary: " + new StringBuffer("min = ").append(salaryMin).append(", max = ").append(salaryMax).append(" payment type = ").append(paymentType).append(", jobType = ").append(jobType));
		
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
                double min = SearchCommons.calculateUnifiedSalary(salaryMin, paymentType);
                double max = SearchCommons.calculateUnifiedSalary(salaryMax, paymentType);
                SystemUtils.trace("hays_search", "Min-Max:" + min+","+max+"Payment Type:"+paymentType);
        		
        		if( min <= max && IHaysSearchConstants.PAYMENT_TYPES.indexOf(paymentType) >= 0) {
        			 SystemUtils.trace("hays_search", "Min-Max in If condition:" + min+","+max+"Payment Type:"+paymentType);
        			query.append("(");
	        		query.append(" (SDATA(xMinSalary <= ").append(min).append(") AND SDATA(xMaxSalary >= ").append(min).append(") )");
	        		query.append(" OR (SDATA(xMinSalary <= ").append(max).append(") AND SDATA(xMaxSalary >= ").append(max).append(") )");
	        		query.append(" OR (SDATA(xMinSalary >= ").append(min).append(") AND SDATA(xMaxSalary <= ").append(max).append(") )");
	        		query.append(")");
	        		if( jobType != null)
	        			query.append(" AND (({").append(jobType).append("}) WITHIN xJobType ) ");
        		}
        		 SystemUtils.trace("hays_search", "Query in processSalary function:" + query);
    		}catch(Exception ex) {
    			SystemUtils.trace("hays_search", "exception while generating query: " + ex);
    			String er = "csInvalidSalaryParameters";
    			throw new DataException(er);
    		}
		}
		return query.toString();
		
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
			    result = callServiceHandlerSpecialism(m_service, "getRelatedTerms", properties, relation+metadata);
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
	
	
	
	
	//////////////added for r7 end///////////////////////////////////////////////////////
}
