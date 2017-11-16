package hays.com.copywebsite;

import intradoc.common.FileUtils;
import intradoc.common.LocaleUtils;
import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.data.DataBinder;
import intradoc.data.DataException;
import intradoc.data.ResultSet;
import intradoc.data.ResultSetUtils;
import intradoc.server.DirectoryLocator;
import intradoc.server.ServiceHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;

public class NewItemServiceHandler extends ServiceHandler
{
	public static final String TRACE_NAME = "COPY_WEBSITE";
	public NewItemServiceHandler()
	{
	}

	
	public void doNewItemCheckin() throws ServiceException, DataException
	{
		

		String rsetName = m_currentAction.getParamAt(0);
		ResultSet rset = m_binder.getResultSet(rsetName);

		if (rset == null)
		{
			// This should never happen.
			String msg = LocaleUtils.encodeMessage("csResultSetMissing", null, rsetName);
			throw new DataException(msg);
		}
		String extension = ResultSetUtils.getValue(rset, "dExtension").toLowerCase();
		if (extension == null)
			extension = "";

		// As ddoctype gets polluted correct it for finding correct file path
		String ddoctype = rset.getStringValueByName("dDocType");
		m_binder.putLocal("dDocType", ddoctype);
		String regiondefn = rset.getStringValueByName("xRegionDefinition");
		m_binder.putLocal("xRegionDefinition", regiondefn);
		 String accnt = rset.getStringValueByName("dDocAccount");
		  m_binder.putLocal("dDocAccount", accnt);
		// locate the roiginal vault file for copying
		String id = m_binder.get("dID");;
		
		String fileName = id;
		if (extension.length() > 0)
			fileName += "." + extension;
//		SystemUtils.trace(TRACE_NAME, "NEW_CHECK_IN -- dID, filename, extension, ddoctype " + id + " " + fileName + " " + extension + " "
//				+ m_binder.get("dDocType"));
		String orgPath = DirectoryLocator.computeVaultPath(fileName, m_binder);
		//SystemUtils.trace(TRACE_NAME, "orgPath " + orgPath);

		// Change the original file name so that Folders doesn't complain -
		// Vijay
		String origName = ResultSetUtils.getValue(rset, "dOriginalName");
		String curTime = Long.toString(System.currentTimeMillis());

		// replace "." with "_" in file name
		String origNameMod = origName.replace('.', '_');
		if (origNameMod.length() > 25)
			origNameMod = origNameMod.substring(0, 24);

		//SystemUtils.trace(TRACE_NAME, "origName,  origNameMod" + origName + " " + origNameMod);

		// add extension if it is not null or empty
		String newName = origNameMod + curTime;
		if (extension.length() > 0)
			newName += "." + extension;

		// make a temp copy of the vault file (primaryFile)
		String vaultDir = DirectoryLocator.getVaultDirectory() + "~temp/";
		String primaryCopy = vaultDir + DataBinder.getNextFileCounter();
		if (extension.length() > 0)
			primaryCopy = primaryCopy + "." + extension;
		SystemUtils.trace(TRACE_NAME, "orgPath, primaryCopy " + orgPath + " " + primaryCopy);
		FileUtils.copyFile(new File(orgPath), new File(primaryCopy), true);

		if ("Yes".equalsIgnoreCase(m_binder.get("ProcessProjectFile")))
		{
			m_binder.putLocal("xWebsiteObjectType", "Project");
			updateProjectFileXML(primaryCopy);
		}
		else
		{
			ResultSet docrset = m_binder.getResultSet("DOCUMENT_LIST");
			updateFileLinks(primaryCopy,docrset);
		}
		String dInDate  = m_binder.getLocal("dInDate");
		//SystemUtils.trace(TRACE_NAME, "dInDate outside :: " + dInDate);
		
			 SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yy HH:mm:ss a");//dd/MM/yyyy
			 Date now = new Date();
			 String strDate = sdfDate.format(now);
			 //SystemUtils.trace(TRACE_NAME, "SDF strDate :: " +strDate);
			 m_binder.putLocal("dInDate", strDate);
			 //SystemUtils.trace(TRACE_NAME, "dInDate inside :: " + m_binder.getLocal("dInDate"));
		
		// set the required parameters to mimic a file upload
		m_binder.addTempFile(primaryCopy);
		m_binder.putLocal("primaryFile:path", primaryCopy);
		m_binder.putLocal("primaryFile", newName); // Vijay

		// create a hashmap of the core fields
		HashMap coreFields = new HashMap();
		coreFields.put("dSecurityGroup", null);
		coreFields.put("dDocType", null);
		coreFields.put("dDocAccount", null);
		coreFields.put("dDocAuthor", null);
		coreFields.put("xWebsites", null);
		coreFields.put("xWebsiteSection", null);
		coreFields.put("xLocale", null);
		coreFields.put("xRegionDefinition", null);

		// loop through existing item's result set, and extract all
		// custom metadata fields, and special core fields
		rset.first();
		int xx = rset.getNumFields();
		String fname = "";
		String value = "";
		if (rset.isRowPresent())
		{
			for (int idx = 0; idx < xx; idx++)
			{
				fname = rset.getFieldName(idx);
				value = rset.getStringValue(idx);
				if (coreFields.containsKey(fname) || fname.startsWith("x"))
				{
					String currentValue = m_binder.getLocal(fname);
					
					if (currentValue == null)
						m_binder.putLocal(fname, value);
						
				}
			}
		}

		// allow the core fields to be overwritten with fields
		// prefixed with 'new', such as 'newDocTitle'
		Iterator it = coreFields.keySet().iterator();
		while (it.hasNext())
		{
			String fieldName = (String) it.next();
			String newFieldName = "new" + fieldName.substring(1);
			String newValue = m_binder.getLocal(newFieldName);
			if (newValue != null && newValue.trim().length() > 0)
				m_binder.putLocal(fieldName, newValue);
		}

		// set the new dDocName, or clear it so autonumber works
		String docName = m_binder.getLocal("newDocName");
		if (docName != null && docName.trim().length() > 0)
			m_binder.putLocal("dDocName", docName);
		else
			m_binder.getLocalData().remove("dDocName");

		// remove the old ResultSet to reduce data pollution
		m_binder.removeResultSet(rsetName);

		// m_binder.putLocal("dID", ""); // needed?
		String origDocName = m_binder.getLocal("dDocName");
		
		if (origDocName == null)
			m_binder.putLocal("dDocName", "");
		
		
		// execute the new check-in
		 String checkinSubService = m_currentAction.getParamAt(1);
		 m_service.executeService(checkinSubService);
		 SystemUtils.trace(TRACE_NAME, "NEW_CHECK_IN COMPLETE -- " + docName + " AS COPY OF : " + origDocName);
	}

