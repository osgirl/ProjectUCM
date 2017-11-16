/*     */ package intradoc.server.vcr;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataBinderUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.server.DocProfileManager;
/*     */ import intradoc.server.SearchService;
/*     */ import intradoc.server.Service;
/*     */ import intradoc.shared.DocProfileData;
/*     */ import intradoc.shared.FilterImplementor;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.HashMap;
/*     */ import java.util.Map;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class VcrFilterImplementor
/*     */   implements FilterImplementor
/*     */ {
/*     */   public DataBinder m_binder;
/*     */   public ExecutionContext m_cxt;
/*     */   public Workspace m_ws;
/*     */ 
/*     */   public int doFilter(Workspace ws, DataBinder binder, ExecutionContext cxt)
/*     */     throws ServiceException, DataException
/*     */   {
/*  53 */     this.m_binder = binder;
/*  54 */     this.m_cxt = cxt;
/*  55 */     this.m_ws = ws;
/*     */ 
/*  57 */     Object paramObj = cxt.getCachedObject("filterParameter");
/*  58 */     if ((paramObj == null) || (!paramObj instanceof String))
/*     */     {
/*  60 */       return 0;
/*     */     }
/*     */ 
/*  63 */     int returnCode = 0;
/*  64 */     String param = (String)paramObj;
/*  65 */     if (param.equals("prepareQueryText"))
/*     */     {
/*  67 */       returnCode = prepareQueryText();
/*     */     }
/*  69 */     else if (param.equals("getEnterpriseSearchResults"))
/*     */     {
/*  71 */       returnCode = getEnterpriseSearchResults();
/*     */     }
/*     */ 
/*  74 */     return returnCode;
/*     */   }
/*     */ 
/*     */   public int prepareQueryText() throws ServiceException, DataException
/*     */   {
/*  79 */     if (!this.m_cxt instanceof SearchService)
/*     */     {
/*  81 */       return 0;
/*     */     }
/*     */ 
/*  84 */     SearchService service = (SearchService)this.m_cxt;
/*  85 */     String contentType = this.m_binder.getLocal("vcrContentType");
/*  86 */     if ((contentType != null) && 
/*  88 */       (contentType.startsWith(VcrServiceHandler.PROFILE_CONTENT_TYPE_PREFIX)))
/*     */     {
/*  90 */       String profileName = contentType.substring(VcrServiceHandler.PROFILE_CONTENT_TYPE_PREFIX.length());
/*     */ 
/*  92 */       DocProfileData profileData = DocProfileManager.getProfile(profileName);
/*     */ 
/*  94 */       if (profileData == null)
/*     */       {
/*  96 */         String msg = LocaleUtils.encodeMessage("csVcrInvalidContentType", null, contentType);
/*     */ 
/*  98 */         service.createServiceException(null, msg);
/*     */       }
/*     */ 
/* 101 */       String triggerField = DocProfileManager.getTriggerField();
/* 102 */       String triggerValue = profileData.getValue("dpTriggerValue");
/* 103 */       String queryText = this.m_binder.get("QueryText");
/* 104 */       IdcStringBuilder newQueryText = new IdcStringBuilder();
/* 105 */       newQueryText.append(triggerField);
/* 106 */       newQueryText.append(" <matches> `");
/* 107 */       newQueryText.append(triggerValue);
/* 108 */       newQueryText.append('`');
/* 109 */       if (queryText.length() > 0)
/*     */       {
/* 111 */         newQueryText.append(" <AND> (");
/* 112 */         newQueryText.append(queryText);
/* 113 */         newQueryText.append(')');
/*     */       }
/* 115 */       this.m_binder.putLocal("QueryText", newQueryText.toString());
/*     */     }
/*     */ 
/* 119 */     return 0;
/*     */   }
/*     */ 
/*     */   public int getEnterpriseSearchResults() throws ServiceException, DataException
/*     */   {
/* 124 */     if (!this.m_cxt instanceof Service)
/*     */     {
/* 126 */       return 0;
/*     */     }
/*     */ 
/* 129 */     Service service = (Service)this.m_cxt;
/* 130 */     DataBinder binder = service.getBinder();
/* 131 */     if (!DataBinderUtils.getLocalBoolean(binder, "vcrAppendObjectClassInfo", false))
/*     */     {
/* 133 */       return 0;
/*     */     }
/*     */ 
/* 136 */     DataResultSet searchResults = (DataResultSet)binder.getResultSet("SearchResults");
/* 137 */     if (searchResults != null)
/*     */     {
/* 140 */       Vector v = new IdcVector();
/* 141 */       FieldInfo objectClassField = new FieldInfo();
/* 142 */       if (!searchResults.getFieldInfo("vcrObjectClass", objectClassField))
/*     */       {
/* 144 */         objectClassField.m_name = "vcrObjectClass";
/* 145 */         v.addElement(objectClassField);
/*     */       }
/* 147 */       if (v.size() > 0)
/*     */       {
/* 149 */         searchResults.mergeFieldsWithFlags(v, 0);
/* 150 */         searchResults.getFieldInfo("vcrObjectClass", objectClassField);
/*     */       }
/*     */ 
/* 153 */       String triggerFieldName = DocProfileManager.getTriggerField();
/* 154 */       FieldInfo triggerField = new FieldInfo();
/* 155 */       if (searchResults.getFieldInfo(triggerFieldName, triggerField))
/*     */       {
/* 157 */         DataResultSet dpSet = SharedObjects.getTable("DocumentProfiles");
/* 158 */         Map triggerProfileMap = new HashMap();
/* 159 */         if (dpSet != null)
/*     */         {
/* 161 */           int nameIndex = ResultSetUtils.getIndexMustExist(dpSet, "dpName");
/* 162 */           int triggerIndex = ResultSetUtils.getIndexMustExist(dpSet, "dpTriggerValue");
/* 163 */           for (dpSet.first(); dpSet.isRowPresent(); dpSet.next())
/*     */           {
/* 165 */             String profileName = dpSet.getStringValue(nameIndex);
/* 166 */             String triggerValue = dpSet.getStringValue(triggerIndex);
/* 167 */             triggerProfileMap.put(triggerValue, profileName);
/*     */           }
/*     */         }
/*     */ 
/* 171 */         for (searchResults.first(); searchResults.isRowPresent(); searchResults.next())
/*     */         {
/* 173 */           String objectClass = null;
/*     */ 
/* 175 */           String triggerValue = searchResults.getStringValue(triggerField.m_index);
/* 176 */           String profileName = (String)triggerProfileMap.get(triggerValue);
/* 177 */           if (profileName == null)
/*     */           {
/* 179 */             objectClass = VcrServiceHandler.GLOBAL_PROFILE_CONTENT_TYPE;
/*     */           }
/*     */           else
/*     */           {
/* 183 */             objectClass = VcrServiceHandler.PROFILE_CONTENT_TYPE_PREFIX + profileName;
/*     */           }
/*     */ 
/* 186 */           searchResults.setCurrentValue(objectClassField.m_index, objectClass);
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 191 */         Report.trace("system", "Unable to append object class information for the VCR. The profile trigger field does not exist in the result set.", null);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 196 */     return 0;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 201 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 74098 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.vcr.VcrFilterImplementor
 * JD-Core Version:    0.5.4
 */