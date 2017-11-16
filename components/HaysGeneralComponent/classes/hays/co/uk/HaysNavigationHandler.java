package hays.co.uk;

import java.io.File;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;




import sitestudio.SSCommon;
import sitestudio.SSDomLoader;
import sitestudio.SSHierarchyServiceHandler;
import sitestudio.SSHierarchyServiceHandler.SiteInfo;

import intradoc.common.FileUtils;
import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.data.DataBinder;
import intradoc.data.DataException;
import intradoc.data.DataResultSet;
import intradoc.data.Parameters;
import intradoc.data.ResultSet;
import intradoc.resource.ResourceUtils;
import intradoc.shared.SharedObjects;

public class HaysNavigationHandler extends SSHierarchyServiceHandler  {
	public static final String RS$ID = "id";
	public static final String RS$NODE_ID = "nodeId";
	public static final String RS$PARENT_ID = "parentNodeId";
	public static final String RS$LABEL = "label";
	public static final String RS$LEVEL = "level";
	public static final String RS$HREF = "href";
	public static final String RS$NUMKIDS = "childNumber";
	public static final String RS$ISLASTKID = "isLastChild";
	public static final String RS$STARTPOINT = "startingNodeId";
	public static final String RS$GROUPPOINT = "groupingNodeId";
	public static final String RS$SPECIALISMPOINT = "specialismNodeId";
	public static final String RS$CONTRIBONLY = "contributorOnly";
	public static final String[]NAV_RS_FIELDS = {RS$NODE_ID,RS$PARENT_ID,RS$LABEL,RS$LEVEL,RS$HREF,RS$NUMKIDS,RS$ISLASTKID,RS$STARTPOINT,RS$GROUPPOINT,RS$SPECIALISMPOINT,RS$CONTRIBONLY};
	
	private static final List<String> STARTING_NODES = SharedObjects.getEnvValueAsList("NavigationStartingSections");
	private static final String GROUPING = SharedObjects.getEnvironmentValue("GroupingSectionId");
	
	private static final String GROUPING_CUSTOM_PROPERTY = SharedObjects.getEnvironmentValue("GroupingSectionCustomProperty");
	

	private SiteInfo originalSiteinfo = null;
	
	class NavigationFilter implements NodeFilter		{
		private String customProp = null;
		private String customPropValue = null;
		
		public NavigationFilter(String name, String val){
			this.customProp = name;
			this.customPropValue = val;
		}
		
		public short acceptNode(Node aNode)  {
			  try {
				  NamedNodeMap map = aNode.getAttributes();
				  if( map != null){
					  Node attr = map.getNamedItem(customProp);
					  if( attr != null && attr.getNodeValue().equals(customPropValue) ) {
						  return FILTER_ACCEPT;
					  }
				  } 
			  } catch (Exception ex) {
				  debug(ex);
			  }
		       return FILTER_SKIP;
		  }
		}
	
	class NavigationContribFilter implements NodeFilter		{
		private String customProp = null;
		private String customPropValue = null;
		private Node root = null;
		
		public NavigationContribFilter(String name, String val, Node root) {
			this.customProp = name;
			this.customPropValue = val;
			this.root = root;
		}
		
		public short acceptNode(Node aNode)  {
			  try {
				  NamedNodeMap map = aNode.getAttributes();
				  if( map != null){
					  Node attr = map.getNamedItem(customProp);
					  if( attr != null && attr.getNodeValue().equals(customPropValue) && root.equals(aNode.getParentNode())) {
						  return FILTER_ACCEPT;
					  }
				  } 
			  } catch (Exception ex) {
				  debug(ex);
			  }
		       return FILTER_SKIP;
		  }
		}
	
	
	class ProjectNamespaceContext implements NamespaceContext {
		
		public String getNamespaceURI(String prefix) {
			if( prefix == null)
				throw new NullPointerException("Null prefix");
			else if( "project".equals(prefix))
				return "http://www.stellent.com/sitestudio/Project/";
			return XMLConstants.NULL_NS_URI;
		}

		public String getPrefix(String namespaceURI) {
			// TODO Auto-generated method stub
			return null;
		}

		public Iterator getPrefixes(String namespaceURI) {
			// TODO Auto-generated method stub
			return null;
		}
	}
	

