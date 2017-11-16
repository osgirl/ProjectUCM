/*    */ package intradoc.gui;
/*    */ 
/*    */ import intradoc.data.FieldInfo;
/*    */ import java.util.Vector;
/*    */ 
/*    */ public abstract class DisplayStringCallbackAdaptor
/*    */   implements DisplayStringCallback
/*    */ {
/*    */   public abstract String createDisplayString(FieldInfo paramFieldInfo, String paramString1, String paramString2, Vector paramVector);
/*    */ 
/*    */   public String createExtendedDisplayString(FieldInfo finfo, String name, String value, Vector row)
/*    */   {
/* 35 */     return null;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 40 */     return "releaseInfo=dev,releaseRevision=$Rev: 78444 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.gui.DisplayStringCallbackAdaptor
 * JD-Core Version:    0.5.4
 */