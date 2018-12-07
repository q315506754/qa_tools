package com.jiangli.doc.sql.helper.zhihuishu.bbs

import com.jiangli.doc.ExcelUtil
import com.jiangli.doc.sql.helper.zhihuishu.Env
import com.jiangli.doc.sql.helper.zhihuishu.Zhsutil
import org.springframework.jdbc.core.ColumnMapRowMapper
import java.io.FileOutputStream

/**
 * 解析excel 根据用户id查问答参与数  写回excel
 *

总助教姓名	 账号 	用户ID	课程名 	专员	课程ID 	招生ID	问答参与数	 论坛参与数
曲泉玫	13052370057	184087269	互联网金融	陈爱莲	2027584	7739
陈琴	18721031606	171198173	互联网与营销创新	陈爱莲	2027582	7737
姚映璇	18621065859	171198993	人际传播能力	陈爱莲	2027585	7748
徐璐	18817302129	164614313	新媒体与社会性别	陈爱莲	2027586	7747
于海成	18717911795	168560409	思辨与创新	陈爱莲	2027591	7744


 * @author Jiangli
 * @date 2018/11/1 11:19
 */
fun main(args: Array<String>) {
    val qajdbc = Zhsutil.getJDBC(Env.WAIWANG_BBS, "ZHS_BBS")
    val onlineshooljdbc = Zhsutil.getJDBC(Env.WAIWANG_ONLINESCHOOL, "db_G2S_OnlineSchool")

    val ouputFile = """C:\Users\Jiangli\Desktop\助教问答和论坛数据-10月需统计.xlsx"""

    ExcelUtil.processRow(ouputFile,0,1){
        file, workbook, sheet, lastRowIdx, lastColIdx, rowIdx, row ->
        if (rowIdx>=1) {
            val userId = ExcelUtil.getCellValueByTitle(sheet,row, "用户ID")
//            val email = ExcelUtil.getCellValueByTitle(sheet,row, "专员")
//            println(userId+" "+email)

//            时间范围
            val gtCreatetime = "2018-10-01"
            val ltCreatetime = "2018-11-01"
            val qCount = qajdbc.queryForObject("""
select count(*) as COUNT from ZHS_BBS.QA_QUESTION where CREATE_USER=$userId AND IS_DELETED=0 AND CREATE_TIME >= '$gtCreatetime'  AND CREATE_TIME<= '$ltCreatetime';
            """.trimIndent(), ColumnMapRowMapper())["COUNT"].toString().toInt()
            val aCount = qajdbc.queryForObject("""
select count(*) as COUNT  from  ZHS_BBS.QA_ANSWER where  A_USER_ID=$userId AND IS_DELETED=0 AND CREATE_TIME >= '$gtCreatetime'  AND CREATE_TIME<= '$ltCreatetime';
            """.trimIndent(), ColumnMapRowMapper())["COUNT"].toString().toInt()
            val cCount = qajdbc.queryForObject("""
select count(*) as COUNT from ZHS_BBS.QA_COMMENT where  COMMENT_USER_ID=$userId AND IS_DELETED=0 AND CREATE_TIME >= '$gtCreatetime'  AND CREATE_TIME<= '$ltCreatetime';
            """.trimIndent(), ColumnMapRowMapper())["COUNT"].toString().toInt()

            ExcelUtil.setCellValueByTitle(sheet,row, "问答参与数",qCount+aCount+cCount)

        }

        if (rowIdx == lastRowIdx) {
            workbook.write(FileOutputStream(ouputFile))
        }

    }
}

