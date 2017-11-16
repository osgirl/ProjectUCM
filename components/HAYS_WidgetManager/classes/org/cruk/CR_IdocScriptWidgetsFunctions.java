package org.cruk;


import java.io.StringReader;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang.StringEscapeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

//import sitestudio.SSServiceHandler;


import intradoc.common.ClassHelper;
import intradoc.common.ExecutionContext;
import intradoc.common.GrammarElement;
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
import intradoc.server.Service;
import intradoc.server.ServiceHandler;
import intradoc.server.script.ScriptExtensionUtils;
import intradoc.shared.UserData;

public class CR_IdocScriptWidgetsFunctions extends ScriptExtensionsAdaptor {

	private Service m_service = null;
	private DataBinder m_binder = null;
	private UserData m_userData = null;
	
	public CR_IdocScriptWidgetsFunctions()	{
		m_functionTable = new String[] {"crXmlToResultSet", "crGetChildrenNodes", "strSubstringRE","rssFormatTimeZone"};
		m_functionDefinitionTable = new int[][]
		{
			{0, 2, GrammarElement.STRING_VAL, GrammarElement.STRING_VAL, -1}, // crXmlToResultSet
			{1, 2, GrammarElement.STRING_VAL, GrammarElement.INTEGER_VAL, -1}, // crGetChildrenNodes
			{2, 2, GrammarElement.STRING_VAL, GrammarElement.STRING_VAL, GrammarElement.STRING_VAL}, // strSubstringRE
			{3, 2, GrammarElement.STRING_VAL, GrammarElement.STRING_VAL,GrammarElement.STRING_VAL} //rssFormatTimeZone
		};
	}
	
