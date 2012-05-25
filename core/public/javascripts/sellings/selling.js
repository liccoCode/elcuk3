$(function(){
    var ALERT_TEMPLATE = "<div class='alert alert-success fade in'><button class='close' data-dismiss='alert'>×</button><div id='replace_it'></div></div>";

    (function(){// Selling 的 Image 处理
        var imagesUL = $('#images');
        var imgLITemplate = "<li class='span2'><a class='thumbnail' target='_blank'><img></a><input style='width:55%;height:8px;padding-left:40%'></li>";
        var imageNames = $("input[name=s\\.aps\\.imageName]").val().split("|-|");
        var imageNameObj = {};
        for(var i = 0; i < imageNames.length; i++) imageNameObj[imageNames[i]] = i;
        $.getJSON('/products/images', {sku:imagesUL.attr('sku')}, function(imgs){
            $.each(imgs, function(i, img){
                var fileName = img['fileName'];
                var imgLI = $(imgLITemplate).attr('filename', fileName);
                imgLI.find('a').attr("href", "/attachs/image?a.fileName=" + fileName);
                imgLI.find('img').attr("src", "/attachs/image?w=140&h=100&a.fileName=" + fileName);
                if(fileName in imageNameObj) imgLI.find('input').val(imageNameObj[fileName]);
                imgLI.appendTo(imagesUL);
            });
        });
    })();

    function baseBtnUpdate(btn, remote){
        var btnGroup = $(btn).parent();
        $.varClosure.params = {remote:remote};
        $('#container :input').map($.varClosure);
        btnGroup.mask("更新中...");
        $.post('/sellings/update', $.varClosure.params, function(r){
            if(r.flag === false) alert(r.message);
            else{
                alert("Selling: " + r['sellingId'] + " 更新成功!");
            }
            btnGroup.unmask();
        });
    }

    // Update 按钮
    $("button:contains('Update')").click(function(){
        baseBtnUpdate(this, false);
    });

    // Deploy 按钮
    $("button:contains('Deploy')").click(function(){
        baseBtnUpdate(this, true);
    });

    // Sync 按钮
    $("button:contains('Sync')").click(function(){
        alert('Sync Button');
    });

    /*
     * Image 值计算的功能.
     * 按照图片下方 input 中的索引进行图片的顺序排列, 如果索引不连续, 需要报告异常并停止取值,
     * 最后检查成功了返回 true, 否则返回 false
     */
    function imageIndexCal(){
        var goon = true;
        var fileNames = {size:0};
        $('#images li[filename]').map(function(){
            if(!goon) return false;
            var val = $(this).find('input').val().trim();
            if(!val || val === '') return false;
            if(!$.isNumeric(val)){
                alert("只能输入数字编号, 代表图片的位置.");
                goon = false;
            }else{
                fileNames[val] = $(this).attr("filename");
                fileNames.size += 1;
            }
        });

        var names = [];
        for(var i = 0; i < 9; i++){
            if(!(i in fileNames) && i < fileNames.size){
                alert("期待的索引应该是 " + i);
                return false;
            }
            if(fileNames[i]) names.push(fileNames[i]);
        }

        if(names.length <= 0){
            alert("请填写索引!");
            return false;
        }else{
            $('input[name=s\\.aps\\.imageName]').val(names.join('|-|'));
            return true;
        }

    }

    // 初始化带有 placement 的 popover 元素
    $('button[rel=popover][placement]').popover();
    $('#img_cal').click(imageIndexCal).find('~ button').click(function(){
        if(!imageIndexCal()) return false; // 如果预览检测不成功, 后续也不需要执行了
        if(confirm("确定要更新到 " + $("select[name=s\\.market]").val() + " ?")){
            var o = $(this);
            o.parent().mask('上传图片中...');
            $.post('/sellings/imageUpload',
                    {'s.sellingId':$("#s_sellingId").val(), 'imgs':$('#image_tr + tr input').val()},
                    function(r){
                        try{
                            if(r.flag){
                                var alert = $(ALERT_TEMPLATE);
                                alert.find('#replace_it').replaceWith("<p>更新成功! <a target='_blank' href='" + r.message + "'>访问 Listing</a></p>");
                                alert.prependTo("#container");
                            }else alert(r.message);
                        }finally{
                            o.parent().unmask();
                        }
                    });
        }
    });
});