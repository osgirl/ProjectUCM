package hays.com.UpdateFilter;

import intradoc.shared.*;
import intradoc.common.*;
import intradoc.data.*;

/***
 ** @author guptak
 *** This filter is made for Hays Worldwide.
 *** This is to update Event Date with Release Date when xEventDate field is empty
 */
public class EventDateUpdate implements FilterImplementor 
{


	public int doFilter(Workspace ws, DataBinder binder, ExecutionContext cxt)throws DataException, ServiceException 
	{
		
		String eventDate = binder.getLocal("xEventDate");
		String releaseDate = binder.getLocal("dInDate");
		String contentType = binder.getLocal("dDocType");
		String websiteSection = binder.getLocal("xWebsiteSection");
		String siteId = binder.getLocal("xWebsites");
		String locale = binder.getLocal("xLocale");
		String account = binder.getLocal("dDocAccount");
		 
	
		
		
		Report.trace("checktrace", "Event date :" + eventDate + " and Release Date :" + releaseDate, null);
		Report.trace("checktrace", "Content Type :" + contentType + " and Website Seaction :" + websiteSection, null);
		Report.trace("checktrace", "Locale :" + locale + " and account :" + account, null);
		
		
		if (eventDate == null || eventDate.length() == 0) 
		{
			Report.trace("checktrace", "Inside first condition and Event date :" + eventDate, null);
			if(contentType.contains("Highlights") || contentType.contains("Initiative") || contentType.contains("CRReport") ||  contentType.contains("Video") || contentType.contains("News") ||  contentType.contains("PromotionalContent")
			||  contentType.contains("Survey"))
			{
				Report.trace("checktrace", "Inside second condition ", null);
				
				if(websiteSection.toLowerCase().contains("haysworldwide") || siteId.toLowerCase().contains("haysworldwide")
						|| account.toLowerCase().contains("haysworldwide") || locale.contains("en-WW") 
						|| websiteSection.toLowerCase().contains("survey"))
				{	
					Report.trace("checktrace", "Inside third condition and Content type :" + contentType, null);
					binder.putLocal("xEventDate", releaseDate);
				}
				Report.trace("checktrace", "Outside third condition ", null);
			}Report.trace("checktrace", "Outside second condition ", null);
		}Report.trace("checktrace", "Outside first condition ", null);
		
		Report.trace("checktrace", "Event date :" + eventDate + " and Release Date :" + releaseDate, null);

		return CONTINUE;

	}

	
}
