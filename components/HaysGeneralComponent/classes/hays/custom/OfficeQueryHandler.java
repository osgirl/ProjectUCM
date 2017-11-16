package hays.custom;

import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import infomentum.ontology.Converter;
import infomentum.ontology.loader.OntologyFacade;
import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.data.DataException;
import intradoc.data.DataResultSet;
import intradoc.data.ResultSet;
import intradoc.data.Workspace;
import intradoc.provider.Provider;
import intradoc.provider.Providers;
import intradoc.server.ServiceHandler;

public class OfficeQueryHandler extends ServiceHandler {
	
	/**
	 * Get the list of specialism Id's and labels against an officeID
	 */
	public void getOfficeSpecialisms() throws ServiceException, DataException {
		
		// obtain the provider name, the query, and the result set name
		// from the action definition in the service
		String providerName = m_currentAction.getParamAt(0);
		String resultSetName = m_currentAction.getParamAt(1);
		String queryName = m_currentAction.getParamAt(2);
		
		String localeString = m_binder.getLocal("xLocale"); 
		
		SystemUtils.trace("getOfficeSpecialisms", "queryName found:"+queryName);
		SystemUtils.trace("getOfficeSpecialisms", "resultSetName found:"+resultSetName);
		SystemUtils.trace("getOfficeSpecialisms", "Locale found:"+localeString);
		
		int index = localeString.indexOf("-");
		if (index < 0){
			index = localeString.indexOf("_");
		}
		String ontLanguageCode = localeString.substring(0, index);
		
		
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

		// grab the provider object that does all the work, and scope it to
		// a workspace object for database access, since we can be reasonably
		// certain at this point that the object returned is a Workspace object
		Workspace ws = (Workspace) p.getProvider();
		DataResultSet result = null;

		// if they specified a predefined query, execute that
		if (queryName != null && queryName.trim().length() > 0) {
			// obtain a JDBC result set with the data in it. This result set is
			// temporary, and we must copy it before putting it in the binder
			ResultSet temp = ws.createResultSet(queryName, m_binder);

			// create a DataResultSet based on the temp result set
			result = new DataResultSet();
			result.copy(temp);
		}
		String state = result.getStringValueByName("xState"); 
		String phone=result.getStringValueByName("xPhone");
		String fax=result.getStringValueByName("xFax");
		String email=result.getStringValueByName("xEmail");
		SystemUtils.trace("getOfficeSpecialisms","resultset phone "+phone) ; 
		SystemUtils.trace("getOfficeSpecialisms", "resultset ::"+result.getNumRows());
		SystemUtils.trace("getOfficeSpecialisms", "resultset :"+result);
		String ontSpecialismId = null, ontSpecialismName;
		DataResultSet fields = new DataResultSet(new String[] {
                "xOfficeSpecialismId", "xSpecialismDescription", "xstate", "xphone", "xfax", "xemail"
            });
		do {
			try {
				
				ontSpecialismId = result.getStringValueByName("xOfficeSpecialismId");
				SystemUtils.trace("getOfficeSpecialisms", "ontSpecialismId = " + ontSpecialismId);
				//tokenize
				if(ontSpecialismId.startsWith(";")){
					ontSpecialismId = ontSpecialismId.substring(0, ontSpecialismId.length()-1);
				}
				StringTokenizer st = new StringTokenizer(ontSpecialismId,";");
				String token = null;
				Vector<String> row = null;
				int count = 0;
				while(st.hasMoreTokens()){
					token = st.nextToken();
					ontSpecialismName = Converter.getLabel(token, OntologyFacade.getOntology("xCategory"), ontLanguageCode);
					//ontSpecialismName = "test"+count; //hard coded for time being
					row = new Vector<String>();
					row.add(token); row.add(ontSpecialismName);
					row.add(state);
					row.add(phone);
					row.add(fax);
					row.add(email);
	                fields.addRow(row);	                
					SystemUtils.trace("getOfficeSpecialisms", "After RS was added specialism description: " + result);
					count ++;
				}
				//end tokenize
				
				
			}catch(Exception ex) {
				SystemUtils.trace("getOfficeSpecialisms", "Exception while processing ontology specialism: " + ontSpecialismId + ", " + ex);
		}
		} while (result.next());
		
		// place the result into the databinder with the appropriate name
		m_binder.addResultSet(resultSetName, fields);

		// release the JDBC connection assigned to this thread (request)
		// which kills the result set 'temp'
		ws.releaseConnection();
	}
	
	
	/**
	 * This method is used set Sub Specialisms ISNULL
	 * @throws ServiceException
	 * @throws DataException
	 */
	public void getOfficeSubspecialism() throws ServiceException, DataException {
		SystemUtils.trace("getOfficeSpecSubSpec","*****************************************************************") ; 		
		// obtain the provider name, the query, and the result set name
		// from the action definition in the service
		String providerName = m_currentAction.getParamAt(0);
		String resultSetName = m_currentAction.getParamAt(1);
		String queryName1 = m_currentAction.getParamAt(2);
		String queryName2 = m_currentAction.getParamAt(3);
				
		 
		SystemUtils.trace("getOfficeSpecSubSpec","providerName "+providerName) ; 		
		SystemUtils.trace("getOfficeSpecSubSpec", "queryName1 found:"+queryName1);
		SystemUtils.trace("getOfficeSpecSubSpec", "queryName2 found:"+queryName2);
		SystemUtils.trace("getOfficeSpecSubSpec", "resultSetName found:"+resultSetName);
		
		
		String queryName = "";
		String subSpecialismId = m_binder.getLocal("subspecialismId"); 
		String SpecialismId = m_binder.getLocal("specialismid"); 
		String officeid = m_binder.getLocal("officeid"); 
		SystemUtils.trace("getOfficeSpecSubSpec", "SpecialismId found:"+SpecialismId);
		SystemUtils.trace("getOfficeSpecSubSpec", "subSpecialismId found:"+subSpecialismId);
		SystemUtils.trace("getOfficeSpecSubSpec", "officeid found:"+officeid);
		//SystemUtils.trace("getOfficeSpecSubSpec", "officeid found:"+subSpecialismId.trim().length());
		
		if (subSpecialismId != null && subSpecialismId.trim().length() > 0 && !subSpecialismId.equalsIgnoreCase("null")) {
			 
			queryName =queryName1;
		}
		else{
			 
			queryName =queryName2;
		}
		SystemUtils.trace("getOfficeSpecSubSpec", "queryName found:"+queryName);
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

		// grab the provider object that does all the work, and scope it to
		// a workspace object for database access, since we can be reasonably
		// certain at this point that the object returned is a Workspace object
		Workspace ws = (Workspace) p.getProvider();
		DataResultSet result = null;

		// if they specified a predefined query, execute that
		if (queryName != null && queryName.trim().length() > 0) {
			// obtain a JDBC result set with the data in it. This result set is
			// temporary, and we must copy it before putting it in the binder
			ResultSet temp = ws.createResultSet(queryName, m_binder);

			// create a DataResultSet based on the temp result set
			result = new DataResultSet();
			result.copy(temp);
		}
 
		SystemUtils.trace("getOfficeSpecSubSpec", "resultset :"+result);
		 
		
		// place the result into the databinder with the appropriate name
		SystemUtils.trace("getOfficeSpecialisms", "resultset :"+result.getNumRows());
		SystemUtils.trace("getOfficeSpecialisms", "resultset :"+result);
		if(result != null && result.getNumRows()>0){
			m_binder.addResultSet(resultSetName, result);
		}
		

		// release the JDBC connection assigned to this thread (request)
		// which kills the result set 'temp'
		ws.releaseConnection();
	}
	
