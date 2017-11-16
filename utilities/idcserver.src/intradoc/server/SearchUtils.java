/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.IdcDateFormat;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.util.Date;
/*     */ import java.util.Iterator;
/*     */ import java.util.Map;
/*     */ import java.util.Set;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class SearchUtils
/*     */ {
/*     */   public static boolean isLocalCollectionID(DataBinder binder)
/*     */   {
/*  34 */     String sid = binder.getAllowMissing("sCollectionID");
/*  35 */     boolean isLocalCollectionID = true;
/*     */     try
/*     */     {
/*  40 */       if ((sid != null) && (sid.length() > 0))
/*     */       {
/*  42 */         DataResultSet drset = SharedObjects.getTable("SearchCollections");
/*  43 */         if (drset != null)
/*     */         {
/*  45 */           FieldInfo[] info = ResultSetUtils.createInfoList(drset, new String[] { "sCollectionID", "sProfile" }, true);
/*     */ 
/*  47 */           Vector row = drset.findRow(info[0].m_index, sid);
/*  48 */           if (row != null)
/*     */           {
/*  50 */             String profile = (String)row.elementAt(info[1].m_index);
/*  51 */             if ((profile != null) && (!profile.equalsIgnoreCase("local")))
/*     */             {
/*  53 */               isLocalCollectionID = false;
/*     */             }
/*     */           }
/*     */         }
/*     */       }
/*     */     }
/*     */     catch (DataException ignore)
/*     */     {
/*  61 */       if (SystemUtils.m_verbose)
/*     */       {
/*  63 */         Report.debug("indexer", null, ignore);
/*     */       }
/*     */     }
/*  66 */     return isLocalCollectionID;
/*     */   }
/*     */ 
/*     */   public static boolean loadCollectionInfo(String collectionID, DataBinder binder)
/*     */   {
/*  71 */     boolean foundIt = false;
/*  72 */     DataResultSet drset = null;
/*  73 */     String currentCollection = binder.getAllowMissing("currentCollectionID");
/*     */     try
/*     */     {
/*  76 */       if ((collectionID != null) && (collectionID.length() > 0))
/*     */       {
/*  78 */         if ((currentCollection != null) && (currentCollection.equals(collectionID)))
/*     */         {
/*  80 */           return true;
/*     */         }
/*  82 */         binder.putLocal("currentCollectionID", collectionID);
/*  83 */         drset = SharedObjects.getTable("SearchCollections");
/*  84 */         if (drset != null)
/*     */         {
/*  86 */           FieldInfo[] info = ResultSetUtils.createInfoList(drset, new String[] { "sCollectionID" }, true);
/*  87 */           Vector row = drset.findRow(info[0].m_index, collectionID);
/*  88 */           if (row != null)
/*     */           {
/*  90 */             foundIt = true;
/*     */           }
/*     */         }
/*     */       }
/*  94 */       if ((drset != null) && (foundIt))
/*     */       {
/*  96 */         binder.addResultSet("SearchCollections", drset);
/*     */       }
/*     */ 
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 102 */       e.printStackTrace();
/* 103 */       return false;
/*     */     }
/* 105 */     return foundIt;
/*     */   }
/*     */ 
/*     */   public static boolean computeURLView(String url, DataBinder binder)
/*     */   {
/* 110 */     boolean result = false;
/* 111 */     String docfn = binder.getAllowMissing("DOC_FN");
/* 112 */     if ((url != null) && (url.length() > 0))
/*     */     {
/* 114 */       result = true;
/*     */     }
/*     */     else
/*     */     {
/* 118 */       int viewType = 1;
/*     */ 
/* 120 */       if (docfn != null)
/*     */       {
/* 122 */         docfn.toLowerCase();
/* 123 */         int index = docfn.indexOf("htm");
/* 124 */         if (index > 0)
/*     */         {
/* 126 */           viewType = 0;
/*     */         }
/*     */       }
/*     */ 
/* 130 */       if (viewType == 0)
/*     */       {
/* 132 */         binder.putLocal("ViewType", "ViewHtml");
/*     */       }
/*     */       else
/*     */       {
/* 136 */         binder.putLocal("ViewType", "ViewText");
/*     */       }
/*     */     }
/* 139 */     return result;
/*     */   }
/*     */ 
/*     */   public static boolean isFullTextIndexed(DataBinder binder)
/*     */   {
/* 144 */     boolean result = false;
/* 145 */     String docfn = binder.getAllowMissing("DOC_FN");
/* 146 */     if ((docfn != null) && (docfn.length() > 0))
/*     */     {
/* 148 */       int index = docfn.indexOf("notext.txt");
/* 149 */       if (index < 0)
/*     */       {
/* 151 */         result = true;
/*     */       }
/*     */     }
/* 154 */     else if (docfn == null)
/*     */     {
/* 156 */       String fullTextFormat = binder.getAllowMissing("dFullTextFormat");
/* 157 */       if ((fullTextFormat != null) && (fullTextFormat.length() > 0))
/*     */       {
/* 159 */         result = true;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 165 */     return result;
/*     */   }
/*     */ 
/*     */   public static String fixVerityDateFields(String query, IdcDateFormat fmt, IdcDateFormat inputFormat, Map types)
/*     */     throws ServiceException
/*     */   {
/* 172 */     ExecutionContextAdaptor cxt = new ExecutionContextAdaptor();
/* 173 */     cxt.setCachedObject("UserDateFormat", inputFormat);
/*     */ 
/* 175 */     Iterator it = types.keySet().iterator();
/* 176 */     while (it.hasNext())
/*     */     {
/* 178 */       String key = (String)it.next();
/* 179 */       String type = (String)types.get(key);
/* 180 */       if (type.equalsIgnoreCase("date"))
/*     */       {
/* 182 */         int i = query.indexOf(key);
/* 183 */         while (i >= 0)
/*     */         {
/* 185 */           int index = i;
/* 186 */           i = index + key.length();
/* 187 */           String pre = query.substring(0, index);
/* 188 */           String tmp = query.substring(index + key.length()).trim();
/*     */ 
/* 190 */           index = tmp.indexOf(" ");
/* 191 */           if (index < 0)
/*     */           {
/* 193 */             i = query.indexOf(key, i);
/*     */           }
/*     */ 
/* 196 */           String op = tmp.substring(0, index);
/* 197 */           tmp = tmp.substring(index).trim();
/* 198 */           if (!tmp.startsWith("`"))
/*     */           {
/* 200 */             i = query.indexOf(key, i);
/*     */           }
/*     */ 
/* 204 */           tmp = tmp.substring(1);
/* 205 */           index = tmp.indexOf("`");
/* 206 */           if (index < 0)
/*     */           {
/* 208 */             i = query.indexOf(key, i);
/*     */           }
/*     */ 
/* 211 */           String value = tmp.substring(0, index);
/* 212 */           String post = tmp.substring(index + 1);
/*     */ 
/* 214 */           Date d = LocaleResources.parseDateDataEntry(value, cxt, "fixQueryDateFields");
/* 215 */           value = fmt.format(d);
/* 216 */           if ((value.endsWith(" 00:00:00")) && (op.indexOf(">") != -1) && (op.indexOf(">=") < 0))
/*     */           {
/* 218 */             index = value.lastIndexOf(" ");
/* 219 */             value = value.substring(0, index);
/* 220 */             value = value + " 00:00:01";
/*     */           }
/* 222 */           query = pre + key + " " + op + " `" + value + "`";
/* 223 */           i = query.length();
/* 224 */           query = query + post;
/* 225 */           i = query.indexOf(key, i);
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 230 */     return query;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 235 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.SearchUtils
 * JD-Core Version:    0.5.4
 */