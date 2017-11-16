package hays.geocoder;


import hays.geocoder.Base64;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import intradoc.common.LocaleUtils;
import intradoc.common.Log;
import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.data.CallableResults;
import intradoc.data.DataException;
import intradoc.data.DataResultSet;
import intradoc.data.Parameters;
import intradoc.data.ResultSet;
import intradoc.data.Workspace;
import intradoc.provider.Provider;
import intradoc.provider.Providers;
import intradoc.server.Service;
import intradoc.shared.SharedObjects;

public class HaysGeocoder extends Service {

	private static final String GEOCODE_REQUEST_PREFIX = SharedObjects.getEnvironmentValue("GoogleGeocodeRequestUrl");
	private final static String OK = "OK";
	private final static String POST_CODE = "postal_code";
	private final static int MAX_RESULT = Integer.parseInt(SharedObjects.getEnvironmentValue("MAX_RESULT"));
	private final static boolean EXTERNALPROXTSET = Boolean.parseBoolean(SharedObjects.getEnvironmentValue("ExternalProxySet"));
	private final static String EXTERNALPROXYHOST = SharedObjects.getEnvironmentValue("ExternalProxyHost");
	private final static int EXTERNALPROXYPORT = Integer.parseInt(SharedObjects.getEnvironmentValue("ExternalProxyPort"));
	private  static String googleCryptoKey = SharedObjects.getEnvironmentValue("GoogleCryptoKey");
	private final static String googleClientId = SharedObjects.getEnvironmentValue("GoogleClientId");
	private static byte[] key;
	

