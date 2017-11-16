/*     */ package intradoc.search;
/*     */ 
/*     */ import intradoc.common.IdcAppendable;
/*     */ import intradoc.common.IdcDebugOutput;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleUtils;
/*     */ 
/*     */ public class SearchDocChangeItem
/*     */   implements IdcDebugOutput
/*     */ {
/*  28 */   public static int CHANGE_ADD = 1;
/*  29 */   public static int CHANGE_DELETE = 2;
/*     */   public long m_capturedTime;
/*     */   public long m_changeTime;
/*     */   public int m_changeType;
/*     */   public String m_specificDocID;
/*     */   public String m_classDocID;
/*     */   public CacheRow m_cacheRow;
/*     */   public int m_itemAge;
/*     */   public SearchDocChangeItem m_prev;
/*     */   public SearchDocChangeItem m_next;
/*     */   public int m_refCounterForMapLookup;
/*     */ 
/*     */   public SearchDocChangeItem(String classID, String id, CacheRow cacheRow, long changeTime, int type, long capturedTime, int curAge)
/*     */   {
/* 111 */     this.m_classDocID = classID;
/* 112 */     this.m_specificDocID = id;
/* 113 */     this.m_cacheRow = cacheRow;
/* 114 */     this.m_changeTime = changeTime;
/* 115 */     this.m_changeType = type;
/* 116 */     this.m_capturedTime = capturedTime;
/* 117 */     this.m_itemAge = curAge;
/*     */   }
/*     */ 
/*     */   public void appendDebugFormat(IdcAppendable appendable)
/*     */   {
/* 126 */     appendable.append(this.m_classDocID);
/* 127 */     appendable.append(" (");
/* 128 */     appendable.append("specificDocID=");
/* 129 */     appendable.append("" + this.m_specificDocID);
/* 130 */     appendable.append(", changeTime=");
/* 131 */     appendable.append(LocaleUtils.debugDate(this.m_changeTime));
/* 132 */     appendable.append(", changeType=");
/* 133 */     appendable.append(getChangeTypeAsString(this.m_changeType));
/* 134 */     appendable.append(", capturedTime=");
/* 135 */     appendable.append(LocaleUtils.debugDate(this.m_capturedTime));
/* 136 */     appendable.append(", itemAge=");
/* 137 */     appendable.append("" + this.m_itemAge);
/* 138 */     appendable.append(")");
/*     */   }
/*     */ 
/*     */   public String getChangeTypeAsString(int type)
/*     */   {
/* 143 */     if (type == CHANGE_ADD)
/*     */     {
/* 145 */       return "add";
/*     */     }
/* 147 */     if (type == CHANGE_DELETE)
/*     */     {
/* 149 */       return "delete";
/*     */     }
/* 151 */     return "undefined";
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 161 */     IdcStringBuilder output = new IdcStringBuilder();
/* 162 */     appendDebugFormat(output);
/* 163 */     return output.toString();
/*     */   }
/*     */ 
/*     */   public void insertBefore(SearchDocChangeItem item)
/*     */   {
/* 172 */     this.m_prev = item.m_prev;
/* 173 */     if (this.m_prev != null)
/*     */     {
/* 175 */       this.m_prev.m_next = this;
/*     */     }
/*     */ 
/* 178 */     item.m_prev = this;
/* 179 */     this.m_next = item;
/*     */   }
/*     */ 
/*     */   public void insertAfter(SearchDocChangeItem item)
/*     */   {
/* 188 */     this.m_next = item.m_next;
/* 189 */     if (this.m_next != null)
/*     */     {
/* 191 */       this.m_next.m_prev = this;
/*     */     }
/*     */ 
/* 194 */     item.m_next = this;
/* 195 */     this.m_prev = item;
/*     */   }
/*     */ 
/*     */   public void remove()
/*     */   {
/* 203 */     SearchDocChangeItem next = this.m_next;
/* 204 */     if (this.m_next != null)
/*     */     {
/* 206 */       this.m_next.m_prev = this.m_prev;
/* 207 */       this.m_next = null;
/*     */     }
/*     */ 
/* 210 */     if (this.m_prev == null)
/*     */       return;
/* 212 */     this.m_prev.m_next = next;
/* 213 */     this.m_prev = null;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 220 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 81300 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.search.SearchDocChangeItem
 * JD-Core Version:    0.5.4
 */