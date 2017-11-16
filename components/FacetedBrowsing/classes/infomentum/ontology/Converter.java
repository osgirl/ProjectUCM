package infomentum.ontology;





import infomentum.ontology.loader.OntologyFacade;


import infomentum.ontology.loader.OntologyReader;
import infomentum.ontology.utils.OntologyUtils;
import intradoc.data.DataException;
import intradoc.data.DataResultSet;
import intradoc.data.ResultSet;
import intradoc.shared.SharedObjects;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import oracle.spatial.rdf.client.jena.Attachment;
import oracle.spatial.rdf.client.jena.GraphOracleSem;
import oracle.spatial.rdf.client.jena.InferenceMaintenanceMode;
import oracle.spatial.rdf.client.jena.ModelOracleSem;
import oracle.spatial.rdf.client.jena.Oracle;
import oracle.spatial.rdf.client.jena.QueryOptions;

import org.mindswap.pellet.jena.PelletReasonerFactory;

import com.hp.hpl.jena.ontology.FunctionalProperty;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.Filter;


public class Converter {
	
	static Query GET_ROOT_ELEMENTS = createQuery();
	static Query GET_NAV_ELEMENTS = createNavigationQuery();
	static private final String TREE_HEADER = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
	static private final String DELIMETER = ";";
	
	
	// SPARQ query to retrieve all entities of the given class, that are in the given relationship
	// with the given entity and have labels in the given Language-Country
	static public Query QUERY_GET_INDIVIDUALS_FULL = QueryFactory.create(SharedObjects.getEnvironmentValue("GetIndividualsFullQuery"));
	
	
	// SPARQ query to retrieve all entities of the given class and have labels in the given Language-Country
	static public Query QUERY_GET_INDIVIDUALS = QueryFactory.create(SharedObjects.getEnvironmentValue("GetIndividualsQuery"));
	
	
	// SPARQ query to retrieve all top-entities of the given class and have labels in the given Language-Country
	static public Query QUERY_GET_ROOTS = QueryFactory.create(SharedObjects.getEnvironmentValue("GetRootElementsQuery"));
	
	static public final String DEFAULT_LANGUAGE = "en";
	static public final String DEFAULT_COUNTRY = "GB";
	
	static public final String WILDCARD_SEARCH = ".*";
	
	private OntModel m_model = null;
	

	/*
	public Converter(OntModel model, Map<String,String> prefixMap) {
		this.m_model = model;
		//this.prefixMap = prefixMap;
		//this.usePrefix = true;
		populatePrefixes();
	}
	
	public Converter(OntModel model,  String lang, String country) {
		this.m_model = model;
		//this.prefixMap = prefixMap;
		//this.usePrefix = true;
		//populatePrefixes();
		this.m_lang = lang;
		this.m_country = country;
	}
	*/
	public Converter(OntModel model) {
		this.m_model = model;
	}
/*	
	public Converter(OntModel model, Property relationship, String lang, OntClass clas, String country) {
		this.m_model = model;
		this.m_relationship = relationship;
		this.m_rootClass = clas;
		this.m_lang = lang;
		this.m_country = country;
	}
	
	private void populatePrefixes() {
		try {
			if( usePrefix && prefixMap != null) {
				OntologyUtils.debug("Prefixes: " + prefixMap );
				if( prefixMap != null) {
					namespaceMap = new Hashtable<String, String>();		
					String prefix = null;
					for(Iterator<String> iter =  prefixMap.keySet().iterator(); iter.hasNext();) {
						prefix = iter.next();
						namespaceMap.put(prefixMap.get(prefix), prefix);
					}
				}
			}
		} catch(Exception ex) {
			OntologyUtils.debug(ex);
		}
	}
	*/
	private class FilterRoot extends Filter {
		Resource ind = null;
		
		public FilterRoot(Resource ind){
			this.ind = ind;
		}

