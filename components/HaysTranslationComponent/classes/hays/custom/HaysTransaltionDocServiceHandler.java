package hays.custom;

import intradoc.common.SystemUtils;
import intradoc.server.ServiceHandler;

public class HaysTransaltionDocServiceHandler extends ServiceHandler{
	public void translationAddFile(){
		String checkinType = m_binder.getLocal("ssDefaultDocumentToken");
		SystemUtils.trace("Translation", "checkinType "+checkinType);
		if (checkinType != null && checkinType.equalsIgnoreCase("SSNonEmptyContributorDataFile")){
			String tempFile = m_binder.getLocal("primaryFile:path");
			SystemUtils.trace("Translation", "tempFile "+tempFile);
			if(tempFile != null && tempFile.length() > 0){
				m_binder.addTempFile(tempFile);
			}
			
		}
	}

}
