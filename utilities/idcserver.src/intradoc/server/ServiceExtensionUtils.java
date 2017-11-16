/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.ResultSet;
/*     */ import java.io.IOException;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class ServiceExtensionUtils
/*     */ {
/*     */   public static void executeDocMetaUpdateSideEffect(ResultSet curData, DataBinder binder, Service service)
/*     */     throws DataException, ServiceException
/*     */   {
/*  39 */     if (!curData.isRowPresent()) {
/*     */       return;
/*     */     }
/*     */ 
/*  43 */     binder.addResultSet("DOC_INFO_CURRENT", curData);
/*  44 */     DataBinder pageMergerBinder = service.m_pageMerger.getDataBinder();
/*  45 */     if (pageMergerBinder != binder)
/*     */     {
/*  47 */       pageMergerBinder.addResultSet("DOC_INFO_CURRENT", curData);
/*     */     }
/*     */     else
/*     */     {
/*  51 */       pageMergerBinder = null;
/*     */     }
/*     */     try
/*     */     {
/*  55 */       executeResourceIncludeSubService("update_metadata_sideeffect_extension", "updateSideEffectServices", binder, service);
/*     */     }
/*     */     finally
/*     */     {
/*  60 */       binder.removeResultSet("DOC_INFO_CURRENT");
/*  61 */       if (pageMergerBinder != null)
/*     */       {
/*  63 */         pageMergerBinder.removeResultSet("DOC_INFO_CURRENT");
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void executeDocDeleteSideEffect(ResultSet curData, DataBinder binder, Service service)
/*     */     throws DataException, ServiceException
/*     */   {
/*  72 */     if (!curData.isRowPresent()) {
/*     */       return;
/*     */     }
/*     */ 
/*  76 */     binder.addResultSet("DOC_INFO_DELETE", curData);
/*  77 */     DataBinder pageMergerBinder = service.m_pageMerger.getDataBinder();
/*  78 */     if (pageMergerBinder != binder)
/*     */     {
/*  80 */       pageMergerBinder.addResultSet("DOC_INFO_DELETE", curData);
/*     */     }
/*     */     else
/*     */     {
/*  84 */       pageMergerBinder = null;
/*     */     }
/*     */     try
/*     */     {
/*  88 */       executeResourceIncludeSubService("delete_document_sideeffect_extension", "deleteSideEffectServices", binder, service);
/*     */     }
/*     */     finally
/*     */     {
/*  93 */       binder.removeResultSet("DOC_INFO_DELETE");
/*  94 */       if (pageMergerBinder != null)
/*     */       {
/*  96 */         pageMergerBinder.removeResultSet("DOC_INFO_DELETE");
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void executeResourceIncludeSubService(String resInclude, String lookupKey, DataBinder binder, Service service)
/*     */     throws DataException, ServiceException
/*     */   {
/* 105 */     if (lookupKey != null)
/*     */     {
/* 107 */       binder.putLocal(lookupKey, "");
/*     */     }
/*     */     try
/*     */     {
/* 111 */       service.m_pageMerger.evaluateResourceInclude(resInclude);
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 115 */       throw new DataException(e.getMessage(), e);
/*     */     }
/* 117 */     if (lookupKey == null)
/*     */       return;
/* 119 */     String serviceNames = binder.getLocal(lookupKey);
/* 120 */     if ((serviceNames == null) || (serviceNames.length() <= 0))
/*     */       return;
/* 122 */     Vector services = StringUtils.parseArray(serviceNames, ',', ',');
/* 123 */     for (int i = 0; i < services.size(); ++i)
/*     */     {
/* 125 */       String serviceName = (String)services.elementAt(i);
/* 126 */       service.executeService(serviceName);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 134 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 93984 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.ServiceExtensionUtils
 * JD-Core Version:    0.5.4
 */