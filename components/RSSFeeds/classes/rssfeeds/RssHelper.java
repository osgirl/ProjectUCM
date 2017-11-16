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

import intradoc.resource.ResourceCacheInfo;
import intradoc.resource.ResourceCacheState;
import intradoc.server.ServiceHandler;
import intradoc.common.LocaleUtils;
import intradoc.common.NumberUtils;
import intradoc.common.ServiceException;
import intradoc.data.*;
import intradoc.common.*;
import intradoc.shared.*;

import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndImage;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import java.io.*;
import java.net.*;
import java.util.*;


/**
 * Helper class used to download and cache XML data
 */
public class RssHelper
{
	/**
	 * Downloads the contents of an XML file over HTTP, based on a URL.
	 * Returns a String with the contents. It pulls the data from a cache
	 * if possible.
	 * @param feedUrl
	 * @param feedMaxAge
	 * @return
	 * @throws DataException
	 */
	public static String getXmlHttpContentsAsString(String feedUrl, int feedMaxAge)
		throws DataException
	{
		String rawXmlData = null;
		Exception err = null;
		long currTime = System.currentTimeMillis();
		ResourceCacheInfo rcinfo = ResourceCacheState.getTemporaryCache(feedUrl, currTime);
		if (rcinfo == null)
    	{
			XmlReader reader = null;
			StringBuffer response = new StringBuffer(8192);
			char chars[] = new char[8192];
			try
			{
				URLConnection uc = (new URL(feedUrl)).openConnection();
				reader = new XmlReader(uc.getInputStream());
				int numRead = 0;
				while((numRead = reader.read(chars)) > 0)
				{
					response.append(chars, 0, numRead);
				}
				rawXmlData = response.toString();

				// place it into a cache
				rcinfo = new ResourceCacheInfo(feedUrl);
				rcinfo.m_resourceObj = rawXmlData;
				rcinfo.m_removalTS = currTime + (feedMaxAge * 1000);
				rcinfo.m_size = rawXmlData.length();
				ResourceCacheState.addTemporaryCache(feedUrl, rcinfo);
			}
			catch (Exception e)
			{
				err = e;
			}
			finally
			{
				FileUtils.closeObject(reader);
			}
    	}

		if (err != null)
			throw new DataException(err.getMessage(), err);

		rawXmlData = (String)rcinfo.m_resourceObj;
		return rawXmlData;
	}

	/**
	 * Parses a XML string containg RSS formatted data, and inserts that data
	 * into the binder. Limited support
	 * @param rssData
	 * @param binder
	 * @throws DataException
	 */
	public static void parseRssIntoBinder(String rssData, DataBinder binder)
		throws ServiceException
	{
		ServiceException se = null;
		try
		{
			SyndFeedInput input = new SyndFeedInput();
			SyndFeed feed = input.build(new StringReader(rssData));

			// prepare the date format for the feed
			String rssDateFormat = SharedObjects.getEnvironmentValue("RssDateFormat");
			if (rssDateFormat == null)
				rssDateFormat = "EEE, dd MMM yyyy HH:mm:ss";
			IdcDateFormat dateFormat = LocaleResources.
				createDateFormatFromPattern(rssDateFormat, null);

			// unfortunately, the feed object does not have a built-in iterator,
			// and values might be null, so we must do this the long way
			HashMap map = new HashMap();
			map.put("Author", feed.getAuthor());
			map.put("Description", feed.getDescription());
			map.put("Language", feed.getLanguage());
			map.put("Language", feed.getLanguage());
			map.put("Link", feed.getLink());
			map.put("Title", feed.getTitle());
			map.put("PublishedDate", dateFormat.format(feed.getPublishedDate()));
			SyndImage image = feed.getImage();
			if (image != null)
			{
				map.put("Image", image.toString());
				map.put("ImageDescription", image.getDescription());
				map.put("ImageLink", image.getLink());
				map.put("ImageTitle", image.getTitle());
				map.put("ImageUrl", image.getUrl());
			}

			// place all non-null values into the binder with a prefix
			Iterator keys = map.keySet().iterator();
			while (keys.hasNext())
			{
				String key = (String)keys.next();
				String value = (String)map.get(key);
				if (value != null)
					binder.putLocal("channel" + key, value);
			}

			// generate a list of RSS entries
			Iterator synEntries = feed.getEntries().iterator();
			DataResultSet rssEntries = new DataResultSet(new String[] {"title",
					"link", "pubDate", "author", "description"});
			while (synEntries.hasNext())
			{
				SyndEntry entry = (SyndEntry)synEntries.next();
				Vector row = rssEntries.createEmptyRow();
				row.setElementAt(entry.getTitle(), 0);
				row.setElementAt(entry.getLink(), 1);
				row.setElementAt(dateFormat.format(
						entry.getPublishedDate()), 2);
				row.setElementAt(entry.getAuthor(), 3);
				row.setElementAt(entry.getDescription().getValue(), 4);
				rssEntries.addRow(row);
			}
			binder.addResultSet("RSS_ENTRIES", rssEntries);
		}
		catch (Exception e)
		{
			SystemUtils.warn(e, "!csUnableToParseRssData");
			se = new ServiceException("!csUnableToParseRssData", e);
		}
		if (se != null)
			throw se;
	}
}
