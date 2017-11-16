/*     */ package intradoc.common;
/*     */ 
/*     */ import intradoc.util.IdcException;
/*     */ import intradoc.util.LocalizationHandlerBase;
/*     */ import java.util.Properties;
/*     */ 
/*     */ public class CommonLocalizationHandlerFactory
/*     */ {
/*     */   protected static boolean m_isInitialized;
/*     */   protected static Class m_handlerClass;
/*     */ 
/*     */   public static Class loadLocalizationClass(String classNameKey)
/*     */     throws ServiceException
/*     */   {
/*  33 */     String className = null;
/*     */     try
/*     */     {
/*  36 */       Properties env = (Properties)AppObjectRepository.getObject("environment");
/*  37 */       if (env == null)
/*     */       {
/*  40 */         return null;
/*     */       }
/*  42 */       className = env.getProperty(classNameKey);
/*  43 */       if (className == null)
/*     */       {
/*  45 */         return null;
/*     */       }
/*  47 */       Class cl = Class.forName(className);
/*  48 */       LocalizationHandlerBase obj = (LocalizationHandlerBase)cl.newInstance();
/*  49 */       obj.verifyPrerequisites();
/*  50 */       return cl;
/*     */     }
/*     */     catch (IdcException e)
/*     */     {
/*  54 */       throw new ServiceException(e, "apUnableToInstantiateClass2", new Object[] { className });
/*     */     }
/*     */     catch (IllegalAccessException e)
/*     */     {
/*  58 */       throw new ServiceException(e, "apUnableToInstantiateClass2", new Object[] { className });
/*     */     }
/*     */     catch (InstantiationException e)
/*     */     {
/*  62 */       throw new ServiceException(e, "apUnableToInstantiateClass2", new Object[] { className });
/*     */     }
/*     */     catch (ClassCastException e)
/*     */     {
/*  66 */       throw new ServiceException(e, "apUnableToInstantiateClass2", new Object[] { className });
/*     */     }
/*     */     catch (ClassNotFoundException e)
/*     */     {
/*  70 */       throw new ServiceException(e, "apUnableToInstantiateClass2", new Object[] { className });
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void verifyClasses(String[] classList) throws ServiceException
/*     */   {
/*     */     try
/*     */     {
/*  78 */       for (String className : classList)
/*     */       {
/*  80 */         Class.forName(className);
/*     */       }
/*     */     }
/*     */     catch (ClassNotFoundException e)
/*     */     {
/*  85 */       throw new ServiceException(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void init()
/*     */   {
/*  91 */     if (m_isInitialized)
/*     */     {
/*     */       return;
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 102 */       m_handlerClass = loadLocalizationClass("CommonLocalizationHandler");
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 106 */       Report.error("localization", null, e);
/*     */     }
/* 108 */     if (m_handlerClass == null)
/*     */     {
/* 110 */       m_handlerClass = DefaultCommonLocalizationHandler.class;
/*     */     }
/* 112 */     m_isInitialized = true;
/*     */   }
/*     */ 
/*     */   public static CommonLocalizationHandler createInstance()
/*     */   {
/* 118 */     CommonLocalizationHandler slh = createInstance(false);
/* 119 */     return slh;
/*     */   }
/*     */ 
/*     */   public static CommonLocalizationHandler createInstance(boolean useDefaultHandler)
/*     */   {
/* 124 */     CommonLocalizationHandler slh = null;
/*     */ 
/* 126 */     if (useDefaultHandler == true)
/*     */     {
/* 128 */       slh = new DefaultCommonLocalizationHandler();
/*     */     }
/*     */     else
/*     */     {
/* 132 */       if (!m_isInitialized)
/*     */       {
/* 134 */         init();
/*     */       }
/*     */       try
/*     */       {
/* 138 */         slh = (CommonLocalizationHandler)m_handlerClass.newInstance();
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 142 */         slh = new DefaultCommonLocalizationHandler();
/*     */       }
/*     */     }
/*     */ 
/* 146 */     return slh;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 151 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 83354 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.CommonLocalizationHandlerFactory
 * JD-Core Version:    0.5.4
 */