		public boolean accept(Object arg0) {
			if(ind.equals(arg0))
				return true;
			return false;
		}
		
	}
	/*
	public DataResultSet convertToNavigationRS() {
		OntologyUtils.debug("convertToNavigation() start...");
		if(root != null){
			Individual[] roots = {root};
			return convertToNavigationRS(roots);
		} else if(rootClass != null) {
			List<Resource> list = getRootElements();
			Individual[] roots = new Individual[list.size()];
			int i =0;
			Resource resource = null;
			for(Iterator<Resource> it = list.iterator(); it.hasNext();) {
				resource = it.next();
				roots[i++] = model.getIndividual(resource.getURI());
			}
			OntologyUtils.debug("convertToNavigation() : roots length: " + roots.length);
			return convertToNavigationRS(roots);
		}
		return null;
	}
	
	public DataResultSet secureConvertToNavigationRSSecure(String[] ids){
		OntologyUtils.debug("secureConvertToNavigationSecure() : " + this.relationship + ", " + this.rootClass );
		Individual[] resources = new Individual[ids.length];
		RDFNode[] nodes = new RDFNode[ids.length];
		for(int i = 0; i<ids.length; i++){
			OntologyUtils.debug("roots for this user : ids[] = " + ids[i]);
			resources[i] = model.getIndividual(ids[i]);
			nodes[i] = model.getRDFNode(resources[i].asNode());
		}
		Vector<Individual> vec = new Vector<Individual>();
		Individual r = null;
		for(int i = 0; i<resources.length; i++){
			r = resources[i];
			for(int m = 0; m<nodes.length; m++){
				if( r.hasProperty(relationship, nodes[m]) ){
					vec.remove(r);
					break;
				}else if (!vec.contains(r)){
					vec.add(r);					
				}
			}
		}
		Individual[] roots = new Individual[vec.size()];
		OntologyUtils.debug("Secure roots for this user were processed to: " + vec);
		vec.copyInto(roots);
		return convertToNavigationRS(roots);
	}
	
	private DataResultSet convertToNavigationRS(Individual[] roots) {
		DataResultSet resultDRS = new DataResultSet(OntologyUtils.NAV_RS_FIELDS);
		int level = 1;
		for( int i = 0; i < roots.length; i++){
			Individual ind = roots[i];
			convertToNavigationRS(ind, null, resultDRS, level);
		}
		return resultDRS;
	}
	
	private void convertToNavigationRS(Individual ind, Individual parent, DataResultSet result, int level){
		if( !ind.isIndividual()) 
			return;
		OntologyUtils.debug("addToNavigation(): Individual: " + ind);
		String uri, label = null;
		uri = getURI(ind);
		label = ind.getLabel(lang);
		if( label == null)
			label = ind.getLocalName();
		
		// populate the current record
		Vector<String> row = new Vector<String>();
		
		row.add(uri);//nodeId ????
		if(parent != null) {
			row.add(getURI(parent)); // parentNodeId ???
		} else
			row.add("");
		row.add(label); // label
		row.add(String.valueOf(level)); // level
		row.add(uri); // href
		row.add(ind.getRDFType(true).getURI()); // class
		OntologyUtils.debug("navigation row: " + row);
	//	OntologyUtils.debug("ind.getRDFType().getLocalName(): " + ind.getRDFType().getLocalName() + ", false: " + ind.getRDFType(false) + ", true: " + ind.getRDFType(true));
		result.addRow(row);
		
		// loop through kids
		QuerySolutionMap initialSettings = new QuerySolutionMap() ;
		initialSettings.add("ind", ind) ;
		initialSettings.add("p", this.relationship) ;
		
		String[] values = {"x"};
		
		DataResultSet kids = executeQuery(GET_NAV_ELEMENTS, initialSettings, values);
		Individual resource = null;
		if( kids.first()) {
			do {
				resource = model.getIndividual(kids.getStringValue(0));			
				convertToNavigationRS( resource, ind, result, level+1);
			}while( kids.next());
		}
		
	}
	
	private String getURI(Individual ind) {
		String namespace, prefix, uri = null;
		if( usePrefix ){
			namespace = ind.getNameSpace();
			prefix = namespaceMap.get(namespace);
			uri = prefix + ":" + ind.getLocalName();
		} else {
			uri = ind.getURI();
		}
		return uri;
	}
	*/
	
