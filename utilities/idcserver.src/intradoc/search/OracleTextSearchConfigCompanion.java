/*     */ package intradoc.search;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.server.SearchIndexerUtils;
/*     */ import intradoc.server.SearchLoader;
/*     */ import intradoc.shared.ActiveIndexState;
/*     */ import intradoc.shared.CommonSearchConfig;
/*     */ import intradoc.shared.IndexerCollectionData;
/*     */ import intradoc.shared.SearchOperatorParsedElements;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ 
/*     */ public class OracleTextSearchConfigCompanion extends CommonSearchConfigCompanionAdaptor
/*     */ {
/*     */   public void init(CommonSearchConfig config)
/*     */     throws ServiceException
/*     */   {
/*  32 */     this.m_textExtras = new char[][] { { 'A', 'N', 'D' }, { 'O', 'R' }, { 'N', 'O', 'T' }, { 'i', 'd', 'c', 'c', 'o', 'n', 't', 'e', 'n', 't', 't', 'r', 'u', 'e', ' ', 'N', 'O', 'T' }, { '%' }, { '\\', '_' } };
/*     */ 
/*  40 */     this.m_wildCards = "%,\\_";
/*  41 */     super.init(config);
/*  42 */     this.m_callbackFilterName = "otsQueryParserCallbackFilter";
/*     */   }
/*     */ 
/*     */   public int prepareQuery(DataBinder binder, ExecutionContext ctxt)
/*     */     throws DataException, ServiceException
/*     */   {
/*  53 */     String sortField = binder.getLocal("SortField");
/*     */ 
/*  55 */     String isTextSdata = "false";
/*  56 */     String searchEngineName = binder.getAllowMissing("SearchEngineName");
/*  57 */     if (searchEngineName == null)
/*     */     {
/*  59 */       searchEngineName = binder.getAllowMissing("SearchIndexerEngineName");
/*     */     }
/*  61 */     if (searchEngineName != null)
/*     */     {
/*  63 */       IndexerCollectionData icd = SearchLoader.getCurrentSearchableFields(searchEngineName);
/*     */ 
/*  65 */       if ((icd != null) && (sortField != null))
/*     */       {
/*  67 */         Properties props = (Properties)icd.m_fieldDesignMap.get(sortField);
/*  68 */         if (props != null)
/*     */         {
/*  70 */           isTextSdata = props.getProperty("isOptimized");
/*     */         }
/*     */       }
/*     */     }
/*  74 */     if (StringUtils.convertToBool(isTextSdata, false))
/*     */     {
/*  76 */       binder.putLocal("SortField", "sd" + sortField);
/*     */     }
/*  78 */     else if ((sortField != null) && (sortField.equalsIgnoreCase("dDocTitle")))
/*     */     {
/*  80 */       binder.putLocal("SortField", "sdDDocTitle");
/*  81 */       if (Report.m_verbose)
/*     */       {
/*  83 */         Report.trace("searchquery", "Unable to find dDocTitle as sdata fields", null);
/*     */       }
/*     */     }
/*     */ 
/*  87 */     String activeIndex = ActiveIndexState.getActiveProperty("ActiveIndex");
/*  88 */     binder.putLocal("ActiveIndex", activeIndex);
/*  89 */     return 0;
/*     */   }
/*     */ 
/*     */   public int prepareQueryText(DataBinder binder, ExecutionContext ctxt)
/*     */     throws DataException, ServiceException
/*     */   {
/*  96 */     super.prepareQueryText(binder, ctxt);
/*  97 */     String queryFormat = SearchIndexerUtils.getSearchQueryFormat(binder, ctxt);
/*  98 */     boolean isUniversal = queryFormat.equalsIgnoreCase("Universal");
/*  99 */     String queryFilter = binder.getAllowMissing("QueryFilter");
/*     */ 
/* 103 */     String isQueryFilterUsed = binder.getAllowMissing("QueryFilterUsed");
/* 104 */     if ((isQueryFilterUsed != null) && (isQueryFilterUsed.equalsIgnoreCase("true")))
/*     */     {
/* 106 */       String queryTextBeforeQueryFilter = binder.getAllowMissing("QueryTextBeforeQueryFilter");
/* 107 */       if (queryTextBeforeQueryFilter != null)
/*     */       {
/* 109 */         binder.putLocal("QueryText", queryTextBeforeQueryFilter);
/* 110 */         binder.putLocal("QueryFilterUsed", "false");
/* 111 */         binder.removeLocal("QueryTextBeforeQueryFilter");
/*     */       }
/*     */       else
/*     */       {
/* 115 */         binder.putLocal("QueryText", "");
/*     */       }
/*     */     }
/* 118 */     if ((queryFilter != null) && (queryFilter.trim().length() != 0))
/*     */     {
/* 120 */       String queryText = binder.get("QueryText");
/* 121 */       if ((isQueryFilterUsed == null) || (!isQueryFilterUsed.equalsIgnoreCase("true")))
/*     */       {
/* 123 */         binder.putLocal("QueryTextBeforeQueryFilter", queryText);
/* 124 */         binder.putLocal("QueryFilterUsed", "true");
/*     */       }
/* 126 */       IdcStringBuilder builder = new IdcStringBuilder();
/* 127 */       if ((queryText != null) && (queryText.trim().length() != 0))
/*     */       {
/* 129 */         builder.append('(');
/* 130 */         builder.append(queryText);
/* 131 */         builder.append(") ");
/* 132 */         if (isUniversal)
/*     */         {
/* 134 */           builder.append(" <AND> ");
/*     */         }
/*     */         else
/*     */         {
/* 138 */           this.m_config.getParsedElements("and", this.m_config.getCurrentEngineName()).appendElements(builder, null, null);
/*     */         }
/* 140 */         builder.append(' ');
/*     */       }
/* 142 */       builder.append(queryFilter);
/* 143 */       binder.putLocal("QueryText", builder.toString());
/* 144 */       binder.putLocal("OriginalQueryText", builder.toString());
/*     */     }
/* 146 */     if (Report.m_verbose)
/*     */     {
/* 148 */       Report.trace("searchquery", "preparedQueryText: " + binder.getLocal("QueryText"), null);
/*     */     }
/* 150 */     return 0;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 155 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 92962 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.search.OracleTextSearchConfigCompanion
 * JD-Core Version:    0.5.4
 */