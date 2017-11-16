package infomentum.ontology.navigation;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import infomentum.ontology.Converter;
import infomentum.ontology.loader.OntologyClassifierHandler;
import infomentum.ontology.loader.OntologyFacade;
import infomentum.ontology.utils.OntologyUtils;
import intradoc.common.NumberUtils;
import intradoc.common.ServiceException;
import intradoc.common.StringUtils;
import intradoc.data.DataException;
import intradoc.data.DataResultSet;
import intradoc.data.ResultSet;
import intradoc.data.ResultSetUtils;

public class OntologyNavigationHandler extends OntologyClassifierHandler {
	
	/*
	public void getOntBasedNavigation() throws ServiceException, DataException {
		String metadata = m_binder.getLocal("ont_metadata");
		
		if(metadata == null )
			throw new DataException("Metadata is not specified");
		
		ResultSet ontNavigationDRS = getResultSetForOntology(metadata);
		if( ontNavigationDRS != null)
			m_binder.addResultSet("ONT_NAVIGATION_RESULT", ontNavigationDRS);
	}*/
	
	
	public void getMatchingTerms()  throws ServiceException, DataException{
		String metadata = m_binder.getLocal("ont_metadata");		
		if(metadata == null )
			throw new DataException("Metadata is not specified");
		String str = m_binder.getLocal("str");
		
		List<String> list = new ArrayList<String>();
		OntModel model = OntologyFacade.getOntology(metadata);
		StringBuffer query = new StringBuffer("SELECT ?l WHERE { ?subject <http://www.w3.org/2000/01/rdf-schema#label> ?l");
		query.append(" FILTER regex(str(?l), '").append(str).append("', 'i') }");
		QueryExecution qexec = QueryExecutionFactory.create(query.toString(), model);
		try {
			com.hp.hpl.jena.query.ResultSet results = qexec.execSelect();
		      while (results.hasNext()) {
		        QuerySolution soln = results.nextSolution();
		        list.add(soln.getLiteral("l").getString());
		    //    System.out.println("runQuery: soln " + soln);
		      }
		    }
		    finally {
		      if (qexec != null)
		        qexec.close();
		    }
		    m_binder.putLocal("terms", list.toString());
		    OntologyUtils.debug("Matching terms: " + list);
	}
	
	
	public void getRelatedTerms() throws ServiceException, DataException{
		String metadata = m_binder.getLocal("ont_metadata");		
		if(metadata == null )
			throw new DataException("Metadata is not specified");
		String termsList = m_binder.getLocal("terms");
		String relation = m_binder.getLocal("relation");
		if( termsList == null || termsList.length() == 0 || relation == null || relation.length() == 0)
			return;
		
		boolean isObject = StringUtils.convertToBool(m_binder.getLocal("isObject"), false);
		
		OntologyUtils.debug("Related term: " + termsList + ", " + relation + ", " + metadata);
		List<String> values = new ArrayList<String>();
		OntModel model = OntologyFacade.getOntology(metadata);
		Map<String,String> prefixes = model.getNsPrefixMap();
		
		OntologyUtils.debug("Map of Prefixes: " + prefixes);
		Property relatProp = null;
		
		String[] pair = OntologyUtils.extractPrefixUri(relation);
		//System.out.println("Pair: " + pair);
		if( pair != null) {
			OntologyUtils.debug(pair[0] + " - " + pair[1]);
			relatProp = model.getProperty(model.getNsPrefixURI(pair[0]) + pair[1]);
		} else
			relatProp = model.getProperty(relation);
		OntologyUtils.debug("Related property: " + relatProp);
		if( relatProp == null)
			return;
		
		String[] list = termsList.split(",");
		Resource resource, relatedResource = null;
		String uri = null;
		String namespace, ns = null;
		for(int i = 0; i < list.length; i++) {
			pair = OntologyUtils.extractPrefixUri(list[i]);
			if( pair != null) {
				resource = model.getResource(model.getNsPrefixURI(pair[0]) + pair[1]);
			} else
				resource = model.getResource(list[i]);
			OntologyUtils.debug("Related term URI: " + resource.getURI());
			if( isObject) {
				NodeIterator iter = model.listObjectsOfProperty(resource, relatProp);
				Node rdfNode = null;
				while(iter.hasNext()) {
					rdfNode = iter.nextNode().asNode();
					namespace = rdfNode.getNameSpace();
					ns = model.getNsURIPrefix(namespace);
					uri = ns + ":" + rdfNode.getLocalName();
//					OntologyUtils.debug("Termlist: " + termsList+ " uri:"+uri);
					if( !values.contains(uri) && termsList.indexOf(uri) < 0 ) {
						values.add(uri);
					}
				}
			} else {
				ResIterator iter = model.listResourcesWithProperty(relatProp, resource);				
				while(iter.hasNext()) {
					relatedResource = iter.nextResource();
					namespace = relatedResource.getNameSpace();
					ns = model.getNsURIPrefix(namespace);
					uri = ns + ":" + relatedResource.getLocalName();
					OntologyUtils.debug("Termlist: " + termsList+ " uri:"+uri);
//					if( !values.contains(uri) && termsList.indexOf(","+uri+",") < 0 ) {
					if( !values.contains(uri)) {//changed 19Aug2011 
						values.add(uri);
					}
				}
			}
		}
		
		if( !values.isEmpty()) {
			//while( values.)
		}
		m_binder.putLocal(relation+metadata, values.toString().replaceAll(", ", ","));
		
	}
	
