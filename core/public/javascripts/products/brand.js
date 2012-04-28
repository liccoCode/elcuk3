$(function(){

    $('#addBrandBtn').click(function(){
        $.varClosure.params = {};
        var form = $('#add_brand_form');
        $('#add_brand_form :input').map($.varClosure);
        form.mask('创建中...');
        $.post('/brands/bc', $.varClosure.params, function(r){
            try{
                if(r.flag) alert("创建成功.");
                else alert(r.message);
            }finally{
                form.unmask();
            }
        });
    });

    function bindCategoryBindBtn(){
        $('button[bind]').click(function(){
            var o = $(this);
            $.varClosure.params = {};
            o.parent().parent().find(':input').map($.varClosure);
            $('#category_' + o.attr('bid')).mask('绑定中...');
            $.post('/categorys/bBrand', $.varClosure.params, function(r){
                try{
                    if(r.flag){
                        alert('绑定成功!');
                        $('tr[bid=' + o.attr('bid') + ']').dblclick();
                    }
                    else alert(r.message);
                }finally{
                    $('#category_' + o.attr('bid')).unmask();
                }
            });
        });
    }

    function bindCategoryUnBindBtn(){
        $('button[unbind]').click(function(){
            var o = $(this);
            var bid = o.attr('bid');
            $('#category_' + bid).mask('解除绑定中...');
            $.post('/categorys/uBrand', {'c.categoryId':o.attr('cid'), 'b.name':bid}, function(r){
                try{
                    if(r.flag){
                        alert("解除绑定成功!");
                        $('tr[bid=' + bid + ']').dblclick();
                    }else alert(r.message);
                }finally{
                    $('#category_' + bid).unmask();
                }
            });
        });
    }


    // 双击加载详细信息
    $('#brand_slider tr[bid]').dblclick(function(){
        var slider = $('#brand_slider');
        var bid = $(this).attr('bid');
        slider.mask("加载中...");
        $('#brand_detail').load('/brands/detail', {bid:bid}, function(r){
            bindCategoryBindBtn();
            bindCategoryUnBindBtn();
            slider.unmask();
        });
        return false;
    });
});