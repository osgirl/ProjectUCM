package infomentum.ontology;

import java.io.File;



import infomentum.ontology.loader.OntologyFacade;
import infomentum.ontology.utils.OntologyUtils;
import intradoc.common.ExecutionContext;
import intradoc.common.FileUtils;
import intradoc.common.ServiceException;
import intradoc.data.DataBinder;
import intradoc.data.DataException;
import intradoc.data.ResultSet;
import intradoc.data.Workspace;
import intradoc.server.IdcSystemLoader;
import intradoc.server.ScheduledSystemEvents;
import intradoc.shared.FilterImplementor;

public class OntologyScheduledEvent  implements FilterImplementor {
	
	public int doFilter(Workspace ws, DataBinder eventData, ExecutionContext cxt)	throws DataException, ServiceException
{
	// get the action, and be sure to only execute your code if the 'action'
	// matches the value for action in the 'CustomScheduledEvents' table 
	String action  = eventData.getLocal("action");
	
	// execute the daily event, or the hourly event
	if (action.equals("OntologyCustomDailyEvent"))
	{
		doCustomDailyEvent(ws, eventData, cxt);
		return FINISHED;
	}
/*	else if (action.equals("CustomHourlyEvent"))
	{
		doCustomHourlyEvent(ws, eventData, cxt); 
		return FINISHED;
	} 
	*/
	// Return CONTINUE so other filters have a chance at it.
	return CONTINUE;
}


/**
 * Execute the custom daily event
 * @return an error string, or null if no error
 */
protected void doCustomDailyEvent(Workspace ws, DataBinder eventData, 	ExecutionContext cxt) throws DataException, ServiceException
{
	// you MUST perform at least one update
	update("OntologyCustomDailyEvent", "event starting...", ws);
	
	try {
		trace("doing custom daily event... should be run around midnight");
	
		ResultSet ontMappingRS = OntologyFacade.getOntMapping();
		do {
			String metadata = ontMappingRS.getStringValueByName("metadata");
			String pathBase = OntologyUtils.getOntConfDir(metadata) ;
			FileUtils.createLockIfNeeded(pathBase );
			File file = new File(pathBase, "lockwait.dat");
			FileUtils.touchFile(file.getPath());	
		} while(ontMappingRS.next());
	}  catch(Exception ex ) {
		trace("Exception while executing daily event: " + ex);
	}
	
	

	// event has finished!
	update("OntologyCustomDailyEvent", "event finished successfully", ws);
}


/**
 * Execute the custom hourly event
 * @return an error string, or null if no error
 */
protected void doCustomHourlyEvent(Workspace ws, DataBinder eventData, ExecutionContext cxt) throws DataException, ServiceException
{
	// you MUST perform at least one update
	update("CustomHourlyEvent", "event starting...", ws);
			
	trace("doing custom hourly event");

	// event has finished!
	update("CustomHourlyEvent", "event finished successfully", ws);		
}

/**
 * Update the state of the event. Must be done at least once to tell the content server
 * when the scehduled event is finished.
 */
protected void update(String action, String msg, Workspace workspace) throws ServiceException, DataException
{
	long curTime = System.currentTimeMillis();
	ScheduledSystemEvents sse = IdcSystemLoader.getOrCreateScheduledSystemEvents(workspace);
	sse.updateEventState(action, msg, curTime);
}

/**
 * Log a trace message to the 'scheduledevents' section
 */
protected void trace(String str)
{
	OntologyUtils.debug( "- custom event - " + str);
}	

}
