/*    */ package intradoc.gui;
/*    */ 
/*    */ import intradoc.util.IdcMessage;
/*    */ import java.awt.event.ActionEvent;
/*    */ 
/*    */ public abstract class DialogCallback
/*    */ {
/*    */   public int m_dialogID;
/*    */   public DialogHelper m_dlgHelper;
/*    */   public int m_promptResult;
/*    */   public IdcMessage m_errorMessage;
/*    */ 
/*    */   public DialogCallback()
/*    */   {
/* 34 */     this.m_errorMessage = null;
/*    */   }
/*    */ 
/*    */   public abstract boolean handleDialogEvent(ActionEvent paramActionEvent);
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg) {
/* 40 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.gui.DialogCallback
 * JD-Core Version:    0.5.4
 */