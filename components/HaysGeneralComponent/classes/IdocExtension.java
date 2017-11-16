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

import hays.co.uk.HaysGeneralServiceHandler;
import hays.co.uk.HaysNavigationHandler;
import intradoc.common.ClassHelper;
import intradoc.common.ExecutionContext;
import intradoc.common.ExecutionContextAdaptor;
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
import intradoc.data.FieldInfo;
import intradoc.data.ResultSet;
import intradoc.data.ResultSetUtils;
import intradoc.data.Workspace;

import intradoc.provider.Provider;
import intradoc.provider.Providers;
import intradoc.server.Service;
import intradoc.server.schema.SchemaUtils;
import intradoc.server.script.ScriptExtensionUtils;

import intradoc.shared.ComponentClassFactory;
import intradoc.shared.SharedObjects;
import intradoc.shared.UserData;
import intradoc.shared.schema.SchemaHelper;
import intradoc.shared.schema.SchemaSecurityFilter;
import intradoc.shared.schema.SchemaViewData;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;

import sitestudio.SSCommon;
import sitestudio.SSScriptExtensions;


/**
 * This class demonstrates how to create custom IdocScript functions. These
 * include variable names that should be evaluated, variables that are either
 * true or false, as well as new kinds of functions.
 */
public class IdocExtension extends ScriptExtensionsAdaptor {
	//TODO - env variable
	private static final String ONT_PROVIDER = "OntologyDatabase";//SharedObjects.getEnvironmentValue("OntologyProvider");
	
	public static final Pattern LOCALE_REGEX = Pattern.compile("(.+)[-_](.+)");
	
	String queryName = "LocationDetailsQuery";
	
