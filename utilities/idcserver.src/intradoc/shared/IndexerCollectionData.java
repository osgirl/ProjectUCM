/*     */ package intradoc.shared;
/*     */ 
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class IndexerCollectionData
/*     */ {
/*  32 */   public Hashtable m_fieldInfos = null;
/*     */ 
/*  36 */   public Hashtable m_securityInfos = null;
/*     */ 
/*  39 */   public Hashtable m_fieldDesignMap = null;
/*     */ 
/*  42 */   public Hashtable m_fieldStates = null;
/*     */ 
/*  45 */   public DataBinder m_binder = null;
/*     */ 
/*     */   public IndexerCollectionData()
/*     */   {
/*  49 */     this.m_fieldInfos = new Hashtable();
/*  50 */     this.m_securityInfos = new Hashtable();
/*  51 */     this.m_fieldDesignMap = new Hashtable();
/*  52 */     this.m_fieldStates = new Hashtable();
/*  53 */     this.m_binder = new DataBinder();
/*     */   }
/*     */ 
/*     */   public void shallowClone(IndexerCollectionData data)
/*     */   {
/*  58 */     this.m_fieldInfos = ((Hashtable)data.m_fieldInfos.clone());
/*  59 */     this.m_securityInfos = ((Hashtable)data.m_securityInfos.clone());
/*  60 */     this.m_fieldDesignMap = ((Hashtable)data.m_fieldDesignMap.clone());
/*  61 */     this.m_binder = data.m_binder.createShallowCopy();
/*     */   }
/*     */ 
/*     */   public DataBinder prepareBinderForSerialization()
/*     */   {
/*  66 */     DataBinder binder = this.m_binder.createShallowCopyCloneResultSets();
/*  67 */     IdcStringBuilder builder = new IdcStringBuilder();
/*  68 */     Enumeration en = this.m_securityInfos.keys();
/*  69 */     boolean isFirst = true;
/*  70 */     while (en.hasMoreElements())
/*     */     {
/*  72 */       String key = (String)en.nextElement();
/*  73 */       if (isFirst)
/*     */       {
/*  75 */         isFirst = false;
/*     */       }
/*     */       else
/*     */       {
/*  79 */         builder.append(',');
/*     */       }
/*  81 */       builder.append(key);
/*     */     }
/*  83 */     String zoneFields = builder.toString();
/*  84 */     binder.putLocal("ZonedSecurityFields", zoneFields);
/*     */ 
/*  86 */     DataResultSet drset = new DataResultSet(new String[] { "SearchFieldName", "SearchFieldType", "FieldAttributes", "IsSortable", "IndexDisabled", "SearchDisabled" });
/*  87 */     en = this.m_fieldInfos.elements();
/*  88 */     while (en.hasMoreElements())
/*     */     {
/*  90 */       FieldInfo fi = (FieldInfo)en.nextElement();
/*  91 */       Properties props = (Properties)this.m_fieldDesignMap.get(fi.m_name);
/*  92 */       Enumeration em = props.propertyNames();
/*  93 */       isFirst = true;
/*  94 */       builder = new IdcStringBuilder();
/*     */ 
/*  99 */       while (em.hasMoreElements())
/*     */       {
/* 101 */         String name = (String)em.nextElement();
/* 102 */         if (name.equalsIgnoreCase("fieldAttributes"))
/*     */         {
/* 104 */           Report.trace(null, "fieldAttributes appeared in design map", null);
/*     */         }
/*     */ 
/* 107 */         String value = props.getProperty(name);
/* 108 */         name = StringUtils.addEscapeChars(name, ':', '^');
/* 109 */         value = StringUtils.addEscapeChars(value, ':', '^');
/* 110 */         if (isFirst)
/*     */         {
/* 112 */           isFirst = false;
/*     */         }
/*     */         else
/*     */         {
/* 116 */           builder.append('\n');
/*     */         }
/* 118 */         builder.append(name);
/* 119 */         builder.append(':');
/* 120 */         builder.append(value);
/*     */       }
/* 122 */       String tmp = props.getProperty("IsSortable");
/* 123 */       boolean isSortable = StringUtils.convertToBool(tmp, false);
/* 124 */       Vector row = new IdcVector();
/* 125 */       row.add(fi.m_name);
/* 126 */       row.add(fi.getTypeName());
/* 127 */       row.add(builder.toString());
/* 128 */       row.add("" + isSortable);
/* 129 */       row.add("");
/* 130 */       row.add("");
/* 131 */       drset.addRow(row);
/*     */     }
/* 133 */     binder.addResultSet("SearchFieldInfo", drset);
/* 134 */     return binder;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 139 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.IndexerCollectionData
 * JD-Core Version:    0.5.4
 */