	private DataResultSet executeQuery(Query query, QuerySolutionMap initialSettings, String[] values) {
		QueryExecution qexec =   QueryExecutionFactory.create(query, this.m_model, initialSettings) ;
		DataResultSet drs = new DataResultSet(values);
		com.hp.hpl.jena.query.ResultSet results = null;
		Vector<Resource> row = null;
		try {
			results = qexec.execSelect() ;
		    for ( ; results.hasNext() ; )    {
		    	QuerySolution sol = results.nextSolution();
		    	for(int i = 0; i < values.length; i++){
		    		row = new Vector<Resource>();
		    		row.add(sol.getResource(values[i]));
		    	}
		    	drs.addRow(row);
		    }
		}
		finally { 
			qexec.close() ; 
		}
		return drs;
	}
	
		
	/**
	 * Generates XML structure that can be used to present the ontology as a tree. The tree requires:
	 * the top entity, class of entities to extract, their relationship to the root element,language
	 * to get entities labels and country for which the tree is build.
	 * In order to start building the tree we need either the top most entity or the class of the top
	 * entities. If 'root' parameter is null then the 'entityClass' parameter is used
	 * to extract all entities of this class.
	 * 
	 * @param root - top entity, might be null
	 * @param entityClass - class of the root entities 
	 * @param relationship - relationship of between the root entity and the tree entities
	 * @param language - language used to extract labels
	 * @param country - country code used to filter entities
	 * @return XML structure that present ontology structure as a tree
	 * @throws DataException 
	 */
	public StringBuffer convertToXML(Individual root, OntClass entityClass, Property relationship, String language, String country) throws DataException {
		OntologyUtils.debug("convertToXML() : " + root + ", " + entityClass);
		if( entityClass == null || relationship == null)
			throw new DataException("To generate the tree structure either the root class and relationship must be set");
		
		if( language == null || language.length() == 0 )
			language = DEFAULT_LANGUAGE;
		
		if( country == null || country.length() == 0 )
			country = DEFAULT_COUNTRY;
		
		if(root != null){
			Individual[] roots = {root};
			return convertToXML(roots, relationship, language, country);
		} else if(entityClass != null) {
			DataResultSet rootEntities = getRootElements(entityClass, relationship, language, country);
			Individual[] roots = null;
			if( rootEntities != null && rootEntities.first()) {
				roots = new Individual[rootEntities.getNumRows()];
				int i =0;
				do {
					roots[i++] = m_model.getIndividual(rootEntities.getStringValueByName("uri"));
				} while( rootEntities.next());
				OntologyUtils.debug("convertToXML() : roots: " + roots.length);
				return convertToXML(roots, relationship, language, country );
			}
		}
		return null;
	}
	
	/*
	public StringBuffer secureConvertToXML(String[] ids){
		OntologyUtils.debug("secureConvertToXML() : " + this.m_relationship + ", " + this.m_rootClass );
		Individual[] resources = new Individual[ids.length];
		RDFNode[] nodes = new RDFNode[ids.length];
		for(int i = 0; i<ids.length; i++){
			OntologyUtils.debug("ids[] = " + ids[i]);
			resources[i] = model.getIndividual(ids[i]);
			OntologyUtils.debug("individual: " + resources[i]);
			nodes[i] = model.getRDFNode(resources[i].asNode());
		}
		Vector<Individual> vec = new Vector<Individual>();
		Individual r = null;
		for(int i = 0; i<resources.length; i++){
			r = resources[i];
			for(int m = 0; m<nodes.length; m++){
				if( r.hasProperty(relationship, nodes[m]) ){System.out.println("Remove: " + r);
					vec.remove(r);
					break;
				}else if (!vec.contains(r)){System.out.println("Add: " + r);
					vec.add(r);					
				}
			}
		}
		Individual[] roots = new Individual[vec.size()];
		OntologyUtils.debug("Vector " + vec);
		vec.copyInto(roots);
		return convertToXML(roots);
	}
*/
	
	private StringBuffer convertToXML(Individual[] roots, Property relationship, String language, String country) throws DataException {
		OntologyUtils.debug("Start convertToXML(): " + roots.length );
		StringBuffer rez = new StringBuffer(TREE_HEADER);
		rez.append("<tree id=\"0\">");
		for( int i = 0; i < roots.length; i++){
			Individual ind = roots[i];
			addXMLElement(ind,  rez, relationship, language, country);
		}
		rez.append("</tree>");
		OntologyUtils.debug("REZ: " + rez);
		return rez;
	}
	
