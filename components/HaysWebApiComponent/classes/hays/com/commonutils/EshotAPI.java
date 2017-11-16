package hays.com.commonutils;

import static hays.com.commonutils.HaysWebApiUtils.HandleExceptions;
import static hays.com.commonutils.HaysWebApiUtils.createResultSetFromData;
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
import intradoc.server.ServiceHandler;
import intradoc.shared.SharedObjects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


public class EshotAPI extends ServiceHandler{
	public static final String ESHOT_TRACE = "eshotdata";
	String domainToAppend = "";
	StringBuffer sb = new StringBuffer();
	DataResultSet resultDoctype = null;
	String docNameList = "";
	HashMap<String, String> docTypeMap = new HashMap<String, String>();
	public void getEshotcontent() throws ServiceException, DataException
	{
		SystemUtils.trace(ESHOT_TRACE, "Inside eshotDataHandler : geteshotDetails:");
		SystemUtils.trace(ESHOT_TRACE, "Binder detail prior to processing : " + this.m_binder.getLocalData());
		this.m_binder.removeLocal("dDocType");
		try{
			String xcountry = this.m_binder.getLocal("xcountry");
			if (xcountry.trim().length() > 0){
				domainToAppend = xcountry.substring(xcountry.indexOf("-")+1);
			}
			else{
				xcountry = "%";
			}
		SystemUtils.trace(ESHOT_TRACE, "domainToAppend : " + domainToAppend);
		String xsubtype = this.m_binder.getLocal("xsubtype");
		xsubtype = (xsubtype.trim().length() > 0) ? xsubtype : "%";
		String cgipath=this.m_binder.getLocal("cgipath");
		String providerName = this.m_currentAction.getParamAt(0);
		String resultSetName = this.m_currentAction.getParamAt(1);
		String queryName = this.m_currentAction.getParamAt(2);
		String queryName1 = this.m_currentAction.getParamAt(3);
		String queryToGetDocType = this.m_currentAction.getParamAt(4);
		SystemUtils.trace(ESHOT_TRACE, "Inside eshotDataHandler country: " + xcountry + " sub_type: " + xsubtype + " cgipath: " + cgipath + " Provider Name: " + providerName + " ResultSet: " + resultSetName +" Query1: " + queryName + " Query2: " + queryName1 + " queryToGetDocType: "+ queryToGetDocType);
		
		if (xsubtype==null || xsubtype=="" || xsubtype=="%" )
		{
			queryName=queryName1;
		}
		this.m_binder.putLocal("xcountry", xcountry);
		this.m_binder.putLocal("xsubtype", xsubtype);
		Workspace ws = getProviderConnection(providerName);

		DataResultSet result = null;
		DataResultSet resultTempTIterate = null;
		

		if (queryName != null && queryName.trim().length() > 0)
		{
			ResultSet temp = ws.createResultSet(queryName, m_binder);
			result = new DataResultSet();
			resultTempTIterate = new DataResultSet();
			result.copy(temp);
			resultTempTIterate.copy(temp);
		}
		//Create a list of contentIds to be passed in query for fetching dDocType
		if (resultTempTIterate != null && resultTempTIterate.getNumRows() > 0)
		{
		do
		{
			String headerLogoId = resultTempTIterate.getStringValueByName("xEshotHeaderLogo");
			String footerLogoId = resultTempTIterate.getStringValueByName("xEshotFooterLogo");
			if(null!=headerLogoId && !headerLogoId.equalsIgnoreCase(""))
				sb=sb.append(",").append(headerLogoId.trim().toUpperCase());
			if(null!=footerLogoId && !footerLogoId.equalsIgnoreCase(""))
				sb=sb.append(",").append(footerLogoId.trim().toUpperCase());	
			
		}
		while (resultTempTIterate.next());
		
		docNameList=sb.substring(1,sb.length());	
		if(docNameList.indexOf(",") >-1)
			docNameList = docNameList.replaceAll(",", "','");
		// Set content list in the binder.
		this.m_binder.putLocal("docNameList", docNameList); 
		if (queryToGetDocType != null && queryToGetDocType.trim().length() > 0)
		{
			ResultSet temp1 = ws.createResultSet(queryToGetDocType, m_binder);
			resultDoctype = new DataResultSet();
			resultDoctype.copy(temp1);
		}
		
		do {
			SystemUtils.trace(ESHOT_TRACE, "resultDoctype current : " + resultDoctype.getCurrentRowAsList().toString());
			if(resultDoctype!=null && resultDoctype.getNumRows()>0){
				String column1 = resultDoctype.getStringValueByName("dDocName");
			    String column2 = resultDoctype.getStringValueByName("dDocType");
			    docTypeMap.put(column1, column2);
			}
		}
		while (resultDoctype.next());
		SystemUtils.trace(ESHOT_TRACE, "resultDoctype: " + resultDoctype);
		}
		SystemUtils.trace(ESHOT_TRACE, "ResultSet: " + result);
		if(result !=null && result.getNumRows()>0 && docTypeMap.size()>0){
		  result=(DataResultSet) readFileContents(result, docTypeMap);
		}
		HaysWebApiUtils.removeResultSets(m_binder, "RemoveResultSetsForSolr");
		this.m_binder.addResultSet(resultSetName, result);
		this.m_binder.putLocal("statusMessage", "Success");
		this.m_binder.putLocal("statusCode", "UC000");
		// release the JDBC connection assigned to this thread (request)
		// which kills the result set 'temp'
		ws.releaseConnection();
		}
		catch (Exception e)
		{
			SystemUtils.trace(ESHOT_TRACE, "Exception in outer: " + e.getMessage());
			//HandleEshotDataExceptions(m_binder, "UC011", "wwFileNotFound");
		}
	}

