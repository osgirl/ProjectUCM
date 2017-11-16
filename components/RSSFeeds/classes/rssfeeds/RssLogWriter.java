/*                                                                            */
/*  Stellent, Incorporated Confidential and Proprietary                       */
/*                                                                            */
/*  This computer program contains valuable, confidential and proprietary     */
/*  information.  Disclosure, use, or reproduction without the written        */
/*  authorization of Stellent is prohibited.  This unpublished                */
/*  work by Stellent is protected by the laws of the United States            */
/*  and other countries.  If publication of the computer program should occur,*/
/*  the following notice shall apply:                                         */
/*                                                                            */
/*  Copyright (c) 1997-2001 IntraNet Solutions, Incorporated.  All rights	  */
/*	reserved.																  */
/*  Copyright (c) 2001-2006 Stellent, Incorporated.  All rights reserved.     */
/*                                                                            */
/******************************************************************************/
package rssfeeds;

import java.io.*;
import java.util.*;
import intradoc.common.*;
import intradoc.shared.*;
import intradoc.server.*;

/**
 * A RSS logger for all errors
 */
public class RssLogWriter implements LogWriter
{
	/**
	 * the numer of log entries to maintain. Older RSS readers only support 15 entries
	 */
	public static int m_numLogEntries = 30;

	/**
	 * flag to check if previous logs were loaded before new values were added
	 */
	protected boolean m_isLoaded = false;

	/**
	 * the date format for the 'pubDate' XML flag.
	 */
	public IdcDateFormat m_dateFormat = null;

	/**
	 * a queue of the last few log entries to rewrite
	 */
	protected Vector m_logEntries = new Vector(m_numLogEntries);

	/**
	 * the minimum error level for logging. Do not log info messages, otherwise it
	 * might slow down the system.
	 */
	public static int m_logLevel = Log.m_warningType;

	/**
	 * the RSS log footer
	 */
	public static final String m_logFooter = "</channel>\n</rss>";

	/**
	 * create a new RSS log writer object, and set the minimum log level (RssLogWriterLogLevel)
	 * and number of logs per file (RssLogWriterNumEntries)
	 */
	public RssLogWriter()
	{
		m_logLevel = SharedObjects.getEnvironmentInt("RssLogWriterLogLevel", m_logLevel);
		m_numLogEntries = SharedObjects.getEnvironmentInt("RssLogWriterNumEntries", m_numLogEntries);
		String dateFormatString = SharedObjects.getEnvironmentValue("RssDateFormat");
		if (dateFormatString == null)
			dateFormatString = "EEE, dd MMM yyyy HH:mm:ss";

		try
		{
			m_dateFormat = new IdcDateFormat();
			m_dateFormat.init(dateFormatString);
		}
		catch (ParseStringException e)
		{
			SystemUtils.dumpException(null, e);
			m_dateFormat = LocaleResources.m_iso8601Format;
		}
	}
	
	/**
	 * Generates a RSS log file of the last few error messages. This log is rewritten every time
	 * an error occurs, so the number of errors should be kept low, and the error level should
	 * be at least 1 (out of 0-3)
	 */
	public void doMessageAppend(int messageType, String msg, LogDirInfo logDirInfo, Throwable t)
	{
		// dont log anything if its not important
		if (messageType < m_logLevel && !msg.startsWith("!csServiceStart"))
			return;
		
		if (!m_isLoaded)
			loadPreviousLogEntries(logDirInfo);

		Calendar curTime = Calendar.getInstance();
		String rawDesc = Log.getRawDesc(messageType);
		String desc = LocaleResources.getString("csLogDesc" + rawDesc, null);

		// force some kind of stack to be presented for errors
		if (t == null && (messageType >= Log.m_errorType))
			t = new Throwable();

		String newEntry = generateLogEntry(curTime, rawDesc, msg, t);
		m_logEntries.add(0, newEntry); // most recent items first
		while (m_logEntries.size() > m_numLogEntries)
			m_logEntries.remove(m_numLogEntries);

		String logFileName = logDirInfo.m_prefix + ".rss";
		BufferedWriter bw = null;
		try
		{
			StringBuffer sb = new StringBuffer(8192);
			sb.append(generateLogHeader(curTime, logDirInfo));
			int size = m_logEntries.size();
			for (int i=0; i<size; i++)
				sb.append(m_logEntries.elementAt(i));
			sb.append(m_logFooter);
			bw = FileUtils.openDataWriter(logDirInfo.m_dir, logFileName);
			bw.write(sb.toString());
		}
		catch (IOException e)
		{
			SystemUtils.traceDumpException(null, "Unable to write to the RSS logs.", e);
		}
		finally
		{
			FileUtils.closeObject(bw);
		}
	}

