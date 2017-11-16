/*     */ package intradoc.data;
/*     */ 
/*     */ import intradoc.common.StringUtils;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class SimpleResultSetFilter
/*     */   implements ResultSetFilter
/*     */ {
/*     */   public boolean m_isFilterValue;
/*     */   public String m_lookupVal;
/*     */   public boolean m_isWildcard;
/*     */   public boolean m_isFilterMaxRows;
/*     */   public int m_maxRows;
/*     */   public boolean m_ignoreCase;
/*     */ 
/*     */   public SimpleResultSetFilter()
/*     */   {
/*  69 */     this.m_isFilterValue = false;
/*  70 */     this.m_lookupVal = "";
/*  71 */     this.m_isWildcard = false;
/*  72 */     this.m_isFilterMaxRows = false;
/*  73 */     this.m_maxRows = 0;
/*  74 */     this.m_ignoreCase = true;
/*     */   }
/*     */ 
/*     */   public SimpleResultSetFilter(String lookupVal)
/*     */   {
/*  82 */     this.m_isFilterValue = true;
/*  83 */     this.m_lookupVal = lookupVal;
/*  84 */     this.m_isWildcard = false;
/*  85 */     this.m_isFilterMaxRows = false;
/*  86 */     this.m_maxRows = 0;
/*  87 */     this.m_ignoreCase = true;
/*     */   }
/*     */ 
/*     */   public SimpleResultSetFilter(int maxRows)
/*     */   {
/*  95 */     this.m_isFilterValue = false;
/*  96 */     this.m_lookupVal = "";
/*  97 */     this.m_isWildcard = false;
/*  98 */     this.m_isFilterMaxRows = true;
/*  99 */     this.m_maxRows = maxRows;
/* 100 */     this.m_ignoreCase = true;
/*     */   }
/*     */ 
/*     */   public SimpleResultSetFilter(String lookupVal, boolean caseSensitive)
/*     */   {
/* 108 */     this.m_isFilterValue = true;
/* 109 */     this.m_lookupVal = lookupVal;
/* 110 */     this.m_isWildcard = false;
/* 111 */     this.m_isFilterMaxRows = false;
/* 112 */     this.m_maxRows = 0;
/* 113 */     this.m_ignoreCase = caseSensitive;
/*     */   }
/*     */ 
/*     */   public int checkRow(String val, int numCopiedRows, Vector row)
/*     */   {
/* 121 */     int retVal = 1;
/* 122 */     if ((this.m_isFilterMaxRows) && 
/* 124 */       (numCopiedRows >= this.m_maxRows))
/*     */     {
/* 126 */       retVal = -1;
/*     */     }
/*     */ 
/* 129 */     if ((retVal == 1) && (this.m_isFilterValue))
/*     */     {
/* 131 */       if (!this.m_isWildcard)
/*     */       {
/* 133 */         if (this.m_ignoreCase)
/*     */         {
/* 135 */           if (!val.equalsIgnoreCase(this.m_lookupVal))
/*     */           {
/* 137 */             retVal = 0;
/*     */           }
/*     */ 
/*     */         }
/* 142 */         else if (!val.equals(this.m_lookupVal))
/*     */         {
/* 144 */           retVal = 0;
/*     */         }
/*     */ 
/*     */       }
/* 150 */       else if (!StringUtils.matchEx(val, this.m_lookupVal, this.m_ignoreCase, true))
/*     */       {
/* 152 */         retVal = 0;
/*     */       }
/*     */     }
/*     */ 
/* 156 */     return retVal;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 161 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 93477 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.data.SimpleResultSetFilter
 * JD-Core Version:    0.5.4
 */