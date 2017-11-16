package changeHttpHeader;

import intradoc.shared.*;
import intradoc.server.*;
import intradoc.data.*;
import intradoc.common.*;
import intradoc.common.IdcStringBuilder;
import intradoc.server.ServiceHttpImplementor;

public class detectHttpHeader implements FilterImplementor 
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

		if (param.equals("editHttpResponseHeader"))
	    {
			SystemUtils.trace("http_header_change", "Inside change of HTTP Header:::");
			boolean changeHeaderResponse = StringUtils.convertToBool( binder.getLocal("ssChangeHTTPHeader"), false);
			boolean changeContentTypeResponse = StringUtils.convertToBool( binder.getLocal("ssChangeTypeHeader"), false);
			
			SystemUtils.trace("http_header_change", "Value of ssChangeTypeHeader:::"+changeContentTypeResponse);
			SystemUtils.trace("http_header_change", "Value of ssChangeHTTPHeader:::"+changeHeaderResponse);
			if( changeHeaderResponse || changeContentTypeResponse){
				SystemUtils.trace("http_header_change", "Inside if loop for changeHeaderResponse:::");
				Object serviceHttpImplementorObj = cxt.getCachedObject("HttpImplementor");
				Object responseBufferObj = cxt.getCachedObject("responseBuffer");

				IdcStringBuilder localIdcStringBuilder = null;
		        if ((responseBufferObj != null) && (responseBufferObj instanceof IdcStringBuilder))
		        {
		          localIdcStringBuilder = (IdcStringBuilder)responseBufferObj;
		          responseBufferObj = new StringBuffer((String)localIdcStringBuilder.toStringNoRelease());
		        }
		        
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
				SystemUtils.trace("http_header_change", "Value of old header is:::"+oldHeader);
				String newHeader = "";
				
				String endHeaderMarker = "\r\n\r\n";
				
				int endHeaderPos = oldHeader.lastIndexOf(endHeaderMarker);
				SystemUtils.trace("http_header_change", "Index of content type in old header is:::"+endHeaderPos);
				
				if( endHeaderPos >= 0 )
					newHeader = responseBuffer.toString().substring(0,endHeaderPos) + "\r\n";
				else
					newHeader = responseBuffer.toString();
				SystemUtils.trace("http_header_change", "New Header is:::"+newHeader);
				
	
				responseBuffer.setLength(0);
	
				String newStatus =  "HTTP/1.1 404 File not found";
				String statusFromBinder = binder.getLocal("HaysNewHTTPStatus");
				if(statusFromBinder !=null && !"".equals(statusFromBinder)){
					newStatus = statusFromBinder;
					SystemUtils.trace("http_header_change", "statusFromBinder:::"+statusFromBinder);
				}
				String oldStatus1 = "HTTP/1.0 200 OK";
				String haysOldHTTPStatus1FromBinder = binder.getLocal("HaysOldHTTPStatus1");
				if(haysOldHTTPStatus1FromBinder !=null && !"".equals(haysOldHTTPStatus1FromBinder)){
					oldStatus1 = haysOldHTTPStatus1FromBinder;
					SystemUtils.trace("http_header_change", "haysOldHTTPStatus1FromBinder:::"+haysOldHTTPStatus1FromBinder);
				}
				
				String oldStatus2 = "HTTP/1.1 200 OK";
				String haysOldHTTPStatus2FromBinder = binder.getLocal("HaysOldHTTPStatus1");
				if(haysOldHTTPStatus2FromBinder !=null && !"".equals(haysOldHTTPStatus2FromBinder)){
					oldStatus2 = haysOldHTTPStatus2FromBinder;
					SystemUtils.trace("http_header_change", "haysOldHTTPStatus2FromBinder:::"+haysOldHTTPStatus2FromBinder);
				}
				
				String oldTypeStatus = "text/html";
				String newTypeStatus = "text/plain";
				int	nPos = newHeader.indexOf(oldStatus1);
				SystemUtils.trace("http_header_change", "Position:::"+nPos);
				int	nTypePos = newHeader.indexOf(oldTypeStatus);
				SystemUtils.trace("http_header_change", "Position of content type is:::"+nTypePos);
				
				if( nPos == -1 )
					nPos = newHeader.indexOf(oldStatus2);
				SystemUtils.trace("http_header_change", "Position2:::"+nPos);
				if( nPos == -1 )
					changeHeaderResponse = false;
				
				SystemUtils.trace("http_header_change", "changeHeaderResponse:::"+changeHeaderResponse);
				if (changeHeaderResponse)
				{
	         		// then replace the the old with  the new status
					if( nPos >= 0 )
					{
						// Replace the existing response
						responseBuffer.append(newHeader.substring(0,nPos));
						responseBuffer.append(newStatus);
						responseBuffer.append(newHeader.substring(nPos + oldStatus1.length()));
					}
					else
					{
						// Not found, just add the new header
						responseBuffer.append(newHeader);
						responseBuffer.append(newStatus);
						responseBuffer.append("\r\n");
					}
				}
				
				else if (changeContentTypeResponse){
				
	         		// then replace the the old with  the new status
					if( nTypePos >= 0 )
					{
						// Replace the existing response
						responseBuffer.append(newHeader.substring(0,nTypePos));
						responseBuffer.append(newTypeStatus);
						responseBuffer.append(newHeader.substring(nTypePos + oldTypeStatus.length()));
						
					}
					else
					{
						// Not found, just add the new header
						responseBuffer.append(newHeader);
						responseBuffer.append(newTypeStatus);
						responseBuffer.append("\r\n");
					}
				}
				
				else{
					responseBuffer.append(newHeader);
				}
							
				responseBuffer.append("\r\n");
				if (localIdcStringBuilder != null)
		        {
		          localIdcStringBuilder.setLength(0);
		          localIdcStringBuilder.append(responseBuffer);
		        }
		    }
	    }
		return CONTINUE;
	    
	}
}
