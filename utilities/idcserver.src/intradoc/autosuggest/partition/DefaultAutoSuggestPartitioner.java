/*     */ package intradoc.autosuggest.partition;
/*     */ 
/*     */ import intradoc.autosuggest.AutoSuggestConstants;
/*     */ import intradoc.autosuggest.AutoSuggestContext;
/*     */ import intradoc.autosuggest.records.ContextInfo;
/*     */ import intradoc.common.ClassHelperUtils;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.QueryUtils;
/*     */ import intradoc.server.Service;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class DefaultAutoSuggestPartitioner
/*     */   implements AutoSuggestPartitioner
/*     */ {
/*     */   public Map<String, DataResultSet> partition(String mode, AutoSuggestContext context, ContextInfo contextInfo, DataResultSet inputResultSet)
/*     */     throws ServiceException, DataException
/*     */   {
/*  59 */     Map partitionedMap = new HashMap();
/*  60 */     String partitionerIdentifier = contextInfo.getPartitioner(mode);
/*  61 */     if ((partitionerIdentifier == null) || (partitionerIdentifier.length() == 0))
/*     */     {
/*  63 */       Report.trace("autosuggest", "No partitioning defined for context -" + context.m_contextKey, null);
/*  64 */       partitionedMap.put("Default", inputResultSet);
/*  65 */       return partitionedMap;
/*     */     }
/*     */ 
/*  70 */     List partitionerSplit = StringUtils.makeListFromSequence(partitionerIdentifier, '=', '=', 0);
/*  71 */     if (partitionerSplit.size() == 2)
/*     */     {
/*  73 */       String partitionerType = (String)partitionerSplit.get(0);
/*  74 */       String partitioner = (String)partitionerSplit.get(1);
/*     */ 
/*  78 */       if (partitionerType.equalsIgnoreCase("class"))
/*     */       {
/*  80 */         Report.trace("autosuggest", "Partitioning using class " + partitioner + "for context -" + context.m_contextKey, null);
/*  81 */         Class partitionerClass = ClassHelperUtils.createClass(partitioner);
/*  82 */         AutoSuggestPartitioner partitionerObject = (AutoSuggestPartitioner)ClassHelperUtils.createInstance(partitionerClass);
/*     */ 
/*  84 */         partitionedMap = partitionerObject.partition(mode, context, contextInfo, inputResultSet);
/*     */       }
/*     */ 
/*  90 */       if (partitionerType.equalsIgnoreCase("datasource"))
/*     */       {
/*  92 */         Report.trace("autosuggest", "Partitioning using data source " + partitioner + "for context -" + context.m_contextKey, null);
/*  93 */         DataBinder binder = context.m_service.getBinder();
/*  94 */         binder.putLocal("resultName", "PartitionResultSet");
/*  95 */         binder.putLocal("dataSource", partitioner);
/*     */ 
/*  97 */         DataResultSet partitionRset = null;
/*  98 */         IdcStringBuilder identifiers = new IdcStringBuilder();
/*  99 */         int identifiersCount = 0;
/* 100 */         FieldInfo identifierFieldInfo = new FieldInfo();
/* 101 */         inputResultSet.getFieldInfo(AutoSuggestConstants.FIELD_AUTOSUGGEST_IDENTIFIER, identifierFieldInfo);
/* 102 */         for (inputResultSet.first(); inputResultSet.isRowPresent(); inputResultSet.next())
/*     */         {
/* 104 */           if (identifiers.length() > 0)
/*     */           {
/* 106 */             identifiers.append(",");
/*     */           }
/* 108 */           QueryUtils.appendParam(identifiers, identifierFieldInfo.m_type, inputResultSet.getStringValueByName(AutoSuggestConstants.FIELD_AUTOSUGGEST_IDENTIFIER));
/* 109 */           ++identifiersCount;
/* 110 */           if (identifiersCount < 200)
/*     */             continue;
/* 112 */           binder.putLocal("identifiers", identifiers.toString());
/* 113 */           context.m_service.createResultSetSQL();
/* 114 */           DataResultSet drset = (DataResultSet)binder.getResultSet("PartitionResultSet");
/* 115 */           if (partitionRset == null)
/*     */           {
/* 117 */             partitionRset = drset;
/*     */           }
/*     */           else
/*     */           {
/* 121 */             partitionRset.mergeWithFlags(AutoSuggestConstants.FIELD_AUTOSUGGEST_IDENTIFIER, drset, 16, 0);
/*     */           }
/* 123 */           identifiers = new IdcStringBuilder();
/* 124 */           identifiersCount = 0;
/*     */         }
/*     */ 
/* 127 */         if (identifiers.length() > 0)
/*     */         {
/* 129 */           binder.putLocal("identifiers", identifiers.toString());
/* 130 */           context.m_service.createResultSetSQL();
/* 131 */           DataResultSet drset = (DataResultSet)binder.getResultSet("PartitionResultSet");
/* 132 */           if (partitionRset == null)
/*     */           {
/* 134 */             partitionRset = drset;
/*     */           }
/*     */           else
/*     */           {
/* 138 */             partitionRset.mergeWithFlags(AutoSuggestConstants.FIELD_AUTOSUGGEST_IDENTIFIER, drset, 16, 0);
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/* 144 */         for (inputResultSet.first(); inputResultSet.isRowPresent(); inputResultSet.next())
/*     */         {
/* 146 */           Vector inputRow = inputResultSet.getCurrentRowValues();
/* 147 */           String identifier = inputResultSet.getStringValueByName(AutoSuggestConstants.FIELD_AUTOSUGGEST_IDENTIFIER);
/* 148 */           Vector partitionRow = partitionRset.findRow(0, identifier);
/* 149 */           if (partitionRow == null)
/*     */             continue;
/* 151 */           String partition = (String)partitionRow.get(1);
/*     */ 
/* 153 */           DataResultSet inputPartitionedRset = (DataResultSet)partitionedMap.get(partition);
/* 154 */           if (inputPartitionedRset == null)
/*     */           {
/* 156 */             inputPartitionedRset = new DataResultSet();
/* 157 */             inputPartitionedRset.copyFieldInfo(inputResultSet);
/* 158 */             partitionedMap.put(partition, inputPartitionedRset);
/*     */           }
/* 160 */           inputPartitionedRset.addRow(inputRow);
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 165 */     return partitionedMap;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg) {
/* 169 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 102609 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.autosuggest.partition.DefaultAutoSuggestPartitioner
 * JD-Core Version:    0.5.4
 */