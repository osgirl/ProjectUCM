package hays.co.uk.search; //updated for automation

import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.data.DataException;
import intradoc.server.ServiceHandler;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.LinkedInApi;
import org.scribe.model.Token;
import org.scribe.oauth.*;

public class LinkedInAuthenticationHandler extends ServiceHandler{
	
	public void getLinkedinAuthURL() throws ServiceException, DataException{
		 
		try{
		SystemUtils.trace("linkedin_oauth_url", "inside linkedin_oauth_url");
		 String API_KEY = "6pi7jfqys0e5";
	     String API_SECRET = "b0IbSREOCdUgJgAp";
	     
	     OAuthService service = new ServiceBuilder().provider(LinkedInApi.class).apiKey(API_KEY).apiSecret(API_SECRET).scope("r_fullprofile r_basicprofile r_emailaddress r_contactinfo").callback("http://hrlvmdv1045.emea.hays.loc:7777/uk/").build();
	     Token requestToken = service.getRequestToken();
	     String linkedinAuthURL = service.getAuthorizationUrl(requestToken);
	     
	     SystemUtils.trace("linkedin_oauth_url", "linkedinAuthURL" + linkedinAuthURL);
	     SystemUtils.trace("linkedin_oauth_url", "requestToken" + requestToken);
	     SystemUtils.trace("linkedin_oauth_url", "service" + service);
	     
	     
	     m_binder.putLocal("authURL", linkedinAuthURL);
		}catch(Exception e)
		{
			SystemUtils.trace("linkedin_oauth_url", "Exception while processing : " + e);
		}
	    
		
	}

}
