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
 * Copyright (c) 2004-2006 Stellent, Inc.
 * All rights reserved.
 *
 * $Id: SoapSerializer.java 49099 2006-09-27 15:26:03Z swhite $
 */

package outputFormat;

import java.io.*;
import java.util.*;

import intradoc.common.*;
import intradoc.data.*;
import intradoc.server.*;

//modelled after intradoc.soap.SoapSerializer
public class XmlSerializer
{		
	static public boolean parseRequest(DataBinder data) throws ServiceException
	{		
		return false;						
	}
	
	static public void postParseRequest(DataBinder data) throws ServiceException
	{
	}	
	
	static public String sendResponse(DataBinder data, ExecutionContext cxt) throws IOException
	{	
	    StringBuffer buffer = new StringBuffer();
	    
	    serializeDataBinderToStringBuffer(data, cxt, buffer);	    
	    
	    return buffer.toString();
	}   
	
	static public byte[] sendResponseBytes(DataBinder data, ExecutionContext cxt,
		String encoding) throws IOException
	{
	    //Send the xml node.		
            String isoEncoding = DataSerializeUtils.getIsoEncoding(encoding);
            if (isoEncoding == null)
            {
            	isoEncoding = encoding;
            }
	    StringBuffer buffer = new StringBuffer();
	    buffer.append("<?xml version='1.0' encoding='" + isoEncoding + "' ?>\r\n");
	    serializeDataBinderToStringBuffer(data, cxt, buffer);
	    
	    return (buffer.toString()).getBytes("UTF8");
	    
	}
	

	static public boolean sendStreamResponse(DataBinder data, Service service,
		DataStreamWrapper streamWrapper, ServiceHttpImplementor httpImplementor) 
			throws IOException
	{	    
	    return false;
	}
	
	public static void serializeDataBinderToStringBuffer(DataBinder data, ExecutionContext cxt, StringBuffer buffer)
	{
	    buffer.append("<idc:service name=\"" + data.getAllowMissing("IdcService") + "\" xmlns:idc=\"http://www.stellent.com/IdcService/\">\r\n")  ; 
	    outputDataBinderToStringBuffer(data, cxt,buffer);
	    if(DataUtils.getBoolean(data, "IS_ERROR", false))
	    {
		buffer.append("\r\n");
		outputErrorToStringBuffer(data, cxt,buffer);
	    }
	    buffer.append("\r\n");	    
	    buffer.append("</idc:service>");
	}
	
	public static void outputErrorToStringBuffer(DataBinder data, ExecutionContext cxt, StringBuffer buffer)
	{	   
	    String error_message = data.getEnvironmentValue("ERROR_MESSAGE");
	    String error_stack = data.getEnvironmentValue("ERROR_STACK");
	    String xmlEncodingMode = data.getLocal("XmlEncodingMode");
	    
            buffer.append("\r\n<idc:error>");
            
            buffer.append("\r\n<idc:message>\r\n");
            buffer.append(StringUtils.encodeXmlEscapeSequence(error_message, xmlEncodingMode));
            buffer.append("\r\n</idc:message>");
            
            buffer.append("\r\n<idc:stack>\r\n");
            buffer.append(StringUtils.encodeXmlEscapeSequence(error_stack, xmlEncodingMode));
            buffer.append("\r\n</idc:stack>");
	    
	    buffer.append("</idc:error>");
	}
	
	public static void outputDataBinderToStringBuffer(DataBinder data, ExecutionContext cxt, StringBuffer buffer)
	{
	    buffer.append("\r\n<idc:result>");
	    outputLocalDataToStringBuffer(data, cxt,buffer);
	    outputAllResultSetsToStringBuffer(data, cxt,buffer);
	    buffer.append("</idc:result>");
	}
	
	public static void outputLocalDataToStringBuffer(DataBinder data, ExecutionContext cxt, StringBuffer buffer)
	{
	    Properties localdata = data.getLocalData();
	    buffer.append("\r\n<idc:localdata>");	    
	    String key, value;
	    String xmlEncodingMode = data.getLocal("XmlEncodingMode");
	    
	    for (Enumeration en = localdata.keys(); en.hasMoreElements();)
            {
            	key = (String)en.nextElement();
            	value = localdata.getProperty(key);;
            	if(key != null && key.length()>0 
            			&& key.indexOf(":") < 0  //colons break xml output
            			)
            	{
                	buffer.append("\r\n<idc:" + key+ ">\r\n");
                	buffer.append(StringUtils.encodeXmlEscapeSequence(value, xmlEncodingMode));
                	buffer.append("\r\n</idc:" + key+ ">\r\n");
            	}
            }	    
	    buffer.append("\r\n</idc:localdata>");
	}
	
	public static void outputAllResultSetsToStringBuffer(DataBinder data, ExecutionContext cxt, StringBuffer buffer)
	{
	    buffer.append("\r\n<idc:resultsets>");
	    
	    String name;
	    for (Enumeration en = data.getResultSetList(); en.hasMoreElements();)
            {
            	name = (String)en.nextElement();
            	outputResultSetToStringBuffer(name, data,cxt,buffer);
            }	    
	    buffer.append("\r\n</idc:resultsets>");
	}
	
	public static void outputResultSetToStringBuffer(String name, DataBinder data, ExecutionContext cxt, StringBuffer buffer)
	{
	    ResultSet rset = data.getResultSet(name);	    
	    int numFields = rset.getNumFields();
	    
	    buffer.append("\r\n<idc:"+name+">");
	    
	    String key;
	    String value = null;
	    FieldInfo finfo = new FieldInfo();	 
	    String xmlEncodingMode = data.getLocal("XmlEncodingMode");
	    
	    for(rset.first(); rset.isRowPresent();rset.next() )
	    {
		buffer.append("\r\n<idc:row>");	  
		for(int i=0; i<numFields;++i)
		{
		    key = rset.getFieldName(i);
		    value = rset.getStringValue(i);
		    rset.getFieldInfo(key, finfo);

		    if(key != null && key.length()>0)
		    {
        		    buffer.append("\r\n<idc:"+key+">");	
        		    
        		    switch(finfo.m_type)
        		    {
        			case FieldInfo.BOOLEAN:
        			case FieldInfo.INT:
        			case FieldInfo.FLOAT:
        			    buffer.append( value);
        			    break;
        			case FieldInfo.DATE:
        			    buffer.append(StringUtils.encodeXmlEscapeSequence(value, xmlEncodingMode));
        			    break;
        			default:
        			    buffer.append(StringUtils.encodeXmlEscapeSequence(value, xmlEncodingMode));
        			    break;
        		    }
        		    buffer.append("\r\n</idc:"+key+">");
		    }
		}
		buffer.append("\r\n</idc:row>");
	    }	    
	    buffer.append("\r\n</idc:"+name+">");	    
	}
	

	
	public static Object idcVersionInfo(Object arg)
	{
		return "releaseInfo=dev,releaseRevision=$Rev: 49099 $";
	}
}
