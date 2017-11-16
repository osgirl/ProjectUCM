/*    */ package intradoc.shared;
/*    */ 
/*    */ import intradoc.common.IdcCharArrayWriter;
/*    */ import intradoc.common.LocaleResources;
/*    */ import intradoc.common.LocaleUtils;
/*    */ import intradoc.common.Report;
/*    */ import intradoc.data.DataBinder;
/*    */ import intradoc.data.DataResultSet;
/*    */ import intradoc.util.IdcVector;
/*    */ import java.util.Vector;
/*    */ 
/*    */ public class ProfileUtils
/*    */ {
/* 33 */   public static final String[] EDIT_COLUMNS = { "topicName", "topicEditAction", "topicKey", "topicValue" };
/*    */ 
/*    */   public static void addTopicEdit(String topic, String action, String key, DataBinder valueBinder, DataBinder binder)
/*    */   {
/* 39 */     DataResultSet topicEdits = (DataResultSet)binder.getResultSet("UserTopicEdits");
/* 40 */     if (topicEdits == null)
/*    */     {
/* 42 */       topicEdits = new DataResultSet(EDIT_COLUMNS);
/* 43 */       binder.addResultSet("UserTopicEdits", topicEdits);
/*    */     }
/* 45 */     Vector row = new IdcVector();
/* 46 */     row.addElement(topic);
/* 47 */     row.addElement(action);
/* 48 */     if (key == null)
/*    */     {
/* 50 */       row.addElement("");
/*    */     }
/*    */     else
/*    */     {
/* 54 */       row.addElement(key);
/*    */     }
/*    */ 
/*    */     try
/*    */     {
/* 59 */       String val = "";
/* 60 */       if (valueBinder != null)
/*    */       {
/* 62 */         IdcCharArrayWriter sw = new IdcCharArrayWriter();
/* 63 */         valueBinder.sendEx(sw, false);
/* 64 */         val = sw.toStringRelease();
/*    */       }
/* 66 */       row.addElement(val);
/*    */     }
/*    */     catch (Exception e)
/*    */     {
/* 70 */       String msg = LocaleUtils.encodeMessage("apUnableToStreamTopicBinder", e.getMessage());
/*    */ 
/* 72 */       Report.trace(null, LocaleResources.localizeMessage(msg, null), e);
/*    */     }
/* 74 */     topicEdits.addRow(row);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 79 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 84156 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.ProfileUtils
 * JD-Core Version:    0.5.4
 */