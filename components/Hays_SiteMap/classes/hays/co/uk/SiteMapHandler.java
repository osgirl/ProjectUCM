package hays.co.uk;

import static intradoc.shared.SharedObjects.getEnvironmentValue;
import hays.custom.multilingual.HaysWebSite;
import intradoc.common.FileUtils;
import intradoc.common.LocaleUtils;
import intradoc.common.ServiceException;
import intradoc.common.StringUtils;
import intradoc.common.SystemUtils;
import intradoc.data.DataBinder;
import intradoc.data.DataException;
import intradoc.data.DataResultSet;
import intradoc.data.ResultSet;
import intradoc.indexer.IndexerConfig;
import intradoc.indexer.OracleTextUtils;
import intradoc.server.ActiveState;
import intradoc.server.DirectoryLocator;
import intradoc.server.SearchIndexerUtils;
import intradoc.shared.SharedObjects;

import java.io.File;
import java.io.IOException;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;

import sitestudio.SSHierarchyServiceHandler;

public class SiteMapHandler extends SSHierarchyServiceHandler
{	
	public static final String SITEMAP = "SiteMap";

	public static final String SITE_MAP_RESULT_COUNT = SharedObjects.getEnvironmentValue("SiteMapResultCount");
	public static final String COMPLEX_META_TBL_EXTENSION = "COMPLEXMETA_";
	public static final String SITE_MAP_CUSTOM_PROP = SharedObjects.getEnvironmentValue("SiteMapProjectProperty");
	public static final String SITE_MAP_CON_TYPE_PROP = SharedObjects.getEnvironmentValue("SiteMapConTypeProperty");
	public static final String SITE_MAP_CON_SUB_TYPE_PROP = SharedObjects.getEnvironmentValue("SiteMapConSubTypeProperty");
	public static final String SITE_MAP_INCLUDED_TYPES = SharedObjects.getEnvironmentValue("SiteMapIncludeContentTypes");
	public static final String SITE_MAP_DEFAULT_SECTIONID = SharedObjects.getEnvironmentValue("SiteMapDefaultSection");

	public static final String CONFIGURED_TYPES = SharedObjects.getEnvironmentValue("SiteMapGenericContentTypes");
	public static final String CONFIGURED_SUB_TYPES = SharedObjects.getEnvironmentValue("SiteMapGenericContentSubTypes");
	public static String GOOGLE_SUBMIT_URL = SharedObjects.getEnvironmentValue("SiteMapGoogleSubmissionURL");
	public static final String SPRTR = "@";
	public String SITE_LOCALE = null;
	// The maximum length of SQL queries allowed is 4000 characters. Without the metadata terms list,
	// the query has a minimum of 600 characters. This leaves only 3400 characters for the metadata 
	// terms list.
	public static final int MAX_METATERMS_LENGTH = 500;
	public static final String SITE_LOCALE_PROPERTY_NAME = SharedObjects.getEnvironmentValue("SiteLocalePropertyName");

	static final String SS_NAV_RESULT_SET_FIELDS[] = { "nodeId", "parentNodeId", "label", "level", "href" };

	class CachedSiteMap
	{
		String sitemap = null;
		long time = -1;

		public CachedSiteMap(String sitemap, long lastTimeUpdate)
		{
			this.sitemap = sitemap;
			this.time = lastTimeUpdate;
		}
	}

