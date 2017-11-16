/*
 * Confidential and Proprietary for Stellent, Inc.
 * 
 * This computer program contains valuable, confidential, and
 * proprietary information.  Disclosure, use, or reproduction
 * without the written authorization of Stellent is prohibited.
 * This unpublished work by Stellent is protected by the laws
 * of the United States and other countries.  If publication
 * of this computer program should occur, the following notice
 * shall apply:
 * 
 * Copyright (c) 1997-2001 IntraNet Solutions, Inc.
 * All rights reserved.
 * Copyright (c) 2001-2006 Stellent, Inc.
 * All rights reserved.
 *
 * $Id$
 */

package outputFormat;

import java.io.*;
import java.util.*;


import intradoc.common.*;
import intradoc.data.*;
import intradoc.serialize.*;
import intradoc.server.*;
import intradoc.shared.*;

/**
 * This class is used to identify WebDAV requests from any other Content Server request
 * and then pass off those requests to the correct service.
 */
public class OutputFormatSerializer implements FilterImplementor
{
	protected Workspace m_ws = null;
	protected DataBinder m_binder = null;
	protected ExecutionContext m_cxt = null;
	
	static public final short m_json=1;
	static public final short m_json_scripted=2;
	static public final short m_xml=3;
	static public final short m_xml_scripted=4;
	static public final short m_debug=5;
	static public final short m_hda=6;
	static public final short m_source=7;
	
	// Single character string that separate different values in WebdavSessionID cookie
	protected static String m_cookieParameterSeparator = ":";
	
	public int doFilter(Workspace ws, DataBinder binder, ExecutionContext cxt)
		throws DataException, ServiceException
	{
		m_ws = ws;
		m_binder = binder;
		m_cxt = cxt;
		int returnCode = FilterImplementor.CONTINUE;
		
		if(cxt instanceof FileService)
		{
		    return FilterImplementor.CONTINUE;
		}
		
		String parameter = (String)m_cxt.getCachedObject("filterParameter");
		if (parameter.equals("postParseDataForServiceRequest"))
		{
				returnCode = doParse();			
		}

		else if (parameter.equals("preDoResponse"))
		{
		    	String outputSelectedResults = m_binder.getEnvironmentValue("OUTPUT_SELECTED_RESULTS");
		    	
        	    	if( DataUtils.getBoolean(m_binder, "IS_SELECTED_OUTPUT", false) && outputSelectedResults!= null 
        	    		&&!outputSelectedResults.equals(""))
        		{
        	    	    removeResultsetsExcludedFromSelection( outputSelectedResults);                	    		    	    
        		}
        	    	
        	    	if( DataUtils.getBoolean(m_binder, "IS_GET_ALL_IDOC_DATA", false) )
        		{
        	    	    loadAllIdocData();                	    		    	    
        		}
        	    	if( DataUtils.getBoolean(m_binder, "IS_LOAD_ENVIRONMENT", false) )
        		{
        	    	    loadEnvironment();                	    		    	    
        		}
        	    	returnCode = preDoResponse();
        		
		}
				
		return returnCode;
	}
	
