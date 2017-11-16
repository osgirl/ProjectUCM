/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcCharArrayWriter;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.TopicInfo;
/*     */ import intradoc.shared.UserData;
/*     */ import intradoc.shared.UserProfileData;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.io.File;
/*     */ import java.io.IOException;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class UserProfileManager
/*     */ {
/*  34 */   protected UserProfileData m_profileData = null;
/*  35 */   protected UserData m_userData = null;
/*     */ 
/*  37 */   protected Vector m_monitoredTopics = null;
/*  38 */   protected UserProfileEditor m_profileEditor = null;
/*     */ 
/*     */   public UserProfileManager(UserData userData, Workspace ws, ExecutionContext ctxt)
/*     */   {
/*  43 */     this.m_userData = userData;
/*  44 */     this.m_profileData = userData.getProfileData();
/*     */ 
/*  46 */     this.m_profileEditor = new UserProfileEditor(userData, ws, ctxt);
/*     */   }
/*     */ 
/*     */   public void init() throws ServiceException
/*     */   {
/*  51 */     checkAndCreateUserDirectory();
/*     */   }
/*     */ 
/*     */   public void initForUser(UserData userData) throws ServiceException
/*     */   {
/*  56 */     this.m_userData = userData;
/*  57 */     this.m_profileData = userData.getProfileData();
/*  58 */     this.m_profileEditor.setUser(userData);
/*     */ 
/*  60 */     init();
/*     */   }
/*     */ 
/*     */   public void updateTopics(DataBinder binder) throws DataException, IOException, ServiceException
/*     */   {
/*  65 */     synchronized (this.m_profileData.getTopics())
/*     */     {
/*     */       try
/*     */       {
/*  69 */         refreshNew(binder);
/*  70 */         String profileDir = this.m_profileEditor.getProfileDirectory();
/*  71 */         File gblFile = new File(profileDir + "topics.gbl");
/*  72 */         long gblMarker = gblFile.lastModified();
/*  73 */         long curGblMarker = this.m_profileData.getGlobalTimeStamp();
/*     */ 
/*  75 */         if (curGblMarker != gblMarker)
/*     */         {
/*  77 */           refreshChanged();
/*  78 */           this.m_profileData.setGlobalTimeStamp(gblMarker);
/*     */         }
/*     */ 
/*  81 */         DataResultSet editSet = (DataResultSet)binder.getResultSet("UserTopicEdits");
/*     */ 
/*  84 */         this.m_profileEditor.doEdits(editSet);
/*     */ 
/*  86 */         notifyTopics(binder);
/*     */       }
/*     */       finally
/*     */       {
/*  90 */         this.m_profileEditor.checkForReleaseDirectory();
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void refreshNew(DataBinder binder) throws ServiceException
/*     */   {
/*  97 */     String str = binder.getLocal("monitoredTopics");
/*  98 */     if ((str == null) || (str.length() == 0))
/*     */     {
/* 101 */       this.m_monitoredTopics = null;
/* 102 */       return;
/*     */     }
/*     */ 
/* 105 */     this.m_monitoredTopics = StringUtils.parseArray(str, ',', ',');
/*     */ 
/* 108 */     int num = this.m_monitoredTopics.size();
/* 109 */     for (int i = 0; i < num; i += 2)
/*     */     {
/* 111 */       String name = (String)this.m_monitoredTopics.elementAt(i);
/* 112 */       TopicInfo info = this.m_profileData.getTopic(name);
/* 113 */       if (info != null)
/*     */         continue;
/* 115 */       info = this.m_profileEditor.loadTopicInfo(name);
/* 116 */       this.m_profileData.addTopic(info);
/*     */     }
/*     */ 
/* 120 */     if (!this.m_profileEditor.isLocked())
/*     */       return;
/* 122 */     String profileDir = this.m_profileEditor.getProfileDirectory();
/* 123 */     FileUtils.touchFile(profileDir + "topics.gbl");
/*     */   }
/*     */ 
/*     */   protected void refreshChanged()
/*     */     throws ServiceException
/*     */   {
/* 130 */     Hashtable topics = this.m_profileData.getTopics();
/* 131 */     for (Enumeration en = topics.elements(); en.hasMoreElements(); )
/*     */     {
/* 133 */       TopicInfo info = (TopicInfo)en.nextElement();
/* 134 */       File file = new File(info.getFilePath());
/* 135 */       long fileMod = file.lastModified();
/*     */ 
/* 137 */       if (info.m_lastLoaded != fileMod)
/*     */       {
/* 139 */         this.m_profileEditor.loadTopicData(info, fileMod);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void notifyTopics(DataBinder binder)
/*     */     throws ServiceException
/*     */   {
/* 147 */     binder.removeResultSet("UpdatedUserTopics");
/*     */ 
/* 149 */     if ((this.m_monitoredTopics == null) || (!binder.m_isJava))
/*     */     {
/* 151 */       return;
/*     */     }
/*     */ 
/* 154 */     Hashtable refreshTopics = new Hashtable();
/*     */ 
/* 156 */     int num = this.m_monitoredTopics.size();
/* 157 */     for (int i = 0; i < num; i += 2)
/*     */     {
/* 159 */       String name = (String)this.m_monitoredTopics.elementAt(i);
/* 160 */       TopicInfo info = this.m_profileData.getTopic(name);
/* 161 */       if (info == null)
/*     */       {
/* 163 */         String msg = LocaleUtils.encodeMessage("csProfileTopicNotFound", null, name, this.m_userData.m_name);
/*     */ 
/* 165 */         Report.trace(null, LocaleResources.localizeMessage(msg, null), null);
/*     */       }
/*     */       else
/*     */       {
/* 169 */         boolean hasChanged = false;
/* 170 */         if (!hasChanged)
/*     */         {
/* 172 */           long ts = Long.parseLong((String)this.m_monitoredTopics.elementAt(i + 1));
/* 173 */           hasChanged = ts != info.m_lastLoaded;
/*     */         }
/* 175 */         if (!hasChanged)
/*     */           continue;
/* 177 */         refreshTopics.put(name, info);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 182 */     DataResultSet rset = new DataResultSet(UserProfileData.UPDATE_TOPIC_COLUMNS);
/*     */ 
/* 185 */     StringBuffer refreshBuff = new StringBuffer();
/* 186 */     for (Enumeration en = refreshTopics.elements(); en.hasMoreElements(); )
/*     */     {
/* 188 */       TopicInfo info = (TopicInfo)en.nextElement();
/*     */ 
/* 190 */       String name = info.m_name;
/* 191 */       String ts = String.valueOf(info.m_lastLoaded);
/* 192 */       if (refreshBuff.length() != 0)
/*     */       {
/* 194 */         refreshBuff.append(",");
/*     */       }
/* 196 */       refreshBuff.append(name);
/* 197 */       refreshBuff.append(",");
/* 198 */       refreshBuff.append(ts);
/*     */ 
/* 200 */       IdcCharArrayWriter sw = new IdcCharArrayWriter();
/*     */       try
/*     */       {
/* 203 */         info.m_data.sendEx(sw, false);
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 208 */         Report.trace(null, e, "csProfileUnableToUpdateTopic", new Object[] { name, this.m_userData.m_name });
/*     */       }
/*     */ 
/* 211 */       Vector row = new IdcVector();
/* 212 */       row.addElement(name);
/* 213 */       row.addElement(sw.toStringRelease());
/* 214 */       row.addElement(ts);
/* 215 */       rset.addRow(row);
/*     */     }
/* 217 */     binder.putLocal("refreshTopics", refreshBuff.toString());
/* 218 */     binder.addResultSet("UpdatedUserTopics", rset);
/*     */   }
/*     */ 
/*     */   public void checkAndCreateUserDirectory()
/*     */     throws ServiceException
/*     */   {
/* 227 */     String profileDir = makeUserProfileDirectoryName();
/* 228 */     this.m_profileEditor.setProfileDirectory(profileDir);
/* 229 */     Hashtable topics = this.m_profileData.getTopics();
/* 230 */     boolean hasTopics = (topics != null) && (topics.size() > 0);
/*     */ 
/* 232 */     if (!this.m_profileData.hasDirectory())
/*     */     {
/* 234 */       if (!hasTopics)
/*     */       {
/* 236 */         FileUtils.checkOrCreateDirectoryPrepareForLocks(profileDir, 2, true);
/*     */       }
/* 238 */       this.m_profileData.setDirectory(profileDir);
/*     */     }
/*     */     else
/*     */     {
/* 242 */       if (hasTopics)
/*     */         return;
/* 244 */       File f = new File(profileDir);
/* 245 */       if (f.exists())
/*     */         return;
/* 247 */       FileUtils.checkOrCreateDirectoryPrepareForLocks(profileDir, 2, true);
/*     */     }
/*     */   }
/*     */ 
/*     */   public String makeUserProfileDirectoryName()
/*     */   {
/* 255 */     String name = this.m_userData.m_name.toLowerCase();
/* 256 */     return makeUserProfileDirectoryNameEx(name);
/*     */   }
/*     */ 
/*     */   public static String makeUserProfileDirectoryNameEx(String name)
/*     */   {
/* 262 */     String subDir = null;
/* 263 */     name = StringUtils.encodeUrlStyle(name, '#', false);
/* 264 */     int offset = SharedObjects.getEnvironmentInt("UserProfileSubDirectoryOffset", 0);
/* 265 */     if (name.length() > offset + 1)
/*     */     {
/* 267 */       subDir = name.substring(offset, offset + 2);
/*     */     }
/*     */     else
/*     */     {
/* 271 */       subDir = name.charAt(0) + "z";
/*     */     }
/* 273 */     subDir = FileUtils.makeSafeDirectoryForNtfs(subDir);
/* 274 */     name = FileUtils.makeSafeDirectoryForNtfs(name);
/* 275 */     return LegacyDirectoryLocator.getUserProfilesDir() + subDir + "/" + name + "/";
/*     */   }
/*     */ 
/*     */   public UserProfileEditor getProfileEditor()
/*     */   {
/* 283 */     return this.m_profileEditor;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 288 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 84156 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.UserProfileManager
 * JD-Core Version:    0.5.4
 */