/*     */ package intradoc.server.utils;
/*     */ 
/*     */ import intradoc.common.IdcAppendable;
/*     */ import intradoc.common.IdcDebugOutput;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.StringUtils;
/*     */ import java.util.Date;
/*     */ 
/*     */ public class DocInfoCacheAssociatedInfo
/*     */   implements IdcDebugOutput
/*     */ {
/*     */   public boolean m_usingFileTimestamps;
/*     */   public boolean m_useNativeForPath;
/*     */   public String m_vaultPath;
/*     */   public long m_vaultPathTimestamp;
/*     */   public String m_webPath;
/*     */   public long m_webPathTimestamp;
/*     */   public String m_dID;
/*     */   public String m_dDocName;
/*     */   public int m_searchCacheAge;
/*     */ 
/*     */   public void appendDebugFormat(IdcAppendable appendable)
/*     */   {
/*  88 */     appendable.append(this.m_dDocName);
/*  89 */     appendable.append(" (");
/*  90 */     StringUtils.appendDebugProperty(appendable, "dID", "" + this.m_dID, false);
/*  91 */     StringUtils.appendDebugProperty(appendable, "useNativeForPath", "" + this.m_useNativeForPath, true);
/*  92 */     if (this.m_useNativeForPath)
/*     */     {
/*  94 */       StringUtils.appendDebugProperty(appendable, "vaultPath", this.m_vaultPath, true);
/*  95 */       StringUtils.appendDebugProperty(appendable, "vaultPathTimestamp", new Date(this.m_vaultPathTimestamp), true);
/*  96 */       StringUtils.appendDebugProperty(appendable, "webPath", this.m_webPath, true);
/*  97 */       StringUtils.appendDebugProperty(appendable, "webPathTimestamp", new Date(this.m_webPathTimestamp), true);
/*     */     }
/*     */     else
/*     */     {
/* 101 */       StringUtils.appendDebugProperty(appendable, "searchCacheAge", "" + this.m_searchCacheAge, true);
/*     */     }
/* 103 */     appendable.append(")");
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 113 */     IdcStringBuilder output = new IdcStringBuilder();
/* 114 */     appendDebugFormat(output);
/* 115 */     return output.toString();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 121 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 81312 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.utils.DocInfoCacheAssociatedInfo
 * JD-Core Version:    0.5.4
 */