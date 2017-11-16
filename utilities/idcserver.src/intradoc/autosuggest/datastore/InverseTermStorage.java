/*    */ package intradoc.autosuggest.datastore;
/*    */ 
/*    */ import intradoc.autosuggest.AutoSuggestContext;
/*    */ import intradoc.autosuggest.records.TermInfo;
/*    */ import intradoc.common.ServiceException;
/*    */ import intradoc.data.DataException;
/*    */ 
/*    */ public class InverseTermStorage extends CacheStorage
/*    */ {
/*    */   public AutoSuggestContext m_context;
/*    */ 
/*    */   public InverseTermStorage(AutoSuggestContext context)
/*    */     throws DataException, ServiceException
/*    */   {
/* 34 */     super(context.m_activeIndex, context.m_contextKey + ":InverseTermStorage");
/* 35 */     this.m_context = context;
/*    */   }
/*    */ 
/*    */   public void put(String identifier, TermInfo termInfo) throws DataException {
/* 39 */     super.put(identifier, termInfo);
/*    */   }
/*    */ 
/*    */   public void update(String identifier, TermInfo termInfo) throws DataException {
/* 43 */     super.update(identifier, termInfo);
/*    */   }
/*    */ 
/*    */   public TermInfo get(String identifier) throws DataException
/*    */   {
/* 48 */     return (TermInfo)super.get(identifier);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg) {
/* 52 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99259 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.autosuggest.datastore.InverseTermStorage
 * JD-Core Version:    0.5.4
 */