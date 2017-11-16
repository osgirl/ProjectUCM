package hays.custom.multilingual;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import intradoc.indexer.OracleTextUtils; // updated for automation

import sitestudio.SSLinkFunctions;

import intradoc.common.ExecutionContext;
import intradoc.common.GrammarElement;
import intradoc.common.IdcLocale;
import intradoc.common.LocaleResources;
import intradoc.common.LocaleUtils;
import intradoc.common.ScriptExtensionsAdaptor;
import intradoc.common.ScriptInfo;
import intradoc.common.ScriptUtils;
import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.data.DataBinder;
import intradoc.data.DataException;
import intradoc.data.DataResultSet;
import intradoc.data.ResultSet;
import intradoc.data.Workspace;
import intradoc.indexer.IndexerConfig;
import intradoc.server.SearchIndexerUtils;
import intradoc.server.Service;
import intradoc.server.script.ScriptExtensionUtils;
import intradoc.shared.SharedObjects;
import intradoc.shared.UserData;

public class UserLocaleIdocExtension extends ScriptExtensionsAdaptor
{

	private Service m_service = null;
	private DataBinder m_binder = null;
	private UserData m_userData = null;

	public final String[] COLUMNS = { "dDocName", "language", "site", "url" };
	public final String[] PROPERTIES_COLUMNS = { "siteLocale", "domainId", "languageId", "distanceUnit", "displaySalaryRate", "isoCountryCode", "searchWidgetSuffix",
			"countryCordinates", "countryName", "searchFacetsSuffix", "websiteDateFormat", "displayPostcode", "languageCode", "languageLabel", "leftNavInclude", "jobtype_permanent",
			"jobtype_temporary", "jobtype_contract", "jobtype_widget_suffix", "jobtype_currency_pos", "country_region", "location_column", "countryNameISO","portal_url"};
	public final String[] PROPERTIES_COLUMNS_LOCALE = { "siteId", "domainId", "languageId", "distanceUnit", "displaySalaryRate", "isoCountryCode", "searchWidgetSuffix",
			"countryCordinates", "countryName", "searchFacetsSuffix", "websiteDateFormat", "displayPostcode", "languageCode", "languageLabel", "leftNavInclude", "jobtype_permanent",
			"jobtype_temporary", "jobtype_contract", "jobtype_widget_suffix", "jobtype_currency_pos", "country_region", "location_column", "countryNameISO","portal_url"};

	public UserLocaleIdocExtension()
	{
		m_functionTable = new String[] { "setLocale", "getTranslation", "getWebsiteProperties", "getWebsitePropertiesLocale" };
		m_functionDefinitionTable = new int[][] { { 0, 1, GrammarElement.STRING_VAL, -1, GrammarElement.STRING_VAL }, // setLocale:
																														// changes
																														// use's
																														// locale
																														// on
																														// the
																														// fly
				{ 1, 3, GrammarElement.STRING_VAL, GrammarElement.STRING_VAL, GrammarElement.STRING_VAL, -1 }, // getTranslation
				{ 2, 1, GrammarElement.STRING_VAL, -1, -1 }, // getWebsiteProperties
				{ 3, 1, GrammarElement.STRING_VAL, -1, -1 } // getWebsitePropertiesLocale
		};
	}

