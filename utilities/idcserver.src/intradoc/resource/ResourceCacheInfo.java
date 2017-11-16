/*     */ package intradoc.resource;
/*     */ 
/*     */ import intradoc.common.IdcAppendable;
/*     */ import intradoc.common.IdcDebugOutput;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.StringUtils;
/*     */ import java.util.Date;
/*     */ import java.util.List;
/*     */ 
/*     */ public class ResourceCacheInfo
/*     */   implements IdcDebugOutput
/*     */ {
/*  40 */   public String m_lookupKey = null;
/*     */ 
/*  45 */   public String m_type = null;
/*     */ 
/*  50 */   public String m_filePath = null;
/*     */ 
/*  55 */   public Object m_resourceObj = null;
/*     */ 
/*  60 */   public Object m_associatedInfo = null;
/*     */ 
/*  65 */   public long m_lastLoaded = 0L;
/*     */ 
/*  70 */   public long m_lastExternalLoadTime = 0L;
/*     */ 
/*  76 */   public long m_agedTS = 0L;
/*     */ 
/*  81 */   public long m_removalTS = 0L;
/*     */ 
/*  87 */   public long m_size = 0L;
/*     */ 
/*  93 */   public String m_languageId = null;
/*     */ 
/*  98 */   public boolean m_hasDynamicResource = false;
/*  99 */   public boolean m_hasDynamicString = false;
/*     */ 
/* 104 */   public List m_resourceList = null;
/*     */ 
/* 109 */   public ResourceCacheInfo m_prev = null;
/*     */ 
/* 114 */   public ResourceCacheInfo m_next = null;
/*     */ 
/*     */   public ResourceCacheInfo()
/*     */   {
/*     */   }
/*     */ 
/*     */   public ResourceCacheInfo(String key)
/*     */   {
/* 129 */     this.m_lookupKey = key;
/*     */   }
/*     */ 
/*     */   public ResourceCacheInfo(String key, String type, String filePath)
/*     */   {
/* 137 */     this.m_lookupKey = key;
/* 138 */     this.m_filePath = filePath;
/*     */   }
/*     */ 
/*     */   public void copy(ResourceCacheInfo info)
/*     */   {
/* 146 */     this.m_lookupKey = info.m_lookupKey;
/* 147 */     this.m_type = info.m_type;
/* 148 */     this.m_filePath = info.m_filePath;
/* 149 */     this.m_resourceObj = info.m_resourceObj;
/* 150 */     this.m_associatedInfo = info.m_associatedInfo;
/* 151 */     this.m_lastLoaded = info.m_lastLoaded;
/* 152 */     this.m_agedTS = info.m_agedTS;
/* 153 */     this.m_removalTS = info.m_removalTS;
/* 154 */     this.m_size = info.m_size;
/* 155 */     this.m_hasDynamicResource = info.m_hasDynamicResource;
/* 156 */     this.m_hasDynamicString = info.m_hasDynamicString;
/* 157 */     this.m_resourceList = info.m_resourceList;
/*     */   }
/*     */ 
/*     */   public void detach()
/*     */   {
/* 165 */     if (this.m_prev != null)
/*     */     {
/* 167 */       this.m_prev.m_next = this.m_next;
/*     */     }
/* 169 */     if (this.m_next != null)
/*     */     {
/* 171 */       this.m_next.m_prev = this.m_prev;
/*     */     }
/* 173 */     this.m_prev = null;
/* 174 */     this.m_next = null;
/*     */   }
/*     */ 
/*     */   public void insertBeforeUs(ResourceCacheInfo info)
/*     */   {
/* 183 */     if (info == null)
/*     */     {
/* 185 */       return;
/*     */     }
/*     */ 
/* 188 */     if (this.m_prev != null)
/*     */     {
/* 190 */       this.m_prev.m_next = info;
/*     */     }
/* 192 */     info.m_prev = this.m_prev;
/* 193 */     this.m_prev = info;
/* 194 */     info.m_next = this;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 199 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 104244 $";
/*     */   }
/*     */ 
/*     */   public void appendDebugFormat(IdcAppendable appendable)
/*     */   {
/* 208 */     appendable.append(this.m_lookupKey);
/* 209 */     appendable.append(" (");
/* 210 */     StringUtils.appendDebugProperty(appendable, "size", "" + this.m_size, false);
/* 211 */     if (this.m_filePath != null)
/*     */     {
/* 213 */       StringUtils.appendDebugProperty(appendable, "filePath", this.m_filePath, true);
/*     */     }
/* 215 */     if (this.m_type != null)
/*     */     {
/* 217 */       StringUtils.appendDebugProperty(appendable, "type", this.m_type, true);
/*     */     }
/* 219 */     if (this.m_languageId != null)
/*     */     {
/* 221 */       StringUtils.appendDebugProperty(appendable, "languageId", this.m_languageId, true);
/*     */     }
/* 223 */     StringUtils.appendDebugProperty(appendable, "lastloaded", new Date(this.m_lastLoaded), true);
/* 224 */     StringUtils.appendDebugProperty(appendable, "agedTS", new Date(this.m_agedTS), true);
/* 225 */     StringUtils.appendDebugProperty(appendable, "removalTS", new Date(this.m_removalTS), true);
/* 226 */     if (this.m_associatedInfo != null)
/*     */     {
/* 228 */       StringUtils.appendDebugProperty(appendable, "associatedInfo", this.m_associatedInfo, true);
/*     */     }
/* 230 */     if (this.m_hasDynamicResource)
/*     */     {
/* 232 */       StringUtils.appendDebugProperty(appendable, "hasDynamicResource", "" + this.m_hasDynamicResource, true);
/*     */     }
/* 234 */     if (this.m_hasDynamicString)
/*     */     {
/* 236 */       StringUtils.appendDebugProperty(appendable, "hasDynamicString", "" + this.m_hasDynamicString, true);
/*     */     }
/* 238 */     if (this.m_resourceList != null)
/*     */     {
/* 240 */       StringUtils.appendDebugProperty(appendable, "resourceList.size()", "" + this.m_resourceList.size(), true);
/*     */     }
/* 242 */     appendable.append(")");
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 251 */     IdcStringBuilder output = new IdcStringBuilder();
/* 252 */     appendDebugFormat(output);
/* 253 */     return output.toString();
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.resource.ResourceCacheInfo
 * JD-Core Version:    0.5.4
 */