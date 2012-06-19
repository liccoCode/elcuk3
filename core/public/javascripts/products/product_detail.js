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
            $(template(index++, {id:$('#p_sku').val() + '_' + o.attr('name'), fullName:o.val(), name:o.attr('name'), value:''})).appendTo("#attrs");
        });

    });

    var dropbox = $('#dropbox');
    var uploaded = $('#uploaded');
    var message = $("#dropbox .message");

    window.dropUpload.loadImages($('#p_sku').val(), message, uploaded);

    function fidCallBack(){
        var sku = $('#p_sku').val();
        if(sku == undefined || sku === ''){
            alert("没有 SKU, 错误页面!");
            return false;
        }
        return {fid:sku, p:'SKU'};
    }

    window.dropUpload.iniDropbox(fidCallBack, dropbox, message, dropbox);

    // -------------- 上架 Amazon 的相关功能

    // UPC 检查
    $('input[name=s\\.aps\\.upc] ~ button').click(function(){
        var o = $(this);
        var upcEl = o.prev();
        var upc = upcEl.val();
        if(!$.isNumeric(upc)){
            alert("UPC 必须是数字!");
            return false;
        }
        $.getJSON('/products/upcCheck', {upc:upc}, function(r){
            if(r.flag === false) alert(r.message);
            else{
                var upcAlertTemplate = "<div class='alert alert-info fade in'>" +
                    "<button class='close' data-dismiss='alert'>×</button>" +
                    "<strong>UPC 检查信息:</strong>" +
                    "</div>";
                var alertEl = $(upcAlertTemplate);
                if(r.length == 0)
                    alertEl.find('strong').after('<div>此 UPC 在系统中还没有 Selling</div>')
                else
                    $.each(r, function(i, s){
                        alertEl.find("strong").after('<div>' + s['merchantSKU'] + " | " + s['market'] + '</div>');
                    });
                alertEl.insertBefore("#btn_div");
                var mskuEl = $('input[name=s\\.merchantSKU]');
                mskuEl.val(mskuEl.val().split(',')[0] + ',' + upc);
                o.removeClass('btn-warning').addClass('btn-success');
            }
        });
    });

    // Market 更换价格单位按钮
    $('#market').change(function(){
        var currency = '';
        switch($(this).val()){
            case 'AMAZON_UK':
            case 'EBAY_UK':
                currency = "&pound;";
                break;
            case 'AMAZON_US':
                currency = "$";
                break;
            default:
                currency = '&euro;';
        }
        $('span.currency').html(currency);
    });

    // Amazon 上架
    $('#s_sale').click(function(){
        var btnDiv = $('#btn_div');
        $.varClosure.params = {'s.listing.listingId':$('#lid').text()};
        btnDiv.mask("创建中...");
        $("#amazon :input").map($.varClosure);
        $.post('/products/saleAmazonListing', $.varClosure.params, function(r){
            if(r.flag === false) alert(r.message);
            else alert('添加成功.');
            btnDiv.unmask();
        });
    });

});