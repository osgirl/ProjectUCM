/*     */ package intradoc.tools.common;
/*     */ 
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.util.IdcException;
/*     */ import java.lang.reflect.InvocationTargetException;
/*     */ import java.lang.reflect.Method;
/*     */ import java.util.List;
/*     */ 
/*     */ public class ComponentListWrapper
/*     */ {
/*     */   protected IdcClassLoaderWrapper m_loader;
/*     */   protected Object m_editor;
/*     */   protected Method m_enableOrDisableComponentMethod;
/*     */   protected Method m_isComponentNameUniqueMethod;
/*     */ 
/*     */   public ComponentListWrapper(IdcClassLoaderWrapper loader)
/*     */   {
/*  37 */     this.m_loader = loader;
/*     */   }
/*     */ 
/*     */   public void init(List<String> classpath, String productName)
/*     */     throws IdcException
/*     */   {
/*  50 */     IdcClassLoaderWrapper wrapper = this.m_loader;
/*  51 */     ClassLoader loader = wrapper.m_loader;
/*     */     try
/*     */     {
/*  54 */       if (classpath != null)
/*     */       {
/*  56 */         wrapper.initWithList(classpath);
/*     */       }
/*     */ 
/*  63 */       Class clazz = Class.forName("intradoc.server.utils.ComponentListManager", true, loader);
/*  64 */       Method method = clazz.getMethod("reset", new Class[0]);
/*  65 */       method.invoke(null, new Object[0]);
/*  66 */       method = clazz.getMethod("init", new Class[0]);
/*  67 */       method.invoke(null, new Object[0]);
/*  68 */       method = clazz.getMethod("getEditor", new Class[0]);
/*  69 */       Object editor = this.m_editor = method.invoke(null, new Object[0]);
/*  70 */       clazz = Class.forName("intradoc.server.utils.ComponentListEditor", true, loader);
/*  71 */       if (productName != null)
/*     */       {
/*  77 */         method = clazz.getMethod("setProductName", new Class[] { String.class });
/*  78 */         method.invoke(editor, new Object[] { productName });
/*  79 */         method = clazz.getMethod("loadComponents", new Class[0]);
/*  80 */         method.invoke(editor, new Object[0]);
/*     */       }
/*     */ 
/*  83 */       this.m_enableOrDisableComponentMethod = clazz.getMethod("enableOrDisableComponent", new Class[] { String.class, Boolean.TYPE });
/*  84 */       this.m_isComponentNameUniqueMethod = clazz.getMethod("isComponentNameUnique", new Class[] { String.class });
/*     */     }
/*     */     catch (InvocationTargetException ite)
/*     */     {
/*  88 */       Throwable t = ite.getCause();
/*  89 */       if (t instanceof IdcException)
/*     */       {
/*  91 */         throw ((IdcException)t);
/*     */       }
/*  93 */       throw new ServiceException(t);
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/*  97 */       throw new ServiceException(t);
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean isComponentInstalled(String componentName) throws IdcException
/*     */   {
/*     */     try
/*     */     {
/* 105 */       boolean rc = ((Boolean)this.m_isComponentNameUniqueMethod.invoke(this.m_editor, new Object[] { componentName })).booleanValue();
/* 106 */       return !rc;
/*     */     }
/*     */     catch (InvocationTargetException ite)
/*     */     {
/* 110 */       Throwable t = ite.getCause();
/* 111 */       if (t instanceof IdcException)
/*     */       {
/* 113 */         throw ((IdcException)t);
/*     */       }
/* 115 */       throw new ServiceException(t);
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/* 119 */       throw new ServiceException(t);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void enableOrDisableComponent(String componentNames, boolean isEnable) throws IdcException
/*     */   {
/*     */     try
/*     */     {
/* 127 */       this.m_enableOrDisableComponentMethod.invoke(this.m_editor, new Object[] { componentNames, Boolean.valueOf(isEnable) });
/*     */     }
/*     */     catch (InvocationTargetException ite)
/*     */     {
/* 131 */       Throwable t = ite.getCause();
/* 132 */       if (t instanceof IdcException)
/*     */       {
/* 134 */         throw ((IdcException)t);
/*     */       }
/* 136 */       throw new ServiceException(t);
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/* 140 */       throw new ServiceException(t);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 147 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98962 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.tools.common.ComponentListWrapper
 * JD-Core Version:    0.5.4
 */