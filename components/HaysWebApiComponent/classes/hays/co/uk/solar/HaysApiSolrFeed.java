package hays.co.uk.solar;

import static hays.com.commonutils.HaysWebApiUtils.HandleExceptions;
import static hays.com.commonutils.HaysWebApiUtils.createResultSetFromData;
import static hays.com.commonutils.HaysWebApiUtils.parseXML;
import static hays.com.commonutils.HaysWebApiUtils.resolveIDOCFunctions;
import hays.com.commonutils.HaysWebApiUtils;
import hays.com.commonutils.NamespaceResolver;
import hays.custom.multilingual.HaysWebSite;
import intradoc.common.LocaleUtils;
import intradoc.common.ServiceException;

import intradoc.common.SystemUtils;
import intradoc.data.DataException;
import intradoc.data.DataResultSet;
import intradoc.data.ResultSet;
import intradoc.server.ServiceHandler;
import intradoc.shared.SharedObjects;

import java.io.IOException;
import java.text.ParseException;

import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import sitestudio.SSLinkFunctions;
import sitestudio.SSHierarchyServiceHandler;

public class HaysApiSolrFeed extends SSHierarchyServiceHandler
{
	public static final String RESULTSET_SEARCHRESULTS = "SearchResults";
	public static final String TRACE_NAME = "HaysApiSolrFeed";
	public static final String BLANK = "";

	public void searchSOLRcontent() throws DataException, ServiceException
	{
		//QssSiteAddressesBySite,QsolarSearch,systemdatabase
		//Get the list of all the contents
		String count = m_binder.getLocal("count");
		String searchQuery = this.m_currentAction.getParamAt(1);
		String dataSource = this.m_currentAction.getParamAt(2);
		m_binder.putLocal("dCount", count);
		
		DataResultSet solarContentsRS = HaysWebApiUtils.executeHaysProviderQuery(dataSource, searchQuery, m_binder);
		
		if (solarContentsRS != null && solarContentsRS.getNumRows() > 0)
		{
			//Reading actual file contents
			DataResultSet fileContents = (DataResultSet) readFileContents(solarContentsRS);

			//Merging both resultsets into one.
			solarContentsRS.mergeFields(fileContents);
			solarContentsRS.merge("dDocName", fileContents, false);

			DataResultSet lFinalResultset = new DataResultSet();
			lFinalResultset.copy(solarContentsRS);
			
			m_binder.clearResultSets();
			m_binder.addResultSet("SOLR_SEARCH_RESULTS", lFinalResultset);
		}
		else
		{
			m_binder.clearResultSets();
			m_binder.putLocal("StatusMessage", "No content found.");
		}
		
		//Removing extra resultsets from result xml
		HaysWebApiUtils.removeLocalData(m_binder, "RemoveLocalDataList");
		this.m_binder.putLocal("StatusMessage", LocaleUtils.encodeMessage("wwWebApiOKMsg", null));
		this.m_binder.putLocal("StatusCode", "UC000");
	}

	public void getValues(ArrayList<String> pList, Document lDocument) throws ServiceException, DataException, XPathExpressionException
	{
		SystemUtils.trace(TRACE_NAME, "getValues : ");
		XPath xpath = XPathFactory.newInstance().newXPath();
		SystemUtils.trace(TRACE_NAME, "xpath : " + xpath);
		xpath.setNamespaceContext(new NamespaceResolver());

		ResultSet lResultSet = m_binder.getResultSet("DOC_INFO");
		SystemUtils.trace(TRACE_NAME, "lResultSet : " + lResultSet);

		XPathExpression expr = xpath.compile("//wcm:root/wcm:list[@name='Content']/wcm:row/wcm:element[@name='Title']/text()");
		pList.add(resolveIDOCFunctions(expr.evaluate(lDocument, XPathConstants.STRING).toString(),m_binder,m_service));
		expr = xpath.compile("//wcm:root/wcm:list[@name='Content']/wcm:row/wcm:element[@name='Summary']/text()");
		pList.add(resolveIDOCFunctions(expr.evaluate(lDocument, XPathConstants.STRING).toString(),m_binder,m_service));
		expr = xpath.compile("//wcm:root/wcm:list[@name='Content']/wcm:row/wcm:element[@name='Body']/text()");
		pList.add(resolveIDOCFunctions(expr.evaluate(lDocument, XPathConstants.STRING).toString(),m_binder,m_service));
	}	

	

