/*     */ package intradoc.shared.schema;
/*     */ 
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class SchemaFieldConfig extends SchemaResultSet
/*     */ {
/*     */   public static final int FIELD_NAME = 0;
/*     */   public static final int CANONICAL_NAME = 1;
/*     */   public static final int FIELD_LAST_LOADED = 2;
/*     */   public static final int FIELD_UPTODATE = 3;
/*     */   public static final int FIELD_SYSTEM = 4;
/*     */   public static final int FIELD_ORDER = 5;
/*  38 */   public static String[] FIELD_COLUMNS = { "schFieldName", "schCanonicalName", "schFieldLastLoaded", "schFieldIsUpToDate", "schIsSystemObject", "schOrder" };
/*     */ 
/*     */   public SchemaFieldConfig()
/*     */   {
/*  50 */     super("SchemaFieldData", FIELD_COLUMNS);
/*  51 */     this.m_infos = new FieldInfo[FIELD_COLUMNS.length];
/*  52 */     this.m_indexes = new int[FIELD_COLUMNS.length];
/*  53 */     for (int i = 0; i < FIELD_COLUMNS.length; ++i)
/*     */     {
/*  55 */       this.m_infos[i] = new FieldInfo();
/*  56 */       getIndexFieldInfo(i, this.m_infos[i]);
/*  57 */       this.m_indexes[i] = this.m_infos[i].m_index;
/*     */     }
/*     */   }
/*     */ 
/*     */   public DataResultSet shallowClone()
/*     */   {
/*  64 */     SchemaFieldConfig rset = new SchemaFieldConfig();
/*  65 */     initShallow(rset);
/*  66 */     return rset;
/*     */   }
/*     */ 
/*     */   public Vector fieldsUsingView(String key)
/*     */   {
/*  71 */     Vector fields = new IdcVector();
/*     */ 
/*  73 */     SchemaFieldConfig config = (SchemaFieldConfig)SharedObjects.getTable("SchemaFieldConfig");
/*     */ 
/*  75 */     SchemaHelper schHelper = new SchemaHelper();
/*  76 */     for (config.first(); config.isRowPresent(); config.next())
/*     */     {
/*  78 */       SchemaFieldData data = (SchemaFieldData)config.getData();
/*  79 */       boolean isOptionList = data.getBoolean("dIsOptionList", false);
/*  80 */       if (!isOptionList)
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/*  85 */       String[][] listMap = { { "UseOptionList", "OptionListKey" }, { "UseViewList", "OptionViewKey" }, { "UseTreeControl", "TreeDefinition" } };
/*     */ 
/*  92 */       for (int i = 0; i < listMap.length; ++i)
/*     */       {
/*  94 */         boolean isList = data.getBoolean(listMap[i][0], false);
/*  95 */         boolean addField = false;
/*  96 */         if (isList)
/*     */         {
/*  98 */           String list = data.get("OptionViewKey");
/*  99 */           if (i == 2)
/*     */           {
/* 101 */             String treeDefinition = data.get("TreeDefinition");
/* 102 */             String[] parsedList = schHelper.parseTreeDefinition(treeDefinition);
/* 103 */             if (schHelper.checkViewInParsedTreeDefinition(parsedList, key))
/*     */             {
/* 105 */               addField = true;
/*     */             }
/*     */           }
/* 108 */           else if (key.equals(list))
/*     */           {
/* 110 */             addField = true;
/*     */           }
/*     */         }
/* 113 */         if (!addField)
/*     */           continue;
/* 115 */         fields.addElement(data);
/* 116 */         break;
/*     */       }
/*     */     }
/*     */ 
/* 120 */     return fields;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 125 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.schema.SchemaFieldConfig
 * JD-Core Version:    0.5.4
 */