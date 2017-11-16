/*    */ package intradoc.util;
/*    */ 
/*    */ import java.util.Map;
/*    */ 
/*    */ public class BasicIdcMessageContainer
/*    */   implements IdcMessageContainer
/*    */ {
/*    */   protected IdcMessage m_message;
/*    */   protected Map m_attributes;
/*    */ 
/*    */   public BasicIdcMessageContainer()
/*    */   {
/*    */   }
/*    */ 
/*    */   public BasicIdcMessageContainer(IdcMessage msg)
/*    */   {
/* 34 */     this.m_message = msg;
/*    */   }
/*    */ 
/*    */   public BasicIdcMessageContainer(IdcMessage msg, Map attributes)
/*    */   {
/* 39 */     this.m_message = msg;
/* 40 */     this.m_attributes = attributes;
/*    */   }
/*    */ 
/*    */   public IdcMessage getIdcMessage()
/*    */   {
/* 45 */     return this.m_message;
/*    */   }
/*    */ 
/*    */   public void setIdcMessage(IdcMessage msg)
/*    */   {
/* 50 */     this.m_message = msg;
/*    */   }
/*    */ 
/*    */   public IdcMessage appendAssociatedMessages(IdcMessage msg)
/*    */   {
/* 55 */     return msg;
/*    */   }
/*    */ 
/*    */   public Object getContainerAttribute(String arg)
/*    */   {
/* 60 */     if (this.m_attributes == null)
/*    */     {
/* 62 */       return null;
/*    */     }
/* 64 */     return this.m_attributes.get(arg);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 69 */     return "releaseInfo=dev,releaseRevision=$Rev: 76452 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.util.BasicIdcMessageContainer
 * JD-Core Version:    0.5.4
 */