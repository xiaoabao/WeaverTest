package com.customization.hxbank.oasys.workflow.publicApi.impl;

import com.customization.hxbank.oasys.workflow.publicApi.WorkflowPA;
import com.customization.hxbank.oasys.workflow.entity.ApiWorkflowBaseInfo;
import com.customization.hxbank.oasys.workflow.entity.ApiWorkflowRequestInfo;
import com.engine.core.impl.Service;
import com.engine.workflow.biz.requestList.RequestListBiz;
import com.engine.workflow.util.CommonUtil;
import com.engine.workflow.util.WorkflowDimensionUtils;
import org.apache.commons.lang.StringUtils;
import weaver.conn.RecordSet;
import weaver.file.Prop;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.hrm.User;
import weaver.hrm.resource.ResourceComInfo;
import weaver.wechat.util.Utils;
import weaver.workflow.request.todo.OfsSettingObject;
import weaver.workflow.request.todo.RequestUtil;
import weaver.workflow.search.WfAdvanceSearchUtil;
import weaver.workflow.workflow.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static weaver.security.util.SecurityMethodUtil.clearKeywordFromConditon;

/**
 * @Description
 * @Author miao.zhang <yyem954135@163.com>
 * @Version V1.0.0
 * @Since 1.0
 * @Date 2020-10-10
 */
public class WorkflowPAImpl extends Service implements WorkflowPA {
    private String nullKeyword = "";
    private boolean isopenos = false;
    private boolean showdone = false;
    private String showOSSysName = "";
    private String oaSysName="";
    private String timeSql = "";

    public static final String IPADDRESS = Prop.getPropValue("EcologyIpAddressConfig", "IPADDRESS");
    public static final String WORKFLOWSSOURL = Prop.getPropValue("EcologyIpAddressConfig", "WORKFLOWSSOURL");
    private WorkflowDoingDimension workflowDoingDimension = null;

