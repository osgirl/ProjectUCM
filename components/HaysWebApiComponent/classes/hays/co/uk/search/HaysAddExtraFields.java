package hays.co.uk.search;

import static hays.com.commonutils.HaysWebApiUtils.*;
import intradoc.common.LocaleUtils;
import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.data.DataBinder;
import intradoc.data.DataException;
import intradoc.data.DataResultSet;
import intradoc.data.FieldInfo;
import intradoc.data.ResultSet;
import intradoc.data.ResultSetUtils;
import intradoc.data.Workspace;
import intradoc.provider.Provider;
import intradoc.provider.Providers;
import intradoc.server.ServiceHandler;
import intradoc.shared.SharedObjects;
import java.io.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import intradoc.provider.Provider;
import intradoc.provider.Providers;

public class HaysAddExtraFields extends ServiceHandler {
	
	public static final String RESULTSET_SEARCHRESULTS 	= "SearchResults";
	public static final String RESULTSET_HAYSDOCINFO 	= "HAYS_DOC_INFO";
	public static final int TOP_LEVEL= 1;
	public static final int BOTTOM_LEVEL=6;
	public static final String HAYS_LOCATION_FORMAT="xHaysLocation";
	private static final String TRACE_NAME = "webapi_jobdetail";
	
	public void SponsoredJobImageUrlWU() throws DataException, ServiceException {
		

		String filelocation=null;
		String webLayoutlUrl=SharedObjects.getEnvironmentValue("WeblayoutDir");			
		String securityGroup="";
		String docAccount="";
		String doctype="";
		String fileExtension="";
		String sponsoredEmployerDocName="";
		ArrayList<String> sponsoredImageUrls = new ArrayList<String>();	
		ArrayList<String> tempSponsoredImageUrls = new ArrayList<String>();	
		DataResultSet sponsoreImageResultSet = null;
		String sEmployerDocNameList="";

		java.util.Date date= new java.util.Date();
		Timestamp currentTimestamp = new java.sql.Timestamp(date.getTime());	
		SystemUtils.trace("webapi_jobdetailWU", "function SponsoredJobImageUrlWU Start Time: "+currentTimestamp);
		
		String dDocName=m_binder.getLocal("dDocName");
		SystemUtils.trace("webapi_jobdetailWU", "Inside SponsoredJobImageUrlWU dDocname: " +dDocName);
		
		String providerName = this.m_currentAction.getParamAt(0);
	     SystemUtils.trace("webapi_jobdetailWU", "Inside SponsoredJobImageUrlWU Provider Name: " +providerName);

	     String resultSetName = this.m_currentAction.getParamAt(1);
	     SystemUtils.trace("webapi_jobdetailWU", "Inside SponsoredJobImageUrlWU Result Set Name: " +resultSetName);

	     String queryName = this.m_currentAction.getParamAt(2);
	     SystemUtils.trace("webapi_jobdetailWU", "Inside SponsoredJobImageUrlWU Query Name: " +queryName);
	     sEmployerDocNameList=m_binder.getLocal("dDocName");
		SystemUtils.trace("webapi_jobdetailWU","Final SponsoredEmployers value ="+ sEmployerDocNameList);	       
	        
	        
			this.m_binder.putLocal("SEmployerDocNameList", sEmployerDocNameList); 
			 
	     Provider p = Providers.getProvider(providerName);
	     if ((p == null) || (!p.isProviderOfType("database")))
		    {
		      throw new ServiceException("You the provider '" + providerName + 
		        "' is not a valid provider of type 'database'.");
		    }
	    	
			Workspace databaseServerWs = (Workspace)p.getProvider();
			ResultSet rs = databaseServerWs.createResultSet(queryName, m_binder);
			DataResultSet drs = new DataResultSet(); 
			drs.copy(rs);
			
			SystemUtils.trace("webapi_jobdetailWU","SponsoredEmployers dRSet ="+ drs);
			
			sponsoredImageUrls.add("xSponsored"); 
        	sponsoredImageUrls.add("SponsoredJobsImage");
        	
			if (drs.getNumRows()>0)
        	{
        		do{
    	        	
            		 
            		//Define variables for creating content file path
        			webLayoutlUrl=SharedObjects.getEnvironmentValue("WeblayoutDir");
        			SystemUtils.trace("webapi_jobdetailWU", "Inside SponsoredJobImageUrlWU webLayoutlUrl Name: " +webLayoutlUrl);
        			securityGroup=drs.getStringValueByName("dSecurityGroup");
        			SystemUtils.trace("webapi_jobdetailWU", "Inside SponsoredJobImageUrlWU securityGroup Name: " +securityGroup);
        			docAccount=drs.getStringValueByName("dDocAccount");
        			SystemUtils.trace("webapi_jobdetailWU", "Inside SponsoredJobImageUrlWU docAccount Name: " +docAccount);
        			doctype=drs.getStringValueByName("dDocType");
        			SystemUtils.trace("webapi_jobdetailWU", "Inside SponsoredJobImageUrlWU doctype Name: " +doctype);
        			fileExtension=drs.getStringValueByName("dWebExtension");    			   			
        			SystemUtils.trace("webapi_jobdetailWU", "Inside SponsoredJobImageUrlWU fileExtension Name: " +fileExtension);
        			sponsoredEmployerDocName=drs.getStringValueByName("dDocName");
        			SystemUtils.trace("webapi_jobdetailWU", "Inside SponsoredJobImageUrlWU sponsoredEmployerDocName Name: " +sponsoredEmployerDocName);
        			
        			if (docAccount != null && docAccount.length() > 0)
        			{
        				SystemUtils.trace("webapi_jobdetailWU", "Inside SponsoredJobImageUrlWU docAccount 1st If: " );
        				if(docAccount.indexOf("/")>0)
        				{
        					SystemUtils.trace("webapi_jobdetailWU", "Inside SponsoredJobImageUrlWU docAccount 2nd If: " );
        					docAccount=docAccount.replaceAll("/", "/@");
        					docAccount="@"+docAccount;						
        				}
        				else
        				{
        					SystemUtils.trace("webapi_jobdetailWU", "Inside SponsoredJobImageUrlWU docAccount 1st else: " );
        					docAccount="@"+docAccount;
        				}
        				
        				filelocation="groups/"+securityGroup+"/"+docAccount+"/documents/"+doctype+"/"+sponsoredEmployerDocName+"."+fileExtension;
        			}
        			
        			else
        			{
        				SystemUtils.trace("webapi_jobdetailWU", "Inside SponsoredJobImageUrlWU docAccount main else: " );
        				filelocation="groups/"+securityGroup+"/documents/"+doctype+"/"+sponsoredEmployerDocName+"."+fileExtension;
        			}
        			
        			filelocation=filelocation.toLowerCase();
        			filelocation=webLayoutlUrl+filelocation;
        			SystemUtils.trace("webapi_jobdetailWU", "File URL: "+filelocation);
        			
        			try
        			{
        				
        				DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        	            DocumentBuilder docBuilder = null;
        				docBuilder = docBuilderFactory.newDocumentBuilder();
        				Document doc = null;
        				doc = docBuilder.parse (filelocation);				
        	            doc.getDocumentElement().normalize();
        	
        	            //SystemUtils.trace("webapi_search", "Root element of the doc is: "+doc.getDocumentElement().getNodeName());
        	            
        	            NodeList ndlst = doc.getElementsByTagName("wcm:element");	
        	           
        	            //SystemUtils.trace("webapi_search", "Total no of doc elements : "+totalElements);            
        	            //String imagePath= null;
        	
        	            for(int s=0; s<ndlst.getLength() ; s++){
        	            	
        	            	Node firstPersonNode = ndlst.item(s);	            	
        	            	if(firstPersonNode.getNodeType() == Node.ELEMENT_NODE){
        	
        	            	Element firstElement = (Element)firstPersonNode;
        	            	//SystemUtils.trace("webapi_search", "XML element Name : " +firstElement.getAttribute("name").trim());
        	            	  	
        	            	Node nd = firstElement.getFirstChild();
        	            	if("Image".equalsIgnoreCase(firstElement.getAttribute("name").trim()) && nd.getNodeValue().toString()!=null)
        	            	{
        	            		//imagePath= null;
        	            		          	
            	            	String elementContent=nd.getNodeValue().trim();
            	            	
            	            	int imageSrcStartIndex=elementContent.indexOf("wcmUrl");
            	            	int imageSrcEndIndex=elementContent.indexOf("')");
            	            	//getSiteId();
            	            	String imagePath=elementContent.substring(imageSrcStartIndex+19, imageSrcEndIndex);
            	            	//imagePath=resolveIDOCFunctions(elementContent.substring(imageSrcStartIndex-5, imageSrcEndIndex+5));
            	            	
            	            	tempSponsoredImageUrls.add(sponsoredEmployerDocName);
            	            	tempSponsoredImageUrls.add(imagePath);
        	            	}   	            	 	            	
        	                    		
        	        		
        					}
        					
        				}
                    
        			}
        			 			
        			
        			catch (ParserConfigurationException e)
        			{
        				// TODO Auto-generated catch block
        				this.m_binder.putLocal("StatusMessage", LocaleUtils.encodeMessage("wwFileNotParse", null));
        				this.m_binder.putLocal("StatusCode", "UC012");
        				throw new ServiceException(LocaleUtils.encodeMessage("wwFileNotParse", null));
        			}
        			catch (SAXException e)
        			{
        				// TODO Auto-generated catch block				
        				this.m_binder.putLocal("StatusMessage", LocaleUtils.encodeMessage("wwFileNotFormedProperly", null));
        				this.m_binder.putLocal("StatusCode", "UC012");			
        				throw new ServiceException(LocaleUtils.encodeMessage("wwFileNotFormedProperly", null));
        			} 
        			catch (IOException e)
        			{
        				// TODO Auto-generated catch block				
        				this.m_binder.putLocal("StatusMessage", LocaleUtils.encodeMessage("wwFileNotFound", null));
        				this.m_binder.putLocal("StatusCode", "UC011");			
        				throw new ServiceException(LocaleUtils.encodeMessage("wwFileNotFound", null));
        			}    			
        		    		
    	        }while(drs.next());
    			
    		  
    			SystemUtils.trace("webapi_jobdetailWU", "Sponsored Employer ImageUrls List1 : " +sponsoredImageUrls);	
    			SystemUtils.trace("webapi_jobdetailWU", "Sponsored Employers ImageUrls List2: " +tempSponsoredImageUrls);
    			
    			//m_binder.addResultSet(resultSetName, tempSponsoredImageUrls);
    				
        	}
			
			
			sponsoreImageResultSet=(DataResultSet)createResultSetFromData(sponsoredImageUrls, tempSponsoredImageUrls);			
			SystemUtils.trace("webapi_search", "SponsoredImage ResultSet : " +sponsoreImageResultSet);		
			m_binder.addResultSet(resultSetName, sponsoreImageResultSet);
			//this.m_binder.removeResultSet("HAYS_SPONSORED_IMAGE_URL");
			this.m_binder.removeLocal("dDocName");
			this.m_binder.removeResultSet("DOC_INFO");
			//this.m_binder.removeLocal("page_name");
			databaseServerWs.releaseConnection();
	     
	}
	