	public static String protectSpecialCharacters(String originalUnprotectedString) {
	    if (originalUnprotectedString == null) {
	        return null;
	    }
	    boolean anyCharactersProtected = false;

	    StringBuffer stringBuffer = new StringBuffer();
	    for (int i = 0; i < originalUnprotectedString.length(); i++) {
	        char ch = originalUnprotectedString.charAt(i);

	        boolean controlCharacter = ch < 32;
	        boolean unicodeButNotAscii = ch > 126;
	        boolean characterWithSpecialMeaningInXML = ch == '<' || ch == '&' || ch == '>';

	        if (characterWithSpecialMeaningInXML || unicodeButNotAscii || controlCharacter) {
	            stringBuffer.append("&#" + (int) ch + ";");
	            anyCharactersProtected = true;
	        } else {
	            stringBuffer.append(ch);
	        }
	    }
	    if (anyCharactersProtected == false) {
	        return originalUnprotectedString;
	    }

	    return stringBuffer.toString();
	}

	
	/**
	 * Recursive method is used to build the XML structure by browsing terms and their related terms.
	 * 
	 * @param ind - current entity
	 * @param result - variable that accumulate the result
	 * @param relationship - 'children entities' will be defined by this relationship
	 * @param language - used to extract the correct labels 
	 * @param country - entity is only used if it has been assigned to this country
	 * @throws DataException 
	 */
	private void addXMLElement(Individual ind,  StringBuffer result,  Property relationship, String language, String country) throws DataException{
		OntologyUtils.debug("addXMLElement(): Individual: " + ind);
		if( !ind.isIndividual()) 
			return;
		String  uri, label = null;
		
		label = ind.getLabel(language);
		if( label == null)
			label = ind.getLocalName();
		uri = m_model.shortForm(ind.getURI());
		result.append("<item id=\"").append(uri).append("\" text=\"").append(protectSpecialCharacters(label)).append("\" >");
		//ExtendedIterator<Resource> kids = model.listResourcesWithProperty(property, ind).filterDrop(new FilterRoot(ind));
		
		DataResultSet individuals = getIndividuals(null, ind, relationship, language, country, null);
		if( individuals != null && individuals.first()){
			Individual resource = null;
			do {
				resource = m_model.getIndividual( individuals.getStringValue(2));	// URI		
				addXMLElement( resource, result, relationship, language, country);
			} while( individuals.next());
		}
		result.append("</item>");
	}
	
	
	/**
	 * Gets label for the given term id in the given language
	 * @param id - term id
	 * @param model - ontology model
	 * @param lang - language id
	 * @return
	 */
	public static String getLabel(String id, OntModel model, String lang) {
		OntologyUtils.debug("get Label for id: " + id + ", " + model.getGraph().size() + ", lang: " + lang);
		String label = null;
		Individual ind= model.getIndividual(model.expandPrefix(id));	
//		OntologyUtils.debug("get Class for id: " + id + ", " + model.getGraph().size() + ", class: " + ind.getOntClass());
		return getLabel(ind, model, lang);
	}
	
	/**
	 * Gets label for the given term id in the given language
	 * @param id - term id
	 * @param model - ontology model
	 * @param lang - language id
	 * @return
	 */
	public static Boolean isSpecialismOntClass(String id, OntModel model, String lang) {
//		OntologyUtils.debug("get Label for id: " + id + ", " + model.getGraph().size() + ", lang: " + lang);
		Individual ind= model.getIndividual(model.expandPrefix(id));	
		if(ind == null){
			return null;
		}
		ExtendedIterator<OntClass> oc = ind.listOntClasses(true);
		//OntologyUtils.debug("get Class for id: " + id + ", size: " + oc.toList().size());
		while(oc.hasNext()){
			String ocName = oc.next().toString();
			OntologyUtils.debug("get Class for id: " + id + ", class: " + ocName);
			if((ocName.indexOf("Specialism")) > -1){
				return new Boolean(true);
			}
					
			//oc.next();
		}
		//OntologyUtils.debug("get Class for id: " + id + ", " + model.getGraph().size() + ", class: " + ind.getOntClass());
		return new Boolean(false);
	}
	
