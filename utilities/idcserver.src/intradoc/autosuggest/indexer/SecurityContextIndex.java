/*     */ package intradoc.autosuggest.indexer;
/*     */ 
/*     */ import intradoc.autosuggest.AutoSuggestConstants;
/*     */ import intradoc.autosuggest.AutoSuggestContext;
/*     */ import intradoc.autosuggest.datastore.AutoSuggestIdentifiersQueue;
/*     */ import intradoc.autosuggest.datastore.SecurityIdentifierInfoStorage;
/*     */ import intradoc.autosuggest.records.ContextInfo;
/*     */ import intradoc.autosuggest.records.SecurityIdentifierInfo;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class SecurityContextIndex
/*     */ {
/*     */   public SecurityIdentifierInfoStorage m_securityIdentifierInfoStorage;
/*     */   public AutoSuggestIdentifiersQueue m_autoSuggestIdentifiersQueue;
/*     */   public SecurityContextIndexWriter m_indexWriter;
/*     */   public AutoSuggestContext m_context;
/*     */   public ContextInfo m_contextInfo;
/*     */ 
/*     */   public void init(AutoSuggestContext context, ContextInfo contextInfo)
/*     */     throws DataException, ServiceException
/*     */   {
/*  45 */     this.m_context = context;
/*  46 */     this.m_contextInfo = contextInfo;
/*  47 */     this.m_autoSuggestIdentifiersQueue = new AutoSuggestIdentifiersQueue(context);
/*  48 */     this.m_indexWriter = new SecurityContextIndexWriter();
/*  49 */     this.m_indexWriter.init(context, contextInfo);
/*  50 */     this.m_securityIdentifierInfoStorage = new SecurityIdentifierInfoStorage(context);
/*     */   }
/*     */ 
/*     */   public SecurityIdentifierInfo getSecurityIdentifierInfo(String identifier) throws DataException {
/*  54 */     return this.m_securityIdentifierInfoStorage.get(identifier);
/*     */   }
/*     */ 
/*     */   public void enqueueAddition(String identifier, String securityGroupId, String accountId, String owner, String users, String groups, String roles, Map<String, String> extraParameters) throws DataException
/*     */   {
/*  59 */     this.m_autoSuggestIdentifiersQueue.add(identifier, securityGroupId, accountId, owner, users, groups, roles, extraParameters);
/*     */   }
/*     */ 
/*     */   public void enqueueDeletion(String identifier, String securityGroupId, String accountId, String owner, String users, String groups, String roles, Map<String, String> extraParameters) throws DataException
/*     */   {
/*  64 */     this.m_autoSuggestIdentifiersQueue.delete(identifier, securityGroupId, accountId, owner, users, groups, roles, extraParameters);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 184 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 102609 $";
/*     */   }
/*     */ 
/*     */   public class SecurityContextIndexWriter
/*     */     implements ContextIndexWriter
/*     */   {
/*     */     public SecurityContextIndexWriter()
/*     */     {
/*     */     }
/*     */ 
/*     */     public void init(AutoSuggestContext context, ContextInfo contextInfo)
/*     */       throws DataException, ServiceException
/*     */     {
/*     */     }
/*     */ 
/*     */     public void index(DataResultSet indexResultset)
/*     */       throws DataException, ServiceException
/*     */     {
/*  77 */       FieldInfo identifierFieldInfo = new FieldInfo();
/*  78 */       indexResultset.getFieldInfo(AutoSuggestConstants.FIELD_AUTOSUGGEST_IDENTIFIER, identifierFieldInfo);
/*  79 */       if (identifierFieldInfo == null)
/*     */       {
/*  81 */         return;
/*     */       }
/*  83 */       for (indexResultset.first(); indexResultset.isRowPresent(); indexResultset.next())
/*     */       {
/*  85 */         String identifier = indexResultset.getStringValue(identifierFieldInfo.m_index);
/*  86 */         String securityGroupId = indexResultset.getStringValueByName(AutoSuggestConstants.FIELD_AUTOSUGGEST_SECURITYGROUP_ID);
/*  87 */         String accountId = indexResultset.getStringValueByName(AutoSuggestConstants.FIELD_AUTOSUGGEST_ACCOUNT_ID);
/*  88 */         String owner = indexResultset.getStringValueByName(AutoSuggestConstants.FIELD_AUTOSUGGEST_OWNER);
/*  89 */         String users = indexResultset.getStringValueByName(AutoSuggestConstants.FIELD_AUTOSUGGEST_USERS);
/*  90 */         String groups = indexResultset.getStringValueByName(AutoSuggestConstants.FIELD_AUTOSUGGEST_GROUPS);
/*  91 */         String roles = indexResultset.getStringValueByName(AutoSuggestConstants.FIELD_AUTOSUGGEST_ROLES);
/*  92 */         Map currentRowMap = indexResultset.getCurrentRowMap();
/*  93 */         if (Report.m_verbose)
/*     */         {
/*  95 */           Report.trace("autosuggest", "Indexing Identifier -- " + currentRowMap, null);
/*     */         }
/*  97 */         currentRowMap.remove(AutoSuggestConstants.FIELD_AUTOSUGGEST_SECURITYGROUP_ID);
/*  98 */         currentRowMap.remove(AutoSuggestConstants.FIELD_AUTOSUGGEST_ACCOUNT_ID);
/*  99 */         currentRowMap.remove(AutoSuggestConstants.FIELD_AUTOSUGGEST_OWNER);
/* 100 */         currentRowMap.remove(AutoSuggestConstants.FIELD_AUTOSUGGEST_USERS);
/* 101 */         currentRowMap.remove(AutoSuggestConstants.FIELD_AUTOSUGGEST_GROUPS);
/* 102 */         currentRowMap.remove(AutoSuggestConstants.FIELD_AUTOSUGGEST_ROLES);
/* 103 */         currentRowMap.remove(AutoSuggestConstants.FIELD_AUTOSUGGEST_IDENTIFIER);
/* 104 */         indexIdentifier(identifier, securityGroupId, accountId, owner, users, groups, roles, currentRowMap);
/*     */       }
/*     */     }
/*     */ 
/*     */     public void indexQueues()
/*     */       throws DataException, ServiceException
/*     */     {
/* 112 */       Iterator identifierAdditionIterator = SecurityContextIndex.this.m_autoSuggestIdentifiersQueue.additionRemoveIterator();
/* 113 */       while (identifierAdditionIterator.hasNext())
/*     */       {
/* 115 */         SecurityIdentifierInfo securityIdentifierInfo = (SecurityIdentifierInfo)identifierAdditionIterator.next();
/* 116 */         indexIdentifier(securityIdentifierInfo.m_identifier, securityIdentifierInfo.m_securityGroupId, securityIdentifierInfo.m_accountId, securityIdentifierInfo.m_owner, securityIdentifierInfo.m_users, securityIdentifierInfo.m_groups, securityIdentifierInfo.m_roles, securityIdentifierInfo.m_extraParameters);
/*     */       }
/*     */ 
/* 119 */       Iterator identifierDeletionIterator = SecurityContextIndex.this.m_autoSuggestIdentifiersQueue.deletionRemoveIterator();
/* 120 */       while (identifierDeletionIterator.hasNext())
/*     */       {
/* 122 */         SecurityIdentifierInfo securityIdentifierInfo = (SecurityIdentifierInfo)identifierDeletionIterator.next();
/* 123 */         SecurityContextIndex.this.m_securityIdentifierInfoStorage.remove(securityIdentifierInfo.m_identifier);
/*     */       }
/*     */ 
/* 126 */       SecurityContextIndex.this.m_autoSuggestIdentifiersQueue.commit();
/*     */     }
/*     */ 
/*     */     public void remove(DataResultSet indexResultset)
/*     */       throws DataException, ServiceException
/*     */     {
/* 133 */       FieldInfo identifierFieldInfo = new FieldInfo();
/* 134 */       indexResultset.getFieldInfo(AutoSuggestConstants.FIELD_AUTOSUGGEST_IDENTIFIER, identifierFieldInfo);
/* 135 */       if ((identifierFieldInfo == null) || (identifierFieldInfo.m_index == -1))
/*     */         return;
/* 137 */       for (indexResultset.first(); indexResultset.isRowPresent(); indexResultset.next())
/*     */       {
/* 139 */         String identifier = indexResultset.getStringValue(identifierFieldInfo.m_index);
/* 140 */         SecurityContextIndex.this.m_securityIdentifierInfoStorage.remove(identifier);
/*     */       }
/*     */     }
/*     */ 
/*     */     public void indexIdentifier(String identifier, String securityGroupId, String accountId, String owner, String users, String groups, String roles, Map<String, String> extraParameters)
/*     */       throws DataException, ServiceException
/*     */     {
/* 150 */       Report.trace("autosuggest", "Indexing Identifier " + identifier, null);
/* 151 */       SecurityIdentifierInfo securityIdentifierInfo = new SecurityIdentifierInfo(SecurityContextIndex.this.m_context);
/* 152 */       securityIdentifierInfo.init(identifier, securityGroupId, accountId, owner, users, groups, roles, extraParameters);
/* 153 */       SecurityContextIndex.this.m_securityIdentifierInfoStorage.put(identifier, securityIdentifierInfo);
/* 154 */       if (!Report.m_verbose)
/*     */         return;
/* 156 */       Report.trace("autosuggest", "Indexed Identifier -- " + securityIdentifierInfo.toString(), null);
/*     */     }
/*     */ 
/*     */     public void indexIdentifier(String identifier, String securityGroupId, String accountId, String owner, List<String> users, List<String> groups, List<String> roles, Map<String, String> extraParameters)
/*     */       throws DataException, ServiceException
/*     */     {
/* 162 */       Report.trace("autosuggest", "Indexing Identifier " + identifier, null);
/* 163 */       SecurityIdentifierInfo securityIdentifierInfo = new SecurityIdentifierInfo(SecurityContextIndex.this.m_context);
/* 164 */       securityIdentifierInfo.init(identifier, securityGroupId, accountId, owner, users, groups, roles, extraParameters);
/* 165 */       SecurityContextIndex.this.m_securityIdentifierInfoStorage.put(identifier, securityIdentifierInfo);
/* 166 */       if (!Report.m_verbose)
/*     */         return;
/* 168 */       Report.trace("autosuggest", "Indexed Identifier -- " + securityIdentifierInfo.toString(), null);
/*     */     }
/*     */ 
/*     */     public void clear()
/*     */       throws DataException, ServiceException
/*     */     {
/* 178 */       SecurityContextIndex.this.m_securityIdentifierInfoStorage.clear();
/* 179 */       SecurityContextIndex.this.m_autoSuggestIdentifiersQueue.clear();
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.autosuggest.indexer.SecurityContextIndex
 * JD-Core Version:    0.5.4
 */