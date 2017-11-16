/*     */ package intradoc.filestore;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.server.Service;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.util.HashMap;
/*     */ import java.util.Iterator;
/*     */ import java.util.Map;
/*     */ import java.util.Set;
/*     */ 
/*     */ public class IdcDescriptorStateImplementor
/*     */   implements IdcDescriptorState
/*     */ {
/*  31 */   protected boolean m_needsUpdate = false;
/*  32 */   protected Map m_updatedKeys = null;
/*     */ 
/*     */   public IdcDescriptorStateImplementor()
/*     */   {
/*  36 */     this.m_updatedKeys = new HashMap();
/*     */   }
/*     */ 
/*     */   public void updateMetaData(Map args, ExecutionContext cxt)
/*     */   {
/*  41 */     if (!cxt instanceof Service)
/*     */       return;
/*  43 */     Service service = (Service)cxt;
/*  44 */     DataBinder binder = service.getBinder();
/*  45 */     Set keys = args.keySet();
/*  46 */     for (Iterator iter = keys.iterator(); iter.hasNext(); )
/*     */     {
/*  48 */       String key = (String)iter.next();
/*  49 */       String val = (String)args.get(key);
/*     */ 
/*  51 */       String curVal = binder.getAllowMissing(key);
/*  52 */       if ((curVal == null) || (!curVal.equals(val)))
/*     */       {
/*  54 */         this.m_needsUpdate = true;
/*  55 */         binder.putLocal(key, val);
/*  56 */         this.m_updatedKeys.put(key, "update");
/*     */       }
/*  58 */       else if (this.m_updatedKeys.get(key) == null)
/*     */       {
/*  60 */         this.m_updatedKeys.put(key, "set");
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void updateToWebless(ExecutionContext cxt)
/*     */     throws DataException
/*     */   {
/*  69 */     String clmn = getWebFlagColumn();
/*  70 */     if (!cxt instanceof Service)
/*     */       return;
/*  72 */     Service service = (Service)cxt;
/*  73 */     DataBinder binder = service.getBinder();
/*  74 */     binder.putLocal(clmn, "N");
/*  75 */     if (!this.m_needsUpdate)
/*     */     {
/*  77 */       Workspace ws = service.getWorkspace();
/*     */ 
/*  79 */       String dID = binder.getLocal("dID");
/*  80 */       String sql = "UPDATE DocMeta SET " + clmn + "='N' WHERE dID=" + dID;
/*  81 */       ws.executeSQL(sql);
/*     */     }
/*  83 */     this.m_updatedKeys.put(clmn, "update");
/*     */   }
/*     */ 
/*     */   public void clearWebless(ExecutionContext cxt)
/*     */   {
/*  89 */     String clmn = getWebFlagColumn();
/*  90 */     FileStoreUtils.updateMetaData(clmn, "", cxt);
/*     */   }
/*     */ 
/*     */   public void finishUpdate(DataBinder binder, Workspace ws)
/*     */     throws DataException
/*     */   {
/*  96 */     if (!this.m_needsUpdate)
/*     */       return;
/*  98 */     ws.execute("Umeta", binder);
/*  99 */     this.m_needsUpdate = false;
/* 100 */     this.m_updatedKeys = new HashMap();
/*     */   }
/*     */ 
/*     */   public boolean isUpdatedMetaData(String key)
/*     */   {
/* 106 */     Object obj = this.m_updatedKeys.get(key);
/* 107 */     return (obj != null) && (obj.equals("update"));
/*     */   }
/*     */ 
/*     */   public boolean isSetMetaData(String key)
/*     */   {
/* 112 */     Object obj = this.m_updatedKeys.get(key);
/* 113 */     return obj != null;
/*     */   }
/*     */ 
/*     */   protected String getWebFlagColumn()
/*     */   {
/* 118 */     String clmn = SharedObjects.getEnvironmentValue("WebFlagColumn");
/* 119 */     if (clmn == null)
/*     */     {
/* 121 */       clmn = "xWebFlag";
/*     */     }
/* 123 */     return clmn;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 128 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 81995 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.filestore.IdcDescriptorStateImplementor
 * JD-Core Version:    0.5.4
 */