package hays.linkedIn_consent.org.tempuri;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;

import hays.linkedIn_consent.org.datacontract.schemas._2004._07.hays_onetouch_sangam_onlineapplication_common.CandidateLinkedInDetails;
import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.data.DataException;
import intradoc.server.ServiceHandler;
import intradoc.shared.SharedObjects;

public class LinkedInAccessConsent extends ServiceHandler
{
	public static ObjectFactory objectFactory = new ObjectFactory();
	public static hays.linkedIn_consent.org.datacontract.schemas._2004._07.hays_onetouch_sangam_onlineapplication_common.ObjectFactory objectFactoryCand = new hays.linkedIn_consent.org.datacontract.schemas._2004._07.hays_onetouch_sangam_onlineapplication_common.ObjectFactory();

	public void saveLinkedInAccessConsent() throws ServiceException, DataException, MalformedURLException
	{
		String LinkedInConsent = null, locale = null;
		SystemUtils.trace("linkedin_consent_mail", "Inside LinkedInAccessConsent saveLinkedInAccessConsent.....");
		// String locale = m_binder.getLocal("SiteLocale");
		// locale=locale.replace("-", "_");
		// String []countryFromLocale = locale.split("_");
		// String countryCode = countryFromLocale[1];

		String languagecode = m_binder.getLocal("languageCode");
		String countrycode = m_binder.getLocal("isoCountryCode");
		SystemUtils.trace("linkedin_consent_mail", "countryCode in linkedin_consent_mail countryName:::" + countrycode + languagecode);

		if ("HRBE".equalsIgnoreCase(countrycode))
		{
			countrycode = "BE";
			locale = languagecode + "_" + countrycode + "_RE";
		}
		else
		{
			locale = languagecode + "_" + countrycode;
		}

		SharedObjects.putEnvironmentValue("countryCode", countrycode);

		String endPoint = SharedObjects.getEnvironmentValue("WSDL_ENDPOINT_LINKEDIN_CONSENT_" + countrycode);

		// String
		// endPoint=SharedObjects.getEnvironmentValue("WSDL_ENDPOINT_LINKEDIN_CONSENT");

		String oneTouchId = m_binder.getLocal("onetouchId");

		String CandLinkedInAccessToken = m_binder.getLocal("AccessToken");
		String LinkedInAccessTokenSecret = m_binder.getLocal("token_secret");
		SystemUtils.trace("linkedin_consent_mail",
				"Inside LinkedInAccessConsent CandLinkedInAccessToken....." + CandLinkedInAccessToken.length());
		SystemUtils.trace("linkedin_consent_mail",
				"Inside LinkedInAccessConsent CandLinkedInAccessToken....." + LinkedInAccessTokenSecret.length());

		if ((CandLinkedInAccessToken != null && CandLinkedInAccessToken != "" && CandLinkedInAccessToken.length() != 0)
				|| (LinkedInAccessTokenSecret != null && LinkedInAccessTokenSecret != "" && LinkedInAccessTokenSecret.length() != 0))
		{
			SystemUtils.trace("linkedin_consent_mail", "Inside LinkedInAccessConsent if .....");
			LinkedInConsent = "Full";
		}
		else
		{
			LinkedInConsent = "Declined";
		}

		SystemUtils.trace("linkedin_consent_mail", "saveLinkedInAccessConsent.....endPoint" + endPoint);
		SystemUtils.trace("linkedin_consent_mail", "saveLinkedInAccessConsent.....oneTouchId" + oneTouchId);
		SystemUtils.trace("linkedin_consent_mail", "saveLinkedInAccessConsent.....locale" + locale);
		SystemUtils.trace("linkedin_consent_mail", "saveLinkedInAccessConsent.....CandLinkedInAccessToken" + CandLinkedInAccessToken);
		SystemUtils.trace("linkedin_consent_mail", "saveLinkedInAccessConsent.....LinkedInAccessTokenSecret" + LinkedInAccessTokenSecret);
		SystemUtils.trace("linkedin_consent_mail", "saveLinkedInAccessConsent.....LinkedInConsent" + LinkedInConsent);

		m_binder.putLocal("consent", LinkedInConsent);

		try
		{
			System.setProperty("http.proxyUser", "");
			System.setProperty("http.proxyPassword", "");
			System.setProperty("http.proxyHost", "");
			System.setProperty("http.proxyPort", "");
			CandidateLinkedInDetails candidateLinkedInDetails = new CandidateLinkedInDetails();
			candidateLinkedInDetails.setCandLinkedInAccessToken(objectFactoryCand
					.createCandidateLinkedInDetailsCandLinkedInAccessToken(CandLinkedInAccessToken));
			candidateLinkedInDetails.setLinkedInAccessTokenSecret(objectFactoryCand
					.createCandidateLinkedInDetailsLinkedInAccessTokenSecret(LinkedInAccessTokenSecret));
			candidateLinkedInDetails.setLinkedInConsent(objectFactoryCand.createCandidateLinkedInDetailsLinkedInConsent(LinkedInConsent));

			CandidateOnlineService candidateOnlineServiceClient = new CandidateOnlineService(new URL(endPoint), new QName(
					"http://tempuri.org/", "CandidateOnlineService"));
			ICandidateOnlineService iCandidateOnlineService = candidateOnlineServiceClient.getBasicHttpBindingICandidateOnlineService();
			SystemUtils.trace("linkedin_consent_mail", "saveLinkedInAccessConsent.....LinkedInConsent inside try block");

			iCandidateOnlineService.updateCandidateLinkedInAccessToken(oneTouchId, locale, candidateLinkedInDetails);

			// System.clearProperty("https.proxyUser");
			// System.clearProperty("https.proxyPassword");
			//
			// System.clearProperty("https.proxyHost");
			// System.clearProperty("https.proxyPort");

		}
		catch (Exception e)
		{
			SystemUtils.trace("linkedin_consent_mail", "in Catch block status Exception is  " + e);
			SystemUtils.dumpException("linkedin_consent_mail", e);
		}

	}
}