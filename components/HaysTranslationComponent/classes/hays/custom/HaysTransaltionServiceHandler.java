package hays.custom;

import java.io.File;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sitestudio.SSCommon;
import hays.custom.multilingual.HaysWebSite;
import intradoc.common.FileUtils;
import intradoc.common.LocaleResources;
import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.data.DataBinder;
import intradoc.data.DataException;
import intradoc.data.DataResultSet;
import intradoc.data.ResultSet;
import intradoc.server.LegacyDirectoryLocator;
import intradoc.server.ServiceHandler;
import intradoc.shared.SharedObjects;



public class HaysTransaltionServiceHandler extends ServiceHandler{
	
	public static final Pattern LOCALE_REGEX = Pattern.compile("(.+)[-_](.+)");
	
	public void setTranslateRegionAssiciationDetails ()throws ServiceException,DataException
	  {
		String ssDocName=m_binder.getLocal("ssDocName");
		String regionName=m_binder.getLocal("region");
		if(ssDocName != null && ssDocName.length() >0){
			DataBinder CacheBinder = new DataBinder();
			SSCommon.getDocInfo(ssDocName, this.m_service, CacheBinder, false, null);
        	ResultSet docInfoRs = CacheBinder.getResultSet("DOC_INFO");
        	if(docInfoRs != null && docInfoRs.isRowPresent()){
               	DataResultSet docInfoDrs = new DataResultSet();
            	docInfoDrs.copy(docInfoRs);
             	String xWebsiteSection = docInfoDrs.getStringValueByName("xWebsiteSection");
            	if(xWebsiteSection != null && xWebsiteSection.length()>0){
            		String siteId = xWebsiteSection.substring(0,xWebsiteSection.indexOf(":"));
            		String nodeId = xWebsiteSection.substring(xWebsiteSection.indexOf(":")+1);
            		m_binder.putLocal("siteId", siteId);
            		m_binder.putLocal("nodeId", nodeId);
            		m_binder.putLocal("primaryUrl", ssDocName);
            		m_binder.putLocal("region", regionName);
         		 
            		SystemUtils.trace("Translation", "siteId "+siteId);
            		SystemUtils.trace("Translation", "nodeId "+nodeId);
            		SystemUtils.trace("Translation", "Region Name =  "+regionName);
            		SystemUtils.trace("Translation", "ssDocName "+ssDocName);

            	}else{
            		throw new DataException("Can not perform region association. Website Section not defined for "+ssDocName);
            	}
        	}else{
        		throw new DataException("Can not perform region association. Can not find Doc Info for "+ssDocName);
        	}
 
		}else{
			throw new ServiceException("Can not perform region association. ssDocName is not defined");
		}
	  }
	
	
	public void getTransaltionDetails()throws DataException{
		SystemUtils.trace("Translation", "getTransaltionDetails() : Called");

		String siteId = m_binder.getLocal("siteId");
		String dDocName = m_binder.getLocal("dDocName");
		String wcmPopId = m_binder.getLocal("WCMPopupId");
		String regionId = m_binder.getLocal("region");
		String languageCode,countryCode,transDocName = null;
		
		String editTransService = m_currentAction.getParamAt(0);
		String chekinFormService = m_currentAction.getParamAt(1);
	
		SystemUtils.trace("Translation", "dDocName "+dDocName);
		SystemUtils.trace("Translation", "Region Name  "+regionId);
		
		if(dDocName == null || dDocName.length() <=0 || siteId == null || siteId.length() <=0){
			throw new DataException("SiteIdor dDocName not Found");
		}else{
			HashMap<String, HaysWebSite> websitesMap = (HashMap<String, HaysWebSite>)SharedObjects.getObject("Multiling", "WebsitesMap");
			HaysWebSite website = websitesMap.get(siteId);
			if(website!=null){
				String siteLocale = website.haysLocaleId;
				SystemUtils.trace("Translation", "siteLocale "+siteLocale);
				Matcher matcher = LOCALE_REGEX.matcher(siteLocale);
			    if (matcher.find() && matcher.groupCount() > 1) {
			    	m_binder.putLocal("siteLocale", siteLocale);
			    	languageCode = matcher.group(1);
			    	countryCode = matcher.group(2);
			    	transDocName = dDocName+languageCode.toUpperCase();
			    	m_binder.putLocal("contentId", transDocName);
			    	ResultSet rs = m_workspace.createResultSet("QIsTranslationExist", m_binder);
			    	if(rs != null && rs.first()){
			    		SystemUtils.trace("Translation", "transaltion exist");
			    		m_binder.putLocal("dDocName", transDocName);
			    		m_binder.putLocal("IdcService", editTransService);
			    	}else{
			    		m_binder.putLocal("dDocName", dDocName);
			    		m_binder.putLocal("IdcService", chekinFormService);
			    	}
			    	
			    }else{
			    	throw new DataException("SiteLocale "+siteLocale+" is not correct");
			    }
			}else{
				throw new DataException("No website found for siteId "+siteId);
			}
		}
	}
	
