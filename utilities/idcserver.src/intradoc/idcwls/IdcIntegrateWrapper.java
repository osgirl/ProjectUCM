/*     */ package intradoc.idcwls;
/*     */ 
/*     */ import intradoc.common.ClassHelperUtils;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.provider.Providers;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcException;
/*     */ import intradoc.util.IdcInvokeInterface;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.OutputStream;
/*     */ import java.io.PrintStream;
/*     */ import java.util.HashMap;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class IdcIntegrateWrapper
/*     */   implements IdcInvokeInterface
/*     */ {
/*  53 */   public Map m_integrateAttributes = new HashMap();
/*     */ 
/*  63 */   public boolean m_toggleThreadContext = false;
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/*  47 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 104594 $";
/*     */   }
/*     */ 
/*     */   public void setIntegrateAttribute(String key, Object val)
/*     */   {
/*  72 */     this.m_integrateAttributes.put(key, val);
/*     */   }
/*     */ 
/*     */   public Object getIntegrateAttribute(String key)
/*     */   {
/*  77 */     return this.m_integrateAttributes.get(key);
/*     */   }
/*     */ 
/*     */   public void initializeServer(Object idcServletConfig)
/*     */     throws IOException
/*     */   {
/*  88 */     ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
/*     */ 
/*  90 */     IdcServletConfigWrapper configWrapper = new IdcServletConfigWrapper(idcServletConfig);
/*  91 */     Map stateProps = (Map)configWrapper.getAttribute("ServerProps");
/*  92 */     if (stateProps != null)
/*     */     {
/*  94 */       String toggleThreadContext = (String)stateProps.get("ToggleThreadContext");
/*  95 */       this.m_toggleThreadContext = StringUtils.convertToBool(toggleThreadContext, false);
/*     */     }
/*  97 */     if (this.m_toggleThreadContext)
/*     */     {
/*  99 */       Thread.currentThread().setContextClassLoader(super.getClass().getClassLoader());
/*     */     }
/*     */     try
/*     */     {
/* 103 */       IdcServletRequestUtils.initializeServer(configWrapper);
/* 104 */       if (SharedObjects.isInit())
/*     */       {
/* 106 */         String instanceName = SharedObjects.getEnvironmentValue("IDC_Name");
/* 107 */         if (instanceName != null)
/*     */         {
/* 109 */           this.m_integrateAttributes.put("IDC_Name", instanceName);
/*     */         }
/*     */       }
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/* 115 */       String localizedMsg = "Servlet failed to full initialize";
/* 116 */       if (SharedObjects.isInit())
/*     */       {
/* 119 */         String fatalErrorKey = "csServletFailedToFullyInitialize";
/*     */         try
/*     */         {
/* 122 */           Report.fatal("servlet", t, fatalErrorKey, new Object[0]);
/* 123 */           String reportMsg = LocaleUtils.encodeMessage(fatalErrorKey, null, null);
/* 124 */           localizedMsg = LocaleResources.localizeMessage(reportMsg, null);
/*     */ 
/* 128 */           SystemUtils.markServerAsStopped();
/*     */         }
/*     */         catch (Throwable reportError)
/*     */         {
/* 134 */           reportErrorDirect(t);
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 139 */       IOException initError = new IOException(localizedMsg);
/*     */ 
/* 141 */       throw initError;
/*     */     }
/*     */     finally
/*     */     {
/* 145 */       Thread.currentThread().setContextClassLoader(oldLoader);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void reportErrorDirect(Throwable t)
/*     */   {
/*     */     try
/*     */     {
/* 157 */       System.err.println(t.getMessage());
/* 158 */       t.printStackTrace(System.err);
/*     */     }
/*     */     catch (Throwable ignore)
/*     */     {
/*     */     }
/*     */   }
/*     */ 
/*     */   public void stopServer(Object idcServletConfig)
/*     */     throws IOException
/*     */   {
/* 168 */     ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
/*     */     try
/*     */     {
/* 171 */       if (this.m_toggleThreadContext)
/*     */       {
/* 173 */         Thread.currentThread().setContextClassLoader(super.getClass().getClassLoader());
/*     */       }
/* 175 */       IdcServletConfigWrapper configWrapper = new IdcServletConfigWrapper(idcServletConfig);
/* 176 */       IdcServletRequestUtils.stopServer(configWrapper);
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/* 181 */       String fatalErrorKey = "csServletFailedToStopServer";
/* 182 */       String reportMsg = LocaleUtils.encodeMessage(fatalErrorKey, null);
/* 183 */       String localizedMsg = reportMsg;
/* 184 */       if (LocaleResources.getStringInternal(fatalErrorKey) != null)
/*     */       {
/* 187 */         localizedMsg = LocaleResources.localizeMessage(reportMsg, null);
/* 188 */         Report.fatal("servlet", t, fatalErrorKey, new Object[0]);
/*     */       }
/*     */       IOException stopError;
/* 193 */       throw stopError;
/*     */     }
/*     */     finally
/*     */     {
/* 197 */       SystemUtils.cleanUpAfterStop();
/* 198 */       Thread.currentThread().setContextClassLoader(oldLoader);
/*     */     }
/*     */   }
/*     */ 
/*     */   public IdcServletRequestContext getRequestWrapper(Object request)
/*     */   {
/* 204 */     IdcServletRequestContextWrapper idcRequest = new IdcServletRequestContextWrapper(request);
/* 205 */     IdcServletRequestUtils.loadActiveData(idcRequest);
/* 206 */     return idcRequest;
/*     */   }
/*     */ 
/*     */   public void processFilterEvent(Object request)
/*     */     throws IOException
/*     */   {
/* 213 */     ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
/*     */     try
/*     */     {
/* 216 */       if (this.m_toggleThreadContext)
/*     */       {
/* 218 */         Thread.currentThread().setContextClassLoader(super.getClass().getClassLoader());
/*     */       }
/* 220 */       SystemUtils.assignReportingThreadIdToCurrentThread(0);
/*     */ 
/* 222 */       IdcServletRequestContext requestWrapper = getRequestWrapper(request);
/* 223 */       IdcServletRequestUtils.processFilterEvent(requestWrapper);
/* 224 */       IdcServletRequestUtils.propagateUpdates(requestWrapper);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 229 */       boolean enableDoStackTraceOverride = SharedObjects.getEnvValueAsBoolean("EnableDoStackTraceOverride", false);
/* 230 */       if (enableDoStackTraceOverride)
/*     */       {
/* 232 */         throw new IOException(e);
/*     */       }
/*     */       String errorString;
/*     */       IOException ioException;
/* 239 */       throw ioException;
/*     */     }
/*     */     finally
/*     */     {
/* 244 */       Providers.releaseConnections();
/* 245 */       SystemUtils.releaseReportingThreadIdForCurrentThread();
/* 246 */       Thread.currentThread().setContextClassLoader(oldLoader);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void processAuthEvent(Object request) throws IOException
/*     */   {
/* 252 */     ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
/*     */     try
/*     */     {
/* 255 */       if (this.m_toggleThreadContext)
/*     */       {
/* 257 */         Thread.currentThread().setContextClassLoader(super.getClass().getClassLoader());
/*     */       }
/*     */ 
/* 260 */       IdcServletRequestContext requestWrapper = getRequestWrapper(request);
/* 261 */       IdcServletRequestUtils.processAuthEvent(requestWrapper);
/* 262 */       IdcServletRequestUtils.propagateUpdates(requestWrapper);
/*     */     }
/*     */     finally
/*     */     {
/* 266 */       Thread.currentThread().setContextClassLoader(oldLoader);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void executeServerRequest(Map<String, String> env, Map params, InputStream inStream, OutputStream outStream, Map args, Map results)
/*     */     throws IOException
/*     */   {
/* 276 */     executeRequest(env, params, inStream, outStream, args, results);
/*     */   }
/*     */ 
/*     */   public static void executeRequest(Map<String, String> env, Map params, InputStream inStream, OutputStream outStream, Map args, Map results)
/*     */     throws IOException
/*     */   {
/* 282 */     IdcServletRequestUtils.executeRequest(env, params, inStream, outStream, args, results);
/*     */   }
/*     */ 
/*     */   public Object calculateMethodToken(String methodName)
/*     */     throws IOException
/*     */   {
/* 295 */     return methodName;
/*     */   }
/*     */ 
/*     */   public Object invokeMethod(Object methodToken, Object[] args)
/*     */     throws IOException, IdcException
/*     */   {
/* 303 */     String methodName = (String)methodToken;
/* 304 */     Object result = null;
/*     */     try
/*     */     {
/* 307 */       result = ClassHelperUtils.executeMethod(this, methodName, args, null);
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/* 311 */       ClassHelperUtils.convertAndThrowUsableException(t, methodName);
/*     */     }
/* 313 */     return result;
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.idcwls.IdcIntegrateWrapper
 * JD-Core Version:    0.5.4
 */