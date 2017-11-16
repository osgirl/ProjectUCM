package com.tcs.checks;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

import oracle.stellent.ridc.IdcClient;
import oracle.stellent.ridc.IdcClientException;
import oracle.stellent.ridc.IdcClientManager;
import oracle.stellent.ridc.IdcContext;
import oracle.stellent.ridc.model.DataBinder;
import oracle.stellent.ridc.protocol.ServiceResponse;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class Checks {
	private static StringBuffer threadCounts = new StringBuffer();
	private static Properties config = new Properties();
	static{
		try {
			config.load(Checks.class.getClassLoader().getResourceAsStream("config.properties"));
		} catch (IOException e) {
			System.out.println("File 'config.properties' is not available on classpath.");
			e.printStackTrace();
		}
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		
		Checks checks = new Checks();
		String envType = config.getProperty(Constants.KEY_INPUT_ENV_TYPE);
		String envTypes[]=envType.split(",");
		String regionType = config.getProperty(Constants.KEY_INPUT_REGION_TYPE);
		String regionTypes[]=regionType.split(",");
		
		String threadEnvType = config.getProperty(Constants.KEY_INPUT_THREAD_ENV_TYPE);
		String threadEnvTypes[]=threadEnvType.split(",");
		String threadRegionType = config.getProperty(Constants.KEY_INPUT_THREAD_REGION_TYPE);
		String threadRegionTypes[]=threadRegionType.split(",");
		
		String urlsForStatus = config.getProperty(Constants.KEYWORD_URLS_FOR_STATUS);
		String urlForStatus[]=urlsForStatus.split(",");
		try{
			for(int i=0;i<threadEnvTypes.length;i++)
			{
				for(int j=0;j<threadRegionTypes.length;j++)
				{
					String threadServer = config.getProperty(Constants.KEY_INPUT_THREAD_SERVERS);
					String threadServers[]=threadServer.split(",");
					for(int k=0;k<threadServers.length;k++)
					{
						checks.getThreadCounts(threadEnvTypes[i], threadRegionTypes[j], threadServers[k]);
					}
						
				}
			}
			threadCounts.append("\n\n\n\n");
			
			
			for(int i=0;i<envTypes.length;i++)
			{
				for(int j=0;j<regionTypes.length;j++)
				{
					String servers = config.getProperty(checks.buildConfigKey(envTypes[i], regionTypes[j], Constants.KEY_INPUT_SERVERS));
					String server[]=servers.split(",");
					String serverTypes = config.getProperty(checks.buildConfigKey(envTypes[i], regionTypes[j], Constants.KEY_INPUT_SERVER_TYPE));
					String serverType[]=serverTypes.split(",");
					for(int k=0;k<server.length;k++)
					{
						checks.getArchieverCount(envTypes[i], regionTypes[j], server[k], serverType[k]);
						threadCounts.append("\n\n\n\n");
					}
				}
			}
			
			
			
			for(int i=0;i<urlForStatus.length;i++)
			{
				checks.getResponseCode(urlForStatus[i]);
			}
			
			System.out.println(threadCounts);
		
		}catch(Exception e){
			System.out.println("Error occured during local log directory clean-up: "+e.getMessage());
		}

	}

	public void getArchieverCount(String envType, String regionType, String server, String serverType)throws Exception{
		
		System.out.println("inside getArchieverCount... envType :: "+envType+" regionType ::"+regionType+" server ::"+server+" serverType ::"+serverType);
		if(serverType.equalsIgnoreCase("linux"))
		{
			JSch jsch = new JSch();
	        Session session = null;
	        try {
	        	String remoteHost = config.getProperty(buildConfigKey(envType, regionType, server, Constants.KEY_SUFFIX_REMOTE_HOST));
	        	String remoteUser = config.getProperty(buildConfigKey(envType, regionType, server, Constants.KEY_SUFFIX_REMOTE_USER));
	        	String remotePassword = config.getProperty(buildConfigKey(envType, regionType, server, Constants.KEY_SUFFIX_REMOTE_PASSWORD));
	
	        	
	        	System.out.println("Connecting to host... "+remoteHost+" remoteUser ::"+remoteUser+" remotePassword ::"+remotePassword);
	            session = jsch.getSession(remoteUser, remoteHost, 22);
	            session.setConfig("StrictHostKeyChecking", "no");
	            session.setPassword(remotePassword);
	            session.connect(); 
	            
	            System.out.println("Connected.....");
	            
	            
	            fetchArchieverCount(session, envType, regionType, server);
	        }
	        catch(Exception e){
				System.out.println("Error occured while connecting : "+e.getMessage());
	        	
	        }
	        finally{
	        	if(session!=null)
	        		session.disconnect();
	        }
		}
		else
		{
			fetchArchieverCount(envType, regionType, server);
		}
	}
	
	public String buildConfigKey(String envType, String regionType, String keyName){
		StringBuilder keyBuilder = new StringBuilder().append(envType).append('.').append(regionType)
				.append('.').append(keyName);
		System.out.println(keyBuilder.toString());
		return keyBuilder.toString();
	}
	
	public String buildConfigKey(String envType, String regionType, String server, String keyName){
		StringBuilder keyBuilder = new StringBuilder().append(envType).append('.').append(regionType)
				.append('.').append(server).append('.').append(keyName);
		System.out.println(keyBuilder.toString());
		return keyBuilder.toString();
	}
	
	private void fetchArchieverCount(Session session, String envType, String regionType, String server){
		System.out.println("inside fetchArchieverCount... envType :: "+envType+" regionType ::"+regionType+" server ::"+server);
		try{

			String archievers = config.getProperty(buildConfigKey(envType, regionType, server, Constants.KEY_SUFFIX_ARCHIEVERS));
			String archieversPath = config.getProperty(buildConfigKey(envType, regionType, server, Constants.KEY_SUFFIX_ARCHIEVERS_PATH));
			String archieverCountCmd = config.getProperty(Constants.KEY_SUFFIX_ARCHIEVERS_COUNT_CMD);
			System.out.println("inside fetchArchieverCount... archievers :: "+archievers+" archieversPath ::"+archieversPath+" archieverCountCmd ::"+archieverCountCmd);
			String archieversList[]=archievers.split(",");
			
			for(int i=0;i<archieversList.length;i++)
			{
				
				String cmd = archieverCountCmd.replace(Constants.KEYWORD_ARCHIEVER_PATH, archieversPath+archieversList[i]);
				System.out.println("command : "+cmd);
				Channel channel = session.openChannel("exec");
				ChannelExec channelExec = (ChannelExec) channel;
				channelExec.setCommand(cmd);
				channelExec.setErrStream(System.err);
				channelExec.setInputStream(null);
				channelExec.connect();

				InputStream in =channelExec.getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(in));
				String tmp;
				System.out.println("Executing find command to get the list of modified files: "+cmd);
				while ((tmp= reader.readLine()) != null) {
//					System.out.println("Archiever Count for archiver "+archieversList[i]+ " for " +regionType+" "+envType+ " is : "+ tmp);
					threadCounts.append("Archiever Count for archiver "+archieversList[i]+ " for " +regionType+" "+envType+ " "+server+" is : "+ tmp+"\n");
	            }
				
				channelExec.disconnect();
			}
			
		}catch(Exception e){
			System.out.println("Error in fetching larchiever counts: "+e.getMessage());
		}
	}
	
	private void fetchArchieverCount(String envType, String regionType, String server){
		System.out.println("inside fetchArchieverCount... envType :: "+envType+" regionType ::"+regionType+" server ::"+server);
		try{

			String archievers = config.getProperty(buildConfigKey(envType, regionType, server, Constants.KEY_SUFFIX_ARCHIEVERS));
			String archieversPath = config.getProperty(buildConfigKey(envType, regionType, server, Constants.KEY_SUFFIX_ARCHIEVERS_PATH));
			String archieverCountCmd = config.getProperty(buildConfigKey(envType, regionType, Constants.KEY_SUFFIX_ARCHIEVERS_COUNT_CMD));
			System.out.println("inside fetchArchieverCount... archievers :: "+archievers+" archieversPath ::"+archieversPath+" archieverCountCmd ::"+archieverCountCmd);
			String archieversList[]=archievers.split(",");
			
			for(int i=0;i<archieversList.length;i++)
			{
				
				System.out.println("File Path "+archieversPath+archieversList[i]+"/exports.hda");
				
				BufferedReader br = new BufferedReader(new FileReader(archieversPath+archieversList[i]+"/exports.hda"));
				
				String sCurrentLine;
				int count=0;

			        // read lines until reaching the end of the file
			        while ((sCurrentLine = br.readLine()) != null) 
			        {
			                // extract the words from the current line in the file
			                if (sCurrentLine.contains(".hda"))
			                {
			                	count++;
			                }
			        }
				
				threadCounts.append("Archiever Count for archiver "+archieversList[i]+ " for " +regionType+" "+envType+ " "+server+" is : "+ count+"\n");
				
				
				
			}
			
			
			
			
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("Error in fetching windows machine: "+e.getMessage());
		}
	}
	
	public void getThreadCounts(String threadEnvType, String threadRegionType, String threadServer)
	{
		// create the IDC Client Manager manager
		IdcClientManager manager = new IdcClientManager();
		 
		// build a client that will communicate using the HTTP protocol
		IdcClient idcClient;
		 
		try {
		//context with ucm username and password
		IdcContext user = new IdcContext ("weblogic", "qwert54321");
		//ucm link
		idcClient = manager.createClient(config.getProperty(buildConfigKey(threadEnvType, threadRegionType, threadServer, Constants.KEY_INPUT_IDC_URL)));
		// get the binder
		DataBinder binder = idcClient.createBinder();
		// populate the binder with the parameters
		binder.putLocal ("IdcService", "GET_SYSTEM_AUDIT_INFO");
		try {
		ServiceResponse response = idcClient.sendRequest(user, binder);
        BufferedReader reader = new BufferedReader(new InputStreamReader(response.getResponseStream()));
        String tmp;
        String threadCount="";
        while ((tmp= reader.readLine()) != null) {
        	threadCount=tmp.replaceAll("numThreads=", "");
        	if(tmp.contains("numThreads="))break;
        }
        reader.close();
        
		threadCounts.append("Thread Count for " +threadRegionType+" "+threadEnvType+ " "+threadServer+" is : "+ threadCount+"\n");
		} catch (IdcClientException e) {
		e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		} catch (IdcClientException e) {
		e.printStackTrace();
		}
	}
	
	public void getResponseCode(String pURL)
    {
		try{
		CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
		
		String PROXY_HOST =config.getProperty(Constants.KEYWORD_PROXY_HOST);
		String PROXY_PORT = config.getProperty(Constants.KEYWORD_PROXY_PORT);
		
		final String authUser = config.getProperty(Constants.KEYWORD_AUTH_USER);
		final String authPassword = config.getProperty(Constants.KEYWORD_AUTH_PASSWORD);
		
		Authenticator.setDefault(new Authenticator()
		{
			public PasswordAuthentication getPasswordAuthentication()
			{
				return new PasswordAuthentication(authUser, authPassword.toCharArray());
			}
		});
		System.setProperty("https.proxyUser", authUser);
		System.setProperty("https.proxyPassword", authPassword);
		System.setProperty("https.proxyHost", PROXY_HOST);
		System.setProperty("https.proxyPort", PROXY_PORT);
		System.setProperty("https.agent", "Mozilla/5.0 (Windows NT 5.1; rv:12.0) Gecko/20100101 Firefox/12.0");
		
		System.setProperty("http.proxyUser", authUser);
		System.setProperty("http.proxyPassword", authPassword);
		System.setProperty("http.proxyHost", PROXY_HOST);
		System.setProperty("http.proxyPort", PROXY_PORT);
		System.setProperty("http.agent", "Mozilla/5.0 (Windows NT 5.1; rv:12.0) Gecko/20100101 Firefox/12.0");
		System.out.println(pURL);	
		
		URL url = new URL(null, pURL, new sun.net.www.protocol.http.Handler());
		URLConnection conn = url.openConnection();
		conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.setUseCaches(false);
		conn.setRequestProperty("Content-Type", "application/xml");
		conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 5.1; rv:12.0) Gecko/20100101 Firefox/12.0");

//		InputStream input = null;
//		input = conn.getInputStream();
//		System.out.println(input.available());
		
		int respCode = ((HttpURLConnection)conn).getResponseCode();
		String responseMessage=((HttpURLConnection)conn).getResponseMessage();
		System.out.println(respCode);
		
		
		threadCounts.append("Response Code for " +pURL+" is : "+ respCode+" "+responseMessage+"\n");
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
    }
}
