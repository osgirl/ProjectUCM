// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(5) braces fieldsfirst noctor nonlb space lnc 
// Source File Name:   ProtegeStandaloneTab.java

package infomentum.ontology.loader.protege;

import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import edu.stanford.smi.protege.model.KnowledgeBaseFactory;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.storage.clips.ClipsKnowledgeBaseFactory;
import edu.stanford.smi.protege.ui.ProjectManager;
import edu.stanford.smi.protege.ui.ProjectMenuBar;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.URIUtilities;
import edu.stanford.smi.protegex.owl.ProtegeOWL;
import edu.stanford.smi.protegex.owl.jena.JenaKnowledgeBaseFactory;
import edu.stanford.smi.protegex.owl.jena.JenaOWLModel;
import intradoc.apps.shared.AppLauncher;
import intradoc.apps.shared.StandAloneApp;
import intradoc.common.FileUtils;
import intradoc.common.ServiceException;
import intradoc.data.DataBinder;
import intradoc.shared.SharedObjects;

import java.applet.AppletContext;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import oracle.spatial.rdf.client.jena.ModelOracleSem;
import oracle.spatial.rdf.client.jena.Oracle;

// Referenced classes of package infomentum.ontology.loader.protege:
//            SaveOntProject

public class ProtegeStandaloneTab  {

	public static final String projURL = "project/empty.pprj";
	
	private String ontFilePathAbs = null;
	private String ontFilePath = null;
	private String ontFilePathURI = null;
	private String ontProjectPathAbs = null;
	private String metadata = null;
	private URL projectURL = null;
	private String cgiURL = null;

	public ProtegeStandaloneTab(String args[], String cgiURL) {
		metadata = args[0];
		projectURL = getProgectURI(args);
		this.cgiURL = cgiURL;
		init();
	}

