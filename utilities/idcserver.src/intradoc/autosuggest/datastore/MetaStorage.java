/*    */ package intradoc.autosuggest.datastore;
/*    */ 
/*    */ import intradoc.autosuggest.AutoSuggestConstants;
/*    */ import intradoc.autosuggest.AutoSuggestContext;
/*    */ import intradoc.autosuggest.records.MetaInfo;
/*    */ import intradoc.common.ServiceException;
/*    */ import intradoc.data.DataException;
/*    */ 
/*    */ public class MetaStorage extends CacheStorage
/*    */ {
/*    */   public AutoSuggestContext m_context;
/*    */ 
/*    */   public MetaStorage(AutoSuggestContext context)
/*    */     throws DataException, ServiceException
/*    */   {
/* 37 */     super(AutoSuggestConstants.AUTO_SUGGEST_META, context.m_contextKey);
/*    */   }
/*    */ 
/*    */   public void put(String field, MetaInfo metaInfo) throws DataException {
/* 41 */     super.put(field, metaInfo);
/*    */   }
/*    */ 
/*    */   public void update(String field, MetaInfo metaInfo) throws DataException {
/* 45 */     super.update(field, metaInfo);
/*    */   }
/*    */ 
/*    */   public MetaInfo get(String field) throws DataException
/*    */   {
/* 50 */     MetaInfo metaInfo = (MetaInfo)super.get(field);
/* 51 */     return metaInfo;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg) {
/* 55 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99259 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.autosuggest.datastore.MetaStorage
 * JD-Core Version:    0.5.4
 */