/*     */ package intradoc.common;
/*     */ 
/*     */ public class SortOptions
/*     */   implements IdcDebugOutput
/*     */ {
/*     */   public String m_sortKey;
/*  30 */   public int m_sortColIndex = -1;
/*     */   public String m_sortType;
/*     */   public String m_sortOrder;
/*     */   public boolean m_isTreeSort;
/*  51 */   public int m_childSortColIndex = -1;
/*     */   public String m_childSortKey;
/*  62 */   public int m_parentSortColIndex = -1;
/*     */   public String m_parentSortKey;
/*  72 */   public int m_sortNestLevelColIndex = -1;
/*     */   public String m_sortNestLevelKey;
/*     */ 
/*     */   public SortOptions cloneOptions()
/*     */   {
/*  93 */     SortOptions newOptions = new SortOptions();
/*  94 */     newOptions.m_sortKey = this.m_sortKey;
/*  95 */     newOptions.m_sortColIndex = this.m_sortColIndex;
/*  96 */     newOptions.m_sortType = this.m_sortType;
/*  97 */     newOptions.m_sortOrder = this.m_sortOrder;
/*  98 */     newOptions.m_isTreeSort = this.m_isTreeSort;
/*  99 */     newOptions.m_parentSortColIndex = this.m_parentSortColIndex;
/* 100 */     newOptions.m_parentSortKey = this.m_parentSortKey;
/* 101 */     newOptions.m_childSortColIndex = this.m_childSortColIndex;
/* 102 */     newOptions.m_childSortKey = this.m_childSortKey;
/* 103 */     newOptions.m_sortNestLevelColIndex = this.m_sortNestLevelColIndex;
/* 104 */     newOptions.m_sortNestLevelKey = this.m_sortNestLevelKey;
/* 105 */     return newOptions;
/*     */   }
/*     */ 
/*     */   public void mergeIfNewIsNotNull(SortOptions newOptions)
/*     */   {
/* 113 */     if (newOptions.m_sortKey != null)
/*     */     {
/* 115 */       this.m_sortKey = newOptions.m_sortKey;
/*     */     }
/* 117 */     if (newOptions.m_sortType != null)
/*     */     {
/* 119 */       this.m_sortType = newOptions.m_sortType;
/*     */     }
/* 121 */     if (newOptions.m_sortOrder != null)
/*     */     {
/* 123 */       this.m_sortOrder = newOptions.m_sortOrder;
/*     */     }
/* 125 */     if (newOptions.m_isTreeSort)
/*     */     {
/* 127 */       this.m_isTreeSort = newOptions.m_isTreeSort;
/*     */     }
/* 129 */     if (newOptions.m_parentSortKey != null)
/*     */     {
/* 131 */       this.m_parentSortKey = newOptions.m_parentSortKey;
/*     */     }
/* 133 */     if (newOptions.m_childSortKey != null)
/*     */     {
/* 135 */       this.m_childSortKey = newOptions.m_childSortKey;
/*     */     }
/* 137 */     if (newOptions.m_sortNestLevelKey == null)
/*     */       return;
/* 139 */     this.m_sortNestLevelKey = newOptions.m_sortNestLevelKey;
/*     */   }
/*     */ 
/*     */   public void setOption(String optionName, String optionValue)
/*     */   {
/* 151 */     String testVal = optionName.toLowerCase();
/* 152 */     if (testVal.equals("sortcolumn"))
/*     */     {
/* 154 */       this.m_sortKey = optionValue;
/*     */     }
/* 156 */     else if (testVal.equals("sorttype"))
/*     */     {
/* 158 */       this.m_sortType = optionValue;
/*     */     }
/* 160 */     else if (testVal.equals("sortorder"))
/*     */     {
/* 162 */       this.m_sortOrder = optionValue;
/*     */     }
/* 164 */     else if (testVal.equals("sortistree"))
/*     */     {
/* 166 */       this.m_isTreeSort = StringUtils.convertToBool(optionValue, true);
/*     */     }
/* 168 */     else if (testVal.equals("sortchildcolumn"))
/*     */     {
/* 170 */       this.m_childSortKey = optionValue;
/*     */     }
/* 172 */     else if (testVal.equals("sortparentcolumn"))
/*     */     {
/* 174 */       this.m_parentSortKey = optionValue;
/*     */     }
/* 176 */     else if (testVal.equals("sortnestlevelcolumn"))
/*     */     {
/* 178 */       this.m_sortNestLevelKey = optionValue;
/*     */     } else {
/* 180 */       if (!SystemUtils.m_verbose)
/*     */         return;
/* 182 */       Report.debug("system", "Set unrecognized sort option " + optionName + " and value " + optionValue, null);
/*     */     }
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 189 */     IdcStringBuilder builder = new IdcStringBuilder(200);
/* 190 */     appendDebugFormat(builder);
/* 191 */     return builder.toString();
/*     */   }
/*     */ 
/*     */   public void appendDebugFormat(IdcAppendable appendable)
/*     */   {
/* 196 */     appendPresentableValue(appendable, "sortColumn", this.m_sortKey);
/* 197 */     appendPresentableValue(appendable, "sortColumnIndex", "" + this.m_sortColIndex);
/* 198 */     appendPresentableValue(appendable, "sortType", this.m_sortType);
/* 199 */     appendPresentableValue(appendable, "sortOrder", this.m_sortOrder);
/* 200 */     if (!this.m_isTreeSort)
/*     */       return;
/* 202 */     appendable.append("\nIsTreeSort: ");
/* 203 */     appendPresentableValue(appendable, "sortParentColumn", this.m_parentSortKey);
/* 204 */     appendPresentableValue(appendable, "sortParentColumnIndex", "" + this.m_parentSortColIndex);
/* 205 */     appendPresentableValue(appendable, "sortChildColumn", this.m_childSortKey);
/* 206 */     appendPresentableValue(appendable, "sortChildColumnIndex", "" + this.m_childSortColIndex);
/* 207 */     appendPresentableValue(appendable, "sortNestLevelColumn", this.m_sortNestLevelKey);
/* 208 */     appendPresentableValue(appendable, "sortNestLevelColumnIndex", "" + this.m_sortNestLevelColIndex);
/*     */   }
/*     */ 
/*     */   public void appendPresentableValue(IdcAppendable appendable, String key, String val)
/*     */   {
/* 214 */     if (val == null)
/*     */     {
/* 216 */       val = "(null)";
/*     */     }
/* 218 */     else if (val.length() == 0)
/*     */     {
/* 220 */       val = "(empty)";
/*     */     }
/* 222 */     appendable.append(',');
/*     */ 
/* 224 */     appendable.append(key);
/* 225 */     appendable.append('=');
/* 226 */     appendable.append(val);
/*     */   }
/*     */ 
/*     */   public boolean computeColumnIndices(String[] names)
/*     */   {
/* 237 */     this.m_sortColIndex = StringUtils.findStringIndexEx(names, this.m_sortKey, true);
/* 238 */     boolean validSortConfig = this.m_sortColIndex >= 0;
/* 239 */     if (this.m_isTreeSort)
/*     */     {
/* 241 */       this.m_childSortColIndex = StringUtils.findStringIndexEx(names, this.m_childSortKey, true);
/* 242 */       this.m_parentSortColIndex = StringUtils.findStringIndexEx(names, this.m_parentSortKey, true);
/* 243 */       this.m_sortNestLevelColIndex = StringUtils.findStringIndexEx(names, this.m_sortNestLevelKey, true);
/* 244 */       validSortConfig = (this.m_childSortColIndex >= 0) && (this.m_parentSortColIndex >= 0);
/*     */     }
/*     */ 
/* 247 */     return validSortConfig;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 253 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.SortOptions
 * JD-Core Version:    0.5.4
 */