	public void getNodeForProperty() throws ServiceException, DataException {	
		String siteId = super.m_binder.getLocal("siteId");
		String property = super.m_binder.getLocal("property");
		String value = super.m_binder.getLocal("value");
		String startNodeId = super.m_binder.getLocal("startNodeId");
		originalSiteinfo = reserveSiteInfo(siteId, false);
		debug("getNodeForProperty()... " + siteId + ", " + property + ", " + value);
		Node doc = originalSiteinfo.m_projectDom;
		String filter = null;
		try {
			if( startNodeId != null) {
				Node startNode = getNode(startNodeId, originalSiteinfo.m_projectDom);
				if( startNode != null ) {
					doc = startNode;
					filter = "descendant::project:section[@" + property + "='" + value + "']";
				}
			}
			
			if( filter == null)
				filter = "//project:section[@" + property + "='" + value + "']";
			debug(" search criteria: " + filter);
			NodeList nodes = filterNodes(filter, doc);
			if( nodes != null) {
				Node node = nodes.item(0);
				if( node != null ) {
					String nodeId = node.getAttributes().getNamedItem("nodeId").getNodeValue();
					debug("node found: " + nodeId);
					m_binder.putLocal("targetNodeId", nodeId);	
				}
			}
			
		} finally {
			releaseSiteInfo(originalSiteinfo, false);
		}
	}
	
	
	
	 public void loadHaysResultSet() throws ServiceException, DataException {		 
	             
	        String siteId = super.m_binder.getLocal("siteId");
	        boolean flag = isContributorMode();
	        debug("\n\tloadSiteNavResultSet() starting..." + siteId);
	       
	     // preload: load project info and validate custom property set as env. variable
	        originalSiteinfo = reserveSiteInfo(siteId, false);
	        
	        debug("Original Site info: " + originalSiteinfo.m_siteId + ", " + originalSiteinfo.m_dDocName + ", " + originalSiteinfo.m_projectDom);
	      
	        SiteInfo siteinfo = null;
	     
	        String s1 = super.m_binder.getLocal("ssUrlPrefix");
	        long l = 0L;
	        long l1 = -2L;
	        DataResultSet navigationRS = null;
	        Object navObj = null;
	        
	        try {
	        	navObj = SharedObjects.getObject("NAV", siteId);
	        	
		        if( navObj != null)	{
		        	siteinfo = (SiteInfo)navObj;
			        if (flag) {
			            l = siteinfo.m_siteNavContribOnlyTimestampCheckTime;
			            l1 = siteinfo.m_siteNavContribOnlyFileTimestamp;
			            navigationRS = siteinfo.m_drsetSiteNavContribOnly;
			            debug("check cache for contribution: " + navigationRS);
			        } else {
			            l = siteinfo.m_siteNavTimestampCheckTime;
			            l1 = siteinfo.m_siteNavFileTimestamp;
			            navigationRS = siteinfo.m_drsetSiteNav;
			            debug("check cache: " + navigationRS);
			        }
		        } else {
		        	siteinfo = new SiteInfo();
		        }
	        
		        if (navigationRS != null) {
		            long l2 = System.currentTimeMillis();
		            debug("check time stamp: " + (l2 - l));
		            if (l2 - l > 5000L) {
		                String s4 = SSCommon.getSiteWeblayoutDir(siteId);
		                String s6 = flag ? "sitenavigation_co.hda" : "sitenavigation.hda";
		                long l4 = (new File(s4 + s6)).lastModified();
		                debug("Compare timestamp: file: " + l4 + ", siteInfo time: " + l1);
		                if (l4 != l1) {
		                	navigationRS = null;
		                	debug("clear cache");
		                }
		                if (flag) {
		                    siteinfo.m_siteNavContribOnlyTimestampCheckTime = l2;
		                } else {
		                    siteinfo.m_siteNavTimestampCheckTime = l2;
		                }
		            }
		        }
		        if (navigationRS == null) {
		            String s3 = SSCommon.getSiteWeblayoutDir(siteId);
		            String s5 = flag ? "sitenavigation_co.hda" : "sitenavigation.hda";
		            long l3 = (new File(s3 + s5)).lastModified();
		                 
		            navigationRS =  buildNavigation(originalSiteinfo.m_projectDom, flag, siteId);
		            debug("rebuild cache: " );
		            
	                if (flag) {
	                    siteinfo.m_cachedSiteNavContribOnlyUrlPrefix = s1;
	                    siteinfo.m_siteNavContribOnlyFileTimestamp = l3;
	                    siteinfo.m_drsetSiteNavContribOnly = navigationRS;
	                } else {
	                    siteinfo.m_cachedSiteNavUrlPrefix = s1;
	                    siteinfo.m_siteNavFileTimestamp = l3;
	                    siteinfo.m_drsetSiteNav = navigationRS;
	                }
	                siteinfo.m_siteId = siteId;
                    SharedObjects.putObject("NAV", siteId, siteinfo);
                    debug("save in cache: " + navigationRS);
		        }
	       
		        if (navigationRS != null) {
		            DataResultSet navigationRS1 = navigationRS.shallowClone();
		            super.m_binder.addResultSet("HaysNavNodes", navigationRS);
		        }
	        
	        } 
	        catch(DataException dex) {
	        	debug(dex);
	        	throw new ServiceException(dex.getMessage());
	        }
	        finally {
	        	releaseSiteInfo(originalSiteinfo, false);
	        }
	    }

	 
	
