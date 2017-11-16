/*    */ package intradoc.common;
/*    */ 
/*    */ public class ResourceObject
/*    */ {
/*    */   public int m_type;
/*    */   public String m_name;
/*    */   public Object m_resource;
/*    */ 
/*    */   public ResourceObject()
/*    */   {
/* 30 */     this.m_type = 0;
/* 31 */     this.m_name = null;
/* 32 */     this.m_resource = null;
/*    */   }
/*    */ 
/*    */   public String toString()
/*    */   {
/* 38 */     IdcStringBuilder result = new IdcStringBuilder();
/*    */ 
/* 40 */     result.append("m_type: " + ResourceContainer.RESOURCE_TYPES[this.m_type]);
/* 41 */     result.append("\nm_name: " + this.m_name);
/* 42 */     result.append("\nm_resource: " + this.m_resource);
/*    */ 
/* 44 */     return result.toString();
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 50 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.ResourceObject
 * JD-Core Version:    0.5.4
 */