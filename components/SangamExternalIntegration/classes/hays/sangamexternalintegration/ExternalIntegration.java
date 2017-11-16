package hays.sangamexternalintegration;

import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.data.DataBinder;
import intradoc.data.DataException;
import intradoc.data.DataResultSet;
import intradoc.data.ResultSet;
import intradoc.data.Workspace;
import intradoc.provider.Provider;
import intradoc.provider.Providers;
import intradoc.server.Action;
import intradoc.server.ServiceHandler;
import intradoc.shared.SharedObjects;

public class ExternalIntegration extends ServiceHandler
{
  public void executeHaysProviderSql()
    throws ServiceException, DataException
  {
    String providerName = this.m_currentAction.getParamAt(0);
    String resultSetName = this.m_currentAction.getParamAt(1);

    String rawSql = this.m_binder.getLocal("RawSql");
    if ((rawSql == null) || (rawSql.length() == 0))
    {
      throw new ServiceException("You must specify a value for 'RawSql'.");
    }

    Provider p = Providers.getProvider(providerName);
    if ((p == null) || (!p.isProviderOfType("database")))
    {
      throw new ServiceException("You the provider '" + providerName + 
        "' is not a valid provider of type 'database'.");
    }

    Workspace ws = (Workspace)p.getProvider();

    ResultSet temp = ws.createResultSetSQL(rawSql);

    DataResultSet result = new DataResultSet();
    result.copy(temp);

    this.m_binder.addResultSet(resultSetName, result);

    ws.releaseConnection();
  }

  public void executeHaysProviderQuery()
    throws ServiceException, DataException
  {
	  String msg="Unable to execute service";
	  try{
		  
		  	String sqlServerproviderName = this.m_currentAction.getParamAt(0);
		    String resultSetName = this.m_currentAction.getParamAt(1);
		    String qGetBrandNames = this.m_currentAction.getParamAt(2);
		    
		    SystemUtils.trace("BrandNames", "sqlServerproviderName "+sqlServerproviderName);
		    SystemUtils.trace("BrandNames", "resultSetName "+resultSetName);
		    SystemUtils.trace("BrandNames", "qGetBrandNames "+qGetBrandNames);
		    
		    String domainId="1",isoCountryCode="GB",languageId="1";
		    
		    domainId = m_binder.getLocal("domainId");
		    isoCountryCode = m_binder.getLocal("isoCountryCode");
		    languageId = m_binder.getLocal("languageId");
		    
		    SystemUtils.trace("BrandNames", "domainId "+domainId);
		    SystemUtils.trace("BrandNames", "isoCountryCode "+isoCountryCode);
		    SystemUtils.trace("BrandNames", "languageId "+languageId);
		    
	    	//DataBinder binder = new DataBinder();
	    	m_binder.putLocal("domainId", domainId);
	       	m_binder.putLocal("isoCountryCode", isoCountryCode);
	    	m_binder.putLocal("languageId", languageId);
	    	
    	   Provider p = Providers.getProvider(sqlServerproviderName);
    	    if ((p == null) || (!p.isProviderOfType("database")))
    	    {
    	      throw new ServiceException("You the provider '" + sqlServerproviderName + 
    	        "' is not a valid provider of type 'database'.");
    	    }
	    	
			Workspace sqlServerWs = (Workspace)p.getProvider(); 
			
			ResultSet rs = sqlServerWs.createResultSet(qGetBrandNames, m_binder);
			//ResultSet rs = sqlServerWs.createResultSetSQL("select ABT.BrandId,ABT.BrandName from AllBrandTranslations ABT where ABT.DomainId="+domainIdDrs.getStringValue(0)+" and ABT.LanguageId ="+domainIdDrs.getStringValue(2));
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
			m_binder.putLocal("StatusMessage", "Error -> "+e.getMessage()+" Please see log files for detials"); 
			m_binder.putLocal("StatusCode", "0");
			SystemUtils.errorEx(e, "Error Occured ", "Error Occured");
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
