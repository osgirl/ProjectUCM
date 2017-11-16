/*     */ package intradoc.autosuggest.records;
/*     */ 
/*     */ import intradoc.autosuggest.AutoSuggestContext;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.shared.SecurityAccessListUtils;
/*     */ import java.io.Serializable;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class SecurityIdentifierInfo
/*     */   implements Serializable
/*     */ {
/*     */   private static final long serialVersionUID = -7502137651217440976L;
/*     */   public transient AutoSuggestContext m_context;
/*     */   public String m_identifier;
/*     */   public String m_securityGroupId;
/*     */   public String m_accountId;
/*     */   public String m_owner;
/*     */   public List<String> m_users;
/*     */   public List<String> m_groups;
/*     */   public List<String> m_roles;
/*     */   public Map<String, String> m_extraParameters;
/*     */ 
/*     */   public SecurityIdentifierInfo(AutoSuggestContext context)
/*     */   {
/*  49 */     this.m_context = context;
/*     */   }
/*     */ 
/*     */   public SecurityIdentifierInfo(SecurityIdentifierInfo inputInfo) {
/*  53 */     this.m_identifier = inputInfo.m_identifier;
/*  54 */     this.m_securityGroupId = inputInfo.m_securityGroupId;
/*  55 */     this.m_accountId = inputInfo.m_accountId;
/*  56 */     this.m_owner = inputInfo.m_owner;
/*  57 */     this.m_users = inputInfo.m_users;
/*  58 */     this.m_groups = inputInfo.m_groups;
/*  59 */     this.m_roles = inputInfo.m_roles;
/*  60 */     this.m_extraParameters = inputInfo.m_extraParameters;
/*     */   }
/*     */ 
/*     */   public void init(String identifier, String securityGroupId, String accountId, String owner, String users, String groups, String roles, Map<String, String> extraParameters) throws DataException {
/*  64 */     this.m_identifier = identifier;
/*  65 */     this.m_securityGroupId = securityGroupId;
/*  66 */     this.m_accountId = accountId;
/*  67 */     this.m_owner = owner;
/*  68 */     this.m_users = new ArrayList();
/*  69 */     if ((users != null) && (users.length() > 0))
/*     */     {
/*  71 */       DataResultSet userListRset = SecurityAccessListUtils.makeResultSetFromAccessListString(users);
/*  72 */       for (userListRset.first(); userListRset.isRowPresent(); userListRset.next())
/*     */       {
/*  74 */         String user = userListRset.getStringValueByName("id");
/*  75 */         this.m_users.add(user);
/*     */       }
/*     */     }
/*  78 */     this.m_groups = new ArrayList();
/*  79 */     if ((groups != null) && (groups.length() > 0))
/*     */     {
/*  81 */       DataResultSet groupsListRset = SecurityAccessListUtils.makeResultSetFromAccessListString(groups);
/*  82 */       for (groupsListRset.first(); groupsListRset.isRowPresent(); groupsListRset.next())
/*     */       {
/*  84 */         String group = groupsListRset.getStringValueByName("id");
/*  85 */         this.m_groups.add(group);
/*     */       }
/*     */     }
/*  88 */     this.m_roles = new ArrayList();
/*  89 */     if ((roles != null) && (roles.length() > 0))
/*     */     {
/*  91 */       DataResultSet rolesListRset = SecurityAccessListUtils.makeResultSetFromAccessListString(roles);
/*  92 */       for (rolesListRset.first(); rolesListRset.isRowPresent(); rolesListRset.next())
/*     */       {
/*  94 */         String role = rolesListRset.getStringValueByName("id");
/*  95 */         this.m_roles.add(role);
/*     */       }
/*     */     }
/*  98 */     this.m_extraParameters = extraParameters;
/*     */   }
/*     */ 
/*     */   public void init(String identifier, String securityGroupId, String accountId, String owner, List<String> users, List<String> groups, List<String> roles, Map<String, String> extraParameters) throws DataException {
/* 102 */     this.m_identifier = identifier;
/* 103 */     this.m_securityGroupId = securityGroupId;
/* 104 */     this.m_accountId = accountId;
/* 105 */     this.m_owner = owner;
/* 106 */     this.m_users = users;
/* 107 */     this.m_groups = groups;
/* 108 */     this.m_roles = roles;
/* 109 */     this.m_extraParameters = extraParameters;
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 114 */     IdcStringBuilder documentInfoBuilder = new IdcStringBuilder();
/* 115 */     documentInfoBuilder.append(" Identifier : " + this.m_identifier);
/* 116 */     documentInfoBuilder.append(" Security Group ID : " + this.m_securityGroupId);
/* 117 */     documentInfoBuilder.append(" Account ID : " + this.m_accountId);
/* 118 */     documentInfoBuilder.append(" Owner : " + this.m_owner);
/* 119 */     documentInfoBuilder.append(" Users ID : " + this.m_users);
/* 120 */     documentInfoBuilder.append(" Groups ID : " + this.m_groups);
/* 121 */     documentInfoBuilder.append(" Roles ID : " + this.m_roles);
/* 122 */     return documentInfoBuilder.toString();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg) {
/* 126 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99650 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.autosuggest.records.SecurityIdentifierInfo
 * JD-Core Version:    0.5.4
 */