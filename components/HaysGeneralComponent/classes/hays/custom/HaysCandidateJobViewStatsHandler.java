package hays.custom;

import intradoc.common.LocaleUtils;
import intradoc.common.Log;
import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.data.DataException;
import intradoc.data.Workspace;
import intradoc.provider.Provider;
import intradoc.provider.Providers;
import intradoc.server.ServiceHandler;

public class HaysCandidateJobViewStatsHandler extends ServiceHandler {
	
	public void populateCandJobViewStats() throws ServiceException, DataException {
		
		// obtain the provider name, and the query
		// from the action definition in the service
		String providerName = m_currentAction.getParamAt(0);
		String queryCandidateViewName = m_currentAction.getParamAt(1);
		String queryJobViewName = m_currentAction.getParamAt(2);
		
		String errMsgCommon = LocaleUtils.encodeMessage("wwCandJobViewCommon", null);
		
		String queryName = null;
		String contentType = m_binder.getLocal("contentType");
		if(contentType != null && "Jobs".equals(contentType)){
			queryName = queryJobViewName.trim();
		}else if(contentType != null && "Candidates".equals(contentType)){
			queryName = queryCandidateViewName.trim();
		}else{
			//Log.errorEx(errMsgCommon+LocaleUtils.encodeMessage("wwCandJobViewStatsContentTypeNotValid", null), errMsgCommon+LocaleUtils.encodeMessage("wwCandJobViewStatsContentTypeNotValid", null));
			//throw new ServiceException("ContentType is not Jobs or Candidates");
			return;
		}
		
		// validate that the provider is a valid database provider
		Provider p = Providers.getProvider(providerName);
		if (p == null) {
			
			Log.errorEx(errMsgCommon+LocaleUtils.encodeMessage("wwCandJobViewStatsNoProvider", null,providerName), errMsgCommon+LocaleUtils.encodeMessage("wwCandJobViewStatsNoProvider", null,providerName));
			//throw new ServiceException("The provider '" + providerName
					//+ "' does not exist.");
			return;
		} else if (!p.isProviderOfType("database")) {
			
			Log.errorEx(errMsgCommon+LocaleUtils.encodeMessage("wwCandJobViewStatsInvalidProvider", null,providerName), errMsgCommon+LocaleUtils.encodeMessage("wwCandJobViewStatsInvalidProvider", null,providerName));
			//throw new ServiceException("The provider '" + providerName
					//+ "' is not a valid provider of type 'database'.");
			return;
		}

		// grab the provider object that does all the work, and scope it to
		// a workspace object for database access, since we can be reasonably
		// certain at this point that the object returned is a Workspace object
		Workspace ws = (Workspace) p.getProvider();
		
		String PortalProfileId = m_binder.getLocal("PortalProfileId");
		String domainId = m_binder.getLocal("domainId");
		String languageId = m_binder.getLocal("languageId");
		String 	ContentId = m_binder.getLocal("ContentId");
		SystemUtils.trace("jobcandviewstats", "Got all details");
		if (PortalProfileId != null && !"".equals(PortalProfileId) && domainId != null && !"".equals(domainId) && ContentId != null && !"".equals(ContentId) && languageId != null && !"".equals(languageId)){
			if (queryName != null && queryName.trim().length() > 0) {
				SystemUtils.trace("jobcandviewstats", "Executing query");
				long status = ws.execute(queryName, m_binder);
				SystemUtils.trace("jobcandviewstats", "status "+status);
			}
		}else{
			Log.errorEx(errMsgCommon+LocaleUtils.encodeMessage("wwCandJobViewStatsMissingParams", null), errMsgCommon+LocaleUtils.encodeMessage("wwCandJobViewStatsMissingParams", null));
			//throw new ServiceException("Some paramenters are missing PortalProfileId,DomainName or ContentId");
			return;
		}
		
		ws.releaseConnection();
	}
}
