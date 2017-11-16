/*    */ package intradoc.autosuggest.utils;
/*    */ 
/*    */ import intradoc.autosuggest.records.OccurrenceInfo;
/*    */ import intradoc.autosuggest.records.TermGramParameters;
/*    */ import intradoc.common.Report;
/*    */ import intradoc.common.ServiceException;
/*    */ import intradoc.data.DataException;
/*    */ import intradoc.shared.SharedObjects;
/*    */ import java.util.Map;
/*    */ 
/*    */ public class DefaultOccurrenceFilter
/*    */   implements OccurrenceFilter
/*    */ {
/*    */   protected short m_positionExpected;
/*    */   protected short m_frequencyExpected;
/*    */   public short m_positionExpectDelta;
/*    */   public short m_frequencyExpectDelta;
/*    */ 
/*    */   public DefaultOccurrenceFilter()
/*    */   {
/* 35 */     this.m_positionExpectDelta = 3;
/* 36 */     this.m_frequencyExpectDelta = 2;
/*    */   }
/*    */ 
/*    */   public void init(Map<String, Object> parameters) throws DataException, ServiceException {
/* 40 */     TermGramParameters gramParams = (TermGramParameters)parameters.get("TermGramParameters");
/* 41 */     this.m_positionExpected = gramParams.m_position;
/* 42 */     this.m_frequencyExpected = gramParams.m_frequency;
/*    */   }
/*    */ 
/*    */   public boolean validate(OccurrenceInfo occurrenceInfo)
/*    */   {
/* 52 */     boolean enableDefaultFilter = SharedObjects.getEnvValueAsBoolean("EnableAutoSuggestDefaultFilter", false);
/* 53 */     if (!enableDefaultFilter)
/*    */     {
/* 55 */       return true;
/*    */     }
/* 57 */     boolean isValid = true;
/* 58 */     if (occurrenceInfo == null)
/*    */     {
/* 60 */       return false;
/*    */     }
/* 62 */     isValid &= Math.abs(occurrenceInfo.m_position - this.m_positionExpected) <= this.m_positionExpectDelta;
/* 63 */     if (!isValid)
/*    */     {
/* 65 */       if (Report.m_verbose)
/*    */       {
/* 67 */         Report.trace("autosuggest", "Filter[Default Occurrence] - " + isValid + " For Occurrence info - " + occurrenceInfo.toString(), null);
/*    */       }
/* 69 */       return isValid;
/*    */     }
/* 71 */     isValid &= occurrenceInfo.m_frequency - this.m_frequencyExpected <= this.m_frequencyExpectDelta;
/* 72 */     if (Report.m_verbose)
/*    */     {
/* 74 */       Report.trace("autosuggest", "Filter[Default Occurrence] - " + isValid + " For Occurrence info - " + occurrenceInfo.toString(), null);
/*    */     }
/* 76 */     return isValid;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 81 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 101631 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.autosuggest.utils.DefaultOccurrenceFilter
 * JD-Core Version:    0.5.4
 */