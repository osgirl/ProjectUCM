/*     */ package intradoc.jdbc;
/*     */ 
/*     */ import intradoc.common.ClassHelper;
/*     */ import intradoc.common.ClassHelperUtils;
/*     */ import intradoc.common.IdcDateFormat;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Log;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DatabaseConfigData;
/*     */ import intradoc.data.ParameterObjects;
/*     */ import intradoc.data.ParameterPacket;
/*     */ import intradoc.data.Parameters;
/*     */ import intradoc.data.QueryParameterInfo;
/*     */ import intradoc.data.QueryUtils;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.io.BufferedInputStream;
/*     */ import java.io.BufferedReader;
/*     */ import java.io.ByteArrayInputStream;
/*     */ import java.io.File;
/*     */ import java.io.FileInputStream;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.Reader;
/*     */ import java.io.StringReader;
/*     */ import java.lang.reflect.InvocationTargetException;
/*     */ import java.math.BigDecimal;
/*     */ import java.sql.CallableStatement;
/*     */ import java.sql.PreparedStatement;
/*     */ import java.sql.Timestamp;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Date;
/*     */ import java.util.HashMap;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class JdbcQueryUtils
/*     */ {
/*  35 */   protected static HashMap m_queryModifiers = new HashMap();
/*     */ 
/*     */   public static void initQueryModifier(JdbcManager manager, String modifiers) throws DataException
/*     */   {
/*  39 */     if ((modifiers == null) || (modifiers.trim().length() == 0) || (manager == null))
/*     */     {
/*  41 */       return;
/*     */     }
/*     */ 
/*  44 */     ArrayList queryModifiers = null;
/*  45 */     synchronized (m_queryModifiers)
/*     */     {
/*  47 */       queryModifiers = (ArrayList)m_queryModifiers.get(manager);
/*  48 */       if (queryModifiers == null)
/*     */       {
/*  50 */         queryModifiers = new ArrayList();
/*  51 */         m_queryModifiers.put(manager, queryModifiers);
/*     */       }
/*     */     }
/*     */ 
/*  55 */     Vector modifierList = StringUtils.parseArray(modifiers, ',', ',');
/*  56 */     int size = modifierList.size();
/*  57 */     for (int i = 0; i < size; ++i)
/*     */     {
/*  59 */       String mod = (String)modifierList.elementAt(i);
/*  60 */       mod = mod.trim();
/*     */       try
/*     */       {
/*  63 */         Class c = Class.forName(mod);
/*  64 */         if (!queryModifiers.contains(c))
/*     */         {
/*  66 */           if (!ClassHelperUtils.checkMethodExistence(c, "modifyQuery", new Class[] { ClassHelper.m_stringClass, ClassHelper.m_objectClass }))
/*     */           {
/*  68 */             String msg = LocaleUtils.encodeMessage("csJdbcQueryModifierNoMethod", c.getName());
/*  69 */             throw new DataException(msg);
/*     */           }
/*  71 */           queryModifiers.add(c);
/*     */         }
/*     */       }
/*     */       catch (ClassNotFoundException e)
/*     */       {
/*  76 */         throw new DataException("!csErrorInitJdbcQueryModifier", e);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static String buildQuery(JdbcQueryDef qDef, Parameters args, JdbcWorkspace ws, JdbcManager manager)
/*     */     throws DataException
/*     */   {
/*  86 */     IdcStringBuilder buff = new IdcStringBuilder();
/*  87 */     String query = qDef.m_query;
/*     */ 
/*  89 */     int prevIndex = 0;
/*  90 */     int index = 0;
/*     */ 
/*  92 */     DatabaseConfigData config = manager.m_config;
/*  93 */     Vector params = qDef.m_parameters;
/*  94 */     int size = params.size();
/*  95 */     boolean useUpperCase = false;
/*  96 */     if (ws != null)
/*     */     {
/*  98 */       useUpperCase = config.getValueAsBool("UseUpperCaseColumnMap", useUpperCase);
/*     */     }
/*     */ 
/* 101 */     for (int i = 0; (index = query.indexOf(63, prevIndex)) >= 0; ++i)
/*     */     {
/* 103 */       if (i == size)
/*     */       {
/* 105 */         String msg = LocaleUtils.encodeMessage("csDbCouldNotBind", null, qDef.m_name);
/*     */ 
/* 107 */         throw new DataException(msg);
/*     */       }
/* 109 */       buff.append(query.substring(prevIndex, index));
/*     */ 
/* 112 */       QueryParameterInfo param = (QueryParameterInfo)params.elementAt(i);
/* 113 */       Object obj = getParameterValue(qDef.m_name, args, param);
/* 114 */       if ((obj != null) && (!obj instanceof String))
/*     */       {
/* 116 */         String msg = LocaleUtils.encodeMessage("csJdbcParameterObjectError", null, qDef.m_name, param.m_name);
/*     */ 
/* 118 */         throw new DataException(msg);
/*     */       }
/* 120 */       String value = (String)obj;
/* 121 */       if (param.m_type == -101)
/*     */       {
/* 123 */         if ((value == null) || (value.length() == 0))
/*     */         {
/* 125 */           int tmpIndex = index;
/* 126 */           int len = query.length();
/* 127 */           boolean foundComma = false;
/*     */           do { if (++tmpIndex >= len)
/*     */               break;
/* 130 */             if (query.charAt(tmpIndex) != ',')
/*     */               continue;
/* 132 */             foundComma = true;
/* 133 */             break; }
/*     */ 
/* 135 */           while (!Character.isLetter(query.charAt(tmpIndex)));
/*     */ 
/* 140 */           if (foundComma)
/*     */           {
/* 142 */             prevIndex = tmpIndex + 1;
/* 143 */             continue;
/*     */           }
/*     */         }
/* 146 */         else if (value.trim().endsWith(","))
/*     */         {
/* 148 */           int lastIndex = value.lastIndexOf(44);
/* 149 */           value = value.substring(0, lastIndex);
/*     */         }
/*     */       }
/* 152 */       else if ((useUpperCase) && (config.isValueUpperCaseNeeded(param.m_name)))
/*     */       {
/* 154 */         value = value.toUpperCase();
/*     */       }
/* 156 */       if (param.m_isList)
/*     */       {
/* 158 */         List list = StringUtils.appendListFromSequence(new ArrayList(), value, 0, value.length(), ',', '^', 32);
/*     */ 
/* 160 */         Iterator it = list.iterator();
/* 161 */         boolean isFirst = true;
/* 162 */         while (it.hasNext())
/*     */         {
/* 164 */           if (isFirst)
/*     */           {
/* 166 */             isFirst = false;
/*     */           }
/*     */           else
/*     */           {
/* 170 */             buff.append(',');
/*     */           }
/* 172 */           CharSequence cs = (CharSequence)it.next();
/*     */ 
/* 174 */           QueryUtils.appendParam(buff, param.m_type, cs.toString(), manager, args);
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 179 */         QueryUtils.appendParam(buff, param.m_type, value, manager, args);
/*     */       }
/* 181 */       prevIndex = index + 1;
/*     */     }
/*     */ 
/* 184 */     buff.append(query.substring(prevIndex));
/*     */ 
/* 186 */     String tmp = buff.toString();
/* 187 */     tmp = parseSQL(tmp, ws, manager);
/* 188 */     return tmp;
/*     */   }
/*     */ 
/*     */   public static Object getParameterValue(String query, Parameters args, QueryParameterInfo param)
/*     */     throws DataException
/*     */   {
/* 198 */     Object obj = null;
/* 199 */     if (args instanceof ParameterObjects)
/*     */     {
/* 201 */       ParameterObjects paramObjs = (ParameterObjects)args;
/* 202 */       obj = paramObjs.getObject(param.m_name);
/* 203 */       if ((((obj == null) || (!obj instanceof String))) && (param.m_alternateName != null))
/*     */       {
/* 205 */         obj = paramObjs.getObject(param.m_alternateName);
/*     */       }
/*     */     }
/*     */ 
/* 209 */     if ((obj == null) || (obj instanceof String))
/*     */     {
/* 211 */       String value = (param.m_name.equals("idcTimeCurrent")) ? "idcTimeCurrent" : args.getSystem(param.m_name);
/* 212 */       boolean isEmptyString = (value != null) && (value.length() == 0);
/* 213 */       if ((((value == null) || (isEmptyString))) && (param.m_alternateName != null))
/*     */       {
/* 215 */         value = args.getSystem(param.m_alternateName);
/* 216 */         if (value != null)
/*     */         {
/* 218 */           isEmptyString = value.length() == 0;
/*     */         }
/*     */       }
/* 221 */       if ((value == null) || (isEmptyString) || (value.equals("idcTimeCurrent")))
/*     */       {
/* 223 */         if ((value == null) || (isEmptyString))
/*     */         {
/* 225 */           if ((param.m_default == null) && (!isEmptyString) && (!param.m_isOutput) && (param.m_type != -101) && (param.m_type != -102))
/*     */           {
/* 229 */             String msg = LocaleUtils.encodeMessage("syParameterNotFound", null, param.m_name);
/*     */ 
/* 231 */             throw new DataException(msg);
/*     */           }
/* 233 */           value = param.m_default;
/*     */         }
/* 235 */         if ((param.m_type == 5) && (value != null) && (value.equals("idcTimeCurrent")))
/*     */         {
/* 238 */           value = LocaleResources.m_odbcFormat.format(new Date());
/*     */         }
/*     */       }
/* 241 */       if ((value == null) && (isEmptyString))
/*     */       {
/* 243 */         value = "";
/*     */       }
/* 245 */       obj = value;
/*     */     }
/* 247 */     return obj;
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static void appendParam(StringBuffer buffer, int type, String value, JdbcWorkspace ws, JdbcManager manager)
/*     */     throws DataException
/*     */   {
/* 258 */     IdcStringBuilder tmpBuilder = new IdcStringBuilder();
/* 259 */     QueryUtils.appendParam(tmpBuilder, type, value, manager);
/* 260 */     buffer.append(tmpBuilder);
/*     */   }
/*     */ 
/*     */   public static Map buildPreparedQuery(JdbcQueryDef qDef, Parameters args, JdbcWorkspace ws, JdbcManager manager)
/*     */     throws DataException
/*     */   {
/* 268 */     Map map = new HashMap();
/* 269 */     Vector params = qDef.m_parameters;
/* 270 */     int size = params.size();
/*     */ 
/* 272 */     DatabaseConfigData config = manager.m_config;
/* 273 */     boolean useUpperCase = false;
/* 274 */     if (ws != null)
/*     */     {
/* 276 */       useUpperCase = config.getValueAsBool("UseUpperCaseColumnMap", useUpperCase);
/*     */     }
/*     */ 
/* 279 */     IdcStringBuilder builder = new IdcStringBuilder("Parameters: (");
/* 280 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 282 */       QueryParameterInfo param = (QueryParameterInfo)params.elementAt(i);
/*     */ 
/* 284 */       Object value = getParameterValue(qDef.m_name, args, param);
/* 285 */       if ((value instanceof String) && (useUpperCase) && (config.isValueUpperCaseNeeded(param.m_name)))
/*     */       {
/* 288 */         value = ((String)value).toUpperCase();
/*     */       }
/* 290 */       setParameter(qDef.m_statement, param, i + 1, value, manager, map);
/* 291 */       if (i != 0)
/*     */       {
/* 293 */         builder.append(",");
/*     */       }
/* 295 */       builder.append('[');
/* 296 */       builder.append(param.m_name);
/* 297 */       if (param.m_isOutput)
/*     */       {
/* 299 */         builder.append(" (output)");
/*     */       }
/*     */       else
/*     */       {
/* 303 */         builder.append(":");
/* 304 */         builder.append(value.toString());
/*     */       }
/* 306 */       builder.append(']');
/*     */     }
/* 308 */     if (builder.length() > 13)
/*     */     {
/* 310 */       builder.append(')');
/* 311 */       manager.debugMsg(builder.toString());
/*     */     }
/* 313 */     return map;
/*     */   }
/*     */ 
/*     */   public static void setParameter(PreparedStatement stmt, QueryParameterInfo info, int index, Object obj, JdbcManager manager, Map map)
/*     */     throws DataException
/*     */   {
/* 319 */     String value = null;
/* 320 */     if (obj instanceof String)
/*     */     {
/* 322 */       value = (String)obj;
/*     */     }
/* 324 */     else if (obj != null)
/*     */     {
/* 326 */       value = obj.toString();
/*     */     }
/*     */     try
/*     */     {
/* 330 */       switch (info.m_type)
/*     */       {
/*     */       case 1:
/* 334 */         if (info.m_isOutput)
/*     */         {
/* 336 */           ((CallableStatement)stmt).registerOutParameter(info.m_index, 16);
/*     */         }
/* 338 */         if (info.m_isInput)
/*     */         {
/* 340 */           stmt.setBoolean(index, StringUtils.convertToBool(value, false)); } break;
/*     */       case 2:
/* 346 */         if (info.m_isOutput)
/*     */         {
/* 348 */           ((CallableStatement)stmt).registerOutParameter(info.m_index, 12);
/*     */         }
/* 350 */         if (info.m_isInput)
/*     */         {
/* 352 */           stmt.setString(index, value); } break;
/*     */       case 3:
/* 358 */         if (info.m_isOutput)
/*     */         {
/* 360 */           ((CallableStatement)stmt).registerOutParameter(info.m_index, -5);
/*     */         }
/* 362 */         if (info.m_isInput)
/*     */         {
/* 364 */           if ((value != null) && (value.length() > 0))
/*     */           {
/* 366 */             long l = new Long(value).longValue();
/* 367 */             stmt.setLong(index, l);
/*     */           }
/*     */           else
/*     */           {
/* 371 */             stmt.setNull(index, -5); } 
/* 371 */         }break;
/*     */       case 11:
/* 378 */         if (info.m_isOutput)
/*     */         {
/* 380 */           ((CallableStatement)stmt).registerOutParameter(info.m_index, 3);
/*     */         }
/* 382 */         if (info.m_isInput)
/*     */         {
/* 384 */           BigDecimal l = null;
/* 385 */           if ((value != null) && (value.length() > 0))
/*     */           {
/* 387 */             l = new BigDecimal(value);
/* 388 */             stmt.setBigDecimal(index, l);
/*     */           }
/*     */           else
/*     */           {
/* 392 */             stmt.setNull(index, 3);
/*     */           }
/*     */         }
/* 394 */         break;
/*     */       case 4:
/* 399 */         if (info.m_isOutput)
/*     */         {
/* 401 */           ((CallableStatement)stmt).registerOutParameter(info.m_index, 6);
/*     */         }
/*     */         else
/*     */         {
/* 405 */           stmt.setFloat(index, Float.valueOf(value).floatValue());
/*     */         }
/*     */ 
/* 408 */         break;
/*     */       case 5:
/* 411 */         if (info.m_isOutput)
/*     */         {
/* 413 */           ((CallableStatement)stmt).registerOutParameter(info.m_index, 93);
/*     */         }
/* 415 */         if (info.m_isInput)
/*     */         {
/*     */           try
/*     */           {
/* 419 */             if ((value != null) && (value.length() != 0))
/*     */             {
/* 422 */               stmt.setTimestamp(index, new Timestamp(LocaleResources.parseDate(value, null).getTime()));
/*     */             }
/*     */             else
/*     */             {
/* 429 */               stmt.setTimestamp(index, null);
/*     */             }
/*     */           }
/*     */           catch (ServiceException e)
/*     */           {
/* 434 */             throw new DataException(null, e);
/*     */           }
/*     */         }
/*     */       case 6:
/* 441 */         if (info.m_isOutput)
/*     */         {
/* 443 */           ((CallableStatement)stmt).registerOutParameter(info.m_index, 12);
/*     */         }
/* 445 */         if (info.m_isInput)
/*     */         {
/* 447 */           stmt.setString(index, value); } break;
/*     */       case 9:
/* 453 */         if (info.m_isOutput)
/*     */         {
/* 455 */           ((CallableStatement)stmt).registerOutParameter(info.m_index, 2004);
/*     */         }
/* 457 */         if (info.m_isInput)
/*     */         {
/*     */           try
/*     */           {
/* 461 */             boolean isCleanup = true;
/* 462 */             InputStream is = null;
/* 463 */             long len = 1L;
/* 464 */             if (obj instanceof ParameterPacket)
/*     */             {
/* 466 */               ParameterPacket packet = (ParameterPacket)obj;
/* 467 */               is = (InputStream)packet.m_primaryObject;
/* 468 */               len = NumberUtils.parseLong((String)packet.m_infoMap.get("fileLength"), 0L);
/*     */ 
/* 470 */               isCleanup = false;
/*     */             }
/* 472 */             else if (value != null)
/*     */             {
/* 474 */               File f = new File(value);
/* 475 */               if (f.exists())
/*     */               {
/* 477 */                 is = new BufferedInputStream(new FileInputStream(value));
/* 478 */                 len = f.length();
/*     */               }
/*     */             }
/*     */ 
/* 482 */             if (is == null)
/*     */             {
/* 485 */               is = new ByteArrayInputStream(new byte[] { 32 });
/* 486 */               len = 1L;
/*     */             }
/*     */ 
/* 489 */             if (isCleanup)
/*     */             {
/* 491 */               List lis = (List)map.get("instreamList");
/* 492 */               if (lis == null)
/*     */               {
/* 494 */                 lis = new ArrayList();
/* 495 */                 map.put("instreamList", lis);
/*     */               }
/* 497 */               lis.add(is);
/*     */             }
/*     */ 
/* 500 */             if (len < 2147483647L)
/*     */             {
/* 502 */               stmt.setBinaryStream(index, is, (int)len);
/*     */             }
/*     */             else
/*     */             {
/* 506 */               Class[] parameterTypes = { Integer.TYPE, InputStream.class };
/* 507 */               Object[] parameterArguments = { Integer.valueOf(index), is };
/* 508 */               String methodName = "setBinaryStream";
/* 509 */               String msg = null;
/*     */               try
/*     */               {
/* 513 */                 Map invokeInfo = new HashMap();
/* 514 */                 invokeInfo.put("object", stmt);
/* 515 */                 invokeInfo.put("method", methodName);
/* 516 */                 invokeInfo.put("parameters", parameterTypes);
/* 517 */                 invokeInfo.put("arguments", parameterArguments);
/* 518 */                 invokeInfo.put("suppressAccessControlCheck", Boolean.TRUE);
/*     */ 
/* 520 */                 ClassHelperUtils.executeMethod(invokeInfo, 0);
/*     */               }
/*     */               catch (Exception e)
/*     */               {
/* 524 */                 if ((e instanceof NoSuchMethodException) || (e instanceof InvocationTargetException))
/*     */                 {
/* 526 */                   msg = LocaleUtils.encodeMessage("csJdbcUnsupportedStreaming", null);
/* 527 */                   Report.error(null, msg, e);
/*     */                 }
/*     */                 else
/*     */                 {
/* 531 */                   Report.trace(null, "Error invoking the setBinaryStream method", e);
/* 532 */                   msg = e.getMessage();
/*     */                 }
/* 534 */                 throw new ServiceException(msg, e);
/*     */               }
/*     */             }
/*     */           }
/*     */           catch (IOException e)
/*     */           {
/* 540 */             throw new DataException(e.getMessage(), e);
/*     */           }
/*     */         }
/*     */       case -201:
/* 547 */         if (info.m_isOutput)
/*     */         {
/* 549 */           if (manager.isOracle())
/*     */           {
/* 551 */             ClassHelper cp = new ClassHelper();
/* 552 */             cp.initWithoutInstatiate("oracle.jdbc.OracleTypes");
/* 553 */             int type = cp.getIntValue("CURSOR");
/*     */ 
/* 555 */             ((CallableStatement)stmt).registerOutParameter(info.m_index, type);
/*     */           }
/*     */           else
/*     */           {
/* 559 */             ((CallableStatement)stmt).registerOutParameter(info.m_index, 1111); } 
/* 559 */         }break;
/*     */       case 10:
/* 566 */         if (info.m_isOutput)
/*     */         {
/* 568 */           ((CallableStatement)stmt).registerOutParameter(info.m_index, 2005);
/*     */         }
/* 570 */         if (info.m_isInput)
/*     */         {
/* 572 */           Reader reader = null;
/* 573 */           int len = 1;
/* 574 */           if (obj instanceof ParameterPacket)
/*     */           {
/* 576 */             ParameterPacket packet = (ParameterPacket)obj;
/* 577 */             reader = (Reader)packet.m_primaryObject;
/* 578 */             len = NumberUtils.parseInteger((String)packet.m_infoMap.get("length"), 0);
/*     */ 
/* 580 */             if (len == 0)
/*     */             {
/* 582 */               len = NumberUtils.parseInteger((String)packet.m_infoMap.get("fileLength"), 0);
/*     */             }
/*     */ 
/*     */           }
/* 586 */           else if (value != null)
/*     */           {
/* 588 */             reader = new BufferedReader(new StringReader(value));
/* 589 */             len = value.length();
/*     */           }
/* 591 */           if (reader != null)
/*     */           {
/* 593 */             stmt.setCharacterStream(index, reader, len);
/*     */           }
/*     */         }
/* 595 */         break;
/*     */       default:
/* 599 */         String msg = LocaleUtils.encodeMessage("csDbUndefinedParameterType", null, "" + info.m_type);
/*     */ 
/* 601 */         throw new DataException(msg);
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 606 */       Report.trace(null, "Unable to set parameter with parameter=" + info.m_name + " value=" + obj, e);
/*     */ 
/* 608 */       String msg = LocaleUtils.encodeMessage("csDbUnableToSetParameter", null, info.m_name, obj);
/*     */ 
/* 610 */       throw new DataException(msg, e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static String parseSQL(String sql, JdbcWorkspace ws, JdbcManager manager)
/*     */     throws DataException
/*     */   {
/* 617 */     boolean doUnicodeConv = allowUnicodeConversion(sql, ws, manager);
/* 618 */     if (doUnicodeConv == true)
/*     */     {
/* 620 */       sql = fixUnicodeQuery(sql, manager);
/*     */     }
/*     */ 
/* 623 */     ArrayList queryModifiers = (ArrayList)m_queryModifiers.get(manager);
/* 624 */     if (queryModifiers != null)
/*     */     {
/* 626 */       Iterator iterator = queryModifiers.iterator();
/* 627 */       while (iterator.hasNext())
/*     */       {
/* 629 */         Class modifier = (Class)iterator.next();
/*     */         try
/*     */         {
/* 632 */           sql = (String)ClassHelperUtils.executeStaticMethod(modifier, "modifyQuery", new Object[] { sql, ws }, new Class[] { ClassHelper.m_stringClass, ClassHelper.m_objectClass });
/*     */         }
/*     */         catch (NoSuchMethodException e)
/*     */         {
/* 638 */           Report.trace(null, null, e);
/*     */         }
/*     */         catch (IllegalAccessException e)
/*     */         {
/* 643 */           Report.trace(null, null, e);
/*     */         }
/*     */         catch (InvocationTargetException e)
/*     */         {
/* 648 */           Throwable t = e.getTargetException();
/* 649 */           if (t instanceof DataException)
/*     */           {
/* 651 */             throw ((DataException)t);
/*     */           }
/* 653 */           if (t instanceof RuntimeException)
/*     */           {
/* 655 */             throw ((RuntimeException)t);
/*     */           }
/* 657 */           if (t instanceof Error)
/*     */           {
/* 659 */             throw ((Error)t);
/*     */           }
/*     */ 
/* 663 */           String msg = LocaleUtils.encodeMessage("csJdbcErrorWhileModifyingQuery", null, sql);
/* 664 */           throw new DataException(msg, e);
/*     */         }
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 670 */     DatabaseConfigData config = manager.m_config;
/* 671 */     if ((ws == null) || (config.getValueAsInt("JdbcFormatType", 0) == 0))
/*     */     {
/* 675 */       return sql;
/*     */     }
/* 677 */     StringBuffer buffer = new StringBuffer();
/*     */ 
/* 680 */     int index = 0;
/* 681 */     int beginIndex = 0;
/* 682 */     while ((index = sql.indexOf("{ts '", beginIndex)) > 0)
/*     */     {
/* 684 */       buffer.append(sql.substring(beginIndex, index));
/* 685 */       beginIndex = index;
/* 686 */       index = sql.indexOf("'}", beginIndex);
/* 687 */       if (index < 0)
/*     */       {
/* 689 */         manager.debugMsg("Error in parsing SQL for date.");
/* 690 */         String msg = LocaleUtils.encodeMessage("csDbDateParseError", null, sql);
/* 691 */         throw new DataException(msg);
/*     */       }
/* 693 */       index += "'}".length();
/*     */ 
/* 696 */       IdcDateFormat outputFormatter = (IdcDateFormat)config.getValue("JdbcOutputFormatter");
/*     */       String dateStr;
/* 697 */       if (outputFormatter != null)
/*     */       {
/* 700 */         String dateStr = sql.substring(beginIndex, index);
/*     */         Date date;
/*     */         try
/*     */         {
/* 703 */           date = LocaleUtils.parseODBC(dateStr);
/*     */         }
/*     */         catch (ServiceException e)
/*     */         {
/* 707 */           String msg = LocaleUtils.encodeMessage("csDbDateParseError2", e.getMessage(), sql);
/*     */ 
/* 709 */           throw new DataException(msg, e);
/*     */         }
/* 711 */         dateStr = outputFormatter.format(date);
/*     */       }
/*     */       else
/*     */       {
/* 715 */         dateStr = sql.substring(beginIndex + "{ts '".length(), index - "'}".length());
/*     */       }
/*     */ 
/* 718 */       buffer.append("'");
/* 719 */       buffer.append(dateStr);
/* 720 */       buffer.append("'");
/* 721 */       beginIndex = index;
/*     */     }
/*     */ 
/* 724 */     buffer.append(sql.substring(beginIndex));
/* 725 */     return buffer.toString();
/*     */   }
/*     */ 
/*     */   public static boolean allowUnicodeConversion(String sql, JdbcWorkspace ws, JdbcManager manager)
/*     */   {
/* 730 */     if (((ws != null) && (manager.m_config.getValueAsBool("SkipUnicodeSQLQueryConversion", false))) || (!manager.useUnicode()))
/*     */     {
/* 733 */       return false;
/*     */     }
/*     */ 
/* 737 */     char[] sqlChars = sql.toCharArray();
/* 738 */     boolean isAllowed = true;
/* 739 */     for (int i = 0; i < sqlChars.length; ++i)
/*     */     {
/* 741 */       if (Character.isSpaceChar(sqlChars[i])) {
/*     */         continue;
/*     */       }
/*     */ 
/* 745 */       switch (sqlChars[i])
/*     */       {
/*     */       case 'A':
/*     */       case 'a':
/* 750 */         if ((sqlChars.length > i + 4) && (((sqlChars[(i + 1)] == 'L') || (sqlChars[(i + 1)] == 'l'))) && (((sqlChars[(i + 2)] == 'T') || (sqlChars[(i + 2)] == 't'))) && (((sqlChars[(i + 3)] == 'E') || (sqlChars[(i + 3)] == 'e'))) && (((sqlChars[(i + 4)] == 'R') || (sqlChars[(i + 4)] == 'r'))))
/*     */         {
/* 757 */           isAllowed = false; } break;
/*     */       case 'C':
/*     */       case 'c':
/* 763 */         if ((sqlChars.length > i + 5) && (((sqlChars[(i + 1)] == 'R') || (sqlChars[(i + 1)] == 'r'))) && (((sqlChars[(i + 2)] == 'E') || (sqlChars[(i + 2)] == 'e'))) && (((sqlChars[(i + 3)] == 'A') || (sqlChars[(i + 3)] == 'a'))) && (((sqlChars[(i + 4)] == 'T') || (sqlChars[(i + 4)] == 't'))) && (((sqlChars[(i + 5)] == 'E') || (sqlChars[(i + 5)] == 'e'))))
/*     */         {
/* 771 */           isAllowed = false; } break;
/*     */       case 'D':
/*     */       case 'd':
/* 777 */         if ((sqlChars.length > i + 3) && (((sqlChars[(i + 1)] == 'R') || (sqlChars[(i + 1)] == 'r'))) && (((sqlChars[(i + 2)] == 'O') || (sqlChars[(i + 2)] == 'o'))) && (((sqlChars[(i + 3)] == 'P') || (sqlChars[(i + 3)] == 'p'))))
/*     */         {
/* 783 */           isAllowed = false;
/*     */         }
/*     */       }
/*     */ 
/* 787 */       break;
/*     */     }
/* 789 */     return isAllowed;
/*     */   }
/*     */ 
/*     */   protected static String fixUnicodeQuery(String sql, JdbcManager manager)
/*     */   {
/* 797 */     boolean openQuote = true;
/*     */ 
/* 799 */     int index = -1;
/* 800 */     int len = sql.length();
/* 801 */     int curIndex = 0;
/* 802 */     StringBuffer sb = new StringBuffer();
/* 803 */     while ((curIndex < len) && ((index = sql.indexOf(39, curIndex)) != -1))
/*     */     {
/* 805 */       String tmpStr = sql.substring(curIndex, index);
/* 806 */       sb.append(tmpStr);
/*     */ 
/* 808 */       if ((!openQuote) && (index < len - 1) && (sql.charAt(index + 1) == '\''))
/*     */       {
/* 812 */         curIndex = index + 2;
/* 813 */         sb.append("''");
/*     */       }
/*     */ 
/* 818 */       if ((openQuote) && (!tmpStr.trim().toLowerCase().endsWith("n")) && (!tmpStr.endsWith("ts ")))
/*     */       {
/* 821 */         sb.append('N');
/*     */       }
/* 823 */       sb.append('\'');
/* 824 */       curIndex = index + 1;
/* 825 */       openQuote = !openQuote;
/*     */     }
/*     */ 
/* 828 */     if (curIndex < len)
/*     */     {
/* 830 */       sb.append(sql.substring(curIndex));
/*     */     }
/* 832 */     String query = sb.toString();
/*     */ 
/* 839 */     return query;
/*     */   }
/*     */ 
/*     */   public static void checkAndLogDDL(String query, String providerName)
/*     */   {
/* 844 */     if ((query == null) || (query.length() < 3))
/*     */     {
/* 846 */       return;
/*     */     }
/* 848 */     char[] queryChars = query.trim().toCharArray();
/* 849 */     if ((queryChars[0] == 's') || (queryChars[0] == 'S') || (queryChars[0] == 'u') || (queryChars[0] == 'U') || (queryChars[0] == 'i') || (queryChars[0] == 'I') || ((((queryChars[0] == 'd') || (queryChars[0] == 'D'))) && (((queryChars[1] == 'e') || (queryChars[1] == 'E')))))
/*     */     {
/* 855 */       return;
/*     */     }
/*     */ 
/* 858 */     IdcMessage msg = IdcMessageFactory.lc();
/* 859 */     msg.m_msgLocalized = (providerName + ":" + query);
/* 860 */     Log.infoEx(LocaleUtils.encodeMessage(msg), "database");
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 865 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 102630 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.jdbc.JdbcQueryUtils
 * JD-Core Version:    0.5.4
 */