	/**
	 * This is where the custom IdocScript function is evaluated.
	 */
	public boolean evaluateFunction(ScriptInfo info, Object[] args, ExecutionContext context) throws ServiceException
	{
		/**
		 * This code below is optimized for speed, not clarity. Do not modify
		 * the code below when making new IdocScript functions. It is needed to
		 * prepare the necessary variables for the evaluation and return of the
		 * custom IdocScript functions. Only customize the switch statement
		 * below.
		 */
		int config[] = (int[]) info.m_entry;
		String function = info.m_key;

		int nargs = args.length - 1;
		int allowedParams = config[1];
		if (allowedParams >= 0 && allowedParams != nargs)
		{
			String msg = LocaleUtils.encodeMessage("csScriptEvalNotEnoughArgs", null, function, "" + allowedParams);
			throw new IllegalArgumentException(msg);
		}

		String msg = LocaleUtils.encodeMessage("csScriptMustBeInService", null, function, "Service");
		m_service = ScriptExtensionUtils.getService(context, msg);
		m_binder = m_service.getBinder();

		m_userData = (UserData) context.getCachedObject("UserData");
		if (m_userData == null)
		{
			msg = LocaleUtils.encodeMessage("csUserDataNotAvailable", null, function);
			throw new ServiceException(msg);
		}

		// Do some initial conversion of arguments. Choices of what initial
		// conversions to make
		// are based on frequency of usage. If a function uses nontypical
		// parameters it will
		// have to do its own conversion.
		String sArg1 = null;
		String sArg2 = null;
		String sArg3 = null;
		long lArg1 = 0;
		long lArg2 = 0;
		if (nargs > 0)
		{
			if (config[2] == GrammarElement.STRING_VAL)
			{
				sArg1 = ScriptUtils.getDisplayString(args[0], context);
			}
			else if (config[2] == GrammarElement.INTEGER_VAL)
			{
				lArg1 = ScriptUtils.getLongVal(args[0], context);
			}

		}
		if (nargs > 1)
		{
			if (config[3] == GrammarElement.STRING_VAL)
			{
				sArg2 = ScriptUtils.getDisplayString(args[1], context);
			}
			else if (config[3] == GrammarElement.INTEGER_VAL)
			{
				lArg2 = ScriptUtils.getLongVal(args[1], context);
			}
		}

		if (nargs > 2)
		{
			if (config[4] == GrammarElement.STRING_VAL)
			{
				sArg3 = ScriptUtils.getDisplayString(args[2], context);
			}

		}
		/**
		 * Here is where the custom code should go. The case values coincide
		 * with the "id values" in m_functionDefinitionTable. Perform the
		 * calculations here, and place the result into ONE of the result
		 * variables declared below. Use 'sArg1' and 'sArg2' for the first and
		 * second String arguments for the function (if they exist). Likewise
		 * use 'lArg1' and 'lArg2' for the first and second long integer
		 * arguments.
		 */
		boolean bResult = false; // Used for functions that return a boolean.
		int iResult = 0; // Used for functions that return an integer.
		double dResult = 0.0; // Used for functions that return a double.
		Object oResult = null; // Used for functions that return an object
								// (string).

		switch (config[0])
		{

		case 0: // setLocale - to change user's locale on the fly
			String newLocaleId = sArg1;
			SystemUtils.trace("Translation", "change locale on the fly to " + newLocaleId);
			IdcLocale locale = LocaleResources.getLocale(newLocaleId);
			if (locale == null)
			{
				HashMap<String, HaysWebSite> websitesMap = (HashMap<String, HaysWebSite>) SharedObjects.getObject("Multiling", "WebsitesMap");

				HaysWebSite website = websitesMap.get(newLocaleId);
				SystemUtils.trace("Translation", "Found website for this locale id: " + website);
				if (website != null)
				{
					locale = website.ucmLocale;
					newLocaleId = website.ucmLocaleId;
					SystemUtils.trace("Translation", "New locale: " + locale.m_languageId + ", " + newLocaleId);
				}
			}
			if (locale != null)
			{
				// LocaleResources.initSystemLocale(newLocaleId);
				m_service.setCachedObject("UserLocale", locale);
				m_service.setCachedObject("Language", locale.m_languageId);
				// m_service.getUserData().setProperty("dUserLocale",
				// newLocaleId);
			}
			oResult = newLocaleId;
			break;

		case 1: // getTranslation
			String dDocName = sArg1;
			String siteId = sArg2;
			String dDocType = sArg3;
			SystemUtils.trace("Translation", "sArg1" + sArg1 + sArg2 + sArg3);

			DataResultSet translationsRS = getTranslations(dDocName, siteId, dDocType);
			//SystemUtils.trace("issue20850", translationsRS.toString());
			m_binder.addResultSet("rsContentTranslations", translationsRS);

			break;
		case 2: // getWebsiteProperties
			DataResultSet drs = new DataResultSet(PROPERTIES_COLUMNS);
			HaysWebSite website = getHaysWebsite(sArg1);
			Vector<String> row = null;
			String salary_range = null;
			if (website != null)
			{
				row = new Vector<String>();
				row.add(website.haysLocaleId);
				row.add(website.domainId);
				row.add(website.languageId);
				row.add(website.distance_unit);
				row.add(website.display_salary_rate);
				row.add(website.isoCountryCode);
				row.add(website.searchWidgetSuffix);
				row.add(website.countryCordinates);
				row.add(website.countryName);
				row.add(website.searchFacetsSuffix);
				row.add(website.websiteDateFormat);
				row.add(website.display_postcode);
				row.add(website.languageCode);
				row.add(website.languageLabel);
				row.add(website.leftNavInclude);
				row.add(website.jobtype_permanent);
				row.add(website.jobtype_temporary);
				row.add(website.jobtype_contract);
				row.add(website.jobtype_widget_suffix);
				row.add(website.jobtype_currency_pos);
				row.add(website.country_region);
				row.add(website.location_column);
				row.add(website.countryNameISO);
				row.add(website.portalURL);
				drs.addRow(row);
				salary_range = website.salary_range;
			}

			if (salary_range != null)
			{
				SystemUtils.trace("Translation", "Inside the salary_range method");
				setJobTypeValues(salary_range, m_binder);
			}
			m_binder.addResultSet("rsWebsiteProperties", drs);

			break;
		case 3: // getWebsitePropertiesLocale
			DataResultSet drsl = new DataResultSet(PROPERTIES_COLUMNS_LOCALE);
			HaysWebSite websiteLocale = getHaysWebsiteLocale(sArg1);
			Vector<String> rowL = null;
			String salaryRange = null;
			if (websiteLocale != null)
			{
				rowL = new Vector<String>();
				rowL.add(websiteLocale.websiteId);
				rowL.add(websiteLocale.domainId);
				rowL.add(websiteLocale.languageId);
				rowL.add(websiteLocale.distance_unit);
				rowL.add(websiteLocale.display_salary_rate);
				rowL.add(websiteLocale.isoCountryCode);
				rowL.add(websiteLocale.searchWidgetSuffix);
				rowL.add(websiteLocale.countryCordinates);
				rowL.add(websiteLocale.countryName);
				rowL.add(websiteLocale.searchFacetsSuffix);
				rowL.add(websiteLocale.websiteDateFormat);
				rowL.add(websiteLocale.display_postcode);
				rowL.add(websiteLocale.languageCode);
				rowL.add(websiteLocale.languageLabel);
				rowL.add(websiteLocale.leftNavInclude);
				rowL.add(websiteLocale.jobtype_permanent);
				rowL.add(websiteLocale.jobtype_temporary);
				rowL.add(websiteLocale.jobtype_contract);
				rowL.add(websiteLocale.jobtype_widget_suffix);
				rowL.add(websiteLocale.jobtype_currency_pos);
				rowL.add(websiteLocale.country_region);
				rowL.add(websiteLocale.location_column);
				rowL.add(websiteLocale.countryNameISO);
				rowL.add(websiteLocale.portalURL);
				drsl.addRow(rowL);
				salaryRange = websiteLocale.salary_range;
			}

			if (salaryRange != null)
			{
				SystemUtils.trace("Translation", "Inside the salary_range method");
				setJobTypeValues(salaryRange, m_binder);
			}
			m_binder.addResultSet("rsWebsitePropertiesLocale", drsl);

			break;
		default:
			return false;
		}

		/**
		 * Do not alter code below here
		 */
		args[nargs] = ScriptExtensionUtils.computeReturnObject(config[4], bResult, iResult, dResult, oResult);

		// Handled function.
		return true;
	}

