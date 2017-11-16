package infomentum.ontology.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hp.hpl.jena.ontology.OntModel;

import oracle.jdbc.pool.*;
import oracle.spatial.rdf.client.jena.ConnectionSetupException;
import oracle.spatial.rdf.client.jena.Oracle;
import oracle.spatial.rdf.client.jena.OraclePool;
import infomentum.ontology.loader.OntologyFacade;
import intradoc.common.FileUtils;
import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.data.DataBinder;
import intradoc.data.DataException;
import intradoc.data.Workspace;
import intradoc.server.DirectoryLocator;
import intradoc.server.ServiceManager;
import intradoc.shared.SharedObjects;

public class OntologyUtils {
	
	public static String db = SharedObjects.getEnvironmentValue("ontDB");
	public static String dbUser = SharedObjects.getEnvironmentValue("ontDBUser");
	public static String dbPsw = SharedObjects.getEnvironmentValue("ontDBPsw");
	public static String dbTableSpace = SharedObjects.getEnvironmentValue("ontDBTableSpace");
/*
	public static String db = null;
	public static String dbUser = null;
	public static String dbPsw = null;
	public static String dbTableSpace = null;
		*/
	
	public static final String ONT_CONF_FILE = "ontology_configuration.hda";
	public static final String ONT_CONF_SECURITY_FILE = "ontology_security_configuration.hda";
	public static final String ONT_MODEL_FILE = "model.owl";
	public static final String ONT_TBL_EXT = "_ONT";
	
	// caching
	public static final String ONT_CACHE = "OntCache";
	public static final String ONT_CONF = "OntConf";
	public static final String ONT_POOL = "OntConn";
	public static final String ONT_CACHED_MODELS = "OntCachedModels";
	
	public static final String[] ONT_CONF_FIELDS = {"metadata", "relationship", "class_root"};
	public static final String[] ONT_SECURITY_FIELDS = {"securityGroup", "root"};
	public static final String[] ONT_NAMESPACE_FIELDS = {"prefix", "namespace"};
	public static final String[] ONT_GENERAL_FIELDS = {"uri", "label"};
	
	public static final String META_CONF_RS_NAME = "OntologyMetadataRS";
	public static final String META_NAMESPACE_RS_NAME = "OntologyNamespaceRS";
	public static final String META_SECURITY_RS_NAME = "OntologySecurityRS";
	
	public static final String RS$ID = "id";
	public static final String RS$NODE_ID = "nodeId";
	public static final String RS$PARENT_ID = "parentNodeId";
	public static final String RS$LABEL = "label";
	public static final String RS$LEVEL = "level";
	public static final String RS$HREF = "href";
	public static final String RS$CLASS = "class";
	public static final String[]NAV_RS_FIELDS = {RS$NODE_ID,RS$PARENT_ID,RS$LABEL,RS$LEVEL,RS$HREF,RS$CLASS};
	

	public static Pattern REGEXP_PREFIX = Pattern.compile("([^:]+):?(.*)");
	
	
	public synchronized static String[] extractPrefixUri(String name) {
		String[] pair = null;
		Matcher matcher = REGEXP_PREFIX.matcher(name);
		if( matcher.find() && matcher.groupCount() > 1) {
			pair = new String[2];
			pair[0]  = matcher.group(1);
			pair[1] = matcher.group(2);
		}
		return pair;
	}
	
	public static String getOntConfDir() throws ServiceException, DataException {
		String base = DirectoryLocator.getAppDataDirectory();
	    FileUtils.checkOrCreateDirectoryPrepareForLocks(base+ "ontologyconfig/", 1, true);
	    return (base + "ontologyconfig/");
	}
	
	public static String getOntConfDir(String name) throws ServiceException, DataException {
		String base = DirectoryLocator.getAppDataDirectory();
		String extra = "ontologyconfig/" + name + "/";
		SystemUtils.trace("OntologyFileOperation", "base+ extra : " + base+ " : " + extra);
	    FileUtils.checkOrCreateDirectoryPrepareForLocks(base+ extra, 1, true);
	    return (base + extra);
	}
	
	public static String getOntWeblayoutDir(String name) throws ServiceException, DataException {
		String base = SharedObjects.getEnvironmentValue("WeblayoutDir") + "FacetedBrowsing";
		String extra = "/ontologyconfig/" + name + "/";
	    FileUtils.checkOrCreateDirectoryPrepareForLocks(base+ extra, 1, true);
	    return (base + extra);
	}
	
	
	 public static void executeService(String serviceName, DataBinder databinder, Workspace ws, String user) throws ServiceException {
		 debug("... Start executing service " + serviceName);
			try {
		        databinder.putLocal("IdcService", serviceName);
		        databinder.setEnvironmentValue("REMOTE_USER", user);
		        ServiceManager servicemanager = new ServiceManager();
		        servicemanager.init(databinder, ws);
		        servicemanager.processCommand();
		        debug("\nDataBinder after service is executed: " + databinder.getLocalData());
			} catch(Exception ex) {
				debug("EXCEPTION while executing service: " + ex);
				throw new ServiceException(ex);
			}
		}

	

	public static OraclePool getOraclePool() throws ConnectionSetupException {
		Object orlOb = SharedObjects.getObject(ONT_CACHE, ONT_POOL);
		OraclePool orcPool = null;
		if( orlOb != null) {
			orcPool =  ((OraclePool) orlOb);
		} else {
			orcPool = createOraclePool();
			SharedObjects.putObject(ONT_CACHE, ONT_POOL, orcPool);
		}
		return orcPool;
	}
	
	private static OraclePool createOraclePool() throws ConnectionSetupException {
		// test with connection properties (taken from some example)
		Properties prop = new Properties();
		prop.setProperty("MinLimit", "2"); // the cache size is 2 at least
		prop.setProperty("MaxLimit", "10");
		prop.setProperty("InitialLimit", "5"); // create 2 connections at startup
		prop.setProperty("InactivityTimeout", "1800"); // seconds
		prop.setProperty("AbandonedConnectionTimeout", "900"); // seconds
		prop.setProperty("MaxStatementsLimit", "10");
		prop.setProperty("PropertyCheckInterval", "60"); // seconds
	//	OntologyUtils.debug("connection details: " + db + ", " + dbUser + ", " + dbPsw);
	//return new OraclePool("jdbc:oracle:thin:@localhost:1522:orclOnto", "rdfusr", "rdfusr", prop, "OracleSemConnPool");
		return (new OraclePool(db, dbUser, dbPsw, prop, "OracleSemConnPool"));
	}
	

	public static void debug(String message) {
		SystemUtils.trace("ontology",  message);
	//	System.out.println(message);
	}

	public static void debug(Exception ex) {
		SystemUtils.trace("ontology", "\nException :" + ex);
		ex.printStackTrace();
	}

}
