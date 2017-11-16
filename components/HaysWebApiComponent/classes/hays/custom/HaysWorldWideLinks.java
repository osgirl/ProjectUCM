package hays.custom;

import static hays.com.commonutils.HaysWebApiUtils.HandleExceptions;
import static hays.com.commonutils.HaysWebApiUtils.createResultSetFromData;
import static hays.com.commonutils.HaysWebApiUtils.grabHTMLLinks;
import static hays.com.commonutils.HaysWebApiUtils.parseXML;
import hays.com.commonutils.EntityHaysWebsites;
import hays.com.commonutils.HaysWebApiUtils;
import hays.com.commonutils.NamespaceResolver;
import intradoc.common.DataStreamWrapper;
import intradoc.common.ExecutionContext;
import intradoc.common.LocaleUtils;
import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.data.DataException;
import intradoc.data.DataResultSet;
import intradoc.data.ResultSet;
import intradoc.filestore.FileStoreUtils;
import intradoc.filestore.IdcFileDescriptor;
import intradoc.server.ServiceHandler;

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
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class HaysWorldWideLinks extends ServiceHandler
{
	public final static String TRACE_NAME = "webAPI_getHaysWorldWideLinks";

	public void getHaysWorldWideLinks() throws ServiceException, DataException
	{
		try
		{
			String resultSetName = this.m_currentAction.getParamAt(0);
			SystemUtils.trace(TRACE_NAME, "resultSetName " + resultSetName);

			String lLocale = m_binder.getLocal("locale");
			SystemUtils.trace(TRACE_NAME, "lLocale " + lLocale);
			
			EntityHaysWebsites lEntityHaysWebsites = HaysWebApiUtils.getHaysWebsitesData((DataResultSet) this.m_binder
					.getResultSet("LOCALE_DETAILS"));
			SystemUtils.trace(TRACE_NAME,
					"lCountryRegion=" + lEntityHaysWebsites.getlDataFilePrefix());
			m_binder.putLocal("dDocName", lEntityHaysWebsites.getlDataFilePrefix() + "_DF_HEADER");
			SystemUtils.trace(TRACE_NAME, "dDocName " + m_binder.getLocal("dDocName"));

			m_service.executeServiceEx("DOC_INFO_BY_NAME", true);
			m_binder.putLocal("allowInterrupt", "1");
			m_binder.putLocal("RevisionSelectionMethod", "LatestReleased");
			
			String lDocURL = getFile();
			SystemUtils.trace(TRACE_NAME, lDocURL);
			SystemUtils.trace(TRACE_NAME, "DocUrl : " + lDocURL);

			ArrayList<String> lData = null;
			ResultSet lResultSet = null;
			Document lDoc = null;
			lDoc = parseXML(lDocURL);
			lData = getValues(lDoc);
			ArrayList<String> lHeaderList = new ArrayList<String>();
			lHeaderList.add("Title");
			lHeaderList.add("Target");
			lResultSet = createResultSetFromData(lHeaderList, lData);
			m_binder.clearResultSets();
			clearLocalData();
			m_binder.addResultSet(resultSetName, lResultSet);
			this.m_binder.putLocal("StatusMessage", LocaleUtils.encodeMessage("wwWebApiOKMsg", null));
			this.m_binder.putLocal("StatusCode", "UC000");
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
			HandleExceptions(m_binder, "UC011", "wwFileNotFound");
		}
	}

	public ArrayList<String> getValues(Document lDocument) throws XPathExpressionException
	{
		XPath xpath = XPathFactory.newInstance().newXPath();
		xpath.setNamespaceContext(new NamespaceResolver());
		XPathExpression expr = xpath.compile("//wcm:root/wcm:list[@name='WorldWideLinks']/wcm:row/wcm:element/text()");

		Object result = expr.evaluate(lDocument, XPathConstants.NODESET);
		NodeList nodes = (NodeList) result;

		StringBuilder lsb = new StringBuilder();
		for (int i = 0; i < nodes.getLength(); i++)
		{
			lsb.append(nodes.item(i).getNodeValue());
		}
		ArrayList<String> lresult = grabHTMLLinks(lsb.toString());
		return lresult;
	}

	public void clearLocalData()
	{
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
		for (String key : keywordsList)
		{
			if (!allowedList.contains(key))
			{
				m_binder.removeLocal(key);
			}
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

}
