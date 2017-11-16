/*     */ package intradoc.loader;
/*     */ 
/*     */ import intradoc.io.IdcByteHandler;
/*     */ import intradoc.io.IdcRandomAccessByteFile;
/*     */ import intradoc.io.zip.IdcZipEntry;
/*     */ import intradoc.io.zip.IdcZipEnvironment;
/*     */ import intradoc.io.zip.IdcZipException;
/*     */ import intradoc.io.zip.IdcZipFile;
/*     */ import intradoc.io.zip.IdcZipUtils;
/*     */ import java.io.File;
/*     */ import java.io.FileNotFoundException;
/*     */ import java.io.IOException;
/*     */ import java.net.URL;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Set;
/*     */ import java.util.concurrent.ConcurrentHashMap;
/*     */ 
/*     */ public class IdcLoaderElementList extends IdcLoaderElement
/*     */ {
/*     */   public IdcLoaderElement[] m_elements;
/*     */   protected Map<String, IdcLoaderElement> m_elementsByName;
/*     */ 
/*     */   public IdcLoaderElementList(IdcLoader loader)
/*     */   {
/*  70 */     this.m_loader = loader;
/*  71 */     this.m_zipenv = loader.getZipEnvironment();
/*     */   }
/*     */ 
/*     */   public void initElements(List<String> pathList)
/*     */   {
/*  88 */     int numPaths = pathList.size();
/*  89 */     if ((numPaths < 1) && (this.m_zipenv.m_verbosity >= 1))
/*     */     {
/*  91 */       this.m_loader.report(1, new Object[] { "no paths specified" });
/*     */     }
/*     */ 
/*  94 */     int numElements = 1;
/*  95 */     for (int tmpPaths = numPaths; tmpPaths > 0; tmpPaths >>= 1)
/*     */     {
/*  97 */       numElements <<= 1;
/*     */     }
/*  99 */     this.m_elements = new IdcLoaderElement[numElements];
/* 100 */     this.m_elementsByName = new ConcurrentHashMap(numElements);
/*     */ 
/* 104 */     int i = 0; for (int j = 0; i < numPaths; ++i)
/*     */     {
/* 106 */       String pathname = (String)pathList.get(i);
/*     */       try
/*     */       {
/* 109 */         IdcLoaderElement element = makePathElement(pathname);
/* 110 */         if (this.m_elementsByName.containsKey(element.m_entryPath))
/*     */         {
/* 112 */           if (this.m_zipenv.m_verbosity >= 6)
/*     */           {
/* 114 */             this.m_loader.report(6, new Object[] { "duplicate path element \"", pathname, "\", ignoring" });
/*     */           }
/*     */         }
/*     */         else
/*     */         {
/* 119 */           element.m_loadOrder = this.m_loadOrder;
/* 120 */           this.m_elements[(j++)] = element;
/* 121 */           this.m_elementsByName.put(element.m_entryPath, element);
/*     */         }
/*     */       }
/*     */       catch (Exception e) {
/* 125 */         if (this.m_zipenv.m_verbosity >= 4)
/*     */         {
/* 127 */           String msg = e.getMessage();
/* 128 */           if ((this.m_zipenv.m_verbosity > 5) || (msg == null))
/*     */           {
/* 130 */             this.m_loader.report(4, new Object[] { "unable to use path element \"", pathname, "\" (ignoring): ", e });
/*     */           }
/*     */           else
/*     */           {
/* 134 */             this.m_loader.report(4, new Object[] { e.getMessage(), ": unable to use path element \"", pathname, "\" (ignoring)" });
/*     */           }
/*     */         }
/*     */       }
/*     */     }
/* 139 */     if (((this.m_elements.length != 0) && (null != this.m_elements[0])) || 
/* 141 */       (this.m_zipenv.m_verbosity < 1))
/*     */       return;
/* 143 */     this.m_loader.report(1, new Object[] { "no valid paths found" });
/*     */   }
/*     */ 
/*     */   protected IdcLoaderElement makePathElement(String pathname)
/*     */     throws Exception
/*     */   {
/* 158 */     if (this.m_zipenv.m_verbosity >= 7)
/*     */     {
/* 160 */       this.m_loader.report(7, new Object[] { "makePathElement(\"", pathname, "\"" });
/*     */     }
/* 162 */     boolean doPreload = this.m_zipenv.m_doPreload;
/*     */ 
/* 165 */     pathname = IdcLoaderUtils.fixupPath(pathname);
/*     */ 
/* 167 */     File file = new File(pathname);
/* 168 */     if (file.isDirectory())
/*     */     {
/* 170 */       if (this.m_zipenv.m_verbosity >= 6)
/*     */       {
/* 172 */         this.m_loader.report(6, new Object[] { "using directory path element \"", pathname, "\"" });
/*     */       }
/* 174 */       return new IdcLoaderDirectoryElement(this.m_loader, file);
/*     */     }
/* 176 */     String innerPath = "";
/* 177 */     if (!file.exists())
/*     */     {
/* 180 */       char[] path = pathname.toCharArray();
/* 181 */       String prefix = pathname;
/* 182 */       int slashIndex = path.length;
/*     */       do { do if (--slashIndex <= 0)
/*     */             break label234;
/* 185 */         while (('/' != path[slashIndex]) && ('\\' != path[slashIndex]));
/*     */ 
/* 189 */         prefix = new String(path, 0, slashIndex);
/* 190 */         file = new File(prefix); }
/* 191 */       while (!file.exists());
/*     */ 
/* 198 */       if (slashIndex < 0)
/*     */       {
/* 200 */         label234: throw new FileNotFoundException(pathname);
/*     */       }
/* 202 */       if (!file.isFile())
/*     */       {
/* 204 */         throw new IOException(new StringBuilder().append("!syPathInvalid,").append(pathname).toString());
/*     */       }
/* 206 */       innerPath = new String(path, slashIndex + 1, path.length - slashIndex - 1);
/*     */     }
/*     */ 
/* 210 */     String canonicalName = file.getCanonicalPath();
/* 211 */     Map zipfiles = this.m_loader.getZipfiles();
/* 212 */     IdcZipFile zip = (IdcZipFile)zipfiles.get(canonicalName);
/* 213 */     if (null == zip)
/*     */     {
/* 215 */       int flags = 1;
/* 216 */       if (doPreload)
/* 217 */         flags |= 1048576;
/* 218 */       IdcRandomAccessByteFile handler = new IdcRandomAccessByteFile(file, flags);
/* 219 */       zip = new IdcZipFile(handler);
/* 220 */       zip.m_description = canonicalName;
/* 221 */       zip.init(this.m_zipenv);
/* 222 */       zipfiles.put(canonicalName, zip);
/*     */     }
/* 224 */     if (this.m_zipenv.m_verbosity >= 6)
/*     */     {
/* 226 */       this.m_loader.report(6, new Object[] { "using zipfile path element \"", canonicalName, "\"" });
/*     */     }
/*     */ 
/* 229 */     while (!innerPath.equals(""))
/*     */     {
/* 231 */       IdcZipEntry entry = zip.getEntry(innerPath, -1);
/* 232 */       if (null != entry)
/*     */       {
/* 234 */         if (entry.m_isDirectory) {
/*     */           break;
/*     */         }
/*     */ 
/* 238 */         canonicalName = new StringBuilder().append(canonicalName).append('/').append(innerPath).toString();
/* 239 */         label600: innerPath = "";
/*     */       }
/*     */       else
/*     */       {
/* 244 */         char[] path = innerPath.toCharArray();
/* 245 */         String prefix = innerPath;
/* 246 */         int slashIndex = path.length;
/*     */         do { do if (--slashIndex <= 0)
/*     */               break label600;
/* 249 */           while ('/' != path[slashIndex]);
/*     */ 
/* 253 */           prefix = new String(path, 0, slashIndex);
/* 254 */           entry = zip.getEntry(prefix, -1); }
/* 255 */         while (null == entry);
/*     */ 
/* 262 */         if (slashIndex < 0)
/*     */         {
/* 264 */           throw new FileNotFoundException(pathname);
/*     */         }
/* 266 */         if (!entry.m_isDirectory)
/*     */         {
/* 268 */           throw new IdcZipException("syPathInvalid", new Object[] { pathname });
/*     */         }
/* 270 */         canonicalName = new StringBuilder().append(canonicalName).append('/').append(prefix).toString();
/* 271 */         innerPath = new String(path, slashIndex + 1, path.length - slashIndex - 1);
/*     */       }
/* 273 */       zip = (IdcZipFile)zipfiles.get(canonicalName);
/* 274 */       if (null == zip)
/*     */       {
/* 276 */         IdcZipUtils.extractEntry(this.m_zipenv, entry, null);
/* 277 */         IdcByteHandler handler = entry.m_bytesUncompressed;
/* 278 */         handler.setPosition(0L);
/* 279 */         zip = new IdcZipFile(handler);
/* 280 */         zip.m_description = canonicalName;
/* 281 */         zip.init(this.m_zipenv);
/* 282 */         zipfiles.put(canonicalName, zip);
/*     */       }
/*     */     }
/* 285 */     if (this.m_zipenv.m_verbosity >= 6)
/*     */     {
/* 287 */       if (innerPath.length() > 0)
/*     */       {
/* 289 */         this.m_loader.report(6, new Object[] { "using zip element \"", innerPath, "\" from \"", pathname, "\"" });
/*     */       }
/*     */       else
/*     */       {
/* 293 */         this.m_loader.report(6, new Object[] { "using zip file \"", pathname, "\"" });
/*     */       }
/*     */     }
/* 296 */     return new IdcLoaderZipElement(this.m_loader, canonicalName, zip, innerPath);
/*     */   }
/*     */ 
/*     */   public synchronized IdcLoaderElement addPathElement(String pathname, int loadOrder)
/*     */     throws Exception
/*     */   {
/* 316 */     pathname = IdcLoaderUtils.fixupPath(pathname);
/* 317 */     if (pathname.length() < 1)
/*     */     {
/* 319 */       if (this.m_zipenv.m_verbosity >= 5)
/*     */       {
/* 321 */         this.m_loader.report(5, new Object[] { "empty path element specified (ignoring)" });
/*     */       }
/* 323 */       return null;
/*     */     }
/* 325 */     int len = this.m_elements.length;
/*     */ 
/* 327 */     int insertIndex = len; int nullIndex = len;
/*     */     do while (true) { if (--insertIndex < 0)
/*     */           break label110;
/* 330 */         if (null != this.m_elements[insertIndex])
/*     */           break;
/* 332 */         nullIndex = insertIndex; }
/*     */ 
/* 334 */     while (loadOrder < this.m_elements[insertIndex].m_loadOrder);
/*     */ 
/* 340 */     if ((++insertIndex >= len) || (null != this.m_elements[insertIndex]))
/*     */     {
/* 343 */       if (nullIndex >= len)
/*     */       {
/* 346 */         label110: int numElements = len << 1;
/* 347 */         IdcLoaderElement[] newElements = new IdcLoaderElement[numElements];
/* 348 */         System.arraycopy(this.m_elements, 0, newElements, 0, len);
/* 349 */         this.m_elements = newElements;
/* 350 */         len = numElements;
/*     */       }
/* 352 */       if (insertIndex < len)
/*     */       {
/* 355 */         System.arraycopy(this.m_elements, insertIndex, this.m_elements, insertIndex + 1, nullIndex - insertIndex);
/*     */       }
/*     */     }
/*     */     IdcLoaderElement element;
/*     */     try
/*     */     {
/* 361 */       element = makePathElement(pathname);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 365 */       this.m_elements[insertIndex] = null;
/* 366 */       if (this.m_zipenv.m_verbosity >= 4)
/*     */       {
/* 368 */         String msg = e.getMessage();
/* 369 */         if ((this.m_zipenv.m_verbosity >= 5) || (msg == null))
/*     */         {
/* 371 */           this.m_loader.report(5, new Object[] { "unable to use path element \"", pathname, "\" (ignoring): ", e });
/*     */         }
/*     */         else
/*     */         {
/* 375 */           this.m_loader.report(5, new Object[] { e.getMessage(), ": unable to use path element \"", pathname, "\" (ignoring)" });
/*     */         }
/*     */       }
/* 378 */       throw e;
/*     */     }
/*     */ 
/* 381 */     IdcLoaderElement existingElement = (IdcLoaderElement)this.m_elementsByName.get(element.m_entryPath);
/* 382 */     if (null != existingElement)
/*     */     {
/* 389 */       if (this.m_zipenv.m_verbosity >= 6)
/*     */       {
/* 391 */         this.m_loader.report(6, new Object[] { "duplicate path element \"", pathname, "\", overriding" });
/*     */       }
/* 393 */       int direction = (existingElement.m_loadOrder > loadOrder) ? 1 : -1;
/* 394 */       for (int i = insertIndex + direction; (i >= 0) && (i < len); i += direction)
/*     */       {
/* 396 */         if (this.m_elements[i] != existingElement)
/*     */           continue;
/* 398 */         this.m_elements[i] = null;
/* 399 */         break;
/*     */       }
/*     */ 
/* 402 */       element = existingElement;
/*     */     }
/* 404 */     element.m_loadOrder = loadOrder;
/* 405 */     this.m_elements[insertIndex] = element;
/* 406 */     this.m_elementsByName.put(element.m_entryPath, element);
/* 407 */     return element;
/*     */   }
/*     */ 
/*     */   public IdcByteHandler lookupByName(String name)
/*     */     throws Exception
/*     */   {
/* 415 */     return lookupByNameAndTrack(name, null);
/*     */   }
/*     */ 
/*     */   public URL lookupURLByName(String name)
/*     */     throws Exception
/*     */   {
/* 421 */     IdcLoaderElement[] elements = this.m_elements;
/* 422 */     if (null == elements)
/*     */     {
/* 424 */       return null;
/*     */     }
/* 426 */     Exception firstException = null;
/* 427 */     for (int i = 0; i < elements.length; ++i)
/*     */     {
/* 429 */       if ((null == elements[i]) || (elements[i].m_isExcludedForResources))
/*     */         continue;
/* 431 */       URL url = null;
/*     */       try
/*     */       {
/* 434 */         url = elements[i].lookupURLByName(name);
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 438 */         if (null == firstException)
/*     */         {
/* 440 */           firstException = e;
/*     */         }
/* 442 */         if (this.m_zipenv.m_verbosity >= 3)
/*     */         {
/* 444 */           this.m_loader.report(3, new Object[] { "Failure attempting to find ", name, " from ", elements[i].m_entryPath, ".", e });
/*     */         }
/*     */       }
/*     */ 
/* 448 */       if (null != url)
/*     */       {
/* 450 */         return url;
/*     */       }
/*     */     }
/*     */ 
/* 454 */     if (null != firstException)
/*     */     {
/* 456 */       throw firstException;
/*     */     }
/* 458 */     return null;
/*     */   }
/*     */ 
/*     */   public IdcByteHandler lookupByNameAndTrack(String name, IdcLoaderElement[] elementPtr)
/*     */     throws Exception
/*     */   {
/* 476 */     IdcLoaderElement[] elements = this.m_elements;
/* 477 */     if (null == elements)
/*     */     {
/* 479 */       return null;
/*     */     }
/* 481 */     Exception firstException = null;
/* 482 */     for (int i = 0; i < elements.length; ++i)
/*     */     {
/* 484 */       IdcLoaderElement element = elements[i];
/* 485 */       if (null == element)
/*     */         continue;
/* 487 */       IdcByteHandler handler = null;
/* 488 */       if (this.m_zipenv.m_verbosity >= 7)
/*     */       {
/* 490 */         this.m_loader.report(7, new Object[] { "Trying to load ", name, " from ", elements[i].getName(), "..." });
/*     */       }
/*     */ 
/*     */       try
/*     */       {
/* 495 */         handler = element.lookupByName(name);
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 499 */         if (firstException == null)
/*     */         {
/* 501 */           firstException = e;
/*     */         }
/* 503 */         if (this.m_zipenv.m_verbosity >= 3)
/*     */         {
/* 505 */           this.m_loader.report(3, new Object[] { "Failure attempting to load ", name, " from ", elements[i].m_entryPath, ".", e });
/*     */         }
/*     */       }
/*     */ 
/* 509 */       if (null == handler) {
/*     */         continue;
/*     */       }
/*     */ 
/* 513 */       if (null != elementPtr)
/*     */       {
/* 515 */         elementPtr[0] = elements[i];
/*     */       }
/* 517 */       return handler;
/*     */     }
/*     */ 
/* 520 */     if (firstException != null)
/*     */     {
/* 522 */       throw firstException;
/*     */     }
/* 524 */     return null;
/*     */   }
/*     */ 
/*     */   public String getClasspathAsString()
/*     */   {
/* 537 */     if (null == IdcLoaderUtils.m_pathSeparator)
/*     */     {
/* 539 */       IdcLoaderUtils.m_pathSeparator = System.getProperty("path.separator");
/*     */     }
/* 541 */     boolean addSeparator = false;
/* 542 */     IdcLoaderElement[] elements = this.m_elements;
/* 543 */     StringBuilder str = new StringBuilder();
/* 544 */     for (int i = 0; i < elements.length; ++i)
/*     */     {
/* 546 */       if (null == elements[i])
/*     */         continue;
/* 548 */       if (addSeparator)
/*     */       {
/* 550 */         str.append(IdcLoaderUtils.m_pathSeparator);
/*     */       }
/* 552 */       addSeparator = true;
/* 553 */       str.append(elements[i].m_entryPath);
/*     */     }
/*     */ 
/* 556 */     return str.toString();
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 562 */     IdcLoaderElement[] elements = this.m_elements;
/* 563 */     if (null == elements)
/*     */     {
/* 565 */       return super.toString();
/*     */     }
/* 567 */     boolean doComma = false;
/* 568 */     StringBuffer str = new StringBuffer("[");
/* 569 */     for (int i = 0; i < elements.length; ++i)
/*     */     {
/* 571 */       if (null == elements[i]) {
/*     */         continue;
/*     */       }
/*     */ 
/* 575 */       if (doComma)
/*     */       {
/* 577 */         str.append(", ");
/*     */       }
/* 579 */       str.append(elements[i].toString());
/* 580 */       doComma = true;
/*     */     }
/* 582 */     str.append(']');
/* 583 */     return str.toString();
/*     */   }
/*     */ 
/*     */   public void clear()
/*     */   {
/* 589 */     this.m_loader.report(6, new Object[] { "IdcLoaderElementList.clear() called" });
/* 590 */     IdcLoaderElement[] elements = this.m_elements;
/* 591 */     if (elements != null)
/*     */     {
/* 594 */       this.m_elements = null;
/* 595 */       for (IdcLoaderElement e : elements)
/*     */       {
/* 597 */         if (e == null)
/*     */           continue;
/* 599 */         e.clear();
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 604 */     if (this.m_elementsByName != null)
/*     */     {
/* 606 */       Set keys = this.m_elementsByName.keySet();
/* 607 */       for (String key : keys)
/*     */       {
/* 609 */         IdcLoaderElement e = (IdcLoaderElement)this.m_elementsByName.get(key);
/* 610 */         if (e != null)
/*     */         {
/* 612 */           e.clear();
/*     */         }
/*     */       }
/* 615 */       this.m_elementsByName.clear();
/*     */     }
/* 617 */     super.clear();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 623 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98978 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.loader.IdcLoaderElementList
 * JD-Core Version:    0.5.4
 */