	/**
	 * Get the list of Offices mapped to a specialismId
	 */
	public void getOfficeForSpecialisms() throws ServiceException, DataException {
		
		// obtain the provider name, the query, and the result set name
		// from the action definition in the service
		String providerName = m_currentAction.getParamAt(0);
		String resultSetName = m_currentAction.getParamAt(1);		
		String queryName1 = m_currentAction.getParamAt(2);
		String queryName2 = m_currentAction.getParamAt(3);
		
		String specialismId = m_binder.getLocal("specialismId"); 
		String subSpecialismId = m_binder.getLocal("subspecialismId"); 
		
		SystemUtils.trace("getOfficeSpecialisms", "queryName1 found:"+queryName1);
		SystemUtils.trace("getOfficeSpecialisms", "queryName2 found:"+queryName2);
		SystemUtils.trace("getOfficeSpecialisms", "resultSetName found:"+resultSetName);
		SystemUtils.trace("getOfficeSpecialisms", "specialismId found:"+specialismId);
		SystemUtils.trace("getOfficeSpecialisms", "subSpecialismId found:"+subSpecialismId);
		
		if(specialismId != null && specialismId.trim().length() > 0)
		{
			try{
				m_binder.putLocal("specialismIdLike", "%"+specialismId+"%");
			}catch(Exception e){
				m_binder.putLocal("specialismIdLike", "*");
			}
		
		}
		String localeString = m_binder.getLocal("xLocale"); 
		
		SystemUtils.trace("getOfficeSpecialisms", "Locale found:"+localeString);
		
		int index = localeString.indexOf("-");
		if (index < 0){
			index = localeString.indexOf("_");
		}
		String country = localeString.substring(index+1);
		
		//m_binder.putLocal("xLocale", "%"+country);
		
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

		// grab the provider object that does all the work, and scope it to
		// a workspace object for database access, since we can be reasonably
		// certain at this point that the object returned is a Workspace object
		Workspace ws = (Workspace) p.getProvider();
		DataResultSet result = null;
		String queryName = "";
		if (subSpecialismId != null && subSpecialismId.trim().length() > 0 && !subSpecialismId.equalsIgnoreCase("null")) {
			 
			queryName =queryName1;
		}
		else{
			 
			queryName =queryName2;
		}
		SystemUtils.trace("getOfficeSpecialisms", "final queryName found:"+queryName);
		// if they specified a predefined query, execute that
		if (queryName != null && queryName.trim().length() > 0) {
			// obtain a JDBC result set with the data in it. This result set is
			// temporary, and we must copy it before putting it in the binder
			ResultSet temp = ws.createResultSet(queryName, m_binder);

			// create a DataResultSet based on the temp result set
			result = new DataResultSet();
			result.copy(temp);
		}

		SystemUtils.trace("getOfficeSpecialisms", "resultset :"+result.getNumRows());
		SystemUtils.trace("getOfficeSpecialisms", "resultset :"+result);
		
		
		// place the result into the databinder with the appropriate name
		m_binder.addResultSet(resultSetName, result);

		// release the JDBC connection assigned to this thread (request)
		// which kills the result set 'temp'
		ws.releaseConnection();
	}
	
