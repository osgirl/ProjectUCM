package hays.co.uk.search;

import hays.co.uk.HaysUtil;
import infomentum.ontology.navigation.OntologyNavigationHandler;
import intradoc.common.ClassHelper;
import intradoc.common.LocaleUtils;
import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.data.DataBinder;
import intradoc.data.DataException;
import intradoc.data.DataResultSet;
import intradoc.data.FieldInfo;
import intradoc.data.ResultSet;
import intradoc.data.Workspace;
import intradoc.provider.Provider;
import intradoc.provider.Providers;
import intradoc.server.Service;
import intradoc.server.ServiceHandler;
import intradoc.shared.SharedObjects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

public class HaysApiContentSearchHandler extends ServiceHandler
{
	public final static String TRACE_NAME = "webapi_content_search";
	
	public void appendExtraSearchParameters() throws DataException, ServiceException
	{
		String isExtraParams =getData("isExtraParams");
		String isMobile =getData("isMobile");
		String queryText =getData("queryText");
		String islinkedin =getData("islinkedin");
		String outputFormat =getData("outputFormat");
		DataResultSet drset = (DataResultSet)m_binder.getResultSet("SearchResults");
		
		m_binder.removeResultSet("SearchResults");
		
		SystemUtils.trace(TRACE_NAME,  "isMobile: " + isMobile);
		SystemUtils.trace(TRACE_NAME,  "isExtraParams: " + isExtraParams);
		SystemUtils.trace(TRACE_NAME,  "queryText: " + queryText);
		SystemUtils.trace(TRACE_NAME,  "islinkedin: " + islinkedin);
		SystemUtils.trace(TRACE_NAME,  "outputFormat: " + outputFormat);
		//SystemUtils.trace(TRACE_NAME,  "drset: " + drset);
		
        if (("Y".equalsIgnoreCase(isMobile) || "Y".equalsIgnoreCase(isExtraParams))	&&  (drset!=null && drset.getNumRows()>0))
        {
        	//added xLinks and sponsoredjobs image url fields in SearchResults ResultSet for Job Search API
	        String restJobDetailUrl=SharedObjects.getEnvironmentValue("restJobDeatilUrl");
	        if(queryText.contains("(PromotionalContent WITHIN dDocType)")){
	        	restJobDetailUrl=SharedObjects.getEnvironmentValue("restContentDetailUrl");
	        }
        	int fieldIndex = drset.getNumFields();
	        String docDIDList="";	
	        StringBuffer sb = new StringBuffer(100);
	      
	        SystemUtils.trace("search",  "\nField Length: " + fieldIndex);
	        Vector<FieldInfo> links = new Vector<FieldInfo>();
	        FieldInfo linksfieldInfo = new FieldInfo();
	        linksfieldInfo.m_name="xlinks";
	        linksfieldInfo.m_type = 6;
	        links.add(linksfieldInfo);
	        drset.appendFields(links);
	        SystemUtils.trace(TRACE_NAME,  "\nField Length After append: " + drset.getNumFields());
	        String serverAddress=SharedObjects.getEnvironmentValue("HttpServerAddress");
	        do{
	        	SystemUtils.trace(TRACE_NAME,  "\nIn while: " + fieldIndex);
        		int currentRowIndex =  drset.getCurrentRow();
        		String dDocName = drset.getCurrentRowMap().get("dDocName").toString();
        		//String dRevLabel = drset.getCurrentRowMap().get("dRevLabel").toString();
        		Vector v = drset.getCurrentRowValues();
        		if("Y".equalsIgnoreCase(islinkedin))
        		{
        			v.set(fieldIndex, restJobDetailUrl+(dDocName.toLowerCase())+"?format="+outputFormat+"&islinkedin="+islinkedin);
        		}
        		else
        		{
        			v.set(fieldIndex, restJobDetailUrl+(dDocName.toLowerCase())+"?format="+outputFormat);
        		}        		
        		sb=sb.append(",").append(drset.getCurrentRowMap().get("dID"));
       		
	        }while(drset.next());
	        
	        //Manipulate and add xDescription, xSalaryDescription metadata field in ResultSet SearchResults
	        			        
	        docDIDList=sb.substring(1,sb.length());			      
	        DataBinder docParams = new DataBinder();
	        docParams.putLocal("docDIDList", docDIDList); 
	              
	        SystemUtils.trace(TRACE_NAME,"Final Job ID list ="+ docDIDList);
	        SystemUtils.trace(TRACE_NAME,"Params ="+ docParams.toString());
	        
	        Provider p = Providers.getProvider("SystemDatabase");	    	    
	        Workspace databaseServerWs = (Workspace)p.getProvider();
			ResultSet jobrset = databaseServerWs.createResultSet("QGetJobDetails", docParams);
			DataResultSet jobdrset=new DataResultSet(); 
			jobdrset.copy(jobrset);
    		//start update Qs text
			if(jobdrset !=null){
				int questionIndex= jobdrset.getFieldInfoIndex("xQuestions");;
				do{
					String question = jobdrset.getStringValueByName("xQuestions");
					if(question != null && question.length() >0){
						question = updateQuestionText(question);
						jobdrset.setCurrentValue(questionIndex, question);
					}
				}while(jobdrset.next());
			}
			
			//end update Qs text

			SystemUtils.trace(TRACE_NAME,"Provider type="+ p.getProviderData().toString());
			SystemUtils.trace(TRACE_NAME,"ResultSet jobrset="+ jobrset);
			SystemUtils.trace(TRACE_NAME,"DataResultSet jobdrset ="+ jobdrset.toString());
			Vector<String> extraFields = new Vector<String>();
			
			extraFields.add("xDescription");
			SystemUtils.trace(TRACE_NAME,"resultset contains  xSalaryDescription="+ drset.getFieldInfoIndex("xSalaryDescription"));
			
			if(drset.getFieldInfoIndex("xSalaryDescription") == -1){
				extraFields.add("xSalaryDescription");
			}
			extraFields.add("dStatus");
			extraFields.add("xEmailMeOnApplication");
			extraFields.add("xQuestions");
			drset.appendFields(extraFields);
			if (drset.first() && jobdrset.first()) {
				drset.merge("dID", jobdrset, true);
				//SystemUtils.trace("hays_search", "After RS were merged for salary and location description: " + drset);
			}			
			m_binder.addResultSet("SearchResults", drset);
			SystemUtils.trace(TRACE_NAME,"EXTRA FIELDS HAVE BEEN MERGED INTO RESULTSET.");
        }
	}
	
