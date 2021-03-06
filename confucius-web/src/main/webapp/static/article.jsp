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
    <meta name="keywords" content="圈外,圈外同学,圈外商学院,商学院">
    <meta name="description" content="圈外商学院">
    <%=request.getServerName().endsWith(".iquanwai.com")?"":"<meta name=\"robots\" content=\"noindex\">"%>
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
                            <img src="https://www.iqycamp.com/images/fragment/logo2x.jpg"
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
                                <li  class="hidden-xs"><a href="/fragment/rise">线上学习</a></li>
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
                        <img src="https://static.iqycamp.com/images/fragment/page_preview_03.jpeg" alt="我们的独立思考：既不独立，也不思考"/>
                    </div>
                    <div class="content">
                        <div class="title">我们的独立思考：既不独立，也不思考</div>
                        <div class="prew-words">
                            我们关心的从来都是“观点和立场”，从来都不是“事实和真相”
                        </div>
                        <div class="article-info">
                            <span class="article-label">#破除思维定式</span>
                        </div>
                    </div>
                </a>
            </div>

            <div class="article">
                <a target="_blank" href="https://mp.weixin.qq.com/s?__biz=MzA5ODI5NTI5OQ==&mid=2651673236&idx=1&sn=c4bec7b2206d789df1f441c172cde469&scene=21#wechat_redirect">
                    <div class="float-left">
                        <img src="https://static.iqycamp.com/images/fragment/page_preview_04.jpeg" alt="思维定式"/>
                    </div>
                    <div class="content">
                        <div class="title">这3个思维定式，将我们牢牢困住</div>
                        <div class="prew-words">
                            希望我们每个人，都不再做纸牌魔术里的那个观众。
                        </div>
                        <div class="article-info">
                            <span class="article-label">#破除思维定式</span>
                        </div>
                    </div>
                </a>
            </div>
            <div class="article">
                <a target="_blank"
                   href="https://mp.weixin.qq.com/s?__biz=MzA5ODI5NTI5OQ==&mid=2651673413&idx=1&sn=41fb718a42c65eae90202aade547c4bb&chksm=8b6a3829bc1db13f75d4254d6d77aef0f21d04e0a26ad2a3a9a45662f31794ca2c21d4a18155&scene=21#wechat_redirect">
                    <div class="float-left">
                        <img src="https://static.iqycamp.com/images/fragment/page_preview_05.jpeg" alt="这4个灵魂问题，解决你80%的困境"/>
                    </div>
                    <div class="content">
                        <div class="title">这4个灵魂问题，解决你80%的困境</div>
                        <div class="prew-words">
                            我个人私藏的4个灵魂问题，现在顺着wifi去找你~
                        </div>
                        <div class="article-info">
                            <span class="article-label">#破除思维定式</span>
                        </div>
                    </div>
                </a>
            </div>

            <div class="article">
                <a target="_blank"
                   href="https://mp.weixin.qq.com/s?__biz=MzA5ODI5NTI5OQ==&mid=2651673352&idx=1&sn=3f1db35b1664341af2750bab17048500&chksm=8b6a3864bc1db1722988f0469fa09188474108cf48c311376c832717ee2715ea5ac233516299&scene=21#wechat_redirect">
                    <div class="float-left">
                        <img src="https://static.iqycamp.com/images/fragment/page_preview_06.jpeg" alt="请把这支笔卖给我"/>
                    </div>
                    <div class="content">
                        <div class="title">请把这支笔卖给我</div>
                        <div class="prew-words">
                            出来混，我们都是卖笔的~
                        </div>
                        <div class="article-info">
                            <span class="article-label">#学会与人沟通</span>
                        </div>
                    </div>
                </a>
            </div>


            <div class="article">
                <a target="_blank" href="https://mp.weixin.qq.com/s?__biz=MzA5ODI5NTI5OQ==&mid=2651673063&idx=1&sn=8c29c9b0da0943f041a6647912b479e4&scene=21#wechat_redirect">
                    <div class="float-left">
                        <img src="https://static.iqycamp.com/images/fragment/page_preview_10.jpeg" alt="情商低不会说话？这是个伪命题"/>
                    </div>
                    <div class="content">
                        <div class="title">情商低不会说话？这是个伪命题</div>
                        <div class="prew-words">
                            你所知道的情商，都是错的……
                        </div>
                        <div class="article-info">
                            <span class="article-label">#学会与人沟通</span>
                        </div>
                    </div>
                </a>
            </div>

            <div class="article">
                <a target="_blank" href="https://mp.weixin.qq.com/s?__biz=MzA5ODI5NTI5OQ==&mid=2651673277&idx=1&sn=c90f28fbc1e27ce017f6744866e8d2e8&chksm=8b6a39d1bc1db0c7faa69867ceef6116e2c178093202cb1d741205296810af5a7411674fedcc&scene=21#wechat_redirect">
                    <div class="float-left">
                        <img src="https://static.iqycamp.com/images/fragment/page_preview_11_1.jpg" alt="穿别人的鞋，才能走好自己的路"/>
                    </div>
                    <div class="content">
                        <div class="title">穿别人的鞋，才能走好自己的路</div>
                        <div class="prew-words">
                            如何把“我”，变成“我们”
                        </div>
                        <div class="article-info">
                            <span class="article-label">#学会与人沟通</span>
                        </div>
                    </div>
                </a>
            </div>


            <div class="article">
                <a target="_blank" href="https://mp.weixin.qq.com/s?__biz=MzA5ODI5NTI5OQ==&mid=2651673646&idx=1&sn=562e8d4007fc648ad12b092e19ba576d&chksm=8b6a3f42bc1db65402aebfbed692599a9a717107cf73e092628fdd521ef4767529ddb52f710c&scene=21#wechat_redirect">
                    <div class="float-left">
                        <img src="https://static.iqycamp.com/images/fragment/page_preview_12_1.jpg" alt="为什么滑雪教练可以教出网球高手"/>
                    </div>
                    <div class="content">
                        <div class="title">为什么滑雪教练可以教出网球高手</div>
                        <div class="prew-words">
                            普通人如何学会做教练，帮朋友提建议、帮客户解决问题、让孩子更爱学习……
                        </div>
                        <div class="article-info">
                            <span class="article-label">#学会与人沟通</span>
                        </div>
                    </div>
                </a>
            </div>


            <div class="article">
                <a target="_blank" href="https://mp.weixin.qq.com/s?__biz=MzA5ODI5NTI5OQ==&mid=2651673620&idx=1&sn=59a5561b8cd088047f59f996e21e6aac&chksm=8b6a3f78bc1db66e6b41ef1439311ad51d40d8dc838739dc772d2cc2525aad56a35869ba4031&scene=21#wechat_redirect">
                    <div class="float-left">
                        <img src="https://static.iqycamp.com/images/fragment/page_preview_13_1.jpg" alt="说了那么多遍，你怎么才能改？"/>
                    </div>
                    <div class="content">
                        <div class="title">说了那么多遍，你怎么才能改？</div>
                        <div class="prew-words">
                            不要再说：听过很多道理，却仍然过不好一生
                        </div>
                        <div class="article-info">
                            <span class="article-label">#学会与人沟通</span>
                        </div>
                    </div>
                </a>
            </div>

            <div class="article">
                <a target="_blank" href="https://mp.weixin.qq.com/s?__biz=MzA5ODI5NTI5OQ==&mid=2651673287&idx=1&sn=8ccb43fb45660c7eb5966ea961116bf8&chksm=8b6a39abbc1db0bdb4e49fc2594506b8cc8eb520264e12e1423395a05778555992052f5ce892&scene=21#wechat_redirect">
                    <div class="float-left">
                        <img src="https://static.iqycamp.com/images/fragment/page_preview_14_1.jpg" alt="大多数人，都是怎样挥霍自己天赋的"/>
                    </div>
                    <div class="content">
                        <div class="title">大多数人，都是怎样挥霍自己天赋的</div>
                        <div class="prew-words">
                            对号入座，你是怎样挥霍自己天赋的
                        </div>
                        <div class="article-info">
                            <span class="article-label">#学会与人沟通</span>
                        </div>
                    </div>
                </a>
            </div>

            <div class="article">
                <a target="_blank" href="https://mp.weixin.qq.com/s?__biz=MzA5ODI5NTI5OQ==&mid=2651673405&idx=1&sn=c05fdb674cd709208a0a1ed4aa782474&chksm=8b6a3851bc1db147618172464b3f96422afaae776d7833c1ea27438c5829dbc8014441dc87a3&scene=21#wechat_redirect">
                    <div class="float-left">
                        <img src="https://static.iqycamp.com/images/fragment/page_preview_15_1.jpg" alt="这个时代的年轻人，还能怎么逆袭"/>
                    </div>
                    <div class="content">
                        <div class="title">这个时代的年轻人，还能怎么逆袭</div>
                        <div class="prew-words">
                            如果这个世界还像多年前的体制内一样，靠经验、论资排辈，那还有年轻人什么事儿呢？
                        </div>
                        <div class="article-info">
                            <span class="article-label">#学会与人沟通</span>
                        </div>
                    </div>
                </a>
            </div>

            <div class="article">
                <a target="_blank" href="https://mp.weixin.qq.com/s?__biz=MzA5ODI5NTI5OQ==&mid=2651674311&idx=1&sn=4bb70289e637fb9008c8a8518e8a38ee&chksm=8b6a3dabbc1db4bd39e518faf7d82f02d467142b8ec23bf10825fb74f04f0cfc0c4d0b8d26c0&scene=21#wechat_redirect">
                    <div class="float-left">
                        <img src="https://static.iqycamp.com/images/fragment/page_preview_16_1.jpg" alt="最赚钱的事情，都不辛苦"/>
                    </div>
                    <div class="content">
                        <div class="title">最赚钱的事情，都不辛苦</div>
                        <div class="prew-words">
                            遇到一个几乎让我跪了的司机，让我理解：最赚钱的事情，都不辛苦
                        </div>
                        <div class="article-info">
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
                        <img src="https://static.iqycamp.com/images/fragment/page_preview_07_1.jpg" alt="未来，你可能不属于任何公司"/>
                    </div>
                    <div class="content">
                        <div class="title">未来，你可能不属于任何公司</div>
                        <div class="prew-words">
                            这是最好的时代，这也是最坏的时代。你要怎么做，才能让它成为你的好时代？
                        </div>
                        <div class="article-info">
                            <span class="article-label">#选择行业</span>
                        </div>
                    </div>
                </a>
            </div>

            <div class="article">
                <a target="_blank"
                   href="https://mp.weixin.qq.com/s?__biz=MzA5ODI5NTI5OQ==&mid=2651673141&idx=1&sn=6a5a9b58512ec3d4c95ab3cfccf98e85&scene=21#wechat_redirect">
                    <div class="float-left">
                        <img src="https://static.iqycamp.com/images/fragment/page_preview_08_1.jpg" alt="作为个人，你的商业模式有没有问题"/>
                    </div>
                    <div class="content">
                        <div class="title">作为个人，你的商业模式有没有问题</div>
                        <div class="prew-words">
                            一个工具，解决三大问题
                        </div>
                        <div class="article-info">
                            <span class="article-label">#选择行业</span>
                        </div>
                    </div>
                </a>
            </div>

            <div class="article">
                <a target="_blank"
                   href="https://mp.weixin.qq.com/s?__biz=MzA5ODI5NTI5OQ==&mid=2651673955&idx=1&sn=b0496d3f2b3049da543379d60d0a1ddf&chksm=8b6a3e0fbc1db7191a16285865ec4a89a14fe27b4a35973fbba2472b07f391b0ed98cd69e491&scene=21#wechat_redirect">
                    <div class="float-left">
                        <img src="https://static.iqycamp.com/images/fragment/page_preview_0224_1.jpeg" alt="转型期迷茫？这样思考，就不困扰"/>
                    </div>
                    <div class="content">
                        <div class="title">转型期迷茫？这样思考，就不困扰</div>
                        <div class="prew-words">
                            究竟该如何看待转行或跳槽？又该以什么样的姿势，才能做出正确的选择呢？
                        </div>
                        <div class="article-info">
                            <span class="article-label">#如何转型</span>
                        </div>
                    </div>
                </a>
            </div>

            <div class="article">
                <a target="_blank"
                   href="https://mp.weixin.qq.com/s?__biz=MzA5ODI5NTI5OQ==&mid=2651673607&idx=1&sn=1694c9c07782a1985ef09be3bde7f433&chksm=8b6a3f6bbc1db67d0b7d60e98d6034b700e326cd3cecb00f1d792d7f8e824889c12bf0175238&scene=21#wechat_redirect">
                    <div class="float-left">
                        <img src="https://static.iqycamp.com/images/fragment/page_preview_09_1.jpg" alt="这种单一视角，阻碍了你的发展"/>
                    </div>
                    <div class="content">
                        <div class="title">这种单一视角，阻碍了你的发展</div>
                        <div class="prew-words">
                            个人成长和发展的“月经”问题， 90%以上都是因为视角的不全面
                        </div>
                        <div class="article-info">
                            <span class="article-label">#未来规划</span>
                        </div>
                    </div>
                </a>
            </div>

            <div class="article">
                <a target="_blank"
                   href="https://mp.weixin.qq.com/s?__biz=MzA5ODI5NTI5OQ==&mid=2651674356&idx=1&sn=95fe54a2d2299aa82f58e91271374670&chksm=8b6a3d98bc1db48e9785ae98c3fa1e85a0bdfa9c7e287852b50ac3dcdaf192c0ab08999625a6&scene=21#wechat_redirect">
                    <div class="float-left">
                        <img src="https://static.iqycamp.com/images/fragment/page_preview_17_1.jpg" alt="如何才能知道自己值多少钱"/>
                    </div>
                    <div class="content">
                        <div class="title">如何才能知道自己值多少钱?</div>
                        <div class="prew-words">
                            找工作不是为了跳槽，而是为了增加市场敏感度！
                        </div>
                        <div class="article-info">
                            <span class="article-label">#未来规划</span>
                        </div>
                    </div>
                </a>
            </div>

            <div class="article">
                <a target="_blank"
                   href="https://mp.weixin.qq.com/s?__biz=MzA5ODI5NTI5OQ==&mid=2651673626&idx=1&sn=49067719a43147874826419df1620041&chksm=8b6a3f76bc1db660d97ce387b4cce466d81a517f4a800f5081629ade333a07836615023292ee&scene=21#wechat_redirect">
                    <div class="float-left">
                        <img src="https://static.iqycamp.com/images/fragment/page_preview_0227_1.jpg" alt="跳槽还是跳坑？你真的能分清？"/>
                    </div>
                    <div class="content">
                        <div class="title">跳槽还是跳坑？你真的能分清？</div>
                        <div class="prew-words">
                            猜猜看，哪些是坑？
                        </div>
                        <div class="article-info">
                            <span class="article-label">#未来规划</span>
                        </div>
                    </div>
                </a>
            </div>

            <div class="article">
                <a target="_blank"
                   href="https://mp.weixin.qq.com/s?__biz=MzA5ODI5NTI5OQ==&mid=2651673091&idx=1&sn=cee106d517cf656ba2f34841fcac305f&scene=21#wechat_redirect">
                    <div class="float-left">
                        <img src="https://static.iqycamp.com/images/fragment/page_preview_18_1.jpg" alt="我有3招，让你不再越规划越迷茫"/>
                    </div>
                    <div class="content">
                        <div class="title">我有3招，让你不再越规划越迷茫</div>
                        <div class="prew-words">
                            什么是正确的规划以及如何做正确的规划
                        </div>
                        <div class="article-info">
                            <span class="article-label">#未来规划</span>
                        </div>
                    </div>
                </a>
            </div>


        </div>
        <div class="banner-box">
            <div class="img">
                <img src="https://static.iqycamp.com/images/fragment/dyh_banner_1.jpeg" alt="圈外订阅号">
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
            长按保存，微信扫码即可申请
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
