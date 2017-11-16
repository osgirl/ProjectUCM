/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.TopicInfo;
/*     */ import intradoc.shared.UserData;
/*     */ import intradoc.shared.UserProfileData;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.io.BufferedReader;
/*     */ import java.io.File;
/*     */ import java.io.StringReader;
/*     */ import java.util.HashSet;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Set;
/*     */ 
/*     */ public class ProfileCache
/*     */ {
/*  37 */   protected static String m_profilesDir = null;
/*  38 */   protected static DataBinder m_profileData = new DataBinder();
/*     */ 
/*  40 */   protected static Hashtable m_topicDefaults = new Hashtable();
/*  41 */   protected static Hashtable m_topicKeyDefaults = new Hashtable();
/*  42 */   protected static Hashtable m_userProfiles = new Hashtable();
/*     */ 
/*  44 */   protected static Set m_topicDirs = new HashSet();
/*     */ 
/*     */   public static void init()
/*     */     throws ServiceException, DataException
/*     */   {
/*  51 */     m_profilesDir = LegacyDirectoryLocator.getUserProfilesDir();
/*  52 */     FileUtils.checkOrCreateDirectoryPrepareForLocks(m_profilesDir, 2, true);
/*     */ 
/*  54 */     String defDir = m_profilesDir + "defaults/";
/*  55 */     FileUtils.checkOrCreateDirectoryPrepareForLocks(defDir, 0, true);
/*     */ 
/*  57 */     loadProfilesData();
/*  58 */     loadAllTopicDefaults();
/*  59 */     loadTopicKeyDefaults();
/*     */   }
/*     */ 
/*     */   public static void loadProfilesData() throws DataException, ServiceException
/*     */   {
/*  64 */     DataBinder binder = new DataBinder();
/*  65 */     ResourceUtils.serializeDataBinder(m_profilesDir, "profiles.hda", binder, false, false);
/*     */ 
/*  67 */     m_profileData = binder;
/*     */   }
/*     */ 
/*     */   public static void loadAllTopicDefaults()
/*     */     throws DataException
/*     */   {
/*  75 */     DataResultSet defSet = SharedObjects.getTable("GlobalUserTopicDefaults");
/*  76 */     if ((defSet == null) || (defSet.isEmpty()))
/*     */     {
/*  78 */       return;
/*     */     }
/*     */ 
/*  81 */     FieldInfo[] infos = null;
/*     */     try
/*     */     {
/*  84 */       String[] columns = { "topicName", "topicValue" };
/*  85 */       infos = ResultSetUtils.createInfoList(defSet, columns, true);
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/*  89 */       throw new DataException(e, "csUnableToLoadResourceTopics", new Object[0]);
/*     */     }
/*     */ 
/*  92 */     int topicIndex = infos[0].m_index;
/*  93 */     int valIndex = infos[1].m_index;
/*  94 */     for (defSet.first(); defSet.isRowPresent(); defSet.next())
/*     */     {
/*  96 */       String key = defSet.getStringValue(topicIndex);
/*  97 */       String val = defSet.getStringValue(valIndex);
/*     */ 
/*  99 */       TopicInfo info = new TopicInfo(key);
/*     */       try
/*     */       {
/* 102 */         DataBinder binder = new DataBinder();
/* 103 */         BufferedReader br = new BufferedReader(new StringReader(val));
/* 104 */         binder.receive(br);
/*     */ 
/* 106 */         info.m_data = binder;
/* 107 */         m_topicDefaults.put(key, info);
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 111 */         throw new DataException(e, "csUnableToFindDefaultTopic", new Object[0]);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void loadTopicKeyDefaults()
/*     */     throws DataException
/*     */   {
/* 121 */     DataResultSet defSet = SharedObjects.getTable("GlobalUserTopicKeyDefaults");
/* 122 */     if ((defSet == null) || (defSet.isEmpty()))
/*     */     {
/* 124 */       return;
/*     */     }
/*     */ 
/* 128 */     String[][] table = (String[][])null;
/*     */     try
/*     */     {
/* 131 */       String[] columns = { "topicName", "topicKey", "topicValue" };
/* 132 */       table = ResultSetUtils.createStringTable(defSet, columns);
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 136 */       throw new DataException(e, "csUnableToLoadRestoreTopic", new Object[0]);
/*     */     }
/* 138 */     int num = table.length;
/* 139 */     for (int i = 0; i < num; ++i)
/*     */     {
/* 141 */       String name = table[i][0].toLowerCase();
/* 142 */       Properties keys = (Properties)m_topicKeyDefaults.get(name);
/* 143 */       if (keys == null)
/*     */       {
/* 145 */         keys = new Properties();
/* 146 */         m_topicKeyDefaults.put(name, keys);
/*     */       }
/*     */ 
/* 149 */       keys.put(table[i][1], table[i][2]);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static TopicInfo retrieveTopicDefaults(TopicInfo info, UserData userData)
/*     */     throws ServiceException
/*     */   {
/* 161 */     String userType = userData.getProperty("dUserType");
/* 162 */     String topic = info.m_name;
/*     */ 
/* 164 */     boolean isFirstLoad = false;
/* 165 */     TopicInfo cachedInfo = null;
/* 166 */     if ((userType == null) || (userType.length() == 0))
/*     */     {
/* 168 */       cachedInfo = (TopicInfo)m_topicDefaults.get(topic);
/* 169 */       if (cachedInfo == null)
/*     */       {
/* 171 */         cachedInfo = new TopicInfo(topic);
/* 172 */         m_topicDefaults.put(topic, cachedInfo);
/* 173 */         isFirstLoad = true;
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 178 */       String key = userType + "/" + topic;
/* 179 */       key = key.toLowerCase();
/* 180 */       cachedInfo = (TopicInfo)m_topicDefaults.get(key);
/*     */ 
/* 182 */       if (cachedInfo == null)
/*     */       {
/* 185 */         cachedInfo = createTopicCacheInfo(topic, userData);
/* 186 */         isFirstLoad = true;
/*     */       }
/*     */ 
/*     */       try
/*     */       {
/* 191 */         File f = new File(cachedInfo.getFilePath());
/* 192 */         long ts = f.lastModified();
/* 193 */         if (ts != cachedInfo.m_lastLoaded)
/*     */         {
/* 195 */           updateDefaultTopic(cachedInfo);
/* 196 */           cachedInfo.m_lastLoaded = ts;
/*     */         }
/*     */       }
/*     */       catch (Exception ignore)
/*     */       {
/* 201 */         IdcMessage msg = IdcMessageFactory.lc(ignore, "csUnableToLoadTopicDefault", new Object[] { key });
/* 202 */         Report.trace(null, LocaleResources.localizeMessage(null, msg, new ExecutionContextAdaptor()).toString(), ignore);
/*     */ 
/* 204 */         cachedInfo.m_lastLoaded = -2L;
/*     */       }
/*     */     }
/*     */ 
/* 208 */     if (isFirstLoad)
/*     */     {
/* 210 */       Properties props = (Properties)m_topicKeyDefaults.get(topic);
/* 211 */       if (props != null)
/*     */       {
/* 214 */         DataBinder data = cachedInfo.m_data;
/* 215 */         if (data == null)
/*     */         {
/* 217 */           data = new DataBinder();
/* 218 */           cachedInfo.init(data, false);
/*     */         }
/* 220 */         Properties defProps = data.getLocalData();
/* 221 */         DataBinder.mergeHashTables(props, defProps);
/* 222 */         data.setLocalData(props);
/*     */       }
/*     */     }
/* 225 */     return cachedInfo;
/*     */   }
/*     */ 
/*     */   public static TopicInfo createTopicCacheInfo(String topic, UserData userData)
/*     */   {
/* 235 */     String userType = userData.getProperty("dUserType");
/* 236 */     TopicInfo info = new TopicInfo(topic);
/*     */ 
/* 238 */     info.m_directory = (m_profilesDir + "defaults/");
/* 239 */     if ((userType != null) && (userType.length() > 0))
/*     */     {
/* 241 */       userType = userType.toLowerCase();
/* 242 */       info.m_lookupKey = (userType + "/" + info.m_name);
/*     */ 
/* 244 */       userType = StringUtils.encodeUrlStyle(userType, '#', false);
/*     */       TopicInfo tmp103_102 = info; tmp103_102.m_directory = (tmp103_102.m_directory + userType + "/");
/* 246 */       ensureTopicInfoDirectoryExists(info);
/*     */     }
/*     */ 
/* 250 */     m_topicDefaults.put(info.m_lookupKey, info);
/*     */ 
/* 252 */     return info;
/*     */   }
/*     */ 
/*     */   protected static void ensureTopicInfoDirectoryExists(TopicInfo info)
/*     */   {
/* 257 */     synchronized (m_topicDirs)
/*     */     {
/* 259 */       if (!m_topicDirs.contains(info.m_directory))
/*     */       {
/*     */         try
/*     */         {
/* 263 */           FileUtils.checkOrCreateDirectoryPrepareForLocks(info.m_directory, 0, true);
/*     */         }
/*     */         catch (ServiceException e)
/*     */         {
/* 267 */           Report.trace("system", null, e);
/*     */         }
/* 269 */         m_topicDirs.add(info.m_directory);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void updateDefaultTopic(TopicInfo info) throws ServiceException
/*     */   {
/* 276 */     String dir = info.m_directory;
/* 277 */     String filename = info.m_filename;
/*     */ 
/* 279 */     FileUtils.reserveDirectory(dir);
/*     */     try
/*     */     {
/* 283 */       DataBinder binder = new DataBinder(true);
/* 284 */       if (!ResourceUtils.serializeDataBinder(dir, filename, binder, false, false))
/*     */       {
/* 286 */         TopicInfo defInfo = (TopicInfo)m_topicDefaults.get(info.m_name);
/* 287 */         if (defInfo != null)
/*     */         {
/* 289 */           binder = defInfo.m_data;
/*     */         }
/*     */       }
/* 292 */       info.init(binder, false);
/*     */     }
/*     */     finally
/*     */     {
/* 296 */       FileUtils.releaseDirectory(dir);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static UserProfileData getUserProfileData(UserData userData)
/*     */   {
/* 305 */     return (UserProfileData)m_userProfiles.get(userData.m_name);
/*     */   }
/*     */ 
/*     */   public static DataBinder getProfilesData()
/*     */   {
/* 310 */     return m_profileData;
/*     */   }
/*     */ 
/*     */   public static String getTopicKeyDefault(String topic, String key)
/*     */   {
/* 315 */     Properties keys = (Properties)m_topicKeyDefaults.get(topic);
/* 316 */     return keys.getProperty(key);
/*     */   }
/*     */ 
/*     */   public static Properties getTopicKeyDefaults(String topic)
/*     */   {
/* 321 */     return (Properties)m_topicKeyDefaults.get(topic);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 326 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 71159 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.ProfileCache
 * JD-Core Version:    0.5.4
 */