	/*
	public void postPopulateOntFacets() throws ServiceException, DataException {
		String metadata = null;
		ResultSet hitsDRS = null;
		DataResultSet ontDRS = null;
		Vector<String> countField = new Vector<String>();
		countField.add("count");
		countField.add("children");
		
		ResultSet idsRS = m_binder.getResultSet("OntKeysRS");
		DataResultSet mappingDRS = (DataResultSet)OntologyFacade.getOntMapping();
		if( mappingDRS != null && mappingDRS.first()) {
			do {
				metadata = mappingDRS.getStringValue(0); // metadata
				OntologyUtils.debug("\n postPopulateOntFacets(): Metadata: " + metadata) ;
				hitsDRS = m_binder.getResultSet("SearchResultNavigation" + metadata);
				if( hitsDRS != null ) {	
					Map<String,StringBuffer> childrenMap = new Hashtable<String,StringBuffer>();
					Map<String,StringBuffer> idsMap = new Hashtable<String,StringBuffer>();
					if(idsRS != null && idsRS.first()) {
						do {
							idsMap.put(idsRS.getStringValue(0), new StringBuffer(idsRS.getStringValue(1)));
						}while(idsRS.next());
					}
					OntologyUtils.debug("\n\nPopulated map of ids: " + idsMap);
					m_binder.removeResultSet("OntKeysRS");
					
					ontDRS = getResultSetForOntology(metadata);
					ontDRS.appendFields(countField);
					ontDRS.fillField(7, "");
					OntologyUtils.debug("Ont Navigation RS: " + ontDRS);
				//	OntologyUtils.debug("Start looping...");
					
					String  childCount, key, parent = null;
					StringBuffer parentIdsStr = null;
					int length = ontDRS.getNumRows()-1;
					for( int i = length; i >=0; i--) {
						ontDRS.setCurrentRow(i);
						key = ontDRS.getStringValue(0);
						
						//	OntologyUtils.debug( "\nterm id = " + key );							
							parent = ontDRS.getStringValue(1); // parentId - > attempt to count parent's hits
						
							OntologyUtils.debug("parent id= " + parent);
							
							StringBuffer childIds = idsMap.get(key);
							if( parent.length() > 0 ) {
								if( idsMap.containsKey(parent) ) {
									parentIdsStr = idsMap.get(parent);
								} else {
									parentIdsStr = new StringBuffer();
									idsMap.put(parent, parentIdsStr);
								}
								
								// add child to list od children
								if(childrenMap.containsKey(parent) && childrenMap.get(parent).indexOf(key) < 0) {
									childrenMap.get(parent).append(",").append(key);
								} else {
									StringBuffer childrenList = new StringBuffer(key);
									childrenMap.put(parent, childrenList);
								}
							}
							childCount = "0";
						//	OntologyUtils.debug("Child str: " + childIds);
							if(childIds != null && childIds.length() > 0){
								String[] childIdsList = childIds.toString().split(",");
								for(int m = 0; m < childIdsList.length; m=m+2){
									OntologyUtils.debug("Child: " + childIdsList[m] + " - " + childIdsList[m + 1]);
									String id = childIdsList[m];
									String curCount = childIdsList[m + 1];
									childCount = String.valueOf(NumberUtils.parseInteger(curCount.trim(),0) + NumberUtils.parseInteger(childCount.trim(),0));
									OntologyUtils.debug("Child count: " + childCount);
									if( parent.length() > 0 ) {
										if(parentIdsStr.indexOf(id.trim()) < 0){
								//			OntologyUtils.debug(id + " Not found in parent str: " + parentIdsStr);
											if( parentIdsStr.length() > 0)
												parentIdsStr.append(",");
											parentIdsStr.append(id).append(",").append(curCount);										
								//			OntologyUtils.debug("Update Parent str: " + parentIdsStr);
										}
									}
								}
							}
							if( NumberUtils.parseInteger(childCount.trim(),0) > 0){
								ontDRS.setCurrentValue(6, childCount);// set count for current node
								
								if( childrenMap.containsKey(key)) {
									ontDRS.setCurrentValue(7, childrenMap.get(key).toString());
								}
							} else
								ontDRS.deleteCurrentRow();
							
							
							OntologyUtils.debug("Current row after updates: " + ontDRS.getCurrentRowProps());
							}
						}
						
				
					m_binder.addResultSet("FacetResultSet_"+metadata, ontDRS);
				
				
			} while( mappingDRS.next());
		}
		
	} */
	
	/*
	private DataResultSet getResultSetForOntology(String metadata)throws ServiceException, DataException  {
		OntModel model = OntologyFacade.getOntology(metadata);
		ResultSet mapping = OntologyFacade.getOntMapping();//readOntMetadataMapping(OntologyUtils.META_CONF_RS_NAME, OntologyUtils.ONT_CONF_FILE);
		Map<String,String> prefixMap = OntologyFacade.getOntPrefixes(metadata);
		String relation, clas = null;
		DataResultSet ontNavigationDRS = null;
		
		String[] rootElements = getRootElementsForUser( this.m_service.getUserData(), metadata );
	//	System.out.println("getOntNavigation(): rootElements: " + rootElements);
		
		if( mapping != null && mapping.first()){
			do {
				if(metadata.equals( mapping.getStringValue(0)) ) {
					relation = mapping.getStringValue(1);
					clas = mapping.getStringValue(2);
					Converter con = new Converter(model, model.getProperty(relation), null, model.getOntClass(clas), prefixMap, true);
					if( rootElements != null) {
						ontNavigationDRS = con.secureConvertToNavigationRSSecure(rootElements);
					} else {
						ontNavigationDRS = con.convertToNavigationRS();
					}
					break;
				}
			} while( mapping.next());
		}
		return ontNavigationDRS;
	}
*/
}