	private String updateQuestionText(String questionDetails){
		//questionDetails format --> id,Y;id1,N
		String questionIds="";
		SystemUtils.trace(TRACE_NAME,"Found Qs ="+ questionDetails);
		
		//get list of question Id's
		if(questionDetails != null){
			String[] questionAutoRejects = questionDetails.split(";");
			String questionAutoReject = null, question =null, autoReject=null;
			for(int i=0;i<questionAutoRejects.length;i++){
				questionAutoReject = questionAutoRejects[i].trim();
				String questions[] = questionAutoReject.split(",");
				question =  questions[0];
				questionIds = questionIds+","+question;
				autoReject = questions[1];
			}
			questionIds = questionIds.substring(1);
			SystemUtils.trace(TRACE_NAME,"Question Ids ="+ questionIds);
			DataBinder docParams = new DataBinder();
	        docParams.putLocal("questionIds", questionIds); 
	        Provider p = Providers.getProvider("SystemDatabase");	    	    
	        Workspace databaseServerWs = (Workspace)p.getProvider();
			ResultSet jobrset = null;
			DataResultSet jobdrset=new DataResultSet();
			try {
				jobrset = databaseServerWs.createResultSet("Get_Questions_By_Id", docParams);
				jobdrset.copy(jobrset);
			} catch (DataException e) {
				SystemUtils.dumpException(TRACE_NAME,e);
			}
			if(jobdrset !=null && !jobdrset.isEmpty()){
				questionDetails = ";"+questionDetails;
				String id=null, text=null, isActive=null, temp = null;
				do{
					id = jobdrset.getStringValueByName("QUESTION_ID");
					text = jobdrset.getStringValueByName("QUESTION_TEXT");
					isActive = jobdrset.getStringValueByName("IS_ACTIVE");
					if("0".equals(isActive)){
						//question has been deleted - do not return the question
						int posStart = questionDetails.indexOf(";"+id);
						int posEnd = questionDetails.indexOf(';', posStart+1);
						if(posEnd == -1){
							posEnd = questionDetails.length();
						}
						temp = questionDetails.substring(posStart, posEnd);
						SystemUtils.trace(TRACE_NAME,"Qs has been deleted ="+ temp);
						questionDetails = questionDetails.replaceFirst(temp, "");
						
					}else{
						int pos = questionDetails.indexOf(id);
						questionDetails = questionDetails.replaceFirst(";"+id+",", ";"+id+","+text+",");
					}
					
				}while(jobdrset.next());
				
				questionDetails = questionDetails.substring(1);
			}
		
		}
		SystemUtils.trace(TRACE_NAME,"Updated Qs ="+ questionDetails);
		return questionDetails;
	}
	
