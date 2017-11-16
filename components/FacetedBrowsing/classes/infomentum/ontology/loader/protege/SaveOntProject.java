// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(5) braces fieldsfirst noctor nonlb space lnc 
// Source File Name:   SaveOntProject.java

package infomentum.ontology.loader.protege;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.GraphUtil;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFWriter;

import edu.stanford.smi.protege.action.LocalProjectAction;
import edu.stanford.smi.protege.action.ProjectAction;
import edu.stanford.smi.protege.action.SaveProject;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.resource.ResourceKey;
import edu.stanford.smi.protege.ui.ProjectManager;
import edu.stanford.smi.protege.util.MessageError;
import edu.stanford.smi.protegex.owl.jena.JenaOWLModel;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import intradoc.apps.shared.AppLauncher;
import intradoc.common.FileUtils;
import intradoc.common.ServiceException;
import intradoc.data.DataBinder;

import java.applet.AppletContext;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import oracle.spatial.rdf.client.jena.*;
import sun.misc.Launcher;

public class SaveOntProject extends SaveProject  {

            public static final String ONT_TBL_EXT = "_ONT";
            private static String TBL_SPACE = "RDF_USERS";
            private String metadata;
            private String ontFilePath;
            private long startUpdateTime = 0;
            AppletContext appletContext = null;
            String m_cgiURL = null;

            public SaveOntProject(boolean large, String metadata, String filePath, AppletContext appletContext, String cgiURL) {
		        super( large);
		        this.metadata = metadata;
		        this.ontFilePath = filePath;
		        this.setName("Save Project and Close");
		        startUpdateTime = new File(ontFilePath).lastModified();
		        this.appletContext = appletContext;
		        this.m_cgiURL = cgiURL;
            }
            
            

            public void actionPerformed(ActionEvent event) {
            	File f = null;
				try {
					 ArrayList errors = new ArrayList();
					 f = new File(ontFilePath);
					 if( f.lastModified() != startUpdateTime) {
						 String errorMsg = "File cannot be saved because it was updated since you opened it";
						 errors.add(new MessageError(null, errorMsg));
						 ProjectAction.getProjectManager().displayErrors("Update Project Errors: ", errors);
					 }
					 else {
						 // update the ontology file
						 super.actionPerformed(event);
						 System.out.println("submit file: " + ontFilePath);
						 ClientHttpRequest client = new ClientHttpRequest(m_cgiURL);
						 client.setParameter("ontMetadata", metadata);
						 client.setParameter("IdcService", "ONT_UPDATE_ONTOLOGY");
						 client.setParameter("ontFilePath", new File(this.ontFilePath));
						 InputStream input = client.post();
						 String response = convertStreamToString(input);
						System.out.println("response: " + response);
						if( response.indexOf("success") > 0 ) {
							 appletContext.showDocument(new URL(m_cgiURL + "?IdcService=ONT_EDIT_CONFIRM&ontMetadata=" + metadata));
						 }

						 
						 
					/*	 DataBinder databinder = new DataBinder();
						 databinder.putLocal("ontMetadata", metadata);
						 databinder.putLocal("ontFilePath:path", this.ontFilePath);
						 AppLauncher.executeService("ONT_EDIT_CONFIRM", databinder);						 
						
						 if( "success".equals( databinder.getLocal("status")) ) {
							 appletContext.showDocument(new URL(m_cgiURL + "?IdcService=ONT_EDIT_CONFIRM&ontMetadata=" + metadata));
						 }*/
				
					 }
                }
		        catch (Exception ex) {
		            ex.printStackTrace();
                } finally{
                	if( f != null && f.exists())
                		f.delete();
                }
                
            }
            
            public String convertStreamToString(InputStream is) throws IOException {
            
            	        if (is != null) {
            	            StringBuilder sb = new StringBuilder();
            	            String line;
            	 
            	           try {
            	                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            	                while ((line = reader.readLine()) != null) {
            	                    sb.append(line).append("\n");
            	                }
            	           } finally {
            	                is.close();
                        }
            	           return sb.toString();
            	        } else {       
            	            return "";
            	        }
            }
            
        
            private void writeOnt1(Model model) throws ServiceException {
		        Oracle oracle = null;
		        try {
		            oracle = new Oracle("jdbc:oracle:thin:@localhost:1522:orclont", "rdfusr", "rdfusr");
		            String tableName = (new StringBuilder(String.valueOf(metadata.substring(1, 6)))).append("_ONT").toString();
		            tableName = tableName.toUpperCase();
		            try {
		                OracleUtils.dropSemanticModel(oracle, tableName);
                    }
		            catch (Exception ex) {
		                ex.printStackTrace();
                    }
		            ModelOracleSem modelDest = ModelOracleSem.createOracleSemModel(oracle, tableName);
		            GraphOracleSem g = modelDest.getGraph();
		            g.dropApplicationTableIndex();
		            g.getBulkUpdateHandler().addInBulk(GraphUtil.findAll(model.getGraph()), TBL_SPACE);
		            g.rebuildApplicationTableIndex();
		            System.out.println((new StringBuilder("Asserted triples count: ")).append(model.size()).toString());
		            Map namespaces = model.getNsPrefixMap();
		            String namespace = null;
		            StringBuffer query = null;
		            StringBuffer queryBase = (new StringBuffer("INSERT INTO ")).append(tableName).append("_NS (PREFIX, NAMESPACE) VALUES ('");
		            if (!namespaces.isEmpty()) {
		                for (Iterator iter = namespaces.keySet().iterator(); iter.hasNext(); oracle.executeQuery(query.toString())) {
		                    String prefix = (String)iter.next();
		                    namespace = (String)namespaces.get(prefix);
		                    query = new StringBuffer(queryBase);
		                    query.append(prefix).append("', '").append(namespace).append("')");
                        }

		                oracle.commitTransaction();
                    }
                }
		        catch (Exception ex) {
		            ex.printStackTrace();
		            throw new ServiceException((new StringBuilder("Exception while saving ontology file into DB: ")).append(ex).toString());
                }
		        finally{
			        if (oracle != null) {
			            try {
			                oracle.dispose();
	                    }
			            catch (Exception ex) {
			                ex.printStackTrace();
	                    }
	                }
                }
		        return;
            }

}