	public void getGoogleSiteMap() throws ServiceException, DataException
	{
		String siteMapStr = null;
		CachedSiteMap siteMapCachedObj = null;

		String siteId = m_binder.getLocal("siteId");
		if (siteId == null)
			throw new DataException("Site Id is not defined");

		HaysWebSite website = null;
		if (siteId != null && siteId.length() > 0)
		{
			HashMap<String, HaysWebSite> websitesMap = (HashMap<String, HaysWebSite>) SharedObjects.getObject("Multiling", "WebsitesMap");
			website = websitesMap.get(siteId);
		}
		SITE_LOCALE = website.haysLocaleId;
		debugKeyPoints("Site locale is : " + SITE_LOCALE);

		// force to rebuild the site map
		boolean toRebuild = false;
		String toRebuildStr = m_binder.getLocal("toRebuild");
		if (toRebuildStr != null && (toRebuildStr.equalsIgnoreCase("true") || toRebuildStr.equalsIgnoreCase("1")))
		{
			toRebuild = true;
		}

		// retrieve from cache
		String cacheName = new StringBuffer(siteId).append("GOOGLE").toString();
		Object siteMap = SharedObjects.getObject(SITEMAP, cacheName);

		// check the lock file for clustered environment
		if (!toRebuild && siteMap != null)
		{
			long timeCachedLast = ((CachedSiteMap) siteMap).time;
			String path = getDataDir(siteId);
			File file = new File(path, "lockwait.dat");
			if (file.exists())
			{
				long fileTimstamp = file.lastModified();
				if (fileTimstamp > timeCachedLast)
				{
					toRebuild = true;
					debugKeyPoints("compare timstamp stored in cache and the locks: to rebuild sitemap? " + toRebuild);
				}
			}
		}

		// generate the map and store it in cache
		if (toRebuild || siteMap == null)
		{
			debugKeyPoints("Rebuild Site Map for " + siteId);
			siteMapStr = buildGoogleSiteMap(siteId);
			if (siteMapStr != null)
			{
				Date aDate = new Date();
				siteMapCachedObj = new CachedSiteMap(siteMapStr.toString(), aDate.getTime());
				SharedObjects.putObject(SITEMAP, cacheName, siteMapCachedObj);
			}
			submitSiteMapToGoogle(siteId);
		}
		else if (siteMap != null)
		{
			siteMapStr = ((CachedSiteMap) siteMap).sitemap;
			debugKeyPoints("use the cached SiteMap for " + siteId);
		}

		if (siteMapStr != null)
		{
			m_binder.putLocal("siteMapXml", siteMapStr);
		}
	}

	/**
	 * Builds SteMap of the given site that in Google SiteMap format
	 * @param siteId
	 * @return Formatted sitemap
	 */
	private String buildGoogleSiteMap(String siteId) throws DataException, ServiceException
	{
		StringBuffer writer = new StringBuffer();
		List<Hashtable<String, String>> additionalContentMapList = null;
		debugKeyPoints("\n buildFullSiteMap() starting for " + siteId);

		// preload: load project info and validate custom property set as env. variable
		SiteInfo siteinfo = loadProjectInfo(siteId);
		if (!isValidProjectProperty(SITE_MAP_CUSTOM_PROP, siteinfo))
			debug("ERROR: Invalid property is defined for site " + siteId);

		ResultSet navigationRS = buildSiteNavResultSet(siteinfo);

		// extract values of that project custom property (env variable) and concatinate in one list
		// to pass to Select query as option values to retrieve content classified with these values
		String listOfCustomPropValues = extractCustomPropValues(SITE_MAP_CUSTOM_PROP, siteinfo.m_projectDom);
		if (listOfCustomPropValues != null)
		{
			String[] listOfCustomPropValuesArray = listOfCustomPropValues.split(";");
			additionalContentMapList = new ArrayList<Hashtable<String, String>>();
			if (SITE_MAP_INCLUDED_TYPES != null && SITE_MAP_INCLUDED_TYPES.length() > 0)
			{
				String[] contentTypeList = SITE_MAP_INCLUDED_TYPES.split(",");
				for (int i = 0; i < contentTypeList.length; i++)
				{
					for (int k = 0; k < listOfCustomPropValuesArray.length; k++)
					{
						additionalContentMapList.add(extractContentForMeta(listOfCustomPropValuesArray[k], contentTypeList[i], COMPLEX_META_TBL_EXTENSION + SITE_MAP_CUSTOM_PROP, siteId));
					}
				}
			}
		}
		// extract generic types and subtypes as given in env variable
		// SiteMapGenericContentTypes and SiteMapGenericContentSubTypes
		HashMap<String, String> genericContentMap = extractGenericContent(siteId);
		

		// get from DB the latest update date for a content assigned to a node:
		// nodeId -> latestDate
		// so sections latest date equals to its latest content
		Hashtable<String, String> latestSectionsUpdatesMap = extractLatestUpdateDatesForSections(siteId);

		String siteUrl = getEnvironmentValue("siteMapUrl"+siteId);
		debugKeyPoints("site url from environment file: |" + siteUrl + "|");
		if (siteUrl == null){
			siteUrl = getDefaultHttpSiteAddress(siteId);
		}
		debugKeyPoints("siteUrl: " + siteUrl); 

		ArrayList<String> lListIndexerContents = extractIndexerContent(siteId);

		HaysGoogleSiteMapBuilder gsiteMapBuilder = new HaysGoogleSiteMapBuilder(siteId, siteUrl, latestSectionsUpdatesMap, m_service, SITE_MAP_CUSTOM_PROP, SITE_MAP_CON_TYPE_PROP,
				SITE_MAP_CON_SUB_TYPE_PROP);
		gsiteMapBuilder.setDefaultSectionId(SITE_MAP_DEFAULT_SECTIONID);
		gsiteMapBuilder.setAdditionalContentMapList(additionalContentMapList);
		gsiteMapBuilder.setIndexerContents(lListIndexerContents);
		gsiteMapBuilder.setGenericContentMap(genericContentMap);
		gsiteMapBuilder.setM_siteLocale(SITE_LOCALE);
		writer = gsiteMapBuilder.buildGoogleSiteMap(navigationRS);

		if (writer != null)
			return writer.toString();
		return null;
	}

