// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(5) braces fieldsfirst noctor nonlb space lnc 
// Source File Name:   OracleTextSearchImplementor.java

package oracletextsearch.search;  

import hays.co.uk.search.IHaysSearchConstants;
import intradoc.common.*;
import intradoc.data.*;
import intradoc.provider.Provider;
import intradoc.provider.Providers;
import intradoc.search.CommonSearchAdaptor;
import intradoc.search.CommonSearchConnection;
import intradoc.search.OracleTextSearchImplementor;
import intradoc.server.Service;
import intradoc.shared.CommonSearchConfig;
import intradoc.shared.SharedObjects;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;
import intradoc.search.DrillDownContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

// Referenced classes of package oracletextsearch.search:
//            DrillDownContentHandler

public class HaysOracleTextSearchImplementor extends OracleTextSearchImplementor {

         

            private String m_defaultColumns1;

			public HaysOracleTextSearchImplementor() {
		      super();
            }
/*
            public void init(CommonSearchConnection sc) {
		        m_csConn = sc;
            }

            public boolean prepareUse(ExecutionContext ctxt) {
		        if (ctxt == null) {
		            return false;
                }
		        super.prepareUse(ctxt);
		        String providerName = m_csConn.m_queryConfig.getEngineValue("IndexerDatabaseProviderName");
		        Workspace ws = null;
		        if (providerName != null && providerName.length() != 0 && !providerName.equalsIgnoreCase("systemdatabase")) {
		            Provider prov = Providers.getProvider(providerName);
		            if (prov != null && prov.checkState("IsStarted", false)) {
		                ws = (Workspace)prov.getProvider();
		                m_releaseConnectionNeeded = true;
                    }
                } else {
		            ws = (Workspace)ctxt.getCachedObject("Workspace");
		            if (ws == null && (ctxt instanceof Service)) {
		                ws = ((Service)ctxt).getWorkspace();
                    }
                }
		        if (ws == null) {
		            return false;
                }
		        m_workspace = ws;
		        if (m_allowConfigChangeEachTime) {
		            m_tagSet = m_csConn.m_queryConfig.getEngineValue("OracleTextHighlightType");
		            String maxSize = m_csConn.m_queryConfig.getEngineValue("OracleTextHighlightMaxDocSize");
		            if (maxSize != null) {
		                m_maxDocSize = NumberUtils.parseInteger(maxSize, 0x4c4b40);
                    }
		            m_startTag = m_csConn.m_queryConfig.getEngineValue("OracleHighlightStartTag");
		            m_endTag = m_csConn.m_queryConfig.getEngineValue("OracleHighlightEndTag");
		            m_prevTag = m_csConn.m_queryConfig.getEngineValue("OracleHighlightPrevTag");
		            m_nextTag = m_csConn.m_queryConfig.getEngineValue("OracleHighlightNextTag");
		            String snippet = m_csConn.m_queryConfig.getEngineValue("OracleTextDisableSearchSnippet");
		            boolean enableDBSnippet = !StringUtils.convertToBool(snippet, false);
		            m_flags = enableDBSnippet ? 2 : 0;
		            String drillDownFields = SharedObjects.getEnvironmentValue("DrillDownFields");
		            if (drillDownFields == null) {
		                drillDownFields = "dDocType,dSecurityGroup,dDocAccount";
                    }
		            m_drillDownFields = StringUtils.makeListFromSequence(drillDownFields, ',', '^', 32);
		            m_numDrillDownFields = m_drillDownFields.size();
		            DataResultSet docMetaDef = SharedObjects.getTable("DocMetaDefinition");
		            m_drillDownFieldCaptions = new HashMap();
                    String name;
                    String caption;
		            for (Iterator i$ = m_drillDownFields.iterator(); i$.hasNext(); m_drillDownFieldCaptions.put(name, caption)) {
		                name = (String)i$.next();
		                caption = null;
		                if (docMetaDef != null) {
		                    try {
		                        caption = ResultSetUtils.findValue(docMetaDef, "dName", name, "dCaption");
                            }
		                    catch (Exception e) { }
                        }
		                if (caption == null) {
		                    caption = (new StringBuilder()).append("ww").append(name).toString();
                        }
		                Report.trace("search", "Populate drill down Captions: " + name + " = " + caption, null);
                    }

		            m_disableSAXParsing = StringUtils.convertToBool(m_csConn.m_queryConfig.getEngineValue("DisableDrillDownParsingWithSAX"), false);
		            String allowChangeEachTime = m_csConn.m_queryConfig.getEngineValue("AllowConfigChangeEachTime");
		            m_allowConfigChangeEachTime = StringUtils.convertToBool(allowChangeEachTime, false);
                }
		        return true;
            }
*/
            