	public int doParse() 
	{
		int returnCode = FilterImplementor.CONTINUE;
		
		String outputFormat = m_binder.getLocal("outputFormat");		
		if(outputFormat == null)
		{
		    return returnCode;
		}
		
		if("EXTENDED_GET_DOC_PAGE".equals(m_binder.getLocal("IdcService")))
		{
		    String page = m_binder.getLocal("Page");
		    m_binder.putLocal("ResourceTemplate", page);
		}
		
		String outputSelectedResults = m_binder.getLocal("outputSelectedResults");		
		if(outputSelectedResults != null)
		{
		    if(outputSelectedResults.equalsIgnoreCase("@preferred") )
		    {
			String serviceName = m_binder.getLocal("IdcService");
			outputSelectedResults = SharedObjects.getEnvironmentValue(serviceName + ":preferredResultSets");
		    }
		    
		    if(outputSelectedResults != null)
		    {
			m_binder.setEnvironmentValue("OUTPUT_SELECTED_RESULTS", outputSelectedResults);
		    }
		}
		
		String isGetAllIdocData = m_binder.getLocal("isGetAllIdocData");		
		if(isGetAllIdocData != null)
                {
		    m_binder.setEnvironmentValue("IS_GET_ALL_IDOC_DATA", isGetAllIdocData);
                }
		
		String isLoadEnvironment = m_binder.getLocal("isLoadEnvironment");		
		if(isLoadEnvironment != null)
                {
		    m_binder.setEnvironmentValue("IS_LOAD_ENVIRONMENT", isLoadEnvironment);
                }
				
		if(outputFormat.equalsIgnoreCase("json") || outputFormat.equalsIgnoreCase("json_scripted"))
		{
                    if(outputFormat.equalsIgnoreCase("json"))
                    {
                	m_binder.setEnvironmentValue("IS_JSON_REQUEST", "1");
                    }
                    else
                    {
                	m_binder.setEnvironmentValue("IS_JSON_SCRIPTED_REQUEST", "1");
                    }
		    
			m_binder.setEnvironmentValue("IS_SELECTED_OUTPUT", "1");
			
                    String callback = m_binder.getLocal("callback");
                    if(callback != null)
                    {
                        m_binder.setEnvironmentValue("JSON_CALLBACK", callback);
                    }
                    String id = m_binder.getLocal("id");
                    if(id != null)
                    {
                        m_binder.setEnvironmentValue("JSON_ID", id);
                    }
                    return FilterImplementor.CONTINUE;
		}
		else if(outputFormat.equalsIgnoreCase("xml") || outputFormat.equalsIgnoreCase("xml_scripted"))
		{
		    if(outputFormat.equalsIgnoreCase("xml"))
		    {
			m_binder.setEnvironmentValue("IS_XML_REQUEST", "1");
		    }
		    else
		    {
			m_binder.setEnvironmentValue("IS_XML_SCRIPTED_REQUEST", "1");
		    }
		    
		    m_binder.setEnvironmentValue("IS_SELECTED_OUTPUT", "1");
		    
		    return FilterImplementor.CONTINUE;
		}
		else if(outputFormat.equalsIgnoreCase("debug"))
		{
		    m_binder.setEnvironmentValue("IS_DEBUG_REQUEST", "1");
		    m_binder.setEnvironmentValue("IS_SELECTED_OUTPUT", "1");
		    return FilterImplementor.CONTINUE;
		}
		else if(outputFormat.equalsIgnoreCase("soap"))
		{
		    m_binder.putLocal("IsSoap", "1");
		    m_binder.setEnvironmentValue("IS_SELECTED_OUTPUT", "1");
		}
		else if(outputFormat.equalsIgnoreCase("hda"))
		{
		    m_binder.setEnvironmentValue("IS_HDA_REQUEST", "1");
		    m_binder.setEnvironmentValue("IS_SELECTED_OUTPUT", "1");
		    m_binder.putLocal("IsJava", "1");
		    m_binder.m_isJava = true;
		}
		else if(outputFormat.equalsIgnoreCase("source"))
		{
		    m_binder.setEnvironmentValue("IS_SOURCE_REQUEST", "1");
		    m_binder.setEnvironmentValue("IS_SELECTED_OUTPUT", "0");
		    m_binder.setEnvironmentValue("IS_GET_ALL_IDOC_DATA", "0");
		}
						
		return returnCode;
	}

	public int preDoResponse() throws DataException, ServiceException
	{ 
	    int returnCode;
	    if (m_cxt instanceof Service && DataUtils.getBoolean(m_binder, "IS_JSON_REQUEST", false))
            {
            	returnCode = preDoResponseOutputFormatter(m_json);
            }
            else if (m_cxt instanceof Service && DataUtils.getBoolean(m_binder, "IS_JSON_SCRIPTED_REQUEST", false))
            {
            	returnCode = preDoResponseOutputFormatter(m_json_scripted);
            }
            else if (m_cxt instanceof Service && DataUtils.getBoolean(m_binder, "IS_XML_REQUEST", false))
            {
            	returnCode = preDoResponseOutputFormatter(m_xml);
            }
            else if (m_cxt instanceof Service && DataUtils.getBoolean(m_binder, "IS_XML_SCRIPTED_REQUEST", false))
            {
            	returnCode = preDoResponseOutputFormatter(m_xml_scripted);
            }
            else if (m_cxt instanceof Service && DataUtils.getBoolean(m_binder, "IS_DEBUG_REQUEST", false))
            {
            	returnCode = preDoResponseOutputFormatter(m_debug);
            }
            else if (m_cxt instanceof Service && DataUtils.getBoolean(m_binder, "IS_HDA_REQUEST", false))
            {
            	returnCode = preDoResponseOutputFormatter(m_hda);
            }
            else if (m_cxt instanceof Service && DataUtils.getBoolean(m_binder, "IS_SOURCE_REQUEST", false))
            {
            	returnCode = preDoResponseOutputFormatter(m_source);
            }
            else
            {
                returnCode = FilterImplementor.CONTINUE;
            }
	    return returnCode;
	}
	
