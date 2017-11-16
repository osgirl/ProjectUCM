/*     */ package intradoc.server.publish;
/*     */ 
/*     */ import intradoc.common.DAGHasCycleException;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.IdcTimer;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.LoggingUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.SortUtils;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.server.PageMerger;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Arrays;
/*     */ import java.util.HashMap;
/*     */ import java.util.HashSet;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Set;
/*     */ 
/*     */ public class WebFeaturesPublisher
/*     */ {
/*     */   public WebPublishState m_state;
/*     */   public DataResultSet m_publishedFeaturesTable;
/*     */   public FieldInfo[] m_publishedFeaturesTableFields;
/*     */ 
/*     */   public WebFeaturesPublisher(WebPublishState state)
/*     */   {
/* 117 */     this.m_state = state;
/*     */   }
/*     */ 
/*     */   public void computeWebFeatures()
/*     */     throws DataException
/*     */   {
/* 169 */     IdcTimer timer = this.m_state.m_timer;
/* 170 */     if (timer != null)
/*     */     {
/* 172 */       timer.start("compute web features");
/* 173 */       timer.start("compute provided features");
/*     */     }
/* 175 */     computeProvidedFeatures();
/* 176 */     if (timer != null)
/*     */     {
/* 178 */       timer.stop(this.m_state.m_timerFlags, new Object[0]);
/* 179 */       timer.start("compute dependencies");
/*     */     }
/*     */ 
/* 182 */     computeFeatureDependsMaps();
/* 183 */     if (timer != null)
/*     */     {
/* 185 */       timer.stop(this.m_state.m_timerFlags, new Object[0]);
/* 186 */       timer.start("prune dependencies");
/*     */     }
/*     */ 
/* 189 */     if ((SystemUtils.m_verbose) && (this.m_state.m_doTrace))
/*     */     {
/* 191 */       outputDependencyTree();
/*     */     }
/* 193 */     computeFeaturesRequirements();
/* 194 */     if (timer == null)
/*     */       return;
/* 196 */     timer.stop(this.m_state.m_timerFlags, new Object[0]);
/* 197 */     timer.stop(this.m_state.m_timerFlags, new Object[0]);
/*     */   }
/*     */ 
/*     */   protected void computeProvidedFeatures()
/*     */     throws DataException
/*     */   {
/* 210 */     String publishedFeaturesTableName = "PublishedFeatures";
/* 211 */     DataResultSet publishedFeaturesTable = SharedObjects.getTable(publishedFeaturesTableName);
/* 212 */     if (null == publishedFeaturesTable)
/*     */     {
/* 214 */       String msg = LocaleUtils.encodeMessage("csResourceTableUndefined", null, publishedFeaturesTableName);
/* 215 */       throw new DataException(msg);
/*     */     }
/* 217 */     this.m_publishedFeaturesTable = publishedFeaturesTable;
/* 218 */     String[] fieldNames = { "class", "providedFeatures", "requiredFeatures", "optionalFeatures" };
/* 219 */     this.m_publishedFeaturesTableFields = ResultSetUtils.createInfoList(publishedFeaturesTable, fieldNames, true);
/* 220 */     int classIndex = this.m_publishedFeaturesTableFields[0].m_index;
/* 221 */     int providedIndex = this.m_publishedFeaturesTableFields[1].m_index;
/*     */ 
/* 223 */     Map providedPublishedFeatures = new HashMap();
/* 224 */     for (publishedFeaturesTable.first(); publishedFeaturesTable.isRowPresent(); publishedFeaturesTable.next())
/*     */     {
/* 226 */       String className = publishedFeaturesTable.getStringValue(classIndex);
/* 227 */       className = PublishedResourceUtils.toClassname(className);
/* 228 */       PublishedResourceContainer.Class resourceClass = (PublishedResourceContainer.Class)this.m_state.m_publishedClasses.get(className);
/* 229 */       if (resourceClass == null)
/*     */       {
/* 231 */         resourceClass = new PublishedResourceContainer.Class(className);
/* 232 */         this.m_state.m_publishedClasses.put(className, resourceClass);
/* 233 */         resourceClass.m_bundle = this.m_state.m_dynamicPublisher.findBundleForClass(className);
/*     */       }
/* 235 */       Set classFeatures = resourceClass.m_providedFeatures;
/* 236 */       if (null != classFeatures)
/*     */       {
/* 239 */         String classFieldName = publishedFeaturesTable.getFieldName(classIndex);
/* 240 */         String msg = LocaleUtils.encodeMessage("csPublishedFeatureDuplicateRow", null, className, classFieldName, "PublishedFeatures");
/*     */ 
/* 242 */         SystemUtils.err(msg);
/* 243 */         label544: LoggingUtils.warning(null, msg, null);
/*     */       }
/*     */       else {
/* 246 */         classFeatures = new HashSet();
/* 247 */         resourceClass.m_providedFeatures = classFeatures;
/*     */ 
/* 249 */         String providedFeatures = publishedFeaturesTable.getStringValue(providedIndex);
/*     */         try
/*     */         {
/* 252 */           providedFeatures = this.m_state.m_publishMerger.evaluateScript(providedFeatures);
/*     */         }
/*     */         catch (Exception e)
/*     */         {
/* 256 */           String msg = LocaleUtils.encodeMessage("csPublishedFeatureTableScriptError", null, "providedFeatures", "PublishedFeatures");
/*     */ 
/* 258 */           SystemUtils.err(msg);
/* 259 */           LoggingUtils.warning(null, msg, null);
/* 260 */           break label544:
/*     */         }
/*     */ 
/* 263 */         List featuresList = StringUtils.parseArray(providedFeatures, ',', ',');
/* 264 */         int featuresCount = featuresList.size();
/* 265 */         for (int i = 0; i < featuresCount; ++i)
/*     */         {
/* 267 */           String featureStr = (String)featuresList.get(i);
/* 268 */           WebFeature feature = new WebFeature(featureStr);
/* 269 */           WebFeature existingFeature = (WebFeature)providedPublishedFeatures.get(feature.m_featureName);
/* 270 */           if (existingFeature != null)
/*     */           {
/* 272 */             int level = existingFeature.compareLevelTo(feature);
/* 273 */             if (0 == level)
/*     */             {
/* 276 */               Object[] args = { feature.m_featureName, className, feature.m_class.m_name, feature.m_featureLevel };
/*     */ 
/* 279 */               String msg = LocaleUtils.encodeMessage("csPublishedFeatureAlreadyProvided", null, args);
/* 280 */               SystemUtils.err(msg);
/* 281 */               LoggingUtils.warning(null, msg, null);
/* 282 */               continue;
/*     */             }
/* 284 */             if (level > 0) {
/*     */               continue;
/*     */             }
/*     */           }
/*     */ 
/* 289 */           feature.m_class = resourceClass;
/* 290 */           feature.m_container = ((resourceClass.m_bundle == null) ? resourceClass : resourceClass.m_bundle);
/* 291 */           providedPublishedFeatures.put(feature.m_featureName, feature);
/* 292 */           classFeatures.add(feature.m_featureName);
/*     */         }
/*     */       }
/*     */     }
/* 296 */     this.m_state.m_features = providedPublishedFeatures;
/*     */   }
/*     */ 
/*     */   protected void computeFeatureDependsMaps()
/*     */     throws DataException
/*     */   {
/* 309 */     DataResultSet publishedFeaturesTable = this.m_publishedFeaturesTable;
/* 310 */     int classIndex = this.m_publishedFeaturesTableFields[0].m_index;
/* 311 */     int requiredIndex = this.m_publishedFeaturesTableFields[2].m_index;
/* 312 */     int optionalIndex = this.m_publishedFeaturesTableFields[3].m_index;
/* 313 */     Set providedClasses = new HashSet();
/* 314 */     Set missingFeatures = new HashSet();
/* 315 */     for (publishedFeaturesTable.first(); publishedFeaturesTable.isRowPresent(); publishedFeaturesTable.next())
/*     */     {
/* 317 */       String className = publishedFeaturesTable.getStringValue(classIndex);
/* 318 */       className = PublishedResourceUtils.toClassname(className);
/* 319 */       boolean classExists = providedClasses.contains(className);
/* 320 */       if (classExists)
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 326 */       providedClasses.add(className);
/*     */ 
/* 328 */       PublishedResourceContainer.Class resourceClass = (PublishedResourceContainer.Class)this.m_state.m_publishedClasses.get(className);
/* 329 */       resourceClass.m_requiredFeatures = new HashSet();
/* 330 */       resourceClass.m_optionalFeatures = new HashSet();
/*     */ 
/* 333 */       String requiredFeatures = publishedFeaturesTable.getStringValue(requiredIndex);
/*     */       try
/*     */       {
/* 336 */         requiredFeatures = this.m_state.m_publishMerger.evaluateScript(requiredFeatures);
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 340 */         String msg = LocaleUtils.encodeMessage("csPublishedFeatureTableScriptError", null, "requiredFeatures", "PublishedFeatures");
/*     */ 
/* 342 */         SystemUtils.err(msg);
/* 343 */         LoggingUtils.warning(e, msg, null);
/* 344 */         break label434:
/*     */       }
/* 346 */       processFeatureDepends(requiredFeatures, resourceClass.m_requiredFeatures, missingFeatures);
/*     */ 
/* 349 */       String optionalFeatures = publishedFeaturesTable.getStringValue(optionalIndex);
/*     */       try
/*     */       {
/* 352 */         optionalFeatures = this.m_state.m_publishMerger.evaluateScript(optionalFeatures);
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 356 */         String msg = LocaleUtils.encodeMessage("csPublishedFeatureTableScriptError", null, "optionalFeatures", "PublishedFeatures");
/*     */ 
/* 358 */         SystemUtils.err(msg);
/* 359 */         LoggingUtils.warning(e, msg, null);
/* 360 */         break label434:
/*     */       }
/* 362 */       processFeatureDepends(optionalFeatures, resourceClass.m_optionalFeatures, missingFeatures);
/*     */ 
/* 365 */       if (missingFeatures.isEmpty())
/*     */         continue;
/* 367 */       String msg = LocaleUtils.encodeMessage("csPublishedFeaturesMissing", null);
/* 368 */       msg = LocaleResources.localizeMessage(msg, null);
/* 369 */       IdcStringBuilder buffer = new IdcStringBuilder(msg);
/* 370 */       buffer.append('\n');
/* 371 */       Iterator iter = missingFeatures.iterator();
/* 372 */       while (iter.hasNext())
/*     */       {
/* 374 */         String featureName = (String)iter.next();
/* 375 */         buffer.append('\t');
/* 376 */         buffer.append(featureName);
/* 377 */         buffer.append('\n');
/*     */       }
/* 379 */       msg = buffer.toString();
/* 380 */       SystemUtils.err(msg);
/* 381 */       LoggingUtils.warning(null, "!$" + msg, null);
/* 382 */       label434: missingFeatures.clear();
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void processFeatureDepends(String featuresString, Set<String> dependsFeatures, Set<String> missingFeatures)
/*     */     throws DataException
/*     */   {
/* 397 */     List featuresList = StringUtils.parseArray(featuresString, ',', ',');
/* 398 */     int featuresCount = featuresList.size();
/* 399 */     for (int i = 0; i < featuresCount; ++i)
/*     */     {
/* 401 */       String dependency = (String)featuresList.get(i);
/* 402 */       WebFeature feature = new WebFeature(dependency);
/* 403 */       String featureName = feature.m_featureName;
/* 404 */       WebFeature existingFeature = (WebFeature)this.m_state.m_features.get(featureName);
/* 405 */       if (null == existingFeature)
/*     */       {
/* 407 */         missingFeatures.add(featureName);
/* 408 */         return;
/*     */       }
/* 410 */       if (existingFeature.compareLevelTo(feature) < 0)
/*     */       {
/* 412 */         String msg = LocaleUtils.encodeMessage("csPublishedFeatureLevelMissing", null, featureName, feature.m_featureLevel, existingFeature.m_featureLevel);
/*     */ 
/* 414 */         SystemUtils.err(msg);
/* 415 */         LoggingUtils.warning(null, msg, null);
/* 416 */         return;
/*     */       }
/*     */ 
/* 419 */       dependsFeatures.add(featureName);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void outputDependencyTree()
/*     */   {
/* 428 */     IdcStringBuilder str = new IdcStringBuilder();
/* 429 */     Set classnamesSet = this.m_state.m_publishedClasses.keySet();
/* 430 */     String[] classnames = StringUtils.convertToArray(classnamesSet);
/* 431 */     Arrays.sort(classnames);
/* 432 */     int numClasses = classnames.length;
/* 433 */     for (int c = 0; c < numClasses; ++c)
/*     */     {
/* 435 */       String classname = classnames[c];
/* 436 */       PublishedResourceContainer.Class resourceClass = (PublishedResourceContainer.Class)this.m_state.m_publishedClasses.get(classname);
/* 437 */       if (resourceClass.m_providedFeatures == null) {
/*     */         continue;
/*     */       }
/*     */ 
/* 441 */       str.setLength(0);
/* 442 */       String[] featuresArray = StringUtils.convertToArray(resourceClass.m_providedFeatures);
/* 443 */       if (featuresArray.length < 1) {
/*     */         continue;
/*     */       }
/*     */ 
/* 447 */       Arrays.sort(featuresArray);
/* 448 */       for (int i = 0; i < featuresArray.length; ++i)
/*     */       {
/* 450 */         if (i > 0)
/*     */         {
/* 452 */           str.append(", ");
/*     */         }
/* 454 */         str.append(featuresArray[i]);
/*     */       }
/* 456 */       str.append(" requires ");
/* 457 */       featuresArray = StringUtils.convertToArray(resourceClass.m_requiredFeatures);
/* 458 */       Arrays.sort(featuresArray);
/* 459 */       boolean doComma = false;
/* 460 */       for (int i = 0; i < featuresArray.length; ++i)
/*     */       {
/* 462 */         if (doComma)
/*     */         {
/* 464 */           str.append(", ");
/*     */         }
/* 466 */         str.append(featuresArray[i]);
/* 467 */         doComma = true;
/*     */       }
/* 469 */       if (!resourceClass.m_optionalFeatures.isEmpty())
/*     */       {
/* 471 */         featuresArray = StringUtils.convertToArray(resourceClass.m_optionalFeatures);
/* 472 */         Arrays.sort(featuresArray);
/* 473 */         str.append((doComma) ? ", [" : " [");
/* 474 */         doComma = false;
/* 475 */         for (int i = 0; i < featuresArray.length; ++i)
/*     */         {
/* 477 */           if (doComma)
/*     */           {
/* 479 */             str.append(", ");
/*     */           }
/* 481 */           str.append(featuresArray[i]);
/* 482 */           doComma = true;
/*     */         }
/* 484 */         str.append(']');
/*     */       }
/* 486 */       Report.trace("publish", str.toStringNoRelease(), null);
/*     */     }
/* 488 */     str.releaseBuffers();
/*     */   }
/*     */ 
/*     */   protected void computeFeaturesRequirements()
/*     */   {
/* 499 */     for (String featureName : this.m_state.m_features.keySet())
/*     */     {
/* 501 */       WebFeature feature = (WebFeature)this.m_state.m_features.get(featureName);
/* 502 */       Set requiresSet = new HashSet();
/* 503 */       addRequiredForFeature(feature, requiresSet);
/* 504 */       feature.m_requiredSet = requiresSet;
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void addRequiredForFeature(WebFeature feature, Set<WebFeature> requiredSet)
/*     */   {
/* 516 */     if (feature.m_requiredSet != null)
/*     */     {
/* 519 */       requiredSet.addAll(feature.m_requiredSet);
/* 520 */       return;
/*     */     }
/* 522 */     requiredSet.add(feature);
/* 523 */     Set requiredNames = feature.m_class.m_requiredFeatures;
/* 524 */     for (String featureName : requiredNames)
/*     */     {
/* 526 */       WebFeature requiredFeature = (WebFeature)this.m_state.m_features.get(featureName);
/* 527 */       if (requiredSet.contains(requiredFeature))
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 532 */       addRequiredForFeature(requiredFeature, requiredSet); }  } 
/*     */   public PublishedResourceContainer[] sortContainers(Set<PublishedResourceContainer> containersSet, boolean followBundle) throws DataException { // Byte code:
/*     */     //   0: aload_1
/*     */     //   1: invokeinterface 116 1 0
/*     */     //   6: istore_3
/*     */     //   7: iload_3
/*     */     //   8: anewarray 25	java/lang/String
/*     */     //   11: astore 4
/*     */     //   13: iload_3
/*     */     //   14: anewarray 117	intradoc/server/publish/PublishedResourceContainer
/*     */     //   17: astore 5
/*     */     //   19: aload_1
/*     */     //   20: aload 5
/*     */     //   22: invokeinterface 118 2 0
/*     */     //   27: pop
/*     */     //   28: aload 5
/*     */     //   30: invokestatic 102	java/util/Arrays:sort	([Ljava/lang/Object;)V
/*     */     //   33: new 33	java/util/HashMap
/*     */     //   36: dup
/*     */     //   37: invokespecial 34	java/util/HashMap:<init>	()V
/*     */     //   40: astore 6
/*     */     //   42: new 53	java/util/HashSet
/*     */     //   45: dup
/*     */     //   46: invokespecial 54	java/util/HashSet:<init>	()V
/*     */     //   49: astore 7
/*     */     //   51: iload_3
/*     */     //   52: iconst_1
/*     */     //   53: isub
/*     */     //   54: istore 8
/*     */     //   56: iload 8
/*     */     //   58: iflt +131 -> 189
/*     */     //   61: aload 5
/*     */     //   63: iload 8
/*     */     //   65: aaload
/*     */     //   66: astore 9
/*     */     //   68: aload 9
/*     */     //   70: getfield 119	intradoc/server/publish/PublishedResourceContainer:m_requiredFeatures	Ljava/util/Set;
/*     */     //   73: astore 10
/*     */     //   75: aload 9
/*     */     //   77: getfield 120	intradoc/server/publish/PublishedResourceContainer:m_optionalFeatures	Ljava/util/Set;
/*     */     //   80: astore 11
/*     */     //   82: aload 9
/*     */     //   84: getfield 121	intradoc/server/publish/PublishedResourceContainer:m_providedFeatures	Ljava/util/Set;
/*     */     //   87: astore 12
/*     */     //   89: aload 12
/*     */     //   91: ifnull +67 -> 158
/*     */     //   94: aload 12
/*     */     //   96: invokeinterface 112 1 0
/*     */     //   101: astore 13
/*     */     //   103: aload 13
/*     */     //   105: invokeinterface 88 1 0
/*     */     //   110: ifeq +38 -> 148
/*     */     //   113: aload 13
/*     */     //   115: invokeinterface 89 1 0
/*     */     //   120: checkcast 25	java/lang/String
/*     */     //   123: astore 14
/*     */     //   125: aload 10
/*     */     //   127: aload 14
/*     */     //   129: invokeinterface 122 2 0
/*     */     //   134: pop
/*     */     //   135: aload 11
/*     */     //   137: aload 14
/*     */     //   139: invokeinterface 122 2 0
/*     */     //   144: pop
/*     */     //   145: goto -42 -> 103
/*     */     //   148: aload 7
/*     */     //   150: aload 12
/*     */     //   152: invokeinterface 115 2 0
/*     */     //   157: pop
/*     */     //   158: aload 4
/*     */     //   160: iload 8
/*     */     //   162: aload 9
/*     */     //   164: getfield 123	intradoc/server/publish/PublishedResourceContainer:m_name	Ljava/lang/String;
/*     */     //   167: aastore
/*     */     //   168: aload 6
/*     */     //   170: aload 9
/*     */     //   172: getfield 123	intradoc/server/publish/PublishedResourceContainer:m_name	Ljava/lang/String;
/*     */     //   175: aload 9
/*     */     //   177: invokeinterface 43 3 0
/*     */     //   182: pop
/*     */     //   183: iinc 8 255
/*     */     //   186: goto -130 -> 56
/*     */     //   189: aload_0
/*     */     //   190: getfield 2	intradoc/server/publish/WebFeaturesPublisher:m_state	Lintradoc/server/publish/WebPublishState;
/*     */     //   193: getfield 75	intradoc/server/publish/WebPublishState:m_features	Ljava/util/Map;
/*     */     //   196: astore 8
/*     */     //   198: iload_3
/*     */     //   199: anewarray 124	[Ljava/lang/String;
/*     */     //   202: astore 9
/*     */     //   204: iload_3
/*     */     //   205: iconst_1
/*     */     //   206: isub
/*     */     //   207: istore 10
/*     */     //   209: iload 10
/*     */     //   211: iflt +205 -> 416
/*     */     //   214: aload 5
/*     */     //   216: iload 10
/*     */     //   218: aaload
/*     */     //   219: astore 11
/*     */     //   221: aload 11
/*     */     //   223: getfield 121	intradoc/server/publish/PublishedResourceContainer:m_providedFeatures	Ljava/util/Set;
/*     */     //   226: ifnonnull +6 -> 232
/*     */     //   229: goto +181 -> 410
/*     */     //   232: new 53	java/util/HashSet
/*     */     //   235: dup
/*     */     //   236: aload 11
/*     */     //   238: getfield 119	intradoc/server/publish/PublishedResourceContainer:m_requiredFeatures	Ljava/util/Set;
/*     */     //   241: invokespecial 125	java/util/HashSet:<init>	(Ljava/util/Collection;)V
/*     */     //   244: astore 12
/*     */     //   246: aload 12
/*     */     //   248: aload 11
/*     */     //   250: getfield 120	intradoc/server/publish/PublishedResourceContainer:m_optionalFeatures	Ljava/util/Set;
/*     */     //   253: invokeinterface 115 2 0
/*     */     //   258: pop
/*     */     //   259: aload 12
/*     */     //   261: invokeinterface 116 1 0
/*     */     //   266: istore 13
/*     */     //   268: iload 13
/*     */     //   270: iconst_1
/*     */     //   271: if_icmpge +6 -> 277
/*     */     //   274: goto +136 -> 410
/*     */     //   277: aload 9
/*     */     //   279: iload 10
/*     */     //   281: iload 13
/*     */     //   283: anewarray 25	java/lang/String
/*     */     //   286: dup_x2
/*     */     //   287: aastore
/*     */     //   288: astore 14
/*     */     //   290: iconst_0
/*     */     //   291: istore 15
/*     */     //   293: aload 12
/*     */     //   295: invokeinterface 112 1 0
/*     */     //   300: astore 16
/*     */     //   302: aload 16
/*     */     //   304: invokeinterface 88 1 0
/*     */     //   309: ifeq +101 -> 410
/*     */     //   312: aload 16
/*     */     //   314: invokeinterface 89 1 0
/*     */     //   319: checkcast 25	java/lang/String
/*     */     //   322: astore 17
/*     */     //   324: aload 7
/*     */     //   326: aload 17
/*     */     //   328: invokeinterface 76 2 0
/*     */     //   333: ifne +6 -> 339
/*     */     //   336: goto -34 -> 302
/*     */     //   339: aload 8
/*     */     //   341: aload 17
/*     */     //   343: invokeinterface 40 2 0
/*     */     //   348: checkcast 63	intradoc/server/publish/WebFeature
/*     */     //   351: astore 18
/*     */     //   353: aload 18
/*     */     //   355: getfield 67	intradoc/server/publish/WebFeature:m_class	Lintradoc/server/publish/PublishedResourceContainer$Class;
/*     */     //   358: astore 19
/*     */     //   360: iload_2
/*     */     //   361: ifeq +33 -> 394
/*     */     //   364: aload 19
/*     */     //   366: instanceof 41
/*     */     //   369: ifeq +25 -> 394
/*     */     //   372: aload 19
/*     */     //   374: checkcast 41	intradoc/server/publish/PublishedResourceContainer$Class
/*     */     //   377: astore 20
/*     */     //   379: aload 20
/*     */     //   381: getfield 46	intradoc/server/publish/PublishedResourceContainer$Class:m_bundle	Lintradoc/server/publish/PublishedResourceContainer$Bundle;
/*     */     //   384: ifnull +10 -> 394
/*     */     //   387: aload 20
/*     */     //   389: getfield 46	intradoc/server/publish/PublishedResourceContainer$Class:m_bundle	Lintradoc/server/publish/PublishedResourceContainer$Bundle;
/*     */     //   392: astore 19
/*     */     //   394: aload 14
/*     */     //   396: iload 15
/*     */     //   398: iinc 15 1
/*     */     //   401: aload 19
/*     */     //   403: getfield 123	intradoc/server/publish/PublishedResourceContainer:m_name	Ljava/lang/String;
/*     */     //   406: aastore
/*     */     //   407: goto -105 -> 302
/*     */     //   410: iinc 10 255
/*     */     //   413: goto -204 -> 209
/*     */     //   416: aload 4
/*     */     //   418: aload 9
/*     */     //   420: iconst_1
/*     */     //   421: invokestatic 126	intradoc/common/SortUtils:acyclicTopologicalSortNamed	([Ljava/lang/String;[[Ljava/lang/String;Z)[Ljava/lang/String;
/*     */     //   424: astore 10
/*     */     //   426: goto +127 -> 553
/*     */     //   429: astore 11
/*     */     //   431: aload 11
/*     */     //   433: getfield 128	intradoc/common/DAGHasCycleException:m_indices	[I
/*     */     //   436: ifnonnull +17 -> 453
/*     */     //   439: iconst_1
/*     */     //   440: newarray int
/*     */     //   442: dup
/*     */     //   443: iconst_0
/*     */     //   444: aload 11
/*     */     //   446: getfield 129	intradoc/common/DAGHasCycleException:m_index	I
/*     */     //   449: iastore
/*     */     //   450: goto +8 -> 458
/*     */     //   453: aload 11
/*     */     //   455: getfield 128	intradoc/common/DAGHasCycleException:m_indices	[I
/*     */     //   458: astore 12
/*     */     //   460: aload 12
/*     */     //   462: arraylength
/*     */     //   463: istore 13
/*     */     //   465: new 130	java/util/ArrayList
/*     */     //   468: dup
/*     */     //   469: iload 13
/*     */     //   471: invokespecial 131	java/util/ArrayList:<init>	(I)V
/*     */     //   474: astore 14
/*     */     //   476: iconst_0
/*     */     //   477: istore 15
/*     */     //   479: iload 15
/*     */     //   481: iload 13
/*     */     //   483: if_icmpge +32 -> 515
/*     */     //   486: aload 5
/*     */     //   488: aload 12
/*     */     //   490: iload 15
/*     */     //   492: iaload
/*     */     //   493: aaload
/*     */     //   494: astore 16
/*     */     //   496: aload 14
/*     */     //   498: aload 16
/*     */     //   500: getfield 123	intradoc/server/publish/PublishedResourceContainer:m_name	Ljava/lang/String;
/*     */     //   503: invokeinterface 132 2 0
/*     */     //   508: pop
/*     */     //   509: iinc 15 1
/*     */     //   512: goto -33 -> 479
/*     */     //   515: new 22	intradoc/data/DataException
/*     */     //   518: dup
/*     */     //   519: aconst_null
/*     */     //   520: ldc 133
/*     */     //   522: iconst_1
/*     */     //   523: anewarray 9	java/lang/Object
/*     */     //   526: dup
/*     */     //   527: iconst_0
/*     */     //   528: aload 14
/*     */     //   530: aastore
/*     */     //   531: invokespecial 134	intradoc/data/DataException:<init>	(Ljava/lang/Throwable;Ljava/lang/String;[Ljava/lang/Object;)V
/*     */     //   534: athrow
/*     */     //   535: astore 11
/*     */     //   537: new 22	intradoc/data/DataException
/*     */     //   540: dup
/*     */     //   541: aload 11
/*     */     //   543: ldc 135
/*     */     //   545: iconst_0
/*     */     //   546: anewarray 9	java/lang/Object
/*     */     //   549: invokespecial 134	intradoc/data/DataException:<init>	(Ljava/lang/Throwable;Ljava/lang/String;[Ljava/lang/Object;)V
/*     */     //   552: athrow
/*     */     //   553: iload_3
/*     */     //   554: anewarray 117	intradoc/server/publish/PublishedResourceContainer
/*     */     //   557: astore 11
/*     */     //   559: aload 10
/*     */     //   561: arraylength
/*     */     //   562: iconst_1
/*     */     //   563: isub
/*     */     //   564: istore 12
/*     */     //   566: iload 12
/*     */     //   568: iflt +37 -> 605
/*     */     //   571: aload 10
/*     */     //   573: iload 12
/*     */     //   575: aaload
/*     */     //   576: astore 13
/*     */     //   578: aload 6
/*     */     //   580: aload 13
/*     */     //   582: invokeinterface 40 2 0
/*     */     //   587: checkcast 117	intradoc/server/publish/PublishedResourceContainer
/*     */     //   590: astore 14
/*     */     //   592: aload 11
/*     */     //   594: iload 12
/*     */     //   596: aload 14
/*     */     //   598: aastore
/*     */     //   599: iinc 12 255
/*     */     //   602: goto -36 -> 566
/*     */     //   605: aload 11
/*     */     //   607: areturn
/*     */     //
/*     */     // Exception table:
/*     */     //   from	to	target	type
/*     */     //   416	426	429	intradoc/common/DAGHasCycleException
/*     */     //   416	426	535	java/lang/Exception } 
/* 659 */   public static Object idcVersionInfo(Object arg) { return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 85055 $"; }
/*     */ 
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.publish.WebFeaturesPublisher
 * JD-Core Version:    0.5.4
 */