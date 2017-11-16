package hays.com.commonutils;

import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;

public class NamespaceResolver implements NamespaceContext
{

	@Override
	public String getNamespaceURI(String arg0)
	{
		String uri;
		if (arg0.equals("wcm"))
			uri = "http://www.stellent.com/wcm-data/ns/8.0.0";
		else if (arg0.equals("idc"))
			uri = "http://www.stellent.com/IdcService/";
		else
			uri = null;
		return uri;
	}

	@Override
	public String getPrefix(String arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator getPrefixes(String arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

}
