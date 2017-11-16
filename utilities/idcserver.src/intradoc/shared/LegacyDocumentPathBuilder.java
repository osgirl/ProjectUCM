/*     */ package intradoc.shared;
/*     */ 
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.Parameters;
/*     */ 
/*     */ public class LegacyDocumentPathBuilder
/*     */ {
/*  31 */   public static boolean m_allLowerCase = true;
/*     */ 
/*     */   public static String getRelativeWebRoot()
/*     */   {
/*  39 */     String str = SharedObjects.getEnvironmentValue("HttpRelativeWebRoot");
/*  40 */     if (str == null)
/*     */     {
/*  42 */       return str = "/";
/*     */     }
/*  44 */     return str;
/*     */   }
/*     */ 
/*     */   public static String getAbsoluteWebRoot()
/*     */   {
/*  54 */     String str = SharedObjects.getEnvironmentValue("HttpAbsoluteWebRoot");
/*  55 */     if (str == null)
/*     */     {
/*  57 */       String domain = SharedObjects.getEnvironmentValue("HttpServerAddress");
/*  58 */       String relRoot = getRelativeWebRoot();
/*  59 */       if (domain == null)
/*     */       {
/*  61 */         return relRoot;
/*     */       }
/*  63 */       boolean isSSL = StringUtils.convertToBool(SharedObjects.getEnvironmentValue("UseSSL"), false);
/*     */ 
/*  65 */       String httpPrefix = (isSSL) ? "https://" : "http://";
/*  66 */       str = httpPrefix + domain + relRoot;
/*     */     }
/*  68 */     return str;
/*     */   }
/*     */ 
/*     */   public static String getBaseAbsoluteRoot()
/*     */   {
/*  78 */     String str = SharedObjects.getEnvironmentValue("HttpAbsoluteWebRoot");
/*  79 */     if (str == null)
/*     */     {
/*  81 */       String domain = SharedObjects.getEnvironmentValue("HttpServerAddress");
/*  82 */       if (domain == null)
/*     */       {
/*  84 */         domain = "localhost";
/*     */       }
/*  86 */       boolean isSSL = StringUtils.convertToBool(SharedObjects.getEnvironmentValue("UseSSL"), false);
/*     */ 
/*  88 */       String httpPrefix = (isSSL) ? "https://" : "http://";
/*  89 */       str = httpPrefix + domain;
/*     */     }
/*  91 */     return str;
/*     */   }
/*     */ 
/*     */   public static String computeWebDirSecuritySuffix(DataBinder docInfo, boolean isActive)
/*     */     throws DataException
/*     */   {
/* 100 */     String docAccount = docInfo.get("dDocAccount", isActive);
/* 101 */     String webDirSuffix = "";
/* 102 */     if ((docAccount != null) && (docAccount.trim().length() > 0))
/*     */     {
/* 104 */       webDirSuffix = "@" + convertToAccountDirStr(docAccount) + "/";
/*     */     }
/* 106 */     return webDirSuffix;
/*     */   }
/*     */ 
/*     */   public static String computeWebDirSecuritySuffix(Parameters docInfo)
/*     */     throws DataException
/*     */   {
/* 112 */     String docAccount = docInfo.get("dDocAccount");
/* 113 */     String webDirSuffix = "";
/* 114 */     if ((docAccount != null) && (docAccount.trim().length() > 0))
/*     */     {
/* 116 */       webDirSuffix = "@" + convertToAccountDirStr(docAccount) + "/";
/*     */     }
/* 118 */     return webDirSuffix;
/*     */   }
/*     */ 
/*     */   public static String computeWebDirSuffix(DataBinder docInfo, boolean isActive)
/*     */     throws DataException
/*     */   {
/* 129 */     String webDirSuffix = computeWebDirSecuritySuffix(docInfo, isActive);
/* 130 */     webDirSuffix = webDirSuffix + "documents/" + docInfo.get("dDocType", isActive).toLowerCase() + "/";
/*     */ 
/* 132 */     return webDirSuffix;
/*     */   }
/*     */ 
/*     */   public static String computeWebDirSuffix(Parameters docInfo) throws DataException
/*     */   {
/* 137 */     String webDirSuffix = computeWebDirSecuritySuffix(docInfo);
/* 138 */     webDirSuffix = webDirSuffix + "documents/" + docInfo.get("dDocType").toLowerCase() + "/";
/*     */ 
/* 140 */     return webDirSuffix;
/*     */   }
/*     */ 
/*     */   public static String computeWebDirPartialPath(DataBinder docInfo, boolean isActive)
/*     */     throws DataException
/*     */   {
/* 149 */     String securityGroup = docInfo.get("dSecurityGroup", isActive);
/* 150 */     String wPartialPath = "groups/" + securityGroup.toLowerCase() + "/";
/* 151 */     wPartialPath = wPartialPath + computeWebDirSuffix(docInfo, isActive);
/* 152 */     return wPartialPath;
/*     */   }
/*     */ 
/*     */   public static String computeWebDirPartialPath(Parameters docInfo) throws DataException
/*     */   {
/* 157 */     String securityGroup = docInfo.get("dSecurityGroup");
/* 158 */     String wPartialPath = "groups/" + securityGroup.toLowerCase() + "/";
/* 159 */     wPartialPath = wPartialPath + computeWebDirSuffix(docInfo);
/* 160 */     return wPartialPath;
/*     */   }
/*     */ 
/*     */   public static String computeWebUrlDir(DataBinder docInfo, boolean isAbsolute, boolean isActive)
/*     */     throws DataException
/*     */   {
/*     */     String root;
/*     */     String root;
/* 170 */     if (isAbsolute)
/*     */     {
/* 172 */       root = getAbsoluteWebRoot();
/*     */     }
/*     */     else
/*     */     {
/* 176 */       root = getRelativeWebRoot();
/*     */     }
/* 178 */     String wDir = root + computeWebDirPartialPath(docInfo, isActive);
/* 179 */     return wDir;
/*     */   }
/*     */ 
/*     */   public static String computeWebUrlDir(Parameters docInfo, boolean isAbsolute)
/*     */     throws DataException
/*     */   {
/*     */     String root;
/*     */     String root;
/* 186 */     if (isAbsolute)
/*     */     {
/* 188 */       root = getAbsoluteWebRoot();
/*     */     }
/*     */     else
/*     */     {
/* 192 */       root = getRelativeWebRoot();
/*     */     }
/* 194 */     String wDir = root + computeWebDirPartialPath(docInfo);
/* 195 */     return wDir;
/*     */   }
/*     */ 
/*     */   public static String computeRelativeVaultDir(Parameters docInfo)
/*     */     throws DataException
/*     */   {
/* 203 */     String docType = docInfo.get("dDocType");
/* 204 */     String docAccount = docInfo.get("dDocAccount");
/*     */ 
/* 206 */     String relativeVaultDir = docType.toLowerCase() + "/";
/* 207 */     if ((docAccount != null) && (docAccount.trim().length() > 0))
/*     */     {
/* 209 */       relativeVaultDir = relativeVaultDir + "@" + convertToAccountDirStr(docAccount) + "/";
/*     */     }
/*     */ 
/* 212 */     return relativeVaultDir;
/*     */   }
/*     */ 
/*     */   public static String convertToAccountDirStr(String acct)
/*     */   {
/* 217 */     int len = acct.length();
/* 218 */     StringBuffer b = new StringBuffer(len * 2);
/*     */ 
/* 220 */     for (int i = 0; i < len; ++i)
/*     */     {
/* 222 */       char ch = acct.charAt(i);
/* 223 */       if (m_allLowerCase)
/*     */       {
/* 225 */         ch = Character.toLowerCase(ch);
/*     */       }
/* 227 */       b.append(ch);
/* 228 */       if (ch != '/')
/*     */         continue;
/* 230 */       b.append('@');
/*     */     }
/*     */ 
/* 234 */     return b.toString();
/*     */   }
/*     */ 
/*     */   public static String computeRenditionFilename(Parameters docInfo, int counter)
/*     */     throws DataException
/*     */   {
/* 240 */     String docName = docInfo.get("dDocName");
/* 241 */     String renFlag = docInfo.get("dRendition" + counter);
/* 242 */     String revLabel = docInfo.get("dRevLabel");
/* 243 */     String ext = docInfo.get("renExtension" + counter);
/*     */ 
/* 245 */     return computeRenditionFilename(docName, renFlag, revLabel, ext);
/*     */   }
/*     */ 
/*     */   public static String computeRenditionFilename(String docName, String renFlag, String revLabel, String ext)
/*     */   {
/* 250 */     String filename = docName.toLowerCase() + computeRenditionTail(renFlag, revLabel, ext);
/* 251 */     return filename;
/*     */   }
/*     */ 
/*     */   public static String computeRenditionTail(String renFlag, String revLabel, String ext)
/*     */   {
/* 256 */     String tail = "@" + renFlag + "~" + revLabel;
/* 257 */     if ((ext != null) && (ext.trim().length() > 0))
/*     */     {
/* 259 */       tail = tail + "." + ext;
/*     */     }
/*     */ 
/* 262 */     return tail.toLowerCase();
/*     */   }
/*     */ 
/*     */   public static String computeWebFileExtension(Parameters docInfo, boolean isReleased)
/*     */     throws DataException
/*     */   {
/* 268 */     String revLabel = docInfo.get("dRevLabel").toLowerCase();
/* 269 */     String ext = docInfo.get("dWebExtension").toLowerCase();
/*     */ 
/* 271 */     String tail = "";
/*     */ 
/* 273 */     if (!isReleased)
/*     */     {
/* 275 */       tail = tail + "~" + revLabel;
/*     */     }
/*     */ 
/* 278 */     if ((ext != null) && (ext.trim().length() > 0))
/*     */     {
/* 280 */       tail = tail + "." + ext;
/*     */     }
/*     */ 
/* 283 */     return tail.toLowerCase();
/*     */   }
/*     */ 
/*     */   public static String extractFileNameFromUrl(String path)
/*     */   {
/* 291 */     int j = path.indexOf("//");
/* 292 */     if (j >= 0)
/*     */     {
/* 294 */       path = path.substring(j + 2);
/* 295 */       j = path.indexOf("/");
/* 296 */       if (j >= 0)
/*     */       {
/* 298 */         path = path.substring(j + 1);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 303 */     String[] seps = { "#", "?" };
/* 304 */     int length = seps.length;
/* 305 */     for (int i = 0; i < length; ++i)
/*     */     {
/* 307 */       j = path.indexOf(seps[i]);
/* 308 */       if (j < 0)
/*     */         continue;
/* 310 */       path = path.substring(0, j);
/*     */     }
/*     */ 
/* 314 */     i = path.lastIndexOf("/");
/* 315 */     path = path.substring(i + 1);
/* 316 */     i = path.lastIndexOf(".");
/* 317 */     if (i >= 0)
/*     */     {
/* 319 */       path = path.substring(0, i);
/*     */     }
/*     */ 
/* 322 */     return path;
/*     */   }
/*     */ 
/*     */   public static String extractDocNameFromFileName(String filename)
/*     */   {
/* 327 */     String path = filename;
/* 328 */     String[] seps = { ".", "~" };
/* 329 */     int length = seps.length;
/* 330 */     for (int i = 0; i < length; ++i)
/*     */     {
/* 332 */       int j = path.lastIndexOf(seps[i]);
/* 333 */       if (j < 0)
/*     */         continue;
/* 335 */       path = path.substring(0, j);
/*     */     }
/*     */ 
/* 339 */     return path;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 344 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.LegacyDocumentPathBuilder
 * JD-Core Version:    0.5.4
 */