	/*
	 * Property that is used to tigh sections and contetn is passed as env variable. We make sure it's valid
	 */
	private boolean isValidProjectProperty(String propertyName, SiteInfo siteinfo)
	{
		Properties prop = siteinfo.m_customPropertiesTypeMap;
		debug("Custom properties:" + prop + "\n property: " + propertyName);
		if (prop.containsKey(propertyName))
		{
			return true;
		}
		return false;
	}

	private ResultSet buildSiteNavResultSet(SiteInfo siteinfo) throws ServiceException
	{
		Node node = getHierarchyRootNode(siteinfo);
		String s3 = getNodePropertyAllowMissing(node, "active");
		debug("Hierarchy root: " + node + ", " + s3);
		if (StringUtils.convertToBool(s3, false))
		{
			HierarchyWalker hierarchywalker = new StandardHierarchyWalker(null, false);
			GSiteMapGeneratorCallback gsitemaphierarchycallback = new GSiteMapGeneratorCallback(siteinfo, false, false);
			hierarchywalker.setCallback(gsitemaphierarchycallback);
			hierarchywalker.walk(node);
			return gsitemaphierarchycallback.m_navResultSet;
		}
		else
		{
			throw new ServiceException(LocaleUtils.encodeMessage("csRootNodeNotActive", null));
		}

	}

	/**
	 * Load the current site information
	 * @return current site info
	 * @throws DataException
	 * @throws ServiceException
	 */
	public SiteInfo loadProjectInfo() throws DataException, ServiceException
	{
		String siteId = m_binder.getLocal("siteId");
		if (siteId == null)
			throw new DataException("Site Id is not defined");
		return loadProjectInfo(siteId);
	}

	/**
	 * Load the current site information
	 * @return current site info
	 * @throws DataException
	 * @throws ServiceException
	 */
	private SiteInfo loadProjectInfo(String projectName) throws DataException, ServiceException
	{
		SiteInfo siteinfo = reserveSiteInfo(projectName, false);
		releaseSiteInfo(siteinfo, false);
		return siteinfo;
	}

