/*      */ package intradoc.common;
/*      */ 
/*      */ import intradoc.util.IdcException;
/*      */ import intradoc.util.IdcPerfectHash;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.io.BufferedInputStream;
/*      */ import java.io.BufferedOutputStream;
/*      */ import java.io.DataInputStream;
/*      */ import java.io.DataOutputStream;
/*      */ import java.io.File;
/*      */ import java.io.FileInputStream;
/*      */ import java.io.FileOutputStream;
/*      */ import java.io.IOException;
/*      */ import java.io.ObjectOutputStream;
/*      */ import java.io.OutputStream;
/*      */ import java.nio.ByteBuffer;
/*      */ import java.nio.CharBuffer;
/*      */ import java.nio.channels.FileChannel;
/*      */ import java.nio.channels.FileChannel.MapMode;
/*      */ import java.util.ArrayList;
/*      */ import java.util.HashMap;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Map.Entry;
/*      */ import java.util.Properties;
/*      */ import java.util.Random;
/*      */ 
/*      */ public class IdcLocalizationStrings
/*      */   implements StreamEventHandler
/*      */ {
/*      */   protected static final long INDEX_MAGIC = 4078153730L;
/*      */   public Properties m_environment;
/*      */   public String m_dataDir;
/*      */   public String m_localDir;
/*      */   public boolean m_sourceIsLocal;
/*      */   public int m_versionNumber;
/*      */   public int m_oldVersionNumber;
/*      */   public IdcPerfectHash[] m_stringMap;
/*      */   public IdcPerfectHash m_languageMap;
/*      */   public IdcPerfectHash m_applicationMap;
/*      */   public int m_defaultLanguageIndex;
/*      */   public CharBuffer[] m_langStringBlocks;
/*      */   public byte[] m_valLangBlockData;
/*      */   public int[] m_valLangOffsetData;
/*      */   public short[] m_valLangSourceData;
/*      */   public HashMap<String, HashMap<String, String>> m_sourceFileInfo;
/*      */   public short m_maxSourceFileValue;
/*      */   public HashMap<String, HashMap> m_tagLists;
/*      */   protected boolean m_readyForIncrementalUpdate;
/*      */   protected boolean m_foundIncrementalWork;
/*      */   protected List<WorkObject>[] m_work;
/*      */   protected int m_workCount;
/*      */   protected Map<String, Integer>[] m_keys;
/*      */   protected int m_stringValueCount;
/*      */   protected List<String> m_newValues;
/*      */   protected List<String> m_newKeys;
/*      */   protected IntervalData m_interval;
/*      */   protected int m_appendedStrings;
/*      */   protected String m_activeTask;
/*      */   protected int m_fileCount;
/*      */   protected ReportProgress m_progress;
/*      */ 
/*      */   public IdcLocalizationStrings(Properties environment, String stringDataDir, String stringLocalDir, ReportProgress progress)
/*      */   {
/*  176 */     this.m_environment = environment;
/*  177 */     this.m_dataDir = stringDataDir;
/*  178 */     this.m_localDir = stringLocalDir;
/*  179 */     this.m_sourceFileInfo = new HashMap();
/*  180 */     this.m_progress = progress;
/*      */   }
/*      */ 
/*      */   protected String getConfigFileName()
/*      */   {
/*  185 */     String configFile = null;
/*  186 */     if (this.m_localDir != null)
/*      */     {
/*  188 */       configFile = FileUtils.getAbsolutePath(this.m_localDir, "strings.cfg");
/*  189 */       if (FileUtils.checkFile(configFile, 1) == 0)
/*      */       {
/*  191 */         this.m_sourceIsLocal = true;
/*  192 */         return configFile;
/*      */       }
/*      */     }
/*  195 */     configFile = FileUtils.getAbsolutePath(this.m_dataDir, "strings.cfg");
/*  196 */     return configFile;
/*      */   }
/*      */ 
/*      */   protected String getStringSourceDir()
/*      */   {
/*  201 */     String stringDir = null;
/*  202 */     if (this.m_sourceIsLocal)
/*      */     {
/*  204 */       stringDir = FileUtils.getAbsolutePath(this.m_localDir, "i" + this.m_oldVersionNumber);
/*  205 */       if (FileUtils.checkFile(stringDir, 0) == 0)
/*      */       {
/*  207 */         return stringDir;
/*      */       }
/*      */     }
/*  210 */     stringDir = FileUtils.getAbsolutePath(this.m_dataDir, "i" + this.m_oldVersionNumber);
/*  211 */     return stringDir;
/*      */   }
/*      */ 
/*      */   protected String getStringTargetDir()
/*      */   {
/*  216 */     if (this.m_localDir != null)
/*      */     {
/*  218 */       String stringDir = FileUtils.getAbsolutePath(this.m_localDir, "i" + this.m_versionNumber);
/*  219 */       if (FileUtils.checkFile(stringDir, 0) == 0)
/*      */       {
/*  221 */         return stringDir;
/*      */       }
/*      */     }
/*  224 */     String stringDir = FileUtils.getAbsolutePath(this.m_dataDir, "i" + this.m_versionNumber);
/*  225 */     return stringDir;
/*      */   }
/*      */ 
/*      */   public Properties readConfigFile() throws ServiceException
/*      */   {
/*  230 */     Properties props = new Properties();
/*      */ 
/*  234 */     String configFile = getConfigFileName();
/*  235 */     if (FileUtils.checkFile(configFile, true, false) == 0)
/*      */     {
/*      */       try
/*      */       {
/*  239 */         props.load(new BufferedInputStream(new FileInputStream(configFile)));
/*      */       }
/*      */       catch (IOException e)
/*      */       {
/*  243 */         throw new ServiceException(e, "syUnableToReadFile", new Object[] { configFile });
/*      */       }
/*      */     }
/*      */ 
/*  247 */     int version = NumberUtils.parseInteger(props.getProperty("version"), -1);
/*  248 */     this.m_versionNumber = version;
/*      */ 
/*  250 */     return props;
/*      */   }
/*      */ 
/*      */   public void saveIndex() throws IOException
/*      */   {
/*  255 */     OutputStream out = null;
/*      */     try
/*      */     {
/*  258 */       DataOutputStream dataOut = null;
/*      */ 
/*  261 */       int version = this.m_versionNumber;
/*  262 */       Properties stringProps = null;
/*      */       try
/*      */       {
/*  265 */         stringProps = readConfigFile();
/*      */       }
/*      */       catch (ServiceException e)
/*      */       {
/*  269 */         throw new IOException(e);
/*      */       }
/*  271 */       this.m_versionNumber = version;
/*      */ 
/*  273 */       String stringDir = getStringTargetDir();
/*  274 */       IntervalData interval = null;
/*  275 */       int flags = 16;
/*  276 */       int langCount = this.m_languageMap.size();
/*      */ 
/*  278 */       if (SystemUtils.isActiveTrace("localization"))
/*      */       {
/*  280 */         interval = new IntervalData(" buildStringIndex()");
/*      */       }
/*      */ 
/*  283 */       out = FileUtils.openOutputStream(stringDir + "/index.dat", flags);
/*  284 */       dataOut = new DataOutputStream(out);
/*  285 */       dataOut.writeLong(4078153730L);
/*  286 */       dataOut.writeInt(this.m_valLangBlockData.length);
/*  287 */       for (int i = 0; i < this.m_valLangBlockData.length; ++i)
/*      */       {
/*  289 */         dataOut.writeInt(this.m_valLangBlockData[i]);
/*      */       }
/*  291 */       for (int i = 0; i < this.m_valLangOffsetData.length; ++i)
/*      */       {
/*  293 */         dataOut.writeInt(this.m_valLangOffsetData[i]);
/*      */       }
/*  295 */       for (int i = 0; i < this.m_valLangSourceData.length; ++i)
/*      */       {
/*  297 */         dataOut.writeInt(this.m_valLangSourceData[i]);
/*      */       }
/*  299 */       dataOut.close();
/*  300 */       out = null;
/*      */ 
/*  302 */       out = FileUtils.openOutputStream(stringDir + "/lang.dat", flags);
/*  303 */       ObjectOutputStream objectOut = new ObjectOutputStream(out);
/*  304 */       objectOut.writeInt(langCount);
/*  305 */       for (int i = 0; i < langCount; ++i)
/*      */       {
/*  307 */         objectOut.writeObject(this.m_languageMap.get(i));
/*      */       }
/*  309 */       objectOut.close();
/*  310 */       out = null;
/*      */ 
/*  312 */       out = FileUtils.openOutputStream(stringDir + "/app.dat", flags);
/*  313 */       objectOut = new ObjectOutputStream(out);
/*  314 */       objectOut.writeInt(this.m_applicationMap.size());
/*  315 */       for (int i = 0; i < this.m_applicationMap.size(); ++i)
/*      */       {
/*  317 */         objectOut.writeObject(this.m_applicationMap.get(i));
/*      */       }
/*  319 */       objectOut.close();
/*  320 */       out = null;
/*      */ 
/*  322 */       out = FileUtils.openOutputStream(stringDir + "/map.dat", flags);
/*  323 */       objectOut = new ObjectOutputStream(out);
/*  324 */       objectOut.writeObject(this.m_stringMap[0]);
/*  325 */       objectOut.close();
/*  326 */       out = null;
/*      */ 
/*  329 */       out = FileUtils.openOutputStream(stringDir + "/tag.dat", flags);
/*  330 */       objectOut = new ObjectOutputStream(out);
/*  331 */       for (String tag : this.m_tagLists.keySet())
/*      */       {
/*  333 */         Map list = (Map)this.m_tagLists.get(tag);
/*  334 */         objectOut.writeObject(tag);
/*  335 */         objectOut.writeObject(list);
/*      */       }
/*  337 */       objectOut.close();
/*  338 */       out = null;
/*      */ 
/*  341 */       out = FileUtils.openOutputStream(stringDir + "/source.dat", flags);
/*  342 */       objectOut = new ObjectOutputStream(out);
/*  343 */       objectOut.writeInt(this.m_maxSourceFileValue);
/*  344 */       objectOut.writeObject(this.m_sourceFileInfo);
/*  345 */       objectOut.close();
/*  346 */       out = null;
/*      */ 
/*  348 */       String configFile = getConfigFileName();
/*  349 */       out = FileUtils.openOutputStream(configFile, flags);
/*  350 */       stringProps.put("version", "" + this.m_versionNumber);
/*  351 */       stringProps.store(out, null);
/*  352 */       out = null;
/*      */ 
/*  354 */       if (interval != null)
/*      */       {
/*  356 */         interval.traceAndRestart("localization", "buildStringIndex()");
/*      */       }
/*      */     }
/*      */     finally
/*      */     {
/*  361 */       FileUtils.closeObject(out);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void cleanup()
/*      */   {
/*  367 */     File dir = new File(this.m_dataDir);
/*  368 */     for (File f : dir.listFiles())
/*      */     {
/*  370 */       String name = f.getName();
/*  371 */       if ((!f.isDirectory()) || (!name.startsWith("i")) || (name.equals("i" + this.m_versionNumber)))
/*      */         continue;
/*      */       try
/*      */       {
/*  375 */         FileUtils.deleteDirectory(f, true);
/*      */       }
/*      */       catch (ServiceException ignore)
/*      */       {
/*  380 */         Report.trace("localization", "Unable to purge directory " + f, ignore);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void closeOutput(OutputStream blockOut, String currentBlockFile, int currentBlock)
/*      */   {
/*  388 */     if (blockOut == null)
/*      */       return;
/*      */     try
/*      */     {
/*  392 */       this.m_langStringBlocks[currentBlock] = null;
/*  393 */       blockOut.close();
/*      */     }
/*      */     catch (Throwable t)
/*      */     {
/*  400 */       String file = getStringTargetDir() + "/tag.dat";
/*  401 */       Report.trace(null, "removing " + file + " due to exception updating index.", t);
/*  402 */       FileUtils.deleteFile(file);
/*  403 */       throw new AssertionError(t);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void loadStringIndex()
/*      */     throws ServiceException
/*      */   {
/*  410 */     readConfigFile();
/*  411 */     this.m_oldVersionNumber = this.m_versionNumber;
/*  412 */     String stringDir = getStringSourceDir();
/*  413 */     loadStringIndex(stringDir); } 
/*      */   // ERROR //
/*      */   public void loadStringIndex(String stringDir) throws ServiceException { // Byte code:
/*      */     //   0: aload_0
/*      */     //   1: sipush 256
/*      */     //   4: anewarray 109	java/nio/CharBuffer
/*      */     //   7: putfield 99	intradoc/common/IdcLocalizationStrings:m_langStringBlocks	[Ljava/nio/CharBuffer;
/*      */     //   10: new 110	java/util/Random
/*      */     //   13: dup
/*      */     //   14: invokestatic 111	java/lang/System:currentTimeMillis	()J
/*      */     //   17: invokespecial 112	java/util/Random:<init>	(J)V
/*      */     //   20: astore_2
/*      */     //   21: aconst_null
/*      */     //   22: astore_3
/*      */     //   23: aconst_null
/*      */     //   24: astore 4
/*      */     //   26: aconst_null
/*      */     //   27: astore 5
/*      */     //   29: aconst_null
/*      */     //   30: astore 6
/*      */     //   32: aload_0
/*      */     //   33: new 113	intradoc/util/IdcPerfectHash
/*      */     //   36: dup
/*      */     //   37: aload_2
/*      */     //   38: invokespecial 114	intradoc/util/IdcPerfectHash:<init>	(Ljava/util/Random;)V
/*      */     //   41: putfield 41	intradoc/common/IdcLocalizationStrings:m_languageMap	Lintradoc/util/IdcPerfectHash;
/*      */     //   44: aload_0
/*      */     //   45: new 113	intradoc/util/IdcPerfectHash
/*      */     //   48: dup
/*      */     //   49: aload_2
/*      */     //   50: invokespecial 114	intradoc/util/IdcPerfectHash:<init>	(Ljava/util/Random;)V
/*      */     //   53: putfield 68	intradoc/common/IdcLocalizationStrings:m_applicationMap	Lintradoc/util/IdcPerfectHash;
/*      */     //   56: new 115	java/io/ObjectInputStream
/*      */     //   59: dup
/*      */     //   60: new 25	java/io/BufferedInputStream
/*      */     //   63: dup
/*      */     //   64: new 26	java/io/FileInputStream
/*      */     //   67: dup
/*      */     //   68: new 13	java/lang/StringBuilder
/*      */     //   71: dup
/*      */     //   72: invokespecial 14	java/lang/StringBuilder:<init>	()V
/*      */     //   75: aload_1
/*      */     //   76: invokevirtual 16	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
/*      */     //   79: ldc 67
/*      */     //   81: invokevirtual 16	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
/*      */     //   84: invokevirtual 19	java/lang/StringBuilder:toString	()Ljava/lang/String;
/*      */     //   87: invokespecial 27	java/io/FileInputStream:<init>	(Ljava/lang/String;)V
/*      */     //   90: invokespecial 28	java/io/BufferedInputStream:<init>	(Ljava/io/InputStream;)V
/*      */     //   93: invokespecial 116	java/io/ObjectInputStream:<init>	(Ljava/io/InputStream;)V
/*      */     //   96: astore_3
/*      */     //   97: aload_3
/*      */     //   98: invokevirtual 117	java/io/ObjectInputStream:readInt	()I
/*      */     //   101: istore 7
/*      */     //   103: iconst_0
/*      */     //   104: istore 8
/*      */     //   106: iload 8
/*      */     //   108: iload 7
/*      */     //   110: if_icmpge +27 -> 137
/*      */     //   113: aload_3
/*      */     //   114: invokevirtual 118	java/io/ObjectInputStream:readObject	()Ljava/lang/Object;
/*      */     //   117: checkcast 77	java/lang/String
/*      */     //   120: astore 9
/*      */     //   122: aload_0
/*      */     //   123: getfield 68	intradoc/common/IdcLocalizationStrings:m_applicationMap	Lintradoc/util/IdcPerfectHash;
/*      */     //   126: aload 9
/*      */     //   128: invokevirtual 119	intradoc/util/IdcPerfectHash:add	(Ljava/lang/Object;)V
/*      */     //   131: iinc 8 1
/*      */     //   134: goto -28 -> 106
/*      */     //   137: aload_3
/*      */     //   138: invokevirtual 120	java/io/ObjectInputStream:close	()V
/*      */     //   141: new 115	java/io/ObjectInputStream
/*      */     //   144: dup
/*      */     //   145: new 25	java/io/BufferedInputStream
/*      */     //   148: dup
/*      */     //   149: new 26	java/io/FileInputStream
/*      */     //   152: dup
/*      */     //   153: new 13	java/lang/StringBuilder
/*      */     //   156: dup
/*      */     //   157: invokespecial 14	java/lang/StringBuilder:<init>	()V
/*      */     //   160: aload_1
/*      */     //   161: invokevirtual 16	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
/*      */     //   164: ldc 60
/*      */     //   166: invokevirtual 16	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
/*      */     //   169: invokevirtual 19	java/lang/StringBuilder:toString	()Ljava/lang/String;
/*      */     //   172: invokespecial 27	java/io/FileInputStream:<init>	(Ljava/lang/String;)V
/*      */     //   175: invokespecial 28	java/io/BufferedInputStream:<init>	(Ljava/io/InputStream;)V
/*      */     //   178: invokespecial 116	java/io/ObjectInputStream:<init>	(Ljava/io/InputStream;)V
/*      */     //   181: astore_3
/*      */     //   182: aload_3
/*      */     //   183: invokevirtual 117	java/io/ObjectInputStream:readInt	()I
/*      */     //   186: istore 8
/*      */     //   188: iconst_0
/*      */     //   189: istore 9
/*      */     //   191: iload 9
/*      */     //   193: iload 8
/*      */     //   195: if_icmpge +27 -> 222
/*      */     //   198: aload_3
/*      */     //   199: invokevirtual 118	java/io/ObjectInputStream:readObject	()Ljava/lang/Object;
/*      */     //   202: checkcast 77	java/lang/String
/*      */     //   205: astore 10
/*      */     //   207: aload_0
/*      */     //   208: getfield 41	intradoc/common/IdcLocalizationStrings:m_languageMap	Lintradoc/util/IdcPerfectHash;
/*      */     //   211: aload 10
/*      */     //   213: invokevirtual 119	intradoc/util/IdcPerfectHash:add	(Ljava/lang/Object;)V
/*      */     //   216: iinc 9 1
/*      */     //   219: goto -28 -> 191
/*      */     //   222: aload_3
/*      */     //   223: invokevirtual 120	java/io/ObjectInputStream:close	()V
/*      */     //   226: goto +27 -> 253
/*      */     //   229: astore 7
/*      */     //   231: new 31	intradoc/common/ServiceException
/*      */     //   234: dup
/*      */     //   235: aload 7
/*      */     //   237: invokespecial 122	intradoc/common/ServiceException:<init>	(Ljava/lang/Throwable;)V
/*      */     //   240: athrow
/*      */     //   241: astore 7
/*      */     //   243: new 31	intradoc/common/ServiceException
/*      */     //   246: dup
/*      */     //   247: aload 7
/*      */     //   249: invokespecial 122	intradoc/common/ServiceException:<init>	(Ljava/lang/Throwable;)V
/*      */     //   252: athrow
/*      */     //   253: aload_0
/*      */     //   254: aload_0
/*      */     //   255: getfield 41	intradoc/common/IdcLocalizationStrings:m_languageMap	Lintradoc/util/IdcPerfectHash;
/*      */     //   258: getstatic 123	intradoc/common/LocaleResources:m_baseLanguage	Ljava/lang/String;
/*      */     //   261: invokevirtual 124	intradoc/util/IdcPerfectHash:getCode	(Ljava/lang/Object;)I
/*      */     //   264: putfield 125	intradoc/common/IdcLocalizationStrings:m_defaultLanguageIndex	I
/*      */     //   267: new 126	java/io/DataInputStream
/*      */     //   270: dup
/*      */     //   271: new 25	java/io/BufferedInputStream
/*      */     //   274: dup
/*      */     //   275: new 26	java/io/FileInputStream
/*      */     //   278: dup
/*      */     //   279: new 13	java/lang/StringBuilder
/*      */     //   282: dup
/*      */     //   283: invokespecial 14	java/lang/StringBuilder:<init>	()V
/*      */     //   286: aload_1
/*      */     //   287: invokevirtual 16	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
/*      */     //   290: ldc 48
/*      */     //   292: invokevirtual 16	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
/*      */     //   295: invokevirtual 19	java/lang/StringBuilder:toString	()Ljava/lang/String;
/*      */     //   298: invokespecial 27	java/io/FileInputStream:<init>	(Ljava/lang/String;)V
/*      */     //   301: invokespecial 28	java/io/BufferedInputStream:<init>	(Ljava/io/InputStream;)V
/*      */     //   304: invokespecial 127	java/io/DataInputStream:<init>	(Ljava/io/InputStream;)V
/*      */     //   307: astore 4
/*      */     //   309: aload 4
/*      */     //   311: invokevirtual 128	java/io/DataInputStream:readLong	()J
/*      */     //   314: lstore 7
/*      */     //   316: lload 7
/*      */     //   318: ldc2_w 52
/*      */     //   321: lcmp
/*      */     //   322: ifeq +13 -> 335
/*      */     //   325: new 30	java/io/IOException
/*      */     //   328: dup
/*      */     //   329: ldc 129
/*      */     //   331: invokespecial 130	java/io/IOException:<init>	(Ljava/lang/String;)V
/*      */     //   334: athrow
/*      */     //   335: aload 4
/*      */     //   337: invokevirtual 131	java/io/DataInputStream:readInt	()I
/*      */     //   340: istore 9
/*      */     //   342: aload_0
/*      */     //   343: iload 9
/*      */     //   345: newarray byte
/*      */     //   347: putfield 55	intradoc/common/IdcLocalizationStrings:m_valLangBlockData	[B
/*      */     //   350: iconst_0
/*      */     //   351: istore 10
/*      */     //   353: iload 10
/*      */     //   355: aload_0
/*      */     //   356: getfield 55	intradoc/common/IdcLocalizationStrings:m_valLangBlockData	[B
/*      */     //   359: arraylength
/*      */     //   360: if_icmpge +22 -> 382
/*      */     //   363: aload_0
/*      */     //   364: getfield 55	intradoc/common/IdcLocalizationStrings:m_valLangBlockData	[B
/*      */     //   367: iload 10
/*      */     //   369: aload 4
/*      */     //   371: invokevirtual 131	java/io/DataInputStream:readInt	()I
/*      */     //   374: i2b
/*      */     //   375: bastore
/*      */     //   376: iinc 10 1
/*      */     //   379: goto -26 -> 353
/*      */     //   382: aload_0
/*      */     //   383: iconst_2
/*      */     //   384: iload 9
/*      */     //   386: imul
/*      */     //   387: newarray int
/*      */     //   389: putfield 57	intradoc/common/IdcLocalizationStrings:m_valLangOffsetData	[I
/*      */     //   392: iconst_0
/*      */     //   393: istore 10
/*      */     //   395: iload 10
/*      */     //   397: aload_0
/*      */     //   398: getfield 57	intradoc/common/IdcLocalizationStrings:m_valLangOffsetData	[I
/*      */     //   401: arraylength
/*      */     //   402: if_icmpge +21 -> 423
/*      */     //   405: aload_0
/*      */     //   406: getfield 57	intradoc/common/IdcLocalizationStrings:m_valLangOffsetData	[I
/*      */     //   409: iload 10
/*      */     //   411: aload 4
/*      */     //   413: invokevirtual 131	java/io/DataInputStream:readInt	()I
/*      */     //   416: iastore
/*      */     //   417: iinc 10 1
/*      */     //   420: goto -25 -> 395
/*      */     //   423: aload_0
/*      */     //   424: iload 9
/*      */     //   426: newarray short
/*      */     //   428: putfield 58	intradoc/common/IdcLocalizationStrings:m_valLangSourceData	[S
/*      */     //   431: iconst_0
/*      */     //   432: istore 10
/*      */     //   434: iload 10
/*      */     //   436: aload_0
/*      */     //   437: getfield 58	intradoc/common/IdcLocalizationStrings:m_valLangSourceData	[S
/*      */     //   440: arraylength
/*      */     //   441: if_icmpge +22 -> 463
/*      */     //   444: aload_0
/*      */     //   445: getfield 58	intradoc/common/IdcLocalizationStrings:m_valLangSourceData	[S
/*      */     //   448: iload 10
/*      */     //   450: aload 4
/*      */     //   452: invokevirtual 131	java/io/DataInputStream:readInt	()I
/*      */     //   455: i2s
/*      */     //   456: sastore
/*      */     //   457: iinc 10 1
/*      */     //   460: goto -26 -> 434
/*      */     //   463: goto +15 -> 478
/*      */     //   466: astore 7
/*      */     //   468: new 31	intradoc/common/ServiceException
/*      */     //   471: dup
/*      */     //   472: aload 7
/*      */     //   474: invokespecial 122	intradoc/common/ServiceException:<init>	(Ljava/lang/Throwable;)V
/*      */     //   477: athrow
/*      */     //   478: new 115	java/io/ObjectInputStream
/*      */     //   481: dup
/*      */     //   482: new 25	java/io/BufferedInputStream
/*      */     //   485: dup
/*      */     //   486: new 26	java/io/FileInputStream
/*      */     //   489: dup
/*      */     //   490: new 13	java/lang/StringBuilder
/*      */     //   493: dup
/*      */     //   494: invokespecial 14	java/lang/StringBuilder:<init>	()V
/*      */     //   497: aload_1
/*      */     //   498: invokevirtual 16	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
/*      */     //   501: ldc 80
/*      */     //   503: invokevirtual 16	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
/*      */     //   506: invokevirtual 19	java/lang/StringBuilder:toString	()Ljava/lang/String;
/*      */     //   509: invokespecial 27	java/io/FileInputStream:<init>	(Ljava/lang/String;)V
/*      */     //   512: invokespecial 28	java/io/BufferedInputStream:<init>	(Ljava/io/InputStream;)V
/*      */     //   515: invokespecial 116	java/io/ObjectInputStream:<init>	(Ljava/io/InputStream;)V
/*      */     //   518: astore 5
/*      */     //   520: aload_0
/*      */     //   521: aload 5
/*      */     //   523: invokevirtual 117	java/io/ObjectInputStream:readInt	()I
/*      */     //   526: i2s
/*      */     //   527: putfield 81	intradoc/common/IdcLocalizationStrings:m_maxSourceFileValue	S
/*      */     //   530: aload_0
/*      */     //   531: aload 5
/*      */     //   533: invokevirtual 118	java/io/ObjectInputStream:readObject	()Ljava/lang/Object;
/*      */     //   536: checkcast 5	java/util/HashMap
/*      */     //   539: putfield 7	intradoc/common/IdcLocalizationStrings:m_sourceFileInfo	Ljava/util/HashMap;
/*      */     //   542: aload 5
/*      */     //   544: invokevirtual 120	java/io/ObjectInputStream:close	()V
/*      */     //   547: new 115	java/io/ObjectInputStream
/*      */     //   550: dup
/*      */     //   551: new 25	java/io/BufferedInputStream
/*      */     //   554: dup
/*      */     //   555: new 26	java/io/FileInputStream
/*      */     //   558: dup
/*      */     //   559: new 13	java/lang/StringBuilder
/*      */     //   562: dup
/*      */     //   563: invokespecial 14	java/lang/StringBuilder:<init>	()V
/*      */     //   566: aload_1
/*      */     //   567: invokevirtual 16	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
/*      */     //   570: ldc 71
/*      */     //   572: invokevirtual 16	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
/*      */     //   575: invokevirtual 19	java/lang/StringBuilder:toString	()Ljava/lang/String;
/*      */     //   578: invokespecial 27	java/io/FileInputStream:<init>	(Ljava/lang/String;)V
/*      */     //   581: invokespecial 28	java/io/BufferedInputStream:<init>	(Ljava/io/InputStream;)V
/*      */     //   584: invokespecial 116	java/io/ObjectInputStream:<init>	(Ljava/io/InputStream;)V
/*      */     //   587: astore 5
/*      */     //   589: aload_0
/*      */     //   590: new 5	java/util/HashMap
/*      */     //   593: dup
/*      */     //   594: invokespecial 6	java/util/HashMap:<init>	()V
/*      */     //   597: putfield 72	intradoc/common/IdcLocalizationStrings:m_tagLists	Ljava/util/HashMap;
/*      */     //   600: aload 5
/*      */     //   602: invokevirtual 118	java/io/ObjectInputStream:readObject	()Ljava/lang/Object;
/*      */     //   605: checkcast 77	java/lang/String
/*      */     //   608: astore 7
/*      */     //   610: goto +23 -> 633
/*      */     //   613: astore 8
/*      */     //   615: getstatic 133	intradoc/common/SystemUtils:m_verbose	Z
/*      */     //   618: ifeq +12 -> 630
/*      */     //   621: ldc 43
/*      */     //   623: ldc 134
/*      */     //   625: aload 8
/*      */     //   627: invokestatic 135	intradoc/common/Report:debug	(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)V
/*      */     //   630: goto +28 -> 658
/*      */     //   633: aload 5
/*      */     //   635: invokevirtual 118	java/io/ObjectInputStream:readObject	()Ljava/lang/Object;
/*      */     //   638: checkcast 5	java/util/HashMap
/*      */     //   641: astore 8
/*      */     //   643: aload_0
/*      */     //   644: getfield 72	intradoc/common/IdcLocalizationStrings:m_tagLists	Ljava/util/HashMap;
/*      */     //   647: aload 7
/*      */     //   649: aload 8
/*      */     //   651: invokevirtual 136	java/util/HashMap:put	(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
/*      */     //   654: pop
/*      */     //   655: goto -55 -> 600
/*      */     //   658: aload 5
/*      */     //   660: invokevirtual 120	java/io/ObjectInputStream:close	()V
/*      */     //   663: goto +27 -> 690
/*      */     //   666: astore 7
/*      */     //   668: new 31	intradoc/common/ServiceException
/*      */     //   671: dup
/*      */     //   672: aload 7
/*      */     //   674: invokespecial 122	intradoc/common/ServiceException:<init>	(Ljava/lang/Throwable;)V
/*      */     //   677: athrow
/*      */     //   678: astore 7
/*      */     //   680: new 31	intradoc/common/ServiceException
/*      */     //   683: dup
/*      */     //   684: aload 7
/*      */     //   686: invokespecial 122	intradoc/common/ServiceException:<init>	(Ljava/lang/Throwable;)V
/*      */     //   689: athrow
/*      */     //   690: new 115	java/io/ObjectInputStream
/*      */     //   693: dup
/*      */     //   694: new 25	java/io/BufferedInputStream
/*      */     //   697: dup
/*      */     //   698: new 26	java/io/FileInputStream
/*      */     //   701: dup
/*      */     //   702: new 13	java/lang/StringBuilder
/*      */     //   705: dup
/*      */     //   706: invokespecial 14	java/lang/StringBuilder:<init>	()V
/*      */     //   709: aload_1
/*      */     //   710: invokevirtual 16	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
/*      */     //   713: ldc 69
/*      */     //   715: invokevirtual 16	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
/*      */     //   718: invokevirtual 19	java/lang/StringBuilder:toString	()Ljava/lang/String;
/*      */     //   721: invokespecial 27	java/io/FileInputStream:<init>	(Ljava/lang/String;)V
/*      */     //   724: invokespecial 28	java/io/BufferedInputStream:<init>	(Ljava/io/InputStream;)V
/*      */     //   727: invokespecial 116	java/io/ObjectInputStream:<init>	(Ljava/io/InputStream;)V
/*      */     //   730: astore 6
/*      */     //   732: aload_0
/*      */     //   733: aload_0
/*      */     //   734: getfield 68	intradoc/common/IdcLocalizationStrings:m_applicationMap	Lintradoc/util/IdcPerfectHash;
/*      */     //   737: invokevirtual 42	intradoc/util/IdcPerfectHash:size	()I
/*      */     //   740: iconst_1
/*      */     //   741: iadd
/*      */     //   742: anewarray 113	intradoc/util/IdcPerfectHash
/*      */     //   745: putfield 70	intradoc/common/IdcLocalizationStrings:m_stringMap	[Lintradoc/util/IdcPerfectHash;
/*      */     //   748: aload_0
/*      */     //   749: getfield 70	intradoc/common/IdcLocalizationStrings:m_stringMap	[Lintradoc/util/IdcPerfectHash;
/*      */     //   752: iconst_0
/*      */     //   753: aload 6
/*      */     //   755: invokevirtual 118	java/io/ObjectInputStream:readObject	()Ljava/lang/Object;
/*      */     //   758: checkcast 113	intradoc/util/IdcPerfectHash
/*      */     //   761: aastore
/*      */     //   762: aload_0
/*      */     //   763: getfield 70	intradoc/common/IdcLocalizationStrings:m_stringMap	[Lintradoc/util/IdcPerfectHash;
/*      */     //   766: iconst_0
/*      */     //   767: aaload
/*      */     //   768: aload_2
/*      */     //   769: putfield 137	intradoc/util/IdcPerfectHash:m_random	Ljava/util/Random;
/*      */     //   772: aload 6
/*      */     //   774: invokevirtual 120	java/io/ObjectInputStream:close	()V
/*      */     //   777: iconst_0
/*      */     //   778: istore 7
/*      */     //   780: iload 7
/*      */     //   782: aload_0
/*      */     //   783: getfield 68	intradoc/common/IdcLocalizationStrings:m_applicationMap	Lintradoc/util/IdcPerfectHash;
/*      */     //   786: invokevirtual 42	intradoc/util/IdcPerfectHash:size	()I
/*      */     //   789: if_icmpgt +60 -> 849
/*      */     //   792: aload_0
/*      */     //   793: getfield 70	intradoc/common/IdcLocalizationStrings:m_stringMap	[Lintradoc/util/IdcPerfectHash;
/*      */     //   796: iconst_0
/*      */     //   797: aaload
/*      */     //   798: astore 8
/*      */     //   800: iload 7
/*      */     //   802: ifle +23 -> 825
/*      */     //   805: new 113	intradoc/util/IdcPerfectHash
/*      */     //   808: dup
/*      */     //   809: invokespecial 138	intradoc/util/IdcPerfectHash:<init>	()V
/*      */     //   812: astore 8
/*      */     //   814: aload 8
/*      */     //   816: aload_0
/*      */     //   817: getfield 70	intradoc/common/IdcLocalizationStrings:m_stringMap	[Lintradoc/util/IdcPerfectHash;
/*      */     //   820: iconst_0
/*      */     //   821: aaload
/*      */     //   822: invokevirtual 139	intradoc/util/IdcPerfectHash:copyFrom	(Lintradoc/util/IdcPerfectHash;)V
/*      */     //   825: aload 8
/*      */     //   827: iload 7
/*      */     //   829: iconst_1
/*      */     //   830: iadd
/*      */     //   831: invokevirtual 140	intradoc/util/IdcPerfectHash:setActiveMapping	(I)V
/*      */     //   834: aload_0
/*      */     //   835: getfield 70	intradoc/common/IdcLocalizationStrings:m_stringMap	[Lintradoc/util/IdcPerfectHash;
/*      */     //   838: iload 7
/*      */     //   840: aload 8
/*      */     //   842: aastore
/*      */     //   843: iinc 7 1
/*      */     //   846: goto -66 -> 780
/*      */     //   849: goto +27 -> 876
/*      */     //   852: astore 7
/*      */     //   854: new 31	intradoc/common/ServiceException
/*      */     //   857: dup
/*      */     //   858: aload 7
/*      */     //   860: invokespecial 122	intradoc/common/ServiceException:<init>	(Ljava/lang/Throwable;)V
/*      */     //   863: athrow
/*      */     //   864: astore 7
/*      */     //   866: new 31	intradoc/common/ServiceException
/*      */     //   869: dup
/*      */     //   870: aload 7
/*      */     //   872: invokespecial 122	intradoc/common/ServiceException:<init>	(Ljava/lang/Throwable;)V
/*      */     //   875: athrow
/*      */     //   876: aload_3
/*      */     //   877: aload 4
/*      */     //   879: invokestatic 141	intradoc/common/FileUtils:closeObjects	(Ljava/lang/Object;Ljava/lang/Object;)V
/*      */     //   882: aload 5
/*      */     //   884: aload 6
/*      */     //   886: invokestatic 141	intradoc/common/FileUtils:closeObjects	(Ljava/lang/Object;Ljava/lang/Object;)V
/*      */     //   889: goto +21 -> 910
/*      */     //   892: astore 11
/*      */     //   894: aload_3
/*      */     //   895: aload 4
/*      */     //   897: invokestatic 141	intradoc/common/FileUtils:closeObjects	(Ljava/lang/Object;Ljava/lang/Object;)V
/*      */     //   900: aload 5
/*      */     //   902: aload 6
/*      */     //   904: invokestatic 141	intradoc/common/FileUtils:closeObjects	(Ljava/lang/Object;Ljava/lang/Object;)V
/*      */     //   907: aload 11
/*      */     //   909: athrow
/*      */     //   910: return
/*      */     //
/*      */     // Exception table:
/*      */     //   from	to	target	type
/*      */     //   32	226	229	java/lang/ClassNotFoundException
/*      */     //   32	226	241	java/io/IOException
/*      */     //   267	463	466	java/io/IOException
/*      */     //   600	610	613	java/io/EOFException
/*      */     //   478	663	666	java/io/IOException
/*      */     //   478	663	678	java/lang/ClassNotFoundException
/*      */     //   690	849	852	java/io/IOException
/*      */     //   690	849	864	java/lang/ClassNotFoundException
/*      */     //   32	876	892	finally
/*      */     //   892	894	892	finally } 
/*  575 */   public String getString(String key, ExecutionContext context) { int appIndex = -1;
/*  576 */     String appName = null;
/*  577 */     if (context != null)
/*      */     {
/*  579 */       appName = (String)context.getLocaleResource(5);
/*      */     }
/*  581 */     if (appName == null)
/*      */     {
/*  583 */       appName = (String)LocaleResources.m_defaultContext.getLocaleResource(5);
/*      */     }
/*  585 */     if (appName != null)
/*      */     {
/*  587 */       appIndex = this.m_applicationMap.getCode(appName);
/*      */     }
/*      */ 
/*  590 */     String lang = null;
/*  591 */     if (context != null)
/*      */     {
/*  593 */       lang = (String)context.getLocaleResource(1);
/*      */     }
/*  595 */     if (lang == null)
/*      */     {
/*  597 */       lang = (String)LocaleResources.m_defaultContext.getLocaleResource(1);
/*      */     }
/*  599 */     if (lang == null)
/*      */     {
/*  601 */       lang = LocaleResources.m_baseLanguage;
/*      */     }
/*  603 */     int langIndex = this.m_languageMap.getCode(lang);
/*  604 */     if (langIndex == -1)
/*      */     {
/*  606 */       langIndex = this.m_defaultLanguageIndex;
/*      */     }
/*      */ 
/*  609 */     String str = getString(key, langIndex, appIndex);
/*  610 */     return str; }
/*      */ 
/*      */ 
/*      */   public String getString(String key, int langIndex, int appIndex)
/*      */   {
/*  615 */     if (langIndex == -1)
/*      */     {
/*  617 */       langIndex = this.m_defaultLanguageIndex;
/*      */     }
/*      */ 
/*  620 */     int index = this.m_stringMap[(appIndex + 1)].getActiveCode(key);
/*  621 */     if (index < 0)
/*      */     {
/*  623 */       return null;
/*      */     }
/*      */ 
/*  626 */     String str = getString(index, langIndex, appIndex);
/*  627 */     return str;
/*      */   }
/*      */ 
/*      */   public String getString(int stringIndex, int langIndex, int appIndex)
/*      */   {
/*  632 */     String s = null;
/*  633 */     int offsetBase = (this.m_languageMap.size() + 1) * stringIndex;
/*  634 */     int start = this.m_valLangOffsetData[(2 * (offsetBase + langIndex))];
/*  635 */     if (start < -1)
/*      */     {
/*  637 */       int index = start + 2;
/*  638 */       index *= -1;
/*  639 */       return (String)this.m_newValues.get(index);
/*      */     }
/*  641 */     if (start == -1)
/*      */     {
/*  643 */       return null;
/*      */     }
/*  645 */     int block = this.m_valLangBlockData[(offsetBase + langIndex)] & 0xFF;
/*  646 */     int length = this.m_valLangOffsetData[(2 * offsetBase + 2 * langIndex + 1)];
/*  647 */     if ((block == 0) && (length == 0) && (start == 0))
/*      */     {
/*  651 */       return null;
/*      */     }
/*  653 */     if (start >= 0)
/*      */     {
/*  655 */       if (this.m_langStringBlocks[block] == null)
/*      */       {
/*  657 */         String filename = findSourceFile("strings_" + block + ".dat");
/*  658 */         FileInputStream fis = null;
/*      */         ByteBuffer buf;
/*      */         try {
/*  661 */           fis = new FileInputStream(filename);
/*  662 */           FileChannel channel = fis.getChannel();
/*  663 */           buf = channel.map(FileChannel.MapMode.READ_ONLY, 0L, channel.size());
/*      */ 
/*  665 */           CharBuffer charbuf = buf.asCharBuffer();
/*  666 */           if (SystemUtils.m_verbose)
/*      */           {
/*  668 */             Report.debug("localization", "opened block " + block + " from file " + filename, null);
/*      */           }
/*      */ 
/*  671 */           this.m_langStringBlocks[block] = charbuf;
/*      */         }
/*      */         catch (IOException e)
/*      */         {
/*  675 */           if (appIndex >= 0)
/*      */           {
/*  677 */             s = attemptReloadAndReturn(e, stringIndex, langIndex, appIndex);
/*  678 */             buf = s;
/*      */ 
/*  687 */             FileUtils.closeObject(fis);
/*      */           }
/*  683 */           throw new AssertionError(e);
/*      */         }
/*      */         finally
/*      */         {
/*  687 */           FileUtils.closeObject(fis);
/*      */         }
/*      */       }
/*  690 */       CharBuffer cb = this.m_langStringBlocks[block];
/*  691 */       s = cb.subSequence(start + 2, start + length + 2).toString();
/*      */     }
/*  693 */     return s;
/*      */   }
/*      */ 
/*      */   protected synchronized String attemptReloadAndReturn(IOException ioe, int keyIndex, int langIndex, int appIndex)
/*      */   {
/*  702 */     int myVersion = this.m_versionNumber;
/*      */     try
/*      */     {
/*  705 */       Report.trace("localization", "attempting string index reload.", ioe);
/*  706 */       readConfigFile();
/*  707 */       if (myVersion != this.m_versionNumber)
/*      */       {
/*  709 */         String app = (String)this.m_applicationMap.get(appIndex);
/*  710 */         String key = (String)this.m_stringMap[appIndex].get(keyIndex);
/*  711 */         String lang = (String)this.m_languageMap.get(langIndex);
/*  712 */         Report.trace("localization", "reloading index to look up value for key " + key + " lang " + lang, null);
/*      */ 
/*  714 */         loadStringIndex();
/*      */ 
/*  716 */         int newAppIndex = this.m_applicationMap.getCode(app);
/*  717 */         int newKeyIndex = this.m_stringMap[newAppIndex].getCode(key);
/*  718 */         int newLangIndex = this.m_languageMap.getCode(lang);
/*  719 */         return getString(newKeyIndex, newLangIndex, newAppIndex);
/*      */       }
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/*  724 */       e.addCause(ioe);
/*  725 */       throw new AssertionError(e);
/*      */     }
/*  727 */     throw new AssertionError(ioe);
/*      */   }
/*      */ 
/*      */   public synchronized void reserve(String task) throws ServiceException
/*      */   {
/*  732 */     if (SystemUtils.m_verbose)
/*      */     {
/*  734 */       Report.debug("localization", null, new StackTrace("reserving for " + task));
/*      */     }
/*  736 */     if (this.m_activeTask != null)
/*      */     {
/*  738 */       throw new AssertionError();
/*      */     }
/*  740 */     FileUtils.reserveLongTermLock(this.m_dataDir, "strings", task, 0L, true);
/*  741 */     this.m_activeTask = task;
/*      */   }
/*      */ 
/*      */   public synchronized void release()
/*      */   {
/*  746 */     if (SystemUtils.m_verbose)
/*      */     {
/*  748 */       Report.debug("localization", null, new StackTrace("releasing for " + this.m_activeTask));
/*      */     }
/*  750 */     String task = this.m_activeTask;
/*  751 */     this.m_activeTask = null;
/*  752 */     if (task == null)
/*      */       return;
/*  754 */     FileUtils.releaseLongTermLock(this.m_dataDir, "strings", task);
/*      */   }
/*      */ 
/*      */   public IdcLocalizationStrings duplicateIndex()
/*      */     throws ServiceException
/*      */   {
/*  763 */     IdcLocalizationStrings newStrings = new IdcLocalizationStrings(this.m_environment, this.m_dataDir, this.m_localDir, this.m_progress);
/*      */ 
/*  766 */     newStrings.m_versionNumber = this.m_versionNumber;
/*  767 */     newStrings.m_oldVersionNumber = this.m_oldVersionNumber;
/*      */ 
/*  769 */     newStrings.m_activeTask = this.m_activeTask;
/*      */ 
/*  771 */     newStrings.m_stringMap = new IdcPerfectHash[this.m_stringMap.length];
/*  772 */     for (int i = 0; i < this.m_stringMap.length; ++i)
/*      */     {
/*  774 */       newStrings.m_stringMap[i] = new IdcPerfectHash();
/*  775 */       newStrings.m_stringMap[i].copyFrom(this.m_stringMap[i]);
/*      */     }
/*      */ 
/*  778 */     newStrings.m_languageMap = new IdcPerfectHash();
/*  779 */     newStrings.m_languageMap.copyFrom(this.m_languageMap);
/*      */ 
/*  781 */     newStrings.m_applicationMap = new IdcPerfectHash();
/*  782 */     newStrings.m_applicationMap.copyFrom(this.m_applicationMap);
/*      */ 
/*  784 */     newStrings.m_defaultLanguageIndex = this.m_defaultLanguageIndex;
/*      */ 
/*  786 */     newStrings.m_langStringBlocks = new CharBuffer[256];
/*      */ 
/*  788 */     newStrings.m_valLangBlockData = new byte[this.m_valLangBlockData.length];
/*  789 */     System.arraycopy(this.m_valLangBlockData, 0, newStrings.m_valLangBlockData, 0, newStrings.m_valLangBlockData.length);
/*      */ 
/*  792 */     newStrings.m_valLangOffsetData = new int[this.m_valLangOffsetData.length];
/*  793 */     System.arraycopy(this.m_valLangOffsetData, 0, newStrings.m_valLangOffsetData, 0, newStrings.m_valLangOffsetData.length);
/*      */ 
/*  796 */     newStrings.m_valLangSourceData = new short[this.m_valLangSourceData.length];
/*  797 */     System.arraycopy(this.m_valLangSourceData, 0, newStrings.m_valLangSourceData, 0, newStrings.m_valLangSourceData.length);
/*      */ 
/*  800 */     newStrings.m_sourceFileInfo = ((HashMap)this.m_sourceFileInfo.clone());
/*      */ 
/*  802 */     newStrings.m_maxSourceFileValue = this.m_maxSourceFileValue;
/*      */ 
/*  804 */     newStrings.m_tagLists = ((HashMap)this.m_tagLists.clone());
/*      */ 
/*  806 */     return newStrings;
/*      */   }
/*      */ 
/*      */   public void prepareIncrementalUpdate() throws IOException
/*      */   {
/*  811 */     if (this.m_readyForIncrementalUpdate)
/*      */     {
/*  813 */       return;
/*      */     }
/*      */ 
/*  816 */     this.m_progress.reportProgress(4, "indexing starting", 0.0F, 100.0F);
/*      */     try
/*      */     {
/*  820 */       readConfigFile();
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/*  824 */       throw new IOException(e);
/*      */     }
/*  826 */     this.m_oldVersionNumber = this.m_versionNumber;
/*      */ 
/*  828 */     this.m_work = new List[257];
/*  829 */     this.m_workCount = 0;
/*  830 */     this.m_fileCount = 0;
/*  831 */     this.m_newKeys = new ArrayList();
/*  832 */     this.m_newValues = new ArrayList();
/*      */ 
/*  834 */     if (this.m_stringMap == null)
/*      */     {
/*  838 */       this.m_oldVersionNumber = -1;
/*  839 */       Random random = new Random(System.currentTimeMillis());
/*  840 */       this.m_stringMap = new IdcPerfectHash[1];
/*  841 */       this.m_stringMap[0] = new IdcPerfectHash(random);
/*  842 */       this.m_languageMap = new IdcPerfectHash(random);
/*  843 */       for (int i = 0; i < LocaleResources.m_languages.size(); ++i)
/*      */       {
/*  845 */         this.m_languageMap.add(LocaleResources.m_languages.get(i));
/*      */       }
/*  847 */       this.m_applicationMap = new IdcPerfectHash(random);
/*  848 */       this.m_langStringBlocks = new CharBuffer[256];
/*  849 */       this.m_tagLists = new HashMap();
/*      */     }
/*      */ 
/*  852 */     int appCount = this.m_applicationMap.size();
/*  853 */     this.m_keys = new Map[appCount + 1];
/*  854 */     int keyCount = this.m_stringMap[0].size();
/*  855 */     for (int i = 0; i <= appCount; ++i)
/*      */     {
/*  857 */       this.m_keys[i] = new HashMap();
/*  858 */       for (int j = 0; j < keyCount; ++j)
/*      */       {
/*  860 */         String key = (String)this.m_stringMap[0].get(j);
/*  861 */         int activeCode = this.m_stringMap[i].getActiveCode(key);
/*  862 */         this.m_keys[i].put(key, new Integer(activeCode));
/*  863 */         if (activeCode < this.m_stringValueCount)
/*      */           continue;
/*  865 */         this.m_stringValueCount = (activeCode + 1);
/*      */       }
/*      */     }
/*      */ 
/*  869 */     this.m_readyForIncrementalUpdate = true;
/*      */   }
/*      */ 
/*      */   public synchronized void finishIncrementalUpdate() throws IOException
/*      */   {
/*      */     try
/*      */     {
/*  876 */       this.m_readyForIncrementalUpdate = false;
/*  877 */       if (!this.m_foundIncrementalWork)
/*      */       {
/*      */         return;
/*      */       }
/*      */ 
/*  883 */       this.m_foundIncrementalWork = false;
/*  884 */       processIncrementalWork();
/*      */ 
/*  886 */       int appSize = this.m_applicationMap.size();
/*  887 */       IdcPerfectHash[] newMap = new IdcPerfectHash[appSize + 1];
/*  888 */       newMap[0] = this.m_stringMap[0];
/*  889 */       this.m_stringMap = newMap;
/*  890 */       int[] edges = this.m_stringMap[0].hashify();
/*      */ 
/*  892 */       for (int i = 0; i <= appSize; ++i)
/*      */       {
/*  897 */         this.m_stringMap[0].resetVertexValues(i + 1);
/*      */       }
/*      */ 
/*  900 */       for (int i = 0; i <= appSize; ++i)
/*      */       {
/*  902 */         IdcPerfectHash appMap = this.m_stringMap[0];
/*  903 */         if (i > 0)
/*      */         {
/*  905 */           appMap = new IdcPerfectHash();
/*  906 */           appMap.copyFrom(this.m_stringMap[0]);
/*      */         }
/*  908 */         appMap.setActiveMapping(i + 1);
/*      */         try
/*      */         {
/*  911 */           appMap.walk(appMap.getMapping(i + 1), edges, this.m_keys[i]);
/*      */         }
/*      */         catch (IdcException e)
/*      */         {
/*  915 */           IOException ioe = new IOException();
/*  916 */           ioe.initCause(e);
/*  917 */           throw ioe;
/*      */         }
/*  919 */         this.m_stringMap[i] = appMap;
/*      */       }
/*      */ 
/*  922 */       this.m_work = null;
/*  923 */       this.m_keys = null;
/*  924 */       this.m_newValues = null;
/*  925 */       this.m_newKeys = null;
/*      */       try
/*      */       {
/*  929 */         FileUtils.copyDirectoryWithFlags(new File(getStringSourceDir()), new File(getStringTargetDir()), 0, null, 12);
/*      */       }
/*      */       catch (ServiceException e)
/*      */       {
/*  935 */         IOException ioe = new IOException();
/*  936 */         e.wrapIn(ioe);
/*  937 */         throw ioe;
/*      */       }
/*      */ 
/*  940 */       saveIndex();
/*      */ 
/*  942 */       String configFile = getConfigFileName();
/*  943 */       OutputStream out = null;
/*      */       try
/*      */       {
/*  946 */         int flags = 16;
/*  947 */         out = FileUtils.openOutputStream(configFile, flags);
/*  948 */         Properties props = new Properties();
/*  949 */         props.put("version", "" + this.m_versionNumber);
/*  950 */         props.store(out, null);
/*  951 */         out.close();
/*  952 */         out = null;
/*      */       }
/*      */       finally
/*      */       {
/*  956 */         FileUtils.abort(out);
/*  957 */         FileUtils.closeObject(out);
/*      */       }
/*      */     }
/*      */     finally
/*      */     {
/*      */       try
/*      */       {
/*  964 */         if (this.m_activeTask != null)
/*      */         {
/*  966 */           release();
/*      */         }
/*      */       }
/*      */       finally
/*      */       {
/*  971 */         this.m_progress.reportProgress(2, "finished indexing", 100.0F, 100.0F);
/*      */       }
/*      */     }
/*      */ 
/*  975 */     cleanup();
/*      */   }
/*      */ 
/*      */   public String canonicalizeFileName(String name)
/*      */   {
/* 1015 */     String idcDir = FileUtils.directorySlashes(this.m_environment.getProperty("IntradocDir"));
/* 1016 */     String homeDir = this.m_environment.getProperty("IdcHomeDir");
/* 1017 */     if (homeDir == null)
/*      */     {
/* 1019 */       homeDir = idcDir;
/*      */     }
/*      */     else
/*      */     {
/* 1023 */       homeDir = FileUtils.directorySlashes(homeDir);
/*      */     }
/* 1025 */     name = FileUtils.fileSlashes(name);
/*      */ 
/* 1027 */     if (name.startsWith(homeDir))
/*      */     {
/* 1029 */       name = "$IdcHomeDir/" + name.substring(homeDir.length());
/*      */     }
/* 1031 */     else if (name.startsWith(idcDir))
/*      */     {
/* 1033 */       name = "$IntradocDir/" + name.substring(idcDir.length());
/*      */     }
/*      */     else
/*      */     {
/* 1042 */       String[] parts = name.split("/");
/* 1043 */       int i = parts.length - 4;
/* 1044 */       if (i < 0)
/*      */       {
/* 1046 */         i = 0;
/*      */       }
/* 1048 */       name = "";
/* 1049 */       while (i < parts.length)
/*      */       {
/* 1051 */         if (name.length() > 0)
/*      */         {
/* 1053 */           name = name + '/';
/*      */         }
/* 1055 */         name = name + parts[i];
/* 1056 */         ++i;
/*      */       }
/*      */     }
/* 1059 */     return name;
/*      */   }
/*      */ 
/*      */   public Map<String, String> getFileLoadInfo(String name)
/*      */   {
/* 1068 */     name = canonicalizeFileName(name);
/* 1069 */     return (Map)this.m_sourceFileInfo.get(name);
/*      */   }
/*      */ 
/*      */   public ReportProgress getReportProgress()
/*      */   {
/* 1074 */     return this.m_progress;
/*      */   }
/*      */ 
/*      */   public void handleStreamEvent(String eventId, Object fileInfoObject, Object data)
/*      */     throws IOException
/*      */   {
/* 1080 */     HashMap fileInfo = (HashMap)fileInfoObject;
/* 1081 */     String fileName = canonicalizeFileName((String)fileInfo.get("file"));
/* 1082 */     HashMap sourceInfo = (HashMap)this.m_sourceFileInfo.get(fileName);
/* 1083 */     if (sourceInfo == null)
/*      */     {
/* 1085 */       sourceInfo = fileInfo;
/*      */     }
/* 1087 */     this.m_fileCount += 1;
/* 1088 */     this.m_progress.reportProgress(1, "processing strings from " + fileName, this.m_fileCount, 25 + this.m_fileCount);
/*      */ 
/* 1090 */     String orderStr = (String)sourceInfo.get("loadOrder");
/* 1091 */     ResourceContainer res = (ResourceContainer)data;
/* 1092 */     List newResourceList = new IdcVector();
/* 1093 */     List resourceList = res.m_resourceList;
/* 1094 */     Map stringObjMap = res.m_stringObjMap;
/*      */ 
/* 1096 */     this.m_interval = null;
/* 1097 */     if (SystemUtils.isActiveTrace("localization"))
/*      */     {
/* 1099 */       this.m_interval = new IntervalData(" handleStreamEvent()");
/*      */     }
/*      */ 
/* 1102 */     boolean fileIsOnlyStrings = true;
/* 1103 */     int langCount = -1;
/* 1104 */     short fileIndex = -1;
/* 1105 */     IdcStringBuilder builder = new IdcStringBuilder();
/* 1106 */     Map processedKeys = new HashMap();
/*      */ 
/* 1108 */     for (ResourceObject resObj : resourceList)
/*      */     {
/* 1110 */       if (resObj.m_type != 0)
/*      */       {
/* 1112 */         fileIsOnlyStrings = false;
/* 1113 */         newResourceList.add(resObj);
/*      */       }
/*      */ 
/* 1116 */       if (langCount == -1)
/*      */       {
/* 1118 */         if (orderStr == null)
/*      */         {
/* 1120 */           orderStr = "" + (this.m_maxSourceFileValue = (short)(this.m_maxSourceFileValue + 1));
/* 1121 */           if (this.m_maxSourceFileValue == 0)
/*      */           {
/* 1123 */             throw new AssertionError("!$More than 256 string source files found.");
/*      */           }
/* 1125 */           sourceInfo.put("loadOrder", orderStr);
/*      */         }
/* 1127 */         this.m_sourceFileInfo.put(fileName, sourceInfo);
/* 1128 */         fileIndex = (short)NumberUtils.parseInteger(orderStr, -1);
/*      */ 
/* 1130 */         prepareIncrementalUpdate();
/* 1131 */         assert (this.m_languageMap != null);
/* 1132 */         langCount = this.m_languageMap.size();
/*      */       }
/*      */ 
/* 1135 */       String fullKey = resObj.m_name;
/* 1136 */       int dotIndex = fullKey.lastIndexOf(".");
/* 1137 */       int braceIndex = fullKey.indexOf("[");
/* 1138 */       if ((dotIndex > 0) && (braceIndex > 0))
/*      */       {
/* 1140 */         fullKey = fullKey.substring(dotIndex + 1, braceIndex);
/*      */       }
/* 1142 */       else if (dotIndex > 0)
/*      */       {
/* 1144 */         fullKey = fullKey.substring(dotIndex + 1);
/*      */       }
/* 1146 */       else if (braceIndex > 0)
/*      */       {
/* 1148 */         fullKey = fullKey.substring(0, braceIndex);
/*      */       }
/*      */ 
/* 1151 */       if (processedKeys.get(fullKey) != null)
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/* 1156 */       if (fullKey.charAt(0) == '!')
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/* 1161 */       processedKeys.put(fullKey, fullKey);
/*      */ 
/* 1163 */       int appIndex = fullKey.lastIndexOf(47);
/* 1164 */       String key = fullKey;
/* 1165 */       String app = null;
/* 1166 */       if (appIndex > 0)
/*      */       {
/* 1168 */         key = fullKey.substring(0, appIndex);
/* 1169 */         app = fullKey.substring(appIndex + 1);
/*      */         int appSize;
/* 1170 */         if (this.m_applicationMap.get(app) == null)
/*      */         {
/* 1172 */           this.m_applicationMap.add(app);
/* 1173 */           appSize = this.m_applicationMap.size();
/*      */ 
/* 1175 */           Map[] newKeys = new Map[appSize + 1];
/* 1176 */           System.arraycopy(this.m_keys, 0, newKeys, 0, this.m_keys.length);
/* 1177 */           this.m_keys = newKeys;
/* 1178 */           this.m_keys[appSize] = new HashMap();
/* 1179 */           for (Map.Entry ent : this.m_keys[0].entrySet())
/*      */           {
/* 1181 */             String entryKey = (String)ent.getKey();
/* 1182 */             Integer entryVal = (Integer)ent.getValue();
/* 1183 */             int tmpVal = entryVal.intValue() + 1;
/* 1184 */             tmpVal *= -1;
/* 1185 */             this.m_keys[appSize].put(entryKey, new Integer(tmpVal));
/*      */           }
/*      */         }
/* 1188 */         appIndex = this.m_applicationMap.getCode(app);
/*      */       }
/* 1190 */       ++appIndex;
/* 1191 */       int stringIndex = 0;
/* 1192 */       Integer intObject = (Integer)this.m_keys[appIndex].get(key);
/* 1193 */       if (intObject != null)
/*      */       {
/* 1195 */         stringIndex = intObject.intValue();
/*      */       }
/* 1197 */       boolean isNew = false;
/* 1198 */       if (intObject == null)
/*      */       {
/* 1200 */         isNew = true;
/* 1201 */         this.m_newKeys.add(key);
/*      */ 
/* 1203 */         stringIndex = this.m_stringValueCount++;
/* 1204 */         for (Map keyMap : this.m_keys)
/*      */         {
/* 1210 */           keyMap.put(key, new Integer(-1 * stringIndex - 1));
/*      */         }
/* 1212 */         this.m_keys[appIndex].put(key, new Integer(stringIndex));
/* 1213 */         if (this.m_valLangBlockData == null)
/*      */         {
/* 1215 */           this.m_valLangBlockData = new byte[''];
/* 1216 */           this.m_valLangOffsetData = new int[256];
/* 1217 */           this.m_valLangSourceData = new short[''];
/*      */         }
/* 1219 */         if (this.m_stringValueCount * (langCount + 1) > this.m_valLangBlockData.length)
/*      */         {
/* 1221 */           int newLen = this.m_valLangBlockData.length * 2;
/* 1222 */           byte[] tmpBlockData = new byte[newLen];
/* 1223 */           System.arraycopy(this.m_valLangBlockData, 0, tmpBlockData, 0, this.m_valLangBlockData.length);
/* 1224 */           this.m_valLangBlockData = tmpBlockData;
/*      */ 
/* 1226 */           short[] tmpSourceData = new short[newLen];
/* 1227 */           System.arraycopy(this.m_valLangSourceData, 0, tmpSourceData, 0, this.m_valLangSourceData.length);
/* 1228 */           this.m_valLangSourceData = tmpSourceData;
/*      */ 
/* 1230 */           newLen = this.m_valLangOffsetData.length * 2;
/* 1231 */           int[] tmpOffsetData = new int[newLen];
/* 1232 */           System.arraycopy(this.m_valLangOffsetData, 0, tmpOffsetData, 0, this.m_valLangOffsetData.length);
/* 1233 */           this.m_valLangOffsetData = tmpOffsetData;
/*      */         }
/*      */ 
/*      */       }
/* 1238 */       else if (stringIndex < 0)
/*      */       {
/* 1242 */         stringIndex = this.m_stringValueCount++;
/* 1243 */         this.m_keys[appIndex].put(key, new Integer(stringIndex));
/*      */       }
/*      */ 
/* 1247 */       IdcLocaleString lcString = (IdcLocaleString)stringObjMap.get(fullKey);
/* 1248 */       assert (lcString != null) : ("!$Unable to find string " + fullKey);
/*      */ 
/* 1250 */       for (int langIndex = 0; langIndex <= langCount; ++langIndex)
/*      */       {
/* 1252 */         String str = null;
/* 1253 */         if (langIndex == langCount)
/*      */         {
/* 1255 */           if ((lcString.m_attributes != null) && (lcString.m_attributes.size() > 0))
/*      */           {
/* 1257 */             List l = new ArrayList();
/* 1258 */             for (String k : lcString.m_attributes.keySet())
/*      */             {
/* 1260 */               String v = (String)lcString.m_attributes.get(k);
/* 1261 */               if (v.length() == 0) {
/*      */                 continue;
/*      */               }
/*      */ 
/* 1265 */               if (v.equals("true"))
/*      */               {
/* 1267 */                 HashMap keyList = (HashMap)this.m_tagLists.get(k);
/* 1268 */                 if (keyList == null)
/*      */                 {
/* 1270 */                   keyList = new HashMap();
/* 1271 */                   this.m_tagLists.put(k, keyList);
/*      */                 }
/* 1273 */                 keyList.put(key, key);
/*      */               }
/* 1275 */               builder.setLength(0);
/* 1276 */               builder.append2(k, '=');
/* 1277 */               builder.append(v);
/* 1278 */               l.add(builder.toStringNoRelease());
/*      */             }
/* 1280 */             if (l.size() > 0)
/*      */             {
/* 1282 */               str = StringUtils.createStringSimple(l);
/*      */             }
/*      */           }
/*      */         }
/* 1286 */         else if ((lcString.m_values != null) && (lcString.m_values.length > langIndex))
/*      */         {
/* 1288 */           str = lcString.m_values[langIndex];
/*      */         }
/* 1290 */         if (str == null)
/*      */         {
/*      */           continue;
/*      */         }
/*      */ 
/* 1295 */         if (!isNew)
/*      */         {
/* 1297 */           int currentFileNumber = this.m_valLangSourceData[(stringIndex * (langCount + 1) + langIndex)];
/* 1298 */           if (currentFileNumber > fileIndex)
/*      */           {
/* 1301 */             if (!SystemUtils.m_verbose)
/*      */               continue;
/* 1303 */             Report.debug("localization", "key " + key + "=" + str + " is going to be overridden so not setting.", null);
/*      */           }
/*      */           else
/*      */           {
/* 1309 */             String currentStr = getString(stringIndex, langIndex, -1);
/* 1310 */             if (currentStr == null)
/*      */             {
/* 1312 */               isNew = true;
/*      */             }
/* 1314 */             else if (currentStr.equals(str))
/*      */             {
/* 1317 */               if (!SystemUtils.m_verbose)
/*      */                 continue;
/* 1319 */               Report.debug("localization", "key " + key + "=" + currentStr + " is unchanged.", null);
/*      */             }
/*      */           }
/*      */ 
/*      */         }
/*      */         else
/*      */         {
/* 1326 */           int offsetBase = (langCount + 1) * stringIndex;
/*      */           int block;
/*      */           int block;
/* 1327 */           if (isNew)
/*      */           {
/* 1330 */             int index = this.m_newValues.size();
/* 1331 */             this.m_newValues.add(str);
/* 1332 */             index *= -1;
/* 1333 */             index -= 2;
/* 1334 */             this.m_valLangOffsetData[(2 * (offsetBase + langIndex))] = index;
/* 1335 */             block = 256;
/*      */           }
/*      */           else
/*      */           {
/* 1339 */             block = this.m_valLangBlockData[(offsetBase + langIndex)] & 0xFF;
/*      */           }
/* 1341 */           this.m_valLangSourceData[(offsetBase + langIndex)] = fileIndex;
/* 1342 */           WorkObject wo = new WorkObject(stringIndex, langIndex, this.m_workCount, str);
/* 1343 */           if (this.m_work[block] == null)
/*      */           {
/* 1345 */             this.m_work[block] = new ArrayList();
/*      */           }
/* 1347 */           this.m_work[block].add(wo);
/* 1348 */           this.m_workCount += 1;
/* 1349 */           if (!this.m_foundIncrementalWork)
/*      */           {
/* 1351 */             this.m_versionNumber += 1;
/*      */             try
/*      */             {
/* 1355 */               FileUtils.checkOrCreateDirectory(this.m_dataDir, 2, 1);
/*      */ 
/* 1357 */               String stringDir = getStringTargetDir();
/* 1358 */               FileUtils.deleteDirectory(new File(stringDir), true);
/* 1359 */               FileUtils.checkOrCreateDirectory(stringDir, 1, 0);
/*      */             }
/*      */             catch (ServiceException e)
/*      */             {
/* 1363 */               IOException ioe = new IOException();
/* 1364 */               e.wrapIn(ioe);
/* 1365 */               throw ioe;
/*      */             }
/* 1367 */             this.m_foundIncrementalWork = true;
/*      */           }
/* 1369 */           isNew = false;
/*      */         }
/*      */       }
/* 1372 */       if (this.m_workCount > 1000)
/*      */       {
/* 1374 */         processIncrementalWork();
/*      */       }
/*      */     }
/*      */ 
/* 1378 */     stringObjMap.clear();
/* 1379 */     resourceList.clear();
/* 1380 */     res.m_resourceList = newResourceList;
/* 1381 */     sourceInfo.put("onlyStrings", "" + fileIsOnlyStrings);
/*      */ 
/* 1383 */     builder.releaseBuffers();
/* 1384 */     if (this.m_languageMap == null)
/*      */       return;
/*      */   }
/*      */ 
/*      */   protected String findSourceFile(String name)
/*      */   {
/* 1396 */     String d = getStringTargetDir();
/* 1397 */     String f = FileUtils.getAbsolutePath(d, name);
/* 1398 */     if ((d != null) && (FileUtils.checkFile(f, 1) == 0))
/*      */     {
/* 1400 */       if (SystemUtils.m_verbose)
/*      */       {
/* 1402 */         Report.debug("localization", "found source file " + f, null);
/*      */       }
/* 1404 */       return f;
/*      */     }
/* 1406 */     d = getStringSourceDir();
/* 1407 */     f = FileUtils.getAbsolutePath(d, name);
/* 1408 */     if (SystemUtils.m_verbose)
/*      */     {
/* 1410 */       Report.debug("localization", "found source file " + f, null);
/*      */     }
/* 1412 */     return f;
/*      */   }
/*      */ 
/*      */   protected String findTargetFile(String name)
/*      */   {
/* 1417 */     String d = getStringTargetDir();
/* 1418 */     String f = FileUtils.getAbsolutePath(d, name);
/* 1419 */     if (SystemUtils.m_verbose)
/*      */     {
/* 1421 */       Report.debug("localization", "using target file " + f, null);
/*      */     }
/* 1423 */     return f;
/*      */   }
/*      */ 
/*      */   protected void processIncrementalWork() throws IOException
/*      */   {
/* 1428 */     OutputStream out = null;
/* 1429 */     IdcStringBuilder builder = new IdcStringBuilder();
/* 1430 */     builder.m_disableToStringReleaseBuffers = true;
/*      */ 
/* 1432 */     int flags = 16;
/*      */ 
/* 1434 */     String sourceBlockFile = null;
/* 1435 */     String targetBlockFile = null;
/*      */ 
/* 1437 */     int langCount = this.m_languageMap.size();
/* 1438 */     int keyCount = this.m_stringMap[0].size();
/*      */ 
/* 1440 */     if (this.m_newKeys == null)
/*      */     {
/* 1442 */       if (SystemUtils.m_verbose)
/*      */       {
/* 1444 */         Report.debug("localization", "processIncrementalWork() called with no work.", null);
/*      */       }
/* 1446 */       return;
/*      */     }
/*      */ 
/* 1449 */     for (String key : this.m_newKeys)
/*      */     {
/* 1451 */       this.m_stringMap[0].add(key);
/*      */     }
/* 1453 */     this.m_newKeys = new ArrayList();
/*      */ 
/* 1455 */     DataOutputStream blockOutput = null;
/* 1456 */     DataInputStream blockInput = null;
/* 1457 */     byte[] buf = new byte[256];
/*      */     try
/*      */     {
/* 1460 */       int stringsPerBlock = (langCount + 1) * keyCount / 256 + 1;
/* 1461 */       if (stringsPerBlock < 1000)
/*      */       {
/* 1463 */         stringsPerBlock = 1000;
/*      */       }
/*      */ 
/* 1466 */       int mostRecentBlock = 0;
/* 1467 */       for (int i = 0; i < 257; ++i)
/*      */       {
/* 1469 */         long inputOffset = 0L;
/* 1470 */         long outputOffset = 0L;
/* 1471 */         int block = i;
/* 1472 */         if (i == 256)
/*      */         {
/* 1474 */           block = mostRecentBlock;
/*      */         }
/* 1476 */         sourceBlockFile = findSourceFile("strings_" + block + ".dat");
/* 1477 */         targetBlockFile = findTargetFile("strings_" + block + ".dat");
/* 1478 */         File f = new File(sourceBlockFile);
/* 1479 */         long fileLengthInChars = -1L;
/* 1480 */         if (f.exists())
/*      */         {
/* 1482 */           fileLengthInChars = f.length() / 2L;
/* 1483 */           mostRecentBlock = block;
/*      */         }
/* 1485 */         List work = this.m_work[i];
/* 1486 */         if (work == null) {
/*      */           continue;
/*      */         }
/*      */ 
/* 1490 */         if (i != 256)
/*      */         {
/* 1494 */           Sort.sortList(work, (IdcComparator)work.get(0));
/*      */         }
/* 1496 */         if (fileLengthInChars >= 0L)
/*      */         {
/* 1498 */           if (i < 256)
/*      */           {
/* 1500 */             blockInput = new DataInputStream(new BufferedInputStream(new FileInputStream(sourceBlockFile)));
/*      */           }
/*      */           else
/*      */           {
/* 1506 */             outputOffset = f.length() / 2L;
/* 1507 */             if (this.m_appendedStrings >= stringsPerBlock)
/*      */             {
/* 1509 */               ++mostRecentBlock;
/* 1510 */               ++block;
/* 1511 */               this.m_appendedStrings = 0;
/* 1512 */               if (block >= 256)
/*      */               {
/* 1514 */                 Report.trace("localization", "overflowed string blocks, opening block 0", null);
/*      */ 
/* 1516 */                 mostRecentBlock = block = 0;
/*      */               }
/*      */               else
/*      */               {
/* 1520 */                 Report.trace("localization", "opening block " + block, null);
/*      */               }
/* 1522 */               sourceBlockFile = findSourceFile("strings_" + block + ".dat");
/* 1523 */               targetBlockFile = findTargetFile("strings_" + block + ".dat");
/* 1524 */               f = new File(sourceBlockFile);
/* 1525 */               outputOffset = f.length() / 2L;
/*      */             }
/*      */ 
/* 1528 */             if (f.exists())
/*      */             {
/* 1530 */               fileLengthInChars = f.length() / 2L;
/* 1531 */               if (!sourceBlockFile.equals(targetBlockFile))
/*      */               {
/*      */                 try
/*      */                 {
/* 1535 */                   FileUtils.copyFile(sourceBlockFile, targetBlockFile);
/*      */                 }
/*      */                 catch (ServiceException e)
/*      */                 {
/* 1539 */                   IOException ioe = new IOException();
/* 1540 */                   ioe.initCause(e);
/* 1541 */                   throw ioe;
/*      */                 }
/*      */               }
/* 1544 */               out = new FileOutputStream(targetBlockFile, true);
/*      */             }
/*      */             else
/*      */             {
/* 1548 */               fileLengthInChars = 0L;
/*      */             }
/*      */           }
/*      */         }
/* 1552 */         if (out == null)
/*      */         {
/* 1556 */           out = FileUtils.openOutputStream(targetBlockFile, flags);
/* 1557 */           outputOffset = 0L;
/* 1558 */           this.m_appendedStrings = 0;
/*      */         }
/* 1560 */         blockOutput = new DataOutputStream(new BufferedOutputStream(out));
/*      */ 
/* 1562 */         int offsetDelta = 0;
/* 1563 */         int workSize = work.size();
/* 1564 */         WorkObject lastWork = null;
/* 1565 */         for (int workIndex = 0; workIndex < workSize; ++workIndex)
/*      */         {
/* 1567 */           WorkObject wo = (WorkObject)work.get(workIndex);
/* 1568 */           int baseOffset = wo.m_stringIndex * (langCount + 1);
/* 1569 */           int valOffset = this.m_valLangOffsetData[(2 * (baseOffset + wo.m_langIndex))];
/* 1570 */           if ((blockInput != null) && (valOffset < inputOffset))
/*      */           {
/* 1578 */             if (workIndex + 1 != workSize) {
/*      */               continue;
/*      */             }
/*      */ 
/* 1582 */             valOffset = (int)fileLengthInChars;
/*      */           }
/*      */ 
/* 1590 */           if (blockInput != null) {
/*      */             while (true) {
/* 1592 */               if (inputOffset >= fileLengthInChars)
/*      */                 break label1192;
/* 1594 */               int indexLocation = blockInput.readInt();
/* 1595 */               int oldValLength = this.m_valLangOffsetData[(indexLocation + 1)];
/* 1596 */               if (buf.length < 2 * oldValLength)
/*      */               {
/* 1598 */                 buf = new byte[2 * oldValLength];
/*      */               }
/* 1600 */               blockInput.read(buf, 0, 2 * oldValLength);
/* 1601 */               boolean isUpdatedString = inputOffset == valOffset;
/* 1602 */               inputOffset += 2L;
/* 1603 */               inputOffset += oldValLength;
/*      */ 
/* 1605 */               this.m_valLangOffsetData[indexLocation] += offsetDelta;
/* 1606 */               blockOutput.writeInt(indexLocation);
/* 1607 */               if (isUpdatedString)
/*      */               {
/* 1610 */                 int l = wo.m_value.length();
/* 1611 */                 byte[] buf2 = wo.m_value.getBytes("UTF-16BE");
/* 1612 */                 blockOutput.write(buf2, 0, buf2.length);
/* 1613 */                 outputOffset += 2L;
/* 1614 */                 outputOffset += l;
/*      */ 
/* 1617 */                 this.m_valLangOffsetData[(indexLocation + 1)] = l;
/*      */ 
/* 1620 */                 offsetDelta += l - oldValLength;
/*      */ 
/* 1623 */                 if (workIndex + 1 != workSize)
/*      */                 {
/*      */                   break label1192;
/*      */                 }
/*      */ 
/* 1629 */                 valOffset = (int)fileLengthInChars;
/* 1630 */                 wo = null;
/*      */               }
/*      */               else
/*      */               {
/* 1647 */                 blockOutput.write(buf, 0, 2 * oldValLength);
/* 1648 */                 outputOffset += 2L;
/* 1649 */                 outputOffset += oldValLength;
/*      */               }
/*      */             }
/*      */ 
/*      */           }
/*      */ 
/* 1655 */           if ((lastWork != null) && (lastWork.m_stringIndex == wo.m_stringIndex) && (lastWork.m_langIndex == wo.m_langIndex))
/*      */           {
/*      */             continue;
/*      */           }
/*      */ 
/* 1663 */           int strOffsetIndex = 2 * (wo.m_stringIndex * (langCount + 1) + wo.m_langIndex);
/* 1664 */           blockOutput.writeInt(strOffsetIndex);
/* 1665 */           int l = 2 * wo.m_value.length();
/* 1666 */           if (buf.length < l)
/*      */           {
/* 1668 */             buf = new byte[l];
/*      */           }
/* 1670 */           buf = wo.m_value.getBytes("UTF-16BE");
/* 1671 */           blockOutput.write(buf, 0, buf.length);
/* 1672 */           this.m_appendedStrings += 1;
/* 1673 */           this.m_valLangBlockData[(wo.m_stringIndex * (langCount + 1) + wo.m_langIndex)] = (byte)mostRecentBlock;
/* 1674 */           this.m_valLangOffsetData[strOffsetIndex] = (int)outputOffset;
/* 1675 */           this.m_valLangOffsetData[(strOffsetIndex + 1)] = (l / 2);
/* 1676 */           outputOffset += 2L;
/* 1677 */           outputOffset += buf.length / 2;
/*      */ 
/* 1679 */           label1192: lastWork = wo;
/*      */         }
/*      */ 
/* 1682 */         if (blockInput != null)
/*      */         {
/* 1684 */           blockInput.close();
/* 1685 */           blockInput = null;
/*      */         }
/* 1687 */         closeOutput(blockOutput, targetBlockFile, mostRecentBlock);
/* 1688 */         out = null;
/*      */       }
/*      */     }
/*      */     finally
/*      */     {
/* 1693 */       this.m_workCount = 0;
/* 1694 */       this.m_work = new List[257];
/* 1695 */       this.m_newValues = new ArrayList();
/* 1696 */       FileUtils.abort(out);
/* 1697 */       FileUtils.closeObject(out);
/* 1698 */       builder.releaseBuffers();
/*      */     }
/*      */   }
/*      */ 
/*      */   public void finalize()
/*      */   {
/* 1705 */     if (this.m_activeTask == null)
/*      */       return;
/* 1707 */     release();
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1713 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 95352 $";
/*      */   }
/*      */ 
/*      */   class WorkObject
/*      */     implements IdcComparator
/*      */   {
/*      */     public int m_stringIndex;
/*      */     public int m_langIndex;
/*      */     public int m_orderIndex;
/*      */     public String m_value;
/*      */ 
/*      */     public WorkObject(int stringIndex, int langIndex, int orderIndex, String value)
/*      */     {
/*  987 */       this.m_stringIndex = stringIndex;
/*  988 */       this.m_langIndex = langIndex;
/*  989 */       this.m_orderIndex = orderIndex;
/*  990 */       this.m_value = value;
/*      */     }
/*      */ 
/*      */     public int compare(Object o1, Object o2)
/*      */     {
/*  995 */       WorkObject w1 = (WorkObject)o1;
/*  996 */       WorkObject w2 = (WorkObject)o2;
/*  997 */       int v1 = IdcLocalizationStrings.this.m_valLangOffsetData[(2 * (w1.m_stringIndex * (IdcLocalizationStrings.this.m_languageMap.size() + 1) + w1.m_langIndex))];
/*  998 */       int v2 = IdcLocalizationStrings.this.m_valLangOffsetData[(2 * (w2.m_stringIndex * (IdcLocalizationStrings.this.m_languageMap.size() + 1) + w2.m_langIndex))];
/*  999 */       if (v1 == v2)
/*      */       {
/* 1006 */         v1 = w2.m_orderIndex;
/* 1007 */         v2 = w1.m_orderIndex;
/*      */       }
/* 1009 */       return v1 - v2;
/*      */     }
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.IdcLocalizationStrings
 * JD-Core Version:    0.5.4
 */