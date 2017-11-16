/*     */ package intradoc.server.script;
/*     */ 
/*     */ import intradoc.apputilities.installer.SysInstaller;
/*     */ import intradoc.common.EnvUtils;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NativeOsUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ScriptExtensionsAdaptor;
/*     */ import intradoc.common.ScriptInfo;
/*     */ import intradoc.common.ScriptUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.common.VersionInfo;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.server.Service;
/*     */ import intradoc.shared.UserData;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.io.IOException;
/*     */ import java.util.Properties;
/*     */ 
/*     */ public class ServerScriptExtensions extends ScriptExtensionsAdaptor
/*     */ {
/*     */   protected NativeOsUtils m_utils;
/*     */ 
/*     */   public ServerScriptExtensions()
/*     */   {
/*  40 */     this.m_variableTable = new String[0];
/*     */ 
/*  42 */     this.m_variableDefinitionTable = new int[0][];
/*     */ 
/*  44 */     this.m_functionTable = new String[] { "getRegistryValue", "isOlderVersion", "isAdminServerDir", "isMasterServerDir", "isServerDir", "normalizeOSPath" };
/*     */ 
/*  54 */     int STRING_VAL = 0;
/*     */ 
/*  63 */     this.m_functionDefinitionTable = new int[][] { { 0, 1, 0, -1, 0 }, { 1, -1, 0, 0, 1 }, { 2, 1, 0, -1, 1 }, { 3, 1, 0, -1, 1 }, { 4, 1, 0, -1, 1 }, { 5, 2, 0, 0, 0 } };
/*     */   }
/*     */ 
/*     */   public boolean evaluateValue(ScriptInfo info, boolean[] bVal, String[] sVal, ExecutionContext context, boolean isConditional)
/*     */   {
/*  79 */     DataBinder binder = ScriptExtensionUtils.getBinder(context);
/*  80 */     if (binder == null)
/*     */     {
/*  82 */       return false;
/*     */     }
/*     */ 
/*  85 */     int[] config = (int[])(int[])info.m_entry;
/*     */ 
/*  88 */     config[0];
/*     */ 
/*  93 */     return false;
/*     */   }
/*     */ 
/*     */   public boolean evaluateFunction(ScriptInfo info, Object[] args, ExecutionContext context)
/*     */     throws ServiceException
/*     */   {
/* 100 */     SysInstaller installer = (SysInstaller)context.getCachedObject("SysInstaller");
/*     */ 
/* 102 */     int[] config = (int[])(int[])info.m_entry;
/* 103 */     String function = info.m_key;
/*     */ 
/* 105 */     int returnType = config[4];
/* 106 */     int nargs = args.length - 1;
/* 107 */     int allowedParams = config[1];
/* 108 */     if ((allowedParams >= 0) && (allowedParams != nargs))
/*     */     {
/* 110 */       String msg = LocaleUtils.encodeMessage("csScriptEvalNotEnoughArgs", null, function, "" + allowedParams);
/*     */ 
/* 112 */       throw new IllegalArgumentException(msg);
/*     */     }
/*     */ 
/* 118 */     String sArg1 = null;
/* 119 */     String sArg2 = null;
/*     */ 
/* 122 */     if ((nargs > 0) && 
/* 124 */       (config[2] == 0))
/*     */     {
/* 126 */       sArg1 = ScriptUtils.getDisplayString(args[0], context);
/*     */     }
/*     */ 
/* 134 */     if ((nargs > 1) && 
/* 136 */       (config[3] == 0))
/*     */     {
/* 138 */       sArg2 = ScriptUtils.getDisplayString(args[1], context);
/*     */     }
/*     */ 
/* 147 */     boolean bResult = false;
/* 148 */     int iResult = 0;
/* 149 */     double dResult = 0.0D;
/* 150 */     Object oResult = null;
/*     */ 
/* 152 */     switch (config[0])
/*     */     {
/*     */     case 0:
/* 156 */       ScriptExtensionUtils.checkSecurityForIdocscript(context, "admin");
/*     */ 
/* 158 */       Service service = (Service)context;
/* 159 */       UserData data = (UserData)service.getCachedObject("UserData");
/* 160 */       if (data == null)
/*     */       {
/* 162 */         data = service.getUserData();
/* 163 */         if (data == null)
/*     */         {
/* 165 */           throw new ServiceException(null, new IdcMessage("csInsufficientPrivilege", new Object[0]));
/*     */         }
/*     */       }
/*     */ 
/* 169 */       oResult = "";
/*     */       try
/*     */       {
/* 172 */         if (this.m_utils == null)
/*     */         {
/* 174 */           if (installer != null)
/*     */           {
/* 176 */             this.m_utils = installer.m_utils;
/*     */           }
/*     */           else
/*     */           {
/* 180 */             this.m_utils = ((NativeOsUtils)context.getCachedObject("NativeOsUtils"));
/*     */           }
/* 182 */           if (this.m_utils == null)
/*     */           {
/*     */             try
/*     */             {
/* 186 */               this.m_utils = new NativeOsUtils();
/*     */             }
/*     */             catch (Throwable t)
/*     */             {
/* 190 */               Report.trace(null, "unable to use NativeOsUtils to implement getRegistryValue()", t);
/*     */             }
/*     */           }
/*     */         }
/*     */ 
/* 195 */         if ((this.m_utils != null) && (this.m_utils.isWindowsRegistrySupported()))
/*     */         {
/* 197 */           oResult = this.m_utils.getRegistryValue(sArg1);
/* 198 */           if (oResult == null)
/*     */           {
/* 200 */             oResult = "";
/*     */           }
/*     */         }
/*     */       }
/*     */       catch (Throwable t)
/*     */       {
/* 206 */         if (SystemUtils.m_verbose)
/*     */         {
/* 208 */           Report.debug(null, null, t);
/*     */         }
/*     */       }
/* 211 */       break;
/*     */     case 1:
/* 213 */       if ((installer != null) && (sArg2 == null))
/*     */       {
/* 215 */         bResult = installer.isOlderVersion(sArg1);
/*     */       }
/*     */       else
/*     */       {
/* 219 */         if (sArg2 == null)
/*     */         {
/* 221 */           sArg2 = VersionInfo.getProductVersionInfo();
/*     */         }
/* 223 */         bResult = SystemUtils.isOlderVersion(sArg1, sArg2);
/*     */       }
/* 225 */       break;
/*     */     case 2:
/* 228 */       DataBinder binder = ScriptExtensionUtils.getBinder(context);
/* 229 */       bResult = isAdminServerDir(sArg1, binder);
/* 230 */       break;
/*     */     case 3:
/* 234 */       DataBinder binder = ScriptExtensionUtils.getBinder(context);
/* 235 */       bResult = isMasterServerDir(sArg1, binder);
/* 236 */       break;
/*     */     case 4:
/* 240 */       DataBinder binder = ScriptExtensionUtils.getBinder(context);
/* 241 */       bResult = isServerDir(sArg1, binder);
/* 242 */       break;
/*     */     case 5:
/* 246 */       oResult = EnvUtils.normalizeOSPath(sArg1, sArg2);
/* 247 */       if (oResult == null)
/*     */       {
/* 249 */         oResult = ""; } break;
/*     */     default:
/* 254 */       return false;
/*     */     }
/*     */ 
/* 258 */     args[nargs] = ScriptExtensionUtils.computeReturnObject(returnType, bResult, iResult, dResult, oResult);
/*     */ 
/* 261 */     return true;
/*     */   }
/*     */ 
/*     */   public void checkNonEmpty(String val)
/*     */     throws IllegalArgumentException
/*     */   {
/* 267 */     if (val.length() != 0)
/*     */       return;
/* 269 */     String msg = LocaleUtils.encodeMessage("csPageMergerNoAttribute", null, val);
/*     */ 
/* 271 */     throw new IllegalArgumentException(msg);
/*     */   }
/*     */ 
/*     */   public boolean isServerDir(String dir, DataBinder binder)
/*     */   {
/* 278 */     String msg = LocaleUtils.encodeMessage("csNotServerDir", null, dir);
/*     */ 
/* 280 */     boolean rc = isGenericServerDir(dir, "config/config.cfg", null, null, msg, binder, null);
/*     */ 
/* 283 */     return rc;
/*     */   }
/*     */ 
/*     */   public boolean isAdminServerDir(String dir, DataBinder binder)
/*     */   {
/* 288 */     String msg = LocaleUtils.encodeMessage("csNotAdminServerDir", null, dir);
/*     */ 
/* 290 */     boolean rc = isGenericServerDir(dir, "config/config.cfg", "IDC_Admin_Name", null, msg, binder, null);
/*     */ 
/* 293 */     return rc;
/*     */   }
/*     */ 
/*     */   public boolean isMasterServerDir(String dir, DataBinder binder)
/*     */   {
/* 298 */     String msg = LocaleUtils.encodeMessage("csNotMasterServerDir", null, dir);
/*     */ 
/* 300 */     Properties props = new Properties();
/* 301 */     boolean rc = isGenericServerDir(dir, "config/config.cfg", "IDC_Name", null, msg, binder, props);
/*     */ 
/* 304 */     if (StringUtils.convertToBool(props.getProperty("IsProxiedServer"), false))
/*     */     {
/* 306 */       binder.putLocal("isGenericServerDirError", msg);
/* 307 */       rc = false;
/*     */     }
/* 309 */     return rc;
/*     */   }
/*     */ 
/*     */   public boolean isGenericServerDir(String dir, String configFile, String requiredKey, String requiredValue, String errMsg, DataBinder binder, Properties props)
/*     */   {
/* 316 */     String msg = null;
/* 317 */     String fileName = FileUtils.fileSlashes(dir + "/" + configFile);
/*     */     try
/*     */     {
/* 320 */       if (props == null)
/*     */       {
/* 322 */         props = new Properties();
/*     */       }
/* 324 */       FileUtils.loadProperties(props, fileName);
/* 325 */       if (requiredKey != null)
/*     */       {
/* 327 */         String value = props.getProperty(requiredKey);
/* 328 */         if (value != null)
/*     */         {
/* 330 */           if (requiredValue != null)
/*     */           {
/* 332 */             if (!value.equalsIgnoreCase(requiredValue))
/*     */             {
/* 334 */               msg = errMsg;
/*     */             }
/*     */           }
/*     */           else
/*     */           {
/* 339 */             if (binder != null)
/*     */             {
/* 341 */               binder.putLocal(requiredKey, value);
/*     */             }
/* 343 */             Report.trace("install", "isGenericServerDir(): " + requiredKey + "=" + value, null);
/*     */           }
/*     */ 
/*     */         }
/*     */         else
/*     */         {
/* 349 */           msg = errMsg;
/*     */         }
/*     */       }
/*     */     }
/*     */     catch (IOException ignore)
/*     */     {
/* 355 */       msg = LocaleUtils.encodeMessage("syUnableToReadFile", ignore.getMessage(), fileName);
/*     */     }
/*     */ 
/* 358 */     if (msg != null)
/*     */     {
/* 360 */       if (binder != null)
/*     */       {
/* 362 */         binder.putLocal("isGenericServerDirError", msg);
/*     */       }
/* 364 */       return false;
/*     */     }
/* 366 */     if (binder != null)
/*     */     {
/* 368 */       binder.putLocal("isGenericServerDirError", "");
/*     */     }
/* 370 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 375 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 87410 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.script.ServerScriptExtensions
 * JD-Core Version:    0.5.4
 */