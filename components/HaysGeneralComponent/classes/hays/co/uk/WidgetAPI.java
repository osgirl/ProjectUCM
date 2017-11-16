package hays.co.uk;

import intradoc.common.LocaleUtils;
import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.data.DataException;
import intradoc.data.DataResultSet;
import intradoc.data.ResultSet;
import intradoc.data.Workspace;
import intradoc.provider.Provider;
import intradoc.provider.Providers;
import intradoc.server.ServiceHandler;

public class WidgetAPI extends ServiceHandler
{

	public void getWidgetDetails() throws ServiceException, DataException
	{
		// get the all required parameters from the binder.
		SystemUtils.trace("widgetdata", "Inside WidgetDataHandler : getWidgetDetails:");

		String locale = this.m_binder.getLocal("locale");
		locale = (locale.trim().length() > 0) ? locale : "%";
		SystemUtils.trace("widgetdata", "Inside WidgetDataHandler Locale: " + locale);

		String page_name = this.m_binder.getLocal("page_name");
		page_name = (page_name.trim().length() > 0) ? page_name : "%";
		SystemUtils.trace("widgetdata", "Inside WidgetDataHandler Page Name: " + page_name);

		String providerName = this.m_currentAction.getParamAt(0);
		SystemUtils.trace("widgetdata", "Inside WidgetDataHandler Provider Name: " + providerName);

		String resultSetName = this.m_currentAction.getParamAt(1);
		SystemUtils.trace("widgetdata", "Inside WidgetDataHandler Result Set Name: " + resultSetName);

		String queryName = this.m_currentAction.getParamAt(2);
		SystemUtils.trace("widgetdata", "Inside WidgetDataHandler Query Name: " + queryName);
		String queryName1 = this.m_currentAction.getParamAt(3);
		SystemUtils.trace("widgetdata", "Inside WidgetDataHandler Query Name: " + queryName1);

		this.m_binder.putLocal("locale", locale);
		this.m_binder.putLocal("page_name", page_name);
		Workspace ws = getProviderConnection(providerName);

		DataResultSet result = null;

		if (queryName != null && queryName.trim().length() > 0)
		{
			ResultSet temp = ws.createResultSet(queryName, m_binder);
			result = new DataResultSet();
			result.copy(temp);
		}

		this.m_binder.addResultSet(resultSetName, result);
		this.m_binder.putLocal("StatusMessage", LocaleUtils.encodeMessage("wwWebApiOKMsg", null));
		this.m_binder.putLocal("StatusCode", "UC000");
		// release the JDBC connection assigned to this thread (request)
		// which kills the result set 'temp'
		ws.releaseConnection();

		this.m_binder.removeResultSet("WidgetRS");
		this.m_binder.removeResultSet("LOCALE_DETAILS");
		this.m_binder.removeLocal("locale");
		this.m_binder.removeLocal("page_name");

	}

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

	public void getPageWidgetsFromDB() throws DataException, ServiceException
	{
		String providerName = m_currentAction.getParamAt(0);
		String resultSetName = m_currentAction.getParamAt(1);
		String queryName = m_currentAction.getParamAt(2);
		SystemUtils.trace("widgetdata", "Inside WidgetDataHandler Provider Name: " + providerName);
		SystemUtils.trace("widgetdata", "Inside WidgetDataHandler Resultset Name: " + resultSetName);
		SystemUtils.trace("widgetdata", "Inside WidgetDataHandler Query Name: " + queryName);

		Workspace ws = getProviderConnection(providerName);

		DataResultSet result = null;
		if (queryName != null && queryName.trim().length() > 0)
		{
			ResultSet temp = ws.createResultSet(queryName, m_binder);
			result = new DataResultSet();
			result.copy(temp);
			SystemUtils.trace("widgetdata", "Inside getPageWidgetsFromDB If condition: ");
		}
		m_binder.addResultSet(resultSetName, result);
		ws.releaseConnection();
	}
	
	public void getContentWidgetsFromDB() throws DataException, ServiceException
	{
		String providerName = m_currentAction.getParamAt(0);
		String resultSetName = m_currentAction.getParamAt(1);
		String queryName = m_currentAction.getParamAt(2);
		SystemUtils.trace("widgetdata", "Inside WidgetDataHandler Provider Name: " + providerName);
		SystemUtils.trace("widgetdata", "Inside WidgetDataHandler Resultset Name: " + resultSetName);
		SystemUtils.trace("widgetdata", "Inside WidgetDataHandler Query Name: " + queryName);

		Workspace ws = getProviderConnection(providerName);

		DataResultSet result = null;
		if (queryName != null && queryName.trim().length() > 0)
		{
			ResultSet temp = ws.createResultSet(queryName, m_binder);
			result = new DataResultSet();
			result.copy(temp);
			SystemUtils.trace("widgetdata", "Inside getPageWidgetsFromDB If condition: ");
		}
		m_binder.addResultSet(resultSetName, result);
		ws.releaseConnection();
	}

