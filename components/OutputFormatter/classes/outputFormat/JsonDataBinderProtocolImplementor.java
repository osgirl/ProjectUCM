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
 * $Id: DataBinderProtocolImplementor.java 49099 2006-09-27 15:26:03Z swhite $
 */

package outputFormat;

import java.io.*;

import intradoc.common.*;
import intradoc.data.*;
import intradoc.shared.*;
import intradoc.soap.*;
import intradoc.server.*;

public class JsonDataBinderProtocolImplementor implements DataBinderProtocolInterface
{
	protected Workspace m_workspace = null;
		
	public void init(Workspace ws, DataBinder data, ExecutionContext cxt)
		throws IOException, DataException, ServiceException
	{
		m_workspace = ws;
		cxt = getExecutionContext(cxt);
		
		PluginFilters.filter("initDataBinderProtocol", ws, data, cxt);		
	}
	
	public boolean parseRequest(DataBinder data, ExecutionContext cxt)
		throws IOException, DataException
	{
		cxt = getExecutionContext(cxt);
		
		try
		{
			int filter = PluginFilters.filter("parseDataForServiceRequest", 
				m_workspace, data, cxt);
			if (filter == FilterImplementor.FINISHED)
			{
				return true;
			}
		}
		catch(ServiceException e)
		{
			throw new DataException(e.getMessage());
		}
		return false;
	}
	
	public boolean continueParse(DataBinder data, ExecutionContext cxt)
		throws IOException, DataException
	{
		cxt = getExecutionContext(cxt);
		
		try
		{
			int filter = PluginFilters.filter("continueParseDataForServiceRequest", m_workspace, 
				data, cxt);
			if (filter == FilterImplementor.FINISHED)
			{
				return true;
			}
		}
		catch (ServiceException e)
		{
			SystemUtils.dumpException(null, e);
			throw new DataException(e.getMessage());
		}		
		return false;
	}
	
	public void postParseRequest(DataBinder data, ExecutionContext cxt)
		throws DataException
	{
		cxt = getExecutionContext(cxt);
		
		try
		{
			PluginFilters.filter("postParseDataForServiceRequest", m_workspace, 
				data, cxt);
		}
		catch (ServiceException e)
		{
			SystemUtils.dumpException(null, e);
			throw new DataException(e.getMessage());
		}
		
	}
	
	public byte[] sendResponseBytes(DataBinder data, ExecutionContext cxt,
		String encoding) throws IOException
	{
		cxt = getExecutionContext(cxt);
		cxt.setCachedObject("encoding", encoding);

		byte[] responseBytes = null;
		try
		{
			int filter = PluginFilters.filter("sendDataForServerResponseBytes", 
				m_workspace, data, cxt);
			if (filter == FilterImplementor.FINISHED)
			{
				Object responseObj = cxt.getCachedObject("responseBytes");
				if ((responseObj != null) && (responseObj instanceof byte[]))
				{
					responseBytes = (byte[])responseObj;
					return responseBytes;
				}
			}
		}
		catch(Exception e)
		{
			IOException ioE = new IOException(e.getMessage());
			SystemUtils.setExceptionCause(ioE, e);
			throw ioE;
		}

		byte[] jsonResponseBytes = null;
		if ( DataUtils.getBoolean(data, "IS_JSON_REQUEST", false))
		{
		    jsonResponseBytes = JsonSerializer.sendResponseBytes(data, cxt, encoding);
		}
		return jsonResponseBytes;
	}
	
	public String sendResponse(DataBinder data, ExecutionContext cxt) 
		throws IOException
	{
		cxt = getExecutionContext(cxt);

		try
		{
			int filter = PluginFilters.filter("sendDataForServerResponse", 
				m_workspace, data, cxt);
			if (filter == FilterImplementor.FINISHED)
			{
				Object responseObj = cxt.getCachedObject("responseString");
				if ((responseObj != null) && (responseObj instanceof String))
				{
					String responseStr = (String)responseObj;
					return responseStr;
				}
			}
		}
		catch(Exception e)
		{
			IOException ioE = new IOException(e.getMessage());
			SystemUtils.setExceptionCause(ioE, e);
			throw ioE;
		}
		String jsonResponse = null;
		if ( DataUtils.getBoolean(data, "IS_JSON_REQUEST", false))
		{
		    jsonResponse = JsonSerializer.sendResponse(data, cxt);
		}
		return jsonResponse;
	}

	public boolean sendFileResponse(DataBinder data, ExecutionContext cxt,
		String fileName, String downloadName, String format) throws IOException
	{
		DataStreamWrapper streamWrapper = new DataStreamWrapper(fileName, downloadName,
				format);
		return sendStreamResponse(data, streamWrapper, cxt);
	}

	public boolean sendStreamResponse(DataBinder data, DataStreamWrapper streamWrapper,
			ExecutionContext cxt) throws IOException
	{
		ServiceHttpImplementor httpImplementor = (ServiceHttpImplementor)
			cxt.getCachedObject("HttpImplementor");
		Service service = (Service)cxt;
		
		boolean isSoapFileResponse = false;
		if (SoapSerializer.m_isValid)
		{
			isSoapFileResponse = SoapSerializer.sendStreamResponse(data,
				service, streamWrapper, httpImplementor);
		}
		return isSoapFileResponse;		

	}

	protected ExecutionContext getExecutionContext(ExecutionContext cxt)
	{
		if (cxt == null)
		{
			cxt = new ExecutionContextAdaptor();
		}
		
		return cxt;
	}
	
	public static Object idcVersionInfo(Object arg)
	{
		return "releaseInfo=dev,releaseRevision=$Rev: 49099 $";
	}

	@Override
	public void postContinueParse(DataBinder data, ExecutionContext cxt) 
			throws IOException, DataException, ServiceException
	{
		cxt = getExecutionContext(cxt);
		
		try
		{
			PluginFilters.filter("postContinueParseDataForServiceRequest", m_workspace, 
				data, cxt);
		}
		catch (ServiceException e)
		{
			SystemUtils.dumpException(null, e);
			throw new DataException(e.getMessage());
		}
		
	}
}
