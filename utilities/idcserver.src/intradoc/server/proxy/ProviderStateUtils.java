/*     */ package intradoc.server.proxy;
/*     */ 
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.provider.Provider;
/*     */ import intradoc.provider.ProviderInterface;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.util.Properties;
/*     */ 
/*     */ public class ProviderStateUtils
/*     */ {
/*     */   public static void updateProviderConnectionState(Provider provider, boolean isSuccess)
/*     */   {
/*  37 */     if (isSuccess)
/*     */     {
/*  39 */       provider.markState("active");
/*     */     }
/*     */     else
/*     */     {
/*  43 */       Properties provState = provider.getProviderState();
/*  44 */       provState.put("LastActivityTs", String.valueOf(System.currentTimeMillis()));
/*     */ 
/*  47 */       int retryCount = NumberUtils.parseInteger(provState.getProperty("RetryCount"), 0);
/*  48 */       ++retryCount;
/*  49 */       provState.put("RetryCount", String.valueOf(retryCount));
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void testConnection(Provider provider)
/*     */   {
/*  55 */     ProviderInterface providerObj = (ProviderInterface)provider.getProvider();
/*  56 */     if (providerObj == null)
/*     */     {
/*  59 */       return;
/*     */     }
/*     */ 
/*  62 */     DataBinder binder = new DataBinder();
/*  63 */     boolean isSuccess = true;
/*     */     try
/*     */     {
/*  66 */       providerObj.testConnection(binder, null);
/*  67 */       checkReturnData(provider, binder);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/*  71 */       isSuccess = false;
/*  72 */       handleRequestError(provider, e);
/*     */     }
/*     */     finally
/*     */     {
/*  76 */       updateProviderConnectionState(provider, isSuccess);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static boolean handleRequestError(Provider provider, Exception e)
/*     */   {
/*  85 */     Properties provState = provider.getProviderState();
/*  86 */     provState.put("IsBadConnection", "1");
/*  87 */     IdcMessage msg = IdcMessageFactory.lc(e);
/*  88 */     provState.put("LastConnectionErrorMsg", LocaleUtils.encodeMessage(msg));
/*  89 */     provState.put("LastConnectionErrorCode", String.valueOf(-1));
/*     */ 
/*  91 */     boolean isServiceException = e instanceof ServiceException;
/*  92 */     boolean isReportError = !isServiceException;
/*  93 */     if (isServiceException)
/*     */     {
/*  95 */       ServiceException se = (ServiceException)e;
/*  96 */       provState.put("LastConnectionErrorCode", String.valueOf(se.m_errorCode));
/*     */ 
/*  98 */       isReportError = (se.m_errorCode > -2) || (se.m_errorCode < -6);
/*     */     }
/*     */ 
/* 102 */     if (isReportError)
/*     */     {
/* 109 */       reportError(e, provider, null);
/*     */     }
/*     */ 
/* 112 */     return isServiceException;
/*     */   }
/*     */ 
/*     */   public static void reportError(Exception e, Provider provider, String msg)
/*     */   {
/* 117 */     String errMsg = LocaleUtils.encodeMessage("csProviderError", null, provider.getName());
/*     */ 
/* 119 */     if (msg != null)
/*     */     {
/* 121 */       errMsg = LocaleUtils.appendMessage(msg, errMsg);
/*     */     }
/* 123 */     Report.error(null, errMsg, e);
/*     */   }
/*     */ 
/*     */   public static void checkReturnData(Provider provider, DataBinder binder)
/*     */     throws ServiceException
/*     */   {
/* 129 */     int statusCode = NumberUtils.parseInteger(binder.getLocal("StatusCode"), 0);
/* 130 */     if (statusCode >= 0)
/*     */       return;
/* 132 */     String errMsg = binder.getLocal("StatusMessage");
/* 133 */     if (errMsg == null)
/*     */     {
/* 135 */       errMsg = "!csUnableToPerformRequest";
/*     */     }
/* 137 */     throw new ServiceException(errMsg);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 143 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 71159 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.proxy.ProviderStateUtils
 * JD-Core Version:    0.5.4
 */