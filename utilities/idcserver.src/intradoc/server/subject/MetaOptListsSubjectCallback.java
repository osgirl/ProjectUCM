/*     */ package intradoc.server.subject;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.server.SubjectCallbackAdapter;
/*     */ import intradoc.shared.MetaFieldData;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class MetaOptListsSubjectCallback extends SubjectCallbackAdapter
/*     */ {
/*     */   public void refresh(String subject)
/*     */     throws DataException, ServiceException
/*     */   {
/*  35 */     ResultSet rset = this.m_workspace.createResultSet("OptionsList", null);
/*     */ 
/*  37 */     DataResultSet dset = new DataResultSet();
/*  38 */     dset.copy(rset);
/*  39 */     SharedObjects.putTable("OptionsList", dset);
/*     */ 
/*  41 */     loadDocOptionsList(dset, null);
/*     */   }
/*     */ 
/*     */   public void loadBinder(String subject, DataBinder binder, ExecutionContext cxt)
/*     */   {
/*  47 */     Exception theException = null;
/*     */     try
/*     */     {
/*  50 */       ResultSet rset = SharedObjects.getTable("OptionsList");
/*  51 */       loadDocOptionsList(rset, binder);
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/*  55 */       theException = e;
/*     */     }
/*  57 */     if (theException == null) {
/*     */       return;
/*     */     }
/*  60 */     String msg = LocaleUtils.encodeMessage("csUnableToLoadSubject", theException.getMessage(), "METAOPTLISTS");
/*     */ 
/*  62 */     Report.trace(null, LocaleResources.localizeMessage(msg, cxt), theException);
/*     */   }
/*     */ 
/*     */   public void loadDocOptionsList(ResultSet optionsList, DataBinder binder)
/*     */     throws DataException
/*     */   {
/*  69 */     String[] fields = { "dKey", "dOption" };
/*  70 */     FieldInfo[] infoList = ResultSetUtils.createInfoList(optionsList, fields, true);
/*  71 */     int keyindex = infoList[0].m_index;
/*  72 */     int optindex = infoList[1].m_index;
/*     */ 
/*  74 */     String curKey = null;
/*  75 */     Vector curList = null;
/*     */ 
/*  77 */     Hashtable loadedLists = new Hashtable();
/*     */ 
/*  79 */     for (optionsList.first(); optionsList.isRowPresent(); optionsList.next())
/*     */     {
/*  81 */       String key = optionsList.getStringValue(keyindex);
/*  82 */       String value = optionsList.getStringValue(optindex);
/*  83 */       if ((curKey == null) || (!curKey.equals(key)))
/*     */       {
/*  85 */         curKey = key;
/*  86 */         if (binder == null)
/*     */         {
/*  88 */           curList = SharedObjects.getOptList(curKey);
/*     */         }
/*     */         else
/*     */         {
/*  92 */           curList = null;
/*     */         }
/*     */ 
/*  95 */         if (curList == null)
/*     */         {
/*  97 */           curList = new IdcVector();
/*  98 */           if (binder != null)
/*     */           {
/* 100 */             binder.addOptionList(curKey, curList);
/*     */           }
/*     */           else
/*     */           {
/* 104 */             SharedObjects.putOptList(curKey, curList);
/*     */           }
/*     */         }
/*     */         else
/*     */         {
/* 109 */           curList.removeAllElements();
/*     */         }
/*     */ 
/* 112 */         loadedLists.put(curKey, "1");
/*     */       }
/* 114 */       curList.addElement(value);
/*     */     }
/*     */ 
/* 118 */     MetaFieldData metaFields = (MetaFieldData)SharedObjects.getTable("DocMetaDefinition");
/* 119 */     metaFields.first();
/*     */ 
/* 121 */     for (metaFields.first(); metaFields.isRowPresent(); metaFields.next())
/*     */     {
/* 123 */       String optKey = metaFields.getStringValue(metaFields.m_optionsListKeyIndex);
/* 124 */       if (optKey == null)
/*     */         continue;
/* 126 */       optKey = optKey.trim();
/* 127 */       if ((optKey.length() <= 0) || (loadedLists.get(optKey) != null))
/*     */         continue;
/* 129 */       Vector v = new IdcVector();
/* 130 */       if (binder != null)
/*     */       {
/* 132 */         binder.addOptionList(optKey, v);
/*     */       }
/*     */       else
/*     */       {
/* 136 */         SharedObjects.putOptList(optKey, v);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 146 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.subject.MetaOptListsSubjectCallback
 * JD-Core Version:    0.5.4
 */