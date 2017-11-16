package hays.com.copywebsite;

import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.data.DataException;
import intradoc.data.DataResultSet;
import intradoc.data.ResultSet;
import intradoc.data.Workspace;
import intradoc.provider.Provider;
import intradoc.provider.Providers;
import intradoc.server.ServiceHandler;

import java.util.List;
import java.util.Vector;

public class GetWebsiteContents extends ServiceHandler
{
	public String NEW_DOC_DIFFERENTIATOR = null;
	public static final String TRACE_NAME = "HAYS_COPY_WEBSITE_COMPONENT";
	
	public void getAllWebsiteContents() throws ServiceException, DataException
	{

		String providerName = m_currentAction.getParamAt(0);
		String resultSetName = m_currentAction.getParamAt(1);
		String queryName = m_currentAction.getParamAt(2);
		String dataPrefixqueryName = m_currentAction.getParamAt(3);

		String oldSiteId = m_binder.get("oldsite");
		String oldLocale = m_binder.get("oldlocale");
		String oldAccount = m_binder.get("oldaccount");
		String newLocale = "";
		String newSiteId = m_binder.get("newsite");
		
		String newAccount = m_binder.get("newaccount");  
		 
		String sitetype = m_binder.get("sitetype");// uk=1, hays.com =2, Microsites=3
		if("3".equalsIgnoreCase(sitetype)){
		
		newLocale = m_binder.get("microlocale");
		SystemUtils.trace("GetWebsiteContents", "Microsite Locale:" + newLocale );
		}
		else{
			newLocale = m_binder.get("newlocale");
			SystemUtils.trace("GetWebsiteContents", "Microsite Locale via newlocale:" + newLocale );
		}
		NEW_DOC_DIFFERENTIATOR = m_binder.get("newsite");
		m_binder.putLocal("ProcessProjectFile", "No");
		
		//validateParams(Arrays.asList((new String[] { oldSiteId, oldLocale, oldAccount, newSiteId, newLocale, newAccount, sitetype,
		//		NEW_DOC_DIFFERENTIATOR })));

		SystemUtils.trace("GetWebsiteContents", "old data:" + oldSiteId + " " + oldLocale + " " + oldAccount);
		SystemUtils.trace("GetWebsiteContents", "new data:" + newSiteId + " " + newLocale + " " + newAccount);
		if("3".equalsIgnoreCase(sitetype)){
			m_binder.putLocal("xlocale", oldLocale+"temp");
		}
		else{
		m_binder.putLocal("xlocale", oldLocale);
		}
		m_binder.putLocal("xwebsitesection", oldSiteId + ":%");
		
		// validate the provider name
		if (providerName == null || providerName.length() == 0)
		{
			throw new ServiceException("You must specify a provider name.");
		}

		// validate that the provider is a valid database provider
		Provider p = Providers.getProvider(providerName);
		if (p == null)
			throw new ServiceException((new StringBuilder("The provider '")).append(providerName).append("' does not exist.").toString());

		if (!p.isProviderOfType("database"))
			throw new ServiceException((new StringBuilder("The provider '")).append(providerName)
					.append("' is not a valid provider of type 'database'.").toString());
		// grab the provider object that does all the work, and scope it to
		// a workspace object for database access, since we can be reasonably
		// certain at this point that the object returned is a Workspace object
		Workspace ws = (Workspace) p.getProvider();

		DataResultSet result = null;
		DataResultSet dataPrefixResultSet = null;
		// if they specified a predefined query, execute that
		if (queryName != null && queryName.trim().length() > 0)
		{
			// obtain a JDBC result set with the data in it. This result set is
			// temporary, and we must copy it before putting it in the binder
			SystemUtils.trace("GetWebsiteContents", "Query in getwebsitecontents()  "+queryName);
			ResultSet temp = ws.createResultSet(queryName, m_binder);
			// create a DataResultSet based on the temp result set
			result = new DataResultSet();
			result.copy(temp);
		}
		// place the result into the databinder with the appropriate name
		m_binder.addResultSet(resultSetName, result);
		m_binder.putLocal("xlocale", oldLocale);
		if (dataPrefixqueryName != null && dataPrefixqueryName.trim().length() > 0)
		{	
			SystemUtils.trace("GetWebsiteContents", "Data Prefix Query in getwebsitecontents()  "+dataPrefixqueryName);
			ResultSet dataPrefixtemp = ws.createResultSet(dataPrefixqueryName, m_binder);
			dataPrefixResultSet = new DataResultSet();
			dataPrefixResultSet.copy(dataPrefixtemp);
		}
		// place the result into the databinder with the appropriate name
		m_binder.addResultSet("Data_Prefix", dataPrefixResultSet);
		// release the JDBC connection assigned to this thread (request)
		// which kills the result set 'temp'
		ws.releaseConnection();
		String dataPrefix =null; 
		if (dataPrefixResultSet.first())
		{
			do
			{
				try
				{
					dataPrefix = dataPrefixResultSet.getStringValueByName("DATAFILEPREFIX");
					SystemUtils.trace(TRACE_NAME, "DATAFILEPREFIX in Try   " + dataPrefix);

				}
				catch (Exception ex)
				{
					SystemUtils.trace(TRACE_NAME, "Exception while processing : ");
				}
			}
			while (dataPrefixResultSet.next());
		}
		if(null != dataPrefix){
		m_binder.putLocal("datafileprefix", dataPrefix);
		}
		else{
			m_binder.putLocal("datafileprefix", "");
		}
		
		DataResultSet outputResult = new DataResultSet((new String[] { "dDocName", "Status", "NewDocName" }));
		DataResultSet documentResult = new DataResultSet((new String[] { "dOldDocName", "dNewDocName"}));
		if (result.first())
		{
			String ddocname = null;
			String newdocname = null;
			do
			{
				try
				{
					
					ddocname = result.getStringValueByName("dDocName");
					SystemUtils.trace(TRACE_NAME, "CONVERTING THIS NAME : : " + ddocname);
					SystemUtils.trace(TRACE_NAME, "CONVERSION CHECK : : " + ddocname.contains("_"+oldSiteId.toUpperCase()+"_"));
					if (ddocname.startsWith("HEADER_HC") || ddocname.startsWith("FOOTER_HC"))
					{
						newdocname = ddocname.replaceFirst(oldSiteId.toUpperCase(), newSiteId.toUpperCase());
					}
					else if (ddocname.endsWith("DF_HEADER") || ddocname.endsWith("DF_FOOTER"))
					{
						if ("uk".equals(oldSiteId))
						{
							newdocname = ddocname.replaceFirst(oldSiteId.toUpperCase(), newLocale.toUpperCase());
						}
						else
						{
							if(null != dataPrefix)
							newdocname = ddocname.replaceFirst(dataPrefix.toUpperCase(), newLocale.toUpperCase());
							else
							newdocname = ddocname.replaceFirst(oldLocale.toUpperCase(), newLocale.toUpperCase());	
						}
					}
					else if (ddocname.startsWith("UK_WC"))
					{
						newdocname = ddocname.replaceFirst(oldSiteId.toUpperCase(), newLocale.toUpperCase());
					}
					else if (null != dataPrefix && ddocname.startsWith(dataPrefix.toUpperCase() + "_WC"))
					{
						newdocname = ddocname.replaceFirst(dataPrefix.toUpperCase(), newLocale.toUpperCase());
					}
					else if (ddocname.contains("_"+oldSiteId.toUpperCase()+"_"))
					{
						newdocname = ddocname.replaceFirst("_"+oldSiteId.toUpperCase()+"_", "_" + NEW_DOC_DIFFERENTIATOR + "_");
					}
					else
					{
						newdocname = ddocname.replaceFirst("_", "_" + NEW_DOC_DIFFERENTIATOR + "_");
					}
					
					//SystemUtils.trace("GetWebsiteContents", "New dDocName ==>"+ newdocname);
					if(null!= newdocname && newdocname.length()>29){
						newdocname = newdocname.substring(0, 29);
						if(newdocname.endsWith("_")){
							newdocname = newdocname.substring(0, 28);
						}
					}
					Vector<String> docfields = new Vector<String>();
					docfields.add(ddocname);
					docfields.add(newdocname);
					documentResult.addRow(docfields);
					
			    }
				catch (Exception ex)
				{
					SystemUtils.trace("GetWebsiteContents", "Exception while processing 1: " + ddocname + ", " + ex.getMessage());
					SystemUtils.traceDumpException("GetWebsiteContents","Exception while processing 1", ex);
				}
			}
			while (result.next());
		}
		
		
		//SystemUtils.trace("GetWebsiteContents", "BINDER ==>"+ m_binder);
		if (result.first())
		{
			String ddocname = null;
			String ddoctitle = null;
			String ddocaccount = null;
			String xwebsites = null;
			String xwebsitesection = null;
			String xlocale = null;

			String newdocname = null;
			String newdocaccount = null;
			String newwebsites = null;
			String newwebsitesection = null;
			// String newlocale = null;
			do
			{
				try
				{
					
					ddocname = result.getStringValueByName("dDocName");
					ddocaccount = result.getStringValueByName("dDocAccount");
					xwebsites = result.getStringValueByName("xWebsites");
					xwebsitesection = result.getStringValueByName("xWebsiteSection");
					xlocale = result.getStringValueByName("xLocale");
					ddoctitle = result.getStringValueByName("dDocTitle");
					// ddoctitle = "copy";
					if (ddocname.startsWith("HEADER_HC") || ddocname.startsWith("FOOTER_HC"))
					{
						newdocname = ddocname.replaceFirst(oldSiteId.toUpperCase(), newSiteId.toUpperCase());
					}
					else if (ddocname.endsWith("DF_HEADER") || ddocname.endsWith("DF_FOOTER"))
					{
						if ("uk".equals(oldSiteId))
						{
							newdocname = ddocname.replaceFirst(oldSiteId.toUpperCase(), newLocale.toUpperCase());
						}
						else
						{
							if(null != dataPrefix)
							newdocname = ddocname.replaceFirst(dataPrefix.toUpperCase(), newLocale.toUpperCase());
							else
							newdocname = ddocname.replaceFirst(oldLocale.toUpperCase(), newLocale.toUpperCase());	
						}
					}
					else if (ddocname.startsWith("UK_WC"))
					{
						newdocname = ddocname.replaceFirst(oldSiteId.toUpperCase(), newLocale.toUpperCase());
					}
					else if (null != dataPrefix && ddocname.startsWith(dataPrefix.toUpperCase() + "_WC"))
					{
						newdocname = ddocname.replaceFirst(dataPrefix.toUpperCase(), newLocale.toUpperCase());
					}
					else if (ddocname.contains("_"+oldSiteId.toUpperCase()+"_"))
					{
						newdocname = ddocname.replaceFirst("_"+oldSiteId.toUpperCase()+"_", "_" + NEW_DOC_DIFFERENTIATOR + "_");
					}
					else
					{
						newdocname = ddocname.replaceFirst("_", "_" + NEW_DOC_DIFFERENTIATOR + "_");
					}
					
					newdocaccount = ddocaccount.replace(oldAccount, newAccount);
					newwebsites = xwebsites.replace(oldSiteId, newSiteId);
					newwebsitesection = xwebsitesection.replace(oldSiteId, newSiteId);
					SystemUtils.trace("GetWebsiteContents", "New dDocName ==>"+ newdocname);
					if(null!= newdocname && newdocname.length()>29){
						newdocname = newdocname.substring(0, 29);
						if(newdocname.endsWith("_")){
							newdocname = newdocname.substring(0, 28);
						}
					}
					SystemUtils.trace("GetWebsiteContents", "New dDocName After truncation==>"+ newdocname);
					m_binder.addResultSet("DOCUMENT_LIST", documentResult);
					m_binder.putLocal("dDocName", ddocname);
					m_binder.putLocal("newDocName", newdocname);
					m_binder.putLocal("dDocAccount", newdocaccount);
					m_binder.putLocal("xWebsites", newwebsites);
					m_binder.putLocal("xWebsiteSection", newwebsitesection);
					m_binder.putLocal("xLocale", newLocale);
					m_binder.putLocal("dDocTitle", ddoctitle);
					

					SystemUtils.trace("GetWebsiteContents", "service call "+ddocname+" "+newdocname+" "+newdocaccount+" "+newwebsites+" "+newwebsitesection+" "+newLocale);
					m_service.executeServiceEx("CHECKIN_NEW_ITEM_BY_NAME", true);
					Vector<String> fields = new Vector<String>();
					fields.add(ddocname);
					fields.add(m_binder.getLocal("StatusCode"));
					fields.add(newdocname);
					outputResult.addRow(fields);
				}
				catch (Exception ex)
				{
					SystemUtils.trace("GetWebsiteContents", "Exception while processing 2: " + ddocname + ", " + ex.getMessage());
					SystemUtils.traceDumpException("GetWebsiteContents","Exception while processing 2", ex);
				}
			}
			while (result.next());
			
		}
		m_binder.addResultSet("OUTPUT_RESULT", outputResult);
		chekinCopyWebsiteXML(outputResult);
		//SystemUtils.trace("GetWebsiteContents", "service result" + result);
		//SystemUtils.trace("GetWebsiteContents", "OUTPUT_RESULT" + outputResult);
		//SystemUtils.trace("GetWebsiteContents", "GET MBINDER OUTPUT_RESULT" + m_binder.getResultSet("OUTPUT_RESULT"));
		

	}

