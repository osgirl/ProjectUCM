/*     */ package intradoc.data;
/*     */ 
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class MultiResultSetFilter
/*     */   implements ResultSetFilter
/*     */ {
/*     */   public static final int F_ALL_ALLOW = 1;
/*     */   public static final int F_ANY_DENY = 1;
/*     */   public static final int F_ANY_ALLOW = 2;
/*     */   public static final int F_ALL_DENY = 2;
/*     */   public static final int F_INVERSE = 128;
/*     */   public int m_flags;
/*     */   public ResultSetFilter[] m_filters;
/*     */ 
/*     */   public MultiResultSetFilter(int flags)
/*     */   {
/*  95 */     this.m_flags = flags;
/*     */   }
/*     */ 
/*     */   public MultiResultSetFilter(int flags, int numFilters)
/*     */   {
/* 104 */     this.m_flags = flags;
/* 105 */     this.m_filters = new ResultSetFilter[numFilters];
/*     */   }
/*     */ 
/*     */   public MultiResultSetFilter(int flags, ResultSetFilter aFilter)
/*     */   {
/* 114 */     this.m_flags = flags;
/* 115 */     this.m_filters = new ResultSetFilter[4];
/* 116 */     this.m_filters[0] = aFilter;
/*     */   }
/*     */ 
/*     */   public MultiResultSetFilter(int flags, ResultSetFilter aFilter, ResultSetFilter bFilter)
/*     */   {
/* 126 */     this.m_flags = flags;
/* 127 */     this.m_filters = new ResultSetFilter[4];
/* 128 */     this.m_filters[0] = aFilter;
/* 129 */     this.m_filters[1] = bFilter;
/*     */   }
/*     */ 
/*     */   public int checkRow(String key, int numRows, Vector row)
/*     */   {
/* 143 */     boolean isAnd = (this.m_flags & 0x1) != 0;
/* 144 */     boolean isOr = (this.m_flags & 0x2) != 0;
/* 145 */     boolean isNot = (this.m_flags & 0x80) != 0;
/* 146 */     if (!((isAnd ^ isOr)))
/*     */     {
/* 148 */       return -1;
/*     */     }
/* 150 */     int numFilters = this.m_filters.length;
/* 151 */     for (int i = 0; i < numFilters; ++i)
/*     */     {
/* 153 */       ResultSetFilter filter = this.m_filters[i];
/* 154 */       if (filter == null) {
/*     */         continue;
/*     */       }
/*     */ 
/* 158 */       int result = filter.checkRow(key, numRows, row);
/* 159 */       switch (result)
/*     */       {
/*     */       case -1:
/* 162 */         return -1;
/*     */       case 1:
/* 164 */         if (isOr)
/*     */         {
/* 166 */           return (isNot) ? 0 : 1;
/*     */         }
/*     */       case 0:
/* 170 */         if (isAnd)
/*     */         {
/* 172 */           return (isNot) ? 1 : 0;
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 177 */     return (isAnd) ? 1 : 0;
/*     */   }
/*     */ 
/*     */   public void addFilter(ResultSetFilter filter)
/*     */   {
/* 188 */     int numFilters = this.m_filters.length;
/* 189 */     for (int i = 0; i < this.m_filters.length; ++i)
/*     */     {
/* 191 */       if (this.m_filters[i] != null)
/*     */         continue;
/* 193 */       this.m_filters[i] = filter;
/* 194 */       return;
/*     */     }
/*     */ 
/* 197 */     ResultSetFilter[] filters = new ResultSetFilter[numFilters << 1];
/* 198 */     System.arraycopy(this.m_filters, 0, filters, 0, numFilters);
/* 199 */     filters[numFilters] = filter;
/* 200 */     this.m_filters = filters;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 206 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 73791 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.data.MultiResultSetFilter
 * JD-Core Version:    0.5.4
 */