package hays.co.uk.metadata;


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

public class ComplexMetadataFilter implements FilterImplementor {

	private Workspace m_ws = null;
	private DataBinder m_binder = null;
	private ExecutionContext m_ctx = null;

	public static final String COMPLEX_META_TBL_EXTENSION = "COMPLEXMETA_";
	public static final String OFFICE_LOCATIONS_TBL = "OFFICE_LOCATIONS";
	static final String SITEMAPTYPES = SharedObjects.getEnvironmentValue("SiteMapIncludeContentTypes");
	static final String SITEMAPMETADATA = SharedObjects.getEnvironmentValue("SiteMapProjectProperty");

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
			SystemUtils.trace("sitemap", "ComplexMetadataFilter called with parameter: afterLoadRecordWebChange");
			IndexerState indexerState = (IndexerState)m_ctx.getCachedObject("IndexerState");
			if (indexerState.isRebuild() || indexerState.m_isRestart) {
				return CONTINUE;
			}
			afterLoadRecordWebChange();
			afterLoadOfficeRecordWebChange();
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

		ResultSet docinfoRS = m_binder.getResultSet("DOC_INFO");
		SystemUtils.trace("sitemap", "Doc Info RS: " + docinfoRS);
		if(docinfoRS == null || !docinfoRS.first())
			return;

		// only proceed for specific dDocType
		String docType = docinfoRS.getStringValueByName("dDocType");
		String metaValue = docinfoRS.getStringValueByName(SITEMAPMETADATA);
		SystemUtils.trace("sitemap", "dDocType = " + docType + ", metaValue: " + metaValue);
		if( docType == null || SITEMAPTYPES.indexOf(docType) < 0 )
			return;