	/**
	 * This is where the custom IdocScript function is evaluated.
	 */
	public boolean evaluateFunction(ScriptInfo info, Object[] args, ExecutionContext context)
		throws ServiceException
	{
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
		if (allowedParams >= 0 && allowedParams != nargs)
		{
			String msg = LocaleUtils.encodeMessage("csScriptEvalNotEnoughArgs", 
				null, function, ""+allowedParams);
			throw new IllegalArgumentException(msg);
		}
		
		String msg = LocaleUtils.encodeMessage("csScriptMustBeInService", 
			null, function, "Service");
		m_service = ScriptExtensionUtils.getService(context, msg);
		m_binder = m_service.getBinder();
		
		m_userData = (UserData)context.getCachedObject("UserData");
		if (m_userData == null)
		{
			msg = LocaleUtils.encodeMessage("csUserDataNotAvailable", null, function);
			throw new ServiceException(msg);
		}
		
		// Do some initial conversion of arguments.  Choices of what initial conversions to make
		// are based on frequency of usage.  If a function uses nontypical parameters it will
		// have to do its own conversion.
		String sArg1 = null;
		String sArg2 = null;
		long lArg1 = 0;
		long lArg2 = 0;
		if (nargs > 0)		{
			if (config[2] == GrammarElement.STRING_VAL)			{
				sArg1 = ScriptUtils.getDisplayString(args[0], context);
			}
			else if (config[2] == GrammarElement.INTEGER_VAL)			{
				lArg1 = ScriptUtils.getLongVal(args[0], context);
			}
				
		}
		if (nargs > 1)		{
			if (config[3] == GrammarElement.STRING_VAL)			{
				sArg2 = ScriptUtils.getDisplayString(args[1], context);
			}
			else if (config[3] == GrammarElement.INTEGER_VAL)			{
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
		boolean bResult = false;  // Used for functions that return a boolean.
		int iResult = 0; // Used for functions that return an integer.
		double dResult = 0.0;  // Used for functions that return a double.
		Object oResult = null; // Used for functions that return an object (string).		
		
		
		
		switch (config[0])		{
		case 0:	// crXmlToResultSet
			SystemUtils.trace("hays", "IdocScriptCRFunctions started. [crXmlToResultSet] for " + sArg1);
			if( sArg1 == null || sArg1.trim().length() == 0) {
				SystemUtils.trace("hays", "crXmlToResultSet nothing to processs - string is empty"); 
				break;
			}
			try {
				ResultSet rs = generateResultSet(sArg1);
				if( rs != null)
					m_binder.addResultSet(sArg2, rs);
				else {
					SystemUtils.trace("hays", "failed to process XML, ResultSet is not set");
					m_binder.addResultSet(sArg2, new DataResultSet());
				}
			}catch(Exception ex) {
				ex.printStackTrace();
			}
			break;
			
		case 1:	// crGetChildrenNodes - retrieve all direct kids of the given node
			String nodeId = null;
	        String siteId = null;
	        DataResultSet originalNavRS = null;
	        
	        String currentSiteId = m_binder.getLocal("siteId");
	        if( currentSiteId == null )
	        	throw new ServiceException("crGetChildrenNodes IDOC call fails because site ID parameter is missing");
	        int index = sArg1.indexOf(":");
	        if( index <= 0 ) {
	        	nodeId = sArg1;
	        	siteId = currentSiteId;
	        } else {
	        	siteId = sArg1.substring(0, index);
	        	nodeId = sArg1.substring(sArg1.indexOf(":") + 1);
	        }
	        	
	        SystemUtils.trace("hays", "crGetChildrenNodes: siteId =  " + siteId + ", nodeId = " + nodeId);
	        SystemUtils.trace("hays", "current SiteId (from binder): " + currentSiteId);
			ResultSet ssNavNodesRS = m_binder.getResultSet("SiteStudioNavNodes");
	        if (!siteId.equals(currentSiteId) ) {
	        	SystemUtils.trace("hays", "Create navigation for the given site");
	        	if( ssNavNodesRS != null) {
	        		originalNavRS = new DataResultSet();
	        		originalNavRS.copy(ssNavNodesRS);	
	        		SystemUtils.trace("hays", "Original navigation is saved");
	        	}
	        	ssNavNodesRS = loadNavRS( siteId );
	            SystemUtils.trace("hays", "generated NavNodesRS for given siteId: " + ssNavNodesRS);
	        } else if( ssNavNodesRS == null) {
	        	ssNavNodesRS = loadNavRS( siteId );
	        	SystemUtils.trace("hays", "generated NavNodesRS for current siteId: " + ssNavNodesRS);
	        }
	        if (ssNavNodesRS != null) {
		        int generations = new Long(lArg2).intValue();
		        DataResultSet ssNavNodesDRS = new DataResultSet();
		        ssNavNodesDRS.copy(ssNavNodesRS);
		        ResultSet childrenNodesRS = getChildNodes(ssNavNodesDRS, nodeId, generations);
		        m_binder.addResultSet("CRCildNodes", childrenNodesRS);
	        }

        	if(originalNavRS != null ) {
            	m_binder.addResultSet("SiteStudioNavNodes", originalNavRS);
            	SystemUtils.trace("hays", "Put back into binder the original NavRS");
        	}
			break;	
			
		case 2:	// strSubstringRE - extracts substring using the passed Regular Expression
			String origStr = sArg1;
			String regExp = sArg2;
			StringBuffer result = new StringBuffer();
			if( origStr == null || regExp == null )
				throw new ServiceException("parameters for IDOC Script expression are not defined ");
			
			Pattern regex = Pattern.compile(regExp);
			Matcher matcher = regex.matcher(origStr);
			boolean matchFound = matcher.find();
			if (matchFound && matcher.groupCount() > 0) {		 
		        for (int i = 1; i <= matcher.groupCount(); i++) {
		        	result.append(matcher.group(i)).append(",");
		        }
		        result.substring(0, result.length()-1);
		    }
			oResult = result.toString();
			break;
		case 3:	// rssFormatTimeZone - Formats Time Zone in format GMT or IST etc
			String dateString = sArg1;
			String strDateFormat = sArg2;
			SimpleDateFormat sdf1 = new SimpleDateFormat(strDateFormat);
			Date date=new Date();
			try{
				date = sdf1.parse(dateString);
			}catch(Exception e){
				
			}
			
			SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat+" zzz");
			
			if(sdf.format(date).indexOf("BST") > 0){
				sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
				oResult = sdf.format(date);
			}else{
				oResult = sdf.format(date);
			}
			break;
		default:
			return false;
		}
		
		/**
		 * Do not alter code below here
		 */
		args[nargs] = ScriptExtensionUtils.computeReturnObject(config[4],
			bResult, iResult, dResult, oResult);

		// Handled function.
		return true;
	}
	
	
	
	private DataResultSet generateResultSet(String xmlStr) throws ServiceException, DataException {
		Document dom = null;
		SystemUtils.trace("hays", "generateResultSet() " + xmlStr);
		if(xmlStr != null && xmlStr.startsWith("&lt;")){
			xmlStr = StringEscapeUtils.unescapeXml(xmlStr);
		}
		try {
			dom = processXML(xmlStr);
			if( dom != null)
				return extractNodesSimple(dom);
		} catch(ServiceException ex) {			
			xmlStr = "<p>" + xmlStr + "</p>";
			try {
				dom = processXML(xmlStr);
				if( dom != null)
					return extractNodes(dom);
			} catch(Exception ex2) {
				//ex2.printStackTrace();
			}
			
		}
		
		return null;
	}

	/**
	 * Processes XML string
	 * 
	 * @param xmlFile -
	 *            MPCT navigation file
	 * @return updated MPCT navigation file
	 * @throws ServiceException
	 */
	public static Document processXML(String xmlString) throws ServiceException {
		Document m_document = null;
		
		DocumentBuilderFactory documentbuilderfactory = DocumentBuilderFactory
				.newInstance();
		documentbuilderfactory.setNamespaceAware(true);
		try { 
			m_document = documentbuilderfactory.newDocumentBuilder()
					.parse(new InputSource(new StringReader(xmlString)));
			
		} catch (Exception ex) {
			SystemUtils.trace("hays", "Exception: " + ex);
			throw new ServiceException(
					"Exception parsing XML string: " + ex);
		}
		SystemUtils.trace("hays", "processXML Document =  " + m_document);
		return m_document;
	}
	
	private DataResultSet extractNodesSimple(Document m_document) throws ServiceException, DataException {
		Node node = null;
		//	Properties properties = new Properties();
			DataResultSet drs = null;
			DataBinder row = new DataBinder();
			Vector<String> fields = new Vector<String>();

	         // and all nodes
			SystemUtils.trace("hays", "root: " + m_document.getDocumentElement());
		     NodeList nodes = m_document.getDocumentElement().getChildNodes();
		     SystemUtils.trace("hays", "length: " + nodes.getLength());
		    
			
		     String name = null;
		     for(int k=0; k < nodes.getLength(); k++) {
	          		node =  nodes.item(k);
	          		SystemUtils.trace("hays", "node: " + node);
	          		if( node.getNodeType() == Node.ELEMENT_NODE) {
	          			if( node.getFirstChild() != null) {
		          				name = node.getNodeName();
		          				if(row.getLocal(name) != null) {
		          					if( drs == null) {
		          						String as[] = StringUtils.convertListToArray(fields);
		          						drs = new DataResultSet(as);
		          					}
		          					drs.addRow( drs.createRow(row)) ;
		          					row = new DataBinder();
		          				} 
		          				row.putLocal(name, node.getFirstChild().getNodeValue());  
		          				if( drs == null) {
		          					fields.add(name);
		          				}
		          				SystemUtils.trace("hays", "Name= " + node.getNodeName() + " =  " +  node.getFirstChild().getNodeValue());
	          				}
	          			}
		      }
		     if( !row.getLocalData().isEmpty()){
		    	 if( drs == null){
		    		String as[] = StringUtils.convertListToArray(fields);
					drs = new DataResultSet(as);
		    	 }
		    	 drs.addRow( drs.createRow(row)) ;
		     }
		     
	        return drs;
	}
	
	
	private DataResultSet extractNodes(Document m_document) throws ServiceException, DataException {
		Node node = null;
		//	Properties properties = new Properties();
			DataResultSet drs = null;
			DataBinder row = new DataBinder();
			Vector<String> fields = new Vector<String>();

	         // and all nodes
			SystemUtils.trace("hays", "root: " + m_document.getDocumentElement());
		     NodeList nodes = m_document.getDocumentElement().getChildNodes();
		     SystemUtils.trace("hays", "length: " + nodes.getLength());
		     NodeList kids = null;
			
		     String name = null;
		     for(int k=0; k < nodes.getLength(); k++) {
	          		node =  nodes.item(k);
	          		SystemUtils.trace("hays", "node: " + node);
	          		if( node.getNodeType() == Node.ELEMENT_NODE) {
	          			if( node.getFirstChild() != null) {
	          				kids = node.getChildNodes();
	          				for(int i=0; i < kids.getLength(); i++) {
	          					node = kids.item(i);
		          				name = node.getNodeName();
		          			//	System.out.println("name: " + name);
		          				if(row.getLocal(name) != null) {
		          					if( drs == null) {
		          						String as[] = StringUtils.convertListToArray(fields);
		          						drs = new DataResultSet(as);
		          					}
		          					drs.addRow( drs.createRow(row)) ;
		          					row = new DataBinder();
		          				} 
		          				row.putLocal(name, node.getFirstChild().getNodeValue());  
		          				if( drs == null) {
		          					fields.add(name);
		          				}
		          				SystemUtils.trace("hays", "Name= " + node.getNodeName() + " =  " +  node.getFirstChild().getNodeValue());
	          				}
	          			}
	          		}
		      }
		     if( !row.getLocalData().isEmpty()){
		    	 if( drs == null){
		    		String as[] = StringUtils.convertListToArray(fields);
					drs = new DataResultSet(as);
		    	 }
		    	 drs.addRow( drs.createRow(row)) ;
		     }
		     
	        return drs;
	}
	
	private ResultSet loadNavRS(String siteId) throws ServiceException {
		Properties properties = new Properties();
        properties.put("siteId", siteId);
        String ssUrlPrefix = m_binder.getLocal("ssUrlPrefix");
        if (ssUrlPrefix != null && ssUrlPrefix.length() > 0) {
            properties.put("ssUrlPrefix", ssUrlPrefix);
        }
        String SSContributor = m_binder.getLocal("SSContributor");
        if (SSContributor != null && SSContributor.length() > 0) {
            properties.put("SSContributor", SSContributor);
        }
        callServiceHandler(m_service, "loadSiteNavResultSet", properties, "");
        ResultSet ssNavNodesRS = m_binder.getResultSet("SiteStudioNavNodes");
        SystemUtils.trace("hays", "generated NavNodesRS : " + ssNavNodesRS);
        
        return ssNavNodesRS;
	}
	
	
	private ResultSet getChildNodes(DataResultSet navRS, String nodeId, int generation) {
		DataResultSet resultRS = new DataResultSet();	
		resultRS.copyFieldInfo(navRS);
		int levelFrom = 1000;
		int levelTo = 1000;
		int level = -1;
		boolean isFound = false;
		SystemUtils.trace("hays", "navRes RS rows =  " + navRS.getNumRows());
		if( navRS == null || ! navRS.first())
			return null;
		
		do {
			if( !isFound && navRS.getStringValueByName("nodeId").equals( nodeId ) ){
				levelFrom = Integer.parseInt( navRS.getStringValueByName("level") );
				levelTo = levelFrom + generation;
				isFound = true;				
				SystemUtils.trace("hays", "parent node is found: " + navRS.getStringValueByName("label"));
				SystemUtils.trace("hays", "level from: " + levelFrom + ", levelTo: " + levelTo);
			}
			else if( isFound ) {
				level = Integer.parseInt( navRS.getStringValueByName("level") );
			//	SystemUtils.trace("hays", "level: " + level);
				if( level > levelFrom && level <= levelTo ) {
					resultRS.addRow( navRS.getCurrentRowValues());
					SystemUtils.trace("hays", "add new row  =  " + navRS.getCurrentRowValues() );
				} else if( level <= levelFrom)
					break;
			}
			
		}while( navRS.next());
		
		return resultRS;
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
	        ServiceHandler ssservicehandler = service.getHandler("sitestudio.SSServiceHandler");
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
	 
	 
	 public static void main(String[] args) throws Exception {
		 String val = "<payrange><jobtype>P</jobtype><min>100.9</min><max>200.00</max><paytype>A</paytype></payrange><payrange><jobtype>P</jobtype><min>100.9</min><max>200.00</max><paytype>A</paytype></payrange>";
		 CR_IdocScriptWidgetsFunctions ad = new CR_IdocScriptWidgetsFunctions();
		 DataResultSet rez = ad.generateResultSet(val);
		 System.out.println(rez);
		
		 
	 }
 
}
