
package hays.custom;

import hays.custom.multilingual.HaysWebSite;
import intradoc.common.*;
import intradoc.data.*;
import intradoc.provider.Provider;
import intradoc.provider.Providers;
import intradoc.server.*;
import intradoc.shared.SharedObjects;
import java.io.*;
import java.util.HashMap;

import com.sun.org.apache.xalan.internal.xsltc.runtime.Hashtable;

public class SearchLocations extends ServiceHandler {

	/**
	 * Executes a named query against a named database provider, and stores the
	 * results into a named result set.
	 */
    public void getAllMatchingLocations() throws ServiceException, DataException {

		// obtain the provider name, the query, and the result set name
		// from the action definition in the service
        String providerName = m_currentAction.getParamAt(0);
        String resultSetName = m_currentAction.getParamAt(1);
        String queryLocationName = m_currentAction.getParamAt(2);
        String queryPostCodeName = m_currentAction.getParamAt(3);
        String queryLocationNameForDomain = m_currentAction.getParamAt(4);
        String queryPostCodeNameForDomain = m_currentAction.getParamAt(5);
        String queryLocationNameForDomain_HRBE = m_currentAction.getParamAt(6);

        String localeString = ((IdcLocale)m_service.getLocaleResource(0)).m_name;
        SystemUtils.trace("SearchLocation", (new StringBuilder("Locale found:")).append(localeString).toString());

        String loc = m_binder.get("location");
        String domainId = m_binder.get("domainId");

        SystemUtils.trace("SearchLocation", (new StringBuilder("domainName ")).append(m_binder.getLocal("domainName")).toString());
        SystemUtils.trace("SearchLocation", (new StringBuilder("domainId ")).append(domainId).toString());

        String queryName = null;
        String isPostCode = null;
        try
        {
            isPostCode = m_binder.get("searchPostcode");
        }
        catch(Exception e)
        {
			//queryName = queryPostCodeName;
            loc = loc.replaceAll(" ", ""); //removing empty spaces
        }
        if( ("38".equals(domainId)) || ("3".equals(domainId))){
            queryName = queryLocationNameForDomain_HRBE;
            SystemUtils.trace("SearchLocation", "Query Executed:"+queryName);
        }
        else if(domainId != null && !"-1".equals(domainId) && domainId != "38" && domainId != "3"){
            queryName = queryLocationNameForDomain;
            SystemUtils.trace("SearchLocation", "Query Executed with other domains:"+queryName);
        }
        else
            queryName = queryLocationName;

        if(isPostCode != null && "1".equals(isPostCode))
        {
            if(domainId != null && !"-1".equals(domainId))
                queryName = queryPostCodeNameForDomain;
            else
                queryName = queryPostCodeName;
            loc = loc.replaceAll(" ", ""); //removing empty spaces
        }

        SystemUtils.trace("SearchLocation", (new StringBuilder("queryName ")).append(queryName).toString()); 
        
        String locationPrefix = null;
        if(isPostCode != null && "1".equals(isPostCode) && "1".equals(domainId)){
        	
        	String postcode = (String)m_binder.get("location");        	     
        	postcode = postcode.trim().replace(" ", "");  
        	postcode = postcode.toUpperCase();
        	    	
        	SystemUtils.trace("SearchLocation", "Postcode:"+postcode);
           
        	locationPrefix = (new StringBuilder("")).append(postcode).append("%").toString();
        }else{
        	locationPrefix = (new StringBuilder("")).append(m_binder.get("location")).append("%").toString();
        }
        
        
        SystemUtils.trace("SearchLocation", "Final Location Prefix:"+locationPrefix);
        
        m_binder.putLocal("locationPrefix", locationPrefix);

		// validate the provider name
        if(providerName == null || providerName.length() == 0) {
            throw new ServiceException("You must specify a provider name.");
		}

		// validate that the provider is a valid database provider
        Provider p = Providers.getProvider(providerName);
        if(p == null)
            throw new ServiceException((new StringBuilder("The provider '")).append(providerName).append("' does not exist.").toString());

        if(!p.isProviderOfType("database"))
            throw new ServiceException((new StringBuilder("The provider '")).append(providerName).append("' is not a valid provider of type 'database'.").toString());
		// grab the provider object that does all the work, and scope it to
		// a workspace object for database access, since we can be reasonably
		// certain at this point that the object returned is a Workspace object
        Workspace ws = (Workspace)p.getProvider();
        if(loc != null && !"".equals(loc) && !loc.contains("*"))
        {
            DataResultSet result = null;
			// if they specified a predefined query, execute that
            if(queryName != null && queryName.trim().length() > 0)
            {
				// obtain a JDBC result set with the data in it. This result set is
				// temporary, and we must copy it before putting it in the binder
                ResultSet temp = ws.createResultSet(queryName, m_binder);
				// create a DataResultSet based on the temp result set
                result = new DataResultSet();
                result.copy(temp);
            }
			// place the result into the databinder with the appropriate name
            m_binder.addResultSet(resultSetName, result);
        }
		// release the JDBC connection assigned to this thread (request)
		// which kills the result set 'temp'
        ws.releaseConnection();
    }

	/**
	 * For demo purposes, turn the result set into a raw string that can be
	 * dumped to a web page.
	 */

