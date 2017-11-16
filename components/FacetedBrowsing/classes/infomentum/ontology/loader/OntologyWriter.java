package infomentum.ontology.loader;

import infomentum.ontology.utils.OntologyUtils;
import intradoc.common.FileUtils;
import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.data.DataException;
import intradoc.shared.SharedObjects;

import java.util.Iterator;
import java.util.Map;

import oracle.spatial.rdf.client.jena.GraphOracleSem;
import oracle.spatial.rdf.client.jena.ModelOracleSem;
import oracle.spatial.rdf.client.jena.Oracle;
import oracle.spatial.rdf.client.jena.OracleUtils;

import com.hp.hpl.jena.graph.GraphUtil;
import com.hp.hpl.jena.rdf.model.Model;
//import test.Test;

public class OntologyWriter {
	private static final String Ucm_Region = SharedObjects.getEnvironmentValue("Ucm_Region");  //MR7 ontology task
	public static void writeOnt(String ontFileFrom, String ontFileTo, String metadata)throws ServiceException, DataException {
		if( !ontFileFrom.equals(ontFileTo)) 
			FileUtils.copyFile(ontFileFrom, ontFileTo);
	}
	
	public static void writeOnt(Model model, String metadata)throws ServiceException {
		Oracle oracle = null;
		try {
			if(Ucm_Region.equalsIgnoreCase("APAC"))
			{
			SystemUtils.info("Trying to connect to database to update ontology configuration");
			}
			oracle = OntologyUtils.getOraclePool().getOracle();
		//	oracle = new Oracle("jdbc:oracle:thin:@localhost:1522:orclOnto", "rdfusr", "rdfusr");
			String tableName = metadata.substring(1, 6) + OntologyUtils.ONT_TBL_EXT;
			tableName = tableName.toUpperCase();
			try {
				OracleUtils.dropSemanticModel(oracle, tableName);
			} catch(Exception ex) {}
			ModelOracleSem modelDest = ModelOracleSem.createOracleSemModel(oracle, tableName);
			
			GraphOracleSem g = modelDest.getGraph();
			g.dropApplicationTableIndex();
			g.getBulkUpdateHandler().addInBulk(GraphUtil.findAll(model.getGraph()), OntologyUtils.dbTableSpace);
			g.rebuildApplicationTableIndex();

			// save namespace
			Map<String,String> namespaces = model.getNsPrefixMap();
			String prefix, namespace = null;
			StringBuffer query = null;
			StringBuffer queryBase = new StringBuffer("INSERT INTO ").append(tableName).append("_NS (PREFIX, NAMESPACE) VALUES ('");
			if( !namespaces.isEmpty()) {
				for(Iterator<String> iter = namespaces.keySet().iterator(); iter.hasNext();) {
					prefix = iter.next();
					namespace = namespaces.get(prefix);
					query = new StringBuffer(queryBase);
					query.append(prefix).append("', '").append(namespace).append("')");
					oracle.executeSQL(query.toString());
				//	OntologyUtils.debug("Update namespace: " + query);
				}
				oracle.commitTransaction();
				if(Ucm_Region.equalsIgnoreCase("APAC"))
				{
				SystemUtils.info("Database changes completed for ontology configuration");
				}
			}
			
		} catch(Exception ex) {
			ex.printStackTrace();
			throw new ServiceException("Exception while saving ontology file into DB: " + ex);
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
	
	public static void updateOnt(Model model, String metadata)throws ServiceException {
		deleteOnt(metadata);
		writeOnt(model, metadata);
	}
	
	public static void deleteOnt(String metadata) {
		Oracle oracle = null;
		try {
			OntologyUtils.debug("OntologyWriter.deleteOnt() starting..." + metadata);
			oracle = OntologyUtils.getOraclePool().getOracle();
			String tableName = metadata.substring(1, 6) + OntologyUtils.ONT_TBL_EXT;
			tableName = tableName.toUpperCase();
			OracleUtils.dropSemanticModel(oracle, tableName);		
			
			OntologyUtils.debug("Ontology was deleted for " + metadata);
		} catch(Exception ex) {
			//ex.printStackTrace();
			//throw new ServiceException("Exception while saving ontology file into DB: " + ex);
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
	
	public static void main(String[] args) throws Exception {
//		Test test = new Test();
		//OntModel model = test.readModel();
	//	System.out.println("Model: " + model.size());
	//	OntologyWriter.writeOnt(model, "XHaysSpec");
		
	}
	

}