	//This function is used for adding SponsoredJob ImageUrl in ResultSet SearchResults
	public void addSponsoredJobImageUrl() throws DataException, ServiceException {
		
		java.util.Date date= new java.util.Date();
		Timestamp currentTimestamp = new java.sql.Timestamp(date.getTime());			
		SystemUtils.trace("webapi_jobdetail", "function addSponsoredJobImageUrl Start Time: "+currentTimestamp);
		
		
		StringBuffer sponsoredEmployers = new StringBuffer(100);
		String sEmployerDocNameList="";
		String filelocation=null;
		String webLayoutlUrl=SharedObjects.getEnvironmentValue("WeblayoutDir");			
		String securityGroup="";
		String docAccount="";
		String doctype="";
		String fileExtension="";
		String sponsoredEmployerDocName="";
		ArrayList<String> sponsoredImageUrls = new ArrayList<String>();	
		ArrayList<String> tempSponsoredImageUrls = new ArrayList<String>();	
		DataResultSet sponsoreImageResultSet = null;
		String flag=m_binder.getLocal("isjobDetail");
		DataBinder docParams = new DataBinder();
		Vector vSearchresults=null;
		int fieldIndex=0;
		String documentName="", websites ="";
		SystemUtils.trace("webapi_search", "isMobile: "+ m_binder.getLocal("isMobile"));	
		SystemUtils.trace("webapi_search", "flag : "+ flag);	
		
		if(m_binder.getLocal("isMobile")!=null && m_binder.getLocal("isMobile").equals("Y"))
        {
			DataResultSet drsSearchResults =new DataResultSet();
			if(flag!=null && ("Y").equalsIgnoreCase(flag))
			{
				drsSearchResults=(DataResultSet)super.m_binder.getResultSet(RESULTSET_HAYSDOCINFO);
			}
			else
			{
				drsSearchResults=(DataResultSet)super.m_binder.getResultSet(RESULTSET_SEARCHRESULTS);
			}
			if(drsSearchResults!=null)
			{				
			
				if(drsSearchResults.getNumRows()>0)
		        {
					SystemUtils.trace("webapi_search", "ResultSet SearchResults total rows: "+ drsSearchResults.getNumRows());			
					fieldIndex = drsSearchResults.getNumFields();
					Vector<FieldInfo> sponsoredImage = new Vector<FieldInfo>();
			        FieldInfo SporedImageFieldInfo = new FieldInfo();
			        SporedImageFieldInfo.m_name="SponsoredJobsImage";
			        SporedImageFieldInfo.m_type = 6;
			        sponsoredImage.add(SporedImageFieldInfo);
			        drsSearchResults.appendFields(sponsoredImage);
			        
			        Vector<FieldInfo> micrositeURL = new Vector<FieldInfo>();
			        FieldInfo micrositeURLFieldInfo = new FieldInfo();
			        micrositeURLFieldInfo.m_name="Microsite_URL";
			        micrositeURLFieldInfo.m_type = 6;
			        micrositeURL.add(micrositeURLFieldInfo);
			        drsSearchResults.appendFields(micrositeURL);
			        
			        Vector<FieldInfo> locationLongLat = new Vector<FieldInfo>();
			        FieldInfo locationLongLatInfo = new FieldInfo();
			        locationLongLatInfo.m_name="HaysLocations";
			        locationLongLatInfo.m_type = 6;
			        locationLongLat.add(locationLongLatInfo);
			        drsSearchResults.appendFields(locationLongLat);	        
			        
			        String sponsoredJobImageUrl="", micrositeUrl="";
					do{
		        		int currentRowIndex =  drsSearchResults.getCurrentRow();
		        		SystemUtils.trace("webapi_search", "current row : " +currentRowIndex);
		        		documentName = drsSearchResults.getCurrentRowMap().get("dDocName").toString().trim();  
		        		websites = (String)drsSearchResults.getCurrentRowMap().get("xWebsites");
		        		
		        		SystemUtils.trace("webapi_search", "dDocname : " +documentName+" xWebsites"+websites);
			        	String HaysLocationsByLevel="";
		        		String locationId = drsSearchResults.getCurrentRowMap().get("xLocation").toString().trim();        		
		        		SystemUtils.trace("webapi_search", "Job Location ID and Description : " +locationId+"##"+drsSearchResults.getCurrentRowMap().get("xLocationDescription").toString());	
		       		    if(locationId!=null && locationId.length()>0)
		       		    {    
		       		    	Object retrieved=null;
		       		    	for( int i = BOTTOM_LEVEL; i >= TOP_LEVEL; i--) {
		       		    		HaysLocationsByLevel=HAYS_LOCATION_FORMAT+i;
		       		    		SystemUtils.trace("webapi_search", "HaysLocationsByLevel : " +HaysLocationsByLevel);
		       		    		retrieved = drsSearchResults.getCurrentRowMap().get(HaysLocationsByLevel);
		       	       		    if(retrieved != null && retrieved.toString().length()>0)
		       					{
		       	       		    	SystemUtils.trace("webapi_search", "HaysLocation By Level : " +drsSearchResults.getCurrentRowMap().get(HaysLocationsByLevel).toString());	
		       	       		    	vSearchresults = drsSearchResults.getCurrentRowValues();	   
		       	       		    	vSearchresults.set(fieldIndex+2, drsSearchResults.getCurrentRowMap().get(HaysLocationsByLevel).toString()); 
		       						break;
		       					}
		       				}
		        		}
		        		 
		        		if(drsSearchResults.getCurrentRowMap().get("xSponsored").toString()!=null && drsSearchResults.getCurrentRowMap().get("xSponsored").toString().length()>0)
		        		{
		        			sponsoredEmployers=sponsoredEmployers.append("','").append(drsSearchResults.getCurrentRowMap().get("xSponsored").toString());
		        		}        		
		        		
			        }while(drsSearchResults.next());
					
					SystemUtils.trace("webapi_search","SponsoredEmployers DocName list ="+ sponsoredEmployers);				
					
				if(sponsoredEmployers!=null && sponsoredEmployers.length()>0)
				 {
					
					sEmployerDocNameList=sponsoredEmployers.substring(3,sponsoredEmployers.length());
					SystemUtils.trace("webapi_search","Final SponsoredEmployers DocName list ="+ sEmployerDocNameList);	       
			        
			        
			        docParams.putLocal("SEmployerDocNameList", sEmployerDocNameList); 
			        
			        Provider p = Providers.getProvider("SystemDatabase");	    	    
			        Workspace databaseServerWs = (Workspace)p.getProvider();
					ResultSet sponsoredEmployersrset = databaseServerWs.createResultSet("QGetSponsoredEmployersDetails", docParams);
					DataResultSet sponsoredEmployersdrset=new DataResultSet(); 
					sponsoredEmployersdrset.copy(sponsoredEmployersrset);
					
					SystemUtils.trace("webapi_search","SponsoredEmployers dRSet ="+ sponsoredEmployersdrset);
					
					sponsoredImageUrls.add("xSponsored"); 
		        	sponsoredImageUrls.add("SponsoredJobsImage");
		        	sponsoredImageUrls.add("Microsite_URL");
		        	
		        	if (sponsoredEmployersdrset.getNumRows()>0)
		        	{
		        		do{
		            		//Define variables for creating content file path
		        			webLayoutlUrl=SharedObjects.getEnvironmentValue("WeblayoutDir");			
		        			securityGroup=sponsoredEmployersdrset.getStringValueByName("dSecurityGroup");
		        			docAccount=sponsoredEmployersdrset.getStringValueByName("dDocAccount");
		        			doctype=sponsoredEmployersdrset.getStringValueByName("dDocType");
		        			fileExtension=sponsoredEmployersdrset.getStringValueByName("dWebExtension");    			   			
		        			sponsoredEmployerDocName=sponsoredEmployersdrset.getStringValueByName("dDocName");
		        			
		        			if (docAccount != null && docAccount.length() > 0)
		        			{
		        				if(docAccount.indexOf("/")>0)
		        				{
		        					docAccount=docAccount.replaceAll("/", "/@");
		        					docAccount="@"+docAccount;						
		        				}
		        				else
		        				{
		        					docAccount="@"+docAccount;
		        				}
		        				filelocation="groups/"+securityGroup+"/"+docAccount+"/documents/"+doctype+"/"+sponsoredEmployerDocName+"."+fileExtension;
		        			}
		        			else
		        			{
		        				filelocation="groups/"+securityGroup+"/documents/"+doctype+"/"+sponsoredEmployerDocName+"."+fileExtension;
		        			}
		        			filelocation=filelocation.toLowerCase();
		        			filelocation=webLayoutlUrl+filelocation;
		        			SystemUtils.trace("webapi_search", "File URL: "+filelocation);
		        			
		        			try
		        			{
		        				DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		        	            DocumentBuilder docBuilder = null;
		        				docBuilder = docBuilderFactory.newDocumentBuilder();
		        				Document doc = null;
		        				doc = docBuilder.parse (filelocation);				
		        	            doc.getDocumentElement().normalize();
		        	
		        	            //SystemUtils.trace("webapi_search", "Root element of the doc is: "+doc.getDocumentElement().getNodeName());
		        	            
		        	            NodeList ndlst = doc.getElementsByTagName("wcm:element");	
		        	           
		        	            //SystemUtils.trace("webapi_search", "Total no of doc elements : "+totalElements);            
		        	            //String imagePath= null;

		        	            String imagePath = "", micURL="";
		        	            for(int s=0; s<ndlst.getLength() ; s++){
		        	            	Node firstPersonNode = ndlst.item(s);	            	
		        	            	if(firstPersonNode.getNodeType() == Node.ELEMENT_NODE){
			        	            	Element firstElement = (Element)firstPersonNode;
			        	            	//SystemUtils.trace("webapi_search", "XML element Name : " +firstElement.getAttribute("name").trim());
			        	            	Node nd = firstElement.getFirstChild();
			        	            	if("Image".equalsIgnoreCase(firstElement.getAttribute("name").trim()) && nd!=null)
			        	            	{
			        	            		//imagePath= null;
			            	            	String elementContent=nd.getNodeValue().trim();
			            	            	int imageSrcStartIndex=elementContent.indexOf("wcmUrl");
			            	            	int imageSrcEndIndex=elementContent.indexOf("')");
			            	            	//getSiteId(documentName, websites);
			            	            	imagePath=elementContent.substring(imageSrcStartIndex+19, imageSrcEndIndex);
			            	            	//imagePath=resolveIDOCFunctions(elementContent.substring(imageSrcStartIndex-5, imageSrcEndIndex+5), websites);
			        	            	}  
			        	            	if("Microsite_URL".equalsIgnoreCase(firstElement.getAttribute("name").trim()))
			        	            	{
			        	            		if(nd !=null){
			        	            			micURL=nd.getNodeValue();
			        	            		}
			        	            	}  
		        					}
		        				}
		        	            tempSponsoredImageUrls.add(sponsoredEmployerDocName);
            	            	tempSponsoredImageUrls.add(imagePath);
            	            	tempSponsoredImageUrls.add(micURL);
		        			}
		        			catch (ParserConfigurationException e)
		        			{
		        				this.m_binder.putLocal("StatusMessage", LocaleUtils.encodeMessage("wwFileNotParse", null));
		        				this.m_binder.putLocal("StatusCode", "UC012");
		        				throw new ServiceException(LocaleUtils.encodeMessage("wwFileNotParse", null));
		        			}
		        			catch (SAXException e)
		        			{
		        				this.m_binder.putLocal("StatusMessage", LocaleUtils.encodeMessage("wwFileNotFormedProperly", null));
		        				this.m_binder.putLocal("StatusCode", "UC012");			
		        				throw new ServiceException(LocaleUtils.encodeMessage("wwFileNotFormedProperly", null));
		        			} 
		        			catch (IOException e)
		        			{
		        				this.m_binder.putLocal("StatusMessage", LocaleUtils.encodeMessage("wwFileNotFound", null));
		        				this.m_binder.putLocal("StatusCode", "UC011");			
		        				throw new ServiceException(LocaleUtils.encodeMessage("wwFileNotFound", null));
		        			}    			
		    	        }while(sponsoredEmployersdrset.next());
		
		        		SystemUtils.trace("webapi_search", "Sponsored Employer ImageUrls List1 : " +sponsoredImageUrls);	
		    			SystemUtils.trace("webapi_search", "Sponsored Employers ImageUrls List2: " +tempSponsoredImageUrls);
		
		    			sponsoreImageResultSet=(DataResultSet)createResultSetFromData(sponsoredImageUrls, tempSponsoredImageUrls);			
		    			SystemUtils.trace("webapi_search", "SponsoredImage ResultSet : " +sponsoreImageResultSet);	
		        	 
		    			//Adding sponsoredImageUrl in Sponsored Jobs
		    			if(drsSearchResults.first())
		    			{
		    				do{
		    		        	//SystemUtils.trace("webapi_search",  "\nIn while: " + fieldIndex);
		    	        		int currentRowIndex =  drsSearchResults.getCurrentRow();
		    	        		String sponsoredDocName = drsSearchResults.getCurrentRowMap().get("xSponsored").toString();
		    	        		if(drsSearchResults.getCurrentRowMap().get("xSponsored").toString()!=null && drsSearchResults.getCurrentRowMap().get("xSponsored").toString().length()>0)
		    	        		{
			    	        		SystemUtils.trace("webapi_search", "Sponsored DocName while merging in ResultSet SearchResults : " +sponsoredDocName);	
			    	        		Vector vSponsored = sponsoreImageResultSet.findRow(0, sponsoredDocName);
			    	        		SystemUtils.trace("webapi_search", "Vector vSponsored : " +vSponsored);
			    	        		if(vSponsored!=null && !(vSponsored.isEmpty()))
			    	        		{
			    	        			sponsoredJobImageUrl=vSponsored.get(1).toString();
			    	        			micrositeUrl = vSponsored.get(2).toString();
			    	        		}
			    	        		vSearchresults = drsSearchResults.getCurrentRowValues();	        		
			    	        		vSearchresults.set(fieldIndex, sponsoredJobImageUrl);
			    	        		vSearchresults.set(fieldIndex+1, micrositeUrl);
			    	        		
			    	        		SystemUtils.trace("webapi_search", "Sponsored Job DocName : " +drsSearchResults.getCurrentRowMap().get("dDocName").toString());	
			    	        		SystemUtils.trace("webapi_search", "Sponsored Employer DocName : " +drsSearchResults.getCurrentRowMap().get("xSponsored").toString());	
			    	        		SystemUtils.trace("webapi_search", "Sponsored Image URL : " +sponsoredJobImageUrl);	
			    	        		SystemUtils.trace("webapi_search", "microsite URL : " +micrositeUrl);
		    	        		}
		    		        }while(drsSearchResults.next());
		    			}
		        	}
				 }
					//Removing xSponsored[custom metadata field] response parameter
					String removeFields[]={"xSponsored","xLocation","xHaysLocation1","xHaysLocation2","xHaysLocation3","xHaysLocation4","xHaysLocation5","xHaysLocation6"};
					drsSearchResults.removeFields(removeFields);
					
					this.m_binder.putLocal("StatusMessage", LocaleUtils.encodeMessage("wwWebApiOKMsg", null));
					this.m_binder.putLocal("StatusCode", "UC000"); 
			        }
			 }	
        }
		java.util.Date date1= new java.util.Date();
		Timestamp currentTimestamp1 = new java.sql.Timestamp(date1.getTime());			
		SystemUtils.trace("webapi_jobdetail", "function addSponsoredJobImageUrl End Time: "+currentTimestamp1);
	}
	