    public void convertResultSetToString()
        throws ServiceException, DataException
    {
		// obtain the result set name, and the local data value that the
		// string should be placed into
        String resultSetName = m_currentAction.getParamAt(0);
        String stringName = m_currentAction.getParamAt(1);

		// get the result set from the databinder, complain if it isn't present
        ResultSet result = m_binder.getResultSet(resultSetName);
        if(result == null)
            throw new ServiceException((new StringBuilder("Cannot turn the result set '")).append(resultSetName).append("' into a string. The result set is null.").toString());

		// turn the resultSet into a string and place it into the local data
        try
        {
            DataBinder tempBinder = new DataBinder();
            tempBinder.addResultSet(resultSetName, result);
            StringWriter sw = new StringWriter();
            tempBinder.send(sw);
            m_binder.putLocal(stringName, sw.toString());
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    public void getDomainId()
    {
        try
        {
            String useDefaultDomainNameIfNotFound = m_binder.getLocal("useDefaultDomainNameIfNotFound");

            String qGetDomainId = m_currentAction.getParamAt(0);
            ResultSet domainIdRs = m_workspace.createResultSet(qGetDomainId, m_binder);

            DataResultSet domainIdDrs = new DataResultSet();
            domainIdDrs.copy(domainIdRs);
            if(domainIdDrs == null || domainIdDrs.getNumRows() <= 0)
            {
                if(useDefaultDomainNameIfNotFound != null && "1".equals(useDefaultDomainNameIfNotFound))
                {
                    m_binder.putLocal("domainName", SharedObjects.getEnvironmentValue("DefaultSiteDomain"));
                    domainIdRs = m_workspace.createResultSet(qGetDomainId, m_binder);
                    domainIdDrs.copy(domainIdRs);
                    SystemUtils.trace("GetDomainId", (new StringBuilder("domainId ")).append(domainIdDrs.getStringValue(0)).toString());
                    SystemUtils.trace("GetDomainId", (new StringBuilder("isoCountryCode ")).append(domainIdDrs.getStringValue(1)).toString());
                    SystemUtils.trace("GetDomainId", (new StringBuilder("languageId ")).append(domainIdDrs.getStringValue(2)).toString());
                    m_binder.putLocal("domainId", domainIdDrs.getStringValue(0));
                    m_binder.putLocal("isoCountryCode", domainIdDrs.getStringValue(1));
                    m_binder.putLocal("languageId", domainIdDrs.getStringValue(2));
                } else
                {
                    m_binder.putLocal("domainId", "-1");
                    m_binder.putLocal("isoCountryCode", "-1");
                    m_binder.putLocal("languageId", "-1");
                }
            } else
            {
                SystemUtils.trace("GetDomainId", (new StringBuilder("numRows ")).append(domainIdDrs.getNumRows()).toString());
                SystemUtils.trace("GetDomainId", (new StringBuilder("domainId ")).append(domainIdDrs.getStringValue(0)).toString());
                SystemUtils.trace("GetDomainId", (new StringBuilder("isoCountryCode ")).append(domainIdDrs.getStringValue(1)).toString());
                SystemUtils.trace("GetDomainId", (new StringBuilder("languageId ")).append(domainIdDrs.getStringValue(2)).toString());
                m_binder.putLocal("domainId", domainIdDrs.getStringValue(0));
                m_binder.putLocal("isoCountryCode", domainIdDrs.getStringValue(1));
                m_binder.putLocal("languageId", domainIdDrs.getStringValue(2));
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
            SystemUtils.trace("GetDomainId", e.toString());
        }finally{
        	m_workspace.releaseConnection();
        }
    }
    @SuppressWarnings("unchecked")
	public void setSelect()
    {
    	try
    	{
    	  String column_val = m_binder.getLocal("locationColumn");
    	  //String siteId = m_binder.getLocal("SiteLocale");
    	 // SystemUtils.trace("setSelect", "Column value is "+column_val + siteId);
    	  if(column_val == null || column_val.length()==0 )
    	  {
    		  SystemUtils.trace("setSelect", "portal Call");
    		  HaysWebSite website = null;
    		  String siteLocale = m_binder.getLocal("sitelocale");
    		  SystemUtils.trace("setSelect", "sitelocale passed from portal"+siteLocale);
    		  if(siteLocale == null || siteLocale.length()==0) // During check-in locationColum and sitelocale(used for webservice) is unavailable
    		  {
    			  siteLocale = m_binder.getLocal("xLocale");
    			  SystemUtils.trace("setSelect", "xLocale value is "+siteLocale);
    		  }
    		  HashMap<String, HaysWebSite> siteLocaleMap = (HashMap<String, HaysWebSite>)SharedObjects.getObject("Multiling", "siteLocaleMap");
    		   website = siteLocaleMap.get(siteLocale);
    		   if(website != null)
    		   {
    			   column_val = website.location_column;
    			   SystemUtils.trace("setSelect", "Location value fetched"+column_val);
    		   }
    		  
    		 
    	  }
    	  
    	  m_binder.putLocal("loc_descr", column_val)	;
    	  SystemUtils.trace("setSelect", "Inside the try block"+m_binder.getLocal("loc_descr"));
    	  //SystemUtils.trace("setSelect", "Country region value"+m_binder.getLocal("countryRegion"));
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    }

}