	public void processContentApiSearchParameters() throws DataException, ServiceException
	{
		super.m_binder.putLocal("SearchQueryFormat", "universal");

		// QUERY
		StringBuffer query = new StringBuffer();
		StringBuffer queryPartforSearchPOC = new StringBuffer();
		StringBuffer queryPart = new StringBuffer();
		String level = super.m_binder.getLocal(IHaysSearchConstants.LEVEL);

		if ("Hays".equals(super.m_binder.getLocal("searchType")))
		{
			SystemUtils.trace("webapi_content_search", "\n\nStarting search process: " + m_binder.getLocalData());

			// KEYWORDS
			String keywords = super.m_binder.getLocal(IHaysSearchConstants.JOB_KEYWORDS);
			super.m_binder.putLocal(IHaysSearchConstants.IS_FUZZY_SEARCH, "true");// Fuzzy true for all countries(set true for alert requirement)
			String isFuzzyStr = super.m_binder.getLocal(IHaysSearchConstants.IS_FUZZY_SEARCH);
			String isOnlyJobTitleStr = super.m_binder.getLocal(IHaysSearchConstants.IS_ONLY_JOB_TITLE);
			SystemUtils.trace("webapi_content_search", "\nisFuzzyStr : " + isFuzzyStr + " \n isOnlyJobTitle : " + isOnlyJobTitleStr);
			String thes_locale = m_binder.getLocal(IHaysSearchConstants.LOCALE);
			SystemUtils.trace("webapi_content_search", "thes_locale" + thes_locale);
			String thes_name = HaysUtil.getLanguageFromLocale(thes_locale);
			boolean isFuzzy = (isFuzzyStr != null && "true".equals(isFuzzyStr)) ? true : false;
			boolean isOnlyJobTitle = (isOnlyJobTitleStr != null && "Y".equals(isOnlyJobTitleStr)) ? true : false;

			//Start POC Builder
			if (keywords != null)
			{

				QueryBuilderFactory queryBuilderFactory = QueryBuilderFactory.getInstance();
				AbstractHaysQueryBuilder abstractHaysQueryBuilder = queryBuilderFactory.getQueryBuilder(isFuzzy, isOnlyJobTitle, thes_name, "", level, null, false, null);
				abstractHaysQueryBuilder.setInputString(keywords);
				abstractHaysQueryBuilder.buildQueryPart();
				queryPart = abstractHaysQueryBuilder.getQueryPart();

				SystemUtils.trace("webapi_content_search", "Trace queryPart : " + queryPart.toString() + "isOnlyJobTitleStr::" + isOnlyJobTitleStr);

			}

			String ref = super.m_binder.getLocal(IHaysSearchConstants.JOB_TITLE_REF);
			query.append("((( ");
			if (queryPart.length() > 0)
			{
				if (query.length() > 0)
				{
					query.append(" (idccontenttrue)*10*10 AND ");
				}
				query.append("(").append(queryPart);

				if (ref == null || "".equals(ref))
				{
					query.append(")");
				}
			}
			else
			{
				query.append("(idccontenttrue)*10*10");

			}

			SystemUtils.trace("webapi_content_search", "processApiSearchParameters: Job Title='" + query + "'");
			int index = query.indexOf("AND");
			if (index > 0)
			{
				queryPartforSearchPOC.append(query.substring(index)).append("*10*10");
			}
			SystemUtils.trace("webapi_content_search", "QueryPartforSEarchPOC" + queryPartforSearchPOC);

			//JobId's for WebCenter 
			String jobIds = super.m_binder.getLocal("jobid");
			if (jobIds != null && jobIds.trim().length() > 0)
			{
				String[] jobIdsArray = jobIds.split(",");
				jobIds = "";
				for (int i = 0; i < jobIdsArray.length; i++)
				{
					jobIds = jobIds + "{" + jobIdsArray[i] + "},";
				}
				jobIds = jobIds.substring(0, jobIds.length() - 1);

				if (query.length() > 0)
				{
					query.append(" AND ");
					queryPartforSearchPOC.append(" AND ");
				}
				query.append("((").append(jobIds).append(") WITHIN dDocName )*10*10");
				queryPartforSearchPOC.append("((").append(jobIds).append(") WITHIN dDocName )*10*10");
				SystemUtils.trace("webapi_content_search", "processApiSearchParameters: For JobIdLIst='" + query + "'");
			}

			// INDUSTRY
			String industriesStr = super.m_binder.getLocal(IHaysSearchConstants.JOB_INDUSTRY);
			if (industriesStr == null)
			{
				StringBuffer industries = processCheckBoxes(IHaysSearchConstants.JOB_INDUSTRY);
				if (industries.length() > 0)
				{
					industriesStr = industries.substring(1);
				}
			}

			if (industriesStr != null && industriesStr.trim().length() > 0)
			{
				if (industriesStr.endsWith(","))
					industriesStr = industriesStr.substring(0, industriesStr.length() - 1);
				industriesStr = industriesStr.replaceAll(",[ ]?", " | ");

				if (query.length() > 0)
				{
					query.append(" AND ");
					queryPartforSearchPOC.append(" AND ");

				}

				query.append("((").append(industriesStr).append(") WITHIN xIndustry )*10*10"); //maxed out score
				queryPartforSearchPOC.append("((").append(industriesStr).append(") WITHIN xIndustry )*10*10");
				SystemUtils.trace("webapi_content_search", "processApiSearchParameters: Industry='" + query + "'");
			}

			// CATEGORY
			String categories = super.m_binder.getLocal(IHaysSearchConstants.JOB_CATEGORY);
			if (categories != null && categories.trim().length() > 0)
			{

				// added for r7 start
				StringBuffer result = new StringBuffer();

				StringBuffer finalsubTerms = new StringBuffer();
				SystemUtils.trace("webapi_content_search", "categories 1st =" + categories);

				if (categories.startsWith(";"))
					categories = categories.substring(1);
				if (categories.endsWith(";"))
					categories = categories.substring(0, categories.length() - 1);
				categories = categories.replaceAll("[,;][ ]?", "#");

				SystemUtils.trace("webapi_content_search", "categories after replace all=" + categories);

				String[] categoriesArray = categories.split("#");

				for (int i = 0; i < categoriesArray.length; i++)
				{

					String subTerms = ontGetRelatedTerms(categoriesArray[i], "xCategory@hays:ParentTerm");
					SystemUtils.trace("webapi_content_search", "subTerms=" + subTerms);

					if ("[]".equalsIgnoreCase(subTerms))
					{
						subTerms = categoriesArray[i];
					}
					if (subTerms.length() > 0)
					{
						subTerms = categoriesArray[i] + "," + subTerms.substring(1, subTerms.length() - 1);
					}
					SystemUtils.trace("webapi_content_search", "for i==" + i + "  ,subTerms=" + subTerms);
					if (i > 0)
					{
						finalsubTerms.append(" | ");
					}
					finalsubTerms.append(subTerms);
					SystemUtils.trace("webapi_content_search", "for i==" + i + "  ,finalsubTerms=" + finalsubTerms.toString());
				}
				SystemUtils.trace("webapi_content_search", "final  finalsubTerms=" + finalsubTerms.toString());
				categories = finalsubTerms.toString();
				categories = categories.replaceAll("[,;#][ ]?", " | ");

				if (query.length() > 0)
				{
					query.append(" AND ");
					queryPartforSearchPOC.append(" AND ");
				}
				query.append("((").append(categories).append(") WITHIN xCategory )*10*10");
				queryPartforSearchPOC.append("((").append(categories).append(") WITHIN xCategory )*10*10");
				SystemUtils.trace("webapi_content_search", "processApiSearchParameters: Category='" + query + "'");

			}

			//POSTED DATE

			//added for R7 
			String posteddate = super.m_binder.getLocal(IHaysSearchConstants.JOB_POSTED_DATE_FILTER);
			SystemUtils.trace("webapi_content_search", "JOB_POSTED_DATE_FILTER = ====" + posteddate);
			if (posteddate != null && posteddate.trim().length() > 0)
			{
				int indexposted = posteddate.indexOf(',');
				SystemUtils.trace("webapi_content_search", "Inside excluddde JOB_POSTED_DATE_FILTER = ====" + indexposted);
				if (indexposted == -1)
				{
					if (query.length() > 0)
					{
						query.append(" AND ");
						queryPartforSearchPOC.append(" AND ");
					}
					query.append(" (").append(" SDATA(dInDate > '").append(posteddate).append("'))*10*10");
					queryPartforSearchPOC.append(" (").append(" SDATA(dInDate > '").append(posteddate).append("'))*10*10");
				}
				else
				{
					posteddate = posteddate.substring(0, indexposted);

					SystemUtils.trace("webapi_content_search", "processApiSearchParameters: posteddateexclude ='" + posteddate);
					if (query.length() > 0)
					{
						query.append(" AND ");
						queryPartforSearchPOC.append(" AND ");
					}
					query.append(" (").append(" SDATA(xEventDate > '").append(posteddate).append("'))");
					queryPartforSearchPOC.append(" (").append(" SDATA(xEventDate > '").append(posteddate).append("'))");

				}
			}
			SystemUtils.trace("webapi_content_search", "processApiSearchParameters: posteddateexclude ='" + posteddate + ", " + query + "'");

			//end Posted Date

			// CONTENT TYPE
			String contentType = super.m_binder.getLocal(IHaysSearchConstants.CONTENT_TYPE);
			if (!("Jobs".equals(contentType) || "Candidates".equals(contentType)))
			{
				if (query.length() > 0)
				{
					query.append(" AND ");
					queryPartforSearchPOC.append(" AND ");
				}
				query.append(" (").append(contentType).append(" WITHIN dDocType)*10*10");
				queryPartforSearchPOC.append(" (").append(contentType).append(" WITHIN dDocType)*10*10");
			}
			SystemUtils.trace("webapi_content_search", "processApiSearchParameters: Content Type ='" + contentType + ", " + query + "'");

			// SUB CONTENT TYPE
			String subContentType = super.m_binder.getLocal(IHaysSearchConstants.SUB_CONTENT_TYPE);
			if (subContentType != null && !("".equals(subContentType)))
			{
				if (query.length() > 0)
				{
					query.append(" AND ");
					queryPartforSearchPOC.append(" AND ");
				}
				query.append(" (").append(subContentType).append(" WITHIN XSUBTYPE)*10*10");
				queryPartforSearchPOC.append(" (").append(contentType).append(" WITHIN XSUBTYPE)*10*10");
			}
			SystemUtils.trace("webapi_content_search", "processApiSearchParameters: Content Type ='" + subContentType + ", " + query + "'");

			// RELEASE DATE
			String releaseDate = super.m_binder.getLocal(IHaysSearchConstants.RELEASE_DATE);
			if (releaseDate != null && releaseDate.length() > 0)
			{
				if (query.length() > 0)
				{
					query.append(" AND ");
					queryPartforSearchPOC.append(" AND ");
				}
				query.append(" (").append(" SDATA(dInDate > '").append(releaseDate).append("'))*10*10");
				queryPartforSearchPOC.append(" (").append(" SDATA(dInDate > '").append(releaseDate).append("'))*10*10");
			}
			SystemUtils.trace("webapi_content_search", "processApiSearchParameters: Release Date ='" + releaseDate + ", " + query + "'");

			// Registered Date
			String registeredDate = super.m_binder.getLocal(IHaysSearchConstants.REGISTERED_DATE);
			if (registeredDate != null && registeredDate.length() > 0)
			{
				if (query.length() > 0)
				{
					query.append(" AND ");
					queryPartforSearchPOC.append(" AND ");
				}
				query.append(" (").append(" SDATA(xEventDate > '").append(registeredDate).append("'))*10*10");
				queryPartforSearchPOC.append(" (").append(" SDATA(xEventDate > '").append(registeredDate).append("'))*10*10");
			}
			SystemUtils.trace("webapi_content_search", "processApiSearchParameters: Registered Date ='" + registeredDate + ", " + query + "'");

			// LOCALE
			String locale = m_binder.getLocal(IHaysSearchConstants.LOCALE);
			if (locale != null && locale.trim().length() > 0)
			{
				query.append(" AND ({").append(locale).append("} WITHIN xLocale)*10*10");
				queryPartforSearchPOC.append(" AND ({").append(locale).append("} WITHIN xLocale)*10*10");
			}

			//	MR 219
			// MicrositeCode
			String micrositecode = m_binder.getLocal(IHaysSearchConstants.micrositeCode);
			if (micrositecode != null && micrositecode.trim().length() > 0)
			{
				query.append(" AND ({").append(micrositecode).append("} WITHIN xMicroSiteCode)*10*10");
				queryPartforSearchPOC.append(" AND ({").append(micrositecode).append("} WITHIN xMicroSiteCode)*10*10");
			}
			//end MR 219	

			//added for LinkedIn Job
			String linkedInJob = super.m_binder.getLocal("linked_in");
			if (linkedInJob != null)
			{
				if (query.length() > 0)
					if (("1").equals(linkedInJob) || ("Y").equalsIgnoreCase(linkedInJob) || ("true").equalsIgnoreCase(linkedInJob) || ("Yes").equalsIgnoreCase(linkedInJob))
					{
						//query.append(" AND (Y WITHIN xisLinkedIn)");
						//query.append(") )");
						query.append(" AND ({").append("Y").append("} WITHIN xisLinkedIn)*10*10");
						queryPartforSearchPOC.append(" AND ({").append("Y").append("} WITHIN xisLinkedIn)*10*10");

					}
			}

			// LOCATION		
			m_binder.putLocal(IHaysSearchConstants.EXCLUDE, "0");
			//String latitudeF = super.m_binder.getLocal(IHaysSearchConstants.NE_LATITUDE_FILTER);
			//String longitudeF = super.m_binder.getLocal(IHaysSearchConstants.NE_LONGITUDE_FILTER);
			//String radiusF = super.m_binder.getLocal(IHaysSearchConstants.RADIUS_FILTER);
			//String levelF = super.m_binder.getLocal(IHaysSearchConstants.LEVEL_FILTER);
			String latitude = super.m_binder.getLocal(IHaysSearchConstants.NE_LATITUDE);
			String longitude = super.m_binder.getLocal(IHaysSearchConstants.NE_LONGITUDE);
			String radius = super.m_binder.getLocal(IHaysSearchConstants.RADIUS);
			level = super.m_binder.getLocal(IHaysSearchConstants.LEVEL);
			int count = 0;

			//String location_set = super.m_binder.getLocal("location_set");

			// Will be non-empty only for APAC countries.
			//SystemUtils.trace("webapi_content_search", "location set"+location_set);

			String location_id = super.m_binder.getLocal("location_id");
			if (location_id != null && !location_id.equals(""))
			{
				ResultSet localeResultSet = this.m_binder.getResultSet("LOCALE_DETAILS");
				String locationColumn = localeResultSet.getStringValueByName("LOCATION_COLUMN");
				String locationIds = location_id;
				DataBinder docParams = new DataBinder();
				docParams.putLocal("locationColumn", locationColumn);
				docParams.putLocal("locationIds", locationIds);
				Provider p = Providers.getProvider("SystemDatabase");
				Workspace databaseServerWs = (Workspace) p.getProvider();
				ResultSet locationsRset = databaseServerWs.createResultSet("LocationDetailsQuery", docParams);
				DataResultSet locationsdrset = new DataResultSet();
				locationsdrset.copy(locationsRset);

				SystemUtils.trace("webapi_content_search", "Location column:" + locationColumn);
				SystemUtils.trace("webapi_content_search", "LocationID's:" + locationIds);
				SystemUtils.trace("webapi_content_search", "Location Data ResultSet:" + locationsdrset);

				if (locationsdrset.getNumRows() > 0)
				{

					do
					{

						int currentRowIndex = locationsdrset.getCurrentRow();
						level = locationsdrset.getStringValueByName("level");
						longitude = locationsdrset.getStringValueByName("longitude");
						latitude = locationsdrset.getStringValueByName("latitude");
						;
						SystemUtils.trace("webapi_content_search", "Location:" + locationsdrset.getStringValueByName("default_description"));
						SystemUtils.trace("webapi_content_search", "level:" + level);
						SystemUtils.trace("webapi_content_search", "Longitude:" + longitude);
						SystemUtils.trace("webapi_content_search", "Latitude:" + latitude);
						if (latitude != null && latitude.trim().length() > 0 && !latitude.equals(IHaysSearchConstants.DEFAULT) && longitude != null && longitude.trim().length() > 0
								&& !longitude.equals(IHaysSearchConstants.DEFAULT) && radius != null && radius.trim().length() > 0 && !radius.equals(IHaysSearchConstants.DEFAULT))
						{

							processLocation(longitude, latitude, radius, "");
						}
						else
						{
							processLocation(null, null, null, "");
						}

						if (longitude != null && longitude.trim().length() > 0 && !longitude.equals(IHaysSearchConstants.DEFAULT) && latitude != null && latitude.trim().length() > 0
								&& !latitude.equals(IHaysSearchConstants.DEFAULT) && level != null && level.trim().length() > 0 && !level.equals(IHaysSearchConstants.DEFAULT))
						{
							SystemUtils.trace("webapi_content_search", "Inside 3rd if condition: " + longitude + ", " + latitude + ", " + radius + ", level" + level);
							if (count > 0)
							{
								query.append(" OR ");
								queryPartforSearchPOC.append(" OR ");
							}
							else
							{
								query.append(" AND ( "); //Opening bracket
								queryPartforSearchPOC.append(" AND ( ");
							}
							query.append(constructLocationQuery(longitude, latitude, level));
							queryPartforSearchPOC.append(constructLocationQuery(longitude, latitude, level));
							SystemUtils.trace("webapi_content_search", "Query after adding location " + query);
						}
						count++;

					}
					while (locationsdrset.next());

					query.append(")"); //Closing bracket
					queryPartforSearchPOC.append(")");
				}

			}

			// INTERNATIONAL
			String jobInternational = super.m_binder.getLocal(IHaysSearchConstants.JOB_INTERN);
			if (jobInternational != null)
			{
				if (query.length() > 0)

					if (("1").equals(jobInternational) || ("Y").equals(jobInternational))
					{
						query.append(" AND (Yes WITHIN xInternational)*10*10");
						query.append(") )");
						queryPartforSearchPOC.append(" AND (Yes WITHIN xInternational)*10*10");
					}
					else
					{
						query.append(" NOT (Yes WITHIN xInternational)");
						queryPartforSearchPOC.append(" NOT (Yes WITHIN xInternational)");
					}
			}
			else
			{
				query.append(") )");
				query.append(" NOT (Yes WITHIN xInternational)");
				queryPartforSearchPOC.append(" NOT (Yes WITHIN xInternational)");
			}

			// Non-Nationals
			String jobNonEU = super.m_binder.getLocal(IHaysSearchConstants.JOB_NONNATIONL);
			if (jobNonEU != null)
			{
				if (query.length() > 0)

					if (("1").equals(jobNonEU) || ("Y").equals(jobNonEU))
					{
						query.append(" AND (Yes WITHIN xNonNationals)");
					}
					else
					{
						query.append(" NOT (Yes WITHIN xNonNationals)");
					}
			}

			// SPONSORED
			String jobSponsored = super.m_binder.getLocal(IHaysSearchConstants.JOB_SPONSORED);
			if (jobSponsored != null)
			{
				if (jobSponsored.equals("1") || jobSponsored.equalsIgnoreCase("Y"))
				{
					query.append(" NOT (idcnull WITHIN xSponsored)");
					queryPartforSearchPOC.append(" NOT (idcnull WITHIN xSponsored)");
				}
			}

			// FILTER
			//query = processFilterSearch(query);
			//queryPartforSearchPOC = processFilterSearch(queryPartforSearchPOC);
			queryPartforSearchPOC.append(")"); //Closing bracket

		}
		query.append(")"); //added to close the query as date weightage is removed

		SystemUtils.trace("webapi_content_search", "processApiSearchParameters: WEIGHTINGSsssssssssss =" + query);

		//end

		SystemUtils.trace(
				"webapi_content_search",
				new StringBuffer("final locations: latitude =").append(m_binder.getLocal(IHaysSearchConstants.NE_LATITUDE)).append(", longitude: ")
						.append(m_binder.getLocal(IHaysSearchConstants.NE_LONGITUDE)).append(", radius: ").append(m_binder.getLocal(IHaysSearchConstants.RADIUS)).toString());

		m_binder.putLocal("QueryText", query.toString());
		SystemUtils.trace("webapi_content_search", "\nFinal query123: =" + query);
		//SystemUtils.info("webapi_content_search \nFinal query123: =" + query );
	}

