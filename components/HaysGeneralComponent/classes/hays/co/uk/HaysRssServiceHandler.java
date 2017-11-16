package hays.co.uk;

import intradoc.server.ServiceHandler;
import intradoc.shared.SharedObjects;


public class HaysRssServiceHandler extends ServiceHandler {
	
	static final String PROXY_SET = SharedObjects.getEnvironmentValue("RSSExternalProxySet");
	static final String PROXY_HOST = SharedObjects.getEnvironmentValue("RSSExternalProxyHost");
	static final String PROXY_PORT = SharedObjects.getEnvironmentValue("RSSExternalProxyPort");
	static {
		/*System.getProperties().put("proxySet", PROXY_SET);
		System.getProperties().put("proxyHost", PROXY_HOST);
		System.getProperties().put("proxyPort", PROXY_PORT);*/
	}
	
	public void setProxy() {}

}
