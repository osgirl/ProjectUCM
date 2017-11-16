/*     */ package intradoc.autosuggest.records;
/*     */ 
/*     */ import intradoc.autosuggest.datastore.ContextInfoStorage;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.Workspace;
/*     */ import java.io.Serializable;
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashMap;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.Set;
/*     */ 
/*     */ public class ContextInfo
/*     */   implements Serializable
/*     */ {
/*     */   private static final long serialVersionUID = -6271871109709375886L;
/*     */   public String m_table;
/*     */   public String m_field;
/*     */   public String m_securityContext;
/*     */   public String m_initDataSource;
/*     */   public String m_addDataSource;
/*     */   public String m_deleteDataSource;
/*     */   public String m_initDataSourcePartitioner;
/*     */   public String m_addDataSourcePartitioner;
/*     */   public String m_deleteDataSourcePartitioner;
/*     */   public boolean m_isSecurityContext;
/*     */   public boolean m_isEnabled;
/*     */   public String m_extraParameters;
/*     */   public transient Workspace m_contextWorkspace;
/*     */   public transient ContextInfo m_securityContextInfo;
/*     */ 
/*     */   public void init(DataBinder binder)
/*     */   {
/*  61 */     init(binder.getLocalData());
/*     */   }
/*     */ 
/*     */   public void init(Properties contextProperties)
/*     */   {
/*  69 */     Map contextParams = new HashMap();
/*  70 */     Iterator paramIterator = contextProperties.keySet().iterator();
/*  71 */     while (paramIterator.hasNext())
/*     */     {
/*  73 */       String paramKey = (String)paramIterator.next();
/*  74 */       String paramValue = (String)contextProperties.get(paramKey);
/*  75 */       contextParams.put(paramKey, paramValue);
/*     */     }
/*  77 */     init(contextParams);
/*     */   }
/*     */ 
/*     */   public void init(Map<String, String> contextParams)
/*     */   {
/*  88 */     this.m_table = ((String)contextParams.get("table"));
/*  89 */     String dataSourcePrefix = this.m_table;
/*  90 */     if (this.m_table.startsWith("DMS"))
/*     */     {
/*  92 */       dataSourcePrefix = "Meta";
/*     */     }
/*  94 */     this.m_field = ((String)contextParams.get("field"));
/*  95 */     this.m_securityContext = ((contextParams.get("securityContext") != null) ? (String)contextParams.get("securityContext") : ContextInfoStorage.getDefaultSecurityContext(this.m_table));
/*  96 */     this.m_initDataSource = (dataSourcePrefix + "_AutoSuggest");
/*  97 */     this.m_addDataSource = (dataSourcePrefix + "_AutoSuggest");
/*  98 */     this.m_deleteDataSource = (dataSourcePrefix + "_AutoSuggest_Remove");
/*  99 */     this.m_initDataSourcePartitioner = ((contextParams.get("initDataSource.partitioner") != null) ? (String)contextParams.get("initDataSource.partitioner") : "");
/* 100 */     this.m_addDataSourcePartitioner = ((contextParams.get("addDataSource.partitioner") != null) ? (String)contextParams.get("addDataSource.partitioner") : "");
/* 101 */     this.m_deleteDataSourcePartitioner = ((contextParams.get("deleteDataSource.partitioner") != null) ? (String)contextParams.get("deleteDataSource.partitioner") : "");
/* 102 */     this.m_isSecurityContext = StringUtils.convertToBool((String)contextParams.get("isSecurityContext"), false);
/* 103 */     this.m_extraParameters = ((contextParams.get("extraParameters") != null) ? (String)contextParams.get("extraParameters") : "");
/* 104 */     this.m_isEnabled = StringUtils.convertToBool((String)contextParams.get("isEnabled"), true);
/*     */   }
/*     */ 
/*     */   public boolean isInitQueued()
/*     */   {
/* 112 */     return this.m_initDataSource.equalsIgnoreCase("queue");
/*     */   }
/*     */ 
/*     */   public boolean isAddQueued() {
/* 116 */     return this.m_addDataSource.equalsIgnoreCase("queue");
/*     */   }
/*     */ 
/*     */   public boolean isDeleteQueued() {
/* 120 */     return this.m_deleteDataSource.equalsIgnoreCase("queue");
/*     */   }
/*     */ 
/*     */   public String getTable()
/*     */   {
/* 128 */     return this.m_table;
/*     */   }
/*     */ 
/*     */   public String getField() {
/* 132 */     return this.m_field;
/*     */   }
/*     */ 
/*     */   public String getKey() {
/* 136 */     return (this.m_table + "." + this.m_field).toLowerCase();
/*     */   }
/*     */ 
/*     */   public String getSecurityContext() {
/* 140 */     return this.m_securityContext;
/*     */   }
/*     */ 
/*     */   public ContextInfo getSecurityContextInfo() {
/* 144 */     if ((this.m_securityContextInfo == null) && 
/* 146 */       (this.m_securityContext != null) && (this.m_securityContext.length() > 0))
/*     */     {
/* 148 */       this.m_securityContextInfo = ContextInfoStorage.getContextInfo(this.m_securityContext);
/*     */     }
/*     */ 
/* 151 */     return this.m_securityContextInfo;
/*     */   }
/*     */ 
/*     */   public String getInitDataSource()
/*     */   {
/* 159 */     return this.m_initDataSource;
/*     */   }
/*     */ 
/*     */   public String getAddDataSource() {
/* 163 */     return this.m_addDataSource;
/*     */   }
/*     */ 
/*     */   public String getDeleteDataSource() {
/* 167 */     return this.m_deleteDataSource;
/*     */   }
/*     */ 
/*     */   public String getExtraParameters() {
/* 171 */     return this.m_extraParameters;
/*     */   }
/*     */ 
/*     */   public String getPartitioner(String mode)
/*     */   {
/* 180 */     if (mode.equalsIgnoreCase("init"))
/*     */     {
/* 182 */       return getInitDataSourcePartitioner();
/*     */     }
/* 184 */     if (mode.equalsIgnoreCase("add"))
/*     */     {
/* 186 */       return getAddDataSourcePartitioner();
/*     */     }
/* 188 */     if (mode.equalsIgnoreCase("delete"))
/*     */     {
/* 190 */       return getDeleteDataSourcePartitioner();
/*     */     }
/* 192 */     return null;
/*     */   }
/*     */ 
/*     */   public String getInitDataSourcePartitioner()
/*     */   {
/* 200 */     return this.m_initDataSourcePartitioner;
/*     */   }
/*     */ 
/*     */   public String getAddDataSourcePartitioner() {
/* 204 */     return this.m_addDataSourcePartitioner;
/*     */   }
/*     */ 
/*     */   public String getDeleteDataSourcePartitioner() {
/* 208 */     return this.m_deleteDataSourcePartitioner;
/*     */   }
/*     */ 
/*     */   public void enable()
/*     */   {
/* 215 */     this.m_isEnabled = true;
/*     */   }
/*     */ 
/*     */   public void disable()
/*     */   {
/* 222 */     this.m_isEnabled = false;
/*     */   }
/*     */ 
/*     */   public boolean isEnabled()
/*     */   {
/* 230 */     return this.m_isEnabled;
/*     */   }
/*     */ 
/*     */   public boolean isDisabled() {
/* 234 */     return !this.m_isEnabled;
/*     */   }
/*     */ 
/*     */   public boolean isSecurityContext() {
/* 238 */     return this.m_isSecurityContext;
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 243 */     IdcStringBuilder contextInfoBuilder = new IdcStringBuilder();
/* 244 */     contextInfoBuilder.append(this.m_table);
/* 245 */     contextInfoBuilder.append(":");
/* 246 */     contextInfoBuilder.append(this.m_field);
/* 247 */     contextInfoBuilder.append(":");
/* 248 */     contextInfoBuilder.append((this.m_initDataSource != null) ? this.m_initDataSource : "");
/* 249 */     contextInfoBuilder.append(":");
/* 250 */     contextInfoBuilder.append((this.m_addDataSource != null) ? this.m_addDataSource : "");
/* 251 */     contextInfoBuilder.append(":");
/* 252 */     contextInfoBuilder.append((this.m_deleteDataSource != null) ? this.m_deleteDataSource : "");
/* 253 */     contextInfoBuilder.append(":");
/* 254 */     contextInfoBuilder.append((this.m_initDataSourcePartitioner != null) ? this.m_initDataSourcePartitioner : "");
/* 255 */     contextInfoBuilder.append(":");
/* 256 */     contextInfoBuilder.append((this.m_addDataSourcePartitioner != null) ? this.m_addDataSourcePartitioner : "");
/* 257 */     contextInfoBuilder.append(":");
/* 258 */     contextInfoBuilder.append((this.m_deleteDataSourcePartitioner != null) ? this.m_deleteDataSourcePartitioner : "");
/* 259 */     contextInfoBuilder.append(":");
/* 260 */     contextInfoBuilder.append(Boolean.toString(this.m_isSecurityContext));
/* 261 */     contextInfoBuilder.append(":");
/* 262 */     contextInfoBuilder.append(Boolean.toString(this.m_isEnabled));
/* 263 */     return contextInfoBuilder.toString();
/*     */   }
/*     */ 
/*     */   public static List<String> getFields()
/*     */   {
/* 271 */     List fields = new ArrayList();
/* 272 */     fields.add("table");
/* 273 */     fields.add("field");
/* 274 */     fields.add("securityContext");
/* 275 */     fields.add("initDataSource");
/* 276 */     fields.add("addDataSource");
/* 277 */     fields.add("deleteDataSource");
/* 278 */     fields.add("initDataSource.partitioner");
/* 279 */     fields.add("addDataSource.partitioner");
/* 280 */     fields.add("deleteDataSource.partitioner");
/* 281 */     fields.add("isSecurityContext");
/* 282 */     fields.add("isEnabled");
/* 283 */     fields.add("extraParameters");
/* 284 */     return fields;
/*     */   }
/*     */ 
/*     */   public List<String> getFieldValues()
/*     */   {
/* 292 */     List fieldValues = new ArrayList();
/* 293 */     fieldValues.add(this.m_table);
/* 294 */     fieldValues.add(this.m_field);
/* 295 */     fieldValues.add(this.m_securityContext);
/* 296 */     fieldValues.add(this.m_initDataSource);
/* 297 */     fieldValues.add(this.m_addDataSource);
/* 298 */     fieldValues.add(this.m_deleteDataSource);
/* 299 */     fieldValues.add(this.m_initDataSourcePartitioner);
/* 300 */     fieldValues.add(this.m_addDataSourcePartitioner);
/* 301 */     fieldValues.add(this.m_deleteDataSourcePartitioner);
/* 302 */     fieldValues.add(Boolean.toString(this.m_isSecurityContext));
/* 303 */     fieldValues.add(Boolean.toString(this.m_isEnabled));
/* 304 */     fieldValues.add((this.m_extraParameters != null) ? this.m_extraParameters : "");
/* 305 */     return fieldValues;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg) {
/* 309 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99650 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.autosuggest.records.ContextInfo
 * JD-Core Version:    0.5.4
 */