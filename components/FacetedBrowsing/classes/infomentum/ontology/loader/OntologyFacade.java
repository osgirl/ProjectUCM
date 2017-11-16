package infomentum.ontology.loader;

import java.io.File;
import java.util.Date;
import java.util.Map;
import java.util.Vector;

import infomentum.ontology.OntologyCacheMinder;
import infomentum.ontology.utils.OntologyUtils;
import intradoc.common.FileUtils;
import intradoc.common.ServiceException;
import intradoc.data.DataBinder;
import intradoc.data.DataException;
import intradoc.data.DataResultSet;
import intradoc.data.ResultSet;
import intradoc.data.ResultSetUtils;
import intradoc.resource.ResourceUtils;
import intradoc.shared.SharedObjects;
import intradoc.common.SystemUtils;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

public class OntologyFacade {
	
	static boolean TO_CACHE = SharedObjects.getEnvValueAsBoolean("isOntologyCached", true);
	static final boolean IS_TO_FILE = SharedObjects.getEnvValueAsBoolean("isSavedToFileSystem", false);

	
	public static File getOntologyFile( String metadataName) throws DataException, ServiceException {
	//	if( IS_TO_FILE ) {
			String path = OntologyUtils.getOntWeblayoutDir(metadataName) + OntologyUtils.ONT_MODEL_FILE;
			File file = new File(path);
			if( file.exists())
				return file;
	//	}
		return null;
	}
	
	
	public static OntModel getOntology( String metadataName) throws ServiceException, DataException {
		SystemUtils.trace("OntologyFileOperation", "In GetOntology");
		OntModel model = null;
		boolean isChanged = false;
		Date date = new Date();
		long lastUpdateTime = date.getTime();
		if( TO_CACHE ) {
			SystemUtils.trace("OntologyFileOperation", "In TO_CACHE");
			String pathBase = OntologyUtils.getOntConfDir(metadataName) ;
			FileUtils.createLockIfNeeded(pathBase );
			File file = new File(pathBase, "lockwait.dat");
			lastUpdateTime = file.lastModified();
			SystemUtils.trace("OntologyFileOperation", "lastUpdatedTime : " + lastUpdateTime);
			long modelLastUpdateTime = OntologyCacheMinder.getLastUpdate(metadataName);
			if( modelLastUpdateTime >= lastUpdateTime)
				SystemUtils.trace("OntologyFileOperation", "Time Difference");
				model = OntologyCacheMinder.getCachedOntModel(metadataName);
		}
		if( model == null) {
			if( IS_TO_FILE ) {
				SystemUtils.trace("OntologyFileOperation", "In IS_TO_FILE");
				String ontFileTo = OntologyUtils.getOntWeblayoutDir(metadataName) + OntologyUtils.ONT_MODEL_FILE;
				model = OntologyReader.readOntology(metadataName, ontFileTo);
				
			} else {
				model = OntologyReader.readOntology(metadataName);
				SystemUtils.trace("OntologyFileOperation", "In Else");
			}
			isChanged = true;
		}
		
		if( TO_CACHE && isChanged ) {
			SystemUtils.trace("OntologyFileOperation", "TO_CACHE & changed");
			OntologyCacheMinder.cacheOntModel(metadataName, model, lastUpdateTime);			
		}
		return model;
	}
	
	
	public static void writeOntology(String metadata, OntModel model, String filePath) throws DataException, ServiceException {
		
		String ontFileTo = OntologyUtils.getOntWeblayoutDir(metadata) + OntologyUtils.ONT_MODEL_FILE;
		OntologyWriter.writeOnt(filePath, ontFileTo, metadata);
		if( !IS_TO_FILE ) {
			OntologyWriter.writeOnt(model, metadata);
		}
		if( TO_CACHE ) {
			OntologyCacheMinder.clearOntModel(metadata);
			String pathBase = OntologyUtils.getOntConfDir(metadata) ;
			FileUtils.createLockIfNeeded(pathBase );
			File file = new File(pathBase, "lockwait.dat");
			FileUtils.touchFile(file.getPath());
			OntologyCacheMinder.cacheOntModel(metadata, model, file.lastModified());			
		}
	}
	
	public static void updateOntology(String metadata, OntModel model, String filePath) throws DataException, ServiceException {
		
		String ontFileTo = OntologyUtils.getOntWeblayoutDir(metadata) + OntologyUtils.ONT_MODEL_FILE;
		OntologyWriter.writeOnt(filePath, ontFileTo, metadata);
		if( !IS_TO_FILE ) {
			OntologyWriter.updateOnt(model, metadata);
		}
		if( TO_CACHE ) {
			OntologyCacheMinder.clearOntModel(metadata);
			String pathBase = OntologyUtils.getOntWeblayoutDir(metadata) ;
			FileUtils.createLockIfNeeded(pathBase );
			File file = new File(pathBase, "lockwait.dat");
			FileUtils.touchFile(file.getPath());
			OntologyCacheMinder.cacheOntModel(metadata, model, file.lastModified());			
		}
	}
	
