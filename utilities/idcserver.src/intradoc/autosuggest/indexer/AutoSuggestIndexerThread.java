/*    */ package intradoc.autosuggest.indexer;
/*    */ 
/*    */ import intradoc.autosuggest.AutoSuggestContext;
/*    */ import intradoc.autosuggest.AutoSuggestManager;
/*    */ import intradoc.common.LocaleUtils;
/*    */ import intradoc.common.Report;
/*    */ import intradoc.data.DataBinder;
/*    */ import intradoc.data.Workspace;
/*    */ import intradoc.server.Service;
/*    */ 
/*    */ public class AutoSuggestIndexerThread
/*    */   implements Runnable
/*    */ {
/*    */   public AutoSuggestContext m_context;
/*    */   public boolean m_isRebuild;
/*    */ 
/*    */   public AutoSuggestIndexerThread(AutoSuggestContext context, boolean isRebuild)
/*    */   {
/* 35 */     this.m_context = context;
/* 36 */     this.m_isRebuild = isRebuild;
/*    */   }
/*    */ 
/*    */   public void run()
/*    */   {
/*    */     try
/*    */     {
/* 45 */       boolean isLocked = this.m_context.lock();
/* 46 */       if (isLocked == true)
/*    */       {
/* 51 */         AutoSuggestManager manager = new AutoSuggestManager(this.m_context);
/* 52 */         manager.index(this.m_isRebuild);
/* 53 */         this.m_context.m_service.getBinder().putLocal("StatusCode." + this.m_context.m_contextKey, Integer.toString(0));
/* 54 */         this.m_context.release();
/*    */       }
/*    */       else
/*    */       {
/* 61 */         Report.trace("autosuggest", "Could not acquire lock for indexing on context " + this.m_context.m_contextKey, null);
/*    */       }
/*    */     }
/*    */     catch (Exception e)
/*    */     {
/* 66 */       String errorMsg = LocaleUtils.encodeMessage("csAutoSuggestIndexingFailed", null, this.m_context.m_contextKey);
/* 67 */       Report.error("autosuggest", errorMsg, e);
/* 68 */       this.m_context.release();
/*    */     }
/*    */     finally
/*    */     {
/* 72 */       this.m_context.m_workspace.releaseConnection();
/*    */     }
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg) {
/* 77 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98887 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.autosuggest.indexer.AutoSuggestIndexerThread
 * JD-Core Version:    0.5.4
 */