package hays.com.copywebsite;

import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.data.DataException;
import intradoc.data.DataResultSet;
import intradoc.data.ResultSet;
import intradoc.data.Workspace;
import intradoc.provider.Provider;
import intradoc.provider.Providers;
import intradoc.serialize.DataBinderLocalizer;
import intradoc.server.ServiceHandler;
import intradoc.shared.FieldDef;
import intradoc.shared.SharedObjects;
import intradoc.shared.ViewFields;

import java.util.Properties;
import java.util.Vector;

public class HaysCopyWebsiteFormAction extends ServiceHandler
{
	public static final String TRACE_NAME = "HAYS_COPY_WEBSITE_COMPONENT";

	public void getBinderValues() throws DataException, ServiceException
	{
		Properties oldLocalData = null;
		Properties newLocalData = null;
		ViewFields fields = new ViewFields(this.m_service);
		fields.addStandardDocFields();
		fields.addDocDateFields(true, true);
		fields.addMetaFields(SharedObjects.getTable("DocMetaDefinition"));
		Vector v = fields.m_viewFields;
		int size = v.size();
		for (int i = 0; i < size; i++)
		{
			FieldDef def = (FieldDef) v.elementAt(i);
			if (def.m_type != null && !def.m_type.equalsIgnoreCase("text"))
			{
				m_binder.m_blFieldTypes.put(def.m_name, def.m_type);
			}
		}
		DataBinderLocalizer localizer = new DataBinderLocalizer(m_binder, this.m_service);
		localizer.localizeBinder(DataBinderLocalizer.ALL);
		oldLocalData = m_binder.getLocalData();
		newLocalData = (Properties) oldLocalData.clone();
		m_binder.setLocalData(newLocalData);
	}

	public void createAccount() throws DataException, ServiceException
	{
		String newAccount = m_binder.get("newaccount");
		m_binder.putLocal("dDocAccount", newAccount);
		
		String createAccount = m_binder.get("createAccount");
		
		if ("Yes".equalsIgnoreCase(createAccount))
		{
			try
			{
				m_service.executeServiceSimple("ADD_DOC_ACCOUNT");
			}
			catch (Exception e)
			{
				SystemUtils.trace(TRACE_NAME, "Exception Caught while creating Account: " + e.getMessage());
			}
		}
	}

