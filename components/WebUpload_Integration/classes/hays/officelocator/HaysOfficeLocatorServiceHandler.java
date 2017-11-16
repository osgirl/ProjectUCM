package hays.officelocator;

import intradoc.common.LocaleUtils;
import intradoc.common.ServiceException;
import intradoc.common.StringUtils;
import intradoc.common.SystemUtils;
import intradoc.data.DataException;
import intradoc.data.DataResultSet;
import intradoc.data.ResultSet;
import intradoc.data.Workspace;
import intradoc.provider.Provider;
import intradoc.provider.Providers;
import intradoc.server.Service;
import intradoc.server.ServiceHandler;
import intradoc.shared.SharedObjects;

public class HaysOfficeLocatorServiceHandler extends ServiceHandler{
	
	public void getOfficeLocations()throws ServiceException, DataException{
		
		String town = m_binder.getLocal("Town");
		String postcode = m_binder.getLocal("PostCode");
		String brandId = m_binder.getLocal("BrandId");
		String latitude = m_binder.getLocal("ne_latitude"); //from auto suggest selection
		String longitude = m_binder.getLocal("ne_longitude"); //from auto suggest selection
		String msg = "";
		
		String sqlServerproviderName = this.m_currentAction.getParamAt(0);
		String resultSetName = this.m_currentAction.getParamAt(1);
		String qGetOfficeLocations = this.m_currentAction.getParamAt(2);
		
		String siteLocale="en-GB",isoCountryCode="GB";
		
		siteLocale = m_binder.getLocal("SiteLocale");
		isoCountryCode = m_binder.getLocal("isoCountryCode");


		
		if((town==null || town.trim().equals("")) && (postcode == null || postcode.trim().equals(""))){
			msg=LocaleUtils.encodeMessage("csOfficeLocatorMissingArguments",null);
			throw new ServiceException(msg);
		}
		
		try{
		   
            //m_service.executeServiceEx("HAYS_GET_GEOCODES",true);
			//m_binder.putLocal("latitude", m_binder.getLocal("Latitude"));
			//m_binder.putLocal("longitude", m_binder.getLocal("Longitude"));
			 
	
			//String latitude = "51.5001524";//m_binder.getLocal("Latitude");
			//String longitude = "-0.1262362";//m_binder.getLocal("Longitude");
			
			if(latitude == null || latitude.equals("") || longitude==null || longitude.equals("")){
				msg = LocaleUtils.encodeMessage("csOfficeLocatorUnableToGetLatLong",null);
				m_binder.putLocal("StatusMessage", msg);
				m_binder.putLocal("StatusCode", "0");
				return;
			}
				
			if(brandId == null || brandId.equals("")){
				brandId="null";
			}
			
			m_binder.putLocal("isoCountryCode", isoCountryCode);
			m_binder.putLocal("latitude", latitude);
			m_binder.putLocal("longitude", longitude);
			m_binder.putLocal("brandId", brandId);
			m_binder.putLocal("websiteLocale", siteLocale);
			
			SystemUtils.trace("GetOfficeLocations", "websiteLocale "+siteLocale);
			SystemUtils.trace("GetOfficeLocations", "isoCountryCode "+isoCountryCode);
			SystemUtils.trace("GetOfficeLocations", "latitude "+latitude);
			SystemUtils.trace("GetOfficeLocations", "longitude "+longitude);
			SystemUtils.trace("GetOfficeLocations", "brandId "+brandId);
			
			Provider p = Providers.getProvider(sqlServerproviderName);
    	    if ((p == null) || (!p.isProviderOfType("database")))
    	    {
    	      throw new ServiceException("You the provider '" + sqlServerproviderName + 
    	        "' is not a valid provider of type 'database'.");
    	    }
	    	
			Workspace sqlServerWs = (Workspace)p.getProvider(); 
							
			ResultSet rs = sqlServerWs.createResultSet(qGetOfficeLocations, m_binder);
			System.out.println(rs);
			DataResultSet drs = new DataResultSet(); 
			drs.copy(rs);
			if(drs!=null && drs.getNumRows() > 0){
				m_binder.addResultSet(resultSetName, drs);
			}else{
				m_binder.putLocal("StatusMessage", msg);
				m_binder.putLocal("StatusCode", "0");
				
			}
			if(sqlServerWs != null){
				sqlServerWs.releaseConnection();
			}
		}
		catch(Exception e){
			m_binder.putLocal("StatusMessage", e.toString());
			SystemUtils.trace("GetOfficeLocations", "StatusMessage "+e.toString());
			m_binder.putLocal("StatusCode", "0");
			e.printStackTrace();
		}finally{
			
		}
		
	}
	
	private static Workspace getDatabaseProviderWorkspace(String databaseProviderName)
	{
		Workspace workspace = null;
		Provider wsProvider =
		Providers.getProvider(databaseProviderName);
		if (wsProvider != null)
		workspace = (Workspace)wsProvider.getProvider();
		return workspace;
	}

}
