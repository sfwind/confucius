<!DOCTYPE html>
<%@ page import="com.iquanwai.confucius.biz.util.ConfigUtils" contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"%>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <!-- For IE -->
    <meta http-equiv="X-UA-Compatible" content="IE=edge">

    <!-- For Resposive Device -->
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>圈外</title>
    <meta name="keywords" content="圈外,圈外同学,圈外商学院">
    <meta name="description" content="圈外商学院">
    <link href="//www.iqycamp.com/images/logo.png" rel="shortcut icon">
    <!-- Main style sheet -->
    <link rel="stylesheet" type="text/css" href="local/css/style.css">
    <!-- responsive style sheet -->
    <link rel="stylesheet" type="text/css" href="local/css/responsive.css">


    <!-- Fix Internet Explorer ______________________________________-->

    <!--[if lt IE 9]>
    <script src="http://html5shiv.googlecode.com/svn/trunk/html5.js"></script>
    <script src="local/vendor/html5shiv.js"></script>
    <script src="local/vendor/respond.js"></script>
    <![endif]-->

</head>

<body class="seo-theme">
<div class="main-page-wrapper">

    <!-- ===================================================
      Loading Transition
    ==================================================== -->
    <div id="loader-wrapper">
        <div id="loader"></div>
    </div>


    <!--
    =============================================
      Theme Header
    ==============================================
    -->
    <header class="seo-header">
        <div class="theme-main-menu">
            <div class="container">
                <div class="main-container clearfix">
                    <div class="logo float-left">
                        <a href="/">
                            <img src="https://www.iqycamp.com/images/logo.png"
                                 style="display:inline-block;width:60px;height:60px;vertical-align: middle;"/>
                            <span style="vertical-align: middle;margin-left:10px;font-size:30px">圈外同学</span>
                            <!--<img src="local/images/logo/logo4.png" alt="Logo">-->
                        </a>
                    </div>

                    <!-- ============== Menu Warpper ================ -->
                    <div class="menu-wrapper float-right">
                        <nav id="mega-menu-holder" class="clearfix">
                            <ul class="clearfix">
                                <li><a href="/">首页</a></li>
                                <li><a href="/course_project">课程项目</a></li>
                                <li class="active"><a href="/article">文章</a></li>
                                <li><a href="/fragment/rise">线上学习</a></li>
                            </ul>
                        </nav> <!-- /#mega-menu-holder -->
                    </div> <!-- /.menu-wrapper -->
                </div> <!-- /.main-container -->
            </div> <!-- /.container -->
        </div> <!-- /.theme-main-menu -->
    </header> <!-- /.seo-header -->

    <div class="container">
        <ul class="article-nav">
            <li onclick="navChange(this)" class="nav-item">
                <a data-content='personal' class="active ">个人能力</a>
            </li>
            <li onclick="navChange(this)" class="nav-item">
                <a data-content='job'>职场规划</a>
            </li>
        </ul>
        <div id="personal" class="nav-content content-item">
            <div class="article">
                <a target="_blank" href="https://mp.weixin.qq.com/s?__biz=MzA5ODI5NTI5OQ==&mid=2651673109&idx=1&sn=06902aa2a5db93033c9114462bb51c9a&scene=21#wechat_redirect">
                    <div class="float-left">
                        <img src="https://static.iqycamp.com/images/fragment/page_preview_03.jpeg" alt=""/>
                    </div>
                    <div class="content">
                        <div class="title">我们的独立思考：既不独立，也不思考</div>
                        <div class="prew-words">
                            前两天又有热点事件霸屏了，起因是女游客在八达岭野生动物园擅自下车，被老虎咬致一死一伤。<br/>
                            这件事发生以后，有人说不要跟易怒的人结婚，有人说不该蔑视规则，有人说公园管理失误，也有人说男人懦弱……
                        </div>
                        <div class="article-info">
                            <span class="time">2016-07-28</span>
                            <span class="article-label">#破除思维定式</span>
                        </div>
                    </div>
                </a>
            </div>

            <div class="article">
                <a target="_blank" href="https://mp.weixin.qq.com/s?__biz=MzA5ODI5NTI5OQ==&mid=2651673236&idx=1&sn=c4bec7b2206d789df1f441c172cde469&scene=21#wechat_redirect">
                    <div class="float-left">
                        <img src="https://static.iqycamp.com/images/fragment/page_preview_04.jpeg" alt=""/>
                    </div>
                    <div class="content">
                        <div class="title">这3个思维定式，将我们牢牢困住</div>
                        <div class="prew-words">
                            不知你们有没看过这个纸牌魔术：魔术师让一个观众上台，他/她一边洗牌，一边让观众回答自己一些问题，同时让观众按自己的要求做些事情。
                            然后魔术师将纸牌放下，要观众心里想一张牌，之后魔术师重新洗牌，让观众掀开最上面那张，竟然真的是观众之前想的那张牌……
                        </div>
                        <div class="article-info">
                            <span class="time">2016-09-08</span>
                            <span class="article-label">#破除思维定式</span>
                        </div>
                    </div>
                </a>
            </div>
            <div class="article">
                <a target="_blank"
                   href="https://mp.weixin.qq.com/s?__biz=MzA5ODI5NTI5OQ==&mid=2651673413&idx=1&sn=41fb718a42c65eae90202aade547c4bb&chksm=8b6a3829bc1db13f75d4254d6d77aef0f21d04e0a26ad2a3a9a45662f31794ca2c21d4a18155&scene=21#wechat_redirect">
                    <div class="float-left">
                        <img src="https://static.iqycamp.com/images/fragment/page_preview_05.jpeg" alt=""/>
                    </div>
                    <div class="content">
                        <div class="title">这4个灵魂问题，解决你80%的困境</div>
                        <div class="prew-words">
                            假如你是一位咨询顾问，受Q公司CEO的邀请，参与竞标一个咨询项目，项目内容是：为Q公司重新制定一套奖金方案，提升员工积极性。<br/>
                            现在，你需要提交一份项目建议书，即：你建议如何来设计和落地这个方案，以及所需的时间和费用等。……
                        </div>
                        <div class="article-info">
                            <span class="time">2016-12-09</span>
                            <span class="article-label">#破除思维定式</span>
                        </div>
                    </div>
                </a>
            </div>

            <div class="article">
                <a target="_blank"
                   href="https://mp.weixin.qq.com/s?__biz=MzA5ODI5NTI5OQ==&mid=2651673352&idx=1&sn=3f1db35b1664341af2750bab17048500&chksm=8b6a3864bc1db1722988f0469fa09188474108cf48c311376c832717ee2715ea5ac233516299&scene=21#wechat_redirect">
                    <div class="float-left">
                        <img src="https://static.iqycamp.com/images/fragment/page_preview_06.jpeg" alt=""/>
                    </div>
                    <div class="content">
                        <div class="title">请把这支笔卖给我</div>
                        <div class="prew-words">
                            《华尔街之狼》里面，毒品销售跟莱昂纳多饰演的 Jordan说，他可以卖出任何东西。然后Jordan随手把一支笔扔给他，让他卖出去。<br/>
                            如果你没看过这部电影，可以试想一下：你会怎么卖出这支笔？……
                        </div>
                        <div class="article-info">
                            <span class="time">2016-11-04</span>
                            <span class="article-label">#学会与人沟通</span>
                        </div>
                    </div>
                </a>
            </div>


            <div class="article">
                <a target="_blank" href="https://mp.weixin.qq.com/s?__biz=MzA5ODI5NTI5OQ==&mid=2651673063&idx=1&sn=8c29c9b0da0943f041a6647912b479e4&scene=21#wechat_redirect">
                    <div class="float-left">
                        <img src="https://static.iqycamp.com/images/fragment/page_preview_10.jpeg" alt=""/>
                    </div>
                    <div class="content">
                        <div class="title">情商低不会说话？这是个伪命题</div>
                        <div class="prew-words">
                            如果你问身边的人，什么是情商，就会发现，大部分人对情商的理解就是：会说话、人脉广、世故，有时还成了智商的反义词。<br/>
                            情商这个概念，真的是被严重扭曲了……
                        </div>
                        <div class="article-info">
                            <span class="time">2016-07-06</span>
                            <span class="article-label">#学会与人沟通</span>
                        </div>
                    </div>
                </a>
            </div>

            <div class="article">
                <a target="_blank" href="https://mp.weixin.qq.com/s?__biz=MzA5ODI5NTI5OQ==&mid=2651673277&idx=1&sn=c90f28fbc1e27ce017f6744866e8d2e8&chksm=8b6a39d1bc1db0c7faa69867ceef6116e2c178093202cb1d741205296810af5a7411674fedcc&scene=21#wechat_redirect">
                    <div class="float-left">
                        <img src="https://static.iqycamp.com/images/fragment/page_preview_11.jpeg" alt=""/>
                    </div>
                    <div class="content">
                        <div class="title">穿别人的鞋，才能走好自己的路</div>
                        <div class="prew-words">
                            前几天又有大新闻，不仅朋友圈，微博都快瘫痪了。男星乔任梁去世，先有传言说SM而死，后来又有辟谣说是抑郁症。<br/>
                            事情刚出来的时候，SM致死被当成丑闻，抑郁症被骂洗白，甚至好友陈乔恩也因为没发微博悼念而被骂上了热搜第一。
                        </div>
                        <div class="article-info">
                            <span class="time">2016-09-21</span>
                            <span class="article-label">#学会与人沟通</span>
                        </div>
                    </div>
                </a>
            </div>


            <div class="article">
                <a target="_blank" href="https://mp.weixin.qq.com/s?__biz=MzA5ODI5NTI5OQ==&mid=2651673646&idx=1&sn=562e8d4007fc648ad12b092e19ba576d&chksm=8b6a3f42bc1db65402aebfbed692599a9a717107cf73e092628fdd521ef4767529ddb52f710c&scene=21#wechat_redirect">
                    <div class="float-left">
                        <img src="https://static.iqycamp.com/images/fragment/page_preview_12.png" alt=""/>
                    </div>
                    <div class="content">
                        <div class="title">为什么滑雪教练可以教出网球高手</div>
                        <div class="prew-words">
                            70年代，哈佛大学网球队的队长Timothy Gallwey，在他开设的网球训练课程中，意外地发现：一位临时借来的滑雪教练教出的网球学员，竟然比专业网球教练教出的学员进步更快！
                        </div>
                        <div class="article-info">
                            <span class="time">2017-03-16</span>
                            <span class="article-label">#学会与人沟通</span>
                        </div>
                    </div>
                </a>
            </div>


            <div class="article">
                <a target="_blank" href="https://mp.weixin.qq.com/s?__biz=MzA5ODI5NTI5OQ==&mid=2651673620&idx=1&sn=59a5561b8cd088047f59f996e21e6aac&chksm=8b6a3f78bc1db66e6b41ef1439311ad51d40d8dc838739dc772d2cc2525aad56a35869ba4031&scene=21#wechat_redirect">
                    <div class="float-left">
                        <img src="https://static.iqycamp.com/images/fragment/page_preview_13.png" alt=""/>
                    </div>
                    <div class="content">
                        <div class="title">说了那么多遍，你怎么才能改？</div>
                        <div class="prew-words">
                            多年来，我妈为了让我多喝水，没少操心。<br/>
                            比如，她买了个我专用的3L的保温壶，然后在周末的早上，过来烧一壶水，晚上再过来看看剩多少，以此来监督我每天的饮水量。<br/>
                            然后一般来说，晚上还会剩下2L水……
                        </div>
                        <div class="article-info">
                            <span class="time">2017-02-23</span>
                            <span class="article-label">#学会与人沟通</span>
                        </div>
                    </div>
                </a>
            </div>

            <div class="article">
                <a target="_blank" href="https://mp.weixin.qq.com/s?__biz=MzA5ODI5NTI5OQ==&mid=2651673287&idx=1&sn=8ccb43fb45660c7eb5966ea961116bf8&chksm=8b6a39abbc1db0bdb4e49fc2594506b8cc8eb520264e12e1423395a05778555992052f5ce892&scene=21#wechat_redirect">
                    <div class="float-left">
                        <img src="https://static.iqycamp.com/images/fragment/page_preview_14.jpeg" alt=""/>
                    </div>
                    <div class="content">
                        <div class="title">大多数人，都是怎样挥霍自己天赋的</div>
                        <div class="prew-words">
                            要论天赋如何发挥，我们先要搞清楚什么是天赋。80%以上的人，对天赋的理解基本都是错的，至少存在以下三个误区……
                        </div>
                        <div class="article-info">
                            <span class="time">2016-09-29</span>
                            <span class="article-label">#学会与人沟通</span>
                        </div>
                    </div>
                </a>
            </div>

            <div class="article">
                <a target="_blank" href="https://mp.weixin.qq.com/s?__biz=MzA5ODI5NTI5OQ==&mid=2651673405&idx=1&sn=c05fdb674cd709208a0a1ed4aa782474&chksm=8b6a3851bc1db147618172464b3f96422afaae776d7833c1ea27438c5829dbc8014441dc87a3&scene=21#wechat_redirect">
                    <div class="float-left">
                        <img src="https://static.iqycamp.com/images/fragment/page_preview_15.jpeg" alt=""/>
                    </div>
                    <div class="content">
                        <div class="title">这个时代的年轻人，还能怎么逆袭</div>
                        <div class="prew-words">
                            我是典型的80后，因为刚好是85年生。刚工作的时候，中二愤青一枚，经常跟朋友一起吐槽社会，其中吐槽次数最多的，是生不逢时。
                        </div>
                        <div class="article-info">
                            <span class="time">2016-12-02</span>
                            <span class="article-label">#学会与人沟通</span>
                        </div>
                    </div>
                </a>
            </div>

            <div class="article">
                <a target="_blank" href="https://mp.weixin.qq.com/s?__biz=MzA5ODI5NTI5OQ==&mid=2651674311&idx=1&sn=4bb70289e637fb9008c8a8518e8a38ee&chksm=8b6a3dabbc1db4bd39e518faf7d82f02d467142b8ec23bf10825fb74f04f0cfc0c4d0b8d26c0&scene=21#wechat_redirect">
                    <div class="float-left">
                        <img src="https://static.iqycamp.com/images/fragment/page_preview_16.png" alt=""/>
                    </div>
                    <div class="content">
                        <div class="title">最赚钱的事情，都不辛苦</div>
                        <div class="prew-words">
                            一个多月之前，亚马逊创始人贝佐斯曾经短暂地成为了世界首富。而亚马逊这家神奇的公司，20年都不赚钱，但贝佐斯却有能力说服华尔街接受这一点，市值还不断升高。
                        </div>
                        <div class="article-info">
                            <span class="time">2017-09-19</span>
                            <span class="article-label">#学会与人沟通</span>
                        </div>
                    </div>
                </a>
            </div>
        </div>
        <div id="job" class="nav-content content-item" style="display:none">
            <div class="article">
                <a target="_blank"
                   href="https://mp.weixin.qq.com/s?__biz=MzA5ODI5NTI5OQ==&mid=2651673121&idx=1&sn=095726bd4eca7bda57f6823c905d4df1&scene=21#wechat_redirect">
                    <div class="float-left">
                        <img src="https://static.iqycamp.com/images/fragment/page_preview_07.jpeg" alt=""/>
                    </div>
                    <div class="content">
                        <div class="title">未来，你可能不属于任何公司</div>
                        <div class="prew-words">
                            最近又有几个朋友辞职做自由职业者了，咨询顾问自己单干，倒也不少见。<br/>
                            这次略有不同，几个先后“单飞”的朋友，联合在一起，用同一个公司的名义走法律、财务流程。一个人接到需求之后，如果不是自己的擅长领域，就拉上其他擅长的顾问，抱团儿谈项目，项目下来之后再分工合作……
                        </div>
                        <div class="article-info">
                            <span class="time">2016-11-04</span>
                            <span class="article-label">#选择行业</span>
                        </div>
                    </div>
                </a>
            </div>

            <div class="article">
                <a target="_blank"
                   href="https://mp.weixin.qq.com/s?__biz=MzA5ODI5NTI5OQ==&mid=2651673141&idx=1&sn=6a5a9b58512ec3d4c95ab3cfccf98e85&scene=21#wechat_redirect">
                    <div class="float-left">
                        <img src="https://static.iqycamp.com/images/fragment/page_preview_08.jpeg" alt=""/>
                    </div>
                    <div class="content">
                        <div class="title">作为个人，你的商业模式有没有问题</div>
                        <div class="prew-words">
                            今天，介绍一个规划工具——个人商业模式画布，这是我自己用过并非常推荐的工具。<br/>
                            借助它，有利于我们拔高思维高度，去思考策略层面的事情
                        </div>
                        <div class="article-info">
                            <span class="time">2016-08-12</span>
                            <span class="article-label">#选择行业</span>
                        </div>
                    </div>
                </a>
            </div>

            <div class="article">
                <a target="_blank"
                   href="https://mp.weixin.qq.com/s?__biz=MzA5ODI5NTI5OQ==&mid=2651673955&idx=1&sn=b0496d3f2b3049da543379d60d0a1ddf&chksm=8b6a3e0fbc1db7191a16285865ec4a89a14fe27b4a35973fbba2472b07f391b0ed98cd69e491&scene=21#wechat_redirect">
                    <div class="float-left">
                        <img src="https://static.iqycamp.com/images/fragment/page_preview_0224_1.jpeg" alt=""/>
                    </div>
                    <div class="content">
                        <div class="title">转型期迷茫？这样思考，就不困扰</div>
                        <div class="prew-words">
                            知乎上曾有个经典问答：“你心中的完美爱情是怎么样的”，点赞最高的回答是“可以有不完美”。<br/>
                            正式交往前，彼此都是“情人眼里出西施”的状态，满满地全是爱，天天给你打电话，那是“关心”；迷迷糊糊不认路，那是“萌”。
                        </div>
                        <div class="article-info">
                            <span class="time">2016-08-12</span>
                            <span class="article-label">#如何转型</span>
                        </div>
                    </div>
                </a>
            </div>

            <div class="article">
                <a target="_blank"
                   href="https://mp.weixin.qq.com/s?__biz=MzA5ODI5NTI5OQ==&mid=2651673607&idx=1&sn=1694c9c07782a1985ef09be3bde7f433&chksm=8b6a3f6bbc1db67d0b7d60e98d6034b700e326cd3cecb00f1d792d7f8e824889c12bf0175238&scene=21#wechat_redirect">
                    <div class="float-left">
                        <img src="https://static.iqycamp.com/images/fragment/page_preview_09.jpeg" alt=""/>
                    </div>
                    <div class="content">
                        <div class="title">这种单一视角，阻碍了你的发展</div>
                        <div class="prew-words">
                            这张图非常经典：如果只看镜头里面的视角，会发现是左边的人想要伤害右边的人。但如果看到全部视角，才会发现，原来事实恰恰相反。<br/>
                            只看到局部，和能够看到整体，两者之间的差异是巨大的。而你所处的视角，决定了你能看到事物的哪一部分。
                        </div>
                        <div class="article-info">
                            <span class="time">2017-02-16</span>
                            <span class="article-label">#未来规划</span>
                        </div>
                    </div>
                </a>
            </div>

            <div class="article">
                <a target="_blank"
                   href="https://mp.weixin.qq.com/s?__biz=MzA5ODI5NTI5OQ==&mid=2651674356&idx=1&sn=95fe54a2d2299aa82f58e91271374670&chksm=8b6a3d98bc1db48e9785ae98c3fa1e85a0bdfa9c7e287852b50ac3dcdaf192c0ab08999625a6&scene=21#wechat_redirect">
                    <div class="float-left">
                        <img src="https://static.iqycamp.com/images/fragment/page_preview_17.jpeg" alt=""/>
                    </div>
                    <div class="content">
                        <div class="title">如何才能知道自己值多少钱?</div>
                        <div class="prew-words">
                            前两个月，有位读者通过在行约我。他在一家公司待了10年，老板对他也不错，但最近夫妻俩有了宝宝，觉得这个收入很难给孩子优越的条件，所以想要改变现在的处境。
                        </div>
                        <div class="article-info">
                            <span class="time">2017-09-26</span>
                            <span class="article-label">#未来规划</span>
                        </div>
                    </div>
                </a>
            </div>

            <%--<div class="article">--%>
                <%--<a target="_blank"--%>
                   <%--href="https://mp.weixin.qq.com/s?__biz=MzA5ODI5NTI5OQ==&mid=2651673626&idx=1&sn=49067719a43147874826419df1620041&chksm=8b6a3f76bc1db660d97ce387b4cce466d81a517f4a800f5081629ade333a07836615023292ee&scene=21#wechat_redirect">--%>
                    <%--<div class="float-left">--%>
                        <%--<img src="https://static.iqycamp.com/images/fragment/page_preview_19.jpeg" alt=""/>--%>
                    <%--</div>--%>
                    <%--<div class="content">--%>
                        <%--<div class="title">跳槽还是跳坑？你真的能分清？</div>--%>
                        <%--<div class="prew-words">--%>
                            <%--所谓 “金三银四”，是找工作的最佳季节。为什么呢？<br/>--%>
                            <%--因为这个时候刚开年，各大公司手头预算都宽裕，不像年底那么抠；而员工呢，该拿的年终奖到手了，该升的职也升了，薪资也调整过了，这时候跳槽，机会成本最低了。<br/>--%>
                            <%--但，你是跳槽还是跳坑，这就很难说了。--%>
                        <%--</div>--%>
                        <%--<div class="article-info">--%>
                            <%--<span class="time">2017-03-03</span>--%>
                            <%--<span class="article-label">#未来规划</span>--%>
                        <%--</div>--%>
                    <%--</div>--%>
                <%--</a>--%>
            <%--</div>--%>

            <div class="article">
                <a target="_blank"
                   href="https://mp.weixin.qq.com/s?__biz=MzA5ODI5NTI5OQ==&mid=2651673091&idx=1&sn=cee106d517cf656ba2f34841fcac305f&scene=21#wechat_redirect">
                    <div class="float-left">
                        <img src="https://static.iqycamp.com/images/fragment/page_preview_18.jpeg" alt=""/>
                    </div>
                    <div class="content">
                        <div class="title">我有3招，让你不再越规划越迷茫</div>
                        <div class="prew-words">
                            职业规划的主题分享包含两部分：什么是正确的规划以及如何做正确的规划。今天主要说什么是正确的职业规划，下次的分享，谈谈如何结合企业发展趋势做个人的职业规划。
                        </div>
                        <div class="article-info">
                            <span class="time">2016-07-20</span>
                            <span class="article-label">#未来规划</span>
                        </div>
                    </div>
                </a>
            </div>
        </div>
    </div>
    <!--
    =============================================
      Footer
    ==============================================
    -->
    <%@include file="footer.html"%>



    <!-- Scroll Top Button -->
    <button class="scroll-top tran3s">
        <i class="fa fa-angle-up" aria-hidden="true"></i>
    </button>

    <div id="business-apply" class="fixed-bottom-right">
        <div class="close" onclick="hiddenDialog('#business-apply')">关闭</div>
        <div class="qr-code text-center">
            <img src="local/images/home/9.jpg" class="qr-img">
        </div>
        <div class="tips text-center">
            暂时只支持移动端申请
        </div>
    </div>

    <!-- Js File_________________________________ -->
    <!-- j Query -->
    <script type="text/javascript" src="local/vendor/jquery.2.2.3.min.js"></script>

    <!-- Bootstrap JS -->
    <script type="text/javascript" src="local/vendor/bootstrap/bootstrap.min.js"></script>

    <!-- local/vendor js _________ -->
    <!-- revolution -->
    <script src="local/vendor/revolution/jquery.themepunch.tools.min.js"></script>
    <script src="local/vendor/revolution/jquery.themepunch.revolution.min.js"></script>
    <script type="text/javascript" src="local/vendor/revolution/revolution.extension.slideanims.min.js"></script>
    <script type="text/javascript" src="local/vendor/revolution/revolution.extension.layeranimation.min.js"></script>
    <script type="text/javascript" src="local/vendor/revolution/revolution.extension.navigation.min.js"></script>
    <script type="text/javascript" src="local/vendor/revolution/revolution.extension.kenburn.min.js"></script>
    <script type="text/javascript" src="local/vendor/revolution/revolution.extension.actions.min.js"></script>
    <script type="text/javascript" src="local/vendor/revolution/revolution.extension.parallax.min.js"></script>
    <script type="text/javascript" src="local/vendor/revolution/revolution.extension.migration.min.js"></script>
    <!-- menu  -->
    <script type="text/javascript" src="local/vendor/menu/src/js/jquery.slimmenu.js"></script>
    <script type="text/javascript" src="local/vendor/jquery.easing.1.3.js"></script>
    <!-- fancy box -->
    <script type="text/javascript" src="local/vendor/fancy-box/jquery.fancybox.pack.js"></script>
    <!-- MixitUp -->
    <script type="text/javascript" src="local/vendor/jquery.mixitup.min.js"></script>

    <!-- WOW js -->
    <script type="text/javascript" src="local/vendor/WOW-master/dist/wow.min.js"></script>
    <!-- owl.carousel -->
    <script type="text/javascript" src="local/vendor/owl-carousel/owl.carousel.min.js"></script>
    <!-- js count to -->
    <script type="text/javascript" src="local/vendor/jquery.appear.js"></script>
    <script type="text/javascript" src="local/vendor/jquery.countTo.js"></script>


    <!-- Theme js -->
    <script type="text/javascript" src="local/js/theme.js"></script>

</div> <!-- /.main-page-wrapper -->
<script type='text/javascript'>
    (function(m, ei, q, i, a, j, s) {
        m[i] = m[i] || function() {
            (m[i].a = m[i].a || []).push(arguments)
        };
        j = ei.createElement(q),
            s = ei.getElementsByTagName(q)[0];
        j.async = true;
        j.charset = 'UTF-8';
        j.src = 'https://static.meiqia.com/dist/meiqia.js?_=t';
        s.parentNode.insertBefore(j, s);
    })(window, document, 'script', '_MEIQIA');
    _MEIQIA('entId', 80143);
    _MEIQIA('withoutBtn');
    _MEIQIA('init');
</script>
<script>
    var display = '<%=ConfigUtils.domainName()%>'
    if(display === 'http://www.iquanwai.com' || display === 'https://www.iquanwai.com') {
        var _hmt = _hmt || [];
        (function () {

            var hm = document.createElement("script");
            hm.src = "https://hm.baidu.com/hm.js?64c8a6d40ec075c726072cd243d008a3";
            var s = document.getElementsByTagName("script")[0];
            s.parentNode.insertBefore(hm, s);
        })();
    }

</script>
</body>
</html>
