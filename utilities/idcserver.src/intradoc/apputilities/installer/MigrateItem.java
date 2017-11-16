/*     */ package intradoc.apputilities.installer;
/*     */ 
/*     */ import java.lang.reflect.Method;
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class MigrateItem
/*     */ {
/*     */   public MigrationEnvironment m_environment;
/*     */   public String m_id;
/*     */   public String m_version;
/*     */   public List<Method> m_initMethods;
/*     */   public List<Method> m_workMethods;
/*     */   public Map<String, String> m_params;
/*     */   public String m_name;
/*     */   public String m_spec;
/*     */   public int m_progressUnits;
/*     */   public int m_currentProgress;
/*     */ 
/*     */   @Deprecated
/*     */   public List<String> m_displayList;
/*     */   public List<String> m_detailsList;
/*     */   public List<String> m_summaryList;
/*     */   public String m_initScript;
/*     */   public Map m_other;
/*     */ 
/*     */   public MigrateItem(MigrationEnvironment env, String id)
/*     */   {
/* 138 */     this(env, id, null, null, null);
/*     */   }
/*     */ 
/*     */   public MigrateItem(MigrationEnvironment env, String id, List<Method> initMethods, List<Method> workMethods, Map<String, String> params)
/*     */   {
/* 144 */     this.m_environment = env;
/* 145 */     this.m_id = id;
/* 146 */     this.m_initMethods = ((initMethods != null) ? initMethods : new ArrayList());
/* 147 */     this.m_workMethods = ((workMethods != null) ? workMethods : new ArrayList());
/* 148 */     this.m_params = ((params != null) ? params : new HashMap());
/*     */ 
/* 150 */     this.m_displayList = new ArrayList();
/* 151 */     this.m_detailsList = new ArrayList();
/* 152 */     this.m_summaryList = new ArrayList();
/* 153 */     this.m_other = new HashMap();
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 159 */     StringBuilder sb = new StringBuilder("MigrateItem: ");
/* 160 */     sb.append(this.m_id);
/* 161 */     if ((this.m_version != null) && (this.m_version.length() > 0))
/*     */     {
/* 163 */       sb.append(" v");
/* 164 */       sb.append(this.m_version);
/*     */     }
/* 166 */     if ((this.m_spec != null) && (this.m_spec.length() > 0) && (!this.m_spec.equals(this.m_id)))
/*     */     {
/* 168 */       sb.append(", ");
/* 169 */       sb.append(this.m_spec);
/*     */     }
/* 171 */     sb.append(" (");
/* 172 */     sb.append(Integer.toString(this.m_progressUnits));
/* 173 */     sb.append(')');
/* 174 */     if ((this.m_detailsList != null) && (this.m_detailsList.size() > 0))
/*     */     {
/* 176 */       sb.append(':');
/* 177 */       for (String display : this.m_detailsList)
/*     */       {
/* 179 */         sb.append('\n');
/* 180 */         sb.append(display);
/*     */       }
/*     */     }
/* 183 */     return sb.toString();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 188 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 82713 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.installer.MigrateItem
 * JD-Core Version:    0.5.4
 */