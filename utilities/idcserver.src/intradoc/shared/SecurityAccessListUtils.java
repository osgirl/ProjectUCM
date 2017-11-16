/*     */ package intradoc.shared;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.ResourceContainerUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.Table;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.server.EntityValue;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class SecurityAccessListUtils
/*     */ {
/*  41 */   public static String m_accessPrefixTypes = null;
/*  42 */   public static List<String> m_accessTypeList = null;
/*     */ 
/*     */   public static String[] parseSecurityFlags(String attribVal, String defFlag)
/*     */   {
/*  46 */     String[] retVal = new String[2];
/*  47 */     int index = attribVal.lastIndexOf(40);
/*  48 */     if (index > 0)
/*     */     {
/*  50 */       int endindex = attribVal.indexOf(41, index);
/*  51 */       if (endindex > 0)
/*     */       {
/*  53 */         retVal[0] = attribVal.substring(0, index);
/*  54 */         String privFlags = attribVal.substring(index + 1, endindex).toUpperCase();
/*  55 */         int nFlags = privFlags.length();
/*  56 */         int securityFlag = 0;
/*  57 */         for (int k = 0; k < nFlags; ++k)
/*     */         {
/*  59 */           char ch = privFlags.charAt(k);
/*  60 */           switch (ch)
/*     */           {
/*     */           case 'R':
/*  63 */             securityFlag |= 1;
/*  64 */             break;
/*     */           case 'W':
/*  66 */             securityFlag |= 2;
/*  67 */             break;
/*     */           case 'D':
/*  69 */             securityFlag |= 4;
/*  70 */             break;
/*     */           case 'A':
/*  72 */             securityFlag |= 8;
/*     */           }
/*     */         }
/*     */ 
/*  76 */         retVal[1] = Integer.toString(securityFlag);
/*     */       }
/*     */       else
/*     */       {
/*  80 */         retVal[1] = defFlag;
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/*     */     {
/*  86 */       retVal[0] = attribVal;
/*  87 */       retVal[1] = defFlag;
/*     */     }
/*     */ 
/*  90 */     return retVal;
/*     */   }
/*     */ 
/*     */   public static String stripEntitySymbols(String entityStr, String entityType)
/*     */   {
/* 103 */     String symbol = "*";
/* 104 */     if (entityType.equalsIgnoreCase("user"))
/*     */     {
/* 106 */       symbol = "&";
/*     */     }
/*     */     else
/*     */     {
/* 110 */       symbol = "@";
/*     */     }
/*     */ 
/* 113 */     Vector entityList = StringUtils.parseArray(entityStr, ',', '^');
/* 114 */     int size = entityList.size();
/* 115 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 117 */       String name = (String)entityList.elementAt(i);
/* 118 */       if (name.startsWith(symbol))
/*     */       {
/* 120 */         name = name.substring(1);
/*     */       }
/* 122 */       entityList.setElementAt(name, i);
/*     */     }
/* 124 */     return StringUtils.createString(entityList, ',', '^');
/*     */   }
/*     */ 
/*     */   public static int determineBestEntityPrivilege(UserData userData, List<EntityValue> entityList, ExecutionContext cxt)
/*     */     throws DataException
/*     */   {
/* 142 */     int currentPriv = 0;
/* 143 */     String userName = userData.m_name;
/*     */ 
/* 145 */     boolean hasEntityList = false;
/* 146 */     for (EntityValue entityValue : entityList)
/*     */     {
/* 148 */       String entityStr = entityValue.m_entityListStr;
/* 149 */       if ((entityStr != null) && (entityStr.length() > 0))
/*     */       {
/* 151 */         hasEntityList = true;
/* 152 */         if (entityValue.m_type.equals("user"))
/*     */         {
/* 154 */           entityStr = entityStr.toLowerCase();
/* 155 */           String userClause = "&" + userName.toLowerCase() + "(";
/*     */ 
/* 157 */           int index = entityStr.indexOf(userClause);
/* 158 */           while ((index >= 0) && (currentPriv != 15))
/*     */           {
/* 160 */             index += userClause.length();
/* 161 */             int endIndex = entityStr.indexOf(")", index);
/* 162 */             if (endIndex >= 0)
/*     */             {
/* 164 */               String priv = entityStr.substring(index, endIndex);
/* 165 */               switch (priv.charAt(priv.length() - 1))
/*     */               {
/*     */               case 'r':
/* 168 */                 currentPriv |= 1;
/* 169 */                 break;
/*     */               case 'w':
/* 171 */                 currentPriv |= 3;
/* 172 */                 break;
/*     */               case 'd':
/* 174 */                 currentPriv |= 7;
/* 175 */                 break;
/*     */               case 'a':
/* 177 */                 currentPriv |= 15;
/*     */               }
/*     */ 
/*     */             }
/*     */ 
/* 182 */             index = entityStr.indexOf(userClause, index);
/*     */           }
/*     */         }
/* 185 */         else if (entityValue.m_type.equals("alias"))
/*     */         {
/* 187 */           entityStr = entityStr.toLowerCase();
/*     */ 
/* 189 */           AliasData aliasData = (AliasData)SharedObjects.getTable("Alias");
/* 190 */           if (aliasData == null)
/*     */           {
/* 192 */             aliasData = new AliasData();
/*     */           }
/*     */ 
/* 195 */           String[][] aliasList = aliasData.getAliasesForUser(userName);
/* 196 */           int numAlias = aliasList.length;
/*     */ 
/* 198 */           for (int j = 0; j < numAlias; ++j)
/*     */           {
/* 200 */             String alias = aliasList[j][0];
/* 201 */             String aliasClause = "@" + alias.toLowerCase() + "(";
/*     */ 
/* 203 */             int index = entityStr.indexOf(aliasClause);
/* 204 */             while ((index >= 0) && (currentPriv != 15))
/*     */             {
/* 206 */               index += aliasClause.length();
/* 207 */               int endIndex = entityStr.indexOf(")", index);
/* 208 */               if (endIndex >= 0)
/*     */               {
/* 210 */                 String priv = entityStr.substring(index, endIndex);
/* 211 */                 switch (priv.charAt(priv.length() - 1))
/*     */                 {
/*     */                 case 'r':
/* 214 */                   currentPriv |= 1;
/* 215 */                   break;
/*     */                 case 'w':
/* 217 */                   currentPriv |= 3;
/* 218 */                   break;
/*     */                 case 'd':
/* 220 */                   currentPriv |= 7;
/* 221 */                   break;
/*     */                 case 'a':
/* 223 */                   currentPriv |= 15;
/*     */                 }
/*     */ 
/*     */               }
/*     */ 
/* 228 */               index = entityStr.indexOf(aliasClause, index);
/*     */             }
/*     */           }
/*     */         }
/*     */         else
/*     */         {
/* 234 */           cxt.setCachedObject("EntityValue", entityValue);
/* 235 */           cxt.setCachedObject("EntityUserData", userData);
/* 236 */           cxt.setCachedObject("CurrentPrivilege", "" + currentPriv);
/* 237 */           cxt.setReturnValue(null);
/*     */           try
/*     */           {
/* 240 */             PluginFilters.filter("determineBestEntityPrivilege", null, null, cxt);
/*     */           }
/*     */           catch (ServiceException e)
/*     */           {
/* 244 */             throw new DataException(e, "csFilterError", new Object[] { "determineBestEntityPrivilege" });
/*     */           }
/* 246 */           Object returnVal = cxt.getReturnValue();
/* 247 */           if ((returnVal != null) && (returnVal instanceof String))
/*     */           {
/* 249 */             currentPriv = NumberUtils.parseInteger((String)returnVal, currentPriv);
/*     */           }
/*     */         }
/* 252 */         if (currentPriv == 15) {
/*     */           break;
/*     */         }
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 259 */     if ((SecurityUtils.m_accessListPrivilegesGrantedWhenEmpty) && (!hasEntityList))
/*     */     {
/* 261 */       currentPriv = 15;
/*     */     }
/* 263 */     return currentPriv;
/*     */   }
/*     */ 
/*     */   public static String getPermissionString(int priv)
/*     */   {
/* 274 */     String permStr = null;
/* 275 */     if ((priv & 0x8) != 0)
/*     */     {
/* 277 */       permStr = "RWDA";
/*     */     }
/* 279 */     else if ((priv & 0x4) != 0)
/*     */     {
/* 281 */       permStr = "RWD";
/*     */     }
/* 283 */     else if ((priv & 0x2) != 0)
/*     */     {
/* 285 */       permStr = "RW";
/*     */     }
/* 287 */     else if ((priv & 0x1) != 0)
/*     */     {
/* 289 */       permStr = "R";
/*     */     }
/*     */     else
/*     */     {
/* 293 */       permStr = "";
/*     */     }
/*     */ 
/* 296 */     return permStr;
/*     */   }
/*     */ 
/*     */   public static String makePrivilegeStr(int privilege)
/*     */   {
/* 301 */     return makePrivilegeStr(privilege);
/*     */   }
/*     */ 
/*     */   public static String makePrivilegeStr(long privilege)
/*     */   {
/* 306 */     String str = "";
/* 307 */     int length = PermissionsData.m_defs.length;
/* 308 */     for (int i = 0; i < length; ++i)
/*     */     {
/* 310 */       long privDef = NumberUtils.parseHexStringAsLong(PermissionsData.m_defs[i][2]);
/* 311 */       if ((privDef & privilege) == 0L)
/*     */         continue;
/* 313 */       str = str + PermissionsData.m_defs[i][1];
/*     */     }
/*     */ 
/* 316 */     return str;
/*     */   }
/*     */ 
/*     */   public static int getPrivilegeRights(char ch)
/*     */   {
/* 321 */     int priv = 0;
/* 322 */     switch (ch)
/*     */     {
/*     */     case 'R':
/* 325 */       priv = 1;
/* 326 */       break;
/*     */     case 'W':
/* 329 */       priv = 2;
/* 330 */       break;
/*     */     case 'D':
/* 333 */       priv = 4;
/* 334 */       break;
/*     */     case 'A':
/* 337 */       priv = 8;
/*     */     }
/*     */ 
/* 341 */     return priv;
/*     */   }
/*     */ 
/*     */   public static int getRightsForApp(String name)
/*     */   {
/* 346 */     for (int i = 0; i < PermissionsData.m_appAllPsgDefs.length; ++i)
/*     */     {
/* 348 */       String appName = PermissionsData.m_appAllPsgDefs[i][1];
/* 349 */       if (appName.equals(name))
/*     */       {
/* 351 */         return Integer.decode(PermissionsData.m_appAllPsgDefs[i][2]).intValue();
/*     */       }
/*     */     }
/* 354 */     return 0;
/*     */   }
/*     */ 
/*     */   public static int getAllAppRights()
/*     */   {
/* 360 */     int numRights = PermissionsData.m_appAllPsgDefs.length;
/*     */ 
/* 362 */     int priv = 0;
/* 363 */     for (int i = 0; i < numRights; ++i)
/*     */     {
/* 365 */       priv |= Integer.decode(PermissionsData.m_appAllPsgDefs[i][2]).intValue();
/*     */     }
/* 367 */     return priv;
/*     */   }
/*     */ 
/*     */   public static int parsePrivilegeRights(String str)
/*     */   {
/* 377 */     if ((str == null) || (str.length() == 0))
/*     */     {
/* 379 */       return 0;
/*     */     }
/*     */ 
/* 382 */     char c = str.charAt(0);
/* 383 */     if ((c == 'r') || (c == 'R'))
/*     */     {
/* 385 */       if (str.equalsIgnoreCase("RWDA"))
/*     */       {
/* 387 */         return 15;
/*     */       }
/*     */ 
/* 390 */       if (str.equalsIgnoreCase("RWD"))
/*     */       {
/* 392 */         return 7;
/*     */       }
/*     */ 
/* 395 */       if (str.equalsIgnoreCase("RW"))
/*     */       {
/* 397 */         return 3;
/*     */       }
/* 399 */       if (str.equalsIgnoreCase("R"))
/*     */       {
/* 401 */         return 1;
/*     */       }
/* 403 */       return 0;
/*     */     }
/* 405 */     if ((c == '0') || (str.length() > 2))
/*     */     {
/* 407 */       c = str.charAt(1);
/* 408 */       if ((c == 'x') || (c == 'X'))
/*     */       {
/* 410 */         long value = NumberUtils.parseHexStringAsLong(str);
/* 411 */         if ((value < 1L) || (value > 2147483647L))
/*     */         {
/* 413 */           value = 0L;
/*     */         }
/* 415 */         return (int)value;
/*     */       }
/*     */     }
/*     */     try
/*     */     {
/* 420 */       return Integer.parseInt(str);
/*     */     }
/*     */     catch (Exception ignore)
/*     */     {
/*     */     }
/*     */ 
/* 426 */     return 0;
/*     */   }
/*     */ 
/*     */   public static DataResultSet makeResultSetFromAccessListString(String accessListStr)
/*     */     throws DataException
/*     */   {
/* 463 */     List accessList = StringUtils.makeListFromSequenceSimple(accessListStr);
/* 464 */     int numItems = accessList.size();
/* 465 */     String[] accessArray = new String[numItems];
/* 466 */     accessList.toArray(accessArray);
/*     */ 
/* 468 */     initAccessTypes();
/*     */ 
/* 471 */     int[] parensArray = new int[numItems];
/* 472 */     int[] lengthArray = new int[numItems];
/* 473 */     boolean hasParens = true;
/* 474 */     for (int i = 0; i < numItems; ++i)
/*     */     {
/* 476 */       int length = accessArray[i].length();
/* 477 */       int index = accessArray[i].lastIndexOf(40);
/* 478 */       boolean endParens = ')' == accessArray[i].charAt(length - 1);
/* 479 */       if (i == 0)
/*     */       {
/* 481 */         hasParens = endParens;
/*     */       }
/* 483 */       if (endParens == hasParens) if (index >= 0 == hasParens)
/*     */           break label146;
/* 485 */       throw new DataException(null, "csClbraAccessListFormatError", new Object[] { accessArray[i] });
/*     */ 
/* 487 */       label146: lengthArray[i] = length;
/* 488 */       parensArray[i] = index;
/*     */     }
/* 490 */     String[] privArray = null;
/*     */ 
/* 492 */     if (!hasParens)
/*     */     {
/* 494 */       if (numItems % 2 != 0)
/*     */       {
/* 496 */         throw new DataException(null, "csClbraAccessListFormatError", new Object[] { accessArray[(numItems - 1)] });
/*     */       }
/* 498 */       numItems >>= 1;
/* 499 */       String[] userArray = new String[numItems];
/* 500 */       privArray = new String[numItems];
/* 501 */       int a = 0; for (int u = 0; u < numItems; ++u)
/*     */       {
/* 503 */         lengthArray[u] = lengthArray[a];
/* 504 */         userArray[u] = accessArray[(a++)];
/* 505 */         privArray[u] = accessArray[(a++)];
/*     */       }
/* 507 */       accessArray = userArray;
/*     */     }
/*     */ 
/* 510 */     String[] columnNames = { "id", "priv", "type" };
/* 511 */     DataResultSet results = new DataResultSet(columnNames);
/* 512 */     for (int i = 0; i < numItems; ++i)
/*     */     {
/* 514 */       int index = (hasParens) ? parensArray[i] : lengthArray[i];
/* 515 */       int offset = 0;
/* 516 */       char typeChar = accessArray[i].charAt(0);
/* 517 */       int typeIndex = m_accessPrefixTypes.indexOf(typeChar);
/* 518 */       String type = "";
/* 519 */       if (typeIndex >= 0)
/*     */       {
/* 521 */         offset = 1;
/* 522 */         type = (String)m_accessTypeList.get(typeIndex);
/*     */       }
/* 524 */       String id = accessArray[i].substring(offset, index);
/* 525 */       String priv = (hasParens) ? accessArray[i].substring(parensArray[i] + 1, lengthArray[i] - 1) : privArray[i];
/* 526 */       int privInt = parsePrivilegeRights(priv);
/* 527 */       if (privInt <= 0)
/*     */       {
/* 529 */         throw new DataException(null, "csClbraAccessListFormatError", new Object[] { accessArray[i] });
/*     */       }
/* 531 */       priv = makePrivilegeStr(privInt);
/*     */ 
/* 533 */       List row = new ArrayList(3);
/* 534 */       row.add(id);
/* 535 */       row.add(priv);
/* 536 */       row.add(type);
/* 537 */       results.addRowWithList(row);
/*     */     }
/* 539 */     return results;
/*     */   }
/*     */ 
/*     */   public static void initAccessTypes()
/*     */   {
/* 548 */     if (m_accessPrefixTypes != null)
/*     */       return;
/* 550 */     Table secFields = ResourceContainerUtils.getDynamicTableResource("EntitySecurityFields");
/* 551 */     IdcStringBuilder prefBuff = new IdcStringBuilder();
/* 552 */     List types = new ArrayList();
/* 553 */     for (int i = 0; i < secFields.getNumRows(); ++i)
/*     */     {
/* 555 */       prefBuff.append(secFields.getString(i, 2));
/* 556 */       types.add(secFields.getString(i, 1));
/*     */     }
/* 558 */     m_accessPrefixTypes = prefBuff.toString();
/* 559 */     m_accessTypeList = types;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 565 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 93014 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.SecurityAccessListUtils
 * JD-Core Version:    0.5.4
 */