	/**
	 * Loop through project's sections and extract values of the given custom property. Concatinate these values.
	 * @param customPropName
	 * @param projectDom
	 * @return
	 */
	private String extractCustomPropValues(String customPropName, Document projectDom)
	{
		debug("extractCustomPropValues(): " + customPropName);
		StringBuffer result = new StringBuffer();
		int bulk = 10;
		int count = 0;
		Node attr = null;
		if (projectDom == null)
			return null;
		DocumentTraversal traversal = (DocumentTraversal) projectDom;
		NodeIterator nodes = traversal.createNodeIterator(projectDom.getDocumentElement(), NodeFilter.SHOW_ELEMENT, null, true);
		if (nodes != null)
		{
			Node node = nodes.nextNode();
			while (node != null)
			{
				attr = node.getAttributes().getNamedItem(customPropName);
				if (attr != null)
				{
					count++;
					if (count == bulk)
					{
						result.append(";");
						count = 0;
					}
					else
					{
						result.append(",");
					}
					result.append(attr.getNodeValue());
				}
				node = nodes.nextNode();
			}
			debug("list of node values: " + result);
			if (result.length() > 0)
			{
				return result.substring(1).replaceAll(";", "");
			}
		}
		return null;
	}

	/**
	 * Query to extract content that is classified with the same terms as project's sections to include these
	 * content into Site Map
	 * @param metadataList - list of concatinated values of the given custom project property
	 * @param docType - filter content by this dDocType
	 * @param tblName - complex metadata is stored in this table
	 * @return Result Set of additional content to include into Site Map
	 * @throws DataException
	 */
	private Hashtable<String, String> extractContentForMeta(String metadataList, String docType, String tblName, String siteId) throws DataException, ServiceException
	{
		String query = "QcontentForSiteMap";
		Hashtable<String, String> dataFilesTable = new Hashtable<String, String>();
		String strMetaTerms = metadataList.replaceAll(",", "|");
		String subListMetaTerms = strMetaTerms;
		int intSizeMetaTerms = strMetaTerms.length();
		int intBeginSubList = 0;
		int intEndSubList = intSizeMetaTerms;
		//DataResultSet contentRS = null;
		DataResultSet contentRS = new DataResultSet();

		while (intEndSubList > intBeginSubList)
		{
			if ((intEndSubList - intBeginSubList) > MAX_METATERMS_LENGTH)
				intEndSubList = intBeginSubList + (strMetaTerms.substring(intBeginSubList, intBeginSubList + MAX_METATERMS_LENGTH)).lastIndexOf("|");

			subListMetaTerms = strMetaTerms.substring(intBeginSubList, intEndSubList);
			debug("intBeginSubList: " + intBeginSubList);
			debug("intEndSubList: " + intEndSubList);
			debug("subListMetaTerms: " + subListMetaTerms);
			intBeginSubList = intEndSubList + 1;

			intEndSubList = intSizeMetaTerms;

			DataBinder binder = new DataBinder();
			binder.putLocal("tblName", tblName);
			binder.putLocal("dDocType", docType);
			binder.putLocal("metaTerms", subListMetaTerms);
			binder.putLocal("resultCount", SiteMapHandler.SITE_MAP_RESULT_COUNT);
			binder.putLocal("siteLocale", SITE_LOCALE);

			debug("Bulk extractContentForMeta(): " + binder.getLocalData());
			ResultSet subRS = this.m_workspace.createResultSet(query, binder);

			SystemUtils.trace("sitemap1", "subRS Row present" + subRS.isRowPresent());

			if (subRS != null && subRS.first())
			{
				contentRS.copy(subRS);
				do
				{
					dataFilesTable.put(contentRS.getStringValue(0), contentRS.getStringValue(1)); // metaterm & dDocType, dInDate list				
				}
				while (contentRS.next());
			}
		}
		debug("Result: " + contentRS.isEmpty() + contentRS);

		debug("Additional content map: " + dataFilesTable);

		SystemUtils.trace("sitemap1", "dataFilesTable " + dataFilesTable.size());

		return dataFilesTable;
	}

