/*    */ package intradoc.autosuggest.records;
/*    */ 
/*    */ import intradoc.autosuggest.AutoSuggestContext;
/*    */ import intradoc.common.IdcStringBuilder;
/*    */ import java.io.Serializable;
/*    */ import java.util.Map;
/*    */ 
/*    */ public class TermInfo
/*    */   implements Serializable
/*    */ {
/*    */   private static final long serialVersionUID = 8484439252009287494L;
/*    */   public transient AutoSuggestContext m_context;
/*    */   public String m_identifier;
/*    */   public String m_indexedTerm;
/*    */   public String m_actualTerm;
/*    */   public Map<String, String> m_extraParameters;
/*    */ 
/*    */   public TermInfo(AutoSuggestContext context)
/*    */   {
/* 41 */     this.m_context = context;
/*    */   }
/*    */ 
/*    */   public TermInfo(TermInfo inputInfo) {
/* 45 */     this.m_context = inputInfo.m_context;
/* 46 */     this.m_indexedTerm = inputInfo.m_indexedTerm;
/* 47 */     this.m_identifier = inputInfo.m_identifier;
/* 48 */     this.m_extraParameters = inputInfo.m_extraParameters;
/*    */   }
/*    */ 
/*    */   public void init(String term, String documentId, Map<String, String> extraParameters) {
/* 52 */     this.m_indexedTerm = term.toLowerCase();
/* 53 */     this.m_actualTerm = term;
/* 54 */     this.m_identifier = documentId;
/* 55 */     this.m_extraParameters = extraParameters;
/*    */   }
/*    */ 
/*    */   public String getKey()
/*    */   {
/* 63 */     return this.m_identifier + "." + this.m_indexedTerm;
/*    */   }
/*    */ 
/*    */   public boolean hasSecurity()
/*    */   {
/* 71 */     return !this.m_identifier.equals("-1");
/*    */   }
/*    */ 
/*    */   public String toString()
/*    */   {
/* 76 */     IdcStringBuilder termInfoBuilder = new IdcStringBuilder();
/* 77 */     termInfoBuilder.append(" Term : " + this.m_indexedTerm);
/* 78 */     termInfoBuilder.append(" Identifier Info : " + this.m_identifier);
/* 79 */     return termInfoBuilder.toString();
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg) {
/* 83 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99650 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.autosuggest.records.TermInfo
 * JD-Core Version:    0.5.4
 */