	/**
	 * Gets label for the given term in the given language
	 * @param ind - term id
	 * @param model - ontology model
	 * @param lang - language id
	 * @return label of the term if found, otherwise null;
	 */
	public static String getLabel(Individual ind, OntModel model, String lang) {
		String label = null;
		OntologyUtils.debug("get Label for individual:" + ind);
		if( ind != null) {
			label = ind.getLabel(lang);
			if( label == null)
				label = ind.getLabel(null);
			if( label == null)
				label = ind.getLocalName();
		}
		OntologyUtils.debug("get Label :" + label);
		return label;
	}
	
	
	/**
	 * Returns all entities of the passed class that are in relationship with the passed
	 * entity identified by the parentTermId. These entities must have labels in the passed language/country
	 * @param classId - ontology class id
	 * @return result set that contains all terms that satisfy the search criteria
	 * @throws DataException 
	 */
	/*public DataResultSet getIndividuals(String classId) {
		DataResultSet drs = new DataResultSet(new String[] {"id", "label"});
		Vector<String> row = null;	
		
		Resource aClass = m_model.getOntClass(m_model.expandPrefix(classId));
		
		// populated search criteria
		QuerySolutionMap initialSettings = new QuerySolutionMap() ;
		initialSettings.add("CLASS", aClass) ;
		initialSettings.add("COUNTRY", m_model.createLiteral(m_country)) ;
		initialSettings.add("LANG", m_model.createLiteral(this.m_lang)) ;
		OntologyUtils.debug("Get entities search criteria: " +  initialSettings);
		
		// execute SPARQ query to extract all indiv. entities that satisfy the criteria
		QueryExecution qexec =   QueryExecutionFactory.create(Converter.QUERY_GET_INDIVIDUALS, m_model, initialSettings) ;
		com.hp.hpl.jena.query.ResultSet results = null;		
		
		// loop the result solution and populate the Result Set
		String id, label = null;
		QuerySolution solution  = null;
		try {
			results = qexec.execSelect() ;
		    for ( ; results.hasNext() ; )    {
		    	solution = results.nextSolution();
		    	id = m_model.shortForm( solution.getResource("subject").getURI());
		    	label = solution.getLiteral("label").getString();
		    	row = new Vector<String>();
		    	row.add(id); row.add( label);
		    	drs.addRow(row);
		    }
		}
		finally { 
			qexec.close() ; 
		}	
		OntologyUtils.debug("entities found: " + drs);
		return drs;
	}
	
	
	public DataResultSet getIndividuals(String parentTermId) {
		if( m_rootClass != null &&  m_relationship != null)
			return getIndividuals(this.m_rootClass.getURI(), parentTermId, this.m_relationship);
		else return null;
	}
	
	public DataResultSet getIndividuals(Resource entityClass, Resource parentEntity) {
		if( m_relationship != null)
			return getIndividuals(entityClass, parentEntity, this.m_relationship);
		else return null;
	}
	*/
	
	
	
	
	public DataResultSet getIndividuals( OntClass entityClass, String language, String country, String filterTerm) throws DataException {
		DataResultSet drs = null;
		
		if( entityClass == null || language == null ||country == null)
			throw new DataException("Entity class, language and country are mandatory parameters for extracting terms");
		
		if( filterTerm == null  || filterTerm.length() == 0)
			filterTerm = Converter.WILDCARD_SEARCH;
		else 
			filterTerm = filterTerm.trim();
			
		// populated search criteria
		QuerySolutionMap initialSettings = new QuerySolutionMap() ;
		initialSettings.add("SEARCHTERM", m_model.createLiteral( filterTerm) ) ;
		initialSettings.add("CLASS", entityClass) ;
		initialSettings.add("COUNTRY", m_model.createLiteral(country)) ;
		initialSettings.add("LANG", m_model.createLiteral(language)) ;
		
		OntologyUtils.debug("Get entities search criteria: " +  initialSettings);
		
		// execute SPARQ query to extract all indiv. entities that satisfy the criteria
		QueryExecution qexec =   QueryExecutionFactory.create(Converter.QUERY_GET_INDIVIDUALS, m_model, initialSettings) ;
		try {
			drs = executeSparqQuery(qexec);
		}
		finally { 
			qexec.close() ; 
		}
		return drs;
	}
	
	
	/**
	 * Returns all entities of the passed class that are in relationship with the passed
	 * entity identified by the parentTermId. These entities must have labels in the passed language/country
	 * @param parentEntity - identifies the parent entity
	 * @param relationship - defines the relationship
	 * @return result set that contains all terms that satisfy the search criteria
	 * @throws DataException 
	 */
	public DataResultSet getIndividuals(OntClass entityClass, Resource parentEntity, Property relationship, String language, String country, String filterTerm) throws DataException {
		DataResultSet drs = null;		
		
		if(  language == null ||country == null)
			throw new DataException("language and country are mandatory parameters for extracting terms");
		
		if( filterTerm == null || filterTerm.trim().length() == 0)
			filterTerm = Converter.WILDCARD_SEARCH;
		else 
			filterTerm = filterTerm.trim();
			
		
		// populated search criteria
		QuerySolutionMap initialSettings = new QuerySolutionMap() ;
		if( entityClass != null)
			initialSettings.add("CLASS", entityClass) ;
		initialSettings.add("SEARCHTERM", m_model.createLiteral( filterTerm) ) ;
		initialSettings.add("COUNTRY", m_model.createLiteral(country)) ;
		initialSettings.add("LANG", m_model.createLiteral(language)) ;
		if( parentEntity != null)
			initialSettings.add("PARENT", parentEntity) ;
		if( relationship != null)
			initialSettings.add("RELATION", relationship) ;
		OntologyUtils.debug("Get entities search criteria: " +  initialSettings);
		
		// execute SPARQ query to extract all indiv. entities that satisfy the criteria
		QueryExecution qexec =   QueryExecutionFactory.create(Converter.QUERY_GET_INDIVIDUALS_FULL, m_model, initialSettings) ;
		
		try {
			drs = executeSparqQuery(qexec);
		}
		finally { 
			qexec.close() ; 
		}
		return drs;
	}
	
	
	private DataResultSet executeSparqQuery(QueryExecution qexec) {
		DataResultSet drs = new DataResultSet(new String[] {"id", "label", "uri"});
		Vector<String> row = null;	
		
		// loop the result solution and populate the Result Set
		String id, label, uri = null;
		QuerySolution solution  = null;
		com.hp.hpl.jena.query.ResultSet results = null;	
		
		results = qexec.execSelect() ;
	    for ( ; results.hasNext() ; )    {
	    	solution = results.nextSolution();
	    	uri = solution.getResource("subject").getURI();
	    	id = m_model.shortForm( uri );
	    	label = solution.getLiteral("label").getString();
	    	row = new Vector<String>();
	    	row.add(id); row.add( label); row.add( uri);
	    	drs.addRow(row);
	    }
		OntologyUtils.debug("entities found: " + drs);
		return drs;
	}
	