	public static void clearOntologyCache(String metadata) {
		if( TO_CACHE ) {
			OntologyCacheMinder.clearOntModel(metadata);
		}
	}
	
	
	public static void deleteOntology(String metadata) throws ServiceException, DataException {
		if( !IS_TO_FILE ) {			
			OntologyWriter.deleteOnt(metadata);	
		}
		// update ont mapping file
		deleteOntMapping(metadata);
		// delete folder for this ontology
		String basedir = OntologyUtils.getOntConfDir( metadata );
		File dir = new File(basedir);
		deleteDir(dir);
		OntologyUtils.debug("Delete ont file: " + dir.getAbsolutePath());
		// remove this ontology from cache
		OntologyCacheMinder.deleteOntModel(metadata);		
	}
	
	  public static boolean deleteDir(File dir) {
	        if (dir.isDirectory()) {
	            String[] children = dir.list();
	            for (int i=0; i<children.length; i++) {
	                boolean success = deleteDir(new File(dir, children[i]));
	                if (!success) {
	                    return false;
	                }
	            }
	        }
	    
	        // The directory is now empty so delete it
	        return dir.delete();
	    } 
	
	public static ResultSet getOntUserClasses(String metadataName) throws ServiceException, DataException{
		OntModel model = getOntology(metadataName);	
		ExtendedIterator<OntClass> it = model.listNamedClasses();	
		DataResultSet classesDRS = new DataResultSet(OntologyUtils.ONT_GENERAL_FIELDS);
		Vector<String> row = null;
		String label = null;
		while(it.hasNext()){
			OntClass cl = it.next();
			if( !cl.getNameSpace().equals("http://www.w3.org/2002/07/owl#")) {
				row = new Vector<String>();
				label = cl.getLabel(null);
				if( label == null)
					label = cl.getLocalName();
				row.add( cl.getURI()); row.add( label );
				classesDRS.addRow(row);
			}
		}
		return classesDRS;
	}
	
	public static ResultSet getOntPropertiesRangedClass(String metadataName, String classURI)throws DataException, ServiceException {
		OntModel model = getOntology(metadataName);	
		OntClass ontClass = model.getOntClass(classURI);
		ExtendedIterator<ObjectProperty> it = model.listObjectProperties();	
		DataResultSet classesDRS = new DataResultSet(OntologyUtils.ONT_GENERAL_FIELDS);
		Vector<String> row = null;
		OntProperty prop = null;
		String label = null;
		while(it.hasNext()){
			prop = it.next();
			//if( prop.hasRange(ontClass)) {
				row = new Vector<String>();
				label = prop.getLabel(null);
				if( label == null)
					label = prop.getLocalName();
				row.add( prop.getURI()); row.add( label);
				classesDRS.addRow(row);
			//}
		}
		return classesDRS;
	}
	
	public static void updateOntMapping(String metadata, String clas, String relationship)  throws ServiceException, DataException {
		ResultSet ontMetaRS = OntologyCacheMinder.getOntMapping();
		DataResultSet ontMetaDRS = new DataResultSet(OntologyUtils.ONT_CONF_FIELDS);

		Vector<String> row  = new Vector<String>();
		row.add(metadata); row.add(relationship); row.add(clas); 
		OntologyUtils.debug("update mapping new row: " + row);
		ontMetaDRS.addRow(row);
		
		if( ontMetaRS != null && ontMetaRS.first()){
			((DataResultSet)ontMetaRS).merge("metadata", ontMetaDRS, false);
			ontMetaDRS = (DataResultSet)ontMetaRS;
			OntologyUtils.debug("updateOntMapping(): Result RS after merging : " + ontMetaDRS);
		}
		// update cache
		OntologyCacheMinder.storeOntMapping(ontMetaDRS);
		// update file
		DataBinder binder = new DataBinder();
		binder.addResultSet(OntologyUtils.META_CONF_RS_NAME, ontMetaDRS);
		serialize(binder, OntologyUtils.ONT_CONF_FILE, null);
	}
	
	
	public static ResultSet getOntMapping()  throws ServiceException, DataException {
		ResultSet mappingRS = null;
		// try cache first
		mappingRS = OntologyCacheMinder.getOntMapping();
		if( mappingRS == null) {
			// read from file
			mappingRS = readSerializedData(OntologyUtils.META_CONF_RS_NAME, OntologyUtils.ONT_CONF_FILE, null );
			// update cache
			if( mappingRS != null) {
				OntologyCacheMinder.storeOntMapping(mappingRS);
			}
		}
		OntologyUtils.debug("OntFacade.getOntMapping() " +mappingRS);
		return mappingRS;
	}
	
	
	
	
	
