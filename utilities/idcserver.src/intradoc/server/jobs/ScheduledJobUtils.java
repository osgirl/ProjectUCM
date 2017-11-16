/*     */ package intradoc.server.jobs;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.Validation;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.server.Service;
/*     */ import intradoc.server.ServiceManager;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.UserData;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.util.Date;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class ScheduledJobUtils
/*     */ {
/*     */   static final int F_THROW_ERROR_IF_ALREADY_DEFINED = 1;
/*     */ 
/*     */   public static String getConfigValue(String key, DataBinder binder, String defaultValue, boolean useEnvironment)
/*     */   {
/*  36 */     String value = null;
/*  37 */     if (binder != null)
/*     */     {
/*  39 */       value = binder.getLocal(key);
/*     */     }
/*  41 */     if ((value == null) && (useEnvironment))
/*     */     {
/*  43 */       value = SharedObjects.getEnvironmentValue(key);
/*     */     }
/*  45 */     if (value == null)
/*     */     {
/*  47 */       return defaultValue;
/*     */     }
/*  49 */     return value;
/*     */   }
/*     */ 
/*     */   public static boolean getConfigBoolean(String key, DataBinder binder, boolean defValue, boolean useEnvironment)
/*     */   {
/*  55 */     String str = getConfigValue(key, binder, null, useEnvironment);
/*  56 */     return StringUtils.convertToBool(str, defValue);
/*     */   }
/*     */ 
/*     */   public static int getConfigInteger(String key, DataBinder binder, int defValue, boolean useEnvironment)
/*     */   {
/*  62 */     String str = getConfigValue(key, binder, null, useEnvironment);
/*  63 */     return NumberUtils.parseInteger(str, defValue);
/*     */   }
/*     */ 
/*     */   public static String checkOrCreateID(String key, int size, DataBinder workBinder, Workspace ws, int flags)
/*     */     throws ServiceException, DataException
/*     */   {
/*  69 */     String name = workBinder.getLocal("dSjName");
/*  70 */     if ((name != null) && (name.length() > 0))
/*     */     {
/*  73 */       IdcMessage errMsg = Validation.checkUrlFileSegmentForDB(name, "csSjInvalidName", 50, null);
/*  74 */       if (errMsg != null)
/*     */       {
/*  76 */         throw new ServiceException(null, errMsg);
/*     */       }
/*     */ 
/*  80 */       ResultSet rset = ws.createResultSet("QscheduledJob", workBinder);
/*  81 */       if (rset.isRowPresent())
/*     */       {
/*  83 */         if ((flags & 0x1) != 0)
/*     */         {
/*  85 */           throw new ServiceException(null, -17, "csSjNameExists", new Object[] { name });
/*     */         }
/*  87 */         workBinder.putLocal("sjNameExists", "1");
/*     */       }
/*  89 */       return name.toLowerCase();
/*     */     }
/*     */ 
/*  93 */     name = StringUtils.createGUIDEx(15, 0, null).toLowerCase();
/*  94 */     workBinder.putLocal(key, name);
/*  95 */     return name;
/*     */   }
/*     */ 
/*     */   public static String getIdFromBinder(DataBinder binder) throws DataException
/*     */   {
/* 100 */     String name = binder.get("dSjName");
/* 101 */     return name.toLowerCase();
/*     */   }
/*     */ 
/*     */   public static String getId(Map map)
/*     */   {
/* 106 */     String name = (String)map.get("dSjName");
/* 107 */     if (name == null)
/*     */     {
/* 109 */       Report.trace("system", "Look up key dSjName for ScheduledJobUtils did not find anything, likely data store design upper column map issue.", new Throwable());
/*     */ 
/* 111 */       return null;
/*     */     }
/* 113 */     return name.toLowerCase();
/*     */   }
/*     */ 
/*     */   public static String getHistoryIdFromBinder(DataBinder binder) throws DataException
/*     */   {
/* 118 */     String guid = binder.get("dSjHistoryGUID");
/* 119 */     return guid.toLowerCase();
/*     */   }
/*     */ 
/*     */   public static boolean isActiveJob(String state)
/*     */   {
/* 124 */     boolean result = (state.equals("A")) || (state.equals("I"));
/* 125 */     return result;
/*     */   }
/*     */ 
/*     */   public static Service initService(String cmd, DataBinder binder, Workspace ws, JobState jState, ScheduledJobImplementor jobImp)
/*     */     throws ServiceException, DataException
/*     */   {
/* 131 */     String user = binder.getLocal("dSjInitUser");
/* 132 */     binder.putLocal("IdcService", cmd);
/* 133 */     binder.setEnvironmentValue("REMOTE_USER", user);
/*     */ 
/* 135 */     Service service = ServiceManager.getInitializedService(cmd, binder, ws);
/*     */ 
/* 137 */     service.setCachedObject("ScheduledJobState", jState);
/* 138 */     service.setCachedObject("ScheduledJobImplementor", jobImp);
/* 139 */     return service;
/*     */   }
/*     */ 
/*     */   public static void addJobHistoryEvent(DataBinder binder, Workspace ws, ExecutionContext cxt)
/*     */   {
/* 152 */     String guid = StringUtils.createGUIDEx(15, 0, null).toLowerCase();
/* 153 */     binder.putLocal("dSjHistoryGUID", guid);
/*     */ 
/* 156 */     String val = binder.getLocal("dSjExceptionJob");
/* 157 */     if (val == null)
/*     */     {
/* 159 */       binder.putLocal("dSjExceptionJob", "");
/*     */     }
/*     */ 
/* 162 */     val = binder.getLocal("dSjLastProcessedTs");
/* 163 */     if ((val == null) || (val.length() == 0))
/*     */     {
/* 165 */       Date dte = new Date();
/* 166 */       String dteStr = LocaleUtils.formatODBC(dte);
/* 167 */       binder.putLocal("dSjLastProcessedTs", dteStr);
/*     */     }
/*     */ 
/* 170 */     val = binder.getLocal("dSjEndUser");
/* 171 */     if (val == null)
/*     */     {
/* 173 */       if (cxt instanceof Service)
/*     */       {
/* 175 */         Service service = (Service)cxt;
/* 176 */         UserData userData = service.getUserData();
/* 177 */         val = userData.m_name;
/*     */       }
/*     */       else
/*     */       {
/* 181 */         val = binder.getLocal("dSjInitUser");
/*     */       }
/* 183 */       if (val == null)
/*     */       {
/* 185 */         val = "guest";
/*     */       }
/* 187 */       binder.putLocal("dSjEndUser", val);
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 193 */       ws.execute("IscheduledJobHistory", binder);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 197 */       Report.error("scheduledjobs", e, "csSjUnableToUpdateHistory2", new Object[] { binder.getLocal("dSjName") });
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 204 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 82268 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.jobs.ScheduledJobUtils
 * JD-Core Version:    0.5.4
 */