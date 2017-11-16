package infomentum.ontology;

import java.util.Date;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import infomentum.ontology.loader.OntologyReader;
import infomentum.ontology.utils.OntologyUtils;
import intradoc.common.AppObjectRepository;
import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.data.DataException;
import intradoc.data.DataResultSet;
import intradoc.data.ResultSet;
import intradoc.data.ResultSetUtils;
import intradoc.shared.SharedObjects;



public class OntologyCacheMinder {
	
	private static class InnerOntModel{
		OntModel m_model = null;
		long lastUpdate;
		
		InnerOntModel(OntModel model, long time){
			this.m_model = model;
			this.lastUpdate = time;
		}
		
	}

	
	public static void storeOntMapping(ResultSet mapping) {
		SharedObjects.putObject(OntologyUtils.ONT_CACHE, OntologyUtils.ONT_CONF, mapping);		
		OntologyUtils.debug("cached ontology conf: " + mapping);
	}


	public static void storeOntSecurity(ResultSet securityRS, String metadataName) {
		SharedObjects.putObject(OntologyUtils.ONT_CACHE, OntologyUtils.META_SECURITY_RS_NAME + metadataName, securityRS);		
		OntologyUtils.debug("cached ontology security: " + securityRS);
	}
	
	public static void deleteOntSecurity(String metadataName) {
		if( getOntSecurity( metadataName) != null) {
			Hashtable obj = (Hashtable)AppObjectRepository.getObject(OntologyUtils.ONT_CACHE);
			obj.remove( OntologyUtils.META_SECURITY_RS_NAME + metadataName );
		}
	}
	
	public static ResultSet getOntMapping() {
		Object obj = SharedObjects.getObject(OntologyUtils.ONT_CACHE, OntologyUtils.ONT_CONF);
		OntologyUtils.debug("Cached Ontology Mapping: " + obj);
		if( obj == null)
			return null;
		else return ((ResultSet)obj);
	}
	
	public static ResultSet getOntSecurity(String metadataName) {
		Object obj = SharedObjects.getObject(OntologyUtils.ONT_CACHE, OntologyUtils.META_SECURITY_RS_NAME + metadataName);
		if( obj == null)
			return null;
		else return ((ResultSet)obj);
	}


	public static void cacheOntModel(String metadata, OntModel model, long lastUpdateTime) {
		Hashtable<String, InnerOntModel> map = getAllCachedOntModels();
		if( map == null ) {
			map = new Hashtable<String, InnerOntModel>();
		}
		
		InnerOntModel inerModel = new InnerOntModel(model, lastUpdateTime);
		map.put(metadata, inerModel);
		SystemUtils.trace("OntologyFileOperation", "lastUpdatedTime : " + map.toString());
		SharedObjects.putObject(OntologyUtils.ONT_CACHE, OntologyUtils.ONT_CACHED_MODELS, map);	
	}
	
	
	
	public static void clearOntModel(String metadata) {
		Hashtable<String, InnerOntModel> map = getAllCachedOntModels();
		if( map == null ) {
			return;
		}
		map.remove(metadata);
		
		SharedObjects.putObject(OntologyUtils.ONT_CACHE, OntologyUtils.ONT_CACHED_MODELS, map);	
	}
	
	public static void deleteOntModel(String metadata) {
		clearOntModel(metadata);
	
		// delete security settings 
		deleteOntSecurity(metadata);		
	}
	
	@SuppressWarnings("unchecked")
	private static Hashtable<String, InnerOntModel> getAllCachedOntModels() {
		Object obj = SharedObjects.getObject(OntologyUtils.ONT_CACHE, OntologyUtils.ONT_CACHED_MODELS);
		
		if( obj != null) {
			Hashtable<String, InnerOntModel> ontModels = (Hashtable<String, InnerOntModel>)obj;
			OntologyUtils.debug("getAllCachedOntModels(): " + ontModels.keySet());
			return ontModels;
		} else {
			OntologyUtils.debug("getAllCachedOntModels(): empty" );
		}
		return null;
	}
	

	public static OntModel getCachedOntModel(String metadataName) {
		Hashtable<String, InnerOntModel> map = getAllCachedOntModels();
		if( map != null) {
			InnerOntModel inerModel =  map.get(metadataName);
			return inerModel.m_model;
		}
		return null;
	}
	

	public static long getLastUpdate(String metadataName) {
		Hashtable<String, InnerOntModel> map = getAllCachedOntModels();
		if( map != null) {
			InnerOntModel inerModel =  map.get(metadataName);
			if( inerModel != null)
				return inerModel.lastUpdate;
		}
		return 0;
	}
	
	
}
