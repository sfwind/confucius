<!doctype html>
<%@ page import="com.iquanwai.confucius.biz.util.ConfigUtils" contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"%>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta http-equiv="x-ua-compatible" content="ie=edge">
    <title>圈外</title>
    <meta name="keywords" content="圈外,RISE">
    <meta name="description" content="圈外训练营">
    <link href="//www.iqycamp.com/images/logo.png" rel="shortcut icon">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0">
    <script>
        var addRippleEffect = function (e) {
            var target = e.target;
            if (target.tagName.toLowerCase() !== 'button') return false;
            var rect = target.getBoundingClientRect();
            var ripple = target.querySelector('.ripple');
            if (!ripple) {
                ripple = document.createElement('span');
                ripple.className = 'ripple';
                ripple.style.height = ripple.style.width = Math.max(rect.width, rect.height) + 'px';
                target.appendChild(ripple);
            }
            ripple.classList.remove('show');
            var top = e.pageY - rect.top - ripple.offsetHeight / 2 - document.body.scrollTop;
            var left = e.pageX - rect.left - ripple.offsetWidth / 2 - document.body.scrollLeft;
            ripple.style.top = top + 'px';
            ripple.style.left = left + 'px';
            ripple.classList.add('show');
            return false;
        }
        document.addEventListener('mousedown', addRippleEffect, false);
        function openUrl(){
            // RISE跳转地址
            location.href="//www.iquanwai.com/fragment/rise"
        }
    </script>
    <style>
        button {
            position: relative;
            display: block;
            border: none;
            outline: none;
            /*letter-spacing: .2em;*/
            font-weight: bold;
            cursor: pointer;
            overflow: hidden;
            user-select: none;
            background: #fff;
        }
        .ripple {
            position: absolute;
            background: rgba(0,0,0,.15);
            border-radius: 100%;
            transform: scale(0);
            pointer-events: none;
        }
        .ripple.show {
            animation: ripple .75s ease-out;
        }
        @keyframes ripple {
            to {
                transform: scale(2);
                opacity: 0;
            }
        }
        body {
            background-color: #f5f5f5;
            margin: 0;
            padding: 0;
            font-size: 0;
        }
        .top {
            background: #fff;
            width: 100%;
            padding: 0;
        }
        .head {
            width: 1000px;
            padding: 0;
            margin: 0px auto;
            height: 80px;
            display: block;
        }
        .imgL {
            width: 36px;
            margin-top: 17px;
            margin-bottom: 25px;
            vertical-align: middle;
        }
        .textL {
            margin-left: 5px;
            margin-right: 55px;
            line-height: 80px;
            font-size: 40px;
            vertical-align: middle;
            color: #55cbcb;
            font-family: "\601D\6E90\9ED1\4F53 cn", "Helvetica Neue" !important;
        }
        .menu {
            margin: 21px 24px;
            display: inline-block;
            padding: 0px 16px;
            line-height: 36px;
            text-transform: uppercase;
            font-weight: 300;
            font-size: 20px;
            font-family: 思源黑体cn, "Helvetica Neue";
            color: rgb(85, 203, 203);
            vertical-align: middle;
            cursor: pointer;
            user-select: none;
            text-decoration: none;
        }
        .init {
            color: rgba(0, 0, 0, 0.870588);
        }
        .menu:hover {
            background: rgba(153, 153, 153, 0.2);
            border-radius: 2px;
        }
        .mid {
            width: 100%;
            margin-top: 1px;
            background: #fff;
        }
        .content {
            width: 1000px;
            margin: 0 auto;
            padding-top: 70px;
            display: block;
        }
        .text {
            font-size: 36px;
            color: #666666;
            height: 136px;
            line-height: 136px;
            vertical-align: middle;
            text-align: center;
            display: block;
            font-family: "\601D\6E90\9ED1\4F53 cn", "Helvetica Neue" !important;
        }
        .text::before {
            display: inline-block;
            position: relative;
            content: '';
            border: 1px solid #cccccc;
            border-right: none;
            width: 100px;
            height: 110px;
            vertical-align: middle;
            left: 34px;
        }
        .text::after {
            display: inline-block;
            position: relative;
            content: '';
            border: 1px solid #cccccc;
            border-left: none;
            width: 100px;
            height: 110px;
            vertical-align: middle;
            right: 34px;
        }
        .intro-card-list {
            margin-top: 60px;
            padding: 0 104px;
            height: 320px;
            font-family: "\601D\6E90\9ED1\4F53 cn", "Helvetica Neue" !important;
        }
        .intro-card-list > .intro-card {
            display: inline-block;
            width: 120px;
            height: 180px;
            text-align: center;
            cursor: default;
            border-radius: 10px;
            padding-top: 20px;
            vertical-align: top;
        }
        .intro-card-list > .intro-card + .intro-card {
            margin-left: 95px;
        }
        .intro-card:hover {
            box-shadow: 0 0 8px rgba(153, 153, 153, 0.32);
        }
        .title {
            font-size: 20px;
            color: #666666;
            margin-bottom: 10px;
        }
        .intro {
            font-size: 16px;
            color: #999999;
            line-height: 30px;
        }
        .qrcode {
            display: none;
            position: relative;
            top: 50px;
            width: 120px;
            height: 120px;
            text-align: center;
        }
        .intro-card:hover > .qrcode {
            display: block;
        }
        .qrcode img {
            width: 110px;
            height: 110px;
        }
        .footer-img {
            display: block;
            width: 100%;
            background: #fff;
        }
        .footer {
            background-color: #393838;
            height: 150px;
            font-size: 14px;
            color: #cccccc;
            text-align: center;
            position: relative;
        }
        .footer .footer-container {
            padding: 25px 0;
            position: relative;
            width: 1000px;
            margin: 0 auto;
        }
        .footer-container > * {
            display: inline-block;
            vertical-align: middle;
        }
        .footer-container > .img-item > img {
            display: block;
            width: 90px;
            height: 90px;
        }
        .footer-container > .img-item > div {
            font-size: 12px;
            margin-top: 10px;
        }
        .footer-container .email {
            cursor: pointer;
        }
        .email > .email-link {
            display: none;
            position: absolute;
            top: -41px;
            left: -22px;
            padding: 10px;
            border: 1px solid #ccc;
            border-radius: 5px;
        }
        .email:hover > .email-link {
            display: block;
        }
    </style>