	/**
	 * generate the header for this log file.
	 */
	public String generateLogHeader(Calendar curTime, LogDirInfo logDirInfo)
	{
		String pubDate = m_dateFormat.format(curTime.getTime());
		String link = DirectoryLocator.getWebRoot(true) + "groups/secure/logs/IdcLnLog.htm";
		String idcName = SharedObjects.getEnvironmentValue("IDC_Name");
		String logHeader = LocaleResources.getString("csLogHeaderDesc", 
				null, idcName);
		
		String xmlStr =
			"<?xml version=\"1.0\" encoding=\"" + FileUtils.m_isoSystemEncoding
				+ "\"?>\n" +
			"<rss version=\"2.0\">\n" +
			"<channel>\n" +
			"<title>" + logHeader + "</title>\n" +
			"<link>" + link + "</link>\n" +
			"<description>" + logHeader + "</description>\n" +
			"<lastBuildDate>" + pubDate + "</lastBuildDate>\n" ;

		return xmlStr;
	}


	public String generateLogEntry(Calendar curTime, String rawDesc,
		String msg, Throwable t)
	{
		String localizedMsg = msg;
		try
		{
			localizedMsg = LocaleResources.localizeMessage(localizedMsg, null);
		}
		catch (Throwable localizationErr)
		{
			SystemUtils.trace(null, "Error localizing error message: ");
			localizationErr.printStackTrace();
		}

		CharArrayWriter arrayWriter = new CharArrayWriter();
		if (t != null)
		{
			PrintWriter printWriter = new PrintWriter(arrayWriter);
			t.printStackTrace(printWriter);
		}
		
		
		String pubDate = m_dateFormat.format(curTime.getTime());
		String description = localizedMsg + "\n\n" + 
			arrayWriter.toString();
		String shortMessage = localizedMsg;
		if (shortMessage.length() > 60)
			shortMessage = shortMessage.substring(0,56) + "...";		
		String title = LocaleResources.getString("csLogDesc" + rawDesc, null)
			+ " - " + shortMessage;

		// format the description so fussy RSS readers can view it
		description = StringUtils.encodeXmlEscapeSequence(
				StringUtils.createErrorStringForBrowser(description));
		
		// create a globalID so some RSS readers dont get confused about multiple entries
		String guid = "" + curTime.getTimeInMillis();

		String xmlString = "<item>\n";
		xmlString += "<pubDate>" + pubDate + "</pubDate>\n";
		xmlString += "<guid isPermaLink=\"false\">" + guid + "</guid>\n";
		xmlString += "<link>" + DirectoryLocator.getWebRoot(true) + 
			"groups/secure/logs/IdcLnLog.htm?" + guid + "</link>\n";
		xmlString += "<title>" + StringUtils.encodeXmlEscapeSequence(
				title) + "</title>\n";
		xmlString += "<description>" + description + "</description>\n";
		xmlString += "</item>\n";

		return xmlString;
	}

	/**
	 * this should only be called once, upon startup
	 */
	public synchronized void loadPreviousLogEntries(LogDirInfo logDirInfo)
	{
		String logFileName = logDirInfo.m_prefix + ".rss";
		int result = FileUtils.checkFile(logDirInfo.m_dir + "/" + logFileName, true, true);
		if (result == Errors.RESOURCE_NOT_FOUND)
		{
			m_isLoaded = true;
			return;
		}

		BufferedReader bw = null;
		try
		{
			bw = FileUtils.openDataReader(logDirInfo.m_dir, logFileName);
			String line = bw.readLine();

			while (line != null)
			{
				if (line.equals("<item>"))
				{
					StringBuffer entry = new StringBuffer();
					entry.append(line).append('\n');
					line = bw.readLine();
					while (line != null && !line.equals("</item>"))
					{
						entry.append(line).append('\n');
						line = bw.readLine();
					}
					if (line != null)
						entry.append(line).append("\n\n");
					if (entry.length() > 0) // add at end of list, most recent FIRST
						m_logEntries.add(entry.toString());
				}
				else
					line = bw.readLine();
			}
		}
		catch (IOException e)
		{
			SystemUtils.traceDumpException(null, "Unable to read previous RSS log messages.", e);
		}
		finally
		{
		}
		m_isLoaded = true;
	}
}