	/*
	 * Removes unwanted data from search results and also adds new fields required 
	 */
	public void postProcessContentSearch() throws DataException, ServiceException
	{

		DataResultSet drsSearchResults = (DataResultSet) super.m_binder.getResultSet("SearchResults");
		String resultSetContentAPI = SharedObjects.getEnvironmentValue("ResultSetContentAPI");
		String resultSetJobCandAPI = SharedObjects.getEnvironmentValue("ResultSetJobCandAPI");
		List<String> resultSetContentAPIReturnList = Arrays.asList(resultSetContentAPI.split(","));
		List<String> resultSetJobCandAPIReturnList = Arrays.asList(resultSetJobCandAPI.split(","));
		if((drsSearchResults!=null && drsSearchResults.getNumRows()>0)){
			DataResultSet drsSearchResultsFields = new DataResultSet();
			drsSearchResultsFields.copyFieldInfo(drsSearchResults);
			int count = drsSearchResultsFields.getNumFields();
			String fieldName = null;
			List<String> fieldsToBeRemoved = new ArrayList<String>();
			for (int i = 0; i < count; i++)
			{
				fieldName = drsSearchResultsFields.getFieldName(i);
				//SystemUtils.trace("webapi_content_search", "Checking field to be removed: " + fieldName + " " + resultSetContentAPIReturnList.contains(fieldName));
				if (m_binder.getLocal("jobid") != null && m_binder.getLocal("jobid").trim().length() > 0)
				{
					if (!(resultSetJobCandAPIReturnList.contains(fieldName)))
					{
						fieldsToBeRemoved.add(fieldName);
						//SystemUtils.trace("webapi_content_search", "Added field to be removed for Jobs details: " + fieldName);
					}
				}
				else
				{
					if (!(resultSetContentAPIReturnList.contains(fieldName)))
					{
						fieldsToBeRemoved.add(fieldName);
						//SystemUtils.trace("webapi_content_search", "Added field to be removed: " + fieldName);
					}
				}
			}
			drsSearchResults.removeFields(fieldsToBeRemoved.toArray(new String[0]));
			m_binder.removeResultSet("NavigationPages");
			SystemUtils.trace("webapi_content_search", "Added removed fields: " + fieldsToBeRemoved);
		}else{
			m_binder.putLocal("ssChangeHTTPHeader","true");
		}
	}

