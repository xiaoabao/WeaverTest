package com.customization.hxbank.oasys.cmd;

import com.api.workplan.util.TimeZoneCastUtil;
import com.engine.common.biz.AbstractCommonCommand;
import com.engine.common.biz.SimpleBizLogger;
import com.engine.common.entity.BizLogContext;
import com.engine.core.interceptor.CommandContext;
import com.engine.hrm.biz.HrmClassifiedProtectionBiz;
import com.engine.workplan.util.WorkPlanUtil;
import org.apache.commons.lang.time.DateFormatUtils;
import weaver.WorkPlan.MutilUserUtil;
import weaver.WorkPlan.WorkPlanShareUtil;
import weaver.WorkPlan.repeat.util.RuleUtil;
import weaver.conn.ConnectionPool;
import weaver.conn.RecordSet;
import weaver.dateformat.DateTransformer;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.hrm.User;
import weaver.systeminfo.SystemEnv;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Description
 * @Author miao.zhang <yyem954135@163.com>
 * @Version V1.0.0
 * @Since 1.0
 * @Date 2020-10-13
 */
public class CalendarCmd extends AbstractCommonCommand <Map <String, Object>> {

    private SimpleBizLogger logger;
    private BizLogContext bizLogContext;
    /**
     * @param user
     * @param params
     */
    public CalendarCmd(User user, Map<String, Object> params) {
        this.user = user;
        this.params = params;
        this.logger = new SimpleBizLogger();
        this.bizLogContext=new BizLogContext();
    }
    /**
     * @return
     */
    @Override
    public BizLogContext getLogContext() {
        return null;
    }