	public DataResultSet buildNavigation(Document document, boolean isContrib, String siteId) throws ServiceException, DataException {
		debug("buildNavigation starting...");		
		
		DocumentTraversal traversal = (DocumentTraversal) document;
		Document documentNew = createNewDom("Navigation");
		
		
		
		
		
		
		String country=siteId.substring(siteId.length()-2, siteId.length()).toUpperCase();
		//String []customizeLHNavigationCountryList = SharedObjects.getEnvironmentValue("customizeLHNavigationCountryList").split(",");
		String customizeLHNavigationCountryList = SharedObjects.getEnvironmentValue("customizeLHNavigationCountryList").toUpperCase();
		boolean custLHNav=false;
		if(customizeLHNavigationCountryList.indexOf(country)>=0)
		{
			custLHNav=true;
		}
		
		SystemUtils.trace("hays_left_nav1", "country: " + country+",customizeLHNavigationCountryList:"+customizeLHNavigationCountryList+",custLHNav:"+custLHNav);
		
		
		//if("CN".equalsIgnoreCase(country) || "JP".equalsIgnoreCase(country))
		if(custLHNav)
		{	
			SystemUtils.trace("hays_left_nav1", "STARTING_NODES: " + STARTING_NODES+", country:"+country);
			if( STARTING_NODES != null) 
			{
	        	Node currentNode = null;
	        	Node currentNodeClone =null;
	        	Node currentSubChildNodeClone=null;
	        	//String currentNodeId = null;
	        	
	    		for( Iterator<String> it = STARTING_NODES.iterator(); it.hasNext();) 
	    		{
	        		String startNodeId = it.next();
	        		Node startNode = getNode(startNodeId, document);
	        		Node clone = documentNew.importNode(startNode, false);
	        		//String cloneNodeId = clone.getAttributes().getNamedItem("nodeId").getNodeValue();
	        		
	        		//int cloneNodeLevel=getLevel(clone);
	        		//SystemUtils.trace("hays_left_nav1", "inside outer for loop, cloneNodeId:"+cloneNodeId+",cloneNodeLevel:"+String.valueOf(cloneNodeLevel));
					documentNew.getDocumentElement().appendChild( clone );
					
					NodeList allChildNodesList = startNode.getChildNodes();
		        	int length = allChildNodesList.getLength();
		        	SystemUtils.trace("hays_left_nav", "length::"+length);
					for( int i = 0; i < length; i++) 
					{
		        		currentNode = allChildNodesList.item(i);
		        		
		        		//currentNodeId = currentNode.getAttributes().getNamedItem("nodeId").getNodeValue();
		        		//int currentNodeLevel=getLevel(currentNode);
		        		//SystemUtils.trace("hays_left_nav1", "i=="+i+",currentNodeId: " + currentNodeId+", currentNodeLevel::"+String.valueOf(currentNodeLevel));
		        		
		        		currentNodeClone = documentNew.importNode(currentNode, false);
		        		SystemUtils.trace("hays_left_nav1", "HIde nav Display property"+currentNodeClone.getAttributes().getNamedItem("active").getNodeValue()+currentNodeClone.getAttributes().getNamedItem("label").getNodeValue());
		        		String toappendSpec = currentNodeClone.getAttributes().getNamedItem("active").getNodeValue();
		        		if(toappendSpec.equalsIgnoreCase("true"))
		        		{	
		        		clone.appendChild(currentNodeClone);
		        		
		        		//SystemUtils.trace("hays_left_nav1", "Appended node: "+currentNodeId +" to clone:"+cloneNodeId);
		        		
		        		//NodeList allChildNodesOfCloneList = clone.getChildNodes();
    		        	//int cloneLength = allChildNodesOfCloneList.getLength();
    		        	//SystemUtils.trace("hays_left_nav1","cloneLength: "+cloneLength);
		        		
		        		
		        		NodeList allSubChildNodesList = currentNode.getChildNodes();
			        	int subChildLength = allSubChildNodesList.getLength();
			        	SystemUtils.trace("hays_left_nav","subChildLength: "+subChildLength);
			        	
			        	
		        		for(int j = 0; j < subChildLength; j++)
		        		{
		        			Node currentSubChildNode = allSubChildNodesList.item(j);
			        		
		        			//String currentSubChildNodeId = currentSubChildNode.getAttributes().getNamedItem("nodeId").getNodeValue();
			        		//int currentSubChildNodeLevel=getLevel(currentSubChildNode);
			        		//SystemUtils.trace("hays_left_nav1", "j=="+j+",currentSubChildNodeId: " + currentSubChildNodeId+", currentSubChildNodeLevel::"+String.valueOf(currentSubChildNodeLevel));
			        		
			        		currentSubChildNodeClone = documentNew.importNode(currentSubChildNode, false);
			        		String toappendSubspec = currentSubChildNodeClone.getAttributes().getNamedItem("active").getNodeValue();
			        		if(toappendSubspec.equalsIgnoreCase("true"))
			        		{
			        			SystemUtils.trace("hays_left_nav1", "HIde nav Display property11"+toappendSubspec+currentSubChildNodeClone.getAttributes().getNamedItem("label").getNodeValue());
			        		currentNodeClone.appendChild(currentSubChildNodeClone);
			        		}
			        		//SystemUtils.trace("hays_left_nav1", "Appended child node: "+currentSubChildNodeId +" to node:"+currentNodeId);
		        		}
		        	  }
					}
	    		}
	    		
	        }	
		}
		else
		{
			SystemUtils.trace("hays_left_nav", "inside else for other contries...");
			if( GROUPING != null && STARTING_NODES != null) 
			{
	        	Node groupingNode = getNode(GROUPING, document);
	        	debug("grouping node: " + groupingNode);
	        	NodeList groupingNodes = groupingNode.getChildNodes();
	        	int length = groupingNodes.getLength();
	        	Node aGroupingNode = null;
	        	String aGroupingNodeId = null;
	        	
	        	
	        	
	    		for( Iterator<String> it = STARTING_NODES.iterator(); it.hasNext();) 
	    		{
	        		String startNodeId = it.next();
	        		Node startNode = getNode(startNodeId, document);
	        		Node clone = documentNew.importNode(startNode, false);
					documentNew.getDocumentElement().appendChild( clone );
					
					for( int i = 0; i < length; i++) 
					{
		        		aGroupingNode = groupingNodes.item(i);
		        		aGroupingNodeId = aGroupingNode.getAttributes().getNamedItem("nodeId").getNodeValue();
		        		SystemUtils.trace("hays_left_nav", "Grouping node ID: " + aGroupingNodeId);
		        		
	            		NodeIterator kids = traversal.createNodeIterator(startNode, NodeFilter.SHOW_ELEMENT, new NavigationFilter(GROUPING_CUSTOM_PROPERTY, aGroupingNodeId), true);
		        		
		        		
	            		if( kids != null ) 
	            		{

	            			debug("Kids are appended to " + aGroupingNodeId );

	            			Node groupungClone = documentNew.importNode(aGroupingNode, false);

	            			clone.appendChild(groupungClone);

	            			SystemUtils.trace("hays_left_nav","Add grouping node: " + groupungClone.getAttributes().getNamedItem("nodeId").getNodeValue() + ", " + groupungClone.getParentNode().getAttributes().getNamedItem("nodeId").getNodeValue());


	            			Node aKid = kids.nextNode();
	            			

	            			while( aKid != null)
	            			{
	            				
	            				Node specialismClone =documentNew.importNode(aKid, false);
	            			
	            				groupungClone.appendChild(specialismClone);
	            				SystemUtils.trace("hays_left_nav","Add Specialism node: " + specialismClone.getAttributes().getNamedItem("nodeId").getNodeValue());
	            				//debug("Appended kid: " + aKid.getNodeName());

	            				NodeIterator SubSpecIter = traversal.createNodeIterator(aKid, NodeFilter.SHOW_ELEMENT, new NavigationFilterforSpecialism(GROUPING_CUSTOM_PROPERTY ,specialismClone.getAttributes().getNamedItem("nodeId").getNodeValue()), true);

	            				if(SubSpecIter !=null){
	            					Node SubSpec = SubSpecIter.nextNode();
	            					while( SubSpec != null){
	            						Node subSpecialismClone=documentNew.importNode(SubSpec, false);
	            						specialismClone.appendChild(subSpecialismClone);
	            						SystemUtils.trace("hays_left_nav","Add Subspecialism node: " + subSpecialismClone.getAttributes().getNamedItem("nodeId").getNodeValue());
	            						//SystemUtils.trace("hays_left_nav", "Subspecialism: " + SubSpec.getNodeName());
	            						SubSpec = SubSpecIter.nextNode();
	            						}
	            					}
	            				aKid = kids.nextNode();
	            				}
	            			}
					}
	    		}
	    		
	        }
		}
		if( isContrib ) 
		{
			Node root = getHierarchyRootNode(originalSiteinfo);
			debug("project root: " +  root);
			NodeIterator contribNodes = traversal.createNodeIterator(document, NodeFilter.SHOW_ELEMENT, new NavigationContribFilter("contributorOnly", "TRUE", root), true);
		//	NodeList contribNodes = filterNodes("//project:section[@contributorOnly='TRUE']", document);
			if( contribNodes != null) {
				Node contribNode = contribNodes.nextNode();
    			debug("adding contrib only sections.. " );
				Node clone = null;
				while( contribNode != null) {    				
					clone = documentNew.importNode(contribNode, true);
					debug("contrib node: " + clone + ", " + clone.getChildNodes());
					documentNew.getDocumentElement().appendChild( clone );
					contribNode = contribNodes.nextNode();
				}
			}
		}
		DataResultSet navigationRS = transformDomToResSet(documentNew, siteId);
    	if( navigationRS != null)
    		m_binder.addResultSet("HaysNavNodes", navigationRS);
    	return navigationRS;
		//return null;
	}
	
