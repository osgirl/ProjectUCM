/*     */ package intradoc.common;
/*     */ 
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Iterator;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class ConfigFileParameters
/*     */ {
/*  31 */   protected static final String[][] FeaturePair = new String[0][];
/*     */   public static final int F_NO_DBCONNECTION = 1;
/*     */   public static final int F_NEW_INSTANCE = 2;
/*     */   public static final int F_UPGRADE_INSTANCE = 4;
/*     */   protected static Vector m_sharedDirectory;
/*     */   protected static Hashtable m_featureMap;
/*  39 */   protected static String m_intradocDir = "";
/*     */   protected static String m_shareCfgDestination;
/*     */   protected static String m_loadCfgFrom;
/*     */ 
/*     */   public static void init(String intradocDir, String archiveDir)
/*     */   {
/*  48 */     m_intradocDir = intradocDir;
/*     */ 
/*  51 */     m_featureMap = new Hashtable();
/*  52 */     for (int i = 0; i < FeaturePair.length; ++i)
/*     */     {
/*  54 */       String key = FeaturePair[i][0];
/*  55 */       String value = FeaturePair[i][1];
/*  56 */       m_featureMap.put(key, value);
/*     */     }
/*     */ 
/*  60 */     String dataDir = m_intradocDir + "data/";
/*  61 */     String searchDir = m_intradocDir + "search/";
/*  62 */     if ((archiveDir == null) || (archiveDir.length() == 0))
/*     */     {
/*  64 */       archiveDir = m_intradocDir + "archives/";
/*     */     }
/*  66 */     m_sharedDirectory = new IdcVector();
/*  67 */     m_sharedDirectory.addElement(dataDir);
/*  68 */     m_sharedDirectory.addElement(searchDir);
/*  69 */     m_sharedDirectory.addElement(archiveDir);
/*  70 */     m_sharedDirectory.addElement(m_intradocDir + "cmu/");
/*     */   }
/*     */ 
/*     */   public static String getLoadFromLocation()
/*     */   {
/*  75 */     return m_loadCfgFrom;
/*     */   }
/*     */ 
/*     */   public static void setLoadFromLocation(String loc)
/*     */   {
/*  80 */     m_loadCfgFrom = loc;
/*     */   }
/*     */ 
/*     */   public static String getMigrateDestination()
/*     */   {
/*  85 */     return m_shareCfgDestination;
/*     */   }
/*     */ 
/*     */   public static void setMigrateDestination(String dest)
/*     */   {
/*  90 */     m_shareCfgDestination = dest;
/*     */   }
/*     */ 
/*     */   public static Vector getSharedDirectories()
/*     */   {
/*  95 */     return m_sharedDirectory;
/*     */   }
/*     */ 
/*     */   public static String getRoot(String dir)
/*     */   {
/* 103 */     int index = dir.indexOf(47);
/* 104 */     if (index > 0)
/*     */     {
/* 106 */       return dir.substring(0, index);
/*     */     }
/* 108 */     return dir;
/*     */   }
/*     */ 
/*     */   public static String getSubRoot(String dir)
/*     */   {
/* 116 */     int index = dir.indexOf(47);
/* 117 */     if (index > 0)
/*     */     {
/* 119 */       String subDir = dir.substring(index + 1, dir.length());
/* 120 */       index = subDir.indexOf(47);
/* 121 */       if (index > 0)
/*     */       {
/* 123 */         return subDir.substring(0, index);
/*     */       }
/*     */     }
/* 126 */     return null;
/*     */   }
/*     */ 
/*     */   public static String getFeature(String dir)
/*     */   {
/* 135 */     String subRoot = getSubRoot(dir);
/* 136 */     String value = null;
/* 137 */     if ((subRoot != null) && (subRoot.length() > 0))
/*     */     {
/* 140 */       value = (String)m_featureMap.get(subRoot);
/* 141 */       if ((value == null) || (value.length() == 0))
/*     */       {
/* 143 */         value = subRoot;
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/*     */     {
/* 149 */       String root = getRoot(dir);
/* 150 */       if ((root != null) && (root.length() > 0))
/*     */       {
/* 152 */         value = (String)m_featureMap.get(root);
/* 153 */         if ((value == null) || (value.length() == 0))
/*     */         {
/* 155 */           value = root;
/*     */         }
/*     */       }
/*     */     }
/* 159 */     return value;
/*     */   }
/*     */ 
/*     */   public static boolean isSharedDirectory(String dir)
/*     */   {
/* 167 */     if (m_sharedDirectory == null)
/*     */     {
/* 169 */       return false;
/*     */     }
/*     */ 
/* 172 */     boolean isShared = false;
/* 173 */     if ((dir != null) && (dir.length() > 0))
/*     */     {
/* 175 */       Iterator i = m_sharedDirectory.iterator();
/*     */ 
/* 177 */       while (i.hasNext())
/*     */       {
/* 179 */         String root = (String)i.next();
/* 180 */         if (dir.startsWith(root))
/*     */         {
/* 182 */           isShared = true;
/* 183 */           break;
/*     */         }
/*     */       }
/*     */     }
/* 187 */     return isShared;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 192 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97732 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.ConfigFileParameters
 * JD-Core Version:    0.5.4
 */