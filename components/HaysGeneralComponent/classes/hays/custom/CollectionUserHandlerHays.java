package hays.custom;

import java.io.IOException;

import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import collections.CollectionStructure;
import collections.CollectionUserHandler;

import intradoc.common.IdcLocale;
import intradoc.common.Report;
import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.data.DataBinder;
import intradoc.data.DataException;
import intradoc.data.DataResultSet;
import intradoc.data.ResultSet;
import intradoc.data.ResultSetUtils;
import intradoc.data.Workspace;
import intradoc.provider.Provider;
import intradoc.provider.Providers;
import intradoc.server.Service;
import intradoc.server.ServiceHandler;

public class CollectionUserHandlerHays extends CollectionUserHandler {
	
	public CollectionUserHandlerHays() throws ServiceException, DataException
	{
		super();
	}
	
	public void canPlaceInCollectionHays()
	   throws ServiceException, DataException
	 {
	    Report.trace("CollectionUserHandlerHays", "Inside CollectionUserHandlerHays",null);
		CollectionStructure col = getSpecifiedCollection();
	   if (col.getType() != 0)
	   {
	     return;
	   }
	   String title = this.m_binder.getLocal("toContentName");
	   if (title == null)
	   {
	     title = this.m_binder.getLocal("dOriginalName");
	     if ((title != null) && (title.length() == 0)) {
	       title = null;
	     } else if (this.m_service.isConditionVarTrue("isCopyRevision"))
	     {
	       title = new StringBuilder().append("Copy.").append(title).toString();
	       this.m_binder.putLocal("dOriginalName", title);
	     }
	   }
	   if (title == null)
	   {
	     title = titleFromPrimaryFile(new String[] { "primaryFile", "primaryFile:path" });
	   }
	   if (title == null)
	   {
	     ResultSet rs = this.m_binder.getResultSet("DOC_INFO");
	     if ((rs != null) && (!rs.isEmpty()))
	     {
	       rs.first();
	       title = ResultSetUtils.getValue(rs, "dOriginalName");
	     }
	   }
	   Report.trace("CollectionUserHandlerHays", "title "+title+" ddoctitle "+this.m_binder.getLocal("dDocTitle"),null);
	   //Report.trace("CollectionUserHandlerHays", "binder "+this.m_binder, null);
	   //added fix
	   if (title == null)
	   {
	     title = this.m_binder.getLocal("dDocTitle");
	   }
	   //added fix
	   if (title == null)
	   {
	     throw new ServiceException("Title not defined");
	   }
	   canPlaceInCollection(col, title);
	 }
}