	private static Query createQuery() {
		StringBuffer queryBase = new StringBuffer("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>");
		queryBase.append("PREFIX owl: <http://www.w3.org/2002/07/owl#>"); 
		queryBase.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>");
		queryBase.append("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>");
		
		
		queryBase.append("SELECT DISTINCT ?x WHERE { ?x rdf:type ?clas.");
		queryBase.append(" OPTIONAL { ?x ?p ?parent} OPTIONAL{ ?parent rdf:type ?ptype} ");
		queryBase.append(" FILTER( !bound( ?parent )  || ?ptype != ?clas ) }");
		return QueryFactory.create(queryBase.toString()) ;
	}
	
	private static Query createNavigationQuery() {
		StringBuffer queryBase = new StringBuffer("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>");
		queryBase.append("PREFIX owl: <http://www.w3.org/2002/07/owl#>"); 
		queryBase.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>");
		queryBase.append("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>");		
		
		queryBase.append("SELECT DISTINCT ?x ?label ?type");
		queryBase.append("WHERE { ?x ?p ?ind.?x rdfs:label ?label. ?x rdf:type ?type} ");
		queryBase.append(" ORDER BY ?label");
		return QueryFactory.create(queryBase.toString()) ;
	}
	
	/**
	 * Extracts all entities of the given class from the Ontology model. Entities are considered
	 * as top-level if they have no other entities of the same class that are in the given
	 * relationship with them.
	 * 
	 * @param rootClass - ontology class of the entities to be extracted
	 * @param relationship - relationship that is used to ensure that the extracted
	 * entities are on the most top (have no other related entities)
	 * 
	 * @return list of Ontology resources
	 */
	public DataResultSet getRootElements(OntClass rootClass, Property relationship, String language, String country) {		
		DataResultSet drs = new DataResultSet(new String[] {"id", "label", "uri"});
		Vector<String> row = null;		
		
		// populated search criteria
		QuerySolutionMap initialSettings = new QuerySolutionMap() ;
		initialSettings.add("CLASS", rootClass) ;
		initialSettings.add("COUNTRY", m_model.createLiteral(country)) ;
		initialSettings.add("LANG", m_model.createLiteral(language)) ;
		initialSettings.add("RELATION", relationship) ;
		OntologyUtils.debug("Get root elements search criteria: " +  initialSettings);
		
		// execute SPARQ query to extract all enetities that satisfy the criteria
		QueryExecution qexec =   QueryExecutionFactory.create(Converter.QUERY_GET_ROOTS, m_model, initialSettings) ;
		com.hp.hpl.jena.query.ResultSet results = null;		
		
		// loop the result solution and populate the Result Set
		String id, label, uri = null;
		QuerySolution solution  = null;
		try {
			results = qexec.execSelect() ;
		    for ( ; results.hasNext() ; )    {
		    	solution = results.nextSolution();
		    	uri = solution.getResource("subject").getURI();
		    	id = m_model.shortForm( uri );
		    	label = solution.getLiteral("label").getString();
		    	row = new Vector<String>();
		    	row.add(id); row.add( label); row.add( uri);
		    	drs.addRow(row);
		    }
		}
		finally { 
			qexec.close() ; 
		}	
		OntologyUtils.debug("getRootElements() : " + drs);
		return drs;
	}
	
	
	/**
	 * The method is used to generate an XML structure that is used to build a tree on UI.
	 * Using a list of ontology terms ids it extract the relevant terms from the ontology
	 * model and use its labels to populate XML.
	 * In some cases a parent term label is also used to construct a term label, for example
	 * Architecture [Building Construction] meaning that the term 'Architecture' is in parent-child 
	 * relationship with the term 'Building Construction'.
	 * 
	 * @param value - delimiter separated list of ontology terms. Might start and end with the delimiter;
	 * @param isParentInclude - boolean true if parents labels should be also included;
	 * @return XML structure
	 */
	public StringBuffer convertToXMLList(String value, Property relationship, String language, boolean isParentInclude) {

		StringBuffer trace = new StringBuffer("Value = ").append(value).append("\nrelationship = ").append(relationship);
		trace.append("\nlanguage = ").append(language).append("\n is parent included: ").append(isParentInclude);
		OntologyUtils.debug("convertToXMLList() starting;   " + trace.toString());
		
		StringBuffer rez = new StringBuffer(TREE_HEADER);
		rez.append("<tree id=\"0\">");
		
		if(value == null || value.trim().length() == 0) {
			rez.append("</tree>");
			return rez;
		}
		
		if( value.startsWith(DELIMETER)) 
			value = value.substring(1);
		if( value.endsWith(DELIMETER))
			value = value.substring(0, value.length() - 1);
			
		Individual ind = null;
		RDFNode parent = null;
		String termId, label = null;
		String[] list = value.split(DELIMETER);
		for(int i = 0; i < list.length; i++) {
			termId = list[i];
			label = Converter.getLabel(termId, m_model, language);
			if( label != null) {
				if( isParentInclude && relationship != null) {
					ind = m_model.getIndividual( m_model.expandPrefix(termId));
					Statement parents = ind.getProperty(relationship);
					if( parents != null ){
						parent = parents.getObject();
						if( parent != null) {
							ind = m_model.getIndividual(parent.asNode().getURI());
							String parentLabel = ind.getLabel(language);
							OntologyUtils.debug("parent label: " + parentLabel);
							if( parentLabel != null){
								label += " [" + parentLabel + "]";
							}
						}
					}
				}
				rez.append("<item id=\"").append(list[i]).append("\" text=\"").append(label).append("\" ></item>");
			}
		}
		rez.append("</tree>");
		OntologyUtils.debug("convertToXMLList() resut XML structure " + rez);
		return rez;
	}
	
	
	/**
	 * The method is used to generate an XML structure that is used to build a tree on UI.
	 * Using a list of ontology terms ids it extract the relevant terms from the ontology
	 * model and use its labels to populate XML.
	 * 
	 * @param value - delimiter separated list of ontology terms. Might start and end with the delimiter;
	 * @return XML structure
	 */
	/*public StringBuffer convertToXMLList(String value) {
		return (convertToXMLList(value, false));
	}
*/
	
