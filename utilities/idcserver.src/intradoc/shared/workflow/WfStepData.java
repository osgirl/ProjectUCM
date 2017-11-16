/*     */ package intradoc.shared.workflow;
/*     */ 
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.PropParameters;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.shared.UserData;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class WfStepData extends DataResultSet
/*     */ {
/*     */   public static final String m_tableName = "WorkflowSteps";
/*     */   public static final int USERS_TYPE = 0;
/*     */   public static final int ALIASES_TYPE = 1;
/*     */   public static final int ALIAS_TYPE = 2;
/*  40 */   public boolean m_isAlias = false;
/*  41 */   public int m_aliasesIndex = -1;
/*  42 */   public int m_userIndex = -1;
/*  43 */   public int m_tokenIndex = -1;
/*     */ 
/*  45 */   protected FieldInfo[] m_fieldInfos = null;
/*  46 */   protected static final String[] TABLE_COLUMNS = { "dWfStepName", "dWfStepID", "dWfID", "dWfStepType", "dWfStepIsAll", "dWfStepWeight", "dWfStepDescription", "dWfStepIsSignature" };
/*     */ 
/*  58 */   public static final String[] TEMPLATE_COLUMNS = { "dAliases", "dWfStepHasWeight" };
/*     */ 
/*  64 */   public static final String[] ALIAS_COLUMNS = { "dAlias", "dAliasType" };
/*     */ 
/*  71 */   public static final String[][] WORKFLOW_STATES = { { ":CA:", "EDIT" }, { ":R:", "REVIEW" }, { ":C:", "EDIT" } };
/*     */ 
/*     */   public WfStepData()
/*     */   {
/*  80 */     super(TABLE_COLUMNS);
/*     */   }
/*     */ 
/*     */   public DataResultSet shallowClone()
/*     */   {
/*  86 */     DataResultSet rset = new WfStepData();
/*  87 */     initShallow(rset);
/*     */ 
/*  89 */     return rset;
/*     */   }
/*     */ 
/*     */   public void initShallow(DataResultSet rset)
/*     */   {
/*  95 */     super.initShallow(rset);
/*     */     try
/*     */     {
/*  99 */       this.m_fieldInfos = ResultSetUtils.createInfoList(this, TABLE_COLUMNS, true);
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 103 */       if (!SystemUtils.m_verbose)
/*     */         return;
/* 105 */       Report.debug(null, null, e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void load(ResultSet rset)
/*     */     throws DataException
/*     */   {
/* 112 */     if (rset != null)
/*     */     {
/* 114 */       copy(rset);
/*     */     }
/* 116 */     this.m_fieldInfos = ResultSetUtils.createInfoList(this, TABLE_COLUMNS, true);
/*     */   }
/*     */ 
/*     */   public void loadStepDataType(ResultSet rset, int type)
/*     */     throws DataException
/*     */   {
/* 124 */     load(rset);
/*     */ 
/* 126 */     String[] newColumns = null;
/* 127 */     switch (type)
/*     */     {
/*     */     case 0:
/* 130 */       if (this.m_userIndex < 0)
/*     */       {
/* 132 */         newColumns = new String[] { "dUsers", "dHasTokens" };
/* 133 */         int num = getNumFields();
/* 134 */         this.m_userIndex = num;
/* 135 */         this.m_tokenIndex = (num + 1);
/* 136 */       }break;
/*     */     case 1:
/* 140 */       if (this.m_aliasesIndex < 0)
/*     */       {
/* 142 */         newColumns = new String[] { "dAliases" };
/* 143 */         this.m_aliasesIndex = getNumFields(); } break;
/*     */     case 2:
/* 148 */       if (!this.m_isAlias)
/*     */       {
/* 150 */         newColumns = new String[] { "dAlias", "dAliasType" };
/* 151 */         this.m_isAlias = true;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 156 */     if (newColumns == null)
/*     */       return;
/* 158 */     Vector infos = ResultSetUtils.createFieldInfo(newColumns, 30);
/* 159 */     mergeFieldsWithFlags(infos, 0);
/*     */   }
/*     */ 
/*     */   public void loadStepDataFromBinder(DataBinder binder, int type)
/*     */     throws DataException
/*     */   {
/* 165 */     Vector values = createRow(binder);
/* 166 */     addRow(values);
/*     */ 
/* 168 */     loadStepDataType(null, type);
/*     */   }
/*     */ 
/*     */   public void addUsers(Vector users, boolean hasTokens)
/*     */     throws DataException
/*     */   {
/* 176 */     Vector userList = new IdcVector();
/* 177 */     int num = users.size();
/* 178 */     for (int i = 0; i < num; ++i)
/*     */     {
/* 180 */       UserData data = (UserData)users.elementAt(i);
/* 181 */       userList.addElement(data.m_name);
/*     */     }
/*     */ 
/* 184 */     String userStr = StringUtils.createString(userList, '\t', '^');
/* 185 */     setCurrentValue(this.m_userIndex, userStr);
/*     */ 
/* 187 */     setCurrentValue(this.m_tokenIndex, String.valueOf(hasTokens));
/*     */   }
/*     */ 
/*     */   public void addUserNames(Vector users, boolean hasTokens) throws DataException
/*     */   {
/* 192 */     Vector userList = new IdcVector();
/* 193 */     int num = users.size();
/* 194 */     for (int i = 0; i < num; ++i)
/*     */     {
/* 196 */       String name = (String)users.elementAt(i);
/* 197 */       userList.addElement(name);
/*     */     }
/*     */ 
/* 200 */     String userStr = StringUtils.createString(userList, '\t', '^');
/* 201 */     setCurrentValue(this.m_userIndex, userStr);
/*     */ 
/* 203 */     setCurrentValue(this.m_tokenIndex, String.valueOf(hasTokens));
/*     */   }
/*     */ 
/*     */   public Vector getUsers() throws DataException
/*     */   {
/* 208 */     if (this.m_userIndex < 0)
/*     */     {
/* 210 */       this.m_userIndex = ResultSetUtils.getIndexMustExist(this, "dUsers");
/*     */     }
/*     */ 
/* 213 */     String userStr = getStringValue(this.m_userIndex);
/* 214 */     return StringUtils.parseArray(userStr, '\t', '^');
/*     */   }
/*     */ 
/*     */   public boolean isUserInStep(String user) throws DataException
/*     */   {
/* 219 */     if (this.m_userIndex < 0)
/*     */     {
/* 221 */       this.m_userIndex = ResultSetUtils.getIndexMustExist(this, "dUsers");
/*     */     }
/*     */ 
/* 224 */     String userStr = getStringValue(this.m_userIndex);
/* 225 */     Vector users = StringUtils.parseArray(userStr, '\t', '^');
/*     */ 
/* 227 */     int num = users.size();
/* 228 */     for (int i = 0; i < num; ++i)
/*     */     {
/* 230 */       String value = (String)users.elementAt(i);
/* 231 */       if (user.equalsIgnoreCase(value))
/*     */       {
/* 234 */         return true;
/*     */       }
/*     */     }
/* 237 */     return false;
/*     */   }
/*     */ 
/*     */   public void loadAliasStepData(Properties stepInfo)
/*     */     throws DataException
/*     */   {
/* 246 */     loadStepDataType(null, 2);
/*     */ 
/* 249 */     String aliases = stepInfo.getProperty("dAliases");
/* 250 */     Vector aliasInfo = StringUtils.parseArray(aliases, '\t', '^');
/*     */ 
/* 252 */     PropParameters params = new PropParameters(stepInfo);
/* 253 */     int num = aliasInfo.size();
/* 254 */     for (int i = 0; i < num; ++i)
/*     */     {
/* 256 */       params.m_properties.put("dAlias", aliasInfo.elementAt(i));
/* 257 */       params.m_properties.put("dAliasType", aliasInfo.elementAt(++i));
/* 258 */       Vector values = createRow(params);
/* 259 */       addRow(values);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void addAliasesString(ResultSet rset) throws DataException
/*     */   {
/* 265 */     if (this.m_aliasesIndex < 0)
/*     */     {
/* 267 */       this.m_aliasesIndex = ResultSetUtils.getIndexMustExist(this, "dAliases");
/*     */     }
/*     */ 
/* 270 */     String aliases = getAliasesString(rset);
/* 271 */     setCurrentValue(this.m_aliasesIndex, aliases);
/*     */   }
/*     */ 
/*     */   public String getAliasesString(ResultSet aliasSet) throws DataException
/*     */   {
/* 276 */     String[] keys = { "dAlias", "dAliasType" };
/* 277 */     String[][] aliasInfo = ResultSetUtils.createStringTable(aliasSet, keys);
/* 278 */     Vector aliases = new IdcVector();
/* 279 */     for (int i = 0; i < aliasInfo.length; ++i)
/*     */     {
/* 281 */       aliases.addElement(aliasInfo[i][0]);
/* 282 */       aliases.addElement(aliasInfo[i][1]);
/*     */     }
/*     */ 
/* 285 */     return StringUtils.createString(aliases, '\t', '^');
/*     */   }
/*     */ 
/*     */   public String validateStep() throws DataException
/*     */   {
/* 290 */     String stepType = getStringValue(this.m_fieldInfos[3].m_index);
/* 291 */     if (WorkflowScriptUtils.isAutoContributorStep(stepType))
/*     */     {
/* 294 */       return null;
/*     */     }
/*     */ 
/* 297 */     String stepName = getStepName();
/* 298 */     Vector stepUsers = getUsers();
/* 299 */     boolean hasTokens = StringUtils.convertToBool(getStringValue(this.m_tokenIndex), false);
/* 300 */     int numUsers = stepUsers.size();
/*     */ 
/* 302 */     String errMsg = null;
/* 303 */     if ((numUsers == 0) && (!hasTokens))
/*     */     {
/* 306 */       errMsg = LocaleUtils.encodeMessage("csWfStepNoUsers", null, stepName);
/*     */     }
/*     */     else
/*     */     {
/* 310 */       boolean isAll = getIsAll();
/* 311 */       if (isAll == true)
/*     */       {
/* 314 */         return null;
/*     */       }
/*     */ 
/* 317 */       int weight = getWeight();
/* 318 */       if (weight < 0)
/*     */       {
/* 320 */         errMsg = LocaleUtils.encodeMessage("csWfStepWeightError1", null, stepName);
/*     */       }
/*     */ 
/* 324 */       if ((errMsg == null) && (!hasTokens) && (weight > numUsers))
/*     */       {
/* 326 */         errMsg = LocaleUtils.encodeMessage("csWfStepWeightError2", null, stepName);
/*     */       }
/*     */ 
/* 330 */       if ((errMsg == null) && (WorkflowScriptUtils.isContributorStep(stepType)) && (!isAll) && (weight == 0) && (!hasTokens))
/*     */       {
/* 333 */         String stepTypeDes = WorkflowScriptUtils.formatStepTypeDescription(stepType);
/* 334 */         errMsg = LocaleUtils.encodeMessage("csWfStepNeedsReviewer", null, stepTypeDes, stepName);
/*     */       }
/*     */     }
/* 337 */     return errMsg;
/*     */   }
/*     */ 
/*     */   public String getStepName()
/*     */   {
/* 345 */     return getStringValue(this.m_fieldInfos[0].m_index);
/*     */   }
/*     */ 
/*     */   public String getStepID()
/*     */   {
/* 350 */     return getStringValue(this.m_fieldInfos[1].m_index);
/*     */   }
/*     */ 
/*     */   public String getStepType()
/*     */   {
/* 355 */     return getStringValue(this.m_fieldInfos[3].m_index);
/*     */   }
/*     */ 
/*     */   public boolean getIsAll()
/*     */   {
/* 360 */     String isAllStr = getStringValue(this.m_fieldInfos[4].m_index);
/* 361 */     return StringUtils.convertToBool(isAllStr, false);
/*     */   }
/*     */ 
/*     */   public int getWeight()
/*     */   {
/* 366 */     String weightStr = getStringValue(this.m_fieldInfos[5].m_index);
/* 367 */     int weight = 1;
/*     */     try
/*     */     {
/* 370 */       weight = Integer.parseInt(weightStr);
/*     */     }
/*     */     catch (Throwable e)
/*     */     {
/* 374 */       Report.trace(null, "Unable to get step weight.  Parse error.", e);
/*     */     }
/* 376 */     return weight;
/*     */   }
/*     */ 
/*     */   public String getRequiredDocState()
/*     */   {
/* 381 */     String stepType = getStringValue(this.m_fieldInfos[3].m_index);
/* 382 */     return getRequiredDocState(stepType);
/*     */   }
/*     */ 
/*     */   public String getRequiredWorkflowState()
/*     */   {
/* 387 */     String stepType = getStringValue(this.m_fieldInfos[3].m_index);
/* 388 */     return getRequiredWorkflowState(stepType);
/*     */   }
/*     */ 
/*     */   public boolean isLastRow()
/*     */   {
/* 393 */     return this.m_currentRow == this.m_numRows - 1;
/*     */   }
/*     */ 
/*     */   public static String getRequiredDocState(String stepType)
/*     */   {
/* 401 */     int len = WORKFLOW_STATES.length;
/* 402 */     for (int i = 0; i < len; ++i)
/*     */     {
/* 404 */       if (stepType.indexOf(WORKFLOW_STATES[i][0]) >= 0)
/*     */       {
/* 406 */         return WORKFLOW_STATES[i][1];
/*     */       }
/*     */     }
/*     */ 
/* 410 */     return "DONE";
/*     */   }
/*     */ 
/*     */   public static String getRequiredWorkflowState(String stepType)
/*     */   {
/* 417 */     boolean isReview = stepType.indexOf(":R:") >= 0;
/* 418 */     boolean isEditable = stepType.indexOf(":C:") >= 0;
/* 419 */     if ((isReview) && (isEditable))
/*     */     {
/* 421 */       return "W";
/*     */     }
/* 423 */     if (isReview)
/*     */     {
/* 425 */       return "R";
/*     */     }
/* 427 */     if (isEditable)
/*     */     {
/* 429 */       return "E";
/*     */     }
/* 431 */     return "E";
/*     */   }
/*     */ 
/*     */   public static String[] getTemplateColumns()
/*     */   {
/* 437 */     int numColumns = TABLE_COLUMNS.length;
/* 438 */     int totalColumns = numColumns + TEMPLATE_COLUMNS.length;
/* 439 */     String[] tColumns = new String[totalColumns];
/*     */ 
/* 441 */     for (int i = 0; i < totalColumns; ++i)
/*     */     {
/* 443 */       if (i < numColumns)
/*     */       {
/* 445 */         tColumns[i] = TABLE_COLUMNS[i];
/*     */       }
/*     */       else
/*     */       {
/* 449 */         tColumns[i] = TEMPLATE_COLUMNS[(i - numColumns)];
/*     */       }
/*     */     }
/* 452 */     return tColumns;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 457 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 94199 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.workflow.WfStepData
 * JD-Core Version:    0.5.4
 */