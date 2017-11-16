/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.Report;
/*     */ import intradoc.shared.SecurityUtils;
/*     */ import intradoc.shared.UserData;
/*     */ import intradoc.shared.UserDocumentAccessFilter;
/*     */ import java.util.Hashtable;
/*     */ 
/*     */ public class UserStateCache
/*     */ {
/*  40 */   protected static Hashtable m_userReadStateCache = new Hashtable();
/*     */ 
/*     */   public static boolean checkUserChange(UserData curUser)
/*     */   {
/*  49 */     Object obj = m_userReadStateCache.get(curUser.m_name);
/*  50 */     boolean isChanged = obj == null;
/*     */ 
/*  53 */     UserDocumentAccessFilter readFilter = null;
/*     */     try
/*     */     {
/*  56 */       readFilter = SecurityUtils.getUserDocumentAccessFilter(curUser, 1);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/*  61 */       Report.trace(null, null, e);
/*  62 */       return false;
/*     */     }
/*  64 */     if ((readFilter != null) && (!isChanged))
/*     */     {
/*  66 */       UserDocumentAccessFilter oldFilter = (UserDocumentAccessFilter)obj;
/*  67 */       isChanged = !readFilter.isEqualSecurity(oldFilter);
/*     */     }
/*  69 */     if (isChanged)
/*     */     {
/*  71 */       m_userReadStateCache.put(curUser.m_name, readFilter);
/*     */     }
/*  73 */     return isChanged;
/*     */   }
/*     */ 
/*     */   public static UserDocumentAccessFilter getReadState(UserData curUser)
/*     */   {
/*  81 */     Object obj = m_userReadStateCache.get(curUser.m_name);
/*  82 */     if (obj == null)
/*     */     {
/*  84 */       return null;
/*     */     }
/*  86 */     return (UserDocumentAccessFilter)obj;
/*     */   }
/*     */ 
/*     */   public static void putReadState(UserData curUser, UserDocumentAccessFilter readFilter)
/*     */   {
/*  94 */     m_userReadStateCache.put(curUser.m_name, readFilter);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 100 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.UserStateCache
 * JD-Core Version:    0.5.4
 */