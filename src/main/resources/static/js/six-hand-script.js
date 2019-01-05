$("document").ready(function(){
    $(".notification").hide();
    $(".notification-server").hide();
    $(".notification-server-success").hide();
    
    
        
    function relationInserter(data){
        var source = data.source, target = data.target;
        var relation_element = '<div class="row center-xs relation-block-row"><div class="col-xs-10"><div class="relation-connection"><div class="first-user-name-block"><p>' + source.firstName + ' ' +source.lastName + '</p></div><div class="handshake-view"><div><p class="relations-number">' + data.count + '<p></div><div class="shaking-img"></div></div><div class="second-user-name-block"><p>' + target.firstName + ' ' + target.lastName + '</p></div></div></div></div>';
        var x = document.createElement('div');
        x.innerHTML = relation_element;
        $(".relation-block").append(x);
        $(x).hide().fadeIn({duration:500});
    }
    
    
    function searchValidator(){
        if (/http\w*:\/\/vk.com\/\w+/.test($("input")[4].value)){
            $(".notification").fadeOut({duration:500});
            $.ajax({
                url: "api/results/get",
                method: "GET",
                data:{
                    user_id: $("input")[4].value.slice(15)
                },
                success: function(data){
                            $(".relation-block-row").remove();
                            for (var i=0; i<data.result.length;i++) relationInserter(data.result[i]);
                            $("input")[4].value = "";
                        }
                ,
                error: function(){
                    alert("Try to reload the page");
                }

            });
        }else{
            $(".notification").fadeIn({duration:500});
        }
    }
    
    
    function requestValidator(){
        if (window.innerWidth<=720){
            if (/http\w*:\/\/vk.com\/\w+/.test($(".first-input")[1].value) && /http\w*:\/\/vk.com\/\w+/.test($(".second-input")[1].value)){
            $(".notification").fadeOut({duration:500});
            $.ajax({
                url: "api/users/insert",
                method: "GET",
                data:{
                    source: $(".first-input")[1].value.slice(15),
                    target: $(".second-input")[1].value.slice(15)
                },
                success: function(data){
                    $(".notification-server-success").fadeIn({duration:1000});
                    setTimeout(function(){
                        $(".notification-server-success").fadeOut({duration:1000});
                    }, 5000);                       
                    $(".first-input")[1].value = "";
                    $(".second-input")[1].value = "";
                    }
                ,
                error: function(){
                    alert("Try to reload the page");
                }

            });}else{
                $(".notification-server").fadeIn({duration:1000});
            }
        }else{
            if (/http\w*:\/\/vk.com\/\w+/.test($(".first-input")[0].value) && /http\w*:\/\/vk.com\/\w+/.test($(".second-input")[0].value)){
            $(".notification").fadeOut({duration:500});
            $.ajax({
                url: "api/users/insert",
                method: "GET",
                data:{
                    source: $(".first-input")[0].value.slice(15),
                    target: $(".second-input")[0].value.slice(15)
                },
                success: function(data){
                    $(".notification-server").fadeOut({duration:500});
                    $(".notification-server-success").fadeIn({duration:1000});
                    setTimeout(function(){
                        $(".notification-server-success").fadeOut({duration:1000});
                    }, 10000);
                    $(".first-input")[0].value = "";
                    $(".second-input")[0].value = "";
                    
                }
                ,
                error: function(){
                    alert("Try to reload the page");
                }

            });}else{
                $(".notification-server").fadeIn({duration:500});
            }
        }
    }
    
    
    function updateRightNowInformation(){
        $.ajax({
                url: "api/update",
                method: "GET",
                success: function(data){
                    $("#queue").html(data.queue);
                    $("#queue-mobile").html(data.queue);
                    $("#processed").html(data.processed);
                    $("#processed-mobile").html(data.processed);
                    $("#average").html(data.average);
                    $("#average-mobile").html(data.average);

                },
                error: function(){
                    console.log("error with updating ${this}");
                }

            });
    }
    
    $("#search-button").on("click", searchValidator);
    $("#digest-button").on("click", requestValidator);
    $("#digest-button-mobile").on("click", requestValidator);
    updateRightNowInformation();
    setInterval(updateRightNowInformation, 10000);

        
});