	protected DataResultSet transformDomToResSet(Document doc, String siteId) throws DataException, ServiceException{
		DataResultSet navDRS = new DataResultSet(NAV_RS_FIELDS);
		DocumentTraversal traversal = (DocumentTraversal) doc;
		Parameters row = null;
		
		NodeIterator nodes = traversal.createNodeIterator(doc.getDocumentElement(), NodeFilter.SHOW_ELEMENT, null, true);
		if (nodes != null) {
			Node node = nodes.nextNode();
			String serverRelativeSite = getServerRelativeSiteRoot(siteId);
			while (node != null) {
				try {
					row = copyContentToProp(node, siteId, serverRelativeSite);
					navDRS.addRow( navDRS.createRow(row) );
				} catch (Exception ex){ debug(ex);}
				node = nodes.nextNode();
			}
		}
		return navDRS;
	}
	


	
	private Document createNewDom(String root){
		try {
		 DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		      // then we have to create document-loader:
		DocumentBuilder loader = factory.newDocumentBuilder();

		// createing a new DOM-document...
		Document document = loader.newDocument();
		      
		// initially it has no root-element, ... so we create it:
		Element rootNode = document.createElement(root);
		Attr attribute = document.createAttribute(RS$NODE_ID);
		attribute.setValue("");
		rootNode.setAttributeNode(attribute);
		document.appendChild( rootNode );
		debug("\nNew docuemnt: " + document.getDocumentElement());
		return document;
		} catch (Exception ex) {
			ex.printStackTrace();
			debug(ex);
		}
		return null;
	}
	
