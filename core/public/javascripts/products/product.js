$(function(){
    var postForm = $('#add_prod_form'); //缓存
    $('a[rel=tooltip]').tooltip();

    /**
     * 更新或者保存 Product 基本信息; 返回 false 防止冒泡
     * @param act
     * @param params
     * @param maskId 需要进行 Mask 的 jquery Select
     */
    function ajax_prod(params, maskId){

        $.ajax({
            url:'/products/p_create',
            data:params,
            dataType:'json',
            success:function(data){
                if(data.id && data['sku'] == params['p.sku']){ //成功
                    // 清零 Form 数据
                    alert('SKU: [' + data['sku'] + ']' + (save ? '添加' :'修改') + '成功.');
                    // 将数据按照格式添加到页面最上面
                }else{ //失败
                    alert("添加失败:\r\n " + JSON.stringify(data));
                }
                $(maskId).unmask();
            },
            error:function(xhr, sta, err){
                alert(xhr.responseText);
                $(maskId).unmask();
            }
        });
        return false;
    }


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