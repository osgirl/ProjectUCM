package infomentum.metadata;



import intradoc.common.ExecutionContext;
import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.data.DataBinder;
import intradoc.data.DataException;
import intradoc.data.ResultSet;
import intradoc.data.Workspace;
import intradoc.indexer.IndexerState;
import intradoc.indexer.WebChange;
import intradoc.shared.FilterImplementor;
import intradoc.shared.SharedObjects;

public class ManageComplexMetadataFilter implements FilterImplementor  {
	
	public static final String COMPLEX_METADATA = SharedObjects.getEnvironmentValue("complexMetadataFields");
	public static final String DELIMETER = ";";
	
	public static String[] COMPLEX_METADATA_ARRAY = null;
	static {
		if( COMPLEX_METADATA != null ) {
			COMPLEX_METADATA_ARRAY = COMPLEX_METADATA.split(",");
		}
	}
	
	public static final String TABLENAME_EXTENSION = "COMPLEXMETA_";
	
	private Workspace m_ws = null;
	private DataBinder m_binder = null;
	private ExecutionContext m_ctx = null;
	
	/**
	 * This Filter is 'hooked' to 'afterLoadRecordWebChange' event when indexing of content item is completed.
	 * It will pick up update, check in, delete, release from WF and expire actions - any actions
	 * that cause indexer to run
	 */
	public int doFilter(Workspace ws, DataBinder binder, ExecutionContext ctx)	throws DataException, ServiceException
	{		
		this.m_ctx = ctx;
		this.m_binder = binder;
		this.m_ws = ws;
	
		String filterParam = (String)ctx.getCachedObject("filterParameter");
		
		if("afterLoadRecordWebChange".equals(filterParam)) {
			IndexerState indexerState = (IndexerState)m_ctx.getCachedObject("IndexerState");
			if (indexerState.isRebuild() || indexerState.m_isRestart) {
				return CONTINUE;
			}
			afterLoadRecordWebChange();
		} 
			
		return CONTINUE;	
	}
	
	
	/**
	 * Executed during indexing of deletions, additions, or retries. This will fire after a web file.
	 * If it returns ABORT, no idexing of the content item is done. 
	 * Execution Context: intradoc.indexer.IndexerWorkObject
	 * Cached Objects: 
	 * 		intradoc.data.DataBinder WebChangeFilterParams, 
	 * 		intradoc.indexer.IndexerInfo FilterIndexerInfo, 
	 * 		intradoc.indexer.IndexerBulkLoader IndexerBulkLoader, 
	 * 		intradoc.indexer.WebChange WebChange, 
	 * 		intradoc.data.ResultSet DemotedRevisionInfo
	 * 
	 * Use this method to deal with events: update, check in, delete, expire, release
	 */
	private void afterLoadRecordWebChange() throws DataException {
		debug("ManageComplexMetadataFilter.afterLoadRecordWebChange() starting...");
		
		if( COMPLEX_METADATA_ARRAY == null) 
			return;
		
		ResultSet docinfoRS = m_binder.getResultSet("DOC_INFO");
		if(docinfoRS == null || !docinfoRS.first())
			return;		
		
		WebChange webChange = (WebChange)m_ctx.getCachedObject("WebChange");
		boolean toInsert = false;
		if( '+' == webChange.m_change ) {
			toInsert = true;
		}
		// remove all previous values for this content name in Metadata table
		updateComplexMetadata( docinfoRS, toInsert);
	}
	
	private void updateComplexMetadata(ResultSet docInfoRS, boolean toInsert) throws DataException {
		debug("updateComplexMetadata(): " + docInfoRS + ", " + toInsert);
		String metaName, metaValue, tblName = null;
		String dID = docInfoRS.getStringValueByName("dID");
		String dDocName = docInfoRS.getStringValueByName("dDocName");
		for(int i=0; i < COMPLEX_METADATA_ARRAY.length; i++) {
			metaName = COMPLEX_METADATA_ARRAY[i];
			tblName = TABLENAME_EXTENSION + metaName.toUpperCase();
			removeOldMetaValues(tblName, dDocName);
			if( toInsert) {
				metaValue = docInfoRS.getStringValueByName(metaName);
				debug("metaValue: " + metaValue + ", " + metaName);
				if( metaValue != null && metaValue.trim().length() > 0)
					insertComplexMetadata(metaValue, dID, dDocName, tblName);
			}
		}
	}
	
	private void insertComplexMetadata(String value, String dID, String dDocName, String tblName) throws DataException {
		if( value.startsWith(DELIMETER))
			value = value.substring(1);
		if( value.endsWith(DELIMETER))
			value = value.substring(0, value.length()-1);
		String[] list = value.split(DELIMETER);
		
		StringBuffer query = new StringBuffer("INSERT INTO ").append(tblName).append(" (dID,dDocname,metaTerm) ");
		StringBuffer row = null;
		for(int i =0; i< list.length; i++) {
			row = new StringBuffer(" SELECT '");
			row.append(dID).append("','").append(dDocName).append("','").append(list[i]).append("' FROM DUAL UNION ALL ");		
			query.append(row);
		}
		String queryStr = query.substring(0, query.lastIndexOf("UNION"));
		debug(" Insert complex metadata query: " + queryStr);		
		
		try {
			m_ws.executeSQL(queryStr);
		}catch(Exception ex){
			debug("Insert new complex metadata failed: " + ex);
		}
		
	}
	
	/**
	 * Clears old values related to the given content item.
	 * @param dDocName
	 * @throws DataException
	 */
	private void removeOldMetaValues(String tblName, String dDocName)  throws DataException {
		debug("removeOldValues for complex metadata() starting... for " + tblName+ ", " + dDocName);
		String query = "DcomlexMetadata";
		DataBinder db = new DataBinder();
		db.putLocal("tblName", tblName);
		db.putLocal("dDocName", dDocName);
		long l = m_ws.execute(query, db);	
		debug(" Delete query for " + dDocName + ": row removed " + l);
	}

	public static void debug(String message) {
		SystemUtils.trace("complex_metadata",  message);
	//	System.out.println(message);
	}

	public static void debug(Exception ex) {
		SystemUtils.trace("complex_metadata", "\nException :" + ex);
		ex.printStackTrace();
	}

}
