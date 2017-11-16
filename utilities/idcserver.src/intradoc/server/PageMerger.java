/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.HtmlChunk;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.shared.SharedPageMerger;
/*     */ import java.io.Writer;
/*     */ 
/*     */ public class PageMerger extends SharedPageMerger
/*     */ {
/*  33 */   IDebugInterface m_debugInterface = null;
/*     */ 
/*     */   public PageMerger()
/*     */   {
/*     */   }
/*     */ 
/*     */   public PageMerger(DataBinder binder, ExecutionContext cxt)
/*     */   {
/*  49 */     super(binder, cxt);
/*     */   }
/*     */ 
/*     */   public void initImplementProtectContext(DataBinder binder, ExecutionContext cxt)
/*     */     throws ServiceException
/*     */   {
/*  65 */     if (cxt instanceof Service)
/*     */     {
/*  67 */       Service s = (Service)cxt;
/*  68 */       ExecutionContext newContext = s.createProtectedContextShallowClone();
/*  69 */       initImplement(binder, newContext);
/*     */     }
/*     */     else
/*     */     {
/*  73 */       super.initImplementProtectContext(binder, cxt);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setActiveBinder(DataBinder binder)
/*     */   {
/*  80 */     if (this.m_cxt instanceof Service)
/*     */     {
/*  82 */       Service s = (Service)this.m_cxt;
/*  83 */       s.setBinder(binder);
/*     */     }
/*  85 */     super.setActiveBinder(binder);
/*     */   }
/*     */ 
/*     */   public void doBreakpoint(HtmlChunk chunk)
/*     */   {
/*  91 */     super.doBreakpoint(chunk);
/*     */     try
/*     */     {
/*  95 */       if (this.m_debugInterface == null)
/*     */       {
/*  97 */         Class clazz = Class.forName("com.stellent.dgidc.DebugPresentation");
/*  98 */         this.m_debugInterface = ((IDebugInterface)clazz.newInstance());
/*     */       }
/* 100 */       this.m_cxt.setCachedObject("CurrentStackDepth", "" + this.m_curStackDepth);
/* 101 */       this.m_cxt.setCachedObject("ScriptStack", this.m_scriptStack);
/* 102 */       this.m_debugInterface.presentBreakpoint(chunk, this);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 106 */       Report.trace("idcdebug", null, e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public String createMergedPage(String page)
/*     */     throws DataException, ServiceException
/*     */   {
/* 120 */     DataLoader.checkCachedPage(page, this.m_cxt);
/* 121 */     return super.createMergedPage(page);
/*     */   }
/*     */ 
/*     */   public Writer writeMergedPage(Writer w, String page)
/*     */     throws DataException, ServiceException
/*     */   {
/* 128 */     DataLoader.checkCachedPage(page, this.m_cxt);
/* 129 */     return super.writeMergedPage(w, page);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 134 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 72682 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.PageMerger
 * JD-Core Version:    0.5.4
 */