/*     */ package intradoc.common;
/*     */ 
/*     */ import java.io.File;
/*     */ import java.lang.reflect.Constructor;
/*     */ import java.lang.reflect.Field;
/*     */ import java.lang.reflect.InvocationTargetException;
/*     */ import java.lang.reflect.Method;
/*     */ 
/*     */ public class ClassHelper
/*     */ {
/*  28 */   public static final Class m_throwableClass = new Throwable().getClass();
/*  29 */   public static final Class m_objectClass = new Object().getClass();
/*  30 */   public static final Class m_objectArrayClass = new Object[0].getClass();
/*  31 */   public static final Class m_stringClass = new String().getClass();
/*  32 */   public static final Class m_fileClass = new File(".").getClass();
/*  33 */   public static final Class m_threadClass = new Thread().getClass();
/*     */ 
/*  35 */   public static final Class m_executionContextClass = ClassHelperUtils.assertclass("intradoc.common.ExecutionContext");
/*     */   public Class m_class;
/*     */   public Object m_obj;
/*     */ 
/*     */   public Class init(String name)
/*     */     throws ServiceException
/*     */   {
/*  50 */     this.m_class = ClassHelperUtils.createClass(name);
/*  51 */     this.m_obj = ClassHelperUtils.createInstance(this.m_class);
/*     */ 
/*  53 */     return this.m_class;
/*     */   }
/*     */ 
/*     */   public Class init(String name, Object[] params)
/*     */     throws ServiceException
/*     */   {
/*     */     try
/*     */     {
/*  61 */       initRaw(name, params);
/*     */     }
/*     */     catch (InvocationTargetException e)
/*     */     {
/*  65 */       Throwable tException = e.getTargetException();
/*  66 */       if (tException instanceof ServiceException)
/*     */       {
/*  68 */         throw ((ServiceException)tException);
/*     */       }
/*  70 */       String msg = LocaleUtils.encodeMessage("csUnableToExecMethod", null, name);
/*     */ 
/*  72 */       throw new ServiceException(msg, tException);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/*  76 */       throw new ServiceException(e);
/*     */     }
/*     */ 
/*  79 */     return this.m_class;
/*     */   }
/*     */ 
/*     */   public Class initRaw(String name, Object[] params)
/*     */     throws InvocationTargetException, ServiceException
/*     */   {
/*  85 */     this.m_class = ClassHelperUtils.createClass(name);
/*  86 */     Class[] paramClasses = ClassHelperUtils.getParamClasses(params);
/*     */     try
/*     */     {
/*  89 */       Constructor con = getConstructor(paramClasses, params);
/*  90 */       this.m_obj = con.newInstance(params);
/*     */     }
/*     */     catch (InvocationTargetException e)
/*     */     {
/*  94 */       throw e;
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/*  98 */       throw new ServiceException(e);
/*     */     }
/* 100 */     return this.m_class;
/*     */   }
/*     */ 
/*     */   public Class initWithoutInstatiate(String name) throws ServiceException
/*     */   {
/* 105 */     this.m_class = ClassHelperUtils.createClass(name);
/* 106 */     return this.m_class;
/*     */   }
/*     */ 
/*     */   public Class initWithObject(Object obj)
/*     */   {
/* 111 */     this.m_obj = obj;
/* 112 */     this.m_class = obj.getClass();
/* 113 */     return this.m_class;
/*     */   }
/*     */ 
/*     */   public Object getClassInstance()
/*     */   {
/* 120 */     return this.m_obj;
/*     */   }
/*     */ 
/*     */   public Class getClassRep()
/*     */   {
/* 125 */     return this.m_class;
/*     */   }
/*     */ 
/*     */   public Object setObject(Object obj)
/*     */   {
/* 130 */     return this.m_obj = obj;
/*     */   }
/*     */ 
/*     */   public Object invoke(String name) throws ServiceException
/*     */   {
/* 135 */     return invoke(name, new Object[0]);
/*     */   }
/*     */ 
/*     */   public Object invoke(String name, Object param) throws ServiceException
/*     */   {
/* 140 */     return invoke(name, new Object[] { param });
/*     */   }
/*     */ 
/*     */   public Object invokeRaw(String name) throws ServiceException, InvocationTargetException
/*     */   {
/* 145 */     return invokeRaw(name, new Object[0]);
/*     */   }
/*     */ 
/*     */   public Object invokeRaw(String name, Object param) throws ServiceException, InvocationTargetException
/*     */   {
/* 150 */     return invokeRaw(name, new Object[] { param });
/*     */   }
/*     */ 
/*     */   public Object invoke(String name, Object[] param) throws ServiceException
/*     */   {
/*     */     try
/*     */     {
/* 157 */       return invokeRaw(name, param);
/*     */     }
/*     */     catch (InvocationTargetException e)
/*     */     {
/* 161 */       Throwable tException = e.getTargetException();
/* 162 */       if (tException instanceof ServiceException)
/*     */       {
/* 164 */         throw ((ServiceException)tException);
/*     */       }
/* 166 */       String msg = LocaleUtils.encodeMessage("csUnableToExecMethod", null, name);
/*     */ 
/* 168 */       throw new ServiceException(msg, tException);
/*     */     }
/*     */   }
/*     */ 
/*     */   public Object invokeRaw(String name, Object[] param)
/*     */     throws InvocationTargetException, ServiceException
/*     */   {
/* 175 */     return invokeRawEx(name, param, param);
/*     */   }
/*     */ 
/*     */   public Object invokeRawWithTypes(String name, Object[] param, Object[] types)
/*     */     throws InvocationTargetException, ServiceException
/*     */   {
/* 181 */     return invokeRawEx(name, param, types);
/*     */   }
/*     */ 
/*     */   public Object invokeRawEx(String name, Object[] param, Object[] types)
/*     */     throws InvocationTargetException, ServiceException
/*     */   {
/* 189 */     Class[] classArray = ClassHelperUtils.getParamClasses(types);
/*     */ 
/* 191 */     Method m = getMethod(name, classArray, types);
/*     */     try
/*     */     {
/* 195 */       return m.invoke(this.m_obj, param);
/*     */     }
/*     */     catch (IllegalAccessException e)
/*     */     {
/* 199 */       String msg = LocaleUtils.encodeMessage("csMethodIllegalAccess", null, name);
/*     */ 
/* 201 */       throw new ServiceException(msg);
/*     */     }
/*     */     catch (InvocationTargetException e)
/*     */     {
/* 205 */       throw e;
/*     */     }
/*     */   }
/*     */ 
/*     */   public Method getMethod(String methodName, Class[] paramTypes, Object[] paramObj)
/*     */     throws ServiceException
/*     */   {
/* 213 */     Method m = null;
/* 214 */     String oldErrorMsg = null;
/*     */     try
/*     */     {
/* 217 */       m = this.m_class.getMethod(methodName, paramTypes);
/*     */     }
/*     */     catch (NoSuchMethodException e)
/*     */     {
/* 221 */       oldErrorMsg = e.getMessage();
/*     */     }
/*     */     catch (SecurityException e)
/*     */     {
/* 225 */       throw new ServiceException(e);
/*     */     }
/*     */ 
/* 228 */     if (m == null)
/*     */     {
/* 232 */       m = (Method)findMatchingMethod(methodName, paramTypes, paramObj, false);
/*     */     }
/*     */ 
/* 236 */     if (m == null)
/*     */     {
/* 238 */       throw new ServiceException(oldErrorMsg);
/*     */     }
/*     */ 
/* 241 */     return m;
/*     */   }
/*     */ 
/*     */   public Object findMatchingMethod(String name, Class[] paramTypes, Object[] paramObj, boolean isConstructor)
/*     */     throws ServiceException
/*     */   {
/* 247 */     return ClassHelperUtils.findMatchingMethod(this.m_class, name, paramTypes, paramObj, isConstructor);
/*     */   }
/*     */ 
/*     */   public Constructor getConstructor(Class[] paramTypes, Object[] paramObj)
/*     */     throws ServiceException
/*     */   {
/* 253 */     Constructor m = null;
/* 254 */     String oldErrorMsg = null;
/*     */     try
/*     */     {
/* 257 */       m = this.m_class.getConstructor(paramTypes);
/*     */     }
/*     */     catch (NoSuchMethodException e)
/*     */     {
/* 261 */       oldErrorMsg = e.getMessage();
/*     */     }
/*     */     catch (SecurityException e)
/*     */     {
/* 265 */       throw new ServiceException(e);
/*     */     }
/*     */ 
/* 268 */     if (m == null)
/*     */     {
/* 272 */       m = (Constructor)findMatchingMethod(null, paramTypes, paramObj, true);
/*     */     }
/*     */ 
/* 275 */     if (m == null)
/*     */     {
/* 277 */       throw new ServiceException(oldErrorMsg);
/*     */     }
/*     */ 
/* 280 */     return m;
/*     */   }
/*     */ 
/*     */   public Field getField(String fieldName) throws ServiceException
/*     */   {
/*     */     try
/*     */     {
/* 287 */       return this.m_class.getField(fieldName);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 291 */       throw new ServiceException(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public Object getFieldValue(String fieldName) throws ServiceException
/*     */   {
/* 297 */     Field f = getField(fieldName);
/*     */     try
/*     */     {
/* 300 */       return f.get(this.m_obj);
/*     */     }
/*     */     catch (IllegalAccessException e)
/*     */     {
/* 304 */       throw new ServiceException(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setFieldValue(String fieldName, Object value) throws ServiceException
/*     */   {
/* 310 */     Field f = getField(fieldName);
/*     */     try
/*     */     {
/* 313 */       f.set(this.m_obj, value);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 317 */       throw new ServiceException(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean getBooleanValue(String fieldName) throws ServiceException
/*     */   {
/* 323 */     Field f = getField(fieldName);
/*     */     try
/*     */     {
/* 326 */       return f.getBoolean(this.m_obj);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 330 */       throw new ServiceException(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setBooleanValue(String fieldName, boolean value) throws ServiceException
/*     */   {
/* 336 */     Field f = getField(fieldName);
/*     */     try
/*     */     {
/* 339 */       f.setBoolean(this.m_obj, value);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 343 */       throw new ServiceException(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public byte getByteValue(String fieldName) throws ServiceException
/*     */   {
/* 349 */     Field f = getField(fieldName);
/*     */     try
/*     */     {
/* 352 */       return f.getByte(this.m_obj);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 356 */       throw new ServiceException(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setByteValue(String fieldName, byte value) throws ServiceException
/*     */   {
/* 362 */     Field f = getField(fieldName);
/*     */     try
/*     */     {
/* 365 */       f.setByte(this.m_obj, value);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 369 */       throw new ServiceException(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public char getCharValue(String fieldName) throws ServiceException
/*     */   {
/* 375 */     Field f = getField(fieldName);
/*     */     try
/*     */     {
/* 378 */       return f.getChar(this.m_obj);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 382 */       throw new ServiceException(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setCharValue(String fieldName, char value) throws ServiceException
/*     */   {
/* 388 */     Field f = getField(fieldName);
/*     */     try
/*     */     {
/* 391 */       f.setChar(this.m_obj, value);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 395 */       throw new ServiceException(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public double getDoubleValue(String fieldName) throws ServiceException
/*     */   {
/* 401 */     Field f = getField(fieldName);
/*     */     try
/*     */     {
/* 404 */       return f.getDouble(this.m_obj);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 408 */       throw new ServiceException(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setDoubleValue(String fieldName, double value) throws ServiceException
/*     */   {
/* 414 */     Field f = getField(fieldName);
/*     */     try
/*     */     {
/* 417 */       f.setDouble(this.m_obj, value);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 421 */       throw new ServiceException(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public float getFloatValue(String fieldName) throws ServiceException
/*     */   {
/* 427 */     Field f = getField(fieldName);
/*     */     try
/*     */     {
/* 430 */       return f.getFloat(this.m_obj);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 434 */       throw new ServiceException(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setFloatValue(String fieldName, float value) throws ServiceException
/*     */   {
/* 440 */     Field f = getField(fieldName);
/*     */     try
/*     */     {
/* 443 */       f.setFloat(this.m_obj, value);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 447 */       throw new ServiceException(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public int getIntValue(String fieldName) throws ServiceException
/*     */   {
/* 453 */     Field f = getField(fieldName);
/*     */     try
/*     */     {
/* 456 */       return f.getInt(this.m_obj);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 460 */       throw new ServiceException(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setIntValue(String fieldName, int value) throws ServiceException
/*     */   {
/* 466 */     Field f = getField(fieldName);
/*     */     try
/*     */     {
/* 469 */       f.setInt(this.m_obj, value);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 473 */       throw new ServiceException(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public long getLongValue(String fieldName) throws ServiceException
/*     */   {
/* 479 */     Field f = getField(fieldName);
/*     */     try
/*     */     {
/* 482 */       return f.getLong(this.m_obj);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 486 */       throw new ServiceException(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setLongValue(String fieldName, long value) throws ServiceException
/*     */   {
/* 492 */     Field f = getField(fieldName);
/*     */     try
/*     */     {
/* 495 */       f.setLong(this.m_obj, value);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 499 */       throw new ServiceException(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public short getShortValue(String fieldName) throws ServiceException
/*     */   {
/* 505 */     Field f = getField(fieldName);
/*     */     try
/*     */     {
/* 508 */       return f.getShort(this.m_obj);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 512 */       throw new ServiceException(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setShortValue(String fieldName, short value) throws ServiceException
/*     */   {
/* 518 */     Field f = getField(fieldName);
/*     */     try
/*     */     {
/* 521 */       f.setShort(this.m_obj, value);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 525 */       throw new ServiceException(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 531 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.ClassHelper
 * JD-Core Version:    0.5.4
 */