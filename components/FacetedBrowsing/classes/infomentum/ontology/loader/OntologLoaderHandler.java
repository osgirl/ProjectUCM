package infomentum.ontology.loader;


import java.io.File;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Map;
import java.util.Vector;

import org.mindswap.pellet.jena.PelletReasonerFactory;


import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;




import infomentum.ontology.OntologyCacheMinder;
import infomentum.ontology.utils.OntologyUtils;
import intradoc.common.FileUtils;
import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.data.DataException;
import intradoc.data.DataResultSet;
import intradoc.data.ResultSet;
import intradoc.data.ResultSetUtils;
import intradoc.server.ServiceHandler;
import intradoc.shared.RoleDefinitions;
import intradoc.shared.SharedObjects;

public class OntologLoaderHandler extends ServiceHandler {
	
	
	
	public void saveOntology() throws ServiceException, DataException {
		OntologyUtils.debug("STARTING ONTOLOGY CREATION");
		
		String nextStep = "0"; // confirm save but do not procede to configuring
		
		String ontFilePath = m_binder.getLocal("ontFilePath:path");
		
		if( ontFilePath == null || ontFilePath.length() == 0) {
			ontFilePath = SharedObjects.getEnvironmentValue("WeblayoutDir") + "FacetedBrowsing/jars/project/model.owl";
		}else
			nextStep = "1";
		
		OntologyUtils.debug("NEXT STEP : " + nextStep);
		
		String metadata = m_binder.getLocal("ontMetadata");		
		if( metadata == null )
			throw new DataException("Mandatory field 'Metadata' is not specified.");
		
		
		OntologyUtils.debug("Passed params: " + ontFilePath + ", " + metadata );
		
		 try {
			 
			 OntologyUtils.debug("INSIDE TRY BLOCK 1");
			 
			OntModel model = OntologyReader.readOntology(null, ontFilePath); 			
			
			// save ontology into DB
			OntologyFacade.writeOntology(metadata, model, ontFilePath);		
			
			// update ontology - metadata association in conf. HDA file
			OntologyFacade.updateOntMapping(metadata, "", "");				
			
			m_binder.putLocal("nextStep", nextStep);
			m_binder.putLocal("modelSize", String.valueOf(model.size()));
			m_binder.putLocal("ontMetadata", metadata);
			
			OntologyUtils.debug("MBINDER VALUES  " + m_binder);
		 } catch(Exception ex) {
			 OntologyUtils.debug("EXCEPTION: " + ex);
				 deleteOntology();
			// } catch (Exception ex) {}
		 } 
	}
	
	public void updateOntology() throws ServiceException, DataException {
		
		String metadata = m_binder.getLocal("ontMetadata");
		String ActiveUserName = m_binder.getLocal("dUser");
		SystemUtils.info("User '"+ ActiveUserName + new StringBuffer("' is trying to change the Ontology data for :  ").append(metadata));
		OntologyUtils.debug("(updateOntology) starting...");
		String ontFilePath = m_binder.getLocal("ontFilePath:path");
		//String metadata = m_binder.getLocal("ontMetadata");		
		if( metadata == null )
			throw new DataException("Mandatory field 'Metadata' is not specified.");
		OntologyUtils.debug("Passed params for update: " + ontFilePath + ", " + metadata );
		OntModel model = OntologyReader.readOntology(null, ontFilePath);
		 try {
			// save ontology into DB
			 OntologyFacade.updateOntology(metadata, model, ontFilePath);
			m_binder.putLocal("modelSize", String.valueOf(model.size()));
			m_binder.putLocal("ontMetadata", metadata);
			m_binder.putLocal("status", "success");
			SystemUtils.info("User '"+ ActiveUserName + new StringBuffer("' has successfully changed the Ontology data for :  ").append(metadata));
		 } catch(Exception ex) {
			// try {
			    ex.printStackTrace();
				 deleteOntology();
			// } catch (Exception ex) {}
		 } 
	}
	
	public void clearOntologyCache() throws ServiceException, DataException {
		String metadata = m_binder.getLocal("ontMetadata");		
		if( metadata == null )
			throw new DataException("Mandatory field 'Metadata' is not specified.");
		OntologyFacade.clearOntologyCache(metadata);
		SystemUtils.info(" Ontology cache has been successfully cleared ");
	}
	
	
	public void getOntologyFile() throws DataException, ServiceException{
		String metadata = m_binder.getLocal("ontMetadata");		
		if( metadata == null )
			throw new DataException("Mandatory field 'Metadata' is not specified.");
		File ontFile = OntologyFacade.getOntologyFile(metadata);
		if( ontFile != null) {
			m_binder.putLocal("ontFilePathAbsolute", FileUtils.getAbsolutePath(ontFile.getPath()));
			m_binder.putLocal("ontFilePath", ontFile.getPath());
		
			// check its project file
			String projectFilePath = ontFile.getParent() + ontFile.separator + "model.pprj";
			File projectFile = new File(projectFilePath);
			if( !projectFile.exists() ) {
				// get weblayout directory
				String pathToEmptyProject = SharedObjects.getEnvironmentValue("WeblayoutDir") + "FacetedBrowsing/jars/project/model.pprj";
				FileUtils.copyFile(pathToEmptyProject, projectFilePath);				
			}
			m_binder.putLocal("ontProjectPathAbsolute", FileUtils.getAbsolutePath(projectFile.getPath()));
			m_binder.putLocal("ontProjectPath", projectFile.getPath());
		}
	}
	
	
	public void deleteOntology() throws ServiceException, DataException {
		String metadata = m_binder.getLocal("ontMetadata");
		
		if( metadata == null )
			throw new DataException("Mandatory field 'Metadata' is not specified.");
		OntologyFacade.deleteOntology(metadata);
		OntologyUtils.debug("Ontology for " + metadata + " was deleted");
		
	}
	
