<html>
<head>
<#include "includes/head.flt">
</head>
<body>
<#include "includes/navbar.flt">
<div class="container">
    <div class="main">
        <div class="row">
            <h1>Umfrage</h1>
            <h3>${poll.getTitle()}</h3>
            <p><i>${poll.getDescription()}</i></p>
            <#include "includes/success.flt">
            </div>
    </div>
</div>
<#include "includes/modal.flt">
</body>
</html>