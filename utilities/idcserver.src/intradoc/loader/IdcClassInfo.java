/*     */ package intradoc.loader;
/*     */ 
/*     */ import intradoc.io.IdcByteHandlerUtils;
/*     */ import intradoc.io.zip.IdcZipEnvironment;
/*     */ import java.lang.reflect.Field;
/*     */ import java.lang.reflect.InvocationTargetException;
/*     */ import java.lang.reflect.Method;
/*     */ import java.lang.reflect.Modifier;
/*     */ 
/*     */ public class IdcClassInfo
/*     */ {
/*     */   protected static final String VERSION_INFO_STRING_NAME = "IDC_VERSION_INFO";
/*     */   protected static final String VERSION_INFO_METHOD_NAME = "idcVersionInfo";
/*  38 */   protected static final byte[] VERSION_INFO_STRING_BYTES = "IDC_VERSION_INFO".getBytes();
/*  39 */   protected static final byte[] VERSION_INFO_METHOD_BYTES = "idcVersionInfo".getBytes();
/*     */   protected static Class ClassObject;
/*     */   public IdcLoaderElement m_origin;
/*     */   public String m_className;
/*     */   public byte[] m_classBytes;
/*     */   public Class m_class;
/*     */   public boolean m_isInterface;
/*     */   public boolean m_isRegularClass;
/*     */   public boolean m_isInnerClass;
/*     */   public Object m_versionInfo;
/*     */   protected String m_string;
/*     */ 
/*     */   public IdcClassInfo(IdcLoaderElement origin, String name)
/*     */   {
/*  60 */     this.m_origin = origin;
/*  61 */     this.m_className = name;
/*     */   }
/*     */ 
/*     */   protected void init()
/*     */   {
/*  70 */     this.m_isInterface = this.m_class.isInterface();
/*     */ 
/*  72 */     this.m_isInnerClass = (this.m_className.indexOf(36) >= 0);
/*     */ 
/*  74 */     this.m_isRegularClass = (!this.m_class.isEnum());
/*     */ 
/*  78 */     if ((this.m_isInnerClass) || (!this.m_isRegularClass) || (!this.m_className.startsWith("intradoc.")))
/*     */       return;
/*  80 */     int verbosity = this.m_origin.m_zipenv.m_verbosity;
/*  81 */     byte[] lookupBytes = (this.m_isInterface) ? VERSION_INFO_STRING_BYTES : VERSION_INFO_METHOD_BYTES;
/*  82 */     if (IdcByteHandlerUtils.findLastBytesInBytes(lookupBytes, this.m_classBytes) < 0)
/*     */     {
/*  84 */       String lookupString = (this.m_isInterface) ? "IDC_VERSION_INFO" : "idcVersionInfo";
/*  85 */       String suffix = (this.m_isInterface) ? "" : "()";
/*  86 */       if (verbosity >= 4)
/*     */       {
/*  88 */         this.m_origin.m_loader.report(4, new Object[] { this.m_className, ".", lookupString, suffix, " is missing." });
/*     */       }
/*     */     }
/*  91 */     if (null == System.getProperty("idc.loader.lookupVersionInfoAtStartup"))
/*     */       return;
/*  93 */     lookupVersionInfo();
/*  94 */     if (verbosity < 7)
/*     */       return;
/*  96 */     this.m_origin.m_loader.report(7, new Object[] { this.m_string });
/*     */   }
/*     */ 
/*     */   public Object lookupVersionInfo()
/*     */   {
/* 109 */     IdcLoader loader = this.m_origin.m_loader;
/* 110 */     int verbosity = this.m_origin.m_zipenv.m_verbosity;
/* 111 */     this.m_versionInfo = null;
/* 112 */     if ((this.m_isInnerClass) || (!this.m_isRegularClass))
/*     */     {
/* 114 */       if (verbosity >= 7)
/*     */       {
/* 116 */         loader.report(7, new Object[] { this.m_className, (this.m_isInnerClass) ? " is inner class" : " is not a regular class", "ignoring version info" });
/*     */       }
/*     */ 
/* 119 */       return null;
/*     */     }
/* 121 */     if (null == ClassObject)
/*     */     {
/*     */       try
/*     */       {
/* 125 */         ClassObject = Class.forName("java.lang.Object");
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 130 */         if (verbosity >= 1)
/*     */         {
/* 132 */           loader.report(1, new Object[] { e });
/*     */         }
/*     */       }
/*     */     }
/* 136 */     Throwable t = null;
/*     */     try
/*     */     {
/* 139 */       int classModifiers = this.m_class.getModifiers();
/* 140 */       boolean isAccessible = true;
/* 141 */       if (!Modifier.isPublic(classModifiers))
/*     */       {
/* 143 */         isAccessible = false;
/* 144 */         if (verbosity >= 4)
/*     */         {
/* 146 */           loader.report(4, new Object[] { this.m_className, " is not public!" });
/*     */         }
/*     */       }
/* 149 */       if (isAccessible)
/*     */       {
/* 151 */         if (this.m_isInterface)
/*     */         {
/* 153 */           Field field = this.m_class.getField("IDC_VERSION_INFO");
/* 154 */           int modifiers = field.getModifiers();
/* 155 */           if (!Modifier.isStatic(modifiers))
/*     */           {
/* 157 */             isAccessible = false;
/* 158 */             if (verbosity >= 4)
/*     */             {
/* 160 */               loader.report(4, new Object[] { this.m_className, ".", "IDC_VERSION_INFO", " is not static!" });
/*     */             }
/*     */           }
/* 163 */           if (!Modifier.isPublic(modifiers))
/*     */           {
/* 165 */             isAccessible = false;
/* 166 */             if (verbosity >= 4)
/*     */             {
/* 168 */               loader.report(4, new Object[] { this.m_className, ".", "IDC_VERSION_INFO", " is not public!" });
/*     */             }
/*     */           }
/* 171 */           if (isAccessible)
/*     */           {
/* 173 */             if (verbosity >= 7)
/*     */             {
/* 175 */               loader.report(7, new Object[] { "accessing ", this.m_className, ".", "IDC_VERSION_INFO" });
/*     */             }
/* 177 */             this.m_versionInfo = field.get(null);
/*     */           }
/*     */         }
/*     */         else
/*     */         {
/* 182 */           Class[] argTypes = { ClassObject };
/* 183 */           Method method = this.m_class.getMethod("idcVersionInfo", argTypes);
/* 184 */           int modifiers = method.getModifiers();
/* 185 */           if (!Modifier.isStatic(modifiers))
/*     */           {
/* 187 */             isAccessible = false;
/* 188 */             if (verbosity >= 4)
/*     */             {
/* 190 */               loader.report(4, new Object[] { this.m_className, ".", "idcVersionInfo", "(Object) is not static!" });
/*     */             }
/*     */           }
/* 193 */           if (!Modifier.isPublic(modifiers))
/*     */           {
/* 195 */             isAccessible = false;
/* 196 */             if (verbosity >= 4)
/*     */             {
/* 198 */               loader.report(4, new Object[] { this.m_className, ".", "idcVersionInfo", "(Object) is not public!" });
/*     */             }
/*     */           }
/* 201 */           if (isAccessible)
/*     */           {
/* 203 */             if (verbosity >= 7)
/*     */             {
/* 205 */               loader.report(7, new Object[] { "invoking ", this.m_className, ".", "idcVersionInfo", "(null)" });
/*     */             }
/* 207 */             Object[] arguments = { null };
/* 208 */             this.m_versionInfo = method.invoke(null, arguments);
/*     */           }
/*     */         }
/*     */       }
/*     */     }
/*     */     catch (ClassCastException e)
/*     */     {
/* 215 */       t = e;
/*     */     }
/*     */     catch (ExceptionInInitializerError e)
/*     */     {
/* 219 */       t = e;
/*     */     }
/*     */     catch (IllegalAccessException e)
/*     */     {
/* 223 */       t = e;
/*     */     }
/*     */     catch (InvocationTargetException e)
/*     */     {
/* 227 */       t = e.getCause();
/*     */     }
/*     */     catch (NoSuchFieldException e)
/*     */     {
/* 231 */       t = e;
/*     */     }
/*     */     catch (NoSuchMethodException e)
/*     */     {
/* 235 */       t = e;
/*     */     }
/*     */     catch (LinkageError e)
/*     */     {
/* 239 */       t = e;
/*     */     }
/* 241 */     if ((null != t) && (verbosity >= 6))
/*     */     {
/* 243 */       loader.report(6, new Object[] { t });
/*     */     }
/*     */ 
/* 246 */     this.m_string = computeString();
/* 247 */     return this.m_versionInfo;
/*     */   }
/*     */ 
/*     */   protected String computeString()
/*     */   {
/* 257 */     if (null == this.m_origin)
/*     */     {
/* 259 */       return super.toString();
/*     */     }
/* 261 */     StringBuffer str = new StringBuffer();
/* 262 */     if (null != this.m_class)
/*     */     {
/* 264 */       str.append(this.m_className);
/* 265 */       if (null != this.m_versionInfo)
/*     */       {
/* 267 */         str.append(" (");
/* 268 */         String versionInfo = (this.m_versionInfo instanceof String) ? (String)this.m_versionInfo : this.m_versionInfo.toString();
/*     */ 
/* 270 */         str.append(versionInfo);
/* 271 */         str.append(')');
/*     */       }
/* 273 */       str.append(" from ");
/* 274 */       str.append(this.m_origin.m_entryPath);
/*     */     }
/* 276 */     return str.toString();
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 282 */     if (null == this.m_string)
/*     */     {
/* 284 */       this.m_string = computeString();
/*     */     }
/* 286 */     return this.m_string;
/*     */   }
/*     */ 
/*     */   public void clear()
/*     */   {
/* 291 */     if (this.m_origin != null)
/*     */     {
/* 293 */       this.m_origin.clear();
/* 294 */       this.m_origin = null;
/*     */     }
/* 296 */     if (ClassObject != null)
/*     */     {
/* 298 */       ClassObject = null;
/*     */     }
/* 300 */     if (this.m_class == null)
/*     */       return;
/* 302 */     this.m_class = null;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 309 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98962 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.loader.IdcClassInfo
 * JD-Core Version:    0.5.4
 */