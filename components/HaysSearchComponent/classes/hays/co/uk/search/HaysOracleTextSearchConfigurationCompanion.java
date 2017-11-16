package hays.co.uk.search;

import intradoc.common.ExecutionContext;
import intradoc.common.ExecutionContextAdaptor;
import intradoc.common.LocaleResources;
import intradoc.common.Report;
import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.data.DataBinder;
import intradoc.data.DataException;
import intradoc.search.OracleTextSearchConfigCompanion;
import intradoc.server.SearchIndexerUtils;


public class HaysOracleTextSearchConfigurationCompanion extends OracleTextSearchConfigCompanion{
	
	 public int fixUpAndValidateQuery(DataBinder databinder, ExecutionContext executioncontext) throws DataException, ServiceException {
	        String s = databinder.getLocal("QueryText");
	        Report.trace("searchquery", (new StringBuilder()).append("Hays fixUpAndValidateQuery() QueryText: ").append(s).toString(), null);
	        String s1 = SearchIndexerUtils.getSearchQueryFormat(databinder, executioncontext);
	        if (s1 != null && s1.equalsIgnoreCase("Universal")) {
	        	SystemUtils.trace("hays_search", "Inside the iffffffff block");
	            m_parser.setDateFormat(LocaleResources.m_searchFormat);
	            ExecutionContextAdaptor executioncontextadaptor = new ExecutionContextAdaptor();
	            executioncontextadaptor.setParentContext(executioncontext);
	            executioncontextadaptor.setCachedObject("UserDateFormat", databinder.m_blDateFormat);
	            
	            // custom
	            String searchType = databinder.getLocal("searchType");
	            if( "Hays".equals(searchType)) {
	       //     	System.out.println("\nfixUpAndValidateQuery(): " + databinder.getLocalData());
	            }else {
	            	s = m_parser.parse(databinder, executioncontextadaptor);
	            }
	         // custom
	         } else {
	        	 SystemUtils.trace("hays_search", "Inside the else block"); 
		            s = prepareFullTextQuery(s, databinder, executioncontext);
	         }
		       
		        
		     
		        databinder.putLocal("TranslatedQueryText", s);
		        databinder.putLocal("QueryText", s);
		        return 0;
     }
	 
	 
	
	 public String getCacheKey(String s, String s1, String s2, DataBinder databinder, int flags, ExecutionContext executioncontext) throws DataException, ServiceException {
		 String key = super.getCacheKey(s, s1, s2, databinder, flags, executioncontext);
		 
		 StringBuffer str = new StringBuffer(key);
		
		 if( databinder.getLocal(IHaysSearchConstants.NE_LONGITUDE) != null) {
			 str.append("/").append(databinder.getLocal(IHaysSearchConstants.NE_LONGITUDE));
		 }
		 if( databinder.getLocal(IHaysSearchConstants.NE_LATITUDE) != null) {
			 str.append("/").append(databinder.getLocal(IHaysSearchConstants.NE_LATITUDE));
		 }
		 if( databinder.getLocal(IHaysSearchConstants.SW_LATITUDE) != null) {
			 str.append("/").append(databinder.getLocal(IHaysSearchConstants.SW_LATITUDE));
		 }
		 if( databinder.getLocal(IHaysSearchConstants.SW_LONGITUDE) != null) {
			 str.append("/").append(databinder.getLocal(IHaysSearchConstants.SW_LONGITUDE));
		 }
		 if( databinder.getLocal(IHaysSearchConstants.RADIUS) != null) {
			 str.append("/").append(databinder.getLocal(IHaysSearchConstants.RADIUS));
		 } 
		 if( databinder.getLocal(IHaysSearchConstants.EXCLUDE) != null) {
			 str.append("/").append(databinder.getLocal(IHaysSearchConstants.EXCLUDE));
		 }
		 if( databinder.getLocal("isMobile") != null) {
			 str.append("/").append(databinder.getLocal("isMobile"));
		 }
		 if( databinder.getLocal("islinkedin") != null) {
			 str.append("/").append(databinder.getLocal("islinkedin"));
		 }
		 if( databinder.getLocal("isExtraParams") != null) {
			 str.append("/").append(databinder.getLocal("isExtraParams"));
		 }
		 if( databinder.getLocal("isWC") != null) {
			 str.append("/").append(databinder.getLocal("isWC"));
		 }
		 return str.toString();
	 }

	 
	
}
