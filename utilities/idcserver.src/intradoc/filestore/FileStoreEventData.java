/*    */ package intradoc.filestore;
/*    */ 
/*    */ import java.util.Map;
/*    */ 
/*    */ public class FileStoreEventData
/*    */ {
/*    */   public IdcFileDescriptor m_descriptor;
/*    */   public Map m_eventArgs;
/*    */ 
/*    */   public FileStoreEventData()
/*    */   {
/* 45 */     this.m_descriptor = null;
/* 46 */     this.m_eventArgs = null;
/*    */   }
/*    */ 
/*    */   public FileStoreEventData(IdcFileDescriptor descriptor, Map eventArgs)
/*    */   {
/* 54 */     this.m_descriptor = descriptor;
/* 55 */     this.m_eventArgs = eventArgs;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 60 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.filestore.FileStoreEventData
 * JD-Core Version:    0.5.4
 */