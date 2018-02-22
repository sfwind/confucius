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

    <!-- Main style sheet -->
    <link rel="stylesheet" type="text/css" href="css/style.css">
    <!-- responsive style sheet -->
    <link rel="stylesheet" type="text/css" href="css/responsive.css">


    <!-- Fix Internet Explorer ______________________________________-->

    <!--[if lt IE 9]>
    <script src="http://html5shiv.googlecode.com/svn/trunk/html5.js"></script>
    <script src="vendor/html5shiv.js"></script>
    <script src="vendor/respond.js"></script>
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
                        <a href="index.html">
                            <img src="https://www.iqycamp.com/images/logo.png"
                                 style="display:inline-block;width:50px;height:50px;vertical-align: middle;"></img>
                            <span style="vertical-align: middle;">圈外同学</span>
                            <!--<img src="images/logo/logo4.png" alt="Logo">-->
                        </a>
                    </div>

                    <!-- ============== Menu Warpper ================ -->
                    <div class="menu-wrapper float-right">
                        <nav id="mega-menu-holder" class="clearfix">
                            <ul class="clearfix">
                                <li class="active"><a href="/">首页</a></li>
                                <li><a href="/course_project">课程项目</a></li>
                                <li><a href="/article">文章</a></li>
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
            </div>

            <div class="article">
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
            </div>
            <div class="article">
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
            </div>

            <div class="article">
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
            </div>
        </div>
        <div id="job" class="nav-content content-item" style="display:none">
            <div class="article">
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
            </div>

            <div class="article">
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
            </div>

            <div class="article">
                <div class="float-left">
                    <img src="https://static.iqycamp.com/images/fragment/page_preview_08.jpeg" alt=""/>
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
            </div>

            <div class="article">
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
            </div>
        </div>
    </div>
    <!--
    =============================================
      Footer
    ==============================================
    -->
    <footer class="default-footer seo-footer">
        <div class="container">
            <div class="top-footer row clearfix">
                <div class="col-md-6 col-sm-12 footer-left">
                    <!--<a href="index.html"><img src="images/logo/logo3.png" alt="Logo"></a>-->
                    <p class="footer-title"><span class="company">圈外同学</span> 你负责努力，我们负责帮你赢</p>
                    <div class="link-container">
                        <span class="item"><a href="course_project.html?jump=#company-banner">企业合作</a></span>
                        <span class="item">意见反馈</span>
                        <span class="item"><a target="_blank" href="https://book.douban.com/subject/26936065/">圈圈的书</a></span>
                        <span class="item"><a target="_blank"
                                              href="https://mp.weixin.qq.com/s?__biz=MzA5ODI5NTI5OQ==&mid=504191235&idx=1&sn=27dc7eba4ce96e23f995a114687bcec7">正在招聘</a></span>
                    </div>
                </div> <!-- /.footer-logo -->
                <div class="col-md-6 col-sm-12 footer-right">
                    <!--<a href="index.html"><img src="images/logo/logo3.png" alt="Logo"></a>-->
                    <div class="qr-code-container text-left">
                        <div class="item text-center">
                            <img src="https://www.iqycamp.com/images/subscribeCode.jpg" alt="圈外孙圈圈">
                            <div class="name">圈外孙圈圈</div>
                        </div>
                        <div class="item text-center">
                            <img src="https://www.iqycamp.com/images/serverQrCode.jpg" alt="圈外同学">
                            <div class="name">圈外同学</div>
                        </div>
                        <div class="item text-center">
                            <img src="images/team/4.jpg" alt="申请商学院">
                            <div class="name">申请商学院</div>
                        </div>
                    </div>
                </div> <!-- /.footer-logo -->
            </div> <!-- /.container -->

            <div class="bottom-footer">
                <div class="container">
                    <div class="wrapper clearfix">
                        <p class="float-left">沪ICP备15006409号</p>
                    </div> <!-- /.wrapper -->
                </div> <!-- /.container -->
            </div> <!-- /.bottom-footer -->
        </div>
    </footer>


    <!-- Scroll Top Button -->
    <button class="scroll-top tran3s">
        <i class="fa fa-angle-up" aria-hidden="true"></i>
    </button>

    <div id="business-apply" class="fixed-bottom-right">
        <div class="close" onclick="hiddenDialog('#business-apply')">关闭</div>
        <div class="qr-code text-center">
            <img src="images/home/9.jpg" class="qr-img">
        </div>
        <div class="tips text-center">
            暂时只支持移动端申请
        </div>
    </div>

    <!-- Js File_________________________________ -->
    <!-- j Query -->
    <script type="text/javascript" src="vendor/jquery.2.2.3.min.js"></script>

    <!-- Bootstrap JS -->
    <script type="text/javascript" src="vendor/bootstrap/bootstrap.min.js"></script>

    <!-- Vendor js _________ -->
    <!-- revolution -->
    <script src="vendor/revolution/jquery.themepunch.tools.min.js"></script>
    <script src="vendor/revolution/jquery.themepunch.revolution.min.js"></script>
    <script type="text/javascript" src="vendor/revolution/revolution.extension.slideanims.min.js"></script>
    <script type="text/javascript" src="vendor/revolution/revolution.extension.layeranimation.min.js"></script>
    <script type="text/javascript" src="vendor/revolution/revolution.extension.navigation.min.js"></script>
    <script type="text/javascript" src="vendor/revolution/revolution.extension.kenburn.min.js"></script>
    <script type="text/javascript" src="vendor/revolution/revolution.extension.actions.min.js"></script>
    <script type="text/javascript" src="vendor/revolution/revolution.extension.parallax.min.js"></script>
    <script type="text/javascript" src="vendor/revolution/revolution.extension.migration.min.js"></script>
    <!-- menu  -->
    <script type="text/javascript" src="vendor/menu/src/js/jquery.slimmenu.js"></script>
    <script type="text/javascript" src="vendor/jquery.easing.1.3.js"></script>
    <!-- fancy box -->
    <script type="text/javascript" src="vendor/fancy-box/jquery.fancybox.pack.js"></script>
    <!-- MixitUp -->
    <script type="text/javascript" src="vendor/jquery.mixitup.min.js"></script>

    <!-- WOW js -->
    <script type="text/javascript" src="vendor/WOW-master/dist/wow.min.js"></script>
    <!-- owl.carousel -->
    <script type="text/javascript" src="vendor/owl-carousel/owl.carousel.min.js"></script>
    <!-- js count to -->
    <script type="text/javascript" src="vendor/jquery.appear.js"></script>
    <script type="text/javascript" src="vendor/jquery.countTo.js"></script>


    <!-- Theme js -->
    <script type="text/javascript" src="js/theme.js"></script>

</div> <!-- /.main-page-wrapper -->
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
