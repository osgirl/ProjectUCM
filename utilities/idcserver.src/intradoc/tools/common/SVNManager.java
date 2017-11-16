/*     */ package intradoc.tools.common;
/*     */ 
/*     */ import java.io.File;
/*     */ import java.io.IOException;
/*     */ import java.lang.reflect.Field;
/*     */ import java.lang.reflect.Method;
/*     */ import java.text.DateFormat;
/*     */ import java.text.SimpleDateFormat;
/*     */ import java.util.Collection;
/*     */ import java.util.Date;
/*     */ import java.util.HashMap;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class SVNManager
/*     */ {
/*     */   public final Object m_SVNClientManager;
/*     */   public Object m_SVNLogClient;
/*     */   public Object m_SVNWCClient;
/*     */   protected static final String SVN_INFO_PROPERTIES = "svn:entry:last-author=getAuthor,svn:entry:committed-date=getCommittedDate,svn:entry:committed-rev=getCommittedRevision,svn:entry:prop-time=getPropTime,revision=getRevision,svn:entry:text-time=getTextTime,svn:entry:url=getURL,";
/*     */   protected static boolean s_isInitialized;
/*     */   protected static DateFormat s_iso8601;
/*     */   protected static Class s_class_ISVNAuthenticationManager;
/*     */   protected static Class s_class_ISVNEventHandler;
/*     */   protected static Class s_class_SVNAdminAreaFactory;
/*     */   protected static Class s_class_SVNBasicClient;
/*     */   protected static Method s_method_SVNBasicClient_setEventHandler;
/*     */   protected static Class s_class_SVNClientManager;
/*     */   protected static Method s_method_SVNClientManager_createRepository;
/*     */   protected static Method s_method_SVNClientManager_getLogClient;
/*     */   protected static Method s_method_SVNClientManager_getWCClient;
/*     */   protected static Method s_method_SVNClientManager_newInstance;
/*     */   protected static Class s_class_SVNDepth;
/*     */   protected static Map<String, Object> s_SVNDepths;
/*     */   protected static Class s_class_SVNDirEntry;
/*     */   protected static Map<String, Integer> s_SVNDirEntries;
/*     */   protected static Method s_method_SVNDirEntry_getKind;
/*     */   protected static Method s_method_SVNDirEntry_getName;
/*     */   protected static Class s_class_SVNNodeKind;
/*     */   protected static Map<String, Object> s_SVNNodeKindsByName;
/*     */   protected static Map<Object, String> s_SVNNodeKindsByKind;
/*     */   protected static Class s_class_SVNProperties;
/*     */   protected static Class s_class_SVNRepository;
/*     */   protected static Method s_method_SVNRepository_closeSession;
/*     */   protected static Method s_method_SVNRepository_getLatestRevision;
/*     */   protected static Method s_method_SVNRepository_getDir_String_long_SVNProperties_int_Collection;
/*     */   protected static Method s_method_SVNRepository_setAuthenticationManager;
/*     */   protected static Class s_class_SVNRevision;
/*     */   protected static Map<String, Object> s_SVNRevisions;
/*     */   protected static Class s_class_SVNURL;
/*     */   protected static Method s_method_SVNURL_parseURIEncoded;
/*     */   protected static SVNManager s_sharedSVNManager;
/*     */ 
/*     */   public SVNManager()
/*     */   {
/*  43 */     initSVNKit();
/*     */     try
/*     */     {
/*  49 */       this.m_SVNClientManager = s_method_SVNClientManager_newInstance.invoke(null, new Object[0]);
/*     */     }
/*     */     catch (RuntimeException re)
/*     */     {
/*  53 */       throw re;
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/*  57 */       throw new RuntimeException(t);
/*     */     }
/*     */   }
/*     */ 
/*     */   public int getSVNDirEntry(String direntString)
/*     */   {
/*  68 */     Integer value = (Integer)s_SVNDirEntries.get(direntString);
/*  69 */     if (value == null)
/*     */     {
/*  71 */       return 0;
/*     */     }
/*  73 */     return value.intValue();
/*     */   }
/*     */ 
/*     */   public String getSVNDirEntryKind(Object dirEntry)
/*     */   {
/*     */     try
/*     */     {
/*  87 */       Object kind = s_method_SVNDirEntry_getKind.invoke(dirEntry, new Object[0]);
/*  88 */       String kindString = (String)s_SVNNodeKindsByKind.get(kind);
/*  89 */       return kindString;
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/*  93 */       throw new RuntimeException(t);
/*     */     }
/*     */   }
/*     */ 
/*     */   public String getSVNDirEntryName(Object dirEntry)
/*     */   {
/*     */     try
/*     */     {
/* 108 */       Object name = s_method_SVNDirEntry_getName.invoke(dirEntry, new Object[0]);
/* 109 */       return (String)name;
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/* 113 */       throw new RuntimeException(t);
/*     */     }
/*     */   }
/*     */ 
/*     */   public Object createRepository(String url)
/*     */     throws IOException
/*     */   {
/* 126 */     return createRepository(url, null);
/*     */   }
/*     */ 
/*     */   public Object createRepository(String url, Object authManager)
/*     */     throws IOException
/*     */   {
/*     */     Object repository;
/*     */     try
/*     */     {
/* 143 */       Object svnurl = s_method_SVNURL_parseURIEncoded.invoke(null, new Object[] { url });
/*     */ 
/* 147 */       repository = s_method_SVNClientManager_createRepository.invoke(this.m_SVNClientManager, new Object[] { svnurl, Boolean.valueOf(true) });
/* 148 */       if (authManager != null)
/*     */       {
/* 153 */         s_method_SVNRepository_setAuthenticationManager.invoke(repository, new Object[] { authManager });
/*     */       }
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/* 158 */       throw new IOException("unable to create repository for URL " + url, t);
/*     */     }
/* 160 */     return repository;
/*     */   }
/*     */ 
/*     */   public Collection getDir(Object repository, String path, long rev, int entryFields, Collection dirEntries)
/*     */     throws IOException
/*     */   {
/*     */     try
/*     */     {
/* 183 */       Object entries = s_method_SVNRepository_getDir_String_long_SVNProperties_int_Collection.invoke(repository, new Object[] { path, Long.valueOf(rev), null, Integer.valueOf(entryFields), dirEntries });
/*     */ 
/* 186 */       return (Collection)entries;
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/* 190 */       throw new IOException("unable to get directory " + path, t);
/*     */     }
/*     */   }
/*     */ 
/*     */   public long getLatestRevision(Object repository)
/*     */     throws IOException
/*     */   {
/*     */     try
/*     */     {
/* 206 */       Object revision = s_method_SVNRepository_getLatestRevision.invoke(repository, new Object[0]);
/* 207 */       return ((Long)revision).longValue();
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/* 211 */       throw new IOException("unable to get latest revision", t);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void closeRepositorySession(Object repository)
/*     */     throws IOException
/*     */   {
/*     */     try
/*     */     {
/* 222 */       s_method_SVNRepository_closeSession.invoke(repository, new Object[0]);
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/* 226 */       throw new IOException("unable to close session", t);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected Object getOrCreateLogClient()
/*     */   {
/* 234 */     Object client = this.m_SVNLogClient;
/* 235 */     if (client == null)
/*     */     {
/*     */       try
/*     */       {
/* 242 */         client = s_method_SVNClientManager_getLogClient.invoke(this.m_SVNClientManager, new Object[0]);
/*     */       }
/*     */       catch (RuntimeException re)
/*     */       {
/* 246 */         throw re;
/*     */       }
/*     */       catch (Throwable t)
/*     */       {
/* 250 */         throw new RuntimeException(t);
/*     */       }
/*     */     }
/* 253 */     return client;
/*     */   }
/*     */ 
/*     */   protected Object getOrCreateWCClient()
/*     */   {
/* 260 */     Object client = this.m_SVNWCClient;
/* 261 */     if (client == null)
/*     */     {
/*     */       try
/*     */       {
/* 268 */         client = s_method_SVNClientManager_getWCClient.invoke(this.m_SVNClientManager, new Object[0]);
/*     */       }
/*     */       catch (RuntimeException re)
/*     */       {
/* 272 */         throw re;
/*     */       }
/*     */       catch (Throwable t)
/*     */       {
/* 276 */         throw new RuntimeException(t);
/*     */       }
/*     */     }
/* 279 */     return client;
/*     */   }
/*     */ 
/*     */   public Map<String, String> getWCInfo(File workingCopyDir)
/*     */     throws IOException
/*     */   {
/*     */     Object info;
/*     */     try
/*     */     {
/* 296 */       Object client = getOrCreateWCClient();
/*     */ 
/* 300 */       Method doInfoMethod = client.getClass().getMethod("doInfo", new Class[] { File.class, s_class_SVNRevision });
/* 301 */       Object working = s_SVNRevisions.get("WORKING");
/* 302 */       info = doInfoMethod.invoke(client, new Object[] { workingCopyDir, working });
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/* 306 */       throw new IOException("unable to get working copy info", t);
/*     */     }
/* 308 */     Map propertyMap = new HashMap();
/*     */     try
/*     */     {
/* 311 */       Class infoClass = info.getClass();
/* 312 */       String[] properties = "svn:entry:last-author=getAuthor,svn:entry:committed-date=getCommittedDate,svn:entry:committed-rev=getCommittedRevision,svn:entry:prop-time=getPropTime,revision=getRevision,svn:entry:text-time=getTextTime,svn:entry:url=getURL,".split(",");
/* 313 */       for (int p = properties.length - 1; p >= 0; --p)
/*     */       {
/* 315 */         String property = properties[p];
/* 316 */         int equals = property.indexOf(61);
/* 317 */         if (equals < 0) {
/*     */           continue;
/*     */         }
/*     */ 
/* 321 */         String propertyName = property.substring(0, equals);
/* 322 */         String methodName = property.substring(equals + 1);
/* 323 */         Method method = infoClass.getMethod(methodName, new Class[0]);
/* 324 */         Object propertyValue = method.invoke(info, new Object[0]);
/*     */ 
/* 326 */         if (propertyValue == null)
/*     */           continue;
/*     */         String value;
/*     */         String value;
/* 330 */         if (propertyValue instanceof Date)
/*     */         {
/* 332 */           value = s_iso8601.format((Date)propertyValue);
/*     */         }
/*     */         else
/*     */         {
/*     */           String value;
/* 334 */           if (propertyValue instanceof String)
/*     */           {
/* 336 */             value = (String)propertyValue;
/*     */           }
/*     */           else
/*     */           {
/* 340 */             value = propertyValue.toString();
/*     */           }
/*     */         }
/* 342 */         propertyMap.put(propertyName, value);
/*     */       }
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/* 347 */       throw new IOException("unable to get working copy info properties", t);
/*     */     }
/* 349 */     return propertyMap;
/*     */   }
/*     */ 
/*     */   protected int getWCFormat(float svnVersion)
/*     */     throws IOException
/*     */   {
/*     */     String fieldName;
/* 363 */     if (svnVersion >= 1.7F)
/*     */     {
/* 365 */       fieldName = "WC_FORMAT_17";
/*     */     }
/*     */     else
/*     */     {
/*     */       String fieldName;
/* 367 */       if (svnVersion >= 1.6F)
/*     */       {
/* 369 */         fieldName = "WC_FORMAT_16";
/*     */       }
/*     */       else
/*     */       {
/*     */         String fieldName;
/* 371 */         if (svnVersion >= 1.5F)
/*     */         {
/* 373 */           fieldName = "WC_FORMAT_15";
/*     */         }
/*     */         else
/*     */         {
/*     */           String fieldName;
/* 375 */           if (svnVersion >= 1.4F)
/*     */           {
/* 377 */             fieldName = "WC_FORMAT_14";
/*     */           }
/*     */           else
/*     */           {
/*     */             String fieldName;
/* 379 */             if (svnVersion >= 1.3F)
/*     */             {
/* 381 */               fieldName = "WC_FORMAT_13";
/*     */             }
/*     */             else
/*     */             {
/* 385 */               throw new IOException("working copy format out of range:" + svnVersion);
/*     */             }
/*     */           }
/*     */         }
/*     */       }
/*     */     }
/*     */     try
/*     */     {
/*     */       String fieldName;
/* 390 */       Field field = s_class_SVNAdminAreaFactory.getField(fieldName);
/* 391 */       int format = field.getInt(null);
/* 392 */       return format;
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/* 396 */       throw new IOException(t);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setWCFormat(File WCPath, float svnVersion)
/*     */     throws IOException
/*     */   {
/* 409 */     int format = getWCFormat(svnVersion);
/* 410 */     Object client = getOrCreateWCClient();
/*     */     try
/*     */     {
/* 413 */       Method method = client.getClass().getMethod("doSetWCFormat", new Class[] { File.class, Integer.TYPE });
/* 414 */       method.invoke(client, new Object[] { WCPath, Integer.valueOf(format) });
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/* 418 */       throw new IOException(t);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void initSVNKit()
/*     */   {
/* 487 */     if (s_isInitialized)
/*     */       return;
/* 489 */     s_iso8601 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss'Z'");
/*     */     try
/*     */     {
/* 500 */       Class cl = Class.forName("org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory");
/* 501 */       Method method = cl.getMethod("setup", new Class[0]);
/* 502 */       method.invoke(null, new Object[0]);
/*     */ 
/* 506 */       cl = Class.forName("org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl");
/* 507 */       method = cl.getMethod("setup", new Class[0]);
/* 508 */       method.invoke(null, new Object[0]);
/*     */ 
/* 511 */       cl = SVNManager.s_class_SVNDepth = Class.forName("org.tmatesoft.svn.core.SVNDepth");
/* 512 */       method = cl.getMethod("getName", new Class[0]);
/* 513 */       Map map = SVNManager.s_SVNDepths = new HashMap();
/* 514 */       String[] depthsNames = { "UNKNOWN", "EXCLUDE", "EMPTY", "FILES", "IMMEDIATES", "INFINITY" };
/* 515 */       for (String name : depthsNames)
/*     */       {
/* 517 */         Field field = cl.getField(name);
/* 518 */         Object depth = field.get(null);
/* 519 */         map.put(name, depth);
/* 520 */         name = (String)method.invoke(depth, new Object[0]);
/* 521 */         map.put(name, depth);
/*     */       }
/*     */ 
/* 524 */       cl = SVNManager.s_class_SVNDirEntry = Class.forName("org.tmatesoft.svn.core.SVNDirEntry");
/* 525 */       map = SVNManager.s_SVNDirEntries = new HashMap();
/* 526 */       String[] direntNames = { "KIND", "SIZE", "HAS_PROPERTIES", "CREATED_REVISION", "TIME", "LAST_AUTHOR", "COMMIT_MESSAGE" };
/*     */ 
/* 528 */       for (String name : direntNames)
/*     */       {
/* 530 */         name = "DIRENT_" + name;
/* 531 */         Field field = cl.getField(name);
/* 532 */         Object dirent = field.get(null);
/* 533 */         map.put(name, dirent);
/*     */       }
/* 535 */       method = SVNManager.s_method_SVNDirEntry_getKind = cl.getMethod("getKind", new Class[0]);
/* 536 */       method = SVNManager.s_method_SVNDirEntry_getName = cl.getMethod("getName", new Class[0]);
/*     */ 
/* 538 */       cl = SVNManager.s_class_SVNNodeKind = Class.forName("org.tmatesoft.svn.core.SVNNodeKind");
/* 539 */       map = SVNManager.s_SVNNodeKindsByName = new HashMap();
/* 540 */       Map mapBack = SVNManager.s_SVNNodeKindsByKind = new HashMap();
/* 541 */       String[] nodeKindNames = { "NONE", "FILE", "DIR", "UNKNOWN" };
/* 542 */       for (String name : nodeKindNames)
/*     */       {
/* 544 */         Field field = cl.getField(name);
/* 545 */         Object nodeKind = field.get(null);
/* 546 */         map.put(name, nodeKind);
/* 547 */         mapBack.put(nodeKind, name);
/*     */       }
/*     */ 
/* 550 */       cl = SVNManager.s_class_SVNProperties = Class.forName("org.tmatesoft.svn.core.SVNProperties");
/*     */ 
/* 552 */       cl = SVNManager.s_class_SVNRepository = Class.forName("org.tmatesoft.svn.core.io.SVNRepository");
/* 553 */       method = SVNManager.s_method_SVNRepository_closeSession = cl.getMethod("closeSession", new Class[0]);
/* 554 */       method = SVNManager.s_method_SVNRepository_getLatestRevision = cl.getMethod("getLatestRevision", new Class[0]);
/* 555 */       method = SVNManager.s_method_SVNRepository_getDir_String_long_SVNProperties_int_Collection = cl.getMethod("getDir", new Class[] { String.class, Long.TYPE, s_class_SVNProperties, Integer.TYPE, Collection.class });
/*     */ 
/* 557 */       Class iAuthM = SVNManager.s_class_ISVNAuthenticationManager = Class.forName("org.tmatesoft.svn.core.auth.ISVNAuthenticationManager");
/*     */ 
/* 559 */       method = SVNManager.s_method_SVNRepository_setAuthenticationManager = cl.getMethod("setAuthenticationManager", new Class[] { iAuthM });
/*     */ 
/* 562 */       cl = SVNManager.s_class_SVNRevision = Class.forName("org.tmatesoft.svn.core.wc.SVNRevision");
/* 563 */       map = SVNManager.s_SVNRevisions = new HashMap();
/* 564 */       String[] revisionsNames = { "HEAD", "WORKING", "PREVIOUS", "BASE", "COMMITTED", "UNDEFINED" };
/* 565 */       for (String name : revisionsNames)
/*     */       {
/* 567 */         Field field = cl.getField(name);
/* 568 */         Object rev = field.get(null);
/* 569 */         map.put(name, rev);
/*     */       }
/*     */ 
/* 573 */       Class iEventH = SVNManager.s_class_ISVNEventHandler = Class.forName("org.tmatesoft.svn.core.wc.ISVNEventHandler");
/* 574 */       s_class_SVNAdminAreaFactory = Class.forName("org.tmatesoft.svn.core.internal.wc.admin.SVNAdminAreaFactory");
/*     */ 
/* 577 */       cl = SVNManager.s_class_SVNURL = Class.forName("org.tmatesoft.svn.core.SVNURL");
/* 578 */       method = SVNManager.s_method_SVNURL_parseURIEncoded = cl.getMethod("parseURIEncoded", new Class[] { String.class });
/*     */ 
/* 580 */       cl = SVNManager.s_class_SVNBasicClient = Class.forName("org.tmatesoft.svn.core.wc.SVNBasicClient");
/* 581 */       method = SVNManager.s_method_SVNBasicClient_setEventHandler = cl.getMethod("setEventHandler", new Class[] { iEventH });
/*     */ 
/* 583 */       cl = SVNManager.s_class_SVNClientManager = Class.forName("org.tmatesoft.svn.core.wc.SVNClientManager");
/* 584 */       method = SVNManager.s_method_SVNClientManager_createRepository = cl.getMethod("createRepository", new Class[] { s_class_SVNURL, Boolean.TYPE });
/*     */ 
/* 586 */       method = SVNManager.s_method_SVNClientManager_getLogClient = cl.getMethod("getLogClient", new Class[0]);
/* 587 */       method = SVNManager.s_method_SVNClientManager_getWCClient = cl.getMethod("getWCClient", new Class[0]);
/* 588 */       method = SVNManager.s_method_SVNClientManager_newInstance = cl.getMethod("newInstance", new Class[0]);
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/* 593 */       throw new RuntimeException("unable to initialize SVNKit", t);
/*     */     }
/* 595 */     s_isInitialized = true;
/*     */   }
/*     */ 
/*     */   public static SVNManager getOrCreateSharedSVNManager()
/*     */   {
/* 606 */     SVNManager manager = s_sharedSVNManager;
/* 607 */     if (manager == null)
/*     */     {
/* 609 */       manager = SVNManager.s_sharedSVNManager = new SVNManager();
/*     */     }
/* 611 */     return manager;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 618 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98273 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.tools.common.SVNManager
 * JD-Core Version:    0.5.4
 */