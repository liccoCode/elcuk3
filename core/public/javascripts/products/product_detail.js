$(function(){
    /**
     * ========================  Product Details Page ============================
     */
        // Product 更新
    $('#prod_basic a[sku]').click(function(){
        $('#prod_basic').mask('更新中...');
        $.varClosure.params = {};
        $('#prod_basic :input').map($.varClosure);
        $.post('/products/p_u', $.varClosure.params, function(r){
            if(r.flag) alert("更新成功.");
            else alert(JSON.stringify(r));
            $('#prod_basic').unmask();
        });
    });


    // SellingQTY 更新
    $('#prod_sqty a[qid]').click(function(){
        var qid = $(this).attr('qid');
        $('#prod_sqty').mask("更新中...");
        $.varClosure.params = {};
        $('#prod_qty_' + qid + " :input").map($.varClosure);
        $.post('/products/p_sqty_u', $.varClosure.params, function(r){
            if(r.flag) alert('更新成功.');
            else alert(JSON.stringify(r));
            $('#prod_sqty').unmask();
        });

    });


    $('#family_bind').click(function(){
        $.post('/products/p_f', {'p.sku':$(this).attr('pid'), 'f.family':$('#family_fix').val()}, function(r){
            alert(r.message);
        })
    });

    // Attrbutes
    $('#attr_btn').click(function(){
        $.varClosure.params = {'p.sku':$('#p_sku').val()};
        var maskObj = $('#attrs');
        $('#attrs :input').map($.varClosure);
        maskObj.mask("更新中...");
        $.post('/products/p_attr', $.varClosure.params, function(r){
            try{
                if(r.flag) alert("更新成功!<br/>" + r.message);
                else alert(r.message);
            }finally{
                maskObj.unmask();
            }
        });
    });

    function afterChangeTextAreaVal(att_id){
        var v = $('#' + att_id + '_text').val();
        $('#' + att_id + '_value').html((v.length >= 50 ? v.substring(0, 50) + '...' :v));
    }

    $('#attrs textarea').blur(function(){
        afterChangeTextAreaVal($(this).attr('id').replace("_text", ""));
    });
    $('#attrs .collapse').on('hide', function(){
        afterChangeTextAreaVal($(this).attr('id'));
    });

    var newAttrs = $("#new_attrs :checkbox");
    newAttrs.click(function(){
        /**
         * 1. 删除 #attrs 下所有 att.name 在 newAttrs 集合中的 div 节点
         * 2. 获取 index
         * 3. 根据 newAttrs 中 check 的进行重新绘制
         */
        newAttrs.each(function(){
            $('#' + $(this).attr('name')).remove();
        });
        var index = $('#attrs .attr').size();

        /**
         * att -> {id:71LNA1-PPU1_func", name:func, value:'超强的功能'}
         * @param i
         * @param att
         */
        function template(i, att){
            var attrTemplate = "<div class='span11 attr' id='" + att.name + "'>";
            attrTemplate += "<div class='span11'>";
            attrTemplate += "<label class='inline checkbox'>";
            attrTemplate += "<input type='checkbox' name='attrs[" + i + "].close'>关闭?&nbsp;";
            attrTemplate += "<a href='#" + att.id + "' data-toggle='collapse' style='cursor:pointer;overflow:hidden;'> " + att.name + "(" + att.fullName + ") | false | <span id='" + att.id + "_value'>" + att.value + "</span> </a>";
            attrTemplate += "</label>";
            attrTemplate += "</div>";

            attrTemplate += "<div id='" + att.id + "' class='collapse span11'>";
            attrTemplate += '<input type="hidden" name="attrs[' + i + '].id" value="' + att.id + '">';
            attrTemplate += '<input type="hidden" name="attrs[' + i + '].attName.name" value="' + att.name + '">';
            attrTemplate += '<textarea id="' + att.id + '_text" rows="4" name="attrs[' + i + '].value" class="span11">' + att.value + '</textarea>';
            attrTemplate += "</div>";

            attrTemplate += "</div>";

            return attrTemplate;
        }

        $('#new_attrs :checkbox').each(function(){
            var o = $(this);
            if(!o.is(":checked")) return;
            $(template(index++, {id:$('#p_sku').val() + '_' + o.attr('name'), name:o.attr('name'), value:''})).appendTo("#attrs");
        });


    });

    // Drag & Drop Pic
    var template = '<li class="span2">' +
            '<a href="#" target="_blank" class="thumbnail"><img/></a>' +
            '<div class="progress"><div class="bar"></div></div>' +
            '<div class="action"><a href="#" class="btn btn-danger btn-mini">X</a></div>' +
            '</li>';
    var dropbox = $('#dropbox');
    var uploaded = $('#uploaded');
    var message = $(".message");


    // 初始化页面的时候加载此 Product 对应的图片
    $.getJSON('/products/images', {sku:$('#p_sku').val()}, function(imgs){
        if(imgs.length > 0) message.remove();
        $.each(imgs, function(i, img){
            var imgEL = $(template);
            var imgUrl = "/attachs/image?a.fileName=" + img['fileName'];
            imgEL.find("img").attr('src', imgUrl + "&w=140&h=100");
            imgEL.find('a.thumbnail').attr("href", imgUrl);
            imgEL.find('a.btn').attr('outName', img['outName']).click(rmImage);
            imgEL.find('div.progress').remove();
            imgEL.appendTo(uploaded);
        });
    });

    /**
     * 利用 Html 的 File API(FileReader) 创建图片的缩略图
     * @param file
     */
    function createImage(file){
        var preview = $(template);
        var img = $('img', preview);
        var reader = new FileReader();
        reader.onload = function(e){
            img.attr('src', e.target.result);
        };
        reader.readAsDataURL(file); // 直接将数据读成二进制字符串以便放在 URL 上显示
        preview.appendTo(uploaded);
        $.data(file, preview);
    }

    /**
     * 删除服务器端的 Image, 同时删除页面中的 Image 元素
     * @r Ajax 的返回 JSON 对象
     */
    function rmImage(e){
        var o = $(this);
        $.post('/products/rmimage', {'a.outName':o.attr('outName')}, function(r){
            if(r.flag) alert("删除成功.");
            else alert(r.message);
            $('a[outName=' + o.attr('outName') + ']').parents('li').remove();
            return false;
        });
    }


    dropbox.filedrop({
        paramname:'a.file',
        maxfiles:20,
        maxfilesize:4, // in mb
        url:'/products/upload',

        beforeEach:function(file){
            // file is a file object
            // return false to cancel upload
            if(file.type.split("/")[0].trim().toLowerCase() !== 'image'){
                alert("只可以上传图片.");
                return false;
            }

            // 'a.fid':'90-kd'
            var sku = $('#p_sku').val();
            if(sku == undefined || sku === ''){
                alert("没有 SKU, 错误页面!");
                return false;
            }
            this.data['a.fid'] = sku;
        },
        uploadStarted:function(i, file, len){
            createImage(file);
        },
        dragOver:function(){
            dropbox.css('background', '#eff');
        },
        dragLeave:function(){
            dropbox.css('background', '#eee');
        },
        drop:function(){
            dropbox.css('background', '#eee');
        },
        progressUpdated:function(i, file, progress){
            $.data(file).find('.bar').width(progress + '%');
        },
        uploadFinished:function(i, file, r, time){
            $.data(file).find('.progress').remove();
            if(message)message.remove();
            if(r['flag'] === false) alert(r.message);
            else{
                $.data(file).find('a.thumbnail').attr("href", "/attachs/image?a.outName=" + r['outName']);
                $.data(file).find('a.btn').attr('outName', r['outName']).click(rmImage);
            }
        },
        error:function(err, file){
            switch(err){
                case 'BrowserNotSupported':
                    alert('browser does not support html5 drag and drop');
                    break;
                case 'TooManyFiles':
                    // user uploaded more than 'maxfiles'
                    break;
                case 'FileTooLarge':
                    // program encountered a file whose size is greater than 'maxfilesize'
                    // FileTooLarge also has access to the file which was too large
                    // use file.name to reference the filename of the culprit file
                    alert("File is too Large!");
                    break;
                default:
                    break;
            }
        }
    });
});