/*     */ package intradoc.autosuggest.indexer;
/*     */ 
/*     */ import intradoc.autosuggest.AutoSuggestContext;
/*     */ import intradoc.autosuggest.datastore.ContextInfoStorage;
/*     */ import intradoc.autosuggest.records.ContextInfo;
/*     */ import intradoc.autosuggest.records.GramInfo;
/*     */ import intradoc.autosuggest.records.MetaInfo;
/*     */ import intradoc.autosuggest.records.OccurrenceInfo;
/*     */ import intradoc.autosuggest.records.SecurityIdentifierInfo;
/*     */ import intradoc.autosuggest.records.TermInfo;
/*     */ import intradoc.autosuggest.utils.GramParameterConstructor;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.server.Service;
/*     */ import intradoc.shared.PluginFilters;
/*     */ import java.util.Iterator;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class AutoSuggestIndexHandler
/*     */ {
/*     */   public AutoSuggestContext m_context;
/*     */   public MetaInfo m_metaInfo;
/*     */   public ContextInfo m_contextInfo;
/*     */   public ContextIndexWriter m_indexWriter;
/*     */   public FieldContextIndex m_fieldContextIndex;
/*     */   public SecurityContextIndex m_securityContextIndex;
/*     */ 
/*     */   public AutoSuggestIndexHandler(AutoSuggestContext context, MetaInfo metaInfo)
/*     */     throws DataException, ServiceException
/*     */   {
/*  43 */     this.m_context = context;
/*  44 */     this.m_metaInfo = metaInfo;
/*  45 */     this.m_contextInfo = ContextInfoStorage.getContextInfo(this.m_context.m_contextKey);
/*  46 */     this.m_fieldContextIndex = new FieldContextIndex();
/*  47 */     this.m_fieldContextIndex.init(context, this.m_contextInfo);
/*  48 */     this.m_securityContextIndex = null;
/*  49 */     if (this.m_contextInfo.m_isSecurityContext)
/*     */     {
/*  51 */       this.m_securityContextIndex = new SecurityContextIndex();
/*  52 */       this.m_securityContextIndex.init(context, this.m_contextInfo);
/*  53 */       this.m_indexWriter = this.m_securityContextIndex.m_indexWriter;
/*     */     }
/*     */     else
/*     */     {
/*  60 */       ContextInfo securityContextInfo = this.m_contextInfo.getSecurityContextInfo();
/*  61 */       if (securityContextInfo != null)
/*     */       {
/*  63 */         this.m_securityContextIndex = new SecurityContextIndex();
/*  64 */         String securityContextKey = this.m_contextInfo.getSecurityContext();
/*  65 */         AutoSuggestContext securityContext = new AutoSuggestContext(securityContextKey, this.m_context.m_workspace);
/*  66 */         securityContext.prepareActiveContext();
/*  67 */         this.m_securityContextIndex.init(securityContext, securityContextInfo);
/*     */       }
/*  69 */       this.m_indexWriter = this.m_fieldContextIndex.m_indexWriter;
/*     */     }
/*     */   }
/*     */ 
/*     */   public void enqueueTermAddition(String term, String identifier, Map<String, String> extraParameters)
/*     */     throws DataException
/*     */   {
/*  78 */     this.m_fieldContextIndex.enqueueAddition(term, identifier, extraParameters);
/*     */   }
/*     */ 
/*     */   public void enqueueTermDeletion(String term, String identifier, Map<String, String> extraParameters) throws DataException {
/*  82 */     this.m_fieldContextIndex.enqueueDeletion(term, identifier, extraParameters);
/*     */   }
/*     */ 
/*     */   public void enqueueIdentifierAddition(String identifier, String securityGroupId, String accountId, String owner, String users, String groups, String roles, Map<String, String> extraParameters) throws DataException
/*     */   {
/*  87 */     if (this.m_securityContextIndex == null)
/*     */       return;
/*  89 */     this.m_securityContextIndex.enqueueAddition(identifier, securityGroupId, accountId, owner, users, groups, roles, extraParameters);
/*     */   }
/*     */ 
/*     */   public void enqueueIdentifierDeletion(String identifier, String securityGroupId, String accountId, String owner, String users, String groups, String roles, Map<String, String> extraParameters)
/*     */     throws DataException
/*     */   {
/*  95 */     if (this.m_securityContextIndex == null)
/*     */       return;
/*  97 */     this.m_securityContextIndex.enqueueDeletion(identifier, securityGroupId, accountId, owner, users, groups, roles, extraParameters);
/*     */   }
/*     */ 
/*     */   public void indexQueues()
/*     */     throws DataException, ServiceException
/*     */   {
/* 108 */     DataBinder binder = this.m_context.m_service.getBinder();
/* 109 */     this.m_context.m_service.setCachedObject("AutoSuggestIdentifiersQueue", (this.m_securityContextIndex != null) ? this.m_securityContextIndex.m_autoSuggestIdentifiersQueue : null);
/* 110 */     this.m_context.m_service.setCachedObject("AutoSuggestTermsQueue", this.m_fieldContextIndex.m_autoSuggestTermsQueue);
/* 111 */     this.m_context.m_service.setCachedObject("AutoSuggestContext", this.m_context);
/* 112 */     this.m_context.m_service.setCachedObject("AutoSuggestIndexHandler", this);
/* 113 */     PluginFilters.filter("preContextIndexingQueues", this.m_context.m_workspace, binder, this.m_context.m_service);
/* 114 */     this.m_indexWriter.indexQueues();
/* 115 */     PluginFilters.filter("postContextIndexingQueues", this.m_context.m_workspace, binder, this.m_context.m_service);
/*     */   }
/*     */ 
/*     */   public void index(DataResultSet indexResultset)
/*     */     throws DataException, ServiceException
/*     */   {
/* 127 */     if ((indexResultset == null) || (indexResultset.getNumRows() <= 0))
/*     */       return;
/* 129 */     DataBinder binder = this.m_context.m_service.getBinder();
/* 130 */     binder.addResultSet("indexResultSet", indexResultset);
/* 131 */     this.m_context.m_service.setCachedObject("AutoSuggestContext", this.m_context);
/* 132 */     this.m_context.m_service.setCachedObject("AutoSuggestIndexHandler", this);
/* 133 */     PluginFilters.filter("preContextIndexing", this.m_context.m_workspace, binder, this.m_context.m_service);
/* 134 */     this.m_indexWriter.index(indexResultset);
/* 135 */     PluginFilters.filter("postContextIndexing", this.m_context.m_workspace, binder, this.m_context.m_service);
/*     */   }
/*     */ 
/*     */   public void remove(DataResultSet indexResultset)
/*     */     throws DataException, ServiceException
/*     */   {
/* 147 */     if ((indexResultset == null) || (indexResultset.getNumRows() <= 0))
/*     */       return;
/* 149 */     DataBinder binder = this.m_context.m_service.getBinder();
/* 150 */     binder.addResultSet("indexResultSet", indexResultset);
/* 151 */     this.m_context.m_service.setCachedObject("AutoSuggestContext", this.m_context);
/* 152 */     this.m_context.m_service.setCachedObject("AutoSuggestIndexHandler", this);
/* 153 */     PluginFilters.filter("preContextIndexingRemove", this.m_context.m_workspace, binder, this.m_context.m_service);
/* 154 */     this.m_indexWriter.remove(indexResultset);
/* 155 */     PluginFilters.filter("postContextIndexingRemove", this.m_context.m_workspace, binder, this.m_context.m_service);
/*     */   }
/*     */ 
/*     */   public void clear()
/*     */     throws DataException, ServiceException
/*     */   {
/* 165 */     this.m_indexWriter.clear();
/*     */   }
/*     */ 
/*     */   public long getTermCount()
/*     */     throws DataException
/*     */   {
/* 173 */     long termCount = this.m_fieldContextIndex.getTermCount();
/* 174 */     return termCount;
/*     */   }
/*     */ 
/*     */   public Iterator getOccurrenceIterator(GramInfo gramInfo)
/*     */     throws DataException
/*     */   {
/* 184 */     return this.m_fieldContextIndex.getOccurrenceIterator(gramInfo);
/*     */   }
/*     */ 
/*     */   public GramInfo getGramInfo(String gram)
/*     */     throws DataException
/*     */   {
/* 194 */     return this.m_fieldContextIndex.getGramInfo(gram);
/*     */   }
/*     */ 
/*     */   public TermInfo getTermInfo(OccurrenceInfo occurrenceInfo)
/*     */     throws DataException
/*     */   {
/* 205 */     return this.m_fieldContextIndex.getTermInfo(occurrenceInfo);
/*     */   }
/*     */ 
/*     */   public GramParameterConstructor getGramParameterConstructor() {
/* 209 */     return this.m_fieldContextIndex.m_gramParameterConstructor;
/*     */   }
/*     */ 
/*     */   public SecurityIdentifierInfo getSecurityIdentifierInfo(OccurrenceInfo occurrenceInfo)
/*     */     throws DataException
/*     */   {
/* 216 */     TermInfo termInfo = getTermInfo(occurrenceInfo);
/* 217 */     return getSecurityIdentifierInfo(termInfo);
/*     */   }
/*     */ 
/*     */   public SecurityIdentifierInfo getSecurityIdentifierInfo(TermInfo termInfo) throws DataException {
/* 221 */     if ((termInfo == null) || (termInfo.m_identifier.equals("-1")) || (this.m_securityContextIndex == null))
/*     */     {
/* 223 */       return null;
/*     */     }
/* 225 */     SecurityIdentifierInfo securityIdentifierInfo = this.m_securityContextIndex.getSecurityIdentifierInfo(termInfo.m_identifier);
/* 226 */     if ((securityIdentifierInfo != null) && (securityIdentifierInfo.m_context == null))
/*     */     {
/* 228 */       securityIdentifierInfo.m_context = this.m_securityContextIndex.m_context;
/*     */     }
/* 230 */     return securityIdentifierInfo;
/*     */   }
/*     */ 
/*     */   public String getSecurityContextField()
/*     */   {
/* 238 */     if (this.m_securityContextIndex != null)
/*     */     {
/* 240 */       ContextInfo securityContextInfo = this.m_securityContextIndex.m_contextInfo;
/* 241 */       return securityContextInfo.m_field;
/*     */     }
/* 243 */     return null;
/*     */   }
/*     */ 
/*     */   public boolean hasSecurityContext()
/*     */   {
/* 251 */     boolean hasSecurity = this.m_securityContextIndex != null;
/* 252 */     return hasSecurity;
/*     */   }
/*     */ 
/*     */   public MetaInfo getMetaInfo()
/*     */   {
/* 260 */     return this.m_metaInfo;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg) {
/* 264 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 103126 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.autosuggest.indexer.AutoSuggestIndexHandler
 * JD-Core Version:    0.5.4
 */