	public void setWidgetDetails() throws ServiceException, DataException
	{
		SystemUtils.trace("widgetdata", "Inside WidgetDataHandler method.");
		String locale = this.m_binder.getLocal("locale");
		String page_name = this.m_binder.getLocal("page_name");
		String widget_name = this.m_binder.getLocal("widget_name");
		String widget_order = this.m_binder.getLocal("widget_order");
		String providerName = this.m_currentAction.getParamAt(0);
		SystemUtils.trace("widgetdata", "locale: " + locale);
		SystemUtils.trace("widgetdata", "page_name: " + page_name);
		SystemUtils.trace("widgetdata", "widget_name: " + widget_name);
		SystemUtils.trace("widgetdata", "widget_order: " + widget_order);
		SystemUtils.trace("widgetdata", "Provider Name: " + providerName);

		String softDeleteQuery = "UPDATE widget_mapping SET is_active=0,SCHCREATETIMESTAMP=current_timestamp,SCHMODIFYTIMESTAMP=current_timestamp"
				+ " where locale = '" + locale + "' and page_name = '" + page_name + "'";
		String insertQuery = null;

		if (locale != null && locale.trim().length() > 0 && page_name != null && page_name.trim().length() > 0 && widget_name != null
				)
		{
			insertQuery = "insert into widget_mapping (MAPPING_ID,PAGE_NAME,LOCALE,WIDGET_NAME,WIDGET_ORDER,"
					+ "IS_ACTIVE,SCHCREATETIMESTAMP,SCHSOURCEID,SCHMODIFYTIMESTAMP) values (WIDGET_MAPPING_SEQ.NEXTVAL,'" + page_name
					+ "','" + locale + "','param_widgetname',param_widgetorder,1, current_timestamp, 'HAYS', current_timestamp)";
			String[] widgets = widget_name.split(",");
			int length = widgets.length;
			SystemUtils.trace("widgetdata", "length: " + length);
			try
			{
				Workspace ws = getProviderConnection(providerName);
				SystemUtils.trace("widgetdata", "delQuery: " + softDeleteQuery);
				ws.executeSQL(softDeleteQuery);
				for (int widgetOrder = 0; widgetOrder < length; widgetOrder++)
				{
					if(widgets[widgetOrder].trim().length() >0){
						SystemUtils.trace("widgetdata", "insertQ: " + insertQuery);
						ws.executeSQL(insertQuery.replace("param_widgetname", widgets[widgetOrder]).replace("param_widgetorder",
								Integer.toString(widgetOrder)));
					}
				}
				ws.releaseConnection();
			}
			catch (Exception ex)
			{
				SystemUtils.trace("widgetdata", "Exception: " + ex.getMessage());
			}
		}
	}
	
	public void setContentWidgetDetails() throws ServiceException, DataException
	{
		SystemUtils.trace("widgetdata", "Inside ContentWidgetDataHandler method.");
		String locale = this.m_binder.getLocal("locale");
		String title = this.m_binder.getLocal("title");
		String widget_name = this.m_binder.getLocal("widget_name");
		String label = widget_name+" (c)";
		String content = this.m_binder.getLocal("content");
		String link = this.m_binder.getLocal("link");
		String button_name = this.m_binder.getLocal("button_name");
		
		
		String providerName = this.m_currentAction.getParamAt(0);
		SystemUtils.trace("contentwidgetdata", "locale: " + locale);
		SystemUtils.trace("contentwidgetdata", "title: " + title);
		SystemUtils.trace("contentwidgetdata", "widget_name: " + widget_name);
		SystemUtils.trace("contentwidgetdata", "content: " + content);
		SystemUtils.trace("contentwidgetdata", "link: " + link);
		SystemUtils.trace("contentwidgetdata", "button_name: " + button_name);
		SystemUtils.trace("contentwidgetdata", "Provider Name: " + providerName);

		
		String insertQuery = null;

		if (locale != null && title != null && content != null && link != null && widget_name != null)
		{
			insertQuery = "insert into content_widget_master (content_widget_id,locale,widget_name,widget_title,widget_content,widget_link,widget_label,button_name) values (CONTENT_WIDGET_MAPPING_SEQ.NEXTVAL,'" + locale + "','" + widget_name + "','" + title + "','" + content + "','" + link + "','" + label + "','" + button_name + "')";
			try
			{
				Workspace ws = getProviderConnection(providerName);
				SystemUtils.trace("contentwidgetdata", "insertQuery: " + insertQuery);
				ws.executeSQL(insertQuery);
				ws.releaseConnection();
			}
			catch (Exception ex)
			{
				SystemUtils.trace("contentwidgetdata", "Exception: " + ex.getMessage());
			}
		}
	}

}
