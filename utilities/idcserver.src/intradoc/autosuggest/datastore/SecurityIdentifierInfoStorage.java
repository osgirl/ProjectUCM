/*    */ package intradoc.autosuggest.datastore;
/*    */ 
/*    */ import intradoc.autosuggest.AutoSuggestContext;
/*    */ import intradoc.autosuggest.records.SecurityIdentifierInfo;
/*    */ import intradoc.common.ServiceException;
/*    */ import intradoc.data.DataException;
/*    */ 
/*    */ public class SecurityIdentifierInfoStorage extends CacheStorage
/*    */ {
/*    */   public AutoSuggestContext m_context;
/*    */ 
/*    */   public SecurityIdentifierInfoStorage(AutoSuggestContext context)
/*    */     throws DataException, ServiceException
/*    */   {
/* 33 */     super(context.m_activeIndex, context.m_contextKey + ":SecurityIdentifierInfoStorage");
/* 34 */     this.m_context = context;
/*    */   }
/*    */ 
/*    */   public void put(String identifier, SecurityIdentifierInfo securityIdentifierInfo) throws DataException {
/* 38 */     super.put(identifier, securityIdentifierInfo);
/*    */   }
/*    */ 
/*    */   public void update(String identifier, SecurityIdentifierInfo securityIdentifierInfo) throws DataException {
/* 42 */     super.update(identifier, securityIdentifierInfo);
/*    */   }
/*    */ 
/*    */   public SecurityIdentifierInfo get(String identifier) throws DataException
/*    */   {
/* 47 */     SecurityIdentifierInfo securityIdentifierInfo = (SecurityIdentifierInfo)super.get(identifier);
/* 48 */     return securityIdentifierInfo;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg) {
/* 52 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99259 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.autosuggest.datastore.SecurityIdentifierInfoStorage
 * JD-Core Version:    0.5.4
 */