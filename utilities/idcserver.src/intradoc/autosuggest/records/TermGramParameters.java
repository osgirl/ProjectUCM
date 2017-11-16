/*    */ package intradoc.autosuggest.records;
/*    */ 
/*    */ public class TermGramParameters
/*    */ {
/*    */   public short m_frequency;
/*    */   public short m_position;
/*    */   public double m_inverseDocFreq;
/*    */   public double m_weight;
/*    */ 
/*    */   public TermGramParameters(short freq, short loc)
/*    */   {
/* 32 */     this.m_frequency = freq;
/* 33 */     this.m_position = loc;
/*    */   }
/*    */ 
/*    */   public void incrementFrequency() {
/* 37 */     this.m_frequency = (short)(this.m_frequency + 1);
/*    */   }
/*    */ 
/*    */   public void prepare(GramInfo gramInfo, long termCount)
/*    */   {
/* 49 */     this.m_inverseDocFreq = Math.log(termCount / gramInfo.m_globalFreq);
/* 50 */     this.m_weight = (this.m_inverseDocFreq * this.m_frequency);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg) {
/* 54 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98808 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.autosuggest.records.TermGramParameters
 * JD-Core Version:    0.5.4
 */