	//This function is used for adding EmployerLogo ImageUrl in ResultSet SearchResults
	public void addEmployerLogoImageUrl() throws DataException, ServiceException {
		
		java.util.Date date= new java.util.Date();
		Timestamp currentTimestamp = new java.sql.Timestamp(date.getTime());			
		SystemUtils.trace("webapi_jobdetail", "function addEmployerLogoImageUrl Start Time: "+currentTimestamp);
		
		StringBuffer sponsoredEmployers = new StringBuffer(100);
		String sEmployerDocNameList="";
		String filelocation=null;
		String webLayoutlUrl=SharedObjects.getEnvironmentValue("WeblayoutDir");			
		String securityGroup="";
		String docAccount="";
		String doctype="";
		String fileExtension="";
		String sponsoredEmployerDocName="";
		ArrayList<String> sponsoredImageUrls = new ArrayList<String>();	
		ArrayList<String> tempSponsoredImageUrls = new ArrayList<String>();	
		DataResultSet sponsoreImageResultSet = null;
		String flag=m_binder.getLocal("isjobDetail");
		DataBinder docParams = new DataBinder();
		Vector vSearchresults=null;
		int fieldIndex=0;
		String documentName="", websites ="";
		SystemUtils.trace("webapi_search", "isMobile: "+ m_binder.getLocal("isMobile"));	
		SystemUtils.trace("webapi_search", "flag : "+ flag);	
		
		if(m_binder.getLocal("isMobile")!=null && m_binder.getLocal("isMobile").equals("Y"))
        {
			DataResultSet drsSearchResults =new DataResultSet();
			if(flag!=null && ("Y").equalsIgnoreCase(flag))
			{
				drsSearchResults=(DataResultSet)super.m_binder.getResultSet(RESULTSET_HAYSDOCINFO);
			}
			else
			{
				drsSearchResults=(DataResultSet)super.m_binder.getResultSet(RESULTSET_SEARCHRESULTS);
			}
			if(drsSearchResults!=null)
			{				
			
				if(drsSearchResults.getNumRows()>0)
		        {
					drsSearchResults.first();
					SystemUtils.trace("webapi_search", "ResultSet SearchResults total rows: "+ drsSearchResults.getNumRows());			
					fieldIndex = drsSearchResults.getNumFields();
					Vector<FieldInfo> sponsoredImage = new Vector<FieldInfo>();
			        FieldInfo SporedImageFieldInfo = new FieldInfo();
			        SporedImageFieldInfo.m_name="EmployerLogo";
			        SporedImageFieldInfo.m_type = 6;
			        sponsoredImage.add(SporedImageFieldInfo);
			        drsSearchResults.appendFields(sponsoredImage);
			        
			        String sponsoredJobImageUrl="";
			        drsSearchResults.first();
					do{
		        		int currentRowIndex =  drsSearchResults.getCurrentRow();
		        		SystemUtils.trace("webapi_search", "current row index: " +currentRowIndex);
		        		SystemUtils.trace("webapi_search", "current row : " +drsSearchResults.getCurrentRowMap());
		        		SystemUtils.trace("webapi_search", "current row xWebsites: " +drsSearchResults.getCurrentRowMap().get("xWebsites"));
		        		SystemUtils.trace("webapi_search", "current row xEmployerLogo: " +drsSearchResults.getCurrentRowMap().get("xEmployerLogo"));
		        		documentName = drsSearchResults.getCurrentRowMap().get("dDocName").toString().trim();  
		        		websites = (String)drsSearchResults.getCurrentRowMap().get("xWebsites");
		        		
		        		SystemUtils.trace("webapi_search", "dDocname : " +documentName+" xWebsites"+websites);
			        	String HaysLocationsByLevel="";
		        		 
		        		if(drsSearchResults.getCurrentRowMap().get("xEmployerLogo").toString()!=null && drsSearchResults.getCurrentRowMap().get("xEmployerLogo").toString().length()>0)
		        		{
		        			sponsoredEmployers=sponsoredEmployers.append("','").append(drsSearchResults.getCurrentRowMap().get("xEmployerLogo").toString());
		        		}        		
		        		
			        }while(drsSearchResults.next());
					
					SystemUtils.trace("webapi_search","xEmployerLogo DocName list ="+ sponsoredEmployers);				
					
				if(sponsoredEmployers!=null && sponsoredEmployers.length()>0)
				 {
					
					sEmployerDocNameList=sponsoredEmployers.substring(3,sponsoredEmployers.length());
					SystemUtils.trace("webapi_search","Final SponsoredEmployers DocName list ="+ sEmployerDocNameList);	       
			        
			        
			        docParams.putLocal("SEmployerDocNameList", sEmployerDocNameList); 
			        
			        Provider p = Providers.getProvider("SystemDatabase");	    	    
			        Workspace databaseServerWs = (Workspace)p.getProvider();
					ResultSet sponsoredEmployersrset = databaseServerWs.createResultSet("QGetSponsoredEmployersDetails", docParams);
					DataResultSet sponsoredEmployersdrset=new DataResultSet(); 
					sponsoredEmployersdrset.copy(sponsoredEmployersrset);
					
					SystemUtils.trace("webapi_search","SponsoredEmployers dRSet ="+ sponsoredEmployersdrset);
					
					sponsoredImageUrls.add("xEmployerLogo");
					sponsoredImageUrls.add("xImageURL"); 
		        	
		        	if (sponsoredEmployersdrset.getNumRows()>0)
		        	{
		        		do{
		            		//Define variables for creating content file path
		        			webLayoutlUrl=SharedObjects.getEnvironmentValue("WeblayoutDir");			
		        			securityGroup=sponsoredEmployersdrset.getStringValueByName("dSecurityGroup");
		        			docAccount=sponsoredEmployersdrset.getStringValueByName("dDocAccount");
		        			doctype=sponsoredEmployersdrset.getStringValueByName("dDocType");
		        			fileExtension=sponsoredEmployersdrset.getStringValueByName("dWebExtension");    			   			
		        			sponsoredEmployerDocName=sponsoredEmployersdrset.getStringValueByName("dDocName");
		        			
		        			if (docAccount != null && docAccount.length() > 0)
		        			{
		        				if(docAccount.indexOf("/")>0)
		        				{
		        					docAccount=docAccount.replaceAll("/", "/@");
		        					docAccount="@"+docAccount;						
		        				}
		        				else
		        				{
		        					docAccount="@"+docAccount;
		        				}
		        				filelocation="groups/"+securityGroup+"/"+docAccount+"/documents/"+doctype+"/"+sponsoredEmployerDocName+"."+fileExtension;
		        			}
		        			else
		        			{
		        				filelocation="groups/"+securityGroup+"/documents/"+doctype+"/"+sponsoredEmployerDocName+"."+fileExtension;
		        			}
		        			filelocation=filelocation.toLowerCase();
		        			filelocation=webLayoutlUrl+filelocation;
		        			SystemUtils.trace("webapi_search", "File URL: "+filelocation);
		        			
		        			try
		        			{
		        				DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		        	            DocumentBuilder docBuilder = null;
		        				docBuilder = docBuilderFactory.newDocumentBuilder();
		        				Document doc = null;
		        				doc = docBuilder.parse (filelocation);				
		        	            doc.getDocumentElement().normalize();
		        	
		        	            //SystemUtils.trace("webapi_search", "Root element of the doc is: "+doc.getDocumentElement().getNodeName());
		        	            
		        	            NodeList ndlst = doc.getElementsByTagName("wcm:element");	
		        	           
		        	            //SystemUtils.trace("webapi_search", "Total no of doc elements : "+totalElements);            
		        	            //String imagePath= null;

		        	            String imagePath = "", micURL="";
		        	            for(int s=0; s<ndlst.getLength() ; s++){
		        	            	Node firstPersonNode = ndlst.item(s);	            	
		        	            	if(firstPersonNode.getNodeType() == Node.ELEMENT_NODE){
			        	            	Element firstElement = (Element)firstPersonNode;
			        	            	//SystemUtils.trace("webapi_search", "XML element Name : " +firstElement.getAttribute("name").trim());
			        	            	Node nd = firstElement.getFirstChild();
			        	            	if("Image".equalsIgnoreCase(firstElement.getAttribute("name").trim()) && nd.getNodeValue().toString()!=null)
			        	            	{
			        	            		//imagePath= null;
			            	            	String elementContent=nd.getNodeValue().trim();
			            	            	int imageSrcStartIndex=elementContent.indexOf("wcmUrl");
			            	            	int imageSrcEndIndex=elementContent.indexOf("')");
			            	            	//getSiteId(documentName, websites);
			            	            	imagePath=elementContent.substring(imageSrcStartIndex+19, imageSrcEndIndex);
			            	            	//imagePath=resolveIDOCFunctions(elementContent.substring(imageSrcStartIndex-5, imageSrcEndIndex+5), websites);
			        	            	}  
		        					}
		        				}
		        	            tempSponsoredImageUrls.add(sponsoredEmployerDocName);
            	            	tempSponsoredImageUrls.add(imagePath);
            	            	
		        			}
		        			catch (ParserConfigurationException e)
		        			{
		        				this.m_binder.putLocal("StatusMessage", LocaleUtils.encodeMessage("wwFileNotParse", null));
		        				this.m_binder.putLocal("StatusCode", "UC012");
		        				throw new ServiceException(LocaleUtils.encodeMessage("wwFileNotParse", null));
		        			}
		        			catch (SAXException e)
		        			{
		        				this.m_binder.putLocal("StatusMessage", LocaleUtils.encodeMessage("wwFileNotFormedProperly", null));
		        				this.m_binder.putLocal("StatusCode", "UC012");			
		        				throw new ServiceException(LocaleUtils.encodeMessage("wwFileNotFormedProperly", null));
		        			} 
		        			catch (IOException e)
		        			{
		        				this.m_binder.putLocal("StatusMessage", LocaleUtils.encodeMessage("wwFileNotFound", null));
		        				this.m_binder.putLocal("StatusCode", "UC011");			
		        				throw new ServiceException(LocaleUtils.encodeMessage("wwFileNotFound", null));
		        			}    			
		    	        }while(sponsoredEmployersdrset.next());
		
		        		SystemUtils.trace("webapi_search", "EmployerLogo ImageUrls List1 : " +sponsoredImageUrls);	
		    			SystemUtils.trace("webapi_search", "EmployerLogo ImageUrls List2: " +tempSponsoredImageUrls);
		
		    			sponsoreImageResultSet=(DataResultSet)createResultSetFromData(sponsoredImageUrls, tempSponsoredImageUrls);			
		    			SystemUtils.trace("webapi_search", "SponsoredImage ResultSet : " +sponsoreImageResultSet);	
		        	 
		    			//Adding xEmployerLogo in Jobs
		    			if(drsSearchResults.first())
		    			{
		    				do{
		    	        		int currentRowIndex =  drsSearchResults.getCurrentRow();
		    	        		String sponsoredDocName = drsSearchResults.getCurrentRowMap().get("xEmployerLogo").toString();
		    	        		if(drsSearchResults.getCurrentRowMap().get("xEmployerLogo").toString()!=null && drsSearchResults.getCurrentRowMap().get("xEmployerLogo").toString().length()>0)
		    	        		{
			    	        		SystemUtils.trace("webapi_search", "xEmployerLogo DocName while merging in ResultSet SearchResults : " +sponsoredDocName);	
			    	        		Vector vSponsored = sponsoreImageResultSet.findRow(0, sponsoredDocName);
			    	        		SystemUtils.trace("webapi_search", "Vector vSponsored : " +vSponsored);
			    	        		if(vSponsored!=null && !(vSponsored.isEmpty()))
			    	        		{
			    	        			sponsoredJobImageUrl=vSponsored.get(1).toString();
			    	        		}
			    	        		vSearchresults = drsSearchResults.getCurrentRowValues();	        		
			    	        		vSearchresults.set(fieldIndex, sponsoredJobImageUrl);
			    	        		
			    	        		SystemUtils.trace("webapi_search", "xEmployerLogo Job DocName : " +drsSearchResults.getCurrentRowMap().get("dDocName").toString());	
			    	        		SystemUtils.trace("webapi_search", "xEmployerLogo Employer DocName : " +drsSearchResults.getCurrentRowMap().get("xEmployerLogo").toString());	
			    	        		SystemUtils.trace("webapi_search", "xEmployerLogo Image URL : " +sponsoredJobImageUrl);	
		    	        		}
		    		        }while(drsSearchResults.next());
		    			}
		        	}
				 }
					//Removing xSponsored[custom metadata field] response parameter
					String removeFields[]={"xEmployerLogo"};
					drsSearchResults.removeFields(removeFields);
					
					this.m_binder.putLocal("StatusMessage", LocaleUtils.encodeMessage("wwWebApiOKMsg", null));
					this.m_binder.putLocal("StatusCode", "UC000"); 
			        }
			 }	
        }
		java.util.Date date1= new java.util.Date();
		Timestamp currentTimestamp1 = new java.sql.Timestamp(date1.getTime());			
		SystemUtils.trace("webapi_jobdetail", "function addEmployerLogoImageUrl End Time: "+currentTimestamp1);
	}
	
