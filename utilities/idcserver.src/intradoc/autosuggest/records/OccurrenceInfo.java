/*    */ package intradoc.autosuggest.records;
/*    */ 
/*    */ import intradoc.autosuggest.AutoSuggestContext;
/*    */ import intradoc.common.IdcStringBuilder;
/*    */ import java.io.Serializable;
/*    */ 
/*    */ public class OccurrenceInfo
/*    */   implements Serializable
/*    */ {
/*    */   private static final long serialVersionUID = 6838510060078551861L;
/*    */   public transient AutoSuggestContext m_context;
/*    */   public String m_gramId;
/*    */   public String m_termId;
/*    */   public short m_position;
/*    */   public short m_frequency;
/*    */ 
/*    */   public OccurrenceInfo(AutoSuggestContext context)
/*    */   {
/* 42 */     this.m_context = context;
/*    */   }
/*    */ 
/*    */   public OccurrenceInfo(OccurrenceInfo inputInfo) {
/* 46 */     this.m_context = inputInfo.m_context;
/* 47 */     this.m_gramId = inputInfo.m_gramId;
/* 48 */     this.m_termId = inputInfo.m_termId;
/* 49 */     this.m_position = inputInfo.m_position;
/* 50 */     this.m_frequency = inputInfo.m_frequency;
/*    */   }
/*    */ 
/*    */   public void init(String gramId, String termId, short position, short frequency) {
/* 54 */     this.m_gramId = gramId;
/* 55 */     this.m_termId = termId;
/* 56 */     this.m_position = position;
/* 57 */     this.m_frequency = frequency;
/*    */   }
/*    */ 
/*    */   public String toString()
/*    */   {
/* 62 */     IdcStringBuilder occurrenceInfoBuilder = new IdcStringBuilder();
/* 63 */     occurrenceInfoBuilder.append(" Gram ID: " + this.m_gramId);
/* 64 */     occurrenceInfoBuilder.append(" Term ID : " + this.m_termId);
/* 65 */     occurrenceInfoBuilder.append(" Position : " + this.m_position);
/* 66 */     occurrenceInfoBuilder.append(" Frequency : " + this.m_frequency);
/* 67 */     return occurrenceInfoBuilder.toString();
/*    */   }
/*    */ 
/*    */   public OccurrenceInfo clone()
/*    */   {
/* 74 */     OccurrenceInfo clonedOccurrenceInfo = new OccurrenceInfo(this);
/* 75 */     return clonedOccurrenceInfo;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg) {
/* 79 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 104206 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.autosuggest.records.OccurrenceInfo
 * JD-Core Version:    0.5.4
 */