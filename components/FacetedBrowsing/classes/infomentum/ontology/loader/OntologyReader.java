package infomentum.ontology.loader;



import java.io.InputStream;

import org.mindswap.pellet.jena.PelletReasonerFactory;

import oracle.spatial.rdf.client.jena.ModelOracleSem;
import oracle.spatial.rdf.client.jena.Oracle;


import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.FileManager;



import infomentum.ontology.utils.OntologyUtils;
import intradoc.common.ServiceException;
import intradoc.shared.SharedObjects;

public class OntologyReader {
	
	private static OntModel readOntology( String metadata, Oracle oracle  ) throws ServiceException {
		
		try {
			String tableName = metadata.substring(1, 6) + OntologyUtils.ONT_TBL_EXT;
			tableName = tableName.toUpperCase();
		
			if(  oracle == null)
				oracle = OntologyUtils.getOraclePool().getOracle();
					
	        ModelOracleSem modeloraclesem = ModelOracleSem.createOracleSemModel(oracle, tableName);
	        OntModel ontModel = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM_TRANS_INF );
	        ontModel.add(modeloraclesem);
	        ontModel.setNsPrefixes(modeloraclesem.getNsPrefixMap());
	        modeloraclesem.commit();
	        OntologyUtils.debug("\n\tReading metadata model size: " + ontModel.size());
			return ontModel;
			
		} catch(Exception ex) {
			ex.printStackTrace();
			throw new ServiceException(ex.toString());
		} finally {
			if( oracle != null) {
				try {
					oracle.dispose();
				}catch (Exception ex){
					OntologyUtils.debug(ex);
				};
			}
		}		
	}
	
	public static OntModel readOntology( String metadata) throws ServiceException {
		Oracle oracle = null;
		return readOntology(metadata, oracle);
	}
	
	public static OntModel readOntology( String metadata, String ontFilePath) throws ServiceException {
		OntModel model = ModelFactory.createOntologyModel( PelletReasonerFactory.THE_SPEC );
		
		OntologyUtils.debug("Reading ontology from file: " + ontFilePath);
		try {			
			InputStream is = FileManager.get().open(ontFilePath);
			model.read(is, "", "RDF/XML");
			is.close();
		} catch (Exception ex) {
			model.close();
			throw new ServiceException("Exception while reading ontology file: " + ex);
		}
		
		return model;
	}
	
	public void getOntChildren(String metadata, String elmRoot) throws ServiceException {
		try {
			String tableName = metadata + infomentum.ontology.utils.OntologyUtils.ONT_TBL_EXT;
			tableName = tableName.toUpperCase();
			ModelOracleSem model = ModelOracleSem.createOracleSemModel(infomentum.ontology.utils.OntologyUtils.getOraclePool().getOracle(), tableName);
			
		} catch(Exception ex) {
			ex.printStackTrace();
			throw new ServiceException(ex.toString());
		}
	}

}