	private StringBuffer constructLocationQuery(String x, String y, String level)
	{
		String[] arrayLongitude = x.split(";");
		String[] arrayLatitude = y.split(";");
		String[] arrayLevel = level.split(";");
		StringBuffer rez = new StringBuffer();
		for (int i = 0; i < arrayLongitude.length; i++)
		{
			if (rez.length() > 0)
				rez.append(" AND ");
			rez.append("{").append(arrayLongitude[i]).append("#").append(arrayLatitude[i]).append("}");
			rez.append(" WITHIN xHaysLocation").append(arrayLevel[i]);
		}
		if (rez.length() > 0)
		{
			rez.append(")*10*10");
			rez.insert(0, "(");
		}
		return rez;
	}

	private StringBuffer processCheckBoxes(String name)
	{
		StringBuffer values = new StringBuffer();
		Properties prop = super.m_binder.getLocalData();
		String key, value;
		for (Enumeration en = prop.keys(); en.hasMoreElements();)
		{
			key = (String) en.nextElement();
			if (key.indexOf(name) == 0 && key.indexOf("filter") < 0)
			{
				value = super.m_binder.getLocal(key);
				if (value != null)
				{
					value = value.trim();
					if (value.length() > 0)
					{
						values.append(", ").append(value);
					}
				}
			}
		}
		return values;
	}