	public void insertHayswebsiteDBValues() throws DataException, ServiceException
	{
		DataResultSet OutputResult = null;
		String providerName = m_currentAction.getParamAt(0);
		String QueryName = m_currentAction.getParamAt(1);
		if (providerName == null || providerName.length() == 0)
		{
			throw new ServiceException("You must specify a provider name.");
		}
		String oldsite = m_binder.get("oldsite");
		String newlocale = m_binder.get("newlocale");
		String newsiteid = m_binder.get("newsite");
		String siteType = m_binder.get("sitetype");
		SystemUtils.trace(TRACE_NAME, " Site type before insert to DB: " + siteType);
		if (!"3".equals(siteType))
		{
			
			Provider p = Providers.getProvider(providerName);
			if (p == null)
				throw new ServiceException((new StringBuilder("The provider '")).append(providerName).append("' does not exist.")
						.toString());

			if (!p.isProviderOfType("database"))
				throw new ServiceException((new StringBuilder("The provider '")).append(providerName)
						.append("' is not a valid provider of type 'database'.").toString());
			Workspace ws = (Workspace) p.getProvider();
			try
			{
				String strSiteTypeValue = "";
				if("1".equals(siteType)){
					strSiteTypeValue="UK";
				}
				if("2".equals(siteType)){
					strSiteTypeValue="hays.com";
				}
				
				String InsertQuery = "Insert into hayswebsites (SITEID,SITELOCALE,ISPRIMARY,PRIMARYSITEID,COUNTRY,ISOCOUNTRYCODE,DOMAINID, "
						+ "LANGUAGEID,LANGUAGELABEL,UCM_LOCALE,PRIMARYSITELOCALE,DISTANCE_UNIT,DISPLAY_SALARY_RATE, "
						+ "SRCH_WGT_SFX,LATLONG,SRCH_FCT_SFX,WEBSITE_DATE_FORMAT,DISPLAY_POSTCODE,LANGUAGECODE, "
						+ "LEFTNAVINCLUDE,JOBTYPE_PERMANENT,JOBTYPE_TEMPORARY,JOBTYPE_CONTRACT,JOBTYPE_WGT_SFX, JOBTYPE_CURRENCY_POS,"
						+ "SITELOCALEDUP,DATAFILEPREFIX,CONTRY_REGION,SALARY_RANGE,LOCATION_COLUMN,SITE_TYPE) (Select '"
						+ newsiteid
						+ "' as SITEID, '"
						+ newlocale
						+ "' as SITELOCALE,"
						+ "ISPRIMARY,PRIMARYSITEID,COUNTRY,ISOCOUNTRYCODE,(select max(domainid)+1 from hayswebsites)as DOMAINID, LANGUAGEID,LANGUAGELABEL,UCM_LOCALE,PRIMARYSITELOCALE,"
						+ "DISTANCE_UNIT,DISPLAY_SALARY_RATE, SRCH_WGT_SFX,LATLONG,SRCH_FCT_SFX,WEBSITE_DATE_FORMAT,DISPLAY_POSTCODE,"
						+ "LANGUAGECODE, LEFTNAVINCLUDE,JOBTYPE_PERMANENT,JOBTYPE_TEMPORARY,JOBTYPE_CONTRACT,JOBTYPE_WGT_SFX, "
						+ "JOBTYPE_CURRENCY_POS,SITELOCALEDUP,'"
						+newlocale
						+ "' as DATAFILEPREFIX,"
						+"CONTRY_REGION,SALARY_RANGE,LOCATION_COLUMN ,'"
						+strSiteTypeValue
						+ "' as SITE_TYPE "
						+ "from hayswebsites where siteid = '" + oldsite + "')";
				SystemUtils.trace(TRACE_NAME, "Hayswebsite Query : " + InsertQuery);
				if (QueryName != null && QueryName.trim().length() > 0)
				{
					ResultSet temp = ws.createResultSet(QueryName, m_binder);
					OutputResult = new DataResultSet();
					OutputResult.copy(temp);
				}
				SystemUtils.trace(TRACE_NAME, "OUTPUT RESULT getNumRows : " + OutputResult.getNumRows());
				if (OutputResult != null && OutputResult.getNumRows() == 0)
				{
					Long temp = ws.executeSQL(InsertQuery);
				}
			}
			catch(Exception e)
			{
				SystemUtils.trace(TRACE_NAME, "Exception Caught while creating entry in hayswebsites table: " + e.getMessage());
			}
		}
	}
	
	public void getResultSetValueFromDB() throws DataException, ServiceException
	{
		String providerName = m_currentAction.getParamAt(0);
		String resultSetName = m_currentAction.getParamAt(1);
		String queryName = m_currentAction.getParamAt(2);

		if (providerName == null || providerName.length() == 0)
		{
			throw new ServiceException("You must specify a provider name.");
		}
		Provider p = Providers.getProvider(providerName);
		if (p == null)
			throw new ServiceException((new StringBuilder("The provider '")).append(providerName).append("' does not exist.").toString());

		if (!p.isProviderOfType("database"))
			throw new ServiceException((new StringBuilder("The provider '")).append(providerName)
					.append("' is not a valid provider of type 'database'.").toString());
		Workspace ws = (Workspace) p.getProvider();
		DataResultSet result = null;
		if (queryName != null && queryName.trim().length() > 0)
		{
			ResultSet temp = ws.createResultSet(queryName, m_binder);
			result = new DataResultSet();
			result.copy(temp);
		}
		m_binder.addResultSet(resultSetName, result);
		ws.releaseConnection();
	}
	
}
