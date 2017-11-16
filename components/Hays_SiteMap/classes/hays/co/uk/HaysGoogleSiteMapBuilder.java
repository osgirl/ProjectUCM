package hays.co.uk;

import intradoc.common.ExecutionContext;
import intradoc.common.ExecutionContextAdaptor;
import intradoc.common.IdcLocale;
import intradoc.common.LocaleResources;
import intradoc.common.ServiceException;
import intradoc.server.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import sitestudio.SSLinkFunctions;

public class HaysGoogleSiteMapBuilder extends GoogleSiteMapBuilder
{
	ExecutionContext ctx = new ExecutionContextAdaptor();
	private List<Hashtable<String, String>> m_additionalContentMapList = null;
	private ArrayList<String> m_indexerContents = new ArrayList<String>();
	private HashMap<String, String> m_genericContentMap = null;
	private Service m_service = null;
	private String defaultSectionId = null;
	String m_projectSustomProperty = null;
	String m_conTypeSystemProperty = null;
	String m_conSubTypeSystemProperty = null;
	String[] listOfValues, listOfContentValues = null;
	String dDocName, dInDate, xWebsiteSection, url, siteMapString = null;

	public HaysGoogleSiteMapBuilder(String siteId, String siteUrl, Hashtable<String, String> latestSectionsUpdatesMap, Service service, String property, String conTypeProp, String conSubTypeProp)
	{
		super(siteId, siteUrl, latestSectionsUpdatesMap);
		super.m_toAdditionalContent = true;
		this.m_service = service;
		this.m_projectSustomProperty = property;
		this.m_conTypeSystemProperty = conTypeProp;
		this.m_conSubTypeSystemProperty = conSubTypeProp;
	}

	/**
	 * For sections, whose Project Custom Property (env. variable) was set, we need to retrieve related
	 * content - content with metadata with the same name and value as the Project Custom Property.
	 * Query returns 2 column: metadata value (METATERM) and a list of associated dDocName@xWebsiteSection@dInDate (DDATE)
	 * 
	 * Note: xWebsiteSection is used to build this content url. If it's unknown than the defauld section (Env. variable)
	 * is used. TBC!
	 */
	protected String includeAdditionalContent(String nodeId) throws ServiceException
	{
		StringBuffer writer = new StringBuffer();
		Hashtable<String, String> map = null;
		String value = null;

		SiteMapHandler sitemaphandler = (SiteMapHandler) m_service.getHandler("hays.co.uk.SiteMapHandler");
		String customPropValue = sitemaphandler.getNodeProperty(m_siteId, nodeId, m_projectSustomProperty, true);
		if (customPropValue != null && customPropValue.length() > 0)
		{
			customPropValue = customPropValue.replaceAll(";", "");
			debug("includeAdditionalContent() : " + nodeId + " - " + customPropValue);
			for (Iterator<Hashtable<String, String>> iter = m_additionalContentMapList.iterator(); iter.hasNext();)
			{
				map = iter.next();
				if (map.containsKey(customPropValue))
				{
					value = map.get(customPropValue);
					writer.append(calculatePriorityAndURLMulti(value));
				}
			}
		}
		customPropValue = sitemaphandler.getNodeProperty(m_siteId, nodeId, m_conSubTypeSystemProperty, true);
		if ((customPropValue == null || "".equals(customPropValue)) || !m_genericContentMap.containsKey(customPropValue.trim()))
		{
			customPropValue = sitemaphandler.getNodeProperty(m_siteId, nodeId, m_conTypeSystemProperty, true);
		}
		if (customPropValue != null && customPropValue.length() > 0)
		{

			if (m_genericContentMap.containsKey(customPropValue.trim()))
			{
				debug("Printing all the " + customPropValue + "for node " + nodeId);
				writer.append(calculatePriorityAndURLMulti(m_genericContentMap.get(customPropValue)));
				m_genericContentMap.remove(customPropValue);
			}
		}
		return writer.toString();
	}

	protected String includeLeftOvers() throws ServiceException
	{
		StringBuffer writer = new StringBuffer();
		if (!m_genericContentMap.isEmpty())
		{
			for (String key : m_genericContentMap.keySet())
			{
				debug("Printing left overs named " + key);
				writer.append(calculatePriorityAndURLMulti(m_genericContentMap.get(key)));
			}
		}
		return writer.toString();
	}

