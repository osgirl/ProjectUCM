/******************************************************************************/
/*                                                                            */
/*  IntraNet Solutions, Incorporated Confidential and Proprietary             */
/*                                                                            */
/*  This computer program contains valuable, confidential and proprietary     */
/*  information.  Disclosure, use, or reproduction without the written        */
/*  authorization of IntraNet Solutions is prohibited.  This unpublished      */
/*  work by IntraNet Solutions is protected by the laws of the United States  */
/*  and other countries.  If publication of the computer program should occur,*/
/*  the following notice shall apply:                                         */
/*                                                                            */
/*  Copyright (c) 1997-2001 IntraNet Solutions, Incorporated.  All rights	  */
/*	reserved.																  */
/*  Copyright (c) 2001-2006 Stellent, Incorporated.  All rights reserved.     */
/*                                                                            */
/******************************************************************************/
package newitem;

import intradoc.common.FileUtils;
import intradoc.common.LocaleUtils;
import intradoc.common.NumberUtils;
import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.shared.PluginFilters;
import intradoc.data.DataBinder;
import intradoc.data.DataException;
import intradoc.data.ResultSet;
import intradoc.data.ResultSetUtils;
import intradoc.server.DirectoryLocator;
import intradoc.server.ServiceHandler;
import intradoc.shared.SharedObjects;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.*;

public class NewItemServiceHandler extends ServiceHandler
{
	/**
	 * Default constructor
	 */
	public NewItemServiceHandler()
	{
	}

	/**
	 * Copies the metadata and primary file of an existing content item,
	 * and makes a copy. Modifications to the metadata are possible by
	 * passing in flags prefixed with "newItem:". For example, to make
	 * a copy of the new item with a title "first copy", specify the
	 * parameter "dDocTitle:first copy"
	 */
	public void doNewItemCheckin() throws ServiceException, DataException
	{
		/*ResultSet rset1 = m_binder.getResultSet("CONTENT_RESULT_LIST");
		if(rset1 != null){
			SystemUtils.trace("GetWebsiteContents", " result set:"+rset1.isRowPresent());
			SystemUtils.trace("GetWebsiteContents", "After result set:"+rset1.isEmpty());
		}
		else{
			SystemUtils.trace("GetWebsiteContents", "Result set is empty");
		}*/
		/*int maxCheckins = SharedObjects.getEnvironmentInt("MaxNewCheckinsPerRequest", 5);
		int thisCheckin = NumberUtils.parseInteger(m_binder.getEnvironmentValue(
				"NumberNewCheckinsThisRequest"), 1);
		if (thisCheckin > maxCheckins)
		{
			String err = LocaleUtils.encodeMessage(
				"csNewItemCheckinsExceedsMaximum", null, ""+maxCheckins);
			throw new ServiceException(err);
		}*/
		//m_binder.setEnvironmentValue("NumberNewCheckinsThisRequest", ""+(thisCheckin+1));

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
		
		// locate the roiginal vault file for copying
		String id = m_binder.get("dID");
		String fileName = id;
		if (extension.length() > 0)
			fileName += "." + extension;
		SystemUtils.trace("GetWebsiteContents", "dID, filename, extension, ddoctype "+id+" "+fileName+" "+extension+" "+m_binder.get("dDocType"));
		String orgPath = DirectoryLocator.computeVaultPath(fileName, m_binder);
		SystemUtils.trace("GetWebsiteContents", "orgPath "+orgPath);
		
		// Change the original file name so that Folders doesn't complain - Vijay
		String origName = ResultSetUtils.getValue(rset, "dOriginalName");
		String curTime = Long.toString(System.currentTimeMillis());

		// replace "." with "_" in file name
		String origNameMod = origName.replace('.', '_');
		if(origNameMod.length() > 25)
			origNameMod = origNameMod.substring(0, 24);
		
		SystemUtils.trace("GetWebsiteContents", "origName,  origNameMod"+origName+" "+origNameMod);
		
		// add extension if it is not null or empty
		String newName = origNameMod + curTime;
		if(extension.length() > 0)
			newName += "." + extension;

		// make a temp copy of the vault file (primaryFile)
		String vaultDir = DirectoryLocator.getVaultDirectory( ) + "~temp/" ;
		String primaryCopy = vaultDir + DataBinder.getNextFileCounter( );
		if (extension.length() > 0)
			primaryCopy = primaryCopy + "." + extension ;
		SystemUtils.trace("GetWebsiteContents", "orgPath, primaryCopy "+orgPath+" "+primaryCopy);
		FileUtils.copyFile( new File( orgPath ), new File( primaryCopy ), true ) ;
		
		updateFileLinks(primaryCopy);
		
		// set the required parameters to mimic a file upload
		m_binder.addTempFile( primaryCopy ) ;
		m_binder.putLocal( "primaryFile:path", primaryCopy ) ;
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
				if (coreFields.containsKey(fname) ||
					fname.startsWith("x"))
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
			String fieldName = (String)it.next();
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

		//m_binder.putLocal("dID", ""); // needed?
		String origDocName = m_binder.getLocal("dDocName");
		if (origDocName == null)
			m_binder.putLocal("dDocName", "");

		// execute the new check-in
		String checkinSubService = m_currentAction.getParamAt(1);
		m_service.executeService(checkinSubService);
		 /*rset1 = m_binder.getResultSet("CONTENT_RESULT_LIST");
			if(rset1 != null){
				SystemUtils.trace("GetWebsiteContents", "After result set:"+rset1.isRowPresent());
				SystemUtils.trace("GetWebsiteContents", "After result set:"+rset1.isEmpty());
				SystemUtils.trace("GetWebsiteContents", "After result set:"+rset1);
			}
			else{
				SystemUtils.trace("GetWebsiteContents", "After Result set is empty");
			}
			m_binder.addResultSet("CONTENT_RESULT_LIST", rset1);
			SystemUtils.trace("GetWebsiteContents", "Set resultset again in binder:CONTENT_RESULT_LIST");*/
	}
	
	//updates href links from HAYS_XYZ to HAYS_CP_XYZ
	public void updateFileLinks(String filePath){
		try{
			String fEncoding = "UTF8";
			StringBuilder text = new StringBuilder();
		    String NL = System.getProperty("line.separator");
		    String newDocNameDifferentiator = m_binder.get("docdiff");//CP
		    Scanner scanner = new Scanner(new FileInputStream(filePath), fEncoding);
		    try {
		      while (scanner.hasNextLine()){
		        text.append(scanner.nextLine() + NL);
		      }
		    }
		    finally{
		      scanner.close();
		    }
		    SystemUtils.trace("GetWebsiteContents", "Text read in: " + text);
		    
		    //replace
		    String textStr = text.toString();
		    textStr = textStr.replaceFirst("_", "_" + newDocNameDifferentiator + "_");;
		    //
		    
		    SystemUtils.trace("GetWebsiteContents","Writing to file named " + filePath + ". Encoding: " + fEncoding);
		    Writer out = new OutputStreamWriter(new FileOutputStream(filePath), fEncoding);
		    try {
		      out.write(textStr);
		    }
		    finally {
		      out.close();
		    }
		    }
	    catch(Exception e){
	    	SystemUtils.trace("GetWebsiteContents",e.getMessage());
	    }
	}
}