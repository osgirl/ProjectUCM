package hays.co.uk;

import intradoc.common.SystemUtils;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeFilter;

public class NavigationFilterforSpecialism implements NodeFilter {
	private String customProp = null;
	//private String customPropValue = null;
	
	public NavigationFilterforSpecialism(String name, String val){
		this.customProp = name;
		//this.customPropValue = val;
	}

	@Override
	public short acceptNode(Node aNode) {
		try {
			  NamedNodeMap map = aNode.getAttributes();
			  if( map != null){
				  Node attr = map.getNamedItem(customProp);
				  if( attr != null && ! ("").equals(attr.getNodeValue()) ) {
					  SystemUtils.trace("hays_left_nav", "Node Accepted ");
					  return FILTER_ACCEPT;
				  }
			  } 
		  } catch (Exception ex) {
			 // debug(ex);
		  }
	       return FILTER_SKIP;
	  }
	}