	public int preDoResponseOutputFormatter(int type) throws DataException, ServiceException
	{
		Service service = (Service)m_cxt;
		
		service.setConditionVar("AllowZeroLengthResponse", true);
		if (service.getHtmlPageAsBytes() == null)
		{
			service.setHtmlPageAsBytes(new byte[0]);
		}
		
		Object[] filterParams = (Object[])m_cxt.getCachedObject("preDoResponse:parameters");
		boolean isError = ((Boolean)filterParams[0]).booleanValue();
		//ServiceException err = (ServiceException)filterParams[1];
		
		if(isError)
		{
        		m_binder.setEnvironmentValue("IS_ERROR", "1");
        		m_binder.setEnvironmentValue("ERROR_MESSAGE", m_binder.getAllowMissing("StatusMessage"));
        		try
        		{
        		    m_binder.setEnvironmentValue("ERROR_STACK", m_binder.getAllowMissing("ErrorStackTrace"));
        		}
        		catch(Exception e)
        		{
        		    
        		}

                        // Set isError to false so that when the filter returns, our formatted output is 
                        // delivered
                        filterParams[0] = new Boolean(false);
		}
		
		Object serviceHttpImplementorObj = m_cxt.getCachedObject("HttpImplementor");
		if (serviceHttpImplementorObj != null &&
			serviceHttpImplementorObj instanceof ServiceHttpImplementor)	
		{
			ServiceHttpImplementor serviceHttpImplementor = (ServiceHttpImplementor)serviceHttpImplementorObj;
			serviceHttpImplementor.m_httpSendResponseHeaderEncoding = DataSerializeUtils.getSystemEncoding();
		}
		
		DataBinderSerializer theSerializer = new DataBinderSerializer();
		byte[] outputByteArray = new byte[0];
		try
		{	
		    switch(type)
		    {
			case m_json:
        		    theSerializer.setDataBinderProtocol(new JsonDataBinderProtocolImplementor());
        		    m_binder.setContentType("text/javascript; charset=\"utf-8\"");
        		    outputByteArray = theSerializer.sendBytes(m_binder, "UTF8", false, m_cxt);
        		    service.setHtmlPageAsBytes(outputByteArray);
        		    service.setDoResponsePageDynHtmlCalculation(false);	
        		    break;
			case m_json_scripted:
			    prepareResultSetsforDebug();
			    m_binder.setContentType("text/javascript; charset=\"utf-8\"");
			    m_binder.m_clientEncoding = "utf-8";
			    m_binder.putLocal("ResourceTemplate", "JsonScripted");
			    break;    
			case m_xml:
			    theSerializer.setDataBinderProtocol(new XmlDataBinderProtocolImplementor());
        		    m_binder.setContentType("text/xml; charset=\"utf-8\"");
        		    outputByteArray = theSerializer.sendBytes(m_binder, "UTF8", false, m_cxt);
        		    service.setHtmlPageAsBytes(outputByteArray);
        		    service.setDoResponsePageDynHtmlCalculation(false);	
			    break;
			case m_xml_scripted:
			    prepareResultSetsforDebug();
			    m_binder.m_clientEncoding = "utf-8";
        		    m_binder.setContentType("text/xml; charset=\"utf-8\"");
			    m_binder.putLocal("ResourceTemplate", "XmlScripted");
			    break;    
			case m_hda:
			    m_binder.m_clientEncoding = "utf-8";
        		    m_binder.setContentType("text/plain; charset=\"utf-8\"");
			    break; 
			case m_source:
			    m_binder.m_clientEncoding = "utf-8";
        		    m_binder.setContentType("text/plain; charset=\"utf-8\"");
			    break;     
			case m_debug:
			    prepareResultSetsforDebug();
			    m_binder.putLocal("ResourceTemplate", "OutputFormatterDebug");
			    break;  
		    }
		    return FilterImplementor.CONTINUE;
		}
		catch(IOException ioe)
		{
		    throw new ServiceException(ioe);
		}		
	}
	
	public void loadEnvironment() throws DataException
	{
	   
	   String [][] environmentValuesArray = new String[][]
	       {   {"HttpWebRoot", DirectoryLocator.getWebRoot(false)}, 
		   {"HttpAbsoluteWebRoot", DirectoryLocator.getWebRoot(true)}, 
		   {"HttpCgiPath", DirectoryLocator.getCgiWebUrl(false)}, 
		   {"HttpAbsoluteCgiPath", DirectoryLocator.getCgiWebUrl(true)}, 
		   {"HttpBrowserFullCgiPath", SharedObjects.getEnvironmentValue("HttpBrowserFullCgiPath")}, 
		   {"AllowIntranetUsers", SharedObjects.getEnvValueAsBoolean("NtlmSecurityEnabled", false) ? "1":"0"}, 
		   //{"HttpCommonRoot"}, 
		   //{"HttpImagesRoot"}, 
		   //{"HttpHelpRoot"}, 
		   //{"HttpSystemHelpRoot"}, 
		   //{"HttpAdminCgiPath"}, 
		   //{"HttpEnterpriseCgiPath"}
	 	                                 };
            String[] temp = new String[2];
            temp[0] = "name";  temp[1]="value";  
            DataResultSet rset = new DataResultSet(temp);
            String key, value;
            for(int i=0; i<environmentValuesArray.length;++i)
            {
            	Vector vect = rset.createEmptyRow(); 
            	key = environmentValuesArray[i][0];
            	value = environmentValuesArray[i][1];
            	vect.setElementAt(key, 0);
            	vect.setElementAt(value, 1);
            	rset.addRow(vect);            
            }
	    //ResultSetUtils.sortResultSet(rset, temp);
	    m_binder.addResultSet("PageEnvironment", rset);
	  

	}
	