		WebChange webChange = (WebChange)m_ctx.getCachedObject("WebChange");
		// remove all previous values for this content name in Taxonomy Usage table
		if( '+' == webChange.m_change || '-' == webChange.m_change) {
			long l = clearComplexmetadataUsage( docinfoRS.getStringValueByName("dDocName"));
			SystemUtils.trace("sitemap", "deleted rows: " + l);
			SystemUtils.trace("sitemap", "webChange.m_change = " + webChange.m_change );
			if( '+' == webChange.m_change && metaValue != null && metaValue.length() > 0) {
				insertComplexMetadata( docinfoRS );
			}
		}
	}


	private long clearComplexmetadataUsage(String dDocName) throws DataException{
		String query = "DcomlexMetadata";
		DataBinder parameters = new DataBinder();
		parameters.putLocal("tblName", COMPLEX_META_TBL_EXTENSION + SITEMAPMETADATA.toUpperCase());
		parameters.putLocal("dDocName", dDocName);
		return (m_ws.execute(query, parameters));
	}


	private void insertComplexMetadata(ResultSet docinfoRS) throws DataException {
		SystemUtils.trace("sitemap", "insert complex metadata");
		DataBinder parameters = null;
		String query = "IcomplexMetadata";
		String metaValue = docinfoRS.getStringValueByName(SITEMAPMETADATA);
		if(metaValue.length() ==  0)
			return;
		metaValue = metaValue.substring(1, metaValue.length()-1);
		String[] values = metaValue.split(";");
		for(int i = 0; i<values.length; i++) {
			parameters = new DataBinder();
			parameters.putLocal("tblName", COMPLEX_META_TBL_EXTENSION + SITEMAPMETADATA.toUpperCase());
			parameters.putLocal("dID", docinfoRS.getStringValueByName("dID"));
			parameters.putLocal("dDocName", docinfoRS.getStringValueByName("dDocName"));
			parameters.putLocal("dDocType", docinfoRS.getStringValueByName("dDocType"));
			parameters.putLocal("xLocale", docinfoRS.getStringValueByName("xLocale"));
			parameters.putLocal("METATERM", values[i]);
			long l =m_ws.execute(query, parameters);
			SystemUtils.trace("sitemap", "inserted rows : " + l);
		}
	}
	
	
	
	private void afterLoadOfficeRecordWebChange() throws DataException {

		ResultSet docinfoRS = m_binder.getResultSet("DOC_INFO");
		SystemUtils.trace("sitemap", "afterLoadOfficeRecordWebChange binder valueeeee " + m_binder);
		if(docinfoRS == null || !docinfoRS.first())
			return;

		// only proceed for specific dDocType
		String docType = docinfoRS.getStringValueByName("dDocType");
		String did = docinfoRS.getStringValueByName("dID");
		SystemUtils.trace("sitemap", "afterLoadOfficeRecordWebChange did = " + did );
		
		String query = "QOfficeLatLong";
		DataBinder parameters = new DataBinder();
		parameters.putLocal("dID", did);
		ResultSet r = m_ws.createResultSet(query, parameters);
		SystemUtils.trace("sitemap", "afterLoadOfficeRecordWebChange :: resultset r" + r);
		String latitude = r.getStringValueByName("latitude");
		String longitude = r.getStringValueByName("longitude");
		SystemUtils.trace("sitemap", "afterLoadOfficeRecordWebChange dDocType = " + docType );
		SystemUtils.trace("sitemap", "afterLoadOfficeRecordWebChange xLatitude = " + latitude );
		SystemUtils.trace("sitemap", "afterLoadOfficeRecordWebChange xLongitude = " + longitude );
		if(latitude != null && latitude.length() > 1 && longitude != null && longitude.length() > 1)
		{
			WebChange webChange = (WebChange)m_ctx.getCachedObject("WebChange");
			SystemUtils.trace("sitemap", "afterLoadOfficeRecordWebChange webChange webChange: " + webChange);
			// remove all previous values for this content name in Taxonomy Usage table
			if( '+' == webChange.m_change || '-' == webChange.m_change) {
				long l = clearOfficeLocations( docinfoRS.getStringValueByName("dDocName"));
				SystemUtils.trace("sitemap", " afterLoadOfficeRecordWebChange deleted rows: " + l);
				SystemUtils.trace("sitemap", "afterLoadOfficeRecordWebChange webChange.m_change = " + webChange.m_change );
				if( '+' == webChange.m_change ) {
					insertOfficeLocations( docinfoRS , latitude , longitude  );
				}
			}
		}
	}
	
	private long clearOfficeLocations(String dDocName) throws DataException{
		String query = "DofficeLocations";
		DataBinder parameters = new DataBinder();
		parameters.putLocal("tblName", OFFICE_LOCATIONS_TBL.toUpperCase());
		parameters.putLocal("dDocName", dDocName);
		
		return (m_ws.execute(query, parameters));
	}
	
	private void insertOfficeLocations(ResultSet docinfoRS , String latitude , String longitude) throws DataException {
		SystemUtils.trace("sitemap", "insert office_locations data");
		DataBinder parameters = null;
		String query = "IOfficeLocations";
		SystemUtils.trace("sitemap", "afterLoadOfficeRecordWebChange dDocType = " + latitude.length() );
		SystemUtils.trace("sitemap", "afterLoadOfficeRecordWebChange dDocType = " + longitude.length() );
		SystemUtils.trace("sitemap", "afterLoadOfficeRecordWebChange did " + docinfoRS.getStringValueByName("dID") );
		SystemUtils.trace("sitemap", "afterLoadOfficeRecordWebChange dDocType = " + docinfoRS.getStringValueByName("dDocType") );
		SystemUtils.trace("sitemap", "afterLoadOfficeRecordWebChange xLocale = " + docinfoRS.getStringValueByName("xCountry") );
		
		if(latitude.length() <=  1 && longitude.length() <= 1)
			return;
		parameters = new DataBinder();
		parameters.putLocal("tblName", OFFICE_LOCATIONS_TBL.toUpperCase());
		parameters.putLocal("dID", docinfoRS.getStringValueByName("dID"));
		parameters.putLocal("dDocName", docinfoRS.getStringValueByName("dDocName"));
		parameters.putLocal("dDocType", docinfoRS.getStringValueByName("dDocType"));
		parameters.putLocal("xCountry", docinfoRS.getStringValueByName("xCountry"));
		parameters.putLocal("longitude", longitude);
		parameters.putLocal("latitude", latitude);
		long l =m_ws.execute(query, parameters);
		SystemUtils.trace("sitemap", "afterLoadOfficeRecordWebChange inserted rows : " + l);
		
		
	}

}
