package hays.custom;

import intradoc.common.LocaleUtils;
import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;

import intradoc.data.DataException;
import intradoc.data.DataResultSet;
import intradoc.data.ResultSet;
import intradoc.data.Workspace;
import intradoc.provider.Provider;
import intradoc.provider.Providers;
import intradoc.server.ServiceHandler;

public class ValidateAPI extends ServiceHandler {
	public void validateDocName() throws DataException, ServiceException {
		String databaseproviderName = this.m_currentAction.getParamAt(0);
		String docInfoInternalResultSetName = this.m_currentAction.getParamAt(1);
		String QdocRev = this.m_currentAction.getParamAt(2);
		
		String dDocName=m_binder.getLocal("dDocName");
		
		SystemUtils.trace("webapi_jobdetail", "databaseproviderName: "+databaseproviderName);
		SystemUtils.trace("webapi_jobdetail", "docInfoInternalResultSetName: "+docInfoInternalResultSetName);
		SystemUtils.trace("webapi_jobdetail", "Query for Doc Info QdocRev: "+QdocRev);
		SystemUtils.trace("webapi_jobdetail", "dDocName: "+dDocName);
		
		Provider p = Providers.getProvider(databaseproviderName);
	    if ((p == null) || (!p.isProviderOfType("database")))
	    {
	      throw new ServiceException("You the provider '" + databaseproviderName + 
	        "' is not a valid provider of type 'database'.");
	    }
    	
		Workspace databaseServerWs = (Workspace)p.getProvider();
		ResultSet rs = databaseServerWs.createResultSet(QdocRev, m_binder);
		DataResultSet drs = new DataResultSet(); 
		drs.copy(rs);
		
		if(drs!=null && drs.getNumRows() > 0){
			this.m_binder.putLocal("StatusMessage", LocaleUtils.encodeMessage("wwWebApiOKMsg", null));
			this.m_binder.putLocal("StatusCode", "UC000");
			m_binder.addResultSet(docInfoInternalResultSetName, drs);
		}else{
			this.m_binder.putLocal("StatusMessage", LocaleUtils.encodeMessage("wwInvalidJobDetailParameters", null));
			this.m_binder.putLocal("StatusCode", "UC015");	
			this.m_binder.removeResultSet("error");
			this.m_binder.m_resultSets.remove("error");
			this.m_binder.putLocal("ssChangeHTTPHeader","true");
			throw new ServiceException("Invalid dDocname: either expired or it does not exist:"+dDocName);
		}
		databaseServerWs.releaseConnection();
	}
}


	


		