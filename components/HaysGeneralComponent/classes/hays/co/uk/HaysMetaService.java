package hays.co.uk;

import intradoc.common.LocaleUtils;
import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.data.DataException;
import intradoc.data.DataResultSet;
import intradoc.data.FieldInfo;
import intradoc.data.ResultSet;
import intradoc.data.ResultSetUtils;
import intradoc.data.Workspace;
import intradoc.provider.Provider;
import intradoc.provider.Providers;
import intradoc.server.MetaService;
import intradoc.server.schema.SchemaUtils;
import intradoc.shared.ComponentClassFactory;
import intradoc.shared.SharedObjects;
import intradoc.shared.schema.SchemaHelper;
import intradoc.shared.schema.SchemaSecurityFilter;
import intradoc.shared.schema.SchemaViewData;

import java.util.ArrayList;
import java.util.Vector;

public class HaysMetaService extends MetaService {
	
	private static String default_locale = "en-GB";
	public void getDocMetaLists() throws DataException, ServiceException {
		String metadata = m_binder.getLocal("metadata");
		if( metadata == null)
			throw new DataException("Metadata is not defined");
		String locale = m_binder.getLocal("locale");
		SystemUtils.trace("getIndustryList","isMobile in industry list= "+m_binder.getLocal("isMobile") );
		getDocMetaDataInfo();
		String view = getOptionListViewName(metadata);
		SystemUtils.trace("getIndustryList","View Name "+view );
		/*Locale aLocale = null;
		if( view != null) {
			if (locale == null || locale.length() == 0 ){
				aLocale = new Locale(this.m_languageId);
			} else {
				aLocale = HaysUtil.getLocale(locale);
			}
			//HaysUtil.trace("aLocale "+aLocale +" locale "+locale);
			getViewValuesResultSet(view, aLocale);
		}*/
		
		if( view != null) {
			if (locale == null || locale.length() == 0 ){
				locale = default_locale;
			}
			if(m_binder.getLocal("isMobile")!=null && m_binder.getLocal("isMobile").equals("Y"))
			{
				String providerName = "SystemDatabase";
				String resultSetName = "LOCALE_DETAILS";
				String queryName = "QGetLocaleDetails";
				

				if(null != this.m_binder.getLocal("locale") && this.m_binder.getLocal("locale").length()>0){
					locale = this.m_binder.getLocal("locale");
				}else{
					locale = this.m_binder.getLocal("SiteLocale");
				}
				this.m_binder.putLocal("sitelocale", locale);
				Provider p = Providers.getProvider(providerName);
				Workspace ws = (Workspace) p.getProvider();
				DataResultSet result = null;

				// if they specified a predefined query, execute that
				if (queryName != null && queryName.trim().length() > 0) {
					// obtain a JDBC result set with the data in it. This result set is
					// temporary, and we must copy it before putting it in the binder
					ResultSet temp = ws.createResultSet(queryName, m_binder);

					// create a DataResultSet based on the temp result set
					result = new DataResultSet();
					result.copy(temp);
				}

				// place the result into the databinder with the appropriate name
				if (result.getNumRows() <= 0) {

					String msg = LocaleUtils.encodeMessage("wwInvalidSiteLocale", "");
					this.m_binder.putLocal("StatusMessage", LocaleUtils.encodeMessage(
							"wwInvalidSiteLocale", null));
					this.m_binder.putLocal("StatusCode", "UC001");					
					throw new ServiceException(msg);

				} else {
					this.m_binder.addResultSet(resultSetName, result);
				}
				getViewValuesMobileResultSet(view, locale);
			}else{
				getViewValuesResultSet(view, locale);
			}
		}
	} 
	

	
    private static String getOptionListViewName( String metadata) throws DataException, ServiceException {
        String s1 = "dIsOptionList";
        DataResultSet dataresultset = SharedObjects.getTable("DocMetaDefinition");
        int i = ResultSetUtils.getIndexMustExist(dataresultset, "dName");
        Vector<String> row = dataresultset.findRow(i, metadata);
        i = ResultSetUtils.getIndexMustExist(dataresultset, "dOptionListKey");
        String view = row.get(i);
        i = view.indexOf("view://");
        if( i == 0) {
        	view = view.substring(7);
        }
       return view;

    }
    
