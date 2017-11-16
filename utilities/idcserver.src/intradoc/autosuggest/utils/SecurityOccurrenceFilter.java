/*     */ package intradoc.autosuggest.utils;
/*     */ 
/*     */ import intradoc.autosuggest.AutoSuggestContext;
/*     */ import intradoc.autosuggest.indexer.AutoSuggestIndexHandler;
/*     */ import intradoc.autosuggest.records.OccurrenceInfo;
/*     */ import intradoc.autosuggest.records.SecurityIdentifierInfo;
/*     */ import intradoc.autosuggest.records.TermInfo;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.shared.AliasData;
/*     */ import intradoc.shared.SecurityUtils;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.UserData;
/*     */ import intradoc.shared.UserDocumentAccessFilter;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class SecurityOccurrenceFilter
/*     */   implements OccurrenceFilter
/*     */ {
/*     */   public AutoSuggestIndexHandler m_indexHandler;
/*     */   public UserData m_userData;
/*     */   public UserDocumentAccessFilter m_userDocumentAccessFilter;
/*     */   public Vector m_roles;
/*     */   public Vector m_aliases;
/*     */ 
/*     */   public void init(Map<String, Object> parameters)
/*     */     throws DataException, ServiceException
/*     */   {
/*  49 */     this.m_indexHandler = ((AutoSuggestIndexHandler)parameters.get("AutoSuggestIndexHandler"));
/*  50 */     this.m_userData = ((UserData)parameters.get("UserData"));
/*  51 */     this.m_userDocumentAccessFilter = SecurityUtils.getUserDocumentAccessFilter(this.m_userData, 1);
/*     */ 
/*  53 */     this.m_roles = SecurityUtils.getRoleList(this.m_userData);
/*     */ 
/*  55 */     this.m_aliases = new Vector();
/*  56 */     AliasData aliasData = (AliasData)SharedObjects.getTable("Alias");
/*  57 */     if (aliasData == null)
/*     */     {
/*  59 */       aliasData = new AliasData();
/*     */     }
/*  61 */     String[][] aliases = aliasData.getAliasesForUser(this.m_userData.m_name);
/*  62 */     if ((aliases == null) || (aliases.length <= 0))
/*     */       return;
/*  64 */     for (int aliasNo = 0; aliasNo < aliases.length; ++aliasNo)
/*     */     {
/*  66 */       this.m_aliases.add(aliases[aliasNo][0]);
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean validate(OccurrenceInfo occurrenceInfo)
/*     */     throws DataException
/*     */   {
/*  78 */     boolean enableAutoSuggestSecurity = SharedObjects.getEnvValueAsBoolean("EnableAutoSuggestSecurity", true);
/*  79 */     if (!enableAutoSuggestSecurity)
/*     */     {
/*  81 */       return true;
/*     */     }
/*  83 */     TermInfo termInfo = this.m_indexHandler.getTermInfo(occurrenceInfo);
/*     */ 
/*  85 */     if (!this.m_indexHandler.hasSecurityContext())
/*     */     {
/*  87 */       Report.trace("autosuggest", "Filter[SecurityOccurrenceFilter] No security context for the field. Returning True.", null);
/*  88 */       return true;
/*     */     }
/*  90 */     if (!termInfo.hasSecurity())
/*     */     {
/*  92 */       Report.trace("autosuggest", "Filter[SecurityOccurrenceFilter] No security for the term info. Returning True.", null);
/*  93 */       return true;
/*     */     }
/*  95 */     SecurityIdentifierInfo securityIdentifierInfo = this.m_indexHandler.getSecurityIdentifierInfo(occurrenceInfo);
/*  96 */     if (securityIdentifierInfo == null)
/*     */     {
/*  99 */       Report.trace("autosuggest", "Missing Identifier Info : Occurrence info " + occurrenceInfo.toString() + " did not find identifier " + termInfo.m_identifier, null);
/* 100 */       return false;
/*     */     }
/*     */ 
/* 105 */     if (SecurityUtils.isUserOfRole(this.m_userData, "admin"))
/*     */     {
/* 107 */       return true;
/*     */     }
/* 109 */     if (Report.m_verbose)
/*     */     {
/* 111 */       Report.trace("autosuggest", "Filter[SecurityOccurrenceFilter] Applying security on -- " + termInfo.toString(), null);
/* 112 */       Report.trace("autosuggest", "Filter[SecurityOccurrenceFilter] Security Identifier Info -- " + securityIdentifierInfo.toString(), null);
/*     */     }
/*     */ 
/* 120 */     boolean hasAccess = true;
/* 121 */     boolean useAccounts = SharedObjects.getEnvValueAsBoolean("UseAccounts", false);
/* 122 */     if (this.m_userDocumentAccessFilter != null)
/*     */     {
/* 124 */       String securityGroup = (securityIdentifierInfo.m_securityGroupId != null) ? securityIdentifierInfo.m_securityGroupId.toLowerCase() : null;
/* 125 */       String account = (securityIdentifierInfo.m_accountId != null) ? securityIdentifierInfo.m_accountId.toLowerCase() : null;
/*     */ 
/* 127 */       if (useAccounts == true)
/*     */       {
/* 129 */         hasAccess = this.m_userDocumentAccessFilter.checkAccess(securityGroup, account);
/*     */       }
/*     */       else
/*     */       {
/* 133 */         hasAccess = this.m_userDocumentAccessFilter.checkAccess(securityGroup);
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 138 */       if (Report.m_verbose)
/*     */       {
/* 140 */         Report.trace("autosuggest", "Filter[SecurityOccurrenceFilter : Group & Account] m_userDocumentAccessFilter is null", null);
/*     */       }
/* 142 */       hasAccess = false;
/*     */     }
/* 144 */     if (!hasAccess)
/*     */     {
/* 146 */       if (Report.m_verbose)
/*     */       {
/* 148 */         Report.trace("autosuggest", "Filter[SecurityOccurrenceFilter : Group & Account] Occurrence info " + occurrenceInfo.toString() + " returned from security check with - " + hasAccess, null);
/*     */       }
/* 150 */       return hasAccess;
/*     */     }
/*     */ 
/* 155 */     boolean useEntitySecurity = SharedObjects.getEnvValueAsBoolean("UseEntitySecurity", false);
/* 156 */     if ((useEntitySecurity == true) && 
/* 161 */       (AutoSuggestContext.m_specialAuthGroups != null) && (AutoSuggestContext.m_specialAuthGroups.contains(securityIdentifierInfo.m_securityGroupId.toLowerCase())))
/*     */     {
/* 164 */       if (Report.m_verbose)
/*     */       {
/* 166 */         Report.trace("autosuggest", "Filter[SecurityOccurrenceFilter : ACL] Applying ACL security " + occurrenceInfo.toString(), null);
/*     */       }
/* 168 */       boolean hasACLAccess = false;
/*     */ 
/* 172 */       if ((SecurityUtils.m_accessListPrivilegesGrantedWhenEmpty) && 
/* 174 */         (((securityIdentifierInfo.m_users == null) || (securityIdentifierInfo.m_users.size() == 0))) && (((securityIdentifierInfo.m_groups == null) || (securityIdentifierInfo.m_groups.size() == 0))) && ((
/* 174 */         (securityIdentifierInfo.m_roles == null) || (securityIdentifierInfo.m_roles.size() == 0))))
/*     */       {
/* 178 */         hasACLAccess = true;
/*     */       }
/*     */ 
/* 184 */       if ((!hasACLAccess) && (securityIdentifierInfo.m_users != null) && (securityIdentifierInfo.m_users.size() > 0))
/*     */       {
/* 186 */         hasACLAccess = (hasACLAccess) || (securityIdentifierInfo.m_users.contains(this.m_userData.m_name));
/*     */       }
/* 188 */       if ((!hasACLAccess) && (securityIdentifierInfo.m_groups != null) && (securityIdentifierInfo.m_groups.size() > 0))
/*     */       {
/* 190 */         boolean present = false;
/* 191 */         for (String group : securityIdentifierInfo.m_groups)
/*     */         {
/* 193 */           present = this.m_aliases.contains(group);
/* 194 */           if (present == true) {
/*     */             break;
/*     */           }
/*     */         }
/*     */ 
/* 199 */         hasACLAccess = (hasACLAccess) || (present);
/*     */       }
/*     */ 
/* 204 */       boolean useRoleSecurity = SharedObjects.getEnvValueAsBoolean("UseRoleSecurity", false);
/* 205 */       if ((useRoleSecurity == true) && 
/* 207 */         (!hasACLAccess) && (securityIdentifierInfo.m_roles != null) && (securityIdentifierInfo.m_roles.size() > 0))
/*     */       {
/* 209 */         boolean present = false;
/* 210 */         for (String role : securityIdentifierInfo.m_roles)
/*     */         {
/* 212 */           present = this.m_roles.contains(role);
/* 213 */           if (present == true) {
/*     */             break;
/*     */           }
/*     */         }
/*     */ 
/* 218 */         hasACLAccess = (hasACLAccess) || (present);
/*     */       }
/*     */ 
/* 221 */       hasAccess = (hasAccess) && (hasACLAccess);
/*     */     }
/*     */ 
/* 224 */     if (Report.m_verbose)
/*     */     {
/* 226 */       Report.trace("autosuggest", "Filter[SecurityOccurrenceFilter : Final] Occurrence info " + occurrenceInfo.toString() + " returned from security check with - " + hasAccess, null);
/*     */     }
/* 228 */     return hasAccess;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 233 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 104604 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.autosuggest.utils.SecurityOccurrenceFilter
 * JD-Core Version:    0.5.4
 */