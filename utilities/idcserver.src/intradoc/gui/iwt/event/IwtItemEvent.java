/*    */ package intradoc.gui.iwt.event;
/*    */ 
/*    */ import java.awt.ItemSelectable;
/*    */ import java.awt.event.ItemEvent;
/*    */ 
/*    */ public class IwtItemEvent extends ItemEvent
/*    */ {
/* 29 */   public static int FINAL_ITEM_EVENT = 1;
/* 30 */   public int m_flags = 0;
/*    */ 
/*    */   public IwtItemEvent(ItemSelectable list, int type, Object item, int state)
/*    */   {
/* 34 */     super(list, type, item, state);
/*    */   }
/*    */ 
/*    */   public IwtItemEvent(ItemSelectable list, int type, Object item, int state, int flags)
/*    */   {
/* 40 */     super(list, type, item, state);
/* 41 */     this.m_flags = flags;
/*    */   }
/*    */ 
/*    */   public boolean checkFlag(int flag)
/*    */   {
/* 46 */     return (this.m_flags & flag) == flag;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 51 */     return "releaseInfo=dev,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.gui.iwt.event.IwtItemEvent
 * JD-Core Version:    0.5.4
 */