            public String doQuery(DataBinder binder) {
                
                String queryCollection;
                String tableName;
                DataBinder rBinder;
		        int flag = m_flags;
		        
		       
		        if (SystemUtils.isActiveTrace("search")) {
		            flag |= 1;
                }
		        binder.putLocal("procFlags", (new StringBuilder()).append("").append(flag).toString());
		        SystemUtils.trace("hays_search","ismobile value is ="+ binder.getLocal("isMobile"));
		        String queryText = binder.getLocal("WhereClause");
		        String querySelection=binder.getLocal("QuerySelection");
		        String parsedQueryTextNoSecurity = binder.getLocal("ParsedQueryTextNoSecurity");
		        
		        if ((queryText == null) || (queryText.trim().length() == 0))
		        {
		        	queryText = binder.getLocal("QueryText");
		        	if ((queryText == null) || (queryText.trim().length() == 0))
			        {
		        		queryText = "idccontenttrue";
			        }
		        }
		        binder.putLocal("queryText", queryText);
		        
		        if ((parsedQueryTextNoSecurity == null) || (parsedQueryTextNoSecurity.trim().length() == 0))
		        {
		        	parsedQueryTextNoSecurity = "idccontenttrue";
		        }
		        binder.putLocal("parsedQueryTextNoSecurity", parsedQueryTextNoSecurity);
	        
		        
		        //Remove securityGroup HaysSecure in QueryText
		        String securityFilter=binder.getLocal("CommonSearchSecurityFilter");
		        if(securityFilter!=null && securityFilter.length()>0)
		        {
			        securityFilter="and  (("+securityFilter+"))";
			        SystemUtils.trace("hays_search","CommonSearchSecurityFilter ="+ securityFilter);
			        binder.removeLocal("CommonSearchSecurityFilter");
			        SystemUtils.trace("hays_search","Search Quert Text Before  removing CommonSearchSecurityFilter="+ queryText);
			        queryText=queryText.replace(securityFilter, "");
			        queryText=queryText.trim();
			        SystemUtils.trace("hays_search","queryText.trim()"+ queryText);
			        SystemUtils.trace("hays_search","Search Quert Text After  removing CommonSearchSecurityFilter="+ queryText);
			        binder.putLocal("QueryText", queryText);
		        }		        	        
		        SystemUtils.trace("hays_search","Final queryText for CotsSearchQuery ="+ queryText);
		       //End Remove securityGroup HaysSecure in QueryText
			     
		        String querySelectionForApi="";
		        
		        if (binder.getLocal("isMobile")!=null && binder.getLocal("isMobile").equals("Y") || (binder.getLocal("isWC")!=null && binder.getLocal("isWC").equals("Y")))
		        {
		        	String[] groupAarray = querySelection.split("<.group>");					
					for(int i = 0; i < groupAarray.length; i++) {
						
						if(i==0)
						{
							querySelectionForApi=groupAarray[i];
							int gindex=querySelectionForApi.indexOf("/>");
							querySelectionForApi=querySelectionForApi.substring(0, gindex+2);
							SystemUtils.trace("hays_search","Hays Api querySelection String1 ="+ querySelectionForApi);
							
						}
						if(i==groupAarray.length-1)
						{
							querySelectionForApi=querySelectionForApi+groupAarray[i];
							SystemUtils.trace("hays_search","Hays Api querySelection String2="+ querySelectionForApi);
						}
						
					}
					querySelectionForApi=querySelectionForApi.replace("false", "true");					
					querySelection=querySelectionForApi;
		        	SystemUtils.trace("hays_search","Hays Api querySelection ="+ querySelection);
		        	binder.putLocal("resultDescriptor", querySelection);
		        }
		        else
		        {		        	
		            SystemUtils.trace("hays_search","querySelection ="+ querySelection);
		        	binder.putLocal("resultDescriptor", querySelection);
		        }
		        
		        queryCollection = binder.getLocal("QueryCollection");
		        tableName = "IdcText2";
		        String indexName = "FT_IdcText2";
		        if (queryCollection == null || queryCollection.equalsIgnoreCase("ots1")) {
		            tableName = "IdcText1";
		            indexName = "FT_IdcText1";
                }
		        binder.putLocal("tableName", tableName);
		        binder.putLocal("indexName", indexName);
		        rBinder = new DataBinder();
		        
		        SystemUtils.trace("hays_search", "Query Collection :"+queryCollection);
		        SystemUtils.trace("hays_search", "Collection Name :"+m_collectionName);
		        SystemUtils.trace("hays_search", "m_defaultColumns :"+m_defaultColumns);
		        
		        CallableResults results = null;
		        boolean isError = false;
		        try {
		        	 SystemUtils.trace("hays_search", "Binder value for isMobile : "+binder.getLocal("isMobile"));
		        	 if (queryCollection != null && (m_collectionName == null || !m_collectionName.equalsIgnoreCase(queryCollection)) 
		        			 || m_collectionName == null) {
	        			 String columnArr[] = WorkspaceUtils.getColumnList(tableName, m_workspace, new String[] {
			                "otsContent", "otsMeta", "otsCounter"
	                    });		             
			            
			            	 m_defaultColumns = StringUtils.createString(StringUtils.convertToList(columnArr), ',', '^');
			            	 
			            	 SystemUtils.trace("hays_search", "Column values for General Search :"+m_defaultColumns);	            
			            
			           
			            m_collectionName = queryCollection;
			            SystemUtils.trace("hays_search", "Column values11 :"+m_defaultColumns);
		        		SystemUtils.trace("hays_search", "Collection Name11 :"+m_collectionName);
	                }
	        		 binder.putLocal("returnFields", m_defaultColumns);
	        		 
		        	 /*if(binder.getLocal("isMobile")==null)
		        	 {	
		        		 SystemUtils.trace("hays_search", "first if :");	
		        		
		        		 if (queryCollection != null && (m_collectionName == null || !m_collectionName.equalsIgnoreCase(queryCollection)) || m_collectionName == null) {
		        			 String columnArr[] = WorkspaceUtils.getColumnList(tableName, m_workspace, new String[] {
				                "otsContent", "otsMeta", "otsCounter"
		                    });		             
				            
				            	 m_defaultColumns = StringUtils.createString(StringUtils.convertToList(columnArr), ',', '^');
				            	 
				            	 SystemUtils.trace("hays_search", "Column values for General Search :"+m_defaultColumns);	            
				            
				           
				            m_collectionName = queryCollection;
				            SystemUtils.trace("hays_search", "Column values11 :"+m_defaultColumns);
			        		SystemUtils.trace("hays_search", "Collection Name11 :"+m_collectionName);
		                }
		        		 binder.putLocal("returnFields", m_defaultColumns);
		        	 }
		        	 else
			        {	
			        	 SystemUtils.trace("hays_search", "else : ");	
			        	 SystemUtils.trace("hays_search", "Column values12 : "+m_defaultColumns);
		        		 SystemUtils.trace("hays_search", "Collection Name12 : "+m_collectionName);
				        //if (binder.getLocal("isMobile")!=null && binder.getLocal("isMobile").equals("Y"))
			           //  {
			            	 String m_defaultColumns_mobile = "dDocTitle,dDocName,dID,dRevLabel,xLocationDescription,xSponsored,xJobType,xLocation,dInDate,xEventDate,xHaysLocation1,xHaysLocation2," +
			            	 		"xHaysLocation3,xHaysLocation4,xHaysLocation5,xHaysLocation6"; 
			            	 
			            	 SystemUtils.trace("hays_search", "Column values for Hays API :"+m_defaultColumns_mobile);
			            	 binder.putLocal("returnFields", m_defaultColumns_mobile);
			           //  }
			            	 m_defaultColumns = m_defaultColumns_mobile;
			        }*/
		        
		        Report.trace("search", "goQuery(): Binder: " + binder.getLocalData(), null);
		        
		        String latitude = binder.getLocal("ne_latitude");
		        String longitude = binder.getLocal("ne_longitude");
		        System.out.println("ne_latitude "+latitude);
		        if( latitude == null || longitude == null) {
		        	System.out.println("inside if.. ");
		        	binder.putLocal("ne_latitude",IHaysSearchConstants.DEFAULT);
		        	binder.putLocal("ne_longitude",IHaysSearchConstants.DEFAULT);
		        	binder.putLocal("sw_latitude",IHaysSearchConstants.DEFAULT);
		        	binder.putLocal("sw_longitude",IHaysSearchConstants.DEFAULT);
		        	binder.putLocal("radius",IHaysSearchConstants.DEFAULT);
		        	binder.putLocal("exclude","2");
		        }
		        
		        boolean disableSnippetForSystemClause = SharedObjects.getEnvValueAsBoolean("DisableSnippetForSystemClause", false);
		        if (disableSnippetForSystemClause)
		        {
		        	results = this.m_workspace.executeCallable("CotsSearchQueryFullTextSnippet", binder);
		        }
		        else
		        {
		        	results = this.m_workspace.executeCallable("CotsSearchQuery", binder);
		        }

		        
		        //revertAlterSession(); // added for stemming implementation
		        ResultSet rset = (ResultSet)results.getObject("metaResult");
		        ResultSet base = (ResultSet)results.getObject("baseResult");		        
		        
		        
		        rset.setDateFormat(LocaleResources.m_iso8601Format);
		        base.setDateFormat(LocaleResources.m_iso8601Format);
		        int count = results.getInteger("count");
		        DataResultSet drset = assembleResult(base, rset);
		        if (count < drset.getNumRows())
		        {
		        	Report.trace("search", "The number of rows reported by result set interface (" + count + ") was less than actual rows returned (" + drset.getNumRows() + ")-- fixing", null);
		        	count = drset.getNumRows();
		        }

		        /* Removed the code for adding extra fields into resultset from here and placed in HaysApiContentSearchHandler.java as
		         * it was not getting executed for the flow that fetches result from cache. - 11G Upgrade - Ankit Srivastava 
		         */
		        
		        
		        rBinder.putLocal("TotalRows", (new StringBuilder()).append("").append(count).toString());
		        		      
		        rBinder.addResultSet("SearchResults", drset);
		        
		        String rsi = results.getString("result");
		        SystemUtils.trace("search",  "\nQuery Result: " + rsi);
		        
		        String isSimple = binder.getLocal("isSimple");
		        SystemUtils.trace("search",  "is simple search: " + isSimple);
		        if( isSimple == null ) {
		        int beginIndex = rsi.indexOf("<groups ");
			        if (beginIndex >= 0) {
			            int endIndex = rsi.lastIndexOf("</groups>");
			            if (endIndex > beginIndex) {
			                String drillDown = "<root>" + rsi.substring(beginIndex, endIndex + 9) + "</root>";
			             //   System.out.println("\nDrillDown: " + drillDown);
			                if (drillDown != null && drillDown.trim().length() > 0) {
			                    if (m_disableSAXParsing) {
			                        processDrillDownInfoManual(drillDown, rBinder);
	                            } else {
			                        processDrillDownInfoEx(drillDown, rBinder);
			                        
	                            }
	                        }
	                    }
	                }
		        }
		        if (Report.m_verbose) {
		            String trace = results.getString("trace");
		            Report.trace("search", (new StringBuilder()).append("ResultSetInterface results: ").append(rsi).toString(), null);
		            Report.trace("search", (new StringBuilder()).append("Trace: ").append(trace).toString(), null);
                }
		        if (m_releaseConnectionNeeded) {
		            m_workspace.releaseConnection();
                }
		        
		        } catch(Exception t) {  
		        	 isError = true;
		        	 Report.trace("search", (new StringBuilder()).append("Error executing query: \"").append(queryText).append("\"").toString(), t);
		        	 SystemUtils.traceDumpException("hays_search","error caught",t);
		        	 //	rBinder.putLocal("isSearchError", "1");
		        	 String msg = t.getMessage();
		        	 if (msg != null && msg.contains("PLS-00201")) {
		        		 msg = LocaleUtils.encodeMessage("csOracleTextProcedureMayNotDefined", msg, queryText);
		        	 } else {
		        		 msg = LocaleUtils.encodeMessage("csOracleTextErrorExecutingQuery", msg, queryText);
		        	 }
		        	 rBinder.putLocal("StatusMessageKey", msg);
		             rBinder.putLocal("StatusMessage",LocaleUtils.encodeMessage("wwJobSearchResultsErrorMsg",null));
		             rBinder.putLocal("StatusCode", "-32");
		       }
		        if (m_releaseConnectionNeeded) {
		            m_workspace.releaseConnection();
                }
		        
               
		        if (m_releaseConnectionNeeded) {
		            m_workspace.releaseConnection();
                }
		        rBinder.putLocal("queryText", queryText);
		        rBinder.putLocal("returnFields", m_defaultColumns);
		        this.m_resultBinder = rBinder;

		        /*try {
		            StringWriter writer = new StringWriter();
		            rBinder.send(writer);
		            m_result = writer.toString();
		            SystemUtils.trace("hays_search","Inside try no error");
                }
		        catch (IOException e) {
		        	SystemUtils.traceDumpException("hays_search","error caught in 2nd catch",e);
		        }*/
		        SystemUtils.trace("hays_search","Returning null probably error");
		        if (isError)
		        {
		        	return getResult();
		        }
		        return null;
            }
            
