JeeWx 微信管家平台，简称 “捷微”.
===============
  （一款免费开源的JAVA微信运营平台）
===============
当前最新版本： 4.0（发布日期：20180815）
官网：[www.jeewx.com](http://www.jeewx.com) 

![jeewx](https://static.oschina.net/uploads/img/201807/26192621_ou91.png "jeewx")
<br>

一、平台简介
-----------------------------------
Jeewx是一款开源的微信运营平台，采用JAVA语言微服务架构，插件式开发，支持微信公众号、企业微信、各种微信活动、商城、小程序等功能。另外JEEWX自带强大的代码生成器，便于用户二次开发，JEEWX属于第一批微信开发商，在2014年荣获CSDN开发商大会第一名，目前功能已经非常完成，机制也非常健全。
![jeewx](https://static.oschina.net/uploads/img/201807/26193036_XzZ1.png "jeewx")

二、平台特性
-----------------------------------
* 	1、JEEWX 从4.0版本开始，技术架构全新换代，采用微服务架构（基础框架：SpringMvc + Mybatis + Velocity + Maven(构建)）
*   2、每个业务模块采用插件方式独立开发，以插件方式提供用户集成（比如：一个活动一个插件，方便用户升级和剥离）
*   3、开源免费，jeewx遵循Apache2开源协议
*   4、详细的二次开发文档，并不断更新增加相关开发案例提供学习参考
*   5、持续升级中，陆续支持微信公众号、H5活动、微信企业号、微信小程序等多触点
*   6、完善的用户权限管理，强大的代码生成器，有效的提高开发效率
*   7、支持微信小程序功能的集成
*   8、拓展翻译、天气、长转短连接、语音识别、二维码等实用工具
*   9、微信Oauth2.0机制的封装，可方便集成第三方应用到微信平台


三、平台功能
-----------------------------------

【微信公众号】
*   1、微信账号管理
*   2、微信菜单管理
*   3、关注欢迎语
*   4、关键字管理
*   5、自定义菜单
*   6、小程序链接
*   7、文本素材管理
*   8、图文素材管理
*   9、微信永久素材
*   10、支持多公众号
*   11、微信大转盘
*   12、微信刮刮乐
*   13、微网站
*   14、翻译
*   15、天气
*   16、Oauth2.0链接
*   17、微信第三方平台（全网发布）
*   18、长链接转短连接
*   19、系统用户管理
*   20、系统用户角色
*   21、系统菜单管理


【企业微信】
*   1、微信企业号管理
*   2、微信应用管理
*   3、素材管理：文本素材
*   4、素材管理：图文素材
*   5、菜单管理
*   6、通讯录管理
*   7、用户管理
*   8、关键字管理
*   9、关注回复管理
*   10、用户消息管理
*   11、用户消息快捷回复
*   12、企业号群发功能
*   13、企业号群发日志


四、架构说明
-----------------------------------
    1.采用SpringMvc + Mybatis + Velocity + Maven(构建) 框架技术
    2.插件引入方式
        pom.xml文件中，引入新开发的插件
        <!-- P3 jar -->
 	    <dependency>
			<groupId>org.h5huodong</groupId>
			<artifactId>P3-Biz-jiugongge</artifactId>
			<version>1.0.0</version>
			<type>jar</type>
		</dependency>
	3.项目启动访问方式：
	  采用maven方式，启动Web项目
      http://localhost:8080/jeewx
    4.页面层面不能采用jsp，需要采用模板语言Velocity
    5.实现插件式开发，按照模块进行开发，每个模块可以单独达成jar包
	6.数据库配置文件：
	  src/main/resources/db.properties


五、项目介绍
-----------------------------------
      捷微 - H5活动源码列表（陆续更新..）
	  1.微信公众号管理   P3-Biz-commonweixin
	  2.摇一摇送卡券     P3-Biz-shaketicket
	  3.九宫格活动       P3-Biz-jiugongge
	  4.启动项目         P3-Web


六、开发入门
-----------------------------------
	1.Eclipse + Maven + JDK7
    2.项目以Maven方式导入eclipse
	3.初始化数据库脚步
	    P3-Web\doc\db\jeewx-h5-mysql-20180810.sql
	4.采用maven方式，启动主项目P3-Web，命令：tomcat:run
      活动访问地址：
	     http://localhost:8080/jeewx
	  说明：插件不能单独启动，maven方式引入到Web项目
	5.系统默认登录账号 admin/123456
	  
	
六、代码生成器
-----------------------------------
	1.工具类：P3-Web/src/main/java/org/jeecgframework/p3/cg/util/CodeToolUtil.java
	2.配置文件：P3-Web/src/main/resources/p3-cg-config.properties
	
	
七、技术交流
-----------------------------------
* 捷微官网：[www.jeewx.com](http://www.jeewx.com)
* 技术论坛 ：[www.jeecg.org](http://www.jeecg.org)
* QQ交流群 : 97460170
* 在线开发文档： [http://jeewx-h5.mydoc.io](http://http://jeewx-h5.mydoc.io)


八、在线体验
-----------------------------------
*   捷微多公众号管理平台: [www.jeewx.com/jeewx](http://www.jeewx.com/jeewx)
*   捷微H5活动平台: [www.h5huodong.com](http://www.h5huodong.com)
*   官方公众号

![github](http://www.jeecg.org/data/attachment/forum/201601/25/180314mjvputsot6hhtvoa.jpg "jeewx521")
![jeewx](http://www.jeecg.org/data/attachment/forum/201808/15/034735nna2fnc1hkhl1993.jpg "jeewx")


九、系统截图 
-----------------------------------
### 捷微H5（后台）
![github](https://static.oschina.net/uploads/img/201808/13105211_M0FW.png "jeecg")
![github](https://static.oschina.net/uploads/img/201808/13105211_AVY4.png "jeecg")
![github](https://static.oschina.net/uploads/img/201808/11172049_s7hH.png "jeecg")
![github](https://static.oschina.net/uploads/img/201808/11153109_73Aj.png "jeecg")
![github](https://static.oschina.net/uploads/img/201808/11221430_KZ1b.png "jeecg")

### 捷微H5（活动效果）
![github](http://www.jeecg.org/data/attachment/forum/201601/25/180710anjfgtn677nojgg0.png "jeecg")
![github](https://static.oschina.net/uploads/img/201808/13105211_lMFh.jpg "jeecg")
![github](http://www.jeecg.org/data/attachment/forum/201601/25/180500iwpg1agqm778wggp.png "jeecg")
![github](https://static.oschina.net/uploads/img/201808/11195358_bi9e.png "jeecg")
