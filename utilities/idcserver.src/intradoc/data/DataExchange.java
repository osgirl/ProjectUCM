/*     */ package intradoc.data;
/*     */ 
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class DataExchange
/*     */ {
/*     */   public ResultSet m_rset;
/*     */   public String[] m_fields;
/*     */   public FieldInfo[] m_info;
/*     */   public int m_numFields;
/*     */   public int m_curField;
/*     */   public FieldInfo m_curFieldInfo;
/*     */   public int m_curRowIndex;
/*     */   public boolean m_appendRow;
/*     */   public Vector m_currentRow;
/*     */   public Object m_curObj;
/*     */ 
/*     */   public DataExchange(ResultSet rset, String[] fields)
/*     */   {
/*  78 */     this.m_rset = rset;
/*  79 */     this.m_fields = fields;
/*     */     try
/*     */     {
/*  82 */       this.m_info = ResultSetUtils.createInfoList(rset, fields, false);
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/*  86 */       throw new Error(e.getMessage());
/*     */     }
/*     */ 
/*  89 */     this.m_numFields = rset.getNumFields();
/*     */ 
/*  91 */     this.m_curFieldInfo = null;
/*  92 */     this.m_curRowIndex = -1;
/*  93 */     this.m_appendRow = true;
/*  94 */     this.m_currentRow = null;
/*  95 */     this.m_curObj = null;
/*     */   }
/*     */ 
/*     */   public void doExchange(DataExchangeBinder binder, boolean writeToResultSet)
/*     */     throws DataException
/*     */   {
/* 101 */     DataResultSet drset = null;
/*     */ 
/* 103 */     if (!writeToResultSet)
/*     */     {
/* 105 */       this.m_rset.first();
/*     */     }
/*     */     else
/*     */     {
/* 109 */       this.m_curRowIndex = 0;
/* 110 */       drset = (DataResultSet)this.m_rset;
/*     */     }
/*     */ 
/*     */     while (true)
/*     */     {
/* 115 */       if ((!writeToResultSet) && 
/* 117 */         (!this.m_rset.isRowPresent()))
/*     */       {
/* 119 */         return;
/*     */       }
/*     */ 
/* 125 */       this.m_curObj = null;
/* 126 */       if (!binder.prepareNextRow(this, writeToResultSet))
/*     */       {
/* 128 */         return;
/*     */       }
/* 130 */       if (writeToResultSet == true)
/*     */       {
/* 132 */         if (!this.m_appendRow)
/*     */         {
/* 134 */           if ((this.m_curRowIndex < 0) || (this.m_curRowIndex >= drset.getNumRows()))
/*     */           {
/* 136 */             return;
/*     */           }
/* 138 */           this.m_currentRow = drset.getRowValues(this.m_curRowIndex);
/*     */         }
/*     */         else
/*     */         {
/* 142 */           this.m_currentRow = new IdcVector();
/* 143 */           this.m_currentRow.setSize(this.m_numFields);
/* 144 */           for (int i = 0; i < this.m_numFields; ++i)
/*     */           {
/* 146 */             this.m_currentRow.setElementAt("", i);
/*     */           }
/*     */         }
/*     */       }
/*     */ 
/* 151 */       boolean allExchanged = true;
/* 152 */       for (int i = 0; i < this.m_fields.length; ++i)
/*     */       {
/* 154 */         setCurrentField(i);
/*     */ 
/* 156 */         this.m_curField = this.m_curFieldInfo.m_index;
/* 157 */         if (this.m_curField < 0) {
/*     */           continue;
/*     */         }
/*     */ 
/* 161 */         if (binder.exchange(this, i, writeToResultSet))
/*     */           continue;
/* 163 */         allExchanged = false;
/* 164 */         break;
/*     */       }
/*     */ 
/* 168 */       if (allExchanged == true)
/*     */       {
/* 170 */         binder.finalizeObject(this, writeToResultSet);
/*     */       }
/*     */ 
/* 173 */       if (!writeToResultSet)
/*     */       {
/* 175 */         this.m_rset.next();
/*     */       }
/*     */       else
/*     */       {
/* 179 */         this.m_curRowIndex += 1;
/* 180 */         if (this.m_appendRow == true)
/*     */         {
/* 182 */           drset.addRow(this.m_currentRow);
/*     */         }
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setCurrentField(int index)
/*     */   {
/* 192 */     this.m_curFieldInfo = this.m_info[index];
/* 193 */     this.m_curField = this.m_curFieldInfo.m_index;
/*     */   }
/*     */ 
/*     */   public void appendMissingFields()
/*     */   {
/* 199 */     Vector missingFields = new IdcVector();
/*     */ 
/* 201 */     for (int i = 0; i < this.m_fields.length; ++i)
/*     */     {
/* 203 */       FieldInfo info = this.m_info[i];
/* 204 */       if (info.m_index != -1)
/*     */         continue;
/* 206 */       info.m_name = this.m_fields[i];
/* 207 */       missingFields.addElement(info);
/*     */     }
/*     */ 
/* 211 */     if (missingFields.size() <= 0)
/*     */       return;
/* 213 */     DataResultSet drset = (DataResultSet)this.m_rset;
/*     */ 
/* 218 */     drset.mergeFieldsWithFlags(missingFields, 2);
/*     */   }
/*     */ 
/*     */   public String getCurValAsString()
/*     */   {
/* 227 */     return this.m_rset.getStringValue(this.m_curField);
/*     */   }
/*     */ 
/*     */   public void setCurValAsString(String val)
/*     */   {
/* 232 */     this.m_currentRow.setElementAt(val, this.m_curField);
/*     */   }
/*     */ 
/*     */   public boolean getCurValAsBoolean()
/*     */   {
/* 237 */     String val = this.m_rset.getStringValue(this.m_curField);
/* 238 */     if (val == null)
/*     */     {
/* 240 */       return false;
/*     */     }
/* 242 */     val = val.trim();
/* 243 */     if (val.length() == 0)
/*     */     {
/* 245 */       return false;
/*     */     }
/* 247 */     return val.charAt(0) != '0';
/*     */   }
/*     */ 
/*     */   public void setCurValAsBoolean(boolean val)
/*     */   {
/* 252 */     String v = (val) ? "1" : "0";
/* 253 */     this.m_currentRow.setElementAt(v, this.m_curField);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 258 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.data.DataExchange
 * JD-Core Version:    0.5.4
 */