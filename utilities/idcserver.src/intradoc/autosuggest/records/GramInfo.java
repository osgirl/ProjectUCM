/*    */ package intradoc.autosuggest.records;
/*    */ 
/*    */ import intradoc.autosuggest.AutoSuggestContext;
/*    */ import intradoc.common.IdcStringBuilder;
/*    */ import java.io.Serializable;
/*    */ import java.util.List;
/*    */ 
/*    */ public class GramInfo
/*    */   implements Serializable
/*    */ {
/*    */   private static final long serialVersionUID = 2691385807392671906L;
/*    */   public transient AutoSuggestContext m_context;
/*    */   public String m_gram;
/*    */   public long m_globalFreq;
/*    */   public List<String> m_occurrenceListIds;
/*    */ 
/*    */   public GramInfo(AutoSuggestContext context)
/*    */   {
/* 42 */     this.m_context = context;
/*    */   }
/*    */ 
/*    */   public GramInfo(GramInfo inputInfo)
/*    */   {
/* 47 */     this.m_context = inputInfo.m_context;
/* 48 */     this.m_gram = inputInfo.m_gram;
/* 49 */     this.m_globalFreq = inputInfo.m_globalFreq;
/* 50 */     this.m_occurrenceListIds = inputInfo.m_occurrenceListIds;
/*    */   }
/*    */ 
/*    */   public void init(String gram, long globalFreq, List<String> occurrenceListIds)
/*    */   {
/* 55 */     this.m_gram = gram;
/* 56 */     this.m_globalFreq = globalFreq;
/* 57 */     this.m_occurrenceListIds = occurrenceListIds;
/*    */   }
/*    */ 
/*    */   public String toString()
/*    */   {
/* 63 */     IdcStringBuilder gramInfoBuilder = new IdcStringBuilder();
/* 64 */     gramInfoBuilder.append(" Gram : " + this.m_gram);
/* 65 */     gramInfoBuilder.append(" Global Frequency : " + this.m_globalFreq);
/* 66 */     gramInfoBuilder.append(" Mapping List IDs : " + this.m_occurrenceListIds);
/* 67 */     return gramInfoBuilder.toString();
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 72 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98754 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.autosuggest.records.GramInfo
 * JD-Core Version:    0.5.4
 */