    public WorkflowPAImpl(){
        RequestUtil requestutil = new RequestUtil();
        OfsSettingObject ofso = requestutil.getOfsSetting();
        this.isopenos = ofso.getIsuse() == 1;// 是否开启异构系统
        this.showdone = "1".equals(ofso.getShowdone());// 是否显示异构系统已办数据
        this.showOSSysName = ofso.getShowsysname();// 是否显示异构系统全名称(1显示简称，2显示全称)
        if("1".equals(showOSSysName)) {            // oa 系统全称或简称
            this.oaSysName = ofso.getOashortname();
        } else if("2".equals(showOSSysName)){
            this.oaSysName = ofso.getOafullname();
        }
        this.timeSql = new RequestListBiz().getTimeSql(new RecordSet());
        this.nullKeyword = CommonUtil.getDBJudgeNullFun(new RecordSet().getDBType());
        try{
            this.workflowDoingDimension = new WorkflowDoingDimension();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public List <ApiWorkflowRequestInfo> getToDoRequestList(User user, String tabIds, Map<String, String> conditions, int pageNo, int pageSize, boolean isMergeShow, boolean isNeedOs) {
        if("".equals(tabIds)){
            tabIds = "0";
        }
        List<ApiWorkflowRequestInfo> requestlist = getRequestList(user,tabIds,conditions,pageNo,pageSize,isMergeShow,isNeedOs);
        return requestlist;
    }

    @Override
    public long getToDoRequestCount(User user, String tabIds, Map<String, String> conditions, boolean isMergeShow, boolean isNeedOs) {
        if("".equals(tabIds)){
            tabIds = "0";
        }
        return getRequestCount(user,tabIds,conditions,isMergeShow,isNeedOs);
    }

    //字段转换处理 length 长度 ，input 输入字符串
    private String splitAndFielterString(String input, int length) {
        if (input == null || input.trim().equals("")) {
            return "";
        }
        // 去掉所有html元素
        String str = input.replaceAll("\\&[a-zA-Z]{1,10};", "").replaceAll(
                "<[^>]*>", "");
        str = str.replaceAll("[(/>)<]", "");
        int len = str.length();
        if (len <= length) {
            return str;
        } else {
            str = str.substring(0, length);
            str += "......";
        }
        return str;
    }

    // 根据 获取流程数据
    @Override
    public List <ApiWorkflowRequestInfo> getRequestList(User user, String tabIds, Map<String, String> conditions, int pageNo, int pageSize, boolean isMergeShow, boolean isNeedOs) {
        String userIDAll = user.getUID()+"";
        String userType = "1".equals(user.getLogintype()) ? "0" : "1";
        if(isMergeShow){ // 主次账号统一显示
            String belongtoids = user.getBelongtoids();
            if(! "".equals(belongtoids) ){
                userIDAll = userIDAll + "," + belongtoids;
            }
        }
        // 开始构建基本查询条件
        String select = " select distinct ";
        String fields = " t1.createdate,t1.createtime,t1.creater,t1.currentnodeid,t1.currentnodetype,t1.lastoperator,t1.creatertype,t1.lastoperatortype,t1.lastoperatedate,t1.lastoperatetime,t1.requestid,t1.requestname,t1.requestlevel,t1.workflowid,'' as workflowname, t1.status, t2.viewtype,t2.receivedate,t2.receivetime,t2.userid, '' as sysname , -1 as workflowtypeid , ''  as workflowtypename,'' as pcurlsrc ";
        String from = " from workflow_requestbase t1,workflow_currentoperator t2,workflow_base t3";
        String sqlwhere = " where t1.requestid = t2.requestid and t2.islasttimes = 1 and t1.workflowid = t3.id";
        String where_os = " where islasttimes=1 and userid in("+userIDAll+") ";
        String where_safe = "";       // 用于处理不用进行安全处理的sql条件
        String where_os_safe = "";
        ArrayList<String> tabIdlist = Util.TokenizerString(tabIds, ",");
        ArrayList<String> tabIdtemp = new ArrayList<String>();
        StringBuilder where_temp= new StringBuilder();
        StringBuilder where_os_temp = new StringBuilder();
        // 拼接tab 条件
        for(String tabid:tabIdlist){
            if((!tabIdtemp.isEmpty() && tabIdtemp.contains(tabid)) || tabid.contains("-")){ // 不需要处理的tabid
                continue;
            }
            tabIdtemp.add(tabid);
            String scope = this.workflowDoingDimension.getScope(tabid);
            if("doing".equals(scope) || "emDoingApp".equals(scope)){    // 待办
                where_temp.append(WorkflowDimensionUtils.getToDoSqlWhere(tabid,"or",user," and (((t2.isremark='0' and (t2.takisremark is null or t2.takisremark=0 )) or t2.isremark in('1','5','8','9','7','11')) and t2.islasttimes=1) " , false));
                where_os_temp.append(WorkflowDimensionUtils.getOsSqlWhere(tabid,"or",user," and (isremark in('0','8','9')) ", true));
            }else if("done".equals(scope) || "emDoneApp".equals(scope)){ // 已办
                where_temp.append(WorkflowDimensionUtils.getToDoSqlWhere(tabid,"or",user," and (t2.isremark in('2','4') or (t2.isremark='0' and t2.takisremark =-2)) ", false));
                where_os_temp.append(WorkflowDimensionUtils.getOsSqlWhere(tabid,"or",user,showdone ? " isremark in ('2','4') " : " 1=2 " , true));
            }else if("mine".equals(scope) || "emMineApp".equals(scope)){ // 我的请求
                where_temp.append(WorkflowDimensionUtils.getToDoSqlWhere(tabid,"or",user," and t1.creater in ("+userIDAll+") and t1.creatertype = " + userType + " and t1.creater = t2.userid " , false));
                where_os_temp.append(WorkflowDimensionUtils.getOsSqlWhere(tabid,"or",user,showdone ? " and creatorid in (" + userIDAll + ") and creatorid=userid and islasttimes=1 " : " and creatorid in (" + userIDAll + ") and creatorid=userid and islasttimes=1 and isremark in(0,8,9) " , true));
            }else if("emFinApp".equals(scope)){  // em端办结
                where_temp.append(WorkflowDimensionUtils.getToDoSqlWhere(tabid,"or",user,"  and t2.iscomplete=1 and t1.currentnodetype = '3' ",false));
                where_os_temp.append(WorkflowDimensionUtils.getOsSqlWhere(tabid,"or",user,showdone ? " and isremark in(2,4) and iscomplete = 1 " : " and 1=2 ",true));
            }else if("emCopyApp".equals(scope)){ // em端抄送
                where_temp.append(WorkflowDimensionUtils.getToDoSqlWhere(tabid,"or",user," and t2.isremark in ('8','9') ",false));
                where_os_temp.append(WorkflowDimensionUtils.getOsSqlWhere(tabid,"or",user," and isremark in('8','9') ",true));
            }
            else{ // portal
                where_temp.append(WorkflowDimensionUtils.getToDoSqlWhere(tabid,"or",user, false));
                where_os_temp.append(WorkflowDimensionUtils.getOsSqlWhere(tabid,"or",user,!showdone ? " isremark not in ('2','4') " : "" , true));
            }
        }
        if(where_temp.length() > 0 ){
            where_safe += "  and ( 1=2 " + where_temp.toString()+ " )";
        }
        if(where_os_temp.length() > 0){
            where_os_safe += " and ( 1=2 ";
            if(isopenos){
                where_os_safe += where_os_temp.toString();
            }
            where_os_safe += " )";
        }

        // 处理自定义条件
        List<String> statusAndviewtypeSql = getStatusAndViewtypeSql(conditions); // 处理 流程状态、查看类型条件
        where_safe += statusAndviewtypeSql.get(0);
        where_os_safe += statusAndviewtypeSql.get(1);
        conditions.put("unophrmid","");  // 避免拦截
        List<String> sqlConditionList = getSqlcondition(conditions);              // 处理其他高级搜索条件
        String sql_condition = sqlConditionList.get(0);
        String sql_os_condition = sqlConditionList.get(1);

        where_safe += " and (t1.deleted<>1 or t1.deleted is null or t1.deleted='') ".replaceAll("t1.deleted","SpecialHandling"); // 特殊处理delete
        where_safe += " and (isprocessing = '' or isprocessing is null) ";
        where_safe += " and t2.usertype = "+ userType + " and t2.userid in(" + userIDAll + ") ";
        where_safe += " and ("+nullKeyword+"(t1.currentstatus,-1) = -1 or ("+nullKeyword+"(t1.currentstatus,-1)=0 and t1.creater in ("+ userIDAll + "))) ";

        sqlwhere += sql_condition;
        where_os += sql_os_condition.replaceAll("t1","").replaceAll("t2","");

        String sql = "";
        // 处理异构系统数据
        if(isNeedOs && isopenos){   // 异构流程显示
            String sysNameSql = "select (case when " + showOSSysName + " = 1 then sysshortname  when " + showOSSysName + " = 2 then sysfullname else '' end ) from ofs_sysinfo where ofs_sysinfo.sysid = ofs_todo_data.sysid";
            String osSql =  " select createdate,createtime,creatorid as creater,-1 as currentnodeid,'' as currentnodetype,-1 as lastoperator,0 as creatertype,0 as lastoperatortype," +
                    "'' as lastoperatedate,'' as lastoperatetime,requestid,requestname,0 as requestlevel,workflowid,workflowname, '' as status, viewtype as viewtype ,receivedate,receivetime, userid, ( " + sysNameSql + ") as sysname, sysid as workflowtypeid ,(select sysshortname from ofs_sysinfo where ofs_sysinfo.sysid = ofs_todo_data.sysid) as workflowtypename , pcurlsrc as pcurlsrc from ofs_todo_data " + where_os;
            sql = getPaginationRequestSql(select,fields,from,sqlwhere,osSql,where_safe,where_os_safe,pageNo,pageSize);
        }else{
            sql = getPaginationRequestSql(select,fields,from,sqlwhere,where_safe,where_os_safe,pageNo,pageSize);
        }
        // 根据得到的sql 查询 出结果
        //new BaseBean().writeLog("获取到的流程数据:" + JSONObject.toJSONString(workflowList));
        return getRequestListData(sql);
    }
    // 处理高级搜索中的 状态和查看类型条件 未操作者
    private List<String> getStatusAndViewtypeSql(Map<String, String> conditions) {
        List<String> statusAndViewtyepList = new ArrayList<>();
        StringBuilder where_safe = new StringBuilder();
        StringBuilder where_os_safe = new StringBuilder();
        String wfstatu = Util.null2String(conditions.get("wfstatu"));//流程状态：0表示无效，1表示有效，2表示全部
        int viewtype = Util.getIntValue(conditions.get("viewtype"), -1);//点击流程树上面的数字，查看类型，0：未读，1：反馈
        String unophrmid = Util.null2String(conditions.get("unophrmid")); // 未操作者
        String sql = "";
        if (!unophrmid.equals("")) { // 未操作者
            sql = "SELECT DISTINCT REQUESTID FROM WORKFLOW_CURRENTOPERATOR WHERE USERID='" + unophrmid + "' AND (isremark IN ('0','1','5','7','8','9','11') OR (isremark='4' AND viewtype=0))";
            where_safe.append(" AND  t1.requestid IN (").append(sql).append(")");
        }

        if ("0".equals(wfstatu)) {//无效
            where_safe.append( " and t1.workflowid in (select id from workflow_base where (isvalid in('0'))) ");
            where_os_safe.append( " and workflowid in (select workflowid from ofs_workflow where (cancel=1)) ");
        } else if("".equals(wfstatu) || "1".equals(wfstatu)){//有效
            where_safe.append(" and t1.workflowid in (select id from workflow_base where (isvalid in('1','3'))) ");
            where_os_safe.append(" and workflowid in (select workflowid from ofs_workflow where (cancel=0 or cancel is null))");
        } else if("2".equals(wfstatu)){//全部
            where_safe .append( " and t1.workflowid in (select id from workflow_base where (isvalid not in('2'))) ");
        }

        if(viewtype == 0){//未读
            where_safe.append(" and t2.viewtype=0 ");
            where_os_safe.append(" and viewtype = 0 ");
        }else if(viewtype == 1){//反馈
            where_safe.append(" and t2.needwfback=1 and (t2.viewtype = '-1' or (t1.lastFeedBackOperator <> t2.userid and t2.viewtype = '-2' and t1.lastFeedBackDate is not null and t1.lastFeedBackTime is not null and (("+timeSql+") or (t2.viewDate is null and t2.viewTime is null))) ");
            where_os_safe.append(" and 1=2 ");
        }
        statusAndViewtyepList.add(where_safe.toString());
        statusAndViewtyepList.add(where_os_safe.toString());
        return statusAndViewtyepList;
    }

    // 不显示异构数据
    private String getPaginationRequestSql(String select, String fields, String from, String sqlwhere, String where_safe, String where_os_safe, int pageNo, int pageSize) {
        return getPaginationRequestSql(select,fields,from,sqlwhere,"",where_safe,where_os_safe,pageNo,pageSize);
    }
    //获取处理后的sql
    private String getPaginationRequestSql(String select, String fields, String from, String sqlwhere, String osSql, String where_safe, String where_os_safe, int pageNo, int pageSize) {
        String execSql = "";
        select = Utils.null2String(select).replaceAll("(?i)(drop|insert|alter|truncate|delete|union|wait|xp_cmdshel|DBMS_PIPE|IIF|UTL_HTTP|SLEEP|net user|--|/\\*.*?\\*/)","__SQLINJECTION__");
        fields = clearKeywordFromConditon(fields);
        from = clearKeywordFromConditon(from);
        sqlwhere = clearKeywordFromConditon(sqlwhere);
        sqlwhere += where_safe;
        boolean isNeedOs = !"".equals(osSql);                              // 是否需要拼接异构sql
        BaseBean bb =new BaseBean();
        osSql += where_os_safe;
        // 定义排序规则
        String orderby = " order by receivedate desc,receivetime desc,requestid desc ";
        //String orderby1 = " order by receivedate asc,receivetime asc,requestid asc";
        //String orderby2 = " order by receivedate desc,receivetime desc,requestid desc ";

        RecordSet recordSet = new RecordSet();
        String dbType = recordSet.getDBType();
        int firstResult = 0;
        int endResult = 0;
        // 返回分页sql
        if("oracle".equals(dbType)){  // rownum
            firstResult = pageNo * pageSize + 1;
            endResult = (pageNo - 1) * pageSize;
            execSql = " select * from ( select tabUN2.*,rownum as my_rownum from ( select tableUN.*,rownum as r from ( " + select + " " + fields + " " + from + " " + sqlwhere +
                    (isNeedOs ? " SpecialHandUN "+osSql : "") + orderby +") tableUN " + ") tabUN2 where r < " + firstResult + "  )  where  my_rownum > " + endResult;
        }else if("mysql".equals(dbType)){  // limit
            firstResult =  (pageNo-1) * pageSize;
            endResult =  pageSize;
            if(pageNo == 1){
                execSql = " select * from (" + select + " " + fields + " " + from + " " + sqlwhere + (isNeedOs ? " SpecialHandUN " + osSql : "") + " ) tableUN " + orderby + " limit " + endResult;
            }else{
                execSql = "select * from ( select * from (" + select + " " + fields + " " + from + " " + sqlwhere + (isNeedOs ? " SpecialHandUN " + osSql : "")+
                        " ) tableUN " + orderby + " ) tabUN2 " +  " limit " + firstResult + "," + endResult;
            }
        }else {  // 使用 ROW_NUMBER OVER()分页
            firstResult = (pageNo -1) * pageSize + 1;
            endResult = pageNo * pageSize;
            execSql = " select * from ( select * ,ROW_NUMBER() OVER ("+ orderby + ") as rowid from ( " + select + " " + fields + " " + from + " " + sqlwhere+
                    (isNeedOs ? " SpecialHandUN " + osSql : "") + " )tableUN ) tableUN2 where rowid between " + firstResult + " and " + endResult;
        }
        bb.writeLog("execSql==>"+execSql);
        return execSql;
    }

    /**
     * 根据sql 获取 流程信息
     * @param sql sql
     * @return 流程 集合
     */
    private List<ApiWorkflowRequestInfo> getRequestListData(String sql) {
        List<ApiWorkflowRequestInfo> wri = new ArrayList<ApiWorkflowRequestInfo>();
        WorkflowNodeComInfo wnc = new WorkflowNodeComInfo();
        RecordSet recordSet = new RecordSet();
        RecordSet rs1 = new RecordSet();
        sql = Util.null2String(sql).replaceAll("(?i)(drop|insert|alter|truncate|delete|union|wait|xp_cmdshel|DBMS_PIPE|IIF|UTL_HTTP|SLEEP|net user|--|/\\*.*?\\*/)","__SQLINJECTION__");
        // 处理特殊字段 deleted union
        sql = sql.replace("SpecialHandling","t1.deleted").replace("SpecialHandUN","union all");
        //new BaseBean().writeLog(this.getClass().getName()+"--sql--" + sql);
        try{
            WorkflowComInfo workflowComInfo = new WorkflowComInfo();   //  流程信息缓存类
            WorkTypeComInfo workTypeComInfo = new WorkTypeComInfo();   //  流程类型信息缓存类
            ResourceComInfo resourceComInfo = new ResourceComInfo();   // 人力资源缓存类
            recordSet.executeQuery(sql);
            new BaseBean().writeLog(this.getClass().getName()+"此次执行的sql:" + sql);
            while(recordSet.next()){   // 获取流程数据
                ApiWorkflowRequestInfo workflowRequestBase = new ApiWorkflowRequestInfo();    // 临时用来存储流程数据
                workflowRequestBase.setViewtype(Util.null2String(recordSet.getString("viewtype")));
                String requestid=Util.null2String(recordSet.getString("requestid"));
                String pcurlsrc=Util.null2String(recordSet.getString("pcurlsrc"));
                workflowRequestBase.setRequestId(Util.null2String(requestid));
                if(StringUtils.isNotBlank(pcurlsrc)){
                    workflowRequestBase.setPcurl(pcurlsrc);
                }else{
                    workflowRequestBase.setPcurl(IPADDRESS+WORKFLOWSSOURL+requestid);
                }
                workflowRequestBase.setRequestName(Util.null2String(recordSet.getString("requestname")));
                workflowRequestBase.setRequestLevel(Util.null2String(recordSet.getString("requestlevel")));
                ApiWorkflowBaseInfo workflowBaseInfo = new ApiWorkflowBaseInfo();
                String os_workflowtypename = "";
                String os_sysname = "";
                workflowBaseInfo.setWorkflowId(Util.null2String(recordSet.getString("workflowid")));
                int workflowid = Util.getIntValue(recordSet.getString("workflowid"),0);
                if(workflowid < 0 ){ // 异构系统  workflowTypeId = sysid workflowTypename -- 根据后台设置来显示， 如果后台设置不显示系统名称，则workflowTypename为简称
                    os_sysname = recordSet.getString("sysname");
                    workflowRequestBase.setSysName(os_sysname);
                    workflowBaseInfo.setWorkflowName(Util.null2String(recordSet.getString("workflowname")));
                    if(!"".equals(os_sysname)){ // 异构系统类型名称根据 后台 设置系统名称 获取，若后台设置不显示异构系统名称，则取简称。
                        os_workflowtypename = os_sysname;
                    }else {
                        os_workflowtypename = Util.null2String(recordSet.getString("workflowtypename"));
                    }
                    workflowBaseInfo.setWorkflowTypeId(Util.null2String(recordSet.getString("workflowtypeid")));       // sysid 从查询取
                    workflowBaseInfo.setWorkflowTypeName(os_workflowtypename);
                }else {  // 从缓存中取
                    workflowBaseInfo.setWorkflowName(Util.null2String(workflowComInfo.getWorkflowname(workflowid + "")));
                    workflowBaseInfo.setFormId(Util.null2String(workflowComInfo.getFormId(workflowid+"")));
                    workflowRequestBase.setSysName(oaSysName);
                    String workflowTypeid = workflowComInfo.getWorkflowtype(workflowid + "");
                    workflowBaseInfo.setWorkflowTypeId(workflowTypeid);  // typeid typename 从缓存取
                    workflowBaseInfo.setWorkflowTypeName(workTypeComInfo.getWorkTypename(workflowTypeid));
                }
                workflowRequestBase.setWorkflowBaseInfo(workflowBaseInfo);
                String currentnodeid = "";
                currentnodeid = Util.null2String(recordSet.getString("currentnodeid"));
                workflowRequestBase.setCurrentNodeId(currentnodeid);
                if(-1 == Util.getIntValue(currentnodeid)){   // 异构系统
                    workflowRequestBase.setCurrentNodeName(Util.null2String(recordSet.getString("nodename")));
                }else {                                      // oa系统
                    workflowRequestBase.setCurrentNodeName(Util.null2String(wnc.getNodename(currentnodeid)));
                }
                workflowRequestBase.setStatus(Util.null2String(recordSet.getString("status")));
                String creatorid = Util.null2String(recordSet.getString("creater"));
                workflowRequestBase.setCreatorId(creatorid);
                workflowRequestBase.setCreatorName(resourceComInfo.getLastname(creatorid));
                workflowRequestBase.setCreateTime(Util.null2String(recordSet.getString("createdate") + " " + recordSet.getString("createtime")));
                String lastOperatorid = Util.null2String(recordSet.getString("lastoperator"));
                workflowRequestBase.setLastOperatorId(lastOperatorid);
                workflowRequestBase.setLastOperatorName(resourceComInfo.getLastname(lastOperatorid));
                workflowRequestBase.setLastOperateTime(Util.null2String(recordSet.getString("lastoperatedate")+ " " + recordSet.getString("lastoperatetime")));
                workflowRequestBase.setReceiveTime(Util.null2String(recordSet.getString("receivedate") +" " + recordSet.getString("receivetime")));
                wri.add(workflowRequestBase);
            }
        } catch (Exception e) {
            new BaseBean().writeLog(this.getClass().getName()+"获取数据时发生异常:"+e.getMessage());
            e.printStackTrace();
        }
        return wri;
    }

    // 获取高级查询条件  使用 WfAdvanceSearchutil 获取 高级搜索条件
    private List<String> getSqlcondition(Map<String, String> conditions) {
        List<String> sqlConditionList = new ArrayList<String>();
        // 使用WfAdvanceSearchUtil 来获取高级搜索条件
        WfAdvanceSearchUtil searchUtil = new WfAdvanceSearchUtil(conditions);
        String condition_sql = searchUtil.getAdVanceSearch4OtherCondition();
        String condition_os_sql = searchUtil.getAdVanceSearch4OtherConditionOs();
        // 增加对 workflowid 和workflowtype条件处理
        if(conditions.get("workflowIds")!=null && !"".equals(conditions.get("workflowIds"))){
            condition_sql += " and " + Util.getSubINClause(conditions.get("workflowIds"),"t1.workflowid","in");
            condition_os_sql += " and " + Util.getSubINClause(conditions.get("workflowIds"),"workflowid","in");
        }
        if(conditions.get("workflowTypes")!=null && !"".equals(conditions.get("workflowTypes"))){
            condition_sql += " and t3.workflowtype in ("+ conditions.get("workflowTypes") + ")";
            condition_os_sql += " and sysid in ("+ conditions.get("workflowTypes") + ")";
        }
        sqlConditionList.add(clearKeywordFromConditon(condition_sql));
        sqlConditionList.add(clearKeywordFromConditon(condition_os_sql));
        return sqlConditionList;
    }

    @Override
    public long getRequestCount(User user, String tabIds, Map<String, String> conditions, boolean isMergeShow, boolean isNeedOs) {
        String userIDAll = user.getUID()+"";
        String usertype = "1".equals(user.getLogintype()) ? "0" : "1";
        if(isMergeShow){//多账号统一显示
            String belongtoids = user.getBelongtoids();
            if (!"".equals(belongtoids))
                userIDAll += "," + belongtoids;
        }
        String select = " select distinct ";
        String fields = " t1.createdate,t1.createtime,t1.creater,t1.currentnodeid,t1.currentnodetype,t1.lastoperator,t1.creatertype,t1.lastoperatortype,t1.lastoperatedate,t1.lastoperatetime,t1.requestid,t1.requestname,t1.requestlevel,t1.workflowid,t2.receivedate,t2.receivetime, t2.userid";
        String from = " from workflow_requestbase t1,workflow_currentoperator t2,workflow_base t3 ";
        String where = " where t1.requestid=t2.requestid and t2.islasttimes = 1 and t1.workflowid = t3.id";
        String where_os = " where islasttimes=1 and userid in("+userIDAll+") ";
        String where_safe = "";//用于处理不需要进行安全处理的sql条件
        String where_safe_os = "";//用于处理不需要进行安全处理的sql条件

        //-----------------------------------处理指定tab的数据显示-----------------------------------
        List<String> tabIdList = Util.TokenizerString(tabIds,",");
        String where_tmp = "";
        String where_os_tmp = "";
        for(String tabId : tabIdList) {//自定义tab条件拼接
            String scope = workflowDoingDimension.getScope(tabId);//我的请求时需要处理creater条件
            if ("doing".equals(scope) || "emDoingApp".equals(scope)) {//全部的待办条件
                where_tmp += WorkflowDimensionUtils.getToDoSqlWhere(tabId, "or", user, " and (((t2.isremark='0' and (t2.takisremark is null or t2.takisremark=0 )) or t2.isremark in('1','5','8','9','7','11')) and t2.islasttimes=1) ", false);
                where_os_tmp += WorkflowDimensionUtils.getOsSqlWhere(tabId, "or", user, " and (isremark in('0','8','9')) ", true);
            } else if ("done".equals(scope) || "emDoneApp".equals(scope)) {
                where_tmp += WorkflowDimensionUtils.getToDoSqlWhere(tabId, "or", user, " and ((t2.isremark in('2','4') or (t2.isremark='0' and t2.takisremark =-2)) and t2.islasttimes=1) ", false);
                where_os_tmp += WorkflowDimensionUtils.getOsSqlWhere(tabId, "or", user, showdone ? " and (isremark in('2','4')) " : " and 1=2 ", true);
            } else if ("mine".equals(scope) || "emMineApp".equals(scope)) {
                where_tmp += WorkflowDimensionUtils.getToDoSqlWhere(tabId, "or", user, " and t1.creater in (" + userIDAll + ") and t1.creatertype = " + usertype + " and t1.creater = t2.userid ", false);
                where_os_tmp += WorkflowDimensionUtils.getOsSqlWhere(tabId, "or", user, showdone ? " and creatorid in (" + userIDAll + ") and creatorid=userid and islasttimes=1 " : " and creatorid in (" + userIDAll + ") and creatorid=userid and islasttimes=1 and isremark in(0,8,9) ", true);
            } else if("emFinApp".equals(scope)){  // em端办结
                where_tmp += WorkflowDimensionUtils.getToDoSqlWhere(tabId,"or",user,"  and t2.iscomplete=1 and t1.currentnodetype = '3' ",false);
                where_os_tmp += WorkflowDimensionUtils.getOsSqlWhere(tabId,"or",user,showdone ? " and isremark in(2,4) and iscomplete = 1 " : " and 1=2 ",true);
            }else if("emCopyApp".equals(scope)){ // em端抄送
                where_tmp += WorkflowDimensionUtils.getToDoSqlWhere(tabId,"or",user," and t2.isremark in ('8','9') ",false);
                where_os_tmp += WorkflowDimensionUtils.getOsSqlWhere(tabId,"or",user," and isremark in('8','9') ",true);
            }
            else {
                where_tmp += WorkflowDimensionUtils.getToDoSqlWhere(tabId, "or", user ,false);
                where_os_tmp += WorkflowDimensionUtils.getOsSqlWhere(tabId, "or", user, showdone ? "" : " and isremark not in(2,4) ", true);
            }

        }
        if(!"".equals(where_tmp)){
            where_safe += " and (1=2 " + where_tmp + ") ";
        }
        if(!"".equals(where_os_tmp)){
            where_safe_os += " and (1=2 " + where_os_tmp + ") ";
            if(!isopenos){
                where_safe_os += " 1=2 ";
            }
        }
        //-----------------------------------处理指定tab的数据显示-----------------------------------

        //-----------------------------------处理自定义条件-----------------------------------
        String sqlCondition = "";
        String sqlCondition_os = "";

        List<String> statusAndviewtypeSql = getStatusAndViewtypeSql(conditions); // 处理 流程状态、查看类型条件
        where_safe += statusAndviewtypeSql.get(0);
        where_safe_os += statusAndviewtypeSql.get(1);
        conditions.put("unophrmid","");  // 避免拦截
        List<String> sqlConditionList = getSqlcondition(conditions);              // 处理其他高级搜索条件
        sqlCondition = sqlConditionList.get(0);
        sqlCondition_os= sqlConditionList.get(1);

        where_safe += " and (t1.deleted<>1 or t1.deleted is null or t1.deleted='') ".replaceAll("t1.deleted","SpecialHandling");//特殊处理deleted字段
        where_safe += " and (isprocessing = '' or isprocessing is null) ";
        where_safe += " and t2.usertype = " + usertype + " and t2.userid in( " + userIDAll + ") ";
        where_safe += " and ("+nullKeyword+"(t1.currentstatus,-1) = -1 or ("+nullKeyword+"(t1.currentstatus,-1)=0 and t1.creater in ("+ userIDAll + "))) ";

        //其余条件看下能否走WfAdvanceSearchUtil处理,每个条件意思，可传参数需要写好文档说明
        //处理condition条件
        /*if (conditions != null){
            for (int m=0;m<conditions.length;m++) {
                String condition = conditions[m];
                condition = clearKeywordFromConditon(condition);
                sqlCondition += (condition != null && !"".equals(condition)) ? " and " + condition : "";
            }
        }*/
        where += sqlCondition;
        where_os += sqlCondition_os.replaceAll("t2","").replaceAll("t1","");
        //-----------------------------------处理自定义条件-----------------------------------

        String sql = "";

        //-----------------------------------处理异构系统数据-----------------------------------
        if(isNeedOs && isopenos){
            String osSql = " select createdate,createtime,creatorid as creater,-1 as currentnodeid,'' as currentnodetype,-1 as lastoperator,0 as creatertype,0 as lastoperatortype," +
                    "'' as lastoperatedate,'' as lastoperatetime,requestid,requestname,0 as requestlevel,workflowid,receivedate,receivetime,userid from ofs_todo_data " + where_os;
            sql = getPaginationCountSql(select, fields, from, where,osSql,where_safe,where_safe_os);
        } else{
            sql = getPaginationCountSql(select, fields, from, where,where_safe,where_safe_os);
        }
        //-----------------------------------处理异构系统数据-----------------------------------
        return getWorkflowRequestCount(sql);
    }

    /**
     * 取得分页统计记录数的SQL
     */
    private String getPaginationCountSql(String select, String fields, String from, String where, String where_safe, String where_safe_os) {
        return getPaginationCountSql(select,fields,from,where,"",where_safe,where_safe_os);
    }

    /**
     * 取得分页统计记录数的SQL
     * @param select sql中select
     * @param fields sql中fields
     * @param from sql中from
     * @param where sql中where
     * @param where_safe 不需要进行安全校验的sql
     * @param where_safe_os 不需要进行安全校验的sql_os
     * @return String sql分页统计记录数语句
     */
    private String getPaginationCountSql(String select, String fields, String from, String where,String osSql, String where_safe, String where_safe_os) {
        select = Util.null2String(select).replaceAll("(?i)(drop|insert|alter|truncate|delete|union|wait|xp_cmdshel|DBMS_PIPE|IIF|UTL_HTTP|SLEEP|net user|--|/\\*.*?\\*/)", "__SQLINJECTION__");
        fields = clearKeywordFromConditon(fields);
        from = clearKeywordFromConditon(from);
        where = clearKeywordFromConditon(where);
        where += where_safe;
        boolean isNeedOs = !"".equals(osSql);
        osSql += where_safe_os;
        String sql = " select count(*) my_count from ( " + select + " " + fields + " " + from + " " + where + (isNeedOs ? (" SpecialHandUN " + osSql) : "") + " ) tableA ";
        return sql;
    }

    /**
     * 取得sql中的记录数
     * @param sql sql语句
     * @return int 记录数
     */
    private int getWorkflowRequestCount(String sql) {
        RecordSet rs = new RecordSet();
        sql = Util.null2String(sql).replaceAll("(?i)(drop|insert|alter|truncate|delete|union|wait|xp_cmdshel|DBMS_PIPE|IIF|UTL_HTTP|SLEEP|net user|--|/\\*.*?\\*/)", "__SQLINJECTION__");
        //特殊处理deleted字段
        sql = sql.replace("SpecialHandling","t1.deleted").replace("SpecialHandUN","union");
        int count = 0;
        try {
            rs.executeSql(sql);
            if (rs.next()) {
                count = rs.getInt("my_count");
            }
        } catch (Exception e) {
            new BaseBean().writeLog(e);
            e.printStackTrace();
        }
        return count;
    }
}
