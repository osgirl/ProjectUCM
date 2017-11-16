package hays.co.uk;

import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.shared.SharedObjects;

import java.io.UnsupportedEncodingException;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URL;

import static intradoc.shared.SharedObjects.getEnvironmentValue;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.codec.binary.Base64;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.json.JSONException;
import org.json.JSONObject;

public class HaysUtil {
	public static final Pattern LOCALE_REGEX = Pattern.compile("(.+)[-_](.+)");
	private final static boolean EXTERNALPROXTSET = Boolean.parseBoolean(SharedObjects.getEnvironmentValue("ExternalProxySet"));
	private final static String EXTERNALPROXYHOST = SharedObjects.getEnvironmentValue("ExternalProxyHost");
	private final static int EXTERNALPROXYPORT = Integer.parseInt(SharedObjects.getEnvironmentValue("ExternalProxyPort"));
	private final static String EXTERNALPROXYUSER = SharedObjects.getEnvironmentValue("ExternalProxyUser");
	private final static String EXTERNALPROXYPASSWORD = SharedObjects.getEnvironmentValue("ExternalProxyUserPassword");
	public static Locale getLocale(String ucmLocale){
		String lang="en", country = "GB";
		Locale locale = null;
		if( ucmLocale != null && ucmLocale.trim().length() > 0){
			Matcher matcher = LOCALE_REGEX.matcher(ucmLocale);
			if (matcher.find() && matcher.groupCount() > 1){
	      		lang = matcher.group(1);
	    		country = matcher.group(2);
			}
		} 
		locale = new Locale(lang, country);
		return locale;
	}
	
