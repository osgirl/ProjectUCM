package hays.custom;

import static hays.com.commonutils.HaysWebApiUtils.HandleExceptions;
import static hays.com.commonutils.HaysWebApiUtils.createResultSetFromData;
import static hays.com.commonutils.HaysWebApiUtils.parseXML;
import hays.com.commonutils.NamespaceResolver;
import intradoc.common.DataStreamWrapper;
import intradoc.common.ExecutionContext;
import intradoc.common.LocaleUtils;
import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.data.DataException;
import intradoc.data.DataResultSet;
import intradoc.data.ResultSet;
import intradoc.data.Workspace;
import intradoc.filestore.FileStoreUtils;
import intradoc.filestore.IdcFileDescriptor;
import intradoc.provider.Provider;
import intradoc.provider.Providers;
import intradoc.server.ServiceHandler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class HaysDocumentContent extends ServiceHandler {
	public final static String TRACE_NAME = "webAPI_getDocumentContent";

	public void getDocumentContents() throws ServiceException, DataException {
		try {
			String resultSetName = this.m_currentAction.getParamAt(0);
			SystemUtils.trace(TRACE_NAME, "resultSetName " + resultSetName);

			String contentid = m_binder.getLocal("contentid");
			SystemUtils.trace(TRACE_NAME, "contentid " + contentid);
			m_binder.putLocal("dDocName", contentid);
			m_binder.putLocal("allowInterrupt", "1");
			m_binder.putLocal("RevisionSelectionMethod", "LatestReleased");
			SystemUtils.trace(TRACE_NAME, "dDocName "
					+ m_binder.getLocal("dDocName"));

			
			m_binder.removeLocal("IsSoap");
			m_service.executeServiceEx("DOC_INFO_BY_NAME", true);

			String path = getFile();
			SystemUtils.trace(TRACE_NAME, path);
			File lFile = new File(path);
			if (lFile.exists())
				SystemUtils.trace(TRACE_NAME, "FILE EXISTS");

			String lDocURL = path;
			SystemUtils.trace(TRACE_NAME, "DocUrl : " + lDocURL);

			ArrayList<String> lData = null;
			ResultSet lResultSet = null;
			Document lDoc = null;
			lDoc = parseXML(lDocURL);
			lData = getValues(lDoc);
			ArrayList<String> lHeaderList = new ArrayList<String>();
			lHeaderList.add("Title");
			lHeaderList.add("Summary");
			lHeaderList.add("Body");
			lResultSet = createResultSetFromData(lHeaderList, lData);
			m_binder.clearResultSets();
			clearLocalData();
			m_binder.addResultSet(resultSetName, lResultSet);
			this.m_binder.putLocal("StatusMessage", LocaleUtils.encodeMessage(
					"wwWebApiOKMsg", null));
			this.m_binder.putLocal("StatusCode", "UC000");
		} catch (ParserConfigurationException e) {
			HandleExceptions(m_binder, "UC013", "wwFileNotParse");
		} catch (SAXException e) {
			HandleExceptions(m_binder, "UC012", "wwFileNotFormedProperly");
		} catch (IOException e) {
			HandleExceptions(m_binder, "UC011", "wwFileNotFound");
		} catch (Exception e) {
			e.printStackTrace();
			HandleExceptions(m_binder, "UC011", "wwFileNotFound");
		}
	}

	public String getFile() {
		DataStreamWrapper localDataStreamWrapper = m_service.getDownloadStream(true);
		try {
			String str2 = this.m_binder.getAllowMissing("Rendition");

			if ((str2 == null) || (str2.equalsIgnoreCase("primary"))) {
				str2 = "primaryFile";
			}
			m_binder.putLocal("RenditionId", str2);
			IdcFileDescriptor localIdcFileDescriptor = null;
			//SystemUtils.trace(TRACE_NAME, "binder " + m_binder);
			localIdcFileDescriptor = m_service.m_fileStore.createDescriptor(m_binder, null, (ExecutionContext) m_service);
			SystemUtils.trace(TRACE_NAME, localIdcFileDescriptor.toString());
			localDataStreamWrapper.m_descriptor = localIdcFileDescriptor;
			FileStoreUtils.forceDownloadStreamToFilePath(localDataStreamWrapper, m_service.m_fileStore,(ExecutionContext) m_service);
		} catch (Exception localException) {
			localException.printStackTrace();
		}

		return localDataStreamWrapper.m_filePath;
	}

	public ArrayList<String> getValues(Document lDocument)
			throws ServiceException, DataException, XPathExpressionException {
		XPath xpath = XPathFactory.newInstance().newXPath();
		xpath.setNamespaceContext(new NamespaceResolver());
		ArrayList<String> lresult = new ArrayList<String>();
		getSiteId();
		String doctype = m_binder.getLocal("dDocType");
		ResultSet lResultSet = m_binder.getResultSet("DOC_INFO");
		String subdoctype = lResultSet.getStringValueByName("xSubType");
		SystemUtils.trace(TRACE_NAME, "DocType : " + doctype+" "+subdoctype);
		if("Press releases".equals(subdoctype)){
			XPathExpression expr = xpath.compile("//wcm:root/wcm:element[@name='Title']/text()");
			lresult.add(resolveIDOCFunctions(expr.evaluate(lDocument, XPathConstants.STRING).toString()));
			expr = xpath.compile("//wcm:root/wcm:element[@name='Summary']/text()");
			lresult.add(resolveIDOCFunctions(expr.evaluate(lDocument, XPathConstants.STRING).toString()));
			expr = xpath.compile("//wcm:root/wcm:list[@name='Content']/wcm:row/wcm:element[@name='Body']/text()");
			lresult.add(resolveIDOCFunctions(expr.evaluate(lDocument, XPathConstants.STRING).toString()));
		}else{
			XPathExpression expr = xpath
					.compile("//wcm:root/wcm:list[@name='Content']/wcm:row/wcm:element[@name='Title']/text()");
			lresult.add(resolveIDOCFunctions(expr.evaluate(lDocument,
					XPathConstants.STRING).toString()));
			expr = xpath
					.compile("//wcm:root/wcm:list[@name='Content']/wcm:row/wcm:element[@name='Summary']/text()");
			lresult.add(resolveIDOCFunctions(expr.evaluate(lDocument,
					XPathConstants.STRING).toString()));
			expr = xpath
					.compile("//wcm:root/wcm:list[@name='Content']/wcm:row/wcm:element[@name='Body']/text()");
			lresult.add(resolveIDOCFunctions(expr.evaluate(lDocument,
					XPathConstants.STRING).toString()));
		}
		return lresult;
	}

	public String resolveIDOCFunctions(String pStringToEvaluate) {
		String websiteAddress = m_binder.getLocal("websiteAddress");//commented for TS
		String returnString = null;
		String localization = null;
		
		if (!websiteAddress.startsWith("http")) {
			websiteAddress = "http://" + websiteAddress;
		}
		String	websiteAddress1 = "";
		int last_slash = websiteAddress.lastIndexOf("/");
		if(last_slash>10)
		{
			websiteAddress1 = websiteAddress.substring(0,last_slash);
			localization = websiteAddress.substring(last_slash+1);
			
		}
		else
			websiteAddress1 = websiteAddress;
		
		SystemUtils.trace(TRACE_NAME, "websiteAddress " + websiteAddress);
		SystemUtils.trace(TRACE_NAME, "websiteAddress1 " + websiteAddress1);
		
		pStringToEvaluate = pStringToEvaluate.replaceAll("src=\"\\[!--",
				"src=\"" + websiteAddress1 + "<");
		pStringToEvaluate = pStringToEvaluate.replaceAll("href=\"\\[!--",
				"href=\""+ websiteAddress + "<");
		pStringToEvaluate = pStringToEvaluate.replaceAll("--]", "\\$>");
		pStringToEvaluate = pStringToEvaluate.replaceAll(
				"\\?ssSourceSiteId=null", "");
		
		SystemUtils.trace(TRACE_NAME, "pStringToEvaluate " + pStringToEvaluate);
		try {
			String siteId = m_binder.getLocal("siteId");
			SystemUtils.trace(TRACE_NAME, "siteId " + siteId);
			returnString = m_service.getPageMerger().evaluateScript(pStringToEvaluate);
			returnString = returnString.replaceAll(websiteAddress+websiteAddress, websiteAddress);
			if(last_slash>10)
				returnString = returnString.replaceAll(localization+"/"+localization, localization);
			return returnString;
		} catch (IllegalArgumentException e) {
			return pStringToEvaluate;
		} catch (IOException e) {
			return pStringToEvaluate;
		}
	}

	private void clearLocalData() {
		Properties localData = m_binder.getLocalData();
		ArrayList<String> keywordsList = new ArrayList(localData.keySet());
		ArrayList<String> allowedList = new ArrayList<String>();
		allowedList.add("refreshSubMonikers");
		allowedList.add("dUser");
		allowedList.add("refreshMonikers");
		allowedList.add("changedMonikers");
		allowedList.add("IdcService");
		allowedList.add("outputFormat");
		allowedList.add("statusCode");
		allowedList.add("statusMessage");
		for (String key : keywordsList) {
			if (!allowedList.contains(key)) {
				m_binder.removeLocal(key);
			}
		}
	}

	public void getSiteId() throws ServiceException, DataException {
		ResultSet lResultSet = m_binder.getResultSet("DOC_INFO");
		String siteId = lResultSet.getStringValueByName("xWebsites");
		String QwebsiteAddressQuery = this.m_currentAction.getParamAt(1);
		String websiteAddress = "";
		String dSiteId = siteId.trim();
		
		if(dSiteId.indexOf(",")>0)
		{
			dSiteId = dSiteId.substring(0, dSiteId.indexOf(","));
		}
		m_binder.putLocal("siteId", dSiteId);
		m_binder.putLocal("dSiteId", dSiteId);

		Provider p = Providers.getProvider("SystemDatabase");
		if ((p == null) || (!p.isProviderOfType("database"))) {
			throw new ServiceException(
					"You the provider SystemDatabase  is not a valid provider of type 'database'.");
		}

		SystemUtils.trace(TRACE_NAME, "Website Address Query:  "
				+ QwebsiteAddressQuery);

		Workspace databaseServerWs = (Workspace) p.getProvider();
		ResultSet rsSiteAddress = databaseServerWs.createResultSet(
				QwebsiteAddressQuery, m_binder);
		DataResultSet drsSiteAddress = new DataResultSet();
		drsSiteAddress.copy(rsSiteAddress);
		if (drsSiteAddress != null && drsSiteAddress.getNumRows() > 0) {
			SystemUtils.trace(TRACE_NAME, "Website Address DataResultSet:  "
					+ drsSiteAddress.toString());
			do {
				if (("1").equals(drsSiteAddress
						.getStringValueByName("dIsDefault"))) {
					websiteAddress = drsSiteAddress
							.getStringValueByName("dAddress");
				}
				/*
				 * SystemUtils.trace("search", "\nIn while: " + fieldIndex); int
				 * currentRowIndex = drset.getCurrentRow(); String dDocName =
				 * drset.getCurrentRowMap().get("dDocName").toString(); //String
				 * dRevLabel =
				 * drset.getCurrentRowMap().get("dRevLabel").toString(); Vector
				 * v = drset.getCurrentRowValues(); String
				 * serverAddress=SharedObjects
				 * .getEnvironmentValue("HttpServerAddress"); v.set(fieldIndex,
				 * restJobDetailUrl
				 * +(dDocName.toLowerCase())+"?format="+binder.getLocal
				 * ("outputFormat"));
				 * 
				 * 
				 * sb=sb.append(",").append(drset.getCurrentRowMap().get("dID"));
				 */

			} while (drsSiteAddress.next());

		}
		SystemUtils.trace(TRACE_NAME, "Website Address:  " + websiteAddress);
		m_binder.putLocal("websiteAddress", websiteAddress);

	}

}
