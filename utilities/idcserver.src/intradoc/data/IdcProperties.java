/*     */ package intradoc.data;
/*     */ 
/*     */ import java.io.InputStream;
/*     */ import java.io.OutputStream;
/*     */ import java.io.PrintStream;
/*     */ import java.io.PrintWriter;
/*     */ import java.util.Collection;
/*     */ import java.util.Enumeration;
/*     */ import java.util.HashMap;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Iterator;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.Set;
/*     */ 
/*     */ public class IdcProperties extends Properties
/*     */   implements Parameters
/*     */ {
/*     */   protected Map m_props;
/*     */   protected Properties m_defaults;
/*     */ 
/*     */   public IdcProperties()
/*     */   {
/*  42 */     this.m_props = new HashMap();
/*     */   }
/*     */ 
/*     */   public IdcProperties(Properties defaultValues)
/*     */   {
/*  47 */     this.m_props = new HashMap();
/*  48 */     this.m_defaults = defaultValues;
/*     */   }
/*     */ 
/*     */   public IdcProperties(Map internalMap, Properties defaultValues)
/*     */   {
/*  53 */     this.m_props = internalMap;
/*  54 */     this.m_defaults = defaultValues;
/*     */   }
/*     */ 
/*     */   public void setMap(Map map)
/*     */   {
/*  59 */     this.m_props = map;
/*     */   }
/*     */ 
/*     */   public void setMap(HashMap map)
/*     */   {
/*  65 */     this.m_props = map;
/*     */   }
/*     */ 
/*     */   public void setDefaults(Properties defaultValues)
/*     */   {
/*  70 */     this.m_defaults = defaultValues;
/*     */   }
/*     */ 
/*     */   public void flatten()
/*     */   {
/*  78 */     Properties def = this.m_defaults;
/*  79 */     if (def == null)
/*     */     {
/*  81 */       return;
/*     */     }
/*     */ 
/*  84 */     Enumeration keys = def.keys();
/*  85 */     while (keys.hasMoreElements())
/*     */     {
/*  87 */       Object key = keys.nextElement();
/*  88 */       Object val = def.get(key);
/*  89 */       if (val != null)
/*     */       {
/*  91 */         put(key, val);
/*     */       }
/*     */     }
/*  94 */     this.m_defaults = null;
/*     */   }
/*     */ 
/*     */   public String get(String key)
/*     */   {
/* 101 */     return getProperty(key);
/*     */   }
/*     */ 
/*     */   public String getSystem(String key)
/*     */   {
/* 106 */     return getProperty(key);
/*     */   }
/*     */ 
/*     */   public Object get(Object obj)
/*     */   {
/* 116 */     Object rc = this.m_props.get(obj);
/* 117 */     if ((rc == null) && (this.m_defaults != null))
/*     */     {
/* 119 */       rc = this.m_defaults.get(obj);
/* 120 */       if ((rc == null) && (obj instanceof String))
/*     */       {
/* 122 */         rc = this.m_defaults.getProperty((String)obj);
/*     */       }
/*     */     }
/* 125 */     return rc;
/*     */   }
/*     */ 
/*     */   public String getProperty(String key)
/*     */   {
/* 131 */     Object obj = this.m_props.get(key);
/* 132 */     if ((obj == null) && (this.m_defaults != null))
/*     */     {
/* 134 */       obj = this.m_defaults.getProperty(key);
/*     */     }
/* 136 */     if (obj == null)
/*     */     {
/* 138 */       return null;
/*     */     }
/*     */ 
/* 141 */     if (obj instanceof String)
/*     */     {
/* 143 */       return (String)obj;
/*     */     }
/* 145 */     return obj.toString();
/*     */   }
/*     */ 
/*     */   public String getProperty(String key, String defaultValue)
/*     */   {
/* 151 */     String value = getProperty(key);
/* 152 */     if (value == null)
/*     */     {
/* 154 */       return defaultValue;
/*     */     }
/* 156 */     return value;
/*     */   }
/*     */ 
/*     */   public void list(PrintStream out)
/*     */   {
/* 162 */     throw new AssertionError("list() not implemented");
/*     */   }
/*     */ 
/*     */   public void list(PrintWriter out)
/*     */   {
/* 168 */     throw new AssertionError("list() not implemented");
/*     */   }
/*     */ 
/*     */   public void load(InputStream in)
/*     */   {
/* 174 */     throw new AssertionError("load() not implemented");
/*     */   }
/*     */ 
/*     */   public void loadFromXML(InputStream in)
/*     */   {
/* 180 */     throw new AssertionError("loadFromXML() not implemented.");
/*     */   }
/*     */ 
/*     */   public Enumeration propertyNames()
/*     */   {
/* 186 */     Enumeration en = new Enumeration()
/*     */     {
/*     */       public boolean m_inParent;
/*     */       public Enumeration m_parentEnum;
/*     */       public Iterator m_myKeys;
/*     */       public Map m_reportedKeys;
/*     */       public Object m_next;
/*     */ 
/*     */       public void checkSetup()
/*     */       {
/* 200 */         if (this.m_reportedKeys == null)
/*     */         {
/* 202 */           this.m_reportedKeys = new HashMap();
/*     */         }
/* 204 */         if (this.m_inParent)
/*     */         {
/* 206 */           if ((this.m_parentEnum == null) && (IdcProperties.this.m_defaults != null))
/*     */           {
/* 208 */             this.m_parentEnum = IdcProperties.this.m_defaults.propertyNames();
/*     */           }
/* 210 */           return;
/*     */         }
/* 212 */         if (this.m_myKeys != null)
/*     */           return;
/* 214 */         this.m_myKeys = IdcProperties.this.m_props.keySet().iterator();
/*     */       }
/*     */ 
/*     */       public boolean hasMoreElements()
/*     */       {
/* 220 */         if (this.m_next != null)
/*     */         {
/* 222 */           return true;
/*     */         }
/* 224 */         checkSetup();
/* 225 */         if (this.m_inParent)
/*     */         {
/* 227 */           if (this.m_parentEnum == null)
/*     */           {
/* 229 */             return false;
/*     */           }
/* 231 */           while (this.m_parentEnum.hasMoreElements())
/*     */           {
/* 233 */             Object tmp = this.m_parentEnum.nextElement();
/* 234 */             if (this.m_reportedKeys.get(tmp) == null)
/*     */             {
/* 236 */               this.m_next = tmp;
/* 237 */               return true;
/*     */             }
/*     */           }
/* 240 */           return false;
/*     */         }
/* 242 */         if (this.m_myKeys.hasNext())
/*     */         {
/* 244 */           return true;
/*     */         }
/* 246 */         this.m_inParent = true;
/* 247 */         return hasMoreElements();
/*     */       }
/*     */ 
/*     */       public Object nextElement()
/*     */       {
/* 252 */         if (this.m_next != null)
/*     */         {
/* 254 */           Object tmp = this.m_next;
/* 255 */           this.m_next = null;
/* 256 */           this.m_reportedKeys.put(tmp, tmp);
/* 257 */           return tmp;
/*     */         }
/* 259 */         checkSetup();
/* 260 */         if (this.m_inParent)
/*     */         {
/* 262 */           if (this.m_parentEnum == null)
/*     */           {
/* 265 */             this.m_myKeys.next();
/*     */           }
/*     */ 
/*     */           while (true)
/*     */           {
/* 270 */             Object tmp = this.m_parentEnum.nextElement();
/* 271 */             if (this.m_reportedKeys.get(tmp) == null)
/*     */             {
/* 273 */               this.m_reportedKeys.put(tmp, tmp);
/* 274 */               return tmp;
/*     */             }
/*     */           }
/*     */         }
/*     */ 
/* 279 */         if (this.m_myKeys.hasNext())
/*     */         {
/* 281 */           Object tmp = this.m_myKeys.next();
/* 282 */           this.m_reportedKeys.put(tmp, tmp);
/* 283 */           return tmp;
/*     */         }
/* 285 */         this.m_inParent = true;
/* 286 */         return nextElement();
/*     */       }
/*     */     };
/* 290 */     return en;
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public void save(OutputStream out, String comments)
/*     */   {
/* 297 */     throw new AssertionError("method not implemented");
/*     */   }
/*     */ 
/*     */   public Object setProperty(String key, String value)
/*     */   {
/* 303 */     if (value == null)
/*     */     {
/* 305 */       throw new NullPointerException(key);
/*     */     }
/* 307 */     return this.m_props.put(key, value);
/*     */   }
/*     */ 
/*     */   public void store(OutputStream out, String comments)
/*     */   {
/* 313 */     throw new AssertionError("method not implemented");
/*     */   }
/*     */ 
/*     */   public void storeToXML(OutputStream out, String comment)
/*     */   {
/* 319 */     throw new AssertionError("method not implemented");
/*     */   }
/*     */ 
/*     */   public void storeToXML(OutputStream out, String comment, String encoding)
/*     */   {
/* 325 */     throw new AssertionError("method not implemented");
/*     */   }
/*     */ 
/*     */   public void clear()
/*     */   {
/* 335 */     this.m_props.clear();
/*     */   }
/*     */ 
/*     */   protected Map cloneMap(Map inMap)
/*     */   {
/* 340 */     Map outMap = null;
/* 341 */     if (inMap instanceof HashMap)
/*     */     {
/* 343 */       HashMap m = (HashMap)inMap;
/* 344 */       outMap = (Map)m.clone();
/*     */     }
/* 346 */     else if (inMap instanceof Hashtable)
/*     */     {
/* 348 */       Hashtable m = (Hashtable)inMap;
/* 349 */       outMap = (Hashtable)m.clone();
/*     */     }
/*     */     else
/*     */     {
/*     */       try
/*     */       {
/* 355 */         outMap = (Map)inMap.getClass().newInstance();
/* 356 */         outMap.putAll(inMap);
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 360 */         CloneNotSupportedException cnes = new CloneNotSupportedException();
/* 361 */         cnes.initCause(e);
/* 362 */         AssertionError ae = new AssertionError();
/* 363 */         ae.initCause(cnes);
/* 364 */         throw ae;
/*     */       }
/*     */     }
/* 367 */     return outMap;
/*     */   }
/*     */ 
/*     */   public Object clone()
/*     */   {
/* 373 */     IdcProperties newobj = (IdcProperties)super.clone();
/*     */ 
/* 375 */     newobj.m_props = cloneMap(this.m_props);
/* 376 */     newobj.m_defaults = this.m_defaults;
/* 377 */     return newobj;
/*     */   }
/*     */ 
/*     */   public boolean contains(Object value)
/*     */   {
/* 383 */     return this.m_props.containsValue(value);
/*     */   }
/*     */ 
/*     */   public boolean containsKey(Object key)
/*     */   {
/* 389 */     return this.m_props.containsKey(key);
/*     */   }
/*     */ 
/*     */   public Enumeration elements()
/*     */   {
/* 395 */     throw new AssertionError("method not implemented");
/*     */   }
/*     */ 
/*     */   public Set entrySet()
/*     */   {
/* 402 */     return this.m_props.entrySet();
/*     */   }
/*     */ 
/*     */   public boolean equals(Object rop)
/*     */   {
/* 408 */     if (this == rop)
/*     */     {
/* 410 */       return true;
/*     */     }
/*     */ 
/* 413 */     if (rop instanceof IdcProperties)
/*     */     {
/* 415 */       IdcProperties r = (IdcProperties)rop;
/* 416 */       if ((((r.m_props == this.m_props) || (r.m_props.equals(this.m_props)))) && ((
/* 418 */         (r.m_defaults == this.m_defaults) || (r.m_defaults.equals(this.m_defaults)))))
/*     */       {
/* 421 */         return true;
/*     */       }
/*     */     }
/*     */ 
/* 425 */     return false;
/*     */   }
/*     */ 
/*     */   public int hashCode()
/*     */   {
/* 431 */     return this.m_props.hashCode() * super.hashCode();
/*     */   }
/*     */ 
/*     */   public boolean isEmpty()
/*     */   {
/* 437 */     return this.m_props.isEmpty();
/*     */   }
/*     */ 
/*     */   public Enumeration keys()
/*     */   {
/* 443 */     Set keyset = this.m_props.keySet();
/* 444 */     Iterator it = keyset.iterator();
/*     */ 
/* 446 */     Enumeration en = new Enumeration(it)
/*     */     {
/*     */       public boolean hasMoreElements()
/*     */       {
/* 450 */         return this.val$it.hasNext();
/*     */       }
/*     */ 
/*     */       public Object nextElement()
/*     */       {
/* 455 */         return this.val$it.next();
/*     */       }
/*     */     };
/* 458 */     return en;
/*     */   }
/*     */ 
/*     */   public Set keySet()
/*     */   {
/* 464 */     return this.m_props.keySet();
/*     */   }
/*     */ 
/*     */   public Object put(Object key, Object value)
/*     */   {
/* 470 */     if (value == null)
/*     */     {
/* 472 */       throw new NullPointerException(key.toString());
/*     */     }
/* 474 */     return this.m_props.put(key, value);
/*     */   }
/*     */ 
/*     */   public void putAll(Map map)
/*     */   {
/* 480 */     this.m_props.putAll(map);
/*     */   }
/*     */ 
/*     */   public void rehash()
/*     */   {
/*     */   }
/*     */ 
/*     */   public Object remove(Object key)
/*     */   {
/* 492 */     return this.m_props.remove(key);
/*     */   }
/*     */ 
/*     */   public int size()
/*     */   {
/* 498 */     return this.m_props.size();
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 504 */     return this.m_props.toString();
/*     */   }
/*     */ 
/*     */   public Collection values()
/*     */   {
/* 510 */     return this.m_props.values();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 517 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 92915 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.data.IdcProperties
 * JD-Core Version:    0.5.4
 */