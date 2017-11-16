/*     */ package intradoc.shared;
/*     */ 
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.util.Hashtable;
/*     */ 
/*     */ public class ComponentClassFactory extends DataResultSet
/*     */ {
/*  39 */   public static Hashtable m_classes = new Hashtable();
/*  40 */   public static Hashtable m_classLoadOrder = new Hashtable();
/*     */ 
/*  47 */   public static Hashtable m_classObjects = new Hashtable();
/*     */ 
/*  49 */   public String m_tableName = "ClassAliases";
/*     */ 
/*     */   public ComponentClassFactory()
/*     */   {
/*     */   }
/*     */ 
/*     */   public ComponentClassFactory(String tableName)
/*     */   {
/*  59 */     this.m_tableName = tableName;
/*     */   }
/*     */ 
/*     */   public DataResultSet shallowClone()
/*     */   {
/*  65 */     DataResultSet rset = new ComponentClassFactory();
/*  66 */     initShallow(rset);
/*     */ 
/*  68 */     return rset;
/*     */   }
/*     */ 
/*     */   public void initShallow(DataResultSet rset)
/*     */   {
/*  74 */     super.initShallow(rset);
/*     */   }
/*     */ 
/*     */   public void loadClasses(DataBinder binder) throws DataException
/*     */   {
/*  79 */     DataResultSet rset = (DataResultSet)binder.getResultSet(this.m_tableName);
/*  80 */     if (rset == null)
/*     */     {
/*  82 */       return;
/*     */     }
/*     */ 
/*  85 */     if (getNumFields() == 0)
/*     */     {
/*  87 */       copy(rset);
/*     */     }
/*     */     else
/*     */     {
/*  91 */       merge("classname", rset, false);
/*     */     }
/*     */ 
/*  95 */     String[] fields = { "classname", "location" };
/*  96 */     FieldInfo[] info = ResultSetUtils.createInfoList(rset, fields, true);
/*     */ 
/*  98 */     String[] orderField = { "loadOrder" };
/*  99 */     FieldInfo[] loadInfo = ResultSetUtils.createInfoList(rset, orderField, false);
/*     */ 
/* 102 */     for (rset.first(); rset.isRowPresent(); rset.next())
/*     */     {
/* 104 */       String className = rset.getStringValue(info[0].m_index);
/* 105 */       String location = rset.getStringValue(info[1].m_index);
/*     */ 
/* 107 */       int newLoadOrder = 1;
/* 108 */       if (loadInfo[0].m_index > -1)
/*     */       {
/* 111 */         newLoadOrder = NumberUtils.parseInteger(rset.getStringValue(loadInfo[0].m_index), 1);
/*     */       }
/*     */ 
/* 116 */       int oldLoadOrder = NumberUtils.parseInteger((String)m_classLoadOrder.get(className), 1);
/*     */ 
/* 118 */       String oldLocation = (String)m_classes.get(className);
/*     */ 
/* 121 */       String loadOrder = newLoadOrder + "";
/*     */ 
/* 124 */       if ((oldLocation != null) && (oldLoadOrder > newLoadOrder))
/*     */       {
/* 127 */         location = oldLocation;
/* 128 */         loadOrder = oldLoadOrder + "";
/*     */       }
/*     */ 
/* 131 */       m_classes.put(className, location);
/*     */ 
/* 134 */       m_classLoadOrder.put(className, loadOrder);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object createClassInstance(String className, String defaultLocation, String message)
/*     */     throws ServiceException
/*     */   {
/* 142 */     String location = (String)m_classes.get(className);
/* 143 */     if (location == null)
/*     */     {
/* 145 */       location = defaultLocation;
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 150 */       Class newClass = (Class)m_classObjects.get(location);
/* 151 */       if (newClass == null)
/*     */       {
/* 153 */         newClass = Class.forName(location);
/* 154 */         if (newClass != null)
/*     */         {
/* 156 */           m_classObjects.put(location, newClass);
/*     */         }
/*     */       }
/* 159 */       if (newClass != null)
/*     */       {
/* 161 */         return newClass.newInstance();
/*     */       }
/*     */     }
/*     */     catch (ClassNotFoundException e)
/*     */     {
/* 166 */       ClassNotFoundException orig = e;
/* 167 */       IdcMessage errMsg = computeCreateClassInstanceErrorMsg(className, location, defaultLocation);
/* 168 */       e = new ClassNotFoundException(LocaleUtils.encodeMessage(errMsg));
/* 169 */       e.setStackTrace(orig.getStackTrace());
/* 170 */       if (message != null)
/*     */       {
/* 172 */         throw new ServiceException(message, e);
/*     */       }
/* 174 */       throw new ServiceException(e);
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/* 178 */       if (message != null)
/*     */       {
/* 180 */         throw new ServiceException(message, t);
/*     */       }
/* 182 */       throw new ServiceException(t);
/*     */     }
/* 184 */     IdcMessage errMsg = computeCreateClassInstanceErrorMsg(className, location, defaultLocation);
/* 185 */     throw new ServiceException(null, 0, errMsg);
/*     */   }
/*     */ 
/*     */   protected static IdcMessage computeCreateClassInstanceErrorMsg(String className, String location, String defaultLocation)
/*     */   {
/* 194 */     if (location.equals(defaultLocation))
/*     */     {
/* 196 */       return IdcMessageFactory.lc("apUnableToInstantiateClass2", new Object[] { className, location });
/*     */     }
/* 198 */     return IdcMessageFactory.lc("apUnableToInstantiateClass", new Object[] { className, location, defaultLocation });
/*     */   }
/*     */ 
/*     */   public String getTableName()
/*     */   {
/* 203 */     return this.m_tableName;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 208 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 76253 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.ComponentClassFactory
 * JD-Core Version:    0.5.4
 */