	// updates href links from HAYS_XYZ to HAYS_CP_XYZ
	public void updateFileLinks(String filePath,ResultSet docrset)
	{
		try
		{
			String fEncoding = "UTF8";
			StringBuilder text = new StringBuilder();
			String NL = System.getProperty("line.separator");
			//ResultSet rset = m_binder.getResultSet("DOCUMENT_LIST");
			String newSiteId = m_binder.get("newsite");
			String siteType = m_binder.get("sitetype");
			String oldSiteId = m_binder.get("oldsite");
			SystemUtils.trace(TRACE_NAME, "DOCUMENT_LIST in MEthod "+docrset);
			//String origDocName = m_binder.getLocal("dDocName");
			String oldDocName = null;
			String newDocName = null;
			Scanner scanner = new Scanner(new FileInputStream(filePath), fEncoding);
			try
			{
				while (scanner.hasNextLine())
				{
					text.append(scanner.nextLine() + NL);
				}
			}
			finally
			{
				scanner.close();
			}
			// replace
			String textStr = text.toString();
			if("3".equals(siteType))
			{
				SystemUtils.trace(TRACE_NAME, "Inside Update content method");
					//SystemUtils.trace(TRACE_NAME, "BEFORE : " + textStr);
				textStr = textStr.replace(oldSiteId, newSiteId);
					//SystemUtils.trace(TRACE_NAME, "AFTER : " + textStr);
				SystemUtils.trace(TRACE_NAME, "OLDSITE=="+oldSiteId+"  NEWSITE=="+newSiteId);
			}
			if (docrset.first())
			{
				do
				{
					oldDocName = docrset.getStringValueByName("dOldDocName");
					newDocName = docrset.getStringValueByName("dNewDocName");
					//SystemUtils.trace(TRACE_NAME, "Link UPDATE  oldDocName : NewDocName::" + oldDocName + " : " + newDocName);
					if(oldDocName != null && newDocName != null)
					{
						textStr = textStr.replace(oldDocName, newDocName);
					}
				}
				while (docrset.next());
			}

			SystemUtils.trace(TRACE_NAME, "Writing to file named " + filePath + ". Encoding: " + fEncoding);
			Writer out = new OutputStreamWriter(new FileOutputStream(filePath), fEncoding);
			try
			{
				out.write(textStr);
				
					//SystemUtils.trace(TRACE_NAME, "WRITING : " + textStr);
					
			}
			catch (Exception e)
			{
				SystemUtils.trace(TRACE_NAME, "Exception == "+e.toString());
				//e.printStackTrace();
			}
			finally
			{
				out.close();
			}
		}
		catch (Exception e)
		{
			SystemUtils.trace(TRACE_NAME, "Exception == "+e);
			e.printStackTrace();
		}
	}

