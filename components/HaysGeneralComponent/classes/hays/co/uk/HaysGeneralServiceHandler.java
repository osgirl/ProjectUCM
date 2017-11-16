package hays.co.uk;

import intradoc.common.ServiceException;
import sitestudio.SSHierarchyServiceHandler;

public class HaysGeneralServiceHandler extends SSHierarchyServiceHandler{
		
	public void ssGetDefaultHttpSiteAddress()throws ServiceException{
		String siteId = super.m_binder.getLocal("siteId");
		String dafaultHttpSiteAddress = getDefaultHttpSiteAddress(siteId);
		m_binder.putLocal("dafaultHttpSiteAddress", dafaultHttpSiteAddress);
	}
}