	private Workspace getProviderConnection(String providerName) throws ServiceException, DataException
	{

		// validate the provider name
		if (providerName == null || providerName.length() == 0)
		{
			throw new ServiceException("You must specify a provider name.");
		}
		// validate that the provider is a valid database provider
		Provider p = Providers.getProvider(providerName);
		if (p == null)
		{
			throw new ServiceException("The provider '" + providerName + "' does not exist.");
		}
		else if (!p.isProviderOfType("database"))
		{
			throw new ServiceException("The provider '" + providerName + "' is not a valid provider of type 'database'.");
		}

		Workspace ws = (Workspace) p.getProvider();

		return ws;
	}


	
	
private ResultSet readFileContents(DataResultSet pDataResultSet, HashMap<String, String> pResultDoctype) throws ServiceException
{
	 String BLANK = "";
	ResultSet lReturnResultSet = null;
	ArrayList<String> lHeaderList = new ArrayList<String>();
	lHeaderList.add("dID");
	lHeaderList.add("dDocName");
	lHeaderList.add("dDocType");
	lHeaderList.add("dDocTitle");
	lHeaderList.add("xCountry");
	lHeaderList.add("xDescription");
	lHeaderList.add("xSubType");
	lHeaderList.add("DocUrl");
	lHeaderList.add("HeaderLogoUrl");
	lHeaderList.add("FooterLogoUrl");
	SystemUtils.trace(ESHOT_TRACE,"Headerlist"+lHeaderList);
	SystemUtils.trace(ESHOT_TRACE,"pDataResultSet : "+pDataResultSet);
	SystemUtils.trace(ESHOT_TRACE,"pResultDoctype : "+pResultDoctype);
	ArrayList<String> lValueData = new ArrayList<String>();
	//Document lDoc = null;
	String lDocURL = null;
	String lHeaderLogoURL = "";
	String lFooterLogoURL = "";

	if (pDataResultSet != null && pDataResultSet.getNumRows() > 0)
	{
		try
		{
			do
			{
				lHeaderLogoURL = "";
				lFooterLogoURL = "";
				SystemUtils.trace(ESHOT_TRACE,"pDataResultSet Inside loop");
				SystemUtils.trace(ESHOT_TRACE,"pDataResultSet"+pDataResultSet.getCurrentRow());
				String contentId = pDataResultSet.getStringValueByName("dDocName");
				String dID = pDataResultSet.getStringValueByName("dID");
				String DocType = pDataResultSet.getStringValueByName("dDocType");
				String DocTitle = pDataResultSet.getStringValueByName("dDocTitle");
				String Country = pDataResultSet.getStringValueByName("xCountry");
				String Description = pDataResultSet.getStringValueByName("xDescription");
				String SubType = pDataResultSet.getStringValueByName("xSubType");
				String headerLogo = pDataResultSet.getStringValueByName("xEshotHeaderLogo");
				String footerLogo = pDataResultSet.getStringValueByName("xEshotFooterLogo");
				
				if(null!=headerLogo && !headerLogo.equalsIgnoreCase(""))
				{
					headerLogo  = headerLogo.trim().toUpperCase();
					SystemUtils.trace(ESHOT_TRACE,"HeaderLogo : " + headerLogo+ "HeaderLogo type : " + pResultDoctype.get(headerLogo));
					lHeaderLogoURL = getLogoFilePath(headerLogo, pResultDoctype.get(headerLogo));
				if(lHeaderLogoURL == null)
					continue;
				}
				SystemUtils.trace(ESHOT_TRACE, "HeaderLogoURL : " + lHeaderLogoURL);
				if(null!=footerLogo && !footerLogo.equalsIgnoreCase(""))
				{
					footerLogo = footerLogo.trim().toUpperCase();
					SystemUtils.trace(ESHOT_TRACE,"footerLogo : " + footerLogo+ "footerLogo type : " + pResultDoctype.get(footerLogo));
					lFooterLogoURL = getLogoFilePath(footerLogo, pResultDoctype.get(footerLogo));
					if(lFooterLogoURL == null)
						continue;
				}
				SystemUtils.trace(ESHOT_TRACE, "FooterLogoURL : " + lFooterLogoURL);
				lValueData.add(dID);
				lValueData.add(contentId);
				lValueData.add(DocType);
				lValueData.add(DocTitle);
				lValueData.add(Country);
				lValueData.add(Description);
				lValueData.add(SubType);
				m_binder.putLocal("dID", dID);
				m_binder.putLocal("allowInterrupt", "1");
				m_binder.putLocal("RevisionSelectionMethod", "Specific");
				SystemUtils.trace(ESHOT_TRACE, "dID "
						+ m_binder.getLocal("dID"));
				m_binder.removeLocal("IsSoap");
				m_service.executeServiceEx("DOC_INFO_BY_NAME", true);
				lDocURL = getFilePath(m_binder);
					SystemUtils.trace(ESHOT_TRACE, "DocUrl : " + lDocURL);
					lValueData.add(lDocURL);
				lValueData.add(lHeaderLogoURL);
				lValueData.add(lFooterLogoURL);
			SystemUtils.trace(ESHOT_TRACE, "lValueData : " + lValueData);
			this.m_binder.removeResultSet("DOC_INFO");
			HaysWebApiUtils.removeLocalData(m_binder, "RemoveLocalDataList");
			}
			while (pDataResultSet.next());
			lReturnResultSet = createResultSetFromData(lHeaderList, lValueData);
			SystemUtils.trace(ESHOT_TRACE, "lResultSet " + lReturnResultSet);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			SystemUtils.trace(ESHOT_TRACE, "error message " + e.getMessage().toString());
			HandleExceptions(m_binder, "UC011", "wwFileNotFound");
		}
	}
	return lReturnResultSet;
}	

private String getFilePath(DataBinder pbinder){
	String path=null;
	SystemUtils.trace(ESHOT_TRACE," inside getFilePath");
	String cgipath=SharedObjects.getEnvironmentValue("eshothost" + domainToAppend);
	String cgiurl=SharedObjects.getEnvironmentValue("cgiurl");
	String [] cgiurllist=cgiurl.split(",");
	Properties lProperties = pbinder.getLocalData();
	SystemUtils.trace(ESHOT_TRACE, "lProperties : " + lProperties);
		path =lProperties.getProperty("DocUrl");
	for (String s:cgiurllist)
	{
		try{
	
	if(path.contains(s)) {
	path=path.replaceAll(s,cgipath);
	}
		SystemUtils.trace(ESHOT_TRACE,"cgipah differs from url : "+s +" path: "+path);
	}	
	catch (Exception e)
	{
		e.printStackTrace();
	}
	}
	return path;
}

private String getLogoFilePath(String contentID, String logoType){
	SystemUtils.trace(ESHOT_TRACE, "Logotype : " + logoType + " Content id :  " + contentID);
	m_binder.putLocal("dDocName", contentID);
	if(logoType!=null)
	m_binder.putLocal("dDocType", logoType);
	m_binder.putLocal("allowInterrupt", "1");
	m_binder.putLocal("RevisionSelectionMethod", "LatestReleased");
	m_binder.putLocal("IsJava", "1");
	String path=null;
	m_binder.removeLocal("IsSoap");
	try
	{
		m_service.executeSafeServiceInNewContext("DOC_INFO_BY_NAME", true);
		m_binder.removeLocal("dDocType");
	}
	catch (Exception e)
	{
		e.printStackTrace();
		SystemUtils.trace(ESHOT_TRACE, "Exception path : " + path);
		//HandleExceptions(m_binder, "UC011", "wwFileNotFound");
		return path;
	}
	m_binder.removeLocal("dDocType");
	String cgipath=SharedObjects.getEnvironmentValue("eshothost" + domainToAppend);
	String cgiurl=SharedObjects.getEnvironmentValue("cgiurl");
	
	String [] cgiurllist=cgiurl.split(",");

	Properties lProperties = m_binder.getLocalData();
	String StatusMessage =lProperties.getProperty("StatusMessage");
	SystemUtils.trace(ESHOT_TRACE, "StatusMessage : " + StatusMessage);
	if (StatusMessage != null && StatusMessage.trim().length() > 0 && (StatusMessage.contains("Unable to retrieve information") || StatusMessage.contains("csGetFileUnableToFindRevision")))
	{
		m_binder.getLocalData().remove("StatusMessage");
		SystemUtils.trace(ESHOT_TRACE, "File not found");
		return path;
	}
	SystemUtils.trace(ESHOT_TRACE, "lProperties : " + lProperties);
		path =lProperties.getProperty("DocUrl");
		String docTypeLower = logoType.toLowerCase();
		if(path != null && !path.contains("/"+docTypeLower+"/")){
			SystemUtils.trace(ESHOT_TRACE,"Binder created wrong url : "+path);
			String stringFromlastSlash = path.substring(path.lastIndexOf("/"));
			SystemUtils.trace(ESHOT_TRACE,"stringFromlastSlash : " + stringFromlastSlash);
			String stringBeforelastSlashTemp = path.replace(stringFromlastSlash,"");
			String stringBeforelastSlash = stringBeforelastSlashTemp.substring(0,stringBeforelastSlashTemp.lastIndexOf("/")+1);
			SystemUtils.trace(ESHOT_TRACE,"stringBeforelastSlash : " + stringBeforelastSlash);
			path = stringBeforelastSlash +docTypeLower + stringFromlastSlash;
			SystemUtils.trace(ESHOT_TRACE,"Updated Path : " + path);
		}
	for (String s:cgiurllist)
	{
		try{
	
	if(path.contains(s)) {
	path=path.replaceAll(s,cgipath);
	}
		SystemUtils.trace(ESHOT_TRACE,"cgipah differs from url"+s + " file path"+path);
	}	
	catch (Exception e)
	{
		e.printStackTrace();
	}
	}
	this.m_binder.removeResultSet("DOC_INFO");
	HaysWebApiUtils.removeLocalData(m_binder, "RemoveLocalDataList");
	return path;

}

private static void HandleEshotDataExceptions(DataBinder pBinder, String pErrorCode, String pErrorStringKey) throws ServiceException
{
	pBinder.putLocal("statusMessage", LocaleUtils.encodeMessage(pErrorStringKey, null));
	pBinder.putLocal("statusCode", pErrorCode);
	pBinder.removeResultSet("error");
	pBinder.m_resultSets.remove("error");
	pBinder.putLocal("ssChangeHTTPHeader","true");
	pBinder.putLocal("HaysNewHTTPStatus","HTTP/1.1 200 OK");
	pBinder.putLocal("HaysOldHTTPStatus1","HTTP/1.1 503 Service Unavailable");
}

}


