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

import intradoc.common.*;
import intradoc.data.*;
import intradoc.resource.ResourceCacheInfo;
import intradoc.resource.ResourceCacheState;
import intradoc.shared.*;
import intradoc.server.*;
import intradoc.server.script.*;

import java.io.StringWriter;
import java.io.StringReader;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import intradoc.common.ServiceException;

/**
 * This class creates new functions for working with RSS. <br>
 *
 * rssTransform(string1,string2) <br>
 * 	Where <br>
 * 	string1 = the absolute URL to the RSS Feed<br>
 * 	string2 = the absolute URL to the xsl to use for the transformation<br>
 */
public class RssIdocExtensions extends ScriptExtensionsAdaptor
{
	/**
	 * Define the IDOC Script Extensions here
	 *
	 */
	public RssIdocExtensions()
	{
		/**
		 * m_functionTable: this is a list of the functions that can be called with the custom code
		 */
		m_functionTable = new String[] {"rssTransform", "xmlDecode"};

		/**
		 * m_functionDefinitionTable
		 * Configuration data for functions.  This list must align with the "m_functionTable"
		 * list.  In order the values are "id number", "Number of arguments", "First argument type",
		 * "Second argument type", "Return Type".  Return type has the following possible
		 * values: 0 generic object (such as strings) 1 boolean 2 integer 3 double.
		 * The value "-1" means the value is unspecified.
		 */
		m_functionDefinitionTable = new int[][]
		{
			{0, 2, GrammarElement.STRING_VAL, GrammarElement.STRING_VAL, 0}, // rssTransform
			{1, 1, GrammarElement.STRING_VAL, -1, 0} // xmlDecode
		};
	}


	/**
	 * This is where the custom IdocScript function is evaluated.
	 */
	public boolean evaluateFunction(ScriptInfo info, Object[] args, ExecutionContext context)
		throws ServiceException
	{
		int config[] = (int[])info.m_entry;
		String function = info.m_key;

		int nargs = args.length - 1;
		int allowedParams = config[1];
		if (allowedParams >= 0 && allowedParams != nargs)
		{
			String msg = LocaleUtils.encodeMessage("csScriptEvalNotEnoughArgs",
				null, function, ""+allowedParams);
			throw new IllegalArgumentException(msg);
		}

		// if you need more than 3 arguments then modify this section
		String sArg1 = null;
		String sArg2 = null;
		long lArg1 = 0;
		long lArg2 = 0;
		long lArg3 = 0;
		if (nargs > 0)
		{
			if (config[2] == GrammarElement.STRING_VAL)
			{
				sArg1 = ScriptUtils.getDisplayString(args[0], context);
			}
			else if (config[2] == GrammarElement.INTEGER_VAL)
			{
				lArg1 = ScriptUtils.getLongVal(args[0], context);
			}

		}
		if (nargs > 1)
		{
			if (config[3] == GrammarElement.STRING_VAL)
			{
				sArg2 = ScriptUtils.getDisplayString(args[1], context);
			}
			else if (config[3] == GrammarElement.INTEGER_VAL)
			{
				lArg2 = ScriptUtils.getLongVal(args[1], context);
			}
		}

		boolean bResult = false;  // Used for functions that return a boolean.
		int iResult = 0; // Used for functions that return an integer.
		double dResult = 0.0;  // Used for functions that return a double.
		Object oResult = null; // Used for functions that return an object (string).
		switch (config[0])
		{
		case 0:		// rssTransform
			
			int feedMaxAge = SharedObjects.getEnvironmentInt("RssFeedMaxAge", 600);
			if (nargs == 3)								
				feedMaxAge = (int)ScriptUtils.getLongVal(args[2], context);

			if (sArg1.equals("") || sArg2.equals(""))
			{
				System.out.println("USAGE: rssTransform(string1, string2)\n" +
						"Where\n" +
						"string1 = the absolute path to the RSS Feed" +
						"string2 = the absolute path to the xsl" + 
						"int1 = the maximum age to cache the feed");

				oResult = "rssTransform: The Arguments for the function are not right.\n" +
				"currently rss feed file is " + sArg1 + ", xsl file is " + sArg2;
			}
			else
			{
				String xmlUrl = sArg1;
				String xslUrl = sArg2;

				ServiceException se = null;
				long currTime = System.currentTimeMillis();
				String renderedHtmlCacheName = sArg1 + "\n" + sArg2;
				ResourceCacheInfo rcinfo = ResourceCacheState.
						getTemporaryCache(renderedHtmlCacheName, currTime);
				if (rcinfo == null)
				{
					try
					{
						TransformerFactory tFactory = TransformerFactory.newInstance();
						StringWriter sw = new StringWriter();
						
						// obtain the XML and XSL files
						String xslData = RssHelper.getXmlHttpContentsAsString(xslUrl, feedMaxAge);
						String xmlData = RssHelper.getXmlHttpContentsAsString(xmlUrl, feedMaxAge);
						
						// convert them into HTML
						Transformer transformer = tFactory.newTransformer(
								new StreamSource(new StringReader(xslData)));
						transformer.transform(new StreamSource(new StringReader(
								xmlData)), new StreamResult(sw));
						String strResult = sw.toString();
						
						// place it into a global cache. No need for security since
						// the XML and XSL must be publicly available
						rcinfo = new ResourceCacheInfo(renderedHtmlCacheName);
						rcinfo.m_resourceObj = strResult;
						rcinfo.m_removalTS = currTime + (feedMaxAge * 1000);
						rcinfo.m_size = sw.getBuffer().length();
						ResourceCacheState.addTemporaryCache(renderedHtmlCacheName, rcinfo);
					}
					catch (Exception e)
					{
						e.printStackTrace();
						se = new ServiceException(e);
					}
				}

				if (se != null)
					throw se;
				
				// result is the value in the cache
				oResult = rcinfo.m_resourceObj;
			}
			break;

		case 1:	// xmlDecode(string [, beginIndex [, endIndex]])

			String xmlString = sArg1;
			int begin = 0;
			int end = xmlString.length();
			
			if (nargs > 1)								
				begin = (int)ScriptUtils.getLongVal(args[1], context);
			if (nargs > 2)								
				end = (int)ScriptUtils.getLongVal(args[2], context);
			
			oResult = StringUtils.decodeXmlEscapeSequence(xmlString.toCharArray(), begin, end);
			
			break;
		default:
			return false;
		}


		/*
		 * Do not alter code below here
		 */
		args[nargs] = ScriptExtensionUtils.computeReturnObject(config[4],
			bResult, iResult, dResult, oResult);
		return true;
	}

}
