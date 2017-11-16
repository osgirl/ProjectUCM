/*    */ package intradoc.autosuggest.utils;
/*    */ 
/*    */ import intradoc.autosuggest.records.SecurityIdentifierInfo;
/*    */ import intradoc.autosuggest.records.TermInfo;
/*    */ 
/*    */ public class ResultTermInfo
/*    */ {
/*    */   public TermInfo m_termInfo;
/*    */   public SecurityIdentifierInfo m_securityIdentifierInfo;
/*    */   public double m_score;
/*    */   public double m_proximityIndex;
/*    */ 
/*    */   public ResultTermInfo(TermInfo termInfo, SecurityIdentifierInfo securityIdentifierInfo, double score, double distance)
/*    */   {
/* 33 */     this.m_termInfo = termInfo;
/* 34 */     this.m_securityIdentifierInfo = securityIdentifierInfo;
/* 35 */     this.m_score = score;
/* 36 */     this.m_proximityIndex = distance;
/*    */   }
/*    */ 
/*    */   public String toString()
/*    */   {
/* 42 */     return this.m_termInfo.toString() + " " + this.m_score + " " + this.m_proximityIndex;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 47 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99650 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.autosuggest.utils.ResultTermInfo
 * JD-Core Version:    0.5.4
 */