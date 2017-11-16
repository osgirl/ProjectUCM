/*     */ package intradoc.indexer;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.FileUtilsCfgBuilder;
/*     */ import intradoc.common.IdcComparator;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.RandomAccessConfigFile;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.Sort;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.io.File;
/*     */ import java.io.IOException;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Observable;
/*     */ import java.util.Observer;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class WebChanges
/*     */   implements Observer, IdcComparator
/*     */ {
/*     */   public static final int READ = 0;
/*     */   public static final int WRITE = 1;
/*     */   protected Hashtable m_revClassChanges;
/*     */   protected Hashtable m_changeOffsets;
/*     */   protected Hashtable[] m_changeTypeChanges;
/*     */   protected Hashtable m_unsynchedChanges;
/*     */   protected Hashtable m_unsynchedDeletions;
/*     */   protected Vector m_deletedOffsets;
/*     */   protected int m_count;
/*     */   protected WebChange[] m_allChanges;
/*     */   protected ExecutionContext m_context;
/*     */   protected String m_fileName;
/*     */   protected RandomAccessConfigFile m_file;
/*     */   protected ServiceException m_exception;
/*     */ 
/*     */   public WebChanges()
/*     */   {
/*  47 */     this.m_allChanges = null;
/*     */ 
/*  49 */     this.m_context = null;
/*     */ 
/*  53 */     this.m_exception = null;
/*     */   }
/*     */ 
/*     */   public void init(ExecutionContext cxt) throws ServiceException {
/*  57 */     if (this.m_context != null)
/*     */       return;
/*  59 */     this.m_context = cxt;
/*  60 */     this.m_revClassChanges = new Hashtable();
/*  61 */     this.m_changeTypeChanges = new Hashtable[''];
/*  62 */     this.m_changeOffsets = new Hashtable();
/*  63 */     this.m_unsynchedChanges = new Hashtable();
/*  64 */     this.m_unsynchedDeletions = new Hashtable();
/*  65 */     this.m_deletedOffsets = new IdcVector();
/*  66 */     this.m_count = 0;
/*     */ 
/*  68 */     String dir = (String)cxt.getCachedObject("WorkDirectory");
/*  69 */     this.m_fileName = (dir + "changes.txt");
/*     */     try
/*     */     {
/*  72 */       this.m_file = FileUtilsCfgBuilder.getCfgRandomAccess(this.m_fileName, "rw");
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/*  76 */       throw new ServiceException(LocaleUtils.encodeMessage("syUnableToOpenFile", null, this.m_fileName), e);
/*     */     }
/*     */ 
/*  80 */     recover();
/*     */   }
/*     */ 
/*     */   public void close()
/*     */     throws ServiceException
/*     */   {
/*  86 */     checkStatus();
/*  87 */     if (this.m_file == null)
/*     */       return;
/*  89 */     save();
/*     */     try
/*     */     {
/*  92 */       this.m_file.close();
/*     */     }
/*     */     catch (IOException ignore)
/*     */     {
/*  96 */       if (SystemUtils.m_verbose)
/*     */       {
/*  98 */         Report.debug("indexer", null, ignore);
/*     */       }
/*     */     }
/* 101 */     this.m_file = null;
/*     */   }
/*     */ 
/*     */   public void destroy()
/*     */     throws ServiceException
/*     */   {
/* 107 */     checkStatus();
/*     */     try
/*     */     {
/* 110 */       if (this.m_file != null)
/*     */       {
/* 112 */         this.m_file.close();
/* 113 */         this.m_file = null;
/*     */       }
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 118 */       throw new ServiceException(e);
/*     */     }
/* 120 */     File file = FileUtilsCfgBuilder.getCfgFile(this.m_fileName, null, false);
/* 121 */     file.delete();
/* 122 */     if (!file.exists())
/*     */       return;
/* 124 */     throw new ServiceException(LocaleUtils.encodeMessage("syUnableToDeleteFile", null, this.m_fileName));
/*     */   }
/*     */ 
/*     */   public WebChange addChange(WebChange change)
/*     */     throws ServiceException
/*     */   {
/* 137 */     this.m_allChanges = null;
/* 138 */     WebChange tmp = find(change.m_dRevClassID);
/* 139 */     if (tmp == null)
/*     */     {
/* 141 */       this.m_unsynchedChanges.put(change.m_dRevClassID, change);
/* 142 */       addChangeEx(change);
/*     */     }
/* 144 */     return tmp;
/*     */   }
/*     */ 
/*     */   protected void addChangeEx(WebChange change)
/*     */   {
/* 149 */     this.m_revClassChanges.put(change.m_dRevClassID, change);
/* 150 */     addChangeEx2(change);
/* 151 */     this.m_count += 1;
/*     */   }
/*     */ 
/*     */   protected void addChangeEx2(WebChange change)
/*     */   {
/* 156 */     Hashtable typeTable = this.m_changeTypeChanges[change.m_change];
/* 157 */     if (typeTable == null)
/*     */     {
/* 159 */       typeTable = this.m_changeTypeChanges[change.m_change] =  = new Hashtable();
/*     */     }
/* 161 */     typeTable.put(change.m_dRevClassID, change);
/*     */   }
/*     */ 
/*     */   public WebChange find(String revClassId) throws ServiceException
/*     */   {
/* 166 */     checkStatus();
/* 167 */     return (WebChange)this.m_revClassChanges.get(revClassId);
/*     */   }
/*     */ 
/*     */   public WebChange findNoCheckStatus(String revClassId)
/*     */   {
/* 172 */     return (WebChange)this.m_revClassChanges.get(revClassId);
/*     */   }
/*     */ 
/*     */   public Object first()
/*     */     throws ServiceException
/*     */   {
/* 180 */     checkStatus();
/* 181 */     return this.m_revClassChanges.elements();
/*     */   }
/*     */ 
/*     */   public Object first(char changeType) throws ServiceException
/*     */   {
/* 186 */     checkStatus();
/* 187 */     Enumeration en = null;
/* 188 */     Hashtable table = this.m_changeTypeChanges[changeType];
/* 189 */     if (table != null)
/*     */     {
/* 197 */       Hashtable clone = (Hashtable)table.clone();
/* 198 */       en = clone.elements();
/*     */     }
/* 200 */     return en;
/*     */   }
/*     */ 
/*     */   public WebChange nextChange(Object obj) throws ServiceException
/*     */   {
/* 205 */     return nextChangeEx(obj, null);
/*     */   }
/*     */   public WebChange nextChangeEx(Object obj, String exclusionType) throws ServiceException {
/* 210 */     checkStatus();
/* 211 */     WebChange change = null;
/* 212 */     Enumeration en = (Enumeration)obj;
/*     */     WebChange tmp;
/*     */     do { if (!en.hasMoreElements())
/*     */         break label60;
/* 214 */       tmp = (WebChange)en.nextElement(); }
/*     */ 
/* 216 */     while ((exclusionType != null) && 
/* 218 */       (exclusionType.indexOf(tmp.m_change) > -1));
/*     */ 
/* 223 */     change = tmp;
/*     */ 
/* 226 */     label60: return change;
/*     */   }
/*     */ 
/*     */   public WebChange[] allChanges()
/*     */     throws ServiceException
/*     */   {
/* 238 */     checkStatus();
/* 239 */     if (this.m_allChanges == null)
/*     */     {
/* 241 */       int count = count();
/* 242 */       this.m_allChanges = new WebChange[count];
/* 243 */       Object obj = first();
/*     */ 
/* 245 */       int i = 0;
/* 246 */       while ((change = nextChange(obj)) != null)
/*     */       {
/*     */         WebChange change;
/* 248 */         this.m_allChanges[(i++)] = change;
/*     */       }
/* 250 */       Sort.sort(this.m_allChanges, 0, count - 1, this);
/*     */     }
/* 252 */     return this.m_allChanges;
/*     */   }
/*     */ 
/*     */   public void setChangeType(WebChange change, String newType) throws ServiceException
/*     */   {
/* 257 */     checkStatus();
/* 258 */     setChangeTypeNoCheckStatus(change, newType);
/*     */   }
/*     */ 
/*     */   public void setChangeTypeNoCheckStatus(WebChange change, String newType)
/*     */   {
/* 263 */     char nt = newType.charAt(0);
/* 264 */     if (change.m_change == nt)
/*     */     {
/* 266 */       return;
/*     */     }
/*     */ 
/* 269 */     this.m_changeTypeChanges[change.m_change].remove(change.m_dRevClassID);
/* 270 */     change.setChange(nt);
/* 271 */     addChangeEx2(change);
/*     */   }
/*     */ 
/*     */   public boolean deleteChange(WebChange change)
/*     */     throws ServiceException
/*     */   {
/* 279 */     checkStatus();
/* 280 */     Long offset = (Long)this.m_changeOffsets.get(change.m_dRevClassID);
/* 281 */     if (offset != null)
/*     */     {
/* 283 */       this.m_deletedOffsets.addElement(offset);
/* 284 */       this.m_unsynchedDeletions.put(offset, offset);
/*     */     }
/*     */ 
/* 287 */     if (this.m_revClassChanges.get(change.m_dRevClassID) != null)
/*     */     {
/* 289 */       this.m_changeOffsets.remove(change.m_dRevClassID);
/* 290 */       this.m_changeTypeChanges[change.m_change].remove(change.m_dRevClassID);
/* 291 */       this.m_unsynchedChanges.remove(change.m_dRevClassID);
/* 292 */       this.m_revClassChanges.remove(change.m_dRevClassID);
/* 293 */       change.deleteObserver(this);
/* 294 */       this.m_count -= 1;
/* 295 */       return true;
/*     */     }
/* 297 */     return false;
/*     */   }
/*     */ 
/*     */   public int count() throws ServiceException
/*     */   {
/* 302 */     checkStatus();
/* 303 */     return this.m_count;
/*     */   }
/*     */ 
/*     */   public int count(char changeType) throws ServiceException
/*     */   {
/* 308 */     checkStatus();
/* 309 */     int count = 0;
/* 310 */     Hashtable table = this.m_changeTypeChanges[changeType];
/* 311 */     if (table != null)
/*     */     {
/* 313 */       count = table.size();
/*     */     }
/* 315 */     return count;
/*     */   }
/*     */ 
/*     */   public void recover() throws ServiceException
/*     */   {
/* 320 */     checkStatus();
/* 321 */     this.m_allChanges = null;
/*     */     try
/*     */     {
/* 324 */       this.m_file.seek(0L);
/*     */ 
/* 329 */       int delta = WebChange.getRecordSize();
/* 330 */       byte[] buf = new byte[delta];
/* 331 */       long offset = 0L;
/* 332 */       this.m_count = 0;
/* 333 */       while (this.m_file.read(buf) > -1)
/*     */       {
/* 335 */         WebChange change = new WebChange();
/* 336 */         change.parseBytes(buf);
/* 337 */         if (change.m_dRevClassID.trim().length() == 0)
/*     */         {
/* 339 */           this.m_deletedOffsets.addElement(new Long(offset));
/*     */         }
/*     */         else
/*     */         {
/* 343 */           change.addObserver(this);
/* 344 */           this.m_changeOffsets.put(change.m_dRevClassID, new Long(offset));
/* 345 */           addChangeEx(change);
/*     */         }
/* 347 */         offset += delta;
/*     */       }
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 352 */       throw new ServiceException("!csIndexerUnableToRecoverChanges", e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void save() throws ServiceException
/*     */   {
/*     */     try
/*     */     {
/* 360 */       Vector newChanges = new IdcVector();
/* 361 */       Enumeration en = this.m_unsynchedChanges.elements();
/* 362 */       while (en.hasMoreElements())
/*     */       {
/* 364 */         WebChange change = (WebChange)en.nextElement();
/* 365 */         Long offset = (Long)this.m_changeOffsets.get(change.m_dRevClassID);
/* 366 */         if (offset == null)
/*     */         {
/* 368 */           newChanges.addElement(change);
/*     */         }
/*     */         else
/*     */         {
/* 372 */           this.m_file.seek(offset.longValue());
/* 373 */           this.m_file.write(change.convertToBytes());
/*     */         }
/* 375 */         change.addObserver(this);
/*     */       }
/*     */ 
/* 378 */       int length = newChanges.size();
/* 379 */       long offset = this.m_file.length();
/* 380 */       int delta = WebChange.getRecordSize();
/* 381 */       this.m_file.seek(offset);
/* 382 */       for (int i = 0; i < length; ++i)
/*     */       {
/* 384 */         WebChange change = (WebChange)newChanges.elementAt(i);
/* 385 */         this.m_file.write(change.convertToBytes());
/* 386 */         this.m_changeOffsets.put(change.m_dRevClassID, new Long(offset));
/* 387 */         offset += delta;
/*     */       }
/*     */ 
/* 390 */       length = WebChange.getRecordSize();
/* 391 */       byte[] blank = new byte[length];
/* 392 */       for (int i = 0; i < length; ++i)
/*     */       {
/* 394 */         blank[i] = 32;
/*     */       }
/* 396 */       Enumeration en = this.m_unsynchedDeletions.elements();
/* 397 */       while (en.hasMoreElements())
/*     */       {
/* 399 */         Long off = (Long)en.nextElement();
/* 400 */         offset = off.longValue();
/* 401 */         this.m_file.seek(offset);
/* 402 */         this.m_file.write(blank);
/*     */       }
/*     */ 
/* 405 */       this.m_unsynchedChanges = new Hashtable();
/* 406 */       this.m_unsynchedDeletions = new Hashtable();
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 410 */       throw new ServiceException("!csIndexerUnableToSaveChanges", e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void checkStatus()
/*     */     throws ServiceException
/*     */   {
/* 418 */     if (this.m_exception == null)
/*     */       return;
/* 420 */     throw this.m_exception;
/*     */   }
/*     */ 
/*     */   protected void finalize()
/*     */     throws Throwable
/*     */   {
/* 427 */     close();
/*     */   }
/*     */ 
/*     */   public void update(Observable o, Object obj)
/*     */   {
/* 435 */     WebChange change = (WebChange)o;
/* 436 */     this.m_unsynchedChanges.put(change.m_dRevClassID, change);
/*     */ 
/* 439 */     o.deleteObserver(this);
/*     */ 
/* 441 */     if (this.m_unsynchedChanges.size() != 20)
/*     */       return;
/*     */     try
/*     */     {
/* 445 */       save();
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 449 */       this.m_exception = e;
/*     */     }
/*     */   }
/*     */ 
/*     */   public int compare(Object o1, Object o2)
/*     */   {
/* 459 */     WebChange c1 = (WebChange)o1;
/* 460 */     WebChange c2 = (WebChange)o2;
/* 461 */     long id1 = Long.parseLong(c1.m_dRevClassID);
/* 462 */     long id2 = Long.parseLong(c2.m_dRevClassID);
/* 463 */     if (id1 > id2)
/*     */     {
/* 465 */       return 1;
/*     */     }
/* 467 */     if (id1 < id2)
/*     */     {
/* 469 */       return -1;
/*     */     }
/* 471 */     return 0;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 476 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97049 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.indexer.WebChanges
 * JD-Core Version:    0.5.4
 */