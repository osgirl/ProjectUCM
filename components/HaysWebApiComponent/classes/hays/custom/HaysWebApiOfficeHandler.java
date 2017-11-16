package hays.custom;

import hays.com.commonutils.HaysWebApiUtils;
import infomentum.ontology.Converter;
import infomentum.ontology.loader.OntologyFacade;
import intradoc.common.LocaleUtils;
import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.data.DataBinder;
import intradoc.data.DataException;
import intradoc.data.DataResultSet;
import intradoc.data.FieldInfo;
import intradoc.data.ResultSet;
import intradoc.data.Workspace;
import intradoc.provider.Provider;
import intradoc.provider.Providers;
import intradoc.server.PageMerger;
import intradoc.server.ServiceHandler;
import intradoc.shared.SharedObjects;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HaysWebApiOfficeHandler extends ServiceHandler
{

	private static final String SELECT_OFFICE_SEARCH_QUERY = "SELECT os.phone as \"telephone\" ,os.fax as \"fax\" , os.emailid as "
			+ "   \"emaildddress\" ,  a.xstate as   \"state\" , specialismid as \"specialismid\" ,ddoctitle as \"officename\" ," + " xaddressline1 as   \"addressLine1\" ,  xaddressline2 as  "
			+ "\"addressLine2\" , a.xpostcode  as  \"postcode\" , "
			+ "a.xcountry as  \"country\" , os.officeid as  \"officeid\" , xsuburb as  \"suburb\" , oloc.geo_location.sdo_point.y as  \"latitude\", oloc.geo_location.sdo_point.x as  \"longitude\""
			+ ", sdo_nn_distance (1) distance FROM docmeta a INNER JOIN revisions r   ON a.did "
			+ "= r.did JOIN OfficeSpecialismsDetails os   ON r.ddocname = os.officeid INNER JOIN office_locations oloc " + " ON oloc.did = a.did WHERE a.xcountry = ";

	private static final String SELECT_OFFICE_SEARCH_QUERY_PART_STATE = " and a.xstate = ";
	private static final String SELECT_OFFICE_SEARCH_QUERY_PART_SPECIALISM = " and specialismid  =  ";
	private static final String SELECT_OFFICE_SEARCH_QUERY_PART_SUBSPECIALISM = " and subspecialismid  =  ";
	private static String SELECT_OFFICE_SEARCH_QUERY_PART_LAT_LONG = " and sdo_nn (oloc.geo_location, SDO_GEOMETRY(2001, 8307, SDO_POINT_TYPE (R_LONG, R_LAT,NULL), NULL, NULL), 'distance = '||R_RAD|| ' unit=MILE sdo_batch_size=100',1) = 'TRUE'";
	private static final String SELECT_OFFICE_SEARCH_QUERY_PART_SUBSPECIALISMID_COND = " and os.SUBSPECIALISMID is null";
	private static final String SELECT_OFFICE_SEARCH_QUERY_PART_ORDER = " and r.drevrank = " + "0 " + " and r.dstatus='RELEASED' order by ddoctitle";

	private static final String VALIDATION_REGEX = "[A-Za-zA_Za_z0-9.:-]*"; 

	/**
	 * 
	 * @throws ServiceException
	 * @throws DataException
	 */

	public void getOfficeResults() throws ServiceException, DataException
	{

		String traceStr = "OfficeResults";
		

		SystemUtils.trace("webAPI_OfficeResults", "Inside HaysWebApiOfficeHandler : getOfficeResults:");
		String locale = getData("locale");
		String locationid = getData("locationid");
		String specialismId = getData("specialismid");
		String subspecialismId = getData("subspecialismid");
		String providerName = this.m_currentAction.getParamAt(0);
		String resultSetName = this.m_currentAction.getParamAt(1);
		String longitude = getData("longitude");
		String latitude = getData("latitude");
		String radius = getData("radius");
		SystemUtils.trace("webAPI_OfficeResults", " locale : " + locale);
		SystemUtils.trace("webAPI_OfficeResults", " locationid : " + locationid);
		SystemUtils.trace("webAPI_OfficeResults", " specialismid : " + specialismId);
		SystemUtils.trace("webAPI_OfficeResults", " subspecialismid : " + subspecialismId);
		SystemUtils.trace("webAPI_OfficeResults", " providerName : " + providerName);
		SystemUtils.trace("webAPI_OfficeResults", "resultSetName : " + resultSetName);

		// get the LOCALE_DETAILS result from the binder to get hayswebsited
		// related data.
		ResultSet localeResultSet = this.m_binder.getResultSet("LOCALE_DETAILS");
		String languageId = locale.substring(0, locale.indexOf("-"));
		SystemUtils.trace("webAPI_OfficeResults", "languageId : " + languageId);

		String[] OfficeLocatorUKCerowList = SharedObjects.getEnvironmentValue("OfficeLocatorUKCerowList").split(",");

		boolean officeLocatorCountry = false;
		for (String s : OfficeLocatorUKCerowList)
		{
			if (locale.endsWith(s))
				officeLocatorCountry = true;
		}

		if (localeResultSet != null)
		{
			// this block if for APAC country
			if (localeResultSet.getStringValueByName("CONTRY_REGION").equalsIgnoreCase("apac") || (officeLocatorCountry))
			{
				boolean isRadialSearch = false;
				//if (!("".equalsIgnoreCase(latitude) && "".equalsIgnoreCase(longitude)))
				if (officeLocatorCountry ||  !("".equalsIgnoreCase(latitude) && "".equalsIgnoreCase(longitude)))
				{
					isRadialSearch = true;
					longitude = getData("longitude");
					latitude = getData("latitude");

					if ("".equals(longitude) && "".equals(latitude))
					{
						m_service.executeServiceEx("GET_LOCATIONID_DATA", true);
						ResultSet locationResultSet = this.m_binder.getResultSet("LOCATION_RESULTS");
						if (locationResultSet != null)
						{
							longitude = locationResultSet.getStringValueByName("longitude");
							latitude = locationResultSet.getStringValueByName("latitude");
							m_binder.putLocal("latitude", latitude);
							m_binder.putLocal("longitude", longitude);
							SystemUtils.trace("webAPI_OfficeResults", "INFO : Langitude and Latidute not passed in parameter. Fetching from DB.");
						}
					}

					SystemUtils.trace("webAPI_OfficeResults", "latitude " + latitude);
					SystemUtils.trace("webAPI_OfficeResults", "longitude  " + longitude);
				}
				try
				{
					StringBuffer sqlQuery = new StringBuffer();
					if (!isRadialSearch)
					{
						sqlQuery.append(SELECT_OFFICE_SEARCH_QUERY.replace(", sdo_nn_distance (1) distance", ""));
					}
					else
					{
						sqlQuery.append(SELECT_OFFICE_SEARCH_QUERY);
					}
					sqlQuery.append("'" + locale + "'");
					if (!"".equalsIgnoreCase(specialismId))
					{
						sqlQuery.append(SELECT_OFFICE_SEARCH_QUERY_PART_SPECIALISM).append("'" + specialismId + "'");
					}
					
					if(!(subspecialismId == null || "null".equalsIgnoreCase(subspecialismId) || subspecialismId.trim().length() == 0))
					{
						sqlQuery.append(SELECT_OFFICE_SEARCH_QUERY_PART_SUBSPECIALISM).append("'" + subspecialismId+ "'");
					}
					if (isRadialSearch)
					{
						if (radius.equalsIgnoreCase(""))
						{
							radius = "20";
						}
						sqlQuery.append(SELECT_OFFICE_SEARCH_QUERY_PART_LAT_LONG.replace("R_LAT", latitude).replace("R_LONG", longitude).replace("R_RAD", radius));
					}
					else if (!"".equalsIgnoreCase(locationid))
					{
						sqlQuery.append(SELECT_OFFICE_SEARCH_QUERY_PART_STATE).append("'" + locationid + "'");
					}
					else
					{
						this.m_binder.putLocal("StatusMessage", LocaleUtils.encodeMessage("wwInvalidLocation", null));
						this.m_binder.putLocal("StatusCode", "UC004");
					}
					
						if((subspecialismId == null || "null".equalsIgnoreCase(subspecialismId) || subspecialismId.trim().length() == 0))
					{
						
						sqlQuery.append(SELECT_OFFICE_SEARCH_QUERY_PART_SUBSPECIALISMID_COND);
						SystemUtils.trace("webAPI_OfficeResults", "subspecialismId is null Query : " + sqlQuery);
					}
					else{
						sqlQuery.append(SELECT_OFFICE_SEARCH_QUERY_PART_SUBSPECIALISMID_COND.replace("is null", "= "+"'" + subspecialismId+ "'"));
						SystemUtils.trace("webAPI_OfficeResults", "subspecialismId is not null Query : " + sqlQuery);
					}
					if (isRadialSearch)
					{
						sqlQuery.append(SELECT_OFFICE_SEARCH_QUERY_PART_ORDER.replace("ddoctitle", "distance"));
					}
					else
					{
						sqlQuery.append(SELECT_OFFICE_SEARCH_QUERY_PART_ORDER);
					}
					SystemUtils.trace("webAPI_OfficeResults", "sqlQuery : " + sqlQuery);

					SystemUtils.trace("webAPI_OfficeResults", " In APAC block ");
					DataResultSet rsOfficeResults = new DataResultSet();
					rsOfficeResults = getOfficeResultSet(sqlQuery, providerName);

					if (null != rsOfficeResults && rsOfficeResults.getNumRows() > 0)
					{
						SystemUtils.trace("webAPI_OfficeResults", " rsOfficeResults getNumRows " + rsOfficeResults.getNumRows());
						// called setSpecialismDesc method to set SpecialismDesc
						// for SpecialismID in resultset.

						setSpecialismDesc(rsOfficeResults, languageId, traceStr);
						String[] removeRSField = { "SPECIALISMID" };
						rsOfficeResults.removeFields(removeRSField);

						if (!isRadialSearch)
						{
							Vector<String> fields = new Vector<String>();
							fields.add("distance");
							rsOfficeResults.appendFields(fields);
						}
						else
						{
							rsOfficeResults.renameField("DISTANCE", "distance");
						}
						this.m_binder.addResultSet(resultSetName, rsOfficeResults);

						this.m_binder.putLocal("StatusMessage", LocaleUtils.encodeMessage("wwWebApiOKMsg", null));
						this.m_binder.putLocal("StatusCode", "UC000");
					}
					else
					{
						this.m_binder.putLocal("StatusMessage", LocaleUtils.encodeMessage("wwInvalidLocation", null));
						this.m_binder.putLocal("StatusCode", "UC004");

					}

					m_binder.removeResultSet("LOCALE_DETAILS");
					m_binder.removeResultSet("UserAttribInfo");
					m_binder.removeLocal("specialismid");
					m_binder.removeLocal("sitelocale");
					m_binder.removeLocal("locationid");
					m_binder.removeLocal("locale");
				}
				catch (Exception e)
				{
					m_binder.putLocal("StatusMessage", LocaleUtils.encodeMessage("wwInvalidLocation", null));
					SystemUtils.trace("GetOfficeLocations", "StatusMessage " + e.toString());
					this.m_binder.putLocal("StatusCode", "UC004");
					e.printStackTrace();
				}

			}
			// this block if for UK and cerow country
			else
			{

				String siteLocale = "en-GB", isoCountryCode = "GB";
				siteLocale = locale;
				isoCountryCode = localeResultSet.getStringValueByName("ISOCOUNTRYCODE");
				String domainId = localeResultSet.getStringValueByName("DOMAINID");
				m_binder.putLocal("isoCountryCode", isoCountryCode);
				m_binder.putLocal("location", locationid);
				m_binder.putLocal("domainId", domainId);
				m_binder.putLocal("brandId", specialismId);
				m_binder.putLocal("websiteLocale", siteLocale);

				SystemUtils.trace("webAPI_OfficeResults", "websiteLocale " + siteLocale);
				SystemUtils.trace("webAPI_OfficeResults", "isoCountryCode " + isoCountryCode);
				SystemUtils.trace("webAPI_OfficeResults", "brandId " + specialismId);
				SystemUtils.trace("webAPI_OfficeResults", "location " + m_binder.getLocal("location"));
				SystemUtils.trace("webAPI_OfficeResults", "domainId " + m_binder.getLocal("domainId"));

				longitude = getData("longitude");
				latitude = getData("latitude");

				if ("".equals(longitude) && "".equals(latitude))
				{
					m_service.executeServiceEx("GET_LOCATIONID_DATA", true);
					ResultSet locationResultSet = this.m_binder.getResultSet("LOCATION_RESULTS");
					if (locationResultSet != null)
					{
						longitude = locationResultSet.getStringValueByName("longitude");
						latitude = locationResultSet.getStringValueByName("latitude");
						m_binder.putLocal("latitude", latitude);
						m_binder.putLocal("longitude", longitude);
						SystemUtils.trace("webAPI_OfficeResults", "INFO : Langitude and Latidute not passed in parameter. Fetching from DB.");
					}
				}

				SystemUtils.trace("webAPI_OfficeResults", "latitude " + latitude);
				SystemUtils.trace("webAPI_OfficeResults", "longitude  " + longitude);

				if (!("".equals(longitude) && "".equals(latitude)))
				{
					try
					{

						Workspace sqlServerWs = getProviderConnection("SqlServer");
						ResultSet rs = sqlServerWs.createResultSet("QGetOfficeLocations", m_binder);
						DataResultSet drs = new DataResultSet();
						drs.copy(rs);
						SystemUtils.trace("webAPI_OfficeResults", "drs.getNumRows()  " + drs.getNumRows());

						DataResultSet fields = new DataResultSet(new String[] { "telephone", "fax", "emaildddress", "state", "officename", "addressLine1", "addressLine2", "postcode", "country",
								"officeid", "suburb", "specialismname", "latitude", "longitude", "distance" });
						// called mapResultSetUK method to map required result
						// set.
						if (drs.getNumRows() > 0)
						{
							mapResultSetUK(drs, fields);
						}

						if (fields != null && fields.getNumRows() > 0)
						{
							m_binder.addResultSet(resultSetName, fields);
							this.m_binder.putLocal("StatusMessage", LocaleUtils.encodeMessage("wwWebApiOKMsg", null));
							this.m_binder.putLocal("StatusCode", "UC000");
						}
						else
						{
							this.m_binder.putLocal("StatusMessage", LocaleUtils.encodeMessage("wwInvalidLocation", null));
							this.m_binder.putLocal("StatusCode", "UC004");
						}
						if (sqlServerWs != null)
						{
							sqlServerWs.releaseConnection();
						}
					}
					catch (Exception e)
					{
						m_binder.putLocal("StatusMessage", LocaleUtils.encodeMessage("wwInvalidLocation", null));
						SystemUtils.trace("GetOfficeLocations", "StatusMessage " + e.toString());
						this.m_binder.putLocal("StatusCode", "UC004");
						e.printStackTrace();
					}
				}
				m_binder.removeResultSet("LOCATION_RESULTS");
				m_binder.removeResultSet("LOCALE_DETAILS");
				m_binder.removeResultSet("UserAttribInfo");
				m_binder.removeLocal("brandId");
				m_binder.removeLocal("isoCountryCode");
				m_binder.removeLocal("longitude");
				m_binder.removeLocal("latitude");
				m_binder.removeLocal("locationPrefix");
				m_binder.removeLocal("sitelocale");
				m_binder.removeLocal("loc_descr");
				m_binder.removeLocal("domainId");
				m_binder.removeLocal("specialismid");
				m_binder.removeLocal("locationColumn");
				m_binder.removeLocal("location_id");
				m_binder.removeLocal("websiteLocale");
				m_binder.removeLocal("location");
				m_binder.removeLocal("UserAttribInfo");

			}
		}

	}

	/**
	 * 
	 * @throws ServiceException
	 * @throws DataException
	 */
	public void getLocationIDdata() throws ServiceException
	{

		// get the all required parameters from the binder.
		SystemUtils.trace("webAPI", "Inside HaysWebApiOfficeHandler : getLocaleDetails:");
		String location_id = this.m_binder.getLocal("locationid");

		String providerName = "";
		String resultSetName = "";
		String queryName = "";

		// put the trace to verify all binder parameters.

		SystemUtils.trace("webAPI_LocationID", "providerName : " + providerName);
		SystemUtils.trace("webAPI_LocationID", "resultSetName : " + resultSetName);
		SystemUtils.trace("webAPI_LocationID", " queryName : " + queryName);
		SystemUtils.trace("webAPI_LocationID", "location_id : " + location_id);

		// this.m_binder.putLocal("location_id", location_id);

		m_binder.putLocal("locationColumn", "default_description");
		m_binder.putLocal("location_id", location_id);

		DataBinder params = new DataBinder();
		params.putLocal("locationColumn", "default_description");
		params.putLocal("locationIds", location_id);

		DataResultSet result = null;
		Workspace ws = null;
		// if they specified a predefined query, execute that

		// obtain a JDBC result set with the data in it. This result set is
		// temporary, and we must copy it before putting it in the binder
		try
		{
			providerName = this.m_currentAction.getParamAt(0);
			resultSetName = this.m_currentAction.getParamAt(1);
			queryName = this.m_currentAction.getParamAt(2);
			if (queryName != null && queryName.trim().length() > 0)
			{
				ws = getProviderConnection(providerName);
				ResultSet temp = ws.createResultSet(queryName, params);

				// create a DataResultSet based on the temp result set
				result = new DataResultSet();
				result.copy(temp);
				SystemUtils.trace("webAPI_LocationID", "result.getNumRows : " + result.getNumRows());

				// place the result into the databinder with the appropriate
				// name
				if (result.getNumRows() <= 0)
				{
					String msg = LocaleUtils.encodeMessage("wwInvalidLocation", "");
					this.m_binder.putLocal("StatusMessage", LocaleUtils.encodeMessage("wwInvalidLocation", null));
					this.m_binder.putLocal("StatusCode", "UC004");
					throw new ServiceException(msg);
				}
				else
				{
					this.m_binder.addResultSet(resultSetName, result);
				}
			}

			// release the JDBC connection assigned to this thread (request)
			// which kills the result set 'temp'
			ws.releaseConnection();
		}
		catch (Exception e)
		{
			m_binder.putLocal("StatusMessage", LocaleUtils.encodeMessage("wwInvalidLocation", null));
			this.m_binder.putLocal("StatusCode", "UC004");
			e.printStackTrace();
		}

	}

	/**
	 * 
	 * @param drs
	 * @param fields
	 */
	public void mapResultSetUK(DataResultSet drs, DataResultSet fields)
	{

		SystemUtils.trace("webAPI_OfficeResults", " in mapResultSet  call");

		if (drs != null & drs.getNumRows() > 0)
		{
			for (int i = 0; i < drs.getNumRows(); i++)
			{
				drs.setCurrentRow(i);
				String officeid = drs.getStringValueByName("OfficeId");
				String officename = drs.getStringValueByName("OfficeName");
				String specialismnameId = drs.getStringValueByName("BrandId");
				String specialismname = drs.getStringValueByName("BrandName");
				String Building = drs.getStringValueByName("Building");
				String Street = drs.getStringValueByName("Street");
				String state = drs.getStringValueByName("Town");
				String County = drs.getStringValueByName("County");
				String country = drs.getStringValueByName("Country");
				String postcode = drs.getStringValueByName("PostCode");
				String telephone = drs.getStringValueByName("Telephone");
				String fax = drs.getStringValueByName("Fax");
				String emaildddress = drs.getStringValueByName("EmailAddress");

				String addressLine1 = Building;
				String addressLine2 = Street;
		
				String longitude = drs.getStringValueByName("Longitude");
				String latitude = drs.getStringValueByName("Latitude");
				String suburb = "";

				String distance = drs.getStringValueByName("miles");

				SystemUtils.trace("webAPI_OfficeResults", "OfficeId  " + officeid);
				SystemUtils.trace("webAPI_OfficeResults", "OfficeName  " + officename);
				SystemUtils.trace("webAPI_OfficeResults", "specialismnameId  " + specialismnameId);
				SystemUtils.trace("webAPI_OfficeResults", "specialismname  " + specialismname);
				SystemUtils.trace("webAPI_OfficeResults", "Building  " + Building);
				SystemUtils.trace("webAPI_OfficeResults", "Street  " + Street);
				SystemUtils.trace("webAPI_OfficeResults", "state  " + state);
				SystemUtils.trace("webAPI_OfficeResults", "country  " + country);
				SystemUtils.trace("webAPI_OfficeResults", "postcode  " + postcode);
				SystemUtils.trace("webAPI_OfficeResults", "fax  " + fax);
				SystemUtils.trace("webAPI_OfficeResults", "emaildddress  " + emaildddress);
				SystemUtils.trace("webAPI_OfficeResults", "addressLine1  " + addressLine1);
				SystemUtils.trace("webAPI_OfficeResults", "addressLine2  " + addressLine2);
				SystemUtils.trace("webAPI_OfficeResults", "distance  " + distance);
				SystemUtils.trace("webAPI_OfficeResults", "longitude  " + longitude);
				SystemUtils.trace("webAPI_OfficeResults", "latitude  " + latitude);

				Vector<String> row = null;

				row = new Vector<String>();
				row.add(telephone);
				row.add(fax);
				row.add(emaildddress);
				row.add(state);
				row.add(officename);
				row.add(addressLine1);
				row.add(addressLine2);
				row.add(postcode);
				row.add(country);
				row.add(officeid);
				row.add(suburb);
				row.add(specialismname);
				row.add(latitude);
				row.add(longitude);
				row.add(distance);
				fields.addRow(row);
			}

		}

	}

	/**
	 * This method is used to execute the
	 * 
	 * @param sqlQuery
	 * @return DataResultSet
	 * @throws ServiceException
	 * @throws DataException
	 */
	private DataResultSet getOfficeResultSet(StringBuffer sqlQuery, String providerName) throws ServiceException, DataException
	{

		Workspace ws = getProviderConnection(providerName);

		ResultSet dbResultSet = null;
		DataResultSet drsDbResultset = null;
		if (sqlQuery != null && sqlQuery.length() > 0)
		{
			dbResultSet = ws.createResultSetSQL(sqlQuery.toString());
		}
		if (dbResultSet != null)
		{
			drsDbResultset = new DataResultSet();
			drsDbResultset.copy(dbResultSet);
		}
		if (ws != null)
		{
			ws.releaseConnection();
		}
		return drsDbResultset;
	}

	/**
	 * Method getApacOfficeStates is used to provide the Apac office states data
	 * based on provided locale .
	 * 
	 * @throws ServiceException
	 * @throws DataException
	 */
	public void getApacOfficeStates() throws ServiceException, DataException
	{

		// get the all required parameters from the binder.
		SystemUtils.trace("webAPI_States", "Inside HaysWebApiOfficeHandler : getApacOfficeStates:");
		String locale = this.m_binder.getLocal("locale");
		String providerName = this.m_currentAction.getParamAt(0);
		String resultSetName = this.m_currentAction.getParamAt(1);
		String queryName = this.m_currentAction.getParamAt(2);

		// put the trace to verify all binder parameters.

		SystemUtils.trace("webAPI_States", " locale : " + locale);
		SystemUtils.trace("webAPI_States", " providerName : " + providerName);
		SystemUtils.trace("webAPI_States", "resultSetName : " + resultSetName);
		SystemUtils.trace("webAPI_States", "queryName : " + queryName);

		this.m_binder.putLocal("SITELOCALE", locale);

		Workspace ws = getProviderConnection(providerName);
		DataResultSet result = null;

		// if they specified a predefined query, execute that
		if (queryName != null && queryName.trim().length() > 0)
		{
			// obtain a JDBC result set with the data in it. This result set is
			// temporary, and we must copy it before putting it in the binder
			ResultSet temp = ws.createResultSet(queryName, m_binder);

			// create a DataResultSet based on the temp result set
			result = new DataResultSet();
			result.copy(temp);
		}

		// place the result into the databinder with the appropriate name

		this.m_binder.addResultSet(resultSetName, result);
		this.m_binder.putLocal("StatusMessage", LocaleUtils.encodeMessage("wwWebApiOKMsg", null));
		this.m_binder.putLocal("StatusCode", "UC000");
		// release the JDBC connection assigned to this thread (request)
		// which kills the result set 'temp'
		ws.releaseConnection();

		// removed irrelevant data from the locale data.
		this.m_binder.removeResultSet("LOCALE_DETAILS");
		this.m_binder.removeLocal("SITELOCALE");
		this.m_binder.removeLocal("sitelocale");

	}

	/**
	 * Method getOfficeDetails is used to provide the Apac office data based on
	 * provided office ID which mindatory and specialismId which option
	 * parameter.
	 * 
	 * @throws ServiceException
	 * @throws DataException
	 */

	public void getOfficeDetails() throws ServiceException, DataException
	{

		// get the all required parameters from the binder.
		SystemUtils.trace("officedata", "Inside HaysWebApiOfficeHandler : getOfficeDetails:");
		String locale = this.m_binder.getLocal("locale");
		String officeId = this.m_binder.getLocal("officeId").toUpperCase().trim();
		String specialismId = this.m_binder.getLocal("specialismId");
		String providerName = this.m_currentAction.getParamAt(0);
		String resultSetName = this.m_currentAction.getParamAt(1);
		String queryNamewithSpec = this.m_currentAction.getParamAt(2);
		String queryName = this.m_currentAction.getParamAt(3);

		// put the trace to verify all binder parameters.
		SystemUtils.trace("webAPI_office", " locale :" + locale);
		SystemUtils.trace("webAPI_office", " officeId : " + officeId);
		SystemUtils.trace("webAPI_office", " specialismId : " + specialismId);
		SystemUtils.trace("webAPI_office", " providerName : " + providerName);
		SystemUtils.trace("webAPI_office", " resultSetName : " + resultSetName);
		SystemUtils.trace("webAPI_office", " queryNamewithSpec : " + queryNamewithSpec);
		SystemUtils.trace("webAPI_office", " queryName : " + queryName);

		Workspace ws = getProviderConnection(providerName);
		DataResultSet result = null;

		// if they specified a predefined query, execute that

		this.m_binder.putLocal("ddocname", officeId);
		this.m_binder.putLocal("officeId", officeId);

		if (specialismId != null && specialismId.trim().length() > 0)
		{
			SystemUtils.trace("webAPI_office", "In IF condition ");
			if (queryNamewithSpec != null && queryNamewithSpec.trim().length() > 0)
			{
				// obtain a JDBC result set with the data in it. This result set
				// is
				// temporary, and we must copy it before putting it in the
				// binder
				ResultSet temp = ws.createResultSet(queryNamewithSpec, m_binder);

				// create a DataResultSet based on the temp result set
				result = new DataResultSet();
				result.copy(temp);
			}
		}
		else
		{
			if (queryName != null && queryName.trim().length() > 0)
			{
				SystemUtils.trace("webAPI_office", "In ElseIF condition ");
				// obtain a JDBC result set with the data in it. This result set
				// is
				// temporary, and we must copy it before putting it in the
				// binder
				ResultSet temp = ws.createResultSet(queryName, m_binder);

				// create a DataResultSet based on the temp result set
				result = new DataResultSet();
				result.copy(temp);
			}
		}

		if (result.getNumRows() > 0 && result.first())
		{

			// below code is to get languageId which will be used in getLabe.
			String country = result.getStringValueByName("COUNTRY");
			String languageId = country.substring(0, country.indexOf("-"));
			SystemUtils.trace("webAPI_office", " languageId : " + languageId);

			// below code is to get append specialism name to the result set.

			String traceStr = "office";

			if (specialismId != null && specialismId.trim().length() > 0)
			{
				SystemUtils.trace("webAPI_office", " setSpecialismDesc if  : ");
				setSpecialismDesc(result, languageId, traceStr);
			}

			String[] removeRSField = { "specialismid" };
			result.removeFields(removeRSField);
			// below code is used to set proper status code and status message.

			this.m_binder.addResultSet(resultSetName, result);
			this.m_binder.putLocal("StatusMessage", LocaleUtils.encodeMessage("wwWebApiOKMsg", null));
			this.m_binder.putLocal("StatusCode", "UC000");
		}
		else
		{
			this.m_binder.putLocal("StatusMessage", LocaleUtils.encodeMessage("wwInvalidOfficeID", null));
			this.m_binder.putLocal("StatusCode", "UC003");
			throw new ServiceException("Invalid data");
		}

		// release the JDBC connection assigned to this thread (request)
		// which kills the result set 'temp'
		ws.releaseConnection();

		// remove the unused data from locale data m_binder.
		this.m_binder.removeResultSet("LOCALE_DETAILS");
		this.m_binder.removeLocal("sitelocale");
		this.m_binder.removeLocal("locale");

	}

	public void setSpecialismDesc(DataResultSet rsOfficeResults, String languageId, String traceStr)
	{
		SystemUtils.trace("webAPI_" + traceStr, " in setSpecialismDesc method call : ");
		int specialismFieldIndex = rsOfficeResults.getNumFields();
		SystemUtils.trace("webAPI_" + traceStr, " specialismFieldIndex " + specialismFieldIndex);

		Vector<FieldInfo> spcialDesc = new Vector<FieldInfo>();
		FieldInfo specialismfieldInfo = new FieldInfo();
		specialismfieldInfo.m_name = "specialismname";
		specialismfieldInfo.m_type = 6;
		spcialDesc.add(specialismfieldInfo);
		rsOfficeResults.appendFields(spcialDesc);
		String specialismidRS = "";
		String specialismDesc = "";

		SystemUtils.trace("webAPI_" + traceStr, " specialismFieldIndex " + rsOfficeResults);
		for (int i = 0; i < rsOfficeResults.getNumRows(); i++)
		{
			rsOfficeResults.setCurrentRow(i);
			SystemUtils.trace("webAPI_" + traceStr, " officename " + rsOfficeResults.getStringValueByName("officename"));
			specialismidRS = "";
			specialismidRS = rsOfficeResults.getStringValueByName("specialismid");

			if (null == specialismidRS)
			{
				specialismidRS = rsOfficeResults.getStringValueByName("specialismid".toUpperCase());
			}
			if (null == specialismidRS)
			{
				specialismidRS = rsOfficeResults.getStringValueByName("specialismid".toLowerCase());
			}

			SystemUtils.trace("webAPI_" + traceStr, " specialismidRS " + specialismidRS);
			specialismDesc = "";

			try
			{
				if (specialismidRS != null)
				{
					specialismDesc = Converter.getLabel(specialismidRS, OntologyFacade.getOntology("xCategory"), languageId);
				}
			}
			catch (ServiceException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (DataException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			SystemUtils.trace("webAPI_" + traceStr, " specialismDesc " + specialismDesc);

			Vector<String> specialismv = rsOfficeResults.getCurrentRowValues();
			SystemUtils.trace("webAPI_" + traceStr, " specialismv " + specialismv);
			SystemUtils.trace("webAPI_" + traceStr, " specialismFieldIndex " + specialismFieldIndex);
			specialismv.set(specialismFieldIndex, specialismDesc);

		}

	}

	/**
	 * This method is used to get the locale details e.g. country name,domain
	 * id,country region.
	 * 
	 * @throws ServiceException
	 * @throws DataException
	 */
	public void getLocaleDetails() throws ServiceException, DataException
	{

		// get the all required parameters from the binder.
		SystemUtils.trace("webAPI", "Inside HaysWebApiOfficeHandler : getLocaleDetails:");
		String locale = "";

		String providerName = this.m_currentAction.getParamAt(0);
		String resultSetName = this.m_currentAction.getParamAt(1);
		String queryName = this.m_currentAction.getParamAt(2);

		// put the trace to verify all binder parameters.
		SystemUtils.trace("webAPI_locale", "locale : " + locale);
		SystemUtils.trace("webAPI_locale", "providerName : " + providerName);
		SystemUtils.trace("webAPI_locale", "resultSetName : " + resultSetName);
		SystemUtils.trace("webAPI_locale", " queryName : " + queryName);

		if (null != this.m_binder.getLocal("locale") && this.m_binder.getLocal("locale").length() > 0)
		{
			locale = this.m_binder.getLocal("locale");
		}
		else
		{
			locale = this.m_binder.getLocal("SiteLocale");
		}
		this.m_binder.putLocal("sitelocale", locale);

		try
		{
			PageMerger pm = m_service.getPageMerger();
			SystemUtils.trace("webAPI_locale", "Evaluating script :  <$exec setLocale(\"" + locale + "\")$>");
			pm.evaluateScript("<$exec setLocale(\"" + locale + "\")$>");
		}
		catch (IllegalArgumentException e)
		{
			HaysWebApiUtils.HandleExceptions(this.m_binder, "UC001", "wwInvalidSiteLocale");
		}
		catch (IOException e)
		{
			HaysWebApiUtils.HandleExceptions(this.m_binder, "UC001", "wwInvalidSiteLocale");
		}

		Workspace ws = getProviderConnection(providerName);
		DataResultSet result = null;

		// if they specified a predefined query, execute that
		if (queryName != null && queryName.trim().length() > 0)
		{
			// obtain a JDBC result set with the data in it. This result set is
			// temporary, and we must copy it before putting it in the binder
			ResultSet temp = ws.createResultSet(queryName, m_binder);

			// create a DataResultSet based on the temp result set
			result = new DataResultSet();
			result.copy(temp);
		}

		// place the result into the databinder with the appropriate name
		if (result.getNumRows() <= 0)
		{

			String msg = LocaleUtils.encodeMessage("wwInvalidSiteLocale", "");
			this.m_binder.putLocal("StatusMessage", LocaleUtils.encodeMessage("wwInvalidSiteLocale", null));
			this.m_binder.putLocal("StatusCode", "UC001");
			m_binder.removeResultSet("error");
			m_binder.m_resultSets.remove("error");
			throw new ServiceException(msg);

		}
		else
		{
			this.m_binder.addResultSet(resultSetName, result);
		}

		// release the JDBC connection assigned to this thread (request)
		// which kills the result set 'temp'
		ws.releaseConnection();

	}

	/**
	 * This method is used to get the Workspace object from the provider.
	 * 
	 * @return Workspace object
	 * @throws ServiceException
	 * @throws DataException
	 */
	private Workspace getProviderConnection(String providerName) throws ServiceException, DataException
	{

		SystemUtils.trace("hays_search", "provider name to be used =" + providerName);
		// validate the provider name
		if (providerName == null || providerName.length() == 0)
		{
			throw new ServiceException("You must specify a provider name.");
		}
		// validate that the provider is a valid database provider
		Provider p = Providers.getProvider(providerName);
		if (p == null)
		{
			throw new ServiceException("The provider '" + providerName + "' does not exist.");
		}
		else if (!p.isProviderOfType("database"))
		{
			throw new ServiceException("The provider '" + providerName + "' is not a valid provider of type 'database'.");
		}

		Workspace ws = (Workspace) p.getProvider();

		return ws;
	}

	public String getData(String pParamName) throws ServiceException
	{
		String returnString = "";
		Boolean multiCharLocationId = false;
		String locationIdVal="";
		//String domainValue=m_binder.get("domainId");
		try
		{
			returnString = m_binder.get(pParamName);
			if ("null".equalsIgnoreCase(returnString))
			{
				returnString = "";
			}
			else if("locationid".equalsIgnoreCase(pParamName) && returnString.indexOf(" ")> -1)
			{
				SystemUtils.trace("webAPI_OfficeResults", "Inside HaysWebApiOfficeHandler locationId Value : getOfficeResults:"+returnString);
				locationIdVal=URLEncoder.encode(returnString.trim(), "UTF-8").replace("+", "%20");
				SystemUtils.trace("webAPI_OfficeResults", "Inside HaysWebApiOfficeHandler returnString Value : getOfficeResults:"+locationIdVal);
				Pattern pp = Pattern.compile("%20");
				Matcher mm = pp.matcher(locationIdVal);
				if (mm.find()) {
				SystemUtils.trace("webAPI_OfficeResults", "Inside HaysWebApiOfficeHandler : getdata for Location Id");
					multiCharLocationId=true;		    
				}
			}
			
			

			if (returnString!=null && !returnString.trim().matches(VALIDATION_REGEX) && !multiCharLocationId)
			{
				SystemUtils.trace("webAPI_OfficeResults", "Inside HaysWebApiOfficeHandler : getData Inside exception");
				throw new ServiceException("The value of parameter '" + pParamName + "' is not valid.");
			}
		}
		catch (DataException e)
		{
		}
		catch (ServiceException e)
		{
			HaysWebApiUtils.HandleExceptions(this.m_binder, "UC001", e.getMessage());
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return returnString;

	}
}