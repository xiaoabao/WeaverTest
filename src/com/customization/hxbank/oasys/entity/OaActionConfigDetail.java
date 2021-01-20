package com.customization.hxbank.oasys.entity;

/**
 * Created by YeShengtao on 2020/12/2 9:50
 */
public class OaActionConfigDetail {

   private String dataname;
   private String fieldname;
   private String changeType;
   private String regex;
   private String regexGroup;
   private String handler;
   private String extraParam;

   public String getDataname() {
      return dataname;
   }

   public OaActionConfigDetail setDataname(String dataname) {
      this.dataname = dataname;
      return this;
   }

   public String getFieldname() {
      return fieldname;
   }

   public OaActionConfigDetail setFieldname(String fieldname) {
      this.fieldname = fieldname;
      return this;
   }

   public String getChangeType() {
      return changeType;
   }

   public OaActionConfigDetail setChangeType(String changeType) {
      this.changeType = changeType;
      return this;
   }

   public String getRegex() {
      return regex;
   }

   public OaActionConfigDetail setRegex(String regex) {
      this.regex = regex;
      return this;
   }

   public String getRegexGroup() {
      return regexGroup;
   }

   public OaActionConfigDetail setRegexGroup(String regexGroup) {
      this.regexGroup = regexGroup;
      return this;
   }

   public String getHandler() {
      return handler;
   }

   public OaActionConfigDetail setHandler(String handler) {
      this.handler = handler;
      return this;
   }

   public String getExtraParam() {
      return extraParam;
   }

   public OaActionConfigDetail setExtraParam(String extraParam) {
      this.extraParam = extraParam;
      return this;
   }
}
