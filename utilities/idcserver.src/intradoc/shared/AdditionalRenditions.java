/*     */ package intradoc.shared;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import java.util.List;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class AdditionalRenditions extends DataResultSet
/*     */ {
/*     */   public static final String m_tableName = "AdditionalRenditions";
/*  32 */   public static final String[][] m_defaultMap = { { "", "!apNoAdditionalRendition" } };
/*  33 */   public static int m_maxNum = 2;
/*     */   public int m_numUsableRenditions;
/*     */ 
/*     */   public AdditionalRenditions()
/*     */   {
/*  34 */     this.m_numUsableRenditions = 0;
/*     */   }
/*     */ 
/*     */   public DataResultSet shallowClone()
/*     */   {
/*  39 */     AdditionalRenditions rset = new AdditionalRenditions();
/*  40 */     initShallow(rset);
/*  41 */     rset.m_numUsableRenditions = this.m_numUsableRenditions;
/*     */ 
/*  43 */     return rset;
/*     */   }
/*     */ 
/*     */   public void load(ResultSet rset) throws DataException
/*     */   {
/*  48 */     loadEx(rset, null);
/*     */   }
/*     */ 
/*     */   public void loadEx(ResultSet rset, String[] allowableList) throws DataException
/*     */   {
/*  53 */     copy(rset);
/*     */ 
/*  55 */     FieldInfo[] info = ResultSetUtils.createInfoList(this, new String[] { "renFlag", "renLabel", "renDescription", "renProductionStep" }, true);
/*     */ 
/*  58 */     if (allowableList != null)
/*     */     {
/*  60 */       List values = createNewResultSetList(32);
/*  61 */       for (int i = 0; i < allowableList.length; ++i)
/*     */       {
/*  63 */         Vector row = findRow(info[3].m_index, allowableList[i]);
/*  64 */         if (row == null)
/*     */         {
/*  66 */           String msg = LocaleUtils.encodeMessage("apAllowableRenditionsListValueNotPresent", null, allowableList[i]);
/*  67 */           throw new DataException(msg);
/*     */         }
/*  69 */         values.add(row);
/*     */       }
/*  71 */       this.m_values = values;
/*  72 */       this.m_numRows = this.m_values.size();
/*     */     }
/*  74 */     this.m_numUsableRenditions = this.m_numRows;
/*     */ 
/*  77 */     Vector def = findRow(info[0].m_index, "");
/*  78 */     if (def == null)
/*     */     {
/*  81 */       def = createEmptyRow();
/*  82 */       def.setElementAt("!apNoAdditionalRendition", info[1].m_index);
/*  83 */       def.setElementAt("!apNoAdditionalRendition", info[2].m_index);
/*  84 */       insertRowAt(def, 0);
/*     */     }
/*     */ 
/*  87 */     int numAdditionalRenditions = SharedObjects.getEnvironmentInt("NumAdditionalRenditions", 0);
/*  88 */     if (numAdditionalRenditions <= m_maxNum)
/*     */       return;
/*  90 */     m_maxNum = numAdditionalRenditions;
/*     */   }
/*     */ 
/*     */   public String[][] createDisplayMap(ExecutionContext cxt)
/*     */   {
/*  96 */     String[] columns = { "renFlag", "renLabel" };
/*     */ 
/*  98 */     String[][] displayMap = (String[][])null;
/*     */     try
/*     */     {
/* 101 */       displayMap = ResultSetUtils.createStringTable(this, columns);
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 105 */       Report.error(null, "!csUnableToCreateRenditionDisplayMap", e);
/*     */     }
/*     */ 
/* 108 */     if ((displayMap == null) || (displayMap.length == 0))
/*     */     {
/* 110 */       displayMap = m_defaultMap;
/* 111 */       LocaleResources.localizeStaticDoubleArray(displayMap, cxt, 1);
/*     */     }
/*     */     else
/*     */     {
/* 115 */       LocaleResources.localizeDoubleArray(displayMap, cxt, 1);
/*     */     }
/*     */ 
/* 118 */     return displayMap;
/*     */   }
/*     */ 
/*     */   public String getExtension(String renFlag) throws DataException
/*     */   {
/* 123 */     String[] columns = { "renFlag", "renExtension" };
/*     */ 
/* 125 */     String[][] extensions = ResultSetUtils.createFilteredStringTable(this, columns, renFlag);
/* 126 */     if ((extensions != null) && (extensions.length > 0))
/*     */     {
/* 128 */       return extensions[0][0];
/*     */     }
/*     */ 
/* 131 */     String msg = LocaleUtils.encodeMessage("csUnableToDetermineExtension", null, renFlag);
/*     */ 
/* 133 */     throw new DataException(msg);
/*     */   }
/*     */ 
/*     */   public String getExtensionByLabel(String renLable) throws DataException
/*     */   {
/* 138 */     String[] columns = { "renLabel", "renExtension" };
/*     */ 
/* 140 */     String[][] extensions = ResultSetUtils.createFilteredStringTable(this, columns, renLable);
/* 141 */     if ((extensions != null) && (extensions.length > 0))
/*     */     {
/* 143 */       return extensions[0][0];
/*     */     }
/*     */ 
/* 146 */     String msg = LocaleUtils.encodeMessage("csUnableToDetermineExtension", null, renLable);
/*     */ 
/* 148 */     throw new DataException(msg);
/*     */   }
/*     */ 
/*     */   public String getFlag(String renExt) throws DataException
/*     */   {
/* 153 */     String[] columns = { "renExtension", "renFlag" };
/*     */ 
/* 155 */     String[][] extensions = ResultSetUtils.createFilteredStringTable(this, columns, renExt);
/* 156 */     if ((extensions != null) && (extensions.length > 0))
/*     */     {
/* 158 */       return extensions[0][0];
/*     */     }
/*     */ 
/* 161 */     String msg = LocaleUtils.encodeMessage("csUnableToDetermineExtension", null, renExt);
/*     */ 
/* 163 */     throw new DataException(msg);
/*     */   }
/*     */ 
/*     */   public String getProductionStepName(String renExt) throws DataException
/*     */   {
/* 168 */     String[] columns = { "renExtension", "renProductionStep" };
/*     */ 
/* 170 */     String[][] extensions = ResultSetUtils.createFilteredStringTable(this, columns, renExt);
/* 171 */     if ((extensions != null) && (extensions.length > 0))
/*     */     {
/* 173 */       return extensions[0][0];
/*     */     }
/*     */ 
/* 176 */     String msg = LocaleUtils.encodeMessage("csUnableToDetermineExtension", null, renExt);
/*     */ 
/* 178 */     throw new DataException(msg);
/*     */   }
/*     */ 
/*     */   public String getExtensionFromProductionStepName(String renProdName) throws DataException
/*     */   {
/* 183 */     String[] columns = { "renProductionStep", "renExtension" };
/*     */ 
/* 185 */     String[][] extensions = ResultSetUtils.createFilteredStringTable(this, columns, renProdName);
/* 186 */     if ((extensions != null) && (extensions.length > 0))
/*     */     {
/* 188 */       return extensions[0][0];
/*     */     }
/*     */ 
/* 191 */     String msg = LocaleUtils.encodeMessage("csUnableToDetermineExtension", null, renProdName);
/*     */ 
/* 193 */     throw new DataException(msg);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 198 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78807 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.AdditionalRenditions
 * JD-Core Version:    0.5.4
 */