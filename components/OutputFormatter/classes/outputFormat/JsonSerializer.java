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
public class JsonSerializer
{		
	static public boolean parseRequest(DataBinder data) throws ServiceException
	{		
		return false;						
	}
	
	static public void postParseRequest(DataBinder data) throws ServiceException
	{
	}	


	static public boolean sendStreamResponse(DataBinder data, Service service,
		DataStreamWrapper streamWrapper, ServiceHttpImplementor httpImplementor) 
			throws IOException
	{	    
	    return false;
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
	    StringBuffer buffer = new StringBuffer();
	    serializeDataBinderToStringBuffer(data, cxt, buffer);
	    
	    return (buffer.toString()).getBytes("UTF8");
	    
	}
	
	
	public static void serializeDataBinderToStringBuffer(DataBinder data, ExecutionContext cxt, StringBuffer buffer)
	{
	    String callback = data.getEnvironmentValue("JSON_CALLBACK");
	    String prefix = "", suffix="";
	    
	    if(callback != null && callback.length() > 0)
	    {
		prefix = callback + "(";
		suffix = ");";
	    }
	    
	    buffer.append(prefix);
	    
	    buffer.append("{");	    
	    outputDataBinderToStringBuffer(data, cxt,buffer);
	    if(DataUtils.getBoolean(data, "IS_ERROR", false))
	    {
		buffer.append(",\n");
		outputErrorToStringBuffer(data, cxt,buffer);
	    }
	    String id;
	    if((id = data.getEnvironmentValue("JSON_ID")) != null && id.length()>0)
	    {
		buffer.append(",\n");
		outputIdToStringBuffer(id,buffer);
	    }
	    buffer.append("\n}");
	    
	    buffer.append(suffix);
	}
	public static void outputIdToStringBuffer(String id, StringBuffer buffer)
	{
	    buffer.append("\"id\":");
	    buffer.append("\"");
	    buffer.append(id);
	    buffer.append("\"");	    
	}
	
	public static void outputErrorToStringBuffer(DataBinder data, ExecutionContext cxt, StringBuffer buffer)
	{	   
	    String error_message = data.getEnvironmentValue("ERROR_MESSAGE");
	    String error_stack = data.getEnvironmentValue("ERROR_STACK");
	    
            buffer.append("\"error\":{\n\t");
            buffer.append("\""+StringUtils.encodeJavascriptString("message")+"\":");
            buffer.append("\""+StringUtils.encodeJavascriptString(error_message)+"\",\n\t");
            
            //buffer.append("\""+StringUtils.encodeJavascriptString("stack")+"\":");
            //buffer.append("\""+StringUtils.encodeJavascriptString(error_stack)+"\"");
	    
	    buffer.append("\n}");
	}
	
	public static void outputDataBinderToStringBuffer(DataBinder data, ExecutionContext cxt, StringBuffer buffer)
	{
	    buffer.append("\"result\":{\n\t");
	    outputLocalDataToStringBuffer(data, cxt,buffer);
	    buffer.append(",\n\t");
	    outputAllResultSetsToStringBuffer(data, cxt,buffer);
	    buffer.append("\n}");
	}
	
	public static void outputLocalDataToStringBuffer(DataBinder data, ExecutionContext cxt, StringBuffer buffer)
	{
	    Properties localdata = data.getLocalData();
	    buffer.append("\"localdata\":{");	    
	    
	    String prefix = "", key, value;
	    for (Enumeration en = localdata.keys(); en.hasMoreElements();)
            {
            	key = (String)en.nextElement();
            	value = localdata.getProperty(key);;
            	buffer.append(prefix);
            	buffer.append("\""+StringUtils.encodeJavascriptString(key)+"\":");
            	buffer.append("\""+StringUtils.encodeJavascriptString(value)+"\"");
            	prefix = ",";
            }	    
	    buffer.append("}");
	}
	
