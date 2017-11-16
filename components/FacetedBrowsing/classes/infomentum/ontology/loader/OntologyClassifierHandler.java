package infomentum.ontology.loader;


import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

import infomentum.ontology.Converter;
import infomentum.ontology.utils.OntologyUtils;
import intradoc.common.LocaleResources;
import intradoc.common.ServiceException;
import intradoc.common.StringUtils;
import intradoc.common.SystemUtils;
import intradoc.data.DataException;
import intradoc.data.DataResultSet;
import intradoc.data.ResultSet;
import intradoc.data.ResultSetUtils;
import intradoc.data.SimpleResultSetFilter;
import intradoc.shared.SecurityUtils;
import intradoc.shared.SharedObjects;
import intradoc.shared.UserAttribInfo;
import intradoc.shared.UserData;

public class OntologyClassifierHandler extends OntologLoaderHandler  {
	private static Pattern TERM_ID_REGEXP = Pattern.compile("item id=\"([^\"]*)\"",  Pattern.MULTILINE);
	
	public static Pattern LOCALE_REG = Pattern.compile("(\\w+)[-_](\\w+)");
	
	
	
	public void getOntStructure() throws ServiceException, DataException {
		String metadata = m_binder.getLocal("ont_metadata");
		
		if(metadata == null )
			throw new DataException("Metadata is not specified");
		
		String lang = m_binder.getLocal("language");
		String country  = m_binder.getLocal("country");
		if(lang == null || country == null)
			throw new DataException("Locale is not specified");
		
		String[] rootElements = getRootElementsForUser( this.m_service.getUserData(), metadata );
		OntologyUtils.debug("getOntStructure(): rootElements: " + rootElements);
		
		OntModel model = OntologyFacade.getOntology(metadata);
		ResultSet mapping = OntologyFacade.getOntMapping();//readOntMetadataMapping(OntologyUtils.META_CONF_RS_NAME, OntologyUtils.ONT_CONF_FILE);
		Map<String,String> prefixMap = OntologyFacade.getOntPrefixes(metadata);
		String relation, clas = null;
		StringBuffer xmlStr = new StringBuffer();
		if( mapping != null && mapping.first()){
			do {
				if(metadata.equals( mapping.getStringValue(0)) ) {
					relation = mapping.getStringValue(1);
					clas = "http://www.hays.co.uk/ont#Grouping";
					SystemUtils.trace("ontology", "Class value is"+clas);
					Converter con = new Converter(model);
					if( rootElements != null) {
						//xmlStr = con.secureConvertToXML(rootElements);
					} else {
						
						xmlStr = con.convertToXML(null, model.getOntClass(clas), model.getProperty(relation), lang, country);
					}
					break;
				}
			} while( mapping.next());
		}
		m_binder.putLocal("xmlTree", xmlStr.toString());
	}
	
	
	/**
	 * Generate XML structure using the ;-separated list of ontology terms IDs. 
	 * It is used to build a flat tree (list) of already assigned metadata values, for 
	 * example of the Info Page. The list contains ontology terms ids and their labels in
	 * the given language.
	 * 
	 * Mandatory parameters are: 
	 * - metadata: to extract the associated ontology structure;
	 * - ont_metadata_value: terms ids as a ;-separated list
	 * - language: language code to select the terms' labels
	 * 
	 * @throws ServiceException
	 * @throws DataException
	 */
	public void getMetadataValuesAsStructure() throws ServiceException, DataException {
		OntologyUtils.debug("Starting getMetadataValuesAsStructure() ..."); 
		String metadata = m_binder.getLocal("ont_metadata");		
		if(metadata == null )
			throw new DataException("Metadata is not specified");
		String lang = m_binder.getLocal("language");
		if(lang == null   )
			throw new DataException("Locale is not specified");
		
		// check in case the whole locale (language-country) is passed instead of the language
		Matcher matcher = LOCALE_REG.matcher(lang);
	    if (matcher.find() && matcher.groupCount() > 1) {
	    	lang = matcher.group(1);
	    }
		
		// ;-separated list of value
		String metaValue = m_binder.getLocal("ont_metadata_value");
		OntologyUtils.debug("getMetadataValuesAsStructure(): ont_metadata_value = " + metaValue);
		
		boolean isParentIncl = StringUtils.convertToBool(m_binder.getLocal("isFullLabel"), false);
		//OntologyUtils.debug("getMetadataValuesAsStructure(): is full label? " + isParentIncl);
		
		// retrieve ontology configuration
		ResultSet ontMappingRS = OntologyFacade.getOntMapping();
		if( ontMappingRS != null && ontMappingRS.first()) {
			do {
				if( metadata.equals( ontMappingRS.getStringValue(0))) {// metadata column
					OntModel model = OntologyFacade.getOntology(metadata);
					Property relationship = model.getProperty( ontMappingRS.getStringValue(1) ); // relationship column
					Individual parentEntity = null;
					Converter converter = new Converter( model);
					StringBuffer valueXml = converter.convertToXMLList(metaValue, relationship, lang, isParentIncl);
					m_binder.putLocal("xmlTree", valueXml.toString());
				}
			} while(ontMappingRS.next());
		}
		//}
	}
	
	
	/**
	 * Extracts terms from the Ontology model that is assigned to the passed metadata. Extracted
	 * entities must be of the given class (ontClass parameter), have labels in the given language
	 * and assigned to the given country.
	 * 
	 * Optionally the extracted list might be limited to the given result count (ResultCount).
	 * If the root element uri is passed (parentTerm) then only entities that are in the main
	 * relationship (assigned at ontology loading time) with the root element are used.
	 * 
	 * In case a search term is passed (ontValue) then the extracted list is filtered further
	 * to include only entities whose labels contain this search term. Used for auto-suggest functionality.
	 * 
	 * Extracted entities are used to build a result set (id, label, uri), which is, if not null, 
	 * added to the binder under the name of 'IndividualsList'.
	 * 
	 * @throws DataException
	 * @throws ServiceException
	 */
	public void extractIndividuals() throws DataException, ServiceException  {
		String metadata = m_binder.getLocal("metadata");	
		if(metadata == null )
			throw new DataException("Metadata is not specified");
		String ontClass = m_binder.getLocal("ontClass");
		if(ontClass == null )
			throw new DataException("Ontology class is not specified");
		String lang = m_binder.getLocal("language");
		String country  = m_binder.getLocal("country");
		if(lang == null || country == null )
				throw new DataException("Locale is not specified");
		
		String resultCount = m_binder.getLocal("ResultCount");
		String parentTerm = m_binder.getLocal("parentTerm");
		String value = m_binder.getLocal("ontValue");
		OntologyUtils.debug("extract individuals passed parameters: " + m_binder.getLocalData());
		
		DataResultSet individualsDRS = null;
		OntModel model = OntologyFacade.getOntology(metadata);
		// retrieve ontology configuration
		ResultSet ontMappingRS = OntologyFacade.getOntMapping();
		if( ontMappingRS != null && ontMappingRS.first()) {
			do {
				if( metadata.equals( ontMappingRS.getStringValue(0))) {// metadata column
					Property relationship = model.getProperty( ontMappingRS.getStringValue(1) ); // relationship column
					OntClass resourceClass = model.getOntClass( model.expandPrefix(ontClass) );
					Individual parentEntity = null;
					if(parentTerm != null && parentTerm.length() >0) {
						parentEntity  = model.getIndividual( model.expandPrefix(parentTerm ));
					}
					Converter converter = new Converter( model);
					individualsDRS = converter.getIndividuals(resourceClass, parentEntity, relationship, lang, country, value);
				}
			} while(ontMappingRS.next());
		}
		
		// sort the result set
		if( individualsDRS != null) {
			//Relay 24625
			String sortById = m_binder.getEnvironmentValue("SpecialismSortById");
			String locale = lang+"-"+country;
			if(sortById != null && sortById.contains(locale)){
				ResultSetUtils.sortResultSet(individualsDRS, new String[]{"id"});
			}
			else{
				ResultSetUtils.sortResultSet(individualsDRS, new String[]{"label"});
			}
			//Relay 24625
	
			OntologyUtils.debug("After sorting: " + individualsDRS);
			if( resultCount != null) {
				int start = Integer.parseInt(resultCount);
				if( start > 0 && start < individualsDRS.getNumRows()){
					for( int i = individualsDRS.getNumRows()-1; i >= start; i--) {
						individualsDRS.deleteRow(i);
					}
				}
			}
		}
		OntologyUtils.debug("after deleting row..." + individualsDRS);
		
		if( individualsDRS != null)
			m_binder.addResultSet("IndividualsList", individualsDRS);
	}
	
	
	
