/*    */ package intradoc.server.subject;
/*    */ 
/*    */ import intradoc.common.LocaleUtils;
/*    */ import intradoc.common.ServiceException;
/*    */ import intradoc.common.StringUtils;
/*    */ import intradoc.data.DataException;
/*    */ import intradoc.data.DataResultSet;
/*    */ import intradoc.data.ResultSetUtils;
/*    */ import intradoc.server.SubjectCallbackAdapter;
/*    */ import intradoc.shared.AdditionalRenditions;
/*    */ import intradoc.shared.SharedObjects;
/*    */ import java.util.Vector;
/*    */ 
/*    */ public class RenditionsSubjectCallback extends SubjectCallbackAdapter
/*    */ {
/*    */   public void refresh(String subject)
/*    */     throws DataException, ServiceException
/*    */   {
/* 39 */     DataResultSet drset = SharedObjects.getTable("AdditionalRenditionsSource");
/* 40 */     if (drset == null)
/*    */     {
/* 42 */       String msg = LocaleUtils.encodeMessage("csUnableToLoadTable", null, "AdditionalRenditions");
/*    */ 
/* 44 */       throw new DataException(msg);
/*    */     }
/*    */ 
/* 47 */     String allowableRenditions = SharedObjects.getEnvironmentValue("AllowableAdditionalRenditions");
/* 48 */     String[] allowableList = null;
/* 49 */     if (allowableRenditions != null)
/*    */     {
/* 51 */       Vector v = StringUtils.parseArray(allowableRenditions, ',', ',');
/* 52 */       allowableList = StringUtils.convertListToArray(v);
/*    */     }
/* 54 */     AdditionalRenditions renSet = new AdditionalRenditions();
/* 55 */     renSet.loadEx(drset, allowableList);
/* 56 */     SharedObjects.putTable("AdditionalRenditions", renSet);
/* 57 */     for (renSet.first(); renSet.isRowPresent(); renSet.next())
/*    */     {
/* 61 */       String productionStep = ResultSetUtils.getValue(renSet, "renProductionStep");
/* 62 */       if (productionStep.length() <= 0)
/*    */         continue;
/* 64 */       String key = "RenditionIsActive:" + productionStep;
/*    */ 
/* 68 */       String existingVal = SharedObjects.getEnvironmentValue(key);
/* 69 */       if (existingVal != null)
/*    */         continue;
/* 71 */       SharedObjects.putEnvironmentValue(key, "1");
/*    */     }
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 79 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.subject.RenditionsSubjectCallback
 * JD-Core Version:    0.5.4
 */