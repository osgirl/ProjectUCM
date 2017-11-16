/*     */ package intradoc.autosuggest.datastore;
/*     */ 
/*     */ import intradoc.autosuggest.AutoSuggestContext;
/*     */ import intradoc.autosuggest.records.TermInfo;
/*     */ import intradoc.autosuggest.utils.QueueRemoveIterator;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataException;
/*     */ import java.io.Serializable;
/*     */ import java.util.Iterator;
/*     */ import java.util.Map;
/*     */ import java.util.Queue;
/*     */ import java.util.concurrent.ConcurrentLinkedQueue;
/*     */ 
/*     */ public class AutoSuggestTermsQueue extends CacheStorage
/*     */ {
/*     */   public AutoSuggestContext m_context;
/*     */   public Queue<TermInfo> m_addQueue;
/*     */   public Queue<TermInfo> m_deleteQueue;
/*     */ 
/*     */   public AutoSuggestTermsQueue(AutoSuggestContext context)
/*     */     throws ServiceException, DataException
/*     */   {
/*  47 */     super(context.m_activeIndex, context.m_contextKey + ":AutoSuggestValuesQueue");
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
/*     */   public void put(String queueType, Queue<TermInfo> queue) throws DataException
/*     */   {
/*  62 */     super.put(queueType, (Serializable)queue);
/*     */   }
/*     */ 
/*     */   public void update(String queueType, Queue<TermInfo> queue) throws DataException {
/*  66 */     super.update(queueType, (Serializable)queue);
/*     */   }
/*     */ 
/*     */   public Queue<TermInfo> get(String queueType) throws DataException
/*     */   {
/*  71 */     Queue queue = (Queue)super.get(queueType);
/*  72 */     return queue;
/*     */   }
/*     */ 
/*     */   public void add(String term, String identifier, Map<String, String> extraParameters)
/*     */     throws DataException
/*     */   {
/*  81 */     TermInfo termInfo = new TermInfo(this.m_context);
/*  82 */     termInfo.init(term, identifier, extraParameters);
/*  83 */     this.m_addQueue.add(termInfo);
/*  84 */     put("AddQueue", this.m_addQueue);
/*     */   }
/*     */ 
/*     */   public void delete(String term, String identifier, Map<String, String> extraParameters) throws DataException {
/*  88 */     TermInfo termInfo = new TermInfo(this.m_context);
/*  89 */     termInfo.init(term, identifier, extraParameters);
/*  90 */     this.m_deleteQueue.add(termInfo);
/*  91 */     put("DeleteQueue", this.m_deleteQueue);
/*     */   }
/*     */ 
/*     */   public void commit() throws DataException {
/*  95 */     put("AddQueue", this.m_addQueue);
/*  96 */     put("DeleteQueue", this.m_deleteQueue);
/*     */   }
/*     */ 
/*     */   public Iterator<TermInfo> additionRemoveIterator()
/*     */   {
/* 107 */     return new QueueRemoveIterator(this.m_addQueue);
/*     */   }
/*     */ 
/*     */   public Iterator<TermInfo> deletionRemoveIterator() {
/* 111 */     return new QueueRemoveIterator(this.m_deleteQueue);
/*     */   }
/*     */ 
/*     */   public Iterator<TermInfo> additionIterator()
/*     */   {
/* 119 */     return this.m_addQueue.iterator();
/*     */   }
/*     */ 
/*     */   public Iterator<TermInfo> deletionIterator() {
/* 123 */     return this.m_deleteQueue.iterator();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg) {
/* 127 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99650 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.autosuggest.datastore.AutoSuggestTermsQueue
 * JD-Core Version:    0.5.4
 */