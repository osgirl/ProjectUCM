package hays.co.uk.search;

import java.util.Properties;
import java.util.Vector;

import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.data.DataException;
import intradoc.data.DataResultSet;
import intradoc.data.FieldInfo;
import intradoc.data.ResultSetUtils;
import intradoc.server.ServiceHandler;
import intradoc.shared.SharedObjects;
import infomentum.ontology.*;
import infomentum.ontology.loader.OntologyFacade;
import infomentum.ontology.navigation.OntologyNavigationHandler;
import infomentum.ontology.utils.OntologyUtils; 


public class HaysSpecialismDescriptionHandler extends ServiceHandler {
	
	public static final String RESULTSET_SPECIALISM 	= "SearchResultNavigationxSpecialismId";
	public static final String SPECIALISM_IDENTIFIER 	= "drillDownOptionValue";
	public static final String SPECIALISM_DESCRIPTION	= "SpecialismDescription";
	
	
	//This function is used for adding specialism description in ResultSet SearchResultNavigationxSpecialismId
	public void addSpecialismDescription() throws DataException, ServiceException {
		
		if(m_binder.getLocal("isMobile")!=null && m_binder.getLocal("SiteLocale").length()>0)
		{
			DataResultSet drsSpecialismFacet = (DataResultSet)super.m_binder.getResultSet(RESULTSET_SPECIALISM);
			SystemUtils.trace("hays_search", "ResultSet SearchResultNavigationxSpecialismId Columns: "+ drsSpecialismFacet.getNumFields());
						
			if (drsSpecialismFacet.getNumFields()<5)
			{
				String haysLocale=m_binder.getLocal("SiteLocale");
				String language="";
				String specialismMetaName="";
				String specialismDescription="";
				int specialismRows = drsSpecialismFacet.getNumRows();			
				int localeIndex=haysLocale.indexOf("-");			
					
				
				if (localeIndex>0)
				{
					language=haysLocale.substring(0, haysLocale.indexOf("-"));
				}
				else
				{
					language="en";
				}
					
				
				SystemUtils.trace("hays_search", "SiteLocale for Specialism "+ haysLocale);
				SystemUtils.trace("hays_search", "Language for Specialism "+ language);
				
				int specialismFieldIndex = drsSpecialismFacet.getNumFields();
				Vector<FieldInfo> spcialDesc = new Vector<FieldInfo>();
		        FieldInfo specialismfieldInfo = new FieldInfo();
		        specialismfieldInfo.m_name="SpecialismDescription";
		        specialismfieldInfo.m_type = 6;
		        spcialDesc.add(specialismfieldInfo);
		        drsSpecialismFacet.appendFields(spcialDesc);
				
				//for(int i=0;i<specialismRows;i++){
					
					//drsSpecialismFacet.setCurrentRow(i);	
		        
		        do{
		        	SystemUtils.trace("webapi_search",  "\nIn while: " + specialismFieldIndex);
	        		int currentRowIndex =  drsSpecialismFacet.getCurrentRow();
					
	        		
					SystemUtils.trace("hays_search", "Specialism Term : " + drsSpecialismFacet.getStringValueByName(SPECIALISM_IDENTIFIER));
					
					specialismMetaName=drsSpecialismFacet.getStringValueByName(SPECIALISM_IDENTIFIER);
					
									
					if(specialismMetaName.indexOf(",")!=-1 && specialismMetaName.length()>0)
					{
						SystemUtils.trace("hays_search", "Multiple Specialisms : " + specialismMetaName);
						specialismDescription="";
					}
					else
					{
					specialismDescription = Converter.getLabel(specialismMetaName,OntologyFacade.getOntology("xCategory"),language);
					}
					
					if(specialismDescription==null || specialismDescription.length()<=0)
					{
						specialismDescription="";
					}
					
					SystemUtils.trace("hays_search", "Specialism Description : " + specialismDescription);
					
					Vector specialismv = drsSpecialismFacet.getCurrentRowValues();
	        		
					specialismv.set(specialismFieldIndex, specialismDescription);       		
					
		        }while(drsSpecialismFacet.next());			
				//}
				
				SystemUtils.trace("hays_search", "Specialism Description after merging in resultset"+ drsSpecialismFacet);
				
			}
		
					
		}
	}
	

}
