/*     */ package intradoc.search;
/*     */ 
/*     */ import intradoc.common.IdcAppendable;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class ParsedQueryElements
/*     */ {
/*     */   public boolean m_isFinishedParsing;
/*     */   public boolean m_isCompacted;
/*     */   public boolean m_hasFullTextElement;
/*     */   public boolean m_isError;
/*     */   public int m_errorReason;
/*     */   public int m_rawElementIndex;
/*  62 */   public List m_rawParsedElements = new ArrayList();
/*     */   public String m_rawParsedElementsDebugString;
/*     */   public int m_rawElementDebugStringIndex;
/*     */   public QueryElement m_securityClauseElement;
/*     */   public QueryElement m_searchQuery;
/*     */   public Map<String, Integer> m_nameToField;
/*     */ 
/*     */   public void setError(int errReason, int index)
/*     */   {
/* 109 */     if (this.m_isError)
/*     */       return;
/* 111 */     this.m_isError = true;
/* 112 */     this.m_errorReason = errReason;
/* 113 */     this.m_rawElementIndex = index;
/*     */   }
/*     */ 
/*     */   public void appendDebugFormat(IdcAppendable appendable)
/*     */   {
/* 123 */     appendable.append("isCompacted=");
/* 124 */     appendable.append("" + this.m_isCompacted);
/* 125 */     appendable.append(",isError=");
/* 126 */     appendable.append("" + this.m_isError);
/* 127 */     if (!this.m_isCompacted)
/*     */     {
/* 129 */       appendable.append("\nRaw Query Elements=");
/* 130 */       SearchQueryUtils.computeRawDisplayStringAndOffset(appendable, this.m_rawParsedElements, 0, null);
/*     */     }
/*     */     else
/*     */     {
/* 134 */       if (this.m_securityClauseElement != null)
/*     */       {
/* 136 */         appendable.append("\nSecurity Clause=");
/* 137 */         this.m_securityClauseElement.appendDebugFormat(appendable);
/*     */       }
/* 139 */       if (this.m_searchQuery == null)
/*     */         return;
/* 141 */       appendable.append("\nSearch Query=");
/* 142 */       this.m_searchQuery.appendDebugFormat(appendable);
/*     */     }
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 154 */     IdcStringBuilder output = new IdcStringBuilder();
/* 155 */     appendDebugFormat(output);
/* 156 */     return output.toString();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 161 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.search.ParsedQueryElements
 * JD-Core Version:    0.5.4
 */