/*     */ package intradoc.autosuggest.datastore;
/*     */ 
/*     */ import intradoc.autosuggest.AutoSuggestContext;
/*     */ import intradoc.autosuggest.records.SecurityIdentifierInfo;
/*     */ import intradoc.autosuggest.utils.QueueRemoveIterator;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataException;
/*     */ import java.io.Serializable;
/*     */ import java.util.Iterator;
/*     */ import java.util.Map;
/*     */ import java.util.Queue;
/*     */ import java.util.concurrent.ConcurrentLinkedQueue;
/*     */ 
/*     */ public class AutoSuggestIdentifiersQueue extends CacheStorage
/*     */ {
/*     */   public AutoSuggestContext m_context;
/*     */   public Queue<SecurityIdentifierInfo> m_addQueue;
/*     */   public Queue<SecurityIdentifierInfo> m_deleteQueue;
/*     */ 
/*     */   public AutoSuggestIdentifiersQueue(AutoSuggestContext context)
/*     */     throws ServiceException, DataException
/*     */   {
/*  47 */     super(context.m_activeIndex, context.m_contextKey + ":AutoSuggestIdentifiersQueue");
/*  48 */     this.m_context = context;
/*  49 */     this.m_addQueue = get("AddQueue");
/*  50 */     if (this.m_addQueue == null)
/*     */     {
/*  52 */       this.m_addQueue = new ConcurrentLinkedQueue();
/*     */     }
/*  54 */     this.m_deleteQueue = get("DeleteQueue");
/*  55 */     if (this.m_deleteQueue != null)
/*     */       return;
/*  57 */     this.m_deleteQueue = new ConcurrentLinkedQueue();
/*     */   }
/*     */ 
/*     */   public void put(String queueType, Queue<SecurityIdentifierInfo> queue) throws DataException
/*     */   {
/*  62 */     super.put(queueType, (Serializable)queue);
/*     */   }
/*     */ 
/*     */   public void update(String queueType, Queue<SecurityIdentifierInfo> queue) throws DataException {
/*  66 */     super.update(queueType, (Serializable)queue);
/*     */   }
/*     */ 
/*     */   public void commit() throws DataException {
/*  70 */     put("AddQueue", this.m_addQueue);
/*  71 */     put("DeleteQueue", this.m_deleteQueue);
/*     */   }
/*     */ 
/*     */   public Queue<SecurityIdentifierInfo> get(String queueType) throws DataException
/*     */   {
/*  76 */     Queue queue = (Queue)super.get(queueType);
/*  77 */     return queue;
/*     */   }
/*     */ 
/*     */   public void add(String identifier, String securityGroupId, String accountId, String owner, String users, String groups, String roles, Map<String, String> extraParameters)
/*     */     throws DataException
/*     */   {
/*  87 */     SecurityIdentifierInfo securityIdentifierInfo = new SecurityIdentifierInfo(this.m_context);
/*  88 */     securityIdentifierInfo.init(identifier, securityGroupId, accountId, owner, users, groups, roles, extraParameters);
/*  89 */     this.m_addQueue.add(securityIdentifierInfo);
/*  90 */     put("AddQueue", this.m_addQueue);
/*     */   }
/*     */ 
/*     */   public void delete(String identifier, String securityGroupId, String accountId, String owner, String users, String groups, String roles, Map<String, String> extraParameters) throws DataException
/*     */   {
/*  95 */     SecurityIdentifierInfo securityIdentifierInfo = new SecurityIdentifierInfo(this.m_context);
/*  96 */     securityIdentifierInfo.init(identifier, securityGroupId, accountId, owner, users, groups, roles, extraParameters);
/*  97 */     this.m_deleteQueue.add(securityIdentifierInfo);
/*  98 */     put("DeleteQueue", this.m_deleteQueue);
/*     */   }
/*     */ 
/*     */   public Iterator<SecurityIdentifierInfo> additionRemoveIterator()
/*     */   {
/* 108 */     return new QueueRemoveIterator(this.m_addQueue);
/*     */   }
/*     */ 
/*     */   public Iterator<SecurityIdentifierInfo> deletionRemoveIterator() {
/* 112 */     return new QueueRemoveIterator(this.m_deleteQueue);
/*     */   }
/*     */ 
/*     */   public Iterator<SecurityIdentifierInfo> additionIterator()
/*     */   {
/* 120 */     return this.m_addQueue.iterator();
/*     */   }
/*     */ 
/*     */   public Iterator<SecurityIdentifierInfo> deletionIterator() {
/* 124 */     return this.m_deleteQueue.iterator();
/*     */   }
/*     */ 
/*     */   public boolean isEmpty() {
/* 128 */     return (this.m_addQueue.isEmpty()) && (this.m_deleteQueue.isEmpty());
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg) {
/* 132 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99650 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.autosuggest.datastore.AutoSuggestIdentifiersQueue
 * JD-Core Version:    0.5.4
 */