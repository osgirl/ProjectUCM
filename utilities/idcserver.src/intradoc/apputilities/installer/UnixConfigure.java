/*     */ package intradoc.apputilities.installer;
/*     */ 
/*     */ import intradoc.common.EnvUtils;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NativeOsUtils;
/*     */ import intradoc.common.PosixStructStat;
/*     */ import intradoc.common.ResourceContainer;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.IdcProperties;
/*     */ import intradoc.resource.ResourceLoader;
/*     */ import intradoc.server.PageMerger;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.io.BufferedReader;
/*     */ import java.io.BufferedWriter;
/*     */ import java.io.FileOutputStream;
/*     */ import java.io.IOException;
/*     */ import java.io.OutputStream;
/*     */ import java.io.Writer;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class UnixConfigure
/*     */   implements SectionInstaller
/*     */ {
/*     */   protected SysInstaller m_installer;
/*     */   protected NativeOsUtils m_utils;
/*     */   protected boolean m_isRefinery;
/*     */ 
/*     */   public int installSection(String name, String disposition, String arg, SysInstaller installer, Properties config)
/*     */     throws ServiceException
/*     */   {
/*  56 */     this.m_installer = installer;
/*  57 */     this.m_utils = installer.m_utils;
/*     */ 
/*  59 */     if (disposition.equals("always"))
/*     */     {
/*  61 */       String dir = installer.m_idcDir;
/*  62 */       if (dir.endsWith("/"))
/*     */       {
/*  64 */         dir = dir.substring(0, dir.length() - 1);
/*     */       }
/*  66 */       String idcLabel = installer.getConfigValue("InstanceMenuLabel");
/*  67 */       if (idcLabel == null)
/*     */       {
/*  69 */         idcLabel = "";
/*     */       }
/*     */       else
/*     */       {
/*  73 */         idcLabel = " " + idcLabel.replace('"', '\'');
/*     */       }
/*  75 */       String productNameLong = this.m_installer.getInstallValue("ProductNameLong", "Content Server");
/*     */ 
/*  77 */       String productEtcName = null;
/*  78 */       this.m_isRefinery = this.m_installer.getInstallBool("IsRefinery", false);
/*  79 */       if (this.m_isRefinery)
/*     */       {
/*  81 */         productEtcName = this.m_installer.getInstallValue("ProductEtcName", "idcrefinery");
/*     */       }
/*     */       else
/*     */       {
/*  86 */         productEtcName = this.m_installer.getInstallValue("ProductEtcName", "idcserver");
/*     */       }
/*     */ 
/*  89 */       Properties props = new IdcProperties();
/*  90 */       props.put("IntradocDir", dir);
/*  91 */       props.put("IntradocProduct", productNameLong + idcLabel);
/*  92 */       props.put("ScriptPrefix", productEtcName);
/*  93 */       String scriptPath = this.m_installer.computeDestination("etc/" + productEtcName + "_ctrl");
/*     */ 
/*  95 */       configureScript(scriptPath, props);
/*  96 */       boolean configureAdminServerDefault = false;
/*  97 */       if (this.m_installer.getInstallValue("ConfigureAdminServer", null) == null)
/*     */       {
/*  99 */         configureAdminServerDefault = true;
/*     */       }
/*     */ 
/* 102 */       if ((this.m_installer.m_isUpdate) && (this.m_installer.getInstallValue("AdminServerDir", null) == null))
/*     */       {
/* 112 */         configureAdminServerDefault = true;
/*     */       }
/* 114 */       boolean configureAdminServer = installer.getInstallBool("ConfigureAdminServer", configureAdminServerDefault);
/*     */ 
/* 116 */       if (configureAdminServer)
/*     */       {
/* 118 */         props = new IdcProperties();
/* 119 */         props.put("IntradocDir", dir + "/admin");
/* 120 */         props.put("IntradocProduct", "Admin Server");
/* 121 */         props.put("ScriptPrefix", "idcadmin");
/*     */ 
/* 123 */         configureScript(this.m_installer.computeDestination("admin/etc/idcadmin_ctrl"), props);
/*     */       }
/*     */     }
/* 126 */     return 0; } 
/*     */   public void configureScript(String file, Properties localData) throws ServiceException { // Byte code:
/*     */     //   0: new 47	intradoc/data/DataBinder
/*     */     //   3: dup
/*     */     //   4: invokespecial 48	intradoc/data/DataBinder:<init>	()V
/*     */     //   7: astore_3
/*     */     //   8: aload_3
/*     */     //   9: aload_2
/*     */     //   10: invokevirtual 49	intradoc/data/DataBinder:setLocalData	(Ljava/util/Properties;)V
/*     */     //   13: aconst_null
/*     */     //   14: astore 4
/*     */     //   16: new 50	intradoc/server/PageMerger
/*     */     //   19: dup
/*     */     //   20: aload_3
/*     */     //   21: aload_0
/*     */     //   22: getfield 2	intradoc/apputilities/installer/UnixConfigure:m_installer	Lintradoc/apputilities/installer/SysInstaller;
/*     */     //   25: getfield 51	intradoc/apputilities/installer/SysInstaller:m_context	Lintradoc/common/ExecutionContext;
/*     */     //   28: invokespecial 52	intradoc/server/PageMerger:<init>	(Lintradoc/data/DataBinder;Lintradoc/common/ExecutionContext;)V
/*     */     //   31: astore 5
/*     */     //   33: invokestatic 53	intradoc/shared/SharedObjects:getResources	()Lintradoc/common/ResourceContainer;
/*     */     //   36: astore 6
/*     */     //   38: aload 6
/*     */     //   40: getfield 54	intradoc/common/ResourceContainer:m_isFullyLoadedSharedResources	Z
/*     */     //   43: ifne +63 -> 106
/*     */     //   46: aload_0
/*     */     //   47: getfield 2	intradoc/apputilities/installer/UnixConfigure:m_installer	Lintradoc/apputilities/installer/SysInstaller;
/*     */     //   50: ldc 55
/*     */     //   52: iconst_0
/*     */     //   53: invokevirtual 56	intradoc/apputilities/installer/SysInstaller:computeDestinationEx	(Ljava/lang/String;Z)Ljava/lang/String;
/*     */     //   56: astore 7
/*     */     //   58: aload_0
/*     */     //   59: getfield 2	intradoc/apputilities/installer/UnixConfigure:m_installer	Lintradoc/apputilities/installer/SysInstaller;
/*     */     //   62: ldc 57
/*     */     //   64: invokevirtual 13	intradoc/apputilities/installer/SysInstaller:getConfigValue	(Ljava/lang/String;)Ljava/lang/String;
/*     */     //   67: astore 8
/*     */     //   69: aload 8
/*     */     //   71: ifnull +28 -> 99
/*     */     //   74: new 15	java/lang/StringBuilder
/*     */     //   77: dup
/*     */     //   78: invokespecial 16	java/lang/StringBuilder:<init>	()V
/*     */     //   81: aload 8
/*     */     //   83: invokevirtual 18	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
/*     */     //   86: ldc 58
/*     */     //   88: invokevirtual 18	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
/*     */     //   91: invokevirtual 20	java/lang/StringBuilder:toString	()Ljava/lang/String;
/*     */     //   94: invokestatic 59	intradoc/common/FileUtils:fileSlashes	(Ljava/lang/String;)Ljava/lang/String;
/*     */     //   97: astore 7
/*     */     //   99: aload 6
/*     */     //   101: aload 7
/*     */     //   103: invokestatic 60	intradoc/resource/ResourceLoader:loadResourceFile	(Lintradoc/common/ResourceContainer;Ljava/lang/String;)V
/*     */     //   106: aconst_null
/*     */     //   107: astore 7
/*     */     //   109: aload_1
/*     */     //   110: bipush 16
/*     */     //   112: invokestatic 61	intradoc/common/FileUtils:openOutputStream	(Ljava/lang/String;I)Ljava/io/OutputStream;
/*     */     //   115: astore 4
/*     */     //   117: aload 4
/*     */     //   119: aconst_null
/*     */     //   120: iconst_1
/*     */     //   121: invokestatic 62	intradoc/common/FileUtils:openDataWriter	(Ljava/io/OutputStream;Ljava/lang/String;I)Ljava/io/BufferedWriter;
/*     */     //   124: astore 7
/*     */     //   126: aload_0
/*     */     //   127: getfield 2	intradoc/apputilities/installer/UnixConfigure:m_installer	Lintradoc/apputilities/installer/SysInstaller;
/*     */     //   130: aload 5
/*     */     //   132: ldc 63
/*     */     //   134: invokevirtual 64	intradoc/apputilities/installer/SysInstaller:evaluateScript	(Lintradoc/server/PageMerger;Ljava/lang/String;)Ljava/lang/String;
/*     */     //   137: astore 8
/*     */     //   139: aload 7
/*     */     //   141: aload 8
/*     */     //   143: invokevirtual 65	java/io/Writer:write	(Ljava/lang/String;)V
/*     */     //   146: aconst_null
/*     */     //   147: astore 4
/*     */     //   149: aload_0
/*     */     //   150: getfield 2	intradoc/apputilities/installer/UnixConfigure:m_installer	Lintradoc/apputilities/installer/SysInstaller;
/*     */     //   153: aload_1
/*     */     //   154: invokevirtual 66	intradoc/apputilities/installer/SysInstaller:setExecutablePermission	(Ljava/lang/String;)Z
/*     */     //   157: pop
/*     */     //   158: aload 4
/*     */     //   160: invokestatic 67	intradoc/common/FileUtils:abort	(Ljava/io/OutputStream;)V
/*     */     //   163: aload 4
/*     */     //   165: aload 7
/*     */     //   167: invokestatic 68	intradoc/common/FileUtils:closeObjects	(Ljava/lang/Object;Ljava/lang/Object;)V
/*     */     //   170: goto +42 -> 212
/*     */     //   173: astore 8
/*     */     //   175: new 70	intradoc/common/ServiceException
/*     */     //   178: dup
/*     */     //   179: aload 8
/*     */     //   181: ldc 71
/*     */     //   183: iconst_1
/*     */     //   184: anewarray 72	java/lang/Object
/*     */     //   187: dup
/*     */     //   188: iconst_0
/*     */     //   189: aload_1
/*     */     //   190: aastore
/*     */     //   191: invokespecial 73	intradoc/common/ServiceException:<init>	(Ljava/lang/Throwable;Ljava/lang/String;[Ljava/lang/Object;)V
/*     */     //   194: athrow
/*     */     //   195: astore 9
/*     */     //   197: aload 4
/*     */     //   199: invokestatic 67	intradoc/common/FileUtils:abort	(Ljava/io/OutputStream;)V
/*     */     //   202: aload 4
/*     */     //   204: aload 7
/*     */     //   206: invokestatic 68	intradoc/common/FileUtils:closeObjects	(Ljava/lang/Object;Ljava/lang/Object;)V
/*     */     //   209: aload 9
/*     */     //   211: athrow
/*     */     //   212: return
/*     */     //
/*     */     // Exception table:
/*     */     //   from	to	target	type
/*     */     //   109	158	173	java/io/IOException
/*     */     //   109	158	195	finally
/*     */     //   173	197	195	finally } 
/* 175 */   @Deprecated
/*     */   public void configureScript(String file, String[] changeList) throws ServiceException { String[] leftSides = new String[changeList.length];
/* 176 */     for (int i = 0; i < changeList.length; ++i)
/*     */     {
/* 178 */       int index = changeList[i].indexOf("=");
/* 179 */       leftSides[i] = changeList[i].substring(0, index + 1);
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 184 */       Vector lines = new IdcVector();
/* 185 */       BufferedReader r = FileUtils.openDataReader(file);
/*     */ 
/* 187 */       while ((line = r.readLine()) != null)
/*     */       {
/*     */         String line;
/* 189 */         for (int i = 0; i < leftSides.length; ++i)
/*     */         {
/* 191 */           if (!line.startsWith(leftSides[i]))
/*     */             continue;
/* 193 */           line = changeList[i];
/* 194 */           break;
/*     */         }
/*     */ 
/* 197 */         lines.addElement(line);
/*     */       }
/* 199 */       r.close();
/*     */ 
/* 201 */       OutputStream outStream = new FileOutputStream(file);
/* 202 */       BufferedWriter w = FileUtils.openDataWriterEx(outStream, null, 1);
/*     */ 
/* 204 */       int size = lines.size();
/*     */       try
/*     */       {
/* 207 */         for (int i = 0; i < size; ++i)
/*     */         {
/* 209 */           w.write((String)lines.elementAt(i));
/* 210 */           w.newLine();
/*     */         }
/*     */       }
/*     */       finally
/*     */       {
/* 215 */         FileUtils.closeObject(w);
/*     */       }
/* 217 */       boolean setMode = !EnvUtils.isFamily("unix");
/* 218 */       if ((EnvUtils.isFamily("unix")) && (this.m_installer.m_utils != null))
/*     */       {
/* 221 */         PosixStructStat sb = new PosixStructStat();
/* 222 */         if (this.m_utils.stat(file, sb) == 0)
/*     */         {
/* 225 */           int mode = sb.st_mode & (NativeOsUtils.S_IRWXU | NativeOsUtils.S_IRWXG | NativeOsUtils.S_IRWXO);
/*     */ 
/* 227 */           mode |= NativeOsUtils.S_IXUSR | NativeOsUtils.S_IXGRP | NativeOsUtils.S_IXOTH;
/* 228 */           setMode = this.m_utils.chmod(file, mode) == 0;
/*     */         }
/*     */       }
/* 231 */       if (!setMode)
/*     */       {
/* 233 */         String msg = LocaleUtils.encodeMessage("csInstallerPermError", null, file);
/*     */ 
/* 235 */         this.m_installer.m_installLog.error(msg);
/*     */       }
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 240 */       this.m_installer.m_installLog.error(LocaleUtils.encodeMessage("csInstallerUnableToSetHomeVar", null, file, e.getMessage()));
/*     */     } }
/*     */ 
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 247 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 95352 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.installer.UnixConfigure
 * JD-Core Version:    0.5.4
 */