package com.jiangli.doc.sql.helper.zhihuishu.bbs

import com.jiangli.doc.sql.helper.zhihuishu.Env
import com.jiangli.doc.sql.helper.zhihuishu.Zhsutil
import com.jiangli.doc.writeMapToExcel
import org.springframework.jdbc.core.ColumnMapRowMapper

/**
 *
导出 xx学校下yy课程下每个招生班级学生的发帖内容
生成到excel

课程id	课程名称	招生id	招生名称	班级名称	用户id	用户名称	问答类型	发帖内容
2015524	大学生心理健康	5410	大学生心理健康-2017秋冬	吉林大学法学院	167734967	万高越	回答	你看哪个没有对号，或者去闯关哪里看自己那关没有得三颗星那就是那关差了一点，差不多就是这样了。
2015524	大学生心理健康	5410	大学生心理健康-2017秋冬	吉林大学法学院	167734967	万高越	回答	异地恋怎么说呢，这个要看双方，不过一般人撑不住太久的异地恋。毕竟人们都渴望被爱。
2015524	大学生心理健康	5410	大学生心理健康-2017秋冬	吉林大学法学院	167734967	万高越	回答	同性恋没错，只要考虑别人的感受就好。只要不打着同性恋的旗去不尊重别人感受，那就好。做人互相尊重而已。
2015524	大学生心理健康	5410	大学生心理健康-2017秋冬	吉林大学法学院	167734967	万高越	回答	爱上一个人，你就去追，要不追不到，要么就能追到。但不追是始终追不到的
2015524	大学生心理健康	5410	大学生心理健康-2017秋冬	吉林大学法学院	167734967	万高越	回答	这个要有自己的目标和兴趣，没有目标和兴趣的话就很容易迷惘的。没有自己的兴趣的话可以培养一下。

 *
 * @author Jiangli
 * @date 2018/11/1 11:19
 */
