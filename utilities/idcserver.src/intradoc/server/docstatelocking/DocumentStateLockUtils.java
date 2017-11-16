/*     */ package intradoc.server.docstatelocking;
/*     */ 
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.server.Service;
/*     */ import intradoc.shared.SharedObjects;
/*     */ 
/*     */ public class DocumentStateLockUtils
/*     */ {
/*  29 */   static boolean m_isInit = false;
/*  30 */   static boolean m_useCommentesForLockingParameters = false;
/*  31 */   static boolean m_disableDocumentLockChecks = false;
/*  32 */   static boolean[] m_syncObject = { true };
/*     */ 
/*     */   public static void checkInit()
/*     */   {
/*  36 */     if (m_isInit)
/*     */       return;
/*  38 */     synchronized (m_syncObject)
/*     */     {
/*  40 */       if (!m_isInit)
/*     */       {
/*  42 */         m_useCommentesForLockingParameters = SharedObjects.getEnvValueAsBoolean("UseCommentsFieldForLockingParameters", false);
/*     */ 
/*  44 */         m_disableDocumentLockChecks = SharedObjects.getEnvValueAsBoolean("DisableDocumentLockChecks", false);
/*     */ 
/*  46 */         m_isInit = true;
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static boolean isLockedDocument(DataBinder binder, Service service) throws DataException, ServiceException
/*     */   {
/*  53 */     checkInit();
/*  54 */     if (m_disableDocumentLockChecks)
/*     */     {
/*  56 */       return false;
/*     */     }
/*  58 */     boolean isLocked = testForDebugCondition("locked", binder);
/*  59 */     service.executeFilter("isStateLockedDocument");
/*  60 */     String v = binder.getEnvironmentValue("IsLocked");
/*  61 */     if (!isLocked)
/*     */     {
/*  63 */       isLocked = StringUtils.convertToBool(v, false);
/*     */     }
/*  65 */     if (isLocked)
/*     */     {
/*  67 */       Report.trace("documentlock", "The document " + binder.get("dDocName") + " is locked", null);
/*     */     }
/*  69 */     return isLocked;
/*     */   }
/*     */ 
/*     */   public static boolean isDoingUnlock(DataBinder binder, Service service) throws DataException, ServiceException
/*     */   {
/*  74 */     checkInit();
/*  75 */     if (m_disableDocumentLockChecks)
/*     */     {
/*  77 */       return false;
/*     */     }
/*  79 */     boolean isUnlock = testForDebugCondition("unlock", binder);
/*  80 */     service.executeFilter("isStateUnlockedDocument");
/*  81 */     if (!isUnlock)
/*     */     {
/*  83 */       isUnlock = service.isConditionVarTrue("IsUnlock");
/*     */     }
/*  85 */     if (isUnlock)
/*     */     {
/*  87 */       Report.trace("documentlock", "Document " + binder.get("dDocName") + " is being unlocked", null);
/*     */     }
/*  89 */     return isUnlock;
/*     */   }
/*     */ 
/*     */   public static boolean isSimplifiedCheckin(DataBinder binder, Service service) throws DataException, ServiceException
/*     */   {
/*  94 */     checkInit();
/*  95 */     if (m_disableDocumentLockChecks)
/*     */     {
/*  97 */       return false;
/*     */     }
/*     */ 
/* 101 */     service.setReturnValue("");
/* 102 */     service.executeFilter("isSimplifiedCheckin");
/* 103 */     Object o = service.getReturnValue();
/* 104 */     if (o instanceof Boolean)
/*     */     {
/* 106 */       Boolean b = (Boolean)o;
/* 107 */       return b.booleanValue();
/*     */     }
/*     */ 
/* 110 */     String httpUserAgent = binder.getEnvironmentValue("HTTP_USER_AGENT");
/* 111 */     String serviceName = binder.getLocal("IdcService");
/* 112 */     String requestMethod = binder.getEnvironmentValue("REQUEST_METHOD");
/* 113 */     boolean isSimpleCheckin = false;
/* 114 */     if ((requestMethod != null) && (((requestMethod.equals("PUT")) || (requestMethod.equals("COPY")))))
/*     */     {
/* 116 */       isSimpleCheckin = true;
/* 117 */       SystemUtils.trace("documentlock", "Unlocking document because of " + requestMethod + " of " + binder.get("dDocName"));
/*     */     }
/* 124 */     else if (("Intradoc Client".equals(httpUserAgent)) && ("CHECKIN_UNIVERSAL".equals(serviceName)))
/*     */     {
/* 126 */       isSimpleCheckin = true;
/* 127 */       SystemUtils.trace("documentlock", "Unlocking document because of " + httpUserAgent + " and " + serviceName + " of " + binder.get("dDocName"));
/*     */     }
/* 129 */     return isSimpleCheckin;
/*     */   }
/*     */ 
/*     */   public static boolean mustWaitRealUnlock(DataBinder binder, Service service)
/*     */     throws DataException, ServiceException
/*     */   {
/* 135 */     checkInit();
/* 136 */     if (m_disableDocumentLockChecks)
/*     */     {
/* 138 */       return false;
/*     */     }
/* 140 */     service.setReturnValue("");
/* 141 */     service.executeFilter("isDoubleLocked");
/* 142 */     Object o = service.getReturnValue();
/* 143 */     if (o instanceof Boolean)
/*     */     {
/* 145 */       Boolean b = (Boolean)o;
/* 146 */       return b.booleanValue();
/*     */     }
/* 148 */     boolean isDoubleLocked = testForDebugCondition("waitunlock", binder);
/* 149 */     return isDoubleLocked;
/*     */   }
/*     */ 
/*     */   protected static boolean testForDebugCondition(String condition, DataBinder binder)
/*     */   {
/* 154 */     if (m_useCommentesForLockingParameters)
/*     */     {
/* 156 */       String comments = binder.getAllowMissing("xComments");
/* 157 */       if ((comments != null) && (comments.indexOf(condition) >= 0))
/*     */       {
/* 159 */         Report.trace("documentlock", "Custom locking condition " + condition + " matched " + comments, null);
/* 160 */         return true;
/*     */       }
/*     */     }
/* 163 */     return false;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 169 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98670 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.docstatelocking.DocumentStateLockUtils
 * JD-Core Version:    0.5.4
 */