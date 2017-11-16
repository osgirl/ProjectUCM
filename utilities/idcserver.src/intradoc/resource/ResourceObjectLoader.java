/*     */ package intradoc.resource;
/*     */ 
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Table;
/*     */ import intradoc.data.DataException;
/*     */ import java.util.List;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class ResourceObjectLoader
/*     */ {
/*     */   public static void loadTableResource(Table tble, String[] fields, String tableName, ResourceCreator rc)
/*     */     throws DataException
/*     */   {
/*  35 */     int[] indices = createTableColumnIndexList(tble, fields, tableName);
/*     */ 
/*  38 */     List rows = tble.m_rows;
/*  39 */     int nrows = rows.size();
/*  40 */     for (int k = 0; k < nrows; ++k)
/*     */     {
/*  42 */       String[] cols = (String[])(String[])rows.get(k);
/*  43 */       rc.createResourceObject(cols, indices);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static int[] createTableColumnIndexList(Table tble, String[] fields, String tableName)
/*     */     throws DataException
/*     */   {
/*  50 */     int[] indices = new int[fields.length];
/*  51 */     String[] colNames = tble.m_colNames;
/*     */ 
/*  53 */     for (int i = 0; i < fields.length; ++i)
/*     */     {
/*  55 */       boolean foundIt = false;
/*  56 */       for (int j = 0; j < colNames.length; ++j)
/*     */       {
/*  58 */         if (!colNames[j].equalsIgnoreCase(fields[i]))
/*     */           continue;
/*  60 */         foundIt = true;
/*  61 */         indices[i] = j;
/*  62 */         break;
/*     */       }
/*     */ 
/*  65 */       if (foundIt)
/*     */         continue;
/*  67 */       throw new DataException(LocaleUtils.encodeMessage("csResourceObjLoaderColumnDefNotFound", null, fields[i], tableName));
/*     */     }
/*     */ 
/*  72 */     return indices;
/*     */   }
/*     */ 
/*     */   public static String parseStringValue(Vector v, int index)
/*     */   {
/*  77 */     String str = (String)v.elementAt(index);
/*  78 */     str = stripHtml(str);
/*  79 */     if (str.equalsIgnoreCase("null"))
/*     */     {
/*  81 */       return null;
/*     */     }
/*  83 */     return str;
/*     */   }
/*     */ 
/*     */   public static int parseIntValue(Vector v, int index)
/*     */   {
/*  88 */     String str = (String)v.elementAt(index);
/*  89 */     str = stripHtml(str);
/*  90 */     return Integer.parseInt(str);
/*     */   }
/*     */ 
/*     */   public static String stripHtml(String str)
/*     */   {
/*  95 */     str = str.trim();
/*  96 */     int len = str.length();
/*  97 */     if (len >= 4)
/*     */     {
/*  99 */       String temp = str.substring(len - 4);
/* 100 */       if (temp.equalsIgnoreCase("<br>"))
/*     */       {
/* 102 */         str = str.substring(0, len - 4);
/*     */       }
/*     */     }
/* 105 */     return str;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 110 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.resource.ResourceObjectLoader
 * JD-Core Version:    0.5.4
 */