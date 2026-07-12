package com.itsmarsss.callerphone;

/**
 * YAML-backed bot configuration. SnakeYAML populates fields via setters.
 */
public class Config {

    private String botToken = "";
    private String prefix = "";
    private String callerphoneNormal = "";
    private String callerphoneError = "";
    private String callerphoneCall = "";

    private String botListingTopGG = "";
    private String upvoteBotTopGG = "";
    private String botListingDBL = "";
    private String upvoteBotDBL = "";
    private String upvoteSupportServerTopGG = "";
    private String upvoteSupportServerDBL = "";

    private String logStatusChannel = "";
    private String tempChatChannel = "";
    private String reportChatChannel = "";
    private String botInviteLink = "";
    private String supportServer = "";
    private String donateLink = "";
    private String privacyPolicy = "";
    private String termsOfService = "";
    private String ownerID = "";
    private String databaseURL = "";
    /** Private channel for re-hosting Match photos (optional). */
    private String matchMediaChannel = "";
    /**
     * Discord Premium Apps SKU id for Callerphone Premium (optional).
     * When set, entitlement create/delete events grant/revoke Premium.
     */
    private String premiumSkuId = "";

    private StartType startUpType;

    public Config() {
    }

    public boolean isValid() {
        return isPresent(botToken) && isPresent(databaseURL);
    }

    private static boolean isPresent(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static String nz(String value) {
        return value != null ? value : "";
    }

    public String getBotToken() {
        return nz(botToken);
    }

    public void setBotToken(String botToken) {
        this.botToken = botToken;
    }

    public String getPrefix() {
        return nz(prefix);
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getCallerphoneNormal() {
        return nz(callerphoneNormal);
    }

    public void setCallerphoneNormal(String callerphoneNormal) {
        this.callerphoneNormal = callerphoneNormal;
    }

    public String getCallerphoneError() {
        return nz(callerphoneError);
    }

    public void setCallerphoneError(String callerphoneError) {
        this.callerphoneError = callerphoneError;
    }

    public String getCallerphoneCall() {
        return nz(callerphoneCall);
    }

    public void setCallerphoneCall(String callerphoneCall) {
        this.callerphoneCall = callerphoneCall;
    }

    public StartType getStartUpType() {
        return startUpType;
    }

    public void setStartUpType(StartType startUpType) {
        this.startUpType = startUpType;
    }

    public String getBotListingTopGG() {
        return nz(botListingTopGG);
    }

    public void setBotListingTopGG(String botListingTopGG) {
        this.botListingTopGG = botListingTopGG;
    }

    public String getUpvoteBotTopGG() {
        return nz(upvoteBotTopGG);
    }

    public void setUpvoteBotTopGG(String upvoteBotTopGG) {
        this.upvoteBotTopGG = upvoteBotTopGG;
    }

    public String getBotListingDBL() {
        return nz(botListingDBL);
    }

    public void setBotListingDBL(String botListingDBL) {
        this.botListingDBL = botListingDBL;
    }

    public String getUpvoteBotDBL() {
        return nz(upvoteBotDBL);
    }

    public void setUpvoteBotDBL(String upvoteBotDBL) {
        this.upvoteBotDBL = upvoteBotDBL;
    }

    public String getUpvoteSupportServerTopGG() {
        return nz(upvoteSupportServerTopGG);
    }

    public void setUpvoteSupportServerTopGG(String upvoteSupportServerTopGG) {
        this.upvoteSupportServerTopGG = upvoteSupportServerTopGG;
    }

    public String getUpvoteSupportServerDBL() {
        return nz(upvoteSupportServerDBL);
    }

    public void setUpvoteSupportServerDBL(String upvoteSupportServerDBL) {
        this.upvoteSupportServerDBL = upvoteSupportServerDBL;
    }

    public String getLogStatusChannel() {
        return nz(logStatusChannel);
    }

    public void setLogStatusChannel(String logStatusChannel) {
        this.logStatusChannel = logStatusChannel;
    }

    public String getTempChatChannel() {
        return nz(tempChatChannel);
    }

    public void setTempChatChannel(String tempChatChannel) {
        this.tempChatChannel = tempChatChannel;
    }

    public String getReportChatChannel() {
        return nz(reportChatChannel);
    }

    public void setReportChatChannel(String reportChatChannel) {
        this.reportChatChannel = reportChatChannel;
    }

    public String getBotInviteLink() {
        return nz(botInviteLink);
    }

    public void setBotInviteLink(String botInviteLink) {
        this.botInviteLink = botInviteLink;
    }

    public String getSupportServer() {
        return nz(supportServer);
    }

    public void setSupportServer(String supportServer) {
        this.supportServer = supportServer;
    }

    public String getDonateLink() {
        return nz(donateLink);
    }

    public void setDonateLink(String donateLink) {
        this.donateLink = donateLink;
    }

    public String getPrivacyPolicy() {
        return nz(privacyPolicy);
    }

    public void setPrivacyPolicy(String privacyPolicy) {
        this.privacyPolicy = privacyPolicy;
    }

    public String getTermsOfService() {
        return nz(termsOfService);
    }

    public void setTermsOfService(String termsOfService) {
        this.termsOfService = termsOfService;
    }

    public String getOwnerID() {
        return nz(ownerID);
    }

    public void setOwnerID(String ownerID) {
        this.ownerID = ownerID;
    }

    public String getDatabaseURL() {
        return nz(databaseURL);
    }

    public void setDatabaseURL(String databaseURL) {
        this.databaseURL = databaseURL;
    }

    public String getMatchMediaChannel() {
        return nz(matchMediaChannel);
    }

    public void setMatchMediaChannel(String matchMediaChannel) {
        this.matchMediaChannel = matchMediaChannel;
    }

    public String getPremiumSkuId() {
        return nz(premiumSkuId);
    }

    public void setPremiumSkuId(String premiumSkuId) {
        this.premiumSkuId = premiumSkuId;
    }
}
