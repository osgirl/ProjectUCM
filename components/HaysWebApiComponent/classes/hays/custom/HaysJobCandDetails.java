package hays.custom;

import static hays.com.commonutils.HaysWebApiUtils.createResultSetFromData;
import static hays.com.commonutils.HaysWebApiUtils.isNotNull;
import static hays.com.commonutils.HaysWebApiUtils.parseXML;
import hays.com.commonutils.HaysWebApiUtils;
import hays.com.commonutils.NamespaceResolver;
import infomentum.ontology.Converter;
import infomentum.ontology.loader.OntologyFacade;
import intradoc.common.DataStreamWrapper;
import intradoc.common.ExecutionContext;
import intradoc.common.LocaleResources;
import intradoc.common.LocaleUtils;
import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.data.DataException;
import intradoc.data.DataResultSet;
import intradoc.data.FieldInfo;
import intradoc.data.ResultSet;
import intradoc.filestore.FileStoreUtils;
import intradoc.filestore.IdcFileDescriptor;
import intradoc.server.ServiceHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class HaysJobCandDetails extends ServiceHandler
{

	public final static String TRACE_NAME = "webapi_jobdetail";

	public void getHaysJobCandDetails() throws ServiceException, DataException, XPathExpressionException
	{
		String databaseproviderName = this.m_currentAction.getParamAt(0);
		String docInfoResultSetName = this.m_currentAction.getParamAt(1);
		String primaryfileResultSetName = this.m_currentAction.getParamAt(2);
		String docInfoInternalResultSetName = this.m_currentAction.getParamAt(4);
		DataResultSet haysContent = new DataResultSet();
		String dDocName = m_binder.getLocal("dDocName");
		String xDescription = "";
		
		DataResultSet DOC_INFO = (DataResultSet) this.m_binder.getResultSet("DOC_INFO");
		if (DOC_INFO != null && DOC_INFO.getNumRows() > 0 && DOC_INFO.getCurrentRowMap().get("xSpecialismId").toString() != null
				&& DOC_INFO.getCurrentRowMap().get("xSpecialismId").toString().length() > 0)
		{
			xDescription = DOC_INFO.getCurrentRowMap().get("xDescription").toString();
		}
		
		SystemUtils.trace(TRACE_NAME, "databaseproviderName: " + databaseproviderName);
		SystemUtils.trace(TRACE_NAME, "docInfoResultSetName: " + docInfoResultSetName);
		SystemUtils.trace(TRACE_NAME, "primaryfileResultSetName: " + primaryfileResultSetName);
		SystemUtils.trace(TRACE_NAME, "dDocName: " + dDocName);
		SystemUtils.trace(TRACE_NAME, "xDescription: " + xDescription);
		String filelocation = getFile();
		SystemUtils.trace(TRACE_NAME, filelocation);
		SystemUtils.trace(TRACE_NAME, "File URL: " + filelocation);
		try
		{
			ArrayList<String> lData = null;
			ResultSet lResultSet = null;
			Document lDoc = null;
			lDoc = parseXML(filelocation);
			lData = getJobCandDesc(lDoc);
			
			String summary = lData.get(2);
			String linkedInSummary = "<p style='white-space:pre-line;'>"+xDescription+"</p>"+summary;
			lData.set(2, linkedInSummary);
			
			ArrayList<String> lHeaderList = new ArrayList<String>();
			lHeaderList.add("Title");
			lHeaderList.add("Summary");
			lHeaderList.add("LinkedInJobSummary");
			lResultSet = createResultSetFromData(lHeaderList, lData);
			haysContent.copy(lResultSet);
			if (m_binder.getLocal("islinkedin") != null && !"".equalsIgnoreCase(m_binder.getLocal("islinkedin"))
					&& ("Y".equalsIgnoreCase(m_binder.getLocal("islinkedin")) || "Yes".equalsIgnoreCase(m_binder.getLocal("islinkedin"))))
			{
				String removeSummaryField[] = { "Summary" };
				haysContent.removeFields(removeSummaryField);
			}
			else
			{
				String removeLinkedInField[] = { "LinkedInJobSummary" };
				haysContent.removeFields(removeLinkedInField);
			}
		}
		catch (ParserConfigurationException e)
		{
			this.m_binder.putLocal("StatusMessage", LocaleUtils.encodeMessage("wwFileNotParse", null));
			this.m_binder.putLocal("StatusCode", "UC012");
		}
		catch (SAXException e)
		{
			this.m_binder.putLocal("StatusMessage", LocaleUtils.encodeMessage("wwInvalidJobDetailParameters", null));
			this.m_binder.putLocal("StatusCode", "UC008");
		}
		catch (IOException e)
		{
			this.m_binder.putLocal("StatusMessage", LocaleUtils.encodeMessage("wwInvalidJobDetailParameters", null));
			this.m_binder.putLocal("StatusCode", "UC008");
		}

		DataResultSet drs = (DataResultSet) this.m_binder.getResultSet(docInfoInternalResultSetName);
		SystemUtils.trace(TRACE_NAME, "HAYS_DOC_INFO ResultSet : " + drs);
		SystemUtils.trace(TRACE_NAME, "HAYS_DATA ResultSet : " + haysContent);

		String removeExtraFields[] = { "dDocType", "dSecurityGroup", "dDocAccount", "dDocType", "dWebExtension", "xRefDomainId", "xRefJobId" };
		drs.removeFields(removeExtraFields);

		
		DataResultSet lLocaleDetails = getJobLocaleAndDomain();
		/* MODIFICATION START: we will be adding SpecialismID as part of our Job API. In case when multiple specialisms are selected this 
		 ** field will have semicolon (;) separated values i.e. SpecialismIDs.*/
		
		String specialismIds = "";
		String specialismDesc = "";
		String industryIds = "";
		String industryDesc = "";
		String isMobileFlag = "N";
		
		if(m_binder.getLocal("isMobile")!=null && m_binder.getLocal("isMobile").equals("Y"))
        {
			isMobileFlag = "Y";
        }
		
		String languageId = m_binder.getLocal("languageId");
		SystemUtils.trace(TRACE_NAME, "languageId: " + languageId);
		if (DOC_INFO != null && DOC_INFO.getNumRows() > 0 && DOC_INFO.getCurrentRowMap().get("xSpecialismId").toString() != null
				&& DOC_INFO.getCurrentRowMap().get("xSpecialismId").toString().length() > 0)
		{
			specialismIds = DOC_INFO.getCurrentRowMap().get("xSpecialismId").toString();
			
			if(specialismIds!=null && !specialismIds.equalsIgnoreCase(""))
			{
				String[] specialismIdList=specialismIds.split(";");
				
				for(int i=0;i<specialismIdList.length;i++)
				{
					if(null!=specialismIdList[i] && !"".equalsIgnoreCase(specialismIdList[i]))
					{
						specialismDesc = specialismDesc+Converter.getLabel(specialismIdList[i], OntologyFacade.getOntology("xCategory"), languageId)+";";
					}
				}
			}
		}
		
		if (DOC_INFO != null && DOC_INFO.getNumRows() > 0 && DOC_INFO.getCurrentRowMap().get("xIndustry").toString() != null
				&& DOC_INFO.getCurrentRowMap().get("xIndustry").toString().length() > 0)
		{
			m_binder.putLocal("metadata","xIndustry");
			SystemUtils.trace(TRACE_NAME, "locale: " + m_binder.getLocal("locale"));
			m_service.executeServiceEx("GET_META_OPTION_LIST", true);
			DataResultSet industryResultSet = (DataResultSet)m_binder.getResultSet("SchemaData");
			
			industryIds = DOC_INFO.getCurrentRowMap().get("xIndustry").toString();
			SystemUtils.trace(TRACE_NAME, "industryResultSet count : " + industryResultSet.getNumRows());
			if(industryIds!=null && !industryIds.equalsIgnoreCase(""))
			{
				String industryIdList[]=industryIds.split(";");
				
				for(int i=0;i<industryIdList.length;i++)
				{
					if(null!=industryIdList[i] && !"".equalsIgnoreCase(industryIdList[i]) && industryResultSet != null && industryResultSet.getNumRows() > 0)
					{
						for(int j=0;j<industryResultSet.getNumRows();j++)
						{
							if (industryIdList[i].equals(industryResultSet.getRowAsList(j).get(0)))
								{
									industryDesc = industryDesc+industryResultSet.getRowValues(j).get(1)+";";
									
								}
						}
					}
				}
			}
		}
		SystemUtils.trace(TRACE_NAME, "industryIds : " + industryIds);
		SystemUtils.trace(TRACE_NAME, "industryDesc : " + industryDesc);
		SystemUtils.trace(TRACE_NAME, "specialismIds : " + specialismIds);
		SystemUtils.trace(TRACE_NAME, "specialismDesc : " + specialismDesc);
		Vector<FieldInfo> xSpecialismId = new Vector<FieldInfo>();
		FieldInfo xSpecialismIdInfo = new FieldInfo();
		xSpecialismIdInfo.m_name = "xSpecialismId";
		xSpecialismIdInfo.m_type = 6;
		xSpecialismId.add(xSpecialismIdInfo);
		drs.appendFields(xSpecialismId);
		
		Vector drsCurrentRow = null;
		drsCurrentRow = drs.getCurrentRowValues();
		drsCurrentRow.set(drs.getNumFields() - 1, specialismIds);
		
		Vector<FieldInfo> xSpecialismLabel = new Vector<FieldInfo>();
		FieldInfo xSpecialismLabelInfo = new FieldInfo();
		xSpecialismLabelInfo.m_name = "xSpecialismLabel";
		xSpecialismLabelInfo.m_type = 6;
		xSpecialismLabel.add(xSpecialismLabelInfo);
		drs.appendFields(xSpecialismLabel);
		
		drsCurrentRow = drs.getCurrentRowValues();
		drsCurrentRow.set(drs.getNumFields() - 1, specialismDesc);
		
		Vector<FieldInfo> xIndustryLabel = new Vector<FieldInfo>();
		FieldInfo xIndustryLabelInfo = new FieldInfo();
		xIndustryLabelInfo.m_name = "xIndustryLabel";
		xIndustryLabelInfo.m_type = 6;
		xIndustryLabel.add(xIndustryLabelInfo);
		drs.appendFields(xIndustryLabel);
		
		drsCurrentRow = drs.getCurrentRowValues();
		drsCurrentRow.set(drs.getNumFields() - 1, industryDesc);
		
		/* MODIFICATION END.*/

		if (drs != null && drs.getNumRows() > 0)
		{
			this.m_binder.putLocal("StatusMessage", LocaleUtils.encodeMessage("wwWebApiOKMsg", null));
			this.m_binder.putLocal("StatusCode", "UC000");
			m_binder.addResultSet(docInfoResultSetName, drs);
		}

		if (haysContent != null && haysContent.getNumRows() > 0)
		{
			m_binder.addResultSet(primaryfileResultSetName, haysContent);
		}
		m_binder.getLocalData().put("isMobile",isMobileFlag);
		m_binder.removeResultSet("SchemaData");
		m_binder.addResultSet("LOCALE_DETAILS", lLocaleDetails);
		SystemUtils.trace(TRACE_NAME, "Fetched locale details as per job's locale successfully.");
	}

	public ArrayList<String> getJobCandDesc(Document lDocument) throws XPathExpressionException
	{
		XPath xpath = XPathFactory.newInstance().newXPath();
		xpath.setNamespaceContext(new NamespaceResolver());
		ArrayList<String> lresult = new ArrayList<String>();
		XPathExpression expr = xpath.compile("//wcm:root/wcm:element[@name='Title']/text()");
		lresult.add(expr.evaluate(lDocument, XPathConstants.STRING).toString());
		expr = xpath.compile("//wcm:root/wcm:element[@name='Summary']/text()");
		lresult.add(expr.evaluate(lDocument, XPathConstants.STRING).toString());
		expr = xpath.compile("//wcm:root/wcm:element[@name='LinkedInJobSummary']/text()");
		lresult.add(expr.evaluate(lDocument, XPathConstants.STRING).toString());
		return lresult;
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

	private DataResultSet getJobLocaleAndDomain() throws ServiceException, DataException
	{
		String providerName = "SystemDatabase";
		String lLocale = null;
		String lDomainId = null;
		DataResultSet lLocaleDetails = null;
		DataResultSet lDocInfo = (DataResultSet) m_binder.getResultSet("DOC_INFO");
		if (lDocInfo != null && lDocInfo.getNumRows() > 0)
		{
			lLocale = lDocInfo.getStringValueByName("xLocale");
			SystemUtils.trace(TRACE_NAME, "Locale of job is : " + lLocale);
		}
		if (isNotNull(lLocale))
		{
			m_binder.putLocal("sitelocale", lLocale.trim());
			lLocaleDetails = HaysWebApiUtils.executeHaysProviderQuery(providerName, "QGetLocaleDetails", m_binder);
			if (lLocaleDetails != null && lLocaleDetails.getNumRows() > 0)
			{
				lDomainId = lLocaleDetails.getStringValueByName("DOMAINID");
				SystemUtils.trace(TRACE_NAME, "Live website domain id is : " + lDomainId);
				m_binder.putLocal("languageId", lLocaleDetails.getStringValueByName("LANGUAGEID"));
			}
			if (!isNotNull(lDomainId))
			{
				lLocaleDetails = HaysWebApiUtils.executeHaysProviderQuery(providerName, "QGetNonLiveSitesDomainId", m_binder);
				if (lLocaleDetails != null && lLocaleDetails.getNumRows() > 0)
				{
					lDomainId = lLocaleDetails.getStringValueByName("DOMAINID");
					lLocale = lLocaleDetails.getStringValueByName("SITELOCALE");
					SystemUtils.trace(TRACE_NAME, "Non-Live website domain id is : " + lDomainId);
					m_binder.putLocal("languageId", lLocaleDetails.getStringValueByName("LANGUAGEID"));
				}
			}
		}
		lLocaleDetails = new DataResultSet(new String[] { "SITE_LOCALE", "DOMAIN_ID" });
		lLocaleDetails.addRowWithList(Arrays.asList(new String[] { lLocale, lDomainId }));
		//override languageId as not correct above
		String localeLang = lLocale;
		if( localeLang != null) {
			Matcher matcher = Pattern.compile("(\\w+)[-_](\\w+)").matcher(localeLang);
		    if (matcher.find() && matcher.groupCount() > 1) {
		    	localeLang = matcher.group(1);
		    }
		} else {
			localeLang = LocaleResources.getSystemLocale().m_languageId;
		}
		m_binder.putLocal("languageId",localeLang);
		//override languageId as not correct above
		m_binder.putLocal("locale",lLocale);
//		m_binder.addResultSet("LOCALE_DETAILS", lLocaleDetails);
		return lLocaleDetails;
	}
}