	public ResultSet readFileContents(DataResultSet pDataResultSet) throws ServiceException
	{
		ResultSet lReturnResultSet = null;
		ArrayList<String> lHeaderList = new ArrayList<String>();
		lHeaderList.add("dDocName");
		lHeaderList.add("Title");
		lHeaderList.add("Summary");
		lHeaderList.add("Body");
		ArrayList<String> lValueData = new ArrayList<String>();
		Document lDoc = null;
		String lDocURL = null;

		if (pDataResultSet != null && pDataResultSet.getNumRows() > 0)
		{
			try
			{
				do
				{
					String contentId = pDataResultSet.getStringValueByName("dDocName");
					lValueData.add(contentId);
					if("RELEASED".equalsIgnoreCase(pDataResultSet.getStringValueByName("dStatus")))
					{
						lDocURL = HaysWebApiUtils.getFilePath(contentId, m_binder, m_service, m_currentAction);
						SystemUtils.trace(TRACE_NAME, "DocUrl : " + lDocURL);
						lDoc = parseXML(lDocURL);
						SystemUtils.trace(TRACE_NAME, "lDoc : " + lDoc);
						getValues(lValueData, lDoc);
					}
					else
					{
						for(int i=0;i<lHeaderList.size()-1;i++)
						{
							lValueData.add(BLANK);
						}
					}
					
					SystemUtils.trace(TRACE_NAME, "lValueData : " + lValueData);
				}
				while (pDataResultSet.next());

				lReturnResultSet = createResultSetFromData(lHeaderList, lValueData);
				SystemUtils.trace(TRACE_NAME, "lResultSet " + lReturnResultSet);
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
			catch (Exception e)
			{
				e.printStackTrace();
				HandleExceptions(m_binder, "UC011", "wwFileNotFound");
			}
		}
		return lReturnResultSet;
	}	

	
	
	public void updateMetadataForSolarContent() throws DataException, ServiceException
	{
		m_binder.putLocal("statusCode", "-32");
		StringBuilder lBuilder = new StringBuilder();
		StringBuilder lUnProccessedContents = new StringBuilder();

		String listOfContentIds = m_binder.getLocal("contentIds");
		if (listOfContentIds != null && listOfContentIds != "")
		{
			SystemUtils.trace(TRACE_NAME, "List of contentids: "+listOfContentIds);
		
			String contentId[] = listOfContentIds.split(",");
			
			int count=contentId.length;
			
			SystemUtils.trace(TRACE_NAME, "length: "+count);
		
			/**Executing service to update xSolrFeedRequired**/
			
			for (int i = 0; i < count; i++)
			{
				SystemUtils.trace(TRACE_NAME, "content# " + i);
				
				m_binder.putLocal("dDocName", contentId[i]);
				SystemUtils.trace(TRACE_NAME, "dDocName: " + m_binder.getLocal("dDocName"));		
			
				m_binder.putLocal("dDocName", m_binder.getLocal("dDocName"));
				m_binder.putLocal("xSolrFeedRequired", "No");
		
				try
				{
					m_service.executeServiceEx("SOLR_UPDATE_METADATA_SERVICE", true);
					SystemUtils.trace(TRACE_NAME, "Service executed ");
					lBuilder.append(contentId[i] + ",");
				}
				catch (Exception e)
				{	
					SystemUtils.trace(TRACE_NAME, "catch block: " + m_binder.getLocal("dDocName"));
					SystemUtils.trace(TRACE_NAME, "catch block: " + e.getMessage());
					SystemUtils.trace(TRACE_NAME, "catch block: " + e.getCause());
					lUnProccessedContents.append(contentId[i] + ",");
					this.m_binder.putLocal("ssChangeHTTPHeader","true");
					this.m_binder.putLocal("HaysNewHTTPStatus","HTTP/1.1 200 OK");
					this.m_binder.putLocal("HaysOldHTTPStatus1","HTTP/1.1 503 Service Unavailable");
				}
				SystemUtils.trace(TRACE_NAME, "recovered from exception");
			}
		
		}
		String processedContents = lBuilder.toString();
		String unProcessedContents = lUnProccessedContents.toString();
		
		SystemUtils.trace(TRACE_NAME, "got string builders");
		
		if(processedContents != null && !processedContents.equals(""))
			m_binder.putLocal("processedContents", processedContents.substring(0, processedContents.length()-1));
		else
			m_binder.putLocal("processedContents", "");
		if(unProcessedContents != null && !unProcessedContents.equals(""))
			m_binder.putLocal("unProcessedContents", unProcessedContents.substring(0, unProcessedContents.length()-1));
		else
			m_binder.putLocal("unProcessedContents", "");
		
		SystemUtils.trace(TRACE_NAME, "done with the processing.");
		m_binder.putLocal("statusCode", "0");
		
		m_binder.removeResultSet("DOC_LIST");
		m_binder.removeResultSet("DOC_INFO");
		m_binder.removeResultSet("UserAttribInfo");
		SystemUtils.trace(TRACE_NAME, "removed resultsets.");
	}
	
	public void searchEvolveAPIcontent() throws DataException, ServiceException
	{
		String count = m_binder.getLocal("docname");
		String searchQuery = this.m_currentAction.getParamAt(1);
		String dataSource = this.m_currentAction.getParamAt(2);
		m_binder.putLocal("docname", count);
		
		DataResultSet evolveContentsRS = HaysWebApiUtils.executeHaysProviderQuery(dataSource, searchQuery, m_binder);
		
		if (evolveContentsRS != null && evolveContentsRS.getNumRows() > 0)
		{
			//Reading actual file contents
			DataResultSet fileContents = (DataResultSet) readFileContentsEvolve(evolveContentsRS);

			//Merging both resultsets into one.
			evolveContentsRS.mergeFields(fileContents);
			evolveContentsRS.merge("dDocName", fileContents, false);
			
			DataResultSet lFinalResultset = new DataResultSet();
			lFinalResultset.copy(evolveContentsRS);
			
			m_binder.clearResultSets();
			m_binder.addResultSet("EVOLVE_SEARCH_API_RESULTS", lFinalResultset);
		}
		else
		{
			m_binder.clearResultSets();
			m_binder.putLocal("StatusMessage", "No content found.");
		}
		
		//Removing extra resultsets from result xml
		HaysWebApiUtils.removeLocalData(m_binder, "RemoveLocalDataList");
		this.m_binder.putLocal("StatusMessage", LocaleUtils.encodeMessage("wwWebApiOKMsg", null));
		this.m_binder.putLocal("StatusCode", "UC000");
	}
	
	public void searchEvolvecontent() throws DataException, ServiceException, ParseException
	{
		String count = m_binder.getLocal("TotalRecords");
		String dDate = m_binder.getLocal("date");
		String resultsetname = this.m_currentAction.getParamAt(0);
		String searchQuery = this.m_currentAction.getParamAt(1);
		String dataSource = this.m_currentAction.getParamAt(2);
		SystemUtils.trace(TRACE_NAME, "Inside searchEvolvecontent dataSource Name: " + dataSource);
		SystemUtils.trace(TRACE_NAME, "Inside searchEvolvecontent Resultset Name: " + resultsetname);
		SystemUtils.trace(TRACE_NAME, "Inside searchEvolvecontent Query Name: " + searchQuery);
		SystemUtils.trace(TRACE_NAME, "Inside searchEvolvecontent count: " + count);
		SystemUtils.trace(TRACE_NAME, "Inside searchEvolvecontent dDate: " + dDate);
		
		if(count==null || count.equalsIgnoreCase("")){
		count=SharedObjects.getEnvironmentValue("defaultCountRSSFeed");	
		} else if(Integer.parseInt(count)>Integer.parseInt(SharedObjects.getEnvironmentValue("maxCountRSSFeed"))){
		count=SharedObjects.getEnvironmentValue("maxCountRSSFeed");
		}
		
		SystemUtils.trace(TRACE_NAME, "Inside searchEvolvecontent count: after calculation :" + count);
		
		m_binder.putLocal("dCount", count);
		m_binder.putLocal("dDate", dDate);
		
		DataResultSet evolveContentsRS = HaysWebApiUtils.executeHaysProviderQuery(dataSource, searchQuery, m_binder);
		
		if (evolveContentsRS != null && evolveContentsRS.getNumRows() > 0)
		{
			m_binder.clearResultSets();
			m_binder.addResultSet(resultsetname, evolveContentsRS);
		}
		else
		{
			m_binder.clearResultSets();
			m_binder.putLocal("StatusMessage", "No content found.");
		}
		
		//Removing extra resultsets from result xml
		HaysWebApiUtils.removeLocalData(m_binder, "RemoveLocalDataList");
		this.m_binder.putLocal("StatusMessage", LocaleUtils.encodeMessage("wwWebApiOKMsg", null));
		this.m_binder.putLocal("StatusCode", "UC000");
	}

	public void getValuesEvolve(ArrayList<String> pList, Document lDocument) throws ServiceException, DataException, XPathExpressionException
	{
		//Title, Summary and Secondary_Title - Start
		SystemUtils.trace(TRACE_NAME, "getValues : ");
		XPath xpath = XPathFactory.newInstance().newXPath();
		SystemUtils.trace(TRACE_NAME, "xpath : " + xpath);
		xpath.setNamespaceContext(new NamespaceResolver());

		ResultSet lResultSet = m_binder.getResultSet("DOC_INFO");
		SystemUtils.trace(TRACE_NAME, "lResultSet : " + lResultSet);
		
		XPathExpression expr = xpath.compile("//wcm:root/wcm:element[@name='Title']/text()");
		String title=resolveIDOCFunctions(expr.evaluate(lDocument, XPathConstants.STRING).toString(),m_binder,m_service);
		pList.add(title);
		expr = xpath.compile("//wcm:root/wcm:element[@name='Summary']/text()");
		pList.add(resolveIDOCFunctions(expr.evaluate(lDocument, XPathConstants.STRING).toString(),m_binder,m_service));
		expr = xpath.compile("//wcm:root/wcm:element[@name='Secondary_Title']/text()");
		pList.add(resolveIDOCFunctions(expr.evaluate(lDocument, XPathConstants.STRING).toString(),m_binder,m_service));
		//Title, Summary and Secondary_Title - End
		
		String evolveHost=SharedObjects.getEnvironmentValue("evolveHost");
		String section = lResultSet.getStringValueByName("xWebsiteSection");
		String xWebsites=lResultSet.getStringValueByName("xWebsites");
		String websiteAddress=null;
		
		//Country, Domain and Isocode - Start
		
		HaysWebSite website = null;
		String locale = lResultSet.getStringValueByName("xLocale");
		SystemUtils.trace(TRACE_NAME, "xLocale value is "+locale);
		HashMap<String, HaysWebSite> siteLocaleMap = (HashMap<String, HaysWebSite>)SharedObjects.getObject("Multiling", "siteLocaleMap");
		if(locale!=null && !locale.equalsIgnoreCase("")){
		    website = siteLocaleMap.get(locale);
		}
		  /*  SystemUtils.trace(TRACE_NAME, "website is "+website);   
		   if(website != null)
		   {
			   pList.add(website.countryName);
			   pList.add(m_binder.getLocal("websiteAddress"));
			   pList.add(website.isoCountryCode);
		   }
		}else{
			   pList.add("");
			   pList.add("");
			   pList.add("");
			   
		}*/
			
		//siteId code logic -start
		if(section!= null && !section.equalsIgnoreCase("") && !section.split(":")[0].equalsIgnoreCase("HAYS")){
			SystemUtils.trace(TRACE_NAME, "xWebsiteSection is present and is : " + section);
		   m_binder.putLocal("siteId", section.split(":")[0]);
		} else if(xWebsites!=null && !xWebsites.equalsIgnoreCase("") && !xWebsites.contains(",")){
			m_binder.putLocal("siteId", xWebsites); 	
		} else if(website != null){
			m_binder.putLocal("siteId", website.websiteId); 		
		} else if(xWebsites!=null && !xWebsites.equalsIgnoreCase("") && xWebsites.contains(",")){
			m_binder.putLocal("siteId", xWebsites.split(",")[0]); 		
		}
		SystemUtils.trace(TRACE_NAME, "siteid is : " +m_binder.getLocal("siteId"));
		//siteId code logic -end
		
		//website address logic
		if(m_binder.getLocal("siteId")!=null && !(m_binder.getLocal("siteId")).equalsIgnoreCase("")){
		websiteAddress=getDefaultHttpSiteAddress(m_binder.getLocal("siteId")).substring(0,getDefaultHttpSiteAddress(m_binder.getLocal("siteId")).length()-1).replaceFirst("http://", "");
		SystemUtils.trace(TRACE_NAME, "website address through http address is "+websiteAddress); 
		}else{
		websiteAddress=m_binder.getLocal("websiteAddress");
		SystemUtils.trace(TRACE_NAME, "website address through binder is "+websiteAddress); 
		}
		
		if(websiteAddress!=null && !websiteAddress.equalsIgnoreCase("")){
			if(websiteAddress.indexOf("/") > 0)
			{
				websiteAddress=websiteAddress.substring(0, websiteAddress.indexOf("/"));
			}
		}
		//website logic ends
		
		SystemUtils.trace(TRACE_NAME, "website is "+website);   
		   if(website != null && locale!=null && !locale.equalsIgnoreCase(""))
		   {
			   pList.add(website.countryName);
			   pList.add(websiteAddress);   
			   pList.add(website.isoCountryCode);
		   }
		else{
			   pList.add("");
			   pList.add(websiteAddress);
			   pList.add("");
			   
		}
		
		if((locale==null || locale.equalsIgnoreCase("")) && m_binder.getLocal("siteId")!=null && !(m_binder.getLocal("siteId")).equalsIgnoreCase("")){
			m_binder.putLocal("property", "siteLocale");
			m_service.executeServiceEx("SS_GET_SITE_PROPERTY", true);
			locale = m_binder.getLocal("value");
			SystemUtils.trace(TRACE_NAME, "locale is "+locale); 
			
			if(locale!=null && !locale.equalsIgnoreCase("")){
			    website = siteLocaleMap.get(locale);
			    SystemUtils.trace(TRACE_NAME, "website is "+website);   
			   if(website != null)
			   {
				   pList.add(website.countryName);
				   pList.add(websiteAddress);   
				   pList.add(website.isoCountryCode);
			   }
			}else{
				   pList.add("");
				   pList.add(websiteAddress);
				   pList.add("");
				   
			}
		}
		
		  
		//Country, Domain and Isocode - End
		
		//Content & Url- Start
		//GetTemplate if possible
		String templateName =null;
		String url = null;
		String dDocName = lResultSet.getStringValueByName("dDocName");
		SystemUtils.trace(TRACE_NAME, "Starting getting Content code");
			   
	   if(section!= null && !section.equalsIgnoreCase("") && m_binder.getLocal("siteId")!=null && !(m_binder.getLocal("siteId")).equalsIgnoreCase("")){
	   SystemUtils.trace(TRACE_NAME, "xWebsiteSection is present and is : " + section);
	   m_binder.putLocal("nodeId", section.split(":")[1]);
	   m_service.executeServiceEx("SS_GET_ALL_NODE_PROPERTIES", true);
	   ResultSet nodeSectionProperties = m_binder.getResultSet("SiteStudioProperties");
	   if(nodeSectionProperties!=null && nodeSectionProperties.isRowPresent()){
	   String primaryUrl = nodeSectionProperties.getStringValueByName("primaryUrl");
	   String[] parts=null;
	   String[] sectionArray=null;
	   SystemUtils.trace(TRACE_NAME, "Check if primaryUrl contains docname "+dDocName+" and is : " + primaryUrl);
	   
			   if(primaryUrl!=null && !primaryUrl.equalsIgnoreCase("") && primaryUrl.contains(dDocName)){
			   SystemUtils.trace(TRACE_NAME, "primaryUrl contains docname ");
			   String primarySection = nodeSectionProperties.getStringValueByName("primaryTemplateUrl");
			   SystemUtils.trace(TRACE_NAME, "values of primaryTemplateUrl: " + primarySection);
			   
					   if(primarySection!=null && !primarySection.equalsIgnoreCase("")){
						   sectionArray= primarySection.split("&");
						   for (int i = 0; i < sectionArray.length; i++)
						   {
						       if (sectionArray[i].contains("Main Region="))
						       {
						           parts = sectionArray[i].split("=");
						           templateName = parts[1];
						           url = getDefaultHttpSiteAddress(m_binder.getLocal("siteId")).substring(0,(getDefaultHttpSiteAddress(m_binder.getLocal("siteId"))).length() - 1)+SSLinkFunctions.computeNodeLinkUrl(m_service, m_binder.getLocal("nodeId"), m_binder.getLocal("siteId"));
						           SystemUtils.trace(TRACE_NAME, "url of primaryTemplate doc is: " + url);
						       }
						   }   
					   } 
			   SystemUtils.trace(TRACE_NAME, "values of Main Regin template name is : " + templateName);
			   
			   }else{
			   SystemUtils.trace(TRACE_NAME, "primaryUrl does not contains docname  hence moveto secondary section");
			   String regionDef=lResultSet.getStringValueByName("xRegionDefinition");
			   SystemUtils.trace(TRACE_NAME, "values of xRegionDefinition is : " + regionDef);
			   String secRegionDef=SharedObjects.getEnvironmentValue("secRegionDef");
			   
				   if(regionDef!=null && !regionDef.equalsIgnoreCase("") && secRegionDef.contains(regionDef))
				   {
					   String seondarySection = nodeSectionProperties.getStringValueByName("secondaryTemplateUrl"); 
					   SystemUtils.trace(TRACE_NAME, "values of secondaryTemplateUrl: " + seondarySection);
							 if(seondarySection!=null && !seondarySection.equalsIgnoreCase("")){
						    	   sectionArray = seondarySection.split("&");
						    	   
						    	   for (int y = 0; y < sectionArray.length; y++)
								   {
								       if (sectionArray[y].contains("Main Region="))
								       {
								           parts = sectionArray[y].split("=");
								           templateName = parts[1];
								           url = getDefaultHttpSiteAddress(m_binder.getLocal("siteId")).substring(0,(getDefaultHttpSiteAddress(m_binder.getLocal("siteId"))).length() - 1)+ SSLinkFunctions.computeLinkUrl(m_service, dDocName, m_binder.getLocal("nodeId"), m_binder.getLocal("siteId"));
								           SystemUtils.trace(TRACE_NAME, "url of secondaryTemplate doc is: " + url);
								       }
								   }
						       }
				   }
			   
			   }
	   }
	   /*m_binder.putLocal("ssDocName", dDocName);
	   m_service.executeServiceEx("SS_GET_FRIENDLY_URL", true);
	   SystemUtils.trace(TRACE_NAME, "ssFriendlyUrl are : "+m_binder.getLocal("ssFriendlyUrl"));
	   SystemUtils.trace(TRACE_NAME, "HttpSiteAddress are : "+m_binder.getLocal("HttpSiteAddress"));
	   url=m_binder.getLocal("HttpSiteAddress")+m_binder.getLocal("ssFriendlyUrl");*/
	  }	   
	   
	   SystemUtils.trace(TRACE_NAME, "templateName is : " +templateName);
	   
	   if(templateName!=null && !templateName.equalsIgnoreCase("")){
	    m_binder.putLocal("templateDocName", templateName);   
	   }
	   
		if(website != null){
		m_binder.putLocal("domainId", website.domainId);
		}
		
		SystemUtils.trace(TRACE_NAME, "values are : dataFileDocName" + dDocName+" regionDefinitionDocName :"+lResultSet.getStringValueByName("xRegionDefinition")+" siteId : "+m_binder.getLocal("siteId")+" domainId : "+m_binder.getLocal("domainId")+" xLocale : "+lResultSet.getStringValueByName("xLocale"));
		m_binder.putLocal("dataFileDocName", dDocName);
		m_binder.putLocal("regionDefinitionDocName", lResultSet.getStringValueByName("xRegionDefinition"));
		m_binder.putLocal("SiteLocale", locale);
		m_service.executeServiceEx("WCM_PLACEHOLDER", true);
		SystemUtils.trace(TRACE_NAME, "values are : "+m_binder.getLocal("placeholderContent"));
		if(m_binder.getLocal("placeholderContent")!=null && !m_binder.getLocal("placeholderContent").equalsIgnoreCase("")){
		pList.add(m_binder.getLocal("placeholderContent").replaceFirst(title, "").replaceAll("src=\"/cs","src=\""+evolveHost+"/cs"));
		}else{
		pList.add("");	
		}
		/*if(url!=null && !url.equalsIgnoreCase("")){
		pList.add(url);
		}else{
		pList.add("");
		}*/
		//Content & Url- End
	}
	
	public ResultSet readFileContentsEvolve(DataResultSet pDataResultSet) throws ServiceException
	{
		ResultSet lReturnResultSet = null;
		ArrayList<String> lHeaderList = new ArrayList<String>();
		lHeaderList.add("dDocName");
		lHeaderList.add("Title");
		lHeaderList.add("Summary");
		lHeaderList.add("Secondary_Title");
		lHeaderList.add("Country");
		lHeaderList.add("Domain");
		lHeaderList.add("ISOCode");
		lHeaderList.add("Content");
		//lHeaderList.add("Url");
		ArrayList<String> lValueData = new ArrayList<String>();
		Document lDoc = null;
		String lDocURL = null;

		if (pDataResultSet != null && pDataResultSet.getNumRows() > 0)
		{
			try
			{
				do
				{
					String contentId = pDataResultSet.getStringValueByName("dID");
					lValueData.add(pDataResultSet.getStringValueByName("dDocName"));
					/*if("RELEASED".equalsIgnoreCase(pDataResultSet.getStringValueByName("dStatus")))
					{*/
						lDocURL = HaysWebApiUtils.getFilePath(contentId, m_binder, m_service, m_currentAction);
						SystemUtils.trace(TRACE_NAME, "DocUrl : " + lDocURL);
						lDoc = parseXML(lDocURL);
						SystemUtils.trace(TRACE_NAME, "lDoc : " + lDoc);
						getValuesEvolve(lValueData, lDoc);
					/*}
					else
					{
						for(int i=0;i<lHeaderList.size()-1;i++)
						{
							lValueData.add(BLANK);
						}
					}*/
					
					SystemUtils.trace(TRACE_NAME, "lValueData : " + lValueData);
				}
				while (pDataResultSet.next());

				lReturnResultSet = createResultSetFromData(lHeaderList, lValueData);
				SystemUtils.trace(TRACE_NAME, "lResultSet " + lReturnResultSet);
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
			catch (Exception e)
			{
				e.printStackTrace();
				HandleExceptions(m_binder, "UC011", "wwFileNotFound");
			}
		}
		return lReturnResultSet;
	}

}
