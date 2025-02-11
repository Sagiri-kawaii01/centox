# MybatisPlus

## 代码生成

[代码生成模板](../../templates)

代码生成器

```kotlin
import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.generator.FastAutoGenerator
import com.baomidou.mybatisplus.generator.config.*
import com.baomidou.mybatisplus.generator.config.converts.MySqlTypeConvert
import com.baomidou.mybatisplus.generator.config.querys.MySqlQuery
import com.baomidou.mybatisplus.generator.keywords.MySqlKeyWordsHandler
import java.util.*

/**
 * @author <a href="https://github.com/Sagiri-kawaii01">lgz</a>
 * @date 2024/6/6 9:51
 * @since
 */
object CodeGenerator {
    @JvmStatic
    fun main(args: Array<String>) {
        val entity = GenEntity()
        // 数据库基本信息，账号，地址等
        entity.port = "3306"
        entity.url = "127.0.0.1"
        entity.dbName = "test_db"
        entity.userName = "root"
        entity.userPwd = "root"
        // 生成路径信息
        // 项目基本路径
        entity.modulePath = "io.github.sagirikawaii01.test"
        // 生成文件项目内路径
        entity.dirPath = "/"
        // 生成文件前缀路径，默认data
        entity.moduleName = ""
        // 生成文件名配置信息
        entity.tables = arrayOf(
            "test_user",
        )
        genTableTemplate(entity, true, true, true)
    }


    private fun genTableTemplate(entity: GenEntity, createEntity: Boolean = false, createMapper: Boolean = false, createService: Boolean = false) {
        // 数据库连接
        val sqlUrl = "jdbc:" + entity.dbType + "://" + entity.url + ":" + entity.port +
                "/" + entity.dbName + entity.dbUrlParams
        // 获取用户基本路径
        val basePath = System.getProperty("user.dir")
        // 接口适用用户（web/app）
        for (t in entity.tables) {
            val strings = t.split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val packSuffix = "." + strings[0] + "." + strings[1]
            FastAutoGenerator.create( //数据源配置，url需要修改
                DataSourceConfig.Builder(
                    sqlUrl, entity.userName, entity.userPwd
                )
                    .dbQuery(MySqlQuery())
                    .schema(entity.dbName!!)
                    .typeConvert(MySqlTypeConvert())
                    .keyWordsHandler(MySqlKeyWordsHandler())
            ) //全局配置
                .globalConfig { builder: GlobalConfig.Builder ->
                    builder.author("your_namne") // 设置作者
                        .enableKotlin()
                        .disableOpenDir() //禁止打开输出目录
                        .fileOverride() // 覆盖已生成文件
                        .outputDir(basePath + entity.dirPath + "/src/main/kotlin") // 指定输出目录
                } //包配置
                .packageConfig { builder: PackageConfig.Builder ->
                    builder.parent(entity.modulePath!!) // 设置父包名，根据实制项目路径修改
                        .moduleName(entity.moduleName) // 父包名路径下再新建的文件夹
                        .also {
                            if (createEntity) it.entity("entity$packSuffix")
                            if (createMapper) it.mapper("mapper$packSuffix")
                            if (createMapper) it.xml("mapper.xml")
                            if (createService) it.service("service$packSuffix")
                            if (createService) it.serviceImpl("service$packSuffix.impl")
                        }
                        .pathInfo(
                            Collections.singletonMap(
                                OutputFile.mapperXml,
                                basePath + entity.dirPath + "/src/main/resources/mapper"
                            )
                        ) // 存放mapper.xml路径
                }.templateConfig { builder: TemplateConfig.Builder ->
                    builder
                        .disable()
                        .also {
                            if (createEntity) it.entity("templates/entity.kt.vm")
                            if (createEntity) it.entityKt("templates/entity.kt")
                            if (createMapper) it.mapper("templates/mapper.kt.vm")
                            if (createMapper) it.mapperXml("templates/mapper.xml.vm")
                            if (createService) it.service("templates/service.kt.vm")
                            if (createService) it.serviceImpl("templates/serviceImpl.kt.vm")
                        }
                        .build()
                } //策略配置
                .strategyConfig { builder: StrategyConfig.Builder ->
                    builder.addInclude(t) // 设置需要生成的表名
                        .addTablePrefix(strings[0]) //.addTablePrefix("tb_", "c_") // 设置过滤表前缀
                        .let {
                            if (createEntity) {
                                it.entityBuilder() //实体类配置
                                    .addIgnoreColumns(entity.ignoreCol)
                                    .idType(IdType.ASSIGN_ID)
                                    .logicDeleteColumnName(entity.logicDeleteField)
                                    .formatFileName("%sEntity")
                                    .enableTableFieldAnnotation() //实体类字段注解
                            }
                            if (createMapper) {
                                it.mapperBuilder()
                                    .enableMapperAnnotation() //开启mapper注解
                                    .enableBaseResultMap() //启用 BaseResultMap 生成
                                    .enableBaseColumnList() //启用 Bas
                            }
                            if (createService) {
                                it.serviceBuilder()
                                    .formatServiceFileName("%sService")
                                    .formatServiceImplFileName("%sServiceImpl")
                            }
                        }
                }.execute()
        }
    }
}
```

