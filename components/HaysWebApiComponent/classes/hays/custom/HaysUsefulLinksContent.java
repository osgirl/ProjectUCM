package hays.custom;


import static hays.com.commonutils.HaysWebApiUtils.createResultSetFromData;
import static hays.com.commonutils.HaysWebApiUtils.parseXML;
import hays.com.commonutils.NamespaceResolver;
import intradoc.common.DataStreamWrapper;
import intradoc.common.ExecutionContext;
import intradoc.common.LocaleUtils;
import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.data.DataBinder;
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
import java.util.HashMap;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class HaysUsefulLinksContent extends ServiceHandler {

	
		public final static String TRACE_NAME = "hays_useful_links";

		public void getUsefulLinksContent() throws ServiceException, DataException
		{
				try
				{
					String locale = m_binder.getLocal("locale");
					SystemUtils.trace(TRACE_NAME, "dDocName: " + locale);
					
					String contentId = "USEFULLINKS_"+locale;
					m_binder.putLocal("dDocName", contentId);
					SystemUtils.trace(TRACE_NAME, "dDocName: " + contentId);
					m_binder.putLocal("allowInterrupt", "1");
					m_binder.putLocal("RevisionSelectionMethod", "LatestReleased");
					m_binder.removeLocal("IsSoap");
					m_service.executeServiceEx("DOC_INFO_BY_NAME", true);
					
					SystemUtils.trace(TRACE_NAME, "DOC_INFO_BY_NAME executed successfully.");
					getSiteId();
					
					String path = getFile();
					SystemUtils.trace(TRACE_NAME, path);
					File lFile = new File(path);
					if (lFile.exists())
						SystemUtils.trace(TRACE_NAME, "FILE EXISTS");

					HashMap<String, ArrayList<String>> lMap = readHeaderFooterFiles(path);
					
					m_binder.clearResultSets();
					convertToUCMFormat(lMap);

					clearLocalData();
					this.m_binder.putLocal("StatusMessage", LocaleUtils.encodeMessage("wwWebApiOKMsg", null));
					this.m_binder.putLocal("StatusCode", "UC000");
				}
				catch (ParserConfigurationException e)
				{
					SystemUtils.trace(TRACE_NAME, "Exception: " + e.getMessage());
					HandleUsefulLinksExceptions(m_binder, "UC013", "wwFileNotParse");
				}
				catch (SAXException e)
				{
					SystemUtils.trace(TRACE_NAME, "Exception: " + e.getMessage());
					HandleUsefulLinksExceptions(m_binder, "UC012", "wwFileNotFormedProperly");
				}
				catch (IOException e)
				{
					SystemUtils.trace(TRACE_NAME, "Exception: " + e.getMessage());
					HandleUsefulLinksExceptions(m_binder, "UC011", "wwFileNotFound");
				}
				catch (Exception e)
				{
					SystemUtils.trace(TRACE_NAME, "Exception: " + e.getMessage());
					HandleUsefulLinksExceptions(m_binder, "UC011", "wwFileNotFound");
				}
		}
		
		public String getFile()
		{
			DataStreamWrapper localDataStreamWrapper = m_service.getDownloadStream(true);
			try
			{
				String str2 = this.m_binder.getAllowMissing("Rendition");

				if ((str2 == null) || (str2.equalsIgnoreCase("primary")))
				{
					str2 = "primaryFile";
				}
				m_binder.putLocal("RenditionId", str2);
				IdcFileDescriptor localIdcFileDescriptor = null;
				//SystemUtils.trace(TRACE_NAME, "binder " + m_binder);
				localIdcFileDescriptor = m_service.m_fileStore.createDescriptor(m_binder, null, (ExecutionContext) m_service);
				SystemUtils.trace(TRACE_NAME, localIdcFileDescriptor.toString());
				localDataStreamWrapper.m_descriptor = localIdcFileDescriptor;
				FileStoreUtils.forceDownloadStreamToFilePath(localDataStreamWrapper, m_service.m_fileStore, (ExecutionContext) m_service);
			}
			catch (Exception localException)
			{
				localException.printStackTrace();
			}

			return localDataStreamWrapper.m_filePath;
		}

		private void convertToUCMFormat(HashMap<String, ArrayList<String>> pMap)
		{
			ArrayList<String> lHeaderList = new ArrayList<String>();
			lHeaderList.add("label");
			lHeaderList.add("url");

			DataResultSet lSearchResult = null;

			for (String key : pMap.keySet())
			{
				lSearchResult = (DataResultSet) createResultSetFromData(lHeaderList, pMap.get(key));
				m_binder.addResultSet(key, lSearchResult);
			}
			System.out.println("done");
		}

		public HashMap<String, ArrayList<String>> readHeaderFooterFiles(String pFilePath) throws ParserConfigurationException, SAXException,
				IOException, XPathExpressionException
		{
			XPath xpath = null;
			XPathExpression expr = null;
			XPathExpression exprText = null;
			Node listNode = null;
			Element listElement = null;
			NodeList listNodes = null;
			NodeList linkNodes = null;
			String listName = null;
			String temp = null;
			String href = null;
			String text = null;
			HashMap<String, ArrayList<String>> lReturnMap = new HashMap<String, ArrayList<String>>();
			ArrayList<String> lTempList = null;
			Document lDocument = parseXML(pFilePath);
			SystemUtils.trace(TRACE_NAME, pFilePath + " parsed");
			xpath = XPathFactory.newInstance().newXPath();
			xpath.setNamespaceContext(new NamespaceResolver());
			expr = xpath.compile("//wcm:root/wcm:list");
			listNodes = (NodeList) expr.evaluate(lDocument, XPathConstants.NODESET);
			
			expr = xpath.compile("wcm:row/wcm:element");
			exprText = xpath.compile("text()");
			SystemUtils.trace(TRACE_NAME, "List listNodes.getLength() :: " +listNodes.getLength());
			for (int i = 0; i < listNodes.getLength(); i++)
			{
				listNode = listNodes.item(i);
				SystemUtils.trace(TRACE_NAME, "listNode :: " +listNode.getNodeValue());
				if (listNode != null && listNode.getNodeType() == Node.ELEMENT_NODE)
				{
					listElement = (Element) listNode;
					listName = listElement.getAttribute("name");
					linkNodes = (NodeList) expr.evaluate(listElement, XPathConstants.NODESET);
					SystemUtils.trace(TRACE_NAME, "listNodes.getLength() :: " +listNodes.getLength());
					if (linkNodes.getLength() > 0)
					{
						lTempList = new ArrayList<String>();
						for (int j = 0; j < linkNodes.getLength(); j++)
						{
							SystemUtils.trace(TRACE_NAME, "Element listNode :: " +listNode.getNodeValue());
							temp = exprText.evaluate(linkNodes.item(j), XPathConstants.STRING).toString();
							href = temp.replaceAll("<a.*href=\"", "").replaceAll("\".*>", "").replace("<p>", "");
							text = temp.replaceAll("\\<.*?>", "");
							lTempList.add(text);
							lTempList.add(resolveIDOCFunctions(href));
						}
						lReturnMap.put(listName, lTempList);
					}
				}
			}
			/*exprText = xpath.compile("//wcm:root/wcm:element[@name='WorldWideTitle']/text()");
			temp = exprText.evaluate(lDocument, XPathConstants.STRING).toString();
			if (null != temp && !"".equals(temp))
			{
				lTempList = new ArrayList<String>();
				lTempList.add("Text");
				lTempList.add(temp);
				lReturnMap.put("WorldWideTitle", lTempList);
			}
			
			exprText = xpath.compile("//wcm:root/wcm:element[@name='SocialNetworking']/text()");
			temp = exprText.evaluate(lDocument, XPathConstants.STRING).toString();
			if (null != temp && !"".equals(temp))
			{
				lTempList = new ArrayList<String>();
				lTempList.add("Text");
				lTempList.add(temp);
				lReturnMap.put("SocialNetworking", lTempList);
			}
			
			exprText = xpath.compile("//wcm:root/wcm:element[@name='LegalText']/text()");
			temp = exprText.evaluate(lDocument, XPathConstants.STRING).toString();
			if (null != temp && !"".equals(temp))
			{
				lTempList = new ArrayList<String>();
				href = temp.replaceAll("<a.*href=\"", "").replaceAll("\".*>", "");
				text = temp.replaceAll("\\<.*?>", "");
				lTempList.add(text);
				lTempList.add(resolveIDOCFunctions(href));
				SystemUtils.trace(TRACE_NAME, "text: " + lTempList.get(0) + " href: " + lTempList.get(1));
				lReturnMap.put("LegalText", lTempList);
			}*/
			return lReturnMap;
		}

		private void clearLocalData()
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

		public String resolveIDOCFunctions(String pStringToEvaluate)
		{
			String websiteAddress = m_binder.getLocal("websiteAddress");//commented for TS
			String returnString = null;
			String localization = null;

			if (!websiteAddress.startsWith("http"))
			{
				websiteAddress = "http://" + websiteAddress;
			}
			String websiteAddress1 = "";
			int last_slash = websiteAddress.lastIndexOf("/");
			if (last_slash > 10)
			{
				websiteAddress1 = websiteAddress.substring(0, last_slash);
				localization = websiteAddress.substring(last_slash + 1);

			}
			else
				websiteAddress1 = websiteAddress;

			SystemUtils.trace(TRACE_NAME, "websiteAddress " + websiteAddress);
			SystemUtils.trace(TRACE_NAME, "websiteAddress1 " + websiteAddress1);
			SystemUtils.trace(TRACE_NAME, "pStringToEvaluate " + pStringToEvaluate);
			pStringToEvaluate = pStringToEvaluate.replaceAll("\\[!--", websiteAddress + "<");
			pStringToEvaluate = pStringToEvaluate.replaceAll("--]", "\\$>");
			pStringToEvaluate = pStringToEvaluate.replaceAll("\\?ssSourceSiteId=null", "");

			SystemUtils.trace(TRACE_NAME, "pStringToEvaluate " + pStringToEvaluate);
			try
			{
				String siteId = m_binder.getLocal("siteId");
				SystemUtils.trace(TRACE_NAME, "siteId " + siteId);
				returnString = m_service.getPageMerger().evaluateScript(pStringToEvaluate);
				returnString = returnString.replaceAll(websiteAddress + websiteAddress, websiteAddress);
				if (last_slash > 10)
					returnString = returnString.replaceAll(localization + "/" + localization, localization);
				if (returnString.contains("profile"))
				{
					returnString = returnString.replaceAll(websiteAddress, "");
				}
				return returnString;
			}
			catch (IllegalArgumentException e)
			{
				return pStringToEvaluate;
			}
			catch (IOException e)
			{
				return pStringToEvaluate;
			}
		}

		public void getSiteId() throws ServiceException, DataException
		{
			ResultSet lResultSet = m_binder.getResultSet("DOC_INFO");
			String siteId = lResultSet.getStringValueByName("xWebsites");
			String QwebsiteAddressQuery = this.m_currentAction.getParamAt(1);
			String websiteAddress = "";
			String dSiteId = siteId.trim();

			if (dSiteId.indexOf(",") > 0)
			{
				dSiteId = dSiteId.substring(0, dSiteId.indexOf(","));
			}
			m_binder.putLocal("siteId", dSiteId);
			m_binder.putLocal("dSiteId", dSiteId);

			Provider p = Providers.getProvider("SystemDatabase");
			if ((p == null) || (!p.isProviderOfType("database")))
			{
				throw new ServiceException("You the provider SystemDatabase  is not a valid provider of type 'database'.");
			}

			SystemUtils.trace(TRACE_NAME, "Website Address Query:  " + QwebsiteAddressQuery);

			Workspace databaseServerWs = (Workspace) p.getProvider();
			ResultSet rsSiteAddress = databaseServerWs.createResultSet(QwebsiteAddressQuery, m_binder);
			DataResultSet drsSiteAddress = new DataResultSet();
			drsSiteAddress.copy(rsSiteAddress);
			if (drsSiteAddress != null && drsSiteAddress.getNumRows() > 0)
			{
				SystemUtils.trace(TRACE_NAME, "Website Address DataResultSet:  " + drsSiteAddress.toString());
				do
				{
					if (("1").equals(drsSiteAddress.getStringValueByName("dIsDefault")))
					{
						websiteAddress = drsSiteAddress.getStringValueByName("dAddress");
					}
				}
				while (drsSiteAddress.next());
			}
			SystemUtils.trace(TRACE_NAME, "Website Address:  " + websiteAddress);
			m_binder.putLocal("websiteAddress", websiteAddress);
		}

		public static void HandleUsefulLinksExceptions(DataBinder pBinder, String pErrorCode, String pErrorStringKey) throws ServiceException
		{
			pBinder.putLocal("StatusMessage", LocaleUtils.encodeMessage(pErrorStringKey, null));
			pBinder.putLocal("StatusCode", pErrorCode);
			pBinder.removeResultSet("error");
			pBinder.m_resultSets.remove("error");
			pBinder.putLocal("ssChangeHTTPHeader","true");
			pBinder.putLocal("HaysNewHTTPStatus","HTTP/1.1 200 OK");
			pBinder.putLocal("HaysOldHTTPStatus1","HTTP/1.1 503 Service Unavailable");
		}

	}

