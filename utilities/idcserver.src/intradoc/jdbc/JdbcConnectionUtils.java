/*     */ package intradoc.jdbc;
/*     */ 
/*     */ import intradoc.common.ClassHelperUtils;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.conversion.CryptoPasswordUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.provider.ProviderConnectionManager;
/*     */ import java.security.Provider;
/*     */ import java.security.Security;
/*     */ import java.sql.Connection;
/*     */ import java.sql.DriverManager;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Properties;
/*     */ 
/*     */ public class JdbcConnectionUtils
/*     */ {
/*  34 */   public static boolean m_allowJdbcConRetry = false;
/*  35 */   public static boolean m_jdbcRetryCounter = false;
/*  36 */   public static int m_retryMax = 5;
/*     */ 
/*     */   public static Connection getConnection(ProviderConnectionManager manager, DataBinder data)
/*     */     throws DataException
/*     */   {
/*  41 */     Connection conn = null;
/*  42 */     int retryCounter = 0;
/*     */ 
/*  45 */     String url = data.get("JdbcConnectionString");
/*  46 */     String user = null;
/*  47 */     String passwrd = null;
/*  48 */     if (isJdbcUserPasswordRequired(url))
/*     */     {
/*  50 */       user = data.get("JdbcUser");
/*  51 */       passwrd = CryptoPasswordUtils.determinePassword("JdbcPassword", data, false);
/*     */     }
/*     */ 
/*  54 */     while (conn == null)
/*     */     {
/*     */       try
/*     */       {
/*  58 */         Properties props = new Properties();
/*  59 */         if (user != null)
/*     */         {
/*  61 */           props.setProperty("user", user);
/*  62 */           props.setProperty("password", passwrd);
/*     */         }
/*     */ 
/*  65 */         String extraPropsString = data.getActiveAllowMissing("JdbcConnExtraProps");
/*  66 */         List list = StringUtils.appendListFromSequenceSimple(null, extraPropsString);
/*     */ 
/*  68 */         boolean isSSLConnection = false;
/*  69 */         int size = list.size();
/*  70 */         for (int i = 0; i < size; ++i)
/*     */         {
/*  72 */           String key = (String)list.get(i);
/*  73 */           if (key == null)
/*     */             continue;
/*  75 */           String value = data.getAllowMissing(key);
/*  76 */           if (value == null)
/*     */             continue;
/*  78 */           props.setProperty(key, value);
/*  79 */           if ((!key.startsWith("javax.net.ssl")) && (!key.startsWith("oracle.net.ssl")))
/*     */             continue;
/*  81 */           isSSLConnection = true;
/*     */         }
/*     */ 
/*  89 */         if ((isSSLConnection == true) && (url.startsWith("jdbc:oracle")))
/*     */         {
/*     */           try
/*     */           {
/*  93 */             Class securityProviderClass = ClassHelperUtils.createClass("oracle.security.pki.OraclePKIProvider");
/*  94 */             Provider securityProviderInstance = (Provider)ClassHelperUtils.createInstance(securityProviderClass);
/*  95 */             Security.insertProviderAt(securityProviderInstance, 3);
/*     */           }
/*     */           catch (Exception e)
/*     */           {
/*  99 */             String sslInitFailedMsg = LocaleResources.localizeMessage(LocaleUtils.encodeMessage("csJdbcOracleSSLInitFailed", null, null), null);
/*     */ 
/* 101 */             Report.error("systemdatabase", sslInitFailedMsg, e);
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/* 107 */         conn = DriverManager.getConnection(url, props);
/* 108 */         if (m_allowJdbcConRetry)
/*     */         {
/* 110 */           m_allowJdbcConRetry = false;
/* 111 */           if ((retryCounter > 0) && 
/* 113 */             (manager != null))
/*     */           {
/* 115 */             manager.debugMsg(LocaleResources.localizeMessage("!csJdbcConnectionMade", null));
/*     */           }
/*     */         }
/*     */ 
/*     */       }
/*     */       catch (Throwable e)
/*     */       {
/* 122 */         if ((!m_allowJdbcConRetry) || (++retryCounter > m_retryMax))
/*     */         {
/* 126 */           throw new DataException(e, "csJdbcUnableToCreateConnection", new Object[] { data.getLocal("ProviderName"), url });
/*     */         }
/*     */       }
/* 128 */       int waitTime = 1 << retryCounter - 1;
/* 129 */       if (manager != null)
/*     */       {
/* 131 */         manager.debugMsg(LocaleResources.localizeMessage(LocaleUtils.encodeMessage("csJdbcWaitBeforeRetry", null, "" + waitTime), null));
/*     */       }
/*     */ 
/* 135 */       SystemUtils.sleep(waitTime * 1000);
/* 136 */       if (manager != null)
/*     */       {
/* 138 */         manager.debugMsg(LocaleResources.localizeMessage("!csJdbcStartRetryConnection", null));
/*     */       }
/*     */     }
/*     */ 
/* 142 */     return conn;
/*     */   }
/*     */ 
/*     */   public static boolean isJdbcUserPasswordRequired(String url)
/*     */   {
/* 151 */     if ((url == null) || (url.length() == 0))
/*     */     {
/* 153 */       return true;
/*     */     }
/* 155 */     url = url.toLowerCase();
/*     */ 
/* 158 */     return (!url.startsWith("jdbc:jtds")) || (url.indexOf(":domain") <= 0);
/*     */   }
/*     */ 
/*     */   public static void removePersistObject(JdbcConnection jCon, Object removeObj)
/*     */     throws DataException
/*     */   {
/* 172 */     for (Iterator i$ = jCon.m_persistObjectList.iterator(); i$.hasNext(); ) { Object obj = i$.next();
/*     */ 
/* 174 */       if ((obj instanceof JdbcResultSet) && 
/* 176 */         (obj == removeObj))
/*     */       {
/* 178 */         JdbcResultSet rset = (JdbcResultSet)obj;
/* 179 */         rset.closeInternals();
/* 180 */         return;
/*     */       } }
/*     */ 
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 188 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97027 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.jdbc.JdbcConnectionUtils
 * JD-Core Version:    0.5.4
 */