	private void processLocation(String longitude, String latitude, String radius, String suffix)
	{
		// LOCATION
		String swLatitude = super.m_binder.getLocal(IHaysSearchConstants.SW_LATITUDE + suffix);
		String swLongitude = super.m_binder.getLocal(IHaysSearchConstants.SW_LONGITUDE + suffix);

		// convert radius to miles
		if (radius != null)
		{
			String distUnit = super.m_binder.getLocal(IHaysSearchConstants.DISTANCE_UNIT);
			if (distUnit != null && IHaysSearchConstants.DISTANCE_UNITS.contains(distUnit))
			{
				if (distUnit.equals("km"))
				{
					radius = String.valueOf((int) (Integer.parseInt(radius) * IHaysSearchConstants.KM_MILES_CONVERT));
				}
			}
		}

		SystemUtils.trace("webapi_content_search", new StringBuffer("latitude: ").append(latitude).append(", long: ").append(longitude).append(", radius: ").append(radius).toString());

		if (latitude == null || longitude == null)
		{
			latitude = IHaysSearchConstants.DEFAULT;
			longitude = IHaysSearchConstants.DEFAULT;
			swLatitude = IHaysSearchConstants.DEFAULT;
			swLongitude = IHaysSearchConstants.DEFAULT;
			radius = IHaysSearchConstants.DEFAULT;
		}
		else
		{
			if (swLatitude == null || swLongitude == null)
			{
				swLatitude = IHaysSearchConstants.DEFAULT;
				swLongitude = IHaysSearchConstants.DEFAULT;
			}
			if (radius == null)
			{
				radius = IHaysSearchConstants.DEFAULT_RADIUS;
			}
			if (longitude != null)
			{
				int index = longitude.lastIndexOf(";");
				if (index > 0)
					longitude = longitude.substring(index + 1);
			}
			if (latitude != null)
			{
				int index = latitude.lastIndexOf(";");
				if (index > 0)
					latitude = latitude.substring(index + 1);
			}
		}
		m_binder.putLocal(IHaysSearchConstants.NE_LATITUDE, latitude);
		m_binder.putLocal(IHaysSearchConstants.NE_LONGITUDE, longitude);
		m_binder.putLocal(IHaysSearchConstants.SW_LATITUDE, swLatitude);
		m_binder.putLocal(IHaysSearchConstants.SW_LONGITUDE, swLongitude);
		m_binder.putLocal(IHaysSearchConstants.RADIUS, radius);
	}

	private boolean isTitleSet()
	{

		String ref = super.m_binder.getLocal(IHaysSearchConstants.JOB_TITLE_REF);

		if (ref != null && ref.trim().length() > 0)
			return true;
		return false;
	}

