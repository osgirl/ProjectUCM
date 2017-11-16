package infomentum.ontology.loader;




import java.util.Iterator;
import java.util.Vector;

//import oracletextsearch.server.OracleTextIndexerServiceHandler;  //commented for automation


import infomentum.ontology.Converter;
import infomentum.ontology.navigation.OntologyNavigationHandler;
import infomentum.ontology.utils.OntologyUtils;
import intradoc.common.LocaleResources;
import intradoc.common.ServiceException;
import intradoc.data.DataException;
import intradoc.data.DataResultSet;
import intradoc.data.ResultSet;
import intradoc.data.ResultSetUtils;
import intradoc.server.ServiceHandler;
import intradoc.shared.SharedObjects;

public class OntologyGeneralHandler extends ServiceHandler {
	
	public void setEnvProperty() {
		String propertyName = m_binder.getLocal("propName");
		if( propertyName != null){
			String property = SharedObjects.getEnvironmentValue(propertyName);
			m_binder.putLocal(propertyName, property);
			
		}
		OntologyUtils.debug("OS Properties: " + SharedObjects.getOSProperties());
		OntologyUtils.debug("Environment: " + SharedObjects.getSafeEnvironment());
		OntologyUtils.debug("Binder env: " + m_binder.getEnvironment());
	}
	
	public void getOntologyForMetadata() throws ServiceException, DataException {
		String isOntBased  = "0";
		String metadata = m_binder.getLocal("ont_metadata");
		if( metadata != null && OntologyFacade.isMetadataOntologyBased(metadata)){
			isOntBased = "1";
		} 
		OntologyUtils.debug("getOntologyForMetadata() " + metadata + ": " + isOntBased);
		m_binder.putLocal("isOntologyBasedMetadata", isOntBased);
	}
	
	public void getMetadataForOntology() throws ServiceException, DataException{
		DataResultSet mappingDRS = (DataResultSet)OntologyFacade.getOntMapping();
		DataResultSet metadataDefRS = SharedObjects.getTable("DocMetaDefinition");
		DataResultSet result = new DataResultSet();
		result.copy(mappingDRS);
		result.renameField("metadata", "dName");
		Vector<String> fields = new Vector<String>();
		fields.add("dCaption");
		result.appendFields(fields);
		OntologyUtils.debug("getMetadataForOntology() : " + result);
		result.merge("dName", metadataDefRS, true);
		result.renameField("dName", "metadata");
		m_binder.addResultSet("OntMetadataRS", result );
	}
	
	public void getAllMetadataForOntology() throws ServiceException, DataException {
		DataResultSet metadataDefRS = SharedObjects.getTable("DocMetaDefinition");
		DataResultSet ontMetadataDRS = new DataResultSet();
		ontMetadataDRS.copyFieldInfo(metadataDefRS);
		ontMetadataDRS.copySimpleFiltered(metadataDefRS, "dType", "Memo");
		ontMetadataDRS.renameField("dName", "metadata");
		ResultSet ontMapping = OntologyFacade.getOntMapping();
		if( ontMapping != null){			
			ontMetadataDRS.mergeDelete("metadata", ontMapping, false);
			OntologyUtils.debug("Metadata for ontology: " + ontMetadataDRS);
		}
		m_binder.addResultSet("OntMetadataRS", ontMetadataDRS);
	}
	
	public void getOntologyClasses() throws ServiceException, DataException {
		String metadata = m_binder.getLocal("ont_metadata");
		if( metadata == null )
			throw new DataException("Metadata parameter is not set");
		m_binder.addResultSet("OntClassesRS",  OntologyFacade.getOntUserClasses(metadata));
		
	}
	
	public void getPropertiesForClass() throws ServiceException, DataException {
		String metadata = m_binder.getLocal("ont_metadata");
		String classURI = m_binder.getLocal("ont_class");
		OntologyUtils.debug("getPropertiesForClass(): " + metadata + ", " + classURI);
		if( metadata == null || classURI == null)
			throw new DataException("Mandatory fields are not set");
		m_binder.addResultSet("OntPropertiesRS",  OntologyFacade.getOntPropertiesRangedClass(metadata, classURI));
		
	}
	
	public void getSecurityForOntology() throws ServiceException, DataException {
		String metadata = m_binder.getLocal("ontMetadata");
		if( metadata == null )
			throw new DataException("Metadata parameter is not set");
		
		DataResultSet m_ontSecurityConfig = new DataResultSet(OntologyUtils.ONT_SECURITY_FIELDS);
	
		ResultSet rolesRS = m_binder.getResultSet("RolesList");
		if( rolesRS != null && !rolesRS.isEmpty()) {
			m_ontSecurityConfig.renameField("securityGroup", "dRoleName");
			m_ontSecurityConfig.merge( "dRoleName", rolesRS, false);
			m_ontSecurityConfig.renameField("dRoleName", "securityGroup" );
		}
		OntologyUtils.debug("Security roles " + m_ontSecurityConfig);
		
		ResultSet ontSecurityConfig = OntologyFacade.getOntSecurity(metadata);
		OntologyUtils.debug("Security Config: Security " + ontSecurityConfig);
		if( ontSecurityConfig != null){
			m_ontSecurityConfig.merge("securityGroup", ontSecurityConfig, true);
			OntologyUtils.debug("Security Config after merging: " + m_ontSecurityConfig);
		}
		
		Vector<String> field = new Vector<String>();
		field.add("RootLabel");
		m_ontSecurityConfig.appendFields(field);
		String rootId, label = null;
		if( m_ontSecurityConfig.first()) {
			do {
				rootId = m_ontSecurityConfig.getStringValue(1); // root
				if( rootId != null && rootId.length() > 0 ) {
					label = Converter.getLabel(rootId, OntologyFacade.getOntology(metadata), null);
					//OntologyUtils.debug("Extracted label for " + rootId + " = " + label);
					m_ontSecurityConfig.setCurrentValue(2, label);
				}
			}while(m_ontSecurityConfig.next());
		}
		m_binder.addResultSet("OntSecurityConfRS", m_ontSecurityConfig);
	}
	
	
	
	
}
