/*    */ package intradoc.server.publish;
/*    */ 
/*    */ import intradoc.data.DataBinder;
/*    */ 
/*    */ public class PublishedResource
/*    */ {
/*    */   public PublishedResourceContainer.Class m_class;
/*    */   public String m_path;
/*    */   public String m_source;
/*    */   public String m_component;
/*    */   public int m_loadOrder;
/*    */   public boolean m_doPublish;
/*    */   public int m_publishType;
/*    */   public DataBinder m_binder;
/*    */ 
/*    */   public PublishedResource(PublishedResourceContainer.Class resourceClass, String path, int loadOrder, int type)
/*    */   {
/* 49 */     this.m_class = resourceClass;
/* 50 */     this.m_path = path;
/* 51 */     this.m_loadOrder = loadOrder;
/* 52 */     this.m_publishType = type;
/* 53 */     this.m_doPublish = true;
/*    */   }
/*    */ 
/*    */   public String toString()
/*    */   {
/* 59 */     return this.m_path;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 65 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 79278 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.publish.PublishedResource
 * JD-Core Version:    0.5.4
 */