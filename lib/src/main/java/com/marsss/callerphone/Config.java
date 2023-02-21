package com.marsss.callerphone;

public class Config {

    private String botToken = "";

    private String prefix = "";
    private String callerphoneNormal = "";
    private String callerphoneError = "";
    private String callerphoneCall = "";

    private String logStatusChannel = "";
    private String tempChatChannel = "";
    private String reportChatChannel = "";
    private String botInviteLink = "";
    private String supportServer = "";
    private String donateLink = "";
    private String privacyPolicy = "";
    private String termsOfService = "";
    private String ownerID = "";

    private StartType startUpType;

    public Config() {
    }

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

    public StartType getStartUpType() {
        return startUpType;
    }

    public void setStartUpType(StartType startUpType) {
        this.startUpType = startUpType;
    }

}
