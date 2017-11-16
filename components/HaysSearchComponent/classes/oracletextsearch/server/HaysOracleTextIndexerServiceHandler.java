package oracletextsearch.server;


import intradoc.common.LocaleResources;
import intradoc.common.ServiceException;
import intradoc.data.DataException;
import intradoc.data.DataResultSet;
import intradoc.data.FieldInfo;
import intradoc.data.ResultSet;
import intradoc.data.ResultSetUtils;
import intradoc.shared.SharedObjects;

import java.util.Iterator;
import java.util.Vector;

//updated for 11g - commented
//import oracletextsearch.server.OracleTextIndexerServiceHandler;

public class HaysOracleTextIndexerServiceHandler extends OracleTextIndexerServiceHandler{
	
	/**
	 * Adds ontology-based metadata to the list of metadata fields for OracleText optimisation
	 * "dName", "label", "labelKey"
	 * @throws ServiceException
	 * @throws DataException
	 */
	public void getOracleTextAdminStatus() throws ServiceException, DataException {
		//System.out.println("getOracleTextAdminStatus()...");
		super.getOracleTextAdminStatus();
		DataResultSet customFields = (DataResultSet)m_binder.getResultSet("CustomFields");
		ResultSet docMetaDef = SharedObjects.getTable("DocMetaDefinition");
		Iterator i$ = m_collectionDef.m_fieldInfos.entrySet().iterator();
		Vector<String> row = null;
        do {
            java.util.Map.Entry entry = (java.util.Map.Entry)i$.next();
            FieldInfo fi = (FieldInfo)entry.getValue();
           
            if (fi.m_name.startsWith("x")) {
                String type = ResultSetUtils.findValue(docMetaDef, "dName", fi.m_name, "dType");
                if (type == null || type.equalsIgnoreCase("Memo")) {
                    String caption = ResultSetUtils.findValue(docMetaDef, "dName", fi.m_name, "dCaption");
                    row = new Vector<String>();
                    row.add(fi.m_name);
                    if (caption != null) {
                        row.add(LocaleResources.getString(caption, m_context));
                        row.add(caption);
                    }
                    customFields.addRow(row);
                }
            } 
        } while (i$.hasNext());
	}

}
