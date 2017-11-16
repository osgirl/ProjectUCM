/*     */ package intradoc.common;
/*     */ 
/*     */ import intradoc.util.IdcException;
/*     */ import java.io.BufferedInputStream;
/*     */ import java.io.BufferedReader;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.lang.reflect.Constructor;
/*     */ import java.lang.reflect.InvocationTargetException;
/*     */ import java.lang.reflect.Method;
/*     */ import java.util.Map;
/*     */ import java.util.concurrent.ConcurrentHashMap;
/*     */ 
/*     */ public class ClassHelperUtils
/*     */ {
/*     */   public static final int F_MUST_EXIST = 1;
/*     */   public static final int F_DEFAULT_RUN = 0;
/*     */   public static final int F_CONVERT_TO_STD_EXCEPTIONS = 1;
/*     */   public static final int F_SUPPRESS_EXCEPTIONS = 2;
/*     */   public static final String CLASS_KEY = "class";
/*     */   public static final String OBJECT_KEY = "object";
/*     */   public static final String ARGUMENTS_KEY = "arguments";
/*     */   public static final String PARAMETERS_KEY = "parameters";
/*     */   public static final String METHOD_TYPE_SELECTOR_KEY = "methodTypeSelector";
/*     */   public static final String SUPPRESS_ACCESS_CONTROL_CHECK_KEY = "suppressAccessControlCheck";
/*     */   public static final String METHOD_KEY = "method";
/*     */   public static final String REPORT_EXEC_STATUS_KEY = "reportExecStatus";
/*  62 */   protected static Object m_syncObj = new Object();
/*  63 */   protected static ConcurrentHashMap m_methodCache = new ConcurrentHashMap();
/*     */ 
/*     */   public static void clearMethodCache()
/*     */   {
/*  74 */     synchronized (m_syncObj)
/*     */     {
/*  76 */       m_methodCache.clear();
/*     */     }
/*     */   }
/*     */ 
/*     */   public static ClassHelper createClassHelperRef(String className) throws ServiceException
/*     */   {
/*  82 */     ClassHelper con = new ClassHelper();
/*  83 */     con.initWithoutInstatiate(className);
/*  84 */     return con;
/*     */   }
/*     */ 
/*     */   public static ClassHelper createClassHelper(String className) throws ServiceException
/*     */   {
/*  89 */     ClassHelper con = new ClassHelper();
/*  90 */     con.init(className);
/*  91 */     return con;
/*     */   }
/*     */ 
/*     */   public static boolean isInstanceOf(String className, Object obj)
/*     */   {
/*  96 */     boolean isInstance = false;
/*     */     try
/*     */     {
/*  99 */       ClassHelper classHlpr = createClassHelperRef(className);
/* 100 */       Class c = classHlpr.getClassRep();
/* 101 */       isInstance = c.isInstance(obj);
/*     */     }
/*     */     catch (Exception ignore)
/*     */     {
/* 105 */       if (SystemUtils.m_verbose)
/*     */       {
/* 107 */         Report.debug(null, null, ignore);
/*     */       }
/*     */     }
/* 110 */     return isInstance;
/*     */   }
/*     */ 
/*     */   public static Class assertclass(String name)
/*     */   {
/*     */     try
/*     */     {
/* 117 */       return Class.forName(name);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 121 */       throw new AssertionError(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Class createClass(String name) throws ServiceException
/*     */   {
/*     */     Class c;
/*     */     try
/*     */     {
/* 130 */       c = Class.forName(name);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 134 */       throw new ServiceException(e);
/*     */     }
/* 136 */     return c;
/*     */   }
/*     */ 
/*     */   public static Object createInstance(Class c) throws ServiceException
/*     */   {
/*     */     Object obj;
/*     */     try
/*     */     {
/* 144 */       obj = c.newInstance();
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 149 */       throw new ServiceException(e);
/*     */     }
/* 151 */     return obj;
/*     */   }
/*     */ 
/*     */   public static IOException convertToIOException(Throwable callException, String methodName)
/*     */   {
/* 156 */     Throwable extractedException = null;
/* 157 */     if (callException instanceof InvocationTargetException)
/*     */     {
/* 159 */       InvocationTargetException targetException = (InvocationTargetException)callException;
/* 160 */       extractedException = targetException.getTargetException();
/*     */     }
/*     */     else
/*     */     {
/* 164 */       extractedException = callException;
/*     */     }
/* 166 */     if (extractedException instanceof IOException)
/*     */     {
/* 168 */       return (IOException)extractedException;
/*     */     }
/* 170 */     String msg = LocaleUtils.encodeMessage("csUnableToExecMethod", null, methodName);
/*     */ 
/* 172 */     IOException ioExcep = new IOException(msg);
/* 173 */     ioExcep.initCause(extractedException);
/* 174 */     return ioExcep;
/*     */   }
/*     */ 
/*     */   public static void convertToStandardExceptionAndThrow(Throwable callException, String methodName) throws ServiceException
/*     */   {
/* 179 */     Exception e = convertToStandardException(callException, methodName);
/* 180 */     throw ((ServiceException)e);
/*     */   }
/*     */ 
/*     */   public static Exception convertToStandardException(Throwable callException, String methodName)
/*     */     throws ServiceException
/*     */   {
/* 186 */     Throwable extractedException = null;
/* 187 */     if (callException instanceof InvocationTargetException)
/*     */     {
/* 189 */       InvocationTargetException targetException = (InvocationTargetException)callException;
/* 190 */       extractedException = targetException.getTargetException();
/*     */     }
/*     */     else
/*     */     {
/* 194 */       extractedException = callException;
/*     */     }
/* 196 */     if (extractedException instanceof ServiceException)
/*     */     {
/* 198 */       return (ServiceException)extractedException;
/*     */     }
/* 200 */     String msg = LocaleUtils.encodeMessage("csUnableToExecMethod", null, methodName);
/*     */ 
/* 202 */     ServiceException serviceExcep = new ServiceException(msg);
/* 203 */     serviceExcep.initCause(extractedException);
/* 204 */     return serviceExcep;
/*     */   }
/*     */ 
/*     */   public static void convertAndThrowUsableException(Throwable callException, String methodName) throws IOException, IdcException
/*     */   {
/* 209 */     Throwable extractedException = null;
/* 210 */     if (callException instanceof InvocationTargetException)
/*     */     {
/* 212 */       InvocationTargetException targetException = (InvocationTargetException)callException;
/* 213 */       extractedException = targetException.getTargetException();
/*     */     }
/*     */     else
/*     */     {
/* 217 */       extractedException = callException;
/*     */     }
/* 219 */     if (extractedException instanceof IdcException)
/*     */     {
/* 221 */       throw ((IdcException)extractedException);
/*     */     }
/* 223 */     if (extractedException instanceof IOException)
/*     */     {
/* 225 */       throw ((IOException)extractedException);
/*     */     }
/* 227 */     String msg = LocaleUtils.encodeMessage("csUnableToExecMethod", null, methodName);
/*     */ 
/* 229 */     ServiceException serviceExcep = new ServiceException(msg);
/* 230 */     serviceExcep.initCause(extractedException);
/* 231 */     throw serviceExcep;
/*     */   }
/*     */ 
/*     */   public static Object executeMethodSuppressException(Object invokee, String methodName, Object[] args)
/*     */   {
/*     */     try
/*     */     {
/* 238 */       return executeMethodEx(invokee, methodName, args, null, null, null, null, null);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 242 */       if (Report.m_verbose)
/*     */       {
/* 244 */         Report.trace("system", "Suppressed exception:", e);
/*     */       }
/*     */     }
/* 247 */     return null;
/*     */   }
/*     */ 
/*     */   public static Object executeMethodSuppressThrowable(Object invokee, String methodName, Object[] args)
/*     */   {
/*     */     try
/*     */     {
/* 254 */       return executeMethodSuppressException(invokee, methodName, args);
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/* 258 */       if (Report.m_verbose)
/*     */       {
/* 260 */         Report.trace("system", "Suppressed throwable:", t);
/*     */       }
/*     */     }
/* 263 */     return null;
/*     */   }
/*     */ 
/*     */   public static Object executeMethodConvertToStandardExceptions(Object invokee, String methodName, Object[] args)
/*     */     throws ServiceException
/*     */   {
/*     */     try
/*     */     {
/* 271 */       return executeMethodEx(invokee, methodName, args, null, null, null, null, null);
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/* 275 */       convertToStandardExceptionAndThrow(t, methodName);
/*     */     }
/* 277 */     return null;
/*     */   }
/*     */ 
/*     */   public static Object executeMethodWithArgs(Object invokee, String methodName, Object[] args)
/*     */     throws NoSuchMethodException, IllegalAccessException, InvocationTargetException
/*     */   {
/* 283 */     return executeMethodEx(invokee, methodName, args, null, null, null, null, null);
/*     */   }
/*     */ 
/*     */   public static Object executeMethodWithoutClasses(Object invokee, String methodName, Object[] args)
/*     */     throws NoSuchMethodException, IllegalAccessException, InvocationTargetException
/*     */   {
/* 289 */     return executeMethodEx(invokee, methodName, args, null, null, null, null, null);
/*     */   }
/*     */ 
/*     */   public static Object executeMethod(Object invokee, String methodName, Object[] args, Class[] argClasses)
/*     */     throws NoSuchMethodException, IllegalAccessException, InvocationTargetException
/*     */   {
/* 295 */     return executeMethodEx(invokee, methodName, args, argClasses, null, null, null, null);
/*     */   }
/*     */ 
/*     */   public static Object executeMethodEx(Object invokee, String methodName, Object[] args, Class[] argParams, Object altInvokee, String altMethodName, Object[] altArgs, Class[] altArgParams)
/*     */     throws NoSuchMethodException, IllegalAccessException, InvocationTargetException
/*     */   {
/* 302 */     IdcMethodHolder methodHolder = getMethodHolder(invokee.getClass(), methodName, args, argParams);
/* 303 */     if ((!methodHolder.m_hasValidMethod) && (altInvokee != null) && (altMethodName != null))
/*     */     {
/* 305 */       methodHolder = getMethodHolder(altInvokee.getClass(), altMethodName, altArgs, altArgParams);
/*     */     }
/*     */ 
/* 308 */     if (methodHolder != null)
/*     */     {
/* 310 */       return methodHolder.invokeMethod(invokee, args, false);
/*     */     }
/* 312 */     return null;
/*     */   }
/*     */ 
/*     */   public static boolean executeMethodReportStatus(Object invokee, String methodName)
/*     */     throws IllegalAccessException, InvocationTargetException
/*     */   {
/* 318 */     IdcMethodHolder methodHolder = getMethodHolder(invokee.getClass(), methodName, null, null);
/* 319 */     Boolean returnObj = Boolean.FALSE;
/* 320 */     if (methodHolder != null)
/*     */     {
/*     */       try
/*     */       {
/* 324 */         returnObj = (Boolean)methodHolder.invokeMethod(invokee, null, true);
/*     */       }
/*     */       catch (NoSuchMethodException e)
/*     */       {
/*     */       }
/*     */     }
/*     */ 
/* 331 */     return returnObj.booleanValue();
/*     */   }
/*     */ 
/*     */   public static Object executeStaticMethodSuppressException(Class invokeeClass, String methodName, Object[] args)
/*     */   {
/*     */     try
/*     */     {
/* 338 */       return executeStaticMethodEx(invokeeClass, methodName, args, null, null, null, null, null);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 342 */       if (Report.m_verbose)
/*     */       {
/* 344 */         Report.trace("system", null, e);
/*     */       }
/*     */     }
/* 347 */     return null;
/*     */   }
/*     */ 
/*     */   public static Object executeStaticMethodConvertToStandardExceptions(Class invokeeClass, String methodName, Object[] args)
/*     */     throws ServiceException
/*     */   {
/*     */     try
/*     */     {
/* 355 */       return executeStaticMethodEx(invokeeClass, methodName, args, null, null, null, null, null);
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/* 359 */       convertToStandardExceptionAndThrow(t, methodName);
/*     */     }
/* 361 */     return null;
/*     */   }
/*     */ 
/*     */   public static Object executeStaticMethodWithArgs(Class invokeeClass, String methodName, Object[] args)
/*     */     throws NoSuchMethodException, IllegalAccessException, InvocationTargetException
/*     */   {
/* 367 */     return executeStaticMethodEx(invokeeClass, methodName, args, null, null, null, null, null);
/*     */   }
/*     */ 
/*     */   public static Object executeStaticMethod(Class invokeeClass, String methodName, Object[] args, Class[] argTypes)
/*     */     throws NoSuchMethodException, IllegalAccessException, InvocationTargetException
/*     */   {
/* 374 */     return executeStaticMethodEx(invokeeClass, methodName, args, argTypes, null, null, null, null);
/*     */   }
/*     */ 
/*     */   public static Object executeStaticMethodWithoutClasses(Class invokeeClass, String methodName, Object[] args)
/*     */     throws NoSuchMethodException, IllegalAccessException, InvocationTargetException
/*     */   {
/* 380 */     return executeStaticMethodEx(invokeeClass, methodName, args, null, null, null, null, null);
/*     */   }
/*     */ 
/*     */   public static Object executeStaticMethodEx(Class invokeeClass, String methodName, Object[] args, Class[] argParams, Class altInvokeeClass, String altMethodName, Object[] altArgs, Class[] altArgParams)
/*     */     throws NoSuchMethodException, IllegalAccessException, InvocationTargetException
/*     */   {
/* 387 */     IdcMethodHolder methodHolder = getMethodHolder(invokeeClass, methodName, args, argParams);
/* 388 */     if ((((methodHolder == null) || (!methodHolder.m_hasValidMethod))) && (altInvokeeClass != null) && (altMethodName != null))
/*     */     {
/* 390 */       methodHolder = getMethodHolder(altInvokeeClass, altMethodName, altArgs, altArgParams);
/*     */     }
/*     */ 
/* 393 */     if (methodHolder != null)
/*     */     {
/* 395 */       return methodHolder.invokeMethod(null, args, false);
/*     */     }
/* 397 */     return null;
/*     */   }
/*     */ 
/*     */   public static Object executeIdcMethodConvertToStandardExceptions(Object invokee, IdcMethodHolder methodHolder, Object[] args)
/*     */     throws ServiceException
/*     */   {
/* 403 */     String methodName = methodHolder.m_methodName;
/*     */     try
/*     */     {
/* 406 */       return methodHolder.invokeMethod(invokee, args, false);
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/* 410 */       convertToStandardExceptionAndThrow(t, methodName);
/*     */     }
/* 412 */     return null;
/*     */   }
/*     */ 
/*     */   public static IdcMethodHolder getMethodHolder(Class invokeeClass, String methodName, Class[] params, int flags)
/*     */     throws ServiceException
/*     */   {
/* 418 */     IdcMethodHolder methodHolder = getMethodHolder(invokeeClass, methodName, null, params);
/* 419 */     if ((!methodHolder.m_hasValidMethod) && ((flags & 0x1) != 0))
/*     */     {
/* 421 */       throw new ServiceException(null, "syClassHelperMethodDoesNotExist", new Object[] { methodName, invokeeClass.toString() });
/*     */     }
/* 423 */     return methodHolder;
/*     */   }
/*     */ 
/*     */   protected static IdcMethodHolder getMethodHolder(Class invokeeClass, String methodName, Object[] args, Class[] params)
/*     */   {
/* 428 */     if ((invokeeClass == null) || (methodName == null) || (methodName.trim().length() == 0))
/*     */     {
/* 430 */       return null;
/*     */     }
/* 432 */     if (params == null)
/*     */     {
/* 434 */       params = getParamClasses(args);
/*     */     }
/*     */ 
/* 438 */     IdcMethodHashKey hashKey = new IdcMethodHashKey();
/* 439 */     hashKey.m_methodName = methodName;
/* 440 */     hashKey.m_paramClasses = params;
/*     */ 
/* 442 */     IdcMethodHolder idcMethod = null;
/* 443 */     Map methods = (Map)m_methodCache.get(invokeeClass);
/* 444 */     if (methods == null)
/*     */     {
/* 446 */       methods = new ConcurrentHashMap();
/* 447 */       m_methodCache.put(invokeeClass, methods);
/*     */     }
/* 449 */     idcMethod = (IdcMethodHolder)methods.get(hashKey);
/* 450 */     if (idcMethod == null)
/*     */     {
/* 452 */       idcMethod = new IdcMethodHolder();
/* 453 */       idcMethod.init(invokeeClass, methodName, params, args);
/* 454 */       methods.put(hashKey, idcMethod);
/*     */     }
/*     */ 
/* 457 */     return idcMethod;
/*     */   }
/*     */ 
/*     */   public static boolean checkMethodExistence(Class invokeeClass, String methodName, Class[] params)
/*     */   {
/* 462 */     IdcMethodHolder holder = getMethodHolder(invokeeClass, methodName, null, params);
/*     */ 
/* 464 */     boolean exists = false;
/* 465 */     if (holder != null)
/*     */     {
/* 467 */       exists = holder.m_hasValidMethod;
/*     */     }
/* 469 */     return exists;
/*     */   }
/*     */ 
/*     */   public static Class[] getParamClasses(Object[] params)
/*     */   {
/* 474 */     if (params == null)
/*     */     {
/* 476 */       return new Class[0];
/*     */     }
/* 478 */     int len = params.length;
/* 479 */     Class[] classArray = new Class[len];
/*     */ 
/* 481 */     for (int i = 0; i < len; ++i)
/*     */     {
/* 483 */       if (params[i] == null) {
/*     */         continue;
/*     */       }
/*     */ 
/* 487 */       classArray[i] = params[i].getClass();
/*     */     }
/*     */ 
/* 490 */     return classArray;
/*     */   }
/*     */ 
/*     */   public static Object findMatchingMethod(Class invokee, String name, Class[] paramTypes, Object[] paramObj, boolean isConstructor)
/*     */     throws ServiceException
/*     */   {
/* 496 */     if ((name == null) && (!isConstructor))
/*     */     {
/* 498 */       return null;
/*     */     }
/*     */ 
/* 501 */     Object[] ma = null;
/* 502 */     if (isConstructor)
/*     */     {
/* 504 */       ma = invokee.getConstructors();
/*     */     }
/*     */     else
/*     */     {
/* 508 */       ma = invokee.getMethods();
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 513 */       for (int i = 0; i < ma.length; ++i)
/*     */       {
/* 515 */         Class[] param = null;
/* 516 */         String newName = null;
/* 517 */         if (isConstructor)
/*     */         {
/* 519 */           Constructor c = (Constructor)ma[i];
/* 520 */           param = c.getParameterTypes();
/* 521 */           newName = c.getName();
/*     */         }
/*     */         else
/*     */         {
/* 525 */           Method m = (Method)ma[i];
/* 526 */           param = m.getParameterTypes();
/* 527 */           newName = m.getName();
/*     */         }
/*     */ 
/* 530 */         if ((name != null) && (!name.equals(newName)))
/*     */         {
/*     */           continue;
/*     */         }
/*     */ 
/* 536 */         if (param.length != paramObj.length)
/*     */         {
/*     */           continue;
/*     */         }
/*     */ 
/* 541 */         boolean sameMethod = true;
/* 542 */         for (int j = 0; j < param.length; ++j)
/*     */         {
/* 544 */           if (param[j].isPrimitive())
/*     */           {
/* 547 */             if (((param[j].equals(Integer.TYPE)) && (paramObj[j] instanceof Integer)) || ((param[j].equals(Short.TYPE)) && (paramObj[j] instanceof Short)) || ((param[j].equals(Long.TYPE)) && (paramObj[j] instanceof Long)) || ((param[j].equals(Float.TYPE)) && (paramObj[j] instanceof Float)) || ((param[j].equals(Double.TYPE)) && (paramObj[j] instanceof Double)) || ((param[j].equals(Byte.TYPE)) && (paramObj[j] instanceof Byte)) || ((param[j].equals(Boolean.TYPE)) && (paramObj[j] instanceof Boolean))) continue; if ((param[j].equals(Character.TYPE)) && (paramObj[j] instanceof Character))
/*     */             {
/*     */               continue;
/*     */             }
/*     */ 
/*     */           }
/*     */ 
/* 560 */           if ((paramObj[j] == null) || (param[j].isInstance(paramObj[j])) || (paramTypes[j] == null) || (param[j].equals(paramTypes[j]))) {
/*     */             continue;
/*     */           }
/* 563 */           sameMethod = false;
/* 564 */           break;
/*     */         }
/*     */ 
/* 569 */         if (sameMethod)
/*     */         {
/* 571 */           return ma[i];
/*     */         }
/*     */       }
/*     */     }
/*     */     catch (Exception exp)
/*     */     {
/* 577 */       throw new ServiceException(exp);
/*     */     }
/* 579 */     return null;
/*     */   }
/*     */ 
/*     */   public static Object executeMethod(Map invokeInfo, int methodSelectorFlag)
/*     */     throws Exception
/*     */   {
/*     */     try
/*     */     {
/* 595 */       return executeMethodEx(invokeInfo);
/*     */     }
/*     */     catch (Exception exception)
/*     */     {
/* 599 */       switch (methodSelectorFlag)
/*     */       {
/*     */       case 0:
/* 603 */         throw exception;
/*     */       case 1:
/*     */       case 2:
/*     */       }
/*     */     }
/* 607 */     String methodName = (String)invokeInfo.get("method");
/* 608 */     convertToStandardExceptionAndThrow(exception, methodName);
/* 609 */     break label67:
/*     */ 
/* 613 */     if (Report.m_verbose)
/*     */     {
/* 615 */       Report.trace("system", null, exception);
/*     */     }
/*     */ 
/* 621 */     label67: return null;
/*     */   }
/*     */ 
/*     */   public static Object executeMethodEx(Map invokeInfo) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException
/*     */   {
/* 626 */     Object returnObject = null;
/*     */ 
/* 628 */     IdcMethodHolder methodHolder = getMethodHolder(invokeInfo);
/*     */ 
/* 630 */     if (methodHolder != null)
/*     */     {
/* 632 */       Object invokee = invokeInfo.get("object");
/* 633 */       returnObject = methodHolder.invokeMethod(invokee, invokeInfo);
/*     */     }
/*     */ 
/* 636 */     return returnObject;
/*     */   }
/*     */ 
/*     */   public static IdcMethodHolder getMethodHolder(Map invokeInfo)
/*     */   {
/* 641 */     Class invokeeClass = (Class)invokeInfo.get("class");
/* 642 */     Object invokeeObject = invokeInfo.get("object");
/*     */ 
/* 644 */     if (invokeeClass == null)
/*     */     {
/* 646 */       invokeeClass = invokeeObject.getClass();
/*     */     }
/*     */ 
/* 649 */     String method = (String)invokeInfo.get("method");
/* 650 */     Object[] arguments = (Object[])(Object[])invokeInfo.get("arguments");
/* 651 */     Class[] parameters = (Class[])(Class[])invokeInfo.get("parameters");
/*     */ 
/* 653 */     IdcMethodHolder methodHolder = getMethodHolder(invokeeClass, method, arguments, parameters);
/* 654 */     return methodHolder;
/*     */   }
/*     */ 
/*     */   public static BufferedReader createBufferedReaderForClassResource(ClassLoader loader, String path)
/*     */     throws ServiceException
/*     */   {
/* 728 */     InputStream is = loader.getResourceAsStream(path);
/* 729 */     if (is == null)
/*     */     {
/* 731 */       throw new ServiceException(null, -16, "syFileUtilsFileNotFound", new Object[] { path });
/*     */     }
/* 733 */     BufferedInputStream bis = new BufferedInputStream(is);
/*     */     try
/*     */     {
/* 736 */       BufferedReader br = FileUtils.openDataReader(bis, null);
/* 737 */       return br;
/*     */     }
/*     */     catch (IOException ioe)
/*     */     {
/* 741 */       throw new ServiceException(ioe, -1, "csErrorLoadingResourceFile", new Object[] { path });
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 748 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 82891 $";
/*     */   }
/*     */ 
/*     */   static class IdcMethodHashKey
/*     */   {
/*     */     public String m_methodName;
/*     */     public Class[] m_paramClasses;
/*     */ 
/*     */     IdcMethodHashKey()
/*     */     {
/* 659 */       this.m_methodName = null;
/* 660 */       this.m_paramClasses = null;
/*     */     }
/*     */ 
/*     */     public boolean equals(Object obj)
/*     */     {
/* 665 */       if ((obj == null) || (!obj instanceof IdcMethodHashKey))
/*     */       {
/* 667 */         return false;
/*     */       }
/* 669 */       IdcMethodHashKey key = (IdcMethodHashKey)obj;
/* 670 */       if ((this.m_methodName == null) || (this.m_paramClasses == null) || (key.m_methodName == null) || (key.m_paramClasses == null))
/*     */       {
/* 672 */         return false;
/*     */       }
/*     */ 
/* 675 */       boolean result = true;
/* 676 */       if ((this.m_methodName.equals(key.m_methodName)) && (this.m_paramClasses.length == key.m_paramClasses.length))
/*     */       {
/* 678 */         for (int i = 0; i < this.m_paramClasses.length; ++i)
/*     */         {
/* 680 */           if (this.m_paramClasses[i] == key.m_paramClasses[i])
/*     */             continue;
/* 682 */           result = false;
/* 683 */           break;
/*     */         }
/*     */ 
/*     */       }
/*     */       else
/*     */       {
/* 689 */         result = false;
/*     */       }
/* 691 */       return result;
/*     */     }
/*     */ 
/*     */     public int hashCode()
/*     */     {
/* 697 */       if ((this.m_methodName == null) || (this.m_paramClasses == null))
/*     */       {
/* 700 */         return 0;
/*     */       }
/*     */ 
/* 703 */       int hashCode = this.m_methodName.hashCode();
/*     */ 
/* 705 */       for (int i = 1; (i < 4) && (i <= this.m_paramClasses.length); ++i)
/*     */       {
/* 707 */         Object toHash = this.m_paramClasses[(this.m_paramClasses.length - i)];
/* 708 */         if (toHash == null)
/*     */           continue;
/* 710 */         hashCode += toHash.hashCode();
/*     */       }
/*     */ 
/* 713 */       return hashCode;
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.ClassHelperUtils
 * JD-Core Version:    0.5.4
 */