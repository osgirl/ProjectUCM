/*     */ package intradoc.admin;
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
/*     */ public class AdminLog
/*     */ {
/*     */   public IdcMessage getLogInfo(Properties cfgProps, SystemPropertiesEditor editor)
/*     */   {
/*  36 */     Properties oldCfg = editor.getConfig();
/*  37 */     Properties newCfgExtra = getExtraProps(cfgProps);
/*  38 */     Properties oldCfgExtra = null;
/*  39 */     if (newCfgExtra != null)
/*     */     {
/*  41 */       removeExtraValue(newCfgExtra, "#", true);
/*  42 */       oldCfgExtra = getExtraProps(oldCfg);
/*  43 */       removeExtraValue(oldCfgExtra, "#", true);
/*     */     }
/*  45 */     Properties newCfg = (Properties)cfgProps.clone();
/*     */ 
/*  48 */     newCfg.remove("dUser");
/*  49 */     newCfg.remove("cfgExtraVariables");
/*  50 */     removeExtraValue(newCfg, "Box", false);
/*  51 */     oldCfg.remove("cfgExtraVariables");
/*     */ 
/*  53 */     Hashtable diffCfg = getDiff(oldCfg, newCfg);
/*  54 */     if (newCfgExtra != null)
/*     */     {
/*  56 */       getDiff(oldCfgExtra, newCfgExtra, diffCfg, true);
/*     */     }
/*     */ 
/*  59 */     IdcMessage info = getInfo(diffCfg);
/*  60 */     if (SystemUtils.m_verbose)
/*     */     {
/*  62 */       Report.debug("idcadmin", "Results are " + info, null);
/*     */     }
/*  64 */     return info;
/*     */   }
/*     */ 
/*     */   protected void removeExtraValue(Properties cfg, String pattern, boolean isStart)
/*     */   {
/*  69 */     Properties tmpProps = (Properties)cfg.clone();
/*  70 */     for (Enumeration en = tmpProps.keys(); en.hasMoreElements(); )
/*     */     {
/*  72 */       String key = (String)en.nextElement();
/*  73 */       if (((isStart) && (key.startsWith(pattern))) || ((!isStart) && (key.endsWith(pattern))))
/*     */       {
/*  76 */         cfg.remove(key);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected IdcMessage getInfo(Hashtable diffCfg)
/*     */   {
/*  84 */     IdcMessage idcMsg = null;
/*  85 */     String[][] keys = { { "ADDED", "csAdminValueAdded" }, { "MODIFIED", "csAdminValueModified" }, { "REMOVED", "csAdminValueRemoved" } };
/*     */ 
/*  93 */     for (int i = 0; i < keys.length; ++i)
/*     */     {
/*  95 */       Properties prop = (Properties)diffCfg.get(keys[i][0]);
/*  96 */       if (prop.size() < 1) {
/*     */         continue;
/*     */       }
/*     */ 
/* 100 */       IdcMessage rMsg = IdcMessageFactory.lc(keys[i][1], new Object[0]);
/*     */ 
/* 102 */       rMsg.m_prior = IdcMessageFactory.lc();
/* 103 */       IdcStringBuilder builder = new IdcStringBuilder("\n");
/* 104 */       for (Enumeration en = prop.keys(); en.hasMoreElements(); )
/*     */       {
/* 106 */         String key = (String)en.nextElement();
/* 107 */         String value = (String)prop.get(key);
/*     */ 
/* 109 */         builder.append(key);
/* 110 */         builder.append(" : ");
/* 111 */         builder.append(value);
/* 112 */         builder.append("\n");
/*     */       }
/* 114 */       rMsg.m_prior.m_msgLocalized = builder.toString();
/* 115 */       if (idcMsg == null)
/*     */       {
/* 117 */         idcMsg = rMsg;
/*     */       }
/*     */       else
/*     */       {
/* 121 */         IdcMessage prevMsg = idcMsg;
/* 122 */         IdcMessage pMsg = idcMsg.m_prior;
/* 123 */         while (pMsg != null)
/*     */         {
/* 125 */           prevMsg = pMsg;
/* 126 */           pMsg = pMsg.m_prior;
/*     */         }
/*     */ 
/* 129 */         prevMsg.m_prior = rMsg;
/*     */       }
/*     */     }
/* 132 */     return idcMsg;
/*     */   }
/*     */ 
/*     */   protected void mergeProps(Properties newCfg, Properties cfgProps)
/*     */   {
/* 137 */     for (Enumeration en = cfgProps.keys(); en.hasMoreElements(); )
/*     */     {
/* 139 */       String key = new String((String)en.nextElement());
/* 140 */       String value = new String((String)cfgProps.get(key));
/* 141 */       newCfg.put(key, value);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected Properties getExtraProps(Properties prop)
/*     */   {
/* 149 */     Properties extra = null;
/* 150 */     String value = (String)prop.get("cfgExtraVariables");
/* 151 */     if (value != null)
/*     */     {
/* 153 */       extra = new Properties();
/* 154 */       StringUtils.parseProperties(extra, value);
/*     */     }
/* 156 */     return extra;
/*     */   }
/*     */ 
/*     */   protected Hashtable getDiff(Properties oldCfg, Properties newCfg)
/*     */   {
/* 161 */     Hashtable hash = new Hashtable();
/* 162 */     hash.put("ADDED", new Properties());
/* 163 */     hash.put("MODIFIED", new Properties());
/* 164 */     hash.put("REMOVED", new Properties());
/* 165 */     getDiff(oldCfg, newCfg, hash, false);
/* 166 */     return hash;
/*     */   }
/*     */ 
/*     */   protected void getDiff(Properties oldCfg, Properties newCfg, Hashtable hash, boolean hasRemove)
/*     */   {
/* 171 */     Properties addedProps = (Properties)hash.get("ADDED");
/* 172 */     Properties modifiedProps = (Properties)hash.get("MODIFIED");
/* 173 */     Properties removedProps = (Properties)hash.get("REMOVED");
/* 174 */     if (hasRemove)
/*     */     {
/* 176 */       mergeProps(removedProps, oldCfg);
/*     */     }
/*     */ 
/* 182 */     for (Enumeration en = newCfg.keys(); en.hasMoreElements(); )
/*     */     {
/* 184 */       String key = (String)en.nextElement();
/* 185 */       String value = newCfg.getProperty(key).trim();
/*     */ 
/* 187 */       String valueOld = oldCfg.getProperty(key);
/* 188 */       if ((valueOld == null) || (valueOld.length() < 1))
/*     */       {
/* 190 */         if (value.length() > 0)
/*     */         {
/* 192 */           addedProps.put(key, value);
/*     */         }
/*     */       }
/* 195 */       else if (!valueOld.equals(value))
/*     */       {
/* 197 */         String newValue = "From " + valueOld + " To " + value;
/* 198 */         modifiedProps.put(key, newValue);
/*     */       }
/* 200 */       if (!hasRemove)
/*     */         continue;
/* 202 */       removedProps.remove(key);
/*     */     }
/*     */ 
/* 205 */     if (!SystemUtils.m_verbose)
/*     */       return;
/* 207 */     Report.debug("idcadmin", "sizes: " + addedProps.size() + " " + removedProps.size() + " " + modifiedProps.size(), null);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 215 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 83022 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.admin.AdminLog
 * JD-Core Version:    0.5.4
 */