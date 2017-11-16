/*     */ package intradoc.autosuggest.indexer;
/*     */ 
/*     */ import intradoc.autosuggest.AutoSuggestConstants;
/*     */ import intradoc.autosuggest.AutoSuggestContext;
/*     */ import intradoc.autosuggest.datastore.AutoSuggestTermsQueue;
/*     */ import intradoc.autosuggest.datastore.ContextInfoStorage;
/*     */ import intradoc.autosuggest.datastore.GramStorage;
/*     */ import intradoc.autosuggest.datastore.InverseTermStorage;
/*     */ import intradoc.autosuggest.datastore.OccurrenceStorage;
/*     */ import intradoc.autosuggest.datastore.TermStorage;
/*     */ import intradoc.autosuggest.records.ContextInfo;
/*     */ import intradoc.autosuggest.records.GramInfo;
/*     */ import intradoc.autosuggest.records.OccurrenceInfo;
/*     */ import intradoc.autosuggest.records.TermGramParameters;
/*     */ import intradoc.autosuggest.records.TermInfo;
/*     */ import intradoc.autosuggest.utils.AutoSuggestUtils;
/*     */ import intradoc.autosuggest.utils.GramParameterConstructor;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Set;
/*     */ 
/*     */ public class FieldContextIndex
/*     */ {
/*     */   public AutoSuggestContext m_context;
/*     */   public ContextInfo m_contextInfo;
/*     */   public GramParameterConstructor m_gramParameterConstructor;
/*     */   public AutoSuggestTermsQueue m_autoSuggestTermsQueue;
/*     */   public FieldContextIndexWriter m_indexWriter;
/*     */   public GramStorage m_gramStorage;
/*     */   public TermStorage m_termStorage;
/*     */   public InverseTermStorage m_inverseTermStorage;
/*     */   public OccurrenceStorage m_occurrenceStorage;
/*     */ 
/*     */   public void init(AutoSuggestContext context, ContextInfo contextInfo)
/*     */     throws DataException, ServiceException
/*     */   {
/*  51 */     this.m_context = context;
/*  52 */     this.m_contextInfo = contextInfo;
/*  53 */     this.m_gramParameterConstructor = new GramParameterConstructor();
/*  54 */     this.m_indexWriter = new FieldContextIndexWriter();
/*  55 */     this.m_indexWriter.init(context, contextInfo);
/*  56 */     this.m_gramStorage = new GramStorage(this.m_context);
/*  57 */     this.m_termStorage = new TermStorage(this.m_context);
/*  58 */     this.m_inverseTermStorage = new InverseTermStorage(this.m_context);
/*  59 */     this.m_occurrenceStorage = new OccurrenceStorage(this.m_context);
/*  60 */     this.m_autoSuggestTermsQueue = new AutoSuggestTermsQueue(this.m_context);
/*     */   }
/*     */ 
/*     */   public void enqueueAddition(String term, String identifier, Map<String, String> extraParameters) throws DataException {
/*  64 */     this.m_autoSuggestTermsQueue.add(term, identifier, extraParameters);
/*     */   }
/*     */ 
/*     */   public void enqueueDeletion(String term, String identifier, Map<String, String> extraParameters) throws DataException {
/*  68 */     this.m_autoSuggestTermsQueue.delete(term, identifier, extraParameters);
/*     */   }
/*     */ 
/*     */   public long getTermCount()
/*     */     throws DataException
/*     */   {
/*  78 */     long termCount = this.m_termStorage.getCount();
/*  79 */     return termCount;
/*     */   }
/*     */ 
/*     */   public Iterator getOccurrenceIterator(GramInfo gramInfo)
/*     */     throws DataException
/*     */   {
/*  89 */     if (gramInfo == null)
/*     */     {
/*  91 */       return null;
/*     */     }
/*  93 */     Iterator occurrenceIterator = new OccurrenceIterator(gramInfo);
/*  94 */     return occurrenceIterator;
/*     */   }
/*     */ 
/*     */   public GramInfo getGramInfo(String gram)
/*     */     throws DataException
/*     */   {
/* 104 */     GramInfo gramInfo = this.m_gramStorage.get(gram);
/* 105 */     return gramInfo;
/*     */   }
/*     */ 
/*     */   public TermInfo getTermInfo(OccurrenceInfo occurrenceInfo)
/*     */     throws DataException
/*     */   {
/* 116 */     return this.m_termStorage.get(occurrenceInfo.m_termId);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 480 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 104206 $";
/*     */   }
/*     */ 
/*     */   public class FieldContextIndexWriter
/*     */     implements ContextIndexWriter
/*     */   {
/*     */     public FieldContextIndexWriter()
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
/* 267 */       FieldInfo identifierFieldInfo = new FieldInfo();
/* 268 */       indexResultset.getFieldInfo(AutoSuggestConstants.FIELD_AUTOSUGGEST_IDENTIFIER, identifierFieldInfo);
/* 269 */       FieldInfo fieldInfo = new FieldInfo();
/* 270 */       String fieldName = ContextInfoStorage.getField(FieldContextIndex.this.m_context.m_contextKey);
/* 271 */       indexResultset.getFieldInfo(fieldName, fieldInfo);
/* 272 */       if (fieldInfo == null)
/*     */       {
/* 274 */         return;
/*     */       }
/* 276 */       for (indexResultset.first(); indexResultset.isRowPresent(); indexResultset.next())
/*     */       {
/* 278 */         String fieldValue = indexResultset.getStringValue(fieldInfo.m_index);
/* 279 */         String identifier = ((identifierFieldInfo == null) || (identifierFieldInfo.m_index == -1)) ? "-1" : indexResultset.getStringValue(identifierFieldInfo.m_index);
/*     */ 
/* 281 */         Map currentRowMap = indexResultset.getCurrentRowMap();
/* 282 */         currentRowMap.remove(AutoSuggestConstants.FIELD_AUTOSUGGEST_IDENTIFIER);
/* 283 */         currentRowMap.remove(fieldName);
/* 284 */         indexTerm(fieldValue, identifier, currentRowMap);
/*     */       }
/*     */     }
/*     */ 
/*     */     public void indexQueues()
/*     */       throws DataException, ServiceException
/*     */     {
/* 293 */       Iterator additionIterator = FieldContextIndex.this.m_autoSuggestTermsQueue.additionRemoveIterator();
/* 294 */       while (additionIterator.hasNext())
/*     */       {
/* 296 */         TermInfo termInfo = (TermInfo)additionIterator.next();
/* 297 */         indexTerm(termInfo.m_actualTerm, termInfo.m_identifier, termInfo.m_extraParameters);
/*     */       }
/* 299 */       Iterator deletionIterator = FieldContextIndex.this.m_autoSuggestTermsQueue.deletionRemoveIterator();
/* 300 */       while (deletionIterator.hasNext())
/*     */       {
/* 302 */         TermInfo termInfo = (TermInfo)deletionIterator.next();
/* 303 */         removeTerm(termInfo);
/*     */       }
/* 305 */       FieldContextIndex.this.m_autoSuggestTermsQueue.commit();
/*     */     }
/*     */ 
/*     */     public void remove(DataResultSet indexResultset)
/*     */       throws DataException, ServiceException
/*     */     {
/* 313 */       FieldInfo identifierFieldInfo = new FieldInfo();
/* 314 */       indexResultset.getFieldInfo(AutoSuggestConstants.FIELD_AUTOSUGGEST_IDENTIFIER, identifierFieldInfo);
/* 315 */       if ((identifierFieldInfo != null) && (identifierFieldInfo.m_index != -1))
/*     */       {
/* 317 */         for (indexResultset.first(); indexResultset.isRowPresent(); indexResultset.next())
/*     */         {
/* 319 */           String identifier = indexResultset.getStringValue(identifierFieldInfo.m_index);
/* 320 */           removeUsingIdentifier(identifier);
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 327 */       String fieldName = ContextInfoStorage.getField(FieldContextIndex.this.m_context.m_contextKey);
/* 328 */       FieldInfo fieldInfo = new FieldInfo();
/* 329 */       indexResultset.getFieldInfo(fieldName, fieldInfo);
/* 330 */       if ((fieldInfo == null) || (fieldInfo.m_index == -1))
/*     */         return;
/* 332 */       for (indexResultset.first(); indexResultset.isRowPresent(); indexResultset.next())
/*     */       {
/* 334 */         String term = indexResultset.getStringValue(fieldInfo.m_index);
/* 335 */         removeTerm(term);
/*     */       }
/*     */     }
/*     */ 
/*     */     public void indexTerm(String term, String identifier, Map<String, String> extraParameters)
/*     */       throws DataException, ServiceException
/*     */     {
/* 345 */       TermInfo termInfo = insertTerm(term, identifier, extraParameters);
/* 346 */       indexTerm(termInfo);
/*     */     }
/*     */ 
/*     */     public void indexTerm(TermInfo termInfo) throws DataException, ServiceException
/*     */     {
/* 351 */       Map gramMap = AutoSuggestUtils.contructGramParameters(termInfo.m_indexedTerm);
/* 352 */       Iterator gramIterator = gramMap.keySet().iterator();
/* 353 */       while (gramIterator.hasNext())
/*     */       {
/* 355 */         String gram = (String)gramIterator.next();
/* 356 */         TermGramParameters gramParams = (TermGramParameters)gramMap.get(gram);
/* 357 */         GramInfo gramInfo = FieldContextIndex.this.m_gramStorage.get(gram);
/* 358 */         if (gramInfo == null)
/*     */         {
/* 360 */           gramInfo = insertGram(gram);
/*     */         }
/* 362 */         gramInfo.m_globalFreq += 1L;
/*     */ 
/* 366 */         String occurenceListId = (String)gramInfo.m_occurrenceListIds.get(gramInfo.m_occurrenceListIds.size() - 1);
/* 367 */         List occurenceInfoList = FieldContextIndex.this.m_occurrenceStorage.get(occurenceListId);
/* 368 */         if ((occurenceInfoList != null) && (occurenceInfoList.size() >= AutoSuggestConstants.OCCURRENCE_BUCKET_SIZE))
/*     */         {
/* 371 */           if (Report.m_verbose)
/*     */           {
/* 373 */             Report.trace("autosuggest", "Occurrence bucket is full " + occurenceListId, null);
/*     */           }
/* 375 */           occurenceListId = StringUtils.createGUIDEx(32, 0, "");
/* 376 */           gramInfo.m_occurrenceListIds.add(occurenceListId);
/* 377 */           if (Report.m_verbose)
/*     */           {
/* 379 */             Report.trace("autosuggest", "Occurrence list id " + occurenceListId + " added to gram " + gram, null);
/*     */           }
/*     */         }
/* 382 */         FieldContextIndex.this.m_gramStorage.update(gram, gramInfo);
/* 383 */         insertOccurrence(occurenceListId, gramInfo.m_gram, termInfo.getKey(), gramParams.m_position, gramParams.m_frequency);
/*     */       }
/*     */     }
/*     */ 
/*     */     public TermInfo insertTerm(String term, String identifier, Map<String, String> extraParameters)
/*     */       throws DataException
/*     */     {
/* 391 */       if (identifier != "-1")
/*     */       {
/* 393 */         removeUsingIdentifier(identifier);
/*     */       }
/* 395 */       TermInfo termInfo = new TermInfo(FieldContextIndex.this.m_context);
/* 396 */       termInfo.init(term, identifier, extraParameters);
/* 397 */       FieldContextIndex.this.m_termStorage.put(termInfo.getKey(), termInfo);
/* 398 */       FieldContextIndex.this.m_inverseTermStorage.put(termInfo.m_identifier, termInfo);
/*     */ 
/* 400 */       if (Report.m_verbose)
/*     */       {
/* 402 */         Report.trace("autosuggest", "Indexed TermInfo " + termInfo.toString(), null);
/*     */       }
/* 404 */       return termInfo;
/*     */     }
/*     */ 
/*     */     public void insertOccurrence(String occurrenceId, String gram, String term, short position, short frequency)
/*     */       throws DataException
/*     */     {
/* 410 */       OccurrenceInfo occurrenceInfo = new OccurrenceInfo(FieldContextIndex.this.m_context);
/* 411 */       occurrenceInfo.init(gram, term, position, frequency);
/* 412 */       FieldContextIndex.this.m_occurrenceStorage.put(occurrenceId, occurrenceInfo);
/* 413 */       if (!Report.m_verbose)
/*     */         return;
/* 415 */       Report.trace("autosuggest", "Indexed Occurence " + occurrenceInfo.toString(), null);
/*     */     }
/*     */ 
/*     */     public GramInfo insertGram(String gram) throws DataException
/*     */     {
/* 420 */       GramInfo gramInfo = new GramInfo(FieldContextIndex.this.m_context);
/* 421 */       String occurrenceListId = StringUtils.createGUIDEx(32, 0, "");
/* 422 */       if (Report.m_verbose)
/*     */       {
/* 424 */         Report.trace("autosuggest", "Inserting gram " + gram + " with occurrence list id " + occurrenceListId, null);
/*     */       }
/* 426 */       List occurrenceListIds = new ArrayList();
/* 427 */       occurrenceListIds.add(occurrenceListId);
/* 428 */       gramInfo.init(gram, 0L, occurrenceListIds);
/* 429 */       FieldContextIndex.this.m_gramStorage.put(gram, gramInfo);
/* 430 */       if (Report.m_verbose)
/*     */       {
/* 432 */         Report.trace("autosuggest", "Indexing GramInfo " + gramInfo.toString(), null);
/*     */       }
/* 434 */       return gramInfo;
/*     */     }
/*     */ 
/*     */     public void removeUsingIdentifier(String identifier) throws DataException {
/* 438 */       TermInfo termInfo = FieldContextIndex.this.m_inverseTermStorage.get(identifier);
/* 439 */       if (termInfo != null)
/*     */       {
/* 441 */         removeTerm(termInfo);
/*     */       }
/* 443 */       FieldContextIndex.this.m_inverseTermStorage.remove(identifier);
/*     */     }
/*     */ 
/*     */     public void removeTerm(String term)
/*     */       throws DataException
/*     */     {
/* 454 */       TermInfo termInfo = new TermInfo(FieldContextIndex.this.m_context);
/* 455 */       termInfo.init(term, "-1", null);
/* 456 */       removeTerm(termInfo);
/*     */     }
/*     */ 
/*     */     public void removeTerm(TermInfo termInfo) throws DataException {
/* 460 */       Report.trace("autosuggest", "Removing Term " + termInfo.m_indexedTerm, null);
/* 461 */       FieldContextIndex.this.m_termStorage.remove(termInfo.getKey());
/*     */     }
/*     */ 
/*     */     public void clear()
/*     */       throws DataException, ServiceException
/*     */     {
/* 470 */       Report.trace("autosuggest", "Cleaning the field context index for " + FieldContextIndex.this.m_contextInfo.getKey(), null);
/* 471 */       FieldContextIndex.this.m_gramStorage.clear();
/* 472 */       FieldContextIndex.this.m_termStorage.clear();
/* 473 */       FieldContextIndex.this.m_inverseTermStorage.clear();
/* 474 */       FieldContextIndex.this.m_occurrenceStorage.clear();
/* 475 */       FieldContextIndex.this.m_autoSuggestTermsQueue.clear();
/*     */     }
/*     */   }
/*     */ 
/*     */   public class OccurrenceIterator
/*     */     implements Iterator<OccurrenceInfo>
/*     */   {
/*     */     public GramInfo m_gramInfo;
/* 125 */     public int m_nextListNo = 0;
/*     */     protected List<OccurrenceInfo> m_currentOccurrenceInfoList;
/*     */     protected Iterator<OccurrenceInfo> m_currentOccurenceListIterator;
/*     */ 
/*     */     public OccurrenceIterator(GramInfo gramInfo)
/*     */       throws DataException
/*     */     {
/* 130 */       this.m_gramInfo = gramInfo;
/* 131 */       if (Report.m_verbose)
/*     */       {
/* 133 */         Report.trace("autosuggest", "OccurrenceIterator - " + this.m_gramInfo.m_gram + " - Occurrence List IDs - " + this.m_gramInfo.m_occurrenceListIds, null);
/*     */       }
/* 135 */       getNextListIterator();
/*     */     }
/*     */ 
/*     */     public boolean hasNext()
/*     */     {
/* 143 */       if (this.m_currentOccurenceListIterator == null)
/*     */       {
/* 145 */         return false;
/*     */       }
/* 147 */       if (hasNextInCurrentOccurrenceList())
/*     */       {
/* 149 */         return true;
/*     */       }
/*     */ 
/* 154 */       if (this.m_gramInfo.m_occurrenceListIds.size() > this.m_nextListNo)
/*     */       {
/*     */         try
/*     */         {
/* 158 */           getNextListIterator();
/* 159 */           if (hasNextInCurrentOccurrenceList())
/*     */           {
/* 161 */             return true;
/*     */           }
/*     */         }
/*     */         catch (DataException e)
/*     */         {
/* 166 */           Report.error("autosuggest", "Error while iterating over occurrences for " + this.m_gramInfo.toString(), e);
/*     */         }
/*     */       }
/* 169 */       return false;
/*     */     }
/*     */ 
/*     */     public OccurrenceInfo next()
/*     */     {
/* 176 */       if (hasNextInCurrentOccurrenceList())
/*     */       {
/* 178 */         return nextInCurrentOccurrenceList();
/*     */       }
/* 180 */       return null;
/*     */     }
/*     */ 
/*     */     public void remove() {
/* 184 */       throw new UnsupportedOperationException();
/*     */     }
/*     */ 
/*     */     public void getNextListIterator()
/*     */       throws DataException
/*     */     {
/* 193 */       String occurenceListId = (String)this.m_gramInfo.m_occurrenceListIds.get(this.m_nextListNo);
/* 194 */       if (Report.m_verbose)
/*     */       {
/* 196 */         Report.trace("autosuggest", "OccurrenceIterator - " + this.m_gramInfo.m_gram + " - getting current occurrence list with m_nextListNo as - " + this.m_nextListNo, null);
/* 197 */         Report.trace("autosuggest", "OccurrenceIterator - " + this.m_gramInfo.m_gram + " - getting current occurrence list id - " + occurenceListId, null);
/*     */       }
/* 199 */       List occurrenceInfoList = FieldContextIndex.this.m_occurrenceStorage.get(occurenceListId);
/* 200 */       if (occurrenceInfoList != null)
/*     */       {
/* 203 */         this.m_currentOccurrenceInfoList = new ArrayList();
/* 204 */         for (OccurrenceInfo occurrenceInfo : occurrenceInfoList)
/*     */         {
/* 206 */           this.m_currentOccurrenceInfoList.add(occurrenceInfo.clone());
/*     */         }
/* 208 */         getCurrentOccurrenceListIterator();
/*     */       }
/*     */       else
/*     */       {
/* 212 */         this.m_currentOccurenceListIterator = null;
/*     */       }
/* 214 */       this.m_nextListNo += 1;
/* 215 */       if (!Report.m_verbose)
/*     */         return;
/* 217 */       Report.trace("autosuggest", "OccurrenceIterator - " + this.m_gramInfo.m_gram + " - Setting m_nextListNo " + this.m_nextListNo, null);
/*     */     }
/*     */ 
/*     */     public void getCurrentOccurrenceListIterator()
/*     */     {
/* 223 */       synchronized (this.m_currentOccurrenceInfoList)
/*     */       {
/* 225 */         this.m_currentOccurenceListIterator = this.m_currentOccurrenceInfoList.iterator();
/*     */       }
/*     */     }
/*     */ 
/*     */     public OccurrenceInfo nextInCurrentOccurrenceList() {
/* 230 */       if (this.m_currentOccurenceListIterator == null)
/*     */       {
/* 232 */         return null;
/*     */       }
/* 234 */       OccurrenceInfo occurrenceInfo = null;
/* 235 */       synchronized (this.m_currentOccurrenceInfoList)
/*     */       {
/* 237 */         occurrenceInfo = (OccurrenceInfo)this.m_currentOccurenceListIterator.next();
/*     */       }
/* 239 */       return occurrenceInfo;
/*     */     }
/*     */ 
/*     */     public boolean hasNextInCurrentOccurrenceList() {
/* 243 */       if (this.m_currentOccurenceListIterator == null)
/*     */       {
/* 245 */         return false;
/*     */       }
/* 247 */       boolean hasNext = false;
/* 248 */       synchronized (this.m_currentOccurrenceInfoList)
/*     */       {
/* 250 */         hasNext = this.m_currentOccurenceListIterator.hasNext();
/*     */       }
/* 252 */       return hasNext;
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.autosuggest.indexer.FieldContextIndex
 * JD-Core Version:    0.5.4
 */