	String ontGetRelatedTerms(String sArg1, String sArg2) throws ServiceException
	{
		SystemUtils.trace("webapi_content_search", "inside ontGetRelatedTerms, sArg1::::=" + sArg1 + " ,sArg2:" + sArg2);
		String result = "";
		String terms = sArg1.trim();
		String meta_relation = sArg2;
		String isObject = "false";
		if (terms != null && meta_relation != null)
		{
			int index = meta_relation.indexOf("@");
			if (index > 0)
			{
				String metadata = meta_relation.substring(0, index);
				String relation = meta_relation.substring(index + 1);
				index = relation.indexOf("@");
				if (index > 0)
				{
					isObject = relation.substring(index + 1);
					relation = relation.substring(0, index);
				}
				Properties properties = new Properties();
				properties.put("ont_metadata", metadata);
				properties.put("relation", relation);
				properties.put("isObject", isObject);
				properties.put("terms", terms);
				SystemUtils.trace("webapi_content_search", "before calling  callServiceHandlerSpecialism, metadata:" + metadata + " ,relation:" + relation + " ,isObject:" + isObject + " ,terms:"
						+ terms);
				result = callServiceHandlerSpecialism(m_service, "getRelatedTerms", properties, relation + metadata);
				SystemUtils.trace("webapi_content_search", "result after::::='" + result);

			}
		}
		return result;
	}

	String callServiceHandlerSpecialism(Service service, String s, Properties properties, String s1) throws ServiceException
	{
		String resultStr = null;
		DataBinder databinder;
		Properties properties1;
		databinder = service.getBinder();
		if (databinder == null)
		{
			return null;
		}
		properties1 = databinder.getLocalData();
		databinder.setLocalData(properties);
		OntologyNavigationHandler ssservicehandler = (OntologyNavigationHandler) service.getHandler("infomentum.ontology.navigation.OntologyNavigationHandler");
		if (ssservicehandler != null)
		{
			ClassHelper classhelper = new ClassHelper();
			classhelper.m_class = ssservicehandler.getClass();
			classhelper.m_obj = ssservicehandler;
			classhelper.invoke(s);
			resultStr = databinder.getLocal(s1);
		}
		databinder.setLocalData(properties1);
		return resultStr;
	}

