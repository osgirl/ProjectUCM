/*     */ package intradoc.autosuggest.utils;
/*     */ 
/*     */ import intradoc.autosuggest.AutoSuggestContext;
/*     */ import intradoc.autosuggest.indexer.AutoSuggestIndexHandler;
/*     */ import intradoc.autosuggest.records.GramInfo;
/*     */ import intradoc.autosuggest.records.MetaInfo;
/*     */ import intradoc.autosuggest.records.TermGramParameters;
/*     */ import intradoc.autosuggest.records.TermInfo;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataException;
/*     */ import java.util.Iterator;
/*     */ import java.util.Map;
/*     */ import java.util.Set;
/*     */ 
/*     */ public class Scorer
/*     */ {
/*     */   public AutoSuggestContext m_context;
/*     */   public AutoSuggestIndexHandler m_indexHandler;
/*     */ 
/*     */   public Scorer(AutoSuggestContext context, MetaInfo metaInfo)
/*     */     throws DataException, ServiceException
/*     */   {
/*  44 */     this.m_context = context;
/*  45 */     this.m_indexHandler = new AutoSuggestIndexHandler(context, metaInfo);
/*     */   }
/*     */ 
/*     */   public double score(String query, TermInfo candidateTermInfo)
/*     */     throws DataException
/*     */   {
/*  58 */     double candidateScore = 0.0D;
/*  59 */     Map queryGramMap = this.m_indexHandler.getGramParameterConstructor().contructGramParameters(query);
/*  60 */     candidateScore = score(queryGramMap, candidateTermInfo);
/*  61 */     return candidateScore;
/*     */   }
/*     */ 
/*     */   public double score(Map<String, TermGramParameters> queryGramMap, TermInfo candidateTermInfo)
/*     */     throws DataException
/*     */   {
/*  73 */     double candidateScore = 0.0D;
/*  74 */     Map candidateGramMap = this.m_indexHandler.getGramParameterConstructor().contructGramParameters(candidateTermInfo.m_indexedTerm);
/*  75 */     candidateScore = score(queryGramMap, candidateGramMap);
/*  76 */     return candidateScore;
/*     */   }
/*     */ 
/*     */   public double score(Map<String, TermGramParameters> queryGramMap, Map<String, TermGramParameters> candidateGramMap)
/*     */     throws DataException
/*     */   {
/*  99 */     double candidateScore = 0.0D;
/* 100 */     long termCount = this.m_indexHandler.getTermCount();
/* 101 */     Iterator queryGramIterator = queryGramMap.keySet().iterator();
/* 102 */     while (queryGramIterator.hasNext())
/*     */     {
/* 104 */       String queryGram = (String)queryGramIterator.next();
/* 105 */       GramInfo gramInfo = this.m_indexHandler.getGramInfo(queryGram);
/* 106 */       if (gramInfo != null)
/*     */       {
/* 108 */         TermGramParameters queryParams = (TermGramParameters)queryGramMap.get(queryGram);
/* 109 */         TermGramParameters candidateParams = (TermGramParameters)candidateGramMap.get(queryGram);
/* 110 */         if (candidateParams != null)
/*     */         {
/* 112 */           candidateParams.prepare(gramInfo, termCount);
/* 113 */           queryParams.prepare(gramInfo, termCount);
/* 114 */           if ((queryParams.m_position == 0) && (queryParams.m_position == candidateParams.m_position))
/*     */           {
/* 116 */             candidateScore += queryParams.m_weight * candidateParams.m_weight * 10.0D;
/*     */           }
/*     */           else
/*     */           {
/* 120 */             candidateScore += queryParams.m_weight * candidateParams.m_weight;
/*     */           }
/*     */         }
/*     */       }
/*     */     }
/* 125 */     return candidateScore;
/*     */   }
/*     */ 
/*     */   public double proximityIndex(String source, String destination)
/*     */   {
/* 143 */     int sourceLength = source.length();
/* 144 */     int destinationLength = destination.length();
/* 145 */     float[] previousCostArray = new float[sourceLength + 1];
/* 146 */     float[] currentCostArray = new float[sourceLength + 1];
/*     */ 
/* 148 */     if ((sourceLength == 0) || (destinationLength == 0))
/*     */     {
/* 150 */       if (sourceLength == destinationLength)
/*     */       {
/* 152 */         return 1.0D;
/*     */       }
/* 154 */       return 0.0D;
/*     */     }
/* 156 */     for (int sourceIndex = 0; sourceIndex <= sourceLength; ++sourceIndex)
/*     */     {
/* 158 */       previousCostArray[sourceIndex] = sourceIndex;
/*     */     }
/* 160 */     for (int destIndex = 1; destIndex <= destinationLength; ++destIndex)
/*     */     {
/* 162 */       char destCharacter = destination.charAt(destIndex - 1);
/* 163 */       currentCostArray[0] = destIndex;
/* 164 */       for (int sourceIndex = 1; sourceIndex <= sourceLength; ++sourceIndex)
/*     */       {
/* 166 */         float cost = (source.charAt(sourceIndex - 1) == destCharacter) ? 0 : 2;
/* 167 */         currentCostArray[sourceIndex] = Math.min(Math.min(currentCostArray[(sourceIndex - 1)] + 1.0F, previousCostArray[sourceIndex] + 1.0F), previousCostArray[(sourceIndex - 1)] + cost);
/*     */       }
/*     */ 
/* 172 */       float[] swapSpace = previousCostArray;
/* 173 */       previousCostArray = currentCostArray;
/* 174 */       currentCostArray = swapSpace;
/*     */     }
/* 176 */     return 1.0F - previousCostArray[sourceLength] / Math.max(destination.length(), source.length());
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 181 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 105661 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.autosuggest.utils.Scorer
 * JD-Core Version:    0.5.4
 */