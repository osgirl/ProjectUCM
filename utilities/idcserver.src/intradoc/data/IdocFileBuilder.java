/*     */ package intradoc.data;
/*     */ 
/*     */ import intradoc.common.DynamicData;
/*     */ import intradoc.common.DynamicHtmlMerger;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.ParseSyntaxException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.io.Closeable;
/*     */ import java.io.IOException;
/*     */ import java.io.UnsupportedEncodingException;
/*     */ import java.io.Writer;
/*     */ 
/*     */ public class IdocFileBuilder
/*     */   implements Closeable
/*     */ {
/*     */   public boolean m_alignDynamicDataColumns;
/*     */   public DataBinder m_binder;
/*     */   public DynamicHtmlMerger m_merger;
/*     */   public Writer m_output;
/*     */ 
/*     */   public void init(DataBinder binder, DynamicHtmlMerger merger, String filename)
/*     */     throws IOException, UnsupportedEncodingException
/*     */   {
/*  61 */     String charsetName = null;
/*  62 */     if (binder != null)
/*     */     {
/*  64 */       charsetName = binder.m_javaEncoding;
/*     */     }
/*  66 */     if (charsetName == null)
/*     */     {
/*  68 */       charsetName = FileUtils.m_javaSystemEncoding;
/*     */     }
/*  70 */     this.m_output = FileUtils.openDataWriter(filename, charsetName, 0);
/*  71 */     this.m_binder = binder;
/*  72 */     this.m_merger = merger;
/*  73 */     appendHeader(charsetName);
/*     */   }
/*     */ 
/*     */   public void appendHeader(String charsetName)
/*     */     throws IOException
/*     */   {
/*  84 */     this.m_output.append("<html>\n<head>\n<meta http-equiv=\"Content-Type\" content=\"text/html; charset=");
/*  85 */     this.m_output.append(charsetName);
/*  86 */     this.m_output.append("\">\n</head>\n<body>\n\n[[%\n\tThis file was generated automatically.\n%]]\n\n");
/*     */   }
/*     */ 
/*     */   public void appendDynamicData(String tableName, DataResultSet drset)
/*     */     throws IOException
/*     */   {
/*  98 */     int numFields = drset.getNumFields();
/*  99 */     int[] fieldLengths = new int[numFields];
/* 100 */     if (this.m_alignDynamicDataColumns)
/*     */     {
/* 102 */       for (int f = numFields - 2; f >= 0; --f)
/*     */       {
/* 104 */         String fieldName = drset.getFieldName(f);
/* 105 */         fieldLengths[f] = fieldName.length();
/*     */       }
/* 107 */       for (drset.first(); drset.isRowPresent(); drset.next())
/*     */       {
/* 109 */         for (int f = numFields - 2; f >= 0; --f)
/*     */         {
/* 111 */           String value = drset.getStringValue(f);
/* 112 */           String escaped = StringUtils.addEscapeChars(value, ',', '^');
/* 113 */           int length = escaped.length();
/* 114 */           if (length <= fieldLengths[f])
/*     */             continue;
/* 116 */           fieldLengths[f] = length;
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 121 */     this.m_output.append("<@dynamicdata ");
/* 122 */     this.m_output.append(tableName);
/* 123 */     this.m_output.append("@>\n<?commatable mergeRule=\"replace\"?>\n");
/* 124 */     int pad = 0;
/* 125 */     for (int f = 0; f < numFields; ++f)
/*     */     {
/* 127 */       String fieldName = drset.getFieldName(f);
/* 128 */       int length = fieldName.length();
/* 129 */       if (f > 0)
/*     */       {
/* 131 */         this.m_output.append(',');
/* 132 */         while (pad-- > 0)
/*     */         {
/* 134 */           this.m_output.append(' ');
/*     */         }
/*     */       }
/* 137 */       this.m_output.append(fieldName);
/* 138 */       pad = 1 + (fieldLengths[f] - length);
/*     */     }
/* 140 */     this.m_output.append("\n");
/* 141 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*     */     {
/* 143 */       for (int f = 0; f < numFields; ++f)
/*     */       {
/* 145 */         String value = drset.getStringValue(f);
/* 146 */         String escaped = StringUtils.addEscapeChars(value, ',', '^');
/* 147 */         int length = escaped.length();
/* 148 */         if (f > 0)
/*     */         {
/* 150 */           this.m_output.append(',');
/* 151 */           while (pad-- > 0)
/*     */           {
/* 153 */             this.m_output.append(' ');
/*     */           }
/*     */         }
/* 156 */         this.m_output.append(escaped);
/* 157 */         pad = 1 + (fieldLengths[f] - length);
/*     */       }
/* 159 */       this.m_output.append('\n');
/*     */     }
/* 161 */     this.m_output.append("<@end@>\n\n");
/*     */   }
/*     */ 
/*     */   public void appendTable(String tableName, DataResultSet drset)
/*     */     throws IOException
/*     */   {
/* 173 */     int numFields = drset.getNumFields();
/* 174 */     this.m_output.append("<@table ");
/* 175 */     this.m_output.append(tableName);
/* 176 */     this.m_output.append("@>\n<table>\n\t<tr>\n");
/* 177 */     for (int f = 0; f < numFields; ++f)
/*     */     {
/* 179 */       String fieldName = drset.getFieldName(f);
/* 180 */       this.m_output.append("\t\t<td>");
/* 181 */       this.m_output.append(fieldName);
/* 182 */       this.m_output.append("</td>\n");
/*     */     }
/* 184 */     this.m_output.append("\t</tr>\n");
/* 185 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*     */     {
/* 187 */       this.m_output.append("\t<tr>\n");
/* 188 */       for (int f = 0; f < numFields; ++f)
/*     */       {
/* 190 */         String value = drset.getStringValue(f);
/* 191 */         this.m_output.append("\t\t<td>");
/* 192 */         this.m_output.append(value);
/* 193 */         this.m_output.append("</td>\n");
/*     */       }
/* 195 */       this.m_output.append("\t</tr>\n");
/*     */     }
/* 197 */     this.m_output.append("</table>\n<@end@>\n\n");
/*     */   }
/*     */ 
/*     */   public void appendResourcesList(String listName)
/*     */     throws DataException, IOException
/*     */   {
/* 213 */     DataResultSet resources = lookupTableMustExist(listName);
/* 214 */     int indexType = resources.getFieldInfoIndex("resourceType");
/* 215 */     if (indexType < 0)
/*     */     {
/* 217 */       IdcMessage msg = new IdcMessage("csColumnNotFound", new Object[] { "resourceType", listName });
/* 218 */       throw new DataException(null, msg);
/*     */     }
/* 220 */     int indexName = resources.getFieldInfoIndex("resourceName");
/* 221 */     if (indexName < 0)
/*     */     {
/* 223 */       IdcMessage msg = new IdcMessage("csColumnNotFound", new Object[] { "resourceName", listName });
/* 224 */       throw new DataException(null, msg);
/*     */     }
/*     */ 
/* 227 */     for (resources.first(); resources.isRowPresent(); resources.next())
/*     */     {
/* 229 */       String type = resources.getStringValue(indexType);
/* 230 */       String name = resources.getStringValue(indexName);
/* 231 */       if (type.equals("dynamicdata"))
/*     */       {
/* 233 */         DataResultSet table = lookupTableMustExist(name);
/* 234 */         appendDynamicData(name, table);
/*     */       }
/* 236 */       else if (type.equals("table"))
/*     */       {
/* 238 */         DataResultSet table = lookupTableMustExist(name);
/* 239 */         appendTable(name, table);
/*     */       }
/*     */       else
/*     */       {
/* 243 */         IdcMessage msg = new IdcMessage("csResourceContainerUnknownTag", new Object[0]);
/* 244 */         throw new DataException(null, msg);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void appendFooter()
/*     */     throws IOException
/*     */   {
/* 256 */     this.m_output.append("\n</body>\n</html>\n");
/*     */   }
/*     */ 
/*     */   public void close()
/*     */     throws IOException
/*     */   {
/* 265 */     appendFooter();
/* 266 */     this.m_output.close();
/* 267 */     this.m_output = null;
/*     */   }
/*     */ 
/*     */   public DataResultSet lookupTableMustExist(String tableName)
/*     */     throws DataException
/*     */   {
/* 283 */     if (this.m_binder != null)
/*     */     {
/* 285 */       ResultSet rset = this.m_binder.getResultSet(tableName);
/* 286 */       if ((rset != null) && (rset instanceof DataResultSet))
/*     */       {
/* 288 */         return (DataResultSet)rset;
/*     */       }
/*     */     }
/* 291 */     DataResultSet drset = SharedObjects.getTable(tableName);
/* 292 */     if (drset != null)
/*     */     {
/* 294 */       return drset;
/*     */     }
/* 296 */     IdcMessage msg = new IdcMessage("csUnableToFindTable", new Object[] { tableName });
/*     */     try
/*     */     {
/* 299 */       if (this.m_merger == null)
/*     */       {
/* 301 */         throw new DataException(null, msg);
/*     */       }
/* 303 */       DynamicData dd = this.m_merger.getDynamicDataResource(tableName, null);
/* 304 */       if (dd == null)
/*     */       {
/* 306 */         throw new DataException(null, msg);
/*     */       }
/* 308 */       drset = new DataResultSet();
/* 309 */       drset.init(dd.m_mergedTable);
/* 310 */       return drset;
/*     */     }
/*     */     catch (ParseSyntaxException pse)
/*     */     {
/* 314 */       throw new DataException(pse, msg);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 320 */     return "releaseInfo=dev,releaseRevision=$Rev: 81424 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.data.IdocFileBuilder
 * JD-Core Version:    0.5.4
 */