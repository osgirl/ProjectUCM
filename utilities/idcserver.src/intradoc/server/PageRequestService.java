/*    */ package intradoc.server;
/*    */ 
/*    */ import intradoc.common.DynamicHtml;
/*    */ import intradoc.common.LocaleUtils;
/*    */ import intradoc.common.ServiceException;
/*    */ import intradoc.data.DataBinder;
/*    */ import intradoc.data.DataException;
/*    */ import intradoc.data.DataSerializeUtils;
/*    */ import intradoc.shared.PluginFilters;
/*    */ 
/*    */ public class PageRequestService extends Service
/*    */ {
/* 31 */   protected DynamicHtml m_page = null;
/*    */ 
/*    */   public void createHandlersForService()
/*    */     throws ServiceException, DataException
/*    */   {
/* 41 */     super.createHandlersForService();
/* 42 */     createHandlers("PageRequestService");
/*    */   }
/*    */ 
/*    */   @IdcServiceAction
/*    */   public void pageRequest() throws ServiceException, DataException
/*    */   {
/* 48 */     PluginFilters.filter("pageRequest", this.m_workspace, this.m_binder, this);
/*    */ 
/* 50 */     String name = this.m_binder.get("Page");
/*    */ 
/* 53 */     this.m_page = getTemplatePage(name);
/* 54 */     if (this.m_page == null)
/*    */     {
/* 56 */       this.m_page = this.m_pageMerger.appGetHtmlResource(name);
/*    */     }
/* 58 */     if (this.m_page != null)
/*    */       return;
/* 60 */     createServiceException(null, LocaleUtils.encodeMessage("csPageRequestTemplateFormNotFound", null, name));
/*    */   }
/*    */ 
/*    */   protected void buildResponsePage(boolean isError)
/*    */     throws ServiceException
/*    */   {
/* 68 */     if (isError)
/*    */     {
/* 70 */       super.buildResponsePage(isError);
/* 71 */       return;
/*    */     }
/*    */ 
/* 74 */     String isoEncoding = null;
/* 75 */     String javaEncoding = DataSerializeUtils.determineEncoding(this.m_binder, null);
/* 76 */     if (javaEncoding != null)
/*    */     {
/* 78 */       this.m_binder.setEnvironmentValue("ClientEncoding", javaEncoding);
/* 79 */       isoEncoding = DataSerializeUtils.getIsoEncoding(javaEncoding);
/*    */     }
/* 81 */     if (isoEncoding != null)
/*    */     {
/* 83 */       this.m_binder.setEnvironmentValue("PageCharset", isoEncoding);
/*    */     }
/*    */ 
/* 86 */     if (this.m_page == null)
/*    */     {
/* 88 */       throw new ServiceException(LocaleUtils.encodeMessage("csPageMergerTemplateNotSpecified", null));
/*    */     }
/*    */ 
/* 91 */     this.m_pageMerger.setIsViewablePageOutput(true);
/* 92 */     merge(this.m_page);
/* 93 */     sendResponse();
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 98 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 71088 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.PageRequestService
 * JD-Core Version:    0.5.4
 */