/*      */ package intradoc.shared;
/*      */ 
/*      */ import intradoc.common.ExecutionContext;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.SortUtils;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.util.Hashtable;
/*      */ import java.util.Iterator;
/*      */ import java.util.List;
/*      */ import java.util.Set;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class SecurityUtils
/*      */ {
/*      */   public static boolean m_useAccounts;
/*      */   public static boolean m_useCollaboration;
/*      */   public static boolean m_useEntitySecurity;
/*      */   public static boolean m_accessListPrivilegesGrantedWhenEmpty;
/*      */ 
/*      */   public static void init()
/*      */   {
/*   49 */     m_useAccounts = SharedObjects.getEnvValueAsBoolean("UseAccounts", false);
/*   50 */     m_useCollaboration = SharedObjects.getEnvValueAsBoolean("UseCollaboration", false);
/*      */ 
/*   52 */     m_useEntitySecurity = SharedObjects.getEnvValueAsBoolean("UseEntitySecurity", m_useCollaboration);
/*   53 */     if (m_useCollaboration)
/*      */     {
/*   55 */       m_useAccounts = true;
/*      */     }
/*   57 */     m_accessListPrivilegesGrantedWhenEmpty = SharedObjects.getEnvValueAsBoolean("AccessListPrivilegesGrantedWhenEmpty", true);
/*      */   }
/*      */ 
/*      */   public static UserDocumentAccessFilter getUserDocumentAccessFilter(UserData userData, int priv)
/*      */     throws DataException, ServiceException
/*      */   {
/*   67 */     Vector groups = getUserGroupsWithPrivilege(userData, priv);
/*   68 */     Vector accounts = getUserAccountsWithPrivilege(userData, priv, true);
/*   69 */     boolean isAdmin = isUserOfRole(userData, "admin");
/*   70 */     int appRights = determineGroupPrivilege(userData, "#AppsGroup");
/*   71 */     return new UserDocumentAccessFilter(groups, accounts, isAdmin, appRights);
/*      */   }
/*      */ 
/*      */   public static Vector getUserGroupsWithPrivilege(UserData userData, int priv)
/*      */     throws ServiceException
/*      */   {
/*   81 */     Vector allGroups = new IdcVector();
/*   82 */     Hashtable groupsAccessible = new Hashtable();
/*      */ 
/*   84 */     determineGroupsAccessible(userData, priv, allGroups, groupsAccessible);
/*      */ 
/*   86 */     Vector retVal = new IdcVector();
/*   87 */     Iterator it = groupsAccessible.keySet().iterator();
/*   88 */     while (it.hasNext())
/*      */     {
/*   90 */       retVal.addElement(it.next());
/*      */     }
/*   92 */     return retVal;
/*      */   }
/*      */ 
/*      */   public static int determineGroupPrivilege(UserData userData, String group)
/*      */     throws DataException, ServiceException
/*      */   {
/*  101 */     RoleDefinitions roleDefs = (RoleDefinitions)SharedObjects.getTable("RoleDefinition");
/*      */ 
/*  104 */     boolean isTracingSecurity = (SystemUtils.m_verbose) && (SystemUtils.isActiveTrace("checkordetermineaccess"));
/*  105 */     if (isTracingSecurity)
/*      */     {
/*  107 */       Report.trace("checkordetermineaccess", "SecurityUtils.determineGroupPrivilege: roleDefs=" + roleDefs, null);
/*      */     }
/*      */ 
/*  110 */     Vector roleList = getRoleList(userData);
/*      */ 
/*  112 */     int nroles = roleList.size();
/*  113 */     int privilege = 0;
/*  114 */     for (int i = 0; i < nroles; ++i)
/*      */     {
/*  116 */       UserAttribInfo uai = (UserAttribInfo)roleList.elementAt(i);
/*  117 */       if (isTracingSecurity)
/*      */       {
/*  119 */         Report.trace("checkordetermineaccess", "SecurityUtils.determineGroupPrivilege: UserAttribInfo=" + uai, null);
/*      */       }
/*  121 */       if (roleDefs == null)
/*      */       {
/*  124 */         if (uai.m_attribName.equals("admin"))
/*      */         {
/*  126 */           return 8;
/*      */         }
/*      */ 
/*      */       }
/*      */       else {
/*  131 */         privilege |= roleDefs.getRolePrivilege(uai.m_attribName, group);
/*      */       }
/*      */     }
/*      */ 
/*  135 */     return privilege;
/*      */   }
/*      */ 
/*      */   public static void checkUserAttributes(UserData loggedInUser, UserData userData)
/*      */     throws ServiceException
/*      */   {
/*  141 */     boolean result = checkAttribute(loggedInUser, userData, "role");
/*  142 */     if (result)
/*      */     {
/*  144 */       result = checkAttribute(loggedInUser, userData, "account");
/*      */     }
/*      */ 
/*  147 */     if (result)
/*      */       return;
/*  149 */     String msg = LocaleUtils.encodeMessage("csUserEditUserPermissionDenied", null, userData.m_name);
/*      */ 
/*  151 */     throw new ServiceException(-1, msg);
/*      */   }
/*      */ 
/*      */   public static boolean checkAttribute(UserData loggedInUser, UserData userData, String attribType)
/*      */     throws ServiceException
/*      */   {
/*  158 */     Vector curAttribInfo = loggedInUser.getAttributes(attribType);
/*  159 */     Vector userAttribInfo = userData.getAttributes(attribType);
/*      */ 
/*  161 */     if ((userAttribInfo == null) || (userAttribInfo.size() == 0))
/*      */     {
/*  163 */       return true;
/*      */     }
/*  165 */     if (curAttribInfo == null)
/*      */     {
/*  167 */       return false;
/*      */     }
/*      */ 
/*  170 */     boolean isAccount = attribType.equals("account");
/*  171 */     boolean useAccounts = m_useAccounts;
/*  172 */     if ((isAccount == true) && (!useAccounts))
/*      */     {
/*  174 */       return true;
/*      */     }
/*      */ 
/*  177 */     int num = curAttribInfo.size();
/*  178 */     int numUserAttrib = userAttribInfo.size();
/*  179 */     for (int i = 0; i < numUserAttrib; ++i)
/*      */     {
/*  181 */       UserAttribInfo uai = (UserAttribInfo)userAttribInfo.elementAt(i);
/*  182 */       String name = uai.m_attribName;
/*  183 */       boolean isOk = false;
/*  184 */       if (isAccount)
/*      */       {
/*  186 */         isOk = isAccountAccessible(loggedInUser, name, uai.m_attribPrivilege);
/*      */       }
/*      */       else
/*      */       {
/*  190 */         for (int j = 0; j < num; ++j)
/*      */         {
/*  192 */           UserAttribInfo info = (UserAttribInfo)curAttribInfo.elementAt(j);
/*  193 */           if (!name.equals(info.m_attribName))
/*      */             continue;
/*  195 */           isOk = true;
/*  196 */           break;
/*      */         }
/*      */       }
/*      */ 
/*  200 */       if (!isOk)
/*      */       {
/*  202 */         return false;
/*      */       }
/*      */     }
/*  205 */     return true;
/*      */   }
/*      */ 
/*      */   public static boolean isUserOfRole(UserData userData, String role)
/*      */   {
/*  213 */     List roleList = getRoleList(userData);
/*  214 */     for (int i = 0; i < roleList.size(); ++i)
/*      */     {
/*  216 */       UserAttribInfo uai = (UserAttribInfo)roleList.get(i);
/*  217 */       if (uai.m_attribName.equalsIgnoreCase(role))
/*      */       {
/*  219 */         return true;
/*      */       }
/*      */     }
/*  222 */     return false;
/*      */   }
/*      */ 
/*      */   public static boolean isUserOfRoleWithPattern(UserData userData, String rolePattern)
/*      */   {
/*  230 */     List roleList = getRoleList(userData);
/*  231 */     for (int i = 0; i < roleList.size(); ++i)
/*      */     {
/*  233 */       UserAttribInfo uai = (UserAttribInfo)roleList.get(i);
/*  234 */       if (StringUtils.matchEx(uai.m_attribName, rolePattern, true, true))
/*      */       {
/*  236 */         return true;
/*      */       }
/*      */     }
/*  239 */     return false;
/*      */   }
/*      */ 
/*      */   public static Vector getRoleList(UserData userData)
/*      */   {
/*  245 */     Vector roleList = null;
/*  246 */     if ((userData != null) && (userData.m_hasAttributesLoaded == true))
/*      */     {
/*  248 */       roleList = userData.getAttributes("role");
/*      */     }
/*      */ 
/*  251 */     if (roleList == null)
/*      */     {
/*  253 */       roleList = new IdcVector();
/*  254 */       UserAttribInfo uai = new UserAttribInfo();
/*  255 */       uai.m_attribType = "role";
/*  256 */       uai.m_attribName = "guest";
/*  257 */       roleList.addElement(uai);
/*      */     }
/*      */ 
/*  260 */     return roleList;
/*      */   }
/*      */ 
/*      */   public static String getRolePackagedList(UserData userData, boolean isDelimited)
/*      */   {
/*  266 */     return getRolePackagedListEx(userData, isDelimited);
/*      */   }
/*      */ 
/*      */   public static String getRolePackagedList(UserData userData)
/*      */   {
/*  271 */     return getRolePackagedListEx(userData, false);
/*      */   }
/*      */ 
/*      */   public static String getRolePackagedListEx(UserData userData, boolean isDelimited)
/*      */   {
/*  276 */     Vector rolesInfo = getRoleList(userData);
/*      */ 
/*  278 */     if (rolesInfo == null)
/*      */     {
/*  280 */       return "";
/*      */     }
/*  282 */     String packageStr = "";
/*  283 */     for (int i = 0; i < rolesInfo.size(); ++i)
/*      */     {
/*  285 */       UserAttribInfo uai = (UserAttribInfo)rolesInfo.elementAt(i);
/*  286 */       if (packageStr.length() > 0)
/*      */       {
/*  288 */         packageStr = packageStr + ",";
/*      */       }
/*      */ 
/*  291 */       if (isDelimited)
/*      */       {
/*  293 */         packageStr = packageStr + ":" + uai.m_attribName + ":";
/*      */       }
/*      */       else
/*      */       {
/*  297 */         packageStr = packageStr + uai.m_attribName;
/*      */       }
/*      */     }
/*  300 */     return packageStr;
/*      */   }
/*      */ 
/*      */   public static UserData createDefaultAdminUserData()
/*      */   {
/*  305 */     return createDefaultAdminUserData("sysadmin");
/*      */   }
/*      */ 
/*      */   public static UserData createDefaultAdminUserData(String name)
/*      */   {
/*  310 */     if (name == null)
/*      */     {
/*  312 */       name = "sysadmin";
/*      */     }
/*      */ 
/*  315 */     UserData userData = UserUtils.createUserData(name);
/*  316 */     userData.checkCreateAttributes(false);
/*  317 */     userData.addAttribute("role", "admin", "15");
/*  318 */     userData.m_hasAttributesLoaded = true;
/*  319 */     userData.setProperty("isInternalSystemUser", "1");
/*  320 */     return userData;
/*      */   }
/*      */ 
/*      */   public static void determineGroupsAccessible(UserData userData, int desiredPriv, Vector allGroups, Hashtable groupsAccessible)
/*      */     throws ServiceException
/*      */   {
/*  331 */     RoleDefinitions roleDefs = (RoleDefinitions)SharedObjects.getTable("RoleDefinition");
/*      */ 
/*  333 */     Vector roleList = getRoleList(userData);
/*  334 */     Vector secGrps = SharedObjects.getOptList("securityGroups");
/*      */ 
/*  337 */     if (secGrps != null)
/*      */     {
/*  339 */       allGroups.addAll(secGrps);
/*      */     }
/*      */ 
/*  343 */     if (desiredPriv == 0)
/*      */     {
/*  345 */       desiredPriv = 15;
/*      */     }
/*      */ 
/*  348 */     int nroles = roleList.size();
/*  349 */     boolean firstTime = true;
/*      */ 
/*  351 */     if (nroles == 0)
/*      */     {
/*  353 */       throw new ServiceException("!apUnableToDetermineSecurityGroups");
/*      */     }
/*      */ 
/*  356 */     for (int i = 0; i < nroles; ++i)
/*      */     {
/*  358 */       UserAttribInfo uai = (UserAttribInfo)roleList.elementAt(i);
/*  359 */       Vector groups = roleDefs.getRoleGroups(uai.m_attribName);
/*  360 */       if (groups == null) {
/*      */         continue;
/*      */       }
/*      */ 
/*  364 */       int ngroups = groups.size();
/*      */ 
/*  366 */       for (int j = 0; j < ngroups; ++j)
/*      */       {
/*  368 */         RoleGroupData data = (RoleGroupData)groups.elementAt(j);
/*  369 */         if ((data.m_privilege & desiredPriv) != 0)
/*      */         {
/*  371 */           if ((!firstTime) && 
/*  374 */             (groupsAccessible.get(data.m_groupName) != null))
/*      */           {
/*      */             continue;
/*      */           }
/*      */ 
/*  379 */           groupsAccessible.put(data.m_groupName, data);
/*      */         }
/*  381 */         if ((secGrps != null) || (!firstTime))
/*      */           continue;
/*  383 */         allGroups.addElement(data.m_groupName);
/*      */       }
/*      */ 
/*  386 */       firstTime = false;
/*      */     }
/*      */   }
/*      */ 
/*      */   public static Vector getAccessibleAccounts(UserData userData, boolean addSpecialAccounts, int priv, ExecutionContext cxt)
/*      */     throws ServiceException
/*      */   {
/*  404 */     Vector v = new IdcVector();
/*      */ 
/*  406 */     String[] allowedAccounts = null;
/*      */ 
/*  409 */     if ((isUserOfRole(userData, "admin")) && (addSpecialAccounts))
/*      */     {
/*  411 */       allowedAccounts = new String[] { "#all" };
/*      */     }
/*      */     else
/*      */     {
/*  415 */       allowedAccounts = getPrivilegedAccounts(userData, priv, false);
/*      */     }
/*      */ 
/*  418 */     if (addSpecialAccounts)
/*      */     {
/*  420 */       Vector specialAccounts = new IdcVector();
/*  421 */       userData.addSpecialAccountsChoices(specialAccounts);
/*  422 */       addAccountsFiltered(userData, v, specialAccounts, allowedAccounts, true, true, cxt);
/*      */     }
/*      */ 
/*  425 */     Vector definedAccounts = SharedObjects.getOptList("docAccounts");
/*  426 */     addAccountsFiltered(userData, v, definedAccounts, allowedAccounts, false, true, cxt);
/*      */ 
/*  428 */     return v;
/*      */   }
/*      */ 
/*      */   public static String[] getPrivilegedAccounts(UserData userData, int priv, boolean optimize)
/*      */     throws ServiceException
/*      */   {
/*  434 */     List accounts = getUserAccountsWithPrivilege(userData, priv, optimize);
/*  435 */     return StringUtils.convertListToArray(accounts);
/*      */   }
/*      */ 
/*      */   public static Vector getUserAccountsWithPrivilege(UserData userData, int priv, boolean optimize)
/*      */   {
/*  440 */     Vector allowedAccountsData = userData.getAttributes("account");
/*  441 */     if (allowedAccountsData == null)
/*      */     {
/*  443 */       allowedAccountsData = new IdcVector();
/*  444 */       addDefaultAccounts(userData, allowedAccountsData);
/*      */     }
/*      */ 
/*  448 */     if (priv == 0)
/*      */     {
/*  450 */       priv = 15;
/*      */     }
/*      */ 
/*  456 */     int size = allowedAccountsData.size();
/*  457 */     Vector accounts = new IdcVector();
/*  458 */     for (int i = 0; i < size; ++i)
/*      */     {
/*  460 */       UserAttribInfo uai = (UserAttribInfo)allowedAccountsData.elementAt(i);
/*  461 */       if ((uai.m_attribPrivilege & priv) == 0) {
/*      */         continue;
/*      */       }
/*  464 */       String newacct = uai.m_attribName;
/*  465 */       boolean notNeeded = false;
/*      */ 
/*  467 */       if (optimize)
/*      */       {
/*  469 */         if (newacct.equals("#all"))
/*      */         {
/*  471 */           accounts.removeAllElements();
/*  472 */           accounts.addElement(newacct);
/*  473 */           break;
/*      */         }
/*      */ 
/*  476 */         int n = accounts.size();
/*  477 */         int newacctlen = newacct.length();
/*      */ 
/*  479 */         for (int j = 0; j < n; ++j)
/*      */         {
/*  481 */           String oldacct = (String)accounts.elementAt(j);
/*  482 */           int oldacctlen = oldacct.length();
/*  483 */           int shorterlen = oldacctlen;
/*  484 */           String shorterstr = oldacct;
/*  485 */           String longerstr = newacct;
/*  486 */           if (newacctlen < shorterlen)
/*      */           {
/*  488 */             shorterlen = newacctlen;
/*  489 */             shorterstr = newacct;
/*  490 */             longerstr = oldacct;
/*      */           }
/*      */ 
/*  493 */           String str = longerstr.substring(0, shorterlen);
/*  494 */           if (!str.equalsIgnoreCase(shorterstr))
/*      */             continue;
/*  496 */           if (oldacctlen != shorterlen)
/*      */           {
/*  500 */             accounts.removeElementAt(j);
/*  501 */             --j;
/*  502 */             --n;
/*      */           }
/*      */           else
/*      */           {
/*  508 */             notNeeded = true;
/*  509 */             break;
/*      */           }
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  515 */       if (notNeeded)
/*      */         continue;
/*  517 */       accounts.addElement(newacct);
/*      */     }
/*      */ 
/*  522 */     return accounts;
/*      */   }
/*      */ 
/*      */   public static String getAccountPackagedList(UserData userData)
/*      */   {
/*  529 */     return getAccountPackagedListEx(userData, false);
/*      */   }
/*      */ 
/*      */   public static String getAccountPackagedListEx(UserData userData, boolean optimize)
/*      */   {
/*  534 */     Vector accounts = getUserAccountsWithPrivilege(userData, 1, optimize);
/*      */ 
/*  537 */     if (accounts == null)
/*      */     {
/*  539 */       return "";
/*      */     }
/*      */ 
/*  542 */     String packageStr = StringUtils.createString(accounts, ',', '^');
/*  543 */     return packageStr;
/*      */   }
/*      */ 
/*      */   public static String getFullExportedAccountslist(UserData userData)
/*      */   {
/*  549 */     Vector accounts = userData.getAttributes("account");
/*  550 */     if (accounts == null)
/*      */     {
/*  552 */       accounts = new IdcVector();
/*  553 */       addDefaultAccounts(userData, accounts);
/*      */     }
/*      */ 
/*  556 */     int naccounts = accounts.size();
/*  557 */     StringBuffer output = new StringBuffer(200);
/*  558 */     for (int i = 0; i < naccounts; ++i)
/*      */     {
/*  560 */       if (i > 0)
/*      */       {
/*  562 */         output.append(",");
/*      */       }
/*  564 */       UserAttribInfo uai = (UserAttribInfo)accounts.elementAt(i);
/*  565 */       output.append(uai.m_attribName);
/*      */ 
/*  567 */       if (uai.m_attribPrivilege == 15)
/*      */         continue;
/*  569 */       output.append("(");
/*  570 */       output.append(SecurityAccessListUtils.makePrivilegeStr(uai.m_attribPrivilege));
/*  571 */       output.append(")");
/*      */     }
/*      */ 
/*  575 */     return output.toString();
/*      */   }
/*      */ 
/*      */   public static boolean isAccountAccessible(UserData userData, String account, int priv)
/*      */     throws ServiceException
/*      */   {
/*  583 */     return computeAccountPrivilege(userData, account, priv) != 0;
/*      */   }
/*      */ 
/*      */   public static int determineBestAccountPrivilege(UserData userData, String account)
/*      */     throws ServiceException
/*      */   {
/*  589 */     return computeAccountPrivilege(userData, account, 0);
/*      */   }
/*      */ 
/*      */   public static int computeAccountPrivilege(UserData userData, String account, int priv)
/*      */     throws ServiceException
/*      */   {
/*  605 */     int retPriv = 0;
/*  606 */     priv &= 15;
/*      */ 
/*  608 */     Vector allowedAccountsData = userData.getAttributes("account");
/*  609 */     if ((allowedAccountsData == null) || (allowedAccountsData.size() == 0))
/*      */     {
/*  611 */       allowedAccountsData = new IdcVector();
/*  612 */       addDefaultAccounts(userData, allowedAccountsData);
/*      */     }
/*      */ 
/*  615 */     if (account != null)
/*      */     {
/*  617 */       account = account.trim();
/*  618 */       if (account.length() == 0)
/*      */       {
/*  620 */         account = "#none";
/*      */       }
/*      */     }
/*      */ 
/*  624 */     int size = allowedAccountsData.size();
/*  625 */     int accountlen = 0;
/*  626 */     if (account != null)
/*      */     {
/*  628 */       accountlen = account.length();
/*      */     }
/*  630 */     for (int i = 0; i < size; ++i)
/*      */     {
/*  632 */       UserAttribInfo uai = (UserAttribInfo)allowedAccountsData.elementAt(i);
/*  633 */       if ((priv != 0) && ((uai.m_attribPrivilege & priv) == 0)) {
/*      */         continue;
/*      */       }
/*      */ 
/*  637 */       if (account == null)
/*      */       {
/*  639 */         if (priv != 0)
/*      */         {
/*  641 */           return priv;
/*      */         }
/*  643 */         retPriv |= uai.m_attribPrivilege;
/*      */       }
/*      */       else
/*      */       {
/*  648 */         String allowedacct = uai.m_attribName;
/*  649 */         if (allowedacct.equals("#all"))
/*      */         {
/*  651 */           if (priv != 0)
/*      */           {
/*  653 */             return priv;
/*      */           }
/*  655 */           retPriv |= uai.m_attribPrivilege;
/*      */         }
/*      */         else
/*      */         {
/*  659 */           int allowedacctlen = allowedacct.length();
/*  660 */           if (accountlen < allowedacctlen)
/*      */           {
/*      */             continue;
/*      */           }
/*      */ 
/*  665 */           String testStr = account.substring(0, allowedacctlen);
/*  666 */           if (!testStr.equalsIgnoreCase(allowedacct))
/*      */             continue;
/*  668 */           if (priv != 0)
/*      */           {
/*  670 */             return priv;
/*      */           }
/*  672 */           retPriv |= uai.m_attribPrivilege;
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/*  677 */     return retPriv;
/*      */   }
/*      */ 
/*      */   public static void addDefaultAccounts(UserData userData, Vector accounts)
/*      */   {
/*  683 */     String defAcct = null;
/*  684 */     if (isUserOfRole(userData, "admin"))
/*      */     {
/*  686 */       defAcct = "#all";
/*      */     }
/*      */     else
/*      */     {
/*  690 */       String defaultAccounts = SharedObjects.getEnvironmentValue("DefaultAccounts");
/*  691 */       if ((defaultAccounts != null) && (defaultAccounts.length() > 0))
/*      */       {
/*  710 */         UserData tempData = UserUtils.createUserData();
/*  711 */         tempData.checkCreateAttributes(false);
/*  712 */         Vector v = StringUtils.parseArray(defaultAccounts, ',', '^');
/*  713 */         loadExternalSecurityAttributes(tempData, "account", v, null, false);
/*  714 */         Vector curAccounts = tempData.getAttributes("account");
/*  715 */         if (curAccounts != null)
/*      */         {
/*  717 */           for (int i = 0; i < curAccounts.size(); ++i)
/*      */           {
/*  719 */             accounts.addElement(curAccounts.elementAt(i));
/*      */           }
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/*  725 */         defAcct = "#none";
/*      */       }
/*      */     }
/*  728 */     if (defAcct == null) {
/*      */       return;
/*      */     }
/*  731 */     UserAttribInfo uai = new UserAttribInfo();
/*  732 */     uai.m_attribType = "account";
/*  733 */     uai.m_attribPrivilege = 15;
/*  734 */     uai.m_attribName = defAcct;
/*  735 */     accounts.addElement(uai);
/*      */   }
/*      */ 
/*      */   public static void addAccountsFiltered(UserData userData, Vector presentationAccounts, Vector knownAccounts, String[] allowedAccounts, boolean isSpecial, boolean doTranslate, ExecutionContext cxt)
/*      */   {
/*  743 */     if (knownAccounts == null)
/*      */     {
/*  745 */       return;
/*      */     }
/*      */ 
/*  748 */     int size = knownAccounts.size();
/*  749 */     boolean showOnlyKnownAccounts = isSpecial;
/*  750 */     if (!isSpecial)
/*      */     {
/*  752 */       showOnlyKnownAccounts = SharedObjects.getEnvValueAsBoolean("ShowOnlyKnownAccounts", false);
/*      */     }
/*      */ 
/*  756 */     boolean isAll = false;
/*  757 */     for (int i = 0; i < allowedAccounts.length; ++i)
/*      */     {
/*  759 */       if (!allowedAccounts[i].equals("#all"))
/*      */         continue;
/*  761 */       isAll = true;
/*  762 */       break;
/*      */     }
/*      */ 
/*  769 */     allowedAccounts = SortUtils.sortCaseInsensitiveStringList(allowedAccounts, (String[][])null, true);
/*      */ 
/*  803 */     int i = 0;
/*  804 */     int j = 0;
/*  805 */     boolean prefixIsUsed = false;
/*  806 */     boolean knownIsUsed = false;
/*  807 */     boolean allowedIsUsed = false;
/*  808 */     String prefix = null;
/*  809 */     while ((i < size) && (j < allowedAccounts.length))
/*      */     {
/*  811 */       String knownAccount = (String)knownAccounts.elementAt(i);
/*  812 */       String allowedAccount = allowedAccounts[j];
/*      */ 
/*  814 */       if ((allowedAccount.startsWith("#")) && (!isSpecial))
/*      */       {
/*  817 */         ++j;
/*      */       }
/*      */ 
/*  823 */       boolean knownKeptForNextLoop = true;
/*      */ 
/*  826 */       boolean allowAddKnownAccount = true;
/*      */ 
/*  829 */       boolean addAllowedAccount = false;
/*      */ 
/*  832 */       String allowedL = allowedAccount.toLowerCase();
/*  833 */       String knownL = knownAccount.toLowerCase();
/*      */ 
/*  842 */       boolean allowedIsPrefix = knownL.startsWith(allowedL);
/*  843 */       if (prefixIsUsed)
/*      */       {
/*  845 */         prefixIsUsed = knownL.startsWith(prefix);
/*      */       }
/*  847 */       if (!prefixIsUsed)
/*      */       {
/*  849 */         prefix = allowedL;
/*  850 */         prefixIsUsed = allowedIsPrefix;
/*      */       }
/*      */ 
/*  855 */       boolean allowed = prefixIsUsed;
/*      */ 
/*  857 */       if (prefixIsUsed)
/*      */       {
/*  865 */         boolean useAllowedAccount = (allowedIsPrefix) && (j + 1 < allowedAccounts.length);
/*  866 */         if (!useAllowedAccount)
/*      */         {
/*  872 */           if (allowedIsPrefix)
/*      */           {
/*  874 */             allowedIsUsed = true;
/*      */           }
/*      */ 
/*  879 */           if ((!allowedIsUsed) && (knownL.compareTo(allowedL) > 0))
/*      */           {
/*  885 */             useAllowedAccount = true;
/*  886 */             addAllowedAccount = true;
/*      */           }
/*      */         }
/*  889 */         if (useAllowedAccount)
/*      */         {
/*  892 */           ++j;
/*  893 */           allowAddKnownAccount = false;
/*  894 */           allowedIsUsed = false;
/*      */         }
/*      */         else
/*      */         {
/*  899 */           ++i;
/*  900 */           knownKeptForNextLoop = false;
/*      */         }
/*      */ 
/*      */       }
/*      */       else
/*      */       {
/*  907 */         if (knownL.compareTo(allowedL) > 0)
/*      */         {
/*  909 */           ++j;
/*  910 */           if ((!allowedIsUsed) && (!showOnlyKnownAccounts))
/*      */           {
/*  912 */             addAllowedAccount = true;
/*      */           }
/*  914 */           allowedIsUsed = false;
/*  915 */           allowAddKnownAccount = false;
/*      */         }
/*      */         else
/*      */         {
/*  919 */           ++i;
/*  920 */           knownKeptForNextLoop = false;
/*      */         }
/*      */ 
/*  926 */         allowed = isAll;
/*      */       }
/*      */ 
/*  929 */       if (addAllowedAccount)
/*      */       {
/*  931 */         String str = allowedAccount;
/*  932 */         if (doTranslate)
/*      */         {
/*  934 */           str = userData.getAccountPresentationString(allowedAccount);
/*      */         }
/*  936 */         presentationAccounts.addElement(str);
/*      */       }
/*      */ 
/*  939 */       boolean addKnown = (allowed) && (allowAddKnownAccount);
/*  940 */       if ((addKnown) && (!knownIsUsed))
/*      */       {
/*  942 */         String str = knownAccount;
/*  943 */         if (doTranslate)
/*      */         {
/*  945 */           str = userData.getAccountPresentationString(knownAccount, cxt);
/*      */         }
/*  947 */         presentationAccounts.addElement(str);
/*      */       }
/*  949 */       knownIsUsed = (knownKeptForNextLoop) && (addKnown);
/*      */     }
/*      */ 
/*  952 */     if (knownIsUsed)
/*      */     {
/*  954 */       ++i;
/*      */     }
/*      */ 
/*  957 */     if (allowedIsUsed)
/*      */     {
/*  959 */       ++j;
/*      */     }
/*      */ 
/*  962 */     while (i < size)
/*      */     {
/*  964 */       String knownAccount = (String)knownAccounts.elementAt(i);
/*  965 */       String knownL = knownAccount.toLowerCase();
/*  966 */       if ((isAll) || ((prefixIsUsed) && (knownL.startsWith(prefix))))
/*      */       {
/*  968 */         String str = knownAccount;
/*  969 */         if (doTranslate)
/*      */         {
/*  971 */           str = userData.getAccountPresentationString(knownAccount, cxt);
/*      */         }
/*  973 */         presentationAccounts.addElement(str);
/*      */       }
/*  975 */       ++i;
/*      */     }
/*      */ 
/*  978 */     while ((!showOnlyKnownAccounts) && 
/*  980 */       (j < allowedAccounts.length))
/*      */     {
/*  982 */       String account = allowedAccounts[j];
/*  983 */       if (!account.startsWith("#"))
/*      */       {
/*  985 */         if (doTranslate)
/*      */         {
/*  987 */           account = userData.getAccountPresentationString(account, cxt);
/*      */         }
/*  989 */         presentationAccounts.addElement(account);
/*      */       }
/*  991 */       ++j;
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void loadExternalSecurityAttributes(UserData userData, String attribName, Vector attribs, RoleDefinitions roleDefs, boolean isRegisteredUser)
/*      */   {
/* 1000 */     int nattribs = attribs.size();
/* 1001 */     boolean isAddingRole = attribName.equals("role");
/* 1002 */     if ((isAddingRole) && (roleDefs == null) && (userData.m_name.equals("sysadmin")))
/*      */     {
/* 1005 */       userData.addAttribute(attribName, "admin", "15");
/* 1006 */       return;
/*      */     }
/*      */ 
/* 1009 */     for (int j = 0; j < nattribs; ++j)
/*      */     {
/* 1011 */       String attribVal = (String)attribs.elementAt(j);
/*      */ 
/* 1013 */       String securityFlagStr = "15";
/*      */ 
/* 1016 */       if (!isAddingRole)
/*      */       {
/* 1019 */         String[] attribInfo = parseSecurityFlags(attribVal, "15");
/* 1020 */         if (attribInfo[0] == null) {
/*      */           continue;
/*      */         }
/*      */ 
/* 1024 */         attribVal = attribInfo[0];
/* 1025 */         securityFlagStr = attribInfo[1];
/*      */       }
/*      */ 
/* 1028 */       boolean addAttribute = true;
/* 1029 */       if ((isAddingRole) && (!isRegisteredUser) && 
/* 1032 */         (roleDefs == null))
/*      */       {
/* 1034 */         if (attribVal.equals("sysmanager"))
/*      */         {
/* 1036 */           attribVal = "admin";
/*      */         }
/*      */         else
/*      */         {
/* 1040 */           addAttribute = false;
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/* 1045 */       if (!addAttribute)
/*      */         continue;
/* 1047 */       attribVal = attribVal.trim();
/* 1048 */       userData.addAttribute(attribName, attribVal, securityFlagStr);
/*      */     }
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static String[] parseSecurityFlags(String attribVal, String defFlag)
/*      */   {
/* 1059 */     return SecurityAccessListUtils.parseSecurityFlags(attribVal, defFlag);
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1064 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97977 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.SecurityUtils
 * JD-Core Version:    0.5.4
 */