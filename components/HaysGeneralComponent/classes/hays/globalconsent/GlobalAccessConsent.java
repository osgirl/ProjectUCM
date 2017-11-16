package hays.globalconsent;

import hays.linkedIn_consent.org.datacontract.schemas._2004._07.hays_onetouch_sangam_onlineapplication_common.CandidateLinkedInDetails;
import hays.linkedIn_consent.org.tempuri.CandidateConsentDetails;
import hays.linkedIn_consent.org.tempuri.CandidateOnlineService;
import hays.linkedIn_consent.org.tempuri.ICandidateOnlineService;


import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;

import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.data.DataException;
import intradoc.server.ServiceHandler;
import intradoc.shared.SharedObjects;

public class GlobalAccessConsent extends ServiceHandler
{
	public static ObjectFactory objectFactory = new ObjectFactory();
	public static hays.linkedIn_consent.org.datacontract.schemas._2004._07.hays_onetouch_sangam_onlineapplication_common.ObjectFactory objectFactoryCand = new hays.linkedIn_consent.org.datacontract.schemas._2004._07.hays_onetouch_sangam_onlineapplication_common.ObjectFactory();
	public void saveGlobalAccessConsent() throws ServiceException, DataException, MalformedURLException{
		String GlobalConsent = null, locale = null, consentSource = null, consentResponse=null;
		SystemUtils.trace("global_consent_mail", "Inside GlobalAccessConsent saveGlobalAccessConsent.....");
		String languagecode = m_binder.getLocal("LanguageCode");
		String countrycode = m_binder.getLocal("isoCountryCode");
		SystemUtils.trace("global_consent_mail", "countryCode in global_consent_mail countryName:::" + countrycode + languagecode);

		if ("HRBE".equalsIgnoreCase(countrycode)){
			countrycode = "BE";
			locale = languagecode + "_" + countrycode + "_RE";
		}
		else{
			locale = languagecode + "_" + countrycode;
		}
		SharedObjects.putEnvironmentValue("countryCode", countrycode);
		String endPoint = SharedObjects.getEnvironmentValue("WSDL_ENDPOINT_GLOBAL_CONSENT_" + countrycode);
		SystemUtils.trace("global_consent_mail","Endpoint Value = " + endPoint);
		
		String oneTouchId = m_binder.getLocal("onetouchId");
		SystemUtils.trace("global_consent_mail","One Touch ID = " + oneTouchId);
		

		String CandGlobalAccessToken = m_binder.getLocal("CandAccessToken");
		String GlobalAccessTokenSecret = m_binder.getLocal("AccessTokenSecret");
		SystemUtils.trace("global_consent_mail",
				"Inside GlobalAccessConsent CandGlobalAccessToken....." + CandGlobalAccessToken.length());
		SystemUtils.trace("global_consent_mail",
				"Inside GlobalAccessConsent CandGlobalAccessToken....." + GlobalAccessTokenSecret.length());
		SystemUtils.trace("global_consent_mail", "saveGlobalAccessConsent.....endPoint" + endPoint);
		SystemUtils.trace("global_consent_mail", "saveGlobalAccessConsent.....oneTouchId" + oneTouchId);
		SystemUtils.trace("global_consent_mail", "saveGlobalAccessConsent.....locale" + locale);
		SystemUtils.trace("global_consent_mail", "saveGlobalAccessConsent.....CandGlobalAccessToken" + CandGlobalAccessToken);
		SystemUtils.trace("global_consent_mail", "saveGlobalAccessConsent.....GlobalAccessTokenSecret" + GlobalAccessTokenSecret);
		
		System.setProperty("http.proxyUser", "");
		System.setProperty("http.proxyPassword", "");
		System.setProperty("http.proxyHost", "");
		System.setProperty("http.proxyPort", "");
		
		GlobalConsent = "Y";
		consentSource = "GLOBAL";
		m_binder.putLocal("consent", GlobalConsent);
		m_binder.putLocal("source", consentSource);
		SystemUtils.trace("global_consent_mail", "saveGlobalAccessConsent.....GlobalConsent" + GlobalConsent);
		SystemUtils.trace("global_consent_mail", "saveGlobalAccessConsent.....consentSource" + consentSource);
		CandidateConsentDetails candidateConsentDetails = new CandidateConsentDetails();
		candidateConsentDetails.setCandAccessToken(objectFactory
				.createCandidateConsentDetailsCandAccessToken(CandGlobalAccessToken));
		candidateConsentDetails.setAccessTokenSecret(objectFactory
				.createCandidateConsentDetailsAccessTokenSecret(GlobalAccessTokenSecret));
		candidateConsentDetails.setCandConsent(objectFactory.createCandidateConsentDetailsCandConsent(GlobalConsent));

		CandidateOnlineService candidateOnlineServiceClient = new CandidateOnlineService(new URL(endPoint), new QName(
				"http://tempuri.org/", "CandidateOnlineService"));
		ICandidateOnlineService iCandidateOnlineService = candidateOnlineServiceClient.getBasicHttpBindingICandidateOnlineService();
		SystemUtils.trace("global_consent_mail", "saveGlobalAccessConsent.....GlobalConsent inside try block");

		consentResponse = iCandidateOnlineService.updateCandidateConsentAccessToken(oneTouchId, locale, candidateConsentDetails,consentSource);
		SystemUtils.trace("global_consent_mail", "saveGlobalAccessConsent.....Global Consent"+ consentResponse);
		m_binder.putLocal("consentResponse", consentResponse);
		
		
	}
}

