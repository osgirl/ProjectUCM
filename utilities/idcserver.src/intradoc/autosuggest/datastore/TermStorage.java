/*    */ package intradoc.autosuggest.datastore;
/*    */ 
/*    */ import intradoc.autosuggest.AutoSuggestContext;
/*    */ import intradoc.autosuggest.records.TermInfo;
/*    */ import intradoc.common.ServiceException;
/*    */ import intradoc.data.DataException;
/*    */ 
/*    */ public class TermStorage extends CacheStorage
/*    */ {
/*    */   public AutoSuggestContext m_context;
/*    */ 
/*    */   public TermStorage(AutoSuggestContext context)
/*    */     throws DataException, ServiceException
/*    */   {
/* 33 */     super(context.m_activeIndex, context.m_contextKey + ":TermStorage");
/* 34 */     this.m_context = context;
/*    */   }
/*    */ 
/*    */   public void put(String term, TermInfo termInfo) throws DataException {
/* 38 */     super.put(term, termInfo);
/*    */   }
/*    */ 
/*    */   public void update(String term, TermInfo termInfo) throws DataException {
/* 42 */     super.update(term, termInfo);
/*    */   }
/*    */ 
/*    */   public TermInfo get(String term) throws DataException
/*    */   {
/* 47 */     return (TermInfo)super.get(term);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg) {
/* 51 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99259 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.autosuggest.datastore.TermStorage
 * JD-Core Version:    0.5.4
 */