	public void getGeocodesFromAddress() throws ServiceException, DataException {
		String msg = "";
		HttpURLConnection conn = null;
		Proxy proxy = null;
	
		String town = m_binder.getLocal("Town");
		String postcode = m_binder.getLocal("Postcode");
		String countryName = m_binder.getLocal("countryName");
		String resultSetName = m_currentAction.getParamAt(0);
		String providerName = m_currentAction.getParamAt(1);
		String languagecode = m_binder.getLocal("LanguageCode");
		
		SystemUtils.trace("HaysGeoCoder", " town "+town);
		SystemUtils.trace("HaysGeoCoder", " postcode "+postcode);
		SystemUtils.trace("HaysGeoCoder", " countryName "+countryName);
		SystemUtils.trace("HaysGeoCoder", " resultSetName "+resultSetName);
		SystemUtils.trace("HaysGeoCoder", " providerName "+providerName);
		SystemUtils.trace("HaysGeoCoder", " languagecode "+languagecode);
		
		
		SystemUtils.trace("HaysGeoCoder","LangCode found "+languagecode);
		String isGoogle = m_binder.getLocal("isGoogle");
		SystemUtils.trace("HaysGeoCoder","countryName found "+countryName);
		if(countryName == null || countryName.length() <= 0){
			countryName = "United Kingdom";
			SystemUtils.trace("HaysGeoCoder", "Inside country code if block ");
		}/*
		else{
			String[] countryCodeArray = countryCode.split("-");
			if(countryCodeArray.length >1){
				countryCode = countryCodeArray[1];
			}else{
				countryCode = countryCodeArray[0];
			}
		}*/
		
		
		SystemUtils.trace("HaysGeoCoder","Final countryCode found "+countryName);
		// validate the provider name
		if (providerName == null || providerName.length() == 0) {
			throw new ServiceException("You must specify a provider name.");
		}
		// validate that the provider is a valid database provider
		Provider p = Providers.getProvider(providerName);
		if (p == null) {
			throw new ServiceException("The provider '" + providerName
					+ "' does not exist.");
		} else if (!p.isProviderOfType("database")) {
			throw new ServiceException("The provider '" + providerName
					+ "' is not a valid provider of type 'database'.");
		}
		SystemUtils.trace("HaysGeoCoder", "providerName "+providerName);

		// grab the provider object that does all the work, and scope it to
		// a workspace object for database access, since we can be reasonably
		// certain at this point that the object returned is a Workspace object
		Workspace ws = (Workspace) p.getProvider();
			
		if ((town == null || town.trim() == "")
				&& (postcode == null || postcode.trim() == "")) {
			msg = LocaleUtils.encodeMessage("csHaysGeoCodesMissingParamaters",
					null);
			SystemUtils.trace("HaysGeoCoder", "town is null ");
			throw new ServiceException(msg);

		} else {

			try {
				String inputQuery, latitude, urlString, longitude = null;

				SystemUtils.trace("HaysGeoCoder","post code or town found");
				//String geocodesSelectQueryName = prop.getProperty("HAYS_GEOCODES_TABLE_SELECT_QUERY");

				String address = "";
				if (town != null && town.trim() != "") {
					address = address + town;
					SystemUtils.trace("HaysGeoCoder", "address + town "+address);
				} else {
					m_binder.putLocal("town", "");
					SystemUtils.trace("HaysGeoCoder", " town =  "+town);
				}

				if (address != "" && postcode != null && postcode != "") {
					address = address + ", " + postcode;
					SystemUtils.trace("HaysGeoCoder", " address + postcode  "+address);
				} else if (address == "" && postcode != null && postcode != "") {
					address = address + postcode;
					SystemUtils.trace("HaysGeoCoder", " address + postcode 1 "+address);
				} else {
					m_binder.putLocal("postcode", "");
					SystemUtils.trace("HaysGeoCoder", "  postcode = "+postcode);
				}

				SystemUtils.trace("HaysGeoCoder","calling google webservice");
				SystemUtils.trace("HaysGeoCoder","address = " + address);

				inputQuery = address.replaceAll(" ", "+").replaceAll(",", ",+");	
				
				SystemUtils.trace("HaysGeoCoder", "  inputQuery = "+inputQuery);
				
				countryName = countryName.replaceAll(" ", "+").replaceAll(",", ",+");
				
				SystemUtils.trace("HaysGeoCoder", "  countryName = "+countryName);

				urlString = GEOCODE_REQUEST_PREFIX + "?address="
						+ inputQuery+"+"+countryName+ "&sensor=false&client="+googleClientId+ "&language=" + languagecode;
				
				SystemUtils.trace("HaysGeoCoder", "  urlString = "+urlString);
				SystemUtils.trace("HaysGeoCoder",urlString);
				
				//  Convert the string to a URL so we can parse it
				
				URI uri = new URI(urlString);
				URL url = new URL(uri.toASCIIString());
				
				String signaturedUrl = null;
				try{
					
					String request = signRequest(url.getPath(),url.getQuery()); 
					signaturedUrl= url.getProtocol() + "://" + url.getHost() + request;
					SystemUtils.trace("HaysGeoCoder", "  signaturedUrl = "+signaturedUrl);
					
				}catch(Exception e){
					
				}
				
				if (signaturedUrl!= null){
					uri = new URI(signaturedUrl);
					url = new URL(uri.toASCIIString());
					SystemUtils.trace("HaysGeoCoder", "  url = "+url);
					
				}
				
				SystemUtils.trace("HaysGeoCoder", "url "+url);
				
				if(EXTERNALPROXTSET){
					proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(EXTERNALPROXYHOST, EXTERNALPROXYPORT) );
					SystemUtils.trace("HaysGeoCoder", "proxy "+proxy);
					conn = (HttpURLConnection) url.openConnection(proxy);
					
					SystemUtils.trace("HaysGeoCoder", "conn if "+conn);
				}else{
					conn = (HttpURLConnection) url.openConnection();
					SystemUtils.trace("HaysGeoCoder", "conn else "+conn);
				}

				Document geocoderResultDocument = null;

				// open the connection and get results as InputSource.
				conn.connect();
				InputSource geocoderResultInputSource = new InputSource(conn
						.getInputStream());
				SystemUtils.trace("HaysGeoCoder", "geocoderResultInputSource "+geocoderResultInputSource);

				// read result and parse into XML Document
				geocoderResultDocument = DocumentBuilderFactory.newInstance()
						.newDocumentBuilder().parse(geocoderResultInputSource);
				
				SystemUtils.trace("HaysGeoCoder", "geocoderResultDocument "+geocoderResultDocument);
				ResultSet rs = handleGeoXML(ws, geocoderResultDocument);
				
				m_binder.addResultSet(resultSetName,rs);
				
				
				

			} catch (Exception e) {
				msg = LocaleUtils.encodeMessage("csHaysGeoCodesUnableToFectchLatLong", null);
				m_binder.putLocal("StatusMessage",msg+" "+e.getMessage());
				Log.errorEx2(msg,msg, e);
				e.printStackTrace();
			} finally {
				if (conn != null) {
					conn.disconnect();
				}
				if(ws != null){
					ws.releaseConnection();
				}
			}
		}
	}
	
	private ResultSet getDummyResults(){
		DataResultSet rs = new DataResultSet(new String[] {"LOCATION_ID","NE_LONGITUDE","NE_LATITUDE","LEVEL_NO","LOCATION_DESC"});
		List rsList = getResultAsList("1", "-12.1234567", "43.121212", "4", "Dummy Location 1");
		rs.addRowWithList(rsList);
		rsList = getResultAsList("2", "-13.1234567", "44.121212", "5", "Dummy Location 2");
		rs.addRowWithList(rsList);
		rsList = getResultAsList("3", "-14.1234567", "44.121212", "5", "Dummy Location 3");
		rs.addRowWithList(rsList);
		return rs;
	}

	private ResultSet handleGeoXML(Workspace ws, Document xml) throws ServiceException{
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		String status = null;
		NodeList nodeList = null;
		String lat, lng, swLat, swLng, neLat, neLng, desc, postCode, type1, type2;
		DataResultSet drs = new DataResultSet(new String[] {"LOCATION_ID","NE_LATITUDE","NE_LONGITUDE","LEVEL_NO","LOCATION_DESC"});
		StringBuffer googleResults = new  StringBuffer();
		
		try {
			status = (String)xpath.evaluate("/GeocodeResponse/status/text()", xml,XPathConstants.STRING);
			if(OK.equals(status))
			{
				nodeList = (NodeList)xpath.evaluate("/GeocodeResponse/result", xml,XPathConstants.NODESET);
				int counter = MAX_RESULT;
				if(nodeList.getLength() < MAX_RESULT)
				{
					counter = nodeList.getLength();
				}
				SystemUtils.trace("HaysGeoCoder", "counter "+counter);
				for(int i=1;i<=counter;i++){
					/**/
					type1 = (String)xpath.evaluate("/GeocodeResponse/result["+i+"]/type[1]/text()", xml,XPathConstants.STRING);
					type2 = (String)xpath.evaluate("/GeocodeResponse/result["+i+"]/type[2]/text()", xml,XPathConstants.STRING);
					/**/
					postCode = (String)xpath.evaluate("/GeocodeResponse/result["+i+"]/type/text()", xml,XPathConstants.STRING);
					desc = (String)xpath.evaluate("/GeocodeResponse/result["+i+"]/formatted_address/text()", xml,XPathConstants.STRING);
					lat = (String)xpath.evaluate("/GeocodeResponse/result["+i+"]/geometry/location/lat/text()", xml,XPathConstants.STRING);
					lng = (String)xpath.evaluate("/GeocodeResponse/result["+i+"]/geometry/location/lng/text()", xml,XPathConstants.STRING);
					swLat = (String)xpath.evaluate("/GeocodeResponse/result["+i+"]/geometry/viewport/southwest/lat/text()", xml,XPathConstants.STRING);
					swLng =  (String)xpath.evaluate("/GeocodeResponse/result["+i+"]/geometry/viewport/southwest/lng/text()", xml,XPathConstants.STRING);
					neLat = (String)xpath.evaluate("/GeocodeResponse/result["+i+"]/geometry/viewport/northeast/lat/text()", xml,XPathConstants.STRING);
					neLng =(String)xpath.evaluate("/GeocodeResponse/result["+i+"]/geometry/viewport/northeast/lng/text()", xml,XPathConstants.STRING);
					
					//populate data in DB
					
					SystemUtils.trace("HaysGeoCoder", "lat "+lat);
					SystemUtils.trace("HaysGeoCoder", "lng "+lng);
					SystemUtils.trace("HaysGeoCoder", "swLat "+swLat);
					SystemUtils.trace("HaysGeoCoder", "neLat "+neLat);
					SystemUtils.trace("HaysGeoCoder", "postCode "+postCode);
					SystemUtils.trace("HaysGeoCoder", "type1 "+type1);
					SystemUtils.trace("HaysGeoCoder", "type2 "+type2);
					SystemUtils.trace("HaysGeoCoder", "desc "+desc);
					SystemUtils.trace("HaysGeoCoder", "swLng "+swLng);
					SystemUtils.trace("HaysGeoCoder", "neLng "+neLng);
					

					/**/ 

						if ("political".equalsIgnoreCase(type1) || "country".equalsIgnoreCase(type1) || "locality".equalsIgnoreCase(type1) ||"political".equalsIgnoreCase(type2) || "country".equalsIgnoreCase(type2) || "locality".equalsIgnoreCase(type2))
						{
						SystemUtils.trace("HaysGeoCoder", "Inside if block...");
					
						if(googleResults.length() == 0){
							googleResults.append(lat).append("###").append(lng).append("###").append(desc);
						}else{
							googleResults.append(":::").append(lat).append("###").append(lng).append("###").append(desc);
						}				
				}
						/**/
				if(googleResults.length() != 0)
				{ 
					drs = populateGeoData(ws,googleResults.toString());
				}
				/*SystemUtils.trace("HaysGeoCoder", "Lat long values in result set ");
				for(int i=0;i<drs.getNumRows();i++){
					SystemUtils.trace("HaysGeoCoder", " drs"+drs.getFieldName(i)+" = "+drs.getStringValue(i));
				}*/
				
			}
			}
			else
			{//status not OK
				SystemUtils.trace("HaysGeoCoder","Google geoCode Result not OK. It is "+status);
			}
			 
		} catch (XPathExpressionException ex) {
			ex.printStackTrace();
		}
		catch (DataException ex) 
		{
			ex.printStackTrace();
		}
		return drs;
	}
	
	private DataResultSet populateGeoData(Workspace ws, String googleResults) throws DataException,ServiceException{
		
		String updateLocationsQuery = m_currentAction.getParamAt(2);
		if(updateLocationsQuery == null || updateLocationsQuery.length() <= 0){
			throw new ServiceException("Error - Can not find UpdateLocationSynonymsQuery");
		}
		
		else{
			
			SystemUtils.trace("HaysGeoCoder", "updateLocationsQuery "+updateLocationsQuery);
			
			SystemUtils.trace("HaysGeoCoder", "Calling the procedure");
			
			SystemUtils.trace("HaysGeoCoder", "googleResults "+googleResults);
						
			m_binder.putLocal("googleLocations", googleResults);
		
			CallableResults results = ws.executeCallable(updateLocationsQuery, (Parameters)m_binder);
			ResultSet rsLocs = (ResultSet)results.getObject("locations_Resultset");
			SystemUtils.trace("HaysGeoCoder", "DBResults "+rsLocs);
			DataResultSet drsLocs = new DataResultSet();
			drsLocs.copyFieldInfo(rsLocs);
			drsLocs.copy(rsLocs);
			SystemUtils.trace("HaysGeoCoder", "DBResults  "+drsLocs);
			SystemUtils.trace("HaysGeoCoder", "Result set found, num rows - "+drsLocs.getNumRows());
			return drsLocs;
		}
	}
	
	private List getResultAsList(String resLocationId, String resLong, String resLat, String resLevel, String resDescription){
		List rsList = new ArrayList();
		rsList.add(resLocationId); 
		rsList.add(resLong);
		rsList.add(resLat);
		rsList.add(resLevel);
		rsList.add(resDescription);	
		SystemUtils.trace("HaysGeoCoder", "Added to list :"+resLocationId+" "+resLong+" "+resLat);
		return rsList;
	}
	
	private String getCurrentLevel(String parentLevel){
		if(parentLevel == null){
			return null;
		}
		String level = "";
		try{
			int parent = Integer.parseInt(parentLevel);
			level = new Integer(parent+1).toString(); 
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return level;
	}
	
	public static String signRequest(String path, String query) throws NoSuchAlgorithmException,InvalidKeyException, UnsupportedEncodingException, URISyntaxException,IOException {
		
		googleCryptoKey = googleCryptoKey.replace('-', '+');
		googleCryptoKey = googleCryptoKey.replace('_', '/');
		SystemUtils.trace("HaysGeoCoder","Key: " + googleCryptoKey);
	    key = Base64.decode(googleCryptoKey);
	    // Retrieve the proper URL components to sign
	    String resource = path + '?' + query;
	    
	    // Get an HMAC-SHA1 signing key from the raw key bytes
	    SecretKeySpec sha1Key = new SecretKeySpec(key, "HmacSHA1");

	    // Get an HMAC-SHA1 Mac instance and initialize it with the HMAC-SHA1 key
	    Mac mac = Mac.getInstance("HmacSHA1");
	    mac.init(sha1Key);

	    // compute the binary signature for the request
	    byte[] sigBytes = mac.doFinal(resource.getBytes());

	    // base 64 encode the binary signature
	    String signature = Base64.encodeBytes(sigBytes);
	    
	    // convert the signature to 'web safe' base 64
	    signature = signature.replace('+', '-');
	    signature = signature.replace('/', '_');
	    
	    return resource + "&signature=" + signature;

	}
}
