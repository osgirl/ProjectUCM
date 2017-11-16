/*     */ package intradoc.filestore.filesystem;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.filestore.CommonStoreImplementor;
/*     */ import intradoc.filestore.FileStoreProvider;
/*     */ import intradoc.provider.Provider;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.util.Hashtable;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class WebLocationParser
/*     */   implements CommonStoreImplementor
/*     */ {
/*  32 */   public static String m_dispersionEndMarker = "~edisp";
/*     */   protected FileStoreProvider m_fileStore;
/*     */   protected Map m_dirMap;
/*     */ 
/*     */   public void preInit(FileStoreProvider fs, Provider provider)
/*     */   {
/*  39 */     this.m_fileStore = fs;
/*     */   }
/*     */ 
/*     */   public void init(FileStoreProvider fs, Provider provider)
/*     */   {
/*  45 */     this.m_dirMap = new Hashtable();
/*     */ 
/*  48 */     this.m_dirMap.put("sg", "d");
/*     */ 
/*  51 */     DataBinder binder = provider.getProviderData();
/*  52 */     DataResultSet drset = (DataResultSet)binder.getResultSet("WebParsingGuide");
/*  53 */     if (drset == null)
/*     */       return;
/*     */     try
/*     */     {
/*  57 */       FieldInfo[] fis = ResultSetUtils.createInfoList(drset, new String[] { "startDir", "endDir" }, true);
/*     */ 
/*  60 */       int startIndex = fis[0].m_index;
/*  61 */       int endIndex = fis[1].m_index;
/*  62 */       for (drset.first(); drset.isRowPresent(); drset.next())
/*     */       {
/*  64 */         String startDir = drset.getStringValue(startIndex);
/*  65 */         String endDir = drset.getStringValue(endIndex);
/*  66 */         this.m_dirMap.put(startDir, endDir);
/*     */       }
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/*  71 */       Report.trace(null, "Unable to initialize the weblocation parser with the guide.", e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean parseDocInfoFromPath(String relPath, Map revProps, ExecutionContext cxt)
/*     */     throws DataException, ServiceException
/*     */   {
/*  80 */     boolean result = false;
/*  81 */     List segments = StringUtils.makeListFromSequence(relPath, '/', '^', 0);
/*     */ 
/*  85 */     int[] pathIndex = new int[1];
/*  86 */     pathIndex[0] = 0;
/*     */ 
/*  89 */     result = parseSecurity(segments, revProps, cxt, pathIndex);
/*  90 */     int startIndex = pathIndex[0];
/*  91 */     int size = segments.size();
/*  92 */     if ((!result) || (startIndex > size - 2))
/*     */     {
/*  98 */       return result;
/*     */     }
/*     */ 
/* 102 */     String startDir = (String)segments.get(startIndex);
/* 103 */     String endDir = null;
/* 104 */     boolean isFound = false;
/* 105 */     if (startDir.equals("documents"))
/*     */     {
/* 109 */       ++startIndex;
/* 110 */       String type = (String)segments.get(startIndex);
/* 111 */       revProps.put("dDocType", type);
/* 112 */       ++startIndex;
/* 113 */       isFound = true;
/*     */     }
/*     */     else
/*     */     {
/* 117 */       endDir = (String)this.m_dirMap.get(startDir);
/* 118 */       if (endDir == null)
/*     */       {
/* 121 */         endDir = "d";
/*     */       }
/*     */ 
/* 126 */       for (int i = startIndex + 1; i < size; ++i)
/*     */       {
/* 128 */         String segment = (String)segments.get(i);
/* 129 */         if (!segment.equals(endDir))
/*     */           continue;
/* 131 */         startIndex = ++i;
/* 132 */         isFound = true;
/* 133 */         break;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 138 */     if ((isFound) && (startIndex < size))
/*     */     {
/* 141 */       startIndex = parseDispersion(segments, startIndex, revProps);
/*     */ 
/* 143 */       String nextSegment = (String)segments.get(startIndex);
/*     */ 
/* 145 */       if (nextSegment.startsWith("~"))
/*     */       {
/* 148 */         List subPath = segments.subList(startIndex, size);
/* 149 */         String tail = StringUtils.createStringRemoveEmpty(subPath, '/', '^');
/* 150 */         revProps.put("conversionPathSuffix", tail);
/* 151 */         ++startIndex;
/*     */       }
/*     */     }
/* 154 */     if ((!isFound) || (startIndex >= size))
/*     */     {
/* 158 */       segments = segments.subList(startIndex, size);
/* 159 */       String tail = StringUtils.createStringRemoveEmpty(segments, '/', '^');
/* 160 */       String patterns = SharedObjects.getEnvironmentValue("WebAllowedSuffixes");
/* 161 */       if (patterns == null)
/*     */       {
/* 163 */         patterns = "logs/*";
/*     */       }
/* 165 */       boolean isOK = StringUtils.matchEx(tail, patterns, true, true);
/* 166 */       if (!isOK)
/*     */       {
/* 169 */         String errMsg = LocaleUtils.encodeMessage("csFsMissingDispersion", null, endDir, startDir, relPath);
/*     */ 
/* 171 */         throw new ServiceException(errMsg);
/*     */       }
/* 173 */       Report.trace("filestore", "Allow " + relPath + " to be delivered. It matched " + " a WebAllowedSuffixes pattern: " + patterns, null);
/*     */ 
/* 175 */       result = false;
/* 176 */       revProps.put("pathExtension", "");
/* 177 */       revProps.put("pathTail", "");
/*     */     }
/*     */     else
/*     */     {
/* 182 */       parseDocName(segments, startIndex, revProps);
/*     */     }
/* 184 */     return result;
/*     */   }
/*     */ 
/*     */   public boolean parseSecurity(List segments, Map revProps, ExecutionContext cxt, int[] resultObj)
/*     */     throws DataException, ServiceException
/*     */   {
/* 191 */     boolean result = false;
/* 192 */     int size = segments.size();
/* 193 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 195 */       String segment = (String)segments.get(i);
/* 196 */       int index = segment.indexOf("groups");
/* 197 */       if (index > 0)
/*     */       {
/* 200 */         char ch = segment.charAt(index - 1);
/* 201 */         if ((ch != '>') && (ch != ']') && (ch != '\\') && (ch != '/')) {
/*     */           continue;
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 207 */       if (index < 0)
/*     */         continue;
/* 209 */       ++i;
/* 210 */       String group = (String)segments.get(i);
/* 211 */       revProps.put("dSecurityGroup", group);
/* 212 */       int r = parseAccount(segments, i, revProps);
/* 213 */       resultObj[0] = r;
/* 214 */       result = true;
/* 215 */       break;
/*     */     }
/*     */ 
/* 218 */     return result;
/*     */   }
/*     */ 
/*     */   public int parseAccount(List pathVector, int startIndex, Map revProps)
/*     */     throws DataException, ServiceException
/*     */   {
/* 226 */     IdcStringBuilder buff = new IdcStringBuilder(50);
/* 227 */     int size = pathVector.size();
/* 228 */     for (int i = startIndex + 1; i < size; ++i)
/*     */     {
/* 230 */       String val = (String)pathVector.get(i);
/* 231 */       if (val.length() <= 0)
/*     */         continue;
/* 233 */       if (val.charAt(0) == '@')
/*     */       {
/* 235 */         if (buff.length() > 0)
/*     */         {
/* 237 */           buff.append('/');
/*     */         }
/* 239 */         buff.append(val, 1, val.length() - 1);
/*     */       }
/*     */       else
/*     */       {
/* 243 */         startIndex = i;
/* 244 */         break;
/*     */       }
/*     */     }
/*     */ 
/* 248 */     revProps.put("dDocAccount", buff.toString());
/* 249 */     return startIndex;
/*     */   }
/*     */ 
/*     */   public int parseDispersion(List segments, int startIndex, Map revProps)
/*     */   {
/* 263 */     int indexOfDispEnd = -1;
/*     */ 
/* 265 */     for (int segNo = startIndex; segNo < segments.size() - 1; ++segNo)
/*     */     {
/* 267 */       String segment = (String)segments.get(segNo);
/* 268 */       if (!segment.equalsIgnoreCase(m_dispersionEndMarker))
/*     */         continue;
/* 270 */       indexOfDispEnd = segNo;
/* 271 */       break;
/*     */     }
/*     */ 
/* 276 */     if (indexOfDispEnd != -1)
/*     */     {
/* 278 */       startIndex = indexOfDispEnd + 1;
/*     */     }
/*     */ 
/* 281 */     return startIndex;
/*     */   }
/*     */ 
/*     */   public void parseDocName(List pathVector, int startIndex, Map props)
/*     */   {
/* 289 */     String fileName = (String)pathVector.get(startIndex);
/*     */ 
/* 291 */     String ext = "";
/* 292 */     String docNamePart = fileName;
/* 293 */     int index = fileName.lastIndexOf(46);
/* 294 */     if (index >= 0)
/*     */     {
/* 296 */       ext = fileName.substring(index + 1);
/* 297 */       docNamePart = fileName.substring(0, index);
/*     */     }
/*     */ 
/* 300 */     index = docNamePart.indexOf(126);
/* 301 */     String docName = docNamePart;
/*     */ 
/* 303 */     boolean isLatestRevision = index < 0;
/* 304 */     if (!isLatestRevision)
/*     */     {
/* 306 */       docName = docNamePart.substring(0, index);
/* 307 */       String revLabel = docNamePart.substring(index + 1);
/*     */ 
/* 310 */       index = revLabel.indexOf(126);
/* 311 */       if (index > 0)
/*     */       {
/* 313 */         revLabel = revLabel.substring(0, index);
/*     */       }
/*     */ 
/* 316 */       index = docName.indexOf(64);
/* 317 */       if (index >= 0)
/*     */       {
/* 319 */         String renFlag = docName.substring(index + 1, index + 2);
/* 320 */         props.put("renFlag", renFlag.toUpperCase());
/* 321 */         docName = docName.substring(0, index);
/*     */       }
/* 323 */       props.put("dRevLabel", revLabel);
/*     */     }
/* 325 */     props.put("isLatestRevision", (isLatestRevision) ? "1" : "");
/* 326 */     props.put("dDocName", docName);
/* 327 */     props.put("dExtension", ext);
/* 328 */     props.put("hasDocInfo", "1");
/*     */ 
/* 333 */     int size = pathVector.size();
/* 334 */     ++startIndex;
/* 335 */     if (startIndex < size)
/*     */     {
/* 340 */       String str = (String)pathVector.get(size - 1);
/* 341 */       index = str.indexOf(46);
/* 342 */       if (index >= 0)
/*     */       {
/* 344 */         ext = str.substring(index + 1);
/* 345 */         props.put("dExtension", ext);
/* 346 */         props.put("pathExtension", ext);
/*     */       }
/* 348 */       props.put("pathTail", str);
/*     */     }
/*     */     else
/*     */     {
/* 352 */       props.put("pathExtension", "");
/* 353 */       props.put("pathTail", "");
/*     */     }
/*     */   }
/*     */ 
/*     */   public String getType()
/*     */   {
/* 360 */     return "LocationParser";
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 367 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 105014 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.filestore.filesystem.WebLocationParser
 * JD-Core Version:    0.5.4
 */