	private ArrayList<String> extractIndexerContent(String siteId) throws DataException, ServiceException
	{
		String value;
		ArrayList<String> dataFilesTable = new ArrayList<String>();

		String query = "QIndexerContentForSiteMap";

		DataBinder binder = new DataBinder();
		binder.putLocal("tableName", getIndexerTbl());
		binder.putLocal("siteLocale", SITE_LOCALE);

		debug("Bulk extractIndexerContent(): " + binder.getLocalData());

		DataResultSet contentRS = new DataResultSet();
		ResultSet subRS = this.m_workspace.createResultSet(query, binder);

		debug("Indexer subRS Row present" + subRS.isRowPresent());

		if (subRS != null && subRS.first())
		{
			contentRS.copy(subRS);
			do
			{
				// VALUE is comma separated dDocName @ xWebsiteSection @ dInDate
				value = contentRS.getStringValue(0) + SPRTR + contentRS.getStringValue(1) + SPRTR + contentRS.getStringValue(2);
				dataFilesTable.add(value);
			}
			while (contentRS.next());
		}

		debug("Result for QIndexerContentForSiteMap: " + contentRS.isEmpty() + contentRS);
		debug("Indexer content map: " + dataFilesTable);
		return dataFilesTable;
	}

	private HashMap<String, String> extractGenericContent(String siteId) throws DataException, ServiceException
	{
		String doctype, subtype, key, value;
		HashMap<String, String> dataFilesTable = new HashMap<String, String>();

		String query = "QGenericContentForSiteMap";

		DataBinder binder = new DataBinder();
		binder.putLocal("dDocType", (CONFIGURED_TYPES != null) ? getQuotedParam(CONFIGURED_TYPES) : ""); // checked null
		binder.putLocal("xSubType", (CONFIGURED_SUB_TYPES != null) ? getQuotedParam(CONFIGURED_SUB_TYPES) : ""); // checked null
		binder.putLocal("siteLocale", SITE_LOCALE);

		debug("Bulk extractGenericContent(): " + binder.getLocalData());

		DataResultSet contentRS = new DataResultSet();
		ResultSet subRS = this.m_workspace.createResultSet(query, binder);

		debug("Generic subRS Row present" + subRS.isRowPresent());

		if (subRS != null && subRS.first())
		{
			contentRS.copy(subRS);
			do
			{
				doctype = contentRS.getStringValue(2);
				subtype = contentRS.getStringValue(3);
				// if subtype of content is present in environment variable then KEY is subtype otherwise doctype
				key = (subtype != null && !"".equals(subtype) && CONFIGURED_SUB_TYPES.indexOf(subtype) != -1) ? subtype : doctype;

				// VALUE is comma separated dDocName @ xWebsiteSection @ dInDate
				value = contentRS.getStringValue(0) + SPRTR + contentRS.getStringValue(4) + SPRTR + contentRS.getStringValue(1);
				if (dataFilesTable.containsKey(key))
				{
					dataFilesTable.put(key.trim(), dataFilesTable.get(key) + "," + value);
				}
				else
				{
					dataFilesTable.put(key.trim(), value);
				}
			}
			while (contentRS.next());
		}

		debug("Result for QGenericContentForSiteMap: " + contentRS.isEmpty() + contentRS);
		debug("Generic content map: " + dataFilesTable);
		return dataFilesTable;
	}

	/**
	 * Query to get the latest update date for each section of the given site. It's the latest update 
	 * date of a content assigned to this section.
	 * @param siteId
	 * @return
	 * @throws DataException
	 */
	private Hashtable<String, String> extractLatestUpdateDatesForSections(String siteId) throws DataException
	{
		String query = "QlatestSectionUpdateDate";
		Hashtable<String, String> dataFilesTable = new Hashtable<String, String>();

		DataBinder binder = new DataBinder();
		binder.putLocal("siteId", siteId);
		ResultSet contentRS = this.m_workspace.createResultSet(query, binder);
		if (contentRS != null && contentRS.first())
		{
			do
			{
				dataFilesTable.put(contentRS.getStringValue(0), contentRS.getStringValue(1)); // sectionId - latest date formatted				
			}
			while (contentRS.next());
		}
		debug("Latest update dates for sections: " + dataFilesTable);
		return dataFilesTable;
	}

	public static void debug(String message)
	{
		SystemUtils.trace("sitemap", message);
		//	System.out.println(message);
	}

	public static void debugKeyPoints(String message)
	{
		SystemUtils.trace("sitemapkeypoints", message);
		SystemUtils.trace("sitemap", message);
	}

	public static void debug(Exception ex)
	{
		SystemUtils.trace("sitemap", "\nException :" + ex);
		ex.printStackTrace();
	}