	public void loadAllIdocData()
	{
	    try
	    {
		PageBuilderSerializer.sendResponseBytes(m_binder, m_cxt);
	    }
	    catch(IOException ioe)
	    {
		
	    }
	    catch(ServiceException iosee)
	    {
		
	    }
	}

	public void removeResultsetsExcludedFromSelection(String outputSelectedResults)
	{
	    Vector list = StringUtils.parseArray(outputSelectedResults, ',', '\\');
            int nstrings = list.size();
            
            Properties selectedResults = new Properties();
            
            for (int i = 0; i < nstrings; i++)
            {
            	String nameValue = (String)list.elementAt(i);
            	selectedResults.put(nameValue.trim(), nameValue);            	
            }
            
	    //Filer the results sets in the local binder
            List removeList = new Vector();            
            int count = 0;
            
            for (Enumeration en = m_binder.getResultSetList(); en.hasMoreElements();)
            {
            	String key = (String)en.nextElement();
            	if(!selectedResults.containsKey(key))
            	{
            	    removeList.add(key);
            	    count++;
            	}
            }
            
            for(int i=0;i<count;++i)
            {
        	m_binder.removeResultSet((String)removeList.get(i));        	
            }
	}
	
	public void prepareResultSetsforDebug() throws DataException
	{

	    //first create a resultset containing the names of 
	    //all actual resultsets, from the service
	    {
        	    String[] temp = new String[2];
        	    temp[0] = "resultsetname"; temp[1] = "fields";       	    
        	    
        	    DataResultSet rset = new DataResultSet(temp);
        	    String key, value;
        	    for (Enumeration en = m_binder.getResultSetList(); en.hasMoreElements();)
                    {
                    	key = (String)en.nextElement();
                    	value = getFieldList(key);
                    	Vector vect = rset.createEmptyRow();            	
                    	vect.setElementAt(key, 0);
                    	vect.setElementAt(value, 1);
                    	rset.addRow(vect);
                    }
        	    ResultSetUtils.sortResultSet(rset, temp);
        	    m_binder.addResultSet("resultsetListing", rset);
	    }
	    //second create a resultset containing the local 
	    //data
	    {
        	    String[] temp = new String[2];
        	    temp[0] = "name";  temp[1]="value";      	    
        	    
        	    DataResultSet rset = new DataResultSet(temp);
        	    String key, value;
        	    for (Enumeration en = m_binder.getLocalData().keys(); en.hasMoreElements();)
                    {
                    	key = (String)en.nextElement();
                    	value = m_binder.getActiveAllowMissing(key);
                    	Vector vect = rset.createEmptyRow();            	
                    	vect.setElementAt(key, 0);
                    	vect.setElementAt(value, 1);
                    	rset.addRow(vect);
                    }
        	    ResultSetUtils.sortResultSet(rset, temp);
        	    m_binder.addResultSet("localdata", rset);
	    }
	    
	    //third create a resultset containing any error data 
	    //data
	    if(DataUtils.getBoolean(m_binder, "IS_ERROR", false))
	    {
		    String message =  m_binder.getAllowMissing("StatusMessage");
		    String stack = m_binder.getAllowMissing("ErrorStackTrace");
		
        	    String[] temp = new String[2];
        	    temp[0] = "message";  temp[1]="stack";      	    
        	    
        	    DataResultSet rset = new DataResultSet(temp);
        	    Vector vect = rset.createEmptyRow();            	
            	    vect.setElementAt(message, 0);
            	    vect.setElementAt(stack, 1);
            	    rset.addRow(vect);
                	
        	    m_binder.addResultSet("error", rset);
	    }	    
	}
	
	public String getFieldList(String key)
	{
	    ResultSet rset = m_binder.getResultSet(key);
	    StringBuffer buffer = new StringBuffer();
	    int numFields = rset.getNumFields();
	    String prefix = "";
	    
	    for(int i=0; i< numFields;++i)
	    {
		buffer.append(prefix);
		buffer.append(rset.getFieldName(i));
		prefix = ",";
	    }	    
	    return buffer.toString();
	}
	
	public static Object idcVersionInfo(Object arg)
	{
		return "releaseInfo=dev,releaseRevision=$Rev$";
	}
}
