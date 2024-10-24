package com.marsss.callerphone;

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

    private StartType startUpType;

    public Config() {}

    public String getBotToken() {
        return botToken;
    }

    public void setBotToken(String botToken) {
        this.botToken = botToken;
    }

    public boolean isValid() {
        return !botToken.isEmpty();
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getCallerphoneNormal() {
        return callerphoneNormal;
    }

    public void setCallerphoneNormal(String callerphoneNormal) {
        this.callerphoneNormal = callerphoneNormal;
    }

    public String getCallerphoneError() {
        return callerphoneError;
    }

    public void setCallerphoneError(String callerphoneError) {
        this.callerphoneError = callerphoneError;
    }

    public String getCallerphoneCall() {
        return callerphoneCall;
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
        return botListingTopGG;
    }

    public void setBotListingTopGG(String botListingTopGG) {
        this.botListingTopGG = botListingTopGG;
    }

    public String getUpvoteBotTopGG() {
        return upvoteBotTopGG;
    }

    public void setUpvoteBotTopGG(String upvoteBotTopGG) {
        this.upvoteBotTopGG = upvoteBotTopGG;
    }

    public String getBotListingDBL() {
        return botListingDBL;
    }

    public void setBotListingDBL(String botListingDBL) {
        this.botListingDBL = botListingDBL;
    }

    public String getUpvoteBotDBL() {
        return upvoteBotDBL;
    }

    public void setUpvoteBotDBL(String upvoteBotDBL) {
        this.upvoteBotDBL = upvoteBotDBL;
    }

    public String getUpvoteSupportServerTopGG() {
        return upvoteSupportServerTopGG;
    }

    public void setUpvoteSupportServerTopGG(String upvoteSupportServerTopGG) {
        this.upvoteSupportServerTopGG = upvoteSupportServerTopGG;
    }

    public String getUpvoteSupportServerDBL() {
        return upvoteSupportServerDBL;
    }

    public void setUpvoteSupportServerDBL(String upvoteSupportServerDBL) {
        this.upvoteSupportServerDBL = upvoteSupportServerDBL;
    }

    public String getLogStatusChannel() {
        return logStatusChannel;
    }

    public void setLogStatusChannel(String logStatusChannel) {
        this.logStatusChannel = logStatusChannel;
    }

    public String getTempChatChannel() {
        return tempChatChannel;
    }

    public void setTempChatChannel(String tempChatChannel) {
        this.tempChatChannel = tempChatChannel;
    }

    public String getReportChatChannel() {
        return reportChatChannel;
    }

    public void setReportChatChannel(String reportChatChannel) {
        this.reportChatChannel = reportChatChannel;
    }

    public String getBotInviteLink() {
        return botInviteLink;
    }

    public void setBotInviteLink(String botInviteLink) {
        this.botInviteLink = botInviteLink;
    }

    public String getSupportServer() {
        return supportServer;
    }

    public void setSupportServer(String supportServer) {
        this.supportServer = supportServer;
    }

    public String getDonateLink() {
        return donateLink;
    }

    public void setDonateLink(String donateLink) {
        this.donateLink = donateLink;
    }

    public String getPrivacyPolicy() {
        return privacyPolicy;
    }

    public void setPrivacyPolicy(String privacyPolicy) {
        this.privacyPolicy = privacyPolicy;
    }

    public String getTermsOfService() {
        return termsOfService;
    }

    public void setTermsOfService(String termsOfService) {
        this.termsOfService = termsOfService;
    }

    public String getOwnerID() {
        return ownerID;
    }

    public void setOwnerID(String ownerID) {
        this.ownerID = ownerID;
    }

    public String getDatabaseURL() {
        return databaseURL;
    }

    public void setDatabaseURL(String databaseURL) {
        this.databaseURL = databaseURL;
    }
}