	public String resolveIDOCFunctions(String pStringToEvaluate, String websites) {
		String websiteAddress = m_binder.getLocal("websiteAddress");
		if (!websiteAddress.startsWith("http")) {
			websiteAddress = "http://" + websiteAddress;
		}
		pStringToEvaluate = pStringToEvaluate.replaceAll("\\[!--",
				websiteAddress + "<");
		pStringToEvaluate = pStringToEvaluate.replaceAll("--]", "\\$>");
		pStringToEvaluate = pStringToEvaluate.replaceAll(
				"\\?ssSourceSiteId=null", "");
		String returnString = "";
		try {
			String siteId = websites;
			SystemUtils.trace(TRACE_NAME, "siteId " + siteId);
			m_binder.putLocal("siteId", siteId.trim());
			returnString =m_service.getPageMerger().evaluateScript(pStringToEvaluate);
			returnString = returnString.substring(returnString.indexOf("groups/"), returnString.length());
			return returnString;
		} catch (IllegalArgumentException e) {
			return pStringToEvaluate;
		} catch (IOException e) {
			return pStringToEvaluate;
		}
	}
	
	public void getSiteId(String documentName, String websites) throws ServiceException, DataException {
		String siteId = websites;
		String test1 = documentName;
		SystemUtils.trace("webapi_jobdetailWU", "Inside getSiteId function dDocName value: "+test1);
		String QwebsiteAddressQuery = "QssSiteAddressesBySite";
		String websiteAddress = "";
		String dSiteId = siteId.trim();
		m_binder.putLocal("siteId", siteId.trim());
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

			} while (drsSiteAddress.next());

		}
		SystemUtils.trace(TRACE_NAME, "Website Address:  " + websiteAddress);
		m_binder.putLocal("websiteAddress", websiteAddress);

	}
	

}
