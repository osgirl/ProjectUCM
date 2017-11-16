/*     */ package intradoc.fdf;
/*     */ 
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.io.IOException;
/*     */ import java.io.Reader;
/*     */ import java.io.Writer;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class Fdf
/*     */ {
/*     */   public static final int FIELD_READONLY = 1;
/*     */   public static final int FIELD_HIDDEN = 2;
/*     */   public static final int FIELD_NOEXPORT = 4;
/*  46 */   public static final String[] SPECIAL_TOKENS = { "<<", ">>", "[", "]", "{", "}" };
/*     */ 
/*  49 */   public static String sep = "\n";
/*     */ 
/*     */   public static void createFdf(Writer w, String pdfFile, Properties props)
/*     */     throws IOException
/*     */   {
/*  61 */     w.write("%FDF-1.2\n1 0 obj <<\n/FDF <<\n");
/*  62 */     if (pdfFile != null)
/*     */     {
/*  64 */       w.write("/F " + postscriptString(pdfFile) + "\n");
/*     */     }
/*     */ 
/*  67 */     Hashtable rootFields = new Hashtable();
/*  68 */     Enumeration en = props.keys();
/*  69 */     while (en.hasMoreElements())
/*     */     {
/*  71 */       Object key = en.nextElement();
/*  72 */       Object val = props.get(key);
/*  73 */       if ((key instanceof String) && (val instanceof String))
/*     */       {
/*  75 */         String strKey = (String)key;
/*  76 */         Vector ancestry = StringUtils.parseArray(strKey, '.', '.');
/*     */ 
/*  79 */         int length = ancestry.size();
/*  80 */         Object[] child = { null, rootFields };
/*  81 */         Object[] parent = null;
/*     */ 
/*  83 */         if (length == 0)
/*     */         {
/*     */           continue;
/*     */         }
/*     */ 
/*  88 */         for (int i = 0; (i < length) && (child != null); ++i)
/*     */         {
/*  90 */           parent = child;
/*  91 */           Hashtable kids = (Hashtable)parent[1];
/*  92 */           if (kids == null)
/*     */           {
/*  94 */             kids = new Hashtable();
/*     */           }
/*  96 */           child = (Object[])(Object[])kids.get(ancestry.elementAt(i));
/*     */         }
/*     */ 
/*  99 */         if (child == null)
/*     */         {
/* 101 */           --i;
/*     */         }
/*     */ 
/* 104 */         while (i < length)
/*     */         {
/* 107 */           child = new Object[2];
/* 108 */           Hashtable kids = (Hashtable)parent[1];
/* 109 */           if (kids == null)
/*     */           {
/*     */              tmp261_258 = new Hashtable(); kids = tmp261_258; parent[1] = tmp261_258;
/*     */           }
/* 113 */           kids.put(ancestry.elementAt(i), child);
/* 114 */           parent = child;
/* 115 */           ++i;
/*     */         }
/* 117 */         child[0] = val;
/*     */       }
/*     */     }
/*     */ 
/* 121 */     w.write("/Fields\n");
/*     */ 
/* 123 */     writeFieldVector(w, "", rootFields);
/*     */ 
/* 125 */     w.write(">>\n>>\nendobj\n");
/* 126 */     w.write("trailer << /Root 1 0 R >>\n%EOF");
/*     */   }
/*     */ 
/*     */   protected static void writeFieldVector(Writer w, String prefix, Hashtable subFields)
/*     */     throws IOException
/*     */   {
/* 132 */     w.write(prefix + "[\n");
/* 133 */     Enumeration en = subFields.keys();
/* 134 */     while (en.hasMoreElements())
/*     */     {
/* 136 */       String key = (String)en.nextElement();
/* 137 */       Object[] val = (Object[])(Object[])subFields.get(key);
/* 138 */       w.write(prefix + "<< /T " + postscriptString(key));
/* 139 */       if (val[0] != null)
/*     */       {
/* 141 */         w.write(prefix + " /V " + postscriptString((String)val[0]) + " ");
/*     */       }
/* 143 */       if (val[1] != null)
/*     */       {
/* 145 */         w.write(prefix + "/Kids \n");
/* 146 */         writeFieldVector(w, prefix + "  ", (Hashtable)val[1]);
/*     */       }
/* 148 */       w.write(prefix + " >>\n");
/*     */     }
/* 150 */     w.write(prefix + "]\n");
/*     */   }
/*     */ 
/*     */   public static void write(Writer w, Vector v)
/*     */     throws IOException
/*     */   {
/* 156 */     int length = v.size();
/* 157 */     for (int i = 0; i < length; ++i)
/*     */     {
/* 159 */       writeObject(w, v.elementAt(i));
/* 160 */       w.write(sep);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void writeVector(Writer w, Vector v)
/*     */     throws IOException
/*     */   {
/* 167 */     w.write("[");
/* 168 */     int length = v.size();
/* 169 */     for (int i = 0; i < length; ++i)
/*     */     {
/* 171 */       writeObject(w, v.elementAt(i));
/*     */     }
/* 173 */     w.write("]\n");
/*     */   }
/*     */ 
/*     */   public static void writeDictionary(Writer w, Properties props)
/*     */     throws IOException
/*     */   {
/* 179 */     w.write("<<" + sep);
/* 180 */     Enumeration en = props.keys();
/* 181 */     while (en.hasMoreElements())
/*     */     {
/* 183 */       String key = (String)en.nextElement();
/* 184 */       Object value = props.get(key);
/* 185 */       w.write(key + " ");
/* 186 */       if (value instanceof String)
/*     */       {
/* 188 */         String s = (String)value;
/* 189 */         int length = s.length();
/* 190 */         if ((s.startsWith("(")) && (s.endsWith(")")))
/*     */         {
/* 192 */           s = s.substring(1, length - 1);
/* 193 */           value = postscriptString(s);
/*     */         }
/*     */       }
/* 196 */       writeObject(w, value);
/* 197 */       w.write(sep);
/*     */     }
/* 199 */     w.write(">>" + sep);
/*     */   }
/*     */ 
/*     */   public static void writeObject(Writer w, Object obj)
/*     */     throws IOException
/*     */   {
/* 205 */     if (obj instanceof String)
/*     */     {
/* 207 */       w.write((String)obj + " ");
/*     */     }
/* 209 */     else if (obj instanceof Properties)
/*     */     {
/* 211 */       writeDictionary(w, (Properties)obj);
/*     */     } else {
/* 213 */       if (!obj instanceof Vector)
/*     */         return;
/* 215 */       writeVector(w, (Vector)obj);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static String postscriptString(String s)
/*     */   {
/* 229 */     if (s == null)
/*     */     {
/* 231 */       return s;
/*     */     }
/*     */ 
/* 234 */     StringBuffer rc = new StringBuffer("(");
/*     */ 
/* 236 */     int l = s.length();
/* 237 */     for (int i = 0; i < l; ++i)
/*     */     {
/*     */       char c;
/* 240 */       switch (c = s.charAt(i))
/*     */       {
/*     */       case '\\':
/* 243 */         rc.append(c);
/*     */       default:
/* 245 */         rc.append(c);
/* 246 */         break;
/*     */       case '(':
/* 248 */         rc.append("\\(");
/* 249 */         break;
/*     */       case ')':
/* 251 */         rc.append("\\)");
/*     */       }
/*     */     }
/*     */ 
/* 255 */     rc.append(')');
/* 256 */     return rc.toString();
/*     */   }
/*     */ 
/*     */   public static Properties readFdf(Reader reader)
/*     */     throws DataException, FdfParseException, IOException
/*     */   {
/*     */     do
/* 276 */       element = readElement(reader);
/* 277 */     while (!element.equals("/FDF"));
/*     */ 
/* 283 */     String element = readElement(reader);
/* 284 */     if (!element.equals("<<"))
/*     */     {
/* 286 */       throw new FdfParseException(null, new Object[] { "csFDFNotADictionary" });
/*     */     }
/*     */ 
/* 289 */     Properties props = readDictionary(reader);
/*     */ 
/* 291 */     return props;
/*     */   }
/*     */ 
/*     */   public static Vector read(Reader r)
/*     */     throws FdfParseException, IOException
/*     */   {
/* 299 */     Vector v = new IdcVector();
/*     */ 
/* 301 */     while ((obj = readObject(r)) != null)
/*     */     {
/*     */       Object obj;
/* 303 */       v.addElement(obj);
/*     */     }
/* 305 */     return v;
/*     */   }
/*     */ 
/*     */   public static String readElement(Reader r)
/*     */     throws FdfParseException, IOException
/*     */   {
/* 317 */     StringBuffer element = new StringBuffer();
/*     */ 
/* 319 */     int type = 0;
/* 320 */     boolean done = false;
/* 321 */     boolean stringEscape = false;
/* 322 */     int stringOctalCount = 0;
/* 323 */     int stringOctalValue = 0;
/*     */ 
/* 325 */     while ((!done) && ((tmp = r.read()) != -1))
/*     */     {
/*     */       int tmp;
/* 327 */       char c = (char)tmp;
/* 328 */       if ((type == 0) && 
/* 330 */         (!Character.isWhitespace(c)))
/*     */       {
/* 332 */         switch (c)
/*     */         {
/*     */         default:
/* 335 */           type = 1;
/* 336 */           break;
/*     */         case '(':
/* 338 */           type = 2;
/* 339 */           break;
/*     */         case '%':
/* 341 */           type = 3;
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 347 */       switch (type)
/*     */       {
/*     */       case 1:
/* 350 */         if (Character.isWhitespace(c))
/*     */         {
/* 352 */           done = true;
/*     */         }
/*     */         else
/*     */         {
/* 356 */           element.append(c);
/* 357 */           for (int i = 0; i < SPECIAL_TOKENS.length; ++i)
/*     */           {
/* 359 */             if (!element.toString().equals(SPECIAL_TOKENS[i]))
/*     */               continue;
/* 361 */             done = true;
/* 362 */             break;
/*     */           }
/*     */         }
/*     */ 
/* 366 */         break;
/*     */       case 2:
/* 368 */         if (stringEscape)
/*     */         {
/* 370 */           switch (c)
/*     */           {
/*     */           case 'n':
/* 373 */             c = '\n';
/* 374 */             break;
/*     */           case 'r':
/* 376 */             c = '\r';
/* 377 */             break;
/*     */           case 't':
/* 379 */             c = '\t';
/* 380 */             break;
/*     */           case 'b':
/* 382 */             c = '\b';
/* 383 */             break;
/*     */           case 'f':
/* 385 */             c = '\f';
/* 386 */             break;
/*     */           case '\n':
/* 388 */             break;
/*     */           case '(':
/*     */           case ')':
/*     */           case '\\':
/* 392 */             break;
/*     */           default:
/* 394 */             stringOctalCount = 1;
/* 395 */             stringOctalValue = c - '0';
/*     */           }
/*     */ 
/* 398 */           stringEscape = false;
/* 399 */           element.append(c);
/*     */         }
/* 401 */         else if (stringOctalCount > 0)
/*     */         {
/* 403 */           ++stringOctalCount;
/* 404 */           stringOctalValue = 8 * stringOctalValue + c - 48;
/* 405 */           if (stringOctalCount == 3)
/*     */           {
/* 407 */             element.append((char)stringOctalValue);
/* 408 */             stringOctalCount = 0;
/*     */           }
/*     */         }
/*     */         else
/*     */         {
/* 413 */           switch (c)
/*     */           {
/*     */           case '\\':
/* 416 */             stringEscape = true;
/* 417 */             break;
/*     */           case ')':
/* 419 */             done = true;
/*     */           default:
/* 421 */             element.append(c);
/*     */           }
/* 422 */         }break;
/*     */       case 3:
/* 427 */         if (c == '\n')
/*     */         {
/* 429 */           done = true;
/*     */         }
/*     */         else
/*     */         {
/* 433 */           element.append(c);
/*     */         }
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 439 */     return (type == 0) ? null : element.toString();
/*     */   }
/*     */ 
/*     */   public static Vector readArray(Reader r, String end)
/*     */     throws FdfParseException, IOException
/*     */   {
/* 445 */     Vector v = new IdcVector();
/* 446 */     boolean done = false;
/* 447 */     while (!done)
/*     */     {
/* 449 */       Object value = readObject(r);
/* 450 */       if (value != null)
/*     */       {
/* 452 */         if (value instanceof String)
/*     */         {
/* 454 */           String str = (String)value;
/* 455 */           if (str.endsWith(end))
/*     */           {
/* 457 */             done = true;
/* 458 */             if (str.length() <= 1)
/*     */               break;
/* 460 */             value = str.substring(0, str.length() - end.length());
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/* 469 */         v.addElement(value);
/*     */       }
/*     */       else
/*     */       {
/* 473 */         done = true;
/*     */       }
/*     */     }
/*     */ 
/* 477 */     return v;
/*     */   }
/*     */ 
/*     */   public static Properties readDictionary(Reader r)
/*     */     throws FdfParseException, IOException
/*     */   {
/* 483 */     Properties props = new Properties();
/*     */ 
/* 485 */     boolean done = false;
/* 486 */     String lastKey = null;
/* 487 */     Object lastValue = null;
/* 488 */     boolean valIsReference = false;
/* 489 */     while (!done)
/*     */     {
/* 491 */       String key = readElement(r);
/* 492 */       if (key.equals(">>"))
/*     */       {
/* 494 */         done = true;
/* 495 */         break;
/*     */       }
/* 497 */       if (!key.startsWith("/"))
/*     */       {
/* 502 */         valIsReference = true;
/*     */       }
/*     */ 
/* 505 */       Object value = readObject(r);
/* 506 */       if (valIsReference)
/*     */       {
/*     */         try
/*     */         {
/* 510 */           value = (String)lastValue + " " + key + " " + (String)value;
/* 511 */           key = lastKey;
/*     */         }
/*     */         catch (Exception e)
/*     */         {
/* 515 */           throw new FdfParseException(null, new Object[] { "csFDFIllegalKey", lastKey });
/*     */         }
/* 517 */         valIsReference = false;
/*     */       }
/* 519 */       else if ((value instanceof String) && (((String)value).endsWith(">>")))
/*     */       {
/* 522 */         String v = (String)value;
/* 523 */         value = v.substring(0, v.length() - 2);
/* 524 */         done = true;
/*     */       }
/*     */ 
/* 527 */       lastKey = key;
/* 528 */       lastValue = value;
/* 529 */       props.put(key, value);
/*     */     }
/*     */ 
/* 532 */     return props;
/*     */   }
/*     */ 
/*     */   public static Object readObject(Reader r)
/*     */     throws FdfParseException, IOException
/*     */   {
/* 538 */     Object obj = null;
/*     */ 
/* 542 */     String element = readElement(r);
/*     */ 
/* 544 */     if ((element == null) || (element.length() == 0))
/*     */     {
/* 546 */       return obj;
/*     */     }
/*     */ 
/* 549 */     for (int type = 0; type < SPECIAL_TOKENS.length; ++type)
/*     */     {
/* 551 */       if (element.equals(SPECIAL_TOKENS[type])) {
/*     */         break;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 557 */     if (type % 2 == 1)
/*     */     {
/* 559 */       return element;
/*     */     }
/*     */ 
/* 562 */     if (type == SPECIAL_TOKENS.length)
/*     */     {
/* 565 */       return element;
/*     */     }
/*     */ 
/* 568 */     type /= 2;
/*     */ 
/* 570 */     switch (type)
/*     */     {
/*     */     case 0:
/* 573 */       obj = readDictionary(r);
/* 574 */       break;
/*     */     case 1:
/* 576 */       obj = readArray(r, "]");
/* 577 */       break;
/*     */     case 2:
/* 579 */       obj = readArray(r, "}");
/*     */     }
/*     */ 
/* 583 */     return obj;
/*     */   }
/*     */ 
/*     */   public static void display(Writer w, Vector v)
/*     */     throws IOException
/*     */   {
/* 590 */     display(w, v, "");
/*     */   }
/*     */ 
/*     */   public static void display(Writer w, Object obj, String space)
/*     */     throws IOException
/*     */   {
/* 596 */     if (obj instanceof String)
/*     */     {
/* 598 */       w.write(space + "String: " + obj + "\n");
/*     */     }
/* 600 */     else if (obj instanceof Vector)
/*     */     {
/* 602 */       displayVector(w, (Vector)obj, space + "  ");
/*     */     } else {
/* 604 */       if (!obj instanceof Properties)
/*     */         return;
/* 606 */       displayProperties(w, (Properties)obj, space);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void displayProperties(Writer w, Properties p, String space)
/*     */     throws IOException
/*     */   {
/* 613 */     Enumeration en = p.keys();
/* 614 */     while (en.hasMoreElements())
/*     */     {
/* 616 */       String key = (String)en.nextElement();
/* 617 */       w.write(space + key + "\n");
/* 618 */       display(w, p.get(key), space + "  ");
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void displayVector(Writer w, Vector v, String space)
/*     */     throws IOException
/*     */   {
/* 625 */     int length = v.size();
/* 626 */     for (int i = 0; i < length; ++i)
/*     */     {
/* 628 */       Object obj = v.elementAt(i);
/* 629 */       String label = "[" + i + "]\n";
/* 630 */       w.write(space + label);
/* 631 */       display(w, obj, space + "  ");
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 637 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 70600 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.fdf.Fdf
 * JD-Core Version:    0.5.4
 */