	public void setDefaultTranslationValues()throws DataException,ServiceException{
		String siteId = m_binder.getLocal("siteId");
		String siteLocale = m_binder.getLocal("siteLocale");
		String dDocName = m_binder.getLocal("dDocName");
		String wcmPopId = m_binder.getLocal("WCMPopupId");
		String nodeId = m_binder.getLocal("nodeId");
		String languageCode="en",countryCode="GB",transDocName = null;
		
		if(siteLocale == null || siteLocale.length() <= 0 || dDocName == null || dDocName.length() <=0){
			throw new DataException("SiteLocale or dDocName not Found");
		}else{
				Matcher matcher = LOCALE_REGEX.matcher(siteLocale);
			    if (matcher.find() && matcher.groupCount() > 1) {
			    	m_binder.putLocal("siteLocale", siteLocale);
			    	languageCode = matcher.group(1);
			    	countryCode = matcher.group(2);
			    	transDocName = dDocName+languageCode.toUpperCase();
			    }
				
	        	DataBinder CacheBinder = new DataBinder();
	        	SSCommon.getDocInfo(dDocName, this.m_service, CacheBinder, false, null);
	        	ResultSet docInfoRs = CacheBinder.getResultSet("DOC_INFO");
	        	
	        	if(docInfoRs != null && docInfoRs.first()){
	               	DataResultSet docInfoDrs = new DataResultSet();
	            	docInfoDrs.copy(docInfoRs);
	            	
	            	CacheBinder.putLocal("dDocName", dDocName);
	            	CacheBinder.addResultSet("DOC_INFO", docInfoDrs);
	            	
	            	String xWebsiteSection = docInfoDrs.getStringValueByName("xWebsiteSection");
	            	if(xWebsiteSection==null || xWebsiteSection.length() <=0){
	            		xWebsiteSection = siteId + ":" + nodeId;
	            	}else{
	            		xWebsiteSection = siteId + xWebsiteSection.substring(xWebsiteSection.indexOf(":"));
	            	}
	            	
	    			
	    			String fileName = LegacyDirectoryLocator.computeVaultFileName(CacheBinder);
	    			String vaultFilePath = LegacyDirectoryLocator.computeVaultPath(fileName, CacheBinder);
	    			SystemUtils.trace("Translation", "fileName "+fileName);
	    			SystemUtils.trace("Translation", "filePath "+vaultFilePath);
	    			
		            String tempDir = DataBinder.getTemporaryDirectory();
		            String tempFilePath = tempDir + fileName;
	          
		            FileUtils.copyFile(vaultFilePath, tempFilePath);
		            
		            
		            m_binder.putLocal("dDocName", transDocName);
		            m_binder.putLocal("dDocAccount", docInfoDrs.getStringValueByName("dDocAccount"));
		            m_binder.putLocal("xWebsites", siteId);
		            m_binder.putLocal("xWebsiteSection", xWebsiteSection);
		            m_binder.putLocal("LanguageCode", languageCode);
		            m_binder.putLocal("CountryCode", countryCode);
		            m_binder.putLocal("xRegionDefinition", docInfoDrs.getStringValueByName("xRegionDefinition"));
		            m_binder.putLocal("xWebsiteObjectType", "Data File");
		            m_binder.putLocal("xLocale", siteLocale);
		            m_binder.putLocal("xCategory", docInfoDrs.getStringValueByName("xCategory"));
		            m_binder.putLocal("xIndustry", docInfoDrs.getStringValueByName("xIndustry"));
		            m_binder.putLocal("dpTriggerValue", docInfoDrs.getStringValueByName("dDocType"));
		            m_binder.putLocal("doMasterFileCopy", "1");
		            m_binder.putLocal("primaryFile:path", tempFilePath);
		            m_binder.putLocal("primaryFile", fileName);
		            m_binder.putLocal("ssDefaultDocumentToken", "SSNonEmptyContributorDataFile");
		            m_binder.putLocal("suppressAlternateFile", "1");
		            m_binder.putLocal("AllowPrimaryMetaFile", "0");
		            m_binder.putLocal("coreContentOnly", "1");
		            m_binder.putLocal("useHaysCheckin", "1");
	    			
	        	}else{
	        		throw new DataException("No information found for contentID "+dDocName);
	        	}
 
		}
	}
}
