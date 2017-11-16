package intradoc.filestore;

import intradoc.data.DataBinder;
import intradoc.data.DataException;
import intradoc.provider.ProviderInterface;
import java.util.Map;

public abstract interface FileStoreProvider extends ProviderInterface, FileStoreEventImplementor, FileStoreMetadataImplementor, FileStoreAccessImplementor, FileStoreDescriptorImplementor
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99299 $";
  public static final String FS_PROVIDER_TYPE = "FileStore";
  public static final String FS_VERSION_INFO = "version_info";
  public static final String F_IGNORE_SOFT_ERRORS = "FileStoreIgnoreSoftErrors";
  public static final String F_OPEN_ERRORS = "OpenErrors";
  public static final String F_DELETE_PURGE = "purge";
  public static final String F_NEW_FILE = "isNew";
  public static final String F_IS_MOVE = "isMove";
  public static final String F_IS_RETAIN_METADATA = "isRetainMetadata";
  public static final String F_IS_CONTAINER = "isContainer";
  public static final String F_DO_CONTAINER_PATH = "doContainerPath";
  public static final String F_IS_UNMANAGED = "isUnmanaged";
  public static final String F_ADD_TO_CACHE_ONLY = "addNewToCache";
  public static final String F_COMPUTE_LAST_MODIFIED = "computeLastModified";
  public static final String F_FORCE_OVERWRITE = "isForce";
  public static final String F_MAKE_BACKUP = "isDoBackup";
  public static final String F_USE_TEMPFILE = "isDoTemp";
  public static final String F_WAS_CLOSED = "isClosed";
  public static final String F_CREATE_CONTAINERS = "doCreateContainers";
  public static final String F_NOT_FILESTORE_FILE_CREATE = "isNotFileStoreCreate";
  public static final String F_LOCAL_IN_PLACE = "localInPlace";
  public static final String F_FORCE_NO_LINK = "forceNoLink";
  public static final String F_IS_LOCATION_ONLY = "isLocationOnly";
  public static final String F_USE_ABSOLUTE = "useAbsolute";
  public static final String F_DO_SUPPORT_FALLBACK_PATH = "FileStoreSupportFallbackPath";
  public static final String F_HANDLED_FALLBACK_PATH = "FileStoreHandledFallbackPath";
  public static final String FS_USES_SUPPLEMENTAL_ID = "uses_supplemental_id";
  public static final String FS_USES_AUTHORATATIVE_ID = "uses_authoratative_id";
  public static final String FS_REQUIRES_PROVIDED_ID = "requires_provided_id";
  public static final String FS_USES_VOLATILE_ID = "uses_volatile_id";
  public static final String FS_FILESYSTEM_WRAPPER = "uses_filesystem";
  public static final String FS_FILESYSTEM_LEGACY = "legacy_compatible";
  public static final String FS_CONSTRUCTS_URLS = "constructs_urls";
  public static final String FS_STORES_METADATA = "stores_metadata";
  public static final String FS_EFFICIENT_MOVE = "efficient_move";
  public static final String FS_EFFICIENT_DUP = "efficient_dup";
  public static final String FS_PURGE_SUPPORTED = "can_purge";
  public static final String FS_IS_VOLATILE = "is_volatile";
  public static final String FS_SUPPORTS_CHANGE_FILTER = "supports_change_filter";
  public static final String FS_IS_QUERYABLE = "is_queryable";
  public static final String FS_IS_ENUMERABLE = "is_enumerable";
  public static final int FS_USES_FILESYSTEM_ONLY = 1;
  public static final int FS_IS_LEGACY = 2;
  public static final String FS_STORAGE_CLASSES = "storage_classes";
  public static final String SP_RENDITION_ID = "RenditionId";
  public static final String SP_RENDITION_PATH = "RenditionId.path";
  public static final String SP_STORAGE_CLASS = "StorageClass";
  public static final String SP_EVENT_TYPE = "EventType";
  public static final String SP_FILE_EXISTS = "fileExists";
  public static final String SP_FILE_SIZE = "fileSize";
  public static final String SP_PATH = "path";
  public static final String SP_LAST_MODIFIED = "lastModified";
  public static final String SP_IS_READABLE = "canRead";
  public static final String SP_IS_WRITEABLE = "canWrite";
  public static final String SP_IS_CONTAINER = "isContainer";
  public static final String SP_UNIQUE_ID = "uniqueId";
  public static final String M_ORIGINAL_DATA = "originalData";
  public static final String R_PRIMARY = "primaryFile";
  public static final String R_WEB = "webViewableFile";
  public static final String R_ALTERNATE = "alternateFile";
  public static final String R_DIRECTORY = "D";
  public static final String R_SUBRENDITION_PREFIX = "rendition";
  public static final String R_WEBINDEX = "webIndex";
  public static final String R_WEBRESOURCE = "webResource";
  public static final String SC_WEB = "web";
  public static final String SC_WEBURL = "weburl";
  public static final String SC_VAULT = "vault";
  public static final String SC_DATA = "data";
  public static final String SC_CONFIG = "config";
  public static final String SC_WEBINDEX = "webIndex";
  public static final String E_NOTIFY_RELEASED = "event_released";
  public static final String E_NOTIFY_UNRELEASED = "event_unreleased";

  public abstract Map getCapabilities(IdcFileDescriptor paramIdcFileDescriptor)
    throws DataException;

  public abstract Map getCapabilities(IdcFileDescriptor paramIdcFileDescriptor1, IdcFileDescriptor paramIdcFileDescriptor2)
    throws DataException;

  public abstract boolean hasFeature(int paramInt);

  public abstract DataBinder getProviderData();

  public abstract Object getImplementor(String paramString);
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.filestore.FileStoreProvider
 * JD-Core Version:    0.5.4
 */