    /**
     * @param commandContext
     * @return
     */
    @Override
    public Map<String, Object> execute(CommandContext commandContext) {

        String selectUser = Util.null2String(params.get("selectUser")); //被选择用户Id
        String selectDateString = Util.null2String(params.get("selectDate")); //被选择日期
        int timeSag = Util.getIntValue(Util.null2String(params.get("timeSag")), 0);
        String workPlanType = Util.null2String(params.get("workPlanType")); //日程类型
        int isShare = Util.getIntValue((String) params.get("isShare"),0); //0 :我的日程 1：所有日程
        int viewType = Util.getIntValue((String) params.get("viewType"),0); //日/周/月
        //int viewType =3;
        String wsBeginDate = Util.null2String(params.get("beginDate")); //被选择用户Id
        String wsEndDate = Util.null2String(params.get("endDate")); //被选择日期
        String from = "";
        if(params.containsKey("from")){
            from = (String)params.get("from");
        }
        Map result = new HashMap();
        RecordSet recordSet = new RecordSet();
        Map workPlanRemind=new HashMap();
        recordSet.execute("SELECT * FROM workplan_remind_type");
        while(recordSet.next()){
            workPlanRemind.put(recordSet.getString("id"),recordSet.getString("label").equals("")?recordSet.getString("name")
                    : SystemEnv.getHtmlLabelName(recordSet.getInt("label"),user.getLanguage()));
        }
        /*
        List workPanTypeList=new ArrayList();
        recordSet.execute("SELECT * FROM WorkPlanType WHERE available = '1' ORDER BY displayOrder ASC");
        while(recordSet.next()){
            Map item=new HashMap();
            item.put("id",recordSet.getString("workPlanTypeID"));
            item.put("name",recordSet.getString("workPlanTypeName"));
            workPanTypeList.add(item);
        }
        */
        int timeRangeStart=0;
        int timeRangeEnd=23;
        int weekStartDay = 0;
        recordSet.execute("select * from WorkPlanSet order by id");
        if(recordSet.next()){
            timeRangeStart	= Util.getIntValue(recordSet.getString("timeRangeStart"), 0);
            timeRangeEnd	= Util.getIntValue(recordSet.getString("timeRangeEnd"), 23);
            weekStartDay = Util.getIntValue(recordSet.getString("weekStartDay"), 0);
        }
        String sTime=(timeRangeStart<10?"0"+timeRangeStart:timeRangeStart)+":00";
        String eTime=(timeRangeEnd<10?"0"+timeRangeEnd:timeRangeEnd)+":59";
//        if(ismobile){//移动端 目前固定为周日开始显示
//            weekStartDay=0;
//        }

        //参数传递
        String userId = String.valueOf(user.getUID()); //当前用户Id
        String userType = user.getLogintype(); //当前用户类型
        boolean appendselectUser = false;
        if ("".equals(selectUser) || userId.equals(selectUser)) {
            appendselectUser = true;
            selectUser = userId;
        }
        boolean belongshow= MutilUserUtil.isShowBelongto(user);
        String belongids="";
        if(belongshow){
            belongids=User.getBelongtoidsByUserId(user.getUID());
        }
        selectUser = selectUser.replaceAll(",", "");
        String beginDate = "";
        String endDate = "";
        if(timeSag != 6 && timeSag >0 ){
            beginDate = TimeZoneCastUtil.getDateByOption(""+timeSag,"0");
            endDate = TimeZoneCastUtil.getDateByOption(""+timeSag,"1");
        }else if(timeSag == 6 ){
            beginDate = wsBeginDate;
            endDate = wsEndDate;
        }else if(timeSag == 0 ){
            Map dateMap = WorkPlanUtil.getCalendarTimeRange(selectDateString, viewType, weekStartDay, user.getUID());
            beginDate = Util.null2String(dateMap.get("begindate"));
            endDate =  Util.null2String(dateMap.get("enddate"));
        }
        /* ----------新增日期转换 start ----------------*/
        String changeToB[] =  TimeZoneCastUtil.FormatDateServer(beginDate,0);
        String changeToE[] = TimeZoneCastUtil.FormatDateServer(endDate,1);
        String beginDateC = changeToB[0];
        String beginTimeC = changeToB[1];
        String endDateC = changeToE[0];
        String endTimeC = changeToE[1];
        /* ----------新增日期转换 end ----------------*/
        String overColor = "";
        String archiveColor = "";
        String archiveAvailable = "0";
        String overAvailable = "0";
        String oversql = "select * from overworkplan order by workplanname desc";
        recordSet.execute(oversql);
        while (recordSet.next()) {
            String id = recordSet.getString("id");
            String workplancolor = recordSet.getString("workplancolor");
            String wavailable = recordSet.getString("wavailable");
            if ("1".equals(id)) {
                overColor = workplancolor;
                if ("1".equals(wavailable))
                    overAvailable = "1";
            } else {
                archiveColor = workplancolor;
                if ("1".equals(wavailable))
                    archiveAvailable = "2";
            }
        }
        if ("".equals(overColor)) {
            overColor = "#c3c3c2";
        }
        if ("".equals(archiveColor)) {
            archiveColor = "#937a47";
        }
        StringBuffer sqlStringBuffer = new StringBuffer();

        sqlStringBuffer
                .append("SELECT C.*,overworkplan.workplancolor FROM (SELECT * FROM ");
        sqlStringBuffer.append("(");
        sqlStringBuffer
                .append("SELECT workPlan.*, workPlanType.workPlanTypeColor, workPlanType.workPlanTypeName");
        sqlStringBuffer
                .append(" FROM WorkPlan workPlan, WorkPlanType workPlanType");
        //显示所有日程，包含已结束日程
        sqlStringBuffer.append(" WHERE ( workPlan.status = 0 ");
        //如果启用显示已完成日程,那么还需要判断当前人员是否已经点击了完成
        if("1".equals(overAvailable)){
            //sqlStringBuffer.append(" workPlan.status = 0 ");
            sqlStringBuffer.append(" or workPlan.status = 1 ");
        }/*else{
            sqlStringBuffer.append(" ( workPlan.status = 0 and not exists(select 1 from workplanFinish wpf where wpf.workplanid = workPlan.id and hasFinish=1 and userid="+user.getUID()+") )");
        }*/
        if("2".equals(archiveAvailable)){
            sqlStringBuffer.append(" or workPlan.status = 2 ");
        }
        sqlStringBuffer.append(" ) ");
        /** Add By Hqf for TD9970 Start **/
        sqlStringBuffer.append(" AND workPlan.deleted <> 1");
        if(!"".equals(workPlanType)){
            sqlStringBuffer.append(" AND workPlan.type_n in ("+workPlanType+")");
        }
        /** Add By Hqf for TD9970 End **/
        sqlStringBuffer
                .append(" AND workPlan.type_n = workPlanType.workPlanTypeId");
        sqlStringBuffer.append(" AND workPlan.createrType = '" + userType
                + "'");

        if (1!=isShare) {
            sqlStringBuffer.append(" AND (");
            if(appendselectUser&&!"".equals(belongids)){//自己
                sqlStringBuffer.append("(");
                sqlStringBuffer
                        .append(getHrmLikeSql("workPlan.resourceID",selectUser, recordSet));
                StringTokenizer idsst = new StringTokenizer(belongids, ",");
                while (idsst.hasMoreTokens()) {
                    String id = idsst.nextToken();
                    sqlStringBuffer
                            .append(" OR ");
                    sqlStringBuffer
                            .append(getHrmLikeSql("workPlan.resourceID",id, recordSet));
                }
                sqlStringBuffer.append(")");
            }else{
                sqlStringBuffer.append(getHrmLikeSql("workPlan.resourceID",selectUser, recordSet));
            }
            sqlStringBuffer.append(" )");
        } else {
            if (!appendselectUser) {
                sqlStringBuffer.append(" AND (");
                StringTokenizer idsst = new StringTokenizer(selectUser, ",");
                if(recordSet.getDBType().equalsIgnoreCase("oracle")){
                    sqlStringBuffer.append("gs".equalsIgnoreCase(recordSet.getOrgindbtype()) || "jc".equalsIgnoreCase(recordSet.getOrgindbtype())?
                            "":" dbms_lob.").append(" instr(workPlan.resourceID ,'"+selectUser+"') >0");
                }else{
                    sqlStringBuffer.append(" workPlan.resourceID = '");
                    sqlStringBuffer.append(selectUser);
                    sqlStringBuffer.append("'");
                }
                while (idsst.hasMoreTokens()) {
                    String id = idsst.nextToken();
                    sqlStringBuffer.append(" OR ");
                    sqlStringBuffer.append(getHrmLikeSql("workPlan.resourceID",id, recordSet));
                }
                sqlStringBuffer.append(")");
            }

        }
        sqlStringBuffer.append(" AND ( ");
//        sqlStringBuffer.append("(workPlan.beginDate < '"+endDateC+"' or ( beginDate = '"+endDateC+"' and workPlan.beginTime <='"+endTimeC+"' )) ");
//        sqlStringBuffer.append("and (workPlan.endDate > '"+ beginDateC+"' or ( endDate = '"+ beginDateC+"' and workPlan.endtime >= '"+beginTimeC+"' ) ) ");
        sqlStringBuffer.append("(workPlan.beginDate <= '"+endDateC+"' ) ");
        sqlStringBuffer.append("and (workPlan.endDate >= '"+ beginDateC+"' ) ");
        sqlStringBuffer.append(" )");
        sqlStringBuffer.append(getSecretSql(user,"workPlan."));
        sqlStringBuffer.append(" ) A");
        if(isShare==1||(isShare!=1&&!appendselectUser)){//所有日程或我的日程选择其他人员时，需要判断权限
            String sql= WorkPlanShareUtil.getShareSql(user);
            sqlStringBuffer.append(" JOIN");
            sqlStringBuffer.append(" (");
            sqlStringBuffer.append(sql);
            sqlStringBuffer.append(" ) B");
            sqlStringBuffer.append(" ON A.id = B.workId) C");
            sqlStringBuffer.append(" LEFT JOIN overworkplan ON overworkplan.id=c.status ");
            sqlStringBuffer.append(" WHERE shareLevel >= 1");
        }else{//我的日程，选择自己时，没必要查权限表
            sqlStringBuffer.append(" ) C");
            sqlStringBuffer.append(" LEFT JOIN overworkplan ON overworkplan.id=c.status ");
        }
        sqlStringBuffer.append(" ORDER BY beginDate asc, beginTime ASC, C.id ASC");
        if(from.equals("webService")){
            result.put("sqlInfo",sqlStringBuffer.toString());
            return result;
        }
        recordSet.execute(sqlStringBuffer.toString());

        List eventslist = new ArrayList();
        List mobileEventslist = new ArrayList();
        //处理日历信息
        Set mobileCalendarInfoSet = new HashSet();
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm");
        SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        DateTransformer dt = new DateTransformer();
        while (recordSet.next()) {
            try {
                boolean isAllDay = false;
                //List event = new ArrayList();
                Map eventMap = new HashMap();
                //event.add(recordSet.getString("id"));
                eventMap.put("id",recordSet.getString("id"));

                changeToB = TimeZoneCastUtil.FormatDateLocal(recordSet.getString("begindate").trim()+ " " + ("".equals(recordSet.getString("begintime").trim())?sTime:recordSet.getString("begintime").trim()),0);
                String  rsBeginDate = changeToB[0];
                String rsBeginTime = changeToB[1];
                changeToE = TimeZoneCastUtil.FormatDateLocal(recordSet.getString("enddate").trim()+ " " + ("".equals(recordSet.getString("endtime").trim())?eTime:recordSet.getString("endtime").trim()),1);;
                String rsEndDate = changeToE[0];
                String rsEndTime = changeToE[1];
                //event.add(recordSet.getString("name"));
                eventMap.put("name",recordSet.getString("name"));

                Date startDate = format2.parse(changeToB[0] + " "+ changeToB[1]);
                Date endDate2 = new Date();

                //event.add(format.format(startDate));
                eventMap.put("startdate",format.format(startDate));


                //这个判断无意义，E8之前判断是为了给开始日期限定在日历范围中
//                if (format2.parse(beginDate + " 00:00").getTime()
//                        - startDate.getTime() > 0) {
//                }
                if (!"".equals(rsEndDate)) {
                    String endTime = rsEndTime;
                    if ("".equals(endTime.trim())) {
                        endTime = eTime;
                    }
                    endDate2 = format2.parse(rsEndDate
                            + " " + endTime);
                    if(rsEndDate.compareTo(rsBeginDate) > 0){
                        isAllDay = true;
                    } else {
                    }
                    if(endDate2.getTime() - startDate.getTime() < 0){
                        endDate2 = startDate;
                    }
                    //event.add(format.format(endDate2));
                    eventMap.put("enddate",format.format(endDate2));
                } else {
                    //endDate = "01/01/10000";
                    //event.add("01/01/10000 00:00");
                    eventMap.put("enddate","01/01/10000 00:00");
                    isAllDay = true;
                }

                //event.add("0");
                if (isAllDay) {
                    eventMap.put("isallday","1");
                    //event.add("1");//是不是全天
                } else {
                    eventMap.put("isallday","0");
                }
                eventMap.put("description",recordSet.getString("description"));
                eventMap.put("status",recordSet.getInt("status"));

                //event.add("0");//,0,1,0,-1,1,
//                if(recordSet.getInt("status")==0){
//
//                    event.add(recordSet.getString("workPlanTypeColor"));//颜色
//                }else{
//                    event.add(recordSet.getString("workplancolor"));//颜色
//                }
//                if ((recordSet.getInt("shareLevel")>1 || (isShare!=1&&appendselectUser)) && recordSet.getInt("status")==0) {
//                    event.add("1");//editable
//                } else {
//                    event.add("0");//editable
//                }
//                event.add("");
//                event.add("0");
//                event.add("");
                //event.add(Util.formatMultiLang(recordSet.getString("workPlanTypeName"),user.getLanguage()+""));
                eventMap.put("workplantypename",Util.formatMultiLang(recordSet.getString("workPlanTypeName"),user.getLanguage()+""));
                eventslist.add(eventMap);
//                if(ismobile && !isCalendar){
//                    eventMap.put("id",event.get(0));
//                    eventMap.put("planName",event.get(1));
//                    String beginDateTime = format2.format(startDate);
//                    String endDateTime = format2.format(endDate2);
//                    eventMap.put("beginDate",beginDateTime.substring(0,10));
////                    eventMap.put("beginTime",beginDateTime.substring(11,16));
//                    eventMap.put("endDate",endDateTime.substring(0,10));
////                    eventMap.put("endTime",endDateTime.substring(11,16));
//                    putShowTimeStr(beginDate,beginDateTime.substring(0,10),beginDateTime.substring(11,16),endDateTime.substring(0,10),endDateTime.substring(11,16),eventMap,isAllDay);
//                    eventMap.put("color",event.get(7));
//                    eventMap.put("urgentLevel",recordSet.getString("urgentLevel"));
//                    eventMap.put("remindBeforeEnd",recordSet.getString("remindBeforeEnd"));
//                    eventMap.put("remindBeforeStart",recordSet.getString("remindBeforeStart"));
//                    eventMap.put("remindDateBeforeEnd",recordSet.getString("remindDateBeforeEnd"));
//                    eventMap.put("remindDateBeforeStart",recordSet.getString("remindDateBeforeStart"));
//                    eventMap.put("remindTimeBeforeEnd",recordSet.getString("remindTimeBeforeEnd"));
//                    eventMap.put("remindTimeBeforeStart",recordSet.getString("remindTimeBeforeStart"));
//                    eventMap.put("remindTimesBeforeStart",recordSet.getString("remindTimesBeforeStart"));
//                    eventMap.put("remindTimesBeforeEnd",recordSet.getString("remindTimesBeforeEnd"));
//                    eventMap.put("remindType",recordSet.getString("remindType"));
//                    eventMap.put("workPlanTypeName",Util.formatMultiLang(recordSet.getString("workPlanTypeName"),user.getLanguage()+""));
//                    String remindTypeName = "";
//                    String remindType = recordSet.getString("remindType");
//                    if(!remindType.equals("")){
//                        String remindTypeArr[] = remindType.split(",");
//                        for (int i = 0; i < remindTypeArr.length; i++) {
//                            if(workPlanRemind.containsKey(remindTypeArr[i])){
//                                remindTypeName += remindTypeName.equals("")?workPlanRemind.get(remindTypeArr[i]):","+workPlanRemind.get(remindTypeArr[i]);
//                            }
//                        }
//                    }
//                    eventMap.put("remindTypeName",remindTypeName);
//                    mobileEventslist.add(eventMap);
//                }else if(ismobile && isCalendar){
//                    betweenDate(recordSet.getString("begindate"),recordSet.getString("enddate"),mobileCalendarInfoSet);
//                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        result.put("events", eventslist);
        //result.put("issort", ""+true);
        result.put("start", beginDate + " 00:00");
        result.put("end", endDate + " 23:59");
        //result.put("weekStartDay", weekStartDay);
        //result.put("error", null);
        return result;
    }

    /**
     * 获取日期范围类的周期日程
     * @param beginDate
     * @param endDate
     * @return
     */
    private List<Map<String,Object>> getModulDatas(String beginDate,String endDate){
        List<Map<String,Object>> list=new ArrayList<Map<String,Object>>();
        Map<String,Object> retMap;
        RecordSet rs =new RecordSet();
        RecordSet rs2 =new RecordSet();
        String ruleRegEx = "";
        String timeModul = "";
        String availableBeginDate = "";
        String beginTimeTemp = "";
        String endDateTemp = "";
        String endTimeTemp = "";
        String persistentType="";// 持续类型
        float persistentTimes = 1; // 日程持续长度
        //新规则&&不是立即触发&&当前用户&&未结束

        Calendar nowtCalendar = Calendar.getInstance(); //用于显示的日期
        String nowDate=Util.add0(nowtCalendar.get(Calendar.YEAR), 4)+"-"; //年
        nowDate += Util.add0(nowtCalendar.get(Calendar.MONTH) + 1, 2)+"-"; // 月
        nowDate += Util.add0(nowtCalendar.get(Calendar.DAY_OF_MONTH), 2); //日

        /* ----------新增日期转换 start ----------------*/
//        String changeToB[] =  TimeZoneCastUtil.FormatDateServer(beginDate,0);
//        String changeToE[] = TimeZoneCastUtil.FormatDateServer(endDate,1);
//        String beginDateC = changeToB[0];
//        String beginTimeC = changeToB[1];
//        String endDateC = changeToE[0];
//        String endTimeC = changeToE[1];
        //参数已经处理过多时区了，不用重复转换
        String changeToB[];
        String changeToE[] ;
        String beginDateC ;
        String beginTimeC;
        String endDateC ;
        String endTimeC ;
        /* ----------新增日期转换 end ----------------*/
        StringBuffer sb = new StringBuffer();
        sb.append(" ( availableBeginDate <= '"+endDate+"' ) ");
        sb.append(" and  ");
        sb.append(" (availableEndDate IS null OR availableEndDate = '' or availableEndDate >= '"+beginDate+"')");
        rs.executeQuery("select type.workPlanTypeColor,modul.* from HrmPerformancePlanModul modul left join WorkPlanType type on modul.workPlanTypeID=type.workPlanTypeID   where ruletag=1 and immediatetouch<>1 and createrid=? and createrType=? " +
                "and "+sb.toString(),user.getUID(),user.getLogintype());
        if(rs.getCounts()>0){//有记录

            //单独删除的日程
            Map<String,Set<String>> delDateSet=new HashMap<String,Set<String>>();
            String moduleIdTemp;
            Set<String> moduleSet=null;
            rs2.executeQuery("select moduleId,delDate from WorkplanModuleDelDate where creater=? and createrType=? and delDate>=? and  delDate<=? ",user.getUID(),user.getLogintype(),beginDate,endDate);
            while(rs2.next()){
                moduleIdTemp=rs2.getInt("moduleId")+"";
                if(delDateSet.containsKey(moduleIdTemp)){
                    moduleSet=delDateSet.get(moduleIdTemp);
                }else{
                    moduleSet=new LinkedHashSet<String>();
                    delDateSet.put(moduleIdTemp,moduleSet);
                }
                moduleSet.add(rs2.getString("delDate"));
            }

            while(rs.next()){
                //新的规则生成
                moduleIdTemp=rs.getInt("id")+"";
                ruleRegEx = rs.getString("ruleRegEx");
                timeModul = rs.getString("timeModul");
                availableBeginDate = rs.getString("availableBeginDate");

                if(!"".equals(ruleRegEx)){
                    String offset="";
                    do {

                        offset= RuleUtil.getNextOccurDate(timeModul, ruleRegEx, availableBeginDate, offset);

                        //过滤已经被删除的日程
                        if(!"".equals(offset)&&delDateSet.containsKey(moduleIdTemp)){
                            moduleSet=delDateSet.get(moduleIdTemp);
                            if(moduleSet.contains(offset)){
                                continue;
                            }
                        }

                        if(!"".equals(offset)){

                            beginTimeTemp=Util.null2String(rs.getString("workPlanCreateTime"),"00:00");

                            persistentType=Util.null2String(rs.getString("persistentType"),"1");// 持续类型
                            persistentTimes = Util.getFloatValue(rs.getString("persistentTimes"),1); // 日程持续长度

                            if ("1".equals(persistentType)) {// 天
                                endDateTemp = (String) (Util.processTimeBySecond(offset, beginTimeTemp, new Float(persistentTimes * 86400).intValue())).get(0); // 计划结束日期
                                endTimeTemp = (String) (Util.processTimeBySecond(offset, beginTimeTemp, new Float(persistentTimes * 86400).intValue())).get(1); // 计划结束时间
                            } else if ("2".equals(persistentType)) {// 小时
                                endDateTemp = (String) (Util.processTimeBySecond(offset, beginTimeTemp, new Float(persistentTimes * 3600).intValue())).get(0); // 计划结束日期
                                endTimeTemp = (String) (Util.processTimeBySecond(offset, beginTimeTemp, new Float(persistentTimes * 3600).intValue())).get(1); // 计划结束时间
                            } else if ("3".equals(persistentType)) {// 分钟
                                endDateTemp = (String) (Util.processTimeBySecond(offset, beginTimeTemp, new Float(persistentTimes * 60).intValue())).get(0); // 计划结束日期
                                endTimeTemp = (String) (Util.processTimeBySecond(offset, beginTimeTemp, new Float(persistentTimes * 60).intValue())).get(1); // 计划结束时间
                            }
                            endTimeTemp = endTimeTemp.substring(0, 5); // 取消秒，保留小时分钟

                            /* ----------新增日期转换 start ----------------*/
                            changeToB =  TimeZoneCastUtil.FormatDateLocal(offset +" "+beginTimeTemp,0);
                            changeToE = TimeZoneCastUtil.FormatDateLocal(endDateTemp +" "+endTimeTemp ,1);
                            beginDateC = changeToB[0];
                            beginTimeC = changeToB[1];
                            endDateC = changeToE[0];
                            endTimeC = changeToE[1];
                            /* ----------新增日期转换 end ----------------*/

                            //周期日程考虑跨天日程，不仅仅判断开始日期
                            //当天日程可能已经生成，也不显示
                            if("".equals(beginDateC)||"".equals(endDateC)||endDate.compareTo(beginDateC)<0||beginDate.compareTo(endDateC)>0||nowDate.equals(beginDateC)){
                                continue;
                            }
                            retMap=new HashMap<String,Object>();
                            retMap.put("id","R"+rs.getInt("id"));
                            retMap.put("name",rs.getString("name"));
                            retMap.put("beginDate",beginDateC);
                            retMap.put("beginTime",beginTimeC);
                            retMap.put("endDate",endDateC);
                            retMap.put("endTime",endTimeC);
                            retMap.put("urgentLevel",rs.getString("urgentLevel"));
                            retMap.put("remindBeforeEnd",rs.getString("remindBeforeEnd"));
                            retMap.put("remindBeforeStart",rs.getString("remindBeforeStart"));
                            retMap.put("remindDateBeforeEnd",rs.getString("remindDateBeforeEnd"));
                            retMap.put("remindDateBeforeStart",rs.getString("remindDateBeforeStart"));
                            retMap.put("remindTimeBeforeEnd",rs.getString("remindTimeBeforeEnd"));
                            retMap.put("remindTimeBeforeStart",rs.getString("remindTimeBeforeStart"));
                            retMap.put("remindTimesBeforeStart",rs.getString("remindTimesBeforeStart"));
                            retMap.put("remindTimesBeforeEnd",rs.getString("remindTimesBeforeEnd"));
                            retMap.put("remindType",rs.getString("remindType"));
                            retMap.put("workPlanTypeColor",rs.getString("workPlanTypeColor"));
                            list.add(retMap);

                        }
                    }while(!"".equals(offset)&&offset.compareTo(endDate)<=0);
                }
            }

        }
        return list;

    }



    /**
     * 记录有日程的日期
     * @param beginDate
     * @param endDate
     * @param set
     */
    public void betweenDate(String beginDate, String endDate,Set set){
        try{
            if(endDate.equals("")){
                endDate = beginDate;
            }
            if(beginDate.equals("") && endDate.equals("")){
                return;
            }
            if(beginDate.equals(endDate)){
                set.add(beginDate);
            }else{
                SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
                Date fDate=sdf.parse(beginDate);
                Date oDate=sdf.parse(endDate);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(fDate);
                int day1 = calendar.get(Calendar.DAY_OF_YEAR);
                calendar.setTime(oDate);
                int day2 = calendar.get(Calendar.DAY_OF_YEAR);
                for(int i = 0 ;i<day2-day1+1;i++){
                    calendar.setTime(fDate);
                    calendar.add(Calendar.DAY_OF_YEAR,i);
                    set.add(DateFormatUtils.format(calendar.getTime(),"yyyy-MM-dd"));
                }
            }
        }catch(Exception e){
            e.printStackTrace();
            new BaseBean().writeLog("会议手机日历信息转换日期失败,失败信息:"+e.getMessage());
        }
    }

    /**
     * 封装跨天日程的显示时间
     * @param searchDate
     * @param beginDate
     * @param beginTime
     * @param endDate
     * @param endTime
     * @param targetMap
     * @param isAllDay
     */
    private void putShowTimeStr(String searchDate,String beginDate,String beginTime,String endDate,String endTime,Map targetMap,boolean isAllDay){
        if(!isAllDay){
            targetMap.put("beginTime",beginTime);
            targetMap.put("endTime",endTime);
            return;
        }

        if(searchDate.equals(beginDate)){//跨天日程第一天
            targetMap.put("beginTime",beginTime);
            targetMap.put("endTime",endDate+" "+endTime);
        }else if(searchDate.compareTo(endDate)<0){//跨天日程中间部分
            targetMap.put("beginTime",SystemEnv.getHtmlLabelName(27641,user.getLanguage()));//全天
            targetMap.put("endTime",endDate+" "+endTime);

        }else if(searchDate.equals(endDate)){//跨天日程最后一天
            targetMap.put("beginTime","00:00");
            targetMap.put("endTime",endTime);

        }else {//异常
            targetMap.put("beginTime",beginTime);
            targetMap.put("endTime",endTime);
        }

    }


    public static String getHrmLikeSql(String column , String hrmMembers ,RecordSet rs){

        return "jc".equalsIgnoreCase(rs.getOrgindbtype()) || "gs".equalsIgnoreCase(rs.getOrgindbtype())?
                " instr(','||"+column+"||',',',"+hrmMembers+",',1,1) >0 ":
                getHrmLikeSql(column, hrmMembers, rs.getDBType());
    }

    public static String getHrmLikeSql(String column , String hrmMembers ,String dbType){
        String sql = "";
        if ("jc".equalsIgnoreCase(ConnectionPool.getInstance().getOrgindbtype()) || "gs".equalsIgnoreCase(ConnectionPool.getInstance().getOrgindbtype()))
            sql = " instr(','||"+column+"||',',',"+hrmMembers+",',1,1) >0 ";
        else if(dbType.equalsIgnoreCase("oracle")){
            sql = " dbms_lob.instr(','||"+column+"||',',',"+hrmMembers+",',1,1) >0 ";
        }else if(dbType.equalsIgnoreCase("sqlserver")){
            sql = " ','+"+column+"+',' LIKE '%,"+hrmMembers+",%' ";
        }else{
            sql = " concat(',',"+column+",',') LIKE '%,"+hrmMembers+",%' ";
        }
        return sql;
    }

    /**
     *
     * @param user 人员类
     * @param alias 表的别名
     * @return
     */
    public static String getSecretSql(User user ,String alias){
        String sql = "";
        //判断开启分级保护后是否可以查看会议(不论什么状态,只要人员级别不能查看资源级别就不能查看)
        if(HrmClassifiedProtectionBiz.isOpenClassification()){
            HrmClassifiedProtectionBiz hrmClassifiedProtectionBiz = new HrmClassifiedProtectionBiz();
            int maxSecretLevel = Util.getIntValue(hrmClassifiedProtectionBiz.getMaxResourceSecLevel(user),-1);
            sql += " and "+alias+"secretLevel >= " + maxSecretLevel+ " ";
        }
        return sql;
    }
}
