package hays.custom;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.Vector;

import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.data.DataException;
import intradoc.server.ServiceHandler;
import intradoc.shared.SharedObjects;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.LinkedInApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.*;

public class LinkedInAuthenticationHandler extends ServiceHandler{
	
	public static final String SEPERATOR = "##"; 
	public void getLinkedinAuthURL() throws ServiceException, DataException{
		
		String oneTouchId = m_binder.getLocal("OTId");  
		String siteId = m_binder.getLocal("siteId");
		String callbackUrl = m_binder.getLocal("callbackUrl")+ "?OTId=" + oneTouchId;
		
		
		 
		try{
		SystemUtils.trace("linkedin_oauth_url", "inside linkedin_oauth_url");
		SystemUtils.trace("linkedin_oauth_url", " :::siteId::::" + siteId);
		SystemUtils.trace("linkedin_oauth_url", " :::callbackUrl::::" + callbackUrl);
		
		 String API_KEY = SharedObjects.getEnvironmentValue("LINKEDIN_API_KEY");
	     String API_SECRET =SharedObjects.getEnvironmentValue("LINKEDIN_API_SECRET");
	     String PROXY_HOST =SharedObjects.getEnvironmentValue("PROXY_HOST");
	     String PROXY_PORT =SharedObjects.getEnvironmentValue("PROXY_PORT");
	     
	     final String authUser = SharedObjects.getEnvironmentValue("PROXY_AUTH_USER");
	     final String authPassword = SharedObjects.getEnvironmentValue("PROXY_AUTH_PASSWORD");
	     
	     Authenticator.setDefault(
	        new Authenticator() {
	           public PasswordAuthentication getPasswordAuthentication() {
	              return new PasswordAuthentication(
	                    authUser, authPassword.toCharArray());
	           }
	        }
	     );

	     System.setProperty("https.proxyUser", authUser);
	     System.setProperty("https.proxyPassword", authPassword); 
	     
  
	     System.setProperty("https.proxyHost", PROXY_HOST);
	     System.setProperty("https.proxyPort", PROXY_PORT);      
	     
	    
	     
	     OAuthService service = new ServiceBuilder().provider(LinkedInApi.class).apiKey(API_KEY).apiSecret(API_SECRET).scope("r_fullprofile r_basicprofile r_emailaddress r_contactinfo").callback(callbackUrl).build();
	     Token requestToken = service.getRequestToken();
	     String linkedinAuthURL = service.getAuthorizationUrl(requestToken);
	     
	     SystemUtils.trace("linkedin_oauth_url", "linkedinAuthURL" + linkedinAuthURL);
	     SystemUtils.trace("linkedin_oauth_url", "requestToken" + requestToken);
	     SystemUtils.trace("linkedin_oauth_url", "service" + service);
	     
	     
	     
	     m_binder.putLocal("authURL", linkedinAuthURL);
	     String strTokenString = requestToken.getToken() + SEPERATOR + requestToken.getSecret() +
	     	SEPERATOR + requestToken.getRawResponse();
	     m_binder.putLocal("requestToken", strTokenString);
	   
	    
	     
		}catch(Exception e)
		{
			SystemUtils.trace("linkedin_oauth_url", "Exception while processing : " + e);
		}
	    
		
	}
	
	public void getLinkedinCallbackParams()throws ServiceException, DataException {
       
		try{
			String[] tokenParams = null;
			Token RequestToken = null;
			String tokenparam = m_binder.getLocal("oauth_token");
			String verifier = m_binder.getLocal("oauth_verifier");
			
			String strTokenstring = m_binder.getLocal("requestToken");
			
			if(strTokenstring != null && strTokenstring != "")
			{
				tokenParams = strTokenstring.split(SEPERATOR);
				RequestToken=new Token(tokenParams[0], tokenParams[1], tokenParams[2]);
			}
			
			OAuthService Authservice = new ServiceBuilder().provider(LinkedInApi.class).apiKey("6pi7jfqys0e5").apiSecret("b0IbSREOCdUgJgAp").build();
			
			
			SystemUtils.trace("linkedin_oauth_url", "getLinkedinCallbackParams : oauth_token" + tokenparam);
			SystemUtils.trace("linkedin_oauth_url", "getLinkedinCallbackParams : oauth_verifier" + verifier);
			SystemUtils.trace("linkedin_oauth_url", "getLinkedinCallbackParams : requestToken" + RequestToken);
			
			if(verifier != null && RequestToken != null)
			{
				getAccessToken(Authservice,RequestToken,verifier);
			}
			
		}catch(Exception e){
			SystemUtils.trace("linkedin_oauth_url", "Exception while processing : " + e);
		}
		
       
  
        
       
        
    }
	
	 public void getAccessToken(OAuthService authservice, Token RequestToken, String verifier) throws ServiceException, DataException 
	 {
		 	  	
	         Verifier v = new Verifier(verifier);
	        
	    
	         Token accessToken = authservice.getAccessToken(RequestToken, v);
	         SystemUtils.trace("linkedin_oauth_url", " OAuth Access Token"+accessToken.getToken()+" Access Token Secret "+accessToken.getSecret());
	         OAuthRequest request = new OAuthRequest(Verb.GET, "http://api.linkedin.com/v1/people/~:(first-name,last-name,positions,skills,educations)");
	         authservice.signRequest(accessToken, request); // the access token from step 4
	         Response response = request.send();
	         SystemUtils.trace("response", response.getBody());
	         
	         m_binder.putLocal("AccessToken", accessToken.getToken());
	         m_binder.putLocal("token_secret", accessToken.getSecret());
	         
	    }


    


}