            private void revertAlterSession() {
       		 try {
       			 SystemUtils.trace("hays_search", "***************** \t revertAlterSession starting...");
       			 m_workspace.beginTran();
       			 long l =m_workspace.executeSQL("ALTER SESSION SET NLS_LANGUAGE=ENGLISH");
       			 m_workspace.commitTran();
       			 System.out.println("Session was updated for English Session language: " + l);
       			 SystemUtils.trace("hays_search", " ************************\t revertAlterSession:::Session was updated for English Language ");
       		 } catch(DataException ex){
       			 ex.printStackTrace();
       		 }
       	 }
       	
  /*          protected void processDrillDownInfoManual(String drillDown, DataBinder binder) throws Exception {
		        Report.trace("search", "Start parsing drill down fields...", null);
		        List navRSets = new ArrayList();
		        for (int i = 0; i < m_numDrillDownFields; i++) {
		            DataResultSet drset = new DataResultSet(new String[] {
		                "drillDownOptionValue", "drillDownModifier", "count", "fieldName"
                    });
		            navRSets.add(drset);
                }

		        int beginIndex = 0;
		        int numGroups = 0;
		        int totalCount = 0;
		        do {
		            if (beginIndex < 0) {
		                break;
                    }
		            beginIndex = drillDown.indexOf("value=\"", beginIndex);
		            if (beginIndex >= 0) {
		                beginIndex += 7;
		                int endIndex = drillDown.indexOf("\"", beginIndex);
		                if (endIndex < 0) {
		                    beginIndex = endIndex;
                        } else {
		                    String value = drillDown.substring(beginIndex, endIndex);
		                    Report.trace("search", "value = " + value, null);
		                    
		                    beginIndex = drillDown.indexOf("<count>", endIndex);
		                    if (beginIndex >= 0) {
		                        beginIndex += 7;
		                        endIndex = drillDown.indexOf('<', beginIndex);
		                        if (endIndex < 0) {
		                            beginIndex = endIndex;
                                } else {
		                            String countStr = drillDown.substring(beginIndex, endIndex);
		                            int count = NumberUtils.parseInteger(countStr, 0);
		                            totalCount += count;
		                            Report.trace("search","total count: " + totalCount, null);
		                            
		                            List keyList = StringUtils.makeListFromSequence(value, m_drillDownFieldSep, '^', 0);
		                            
		                            Report.trace("search", " Key List ; " + keyList, null);
		                            if (keyList.size() == m_numDrillDownFields) {
		                                for (int i = 0; i < m_numDrillDownFields; i++) {
		                                    DataResultSet drset = (DataResultSet)navRSets.get(i);
		                                    String key = (String)keyList.get(i);
		                                    if (key != null && key.equalsIgnoreCase("idcnull")) {
		                                        key = "";
                                            }
		                                    Vector row = drset.findRow(0, key);
		                                    if (row == null) {
		                                        row = new Vector();
		                                        row.add(key);
		                                        row.add(key);
		                                        row.add((new StringBuilder()).append("").append(count).toString());
		                                        row.add((new StringBuilder()).append("").append((String)m_drillDownFields.get(i)).toString());
		                                        Report.trace("search"," Row: " + row, null);
		                                        drset.addRow(row);
                                            } else {
		                                        int tmpCount = count + NumberUtils.parseInteger((String)row.elementAt(2), 0);
		                                        row.set(2, (new StringBuilder()).append("").append(tmpCount).toString());
                                            }
                                        }

                                    }
		                            numGroups++;
                                }
                            }
                        }
                    }
                } while (true);
		        DataResultSet fields = new DataResultSet(new String[] {
		            "drillDownFieldName", "drillDownDisplayValue", "categoryCount", "totalCount"
                });
		        for (int i = 0; i < m_numDrillDownFields; i++) {
		            DataResultSet drset = (DataResultSet)navRSets.get(i);
		            String fieldName = (String)m_drillDownFields.get(i);
		            try {
		                ResultSetUtils.sortResultSet(drset, new String[] {
		                    "drillDownOptionValue"
                        });
                    }
		            catch (DataException e) {
		                Report.trace("search", null, e);
                    }
		            binder.addResultSetDirect((new StringBuilder()).append("SearchResultNavigation").append(fieldName).toString(), drset);
		            Vector row = new Vector();
		            row.add(fieldName);
		            row.add(m_drillDownFieldCaptions.get(fieldName));
		            row.add((new StringBuilder()).append("").append(drset.getNumRows()).toString());
		            row.add((new StringBuilder()).append("").append(totalCount).toString());
		            fields.addRow(row);
                }

		        binder.addResultSet("SearchResultNavigation", fields);
		        Report.trace("search", (new StringBuilder()).append("Completed parsing ").append(numGroups).append(" groups.").toString(), null);
            }
*/	
            	
