<!DOCTYPE html>
<%@ page session="true"%>
<%@ page import="org.jboss.mjolnir.server.TokenUtil" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <link type="text/css" rel="stylesheet" href="mjolnir.css">
    <script type="text/javascript">
        var info = {
            "xsrf": "<%=TokenUtil.getToken(request.getSession().getId())%>"
        };
    </script>
    <title>Mjolnir web-app</title>
    <script type="text/javascript" language="javascript" src="mjolnir/mjolnir.nocache.js"></script>
</head>
<body>
    <iframe src="javascript:''" id="__gwt_historyFrame" tabIndex='-1' style="position:absolute;width:0;height:0;border:0"></iframe>
    <noscript>
        <div style="width: 22em; position: absolute; left: 50%; margin-left: -11em; color: red;
         background-color: white; border: 1px solid red; padding: 4px; font-family: sans-serif">
            Your browser must have JavaScript enabled for this application to run.
        </div>
    </noscript>

    <!-- Now we can actually display stuff -->
    <h1>Hello. Welcome to Mjolnir.</h1>

    <table align="center">
        <tr>
            <td id="loginPanelContainer"></td>
        </tr>
        <tr>
            <td id="subscriptionPanelContainer"></td>
        </tr>
    </table>

</body>
</html>