	public static void outputAllResultSetsToStringBuffer(DataBinder data, ExecutionContext cxt, StringBuffer buffer)
	{
	    buffer.append("\"resultsets\":{");
	    
	    String prefix = "\n\t\t", name;
	    for (Enumeration en = data.getResultSetList(); en.hasMoreElements();)
            {
            	name = (String)en.nextElement();
            	buffer.append(prefix);
            	outputResultSetToStringBuffer(name, data,cxt,buffer);
            	prefix = ",\n\t\t";
            }	    
	    buffer.append("\n\t}");
	}
	
	public static void outputResultSetToStringBuffer(String name, DataBinder data, ExecutionContext cxt, StringBuffer buffer)
	{
	    ResultSet rset = data.getResultSet(name);	    
	    int numFields = rset.getNumFields();
	    	    
	    buffer.append("\""+StringUtils.encodeJavascriptString(name)+"\"");
	    buffer.append(":[");
	    
	    String key;
	    String value = null;
	    FieldInfo finfo = new FieldInfo();	 
	    String prefix = "";	    
	    String outerPrefix = "";
	    
	    for(rset.first(); rset.isRowPresent();rset.next() )
	    {
		buffer.append(outerPrefix);
		outerPrefix = ",";
		buffer.append("\n\t\t{");
		prefix = "";	  
		for(int i=0; i<numFields;++i)
		{
		    key = rset.getFieldName(i);
		    value = rset.getStringValue(i);
		    rset.getFieldInfo(key, finfo);

		    buffer.append(prefix);
                    buffer.append("\""+StringUtils.encodeJavascriptString(key)+"\":");
            if(value == null){
            	buffer.append("\"\"");
            }else{
			    switch(finfo.m_type)
			    {
				case FieldInfo.BOOLEAN:
				case FieldInfo.INT:
				case FieldInfo.FLOAT:
				    buffer.append("\""+value+"\"");
				    break;
				case FieldInfo.DATE:
				    buffer.append("\""+StringUtils.encodeJavascriptString(value)+"\"");
				    break;
				default:
				    buffer.append("\""+encodeJavascriptString(value)+"\"");
				    break;
			    }
             }
                    prefix = ",";
		}
                buffer.append("}");
	    }	    
	    buffer.append("\n\t\t]");	    
	}
	

	
	public static Object idcVersionInfo(Object arg)
	{
		return "releaseInfo=dev,releaseRevision=$Rev: 49099 $";
	}
	
	public static String encodeJavascriptString(String paramString)
	   {
	     paramString = encodeLiteralStringEscapeSequence(paramString);
	     IdcStringBuilder localIdcStringBuilder = new IdcStringBuilder(paramString.length() * 4);
	
	     for (int j = 0; j < paramString.length(); ++j)
	     {
	    	 int i = paramString.charAt(j);
	    	 if (i > 4095)
	    	 {
	    		 localIdcStringBuilder.append("\\u").append(Integer.toHexString(i));
	    	 }
	       else if (i > 255)
	       {
	         localIdcStringBuilder.append("\\u0").append(Integer.toHexString(i));
	       }
	       else if ((i > 127) || (i == 60) || (i == 62))
	       {
	    	   localIdcStringBuilder.append("\\u00").append(Integer.toHexString(i));
	       }
	       else
	       {
	         localIdcStringBuilder.append((char)i);
	       }
	     }
	 
	     return localIdcStringBuilder.toString();
	   }
	
	public static  String encodeLiteralStringEscapeSequence(String paramString)
	{
		int i = paramString.length();
		IdcStringBuilder localIdcStringBuilder = new IdcStringBuilder();
		localIdcStringBuilder.ensureCapacity(i + 8);
	
		for (int j = 0; j < i; ++j)
		{
			char c = paramString.charAt(j);
			switch (c)
			{
				case '\\':
					localIdcStringBuilder.append("\\\\");
					break;
				case '"':
					localIdcStringBuilder.append("\\\"");
					break;				
				case '\t':
					localIdcStringBuilder.append("\\t");
					break;
				case '\n':
					localIdcStringBuilder.append("\\n");
					break;
				case '\r':
					localIdcStringBuilder.append("\\r");
					break;
				default:
					localIdcStringBuilder.append(c);
			}
		}
	
	return localIdcStringBuilder.toString();
	}
}