	public void getCountryForOffices() throws DataException
	{
	
		String xlocale = m_currentAction.getParamAt(0);
		String locale = m_binder.getLocal(xlocale);
		int index = locale.indexOf("-");
		if (index < 0){
			index = locale.indexOf("_");
		}
		String country = locale.substring(index+1, locale.length());
		SystemUtils.trace("getOfficeSpecialisms", "country is :::"+country);
		m_binder.putLocal(xlocale, "%"+country);
		
	}
	
	public void setTimeStamp()
	{
	
		 java.util.Date date= new java.util.Date();
		 java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(date.getTime());
		 SystemUtils.trace("getOfficeSpecialisms", "Timestamp value is "+currentTimestamp);
		 m_binder.putLocalDate("ts", currentTimestamp);
		 
	}
	
	public void updateInsertOfficeSpecialismsDetails() throws DataException, ServiceException
	{
		// obtain the provider name, the query, and the result set name
		// from the action definition in the service
		String providerName = m_currentAction.getParamAt(0);
		String queryName1 = m_currentAction.getParamAt(1);	
		String queryName2 = m_currentAction.getParamAt(2); 
		
		
		String officeId = m_binder.getLocal("officeId"); 
		String specialismId = m_binder.getLocal("specialismId"); 
		String subSpecialismId = m_binder.getLocal("subspecialismId"); 
		String phone = m_binder.getLocal("phone"); 
		String fax = m_binder.getLocal("fax"); 
		String emailId = m_binder.getLocal("emailId"); 
		String username = m_binder.getLocal("user"); 
		String password = m_binder.getLocal("password"); 
		
		SystemUtils.trace("getOfficeSpecialisms", "queryName found:"+queryName1+","+queryName2);
		SystemUtils.trace("getOfficeSpecialisms", "officeId found:"+officeId);
		SystemUtils.trace("getOfficeSpecialisms", "specialismId found:"+specialismId);
		SystemUtils.trace("getOfficeSpecialisms", "subSpecialismId found:"+subSpecialismId);
		SystemUtils.trace("getOfficeSpecialisms", "phone ,fax, email, user and password found:"+phone+","+fax+","+emailId+","+username+","+password);
		SystemUtils.trace("getOfficeSpecialisms", "subSpecialismId found:"+subSpecialismId);
		
				
		
				
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

		// grab the provider object that does all the work, and scope it to
		// a workspace object for database access, since we can be reasonably
		// certain at this point that the object returned is a Workspace object
		Workspace ws = (Workspace) p.getProvider();
		DataResultSet result = null;
		String queryName ="";
		if (subSpecialismId != null && subSpecialismId.trim().length() > 0) {
			 
			queryName =queryName1;
		}
		else{
			 
			queryName =queryName2;
		}
		SystemUtils.trace("getOfficeSpecialisms", "final queryName found:"+queryName);
		// if they specified a predefined query, execute that
		if (queryName != null && queryName.trim().length() > 0) {
			
			ws.execute(queryName, m_binder);//Execute the Update/Insert Query

			}

				
		
		

		// release the JDBC connection assigned to this thread (request)
		// which kills the result set 'temp'
		ws.releaseConnection();
		
	}
	
