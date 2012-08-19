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
            $('#cat_div tr[cid]').click(function(){
                var cid = $(this).attr('cid');
                brand_div(cid);
                $('#cid_val').attr('cid', cid).html(cid);
            });

            $.tableRowSelect("#cat_div tr[cid]");
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
            // 清空 Family
            $('#fam_div').html('');

            //绑定双击事件
            $('#brand_div tr[bid]').click(function(){
                var bid = $(this).attr('bid');
                $('#bid_val').attr('bid', bid).html(bid);
                fam_div(bid);
            });

            $.tableRowSelect("#brand_div tr[bid]");
            brandDiv.unmask();
        });
    }

    /**
     * 在每一次 Family 加载的时候, 需要绑定添加 Family 按钮
     */
    function bindAddFamilyBtn(bid){
        $('#add_family').click(function(){
            var mask = $('#container');
            mask.mask('添加 Family...');
            $.post('/familys/create', $(this).parents('table').find(":input").fieldSerialize(), function(r){
                mask.unmask();
                if(r.flag === false){
                    alert(r.message);
                }else{
                    fam_div(bid);
                    alert("添加成功.");
                }
            });
        });
    }

    function fam_div(bid){
        var famDiv = $("#fam_div");
        famDiv.mask("加载中...");
        famDiv.load('/familys/fam_div', {'c.categoryId':$('#cat_div tr.active').attr('cid'), 'b.name':bid}, function(){
            famDiv.unmask();
            bindAddFamilyBtn(bid);
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
                }else alert(r.message);
            }finally{
                myModal.unmask();
            }
        });
    });
});