	public void getTermLabel() throws DataException, ServiceException  {
		String metadata = m_binder.getLocal("metadata");		
		if(metadata == null )
			throw new DataException("Metadata is not specified");
		String termId = m_binder.getLocal("termId");
		// get the locale
		String locale = m_binder.getLocal("value");
		if( locale != null) {
			Matcher matcher = LOCALE_REG.matcher(locale);
		    if (matcher.find() && matcher.groupCount() > 1) {
		    	locale = matcher.group(1);
		    }
		} else {
			locale = LocaleResources.getSystemLocale().m_languageId;
		}
		String label = Converter.getLabel(termId, OntologyFacade.getOntology(metadata), locale);
		if( label == null)
			label = "";
		m_binder.putLocal("termLabel", label);
	}
	
	public void getTermOntClass() throws DataException, ServiceException  {
		String metadata = m_binder.getLocal("metadata");		
		if(metadata == null )
			throw new DataException("Metadata is not specified");
		String termId = m_binder.getLocal("termId");
		// get the locale
		String locale = m_binder.getLocal("value");
		if( locale != null) {
			Matcher matcher = LOCALE_REG.matcher(locale);
		    if (matcher.find() && matcher.groupCount() > 1) {
		    	locale = matcher.group(1);
		    }
		} else {
			locale = LocaleResources.getSystemLocale().m_languageId;
		}
		String label = Converter.getLabel(termId, OntologyFacade.getOntology(metadata), locale);
		if( label == null)
			label = "";
		m_binder.putLocal("termLabel", label);
	}
	
