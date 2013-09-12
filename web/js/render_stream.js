$(function() {
    var stats = new Stats();
    stats.setMode(0);
    stats.domElement.style.position = "absolute";
    stats.domElement.style.right = "0px";
    stats.domElement.style.top = "0px";
    document.body.appendChild(stats.domElement);

    var ws = new WebSocket("ws://localhost:8080");
    ws.onopen = function() {
        render();
        // ws.send("get_capture");
    };
    ws.onmessage = function(e) {
        imageObj.src = 'data:image/jpg;base64,' + e.data;
        // $("#image").attr('src',  'data:image/jpg;base64,'+e.data);
    };
    ws.onclose = function() {
        console.log("Connection is closed...");
    };


    var canvas = document.createElement("canvas")
            , ctx = canvas.getContext("2d")
            , imageObj = new Image()

    imageObj.onload = function() {
        stats.begin();

        ctx.save();
        ctx.clearRect(0, 0, canvas.width, canvas.height);
        ctx.drawImage(imageObj, 0, 0);
        ctx.restore();

        stats.end();
    };

    var requestAnimFrame = (function() {
        return function(callback) {
            return window.setTimeout(callback, 1000 / 25);
        };
        // return window.requestAnimationFrame || window.webkitRequestAnimationFrame || window.mozRequestAnimationFrame || window.oRequestAnimationFrame || window.msRequestAnimationFrame || function(callback) {
        //   return window.setTimeout(callback, 1000 / 60);
        // };
    })();

    var render = function() {
        ws.send("get_capture");
        return requestAnimFrame(render);
    };

    canvas.width = 320
    canvas.style.width = '800px'
    canvas.height = 200
    canvas.style.height = '600px'

    document.body.appendChild(canvas)

});

