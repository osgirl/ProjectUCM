package hays.co.uk.search;

import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.data.DataException;
import intradoc.data.DataResultSet;
import intradoc.data.ResultSet;
import intradoc.data.Workspace;
import intradoc.provider.Provider;
import intradoc.provider.Providers;
import intradoc.server.ServiceHandler;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class HaysSearchKeywordHandler extends ServiceHandler {
	
	// parameters
	public static final String RESULTSET_KEYWORD 	= "SearchResultNavigationxKeywords";
	public static final String DRILL_DOWN_OPT_COL = "drillDownOptionValue";
	public static final String DRILL_DOWN_COUNT_COL = "count";
	DataResultSet KeywordResultSet = new DataResultSet(new String[] {"newkeyword", "count"});
	
	public void keywordCount() throws DataException, ServiceException {
		String keywords=QueryUtils.decodeHaysSpecialKeywords(super.m_binder.getLocal(IHaysSearchConstants.JOB_TITLE_REF));	
		SystemUtils.trace("hays_search", "Search Keyword: "+keywords);
		String keywordsArr[]=keywords.split(",");
		for( int i = 0; i < keywordsArr.length; i++) {
			SystemUtils.trace("hays_search", "Search Keyword"+i+":" + keywordsArr[i]);
			keywordsOccurance(keywordsArr[i]);		
		}	
		
		m_binder.addResultSet("KeywordResultSet",KeywordResultSet);
		
	}
	
	
	
	private void keywordsOccurance(String keyword) throws ServiceException, DataException {
		DataResultSet drsKeywordFacet = (DataResultSet)super.m_binder.getResultSet(RESULTSET_KEYWORD);
		int count =0;
		String keywordLCase=keyword.toLowerCase();
		
		Vector drillDownOptionValue = new Vector();
		if( drsKeywordFacet == null)
			return;

				
		SystemUtils.trace("hays_search", "SearchResultNavigationxKeywords Rows: " + drsKeywordFacet.getNumRows());
		for( int i = 0; i < drsKeywordFacet.getNumRows(); i++) {	
			
			drillDownOptionValue=drsKeywordFacet.getRowValues(i) ;			
			SystemUtils.trace("hays_search", "Result Set Keyword Row"+" "+i+": " + drillDownOptionValue);
			//SystemUtils.trace("hays_search", "Result Set Keyword Row"+" "+i+" Column 0: " + drillDownOptionValue.elementAt(0).toString());
			//SystemUtils.trace("hays_search", "Result Set Keyword Row"+" "+i+" Column 1: " + drillDownOptionValue.elementAt(1).toString());
			//SystemUtils.trace("hays_search", "Result Set Keyword Row"+" "+i+" Column 2: " + drillDownOptionValue.elementAt(2).toString());
			//SystemUtils.trace("hays_search", "Result Set Keyword Row"+" "+i+" Column 3: " + drillDownOptionValue.elementAt(3).toString());
			

			if(drillDownOptionValue.elementAt(0).toString().toLowerCase().contains(keywordLCase))
			{
				SystemUtils.trace("hays_search", "Matched ResultSet Keyword: " + drillDownOptionValue.elementAt(0).toString());
				count=count+Integer.parseInt((drillDownOptionValue.elementAt(2)).toString());
									
			}
			else if(drillDownOptionValue.elementAt(0).toString().toLowerCase().contains(keywordLCase+",") || drillDownOptionValue.elementAt(0).toString().toLowerCase().contains(","+keywordLCase))
			{
				SystemUtils.trace("hays_search", "ResultSet Keyword contains: " + drillDownOptionValue.elementAt(0).toString());
				count=count+1;					
			}
			else
			{
				SystemUtils.trace("hays_search", "No match found in the keyword Result Set for Keyword:"+keyword);
			}
			
		}
		SystemUtils.trace("hays_search", "Matching Keywod count: " + count);
		if(count>0)
		{
			Vector<String> fields = new Vector<String>();
			fields.add(keyword);
			fields.add(Integer.toString(count));			
			KeywordResultSet.addRow(fields);
		}
	}
}
