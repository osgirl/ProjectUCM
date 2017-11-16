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
/*  Copyright (c) 2001-2005 Stellent, Incorporated.  All rights reserved.     */
/*                                                                            */
/******************************************************************************/

package hays.custom;

//import hays.co.uk.HaysGeneralServiceHandler;  //commented for automation
//import hays.co.uk.HaysNavigationHandler;	//commented for automation
import intradoc.common.ClassHelper;
import intradoc.common.ExecutionContext;
import intradoc.common.ExecutionContextAdaptor;
import intradoc.common.FileUtils;
import intradoc.common.GrammarElement;
import intradoc.common.IdcLocale;
import intradoc.common.LocaleResources;
import intradoc.common.LocaleUtils;
import intradoc.common.ScriptExtensionsAdaptor;
import intradoc.common.ScriptInfo;
import intradoc.common.ScriptUtils;
import intradoc.common.ServiceException;
import intradoc.common.StringUtils;
import intradoc.common.SystemUtils;

import intradoc.data.DataBinder;
import intradoc.data.DataException;
import intradoc.data.DataResultSet;
import intradoc.data.ResultSet;
import intradoc.data.ResultSetUtils;
import intradoc.data.Workspace;

import intradoc.provider.Provider;
import intradoc.provider.Providers;
import intradoc.server.DirectoryLocator;
import intradoc.server.Service;
import intradoc.server.script.ScriptExtensionUtils;

import intradoc.shared.SharedObjects;
import intradoc.shared.UserData;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Properties;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;

import sitestudio.SSCommon;
import sitestudio.SSLinkFunctions;




/**
 * This class demonstrates how to create custom IdocScript functions. These
 * include variable names that should be evaluated, variables that are either
 * true or false, as well as new kinds of functions.
 */
