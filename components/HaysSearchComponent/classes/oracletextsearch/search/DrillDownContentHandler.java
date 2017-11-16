// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(5) braces fieldsfirst noctor nonlb space lnc 
// Source File Name:   DrillDownContentHandler.java

package oracletextsearch.search;



import intradoc.common.NumberUtils;
import intradoc.common.Report;
import intradoc.common.StringUtils;
import intradoc.common.SystemUtils;
import intradoc.data.DataBinder;
import intradoc.data.DataException;
import intradoc.data.DataResultSet;
import intradoc.data.ResultSet;
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

public class DrillDownContentHandler
    implements ContentHandler {

            protected DataBinder m_binder;
            public String m_value;
            public String m_count;
            protected boolean m_isInCount;
            protected List m_navRSets;
            protected List<String> m_fields;
            protected Map<String,String> m_captions;
            protected int m_numFields;
            protected char m_fieldSeparator;
            protected int m_totalCount;
            public int m_numGroups;
            private Hashtable<String, Vector<String>> m_map = new Hashtable<String, Vector<String>>();
            private Hashtable<String,Vector<String>> m_metaHits = new Hashtable<String,Vector<String>>();
            private int m_hitId = 0;

            public DrillDownContentHandler() {
		        m_binder = null;
		        m_value = null;
		        m_count = null;
		        m_isInCount = false;
		        m_navRSets = new ArrayList();
		        m_fields = null;
		        m_captions = null;
		        m_numFields = 0;
		        m_totalCount = 0;
		        m_numGroups = 0;
            }

            public void init(DataBinder binder, List<String> fields, Map<String,String> captions, char fieldSeparator) {
		        m_binder = binder;
		        m_fields = fields;
		        m_captions = captions;
		        m_fieldSeparator = fieldSeparator;
		        if (m_fields != null) {
		            m_numFields = m_fields.size();
                }
		    
		        
            }

            public void characters(char ch[], int start, int length) throws SAXException {
		        if (m_isInCount) {
		            m_count = new String(ch, start, length);
                }
            }

            public void endDocument() throws SAXException {
            	//System.out.println("total hits count: " + m_totalCount);
		        if (m_totalCount > 0) {
		            DataResultSet fields = new DataResultSet(new String[] {
		                "drillDownFieldName", "drillDownDisplayValue", "categoryCount", "totalCount"
                    });
		            
		            DataResultSet metadrset = new DataResultSet(new String[] {
	                        "drillDownOptionValue", "drillDownModifier", "count", "fieldName"  });
		            for(Iterator<Vector<String>> en = m_map.values().iterator(); en.hasNext();){
		            	metadrset.addRow( en.next() );
	                }
		          //  System.out.println("Total Result Set: " + metadrset);
		            
		            for (int i = 0; i < m_numFields; i++) {
		                String fieldName = (String)m_fields.get(i);
		                DataResultSet drset = new DataResultSet(new String[] {
		                        "drillDownOptionValue", "drillDownModifier", "count", "fieldName"  });
		                drset.copySimpleFiltered(metadrset, "fieldName", fieldName);		  
		                
		              
		             //   System.out.println("Drill Down Result Set: " + drset);
		                try {
		                    ResultSetUtils.sortResultSet(drset, new String[] {
		                        "drillDownOptionValue"
                            });
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
		            debug("\nMap of hits: " + m_metaHits);
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
                }
            }

            public void endElement(String uri, String localName, String name) throws SAXException {
             
		        if (localName.equals("count")) {
		            m_isInCount = false;
                } else
		        if (localName.equals("group")) {		            
	                Pattern regexOntValues = Pattern.compile(";?(.+?);");
	                Matcher ontValuesMatcher = null;
	                String key, subKey = null;	
	                boolean isSet = false;
		                
		            if (m_value != null && m_count != null) {
		            	 int count = NumberUtils.parseInteger(m_count, 0);
			             m_totalCount += count;
			            List<String> keyList = StringUtils.makeListFromSequence(m_value, m_fieldSeparator, '^', 0);
			            debug("endElement() KeyList: " + keyList);
			            
			            if (keyList.size() == m_numFields) {
			                 for (int i = 0; i < m_numFields; i++) {
			                	 key = (String)keyList.get(i);
			                     if ("idcnull".equals(key)) {
			                            key = "";
	                             }	               
	                	 	
		                		// if it's ontology value than extract each key values
		                		ontValuesMatcher = regexOntValues.matcher(key);
		                		m_hitId++;
		                		while (ontValuesMatcher.find()) {
		                			debug("Process multi-values: " + m_fields.get(i));
		                			if( ontValuesMatcher.groupCount() > 0) {
		                				subKey = ontValuesMatcher.group(1);
		                				populateMetaValues(m_fields.get(i), count, subKey);
		                				populateHitsIds(subKey, count);
		                			}
		                			isSet = true;
		                		}
		                		if( !isSet) {
		                			debug("Process single value: " + m_fields.get(i));
		                			populateMetaValues( m_fields.get(i), count, key);
		                			populateHitsIds(key, count);
		                		}
		                		isSet = false;
			                 }
			          
                        }
                    }
		            m_numGroups++;
		            m_value = null;
		            m_count = null;
                }
            }
            
            private void populateMetaValues( String fieldName, int count, String key){
            	debug("Populate meta values: fieldName: " + fieldName + ", count="+count+", key = "+ key);
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
            	debug("Populating hits: " + key + ", " + count + ", " + m_hitId);
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
		        if (localName.equals("count")) {
		            m_isInCount = true;
                } else
		        if (localName.equals("group")) {
		            m_value = atts.getValue("value");
                }
            }

            public void startPrefixMapping(String s, String s1) throws SAXException {
            }

            public static Object idcVersionInfo(Object arg) {
		        return "releaseInfo=dev,releaseRevision=$Rev: 64561 $";
            }
            
            public static void debug(String message) {
        		SystemUtils.trace("drilldown",  message);
        	//	System.out.println(message);
        	}
}
