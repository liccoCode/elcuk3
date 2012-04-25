$(function(){
    var postForm = $('#add_prod_form'); //缓存
    $('a[rel=tooltip]').tooltip();

    // Prod 添加按钮
    $('#addProdPostBtn').click(function(){
        $.varClosure.params = {};
        postForm.find(':input').map($.varClosure);
        if('p.nocat' in $.varClosure.params){
            alert('请选择 Category!');
            return false;
        }
        $('#add_modal').mask('添加中...');
        $.post('/products/p_create', $.varClosure.params, function(r){
            if(r.flag) alert("添加成功");
            else alert(r.message);
            $('#add_modal').unmask();
        });
    });


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

    })
});