	public void chekinCopyWebsiteXML(DataResultSet outputResult) throws ServiceException, DataException
	{
		String newdocname = null;
		String newdocaccount = null;
		String newwebsites = null;
		String newwebsitesection = null;
		String ddocname = null;
		try
		{
			String oldSiteId = m_binder.get("oldsite");
			String oldAccount = m_binder.get("oldaccount");
			
			String newSiteId = m_binder.get("newsite");
			String newAccount = m_binder.get("newaccount");
			ddocname = m_binder.getLocal("ProjectdDocName");
			m_binder.putLocal("ProcessProjectFile", "Yes");
			m_binder.putLocal("dDocName", ddocname);
			m_service.executeServiceEx("DOC_INFO_BY_NAME", true);
			String newLocale = "";
			String ddocaccount = m_binder.get("dDocAccount");
			String xwebsites =  m_binder.get("xWebsites");
			String xwebsitesection = m_binder.get("xWebsiteSection");
			String ddoctitle = m_binder.get("dDocTitle");
			String sitetype = m_binder.get("sitetype");

			if (!(ddocname.indexOf("_")>0))
			{
				newdocname = ddocname + "_" + NEW_DOC_DIFFERENTIATOR;
			}			
			else
			{
				newdocname = ddocname.replaceFirst("_", "_" + NEW_DOC_DIFFERENTIATOR + "_");
			}
			newdocaccount = ddocaccount.replace(oldAccount, newAccount);
			newwebsites = xwebsites.replace(oldSiteId, newSiteId);
			newwebsitesection = xwebsitesection.replace(oldSiteId, newSiteId);
			if(null!= newdocname && newdocname.length()>30){
				newdocname = newdocname.substring(0, 29);
				if(newdocname.endsWith("_")){
					newdocname = newdocname.substring(0, 28);
				}
			}
			if("3".equalsIgnoreCase(sitetype)){
				
				newLocale = m_binder.get("microlocale");
				SystemUtils.trace("GetWebsiteContents", "Microsite Locale:" + newLocale );
				}
				else{
					newLocale = m_binder.get("newlocale");
					SystemUtils.trace("GetWebsiteContents", "Microsite Locale via newlocale:" + newLocale );
				}
			m_binder.putLocal("dDocName", ddocname);
			m_binder.putLocal("newDocName", newdocname);
			m_binder.putLocal("dDocAccount", newdocaccount);
			m_binder.putLocal("xWebsites", newwebsites);
			m_binder.putLocal("xWebsiteSection", newwebsitesection);
			m_binder.putLocal("xLocale", newLocale);
			m_binder.putLocal("dDocTitle", ddoctitle);
			m_binder.addResultSet("OUTPUT_RESULT", outputResult);
			SystemUtils.trace("GetWebsiteContents", "service call "+ddocname+" "+newdocname+" "+newdocaccount+" "+newwebsites+" "+newwebsitesection+" "+newLocale);
			m_service.executeServiceEx("CHECKIN_NEW_ITEM_BY_NAME", true);
			Vector<String> fields = new Vector<String>();
			fields.add(ddocname);
			fields.add(m_binder.getLocal("StatusCode"));
			fields.add(newdocname);
			outputResult.addRow(fields);
		}
		catch (Exception ex)
		{
			SystemUtils.trace("GetWebsiteContents", "Exception while processing 3: " + ddocname + ", " + ex);
			SystemUtils.traceDumpException("GetWebsiteContents","Exception while processing 3", ex);
		}
		m_binder.addResultSet("OutputDescription", outputResult);
	}

	void validateParams(List paramlist) throws ServiceException
	{
		for (int i = 0; i < paramlist.size(); i++)
		{
			Object param = paramlist.get(i);
			if (param == null || ((String) param).isEmpty())
			{
				throw new ServiceException("Parameter is empty: " + i);
			}
		}
	}
}
