package com.jiangli.doc.zhihuishu.bbs

import com.jiangli.doc.sql.helper.zhihuishu.Env
import com.jiangli.doc.sql.helper.zhihuishu.Zhsutil
import com.jiangli.doc.zhihuishu.utils.DELETE_OPEN
import com.jiangli.doc.zhihuishu.utils.inf_domain
import com.jiangli.doc.zhihuishu.utils.requestQuestion
import org.springframework.jdbc.core.ColumnMapRowMapper

/**
 *
 *
 * @author Jiangli
 * @date 2018/11/8 16:49
 */
fun main(args: Array<String>) {
    val qajdbc = Zhsutil.getJDBC(Env.WAIWANG_BBS, "ZHS_BBS")
    val keywords = arrayListOf(
            "网课助手"
    )

//    先打印好确定之后再删除
    DELETE_OPEN = true
//    DELETE_OPEN = false

//    参数需要id和创建者id
//   会级联删除问题下回答 回答下评论
    keywords.forEach {
        val sql  = """
SELECT QUESTION_ID,CREATE_USER,RECRUIT_ID,CLASS_ID,CONTENT,CREATE_TIME  from ZHS_BBS.QA_QUESTION WHERE CONTENT LIKE '%${it}%' AND IS_DELETED=0 ORDER BY `CREATE_TIME` DESC;
        """.trimIndent()
        //        println(sql)

        val quesList = qajdbc.query(sql, ColumnMapRowMapper())
        quesList.forEach {
            println(it)
            val qId = it["QUESTION_ID"].toString().toLong()
            val userId = it["CREATE_USER"].toString()

//            requestQuestion(qajdbc, inf_domain, 720570, "187564133")
            requestQuestion(qajdbc, inf_domain, qId, userId)

//            return
        }

    }
//    requestAnswer(qajdbc, inf_domain, id.toLong(), mutableMap["CREATOR"].toString())
//    requestComment(qajdbc, inf_domain, id.toLong(), mutableMap["CREATOR"].toString())
}