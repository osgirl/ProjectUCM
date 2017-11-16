/*    */ package intradoc.autosuggest.datastore;
/*    */ 
/*    */ import intradoc.autosuggest.AutoSuggestContext;
/*    */ import intradoc.autosuggest.records.GramInfo;
/*    */ import intradoc.common.ServiceException;
/*    */ import intradoc.data.DataException;
/*    */ 
/*    */ public class GramStorage extends CacheStorage
/*    */ {
/*    */   public AutoSuggestContext m_context;
/*    */ 
/*    */   public GramStorage(AutoSuggestContext context)
/*    */     throws DataException, ServiceException
/*    */   {
/* 32 */     super(context.m_activeIndex, context.m_contextKey + ":GramStorage");
/* 33 */     this.m_context = context;
/*    */   }
/*    */ 
/*    */   public void put(String gram, GramInfo gramInfo) throws DataException {
/* 37 */     super.put(gram, gramInfo);
/*    */   }
/*    */ 
/*    */   public void update(String gram, GramInfo gramInfo) throws DataException {
/* 41 */     super.update(gram, gramInfo);
/*    */   }
/*    */ 
/*    */   public GramInfo get(String gram) throws DataException
/*    */   {
/* 46 */     GramInfo gramInfo = (GramInfo)super.get(gram);
/* 47 */     return gramInfo;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg) {
/* 51 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99259 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.autosuggest.datastore.GramStorage
 * JD-Core Version:    0.5.4
 */