/*     */ package intradoc.data;
/*     */ 
/*     */ import intradoc.common.IdcDateFormat;
/*     */ import intradoc.common.StringUtils;
/*     */ import java.util.Date;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class AliasingResultSet
/*     */   implements ResultSet
/*     */ {
/*     */   protected ResultSet m_resultSet;
/*     */   protected Hashtable m_forwardMap;
/*     */   protected Hashtable m_reverseMap;
/*     */ 
/*     */   public void init(ResultSet parentSet, String mapString)
/*     */   {
/*  35 */     this.m_resultSet = parentSet;
/*     */ 
/*  37 */     Vector list = StringUtils.parseArray(mapString, ',', '^');
/*  38 */     this.m_forwardMap = new Hashtable();
/*  39 */     this.m_reverseMap = new Hashtable();
/*  40 */     for (int i = 0; i < list.size(); ++i)
/*     */     {
/*  42 */       String pairString = (String)list.elementAt(i);
/*  43 */       int index = pairString.indexOf(":");
/*  44 */       if (index <= 0)
/*     */         continue;
/*  46 */       String oldKey = pairString.substring(0, index);
/*  47 */       String newKey = pairString.substring(index + 1);
/*  48 */       this.m_forwardMap.put(oldKey, newKey);
/*  49 */       this.m_reverseMap.put(newKey, oldKey);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void init(ResultSet parentSet, Hashtable map)
/*     */   {
/*  56 */     this.m_resultSet = parentSet;
/*  57 */     this.m_forwardMap = map;
/*  58 */     this.m_reverseMap = new Hashtable();
/*  59 */     Enumeration en = map.keys();
/*  60 */     while (en.hasMoreElements())
/*     */     {
/*  62 */       String key = (String)en.nextElement();
/*  63 */       String newKey = (String)map.get(key);
/*  64 */       this.m_reverseMap.put(newKey, key);
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean isMutable()
/*     */   {
/*  70 */     return false;
/*     */   }
/*     */ 
/*     */   public boolean hasRawObjects()
/*     */   {
/*  75 */     return this.m_resultSet.hasRawObjects();
/*     */   }
/*     */ 
/*     */   public int getNumFields()
/*     */   {
/*  80 */     return this.m_resultSet.getNumFields();
/*     */   }
/*     */ 
/*     */   public boolean isEmpty()
/*     */   {
/*  85 */     return this.m_resultSet.isEmpty();
/*     */   }
/*     */ 
/*     */   public boolean isRowPresent()
/*     */   {
/*  90 */     return this.m_resultSet.isRowPresent();
/*     */   }
/*     */ 
/*     */   public String getFieldName(int index)
/*     */   {
/*  95 */     String name = this.m_resultSet.getFieldName(index);
/*  96 */     String newName = (String)this.m_forwardMap.get(name);
/*  97 */     if (newName != null)
/*     */     {
/*  99 */       return newName;
/*     */     }
/* 101 */     return name;
/*     */   }
/*     */ 
/*     */   public boolean getFieldInfo(String fieldName, FieldInfo fieldInfo)
/*     */   {
/* 106 */     String name = (String)this.m_reverseMap.get(fieldName);
/* 107 */     if (name == null)
/*     */     {
/* 109 */       name = fieldName;
/*     */     }
/* 111 */     boolean rc = this.m_resultSet.getFieldInfo(name, fieldInfo);
/*     */ 
/* 117 */     if (!rc)
/*     */     {
/* 119 */       rc = this.m_resultSet.getFieldInfo(fieldName, fieldInfo);
/*     */     }
/*     */ 
/* 122 */     fieldInfo.m_name = fieldName;
/* 123 */     return rc;
/*     */   }
/*     */ 
/*     */   public int getFieldInfoIndex(String fieldName)
/*     */   {
/* 134 */     String name = (String)this.m_reverseMap.get(fieldName);
/* 135 */     if (name == null)
/*     */     {
/* 137 */       name = fieldName;
/*     */     }
/*     */ 
/* 140 */     int index = this.m_resultSet.getFieldInfoIndex(name);
/*     */ 
/* 146 */     if (index < 0)
/*     */     {
/* 148 */       index = this.m_resultSet.getFieldInfoIndex(fieldName);
/*     */     }
/*     */ 
/* 151 */     return index;
/*     */   }
/*     */ 
/*     */   public void getIndexFieldInfo(int index, FieldInfo fieldInfo)
/*     */   {
/* 156 */     String name = getFieldName(index);
/* 157 */     getFieldInfo(name, fieldInfo);
/*     */   }
/*     */ 
/*     */   public String getStringValue(int index)
/*     */   {
/* 162 */     return this.m_resultSet.getStringValue(index);
/*     */   }
/*     */ 
/*     */   public Date getDateValue(int index)
/*     */   {
/* 167 */     return this.m_resultSet.getDateValue(index);
/*     */   }
/*     */ 
/*     */   public String getStringValueByName(String name)
/*     */   {
/* 172 */     String oldName = (String)this.m_reverseMap.get(name);
/* 173 */     String val = this.m_resultSet.getStringValueByName(oldName);
/*     */ 
/* 179 */     if (val == null)
/*     */     {
/* 181 */       val = this.m_resultSet.getStringValueByName(name);
/*     */     }
/*     */ 
/* 184 */     return val;
/*     */   }
/*     */ 
/*     */   public Date getDateValueByName(String name)
/*     */   {
/* 189 */     return this.m_resultSet.getDateValueByName(name);
/*     */   }
/*     */ 
/*     */   public void setDateFormat(IdcDateFormat fmt)
/*     */   {
/* 194 */     this.m_resultSet.setDateFormat(fmt);
/*     */   }
/*     */ 
/*     */   public IdcDateFormat getDateFormat()
/*     */   {
/* 199 */     return this.m_resultSet.getDateFormat();
/*     */   }
/*     */ 
/*     */   public boolean next()
/*     */   {
/* 204 */     return this.m_resultSet.next();
/*     */   }
/*     */ 
/*     */   public boolean first()
/*     */   {
/* 209 */     return this.m_resultSet.first();
/*     */   }
/*     */ 
/*     */   public int skip(int numRows)
/*     */   {
/* 214 */     return this.m_resultSet.skip(numRows);
/*     */   }
/*     */ 
/*     */   public void closeInternals()
/*     */   {
/* 219 */     this.m_resultSet.closeInternals();
/*     */   }
/*     */ 
/*     */   public boolean canRenameFields()
/*     */   {
/* 224 */     return false;
/*     */   }
/*     */ 
/*     */   public boolean renameField(String from, String to)
/*     */   {
/* 229 */     return false;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 234 */     return "releaseInfo=dev,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.data.AliasingResultSet
 * JD-Core Version:    0.5.4
 */