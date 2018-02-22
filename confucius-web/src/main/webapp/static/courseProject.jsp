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
                                 style="display:inline-block;width:50px;height:50px;vertical-align: middle;"/>
                            <span style="vertical-align: middle;">圈外同学</span>
                            <!--<img src="images/logo/logo4.png" alt="Logo">-->
                        </a>
                    </div>

                    <!-- ============== Menu Warpper ================ -->
                    <div class="menu-wrapper float-right">
                        <nav id="mega-menu-holder" class="clearfix">
                            <ul class="clearfix">
                                <li><a href="/">首页</a></li>
                                <li class="active"><a href="/course_project">课程项目</a></li>
                                <li><a href="/article">文章</a></li>
                                <li><a href="/fragment/rise">线上学习</a></li>
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
    SEO company Goal
  ==============================================
  -->
    <section class="company-goal">
        <div class="container">
            <h2 class="text-center">圈外商学院课程项目</h2>
            <div class="row">
                <div class="col-md-3 col-xs-12 text-center">
                    <div class="single-goal text-center hvr-float-shadow wow fadeInUp">
                        <div class="img-icon round-border"><img src="https://static.iqycamp.com/images/fragment/pc_icon_course_project_core_0213.png?imageslim" alt="Icon" class="round-border"></div>
                        <h5>核心能力项目</h5>
                        <ul class="ul-style text-left">
                            <li>针对企业高潜人才</li>
                            <li>一年体系化课程</li>
                            <li>三大核心能力模块：管理|思维|沟通</li>
                            <li>24小时移动端学习</li>
                            <li>80%学员高完课率，数据定期跟踪反馈</li>
                        </ul>
                    </div> <!-- /.single-goal -->
                </div> <!-- /.col- -->
                <div class="col-md-3 col-xs-12 text-center">
                    <div class="single-goal text-center hvr-float-shadow wow fadeInUp" data-wow-delay="0.1s">
                        <div class="img-icon round-border"><img src="https://static.iqycamp.com/images/fragment/pc_icon_course_project_biz_0213.png?imageslim" alt="Icon" class="round-border"></div>
                        <h5>商业进阶项目</h5>
                        <ul class="ul-style text-left">
                            <li>针对企业技术精英和中高管</li>
                            <li>半年制mini-MBA课程</li>
                            <li>六大商业进阶模块</li>
                            <li>24小时移动端学习</li>
                            <li>顶尖商学院教授授课，定期考核和反馈</li>
                        </ul>
                    </div> <!-- /.single-goal -->
                </div> <!-- /.col- -->
                <div class="col-md-3 col-xs-12 text-center">
                    <div class="single-goal text-center hvr-float-shadow wow fadeInUp" data-wow-delay="0.2s">
                        <div class="img-icon round-border"><img src="https://static.iqycamp.com/images/fragment/pc_icon_course_project_camp_0213.png?imageslim" alt="Icon" class="round-border"></div>
                        <h5>专项课</h5>
                        <ul class="ul-style text-left">
                            <li>专项能力提升</li>
                            <li>30天烧脑训练营</li>
                            <li>24小时移动端学习</li>
                            <li>全程督学，超高完课率</li>
                        </ul>
                    </div> <!-- /.single-goal -->
                </div> <!-- /.col- -->
                <div class="col-md-3 col-xs-12  text-center">
                    <div class="single-goal text-center hvr-float-shadow wow fadeInUp" data-wow-delay="0.2s">
                        <div class="img-icon round-border"><img src="https://static.iqycamp.com/images/fragment/pc_icon_course_project_enterprise_0213.png?imageslim" alt="Icon" class="round-border"></div>
                        <h5>企业定制方案</h5>
                        <ul class="ul-style text-left">
                            <li>思维提升|商业案例|领导力|创业投资</li>
                            <li>线上+线下结合，形式更灵活</li>
                            <li>专业能力测评，科学诊断指导</li>
                            <li>专属教练配备，全程督学辅导</li>
                        </ul>
                    </div> <!-- /.single-goal -->
                </div> <!-- /.col- -->
            </div> <!-- /.row -->
        </div> <!-- /.container -->
    </section> <!-- /.company-goal -->


    <!--
  =============================================
  Theme Main Banner
  ==============================================
  -->

    <div id="banner">
        <div class="rev_slider_wrapper">
            <!-- START REVOLUTION SLIDER 5.0.7 auto mode -->
            <div id="seo-main-banner" class="rev_slider" data-version="5.0.7">
                <ul>
                    <!-- SLIDE1  -->
                    <li data-index="rs-280" data-transition="zoomout" data-slotamount="default" data-easein="Power4.easeInOut"
                        data-easeout="Power4.easeInOut" data-masterspeed="2000" data-rotate="0" data-saveperformance="off"
                        data-title="01" data-description="">
                        <!-- MAIN IMAGE -->
                        <img src="images/home/slide-7.jpg" alt="image" class="rev-slidebg" data-bgparallax="3"
                             data-bgposition="center center" data-duration="20000" data-ease="Linear.easeNone" data-kenburns="on"
                             data-no-retina="" data-offsetend="0 0" data-offsetstart="0 0" data-rotateend="0" data-rotatestart="0"
                             data-scaleend="100" data-scalestart="140">
                        <!-- LAYERS -->
                        <!-- LAYER NR. 1 -->
                        <div class="tp-caption"
                             data-x="['left','left','left','left']" data-hoffset="['0','0','15','5']"
                             data-y="['middle','middle','middle','middle']" data-voffset="['-120','-120','-120','-120']"
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
                            <h2>核心能力项目</h2>
                        </div>
                        <!-- LAYER NR. 2 -->
                        <div class="tp-caption"
                             data-x="['left','left','left','left']" data-hoffset="['0','0','15','5']"
                             data-y="['middle','middle','middle','middle']" data-voffset="['10','10','10','0']"
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
                            <p>
                                •   针对企业高潜人才<br/>
                                •   一年体系化课程<br/>
                                •   三大核心能力模块：管理|思维|沟通<br/>
                                •   24小时移动端学习<br/>
                                •   80%学员高完课率，数据定期跟踪反馈</p>
                        </div>
                        <!-- LAYER NR. 3 -->
                        <div class="tp-caption"
                             data-x="['left','left','left','left']" data-hoffset="['0','0','15','5']"
                             data-y="['middle','middle','middle','middle']" data-voffset="['115','115','115','130']"
                             data-transform_idle="o:1;"
                             data-transform_hover="o:1;rX:0;rY:0;rZ:0;z:0;s:300;e:Power1.easeInOut;"
                             data-transform_in="x:[-100%];z:0;rX:0deg;rY:0deg;rZ:0deg;sX:1;sY:1;skX:0;skY:0;opacity:0;s:2500;e:Power3.easeInOut;"
                             data-transform_out="auto:auto;s:1000;e:Power2.easeInOut;"
                             data-start="2000"
                             data-splitin="none"
                             data-splitout="none"
                             data-responsive_offset="on">
                            <a href="service.html" class="service-button button-two">立即申请</a>
                        </div>
                        <!-- LAYER NR. 4 -->
                        <div class="tp-caption"
                             data-x="['left','left','left','left']" data-hoffset="['170','170','185','5']"
                             data-y="['middle','middle','middle','middle']" data-voffset="['115','115','115','220']"
                             data-transform_idle="o:1;"
                             data-transform_hover="o:1;rX:0;rY:0;rZ:0;z:0;s:300;e:Power1.easeInOut;"
                             data-transform_in="x:[100%];z:0;rX:0deg;rY:0deg;rZ:0deg;sX:1;sY:1;skX:0;skY:0;opacity:0;s:2500;e:Power3.easeInOut;"
                             data-transform_out="auto:auto;s:1000;e:Power2.easeInOut;"
                             data-start="2000"
                             data-splitin="none"
                             data-splitout="none"
                             data-responsive_offset="on">
                            <a href="about-us.html" class="about-button button-two">了解更多</a>
                        </div>
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
                            <img class="tp-banner"
                                 src="https://static.iqycamp.com/images/fragment/pc_icon_course_big_core_0213_1.png?imageslim"
                                 alt="image">
                        </div>
                    </li>
                </ul>
            </div>
        </div><!-- END REVOLUTION SLIDER -->
    </div> <!--  /#banner -->


    <!--
    =============================================
      Client slider
    ==============================================
    -->
    <div class="client-slider">
        <div class="container">
            <h2 class="text-center">最实用的体系课程</h2>
            <div class="sldier-wrapper">
                <div class="slider-controller">
                </div>
                <div class="seo-course-slider slider-container ">
                    <div class="item">
                        <div class="clearfix course-img">
                            <img src="https://static.iqycamp.com/images/fragment/01月课程.png?imageslim"/>
                        </div> <!-- /.clearfix -->
                    </div> <!-- /.item -->
                    <div class="item">
                        <div class="clearfix course-img">
                            <img src="https://static.iqycamp.com/images/fragment/02月课程.png?imageslim"/>
                        </div> <!-- /.clearfix -->
                    </div> <!-- /.item -->
                    <div class="item">
                        <div class="clearfix course-img">
                            <img src="https://static.iqycamp.com/images/fragment/03月课程.png?imageslim"/>
                        </div> <!-- /.clearfix -->
                    </div> <!-- /.item -->
                    <div class="item">
                        <div class="clearfix course-img">
                            <img src="https://static.iqycamp.com/images/fragment/04月课程.png?imageslim"/>
                        </div> <!-- /.clearfix -->
                    </div> <!-- /.item -->
                    <div class="item">
                        <div class="clearfix course-img">
                            <img src="https://static.iqycamp.com/images/fragment/05月课程.png?imageslim"/>
                        </div> <!-- /.clearfix -->
                    </div> <!-- /.item -->
                    <div class="item">
                        <div class="clearfix course-img">
                            <img src="https://static.iqycamp.com/images/fragment/06月课程.png?imageslim"/>
                        </div> <!-- /.clearfix -->
                    </div> <!-- /.item -->
                    <div class="item">
                        <div class="clearfix course-img">
                            <img src="https://static.iqycamp.com/images/fragment/07月课程.png?imageslim"/>
                        </div> <!-- /.clearfix -->
                    </div> <!-- /.item -->
                    <div class="item">
                        <div class="clearfix course-img">
                            <img src="https://static.iqycamp.com/images/fragment/08月课程.png?imageslim"/>
                        </div> <!-- /.clearfix -->
                    </div> <!-- /.item -->
                    <div class="item">
                        <div class="clearfix course-img">
                            <img src="https://static.iqycamp.com/images/fragment/09月课程.png?imageslim"/>
                        </div> <!-- /.clearfix -->
                    </div> <!-- /.item -->
                    <div class="item">
                        <div class="clearfix course-img">
                            <img src="https://static.iqycamp.com/images/fragment/10月课程.png?imageslim"/>
                        </div> <!-- /.clearfix -->
                    </div> <!-- /.item -->
                    <div class="item">
                        <div class="clearfix course-img">
                            <img src="https://static.iqycamp.com/images/fragment/11月课程.png?imageslim"/>
                        </div> <!-- /.clearfix -->
                    </div> <!-- /.item -->
                    <div class="item">
                        <div class="clearfix course-img">
                            <img src="https://static.iqycamp.com/images/fragment/12月课程.png?imageslim"/>
                        </div> <!-- /.clearfix -->
                    </div> <!-- /.item -->
                </div> <!-- /.seo-client-slider -->
            </div> <!-- /.sldier-wrapper -->
            <div class="button-container text-center tp-caption">
                <a class="service-button button-two" onclick="alert('hello');">
                    立即申请
                </a>
                <a class="service-button button-two">
                    课程咨询
                </a>
            </div>
        </div> <!-- /.container -->
    </div> <!-- /client-slider -->

    <!--
              =============================================
                  Our Project
              ==============================================
              -->
    <div class="professional-course">
        <div class="container">
            <div class="seo-title-one text-center">
                <h2>权威专家设计和精选课程</h2>
            </div> <!-- /.business-title-one -->
        </div> <!-- /.container -->
        <div class="container">
            <div class="row">
                <div class="col-md-12 text-center">
                    <img class="d-ib" src="https://static.iqycamp.com/images/fragment/pc_icon_course_0222.png?imageslim" alt=""/>
                </div>
            </div>

        </div>
    </div> <!-- /.our-project -->


    <!--&lt;!&ndash;-->
    <!--=============================================-->
    <!--Welocome To SEO-->
    <!--==============================================-->
    <!--&ndash;&gt;-->
    <!--<div class="welcome-seo">-->
    <!--<div class="wrapper">-->
    <!--<div class="container">-->
    <!--<div>-->
    <!--<div class="row">-->
    <!--<div class="col-lg-6 col-md-7 col-xs-12 text wow fadeInRight float-right">-->
    <!--<h2>商业进阶项目<br/>-->
    <!--<span class="grey">（即将推出）</span></h2>-->
    <!--<p>-->
    <!--•   针对企业技术精英和中高管<br/>-->
    <!--•   半年制mini-MBA课程<br/>-->
    <!--•   六大商业进阶模块<br/>-->
    <!--•   24小时移动端学习<br/>-->
    <!--•   顶尖商学院教授授课，定期考核和反馈-->
    <!--</p>-->
    <!--<div class="button-container tp-caption">-->
    <!--<a class="button-three button ">-->
    <!--预约报名-->
    <!--</a>-->
    <!--</div>-->
    <!--</div> &lt;!&ndash; /.col- &ndash;&gt;-->
    <!--<div class="col-lg-6 col-md-5 col-xs-12"><img src="images/home/object2.png" alt="Image"></div>-->
    <!--</div> &lt;!&ndash; /.row &ndash;&gt;-->
    <!--</div> &lt;!&ndash; /.wrapper &ndash;&gt;-->
    <!--</div> &lt;!&ndash; /.container &ndash;&gt;-->
    <!--</div>-->
    <!--</div> &lt;!&ndash; /.welcome-seo &ndash;&gt;-->

    <div id="bs-banner">
        <div class="rev_slider_wrapper">
            <!-- START REVOLUTION SLIDER 5.0.7 auto mode -->
            <div id="seo-bs-banner" class="rev_slider" data-version="5.0.7">
                <ul>
                    <!-- SLIDE1  -->
                    <li data-index="rs-280" data-transition="zoomout" data-slotamount="default" data-easein="Power4.easeInOut"
                        data-easeout="Power4.easeInOut" data-masterspeed="2000" data-rotate="0" data-saveperformance="off"
                        data-title="01" data-description="">
                        <!-- MAIN IMAGE -->
                        <img src="images/home/slide-7.jpg" alt="image" class="rev-slidebg" data-bgparallax="3"
                             data-bgposition="center center" data-duration="20000" data-ease="Linear.easeNone" data-kenburns="on"
                             data-no-retina="" data-offsetend="0 0" data-offsetstart="0 0" data-rotateend="0" data-rotatestart="0"
                             data-scaleend="100" data-scalestart="140">
                        <!-- LAYERS -->
                        <!-- LAYER NR. 1 -->
                        <div class="tp-caption text-right"
                             data-x="['right','right','right','right']" data-hoffset="['40','40','40','40']"
                             data-y="['middle','middle','middle','middle']" data-voffset="['-150','-150','-150','-150']"
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
                            <h3>商业进阶项目<br/>
                                <span class="gray">（即将推出）</span>
                            </h3>
                        </div>
                        <!-- LAYER NR. 2 -->
                        <div class="tp-caption text-right"
                             data-x="['right','right','right','right']" data-hoffset="['0','0','15','5']"
                             data-y="['middle','middle','middle','middle']" data-voffset="['10','10','10','0']"
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
                            <div class="d-ib tips text-left">
                                <p>
                                    •   针对企业技术精英和中高管<br/>
                                    •   半年制mini-MBA课程<br/>
                                    •   六大商业进阶模块<br/>
                                    •   24小时移动端学习<br/>
                                    •   顶尖商学院教授授课，定期考核和反馈
                                </p>
                            </div>
                        </div>
                        <!-- LAYER NR. 3 -->
                        <div class="tp-caption"
                             data-x="['right','right','right','right']" data-hoffset="['160','160','160','160']"
                             data-y="['middle','middle','middle','middle']" data-voffset="['170','170','170','170']"
                             data-transform_idle="o:1;"
                             data-transform_hover="o:1;rX:0;rY:0;rZ:0;z:0;s:300;e:Power1.easeInOut;"
                             data-transform_in="x:[-100%];z:0;rX:0deg;rY:0deg;rZ:0deg;sX:1;sY:1;skX:0;skY:0;opacity:0;s:2500;e:Power3.easeInOut;"
                             data-transform_out="auto:auto;s:1000;e:Power2.easeInOut;"
                             data-start="2000"
                             data-splitin="none"
                             data-splitout="none"
                             data-responsive_offset="on">
                            <a href="service.html" class="button button-two">预约报名</a>
                        </div>
                        <!-- LAYER NR. 5 -->
                        <div class="tp-caption rs-parallaxlevel-3"
                             data-x="['left','left','left','left']" data-hoffset="['0','0','-60','0']"
                             data-y="['middle','middle','bottom','middle']" data-voffset="['0','0','0','0']"
                             data-transform_idle="o:1;"
                             data-transform_hover="o:1;rX:0;rY:0;rZ:0;z:0;s:300;e:Power1.easeInOut;"
                             data-transform_in="x:[100%];z:0;rX:0deg;rY:0deg;rZ:0deg;sX:1;sY:1;skX:0;skY:0;opacity:0;s:2500;e:Power3.easeInOut;"
                             data-transform_out="auto:auto;s:1000;e:Power2.easeInOut;"
                             data-start="2200"
                             data-splitin="none"
                             data-splitout="none"
                             data-responsive_offset="on">
                            <img  class="tp-banner"
                                  src="https://static.iqycamp.com/images/fragment/pc_icon_course_0222_2.png?imageslim" alt="image">
                        </div>
                    </li>
                </ul>
            </div>
        </div><!-- END REVOLUTION SLIDER -->
    </div> <!--  /#banner -->


    <div id="camp-banner">
        <div class="rev_slider_wrapper">
            <!-- START REVOLUTION SLIDER 5.0.7 auto mode -->
            <div id="seo-camp-banner" class="rev_slider" data-version="5.0.7">
                <ul>
                    <!-- SLIDE1  -->
                    <li data-index="rs-280" data-transition="zoomout" data-slotamount="default" data-easein="Power4.easeInOut"
                        data-easeout="Power4.easeInOut" data-masterspeed="2000" data-rotate="0" data-saveperformance="off"
                        data-title="01" data-description="">
                        <!-- MAIN IMAGE -->
                        <img src="images/home/slide-7.jpg" alt="image" class="rev-slidebg" data-bgparallax="3"
                             data-bgposition="center center" data-duration="20000" data-ease="Linear.easeNone" data-kenburns="on"
                             data-no-retina="" data-offsetend="0 0" data-offsetstart="0 0" data-rotateend="0" data-rotatestart="0"
                             data-scaleend="100" data-scalestart="140">
                        <!-- LAYERS -->
                        <!-- LAYER NR. 1 -->
                        <div class="tp-caption"
                             data-x="['left','left','left','left']" data-hoffset="['40','40','40','40']"
                             data-y="['middle','middle','middle','middle']" data-voffset="['-150','-150','-150','-150']"
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
                            <h3 class="right">[专项课]结构化表达</h3>
                        </div>
                        <!-- LAYER NR. 2 -->
                        <div class="tp-caption"
                             data-x="['left','left','left','left']" data-hoffset="['0','0','15','5']"
                             data-y="['middle','middle','middle','middle']" data-voffset="['10','10','10','0']"
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
                            <p>
                                •  专项能力提升<br/>
                                •  30天烧脑训练营<br/>
                                •  24小时移动端学习<br/>
                                •  全程督学，超高完课率
                            </p>
                        </div>
                        <!-- LAYER NR. 3 -->
                        <div class="tp-caption"
                             data-x="['left','left','left','left']" data-hoffset="['0','0','15','5']"
                             data-y="['middle','middle','middle','middle']" data-voffset="['170','170','170','170']"
                             data-transform_idle="o:1;"
                             data-transform_hover="o:1;rX:0;rY:0;rZ:0;z:0;s:300;e:Power1.easeInOut;"
                             data-transform_in="x:[-100%];z:0;rX:0deg;rY:0deg;rZ:0deg;sX:1;sY:1;skX:0;skY:0;opacity:0;s:2500;e:Power3.easeInOut;"
                             data-transform_out="auto:auto;s:1000;e:Power2.easeInOut;"
                             data-start="2000"
                             data-splitin="none"
                             data-splitout="none"
                             data-responsive_offset="on">
                            <a href="service.html" class="button button-two">立即申请</a>
                        </div>
                        <!-- LAYER NR. 4 -->
                        <div class="tp-caption"
                             data-x="['left','left','left','left']" data-hoffset="['170','170','185','5']"
                             data-y="['middle','middle','middle','middle']" data-voffset="['170','170','170','170']"
                             data-transform_idle="o:1;"
                             data-transform_hover="o:1;rX:0;rY:0;rZ:0;z:0;s:300;e:Power1.easeInOut;"
                             data-transform_in="x:[100%];z:0;rX:0deg;rY:0deg;rZ:0deg;sX:1;sY:1;skX:0;skY:0;opacity:0;s:2500;e:Power3.easeInOut;"
                             data-transform_out="auto:auto;s:1000;e:Power2.easeInOut;"
                             data-start="2000"
                             data-splitin="none"
                             data-splitout="none"
                             data-responsive_offset="on">
                            <a href="about-us.html" class="button button-two">了解更多</a>
                        </div>
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
                            <img  class="tp-banner"
                                  src="https://static.iqycamp.com/images/fragment/pc_icon_course_0222_3.png?imageslim" alt="image">
                        </div>
                    </li>
                </ul>
            </div>
        </div><!-- END REVOLUTION SLIDER -->
    </div> <!--  /#banner -->

    <div id="camp-two-banner">
        <div class="wrapper">
            <div class="rev_slider_wrapper">
                <!-- START REVOLUTION SLIDER 5.0.7 auto mode -->
                <div id="seo-camp-two-banner" class="rev_slider" data-version="5.0.7">
                    <ul>
                        <!-- SLIDE1  -->
                        <li data-index="rs-280" data-transition="zoomout" data-slotamount="default" data-easein="Power4.easeInOut"
                            data-easeout="Power4.easeInOut" data-masterspeed="2000" data-rotate="0" data-saveperformance="off"
                            data-title="01" data-description="">
                            <!-- MAIN IMAGE -->
                            <img src="images/home/slide-7.jpg" alt="image" class="rev-slidebg" data-bgparallax="3"
                                 data-bgposition="center center" data-duration="20000" data-ease="Linear.easeNone" data-kenburns="on"
                                 data-no-retina="" data-offsetend="0 0" data-offsetstart="0 0" data-rotateend="0" data-rotatestart="0"
                                 data-scaleend="100" data-scalestart="140">
                            <!-- LAYERS -->
                            <!-- LAYER NR. 1 -->
                            <div class="tp-caption text-right"
                                 data-x="['right','right','right','right']" data-hoffset="['40','40','40','40']"
                                 data-y="['middle','middle','middle','middle']" data-voffset="['-150','-150','-150','-150']"
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
                                <h3>
                                    <span class="grey">[专项课]高效学习</span>
                                </h3>
                            </div>
                            <!-- LAYER NR. 2 -->
                            <div class="tp-caption text-right"
                                 data-x="['right','right','right','right']" data-hoffset="['0','0','15','5']"
                                 data-y="['middle','middle','middle','middle']" data-voffset="['10','10','10','0']"
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
                                <div class="d-ib tips text-left">
                                    <p>
                                        •  专项能力提升<br/>
                                        •  30天烧脑训练营<br/>
                                        •  24小时移动端学习<br/>
                                        •  全程督学，超高完课率
                                    </p>
                                </div>
                            </div>
                            <!-- LAYER NR. 3 -->
                            <div class="tp-caption"
                                 data-x="['right','right','right','right']" data-hoffset="['0','0','15','5']"
                                 data-y="['middle','middle','middle','middle']" data-voffset="['170','170','170','170']"
                                 data-transform_idle="o:1;"
                                 data-transform_hover="o:1;rX:0;rY:0;rZ:0;z:0;s:300;e:Power1.easeInOut;"
                                 data-transform_in="x:[-100%];z:0;rX:0deg;rY:0deg;rZ:0deg;sX:1;sY:1;skX:0;skY:0;opacity:0;s:2500;e:Power3.easeInOut;"
                                 data-transform_out="auto:auto;s:1000;e:Power2.easeInOut;"
                                 data-start="2000"
                                 data-splitin="none"
                                 data-splitout="none"
                                 data-responsive_offset="on">
                                <a href="service.html" class="button button-two">立即申请</a>
                            </div>
                            <!-- LAYER NR. 4 -->
                            <div class="tp-caption"
                                 data-x="['right','right','right','right']" data-hoffset="['170','170','185','5']"
                                 data-y="['middle','middle','middle','middle']" data-voffset="['170','170','170','170']"
                                 data-transform_idle="o:1;"
                                 data-transform_hover="o:1;rX:0;rY:0;rZ:0;z:0;s:300;e:Power1.easeInOut;"
                                 data-transform_in="x:[100%];z:0;rX:0deg;rY:0deg;rZ:0deg;sX:1;sY:1;skX:0;skY:0;opacity:0;s:2500;e:Power3.easeInOut;"
                                 data-transform_out="auto:auto;s:1000;e:Power2.easeInOut;"
                                 data-start="2000"
                                 data-splitin="none"
                                 data-splitout="none"
                                 data-responsive_offset="on">
                                <a href="about-us.html" class="button button-two">了解更多</a>
                            </div>
                            <!-- LAYER NR. 5 -->
                            <div class="tp-caption rs-parallaxlevel-3"
                                 data-x="['left','left','left','left']" data-hoffset="['0','0','-60','0']"
                                 data-y="['middle','middle','bottom','middle']" data-voffset="['0','0','0','0']"
                                 data-transform_idle="o:1;"
                                 data-transform_hover="o:1;rX:0;rY:0;rZ:0;z:0;s:300;e:Power1.easeInOut;"
                                 data-transform_in="x:[100%];z:0;rX:0deg;rY:0deg;rZ:0deg;sX:1;sY:1;skX:0;skY:0;opacity:0;s:2500;e:Power3.easeInOut;"
                                 data-transform_out="auto:auto;s:1000;e:Power2.easeInOut;"
                                 data-start="2200"
                                 data-splitin="none"
                                 data-splitout="none"
                                 data-responsive_offset="on">
                                <img class="tp-banner"
                                     src="https://static.iqycamp.com/images/fragment/pc_icon_course_0222_4.png?imageslim" alt="image">
                            </div>
                        </li>
                    </ul>
                </div>
            </div><!-- END REVOLUTION SLIDER -->
        </div>
    </div> <!--  /#banner -->

    <!--
    =============================================
      Our Service
    ==============================================
    -->
    <div id="company-banner">
        <div class="rev_slider_wrapper">
            <!-- START REVOLUTION SLIDER 5.0.7 auto mode -->
            <div id="seo-company-banner" class="rev_slider" data-version="5.0.7">
                <ul>
                    <!-- SLIDE1  -->
                    <li data-index="rs-280" data-transition="zoomout" data-slotamount="default" data-easein="Power4.easeInOut"
                        data-easeout="Power4.easeInOut" data-masterspeed="2000" data-rotate="0" data-saveperformance="off"
                        data-title="01" data-description="">
                        <!-- MAIN IMAGE -->
                        <img src="images/home/slide-7.jpg" alt="image" class="rev-slidebg" data-bgparallax="3"
                             data-bgposition="center center" data-duration="20000" data-ease="Linear.easeNone" data-kenburns="on"
                             data-no-retina="" data-offsetend="0 0" data-offsetstart="0 0" data-rotateend="0" data-rotatestart="0"
                             data-scaleend="100" data-scalestart="140">
                        <!-- LAYERS -->
                        <!-- LAYER NR. 1 -->
                        <div class="tp-caption"
                             data-x="['left','left','left','left']" data-hoffset="['0','0','15','5']"
                             data-y="['middle','middle','middle','middle']" data-voffset="['-150','-150','-150','-150']"
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
                            <h3>
                                <span class="grey">企业解决方案</span>
                            </h3>
                        </div>
                        <!-- LAYER NR. 2 -->
                        <div class="tp-caption"
                             data-x="['left','left','left','left']" data-hoffset="['0','0','15','5']"
                             data-y="['middle','middle','middle','middle']" data-voffset="['10','10','10','0']"
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
                            <p>
                                •  大规模提升员工能力和效率<br/>
                                •  99%成本节省<br/>
                                •  80%的高完课率<br/>
                                •  学习数据可监测<br/>
                                •  线上线下结合<br/>
                                •  灵活学习 时间<br/>
                                •  贴近工作场景的前沿实用课程
                            </p>
                        </div>
                        <!-- LAYER NR. 3 -->
                        <div class="tp-caption"
                             data-x="['left','left','left','left']" data-hoffset="['0','0','0','0']"
                             data-y="['middle','middle','middle','middle']" data-voffset="['170','170','170','170']"
                             data-transform_idle="o:1;"
                             data-transform_hover="o:1;rX:0;rY:0;rZ:0;z:0;s:300;e:Power1.easeInOut;"
                             data-transform_in="x:[-100%];z:0;rX:0deg;rY:0deg;rZ:0deg;sX:1;sY:1;skX:0;skY:0;opacity:0;s:2500;e:Power3.easeInOut;"
                             data-transform_out="auto:auto;s:1000;e:Power2.easeInOut;"
                             data-start="2000"
                             data-splitin="none"
                             data-splitout="none"
                             data-responsive_offset="on">
                            <a href="service.html" class="button button-two">企业方案咨询</a>
                        </div>
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
                            <img class="tp-banner"
                                 src="https://static.iqycamp.com/images/fragment/pc_icon_course_0222_5.png?imageslim" alt="image">
                        </div>
                    </li>
                </ul>
            </div>
        </div><!-- END REVOLUTION SLIDER -->
    </div> <!--  /#banner -->
    <div class="our-service bg-color">
        <div class="container">
            <!--<div class="seo-title-one text-center">-->
            <!--<h2>Service We Provide</h2>-->
            <!--<h6>We have all type SEO &amp; Marketing of service for our customer</h6>-->
            <!--</div> &lt;!&ndash; /.seo-title-one &ndash;&gt;-->
            <div class="row">
                <div class="col-md-6 col-sm-6 col-xs-12 wow fadeInUp">
                    <div class="single-service tran4s">
                        <img src="https://static.iqycamp.com/images/fragment/pc_icon_course_0222_6.png?imageslim" alt="icon">
                        <h2>01</h2>
                        <h5><a href="service-details.html" class="tran4s">1.商学院项目（1-2年，线上）</a></h5>
                        <p>1.1 核心能力项目 - 针对有2-8年职场经验的核心员工和管理人员</p>
                        <p>1.2 商业管理项目 - 针对有5-10年职场经验的中高级管理层</p>
                    </div> <!-- /.single-service -->
                </div> <!-- /.col- -->
                <div class="col-md-6 col-sm-6 col-xs-12 wow fadeInUp" data-wow-delay="0.1s">
                    <div class="single-service tran4s">
                        <img src="https://static.iqycamp.com/images/fragment/pc_icon_course_0222_7.png?imageslim" alt="icon">
                        <h2>02</h2>
                        <h5><a href="service-details.html" class="tran4s">2. Mini-MBA项目（6个月，线上+线下）</a></h5>
                        <p>与国内外顶尖商科院校联合开发的商业管理课程， 针对企业技术精英、中高管理层</p>
                    </div> <!-- /.single-service -->
                </div> <!-- /.col- -->
                <div class="col-md-6 col-sm-6 col-xs-12 wow fadeInUp" data-wow-delay="0.2s">
                    <div class="single-service tran4s">
                        <img src="https://static.iqycamp.com/images/fragment/pc_icon_course_0222_8.png?imageslim" alt="icon">
                        <h2>03</h2>
                        <h5><a href="service-details.html" class="tran4s">3. 定制化企业培训</a></h5>
                        <p>思维提升、商业案例、领导力等模块化精品课程，精准触达企业痛点</p>
                    </div> <!-- /.single-service -->
                </div> <!-- /.col- -->
                <div class="col-md-6 col-sm-6 col-xs-12 wow fadeInUp" data-wow-delay="0.1s">
                    <div class="single-service tran4s">
                        <img src="https://static.iqycamp.com/images/fragment/pc_icon_course_0222_9.png?imageslim" alt="icon">
                        <h2>04</h2>
                        <h5><a href="service-details.html" class="tran4s">4. 私董会</a></h5>
                        <p>
                            针对企业高管和行业专家<br/>
                            会员制度，定期认知升级<br/>
                            商业案例实战，解决真实问题<br/>
                            高管教练和行业大咖加持
                        </p>
                    </div> <!-- /.single-service -->
                </div> <!-- /.col- -->
            </div>
        </div> <!-- /.container -->
    </div> <!-- /.our-service -->


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
                    <div class="item"><img src="https://static.iqycamp.com/images/fragment/pc_icon_enterprise_tongcheng_0213.png?imageslim" alt="logo"></div>
                    <div class="item"><img src="https://static.iqycamp.com/images/fragment/pc_icon_enterprise_yinlian_0213.png?imageslim" alt="logo"></div>
                </div> <!-- End .partner_logo -->
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
            <div class="top-footer row">
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
                            <img src="images/team/4.jpg" alt="圈外孙圈圈">
                            <div class="name">圈外孙圈圈</div>
                        </div>
                        <div class="item text-center">
                            <img src="images/team/4.jpg" alt="圈外同学">
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
    <button class="scroll-top tran3s hvr-shutter-out-horizontal">
        <i class="fa fa-angle-up" aria-hidden="true"></i>
    </button>


    <!-- Js File_________________________________ -->
    <!-- j Query -->
    <script type="text/javascript" src="vendor/jquery.2.2.3.min.js"></script>

    <!-- Bootstrap JS -->
    <script type="text/javascript" src="vendor/bootstrap/bootstrap.min.js"></script>

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
    <!-- Vendor js _________ -->
    <!-- menu  -->
    <script type="text/javascript" src="vendor/menu/src/js/jquery.slimmenu.js"></script>
    <script type="text/javascript" src="vendor/jquery.easing.1.3.js"></script>

    <!-- MixitUp -->
    <script type="text/javascript" src="vendor/jquery.mixitup.min.js"></script>

    <!-- WOW js -->
    <script type="text/javascript" src="vendor/WOW-master/dist/wow.min.js"></script>
    <!-- owl.carousel -->
    <script type="text/javascript" src="vendor/owl-carousel/owl.carousel.min.js"></script>

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