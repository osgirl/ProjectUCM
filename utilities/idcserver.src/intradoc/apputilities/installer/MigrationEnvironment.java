/*     */ package intradoc.apputilities.installer;
/*     */ 
/*     */ import intradoc.common.DynamicHtmlMerger;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcDateFormat;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.ReportProgress;
/*     */ import intradoc.common.ReportSubProgress;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.server.IdcExtendedLoader;
/*     */ import intradoc.server.utils.ComponentListEditor;
/*     */ import intradoc.server.utils.SystemPropertiesEditor;
/*     */ import java.util.Calendar;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ 
/*     */ public class MigrationEnvironment
/*     */ {
/*     */   public String m_migrateType;
/*     */   public int m_migrateFlags;
/*     */   public Workspace m_workspace;
/*     */   public IdcExtendedLoader m_extendedLoader;
/*     */   public String m_dbType;
/*     */   public String m_jdbcConnectionString;
/*     */   public String m_jdbcDriver;
/*     */   public String m_jdbcUser;
/*     */   public String m_jdbcPassword;
/*     */   public String m_jdbcPasswordEncoding;
/*     */   public String m_targetHomeDir;
/*     */   public String m_sourceBinDir;
/*     */   public String m_targetDomainDir;
/*     */   public String m_targetBinDir;
/*     */   public String m_targetIntradocDir;
/*     */   public SystemPropertiesEditor m_sourceSysPropsEditor;
/*     */   public Properties m_sourceEnvironment;
/*     */   public ComponentListEditor m_sourceComponents;
/*     */   public String m_sourceIntradocDir;
/*     */   public SystemPropertiesEditor m_targetSysPropsEditor;
/*     */   public Properties m_targetEnvironment;
/*     */   public ComponentListEditor m_targetComponents;
/*     */   public String m_targetBackupDir;
/*     */   public String m_targetMigrateDir;
/*     */   public String m_targetMigrateStateFilename;
/*     */   public List<String> m_resourceFilenames;
/*     */   public DataBinder m_migrateState;
/*     */   public List<MigrateItem> m_items;
/*     */   public DataResultSet m_itemsTable;
/*     */   public int m_currentItemIndex;
/*     */   public int m_currentItemDisplayListIndex;
/*     */   public int m_currentProgressUnits;
/*     */   public int m_maximumProgressUnits;
/*     */   public ReportProgress m_reportProgress;
/*     */   public List<ReportSubProgress> m_reportSubProgresses;
/*     */   public DataBinder m_binder;
/*     */   public ExecutionContext m_context;
/*     */   public DynamicHtmlMerger m_merger;
/*     */   public Calendar m_startDate;
/*     */   public String m_startDateString;
/*     */   public String m_productName;
/*     */   public String m_specificProductName;
/*     */   public boolean m_tryRenameBackupFirst;
/*     */   public boolean m_tryDeleteAfterZip;
/*     */   public boolean m_shouldAbortIfDeleteAfterZipFails;
/*     */   public IdcDateFormat m_timestampFormat;
/*     */   public Map m_other;
/*     */ 
/*     */   public String toString()
/*     */   {
/* 113 */     int flags = this.m_migrateFlags;
/* 114 */     IdcStringBuilder str = new IdcStringBuilder();
/* 115 */     str.append((this.m_migrateType != null) ? this.m_migrateType : "Migrate");
/* 116 */     str.append(" for ");
/* 117 */     str.append((this.m_productName != null) ? this.m_productName.toUpperCase() : "?");
/* 118 */     str.append(" 0x");
/* 119 */     str.append(Integer.toHexString(flags));
/* 120 */     boolean skipDatabase = (flags & 0x400) == 0;
/* 121 */     boolean skipFilesystem = (flags & 0x800) == 0;
/* 122 */     if ((!skipDatabase) && (skipFilesystem))
/*     */     {
/* 124 */       str.append(" (DB only)");
/*     */     }
/* 126 */     else if ((skipDatabase) && (!skipFilesystem))
/*     */     {
/* 128 */       str.append(" (files only)");
/*     */     }
/* 130 */     if (this.m_startDateString != null)
/*     */     {
/* 132 */       str.append(" started on ");
/* 133 */       str.append(this.m_startDateString);
/*     */     }
/* 135 */     str.append(", environment:");
/* 136 */     if (this.m_sourceBinDir != null)
/*     */     {
/* 138 */       str.append("\n\tsource BinDir      = ");
/* 139 */       str.append(this.m_sourceBinDir);
/*     */     }
/* 141 */     if (this.m_sourceIntradocDir != null)
/*     */     {
/* 143 */       str.append("\n\tsource IntradocDir = ");
/* 144 */       str.append(this.m_sourceIntradocDir);
/*     */     }
/* 146 */     if (!skipFilesystem)
/*     */     {
/* 148 */       if (this.m_targetBinDir != null)
/*     */       {
/* 150 */         str.append("\n\ttarget BinDir      = ");
/* 151 */         str.append(this.m_targetBinDir);
/*     */       }
/* 153 */       if (this.m_targetIntradocDir != null)
/*     */       {
/* 155 */         str.append("\n\ttarget IntradocDir = ");
/* 156 */         str.append(this.m_targetIntradocDir);
/*     */       }
/* 158 */       if (this.m_targetBackupDir != null)
/*     */       {
/* 160 */         str.append("\n\tbackup to dir      = ");
/* 161 */         str.append(this.m_targetBackupDir);
/*     */       }
/* 163 */       if (this.m_targetMigrateDir != null)
/*     */       {
/* 165 */         str.append("\n\tmigrate dir        = ");
/* 166 */         str.append(this.m_targetMigrateDir);
/*     */       }
/* 168 */       if (this.m_targetMigrateStateFilename != null)
/*     */       {
/* 170 */         str.append("\n\tstate filename     = ");
/* 171 */         str.append(this.m_targetMigrateStateFilename);
/*     */       }
/*     */     }
/* 174 */     if (this.m_resourceFilenames != null)
/*     */     {
/* 176 */       str.append("\n\tload resources = [");
/* 177 */       int numResources = this.m_resourceFilenames.size();
/* 178 */       for (int r = 0; r < numResources; ++r)
/*     */       {
/* 180 */         if (r > 0)
/*     */         {
/* 182 */           str.append(", ");
/*     */         }
/* 184 */         str.append((String)this.m_resourceFilenames.get(r));
/*     */       }
/* 186 */       str.append(']');
/*     */     }
/* 188 */     if (!skipDatabase)
/*     */     {
/* 190 */       if (this.m_jdbcConnectionString != null)
/*     */       {
/* 192 */         str.append("\n\tJdbcConnectionString=");
/* 193 */         str.append(this.m_jdbcConnectionString);
/*     */       }
/* 195 */       if (this.m_jdbcDriver != null)
/*     */       {
/* 197 */         str.append("\n\tJdbcDriver=");
/* 198 */         str.append(this.m_jdbcDriver);
/*     */       }
/* 200 */       if (this.m_jdbcUser != null)
/*     */       {
/* 202 */         str.append("\n\tJdbcUser=");
/* 203 */         str.append(this.m_jdbcUser);
/*     */       }
/* 205 */       if (this.m_jdbcPassword != null)
/*     */       {
/* 207 */         str.append("\n\tJdbcPassword=");
/* 208 */         for (int i = this.m_jdbcPassword.length(); i > 0; --i)
/*     */         {
/* 210 */           str.append('*');
/*     */         }
/*     */       }
/* 213 */       if (this.m_jdbcPasswordEncoding != null)
/*     */       {
/* 215 */         str.append("\n\tJdbcPasswordEncoding=");
/* 216 */         str.append(this.m_jdbcPasswordEncoding);
/*     */       }
/*     */     }
/* 219 */     if (!skipFilesystem)
/*     */     {
/* 221 */       str.append("\n\ttry rename before zip? = ");
/* 222 */       str.append(Boolean.toString(this.m_tryRenameBackupFirst));
/* 223 */       str.append("\n\ttry delete after zip?  = ");
/* 224 */       str.append(Boolean.toString(this.m_tryDeleteAfterZip));
/* 225 */       str.append("\n\tabort if delete fails? = ");
/* 226 */       str.append(Boolean.toString(this.m_shouldAbortIfDeleteAfterZipFails));
/*     */     }
/* 228 */     if (this.m_maximumProgressUnits > 0)
/*     */     {
/* 230 */       str.append("\n\tprogress = ");
/* 231 */       str.append(this.m_currentProgressUnits);
/* 232 */       str.append('/');
/* 233 */       str.append(this.m_maximumProgressUnits);
/* 234 */       str.append(" (");
/* 235 */       int tenthpercent = 1000 * this.m_currentProgressUnits / this.m_maximumProgressUnits;
/* 236 */       str.append(Integer.toString(tenthpercent / 10));
/* 237 */       str.append('.');
/* 238 */       str.append(Integer.toString(tenthpercent % 10));
/* 239 */       str.append("%)");
/*     */     }
/* 241 */     return str.toString();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 246 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 93259 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.installer.MigrationEnvironment
 * JD-Core Version:    0.5.4
 */