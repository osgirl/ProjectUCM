/*      */ package intradoc.server;
/*      */ 
/*      */ import intradoc.common.DataStreamWrapper;
/*      */ import intradoc.common.FileUtils;
/*      */ import intradoc.common.IdcMessageFactory;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.NumberUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ResourceContainerUtils;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.Table;
/*      */ import intradoc.common.Validation;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataBinderUtils;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.DataSerializeUtils;
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.data.IdcCounterUtils;
/*      */ import intradoc.data.IdcDataSourceUtils;
/*      */ import intradoc.data.MapParameters;
/*      */ import intradoc.data.Parameters;
/*      */ import intradoc.data.PropParameters;
/*      */ import intradoc.data.ResultSet;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.data.Workspace;
/*      */ import intradoc.filestore.FileStoreProvider;
/*      */ import intradoc.filestore.FileStoreProviderHelper;
/*      */ import intradoc.filestore.FileStoreProviderLoader;
/*      */ import intradoc.filestore.IdcDescriptorState;
/*      */ import intradoc.filestore.IdcFileDescriptor;
/*      */ import intradoc.provider.ProviderInterface;
/*      */ import intradoc.provider.WorkspaceProviderConfigUtils;
/*      */ import intradoc.refinery.ConvertedWorkThread;
/*      */ import intradoc.refinery.RefineryUtils;
/*      */ import intradoc.resource.ResourceCacheState;
/*      */ import intradoc.server.docstatelocking.DocumentStateLockUtils;
/*      */ import intradoc.server.utils.EnvironmentPackager;
/*      */ import intradoc.server.utils.ServerFileUtils;
/*      */ import intradoc.server.workflow.WorkflowDocImplementor;
/*      */ import intradoc.server.workflow.WorkflowUtils;
/*      */ import intradoc.shared.AdditionalRenditions;
/*      */ import intradoc.shared.CollaborationUtils;
/*      */ import intradoc.shared.Collaborations;
/*      */ import intradoc.shared.ComponentClassFactory;
/*      */ import intradoc.shared.FieldDef;
/*      */ import intradoc.shared.MetaFieldData;
/*      */ import intradoc.shared.MetaFieldUtils;
/*      */ import intradoc.shared.PluginFilters;
/*      */ import intradoc.shared.RevisionSpec;
/*      */ import intradoc.shared.SecurityUtils;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.shared.UserData;
/*      */ import intradoc.shared.UserUtils;
/*      */ import intradoc.shared.ViewFields;
/*      */ import intradoc.shared.workflow.WorkflowScriptUtils;
/*      */ import intradoc.util.IdcMessage;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.io.BufferedWriter;
/*      */ import java.io.File;
/*      */ import java.io.IOException;
/*      */ import java.io.OutputStream;
/*      */ import java.io.Writer;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Date;
/*      */ import java.util.HashMap;
/*      */ import java.util.Hashtable;
/*      */ import java.util.Iterator;
/*      */ import java.util.LinkedHashMap;
/*      */ import java.util.List;
/*      */ import java.util.Locale;
/*      */ import java.util.Map;
/*      */ import java.util.Properties;
/*      */ import java.util.Set;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class DocServiceHandler extends ServiceHandler
/*      */ {
/*      */   public static final int REVCLASS_ID = 0;
/*      */   public static final int REV_ID = 1;
/*      */   public static final int DOC_ID = 2;
/*  110 */   public static final String[][] ID_INCREMENTS = { { "QnextRevClassID", "UnextRevClassID", "dRevClassID", "RevClassID" }, { "QnextRevID", "UnextRevID", "dID", "RevID" }, { "QnextDocID", "UnextDocID", "dDocID", "DocID" } };
/*      */ 
/*  117 */   static final String[] renditions = { "webViewableFile", "renditions", "primaryFile", "alternateFile" };
/*      */   public static final int EARLY_CHECK = 0;
/*      */   public static final int FINAL_CHECK = 1;
/*  124 */   public boolean m_isWf = false;
/*  125 */   public String m_vaultPath = null;
/*      */ 
/*  128 */   public boolean m_copyFiles = false;
/*      */ 
/*  132 */   public boolean m_isSplitDir = false;
/*      */ 
/*  138 */   public boolean m_addReleasedWebFile = false;
/*      */ 
/*  143 */   public boolean m_useRenameWhenPossible = false;
/*      */ 
/*  146 */   public Date m_checkInDate = null;
/*      */ 
/*  148 */   public WorkflowDocImplementor m_wfImplementor = null;
/*      */ 
/*  150 */   public FileStoreProvider m_fileStore = null;
/*      */ 
/*  152 */   public boolean m_isJava = true;
/*  153 */   public boolean m_isStandAlone = true;
/*      */ 
/*  155 */   public boolean m_changePrimaryFileExtension = false;
/*      */ 
/*  157 */   public OutputStream m_output = null;
/*      */ 
/*  160 */   public boolean m_checkUnique = false;
/*      */ 
/*  163 */   public boolean m_isNewRevClass = false;
/*      */ 
/*  167 */   public boolean m_determinedPromotedID = false;
/*  168 */   public String m_promotedID = null;
/*      */ 
/*  171 */   public boolean m_determinedMinimumRankDeleteRevID = false;
/*  172 */   public String m_minimumRankDeleteRevID = null;
/*      */ 
/*  176 */   public Map m_lockedIDsList = new HashMap();
/*  177 */   public Map m_filesToDelete = null;
/*      */ 
/*  179 */   public HashMap<String, String> m_upperCaseColumns = null;
/*      */ 
/* 1904 */   private static String[] STD_FIELDS_TO_REPOPULATE = { "dDocTitle", "dDocType", "dSecurityGroup", "dDocAccount" };
/*      */ 
/*      */   public void init(Service service)
/*      */     throws ServiceException, DataException
/*      */   {
/*  191 */     super.init(service);
/*  192 */     this.m_output = service.getOutput();
/*  193 */     this.m_isJava = service.isJava();
/*  194 */     this.m_isStandAlone = service.isStandAlone();
/*      */ 
/*  196 */     this.m_vaultPath = LegacyDirectoryLocator.getVaultDirectory();
/*      */ 
/*  198 */     String doFileCopy = this.m_binder.getAllowMissing("doFileCopy");
/*  199 */     this.m_copyFiles = StringUtils.convertToBool(doFileCopy, false);
/*  200 */     String isSplitDir = this.m_binder.getEnvironmentValue("isPhysicallySplitDir");
/*  201 */     if (isSplitDir == null)
/*      */     {
/*  203 */       isSplitDir = this.m_binder.getEnvironmentValue("IsPhysicallySplitDir");
/*      */     }
/*  205 */     this.m_isSplitDir = StringUtils.convertToBool(isSplitDir, false);
/*  206 */     String useRename = this.m_binder.getEnvironmentValue("DoRenameWhenPossible");
/*  207 */     this.m_useRenameWhenPossible = ((!this.m_isSplitDir) && (StringUtils.convertToBool(useRename, false)));
/*      */ 
/*  216 */     String addReleasedWebFile = this.m_binder.getLocal("addReleasedWebFile");
/*  217 */     this.m_addReleasedWebFile = StringUtils.convertToBool(addReleasedWebFile, false);
/*      */ 
/*  228 */     this.m_filesToDelete = checkCreateOrRegisterLinkedMap("FilesToDelete");
/*  229 */     this.m_lockedIDsList = checkCreateOrRegisterLinkedMap("LockedIDsList");
/*      */ 
/*  231 */     this.m_fileStore = this.m_service.m_fileStore;
/*  232 */     boolean isPreserveCase = false;
/*  233 */     if (this.m_workspace != null)
/*      */     {
/*  235 */       isPreserveCase = StringUtils.convertToBool(this.m_workspace.getProperty("DatabasePreserveCase"), false);
/*      */     }
/*  237 */     if ((!isPreserveCase) || (SharedObjects.getEnvValueAsBoolean("DisableUpperCaseValueOnCheckinReturn", false)))
/*      */       return;
/*  239 */     this.m_upperCaseColumns = ((HashMap)WorkspaceProviderConfigUtils.retrieveConfig((ProviderInterface)this.m_workspace, "ParsedUpperCaseColumns"));
/*      */   }
/*      */ 
/*      */   public Map checkCreateOrRegisterLinkedMap(String id)
/*      */   {
/*  245 */     Map m = null;
/*  246 */     Object o = this.m_service.getCachedObject(id);
/*  247 */     if ((o != null) && (o instanceof Map))
/*      */     {
/*  249 */       m = (Map)o;
/*      */     }
/*      */     else
/*      */     {
/*  253 */       m = new LinkedHashMap();
/*  254 */       this.m_service.setCachedObject(id, m);
/*      */     }
/*  256 */     return m;
/*      */   }
/*      */ 
/*      */   public void initImplementor()
/*      */     throws ServiceException
/*      */   {
/*  262 */     String className = "intradoc.server.workflow.WorkflowDocImplementor";
/*  263 */     this.m_wfImplementor = ((WorkflowDocImplementor)ComponentClassFactory.createClassInstance("WorkflowDocImplementor", className, "!csWorkflowImplementorMissing"));
/*      */ 
/*  267 */     this.m_wfImplementor.init(this.m_service);
/*      */   }
/*      */ 
/*      */   public void checkInitImplementor()
/*      */     throws ServiceException
/*      */   {
/*  276 */     if (this.m_wfImplementor != null)
/*      */       return;
/*  278 */     initImplementor();
/*      */   }
/*      */ 
/*      */   public WorkflowDocImplementor getWorkflowImplementor()
/*      */   {
/*  284 */     return this.m_wfImplementor;
/*      */   }
/*      */ 
/*      */   public boolean isWorkflow()
/*      */   {
/*  289 */     return this.m_isWf;
/*      */   }
/*      */ 
/*      */   public void setIsWorkflow(boolean isWf)
/*      */   {
/*  294 */     this.m_isWf = isWf;
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void preCheckinFilter()
/*      */     throws ServiceException, DataException
/*      */   {
/*  301 */     PluginFilters.filter("preCheckinFilter", this.m_workspace, this.m_binder, this.m_service);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void postCheckinUniversalFilter() throws ServiceException, DataException
/*      */   {
/*  307 */     PluginFilters.filter("postCheckinUniversalFilter", this.m_workspace, this.m_binder, this.m_service);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void checkInByID()
/*      */     throws DataException, ServiceException
/*      */   {
/*  318 */     String isUpdateStr = this.m_binder.getAllowMissing("IsUpdate");
/*  319 */     if (isUpdateStr != null)
/*      */     {
/*  322 */       this.m_binder.putLocal("IsUpdate", "");
/*      */     }
/*  324 */     String releaseState = "N";
/*  325 */     if (this.m_isWf)
/*      */     {
/*  327 */       releaseState = "E";
/*      */     }
/*      */ 
/*  331 */     this.m_binder.putLocal("dReleaseState", releaseState);
/*  332 */     checkInRevByID(false);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void checkInUpdateRevByID()
/*      */     throws DataException, ServiceException
/*      */   {
/*  339 */     this.m_binder.putLocal("IsUpdate", "1");
/*      */ 
/*  341 */     checkInRevByID(true);
/*      */   }
/*      */ 
/*      */   protected void checkInRevByID(boolean isUpdate)
/*      */     throws DataException, ServiceException
/*      */   {
/*  347 */     String docName = this.m_binder.getLocal("dDocName");
/*  348 */     String oldName = this.m_binder.getLocal("oldName");
/*  349 */     if (!docName.equalsIgnoreCase(oldName))
/*      */     {
/*  352 */       if (this.m_isWf)
/*      */       {
/*  354 */         String msg = LocaleUtils.encodeMessage("csDocNameMismatch", null, docName, oldName);
/*  355 */         this.m_service.createServiceException(null, msg);
/*      */       }
/*  357 */       makeNewRevClass();
/*  358 */       return;
/*      */     }
/*      */ 
/*  362 */     String id = this.m_binder.getLocal("dID");
/*  363 */     if (id == null)
/*      */     {
/*  365 */       this.m_service.createServiceException(null, "!csCheckinItemExists");
/*      */     }
/*  367 */     this.m_binder.putLocal("CurRevID", id);
/*  368 */     isLatestID();
/*  369 */     checkIsNotPublished();
/*      */ 
/*  372 */     prepareDocAuthor();
/*      */ 
/*  375 */     canCheckInCheckedOutDoc(docName);
/*      */ 
/*  377 */     if (!isUpdate)
/*      */     {
/*  379 */       updateRevisionID(false);
/*      */     }
/*      */ 
/*  383 */     validateRevLabel(isUpdate, id);
/*      */   }
/*      */ 
/*      */   protected void validateRevLabel(boolean isUpdate, String curRevID)
/*      */     throws DataException, ServiceException
/*      */   {
/*  390 */     if (!SharedObjects.getEnvValueAsBoolean("ForceDistinctRevLabels", false))
/*      */       return;
/*  392 */     boolean validRevLabel = true;
/*      */     try
/*      */     {
/*  395 */       ResultSet rset = this.m_workspace.createResultSet("QdocRev", this.m_binder);
/*  396 */       if (isUpdate)
/*      */       {
/*  398 */         for (rset.first(); ; rset.next()) { if (!rset.isRowPresent())
/*      */             break label92;
/*  400 */           String id = ResultSetUtils.getValue(rset, "dID");
/*  401 */           if (id.equals(curRevID))
/*      */             continue;
/*  403 */           validRevLabel = false;
/*  404 */           break label92: }
/*      */ 
/*      */ 
/*      */       }
/*      */ 
/*  410 */       label92: validRevLabel = rset.isEmpty();
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  415 */       this.m_service.createServiceException(e, "Unable to validate revision label.");
/*      */     }
/*  417 */     if (validRevLabel)
/*      */       return;
/*  419 */     String msg = LocaleUtils.encodeMessage("csCheckinRevLabelAlreadyExists", null, this.m_binder.get("dRevLabel"));
/*      */ 
/*  421 */     this.m_service.createServiceException(null, msg);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void checkInByName()
/*      */     throws DataException, ServiceException
/*      */   {
/*  432 */     boolean doesExist = false;
/*  433 */     String query = this.m_currentAction.getParamAt(0);
/*  434 */     ResultSet rset = this.m_workspace.createResultSet(query, this.m_binder);
/*  435 */     if ((rset != null) && (rset.isRowPresent()))
/*      */     {
/*  437 */       doesExist = true;
/*  438 */       this.m_service.mapValues(rset);
/*      */     }
/*      */ 
/*  441 */     if (!doesExist)
/*      */     {
/*  443 */       makeNewRevClass();
/*      */     }
/*      */     else
/*      */     {
/*  449 */       String id = this.m_binder.getLocal("dID");
/*  450 */       if (id == null)
/*      */       {
/*  452 */         this.m_service.createServiceException(null, "!csCheckinItemExists");
/*      */       }
/*  454 */       this.m_service.lockContent("dDocName");
/*  455 */       this.m_binder.putLocal("CurRevID", id);
/*  456 */       isLatestID();
/*  457 */       checkIsNotPublished();
/*  458 */       updateRevisionID(false);
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void prepareInsertNew() throws DataException, ServiceException
/*      */   {
/*  465 */     boolean docExists = StringUtils.convertToBool(this.m_binder.getLocal("DocExists"), false);
/*      */ 
/*  468 */     if (!docExists)
/*      */     {
/*  470 */       makeNewRevClass();
/*      */     }
/*      */     else
/*      */     {
/*  475 */       this.m_workspace.execute("Uuncheckout", this.m_binder);
/*      */ 
/*  479 */       String id = this.m_binder.getLocal("dID");
/*  480 */       if (id == null)
/*      */       {
/*  482 */         throw new DataException("!csCheckinUnableToInsertByName");
/*      */       }
/*      */ 
/*  485 */       boolean updateRevLabel = this.m_binder.getLocal("dRevLabel") == null;
/*      */ 
/*  487 */       if (updateRevLabel)
/*      */       {
/*  489 */         String latestRevLabel = this.m_binder.getLocal("latestRevLabel");
/*  490 */         if (latestRevLabel != null)
/*      */         {
/*  492 */           this.m_binder.putLocal("dRevLabel", latestRevLabel);
/*      */         }
/*      */       }
/*      */ 
/*  496 */       updateRevisionID(updateRevLabel);
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void addFiles()
/*      */     throws DataException, ServiceException
/*      */   {
/*  505 */     deleteFilesInDeleteList();
/*      */ 
/*  507 */     if (PluginFilters.filter("addFiles", this.m_workspace, this.m_binder, this.m_service) == -1)
/*      */     {
/*  510 */       return;
/*      */     }
/*      */ 
/*  514 */     boolean reusePreviousRevisionsOriginalName = StringUtils.convertToBool(this.m_binder.getLocal("reusePreviousRevisionsOriginalName"), false);
/*  515 */     String curRevID = this.m_binder.getLocal("CurRevID");
/*  516 */     if ((reusePreviousRevisionsOriginalName) && (curRevID != null) && (curRevID.length() > 0))
/*      */     {
/*  518 */       DataBinder tmpBinder = new DataBinder();
/*  519 */       tmpBinder.putLocal("dID", curRevID);
/*  520 */       ResultSet rset = this.m_workspace.createResultSet("QdocumentPrimary", tmpBinder);
/*  521 */       if (rset.isRowPresent())
/*      */       {
/*  523 */         this.m_binder.putLocal("dOriginalName", ResultSetUtils.getValue(rset, "dOriginalName"));
/*      */       }
/*      */     }
/*  526 */     String docQuery = this.m_currentAction.getParamAt(0);
/*  527 */     addFile("primaryFile", docQuery, true);
/*      */ 
/*  529 */     boolean isCreateDocID = true;
/*  530 */     if (createDocData("alternateFile", "0", isCreateDocID))
/*      */     {
/*  532 */       addFile("alternateFile", docQuery, false);
/*      */     }
/*      */ 
/*  535 */     addRenditions();
/*      */ 
/*  537 */     if (PluginFilters.filter("postAddFiles", this.m_workspace, this.m_binder, this.m_service) != -1) {
/*      */       return;
/*      */     }
/*  540 */     return;
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void addReleasedWebFileDirect()
/*      */     throws DataException, ServiceException
/*      */   {
/*  547 */     String path = this.m_binder.getLocal("primaryFile:path");
/*  548 */     if ((path != null) && (path.length() != 0))
/*      */     {
/*  550 */       this.m_binder.putLocal("webViewableFile:path", path);
/*  551 */       String ext = this.m_binder.getLocal("dExtension");
/*  552 */       this.m_binder.putLocal("dWebExtension", ext);
/*  553 */       this.m_binder.putLocal("dStatus", "RELEASED");
/*  554 */       this.m_binder.putLocal("dReleaseState", "Y");
/*  555 */       this.m_binder.putLocal("doRenameWhenPossible", "FALSE");
/*  556 */       addIncrementID(null, 2, "dDocID");
/*  557 */       addFile("webViewableFile", "IwebDocument", true);
/*  558 */       this.m_binder.putLocal("dProcessingState", "Y");
/*  559 */       this.m_workspace.execute("UrevisionExtension", this.m_binder);
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/*  564 */       int filterReturn = PluginFilters.filter("addReleasedWebFileDirect", this.m_workspace, this.m_binder, this.m_service);
/*  565 */       if (filterReturn == 1)
/*      */       {
/*  567 */         return;
/*      */       }
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  572 */       String msg = LocaleUtils.encodeMessage("csFilterError", null, "addReleasedWebFileDirect");
/*  573 */       Report.error(null, msg, e);
/*      */     }
/*      */ 
/*  576 */     finishDescriptorStateUpdate();
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void makeNewRevClass() throws ServiceException, DataException
/*      */   {
/*      */     try
/*      */     {
/*  584 */       int filterReturn = PluginFilters.filter("makeNewRevClass", this.m_workspace, this.m_binder, this.m_service);
/*  585 */       if (filterReturn == 1)
/*      */       {
/*  587 */         return;
/*      */       }
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  592 */       String msg = LocaleUtils.encodeMessage("csFilterError", null, "makeNewRevClass");
/*  593 */       Report.error(null, msg, e);
/*      */     }
/*      */ 
/*  597 */     this.m_checkUnique = false;
/*  598 */     if (!isUniqueDocName())
/*      */     {
/*  600 */       this.m_service.createServiceException(null, "!csCheckinIDNotUnique");
/*      */     }
/*      */ 
/*  604 */     this.m_binder.putLocal("dRevisionID", "1");
/*  605 */     if (this.m_binder.getLocal("dRevLabel") == null)
/*      */     {
/*  607 */       this.m_binder.putLocal("dRevLabel", RevisionSpec.getFirst());
/*      */     }
/*      */ 
/*  610 */     String tempRevClass = this.m_binder.getLocal("dRevClassID");
/*  611 */     if ((tempRevClass == null) || (tempRevClass.trim().length() <= 0) || (!StringUtils.convertToBool(this.m_binder.getLocal("UseRevClassFromImport"), false)))
/*      */     {
/*  614 */       addIncrementID(null, 0, "dRevClassID");
/*      */     }
/*      */ 
/*  618 */     String revClass = this.m_binder.getLocal("dRevClassID");
/*  619 */     if (revClass != null)
/*      */     {
/*  621 */       long id = Long.parseLong(revClass);
/*  622 */       if (id > 200L)
/*      */       {
/*  624 */         Service.checkFeatureAllowed("UnlimitedDocuments");
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  629 */     checkOrGenerateCreateDate();
/*      */ 
/*  632 */     checkOrGenerateDocName(false);
/*      */ 
/*  635 */     addRevClassesEntry();
/*      */ 
/*  640 */     this.m_binder.putLocal("isNew", "1");
/*      */ 
/*  643 */     this.m_isNewRevClass = true;
/*      */ 
/*  646 */     clearStatusParameters();
/*      */   }
/*      */ 
/*      */   public void updateUserForRevClasses(String[] authors) throws ServiceException
/*      */   {
/*  651 */     updateForRevClasses(authors, "dUser");
/*      */   }
/*      */ 
/*      */   public void updateForRevClasses(String[] newKeys, String defaultKey) throws ServiceException
/*      */   {
/*  656 */     String newKey = this.m_binder.getAllowMissing(defaultKey);
/*  657 */     if (newKey == null)
/*      */     {
/*  659 */       newKey = "";
/*      */     }
/*      */ 
/*  662 */     for (int i = 0; i < newKeys.length; ++i)
/*      */     {
/*  664 */       String value = this.m_binder.getAllowMissing(newKeys[i]);
/*  665 */       if (value != null)
/*      */         continue;
/*  667 */       this.m_binder.putLocal(newKeys[i], newKey);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void addRevClassesEntry()
/*      */     throws ServiceException, DataException
/*      */   {
/*  675 */     String[] keys = { "dDocOwner", "dDocLastModifier", "dDocCreator" };
/*  676 */     updateForRevClasses(keys, "dUser");
/*      */ 
/*  678 */     keys = new String[] { "dDocCreatedDate", "dDocLastModifiedDate" };
/*  679 */     updateForRevClasses(keys, "dCreateDate");
/*      */ 
/*  681 */     if ((this.m_service.isConditionVarTrue("SkipRevClassesEntry")) || (this.m_service.isConditionVarTrue("IsPreviewUpload"))) {
/*      */       return;
/*      */     }
/*  684 */     this.m_workspace.execute("IrevClasses", this.m_binder);
/*      */   }
/*      */ 
/*      */   public void checkAndValidateRevClassesRow()
/*      */     throws ServiceException, DataException
/*      */   {
/*  691 */     ResultSet rset = this.m_workspace.createResultSet("QrevClasses", this.m_binder);
/*  692 */     if (!rset.isEmpty())
/*      */       return;
/*  694 */     String[] authors = { "dDocOwner", "dDocLastModifier", "dDocCreator" };
/*  695 */     updateUserForRevClasses(authors);
/*      */ 
/*  697 */     String[] keys = { "dDocCreatedDate", "dDocLastModifiedDate" };
/*  698 */     updateForRevClasses(keys, "dCreateDate");
/*      */ 
/*  700 */     rset = this.m_workspace.createResultSet("QrevClassIDByName", this.m_binder);
/*  701 */     DataResultSet drset = new DataResultSet();
/*  702 */     drset.copy(rset);
/*  703 */     this.m_binder.pushActiveResultSet("RevClasses", drset);
/*      */ 
/*  706 */     this.m_workspace.execute("IrevClasses", this.m_binder);
/*  707 */     this.m_binder.popActiveResultSet();
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void updateDocInfo()
/*      */     throws ServiceException, DataException
/*      */   {
/*  714 */     checkUpdateDocInfo(true);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void updateDocInfoNoPathCheck() throws ServiceException, DataException
/*      */   {
/*  720 */     checkUpdateDocInfo(false);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void updateRevClassReleaseState()
/*      */     throws ServiceException, DataException
/*      */   {
/*  729 */     String query = this.m_currentAction.getParamAt(0);
/*  730 */     String curID = this.m_binder.getLocal("dID");
/*  731 */     String updateQuery1 = this.m_currentAction.getParamAt(1);
/*  732 */     String updateQuery2 = null;
/*  733 */     if (this.m_currentAction.getNumParams() > 2)
/*      */     {
/*  735 */       updateQuery2 = this.m_currentAction.getParamAt(2);
/*      */     }
/*  737 */     ResultSet rset = this.m_workspace.createResultSet(query, this.m_binder);
/*  738 */     DataResultSet drset = new DataResultSet();
/*  739 */     drset.copy(rset);
/*  740 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*      */     {
/*  742 */       String dID = ResultSetUtils.getValue(drset, "dID");
/*  743 */       this.m_binder.putLocal("dID", dID);
/*  744 */       boolean notified = checkUpdateDocInfoWithParams(false, drset, null, null);
/*  745 */       if (notified)
/*      */       {
/*  747 */         this.m_workspace.execute(updateQuery1, this.m_binder);
/*      */       }
/*  749 */       if (updateQuery2 == null)
/*      */         continue;
/*  751 */       this.m_workspace.execute(updateQuery2, this.m_binder);
/*      */     }
/*      */ 
/*  754 */     if (curID == null)
/*      */       return;
/*  756 */     this.m_binder.putLocal("dID", curID);
/*      */   }
/*      */ 
/*      */   public void checkUpdateDocInfo(boolean doMoveCheck)
/*      */     throws ServiceException, DataException
/*      */   {
/*  763 */     this.m_binder.putLocal("IsUpdate", "1");
/*      */ 
/*  766 */     String resultSetName = this.m_currentAction.getParamAt(0);
/*  767 */     ResultSet rset = this.m_binder.getResultSet(resultSetName);
/*  768 */     if (rset == null)
/*      */     {
/*  770 */       String msg = LocaleUtils.encodeMessage("csResultSetNotFound", null, resultSetName);
/*      */ 
/*  772 */       throw new DataException(msg);
/*      */     }
/*      */ 
/*  775 */     String docSetName = null;
/*  776 */     if (doMoveCheck)
/*      */     {
/*  778 */       docSetName = this.m_currentAction.getParamAt(1);
/*      */     }
/*  780 */     checkUpdateDocInfoWithParams(doMoveCheck, rset, resultSetName, docSetName);
/*      */   }
/*      */ 
/*      */   public boolean checkUpdateDocInfoWithParams(boolean doMoveCheck, ResultSet rset, String resultSetName, String docSetName)
/*      */     throws DataException, ServiceException
/*      */   {
/*  786 */     boolean mustNotify = false;
/*      */ 
/*  789 */     Object[] params = { new Boolean(doMoveCheck), rset, resultSetName, docSetName };
/*      */ 
/*  791 */     this.m_service.setReturnValue(null);
/*  792 */     this.m_service.setCachedObject("checkUpdateDocInfoParams", params);
/*  793 */     int retVal = PluginFilters.filter("checkUpdateDocInfo", this.m_workspace, this.m_binder, this.m_service);
/*  794 */     Object o = this.m_service.getReturnValue();
/*  795 */     if ((o != null) && (o instanceof Boolean))
/*      */     {
/*  797 */       mustNotify = ((Boolean)o).booleanValue();
/*      */     }
/*  799 */     if (retVal == -1)
/*      */     {
/*  801 */       return mustNotify;
/*      */     }
/*  803 */     doMoveCheck = ((Boolean)params[0]).booleanValue();
/*  804 */     rset = (ResultSet)params[1];
/*  805 */     resultSetName = (String)params[2];
/*  806 */     docSetName = (String)params[3];
/*      */ 
/*  812 */     String relState = ResultSetUtils.getValue(rset, "dReleaseState");
/*  813 */     this.m_binder.putLocal("dReleaseState", relState);
/*  814 */     this.m_binder.putLocal("prevReleaseState", relState);
/*      */ 
/*  816 */     if (!this.m_addReleasedWebFile)
/*      */     {
/*  818 */       if ((relState.equals("Y")) || (relState.equals("U")) || (relState.equals("I")))
/*      */       {
/*  822 */         String status = ResultSetUtils.getValue(rset, "dStatus");
/*  823 */         String indexerState = this.m_binder.getAllowMissing("dIndexerState");
/*  824 */         String indexedID = this.m_binder.getAllowMissing("dIndexedID");
/*  825 */         if ((((!relState.equals("Y")) || (indexerState == null) || (indexedID == null) || (indexerState.length() != 0) || (indexedID.length() != 0))) && ((
/*  828 */           (status.equals("DONE")) || (status.equals("RELEASED")))))
/*      */         {
/*  830 */           mustNotify = true;
/*      */         }
/*      */ 
/*      */       }
/*  834 */       else if (!relState.equals("E"))
/*      */       {
/*  836 */         this.m_binder.putLocal("dReleaseState", "N");
/*      */       }
/*      */     }
/*      */ 
/*  840 */     if (doMoveCheck)
/*      */     {
/*  842 */       doUpdateMoveCheck(resultSetName, docSetName, rset, relState, mustNotify);
/*      */     }
/*      */ 
/*  845 */     if (mustNotify)
/*      */     {
/*  848 */       String status = ResultSetUtils.getValue(rset, "dStatus");
/*  849 */       boolean doFullIndex = DataBinderUtils.getBoolean(this.m_binder, "doFullIndex", false);
/*  850 */       if ((doFullIndex) && (status != null) && (status.equals("RELEASED")))
/*      */       {
/*  852 */         this.m_binder.putLocal("dStatus", "DONE");
/*      */       }
/*      */ 
/*  856 */       this.m_binder.putLocal("dReleaseState", "U");
/*  857 */       notifyReleaseChange();
/*      */     }
/*      */ 
/*  861 */     params = new Object[] { new Boolean(doMoveCheck), rset, resultSetName, docSetName };
/*      */ 
/*  863 */     this.m_service.setReturnValue(null);
/*  864 */     this.m_service.setCachedObject("postCheckUpdateDocInfoParams", params);
/*  865 */     PluginFilters.filter("postCheckUpdateDocInfo", this.m_workspace, this.m_binder, this.m_service);
/*  866 */     o = this.m_service.getReturnValue();
/*  867 */     if ((o != null) && (o instanceof Boolean))
/*      */     {
/*  869 */       mustNotify = ((Boolean)o).booleanValue();
/*      */     }
/*  871 */     return mustNotify;
/*      */   }
/*      */ 
/*      */   public void doUpdateMoveCheck(String resultSetName, String docSetName, ResultSet rset, String relState, boolean mustNotify)
/*      */     throws ServiceException, DataException
/*      */   {
/*  877 */     Object[] moveCheckParams = { resultSetName, docSetName, rset, relState, new Boolean(mustNotify) };
/*  878 */     this.m_service.setCachedObject("doUpdateMoveCheckParams", moveCheckParams);
/*  879 */     if (PluginFilters.filter("doUpdateMoveCheck", this.m_workspace, this.m_binder, this.m_service) != 0)
/*      */     {
/*  881 */       return;
/*      */     }
/*  883 */     resultSetName = (String)moveCheckParams[0];
/*  884 */     docSetName = (String)moveCheckParams[1];
/*  885 */     rset = (ResultSet)moveCheckParams[2];
/*  886 */     relState = (String)moveCheckParams[3];
/*  887 */     mustNotify = ((Boolean)moveCheckParams[4]).booleanValue();
/*      */ 
/*  889 */     String idStr = this.m_binder.get("dID");
/*  890 */     String curRevLabel = ResultSetUtils.getValue(rset, "dRevLabel").toLowerCase();
/*  891 */     String newRevLabel = this.m_binder.getLocal("dRevLabel");
/*  892 */     newRevLabel = (newRevLabel != null) ? newRevLabel.toLowerCase() : null;
/*      */ 
/*  895 */     if ((newRevLabel != null) && (!newRevLabel.equals(curRevLabel)))
/*      */     {
/*  897 */       validateRevLabel(true, idStr);
/*      */     }
/*      */ 
/*  901 */     checkUpdateChange(resultSetName);
/*      */ 
/*  905 */     DataBinder curDocParams = new DataBinder(this.m_binder.getEnvironment());
/*  906 */     curDocParams.addResultSet(resultSetName, rset);
/*      */ 
/*  908 */     String status = ResultSetUtils.getValue(rset, "dStatus");
/*  909 */     String workflowState = ResultSetUtils.getValue(rset, "dWorkflowState");
/*  910 */     if (status.equalsIgnoreCase("EXPIRED"))
/*      */     {
/*  913 */       Date curOutDate = ResultSetUtils.getDateValue(rset, "dOutDate");
/*      */ 
/*  915 */       String newDateStr = this.m_binder.getLocal("dOutDate");
/*  916 */       Date newOutDate = null;
/*  917 */       if ((newDateStr != null) && (newDateStr.length() > 0))
/*      */       {
/*  919 */         newOutDate = LocaleResources.parseDate(newDateStr, this.m_service);
/*      */       }
/*  921 */       if ((newOutDate == null) || (curOutDate == null) || (newOutDate.after(curOutDate)))
/*      */       {
/*  925 */         this.m_binder.putLocal("dStatus", "DONE");
/*  926 */         this.m_binder.putLocal("dReleaseState", "N");
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  931 */     boolean isStillChangingFiles = false;
/*  932 */     if ((status.equalsIgnoreCase("GENWWW")) || ((workflowState.length() > 0) && ("E".indexOf(workflowState) >= 0)))
/*      */     {
/*  935 */       isStillChangingFiles = true;
/*      */     }
/*  937 */     moveFilesInStore(curDocParams, moveCheckParams, isStillChangingFiles, status);
/*      */   }
/*      */ 
/*      */   public void moveFilesInStore(DataBinder curDocParams, Object[] moveDocParams, boolean isStillChangingFiles, String status)
/*      */     throws DataException, ServiceException
/*      */   {
/*  944 */     moveFilesInStore(curDocParams, moveDocParams, isStillChangingFiles, status, renditions);
/*      */   }
/*      */ 
/*      */   public void moveFilesInStore(DataBinder curDocParams, Object[] moveDocParams, boolean isStillChangingFiles, String status, String[] listOfRenditions)
/*      */     throws DataException, ServiceException
/*      */   {
/*  956 */     ResultSet rset = (ResultSet)moveDocParams[2];
/*      */     try
/*      */     {
/*  959 */       String docSetName = (String)moveDocParams[1];
/*  960 */       DataResultSet docSet = (DataResultSet)this.m_binder.getResultSet(docSetName);
/*  961 */       if (docSet == null)
/*      */       {
/*  964 */         String msg = LocaleUtils.encodeMessage("csResultSetNotFound", null, docSetName);
/*  965 */         throw new DataException(msg);
/*      */       }
/*      */ 
/*  968 */       String[] dFields = { "dIsWebFormat", "dExtension", "dIsPrimary" };
/*  969 */       FieldInfo[] docInfos = ResultSetUtils.createInfoList(docSet, dFields, true);
/*      */ 
/*  972 */       String newWebExtension = this.m_binder.getLocal("dWebExtension");
/*  973 */       String newExtension = this.m_binder.getLocal("dExtension");
/*  974 */       String newAltExtension = this.m_binder.getLocal("altExtension");
/*  975 */       String newRevLabel = this.m_binder.getLocal("dRevLabel");
/*      */ 
/*  979 */       String curWebExtension = curDocParams.getAllowMissing("dWebExtension");
/*  980 */       String curRevLabel = curDocParams.getAllowMissing("dRevLabel");
/*      */ 
/*  984 */       IdcFileDescriptor[] descPacket = null;
/*  985 */       Map descMap = new HashMap();
/*  986 */       boolean isGenWWW = status.equals("GENWWW");
/*  987 */       for (docSet.first(); docSet.isRowPresent(); docSet.next())
/*      */       {
/*  989 */         String isWebFormatStr = docSet.getStringValue(docInfos[0].m_index);
/*  990 */         boolean isWebFormat = StringUtils.convertToBool(isWebFormatStr, false);
/*      */ 
/*  992 */         if ((isWebFormat) && (!isGenWWW))
/*      */         {
/*  995 */           descPacket = createDescriptorsForMoveStoreFile(curDocParams, this.m_binder, curWebExtension, newWebExtension, "webViewableFile");
/*      */ 
/*  997 */           List descList = moveRenditions(rset, curDocParams);
/*  998 */           descMap.put("webViewableFile", descPacket);
/*  999 */           descMap.put("renditions", descList);
/* 1000 */           if ((newRevLabel != null) && (!newRevLabel.equals(curRevLabel)) && (!status.equalsIgnoreCase("RELEASED")))
/*      */           {
/* 1006 */             descMap.put("forceMoveWebViewable", Boolean.valueOf(true));
/*      */           }
/*      */         } else {
/* 1009 */           if (isWebFormat)
/*      */             continue;
/* 1011 */           String newVaultExt = newExtension;
/* 1012 */           String isPrimaryStr = docSet.getStringValue(docInfos[2].m_index);
/* 1013 */           boolean isPrimary = StringUtils.convertToBool(isPrimaryStr, false);
/* 1014 */           String rendition = "primaryFile";
/* 1015 */           if (!isPrimary)
/*      */           {
/* 1017 */             rendition = "alternateFile";
/* 1018 */             newVaultExt = newAltExtension;
/*      */           }
/*      */ 
/* 1021 */           String extension = docSet.getStringValue(docInfos[1].m_index).toLowerCase();
/*      */ 
/* 1023 */           descPacket = createDescriptorsForMoveStoreFile(curDocParams, this.m_binder, extension, newVaultExt, rendition);
/*      */ 
/* 1025 */           descMap.put(rendition, descPacket);
/* 1026 */           boolean updateConversionSource = true;
/* 1027 */           if (isPrimary)
/*      */           {
/* 1029 */             updateConversionSource = descMap.get("conversionSource") == null;
/*      */           }
/* 1031 */           if (!updateConversionSource)
/*      */             continue;
/* 1033 */           descMap.put("conversionSource", descPacket);
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/* 1038 */       boolean movingFiles = false;
/* 1039 */       boolean movingVaultFiles = false;
/*      */ 
/* 1053 */       String[] containers = { "primaryFile", "webViewableFile" };
/* 1054 */       for (int i = 0; i < containers.length; ++i)
/*      */       {
/* 1056 */         String con = containers[i];
/* 1057 */         IdcFileDescriptor[] packet = (IdcFileDescriptor[])(IdcFileDescriptor[])descMap.get(con);
/* 1058 */         if (packet == null)
/*      */           continue;
/* 1060 */         Map args = new HashMap();
/* 1061 */         args.put("isContainer", "1");
/* 1062 */         boolean isChanged = !this.m_fileStore.compareDescriptors(packet[0], packet[1], args, this.m_service);
/*      */ 
/* 1064 */         Boolean forceMoveWebViewable = (Boolean)descMap.get("forceMoveWebViewable");
/* 1065 */         if ((forceMoveWebViewable != null) && (forceMoveWebViewable.booleanValue()) && (con.equals(containers[1])))
/*      */         {
/* 1067 */           isChanged = true;
/*      */         }
/* 1069 */         if (!isChanged)
/*      */           continue;
/* 1071 */         movingFiles = true;
/* 1072 */         if (i >= containers.length - 1)
/*      */           continue;
/* 1074 */         movingVaultFiles = true;
/* 1075 */         break;
/*      */       }
/*      */ 
/* 1085 */       if ((isStillChangingFiles) && (movingVaultFiles))
/*      */       {
/* 1087 */         boolean allowUpdate = (this.m_service.isConditionVarTrue("AllowUpdateForGenwww")) || (SharedObjects.getEnvValueAsBoolean("AllowUpdateForGenwww", false));
/*      */ 
/* 1089 */         if ((!allowUpdate) || (movingFiles))
/*      */         {
/* 1091 */           String msg = LocaleUtils.encodeMessage("csUnableToUpdateStateError", null, status);
/* 1092 */           this.m_service.createServiceException(null, msg);
/*      */         }
/*      */       }
/* 1095 */       if (movingFiles)
/*      */       {
/* 1097 */         moveDocumentFiles(descMap, movingVaultFiles, listOfRenditions);
/*      */       }
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/* 1102 */       throw new ServiceException(e);
/*      */     }
/*      */   }
/*      */ 
/*      */   public IdcFileDescriptor[] createDescriptorsForMoveStoreFile(DataBinder curDocParams, DataBinder newDocParams, String curExtension, String extension, String rendition)
/*      */     throws IOException, DataException, ServiceException
/*      */   {
/* 1110 */     curDocParams.putLocal("RenditionId", rendition);
/* 1111 */     newDocParams.putLocal("RenditionId", rendition);
/*      */ 
/* 1113 */     if (!rendition.startsWith("rendition:"))
/*      */     {
/* 1115 */       String extKey = "dExtension";
/* 1116 */       if (rendition.equals("webViewableFile"))
/*      */       {
/* 1118 */         extKey = "dWebExtension";
/*      */       }
/* 1120 */       curDocParams.putLocal(extKey, curExtension);
/* 1121 */       if (extension == null)
/*      */       {
/* 1123 */         newDocParams.putLocal(extKey, curExtension);
/*      */       }
/*      */       else
/*      */       {
/* 1127 */         newDocParams.putLocal(extKey, extension);
/*      */       }
/*      */     }
/*      */ 
/* 1131 */     IdcFileDescriptor[] desc = new IdcFileDescriptor[2];
/* 1132 */     desc[0] = this.m_fileStore.createDescriptor(curDocParams, null, this.m_service);
/*      */ 
/* 1134 */     Map args = new HashMap();
/* 1135 */     args.put("isMove", "1");
/* 1136 */     boolean isRetainMeta = DataBinderUtils.getBoolean(this.m_binder, "FSIsRetainMetadata", true);
/* 1137 */     if (isRetainMeta)
/*      */     {
/* 1140 */       args.put("isRetainMetadata", "1");
/*      */     }
/* 1142 */     desc[1] = this.m_fileStore.createDescriptor(newDocParams, args, this.m_service);
/* 1143 */     return desc;
/*      */   }
/*      */ 
/*      */   public void moveDocumentFiles(Map descMap, boolean movingVaultFiles)
/*      */     throws IOException, DataException, ServiceException
/*      */   {
/* 1149 */     moveDocumentFiles(descMap, movingVaultFiles, renditions);
/*      */   }
/*      */ 
/*      */   public void moveDocumentFiles(Map descMap, boolean movingVaultFiles, String[] listOfRenditions)
/*      */     throws IOException, DataException, ServiceException
/*      */   {
/* 1159 */     Object[] args = { listOfRenditions, descMap, new Boolean(movingVaultFiles), null, null };
/* 1160 */     this.m_service.setCachedObject("moveDocumentFiles:parameters", args);
/* 1161 */     if (PluginFilters.filter("moveDocumentFiles", this.m_workspace, this.m_binder, this.m_service) != 0)
/*      */     {
/* 1164 */       return;
/*      */     }
/* 1166 */     listOfRenditions = (String[])(String[])args[0];
/* 1167 */     descMap = (Map)args[1];
/* 1168 */     movingVaultFiles = ((Boolean)args[2]).booleanValue();
/*      */ 
/* 1170 */     int len = listOfRenditions.length;
/* 1171 */     int endIndex = (movingVaultFiles) ? len : len - 2;
/* 1172 */     for (int i = 0; i < endIndex; ++i)
/*      */     {
/* 1174 */       String key = listOfRenditions[i];
/* 1175 */       Object obj = descMap.get(key);
/* 1176 */       if (obj instanceof IdcFileDescriptor[])
/*      */       {
/* 1178 */         IdcFileDescriptor[] packet = (IdcFileDescriptor[])(IdcFileDescriptor[])obj;
/* 1179 */         moveDocumentFile(packet, key, args);
/*      */       } else {
/* 1181 */         if (!obj instanceof List)
/*      */           continue;
/* 1183 */         List list = (List)obj;
/* 1184 */         for (int j = 0; j < list.size(); ++j)
/*      */         {
/* 1186 */           IdcFileDescriptor[] packet = (IdcFileDescriptor[])(IdcFileDescriptor[])list.get(j);
/* 1187 */           moveDocumentFile(packet, key, args);
/*      */         }
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public void moveDocumentFile(IdcFileDescriptor[] packet, String renditionName, Object[] args)
/*      */     throws IOException, DataException, ServiceException
/*      */   {
/* 1198 */     int endIndex = 3;
/* 1199 */     int packetIndex = endIndex++;
/* 1200 */     int renditionIndex = endIndex++;
/* 1201 */     args[packetIndex] = packet;
/* 1202 */     args[renditionIndex] = renditionName;
/* 1203 */     if (PluginFilters.filter("moveDocumentFiles", this.m_workspace, this.m_binder, this.m_service) != 0)
/*      */     {
/* 1205 */       return;
/*      */     }
/* 1207 */     packet = (IdcFileDescriptor[])(IdcFileDescriptor[])args[packetIndex];
/* 1208 */     renditionName = (String)args[renditionIndex];
/*      */ 
/* 1210 */     boolean doneIt = false;
/* 1211 */     if (renditionName.equals("webViewableFile"))
/*      */     {
/* 1215 */       boolean doRecreateCheck = ((Boolean)args[2]).booleanValue();
/* 1216 */       if (doRecreateCheck)
/*      */       {
/* 1218 */         Map descMap = (Map)args[1];
/*      */ 
/* 1220 */         IdcFileDescriptor[] conversionSource = (IdcFileDescriptor[])(IdcFileDescriptor[])descMap.get("conversionSource");
/* 1221 */         if (conversionSource != null)
/*      */         {
/* 1223 */           doneIt = checkRecreateWebviewable(conversionSource, packet);
/*      */         }
/*      */       }
/*      */     }
/* 1227 */     if (doneIt)
/*      */       return;
/* 1229 */     this.m_fileStore.moveFile(packet[0], packet[1], null, this.m_service);
/*      */   }
/*      */ 
/*      */   public boolean checkRecreateWebviewable(IdcFileDescriptor[] conversionSource, IdcFileDescriptor[] webFile)
/*      */     throws IOException, DataException, ServiceException
/*      */   {
/* 1238 */     IdcFileDescriptor curWebFile = webFile[0];
/* 1239 */     IdcFileDescriptor newWebFile = webFile[1];
/* 1240 */     IdcFileDescriptor conversionSourceNew = conversionSource[1];
/* 1241 */     WebViewableConverterOutput converterOutput = new WebViewableConverterOutput();
/* 1242 */     converterOutput.init(this, this.m_service, false, false, true, null);
/* 1243 */     return converterOutput.checkRecreateWebviewableOnUpdate(conversionSourceNew, curWebFile, newWebFile);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void validateStandard()
/*      */     throws ServiceException, DataException
/*      */   {
/* 1251 */     int ret = PluginFilters.filter("validateStandard", this.m_workspace, this.m_binder, this.m_service);
/* 1252 */     if (ret == -1)
/*      */     {
/* 1254 */       return;
/*      */     }
/*      */ 
/* 1257 */     boolean isValidateOnly = this.m_service.isConditionVarTrue("IsValidateOnly");
/* 1258 */     boolean isSkipDocumentSpecificValidation = this.m_service.isConditionVarTrue("IsSkipDocumentSpecificValidation");
/*      */ 
/* 1266 */     boolean isForceCheckinValidation = DataBinderUtils.getBoolean(this.m_binder, "isForceCheckinValidation", false);
/* 1267 */     boolean skipUniqueDocNameValidation = DataBinderUtils.getBoolean(this.m_binder, "skipUniqueDocNameValidation", false);
/* 1268 */     if ((isValidateOnly) && (isForceCheckinValidation) && (!skipUniqueDocNameValidation) && 
/* 1270 */       (!isUniqueDocName()))
/*      */     {
/* 1272 */       String docName = this.m_binder.getLocal("dDocName");
/* 1273 */       String msg = LocaleUtils.encodeMessage("csCheckinIDNotUnique2", null, docName);
/* 1274 */       this.m_service.createServiceException(null, msg);
/*      */     }
/*      */ 
/* 1280 */     if ((!this.m_service.m_hasLockedContent) && (!isValidateOnly) && (!isSkipDocumentSpecificValidation) && (this.m_binder.getLocal(new StringBuilder().append("LockContents").append(this.m_service.m_requestImplementor.m_tranCounter).toString()) == null))
/*      */     {
/* 1283 */       if (this.m_binder.getAllowMissing("dDocName") != null)
/*      */       {
/* 1285 */         this.m_service.lockContent("dDocName");
/*      */       }
/* 1287 */       else if (this.m_binder.getAllowMissing("dRevClassID") != null)
/*      */       {
/* 1289 */         this.m_service.lockContent("dRevClassID");
/*      */       }
/*      */       else
/*      */       {
/* 1294 */         Report.trace(null, "Unable to auto place content lock. Neither dDocName nor dRevClassID exists", null);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1299 */     if (this.m_binder.getLocal("dReleaseState") == null)
/*      */     {
/* 1301 */       this.m_binder.putLocal("dReleaseState", "N");
/*      */     }
/* 1303 */     if (this.m_binder.getLocal("dWorkflowState") == null)
/*      */     {
/* 1305 */       this.m_binder.putLocal("dWorkflowState", "");
/*      */     }
/*      */ 
/* 1319 */     if (SecurityUtils.m_useCollaboration)
/*      */     {
/* 1321 */       CollaborationUtils.checkAndGetDerivedCollaborationName(this.m_binder);
/*      */     }
/*      */ 
/* 1324 */     if (this.m_isWf)
/*      */     {
/* 1327 */       this.m_wfImplementor.validateForWfStandard();
/*      */     }
/*      */ 
/* 1330 */     if (isSkipDocumentSpecificValidation)
/*      */     {
/* 1334 */       if (this.m_binder.getLocal("dDocName") == null)
/*      */       {
/* 1336 */         this.m_binder.putLocal("dDocName", "");
/*      */       }
/*      */ 
/* 1340 */       if (this.m_binder.getLocal("dDocAuthor") == null)
/*      */       {
/* 1342 */         UserData userData = this.m_service.getUserData();
/* 1343 */         if (userData != null)
/*      */         {
/* 1345 */           this.m_binder.putLocal("dDocAuthor", userData.m_name);
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */     else
/*      */     {
/* 1352 */       if (!this.m_isNewRevClass)
/*      */       {
/* 1354 */         checkOrGenerateDocName(isValidateOnly);
/*      */       }
/*      */ 
/* 1358 */       String title = this.m_binder.get("dDocTitle");
/* 1359 */       title = title.trim();
/*      */ 
/* 1361 */       if ((title == null) || (title.length() == 0))
/*      */       {
/* 1363 */         this.m_service.createServiceException(null, "!csCheckinTitleRequired");
/*      */       }
/*      */ 
/* 1366 */       doMetaFileValidation();
/*      */ 
/* 1369 */       PluginFilters.filter("postMetaFileValidation", this.m_workspace, this.m_binder, this.m_service);
/*      */ 
/* 1372 */       boolean isUpdate = DataBinderUtils.getLocalBoolean(this.m_binder, "IsUpdateMetaOnly", false);
/* 1373 */       boolean validatePrimary = DataBinderUtils.getBoolean(this.m_binder, "ValidatePrimaryFile", true);
/* 1374 */       if ((!isUpdate) && (validatePrimary))
/*      */       {
/* 1376 */         String pPath = getFilePath("primaryFile");
/* 1377 */         if ((pPath == null) || (pPath.trim().length() == 0))
/*      */         {
/* 1379 */           this.m_service.createServiceException(null, "!csCheckinPrimaryFileRequired");
/*      */         }
/*      */ 
/* 1382 */         File pFile = new File(pPath);
/* 1383 */         boolean validatePrimaryNonEmpty = DataBinderUtils.getBoolean(this.m_binder, "ValidatePrimaryFileNotEmpty", false);
/*      */ 
/* 1385 */         boolean exists = pFile.exists();
/* 1386 */         if ((!exists) || ((validatePrimaryNonEmpty) && (pFile.length() == 0L)))
/*      */         {
/* 1388 */           this.m_service.createServiceException(null, (exists) ? "!csCheckinPrimaryFileIsEmpty" : "!csCheckinPrimaryFileMissing");
/*      */         }
/* 1391 */         else if (pFile.isDirectory())
/*      */         {
/* 1393 */           this.m_service.createServiceException(null, "!csCheckinPrimaryFileIsDir");
/*      */         }
/*      */ 
/* 1397 */         String aPath = getFilePath("alternateFile");
/* 1398 */         if (aPath != null)
/*      */         {
/* 1400 */           aPath = aPath.trim();
/* 1401 */           if (aPath.length() != 0)
/*      */           {
/* 1403 */             String aExtension = FileUtils.getExtension(aPath);
/* 1404 */             String pExtension = FileUtils.getExtension(pPath);
/*      */ 
/* 1406 */             boolean isFileExtensionsEqualOverride = StringUtils.convertToBool(this.m_binder.m_environment.getProperty("IsFileExtensionsEqualOverride"), false);
/*      */ 
/* 1409 */             if ((aExtension.equalsIgnoreCase(pExtension)) || (isFileExtensionsEqualOverride))
/*      */             {
/* 1411 */               if (!SharedObjects.getEnvValueAsBoolean("AllowSamePrimaryAlternateExtensions", false))
/*      */               {
/* 1413 */                 this.m_service.createServiceException(null, "!csCheckinPrimaryAlternateExtensions");
/*      */               }
/*      */ 
/* 1416 */               this.m_changePrimaryFileExtension = true;
/*      */             }
/*      */           }
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1425 */     checkOrGenerateCreateDate();
/* 1426 */     String createDate = this.m_binder.getLocal("dCreateDate");
/* 1427 */     Date dte = null;
/*      */     try
/*      */     {
/* 1430 */       dte = this.m_binder.parseDate("dCreateDate", createDate);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 1434 */       this.m_service.createServiceException(null, "!csCheckinCreateDateInvalid");
/*      */     }
/*      */ 
/* 1437 */     String dateStr = null;
/*      */     try
/*      */     {
/* 1440 */       dateStr = this.m_binder.getAllowMissing("dInDate");
/* 1441 */       if (dateStr != null)
/*      */       {
/* 1443 */         dateStr = dateStr.trim();
/* 1444 */         if (dateStr.length() > 0)
/*      */         {
/* 1446 */           this.m_checkInDate = this.m_binder.parseDate("dInDate", dateStr);
/*      */         }
/*      */       }
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/* 1452 */       String msg = LocaleUtils.encodeMessage("csCheckinInDateInvalid", e.getMessage());
/*      */ 
/* 1454 */       this.m_service.createServiceException(null, msg);
/*      */     }
/*      */ 
/* 1457 */     if ((dateStr == null) || (dateStr.length() == 0))
/*      */     {
/* 1459 */       this.m_binder.putLocal("dInDate", createDate);
/* 1460 */       this.m_checkInDate = this.m_binder.parseDate("dInDate", createDate);
/*      */     }
/*      */ 
/* 1464 */     dateStr = this.m_binder.getAllowMissing("dOutDate");
/* 1465 */     boolean hasExpireDate = false;
/*      */     try
/*      */     {
/* 1468 */       if ((dateStr != null) && (dateStr.trim().length() > 0))
/*      */       {
/* 1470 */         dte = this.m_binder.parseDate("dOutDate", dateStr);
/* 1471 */         hasExpireDate = true;
/*      */       }
/*      */       else
/*      */       {
/* 1475 */         this.m_binder.putLocal("dOutDate", "");
/*      */       }
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/* 1480 */       String msg = LocaleUtils.encodeMessage("csCheckinOutDateInvalid", e.getMessage());
/*      */ 
/* 1482 */       this.m_service.createServiceException(null, msg);
/*      */     }
/*      */ 
/* 1486 */     if ((hasExpireDate) && (this.m_checkInDate.getTime() >= dte.getTime()))
/*      */     {
/* 1488 */       String msg = LocaleUtils.encodeMessage("csCheckinExpirationDateError", null, dte, this.m_checkInDate);
/*      */ 
/* 1490 */       this.m_service.createServiceException(null, msg);
/*      */     }
/*      */ 
/* 1493 */     validateAndFormatNumericFields();
/*      */   }
/*      */ 
/*      */   public void validateAndFormatNumericFields()
/*      */     throws ServiceException
/*      */   {
/* 1503 */     boolean disableFormatting = SharedObjects.getEnvValueAsBoolean("DisableNumericFieldFormat", false);
/*      */ 
/* 1505 */     if (disableFormatting == true)
/*      */     {
/* 1507 */       return;
/*      */     }
/*      */ 
/* 1512 */     Map numericFields = DataBinderUtils.getFieldsOfType(this.m_binder, "int,decimal");
/*      */ 
/* 1514 */     Iterator numFieldsIterator = numericFields.keySet().iterator();
/*      */ 
/* 1516 */     while (numFieldsIterator.hasNext())
/*      */     {
/* 1518 */       String numFieldName = (String)numFieldsIterator.next();
/* 1519 */       String numFieldValue = this.m_binder.getLocal(numFieldName);
/*      */ 
/* 1521 */       if ((numFieldValue != null) && (numFieldValue.length() > 0))
/*      */       {
/*      */         try
/*      */         {
/* 1525 */           Locale userLocale = LocaleUtils.constructJavaLocaleFromContext(this.m_service);
/* 1526 */           String formattedNumValue = NumberUtils.formatNumber(numFieldValue, userLocale, Locale.US);
/* 1527 */           this.m_binder.putLocal(numFieldName, formattedNumValue);
/*      */         }
/*      */         catch (Exception e)
/*      */         {
/* 1531 */           String errorMsg = LocaleUtils.encodeMessage("csNumberInvalidCharacters", null, numFieldValue);
/* 1532 */           throw new ServiceException(e, errorMsg, new Object[0]);
/*      */         }
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void validateCheckinData()
/*      */     throws DataException, ServiceException
/*      */   {
/* 1542 */     boolean isSkipDocumentSpecificValidation = this.m_service.isConditionVarTrue("IsSkipDocumentSpecificValidation");
/*      */ 
/* 1549 */     if (this.m_wfImplementor == null)
/*      */     {
/* 1551 */       this.m_binder.putLocal("dWorkflowState", "");
/*      */     }
/*      */ 
/* 1554 */     int ret = PluginFilters.filter("validateCheckinData", this.m_workspace, this.m_binder, this.m_service);
/* 1555 */     if (ret == -1)
/*      */     {
/* 1557 */       return;
/*      */     }
/*      */ 
/* 1560 */     boolean useProfile = SharedObjects.getEnvValueAsBoolean("UseProfileForCheckin", true);
/* 1561 */     if (useProfile)
/*      */     {
/* 1564 */       DocProfileManager.loadDocumentProfile(this.m_binder, this.m_service, true);
/*      */     }
/*      */ 
/* 1567 */     boolean isUpdate = DataBinderUtils.getBoolean(this.m_binder, "isUpdate", false);
/*      */ 
/* 1569 */     String account = this.m_binder.getLocal("dDocAccount");
/* 1570 */     if (!isSkipDocumentSpecificValidation)
/*      */     {
/* 1572 */       String revLabel = null;
/* 1573 */       if (isUpdate)
/*      */       {
/* 1575 */         revLabel = this.m_binder.getAllowMissing("dRevLabel");
/* 1576 */         account = this.m_binder.getAllowMissing("dDocAccount");
/*      */ 
/* 1580 */         this.m_binder.putLocal("dRevLabel", revLabel);
/*      */       }
/*      */       else
/*      */       {
/* 1584 */         revLabel = this.m_binder.getLocal("dRevLabel");
/*      */       }
/*      */ 
/* 1587 */       if (!RevisionSpec.isValid(revLabel))
/*      */       {
/* 1589 */         String msg = LocaleUtils.encodeMessage("csCheckinRevLabelInvalid", null, revLabel);
/*      */ 
/* 1591 */         this.m_service.createServiceException(null, msg);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1596 */     if ((account == null) || ((!SecurityUtils.m_useAccounts) && (!SecurityUtils.m_useCollaboration)))
/*      */     {
/* 1598 */       account = "";
/*      */     }
/*      */ 
/* 1601 */     int alen = account.length();
/* 1602 */     if (alen > 0)
/*      */     {
/* 1604 */       boolean isError = true;
/* 1605 */       String msg = null;
/* 1606 */       if (account.charAt(alen - 1) == '/')
/*      */       {
/* 1608 */         msg = "csCheckinAccountTrailingSlash";
/*      */       }
/*      */       else
/*      */       {
/* 1614 */         switch (Validation.checkUrlFilePathPart(account))
/*      */         {
/*      */         case 0:
/* 1617 */           isError = false;
/* 1618 */           break;
/*      */         case -2:
/* 1620 */           msg = "csCheckinAccountHasSpaces";
/* 1621 */           break;
/*      */         default:
/* 1623 */           msg = "csCheckinAccountHasInvalidChars";
/*      */         }
/*      */       }
/*      */ 
/* 1627 */       if (isError)
/*      */       {
/* 1629 */         msg = LocaleUtils.encodeMessage(msg, null, account);
/* 1630 */         this.m_service.createServiceException(null, msg);
/*      */       }
/*      */ 
/* 1640 */       if (SecurityUtils.m_useCollaboration)
/*      */       {
/* 1642 */         String clbraName = CollaborationUtils.checkAndGetDerivedCollaborationName(this.m_binder);
/* 1643 */         if (clbraName.length() > 0)
/*      */         {
/* 1646 */           boolean isValid = Collaborations.isCollaboration(clbraName);
/* 1647 */           if (!isValid)
/*      */           {
/* 1649 */             String errMsg = LocaleUtils.encodeMessage("csClbraMissingProject", null, clbraName);
/*      */ 
/* 1651 */             this.m_service.createServiceException(null, errMsg);
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/* 1656 */     this.m_binder.putLocal("dDocAccount", account);
/*      */ 
/* 1659 */     boolean isPublish = this.m_service.isConditionVarTrue("IsPublish");
/* 1660 */     String publishState = this.m_binder.getLocal("dPublishState");
/* 1661 */     if ((publishState == null) || (publishState.length() == 0))
/*      */     {
/* 1663 */       if (isPublish)
/*      */       {
/* 1665 */         this.m_service.createServiceException(null, "!csCheckinPublishContentStateError");
/*      */       }
/* 1667 */       this.m_binder.putLocal("dPublishState", "");
/*      */     }
/* 1671 */     else if (!isPublish)
/*      */     {
/* 1673 */       this.m_service.createServiceException(null, "!csCheckinPublishOverrideAttempted");
/*      */     }
/*      */ 
/* 1677 */     String publishType = this.m_binder.getLocal("dPublishType");
/* 1678 */     if (publishType == null)
/*      */     {
/* 1680 */       publishType = "";
/* 1681 */       this.m_binder.putLocal("dPublishType", "");
/*      */     }
/*      */ 
/* 1685 */     boolean isHtmlForm = this.m_service.isConditionVarTrue("IsCreateHtmlForm");
/* 1686 */     if (isHtmlForm)
/*      */     {
/* 1690 */       this.m_service.doCode("buildHtmlFormForCheckin");
/*      */     }
/*      */ 
/* 1693 */     boolean isUpdateMetaOnly = StringUtils.convertToBool(this.m_binder.getLocal("IsUpdateMetaOnly"), false);
/* 1694 */     if ((!isUpdateMetaOnly) && (!isSkipDocumentSpecificValidation))
/*      */     {
/* 1700 */       createDocData("primaryFile", "1", (!isUpdate) || (!this.m_isWf) || (publishType.length() > 0));
/* 1701 */       this.m_binder.putLocal("dIsWebFormat", "0");
/*      */     }
/*      */ 
/* 1704 */     validateDBFieldLengths();
/*      */ 
/* 1706 */     boolean isSkipSetMetaDefaults = DataBinderUtils.getBoolean(this.m_binder, "IsSkipSetMetaDefaults", false);
/* 1707 */     if (!isSkipSetMetaDefaults)
/*      */     {
/* 1710 */       this.m_service.doCode("setMetaDefaults");
/*      */     }
/*      */ 
/* 1714 */     validateMetaData();
/*      */ 
/* 1717 */     PluginFilters.filter("postValidateCheckinData", this.m_workspace, this.m_binder, this.m_service);
/*      */ 
/* 1723 */     if (this.m_currentAction.getNumParams() > 1)
/*      */     {
/* 1725 */       String revAction = this.m_currentAction.getParamAt(1);
/* 1726 */       updateExtendedAttributes(revAction);
/*      */ 
/* 1730 */       this.m_workspace.checkQuery(this.m_currentAction.getParamAt(0), this.m_binder);
/*      */     }
/*      */ 
/* 1734 */     if (this.m_upperCaseColumns != null)
/*      */     {
/* 1736 */       for (String col : this.m_upperCaseColumns.keySet())
/*      */       {
/* 1738 */         String value = this.m_binder.getLocal(col);
/* 1739 */         if (value != null)
/*      */         {
/* 1741 */           value = value.toUpperCase();
/* 1742 */           this.m_binder.putLocal(col, value);
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1749 */     String validateAcceptFieldList = SharedObjects.getEnvironmentValue("ValidateDocInfoAcceptFieldsList");
/*      */ 
/* 1751 */     if (validateAcceptFieldList != null)
/*      */     {
/* 1753 */       this.m_binder.putLocal("ValidateDocInfoAcceptFieldsList", validateAcceptFieldList);
/*      */     }
/*      */ 
/* 1756 */     String validateDenyFieldList = SharedObjects.getEnvironmentValue("ValidateDocInfoDenyFieldsList");
/*      */ 
/* 1758 */     if (validateDenyFieldList != null)
/*      */     {
/* 1760 */       this.m_binder.putLocal("ValidateDocInfoDenyFieldsList", validateDenyFieldList);
/*      */     }
/*      */ 
/* 1763 */     String httpUserAgent = this.m_binder.getEnvironmentValue("HTTP_USER_AGENT");
/* 1764 */     if ((!SharedObjects.getEnvValueAsBoolean("DISAssignInfoFormPatchBUG10256554", true)) || (this.m_binder.getLocal("IdcService") == null) || (!this.m_binder.getLocal("IdcService").equals("VALIDATE_DOCINFO")) || (httpUserAgent == null) || (!httpUserAgent.equals("Intradoc Client"))) {
/*      */       return;
/*      */     }
/*      */ 
/* 1768 */     this.m_binder.removeLocal("dpEvent");
/* 1769 */     this.m_binder.removeLocal("isDocProfileDone");
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void validateDBFieldLengths()
/*      */     throws DataException, ServiceException
/*      */   {
/* 1776 */     ViewFields docFields = new ViewFields(this.m_service);
/* 1777 */     docFields.addStandardDocFields();
/*      */ 
/* 1779 */     Vector fieldDefs = docFields.m_viewFields;
/* 1780 */     int numFields = fieldDefs.size();
/* 1781 */     for (int i = 0; i < numFields; ++i)
/*      */     {
/* 1783 */       FieldDef def = (FieldDef)fieldDefs.elementAt(i);
/* 1784 */       String name = def.m_name;
/* 1785 */       String val = null;
/*      */       try
/*      */       {
/* 1788 */         val = this.m_binder.get(def.m_name);
/*      */       }
/*      */       catch (DataException e)
/*      */       {
/* 1793 */         boolean isValidateOnly = this.m_service.isConditionVarTrue("IsValidateOnly");
/* 1794 */         boolean isAuto = StringUtils.convertToBool(this.m_binder.getAllowMissing("IsAutoNumber"), false);
/*      */ 
/* 1796 */         if ((isValidateOnly) && (isAuto) && (name.equalsIgnoreCase("dDocName")))
/*      */         {
/* 1798 */           val = "";
/*      */         }
/*      */         else
/*      */         {
/* 1802 */           throw new ServiceException(e, "csRequiredFieldMissing", new Object[] { def.m_caption });
/*      */         }
/*      */       }
/*      */ 
/* 1806 */       String envKey = new StringBuilder().append(name).append(":maxLength").toString();
/* 1807 */       int maxLength = SharedObjects.getEnvironmentInt(envKey, 30);
/* 1808 */       if (val.length() <= maxLength)
/*      */         continue;
/* 1810 */       String msg = LocaleUtils.encodeMessage("csCheckinFieldTooLong", null, def.m_caption, new StringBuilder().append("").append(maxLength).toString());
/*      */ 
/* 1812 */       this.m_service.createServiceException(null, msg);
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void validateMetaData()
/*      */     throws DataException, ServiceException
/*      */   {
/* 1821 */     boolean allowEmptyCheckinFields = DataBinderUtils.getBoolean(this.m_binder, "allowEmptyCheckinFields", false);
/* 1822 */     MetaFieldData metaData = (MetaFieldData)SharedObjects.getTable("DocMetaDefinition");
/* 1823 */     if (metaData == null)
/*      */     {
/* 1825 */       this.m_service.createServiceException(null, "!csCheckinCustomDefsNotLoaded");
/*      */     }
/*      */     try
/*      */     {
/* 1829 */       metaData.validate(this.m_binder, this.m_workspace, this.m_service);
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/* 1833 */       this.m_service.createServiceException(e, null);
/*      */     }
/*      */ 
/* 1837 */     String[][] requiredOptFields = { { "dDocType", "docTypes", "1", "type" }, { "dSecurityGroup", "securityGroups", "1", "group" }, { "dDocAuthor", "docAuthors", "0", "user" } };
/*      */ 
/* 1844 */     if (UserUtils.hasExternalUsers())
/*      */     {
/* 1848 */       requiredOptFields[2][1] = null;
/*      */     }
/*      */ 
/* 1851 */     String value = null;
/* 1852 */     IdcMessage errMsg = null;
/* 1853 */     String errSegment = null;
/* 1854 */     boolean isFileSeg = false;
/* 1855 */     for (int i = 0; i < requiredOptFields.length; ++i)
/*      */     {
/* 1857 */       String key = requiredOptFields[i][0];
/* 1858 */       value = this.m_binder.get(key);
/* 1859 */       isFileSeg = StringUtils.convertToBool(requiredOptFields[i][2], false);
/* 1860 */       errSegment = new StringBuilder().append("csCheckinFieldError_").append(requiredOptFields[i][3]).toString();
/* 1861 */       int maxLength = SharedObjects.getEnvironmentInt(new StringBuilder().append(requiredOptFields[i][0]).append(":maxLength").toString(), 30);
/*      */ 
/* 1864 */       if ((((value == null) || (value.length() == 0))) && 
/* 1866 */         (allowEmptyCheckinFields))
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/* 1872 */       if (isFileSeg)
/*      */       {
/* 1874 */         errMsg = Validation.checkUrlFileSegmentForDB(value, errSegment, maxLength, null);
/*      */       }
/*      */       else
/*      */       {
/* 1878 */         errMsg = Validation.checkFormFieldForDB(value, errSegment, maxLength, null);
/*      */       }
/*      */ 
/* 1881 */       if ((errMsg == null) && 
/* 1883 */         (requiredOptFields[i][1] != null))
/*      */       {
/* 1885 */         String[] casedValue = new String[1];
/* 1886 */         if (!metaData.isValueInOptionListEx(requiredOptFields[i][1], value, casedValue))
/*      */         {
/* 1888 */           errMsg = IdcMessageFactory.lc("csCheckinFieldErrorMsg", new Object[] { errSegment, value });
/*      */         }
/*      */         else
/*      */         {
/* 1892 */           this.m_binder.putLocal(key, casedValue[0]);
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/* 1897 */       if (errMsg == null)
/*      */         continue;
/* 1899 */       this.m_service.createServiceException(null, LocaleUtils.encodeMessage(errMsg));
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void populateMissingDocumentValues()
/*      */     throws DataException, ServiceException
/*      */   {
/* 1909 */     String skipPopulateStr = this.m_binder.getAllowMissing("SkipPopulateMissingDocumentValues");
/* 1910 */     boolean skipPopulate = StringUtils.convertToBool(skipPopulateStr, false);
/* 1911 */     if (skipPopulate)
/*      */     {
/* 1913 */       return;
/*      */     }
/*      */ 
/* 1916 */     ResultSet rset = null;
/* 1917 */     if (this.m_currentAction.getNumParams() > 0)
/*      */     {
/* 1919 */       String rsetName = this.m_currentAction.getParamAt(0);
/* 1920 */       rset = this.m_binder.getResultSet(rsetName);
/*      */     }
/*      */     else
/*      */     {
/* 1924 */       Object o = this.m_service.getCachedObject("DocInfoForPopulate");
/* 1925 */       if ((o != null) && (o instanceof ResultSet))
/*      */       {
/* 1927 */         rset = (ResultSet)o;
/*      */       }
/*      */       else
/*      */       {
/* 1931 */         String dID = this.m_binder.getLocal("dID");
/* 1932 */         if (dID != null)
/*      */         {
/* 1934 */           rset = this.m_service.getWorkspace().createResultSet("QdocInfo", this.m_binder);
/*      */         }
/*      */ 
/* 1937 */         if (rset == null)
/*      */         {
/* 1939 */           String dDocName = this.m_binder.getLocal("dDocName");
/* 1940 */           if ((dDocName != null) && (dDocName.trim().length() > 0))
/*      */           {
/* 1942 */             rset = this.m_service.getWorkspace().createResultSet("QlatestDocInfoByName", this.m_binder);
/*      */           }
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1949 */     if ((rset != null) && (rset.first()))
/*      */     {
/* 1951 */       for (int i = 0; i < rset.getNumFields(); ++i)
/*      */       {
/* 1953 */         boolean doCopy = false;
/* 1954 */         String fieldName = rset.getFieldName(i);
/* 1955 */         if (fieldName.startsWith("x"))
/*      */         {
/* 1957 */           doCopy = true;
/*      */         }
/* 1959 */         else if (fieldName.startsWith("d"))
/*      */         {
/* 1961 */           for (int j = 0; j < STD_FIELDS_TO_REPOPULATE.length; ++j)
/*      */           {
/* 1963 */             if (!fieldName.equals(STD_FIELDS_TO_REPOPULATE[j]))
/*      */               continue;
/* 1965 */             doCopy = true;
/*      */           }
/*      */ 
/*      */         }
/*      */ 
/* 1970 */         if (!doCopy)
/*      */           continue;
/* 1972 */         String curValue = this.m_binder.getLocal(fieldName);
/* 1973 */         if (curValue != null)
/*      */           continue;
/* 1975 */         String fieldValue = rset.getStringValue(i);
/* 1976 */         this.m_binder.putLocal(fieldName, fieldValue);
/*      */       }
/*      */ 
/* 1981 */       if (this.m_service.getServiceData().m_name.equals("VALIDATE_DOCINFO"))
/*      */       {
/* 1983 */         DataResultSet drset = new DataResultSet();
/* 1984 */         drset.copy(rset);
/* 1985 */         this.m_service.setCachedObject("OLD_DOC_INFO", drset);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1990 */     PluginFilters.filter("postPopulateMissingDocumentValues", this.m_workspace, this.m_binder, this.m_service);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void conditionalPopulateMissingDocumentValues()
/*      */     throws DataException, ServiceException
/*      */   {
/* 2000 */     if (!StringUtils.convertToBool(this.m_binder.getAllowMissing("forcePopulateMissingDocumentValues"), false))
/*      */       return;
/* 2002 */     populateMissingDocumentValues();
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void prepareDocAuthor()
/*      */   {
/* 2009 */     String docAuthor = this.m_binder.getLocal("dDocAuthor");
/* 2010 */     if (docAuthor != null)
/*      */       return;
/* 2012 */     boolean isCheckin = DataBinderUtils.getLocalBoolean(this.m_binder, "isCheckin", false);
/* 2013 */     if (!isCheckin)
/*      */       return;
/* 2015 */     this.m_binder.putLocal("dDocAuthor", this.m_service.getUserData().m_name);
/*      */   }
/*      */ 
/*      */   public void updateExtendedAttributes(String revAction)
/*      */     throws DataException, ServiceException
/*      */   {
/* 2039 */     if (revAction == null)
/*      */     {
/* 2041 */       return;
/*      */     }
/* 2043 */     this.m_service.setCachedObject("attributes:revAction", revAction);
/* 2044 */     if (PluginFilters.filter("updateExtendedAttributes", this.m_workspace, this.m_binder, this.m_service) == -1)
/*      */     {
/* 2047 */       return;
/*      */     }
/*      */ 
/* 2052 */     boolean incrementRevRank = false;
/* 2053 */     boolean decrementRevRank = false;
/* 2054 */     boolean setRevRankNow = false;
/* 2055 */     if (revAction.startsWith("newRev"))
/*      */     {
/* 2058 */       this.m_binder.putLocal("dRevRank", "0");
/* 2059 */       incrementRevRank = true;
/*      */     }
/* 2061 */     else if ((revAction.startsWith("insertRev")) || ((revAction.startsWith("updateRev")) && (this.m_service.isConditionVarTrue("hasBeenDeleted"))))
/*      */     {
/* 2064 */       ResultSet rset = this.m_workspace.createResultSet("QrevisionsComputeRank", this.m_binder);
/* 2065 */       String revRank = "0";
/* 2066 */       if (!rset.isEmpty())
/*      */       {
/* 2068 */         revRank = ResultSetUtils.getValue(rset, "dRevRank");
/*      */       }
/* 2070 */       this.m_binder.putLocal("dRevRank", revRank);
/* 2071 */       if (revAction.startsWith("updateRev"))
/*      */       {
/* 2074 */         setRevRankNow = true;
/*      */       }
/* 2076 */       incrementRevRank = true;
/*      */     }
/* 2078 */     else if (revAction.startsWith("deleteRev"))
/*      */     {
/* 2080 */       decrementRevRank = true;
/*      */     }
/* 2082 */     if (setRevRankNow)
/*      */     {
/* 2084 */       this.m_workspace.execute("UrevisionRevRank", this.m_binder);
/*      */     }
/* 2086 */     if (incrementRevRank)
/*      */     {
/* 2088 */       boolean limitRevRank = SharedObjects.getEnvValueAsBoolean("RevisionRankHasMaximum", false);
/* 2089 */       int maxRevRank = -1;
/* 2090 */       if (limitRevRank)
/*      */       {
/* 2092 */         maxRevRank = SharedObjects.getEnvironmentInt("RevisionRankMaximum", 5);
/* 2093 */         this.m_binder.putLocal("maxRevRank", new StringBuilder().append("").append(maxRevRank).toString());
/*      */       }
/* 2095 */       String incrementRevRankQuery = (limitRevRank) ? "UrevisionClassIncrementRanksCapped" : "UrevisionClassIncrementRanks";
/*      */ 
/* 2097 */       this.m_workspace.execute(incrementRevRankQuery, this.m_binder);
/*      */     } else {
/* 2099 */       if (!decrementRevRank)
/*      */         return;
/* 2101 */       String decrementRevRankQuery = null;
/* 2102 */       if ((this.m_determinedMinimumRankDeleteRevID) && (this.m_minimumRankDeleteRevID != null))
/*      */       {
/* 2105 */         this.m_binder.putLocal("minRevRankDID", this.m_minimumRankDeleteRevID);
/* 2106 */         decrementRevRankQuery = "UrevisionClassDecrementRanksCapped";
/*      */       }
/*      */       else
/*      */       {
/* 2110 */         decrementRevRankQuery = "UrevisionClassDecrementRanks";
/*      */       }
/* 2112 */       this.m_workspace.execute(decrementRevRankQuery, this.m_binder);
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void validateForUpdate() throws ServiceException, DataException
/*      */   {
/* 2119 */     canUpdateDocCheckoutState();
/*      */ 
/* 2122 */     String curAuthor = this.m_binder.getLocal("dDocAuthor");
/* 2123 */     String prevAuthor = this.m_binder.getFromSets("dDocAuthor");
/* 2124 */     if (curAuthor.equals(prevAuthor))
/*      */     {
/* 2126 */       return;
/*      */     }
/*      */ 
/* 2130 */     checkChangeAuthor();
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void checkParametersAgainstResultSet() throws DataException, ServiceException
/*      */   {
/* 2136 */     String setName = this.m_currentAction.getParamAt(0);
/* 2137 */     ResultSet rset = this.m_binder.getResultSet(setName);
/*      */ 
/* 2139 */     int nactions = this.m_currentAction.getNumParams();
/* 2140 */     for (int i = 1; i < nactions - 1; i += 2)
/*      */     {
/* 2142 */       String key = this.m_currentAction.getParamAt(i);
/* 2143 */       String errMsg = this.m_currentAction.getParamAt(i + 1);
/* 2144 */       checkParameter(rset, key, errMsg);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void checkParameter(ResultSet rset, String key, String errMsg)
/*      */     throws DataException, ServiceException
/*      */   {
/* 2151 */     String curVal = ResultSetUtils.getValue(rset, key);
/* 2152 */     String paramVal = this.m_binder.getLocal(key);
/* 2153 */     if (paramVal != null)
/*      */     {
/* 2155 */       if (paramVal.equalsIgnoreCase(curVal))
/*      */         return;
/* 2157 */       if (errMsg.startsWith("!"))
/*      */       {
/* 2159 */         errMsg = errMsg.substring(1);
/*      */       }
/* 2161 */       String msg = LocaleUtils.encodeMessage(errMsg, null, paramVal, curVal);
/* 2162 */       this.m_service.createServiceException(null, msg);
/*      */     }
/*      */     else
/*      */     {
/* 2167 */       this.m_binder.putLocal(key, curVal);
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void prepareCheckinSecurity() throws DataException, ServiceException
/*      */   {
/* 2174 */     boolean isCollaboration = StringUtils.convertToBool(this.m_binder.getLocal("isCollaboration"), false);
/* 2175 */     if (isCollaboration)
/*      */     {
/* 2179 */       String clbraName = this.m_binder.getAllowMissing("dClbraName");
/* 2180 */       if ((clbraName == null) || (clbraName.trim().length() == 0))
/*      */       {
/* 2182 */         this.m_service.createServiceException(null, "!csClbraNotSpecified");
/*      */       }
/*      */ 
/* 2186 */       String account = new StringBuilder().append("prj/").append(clbraName.trim()).toString();
/* 2187 */       this.m_binder.putLocal("dDocAccount", account);
/*      */     }
/*      */     else
/*      */     {
/* 2191 */       String account = this.m_binder.getAllowMissing("dDocAccount");
/* 2192 */       if (account == null)
/*      */       {
/* 2194 */         account = "";
/*      */       }
/* 2196 */       this.m_binder.putLocal("dDocAccount", account);
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void checkDocRule()
/*      */     throws ServiceException, DataException
/*      */   {
/* 2204 */     int type = NumberUtils.parseInteger(this.m_currentAction.getParamAt(0), 1);
/*      */ 
/* 2206 */     switch (type)
/*      */     {
/*      */     case 1:
/* 2209 */       isLatestID();
/* 2210 */       checkForSingleCheckinUser("!csCheckinUserNotOwnerError");
/* 2211 */       break;
/*      */     case 2:
/* 2214 */       isLatestID();
/* 2215 */       canCheckout();
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void checkDocRules()
/*      */     throws ServiceException, DataException
/*      */   {
/* 2224 */     int num = this.m_currentAction.getNumParams();
/* 2225 */     for (int i = 0; i < num; ++i)
/*      */     {
/* 2227 */       String rule = this.m_currentAction.getParamAt(i);
/*      */ 
/* 2229 */       this.m_service.setCachedObject("CheckRule", rule);
/* 2230 */       int result = PluginFilters.filter("checkDocRules", this.m_workspace, this.m_binder, this.m_service);
/* 2231 */       if (result == 1) {
/*      */         continue;
/*      */       }
/*      */ 
/* 2235 */       if (result == -1)
/*      */       {
/* 2237 */         return;
/*      */       }
/*      */ 
/* 2240 */       if (rule.equals("update"))
/*      */       {
/* 2242 */         validateForUpdate();
/*      */       }
/* 2244 */       else if (rule.equals("updateForm"))
/*      */       {
/* 2246 */         checkForUpdate();
/*      */       }
/* 2248 */       else if (rule.equals("isLatestRev"))
/*      */       {
/* 2250 */         isLatestID();
/*      */       }
/* 2252 */       else if (rule.equals("checkout"))
/*      */       {
/* 2254 */         canCheckout();
/*      */       }
/* 2256 */       else if (rule.equals("undoCheckout"))
/*      */       {
/* 2264 */         this.m_service.setConditionVar("checkWebdavUndoCheckout", true);
/* 2265 */         canUndoCheckout();
/*      */       }
/* 2267 */       else if (rule.equals("singleCheckinUser"))
/*      */       {
/* 2269 */         isLatestID();
/* 2270 */         checkForSingleCheckinUser("!csCheckinUserNotOwnerError");
/*      */       }
/* 2272 */       else if (rule.equals("changeAuthor"))
/*      */       {
/* 2274 */         checkChangeAuthor();
/*      */       }
/* 2276 */       else if (rule.equals("changeCheckoutUser"))
/*      */       {
/* 2278 */         checkChangeCheckoutUser();
/*      */       }
/* 2280 */       else if (rule.equals("isNotPublished"))
/*      */       {
/* 2282 */         checkIsNotPublished();
/*      */       }
/* 2284 */       else if (rule.equals("newDoc"))
/*      */       {
/* 2288 */         SecurityImplementor securityImpl = this.m_service.getSecurityImplementor();
/* 2289 */         securityImpl.checkMetaChangeSecurity(this.m_service, this.m_service.getBinder(), null, true);
/*      */       }
/* 2291 */       else if (rule.equals("validateDoc"))
/*      */       {
/* 2295 */         boolean forcePopulateMissingDocumentValues = DataBinderUtils.getBoolean(this.m_binder, "forcePopulateMissingDocumentValues", false);
/* 2296 */         ResultSet rset = (ResultSet)this.m_service.getCachedObject("OLD_DOC_INFO");
/* 2297 */         SecurityImplementor securityImpl = this.m_service.getSecurityImplementor();
/* 2298 */         if ((rset != null) && (forcePopulateMissingDocumentValues))
/*      */         {
/* 2302 */           securityImpl.checkMetaChangeSecurity(this.m_service, this.m_service.getBinder(), rset, false);
/*      */         }
/*      */         else
/*      */         {
/* 2307 */           securityImpl.checkMetaChangeSecurity(this.m_service, this.m_service.getBinder(), null, true);
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/* 2312 */         Report.trace(null, "Unrecognized doc rule to check.", null);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void checkIsNotPublished() throws DataException, ServiceException
/*      */   {
/* 2320 */     String pubState = this.m_binder.get("dPublishState");
/* 2321 */     if (pubState.length() <= 0) {
/*      */       return;
/*      */     }
/* 2324 */     ResultSet rset = this.m_workspace.createResultSet("QprojectDocument", this.m_binder);
/* 2325 */     if (rset.isEmpty())
/*      */       return;
/* 2327 */     this.m_service.createServiceException(null, "!csCheckinItemPublished");
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void checkChangeCheckoutUser()
/*      */     throws DataException, ServiceException
/*      */   {
/* 2335 */     String checkoutUser = this.m_binder.getAllowMissing("dCheckoutUser");
/* 2336 */     if ((checkoutUser == null) || (checkoutUser.trim().length() == 0))
/*      */     {
/* 2338 */       checkoutUser = this.m_service.getUserData().m_name;
/*      */     }
/*      */ 
/* 2341 */     if (checkNonOwnershipAdmin(checkoutUser))
/*      */       return;
/* 2343 */     String msg = LocaleUtils.encodeMessage("csChangeOwnerPermissionDenied", null, checkoutUser);
/*      */ 
/* 2345 */     this.m_service.createServiceExceptionEx(null, msg, -18);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void checkChangeAuthor()
/*      */     throws DataException, ServiceException
/*      */   {
/* 2353 */     prepareDocAuthor();
/*      */ 
/* 2355 */     String docAuthor = this.m_binder.get("dDocAuthor");
/* 2356 */     if (checkNonOwnershipAdmin(docAuthor))
/*      */       return;
/* 2358 */     String msg = LocaleUtils.encodeMessage("csChangeOwnerPermissionDenied", null, docAuthor);
/*      */ 
/* 2360 */     this.m_service.createServiceExceptionEx(null, msg, -18);
/*      */   }
/*      */ 
/*      */   protected void checkForSingleCheckinUser(String errMsg)
/*      */     throws DataException, ServiceException
/*      */   {
/* 2366 */     boolean isAdmin = ((this.m_service.m_privilege & 0x8) != 0) || (!this.m_useSecurity);
/*      */ 
/* 2368 */     String defaultAuthor = this.m_binder.get("dUser");
/*      */ 
/* 2370 */     if (StringUtils.convertToBool(this.m_binder.getLocal("CurRevIsCheckedOut"), false))
/*      */     {
/* 2372 */       String checkoutUser = this.m_binder.getLocal("CurRevCheckoutUser");
/* 2373 */       if ((!defaultAuthor.equalsIgnoreCase(checkoutUser)) && (!isAdmin))
/*      */       {
/* 2375 */         this.m_service.createServiceException(null, errMsg);
/*      */       }
/* 2377 */       defaultAuthor = checkoutUser;
/*      */     }
/*      */ 
/* 2380 */     this.m_binder.putLocal("dDocAuthor", defaultAuthor);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void checkForUpdate()
/*      */     throws DataException, ServiceException
/*      */   {
/* 2390 */     canUpdateDocCheckoutState();
/*      */ 
/* 2393 */     boolean exclusiveCheckout = SharedObjects.getEnvValueAsBoolean("ExclusiveCheckout", false);
/*      */ 
/* 2395 */     boolean isCanChangeAuthor = true;
/* 2396 */     if (exclusiveCheckout)
/*      */     {
/* 2398 */       String group = this.m_binder.get("dSecurityGroup");
/* 2399 */       if (this.m_useSecurity == true)
/*      */       {
/* 2401 */         UserData userData = this.m_service.getUserData();
/* 2402 */         int priv = SecurityUtils.determineGroupPrivilege(userData, group);
/* 2403 */         if ((priv & 0x8) == 0)
/*      */         {
/* 2405 */           isCanChangeAuthor = false;
/*      */         }
/*      */       }
/*      */     }
/* 2409 */     this.m_service.setConditionVar("SingleUser", isCanChangeAuthor);
/*      */ 
/* 2412 */     String status = this.m_binder.get("dStatus");
/* 2413 */     String workflowState = this.m_binder.get("dWorkflowState");
/* 2414 */     boolean isCanEditReleaseDate = true;
/* 2415 */     if ((status.equals("RELEASED")) || (status.equals("EXPIRED")))
/*      */     {
/* 2417 */       isCanEditReleaseDate = false;
/*      */     }
/* 2419 */     else if ((workflowState.length() > 0) && ("WE".indexOf(workflowState) >= 0))
/*      */     {
/* 2421 */       this.m_service.setConditionVar("SingleGroup", true);
/*      */     }
/* 2423 */     this.m_service.setConditionVar("IsCanEditReleaseDate", isCanEditReleaseDate);
/*      */   }
/*      */ 
/*      */   protected void isLatestID()
/*      */     throws DataException, ServiceException
/*      */   {
/* 2429 */     String id = this.m_binder.getLocal("CurRevID");
/* 2430 */     String aID = this.m_binder.getLocal("latestID");
/*      */ 
/* 2432 */     if (id == null)
/*      */     {
/* 2434 */       this.m_service.createServiceException(null, "!csPreviousVersionReference");
/*      */     }
/*      */ 
/* 2437 */     if (!id.equals(aID))
/*      */     {
/* 2439 */       this.m_service.createServiceException(null, "!csRevIsNotLatest");
/*      */     }
/* 2441 */     this.m_binder.putLocal("IsNotLatestRev", "");
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void checkIsLatestRev()
/*      */     throws DataException, ServiceException
/*      */   {
/* 2448 */     String id = this.m_binder.get("dID");
/* 2449 */     this.m_binder.putLocal("dID", id);
/* 2450 */     String isNotLatestRevValue = "";
/*      */ 
/* 2453 */     String curID = this.m_binder.getAllowMissing("dCurRevID");
/* 2454 */     if (curID == null)
/*      */       return;
/* 2456 */     boolean isNotLatestRev = !curID.equals(id);
/* 2457 */     if (isNotLatestRev)
/*      */     {
/* 2459 */       isNotLatestRevValue = "1";
/*      */     }
/* 2461 */     if (this.m_currentAction.getNumParams() == 0)
/*      */     {
/* 2463 */       this.m_service.setConditionVar("IsNotSyncRev", isNotLatestRev);
/*      */     }
/* 2465 */     this.m_binder.putLocal("IsNotLatestRev", isNotLatestRevValue);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void canCheckout()
/*      */     throws DataException, ServiceException
/*      */   {
/* 2473 */     String user = this.m_binder.getAllowMissing("dCheckoutUser");
/* 2474 */     if ((user == null) || (user.trim().length() == 0))
/*      */     {
/* 2476 */       user = this.m_service.getUserData().m_name;
/*      */     }
/* 2478 */     if ((user == null) || ((user.equalsIgnoreCase("anonymous")) && (this.m_useSecurity == true)))
/*      */     {
/* 2480 */       this.m_service.createServiceException(null, "!csCheckoutUserValidationError");
/*      */     }
/*      */ 
/* 2484 */     String workflowState = this.m_binder.get("dWorkflowState");
/*      */ 
/* 2487 */     boolean isCheckedOut = DataBinderUtils.getBoolean(this.m_binder, "CurRevIsCheckedOut", false);
/* 2488 */     if (isCheckedOut)
/*      */     {
/* 2490 */       boolean isError = true;
/* 2491 */       String curCheckoutUser = this.m_binder.getLocal("CurRevCheckoutUser");
/* 2492 */       boolean isAllowCheckedOut = this.m_service.isConditionVarTrue("allowCheckout");
/* 2493 */       if ((isAllowCheckedOut) && 
/* 2495 */         (curCheckoutUser.equals(user)))
/*      */       {
/* 2497 */         isError = false;
/*      */       }
/*      */ 
/* 2500 */       if (isError)
/*      */       {
/* 2502 */         String msg = LocaleUtils.encodeMessage("csRevIsCheckedOut", null, curCheckoutUser);
/*      */ 
/* 2504 */         this.m_binder.putLocal("StatusReason", new StringBuilder().append("csRevIsCheckedOut:").append(curCheckoutUser).toString());
/* 2505 */         this.m_service.createServiceExceptionEx(null, msg, -22);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 2510 */     this.m_binder.putLocal("CurRevCheckoutUser", user);
/*      */ 
/* 2514 */     String wfInfo = this.m_currentAction.getParamAt(1);
/* 2515 */     ResultSet rset = this.m_binder.getResultSet(wfInfo);
/* 2516 */     boolean isWorkflow = false;
/* 2517 */     if ((rset != null) && (!rset.isEmpty()))
/*      */     {
/* 2519 */       String wfName = ResultSetUtils.getValue(rset, "dWfName");
/* 2520 */       String wfState = ResultSetUtils.getValue(rset, "dWfStatus");
/* 2521 */       if (!wfState.equalsIgnoreCase("INIT"))
/*      */       {
/* 2523 */         isWorkflow = true;
/*      */ 
/* 2525 */         String stepType = this.m_binder.get("dWfStepType");
/* 2526 */         stepType = WorkflowScriptUtils.getUpgradedStepType(stepType);
/* 2527 */         if (workflowState.equals("R"))
/*      */         {
/* 2529 */           this.m_service.createServiceException(null, "!csWorkflowMustBeEditable");
/*      */         }
/*      */ 
/* 2534 */         String wfType = ResultSetUtils.getValue(rset, "dWfType");
/* 2535 */         if ((((wfType.equalsIgnoreCase("basic")) || (WorkflowScriptUtils.isContributorStep(stepType)))) && 
/* 2538 */           (!WorkflowUtils.isUserInStep(this.m_workspace, this.m_binder, user, this.m_service)))
/*      */         {
/* 2541 */           String msg = LocaleUtils.encodeMessage("csWorkflowUnableToCheckout", null, wfName);
/*      */ 
/* 2543 */           this.m_service.createServiceException(null, msg);
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 2549 */     if (!isWorkflow)
/*      */     {
/* 2552 */       boolean exclusiveCheckout = SharedObjects.getEnvValueAsBoolean("ExclusiveCheckout", false);
/*      */ 
/* 2554 */       if (exclusiveCheckout)
/*      */       {
/* 2556 */         String docAuthor = this.m_binder.get("dDocAuthor");
/* 2557 */         if (!checkNonOwnershipAdmin(docAuthor))
/*      */         {
/* 2559 */           String msg = LocaleUtils.encodeMessage("csCheckoutNoAccess", null, docAuthor);
/*      */ 
/* 2561 */           this.m_service.createServiceExceptionEx(null, msg, -18);
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/* 2566 */     PluginFilters.filter("validateCheckout", this.m_workspace, this.m_binder, this.m_service);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void isDocCheckedOut() throws DataException, ServiceException
/*      */   {
/* 2572 */     boolean isState = true;
/* 2573 */     if (this.m_currentAction.getNumParams() > 0)
/*      */     {
/* 2575 */       isState = StringUtils.convertToBool(this.m_currentAction.getParamAt(0), true);
/*      */     }
/*      */ 
/* 2578 */     if (StringUtils.convertToBool(this.m_binder.get("dIsCheckedOut"), false) == isState)
/*      */       return;
/* 2580 */     String key = (isState) ? "csIsNotCheckedOutError" : "csIsCheckedOutError";
/* 2581 */     String msg = LocaleUtils.encodeMessage(key, null, this.m_binder.get("dCheckoutUser"));
/*      */ 
/* 2583 */     this.m_service.createServiceException(null, msg);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void canUpdateDocCheckoutState()
/*      */     throws DataException, ServiceException
/*      */   {
/* 2590 */     String checkoutUser = this.m_binder.get("dCheckoutUser");
/* 2591 */     if ((checkoutUser == null) || (checkoutUser.length() == 0))
/*      */     {
/* 2593 */       return;
/*      */     }
/*      */ 
/* 2596 */     UserData userData = this.m_service.getUserData();
/* 2597 */     String user = userData.m_name;
/*      */ 
/* 2599 */     if (checkoutUser.equalsIgnoreCase(user))
/*      */     {
/* 2601 */       return;
/*      */     }
/*      */ 
/* 2604 */     if (this.m_service.checkAccess(this.m_binder, 8))
/*      */     {
/* 2606 */       return;
/*      */     }
/*      */ 
/* 2609 */     String msg = LocaleUtils.encodeMessage("csUpdateNoAccess", null, checkoutUser);
/*      */ 
/* 2611 */     this.m_service.createServiceException(null, msg);
/*      */   }
/*      */ 
/*      */   public void canCheckInCheckedOutDoc(String docName)
/*      */     throws DataException, ServiceException
/*      */   {
/* 2617 */     checkCheckedOutState(docName, false, true);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void canUndoCheckout() throws DataException, ServiceException
/*      */   {
/* 2623 */     String docName = this.m_binder.getLocal("dDocName");
/*      */ 
/* 2625 */     checkCheckedOutState(docName, true, false);
/*      */   }
/*      */ 
/*      */   public void checkCheckedOutState(String docName, boolean isUndoCheckout, boolean isCheckin)
/*      */     throws DataException, ServiceException
/*      */   {
/* 2634 */     if (!this.m_useSecurity)
/*      */     {
/* 2636 */       return;
/*      */     }
/*      */ 
/* 2639 */     PluginFilters.filter("checkCheckedOutState", this.m_workspace, this.m_binder, this.m_service);
/*      */ 
/* 2641 */     boolean isForceCheckout = (this.m_service.isConditionVarTrue("isForceCheckout")) || (StringUtils.convertToBool(this.m_binder.getLocal("isForceCheckout"), false));
/*      */ 
/* 2644 */     String checkoutUser = this.m_binder.get("dCheckoutUser");
/* 2645 */     if ((checkoutUser == null) || (checkoutUser.length() == 0))
/*      */     {
/* 2648 */       if (isForceCheckout)
/*      */       {
/* 2652 */         String isFinishedStr = this.m_binder.getLocal("isFinished");
/* 2653 */         this.m_service.executeService("INTERNAL_CHECKOUT_SUB");
/* 2654 */         checkoutUser = this.m_binder.get("dCheckoutUser");
/* 2655 */         if (isFinishedStr != null)
/*      */         {
/* 2657 */           this.m_binder.putLocal("isFinished", isFinishedStr);
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/* 2662 */         this.m_binder.putLocal("StatusReason", new StringBuilder().append("csIsNotCheckedOutError:").append(docName).toString());
/* 2663 */         this.m_service.createServiceException(null, "!csIsNotCheckedOutError");
/*      */       }
/*      */     }
/*      */ 
/* 2667 */     UserData userData = this.m_service.getUserData();
/* 2668 */     String user = userData.m_name;
/*      */ 
/* 2670 */     boolean okAuthor = true;
/* 2671 */     String docAuthor = null;
/* 2672 */     if (isCheckin)
/*      */     {
/* 2674 */       docAuthor = this.m_binder.get("dDocAuthor");
/* 2675 */       okAuthor = docAuthor.equalsIgnoreCase(user);
/*      */     }
/* 2677 */     if ((okAuthor) && (checkoutUser.equalsIgnoreCase(user)))
/*      */     {
/* 2679 */       return;
/*      */     }
/*      */ 
/* 2682 */     if (this.m_service.checkAccess(this.m_binder, 8))
/*      */     {
/* 2684 */       return;
/*      */     }
/*      */ 
/* 2688 */     int errCode = -20;
/* 2689 */     String statusReason = null;
/*      */     String errMsg;
/* 2690 */     if (okAuthor)
/*      */     {
/* 2692 */       if (isUndoCheckout)
/*      */       {
/* 2694 */         String errMsg = LocaleUtils.encodeMessage("csCheckOutStateOkAuthorIsUndo", null, docName, checkoutUser);
/*      */ 
/* 2696 */         statusReason = "csCheckOutStateOkAuthorIsUndo:";
/*      */       }
/*      */       else
/*      */       {
/* 2700 */         String errMsg = LocaleUtils.encodeMessage("csCheckOutStateOkAuthorCheckIn", null, docName, checkoutUser);
/*      */ 
/* 2702 */         statusReason = "csCheckOutStateOkAuthorCheckIn:";
/*      */       }
/* 2704 */       statusReason = new StringBuilder().append(statusReason).append(docName).append(",").append(checkoutUser).toString();
/*      */     }
/*      */     else
/*      */     {
/* 2708 */       errMsg = LocaleUtils.encodeMessage("csCheckOutStateInsufficientPrivilege", null, docName, docAuthor);
/*      */ 
/* 2710 */       statusReason = new StringBuilder().append("csCheckOutStateInsufficientPrivilege:").append(docName).append(",").append(docAuthor).toString();
/*      */     }
/* 2712 */     this.m_binder.putLocal("StatusReason", statusReason);
/* 2713 */     this.m_service.createServiceExceptionEx(null, errMsg, errCode);
/*      */   }
/*      */ 
/*      */   public void checkUpdateChange(String rsName) throws DataException, ServiceException
/*      */   {
/* 2718 */     DataResultSet drset = (DataResultSet)this.m_binder.getResultSet(rsName);
/* 2719 */     if (drset != null)
/*      */     {
/* 2721 */       String oldGroup = ResultSetUtils.getValue(drset, "dSecurityGroup");
/* 2722 */       String oldAccount = ResultSetUtils.getValue(drset, "dDocAccount");
/* 2723 */       String oldAuthor = ResultSetUtils.getValue(drset, "dDocAuthor");
/* 2724 */       String newGroup = this.m_binder.get("dSecurityGroup");
/* 2725 */       String newAccount = this.m_binder.getAllowMissing("dDocAccount");
/* 2726 */       String newAuthor = this.m_binder.get("dDocAuthor");
/* 2727 */       boolean isChanged = false;
/* 2728 */       String errMsg = null;
/* 2729 */       boolean authorChanged = false;
/* 2730 */       if (newAccount == null)
/*      */       {
/* 2732 */         newAccount = "";
/*      */       }
/*      */ 
/* 2735 */       if ((!this.m_isWf) && (!newAuthor.equals(oldAuthor)))
/*      */       {
/* 2737 */         errMsg = LocaleUtils.encodeMessage("csChangeAuthorPermissionDenied", null);
/* 2738 */         authorChanged = true;
/* 2739 */         isChanged = true;
/*      */       }
/* 2741 */       if ((!isChanged) && (!newGroup.equalsIgnoreCase(oldGroup)))
/*      */       {
/* 2743 */         errMsg = LocaleUtils.encodeMessage("csChangeGroupPermissionDenied", null);
/* 2744 */         isChanged = true;
/*      */       }
/* 2746 */       if ((!isChanged) && (SecurityUtils.m_useAccounts) && (!newAccount.equalsIgnoreCase(oldAccount)))
/*      */       {
/* 2748 */         errMsg = LocaleUtils.encodeMessage("csChangeAccountPermissionDenied", null);
/* 2749 */         isChanged = true;
/*      */       }
/* 2751 */       if (isChanged)
/*      */       {
/* 2753 */         String docAuthor = ResultSetUtils.getValue(drset, "dDocAuthor");
/* 2754 */         String potentialNewAuthor = null;
/* 2755 */         this.m_binder.putLocal("SecurityProfileResultSet", rsName);
/* 2756 */         if (authorChanged)
/*      */         {
/* 2759 */           potentialNewAuthor = newAuthor;
/*      */         }
/*      */ 
/* 2764 */         if (!checkNonOwnershipAdminRevUpdate(docAuthor, potentialNewAuthor))
/*      */         {
/* 2766 */           this.m_service.createServiceExceptionEx(null, errMsg, -18);
/*      */         }
/* 2768 */         this.m_binder.removeLocal("SecurityProfileResultSet");
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 2773 */     SecurityImplementor securityImpl = this.m_service.getSecurityImplementor();
/* 2774 */     securityImpl.checkMetaChangeSecurity(this.m_service, this.m_service.getBinder(), drset, false);
/*      */ 
/* 2776 */     ServiceExtensionUtils.executeDocMetaUpdateSideEffect(drset, this.m_binder, this.m_service);
/*      */   }
/*      */ 
/*      */   public boolean checkNonOwnershipAdmin(String docAuthor)
/*      */     throws DataException, ServiceException
/*      */   {
/* 2782 */     return checkNonOwnershipAdminRevUpdate(docAuthor, null);
/*      */   }
/*      */ 
/*      */   public boolean checkNonOwnershipAdminRevUpdate(String docAuthor, String newUser)
/*      */     throws DataException, ServiceException
/*      */   {
/* 2788 */     if (this.m_useSecurity == true)
/*      */     {
/* 2790 */       if (newUser == null)
/*      */       {
/* 2792 */         return DocumentSecurityUtils.canUpdateOwnershipSecurity(docAuthor, null, this.m_binder, this.m_service);
/*      */       }
/*      */ 
/* 2796 */       return DocumentSecurityUtils.canUpdateOwnershipSecurity(newUser, docAuthor, this.m_binder, this.m_service);
/*      */     }
/*      */ 
/* 2800 */     return true;
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void deleteDoc()
/*      */     throws ServiceException, DataException
/*      */   {
/* 2807 */     updateExtendedAttributes("deleteDoc");
/* 2808 */     String setName = this.m_currentAction.getParamAt(0);
/* 2809 */     deleteQueryFiles(setName, null);
/*      */ 
/* 2815 */     DataResultSet drset = (DataResultSet)this.m_binder.getResultSet(setName);
/*      */ 
/* 2817 */     DataBinder binder = new DataBinder();
/* 2818 */     Hashtable alreadyDone = new Hashtable();
/* 2819 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*      */     {
/* 2821 */       String dID = ResultSetUtils.getValue(drset, "dID");
/* 2822 */       if (alreadyDone.get(dID) != null)
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/* 2828 */       String dReleaseState = ResultSetUtils.getValue(drset, "dReleaseState");
/*      */ 
/* 2830 */       if ((this.m_promotedID != null) && 
/* 2832 */         (dID.equals(this.m_promotedID)))
/*      */       {
/* 2836 */         dReleaseState = "U";
/*      */       }
/*      */ 
/* 2839 */       String newReleaseState = computeDeleteRevReleaseState(dReleaseState);
/* 2840 */       binder.putLocal("dID", dID);
/* 2841 */       binder.putLocal("newReleaseState", newReleaseState);
/* 2842 */       String dIndexedID = ResultSetUtils.getValue(drset, "dIndexedID");
/* 2843 */       binder.putLocal("dIndexedID", dIndexedID);
/* 2844 */       doDatabaseDelete(newReleaseState, dReleaseState, binder);
/*      */ 
/* 2847 */       Vector newParams = new IdcVector();
/* 2848 */       newParams.add("Delete Revision");
/* 2849 */       newParams.add("IdocHistory");
/* 2850 */       this.m_service.doCodeWithActionParameters("docHistoryInfo", newParams);
/*      */ 
/* 2852 */       alreadyDone.put(dID, "1");
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void deleteRev() throws ServiceException, DataException
/*      */   {
/* 2859 */     updateExtendedAttributes("deleteRev");
/* 2860 */     deleteRevFiles();
/*      */ 
/* 2862 */     String oldReleaseState = this.m_binder.getLocal("currentReleaseState");
/* 2863 */     String newReleaseState = this.m_binder.getLocal("newReleaseState");
/* 2864 */     doDatabaseDelete(newReleaseState, oldReleaseState, this.m_binder);
/*      */   }
/*      */ 
/*      */   protected void doDatabaseDelete(String newReleaseState, String oldReleaseState, DataBinder binder)
/*      */     throws DataException
/*      */   {
/* 2870 */     boolean isDeleteImmediate = false;
/* 2871 */     if ("NE".indexOf(oldReleaseState) >= 0)
/*      */     {
/* 2873 */       isDeleteImmediate = true;
/*      */     }
/* 2875 */     else if (oldReleaseState.equals("Y"))
/*      */     {
/* 2877 */       String indexerState = this.m_binder.getAllowMissing("dIndexerState");
/* 2878 */       String indexedID = binder.getAllowMissing("dIndexedID");
/* 2879 */       if ((indexerState != null) && (indexedID != null) && (indexerState.length() == 0) && (indexedID.length() == 0))
/*      */       {
/* 2882 */         isDeleteImmediate = true;
/*      */       }
/*      */     }
/*      */ 
/* 2886 */     if (isDeleteImmediate)
/*      */     {
/* 2890 */       checkOrDeleteRevClassesEntry(binder);
/* 2891 */       this.m_workspace.execute("Drevision", binder);
/* 2892 */       IdcDataSourceUtils.deleteData(this.m_workspace, "DocMetaData", binder, this.m_service);
/* 2893 */       this.m_workspace.execute("Ddocument", binder);
/*      */     }
/*      */     else
/*      */     {
/* 2898 */       this.m_workspace.execute("UdeleteRevision", binder);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void checkOrDeleteRevClassesEntry(DataBinder binder) throws DataException
/*      */   {
/* 2904 */     ResultSet rset = this.m_workspace.createResultSet("QrevCountWithDID", binder);
/* 2905 */     String countStr = rset.getStringValueByName("theCount");
/* 2906 */     int count = NumberUtils.parseInteger(countStr, 0);
/* 2907 */     if (count != 1)
/*      */       return;
/* 2909 */     this.m_workspace.execute("DrevClassesWithdID", binder);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void setMetaDataOnlyInfo()
/*      */     throws DataException
/*      */   {
/* 2923 */     String rsetName = this.m_currentAction.getParamAt(0);
/* 2924 */     ResultSet rset = this.m_binder.getResultSet(rsetName);
/* 2925 */     if ((rset == null) || (rset.isEmpty()))
/*      */     {
/* 2927 */       return;
/*      */     }
/*      */ 
/* 2930 */     String format = ResultSetUtils.getValue(rset, "dFormat");
/* 2931 */     String filePath = this.m_binder.getLocal("primaryFile:path");
/* 2932 */     if ((!format.startsWith("idcmeta")) || (this.m_binder.getLocal("createPrimaryMetaFile") != null) || (filePath != null))
/*      */       return;
/* 2934 */     this.m_binder.putLocal("createPrimaryMetaFile", "1");
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void setDeleteRevReleaseState()
/*      */     throws DataException
/*      */   {
/* 2946 */     String rsetName = this.m_currentAction.getParamAt(0);
/* 2947 */     ResultSet rset = this.m_binder.getResultSet(rsetName);
/* 2948 */     if (rset == null)
/*      */     {
/* 2951 */       String msg = LocaleUtils.encodeMessage("csResultSetMissing", null, rsetName);
/* 2952 */       throw new DataException(msg);
/*      */     }
/* 2954 */     String releaseState = ResultSetUtils.getValue(rset, "dReleaseState");
/* 2955 */     if (releaseState == null)
/*      */     {
/* 2957 */       throw new DataException(new StringBuilder().append("!$The field dReleaseState is missing from the result set '").append(rsetName).append("'. ").toString());
/*      */     }
/*      */ 
/* 2961 */     String indexerState = ResultSetUtils.getValue(rset, "dIndexerState");
/* 2962 */     boolean determinePromoted = indexerState.length() > 0;
/* 2963 */     String status = ResultSetUtils.getValue(rset, "dStatus");
/* 2964 */     determineIndexerPromotedRevisionAndRevisionRankMaximum(releaseState, status, determinePromoted, true);
/* 2965 */     String promotedID = this.m_promotedID;
/* 2966 */     if (promotedID != null)
/*      */     {
/* 2968 */       String curID = this.m_binder.get("dID");
/* 2969 */       if (curID.equals(promotedID))
/*      */       {
/* 2973 */         releaseState = "U";
/*      */       }
/*      */     }
/*      */ 
/* 2977 */     String newReleaseState = computeDeleteRevReleaseState(releaseState);
/* 2978 */     this.m_binder.putLocal("newReleaseState", newReleaseState);
/* 2979 */     this.m_binder.putLocal("currentReleaseState", releaseState);
/*      */   }
/*      */ 
/*      */   public String computeDeleteRevReleaseState(String dReleaseState)
/*      */   {
/* 2984 */     String newReleaseState = "N";
/* 2985 */     if ("YIU".indexOf(dReleaseState) >= 0)
/*      */     {
/* 2987 */       newReleaseState = "U";
/*      */     }
/* 2989 */     else if ("E".indexOf(dReleaseState) >= 0)
/*      */     {
/* 2991 */       newReleaseState = "E";
/*      */     }
/* 2993 */     return newReleaseState;
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void computeDeleteSecurity() throws DataException
/*      */   {
/* 2999 */     boolean doSkipComputeDeleteSecurity = DataBinderUtils.getLocalBoolean(this.m_binder, "doSkipComputeDeleteSecurity", false);
/* 3000 */     if (doSkipComputeDeleteSecurity == true)
/*      */     {
/* 3002 */       return;
/*      */     }
/* 3004 */     ServiceData serviceData = this.m_service.getServiceData();
/*      */ 
/* 3006 */     if ((this.m_isWf) && (this.m_wfImplementor != null) && (!this.m_wfImplementor.isDeleteAllWorkflowDocs()))
/*      */     {
/* 3008 */       String workflowState = this.m_binder.get("dWorkflowState");
/* 3009 */       if (workflowState.length() > 0)
/*      */       {
/* 3015 */         serviceData.m_accessLevel = 1;
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/* 3020 */       boolean isDeleteDoc = this.m_service.isConditionVarTrue("IsDeleteDoc");
/* 3021 */       int priv = 4;
/* 3022 */       boolean isAuthorDelete = SharedObjects.getEnvValueAsBoolean("AuthorDelete", false);
/*      */ 
/* 3024 */       if (isAuthorDelete)
/*      */       {
/* 3026 */         ResultSet rset = null;
/* 3027 */         String dUser = this.m_binder.get("dUser");
/* 3028 */         if (isDeleteDoc)
/*      */         {
/* 3032 */           rset = this.m_binder.getResultSet("REVISIONS");
/*      */         }
/*      */         else
/*      */         {
/* 3037 */           rset = this.m_binder.getResultSet("DOC_INFO");
/*      */         }
/*      */ 
/* 3040 */         if ((rset != null) && (!rset.isEmpty()))
/*      */         {
/* 3042 */           boolean isReset = true;
/* 3043 */           int authorIndex = ResultSetUtils.getIndexMustExist(rset, "dDocAuthor");
/* 3044 */           for (rset.first(); rset.isRowPresent(); rset.next())
/*      */           {
/* 3046 */             String dDocAuthor = rset.getStringValue(authorIndex);
/* 3047 */             if (dUser.equals(dDocAuthor))
/*      */               continue;
/* 3049 */             isReset = false;
/* 3050 */             break;
/*      */           }
/*      */ 
/* 3053 */           if (isReset)
/*      */           {
/* 3055 */             priv = 3;
/*      */           }
/*      */ 
/* 3058 */           rset.first();
/*      */         }
/*      */       }
/* 3061 */       serviceData.m_accessLevel = priv;
/*      */     }
/*      */     try
/*      */     {
/* 3065 */       PluginFilters.filter("postComputeDeleteSecurity", this.m_workspace, this.m_binder, this.m_service);
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/* 3069 */       throw new DataException(e.getMessage());
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void deleteRevFiles() throws ServiceException, DataException
/*      */   {
/* 3076 */     String docListName = "DOC_LIST";
/* 3077 */     String docQuery = "Qdocuments";
/* 3078 */     ResultSet rset = this.m_workspace.createResultSet(docQuery, this.m_binder);
/* 3079 */     if (rset == null)
/*      */     {
/* 3081 */       String msg = LocaleUtils.encodeMessage("csInvalidQuery", null, docQuery);
/* 3082 */       throw new DataException(msg);
/*      */     }
/*      */ 
/* 3085 */     if (rset.isEmpty())
/*      */       return;
/* 3087 */     DataResultSet drset = new DataResultSet();
/* 3088 */     drset.copy(rset);
/* 3089 */     this.m_binder.addResultSet(docListName, drset);
/* 3090 */     String prevRsName = null;
/* 3091 */     if (this.m_currentAction.getNumParams() > 0)
/*      */     {
/* 3093 */       prevRsName = this.m_currentAction.getParamAt(0);
/*      */     }
/*      */ 
/* 3096 */     deleteQueryFiles(docListName, prevRsName);
/* 3097 */     this.m_binder.removeResultSet(docListName);
/*      */   }
/*      */ 
/*      */   public void deleteQueryFiles(String resultSetName, String prevRsName)
/*      */     throws DataException, ServiceException
/*      */   {
/* 3104 */     boolean isNotifyReleaseChange = false;
/* 3105 */     Properties localData = null;
/* 3106 */     boolean isUpdate = DataBinderUtils.getLocalBoolean(this.m_binder, "IsUpdate", false);
/*      */     try
/*      */     {
/* 3109 */       Properties prevProps = null;
/* 3110 */       DataResultSet prevDrSet = null;
/* 3111 */       if (prevRsName != null)
/*      */       {
/* 3113 */         prevDrSet = (DataResultSet)this.m_binder.getResultSet(prevRsName);
/*      */       }
/*      */ 
/* 3116 */       if (prevDrSet != null)
/*      */       {
/* 3118 */         if (isUpdate)
/*      */         {
/* 3122 */           checkUpdateChange(prevRsName);
/*      */         }
/*      */ 
/* 3128 */         localData = this.m_binder.getLocalData();
/* 3129 */         this.m_binder.setLocalData(new Properties());
/* 3130 */         prevProps = prevDrSet.getCurrentRowProps();
/*      */       }
/*      */ 
/* 3133 */       boolean isFirst = true;
/* 3134 */       DataResultSet drset = (DataResultSet)this.m_binder.getResultSet(resultSetName);
/* 3135 */       for (drset.first(); drset.isRowPresent(); drset.next())
/*      */       {
/* 3137 */         PropParameters params = null;
/* 3138 */         Properties props = drset.getCurrentRowProps();
/* 3139 */         if (prevProps != null)
/*      */         {
/* 3141 */           params = new PropParameters(props, new PropParameters(prevProps));
/*      */         }
/*      */         else
/*      */         {
/* 3145 */           params = new PropParameters(props, this.m_binder);
/*      */         }
/* 3147 */         Map map = DataBinderUtils.getInternalMap(params);
/*      */ 
/* 3151 */         if (isFirst)
/*      */         {
/* 3157 */           isFirst = false;
/*      */         }
/*      */ 
/* 3160 */         String docType = params.get("dDocType");
/* 3161 */         if (docType == null) continue; if (docType.length() == 0)
/*      */         {
/*      */           continue;
/*      */         }
/*      */ 
/* 3168 */         String docID = params.get("dDocID");
/* 3169 */         String id = params.get("dID");
/* 3170 */         String isWebFormat = params.get("dIsWebFormat");
/* 3171 */         String isPrimaryStr = params.get("dIsPrimary");
/*      */ 
/* 3173 */         IdcFileDescriptor source = null;
/* 3174 */         boolean deleteDoc = true;
/* 3175 */         boolean isWebFormatVal = !isWebFormat.equals("0");
/* 3176 */         if (isWebFormatVal)
/*      */         {
/* 3180 */           String status = params.get("dStatus");
/* 3181 */           String webExt = props.getProperty("dExtension");
/* 3182 */           props.put("dWebExtension", webExt);
/*      */ 
/* 3185 */           if (status.equals("GENWWW"))
/*      */           {
/* 3187 */             String procState = params.get("dProcessingState");
/* 3188 */             char pState = ' ';
/* 3189 */             if ((procState != null) && (procState.length() > 0))
/*      */             {
/* 3191 */               pState = procState.charAt(0);
/*      */             }
/*      */ 
/* 3194 */             switch (pState)
/*      */             {
/*      */             case 'C':
/* 3198 */               if (!isUpdate)
/*      */               {
/* 3200 */                 deleteDoc = false;
/* 3201 */                 Properties docProps = new Properties();
/* 3202 */                 docProps.setProperty("dID", id);
/* 3203 */                 String[] lockedIDs = QueueProcessor.removeDocFromConversionQueue(this.m_workspace, docID, docProps);
/* 3204 */                 if (lockedIDs != null)
/*      */                 {
/* 3206 */                   for (int i = 0; i < lockedIDs.length; ++i)
/*      */                   {
/* 3208 */                     this.m_lockedIDsList.put(lockedIDs[i], "1");
/*      */                   }
/*      */                 }
/*      */               }
/*      */               else
/*      */               {
/* 3214 */                 this.m_lockedIDsList.put(docID, "1");
/*      */               }
/*      */ 
/* 3217 */               break;
/*      */             case 'M':
/*      */             case 'P':
/*      */             case 'Y':
/* 3222 */               break;
/*      */             default:
/* 3225 */               deleteDoc = false;
/*      */             }
/*      */           }
/*      */ 
/* 3229 */           if (deleteDoc)
/*      */           {
/* 3231 */             String state = params.get("dReleaseState");
/* 3232 */             String originalState = null;
/* 3233 */             if (!this.m_determinedPromotedID)
/*      */             {
/* 3235 */               String indexerState = params.get("dIndexerState");
/* 3236 */               if (indexerState.length() > 0)
/*      */               {
/* 3238 */                 determineIndexerPromotedRevisionAndRevisionRankMaximum(state, status, true, false);
/*      */               }
/*      */             }
/*      */ 
/* 3242 */             boolean isReleased = false;
/* 3243 */             if ("YUI".indexOf(state) >= 0)
/*      */             {
/* 3245 */               isReleased = true;
/*      */             }
/* 3247 */             if ((this.m_promotedID != null) && (id.equals(this.m_promotedID)))
/*      */             {
/* 3249 */               if (!isReleased)
/*      */               {
/* 3251 */                 originalState = state;
/*      */               }
/* 3253 */               state = "U";
/*      */             }
/* 3255 */             if (isReleased)
/*      */             {
/* 3257 */               isNotifyReleaseChange = true;
/*      */             }
/* 3259 */             map.put("RenditionId", "webViewableFile");
/* 3260 */             if (originalState != null)
/*      */             {
/* 3265 */               IdcFileDescriptor originalSource = this.m_fileStore.createDescriptor(params, null, this.m_service);
/*      */ 
/* 3268 */               this.m_filesToDelete.put(new StringBuilder().append(docID).append("_original").toString(), originalSource);
/*      */ 
/* 3272 */               map.put("dReleaseState", state);
/*      */             }
/* 3274 */             source = this.m_fileStore.createDescriptor(params, null, this.m_service);
/*      */           }
/*      */ 
/* 3278 */           if (!this.m_service.isConditionVarTrue("KeepOldAdditionalRenditions"))
/*      */           {
/* 3280 */             String docName = params.get("dDocName");
/* 3281 */             String revLabel = params.get("dRevLabel");
/* 3282 */             if ((deleteRenditions(docName, revLabel, docID, this.m_filesToDelete, params)) && 
/* 3284 */               (isUpdate))
/*      */             {
/* 3286 */               this.m_workspace.execute("UrenditionsClear", this.m_binder);
/*      */             }
/*      */           }
/*      */ 
/*      */         }
/*      */         else
/*      */         {
/* 3293 */           boolean isPrimary = !isPrimaryStr.equals("0");
/* 3294 */           String rendition = "primaryFile";
/* 3295 */           if (!isPrimary)
/*      */           {
/* 3297 */             rendition = "alternateFile";
/*      */           }
/* 3299 */           map.put("RenditionId", rendition);
/* 3300 */           source = this.m_fileStore.createDescriptor(params, null, this.m_service);
/*      */         }
/* 3302 */         if ((!deleteDoc) || (source == null))
/*      */           continue;
/* 3304 */         this.m_filesToDelete.put(docID, source);
/*      */       }
/*      */ 
/*      */     }
/*      */     finally
/*      */     {
/* 3310 */       if (localData != null)
/*      */       {
/* 3312 */         this.m_binder.setLocalData(localData);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 3317 */     if (!isNotifyReleaseChange)
/*      */       return;
/* 3319 */     notifyReleaseChange();
/*      */   }
/*      */ 
/*      */   protected void determineIndexerPromotedRevisionAndRevisionRankMaximum(String releaseState, String status, boolean determinePromoted, boolean determineMaxRank)
/*      */     throws DataException
/*      */   {
/* 3333 */     boolean determineMaximumRevRank = (determineMaxRank) && (!this.m_determinedMinimumRankDeleteRevID) && (SharedObjects.getEnvValueAsBoolean("RevisionRankHasMaximum", false));
/*      */ 
/* 3335 */     int maxRevRank = -1;
/* 3336 */     if (determineMaximumRevRank)
/*      */     {
/* 3338 */       maxRevRank = SharedObjects.getEnvironmentInt("RevisionRankMaximum", 5);
/*      */     }
/* 3340 */     boolean determinePromotedID = (determinePromoted) && (!this.m_determinedPromotedID);
/* 3341 */     if ((determinePromotedID) && ("NRO".indexOf(releaseState) < 0))
/*      */     {
/* 3343 */       determinePromotedID = false;
/*      */     }
/* 3345 */     if ((determinePromotedID) && (status != null) && (!status.equals("DONE")) && (!status.equals("RELEASED")))
/*      */     {
/* 3348 */       determinePromotedID = false;
/*      */     }
/* 3350 */     if ((!determinePromotedID) && (!determineMaximumRevRank))
/*      */     {
/* 3353 */       return;
/*      */     }
/* 3355 */     if (determinePromotedID)
/*      */     {
/* 3357 */       this.m_promotedID = null;
/*      */     }
/* 3359 */     String query = (determineMaximumRevRank) ? "QrevisionsAllStateAndRevRankOnly" : "QrevisionsAllStateOnly";
/* 3360 */     ResultSet allRevsRset = this.m_workspace.createResultSet(query, this.m_binder);
/*      */ 
/* 3364 */     boolean foundDeleteCurrent = false;
/* 3365 */     String latestGoodID = null;
/* 3366 */     boolean computingPromotedID = determinePromotedID;
/* 3367 */     boolean foundMinRevRankID = false;
/* 3368 */     String minRevRankID = null;
/* 3369 */     boolean computingMaxRevRank = determineMaximumRevRank;
/* 3370 */     for (allRevsRset.first(); allRevsRset.isRowPresent(); allRevsRset.next())
/*      */     {
/* 3372 */       if (computingPromotedID)
/*      */       {
/* 3374 */         String revReleaseState = ResultSetUtils.getValue(allRevsRset, "dReleaseState");
/* 3375 */         String revStatus = ResultSetUtils.getValue(allRevsRset, "dStatus");
/* 3376 */         boolean isLatestReleased = "YUI".indexOf(revReleaseState) >= 0;
/* 3377 */         if ((revStatus.equals("DONE")) || (revStatus.equals("RELEASED")))
/*      */         {
/* 3380 */           if ("NRO".indexOf(revReleaseState) >= 0)
/*      */           {
/* 3382 */             if (revReleaseState.equals("O"))
/*      */             {
/* 3385 */               latestGoodID = ResultSetUtils.getValue(allRevsRset, "dID");
/*      */             }
/* 3387 */             computingPromotedID = false;
/*      */           }
/* 3389 */           if (isLatestReleased)
/*      */           {
/* 3391 */             computingPromotedID = false;
/*      */           }
/*      */         }
/* 3394 */         else if ((!foundDeleteCurrent) && (revStatus.equals("DELETED")))
/*      */         {
/* 3396 */           if (isLatestReleased)
/*      */           {
/* 3398 */             String revIndexerState = ResultSetUtils.getValue(allRevsRset, "dIndexerState");
/* 3399 */             if (revIndexerState.length() > 0)
/*      */             {
/* 3403 */               foundDeleteCurrent = true;
/*      */             }
/*      */             else
/*      */             {
/* 3407 */               computingPromotedID = false;
/*      */             }
/*      */           }
/*      */         }
/* 3411 */         else if (isLatestReleased)
/*      */         {
/* 3413 */           computingPromotedID = false;
/*      */         }
/*      */       }
/* 3416 */       if (computingMaxRevRank)
/*      */       {
/* 3418 */         String revRankStr = ResultSetUtils.getValue(allRevsRset, "dRevRank");
/* 3419 */         int revRank = NumberUtils.parseInteger(revRankStr, 0);
/* 3420 */         if (revRank >= maxRevRank)
/*      */         {
/* 3422 */           minRevRankID = ResultSetUtils.getValue(allRevsRset, "dID");
/* 3423 */           foundMinRevRankID = true;
/* 3424 */           computingMaxRevRank = false;
/*      */         }
/*      */       }
/* 3427 */       if ((!computingPromotedID) && (!computingMaxRevRank))
/*      */       {
/*      */         break;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 3434 */     if (determinePromotedID)
/*      */     {
/* 3436 */       if (foundDeleteCurrent)
/*      */       {
/* 3438 */         this.m_promotedID = latestGoodID;
/*      */       }
/* 3440 */       this.m_determinedPromotedID = true;
/*      */     }
/* 3442 */     if (!determineMaximumRevRank)
/*      */       return;
/* 3444 */     if (foundMinRevRankID)
/*      */     {
/* 3446 */       this.m_minimumRankDeleteRevID = minRevRankID;
/*      */     }
/* 3448 */     this.m_determinedMinimumRankDeleteRevID = true;
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void markDocDeleted()
/*      */     throws DataException, ServiceException
/*      */   {
/* 3455 */     markRevDeleted();
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void markRevDeleted()
/*      */     throws DataException, ServiceException
/*      */   {
/* 3462 */     String isUpdateStr = this.m_binder.getAllowMissing("IsUpdate");
/* 3463 */     if (isUpdateStr != null)
/*      */     {
/* 3465 */       this.m_binder.putLocal("IsUpdate", "");
/*      */     }
/*      */ 
/* 3472 */     ResultSet rset = this.m_workspace.createResultSet("QprojectDocument", this.m_binder);
/* 3473 */     if ((rset == null) || (rset.isEmpty()))
/*      */     {
/* 3475 */       return;
/*      */     }
/*      */ 
/* 3478 */     String agent = this.m_binder.getAllowMissing("dPrjAgentID");
/* 3479 */     if ((agent == null) || (agent.length() == 0))
/*      */     {
/* 3481 */       this.m_binder.putLocal("dPrjAgentID", "repository");
/*      */     }
/* 3483 */     this.m_binder.putLocal("dPrjAction", "DELETED");
/*      */ 
/* 3485 */     this.m_workspace.execute("UprjDocumentDelete", this.m_binder);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void removeConversionJob()
/*      */     throws DataException, ServiceException
/*      */   {
/* 3492 */     ResultSet rset = this.m_workspace.createResultSet("QisActiveRefineryJob", this.m_binder);
/* 3493 */     if (rset.isEmpty()) {
/*      */       return;
/*      */     }
/*      */ 
/* 3497 */     DataBinder binder = new DataBinder(this.m_binder.getEnvironment());
/* 3498 */     Properties props = ResultSetUtils.getCurrentRowProps(rset);
/* 3499 */     binder.setLocalData(props);
/* 3500 */     String oldConversionState = props.getProperty("dConversionState");
/* 3501 */     binder.putLocal("dConvMessage", "!csRefineryRequestedDelete");
/* 3502 */     binder.putLocal("dConversionState", "Aborted");
/* 3503 */     binder.putLocal("oldConversionState", oldConversionState);
/* 3504 */     RefineryUtils.updateConversionJobStateEx(this.m_workspace, binder);
/* 3505 */     ConvertedWorkThread.enableCheckForAbortedJobs();
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void checkCounters()
/*      */     throws DataException, ServiceException
/*      */   {
/* 3512 */     List params = this.m_currentAction.getParams();
/* 3513 */     int[] indexes = new int[ID_INCREMENTS.length];
/* 3514 */     int paramsLength = params.size();
/*      */ 
/* 3516 */     for (int i = 0; i < ID_INCREMENTS.length; ++i)
/*      */     {
/* 3518 */       indexes[i] = i;
/* 3519 */       if (paramsLength == 0)
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/* 3524 */       boolean found = false;
/* 3525 */       String key = ID_INCREMENTS[i][3];
/*      */ 
/* 3527 */       for (int j = 0; j < paramsLength; ++j)
/*      */       {
/* 3529 */         String type = (String)params.get(j);
/* 3530 */         if (!type.equalsIgnoreCase(key))
/*      */           continue;
/* 3532 */         found = true;
/* 3533 */         break;
/*      */       }
/*      */ 
/* 3537 */       if (found)
/*      */         continue;
/* 3539 */       indexes[i] = -1;
/*      */     }
/*      */ 
/* 3542 */     checkCountersByType(indexes);
/*      */   }
/*      */ 
/*      */   public void checkCountersByType(int[] indexes) throws DataException, ServiceException
/*      */   {
/* 3547 */     for (int i = 0; i < indexes.length; ++i)
/*      */     {
/* 3549 */       int type = indexes[i];
/* 3550 */       if (type == -1) {
/*      */         continue;
/*      */       }
/*      */ 
/* 3554 */       long nextCounter = IdcCounterUtils.nextValue(this.m_workspace, ID_INCREMENTS[type][3]);
/* 3555 */       int curCounter = Integer.parseInt(this.m_binder.get(ID_INCREMENTS[type][2]));
/*      */ 
/* 3557 */       if (curCounter < nextCounter) {
/*      */         continue;
/*      */       }
/* 3560 */       IdcCounterUtils.registerCounter(this.m_workspace, ID_INCREMENTS[type][3], curCounter + 1, 1);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void addIncrementID(DataResultSet dSet, int type, String clmn)
/*      */     throws DataException, ServiceException
/*      */   {
/* 3569 */     long nextValue = IdcCounterUtils.nextValue(this.m_workspace, ID_INCREMENTS[type][3]);
/* 3570 */     boolean isLocal = false;
/*      */     try
/*      */     {
/* 3573 */       if (dSet == null)
/*      */       {
/* 3575 */         isLocal = true;
/*      */       }
/*      */       else
/*      */       {
/* 3579 */         int revClassIndex = ResultSetUtils.getIndexMustExist(dSet, clmn);
/* 3580 */         dSet.setCurrentValue(revClassIndex, new StringBuilder().append("").append(nextValue).toString());
/*      */       }
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/* 3585 */       isLocal = true;
/*      */     }
/*      */ 
/* 3588 */     if (!isLocal) {
/*      */       return;
/*      */     }
/* 3591 */     this.m_binder.putLocal(clmn, new StringBuilder().append("").append(nextValue).toString());
/*      */   }
/*      */ 
/*      */   public boolean isUniqueDocName()
/*      */     throws ServiceException, DataException
/*      */   {
/* 3599 */     String docName = this.m_binder.getLocal("dDocName");
/* 3600 */     if ((docName == null) || (docName.length() == 0))
/*      */     {
/* 3602 */       this.m_checkUnique = true;
/* 3603 */       return true;
/*      */     }
/*      */ 
/* 3611 */     ResultSet rset = this.m_workspace.createResultSet("QuniqueDocName", this.m_binder);
/*      */ 
/* 3614 */     return (rset == null) || (!rset.isRowPresent());
/*      */   }
/*      */ 
/*      */   public boolean createDocData(String name, String isPrimary, boolean isCreateDocID)
/*      */     throws DataException, ServiceException
/*      */   {
/* 3624 */     String filePath = this.m_binder.getAllowMissing(name);
/* 3625 */     boolean isPrimaryFile = isPrimary.equals("1");
/* 3626 */     if ((filePath == null) && (isPrimaryFile))
/*      */     {
/* 3629 */       String docName = this.m_binder.getLocal("dDocName").toLowerCase();
/* 3630 */       String ext = this.m_binder.getLocal("dExtension");
/* 3631 */       if (ext == null)
/*      */       {
/* 3634 */         String msg = LocaleUtils.encodeMessage("csCheckinPrimaryFileError", null);
/* 3635 */         this.m_service.createServiceException(null, msg);
/*      */       }
/*      */       else
/*      */       {
/* 3639 */         ext = ext.toLowerCase();
/*      */       }
/* 3641 */       filePath = new StringBuilder().append(docName).append(".").append(ext).toString();
/*      */     }
/*      */ 
/* 3644 */     Object[] params = { name, isPrimary, new Boolean(isCreateDocID), filePath };
/* 3645 */     this.m_service.setCachedObject("createDocData:parameters", params);
/* 3646 */     int returnCode = PluginFilters.filter("createDocData", this.m_workspace, this.m_binder, this.m_service);
/* 3647 */     if (returnCode == -1)
/*      */     {
/* 3649 */       return true;
/*      */     }
/* 3651 */     name = (String)params[0];
/* 3652 */     isPrimary = (String)params[1];
/* 3653 */     isPrimaryFile = isPrimary.equals("1");
/* 3654 */     isCreateDocID = ((Boolean)params[2]).booleanValue();
/* 3655 */     filePath = (String)params[3];
/*      */ 
/* 3657 */     if ((filePath == null) || (filePath.length() == 0))
/*      */     {
/* 3659 */       return false;
/*      */     }
/*      */ 
/* 3662 */     if (isCreateDocID)
/*      */     {
/* 3665 */       addIncrementID(null, 2, "dDocID");
/*      */     }
/*      */ 
/* 3669 */     String fileName = FileUtils.fileSlashes(filePath);
/* 3670 */     int extIndex = fileName.lastIndexOf(46);
/* 3671 */     int nameIndex = fileName.lastIndexOf(47);
/*      */ 
/* 3673 */     if (extIndex < nameIndex)
/*      */     {
/* 3677 */       extIndex = -1;
/*      */     }
/*      */ 
/* 3683 */     String key = (isPrimaryFile) ? "dOriginalName" : "alternateFile:name";
/* 3684 */     String orgName = this.m_binder.getLocal(key);
/* 3685 */     if ((orgName == null) || (orgName.length() == 0))
/*      */     {
/* 3687 */       orgName = fileName.substring(nameIndex + 1);
/*      */     }
/*      */ 
/* 3690 */     String extension = "";
/* 3691 */     if (extIndex >= 0)
/*      */     {
/* 3693 */       extension = fileName.substring(extIndex + 1);
/*      */     }
/*      */ 
/* 3697 */     this.m_binder.putLocal("dIsPrimary", isPrimary);
/* 3698 */     this.m_binder.putLocal("dOriginalName", orgName);
/* 3699 */     this.m_binder.putLocal("dExtension", extension.toLowerCase());
/* 3700 */     this.m_binder.putLocal("dLocation", "");
/*      */ 
/* 3702 */     String rawDocID = this.m_binder.getLocal("dDocID");
/* 3703 */     if (rawDocID != null)
/*      */     {
/* 3709 */       this.m_binder.putLocal("dRawDocID", this.m_binder.getLocal("dDocID"));
/*      */     }
/*      */ 
/* 3714 */     determineDocConversion(false, name);
/*      */ 
/* 3716 */     return true;
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void updateRevisionIDAndLabel()
/*      */     throws DataException, ServiceException
/*      */   {
/* 3724 */     boolean isUpdate = false;
/* 3725 */     if (this.m_service.isConditionVarTrue("HasOriginal"))
/*      */     {
/* 3727 */       if (this.m_isWf)
/*      */       {
/* 3729 */         String stepType = this.m_binder.getLocal("wfStepCheckinType");
/* 3730 */         isUpdate = stepType.indexOf(":CE:") >= 0;
/*      */       }
/*      */ 
/*      */     }
/*      */     else {
/* 3735 */       isUpdate = true;
/*      */     }
/* 3737 */     if (isUpdate)
/*      */       return;
/* 3739 */     updateRevisionLabel();
/*      */   }
/*      */ 
/*      */   public void updateRevisionID(boolean updateLabel)
/*      */     throws DataException, ServiceException
/*      */   {
/*      */     try
/*      */     {
/* 3748 */       int revID = Integer.parseInt(this.m_binder.get("dRevisionID"));
/* 3749 */       this.m_binder.putLocal("dRevisionID", String.valueOf(revID + 1));
/* 3750 */       if (updateLabel)
/*      */       {
/* 3752 */         updateRevisionLabel();
/*      */       }
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 3757 */       this.m_service.createServiceException(e, "!csInvalidRevision");
/*      */     }
/* 3759 */     clearStatusParameters();
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void updateRevisionLabel() throws DataException
/*      */   {
/* 3765 */     String revLabel = this.m_binder.get("dRevLabel");
/* 3766 */     String nextRev = RevisionSpec.getNext(revLabel);
/* 3767 */     if (nextRev == null)
/*      */     {
/* 3769 */       nextRev = RevisionSpec.getInvalidLabel();
/*      */     }
/* 3771 */     this.m_binder.putLocal("dRevLabel", nextRev);
/*      */   }
/*      */ 
/*      */   protected void clearStatusParameters()
/*      */   {
/* 3777 */     this.m_binder.putLocal("dStatus", "");
/*      */   }
/*      */ 
/*      */   public String getFilePath(String fileKey)
/*      */     throws DataException
/*      */   {
/* 3783 */     return ServerFileUtils.getTemporaryFilePath(this.m_binder, fileKey);
/*      */   }
/*      */ 
/*      */   public void addFile(String fileKey, String query, boolean mustExist)
/*      */     throws DataException, ServiceException
/*      */   {
/* 3789 */     String dOriginalName = this.m_binder.getLocal("dOriginalName");
/*      */ 
/* 3791 */     int maxLen = SharedObjects.getEnvironmentInt("dOriginalName:maxLength", 80);
/* 3792 */     if (maxLen < dOriginalName.length())
/*      */     {
/* 3794 */       String msg = LocaleUtils.encodeMessage("csCheckinFileNameTooLong", null, new StringBuilder().append("").append(maxLen).toString());
/*      */ 
/* 3796 */       this.m_service.createServiceException(null, msg);
/*      */     }
/*      */ 
/* 3799 */     String filePath = getFilePath(fileKey);
/* 3800 */     if ((this.m_changePrimaryFileExtension) && (fileKey.equals("primaryFile")))
/*      */     {
/* 3802 */       String ext = FileUtils.getExtension(filePath);
/* 3803 */       String newExt = new StringBuilder().append("@p.").append(ext).toString();
/* 3804 */       this.m_binder.putLocal("dExtension", newExt);
/*      */     }
/*      */ 
/* 3808 */     if ((filePath == null) || (filePath.trim().length() == 0))
/*      */     {
/* 3810 */       if (mustExist)
/*      */       {
/* 3812 */         String msg = LocaleUtils.encodeMessage("csCheckinFileKeyNotSpecified", null, fileKey);
/*      */ 
/* 3814 */         throw new DataException(msg);
/*      */       }
/* 3816 */       return;
/*      */     }
/*      */ 
/* 3819 */     this.m_service.setCachedObject(new StringBuilder().append(fileKey).append(":path").toString(), filePath);
/* 3820 */     this.m_binder.putLocal(new StringBuilder().append(fileKey).append(":path").toString(), filePath);
/* 3821 */     DataStreamWrapper streamWrapper = new DataStreamWrapper(filePath, dOriginalName, null);
/*      */ 
/* 3824 */     String[] keys = { "dLanguage", "dCharacterSet" };
/* 3825 */     for (String k : keys)
/*      */     {
/* 3827 */       String val = getFileParameter(fileKey, k);
/* 3828 */       if (val == null)
/*      */       {
/* 3830 */         val = "";
/*      */       }
/*      */ 
/* 3833 */       this.m_binder.putLocal(k, val);
/*      */     }
/*      */ 
/* 3837 */     Object[] params = { streamWrapper, fileKey, query };
/* 3838 */     this.m_service.setCachedObject("addFile:parameters", params);
/* 3839 */     if (PluginFilters.filter("addFile", this.m_workspace, this.m_binder, this.m_service) == -1)
/*      */     {
/* 3841 */       return;
/*      */     }
/*      */ 
/* 3844 */     streamWrapper = (DataStreamWrapper)params[0];
/* 3845 */     fileKey = (String)params[1];
/* 3846 */     query = (String)params[2];
/*      */ 
/* 3849 */     if (streamWrapper != null)
/*      */     {
/* 3851 */       addFileToFileStore(fileKey, streamWrapper);
/*      */     }
/*      */ 
/* 3855 */     if (query != null)
/*      */     {
/* 3857 */       this.m_workspace.execute(query, this.m_binder);
/*      */     }
/*      */ 
/* 3861 */     for (String k : keys)
/*      */     {
/* 3863 */       this.m_binder.removeLocal(k);
/*      */     }
/*      */   }
/*      */ 
/*      */   public String getFileParameter(String fileKey, String parameterName)
/*      */   {
/* 3869 */     return this.m_binder.getLocal(new StringBuilder().append(fileKey).append(":").append(parameterName).toString());
/*      */   }
/*      */ 
/*      */   public void addFileToFileStore(String fileKey, DataStreamWrapper streamWrapper)
/*      */     throws DataException, ServiceException
/*      */   {
/*      */     try
/*      */     {
/* 3877 */       Map args = new HashMap();
/* 3878 */       args.put("copyFiles", new StringBuilder().append("").append(this.m_copyFiles).toString());
/* 3879 */       args.put("isSplitDir", new StringBuilder().append("").append(this.m_isSplitDir).toString());
/* 3880 */       args.put("isNew", "1");
/*      */ 
/* 3882 */       if (!this.m_isSplitDir)
/*      */       {
/* 3884 */         String useRename = this.m_binder.getLocal("doRenameWhenPossible");
/* 3885 */         if (useRename == null)
/*      */         {
/* 3887 */           useRename = new StringBuilder().append("").append(this.m_useRenameWhenPossible).toString();
/*      */         }
/* 3889 */         args.put("doRenameWhenPossible", useRename);
/*      */       }
/* 3891 */       if (streamWrapper.m_filePath != null)
/*      */       {
/* 3893 */         args.put("TempPath", streamWrapper.m_filePath);
/*      */       }
/* 3895 */       this.m_binder.putLocal("RenditionId", fileKey);
/*      */ 
/* 3898 */       Object[] params = { streamWrapper, fileKey, args };
/* 3899 */       this.m_service.setCachedObject("addFileToFileStore:parameters", params);
/* 3900 */       if (PluginFilters.filter("addFileToFileStore", this.m_workspace, this.m_binder, this.m_service) == -1)
/*      */       {
/*      */         return;
/*      */       }
/*      */ 
/* 3906 */       IdcFileDescriptor descriptor = this.m_fileStore.createDescriptor(this.m_binder, args, this.m_service);
/*      */ 
/* 3908 */       this.m_fileStore.storeFromStreamWrapper(descriptor, streamWrapper, args, this.m_service);
/* 3909 */       if ((!this.m_copyFiles) && (streamWrapper.m_filePath != null) && (!this.m_binder.m_isExternalRequest))
/*      */       {
/* 3912 */         this.m_binder.addTempFile(streamWrapper.m_filePath);
/*      */ 
/* 3914 */         DataBinder retryBinder = (DataBinder)this.m_service.getCachedObject("RetryBinder");
/* 3915 */         if (retryBinder != null)
/*      */         {
/* 3917 */           retryBinder.addTempFile(streamWrapper.m_filePath);
/*      */         }
/*      */       }
/*      */ 
/* 3921 */       Map storageData = this.m_fileStore.getStorageData(descriptor, args, this.m_service);
/* 3922 */       this.m_service.setCachedObject(new StringBuilder().append(fileKey).append(":descriptor").toString(), descriptor);
/* 3923 */       String dFileSize = (String)storageData.get("fileSize");
/* 3924 */       if ((dFileSize != null) && (dFileSize.length() != 0))
/*      */       {
/* 3926 */         this.m_binder.putLocal("dFileSize", dFileSize);
/*      */       }
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/* 3931 */       this.m_service.createServiceException(e, "IOException");
/*      */     }
/*      */     catch (SecurityException e)
/*      */     {
/* 3940 */       this.m_service.createServiceException(e, "Security exception");
/*      */     }
/*      */     finally
/*      */     {
/* 3944 */       this.m_binder.removeLocal("RenditionId");
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void docRefinery()
/*      */     throws DataException, ServiceException
/*      */   {
/* 3952 */     if (this.m_addReleasedWebFile)
/*      */     {
/* 3954 */       addReleasedWebFileDirect();
/*      */     }
/*      */     else
/*      */     {
/* 3958 */       processDocConversion(false);
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void resubmitDocToConversion() throws DataException, ServiceException
/*      */   {
/* 3965 */     processDocConversion(true);
/*      */   }
/*      */ 
/*      */   public void determineDocConversion(boolean isResubmit, String fileKey)
/*      */     throws DataException, ServiceException
/*      */   {
/* 3973 */     DocFormats docFormats = (DocFormats)SharedObjects.getTable("DocFormats");
/* 3974 */     if (docFormats == null)
/*      */     {
/* 3976 */       this.m_service.createServiceException(null, "!csCheckinFormatsMissing");
/*      */     }
/*      */ 
/* 3980 */     boolean extractCurrentFormat = (isResubmit) && (!DataBinderUtils.getBoolean(this.m_binder, "recomputeFormat", false));
/* 3981 */     String format = null;
/*      */ 
/* 3983 */     if (extractCurrentFormat)
/*      */     {
/* 3985 */       format = this.m_binder.getAllowMissing("dFormat");
/*      */     }
/* 3987 */     if ((format == null) || (format.length() == 0))
/*      */     {
/* 3989 */       format = docFormats.getFormatByFileKey(this.m_binder, fileKey);
/*      */     }
/* 3991 */     if ((((format == null) || (format.length() == 0))) && 
/* 3993 */       (checkSpecialFormat()))
/*      */     {
/* 3995 */       return;
/*      */     }
/*      */ 
/* 3999 */     String[] convType = new String[1];
/*      */     try
/*      */     {
/* 4002 */       format = docFormats.determineFormat(this.m_binder, convType, format, extractCurrentFormat, fileKey);
/* 4003 */       this.m_binder.putLocal("dFormat", format);
/*      */ 
/* 4005 */       Object[] params = { docFormats, new Boolean(isResubmit), fileKey, format, convType };
/* 4006 */       this.m_service.setCachedObject("DocConversionParams", params);
/* 4007 */       if (PluginFilters.filter("determineDocConversion", this.m_workspace, this.m_binder, this.m_service) == -1)
/*      */       {
/* 4009 */         return;
/*      */       }
/* 4011 */       if (!checkIsSpecialConversion(format))
/*      */       {
/* 4013 */         this.m_binder.putLocal("dConversion", convType[0]);
/*      */       }
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/* 4018 */       this.m_service.createServiceException(e, "!csCheckinUnableToDetermineFormat");
/*      */     }
/*      */   }
/*      */ 
/*      */   public boolean checkSpecialFormat()
/*      */     throws DataException, ServiceException
/*      */   {
/* 4035 */     String ext = this.m_binder.getAllowMissing("dExtension");
/* 4036 */     if ((ext != null) && (ext.equalsIgnoreCase("hcsp")))
/*      */     {
/* 4039 */       String pubType = this.m_binder.getAllowMissing("dPublishType");
/* 4040 */       boolean isNavPage = (pubType != null) && (pubType.equalsIgnoreCase("N"));
/* 4041 */       if (!isNavPage)
/*      */       {
/* 4043 */         this.m_binder.putLocal("dFormat", "text/hcsp");
/* 4044 */         this.m_binder.putLocal("dConversion", "PASSTHRU");
/* 4045 */         return true;
/*      */       }
/*      */     }
/*      */ 
/* 4049 */     return false;
/*      */   }
/*      */ 
/*      */   public boolean checkIsSpecialConversion(String format)
/*      */   {
/* 4054 */     if ((format.equalsIgnoreCase("application/fdf")) && 
/* 4056 */       (!DataBinderUtils.getBoolean(this.m_binder, "DisableFdfConversion", false)))
/*      */     {
/* 4058 */       this.m_binder.putLocal("dConversion", "EXCHANGE-FDF");
/* 4059 */       return true;
/*      */     }
/*      */ 
/* 4062 */     return false;
/*      */   }
/*      */ 
/*      */   public void processDocConversion(boolean isResubmit) throws DataException, ServiceException
/*      */   {
/* 4067 */     String query = null;
/* 4068 */     if (this.m_currentAction.m_params.size() > 0)
/*      */     {
/* 4070 */       query = this.m_currentAction.getParamAt(0);
/*      */     }
/*      */ 
/* 4073 */     processDocConversion(isResubmit, query);
/*      */   }
/*      */ 
/*      */   public void finishDescriptorStateUpdate() throws DataException
/*      */   {
/* 4078 */     IdcDescriptorState states = (IdcDescriptorState)this.m_service.getCachedObject("DescriptorStates");
/*      */ 
/* 4080 */     if (states == null)
/*      */     {
/*      */       return;
/*      */     }
/*      */ 
/* 4086 */     states.finishUpdate(this.m_binder, this.m_workspace);
/*      */   }
/*      */ 
/*      */   public void processDocConversion(boolean isResubmit, String query)
/*      */     throws DataException, ServiceException
/*      */   {
/* 4093 */     boolean createNewDocId = true;
/* 4094 */     boolean hasWebViewable = false;
/* 4095 */     String webViewableFile = null;
/* 4096 */     String procState = null;
/*      */ 
/* 4099 */     boolean[] isLocked = { false };
/* 4100 */     doUnlockCheck(1, isLocked);
/* 4101 */     if (isLocked[0] != 0)
/*      */     {
/* 4104 */       if (this.m_binder.getAllowMissing("IsUpdate") == null)
/*      */       {
/* 4106 */         this.m_binder.putLocal("dProcessingState", "W");
/* 4107 */         this.m_workspace.execute("UprocessingState", this.m_binder);
/*      */       }
/*      */ 
/* 4110 */       finishDescriptorStateUpdate();
/* 4111 */       return;
/*      */     }
/*      */ 
/* 4116 */     String processingState = this.m_binder.getAllowMissing("dProcessingState");
/* 4117 */     if ((processingState != null) && (processingState.equals("W")))
/*      */     {
/* 4119 */       this.m_binder.putLocal("dProcessingState", "C");
/* 4120 */       this.m_workspace.execute("UprocessingState", this.m_binder);
/*      */     }
/*      */ 
/* 4124 */     this.m_binder.putLocal("noDocLock", "1");
/*      */ 
/* 4126 */     if (isResubmit)
/*      */     {
/* 4128 */       if (!this.m_service.m_hasLockedContent)
/*      */       {
/* 4130 */         this.m_service.createServiceException(null, "!csResubmitFailedNoLock");
/*      */       }
/* 4132 */       procState = this.m_binder.get("dProcessingState");
/* 4133 */       String always = this.m_binder.getAllowMissing("AlwaysResubmit");
/* 4134 */       boolean isAlways = StringUtils.convertToBool(always, false);
/* 4135 */       if ((!isAlways) && 
/* 4137 */         (!procState.equals("F")) && (!procState.equals("M")) && (!procState.equals("P")) && (!procState.equals("I")))
/*      */       {
/* 4140 */         this.m_service.createServiceException(null, "!csResubmitNotFailed");
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 4146 */     webViewableFile = getFilePath("webViewableFile");
/* 4147 */     if ((webViewableFile != null) && (webViewableFile.length() > 0))
/*      */     {
/* 4149 */       hasWebViewable = true;
/*      */     }
/*      */ 
/* 4154 */     if ((!hasWebViewable) && 
/* 4156 */       (query != null) && (query.length() > 0))
/*      */     {
/* 4158 */       ResultSet rset = this.m_workspace.createResultSet(query, this.m_binder);
/* 4159 */       if ((rset == null) || (!rset.isRowPresent()))
/*      */       {
/* 4161 */         throw new DataException("!csConversionSourceDocumentMissing");
/*      */       }
/* 4163 */       DataResultSet dset = new DataResultSet();
/* 4164 */       dset.copy(rset);
/*      */ 
/* 4168 */       int numrows = dset.getNumRows();
/* 4169 */       int webindex = -1;
/* 4170 */       int convertindex = -1;
/* 4171 */       for (int i = 0; i < numrows; ++i)
/*      */       {
/* 4173 */         dset.setCurrentRow(i);
/* 4174 */         String isWebDoc = ResultSetUtils.getValue(dset, "dIsWebFormat");
/* 4175 */         if (!isWebDoc.equals("0"))
/*      */         {
/* 4183 */           createNewDocId = false;
/* 4184 */           webindex = i;
/* 4185 */           String webDocID = ResultSetUtils.getValue(dset, "dDocID");
/* 4186 */           this.m_binder.putLocal("dDocID", webDocID);
/*      */         }
/*      */         else
/*      */         {
/* 4190 */           String isPrimary = ResultSetUtils.getValue(dset, "dIsPrimary");
/* 4191 */           if (isPrimary.equals("0"))
/*      */           {
/* 4193 */             convertindex = i;
/*      */           }
/*      */           else
/*      */           {
/* 4198 */             if (convertindex >= 0)
/*      */               continue;
/* 4200 */             convertindex = i;
/*      */           }
/*      */         }
/*      */       }
/*      */ 
/* 4205 */       if ((webindex < 0) && (isResubmit))
/*      */       {
/* 4207 */         String msg = LocaleUtils.encodeMessage("csWebVersionNotReserved", null, this.m_binder.get("dDocName"));
/*      */ 
/* 4209 */         throw new DataException(msg);
/*      */       }
/* 4211 */       if (convertindex < 0)
/*      */       {
/* 4213 */         String msg = LocaleUtils.encodeMessage("csVaultVersionNotReserved", null, this.m_binder.get("dDocName"));
/*      */ 
/* 4215 */         throw new DataException(msg);
/*      */       }
/*      */ 
/* 4218 */       dset.setCurrentRow(convertindex);
/* 4219 */       String rawDocID = ResultSetUtils.getValue(dset, "dDocID");
/* 4220 */       this.m_binder.putLocal("dRawDocID", rawDocID);
/*      */ 
/* 4222 */       String ext = ResultSetUtils.getValue(dset, "dExtension");
/* 4223 */       this.m_binder.putLocal("dExtension", ext);
/*      */ 
/* 4227 */       String dFormat = ResultSetUtils.getValue(dset, "dFormat");
/* 4228 */       this.m_binder.putLocal("dFormat", dFormat);
/*      */ 
/* 4230 */       this.m_binder.addResultSet(query, dset);
/*      */ 
/* 4233 */       determineDocConversion(isResubmit, null);
/*      */     }
/*      */ 
/* 4238 */     if (isResubmit)
/*      */     {
/* 4240 */       ResultSet rset = this.m_workspace.createResultSet("QisActiveRefineryJob", this.m_binder);
/* 4241 */       if (!rset.isEmpty())
/*      */       {
/* 4244 */         String msgStub = "csRefineryAlreadyTransfered";
/* 4245 */         String prov = rset.getStringValue(ResultSetUtils.getIndexMustExist(rset, "dConvProvider"));
/*      */ 
/* 4247 */         if ((prov == null) || (prov.length() == 0))
/*      */         {
/* 4249 */           msgStub = "csRefineryAlreadyQueued";
/* 4250 */           prov = "csOutQueueDescript";
/*      */         }
/* 4252 */         String errMsg = LocaleUtils.encodeMessage(msgStub, null, prov);
/* 4253 */         this.m_service.createServiceException(null, errMsg);
/*      */       }
/*      */     }
/*      */ 
/* 4257 */     WebViewableConverterOutput webData = createWebViewableOutput();
/* 4258 */     webData.init(this, this.m_service, isResubmit, createNewDocId, hasWebViewable, webViewableFile);
/* 4259 */     webData.doConversion();
/*      */ 
/* 4269 */     if (isResubmit)
/*      */     {
/* 4271 */       String docQuery = "Qdocuments";
/* 4272 */       ResultSet docListRset = this.m_workspace.createResultSet(docQuery, this.m_binder);
/* 4273 */       DataResultSet drDocListSet = new DataResultSet();
/* 4274 */       drDocListSet.copy(docListRset);
/* 4275 */       DataBinder curDocParams = new DataBinder(this.m_binder.getEnvironment());
/* 4276 */       String resultSetName = "DOC_INFO";
/* 4277 */       String docSetName = "DOC_LIST";
/* 4278 */       ResultSet rset = this.m_binder.getResultSet("DOC_INFO");
/*      */ 
/* 4280 */       if ((rset != null) && (!drDocListSet.isEmpty()))
/*      */       {
/* 4282 */         this.m_binder.addResultSet(docSetName, drDocListSet);
/*      */ 
/* 4297 */         String dExtension = this.m_binder.getActiveAllowMissing("dExtension");
/* 4298 */         this.m_binder.removeLocal("dExtension");
/*      */ 
/* 4300 */         curDocParams.addResultSet(resultSetName, rset);
/* 4301 */         Object[] moveCheckParams = { resultSetName, docSetName, rset, Character.valueOf('Y'), Boolean.valueOf(true) };
/* 4302 */         String[] listOfRenditions = { "primaryFile", "alternateFile" };
/* 4303 */         moveFilesInStore(curDocParams, moveCheckParams, false, "RELEASED", listOfRenditions);
/* 4304 */         if (dExtension != null)
/*      */         {
/* 4306 */           this.m_binder.putLocal("dExtension", dExtension);
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/* 4311 */     if (webData.m_webExtension != null)
/*      */     {
/* 4317 */       this.m_binder.putLocal("dExtension", webData.m_webExtension);
/*      */     }
/*      */ 
/* 4321 */     if (createNewDocId)
/*      */     {
/* 4324 */       this.m_binder.putLocal("dLocation", "");
/* 4325 */       String originalName = this.m_binder.getLocal("dOriginalName");
/* 4326 */       String webOriginalName = this.m_binder.getLocal("dWebOriginalName");
/* 4327 */       if (webOriginalName != null)
/*      */       {
/* 4329 */         this.m_binder.putLocal("dOriginalName", webOriginalName);
/*      */       }
/* 4331 */       this.m_workspace.execute("IwebDocument", this.m_binder);
/* 4332 */       this.m_binder.putLocal("dOriginalName", originalName);
/*      */     }
/*      */ 
/* 4336 */     this.m_binder.putLocal("dProcessingState", webData.m_processingState);
/* 4337 */     this.m_workspace.execute("UrevisionExtension", this.m_binder);
/* 4338 */     String newDocState = this.m_binder.getLocal("dStatus");
/*      */ 
/* 4341 */     boolean notifyIndexer = DataBinderUtils.getLocalBoolean(this.m_binder, "notifyIndexer", false);
/* 4342 */     if (newDocState.equals("DONE"))
/*      */     {
/* 4348 */       if (!createNewDocId)
/*      */       {
/* 4350 */         this.m_workspace.execute("Udocument", this.m_binder);
/*      */       }
/*      */ 
/* 4353 */       Date curDate = new Date();
/* 4354 */       curDate.setTime(curDate.getTime() + 5000L);
/* 4355 */       if (this.m_checkInDate == null)
/*      */       {
/* 4357 */         notifyIndexer = true;
/*      */       }
/* 4361 */       else if (curDate.after(this.m_checkInDate))
/*      */       {
/* 4363 */         notifyIndexer = true;
/*      */       }
/*      */     }
/*      */ 
/* 4367 */     if (notifyIndexer)
/*      */     {
/* 4369 */       notifyReleaseChange();
/*      */     }
/*      */ 
/* 4372 */     if (isResubmit) {
/*      */       return;
/*      */     }
/* 4375 */     this.m_binder.putLocal("dOriginalName", webData.m_originalName);
/* 4376 */     this.m_binder.putLocal("dFormat", webData.m_format);
/* 4377 */     this.m_binder.putLocal("dExtension", webData.m_extension);
/*      */   }
/*      */ 
/*      */   public WebViewableConverterOutput createWebViewableOutput()
/*      */     throws ServiceException
/*      */   {
/* 4383 */     WebViewableConverterOutput viewableOutput = null;
/*      */     try
/*      */     {
/* 4386 */       viewableOutput = (WebViewableConverterOutput)ComponentClassFactory.createClassInstance("WebViewableConverterOutput", "intradoc.server.WebViewableConverterOutput", "!csCreateWebViewableConverterError");
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/* 4391 */       this.m_service.createServiceException(e, null);
/*      */     }
/* 4393 */     return viewableOutput;
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void notifyReleaseChange()
/*      */   {
/* 4399 */     boolean isSuppressNotification = StringUtils.convertToBool(this.m_binder.getAllowMissing("isSuppressReleaseNotification"), false);
/*      */ 
/* 4401 */     if (!isSuppressNotification)
/*      */     {
/* 4403 */       SubjectManager.notifyChanged("indexerwork");
/*      */     }
/*      */     else
/*      */     {
/* 4407 */       this.m_binder.putLocal("releaseNotificationSuppressed", "1");
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void checkCriteriaWorkflow() throws DataException, ServiceException
/*      */   {
/* 4414 */     checkInitImplementor();
/* 4415 */     if (this.m_wfImplementor == null)
/*      */       return;
/* 4417 */     this.m_wfImplementor.checkCriteriaWorkflow();
/*      */ 
/* 4421 */     this.m_service.setConditionVar("CheckedForEnteringWorkflow", true);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void checkForPublish()
/*      */     throws DataException, ServiceException
/*      */   {
/* 4428 */     checkInitImplementor();
/* 4429 */     if (this.m_wfImplementor == null)
/*      */       return;
/* 4431 */     this.m_wfImplementor.checkPublishCriteriaWorkflow();
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void checkWorkflow()
/*      */     throws DataException, ServiceException
/*      */   {
/* 4438 */     this.m_isWf = false;
/* 4439 */     checkInitImplementor();
/* 4440 */     if (this.m_wfImplementor == null)
/*      */       return;
/* 4442 */     String wfInfoName = this.m_currentAction.getParamAt(0);
/* 4443 */     String action = this.m_currentAction.getParamAt(1);
/* 4444 */     String rsName = null;
/*      */ 
/* 4450 */     this.m_binder.putLocal("isCurRevEmpty", "");
/* 4451 */     this.m_service.setConditionVar("CheckedForEnteringWorkflow", false);
/*      */ 
/* 4453 */     if (this.m_currentAction.getNumParams() >= 3)
/*      */     {
/* 4457 */       rsName = this.m_currentAction.getParamAt(2);
/* 4458 */       if ((rsName != null) && (rsName.length() == 0))
/*      */       {
/* 4460 */         rsName = null;
/*      */       }
/* 4462 */       if (rsName != null)
/*      */       {
/* 4470 */         this.m_binder.putLocal("SecurityProfileResultSet", rsName);
/*      */ 
/* 4476 */         if (action.indexOf("checkin") < 0)
/*      */         {
/* 4478 */           this.m_wfImplementor.loadWorkflowStateInfo(rsName);
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/* 4483 */     String isWorkflowStr = this.m_binder.getLocal("IsWorkflow");
/* 4484 */     if (isWorkflowStr != null)
/*      */     {
/* 4487 */       this.m_binder.putLocal("IsWorkflow", "");
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/* 4493 */       this.m_wfImplementor.checkWorkflow(wfInfoName, action);
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/* 4497 */       this.m_binder.putLocal("StatusReason", new StringBuilder().append("csCheckWorkflowFailed:").append(this.m_binder.get("dDocName")).toString());
/* 4498 */       throw e;
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/* 4502 */       this.m_binder.putLocal("StatusReason", new StringBuilder().append("csCheckWorkflowFailed:").append(this.m_binder.get("dDocName")).toString());
/* 4503 */       throw e;
/*      */     }
/* 4505 */     this.m_isWf = StringUtils.convertToBool(this.m_binder.getLocal("IsWorkflow"), false);
/* 4506 */     if (rsName != null)
/*      */     {
/* 4508 */       this.m_binder.removeLocal("SecurityProfileResultSet");
/*      */     }
/* 4510 */     if ((!this.m_isWf) && (action.equals("loadInfoCheckIsWorkflow")))
/*      */     {
/* 4512 */       this.m_binder.putLocal("StatusReason", "!csWfNotInWorkflow");
/* 4513 */       this.m_service.createServiceException(null, "!csWfNotInWorkflow");
/*      */     }
/*      */ 
/* 4519 */     this.m_service.setCachedObject("WorkflowDocImplementor", this.m_wfImplementor);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void loadAlreadyComputedCheckWorkflow()
/*      */   {
/* 4529 */     this.m_wfImplementor = ((WorkflowDocImplementor)this.m_service.getCachedObject("WorkflowDocImplementor"));
/* 4530 */     this.m_isWf = DataBinderUtils.getLocalBoolean(this.m_binder, "IsWorkflow", false);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void computeWorkflowCheckinType()
/*      */     throws DataException, ServiceException
/*      */   {
/* 4539 */     this.m_binder.putLocal("IsUpdate", "");
/* 4540 */     if ((!this.m_isWf) || (this.m_wfImplementor == null))
/*      */       return;
/* 4542 */     this.m_wfImplementor.computeWorkflowCheckinType();
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void determineWorkflowCheckin()
/*      */     throws DataException, ServiceException
/*      */   {
/* 4550 */     if (!this.m_isWf)
/*      */     {
/* 4552 */       this.m_service.createServiceException(null, "!csWfNotInWorkflow");
/*      */     }
/* 4554 */     String docRsetName = this.m_currentAction.getParamAt(0);
/* 4555 */     String workflowService = this.m_wfImplementor.determineWorkflowCheckin(docRsetName);
/* 4556 */     if (!DataBinderUtils.getLocalBoolean(this.m_binder, "IsUpdate", false))
/*      */     {
/* 4559 */       this.m_binder.removeResultSet(docRsetName);
/*      */     }
/* 4561 */     this.m_service.executeService(workflowService);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void updateWorkflowStateAfterCheckin()
/*      */     throws DataException, ServiceException
/*      */   {
/* 4572 */     doUnlockCheck(0, null);
/*      */ 
/* 4574 */     updateWorkflowStateAfterCheckinImplement(1);
/*      */   }
/*      */ 
/*      */   public void updateWorkflowStateAfterCheckinImplement(int flags)
/*      */     throws DataException, ServiceException
/*      */   {
/* 4582 */     if (!this.m_service.executeFilter("updateWorkflowStateAfterChange"))
/*      */     {
/* 4584 */       return;
/*      */     }
/* 4586 */     if (this.m_wfImplementor == null)
/*      */     {
/* 4588 */       return;
/*      */     }
/* 4590 */     this.m_wfImplementor.updateWorkflowStateAfterCheckin(flags);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void computeWfDocumentsInfo()
/*      */     throws DataException, ServiceException
/*      */   {
/* 4597 */     checkInitImplementor();
/* 4598 */     if ((!this.m_isWf) || (this.m_wfImplementor == null))
/*      */       return;
/* 4600 */     this.m_wfImplementor.computeWfDocumentsInfo(this.m_currentAction.getParamAt(0));
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void getWorkflowInfo()
/*      */     throws DataException, ServiceException
/*      */   {
/* 4611 */     checkInitImplementor();
/* 4612 */     if (this.m_wfImplementor == null)
/*      */     {
/*      */       return;
/*      */     }
/*      */ 
/* 4618 */     this.m_wfImplementor.getAndLoadWorkflowInfoWithFlags(this.m_currentAction.getParamAt(0), 1);
/*      */ 
/* 4620 */     getWorkflowProjectInfo();
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void getWorkflowProjectInfo()
/*      */     throws DataException, ServiceException
/*      */   {
/* 4635 */     this.m_binder.setFieldType("ConnectionState", "message");
/* 4636 */     this.m_binder.setFieldType("LastConnectionErrorMsg", "message");
/* 4637 */     if (this.m_wfImplementor == null)
/*      */       return;
/* 4639 */     this.m_wfImplementor.getProjectInfo();
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void doWorkflowAction()
/*      */     throws DataException, ServiceException
/*      */   {
/* 4650 */     if (this.m_wfImplementor == null)
/*      */     {
/* 4652 */       throw new DataException("!csWfImplementorNotLoaded");
/*      */     }
/* 4654 */     if (this.m_wfImplementor != null)
/*      */     {
/* 4656 */       this.m_wfImplementor.doWorkflowAction(this.m_currentAction.getParamAt(0));
/*      */     }
/*      */ 
/* 4666 */     deleteFilesInDeleteList();
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void deleteFilesInDeleteList()
/*      */     throws DataException, ServiceException
/*      */   {
/* 4680 */     this.m_service.setCachedObject("filesToDelete", this.m_filesToDelete);
/* 4681 */     this.m_service.setCachedObject("lockedIDsList", this.m_lockedIDsList);
/* 4682 */     if (PluginFilters.filter("preDeleteFilesInDeleteList", this.m_workspace, this.m_binder, this.m_service) != 0)
/*      */     {
/* 4685 */       return;
/*      */     }
/*      */ 
/* 4690 */     boolean isUpdate = DataBinderUtils.getLocalBoolean(this.m_binder, "IsUpdate", false);
/* 4691 */     Map map = createDeleteFileArguments(isUpdate);
/* 4692 */     Set keySet = this.m_filesToDelete.keySet();
/* 4693 */     for (Iterator keys = keySet.iterator(); keys.hasNext(); )
/*      */     {
/* 4695 */       String key = (String)keys.next();
/* 4696 */       IdcFileDescriptor source = (IdcFileDescriptor)this.m_filesToDelete.get(key);
/*      */ 
/* 4699 */       boolean maybeBeingProducedByConversionQueue = this.m_lockedIDsList.get(key) != null;
/* 4700 */       map.put("isLocked", new StringBuilder().append("").append(maybeBeingProducedByConversionQueue).toString());
/* 4701 */       this.m_service.m_fileUtils.deleteFile(source, map, this.m_service);
/*      */ 
/* 4703 */       if ((maybeBeingProducedByConversionQueue) && (isUpdate))
/*      */       {
/* 4705 */         Properties docProps = new Properties();
/* 4706 */         QueueProcessor.removeDocFromConversionQueue(this.m_workspace, key, docProps);
/*      */       }
/*      */     }
/* 4709 */     this.m_filesToDelete.clear();
/* 4710 */     this.m_lockedIDsList.clear();
/*      */   }
/*      */ 
/*      */   public Map createDeleteFileArguments(boolean isUpdate)
/*      */   {
/* 4720 */     int numRetries = 0;
/* 4721 */     if (isUpdate)
/*      */     {
/* 4723 */       numRetries = SharedObjects.getEnvironmentInt("DeleteNativeFileRetries", 4);
/*      */     }
/* 4725 */     HashMap map = new HashMap();
/* 4726 */     map.put("numRetries", new StringBuilder().append("").append(numRetries).toString());
/* 4727 */     map.put("isAllowFailure", new StringBuilder().append("").append(!isUpdate).toString());
/*      */ 
/* 4731 */     map.put("ErrorOnReadOnly", new StringBuilder().append("").append(SharedObjects.getEnvironmentInt("ErrorOnReadOnly", 1)).toString());
/* 4732 */     return map;
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void determineAdvanceDocumentStateFunction()
/*      */     throws DataException, ServiceException
/*      */   {
/* 4744 */     Vector oldParams = this.m_currentAction.m_params;
/*      */ 
/* 4746 */     Vector newParams = new IdcVector();
/*      */ 
/* 4748 */     this.m_binder.putLocal("isFinished", "0");
/*      */ 
/* 4750 */     String serviceAction = "undoCheckout";
/* 4751 */     String function = "advanceDocumentStateSimple";
/*      */ 
/* 4754 */     this.m_service.setConditionVar("checkWebdavUndoCheckout", false);
/* 4755 */     if (this.m_currentAction.getNumParams() > 1)
/*      */     {
/* 4757 */       serviceAction = this.m_currentAction.getParamAt(0);
/* 4758 */       function = this.m_currentAction.getParamAt(1);
/*      */     }
/* 4760 */     if (serviceAction.equals("undoCheckout"))
/*      */     {
/* 4764 */       boolean isEmptyRev = this.m_service.isConditionVarTrue("IsEmptyRev");
/* 4765 */       ResultSet rset = this.m_workspace.createResultSet("QdocWebFormat", this.m_binder);
/* 4766 */       this.m_binder.putLocal("noDocLock", "1");
/* 4767 */       String docName = this.m_binder.getAllowMissing("dDocName");
/*      */ 
/* 4773 */       String isWebdavLocked = this.m_binder.getEnvironmentValue("IsLocked");
/* 4774 */       if (StringUtils.convertToBool(isWebdavLocked, false))
/*      */       {
/* 4776 */         Report.trace("documentlock", new StringBuilder().append("Converting webdav IsLocked state to a IsUnlock condition variable - ").append(docName).toString(), null);
/*      */ 
/* 4779 */         this.m_service.setConditionVar("IsUnlock", true);
/*      */       }
/*      */ 
/* 4782 */       if ((!isEmptyRev) && (rset.isEmpty()) && (!DocumentStateLockUtils.mustWaitRealUnlock(this.m_binder, this.m_service)))
/*      */       {
/* 4786 */         function = "docRefinery";
/* 4787 */         Report.trace("documentlock", new StringBuilder().append("Doing webviewable production on undo check-out - ").append(docName).toString(), null);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 4792 */     if (function.equals("docRefinery"))
/*      */     {
/* 4794 */       newParams.insertElementAt("Qdocuments", 0);
/*      */     }
/* 4796 */     else if ((function.equals("advanceDocumentState")) && 
/* 4799 */       (DocumentStateLockUtils.isDoingUnlock(this.m_binder, this.m_service)))
/*      */     {
/* 4801 */       this.m_service.doCode("checkIsFinishedFromUnlock");
/*      */     }
/*      */ 
/* 4804 */     this.m_currentAction.m_params = newParams;
/* 4805 */     this.m_service.doCode(function);
/* 4806 */     this.m_currentAction.m_params = oldParams;
/*      */   }
/*      */ 
/*      */   public void doUnlockCheck(int flags, boolean[] isLockedReturn)
/*      */     throws DataException, ServiceException
/*      */   {
/* 4812 */     boolean setConditionVars = false;
/* 4813 */     boolean isFinalCheck = (flags & 0x1) != 0;
/* 4814 */     boolean isLocked = false;
/* 4815 */     String executionKey = "checkIsFinishedFromUnlock:isExecuted";
/* 4816 */     String lockedKey = "checkIsFinishedFromUnlock:isLocked";
/* 4817 */     boolean alreadyDidIt = this.m_service.isConditionVarTrue(executionKey);
/* 4818 */     if (alreadyDidIt)
/*      */     {
/* 4820 */       isLocked = this.m_service.isConditionVarTrue(lockedKey);
/* 4821 */       setConditionVars = isFinalCheck;
/*      */     }
/*      */     else
/*      */     {
/* 4825 */       if ((!DataBinderUtils.getLocalBoolean(this.m_binder, "noDocLock", false)) && 
/* 4827 */         (DocumentStateLockUtils.isLockedDocument(this.m_binder, this.m_service)))
/*      */       {
/* 4829 */         isLocked = true;
/*      */       }
/*      */ 
/* 4832 */       if ((!isLocked) && (((DocumentStateLockUtils.isDoingUnlock(this.m_binder, this.m_service)) || (DocumentStateLockUtils.isSimplifiedCheckin(this.m_binder, this.m_service)))))
/*      */       {
/* 4835 */         this.m_service.doCode("checkIsFinishedFromUnlock");
/*      */       }
/* 4837 */       if (isFinalCheck)
/*      */       {
/* 4839 */         String workflowState = this.m_binder.getAllowMissing("dWorkflowState");
/* 4840 */         String requestMethod = this.m_binder.getEnvironmentValue("REQUEST_METHOD");
/* 4841 */         if ((workflowState != null) && (workflowState.length() > 0) && (!workflowState.equals("E")) && (requestMethod != null) && (requestMethod.equals("UNLOCK")) && (DataBinderUtils.getLocalBoolean(this.m_binder, "isFinished", false)))
/*      */         {
/* 4848 */           checkInitImplementor();
/*      */ 
/* 4850 */           updateWorkflowStateAfterCheckinImplement(2);
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/* 4855 */         setConditionVars = true;
/*      */       }
/*      */     }
/* 4858 */     if (setConditionVars)
/*      */     {
/* 4862 */       this.m_service.setConditionVar(executionKey, !isFinalCheck);
/* 4863 */       this.m_service.setConditionVar(lockedKey, (isLocked) && (!isFinalCheck));
/*      */     }
/* 4865 */     if ((!isLocked) || (isLockedReturn == null) || (isLockedReturn.length <= 0))
/*      */       return;
/* 4867 */     isLockedReturn[0] = isLocked;
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void checkIsFinishedFromUnlock()
/*      */     throws DataException, ServiceException
/*      */   {
/* 4880 */     boolean isFinished = false;
/* 4881 */     String workflowState = this.m_binder.get("dWorkflowState");
/* 4882 */     if ((workflowState != null) && (workflowState.trim().length() > 0))
/*      */     {
/* 4884 */       if ((workflowState.equals("E")) && (((SharedObjects.getEnvValueAsBoolean("AutoContributorAdvancesOnUnlock", false)) || (this.m_service.isConditionVarTrue("AutoContributorAdvancesOnUnlock")))))
/*      */       {
/* 4887 */         Report.trace("documentlock", "Setting isFinished to true for auto contributor step because of unlock", null);
/*      */ 
/* 4889 */         isFinished = true;
/*      */       }
/* 4891 */       else if ((SharedObjects.getEnvValueAsBoolean("WorkflowAdvancesOnUnlock", false)) || (this.m_service.isConditionVarTrue("WorkflowAdvancesOnUnlock")))
/*      */       {
/* 4894 */         Report.trace("documentlock", "Setting isFinished to true for workflow step because of unlock", null);
/*      */ 
/* 4896 */         isFinished = true;
/*      */       }
/*      */     }
/* 4899 */     if (!isFinished)
/*      */       return;
/* 4901 */     this.m_binder.putLocal("isFinished", "1");
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void advanceDocumentState()
/*      */     throws DataException, ServiceException
/*      */   {
/* 4912 */     if (this.m_service.isConditionVarTrue("checkWebdavUndoCheckout"))
/*      */     {
/* 4915 */       this.m_service.doCode("determineAdvanceDocumentStateFunction");
/* 4916 */       return;
/*      */     }
/*      */ 
/* 4919 */     this.m_binder.putLocal("isFinished", "0");
/* 4920 */     advanceDocumentStateSimple();
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void advanceDocumentStateSimple()
/*      */     throws DataException, ServiceException
/*      */   {
/* 4932 */     boolean isNotLatestRev = DataBinderUtils.getLocalBoolean(this.m_binder, "IsNotLatestRev", false);
/* 4933 */     String oldID = null;
/* 4934 */     if ((!isNotLatestRev) && (this.m_service.isConditionVarTrue("HasOriginal")))
/*      */     {
/* 4938 */       String activeID = this.m_binder.getLocal("dID");
/* 4939 */       String latestID = this.m_binder.getLocal("latestID");
/* 4940 */       if (!activeID.equals(latestID))
/*      */       {
/* 4942 */         this.m_binder.putLocal("dID", latestID);
/* 4943 */         oldID = activeID;
/*      */       }
/*      */     }
/* 4946 */     DocStateTransition.advanceDocumentState(this.m_binder, this.m_workspace, isNotLatestRev, false, false, true, this.m_service);
/*      */ 
/* 4948 */     DocStateTransition.updateStateAfterDocumentAdvance(this.m_binder, this.m_workspace);
/* 4949 */     if (oldID == null)
/*      */       return;
/* 4951 */     this.m_binder.putLocal("dID", oldID);
/*      */   }
/*      */ 
/*      */   protected void addRenditions()
/*      */     throws DataException, ServiceException
/*      */   {
/* 4959 */     AdditionalRenditions renSet = (AdditionalRenditions)SharedObjects.getTable("AdditionalRenditions");
/*      */ 
/* 4962 */     int count = 1;
/* 4963 */     for (int i = 0; i < AdditionalRenditions.m_maxNum; ++count)
/*      */     {
/* 4965 */       String renFlag = this.m_binder.getLocal(new StringBuilder().append("dRendition").append(count).toString());
/* 4966 */       if (renFlag != null) if (renFlag.trim().length() != 0)
/*      */         {
/* 4971 */           String rext = renSet.getExtension(renFlag);
/*      */ 
/* 4973 */           String errMsg = null;
/* 4974 */           String renPath = this.m_binder.getLocal(new StringBuilder().append("dRendition").append(count).append(":path").toString());
/* 4975 */           if ((renPath == null) || (renPath.trim().length() == 0))
/*      */           {
/* 4977 */             errMsg = LocaleUtils.encodeMessage("csRenditionPathNotDefined", null, new StringBuilder().append("").append(count).toString());
/*      */           }
/*      */           else
/*      */           {
/* 4983 */             int index = renPath.lastIndexOf(".");
/* 4984 */             if (index >= 0)
/*      */             {
/* 4986 */               String ext = renPath.substring(index + 1);
/* 4987 */               if (!ext.equalsIgnoreCase(rext))
/*      */               {
/* 4989 */                 errMsg = LocaleUtils.encodeMessage("csRenditionExtensionMismatch", null, ext, rext);
/*      */               }
/*      */ 
/*      */             }
/*      */             else
/*      */             {
/* 4995 */               errMsg = "!csRenditionExtensionMissing";
/*      */             }
/*      */           }
/*      */ 
/* 4999 */           if (errMsg != null)
/*      */           {
/* 5001 */             throw new DataException(errMsg);
/*      */           }
/*      */ 
/* 5004 */           this.m_binder.putLocal("RenditionId", new StringBuilder().append("rendition:").append(renFlag).toString());
/* 5005 */           IdcFileDescriptor descriptor = this.m_fileStore.createDescriptor(this.m_binder, null, this.m_service);
/*      */           try
/*      */           {
/* 5008 */             Map args = new HashMap();
/* 5009 */             args.put("copyFiles", new StringBuilder().append("").append(this.m_copyFiles).toString());
/* 5010 */             args.put("isSplitDir", new StringBuilder().append("").append(this.m_isSplitDir).toString());
/* 5011 */             args.put("isNew", "1");
/* 5012 */             this.m_fileStore.storeFromLocalFile(descriptor, new File(renPath), args, this.m_service);
/* 5013 */             if (!this.m_copyFiles)
/*      */             {
/* 5015 */               this.m_binder.addTempFile(renPath);
/*      */             }
/* 5017 */             this.m_workspace.execute(new StringBuilder().append("Urendition").append(count).toString(), this.m_binder);
/*      */           }
/*      */           catch (IOException e)
/*      */           {
/* 5021 */             throw new ServiceException(errMsg, e);
/*      */           }
/*      */         }
/* 4963 */       ++i;
/*      */     }
/*      */   }
/*      */ 
/*      */   protected List moveRenditions(ResultSet rset, DataBinder curDocParams)
/*      */     throws DataException, ServiceException, IOException
/*      */   {
/* 5029 */     List list = new ArrayList();
/* 5030 */     Object[] o = { rset, curDocParams };
/* 5031 */     this.m_service.setCachedObject("docHandlerMoveRenditionsParams", o);
/* 5032 */     int ret = PluginFilters.filter("docHandlerMoveRenditions", this.m_workspace, this.m_binder, this.m_service);
/* 5033 */     if (ret == -1)
/*      */     {
/* 5035 */       return list;
/*      */     }
/*      */ 
/* 5039 */     int count = 1;
/* 5040 */     for (int i = 0; i < AdditionalRenditions.m_maxNum; ++count)
/*      */     {
/* 5042 */       String renFlag = ResultSetUtils.getValue(rset, new StringBuilder().append("dRendition").append(count).toString());
/* 5043 */       if (renFlag != null) if (renFlag.trim().length() != 0)
/*      */         {
/* 5048 */           IdcFileDescriptor[] packet = createDescriptorsForMoveStoreFile(curDocParams, this.m_binder, null, null, new StringBuilder().append("rendition:").append(renFlag).toString());
/*      */ 
/* 5050 */           list.add(packet);
/*      */         }
/* 5040 */       ++i;
/*      */     }
/*      */ 
/* 5052 */     return list;
/*      */   }
/*      */ 
/*      */   protected boolean deleteRenditions(String docName, String revLabel, String docID, Map filesToDelete, Parameters params)
/*      */     throws DataException, ServiceException
/*      */   {
/* 5059 */     Object[] o = { docName, revLabel, docID, filesToDelete };
/* 5060 */     this.m_service.setCachedObject("docHandlerDeleteRenditionsParams", o);
/* 5061 */     boolean[] retVal = { false };
/* 5062 */     this.m_service.setReturnValue(retVal);
/*      */     try
/*      */     {
/* 5065 */       int ret = PluginFilters.filter("docHandlerDeleteRenditions", this.m_workspace, this.m_binder, this.m_service);
/* 5066 */       if (ret == -1)
/*      */       {
/* 5068 */         return retVal[0];
/*      */       }
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/* 5073 */       throw new DataException(e.getMessage(), e);
/*      */     }
/* 5075 */     filesToDelete = (Map)o[3];
/*      */ 
/* 5077 */     int count = 1;
/* 5078 */     boolean renditionPresent = false;
/* 5079 */     for (int i = 0; i < AdditionalRenditions.m_maxNum; ++count)
/*      */     {
/* 5081 */       String renFlag = this.m_binder.getActiveValueSearchAll(new StringBuilder().append("dRendition").append(count).toString());
/* 5082 */       if ((renFlag != null) && (renFlag.trim().length() != 0)) if (!renFlag.trim().equalsIgnoreCase("D"))
/*      */         {
/* 5090 */           renditionPresent = true;
/*      */ 
/* 5092 */           Map map = DataBinderUtils.getInternalMap(params);
/* 5093 */           map.put("RenditionId", new StringBuilder().append("rendition:").append(renFlag).toString());
/* 5094 */           IdcFileDescriptor source = this.m_fileStore.createDescriptor(params, null, this.m_service);
/* 5095 */           if (source != null)
/*      */           {
/* 5097 */             filesToDelete.put(new StringBuilder().append(count).append("r_").append(docID).toString(), source);
/*      */           }
/*      */         }
/* 5079 */       ++i;
/*      */     }
/*      */ 
/* 5100 */     return renditionPresent;
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void doMetaFileValidation()
/*      */     throws DataException, ServiceException
/*      */   {
/* 5111 */     if (DataBinderUtils.getBoolean(this.m_binder, "isWebFormEdit", false))
/*      */     {
/* 5113 */       createWebForm();
/* 5114 */       return;
/*      */     }
/*      */ 
/* 5117 */     ResultSet docInfoRset = this.m_binder.getResultSet("DOC_INFO");
/* 5118 */     String primaryMeta = this.m_binder.getLocal("createPrimaryMetaFile");
/* 5119 */     String alternateMeta = this.m_binder.getLocal("createAlternateMetaFile");
/* 5120 */     boolean isRev = StringUtils.convertToBool(this.m_binder.getLocal("isRev"), false);
/* 5121 */     boolean createPrimaryMeta = StringUtils.convertToBool(primaryMeta, false);
/* 5122 */     boolean createAlternateMeta = StringUtils.convertToBool(alternateMeta, false);
/*      */ 
/* 5127 */     String filePath = this.m_binder.getLocal("primaryFile:path");
/* 5128 */     String format = this.m_binder.getLocal("dFormat");
/* 5129 */     if ((((filePath == null) || (filePath.equals("")))) && (format != null) && (format.indexOf("idcmeta") > -1))
/*      */     {
/* 5132 */       createPrimaryMeta = true;
/*      */     }
/*      */ 
/* 5137 */     if ((docInfoRset != null) && (docInfoRset.first()) && (isRev != true))
/*      */     {
/* 5139 */       String curNativeFormat = ResultSetUtils.getValue(docInfoRset, "dFormat");
/* 5140 */       if ((curNativeFormat == null) || (curNativeFormat.indexOf("idcmeta") != 0))
/*      */       {
/* 5142 */         if ((createPrimaryMeta) || (createAlternateMeta))
/*      */         {
/* 5144 */           this.m_service.createServiceException(null, "!csMetaFilePrimaryAlreadyExists");
/*      */         }
/* 5146 */         return;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 5151 */     String isFileUpdate = this.m_binder.getLocal("IsUpdateMetaOnly");
/* 5152 */     if ((StringUtils.convertToBool(isFileUpdate, false)) && (docInfoRset != null) && (docInfoRset.isRowPresent()))
/*      */     {
/* 5156 */       this.m_binder.putLocal("dStatus", "GENWWW");
/* 5157 */       String state = this.m_binder.get("dReleaseState");
/* 5158 */       this.m_binder.putLocal("prevReleaseState", state);
/* 5159 */       if ((state.equals("O")) || (state.equals("N")))
/*      */       {
/* 5161 */         state = "N";
/*      */       }
/* 5163 */       else if (!state.equals("E"))
/*      */       {
/* 5165 */         state = "U";
/*      */       }
/* 5167 */       this.m_binder.putLocal("dReleaseState", state);
/* 5168 */       this.m_binder.putLocal("IsUpdateMetaOnly", "0");
/*      */ 
/* 5171 */       if (createPrimaryMeta)
/*      */       {
/* 5173 */         this.m_service.setConditionVar("KeepOldAdditionalRenditions", true);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 5178 */     if (createPrimaryMeta)
/*      */     {
/* 5180 */       if (createAlternateMeta)
/*      */       {
/* 5182 */         String msg = LocaleUtils.encodeMessage("csCannotHaveTwoMetaFiles", null);
/* 5183 */         this.m_service.createServiceException(null, msg);
/*      */       }
/* 5185 */       createMetaFile("primaryFile", "MetaFileTemplate");
/* 5186 */       this.m_binder.putLocal("ValidatePrimaryFile", "false");
/* 5187 */       this.m_binder.putLocal("primaryOverrideFormat", "idcmeta/html");
/*      */     } else {
/* 5189 */       if (!createAlternateMeta)
/*      */         return;
/* 5191 */       createMetaFile("alternateFile", "PreviewFileTemplate");
/* 5192 */       this.m_binder.putLocal("ValidateAlternateFile", "false");
/* 5193 */       this.m_binder.putLocal("alternateOverrideFormat", "idcmeta/html");
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void createMetaFile(String fileType, String templateNameVariable)
/*      */     throws ServiceException
/*      */   {
/* 5204 */     String tempDir = DataBinder.getTemporaryDirectory();
/*      */ 
/* 5206 */     Writer writer = null;
/*      */     try
/*      */     {
/* 5209 */       this.m_binder.putLocal(new StringBuilder().append(fileType).append(":format").toString(), "idcmeta/html");
/* 5210 */       String docName = this.m_binder.getLocal("dDocName");
/* 5211 */       String counter = this.m_binder.getLocal("metaFileCounter");
/* 5212 */       String name = docName.toLowerCase();
/*      */ 
/* 5216 */       if (counter != null)
/*      */       {
/* 5218 */         name = new StringBuilder().append(name).append("_").append(counter).toString();
/*      */       }
/* 5220 */       name = new StringBuilder().append(name).append("_meta.htm").toString();
/* 5221 */       if (counter == null)
/*      */       {
/* 5223 */         counter = "1";
/*      */       }
/*      */       else
/*      */       {
/* 5227 */         int counterVal = NumberUtils.parseInteger(counter, 0);
/* 5228 */         counter = Integer.toString(counterVal + 1);
/*      */       }
/* 5230 */       this.m_binder.putLocal("metaFileCounter", counter);
/*      */ 
/* 5232 */       String fullName = new StringBuilder().append(tempDir).append(name).toString();
/* 5233 */       this.m_binder.putLocal(new StringBuilder().append(fileType).append(":path").toString(), fullName);
/* 5234 */       this.m_binder.putLocal(fileType, name);
/* 5235 */       this.m_binder.addTempFile(fullName);
/* 5236 */       this.m_binder.putLocal("CreateMetaFile", "true");
/*      */ 
/* 5240 */       if (templateNameVariable == null)
/*      */       {
/* 5242 */         templateNameVariable = "MetaFileTemplate";
/*      */       }
/* 5244 */       String template = this.m_binder.getLocal(templateNameVariable);
/* 5245 */       if ((template == null) || (template.length() == 0))
/*      */       {
/* 5247 */         template = "META_FILE_TEMPLATE";
/*      */       }
/* 5249 */       String dhtml = this.m_service.createMergedPage(template);
/*      */ 
/* 5252 */       writer = FileUtils.openDataWriter(new File(fullName), FileUtils.m_javaSystemEncoding);
/*      */ 
/* 5254 */       writer.write(dhtml);
/* 5255 */       writer.close();
/* 5256 */       writer = null;
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/* 5260 */       throw new ServiceException("!csMetaFileUnableToCreate", e);
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void createWebForm()
/*      */     throws DataException, ServiceException
/*      */   {
/* 5270 */     String contents = this.m_binder.get("fileContents");
/* 5271 */     if (contents.length() == 0)
/*      */     {
/* 5273 */       throw new DataException("!csFeContentMissing");
/*      */     }
/*      */ 
/* 5276 */     String tempDir = DataBinder.getTemporaryDirectory();
/* 5277 */     Writer writer = null;
/*      */     try
/*      */     {
/* 5280 */       this.m_binder.putLocal("primaryFile:format", "form/hcsw");
/* 5281 */       String docName = this.m_binder.getLocal("dDocName");
/* 5282 */       if (docName == null)
/*      */       {
/* 5284 */         docName = "preview";
/*      */       }
/* 5286 */       String name = new StringBuilder().append(docName.toLowerCase()).append(".hcsw").toString();
/* 5287 */       String fullName = new StringBuilder().append(tempDir).append(name).toString();
/*      */ 
/* 5289 */       this.m_binder.putLocal("primaryFile:path", fullName);
/* 5290 */       this.m_binder.putLocal("primaryFile", name);
/* 5291 */       this.m_binder.addTempFile(fullName);
/*      */ 
/* 5294 */       writer = FileUtils.openDataWriter(new File(fullName), FileUtils.m_javaSystemEncoding);
/*      */ 
/* 5296 */       writer.write(contents);
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/*      */     }
/*      */     finally
/*      */     {
/* 5304 */       FileUtils.closeObject(writer);
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void doSubserviceIfMetafile()
/*      */     throws ServiceException, DataException
/*      */   {
/* 5318 */     ResultSet rset = this.m_binder.getResultSet("DOC_INFO");
/* 5319 */     if (rset == null)
/*      */     {
/* 5321 */       return;
/*      */     }
/*      */ 
/* 5324 */     String subService = getSubServiceForUpdateDocInfo();
/*      */ 
/* 5326 */     if (subService == null)
/*      */       return;
/* 5328 */     this.m_service.executeService(subService);
/*      */   }
/*      */ 
/*      */   public String getSubServiceForUpdateDocInfo()
/*      */     throws ServiceException, DataException
/*      */   {
/* 5334 */     String subService = null;
/*      */ 
/* 5338 */     int filterReturn = PluginFilters.filter("getSubServiceForUpdateDocInfo", this.m_workspace, this.m_binder, this.m_service);
/* 5339 */     if (filterReturn != -1)
/*      */     {
/* 5341 */       subService = (String)this.m_service.getCachedObject("subServiceFromFilter");
/*      */ 
/* 5343 */       this.m_service.setCachedObject("subServiceFromFilter", null);
/*      */     }
/*      */ 
/* 5346 */     if (subService == null)
/*      */     {
/* 5348 */       ResultSet rset = this.m_binder.getResultSet("DOC_INFO");
/*      */ 
/* 5350 */       if (rset != null)
/*      */       {
/* 5352 */         String format = ResultSetUtils.getValue(rset, "dFormat");
/* 5353 */         if ((format != null) && (format.startsWith("idcmeta")))
/*      */         {
/* 5355 */           subService = this.m_currentAction.getParamAt(0);
/*      */         }
/* 5359 */         else if (this.m_currentAction.getNumParams() > 1)
/*      */         {
/* 5361 */           subService = this.m_currentAction.getParamAt(1);
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 5367 */     return subService;
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void checkAssociateFileSideEffectOnly()
/*      */     throws DataException, ServiceException
/*      */   {
/* 5376 */     Properties prevProps = this.m_binder.getLocalData();
/* 5377 */     Properties wrapProps = new Properties(prevProps);
/* 5378 */     this.m_binder.setLocalData(wrapProps);
/* 5379 */     Map prevResultSets = this.m_binder.getResultSets();
/* 5380 */     Map newResultSet = this.m_binder.cloneMap(prevResultSets);
/* 5381 */     this.m_binder.setResultSets(newResultSet);
/*      */     try
/*      */     {
/* 5384 */       checkForServiceActionAssociatedFile(true);
/*      */     }
/*      */     finally
/*      */     {
/* 5388 */       this.m_binder.setLocalData(prevProps);
/* 5389 */       this.m_binder.setResultSets(prevResultSets);
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void checkAssociatedFile()
/*      */     throws DataException, ServiceException
/*      */   {
/* 5401 */     checkForServiceActionAssociatedFile(false);
/*      */   }
/*      */ 
/*      */   protected void checkForServiceActionAssociatedFile(boolean serviceOnlyIfExists)
/*      */     throws DataException, ServiceException
/*      */   {
/* 5407 */     String inc = this.m_currentAction.getParamAt(0);
/*      */     try
/*      */     {
/* 5410 */       this.m_service.m_pageMerger.evaluateResourceInclude(inc);
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/* 5414 */       throw new DataException(e.getMessage(), e);
/*      */     }
/*      */ 
/* 5417 */     String skip = this.m_binder.getLocal("skipAssociatedFileCheck");
/* 5418 */     if ((skip != null) && (skip.length() > 0))
/*      */     {
/* 5420 */       this.m_binder.removeLocal("skipAssociatedFileCheck");
/* 5421 */       String statusCode = this.m_binder.getLocal("StatusCode");
/* 5422 */       if ((statusCode != null) && (statusCode.length() > 0))
/*      */       {
/* 5424 */         int code = NumberUtils.parseInteger(statusCode, -1);
/* 5425 */         this.m_service.createServiceExceptionEx(null, this.m_binder.getLocal("StatusMessage"), code);
/*      */       }
/* 5427 */       return;
/*      */     }
/*      */ 
/* 5430 */     String docName = this.m_binder.get("dDocName");
/* 5431 */     int l = SharedObjects.getEnvironmentInt("dDocName:maxLength", 30);
/* 5432 */     if (docName.length() > l)
/*      */     {
/* 5434 */       String msg = LocaleUtils.encodeMessage("csAssociatedDocumentNameIsTooLong", null, docName);
/* 5435 */       this.m_service.createServiceException(null, msg);
/*      */     }
/*      */ 
/* 5438 */     ResultSet rset = this.m_service.m_workspace.createResultSet("QdocName", this.m_binder);
/* 5439 */     if (rset.isEmpty())
/*      */     {
/* 5441 */       if (serviceOnlyIfExists)
/*      */         return;
/* 5443 */       String newDocService = this.m_currentAction.getParamAt(1);
/* 5444 */       this.m_service.executeService(newDocService);
/*      */     }
/*      */     else
/*      */     {
/* 5449 */       String id = ResultSetUtils.getValue(rset, "dID");
/* 5450 */       this.m_binder.putLocal("dID", id);
/* 5451 */       int paramArg = (serviceOnlyIfExists) ? 1 : 2;
/* 5452 */       if (this.m_currentAction.getNumParams() < paramArg)
/*      */         return;
/* 5454 */       String editDocService = this.m_currentAction.getParamAt(paramArg);
/* 5455 */       this.m_service.executeService(editDocService);
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void evaluateIncludeForPrimaryFile()
/*      */     throws DataException, ServiceException
/*      */   {
/* 5470 */     String inc = this.m_currentAction.getParamAt(0);
/* 5471 */     String result = null;
/* 5472 */     String encoding = DataSerializeUtils.getSystemEncoding();
/*      */     try
/*      */     {
/* 5475 */       result = this.m_service.m_pageMerger.evaluateResourceInclude(inc);
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/* 5479 */       throw new DataException(e.getMessage(), e);
/*      */     }
/*      */ 
/* 5482 */     String tmpFile = new StringBuilder().append(DataBinder.getTemporaryDirectory()).append(DataBinder.getNextFileCounter()).append(".hcsp").toString();
/* 5483 */     File f = new File(tmpFile);
/*      */     try
/*      */     {
/* 5486 */       BufferedWriter w = FileUtils.openDataWriter(f, encoding);
/* 5487 */       w.write(result);
/* 5488 */       w.close();
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/* 5492 */       throw new DataException(e.getMessage(), e);
/*      */     }
/*      */ 
/* 5495 */     this.m_binder.addTempFile(tmpFile);
/* 5496 */     this.m_binder.putLocal("primaryFile:path", tmpFile);
/* 5497 */     this.m_binder.putLocal("dExtension", "hcsp");
/* 5498 */     this.m_binder.putLocal("dFormat", "form/hcsp");
/* 5499 */     this.m_binder.putLocal("primaryFile:format", "form/hcsp");
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void packageEnvironment()
/*      */     throws ServiceException, DataException
/*      */   {
/* 5510 */     String envPkgFileName = new StringBuilder().append("server_environment_").append(System.currentTimeMillis() / 1000L).append(".zip").toString();
/* 5511 */     String envPkgUrl = new StringBuilder().append(DirectoryLocator.getWebRoot(true)).append("groups/secure/logs/env/").append(envPkgFileName).toString();
/* 5512 */     String dirPath = DirectoryLocator.getLogDirectory("env");
/* 5513 */     FileUtils.checkOrCreateDirectory(dirPath, 5);
/*      */ 
/* 5516 */     this.m_binder.putLocal("envPkgDestPath", new StringBuilder().append(dirPath).append("/").append(envPkgFileName).toString());
/* 5517 */     this.m_binder.putLocal("envPkgUrl", envPkgUrl);
/* 5518 */     this.m_binder.putLocal("envPkgFileName", envPkgFileName);
/*      */ 
/* 5521 */     EnvironmentPackager packager = new EnvironmentPackager();
/* 5522 */     packager.packageEnvironment(this.m_binder);
/*      */ 
/* 5525 */     this.m_binder.putLocal("msgResourceInclude", "env_pkg_status_message");
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void appendIncludeCachingMessage()
/*      */   {
/* 5534 */     this.m_binder.putLocal("cacheReportSimpleFilter", "include://");
/* 5535 */     ResourceCacheState.getReport(false, this.m_binder);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void workflowReviewPrepareRedirect()
/*      */     throws ServiceException, DataException
/*      */   {
/* 5546 */     PluginFilters.filter("workflowReviewPrepareRedirect", this.m_workspace, this.m_binder, this.m_service);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void loadFileRenditionsInfo()
/*      */     throws ServiceException, DataException
/*      */   {
/* 5561 */     boolean skipLoadingRenditionsInfo = SharedObjects.getEnvValueAsBoolean("SkipLoadingRenditionsInfo", false);
/*      */ 
/* 5563 */     boolean includeFileRenditionsInfo = DataBinderUtils.getLocalBoolean(this.m_binder, "includeFileRenditionsInfo", false);
/*      */ 
/* 5566 */     if ((skipLoadingRenditionsInfo) || (!includeFileRenditionsInfo))
/*      */     {
/* 5568 */       return;
/*      */     }
/*      */ 
/* 5571 */     DataResultSet rendtionsRes = createInitialFileRenditionsInfo();
/* 5572 */     ResultSet docInfoRes = this.m_binder.getResultSet("DOC_INFO");
/*      */ 
/* 5574 */     this.m_service.setCachedObject("securityProfileResultSet", docInfoRes);
/*      */ 
/* 5579 */     PluginFilters.filter("updateGetFileAvailableFlags", this.m_workspace, this.m_binder, this.m_service);
/*      */ 
/* 5583 */     boolean allowGetCopy = SharedObjects.getEnvValueAsBoolean("GetCopyAccess", true);
/*      */ 
/* 5587 */     boolean treatRenditionsAsWebviewables = SharedObjects.getEnvValueAsBoolean("GetRenditionAccess", true);
/*      */ 
/* 5589 */     boolean userInWorkflow = false;
/* 5590 */     boolean userInWorkflowCheckDone = false;
/* 5591 */     Map resolvedPermissions = new HashMap();
/*      */ 
/* 5593 */     rendtionsRes.first();
/* 5594 */     while (rendtionsRes.isRowPresent())
/*      */     {
/* 5596 */       String rendition = rendtionsRes.getStringValue(0);
/* 5597 */       boolean isAttachment = StringUtils.convertToBool(rendtionsRes.getStringValue(1), false);
/* 5598 */       boolean isWebViewable = false;
/* 5599 */       boolean isNativeRendition = false;
/*      */ 
/* 5601 */       if ((rendition.equalsIgnoreCase("Web")) && (!isAttachment))
/*      */       {
/* 5604 */         String status = ResultSetUtils.getValue(docInfoRes, "dStatus");
/* 5605 */         if ((status == null) || (status.equalsIgnoreCase("GENWWW")))
/*      */         {
/* 5607 */           rendtionsRes.setCurrentValue(2, "0");
/* 5608 */           rendtionsRes.next();
/*      */         }
/*      */ 
/* 5612 */         isWebViewable = true;
/*      */       }
/*      */ 
/* 5615 */       isNativeRendition = (rendition.equalsIgnoreCase("primary")) && (!isAttachment);
/*      */ 
/* 5617 */       if ((!isNativeRendition) && (treatRenditionsAsWebviewables))
/*      */       {
/* 5619 */         isWebViewable = true;
/*      */       }
/*      */ 
/* 5622 */       if (!isWebViewable)
/*      */       {
/* 5625 */         int accessLevelRequiredToRead = 1;
/*      */ 
/* 5627 */         if (!allowGetCopy)
/*      */         {
/* 5633 */           if (!userInWorkflowCheckDone)
/*      */           {
/* 5635 */             userInWorkflow = isUserInWorkflow();
/* 5636 */             userInWorkflowCheckDone = true;
/*      */           }
/*      */ 
/* 5639 */           if (!userInWorkflow)
/*      */           {
/* 5641 */             accessLevelRequiredToRead = 2;
/*      */           }
/*      */         }
/*      */ 
/* 5645 */         String pubType = ResultSetUtils.getValue(docInfoRes, "dPublishType");
/* 5646 */         if ((pubType != null) && (pubType.equalsIgnoreCase("N")))
/*      */         {
/* 5648 */           accessLevelRequiredToRead = 8;
/*      */         }
/*      */ 
/* 5651 */         Boolean hasPermission = (Boolean)resolvedPermissions.get(Integer.valueOf(accessLevelRequiredToRead));
/*      */ 
/* 5653 */         if (hasPermission != null)
/*      */         {
/* 5655 */           if (!hasPermission.booleanValue())
/*      */           {
/* 5657 */             rendtionsRes.setCurrentValue(2, "0");
/*      */           }
/* 5659 */           rendtionsRes.next();
/*      */         }
/*      */ 
/* 5663 */         int m_accessLevel = this.m_service.m_serviceData.m_accessLevel;
/* 5664 */         Properties oldData = this.m_binder.getLocalData();
/* 5665 */         Properties tempData = new Properties(oldData);
/* 5666 */         this.m_binder.setLocalData(tempData);
/*      */         try
/*      */         {
/* 5670 */           this.m_service.m_serviceData.m_accessLevel = accessLevelRequiredToRead;
/* 5671 */           this.m_service.m_securityImpl.checkSecurity(this.m_service, this.m_binder, docInfoRes);
/*      */ 
/* 5675 */           resolvedPermissions.put(Integer.valueOf(accessLevelRequiredToRead), Boolean.valueOf(true));
/*      */         }
/*      */         catch (ServiceException e)
/*      */         {
/* 5679 */           rendtionsRes.setCurrentValue(2, "0");
/* 5680 */           resolvedPermissions.put(Integer.valueOf(accessLevelRequiredToRead), Boolean.valueOf(false));
/*      */         }
/*      */         finally
/*      */         {
/* 5684 */           this.m_binder.setLocalData(oldData);
/*      */         }
/* 5686 */         this.m_service.m_serviceData.m_accessLevel = m_accessLevel;
/*      */       }
/*      */ 
/* 5689 */       rendtionsRes.next();
/*      */     }
/*      */   }
/*      */ 
/*      */   private DataResultSet createInitialFileRenditionsInfo()
/*      */   {
/* 5702 */     String[] fields = { "rendition", "isExtRendition", "isAvailable" };
/* 5703 */     DataResultSet rendtionsRes = new DataResultSet(fields);
/* 5704 */     this.m_binder.addResultSet("RENDITIONS_INFO", rendtionsRes);
/*      */ 
/* 5706 */     addRenditionRowIntoBinder("primary", false, rendtionsRes);
/* 5707 */     addRenditionRowIntoBinder("web", false, rendtionsRes);
/*      */ 
/* 5709 */     ResultSet doc_info = this.m_binder.getResultSet("DOC_INFO");
/* 5710 */     if (doc_info.first())
/*      */     {
/* 5712 */       String coreRendition = doc_info.getStringValueByName("dRendition1");
/* 5713 */       if ((coreRendition != null) && (!coreRendition.isEmpty()))
/*      */       {
/* 5715 */         addRenditionRowIntoBinder(new StringBuilder().append("rendition:").append(coreRendition).toString(), false, rendtionsRes);
/*      */       }
/*      */ 
/* 5718 */       coreRendition = doc_info.getStringValueByName("dRendition2");
/* 5719 */       if ((coreRendition != null) && (!coreRendition.isEmpty()))
/*      */       {
/* 5721 */         addRenditionRowIntoBinder(new StringBuilder().append("rendition:").append(coreRendition).toString(), false, rendtionsRes);
/*      */       }
/*      */     }
/*      */ 
/* 5725 */     ResultSet manifest = this.m_binder.getResultSet("manifest");
/* 5726 */     if (manifest != null)
/*      */     {
/* 5728 */       manifest.first();
/*      */     }
/* 5730 */     while ((manifest != null) && (manifest.isRowPresent()))
/*      */     {
/* 5732 */       String extRenditionName = manifest.getStringValueByName("extRenditionName");
/* 5733 */       if ((extRenditionName != null) && (!extRenditionName.isEmpty()))
/*      */       {
/* 5735 */         addRenditionRowIntoBinder(extRenditionName, true, rendtionsRes);
/*      */       }
/* 5737 */       manifest.next();
/*      */     }
/*      */ 
/* 5740 */     return rendtionsRes;
/*      */   }
/*      */ 
/*      */   protected void addRenditionRowIntoBinder(String rendition, boolean isExtRendition, DataResultSet dataResultSet)
/*      */   {
/* 5752 */     Vector row = new Vector(3);
/* 5753 */     row.add(rendition);
/* 5754 */     row.add((isExtRendition) ? "1" : "0");
/* 5755 */     row.add("1");
/* 5756 */     dataResultSet.addRow(row);
/*      */   }
/*      */ 
/*      */   protected boolean isUserInWorkflow()
/*      */     throws DataException, ServiceException
/*      */   {
/* 5769 */     boolean isUserInWorkflow = false;
/*      */ 
/* 5771 */     ResultSet wfSet = this.m_binder.getResultSet("WF_INFO");
/* 5772 */     if ((wfSet != null) && (!wfSet.isEmpty()))
/*      */     {
/* 5774 */       String wfState = ResultSetUtils.getValue(wfSet, "dWfStatus");
/* 5775 */       if (!wfState.equalsIgnoreCase("INIT"))
/*      */       {
/* 5777 */         isUserInWorkflow = WorkflowUtils.isUserInStep(this.m_workspace, this.m_binder, this.m_service.getUserData().m_name, this.m_service);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 5782 */     return isUserInWorkflow;
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void prepareRedirectToWebviewable()
/*      */     throws ServiceException, DataException
/*      */   {
/* 5789 */     DataBinder fileParams = null;
/* 5790 */     if (this.m_currentAction.getNumParams() > 0)
/*      */     {
/* 5792 */       String rsetName = this.m_currentAction.getParamAt(0);
/* 5793 */       fileParams = new DataBinder();
/* 5794 */       ResultSet drset = this.m_binder.getResultSet(rsetName);
/* 5795 */       fileParams.addResultSet(rsetName, drset);
/*      */ 
/* 5799 */       String dbDocName = ResultSetUtils.getValue(drset, "dDocName");
/* 5800 */       this.m_binder.putLocal("dDocName", dbDocName);
/*      */     }
/*      */     else
/*      */     {
/* 5804 */       fileParams = this.m_binder;
/*      */     }
/*      */ 
/* 5807 */     fileParams.putLocal("RenditionId", "webViewableFile");
/* 5808 */     IdcFileDescriptor d = this.m_fileStore.createDescriptor(fileParams, null, this.m_service);
/* 5809 */     HashMap fsArgs = new HashMap();
/* 5810 */     fsArgs.put("useAbsolute", "0");
/* 5811 */     String url = this.m_fileStore.getClientURL(d, null, fsArgs, this.m_service);
/*      */ 
/* 5813 */     this.m_binder.putLocal("RedirectUrl", url);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void expireUnexpireDocument()
/*      */     throws ServiceException, DataException
/*      */   {
/* 5824 */     boolean isExpire = false;
/* 5825 */     String dOutDate = null;
/* 5826 */     boolean mustNotify = false;
/* 5827 */     Workspace ws = this.m_service.getWorkspace();
/* 5828 */     String dRevClassID = this.m_binder.getLocal("dRevClassID");
/*      */ 
/* 5831 */     String action = this.m_binder.get("action");
/* 5832 */     if (action.equals("expire"))
/*      */     {
/* 5834 */       isExpire = true;
/* 5835 */       dOutDate = "idcTimeCurrent";
/*      */     }
/* 5837 */     else if (action.equals("unexpire"))
/*      */     {
/* 5839 */       dOutDate = "";
/* 5840 */       isExpire = false;
/* 5841 */       mustNotify = true;
/*      */     }
/*      */     else
/*      */     {
/* 5845 */       String msg = LocaleUtils.encodeMessage("csInvalidAction", null, action);
/* 5846 */       this.m_service.createServiceException(null, msg);
/*      */     }
/*      */ 
/* 5849 */     String dReleaseState = this.m_binder.getLocal("dReleaseState");
/* 5850 */     String dID = this.m_binder.getLocal("dID");
/*      */ 
/* 5852 */     if ((dReleaseState.equals("O")) && (isExpire))
/*      */     {
/* 5854 */       String msg = LocaleUtils.encodeMessage("csDocumentIsAlreadyExpired", null, this.m_binder.get("dDocName"));
/*      */ 
/* 5856 */       this.m_service.createServiceException(null, msg);
/*      */     }
/* 5858 */     else if ("RYUIO".indexOf(dReleaseState) >= 0)
/*      */     {
/* 5860 */       MapParameters args = new MapParameters(new HashMap());
/* 5861 */       args.setObject("dID", dID);
/*      */ 
/* 5863 */       if ("YUI".indexOf(dReleaseState) >= 0)
/*      */       {
/* 5865 */         mustNotify = true;
/* 5866 */         args.setObject("dReleaseState", "U");
/*      */       }
/*      */       else
/*      */       {
/* 5870 */         args.setObject("dReleaseState", "N");
/*      */       }
/*      */ 
/* 5873 */       if (isExpire)
/*      */       {
/* 5875 */         args.setObject("dOutDate", dOutDate);
/* 5876 */         ws.execute("UexpireDocument", args);
/*      */       }
/*      */       else
/*      */       {
/* 5880 */         ws.execute("UunexpireDocument", args);
/*      */       }
/*      */     }
/* 5883 */     else if (dReleaseState.equals("N"))
/*      */     {
/* 5888 */       MapParameters args = new MapParameters(new HashMap());
/* 5889 */       args.setObject("dRevClassID", dRevClassID);
/*      */ 
/* 5891 */       ResultSet rset = ws.createResultSet("Qrevisions", args);
/* 5892 */       FieldInfo[] infos = ResultSetUtils.createInfoList(rset, new String[] { "dID", "dReleaseState" }, true);
/*      */ 
/* 5895 */       for (rset.first(); rset.isRowPresent(); rset.next())
/*      */       {
/* 5897 */         String thisID = rset.getStringValue(infos[0].m_index);
/* 5898 */         String thisReleaseState = rset.getStringValue(infos[1].m_index);
/*      */ 
/* 5900 */         args = new MapParameters(new HashMap());
/* 5901 */         args.setObject("dID", thisID);
/* 5902 */         args.setObject("dOutDate", dOutDate);
/*      */ 
/* 5904 */         if ("YUI".indexOf(thisReleaseState) >= 0)
/*      */         {
/* 5906 */           mustNotify = true;
/* 5907 */           args.setObject("dReleaseState", "U");
/* 5908 */           if (isExpire)
/*      */           {
/* 5910 */             args.setObject("dOutDate", dOutDate);
/* 5911 */             ws.execute("UexpireDocument", args); break;
/*      */           }
/*      */ 
/* 5915 */           ws.execute("UunexpireDocument", args);
/*      */ 
/* 5917 */           break;
/*      */         }
/*      */ 
/* 5920 */         ws.execute("UsetOutDate", args);
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/*      */       String msg;
/*      */       String msg;
/* 5926 */       if (isExpire)
/*      */       {
/* 5928 */         msg = LocaleUtils.encodeMessage("csCantExpireDocumentInReleaseState", null, this.m_binder.get("dDocName"));
/*      */       }
/*      */       else
/*      */       {
/* 5933 */         msg = LocaleUtils.encodeMessage("csCantUnexpireDocumentInReleaseState", null, this.m_binder.get("dDocName"));
/*      */       }
/*      */ 
/* 5936 */       this.m_service.createServiceException(null, msg);
/*      */     }
/*      */ 
/* 5941 */     if (!isExpire)
/*      */     {
/* 5943 */       MapParameters args = new MapParameters(new HashMap());
/* 5944 */       args.setObject("dRevClassID", dRevClassID);
/* 5945 */       ws.execute("UunexpireOldRevs", args);
/*      */     }
/*      */ 
/* 5948 */     if (!mustNotify)
/*      */       return;
/* 5950 */     notifyReleaseChange();
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void prepareCopyRevision()
/*      */     throws ServiceException, DataException
/*      */   {
/* 5964 */     String rsetName = this.m_currentAction.getParamAt(0);
/* 5965 */     ResultSet docInfo = this.m_binder.getResultSet(rsetName);
/* 5966 */     if ((docInfo == null) || (!docInfo.isRowPresent()))
/*      */     {
/* 5968 */       String msg = LocaleUtils.encodeMessage("csResultSetNotFoundOrEmpty", null, rsetName);
/*      */ 
/* 5970 */       this.m_service.createServiceException(null, msg);
/*      */     }
/*      */ 
/* 5974 */     String docName = this.m_binder.getLocal("dDocName");
/* 5975 */     String docInfoName = docInfo.getStringValueByName("dDocName");
/* 5976 */     if ((docName == null) || (docInfoName == null) || (!docInfoName.equalsIgnoreCase(docName)))
/*      */     {
/* 5978 */       if (docName == null)
/*      */       {
/* 5980 */         docName = "";
/*      */       }
/* 5982 */       String msg = LocaleUtils.encodeMessage("csCheckinIDNotValid", null, docName);
/* 5983 */       this.m_service.createServiceException(null, msg);
/*      */     }
/*      */ 
/* 5988 */     Table t = ResourceContainerUtils.getDynamicTableResource("CopyRevisionFieldsToInherit");
/* 5989 */     int numFields = docInfo.getNumFields();
/* 5990 */     for (int i = 0; i < numFields; ++i)
/*      */     {
/* 5992 */       String fieldName = docInfo.getFieldName(i);
/* 5993 */       if (((StringUtils.findStringIndex(t.m_colNames, fieldName) < 0) && (!fieldName.startsWith("x"))) || 
/* 5996 */         (this.m_binder.getLocal(fieldName) != null))
/*      */         continue;
/* 5998 */       this.m_binder.putLocal(fieldName, docInfo.getStringValue(i));
/*      */     }
/*      */ 
/* 6004 */     String tempFileName = new StringBuilder().append("").append(DataBinder.getNextFileCounter()).toString();
/* 6005 */     String extension = ResultSetUtils.getValue(docInfo, "dExtension");
/* 6006 */     String originalName = ResultSetUtils.getValue(docInfo, "dOriginalName");
/* 6007 */     if ((extension != null) && (extension.length() > 0))
/*      */     {
/* 6009 */       tempFileName = new StringBuilder().append(tempFileName).append(".").append(extension).toString();
/*      */     }
/* 6011 */     String tempFilePath = new StringBuilder().append(DataBinder.m_tempDir).append(tempFileName).toString();
/*      */ 
/* 6014 */     DataBinder binder = new DataBinder();
/* 6015 */     binder.addResultSet(rsetName, docInfo);
/* 6016 */     binder.putLocal("RenditionId", "primaryFile");
/* 6017 */     IdcFileDescriptor d = this.m_service.m_fileStore.createDescriptor(binder, null, this.m_service);
/*      */     try
/*      */     {
/* 6020 */       this.m_service.m_fileStore.copyToLocalFile(d, new File(tempFilePath), null);
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/* 6024 */       this.m_service.createServiceException(e, null);
/*      */     }
/* 6026 */     this.m_binder.addTempFile(tempFilePath);
/* 6027 */     this.m_binder.putLocal("primaryFile", originalName);
/* 6028 */     this.m_binder.putLocal("primaryFile:path", tempFilePath);
/*      */ 
/* 6031 */     binder.putLocal("RenditionId", "webViewableFile");
/* 6032 */     IdcFileDescriptor a = this.m_service.m_fileStore.createDescriptor(binder, null, this.m_service);
/* 6033 */     String altExtension = a.getProperty("dWebExtension");
/* 6034 */     if ((extension != null) && (altExtension != null) && (!altExtension.equals(extension)))
/*      */     {
/* 6036 */       String altFilePath = new StringBuilder().append(DataBinder.m_tempDir).append(DataBinder.getNextFileCounter()).toString();
/* 6037 */       if (altExtension.length() > 0)
/*      */       {
/* 6039 */         altFilePath = new StringBuilder().append(altFilePath).append(".").append(altExtension).toString();
/*      */       }
/*      */       try
/*      */       {
/* 6043 */         this.m_service.m_fileStore.copyToLocalFile(a, new File(altFilePath), null);
/*      */       }
/*      */       catch (IOException e)
/*      */       {
/* 6047 */         this.m_service.createServiceException(e, null);
/*      */       }
/* 6049 */       this.m_binder.addTempFile(altFilePath);
/* 6050 */       this.m_binder.putLocal("alternateFile:path", altFilePath);
/* 6051 */       this.m_binder.putLocal("alternateFiel", altFilePath);
/*      */     }
/*      */ 
/* 6055 */     this.m_binder.removeLocal("dID");
/* 6056 */     this.m_binder.removeLocal("dDocName");
/*      */ 
/* 6059 */     String newDocName = this.m_binder.getLocal("newDocName");
/* 6060 */     if ((newDocName != null) && (newDocName.length() > 0))
/*      */     {
/* 6062 */       this.m_binder.putLocal("dDocName", newDocName);
/*      */     }
/*      */ 
/* 6067 */     this.m_service.setCachedObject("OriginalDocInfo", docInfo);
/* 6068 */     this.m_binder.removeResultSet(rsetName);
/*      */ 
/* 6070 */     this.m_service.setConditionVar("isCopyRevision", true);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void finalizeCopyRevision()
/*      */     throws ServiceException, DataException
/*      */   {
/* 6082 */     ResultSet originalDocInfo = (ResultSet)this.m_service.getCachedObject("OriginalDocInfo");
/* 6083 */     this.m_binder.addResultSet("OriginalDocInfo", originalDocInfo);
/*      */ 
/* 6085 */     Map args = new HashMap();
/* 6086 */     args.put("dID", this.m_binder.get("dID"));
/* 6087 */     ResultSet rset = this.m_workspace.createResultSet("QdocInfo", new MapParameters(args));
/* 6088 */     DataResultSet drset = new DataResultSet();
/* 6089 */     drset.copy(rset);
/* 6090 */     this.m_binder.addResultSet("NewDocInfo", drset);
/*      */ 
/* 6092 */     this.m_binder.removeLocal("primaryFile:path");
/* 6093 */     this.m_binder.removeLocal("alternateFile:path");
/* 6094 */     this.m_binder.removeLocal("VaultfilePath");
/* 6095 */     this.m_binder.removeLocal("WebfilePath");
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void loadAdditionalDocInfo()
/*      */     throws ServiceException, DataException
/*      */   {
/* 6106 */     MetaFieldUtils.loadAdditionalDocInfo(this.m_workspace, this.m_binder, this.m_service);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void updateContentModification()
/*      */     throws ServiceException, DataException
/*      */   {
/* 6113 */     if (this.m_binder.getActiveAllowMissing("dCreateDate") == null)
/*      */     {
/* 6115 */       checkOrGenerateCreateDate();
/*      */     }
/*      */ 
/* 6118 */     String[] authors = { "dDocLastModifier" };
/* 6119 */     updateUserForRevClasses(authors);
/* 6120 */     this.m_workspace.execute("UrevClassesLastModified", this.m_binder);
/*      */ 
/* 6122 */     this.m_service.setCachedObject("ActionParam", this.m_currentAction.getParamAt(0));
/* 6123 */     PluginFilters.filter("updateContentModification", this.m_workspace, this.m_binder, this.m_service);
/*      */   }
/*      */ 
/*      */   public void checkOrGenerateCreateDate()
/*      */     throws ServiceException, DataException
/*      */   {
/* 6130 */     String createDate = this.m_binder.getLocal("dCreateDate");
/* 6131 */     if ((createDate != null) && (createDate.trim().length() != 0))
/*      */       return;
/* 6133 */     Date dte = new Date();
/* 6134 */     createDate = LocaleUtils.formatODBC(dte);
/* 6135 */     this.m_binder.getLocalizedFields().put("dCreateDate", "");
/* 6136 */     this.m_binder.putLocal("dCreateDate", createDate);
/*      */   }
/*      */ 
/*      */   public void checkOrGenerateDocName(boolean isValidateOnly)
/*      */     throws ServiceException, DataException
/*      */   {
/* 6143 */     int ret = PluginFilters.filter("preComputeDocName", this.m_workspace, this.m_binder, this.m_service);
/* 6144 */     if (ret == -1)
/*      */     {
/* 6146 */       return;
/*      */     }
/*      */ 
/* 6152 */     boolean isAuto = StringUtils.convertToBool(this.m_binder.getAllowMissing("IsAutoNumber"), false);
/*      */ 
/* 6154 */     if ((isAuto) && (!isValidateOnly))
/*      */     {
/* 6156 */       boolean doAuto = true;
/* 6157 */       String curDocName = this.m_binder.getAllowMissing("dDocName");
/* 6158 */       String curRevClassID = this.m_binder.getAllowMissing("dRevClassID");
/* 6159 */       String autoPrefix = this.m_binder.getAllowMissing("AutoNumberPrefix");
/* 6160 */       if (autoPrefix == null)
/*      */       {
/* 6162 */         autoPrefix = "";
/*      */       }
/* 6164 */       if (curRevClassID == null)
/*      */       {
/* 6166 */         doAuto = false;
/*      */       }
/* 6168 */       if ((curDocName != null) && (curDocName.length() > 0))
/*      */       {
/* 6170 */         doAuto = false;
/*      */       }
/* 6172 */       if (isValidateOnly)
/*      */       {
/* 6174 */         doAuto = false;
/*      */       }
/*      */ 
/* 6177 */       if (doAuto)
/*      */       {
/* 6180 */         String num = curRevClassID;
/* 6181 */         int l = curRevClassID.length();
/* 6182 */         int width = SharedObjects.getEnvironmentInt("AutoNumberWidth", 6);
/*      */ 
/* 6184 */         if (l < width)
/*      */         {
/* 6186 */           int diff = width - l;
/* 6187 */           char[] filler = new char[width];
/* 6188 */           for (int i = 0; i < width; ++i)
/*      */           {
/* 6190 */             char ch = (i < diff) ? '0' : curRevClassID.charAt(i - diff);
/* 6191 */             filler[i] = ch;
/*      */           }
/* 6193 */           num = new String(filler);
/*      */         }
/* 6195 */         String prefix = null;
/*      */         try
/*      */         {
/* 6198 */           prefix = this.m_service.m_pageMerger.evaluateScript(autoPrefix);
/*      */         }
/*      */         catch (Throwable e)
/*      */         {
/* 6202 */           this.m_service.createServiceException(e, "!csUnableToEvalAutoNumber");
/*      */         }
/* 6204 */         this.m_binder.putLocal("dDocName", new StringBuilder().append(StringUtils.removeWhitespace(prefix)).append(num).toString());
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 6210 */     ret = PluginFilters.filter("computeDocName", this.m_workspace, this.m_binder, this.m_service);
/* 6211 */     if (ret == -1)
/*      */     {
/* 6213 */       return;
/*      */     }
/*      */ 
/* 6218 */     int resultVal = 0;
/* 6219 */     String name = this.m_binder.getLocal("dDocName");
/* 6220 */     if (((name != null) && (name.length() > 0)) || (!isValidateOnly))
/*      */     {
/* 6222 */       resultVal = Validation.checkUrlFileSegment(name);
/*      */     }
/* 6224 */     if (resultVal != 0)
/*      */     {
/* 6226 */       String msg = LocaleUtils.encodeMessage("csCheckinIDNotValid", null, name);
/*      */ 
/* 6228 */       switch (resultVal)
/*      */       {
/*      */       case -1:
/* 6231 */         msg = "!csCheckinIDNotDefined";
/* 6232 */         break;
/*      */       case -2:
/* 6234 */         msg = LocaleUtils.encodeMessage("csCheckinIDHasSpaces", msg);
/* 6235 */         break;
/*      */       case -3:
/* 6237 */         msg = LocaleUtils.encodeMessage("csCheckinIDHasInvalidChars", msg);
/*      */       }
/*      */ 
/* 6241 */       this.m_service.createServiceException(null, msg);
/*      */     }
/*      */ 
/* 6244 */     if ((this.m_checkUnique) && 
/* 6246 */       (!isUniqueDocName()))
/*      */     {
/* 6248 */       String docName = this.m_binder.getLocal("dDocName");
/* 6249 */       String msg = LocaleUtils.encodeMessage("csCheckinIDNotUnique2", null, docName);
/*      */ 
/* 6251 */       this.m_service.createServiceException(null, msg);
/*      */     }
/*      */ 
/* 6254 */     if (SharedObjects.getEnvValueAsBoolean("Found8dot3Weblayout", false))
/*      */     {
/* 6256 */       String docName = this.m_binder.getLocal("dDocName");
/* 6257 */       if ((docName != null) && (docName.length() == 6))
/*      */       {
/* 6259 */         throw new ServiceException(null, "cs8dot3CheckinError", new Object[0]);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 6265 */     ret = PluginFilters.filter("postComputeDocName", this.m_workspace, this.m_binder, this.m_service);
/* 6266 */     if (ret != -1)
/*      */       return;
/* 6268 */     return;
/*      */   }
/*      */ 
/*      */   public void restoreRevision()
/*      */     throws ServiceException, DataException, IOException
/*      */   {
/* 6274 */     String docInfoName = this.m_currentAction.getParamAt(0);
/* 6275 */     String revHistoryName = this.m_currentAction.getParamAt(1);
/* 6276 */     String latestInfoName = this.m_currentAction.getParamAt(2);
/* 6277 */     ResultSet revHistory = this.m_binder.getResultSet(revHistoryName);
/* 6278 */     ResultSet docInfo = this.m_binder.getResultSet(docInfoName);
/* 6279 */     ResultSet latestInfo = this.m_binder.getResultSet(latestInfoName);
/* 6280 */     Properties origData = (Properties)this.m_binder.getLocalData().clone();
/* 6281 */     String dID = this.m_binder.getLocal("dID");
/* 6282 */     String latestID = ResultSetUtils.getValue(revHistory, "dID");
/*      */ 
/* 6285 */     if ((latestInfo != null) && (latestInfo.first()))
/*      */     {
/* 6287 */       String checkedOutUser = ResultSetUtils.getValue(latestInfo, "dCheckoutUser");
/* 6288 */       if ((checkedOutUser != null) && (checkedOutUser.length() > 0))
/*      */       {
/* 6290 */         String currentUser = this.m_binder.getLocal("dUser");
/* 6291 */         if ((currentUser != null) && (currentUser.equals(checkedOutUser)))
/*      */         {
/* 6293 */           this.m_binder.putLocal("dID", latestID);
/* 6294 */           this.m_service.executeService("UNDO_CHECKOUT_IMPLEMENT");
/*      */         }
/*      */         else
/*      */         {
/* 6298 */           IdcMessage msg = IdcMessageFactory.lc("csRevIsCheckedOut", new Object[] { checkedOutUser });
/* 6299 */           this.m_service.createServiceException(msg);
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/* 6307 */       this.m_binder.putLocal("dID", latestID);
/* 6308 */       this.m_service.executeService("CHECKOUT_IMPLEMENT");
/*      */ 
/* 6310 */       this.m_binder.setLocalData(origData);
/* 6311 */       this.m_binder.mergeResultSetRowIntoLocalData(docInfo);
/* 6312 */       this.m_binder.putLocal("dID", latestID);
/* 6313 */       this.m_binder.putLocal("dProcessingState", "");
/*      */ 
/* 6316 */       this.m_binder.removeResultSet("DOC_INFO");
/*      */     }
/*      */     catch (ServiceException se)
/*      */     {
/* 6320 */       this.m_service.executeService("UNDO_CHECKOUT_IMPLEMENT");
/* 6321 */       this.m_service.createServiceException(se, se.getLocalizedMessage());
/*      */     }
/*      */     catch (DataException de)
/*      */     {
/* 6325 */       this.m_service.executeService("UNDO_CHECKOUT_IMPLEMENT");
/* 6326 */       this.m_service.createServiceException(de, de.getLocalizedMessage());
/*      */     }
/*      */ 
/* 6329 */     String extension = ResultSetUtils.getValue(docInfo, "dExtension");
/* 6330 */     if ((extension != null) && (extension.length() > 0))
/*      */     {
/* 6332 */       extension = new StringBuilder().append(".").append(extension).toString();
/*      */     }
/*      */ 
/* 6335 */     FileStoreProvider fsp = FileStoreProviderLoader.initFileStore(this.m_service);
/* 6336 */     Properties props = ResultSetUtils.getCurrentRowProps(docInfo);
/* 6337 */     props.put("RenditionId", "primaryFile");
/* 6338 */     PropParameters params = new PropParameters(props);
/* 6339 */     IdcFileDescriptor primaryFile = fsp.createDescriptor(params, null, this.m_service);
/* 6340 */     String primaryExtension = primaryFile.getProperty("dExtension");
/* 6341 */     String tmpDir = LegacyDirectoryLocator.getVaultTempDirectory();
/* 6342 */     String tmpFilePath = new StringBuilder().append(tmpDir).append(dID).append(extension).toString();
/*      */ 
/* 6344 */     props.put("RenditionId", "webViewableFile");
/* 6345 */     params = new PropParameters(props);
/* 6346 */     IdcFileDescriptor alternateFile = fsp.createDescriptor(params, null, this.m_service);
/* 6347 */     String alternateExtension = alternateFile.getProperty("dWebExtension");
/* 6348 */     boolean skipCheckInAlternate = primaryExtension.equals(alternateExtension);
/*      */     try
/*      */     {
/* 6351 */       fsp.copyToLocalFile(primaryFile, new File(tmpFilePath), null);
/* 6352 */       this.m_binder.putLocal("primaryFile:path", tmpFilePath);
/* 6353 */       this.m_binder.addTempFile(tmpFilePath);
/* 6354 */       if (!skipCheckInAlternate)
/*      */       {
/* 6357 */         String tmpAltPath = new StringBuilder().append(tmpDir).append(dID).append(".").append(alternateExtension).toString();
/* 6358 */         fsp.copyToLocalFile(alternateFile, new File(tmpAltPath), null);
/* 6359 */         this.m_binder.putLocal("alternateFile:path", tmpAltPath);
/* 6360 */         this.m_binder.putLocal("alternateFile", tmpAltPath);
/* 6361 */         this.m_binder.addTempFile(tmpAltPath);
/*      */       }
/*      */     }
/*      */     catch (IOException ioe)
/*      */     {
/* 6366 */       this.m_service.executeService("UNDO_CHECKOUT_IMPLEMENT");
/* 6367 */       this.m_service.createServiceException(ioe, ioe.getLocalizedMessage());
/*      */     }
/*      */ 
/* 6371 */     String revLabel = ResultSetUtils.getValue(revHistory, "dRevLabel");
/* 6372 */     if ((revLabel != null) && (revLabel.length() > 0))
/*      */     {
/* 6374 */       this.m_binder.putLocal("dRevLabel", revLabel);
/* 6375 */       updateRevisionLabel();
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/* 6382 */       this.m_binder.putLocal("dRendition1", "");
/* 6383 */       this.m_binder.putLocal("dRendition2", "");
/*      */ 
/* 6385 */       this.m_binder.putLocal("dDocAuthor", this.m_binder.getLocal("dUser"));
/*      */ 
/* 6387 */       this.m_service.executeService("CHECKIN_SEL_SUB");
/*      */     }
/*      */     catch (ServiceException se)
/*      */     {
/* 6391 */       this.m_service.executeService("UNDO_CHECKOUT_IMPLEMENT");
/* 6392 */       this.m_service.createServiceException(se, se.getLocalizedMessage());
/*      */     }
/*      */     catch (DataException de)
/*      */     {
/* 6396 */       this.m_service.executeService("UNDO_CHECKOUT_IMPLEMENT");
/* 6397 */       this.m_service.createServiceException(de, de.getLocalizedMessage());
/*      */     }
/*      */   }
/*      */ 
/*      */   public void getAlternateFileName()
/*      */     throws DataException, ServiceException
/*      */   {
/* 6417 */     ResultSet alternateFileResultSet = this.m_service.getWorkspace().createResultSet("QalternateDocument", this.m_binder);
/*      */ 
/* 6420 */     if (alternateFileResultSet == null)
/*      */     {
/* 6422 */       String errorMessage = LocaleUtils.encodeMessage("csDbUnableToCreateResultSet", null, "QalternateDocument");
/*      */ 
/* 6424 */       throw new ServiceException(errorMessage);
/*      */     }
/*      */ 
/* 6431 */     boolean hasRow = alternateFileResultSet.first();
/* 6432 */     if (!hasRow)
/*      */       return;
/* 6434 */     String alternateFiledOriginalName = alternateFileResultSet.getStringValueByName("dOriginalName");
/*      */ 
/* 6437 */     this.m_binder.putLocal("alternateFiledOriginalName", alternateFiledOriginalName);
/*      */   }
/*      */ 
/*      */   public void conditionalUpdateOriginalName()
/*      */     throws DataException, ServiceException
/*      */   {
/* 6444 */     if (!StringUtils.convertToBool(this.m_binder.getAllowMissing("allowUpdateOriginalName"), false))
/*      */       return;
/* 6446 */     this.m_workspace.execute("UdocumentOriginalName", this.m_binder);
/* 6447 */     PluginFilters.filter("conditionalUpdateOriginalName", this.m_workspace, this.m_binder, this.m_service);
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 6453 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 103070 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.DocServiceHandler
 * JD-Core Version:    0.5.4
 */