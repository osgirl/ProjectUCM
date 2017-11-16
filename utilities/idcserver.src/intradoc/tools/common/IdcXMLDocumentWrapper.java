/*     */ package intradoc.tools.common;
/*     */ 
/*     */ import java.io.File;
/*     */ import java.io.IOException;
/*     */ import javax.xml.parsers.DocumentBuilder;
/*     */ import javax.xml.parsers.DocumentBuilderFactory;
/*     */ import javax.xml.transform.Transformer;
/*     */ import javax.xml.transform.TransformerFactory;
/*     */ import javax.xml.transform.dom.DOMSource;
/*     */ import javax.xml.transform.stream.StreamResult;
/*     */ import org.w3c.dom.DOMException;
/*     */ import org.w3c.dom.Document;
/*     */ import org.w3c.dom.Element;
/*     */ import org.w3c.dom.Node;
/*     */ import org.w3c.dom.NodeList;
/*     */ 
/*     */ public class IdcXMLDocumentWrapper
/*     */ {
/*     */   public Document m_document;
/*     */ 
/*     */   public void clear()
/*     */   {
/*  50 */     this.m_document = null;
/*     */   }
/*     */ 
/*     */   public void createEmptyDocument()
/*     */     throws DOMException
/*     */   {
/*     */     Document document;
/*     */     try
/*     */     {
/*  63 */       document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/*  67 */       DOMException e = new DOMException(9, "unable to create empty document");
/*  68 */       e.initCause(t);
/*  69 */       throw e;
/*     */     }
/*  71 */     document.setXmlStandalone(false);
/*  72 */     this.m_document = document;
/*     */   }
/*     */ 
/*     */   public void loadFromFile(File file)
/*     */     throws DOMException, IOException
/*     */   {
/*     */     DocumentBuilder builder;
/*     */     try
/*     */     {
/*  88 */       builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/*  92 */       DOMException e = new DOMException(9, "unable to create parser");
/*  93 */       e.initCause(t);
/*  94 */       throw e;
/*     */     }
/*     */     Document document;
/*     */     try
/*     */     {
/*  99 */       document = builder.parse(file);
/*     */     }
/*     */     catch (IOException ioe)
/*     */     {
/* 103 */       throw ioe;
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/* 107 */       DOMException e = new DOMException(12, new StringBuilder().append("unable to parse file \"").append(file).append("\"").toString());
/* 108 */       e.initCause(t);
/* 109 */       throw e;
/*     */     }
/* 111 */     document.setXmlStandalone(true);
/* 112 */     this.m_document = document;
/*     */   }
/*     */ 
/*     */   public void saveToFile(File file)
/*     */     throws DOMException, IOException
/*     */   {
/*     */     Transformer transformer;
/*     */     try
/*     */     {
/* 127 */       transformer = TransformerFactory.newInstance().newTransformer();
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/* 131 */       DOMException e = new DOMException(9, "unable to create transformer");
/* 132 */       e.initCause(t);
/* 133 */       throw e;
/*     */     }
/* 135 */     transformer.setOutputProperty("indent", "yes");
/*     */     try
/*     */     {
/* 138 */       DOMSource source = new DOMSource(this.m_document);
/* 139 */       StreamResult result = new StreamResult(file);
/* 140 */       transformer.transform(source, result);
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/* 144 */       throw new IOException(new StringBuilder().append("unable to save file \"").append(file).append("\"").toString(), t);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void insertBeforeLastTextNode(Node parent, Node child)
/*     */     throws DOMException
/*     */   {
/* 161 */     Node last = parent.getLastChild();
/* 162 */     if ((last == null) || (last.getNodeType() != 3))
/*     */     {
/* 164 */       parent.appendChild(child);
/*     */     }
/*     */     else
/*     */     {
/* 168 */       parent.insertBefore(child, last);
/*     */     }
/*     */   }
/*     */ 
/*     */   public Element insertElementBeforeLastTextNode(Node node, String tagName)
/*     */     throws DOMException
/*     */   {
/* 182 */     Element child = this.m_document.createElement(tagName);
/* 183 */     insertBeforeLastTextNode(node, child);
/* 184 */     return child;
/*     */   }
/*     */ 
/*     */   public Node insertTextBeforeLastTextNode(Node node, String text)
/*     */     throws DOMException
/*     */   {
/* 197 */     Node child = this.m_document.createTextNode(text);
/* 198 */     insertBeforeLastTextNode(node, child);
/* 199 */     return child;
/*     */   }
/*     */ 
/*     */   public void removeAllChildNodes(Node node)
/*     */     throws DOMException
/*     */   {
/* 211 */     while ((child = node.getFirstChild()) != null)
/*     */     {
/*     */       Node child;
/* 213 */       node.removeChild(child);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void removeNodeAndPriorTextNode(Node node)
/*     */     throws DOMException
/*     */   {
/* 225 */     Node parent = node.getParentNode();
/* 226 */     Node sibling = node.getPreviousSibling();
/* 227 */     parent.removeChild(node);
/* 228 */     if (sibling.getNodeType() != 3)
/*     */       return;
/* 230 */     parent.removeChild(sibling);
/*     */   }
/*     */ 
/*     */   public String getNodeText(Node node)
/*     */     throws DOMException
/*     */   {
/* 243 */     Node text = node.getFirstChild();
/* 244 */     if ((text != node.getLastChild()) || (text.getNodeType() != 3))
/*     */     {
/* 246 */       throw new DOMException(17, "expected single text node as child");
/*     */     }
/* 248 */     return text.getNodeValue();
/*     */   }
/*     */ 
/*     */   public String getNodeAttributeValue(Node node, String attributeName)
/*     */     throws DOMException
/*     */   {
/* 261 */     if (!node instanceof Element)
/*     */     {
/* 263 */       throw new DOMException(17, "not an element node");
/*     */     }
/* 265 */     Element el = (Element)node;
/* 266 */     return el.getAttribute(attributeName);
/*     */   }
/*     */ 
/*     */   public void setNodeAttributeValue(Node node, String attributeName, String value)
/*     */     throws DOMException
/*     */   {
/* 279 */     if (!node instanceof Element)
/*     */     {
/* 281 */       throw new DOMException(17, "not an element node");
/*     */     }
/* 283 */     Element el = (Element)node;
/* 284 */     el.setAttribute(attributeName, value);
/*     */   }
/*     */ 
/*     */   public Node lookupNode(Node parent, String[] path)
/*     */     throws DOMException
/*     */   {
/* 299 */     if (parent == null)
/*     */     {
/* 301 */       parent = this.m_document;
/*     */     }
/* 303 */     else if (parent.getOwnerDocument() != this.m_document)
/*     */     {
/* 305 */       throw new DOMException(4, "node not in this document");
/*     */     }
/* 307 */     for (int d = 0; d < path.length; ++d)
/*     */     {
/* 309 */       Node child = parent.getFirstChild();
/* 310 */       while (child != null)
/*     */       {
/* 312 */         if ((child.getNodeType() == 1) && (path[d].equals(child.getNodeName()))) {
/*     */           break;
/*     */         }
/*     */ 
/* 316 */         child = child.getNextSibling();
/*     */       }
/* 318 */       if (child == null)
/*     */       {
/* 320 */         return null;
/*     */       }
/* 322 */       parent = child;
/*     */     }
/* 324 */     return parent;
/*     */   }
/*     */ 
/*     */   public Node lookupOrAppendNode(Node parent, String[] path)
/*     */     throws DOMException
/*     */   {
/* 339 */     int depth = 0;
/* 340 */     if (parent == null)
/*     */     {
/* 342 */       parent = this.m_document;
/*     */     }
/*     */     else
/*     */     {
/* 346 */       if (parent.getOwnerDocument() != this.m_document)
/*     */       {
/* 348 */         throw new DOMException(4, "node not in this document");
/*     */       }
/* 350 */       Node node = parent;
/*     */ 
/* 352 */       while ((node != null) && (node != this.m_document))
/*     */       {
/* 354 */         node = node.getParentNode();
/* 355 */         ++depth;
/*     */       }
/*     */     }
/* 358 */     for (int d = 0; d < path.length; ++d)
/*     */     {
/* 360 */       Node child = parent.getFirstChild();
/* 361 */       while (child != null)
/*     */       {
/* 363 */         if ((child.getNodeType() == 1) && (path[d].equals(child.getNodeName()))) {
/*     */           break;
/*     */         }
/*     */ 
/* 367 */         child = child.getNextSibling();
/*     */       }
/* 369 */       if (child == null)
/*     */       {
/* 371 */         StringBuilder str = new StringBuilder("\n");
/* 372 */         if (depth + d > 0)
/*     */         {
/* 374 */           for (int i = 0; i < depth + d; ++i)
/*     */           {
/* 376 */             str.append('\t');
/*     */           }
/* 378 */           insertTextBeforeLastTextNode(parent, str.toString());
/*     */         }
/* 380 */         child = insertElementBeforeLastTextNode(parent, path[d]);
/* 381 */         if (depth + d > 0)
/*     */         {
/* 383 */           insertTextBeforeLastTextNode(child, str.toString());
/*     */         }
/*     */       }
/* 386 */       parent = child;
/*     */     }
/* 388 */     return parent;
/*     */   }
/*     */ 
/*     */   public Node replaceNodeWithText(Node node, String text)
/*     */     throws DOMException
/*     */   {
/* 401 */     Node child = this.m_document.createTextNode(text);
/* 402 */     removeAllChildNodes(node);
/* 403 */     node.appendChild(child);
/* 404 */     return child;
/*     */   }
/*     */ 
/*     */   protected void appendNodeDump(StringBuilder str, Node node, int level)
/*     */   {
/* 414 */     for (int i = 0; i < level; ++i) str.append('\t');
/* 415 */     str.append("type=");
/* 416 */     str.append(node.getNodeType());
/* 417 */     str.append("\tname=");
/* 418 */     str.append(node.getNodeName());
/* 419 */     if (node.getNodeType() == 3)
/*     */     {
/* 421 */       str.append("\ttext=\"");
/* 422 */       String value = node.getNodeValue();
/* 423 */       int length = value.length();
/* 424 */       for (int i = 0; i < length; ++i)
/*     */       {
/* 426 */         char ch = value.charAt(i);
/* 427 */         if (ch >= ' ')
/*     */         {
/* 429 */           str.append(ch);
/*     */         }
/* 431 */         else if (ch == '\n')
/*     */         {
/* 433 */           str.append("\\n");
/*     */         }
/* 435 */         else if (ch == '\t')
/*     */         {
/* 437 */           str.append("\\t");
/*     */         }
/*     */         else
/*     */         {
/* 441 */           str.append("\\?");
/*     */         }
/*     */       }
/* 444 */       str.append('"');
/*     */     }
/* 446 */     str.append('\n');
/* 447 */     NodeList list = node.getChildNodes();
/* 448 */     int length = list.getLength();
/* 449 */     for (int i = 0; i < length; ++i)
/*     */     {
/* 451 */       appendNodeDump(str, list.item(i), level + 1);
/*     */     }
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 458 */     StringBuilder str = new StringBuilder();
/* 459 */     appendNodeDump(str, this.m_document, 0);
/* 460 */     return str.toString();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 466 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97481 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.tools.common.IdcXMLDocumentWrapper
 * JD-Core Version:    0.5.4
 */