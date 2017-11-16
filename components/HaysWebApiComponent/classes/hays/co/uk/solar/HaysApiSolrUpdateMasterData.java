package hays.co.uk.solar;

import static intradoc.shared.SharedObjects.getEnvironmentValue;
import static hays.com.commonutils.HaysWebApiUtils.unescapeHTML;
import hays.com.commonutils.HaysWebApiUtils;
import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.data.DataException;
import intradoc.data.Workspace;
import intradoc.server.ServiceHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HaysApiSolrUpdateMasterData extends ServiceHandler
{
	public static final String TRACE_NAME = "HaysApiSolrUpdateMasterData";

	public void updateSolarMetaData() throws DataException, ServiceException
	{
		//SolrCategorySyncAction,IsolarCategories,DsolarCategories,SolrUseProxy,solar,systemdatabase,categories,id,parent,title,description
		int paramCount = 0;
		String url = getEnvironmentValue(this.m_currentAction.getParamAt(paramCount++));
		String insertQuery = this.m_currentAction.getParamAt(paramCount++);
		String deleteQuery = this.m_currentAction.getParamAt(paramCount++);
		String proxyConfig = this.m_currentAction.getParamAt(paramCount++);
		String sourceId = this.m_currentAction.getParamAt(paramCount++);
		String dataSource = this.m_currentAction.getParamAt(paramCount++);
		String jSonRootNode = this.m_currentAction.getParamAt(paramCount++);

		String jSonNodeID = this.m_currentAction.getParamAt(paramCount++);
		String jSonNodeParentID = this.m_currentAction.getParamAt(paramCount++);
		String jSonNodeTitle = this.m_currentAction.getParamAt(paramCount++);
		String jSonNodeDescription = this.m_currentAction.getParamAt(paramCount++);

		String date = HaysWebApiUtils.getFormattedDateForDB();

		m_binder.putLocal("dSchModifyTimestamp", date);
		m_binder.putLocal("dSchCreateTimestamp", date);
		m_binder.putLocal("dSchSourceId", sourceId);
		InputStream lInputStream = null;
		try
		{
			lInputStream = HaysWebApiUtils.getExternalURLStream(proxyConfig, url, "application/xml");
		}
		catch (IOException e)
		{
			SystemUtils.trace(TRACE_NAME, "Exception : " + e.getMessage());
			HaysWebApiUtils.HandleExceptions(m_binder, "UC012", "wwFileNotFormedProperly");
		}

		Scanner lScanner = new Scanner(lInputStream).useDelimiter("\\A");
		String jsonContent = lScanner.hasNext() ? lScanner.next() : "";

		JSONObject lJsonObject;
		JSONArray lJSONArray = null;
		try
		{
			lJsonObject = new JSONObject(jsonContent);
			lJSONArray = lJsonObject.getJSONArray(jSonRootNode);
			Workspace dbProvider = HaysWebApiUtils.getDatabaseProvider(dataSource);

			dbProvider.execute(deleteQuery, m_binder);

			for (int currentRecord = 0; currentRecord < lJSONArray.length(); currentRecord++)
			{
				m_binder.putLocal("dSolar_id", lJSONArray.getJSONObject(currentRecord).getString(jSonNodeID));
				m_binder.putLocal("dSolar_parent_id", lJSONArray.getJSONObject(currentRecord).getString(jSonNodeParentID));
				m_binder.putLocal("dTitle", unescapeHTML(lJSONArray.getJSONObject(currentRecord).getString(jSonNodeTitle)));
				m_binder.putLocal("dDescription", unescapeHTML(lJSONArray.getJSONObject(currentRecord).getString(jSonNodeDescription)));

				dbProvider.execute(insertQuery, m_binder);
			}
		}
		catch (JSONException e)
		{
			SystemUtils.trace(TRACE_NAME, "Exception : " + e.getMessage());
			HaysWebApiUtils.HandleExceptions(m_binder, "UC012", "wwFileNotFormedProperly");
		}
	}
}
