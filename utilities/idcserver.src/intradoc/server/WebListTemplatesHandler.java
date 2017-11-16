/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.Validation;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataBinderUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.shared.UserData;
/*     */ import java.io.IOException;
/*     */ 
/*     */ public class WebListTemplatesHandler extends ServiceHandler
/*     */ {
/*     */   @IdcServiceAction
/*     */   public void saveUserListTemplate()
/*     */     throws ServiceException, DataException
/*     */   {
/*  37 */     if ((this.m_service.m_userData == null) || (this.m_service.m_userData.m_name.equals("anonymous")))
/*     */     {
/*  39 */       String msg = LocaleUtils.encodeMessage("csAccessDenied", null);
/*  40 */       this.m_service.createServiceException(null, msg);
/*     */     }
/*     */ 
/*  43 */     validateIdAndLabel();
/*     */ 
/*  45 */     loadCustomUserListTemplates();
/*  46 */     DataResultSet listTemplates = (DataResultSet)this.m_binder.getResultSet("CustomListTemplates");
/*  47 */     if (listTemplates != null)
/*     */     {
/*  49 */       String listTemplateId = this.m_binder.getLocal("customListTemplateId");
/*  50 */       String listTemplateIdLower = listTemplateId.toLowerCase();
/*  51 */       for (listTemplates.first(); listTemplates.isRowPresent(); listTemplates.next())
/*     */       {
/*  53 */         String tmpTemplateId = ResultSetUtils.getValue(listTemplates, "customListTemplateId");
/*  54 */         if (!tmpTemplateId.toLowerCase().equals(listTemplateIdLower))
/*     */           continue;
/*  56 */         String msg = LocaleUtils.encodeMessage("csListTemplateIdAlreadyExists", null, listTemplateId);
/*  57 */         this.m_service.createServiceException(null, msg);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/*  62 */     setSaveTopicStrings();
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void validateIdAndLabel() throws DataException, ServiceException
/*     */   {
/*  68 */     String listTemplateId = this.m_binder.get("customListTemplateId").trim();
/*  69 */     if (listTemplateId.length() == 0)
/*     */     {
/*  71 */       throw new DataException(LocaleUtils.encodeMessage("syParameterNotFound", null, "customListTemplateId"));
/*     */     }
/*  73 */     validateListTemplateId(listTemplateId);
/*  74 */     this.m_binder.putLocal("customListTemplateId", listTemplateId);
/*     */ 
/*  76 */     String label = this.m_binder.get("label").trim();
/*  77 */     if (label.length() == 0)
/*     */     {
/*  79 */       throw new DataException(LocaleUtils.encodeMessage("syParameterNotFound", null, "label"));
/*     */     }
/*  81 */     this.m_binder.putLocal("label", label);
/*     */   }
/*     */ 
/*     */   public void validateListTemplateId(String listTemplateId) throws ServiceException
/*     */   {
/*  86 */     if (listTemplateId.indexOf(' ') >= 0)
/*     */     {
/*  88 */       String msg = LocaleUtils.encodeMessage("csListTemplateIdContainsSpaces", null);
/*  89 */       this.m_service.createServiceException(null, msg);
/*     */     }
/*     */ 
/*  92 */     int result = Validation.checkString(listTemplateId, 0);
/*  93 */     if (result != 0)
/*     */     {
/*  95 */       String msg = LocaleUtils.encodeMessage("csListTemplateIdContainsInvalidCharacters", null);
/*  96 */       this.m_service.createServiceException(null, msg);
/*     */     }
/*     */ 
/*  99 */     String otherInvalidChars = ".,";
/* 100 */     for (int i = 0; i < otherInvalidChars.length(); ++i)
/*     */     {
/* 102 */       if (listTemplateId.indexOf(otherInvalidChars.charAt(i)) < 0)
/*     */         continue;
/* 104 */       String msg = LocaleUtils.encodeMessage("csListTemplateIdContainsInvalidCharacters", null);
/* 105 */       this.m_service.createServiceException(null, msg);
/*     */     }
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void editUserListTemplate()
/*     */     throws ServiceException, DataException
/*     */   {
/* 113 */     if ((this.m_service.m_userData == null) || (this.m_service.m_userData.m_name.equals("anonymous")))
/*     */     {
/* 115 */       String msg = LocaleUtils.encodeMessage("csAccessDenied", null);
/* 116 */       this.m_service.createServiceException(null, msg);
/*     */     }
/*     */ 
/* 119 */     validateIdAndLabel();
/* 120 */     loadCustomUserListTemplates();
/* 121 */     setSaveTopicStrings();
/*     */ 
/* 123 */     String listTemplateId = this.m_binder.getLocal("customListTemplateId");
/* 124 */     String listTemplateIdLower = listTemplateId.toLowerCase();
/* 125 */     String oldListTemplateId = this.m_binder.get("oldListTemplateId");
/* 126 */     if (oldListTemplateId.toLowerCase().equals(listTemplateIdLower))
/*     */       return;
/* 128 */     int numTopics = DataBinderUtils.getInteger(this.m_binder, "numTopics", 0);
/* 129 */     ++numTopics;
/* 130 */     this.m_binder.putLocal("topicString" + numTopics, "deleteRows:pne_portal:CustomListTemplates:" + StringUtils.urlEncode(oldListTemplateId));
/* 131 */     this.m_binder.putLocal("numTopics", Integer.toString(numTopics));
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void loadCustomUserListTemplates()
/*     */   {
/* 138 */     PageMerger pm = this.m_service.getPageMerger();
/*     */     try
/*     */     {
/* 142 */       pm.evaluateScript("<$exec utLoadResultSet(\"pne_portal\", \"CustomListTemplates\")$>");
/*     */     }
/*     */     catch (IOException ignore)
/*     */     {
/* 146 */       Report.trace(null, null, ignore);
/*     */     }
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void setSaveTopicStrings() throws ServiceException, DataException
/*     */   {
/* 153 */     int numTopics = DataBinderUtils.getInteger(this.m_binder, "numTopics", 0);
/*     */ 
/* 155 */     ++numTopics;
/* 156 */     this.m_binder.putLocal("topicString" + numTopics, "addMruRow:pne_portal:CustomListTemplates:customListTemplateId,baseTemplateId,label");
/*     */ 
/* 158 */     this.m_binder.putLocal("numTopics", Integer.toString(numTopics));
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void deleteUserListTemplate() throws ServiceException, DataException
/*     */   {
/* 164 */     String listTemplateId = this.m_binder.get("listTemplateId");
/* 165 */     this.m_binder.putLocal("topicString1", "deleteRows:pne_portal:CustomListTemplates:" + StringUtils.urlEncode(listTemplateId));
/* 166 */     this.m_binder.putLocal("numTopics", "1");
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 171 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 70705 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.WebListTemplatesHandler
 * JD-Core Version:    0.5.4
 */