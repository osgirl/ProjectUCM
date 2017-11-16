package hays.custom;

import intradoc.common.ExecutionContext;
import intradoc.common.ServiceException;
import intradoc.data.DataBinder;
import intradoc.data.DataException;
import intradoc.data.DataResultSet;
import intradoc.data.ResultSet;
import intradoc.data.Workspace;
import intradoc.shared.FilterImplementor;
import intradoc.common.SystemUtils;

public class SetLocationDescriptionFilter implements FilterImplementor {
	/**
	 * Run this filter after the services are all loaded
	 */
	public int doFilter(Workspace ws, DataBinder binder, ExecutionContext cxt)
			throws DataException, ServiceException {

			String locationId = binder.getLocal("xLocation");
			SystemUtils.trace("SetLocationDescriptionFilter", "locationId "+locationId);
			if( locationId != null ){
				locationId = locationId.trim();
				if( locationId.length() > 0 ) {
					locationId = locationId.replaceAll(";", ",");
					if( locationId.startsWith(",")){
						locationId = locationId.substring(1);
					}
					if( locationId.endsWith(",")){
						locationId = locationId.substring(0, locationId.length()-1);
					}
					DataBinder params = new DataBinder();
					params.putLocal("locationIds", locationId);
					ResultSet rez = null;
					try {
						rez = ws.createResultSet("LocationDetailsFilterQuery", params);
						SystemUtils.trace("SetLocationDescriptionFilter","search for locations: " + locationId);
					} catch(DataException ex){
						SystemUtils.trace("SetLocationDescriptionFilter","error "+ex.getStackTrace());
					}
					if( rez != null && rez.first()) {
						//SystemUtils.trace("SetLocationDescriptionFilter","resultset  "+rez.getFieldName(1));
						//SystemUtils.trace("SetLocationDescriptionFilter","resultset  "+rez.getStringValue(1));
						SystemUtils.trace("SetLocationDescriptionFilter","resultset  "+rez.getNumFields());
						String locationDescription = rez.getStringValueByName("DEFAULT_DESCRIPTION");
						SystemUtils.trace("SetLocationDescriptionFilter","locationDescription is "+locationDescription);
						locationDescription = locationDescription.replaceAll(" -", ",");
						locationDescription = locationDescription.replaceAll("INT -", "");
						binder.putLocal("xLocationDescription", locationDescription);
						SystemUtils.trace("SetLocationDescriptionFilter","xLocationDescription "+locationDescription);
					}
				}
			} 
		return CONTINUE;
	}
}
