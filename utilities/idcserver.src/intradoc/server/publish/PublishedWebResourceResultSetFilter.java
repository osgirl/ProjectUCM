/*     */ package intradoc.server.publish;
/*     */ 
/*     */ import intradoc.common.Report;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.ResultSetFilter;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class PublishedWebResourceResultSetFilter
/*     */   implements ResultSetFilter
/*     */ {
/*     */   protected int m_index;
/*     */   protected String[] m_classTypes;
/*     */   protected String[] m_suffixes;
/*     */   protected int[] m_suffixLengths;
/*     */   protected boolean m_didInit;
/*     */ 
/*     */   public PublishedWebResourceResultSetFilter(int index)
/*     */   {
/*  36 */     this.m_index = index;
/*     */   }
/*     */ 
/*     */   protected void init() throws DataException
/*     */   {
/*  41 */     this.m_suffixes = PublishedResourceUtils.getPublishedWebResourceSuffixes();
/*  42 */     this.m_suffixLengths = new int[this.m_suffixes.length];
/*  43 */     for (int i = 0; i < this.m_suffixes.length; ++i)
/*     */     {
/*  45 */       if (!this.m_suffixes[i].startsWith("."))
/*     */       {
/*  47 */         this.m_suffixes[i] = ('.' + this.m_suffixes[i]);
/*     */       }
/*  49 */       this.m_suffixLengths[i] = this.m_suffixes[i].length();
/*     */     }
/*  51 */     this.m_didInit = true;
/*     */   }
/*     */ 
/*     */   public int checkRow(String value, int curNumRows, Vector row)
/*     */   {
/*  56 */     if (!this.m_didInit)
/*     */     {
/*     */       try
/*     */       {
/*  60 */         init();
/*     */       }
/*     */       catch (DataException e)
/*     */       {
/*  64 */         Report.trace("publish", null, e);
/*  65 */         return -1;
/*     */       }
/*     */     }
/*     */ 
/*  69 */     boolean found = false;
/*  70 */     if (null != this.m_classTypes)
/*     */     {
/*  73 */       for (int i = 0; i < this.m_classTypes.length; ++i)
/*     */       {
/*  75 */         if (!PublishedResourceUtils.classnameMatches(this.m_classTypes[i], value))
/*     */           continue;
/*  77 */         found = true;
/*  78 */         break;
/*     */       }
/*     */ 
/*  81 */       if (found)
/*     */       {
/*  84 */         String classname = PublishedResourceUtils.toClassname(value);
/*  85 */         row.set(this.m_index, classname);
/*     */       }
/*     */     }
/*  88 */     if (null != this.m_suffixes)
/*     */     {
/*  90 */       int vlen = value.length();
/*  91 */       for (int i = 0; i < this.m_suffixes.length; ++i)
/*     */       {
/*  93 */         String suffix = this.m_suffixes[i];
/*  94 */         int slen = this.m_suffixLengths[i];
/*  95 */         if (vlen < slen) {
/*     */           continue;
/*     */         }
/*     */ 
/*  99 */         if (!value.regionMatches(true, vlen - slen, suffix, 0, slen))
/*     */           continue;
/* 101 */         found = true;
/* 102 */         break;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 107 */     return (found) ? 1 : 0;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 112 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 84562 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.publish.PublishedWebResourceResultSetFilter
 * JD-Core Version:    0.5.4
 */