	public static String getLanguageFromLocale(String locale){
		if(locale == null){
			return null;
		}
		String lang = null;
		Matcher matcher = LOCALE_REGEX.matcher(locale);
	    if (matcher.find() && matcher.groupCount() > 1) {
	    	lang = matcher.group(1);
	    }
		return lang;
	}
	//Method to get Hmac hash(Java)
	public static String generate(String secret, String data) {
        try {
            SecretKeySpec signingKey = new SecretKeySpec(secret.getBytes("UTF-8"), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(data.getBytes("UTF-8"));
            return new String(Base64.encodeBase64(rawHmac));
        } catch (Exception e) {
            throw new IllegalArgumentException();
        }
    }
	
	//Common Method to set Proxy values
		public static void setProxy() {
			String PROXY_AUTH_PASSWORD = getEnvironmentValue("PROXY_AUTH_PASSWORD");
			String PROXY_AUTH_USER = getEnvironmentValue("PROXY_AUTH_USER");
			String PROXY_HOST = getEnvironmentValue("PROXY_HOST");
			String PROXY_PORT = getEnvironmentValue("PROXY_PORT");
			System.setProperty("http.proxyUser", PROXY_AUTH_USER);
			System.setProperty("http.proxyPassword", PROXY_AUTH_PASSWORD);
			System.setProperty("http.proxyHost", PROXY_HOST);
			System.setProperty("http.proxyPort", PROXY_PORT);	
			SystemUtils.trace("hays_util", "http.proxyUser : " + PROXY_AUTH_USER + " http.proxyPassword : " + PROXY_AUTH_PASSWORD + " http.proxyHost : " + PROXY_HOST + " http.proxyPort : " + PROXY_PORT);
			SystemUtils.trace("hays_util", "Setting Proxy");
	    }
		
		
	//Common Method to remove Proxy values
		public static void removeProxy() {
			SystemUtils.trace("hays_util", "http.proxyUser before remove: " + System.getProperty("http.proxyUser") + " proxyPassword : " + System.getProperty("http.proxyPassword") +" proxyHost : "+System.getProperty("http.proxyHost") + " proxyPort : " +System.getProperty("http.proxyPort"));
			System.getProperties().remove("http.proxyUser");
			System.getProperties().remove("http.proxyPassword");
			System.getProperties().remove("http.proxyHost");
			System.getProperties().remove("http.proxyPort");
		        
	    }
	
	 public static 	 String getRequestTime() {
		 String date=null;
		 try {
	            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
	            format.setTimeZone(TimeZone.getTimeZone("GMT"));
	            date = format.format(new Date());
	
	        } catch (Exception e) {            
	        }
	        return date;
	    }
	
	 public static String getDigest(String requestURI,String date, String requestMethod, String inputJson, String contentType) throws Exception{
		  SystemUtils.trace("hays_util_Hmac", "contentType"+contentType);
		    SystemUtils.trace("hays_util_Hmac","requestURI "+requestURI);
		    SystemUtils.trace("hays_util_Hmac","date "+date);
		    SystemUtils.trace("hays_util_Hmac","requestMethod "+requestMethod);
		    SystemUtils.trace("hays_util_Hmac","inputJson "+inputJson);
		    SystemUtils.trace("hays_util_Hmac","contentType "+contentType);
		    //String base64 = null;
		    //if(inputJson!=null)
		    //base64 = new String(Base64.encodeBase64(inputJson.getBytes()));
		    
		    //SystemUtils.trace("hays_util_Hmac", "base64"+base64);
		    SystemUtils.trace("hays_util_Hmac", "contentType"+contentType);
		    SystemUtils.trace("hays_util_Hmac","requestURI "+requestURI);

		    // calculate content to sign
		    StringBuilder toSign =	new StringBuilder();
		    toSign.append(requestMethod).append("\n")
		            //.append(base64).append("\n")
		            .append(contentType).append("\n")
		            .append(date).append("\n")
		            .append(requestURI);

		    
		    SystemUtils.trace("hays_util_Hmac",  "String sent = "+toSign.toString());

		    String digest = generate(getEnvironmentValue("HMAC_API_SECRET"), toSign.toString());
		    SystemUtils.trace("hays_util_Hmac","digest... "+digest);
		    
		    return digest;
		}

	 
	 
	public static String getCountryFromLocale(String locale){
		if(locale == null){
			return null;
		}
		String lang = null;
		Matcher matcher = LOCALE_REGEX.matcher(locale);
	    if (matcher.find() && matcher.groupCount() > 1) {
	    	lang = matcher.group(0);
	    }
		return lang;
	}
	public static void debug(Exception message) {
		SystemUtils.trace("hays_exception",  "Exception: " + message);
	}
	
	public static void trace(String message) {
		SystemUtils.trace("hays_general",  message);
	}
	public static boolean getTikaCode(String fileLocation){
		boolean isValid=false;		
		
		if ( fileLocation != null )
			SystemUtils.trace("HaysUtils", "filelocation check.................");	
		{

	        //FileInputStream fileInputStream=null;

	                        File fileLoc = new File(fileLocation);

	        try {

	        	

	            TikaConfig config = TikaConfig.getDefaultConfig();

	            TikaInputStream tikaIS = null;
     
	           	        
	            try {
	            	SystemUtils.trace("HaysUtils", "inside try...............");	
	                tikaIS = TikaInputStream.get(fileLoc);

	                final Metadata metadata = new Metadata();
	                MediaType mimetype = config.getDetector().detect(TikaInputStream.get(fileLoc), metadata);
	                SystemUtils.trace("HaysMail","mimeType : " +mimetype);
	                if(!((mimetype.toString().equalsIgnoreCase("application/pdf")) || (mimetype.toString().equalsIgnoreCase("application/rtf")) || (mimetype.toString().equalsIgnoreCase("application/octet-stream")) || (mimetype.toString().equalsIgnoreCase("application/x-tika-msoffice")) || (mimetype.toString().equalsIgnoreCase("application/x-tika-ooxml"))) ){
	               
	                	SystemUtils.trace("HaysUtils","mimeType : for false value " );
	                	isValid=false;
                    }else{
                    	SystemUtils.trace("HaysUtils","mimeType : for true value " );
                    	 isValid=true;                    	
                    }

	            } finally {

	                if (tikaIS != null) {
	                	SystemUtils.trace("HaysUtils", "mimeType close");
	                    tikaIS.close();

	                }

	            }

	        }catch(IOException e){
	        	
	            e.printStackTrace();

	        }

	    }
		return isValid; 
	}
	public static String escapeHTML(String s) {
	    StringBuilder out = new StringBuilder(Math.max(16, s.length()));
	    for (int i = 0; i < s.length(); i++) {
	        char c = s.charAt(i);
	        if (c == '<' || c == '>' || c == '&' || c == '"' || c=='\'') {
	            out.append("&#");
	            out.append((int) c);
	            out.append(';');
	        } else {
	            out.append(c);
	        }
	    }
	    return out.toString();
	}
	
	public static String decodeBase64(String encodedStr){
		SystemUtils.trace("HaysUtils","Decoding string ..." );
		String email = null;
		byte[] bytes = Base64.decodeBase64(encodedStr);
		String decoder = null;
		try {
			decoder = new String(bytes, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			SystemUtils.trace("HaysUtils","Decoding failed " + e.getMessage() );
		}
		String[] params = decoder.split("&");
		for (String param : params) {
			String name = param.split("=")[0];
			if (name.equalsIgnoreCase("email")) {
				email = param.split("=")[1];
				SystemUtils.trace("HaysUtils","Decoded String : "+ email );
			}
		}
		return email;
	}
	
	public static String callPortalApi(String apiUrl, JSONObject param, String useProxy) throws MalformedURLException, IOException
	{
		boolean isProxy = false;
		if (useProxy != null && "TRUE".equalsIgnoreCase(useProxy))
		{
			isProxy = true;
			CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
			String PROXY_HOST = getEnvironmentValue("PROXY_HOST");
			String PROXY_PORT = getEnvironmentValue("PROXY_PORT");
			final String authUser = getEnvironmentValue("PROXY_AUTH_USER");
			final String authPassword = getEnvironmentValue("PROXY_AUTH_PASSWORD");
			Authenticator.setDefault(new Authenticator()
			{
				public PasswordAuthentication getPasswordAuthentication()
				{
					return new PasswordAuthentication(authUser, authPassword.toCharArray());
				}
			});
			System.setProperty("http.proxyUser", authUser);
			System.setProperty("http.proxyPassword", authPassword);
			System.setProperty("http.proxyHost", PROXY_HOST);
			System.setProperty("http.proxyPort", PROXY_PORT);
			System.setProperty("http.agent", "Mozilla/5.0 (Windows NT 5.1; rv:12.0) Gecko/20100101 Firefox/12.0");
			SystemUtils.trace("HaysUtils", "http.proxyUser : " + authUser);
			SystemUtils.trace("HaysUtils", "http.proxyPassword : " + authPassword);
			SystemUtils.trace("HaysUtils", "http.proxyHost : " + PROXY_HOST);
			SystemUtils.trace("HaysUtils", "http.proxyPort : " + PROXY_PORT);
		}
		else
		{
			SystemUtils.trace("HaysUtils", "http.proxyUser before remove: " + System.getProperty("http.proxyUser"));
			SystemUtils.trace("HaysUtils", "http.proxyPassword before remove: " + System.getProperty("http.proxyPassword"));
			SystemUtils.trace("HaysUtils", "http.proxyHost before remove: " + System.getProperty("http.proxyHost"));
			SystemUtils.trace("HaysUtils", "http.proxyPort before remove: " + System.getProperty("http.proxyPort"));
			System.getProperties().remove("http.proxyUser");
			System.getProperties().remove("http.proxyPassword");
			System.getProperties().remove("http.proxyHost");
			System.getProperties().remove("http.proxyPort");
		}
		SystemUtils.trace("HaysUtils", "isProxy : " + isProxy);
        URL object= new URL(apiUrl);
		HttpURLConnection conn = null;  
        if (isProxy)
		{
			conn = (HttpURLConnection) object.openConnection();     //url.openConnection();
		}
		else
		{
			conn = (HttpURLConnection) object.openConnection(Proxy.NO_PROXY);     //url.openConnection(Proxy.NO_PROXY);
		}
		SystemUtils.trace("HaysUtils", "Connection : " + conn);
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        
        conn.setRequestMethod("POST");
        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
        wr.writeBytes(param.toString());
        wr.flush();
        wr.close();
        int responseCode = conn.getResponseCode();
        SystemUtils.trace("HaysUtils", "Post url : " + apiUrl); 
        SystemUtils.trace("HaysUtils", "Post parameters : " + param.toString());
        SystemUtils.trace("HaysUtils", "Response Code : " + responseCode);
        BufferedReader in = new BufferedReader(new InputStreamReader(
                conn.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer(); 
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close(); 
        // print result
        SystemUtils.trace("HaysUtils", "Final Response Code : " + response.toString());
		return response.toString();
	}
	
	public static boolean verify(String gRecaptchaResponse, String url, String USER_AGENT, String secret, String apacCheck1) throws ServiceException {
	   	
	    SystemUtils.trace("HaysUtils", "apacCheck : " + apacCheck1);
		if (gRecaptchaResponse == null || "".equals(gRecaptchaResponse)) {
            return false;
        }
         
        try{
        URL obj = new URL(url); 
        SystemUtils.trace("HaysUtils", "URL =  "+obj);
        HttpsURLConnection conn = null;
        Proxy proxy = null;  
        if ("true".equalsIgnoreCase(apacCheck1))
        {
        String sslCaptchaURL= SharedObjects.getEnvironmentValue("sslCaptcha");	
        SystemUtils.trace("HaysUtils", "check ssl value "+sslCaptchaURL);
        System.setProperty("javax.net.ssl.trustStore",sslCaptchaURL);
        
        //System.setProperty("javax.net.ssl.trustStore","/shared_UCM/oracle/oat/oat_consump/ucm_cluster/cs/data/providers/sslincomingprovider/websweeper.hays.com.au.jks");
        SystemUtils.trace("HaysUtils", "Inside if for APAC check ");
        }
        if(EXTERNALPROXTSET){
					proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(EXTERNALPROXYHOST, EXTERNALPROXYPORT) );
					SystemUtils.trace("HaysUtils", "proxy "+proxy);
					conn = (HttpsURLConnection) obj.openConnection(proxy);
					final String authUser = EXTERNALPROXYUSER;
					final String authPassword = EXTERNALPROXYPASSWORD;
					SystemUtils.trace("HaysUtils", "SauthUser : " + authUser);
					SystemUtils.trace("HaysUtils", "authPassword : " + authPassword);
					Authenticator.setDefault(new Authenticator() {
				    public PasswordAuthentication getPasswordAuthentication() {
				    	
					return new PasswordAuthentication(authUser, authPassword.toCharArray());
				    	
				}
				  
			});
			SystemUtils.trace("HaysUtils", "conn if "+conn);

					
					
				}else{
					conn = (HttpsURLConnection) obj.openConnection();
					SystemUtils.trace("HaysUtils", "conn else "+conn);
				}
 
        // add reuqest header
        conn.setRequestMethod("POST");
        conn.setRequestProperty("User-Agent", USER_AGENT);
        conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
 
        String postParams = "secret=" + secret + "&response="
                + gRecaptchaResponse;
        SystemUtils.trace("HaysUtils", "Sending postParams request to URL : " + postParams);
        // Send post request
        conn.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
        wr.writeBytes(postParams);
        wr.flush();
        wr.close();
        SystemUtils.trace("HaysUtils", "DoOutput : ");
        int responseCode = conn.getResponseCode();
        SystemUtils.trace("HaysUtils", "Sending 'POST' request to URL : " + url);
        SystemUtils.trace("HaysUtils", "Post parameters : " + postParams);
        SystemUtils.trace("HaysUtils", "Response Code : " + responseCode);
 
        BufferedReader in = new BufferedReader(new InputStreamReader(
                conn.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer(); 
        
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close(); 
 
        // print result
        SystemUtils.trace("HaysUtils", "Final Response Code : " + response.toString());
             
  
        JSONObject jsonObj= new JSONObject(response.toString());
        SystemUtils.trace("HaysUtils", "Json Object : " + jsonObj);
        boolean responseData = false;
        if(jsonObj.has("success")){
	        responseData = jsonObj.getBoolean("success");
	        SystemUtils.trace("HaysUtils", "respdata : " + responseData);
        }
        return responseData;
     /*   //parse JSON response and return 'success' value
        JsonReader jsonReader = Json.createReader(new StringReader(response.toString()));
        SystemUtils.trace("HaysMail", "JsonReader : " + jsonReader);
       // boolean JsonObject jsonObject =new Integer(1).equals(jsonReader.readObject());
        JsonParser parser = new JsonParser(); 
        JsonObject jsonObject = (JsonObject) parser.parse((Reader) jsonReader.readObject());
        SystemUtils.trace("HaysMail", "JsonObject : " + jsonObject); 
        jsonReader.close();
         
        return jsonObject.getBoolean("success");*/
        }catch(Exception e){
        	SystemUtils.trace("HaysUtils", "Error message = " + e.toString());
            e.printStackTrace();
            return false;
        }
    }

}
