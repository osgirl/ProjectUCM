/*     */ package intradoc.data;
/*     */ 
/*     */ import java.util.HashMap;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class FieldInfo
/*     */ {
/*     */   public static final short BOOLEAN = 1;
/*     */   public static final short CHAR = 2;
/*     */   public static final short INT = 3;
/*     */   public static final short FLOAT = 4;
/*     */   public static final short DATE = 5;
/*     */   public static final short STRING = 6;
/*     */   public static final short BINARY = 7;
/*     */   public static final short MEMO = 8;
/*     */   public static final short BLOB = 9;
/*     */   public static final short CLOB = 10;
/*     */   public static final short DECIMAL = 11;
/*  87 */   public static final String[] FIELD_NAMES = { "", "Bool", "Char", "Int", "Float", "Date", "Varchar", "Binary", "Memo", "Blob", "Clob", "Decimal" };
/*     */   public int m_index;
/*     */   public String m_name;
/*     */   public int m_type;
/*     */   public boolean m_isFixedLen;
/*     */   public int m_maxLen;
/*     */   public int m_scale;
/*     */   public Map<String, String> m_additionalOptions;
/*     */ 
/*     */   public FieldInfo()
/*     */   {
/* 137 */     this.m_index = -1;
/* 138 */     this.m_name = null;
/* 139 */     this.m_type = 6;
/* 140 */     this.m_isFixedLen = false;
/* 141 */     this.m_maxLen = 0;
/* 142 */     this.m_scale = 0;
/*     */   }
/*     */ 
/*     */   public FieldInfo(CharSequence seq)
/*     */   {
/* 150 */     String str = seq.toString();
/* 151 */     int index1 = str.indexOf("[");
/* 152 */     if (index1 > 0)
/*     */     {
/* 154 */       int index2 = str.indexOf("]", index1);
/* 155 */       if (index2 > 0)
/*     */       {
/* 157 */         str = str.substring(0, index1) + str.substring(index2 + 1);
/*     */       }
/*     */     }
/*     */ 
/* 161 */     index1 = str.indexOf("(");
/* 162 */     if (index1 <= 0)
/*     */       return;
/* 164 */     this.m_name = str.substring(0, index1).trim();
/* 165 */     int index2 = str.indexOf(")", index1);
/* 166 */     if (index2 <= 0)
/*     */       return;
/* 168 */     String typeInfo = str.substring(index1 + 1, index2);
/* 169 */     index2 = typeInfo.indexOf(" ");
/* 170 */     if (index2 > 0)
/*     */     {
/* 172 */       String lengthStr = typeInfo.substring(index2 + 1).trim();
/* 173 */       this.m_maxLen = Integer.parseInt(lengthStr);
/* 174 */       this.m_isFixedLen = true;
/* 175 */       typeInfo = typeInfo.substring(0, index2).trim();
/*     */     }
/* 177 */     for (int i = 0; i < FIELD_NAMES.length; ++i)
/*     */     {
/* 179 */       if (!typeInfo.equalsIgnoreCase(FIELD_NAMES[i]))
/*     */         continue;
/* 181 */       this.m_type = i;
/* 182 */       return;
/*     */     }
/*     */   }
/*     */ 
/*     */   public void copy(FieldInfo info)
/*     */   {
/* 195 */     this.m_index = info.m_index;
/* 196 */     this.m_name = info.m_name;
/* 197 */     this.m_type = info.m_type;
/* 198 */     this.m_isFixedLen = info.m_isFixedLen;
/* 199 */     this.m_maxLen = info.m_maxLen;
/* 200 */     this.m_scale = info.m_scale;
/* 201 */     if (this.m_additionalOptions == null)
/*     */       return;
/* 203 */     this.m_additionalOptions = new HashMap();
/* 204 */     this.m_additionalOptions.putAll(info.m_additionalOptions);
/*     */   }
/*     */ 
/*     */   public boolean equals(Object obj)
/*     */   {
/* 215 */     FieldInfo info = (FieldInfo)obj;
/*     */ 
/* 224 */     return (info.m_name.equals(this.m_name)) && (info.m_type == this.m_type) && (info.m_isFixedLen == this.m_isFixedLen) && (info.m_maxLen == this.m_maxLen) && (info.m_scale == this.m_scale) && (info.m_index == this.m_index);
/*     */   }
/*     */ 
/*     */   public String getTypeName()
/*     */   {
/* 234 */     if ((this.m_type < 1) || (this.m_type > 11))
/*     */     {
/* 236 */       return null;
/*     */     }
/* 238 */     return FIELD_NAMES[this.m_type];
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 244 */     return this.m_name + "[" + this.m_index + "](" + getTypeName() + " " + this.m_maxLen + ")";
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 249 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 69804 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.data.FieldInfo
 * JD-Core Version:    0.5.4
 */