$(function(){
    /**
     * 加载 cat_div 中的数据, 并且给所有的 Cateogry 添加双击展开其关联的 Brand 事件
     * @param bid
     */
    function cat_div(){
        var catdiv = $('#cat_div');
        catdiv.mask('加载中...');
        catdiv.load('/familys/cat_div', {'b.name':''/*永远为空, 因为不需要从 Brand 返回查看 Category 了*/}, function(){
            //绑定双击事件
            $('#cat_div tr[cid]').dblclick(function(){
                var cid = $(this).attr('cid');
                brand_div(cid);
                $('#cid_val').attr('cid', cid).html(cid);
            });
            catdiv.unmask();
        });
    }

    /**
     * 加载 brand_div 中的数据, 并且给所有 Brand 添加双击展示 Category + Brand 的 Family 事件
     * @param cid
     */
    function brand_div(cid){
        var brandDiv = $('#brand_div');
        brandDiv.mask("加载中...");
        brandDiv.load('/familys/brand_div', {'c.categoryId':cid}, function(){
            //绑定双击事件
            $('#brand_div tr[bid]').dblclick(function(){
                var bid = $(this).attr('bid');
                $('#bid_val').attr('bid', bid).html(bid);
                fam_div();
            });
            brandDiv.unmask();
        });
    }

    function fam_div(){
        var famDiv = $("#fam_div");
        famDiv.mask("加载中...");
        famDiv.load('/familys/fam_div', {'c.categoryId':$('#cid_val').attr('cid'), 'b.name':$('#bid_val').attr('bid')}, function(){
            famDiv.unmask();
        });
    }

    $('#myModal').on('show', function(e){
        var cid = $('#cid_val').attr('cid');
        var bid = $('#bid_val').attr('bid');
        $('#f_cat').val(cid);
        $('#f_brand').val(bid);
        $('#f_family').val(cid + bid);
    });

    cat_div();
    brand_div('');

    $('#cFamily').click(function(){
        $.params = {};
        $('#myModal form :input').map($.varClosure);
        var myModal = $('#myModal');

        myModal.mask("添加中...");
        $.post('/familys/create', $.params, function(r){
            try{
                if(r.flag){
                    alert("添加成功.");
                    fam_div();
                    $('#myModal').modal('hide');
                }
                else alert(r.message);
            }finally{
                myModal.unmask();
            }
        });
    });
});