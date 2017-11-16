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
 * $Id: PageBuilderSerializer.java 48374 2006-10-25 18:17:32Z vowuor $
 */
package intradoc.server;


import intradoc.common.*;
import intradoc.data.*;
import java.io.IOException;

public class PageBuilderSerializer
{
    
    static public byte[] sendResponseBytes(DataBinder data, ExecutionContext cxt) throws IOException, ServiceException
    {
    	String javaEncoding = DataSerializeUtils.determineEncoding(data, null);
    	try
    	{
    	return sendResponseBytes(data, cxt, javaEncoding);
    	}
    	catch(ParseSyntaxException e)
    	{
    	    throw new ServiceException(e);
    	}
    }

    static public byte[] sendResponseBytes(DataBinder data, ExecutionContext cxt,
		String javaEncoding) throws IOException, ParseSyntaxException, ServiceException
    {
	Service service;
	PageMerger pageMerger;
	
	if(cxt instanceof Service)
	{
	    service = (Service) cxt;
	    pageMerger = service.getPageMerger();
	}
	else
	{
	    return new byte[0];
	}
	String isoEncoding = null;
	
	if (javaEncoding != null)
	{
		data.setEnvironmentValue("ClientEncoding", javaEncoding);
		isoEncoding = DataSerializeUtils.getIsoEncoding(javaEncoding);
	}
	if (isoEncoding != null)
	{
		data.setEnvironmentValue("PageCharset", isoEncoding);
	}
	
	// First do the non error case.  Note: in theory determineResponsePage
	// might use the values of ClientEncoding and PageCharset above
	// to determine which template to return, so we need to do the
	// initial computation above and then potentially override the
	// choice with the dynamic html's source choice.
	DynamicHtml dynHtml = null;
	
	dynHtml = service.determineResponsePage(false);
	
	
	// If the source file is UTF-8 for the response page, then this
	// is the one scenario where we allow the original content encoding
	// to win over the client's chosen value.  Allowing override in
	// other scenarios may allow resource includes or other dynamically
	// generated content (such a search results) to not present correctly.
	// If a customer wishes to have complicated mixtures of character encodings,
	// they should be willing for UTF-8 to win in cases where there may
	// be confusion as to the best character encoding.
	if (dynHtml.m_sourceEncoding != null && 
		dynHtml.m_sourceEncoding.equalsIgnoreCase("UTF8"))
	{
		javaEncoding = "UTF8";
		isoEncoding = "UTF-8";  // Use hardwired knowledge of lookup value.
		data.setEnvironmentValue("ClientEncoding", "UTF8");
		data.setEnvironmentValue("PageCharset", "UTF-8");
	}
	
	long startTime = 0;
	boolean isTracingPageCreation = SystemUtils.isActiveTrace("pagecreation");
	if (isTracingPageCreation)
	{
		startTime = System.currentTimeMillis();
	}
	
	// obtain the result page 
	String resultPage = null;
	resultPage = pageMerger.outputDynamicHtmlPage(dynHtml);
	

	if (isTracingPageCreation)
	{
		long endTime = System.currentTimeMillis();
		long diff = endTime - startTime;
		SystemUtils.trace("pagecreation", "Time to generate page: " + diff);
		IdcStringBuilder buf = new IdcStringBuilder();
		pageMerger.appendPageStatistics(buf);
		SystemUtils.trace("pagecreation", buf.toString());
	}

	return (StringUtils.getBytes(resultPage, javaEncoding) );
	
    }
}