	//not used, to be removed
	public void apiValidateSearchParams() throws ServiceException, DataException
	{
		{
			SystemUtils.trace("webapi_content_search", "apiValidateSearchParams() binder Before:  " + super.m_binder.getLocalData());
			String jobTitle = super.m_binder.getLocal(IHaysSearchConstants.JOB_TITLE);
			String jobTitleFull = super.m_binder.getLocal(IHaysSearchConstants.JOB_TITLE_FULL);
			String jobTitleFullDescr = super.m_binder.getLocal(IHaysSearchConstants.JOB_TITLE_FULL_DECR);
			String ref = super.m_binder.getLocal(IHaysSearchConstants.JOB_TITLE_REF);
			String industry = super.m_binder.getLocal(IHaysSearchConstants.JOB_INDUSTRY);
			if (industry == null)
			{
				StringBuffer industries = processCheckBoxes(IHaysSearchConstants.JOB_INDUSTRY);
				if (industries.length() > 0)
				{
					industry = industries.substring(1);
				}
			}
			String keywords = super.m_binder.getLocal(IHaysSearchConstants.JOB_KEYWORDS);
			//String typeP = "A";//super.m_binder.getLocal("job_permanent"); // commented for release 8.0
			String typeP = super.m_binder.getLocal(IHaysSearchConstants.JOB_SELECT_PERM);//added for release 8.0
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
			String location_id = super.m_binder.getLocal(IHaysSearchConstants.LOCATION_ID);
			//String isHomePageSearch = super.m_binder.getLocal("isHomePageSearch");
			String job_category = super.m_binder.getLocal(IHaysSearchConstants.JOB_CATEGORY);
			String isAdvanceSearch = super.m_binder.getLocal(IHaysSearchConstants.IS_ADVANCE_SEARCH);// added  for r7
			String isHomePageSearch = super.m_binder.getLocal(IHaysSearchConstants.IS_HOME_SEARCH);
			String jobPostCode = super.m_binder.getLocal(IHaysSearchConstants.JOB_POST_CODE);// added for alert form requirement for r7
			String locationSet = super.m_binder.getLocal("location_set");
			String linkedInJob = super.m_binder.getLocal("linked_in");
			String jobExperienceLevel = super.m_binder.getLocal("experience_level");
			String er = null;
			StringBuffer parameters = new StringBuffer();

			SystemUtils.trace("webapi_content_search", "Sort Field:  " + sortfield);
			SystemUtils.trace("webapi_content_search", "Sort Order:  " + sortorder);

			if (jobTitle != null)
			{
				jobTitle = QueryUtils.encodeForHaysSpecialKeywords(jobTitle);
				parameters.append("&").append(IHaysSearchConstants.JOB_TITLE).append("=").append(jobTitle.replaceAll(IHaysSearchConstants.fuzzySpecialCharacters, " "));
			}
			if (jobTitleFull != null)
			{
				jobTitleFull = QueryUtils.encodeForHaysSpecialKeywords(jobTitleFull);
				parameters.append("&").append(IHaysSearchConstants.JOB_TITLE_FULL).append("=").append(jobTitleFull);
			}
			if (jobTitleFullDescr != null)
			{
				jobTitleFullDescr = QueryUtils.encodeForHaysSpecialKeywords(jobTitleFullDescr);
				parameters.append("&").append(IHaysSearchConstants.JOB_TITLE_FULL_DECR).append("=").append(jobTitleFullDescr);
			}
			if (ref != null)
			{
				ref = QueryUtils.encodeForHaysSpecialKeywords(ref);
				parameters.append("&").append(IHaysSearchConstants.JOB_TITLE_REF).append("=").append(ref);
			}
			if (industry != null)
				parameters.append("&").append(IHaysSearchConstants.JOB_INDUSTRY).append("=").append(industry);
			if (keywords != null)
			{
				keywords = QueryUtils.encodeForHaysSpecialKeywords(keywords);
				parameters.append("&").append(IHaysSearchConstants.JOB_KEYWORDS).append("=").append(keywords.replaceAll(IHaysSearchConstants.specialCharactersForKeywords, " "));
			}
			if (jobTypeP != null)
			{
				parameters.append("&").append(IHaysSearchConstants.JOB_PERM).append("=").append(jobTypeP).append("&").append(IHaysSearchConstants.JOB_MIN_PERM).append("=").append(minP);

				if (typeP == null)
				{
					typeP = "A"; //for uk and cerow
					parameters.append("&").append(IHaysSearchConstants.JOB_MAX_PERM).append("=").append(maxP).append("&").append(IHaysSearchConstants.JOB_SELECT_PERM).append("=A");// commented for release 8.0
				}
				else
				{
					parameters.append("&").append(IHaysSearchConstants.JOB_MAX_PERM).append("=").append(maxP).append("&").append(IHaysSearchConstants.JOB_SELECT_PERM).append("=" + typeP);// added for release 8.0
				}
			}
			if (jobTypeC != null)
			{
				parameters.append("&").append(IHaysSearchConstants.JOB_CONTRACT).append("=").append(jobTypeC).append("&").append(IHaysSearchConstants.JOB_MIN_CONTRACT).append("=").append(minC);
				parameters.append("&").append(IHaysSearchConstants.JOB_MAX_CONTRACT).append("=").append(maxC).append("&").append(IHaysSearchConstants.JOB_SELECT_CONTRACT).append("=").append(typeC);
			}
			if (jobTypeT != null)
			{
				parameters.append("&").append(IHaysSearchConstants.JOB_TEMP).append("=").append(jobTypeT).append("&").append(IHaysSearchConstants.JOB_MIN_TEMP).append("=").append(minT);
				parameters.append("&").append(IHaysSearchConstants.JOB_MAX_TEMP).append("=").append(maxT).append("&").append(IHaysSearchConstants.JOB_SELECT_TEMP).append("=").append(typeT);
			}
			if (contentType != null)
				parameters.append("&").append(IHaysSearchConstants.CONTENT_TYPE).append("=").append(contentType);

			if (locale != null)
				parameters.append("&").append(IHaysSearchConstants.LOCALE).append("=").append(locale);

			if (location != null)
				parameters.append("&").append(IHaysSearchConstants.JOB_LOCATION).append("=").append(location);
			if (neLongitude != null && neLatitude != null)
			{
				parameters.append("&").append(IHaysSearchConstants.NE_LONGITUDE).append("=").append(neLongitude);
				parameters.append("&").append(IHaysSearchConstants.NE_LATITUDE).append("=").append(neLatitude);
			}
			if (level != null)
				parameters.append("&").append(IHaysSearchConstants.LEVEL).append("=").append(level);
			if (radius != null)
				parameters.append("&").append(IHaysSearchConstants.RADIUS).append("=").append(radius);
			if (sortfield != null)
				parameters.append("&").append(IHaysSearchConstants.SORTFIELD).append("=").append(sortfield);
			if (sortorder != null)
				parameters.append("&").append(IHaysSearchConstants.SORTORDER).append("=").append(sortorder);
			if (jobInternational != null)
				parameters.append("&").append(IHaysSearchConstants.JOB_INTERN).append("=").append(jobInternational);
			if (jobNonNational != null)
				parameters.append("&").append(IHaysSearchConstants.JOB_NONNATIONL).append("=").append(jobNonNational);
			if (isOnlyJobTitle != null)
				parameters.append("&").append(IHaysSearchConstants.IS_ONLY_JOB_TITLE).append("=").append(isOnlyJobTitle);
			if (isFuzzy != null)
				parameters.append("&").append(IHaysSearchConstants.IS_FUZZY_SEARCH).append("=").append(isFuzzy);
			if (location_id != null)
				parameters.append("&").append(IHaysSearchConstants.LOCATION_ID).append("=").append(location_id);

			if (isHomePageSearch != null && isHomePageSearch.equalsIgnoreCase("1"))
			{
				parameters.append("&").append(IHaysSearchConstants.IS_HOME_SEARCH).append("=").append(isHomePageSearch);
			}
			if (job_category != null && !job_category.equalsIgnoreCase(""))
			{
				parameters.append("&").append(IHaysSearchConstants.JOB_CATEGORY).append("=").append(job_category);
			}
			// added for r7
			if (isAdvanceSearch != null && !isAdvanceSearch.equalsIgnoreCase(""))
			{
				parameters.append("&").append(IHaysSearchConstants.IS_ADVANCE_SEARCH).append("=").append(isAdvanceSearch);
			}
			//added for r7 alert form requirement
			if (jobPostCode != null && !jobPostCode.equalsIgnoreCase(""))
			{
				parameters.append("&").append(IHaysSearchConstants.JOB_POST_CODE).append("=").append(jobPostCode);
			}

			//added for r8 
			if (locationSet != null && !locationSet.equalsIgnoreCase(""))
			{
				parameters.append("&").append("location_set").append("=").append(locationSet);
			}

			SystemUtils.trace("webapi_content_search", "apiValidateSearchParams() binder After:  " + parameters);
			// check mandatory parameters
			if (!isTitleSet() && (industry == null || industry.trim().length() == 0) && (keywords == null || keywords.trim().length() == 0)
					&& (job_category == null || job_category.trim().length() == 0) && (location_id == null || location_id.trim().length() == 0)
					&& (m_binder.getLocal("jobid") == null || m_binder.getLocal("jobid").trim().length() == 0))
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

			if (linkedInJob != null && !linkedInJob.equalsIgnoreCase(""))
			{
				parameters.append("&").append("linkedIn_job").append("=").append(linkedInJob);
			}

			if (jobExperienceLevel != null && !jobExperienceLevel.equalsIgnoreCase(""))
			{
				parameters.append("&").append("experience_Level").append("=").append(jobExperienceLevel);
			}

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
			returnString = returnString.trim();
		}
		catch (Exception e)
		{

		}
		return returnString;
	}

}
