/*     */ package intradoc.json;
/*     */ 
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataFormatUtils;
/*     */ import intradoc.data.DataFormatter;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import java.io.IOException;
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashMap;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class JsonUtils
/*     */ {
/*     */   public static Map<String, Object> createObject(char[] buf, int[] index, int end)
/*     */     throws IOException
/*     */   {
/*  31 */     Map map = new HashMap();
/*     */ 
/*  33 */     int start = index[0];
/*     */ 
/*  36 */     skipWhitespaceAndComments(buf, index, start, end);
/*  37 */     if ((index[0] >= end) || (buf[index[0]] != '{'))
/*     */     {
/*  39 */       String str = LocaleUtils.encodeMessage("csJsonStringMustBeginWithBrace", null);
/*  40 */       throw new IOException(str);
/*     */     }
/*  42 */     index[0] += 1;
/*     */     while (true)
/*     */     {
/*  48 */       skipWhitespaceAndComments(buf, index, start, end);
/*  49 */       if (index[0] >= end)
/*     */       {
/*  51 */         String str = LocaleUtils.encodeMessage("csUnexpectedEndOfJsonString", null);
/*  52 */         throw new IOException(str);
/*     */       }
/*     */ 
/*  56 */       if (buf[index[0]] == '}')
/*     */       {
/*  58 */         index[0] += 1;
/*  59 */         break;
/*     */       }
/*     */ 
/*  63 */       String key = nextKey(buf, index, start, end);
/*     */ 
/*  66 */       skipWhitespaceAndComments(buf, index, start, end);
/*  67 */       if ((index[0] >= end) || (buf[index[0]] != ':'))
/*     */       {
/*  69 */         String str = LocaleUtils.encodeMessage("csColonMustFollowJsonKey", null, key);
/*  70 */         throw new IOException(str);
/*     */       }
/*  72 */       index[0] += 1;
/*     */ 
/*  75 */       skipWhitespaceAndComments(buf, index, start, end);
/*  76 */       if (index[0] >= end)
/*     */       {
/*  78 */         String str = LocaleUtils.encodeMessage("csUnexpectedEndOfJsonString", null);
/*  79 */         throw new IOException(str);
/*     */       }
/*  81 */       Object value = nextValue(buf, index, start, end);
/*     */ 
/*  83 */       map.put(key, value);
/*     */ 
/*  86 */       skipWhitespaceAndComments(buf, index, start, end);
/*  87 */       if (index[0] >= end)
/*     */       {
/*  89 */         String str = LocaleUtils.encodeMessage("csUnexpectedEndOfJsonString", null);
/*  90 */         throw new IOException(str);
/*     */       }
/*  92 */       if (buf[index[0]] == ',')
/*     */       {
/*  94 */         index[0] += 1;
/*     */       }
/*     */     }
/*     */ 
/*  98 */     return map;
/*     */   }
/*     */ 
/*     */   public static List createArray(char[] buf, int[] index, int end)
/*     */     throws IOException
/*     */   {
/* 104 */     int start = index[0];
/* 105 */     List array = new ArrayList();
/*     */ 
/* 108 */     if (buf[index[0]] != '[')
/*     */     {
/* 110 */       String message = LocaleUtils.encodeMessage("csJsonArrayMustBeginWithBracket", null);
/* 111 */       throw new IOException(message);
/*     */     }
/* 113 */     index[0] += 1;
/*     */ 
/* 116 */     skipWhitespaceAndComments(buf, index, start, end);
/* 117 */     if (index[0] >= end)
/*     */     {
/* 119 */       String str = LocaleUtils.encodeMessage("csUnexpectedEndOfJsonArrayString", null);
/* 120 */       throw new IOException(str);
/*     */     }
/*     */ 
/* 124 */     if (buf[index[0]] == ']')
/*     */     {
/* 126 */       index[0] += 1;
/* 127 */       return array;
/*     */     }
/*     */ 
/*     */     while (true)
/*     */     {
/* 133 */       skipWhitespaceAndComments(buf, index, start, end);
/* 134 */       if (index[0] >= end)
/*     */       {
/* 136 */         String str = LocaleUtils.encodeMessage("csUnexpectedEndOfJsonArrayString", null);
/* 137 */         throw new IOException(str);
/*     */       }
/*     */ 
/* 141 */       if (buf[index[0]] == ',')
/*     */       {
/* 143 */         array.add(null);
/* 144 */         index[0] += 1;
/*     */       }
/*     */ 
/* 148 */       Object o = nextValue(buf, index, start, end);
/* 149 */       array.add(o);
/*     */ 
/* 152 */       skipWhitespaceAndComments(buf, index, start, end);
/* 153 */       if (index[0] >= end)
/*     */       {
/* 155 */         String str = LocaleUtils.encodeMessage("csUnexpectedEndOfJsonArrayString", null);
/* 156 */         throw new IOException(str);
/*     */       }
/*     */ 
/* 159 */       if (buf[index[0]] == ',')
/*     */       {
/* 161 */         index[0] += 1;
/*     */       } else {
/* 163 */         if (buf[index[0]] == ']')
/*     */         {
/* 165 */           index[0] += 1;
/* 166 */           break;
/*     */         }
/*     */ 
/* 170 */         String str = LocaleUtils.encodeMessage("csUnexpectedJsonArrayCharacter", null, new Character(buf[index[0]]));
/*     */ 
/* 172 */         throw new IOException(str);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 177 */     return array;
/*     */   }
/*     */ 
/*     */   private static String nextKey(char[] buf, int[] index, int start, int end)
/*     */     throws IOException
/*     */   {
/* 183 */     IdcStringBuilder str = new IdcStringBuilder();
/*     */ 
/* 185 */     char quote = buf[index[0]];
/* 186 */     if ((quote != '\'') && (quote != '"'))
/*     */     {
/* 188 */       String message = LocaleUtils.encodeMessage("csJsonKeyMustBeginWithQuote", null);
/* 189 */       throw new IOException(message);
/*     */     }
/* 191 */     index[0] += 1;
/*     */ 
/* 193 */     while ((index[0] < end) && (buf[index[0]] != quote))
/*     */     {
/* 195 */       if ((buf[index[0]] == '\r') || (buf[index[0]] == '\n'))
/*     */       {
/* 197 */         String message = LocaleUtils.encodeMessage("csInvalidJsonKey", null, str.toString());
/* 198 */         throw new IOException(message);
/*     */       }
/*     */ 
/* 202 */       if (buf[index[0]] == '\\')
/*     */       {
/* 204 */         if (index[0] + 1 >= end)
/*     */         {
/* 206 */           String message = LocaleUtils.encodeMessage("csInvalidJsonKey", null, str.toString() + "\\");
/*     */ 
/* 208 */           throw new IOException(message);
/*     */         }
/*     */ 
/* 211 */         index[0] += 1;
/*     */ 
/* 213 */         switch (buf[index[0]])
/*     */         {
/*     */         case '"':
/*     */         case '\'':
/*     */         case '\\':
/* 218 */           str.append(buf[index[0]]);
/* 219 */           break;
/*     */         case 'n':
/* 221 */           str.append('\n');
/* 222 */           break;
/*     */         case 'r':
/* 224 */           str.append('\r');
/* 225 */           break;
/*     */         case 't':
/* 227 */           str.append('\t');
/* 228 */           break;
/*     */         case 'u':
/* 231 */           if (index[0] + 4 >= end)
/*     */           {
/* 233 */             String message = LocaleUtils.encodeMessage("csInvalidJsonKey", null, str.toString() + "\\u");
/*     */ 
/* 235 */             throw new IOException(message);
/*     */           }
/*     */ 
/* 238 */           String byteStr = new String(buf, index[0] + 1, 4);
/* 239 */           str.append((char)Integer.parseInt(byteStr, 16));
/* 240 */           index[0] += 4;
/* 241 */           break;
/*     */         default:
/* 243 */           String message = LocaleUtils.encodeMessage("csInvalidJsonKey", null, str.toString() + "\\");
/*     */ 
/* 245 */           throw new IOException(message);
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 250 */         str.append(buf[index[0]]);
/*     */       }
/*     */ 
/* 253 */       index[0] += 1;
/*     */     }
/*     */ 
/* 256 */     if (index[0] == end)
/*     */     {
/* 258 */       String message = LocaleUtils.encodeMessage("csUnexpectedEndOfJsonString", null);
/* 259 */       throw new IOException(message);
/*     */     }
/*     */ 
/* 263 */     index[0] += 1;
/*     */ 
/* 265 */     return str.toString();
/*     */   }
/*     */ 
/*     */   private static Object nextValue(char[] buf, int[] index, int start, int end)
/*     */     throws IOException
/*     */   {
/* 271 */     Object o = null;
/*     */ 
/* 273 */     switch (buf[index[0]])
/*     */     {
/*     */     case '"':
/*     */     case '\'':
/* 277 */       o = nextKey(buf, index, start, end);
/* 278 */       break;
/*     */     case '{':
/* 280 */       o = createObject(buf, index, end);
/* 281 */       break;
/*     */     case '[':
/* 283 */       o = createArray(buf, index, end);
/*     */     }
/*     */ 
/* 287 */     if (o == null)
/*     */     {
/* 291 */       IdcStringBuilder str = new IdcStringBuilder();
/*     */ 
/* 293 */       while ((index[0] < end) && (buf[index[0]] > ' ') && (",:]}/\\\"[{;=#".indexOf(buf[index[0]]) < 0))
/*     */       {
/* 296 */         str.append(buf[index[0]]);
/* 297 */         index[0] += 1;
/*     */       }
/*     */ 
/* 300 */       String val = str.toString();
/* 301 */       if (val.equals("true"))
/*     */       {
/* 303 */         o = Boolean.TRUE;
/*     */       }
/* 305 */       else if (val.equals("false"))
/*     */       {
/* 307 */         o = Boolean.FALSE;
/*     */       }
/* 309 */       else if (!val.equals("null")) if (!val.equals("NULL"))
/*     */         {
/*     */           try
/*     */           {
/* 318 */             if (val.indexOf(46) >= 0)
/*     */             {
/* 320 */               o = new Double(val);
/*     */             }
/*     */             else
/*     */             {
/* 324 */               o = new Integer(val);
/*     */             }
/*     */ 
/*     */           }
/*     */           catch (NumberFormatException e)
/*     */           {
/*     */           }
/*     */ 
/* 332 */           if (o == null)
/*     */           {
/* 334 */             String message = LocaleUtils.encodeMessage("csInvalidJsonValue", null, val);
/* 335 */             throw new IOException(message);
/*     */           }
/*     */         }
/*     */     }
/*     */ 
/* 340 */     return o;
/*     */   }
/*     */ 
/*     */   private static void skipWhitespaceAndComments(char[] buf, int[] index, int start, int end)
/*     */     throws IOException
/*     */   {
/* 346 */     boolean foundIt = false;
/* 347 */     int i = index[0];
/*     */ 
/* 349 */     while ((!foundIt) && (i < end))
/*     */     {
/* 351 */       switch (buf[i])
/*     */       {
/*     */       case '\t':
/*     */       case '\n':
/*     */       case '\r':
/*     */       case ' ':
/* 357 */         ++i;
/* 358 */         break;
/*     */       case '/':
/* 360 */         if ((i < end - 1) && (buf[(i + 1)] == '/')) {
/*     */           while (true)
/*     */           {
/* 363 */             if ((i < end) && (buf[i] != '\r') && (buf[i] != '\n'));
/* 365 */             ++i;
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/* 370 */         if ((i < end - 1) && (buf[(i + 1)] == '*'))
/*     */         {
/* 373 */           while ((i < end - 1) && (buf[i] != '*') && (buf[(i - 1)] != '/'))
/*     */           {
/* 375 */             ++i;
/*     */           }
/*     */ 
/* 378 */           if (i <= end - 1)
/*     */             continue;
/* 380 */           String str = LocaleUtils.encodeMessage("csUnclosedJsonComment", null);
/* 381 */           throw new IOException(str);
/*     */         }
/*     */ 
/* 387 */         foundIt = true;
/* 388 */         break;
/*     */       case '#':
/* 390 */         if ((i > start) && (((buf[(i - 1)] == '\r') || (buf[(i - 1)] == '\n')))) {
/*     */           while (true)
/*     */           {
/* 393 */             if ((i < end) && (buf[i] != '\r') && (buf[i] != '\n'));
/* 395 */             ++i;
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/* 401 */         foundIt = true;
/* 402 */         break;
/*     */       }
/* 404 */       foundIt = true;
/*     */     }
/*     */ 
/* 408 */     index[0] = i;
/*     */   }
/*     */ 
/*     */   public static void mergeIntoDataBinder(Map map, DataBinder binder)
/*     */     throws IOException
/*     */   {
/* 414 */     mergeIntoDataBinder(map, binder, null);
/*     */   }
/*     */ 
/*     */   public static void mergeIntoDataBinder(Map map, DataBinder binder, List rsetList)
/*     */     throws IOException
/*     */   {
/* 420 */     Object o = map.get("LocalData");
/*     */     Map localData;
/* 421 */     if ((o != null) && (o instanceof Map))
/*     */     {
/* 423 */       localData = (Map)o;
/* 424 */       for (String key : localData.keySet())
/*     */       {
/* 426 */         Object value = localData.get(key);
/* 427 */         if (value != null)
/*     */         {
/* 429 */           binder.putLocal(key, value.toString());
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 434 */     o = map.get("ResultSets");
/* 435 */     if ((o == null) || (!o instanceof Map))
/*     */       return;
/* 437 */     Map resultSets = (Map)o;
/* 438 */     for (String resultSetName : resultSets.keySet())
/*     */     {
/* 440 */       Object rsetObject = resultSets.get(resultSetName);
/* 441 */       if ((rsetObject != null) && (rsetObject instanceof Map))
/*     */       {
/* 443 */         DataResultSet drset = new DataResultSet();
/* 444 */         List allFields = new ArrayList();
/*     */ 
/* 446 */         Map rset = (Map)rsetObject;
/* 447 */         Object fieldsObject = rset.get("fields");
/*     */         Iterator i$;
/* 448 */         if ((fieldsObject != null) && (fieldsObject instanceof List))
/*     */         {
/* 450 */           List fields = (List)fieldsObject;
/* 451 */           for (i$ = fields.iterator(); i$.hasNext(); ) { Object fieldObject = i$.next();
/*     */ 
/* 453 */             if (fieldObject != null)
/*     */             {
/* 455 */               FieldInfo fi = new FieldInfo();
/*     */ 
/* 457 */               if (fieldObject instanceof Map)
/*     */               {
/* 459 */                 Map fieldInfo = (Map)fieldObject;
/* 460 */                 Object name = fieldInfo.get("name");
/* 461 */                 Object type = fieldInfo.get("type");
/* 462 */                 Object length = fieldInfo.get("length");
/*     */ 
/* 464 */                 if ((name != null) && (name instanceof String))
/*     */                 {
/* 466 */                   fi.m_name = ((String)name);
/*     */                 }
/*     */                 else
/*     */                 {
/* 470 */                   String msg = LocaleUtils.encodeMessage("csUnknownJsonResultSetField", null);
/*     */ 
/* 472 */                   throw new IOException(msg);
/*     */                 }
/*     */ 
/* 475 */                 if ((type != null) && (type instanceof Integer))
/*     */                 {
/* 477 */                   fi.m_type = ((Integer)type).intValue();
/*     */                 }
/*     */ 
/* 480 */                 if ((length != null) && (length instanceof Integer))
/*     */                 {
/* 482 */                   fi.m_maxLen = ((Integer)length).intValue();
/*     */                 }
/*     */               }
/* 485 */               else if (fieldObject instanceof List)
/*     */               {
/* 487 */                 List fieldInfo = (List)fieldObject;
/* 488 */                 fi.m_name = ((String)fieldInfo.get(0));
/*     */ 
/* 490 */                 if (fieldInfo.size() > 1)
/*     */                 {
/* 492 */                   Object typeObject = fieldInfo.get(1);
/* 493 */                   if (typeObject instanceof Integer)
/*     */                   {
/* 495 */                     fi.m_type = ((Integer)typeObject).intValue();
/*     */                   }
/*     */                 }
/*     */ 
/* 499 */                 if (fieldInfo.size() > 2)
/*     */                 {
/* 501 */                   Object maxLenObject = fieldInfo.get(2);
/* 502 */                   if (maxLenObject instanceof Integer)
/*     */                   {
/* 504 */                     fi.m_maxLen = ((Integer)maxLenObject).intValue();
/*     */                   }
/*     */                 }
/*     */               }
/* 508 */               else if (fieldObject instanceof String)
/*     */               {
/* 510 */                 fi.m_name = ((String)fieldObject);
/*     */               }
/*     */ 
/* 513 */               allFields.add(fi);
/*     */             } }
/*     */ 
/*     */         }
/*     */ 
/* 518 */         drset.mergeFieldsWithFlags(allFields, 1);
/*     */ 
/* 520 */         Object rowsObject = rset.get("rows");
/*     */         Iterator i$;
/* 521 */         if ((rowsObject != null) && (rowsObject instanceof List))
/*     */         {
/* 523 */           List rows = (List)rowsObject;
/* 524 */           for (i$ = rows.iterator(); i$.hasNext(); ) { Object rowObject = i$.next();
/*     */ 
/* 526 */             if ((rowObject != null) && (rowObject instanceof List))
/*     */             {
/* 528 */               List rowToAdd = new ArrayList();
/*     */ 
/* 530 */               List row = (List)rowObject;
/* 531 */               for (Iterator i$ = row.iterator(); i$.hasNext(); ) { Object cellObject = i$.next();
/*     */ 
/* 533 */                 if (cellObject == null)
/*     */                 {
/* 535 */                   cellObject = "";
/*     */                 }
/*     */ 
/* 538 */                 rowToAdd.add(cellObject.toString()); }
/*     */ 
/*     */ 
/* 541 */               drset.addRowWithList(rowToAdd);
/*     */             } }
/*     */ 
/*     */         }
/*     */ 
/* 546 */         binder.addResultSet(resultSetName, drset);
/*     */ 
/* 548 */         if (rsetList != null)
/*     */         {
/* 550 */           rsetList.add(drset);
/*     */         }
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static String binderToJsonString(DataBinder binder)
/*     */   {
/* 559 */     DataFormatter jsonFormat = new DataFormatter("json,rows=-1,noshowenv");
/* 560 */     DataFormatUtils.appendDataBinder(jsonFormat, null, binder, 0);
/* 561 */     return jsonFormat.toString();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 566 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78147 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.json.JsonUtils
 * JD-Core Version:    0.5.4
 */