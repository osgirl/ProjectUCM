/*      */ package intradoc.filestore;
/*      */ 
/*      */ import intradoc.common.DataStreamWrapper;
/*      */ import intradoc.common.EventOutputStream;
/*      */ import intradoc.common.ExecutionContext;
/*      */ import intradoc.common.FileUtils;
/*      */ import intradoc.common.IdcTransactionListener;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StreamEventHandler;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.Parameters;
/*      */ import intradoc.provider.Provider;
/*      */ import intradoc.provider.ProviderConfig;
/*      */ import intradoc.shared.FilterImplementor;
/*      */ import intradoc.shared.PluginFilters;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import java.io.File;
/*      */ import java.io.IOException;
/*      */ import java.io.InputStream;
/*      */ import java.io.OutputStream;
/*      */ import java.util.ArrayList;
/*      */ import java.util.HashMap;
/*      */ import java.util.Hashtable;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Properties;
/*      */ 
/*      */ public class BaseFileStore
/*      */   implements FileStoreProvider, StreamEventHandler, IdcTransactionListener
/*      */ {
/*   32 */   public static String[][] m_pathComponentReplaceDefaults = { { "dispersion", "" }, { "endDispMarker", "" }, { "FsWeblayoutDir", "$#env.WeblayoutDir$" }, { "FsHttpWebRoot", "$HttpWebRoot$" } };
/*      */   public Provider m_provider;
/*      */   public String m_providerName;
/*      */   public String m_providerConfigDefaultClass;
/*      */   public boolean m_isStarted;
/*      */   public Map m_defaultArgs;
/*      */   protected List m_createdImplementorsKeys;
/*      */   protected Map m_implementMap;
/*      */   public FileStoreAccessImplementor m_accessImp;
/*      */   public FileStoreMetadataImplementor m_metaDataImp;
/*      */   public FileStoreDescriptorImplementor m_descriptorImp;
/*      */   public FileStoreEventImplementor m_eventImp;
/*      */   public BaseFileHelper m_fileHelper;
/*      */   public int m_featureFlags;
/*      */ 
/*      */   public BaseFileStore()
/*      */   {
/*   42 */     this.m_providerConfigDefaultClass = "intradoc.filestore.FileStoreProviderConfig";
/*      */ 
/*   48 */     this.m_createdImplementorsKeys = null;
/*      */   }
/*      */ 
/*      */   public void createImplementors()
/*      */     throws DataException
/*      */   {
/*   66 */     this.m_implementMap = new Hashtable();
/*      */ 
/*   68 */     String[][] standardImp = { { "AccessImplementor", "intradoc.filestore.filesystem.BaseAccessImplementor" }, { "MetadataImplementor", "intradoc.filestore.filesystem.BaseMetadataImplementor" }, { "EventImplementor", "intradoc.filestore.filesystem.BaseEventImplementor" }, { "DescriptorImplementor", "intradoc.filestore.filesystem.BaseDescriptorImplementor" }, { "FileHelper", "intradoc.filestore.BaseFileHelper" } };
/*      */ 
/*   77 */     this.m_createdImplementorsKeys = new ArrayList();
/*   78 */     for (int i = 0; i < standardImp.length; ++i)
/*      */     {
/*   80 */       String key = standardImp[i][0];
/*   81 */       String clName = standardImp[i][1];
/*   82 */       CommonStoreImplementor csi = (CommonStoreImplementor)this.m_provider.createClass(key, clName);
/*      */ 
/*   84 */       this.m_createdImplementorsKeys.add(key);
/*   85 */       this.m_implementMap.put(key, csi);
/*   86 */       checkAssignToInternalAttribute(key, csi);
/*      */     }
/*      */ 
/*   89 */     createExtraImplementors();
/*      */   }
/*      */ 
/*      */   public void createExtraImplementors()
/*      */     throws DataException
/*      */   {
/*   95 */     DataResultSet drset = (DataResultSet)getProviderData().getResultSet("FSImplementors");
/*   96 */     if (drset == null)
/*      */       return;
/*   98 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*      */     {
/*  100 */       Map map = drset.getCurrentRowMap();
/*  101 */       String name = (String)map.get("name");
/*  102 */       String loc = (String)map.get("location");
/*      */ 
/*  104 */       CommonStoreImplementor csi = (CommonStoreImplementor)this.m_provider.createClass(name, loc);
/*      */ 
/*  106 */       if (this.m_implementMap.get(name) == null)
/*      */       {
/*  108 */         this.m_createdImplementorsKeys.add(name);
/*      */       }
/*      */       else
/*      */       {
/*  112 */         checkAssignToInternalAttribute(name, csi);
/*      */       }
/*  114 */       this.m_implementMap.put(name, csi);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void preInitImplementors()
/*      */     throws DataException
/*      */   {
/*  122 */     for (int i = 0; i < this.m_createdImplementorsKeys.size(); ++i)
/*      */     {
/*  124 */       String key = (String)this.m_createdImplementorsKeys.get(i);
/*  125 */       Object o = this.m_implementMap.get(key);
/*  126 */       if ((o == null) || (!o instanceof CommonStoreImplementor))
/*      */         continue;
/*  128 */       CommonStoreImplementor csi = (CommonStoreImplementor)o;
/*  129 */       csi.preInit(this, this.m_provider);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void initImplementors()
/*      */   {
/*  142 */     for (int i = 0; i < this.m_createdImplementorsKeys.size(); ++i)
/*      */     {
/*  144 */       String key = (String)this.m_createdImplementorsKeys.get(i);
/*  145 */       Object o = this.m_implementMap.get(key);
/*  146 */       if ((o == null) || (!o instanceof CommonStoreImplementor))
/*      */         continue;
/*  148 */       CommonStoreImplementor csi = (CommonStoreImplementor)o;
/*  149 */       csi.preInit(this, this.m_provider);
/*  150 */       csi.init(this, this.m_provider);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void checkAssignToInternalAttribute(String name, CommonStoreImplementor csi)
/*      */   {
/*  157 */     if (csi instanceof FileStoreAccessImplementor)
/*      */     {
/*  159 */       this.m_accessImp = ((FileStoreAccessImplementor)csi);
/*      */     }
/*  161 */     else if (csi instanceof FileStoreMetadataImplementor)
/*      */     {
/*  163 */       this.m_metaDataImp = ((FileStoreMetadataImplementor)csi);
/*      */     }
/*  165 */     else if (csi instanceof FileStoreEventImplementor)
/*      */     {
/*  167 */       this.m_eventImp = ((FileStoreEventImplementor)csi);
/*      */     }
/*  169 */     else if (csi instanceof FileStoreDescriptorImplementor)
/*      */     {
/*  171 */       this.m_descriptorImp = ((FileStoreDescriptorImplementor)csi);
/*      */     } else {
/*  173 */       if (!csi instanceof BaseFileHelper)
/*      */         return;
/*  175 */       this.m_fileHelper = ((BaseFileHelper)csi);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void init(Provider provider)
/*      */     throws DataException
/*      */   {
/*  189 */     this.m_provider = provider;
/*  190 */     this.m_providerName = this.m_provider.getName();
/*      */ 
/*  192 */     this.m_featureFlags = 3;
/*      */ 
/*  194 */     createImplementors();
/*  195 */     preInitImplementors();
/*      */   }
/*      */ 
/*      */   public void startProvider() throws DataException, ServiceException
/*      */   {
/*  200 */     this.m_isStarted = true;
/*      */   }
/*      */ 
/*      */   public void stopProvider()
/*      */   {
/*  205 */     this.m_isStarted = false;
/*      */   }
/*      */ 
/*      */   public Provider getProvider()
/*      */   {
/*  210 */     return this.m_provider;
/*      */   }
/*      */ 
/*      */   public void testConnection(DataBinder binder, ExecutionContext ctxt)
/*      */     throws DataException, ServiceException
/*      */   {
/*  216 */     if (this.m_isStarted)
/*      */       return;
/*  218 */     String msg = LocaleUtils.encodeMessage("csProviderNotStarted", null);
/*  219 */     throw new ServiceException(msg);
/*      */   }
/*      */ 
/*      */   public String getReportString(String key)
/*      */   {
/*  229 */     if (key.equals("startup"))
/*      */     {
/*  231 */       Map capabilities = (Map)this.m_provider.getProviderObject("Capabilities");
/*  232 */       return LocaleUtils.encodeMessage("csStartedMessage", null, capabilities.get("version_info"));
/*      */     }
/*      */ 
/*  235 */     return "";
/*      */   }
/*      */ 
/*      */   public ProviderConfig createProviderConfig() throws DataException
/*      */   {
/*  240 */     ProviderConfig pConfig = null;
/*  241 */     boolean isRetry = false;
/*      */     try
/*      */     {
/*  244 */       pConfig = (ProviderConfig)this.m_provider.createClass("ProviderConfig", this.m_providerConfigDefaultClass);
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/*  249 */       isRetry = true;
/*  250 */       String name = this.m_provider.getName();
/*  251 */       this.m_provider.markErrorState(-26, e);
/*  252 */       String msg = LocaleUtils.encodeMessage("csProviderConfigError", null, name);
/*      */ 
/*  254 */       Report.error(null, msg, e);
/*      */     }
/*      */ 
/*  257 */     if (isRetry)
/*      */     {
/*  259 */       this.m_provider.getProviderData().putLocal("ProviderConfig", this.m_providerConfigDefaultClass);
/*  260 */       pConfig = (ProviderConfig)this.m_provider.createClass("ProviderConfig", this.m_providerConfigDefaultClass);
/*      */     }
/*      */ 
/*  263 */     return pConfig;
/*      */   }
/*      */ 
/*      */   public void pollConnectionState(DataBinder data, Properties state)
/*      */   {
/*      */   }
/*      */ 
/*      */   public void releaseConnection()
/*      */   {
/*      */   }
/*      */ 
/*      */   public Map getCapabilities(IdcFileDescriptor descriptor)
/*      */     throws DataException
/*      */   {
/*  280 */     return (Map)this.m_provider.getProviderObject("Capabilities");
/*      */   }
/*      */ 
/*      */   public Map getCapabilities(IdcFileDescriptor source, IdcFileDescriptor target)
/*      */     throws DataException
/*      */   {
/*  286 */     return (Map)this.m_provider.getProviderObject("Capabilities");
/*      */   }
/*      */ 
/*      */   public boolean hasFeature(int featureFlag)
/*      */   {
/*  291 */     return (this.m_featureFlags & featureFlag) != 0;
/*      */   }
/*      */ 
/*      */   public DataBinder getProviderData()
/*      */   {
/*  296 */     return this.m_provider.getProviderData();
/*      */   }
/*      */ 
/*      */   public Object getImplementor(String name)
/*      */   {
/*  301 */     Object obj = this.m_implementMap.get(name);
/*  302 */     return obj;
/*      */   }
/*      */ 
/*      */   public IdcFileDescriptor createDescriptor(Parameters metaData, Map args, ExecutionContext context)
/*      */     throws ServiceException, DataException
/*      */   {
/*  310 */     checkProviderData();
/*  311 */     BasicIdcFileDescriptor descriptor = (BasicIdcFileDescriptor)this.m_descriptorImp.createDescriptor(metaData, args, context);
/*      */ 
/*  313 */     descriptor.put("dRenditionId", metaData.get("RenditionId"));
/*      */ 
/*  315 */     String storageClass = descriptor.getProperty("StorageClass");
/*  316 */     Map fsMetaData = (Map)this.m_provider.getProviderObject("MetaData");
/*  317 */     if ((fsMetaData != null) && (storageClass != null))
/*      */     {
/*  319 */       String storageRule = descriptor.getProperty("StorageRule");
/*  320 */       String key = computeStorageKey(storageClass, storageRule);
/*      */ 
/*  322 */       String[] metaDataFields = (String[])(String[])fsMetaData.get(key);
/*  323 */       appendMetaData(metaData, descriptor, args, metaDataFields);
/*      */ 
/*  325 */       boolean isContainer = getConfigBoolean("isContainer", args, false, false);
/*  326 */       if (!isContainer)
/*      */       {
/*  328 */         metaDataFields = (String[])(String[])fsMetaData.get(key + "_ext");
/*  329 */         if (metaDataFields != null)
/*      */         {
/*  331 */           appendMetaData(metaData, descriptor, args, metaDataFields);
/*      */         }
/*      */       }
/*  334 */       String[] optionalFields = (String[])(String[])fsMetaData.get("optionalFields");
/*  335 */       if (optionalFields != null)
/*      */       {
/*  337 */         Map mdArgs = new HashMap();
/*  338 */         mdArgs.put("isOptionalField", "1");
/*  339 */         appendMetaData(metaData, descriptor, mdArgs, optionalFields);
/*      */       }
/*      */     }
/*      */ 
/*  343 */     updateCacheData(descriptor, args, context);
/*  344 */     return descriptor;
/*      */   }
/*      */ 
/*      */   public String getClientURL(IdcFileDescriptor descriptor, String baseUrl, Map args, ExecutionContext context)
/*      */     throws DataException, ServiceException
/*      */   {
/*  350 */     return this.m_descriptorImp.getClientURL(descriptor, baseUrl, args, context);
/*      */   }
/*      */ 
/*      */   public String getFilesystemPath(IdcFileDescriptor descriptor, ExecutionContext context)
/*      */     throws DataException, ServiceException
/*      */   {
/*  357 */     return this.m_descriptorImp.getFilesystemPath(descriptor, context);
/*      */   }
/*      */ 
/*      */   public String getFilesystemPathWithArgs(IdcFileDescriptor descriptor, Map args, ExecutionContext context)
/*      */     throws DataException, ServiceException
/*      */   {
/*  363 */     return this.m_descriptorImp.getFilesystemPathWithArgs(descriptor, args, context);
/*      */   }
/*      */ 
/*      */   public IdcFileDescriptor getContainer(IdcFileDescriptor descriptor, Map args)
/*      */     throws DataException, ServiceException
/*      */   {
/*  369 */     return this.m_descriptorImp.getContainer(descriptor, args);
/*      */   }
/*      */ 
/*      */   public String getContainerPath(IdcFileDescriptor descriptor, Map args, ExecutionContext context)
/*      */     throws DataException, ServiceException
/*      */   {
/*  375 */     return this.m_descriptorImp.getContainerPath(descriptor, args, context);
/*      */   }
/*      */ 
/*      */   public boolean compareDescriptors(IdcFileDescriptor desc1, IdcFileDescriptor desc2, Map args, ExecutionContext context)
/*      */     throws DataException, ServiceException
/*      */   {
/*  382 */     return this.m_descriptorImp.compareDescriptors(desc1, desc2, args, context);
/*      */   }
/*      */ 
/*      */   public Map getKeyMetaData(IdcFileDescriptor descriptor)
/*      */     throws DataException, ServiceException
/*      */   {
/*  389 */     return this.m_metaDataImp.getKeyMetaData(descriptor);
/*      */   }
/*      */ 
/*      */   public void appendMetaData(Parameters source, IdcFileDescriptor target, Map args, String[] list)
/*      */     throws DataException
/*      */   {
/*  395 */     this.m_metaDataImp.appendMetaData(source, target, args, list);
/*      */   }
/*      */ 
/*      */   public void updateCacheData(IdcFileDescriptor descriptor, Map args, ExecutionContext cxt)
/*      */   {
/*  400 */     this.m_metaDataImp.updateCacheData(descriptor, args, cxt);
/*      */   }
/*      */ 
/*      */   public Map getStorageData(IdcFileDescriptor descriptor, Map args, ExecutionContext cxt)
/*      */     throws DataException, ServiceException, IOException
/*      */   {
/*  407 */     boolean isUpdate = getConfigBoolean("isUpdateCache", args, false, false);
/*  408 */     String fileExistsStr = ((BasicIdcFileDescriptor)descriptor).getCacheProperty("fileExists");
/*      */ 
/*  410 */     if ((fileExistsStr == null) || (isUpdate))
/*      */     {
/*  412 */       updateCacheData(descriptor, args, cxt);
/*      */     }
/*  414 */     return this.m_metaDataImp.getStorageData(descriptor, args, cxt);
/*      */   }
/*      */ 
/*      */   public OutputStream getOutputStream(IdcFileDescriptor descriptor, Map args)
/*      */     throws DataException, ServiceException, IOException
/*      */   {
/*  422 */     OutputStream outStream = this.m_accessImp.getOutputStream(descriptor, args);
/*  423 */     EventOutputStream out = new EventOutputStream(outStream);
/*  424 */     FileStoreEventData data = new FileStoreEventData(descriptor, args);
/*  425 */     out.addStreamEventHandler(this, data);
/*      */ 
/*  427 */     ((BasicIdcFileDescriptor)descriptor).remove("fileSize");
/*  428 */     return out;
/*      */   }
/*      */ 
/*      */   public void commitOutputStream(OutputStream stream, IdcFileDescriptor descriptor, Map args)
/*      */     throws DataException, ServiceException, IOException
/*      */   {
/*  434 */     this.m_accessImp.commitOutputStream(stream, descriptor, args);
/*  435 */     if (args == null)
/*      */       return;
/*  437 */     args.put("isClosed", "1");
/*      */   }
/*      */ 
/*      */   public void storeFromInputStream(IdcFileDescriptor descriptor, InputStream inputStream, Map args, ExecutionContext cxt)
/*      */     throws ServiceException, DataException, IOException
/*      */   {
/*  444 */     this.m_accessImp.storeFromInputStream(descriptor, inputStream, args, cxt);
/*      */ 
/*  447 */     if (cxt == null) {
/*      */       return;
/*      */     }
/*  450 */     boolean isMove = getConfigBoolean("isMove", args, false, false);
/*  451 */     if (isMove)
/*      */       return;
/*  453 */     FileStoreUtils.addActionToRollbackLog("delete", descriptor, null, args, this.m_providerName, cxt);
/*      */   }
/*      */ 
/*      */   public void storeFromLocalFile(IdcFileDescriptor descriptor, File localFile, Map args, ExecutionContext cxt)
/*      */     throws ServiceException, DataException, IOException
/*      */   {
/*  462 */     this.m_accessImp.storeFromLocalFile(descriptor, localFile, args, cxt);
/*      */   }
/*      */ 
/*      */   public void storeFromStreamWrapper(IdcFileDescriptor descriptor, DataStreamWrapper streamWrapper, Map args, ExecutionContext cxt)
/*      */     throws ServiceException, DataException, IOException
/*      */   {
/*  468 */     this.m_accessImp.storeFromStreamWrapper(descriptor, streamWrapper, args, cxt);
/*      */   }
/*      */ 
/*      */   public InputStream getInputStream(IdcFileDescriptor descriptor, Map args)
/*      */     throws DataException, ServiceException, IOException
/*      */   {
/*  476 */     return this.m_accessImp.getInputStream(descriptor, args);
/*      */   }
/*      */ 
/*      */   public void fillInputWrapper(DataStreamWrapper wrapper, ExecutionContext cxt)
/*      */     throws DataException, ServiceException, IOException
/*      */   {
/*  482 */     this.m_accessImp.fillInputWrapper(wrapper, cxt);
/*      */   }
/*      */ 
/*      */   public void copyToOutputStream(IdcFileDescriptor descriptor, OutputStream outputStream, Map args)
/*      */     throws ServiceException, DataException, IOException
/*      */   {
/*  488 */     this.m_accessImp.copyToOutputStream(descriptor, outputStream, args);
/*      */   }
/*      */ 
/*      */   public void copyToLocalFile(IdcFileDescriptor descriptor, File localPath, Map args)
/*      */     throws ServiceException, DataException, IOException
/*      */   {
/*  494 */     this.m_accessImp.copyToLocalFile(descriptor, localPath, args);
/*      */   }
/*      */ 
/*      */   public void duplicateFile(IdcFileDescriptor source, IdcFileDescriptor target, Map args, ExecutionContext cxt)
/*      */     throws ServiceException, DataException, IOException
/*      */   {
/*  502 */     this.m_accessImp.duplicateFile(source, target, args, cxt);
/*  503 */     updateCacheData(target, args, cxt);
/*      */ 
/*  505 */     if (cxt == null)
/*      */       return;
/*  507 */     FileStoreUtils.addActionToRollbackLog("delete", target, null, args, this.m_providerName, cxt);
/*      */   }
/*      */ 
/*      */   public void moveFile(IdcFileDescriptor source, IdcFileDescriptor target, Map args, ExecutionContext cxt)
/*      */     throws ServiceException, DataException, IOException
/*      */   {
/*  514 */     this.m_accessImp.moveFile(source, target, args, cxt);
/*  515 */     if (cxt == null)
/*      */       return;
/*  517 */     FileStoreUtils.addActionToRollbackLog("rename", target, source, args, this.m_providerName, cxt);
/*      */   }
/*      */ 
/*      */   public void deleteFile(IdcFileDescriptor descriptor, Map args, ExecutionContext cxt)
/*      */     throws DataException, ServiceException, IOException
/*      */   {
/*  525 */     this.m_accessImp.deleteFile(descriptor, args, cxt);
/*      */   }
/*      */ 
/*      */   public void forceToFilesystemPath(IdcFileDescriptor descriptor, Map args, ExecutionContext cxt)
/*      */     throws DataException, ServiceException, IOException
/*      */   {
/*  531 */     this.m_accessImp.forceToFilesystemPath(descriptor, args, cxt);
/*      */   }
/*      */ 
/*      */   public void handleStreamEvent(String event, Object stream, Object data)
/*      */     throws IOException
/*      */   {
/*  538 */     if ((!stream instanceof EventOutputStream) || (event != "close"))
/*      */     {
/*  540 */       return;
/*      */     }
/*  542 */     FileStoreEventData eventData = (FileStoreEventData)data;
/*  543 */     Map args = eventData.m_eventArgs;
/*  544 */     IdcFileDescriptor descriptor = eventData.m_descriptor;
/*      */     try
/*      */     {
/*  547 */       boolean isClosed = getConfigBoolean("isClosed", args, false, false);
/*  548 */       if (!isClosed)
/*      */       {
/*  550 */         commitOutputStream((EventOutputStream)stream, descriptor, args);
/*      */       }
/*  552 */       updateCacheData(descriptor, args, null);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  556 */       if (e instanceof IOException)
/*      */       {
/*  558 */         throw ((IOException)e);
/*      */       }
/*  560 */       IOException io = new IOException(e.getMessage());
/*  561 */       io.initCause(e);
/*  562 */       throw io;
/*      */     }
/*      */   }
/*      */ 
/*      */   public void checkProviderData()
/*      */     throws DataException, ServiceException
/*      */   {
/*  569 */     if (this.m_provider.isInError())
/*      */     {
/*  571 */       throw new DataException("!csFsMisconfigured");
/*      */     }
/*  573 */     ProviderConfig pConfig = this.m_provider.getProviderConfig();
/*  574 */     pConfig.loadResources();
/*      */   }
/*      */ 
/*      */   public String computeStorageClass(Parameters metaData) throws DataException
/*      */   {
/*  579 */     String rendition = metaData.getSystem("RenditionId.path");
/*  580 */     if ((rendition == null) || (rendition.length() == 0))
/*      */     {
/*  582 */       rendition = metaData.get("RenditionId");
/*      */     }
/*  584 */     if ((rendition == null) || (rendition.length() == 0))
/*      */     {
/*  586 */       String msg = LocaleUtils.encodeMessage("csRequiredFieldMissing2", null, "RenditionId.path");
/*      */ 
/*  588 */       throw new DataException(msg);
/*      */     }
/*  590 */     return computeStorageClass(rendition);
/*      */   }
/*      */ 
/*      */   public String computeStorageClass(String rendition) throws DataException
/*      */   {
/*  595 */     String storageClass = rendition;
/*  596 */     Map storageMap = (Map)this.m_provider.getProviderObject("RenditionToStorage");
/*  597 */     if (storageMap != null)
/*      */     {
/*  599 */       int index = rendition.indexOf(":");
/*  600 */       if (index > 0)
/*      */       {
/*  602 */         rendition = rendition.substring(0, index);
/*      */       }
/*  604 */       storageClass = (String)storageMap.get(rendition);
/*  605 */       if (storageClass == null)
/*      */       {
/*  607 */         storageClass = rendition;
/*      */       }
/*      */     }
/*  610 */     return storageClass;
/*      */   }
/*      */ 
/*      */   public String computeStorageKey(IdcFileDescriptor descriptor)
/*      */   {
/*  615 */     String storageClass = descriptor.getProperty("StorageClass");
/*  616 */     String storageRule = descriptor.getProperty("StorageRule");
/*      */ 
/*  618 */     return computeStorageKey(storageClass, storageRule);
/*      */   }
/*      */ 
/*      */   public String computeStorageKey(String storageClass, String storageRule)
/*      */   {
/*  625 */     String[] result = new String[1];
/*  626 */     determinePathConfigEx(storageRule, storageClass, result);
/*      */ 
/*  628 */     String key = "";
/*  629 */     if ((storageRule != null) && (storageRule.length() > 0))
/*      */     {
/*  631 */       key = storageRule + "_";
/*      */     }
/*  633 */     if (result[0] != null)
/*      */     {
/*  635 */       key = key + result[0];
/*      */     }
/*      */     else
/*      */     {
/*  641 */       key = key + storageClass;
/*      */     }
/*  643 */     return key;
/*      */   }
/*      */ 
/*      */   public Map determinePathConfigEx(String storageRule, String storageClass, String[] result)
/*      */   {
/*  648 */     Map pathConfig = null;
/*  649 */     Map storeMap = (Map)this.m_provider.getProviderObject("StorageRules");
/*  650 */     if (storeMap != null)
/*      */     {
/*  652 */       StorageRule storeObj = (StorageRule)storeMap.get(storageRule);
/*  653 */       Map pathConfigObj = storeObj.m_pathConfig;
/*      */ 
/*  656 */       int index = storageClass.length();
/*  657 */       while (index > 0)
/*      */       {
/*  659 */         storageClass = storageClass.substring(0, index);
/*  660 */         pathConfig = (Map)pathConfigObj.get(storageClass);
/*  661 */         if (pathConfig != null)
/*      */         {
/*  664 */           pathConfig = populatePathConfig(storeObj, pathConfig);
/*  665 */           break;
/*      */         }
/*  667 */         index = storageClass.lastIndexOf(46);
/*      */       }
/*  669 */       if ((result != null) && (pathConfig != null))
/*      */       {
/*  671 */         result[0] = storageClass;
/*      */       }
/*      */     }
/*      */ 
/*  675 */     return pathConfig;
/*      */   }
/*      */ 
/*      */   public Map populatePathConfig(StorageRule storeObj, Map pathConfig)
/*      */   {
/*  687 */     String[][] pathComponents = m_pathComponentReplaceDefaults;
/*      */ 
/*  689 */     for (int pathCompNo = 0; pathCompNo < pathComponents.length; ++pathCompNo)
/*      */     {
/*  691 */       String pathCompName = pathComponents[pathCompNo][0];
/*  692 */       String pathCompExpression = getPathExpression(storeObj, pathCompName);
/*      */ 
/*  695 */       if (pathCompExpression == null)
/*      */       {
/*  697 */         pathCompExpression = pathComponents[pathCompNo][1];
/*      */       }
/*      */ 
/*  700 */       pathConfig.put(pathCompName, pathCompExpression);
/*      */     }
/*      */ 
/*  703 */     return pathConfig;
/*      */   }
/*      */ 
/*      */   public String getDispersion(String storageRule)
/*      */   {
/*  708 */     String pathExpression = "";
/*      */ 
/*  710 */     Map storeMap = (Map)this.m_provider.getProviderObject("StorageRules");
/*  711 */     if (storeMap != null)
/*      */     {
/*  713 */       StorageRule storeObj = (StorageRule)storeMap.get(storageRule);
/*  714 */       pathExpression = getDispersion(storeObj);
/*      */     }
/*      */ 
/*  717 */     return pathExpression;
/*      */   }
/*      */ 
/*      */   public String getFsWeblayoutDir(StorageRule storeObj)
/*      */   {
/*  722 */     return getPathExpression(storeObj, "FsWeblayoutDir");
/*      */   }
/*      */ 
/*      */   public String getDispersion(StorageRule storeObj)
/*      */   {
/*  727 */     return getPathExpression(storeObj, "dispersion");
/*      */   }
/*      */ 
/*      */   public String getPathExpression(StorageRule storeObj, String storageClass)
/*      */   {
/*  732 */     Map pathConfig = null;
/*  733 */     String pathExpression = "";
/*  734 */     Map pathConfigObj = storeObj.m_pathConfig;
/*  735 */     pathConfig = (Map)pathConfigObj.get(storageClass);
/*      */ 
/*  737 */     if (pathConfig != null)
/*      */     {
/*  739 */       pathExpression = (String)pathConfig.get("PathExpression");
/*      */     }
/*      */ 
/*  742 */     return pathExpression;
/*      */   }
/*      */ 
/*      */   public Map determinePathConfig(String storageRule, String storageClass)
/*      */   {
/*  747 */     return determinePathConfigEx(storageRule, storageClass, null);
/*      */   }
/*      */ 
/*      */   public void registerEventFilter(FilterImplementor filter)
/*      */     throws ServiceException
/*      */   {
/*  753 */     this.m_eventImp.registerEventFilter(filter);
/*      */   }
/*      */ 
/*      */   public void renameOnEvent(String type, IdcFileDescriptor descriptor, Map data, ExecutionContext cxt)
/*      */     throws DataException, ServiceException, IOException
/*      */   {
/*  760 */     this.m_eventImp.renameOnEvent(type, descriptor, data, cxt);
/*      */   }
/*      */ 
/*      */   public void unregisterEventFilter(FilterImplementor filter) throws ServiceException
/*      */   {
/*  765 */     this.m_eventImp.unregisterEventFilter(filter);
/*      */   }
/*      */ 
/*      */   public List getEventFilters() throws ServiceException
/*      */   {
/*  770 */     return this.m_eventImp.getEventFilters();
/*      */   }
/*      */ 
/*      */   public void notifyOfEvent(IdcFileDescriptor descriptor, Map data, ExecutionContext cxt)
/*      */     throws DataException, ServiceException, IOException
/*      */   {
/*  776 */     this.m_eventImp.notifyOfEvent(descriptor, data, cxt);
/*      */   }
/*      */ 
/*      */   public String getConfigValue(String key, Map args, boolean useEnvironment)
/*      */   {
/*  783 */     String value = null;
/*  784 */     if (args != null)
/*      */     {
/*  786 */       value = (String)args.get(key);
/*      */     }
/*  788 */     if ((value == null) && (this.m_defaultArgs != null))
/*      */     {
/*  790 */       Object obj = this.m_defaultArgs.get(key);
/*  791 */       if (obj instanceof String)
/*      */       {
/*  793 */         value = (String)obj;
/*      */       }
/*      */     }
/*  796 */     if (value == null)
/*      */     {
/*  798 */       DataBinder providerData = this.m_provider.getProviderData();
/*  799 */       value = providerData.getAllowMissing(key);
/*      */     }
/*  801 */     if ((value == null) && (useEnvironment))
/*      */     {
/*  803 */       value = SharedObjects.getEnvironmentValue(key);
/*      */     }
/*  805 */     return value;
/*      */   }
/*      */ 
/*      */   public boolean getConfigBoolean(String key, Map args, boolean defaultValue, boolean useEnvironment)
/*      */   {
/*  811 */     String value = getConfigValue(key, args, useEnvironment);
/*  812 */     return StringUtils.convertToBool(value, defaultValue);
/*      */   }
/*      */ 
/*      */   public DataResultSet getConfigResultSet(String name)
/*      */   {
/*  817 */     DataResultSet drset = (DataResultSet)this.m_provider.getProviderData().getResultSet(name);
/*  818 */     if (drset != null)
/*      */     {
/*  820 */       drset = drset.shallowClone();
/*      */     }
/*  822 */     return drset;
/*      */   }
/*      */ 
/*      */   public void beginTransaction(int flags, ExecutionContext context)
/*      */     throws ServiceException
/*      */   {
/*      */     try
/*      */     {
/*  831 */       List list = new ArrayList();
/*  832 */       context.setCachedObject("FileStore:" + this.m_providerName + ":transactionList", list);
/*  833 */       PluginFilters.filter("beginTransaction", null, null, context);
/*  834 */       int size = list.size();
/*  835 */       for (int i = 0; i < size; ++i)
/*      */       {
/*  837 */         IdcTransactionListener listener = (IdcTransactionListener)list.get(i);
/*  838 */         listener.beginTransaction(flags, context);
/*      */       }
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/*  843 */       throw new ServiceException(e);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void commitTransaction(int flags, ExecutionContext context) throws ServiceException
/*      */   {
/*  849 */     List list = (List)context.getCachedObject("FileStore:" + this.m_providerName + ":commitLog");
/*      */     try
/*      */     {
/*  853 */       performTransactionActivity(list, false, context);
/*  854 */       performListenerActivity("commit", flags, context);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  858 */       throw new ServiceException("!csFsCommitError", e);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void rollbackTransaction(int flags, ExecutionContext context) throws ServiceException
/*      */   {
/*  864 */     List list = (List)context.getCachedObject("FileStore:" + this.m_providerName + ":rollbackLog");
/*      */     try
/*      */     {
/*  868 */       performTransactionActivity(list, true, context);
/*  869 */       performListenerActivity("rollback", flags, context);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*      */     }
/*      */     finally
/*      */     {
/*  878 */       list = (ArrayList)context.getCachedObject("FileStore:" + this.m_providerName + ":commitLog");
/*  879 */       if (list != null)
/*      */       {
/*  881 */         dumpTransactionObjects(list, "clear commit log");
/*  882 */         list.clear();
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void performListenerActivity(String action, int flags, ExecutionContext context)
/*      */     throws ServiceException
/*      */   {
/*  890 */     String id = "FileStore:" + this.m_providerName + ":transactionList";
/*  891 */     List list = (List)context.getCachedObject(id);
/*  892 */     if (list == null)
/*      */     {
/*  894 */       return;
/*      */     }
/*      */ 
/*  897 */     boolean isCommit = action.equals("commit");
/*  898 */     boolean isRollback = action.equals("rollback");
/*  899 */     int size = list.size();
/*  900 */     if (isRollback)
/*      */     {
/*  902 */       for (int i = size - 1; i != -1; --i)
/*      */       {
/*  904 */         IdcTransactionListener listener = (IdcTransactionListener)list.get(i);
/*  905 */         listener.rollbackTransaction(flags, context);
/*      */       }
/*      */ 
/*      */     }
/*      */     else {
/*  910 */       for (int i = 0; i < size; ++i)
/*      */       {
/*  912 */         IdcTransactionListener listener = (IdcTransactionListener)list.get(i);
/*  913 */         if (isCommit)
/*      */         {
/*  915 */           listener.commitTransaction(flags, context);
/*      */         }
/*      */         else
/*      */         {
/*  919 */           listener.closeTransactionListener(flags, context);
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/*  924 */     if ((isCommit) || (isRollback))
/*      */       return;
/*  926 */     list.clear();
/*      */   }
/*      */ 
/*      */   public void performTransactionActivity(List list, boolean isAllowDelete, ExecutionContext context)
/*      */     throws ServiceException, DataException, IOException
/*      */   {
/*  934 */     if (list == null)
/*      */     {
/*  936 */       return;
/*      */     }
/*      */ 
/*  940 */     int size = list.size();
/*  941 */     for (int i = size - 1; i >= 0; --i)
/*      */     {
/*  943 */       boolean isHandled = true;
/*  944 */       List opList = (List)list.get(i);
/*  945 */       String op = (String)opList.get(0);
/*  946 */       Object src = opList.get(1);
/*      */ 
/*  948 */       Map args = (Map)opList.get(3);
/*  949 */       if (args == null)
/*      */       {
/*  951 */         args = new HashMap();
/*      */       }
/*  953 */       args.put("isTransactionActivity", "1");
/*  954 */       if ((isAllowDelete) && (op.equals("delete")))
/*      */       {
/*  956 */         args.put("isOnlyFileDelete", "1");
/*  957 */         if (src instanceof IdcFileDescriptor)
/*      */         {
/*  959 */           IdcFileDescriptor descriptor = (IdcFileDescriptor)src;
/*  960 */           this.m_accessImp.deleteFile(descriptor, args, context);
/*      */         }
/*      */         else
/*      */         {
/*  964 */           String path = (String)src;
/*  965 */           Report.trace("filestore", "transaction: deleting " + path, null);
/*  966 */           FileUtils.deleteFile(path);
/*      */         }
/*      */       }
/*  969 */       else if (op.equals("rename"))
/*      */       {
/*  971 */         Object target = opList.get(2);
/*  972 */         if (src instanceof IdcFileDescriptor)
/*      */         {
/*  974 */           IdcFileDescriptor srcDesc = (IdcFileDescriptor)src;
/*  975 */           IdcFileDescriptor targetDesc = (IdcFileDescriptor)target;
/*  976 */           this.m_accessImp.moveFile(srcDesc, targetDesc, args, context);
/*      */         }
/*      */         else
/*      */         {
/*  980 */           String srcPath = (String)src;
/*  981 */           String targetPath = (String)target;
/*  982 */           boolean isCopyDelete = getConfigBoolean("FsIsCopyDeleteOnRename", args, false, true);
/*      */ 
/*  984 */           int flag = 0;
/*  985 */           if (isCopyDelete)
/*      */           {
/*  987 */             flag = 8;
/*      */           }
/*  989 */           FileUtils.renameFileEx(srcPath, targetPath, flag);
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/*  994 */         isHandled = false;
/*      */       }
/*      */ 
/*  997 */       dumpTransactionObject(opList, isHandled);
/*      */ 
/*  999 */       if (!isHandled)
/*      */         continue;
/* 1001 */       list.remove(i);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void closeTransactionListener(int flags, ExecutionContext context)
/*      */     throws ServiceException
/*      */   {
/* 1009 */     ArrayList list = (ArrayList)context.getCachedObject("FileStore:" + this.m_providerName + ":commitLog");
/*      */     try
/*      */     {
/* 1013 */       performTransactionActivity(list, true, context);
/* 1014 */       performListenerActivity("close", flags, context);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 1020 */       if (SystemUtils.m_verbose)
/*      */       {
/* 1022 */         Report.debug("filestore", "Close transaction failed.", e);
/*      */       }
/*      */     }
/*      */     finally
/*      */     {
/* 1027 */       if (list != null)
/*      */       {
/* 1030 */         dumpTransactionObjects(list, "clear left over commit log");
/* 1031 */         list.clear();
/*      */       }
/*      */ 
/* 1035 */       list = (ArrayList)context.getCachedObject("FileStore:" + this.m_providerName + ":rollbackLog");
/*      */ 
/* 1037 */       if (list != null)
/*      */       {
/* 1039 */         dumpTransactionObjects(list, "clear rollback log");
/* 1040 */         list.clear();
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void dumpTransactionObjects(List list, String action)
/*      */   {
/* 1047 */     if ((!SystemUtils.m_verbose) || (!SystemUtils.isActiveTrace("filestore")))
/*      */     {
/* 1049 */       return;
/*      */     }
/* 1051 */     int size = list.size();
/* 1052 */     Report.debug("filestore", "dumpTransactionObjects: " + action + " number=" + size, null);
/* 1053 */     if (size == 0)
/*      */     {
/* 1055 */       return;
/*      */     }
/*      */ 
/* 1058 */     for (int i = 0; i < size; ++i)
/*      */     {
/* 1060 */       List opList = (List)list.get(i);
/* 1061 */       dumpTransactionObject(opList, false);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void dumpTransactionObject(List list, boolean isHandled)
/*      */   {
/* 1067 */     if ((!SystemUtils.m_verbose) || (!SystemUtils.isActiveTrace("filestore")))
/*      */     {
/* 1069 */       return;
/*      */     }
/* 1071 */     String op = (String)list.get(0);
/* 1072 */     Object src = list.get(1);
/* 1073 */     Object target = list.get(2);
/*      */ 
/* 1075 */     String srcPath = null;
/* 1076 */     String targetPath = null;
/* 1077 */     if (src instanceof IdcFileDescriptor)
/*      */     {
/* 1079 */       srcPath = ((IdcFileDescriptor)src).getProperty("path");
/* 1080 */       if (target != null)
/*      */       {
/* 1082 */         targetPath = ((IdcFileDescriptor)target).getProperty("path");
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/* 1087 */       srcPath = (String)src;
/* 1088 */       targetPath = (String)target;
/*      */     }
/* 1090 */     Report.debug("filestore", "performTransactionActivity: handled=" + isHandled + "  action=" + op + "  sourcePath=" + srcPath + "  targetPath=" + targetPath, null);
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1097 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 100026 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.filestore.BaseFileStore
 * JD-Core Version:    0.5.4
 */