/*     */ package intradoc.shared;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSet;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class TopicInfo
/*     */ {
/*     */   public String m_lookupKey;
/*     */   public String m_name;
/*  45 */   public String m_filename = null;
/*     */ 
/*  50 */   public String m_directory = null;
/*     */ 
/*  55 */   public long m_lastLoaded = -2L;
/*     */ 
/*  60 */   public DataBinder m_data = new DataBinder();
/*     */ 
/*  65 */   public boolean m_isRelocated = false;
/*  66 */   public TopicImplementor m_topicImp = null;
/*     */ 
/*     */   public TopicInfo()
/*     */   {
/*  70 */     this.m_name = null;
/*  71 */     this.m_lookupKey = null;
/*     */   }
/*     */ 
/*     */   public TopicInfo(String name)
/*     */   {
/*  76 */     this.m_name = name.toLowerCase();
/*  77 */     this.m_lookupKey = this.m_name;
/*     */ 
/*  79 */     this.m_filename = (this.m_name + ".hda");
/*     */   }
/*     */ 
/*     */   public void init(DataBinder data, boolean isMerge) throws ServiceException
/*     */   {
/*  84 */     if (isMerge)
/*     */     {
/*  86 */       this.m_data = mergeInNewData(data);
/*     */     }
/*     */     else
/*     */     {
/*  90 */       this.m_data = data;
/*     */     }
/*     */ 
/*  94 */     String implementorClass = SharedObjects.getEnvironmentValue(this.m_name + "_TopicImplementor");
/*  95 */     if ((implementorClass == null) || (implementorClass.length() == 0))
/*     */     {
/*  97 */       implementorClass = this.m_data.getLocal("TopicImplementor");
/*  98 */       if ((implementorClass == null) || (implementorClass.length() == 0))
/*     */       {
/* 101 */         implementorClass = "intradoc.shared.BasicTopicImplementor";
/*     */       }
/*     */     }
/*     */ 
/* 105 */     this.m_topicImp = ((TopicImplementor)ComponentClassFactory.createClassInstance("TopicImplementor", implementorClass, "!csTopicImplementorClassError"));
/*     */   }
/*     */ 
/*     */   public void copy(TopicInfo info)
/*     */   {
/* 114 */     this.m_lookupKey = info.m_lookupKey;
/* 115 */     this.m_name = info.m_name;
/*     */ 
/* 117 */     this.m_lastLoaded = info.m_lastLoaded;
/*     */ 
/* 119 */     this.m_filename = info.m_filename;
/* 120 */     this.m_directory = info.m_directory;
/*     */ 
/* 122 */     this.m_topicImp = info.m_topicImp;
/* 123 */     this.m_data = copyData(info.m_data);
/*     */   }
/*     */ 
/*     */   public DataBinder copyData(DataBinder data)
/*     */   {
/* 128 */     DataBinder copy = new DataBinder();
/*     */     Enumeration en;
/* 129 */     if (data != null)
/*     */     {
/* 131 */       copy.copyLocalDataStateClone(data);
/*     */ 
/* 134 */       for (en = data.getResultSetList(); en.hasMoreElements(); )
/*     */       {
/* 136 */         String name = (String)en.nextElement();
/* 137 */         DataResultSet rset = (DataResultSet)data.getResultSet(name);
/*     */ 
/* 139 */         DataResultSet cSet = new DataResultSet();
/* 140 */         cSet.copy(rset);
/* 141 */         copy.addResultSet(name, cSet);
/*     */       }
/*     */     }
/* 144 */     return copy;
/*     */   }
/*     */ 
/*     */   public DataBinder mergeInNewData(DataBinder data)
/*     */   {
/* 150 */     DataBinder result = this.m_data;
/*     */     Enumeration en;
/* 151 */     if (data != null)
/*     */     {
/* 154 */       Map newProps = data.getLocalData();
/* 155 */       for (String key : newProps.keySet())
/*     */       {
/* 157 */         String curVal = this.m_data.getLocal(key);
/* 158 */         if ((key.equals("topicCounter")) || (curVal == null))
/*     */         {
/* 160 */           String val = (String)newProps.get(key);
/* 161 */           this.m_data.putLocal(key, val);
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 166 */       for (en = data.getResultSetList(); en.hasMoreElements(); )
/*     */       {
/* 168 */         String name = (String)en.nextElement();
/* 169 */         if (this.m_data.getResultSet(name) == null)
/*     */         {
/* 171 */           DataResultSet drset = (DataResultSet)result.getResultSet(name);
/* 172 */           DataResultSet cSet = new DataResultSet();
/* 173 */           cSet.copy(drset);
/* 174 */           this.m_data.addResultSet(name, cSet);
/*     */         }
/*     */       }
/*     */     }
/* 178 */     return result;
/*     */   }
/*     */ 
/*     */   public ResultSet retrieveResultSet(String rName, ExecutionContext cxt)
/*     */   {
/* 183 */     return this.m_topicImp.retrieveResultSet(rName, this.m_data, cxt);
/*     */   }
/*     */ 
/*     */   public String getFilePath()
/*     */   {
/* 188 */     return this.m_directory + this.m_filename;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 193 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 71154 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.TopicInfo
 * JD-Core Version:    0.5.4
 */