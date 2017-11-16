/*     */ package intradoc.server.proxy;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class ProviderValidation
/*     */ {
/*  32 */   static String[][] PROXY_COLUMNS = { { "IDC_Name", "1" }, { "HttpRelativeWebRoot", "1" }, { "IntradocServerHostName", "1" }, { "IntradocServerPort", "1" }, { "ExportedRolesMap", "0" }, { "AllowedExportedAccounts", "0" } };
/*     */ 
/*     */   public static Vector getStandardFields(String type)
/*     */   {
/*  45 */     String[][] stdFields = { { "ProviderType", "text" }, { "ProviderClass", "text" }, { "ProviderConnection", "text" }, { "ProviderConfig", "text" } };
/*     */ 
/*  53 */     String[][] typeFields = (String[][])null;
/*  54 */     if (type.equals("outgoing"))
/*     */     {
/*  56 */       typeFields = new String[][] { { "ServerPort", "text" }, { "IntradocServerPort", "int" }, { "IntradocServerHostName", "text" }, { "HttpServerAddress", "text" }, { "HttpRelativeWebRoot", "text" }, { "IsProxiedServer", "bool" }, { "IsSearchable", "bool" }, { "SearchRequiredRoles", "text" }, { "SearchAccountFilter", "text" }, { "IsNotifyTarget", "bool" }, { "IDC_Name", "text" }, { "NotifySubjects", "text" }, { "IsRefinery", "bool" }, { "RefineryReadOnly", "bool" }, { "RefineryMaxJobs", "int" } };
/*     */     }
/*  75 */     else if (type.equals("incoming"))
/*     */     {
/*  77 */       typeFields = new String[][] { { "ServerPort", "int" } };
/*     */     }
/*  82 */     else if (type.equals("database"))
/*     */     {
/*  84 */       typeFields = new String[][] { { "IsJdbc", "bool" }, { "DatabaseType", "text" }, { "DatabaseDir", "text" }, { "JdbcDriver", "text" }, { "JdbcConnectionString", "text" }, { "JdbcUser", "text" }, { "JdbcPassword", "text" }, { "NumConnections", "int" }, { "TestQuery", "text" }, { "ExtraStorageKeys", "text" }, { "UseDataSource", "text" }, { "DataSource", "text" } };
/*     */     }
/*     */ 
/* 101 */     Vector fields = new IdcVector();
/* 102 */     int num = stdFields.length;
/* 103 */     for (int i = 0; i < num; ++i)
/*     */     {
/* 105 */       fields.addElement(stdFields[i]);
/*     */     }
/* 107 */     if (typeFields != null)
/*     */     {
/* 109 */       num = typeFields.length;
/* 110 */       for (int i = 0; i < num; ++i)
/*     */       {
/* 112 */         fields.addElement(typeFields[i]);
/*     */       }
/*     */     }
/*     */ 
/* 116 */     return fields;
/*     */   }
/*     */ 
/*     */   public static void validateDefaults(String type, DataBinder providerData)
/*     */     throws DataException, ServiceException
/*     */   {
/* 122 */     String[][] keys = (String[][])null;
/* 123 */     if (type.equals("outgoing"))
/*     */     {
/* 125 */       keys = new String[][] { { "IntradocServerPort", "ServerPort", "Server Port" } };
/*     */ 
/* 130 */       validateOutgoing(providerData);
/*     */     }
/* 132 */     else if (!type.equals("database"))
/*     */     {
/* 136 */       if (type.equals("incoming"))
/*     */       {
/* 138 */         keys = new String[][] { { "ServerPort", null, "Server Port" } };
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 144 */     if (keys == null)
/*     */     {
/* 146 */       keys = new String[0][0];
/*     */     }
/* 148 */     int num = keys.length;
/* 149 */     for (int i = 0; i < num; ++i)
/*     */     {
/* 151 */       String key = keys[i][0];
/* 152 */       String key1 = keys[i][1];
/*     */ 
/* 154 */       String value = providerData.getLocal(key);
/* 155 */       String value1 = null;
/* 156 */       if (key1 != null)
/*     */       {
/* 158 */         value1 = providerData.getLocal(key1);
/*     */       }
/* 160 */       if ((value == null) && (value1 == null))
/*     */       {
/* 162 */         String msg = LocaleUtils.encodeMessage("csRequiredFieldMissing2", null, key);
/*     */ 
/* 164 */         throw new DataException(msg);
/*     */       }
/* 166 */       if (value != null)
/*     */       {
/* 168 */         if (key1 == null)
/*     */           continue;
/* 170 */         providerData.putLocal(key1, value);
/*     */       }
/*     */       else
/*     */       {
/* 175 */         providerData.putLocal(key, value1);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void validateOutgoing(DataBinder providerData)
/*     */     throws ServiceException
/*     */   {
/* 183 */     String str = providerData.getLocal("IntradocServerHostName");
/* 184 */     if (str == null)
/*     */     {
/* 186 */       str = providerData.getLocal("HttpServerAddress");
/* 187 */       if (str == null)
/*     */       {
/* 189 */         throw new ServiceException(-26, "!csProviderServerHostNameNotDefined");
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 194 */     String relWebRoot = providerData.getLocal("HttpRelativeWebRoot");
/* 195 */     relWebRoot = FileUtils.directorySlashes(relWebRoot);
/* 196 */     if ((relWebRoot.length() > 0) && (relWebRoot.charAt(0) != '/'))
/*     */     {
/* 198 */       relWebRoot = "/" + relWebRoot;
/*     */     }
/* 200 */     if (relWebRoot.length() <= 1)
/*     */     {
/* 202 */       throw new ServiceException(-26, "!csProviderHttpRelativeWebRootInvalid");
/*     */     }
/*     */ 
/* 205 */     providerData.putLocal("HttpRelativeWebRoot", relWebRoot);
/*     */ 
/* 208 */     boolean isProxied = StringUtils.convertToBool(providerData.getLocal("IsProxiedServer"), false);
/* 209 */     boolean isTarget = StringUtils.convertToBool(providerData.getLocal("IsNotifyTarget"), false);
/* 210 */     if ((!isProxied) && (!isTarget))
/*     */       return;
/* 212 */     String[][] columnInfo = PROXY_COLUMNS;
/* 213 */     int num = columnInfo.length;
/* 214 */     for (int i = 0; i < num; ++i)
/*     */     {
/* 216 */       String key = columnInfo[i][0];
/* 217 */       str = providerData.getLocal(key);
/* 218 */       boolean isRequired = StringUtils.convertToBool(columnInfo[i][1], false);
/*     */ 
/* 220 */       if ((!isRequired) || ((str != null) && (str.length() != 0)))
/*     */         continue;
/* 222 */       String msg = LocaleUtils.encodeMessage("csRequiredConfigFieldMissing", null, columnInfo[i][0]);
/*     */ 
/* 224 */       throw new ServiceException(-26, msg);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 232 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 69633 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.proxy.ProviderValidation
 * JD-Core Version:    0.5.4
 */