	private void setJobTypeValues(String salary_range, DataBinder m_binder2)
	{
		// TODO Auto-generated method stub
		if (salary_range.indexOf(",") > 0)
		{
			String salaryarr[] = salary_range.split(",");
			m_binder2.putLocal("perm_min", salaryarr[0]);
			m_binder2.putLocal("perm_max", salaryarr[1]);
			m_binder2.putLocal("temp_min", salaryarr[2]);
			m_binder2.putLocal("temp_max", salaryarr[3]);
			m_binder2.putLocal("cont_min", salaryarr[4]);
			m_binder2.putLocal("cont_max", salaryarr[5]);

			SystemUtils.trace("Translation", "Salary Values : Perm _min :" + salaryarr[0]);
		}
	}

	/**
	 * Extracts all secondary websites related to the given one (siteLocaleId)
	 * and contracts possible doc names using the original name (dDocName) and
	 * website's language property
	 * 
	 * @param dDocName
	 *            - original doc name
	 * @param siteLocaleId
	 *            - website ID
	 * @return
	 */
	private DataResultSet getTranslations(String dDocName, String siteId, String dDocType)
	{
		boolean issue20850 = false;
		String trace_name = "issue20850";
		if(dDocName.startsWith("EN-JP_HAYS_372995"))
		{
			issue20850 = true;
		}
		HashMap<String, HaysWebSite> websitesMap = (HashMap<String, HaysWebSite>) SharedObjects.getObject("Multiling", "WebsitesMap");
		HaysWebSite website = websitesMap.get(siteId);
		SystemUtils.trace("Translation", "get translations: Found website for this locale id: " + website);
		if(issue20850)
		{
			SystemUtils.trace(trace_name, "get translations: Found website for this locale id: " + website);
		}
		List<HaysWebSite> secondarySites = new ArrayList<HaysWebSite>();
		String language, docName = null;

		SystemUtils.trace("Translation", "DDoctype value found is " + dDocType);
		if(issue20850)
		{
			SystemUtils.trace(trace_name, "DDoctype value found is " + dDocType);
		}

		DataResultSet translationsRS = new DataResultSet(COLUMNS);
		Vector<String> row = null;
		String nodeId = m_binder.getLocal("nodeId");
		boolean isSecondaryPage = (m_binder.getLocal("ssDocName") == null) ? false : true;
		String url = null;
		boolean prefixInDocNameFlg = false;
		boolean suffixInDocNameFlg = false;

		if (website != null)
		{
			SystemUtils.trace("Translation", "Inside if");
			if(issue20850)
			{
				SystemUtils.trace(trace_name, "Inside if");
			}
			if (website.isPrimarySite())
			{
				secondarySites.addAll(website.secondaryWebsites);
				String sitelocale = website.haysLocaleId.toUpperCase();
				SystemUtils.trace("Translation", "SiteLocale :" + sitelocale + "and " + sitelocale.length());
				if(issue20850)
				{
					SystemUtils.trace(trace_name, "SiteLocale :" + sitelocale + "and " + sitelocale.length());
				}
				if (dDocName.startsWith(sitelocale))
				{
					SystemUtils.trace("Translation", "Okie it starts with");
					if(issue20850)
					{
						SystemUtils.trace(trace_name, "Okie it starts with");
					}
					dDocName = dDocName.substring(sitelocale.length() + 1, dDocName.length());
					prefixInDocNameFlg = true;

				}
				SystemUtils.trace("Translation", "\nDocument Name1212: " + dDocName);
				if(issue20850)
				{
					SystemUtils.trace(trace_name, "\nDocument Name1212: " + dDocName);
				}
			}
			else
			{
				if(issue20850)
				{
					SystemUtils.trace(trace_name, "ELSE ELSE ELSE");
				}
				secondarySites.addAll(website.primaryWebsite.secondaryWebsites);
				secondarySites.remove(website);
				secondarySites.add(website.primaryWebsite);
				language = website.haysLocaleId.split("-")[0].toUpperCase();

				if (dDocName.endsWith(language))
				{
					dDocName = dDocName.substring(0, dDocName.length() - language.length());
					suffixInDocNameFlg = true;
				}

			}
			SystemUtils.trace("Translation", "\nDocument Name: " + dDocName);
			if(issue20850)
			{
				SystemUtils.trace(trace_name, "\nDocument Name: " + dDocName);
			}
			SystemUtils.trace("Translation", "Test trace");

			for (Iterator<HaysWebSite> sitesIter = secondarySites.iterator(); sitesIter.hasNext();)
			{
				SystemUtils.trace("Translation", "Inside for");
				if(issue20850)
				{
					SystemUtils.trace(trace_name, "Inside for");
				}
				website = sitesIter.next();

				SystemUtils.trace("Translation", "\nTranslation Document Name: " + website.websiteId);
				SystemUtils.trace("Translation", "\nwebsite.isPrimarySite(): " + website.isPrimarySite());
				SystemUtils.trace("Translation", "\nwebsite.languageCode.toUpperCase(): " + website.languageCode.toUpperCase() + "" + website.country_region.toUpperCase());
				if(issue20850)
				{
					SystemUtils.trace(trace_name, "\nTranslation Document Name: " + website.websiteId);
					SystemUtils.trace(trace_name, "\nwebsite.isPrimarySite(): " + website.isPrimarySite());
					SystemUtils.trace(trace_name, "\nwebsite.languageCode.toUpperCase(): " + website.languageCode.toUpperCase() + "" + website.country_region.toUpperCase());
					
				}

				if (!website.isPrimarySite())
				{
					SystemUtils.trace("Translation", "Not a primary site");
					if(issue20850)
					{
						SystemUtils.trace(trace_name, "Not a primary site");
					}
					if (website.country_region.equalsIgnoreCase("apac") && !(dDocType.equalsIgnoreCase("Jobs") || dDocType.equalsIgnoreCase("Candidates")))
					{
						if (prefixInDocNameFlg)
						{
							docName = dDocName;
						}
						else
						{
							docName = dDocName + website.haysLocaleId.substring(0, 2).toUpperCase();
						}
					}
					else
					{
						SystemUtils.trace("Translation", "Translation HaysLocaleId: " + website.haysLocaleId.toString());
						if(issue20850)
						{
							SystemUtils.trace(trace_name, "Translation HaysLocaleId: " + website.haysLocaleId.toString());
						}
						docName = dDocName+website.haysLocaleId.split("-")[0].toUpperCase();
						SystemUtils.trace("Translation", "Translation Secondary website Document Name: " + docName);
						if(issue20850)
						{
							SystemUtils.trace(trace_name, "Translation Secondary website Document Name: " + docName);
						}
						
					}

				}
				else
				{

					SystemUtils.trace("Translation", "Inside else");
					if(issue20850)
					{
						SystemUtils.trace(trace_name, "Inside else");
					}
					if (website.country_region.equalsIgnoreCase("apac") && !(dDocType.equalsIgnoreCase("Jobs") || dDocType.equalsIgnoreCase("Candidates")))
					{

						if (suffixInDocNameFlg)
						{
							docName = dDocName;
						}
						else
						{
							docName = website.haysLocaleId.toUpperCase() + "_" + dDocName;
						}
					}
					else
						docName = dDocName;
				}

				row = new Vector<String>();
				row.add(docName);
				row.add(website.languageLabel);
				row.add(website.websiteId);
				if (isSecondaryPage)
					url = SSLinkFunctions.computeLinkUrl(m_service, docName, nodeId, website.websiteId);
				else
					url = SSLinkFunctions.computeNodeLinkUrl(m_service, nodeId, website.websiteId);

				SystemUtils.trace("Translation", "\nTranslation Document Name: " + dDocName);
				SystemUtils.trace("Translation", "\nTranslation Document url11: " + url);
				if(issue20850)
				{
					SystemUtils.trace(trace_name, "\nTranslation Document Name: " + dDocName);
					SystemUtils.trace(trace_name, "\nTranslation Document url11: " + url);
				}

				if (url != null && url.length() > 0)
				{
					// SiteStudio adds 'ssSourceSiteId' to the url so we remove
					// it
					row.add(url.replaceFirst("\\?ssSourceSiteId=.*", ""));
					translationsRS.addRow(row);
				}

				SystemUtils.trace("Translation", "Possible translations: " + row);
				if(issue20850)
				{
					SystemUtils.trace(trace_name, "Possible translations: " + row);
				}
			}
		}
		DataResultSet translationsRSFiltered = new DataResultSet();
		// make sure that all translation docs have beed relesed by merging with
		// the
		// result of the search query over the Index table
		if (!translationsRS.isEmpty())
		{
			ResultSet docNamesRS = retrieveTranslationDocs(dDocName, siteId, dDocType, prefixInDocNameFlg);
			if(issue20850)
			{
				SystemUtils.trace(trace_name, "docNamesRS" + docNamesRS.toString());
			}
			if (docNamesRS != null)
			{
				translationsRSFiltered = filterReleasedTranslations(translationsRS, docNamesRS);
			}
			if(issue20850)
			{
				SystemUtils.trace(trace_name, "translationsRS" + translationsRSFiltered.toString());
			}
		}
		return translationsRSFiltered;
	}

