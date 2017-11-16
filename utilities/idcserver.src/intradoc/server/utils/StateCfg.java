/*     */ package intradoc.server.utils;
/*     */ 
/*     */ import intradoc.common.EnvUtils;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.shared.ResultSetTreeSort;
/*     */ import java.io.OutputStream;
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class StateCfg
/*     */ {
/*     */   protected DataResultSet m_stateCfgExportedVariables;
/*     */   protected FieldInfo[] m_stateCfgVarsFieldInfo;
/*     */   protected String m_intradocDir;
/*     */   protected String m_configDir;
/*     */   protected String m_homeDir;
/*     */   protected OutputStream m_output;
/*     */   protected static final String m_stateCfgTableName = "StateCfgExportedVars";
/*     */   protected static final String m_stateFileName = "state.cfg";
/*  54 */   public static final String[] STATE_CFG_COLUMNS = { "name", "value", "loadOrder", "componentName", "isPath" };
/*     */ 
/*     */   public StateCfg()
/*     */   {
/*  61 */     this.m_stateCfgExportedVariables = new DataResultSet(STATE_CFG_COLUMNS);
/*     */     try
/*     */     {
/*  64 */       this.m_stateCfgVarsFieldInfo = ResultSetUtils.createInfoList(this.m_stateCfgExportedVariables, STATE_CFG_COLUMNS, true);
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/*  70 */       Report.trace(null, "Unable to build state cfg field info list.", e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void init(String intradocDir, String configDir, String homeDir)
/*     */   {
/*  76 */     this.m_intradocDir = intradocDir;
/*  77 */     this.m_configDir = configDir;
/*  78 */     this.m_homeDir = homeDir;
/*     */   }
/*     */ 
/*     */   public void setOutput(OutputStream output)
/*     */   {
/*  83 */     this.m_output = output;
/*     */   }
/*     */ 
/*     */   public OutputStream getOutput()
/*     */   {
/*  88 */     return this.m_output;
/*     */   }
/*     */ 
/*     */   public ResultSet getExportedVariables()
/*     */   {
/*  93 */     return this.m_stateCfgExportedVariables.shallowClone();
/*     */   }
/*     */ 
/*     */   public void updateComponentVarsRow(String name, DataBinder binder)
/*     */     throws DataException
/*     */   {
/*  99 */     synchronized ("StateCfgExportedVars")
/*     */     {
/* 101 */       DataResultSet exportedVars = (DataResultSet)binder.getResultSet("StateCfgExportedVars");
/* 102 */       if (exportedVars != null)
/*     */       {
/* 104 */         int cmpNameIndex = ResultSetUtils.getIndexMustExist(this.m_stateCfgExportedVariables, "componentName");
/*     */ 
/* 106 */         int nameIndex = ResultSetUtils.getIndexMustExist(this.m_stateCfgExportedVariables, "name");
/*     */ 
/* 109 */         for (exportedVars.first(); exportedVars.isRowPresent(); exportedVars.next())
/*     */         {
/* 111 */           Map map = exportedVars.getCurrentRowMap();
/* 112 */           String varName = (String)map.get("name");
/* 113 */           if (varName == null) continue; if (varName.length() == 0)
/*     */           {
/*     */             continue;
/*     */           }
/*     */ 
/* 119 */           boolean isFound = false;
/* 120 */           List row = this.m_stateCfgExportedVariables.findRow(cmpNameIndex, name, 0, 0);
/* 121 */           while (row != null)
/*     */           {
/* 123 */             String curVarName = (String)row.get(nameIndex);
/* 124 */             if (curVarName.equals(varName))
/*     */             {
/* 126 */               isFound = true;
/* 127 */               break;
/*     */             }
/* 129 */             int start = this.m_stateCfgExportedVariables.getCurrentRow() + 1;
/* 130 */             row = this.m_stateCfgExportedVariables.findRow(cmpNameIndex, name, start, 0);
/*     */           }
/*     */ 
/* 134 */           List newRow = this.m_stateCfgExportedVariables.createEmptyRowAsList();
/* 135 */           int numFields = this.m_stateCfgVarsFieldInfo.length;
/* 136 */           for (int i = 0; i < numFields; ++i)
/*     */           {
/* 138 */             FieldInfo fi = this.m_stateCfgVarsFieldInfo[i];
/* 139 */             String clmn = fi.m_name;
/* 140 */             String val = null;
/* 141 */             if (clmn.equals("componentName"))
/*     */             {
/* 143 */               val = name;
/*     */             }
/*     */             else
/*     */             {
/* 147 */               val = (String)map.get(fi.m_name);
/*     */             }
/* 149 */             if (val == null)
/*     */               continue;
/* 151 */             newRow.set(i, val);
/*     */           }
/*     */ 
/* 154 */           if (!isFound)
/*     */           {
/* 156 */             this.m_stateCfgExportedVariables.addRowWithList(newRow);
/*     */           }
/*     */           else
/*     */           {
/* 161 */             int curRow = this.m_stateCfgExportedVariables.getCurrentRow();
/* 162 */             this.m_stateCfgExportedVariables.setRowWithList(newRow, curRow);
/*     */           }
/*     */         }
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void removeComponentVars(String name) throws DataException
/*     */   {
/* 171 */     synchronized ("StateCfgExportedVars")
/*     */     {
/* 173 */       int nameIndex = ResultSetUtils.getIndexMustExist(this.m_stateCfgExportedVariables, "componentName");
/*     */ 
/* 175 */       DataResultSet scSet = new DataResultSet();
/* 176 */       scSet.copy(this.m_stateCfgExportedVariables);
/*     */ 
/* 178 */       DataResultSet newSet = new DataResultSet();
/* 179 */       newSet.copyFieldInfo(this.m_stateCfgExportedVariables);
/* 180 */       for (scSet.first(); scSet.isRowPresent(); scSet.next())
/*     */       {
/* 182 */         String cmp = scSet.getStringValue(nameIndex);
/* 183 */         if (cmp.equals(name))
/*     */           continue;
/* 185 */         Vector row = scSet.getCurrentRowValues();
/* 186 */         newSet.addRow(row);
/*     */       }
/*     */ 
/* 189 */       this.m_stateCfgExportedVariables = newSet;
/*     */     }
/*     */   }
/*     */ 
/*     */   public void updateStateConfigWithArgs(DataBinder compBinder, Map env, Map args)
/*     */     throws ServiceException
/*     */   {
/* 196 */     boolean isAdminServer = StringUtils.convertToBool((String)args.get("isAdminServer"), false);
/* 197 */     boolean isInDev = StringUtils.convertToBool((String)args.get("isComponentsInDev"), false);
/* 198 */     DataResultSet oldVars = null;
/* 199 */     if (isAdminServer)
/*     */     {
/* 201 */       DataResultSet exportedVars = (DataResultSet)compBinder.getResultSet("StateCfgExportedVars");
/* 202 */       if ((exportedVars != null) && (!exportedVars.isEmpty()))
/*     */       {
/* 204 */         oldVars = this.m_stateCfgExportedVariables;
/* 205 */         this.m_stateCfgExportedVariables = exportedVars;
/*     */       }
/*     */     }
/*     */     try
/*     */     {
/* 210 */       DataResultSet componentSet = (DataResultSet)compBinder.getResultSet("Components");
/* 211 */       updateStateConfig(componentSet, isInDev, env);
/*     */     }
/*     */     finally
/*     */     {
/* 215 */       if (oldVars != null)
/*     */       {
/* 217 */         this.m_stateCfgExportedVariables = oldVars;
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void updateStateConfig(DataResultSet componentSet, boolean componentsInDev, Map env)
/*     */     throws ServiceException
/*     */   {
/* 225 */     List pathOrder = new ArrayList();
/* 226 */     Map props = new HashMap();
/* 227 */     String defaultSeparator = EnvUtils.getPathSeparator();
/* 228 */     String[][] orderItems = { { "classpath", "classpathorder", "COMPONENTS_CLASSPATH", defaultSeparator, "true" }, { "libpath", "libpathorder", "COMPONENTS_LIBPATH", defaultSeparator, "true" }, { "Launchers", "LaunchersOrder", "LAUNCHERS_components", ",", "false" } };
/*     */     try
/*     */     {
/* 235 */       pathOrder.add(LocaleResources.getString("csCompWizStateCfgWarning", null));
/*     */ 
/* 237 */       for (int i = 0; i < orderItems.length; ++i)
/*     */       {
/* 241 */         pathOrder.add(orderItems[i][2]);
/*     */       }
/*     */ 
/* 244 */       for (int i = 0; i < orderItems.length; ++i)
/*     */       {
/* 246 */         char sp = orderItems[i][3].charAt(0);
/* 247 */         boolean isPathEntry = StringUtils.convertToBool(orderItems[i][4], false);
/* 248 */         List pathItems = new ArrayList();
/* 249 */         int fieldIndex = ResultSetUtils.getIndexMustExist(componentSet, orderItems[i][0]);
/*     */ 
/* 252 */         DataResultSet drset = new DataResultSet();
/* 253 */         drset.copy(componentSet);
/*     */ 
/* 256 */         FieldInfo orderInfo = new FieldInfo();
/* 257 */         if (componentSet.getFieldInfo(orderItems[i][1], orderInfo))
/*     */         {
/* 260 */           ResultSetTreeSort treeSort = new ResultSetTreeSort(drset, orderInfo.m_index, false);
/*     */ 
/* 262 */           treeSort.determineFieldType("int");
/* 263 */           treeSort.sort();
/*     */         }
/*     */ 
/* 267 */         for (drset.first(); drset.isRowPresent(); drset.next())
/*     */         {
/* 269 */           Map map = drset.getCurrentRowMap();
/* 270 */           String compName = (String)map.get("name");
/*     */ 
/* 272 */           String compDir = FileUtils.getDirectory(ComponentLocationUtils.determineComponentLocationWithEnv(map, 0, env, true));
/*     */ 
/* 276 */           Vector row = componentSet.findRow(0, compName);
/* 277 */           if (row == null)
/*     */             continue;
/* 279 */           String status = (String)map.get("status");
/* 280 */           if (!status.equalsIgnoreCase("enabled"))
/*     */             continue;
/* 282 */           String val = drset.getStringValue(fieldIndex);
/* 283 */           String name = compName + "_" + orderItems[i][0];
/* 284 */           if (isPathEntry)
/*     */           {
/* 286 */             ComponentListUtils.addPath(compDir, props, pathOrder, pathItems, name, val, sp, componentsInDev, env);
/*     */           }
/*     */           else {
/* 289 */             if (val.length() <= 0)
/*     */               continue;
/* 291 */             pathItems.add(val);
/* 292 */             pathOrder.add(name);
/* 293 */             props.put(name, val);
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/* 300 */         String pathStr = StringUtils.createString(pathItems, sp, sp);
/* 301 */         props.put(orderItems[i][2], pathStr);
/*     */       }
/*     */ 
/* 304 */       if ((this.m_stateCfgExportedVariables != null) && (this.m_stateCfgExportedVariables.getNumRows() > 0))
/*     */       {
/* 307 */         DataResultSet drset = new DataResultSet();
/* 308 */         drset.copy(this.m_stateCfgExportedVariables);
/*     */ 
/* 311 */         ResultSetTreeSort treeSort = new ResultSetTreeSort(drset, this.m_stateCfgVarsFieldInfo[2].m_index, false);
/*     */ 
/* 313 */         treeSort.determineFieldType("int");
/* 314 */         treeSort.sort();
/*     */ 
/* 316 */         List pathItems = new ArrayList();
/* 317 */         for (drset.first(); drset.isRowPresent(); drset.next())
/*     */         {
/* 319 */           String compName = drset.getStringValue(this.m_stateCfgVarsFieldInfo[3].m_index);
/* 320 */           if (componentSet.findRow(0, compName) == null)
/*     */             continue;
/* 322 */           Map map = componentSet.getCurrentRowMap();
/* 323 */           String status = (String)map.get("status");
/* 324 */           if (!status.equalsIgnoreCase("enabled")) {
/*     */             continue;
/*     */           }
/*     */ 
/* 328 */           String compDir = FileUtils.getDirectory(ComponentLocationUtils.determineComponentLocationWithEnv(map, 0, env, true));
/*     */ 
/* 332 */           String varName = drset.getStringValue(this.m_stateCfgVarsFieldInfo[0].m_index);
/* 333 */           String value = drset.getStringValue(this.m_stateCfgVarsFieldInfo[1].m_index);
/*     */ 
/* 336 */           boolean isPathEntry = StringUtils.convertToBool(drset.getStringValue(this.m_stateCfgVarsFieldInfo[4].m_index), true);
/*     */ 
/* 338 */           if (isPathEntry)
/*     */           {
/* 341 */             ComponentListUtils.addPath(compDir, props, pathOrder, pathItems, varName, value, defaultSeparator.charAt(0), componentsInDev, env);
/*     */           }
/*     */           else
/*     */           {
/* 345 */             if (value.length() <= 0)
/*     */               continue;
/* 347 */             pathItems.add(value);
/* 348 */             pathOrder.add(varName);
/* 349 */             props.put(varName, value);
/*     */           }
/*     */         }
/*     */       }
/*     */ 
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 357 */       throw new ServiceException("!csUnableToMergeComponents", e);
/*     */     }
/*     */ 
/* 361 */     SystemPropertiesEditor spe = new SystemPropertiesEditor();
/* 362 */     String filename = (String)env.get("StateCfgFilename");
/* 363 */     if (filename == null)
/*     */     {
/* 365 */       filename = "state.cfg";
/*     */     }
/* 367 */     spe.writePropertiesEx(props, pathOrder, null, this.m_configDir + filename, this.m_output, null, false);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 372 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 93549 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.utils.StateCfg
 * JD-Core Version:    0.5.4
 */