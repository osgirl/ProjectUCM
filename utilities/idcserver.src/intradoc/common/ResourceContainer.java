/*      */ package intradoc.common;
/*      */ 
/*      */ import intradoc.util.IdcPerfectHash;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.io.CharArrayReader;
/*      */ import java.io.IOException;
/*      */ import java.io.Reader;
/*      */ import java.io.StringReader;
/*      */ import java.util.ArrayList;
/*      */ import java.util.HashMap;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Properties;
/*      */ import java.util.Random;
/*      */ import java.util.StringTokenizer;
/*      */ import java.util.Vector;
/*      */ import java.util.concurrent.ConcurrentHashMap;
/*      */ 
/*      */ public class ResourceContainer
/*      */   implements HtmlResourceBinder
/*      */ {
/*      */   public Map m_dynamicHtml;
/*      */   public Map m_dynamicHtmlOL;
/*      */   public List m_dynamicHtmlList;
/*      */   public List m_dynamicDataList;
/*      */   public Map m_apps;
/*      */   public List m_stringsList;
/*      */   public Map m_stringsOL;
/*      */   public Map m_stringArrays;
/*      */   public Map m_stringArraysOL;
/*      */   public Map<String, Table> m_tables;
/*      */   public Map m_tablesOL;
/*      */   public List m_xmlNodes;
/*      */   public List m_defaultTags;
/*      */   public StreamEventHandler m_handler;
/*      */   public boolean m_dynamicHtmlLoaded;
/*      */   public boolean m_dynamicResourceLoaded;
/*      */   public boolean m_dynamicStringLoaded;
/*      */   public List m_resourceList;
/*      */   public Map m_resourceMap;
/*  143 */   public boolean m_unencodeResourceStrings = true;
/*      */   public IdcPerfectHash m_languages;
/*      */   public Map<String, IdcLocaleString> m_stringObjMap;
/*      */   public boolean m_isFullyLoadedSharedResources;
/*      */   public static final short DECODE_REMOVE_EXCESS_BACKSLASHES = 1;
/*      */   public static final short LOAD_STRINGS_FOR_TRANSLATION = 2;
/*      */   public static final short RES_STRING = 0;
/*      */   public static final short RES_TABLE = 1;
/*      */   public static final short RES_DYNAMIC_HTML = 2;
/*      */   public static final short RES_DYNAMIC_DATA = 3;
/*      */   public static final short RES_STRINGLIST = 4;
/*      */   public static final short RES_STRINGARRAY = 5;
/*      */   public static final short RES_END = 6;
/*      */   public static final short RES_COMMENT = 7;
/*  182 */   public static final String[] RESOURCE_TYPES = { "string", "table", "dynamichtml", "dynamicdata", "stringlist", "stringarray", "end", "comment" };
/*      */ 
/*  187 */   public static final char[] DD_TAG = { 'd', 'd' };
/*  188 */   public static final char[] SLASHDD_TAG = { '/', 'd', 'd' };
/*  189 */   public static final char[] TD_TAG = { 't', 'd' };
/*  190 */   public static final char[] SLASHTD_TAG = { '/', 't', 'd' };
/*  191 */   public static final char[] TR_TAG = { 't', 'r' };
/*  192 */   public static final char[] SLASHTR_TAG = { '/', 't', 'r' };
/*  193 */   public static final char[] TABLE_TAG = { 't', 'a', 'b', 'l', 'e' };
/*  194 */   public static final char[] SLASHTABLE_TAG = { '/', 't', 'a', 'b', 'l', 'e' };
/*  195 */   public static final char[][] ENDROW_TAGS = { SLASHTABLE_TAG, TR_TAG, SLASHTR_TAG };
/*  196 */   public static final char[][] ITEM_TAGS = { SLASHTABLE_TAG, TR_TAG, SLASHTR_TAG, TD_TAG, SLASHTD_TAG, TABLE_TAG };
/*  197 */   public static final char[][] NESTED_TABLE_TAGS = { TABLE_TAG, SLASHTABLE_TAG };
/*      */ 
/*      */   public ResourceContainer()
/*      */   {
/*  201 */     this.m_dynamicHtml = new ConcurrentHashMap();
/*  202 */     this.m_dynamicHtmlOL = new ConcurrentHashMap();
/*  203 */     this.m_dynamicHtmlList = new IdcVector();
/*  204 */     this.m_dynamicDataList = new IdcVector();
/*  205 */     this.m_apps = new HashMap();
/*  206 */     this.m_stringsList = new IdcVector();
/*  207 */     this.m_stringsOL = new ConcurrentHashMap();
/*  208 */     this.m_stringArrays = new ConcurrentHashMap();
/*  209 */     this.m_stringArraysOL = new ConcurrentHashMap();
/*  210 */     this.m_tables = new ConcurrentHashMap();
/*  211 */     this.m_tablesOL = new ConcurrentHashMap();
/*  212 */     this.m_xmlNodes = new IdcVector();
/*  213 */     this.m_dynamicHtmlLoaded = false;
/*  214 */     this.m_dynamicResourceLoaded = false;
/*  215 */     this.m_dynamicStringLoaded = false;
/*  216 */     this.m_resourceList = new IdcVector();
/*  217 */     this.m_resourceMap = new ConcurrentHashMap();
/*  218 */     this.m_defaultTags = new IdcVector();
/*  219 */     this.m_languages = new IdcPerfectHash(new Random(System.currentTimeMillis()));
/*  220 */     this.m_stringObjMap = new ConcurrentHashMap();
/*      */   }
/*      */ 
/*      */   public void resetStrings()
/*      */   {
/*  230 */     this.m_stringsList = new IdcVector();
/*  231 */     this.m_stringsOL = new ConcurrentHashMap();
/*  232 */     this.m_stringArrays = new ConcurrentHashMap();
/*  233 */     this.m_stringArraysOL = new ConcurrentHashMap();
/*  234 */     this.m_resourceList = new IdcVector();
/*  235 */     this.m_resourceMap = new ConcurrentHashMap();
/*  236 */     this.m_languages = new IdcPerfectHash(new Random(System.currentTimeMillis()));
/*  237 */     this.m_stringObjMap = new ConcurrentHashMap();
/*  238 */     this.m_xmlNodes = new IdcVector();
/*  239 */     this.m_resourceMap = new ConcurrentHashMap();
/*  240 */     this.m_defaultTags = new IdcVector();
/*      */   }
/*      */ 
/*      */   public void parseAndAddResources(Reader reader, String filename)
/*      */     throws IOException, ParseSyntaxException
/*      */   {
/*  249 */     parseAndAddResourcesInternal(reader, filename, null, null, 0);
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public void parseAndAddResourcesWithFlags(Reader reader, String filename, int flags)
/*      */     throws IOException, ParseSyntaxException
/*      */   {
/*  257 */     IdcBreakpoints bp = null;
/*  258 */     parseAndAddResourcesWithFlags(reader, filename, null, bp, flags);
/*      */   }
/*      */ 
/*      */   public void parseAndAddResourcesWithFlags(Reader reader, String filename, String langId, IdcBreakpoints bp, int flags)
/*      */     throws IOException, ParseSyntaxException
/*      */   {
/*  265 */     parseAndAddResourcesInternal(reader, filename, langId, bp, flags);
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public void parseAndAddResourcesEx(Reader reader, String filename, String langId, IdcBreakpoints bp)
/*      */     throws IOException, ParseSyntaxException
/*      */   {
/*  273 */     parseAndAddResourcesInternal(reader, filename, langId, bp, 0);
/*      */   }
/*      */ 
/*      */   protected void parseAndAddResourcesInternal(Reader reader, String filename, String langId, IdcBreakpoints bp, int parseFlags)
/*      */     throws IOException, ParseSyntaxException
/*      */   {
/*  279 */     String defaultLangId = langId;
/*  280 */     String appId = null;
/*      */ 
/*  283 */     String[] defaultAttributeNames = new String[0];
/*  284 */     String[] defaultAttributeValues = new String[0];
/*      */ 
/*  287 */     ResourceTrace.msg(LocaleUtils.encodeMessage("csComponentLoad", null, filename));
/*      */ 
/*  290 */     this.m_dynamicHtmlLoaded = false;
/*  291 */     this.m_dynamicResourceLoaded = false;
/*  292 */     this.m_dynamicStringLoaded = false;
/*      */ 
/*  294 */     this.m_resourceList = new IdcVector();
/*  295 */     this.m_resourceMap = new ConcurrentHashMap();
/*      */ 
/*  298 */     ResourceContainerParams params = new ResourceContainerParams();
/*  299 */     IdcStringBuilder builder = new IdcStringBuilder();
/*  300 */     builder.m_disableToStringReleaseBuffers = true;
/*  301 */     String resName = null;
/*  302 */     String lastResName = null;
/*      */ 
/*  305 */     IdcCharArrayWriter outbuf = new IdcCharArrayWriter();
/*  306 */     ParseOutput parseOutput = new ParseOutput();
/*  307 */     parseOutput.m_parseInfo.m_fileName = filename;
/*  308 */     parseOutput.m_writer = outbuf;
/*      */ 
/*  312 */     parseOutput.m_noLiteralStrings = true;
/*  313 */     ResourceOptions resOptions = null;
/*      */     try
/*      */     {
/*  318 */       while (findResourceScriptTag(reader, parseOutput, params, parseFlags))
/*      */       {
/*  321 */         params.m_isEnd = false;
/*      */ 
/*  323 */         ArrayList tokens = new ArrayList();
/*  324 */         String value = null;
/*  325 */         int start = parseOutput.m_readOffset;
/*  326 */         int stop = parseOutput.m_readOffset + parseOutput.m_numWaiting - 1;
/*  327 */         int firstNonSpace = -1;
/*  328 */         int lastNonSpace = -1;
/*  329 */         boolean skippingSpaces = true;
/*  330 */         boolean gettingValue = false;
/*  331 */         boolean hasPropertiesString = false;
/*  332 */         for (int i = start; i < stop; ++i)
/*      */         {
/*  334 */           char c = parseOutput.m_outputBuf[i];
/*      */           boolean isSpace;
/*  336 */           switch (c)
/*      */           {
/*      */           case '\t':
/*      */           case '\n':
/*      */           case '\f':
/*      */           case '\r':
/*      */           case ' ':
/*  343 */             isSpace = true;
/*  344 */             break;
/*      */           default:
/*  346 */             isSpace = false;
/*      */           }
/*      */ 
/*  350 */           if (skippingSpaces)
/*      */           {
/*  352 */             if (isSpace)
/*      */             {
/*      */               continue;
/*      */             }
/*      */ 
/*  358 */             firstNonSpace = i;
/*  359 */             lastNonSpace = i;
/*  360 */             skippingSpaces = false;
/*  361 */             if (tokens.size() != 2) {
/*      */               continue;
/*      */             }
/*      */ 
/*  365 */             gettingValue = true;
/*  366 */             hasPropertiesString = true;
/*      */           }
/*      */           else
/*      */           {
/*  372 */             if (!isSpace)
/*      */             {
/*  374 */               lastNonSpace = i;
/*      */             }
/*      */ 
/*  377 */             if (gettingValue)
/*      */             {
/*      */               continue;
/*      */             }
/*      */ 
/*  383 */             if (c == '=')
/*      */             {
/*  385 */               String str = new String(parseOutput.m_outputBuf, firstNonSpace, lastNonSpace - firstNonSpace);
/*      */ 
/*  387 */               tokens.add(str);
/*  388 */               gettingValue = true;
/*  389 */               firstNonSpace = -1;
/*  390 */               skippingSpaces = true;
/*      */             }
/*      */             else
/*      */             {
/*  394 */               if (!isSpace)
/*      */                 continue;
/*  396 */               String str = new String(parseOutput.m_outputBuf, firstNonSpace, lastNonSpace - firstNonSpace + 1);
/*      */ 
/*  398 */               tokens.add(str);
/*  399 */               firstNonSpace = -1;
/*  400 */               skippingSpaces = true;
/*      */             }
/*      */ 
/*      */           }
/*      */ 
/*      */         }
/*      */ 
/*  411 */         if (hasPropertiesString)
/*      */         {
/*  413 */           if (resOptions == null)
/*      */           {
/*  415 */             resOptions = new ResourceOptions();
/*      */           }
/*  417 */           int flags = 3;
/*      */ 
/*  419 */           DynamicDataUtils.parseSpecialXmlTag(null, null, parseOutput.m_outputBuf, firstNonSpace, lastNonSpace + 1, resOptions, null, flags);
/*      */         }
/*  424 */         else if ((firstNonSpace >= 0) && (lastNonSpace >= firstNonSpace))
/*      */         {
/*  426 */           String tmp = new String(parseOutput.m_outputBuf, firstNonSpace, lastNonSpace - firstNonSpace + 1);
/*      */ 
/*  428 */           if (gettingValue)
/*      */           {
/*  430 */             value = tmp;
/*      */           }
/*      */           else
/*      */           {
/*  434 */             tokens.add(tmp);
/*      */           }
/*      */         }
/*  437 */         else if (gettingValue)
/*      */         {
/*  439 */           value = "";
/*      */         }
/*      */ 
/*  444 */         Reader resReader = null;
/*      */ 
/*  447 */         String resString = null;
/*      */ 
/*  450 */         String token = null;
/*  451 */         if (tokens.size() > 0)
/*      */         {
/*  453 */           token = (String)tokens.get(0);
/*      */         }
/*  455 */         if (token != null)
/*      */         {
/*  457 */           String[] options = RESOURCE_TYPES;
/*  458 */           boolean foundMatch = false;
/*  459 */           int selOption = -1;
/*      */ 
/*  461 */           for (int i = 0; i < options.length; ++i)
/*      */           {
/*  463 */             if (!options[i].equals(token))
/*      */               continue;
/*  465 */             foundMatch = true;
/*  466 */             selOption = i;
/*  467 */             break;
/*      */           }
/*      */ 
/*  470 */           if ((!foundMatch) && (value == null))
/*      */           {
/*  472 */             if (token.startsWith("!"))
/*      */             {
/*  474 */               value = "";
/*      */             }
/*      */             else
/*      */             {
/*  478 */               parseOutput.createParsingException("!csResourceContainerUnknownTag");
/*      */             }
/*      */           }
/*  481 */           if (selOption == 7)
/*      */           {
/*  483 */             parseOutput.copyToPending(false, false);
/*      */           }
/*      */ 
/*  487 */           params.m_isEnd = (selOption == 6);
/*      */ 
/*  489 */           if (params.m_inResource)
/*      */           {
/*  491 */             if (params.m_isEnd)
/*      */             {
/*  493 */               parseOutput.writePending();
/*  494 */               if (params.m_curResource != 0)
/*      */               {
/*  496 */                 resReader = new CharArrayReader(outbuf.m_charArray, 0, outbuf.m_length);
/*      */               }
/*      */               else
/*      */               {
/*  501 */                 String t = outbuf.toString();
/*  502 */                 resString = unencodeResourceString(t);
/*      */               }
/*      */             }
/*      */             else
/*      */             {
/*  507 */               parseOutput.createParsingException("!csResourceContainerTagWithinTag");
/*      */             }
/*      */           }
/*      */           else
/*      */           {
/*  512 */             if (params.m_isEnd)
/*      */             {
/*  514 */               parseOutput.createParsingException("!csResourceContainerEndTagWithoutStartTag");
/*      */             }
/*      */ 
/*  517 */             parseOutput.copyToPending(false, false);
/*  518 */             parseOutput.clearPending();
/*  519 */             outbuf.reset();
/*      */           }
/*      */ 
/*  524 */           if ((!params.m_inResource) && (!params.m_isEnd))
/*      */           {
/*  526 */             params.m_curResource = selOption;
/*  527 */             parseOutput.markParseLocation();
/*      */ 
/*  529 */             resName = null;
/*  530 */             if (foundMatch)
/*      */             {
/*  532 */               if (tokens.size() > 0)
/*      */               {
/*  534 */                 resName = (String)tokens.get(1);
/*      */               }
/*      */             }
/*      */             else
/*      */             {
/*  539 */               resName = token;
/*  540 */               params.m_curResource = 0;
/*      */             }
/*      */ 
/*  543 */             if (resName != null)
/*      */             {
/*  545 */               if (value != null)
/*      */               {
/*  547 */                 if (params.m_curResource == 0)
/*      */                 {
/*  549 */                   resString = unencodeResourceString(value);
/*      */                 }
/*      */                 else
/*      */                 {
/*  553 */                   resReader = new StringReader(value);
/*      */                 }
/*      */               }
/*      */               else
/*      */               {
/*  558 */                 params.m_inScriptResource = ((params.m_curResource == 2) || (params.m_curResource == 3));
/*  559 */                 params.m_inResource = true;
/*      */               }
/*      */             }
/*      */           }
/*      */ 
/*  564 */           if ((resReader != null) || (resString != null))
/*      */           {
/*  566 */             if (resName != null)
/*      */             {
/*  568 */               if (resReader != null)
/*      */               {
/*  570 */                 if (langId != null)
/*      */                 {
/*  572 */                   parseOutput.createParsingException("!csResourceContainerIllegalResource");
/*      */                 }
/*  574 */                 addResourceEx(resName, resReader, params.m_curResource, resOptions, parseOutput.m_markParseInfo, bp);
/*      */               }
/*      */               else
/*      */               {
/*  580 */                 ResourceObject resourceObj = new ResourceObject();
/*  581 */                 resourceObj.m_name = resName;
/*  582 */                 if ((appId != null) && (resName.indexOf(47) == -1))
/*      */                 {
/*  584 */                   resourceObj.m_name = (resName + "/" + appId);
/*      */                 }
/*  586 */                 resourceObj.m_type = 0;
/*  587 */                 resourceObj.m_resource = resString;
/*      */ 
/*  589 */                 if (resName.startsWith("!"))
/*      */                 {
/*  591 */                   if (resName.equals("!reset"))
/*      */                   {
/*  593 */                     addDefaultAttributes(builder, lastResName, defaultAttributeNames, defaultAttributeValues);
/*      */ 
/*  596 */                     langId = defaultLangId;
/*  597 */                     appId = null;
/*  598 */                     defaultAttributeNames = defaultAttributeValues = new String[0];
/*      */                   }
/*      */ 
/*  601 */                   if (resName.equals("!lang"))
/*      */                   {
/*  603 */                     langId = resString;
/*      */                   }
/*  605 */                   if (resName.equals("!app"))
/*      */                   {
/*  607 */                     appId = resString;
/*      */                   }
/*  609 */                   if ((resName.startsWith("![")) && (resName.endsWith("]")))
/*      */                   {
/*  611 */                     String attributeName = resName.substring(2, resName.length() - 1).intern();
/*      */ 
/*  613 */                     resString = resString.intern();
/*  614 */                     boolean append = true;
/*  615 */                     for (int i = 0; i < defaultAttributeNames.length; ++i)
/*      */                     {
/*  617 */                       if (!attributeName.equals(defaultAttributeNames[i]))
/*      */                         continue;
/*  619 */                       defaultAttributeValues[i] = resString;
/*  620 */                       append = false;
/*  621 */                       break;
/*      */                     }
/*      */ 
/*  624 */                     if (append)
/*      */                     {
/*  626 */                       String[] tmp = defaultAttributeNames;
/*  627 */                       defaultAttributeNames = new String[tmp.length + 1];
/*  628 */                       System.arraycopy(tmp, 0, defaultAttributeNames, 0, tmp.length);
/*  629 */                       defaultAttributeNames[tmp.length] = attributeName;
/*      */ 
/*  631 */                       tmp = defaultAttributeValues;
/*  632 */                       defaultAttributeValues = new String[tmp.length + 1];
/*  633 */                       System.arraycopy(tmp, 0, defaultAttributeValues, 0, tmp.length);
/*  634 */                       defaultAttributeValues[tmp.length] = resString;
/*      */                     }
/*      */                   }
/*  637 */                   if ((resName.startsWith("!+")) || (resName.startsWith("!-")))
/*      */                   {
/*  639 */                     List[] tagList = StringUtils.splitString(resName.substring(1), new char[] { '+', '-' }, 0);
/*      */ 
/*  641 */                     this.m_defaultTags.addAll(tagList[0]);
/*  642 */                     this.m_defaultTags.removeAll(tagList[1]);
/*      */                   }
/*      */ 
/*  646 */                   String msg = LocaleUtils.encodeMessage("csResourceContainerUnknownTag", null);
/*      */ 
/*  648 */                   parseOutput.createParsingException(msg);
/*      */                 }
/*      */ 
/*  652 */                 int bracketIndex = resName.indexOf("[");
/*  653 */                 if ((bracketIndex >= 0) && (!resName.endsWith("]")))
/*      */                 {
/*  655 */                   bracketIndex = -1;
/*  656 */                   String msg = LocaleUtils.encodeMessage("csResourceContainerUnknownTag", null);
/*      */ 
/*  658 */                   parseOutput.createParsingException(msg);
/*      */                 }
/*      */ 
/*  661 */                 Vector addTagList = new IdcVector();
/*  662 */                 Vector delTagList = new IdcVector();
/*  663 */                 String attributeName = null;
/*  664 */                 if (bracketIndex >= 0)
/*      */                 {
/*  666 */                   attributeName = resName.substring(bracketIndex + 1, resName.length() - 1);
/*      */ 
/*  668 */                   if (bracketIndex == 0)
/*      */                   {
/*  670 */                     resName = lastResName;
/*      */                   }
/*      */                   else
/*      */                   {
/*  674 */                     resName = resName.substring(0, bracketIndex);
/*      */                   }
/*      */ 
/*  677 */                   if ((attributeName.startsWith("+")) || (attributeName.startsWith("-")))
/*      */                   {
/*  679 */                     List[] tagList = StringUtils.splitString(attributeName, new char[] { '+', '-' }, 0);
/*      */ 
/*  681 */                     addTagList.addAll(tagList[0]);
/*  682 */                     delTagList.addAll(tagList[1]);
/*      */ 
/*  685 */                     attributeName = null;
/*      */                   }
/*      */                   else
/*      */                   {
/*  689 */                     attributeName = attributeName.intern();
/*  690 */                     resString = resString.intern();
/*      */                   }
/*      */                 }
/*      */ 
/*  694 */                 boolean updatedApp = false;
/*  695 */                 int index = resName.lastIndexOf(".");
/*  696 */                 String strLangId = langId;
/*  697 */                 if (index >= 0)
/*      */                 {
/*  699 */                   String prefix = resName.substring(0, index);
/*  700 */                   if ((langId != null) && (!prefix.equals(langId)))
/*      */                   {
/*  702 */                     parseOutput.createParsingException(LocaleUtils.encodeMessage("csResourceContainerIllegalLang", null, prefix));
/*      */                   }
/*      */ 
/*  706 */                   strLangId = resName.substring(0, index);
/*  707 */                   resName = resName.substring(index + 1);
/*      */                 }
/*  709 */                 boolean setLangToBase = false;
/*  710 */                 if ((strLangId == null) || (strLangId.length() == 0))
/*      */                 {
/*  712 */                   strLangId = LocaleResources.m_baseLanguage;
/*  713 */                   setLangToBase = true;
/*      */                 }
/*  715 */                 int langIndex = this.m_languages.getCode(strLangId);
/*  716 */                 if (langIndex == -1)
/*      */                 {
/*  718 */                   this.m_languages.add(strLangId);
/*  719 */                   langIndex = this.m_languages.getCode(strLangId);
/*      */                 }
/*  721 */                 if ((appId != null) && (resName.indexOf("/") == -1))
/*      */                 {
/*  723 */                   builder.setLength(0);
/*  724 */                   builder.append(resName);
/*  725 */                   builder.append2('/', appId);
/*  726 */                   resName = builder.toString();
/*  727 */                   this.m_apps.put(appId, appId);
/*  728 */                   updatedApp = true;
/*      */                 }
/*      */ 
/*  731 */                 if (!updatedApp)
/*      */                 {
/*  733 */                   index = resName.indexOf("/");
/*  734 */                   if (index > 0)
/*      */                   {
/*  736 */                     String tmpApp = resName.substring(index + 1);
/*  737 */                     int index2 = tmpApp.indexOf(91);
/*  738 */                     if (index2 > 0)
/*      */                     {
/*  740 */                       tmpApp = tmpApp.substring(0, index2);
/*      */                     }
/*  742 */                     if (tmpApp.equals("ext"))
/*      */                     {
/*  744 */                       Report.deprecatedUsage("/ext application strings are deprecated.  Use the [+extended] tag instead for string: " + resName);
/*      */ 
/*  749 */                       resName = resName.substring(0, index);
/*  750 */                       addTagList.add("extended");
/*      */                     }
/*      */                     else
/*      */                     {
/*  754 */                       this.m_apps.put(tmpApp, tmpApp);
/*      */                     }
/*      */                   }
/*      */                 }
/*      */ 
/*  759 */                 resourceObj.m_name = resName;
/*      */ 
/*  761 */                 if (validateStringTags(addTagList, strLangId, parseFlags))
/*      */                 {
/*  763 */                   this.m_resourceList.add(resourceObj);
/*  764 */                   this.m_resourceMap.put(resourceObj.m_name, resourceObj);
/*      */ 
/*  766 */                   boolean created = false;
/*  767 */                   IdcLocaleString strObj = (IdcLocaleString)this.m_stringObjMap.get(resName);
/*  768 */                   if (strObj == null)
/*      */                   {
/*  771 */                     created = true;
/*  772 */                     strObj = new IdcLocaleString(resName);
/*  773 */                     index = resName.indexOf("/");
/*  774 */                     if (index > 0)
/*      */                     {
/*  776 */                       String baseKey = resName.substring(0, index);
/*  777 */                       IdcLocaleString baseObj = (IdcLocaleString)this.m_stringObjMap.get(baseKey);
/*  778 */                       if (baseObj != null)
/*      */                       {
/*  780 */                         if (baseObj.m_attributes == null)
/*      */                         {
/*  782 */                           baseObj.m_attributes = new HashMap();
/*      */                         }
/*  784 */                         strObj.m_attributes = baseObj.m_attributes;
/*      */                       }
/*      */                     }
/*  787 */                     for (int i = 0; i < this.m_defaultTags.size(); ++i)
/*      */                     {
/*  789 */                       strObj.setAttribute((String)this.m_defaultTags.get(i), "true");
/*      */                     }
/*      */                   }
/*  792 */                   if (attributeName == null)
/*      */                   {
/*  794 */                     strObj.setLangValue(langIndex, resString);
/*      */                   }
/*      */                   else
/*      */                   {
/*  798 */                     strObj.setAttribute(attributeName, resString);
/*      */                   }
/*      */ 
/*  801 */                   for (int i = 0; i < addTagList.size(); ++i)
/*      */                   {
/*  803 */                     strObj.setAttribute((String)addTagList.get(i), "true");
/*      */                   }
/*  805 */                   for (int i = 0; i < delTagList.size(); ++i)
/*      */                   {
/*  807 */                     strObj.setAttribute((String)delTagList.get(i), null);
/*      */                   }
/*  809 */                   builder.setLength(0);
/*  810 */                   if (!setLangToBase)
/*      */                   {
/*  812 */                     builder.append2(strLangId, '.');
/*      */                   }
/*  814 */                   if (attributeName == null)
/*      */                   {
/*  816 */                     builder.append(resName);
/*      */                   }
/*      */                   else
/*      */                   {
/*  820 */                     builder.append2(resName, '[');
/*  821 */                     builder.append2(attributeName, ']');
/*      */                   }
/*      */ 
/*  828 */                   if (created)
/*      */                   {
/*  830 */                     this.m_stringObjMap.put(resName, strObj);
/*      */ 
/*  834 */                     this.m_stringsList.add(resName);
/*      */                   }
/*  836 */                   this.m_dynamicHtmlLoaded = true;
/*  837 */                   this.m_dynamicStringLoaded = true;
/*      */ 
/*  839 */                   if ((lastResName == null) || (!resName.equals(lastResName)))
/*      */                   {
/*  841 */                     if (lastResName != null)
/*      */                     {
/*  843 */                       addDefaultAttributes(builder, lastResName, defaultAttributeNames, defaultAttributeValues);
/*      */                     }
/*      */ 
/*  846 */                     lastResName = resName;
/*      */                   }
/*      */                 }
/*      */               }
/*      */             }
/*  851 */             params.m_inScriptResource = false;
/*  852 */             params.m_inResource = false;
/*      */           }
/*      */         }
/*  855 */         if (!params.m_inScriptResource)
/*      */         {
/*  857 */           resOptions = null;
/*      */         }
/*      */       }
/*  860 */       if (lastResName != null)
/*      */       {
/*  862 */         addDefaultAttributes(builder, lastResName, defaultAttributeNames, defaultAttributeValues);
/*      */       }
/*      */ 
/*  866 */       if (params.m_inResource)
/*      */       {
/*  868 */         parseOutput.createMarkedParsingException("!csResourceContainerDefNotTerminated");
/*      */       }
/*      */     }
/*      */     finally
/*      */     {
/*  873 */       builder.releaseBuffers();
/*  874 */       outbuf.releaseBuffers();
/*  875 */       parseOutput.releaseBuffers();
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void addDefaultAttributes(IdcStringBuilder builder, String lastResName, String[] defaultAttributeNames, String[] defaultAttributeValues)
/*      */   {
/*  882 */     IdcLocaleString strObj = (IdcLocaleString)this.m_stringObjMap.get(lastResName);
/*  883 */     for (int i = 0; i < defaultAttributeNames.length; ++i)
/*      */     {
/*  885 */       if (strObj.getAttribute(defaultAttributeNames[i]) != null) {
/*      */         continue;
/*      */       }
/*      */ 
/*  889 */       strObj.setAttribute(defaultAttributeNames[i], defaultAttributeValues[i]);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected boolean findResourceScriptTag(Reader reader, ParseOutput parseOutput, ResourceContainerParams params, int flags)
/*      */     throws IOException, ParseSyntaxException
/*      */   {
/*  907 */     ResourceObject oldResource = null;
/*  908 */     if ((!params.m_inResource) && (this.m_resourceList.size() > 0))
/*      */     {
/*  910 */       oldResource = (ResourceObject)this.m_resourceList.get(this.m_resourceList.size() - 1);
/*      */     }
/*      */ 
/*  913 */     boolean retVal = false;
/*  914 */     if (params.m_inScriptResource)
/*      */     {
/*  919 */       retVal = Parser.findScriptTag(reader, parseOutput, '@', null, null);
/*      */     }
/*      */     else
/*      */     {
/*  924 */       retVal = Parser.findScriptTag(reader, parseOutput, '@', DynamicHtmlStatic.NORMAL_SCRIPT_START_COMMENT_CHARS, DynamicHtmlStatic.NORMAL_SCRIPT_END_COMMENT_CHARS);
/*      */     }
/*      */ 
/*  931 */     if (((flags & 0x2) != 0) && (oldResource != null) && (oldResource.m_type == 0))
/*      */     {
/*  935 */       readComment(parseOutput, oldResource);
/*      */     }
/*      */ 
/*  938 */     return retVal;
/*      */   }
/*      */ 
/*      */   protected void addResource(String resName, Reader resReader, int curResource, ParseLocationInfo parseInfo)
/*      */     throws IOException, ParseSyntaxException
/*      */   {
/*  947 */     addResourceEx(resName, resReader, curResource, null, parseInfo, null);
/*      */   }
/*      */ 
/*      */   protected void addResourceEx(String resName, Reader resReader, int curResource, ResourceOptions resOptions, ParseLocationInfo parseInfo, IdcBreakpoints bp)
/*      */     throws IOException, ParseSyntaxException
/*      */   {
/*  954 */     Object res = null;
/*  955 */     Map hTb = null;
/*  956 */     Map ols = null;
/*  957 */     ResourceObject resourceObj = new ResourceObject();
/*  958 */     Object resSave = null;
/*  959 */     boolean noOverload = false;
/*  960 */     switch (curResource)
/*      */     {
/*      */     case 1:
/*  963 */       res = parseTable(resReader, null, parseInfo);
/*  964 */       hTb = this.m_tables;
/*  965 */       ols = this.m_tablesOL;
/*  966 */       noOverload = true;
/*  967 */       break;
/*      */     case 2:
/*      */     case 3:
/*  970 */       this.m_dynamicHtmlLoaded = true;
/*  971 */       this.m_dynamicResourceLoaded = true;
/*  972 */       hTb = this.m_dynamicHtml;
/*  973 */       ols = this.m_dynamicHtmlOL;
/*  974 */       int loadType = (curResource == 3) ? 1 : 0;
/*  975 */       res = parseDynamicHtml(resReader, resOptions, parseInfo, loadType, bp);
/*  976 */       DynamicHtml dynHtml = (DynamicHtml)res;
/*  977 */       resSave = dynHtml.shallowClone();
/*  978 */       if (hTb != null)
/*      */       {
/*  980 */         DynamicHtml priorResource = (DynamicHtml)hTb.get(resName);
/*  981 */         if (priorResource != null)
/*      */         {
/*  983 */           DynamicHtml oRes = dynHtml.findEarliestValidPriorScript(priorResource);
/*  984 */           if (oRes != null)
/*      */           {
/*  986 */             checkIncompatibility(resName, dynHtml, parseInfo, oRes);
/*  987 */             dynHtml.setPriorScript(oRes);
/*      */           }
/*      */         }
/*      */         else
/*      */         {
/*  992 */           this.m_dynamicHtmlList.add(resName);
/*      */         }
/*      */       }
/*  994 */       break;
/*      */     case 4:
/*  998 */       hTb = this.m_tables;
/*  999 */       ols = this.m_tablesOL;
/* 1000 */       break;
/*      */     case 5:
/* 1002 */       res = parseStringArray(resReader, parseInfo);
/* 1003 */       hTb = this.m_stringArrays;
/* 1004 */       ols = this.m_stringArraysOL;
/*      */     }
/*      */ 
/* 1007 */     if (hTb != null)
/*      */     {
/* 1011 */       ResourceTrace.doHashtableLoadAndLog(hTb, ols, resName, res, resName, noOverload);
/*      */     }
/*      */ 
/* 1014 */     if (resSave == null)
/*      */     {
/* 1016 */       resSave = res;
/*      */     }
/*      */ 
/* 1019 */     resourceObj.m_name = resName;
/* 1020 */     resourceObj.m_type = curResource;
/* 1021 */     resourceObj.m_resource = resSave;
/* 1022 */     this.m_resourceList.add(resourceObj);
/*      */   }
/*      */ 
/*      */   public void checkIncompatibility(String key, DynamicHtml cur, ParseLocationInfo parseInfo, DynamicHtml prev)
/*      */     throws ParseSyntaxException
/*      */   {
/* 1037 */     DynamicHtml dynDataObj = null;
/* 1038 */     DynamicHtml dynHtmlObj = null;
/* 1039 */     if (cur.m_dynamicData != null)
/*      */     {
/* 1041 */       dynDataObj = cur;
/*      */     }
/*      */     else
/*      */     {
/* 1045 */       dynHtmlObj = cur;
/*      */     }
/* 1047 */     if (prev.m_dynamicData != null)
/*      */     {
/* 1049 */       dynDataObj = prev;
/*      */     }
/*      */     else
/*      */     {
/* 1053 */       dynHtmlObj = prev;
/*      */     }
/*      */ 
/* 1056 */     if ((dynDataObj == null) || (dynHtmlObj == null)) {
/*      */       return;
/*      */     }
/* 1059 */     String msg = LocaleUtils.encodeMessage("csResourceContainerIncompatibleResourceTypes", null, key, dynHtmlObj.m_fileName, dynDataObj.m_fileName);
/*      */ 
/* 1061 */     throw new ParseSyntaxException(parseInfo, msg);
/*      */   }
/*      */ 
/*      */   public void mergeDynamicHtml(ResourceContainer newRes, ResourceContainer globalRes)
/*      */     throws IllegalArgumentException
/*      */   {
/* 1074 */     if ((newRes == null) || (newRes.m_dynamicHtmlList == null) || (this.m_dynamicHtmlList == null))
/*      */     {
/* 1076 */       return;
/*      */     }
/*      */ 
/* 1080 */     List list = newRes.m_dynamicHtmlList;
/* 1081 */     int n = list.size();
/* 1082 */     for (int i = 0; i < n; ++i)
/*      */     {
/* 1084 */       String resName = (String)list.get(i);
/* 1085 */       DynamicHtml newDynHtml = (DynamicHtml)newRes.m_dynamicHtml.get(resName);
/* 1086 */       addDynamicHtml(resName, newDynHtml, globalRes);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void addDynamicHtml(String resName, DynamicHtml newDynHtml, ResourceContainer globalRes)
/*      */     throws IllegalArgumentException
/*      */   {
/* 1093 */     if (newDynHtml == null)
/*      */     {
/* 1095 */       return;
/*      */     }
/*      */ 
/* 1098 */     DynamicHtml curDynHtml = (DynamicHtml)this.m_dynamicHtml.get(resName);
/* 1099 */     boolean isInList = true;
/* 1100 */     if (curDynHtml == null)
/*      */     {
/* 1102 */       isInList = false;
/* 1103 */       if ((globalRes != null) && (globalRes.m_dynamicHtml != null))
/*      */       {
/* 1105 */         curDynHtml = (DynamicHtml)globalRes.m_dynamicHtml.get(resName);
/*      */       }
/*      */     }
/* 1108 */     if (curDynHtml != null)
/*      */     {
/* 1112 */       if (curDynHtml.m_capturedVersion != null)
/*      */       {
/* 1114 */         String msg = LocaleUtils.encodeMessage("csResourceContainerCannotReplaceCurrentInclude", null, resName);
/*      */ 
/* 1116 */         if (newDynHtml.m_fileName != null)
/*      */         {
/* 1118 */           msg = LocaleUtils.appendMessage("!csResourceContainerNewVersionLocation", msg);
/*      */         }
/*      */ 
/* 1121 */         throw new IllegalArgumentException(msg);
/*      */       }
/* 1123 */       newDynHtml = newDynHtml.shallowCloneWithPriorScript(curDynHtml);
/*      */     }
/* 1125 */     if (!isInList)
/*      */     {
/* 1127 */       this.m_dynamicHtmlList.add(resName);
/*      */     }
/* 1129 */     this.m_dynamicHtml.put(resName, newDynHtml);
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   protected String getNextHtmlToken(StringTokenizer tokenizer)
/*      */   {
/* 1143 */     boolean inTag = false;
/*      */ 
/* 1145 */     if (!tokenizer.hasMoreTokens()) {
/* 1146 */       return null;
/*      */     }
/* 1148 */     String token = tokenizer.nextToken();
/* 1149 */     IdcStringBuilder retVal = null;
/*      */ 
/* 1152 */     int sloc = 0;
/* 1153 */     int tokenLength = token.length();
/* 1154 */     for (int i = 0; i < tokenLength; ++i)
/*      */     {
/* 1156 */       char ch = token.charAt(i);
/* 1157 */       if (!inTag)
/*      */       {
/* 1159 */         if (ch != '<')
/*      */           continue;
/* 1161 */         if (i > sloc + 1)
/*      */         {
/* 1163 */           if (retVal == null)
/*      */           {
/* 1165 */             retVal = new IdcStringBuilder();
/*      */           }
/* 1167 */           retVal.append(token, sloc, i);
/*      */         }
/* 1169 */         inTag = true;
/*      */       }
/*      */       else
/*      */       {
/* 1174 */         if (ch != '>')
/*      */           continue;
/* 1176 */         inTag = false;
/* 1177 */         sloc = i + 1;
/*      */       }
/*      */     }
/*      */ 
/* 1181 */     if ((sloc < tokenLength) && (retVal != null))
/*      */     {
/* 1183 */       retVal.append(token, sloc, tokenLength);
/*      */     }
/*      */ 
/* 1186 */     if (retVal == null)
/*      */     {
/* 1188 */       return token;
/*      */     }
/*      */ 
/* 1191 */     if (retVal.length() == 0)
/*      */     {
/* 1193 */       return null;
/*      */     }
/* 1195 */     return retVal.toString();
/*      */   }
/*      */ 
/*      */   public static String[] parseStringArray(Reader resReader, ParseLocationInfo parseInfo)
/*      */     throws IOException, ParseSyntaxException
/*      */   {
/* 1206 */     Vector strings = new IdcVector();
/*      */ 
/* 1209 */     IdcCharArrayWriter outbuf = new IdcCharArrayWriter();
/* 1210 */     ParseOutput parseOutput = new ParseOutput(parseInfo);
/* 1211 */     parseOutput.m_writer = outbuf;
/*      */ 
/* 1214 */     while (Parser.findHtmlTagPrefix(resReader, parseOutput, DD_TAG) == true)
/*      */     {
/* 1216 */       parseOutput.clearPending();
/* 1217 */       outbuf.reset();
/* 1218 */       if (!Parser.findHtmlTagPrefix(resReader, parseOutput, SLASHDD_TAG))
/*      */         break;
/* 1220 */       parseOutput.writePending();
/* 1221 */       strings.addElement(outbuf.toString());
/*      */     }
/* 1223 */     String[] retStrs = new String[strings.size()];
/* 1224 */     for (int i = 0; i < retStrs.length; ++i)
/*      */     {
/* 1226 */       retStrs[i] = ((String)strings.elementAt(i));
/*      */     }
/* 1228 */     parseOutput.releaseBuffers();
/*      */ 
/* 1230 */     return retStrs;
/*      */   }
/*      */ 
/*      */   public static Table parseTable(Reader resReader, Map strings, ParseLocationInfo parseInfo)
/*      */     throws IOException, ParseSyntaxException
/*      */   {
/* 1249 */     Table tbl = new Table();
/*      */ 
/* 1253 */     IdcCharArrayWriter outbuf = new IdcCharArrayWriter();
/* 1254 */     ParseOutput parseOutput = new ParseOutput(parseInfo);
/*      */     try
/*      */     {
/* 1257 */       parseOutput.m_noLiteralStrings = true;
/* 1258 */       parseOutput.m_writer = outbuf;
/*      */ 
/* 1260 */       boolean isDone = false;
/* 1261 */       boolean doingHeader = true;
/* 1262 */       boolean inRow = false;
/* 1263 */       boolean inField = false;
/* 1264 */       int nestedTableCount = 0;
/*      */ 
/* 1266 */       String[] items = null;
/* 1267 */       Vector hdr = new IdcVector();
/*      */ 
/* 1269 */       int nFields = 0;
/* 1270 */       int curField = 0;
/*      */ 
/* 1272 */       char[][] tags = (char[][])null;
/* 1273 */       int match = 0;
/*      */ 
/* 1278 */       while (!isDone)
/*      */       {
/* 1280 */         if (!inRow)
/*      */         {
/* 1282 */           tags = ENDROW_TAGS;
/*      */         }
/* 1284 */         else if (nestedTableCount > 0)
/*      */         {
/* 1286 */           tags = NESTED_TABLE_TAGS;
/*      */         }
/*      */         else
/*      */         {
/* 1290 */           tags = ITEM_TAGS;
/*      */         }
/* 1292 */         if (!inField) {
/* 1293 */           outbuf.reset();
/*      */         }
/* 1295 */         match = Parser.findHtmlPrefixTags(resReader, parseOutput, tags);
/* 1296 */         parseOutput.writePending();
/* 1297 */         if (!inRow)
/*      */         {
/* 1299 */           if (match == 0)
/*      */             break;
/* 1301 */           if (match == 1)
/*      */           {
/* 1303 */             inRow = true;
/* 1304 */             if (!doingHeader)
/*      */             {
/* 1306 */               items = new String[nFields];
/*      */             }
/*      */           }
/* 1309 */           if ((match >= 0) || (!parseOutput.m_isEOF))
/*      */             continue;
/* 1311 */           parseOutput.createParsingException("!csResourceContainerUnexpectedEOF");
/*      */         }
/*      */ 
/* 1317 */         if (nestedTableCount > 0)
/*      */         {
/* 1319 */           if (match == 0)
/*      */           {
/* 1321 */             outbuf.write(60);
/* 1322 */             outbuf.write(TABLE_TAG);
/* 1323 */             outbuf.write(62);
/* 1324 */             ++nestedTableCount;
/*      */           }
/*      */ 
/* 1328 */           outbuf.write(60);
/* 1329 */           outbuf.write(SLASHTABLE_TAG);
/* 1330 */           outbuf.write(62);
/* 1331 */           --nestedTableCount;
/*      */         }
/*      */ 
/* 1337 */         if (inField)
/*      */         {
/* 1340 */           if (match == 5)
/*      */           {
/* 1342 */             ++nestedTableCount;
/* 1343 */             outbuf.write(60);
/* 1344 */             outbuf.write(TABLE_TAG);
/* 1345 */             outbuf.write(62);
/*      */           }
/*      */ 
/* 1350 */           String itm = outbuf.toString();
/* 1351 */           if (doingHeader)
/*      */           {
/* 1353 */             hdr.addElement(itm);
/*      */           }
/* 1357 */           else if (curField < nFields)
/*      */           {
/* 1359 */             items[curField] = itm;
/* 1360 */             ++curField;
/*      */           }
/*      */ 
/* 1363 */           inField = false;
/*      */         }
/*      */ 
/* 1366 */         if (match <= 2)
/*      */         {
/* 1368 */           if (match == 0)
/*      */           {
/* 1370 */             isDone = true;
/*      */           }
/*      */ 
/* 1373 */           if (doingHeader)
/*      */           {
/* 1377 */             nFields = hdr.size();
/* 1378 */             String[] tmp = new String[nFields];
/* 1379 */             for (int i = 0; i < nFields; ++i)
/*      */             {
/* 1381 */               tmp[i] = ((String)hdr.elementAt(i));
/*      */             }
/* 1383 */             tbl.m_colNames = tmp;
/*      */ 
/* 1386 */             doingHeader = false;
/*      */           }
/*      */           else
/*      */           {
/* 1391 */             for (int i = curField; i < nFields; ++i)
/*      */             {
/* 1393 */               items[i] = new String();
/*      */             }
/*      */ 
/* 1397 */             tbl.m_rows.add(items);
/*      */ 
/* 1401 */             if ((strings != null) && (nFields >= 2))
/*      */             {
/* 1403 */               String name = items[0].trim();
/* 1404 */               String value = items[1];
/* 1405 */               if ((name.length() > 0) && (value.length() > 0))
/*      */               {
/* 1407 */                 strings.put(name, value);
/*      */               }
/*      */ 
/*      */             }
/*      */ 
/*      */           }
/*      */ 
/* 1414 */           curField = 0;
/* 1415 */           if (match == 1)
/*      */           {
/* 1417 */             items = new String[nFields];
/*      */           }
/*      */           else
/*      */           {
/* 1421 */             inRow = false;
/*      */           }
/*      */         }
/* 1424 */         if (match != 3)
/*      */           continue;
/* 1426 */         inField = true;
/* 1427 */         outbuf.reset();
/*      */       }
/*      */ 
/*      */     }
/*      */     finally
/*      */     {
/* 1433 */       outbuf.releaseBuffers();
/* 1434 */       parseOutput.releaseBuffers();
/*      */     }
/* 1436 */     return tbl;
/*      */   }
/*      */ 
/*      */   public static DynamicHtml parseDynamicHtml(Reader reader, ResourceOptions resOptions, ParseLocationInfo parseInfo, int loadType, IdcBreakpoints bp)
/*      */     throws IOException, ParseSyntaxException
/*      */   {
/* 1445 */     DynamicHtml dynHtml = new DynamicHtml();
/* 1446 */     ParseOutput parseOutput = new ParseOutput(parseInfo);
/* 1447 */     dynHtml.loadHtmlInContextEx(reader, resOptions, parseOutput, loadType, bp);
/* 1448 */     parseOutput.releaseBuffers();
/* 1449 */     return dynHtml;
/*      */   }
/*      */ 
/*      */   public void parseAndAddXmlResources(Reader reader, String filename)
/*      */     throws IOException, ParseSyntaxException
/*      */   {
/* 1458 */     parseAndAddXmlResourcesEx(reader, filename, true);
/*      */   }
/*      */ 
/*      */   public void parseAndAddXmlResourcesEx(Reader reader, String filename, boolean isStrict)
/*      */     throws IOException, ParseSyntaxException
/*      */   {
/* 1464 */     IdcCharArrayWriter outbuf = new IdcCharArrayWriter();
/* 1465 */     ParseOutput parseOutput = new ParseOutput();
/*      */     try
/*      */     {
/* 1468 */       parseOutput.m_isXmlLiteralEscape = true;
/* 1469 */       parseOutput.m_parseInfo.m_fileName = filename;
/* 1470 */       parseOutput.m_writer = outbuf;
/*      */ 
/* 1472 */       ParseLocationInfo parseInfo = parseOutput.m_parseInfo;
/*      */ 
/* 1474 */       this.m_resourceList = new IdcVector();
/* 1475 */       this.m_resourceMap = new ConcurrentHashMap();
/*      */ 
/* 1477 */       Vector parents = new IdcVector();
/* 1478 */       PropertiesTreeNode currentParent = null;
/*      */ 
/* 1480 */       String unparsedTag = null;
/* 1481 */       while ((unparsedTag = Parser.findAnyTag(reader, parseOutput)) != null)
/*      */       {
/* 1483 */         boolean isComment = false;
/* 1484 */         boolean skipTag = false;
/* 1485 */         String tag = unparsedTag.trim();
/* 1486 */         int len = tag.length();
/* 1487 */         if (len == 0)
/*      */         {
/* 1489 */           skipTag = true;
/*      */         }
/*      */ 
/* 1492 */         char ch = '\000';
/* 1493 */         String key = null;
/* 1494 */         if (!skipTag)
/*      */         {
/* 1496 */           ch = tag.charAt(0);
/* 1497 */           if (ch == '/')
/*      */           {
/* 1502 */             key = tag.substring(1);
/* 1503 */             if (isAllowableHtmlTag(key))
/*      */             {
/* 1505 */               skipTag = true;
/*      */             }
/* 1507 */             if (!skipTag)
/*      */             {
/* 1509 */               appendToNodeValue(currentParent, parseOutput, outbuf);
/* 1510 */               if ((currentParent == null) || (!key.equalsIgnoreCase(currentParent.m_name)))
/*      */               {
/* 1512 */                 if (!isStrict)
/*      */                   break;
/* 1514 */                 String name = null;
/* 1515 */                 if (currentParent != null)
/*      */                 {
/* 1517 */                   name = currentParent.m_name;
/*      */                 }
/* 1519 */                 throw new ParseSyntaxException(parseInfo, LocaleUtils.encodeMessage("csResourceContainerXMLParseError", null, name, key));
/*      */               }
/*      */ 
/* 1526 */               int count = parents.size();
/* 1527 */               parents.removeElementAt(count - 1);
/* 1528 */               if (count > 1)
/*      */               {
/* 1530 */                 currentParent = (PropertiesTreeNode)parents.elementAt(count - 2);
/*      */               }
/*      */ 
/* 1534 */               currentParent = null;
/*      */             }
/*      */ 
/*      */           }
/*      */ 
/*      */         }
/*      */ 
/* 1543 */         if ((!skipTag) && (!Validation.isAlphaNum(ch)))
/*      */         {
/* 1545 */           skipTag = true;
/* 1546 */           isComment = (ch == '?') || (ch == '!');
/*      */         }
/*      */ 
/* 1549 */         if (!skipTag)
/*      */         {
/* 1552 */           boolean isComplete = false;
/* 1553 */           ch = tag.charAt(len - 1);
/* 1554 */           if (ch == '/')
/*      */           {
/* 1558 */             isComplete = true;
/* 1559 */             tag = tag.substring(0, len - 1);
/* 1560 */             tag = tag.trim();
/*      */           }
/*      */ 
/* 1564 */           int loc = tag.indexOf(32);
/* 1565 */           String value = null;
/* 1566 */           if (loc > 0)
/*      */           {
/* 1569 */             key = tag.substring(0, loc).trim();
/* 1570 */             value = tag.substring(loc + 1);
/*      */           }
/*      */           else
/*      */           {
/* 1574 */             key = tag;
/*      */           }
/*      */ 
/* 1577 */           if (isAllowableHtmlTag(key))
/*      */           {
/* 1579 */             skipTag = true;
/*      */           }
/*      */ 
/* 1582 */           if (!skipTag)
/*      */           {
/* 1584 */             appendToNodeValue(currentParent, parseOutput, outbuf);
/* 1585 */             Properties props = null;
/* 1586 */             if (value != null)
/*      */             {
/* 1588 */               props = parseTagProperties(parseInfo, key, value);
/*      */             }
/*      */             else
/*      */             {
/* 1592 */               props = new Properties();
/*      */             }
/* 1594 */             PropertiesTreeNode node = new PropertiesTreeNode(key, props);
/*      */ 
/* 1596 */             if (currentParent != null)
/*      */             {
/* 1598 */               currentParent.addSubNode(node);
/*      */             }
/*      */             else
/*      */             {
/* 1602 */               this.m_xmlNodes.add(node);
/*      */             }
/*      */ 
/* 1605 */             if (!isComplete)
/*      */             {
/* 1608 */               currentParent = node;
/* 1609 */               parents.addElement(node);
/*      */             }
/*      */           }
/*      */         }
/* 1613 */         if (skipTag)
/*      */         {
/* 1616 */           parseOutput.m_pendingBuf[(parseOutput.m_numPending++)] = '<';
/*      */ 
/* 1620 */           parseOutput.copyToPending(!isComment, false);
/* 1621 */           if (isComment)
/*      */           {
/* 1624 */             parseOutput.clearPending();
/* 1625 */             outbuf.reset();
/*      */           }
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/* 1631 */       int num = parents.size();
/* 1632 */       if (num > 0)
/*      */       {
/* 1634 */         String str = "";
/* 1635 */         for (int i = 0; i < num; ++i)
/*      */         {
/* 1637 */           PropertiesTreeNode node = (PropertiesTreeNode)parents.elementAt(i);
/* 1638 */           if (str.length() > 0)
/*      */           {
/* 1640 */             str = str + ",";
/*      */           }
/* 1642 */           str = str + node.m_name;
/*      */         }
/* 1644 */         String msg = LocaleUtils.encodeMessage("csResourceContainerTagsNotClosed", null, filename, str);
/*      */ 
/* 1646 */         if (isStrict)
/*      */         {
/* 1648 */           throw new ParseSyntaxException(parseInfo, msg);
/*      */         }
/* 1650 */         Report.trace(null, LocaleResources.localizeMessage(msg, null), null);
/*      */       }
/*      */     }
/*      */     finally
/*      */     {
/* 1655 */       parseOutput.releaseBuffers();
/* 1656 */       outbuf.release();
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void appendToNodeValue(PropertiesTreeNode currentParent, ParseOutput parseOutput, IdcCharArrayWriter outbuf)
/*      */     throws IOException
/*      */   {
/* 1664 */     if (currentParent == null)
/*      */       return;
/* 1666 */     parseOutput.writePending();
/* 1667 */     if (currentParent.m_value.length() > 0)
/*      */     {
/* 1669 */       currentParent.m_value += " ";
/*      */     }
/* 1671 */     currentParent.m_value += StringUtils.decodeXmlEscapeSequence(outbuf.m_charArray, 0, outbuf.m_length).trim();
/*      */ 
/* 1673 */     if (outbuf.m_length <= 0)
/*      */       return;
/* 1675 */     outbuf.reset();
/*      */   }
/*      */ 
/*      */   public Properties parseTagProperties(ParseLocationInfo parseInfo, String tag, String propStr)
/*      */     throws ParseSyntaxException
/*      */   {
/* 1683 */     Properties props = new Properties();
/* 1684 */     parseTagPropertiesToMap(parseInfo, tag, propStr, props);
/* 1685 */     return props;
/*      */   }
/*      */ 
/*      */   public void parseTagPropertiesToMap(ParseLocationInfo parseInfo, String tag, String propStr, Map props)
/*      */     throws ParseSyntaxException
/*      */   {
/* 1691 */     if (props == null)
/*      */     {
/* 1694 */       props = new Properties();
/*      */     }
/*      */ 
/* 1697 */     IdcCharArrayWriter outbuf = new IdcCharArrayWriter();
/*      */ 
/* 1699 */     boolean isKey = true;
/* 1700 */     boolean inQuotes = false;
/* 1701 */     boolean isFinished = false;
/*      */ 
/* 1703 */     String key = null;
/* 1704 */     String value = null;
/*      */ 
/* 1707 */     char quoteChar = '"';
/* 1708 */     char[] chArray = propStr.toCharArray();
/* 1709 */     int len = chArray.length;
/* 1710 */     for (int i = 0; i < len; ++i)
/*      */     {
/* 1712 */       isFinished = false;
/* 1713 */       char ch = chArray[i];
/* 1714 */       switch (ch)
/*      */       {
/*      */       case '"':
/*      */       case '\'':
/* 1718 */         if (inQuotes)
/*      */         {
/* 1720 */           if (quoteChar == ch)
/*      */           {
/* 1722 */             isFinished = true;
/* 1723 */             inQuotes = false;
/*      */           }
/*      */           else
/*      */           {
/* 1727 */             outbuf.write(ch);
/*      */           }
/*      */         }
/*      */         else
/*      */         {
/* 1732 */           inQuotes = true;
/* 1733 */           quoteChar = ch;
/*      */         }
/* 1735 */         break;
/*      */       case ' ':
/* 1738 */         if (inQuotes)
/*      */         {
/* 1740 */           outbuf.write(ch);
/*      */         }
/*      */         else
/*      */         {
/* 1744 */           isFinished = true;
/*      */         }
/* 1746 */         break;
/*      */       case '=':
/* 1749 */         if (inQuotes)
/*      */         {
/* 1751 */           outbuf.write(ch);
/*      */         }
/*      */         else
/*      */         {
/* 1755 */           isFinished = true;
/* 1756 */           isKey = true;
/*      */         }
/* 1758 */         break;
/*      */       case '&':
/* 1762 */         ++i;
/* 1763 */         int start = i;
/* 1764 */         while (i < len)
/*      */         {
/* 1766 */           ch = chArray[i];
/* 1767 */           if (ch == ';') {
/*      */             break;
/*      */           }
/*      */ 
/* 1771 */           ++i;
/*      */         }
/* 1773 */         ch = StringUtils.decodeXmlEscapeCharacter(chArray, start, i - start);
/* 1774 */         outbuf.write(ch);
/* 1775 */         break;
/*      */       default:
/* 1777 */         outbuf.write(ch);
/*      */       }
/*      */ 
/* 1781 */       if (!isFinished)
/*      */         continue;
/* 1783 */       if (isKey)
/*      */       {
/* 1785 */         key = outbuf.toString().trim();
/* 1786 */         if (key.length() > 0)
/*      */         {
/* 1788 */           props.put(key, "1");
/* 1789 */           isKey = false;
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/* 1794 */         value = outbuf.toString();
/* 1795 */         props.put(key, value);
/* 1796 */         key = null;
/* 1797 */         isKey = true;
/*      */       }
/* 1799 */       isFinished = false;
/* 1800 */       value = null;
/* 1801 */       outbuf.reset();
/*      */     }
/*      */ 
/* 1805 */     if (inQuotes)
/*      */     {
/* 1808 */       throw new ParseSyntaxException(parseInfo, LocaleUtils.encodeMessage("csResourceContainerMismatchedQuotes", null, tag, propStr));
/*      */     }
/*      */ 
/* 1811 */     if (isFinished)
/*      */       return;
/* 1813 */     key = outbuf.toString();
/* 1814 */     if (key.trim().length() <= 0)
/*      */       return;
/* 1816 */     props.put(key, "1");
/*      */   }
/*      */ 
/*      */   public boolean isAllowableHtmlTag(String key)
/*      */   {
/* 1824 */     return Validation.isAllowableHtmlTag(key);
/*      */   }
/*      */ 
/*      */   public String unencodeResourceString(String val)
/*      */   {
/* 1829 */     return decodeResourceStringWithFlags(val, 0);
/*      */   }
/*      */ 
/*      */   public String decodeResourceStringWithFlags(String val, int flags)
/*      */   {
/* 1834 */     if (!this.m_unencodeResourceStrings)
/*      */     {
/* 1836 */       return val;
/*      */     }
/*      */ 
/* 1839 */     boolean removeExcessBackslashes = (flags & 0x1) != 0;
/*      */ 
/* 1841 */     if ((val.indexOf("\r") == -1) && (val.indexOf("\\&") == -1))
/*      */     {
/* 1843 */       return val;
/*      */     }
/* 1845 */     IdcStringBuilder result = new IdcStringBuilder(val);
/* 1846 */     char[] buf = result.m_charArray;
/* 1847 */     boolean eatingWs = false;
/* 1848 */     int i = 0; int j = 0;
/* 1849 */     while (i < result.m_length - 1)
/*      */     {
/* 1851 */       char c1 = buf[(i++)];
/* 1852 */       char c2 = buf[i];
/*      */ 
/* 1854 */       int startIndex = -1;
/* 1855 */       if ((removeExcessBackslashes) && (c1 == '\\') && (c2 == '\\'))
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/* 1861 */       if ((c1 == '\\') && (c2 == '&'))
/*      */       {
/* 1863 */         eatingWs = false;
/* 1864 */         startIndex = i + 1;
/*      */       }
/*      */       else
/*      */       {
/* 1868 */         if (eatingWs)
/*      */         {
/* 1870 */           switch (c1)
/*      */           {
/*      */           case '\t':
/*      */           case '\n':
/*      */           case '\f':
/*      */           case '\r':
/*      */           case ' ':
/* 1877 */             break;
/*      */           }
/* 1879 */           eatingWs = false;
/* 1880 */           buf[(j++)] = c1;
/*      */         }
/*      */ 
/* 1884 */         if (c1 == '\r')
/*      */           continue;
/* 1886 */         buf[(j++)] = c1; continue;
/*      */       }
/*      */ 
/* 1890 */       int endIndex = i + 1;
/* 1891 */       while (endIndex < result.m_length)
/*      */       {
/* 1893 */         if (buf[endIndex] == ';') {
/*      */           break;
/*      */         }
/*      */ 
/* 1897 */         ++endIndex;
/*      */       }
/* 1899 */       if (endIndex == result.m_length)
/*      */       {
/* 1902 */         while (i < result.m_length)
/*      */         {
/* 1904 */           if (eatingWs)
/*      */           {
/* 1906 */             switch (buf[i])
/*      */             {
/*      */             case '\t':
/*      */             case '\n':
/*      */             case '\f':
/*      */             case '\r':
/*      */             case ' ':
/* 1913 */               ++i;
/* 1914 */               break;
/*      */             }
/* 1916 */             eatingWs = false;
/* 1917 */             buf[(j++)] = buf[(i++)];
/*      */           }
/*      */ 
/* 1923 */           buf[(j++)] = buf[(i++)];
/*      */         }
/*      */ 
/* 1926 */         result.setLength(j);
/* 1927 */         break;
/*      */       }
/*      */ 
/* 1930 */       int ch = 0;
/* 1931 */       int tmpLength = endIndex - startIndex;
/*      */       char c3;
/*      */       char c4;
/*      */       char c5;
/* 1932 */       switch (tmpLength)
/*      */       {
/* 1937 */       case 2:
/* 1935 */         c3 = buf[startIndex];
/* 1936 */         c4 = buf[(startIndex + 1)];
/* 1937 */         if ((c3 == 'a') && (c4 == 't')) ch = 64;
/* 1938 */         else if ((c3 == 'l') && (c4 == 'f')) ch = 10;
/* 1939 */         else if ((c3 == 'c') && (c4 == 'r')) ch = 13;
/* 1940 */         else if ((c3 == 'l') && (c4 == 't')) ch = 60;
/* 1941 */         else if ((c3 == 'g') && (c4 == 't')) ch = 62;
/* 1942 */         else if ((c3 == 's') && (c4 == 'p')) ch = 32; break;
/*      */       case 3:
/* 1945 */         c3 = buf[startIndex];
/* 1946 */         c4 = buf[(startIndex + 1)];
/* 1947 */         c5 = buf[(startIndex + 2)];
/* 1948 */         if ((c3 == 't') && (c4 == 'a') && (c5 == 'b')) ch = 9; break;
/*      */       case 5:
/* 1951 */         c3 = buf[startIndex];
/* 1952 */         c4 = buf[(startIndex + 1)];
/* 1953 */         c5 = buf[(startIndex + 2)];
/* 1954 */         char c6 = buf[(startIndex + 3)];
/* 1955 */         char c7 = buf[(startIndex + 4)];
/* 1956 */         if ((c3 == 'e') && (c4 == 'a') && (c5 == 't') && (c6 == 'w') && (c7 == 's'))
/*      */         {
/* 1959 */           eatingWs = true;
/* 1960 */           ch = -1;
/*      */         }
/*      */ 
/*      */       case 4:
/*      */       }
/*      */ 
/* 1966 */       if (ch == 0)
/*      */       {
/* 1968 */         c3 = buf[startIndex];
/* 1969 */         c4 = buf[(startIndex + 1)];
/* 1970 */         if (c3 == '#')
/*      */         {
/* 1972 */           long[] value = new long[1];
/* 1973 */           if (c4 == 'x')
/*      */           {
/* 1975 */             if (NumberUtils.parseHexValue(buf, startIndex + 2, endIndex, value))
/*      */             {
/* 1977 */               ch = (char)(int)value[0];
/*      */             }
/*      */ 
/*      */           }
/* 1982 */           else if (NumberUtils.parseDecimalValue(buf, startIndex + 1, endIndex, value))
/*      */           {
/* 1984 */             ch = (char)(int)value[0];
/*      */           }
/*      */         }
/*      */       }
/*      */ 
/* 1989 */       if (ch == -1)
/*      */       {
/* 1991 */         i = endIndex + 1;
/*      */       }
/* 1993 */       else if (ch > 0)
/*      */       {
/* 1995 */         buf[(j++)] = (char)ch;
/* 1996 */         i = endIndex + 1;
/*      */       }
/*      */       else
/*      */       {
/* 2000 */         buf[(j++)] = c1;
/*      */       }
/*      */     }
/* 2003 */     if (i < result.m_length)
/*      */     {
/* 2005 */       if (eatingWs);
/* 2007 */       switch (buf[i])
/*      */       {
/*      */       case '\t':
/*      */       case '\n':
/*      */       case '\f':
/*      */       case '\r':
/*      */       case ' ':
/* 2014 */         ++i;
/* 2015 */         break;
/*      */       default:
/* 2017 */         eatingWs = false;
/* 2018 */         buf[(j++)] = buf[(i++)];
/* 2019 */         break label1004:
/*      */ 
/* 2024 */         buf[(j++)] = buf[(i++)];
/*      */       }
/*      */     }
/* 2027 */     label1004: result.setLength(j);
/*      */ 
/* 2029 */     return result.toString();
/*      */   }
/*      */ 
/*      */   public String leftTrim(String s)
/*      */   {
/* 2034 */     int offset = 0;
/* 2035 */     for (int i = 0; (i < s.length()) && 
/* 2037 */       (Character.isWhitespace(s.charAt(i))); ++i)
/*      */     {
/* 2039 */       ++offset;
/*      */     }
/*      */ 
/* 2046 */     if (offset > 0)
/*      */     {
/* 2048 */       s = s.substring(offset);
/*      */     }
/* 2050 */     return s;
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public String getString(String varName)
/*      */   {
/* 2061 */     Report.deprecatedUsage("Accessing localization data directly is deprecated.");
/* 2062 */     String lang = null;
/* 2063 */     String attribute = null;
/* 2064 */     int index1 = varName.lastIndexOf(46);
/* 2065 */     int index2 = varName.indexOf(91);
/* 2066 */     if ((index1 > 0) && (index2 > 0))
/*      */     {
/* 2068 */       lang = varName.substring(0, index1);
/* 2069 */       attribute = varName.substring(index2 + 1, varName.length() - 1);
/* 2070 */       varName = varName.substring(index1 + 1, index2);
/*      */     }
/* 2072 */     else if (index1 > 0)
/*      */     {
/* 2074 */       lang = varName.substring(0, index1);
/* 2075 */       varName = varName.substring(index1 + 1);
/*      */     }
/* 2077 */     else if (index2 > 0)
/*      */     {
/* 2079 */       attribute = varName.substring(index2 + 1, varName.length() - 1);
/* 2080 */       varName = varName.substring(0, index2);
/*      */     }
/* 2082 */     if (lang == null)
/*      */     {
/* 2084 */       lang = LocaleResources.m_baseLanguage;
/*      */     }
/* 2086 */     IdcLocaleString str = (IdcLocaleString)this.m_stringObjMap.get(varName);
/* 2087 */     if (str == null)
/*      */     {
/* 2089 */       return null;
/*      */     }
/* 2091 */     if (attribute != null)
/*      */     {
/* 2093 */       return str.getAttribute(attribute);
/*      */     }
/* 2095 */     int langIndex = this.m_languages.getCode(lang);
/* 2096 */     if (langIndex < 0)
/*      */     {
/* 2098 */       if (SystemUtils.m_verbose)
/*      */       {
/* 2100 */         Report.debug("localization", "language " + lang + " is undefined", null);
/*      */       }
/* 2102 */       return null;
/*      */     }
/* 2104 */     return str.getLangValue(langIndex);
/*      */   }
/*      */ 
/*      */   public String[] getStringArray(String varName)
/*      */   {
/* 2109 */     return (String[])(String[])this.m_stringArrays.get(varName);
/*      */   }
/*      */ 
/*      */   public Table getTable(String varName)
/*      */   {
/* 2114 */     return (Table)this.m_tables.get(varName);
/*      */   }
/*      */ 
/*      */   public DynamicData getDynamicDataResource(String varName)
/*      */   {
/* 2128 */     DynamicHtml dynHtml = getDynamicDataResourceWithPriorList(varName, null);
/* 2129 */     if (dynHtml == null)
/*      */     {
/* 2131 */       return null;
/*      */     }
/* 2133 */     return dynHtml.m_dynamicData;
/*      */   }
/*      */ 
/*      */   protected DynamicHtml getDynamicDataResourceWithPriorList(String varName, List<String> activeList)
/*      */   {
/* 2147 */     DynamicHtml dynHtml = (DynamicHtml)this.m_dynamicHtml.get(varName);
/* 2148 */     DynamicData dynData = null;
/* 2149 */     if (dynHtml != null)
/*      */     {
/* 2151 */       boolean didIt = false;
/* 2152 */       boolean doIt = false;
/* 2153 */       List dataToMergeIn = null;
/* 2154 */       DynamicDataMergeInfo mergeInfo = null;
/* 2155 */       DynamicData mergedData = null;
/*      */ 
/* 2157 */       int activeListSize = 0;
/* 2158 */       synchronized (dynHtml)
/*      */       {
/* 2162 */         dynData = dynHtml.m_dynamicData;
/* 2163 */         if ((dynData != null) && (!dynHtml.m_isMergedDynamicData))
/*      */         {
/* 2165 */           doIt = true;
/*      */ 
/* 2167 */           mergeInfo = new DynamicDataMergeInfo();
/* 2168 */           mergeInfo.m_varName = varName;
/* 2169 */           mergedData = dynData.shallowCloneForMerging();
/* 2170 */           dynData.loadRecursiveData(mergeInfo, dynHtml);
/* 2171 */           mergedData.mergeData(dynHtml, mergeInfo);
/* 2172 */           mergeInfo.m_dynamicHtmlToMerge = null;
/* 2173 */           if (mergeInfo.m_tableMergingDone)
/*      */           {
/* 2175 */             dynHtml.m_isMergedDynamicData = true;
/* 2176 */             dynHtml.m_dynamicData = mergedData;
/* 2177 */             didIt = true;
/*      */           }
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/* 2183 */       if (activeList != null)
/*      */       {
/* 2185 */         if ((doIt) && (!didIt))
/*      */         {
/* 2187 */           activeListSize = activeList.size();
/* 2188 */           if (activeListSize + 1 > 20)
/*      */           {
/* 2190 */             Report.trace("system", "Dynamic data using mergeOtherData to reference too many (over 20) other resources, parent path = " + activeList + "new data = " + dynData.m_mergeOtherData, null);
/*      */ 
/* 2196 */             didIt = true;
/* 2197 */             dynData = null;
/*      */           }
/*      */         }
/* 2200 */         if ((doIt) && (!didIt) && (mergeInfo.m_mergeOtherData != null))
/*      */         {
/* 2202 */           for (String s : activeList)
/*      */           {
/* 2204 */             if (mergeInfo.m_mergeOtherData.contains(s))
/*      */             {
/* 2206 */               Report.trace("system", "Dynamic data using mergeOtherData to recursively reference " + s + "  parent data = " + activeList, null);
/*      */ 
/* 2211 */               didIt = true;
/* 2212 */               dynData = null;
/*      */             }
/*      */           }
/*      */         }
/*      */       }
/*      */ 
/* 2218 */       if ((doIt) && (!didIt))
/*      */       {
/*      */         Object newActiveList;
/* 2220 */         if (mergeInfo.m_mergeOtherData != null)
/*      */         {
/* 2222 */           List otherData = mergeInfo.m_mergeOtherData;
/* 2223 */           newActiveList = new ArrayList(activeListSize + 1);
/* 2224 */           dataToMergeIn = new ArrayList(otherData.size());
/* 2225 */           if (activeList != null)
/*      */           {
/* 2227 */             ((List)newActiveList).addAll(activeList);
/*      */           }
/* 2229 */           for (String s : otherData)
/*      */           {
/* 2231 */             ((List)newActiveList).add(s);
/* 2232 */             DynamicHtml retVal = getDynamicDataResourceWithPriorList(s, (List)newActiveList);
/* 2233 */             if (retVal != null)
/*      */             {
/* 2235 */               dataToMergeIn.add(retVal);
/*      */             }
/*      */ 
/* 2239 */             ((List)newActiveList).remove(((List)newActiveList).size() - 1);
/*      */           }
/*      */ 
/*      */         }
/*      */ 
/* 2245 */         synchronized (dynHtml)
/*      */         {
/* 2249 */           if (!dynHtml.m_isMergedDynamicData)
/*      */           {
/* 2254 */             mergeInfo.m_otherDynamicHtmlToMerge = dataToMergeIn;
/* 2255 */             mergedData.mergeData(dynHtml, mergeInfo);
/* 2256 */             dynHtml.m_isMergedDynamicData = true;
/* 2257 */             dynHtml.m_dynamicData = mergedData;
/*      */           }
/*      */         }
/*      */       }
/* 2261 */       if (mergeInfo != null)
/*      */       {
/* 2264 */         mergeInfo.clearDynamicData();
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 2269 */     if (dynData == null)
/*      */     {
/* 2271 */       dynHtml = null;
/*      */     }
/* 2273 */     return (DynamicHtml)dynHtml;
/*      */   }
/*      */ 
/*      */   public DynamicHtml getHtmlResource(String varName)
/*      */   {
/* 2283 */     return (DynamicHtml)this.m_dynamicHtml.get(varName);
/*      */   }
/*      */ 
/*      */   public List getXmlNodes()
/*      */   {
/* 2292 */     return this.m_xmlNodes;
/*      */   }
/*      */ 
/*      */   public boolean validateStringTags(List addTagList, String lang, int flags)
/*      */   {
/* 2297 */     if ((lang != null) && (!lang.equals("")) && (!lang.equals(LocaleResources.m_baseLanguage)))
/*      */     {
/* 2299 */       return true;
/*      */     }
/*      */ 
/* 2302 */     int notransIndex = -1;
/* 2303 */     int extendedIndex = -1;
/*      */ 
/* 2305 */     for (int i = 0; i < addTagList.size(); ++i)
/*      */     {
/* 2307 */       if (addTagList.get(i).equals("notranslate"))
/*      */       {
/* 2309 */         notransIndex = i;
/*      */       } else {
/* 2311 */         if (!addTagList.get(i).equals("extended"))
/*      */           continue;
/* 2313 */         extendedIndex = i;
/*      */       }
/*      */     }
/* 2316 */     if (notransIndex > extendedIndex)
/*      */     {
/* 2318 */       int tmp = extendedIndex;
/* 2319 */       extendedIndex = notransIndex;
/* 2320 */       notransIndex = tmp;
/*      */     }
/* 2322 */     if (extendedIndex >= 0)
/*      */     {
/* 2324 */       addTagList.remove(extendedIndex);
/*      */     }
/* 2326 */     if (notransIndex >= 0)
/*      */     {
/* 2328 */       addTagList.remove(notransIndex);
/*      */     }
/*      */ 
/* 2335 */     return ((flags & 0x2) <= 0) || ((extendedIndex < 0) && (notransIndex < 0));
/*      */   }
/*      */ 
/*      */   protected String readComment(ParseOutput parseOutput, ResourceObject resourceObj)
/*      */     throws IOException
/*      */   {
/* 2344 */     parseOutput.writePending();
/* 2345 */     IdcCharArrayWriter outbuf = (IdcCharArrayWriter)parseOutput.m_writer;
/* 2346 */     char[] tmp = outbuf.toCharArray();
/* 2347 */     String extra = new String(tmp);
/*      */ 
/* 2349 */     String comment = null;
/* 2350 */     if ((tmp.length > 0) && (resourceObj != null))
/*      */     {
/* 2352 */       int endOfString = extra.indexOf("@>");
/* 2353 */       if (endOfString > 0)
/*      */       {
/* 2355 */         extra = extra.substring(endOfString + 2);
/*      */       }
/*      */ 
/* 2358 */       int index = extra.indexOf(13);
/* 2359 */       if (index < 0)
/*      */       {
/* 2361 */         index = extra.indexOf(10);
/*      */       }
/*      */ 
/* 2364 */       if (index >= 0)
/*      */       {
/* 2366 */         comment = extra.substring(0, index);
/* 2367 */         extra = extra.substring(index + 1);
/* 2368 */         comment = StringUtils.replaceCRLF(comment);
/*      */       }
/*      */ 
/* 2371 */       IdcLocaleString prevStr = (IdcLocaleString)this.m_stringObjMap.get(resourceObj.m_name);
/* 2372 */       if ((comment != null) && (comment.length() > 0) && (prevStr != null))
/*      */       {
/* 2374 */         prevStr.setAttribute("comment", StringUtils.encodeXmlEscapeSequence(comment.trim()));
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 2379 */     return extra;
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 2387 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 100525 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.ResourceContainer
 * JD-Core Version:    0.5.4
 */