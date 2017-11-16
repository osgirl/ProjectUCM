/*     */ package intradoc.server.publish;
/*     */ 
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import java.util.Set;
/*     */ 
/*     */ public class PublishedResourceContainer
/*     */   implements Comparable<PublishedResourceContainer>
/*     */ {
/*     */   public String m_name;
/*     */   public String[] m_sortedResourcePaths;
/*     */   public List<PublishedResource> m_resources;
/*     */   public Set<String> m_providedFeatures;
/*     */   public Set<String> m_requiredFeatures;
/*     */   public Set<String> m_optionalFeatures;
/*     */ 
/*     */   public String toString()
/*     */   {
/* 117 */     return this.m_name;
/*     */   }
/*     */ 
/*     */   public int compareTo(PublishedResourceContainer c)
/*     */   {
/* 122 */     return this.m_name.compareTo(c.m_name);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 128 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 75690 $";
/*     */   }
/*     */ 
/*     */   public static class Class extends PublishedResourceContainer
/*     */   {
/*     */     public PublishedResourceContainer.Bundle m_bundle;
/*     */ 
/*     */     public Class(String name)
/*     */     {
/*  83 */       this.m_name = name;
/*  84 */       this.m_resources = new ArrayList();
/*     */     }
/*     */   }
/*     */ 
/*     */   public static class Bundle extends PublishedResourceContainer
/*     */   {
/*     */     public Filter[] m_sortedFilters;
/*     */     public String m_path;
/*     */ 
/*     */     public Bundle(String bundlePath)
/*     */     {
/*  66 */       this.m_sortedResourcePaths = new String[1];
/*  67 */       this.m_sortedResourcePaths[0] = bundlePath;
/*  68 */       this.m_path = bundlePath;
/*  69 */       this.m_name = ("bundle:" + bundlePath);
/*     */     }
/*     */ 
/*     */     public static class Filter
/*     */     {
/*     */       public boolean m_isExclusive;
/*     */       public String m_classname;
/*     */ 
/*     */       public Filter(boolean isExclusive, String classname)
/*     */       {
/*  44 */         this.m_isExclusive = isExclusive;
/*  45 */         this.m_classname = classname;
/*     */       }
/*     */ 
/*     */       public String toString()
/*     */       {
/*  51 */         if (this.m_isExclusive)
/*     */         {
/*  53 */           return "!" + this.m_classname;
/*     */         }
/*  55 */         return this.m_classname;
/*     */       }
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.publish.PublishedResourceContainer
 * JD-Core Version:    0.5.4
 */