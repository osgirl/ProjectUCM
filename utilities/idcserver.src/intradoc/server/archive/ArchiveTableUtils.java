/*     */ package intradoc.server.archive;
/*     */ 
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.Workspace;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class ArchiveTableUtils
/*     */ {
/*  29 */   public static boolean m_useOracleSyntaxForOuterJoin = false;
/*  30 */   public static boolean m_isOuterJoinSyntaxDetermined = false;
/*     */ 
/*     */   public static String constructArchiveQueryFragment(String table, String parentTables, String relStr, String query) {
/*  33 */     Vector tableVect = StringUtils.parseArray(parentTables, ',', '^');
/*  34 */     Vector relations = StringUtils.parseArray(relStr, ',', '^');
/*     */ 
/*  36 */     if (!m_isOuterJoinSyntaxDetermined)
/*     */     {
/*  38 */       Report.trace(null, "Syntax for Oracle Outer join has not been determined.", null);
/*     */     }
/*  40 */     StringBuffer fromStr = new StringBuffer();
/*  41 */     StringBuffer queryBuf = new StringBuffer();
/*     */ 
/*  43 */     if ((m_useOracleSyntaxForOuterJoin) && (query != null) && (query.length() > 0))
/*     */     {
/*  45 */       queryBuf.insert(0, " WHERE (");
/*  46 */       queryBuf.append(')');
/*     */     }
/*     */ 
/*  49 */     if ((!m_useOracleSyntaxForOuterJoin) && (query != null) && (query.length() > 0))
/*     */     {
/*  51 */       queryBuf.append(" WHERE (");
/*  52 */       queryBuf.append(query);
/*  53 */       queryBuf.append(")");
/*     */     }
/*     */ 
/*  56 */     int size = tableVect.size();
/*  57 */     fromStr.append(" FROM ");
/*  58 */     fromStr.append(table);
/*  59 */     for (int i = 0; i < size; ++i)
/*     */     {
/*  61 */       if (m_useOracleSyntaxForOuterJoin)
/*     */       {
/*  63 */         fromStr.append(',');
/*     */       }
/*     */       else
/*     */       {
/*  67 */         fromStr.append(" LEFT OUTER JOIN ");
/*     */       }
/*  69 */       fromStr.append(tableVect.elementAt(i));
/*  70 */       if (m_useOracleSyntaxForOuterJoin)
/*     */       {
/*  72 */         if (queryBuf.length() != 0)
/*     */         {
/*  74 */           queryBuf.append(" AND ");
/*     */         }
/*  76 */         queryBuf.append(relations.elementAt(i));
/*  77 */         queryBuf.append("(+)");
/*     */       }
/*     */       else
/*     */       {
/*  81 */         fromStr.append(" ON ");
/*  82 */         fromStr.append(relations.elementAt(i));
/*     */       }
/*     */     }
/*  85 */     return fromStr.append(queryBuf).toString();
/*     */   }
/*     */ 
/*     */   public static synchronized boolean determineSyntaxOnOuterJoin(Workspace ws)
/*     */   {
/*  90 */     if (m_isOuterJoinSyntaxDetermined)
/*     */     {
/*  92 */       return true;
/*     */     }
/*  94 */     String dbType = ws.getProperty("DatabaseType");
/*  95 */     String version = ws.getProperty("DatabaseVersion");
/*  96 */     if (((dbType.equals("oracle")) && (version.compareTo("09") < 0)) || (dbType.equals("tamino")))
/*     */     {
/*  98 */       m_useOracleSyntaxForOuterJoin = true;
/*     */     }
/* 100 */     m_isOuterJoinSyntaxDetermined = true;
/* 101 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 106 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.archive.ArchiveTableUtils
 * JD-Core Version:    0.5.4
 */