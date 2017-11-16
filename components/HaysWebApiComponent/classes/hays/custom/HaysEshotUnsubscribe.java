package hays.custom;

import static hays.com.commonutils.HaysWebApiUtils.HandleExceptions;
import static intradoc.shared.SharedObjects.getEnvironmentValue;
import hays.co.uk.HaysUtil;
import intradoc.common.LocaleUtils;
import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.data.DataException;
import intradoc.server.Service;
import intradoc.shared.SharedObjects;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;

public class HaysEshotUnsubscribe extends Service {
	public final static String TRACE_NAME = "hays_eshot_unsubscribe_call";
	String USER_AGENT = "Mozilla/5.0";
	
	public void callEshotUnsubscribeAPI() throws ServiceException, DataException, IOException {
		
		String identifier = m_binder.getLocal("identifier");
		String requestType = m_binder.getLocal("action");
		String languageId = m_binder.getLocal("languageId");
		SystemUtils.trace(TRACE_NAME, "identifier : " + identifier);
		SystemUtils.trace(TRACE_NAME, "requestType : " + requestType);
		SystemUtils.trace(TRACE_NAME, "languageId : " + languageId);
		String emailId = null;
		if(identifier != null) {
			emailId = HaysUtil.decodeBase64(identifier);
			SystemUtils.trace(TRACE_NAME, "requestType : " + emailId);
		}
		if(requestType==null){
			m_binder.putLocal("eshotEmailId", emailId);
			m_binder.putLocal("identifier", identifier);
			return;
		}
		else{
			String portalApiURL = getEnvironmentValue("PortalApiURL");
			String eshotUnSubService = getEnvironmentValue("EshotUnsubscribe");
			String eshotUnSubServiceUrl = portalApiURL + eshotUnSubService;
			SystemUtils.trace(TRACE_NAME, "eshotUnSubServiceUrl : "+eshotUnSubServiceUrl);
			String useProxy = getEnvironmentValue("PortalAPIUseProxy");
		JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put("email", emailId);
            jsonObj.put("identifier", identifier);
            jsonObj.put("languageId", languageId);
            SystemUtils.trace(TRACE_NAME, "json for post parameter : "+jsonObj.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        String responsePortal = HaysUtil.callPortalApi(eshotUnSubServiceUrl, jsonObj, useProxy);
        JSONObject responseObj = null;
		try {
			responseObj = new JSONObject(responsePortal);
			if(responseObj.getString("statuscode").equals("WC000")){
				m_binder.putLocal("unsubscribed", "Y");
				SystemUtils.trace(TRACE_NAME, "Unsubscribed Successfully");
			}
			else{
				m_binder.putLocal("unsubscribed", "N");
				SystemUtils.trace(TRACE_NAME, "Error Occurred");
			}
			
      } catch (JSONException e) {
			// TODO Auto-generated catch block
    	  SystemUtils.trace(TRACE_NAME, "JSonException ");
    	  m_binder.putLocal("unsubscribed", "N");
		  e.printStackTrace();
		}
		return;
		}
	}
}
