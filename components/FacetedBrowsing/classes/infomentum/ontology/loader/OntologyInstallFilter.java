package infomentum.ontology.loader;

import java.util.Hashtable;
import java.util.Map;




import infomentum.ontology.OntologyCacheMinder;
import infomentum.ontology.utils.OntologyUtils;
import intradoc.common.ExecutionContext;
import intradoc.common.FileUtils;
import intradoc.common.NumberUtils;
import intradoc.common.ServiceException;
import intradoc.data.DataBinder;
import intradoc.data.DataException;
import intradoc.data.DataResultSet;
import intradoc.data.ResultSet;
import intradoc.data.Workspace;
import intradoc.resource.ResourceUtils;
import intradoc.server.IdcExtendedLoader;
import intradoc.server.Service;
import intradoc.shared.ComponentClassFactory;
import intradoc.shared.FilterImplementor;

public class OntologyInstallFilter implements FilterImplementor  {
	
	protected Workspace m_workspace = null;
	protected Service m_service = null;
	
	 public int doFilter(Workspace ws, DataBinder binder, ExecutionContext cxt) throws DataException, ServiceException {
		 this.m_workspace = ws;
		 
	        Object paramObj = cxt.getCachedObject("filterParameter");
	        if (paramObj == null || !(paramObj instanceof String)) {
	            return 0;
	        }
	        String param = (String)paramObj;
	        IdcExtendedLoader loader = null;
	        if (cxt instanceof IdcExtendedLoader) {
	            loader = (IdcExtendedLoader)cxt;
	            if (ws == null) {
	                ws = loader.getLoaderWorkspace();
	            }
	        } else
		        if (cxt instanceof Service) {
		            loader = (IdcExtendedLoader)ComponentClassFactory.createClassInstance("IdcExtendedLoader", "intradoc.server.IdcExtendedLoader", "!csCustomInitializerConstructionError");
		        }
	        String updateState = loader.getDBConfigValue("ComponentUpdate", "FacetedBrowsing", "1.0");
	       // System.out.println("Sec Nav component: updateState = " + updateState);
	        int compLevel = NumberUtils.parseInteger(updateState, 0);
	       // System.out.println( "SPNavigation Component compLevel = " + compLevel);
	        if (param.equals("extraBeforeCacheLoadInit")) {
	        	upgrade(loader, ws, compLevel);
	        //	 ws.executeSQL("DELETE FROM Config WHERE (dSection = 'ComponentUpdate') AND (dName = 'SPNavigation')");
	         //   loader.setDBConfigValue("ComponentUpdate", "FacetedBrowsing", "10.0", "11");
	        } else if (param.equals("extraAfterServicesLoadInit")) {
                doInstall(loader, binder, cxt);
            } 
	        return 0;
	 }
	 
	 protected int doInstall(IdcExtendedLoader idcextendedloader, DataBinder databinder, ExecutionContext executioncontext)  {
			
			return 0;
	 }
	 
	
	 
	 
	 private void upgrade(IdcExtendedLoader loader, Workspace ws, int compLevel) throws DataException {
		 
	 }

}
