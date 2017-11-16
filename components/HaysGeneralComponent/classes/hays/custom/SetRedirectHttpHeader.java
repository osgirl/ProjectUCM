package hays.custom;

import intradoc.shared.*;
import intradoc.server.*;
import intradoc.data.*;
import intradoc.common.*;
import intradoc.resource.*;
import java.io.*;
import java.util.*;

public class SetRedirectHttpHeader implements FilterImplementor 
{
	public int doFilter(Workspace ws, DataBinder binder, ExecutionContext cxt)
	throws DataException, ServiceException
	{
		if (cxt == null)
		{
			System.out.println("DetectHTTPHeader: Plugin filter called without a context.");
			return CONTINUE;
		}

		Object paramObj = cxt.getCachedObject("filterParameter");
		if (paramObj == null || (paramObj instanceof String) == false)
		{
			System.out.println("DetectHTTPHeader: Plugin filter called without filter parameter.");
			System.out.println("paramObj" + paramObj);
			return CONTINUE;
		}

		String param = (String)paramObj;
		Service service = (Service)cxt;
		SystemUtils.trace("SetRedirectHttpHeader", "Inside  SetRedirectHttpHeader");
		if (param.equals("editHttpResponseHeader"))
	    {
			boolean changeHeaderResponse = StringUtils.convertToBool( binder.getLocal("ssRedirectHTTPHeader"), false);
			String lHTTPHeaderStatus = binder.getLocal("HTTPHeaderStatusValue");
			SystemUtils.trace("SetRedirectHttpHeader", "changeHeaderResponse "+changeHeaderResponse+" lHTTPHeaderStatus "+lHTTPHeaderStatus);
			if( changeHeaderResponse)
			{
				SystemUtils.trace("SetRedirectHttpHeader", "changeHeaderResponse "+changeHeaderResponse);
				Object serviceHttpImplementorObj = cxt.getCachedObject("HttpImplementor");
				Object responseBufferObj = cxt.getCachedObject("responseBuffer");

				if( serviceHttpImplementorObj == null || responseBufferObj == null ||
				  	(responseBufferObj instanceof StringBuffer) == false ||
				  	(serviceHttpImplementorObj instanceof ServiceHttpImplementor) == false )
				{
					return CONTINUE;
				}
	
				ServiceHttpImplementor serviceHttpImplementor = (ServiceHttpImplementor) serviceHttpImplementorObj;
				StringBuffer responseBuffer = (StringBuffer) responseBufferObj;
	
				// First we look for and remove the end header marker if its there.
				String oldHeader = responseBuffer.toString().toUpperCase();
				String newHeader = "";
				String endHeaderMarker = "\r\n\r\n";
				int endHeaderPos = oldHeader.lastIndexOf(endHeaderMarker);
				if( endHeaderPos >= 0 )
					newHeader = responseBuffer.toString().substring(0,endHeaderPos) + "\r\n";
				else
					newHeader = responseBuffer.toString();
	
				responseBuffer.setLength(0);
	
				String newStatus =  "HTTP/1.1 301 Redirect";
				String oldStatus1 = "HTTP/1.1 303 See other";
				String oldStatus2 = "HTTP/1.0 303 See other";
				int	nPos = newHeader.indexOf(oldStatus1);
				if( nPos == -1 )
					nPos = newHeader.indexOf(oldStatus2);
				if( nPos == -1 )
					changeHeaderResponse = false;
	
				if (changeHeaderResponse)
				{
	         		// then replace the the old with  the new status
					SystemUtils.trace("SetRedirectHttpHeader", "then replace the the old with  the new status "+changeHeaderResponse);
					if( nPos >= 0 )
					{
						SystemUtils.trace("SetRedirectHttpHeader", "Replace the existing response "+nPos);
						// Replace the existing response
						responseBuffer.append(newHeader.substring(0,nPos));
						responseBuffer.append(newStatus);
						responseBuffer.append(newHeader.substring(nPos + oldStatus1.length()));
					}
					else
					{
						SystemUtils.trace("SetRedirectHttpHeader", "Not found, just add the new header "+nPos);
						// Not found, just add the new header
						responseBuffer.append(newHeader);
						responseBuffer.append(newStatus);
						responseBuffer.append("\r\n");
					}
				}
				else
				{
					responseBuffer.append(newHeader);
				}
				responseBuffer.append("\r\n");
		    }
	    }
		return CONTINUE;
	    
	}
}

