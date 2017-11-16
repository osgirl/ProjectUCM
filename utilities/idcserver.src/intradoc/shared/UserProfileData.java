/*     */ package intradoc.shared;
/*     */ 
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.io.BufferedReader;
/*     */ import java.io.IOException;
/*     */ import java.io.StringReader;
/*     */ import java.util.Hashtable;
/*     */ 
/*     */ public class UserProfileData
/*     */ {
/*  35 */   protected Hashtable m_topics = null;
/*     */ 
/*  37 */   protected boolean m_hasDirectory = false;
/*  38 */   protected String m_directory = null;
/*     */ 
/*  40 */   protected long m_globalTimeStamp = -2L;
/*     */ 
/*  42 */   public static final String[] UPDATE_TOPIC_COLUMNS = { "topicName", "topicValue", "topicTS" };
/*     */ 
/*     */   public UserProfileData()
/*     */   {
/*  47 */     this.m_topics = new Hashtable();
/*     */   }
/*     */ 
/*     */   public void setTopicInfo(TopicInfo info)
/*     */   {
/*  52 */     this.m_topics.put(info.m_lookupKey, info);
/*     */   }
/*     */ 
/*     */   public boolean hasDirectory()
/*     */   {
/*  57 */     return this.m_hasDirectory;
/*     */   }
/*     */ 
/*     */   public void setDirectory(String dir)
/*     */   {
/*  62 */     if (dir == null)
/*     */       return;
/*  64 */     this.m_hasDirectory = true;
/*  65 */     this.m_directory = dir;
/*     */   }
/*     */ 
/*     */   public void updateTopics(ResultSet drset)
/*     */     throws DataException
/*     */   {
/*  75 */     if (drset == null)
/*     */     {
/*  77 */       return;
/*     */     }
/*     */ 
/*  80 */     FieldInfo[] infos = ResultSetUtils.createInfoList(drset, UPDATE_TOPIC_COLUMNS, true);
/*  81 */     int nameIndex = infos[0].m_index;
/*  82 */     int valIndex = infos[1].m_index;
/*  83 */     int tsIndex = infos[2].m_index;
/*     */ 
/*  85 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*     */     {
/*  87 */       String name = drset.getStringValue(nameIndex);
/*  88 */       String val = drset.getStringValue(valIndex);
/*  89 */       String ts = drset.getStringValue(tsIndex);
/*     */ 
/*  91 */       DataBinder binder = new DataBinder();
/*     */       try
/*     */       {
/*  94 */         StringReader sr = new StringReader(val);
/*  95 */         binder.receive(new BufferedReader(sr));
/*     */ 
/*  97 */         TopicInfo info = (TopicInfo)this.m_topics.get(name);
/*  98 */         if (info == null)
/*     */         {
/* 100 */           info = new TopicInfo(name);
/* 101 */           this.m_topics.put(name, info);
/*     */         }
/* 103 */         info.m_data = binder;
/* 104 */         info.m_lastLoaded = Long.parseLong(ts);
/*     */       }
/*     */       catch (IOException e)
/*     */       {
/* 108 */         IdcMessage msg = IdcMessageFactory.lc(e);
/* 109 */         String msgText = LocaleResources.localizeMessage(null, msg, null).toString();
/* 110 */         Report.trace(null, "Unable to update the topic " + name + ". " + msgText, e);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public String getDirectory()
/*     */   {
/* 120 */     return this.m_directory;
/*     */   }
/*     */ 
/*     */   public Hashtable getTopics()
/*     */   {
/* 125 */     return this.m_topics;
/*     */   }
/*     */ 
/*     */   public TopicInfo getTopic(String topic)
/*     */   {
/* 130 */     return (TopicInfo)this.m_topics.get(topic);
/*     */   }
/*     */ 
/*     */   public void addTopic(TopicInfo info)
/*     */   {
/* 135 */     this.m_topics.put(info.m_lookupKey, info);
/*     */   }
/*     */ 
/*     */   public long getGlobalTimeStamp()
/*     */   {
/* 140 */     return this.m_globalTimeStamp;
/*     */   }
/*     */ 
/*     */   public void setGlobalTimeStamp(long ts)
/*     */   {
/* 145 */     this.m_globalTimeStamp = ts;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 150 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 71159 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.UserProfileData
 * JD-Core Version:    0.5.4
 */