    public void getViewValuesResultSet(String s, String paramLocale) throws ServiceException, DataException {
        
    	SchemaHelper schemahelper = (SchemaHelper)ComponentClassFactory.createClassInstance("SchemaHelper", "intradoc.shared.schema.SchemaHelper", null);
    	SchemaUtils schemaUtils = (SchemaUtils)ComponentClassFactory.createClassInstance("SchemaUtils", "intradoc.server.schema.SchemaUtils", null);
    	SchemaViewData schemaviewdata = schemahelper.getView(s);
    	SchemaSecurityFilter schemasecurityfilter = schemaUtils.getSecurityImplementor(schemaviewdata);
    	if (schemasecurityfilter != null) {
            schemasecurityfilter.init(this);
        }
        Object obj = schemaviewdata.getAllViewValuesWithFilter(schemasecurityfilter);
        DataResultSet resSet = new DataResultSet();
        DataResultSet temp = new DataResultSet();
        String locale = null;
        // filter by language
        if( obj != null && paramLocale != null ) {
        	temp.copy((ResultSet)obj );
        	
        	if( temp.first() && temp.getFieldInfo("LOCALE", new FieldInfo()) ) {
        		resSet.copyFieldInfo(temp);
	        	do {
	        		locale = temp.getStringValueByName("LOCALE");
	        		if( paramLocale.equals(locale)) {	        		
	        			resSet.addRow( temp.getCurrentRowValues() );
	        		}
	        	} while( temp.next());
        	}
        }  
       
        if( !resSet.isEmpty() && resSet.getFieldInfo("dDescription", new FieldInfo()) ) {
        		ResultSetUtils.sortResultSet(resSet, new String[]{"dDescription"});
    	}	
        m_binder.addResultSet("SchemaData", resSet);
    }
    
    public void getViewValuesMobileResultSet(String s, String paramLocale) throws ServiceException, DataException {
    	SchemaHelper schemahelper = (SchemaHelper)ComponentClassFactory.createClassInstance("SchemaHelper", "intradoc.shared.schema.SchemaHelper", null);
    	SchemaUtils schemaUtils = (SchemaUtils)ComponentClassFactory.createClassInstance("SchemaUtils", "intradoc.server.schema.SchemaUtils", null);
    	SchemaViewData schemaviewdata = schemahelper.getView(s);
    	SchemaSecurityFilter schemasecurityfilter = schemaUtils.getSecurityImplementor(schemaviewdata);
    	if (schemasecurityfilter != null) {
            schemasecurityfilter.init(this);
        }
    	ResultSet obj = schemaviewdata.getAllViewValuesWithFilter(schemasecurityfilter);
    	String[] lHeaderArray = new String[]{"dKey","dDescription","LOCALE","Display.default"};
        DataResultSet resSet = new DataResultSet(lHeaderArray);
        String locale = null;
        SystemUtils.trace("getIndustryList", "Obj Field Number"+obj.getNumFields());
        // filter by language
        if( obj != null && paramLocale != null ) {
        	do {
        		locale = obj.getStringValueByName("LOCALE");
        		if( paramLocale.equals(locale)) {	        		
        			ArrayList<String> new_values = new ArrayList<String>();
	        		new_values.add(obj.getStringValueByName("dKey"));
	        		new_values.add(obj.getStringValueByName("dDescription"));
	        		new_values.add(locale);
	        		new_values.add(obj.getStringValueByName("Display.default"));
	        		SystemUtils.trace("getIndustryList"," Vector values "+ new_values);
	        		resSet.addRowWithList(new_values);
        		}
        	} while( obj.next());
        }  
       
        if( !resSet.isEmpty() && resSet.getFieldInfo("dDescription", new FieldInfo()) ) {
        		ResultSetUtils.sortResultSet(resSet, new String[]{"dDescription"});
    	}	
        m_binder.addResultSet("SchemaData", resSet);
        
	   if(m_binder.getResultSets().containsKey("DocMetaDefinition")){
       	m_binder.removeResultSet("DocMetaDefinition");
       }
       if(m_binder.getResultSets().containsKey("DocTypes")){
       	m_binder.removeResultSet("DocTypes");
       } 
       if(m_binder.getResultSets().containsKey("UserAttribInfo")){
          	m_binder.removeResultSet("UserAttribInfo");
       } 
       m_binder.getLocalData().remove("isMobile");
       m_binder.removeLocal("isMobile");
       m_binder.removeResultSet("LOCALE_DETAILS");
       m_binder.removeLocal("sitelocale");
       this.m_binder.putLocal("StatusMessage", LocaleUtils.encodeMessage("wwWebApiOKMsg", null));
	   this.m_binder.putLocal("StatusCode", "UC000");
    }
    