	protected String includeIndexerContents() throws ServiceException
	{
		StringBuffer writer = new StringBuffer();
		for (String singleValue : m_indexerContents)
		{
			debug("Printing indexer content named " + singleValue);
			writer.append(calculatePriorityAndURLSingle(singleValue));
		}
		return writer.toString();
	}

	protected String includeLocalJobsURL() throws ServiceException
	{
		debug("Printing local jobs url ");
		setLocale(m_siteLocale);
		String localJobsURL = LocaleResources.getString("wwLocalJobsURL", ctx);

		if (localJobsURL != null && !"".equals(localJobsURL.trim()) && !"wwLocalJobsURL".equalsIgnoreCase(localJobsURL.trim()))
		{
			url = m_siteUrl + localJobsURL;
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm+00:00");
			dInDate = simpleDateFormat.format(new Date());
			siteMapString = populateSiteMap(url, "0.5", dInDate);
			debug("Local Jobs String : " + siteMapString);
			return siteMapString;
		}
		else
		{
			debug("Local Jobs String is not available for this country.");
			return "";
		}
	}

	protected String calculatePriorityAndURLMulti(String pMultiValue)
	{
		StringBuilder siteMapString = new StringBuilder();
		String[] listOfValues = pMultiValue.split(",");
		debug("length = " + listOfValues.length + "  value is  : " + pMultiValue);
		for (int i = 0; i < listOfValues.length; i++)
		{
			siteMapString.append(calculatePriorityAndURLSingle(listOfValues[i]));
		}
		return siteMapString.toString();
	}

	protected String calculatePriorityAndURLSingle(String pSingleValue)
	{
		listOfContentValues = pSingleValue.split("@");
		dDocName = listOfContentValues[0];
		xWebsiteSection = listOfContentValues[1];
		dInDate = listOfContentValues[2];
		if (xWebsiteSection.length() == 0)
		{
			xWebsiteSection = defaultSectionId;
		}
		else
		{
			xWebsiteSection = xWebsiteSection.substring(xWebsiteSection.indexOf(":") + 1);
		}
		String contentRelativeLink = SSLinkFunctions.computeLinkUrl(this.m_service, dDocName, xWebsiteSection, m_siteId, true);
		debug("SSLinkFunctions.computeLinkUrl(this.m_service, " + dDocName + ", " + xWebsiteSection + ", " + m_siteId + ", true)");
		if (contentRelativeLink != null && !contentRelativeLink.equals("null"))
		{
			url = m_siteUrl + contentRelativeLink;
			siteMapString = populateSiteMap(url, "0.5", dInDate);
		}
		return siteMapString;
	}

	public void setProjectCustomProperty(String property)
	{
		this.m_projectSustomProperty = property;
	}

	public List<Hashtable<String, String>> getAdditionalContentMapList()
	{
		return m_additionalContentMapList;
	}

	public void setAdditionalContentMapList(List<Hashtable<String, String>> contentMapList)
	{
		m_additionalContentMapList = contentMapList;
	}

	public String getDefaultSectionId()
	{
		return defaultSectionId;
	}

	public void setDefaultSectionId(String defaultSectionId)
	{
		this.defaultSectionId = defaultSectionId;
	}

	public ArrayList<String> getIndexerContents()
	{
		return m_indexerContents;
	}

	public void setIndexerContents(ArrayList<String> m_indexerContents)
	{
		this.m_indexerContents = m_indexerContents;
	}

	public HashMap<String, String> getGenericContentMap()
	{
		return m_genericContentMap;
	}

	public void setGenericContentMap(HashMap<String, String> m_genericContentMap)
	{
		this.m_genericContentMap = m_genericContentMap;
	}

	public void setLocale(String locale)
	{
		debug("Value of locale is : " + locale);
		if (locale == null || locale.length() <= 0)
		{
			locale = "en-GB";
			debug("Value of locale is : default : " + m_siteLocale);
		}
		IdcLocale idcl = LocaleResources.getLocale(locale);
		ctx.setCachedObject("UserLocale", idcl);
	}
}
