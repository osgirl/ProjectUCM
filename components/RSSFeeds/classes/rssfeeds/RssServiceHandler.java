/******************************************************************************/
/*                                                                            */
/*  Stellent, Incorporated Confidential and Proprietary                       */
/*                                                                            */
/*  This computer program contains valuable, confidential and proprietary     */
/*  information.  Disclosure, use, or reproduction without the written        */
/*  authorization of IntraNet Solutions is prohibited.  This unpublished      */
/*  work by IntraNet Solutions is protected by the laws of the United States  */
/*  and other countries.  If publication of the computer program should occur,*/
/*  the following notice shall apply:                                         */
/*                                                                            */
/*  Copyright (c) 1997-2001 IntraNet Solutions, Incorporated.  All rights	  */
/*	reserved.                                                                 */
/*  Copyright (c) 2001-2006 Stellent, Incorporated.  All rights reserved.     */
/*                                                                            */
/******************************************************************************/

package rssfeeds;

import intradoc.server.ServiceHandler;
import intradoc.common.*;
import intradoc.shared.*;
import intradoc.data.*;
import intradoc.resource.*;

/**
 * Contains code to allow RSS-based services.
 */
public class RssServiceHandler extends ServiceHandler 
{
    public RssServiceHandler()
    {
    }

    /**
     * Download a RSS feed, cache it, and return it to the user. The parameter
     * 'feedUrl' contains the URL to the RSS feed. 'parseXml' defaults to true,
     * otherwise the raw XML string is returned in the parameter 'rawXmlData'.
     * 'feedMaxAge' contains info on how long to cache the feed, in seconds.
     * @throws ServiceException
     * @throws DataException
     */
    public void getRssFeedForUrl() throws ServiceException, DataException 
	{
    	String feedUrl = m_binder.get("feedUrl");
    	String parseXmlStr = m_binder.getLocal("parseXml");
    	boolean parseXml = StringUtils.convertToBool(parseXmlStr, true);   
    	String rawXmlData = null;
    	
    	// determine how long to cache this RSS feed
    	String feedMaxAgeStr = m_binder.getLocal("feedMaxAge");
    	int feedMaxAge = 600;
    	if (feedMaxAgeStr == null)
    		feedMaxAgeStr = SharedObjects.getEnvironmentValue("RssFeedMaxAge");    	
    	if (feedMaxAgeStr != null)
    		feedMaxAge = NumberUtils.parseInteger(feedMaxAgeStr, feedMaxAge);
    	
    	rawXmlData = RssHelper.getXmlHttpContentsAsString(feedUrl, feedMaxAge);
    	
    	if (parseXml)
    	{
    		RssHelper.parseRssIntoBinder(rawXmlData, m_binder);
    	}
    	else
    	{
    		m_binder.putLocal("rawXmlData", rawXmlData);
    	}
	}
}
