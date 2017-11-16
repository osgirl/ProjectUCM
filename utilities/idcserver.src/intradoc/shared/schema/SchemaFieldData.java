/*     */ package intradoc.shared.schema;
/*     */ 
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import java.util.ArrayList;
/*     */ 
/*     */ public class SchemaFieldData extends SchemaData
/*     */ {
/*     */   public SchemaFieldData()
/*     */   {
/*  31 */     this.m_localDataCruftKeys.add("AllowMissing");
/*  32 */     this.m_resultSetCruftKeys.add("QmetaDef");
/*     */   }
/*     */ 
/*     */   public String getNameField()
/*     */   {
/*  38 */     return "schFieldName";
/*     */   }
/*     */ 
/*     */   public String getTimestampField()
/*     */   {
/*  44 */     return "schFieldLastLoaded";
/*     */   }
/*     */ 
/*     */   public String getIsUpToDateField()
/*     */   {
/*  50 */     return "schFieldIsUpToDate";
/*     */   }
/*     */ 
/*     */   protected void initIndexes()
/*     */   {
/*  56 */     super.initIndexes();
/*     */   }
/*     */ 
/*     */   public void update(DataBinder binder)
/*     */     throws DataException
/*     */   {
/*  67 */     super.update(binder);
/*     */   }
/*     */ 
/*     */   public void updateEx(DataBinder binder)
/*     */   {
/*  73 */     super.updateEx(binder);
/*  74 */     if (!getBoolean("UseViewList", false))
/*     */     {
/*  76 */       String optionListKey = get("OptionListKey");
/*  77 */       if (optionListKey != null)
/*     */       {
/*  79 */         this.m_data.putLocal("OptionViewKey", optionListKey);
/*     */       }
/*     */       else
/*     */       {
/*  83 */         this.m_data.removeLocal("OptionViewKey");
/*     */       }
/*     */     }
/*     */ 
/*  87 */     String sep = get("MultiselectStorageSeparator");
/*  88 */     if ((!getBoolean("PadMultiselectStorage", false)) || (sep == null))
/*     */       return;
/*  90 */     while (sep.startsWith(" "))
/*     */     {
/*  92 */       sep = sep.substring(1);
/*     */     }
/*  94 */     int index = -1;
/*  95 */     while ((index = sep.lastIndexOf(" ")) == sep.length() - 1)
/*     */     {
/*  97 */       sep = sep.substring(0, index);
/*     */     }
/*  99 */     this.m_data.putLocal("MultiselectStorageSeparator", sep);
/*     */   }
/*     */ 
/*     */   public void populateBinder(DataBinder binder)
/*     */   {
/* 106 */     super.populateBinder(binder);
/*     */   }
/*     */ 
/*     */   public void removeServerCruft(DataBinder data)
/*     */   {
/* 112 */     super.removeServerCruft(data);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 117 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 80705 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.schema.SchemaFieldData
 * JD-Core Version:    0.5.4
 */