</head>
<body>
<div class="top">
    <div class="head">
        <img class="imgL" alt="圈外logo" src="//www.iqycamp.com/images/logo.png"/>
        <span class="textL">圈外</span>
        <button class="menu">首页</button>
        <button class="menu init" onclick="openUrl()">RISE</button>
    </div>
</div>
<div class="mid">
    <div class="content">
        <div class="text">
            为每一个想成长的人提供价值
        </div>
        <ul class="intro-card-list">
            <li class="intro-card">
                <div class="title">RISE</div>
                <div class="intro">解决问题</div>
                <div class="intro">提升能力</div>
                <div class="intro">养成习惯</div>
                <div class="intro">交流经验</div>
                <div class="qrcode"><img alt="RISE二维码" src="//www.iqycamp.com/images/serverQrCode.jpg"></div>
            </li>
            <li class="intro-card">
                <div class="title">训练营</div>
                <div class="intro">升级思维</div>
                <div class="intro">翻转课堂</div>
                <div class="intro">实操案例</div>
                <div class="intro">跟进复盘</div>
                <div class="qrcode"><img alt="训练营二维码" src="//www.iqycamp.com/images/serverQrCode.jpg"></div>
            </li>
            <li class="intro-card">
                <div class="title">同路人</div>
                <div class="intro">认识伙伴</div>
                <div class="intro">共同学习</div>
                <div class="intro">互相鼓励</div>
                <div class="intro">对接资源</div>
                <div class="qrcode"><img alt="同路人二维码" src="//www.iqycamp.com/images/serverQrCode.jpg"></div>
            </li>
            <li class="intro-card">
                <div class="title">烧脑文</div>
                <div class="intro">思维方法</div>
                <div class="intro">沟通技巧</div>
                <div class="intro">职业种种</div>
                <div class="intro">自我管理</div>
                <div class="qrcode"><img alt="烧脑文二维码" src="//www.iqycamp.com/images/subscribeCode.jpg"></div>
            </li>
        </ul>

    </div>
</div>
<img class="footer-img" src="//www.iqycamp.com/images/pc/index_panel2.png"/>
<div class="footer">
    <div class="footer-container">
        <div class="img-item"><img alt="圈外订阅号二维码" src="//www.iqycamp.com/images/subscribeCode.jpg">
            <div>圈外订阅号</div>
        </div>
        <div class="img-item" style="margin-right: -93px;"><img alt="圈外服务号二维码" src="//www.iqycamp.com/images/serverQrCode.jpg">
            <div>圈外服务号</div>
        </div>
        <div style="margin-top: 30px; float: right; margin-left: 30px;">
            <a target="_blank"
               rel="nofollow"
               href="https://book.douban.com/subject/26936065/"
               style="text-decoration: none; color: rgb(255, 255, 255);">圈圈的书</a>
        </div>
        <div style="margin-top: 30px; float: right;">
            <div class="email" style="text-decoration: none; color: rgb(255, 255, 255); position: relative;">
                意见反馈
                <div class="email-link">iquanwaivip@163.com</div>
            </div>
        </div>
        <div
                style="font-size: 12px; position: absolute; width: 280px; top: 76px; right: -69px; color: rgb(204, 204, 204);">
            <a rel="nofollow" href="//www.miitbeian.gov.cn/"
               style="text-decoration: none; color: rgb(204, 204, 204);">ICP备15006409号</a></div>
    </div>
</div>

<script>
    window.ENV = {
        reactMountPoint: "react-app",
        userName: "${userName}",
        headImage:"${headImage}",
        loginSocketUrl:"${loginSocketUrl}",
        isDevelopment:${isDevelopment},
        feedBack:${feedBack},
        openFeedBack:${openFeedBack},
        openComment:${openComment}

    }
</script>

<div id="react-app"></div>
<!-- 业务代码-->
<script src="${resource}"></script>

<script>
    <%--(function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){--%>
            <%--(i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),--%>
        <%--m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)--%>
    <%--})(window,document,'script','https://www.google-analytics.com/analytics.js','ga');--%>

    <%--ga('create', '<%=ConfigUtils.gaId()%>', 'auto');--%>
    <%--ga('send', 'pageview');--%>
    var display = '<%=ConfigUtils.domainName()%>'
    if(display === 'http://www.iquanwai.com') {
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