	private void init( ) {
		OntModel model = null;
		System.out.println("\nCGIurl: " + cgiURL);
		try {
			 DataBinder databinder = new DataBinder();
			 databinder.putLocal("ontMetadata", metadata);
			 AppLauncher.executeService("ONT_GET_ONT_FILEPATH", databinder);
			 String path = databinder.getLocal("ontFilePathAbsolute");
			 ontFilePath = path;
			 
		//	System.out.println("\nEnv: " + SharedObjects.getEnvironment());
			if( path != null) {
				int i = path.indexOf("FacetedBrowsing");
				if( i > 0) {
					ontFilePathURI  = cgiURL.replaceFirst("idcplg", path.substring(i));	
					StringBuffer modelStr = readFileFromURL(new URL(ontFilePathURI));
					ontFilePathURI = writeTempFile(modelStr, "owl");
				} 
				ontFilePathAbs = ontFilePathURI;
				ontFilePathURI = ontFilePathURI.replaceAll("\\\\", "/");
				System.out.println("\nOntology files: " + ontFilePathURI + ", " + ontFilePath);
			} /*else {
				model = readOntology();
				System.out.println((new StringBuilder("Loading model in applet : ")).append(model.size()).toString());
				ontFilePathAbs = copyOntologyToTempFile(model);
			}*/
			if (!ontFilePathURI.startsWith("file:///")) {
				ontFilePathURI = (new StringBuilder("file:///")).append(ontFilePathURI).toString();
			}
			
			path  = databinder.getLocal("ontProjectPathAbsolute");
			int i = path.indexOf("FacetedBrowsing");
			StringBuffer modelStr = readFileFromURL(new URL(cgiURL.replaceFirst("idcplg", path.substring(i))));
			ontProjectPathAbs = writeTempFile(modelStr, "ppr");
			System.out.println("Project file: " + ontProjectPathAbs);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		if (model != null) {
			model.close();
		}
	}
	
	
	

	private URL getProgectURI(String args[]) {
		URL uri = null;
		try {
			if (args.length > 1) {
				uri = new URL(args[1]);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return uri;
	}

	private OntModel readOntology() throws Exception {
		try {
			Oracle oracle = new Oracle(
					"jdbc:oracle:thin:@localhost:1522:orclont", "rdfusr","rdfusr");
			return readOntology(metadata, oracle);
		} catch (Exception ex) {
			throw new Exception("Exception while reading Ont model from DB");
		}
	}

	public void loadTab(AppletContext appletContext) {
		System.out.println("\nLoading project... " );
		ArrayList errors = new ArrayList();
		edu.stanford.smi.protege.util.PropertyList prop = null;
		//java.net.URI uri = URIUtilities.createURI((new StringBuilder()).append(projectURL).append("project/empty.pprj").toString());
		System.out.println((new StringBuilder("\nProject.loadProjectFromFile(): ")).append(ontProjectPathAbs).append(", model from: ").append(ontFilePathURI).toString());
		
		Project prj = Project.loadProjectFromFile(ontProjectPathAbs, errors);//loadProjectFromURI(uri, errors);
		
	/*	KnowledgeBaseFactory jfac =  prj.getKnowledgeBaseFactory();
		System.out.println("Reading ontoly from " + ontFilePathURI);
		JenaKnowledgeBaseFactory.setOWLFileName(prj.getSources(), ontFilePathURI);
		JenaKnowledgeBaseFactory.setOWLFileLanguage(prj.getSources(), "RDF/XML");
	//	System.out.println((new StringBuilder("prj.getActiveRootURI(): ")).append(prj.getActiveRootURI()).toString());
	//	System.out.println((new StringBuilder("Base factory ")).append(prj.getKnowledgeBaseFactory().getDescription()).toString());
		prj.createDomainKnowledgeBase(jfac, errors, true);*/
		if (errors.size() > 0) {
			System.out.println((new StringBuilder("There were erros at loading prj: ")).append(prj).toString());
			return;
		} else {
			JFrame frame = new JFrame("Ontology Frame");
			ProjectManager.getProjectManager().setRootPane(frame.getRootPane());
			ProjectManager.getProjectManager().setCurrentProject(prj, false);
			ProjectMenuBar menubar = (ProjectMenuBar) ProjectManager.getProjectManager().getCurrentProjectMenuBar();
		//	java.awt.Component menuSave = ProjectManager.getProjectManager().getCurrentProjectMenuBar().getMenu(0).getMenuComponent(6);
			
			menubar.getMenu(0).removeAll();
			ComponentFactory.addMenuItem(menubar.getMenu(0),new SaveOntProject(false, metadata, ontFilePathAbs,  appletContext, cgiURL));
		
			frame.pack();
			frame.setVisible(true);
			return;
		}
	}

	public String copyOntologyToTempFile(OntModel model) throws ServiceException, IOException {
		StringBuffer filePath;
		File file;
		FileOutputStream outs;
		String fileName = (new StringBuilder(String.valueOf(metadata))).append(
				".owl").toString();
		filePath = (new StringBuffer()).append("/temp/ontology/").append(
				metadata).append("/");
		file = new File(
				(new StringBuilder(String.valueOf(filePath.toString())))
						.append(fileName).toString());
		FileUtils.checkOrCreateDirectoryPrepareForLocks(filePath.toString(), 1,
				true);
		FileUtils.reserveDirectory(filePath.toString());
		System.out.println((new StringBuilder("Write ontology to temp file: "))
				.append(file.getAbsolutePath()).toString());
		outs = null;
		try {
			outs = new FileOutputStream(file);
			model = prepareModelToWrite(model);
			RDFWriter writer = model.getWriter("RDF/XML");
			writer.setProperty("xmlbase", "http://www.hays.co.uk/ont");
			writer.write(model, outs, "http://www.hays.co.uk/ont");
		} catch (Exception fex) {
			FileUtils.releaseDirectory(filePath.toString());
		} finally {
			if (outs != null) {
				outs.close();
			}
		}
		return file.getAbsolutePath().replace("\\", "/");
	}
	
	private String writeTempFile(StringBuffer content, String extension) {
		FileWriter fos = null; 
	    BufferedWriter dos = null;
	    File file= null;
	    try {
	    	file = new File(System.getProperty("java.io.tmpdir"), "model." + extension);
	    	System.out.println("\nwrite to temp ontology file ontFilePath: " + file.getAbsolutePath());
	    	if( !file.exists())
	    		file.createNewFile();
	      fos = new FileWriter(file);
	      dos=new BufferedWriter(fos);
	      dos.write(content.toString());

	    } catch (IOException e) {
	      e.printStackTrace();
	    }
	    finally {
	    	try {
	    		if( dos!=null)
	    			dos.close();
	    		if( fos != null)
	    			fos.close();
	    	} catch (IOException e) {
	  	      e.printStackTrace();
	  	    }
	    }
	    return file.getAbsolutePath();
	}
	
	private StringBuffer readFileFromURL (URL url) { 
		System.out.println("\nPath to ont File: " + url);
	   BufferedReader in = null;
	    StringBuffer rez = new StringBuffer();
	  try {	    
	    URLConnection      urlConn; 

	    urlConn = url.openConnection(); 
	    urlConn.setDoInput(true); 
	    urlConn.setUseCaches(false);
	  
	    in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
	    String line;
	    while ((line = in.readLine()) != null)
	    {
	        rez.append(line + "\n");
	    }
	  } catch (IOException ioe) {
		  ioe.printStackTrace();
	  } finally {
		  try {
			  if( in != null)
				  in.close(); 
		  } catch(Exception ex) {}
	  }
	  System.out.println("\nRead ontolody from net: " + rez.length());
	  return rez;
	}

	
	

	private OntModel prepareModelToWrite(OntModel model) throws Exception {
		OntModel ontModel = model;
		OntModel ontModel1 = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_TRANS_INF);
		
		OntClass thing = ontModel.getOntClass("http://www.w3.org/2002/07/owl#Thing");
		OntClass nothing = ontModel.getOntClass("http://www.w3.org/2002/07/owl#Nothing");
		
		OntClass cl = null;
		String lbl = null;
		ExtendedIterator it2 = null;
		Vector<OntClass> classes = new Vector<OntClass>();
		
		for ( it2 = ontModel.listNamedClasses(); it2.hasNext();) {
			cl = (OntClass) it2.next();
			if (!cl.isSameAs(thing)) {
				cl.removeSuperClass(cl);
				cl.removeEquivalentClass(cl);
				cl.removeSuperClass(thing);
				cl.removeDisjointWith(nothing);
				System.out.println("class: " + cl.getLabel(null) + ", " + cl.getLocalName());
				System.out.println("ont Class:  " + cl.isClass());
				lbl = cl.getLabel(null);
				cl =  ontModel1.createClass(cl.getURI());
				if( lbl != null)
					cl.addLabel(lbl, null);
				classes.add(cl);
			}
		}

		OntProperty prop;
		for (ExtendedIterator it = ontModel.listAllOntProperties(); it.hasNext(); prop.removeInverseProperty(prop)) {
			prop = (OntProperty) it.next();
			prop.removeEquivalentProperty(prop);
			prop.removeSameAs(prop);
			prop.removeSubProperty(prop);
			System.out.println("prop: " + prop + ", " + prop.isObjectProperty());
			System.out.println("prop: " + prop + ", " + prop.isTransitiveProperty());
			System.out.println("prop: " + prop + ", " + prop.isSymmetricProperty());
			if(prop.isObjectProperty()) {
				lbl = prop.getLabel(null);
				if( prop.isTransitiveProperty())
					prop = ontModel1.createTransitiveProperty(prop.getURI(), prop.isFunctionalProperty());
				else if( prop.isSymmetricProperty())
					prop = ontModel1.createSymmetricProperty(prop.getURI(), prop.isFunctionalProperty());
				else 
					prop = ontModel1.createObjectProperty(prop.getURI());
				if( lbl != null)
					prop.addLabel(lbl, null);
			}
		}

		System.out.println("\n");
		Individual ind;
		for(int i = 0; i < classes.size(); i++){
			for (ExtendedIterator it1 = ontModel.listIndividuals(classes.get(i)); it1.hasNext(); ind.removeSameAs(ind)) {
				ind = (Individual) it1.next();
			//	ontModel1.createIndividual(resource)
				System.out.println("ind: " + ind.getLabel(null) );
				if(!ind.isClass() && !ind.isObjectProperty()){
					cl = ind.getOntClass();
					System.out.println("ind: " + ind.getLocalName() + ", class: " + cl);
					lbl = ind.getLabel(null);
					ind = ontModel1.createIndividual(ind.getURI(), cl);
					if( lbl != null)
						ind.addLabel(lbl, null);
				}
			}
		}
		ontModel1.setNsPrefixes(ontModel.getNsPrefixMap());
		return ontModel1;
	}

	
	
	
	public static OntModel readOntology(String metadata, Oracle oracle) throws ServiceException {
		OntModel ontmodel;
		try {
			String tableName = (new StringBuilder(String.valueOf(metadata.substring(1, 6)))).append("_ONT").toString();
			tableName = tableName.toUpperCase();
			ModelOracleSem modeloraclesem = ModelOracleSem.createOracleSemModel(oracle, tableName);
			System.out.println((new StringBuilder("Prefixes: ")).append(modeloraclesem.getNsPrefixMap()).toString());
			OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
			ontModel.add(modeloraclesem);
			ontModel.setNsPrefixes(modeloraclesem.getNsPrefixMap());
			modeloraclesem.commit();
			ontmodel = ontModel;
			
		//	modeloraclesem.
			
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new ServiceException(ex.toString());
		} finally {

			if (oracle != null) {
				try {
					oracle.dispose();
				} catch (Exception ex) {
					System.out.println(ex);
				}
			}
		}
		return ontmodel;
	}
}