	/**
	 * Get released translations using the base doc name + languages of the
	 * relates websites
	 * 
	 * @param baseDocName
	 *            - base doc name
	 * @return
	 */
	private ResultSet retrieveTranslationDocs(String baseDocName, String siteId, String dDocType, boolean prefixInDocNameFlg)
	{
		Workspace ws = m_service.getWorkspace();
		DataBinder params = new DataBinder();
		params.putLocal("indexTable", getIndexerTbl());
		params.putLocal("dDocName", baseDocName.replaceAll("_|-", "."));
		SystemUtils.trace("Translation", "Ddocname :" + baseDocName.replaceAll("_|-", "."));
		ResultSet docNamesRS = null;
		String query = "QindexedTranslations";
		HashMap<String, HaysWebSite> websitesMap = (HashMap<String, HaysWebSite>) SharedObjects.getObject("Multiling", "WebsitesMap");
		HaysWebSite website = websitesMap.get(siteId);
		if (website.country_region.equalsIgnoreCase("apac") && !(dDocType.equalsIgnoreCase("Jobs") || dDocType.equalsIgnoreCase("Candidates")) && prefixInDocNameFlg)
			query = "QindexedTranslationsForAPAC";

		try
		{
			SystemUtils.trace("Translation", "Before executing query");
			SystemUtils.trace("Translation", "Query executing is :" + query);
			docNamesRS = ws.createResultSet(query, params);
			SystemUtils.trace("Translation", "docNameRS" + docNamesRS);
			/*
			 * DataResultSet d = new DataResultSet(); d.copy(docNamesRS);
			 * SystemUtils.trace("Translation","number of rows"+d.getNumRows());
			 */
			docNamesRS.first();
			SystemUtils.trace("Translation", "docNameRS!!!" + docNamesRS.getFieldName(0) + "," + docNamesRS.getFieldName(1) + docNamesRS.getStringValueByName("dDocName"));
		}
		catch (DataException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return docNamesRS;
	}

	/**
	 * docNamesRS contains only released documents , when merged with the set of
	 * possible translations only those that are released will have column dID
	 * populated
	 * 
	 * @param translationsRS
	 * @param docNamesRS
	 */
	private DataResultSet filterReleasedTranslations(DataResultSet translationsRS, ResultSet docNamesRS)
	{
		try
		{
			translationsRS.mergeFields(docNamesRS);
			translationsRS.merge("dDocName", docNamesRS, true);
			/*
			 * DataResultSet d1 = new DataResultSet(); d1.copy(translationsRS);
			 * SystemUtils.trace("Translation", "Number Of rows"+d1.getNumRows()
			 * + "" +d1.getFieldName(4));
			 */
			String did = null;

			translationsRS.first();
			do
			{
				did = translationsRS.getStringValueByName("dID");
				SystemUtils.trace("Translation", "DID :" + did + "" + translationsRS.getStringValueByName("dDocName"));
				if (did == null || did.length() == 0)
					translationsRS.deleteCurrentRow();
			}
			while (translationsRS.next());
			SystemUtils.trace("Translation", "Retrieved doc names: " + translationsRS);
		}
		catch (DataException e)
		{
			e.printStackTrace();
		}
		return translationsRS;
	}

	private String getIndexerTbl()
	{
		String m_tableName = "IDCTEXT1";
		try
		{
			IndexerConfig m_config = SearchIndexerUtils.getIndexerConfig(null, "update");
			String m_activeIndex = intradoc.shared.ActiveIndexState.getActiveProperty("ActiveIndex");
			m_tableName = OracleTextUtils.getTableName(m_activeIndex, m_config);	//updated for automation
			SystemUtils.trace("Translation", "Indexer TBL: " + m_activeIndex + ", " + m_tableName);

		}
		catch (Exception e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return m_tableName;
	}

	private HaysWebSite getHaysWebsite(String siteId)
	{
		HaysWebSite website = null;
		HashMap<String, HaysWebSite> websitesMap = (HashMap<String, HaysWebSite>) SharedObjects.getObject("Multiling", "WebsitesMap");
		website = websitesMap.get(siteId);
		SystemUtils.trace("Translation", "getHaysWebsite: Found website for this locale id: " + website);
		return website;
	}

	private HaysWebSite getHaysWebsiteLocale(String locale)
	{
		HaysWebSite website = null;
		HashMap<String, HaysWebSite> websitesMap = (HashMap<String, HaysWebSite>) SharedObjects.getObject("Multiling", "siteLocaleMap");
		website = websitesMap.get(locale);
		SystemUtils.trace("Translation", "getHaysWebsiteLocale: Found website using  locale" + locale + " is: " + website);
		return website;
	}
}
