/*     */ package intradoc.shared;
/*     */ 
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class ResultData
/*     */ {
/*  31 */   protected Properties m_props = null;
/*  32 */   protected Properties m_values = null;
/*  33 */   protected Vector m_flexData = null;
/*     */ 
/*     */   public ResultData()
/*     */   {
/*  38 */     this.m_props = new Properties();
/*     */   }
/*     */ 
/*     */   public void setValues(Properties values)
/*     */   {
/*  43 */     this.m_props.clear();
/*     */ 
/*  45 */     if (values == null)
/*     */     {
/*  47 */       this.m_values = new Properties();
/*  48 */       this.m_values.put("formtype", "ResultsPage");
/*  49 */       this.m_values.put("description", "");
/*     */     }
/*     */     else
/*     */     {
/*  53 */       this.m_values = values;
/*  54 */       String str = this.m_values.getProperty("flexdata");
/*  55 */       parse(str);
/*     */     }
/*     */   }
/*     */ 
/*     */   public Properties getValues()
/*     */   {
/*  62 */     String str = formatString();
/*  63 */     this.m_values.put("flexdata", str);
/*     */ 
/*  65 */     return this.m_values;
/*     */   }
/*     */ 
/*     */   public void parse(String str)
/*     */   {
/*  72 */     Vector props = StringUtils.parseArray(str, '\t', '&');
/*     */ 
/*  74 */     int nprops = props.size();
/*  75 */     if (nprops == 0)
/*     */     {
/*  77 */       return;
/*     */     }
/*     */ 
/*  81 */     for (int i = 0; i < nprops; ++i)
/*     */     {
/*  83 */       String nameValueStr = (String)props.elementAt(i);
/*  84 */       Vector nameValue = StringUtils.parseArray(nameValueStr, ' ', '%');
/*     */ 
/*  86 */       String name = (String)nameValue.elementAt(0);
/*  87 */       String val = "";
/*  88 */       if (nameValue.size() > 1)
/*     */       {
/*  90 */         val = (String)nameValue.elementAt(1);
/*     */       }
/*     */ 
/*  93 */       this.m_props.put(name, val);
/*     */     }
/*     */   }
/*     */ 
/*     */   public String formatString()
/*     */   {
/* 100 */     Vector outList = new IdcVector();
/* 101 */     int size = this.m_props.size();
/* 102 */     outList.setSize(size);
/* 103 */     int count = 0;
/* 104 */     for (Enumeration en = this.m_props.propertyNames(); en.hasMoreElements(); ++count)
/*     */     {
/* 106 */       String key = (String)en.nextElement();
/* 107 */       String value = this.m_props.getProperty(key);
/* 108 */       setNameValueFormatAt(outList, count, key, value);
/*     */     }
/*     */ 
/* 112 */     String retVal = StringUtils.createString(outList, '\t', '&');
/* 113 */     return retVal;
/*     */   }
/*     */ 
/*     */   public void setNameValueFormatAt(Vector list, int index, String name, String value)
/*     */   {
/* 118 */     Vector nameValue = createNameValue(name, value);
/* 119 */     String nameValueStr = StringUtils.createString(nameValue, ' ', '%');
/* 120 */     list.setElementAt(nameValueStr, index);
/*     */   }
/*     */ 
/*     */   public Vector createNameValue(String name, String value)
/*     */   {
/* 125 */     Vector nameValue = new IdcVector();
/* 126 */     nameValue.setSize(2);
/* 127 */     nameValue.setElementAt(name, 0);
/* 128 */     nameValue.setElementAt(value, 1);
/* 129 */     return nameValue;
/*     */   }
/*     */ 
/*     */   public String get(String name)
/*     */   {
/* 134 */     if ((name.equals("name")) || (name.equals("description")))
/*     */     {
/* 136 */       return this.m_values.getProperty(name);
/*     */     }
/*     */ 
/* 140 */     String value = this.m_props.getProperty(name);
/* 141 */     if (value == null)
/*     */     {
/* 143 */       value = "";
/*     */     }
/* 145 */     return value;
/*     */   }
/*     */ 
/*     */   public void put(String name, String value)
/*     */   {
/* 150 */     if ((name.equals("name")) || (name.equals("description")))
/*     */     {
/* 152 */       this.m_values.put(name, value);
/*     */     }
/*     */     else
/*     */     {
/* 156 */       this.m_props.put(name, value);
/*     */     }
/*     */   }
/*     */ 
/*     */   public Properties getProps()
/*     */   {
/* 162 */     return this.m_props;
/*     */   }
/*     */ 
/*     */   protected String getFormatString(String field)
/*     */   {
/* 168 */     String newVal = "";
/*     */ 
/* 170 */     newVal = newVal + "<$";
/* 171 */     newVal = newVal + field.trim();
/* 172 */     newVal = newVal + "$>";
/*     */ 
/* 174 */     return newVal;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 180 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.ResultData
 * JD-Core Version:    0.5.4
 */