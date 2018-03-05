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
                                <li class="active"><a href="/">首页</a></li>
                                <li><a href="/course_project">课程项目</a></li>
                                <li><a href="/article">文章</a></li>
                                <li class="hidden-xs"><a href="/fragment/rise">线上学习</a></li>
                                <!--<li><a href="contact-us.html">Contact us</a></li>-->
                            </ul>
                        </nav> <!-- /#mega-menu-holder -->
                    </div> <!-- /.menu-wrapper -->
                </div> <!-- /.main-container -->
            </div> <!-- /.container -->
        </div> <!-- /.theme-main-menu -->
    </header> <!-- /.seo-header -->

    <!--
    =============================================
      Theme Main Banner
    ==============================================
    -->

    <div id="banner" class="no-mg">
        <div class="rev_slider_wrapper">
            <!-- START REVOLUTION SLIDER 5.0.7 auto mode -->
            <div id="seo-main-banner" class="rev_slider" data-version="5.0.7">
                <ul>
                    <!-- SLIDE1  -->
                    <li data-index="rs-280" data-transition="zoomout" data-slotamount="default" data-easein="Power4.easeInOut"
                        data-easeout="Power4.easeInOut" data-masterspeed="2000" data-rotate="0" data-saveperformance="off"
                        data-title="01" data-description="">
                        <!-- MAIN IMAGE -->
                        <img src="local/images/home/slide-7.jpg" alt="背景图" class="rev-slidebg" data-bgparallax="3"
                             data-bgposition="center center" data-duration="20000" data-ease="Linear.easeNone" data-kenburns="on"
                             data-no-retina="" data-offsetend="0 0" data-offsetstart="0 0" data-rotateend="0" data-rotatestart="0"
                             data-scaleend="100" data-scalestart="140">
                        <!-- LAYERS -->
                        <!-- LAYER NR. 1 -->
                        <div class="tp-caption"
                             data-x="['left','left','left','left']" data-hoffset="['15','15','20','20']"
                             data-y="['middle','middle','middle','middle']" data-voffset="['-120','-120','-120','-170']"
                             data-width="full"
                             data-height="none"
                             data-whitespace="normal"
                             data-transform_idle="o:1;"
                             data-transform_in="y:[100%];z:0;rX:0deg;rY:0;rZ:0;sX:1;sY:1;skX:0;skY:0;opacity:0;s:2000;e:Power4.easeInOut;"
                             data-transform_out="y:[100%];s:1000;e:Power2.easeInOut;s:1000;e:Power2.easeInOut;"
                             data-mask_in="x:0px;y:[100%];"
                             data-mask_out="x:inherit;y:inherit;"
                             data-start="500"
                             data-splitin="none"
                             data-splitout="none"
                             data-responsive_offset="on"
                             style="z-index: 6;">
                            <h1>圈外商学院</h1>
                        </div>
                        <!-- LAYER NR. 2 -->
                        <div class="tp-caption"
                             data-x="['left','left','left','left']" data-hoffset="['15','15','20','20']"
                             data-y="['middle','middle','middle','middle']" data-voffset="['10','10','10','-50']"
                             data-width="full"
                             data-height="none"
                             data-whitespace="normal"
                             data-transform_idle="o:1;"
                             data-transform_in="y:[100%];z:0;rX:0deg;rY:0;rZ:0;sX:1;sY:1;skX:0;skY:0;opacity:0;s:2000;e:Power4.easeInOut;"
                             data-transform_out="y:[100%];s:1000;e:Power2.easeInOut;s:1000;e:Power2.easeInOut;"
                             data-mask_in="x:0px;y:[100%];"
                             data-mask_out="x:inherit;y:inherit;"
                             data-start="1500"
                             data-splitin="none"
                             data-splitout="none"
                             style="z-index: 6;">
                            <p>一所创新型在线商学院，为职场人士提供最实用的课程、最适合的发展机会。让每一个有潜力的人，在这个时代拥有自己的职场话语权。</p>
                        </div>
                        <!-- LAYER NR. 3 -->
                        <div class="tp-caption"
                             data-x="['left','left','left','left']" data-hoffset="['15','15','20','20']"
                             data-y="['middle','middle','middle','middle']" data-voffset="['115','115','115','80']"
                             data-transform_idle="o:1;"
                             data-transform_hover="o:1;rX:0;rY:0;rZ:0;z:0;s:300;e:Power1.easeInOut;"
                             data-transform_in="x:[-100%];z:0;rX:0deg;rY:0deg;rZ:0deg;sX:1;sY:1;skX:0;skY:0;opacity:0;s:2500;e:Power3.easeInOut;"
                             data-transform_out="auto:auto;s:1000;e:Power2.easeInOut;"
                             data-start="2000"
                             data-splitin="none"
                             data-splitout="none"
                             data-responsive_offset="on">
                            <a href="javascript:jScrollTo('.company-goal',1000)" class="service-button button-two">了解更多</a>
                        </div>
                        <!-- LAYER NR. 4 -->
                        <div class="tp-caption"
                             data-x="['left','left','left','left']" data-hoffset="['185','185','20','20']"
                             data-y="['middle','middle','middle','middle']" data-voffset="['115','115','115','170']"
                             data-transform_idle="o:1;"
                             data-transform_hover="o:1;rX:0;rY:0;rZ:0;z:0;s:300;e:Power1.easeInOut;"
                             data-transform_in="x:[100%];z:0;rX:0deg;rY:0deg;rZ:0deg;sX:1;sY:1;skX:0;skY:0;opacity:0;s:2500;e:Power3.easeInOut;"
                             data-transform_out="auto:auto;s:1000;e:Power2.easeInOut;"
                             data-start="2000"
                             data-splitin="none"
                             data-splitout="none"
                             data-responsive_offset="on">
                            <a href="javascript:_MEIQIA('showPanel')" class="about-button button-two">课程咨询</a>
                        </div>
                        <!-- LAYER NR. 4 -->
                        <div class="tp-caption"
                             data-x="['right','right','right','right']" data-hoffset="['0','0','0','0']"
                             data-y="['top','top','top','top']" data-voffset="['20','20','20','20']"
                             data-transform_idle="o:1;"
                             data-transform_hover="o:1;rX:0;rY:0;rZ:0;z:0;s:300;e:Power1.easeInOut;"
                             data-transform_in="x:[100%];z:0;rX:0deg;rY:0deg;rZ:0deg;sX:1;sY:1;skX:0;skY:0;opacity:0;s:2500;e:Power3.easeInOut;"
                             data-transform_out="auto:auto;s:1000;e:Power2.easeInOut;"
                             data-start="2000"
                             data-splitin="none"
                             data-splitout="none"
                             data-responsive_offset="on">
                            <a href="https://mp.weixin.qq.com/s?__biz=MzA5ODI5NTI5OQ==&mid=504191235&idx=1&sn=27dc7eba4ce96e23f995a114687bcec7"
                                    class="about-button button-two">
                                <img src="https://www.iqycamp.com/images/fragment/zhaopin_0228_5.png" alt="招聘" style="width: 100px;">
                            </a>
                        </div>

                        <!-- LAYER NR. 4 -->
                        <%--<div class="tp-caption"--%>
                             <%--data-x="['right','right','right','right']" data-hoffset="['50','50','50','50']"--%>
                             <%--data-y="['bottom','bottom','bottom','bottom']" data-voffset="['50','50','50','50']"--%>
                             <%--data-transform_idle="o:1;"--%>
                             <%--data-transform_hover="o:1;rX:0;rY:0;rZ:0;z:0;s:300;e:Power1.easeInOut;"--%>
                             <%--data-transform_in="x:[100%];z:0;rX:0deg;rY:0deg;rZ:0deg;sX:1;sY:1;skX:0;skY:0;opacity:0;s:2500;e:Power3.easeInOut;"--%>
                             <%--data-transform_out="auto:auto;s:1000;e:Power2.easeInOut;"--%>
                             <%--data-start="2000"--%>
                             <%--data-splitin="none"--%>
                             <%--data-splitout="none"--%>
                             <%--data-responsive_offset="on">--%>
                            <%--<a href="javascript:showDialog('#business-apply')"--%>
                               <%--class="about-button button-two">立即申请</a>--%>
                        <%--</div>--%>
                        <!-- LAYER NR. 5 -->
                        <div class="tp-caption rs-parallaxlevel-3"
                             data-x="['right','right','right','right']" data-hoffset="['0','0','-60','0']"
                             data-y="['middle','middle','bottom','middle']" data-voffset="['0','0','0','0']"
                             data-transform_idle="o:1;"
                             data-transform_hover="o:1;rX:0;rY:0;rZ:0;z:0;s:300;e:Power1.easeInOut;"
                             data-transform_in="x:[100%];z:0;rX:0deg;rY:0deg;rZ:0deg;sX:1;sY:1;skX:0;skY:0;opacity:0;s:2500;e:Power3.easeInOut;"
                             data-transform_out="auto:auto;s:1000;e:Power2.easeInOut;"
                             data-start="2200"
                             data-splitin="none"
                             data-splitout="none"
                             data-responsive_offset="on">
                            <img src="https://static.iqycamp.com/images/fragment/earth-article.jpg?imageslim" alt="圈外商学院">
                        </div>
                    </li>
                </ul>
            </div>
        </div><!-- END REVOLUTION SLIDER -->
    </div> <!--  /#banner -->

    <a id="quick-apply" href="javascript:showDialog('#business-apply')"
       class="about-button">
        <span>立即申请</span>
    </a>
    <!--
  =============================================
  Short Banner
  ==============================================
  -->
    <div class="short-banner se-p-bg-color m-bottom0">
        <div class="container">
            <h4 class="float-left">手机端随时随地升级自己，利用碎片化时间，学习体系化知识</h4>
            <div class="tips clearfix hidden-xs">
                <a class="hover-to-show button-four float-right">扫码关注</a>
                <img class="qr-code" src="https://static.iqycamp.com/images/fragment/home_middle.jpeg?imageslim" alt="圈外商学院">
            <%--<div class="words">--%>
                    <%--<span>微信扫码</span><br/>--%>
                    <%--<span>关注学习</span>--%>
                <%--</div>--%>
            </div>
        </div> <!-- /.container -->
    </div> <!-- /.short-banner -->


    <!--
    =============================================
      SEO company Goal
    ==============================================
    -->
    <section class="company-goal">
        <div class="container">
            <h2 class="text-center">圈外商学院课程项目</h2>
            <div class="row">
                <div class="col-md-3 col-xs-12 text-center">
                    <a target="_blank" href="course_project.html?jump=point1"> <div
                            class="single-goal text-center hvr-float-shadow wow fadeInUp">
                        <div class="img-icon round-border"><img
                                src="https://static.iqycamp.com/images/fragment/pc_icon_course_project_core_0213.png?imageslim" alt="核心能力项目"
                                class="round-border"></div>
                        <h5>核心能力项目</h5>
                        <ul class="ul-style text-left">
                            <li>针对企业高潜人才</li>
                            <li>一年体系化课程</li>
                            <li>三大核心能力模块：管理、思维、沟通</li>
                            <li>24小时移动端学习</li>
                            <li>80%学员高完课率，数据定期跟踪反馈</li>
                        </ul>
                    </div></a> <!-- /.single-goal -->
                </div> <!-- /.col- -->
                <div class="col-md-3 col-xs-12 text-center">
                    <a target="_blank" href="course_project.html?jump=point2"><div
                            class="single-goal text-center hvr-float-shadow wow fadeInUp" data-wow-delay="0.1s">
                        <div class="img-icon round-border"><img
                                src="https://static.iqycamp.com/images/fragment/pc_icon_course_project_biz_0213.png?imageslim" alt="商业进阶项目"
                                class="round-border"></div>
                        <h5>商业进阶项目</h5>
                        <ul class="ul-style text-left">
                            <li>针对企业技术精英和中高管</li>
                            <li>半年制mini-MBA课程</li>
                            <li>六大商业进阶模块</li>
                            <li>24小时移动端学习</li>
                            <li>顶尖商学院教授授课，定期考核和反馈</li>
                        </ul>
                    </div></a> <!-- /.single-goal -->
                </div> <!-- /.col- -->
                <div class="col-md-3 col-xs-12 text-center">
                    <a target="_blank" href="course_project.html?jump=point3"><div
                            class="single-goal text-center hvr-float-shadow wow fadeInUp" data-wow-delay="0.2s">
                        <div class="img-icon round-border"><img
                                src="https://static.iqycamp.com/images/fragment/pc_icon_course_project_camp_0213.png?imageslim" alt="专项课"
                                class="round-border"></div>
                        <h5>专项课</h5>
                        <ul class="ul-style text-left">
                            <li>专项能力提升</li>
                            <li>30天烧脑训练营</li>
                            <li>24小时移动端学习</li>
                            <li>全程督学，超高完课率</li>
                        </ul>
                    </div></a> <!-- /.single-goal -->
                </div> <!-- /.col- -->
                <div class="col-md-3 col-xs-12  text-center">
                    <a target="_blank" href="course_project.html?jump=point4"><div
                            class="single-goal text-center hvr-float-shadow wow fadeInUp" data-wow-delay="0.2s">
                        <div class="img-icon round-border"><img
                                src="https://static.iqycamp.com/images/fragment/pc_icon_course_project_enterprise_0213.png?imageslim"
                                alt="企业定制方案" class="round-border"></div>
                        <h5>企业定制方案</h5>
                        <ul class="ul-style text-left">
                            <li>思维提升、商业案例、领导力、创业投资</li>
                            <li>线上+线下结合，形式更灵活</li>
                            <li>专业能力测评，科学诊断指导</li>
                            <li>专属教练配备，全程督学辅导</li>
                        </ul>
                    </div></a> <!-- /.single-goal -->
                </div> <!-- /.col- -->
            </div> <!-- /.row -->
        </div> <!-- /.container -->
    </section> <!-- /.company-goal -->
    <div class="you-work-win">
        <div class="container">
            <h2>你负责努力，圈外商学院负责帮你赢</h2>
            <div class="row">
                <div class="col-md-4 col-xs-12">
                    <div class="icon-word-wrapper">
                        <div class="icon">
                            <img src="local/images/icon/97per.png" alt="97%"/>
                        </div>
                        <div class="word">
                            <div class="number">
                                97%
                            </div>
                            <div class="desc">
                                的学员在职业和工作方面有进步
                            </div>
                        </div>
                    </div>
                </div>
                <div class="col-md-4 col-xs-12">
                    <div class="icon-word-wrapper">
                        <div class="icon">
                            <img src="local/images/icon/19per.png" alt="97%"/>
                        </div>
                        <div class="word">
                            <div class="number">
                                19%
                            </div>
                            <div class="desc">
                                的学员实现了升职加薪或者成功转行
                            </div>
                        </div>
                    </div>
                </div>
                <div class="col-md-4 col-xs-12">
                    <div class="icon-word-wrapper">
                        <div class="icon">
                            <img src="local/images/icon/27per.png" alt="97%"/>
                        </div>
                        <div class="word">
                            <div class="number">
                                27%
                            </div>
                            <div class="desc">
                                平均涨薪幅度
                            </div>
                        </div>
                    </div>
                </div>
            </div><!-- /.row -->
            <%--<div class="row">--%>
                <%--<div class="col-md-4 col-xs-12">--%>
                    <%--<div class="icon-word-wrapper">--%>
                        <%--<div class="icon">--%>
                            <%--<img src="local/images/icon/48per.png" alt="97%"/>--%>
                        <%--</div>--%>
                        <%--<div class="word">--%>
                            <%--<div class="number">--%>
                                <%--48%--%>
                            <%--</div>--%>
                            <%--<div class="desc">--%>
                                <%--学员为资深员工与管理层--%>
                            <%--</div>--%>
                        <%--</div>--%>
                    <%--</div>--%>
                <%--</div>--%>
                <%--<div class="col-md-4 col-xs-12">--%>
                    <%--<div class="icon-word-wrapper">--%>
                        <%--<div class="icon">--%>
                            <%--<img src="local/images/icon/5年.png" alt="97%"/>--%>
                        <%--</div>--%>
                        <%--<div class="word">--%>
                            <%--<div class="number">--%>
                                <%--5<span class="unit">年</span>--%>
                            <%--</div>--%>
                            <%--<div class="desc">--%>
                                <%--学员平均工作年限--%>
                            <%--</div>--%>
                        <%--</div>--%>
                    <%--</div>--%>
                <%--</div>--%>
                <%--<div class="col-md-4 col-xs-12">--%>
                    <%--<div class="icon-word-wrapper">--%>
                        <%--<div class="icon">--%>
                            <%--<img src="local/images/icon/12.6万.png" alt="97%"/>--%>
                        <%--</div>--%>
                        <%--<div class="word">--%>
                            <%--<div class="number">--%>
                                <%--13<span class="unit">万</span>--%>
                            <%--</div>--%>
                            <%--<div class="desc">--%>
                                <%--学员平均年薪--%>
                            <%--</div>--%>
                        <%--</div>--%>
                    <%--</div>--%>
                <%--</div>--%>
            <%--</div>--%>

        </div>

    </div>


    <!--
    =============================================
      SEO Counter
    ==============================================
    -->
    <div class="seo-counter">
        <div class="main-content">
            <img src="local/images/home/object3.png" alt="背景图">
            <div class="container">
                <div class="row">
                    <div class="col-lg-4 col-md-4 col-sm-4 col-xs-12">
                        <div class="single-box">
                            <h2 class="number"><span class="timer" data-from="0" data-to="48" data-speed="1000"
                                                     data-refresh-interval="5">0</span>%</h2>
                            <p>学员为资深员工与管理层</p>
                        </div> <!-- /.single-box -->
                    </div>
                    <div class="col-lg-4 col-md-4 col-sm-4 col-xs-12">
                        <div class="single-box">
                            <h2 class="number"><span class="timer" data-from="0" data-to="5" data-speed="1000"
                                                     data-refresh-interval="5">0</span>年</h2>
                            <p>学员平均工作年限</p>
                        </div> <!-- /.single-box -->
                    </div>
                    <div class="col-lg-4 col-md-4 col-sm-4 col-xs-12">
                        <div class="single-box border-fix">
                            <h2 class="number"><span class="timer" data-from="0" data-to="13" data-speed="1000"
                                                     data-refresh-interval="5">0</span>万</h2>
                            <p>学员平均年薪</p>
                        </div> <!-- /.single-box -->
                    </div>
                </div> <!-- /.row -->
            </div> <!-- /.container -->
        </div> <!-- /.main-content -->
    </div> <!-- /.seo-counter -->

    <!--
    =============================================
      Our Service
    ==============================================
    -->
    <div class="our-service">
        <div class="container">
            <div class="seo-title-one text-center">
                <h2>体系化课程和创新教学方式，让你随时随地升级自己</h2>
                <!--<h6>We have all type SEO &amp; Marketing of service for our customer</h6>-->
                <!--<a href="service.html" class="tran3s button-four se-s-bg-color">Go to Services</a>-->
            </div> <!-- /.seo-title-one -->

            <div class="wrapper">
                <div class="row">
                    <div class="col-md-4 col-sm-6 col-xs-12">
                        <div class="single-service tran4s">
                            <img src="https://static.iqycamp.com/images/fragment/pc_icon_tixi_0214.png?imageslim" alt="课程体系">
                            <h2>01</h2>
                            <h5><a class="tran4s">课程体系</a></h5>
                            <p>课程体系基于100家各行业龙头企业的人才需求设计，并可定制自己的学习计划</p>
                        </div> <!-- /.single-service -->
                    </div> <!-- /.col- -->
                    <div class="col-md-4 col-sm-6 col-xs-12" data-wow-delay="0.1s">
                        <div class="single-service tran4s">
                            <img src="https://static.iqycamp.com/images/fragment/pc_icon_quanwei_0214_2.png?imageslim" alt="权威教育心理学专家">
                            <h2>02</h2>
                            <h5><a class="tran4s">权威教育心理学专家</a></h5>
                            <p>负责整体教学方法设计，提供最科学有效的学习方式</p>
                        </div> <!-- /.single-service -->
                    </div> <!-- /.col- -->
                    <div class="col-md-4 col-sm-6 col-xs-12" data-wow-delay="0.2s">
                        <div class="single-service tran4s">
                            <img src="https://static.iqycamp.com/images/fragment/pc_icon_fanzhuan_0214.png?imageslim" alt="翻转课堂">
                            <h2>03</h2>
                            <h5><a class="tran4s">翻转课堂</a></h5>
                            <p>充分利用碎片化时间，平均每天30分钟，3个月即对职业发展有明显效果</p>
                        </div> <!-- /.single-service -->
                    </div> <!-- /.col- -->
                    <div class="col-md-4 col-sm-6 col-xs-12" data-wow-delay="0.1s">
                        <div class="single-service tran4s">
                            <img src="https://static.iqycamp.com/images/fragment/pc_icon_baiban_0214.png?imageslim" alt="案例式教学">
                            <h2>04</h2>
                            <h5><a class="tran4s">案例式教学</a></h5>
                            <p>结合创投大赛、咨询案例赛等方式，让学习更有趣</p>
                        </div> <!-- /.single-service -->
                    </div> <!-- /.col- -->
                    <div class="col-md-4 col-sm-6 col-xs-12" data-wow-delay="0.2s">
                        <div class="single-service tran4s">
                            <img src="https://static.iqycamp.com/images/fragment/pc_icon_price_0214.png?imageslim" alt="奖学金多">
                            <h2>05</h2>
                            <h5><a class="tran4s">奖学金多</a></h5>
                            <p>还有机会成为圈外合作助教，让学习更易坚持</p>
                        </div> <!-- /.single-service -->
                    </div> <!-- /.col- -->
                    <div class="col-md-4 col-sm-6 col-xs-12" data-wow-delay="0.3s">
                        <div class="single-service tran4s">
                            <img src="https://static.iqycamp.com/images/fragment/pc_icon_shangxueyuan_0214.png?imageslim" alt="顶级商学院">
                            <h2>06</h2>
                            <h5><a class="tran4s">顶级商学院</a></h5>
                            <p>戈壁挑战赛的首个受邀在线商学院，为学员对接中欧、长江等MBA/EMBA等资源</p>
                        </div> <!-- /.single-service -->
                    </div> <!-- /.col- -->
                </div>
            </div>
        </div> <!-- /.container -->
    </div> <!-- /.our-service -->

    <!--
     =============================================
       Welocome To SEO
     ==============================================
     -->
    <div class="welcome-seo class-mate">
        <div class="container">
            <div class="class-mate-title">
                <h2>优质校友人脉为你提供更多机会</h2>
            </div>
            <div class="wrapper">
                <div class="row">
                    <div class="col-lg-6 col-md-7 col-xs-12 text wow fadeInRight float-right">
                        <h2>学员坐标一览</h2>
                        <span>覆盖亚洲、北美、欧洲和澳洲4大洲，205个城市</span>
                    </div> <!-- /.col- -->
                    <div class="col-lg-6 col-md-5 col-xs-12"><img
                            src="https://static.iqycamp.com/images/fragment/pc_icon_area_0222_2.png?imageslim" alt="学员坐标一览"></div>
                </div> <!-- /.row -->
                <div class="row">
                    <div class="col-lg-6 col-md-7 col-xs-12 text wow fadeInRight float-left">
                        <h2>学员行业一览</h2>
                        <span>50%学员来自于以下行业:<br/>
               互联网/IT、金融行业、科研/学生、咨询行业、医疗行业 <br/><br/>
              其他行业：<br/>
               政府/公共事业、房地产、通信/电子、快消品、机械/重工、能源/化工、人力资源、法律等</span>
                    </div> <!-- /.col- -->
                    <div class="col-lg-6 col-md-5 col-xs-12"><img
                            src="https://static.iqycamp.com/images/fragment/pc_icon_industry_0222_2.png?imageslim" alt="学员行业一览"></div>
                </div> <!-- /.row -->
            </div> <!-- /.wrapper -->
        </div> <!-- /.container -->
    </div> <!-- /.welcome-seo -->


    <!--
    =============================================
      Client slider
    ==============================================
    -->
    <div class="client-slider student-comment">
        <div class="container">
            <h2 class="text-center">学员评价</h2>
            <div class="sldier-wrapper">
                <div class="slider-controller"></div>
                <div class="seo-client-slider slider-container">
                    <div class="item">
                        <div class="clearfix">
                            <div class="text text-left lft">
                                <p class="rgt">
                                    “给我一个支点，我将撬动地球。”古希腊数学家阿基米德的这句名言到如今依旧还被人们传诵不绝。我对撬动地球暂时没有兴趣，目前致力于撬动人生的思考中。<br/><br/>
                                    三十岁是人生旅程中的一个重要“支点”，支点的一端是职场中我的“重量”，支点的另外一端是我的人生，当然包括我的家庭。有幸在这个重要“支点”遇见了圈外，我在圈外学到的知识、提升的能力、遇到过的圈柚，都为我杠杆的这一端增加了沉甸甸的重量，让我在三十岁的支点上撬动我的小星球。
                                </p>
                            </div>
                            <div class="role-info text-center text">
                                <img src="https://static.iqycamp.com/images/case_headimage_1.png?imageslim" class="round-border"/>
                                <h4 class="name">Roger</h4>
                                <p class="tips">
                                    网易游戏项目管理
                                </p>
                            </div>
                        </div> <!-- /.clearfix -->
                    </div> <!-- /.item -->
                    <div class="item">
                        <div class="clearfix">
                            <div class="text text-left lft">
                                <p class="rgt">
                                    都说这个时代需要终身学习才能跟上迅猛发展的变革大潮，可是在职场外有限的时间里到底学点什么才是“性价比”最高的？圈外商学院给了我们最好的答案，它为职场人打造出一套完整的知识体系，从根本上帮助职场人掌握提高个人能力的方法，让人受益良多。<br/><br/>
                                    自从加入圈外以来，我在工作中多次体会到所学方法的实用性和指导意义。同时，圈外商学院还架起了一座学校与职场之间的桥梁。在这里能够接触到人们常说的在学校里学不到的知识，而这些也是在校学生进入社会最需要的知识。
                                </p>
                            </div>
                            <div class="role-info text-center text">
                                <img src="https://static.iqycamp.com/images/case_headimage_2.png?imageslim" class="round-border"/>
                                <h4 class="name">LANLAN</h4>
                                <p class="tips">
                                    大学教授
                                </p>
                            </div>
                        </div> <!-- /.clearfix -->
                    </div> <!-- /.item -->

                    <div class="item">
                        <div class="clearfix">
                            <div class="text text-left lft">
                                <p class="rgt">
                                    这个时代最重要的职场核心竞争力就是学习力。如今的知识量以几何级增长，只有掌握了底层逻辑才能在面对不同种类的知识时都做到游刃有余。<br/><br/>
                                    那么如何做到呢？圈外商学院给了我们最好的答案。它着眼于通用能力的打造，从根基上帮我们塑造了横跨行业的学习技能，让人受益良多。<br/><br/>
                                    而圈圈也是我认识的，少有的逻辑清晰，思维全面的人。她非常善于提炼和总结，大家能在这里学到许多学校和公司里学不到的知识，因此Boy 在这里强烈推荐！
                                </p>
                            </div>
                            <div class="role-info text-center text">
                                <img src="https://static.iqycamp.com/images/case_headimage_3.png?imageslim" class="round-border"/>
                                <h4 class="name">张良计张鹏</h4>
                                <p class="tips">
                                    知名广告公司品牌战略负责人
                                </p>
                            </div>
                        </div> <!-- /.clearfix -->
                    </div> <!-- /.item -->
                </div> <!-- /.seo-client-slider -->
            </div> <!-- /.sldier-wrapper -->
        </div> <!-- /.container -->
    </div> <!-- /client-slider -->


    <!--
    =============================================
      Partner Logo
    ==============================================
    -->
    <div class="partners-section">
        <div class="container">
            <div class="seo-title-one text-center">
                <h2>合作企业</h2>
                <!--<a href="#" class="tran3s button-four se-s-bg-color">Go to Blog</a>-->
            </div> <!-- /.seo-title-one -->

            <div class="row">
                <div id="partner-logo">
                    <div class="item"><img
                            src="https://static.iqycamp.com/images/fragment/pc_icon_enterprise_tongcheng_0213.png?imageslim" alt="合作企业">
                    </div>
                    <div class="item"><img
                            src="https://static.iqycamp.com/images/fragment/pc_icon_enterprise_yinlian_0213.png?imageslim" alt="合作企业">
                    </div>
                    <div class="item"><img
                            src="https://static.iqycamp.com/images/fragment/pc_icon_enterprise_jumen_0223_1.png?imageslim" alt="合作企业">
                    </div>
                    <div class="item"><img
                            src="https://static.iqycamp.com/images/fragment/pc_icon_enterprise_changjiang_0226_2.png?imageslim" alt="合作企业">
                    </div>
                </div> <!-- End .partner_logo -->
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
<div id="business-apply" class="popup-layer">
    <div class="mask" onclick="hiddenDialog('#business-apply')"></div>
    <div class="inner">
        <div class="close" onclick="hiddenDialog('#business-apply')">
            <img src="local/images/icon/close.png"/>
        </div>
        <div class="qr-code text-center">
            <img src="https://static.iqycamp.com/images/fragment/home_to_apply_5.png?imageslim" class="qr-img">
        </div>
        <div class="tips text-center">
            暂时只支持微信申请
        </div>
    </div>
</div>
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
