package hays.co.uk;

import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.data.DataException;
import intradoc.data.ResultSet;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;

public class GoogleSiteMapBuilder
{
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm+00:00");
	public static StringBuffer SITE_MAP_FIRST_LINE = new StringBuffer("<?xml version='1.0' encoding='UTF-8'?>").append("<urlset xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ")
			.append(" xsi:schemaLocation=\"http://www.sitemaps.org/schemas/sitemap/0.9 http://www.sitemaps.org/schemas/sitemap/0.9/sitemap.xsd\"")
			.append(" xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">");

	protected boolean m_toAdditionalContent = false;
	protected Hashtable<String, String> m_latestSectionsUpdatesMap = new Hashtable<String, String>();
	protected String m_siteId = null;
	protected String m_siteUrl = null;
	protected String m_siteLocale = null;

	public GoogleSiteMapBuilder(String siteId, String siteUrl, Hashtable<String, String> latestSectionsUpdatesMap)
	{
		this.m_siteId = siteId;
		this.m_siteUrl = siteUrl;
		if (latestSectionsUpdatesMap != null)
		{
			this.m_latestSectionsUpdatesMap = latestSectionsUpdatesMap;
		}
	}

	/**
	 * Builds SteMap of the given site that in Google SiteMap format
	 * @param siteId
	 * @return Formatted sitemap
	 */
	public StringBuffer buildGoogleSiteMap(ResultSet navigationRS) throws DataException, ServiceException
	{
		StringBuffer writer = new StringBuffer();
		String nodeId, friendlyUrl, websiteSectionId = null;
		String priority = "0.5";
		String level = null;
		String nodeLink = null;

		debug("\n buildFullSiteMap() starting for " + m_siteId);
		String nowDate = DATE_FORMAT.format(new Date());

		writer.append(SITE_MAP_FIRST_LINE);
		if (navigationRS != null && navigationRS.first())
		{
			debug("siteUrl: " + m_siteUrl);
			debugKeyPoints("Printing additional contents.");
			String additional = null;
			do
			{

				nodeId = navigationRS.getStringValue(0); // nodeId
				nodeLink = navigationRS.getStringValueByName("href");
				if (nodeLink != null && !nodeLink.equals("null"))
				{
					friendlyUrl = m_siteUrl + navigationRS.getStringValueByName("href");
					level = navigationRS.getStringValueByName("level");
					if (level.equals("0") || level.equals("1"))
					{
						priority = "0.9";
					}
					else
					{
						priority = "0.5";
					}
					websiteSectionId = m_siteId + ":" + nodeId;
					if (m_latestSectionsUpdatesMap.containsKey(websiteSectionId))
					{
						nowDate = m_latestSectionsUpdatesMap.get(websiteSectionId);
					}
					writer.append(populateSiteMap(friendlyUrl, priority, nowDate));
					// if some additional content has to be included
					if (m_toAdditionalContent)
					{
						additional = includeAdditionalContent(nodeId);
						if (additional != null)
						{
							writer.append(additional);
						}
					}

				}

			}
			while (navigationRS.next());
			debugKeyPoints("Printing jobs from indexer.");
			String indexerContents = includeIndexerContents();
			if (indexerContents != null)
			{
				writer.append(indexerContents);
			}

			String leftOvers = includeLeftOvers();
			if (leftOvers != null)
			{
				writer.append(leftOvers);
			}
			String localJobsURL =includeLocalJobsURL();
			if (localJobsURL != null && !"".equals(localJobsURL))
			{
				writer.append(localJobsURL);
			}
		}
		writer.append("</urlset>");
		return writer;
	}

	protected String includeAdditionalContent(String nodeId) throws ServiceException
	{
		return null;
	}

	protected String includeIndexerContents() throws ServiceException
	{
		return null;
	}

	protected String includeLeftOvers() throws ServiceException
	{
		return null;
	}
	
	protected String includeLocalJobsURL() throws ServiceException
	{
		return null;
	}

	protected String populateSiteMap(String url, String priority, String date)
	{
		StringBuffer writer = new StringBuffer();
		url = url.replaceAll("(?<!:)//", "/");
		url = this.entityEscape(url);

		writer.append("<url>").append("<loc>").append(url).append("</loc>");
		writer.append("<priority>").append(priority).append("</priority>");
		writer.append("<lastmod>").append(date.replaceAll(" ", "T")).append("</lastmod>");
		writer.append("<changefreq>monthly</changefreq>");

		writer.append("</url>\n");
		return writer.toString();
	}

	protected String entityEscape(String str)
	{
		String rez = null;
		rez = str.replaceAll("&", "&amp;");
		rez = rez.replaceAll("'", "&apos;");
		rez = rez.replaceAll("\"", "&quot;");
		rez = rez.replaceAll(">", "&gt;");
		rez = rez.replaceAll("<", "&lt;");
		return rez;
	}

	public static void debug(String message)
	{
		SystemUtils.trace("sitemap", message);
	}

	public static void debugKeyPoints(String message)
	{
		SystemUtils.trace("sitemapkeypoints", message);
		SystemUtils.trace("sitemap", message);
	}

	public static void debug(Exception ex)
	{
		SystemUtils.trace("sitemap", "\nException :" + ex);
		ex.printStackTrace();
	}

	public String getM_siteLocale()
	{
		return m_siteLocale;
	}

	public void setM_siteLocale(String m_siteLocale)
	{
		this.m_siteLocale = m_siteLocale;
	}
}
