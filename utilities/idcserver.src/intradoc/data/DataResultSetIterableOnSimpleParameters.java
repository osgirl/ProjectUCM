/*     */ package intradoc.data;
/*     */ 
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class DataResultSetIterableOnSimpleParameters
/*     */   implements Iterable<SimpleParameters>, Iterator<SimpleParameters>, SimpleParameters
/*     */ {
/*     */   private DataResultSet m_drset;
/*     */   private int m_currentRowIndex;
/*     */   private int m_cacheMisses;
/*     */   private List m_values;
/*     */   private List m_currentRow;
/*     */   private Object[] m_currentRowArray;
/*     */   private int m_keyCount;
/*     */   private String[] m_keyList;
/*     */   private int[] m_keyIndexes;
/*     */   private int m_nextIndex;
/*     */ 
/*     */   public DataResultSetIterableOnSimpleParameters(DataResultSet drset)
/*     */   {
/*  35 */     this(drset, false);
/*     */   }
/*     */ 
/*     */   public DataResultSetIterableOnSimpleParameters(DataResultSet drset, boolean useCurrentRow)
/*     */   {
/*  40 */     this.m_drset = drset;
/*  41 */     this.m_values = this.m_drset.m_values;
/*  42 */     int sizes = drset.m_fieldList.size() + 3;
/*  43 */     this.m_keyCount = 0;
/*  44 */     this.m_keyList = new String[sizes];
/*  45 */     this.m_keyIndexes = new int[sizes];
/*  46 */     if (useCurrentRow)
/*     */     {
/*  48 */       this.m_currentRowIndex = drset.m_currentRow;
/*  49 */       this.m_currentRow = ((List)drset.m_values.get(this.m_currentRowIndex));
/*  50 */       this.m_currentRowArray = ((IdcVector)this.m_currentRow).m_array;
/*     */     }
/*     */     else
/*     */     {
/*  54 */       this.m_currentRowIndex = -1;
/*     */     }
/*     */   }
/*     */ 
/*     */   public Iterator<SimpleParameters> iterator()
/*     */   {
/*  60 */     return this;
/*     */   }
/*     */ 
/*     */   public SimpleParameters next()
/*     */   {
/*  72 */     this.m_currentRowIndex += 1;
/*  73 */     this.m_currentRow = ((List)this.m_values.get(this.m_currentRowIndex));
/*  74 */     this.m_currentRowArray = ((IdcVector)this.m_currentRow).m_array;
/*  75 */     return this;
/*     */   }
/*     */ 
/*     */   public boolean hasNext()
/*     */   {
/*  80 */     return this.m_currentRowIndex + 1 < this.m_drset.m_numRows;
/*     */   }
/*     */ 
/*     */   public void remove()
/*     */   {
/*  85 */     this.m_drset.deleteRow(this.m_currentRowIndex);
/*  86 */     this.m_currentRow = ((List)this.m_values.get(this.m_currentRowIndex));
/*  87 */     this.m_currentRowIndex -= 1;
/*     */   }
/*     */ 
/*     */   public String get(String key)
/*     */   {
/*  99 */     int tmp = this.m_nextIndex;
/* 100 */     int lookupIndex = -1;
/* 101 */     if (this.m_keyList[tmp] == key)
/*     */     {
/* 104 */       lookupIndex = this.m_keyIndexes[tmp];
/* 105 */       if (++tmp == this.m_keyCount) tmp = 0;
/* 106 */       this.m_nextIndex = tmp;
/* 107 */       if (lookupIndex >= 0)
/*     */       {
/* 109 */         if (this.m_currentRowArray != null)
/*     */         {
/* 111 */           return (String)this.m_currentRowArray[lookupIndex];
/*     */         }
/* 113 */         return (String)this.m_currentRow.get(lookupIndex);
/*     */       }
/* 115 */       return null;
/*     */     }
/* 117 */     for (int i = 0; i < this.m_keyCount; ++i)
/*     */     {
/* 119 */       if (this.m_keyList[tmp] == key)
/*     */       {
/* 122 */         int finalIndex = this.m_keyIndexes[tmp];
/* 123 */         if (++tmp == this.m_keyCount) tmp = 0;
/* 124 */         this.m_nextIndex = tmp;
/* 125 */         if (finalIndex >= 0)
/*     */         {
/* 127 */           if (this.m_currentRowArray != null)
/*     */           {
/* 129 */             return (String)this.m_currentRowArray[finalIndex];
/*     */           }
/* 131 */           return (String)this.m_currentRow.get(finalIndex);
/*     */         }
/* 133 */         return null;
/*     */       }
/* 135 */       if (++tmp != this.m_keyCount) continue; tmp = 0;
/*     */     }
/* 137 */     for (int i = 0; i < this.m_keyCount; ++i)
/*     */     {
/* 139 */       if (this.m_keyList[tmp].equals(key))
/*     */       {
/* 143 */         this.m_keyList[tmp] = key;
/* 144 */         int finalIndex = this.m_keyIndexes[tmp];
/* 145 */         if (++tmp == this.m_keyCount) tmp = 0;
/* 146 */         this.m_nextIndex = tmp;
/* 147 */         if (finalIndex >= 0)
/*     */         {
/* 149 */           if (this.m_currentRowArray != null)
/*     */           {
/* 151 */             return (String)this.m_currentRowArray[finalIndex];
/*     */           }
/* 153 */           return (String)this.m_currentRow.get(finalIndex);
/*     */         }
/* 155 */         return null;
/*     */       }
/* 157 */       if (++tmp != this.m_keyCount) continue; tmp = 0;
/*     */     }
/*     */ 
/* 160 */     FieldInfo info = (FieldInfo)this.m_drset.m_fieldMapping.get(key);
/* 161 */     String value = null;
/* 162 */     if (info != null)
/*     */     {
/* 164 */       lookupIndex = info.m_index;
/* 165 */       value = (String)this.m_currentRow.get(lookupIndex);
/*     */     }
/*     */ 
/* 169 */     if (this.m_keyCount < this.m_keyList.length)
/*     */     {
/* 171 */       this.m_keyList[this.m_keyCount] = key;
/* 172 */       this.m_keyIndexes[this.m_keyCount] = lookupIndex;
/*     */ 
/* 175 */       this.m_keyCount += 1;
/*     */     }
/*     */     else
/*     */     {
/* 179 */       this.m_cacheMisses += 1;
/*     */     }
/*     */ 
/* 183 */     return value;
/*     */   }
/*     */ 
/*     */   public String getSystem(String key)
/*     */   {
/* 188 */     return get(key);
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 194 */     IdcStringBuilder builder = new IdcStringBuilder();
/* 195 */     builder.append("row ");
/* 196 */     builder.append(this.m_currentRowIndex);
/* 197 */     builder.append(" ");
/* 198 */     builder.append(this.m_currentRow.toString());
/* 199 */     return builder.toString();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 204 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 95548 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.data.DataResultSetIterableOnSimpleParameters
 * JD-Core Version:    0.5.4
 */