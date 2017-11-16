/*     */ package intradoc.server.subject;
/*     */ 
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DocClassUtils;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.server.SubjectCallbackAdapter;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.concurrent.ConcurrentHashMap;
/*     */ 
/*     */ public class DocClassSubjectCallback extends SubjectCallbackAdapter
/*     */ {
/*     */   public static final String m_tableNameClassDefs = "DocClassDefinition";
/*     */   public static final String m_queryNameClassDefs = "QdocClassDefs";
/*     */   public static final String m_tableNameClassNames = "DocClasses";
/*     */   public static final String m_queryNameClassNames = "QdocClasses";
/*     */ 
/*     */   public void refresh(String subject)
/*     */     throws DataException, ServiceException
/*     */   {
/*  39 */     Map docClassSetMap = new ConcurrentHashMap();
/*  40 */     Map docClassProfileMap = new ConcurrentHashMap();
/*  41 */     List docClassNames = new ArrayList();
/*  42 */     ResultSet rset = this.m_workspace.createResultSet("QdocClassDefs", null);
/*  43 */     if (!rset.isEmpty())
/*     */     {
/*  45 */       int docClassIndex = rset.getFieldInfoIndex("dDocClass");
/*  46 */       int docMetaSetIndex = rset.getFieldInfoIndex("dDocMetaSet");
/*  47 */       int statusIndex = rset.getFieldInfoIndex("dStatus");
/*  48 */       if ((docClassIndex < 0) || (docMetaSetIndex < 0))
/*     */       {
/*  50 */         throw new DataException(null, "apErrorWithTable", new Object[] { "DocClassDefinition" });
/*     */       }
/*     */ 
/*  53 */       for (rset.first(); rset.isRowPresent(); rset.next())
/*     */       {
/*  55 */         String dclass = rset.getStringValue(docClassIndex);
/*  56 */         String dms = rset.getStringValue(docMetaSetIndex);
/*  57 */         String status = rset.getStringValue(statusIndex);
/*     */ 
/*  61 */         if ((dms.equals("DocMeta")) || (dclass.equalsIgnoreCase("Base"))) continue; if (status.equals("ADD"))
/*     */         {
/*     */           continue;
/*     */         }
/*     */ 
/*  67 */         List dSets = (List)docClassSetMap.get(dclass);
/*  68 */         if (dSets == null)
/*     */         {
/*  70 */           dSets = new ArrayList();
/*  71 */           docClassSetMap.put(dclass, dSets);
/*     */         }
/*  73 */         dSets.add(dms);
/*     */       }
/*     */     }
/*     */ 
/*  77 */     rset = this.m_workspace.createResultSet("QdocClasses", null);
/*  78 */     if (!rset.isEmpty())
/*     */     {
/*  80 */       int docClassIndex = rset.getFieldInfoIndex("dDocClass");
/*  81 */       int docClassProfIndex = rset.getFieldInfoIndex("dDefaultProfile");
/*  82 */       if (docClassIndex < 0)
/*     */       {
/*  84 */         throw new DataException(null, "apErrorWithTable", new Object[] { "DocClasses" });
/*     */       }
/*     */ 
/*  87 */       for (rset.first(); rset.isRowPresent(); rset.next())
/*     */       {
/*  89 */         String dclass = rset.getStringValue(docClassIndex);
/*  90 */         docClassNames.add(dclass);
/*  91 */         String profile = rset.getStringValue(docClassProfIndex);
/*  92 */         if ((profile == null) || (profile.length() == 0))
/*     */         {
/*  94 */           profile = "DCP_" + dclass;
/*     */         }
/*  96 */         docClassProfileMap.put(dclass, profile);
/*     */       }
/*     */     }
/*     */ 
/* 100 */     DocClassUtils.cacheDocClasses(docClassSetMap, docClassNames);
/* 101 */     DocClassUtils.cacheDocClassesInfo(docClassProfileMap);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 106 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97937 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.subject.DocClassSubjectCallback
 * JD-Core Version:    0.5.4
 */