fun main(args: Array<String>) {
    val qajdbc = Zhsutil.getJDBC(Env.WAIWANG_BBS, "ZHS_BBS")
    val onlineshooljdbc = Zhsutil.getJDBC(Env.WAIWANG_ONLINESCHOOL, "db_G2S_OnlineSchool")


    val schoolIds = arrayListOf(252)
    val courseNames = arrayListOf("大学生心理健康")
    val data = mutableListOf<MutableMap<String, Any>>()
    val ouputFile = """C:\Users\Jiangli\Desktop\吉林大学-统计-问答.xlsx"""
    var totalCount = 0

    schoolIds.forEach { schoolId ->
        courseNames.forEach { courseName ->

            val schoolAllRecruit = onlineshooljdbc.query("""
select DISTINCT (r.ID),r.NAME
from
  db_G2S_OnlineSchool.CLASS c
  LEFT JOIN db_G2S_OnlineSchool.TBL_COURSE tc on c.COURSE_ID = tc.COURSE_ID
  LEFT JOIN db_G2S_OnlineSchool.V2_RECRUIT r on r.ID = c.RECRUIT_ID
WHERE c.SCHOOL_ID=$schoolId AND tc.name LIKE '%$courseName%';
            """.trimIndent(), ColumnMapRowMapper())
//            println(schoolAllRecruit)

            schoolAllRecruit.forEach {
                val recruitId = it["ID"].toString().toInt()
                val recruitName = it["NAME"].toString()

                val recruitCourse = onlineshooljdbc.queryForObject("""
select  tc.COURSE_ID,tc.NAME,tc.ROOT_COURSE_ID from db_G2S_OnlineSchool.V2_RECRUIT r
  LEFT JOIN db_G2S_OnlineSchool.TBL_COURSE tc  on r.COURSE_ID = tc.COURSE_ID
WHERE r.ID=$recruitId;
            """.trimIndent(), ColumnMapRowMapper())

                val courseId = recruitCourse["COURSE_ID"].toString().toInt()
                val courseName = recruitCourse["NAME"].toString()
                var rootCourseId =recruitCourse["ROOT_COURSE_ID"]
                if (rootCourseId == null) {
                    rootCourseId = courseId
                }

                val schoolRecruitClass = onlineshooljdbc.query("""
select CLASS_ID,NAME from db_G2S_OnlineSchool.CLASS WHERE SCHOOL_ID=$schoolId AND RECRUIT_ID=$recruitId AND IS_DELETE=0;
            """.trimIndent(), ColumnMapRowMapper())

                schoolRecruitClass.forEach {
                    val classId = it["CLASS_ID"].toString().toInt()
                    val className = it["NAME"].toString()

                    val classUsers = onlineshooljdbc.query("""
select DISTINCT (u.ID),u.REAL_NAME from
  db_G2S_OnlineSchool.STUDENT s
  LEFT JOIN db_G2S_OnlineSchool.TBL_USER u on u.ID=s.STUDENT_ID
WHERE CLASS_ID=$classId AND IS_DELETE=0;
            """.trimIndent(), ColumnMapRowMapper())


                    classUsers.forEach {
                        val userId = it["ID"].toString().toInt()
                        val userName = it["REAL_NAME"].toString()

                        val dataOneProto = mutableMapOf<String,Any>()
                        dataOneProto["课程id"]=courseId
                        dataOneProto["课程名称"]=courseName
                        dataOneProto["招生id"]=recruitId
                        dataOneProto["招生名称"]=recruitName
                        dataOneProto["班级名称"]=className
                        dataOneProto["用户id"]=userId
                        dataOneProto["用户名称"]=userName


                        println("${++totalCount} $userId")

                        val qrs = qajdbc.query("""
select CONTENT from ZHS_BBS.QA_QUESTION where ANCESTOR_COURSE_ID=$rootCourseId AND CREATE_USER=$userId AND IS_DELETED=0;
            """.trimIndent(), ColumnMapRowMapper())
                        val QA_TYPE_KEY = "问答类型"
                        val QA_CONTENT_KEY = "发帖内容"

                        qrs.forEach {
                            val dataOne = cloneMap(dataOneProto)

                            val CONTENT = it["CONTENT"].toString()
                            dataOne[QA_TYPE_KEY]="提问"
                            dataOne[QA_CONTENT_KEY]=CONTENT

                            data.add(dataOne)
                        }

                        val ars = qajdbc.query("""
select A_CONTENT as CONTENT  from  ZHS_BBS.QA_ANSWER where ANCESTOR_COURSE_ID=$rootCourseId AND A_USER_ID=$userId AND IS_DELETED=0;
            """.trimIndent(), ColumnMapRowMapper())
                        ars.forEach {
                            val dataOne = cloneMap(dataOneProto)

                            val CONTENT = it["CONTENT"].toString()
                            dataOne[QA_TYPE_KEY]="回答"
                            dataOne[QA_CONTENT_KEY]=CONTENT

                            data.add(dataOne)
                        }

                        val crs = qajdbc.query("""
select COMMENT_CONTENT as CONTENT from ZHS_BBS.QA_COMMENT where ANCESTOR_COURSE_ID=$rootCourseId AND COMMENT_USER_ID=$userId AND IS_DELETED=0;
            """.trimIndent(), ColumnMapRowMapper())
                        crs.forEach {
                            val dataOne = cloneMap(dataOneProto)

                            val CONTENT = it["CONTENT"].toString()
                            dataOne[QA_TYPE_KEY]="评论"
                            dataOne[QA_CONTENT_KEY]=CONTENT

                            data.add(dataOne)
                        }

                    }

                }
            }
        }
    }

    //create
//    println(data)

    println("now writing...")

    writeMapToExcel(ouputFile,data)
}

fun cloneMap(dataOneProto: MutableMap<String, Any>): MutableMap<String, Any> {
    val dataOne = mutableMapOf<String, Any>()
    dataOne.putAll(dataOneProto)
    return dataOne
}