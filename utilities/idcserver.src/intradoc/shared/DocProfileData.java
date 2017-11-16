/*    */ package intradoc.shared;
/*    */ 
/*    */ import intradoc.data.DataBinder;
/*    */ 
/*    */ public class DocProfileData
/*    */ {
/*    */   protected String m_name;
/*    */   protected String m_lookupKey;
/*    */   protected DataBinder m_binder;
/*    */   protected long m_lastLoadedTs;
/*    */   protected String m_idColumn;
/*    */ 
/*    */   public DocProfileData(String idColumn)
/*    */   {
/* 36 */     this.m_lastLoadedTs = -2L;
/* 37 */     this.m_idColumn = idColumn;
/*    */   }
/*    */ 
/*    */   public void init(DataBinder binder, long timestamp)
/*    */   {
/* 42 */     this.m_name = binder.getLocal(this.m_idColumn);
/* 43 */     this.m_binder = binder;
/* 44 */     this.m_lastLoadedTs = timestamp;
/*    */   }
/*    */ 
/*    */   public String getName()
/*    */   {
/* 49 */     return this.m_name;
/*    */   }
/*    */ 
/*    */   public DataBinder getData()
/*    */   {
/* 54 */     return this.m_binder.createShallowCopyCloneResultSets();
/*    */   }
/*    */ 
/*    */   public long getLastLoadedTs()
/*    */   {
/* 59 */     return this.m_lastLoadedTs;
/*    */   }
/*    */ 
/*    */   public String getValue(String key)
/*    */   {
/* 64 */     return this.m_binder.getLocal(key);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 69 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.DocProfileData
 * JD-Core Version:    0.5.4
 */