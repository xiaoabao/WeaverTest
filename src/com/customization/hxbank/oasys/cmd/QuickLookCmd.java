package com.customization.hxbank.oasys.cmd;

import com.engine.common.biz.AbstractCommonCommand;
import com.api.doc.detail.service.DocDetailService;
import com.engine.common.entity.BizLogContext;
import com.engine.core.interceptor.CommandContext;
import com.weaver.qfengx.DateUtils;
import com.weaver.qfengx.StringUtils;
import weaver.conn.RecordSet;
import weaver.crm.Maint.CustomerInfoComInfo;
import weaver.file.Prop;
import weaver.general.TimeUtil;
import weaver.general.Util;
import weaver.hrm.User;
import weaver.hrm.resource.ResourceComInfo;
import weaver.share.ShareManager;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Description
 * @Author miao.zhang <yyem954135@163.com>
 * @Version V1.0.0
 * @Since 1.0
 * @Date 2020-09-27
 */
public class QuickLookCmd extends AbstractCommonCommand <Map<String,Object>> {

    public static final String IPADDRESS = Prop.getPropValue("EcologyIpAddressConfig", "IPADDRESS");
    public static final String DOCDETAILSSOURL = Prop.getPropValue("EcologyIpAddressConfig", "DOCDETAILSSOURL");

    public QuickLookCmd(User user, Map <String,Object> params) {
        this.user = user;
        this.params = params;
    }

    @Override
    public BizLogContext getLogContext() {
        return null;
    }