	private Parameters copyContentToProp(Node node, String siteId, String serverRelativeSite) {
		DataBinder row = new DataBinder();	
		
		NamedNodeMap attributes = node.getAttributes();		
		String nodeId = attributes.getNamedItem(RS$NODE_ID).getNodeValue();
	//	debug("Node: " + node.getNodeName() + ", atts: " + attributes.getNamedItem("nodeId"));
		row.putLocal(RS$NODE_ID, nodeId);
		row.putLocal(RS$LABEL,attributes.getNamedItem(RS$LABEL).getNodeValue() );
		
		String href = null;
		try {
			href = getSiteRelativePublishUrl(siteId, nodeId);
		} catch(ServiceException ex) {
			debug(ex);
		}
		if( href != null)
			row.putLocal(RS$HREF, serverRelativeSite + href );
		else 
			row.putLocal(RS$HREF, serverRelativeSite );
		row.putLocal(RS$LEVEL, String.valueOf( getLevel(node)) );
		if(node.getParentNode() != null && node.getParentNode().getAttributes() != null)
			row.putLocal(RS$PARENT_ID, node.getParentNode().getAttributes().getNamedItem(RS$NODE_ID).getNodeValue() );
		else 
			row.putLocal(RS$PARENT_ID, "");
		
		// number of kids
		NodeList kids = node.getChildNodes();
		String numberOfKids = "0";
		if( kids != null)
			numberOfKids = String.valueOf( kids.getLength() );
		row.putLocal(RS$NUMKIDS, numberOfKids); 
		
		// is the last child
		if( node.getNextSibling() == null)
			row.putLocal(RS$ISLASTKID, "1");
		else
			row.putLocal(RS$ISLASTKID, "0");
		
		// starting node		
		row.putLocal(RS$STARTPOINT, getStartingNodeId(node));
		
		// specialism
		row.putLocal(RS$SPECIALISMPOINT, getSpecialismNodeId(node));
		
		// grouping node id
		row.putLocal(RS$GROUPPOINT, getGroupingNodeId(node));
		
		// contrib only
		Node contribOnly = attributes.getNamedItem("contributorOnly");
		if( contribOnly != null &&  "TRUE".equals( contribOnly.getNodeValue())) {
			row.putLocal(RS$CONTRIBONLY, "1");
			Node origNode = getNode(nodeId, originalSiteinfo.m_projectDom);
			row.putLocal(RS$PARENT_ID, origNode.getParentNode().getAttributes().getNamedItem(RS$NODE_ID).getNodeValue() );
		} else {
			row.putLocal(RS$CONTRIBONLY, "0");
		}
		debug(attributes.getNamedItem("nodeId") + " convert to property: " + row.getLocalData());
		return row;
	}
	
	
	private Node getNode(String nodeId, Object doc) {
		String id = "//project:section[@nodeId='" + nodeId + "']";
	//	debug("getNode() " + id);
		NodeList nodes = filterNodes(id, doc);
		if( nodes != null)
		    	return nodes.item(0);		
	    return null;
	}
	
