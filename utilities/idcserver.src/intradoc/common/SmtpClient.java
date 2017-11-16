/*     */ package intradoc.common;
/*     */ 
/*     */ import intradoc.util.IdcMessage;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.io.BufferedOutputStream;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.OutputStream;
/*     */ import java.io.UnsupportedEncodingException;
/*     */ import java.net.Socket;
/*     */ import java.net.UnknownHostException;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Date;
/*     */ import java.util.HashMap;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.Set;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class SmtpClient
/*     */ {
/*  35 */   public String m_hostname = null;
/*  36 */   public int m_smtpPort = 25;
/*  37 */   public String m_sysAdminAddress = null;
/*  38 */   public int m_waitTimeInSeconds = 60;
/*  39 */   public String m_encoding = "iso-8859-1";
/*  40 */   public String m_javaEncoding = "iso-8859-1";
/*     */   public String m_httpServerAddress;
/*  42 */   public boolean m_waitFor220 = true;
/*     */ 
/*  45 */   public boolean m_sendOrgDate = false;
/*  46 */   public String m_orgDateFormat = null;
/*     */ 
/*     */   public void initEnv(Properties environment, String hostname, int port, String sysAddress, int waitTime)
/*     */   {
/*  55 */     this.m_hostname = hostname;
/*  56 */     this.m_smtpPort = port;
/*  57 */     this.m_sysAdminAddress = sysAddress;
/*     */ 
/*  59 */     this.m_waitTimeInSeconds = waitTime;
/*     */ 
/*  62 */     this.m_httpServerAddress = environment.getProperty("HttpServerAddress");
/*  63 */     if (this.m_httpServerAddress != null)
/*     */     {
/*  66 */       int index = this.m_httpServerAddress.indexOf(":");
/*  67 */       if (index >= 0)
/*     */       {
/*  69 */         this.m_httpServerAddress = this.m_httpServerAddress.substring(0, index);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/*  74 */     this.m_waitFor220 = StringUtils.convertToBool(environment.getProperty("SmtpWaitFor220"), true);
/*  75 */     this.m_sendOrgDate = StringUtils.convertToBool(environment.getProperty("SmtpSendOrgDate"), false);
/*  76 */     if (!this.m_sendOrgDate)
/*     */       return;
/*  78 */     this.m_orgDateFormat = environment.getProperty("SmtpDateFormat");
/*     */   }
/*     */ 
/*     */   public void setEncoding(String encoding, String javaEncoding)
/*     */   {
/*  84 */     if (encoding != null)
/*     */     {
/*  86 */       this.m_encoding = encoding;
/*     */     }
/*  88 */     if (javaEncoding == null)
/*     */       return;
/*  90 */     this.m_javaEncoding = javaEncoding;
/*     */   }
/*     */ 
/*     */   public String formatEnvelopeAddress(String addr)
/*     */   {
/*     */     while (true)
/*     */     {
/*  98 */       int index1 = addr.indexOf("<");
/*  99 */       int index2 = addr.lastIndexOf(">");
/* 100 */       if ((index1 < 0) || (index2 <= index1))
/*     */         break;
/* 102 */       addr = addr.substring(index1 + 1, index2);
/*     */     }
/*     */ 
/* 109 */     return "<" + addr + ">";
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public String formatHeaderAddress(String addr)
/*     */     throws UnsupportedEncodingException
/*     */   {
/* 119 */     addr = EncodingUtils.rfc2047Encode(addr, this.m_javaEncoding, this.m_encoding);
/* 120 */     return addr;
/*     */   }
/*     */ 
/*     */   public static List computeMailRecipients(Map emailHeaders)
/*     */   {
/* 138 */     ArrayList allRecipients = new ArrayList();
/* 139 */     List toList = (List)emailHeaders.get("To");
/* 140 */     if (null != toList)
/*     */     {
/* 142 */       allRecipients.addAll(toList);
/*     */     }
/* 144 */     List ccList = (List)emailHeaders.get("Cc");
/* 145 */     if (null != ccList)
/*     */     {
/* 147 */       allRecipients.addAll(ccList);
/*     */     }
/* 149 */     List bccList = (List)emailHeaders.get("Bcc");
/* 150 */     if (null != bccList)
/*     */     {
/* 152 */       allRecipients.addAll(bccList);
/*     */     }
/* 154 */     return allRecipients;
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public void sendMail(String recptStr, String subject, String msg)
/*     */     throws ServiceException
/*     */   {
/* 167 */     SystemUtils.reportDeprecatedUsage("use sendMail(List, Map, String) instead of sendMail(String, String, String)");
/*     */ 
/* 169 */     sendMail(recptStr, subject, msg, this.m_sysAdminAddress);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public void sendMail(String recptStr, String subject, String msg, String mailFromAddress)
/*     */     throws ServiceException
/*     */   {
/* 179 */     SystemUtils.reportDeprecatedUsage("use sendMail(List, Map, String) instead of SendMail(String, String, String, String)");
/*     */ 
/* 181 */     if ((recptStr == null) || (recptStr.length() == 0))
/*     */     {
/* 183 */       return;
/*     */     }
/*     */ 
/* 186 */     Vector recipients = StringUtils.parseArray(recptStr, ',', '^');
/* 187 */     sendMail(recipients, subject, msg, mailFromAddress);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public void sendMail(Vector recpt, String subject, String msg, String mailFromAddress)
/*     */     throws ServiceException
/*     */   {
/* 197 */     SystemUtils.reportDeprecatedUsage("use sendMail(List, Map, String) instead of (Vector, String, String, String)");
/*     */ 
/* 199 */     HashMap headers = new HashMap();
/* 200 */     headers.put("From", mailFromAddress);
/* 201 */     headers.put("To", recpt);
/* 202 */     headers.put("Subject", subject);
/* 203 */     sendMail(recpt, headers, msg);
/*     */   }
/*     */ 
/*     */   public void sendMail(List recipientList, Map headers, String body)
/*     */     throws ServiceException
/*     */   {
/* 237 */     Socket smtpSocket = null;
/* 238 */     OutputStream out = null;
/* 239 */     InputStream in = null;
/*     */ 
/* 241 */     OutputStream inTrace = null;
/* 242 */     OutputStream outTrace = null;
/*     */ 
/* 244 */     String errMsg = null;
/* 245 */     Exception ex = null;
/*     */ 
/* 247 */     if (recipientList.size() < 1)
/*     */     {
/* 249 */       throw new ServiceException("!csNoRecipient");
/*     */     }
/* 251 */     if (null == headers)
/*     */     {
/* 253 */       return;
/*     */     }
/* 255 */     if (null == body)
/*     */     {
/* 257 */       body = "";
/*     */     }
/* 259 */     String fromAddress = (String)headers.remove("From");
/*     */ 
/* 262 */     if ((fromAddress == null) || (fromAddress.length() == 0))
/*     */     {
/* 264 */       fromAddress = this.m_sysAdminAddress;
/*     */     }
/*     */ 
/* 267 */     List toList = (List)headers.remove("To");
/* 268 */     List ccList = (List)headers.remove("Cc");
/* 269 */     headers.remove("Bcc");
/* 270 */     String subject = (String)headers.remove("Subject");
/*     */     try
/*     */     {
/* 275 */       smtpSocket = new Socket(this.m_hostname, this.m_smtpPort);
/* 276 */       out = new BufferedOutputStream(smtpSocket.getOutputStream());
/* 277 */       in = smtpSocket.getInputStream();
/*     */     }
/*     */     catch (UnknownHostException e)
/*     */     {
/* 281 */       errMsg = LocaleUtils.encodeMessage("csSMTPUnknownHost", null, this.m_hostname);
/* 282 */       ex = e;
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 286 */       errMsg = LocaleUtils.encodeMessage("csSMTPConnectionIO", null, this.m_hostname);
/* 287 */       ex = e;
/*     */     }
/*     */ 
/* 290 */     if (errMsg != null)
/*     */     {
/* 292 */       logFailedMail(errMsg, recipientList, subject, fromAddress, ex);
/* 293 */       throw new ServiceException(errMsg, ex);
/*     */     }
/*     */ 
/* 297 */     if ((SystemUtils.m_verbose) && (SystemUtils.isActiveTrace("mail")))
/*     */     {
/*     */       try
/*     */       {
/* 303 */         outTrace = new TracingOutputStream("To   SMTP Server: ", "mail");
/* 304 */         ForkedOutputStream outFork = new ForkedOutputStream(new OutputStream[] { outTrace, out });
/*     */ 
/* 306 */         TeeInputStream inTee = new TeeInputStream(in);
/* 307 */         inTrace = new TracingOutputStream("From SMTP Server: ", "mail");
/* 308 */         inTee.addOutputStream(inTrace);
/* 309 */         out = outFork;
/* 310 */         in = inTee;
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 314 */         Report.trace("mail", "SMTP tracing disabled", e);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 320 */     if ((smtpSocket == null) || (out == null) || (in == null))
/*     */       return;
/* 322 */     IdcStringBuilder builder = new IdcStringBuilder();
/*     */     try
/*     */     {
/* 327 */       if (this.m_waitFor220)
/*     */       {
/* 329 */         writeMsg(out, in, "", "220");
/*     */       }
/*     */ 
/* 332 */       writeMsg(out, in, "HELO " + this.m_httpServerAddress + "\r\n", "250");
/* 333 */       writeMsg(out, in, "MAIL From: " + formatEnvelopeAddress(fromAddress) + "\r\n", "250");
/*     */ 
/* 337 */       writeMultiMsg(out, in, "RCPT TO: ", recipientList, subject, fromAddress, "250", "csSMTPUnableToFindRecipients");
/*     */ 
/* 341 */       writeMsg(out, in, "DATA\r\n", "354");
/*     */ 
/* 344 */       EncodingUtils.rfc2047Encode(builder, "From", fromAddress, this.m_javaEncoding, this.m_encoding).append("\r\n");
/*     */ 
/* 346 */       String fromStr = builder.toStringNoReleaseTruncate();
/* 347 */       writeMsg(out, in, fromStr, null);
/*     */ 
/* 349 */       if ((null != toList) && (toList.size() > 0))
/*     */       {
/* 351 */         String toStr = encodeHeaderList("To", toList) + "\r\n";
/* 352 */         writeMsg(out, in, toStr, null);
/*     */       }
/* 354 */       if ((null != ccList) && (ccList.size() > 0))
/*     */       {
/* 356 */         String ccStr = encodeHeaderList("Cc", ccList) + "\r\n";
/* 357 */         writeMsg(out, in, ccStr, null);
/*     */       }
/* 359 */       EncodingUtils.rfc2047Encode(builder, "Subject", subject, this.m_javaEncoding, this.m_encoding).append("\r\n");
/*     */ 
/* 361 */       String subjectStr = builder.toStringNoReleaseTruncate();
/* 362 */       writeMsg(out, in, subjectStr, null);
/*     */ 
/* 364 */       if (this.m_sendOrgDate)
/*     */       {
/* 366 */         headers.remove("Date");
/* 367 */         String ts = LocaleUtils.formatRfcMailDate(new Date(), this.m_orgDateFormat);
/* 368 */         String DateStr = "Date: " + ts + "\r\n";
/* 369 */         writeMsg(out, in, DateStr, null);
/*     */       }
/*     */ 
/* 372 */       Iterator i = headers.keySet().iterator();
/* 373 */       while (i.hasNext())
/*     */       {
/* 375 */         Object key = i.next();
/* 376 */         if (!key instanceof String) {
/*     */           continue;
/*     */         }
/*     */ 
/* 380 */         Object value = headers.get(key);
/* 381 */         if (!value instanceof String) {
/*     */           continue;
/*     */         }
/*     */ 
/* 385 */         EncodingUtils.rfc2047Encode(builder, (String)key, (String)value, this.m_javaEncoding, this.m_encoding).append("\r\n");
/*     */ 
/* 388 */         String encodedValue = builder.toStringNoReleaseTruncate();
/* 389 */         writeMsg(out, in, encodedValue, null);
/*     */       }
/*     */ 
/* 395 */       writeMsg(out, in, body, null);
/*     */ 
/* 398 */       writeMsg(out, in, "\r\n.\r\n", "250");
/* 399 */       writeMsg(out, in, "QUIT\r\n", "221");
/*     */     }
/*     */     catch (UnknownHostException e)
/*     */     {
/* 403 */       errMsg = "!csSMTPUnknownHost";
/* 404 */       ex = e;
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 408 */       errMsg = "!csSMTPConnectionIO";
/* 409 */       ex = e;
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 414 */       errMsg = "";
/* 415 */       ex = e;
/*     */     }
/*     */     finally
/*     */     {
/* 419 */       builder.releaseBuffers();
/* 420 */       FileUtils.closeObjects(in, out);
/* 421 */       FileUtils.closeObjects(inTrace, outTrace);
/* 422 */       FileUtils.closeObject(smtpSocket);
/*     */     }
/*     */ 
/* 425 */     if (errMsg == null)
/*     */       return;
/* 427 */     logFailedMail(errMsg, recipientList, subject, fromAddress, ex);
/*     */ 
/* 431 */     throw new ServiceException(errMsg, ex);
/*     */   }
/*     */ 
/*     */   public void writeBytes(OutputStream out, String str)
/*     */     throws IOException
/*     */   {
/* 438 */     int length = str.length();
/* 439 */     if (length == 0)
/*     */     {
/* 441 */       return;
/*     */     }
/*     */ 
/* 444 */     byte[] buf = new byte[2 * length];
/* 445 */     boolean wasCR = false;
/* 446 */     int ptr = 0;
/* 447 */     for (int i = 0; i < length; ++i)
/*     */     {
/* 449 */       char c = str.charAt(i);
/* 450 */       buf[ptr] = (byte)(c % 'ÿ');
/* 451 */       if ((buf[ptr] == 10) && (!wasCR))
/*     */       {
/* 454 */         buf[(ptr++)] = 13;
/* 455 */         buf[ptr] = 10;
/*     */       }
/* 457 */       else if ((buf[ptr] != 10) && (wasCR))
/*     */       {
/* 460 */         buf[(ptr++)] = 10;
/* 461 */         buf[ptr] = (byte)(c % 'ÿ');
/*     */       }
/* 463 */       wasCR = buf[ptr] == 13;
/* 464 */       ++ptr;
/*     */     }
/*     */ 
/* 467 */     out.write(buf, 0, ptr);
/* 468 */     out.flush();
/*     */   }
/*     */ 
/*     */   public void writeMsg(OutputStream out, InputStream in, String msg, String okValue)
/*     */     throws Exception
/*     */   {
/* 474 */     writeBytes(out, msg);
/*     */ 
/* 476 */     if (okValue == null)
/*     */     {
/* 478 */       return;
/*     */     }
/*     */ 
/* 481 */     byte[] rBytes = new byte[256];
/*     */ 
/* 483 */     int numRead = 0;
/* 484 */     int count = this.m_waitTimeInSeconds * 10;
/* 485 */     String accumulatedResponseLine = null;
/* 486 */     boolean isSuccess = false;
/* 487 */     boolean isDone = false;
/* 488 */     for (int i = 0; i < count; ++i)
/*     */     {
/* 490 */       while (in.available() > 0)
/*     */       {
/* 492 */         numRead = in.read(rBytes);
/* 493 */         String responseLine = new String(rBytes, 0, numRead - 1);
/* 494 */         if (responseLine.indexOf(okValue) >= 0)
/*     */         {
/* 496 */           isSuccess = true;
/* 497 */           isDone = true;
/* 498 */           break;
/*     */         }
/* 500 */         if (responseLine.length() > 5)
/*     */         {
/* 502 */           if ((accumulatedResponseLine == null) || (accumulatedResponseLine.length() > 255))
/*     */           {
/* 504 */             accumulatedResponseLine = responseLine;
/*     */           }
/*     */           else
/*     */           {
/* 508 */             accumulatedResponseLine = accumulatedResponseLine + responseLine;
/*     */           }
/* 510 */           if (responseLine.charAt(0) == '5')
/*     */           {
/* 513 */             isDone = true;
/* 514 */             break;
/*     */           }
/*     */         }
/*     */       }
/* 518 */       if (isDone) {
/*     */         break;
/*     */       }
/*     */ 
/* 522 */       SystemUtils.sleep(100L);
/*     */     }
/* 524 */     if (isSuccess)
/*     */       return;
/* 526 */     String errMsg = "!csSMTPNoResponse";
/* 527 */     if (accumulatedResponseLine != null)
/*     */     {
/* 529 */       errMsg = LocaleUtils.encodeMessage("csSMTPResponseIndicatesFailure", null, accumulatedResponseLine);
/*     */     }
/* 531 */     throw new Exception(errMsg);
/*     */   }
/*     */ 
/*     */   public int writeMultiMsg(OutputStream out, InputStream in, String prefix, List multi, String subject, String from, String okValue, String errMsg)
/*     */     throws Exception
/*     */   {
/* 538 */     int numSucceeded = 0;
/* 539 */     int numMsg = multi.size();
/* 540 */     Vector failedMsgs = new IdcVector();
/* 541 */     for (int i = 0; i < numMsg; ++i)
/*     */     {
/* 543 */       String tempStr = null;
/* 544 */       String multiElt = (String)multi.get(i);
/*     */       try
/*     */       {
/* 547 */         if (prefix.startsWith("RCPT TO:"))
/*     */         {
/* 549 */           tempStr = formatEnvelopeAddress(multiElt);
/*     */         }
/*     */         else
/*     */         {
/* 553 */           tempStr = multiElt;
/*     */         }
/* 555 */         writeMsg(out, in, prefix + tempStr + "\r\n", okValue);
/* 556 */         ++numSucceeded;
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 560 */         String overallErrMsg = null;
/* 561 */         if ((i == numMsg - 1) && (numSucceeded == 0))
/*     */         {
/* 565 */           overallErrMsg = LocaleUtils.encodeMessage(errMsg, null, "" + numMsg);
/*     */         }
/* 567 */         String exceptionMsg = LocaleUtils.encodeMessage("csSMTPInvalidMessage", overallErrMsg, multiElt);
/* 568 */         if (overallErrMsg != null)
/*     */         {
/* 570 */           Exception newException = new Exception(exceptionMsg);
/* 571 */           SystemUtils.setExceptionCause(newException, e);
/* 572 */           throw newException;
/*     */         }
/*     */ 
/* 575 */         Report.error(null, exceptionMsg, e);
/* 576 */         Object[] failedMsgObjs = { tempStr, e };
/* 577 */         failedMsgs.addElement(failedMsgObjs);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 583 */     for (int i = 0; i < failedMsgs.size(); ++i)
/*     */     {
/* 585 */       Object[] failedMsgObjs = (Object[])(Object[])failedMsgs.elementAt(i);
/* 586 */       String tempStr = (String)failedMsgObjs[0];
/* 587 */       Exception e = (Exception)failedMsgObjs[1];
/* 588 */       IdcMessage idcMsg = IdcMessageFactory.lc(e);
/*     */ 
/* 591 */       logFailedMailEx(LocaleResources.localizeMessage(null, idcMsg, null).toString(), tempStr, subject, from, e);
/*     */     }
/*     */ 
/* 595 */     return numSucceeded;
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public String buildRecipientString(List list)
/*     */     throws UnsupportedEncodingException
/*     */   {
/* 604 */     IdcStringBuilder buf = new IdcStringBuilder();
/* 605 */     int size = list.size();
/* 606 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 608 */       String email = (String)list.get(i);
/* 609 */       if (i > 0)
/*     */       {
/* 611 */         buf.append(",\r\n ");
/*     */       }
/* 613 */       buf.append(email);
/*     */     }
/*     */ 
/* 616 */     String recipients = EncodingUtils.rfc2047Encode(buf.toString(), this.m_javaEncoding, this.m_encoding);
/* 617 */     return recipients;
/*     */   }
/*     */ 
/*     */   public String encodeHeaderList(String header, List list) throws UnsupportedEncodingException
/*     */   {
/* 622 */     IdcStringBuilder buf = new IdcStringBuilder();
/* 623 */     int size = list.size();
/* 624 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 626 */       String email = (String)list.get(i);
/* 627 */       if (i > 0)
/*     */       {
/* 629 */         buf.append(",");
/*     */       }
/* 631 */       buf.append(email);
/*     */     }
/*     */ 
/* 634 */     String recipients = EncodingUtils.rfc2047Encode(null, header, buf.toString(), this.m_javaEncoding, this.m_encoding).toString();
/*     */ 
/* 636 */     return recipients;
/*     */   }
/*     */ 
/*     */   protected void logFailedMail(String errMsg, List recpt, String subject, String mailFromAddress, Exception e)
/*     */   {
/* 642 */     String str = StringUtils.createString(recpt, ',', '^');
/* 643 */     logFailedMailEx(errMsg, str, subject, mailFromAddress, e);
/*     */   }
/*     */ 
/*     */   protected void logFailedMailEx(String errMsg, String recpt, String subject, String mailFromAddress, Exception e)
/*     */   {
/* 650 */     if (!SystemUtils.m_verbose)
/*     */       return;
/* 652 */     String fullErrMsg = "Email send failed. " + errMsg;
/* 653 */     fullErrMsg = fullErrMsg + "\nFrom:    " + mailFromAddress;
/* 654 */     fullErrMsg = fullErrMsg + "\nTo:      " + recpt;
/* 655 */     fullErrMsg = fullErrMsg + "\nSubject: " + subject;
/* 656 */     Report.debug("mail", fullErrMsg, e);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 662 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 82533 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.SmtpClient
 * JD-Core Version:    0.5.4
 */