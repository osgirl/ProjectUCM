/*     */ package intradoc.common;
/*     */ 
/*     */ import intradoc.util.MapUtils;
/*     */ import java.util.HashMap;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ 
/*     */ public class LogDirInfo
/*     */ {
/*     */   public String m_appName;
/*     */   public String m_dir;
/*     */   public String m_prefix;
/*     */   public String m_header;
/*     */   public String m_indexPage;
/*     */   public int m_curSuffix;
/*     */   public boolean m_linkPage;
/*  63 */   public Map[] m_logWriters = null;
/*     */ 
/*     */   public LogDirInfo(String appName)
/*     */   {
/*  68 */     this.m_appName = appName;
/*  69 */     this.m_dir = "./";
/*  70 */     this.m_indexPage = "";
/*  71 */     this.m_prefix = "";
/*  72 */     this.m_header = "";
/*  73 */     this.m_curSuffix = 1;
/*  74 */     this.m_linkPage = false;
/*     */ 
/*  76 */     Properties props = null;
/*     */     try
/*     */     {
/*  79 */       props = System.getProperties();
/*     */     }
/*     */     catch (SecurityException e)
/*     */     {
/*  83 */       props = new Properties();
/*     */     }
/*  85 */     if (props.get("idc.log.writer.default.implementor") == null)
/*     */     {
/*  87 */       props.put("idc.log.writer.default.implementor", "intradoc.common.IdcLogWriter");
/*     */     }
/*     */ 
/*  92 */     String prefix = "idc.log.writer";
/*  93 */     Map[] writerInfo = MapUtils.computeAttributeSets(props, prefix, "order", "", '.');
/*     */ 
/*  95 */     for (int i = 0; i < writerInfo.length; ++i)
/*     */     {
/*  97 */       Map info = writerInfo[i];
/*  98 */       String lwClassName = (String)info.get("implementor");
/*  99 */       if (lwClassName == null) continue; if (!StringUtils.convertToBool(lwClassName, true))
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 104 */       LogWriter lw = null;
/*     */       try
/*     */       {
/* 107 */         Class lwClass = Class.forName(lwClassName);
/* 108 */         lw = (LogWriter)lwClass.newInstance();
/*     */       }
/*     */       catch (Throwable t)
/*     */       {
/* 112 */         Report.trace(null, "unable to instantiate log writer class " + lwClassName, t);
/*     */       }
/*     */ 
/* 115 */       addLogWriter(lw);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void addLogWriter(LogWriter writer)
/*     */   {
/* 125 */     Map m = new HashMap();
/* 126 */     m.put("writer", writer);
/* 127 */     if (this.m_logWriters == null)
/*     */     {
/* 129 */       this.m_logWriters = new Map[] { m };
/*     */     }
/*     */     else
/*     */     {
/* 133 */       Map[] temp = new Map[this.m_logWriters.length + 1];
/* 134 */       System.arraycopy(this.m_logWriters, 0, temp, 0, this.m_logWriters.length);
/* 135 */       temp[this.m_logWriters.length] = m;
/* 136 */       this.m_logWriters = temp;
/*     */     }
/*     */   }
/*     */ 
/*     */   public void buildExtraLogWriter()
/*     */   {
/*     */     try
/*     */     {
/* 149 */       Properties env = (Properties)AppObjectRepository.getObject("environment");
/* 150 */       if (env != null)
/*     */       {
/* 152 */         String classPath = env.getProperty("ExtraLogWriterPath");
/* 153 */         if ((classPath != null) && (classPath.trim().length() > 0))
/*     */         {
/* 155 */           LogWriter lw = (LogWriter)Class.forName(classPath).newInstance();
/* 156 */           addLogWriter(lw);
/*     */         }
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 162 */       Report.trace(null, "Invalid value for 'ExtraLogWriterPath'.", e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 168 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97333 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.LogDirInfo
 * JD-Core Version:    0.5.4
 */