	public void getAllSpecialismsMappedToOffices() throws DataException, ServiceException
	{
		String providerName = m_currentAction.getParamAt(0);
		String resultSetName = m_currentAction.getParamAt(1);
		String queryName = m_currentAction.getParamAt(2);
		
		String localeString = m_binder.getLocal("xLocale"); 
		
		SystemUtils.trace("getOfficeSpecialisms", "queryName found:"+queryName);
		SystemUtils.trace("getOfficeSpecialisms", "resultSetName found:"+resultSetName);
		SystemUtils.trace("getOfficeSpecialisms", "Locale found:"+localeString);
		
		int index = localeString.indexOf("-");
		if (index < 0){
			index = localeString.indexOf("_");
		}
		String ontLanguageCode = localeString.substring(0, index);
		String country = localeString.substring(index+1, localeString.length());

		m_binder.putLocal("metadata", "xCategory");
		m_binder.putLocal("language", ontLanguageCode);
		m_binder.putLocal("country", country);
		m_binder.putLocal("ontClass", "hays:Specialism");
		m_service.executeServiceEx("ONT_GET_INDIVIDUALS", true);
		SystemUtils.trace("getOfficeSpecialisms", "Executed service :: ONT_GET_INDIVIDUALS");
		
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

		Workspace ws = (Workspace) p.getProvider();
		DataResultSet result = null;

		if (queryName != null && queryName.trim().length() > 0) {
			ResultSet temp = ws.createResultSet(queryName, m_binder);
			result = new DataResultSet();
			result.copy(temp);
		}
		ws.releaseConnection();
		
		DataResultSet ontologyRS = (DataResultSet)m_binder.getResultSet("IndividualsList");
		DataResultSet rsFinal = new DataResultSet();
		rsFinal.copy(ontologyRS);
		
		if(result !=null && result.isRowPresent()){
			ontologyRS.mergeDelete("id", result, false);
		}
		rsFinal.mergeDelete("id", ontologyRS, false);
		
		m_binder.addResultSet(resultSetName, rsFinal);
		
	}
	
	
}
