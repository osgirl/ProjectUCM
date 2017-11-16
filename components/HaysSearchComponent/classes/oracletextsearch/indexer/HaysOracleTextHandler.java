package oracletextsearch.indexer;

import intradoc.common.IdcStringBuilder;
import intradoc.common.Report;
import intradoc.common.ServiceException;
import intradoc.common.StringUtils;
import intradoc.common.SystemUtils;
import intradoc.data.DataBinder;
import intradoc.data.DataException;
import intradoc.data.DataResultSet;
import intradoc.data.FieldInfo;
import intradoc.data.ResultSet;
import intradoc.data.ResultSetUtils;
import intradoc.indexer.IndexerInfo;
import intradoc.indexer.OracleTextHandler;
import intradoc.indexer.OracleTextUtils;
import intradoc.shared.PluginFilters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

public class HaysOracleTextHandler extends OracleTextHandler {
	
    public void prepareIndexDoc(Properties prop, IndexerInfo ii) {
    	Report.trace("indexer", "\nprepareIndexDoc() " + prop, null);
        ii.m_indexStatus = 5;
        loadMetaFields();
        String fileName = prop.getProperty("DOC_FN");
        prop.put("otsContent", fileName);
        String dDocName = prop.getProperty("dDocName");
        if (dDocName != null) {
            prop.put("dDocName", dDocName.toUpperCase());
        }
        if (!ii.m_isDelete) {
            int size = m_dateFields.size();
            for (int i = 0; i < size; i++) {
                String key = (String)m_dateFields.get(i);
                String date = (String)prop.get(key);
                if (date == null || date.length() == 0) {
                    continue;
                }
                try {
                    date = fixDate(date);
                }
                catch (ServiceException e) {
                    SystemUtils.dumpException("indexer", e);
                }
                prop.put(key, date);
            }
            //updated for 11g
            //IdcStringBuilder metaValue = HaysOracleTextUtils.buildOtsMeta(prop, m_textSdataFields, m_drillDownFields, m_data.m_collectionDef.m_securityInfos, false, m_defaultNullValue, m_defaultTrueValue);
            IdcStringBuilder drillDownTag = HaysOracleTextUtils.constructDrillDownTag(prop, this.m_drillDownFields, this.m_idxWorkspace, this.m_data.m_workspace, null, this.m_data, this.m_config);
            
            IdcStringBuilder metaValue = HaysOracleTextUtils.buildOtsMeta(prop, this.m_textSdataFields, drillDownTag, this.m_data.m_collectionDef.m_securityInfos, false, this.m_defaultNullValue, this.m_defaultTrueValue, this.m_maxSdataSize);
            
            FieldInfo arr$[] = m_metaFields;
            int len$ = arr$.length;
            for (int i$ = 0; i$ < len$; i$++) {
                FieldInfo fi = arr$[i$];
                if (fi.m_type != 6) {
                    continue;
                }
                String value = prop.getProperty(fi.m_name);
                if (value != null && value.length() == 0) {
                    prop.put(fi.m_name, m_defaultNullValue);
                    continue;
                }
                if (value != null && value.length() > m_maxSize) {
                    value = value.substring(0, m_maxSize);
                    prop.put(fi.m_name, value);
                }
            }
            
            //updated for 11g - removed if condition present here
            Object[] objs = { metaValue, prop, this.m_idxWorkspace, this.m_config };
            this.m_data.setCachedObject("OtsMetaValueObjs", objs);
            try
            {
            	PluginFilters.filter("IndexingOtsMetaValueFilter", this.m_data.m_workspace, null, this.m_data);
            }
            catch (Exception ignore)
            {
            	Report.trace("indexer", null, ignore);
            }
            
            prop.put("otsMeta", metaValue.toString());
        }
    }
    /*
    protected void retrieveUpdatableRows(String collectionID, ArrayList inserts, ArrayList updates, HashMap map) throws ServiceException, DataException {
    	Report.trace("indexer", "\nretrieveUpdatableRows() " + collectionID, null);
        IdcStringBuilder builder = new IdcStringBuilder();
        boolean isFirst = true;
        Iterator i$ = map.entrySet().iterator();
        do {
            if (!i$.hasNext()) {
                break;
            }
            Object obj = i$.next();
            IndexerInfo ii = (IndexerInfo)((java.util.Map.Entry)obj).getValue();
            if (!isFirst) {
                builder.append(',');
            }
            if (ii.m_indexKey != null && ii.m_indexKey.trim().length() > 0) {
                isFirst = false;
                String value = StringUtils.addEscapeChars(ii.m_indexKey, ',', '^');
           //     builder.append(value.toUpperCase());
                builder.append(value);
            }
        } while (true);
        ResultSet result = new DataResultSet();
        if (!isFirst) {
            DataBinder binder = new DataBinder();
            String tableName = getTableName(collectionID);
            binder.putLocal("tableName", tableName);
            binder.putLocal("docNameList", builder.toString());
            result = m_idxWorkspace.createResultSet("QdocumentUpdatable", binder);
        }
        for (; result.isRowPresent(); result.next()) {
            String docName = ResultSetUtils.getValue(result, "dDocName");
            IndexerInfo ii = (IndexerInfo)map.remove(docName.toLowerCase());
            updates.add(ii);
        }

        Object entry;
        for (Iterator iter = map.entrySet().iterator(); iter.hasNext(); inserts.add(((java.util.Map.Entry)entry).getValue())) {
            entry = iter.next();
        }

    }
*/
}