         /*   protected void processDrillDownInfoEx(String drillDown, DataBinder binder) throws Exception
            {
              Report.trace("search", "Start parsing drill down fields with SAX...", null);

              initDrillDownFields();

              DrillDownContentHandler handler = new DrillDownContentHandler();
              handler.init(binder, this.m_drillDownFields, this.m_drillDownFieldSep);
              XMLReader adapter = XMLReaderFactory.createXMLReader();
              adapter.setContentHandler(handler);
              InputSource source = new InputSource();
              source.setCharacterStream(new StringReader(drillDown));

              adapter.parse(source);

              Report.trace("search", "Completed parsing " + handler.m_numGroups + " groups.", null);
            }*/
            
           
            protected void processDrillDownInfoEx(String drillDown, DataBinder binder) throws Exception {
		        Report.trace("search", "Start parsing drill down fields with SAX...", null);
		        
		        HaysDrillDownContentHandler handler = new HaysDrillDownContentHandler();
		    //    Report.trace("search", "Caption map: " + m_drillDownFieldCaptions, null);
		        
	            //updated for 11g
		        initDrillDownFields();
		        //handler.init(binder, m_drillDownFieldCaptions, m_drillDownFieldSep);
		        handler.init(binder, m_drillDownFields, m_drillDownFieldSep);
		        
		        XMLReader adapter = XMLReaderFactory.createXMLReader();
		        adapter.setContentHandler(handler);
		        InputSource source = new InputSource();
		        source.setCharacterStream(new StringReader(drillDown));
		        adapter.parse(source);
		        Report.trace("search", (new StringBuilder()).append("Completed parsing ").append(handler.m_numGroups).append(" groups.").toString(), null);
            }
/*
            protected DataResultSet assembleResult(ResultSet base, ResultSet meta) throws Exception {
		        DataResultSet baseDrset = new DataResultSet();
		        baseDrset.copyFieldInfo(base);
		        baseDrset.copy(base);
		        HashMap indexMap = buildIndexMap(baseDrset);
		        FieldInfo extraFields[] = findExtraFields(meta, base);
		        ArrayList rows = new ArrayList(baseDrset.getNumRows());
		        int numRows = baseDrset.getNumRows();
		        for (int i = 0; i < numRows; i++) {
		            rows.add(null);
                }

		        FieldInfo fi = new FieldInfo();
		        meta.getFieldInfo("dID", fi);
		        if (fi.m_index < 0) {
		            throw new ServiceException("csSearchResultsContainsNodID");
                }
		        String did = null;
		        for (; meta.isRowPresent(); meta.next()) {
		            did = meta.getStringValue(fi.m_index);
		            Integer index = (Integer)indexMap.remove(did);
		            if (index == null) {
		                Report.trace("search", (new StringBuilder()).append("metaResult and baseResult mismatch. Entry with id of '").append(did).append("' is not found in baseResult.").toString(), null);
		                continue;
                    }
		            Vector row = new Vector();
		            int size = meta.getNumFields();
		            for (int i = 0; i < size; i++) {
		                String value = meta.getStringValue(i);
		                if (value.equals("idcnull")) {
		                    value = "";
                        }
		                row.addElement(value);
                    }

		            for (int i = 0; i < extraFields.length; i++) {
		                baseDrset.setCurrentRow(index.intValue());
		                String value = baseDrset.getStringValue(extraFields[i].m_index);
		                if (value.equals("idcnull")) {
		                    value = "";
                        }
		                row.addElement(value);
                    }

		            rows.set(index.intValue(), row);
                }

		        removeAdditionalRows(rows, indexMap);
		        intradoc.common.IdcDateFormat format = meta.getDateFormat();
		        DataResultSet drset = new DataResultSet();
		        drset.copyFieldInfo(meta);
		        drset.setDateFormat(format);
		        drset.appendFields(new Vector(Arrays.asList(extraFields)));
		        Iterator i$ = rows.iterator();
		        do {
		            if (!i$.hasNext()) {
		                break;
                    }
		            Vector row = (Vector)i$.next();
		            if (row != null) {
		                drset.addRow(row);
                    }
                } while (true);
		        return drset;
            }

            protected void removeAdditionalRows(ArrayList rows, HashMap indexMap) {
		        if (indexMap == null || indexMap.size() == 0) {
		            return;
                }
		
    class _anm1 {};
		        TreeSet tset = new TreeSet(rows);
		        tset.addAll(indexMap.values());
		        Iterator i$ = tset.iterator();
		        do {
		            if (!i$.hasNext()) {
		                break;
                    }
		            Integer index = (Integer)i$.next();
		            Vector row = (Vector)rows.remove(index.intValue());
		            if (row != null && SystemUtils.m_verbose) {
		                Report.trace("search", (new StringBuilder()).append("Removing row:").append(index).toString(), null);
                    }
                } while (true);
            }

            protected FieldInfo[] findExtraFields(ResultSet destRset, ResultSet srcRset) {
		        int numFields = srcRset.getNumFields();
		        ArrayList infos = new ArrayList();
		        for (int i = 0; i < numFields; i++) {
		            FieldInfo fi = new FieldInfo();
		            srcRset.getIndexFieldInfo(i, fi);
		            FieldInfo dfi = new FieldInfo();
		            if (!destRset.getFieldInfo(fi.m_name, dfi)) {
		                infos.add(fi);
                    }
                }

		        FieldInfo finfos[] = new FieldInfo[infos.size()];
		        infos.toArray(finfos);
		        return finfos;
            }

            public HashMap buildIndexMap(DataResultSet drset) {
		        FieldInfo fi = new FieldInfo();
		        drset.getFieldInfo("dID", fi);
		        if (fi.m_index != -1);
		        HashMap indexMap = new HashMap();
		        int counter = 0;
		        String tmp = null;
		        drset.first();
		        while (drset.isRowPresent())  {
		            tmp = drset.getStringValue(fi.m_index);
		            indexMap.put(tmp, new Integer(counter));
		            drset.next();
		            counter++;
                }
		        return indexMap;
            }

            public String getResult() {
		        return m_result;
            }

            public String getString(DataBinder binder) {
		        String result = null;
		        try {
		            StringWriter sw = new StringWriter();
		            binder.send(sw);
		            result = sw.toString();
                }
		        catch (IOException e) {
		            SystemUtils.dumpException("search", e);
                }
		        return result;
            }

            public String retrieveHighlightInfo(DataBinder binder, int hlType, String hlBegin, String hlEnd) {
		        String type = m_workspace.getProperty("DatabaseType");
		        if (!type.equalsIgnoreCase("oracle")) {
		            return super.retrieveHighlightInfo(binder, hlType, hlBegin, hlEnd);
                }
		        try {
		            String textQuery = binder.get("QueryText");
		            String activeIndex = binder.get("ActiveIndex");
		            if (activeIndex == null || activeIndex.equalsIgnoreCase("ots1")) {
		                activeIndex = "FT_IdcText1";
                    } else {
		                activeIndex = "FT_IdcText2";
                    }
		            binder.putLocal("textQuery", textQuery);
		            binder.putLocal("tagSet", m_tagSet);
		            binder.putLocal("ftIndexName", activeIndex);
		            binder.putLocal("hlBegin", m_startTag);
		            binder.putLocal("hlEnd", m_endTag);
		            binder.putLocal("prevTag", m_prevTag);
		            binder.putLocal("nextTag", m_nextTag);
		            CallableResults rset = m_workspace.executeCallable("CoracleTextHighlight", binder);
		            setResult(rset);
                }
		        catch (Exception e) {
		            return CommonSearchConnection.createErrorMsg(e, "csSearchRetrieveHighlightInfoError");
                }
		        return null;
            }

            public void setResult(CallableResults rset) throws DataException, IOException {
		        java.io.Reader reader = rset.getReader("highlightClob");
		        BufferedReader breader = new BufferedReader(reader);
		        IdcStringBuilder buf = new IdcStringBuilder();
		        char cbuf[] = new char[65526];
		        int len = cbuf.length;
		        int total = 0;
		        do {
		            if ((len = breader.read(cbuf, 0, len)) == -1) {
		                break;
                    }
		            buf.append(cbuf, 0, len);
		            total += len;
		            if (total > m_maxDocSize) {
		                break;
                    }
		            if (total + len > m_maxDocSize) {
		                len = m_maxDocSize - total;
                    } else {
		                len = cbuf.length;
                    }
                } while (true);
		        m_result = buf.toString();
            }

            public static Object idcVersionInfo(Object arg) {
		        return "releaseInfo=dev,releaseRevision=$Rev: 64563 $";
            }
           */
}
