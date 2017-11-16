package infomentum.ontology;



import java.util.Properties;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

import infomentum.ontology.loader.OntologyFacade;
import infomentum.ontology.navigation.OntologyNavigationHandler;
import infomentum.ontology.utils.OntologyUtils;
import intradoc.common.ClassHelper;
import intradoc.common.ExecutionContext;
import intradoc.common.GrammarElement;
import intradoc.common.LocaleResources;
import intradoc.common.LocaleUtils;
import intradoc.common.ScriptExtensionsAdaptor;
import intradoc.common.ScriptInfo;
import intradoc.common.ScriptUtils;
import intradoc.common.ServiceException;
import intradoc.data.DataBinder;
import intradoc.data.ResultSet;
import intradoc.server.Service;
import intradoc.server.script.ScriptExtensionUtils;
import intradoc.shared.UserData;

import com.hp.hpl.jena.ontology.OntModel;

public class OntologyIdocScriptExtension extends ScriptExtensionsAdaptor {
	
	private Service m_service = null;
	private DataBinder m_binder = null;
	private UserData m_userData = null;
	
	public OntologyIdocScriptExtension()	{
		m_functionTable = new String[] {"ontConvertToXml", 
										"ontLoadSiteNavResultSet", 
										"ontGetRelatedTerms", 
										"ontGetMatchingTerms", 
										"ontGetTermLabel"};
		m_functionDefinitionTable = new int[][]
		{
			{0, 3, GrammarElement.STRING_VAL, GrammarElement.STRING_VAL, GrammarElement.STRING_VAL, 0}, // ontConvertToXml
			{1, -1, GrammarElement.STRING_VAL, GrammarElement.STRING_VAL, 0}, // spLoadSiteNavResultSet
			{2, 2, GrammarElement.STRING_VAL, GrammarElement.STRING_VAL,  0}, // ontGetRelatedTerms
			{3, 2, GrammarElement.STRING_VAL, GrammarElement.STRING_VAL,  0}, // ontGetMatchingTerms
			{4, 3, GrammarElement.STRING_VAL, GrammarElement.STRING_VAL, GrammarElement.STRING_VAL, 0} // ontGetTermLabel
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
		case 0:	// ontConvertToXml - converts passed metadata value (list of ont terms)=> xml to buid a tree structure
			String metaValue = sArg1;
			String metaName = sArg2;
			String language = ScriptUtils.getDisplayString(args[2], context);
			StringBuffer rez = null;
			OntologyUtils.debug( "ontConvertToXml started... " + metaName + " => " + metaValue);
			try {
				ResultSet ontMappingRS = OntologyFacade.getOntMapping();
				if( ontMappingRS != null && ontMappingRS.first()) {
					if( metaName.equals( ontMappingRS.getStringValue(0))) {// metadata column
						OntModel model = OntologyFacade.getOntology(metaName);
						Property relationship = model.getProperty( ontMappingRS.getStringValue(1) ); // relationship column
						
						Converter converter = new Converter( model);
						StringBuffer valueXml = converter.convertToXMLList(metaValue, relationship, language, false);
						m_binder.putLocal("xmlTree", valueXml.toString());
					}
				}
				
			} catch(Exception ex) {
				OntologyUtils.debug(ex);
				ex.getStackTrace();
			}
			
			if( rez != null)
				oResult = rez.toString(); 
			else oResult = "";
			break;
		
		case 1: //load ont-based navigation
			String metadataName = sArg1;
            if(metadataName==null || metadataName.trim().length()==0)
                throw new ServiceException("metadata must be specified to build ontology navigation");
       
            String resultName = sArg2;
            if( resultName == null || resultName.length() == 0)
            	resultName = "ONT_NAVIGATION_RESULT";
            
            if( m_binder.getResultSet(resultName) == null) {
        		OntologyUtils.debug("ONT_NAVIGATION_RESULT Result Set is not in the binder so fetch it");
        		
        		Properties properties = new Properties();
                properties.put("ont_metadata", metadataName);
        		callServiceHandler(m_service, "getOntBasedNavigation", properties, "ONT_NAVIGATION_RESULT");
        		
        		ResultSet ontNavigationRS = m_binder.getResultSet("ONT_NAVIGATION_RESULT");
        		if( ontNavigationRS != null &&  !resultName.equals("ONT_NAVIGATION_RESULT")) {
        			m_binder.removeResultSet("ONT_NAVIGATION_RESULT");
        			m_binder.addResultSet(resultName, ontNavigationRS);
        		}
        		OntologyUtils.debug("Complete building ont based navigation: " + ontNavigationRS);
            }
            break;
            
		case 2:	// ontGetRelatedTerms
			String terms = sArg1;
			String meta_relation = sArg2;
			String isObject = "false";
			if( terms != null && meta_relation != null) {
				int index = meta_relation.indexOf("@");
				if( index > 0){
					String metadata = meta_relation.substring(0, index);
					String relation = meta_relation.substring(index + 1);
					index = relation.indexOf("@");
					if( index > 0 ){
						isObject = relation.substring(index+1 );
						relation = relation.substring(0, index );
					}
					Properties properties = new Properties();
	                properties.put("ont_metadata", metadata);
	                properties.put("relation", relation);
	                properties.put("isObject", isObject);
	                properties.put("terms", terms);
	        		String result = callServiceHandler(m_service, "getRelatedTerms", properties, relation+metadata);
	        		
	        		
	        		if( result != null) {
	        			oResult = result; 
	        		} else
	        			oResult = ""; 
	        		OntologyUtils.debug("Related RS: " + oResult);
				}
			}
			break;
			
		case 3: // ontGetMatchingTerms
			String str = sArg1;
			metaName = sArg2;
			Properties properties = new Properties();
            properties.put("ont_metadata", metaName);
            properties.put("str", str);
    		String result = callServiceHandler(m_service, "getMatchingTerms", properties, "terms");
    		if( result != null) {
    			oResult = result; 
    		} else
    			oResult = ""; 
			break;
			
		case 4: // ontGetTermLabel
			metaName = sArg1;
			String termId = sArg2;
			language = ScriptUtils.getDisplayString(args[2], context);
			result = null;
			try {
				result = Converter.getLabel(termId, OntologyFacade.getOntology(metaName), language);
			}catch(Exception ex){}
			if( result != null) {
    			oResult = result; 
    		} else
    			oResult = termId; 
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
        OntologyNavigationHandler ssservicehandler = (OntologyNavigationHandler)service.getHandler("infomentum.ontology.navigation.OntologyNavigationHandler");
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
