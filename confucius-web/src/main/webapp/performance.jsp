<!DOCTYPE html>
<html>
    <head>
        <meta charset=utf-8><title>performance-web</title>
        <%--todo 更改为对应上传app.css 的地址--%>
        <link href="/static/css/app.css" rel=stylesheet>
    </head>
    <body>
        <div id=app>
            <router-view>
            </router-view>uno
        </div>
        <%--todo 更改为对应上传js 的地址--%>
        <script type="text/javascript" src="/static/js/manifest.js"></script>
        <script type="text/javascript" src="/static/js/vendor.js"></script>
        <script type="text/javascript" src="/static/js/app.js"></script>
    </body>
</html>