	private int getLevel(Node node) {
		String id = "ancestor::project:section";
	//	debug("get level() for" + node);
		NodeList nodes = filterNodes(id, node);
		if( nodes != null)
		    	return nodes.getLength();		
	    return 0;
	}
	
	private String getStartingNodeId(Node node) {
		try {
			String filter = "ancestor::project:section[position()=last()]/@nodeId";
			XPath xpath = XPathFactory.newInstance().newXPath();
			xpath.setNamespaceContext(new ProjectNamespaceContext());
			String startId = (String) xpath.evaluate(filter, node, XPathConstants.STRING);
		//    debug("starting node id found? " + startId);
		   
		    return startId;
		} catch(Exception ex) { debug(ex);}
	    return null;
	}
	
	private String getGroupingNodeId(Node node) {
		try {
			String filter = "ancestor::project:section[position()=last()-1]/@nodeId";
			XPath xpath = XPathFactory.newInstance().newXPath();
			xpath.setNamespaceContext(new ProjectNamespaceContext());
			String startId = (String) xpath.evaluate(filter, node, XPathConstants.STRING);
		//    debug("starting node id found? " + startId);
		   
		    return startId;
		} catch(Exception ex) { debug(ex);}
	    return null;
	}
	
	private String getSpecialismNodeId(Node node) {
		try {
			String filter = "ancestor::project:section[position()=last()-2]/@nodeId";
			XPath xpath = XPathFactory.newInstance().newXPath();
			xpath.setNamespaceContext(new ProjectNamespaceContext());
			String startId = (String) xpath.evaluate(filter, node, XPathConstants.STRING);
		//    debug("starting node id found? " + startId);
		   
		    return startId;
		} catch(Exception ex) { debug(ex);}
	    return null;
	}
	
	private NodeList filterNodes(String filter, Object document){
		try {
			XPath xpath = XPathFactory.newInstance().newXPath();
			xpath.setNamespaceContext(new ProjectNamespaceContext());
			NodeList nodes = (NodeList) xpath.evaluate(filter, document, XPathConstants.NODESET);
		//    debug("parent nodes found? " + nodes);
		    if( nodes != null)
		    	return nodes;
		} catch(Exception ex) { debug(ex);}
	    return null;
	}
	
	private void debug(String message) {
		SystemUtils.trace("hays_nav",  message);
	}
	
	private void debug(Exception message) {
		SystemUtils.trace("hays_nav",  "Exception: " + message);
	}

}
