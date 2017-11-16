/*     */ package intradoc.util;
/*     */ 
/*     */ import java.lang.reflect.Constructor;
/*     */ import java.util.HashMap;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Map.Entry;
/*     */ import java.util.Set;
/*     */ import java.util.concurrent.ConcurrentHashMap;
/*     */ 
/*     */ public class MapUtils
/*     */ {
/*     */   protected static Constructor m_mapMaker;
/*     */ 
/*     */   public static Map createConcurrentMap()
/*     */   {
/*  47 */     if (null == m_mapMaker)
/*     */     {
/*     */       Class cl;
/*     */       try
/*     */       {
/*  52 */         cl = Class.forName("java.util.concurrent.ConcurrentHashMap");
/*     */       }
/*     */       catch (Exception ignore)
/*     */       {
/*     */         try
/*     */         {
/*  58 */           cl = Class.forName("java.util.Hashtable");
/*     */         }
/*     */         catch (Exception e)
/*     */         {
/*  62 */           return null;
/*     */         }
/*     */       }
/*  65 */       Class[] types = new Class[0];
/*     */       try
/*     */       {
/*  68 */         m_mapMaker = cl.getConstructor(types);
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/*  72 */         return null;
/*     */       }
/*     */     }
/*     */     try
/*     */     {
/*  77 */       Map map = (Map)m_mapMaker.newInstance((Object[])null);
/*  78 */       return map;
/*     */     }
/*     */     catch (Exception e) {
/*     */     }
/*  82 */     return null;
/*     */   }
/*     */ 
/*     */   public static Map createSynchronizedMap(int flags)
/*     */   {
/*  97 */     return new ConcurrentHashMap();
/*     */   }
/*     */ 
/*     */   public static Map fillMapFromOptionsList(Map map, List options)
/*     */   {
/* 113 */     if (null == map)
/*     */     {
/* 115 */       map = createConcurrentMap();
/*     */     }
/* 117 */     int numOptions = options.size();
/* 118 */     for (int i = 0; i < numOptions; ++i)
/*     */     {
/* 120 */       String option = (String)options.get(i);
/* 121 */       String value = "1";
/* 122 */       boolean defaultBoolean = true;
/* 123 */       if ((option.length() > 0) && ('!' == option.charAt(0)))
/*     */       {
/* 125 */         defaultBoolean = false;
/* 126 */         value = "0";
/* 127 */         option = option.substring(1);
/*     */       }
/* 129 */       int equalsIndex = option.indexOf(61, (defaultBoolean) ? 0 : 1);
/* 130 */       if (equalsIndex >= 0)
/*     */       {
/* 132 */         if (!defaultBoolean)
/*     */         {
/* 134 */           throw new IllegalArgumentException(option);
/*     */         }
/* 136 */         value = option.substring(equalsIndex + 1);
/* 137 */         option = option.substring(0, equalsIndex);
/*     */       }
/* 139 */       map.put(option, value);
/*     */     }
/*     */ 
/* 142 */     return map;
/*     */   }
/*     */ 
/*     */   public static Map fillMapFromOptionsString(Map map, String options)
/*     */   {
/* 159 */     if (null == map)
/*     */     {
/* 161 */       map = createConcurrentMap();
/*     */     }
/* 163 */     int lenTotal = options.length();
/* 164 */     char[] chars = new char[lenTotal];
/* 165 */     options.getChars(0, lenTotal, chars, 0);
/* 166 */     int start = 0;
/* 167 */     while (start < lenTotal)
/*     */     {
/* 169 */       for (int stop = start; stop < lenTotal; ++stop)
/*     */       {
/* 171 */         if (chars[stop] == ',') {
/*     */           break;
/*     */         }
/*     */       }
/*     */ 
/* 176 */       if (stop != start)
/*     */       {
/* 178 */         int len = stop - start;
/* 179 */         boolean booleanDefault = true;
/* 180 */         String value = "1";
/* 181 */         if ((len > 0) && ('!' == chars[start]))
/*     */         {
/* 183 */           booleanDefault = false;
/* 184 */           value = "0";
/* 185 */           ++start;
/* 186 */           --len;
/*     */         }
/* 188 */         int equals = start;
/*     */         do if (++equals >= stop)
/*     */             break;
/* 191 */         while ('=' != chars[equals]);
/*     */         String option;
/* 196 */         if (equals < stop)
/*     */         {
/* 198 */           if (!booleanDefault)
/*     */           {
/* 200 */             throw new IllegalArgumentException(new String(chars, start - 1, len + 1));
/*     */           }
/* 202 */           String option = new String(chars, start, equals - start);
/* 203 */           value = new String(chars, equals + 1, stop - equals - 1);
/*     */         }
/*     */         else
/*     */         {
/* 207 */           option = new String(chars, start, len);
/*     */         }
/* 209 */         map.put(option, value);
/*     */       }
/* 211 */       start = stop + 1;
/*     */     }
/*     */ 
/* 214 */     return map;
/*     */   }
/*     */ 
/*     */   public static boolean getBoolValueFromMap(Map options, String optionName, boolean defaultValue)
/*     */   {
/* 228 */     if ((null == options) || (null == optionName))
/*     */     {
/* 230 */       return defaultValue;
/*     */     }
/* 232 */     Object value = options.get(optionName);
/* 233 */     if (null == value)
/*     */     {
/* 235 */       return defaultValue;
/*     */     }
/*     */     try
/*     */     {
/* 239 */       if (value instanceof Boolean)
/*     */       {
/* 241 */         return ((Boolean)value).booleanValue();
/*     */       }
/* 243 */       if (value instanceof Number)
/*     */       {
/* 245 */         return ((Number)value).intValue() != 0;
/*     */       }
/*     */       String str;
/* 248 */       if (value instanceof String)
/*     */       {
/* 250 */         str = (String)value;
/*     */       }
/*     */       else
/*     */       {
/* 254 */         str = value.toString();
/*     */       }
/* 256 */       String str = str.trim();
/* 257 */       if (str.length() == 0)
/*     */       {
/* 259 */         return defaultValue;
/*     */       }
/* 261 */       char ch = Character.toUpperCase(str.charAt(0));
/* 262 */       if (defaultValue)
/*     */       {
/* 264 */         return (ch != '0') && (ch != 'F') && (ch != 'N');
/*     */       }
/* 266 */       return (ch == '1') || (ch == 'T') || (ch == 'Y');
/*     */     }
/*     */     catch (Exception ignore) {
/*     */     }
/* 270 */     return defaultValue;
/*     */   }
/*     */ 
/*     */   public static int getIntValueFromMap(Map options, String optionName, int defaultValue)
/*     */   {
/* 284 */     if ((null == options) || (null == optionName))
/*     */     {
/* 286 */       return defaultValue;
/*     */     }
/* 288 */     Object value = options.get(optionName);
/* 289 */     if (null == value)
/*     */     {
/* 291 */       return defaultValue;
/*     */     }
/*     */     try
/*     */     {
/* 295 */       if (value instanceof Number)
/*     */       {
/* 297 */         return ((Number)value).intValue();
/*     */       }
/* 299 */       if (value instanceof String)
/*     */       {
/* 301 */         return Integer.parseInt((String)value);
/*     */       }
/*     */ 
/* 305 */       return Integer.parseInt(value.toString());
/*     */     }
/*     */     catch (Exception ignore)
/*     */     {
/*     */     }
/* 310 */     return defaultValue;
/*     */   }
/*     */ 
/*     */   public static long getLongValueFromMap(Map options, String optionName, long defaultValue)
/*     */   {
/* 324 */     if ((null == options) || (null == optionName))
/*     */     {
/* 326 */       return defaultValue;
/*     */     }
/* 328 */     Object value = options.get(optionName);
/* 329 */     if (null == value)
/*     */     {
/* 331 */       return defaultValue;
/*     */     }
/*     */     try
/*     */     {
/* 335 */       if (value instanceof Number)
/*     */       {
/* 337 */         return ((Number)value).longValue();
/*     */       }
/* 339 */       if (value instanceof String)
/*     */       {
/* 341 */         return Long.parseLong((String)value);
/*     */       }
/*     */ 
/* 345 */       return Long.parseLong(value.toString());
/*     */     }
/*     */     catch (Exception ignore)
/*     */     {
/*     */     }
/* 350 */     return defaultValue;
/*     */   }
/*     */ 
/*     */   public static Map cloneMap(Map map)
/*     */   {
/* 363 */     if (map == null)
/*     */     {
/* 365 */       return map;
/*     */     }
/* 367 */     Map outMap = null;
/* 368 */     if (map instanceof HashMap)
/*     */     {
/* 370 */       HashMap m = (HashMap)map;
/* 371 */       outMap = (Map)m.clone();
/*     */     }
/* 373 */     else if (map instanceof ConcurrentHashMap)
/*     */     {
/* 376 */       outMap = new ConcurrentHashMap(map);
/*     */     }
/* 378 */     else if (map instanceof Hashtable)
/*     */     {
/* 380 */       Hashtable m = (Hashtable)map;
/* 381 */       outMap = (Hashtable)m.clone();
/*     */     }
/*     */ 
/* 385 */     outMap.isEmpty();
/* 386 */     return outMap;
/*     */   }
/*     */ 
/*     */   public static Map[] computeAttributeSets(Map source, String prefix, String sortSuffix, String sortDefault, char sep)
/*     */   {
/* 412 */     int prefixLen = prefix.length();
/* 413 */     Map data = new HashMap();
/* 414 */     Set s = source.entrySet();
/* 415 */     Iterator i = s.iterator();
/* 416 */     while (i.hasNext())
/*     */     {
/* 418 */       Map.Entry e = (Map.Entry)i.next();
/* 419 */       Object keyObj = e.getKey();
/* 420 */       if (keyObj instanceof String)
/*     */       {
/* 422 */         String key = (String)keyObj;
/* 423 */         if (key.startsWith(prefix))
/*     */         {
/* 425 */           boolean found = true;
/* 426 */           int index = key.indexOf(sep, prefixLen + 1);
/* 427 */           if (index == -1)
/*     */           {
/* 429 */             index = key.length();
/* 430 */             found = false;
/*     */           }
/* 432 */           String val = key.substring(prefixLen + 1, index);
/* 433 */           Map m = (Map)data.get(val);
/* 434 */           if (m == null)
/*     */           {
/* 436 */             m = new HashMap();
/* 437 */             data.put(val, m);
/* 438 */             m.put(prefix, val);
/*     */           }
/* 440 */           Object v = e.getValue();
/* 441 */           m.put(key, v);
/* 442 */           String shortKey = "";
/* 443 */           if (found)
/*     */           {
/* 445 */             shortKey = key.substring(index + 1);
/*     */           }
/* 447 */           m.put(shortKey, v);
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 452 */     Map[] list = new Map[data.size()];
/* 453 */     s = data.entrySet();
/* 454 */     i = s.iterator();
/* 455 */     for (int index = 0; i.hasNext(); ++index)
/*     */     {
/* 457 */       Map.Entry e = (Map.Entry)i.next();
/* 458 */       list[index] = ((Map)e.getValue());
/*     */     }
/*     */ 
/* 463 */     return list;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 469 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78304 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.util.MapUtils
 * JD-Core Version:    0.5.4
 */