/*     */ package intradoc.preview;
/*     */ 
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.PropertiesTreeNode;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataSerializeUtils;
/*     */ import intradoc.server.project.ProjectUtils;
/*     */ import intradoc.shared.QueryOperatorUtils;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Date;
/*     */ import java.util.List;
/*     */ import java.util.Properties;
/*     */ import java.util.StringTokenizer;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class PreviewUtils
/*     */ {
/*     */   public static List parseProject(List nodes, DataBinder docData, List resNodes)
/*     */     throws ServiceException
/*     */   {
/*  37 */     int size = nodes.size();
/*  38 */     for (int i = 0; i < size; ++i)
/*     */     {
/*  40 */       PropertiesTreeNode node = (PropertiesTreeNode)nodes.get(i);
/*  41 */       String name = node.m_name;
/*  42 */       Properties props = node.m_properties;
/*  43 */       if (name.equals("project"))
/*     */       {
/*  45 */         boolean isOurs = checkOwnership(props, "sourcePath", "idc://");
/*  46 */         if (!isOurs) {
/*     */           continue;
/*     */         }
/*     */ 
/*  50 */         Vector subNodes = new IdcVector();
/*  51 */         parseProject(node.m_subNodes, docData, subNodes);
/*  52 */         if (subNodes.size() > 0)
/*     */         {
/*  54 */           PropertiesTreeNode newNode = new PropertiesTreeNode(name, props);
/*  55 */           newNode.m_subNodes = subNodes;
/*  56 */           resNodes.add(newNode);
/*     */         }
/*     */       }
/*  59 */       else if (name.equals("publication"))
/*     */       {
/*  62 */         Vector subNodes = new IdcVector();
/*  63 */         parseProject(node.m_subNodes, docData, subNodes);
/*  64 */         if (subNodes.size() > 0)
/*     */         {
/*  66 */           PropertiesTreeNode newNode = new PropertiesTreeNode(name, props);
/*  67 */           newNode.m_subNodes = subNodes;
/*  68 */           resNodes.add(newNode);
/*     */         }
/*     */       }
/*  71 */       else if (name.equals("document"))
/*     */       {
/*  73 */         boolean isOurs = checkOwnership(props, "repoid", "Stellent");
/*  74 */         if (!isOurs)
/*     */         {
/*  77 */           isOurs = checkOwnership(props, "repoid", "Xpedio");
/*  78 */           if (!isOurs) {
/*     */             continue;
/*     */           }
/*     */         }
/*     */ 
/*  83 */         String sourcePath = props.getProperty("sourcePath");
/*  84 */         boolean isIn = isInSourcePath(sourcePath, docData);
/*  85 */         if (isIn)
/*     */         {
/*  87 */           resNodes.add(node);
/*     */         }
/*     */       } else {
/*  90 */         if (!name.equals("directory"))
/*     */           continue;
/*  92 */         boolean isOurs = checkOwnership(props, "repoid", "Stellent");
/*  93 */         if (!isOurs)
/*     */         {
/*  96 */           isOurs = checkOwnership(props, "repoid", "Xpedio");
/*  97 */           if (!isOurs) {
/*     */             continue;
/*     */           }
/*     */         }
/*     */ 
/* 102 */         String dirType = props.getProperty("dirType");
/* 103 */         if (dirType == null) continue; if (!dirType.equals("Restricted")) {
/*     */           continue;
/*     */         }
/*     */ 
/* 107 */         String queryPath = props.getProperty("queryPath");
/* 108 */         boolean isIn = isInSourceQuery(queryPath, docData);
/* 109 */         if (!isIn) {
/*     */           continue;
/*     */         }
/* 112 */         PropertiesTreeNode newNode = new PropertiesTreeNode(name, props);
/* 113 */         resNodes.add(newNode);
/*     */       }
/*     */     }
/*     */ 
/* 117 */     return resNodes;
/*     */   }
/*     */ 
/*     */   protected static boolean checkOwnership(Properties props, String key, String startVal)
/*     */   {
/* 122 */     String repo = props.getProperty(key);
/* 123 */     if (repo == null)
/*     */     {
/* 125 */       return false;
/*     */     }
/*     */ 
/* 128 */     return repo.startsWith(startVal);
/*     */   }
/*     */ 
/*     */   protected static boolean isInSourcePath(String sourcePath, DataBinder docData)
/*     */     throws ServiceException
/*     */   {
/* 134 */     if (sourcePath == null)
/*     */     {
/* 136 */       return false;
/*     */     }
/* 138 */     Properties srcProps = ProjectUtils.parseSourcePath(sourcePath, "IDC_Name", "idc://");
/*     */ 
/* 140 */     String srcIdcName = srcProps.getProperty("IDC_Name");
/* 141 */     String idcName = SharedObjects.getEnvironmentValue("IDC_Name");
/* 142 */     if (!srcIdcName.equalsIgnoreCase(idcName))
/*     */     {
/* 144 */       return false;
/*     */     }
/*     */ 
/* 147 */     String docName = docData.getAllowMissing("dDocName");
/* 148 */     String srcName = srcProps.getProperty("dDocName");
/*     */ 
/* 151 */     if ((docName != null) && (srcName != null))
/*     */     {
/* 153 */       return docName.equalsIgnoreCase(srcName);
/*     */     }
/* 155 */     return false;
/*     */   }
/*     */ 
/*     */   protected static boolean isInSourceQuery(String queryPath, DataBinder docData)
/*     */     throws ServiceException
/*     */   {
/* 161 */     if (queryPath == null)
/*     */     {
/* 163 */       return false;
/*     */     }
/*     */ 
/* 166 */     Properties srcProps = ProjectUtils.parseSourcePath(queryPath, "IDC_Name", "idc://");
/*     */ 
/* 168 */     String srcIdcName = srcProps.getProperty("IDC_Name");
/* 169 */     String idcName = SharedObjects.getEnvironmentValue("IDC_Name");
/* 170 */     if (!srcIdcName.equalsIgnoreCase(idcName))
/*     */     {
/* 172 */       return false;
/*     */     }
/*     */ 
/* 175 */     String queryFieldValues = srcProps.getProperty("QueryFieldValues");
/* 176 */     if ((queryFieldValues == null) || (queryFieldValues.length() == 0))
/*     */     {
/* 179 */       return true;
/*     */     }
/*     */ 
/* 183 */     queryFieldValues = DataSerializeUtils.decode(docData, queryFieldValues, null);
/*     */ 
/* 186 */     boolean isMatched = false;
/* 187 */     StringTokenizer tokenizer = new StringTokenizer(queryFieldValues, "\n");
/* 188 */     while (tokenizer.hasMoreElements())
/*     */     {
/* 190 */       String name = tokenizer.nextToken().trim();
/* 191 */       String op = tokenizer.nextToken().trim();
/* 192 */       String value = tokenizer.nextToken().trim();
/*     */ 
/* 194 */       op = QueryOperatorUtils.findOperatorFromAlias(op);
/*     */ 
/* 196 */       boolean isPatternCompare = false;
/* 197 */       String dValue = docData.getAllowMissing(name);
/* 198 */       if (dValue != null)
/*     */       {
/* 200 */         String[] pattern = null;
/* 201 */         if (op.equalsIgnoreCase("hasAsWord"))
/*     */         {
/* 203 */           isPatternCompare = true;
/* 204 */           value = createContainsPattern(value);
/* 205 */           dValue = createContainsPattern(dValue);
/*     */ 
/* 207 */           pattern = new String[4];
/* 208 */           pattern[0] = value;
/* 209 */           pattern[1] = ("* " + value + " *");
/* 210 */           pattern[2] = (value + " *");
/* 211 */           pattern[3] = ("* " + value);
/*     */         }
/* 213 */         else if ((op.equalsIgnoreCase("hasAsSubstring")) || (op.equalsIgnoreCase("equals")) || (op.equalsIgnoreCase("endsWith")) || (op.equalsIgnoreCase("beginsWith")))
/*     */         {
/* 216 */           isPatternCompare = true;
/* 217 */           pattern = new String[1];
/* 218 */           if (op.equalsIgnoreCase("hasAsSubString"))
/*     */           {
/* 220 */             pattern[0] = ("*" + value + "*");
/*     */           }
/* 222 */           else if (op.equalsIgnoreCase("equals"))
/*     */           {
/* 224 */             pattern[0] = value;
/*     */           }
/* 226 */           else if (op.equalsIgnoreCase("endsWith"))
/*     */           {
/* 228 */             pattern[0] = ("*" + value);
/*     */           }
/* 230 */           else if (op.equalsIgnoreCase("beginsWith"))
/*     */           {
/* 232 */             pattern[0] = (value + "*");
/*     */           }
/*     */         }
/* 235 */         else if ((op.equalsIgnoreCase("dateGE")) || (op.equalsIgnoreCase("dateLE")))
/*     */         {
/*     */           try
/*     */           {
/* 239 */             Date dDate = LocaleResources.parseDate(dValue, null);
/* 240 */             Date dte = LocaleResources.parseDate(value, null);
/* 241 */             if (op.equalsIgnoreCase("dateGE"))
/*     */             {
/* 243 */               isMatched = dDate.after(dte);
/*     */             }
/*     */             else
/*     */             {
/* 247 */               isMatched = dDate.before(dte);
/*     */             }
/*     */           }
/*     */           catch (Exception ignore)
/*     */           {
/*     */           }
/*     */ 
/*     */         }
/* 255 */         else if ((op.equalsIgnoreCase("numberGE")) || (op.equalsIgnoreCase("numberLE")))
/*     */         {
/*     */           try
/*     */           {
/* 259 */             int dValInt = Integer.parseInt(dValue);
/* 260 */             int valInt = Integer.parseInt(value);
/* 261 */             if (op.equalsIgnoreCase("numberGE"))
/*     */             {
/* 263 */               isMatched = dValInt >= valInt;
/*     */             }
/*     */             else
/*     */             {
/* 267 */               isMatched = dValInt <= valInt;
/*     */             }
/*     */           }
/*     */           catch (Exception ignore)
/*     */           {
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/* 276 */         if (isPatternCompare)
/*     */         {
/* 278 */           for (int i = 0; i < pattern.length; ++i)
/*     */           {
/* 280 */             isMatched = StringUtils.matchEx(dValue, pattern[i], false, true);
/* 281 */             if (isMatched) {
/*     */               break;
/*     */             }
/*     */           }
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 289 */       if (!isMatched)
/*     */       {
/* 291 */         return false;
/*     */       }
/*     */     }
/*     */ 
/* 295 */     return isMatched;
/*     */   }
/*     */ 
/*     */   public static String createContainsPattern(String pattern)
/*     */   {
/* 307 */     int len = pattern.length();
/* 308 */     char[] chars = new char[len];
/* 309 */     pattern.getChars(0, len, chars, 0);
/*     */ 
/* 311 */     for (int i = 0; i < len; ++i)
/*     */     {
/* 313 */       char ch = chars[i];
/* 314 */       if (ch == '?') continue; if (ch == '*') {
/*     */         continue;
/*     */       }
/*     */ 
/* 318 */       if ((Character.isDigit(ch)) || (Character.isLetter(ch)))
/*     */         continue;
/* 320 */       chars[i] = ' ';
/*     */     }
/*     */ 
/* 324 */     String str = new String(chars);
/* 325 */     str.trim();
/* 326 */     return str;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 331 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.preview.PreviewUtils
 * JD-Core Version:    0.5.4
 */