public class HaysTranslationIdocExtension extends ScriptExtensionsAdaptor {

	
    public HaysTranslationIdocExtension() {
         // this is a list of the functions that can be called with the custom code
        m_functionTable = new String[] {"isPrimaryWebsite","getSecondarySites","getWebsitesForDomain",
        		"haysNodeLink","getAllWebsitesForDomain"};

        // Configuration data for functions.  This list must align with the "m_functionTable"
        // list.  In order the values are "id number", "Number of arguments", "First argument type",
        // "Second argument type", "Return Type".  Return type has the following possible
        // values: 0 generic object (such as strings) 1 boolean 2 integer 3 double.
        // The value "-1" means the value is unspecified.
        m_functionDefinitionTable = new int[][] 
            {
				{0, 1, GrammarElement.STRING_VAL, -1, 0}, // getPrimaryWebsites
				{1, 1, GrammarElement.STRING_VAL, -1, 1}, // getSecondarySites
				{2, 2, GrammarElement.STRING_VAL,GrammarElement.STRING_VAL, 1}, // getWebsitesForDomain
				{3, 2, GrammarElement.STRING_VAL,GrammarElement.STRING_VAL, 0},// haysNodeLink
				{4, 1, GrammarElement.STRING_VAL,-1, -1} // getAllWebsitesForDomain
            } ;
    }

 
    /**
     * This is where the custom IdocScript function is evaluated.
     */
    public boolean evaluateFunction(ScriptInfo info, Object[] args,
                                    ExecutionContext context) throws ServiceException {
        /**
		 * This code below is optimized for speed, not clarity.  Do not modify
		 * the code below when making new IdocScript functions.  It is needed to
		 * prepare the necessary variables for the evaluation and return of the
		 * custom IdocScript functions.  Only customize the switch statement below.
		 */
        int config[] = (int[])info.m_entry;
        String function = info.m_key;
        int nargs = args.length - 1;
    	 
        int allowedParams = config[1];      
        if (allowedParams >= 0 && allowedParams != nargs) {
            String msg =
                LocaleUtils.encodeMessage("csScriptEvalNotEnoughArgs", null,
                                          function, "" + allowedParams);
            throw new IllegalArgumentException(msg);
        }

        String msg =
            LocaleUtils.encodeMessage("csScriptMustBeInService", null, function,
                                      "Service");
        Service service = ScriptExtensionUtils.getService(context, msg);
        DataBinder binder = service.getBinder();

        UserData userData = (UserData)context.getCachedObject("UserData");
        if (userData == null) {
            msg =
LocaleUtils.encodeMessage("csUserDataNotAvailable", null, function);
            throw new ServiceException(msg);
        }

        // Do some initial conversion of arguments.  Choices of what initial conversions to make
        // are based on frequency of usage.  If a function uses nontypical parameters it will
        // have to do its own conversion.
        String sArg1 = null;
        String sArg2 = null;
        long lArg1 = 0;
        long lArg2 = 0;         
        if (nargs > 0) {
            if (config[2] == GrammarElement.STRING_VAL) {
                sArg1 = ScriptUtils.getDisplayString(args[0], context);
            } else if (config[2] == GrammarElement.INTEGER_VAL) {
                lArg1 = ScriptUtils.getLongVal(args[0], context);
            }
            

        }
        if (nargs > 1) {
            if (config[3] == GrammarElement.STRING_VAL) {
                sArg2 = ScriptUtils.getDisplayString(args[1], context);
            } else if (config[3] == GrammarElement.INTEGER_VAL) {
                lArg2 = ScriptUtils.getLongVal(args[1], context);
            }
        }
        /**
		 * Here is where the custom code should go. The case values coincide
		 * with the "id values" in m_functionDefinitionTable. Perform the
		 * calculations here, and place the result into ONE of the result
		 * variables declared below.  Use 'sArg1' and 'sArg2' for the first and
		 * second String arguments for the function (if they exist).  Likewise use
		 * 'lArg1' and 'lArg2' for the first and second long integer arguments.
		 */
        boolean bResult = false; // Used for functions that return a boolean.
        int iResult = 0; // Used for functions that return an integer.
        double dResult = 0.0; // Used for functions that return a double.
        Object oResult =
            null; // Used for functions that return an object (string).
        Workspace ws = null;
        
        switch (config[0]) {
        
        case 0 ://isPrimaryWebsite
        	
        	try{
	        	ws = getProviderConnection("SystemDataBase");
	        	binder.putLocal("websiteId", sArg1);
	        	ResultSet rs = ws.createResultSet("QisPrimaryWebsite", binder);
				if (rs == null || rs.isEmpty())
				{
					oResult = "0";
				}
				else
				{
					rs.first();
					oResult = rs.getStringValue(0);
				}
	        }catch(Exception e){
        		oResult = "0";
        	}finally{
    			if( ws != null)
    				ws.releaseConnection();
        	}
        	break;
        case 1 ://getSecondarySites
        	
          	try{
          		DataResultSet drs = null;
	        	ws= getProviderConnection("SystemDataBase");
	        	binder.putLocal("websiteId", sArg1);
	        	SystemUtils.trace("TransIdoc", "websiteId "+sArg1);
	        	ResultSet rs = ws.createResultSet("QgetSecondarySites", binder);
	        	SystemUtils.trace("TransIdoc", "After query execute ");
				if (rs == null || rs.isEmpty())
				{
					SystemUtils.trace("TransIdoc", "In if");
					oResult = false;
				}
				else
				{
					SystemUtils.trace("TransIdoc", "in else");
					drs = new DataResultSet();
					drs.copy(rs);
					binder.addResultSet("SecondaryWebsites", drs);
					
					oResult = true;
				}
	        }catch(Exception e){
	        	SystemUtils.trace("TransIdoc", e.getMessage());
	        	e.printStackTrace();
        		oResult = false;
        	}finally{
    			if( ws != null)
    				ws.releaseConnection();
        	}
        	break;
        case 2 ://getWebsitesForDomain
        	
          	try{
          		DataResultSet drs = null;
	        	ws= getProviderConnection("SystemDataBase");
	        	binder.putLocal("domainId", sArg1);
	        	SystemUtils.trace("TransIdoc", "domainId "+sArg1);
	        	binder.putLocal("siteid", sArg2);
	        	SystemUtils.trace("TransIdoc", "siteid "+sArg2);
	        	ResultSet rs = ws.createResultSet("QgetWebsitesForDomain", binder);
	        	SystemUtils.trace("TransIdoc", "After query execute ");
				if (rs == null || rs.isEmpty())
				{
					SystemUtils.trace("TransIdoc", "In if");
					oResult = false;
				}
				else
				{
					SystemUtils.trace("TransIdoc", "in else");
					drs = new DataResultSet();
					drs.copy(rs);
					binder.addResultSet("WebsitesForDomain", drs);
					oResult = true;
				}
	        }catch(Exception e){
	        	SystemUtils.trace("TransIdoc", e.getMessage());
	        	e.printStackTrace();
        		oResult = false;
        	}finally{
    			if( ws != null)
    				ws.releaseConnection();
        	}
        	SystemUtils.trace("TransIdoc", "binder:"+binder);
        	break;	
        case 3 ://haysNodeLink
          	try{
          		oResult = SSLinkFunctions.computeNodeLinkUrl((Service)service, (String)sArg1, (String)sArg2);
          		int index = ((String)oResult).indexOf('?');
          		if(index > 0){
          			oResult = ((String)oResult).substring(0, index);
          		}
	        }catch(Exception e){
	        	SystemUtils.trace("TransIdoc", e.getMessage());
	        	e.printStackTrace();
        		oResult = null;
        	}
        	break;	
        	
        	  
        	
        	
        case 4 ://getAllWebsitesForDomain
        	
          	try{
          		DataResultSet drs = null;
	        	ws= getProviderConnection("SystemDataBase");
	        	binder.putLocal("domainId", sArg1);
	        	SystemUtils.trace("TransIdoc", "domainId "+sArg1);	         
	         
	        	ResultSet rs = ws.createResultSet("QgetAllWebsitesForDomain", binder);
	        	SystemUtils.trace("TransIdoc", "After query execute ");
				if (rs == null || rs.isEmpty())
				{
					SystemUtils.trace("TransIdoc", "In if");
					oResult = false;
				}
				else
				{
					SystemUtils.trace("TransIdoc", "in else");
					drs = new DataResultSet();
					drs.copy(rs);
					SystemUtils.trace("TransIdoc", "allWebsitesForDomain");
					binder.addResultSet("allWebsitesForDomain", drs);					
					oResult = true;
				}
	        }catch(Exception e){
	        	SystemUtils.trace("TransIdoc", e.getMessage());
	        	e.printStackTrace();
        		oResult = false;
        	}finally{
    			if( ws != null)
    				ws.releaseConnection();
        	}
        	SystemUtils.trace("TransIdoc", "oResult:"+oResult);
        	SystemUtils.trace("TransIdoc", "binder:"+binder);
        	break;	
         default:
            return false;

        }


        /**
		 * Do not alter code below here
		 */
               
        args[nargs] =
            ScriptExtensionUtils.computeReturnObject(config[4], bResult,
                                                     iResult, dResult,
                                                     oResult);
        // Handled function.
        return true;
    }
    
    
    private Workspace getProviderConnection(String providerName) throws ServiceException, DataException {
				
		SystemUtils.trace("HaysTranslationIdoc", "provider name to be used =" + providerName);
		// validate the provider name
		if (providerName == null || providerName.length() == 0) {
			throw new ServiceException("You must specify a provider name.");
		}
		// validate that the provider is a valid database provider
		Provider p = Providers.getProvider(providerName);
		if (p == null) {
			throw new ServiceException("The provider '" + providerName + "' does not exist.");
		} else if (!p.isProviderOfType("database")) {
			throw new ServiceException("The provider '" + providerName
					+ "' is not a valid provider of type 'database'.");
		}

		Workspace ws = (Workspace) p.getProvider();
		
		return ws;
	}
    
	private String getAccountPath(String acct) {
		int len = acct.length();
		StringBuffer b = new StringBuffer(len * 2);

		for (int i = 0; i < len; i++)
		{
			char ch = acct.charAt(i);
			ch = Character.toLowerCase(ch);
			b.append(ch);
			if (ch == '/')
			{
				b.append('@');
			}
		}

		return b.toString();
	}


}

