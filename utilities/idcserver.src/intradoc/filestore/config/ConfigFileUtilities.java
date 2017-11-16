/*     */ package intradoc.filestore.config;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.PropParameters;
/*     */ import intradoc.filestore.IdcFileDescriptor;
/*     */ import intradoc.provider.Provider;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.shared.ComponentClassFactory;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.OutputStream;
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ 
/*     */ public class ConfigFileUtilities
/*     */ {
/*     */   public ConfigFileStore m_CFS;
/*     */   public ExecutionContext m_context;
/*     */   protected HashMap m_savedArgMaps;
/*     */ 
/*     */   public ConfigFileUtilities()
/*     */   {
/*  58 */     this.m_savedArgMaps = new HashMap();
/*     */   }
/*     */ 
/*     */   public static ConfigFileUtilities createConfigFileUtilities(ConfigFileStore cfs, ExecutionContext cxt)
/*     */     throws ServiceException
/*     */   {
/*  74 */     ConfigFileUtilities utils = (ConfigFileUtilities)ComponentClassFactory.createClassInstance("ConfigFileUtilities", "intradoc.filestore.config.ConfigFileUtilities", null);
/*     */ 
/*  77 */     if (null == cfs)
/*     */     {
/*  79 */       cfs = ConfigFileStore.lookupConfigFileStoreMustExist(null);
/*     */     }
/*  81 */     utils.m_CFS = cfs;
/*  82 */     if (null == cxt)
/*     */     {
/*  84 */       cxt = ConfigFileLoader.m_defaultCFU.m_context;
/*     */     }
/*  86 */     utils.m_context = cxt;
/*  87 */     cxt.setCachedObject("ConfigFileUtilities", utils);
/*  88 */     return utils;
/*     */   }
/*     */ 
/*     */   public static ConfigFileUtilities createConfigFileUtilitiesByName(String id, ExecutionContext cxt)
/*     */     throws ServiceException
/*     */   {
/* 102 */     ConfigFileStore cfs = ConfigFileStore.lookupConfigFileStoreMustExist(id);
/* 103 */     ConfigFileUtilities utils = createConfigFileUtilities(cfs, cxt);
/* 104 */     return utils;
/*     */   }
/*     */ 
/*     */   public static ConfigFileUtilities getOrCreateConfigFileUtilitiesForExecutionContext(ExecutionContext cxt)
/*     */     throws ServiceException
/*     */   {
/* 118 */     ConfigFileUtilities cfu = null;
/* 119 */     if (null != cxt)
/*     */     {
/* 121 */       cfu = (ConfigFileUtilities)cxt.getCachedObject("ConfigFileUtilities");
/*     */     }
/* 123 */     if (null == cfu)
/*     */     {
/* 125 */       cfu = createConfigFileUtilities(null, cxt);
/*     */     }
/* 127 */     return cfu;
/*     */   }
/*     */ 
/*     */   public Map createMapFromOptionsList(List options)
/*     */     throws DataException
/*     */   {
/* 148 */     HashMap args = new HashMap();
/* 149 */     int numOptions = options.size();
/* 150 */     for (int i = 0; i < numOptions; ++i)
/*     */     {
/* 152 */       String option = (String)options.get(i);
/* 153 */       String value = null;
/* 154 */       boolean defaultBoolean = '!' != option.charAt(0);
/* 155 */       int equalsIndex = option.indexOf(61, (defaultBoolean) ? 0 : 1);
/* 156 */       if ((equalsIndex >= 0) && (!defaultBoolean))
/*     */       {
/* 158 */         String msg = LocaleUtils.encodeMessage("csCFSInvalidOptionSpecification", null, option);
/*     */ 
/* 160 */         throw new DataException(msg);
/*     */       }
/* 162 */       if (equalsIndex >= 0)
/*     */       {
/* 164 */         CharSequence seq = option.substring(equalsIndex + 1);
/* 165 */         value = seq.toString();
/* 166 */         seq = option.substring(0, equalsIndex);
/* 167 */         option = seq.toString();
/*     */       }
/* 169 */       else if (defaultBoolean)
/*     */       {
/* 171 */         value = "1";
/*     */       }
/*     */       else
/*     */       {
/* 175 */         CharSequence seq = option.substring(1);
/* 176 */         option = seq.toString();
/* 177 */         value = "0";
/*     */       }
/* 179 */       args.put(option, value);
/*     */     }
/*     */ 
/* 182 */     return args;
/*     */   }
/*     */ 
/*     */   public Map createMapFromOptionsString(String options, char delimiter)
/*     */     throws DataException
/*     */   {
/* 200 */     Map args = (Map)this.m_savedArgMaps.get(options);
/* 201 */     if (null != args)
/*     */     {
/* 203 */       return args;
/*     */     }
/*     */ 
/* 207 */     ArrayList list = new ArrayList();
/*     */ 
/* 209 */     int start = 0;
/* 210 */     while ((stop = options.indexOf(delimiter, start)) >= 0)
/*     */     {
/*     */       int stop;
/* 213 */       if (start != stop)
/*     */       {
/* 217 */         CharSequence seq = options.substring(start, stop);
/* 218 */         String option = seq.toString();
/* 219 */         list.add(option);
/*     */       }
/* 211 */       start = stop + 1;
/*     */     }
/*     */ 
/* 221 */     if (start < options.length())
/*     */     {
/* 223 */       CharSequence seq = options.substring(start);
/* 224 */       String option = seq.toString();
/* 225 */       list.add(option);
/*     */     }
/*     */ 
/* 229 */     args = createMapFromOptionsList(list);
/* 230 */     this.m_savedArgMaps.put(options, args);
/* 231 */     return args;
/*     */   }
/*     */ 
/*     */   public ServerData getServerData()
/*     */   {
/* 245 */     ServerData serverData = (ServerData)this.m_CFS.m_provider.getProviderObject("ServerData");
/* 246 */     return serverData;
/*     */   }
/*     */ 
/*     */   public void reprocessKeyPathPairsFromEnv(Properties props)
/*     */     throws ServiceException
/*     */   {
/* 252 */     ServerData serverData = getServerData();
/*     */ 
/* 254 */     serverData.m_binder.setEnvironment((Properties)props.clone());
/* 255 */     serverData.reprocessKeyPathStrings();
/*     */   }
/*     */ 
/*     */   public IdcFileDescriptor createDescriptorByName(String pathname, Map args)
/*     */     throws DataException, ServiceException
/*     */   {
/* 272 */     Properties metadata = new Properties();
/* 273 */     metadata.setProperty("filename", pathname);
/* 274 */     PropParameters params = new PropParameters(metadata);
/* 275 */     IdcFileDescriptor desc = this.m_CFS.createDescriptor(params, args, this.m_context);
/* 276 */     return desc;
/*     */   }
/*     */ 
/*     */   public String getFilesystemPathByName(String pathname)
/*     */     throws DataException, ServiceException
/*     */   {
/* 290 */     IdcFileDescriptor descriptor = createDescriptorByName(pathname, null);
/* 291 */     String path = this.m_CFS.getFilesystemPath(descriptor, this.m_context);
/* 292 */     return path;
/*     */   }
/*     */ 
/*     */   public String getStorageDataAttribute(IdcFileDescriptor descriptor, String attributeName, ExecutionContext cxt)
/*     */     throws DataException, ServiceException, IOException
/*     */   {
/* 306 */     Map storageData = this.m_CFS.getStorageData(descriptor, null, cxt);
/* 307 */     Object attribute = storageData.get(attributeName);
/* 308 */     if (null == attribute)
/*     */     {
/* 310 */       String pathname = this.m_CFS.getFilesystemPath(descriptor, this.m_context);
/* 311 */       String msg = LocaleUtils.encodeMessage("csCFSMissingStorageDataAttribute", null, attributeName, pathname);
/*     */ 
/* 313 */       throw new ServiceException(msg);
/*     */     }
/* 315 */     if (attribute instanceof String)
/*     */     {
/* 317 */       return (String)attribute;
/*     */     }
/* 319 */     return attribute.toString();
/*     */   }
/*     */ 
/*     */   public String getStorageDataAttributeByName(String pathname, String attributeName)
/*     */     throws DataException, ServiceException, IOException
/*     */   {
/* 332 */     IdcFileDescriptor descriptor = createDescriptorByName(pathname, null);
/* 333 */     Map storageData = this.m_CFS.getStorageData(descriptor, null, null);
/* 334 */     Object attribute = storageData.get(attributeName);
/* 335 */     if (null == attribute)
/*     */     {
/* 337 */       String msg = LocaleUtils.encodeMessage("csCFSMissingStorageDataAttribute", null, attributeName, pathname);
/*     */ 
/* 339 */       throw new ServiceException(msg);
/*     */     }
/* 341 */     if (attribute instanceof String)
/*     */     {
/* 343 */       return (String)attribute;
/*     */     }
/* 345 */     return attribute.toString();
/*     */   }
/*     */ 
/*     */   public OutputStream getOutputStreamByName(String pathname, Map args)
/*     */     throws DataException, ServiceException, IOException
/*     */   {
/* 360 */     IdcFileDescriptor descriptor = createDescriptorByName(pathname, null);
/* 361 */     OutputStream stream = this.m_CFS.getOutputStream(descriptor, args);
/* 362 */     return stream;
/*     */   }
/*     */ 
/*     */   public InputStream getInputStreamByName(String pathname, Map args)
/*     */     throws DataException, ServiceException, IOException
/*     */   {
/* 377 */     IdcFileDescriptor descriptor = createDescriptorByName(pathname, null);
/* 378 */     InputStream stream = this.m_CFS.getInputStream(descriptor, args);
/* 379 */     return stream;
/*     */   }
/*     */ 
/*     */   public boolean readDataBinderFromName(String pathname, DataBinder binder, Map args)
/*     */     throws DataException, ServiceException, IOException
/*     */   {
/* 395 */     IdcFileDescriptor descriptor = createDescriptorByName(pathname, null);
/* 396 */     InputStream stream = null;
/* 397 */     boolean result = false;
/*     */     try
/*     */     {
/* 400 */       stream = this.m_CFS.getInputStream(descriptor, args);
/* 401 */       if (null != stream)
/*     */       {
/* 403 */         result = true;
/* 404 */         ResourceUtils.readDataBinderFromStream(stream, binder, 0, getFilesystemPathByName(pathname));
/*     */       }
/*     */     }
/*     */     finally
/*     */     {
/* 409 */       if (stream != null)
/*     */       {
/* 411 */         stream.close();
/*     */       }
/*     */     }
/* 414 */     return result;
/*     */   }
/*     */ 
/*     */   public void writeDataBinderToName(String pathname, DataBinder binder, Map args)
/*     */     throws DataException, ServiceException, IOException
/*     */   {
/* 426 */     IdcFileDescriptor descriptor = createDescriptorByName(pathname, null);
/* 427 */     OutputStream stream = this.m_CFS.getOutputStream(descriptor, args);
/*     */     try
/*     */     {
/* 430 */       ResourceUtils.writeDataBinderToStream(stream, binder, 0, getFilesystemPathByName(pathname));
/*     */     }
/*     */     finally
/*     */     {
/* 434 */       FileUtils.closeObject(stream);
/*     */     }
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static String getComputedPath(String pathname)
/*     */     throws ServiceException
/*     */   {
/* 445 */     return getComputedPathFor(null, pathname);
/*     */   }
/*     */ 
/*     */   public static String getComputedPathFor(String servername, String pathname)
/*     */     throws ServiceException
/*     */   {
/* 459 */     ConfigFileStore cfs = ConfigFileStore.lookupConfigFileStoreMustExist(servername);
/* 460 */     ConfigMetadataImplementor md = (ConfigMetadataImplementor)cfs.getImplementor("MetadataImplementor");
/* 461 */     pathname = md.m_server.computeActualPathname(pathname);
/* 462 */     return pathname;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 468 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97029 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.filestore.config.ConfigFileUtilities
 * JD-Core Version:    0.5.4
 */