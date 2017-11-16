/*      */ package intradoc.server;
/*      */ 
/*      */ import intradoc.common.FileUtils;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataBinderUtils;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.DataSerializeUtils;
/*      */ import intradoc.data.ResultSet;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.data.Workspace;
/*      */ import intradoc.fdf.Fdf;
/*      */ import intradoc.filestore.FileStoreProvider;
/*      */ import intradoc.filestore.IdcFileDescriptor;
/*      */ import intradoc.lang.EmptyQueueException;
/*      */ import intradoc.lang.Queue;
/*      */ import intradoc.resource.DataTransformationUtils;
/*      */ import intradoc.resource.DynamicDataMerger;
/*      */ import intradoc.shared.DocFieldUtils;
/*      */ import intradoc.shared.LegacyDocumentPathBuilder;
/*      */ import intradoc.shared.RevisionSpec;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.shared.UserData;
/*      */ import java.io.ByteArrayOutputStream;
/*      */ import java.io.FileInputStream;
/*      */ import java.io.FileReader;
/*      */ import java.io.FileWriter;
/*      */ import java.io.IOException;
/*      */ import java.io.InputStream;
/*      */ import java.io.InputStreamReader;
/*      */ import java.io.Reader;
/*      */ import java.util.Calendar;
/*      */ import java.util.Enumeration;
/*      */ import java.util.HashMap;
/*      */ import java.util.Hashtable;
/*      */ import java.util.Properties;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class FormHandler extends ServiceHandler
/*      */ {
/*      */   protected static final int TIMEOUT = 20;
/*   73 */   protected static Hashtable m_requestTable = new Hashtable();
/*   74 */   protected static Queue m_requestQueue = new Queue();
/*   75 */   protected static long m_lastId = System.currentTimeMillis();
/*   76 */   protected static boolean[] m_lockObject = new boolean[0];
/*      */ 
/*      */   protected long nextId()
/*      */   {
/*   90 */     synchronized (m_lockObject)
/*      */     {
/*   92 */       long id = System.currentTimeMillis();
/*   93 */       if (id == m_lastId)
/*      */       {
/*   95 */         id += 1L;
/*      */       }
/*   97 */       return FormHandler.m_lastId = id;
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void submitForm()
/*      */     throws ServiceException
/*      */   {
/*  107 */     Service.checkFeatureAllowed("Forms");
/*      */ 
/*  109 */     boolean isFormsPresent = SharedObjects.getEnvValueAsBoolean("IsContentRefineryPresent", false);
/*  110 */     if (isFormsPresent)
/*      */     {
/*  112 */       long id = nextId();
/*  113 */       Long idObj = new Long(id);
/*  114 */       String idText = Long.toHexString(id);
/*  115 */       String url = DirectoryLocator.getCgiWebUrl(true) + "?IdcService=FORM_PROCESS&ClientId=" + idText;
/*      */ 
/*  117 */       this.m_service.m_httpImplementor.setRedirectUrl(url);
/*  118 */       this.m_binder.putLocal("ClientId", idText);
/*  119 */       this.m_binder.putLocal("isAbsoluteWeb", "1");
/*  120 */       this.m_binder.putLocal("isAbsoluteCgi", "1");
/*  121 */       m_requestTable.put(idObj, this.m_binder.getLocalData());
/*  122 */       m_requestQueue.insert(idObj);
/*      */     }
/*      */     else
/*      */     {
/*  126 */       this.m_service.createServiceException(null, "!csFormsNotConfigured");
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void processForm()
/*      */     throws DataException, ServiceException
/*      */   {
/*  142 */     if (this.m_service.m_userData.m_name.equals("anonymous"))
/*      */     {
/*  144 */       this.m_service.setPromptForLogin(true);
/*  145 */       this.m_service.createServiceException(null, "!csLoginCredentialsMissing");
/*      */     }
/*      */ 
/*  149 */     Properties props = findRequestWithCleanup();
/*  150 */     props.put("dUser", this.m_service.m_userData.m_name);
/*      */ 
/*  153 */     this.m_binder.putLocal("isAbsoluteWeb", "1");
/*  154 */     this.m_binder.putLocal("isAbsoluteCgi", "1");
/*  155 */     props.put("isAbsoluteWeb", "1");
/*  156 */     props.put("isAbsoluteCgi", "1");
/*      */ 
/*  159 */     boolean isTemplate = StringUtils.convertToBool((String)props.get("isTemplate"), false);
/*      */ 
/*  162 */     String formURL = (String)props.get("pathToTemplate");
/*  163 */     if (formURL == null)
/*      */     {
/*  165 */       throw new DataException("!csFormsPathToTemplateMissing");
/*      */     }
/*      */ 
/*  169 */     String pdfFileName = LegacyDocumentPathBuilder.extractFileNameFromUrl(formURL);
/*  170 */     String pdfDocName = LegacyDocumentPathBuilder.extractDocNameFromFileName(pdfFileName);
/*      */ 
/*  172 */     if (!pdfFileName.toLowerCase().endsWith(".pdf"))
/*      */     {
/*  176 */       int index = formURL.indexOf("dDocName=");
/*  177 */       if (index >= 0)
/*      */       {
/*  179 */         String tmp = formURL.substring(index);
/*  180 */         index = tmp.indexOf("&");
/*  181 */         if (index == -1)
/*      */         {
/*  183 */           index = tmp.indexOf("#");
/*  184 */           if (index == -1)
/*      */           {
/*  186 */             index = tmp.length();
/*      */           }
/*      */         }
/*  189 */         tmp = tmp.substring(0, index);
/*  190 */         pdfDocName = tmp.substring("dDocName=".length());
/*      */ 
/*  192 */         if (isTemplate)
/*      */         {
/*  195 */           DataBinder binder = new DataBinder();
/*  196 */           binder.putLocal("dDocName", pdfDocName);
/*  197 */           ResultSet rset = this.m_workspace.createResultSet("QdocName", binder);
/*  198 */           if (rset.isRowPresent())
/*      */           {
/*  200 */             DataResultSet drset = new DataResultSet();
/*  201 */             drset.copy(rset);
/*  202 */             binder.addResultSet("DOC_INFO", drset);
/*  203 */             binder.putLocal("RenditionId", "webViewableFile");
/*  204 */             IdcFileDescriptor d = this.m_service.m_fileStore.createDescriptor(binder, null, this.m_service);
/*      */ 
/*  206 */             HashMap args = new HashMap();
/*  207 */             args.put("useAbsolute", "1");
/*  208 */             String url = this.m_service.m_fileStore.getClientURL(d, null, args, this.m_service);
/*  209 */             props.put("pathToTemplate", url);
/*      */           }
/*      */           else
/*      */           {
/*  213 */             String msg = LocaleUtils.encodeMessage("csFormsUnableToCreateInstance", null, pdfDocName);
/*      */ 
/*  215 */             this.m_service.createServiceException(null, msg);
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/*  221 */     String docName = null;
/*  222 */     if (isTemplate)
/*      */     {
/*  224 */       docName = (String)props.get("dDocName");
/*      */ 
/*  226 */       if (docName != null)
/*      */       {
/*  230 */         this.m_binder.putLocal("dDocName", docName);
/*  231 */         ResultSet rset = this.m_workspace.createResultSet("QdocName", this.m_binder);
/*  232 */         if (rset.isRowPresent())
/*      */         {
/*  234 */           String msg = LocaleUtils.encodeMessage("csCannotCreateContentItem", null, docName);
/*      */ 
/*  236 */           this.m_service.createServiceException(null, msg);
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  241 */       this.m_binder.putLocal("DocExists", "");
/*      */     }
/*      */     else
/*      */     {
/*  247 */       String dID = (String)props.get("dID");
/*  248 */       docName = pdfDocName;
/*  249 */       if (dID != null)
/*      */       {
/*  252 */         this.m_binder.putLocal("dDocName", docName);
/*  253 */         ResultSet rset = this.m_workspace.createResultSet("QdocName", this.m_binder);
/*  254 */         if (!rset.isRowPresent())
/*      */         {
/*  256 */           String msg = LocaleUtils.encodeMessage("csContentItemNoLongerExists", "!csUpdateCancelled", docName);
/*      */ 
/*  258 */           this.m_service.createServiceException(null, msg);
/*      */         }
/*  260 */         String latestID = ResultSetUtils.getValue(rset, "dID");
/*  261 */         if (!latestID.equalsIgnoreCase(dID))
/*      */         {
/*  263 */           String msg = LocaleUtils.encodeMessage("csFormsRevisionIsNotLatest", "!csUpdateCancelled");
/*      */ 
/*  265 */           this.m_service.createServiceException(null, msg);
/*      */         }
/*      */       }
/*      */ 
/*  269 */       props.put("DocExists", "1");
/*  270 */       props.put("dDocName", docName);
/*      */     }
/*      */ 
/*  274 */     this.m_binder.putLocal("dDocName", pdfDocName);
/*  275 */     ResultSet rset = this.m_workspace.createResultSet("QdocNameMeta", this.m_binder);
/*      */ 
/*  277 */     if (!rset.isRowPresent())
/*      */     {
/*  279 */       String msg = LocaleUtils.encodeMessage("csFormNoLongerExists", null, pdfDocName);
/*      */ 
/*  281 */       msg = LocaleUtils.encodeMessage("csCannotCheckin", msg, docName);
/*  282 */       this.m_service.createServiceException(null, msg);
/*      */     }
/*      */ 
/*  285 */     DataResultSet drset = new DataResultSet();
/*  286 */     drset.copy(rset, 1);
/*  287 */     rset = null;
/*      */ 
/*  289 */     Properties oldRevProps = drset.getCurrentRowProps();
/*      */ 
/*  291 */     this.m_binder.setLocalData(oldRevProps);
/*  292 */     this.m_binder.putLocal("dUser", this.m_service.m_userData.m_name);
/*  293 */     this.m_binder.putLocal("isAbsoluteWeb", "1");
/*  294 */     this.m_binder.putLocal("isAbsoluteCgi", "1");
/*      */ 
/*  297 */     boolean doCheckin = false;
/*  298 */     boolean doUpdate = false;
/*  299 */     String actions = (String)props.get("actions");
/*      */ 
/*  301 */     if (actions != null)
/*      */     {
/*  303 */       actions = actions.toLowerCase();
/*  304 */       doCheckin = actions.indexOf("createdocument") >= 0;
/*  305 */       doUpdate = actions.indexOf("updatedocument") >= 0;
/*  306 */       if ((doCheckin) && (doUpdate))
/*      */       {
/*  308 */         this.m_service.createServiceException(null, "!csFormsConflictingFlags");
/*      */       }
/*      */     }
/*      */ 
/*  312 */     props.put("DoCheckin", (doCheckin) ? "1" : "0");
/*  313 */     props.put("DoUpdate", (doUpdate) ? "1" : "0");
/*      */ 
/*  315 */     if (isTemplate)
/*      */     {
/*  317 */       ServiceData serviceData = this.m_service.getServiceData();
/*      */ 
/*  320 */       serviceData.m_accessLevel = 1;
/*  321 */       this.m_service.checkSecurity();
/*  322 */       serviceData.m_accessLevel = 2;
/*      */ 
/*  324 */       oldRevProps.remove("dRevLabel");
/*  325 */       oldRevProps.remove("dRevisionID");
/*      */     }
/*      */     else
/*      */     {
/*  329 */       String checkoutUser = this.m_binder.get("dCheckoutUser");
/*  330 */       String ID = this.m_binder.get("dID");
/*  331 */       if ((checkoutUser == null) || (checkoutUser.length() == 0))
/*      */       {
/*  334 */         this.m_binder.addResultSet("DOC_INFO", drset);
/*      */ 
/*  337 */         this.m_binder.putLocal("CurRevID", ID);
/*  338 */         this.m_binder.putLocal("latestID", ID);
/*  339 */         this.m_binder.putLocal("CurRevCheckoutUser", this.m_service.m_userData.m_name);
/*  340 */         this.m_binder.putLocal("dExtension", "fdf");
/*  341 */         this.m_binder.putLocal("dFormat", "application/fdf");
/*  342 */         this.m_service.executeService("CHECKOUT_SUB");
/*      */ 
/*  345 */         Enumeration en = this.m_binder.getResultSetList();
/*  346 */         while (en.hasMoreElements())
/*      */         {
/*  348 */           this.m_binder.removeResultSet((String)en.nextElement());
/*      */         }
/*      */       }
/*      */ 
/*  352 */       this.m_binder.putLocal("RenditionId", "primaryFile");
/*  353 */       IdcFileDescriptor d = this.m_service.m_fileStore.createDescriptor(this.m_binder, null, this.m_service);
/*  354 */       props.put("priorVaultIdcFileDescriptor", d);
/*      */     }
/*      */ 
/*  357 */     this.m_binder.setLocalData(props);
/*  358 */     doOverrides();
/*  359 */     mergeOldProps(props, oldRevProps);
/*      */ 
/*  361 */     if (props.get("isFinished") == null)
/*      */     {
/*  364 */       props.put("isFinished", "1");
/*      */     }
/*      */ 
/*  367 */     this.m_service.executeFilter("formPreSubmit");
/*      */   }
/*      */ 
/*      */   public void mergeOldProps(Properties newProps, Properties oldProps)
/*      */     throws ServiceException
/*      */   {
/*  375 */     String baseCanCopyList = "dDocType,dSecurityGroup,dDocAccount";
/*  376 */     String canCopyList = SharedObjects.getEnvironmentValue("FormInheritMetaDataFieldList");
/*  377 */     if ((canCopyList == null) || (canCopyList.length() == 0))
/*      */     {
/*  379 */       canCopyList = baseCanCopyList;
/*      */     }
/*      */     else
/*      */     {
/*  383 */       canCopyList = canCopyList + "," + baseCanCopyList;
/*      */     }
/*      */ 
/*  386 */     String[] mustCopy = { "dID", "dRevClassID", "dRevisionID" };
/*  387 */     Vector canCopy = StringUtils.parseArray(canCopyList, ',', ',');
/*      */ 
/*  390 */     String revLabel = (String)newProps.get("dRevLabel");
/*  391 */     String latestRevLabel = (String)oldProps.get("dRevLabel");
/*  392 */     if ((revLabel == null) && (latestRevLabel != null))
/*      */     {
/*  394 */       newProps.put("latestRevLabel", latestRevLabel);
/*      */     }
/*  396 */     else if ((revLabel != null) && (latestRevLabel != null) && (revLabel.equalsIgnoreCase(latestRevLabel)))
/*      */     {
/*  399 */       newProps.remove("dRevLabel");
/*      */     }
/*      */ 
/*  403 */     int i = 0; for (int l = mustCopy.length; i < l; ++i)
/*      */     {
/*  405 */       String key = mustCopy[i];
/*  406 */       String old = (String)oldProps.get(key);
/*  407 */       if (old != null)
/*      */       {
/*  409 */         newProps.put(key, old);
/*      */       }
/*      */       else
/*      */       {
/*  413 */         newProps.put(key, "");
/*      */       }
/*      */     }
/*      */ 
/*  417 */     i = 0; for (l = canCopy.size(); i < l; ++i)
/*      */     {
/*  419 */       String key = (String)canCopy.elementAt(i);
/*  420 */       if (key == null) continue; if (key.length() == 0)
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/*  425 */       if (newProps.get(key) != null)
/*      */         continue;
/*  427 */       String old = (String)oldProps.get(key);
/*  428 */       if (old == null)
/*      */         continue;
/*  430 */       newProps.put(key, old);
/*      */     }
/*      */ 
/*  436 */     String docTitle = (String)newProps.get("dDocTitle");
/*  437 */     if ((docTitle != null) && (docTitle.length() != 0))
/*      */       return;
/*  439 */     String dDocType = (String)newProps.get("dDocType");
/*  440 */     String script = this.m_binder.getAllowMissing("FormDocTitleScript" + dDocType);
/*  441 */     if (script == null)
/*      */     {
/*  443 */       script = this.m_binder.getAllowMissing("FormDocTitleScript");
/*      */     }
/*  445 */     if (script == null)
/*      */     {
/*  447 */       script = "<$dDocAuthor$> - <$dDocType$> - <$dateCurrent()$>";
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/*  452 */       docTitle = this.m_service.m_pageMerger.evaluateScript(script);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  456 */       this.m_service.createServiceException(e, "!csFormsUnableToComputeTitle");
/*      */     }
/*      */ 
/*  459 */     newProps.put("dDocTitle", docTitle);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void createFdf()
/*      */     throws DataException, ServiceException
/*      */   {
/*  468 */     String tmpDir = DataBinder.getTemporaryDirectory();
/*  469 */     FileUtils.checkOrCreateDirectory(tmpDir, 2);
/*      */ 
/*  484 */     String templateFlag = this.m_binder.getLocal("isTemplate");
/*  485 */     boolean isTemplate = StringUtils.convertToBool(templateFlag, false);
/*      */     String templateURL;
/*  488 */     if (isTemplate)
/*      */     {
/*  490 */       String templateURL = this.m_binder.getLocal("pathToTemplate");
/*      */ 
/*  492 */       String[] seps = { "?", "#" };
/*  493 */       int length = seps.length;
/*  494 */       for (int i = 0; i < length; ++i)
/*      */       {
/*  496 */         int j = templateURL.indexOf(seps[i]);
/*  497 */         if (j < 0)
/*      */           continue;
/*  499 */         templateURL = templateURL.substring(0, j);
/*      */       }
/*      */ 
/*  502 */       this.m_binder.putLocal("pathToTemplate", templateURL);
/*      */     }
/*      */     else
/*      */     {
/*  506 */       Properties props = this.m_binder.getLocalData();
/*  507 */       IdcFileDescriptor d = (IdcFileDescriptor)props.get("priorVaultIdcFileDescriptor");
/*      */ 
/*  509 */       props.remove("priorVaultIdcFileDescriptor");
/*  510 */       String vaultFileName = this.m_binder.get("priorVaultFilePath");
/*  511 */       Reader r = null;
/*      */       try
/*      */       {
/*  515 */         if (d != null)
/*      */         {
/*  517 */           InputStream s = this.m_service.m_fileStore.getInputStream(d, null);
/*  518 */           r = new InputStreamReader(s);
/*      */         }
/*      */         else
/*      */         {
/*  522 */           r = new FileReader(vaultFileName);
/*      */         }
/*  524 */         Properties prevProps = Fdf.readFdf(r);
/*  525 */         templateURL = (String)prevProps.get("/F");
/*  526 */         templateURL = templateURL.substring(1, templateURL.length() - 1);
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/*  530 */         String msg = LocaleUtils.encodeMessage("csFormsUnableToReadVaultFile", null, vaultFileName);
/*      */ 
/*  532 */         this.m_service.createServiceException(e, msg);
/*      */ 
/*  537 */         return; } finally { FileUtils.closeObject(r); }
/*      */ 
/*  539 */       if (templateURL == null)
/*      */       {
/*  541 */         String msg = LocaleUtils.encodeMessage("csFormsUnableToReadFormValue", null, vaultFileName);
/*      */ 
/*  543 */         this.m_service.createServiceException(null, msg);
/*  544 */         return;
/*      */       }
/*      */     }
/*      */ 
/*  548 */     this.m_binder.putLocal("isTemplate", "false");
/*  549 */     String idText = this.m_binder.getLocal("ClientId");
/*      */ 
/*  551 */     String fdfName = tmpDir + idText + ".fdf";
/*  552 */     this.m_binder.putLocal("primaryFile", fdfName);
/*  553 */     this.m_binder.putLocal("primaryFile:path", fdfName);
/*  554 */     this.m_binder.addTempFile(fdfName);
/*  555 */     FileWriter w = null;
/*      */     try
/*      */     {
/*  558 */       w = new FileWriter(fdfName);
/*  559 */       Fdf.createFdf(w, templateURL, this.m_binder.getLocalData());
/*  560 */       w.close();
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/*  564 */       String msg = LocaleUtils.encodeMessage("csFormsUnableToCreateFDF", null, fdfName);
/*      */ 
/*  566 */       this.m_service.createServiceException(e, msg);
/*      */     }
/*      */     finally
/*      */     {
/*  570 */       FileUtils.closeObject(w);
/*      */     }
/*      */ 
/*  573 */     this.m_binder.putLocal("isTemplate", (isTemplate) ? "true" : "false");
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void deleteFdf() throws ServiceException
/*      */   {
/*  579 */     String tmpDir = DataBinder.getTemporaryDirectory();
/*  580 */     String idText = this.m_binder.getLocal("ClientId");
/*  581 */     String fdfName = tmpDir + idText + ".fdf";
/*  582 */     FileUtils.deleteFile(fdfName);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void doOverrides()
/*      */   {
/*  588 */     this.m_binder.putLocal("dDocAuthor", this.m_service.m_userData.m_name);
/*  589 */     Calendar now = Calendar.getInstance();
/*  590 */     String nowStr = LocaleResources.localizeDate(now, this.m_service);
/*      */ 
/*  592 */     doOverride("dInDate", nowStr);
/*  593 */     doOverride("dCreateDate", nowStr);
/*  594 */     doOverride("dDocName", "");
/*      */   }
/*      */ 
/*      */   protected boolean doOverride(String name, String value)
/*      */   {
/*  599 */     String v = this.m_binder.getLocal(name);
/*  600 */     boolean rc = (v == null) || (v.trim().length() == 0);
/*  601 */     if (rc)
/*      */     {
/*  603 */       this.m_binder.putLocal(name, value);
/*      */     }
/*  605 */     return rc;
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void conditionalDetermineCheckin()
/*      */     throws DataException, ServiceException
/*      */   {
/*  612 */     String flag = this.m_binder.getLocal("DoCheckin");
/*  613 */     boolean doCheckin = StringUtils.convertToBool(flag, true);
/*  614 */     flag = this.m_binder.getLocal("DoUpdate");
/*  615 */     boolean doUpdate = StringUtils.convertToBool(flag, false);
/*      */ 
/*  617 */     if (doUpdate)
/*      */     {
/*  619 */       this.m_service.createServiceException(null, "!csFormsUpdateNotSupported");
/*      */     } else {
/*  621 */       if (!doCheckin)
/*      */         return;
/*  623 */       createFdf();
/*  624 */       this.m_service.doCode("determineCheckin");
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void postCheckinFilter() throws ServiceException
/*      */   {
/*      */     try
/*      */     {
/*  633 */       this.m_service.executeFilter("formPostSubmit");
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  637 */       this.m_service.createServiceException(e, "!csFormsUnableToSubmit");
/*      */     }
/*      */   }
/*      */ 
/*      */   protected Properties findRequestWithCleanup() throws DataException, ServiceException
/*      */   {
/*  643 */     String idText = this.m_binder.get("ClientId");
/*  644 */     Long idObj = Long.valueOf(idText, 16);
/*  645 */     long id = idObj.longValue();
/*      */ 
/*  647 */     Properties props = (Properties)m_requestTable.get(idObj);
/*  648 */     if (props == null)
/*      */     {
/*  650 */       String msg = LocaleUtils.encodeMessage("csFormsTimeout", null, "20");
/*      */ 
/*  652 */       this.m_service.createServiceException(null, msg);
/*      */     }
/*      */ 
/*  656 */     m_requestTable.remove(idObj);
/*      */     try
/*      */     {
/*      */       while (true)
/*      */       {
/*  661 */         Long oldestObj = (Long)m_requestQueue.peek();
/*  662 */         long oldest = oldestObj.longValue();
/*  663 */         if ((oldest != id) && (oldest + 1200000L >= id))
/*      */           break;
/*  665 */         m_requestTable.remove(oldestObj);
/*  666 */         m_requestQueue.remove();
/*  667 */         if (oldest == id)
/*      */         {
/*      */           break;
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*      */     }
/*      */     catch (EmptyQueueException e)
/*      */     {
/*  682 */       this.m_service.createServiceException(e, "!csFormsLogicError");
/*      */     }
/*      */ 
/*  685 */     return props;
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void checkRevisionProperties()
/*      */     throws DataException, ServiceException
/*      */   {
/*  704 */     int flags = Integer.parseInt(this.m_currentAction.getParamAt(0));
/*      */ 
/*  707 */     String format = this.m_binder.get("dFormat");
/*  708 */     boolean isForm = false;
/*  709 */     if (format.equalsIgnoreCase("application/fdf"))
/*      */     {
/*      */       Properties fdf;
/*      */       try
/*      */       {
/*  715 */         this.m_binder.putLocal("RenditionId", "primaryFile");
/*  716 */         IdcFileDescriptor d = this.m_service.m_fileStore.createDescriptor(this.m_binder, null, this.m_service);
/*      */ 
/*  718 */         InputStream s = this.m_service.m_fileStore.getInputStream(d, null);
/*  719 */         Reader r = new InputStreamReader(s);
/*  720 */         fdf = Fdf.readFdf(r);
/*  721 */         r.close();
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/*  725 */         this.m_service.createServiceException(e, "!csFormsUnableToReadVaultFile2");
/*  726 */         return;
/*      */       }
/*  728 */       Vector fields = (Vector)fdf.get("/Fields");
/*  729 */       if (fields == null)
/*      */       {
/*  731 */         this.m_service.createServiceException(null, "!csFormsUnableToReadFieldsObject");
/*      */       }
/*  733 */       int l = fields.size();
/*  734 */       isForm = true;
/*  735 */       for (int i = 0; i < l; ++i)
/*      */       {
/*  737 */         Properties fieldData = (Properties)fields.elementAt(i);
/*  738 */         String fieldName = (String)fieldData.get("/T");
/*  739 */         if (!fieldName.equals("(finalVersion)"))
/*      */           continue;
/*  741 */         String fieldValue = (String)fieldData.get("/V");
/*  742 */         fieldValue = fieldValue.substring(1, fieldValue.length() - 1);
/*  743 */         isForm = !StringUtils.convertToBool(fieldValue, false);
/*  744 */         break;
/*      */       }
/*      */ 
/*  747 */       this.m_binder.putLocal("WasForm", "1");
/*      */     }
/*      */ 
/*  750 */     if (isForm)
/*      */     {
/*  752 */       this.m_binder.putLocal("IsForm", "1");
/*      */ 
/*  754 */       if ((flags & 0x1) > 0)
/*      */       {
/*  756 */         this.m_service.m_serviceData.m_htmlPage = this.m_currentAction.getParamAt(1);
/*      */       }
/*      */ 
/*  759 */       if ((flags & 0x2) > 0)
/*      */       {
/*  761 */         this.m_service.createServiceException(null, this.m_currentAction.getParamAt(1));
/*      */       }
/*      */     }
/*      */ 
/*  765 */     if ((flags & 0x4) <= 0)
/*      */       return;
/*  767 */     DocCommonHandler handler = (DocCommonHandler)this.m_service.m_handlerMap.get("DocCommonHandler");
/*      */ 
/*  769 */     if (handler == null)
/*      */       return;
/*  771 */     handler.getURL(true, true);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void retrieveHtmlFormForEdit()
/*      */     throws DataException, ServiceException
/*      */   {
/*  782 */     FormState formState = retrieveHtmlFormState(true);
/*      */     try
/*      */     {
/*  785 */       String str = extractFile(formState.m_fileName);
/*  786 */       this.m_binder.putLocal("fileContents", str);
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/*  790 */       String errMsg = LocaleUtils.encodeMessage("csFormUnableToExtract", null, formState.m_fileName);
/*      */ 
/*  792 */       this.m_service.createServiceException(e, errMsg);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected String extractFile(String fileName) throws IOException
/*      */   {
/*  798 */     String encoding = DataSerializeUtils.determineEncoding(this.m_binder, null);
/*  799 */     FileInputStream fis = new FileInputStream(fileName);
/*  800 */     ByteArrayOutputStream baos = new ByteArrayOutputStream();
/*      */ 
/*  802 */     byte[] tempBuff = new byte[65536];
/*  803 */     boolean isFirst = true;
/*  804 */     int nread = 0;
/*      */     try
/*      */     {
/*  807 */       while ((nread = fis.read(tempBuff)) > 0)
/*      */       {
/*  809 */         int start = 0;
/*  810 */         if ((isFirst) && (encoding != null))
/*      */         {
/*  812 */           encoding = encoding.toLowerCase();
/*  813 */           if (encoding.indexOf("utf") >= 0)
/*      */           {
/*  816 */             start = 3;
/*      */           }
/*      */         }
/*  819 */         baos.write(tempBuff, start, nread - start);
/*  820 */         isFirst = false;
/*      */       }
/*      */     }
/*      */     finally
/*      */     {
/*  825 */       FileUtils.closeObject(fis);
/*      */     }
/*  827 */     byte[] mergeb = baos.toByteArray();
/*  828 */     return StringUtils.getString(mergeb, encoding);
/*      */   }
/*      */ 
/*      */   public FormState retrieveHtmlFormState(boolean isAllowWebForm)
/*      */     throws DataException, ServiceException
/*      */   {
/*  836 */     FormState formState = new FormState();
/*      */ 
/*  838 */     Properties savedLocalData = this.m_binder.getLocalData();
/*  839 */     Properties props = new Properties();
/*  840 */     String idStr = savedLocalData.getProperty("dID");
/*  841 */     if (idStr != null)
/*      */     {
/*  847 */       props.put("dID", idStr);
/*  848 */       props.put("Rendition", "web");
/*  849 */       this.m_service.setConditionVar("AllowWebFormSource", isAllowWebForm);
/*      */ 
/*  854 */       this.m_service.setConditionVar("UseNativeForWebViewable", true);
/*      */ 
/*  866 */       this.m_service.setConditionVar("IsInternalLoadOnly", true);
/*      */     }
/*      */ 
/*  870 */     this.m_binder.setLocalData(props);
/*      */     try
/*      */     {
/*  874 */       this.m_service.executeSafeServiceInNewContext("GET_FILE", false);
/*  875 */       formState.m_fileName = ((String)this.m_service.getCachedObject("PrimaryFilePath"));
/*      */ 
/*  881 */       String docFormat = (String)this.m_service.getCachedObject("DownloadFormat");
/*  882 */       if (docFormat == null)
/*      */       {
/*  884 */         docFormat = this.m_binder.get("dFormat");
/*  885 */         checkFormat(docFormat);
/*      */       }
/*      */ 
/*  888 */       formState.m_isWebForm = (docFormat.indexOf("hcsw") >= 0);
/*  889 */       formState.m_isTemplate = (docFormat.indexOf("hcsf") >= 0);
/*  890 */       this.m_service.setConditionVar("isWebForm", formState.m_isWebForm);
/*  891 */       this.m_service.setConditionVar("isTemplate", formState.m_isTemplate);
/*      */ 
/*  893 */       if ((formState.m_isTemplate) || (formState.m_isWebForm))
/*      */       {
/*  897 */         String query = "QlatestReleasedIDByName";
/*  898 */         if (formState.m_isWebForm)
/*      */         {
/*  900 */           query = "QlatestNonDeletedIDByName";
/*      */         }
/*  902 */         ResultSet rset = this.m_workspace.createResultSet(query, this.m_binder);
/*  903 */         if (rset.isEmpty())
/*      */         {
/*  905 */           String msg = LocaleUtils.encodeMessage("csFormsUnableToFindRevisions", null, this.m_binder.get("dDocName"));
/*      */ 
/*  907 */           this.m_service.createServiceException(null, msg);
/*      */         }
/*  909 */         String str = ResultSetUtils.getValue(rset, "dID");
/*  910 */         if (!str.equals(idStr))
/*      */         {
/*  912 */           this.m_service.createServiceException(null, "!csFormIsNotLatest");
/*      */         }
/*      */       }
/*  915 */       if (!formState.m_isWebForm)
/*      */       {
/*  918 */         formState.m_dataMerger = DataTransformationUtils.parseDynamicData(formState.m_fileName);
/*      */ 
/*  923 */         if (formState.m_dataMerger == null)
/*      */         {
/*  925 */           this.m_service.createServiceException(null, "!csFormsNoData");
/*      */         }
/*      */ 
/*  929 */         formState.m_cfgRules = formState.m_dataMerger.extractRules();
/*  930 */         formState.m_isFinished = StringUtils.convertToBool((String)formState.m_cfgRules.get("isFormFinished"), false);
/*      */ 
/*  933 */         if ((formState.m_isFinished) && (!formState.m_isTemplate))
/*      */         {
/*  935 */           this.m_service.createServiceException(null, "!csFormIsFinished");
/*      */         }
/*      */       }
/*      */     }
/*      */     finally
/*      */     {
/*      */       String val;
/*  943 */       this.m_service.setConditionVar("IsInternalLoadOnly", false);
/*      */ 
/*  946 */       String val = this.m_binder.getLocal("StatusCode");
/*  947 */       if (val != null)
/*      */       {
/*  949 */         savedLocalData.put("StatusCode", val);
/*  950 */         savedLocalData.put("StatusMessage", this.m_binder.getLocal("StatusMessage"));
/*  951 */         savedLocalData.put("StatusMessageKey", this.m_binder.getLocal("StatusMessageKey"));
/*      */       }
/*  953 */       this.m_binder.setLocalData(savedLocalData);
/*      */     }
/*  955 */     return formState;
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void submitHtmlForm()
/*      */     throws DataException, ServiceException
/*      */   {
/*  962 */     FormState formState = retrieveHtmlFormState(false);
/*      */ 
/*  964 */     boolean isAppend = DataBinderUtils.getBoolean(this.m_binder, "isAppend", false);
/*  965 */     boolean isUpdate = (!formState.m_isTemplate) && (isAppend);
/*  966 */     String inheritedFieldsStr = this.m_binder.getLocal("InheritedFields");
/*  967 */     String[] inheritedFieldsArray = null;
/*  968 */     if (inheritedFieldsStr != null)
/*      */     {
/*  970 */       Vector v = StringUtils.parseArray(inheritedFieldsStr, ',', ',');
/*  971 */       inheritedFieldsArray = StringUtils.convertListToArray(v);
/*      */     }
/*      */ 
/*  975 */     Properties revProp = retrieveFormRevisionData(formState.m_isTemplate, isUpdate);
/*      */ 
/*  978 */     if (this.m_currentAction.getNumParams() > 0)
/*      */     {
/*  980 */       String keepKey = this.m_currentAction.getParamAt(0);
/*  981 */       this.m_service.moveResultSetToCache(keepKey);
/*      */     }
/*  983 */     this.m_binder.clearResultSets();
/*      */ 
/*  988 */     if (inheritedFieldsArray != null)
/*      */     {
/*  990 */       Hashtable h = new Hashtable();
/*  991 */       h.put("FieldList", inheritedFieldsArray);
/*  992 */       formState.m_dataMerger.mergeInto(this.m_binder, h);
/*      */     }
/*      */ 
/*  996 */     this.m_service.setConditionVar("IsCreateHtmlForm", true);
/*  997 */     this.m_service.setCachedObject("HtmlFormState", formState);
/*  998 */     this.m_service.setConditionVar("IsFormUpdate", isUpdate);
/*  999 */     this.m_service.setCachedObject("IsTemplate", String.valueOf(formState.m_isTemplate));
/* 1000 */     this.m_binder.putLocal("ValidatePrimaryFile", "0");
/*      */ 
/* 1007 */     this.m_service.setConditionVar("IsSubmit", true);
/*      */ 
/* 1009 */     mergeInData(revProp, formState.m_isTemplate, isUpdate);
/*      */ 
/* 1011 */     this.m_service.executeFilter("submitHtmlForm");
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void buildHtmlFormForCheckin() throws ServiceException, DataException
/*      */   {
/* 1017 */     boolean isUpdate = this.m_service.isConditionVarTrue("IsFormUpdate");
/* 1018 */     FormState formState = (FormState)this.m_service.getCachedObject("HtmlFormState");
/*      */ 
/* 1021 */     updateFormData(formState.m_dataMerger, formState.m_cfgRules, isUpdate);
/*      */ 
/* 1024 */     createHtmlForm(formState.m_fileName, formState.m_dataMerger);
/*      */   }
/*      */ 
/*      */   protected void checkFormat(String docFormat)
/*      */     throws ServiceException
/*      */   {
/* 1032 */     if (docFormat.indexOf("hcs") >= 0)
/*      */       return;
/* 1034 */     String msg = LocaleUtils.encodeMessage("csFormHasInvalidFormat", null, docFormat);
/* 1035 */     this.m_service.createServiceException(null, msg);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void restoreSavedDocInfo()
/*      */     throws DataException
/*      */   {
/* 1042 */     String restoreKey = this.m_currentAction.getParamAt(0);
/* 1043 */     ResultSet rset = (ResultSet)this.m_service.getCachedObject("FormDocInfoResultSet");
/* 1044 */     if (rset == null)
/*      */       return;
/* 1046 */     this.m_binder.addResultSet(restoreKey, rset);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void determineFormCheckin()
/*      */     throws DataException, ServiceException
/*      */   {
/* 1055 */     boolean isTemplate = this.m_service.isConditionVarTrue("isTemplate");
/* 1056 */     boolean isAppend = DataBinderUtils.getBoolean(this.m_binder, "isAppend", false);
/* 1057 */     if ((isAppend) && (!isTemplate) && (this.m_currentAction.getNumParams() > 0))
/*      */     {
/* 1059 */       String lookupKey = this.m_currentAction.getParamAt(0);
/* 1060 */       ResultSet docInfoRset = this.m_binder.getResultSet(lookupKey);
/*      */ 
/* 1062 */       ResultSet rset = this.m_workspace.createResultSet("QdocName", this.m_binder);
/* 1063 */       if (rset.isEmpty())
/*      */       {
/* 1065 */         String msg = LocaleUtils.encodeMessage("csContentItemNoLongerExists", null, this.m_binder.getLocal("dDocName"));
/*      */ 
/* 1067 */         this.m_service.createServiceException(null, msg);
/*      */       }
/* 1069 */       String latestID = ResultSetUtils.getValue(rset, "dID");
/* 1070 */       if ((!latestID.equalsIgnoreCase(this.m_binder.getLocal("dID"))) && (docInfoRset != null))
/*      */       {
/* 1073 */         String oldInDate = ResultSetUtils.getValue(docInfoRset, "dInDate");
/* 1074 */         this.m_binder.putLocal("dInDate", oldInDate);
/*      */       }
/* 1076 */       this.m_service.executeService("UPDATE_BYREV");
/*      */     }
/*      */     else
/*      */     {
/* 1081 */       this.m_service.doCode("determineCheckin");
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void prepareForPreview() throws ServiceException, DataException
/*      */   {
/* 1088 */     this.m_binder.putLocal("IsHtmlForm", "1");
/*      */ 
/* 1090 */     String docName = this.m_binder.getAllowMissing("dDocName");
/* 1091 */     if ((docName == null) || (docName.length() == 0))
/*      */     {
/* 1093 */       String prvName = this.m_binder.get("PreviewDocName");
/* 1094 */       this.m_binder.putLocal("dDocName", prvName);
/* 1095 */       this.m_binder.putLocal("IsPreviewName", "1");
/*      */     }
/*      */ 
/* 1099 */     String val = this.m_binder.getAllowMissing("dRevLabel");
/* 1100 */     if (val == null)
/*      */     {
/* 1102 */       this.m_binder.putLocal("dRevLabel", RevisionSpec.getFirst());
/* 1103 */       this.m_binder.putLocal("dRevisionID", "-1");
/*      */     }
/* 1105 */     this.m_binder.putLocal("dRevClassID", "-1");
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void postHtmlFormCheckin()
/*      */     throws ServiceException, DataException
/*      */   {
/* 1112 */     DynamicDataMerger dataMerger = (DynamicDataMerger)this.m_service.getCachedObject("DataMerger");
/* 1113 */     dataMerger.mergeInto(this.m_binder, null);
/*      */ 
/* 1115 */     boolean isTemplate = StringUtils.convertToBool((String)this.m_service.getCachedObject("IsTemplate"), true);
/* 1116 */     if (!isTemplate)
/*      */     {
/* 1119 */       this.m_binder.putLocal("SourceID", this.m_binder.getLocal("dID"));
/* 1120 */       String docName = this.m_binder.getLocal("dDocName");
/* 1121 */       if (docName != null)
/*      */       {
/* 1123 */         this.m_binder.putLocal("SourceName", docName);
/*      */       }
/*      */     }
/* 1126 */     this.m_service.executeFilter("postHtmlFormCheckin");
/*      */   }
/*      */ 
/*      */   protected Properties retrieveFormRevisionData(boolean isTemplate, boolean isUpdate)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1136 */     Properties revProps = null;
/* 1137 */     Properties curLocalData = this.m_binder.getLocalData();
/*      */     try
/*      */     {
/* 1140 */       Properties props = new Properties();
/* 1141 */       this.m_binder.setLocalData(props);
/* 1142 */       String dID = this.m_binder.get("dID");
/*      */ 
/* 1144 */       if (!isUpdate)
/*      */       {
/* 1146 */         validateDocName(dID, isTemplate);
/*      */       }
/* 1148 */       DataResultSet drset = (DataResultSet)this.m_binder.getResultSet("FILE_DOC_INFO");
/* 1149 */       if ((drset == null) || (drset.isEmpty()))
/*      */       {
/* 1152 */         this.m_service.createServiceException(null, "!csRevisionNoLongerExists");
/*      */       }
/*      */ 
/* 1155 */       revProps = computeRevInfo(this.m_binder, drset, dID, isTemplate, isUpdate);
/*      */ 
/* 1157 */       boolean isPreviewUpload = this.m_service.isConditionVarTrue("IsPreviewUpload");
/* 1158 */       if ((!isTemplate) && (!isPreviewUpload) && (!isUpdate))
/*      */       {
/* 1161 */         String checkOutUser = this.m_binder.get("dCheckoutUser");
/* 1162 */         if ((checkOutUser == null) || (checkOutUser.length() == 0))
/*      */         {
/* 1166 */           this.m_binder.addResultSet("DOC_INFO", drset);
/*      */ 
/* 1168 */           this.m_service.executeServiceEx("CHECKOUT_SUB", false);
/* 1169 */           this.m_binder.removeResultSet("DOC_INFO");
/*      */         }
/*      */         else
/*      */         {
/* 1173 */           UserData userData = this.m_service.getUserData();
/* 1174 */           if (!userData.m_name.equals(checkOutUser))
/*      */           {
/* 1176 */             this.m_service.createServiceException(null, "!csFormAlreadyCheckedOut");
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/*      */     finally
/*      */     {
/*      */       String val;
/* 1184 */       String val = this.m_binder.getLocal("StatusCode");
/* 1185 */       if (val != null)
/*      */       {
/* 1187 */         curLocalData.put("StatusCode", val);
/* 1188 */         curLocalData.put("StatusMessage", this.m_binder.getLocal("StatusMessage"));
/* 1189 */         curLocalData.put("StatusMessageKey", this.m_binder.getLocal("StatusMessageKey"));
/*      */       }
/* 1191 */       this.m_binder.setLocalData(curLocalData);
/*      */     }
/*      */ 
/* 1194 */     return revProps;
/*      */   }
/*      */ 
/*      */   protected void updateFormData(DynamicDataMerger dataMerger, Hashtable cfgRules, boolean isUpdate)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1203 */     String script = this.m_binder.getLocal("DataScript");
/* 1204 */     if ((script != null) && (script.length() > 0))
/*      */     {
/*      */       try
/*      */       {
/* 1208 */         this.m_service.m_pageMerger.evaluateScript(script);
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/* 1212 */         String msg = LocaleUtils.encodeMessage("csFormsUnableToEvalDataScript", null);
/*      */ 
/* 1214 */         this.m_service.createServiceException(e, msg);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1220 */     Properties localData = this.m_binder.getLocalData();
/* 1221 */     Properties defProps = new Properties();
/* 1222 */     for (Enumeration en = localData.keys(); en.hasMoreElements(); )
/*      */     {
/* 1224 */       String key = (String)en.nextElement();
/* 1225 */       if (key.endsWith(":default"))
/*      */       {
/* 1227 */         int len = key.length() - 8;
/* 1228 */         String lookupKey = key.substring(0, len);
/* 1229 */         String value = localData.getProperty(lookupKey);
/* 1230 */         if (value == null)
/*      */         {
/* 1233 */           String defVal = localData.getProperty(key);
/*      */           try
/*      */           {
/* 1236 */             value = this.m_service.m_pageMerger.evaluateScript(defVal);
/*      */           }
/*      */           catch (Exception e)
/*      */           {
/* 1240 */             String msg = LocaleUtils.encodeMessage("csFormsUnableToEvalDefault", null, key);
/*      */ 
/* 1242 */             this.m_service.createServiceException(e, msg);
/*      */           }
/*      */ 
/* 1245 */           defProps.put(lookupKey, value);
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/* 1250 */     if (isUpdate)
/*      */     {
/* 1252 */       cfgRules.put("isAppendMode", "1");
/*      */     }
/* 1254 */     dataMerger.mergeFrom(this.m_binder, defProps, cfgRules);
/*      */ 
/* 1256 */     this.m_service.setCachedObject("DataMerger", dataMerger);
/*      */   }
/*      */ 
/*      */   protected void createHtmlForm(String primaryFilePath, DynamicDataMerger dataMerger)
/*      */     throws ServiceException
/*      */   {
/* 1263 */     String fileName = DataTransformationUtils.createMergedFile(primaryFilePath, dataMerger);
/* 1264 */     this.m_binder.addTempFile(fileName);
/*      */ 
/* 1266 */     this.m_binder.putLocal("primaryFile:path", fileName);
/* 1267 */     this.m_binder.putLocal("dExtension", "hcsp");
/* 1268 */     this.m_binder.putLocal("dFormat", "form/hcsp");
/* 1269 */     this.m_binder.putLocal("primaryFile:format", "form/hcsp");
/*      */   }
/*      */ 
/*      */   protected Properties computeRevInfo(DataBinder binder, DataResultSet drset, String id, boolean isTemplate, boolean isUpdate)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1275 */     binder.putLocal("dID", id);
/*      */ 
/* 1278 */     Properties props = drset.getCurrentRowProps();
/*      */ 
/* 1280 */     binder.putLocal("dUser", this.m_service.m_userData.m_name);
/* 1281 */     if (isTemplate)
/*      */     {
/* 1283 */       props.remove("dRevLabel");
/* 1284 */       props.remove("dRevisionID");
/*      */     }
/* 1286 */     else if (!isUpdate)
/*      */     {
/* 1290 */       binder.putLocal("CurRevID", id);
/* 1291 */       binder.putLocal("latestID", id);
/*      */ 
/* 1293 */       String isCheckedOut = props.getProperty("dIsCheckedOut");
/* 1294 */       binder.putLocal("CurRevIsCheckedOut", isCheckedOut);
/*      */ 
/* 1296 */       String checkoutUser = props.getProperty("dCheckoutUser");
/* 1297 */       if ((StringUtils.convertToBool(isCheckedOut, false)) && (checkoutUser != null))
/*      */       {
/* 1299 */         binder.putLocal("CurRevCheckoutUser", checkoutUser);
/*      */       }
/*      */ 
/* 1303 */       binder.putLocal("dSecurityGroup", props.getProperty("dSecurityGroup"));
/* 1304 */       String val = props.getProperty("dDocAccount");
/* 1305 */       if (val == null)
/*      */       {
/* 1307 */         val = "";
/*      */       }
/* 1309 */       binder.putLocal("dDocAccount", val);
/*      */ 
/* 1312 */       props.remove("dReleaseDate");
/* 1313 */       props.remove("dCreateDate");
/* 1314 */       props.remove("dInDate");
/*      */     }
/*      */ 
/* 1317 */     return props;
/*      */   }
/*      */ 
/*      */   protected void validateDocName(String dID, boolean isTemplate)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1323 */     if (isTemplate)
/*      */     {
/* 1326 */       String docName = this.m_binder.getLocal("dDocName");
/*      */ 
/* 1328 */       if (docName != null)
/*      */       {
/* 1332 */         this.m_binder.putLocal("dDocName", docName);
/* 1333 */         ResultSet rset = this.m_workspace.createResultSet("QdocName", this.m_binder);
/* 1334 */         if (rset.isRowPresent())
/*      */         {
/* 1336 */           String msg = LocaleUtils.encodeMessage("csCannotCreateContentItem", null, docName);
/*      */ 
/* 1338 */           this.m_service.createServiceException(null, msg);
/*      */         }
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/* 1344 */       String docName = this.m_binder.get("dDocName");
/* 1345 */       this.m_binder.putLocal("dDocName", docName);
/*      */ 
/* 1347 */       ResultSet rset = this.m_workspace.createResultSet("QdocName", this.m_binder);
/* 1348 */       if (rset.isEmpty())
/*      */       {
/* 1350 */         String msg = LocaleUtils.encodeMessage("csContentItemNoLongerExists", null, docName);
/*      */ 
/* 1352 */         this.m_service.createServiceException(null, msg);
/*      */       }
/* 1354 */       String latestID = ResultSetUtils.getValue(rset, "dID");
/* 1355 */       if (latestID.equalsIgnoreCase(dID))
/*      */         return;
/* 1357 */       this.m_service.createServiceException(null, "!csFormsRevisionIsNotLatest");
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void mergeInData(Properties revProps, boolean isTemplate, boolean isUpdate)
/*      */     throws ServiceException
/*      */   {
/* 1365 */     if (isTemplate)
/*      */     {
/* 1367 */       mergeForTemplate(revProps);
/*      */     }
/*      */     else
/*      */     {
/* 1373 */       mergeInRevisionData(revProps);
/*      */     }
/*      */ 
/* 1376 */     if (!isUpdate)
/*      */     {
/* 1378 */       doOverrides();
/*      */     }
/*      */ 
/* 1382 */     if (!isUpdate)
/*      */     {
/* 1384 */       String revLabel = this.m_binder.getLocal("dRevLabel");
/* 1385 */       String latestRevLabel = this.m_binder.getLocal("dRevLabel");
/* 1386 */       if ((revLabel == null) && (latestRevLabel != null))
/*      */       {
/* 1388 */         this.m_binder.putLocal("latestRevLabel", latestRevLabel);
/*      */       }
/* 1390 */       else if ((revLabel != null) && (latestRevLabel != null) && (revLabel.equalsIgnoreCase(latestRevLabel)))
/*      */       {
/* 1393 */         this.m_binder.removeLocal("dRevLabel");
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/* 1398 */       String docName = revProps.getProperty("dDocName");
/* 1399 */       this.m_binder.putLocal("dDocName", docName);
/* 1400 */       String revLabel = revProps.getProperty("dRevLabel");
/* 1401 */       this.m_binder.putLocal("dRevLabel", revLabel);
/*      */     }
/*      */ 
/* 1404 */     createSpecialFields(isTemplate);
/*      */   }
/*      */ 
/*      */   protected void mergeForTemplate(Properties revProps)
/*      */   {
/* 1409 */     String baseCanCopyList = "dDocType,dSecurityGroup,dDocAccount";
/* 1410 */     String canCopyList = SharedObjects.getEnvironmentValue("FormInheritMetaDataFieldList");
/* 1411 */     if ((canCopyList == null) || (canCopyList.length() == 0))
/*      */     {
/* 1413 */       canCopyList = baseCanCopyList;
/*      */     }
/*      */     else
/*      */     {
/* 1417 */       canCopyList = canCopyList + "," + baseCanCopyList;
/*      */     }
/*      */ 
/* 1420 */     Vector canCopy = StringUtils.parseArray(canCopyList, ',', ',');
/* 1421 */     int num = canCopy.size();
/* 1422 */     for (int i = 0; i < num; ++i)
/*      */     {
/* 1424 */       String key = (String)canCopy.elementAt(i);
/* 1425 */       if (key == null) continue; if (key.length() == 0)
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/* 1430 */       if (this.m_binder.getLocal(key) != null)
/*      */         continue;
/* 1432 */       String old = revProps.getProperty(key);
/* 1433 */       if (old == null)
/*      */         continue;
/* 1435 */       this.m_binder.putLocal(key, old);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void mergeInRevisionData(Properties revProps)
/*      */   {
/* 1443 */     for (Enumeration en = revProps.keys(); en.hasMoreElements(); )
/*      */     {
/* 1445 */       String key = (String)en.nextElement();
/* 1446 */       if (!DocFieldUtils.isDocComputedField(key))
/*      */       {
/* 1448 */         String val = this.m_binder.getLocal(key);
/* 1449 */         if (val == null)
/*      */         {
/* 1451 */           val = revProps.getProperty(key);
/* 1452 */           this.m_binder.putLocal(key, val);
/*      */         }
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void createSpecialFields(boolean isTemplate) throws ServiceException
/*      */   {
/* 1460 */     if (!isTemplate)
/*      */     {
/* 1462 */       return;
/*      */     }
/*      */ 
/* 1466 */     String docTitle = this.m_binder.getLocal("dDocTitle");
/* 1467 */     if ((docTitle != null) && (docTitle.length() != 0))
/*      */       return;
/* 1469 */     String dDocType = this.m_binder.getLocal("dDocType");
/* 1470 */     String script = this.m_binder.getAllowMissing("FormDocTitleScript" + dDocType);
/* 1471 */     if (script == null)
/*      */     {
/* 1473 */       script = this.m_binder.getAllowMissing("FormDocTitleScript");
/*      */     }
/* 1475 */     if (script == null)
/*      */     {
/* 1477 */       script = "<$dDocAuthor$> - <$dDocType$> - <$dateCurrent()$>";
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/* 1482 */       docTitle = this.m_service.m_pageMerger.evaluateScript(script);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 1486 */       this.m_service.createServiceException(e, "!csFormsUnableToComputeTitle");
/*      */     }
/*      */ 
/* 1489 */     this.m_binder.putLocal("dDocTitle", docTitle);
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1495 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98120 $";
/*      */   }
/*      */ 
/*      */   public class FormState
/*      */   {
/*      */     public boolean m_isTemplate;
/*      */     public boolean m_isFinished;
/*      */     public boolean m_isWebForm;
/*      */     public DynamicDataMerger m_dataMerger;
/*      */     public String m_fileName;
/*      */     public Hashtable m_cfgRules;
/*      */ 
/*      */     public FormState()
/*      */     {
/*      */     }
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.FormHandler
 * JD-Core Version:    0.5.4
 */