    @Override
    public Map <String, Object> execute(CommandContext commandContext) {
        Map<String, Object> apidatas = new HashMap <String, Object>();
        if (null == user){
            apidatas.put("hasRight", false);
            return apidatas;
        }
        String seccategorys = "";
        String createdateFrom = "";
        String createdateTo = "";

        String seccategorytype=Util.null2String(params.get("seccategorytype"));
        if("quicklook".equals(seccategorytype)){ //文件预览
            seccategorys=Util.null2String(Prop.getPropValue("HxbankQuickLook","quicklook"));
        }else if ("notification".equals(seccategorytype)){ //通知公告
            seccategorys=Util.null2String(Prop.getPropValue("HxbankQuickLook","notification"));
        }
        int pageIndex =Util.getIntValue(Prop.getPropValue("HxbankQuickLook","pageIndex"));
        if(!"".equals(Util.null2String(params.get("pageIndex")))){//当前页
            pageIndex =Util.getIntValue((String)params.get("pageIndex"));
        }
        int pageSize =Util.getIntValue(Prop.getPropValue("HxbankQuickLook","pageSize"));
        if(!"".equals(Util.null2String(params.get("pageSize")))){//页码
            pageSize =Util.getIntValue((String)params.get("pageSize"));
        }
        int isOpenSection =Util.getIntValue(Prop.getPropValue("HxbankQuickLook","isOpenSection"));
        try {
            if(isOpenSection==1) { //是否开启半年度区间查询
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                Calendar c = Calendar.getInstance();
                c.setTime(new Date());
                c.add(Calendar.MONTH, -6);
                Date m3 = c.getTime();
                createdateFrom = format.format(m3);
                createdateTo = TimeUtil.getCurrentDateString();
            }
            apidatas =getDocumentList( pageIndex,  pageSize, seccategorys, createdateFrom, createdateTo, user);
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return apidatas;
    }

    public Map getDocumentList(int pageIndex, int pageSize, String seccategorys,String createdateFrom,String createdateTo,User user) throws Exception {
        Map result = new HashMap();
        List list = new ArrayList();
        int count = 0;
        int pageCount = 0;
        int isHavePre = 0;
        int isHaveNext = 0;
        if (user != null) {
            RecordSet rs = new RecordSet();
            RecordSet rs1 = new RecordSet();
            ShareManager shareManager = new ShareManager();
            shareManager.setDocSecCategorys(seccategorys);
            shareManager.setDocCreateDateStart(createdateFrom);
            shareManager.setDocCreateDateEnd(createdateTo);
            CustomerInfoComInfo cici = new CustomerInfoComInfo();
            ResourceComInfo rci = new ResourceComInfo();
            String sql = "";
            if (rs.getDBType().equals("oracle") || rs.getDBType().equals("mysql")) {
                sql = " from DocDetail t1," + shareManager.getShareDetailTableByUser("doc", user) + " t2,DocDetailContent t3 where t1.id = t2.sourceid and t1.id = t3.docid ";
            }else {
                sql = " from DocDetail t1," + shareManager.getShareDetailTableByUser("doc", user) + " t2 where t1.id = t2.sourceid ";
            }
            sql += " and ((docstatus = 7 and (sharelevel>1 or (t1.doccreaterid="+user.getUID()+")) ) or t1.docstatus in ('1','2','5')) ";
            sql += " and seccategory!=0 and (ishistory is null or ishistory = 0) ";
//            for(int i=0;conditions!=null&&conditions.size()>0&&i<conditions.size();i++) {
//                String condition = (String) conditions.get(i);
//                if(StringUtils.isNotEmpty(condition)) {
//                    sql += " and " + condition + " ";
//                }
//            }
            sql = " select count(*) as c " + sql;
            rs.executeQuery(sql,new Object[0]);
            if(rs.next())
                count = rs.getInt("c");

            if (count <= 0) pageCount = 0;
            pageCount = count / pageSize + ((count % pageSize > 0)?1:0);

            if(pageIndex <= pageCount) {
                isHaveNext = (pageIndex + 1 <= pageCount)?1:0;
                isHavePre = (pageIndex - 1 >= 1)?1:0;
                sql = "";
                if (rs.getDBType().equals("oracle") || rs.getDBType().equals("mysql")) {
                    sql = " t1.*,t2.sharelevel,t3.doccontent from DocDetail t1," + shareManager.getShareDetailTableByUser("doc", user) + " t2,DocDetailContent t3 where t1.id = t2.sourceid and t1.id = t3.docid ";
                }else {
                    sql = " t1.*,t2.sharelevel from DocDetail t1," + shareManager.getShareDetailTableByUser("doc", user) + " t2 where t1.id = t2.sourceid ";
                }

                sql += " and ((docstatus = 7 and (sharelevel>1 or (t1.doccreaterid="+user.getUID()+")) ) or t1.docstatus in ('1','2','5')) ";
                sql += "  and seccategory!=0 and (ishistory is null or ishistory = 0) ";

//                for(int i=0;conditions!=null&&conditions.size()>0&&i<conditions.size();i++) {
//                    String condition = (String) conditions.get(i);
//                    if(StringUtils.isNotEmpty(condition)) {
//                        sql += " and " + condition + " ";
//                    }
//                }

                sql += " order by istop desc,doclastmoddate desc,doclastmodtime desc,id desc";

                if(pageIndex>0&&pageSize>0) {
                    if (rs.getDBType().equals("oracle")) {
                        sql = " select " + sql;
                        sql = "select * from ( select row_.*, rownum rownum_ from ( " + sql + " ) row_ where rownum <= " + (pageIndex * pageSize) + ") where rownum_ > " + ((pageIndex - 1) * pageSize);
                    }else if(rs.getDBType().equals("mysql")){
                        sql = " select " + sql + " limit "+(pageIndex - 1) * pageSize+","+pageSize+" ";
                    } else {
                        if(pageIndex>1) {
                            int topSize = pageSize;
                            if(pageSize * pageIndex > count) {
                                topSize = count - (pageSize * (pageIndex - 1));
                            }
                            sql = " select top " + topSize + " * from ( select top  " + topSize + " * from ( select top " + (pageIndex * pageSize) + sql + " ) tbltemp1  order by doclastmoddate asc,doclastmodtime asc,id asc ) tbltemp2  order by doclastmoddate desc,doclastmodtime desc,id desc ";
                        } else {
                            sql = " select top " + pageSize + sql;
                        }
                    }
                } else {
                    sql = " select " + sql;
                }

                rs.executeQuery(sql,new Object[0]);
                //writeLog("quicklooksql==>"+sql);
                //result.put("quicklooksql",sql);
                while (rs.next()) {
                    //{"createtime":"2011-11-15 17:22:07","docimg":"750","docid":"670","owner":"赵静","isnew":"0","docsubject":"维森集团2011年5月份维森之星"}
                    Map doc = new HashMap();
                    String docid =Util.null2String(rs.getString("id"));

                    doc.put("createtimefullstr", Util.null2String(rs.getString("doccreatedate")) + " " + Util.null2String(rs.getString("doccreatetime")));

                    doc.put("createtime", DateUtils.relative(DateUtils.parse( Util.null2String(rs.getString("doccreatedate")) + " " + Util.null2String(rs.getString("doccreatetime")), "yyyy-MM-dd HH:mm:ss"), "yyyy-MM-dd"));
                    doc.put("modifytime", DateUtils.relative(DateUtils.parse( Util.null2String(rs.getString("doclastmoddate")) + " " + Util.null2String(rs.getString("doclastmodtime")), "yyyy-MM-dd HH:mm:ss"), "yyyy-MM-dd"));
                    doc.put("modifytimefullstr", Util.null2String(rs.getString("doclastmoddate")) + " " + Util.null2String(rs.getString("doclastmodtime")));

                    //doc.put("modifytime", Util.null2String(rs.getString("doclastmoddate")) + " " + Util.null2String(rs.getString("doclastmodtime")));
                    doc.put("docid", docid);
                    doc.put("owner", Util.null2String(Util.getIntValue(Util.null2String(rs.getString("ownerType"))) == 2 ? cici.getCustomerInfoname(rs.getInt("ownerid") + "") : rci.getResourcename(rs.getInt("ownerid") + "")));
                    String docsubject_tmp = Util.null2String(rs.getString("docsubject"));
                    docsubject_tmp = docsubject_tmp.replaceAll("\n", "");// TD11607
                    docsubject_tmp = docsubject_tmp.replaceAll("&lt;", "<");
                    docsubject_tmp = docsubject_tmp.replaceAll("&gt;", ">");
                    doc.put("docsubject",docsubject_tmp);
                    String doccontent = Util.null2String(rs.getString("doccontent"));
                    sql = "select count(0) as c from DocDetail document where id = "+docid+" and ((document.doccreaterid != "+user.getUID()+" and ownerid != "+user.getUID()+") and (not exists (select 1 from docReadTag where userid="+user.getUID()+" and docid = document.id)))";
                    rs1.execute(sql);
                    if(rs1.next()&&rs1.getInt("c")>0) {
                        doc.put("isnew", "1");
                    } else {
                        doc.put("isnew", "0");
                    }
                    doc.put("istop",rs.getString("istop"));

                    doc.put("pcurlsrc",IPADDRESS+ StringUtils.replace(DOCDETAILSSOURL,"${docid}",docid));

                    DocDetailService detailService = new DocDetailService();
                    doccontent = detailService.getDocContent(Util.getIntValue(docid),user);
                    //doc.put("doctype", rs.getString("docextendname"));
                    String docextendname=Util.null2String(rs.getString("docextendname"));
                    String doctype=getDocTypeByDocId(docid,docextendname,doccontent);
                    doc.put("doctype", doctype);


//					sql = "select i.imagefileid,i.imagefiletype from docimagefile di,imagefile i where di.imagefileid = i.imagefileid and di.docid = "+Util.null2String(rs.getString("id"))+" and di.docfiletype='1'  order by i.imagefileid";
//					rs1.execute(sql);
//					if(rs1.next()) {
//						doc.put("docimg", rs1.getString("imagefileid"));
//					}
//                  String docimg=getFirstImageFileIdByDoccontent(doccontent);
//                  doc.put("docimg", docimg);

                    String imagesize=getFirstImagesizeByDocid(docid);
                    doc.put("docimagesize", imagesize);
                    String doccustype=getDocCusTypeByDocId(docid);
                    doc.put("doccustype",doccustype);

                    list.add(doc);
                }
            }
            result.put("result", "list");
            result.put("pagesize",pageSize+"");

            result.put("pageindex",pageIndex+"");
            result.put("count",count+"");
            result.put("pagecount",pageCount+"");
            result.put("ishavepre",isHavePre+"");
            result.put("ishavenext",isHaveNext+"");
            result.put("list",list);
        }
        return result;
    }

    private String getDocTypeByDocId(String docid,String docextendname,String doccontent){
        String doctype="";
        RecordSet rs =new RecordSet();
        try {
            rs.executeQuery("select imageFileName  from DocImageFile where docid=? order by isextfile asc",docid);
            if(rs.next()){
                String imageFileName=Util.null2String(rs.getString("imageFileName"));
                if(imageFileName.lastIndexOf(".")>=0){
                    if(!(imageFileName.endsWith("."))){
                        doctype=imageFileName.substring(imageFileName.lastIndexOf(".")+1);
                    }
                }
            }
        }catch (Exception e) {
            writeLog(e);
        }
        return doctype;
    }

    private String getDocCusTypeByDocId(String docid){
        String doctype="";
        RecordSet rs =new RecordSet();
        try {
            rs.executeQuery("select selectitem.selectname as selectname from cus_fielddata fielddata ," +
                    "(select selectname,selectvalue from cus_selectitem where fieldid in (" +
                    "select id from cus_formdict where fieldname='tzgglx3' and scope='DocCustomFieldBySecCategory'" +
                    ")) selectitem" +
                    " where fielddata.tzgglx3 =selectitem.selectvalue  and  scope='DocCustomFieldBySecCategory' and id =?",docid);
            if(rs.next()){
                doctype=Util.null2String(rs.getString("selectname"));
            }
        }catch (Exception e) {
            writeLog(e);
        }
        return doctype;

    }




    private String getFirstImagesizeByDocid(String docid) {
        Double filesize=0.00;
        RecordSet rs1 = new RecordSet();
        String sql = "select i.imagefileid,i.imagefiletype,i.FILESIZE from docimagefile di,imagefile i where di.imagefileid = i.imagefileid and di.docid =? order by di.isextfile asc";
        rs1.executeQuery(sql,docid);
        if(rs1.next()) {
            filesize =Util.getDoubleValue(rs1.getString("FILESIZE"),0.00);
        }
        BigDecimal   b   =   new BigDecimal(filesize / 1024 / 1024);
        double   f1   =   b.setScale(2,   BigDecimal.ROUND_HALF_UP).doubleValue();
        return f1+"M";
    }


}