	public static void deleteOntMapping(String metadata)  throws ServiceException, DataException {
		DataResultSet ontMetaRS = (DataResultSet)OntologyCacheMinder.getOntMapping();//readOntMetadataMapping(OntologyUtils.META_CONF_RS_NAME, OntologyUtils.ONT_CONF_FILE);
		OntologyUtils.debug("Before deleting the row in mapping RS:" + ontMetaRS);
		if(ontMetaRS != null && ontMetaRS.first() ) {
			do {
				if( metadata.equals(ontMetaRS.getStringValue(0))) {
					ontMetaRS.deleteCurrentRow();
					break;
				}
			} while( ontMetaRS.next());
			OntologyUtils.debug("After deleting the row in mapping RS:" + ontMetaRS);
			
			// update cache
			OntologyCacheMinder.storeOntMapping(ontMetaRS);
		} else {
			ontMetaRS = new DataResultSet(OntologyUtils.ONT_CONF_FIELDS);
		}
		// update file
		DataBinder binder = new DataBinder();
		binder.addResultSet(OntologyUtils.META_CONF_RS_NAME, ontMetaRS);
		serialize(binder, OntologyUtils.ONT_CONF_FILE, null);
	}
	
	public static void updateOntSecurity(ResultSet securityRS, String metadataName) throws ServiceException, DataException {
		if( securityRS == null || metadataName == null )
			return;
		
		// write to file
		DataBinder binder = new DataBinder();
		binder.addResultSet(OntologyUtils.META_SECURITY_RS_NAME, securityRS);
		serialize(binder, OntologyUtils.ONT_CONF_SECURITY_FILE, metadataName);
		
		// update cache 
		OntologyCacheMinder.storeOntSecurity(securityRS, metadataName);
	}
	
	
	public static ResultSet getOntSecurity(String metadataName) throws ServiceException, DataException {
		ResultSet securityRS = null;
		// try cache first
		securityRS = OntologyCacheMinder.getOntSecurity(metadataName);
		if( securityRS == null ) {
			// read from file
			securityRS = readSerializedData(OntologyUtils.META_SECURITY_RS_NAME, OntologyUtils.ONT_CONF_SECURITY_FILE, metadataName);
			// update cache
			if(securityRS != null ){
				OntologyCacheMinder.storeOntSecurity(securityRS, metadataName);
			}
		}
		
		return securityRS;
	}
	/*
	public static void updateOntPrefixes(Map<String,String> prefixMap, String metadataName) throws ServiceException, DataException {
		if( prefixMap == null || prefixMap.isEmpty() || metadataName == null )
			return;
		
		// convert to Result Set
		DataResultSet ontNamespaceDRS = new DataResultSet(OntologyUtils.ONT_NAMESPACE_FIELDS);
		Vector<String> row = null;
		String prefix = null;
		for( Iterator<String> iter = prefixMap.keySet().iterator(); iter.hasNext();) {
			prefix = iter.next();
			row = new Vector<String>();
			row.add(prefix); row.add( prefixMap.get(prefix) );
			ontNamespaceDRS.addRow(row);
		}
		// write to file
		DataBinder binder = new DataBinder();
		binder.addResultSet(OntologyUtils.META_NAMESPACE_RS_NAME, ontNamespaceDRS);
		serialize(binder, OntologyUtils.ONT_CONF_FILE, metadataName);
	}
	*/
	public static Map<String,String> getOntPrefixes( String metadataName) throws ServiceException, DataException {
		Map<String,String> prefixesMap = null;
		// try first to get from cache
		OntModel model = OntologyFacade.getOntology( metadataName);
		if( model != null )
			return model.getNsPrefixMap();
		return null;
	}
	
	private static void serialize(DataBinder binder, String fileName, String metadataName) throws ServiceException, DataException {
		if(binder.getResultSetList().hasMoreElements() ) {
			//synchronized (m_adminSync) {
			String basedir = null;
			if( metadataName == null)
				basedir = OntologyUtils.getOntConfDir();
			else
				basedir = OntologyUtils.getOntConfDir( metadataName );
			ResourceUtils.serializeDataBinder(basedir, fileName, binder, true, false);
			//}
		}
	}
	
	private static ResultSet readSerializedData(String resultSetName, String fileName, String metadataName) throws ServiceException, DataException {
		String basedir = null;
		if( metadataName == null)
			basedir = OntologyUtils.getOntConfDir();
		else
			basedir = OntologyUtils.getOntConfDir( metadataName );
		DataBinder binder = new DataBinder();
		ResourceUtils.serializeDataBinder(basedir, fileName , binder, false, false);
		return (binder.getResultSet(resultSetName));
	}
	
	public static boolean isMetadataOntologyBased(String metadata) throws ServiceException, DataException {
		
		ResultSet ontBasedMetadataRS = getOntMapping();
		String value = ResultSetUtils.findValue(ontBasedMetadataRS, "metadata", metadata, "metadata");
		if( value != null ) {
			return true;
		}
		return false;
	}

}