    /* commented as unnecessary Locale objects were being created */
    /* public void getViewValuesResultSet(String s, Locale paramLocale) throws ServiceException, DataException {
    
    	SchemaHelper schemahelper = (SchemaHelper)ComponentClassFactory.createClassInstance("SchemaHelper", "intradoc.shared.schema.SchemaHelper", null);
    	SchemaUtils schemaUtils = (SchemaUtils)ComponentClassFactory.createClassInstance("SchemaUtils", "intradoc.server.schema.SchemaUtils", null);
    	SchemaViewData schemaviewdata = schemahelper.getView(s);
    	SchemaSecurityFilter schemasecurityfilter = schemaUtils.getSecurityImplementor(schemaviewdata);
    	if (schemasecurityfilter != null) {
            schemasecurityfilter.init(this);
        }
        Object obj = schemaviewdata.getAllViewValuesWithFilter(schemasecurityfilter);
    //    System.out.println("Obj: " + obj);
        DataResultSet resSet = new DataResultSet();
        DataResultSet temp = new DataResultSet();
        String locale = null;
        Locale aLocale = null;
        // filter by language
        if( obj != null && paramLocale != null ) {
        	temp.copy((ResultSet)obj );
        	if( temp.first() && temp.getFieldInfo("LOCALE", new FieldInfo()) ) {
        		resSet.copyFieldInfo(temp);
	        	do {
	        		locale = temp.getStringValueByName("LOCALE");
	        		aLocale = HaysUtil.getLocale(locale);
	        		//HaysUtil.trace("aLocale "+aLocale +" locale "+locale);
	        		
	        		 int index = locale.indexOf("_");
	        		if( index > 0 )
	        			aLocale = new Locale( locale.substring(0, index), locale.substring(index+1));
	        		else
	        			aLocale = new Locale(locale);
	        		
	        	//	System.out.println(paramLocale.getLanguage() + ", " + paramLocale.getCountry() + "|  Lang: " + aLocale.getLanguage() + " | " + aLocale.getDisplayLanguage() );
	        		if( paramLocale.getLanguage().equals( aLocale.getLanguage()) &&
	        				(paramLocale.getCountry().length() == 0 || (paramLocale.getCountry().length() > 0 && paramLocale.getCountry().equals(aLocale.getCountry())) )) {	        		
	        			resSet.addRow( temp.getCurrentRowValues() );
	        		}
	        	} while( temp.next());
        	}
        }  
       
        if( !resSet.isEmpty() && resSet.getFieldInfo("dDescription", new FieldInfo()) ) {
        		ResultSetUtils.sortResultSet(resSet, new String[]{"dDescription"});
    	}	
        m_binder.addResultSet("SchemaData", resSet);
    }*/


}
