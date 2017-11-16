package oracletextsearch.search;

import intradoc.common.NumberUtils;
import intradoc.common.Report;
import intradoc.common.StringUtils;
import intradoc.common.SystemUtils;
import intradoc.data.DataBinder;
import intradoc.data.DataException;
import intradoc.data.DataResultSet;
import intradoc.data.ResultSetUtils;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public class HaysDrillDownContentHandler implements ContentHandler {
	
	 protected DataBinder m_binder;
     public String m_value;
     public String m_count;
     protected boolean m_isInCount;
   //  protected List m_navRSets;
     protected List<String> m_fields;
     protected String m_group_name;
     protected Map<String,String> m_captions;
     protected int m_numFields;
     protected char m_fieldSeparator;
     protected int m_totalCount;
     public int m_numGroups;
     private Hashtable<String, Vector<String>> m_map = new Hashtable<String, Vector<String>>();
     private Hashtable<String,Vector<String>> m_metaHits = new Hashtable<String,Vector<String>>();
     private int m_hitId = 0;

     public HaysDrillDownContentHandler() {
	        m_binder = null;
	        m_value = null;
	        m_count = null;
	        m_group_name = null;
	        m_isInCount = false;
	   //     m_navRSets = new ArrayList();
	        m_fields = new ArrayList();
	        
   	       //updated for 11g
	        //m_captions = null;
	        m_captions = new Hashtable();
	        
	        m_numFields = 0;
	        m_totalCount = 0;
	        m_numGroups = 0;
     }
     
     //updated for 11g
     public void init(DataBinder binder, List fields, char fieldSeparator) {
    	 this.m_binder = binder;
    	 this.m_fields = fields;
    	 this.m_fieldSeparator = fieldSeparator;
    	 if (this.m_fields == null)
    		 return;
         this.m_numFields = this.m_fields.size();
    }
     
    /* 
     //updated for 11g
     //public void init(DataBinder binder,  Map<String,String> captions, char fieldSeparator) {
    	public void init(DataBinder binder,  char fieldSeparator) {
	        m_binder = binder;
	     //   m_fields = fields;
	        
	        //updated for 11g - commented
	        //m_captions = captions;
	        
	        m_fieldSeparator = fieldSeparator;
	   //     if (m_fields != null) {
	   //         m_numFields = m_fields.size();
       //  }
     }*/

     public void characters(char ch[], int start, int length) throws SAXException {
   // 	 debug("characters: " + ch + ", " + length);
	        if (m_isInCount) {
	            m_count = new String(ch, start, length);
         }
     }

     public void endDocument() throws SAXException {
    	 debug("endDocument()");
     	//System.out.println("total hits count: " + m_totalCount);
	    
         
     }

     public void endElement(String uri, String localName, String name) throws SAXException {
    	 if (!localName.equals("count") && !localName.equals("group")) 
    		 debug("endElement(): " + localName + ", name: " + name);
	     if (localName.equals("count")) {
	            m_isInCount = false;
         } else  if (localName.equals("group")) {		            
	        	processGroup();
         } else if (localName.equals("groups")) {		            
	        	processGroups();
	        	m_totalCount = 0;
	        	
         }         
     }
     
     
     private void processGroup() {        
         if (m_value != null && !"idcnull".equalsIgnoreCase(m_value) && m_count != null) {        	 
        	 String key = m_value;
            	 
             int count = NumberUtils.parseInteger(m_count, 0);
	         m_totalCount += count;
         	 m_hitId++;
    		// if it's ontology value than extract each key values
	         String[] arrayOfKeys = key.split(";");
	         for( int i = 0; i < arrayOfKeys.length; i++){
	        	 if( arrayOfKeys[i].length() > 0 )
	        		 populateMetaValues(m_group_name, count, arrayOfKeys[i]);
	         }    		
         }  
     
         //   m_numGroups++;
            m_value = null;
            m_count = null;
     }
     
     
     private void processGroups() {
    	   if (m_fields.size() > 0) {
	            DataResultSet fields = new DataResultSet(new String[] {
	                "drillDownFieldName", "drillDownDisplayValue", "categoryCount", "totalCount"    });
	            
	            DataResultSet metadrset = new DataResultSet(new String[] {
                    "drillDownOptionValue", "drillDownModifier", "count", "fieldName"  });
	            for(Iterator<Vector<String>> en = m_map.values().iterator(); en.hasNext();){
	            	metadrset.addRow( en.next() );
	            }
	            debug("Total Result Set: " + metadrset);
	            debug("Fields: " + m_numFields);
	            
	            for (int i = 0; i < m_numFields; i++) {
	                String fieldName = (String)m_fields.get(i);
	                debug("looping over groups names: " + fieldName);
	                DataResultSet drset = new DataResultSet(new String[] {
	                        "drillDownOptionValue", "drillDownModifier", "count", "fieldName"  });
	                drset.copySimpleFiltered(metadrset, "fieldName", fieldName);		  
	                
	              
	                debug("Drill Down Result Set: " + drset);
	                try {
	                    ResultSetUtils.sortResultSet(drset, new String[] {"drillDownOptionValue"});
	                }
	                catch (DataException e) {
	                    Report.trace("search", null, e);
	                }
	                
                m_binder.addResultSetDirect((new StringBuilder()).append("SearchResultNavigation").append(fieldName).toString(), drset);
               
                Vector<String> row = new Vector<String>();
                row.add(fieldName);
                row.add(m_captions.get(fieldName));
                row.add(String.valueOf(drset.getNumRows()) );
                row.add((new StringBuilder()).append(m_totalCount).toString());
                fields.addRow(row);	    
            }

	            m_binder.addResultSet("SearchResultNavigation", fields);
	            
	            // populate for ontology ids
	     /*       debug("\nMap of hits: " + m_metaHits);
	            if(!m_metaHits.isEmpty()){
	            	DataResultSet ontRS = new DataResultSet(new String[]{"key","ids"});
	            	Vector<String> row = null;
	            	String id, ids;
	            	for(Enumeration<String> it = m_metaHits.keys(); it.hasMoreElements();){
	            		row = new Vector<String>();
	            		id = it.nextElement();
	            		ids = m_metaHits.get(id).toString();
	            		ids = ids.substring(1, ids.length()-1);// remove []
	            		row.add(id); row.add(ids);
	            		ontRS.addRow( row );
	                }
	            	m_binder.addResultSet("OntKeysRS", ontRS);
	            	debug("\n Map result: " + ontRS);
	            }
	            */
        }
     }
     
     private void populateMetaValues( String fieldName, int count, String key){
     //	debug("Populate meta values: fieldName: " + fieldName + ", count="+count+", key = "+ key);
     	if( key.length() == 0)
     		return;
     	//System.out.println("Map before: " + m_map);
     	Vector<String> row = null;
     	int hitsCount = 0;
     	String hitsCountStr = null;
     	String k = key + "_" + fieldName;
 		if( m_map.containsKey(k) ) {	                			
 			row = m_map.get(k);
 			hitsCount = NumberUtils.parseInteger(row.get(2), 0);
 			hitsCountStr = String.valueOf(count + hitsCount) ;
 			debug("found: hit = " + hitsCountStr);
 			row.set(2, hitsCountStr);
 		} else {
 			row = new Vector<String>();
 			row.add(key);
             row.add(key);
             hitsCountStr = String.valueOf(count);
             row.add(hitsCountStr);
             row.add(fieldName);
             m_map.put(k, row);
 		}
 		
     }
     
     private void populateHitsIds( String key, int count) {
     //	debug("Populating hits: " + key + ", " + count + ", " + m_hitId);
     	if( key.length() == 0)
     		return;
     	Vector<String> row = null;
     	if(m_metaHits.containsKey(key)){
     		row = m_metaHits.get(key);
     	}else {
     		row = new Vector<String>();
     		m_metaHits.put(key, row);
     	}
     	row.add("id" + String.valueOf(this.m_hitId));
     	row.add(String.valueOf(count));
     }
     

     public void endPrefixMapping(String s) throws SAXException {
     }

     public void ignorableWhitespace(char ac[], int i, int j) throws SAXException {
     }

     public void processingInstruction(String s, String s1) throws SAXException {
     }

     public void setDocumentLocator(Locator locator1) {
     }

     public void skippedEntity(String s) throws SAXException {
     }

     public void startDocument() throws SAXException {
     }

     public void startElement(String uri, String localName, String name, Attributes atts) throws SAXException {
    //	 debug("startElement() " + localName + ", name: " + name + ", atts: " + atts);
    	 if (localName.equals("groups")) {
    		 m_group_name = atts.getValue("sdata").substring(2);
    		 m_group_name = getFieldId(m_group_name);
    		 //m_fields.add(m_group_name  );
    		 //m_numFields = m_fields.size();
    	 } else if (localName.equals("count")) {
	            m_isInCount = true;
         } else if (localName.equals("group")) {
	            m_value = atts.getValue("value");
         }
     }

     public void startPrefixMapping(String s, String s1) throws SAXException {
     }

     public static Object idcVersionInfo(Object arg) {
	        return "releaseInfo=dev,releaseRevision=$Rev: 64561 $";
     }
     
     
     //new function for 11g
     private String getFieldId(String upperCaseId){
    	 String key = null;
    	 int count = m_fields.size();
    	 for(int i=0;i<count;i++) {
    		 key = m_fields.get(i);
    		 if( key.equalsIgnoreCase(upperCaseId)){
    			 debug("getFieldId: " + upperCaseId + " = " + key);
    			 return key;
    		 }
    	 }
    	 debug("getFieldId: not found: " + upperCaseId);
    	 return upperCaseId;
     }
     
     /*
     private String getFieldId(String upperCaseId){
    	 String key = null;
    	 for( Iterator<String> keys = m_captions.keySet().iterator(); keys.hasNext();) {
    		 key = keys.next();
    		 if( key.equalsIgnoreCase(upperCaseId)){
    		//	 debug("getFieldId: " + upperCaseId + " = " + key);
    			 return key;
    		 }
    	 }
    	// debug("getFieldId: not found: " + upperCaseId);
    	 return upperCaseId;
     }*/
     
     public static void debug(String message) {
 		SystemUtils.trace("drilldown",  message);
 	//	System.out.println(message);
 	}

}
