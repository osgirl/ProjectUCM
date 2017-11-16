/*     */ package intradoc.common;
/*     */ 
/*     */ public class OsUtils
/*     */ {
/*     */   protected static boolean m_isOsFlagsInitialized;
/*     */   protected static String m_osName;
/*     */   protected static String m_osFamily;
/*     */   public static boolean m_hasAppletContext;
/*     */   public static final String OS_AIX = "aix";
/*     */   public static final String OS_AIX64 = "aix64";
/*     */   public static final String OS_FREEBSD = "freebsd";
/*     */   public static final String OS_FREEBSD64 = "freebsd64";
/*     */   public static final String OS_HPUX = "hpux";
/*     */   public static final String OS_HPUX64 = "hpux64";
/*     */   public static final String OS_HPUXIA = "hpux-ia";
/*     */   public static final String OS_HPUXIA64 = "hpux-ia64";
/*     */   public static final String OS_LINUX = "linux";
/*     */   public static final String OS_LINUX64 = "linux64";
/*     */   public static final String OS_LINUXS390 = "linux-s390";
/*     */   public static final String OS_LINUXS390X = "linux-s390x";
/*     */   public static final String OS_SOLARIS = "solaris";
/*     */   public static final String OS_SOLARIS64 = "solaris64";
/*     */   public static final String OS_SOLARISX86 = "solaris-x86";
/*     */   public static final String OS_SOLARISAMD64 = "solaris-amd64";
/*     */   public static final String OS_WIN32 = "win32";
/*     */   public static final String OS_WINDOWSAMD64 = "windows-amd64";
/*     */   public static final String OS_MACOSX = "osx";
/*     */   public static final String FAM_UNIX = "unix";
/*     */   public static final String FAM_WINDOWS = "windows";
/*     */ 
/*     */   public static void initializeOsFlags()
/*     */   {
/*  59 */     if (m_isOsFlagsInitialized)
/*     */     {
/*  61 */       return;
/*     */     }
/*     */ 
/*  64 */     synchronized ("unix")
/*     */     {
/*  66 */       if (m_isOsFlagsInitialized)
/*     */       {
/*  68 */         return;
/*     */       }
/*     */ 
/*     */       try
/*     */       {
/*  76 */         if ((m_osName == null) && (!m_hasAppletContext))
/*     */         {
/*  78 */           m_osName = System.getProperty("idc.osname");
/*     */         }
/*  80 */         if ((m_osFamily == null) && (!m_hasAppletContext))
/*     */         {
/*  82 */           m_osFamily = System.getProperty("idc.osfamily");
/*     */         }
/*     */ 
/*     */       }
/*     */       catch (Throwable t)
/*     */       {
/*  88 */         m_osName = null;
/*     */       }
/*     */ 
/*  92 */       if (m_osName == null)
/*     */       {
/*  94 */         String arch = System.getProperty("os.arch").toLowerCase();
/*  95 */         String osName = System.getProperty("os.name");
/*     */ 
/*  97 */         if (arch == null)
/*     */         {
/*  99 */           arch = "";
/*     */         }
/*     */ 
/* 102 */         m_osFamily = "unix";
/* 103 */         boolean is64 = false;
/* 104 */         if ((arch.endsWith("64")) || (arch.endsWith("w")))
/*     */         {
/* 106 */           is64 = true;
/*     */         }
/*     */ 
/* 109 */         if (osName != null)
/*     */         {
/* 111 */           if ((osName.equalsIgnoreCase("SunOS")) || (osName.equalsIgnoreCase("solaris")))
/*     */           {
/* 113 */             if (is64)
/*     */             {
/* 115 */               m_osName = "solaris-amd64";
/*     */             }
/* 117 */             else if ((arch.equals("sparc9")) || (arch.equals("sparcv9")))
/*     */             {
/* 119 */               m_osName = "solaris64";
/*     */             }
/* 121 */             else if (arch.equals("sparc"))
/*     */             {
/* 123 */               m_osName = "solaris";
/*     */             }
/*     */             else
/*     */             {
/* 127 */               m_osName = "solaris-x86";
/*     */             }
/*     */           }
/* 130 */           else if ((osName.equalsIgnoreCase("HP-UX")) || (osName.equalsIgnoreCase("hpux")))
/*     */           {
/* 132 */             if (is64)
/*     */             {
/* 134 */               if (arch.startsWith("pa"))
/*     */               {
/* 136 */                 m_osName = "hpux64";
/*     */               }
/*     */               else
/*     */               {
/* 140 */                 m_osName = "hpux-ia64";
/*     */               }
/*     */ 
/*     */             }
/* 145 */             else if (arch.startsWith("pa"))
/*     */             {
/* 147 */               m_osName = "hpux";
/*     */             }
/*     */             else
/*     */             {
/* 151 */               m_osName = "hpux-ia";
/*     */             }
/*     */ 
/*     */           }
/* 155 */           else if (osName.equalsIgnoreCase("aix"))
/*     */           {
/* 157 */             if (is64)
/*     */             {
/* 159 */               m_osName = "aix64";
/*     */             }
/*     */             else
/*     */             {
/* 163 */               m_osName = "aix";
/*     */             }
/*     */           }
/* 166 */           else if (osName.equalsIgnoreCase("freebsd"))
/*     */           {
/* 168 */             if (is64)
/*     */             {
/* 170 */               m_osName = "freebsd64";
/*     */             }
/*     */             else
/*     */             {
/* 174 */               m_osName = "freebsd";
/*     */             }
/*     */           }
/* 177 */           else if ((osName.equalsIgnoreCase("Mac OS X")) || (osName.equalsIgnoreCase("osx")))
/*     */           {
/* 179 */             m_osName = "osx";
/*     */           }
/* 181 */           else if (osName.equalsIgnoreCase("linux"))
/*     */           {
/* 183 */             if (is64)
/*     */             {
/* 185 */               m_osName = "linux64";
/*     */             }
/*     */             else
/*     */             {
/* 189 */               m_osName = "linux";
/*     */             }
/*     */           }
/* 192 */           else if (osName.equalsIgnoreCase("linux64"))
/*     */           {
/* 194 */             m_osName = "linux64";
/*     */           }
/* 196 */           else if (osName.toLowerCase().startsWith("win"))
/*     */           {
/* 198 */             m_osFamily = "windows";
/* 199 */             if (is64)
/*     */             {
/* 201 */               m_osName = "windows-amd64";
/*     */             }
/*     */             else
/*     */             {
/* 205 */               m_osName = "win32";
/*     */             }
/*     */           }
/*     */         }
/*     */       }
/*     */ 
/* 211 */       if (m_osFamily == null)
/*     */       {
/* 213 */         if (m_osName.startsWith("win"))
/*     */         {
/* 215 */           m_osFamily = "windows";
/*     */         }
/*     */         else
/*     */         {
/* 219 */           m_osFamily = "unix";
/*     */         }
/*     */       }
/*     */ 
/* 223 */       m_isOsFlagsInitialized = true;
/*     */     }
/*     */   }
/*     */ 
/*     */   public static String getOSName()
/*     */   {
/* 229 */     initializeOsFlags();
/* 230 */     return m_osName;
/*     */   }
/*     */ 
/*     */   public static String getOSFamily()
/*     */   {
/* 235 */     initializeOsFlags();
/* 236 */     return m_osFamily;
/*     */   }
/*     */ 
/*     */   public static boolean isFamily(String family)
/*     */   {
/* 241 */     initializeOsFlags();
/* 242 */     return m_osFamily.equals(family);
/*     */   }
/*     */ 
/*     */   public static boolean isOS(String os)
/*     */   {
/* 247 */     initializeOsFlags();
/* 248 */     return m_osName.equals(os);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 253 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 95814 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.OsUtils
 * JD-Core Version:    0.5.4
 */