/*    */ package intradoc.autosuggest.datastore;
/*    */ 
/*    */ import intradoc.autosuggest.AutoSuggestContext;
/*    */ import intradoc.autosuggest.records.OccurrenceInfo;
/*    */ import intradoc.common.Report;
/*    */ import intradoc.common.ServiceException;
/*    */ import intradoc.data.DataException;
/*    */ import java.io.Serializable;
/*    */ import java.util.ArrayList;
/*    */ import java.util.List;
/*    */ 
/*    */ public class OccurrenceStorage extends CacheStorage
/*    */ {
/*    */   public AutoSuggestContext m_context;
/*    */ 
/*    */   public OccurrenceStorage(AutoSuggestContext context)
/*    */     throws ServiceException, DataException
/*    */   {
/* 39 */     super(context.m_activeIndex, context.m_contextKey + ":OccurrenceStorage");
/* 40 */     this.m_context = context;
/*    */   }
/*    */ 
/*    */   public synchronized void put(String id, OccurrenceInfo info) throws DataException {
/* 44 */     List occurrenceList = get(id);
/* 45 */     if (occurrenceList == null)
/*    */     {
/* 47 */       if (Report.m_verbose)
/*    */       {
/* 49 */         Report.trace("autosuggest", "Inserting new bucket for occurrence info with id " + id, null);
/*    */       }
/* 51 */       occurrenceList = new ArrayList();
/* 52 */       occurrenceList.add(info);
/* 53 */       put(id, occurrenceList);
/*    */     }
/*    */     else
/*    */     {
/* 57 */       if (Report.m_verbose)
/*    */       {
/* 59 */         Report.trace("autosuggest", "Updating existing bucket for occurrence info with id " + id, null);
/*    */       }
/* 61 */       synchronized (occurrenceList)
/*    */       {
/* 63 */         occurrenceList.add(info);
/* 64 */         update(id, occurrenceList);
/*    */       }
/*    */     }
/*    */   }
/*    */ 
/*    */   public void put(String id, List<OccurrenceInfo> info) throws DataException {
/* 70 */     super.put(id, (Serializable)info);
/*    */   }
/*    */ 
/*    */   public void update(String id, List<OccurrenceInfo> info) throws DataException {
/* 74 */     super.update(id, (Serializable)info);
/*    */   }
/*    */ 
/*    */   public List<OccurrenceInfo> get(String id) throws DataException
/*    */   {
/* 79 */     List occurrenceList = (List)super.get(id);
/* 80 */     return occurrenceList;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg) {
/* 84 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 104132 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.autosuggest.datastore.OccurrenceStorage
 * JD-Core Version:    0.5.4
 */