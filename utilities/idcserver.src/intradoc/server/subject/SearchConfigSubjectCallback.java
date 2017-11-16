/*     */ package intradoc.server.subject;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.FileUtilsCfgBuilder;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.server.LegacyDirectoryLocator;
/*     */ import intradoc.server.SubjectCallbackAdapter;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.io.File;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class SearchConfigSubjectCallback extends SubjectCallbackAdapter
/*     */ {
/*     */   public static final String TARGETED_QUICK_SEARCHES_FILE = "targeted_quick_searches.hda";
/*  35 */   public static final String DIRECTORY = LegacyDirectoryLocator.getAppDataDirectory() + "search";
/*  36 */   public static final String[] TARGETED_QUICK_SEARCH_COLUMNS = { "tqsKey", "tqsLabel", "searchFormType", "QueryText", "QueryFullText", "QueryFieldValues", "SearchQueryFormat", "ResultCount", "SortField", "SortOrder", "SearchProviders" };
/*     */ 
/*     */   public void refresh(String subject)
/*     */     throws DataException, ServiceException
/*     */   {
/*     */     try
/*     */     {
/*  45 */       FileUtils.reserveDirectory(DIRECTORY);
/*  46 */       cacheAdminTargetedQuickSearches();
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/*  50 */       String msg = LocaleUtils.encodeMessage("csUnableToLoadSubject", null, subject);
/*     */ 
/*  52 */       Report.error(null, msg, e);
/*     */     }
/*     */     finally
/*     */     {
/*  56 */       FileUtils.releaseDirectory(DIRECTORY);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void cacheAdminTargetedQuickSearches() throws ServiceException, DataException
/*     */   {
/*  62 */     File file = FileUtilsCfgBuilder.getCfgFile(DIRECTORY + "/" + "targeted_quick_searches.hda", "Search", false);
/*     */ 
/*  64 */     if (file.exists())
/*     */     {
/*  66 */       DataBinder binder = ResourceUtils.readDataBinder(DIRECTORY, "targeted_quick_searches.hda");
/*  67 */       DataResultSet rset = (DataResultSet)binder.getResultSet("AdminTargetedQuickSearches");
/*  68 */       SharedObjects.putTable("AdminTargetedQuickSearches", rset);
/*     */     }
/*     */     else
/*     */     {
/*  72 */       DataBinder binder = new DataBinder();
/*  73 */       DataResultSet rset = new DataResultSet(TARGETED_QUICK_SEARCH_COLUMNS);
/*     */ 
/*  75 */       Vector v = new IdcVector();
/*  76 */       v.addElement("a");
/*  77 */       v.addElement("wwAuthor");
/*  78 */       v.addElement("queryBuilder");
/*  79 */       v.addElement("<usch>dDocAuthor <substring> `#s`</usch>");
/*  80 */       v.addElement("");
/*  81 */       v.addElement("");
/*  82 */       v.addElement("Universal");
/*  83 */       v.addElement("20");
/*  84 */       v.addElement("dInDate");
/*  85 */       v.addElement("Desc");
/*  86 */       v.addElement("");
/*  87 */       rset.addRow(v);
/*     */ 
/*  89 */       v = new IdcVector();
/*  90 */       v.addElement("t");
/*  91 */       v.addElement("wwTitle");
/*  92 */       v.addElement("queryBuilder");
/*  93 */       v.addElement("<usch>dDocTitle <substring> `#s`</usch>");
/*  94 */       v.addElement("");
/*  95 */       v.addElement("");
/*  96 */       v.addElement("Universal");
/*  97 */       v.addElement("20");
/*  98 */       v.addElement("dInDate");
/*  99 */       v.addElement("Desc");
/* 100 */       v.addElement("");
/* 101 */       rset.addRow(v);
/*     */ 
/* 103 */       binder.addResultSet("AdminTargetedQuickSearches", rset);
/* 104 */       ResourceUtils.serializeDataBinder(DIRECTORY, "targeted_quick_searches.hda", binder, true, false);
/*     */ 
/* 107 */       SharedObjects.putTable("AdminTargetedQuickSearches", rset);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 113 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97049 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.subject.SearchConfigSubjectCallback
 * JD-Core Version:    0.5.4
 */