	public void updateProjectFileXML(String filePath)
	{
		try
		{
			String fEncoding = "UTF8";
			StringBuilder text = new StringBuilder();
			String NL = System.getProperty("line.separator");
			// String newDocNameDifferentiator = "PD";// CP
			Scanner scanner = new Scanner(new FileInputStream(filePath), fEncoding);
			try
			{
				while (scanner.hasNextLine())
				{
					text.append(scanner.nextLine() + NL);
				}
			}
			finally
			{
				scanner.close();
			}
			String textStr = text.toString();
			ResultSet rset = m_binder.getResultSet("OUTPUT_RESULT");
			String oldDocName = null;
			String newDocName = null;
			String oldSiteId = m_binder.get("oldsite");
			String oldLocale = m_binder.get("oldlocale");
			String newSiteId = m_binder.get("newsite");
			String newLocale = m_binder.get("newlocale");
			String siteType = m_binder.get("sitetype");
			String oldSiteLabel = m_binder.get("oldSiteLabel");
			String newSiteLabel = m_binder.get("newSiteLabel");
			SystemUtils.trace(TRACE_NAME, "OLDSITE--"+oldSiteId+",OLDLOCALE--"+oldLocale+",newSiteId--"+newSiteId+",newLocale--"+newLocale+",siteType--"+siteType);
			
			if(!"3".equals(siteType))
			{
				SystemUtils.trace(TRACE_NAME, "Inside IF BLOCK");
				if(!"".equals(oldLocale))
				textStr = textStr.replace(oldLocale, newLocale);
			}
			else
			{
				String oldSiteLocale = m_binder.get("oldsitelocale");
				String oldSiteDomainId = m_binder.get("oldsitedomain");
				String newSiteDomainId = m_binder.get("newsitedomain");
				String oldMicrositeCode = m_binder.get("micrositecode");
				String newMicrositeLocale = m_binder.get("microlocale");
				
				String newMicrositeCode = m_binder.get("newmicrocode");
				SystemUtils.trace(TRACE_NAME, "oldSiteLocale : newMicrositeLocale::" + oldSiteLocale + " : " + newMicrositeLocale);
				if(!"".equals(oldSiteLocale))
				textStr = textStr.replace(oldSiteLocale, newMicrositeLocale);
				textStr = textStr.replace("domainId=\""+oldSiteDomainId+"\"" , "domainId=\""+newSiteDomainId+"\"");
				textStr = textStr.replace("micrositeCode=\""+oldMicrositeCode+"\"" , "micrositeCode=\""+newMicrositeCode+"\"");
			}
			
			if (rset.first())
			{
				do
				{
					oldDocName = rset.getStringValueByName("dDocName");
					newDocName = rset.getStringValueByName("NewDocName");
					SystemUtils.trace(TRACE_NAME, "oldDocName : NewDocName::" + oldDocName + " : " + newDocName);
					if(oldDocName != null && newDocName != null)
						textStr = textStr.replaceAll("(?i)" + oldDocName, newDocName);
				}
				while (rset.next());
			}
			textStr = textStr.replace("siteId=\""+oldSiteId+"\"" , "siteId=\""+newSiteId+"\"");
			textStr = textStr.replace("siteLabel=\""+oldSiteLabel+"\"" , "siteLabel=\""+newSiteLabel+"\"");
			//replace job opportunities, rss and search results section attributes
			textStr = textStr.replace("nodeId=\"jobs_"+oldSiteId+"\"" , "nodeId=\"jobs_"+newSiteId+"\"");
			textStr = textStr.replace("nodeId=\"rss_"+oldSiteId+"\"" , "nodeId=\"rss_"+newSiteId+"\"");
			textStr = textStr.replace("nodeId=\"results_"+oldSiteId+"\"" , "nodeId=\"results_"+newSiteId+"\"");
			String oldDataPrefix = null; 
			if(!"3".equals(siteType) && !"".equals(m_binder.getAllowMissing("datafileprefix")))
			{
				oldDataPrefix = m_binder.getAllowMissing("datafileprefix");
				textStr = textStr.replace(oldDataPrefix, newLocale);
			}
			SystemUtils.trace(TRACE_NAME, "Writing to file named " + filePath + ". Encoding: " + fEncoding);
			Writer out = new OutputStreamWriter(new FileOutputStream(filePath), fEncoding);
			try
			{
				out.write(textStr);
				//SystemUtils.trace(TRACE_NAME, textStr);
			}
			finally
			{
				out.close();
			}
		}
		catch (Exception e)
		{
			SystemUtils.trace(TRACE_NAME, "Writing to file exception");
			SystemUtils.trace(TRACE_NAME, e.getMessage());
		}

	}
}