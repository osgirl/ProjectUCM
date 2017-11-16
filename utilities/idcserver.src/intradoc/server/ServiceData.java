/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.resource.ComponentData;
/*     */ import intradoc.resource.ResourceObjectLoader;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class ServiceData
/*     */ {
/*     */   public String m_name;
/*     */   public String m_classID;
/*     */   public String m_formType;
/*     */   public int m_accessLevel;
/*     */   public String m_htmlPage;
/*     */   public Vector m_actionList;
/*     */   public String m_serviceType;
/*     */   public String m_errorMsg;
/*     */   public String m_successMsg;
/*     */   public Vector m_subjects;
/*     */   public ComponentData m_componentData;
/*     */ 
/*     */   public ServiceData()
/*     */   {
/*  67 */     this.m_name = "NO NAME";
/*  68 */     this.m_classID = "Service";
/*  69 */     this.m_formType = "";
/*  70 */     this.m_accessLevel = 0;
/*  71 */     this.m_htmlPage = null;
/*  72 */     this.m_actionList = new IdcVector();
/*  73 */     this.m_serviceType = "";
/*  74 */     this.m_errorMsg = "";
/*  75 */     this.m_subjects = null;
/*  76 */     this.m_componentData = null;
/*     */   }
/*     */ 
/*     */   public void init(String name, String classID, int accessLevel, String htmlPage, String serviceType, String errMsg, String subjects)
/*     */     throws Exception
/*     */   {
/*  82 */     this.m_name = name;
/*  83 */     this.m_classID = classID;
/*  84 */     this.m_accessLevel = accessLevel;
/*  85 */     this.m_htmlPage = htmlPage;
/*  86 */     this.m_actionList = new IdcVector();
/*  87 */     this.m_serviceType = serviceType;
/*  88 */     this.m_errorMsg = errMsg;
/*  89 */     this.m_subjects = StringUtils.parseArray(subjects, ',', ',');
/*     */   }
/*     */ 
/*     */   public ServiceData shallowClone()
/*     */   {
/*  94 */     ServiceData d = new ServiceData();
/*     */ 
/*  96 */     d.m_name = this.m_name;
/*  97 */     d.m_classID = this.m_classID;
/*  98 */     d.m_formType = this.m_formType;
/*  99 */     d.m_accessLevel = this.m_accessLevel;
/* 100 */     d.m_htmlPage = this.m_htmlPage;
/* 101 */     d.m_actionList = this.m_actionList;
/* 102 */     d.m_serviceType = this.m_serviceType;
/* 103 */     d.m_errorMsg = this.m_errorMsg;
/* 104 */     d.m_successMsg = this.m_successMsg;
/* 105 */     d.m_subjects = this.m_subjects;
/* 106 */     d.m_componentData = this.m_componentData;
/*     */ 
/* 108 */     return d;
/*     */   }
/*     */ 
/*     */   public void init(String name, String attribs, String scripts)
/*     */     throws DataException
/*     */   {
/* 119 */     this.m_name = name;
/*     */ 
/* 122 */     Vector vAttribs = StringUtils.parseArray(attribs, '\n', '\\');
/* 123 */     if (vAttribs.size() != 6)
/*     */     {
/* 125 */       String msg = LocaleUtils.encodeMessage("csServiceHasWrongNumberOfAttributes", null, name);
/*     */ 
/* 127 */       throw new DataException(msg);
/*     */     }
/*     */ 
/* 130 */     int count = 0;
/*     */     try
/*     */     {
/* 133 */       this.m_classID = ResourceObjectLoader.parseStringValue(vAttribs, count++);
/* 134 */       this.m_accessLevel = ResourceObjectLoader.parseIntValue(vAttribs, count++);
/* 135 */       this.m_htmlPage = ResourceObjectLoader.parseStringValue(vAttribs, count++);
/* 136 */       this.m_serviceType = ResourceObjectLoader.parseStringValue(vAttribs, count++);
/*     */ 
/* 138 */       String subjects = ResourceObjectLoader.parseStringValue(vAttribs, count++);
/* 139 */       this.m_subjects = StringUtils.parseArray(subjects, ',', ',');
/*     */ 
/* 141 */       this.m_errorMsg = ResourceObjectLoader.parseStringValue(vAttribs, count++);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 146 */       String msg = LocaleUtils.encodeMessage("csServiceIncorrectValue", null, name, "" + count);
/*     */ 
/* 148 */       throw new DataException(msg);
/*     */     }
/*     */ 
/* 151 */     this.m_actionList = new IdcVector();
/* 152 */     Vector vScripts = StringUtils.parseArray(scripts, '\n', '\\');
/* 153 */     int nscripts = vScripts.size();
/* 154 */     for (int i = 0; i < nscripts; ++i)
/*     */     {
/* 156 */       String action = (String)vScripts.elementAt(i);
/* 157 */       addAction(action);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void addAction(String action) throws DataException
/*     */   {
/* 163 */     int actionIndex = this.m_actionList.size() + 1;
/*     */ 
/* 165 */     Vector vActions = StringUtils.parseArray(action, ':', '*');
/* 166 */     if (vActions.size() != 5)
/*     */     {
/* 168 */       String msg = LocaleUtils.encodeMessage("csServiceIncorrectAction", null, this.m_name, "" + actionIndex);
/*     */ 
/* 170 */       throw new DataException(msg);
/*     */     }
/*     */ 
/* 173 */     int count = 0;
/*     */     try
/*     */     {
/* 176 */       int type = ResourceObjectLoader.parseIntValue(vActions, count++);
/* 177 */       String fnct = ResourceObjectLoader.parseStringValue(vActions, count++);
/* 178 */       String params = ResourceObjectLoader.parseStringValue(vActions, count++);
/* 179 */       String flags = ResourceObjectLoader.parseStringValue(vActions, count++);
/* 180 */       String err = ResourceObjectLoader.parseStringValue(vActions, count++);
/*     */ 
/* 182 */       Action act = new Action();
/* 183 */       act.init(type, fnct, params, flags, err);
/* 184 */       this.m_actionList.addElement(act);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 188 */       throw new DataException(e, "csServiceIncorrectAction2", new Object[] { this.m_name, "" + actionIndex, "" + count });
/*     */     }
/*     */   }
/*     */ 
/*     */   public void addAction(int type, String fnct, String params, String flags, String err)
/*     */     throws DataException
/*     */   {
/* 196 */     Action act = new Action();
/* 197 */     act.init(type, fnct, params, flags, err);
/* 198 */     this.m_actionList.addElement(act);
/*     */   }
/*     */ 
/*     */   public void addAction(Action act)
/*     */   {
/* 203 */     this.m_actionList.addElement(act);
/*     */   }
/*     */ 
/*     */   public Vector getActionList()
/*     */   {
/* 210 */     Vector actionListClone = (Vector)this.m_actionList.clone();
/* 211 */     for (int i = 0; i < actionListClone.size(); ++i)
/*     */     {
/* 213 */       Action a = (Action)actionListClone.elementAt(i);
/* 214 */       a = a.shallowClone();
/* 215 */       actionListClone.setElementAt(a, i);
/*     */     }
/* 217 */     return actionListClone;
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 223 */     return this.m_name;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 228 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 69834 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.ServiceData
 * JD-Core Version:    0.5.4
 */