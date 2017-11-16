/*    */ package intradoc.autosuggest.utils;
/*    */ 
/*    */ import intradoc.autosuggest.records.TermGramParameters;
/*    */ import java.util.HashMap;
/*    */ import java.util.Map;
/*    */ 
/*    */ public class GramParameterConstructor
/*    */ {
/*    */   public int m_minimumGramLength;
/*    */   public int m_maximumGramLength;
/*    */ 
/*    */   public GramParameterConstructor()
/*    */   {
/* 31 */     this.m_minimumGramLength = AutoSuggestUtils.getMinimumGramLength();
/* 32 */     this.m_maximumGramLength = AutoSuggestUtils.getMaximumGramLength();
/*    */   }
/*    */ 
/*    */   public GramParameterConstructor(short minimumGramLength, short maximumGramLength) {
/* 36 */     this.m_minimumGramLength = minimumGramLength;
/* 37 */     this.m_maximumGramLength = maximumGramLength;
/*    */   }
/*    */ 
/*    */   public Map<String, TermGramParameters> contructGramParameters(String input)
/*    */   {
/* 47 */     Map gramMap = new HashMap();
/* 48 */     for (int gramLength = this.m_minimumGramLength; gramLength <= this.m_maximumGramLength; ++gramLength)
/*    */     {
/* 50 */       contructGramParameters(gramMap, input, gramLength);
/*    */     }
/* 52 */     return gramMap;
/*    */   }
/*    */ 
/*    */   public Map<String, TermGramParameters> contructGramParameters(Map<String, TermGramParameters> gramMap, String input, int gramLength) {
/* 56 */     for (short index = 0; index <= input.length() - gramLength; index = (short)(index + 1))
/*    */     {
/* 58 */       String gram = input.substring(index, index + gramLength);
/* 59 */       TermGramParameters gramParams = (TermGramParameters)gramMap.get(gram);
/* 60 */       if (gramParams == null)
/*    */       {
/* 62 */         gramParams = new TermGramParameters(0, index);
/*    */       }
/* 64 */       gramParams.incrementFrequency();
/* 65 */       gramMap.put(gram, gramParams);
/*    */     }
/* 67 */     return gramMap;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg) {
/* 71 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 101634 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.autosuggest.utils.GramParameterConstructor
 * JD-Core Version:    0.5.4
 */