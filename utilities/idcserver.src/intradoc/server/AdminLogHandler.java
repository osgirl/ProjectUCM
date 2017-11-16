/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.server.utils.SystemPropertiesEditor;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ 
/*     */ public class AdminLogHandler
/*     */ {
/*     */   public IdcMessage getLogInfo(Properties cfgProps, SystemPropertiesEditor editor)
/*     */   {
/*  32 */     Properties oldCfg = editor.getConfig();
/*  33 */     Properties newCfgExtra = getExtraProps(cfgProps);
/*  34 */     Properties oldCfgExtra = null;
/*  35 */     if (newCfgExtra != null)
/*     */     {
/*  37 */       removeExtraValue(newCfgExtra, "#", true);
/*  38 */       oldCfgExtra = getExtraProps(oldCfg);
/*  39 */       removeExtraValue(oldCfgExtra, "#", true);
/*     */     }
/*  41 */     Properties newCfg = (Properties)cfgProps.clone();
/*     */ 
/*  44 */     newCfg.remove("dUser");
/*  45 */     newCfg.remove("cfgExtraVariables");
/*  46 */     removeExtraValue(newCfg, "Box", false);
/*  47 */     oldCfg.remove("cfgExtraVariables");
/*     */ 
/*  49 */     Hashtable diffCfg = getDiff(oldCfg, newCfg);
/*  50 */     if (newCfgExtra != null)
/*     */     {
/*  52 */       getDiff(oldCfgExtra, newCfgExtra, diffCfg, true);
/*     */     }
/*     */ 
/*  55 */     IdcMessage info = getInfo(diffCfg);
/*  56 */     if (SystemUtils.m_verbose)
/*     */     {
/*  58 */       Report.debug("idcadmin", "Results are " + info, null);
/*     */     }
/*  60 */     return info;
/*     */   }
/*     */ 
/*     */   protected void removeExtraValue(Properties cfg, String pattern, boolean isStart)
/*     */   {
/*  65 */     Properties tmpProps = (Properties)cfg.clone();
/*  66 */     for (Enumeration en = tmpProps.keys(); en.hasMoreElements(); )
/*     */     {
/*  68 */       String key = (String)en.nextElement();
/*  69 */       if (((isStart) && (key.startsWith(pattern))) || ((!isStart) && (key.endsWith(pattern))))
/*     */       {
/*  72 */         cfg.remove(key);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected IdcMessage getInfo(Hashtable diffCfg)
/*     */   {
/*  80 */     IdcMessage idcMsg = null;
/*  81 */     String[][] keys = { { "ADDED", "csAdminValueAdded" }, { "MODIFIED", "csAdminValueModified" }, { "REMOVED", "csAdminValueRemoved" } };
/*     */ 
/*  89 */     for (int i = 0; i < keys.length; ++i)
/*     */     {
/*  91 */       Properties prop = (Properties)diffCfg.get(keys[i][0]);
/*  92 */       if (prop.size() < 1) {
/*     */         continue;
/*     */       }
/*     */ 
/*  96 */       IdcMessage rMsg = IdcMessageFactory.lc(keys[i][1], new Object[0]);
/*     */ 
/*  98 */       rMsg.m_prior = IdcMessageFactory.lc();
/*  99 */       IdcStringBuilder builder = new IdcStringBuilder("\n");
/* 100 */       for (Enumeration en = prop.keys(); en.hasMoreElements(); )
/*     */       {
/* 102 */         String key = (String)en.nextElement();
/* 103 */         String value = (String)prop.get(key);
/*     */ 
/* 105 */         builder.append(key);
/* 106 */         builder.append(" : ");
/* 107 */         builder.append(value);
/* 108 */         builder.append("\n");
/*     */       }
/* 110 */       rMsg.m_prior.m_msgLocalized = builder.toString();
/* 111 */       if (idcMsg == null)
/*     */       {
/* 113 */         idcMsg = rMsg;
/*     */       }
/*     */       else
/*     */       {
/* 117 */         IdcMessage prevMsg = idcMsg;
/* 118 */         IdcMessage pMsg = idcMsg.m_prior;
/* 119 */         while (pMsg != null)
/*     */         {
/* 121 */           prevMsg = pMsg;
/* 122 */           pMsg = pMsg.m_prior;
/*     */         }
/*     */ 
/* 125 */         prevMsg.m_prior = rMsg;
/*     */       }
/*     */     }
/* 128 */     return idcMsg;
/*     */   }
/*     */ 
/*     */   protected void mergeProps(Properties newCfg, Properties cfgProps)
/*     */   {
/* 133 */     for (Enumeration en = cfgProps.keys(); en.hasMoreElements(); )
/*     */     {
/* 135 */       String key = new String((String)en.nextElement());
/* 136 */       String value = new String((String)cfgProps.get(key));
/* 137 */       newCfg.put(key, value);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected Properties getExtraProps(Properties prop)
/*     */   {
/* 145 */     Properties extra = null;
/* 146 */     String value = (String)prop.get("cfgExtraVariables");
/* 147 */     if (value != null)
/*     */     {
/* 149 */       extra = new Properties();
/* 150 */       StringUtils.parseProperties(extra, value);
/*     */     }
/* 152 */     return extra;
/*     */   }
/*     */ 
/*     */   protected Hashtable getDiff(Properties oldCfg, Properties newCfg)
/*     */   {
/* 157 */     Hashtable hash = new Hashtable();
/* 158 */     hash.put("ADDED", new Properties());
/* 159 */     hash.put("MODIFIED", new Properties());
/* 160 */     hash.put("REMOVED", new Properties());
/* 161 */     getDiff(oldCfg, newCfg, hash, false);
/* 162 */     return hash;
/*     */   }
/*     */ 
/*     */   protected void getDiff(Properties oldCfg, Properties newCfg, Hashtable hash, boolean hasRemove)
/*     */   {
/* 167 */     Properties addedProps = (Properties)hash.get("ADDED");
/* 168 */     Properties modifiedProps = (Properties)hash.get("MODIFIED");
/* 169 */     Properties removedProps = (Properties)hash.get("REMOVED");
/* 170 */     if (hasRemove)
/*     */     {
/* 172 */       mergeProps(removedProps, oldCfg);
/*     */     }
/*     */ 
/* 178 */     for (Enumeration en = newCfg.keys(); en.hasMoreElements(); )
/*     */     {
/* 180 */       String key = (String)en.nextElement();
/* 181 */       String value = newCfg.getProperty(key).trim();
/*     */ 
/* 183 */       String valueOld = oldCfg.getProperty(key);
/* 184 */       if ((valueOld == null) || (valueOld.length() < 1))
/*     */       {
/* 186 */         if (value.length() > 0)
/*     */         {
/* 188 */           addedProps.put(key, value);
/*     */         }
/*     */       }
/* 191 */       else if (!valueOld.equals(value))
/*     */       {
/* 193 */         String newValue = "From " + valueOld + " To " + value;
/* 194 */         modifiedProps.put(key, newValue);
/*     */       }
/* 196 */       if (!hasRemove)
/*     */         continue;
/* 198 */       removedProps.remove(key);
/*     */     }
/*     */ 
/* 201 */     if (!SystemUtils.m_verbose)
/*     */       return;
/* 203 */     Report.debug("idcadmin", "sizes: " + addedProps.size() + " " + removedProps.size() + " " + modifiedProps.size(), null);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 211 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97473 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.AdminLogHandler
 * JD-Core Version:    0.5.4
 */