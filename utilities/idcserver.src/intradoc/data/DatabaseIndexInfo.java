/*     */ package intradoc.data;
/*     */ 
/*     */ import java.util.ArrayList;
/*     */ import java.util.Properties;
/*     */ 
/*     */ public class DatabaseIndexInfo
/*     */ {
/*     */   public static final int BTREE = 0;
/*     */   public static final int BITMAP = 1;
/*     */   public static final int HASH = 2;
/*     */   public static final int FULLTEXT = 3;
/*     */   public String m_name;
/*     */   public int m_type;
/*     */   public String m_table;
/*     */   public ArrayList m_columns;
/*     */   public boolean m_isUnique;
/*     */   public boolean m_isPrimary;
/*     */   public boolean m_isClustered;
/*     */   public boolean m_isFunctional;
/*     */   public String m_function;
/*     */   public Properties m_additionalProps;
/*     */ 
/*     */   public DatabaseIndexInfo()
/*     */   {
/*  57 */     this.m_name = null;
/*     */ 
/*  62 */     this.m_type = 0;
/*     */ 
/*  67 */     this.m_table = null;
/*     */ 
/*  72 */     this.m_columns = new ArrayList();
/*     */ 
/*  77 */     this.m_isUnique = false;
/*     */ 
/*  82 */     this.m_isPrimary = false;
/*     */ 
/*  87 */     this.m_isClustered = false;
/*     */ 
/*  92 */     this.m_isFunctional = false;
/*     */ 
/*  98 */     this.m_function = null;
/*     */ 
/* 104 */     this.m_additionalProps = new Properties();
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 109 */     return this.m_name + "[" + this.m_table + "(" + this.m_columns + ")]";
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 114 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.data.DatabaseIndexInfo
 * JD-Core Version:    0.5.4
 */