$(function(){
    var addCatForm = $('#add_cat_form');//缓存起来

    /**
     * 根据参数, 修改或者添加 Category
     * @param act
     */
    function ajax_cat(act, params){
        var save = true;
        if(act == 'save') save = true;else if(act == 'edit') save = false;
        $.ajax({
            url:'/categorys/cc',
            data:params,
            dataType:'json',
            success:function(data){
                if(data['categoryId'] == params['c.categoryId']){ //成功
                    // 清零 Form 数据
                    alert('Category: [' + data['categoryId'] + ']' + (save ? '添加' :'修改') + '成功.');
                    $('#add_modal').modal('hide');
                }else{ //失败
                    alert("添加失败:\r\n " + JSON.stringify(data));
                }
            },
            error:function(xhr, ajaxstat, err){
                alert(xhr.responseText);
            }
        });
    }

    //添加 category
    $('#addCategoryBtn').click(function(){
        $.varClosure.params = {};
        addCatForm.find(':input').map($.varClosure);
        ajax_cat('save', $.varClosure.params);
    });

    /**
     * 绑定详细页面中的 更新 按钮
     * @param cid
     */
    function bindBasicInfoUpdate(cid){
        $('#btn_' + cid).click(function(){
            $.varClosure.params = {};
            $('#detail_' + cid + ' :input').map($.varClosure);
            var basic = $('#detail_' + cid);
            basic.mask('更新中...');
            $.post('/categorys/cu', $.varClosure.params, function(r){
                try{
                    if(r.flag) alert("更新成功:[" + r.message + "]");
                    else alert('更新失败:[' + r.message + ']');
                }finally{
                    basic.unmask();
                }
            });
        });
    }

    /**
     * 绑定详细页面中的 Bind 按钮
     */
    function bindBrandBindBtn(){
        $("button[bind]").click(function(){
            var o = $(this);
            $.varClosure.params = {};
            o.parent().parent().find(':input').map($.varClosure);
            if(!('b.name' in $.varClosure.params)){
                alert("请选择正确的 Brand.");
                return false;
            }
            $('#brands_' + o.attr('cid')).mask('绑定中...');
            $.post('/categorys/bBrand', $.varClosure.params, function(r){
                try{
                    if(r.flag){
                        alert('绑定成功!');
                        $('tr[cid=' + o.attr('cid') + ']').dblclick();
                    }
                    else alert(r.message);
                }finally{
                    $('#brands_' + o.attr('cid')).unmask();
                }
            });
        });
    }

    /**
     * 绑定详细页面中 UnBind 按钮
     */
    function bindBrandUnBindBtn(){
        $('button[unbind]').click(function(){
            var o = $(this);
            $('#brands_' + o.attr('cid')).mask('解除绑定中...');
            $.post('/categorys/uBrand', {'c.categoryId':o.attr('cid'), 'b.name':o.attr('bid')}, function(r){
                try{
                    if(r.flag){
                        alert("解除绑定成功.");
                        $('tr[cid=' + o.attr('cid') + ']').dblclick();
                    }
                    else alert(r.message);
                }finally{
                    $('#brands_' + o.attr('cid')).unmask();
                }
            });
        });
    }

    // 双击加载详细信息
    $('#cat_slider tr[cid]').dblclick(function(){
        var slider = $('#cat_slider');
        var cid = $(this).attr('cid');
        slider.mask('加载中...');
        $('#cat_detail').load('/categorys/detail', {cid:cid}, function(){
            bindBasicInfoUpdate(cid);
            bindBrandBindBtn();
            bindBrandUnBindBtn();
            slider.unmask();
        });
    });

});