	public static void main(String[] args) {
		String	ontology_life_events = "file:///C:/Users/Natalia/Infomentum/Hays/NataliaOntology/Hays_Ontology.owl";
		String root = "http://www.hays.co.uk/ont#Banking";
		String relationship = "http://www.hays.co.uk/ont#ParentTerm";
		String clas = "http://www.hays.co.uk/ont#Specialism";
	
		
	//	OntModel model = owlModel.getOntModel();
try {
		
	Oracle oracle = new Oracle("jdbc:oracle:thin:@localhost:1522:orclOnt", "rdfusr", "rdfusr");

	//	Model model = ModelOracleSem.createOracleSemModel( oracle, "xHaysSpec" +  OntologyUtils.ONT_TBL_EXT);
		
	//	Resource ontClass = model.getResource(clas);
		//Resource rootEl = model.getResource(root); 
	//	Property property = model.getProperty(relationship);
	//	System.out.println("OWL: "+ model.size());
		
		StringBuffer st = new StringBuffer("select x from table(SEM_MATCH('{?x rdf:type ?.}'");
		st.append(", SEM_Models('XHAYSSPEC_ONT_TBL'), null, ");
		st.append("SEM_ALIASES(SEM_ALIAS('hays', 'http://www.hays.co.uk/ont#')), null))");
	//	StringBuffer st = new StringBuffer("SELECT ?x ?p ?y  WHERE { ?x ?p ?y }");
	//	List<String> list = new ArrayList<String>();
		//list.add("'<http://http://www.direct.gov.uk/ontology#dgvocabulary>'");
		List<String> list = new ArrayList<String>();
		list.add("<http://www.hays.co.uk/ont#Specialism>");
	//	java.sql.ResultSet rs = oracle.executeQuery(st.toString(),0,1, list);
	//	while(rs.next()) {
		//	System.out.println("RS: " + rs.getString("x"));
	//	}
	//	rs.close();
	//    model.close();
	    oracle.dispose();


	//	OntModel model = OntologyReader.readOntology("xHaysSpec");//ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_TRANS_INF   );
//	System.out.println("OWL: " +OntologyFacade.getOntUserClasses("XTEST"));
		//model.read( ontology_life_events );
		
		
		//Converter con = new Converter(model, property, "en", ontClass, null, true);
	//	con.getRootElements();
		// Then read the data from the file into the ontology model
		
	//	System.out.println("OWL: " + model);
}catch(Exception ex) {System.out.println("Exception : " + ex);}
		
		//c.convertToXML(model, root, relationship, null);
	//	OntModel model = owlModel.getOntModel();
	/*	Resource rootEl = model.getResource(root); 
		Resource root2 = model.getResource("http://www.hays.com/ontology/spec#accounts_manager_director");
		Property property = model.getProperty(relationship);
		OntClass clasR = model.getOntClass(clas);
	//	RootSelector sel = new RootSelector(clasR, property, model);
	//	Filter f = new Converter.FilterRoot(rootEl);
	//	Individual ind = model.getIndividual("http://www.hays.com/ontology/spec#architectural_technician");
	//	System.out.println("Def: " + root2.hasProperty(property, model.getRDFNode(rootEl.asNode())));
	//	System.out.println("Class: " + clasR + clasR.listInstances().toList());
		Converter con = new Converter(model, property, null, clasR);
		String[] str = {"http://www.hays.com/ontology/spec#accounts_manager_director","http://www.hays.com/ontology/sector#acc_finance"};
		con.secureConvertToXML(str);
	*/	
	
	}

}
