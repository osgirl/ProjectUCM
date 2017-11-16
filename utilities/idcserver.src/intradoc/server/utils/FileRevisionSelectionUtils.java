/*     */ package intradoc.server.utils;
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
/*     */ import intradoc.data.MapParameters;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.serialize.DataBinderLocalizer;
/*     */ import intradoc.shared.PluginFilters;
/*     */ import java.util.Collection;
/*     */ import java.util.HashMap;
/*     */ import java.util.HashSet;
/*     */ import java.util.List;
/*     */ 
/*     */ public class FileRevisionSelectionUtils
/*     */ {
/*     */   public static DataResultSet loadAdditionalDocInfo(String queryKey, DataBinder binder, ExecutionContext cxt, Workspace ws)
/*     */     throws DataException, ServiceException
/*     */   {
/*  65 */     String docNameTest = binder.getAllowMissing("dDocName");
/*  66 */     if ((docNameTest == null) || (docNameTest.length() == 0))
/*     */     {
/*  68 */       Report.trace("system", "loadAdditionalDocInfo() dDocName missing", null);
/*  69 */       return null;
/*     */     }
/*  71 */     RevisionSelectionParameters params = new RevisionSelectionParameters("RevLabel", null, queryKey);
/*  72 */     params.m_computeDocInfo = true;
/*  73 */     params.m_suppressErrorRetrievingDocInfo = true;
/*  74 */     computeDocumentRevisionMethod(binder, cxt, ws, params);
/*  75 */     computeDocumentRevisionInfo(binder, cxt, ws, params);
/*  76 */     return params.m_docInfo;
/*     */   }
/*     */ 
/*     */   public static void computeDocumentRevisionMethod(DataBinder binder, ExecutionContext cxt, Workspace ws, RevisionSelectionParameters params)
/*     */     throws DataException, ServiceException
/*     */   {
/*  86 */     params.m_id = getBinderValue("dID", binder, params);
/*  87 */     params.m_docName = getBinderValue("dDocName", binder, params);
/*     */ 
/*  89 */     cxt.setCachedObject("RevisionSelectionParameters", params);
/*  90 */     if (PluginFilters.filter("computeDocumentRevisionMethod", ws, binder, cxt) == -1) {
/*     */       return;
/*     */     }
/*  93 */     String mandatoryParameter = params.m_docName;
/*  94 */     String[] mandatoryArray = params.m_docNames;
/*  95 */     if (params.m_revisionSelectionMethod.equalsIgnoreCase("Specific"))
/*     */     {
/*  98 */       params.m_revisionSelectionMethod = "Specific";
/*  99 */       params.m_haveRevID = true;
/*     */ 
/* 101 */       if (params.m_isBatchSelection)
/*     */       {
/* 103 */         mandatoryArray = params.m_ids;
/*     */       }
/*     */       else
/*     */       {
/* 107 */         mandatoryParameter = params.m_id;
/*     */       }
/*     */     }
/* 110 */     else if (params.m_revisionSelectionMethod.equalsIgnoreCase("LatestReleased"))
/*     */     {
/* 113 */       params.m_revisionSelectionMethod = "LatestReleased";
/* 114 */       params.m_doesLatestReleased = true;
/*     */     }
/* 116 */     else if (params.m_revisionSelectionMethod.equalsIgnoreCase("Latest"))
/*     */     {
/* 119 */       params.m_revisionSelectionMethod = "Latest";
/*     */ 
/* 121 */       if (params.m_isBatchSelection)
/*     */       {
/* 123 */         String msg = LocaleUtils.encodeMessage("csGetFileCanNotBatchMethod", null, params.m_revisionSelectionMethod);
/*     */ 
/* 125 */         throw new DataException(msg);
/*     */       }
/*     */     }
/* 128 */     else if (params.m_revisionSelectionMethod.equalsIgnoreCase("RevLabel"))
/*     */     {
/* 131 */       params.m_revisionSelectionMethod = "RevLabel";
/*     */ 
/* 133 */       if (params.m_isBatchSelection)
/*     */       {
/* 135 */         String msg = LocaleUtils.encodeMessage("csGetFileCanNotBatchMethod", null, params.m_revisionSelectionMethod);
/*     */ 
/* 137 */         throw new DataException(msg);
/*     */       }
/*     */     }
/* 140 */     else if (params.m_revisionSelectionMethod.equalsIgnoreCase("LatestNonDeleted"))
/*     */     {
/* 142 */       params.m_revisionSelectionMethod = "LatestNonDeleted";
/*     */     }
/* 144 */     else if (!params.m_selectionMethodIsValid)
/*     */     {
/* 147 */       String presentationDocName = (params.m_docName != null) ? params.m_docName : params.m_id;
/*     */ 
/* 149 */       String msg = LocaleUtils.encodeMessage("csGetFileRevisionSelectionMethodError", null, presentationDocName);
/*     */ 
/* 151 */       throw new DataException(msg);
/*     */     }
/*     */ 
/* 154 */     if ((!params.m_selectionMethodIsValid) && ((
/* 157 */       ((params.m_isBatchSelection) && (((mandatoryArray == null) || (mandatoryArray.length == 0)))) || ((!params.m_isBatchSelection) && (((mandatoryParameter == null) || (mandatoryParameter.length() == 0)))))))
/*     */     {
/* 162 */       String msg = LocaleUtils.encodeMessage("csGetFileNeedsParameter", null, params.m_revisionSelectionMethod);
/*     */ 
/* 164 */       throw new DataException(msg);
/*     */     }
/*     */ 
/* 169 */     params.m_selectionMethodIsValid = true; } 
/*     */   public static void computeDocumentRevisionInfo(DataBinder binder, ExecutionContext cxt, Workspace ws, RevisionSelectionParameters params) throws DataException, ServiceException { // Byte code:
/*     */     //   0: aload_0
/*     */     //   1: ldc 42
/*     */     //   3: ldc 43
/*     */     //   5: invokevirtual 44	intradoc/data/DataBinder:putLocal	(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
/*     */     //   8: pop
/*     */     //   9: aload_3
/*     */     //   10: getfield 25	intradoc/server/utils/RevisionSelectionParameters:m_revisionSelectionMethod	Ljava/lang/String;
/*     */     //   13: astore 4
/*     */     //   15: iconst_0
/*     */     //   16: istore 5
/*     */     //   18: aload_3
/*     */     //   19: getfield 45	intradoc/server/utils/RevisionSelectionParameters:m_queryKey	Ljava/lang/String;
/*     */     //   22: ifnonnull +12 -> 34
/*     */     //   25: aload_3
/*     */     //   26: ldc 46
/*     */     //   28: putfield 45	intradoc/server/utils/RevisionSelectionParameters:m_queryKey	Ljava/lang/String;
/*     */     //   31: iconst_1
/*     */     //   32: istore 5
/*     */     //   34: aload_3
/*     */     //   35: getfield 47	intradoc/server/utils/RevisionSelectionParameters:m_haveDocInfo	Z
/*     */     //   38: ifne +787 -> 825
/*     */     //   41: aconst_null
/*     */     //   42: astore 6
/*     */     //   44: aconst_null
/*     */     //   45: astore 7
/*     */     //   47: aload_3
/*     */     //   48: getfield 28	intradoc/server/utils/RevisionSelectionParameters:m_haveRevID	Z
/*     */     //   51: ifne +774 -> 825
/*     */     //   54: aload_3
/*     */     //   55: getfield 25	intradoc/server/utils/RevisionSelectionParameters:m_revisionSelectionMethod	Ljava/lang/String;
/*     */     //   58: ldc 9
/*     */     //   60: invokevirtual 48	java/lang/String:equals	(Ljava/lang/Object;)Z
/*     */     //   63: ifeq +44 -> 107
/*     */     //   66: ldc 49
/*     */     //   68: aload_0
/*     */     //   69: aload_3
/*     */     //   70: invokestatic 17	intradoc/server/utils/FileRevisionSelectionUtils:getBinderValue	(Ljava/lang/String;Lintradoc/data/DataBinder;Lintradoc/server/utils/RevisionSelectionParameters;)Ljava/lang/String;
/*     */     //   73: astore 8
/*     */     //   75: aload 8
/*     */     //   77: ifnull +18 -> 95
/*     */     //   80: aload 8
/*     */     //   82: invokevirtual 4	java/lang/String:length	()I
/*     */     //   85: ifle +10 -> 95
/*     */     //   88: ldc 50
/*     */     //   90: astore 7
/*     */     //   92: goto +12 -> 104
/*     */     //   95: ldc 31
/*     */     //   97: astore 4
/*     */     //   99: aload_3
/*     */     //   100: iconst_1
/*     */     //   101: putfield 32	intradoc/server/utils/RevisionSelectionParameters:m_doesLatestReleased	Z
/*     */     //   104: goto +52 -> 156
/*     */     //   107: aload_3
/*     */     //   108: getfield 25	intradoc/server/utils/RevisionSelectionParameters:m_revisionSelectionMethod	Ljava/lang/String;
/*     */     //   111: ldc 33
/*     */     //   113: invokevirtual 48	java/lang/String:equals	(Ljava/lang/Object;)Z
/*     */     //   116: ifeq +10 -> 126
/*     */     //   119: ldc 51
/*     */     //   121: astore 7
/*     */     //   123: goto +33 -> 156
/*     */     //   126: aload_3
/*     */     //   127: getfield 25	intradoc/server/utils/RevisionSelectionParameters:m_revisionSelectionMethod	Ljava/lang/String;
/*     */     //   130: ldc 38
/*     */     //   132: invokevirtual 48	java/lang/String:equals	(Ljava/lang/Object;)Z
/*     */     //   135: ifeq +21 -> 156
/*     */     //   138: aload_3
/*     */     //   139: getfield 29	intradoc/server/utils/RevisionSelectionParameters:m_isBatchSelection	Z
/*     */     //   142: ifeq +10 -> 152
/*     */     //   145: ldc 52
/*     */     //   147: astore 7
/*     */     //   149: goto +7 -> 156
/*     */     //   152: ldc 53
/*     */     //   154: astore 7
/*     */     //   156: aload_3
/*     */     //   157: getfield 32	intradoc/server/utils/RevisionSelectionParameters:m_doesLatestReleased	Z
/*     */     //   160: ifeq +159 -> 319
/*     */     //   163: aload_3
/*     */     //   164: getfield 54	intradoc/server/utils/RevisionSelectionParameters:m_useLatestReleasedDocInfoCache	Z
/*     */     //   167: istore 8
/*     */     //   169: aload_3
/*     */     //   170: getfield 11	intradoc/server/utils/RevisionSelectionParameters:m_computeDocInfo	Z
/*     */     //   173: ifeq +121 -> 294
/*     */     //   176: invokestatic 55	intradoc/server/utils/DocumentInfoCacheUtils:allowOptimizeLatestReleaseQueryingBasedOnTimestamps	()Z
/*     */     //   179: ifne +9 -> 188
/*     */     //   182: invokestatic 56	intradoc/server/utils/DocumentInfoCacheUtils:useSearchCacheRepairForLatestReleaseQuerying	()Z
/*     */     //   185: ifeq +109 -> 294
/*     */     //   188: aload_0
/*     */     //   189: ldc 57
/*     */     //   191: iload 8
/*     */     //   193: invokestatic 58	intradoc/data/DataBinderUtils:getLocalBoolean	(Lintradoc/data/DataBinder;Ljava/lang/String;Z)Z
/*     */     //   196: ifeq +98 -> 294
/*     */     //   199: aload_3
/*     */     //   200: getfield 29	intradoc/server/utils/RevisionSelectionParameters:m_isBatchSelection	Z
/*     */     //   203: ifne +91 -> 294
/*     */     //   206: aload_3
/*     */     //   207: getfield 59	intradoc/server/utils/RevisionSelectionParameters:m_currentTime	J
/*     */     //   210: lconst_0
/*     */     //   211: lcmp
/*     */     //   212: ifne +10 -> 222
/*     */     //   215: aload_3
/*     */     //   216: invokestatic 60	java/lang/System:currentTimeMillis	()J
/*     */     //   219: putfield 59	intradoc/server/utils/RevisionSelectionParameters:m_currentTime	J
/*     */     //   222: aload_3
/*     */     //   223: getfield 19	intradoc/server/utils/RevisionSelectionParameters:m_docName	Ljava/lang/String;
/*     */     //   226: aload_0
/*     */     //   227: aload_3
/*     */     //   228: getfield 45	intradoc/server/utils/RevisionSelectionParameters:m_queryKey	Ljava/lang/String;
/*     */     //   231: aconst_null
/*     */     //   232: aconst_null
/*     */     //   233: iconst_0
/*     */     //   234: aload_3
/*     */     //   235: getfield 59	intradoc/server/utils/RevisionSelectionParameters:m_currentTime	J
/*     */     //   238: aload_2
/*     */     //   239: aload_1
/*     */     //   240: invokestatic 61	intradoc/server/utils/DocumentInfoCacheUtils:getLatestReleasedDocInfo	(Ljava/lang/String;Lintradoc/data/DataBinder;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZJLintradoc/data/Workspace;Lintradoc/common/ExecutionContext;)Z
/*     */     //   243: istore 9
/*     */     //   245: iload 9
/*     */     //   247: ifeq +37 -> 284
/*     */     //   250: aload_3
/*     */     //   251: aload_0
/*     */     //   252: aload_3
/*     */     //   253: getfield 45	intradoc/server/utils/RevisionSelectionParameters:m_queryKey	Ljava/lang/String;
/*     */     //   256: invokevirtual 62	intradoc/data/DataBinder:getResultSet	(Ljava/lang/String;)Lintradoc/data/ResultSet;
/*     */     //   259: checkcast 63	intradoc/data/DataResultSet
/*     */     //   262: putfield 15	intradoc/server/utils/RevisionSelectionParameters:m_docInfo	Lintradoc/data/DataResultSet;
/*     */     //   265: aload_3
/*     */     //   266: iconst_1
/*     */     //   267: putfield 28	intradoc/server/utils/RevisionSelectionParameters:m_haveRevID	Z
/*     */     //   270: aload_3
/*     */     //   271: iconst_1
/*     */     //   272: putfield 47	intradoc/server/utils/RevisionSelectionParameters:m_haveDocInfo	Z
/*     */     //   275: aload_3
/*     */     //   276: getfield 15	intradoc/server/utils/RevisionSelectionParameters:m_docInfo	Lintradoc/data/DataResultSet;
/*     */     //   279: astore 6
/*     */     //   281: goto +13 -> 294
/*     */     //   284: aload_3
/*     */     //   285: aconst_null
/*     */     //   286: putfield 15	intradoc/server/utils/RevisionSelectionParameters:m_docInfo	Lintradoc/data/DataResultSet;
/*     */     //   289: aload_3
/*     */     //   290: iconst_1
/*     */     //   291: putfield 64	intradoc/server/utils/RevisionSelectionParameters:m_isError	Z
/*     */     //   294: aload_3
/*     */     //   295: getfield 28	intradoc/server/utils/RevisionSelectionParameters:m_haveRevID	Z
/*     */     //   298: ifne +21 -> 319
/*     */     //   301: aload_3
/*     */     //   302: getfield 29	intradoc/server/utils/RevisionSelectionParameters:m_isBatchSelection	Z
/*     */     //   305: ifeq +10 -> 315
/*     */     //   308: ldc 65
/*     */     //   310: astore 7
/*     */     //   312: goto +7 -> 319
/*     */     //   315: ldc 66
/*     */     //   317: astore 7
/*     */     //   319: aload_3
/*     */     //   320: getfield 67	intradoc/server/utils/RevisionSelectionParameters:m_computeIdQuery	Ljava/lang/String;
/*     */     //   323: ifnonnull +16 -> 339
/*     */     //   326: aload_3
/*     */     //   327: getfield 11	intradoc/server/utils/RevisionSelectionParameters:m_computeDocInfo	Z
/*     */     //   330: ifeq +9 -> 339
/*     */     //   333: aload_3
/*     */     //   334: aload 7
/*     */     //   336: putfield 67	intradoc/server/utils/RevisionSelectionParameters:m_computeIdQuery	Ljava/lang/String;
/*     */     //   339: aload_3
/*     */     //   340: getfield 29	intradoc/server/utils/RevisionSelectionParameters:m_isBatchSelection	Z
/*     */     //   343: ifeq +306 -> 649
/*     */     //   346: aload_3
/*     */     //   347: getfield 64	intradoc/server/utils/RevisionSelectionParameters:m_isError	Z
/*     */     //   350: ifne +475 -> 825
/*     */     //   353: aload_3
/*     */     //   354: getfield 28	intradoc/server/utils/RevisionSelectionParameters:m_haveRevID	Z
/*     */     //   357: ifne +468 -> 825
/*     */     //   360: aload_3
/*     */     //   361: getfield 67	intradoc/server/utils/RevisionSelectionParameters:m_computeIdQuery	Ljava/lang/String;
/*     */     //   364: ifnull +461 -> 825
/*     */     //   367: aload_3
/*     */     //   368: getfield 11	intradoc/server/utils/RevisionSelectionParameters:m_computeDocInfo	Z
/*     */     //   371: ifeq +454 -> 825
/*     */     //   374: new 68	java/util/HashSet
/*     */     //   377: dup
/*     */     //   378: invokespecial 69	java/util/HashSet:<init>	()V
/*     */     //   381: astore 8
/*     */     //   383: iconst_0
/*     */     //   384: istore 9
/*     */     //   386: new 70	intradoc/common/IdcStringBuilder
/*     */     //   389: dup
/*     */     //   390: invokespecial 71	intradoc/common/IdcStringBuilder:<init>	()V
/*     */     //   393: astore 10
/*     */     //   395: iload 9
/*     */     //   397: aload_3
/*     */     //   398: getfield 24	intradoc/server/utils/RevisionSelectionParameters:m_docNames	[Ljava/lang/String;
/*     */     //   401: arraylength
/*     */     //   402: if_icmpeq +11 -> 413
/*     */     //   405: iload 9
/*     */     //   407: bipush 25
/*     */     //   409: irem
/*     */     //   410: ifne +133 -> 543
/*     */     //   413: aload 10
/*     */     //   415: invokevirtual 72	intradoc/common/IdcStringBuilder:length	()I
/*     */     //   418: ifle +112 -> 530
/*     */     //   421: new 73	java/util/HashMap
/*     */     //   424: dup
/*     */     //   425: invokespecial 74	java/util/HashMap:<init>	()V
/*     */     //   428: astore 11
/*     */     //   430: aload 11
/*     */     //   432: ldc 2
/*     */     //   434: aload 10
/*     */     //   436: invokevirtual 75	intradoc/common/IdcStringBuilder:toString	()Ljava/lang/String;
/*     */     //   439: invokevirtual 76	java/util/HashMap:put	(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
/*     */     //   442: pop
/*     */     //   443: aload_2
/*     */     //   444: aload_3
/*     */     //   445: getfield 67	intradoc/server/utils/RevisionSelectionParameters:m_computeIdQuery	Ljava/lang/String;
/*     */     //   448: new 77	intradoc/data/MapParameters
/*     */     //   451: dup
/*     */     //   452: aload 11
/*     */     //   454: invokespecial 78	intradoc/data/MapParameters:<init>	(Ljava/util/Map;)V
/*     */     //   457: invokeinterface 79 3 0
/*     */     //   462: astore 12
/*     */     //   464: aload 12
/*     */     //   466: ldc 16
/*     */     //   468: invokeinterface 80 2 0
/*     */     //   473: istore 13
/*     */     //   475: aload 12
/*     */     //   477: invokeinterface 81 1 0
/*     */     //   482: pop
/*     */     //   483: aload 12
/*     */     //   485: invokeinterface 82 1 0
/*     */     //   490: ifeq +31 -> 521
/*     */     //   493: aload 8
/*     */     //   495: aload 12
/*     */     //   497: iload 13
/*     */     //   499: invokeinterface 83 2 0
/*     */     //   504: invokeinterface 84 2 0
/*     */     //   509: pop
/*     */     //   510: aload 12
/*     */     //   512: invokeinterface 85 1 0
/*     */     //   517: pop
/*     */     //   518: goto -35 -> 483
/*     */     //   521: new 70	intradoc/common/IdcStringBuilder
/*     */     //   524: dup
/*     */     //   525: invokespecial 71	intradoc/common/IdcStringBuilder:<init>	()V
/*     */     //   528: astore 10
/*     */     //   530: iload 9
/*     */     //   532: aload_3
/*     */     //   533: getfield 24	intradoc/server/utils/RevisionSelectionParameters:m_docNames	[Ljava/lang/String;
/*     */     //   536: arraylength
/*     */     //   537: if_icmpne +6 -> 543
/*     */     //   540: goto +38 -> 578
/*     */     //   543: aload 10
/*     */     //   545: invokevirtual 72	intradoc/common/IdcStringBuilder:length	()I
/*     */     //   548: ifle +11 -> 559
/*     */     //   551: aload 10
/*     */     //   553: bipush 44
/*     */     //   555: invokevirtual 86	intradoc/common/IdcStringBuilder:append	(C)Lintradoc/common/IdcAppendable;
/*     */     //   558: pop
/*     */     //   559: aload 10
/*     */     //   561: aload_3
/*     */     //   562: getfield 24	intradoc/server/utils/RevisionSelectionParameters:m_docNames	[Ljava/lang/String;
/*     */     //   565: iload 9
/*     */     //   567: aaload
/*     */     //   568: invokevirtual 87	intradoc/common/IdcStringBuilder:append	(Ljava/lang/String;)Lintradoc/common/IdcAppendable;
/*     */     //   571: pop
/*     */     //   572: iinc 9 1
/*     */     //   575: goto -180 -> 395
/*     */     //   578: aload_3
/*     */     //   579: aload 8
/*     */     //   581: invokeinterface 88 1 0
/*     */     //   586: anewarray 89	java/lang/String
/*     */     //   589: putfield 30	intradoc/server/utils/RevisionSelectionParameters:m_ids	[Ljava/lang/String;
/*     */     //   592: iconst_0
/*     */     //   593: istore 11
/*     */     //   595: aload 8
/*     */     //   597: invokeinterface 90 1 0
/*     */     //   602: astore 12
/*     */     //   604: aload 12
/*     */     //   606: invokeinterface 91 1 0
/*     */     //   611: ifeq +30 -> 641
/*     */     //   614: aload 12
/*     */     //   616: invokeinterface 92 1 0
/*     */     //   621: checkcast 89	java/lang/String
/*     */     //   624: astore 13
/*     */     //   626: aload_3
/*     */     //   627: getfield 30	intradoc/server/utils/RevisionSelectionParameters:m_ids	[Ljava/lang/String;
/*     */     //   630: iload 11
/*     */     //   632: aload 13
/*     */     //   634: aastore
/*     */     //   635: iinc 11 1
/*     */     //   638: goto -34 -> 604
/*     */     //   641: aload_3
/*     */     //   642: iconst_1
/*     */     //   643: putfield 28	intradoc/server/utils/RevisionSelectionParameters:m_haveRevID	Z
/*     */     //   646: goto +179 -> 825
/*     */     //   649: aload_3
/*     */     //   650: getfield 64	intradoc/server/utils/RevisionSelectionParameters:m_isError	Z
/*     */     //   653: ifne +37 -> 690
/*     */     //   656: aload_3
/*     */     //   657: getfield 28	intradoc/server/utils/RevisionSelectionParameters:m_haveRevID	Z
/*     */     //   660: ifne +30 -> 690
/*     */     //   663: aload_3
/*     */     //   664: getfield 67	intradoc/server/utils/RevisionSelectionParameters:m_computeIdQuery	Ljava/lang/String;
/*     */     //   667: ifnull +23 -> 690
/*     */     //   670: aload_3
/*     */     //   671: getfield 11	intradoc/server/utils/RevisionSelectionParameters:m_computeDocInfo	Z
/*     */     //   674: ifeq +16 -> 690
/*     */     //   677: aload_2
/*     */     //   678: aload_3
/*     */     //   679: getfield 67	intradoc/server/utils/RevisionSelectionParameters:m_computeIdQuery	Ljava/lang/String;
/*     */     //   682: aload_0
/*     */     //   683: invokeinterface 79 3 0
/*     */     //   688: astore 6
/*     */     //   690: aconst_null
/*     */     //   691: astore 8
/*     */     //   693: aload_3
/*     */     //   694: getfield 64	intradoc/server/utils/RevisionSelectionParameters:m_isError	Z
/*     */     //   697: ifne +27 -> 724
/*     */     //   700: aload 6
/*     */     //   702: ifnull +22 -> 724
/*     */     //   705: aload 6
/*     */     //   707: invokeinterface 82 1 0
/*     */     //   712: ifeq +12 -> 724
/*     */     //   715: aload 6
/*     */     //   717: ldc 16
/*     */     //   719: invokestatic 93	intradoc/data/ResultSetUtils:getValue	(Lintradoc/data/ResultSet;Ljava/lang/String;)Ljava/lang/String;
/*     */     //   722: astore 8
/*     */     //   724: aload_3
/*     */     //   725: getfield 64	intradoc/server/utils/RevisionSelectionParameters:m_isError	Z
/*     */     //   728: ifne +16 -> 744
/*     */     //   731: aload 8
/*     */     //   733: ifnull +11 -> 744
/*     */     //   736: aload 8
/*     */     //   738: invokevirtual 4	java/lang/String:length	()I
/*     */     //   741: ifne +70 -> 811
/*     */     //   744: aload_3
/*     */     //   745: iconst_1
/*     */     //   746: putfield 64	intradoc/server/utils/RevisionSelectionParameters:m_isError	Z
/*     */     //   749: aload_3
/*     */     //   750: getfield 94	intradoc/server/utils/RevisionSelectionParameters:m_errMsg	Ljava/lang/String;
/*     */     //   753: ifnonnull +72 -> 825
/*     */     //   756: aload_3
/*     */     //   757: getfield 19	intradoc/server/utils/RevisionSelectionParameters:m_docName	Ljava/lang/String;
/*     */     //   760: ifnull +10 -> 770
/*     */     //   763: aload_3
/*     */     //   764: getfield 19	intradoc/server/utils/RevisionSelectionParameters:m_docName	Ljava/lang/String;
/*     */     //   767: goto +7 -> 774
/*     */     //   770: aload_3
/*     */     //   771: getfield 18	intradoc/server/utils/RevisionSelectionParameters:m_id	Ljava/lang/String;
/*     */     //   774: astore 9
/*     */     //   776: aload_3
/*     */     //   777: ldc 95
/*     */     //   779: aconst_null
/*     */     //   780: new 96	java/lang/StringBuilder
/*     */     //   783: dup
/*     */     //   784: invokespecial 97	java/lang/StringBuilder:<init>	()V
/*     */     //   787: ldc 98
/*     */     //   789: invokevirtual 99	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
/*     */     //   792: aload 4
/*     */     //   794: invokevirtual 99	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
/*     */     //   797: invokevirtual 100	java/lang/StringBuilder:toString	()Ljava/lang/String;
/*     */     //   800: aload 9
/*     */     //   802: invokestatic 101	intradoc/common/LocaleUtils:encodeMessage	(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/String;
/*     */     //   805: putfield 94	intradoc/server/utils/RevisionSelectionParameters:m_errMsg	Ljava/lang/String;
/*     */     //   808: goto +17 -> 825
/*     */     //   811: aload_0
/*     */     //   812: ldc 16
/*     */     //   814: aload 8
/*     */     //   816: invokevirtual 44	intradoc/data/DataBinder:putLocal	(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
/*     */     //   819: pop
/*     */     //   820: aload_3
/*     */     //   821: iconst_1
/*     */     //   822: putfield 28	intradoc/server/utils/RevisionSelectionParameters:m_haveRevID	Z
/*     */     //   825: aload_3
/*     */     //   826: getfield 64	intradoc/server/utils/RevisionSelectionParameters:m_isError	Z
/*     */     //   829: ifne +465 -> 1294
/*     */     //   832: aload_3
/*     */     //   833: getfield 47	intradoc/server/utils/RevisionSelectionParameters:m_haveDocInfo	Z
/*     */     //   836: ifne +458 -> 1294
/*     */     //   839: aload_3
/*     */     //   840: getfield 11	intradoc/server/utils/RevisionSelectionParameters:m_computeDocInfo	Z
/*     */     //   843: ifeq +451 -> 1294
/*     */     //   846: aload_3
/*     */     //   847: getfield 28	intradoc/server/utils/RevisionSelectionParameters:m_haveRevID	Z
/*     */     //   850: ifeq +444 -> 1294
/*     */     //   853: aload_3
/*     */     //   854: getfield 102	intradoc/server/utils/RevisionSelectionParameters:m_docInfoQuery	Ljava/lang/String;
/*     */     //   857: ifnonnull +25 -> 882
/*     */     //   860: aload_3
/*     */     //   861: getfield 29	intradoc/server/utils/RevisionSelectionParameters:m_isBatchSelection	Z
/*     */     //   864: ifeq +12 -> 876
/*     */     //   867: aload_3
/*     */     //   868: ldc 103
/*     */     //   870: putfield 102	intradoc/server/utils/RevisionSelectionParameters:m_docInfoQuery	Ljava/lang/String;
/*     */     //   873: goto +9 -> 882
/*     */     //   876: aload_3
/*     */     //   877: ldc 104
/*     */     //   879: putfield 102	intradoc/server/utils/RevisionSelectionParameters:m_docInfoQuery	Ljava/lang/String;
/*     */     //   882: aload_3
/*     */     //   883: getfield 29	intradoc/server/utils/RevisionSelectionParameters:m_isBatchSelection	Z
/*     */     //   886: ifeq +276 -> 1162
/*     */     //   889: new 63	intradoc/data/DataResultSet
/*     */     //   892: dup
/*     */     //   893: invokespecial 105	intradoc/data/DataResultSet:<init>	()V
/*     */     //   896: astore 6
/*     */     //   898: iconst_0
/*     */     //   899: istore 7
/*     */     //   901: new 70	intradoc/common/IdcStringBuilder
/*     */     //   904: dup
/*     */     //   905: invokespecial 71	intradoc/common/IdcStringBuilder:<init>	()V
/*     */     //   908: astore 8
/*     */     //   910: iload 7
/*     */     //   912: aload_3
/*     */     //   913: getfield 30	intradoc/server/utils/RevisionSelectionParameters:m_ids	[Ljava/lang/String;
/*     */     //   916: arraylength
/*     */     //   917: if_icmpeq +11 -> 928
/*     */     //   920: iload 7
/*     */     //   922: bipush 25
/*     */     //   924: irem
/*     */     //   925: ifne +188 -> 1113
/*     */     //   928: aload 8
/*     */     //   930: invokevirtual 72	intradoc/common/IdcStringBuilder:length	()I
/*     */     //   933: ifle +167 -> 1100
/*     */     //   936: new 73	java/util/HashMap
/*     */     //   939: dup
/*     */     //   940: invokespecial 74	java/util/HashMap:<init>	()V
/*     */     //   943: astore 9
/*     */     //   945: aload 9
/*     */     //   947: ldc 16
/*     */     //   949: aload 8
/*     */     //   951: invokevirtual 75	intradoc/common/IdcStringBuilder:toString	()Ljava/lang/String;
/*     */     //   954: invokevirtual 76	java/util/HashMap:put	(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
/*     */     //   957: pop
/*     */     //   958: aload_2
/*     */     //   959: aload_3
/*     */     //   960: getfield 102	intradoc/server/utils/RevisionSelectionParameters:m_docInfoQuery	Ljava/lang/String;
/*     */     //   963: new 77	intradoc/data/MapParameters
/*     */     //   966: dup
/*     */     //   967: aload 9
/*     */     //   969: invokespecial 78	intradoc/data/MapParameters:<init>	(Ljava/util/Map;)V
/*     */     //   972: invokeinterface 79 3 0
/*     */     //   977: astore 10
/*     */     //   979: aload 6
/*     */     //   981: invokevirtual 106	intradoc/data/DataResultSet:getNumFields	()I
/*     */     //   984: ifne +22 -> 1006
/*     */     //   987: aload 6
/*     */     //   989: aload 10
/*     */     //   991: invokevirtual 107	intradoc/data/DataResultSet:copyFieldInfo	(Lintradoc/data/ResultSet;)V
/*     */     //   994: aload 6
/*     */     //   996: aload 10
/*     */     //   998: invokeinterface 108 1 0
/*     */     //   1003: invokevirtual 109	intradoc/data/DataResultSet:setDateFormat	(Lintradoc/common/IdcDateFormat;)V
/*     */     //   1006: aload 6
/*     */     //   1008: invokevirtual 106	intradoc/data/DataResultSet:getNumFields	()I
/*     */     //   1011: istore 11
/*     */     //   1013: aload 10
/*     */     //   1015: invokeinterface 81 1 0
/*     */     //   1020: pop
/*     */     //   1021: aload 10
/*     */     //   1023: invokeinterface 82 1 0
/*     */     //   1028: ifeq +63 -> 1091
/*     */     //   1031: aload 6
/*     */     //   1033: iload 11
/*     */     //   1035: invokevirtual 110	intradoc/data/DataResultSet:createNewRowList	(I)Ljava/util/List;
/*     */     //   1038: astore 12
/*     */     //   1040: iconst_0
/*     */     //   1041: istore 13
/*     */     //   1043: iload 13
/*     */     //   1045: iload 11
/*     */     //   1047: if_icmpge +26 -> 1073
/*     */     //   1050: aload 12
/*     */     //   1052: aload 10
/*     */     //   1054: iload 13
/*     */     //   1056: invokeinterface 83 2 0
/*     */     //   1061: invokeinterface 111 2 0
/*     */     //   1066: pop
/*     */     //   1067: iinc 13 1
/*     */     //   1070: goto -27 -> 1043
/*     */     //   1073: aload 6
/*     */     //   1075: aload 12
/*     */     //   1077: invokevirtual 112	intradoc/data/DataResultSet:addRowWithList	(Ljava/util/List;)V
/*     */     //   1080: aload 10
/*     */     //   1082: invokeinterface 85 1 0
/*     */     //   1087: pop
/*     */     //   1088: goto -67 -> 1021
/*     */     //   1091: new 70	intradoc/common/IdcStringBuilder
/*     */     //   1094: dup
/*     */     //   1095: invokespecial 71	intradoc/common/IdcStringBuilder:<init>	()V
/*     */     //   1098: astore 8
/*     */     //   1100: iload 7
/*     */     //   1102: aload_3
/*     */     //   1103: getfield 30	intradoc/server/utils/RevisionSelectionParameters:m_ids	[Ljava/lang/String;
/*     */     //   1106: arraylength
/*     */     //   1107: if_icmpne +6 -> 1113
/*     */     //   1110: goto +38 -> 1148
/*     */     //   1113: aload 8
/*     */     //   1115: invokevirtual 72	intradoc/common/IdcStringBuilder:length	()I
/*     */     //   1118: ifle +11 -> 1129
/*     */     //   1121: aload 8
/*     */     //   1123: bipush 44
/*     */     //   1125: invokevirtual 86	intradoc/common/IdcStringBuilder:append	(C)Lintradoc/common/IdcAppendable;
/*     */     //   1128: pop
/*     */     //   1129: aload 8
/*     */     //   1131: aload_3
/*     */     //   1132: getfield 30	intradoc/server/utils/RevisionSelectionParameters:m_ids	[Ljava/lang/String;
/*     */     //   1135: iload 7
/*     */     //   1137: aaload
/*     */     //   1138: invokevirtual 87	intradoc/common/IdcStringBuilder:append	(Ljava/lang/String;)Lintradoc/common/IdcAppendable;
/*     */     //   1141: pop
/*     */     //   1142: iinc 7 1
/*     */     //   1145: goto -235 -> 910
/*     */     //   1148: aload_3
/*     */     //   1149: aload 6
/*     */     //   1151: putfield 15	intradoc/server/utils/RevisionSelectionParameters:m_docInfo	Lintradoc/data/DataResultSet;
/*     */     //   1154: aload_3
/*     */     //   1155: iconst_1
/*     */     //   1156: putfield 47	intradoc/server/utils/RevisionSelectionParameters:m_haveDocInfo	Z
/*     */     //   1159: goto +135 -> 1294
/*     */     //   1162: aload_2
/*     */     //   1163: aload_3
/*     */     //   1164: getfield 102	intradoc/server/utils/RevisionSelectionParameters:m_docInfoQuery	Ljava/lang/String;
/*     */     //   1167: aload_0
/*     */     //   1168: invokeinterface 79 3 0
/*     */     //   1173: astore 6
/*     */     //   1175: aload 6
/*     */     //   1177: ifnull +43 -> 1220
/*     */     //   1180: aload 6
/*     */     //   1182: invokeinterface 113 1 0
/*     */     //   1187: ifne +33 -> 1220
/*     */     //   1190: new 63	intradoc/data/DataResultSet
/*     */     //   1193: dup
/*     */     //   1194: invokespecial 105	intradoc/data/DataResultSet:<init>	()V
/*     */     //   1197: astore 7
/*     */     //   1199: aload 7
/*     */     //   1201: aload 6
/*     */     //   1203: invokevirtual 114	intradoc/data/DataResultSet:copy	(Lintradoc/data/ResultSet;)V
/*     */     //   1206: aload_3
/*     */     //   1207: aload 7
/*     */     //   1209: putfield 15	intradoc/server/utils/RevisionSelectionParameters:m_docInfo	Lintradoc/data/DataResultSet;
/*     */     //   1212: aload_3
/*     */     //   1213: iconst_1
/*     */     //   1214: putfield 47	intradoc/server/utils/RevisionSelectionParameters:m_haveDocInfo	Z
/*     */     //   1217: goto +77 -> 1294
/*     */     //   1220: aload_3
/*     */     //   1221: getfield 19	intradoc/server/utils/RevisionSelectionParameters:m_docName	Ljava/lang/String;
/*     */     //   1224: ifnull +8 -> 1232
/*     */     //   1227: ldc 115
/*     */     //   1229: goto +5 -> 1234
/*     */     //   1232: ldc 116
/*     */     //   1234: astore 7
/*     */     //   1236: aload_3
/*     */     //   1237: getfield 19	intradoc/server/utils/RevisionSelectionParameters:m_docName	Ljava/lang/String;
/*     */     //   1240: ifnull +24 -> 1264
/*     */     //   1243: iconst_2
/*     */     //   1244: anewarray 117	java/lang/Object
/*     */     //   1247: dup
/*     */     //   1248: iconst_0
/*     */     //   1249: aload_3
/*     */     //   1250: getfield 19	intradoc/server/utils/RevisionSelectionParameters:m_docName	Ljava/lang/String;
/*     */     //   1253: aastore
/*     */     //   1254: dup
/*     */     //   1255: iconst_1
/*     */     //   1256: aload_3
/*     */     //   1257: getfield 18	intradoc/server/utils/RevisionSelectionParameters:m_id	Ljava/lang/String;
/*     */     //   1260: aastore
/*     */     //   1261: goto +14 -> 1275
/*     */     //   1264: iconst_1
/*     */     //   1265: anewarray 117	java/lang/Object
/*     */     //   1268: dup
/*     */     //   1269: iconst_0
/*     */     //   1270: aload_3
/*     */     //   1271: getfield 18	intradoc/server/utils/RevisionSelectionParameters:m_id	Ljava/lang/String;
/*     */     //   1274: aastore
/*     */     //   1275: astore 8
/*     */     //   1277: aload_3
/*     */     //   1278: iconst_1
/*     */     //   1279: putfield 64	intradoc/server/utils/RevisionSelectionParameters:m_isError	Z
/*     */     //   1282: aload_3
/*     */     //   1283: aload 7
/*     */     //   1285: aconst_null
/*     */     //   1286: aload 8
/*     */     //   1288: invokestatic 118	intradoc/common/LocaleUtils:encodeMessage	(Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;
/*     */     //   1291: putfield 94	intradoc/server/utils/RevisionSelectionParameters:m_errMsg	Ljava/lang/String;
/*     */     //   1294: aload_3
/*     */     //   1295: getfield 64	intradoc/server/utils/RevisionSelectionParameters:m_isError	Z
/*     */     //   1298: ifeq +81 -> 1379
/*     */     //   1301: ldc 119
/*     */     //   1303: aload_2
/*     */     //   1304: aload_0
/*     */     //   1305: aload_1
/*     */     //   1306: invokestatic 23	intradoc/shared/PluginFilters:filter	(Ljava/lang/String;Lintradoc/data/Workspace;Lintradoc/data/DataBinder;Lintradoc/common/ExecutionContext;)I
/*     */     //   1309: pop
/*     */     //   1310: goto +14 -> 1324
/*     */     //   1313: astore 6
/*     */     //   1315: ldc 5
/*     */     //   1317: ldc 121
/*     */     //   1319: aload 6
/*     */     //   1321: invokestatic 7	intradoc/common/Report:trace	(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)V
/*     */     //   1324: aload_0
/*     */     //   1325: ldc 122
/*     */     //   1327: iconst_0
/*     */     //   1328: invokestatic 58	intradoc/data/DataBinderUtils:getLocalBoolean	(Lintradoc/data/DataBinder;Ljava/lang/String;Z)Z
/*     */     //   1331: ifeq +8 -> 1339
/*     */     //   1334: bipush 192
/*     */     //   1336: goto +5 -> 1341
/*     */     //   1339: bipush 240
/*     */     //   1341: istore 6
/*     */     //   1343: new 123	intradoc/common/ServiceException
/*     */     //   1346: dup
/*     */     //   1347: iload 6
/*     */     //   1349: aload_3
/*     */     //   1350: getfield 94	intradoc/server/utils/RevisionSelectionParameters:m_errMsg	Ljava/lang/String;
/*     */     //   1353: invokespecial 124	intradoc/common/ServiceException:<init>	(ILjava/lang/String;)V
/*     */     //   1356: astore 7
/*     */     //   1358: aload_3
/*     */     //   1359: getfield 12	intradoc/server/utils/RevisionSelectionParameters:m_suppressErrorRetrievingDocInfo	Z
/*     */     //   1362: ifeq +14 -> 1376
/*     */     //   1365: ldc 125
/*     */     //   1367: aconst_null
/*     */     //   1368: aload 7
/*     */     //   1370: invokestatic 7	intradoc/common/Report:trace	(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)V
/*     */     //   1373: goto +6 -> 1379
/*     */     //   1376: aload 7
/*     */     //   1378: athrow
/*     */     //   1379: aload_3
/*     */     //   1380: getfield 47	intradoc/server/utils/RevisionSelectionParameters:m_haveDocInfo	Z
/*     */     //   1383: ifeq +131 -> 1514
/*     */     //   1386: aload_3
/*     */     //   1387: getfield 29	intradoc/server/utils/RevisionSelectionParameters:m_isBatchSelection	Z
/*     */     //   1390: ifne +124 -> 1514
/*     */     //   1393: aload_3
/*     */     //   1394: aload_3
/*     */     //   1395: getfield 15	intradoc/server/utils/RevisionSelectionParameters:m_docInfo	Lintradoc/data/DataResultSet;
/*     */     //   1398: ldc 2
/*     */     //   1400: invokestatic 93	intradoc/data/ResultSetUtils:getValue	(Lintradoc/data/ResultSet;Ljava/lang/String;)Ljava/lang/String;
/*     */     //   1403: putfield 19	intradoc/server/utils/RevisionSelectionParameters:m_docName	Ljava/lang/String;
/*     */     //   1406: aload_3
/*     */     //   1407: aload_3
/*     */     //   1408: getfield 15	intradoc/server/utils/RevisionSelectionParameters:m_docInfo	Lintradoc/data/DataResultSet;
/*     */     //   1411: ldc 16
/*     */     //   1413: invokestatic 93	intradoc/data/ResultSetUtils:getValue	(Lintradoc/data/ResultSet;Ljava/lang/String;)Ljava/lang/String;
/*     */     //   1416: putfield 18	intradoc/server/utils/RevisionSelectionParameters:m_id	Ljava/lang/String;
/*     */     //   1419: aload_0
/*     */     //   1420: ldc 2
/*     */     //   1422: aload_3
/*     */     //   1423: getfield 19	intradoc/server/utils/RevisionSelectionParameters:m_docName	Ljava/lang/String;
/*     */     //   1426: invokevirtual 44	intradoc/data/DataBinder:putLocal	(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
/*     */     //   1429: pop
/*     */     //   1430: aload_0
/*     */     //   1431: ldc 16
/*     */     //   1433: aload_3
/*     */     //   1434: getfield 18	intradoc/server/utils/RevisionSelectionParameters:m_id	Ljava/lang/String;
/*     */     //   1437: invokevirtual 44	intradoc/data/DataBinder:putLocal	(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
/*     */     //   1440: pop
/*     */     //   1441: iload 5
/*     */     //   1443: ifeq +15 -> 1458
/*     */     //   1446: aload_0
/*     */     //   1447: aload_3
/*     */     //   1448: getfield 45	intradoc/server/utils/RevisionSelectionParameters:m_queryKey	Ljava/lang/String;
/*     */     //   1451: invokevirtual 126	intradoc/data/DataBinder:removeResultSet	(Ljava/lang/String;)Lintradoc/data/ResultSet;
/*     */     //   1454: pop
/*     */     //   1455: goto +59 -> 1514
/*     */     //   1458: aload_0
/*     */     //   1459: aload_3
/*     */     //   1460: getfield 45	intradoc/server/utils/RevisionSelectionParameters:m_queryKey	Ljava/lang/String;
/*     */     //   1463: aload_3
/*     */     //   1464: getfield 15	intradoc/server/utils/RevisionSelectionParameters:m_docInfo	Lintradoc/data/DataResultSet;
/*     */     //   1467: invokevirtual 127	intradoc/data/DataBinder:addResultSet	(Ljava/lang/String;Lintradoc/data/ResultSet;)Lintradoc/data/ResultSet;
/*     */     //   1470: pop
/*     */     //   1471: new 128	intradoc/serialize/DataBinderLocalizer
/*     */     //   1474: dup
/*     */     //   1475: aload_0
/*     */     //   1476: aload_1
/*     */     //   1477: invokespecial 129	intradoc/serialize/DataBinderLocalizer:<init>	(Lintradoc/data/DataBinder;Lintradoc/common/ExecutionContext;)V
/*     */     //   1480: astore 6
/*     */     //   1482: aload 6
/*     */     //   1484: aload_3
/*     */     //   1485: getfield 15	intradoc/server/utils/RevisionSelectionParameters:m_docInfo	Lintradoc/data/DataResultSet;
/*     */     //   1488: aload_0
/*     */     //   1489: getfield 130	intradoc/data/DataBinder:m_blDateFormat	Lintradoc/common/IdcDateFormat;
/*     */     //   1492: iconst_1
/*     */     //   1493: invokevirtual 131	intradoc/serialize/DataBinderLocalizer:coerceResultSet	(Lintradoc/data/ResultSet;Lintradoc/common/IdcDateFormat;I)Lintradoc/data/DataResultSet;
/*     */     //   1496: astore 7
/*     */     //   1498: aload 7
/*     */     //   1500: ifnull +14 -> 1514
/*     */     //   1503: aload_0
/*     */     //   1504: aload_3
/*     */     //   1505: getfield 45	intradoc/server/utils/RevisionSelectionParameters:m_queryKey	Ljava/lang/String;
/*     */     //   1508: aload 7
/*     */     //   1510: invokevirtual 127	intradoc/data/DataBinder:addResultSet	(Ljava/lang/String;Lintradoc/data/ResultSet;)Lintradoc/data/ResultSet;
/*     */     //   1513: pop
/*     */     //   1514: return
/*     */     //
/*     */     // Exception table:
/*     */     //   from	to	target	type
/*     */     //   1301	1310	1313	java/lang/Exception } 
/* 531 */   public static String getBinderValue(String key, DataBinder binder, RevisionSelectionParameters params) throws DataException { return binder.getAllowMissing(key); }
/*     */ 
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 536 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 94227 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.utils.FileRevisionSelectionUtils
 * JD-Core Version:    0.5.4
 */