	static ResultSet mergeResultSet(ResultSet rs1, ResultSet rs2)
	{
		DataResultSet drs1 = new DataResultSet();
		drs1.copy(rs1);
		debug("rs1 num rows = " + drs1.getNumRows());
		drs1.copy(rs2);
		debug("rs2 num rows = " + drs1.getNumRows());

		return drs1;
	}

	static Node getParentSection(Node node)
	{
		Node node1 = null;
		Node node2 = node.getParentNode();
		if (node2 != null && (node2 instanceof Element))
		{
			String s = node2.getNodeName();
			if (s.equals("section"))
			{
				node1 = node2;
			}
		}
		return node1;
	}

	public class GSiteMapGeneratorCallback implements HierarchyWalkerCallback
	{

		public DataResultSet m_navResultSet;
		protected SiteInfo m_siteInfo;
		protected String m_siteId;
		protected boolean m_incContribOnlySections;
		protected boolean m_bEmitInactiveNodes;
		protected Vector<String> m_vAdditionalFields;

		public int callback(Node node, String s, int i, int j) throws ServiceException
		{
			int k = 1;
			boolean flag = StringUtils.convertToBool(SSHierarchyServiceHandler.getNodePropertyAllowMissing(node, "contributorOnly"), false);
			boolean flag1 = !m_incContribOnlySections && flag;
			String s1 = SSHierarchyServiceHandler.getNodePropertyAllowMissing(node, "active");
			boolean flag2 = StringUtils.convertToBool(s1, false);
			if ((flag2 || m_bEmitInactiveNodes) && !flag1)
			{
				k = 0;
				String s2 = null;
				boolean flag3 = i == 0;
				if (flag3)
				{
					s2 = SSHierarchyServiceHandler.getNodePropertyAllowMissing(node, "navLabel");
				}
				if (s2 == null)
				{
					s2 = SSHierarchyServiceHandler.getNodePropertyAllowMissing(node, "label");
				}
				s2 = s2 == null ? "" : s2;
				String s3 = SSHierarchyServiceHandler.getNodePropertyAllowMissing(node, "nodeId");
				s3 = s3 == null ? "" : s3;
				String s4 = getSiteRelativePublishUrl(m_siteId, node);
				s4 = s4 == null ? "" : s4;
				String s5 = null;
				Node node1 = getParentSection(node);
				if (node1 != null)
				{
					s5 = SSHierarchyServiceHandler.getNodePropertyAllowMissing(node1, "nodeId");
				}
				s5 = s5 == null ? "" : s5;
				int l = 0;

				Vector<String> vector = m_navResultSet.createEmptyRow();
				vector.setElementAt(s3, l++);
				vector.setElementAt(s5, l++);
				vector.setElementAt(s2, l++);
				vector.setElementAt("" + i, l++);
				vector.setElementAt(s4, l++);
				if (m_vAdditionalFields != null)
				{
					for (int i1 = 0; i1 < m_vAdditionalFields.size(); i1++)
					{
						String s6 = (String) m_vAdditionalFields.elementAt(i1);
						String s7 = SSHierarchyServiceHandler.getNodePropertyAllowMissing(node, s6);
						if (s7 != null)
						{
							vector.setElementAt(s7, l + i1);
						}
					}

				}
				m_navResultSet.addRow(vector);
			}
			return k;
		}

		public int finishLevel(int i) throws ServiceException
		{
			return 0;
		}

		public void finish()
		{
		}