	public void extractTermsFromXml() {
		StringBuffer result = new StringBuffer();
		String metaValueAsXml = m_binder.getLocal("xmlString");
		if( metaValueAsXml != null ) {
			Matcher matcher = TERM_ID_REGEXP.matcher(metaValueAsXml);
			while (matcher.find()) {	
				result.append(";").append( matcher.group(1)) ; // term id
		    }
			if( result.length() > 0)
				result.append(";");
		}
		m_binder.putLocal("metadataValue", result.toString());
	}
	
	
	
	protected String[] getRootElementsForUser(UserData user, String metadata) throws DataException, ServiceException  {
		Vector<String> roots = new Vector<String>();
		Vector<UserAttribInfo> roles = SecurityUtils.getRoleList(user);
		// check if it is an Ontology Admin
		if( SecurityUtils.isUserOfRole(user, SharedObjects.getEnvironmentValue("OntologyAdminRole"))) {
			return null;
		}
		ResultSet ontSecurityConfig = OntologyFacade.getOntSecurity(metadata);
		
		String role, root = null;
		if( ontSecurityConfig != null && ontSecurityConfig.first()){
			do {
				role = ontSecurityConfig.getStringValue(0); // role
				root = ontSecurityConfig.getStringValue(1);  // root element URI
			//	OntologyUtils.debug("Role: Root : " + role + " - " + root);
				for(int i =0; i<roles.size(); i++) {
			//		OntologyUtils.debug("compare: " + roles.get(i).m_attribName + " - " + roles.get(i).m_attribName.equals(role));
					if( roles.get(i).m_attribName.equals(role) && !roots.contains(root)) {
						roots.add( root );						
					}
				}
			} while(ontSecurityConfig.next());
		}
		OntologyUtils.debug("Root elements for " + user + " : " + roots);
		if( roots.size() == 0)
			return null;
		String[] rootElements = new String[roots.size()];
		roots.copyInto(rootElements);
		return rootElements;
	}

}