    public IdocExtension() {
        //		// this is a list of all the custom variable names that can be evaluated
        //		m_variableTable = new String[] {
        //			// these variables should be evaluated, and a string should be returned
        //			"UppercaseUserName", "DayOfWeek",
        //
        //			// these variables should be evaluated as true or false
        //			"TodaysDateIsEven", "HasAdminRole"
        //		};
        //
        //		// this is the definition table for the variables.  The first integer is
        //		// an id used in the switch statement in evaluateVariable(...) below, the second
        //		// integer simply shows that the variable should be evaluated as a boolean
        //		m_variableDefinitionTable = new int[][]
        //		{
        //			{0, 0}, // UppercaseUserName
        //			{1, 0}, // DayOfWeek
        //			{2, 1}, // TodaysDateIsEven
        //			{3, 1}  // HasAdminRole		
        //		};

        // this is a list of the functions that can be called with the custom code
        m_functionTable = new String[] { "randomNumber","locationDetails", "formatCurrency", "urlDecode", "urlEncode", "haysLoadSiteNavResultSet", "getNodeByProperty","getDefaultHttpSiteAddress","strReplaceReg","strRemoveDuplicates",
        		"formatNumberWithComma", "strLastIndexOf" ,"lcc","getEnabledLocales","haysIncludeXML","haysGetFieldViewDisplayValue","round","stripHTML","UserAgent","UserAgent_UK"};

        // Configuration data for functions.  This list must align with the "m_functionTable"
        // list.  In order the values are "id number", "Number of arguments", "First argument type",
        // "Second argument type", "Return Type".  Return type has the following possible
        // values: 0 generic object (such as strings) 1 boolean 2 integer 3 double.
        // The value "-1" means the value is unspecified.
        m_functionDefinitionTable = new int[][] { 
                        {0, 1, GrammarElement.INTEGER_VAL, -1, 2 }, // Random Number
                        {1, 2, GrammarElement.STRING_VAL, GrammarElement.STRING_VAL, 1 }, //locationDetails
                    	{2, 2, GrammarElement.STRING_VAL, GrammarElement.STRING_VAL, 0}, // formatCurrency
                    	{3, 1, GrammarElement.STRING_VAL, -1, 0},// urlDecode
            			{4, 1, GrammarElement.STRING_VAL, -1, 0},  // urlEncode
            			{5, 1, GrammarElement.STRING_VAL, -1, 0},  // haysLoadSiteNavResultSet
            			{6, 2, GrammarElement.STRING_VAL, GrammarElement.STRING_VAL, 0},// getNodeByProperty
            			{7, 1, GrammarElement.STRING_VAL, -1, 0}, // getDefaultHttpSiteAddress
            			{8, 3, GrammarElement.STRING_VAL, GrammarElement.STRING_VAL, GrammarElement.STRING_VAL, 0}, // strReplaceReg
            			{9, 2, GrammarElement.STRING_VAL, GrammarElement.STRING_VAL, 0}, // strRemoveDuplicates
            			{10, 1, GrammarElement.STRING_VAL, -1, 0}, // formatNumberWithComma
            			{11, 2, GrammarElement.STRING_VAL, GrammarElement.STRING_VAL, 2}, // strLastIndexOf
            			{12, -1, GrammarElement.STRING_VAL, GrammarElement.STRING_VAL, 0}, // lcc
            			{13, 0, -1, -1, 0}, // getEnabledLocales
            			{14, 2, GrammarElement.STRING_VAL, GrammarElement.STRING_VAL, 0}, // haysIncludeXML
            			{15, 3, GrammarElement.STRING_VAL, GrammarElement.STRING_VAL, 0}, // haysGetFieldViewDisplayValue
            			{16, 2, GrammarElement.INTEGER_VAL, GrammarElement.INTEGER_VAL, 2}, // round
            			{17, 1, GrammarElement.STRING_VAL, -1, 0}, // stripHTML
            			{18, 1, GrammarElement.STRING_VAL, -1, 1}, // UserAgent
            			{19, 1, GrammarElement.STRING_VAL, -1, 1} // UserAgent_UK
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
        
        SystemUtils.trace("lcc", "sarg1 before  "+sArg1);

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
        switch (config[0]) {
        
        
        case 0: //Random Number
            // this will generate a random number between 1 and upper limit
            // specified by the input paramters
            Random randomGenerator = new Random();
            int randomInt =
                randomGenerator.nextInt(new Long(lArg1).intValue());
            iResult = randomInt;
            break;
            
            
        case 1:		//location details
        	if (sArg1 == null || "".equals(sArg1)){
    				// print the error message to the display
    				System.out.println("Location Id can not be empty");
    
    				// set the result to an error string
    				oResult = "Error";
    
    				// using the code from below, we will compute the return
    				// object using the result variables, but will force the
    				// function to calculate a string return type by passing a
    				// '0' as the first parameter, instead of config[4], which
    				// comes from the m_functionDefinitionTable defined above
    				args[nargs] = ScriptExtensionUtils.computeReturnObject(
    					0, bResult, iResult, dResult, oResult);
    
    				// exit the function here, rather than below
    				return true;
        	}
        	binder.putLocal("locationIds", sArg1);
        	if(sArg2 == null || "".equals(sArg2)){
        		sArg2 = "LOCATION_DETAILS";
        	}
        	
    		
    		DataResultSet result = null;
    		Workspace ws = null;
    		
    		try{
    			// grab the provider object that does all the work, and scope it to
	    		// a workspace object for database access, since we can be reasonably
	    		// certain at this point that the object returned is a Workspace object
	    		ws = getProviderConnection();
	    		
	    		// if they specified a predefined query, execute that
	    		if (queryName != null && queryName.trim().length() > 0) {
	    			// obtain a JDBC result set with the data in it. This result set is
	    			// temporary, and we must copy it before putting it in the binder
	    			ResultSet temp = ws.createResultSet(queryName, binder);
	
	    			// create a DataResultSet based on the temp result set
	    			result = new DataResultSet();
	    			result.copy(temp);
	    		}
    		}catch(DataException ex){
    			System.out.println("Exception occured in Location details:"+ex);
    		    ex.getStackTrace();
				// set the result to an error string
				oResult = "Error";

				// using the code from below, we will compute the return
				// object using the result variables, but will force the
				// function to calculate a string return type by passing a
				// '0' as the first parameter, instead of config[4], which
				// comes from the m_functionDefinitionTable defined above
				args[nargs] = ScriptExtensionUtils.computeReturnObject(
					0, bResult, iResult, dResult, oResult);

				// exit the function here, rather than below
				return true;
    		}
    		finally {
    			// release the JDBC connection assigned to this thread (request)
        		// which kills the result set 'temp'
    			if( ws != null)
    				ws.releaseConnection();
    		}
    		// place the result into the databinder with the appropriate name
    		binder.addResultSet(sArg2, result);

    		
    		bResult = true;
			break;
			
        case 2: // formatCurrency
        	String val = sArg1;
        	String localeStr = sArg2;  // lang,country
        	StringBuffer rez = new StringBuffer();
        	String lang="en", country = "GB";
        	Locale locale = null;
        	if( localeStr != null && localeStr.trim().length() > 0){
        		Matcher matcher = LOCALE_REGEX.matcher(localeStr);
        		if (matcher.find() && matcher.groupCount() > 1){
              		lang = matcher.group(1);
            		country = matcher.group(2);
        		}
        	} 
        	locale = new Locale(lang, country);
            		
    		double valD = Double.valueOf(val);
    		if( locale.getLanguage().equals(Locale.ENGLISH.getLanguage())) {
    			if( valD > 1000) {
    				valD = valD / 1000;
    				rez.append(NumberFormat.getCurrencyInstance(locale).getCurrency().getSymbol(locale));
    				rez.append((int)valD).append("K");
    			} else {
    				rez.append(NumberFormat.getCurrencyInstance(locale).format(valD));
    			}
    		}
    		else {
    			rez.append(NumberFormat.getCurrencyInstance(locale).format(valD));
    		}
        	oResult = rez.toString();
        	break;
        	
        	
        case 3: // urlDecode
        	try {
				oResult = URLDecoder.decode(sArg1, "UTF-8");
			}catch(UnsupportedEncodingException ex) {
				throw new ServiceException(ex.toString());
			}
			break;
			
        case 4: // urlEncode
        	try {
				oResult = URLEncoder.encode(sArg1, "UTF-8");
			}catch(UnsupportedEncodingException ex) {
				throw new ServiceException(ex.toString());
			}
			break;
        	
        case 5:	// haysLoadSiteNavResultSet
        	intradoc.data.ResultSet resultset = binder.getResultSet("HaysNavNodes");
            if (resultset == null) {
                String siteId = sArg1;
                Properties properties3 = new Properties();
                properties3.put("siteId", siteId);
                String ssUrlPrefix = binder.getLocal("ssUrlPrefix");
                if (ssUrlPrefix != null && ssUrlPrefix.length() > 0) {
                    properties3.put("ssUrlPrefix", ssUrlPrefix);
                }
                String isContrib = binder.getLocal("SSContributor");
                if (isContrib != null && isContrib.length() > 0) {
                    properties3.put("SSContributor", isContrib);
                }
                callServiceHandler(service, "loadHaysResultSet", properties3, "");
               // ResultSet navigationRS = binder.getResultSet("HaysNavNodes");
               // resultset = binder.getResultSet("HaysNavNodes");
            }
            
            break;
            
        case 6:		// getNodeByProperty
        	Properties params = new Properties();
        	String siteId = binder.getLocal("siteId");
        	if( siteId == null)
        		throw new ServiceException("Site Id is not specified.");
        	String startNodeId = binder.getLocal("startNodeId");
        	if(startNodeId == null )
        		throw new ServiceException("Start node is not specified.");
        	params.put("siteId", siteId);
        	String customPropertyName = sArg1;
        	String customPropertyValue = sArg2;
        	params.put("property", customPropertyName);
        	params.put("value", customPropertyValue);
        	params.put("startNodeId", startNodeId);
        	oResult = callServiceHandler(service, "getNodeForProperty", params, "targetNodeId");
        	
        	break;
        case 7: //getDefaultHttpSiteAddress
        	String websiteId = sArg1;
        	Properties parameters = new Properties();
        	parameters.put("siteId", websiteId);
        	oResult = callServiceHandler(service, "ssGetDefaultHttpSiteAddress", parameters, "dafaultHttpSiteAddress","hays.co.uk.HaysGeneralServiceHandler");
        	
        	break;
        	
        case 8: // strReplaceReg            
        	String str = ScriptUtils.getDisplayString(args[0], context);;
        	String regExpr = ScriptUtils.getDisplayString(args[1], context);
        	String replacement = ScriptUtils.getDisplayString(args[2], context);
        	SystemUtils.trace("hays", "strReplaceReg(): " + str + ", " + regExpr);
        	oResult = str.replaceAll(regExpr, replacement);
        	SystemUtils.trace("hays", "result: " + oResult);
        	break;
        	
        case 9: // strRemoveDuplicates - removes duplicate words
        	String value = sArg1;
        	String delimeter = sArg2 ;
        	String[] array = value.split(delimeter+ "[ ]?");
    		List<String> list = new ArrayList<String>();
    		String key = null;
    		for(int i = 0; i < array.length; i++){
    			key = array[i];
    			if( key.length() > 0 && !list.contains(key))
    				list.add(key);
    		}
    		if( list.size() > 1){
	    		value = list.toString();
	    		value = value.substring(1, value.length()-1).replaceAll(",[ ]?", delimeter);
    		}
    		oResult = value;
        	break;
        	
        	
        case 10://formatNumberWithComma
        	String num = sArg1;
			DecimalFormat df = new DecimalFormat();
			DecimalFormatSymbols dfs = new DecimalFormatSymbols();
			dfs.setGroupingSeparator(',');
			df.setDecimalFormatSymbols(dfs);
			oResult = df.format(Double.parseDouble(num));
			break;
			
        case 11:
        	value = sArg1;
        	String subString = sArg2;
        	iResult = value.lastIndexOf(subString);
        	break;
        	
        case 12 : //lcc
            Object[] localArgs = new Object[args.length-2];
            System.arraycopy(args, 2, localArgs, 0, localArgs.length);
            SystemUtils.trace("lcc", "sArg1 "+sArg1);
            SystemUtils.trace("lcc", "sArg2 "+sArg2);
            IdcLocale idcl=LocaleResources.getLocale(sArg1);
            
            try{
            	sArg1 = URLDecoder.decode(sArg1, "UTF-8");
            }catch(Exception e){
            	
            }
            SystemUtils.trace("lcc", "sArg1 after "+sArg1);
            if(idcl==null){
            	SystemUtils.trace("lcc", "idcl Locale is null .. "+idcl);
                oResult = LocaleResources.getString(sArg2, context, localArgs);
            }
            else{
            	SystemUtils.trace("lcc", "idcl Locale "+idcl.m_name);
                ExecutionContext ctx = new ExecutionContextAdaptor();
                ctx.setCachedObject("UserLocale", idcl);
                oResult = LocaleResources.getString(sArg2, ctx,localArgs);
            }
            break;
        case 13 ://getEnabledLocales
        	StringBuffer localeString = new StringBuffer();
        	try{
        	 String[] arrayOfString1 = { "lcLocaleId", "lcIsEnabled" };
        	 DataResultSet localDataResultSet1 = SharedObjects.getTable("LocaleConfig");
        	 String[][] arrayOfString2 = ResultSetUtils.createStringTable(localDataResultSet1, arrayOfString1);
        	 if (arrayOfString2 != null){
                 for (int j = 0; j < arrayOfString2.length; j++)
                 {
                   String[] arrayOfString3 = arrayOfString2[j];
                   boolean bool2 = StringUtils.convertToBool(arrayOfString3[1], false);
                   if ((!bool2) || (arrayOfString3[0] == null) || (arrayOfString3[0].length() <= 0))
                     continue;
                   if(localeString.length() >0){
                	   localeString.append(",").append(arrayOfString3[0]);
                   }else{
                	   localeString.append(arrayOfString3[0]);
                   }
                 }
        	 }
        	}catch(Exception e){
        	}
        	oResult = localeString.toString();
        	break;
        case 14: // haysIncludeXML
        	ResultSet rs = binder.getResultSet("DOC_INFO");
        	SystemUtils.trace("HaysIncludeXML","sArg1 "+sArg1);
        	SystemUtils.trace("HaysIncludeXML","sArg2 "+sArg2);
        	String xmlValue = "";
        	try{
       		String includeString = SSCommon.doIncludeXml(service, sArg1, sArg2);
       		SystemUtils.trace("HaysIncludeXML","includeString "+includeString);
       		xmlValue = SSScriptExtensions.evaluateIncludeXmlEx(service, includeString, true);
       		SystemUtils.trace("HaysIncludeXML","xmlValue "+xmlValue);
        	}catch(ServiceException e){
        		SystemUtils.trace("HaysIncludeXML","Doc info not found for "+sArg1);
        		//e.printStackTrace();
        	}
        	oResult = xmlValue;
        	SystemUtils.trace("HaysIncludeXML","oResult "+oResult);
       		binder.addResultSet("DOC_INFO", rs);
			break;	
        case 15: // 	haysGetFieldViewDisplayValue
        	SchemaHelper schemahelper = (SchemaHelper)ComponentClassFactory.createClassInstance("SchemaHelper", "intradoc.shared.schema.SchemaHelper", null);
        	SchemaUtils schemaUtils = (SchemaUtils)ComponentClassFactory.createClassInstance("SchemaUtils", "intradoc.server.schema.SchemaUtils", null);
        	SchemaViewData schemaviewdata = schemahelper.getView(sArg1);
        	SchemaSecurityFilter schemasecurityfilter = schemaUtils.getSecurityImplementor(schemaviewdata);
        	if (schemasecurityfilter != null) {
                schemasecurityfilter.init(service);
            }
            Object obj;
			try {
				obj = schemaviewdata.getAllViewValuesWithFilter(schemasecurityfilter);
	            DataResultSet resSet = new DataResultSet();
	            DataResultSet temp = new DataResultSet();
	            String locale1 = null, dKey = null;
	            String paramLocale = args[2].toString();
	            // filter by language
	            if( obj != null && paramLocale != null ) {
	            	temp.copy((ResultSet)obj );
	            	if( temp.first() && temp.getFieldInfo("LOCALE", new FieldInfo()) ) {
	            		resSet.copyFieldInfo(temp);
	    	        	do {
	    	        		locale1 = temp.getStringValueByName("LOCALE");
	    	        		dKey = temp.getStringValue(0);
	    	        		//SystemUtils.trace("HaysIncludeXML","sArg1 "+sArg1+" sArg2 "+sArg2+" sArg3 "+paramLocale+" key "+dKey+" value "+temp.getStringValue(1));
	    	        		if( paramLocale.equals(locale1) && sArg2.equals(dKey)) {	        		
	    	        			//resSet.addRow( temp.getCurrentRowValues() );
	    	        			oResult = temp.getStringValue(1);
	    	        			break;
	    	        		}
	    	        	} while( temp.next());
	            	}
	            }  
			} catch (DataException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            	
            
			break;	
        case 16: //round
            // this will round the percentage
        	iResult = (int) Math.round((double)lArg1*100/lArg2);
            break;	
            
        case 17: // stripHTML
        	try
			{
				oResult = Jsoup.parse(sArg1).text();
			}
			catch (Exception e)
			{
				// TODO Auto-generated catch block
				SystemUtils.trace("stripHTML","Exception: "+e.toString());
			}        	
			break;
        case 18: // UserAgent
        	try
			{
        		String ua=sArg1.toLowerCase();
        		SystemUtils.trace("useragent","User Agent "+ua);
        		if(ua.matches("(?i).*((android|bb\\d+|meego).+mobile|avantgo|bada\\/|blackberry|blazer|compal|elaine|fennec|hiptop|iemobile|ip(hone|od)|iris|kindle|lge |maemo|midp|mmp|mobile.+firefox|netfront|opera m(ob|in)i|palm( os)?|phone|p(ixi|re)\\/|plucker|pocket|psp|series(4|6)0|symbian|treo|up\\.(browser|link)|vodafone|wap|windows (ce|phone)|xda|xiino).*")||ua.substring(0,4).matches("(?i)1207|6310|6590|3gso|4thp|50[1-6]i|770s|802s|awa|abac|ac(er|oo|s\\-)|ai(ko|rn)|al(av|ca|co)|amoi|an(ex|ny|yw)|aptu|ar(ch|go)|as(te|us)|attw|au(di|\\-m|r |s)|avan|be(ck|ll|nq)|bi(lb|rd)|bl(ac|az)|br(e|v)w|bumb|bw\\-(n|u)|c55\\/|capi|ccwa|cdm\\-|cell|chtm|cldc|cmd\\-|co(mp|nd)|craw|da(it|ll|ng)|dbte|dc\\-s|devi|dica|dmob|do(c|p)o|ds(12|\\-d)|el(49|ai)|em(l2|ul)|er(ic|k0)|esl8|ez([4-7]0|os|wa|ze)|fetc|fly(\\-|_)|g1 u|g560|gene|gf\\-5|g\\-mo|go(\\.w|od)|gr(ad|un)|haie|hcit|hd\\-(m|p|t)|hei\\-|hi(pt|ta)|hp( i|ip)|hs\\-c|ht(c(\\-| |_|a|g|p|s|t)|tp)|hu(aw|tc)|i\\-(20|go|ma)|i230|iac( |\\-|\\/)|ibro|idea|ig01|ikom|im1k|inno|ipaq|iris|ja(t|v)a|jbro|jemu|jigs|kddi|keji|kgt( |\\/)|klon|kpt |kwc\\-|kyo(c|k)|le(no|xi)|lg( g|\\/(k|l|u)|50|54|\\-[a-w])|libw|lynx|m1\\-w|m3ga|m50\\/|ma(te|ui|xo)|mc(01|21|ca)|m\\-cr|me(rc|ri)|mi(o8|oa|ts)|mmef|mo(01|02|bi|de|do|t(\\-| |o|v)|zz)|mt(50|p1|v )|mwbp|mywa|n10[0-2]|n20[2-3]|n30(0|2)|n50(0|2|5)|n7(0(0|1)|10)|ne((c|m)\\-|on|tf|wf|wg|wt)|nok(6|i)|nzph|o2im|op(ti|wv)|oran|owg1|p800|pan(a|d|t)|pdxg|pg(13|\\-([1-8]|c))|phil|pire|pl(ay|uc)|pn\\-2|po(ck|rt|se)|prox|psio|pt\\-g|qa\\-a|qc(07|12|21|32|60|\\-[2-7]|i\\-)|qtek|r380|r600|raks|rim9|ro(ve|zo)|s55\\/|sa(ge|ma|mm|ms|ny|va)|sc(01|h\\-|oo|p\\-)|sdk\\/|se(c(\\-|0|1)|47|mc|nd|ri)|sgh\\-|shar|sie(\\-|m)|sk\\-0|sl(45|id)|sm(al|ar|b3|it|t5)|so(ft|ny)|sp(01|h\\-|v\\-|v )|sy(01|mb)|t2(18|50)|t6(00|10|18)|ta(gt|lk)|tcl\\-|tdg\\-|tel(i|m)|tim\\-|t\\-mo|to(pl|sh)|ts(70|m\\-|m3|m5)|tx\\-9|up(\\.b|g1|si)|utst|v400|v750|veri|vi(rg|te)|vk(40|5[0-3]|\\-v)|vm40|voda|vulc|vx(52|53|60|61|70|80|81|83|85|98)|w3c(\\-| )|webc|whit|wi(g |nc|nw)|wmlb|wonu|x700|yas\\-|your|zeto|zte\\-")){
        			bResult = true;
        			if(ua.matches(".*jooble.*")){
        				SystemUtils.trace("useragent","jooble check passed setting redirectcondn to false");
        				bResult = false; //do not redirect for jooble bot
        			}
        			break;
        		}
        		else{
        			bResult = false;
        			break;
        		}
        		}
        		  
			catch (Exception e)
			{
				// TODO Auto-generated catch block
				SystemUtils.trace("UserAgent","Exception: "+e.toString());
			}        	
			break;
        case 19: // UserAgent_UK
        	try
			{
        		String ua_uk=sArg1.toLowerCase();
        		if(ua_uk.matches("(?i).*(android|avantgo|playbook|blackberry|samsung|blazer|compal|elaine|fennec|hiptop|iemobile|ip(hone|od|ad)|iris|kindle|lge|maemo|midp|mmp|opera m(ob|in)i|palm(os)?|phone|p(ixi|re)\\/|plucker|pocket|psp|symbian|treo|up\\.(browser|link)|vodafone|wap|windows (ce|phone)|xda|xiino).*")){
        			bResult = true;
        			break;
        		}
        		else{
        			bResult = false;
        			break;
        		}
        		}
        		  
			catch (Exception e)
			{
				// TODO Auto-generated catch block
				SystemUtils.trace("UserAgent_UK","Exception: "+e.toString());
			}        	
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
    
    
    private Workspace getProviderConnection() throws ServiceException, DataException {
		String providerName = ONT_PROVIDER;
		
		SystemUtils.trace("hays_search2", "provider name to be used =" + providerName);
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


	String callServiceHandler(Service service, String s, Properties properties, String s1) throws ServiceException {
        String resultStr = null;
        DataBinder databinder;
        Properties properties1;
        databinder = service.getBinder();
        if (databinder == null) {
            return null;
        }
        properties1 = databinder.getLocalData();
        databinder.setLocalData(properties);
        HaysNavigationHandler ssservicehandler = (HaysNavigationHandler)service.getHandler("hays.co.uk.HaysNavigationHandler");
        if (ssservicehandler != null) {
            ClassHelper classhelper = new ClassHelper();
            classhelper.m_class = ssservicehandler.getClass();
            classhelper.m_obj = ssservicehandler;
            classhelper.invoke(s);
            resultStr = databinder.getLocal(s1);
        }
        databinder.setLocalData(properties1);
        return resultStr;
    }
	
	String callServiceHandler(Service service, String s, Properties properties, String s1,String genralServiceHandler) throws ServiceException {
        String resultStr = null;
        DataBinder databinder;
        Properties properties1;
        databinder = service.getBinder();
        if (databinder == null) {
            return null;
        }
        properties1 = databinder.getLocalData();
        databinder.setLocalData(properties);
        HaysGeneralServiceHandler ssservicehandler = (HaysGeneralServiceHandler)service.getHandler(genralServiceHandler);
        if (ssservicehandler != null) {
            ClassHelper classhelper = new ClassHelper();
            classhelper.m_class = ssservicehandler.getClass();
            classhelper.m_obj = ssservicehandler;
            classhelper.invoke(s);
            resultStr = databinder.getLocal(s1);
        }
        databinder.setLocalData(properties1);
        return resultStr;
    }

}
