/*     */ package intradoc.provider;
/*     */ 
/*     */ import intradoc.common.IdcComparator;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.Sort;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class Providers
/*     */ {
/*  32 */   public static final String[] RESERVED_NAMES = { "SystemDatabase", "SystemServerSocket", "SystemJspServer", "SystemUserDatabase" };
/*     */ 
/*  35 */   public static final String[] COLUMNS = { "pName", "pDescription" };
/*  36 */   protected static DataResultSet m_providerSet = new DataResultSet(COLUMNS);
/*  37 */   protected static long m_lastModifiedTs = -2L;
/*     */ 
/*  39 */   protected static Vector m_providerDataList = new IdcVector();
/*  40 */   protected static Hashtable m_providerDataMap = new Hashtable();
/*     */ 
/*  43 */   protected static Hashtable m_providers = new Hashtable();
/*  44 */   protected static Vector m_providerList = new IdcVector();
/*     */ 
/*  46 */   protected static Vector m_prioritizedProviderList = new IdcVector();
/*  47 */   protected static Hashtable m_prioritizedProviderMap = new Hashtable();
/*     */ 
/*  51 */   protected static IdcComparator m_priorityCmp = new Object()
/*     */   {
/*     */     public int compare(Object obj1, Object obj2)
/*     */     {
/*  55 */       Provider v1 = (Provider)obj1;
/*  56 */       Provider v2 = (Provider)obj2;
/*  57 */       DataBinder b1 = v1.getProviderData();
/*  58 */       DataBinder b2 = v2.getProviderData();
/*     */ 
/*  60 */       int p1 = NumberUtils.parseInteger(b1.getLocal("Priority"), 1);
/*  61 */       int p2 = NumberUtils.parseInteger(b2.getLocal("Priority"), 1);
/*  62 */       if (p1 < p2)
/*     */       {
/*  64 */         return -1;
/*     */       }
/*  66 */       if (p1 == p2)
/*     */       {
/*  68 */         return 0;
/*     */       }
/*     */ 
/*  72 */       return 1;
/*     */     }
/*  51 */   };
/*     */ 
/*  78 */   protected static Hashtable m_allProviders = new Hashtable();
/*  79 */   public static Vector m_allProviderList = new IdcVector();
/*     */ 
/*  81 */   public static Hashtable m_registeredProviders = new Hashtable();
/*     */ 
/*     */   public static void loadResultSet(DataBinder binder) throws DataException
/*     */   {
/*  85 */     ResultSet rset = binder.getResultSet("Providers");
/*  86 */     if (rset == null)
/*     */     {
/*  88 */       String msg = LocaleUtils.encodeMessage("csTableDoesNotExist", null, "Providers");
/*     */ 
/*  90 */       throw new DataException(msg);
/*     */     }
/*     */ 
/*  93 */     m_providerSet.copy(rset);
/*     */   }
/*     */ 
/*     */   public static void loadBinder(DataBinder binder)
/*     */   {
/*  98 */     binder.addResultSet("Providers", m_providerSet);
/*     */   }
/*     */ 
/*     */   public static DataResultSet getResultSet()
/*     */   {
/* 106 */     return m_providerSet;
/*     */   }
/*     */ 
/*     */   public static long getLastModified()
/*     */   {
/* 111 */     return m_lastModifiedTs;
/*     */   }
/*     */ 
/*     */   public static void setLastModified(long ts)
/*     */   {
/* 116 */     m_lastModifiedTs = ts;
/*     */   }
/*     */ 
/*     */   public static DataBinder getProviderData(String name)
/*     */   {
/* 121 */     name = name.toLowerCase();
/* 122 */     return (DataBinder)m_providerDataMap.get(name);
/*     */   }
/*     */ 
/*     */   public static Vector getProviderDataList()
/*     */   {
/* 127 */     return m_providerDataList;
/*     */   }
/*     */ 
/*     */   public static void addProviderData(String name, DataBinder data)
/*     */   {
/* 132 */     name = name.toLowerCase();
/* 133 */     m_providerDataList.addElement(data);
/* 134 */     m_providerDataMap.put(name, data);
/*     */   }
/*     */ 
/*     */   public static void setProviderData(String name, DataBinder data)
/*     */   {
/* 139 */     name = name.toLowerCase();
/* 140 */     m_providerDataMap.put(name, data);
/*     */   }
/*     */ 
/*     */   public static Vector getProvidersOfType(String type)
/*     */   {
/* 145 */     Vector provs = new IdcVector();
/* 146 */     int num = m_providerList.size();
/* 147 */     for (int i = 0; i < num; ++i)
/*     */     {
/* 149 */       Provider prov = (Provider)m_providerList.elementAt(i);
/* 150 */       if (!prov.isProviderOfType(type))
/*     */         continue;
/* 152 */       provs.addElement(prov);
/*     */     }
/*     */ 
/* 155 */     return provs;
/*     */   }
/*     */ 
/*     */   public static Vector getProviderList()
/*     */   {
/* 160 */     return (Vector)m_providerList.clone();
/*     */   }
/*     */ 
/*     */   public static Hashtable getProviders()
/*     */   {
/* 165 */     return m_providers;
/*     */   }
/*     */ 
/*     */   public static void addProvider(String name, Provider provider)
/*     */   {
/* 170 */     name = name.toLowerCase();
/* 171 */     m_providerList.addElement(provider);
/* 172 */     m_prioritizedProviderList.addElement(provider);
/* 173 */     m_providers.put(name, provider);
/* 174 */     Sort.sortVector(m_prioritizedProviderList, m_priorityCmp);
/*     */   }
/*     */ 
/*     */   public static Provider getProvider(String name)
/*     */   {
/* 179 */     name = name.toLowerCase();
/* 180 */     return (Provider)m_providers.get(name);
/*     */   }
/*     */ 
/*     */   public static void addToAllProviderList(String name, Provider prov)
/*     */   {
/* 186 */     name = name.toLowerCase();
/* 187 */     m_allProviderList.addElement(prov);
/* 188 */     m_allProviders.put(name, prov);
/*     */   }
/*     */ 
/*     */   public static Vector getAllProviderList()
/*     */   {
/* 193 */     return m_allProviderList;
/*     */   }
/*     */ 
/*     */   public static Vector getPrioritizedProviderList()
/*     */   {
/* 198 */     return m_prioritizedProviderList;
/*     */   }
/*     */ 
/*     */   public static Provider getFromAllProviders(String name)
/*     */   {
/* 203 */     name = name.toLowerCase();
/* 204 */     return (Provider)m_allProviders.get(name);
/*     */   }
/*     */ 
/*     */   public static Provider registerProvider(String name, Provider provider)
/*     */   {
/* 209 */     name = name.toLowerCase();
/* 210 */     return (Provider)m_registeredProviders.put(name, provider);
/*     */   }
/*     */ 
/*     */   public static Provider getRegisteredProvider(String name)
/*     */   {
/* 215 */     name = name.toLowerCase();
/* 216 */     return (Provider)m_registeredProviders.get(name);
/*     */   }
/*     */ 
/*     */   public static void releaseConnections()
/*     */   {
/* 221 */     for (Enumeration en = m_registeredProviders.elements(); en.hasMoreElements(); )
/*     */     {
/* 223 */       Provider provider = (Provider)en.nextElement();
/* 224 */       provider.releaseConnection();
/*     */     }
/*     */   }
/*     */ 
/*     */   public static boolean isReservedName(String name)
/*     */   {
/* 231 */     int num = RESERVED_NAMES.length;
/* 232 */     for (int i = 0; i < num; ++i)
/*     */     {
/* 234 */       if (name.equalsIgnoreCase(RESERVED_NAMES[i]))
/*     */       {
/* 236 */         return true;
/*     */       }
/*     */     }
/* 239 */     return false;
/*     */   }
/*     */ 
/*     */   public static void removeProvider(String name)
/*     */   {
/* 246 */     name = name.toLowerCase();
/*     */ 
/* 248 */     Provider provider = getFromAllProviders(name);
/* 249 */     if (provider == null)
/*     */     {
/* 252 */       return;
/*     */     }
/*     */ 
/* 255 */     m_providerList.removeElement(provider);
/* 256 */     m_prioritizedProviderList.removeElement(provider);
/* 257 */     m_providers.remove(name);
/* 258 */     m_registeredProviders.remove(name);
/*     */ 
/* 260 */     provider.stopProvider();
/*     */ 
/* 262 */     provider.markState("editRemove");
/* 263 */     Sort.sortVector(m_prioritizedProviderList, m_priorityCmp);
/*     */   }
/*     */ 
/*     */   public static void enableProvider(String name, boolean isEnable)
/*     */     throws DataException, ServiceException
/*     */   {
/* 269 */     name = name.toLowerCase();
/*     */ 
/* 271 */     Provider provider = getFromAllProviders(name);
/* 272 */     if (provider == null)
/*     */     {
/* 275 */       return;
/*     */     }
/*     */ 
/* 278 */     boolean isInit = provider.checkState("IsInitialized", false);
/*     */ 
/* 280 */     String state = null;
/*     */     try
/*     */     {
/* 283 */       if (isEnable)
/*     */       {
/* 285 */         state = "enabled";
/* 286 */         if (isInit)
/*     */         {
/* 290 */           m_providerList.addElement(provider);
/* 291 */           m_prioritizedProviderList.addElement(provider);
/* 292 */           m_providers.put(name, provider);
/* 293 */           m_registeredProviders.put(name, provider);
/*     */ 
/* 296 */           provider.startProvider();
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 301 */         state = "disabled";
/* 302 */         if (isInit)
/*     */         {
/* 304 */           m_providerList.removeElement(provider);
/* 305 */           m_prioritizedProviderList.removeElement(provider);
/* 306 */           m_providers.remove(name);
/* 307 */           m_registeredProviders.remove(name);
/*     */ 
/* 309 */           provider.stopProvider(false);
/*     */         }
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 315 */       int errCode = -25;
/* 316 */       if (e instanceof ServiceException)
/*     */       {
/* 318 */         ServiceException se = (ServiceException)e;
/* 319 */         errCode = se.m_errorCode;
/*     */       }
/* 321 */       provider.markErrorState(errCode, e);
/*     */     }
/*     */     finally
/*     */     {
/* 325 */       Sort.sortVector(m_prioritizedProviderList, m_priorityCmp);
/* 326 */       provider.markState(state);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void clearAll()
/*     */   {
/* 332 */     m_providerList.clear();
/* 333 */     m_providerDataList.clear();
/* 334 */     m_providers.clear();
/* 335 */     m_registeredProviders.clear();
/* 336 */     m_prioritizedProviderList.clear();
/* 337 */     m_prioritizedProviderMap.clear();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 342 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98022 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.provider.Providers
 * JD-Core Version:    0.5.4
 */