		GSiteMapGeneratorCallback(SiteInfo siteinfo, boolean incContribOnlySections, boolean emitInactiveNodes)
		{
			m_navResultSet = null;
			m_siteInfo = null;
			m_siteId = null;
			m_incContribOnlySections = false;
			m_bEmitInactiveNodes = false;
			m_vAdditionalFields = null;
			m_siteInfo = siteinfo;
			m_siteId = siteinfo.m_siteId;
			m_incContribOnlySections = incContribOnlySections;
			m_bEmitInactiveNodes = emitInactiveNodes;
			m_navResultSet = new DataResultSet(SiteMapHandler.SS_NAV_RESULT_SET_FIELDS);
			String s = SharedObjects.getEnvironmentValue("SSAdditionalNavResultSetFields");
			if (s != null && s.length() != 0)
			{
				Vector<String> vector = StringUtils.parseArrayEx(s, ',', '^', true);
				if (vector != null && vector.size() > 0)
				{
					m_vAdditionalFields = vector;
					String as[] = StringUtils.convertToArray(vector);
					DataResultSet dataresultset = new DataResultSet(as);
					m_navResultSet.mergeFields(dataresultset);
				}
			}
		}
	}

	public String getQuotedParam(String pParamName)
	{
		String returnString = "";
		try
		{
			returnString = pParamName.replaceAll(",", "','");
		}
		catch (Exception e)
		{

		}
		return returnString.trim();
	}

	public static String getDataDir(String siteId) throws ServiceException, DataException
	{
		String path = DirectoryLocator.getAppDataDirectory() + SITEMAP + siteId;
		FileUtils.checkOrCreateDirectoryPrepareForLocks(path, 1, true);
		return (path);
	}

	private String getIndexerTbl()
	{
		String m_tableName = "IDCTEXT1";
		try
		{
			IndexerConfig m_config = SearchIndexerUtils.getIndexerConfig(null, "update");
			String m_activeIndex = ActiveState.getActiveProperty("ActiveIndex");
			m_tableName = OracleTextUtils.getTableName(m_activeIndex, m_config);
			SystemUtils.trace("Translation", "Indexer TBL: " + m_activeIndex + ", " + m_tableName);

		}
		catch (Exception e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return m_tableName;
	}

	public void submitSiteMapToGoogle(String siteId) throws ServiceException
	{
		String siteUrl = getEnvironmentValue("siteMapUrl"+siteId);
		debugKeyPoints("site url from environment file: |" + siteUrl + "|");
		if (siteUrl == null)
			siteUrl = getDefaultHttpSiteAddress(siteId);
		debugKeyPoints("site url : " + siteUrl);
		debugKeyPoints("google site map url : " + GOOGLE_SUBMIT_URL);
		
		GOOGLE_SUBMIT_URL = GOOGLE_SUBMIT_URL.replace("REPLACE_URL", siteUrl);

		debugKeyPoints("GOOGLE_SUBMIT_URL : " + GOOGLE_SUBMIT_URL);

		CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
		String PROXY_HOST = getEnvironmentValue("PROXY_HOST");
		String PROXY_PORT = getEnvironmentValue("PROXY_PORT");
		final String authUser = getEnvironmentValue("PROXY_AUTH_USER");
		final String authPassword = getEnvironmentValue("PROXY_AUTH_PASSWORD");
		Authenticator.setDefault(new Authenticator()
		{
			public PasswordAuthentication getPasswordAuthentication()
			{
				return new PasswordAuthentication(authUser, authPassword.toCharArray());
			}
		});
		System.setProperty("http.proxyUser", authUser);
		System.setProperty("http.proxyPassword", authPassword);
		System.setProperty("http.proxyHost", PROXY_HOST);
		System.setProperty("http.proxyPort", PROXY_PORT);

		debugKeyPoints("http.proxyUser : " + authUser);
		debugKeyPoints("http.proxyPassword : " + authPassword);
		debugKeyPoints("http.proxyHost : " + PROXY_HOST);
		debugKeyPoints("http.proxyPort : " + PROXY_PORT);

		URL url = null;
		try
		{
			url = new URL(null, GOOGLE_SUBMIT_URL, new sun.net.www.protocol.http.Handler());
			debugKeyPoints("URL : " + url);
		}
		catch (MalformedURLException e)
		{
			debugKeyPoints("URL creation exception : " + e);
		}

		HttpURLConnection conn = null;
		try
		{
			conn = (HttpURLConnection) url.openConnection();
			debugKeyPoints("Connection response code: " + conn.getResponseCode());
		}
		catch (IOException e)
		{
			debugKeyPoints("Connection creation exception : " + e);
		}

	}

}
