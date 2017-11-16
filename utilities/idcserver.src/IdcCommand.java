/*     */ import intradoc.apputilities.idccommand.FileReaderStack;
/*     */ import intradoc.apputilities.idccommand.IdcExecuteServer;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.LoggingUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.server.IdcSystemConfig;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcMessage;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.io.BufferedReader;
/*     */ import java.io.File;
/*     */ import java.io.FileWriter;
/*     */ import java.io.IOException;
/*     */ import java.io.PrintStream;
/*     */ import java.io.StringReader;
/*     */ import java.util.Date;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class IdcCommand
/*     */ {
/*  57 */   int m_port = 4444;
/*  58 */   Properties m_props = null;
/*     */ 
/*  60 */   FileWriter m_logFile = null;
/*  61 */   IdcExecuteServer m_idcServer = null;
/*     */ 
/*  63 */   final String[][] COMMAND_LINE_KEYS = { { "IdcCommandFile", "-f" }, { "IdcCommandLog", "-l" }, { "IdcCommandUserName", "-u" }, { "ConnectionMode", "-c" }, { "IdcResponseDump", "-d" } };
/*     */ 
/*     */   public IdcCommand()
/*     */   {
/*  74 */     this.m_props = new Properties();
/*     */ 
/*  76 */     this.m_idcServer = new IdcExecuteServer();
/*  77 */     this.m_idcServer.setIsSerializeResult(false);
/*     */   }
/*     */ 
/*     */   public static void main(String[] args)
/*     */   {
/*  83 */     IdcCommand idcCommand = new IdcCommand();
/*  84 */     idcCommand.go(args);
/*     */ 
/*  86 */     System.exit(0);
/*     */   }
/*     */ 
/*     */   public void go(String[] args)
/*     */   {
/*  91 */     boolean isInitialized = false;
/*     */     try
/*     */     {
/*  95 */       LoggingUtils.setLogFileMsgPrefix("IdcCommand");
/*     */ 
/*  97 */       isInitialized = init(args);
/*     */ 
/* 105 */       Vector v = loadFile(this.m_props.getProperty("IdcCommandFile"));
/* 106 */       if (v.size() == 0)
/*     */       {
/* 108 */         throw new ServiceException("!csIDCCommandNoValidCommandsInFile");
/*     */       }
/*     */ 
/* 111 */       executeCommands(v);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 115 */       log(e);
/* 116 */       if (isInitialized)
/*     */         return;
/* 118 */       log("!csIDCCommandUsage");
/*     */     }
/*     */   }
/*     */ 
/*     */   protected boolean init(String[] args)
/*     */     throws ServiceException
/*     */   {
/*     */     try
/*     */     {
/* 127 */       IdcSystemConfig.loadInitialConfig();
/* 128 */       IdcSystemConfig.loadAppConfigInfo();
/* 129 */       IdcSystemConfig.initLocalization(IdcSystemConfig.F_STANDARD_SERVER);
/* 130 */       IdcSystemConfig.configLocalization();
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 134 */       System.err.println(LocaleResources.localizeMessage(LocaleUtils.encodeMessage("csIDCCommandInitError", e.getMessage()), null));
/*     */     }
/*     */ 
/* 142 */     for (int i = 0; i < this.COMMAND_LINE_KEYS.length; ++i)
/*     */     {
/* 144 */       String key = this.COMMAND_LINE_KEYS[i][0];
/* 145 */       String value = SharedObjects.getEnvironmentValue(key);
/* 146 */       if (value == null)
/*     */         continue;
/* 148 */       this.m_props.put(key, value);
/*     */     }
/*     */ 
/* 164 */     for (int i = 0; i < args.length; ++i)
/*     */     {
/* 166 */       String arg = args[i];
/* 167 */       int index = findCommandLineKey(arg);
/* 168 */       if (index < 0)
/*     */       {
/* 171 */         throw new ServiceException(LocaleUtils.encodeMessage("csIDCCommandInvalidArgument", null, arg));
/*     */       }
/* 173 */       String key = this.COMMAND_LINE_KEYS[index][0];
/* 174 */       ++i;
/* 175 */       readCommandLine(key, args, i);
/*     */     }
/*     */ 
/* 183 */     String errMsg = null;
/* 184 */     String fileName = this.m_props.getProperty("IdcCommandFile");
/* 185 */     if ((fileName == null) || (fileName.length() == 0))
/*     */     {
/* 187 */       errMsg = "!csIDCCommandNoCommandFile";
/*     */     }
/*     */     else
/*     */     {
/* 191 */       File file = new File(fileName);
/* 192 */       if (!file.exists())
/*     */       {
/* 194 */         errMsg = LocaleUtils.encodeMessage("csIDCCommandCommandFileInvalid", null, fileName);
/*     */       }
/*     */     }
/*     */ 
/* 198 */     if (errMsg == null)
/*     */     {
/* 200 */       String userName = this.m_props.getProperty("IdcCommandUserName");
/* 201 */       if ((userName == null) || (userName.length() == 0))
/*     */       {
/* 203 */         errMsg = "!csIDCCommandNoUsername";
/*     */       }
/*     */     }
/*     */ 
/* 207 */     if (errMsg != null)
/*     */     {
/* 209 */       throw new ServiceException(errMsg);
/*     */     }
/*     */ 
/* 212 */     String logFileName = this.m_props.getProperty("IdcCommandLog");
/*     */     try
/*     */     {
/* 215 */       if ((logFileName != null) && (logFileName.trim().length() > 0))
/*     */       {
/* 217 */         this.m_logFile = new FileWriter(logFileName);
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 222 */       throw new ServiceException(LocaleUtils.encodeMessage("csIDCCommandUnableToCreateLog", null, logFileName));
/*     */     }
/*     */ 
/* 227 */     this.m_idcServer.setConnectionMode(this.m_props.getProperty("ConnectionMode"));
/*     */ 
/* 229 */     return true;
/*     */   }
/*     */ 
/*     */   protected void readCommandLine(String key, String[] args, int index)
/*     */     throws ServiceException
/*     */   {
/* 235 */     if (args.length <= index)
/*     */     {
/* 238 */       throw new ServiceException("!csIDCCommandLineError");
/*     */     }
/*     */ 
/* 241 */     String value = args[index];
/* 242 */     if (findCommandLineKey(value) >= 0)
/*     */     {
/* 245 */       throw new ServiceException(LocaleUtils.encodeMessage("csIDCCommandLineKeyError", null, key));
/*     */     }
/* 247 */     this.m_props.put(key, value);
/*     */   }
/*     */ 
/*     */   protected int findCommandLineKey(String key)
/*     */   {
/* 252 */     int result = -1;
/* 253 */     for (int i = 0; i < this.COMMAND_LINE_KEYS.length; ++i)
/*     */     {
/* 255 */       String id = this.COMMAND_LINE_KEYS[i][1];
/* 256 */       if (!key.equals(id))
/*     */         continue;
/* 258 */       result = i;
/* 259 */       break;
/*     */     }
/*     */ 
/* 262 */     return result;
/*     */   }
/*     */ 
/*     */   public Vector loadFile(String filename)
/*     */     throws ServiceException
/*     */   {
/* 271 */     Vector v = new IdcVector();
/* 272 */     FileReaderStack reader = new FileReaderStack();
/* 273 */     int count = 0;
/*     */     try
/*     */     {
/* 277 */       reader.setTrimLines(true);
/* 278 */       reader.setIgnoreEmptyLines(true);
/* 279 */       reader.push(filename);
/*     */ 
/* 282 */       String line = reader.readLine();
/* 283 */       while (line != null)
/*     */       {
/* 287 */         while ((line != null) && (isLineIgnored(line)))
/*     */         {
/* 290 */           line = reader.readLine();
/*     */         }
/* 292 */         if (line == null)
/*     */         {
/*     */           break;
/*     */         }
/*     */ 
/* 298 */         DataBinder binder = new DataBinder(true);
/* 299 */         Properties props = new Properties();
/* 300 */         StringBuffer buffer = new StringBuffer();
/* 301 */         ++count;
/*     */ 
/* 304 */         while ((line != null) && (!line.equals("<<EOD>>")))
/*     */         {
/* 308 */           if ((line.startsWith("@Properties")) || (line.startsWith("@ResultSet")) || (line.startsWith("@OptionList")))
/*     */           {
/* 312 */             if ((line.startsWith("@ResultSet")) || (line.startsWith("@OptionList")))
/*     */             {
/* 315 */               reader.setIgnoreEmptyLines(false);
/*     */             }do {
/* 317 */               if (line.equals("@end"))
/*     */                 break label254;
/* 319 */               buffer.append(line + "\n");
/* 320 */               line = reader.readLine();
/* 321 */               if (line != null)
/*     */                 continue;
/* 323 */               throw new Exception("!csIDCCommandUnexpectedEOF");
/*     */             }
/* 325 */             while (!line.equals("<<EOD>>"));
/*     */ 
/* 327 */             throw new Exception("!csIDCCommandUnexpectedEOD");
/*     */ 
/* 330 */             if (line.equals("@end"))
/*     */             {
/* 332 */               label254: buffer.append(line + "\n");
/* 333 */               reader.setIgnoreEmptyLines(true);
/*     */             }
/*     */           }
/* 336 */           else if (line.startsWith("@Include"))
/*     */           {
/* 339 */             if (line.length() >= 9)
/*     */             {
/* 341 */               reader.push(line.substring(9).trim());
/*     */             }
/*     */           }
/* 344 */           else if (!isLineIgnored(line))
/*     */           {
/* 348 */             int index = line.indexOf(61);
/* 349 */             String name = null;
/* 350 */             String value = null;
/* 351 */             if (index < 0)
/*     */             {
/* 353 */               name = line;
/*     */             }
/*     */             else
/*     */             {
/* 357 */               name = line.substring(0, index).trim();
/*     */             }
/*     */ 
/* 360 */             if (index == line.length() - 1)
/*     */             {
/* 362 */               value = "";
/*     */             }
/*     */             else
/*     */             {
/* 366 */               value = line.substring(index + 1).trim();
/* 367 */               value = StringUtils.decodeLiteralStringEscapeSequence(value);
/*     */             }
/* 369 */             props.put(name, value);
/*     */           }
/*     */ 
/* 373 */           line = reader.readLine();
/*     */         }
/*     */ 
/* 378 */         if (buffer.length() != 0)
/*     */         {
/* 381 */           binder.receive(new BufferedReader(new StringReader(buffer.toString())));
/*     */         }
/*     */ 
/* 385 */         for (Enumeration e = props.keys(); e.hasMoreElements(); )
/*     */         {
/* 387 */           String name = (String)e.nextElement();
/* 388 */           String value = (String)props.get(name);
/* 389 */           binder.putLocal(name, value);
/*     */         }
/*     */ 
/* 393 */         v.addElement(binder);
/*     */ 
/* 396 */         line = reader.readLine();
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 401 */       String fName = reader.getCurrentFileName();
/* 402 */       String errMsg = null;
/*     */ 
/* 404 */       if (fName != null)
/*     */       {
/* 406 */         errMsg = LocaleUtils.encodeMessage("csIDCCommandUnableToParseFile", null, fName, "" + count);
/*     */       }
/*     */       else
/*     */       {
/* 410 */         errMsg = LocaleUtils.encodeMessage("csIDCCommandUnableToParse", null, fName);
/*     */       }
/*     */ 
/* 413 */       throw new ServiceException(errMsg, e);
/*     */     }
/* 415 */     return v;
/*     */   }
/*     */ 
/*     */   protected boolean isLineIgnored(String line)
/*     */   {
/* 424 */     return (line.startsWith("#") == true) || (line.equals("HTTP/1.0 200 OK") == true) || (line.equals("Content-type: text/html") == true);
/*     */   }
/*     */ 
/*     */   public void executeCommands(Vector v)
/*     */   {
/* 431 */     String user = this.m_props.getProperty("IdcCommandUserName");
/* 432 */     this.m_idcServer.init(user, null);
/*     */ 
/* 435 */     for (int i = 0; i < v.size(); ++i)
/*     */     {
/* 437 */       DataBinder binder = (DataBinder)v.elementAt(i);
/* 438 */       this.m_idcServer.executeCommand(binder);
/* 439 */       log(binder);
/*     */ 
/* 442 */       v.setElementAt(null, i);
/* 443 */       binder = null;
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void log(DataBinder binder)
/*     */   {
/* 453 */     String cmd = binder.getLocal("IdcService");
/* 454 */     if (cmd == null)
/*     */     {
/* 456 */       cmd = LocaleResources.getString("csIDCCommandLabelUndefined", null);
/*     */     }
/*     */ 
/* 459 */     String smsg = binder.getLocal("StatusMessage");
/* 460 */     if (smsg == null)
/*     */     {
/* 462 */       smsg = LocaleResources.getString("csIDCCommandLabelUnknown", null);
/*     */     }
/* 464 */     else if (StringUtils.convertToBool(binder.getLocal("IsStandAlone"), true))
/*     */     {
/* 467 */       smsg = LocaleResources.localizeMessage(smsg, null);
/*     */     }
/*     */ 
/* 470 */     int code = 0;
/*     */     try
/*     */     {
/* 473 */       String scode = binder.getLocal("StatusCode");
/* 474 */       code = Integer.parseInt(scode);
/*     */     }
/*     */     catch (Throwable ignore)
/*     */     {
/* 478 */       if (SystemUtils.m_verbose)
/*     */       {
/* 480 */         Report.debug("systemparse", null, ignore);
/*     */       }
/*     */     }
/*     */ 
/* 484 */     String msg = null;
/* 485 */     if (code < 0)
/*     */     {
/* 487 */       msg = LocaleUtils.encodeMessage("csIDCCommandErrorExecSvc", null, cmd, smsg);
/*     */     }
/*     */     else
/*     */     {
/* 491 */       msg = LocaleUtils.encodeMessage("csIDCCommandSuccessExecSvc", null, cmd);
/*     */     }
/*     */ 
/* 494 */     log(msg);
/* 495 */     String responseDump = this.m_props.getProperty("IdcResponseDump");
/*     */ 
/* 497 */     if ((responseDump == null) || (responseDump.trim().length() <= 0) || (!responseDump.equalsIgnoreCase("true")))
/*     */       return;
/* 499 */     log(binder.toString());
/*     */   }
/*     */ 
/*     */   protected void log(Exception e)
/*     */   {
/* 507 */     log(IdcMessageFactory.lc(e));
/*     */   }
/*     */ 
/*     */   protected void log(IdcMessage msg)
/*     */   {
/* 513 */     Date dte = new Date();
/* 514 */     msg = IdcMessageFactory.lc(msg, "csIDCCommandLogFormat", new Object[] { LocaleResources.localizeDate(dte, null) });
/*     */     try
/*     */     {
/* 518 */       if (this.m_logFile != null)
/*     */       {
/* 520 */         this.m_logFile.write(LocaleResources.localizeMessage(msg + "!csLinefeed", null));
/* 521 */         this.m_logFile.flush();
/*     */       }
/*     */     }
/*     */     catch (IOException text)
/*     */     {
/*     */       String text;
/* 526 */       if (SystemUtils.m_verbose)
/*     */       {
/* 528 */         Report.debug("system", null, ignore);
/*     */       }
/*     */     }
/*     */     finally
/*     */     {
/*     */       String text;
/* 533 */       String text = LocaleResources.localizeMessage(null, msg, null).toString();
/* 534 */       System.err.println(text);
/*     */     }
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   protected void log(String msg)
/*     */   {
/* 542 */     IdcMessage idcmsg = IdcMessageFactory.lc();
/* 543 */     idcmsg.m_msgEncoded = msg;
/* 544 */     log(idcmsg);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 549 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 84193 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     IdcCommand
 * JD-Core Version:    0.5.4
 */