	@SuppressWarnings("unchecked")
	public void saveOntologySecurity() throws ServiceException, DataException {
		String metadata = m_binder.getLocal("ontMetadata");
		if( metadata == null )
			throw new DataException("Mandatory field 'Metadata' is not specified.");
		
		RoleDefinitions roledefinitions = (RoleDefinitions)SharedObjects.getTable("RoleDefinition");
		
		Map<String,String> prefixMap = OntologyFacade.getOntPrefixes(metadata);
		
		// map security roles to ontology terms
		DataResultSet ontSecDRS = new DataResultSet(OntologyUtils.ONT_SECURITY_FIELDS);
		Vector<String> row  = null;
		String role, ontRoot, prefix = null;
		int index = -1;
		for(Enumeration<String> it = roledefinitions.getRoles().keys(); it.hasMoreElements();){
			role = it.nextElement();
			row = new Vector<String>();
			row.add(role); 
			ontRoot = m_binder.getLocal(role + "_ont");
			if( ontRoot != null && ontRoot.length() > 0) {
				if( prefixMap != null) {
					index = ontRoot.indexOf(":");
					if( index == 0)
						prefix = "";
					else {
						prefix = ontRoot.substring(0, index);
					}
					
					prefix = prefixMap.get(prefix);
					row.add(prefix  + ontRoot.substring(index+1));
				} else{
					row.add( ontRoot);
				}				
				ontSecDRS.addRow(row);
			}
			OntologyUtils.debug("saveOntologySecurity(): Security RS: " + ontSecDRS);
		}
		
		// update cache and files
		if( !ontSecDRS.isEmpty()){
			OntologyFacade.updateOntSecurity(ontSecDRS, metadata);
		}
	}
	
	
	public void getOntologyInfo() throws ServiceException, DataException {
		String metadata = m_binder.getLocal("ontMetadata");
		if( metadata == null)
			return;
		OntModel model = OntologyFacade.getOntology(metadata);
		m_binder.putLocal("modelSize", String.valueOf( model.getGraph().size()) );
		
		ResultSet mappingRS = OntologyFacade.getOntMapping();
		m_binder.addResultSet("OntMappingRS", mappingRS );
		String classRoot = ResultSetUtils.findValue(mappingRS, "metadata", metadata, "class_root");
		if( classRoot == null || classRoot.length() == 0 ) {
			m_binder.putLocal("toBeConfigured", "1");
		} else
			m_binder.putLocal("toBeConfigured", "0");
		ResultSet securityRS = OntologyFacade.getOntSecurity(metadata);
		if( securityRS != null) {
			m_binder.addResultSet("OntMappingRS", securityRS);
		}
		
	}
	
/*	
	public ResultSet readOntologySecurity() throws ServiceException, DataException {
		return readOntMetadataMapping(OntologyUtils.META_SECURITY_RS_NAME, OntologyUtils.ONT_CONF_SECURITY_FILE);
	}
*/	
	public void saveOntologyConfig() throws ServiceException, DataException {
		String metadata = m_binder.getLocal("ontMetadata");
		String hierarchRelation = m_binder.getLocal("ontRelationship");
		String rootClass = m_binder.getLocal("ontClass");
		String ActiveUserName = m_binder.getLocal("dUser");
		OntologyUtils.debug(new StringBuffer("saveOntologyConfig(): ").append(metadata).append(", ").append(hierarchRelation).append(", ").append(rootClass).toString());
		
		SystemUtils.info("User '" +ActiveUserName + new StringBuffer("' has changed the Ontology Configuration :  ").append(metadata).append(", ").append(hierarchRelation).append(", ").append(rootClass).toString());
		SystemUtils.info("binder = "+ m_binder);
		if( metadata == null || hierarchRelation == null || rootClass == null)
			throw new DataException("Mandatory fields are not specified.");
		
		// update cacje and file
		OntologyFacade.updateOntMapping(metadata, rootClass, hierarchRelation);
	}
	

	/**
	 * OntologyMetadataRS result set: metadata, relation, root
	 * @param metadata
	 * @throws ServiceException
	 *//*	
	protected ResultSet readOntMetadataMapping(String resultSetName, String fileName) throws ServiceException, DataException {
		String basedir = OntologyUtils.getOntConfDir();
		DataBinder binder = new DataBinder();
		ResourceUtils.serializeDataBinder(basedir, fileName , binder, false, false);
		return (binder.getResultSet(resultSetName));
	}
*/	
	
	
	
	
	public void getOntChildren() throws DataException, ServiceException {
		String metadata = m_binder.getLocal("ontMetadata");
		String elementId = m_binder.getLocal("id");
		if(metadata == null  )
			throw new DataException("Mandatory field 'metadata' is missing.");
		
		if( !OntologyFacade.isMetadataOntologyBased(metadata))
			throw new DataException("Passed metadata is not ontology based.");
		
		// if current root element is not passed as a parameter use the default one
		if( elementId == null ) {
			ResultSet ontConfRS = OntologyCacheMinder.getOntMapping();
			if( ontConfRS != null && ontConfRS.first()){
				do {
					if( ontConfRS.getStringValue(0).equals(metadata)) { // metadata column
						elementId = ontConfRS.getStringValue(2); // root column
						break;
					}
				} while( ontConfRS.next());
			}
		}
		
	}
	
	

}
