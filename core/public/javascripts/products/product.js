$(function(){
    var postForm = $('#add_prod_form'); //缓存
    $('a[rel=tooltip]').tooltip();

    /**
     * 更新或者保存 Product 基本信息; 返回 false 防止冒泡
     * @param act
     * @param params
     */
    function ajax_prod(act, params){
        var save = true;
        if(act == 'save') save = true;else if(act == 'edit') save = false;else return false;
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
            },
            error:function(xhr, sta, err){
                alert(err);
            }
        });
        return false;
    }

    function ajax_prodQty(act, params){
        var save = true;
        if(act == 'save') save = true;else if(act == 'edit') save = false;else return false;
        $.ajax({
            url:'/products/pt_create',
            data:params,
            dataType:'json',
            success:function(data){
                if(data['flag']){ //成功
                    // 清零 Form 数据
                    alert('ProductQTY: ' + (save ? '添加' :'修改') + '成功.');
                    // 将数据按照格式添加到页面最上面
                }else{ //失败
                    alert("添加失败:\r\n " + JSON.stringify(data));
                }
            },
            error:function(xhr, sta, err){
                alert(err);
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
        ajax_prod('save', $.varClosure.params);
    });

    // Prod 更新按钮
    $('.saveProdInfo').click(function(){
        var id = $(this).attr('pid');
        $.varClosure.params = {};
        $('#basic_' + id + ' :input').map($.varClosure);
        delete $.varClosure.params['relateSKU']; //这个参数不进行提交
        ajax_prod('edit', $.varClosure.params);
    });

    // ProdQt 添加按钮
    $('.addProdQtyBtn').click(function(){
        var pid = $(this).attr('pid');
        $.varClosure.params = {};
        $('#add_modal_' + pid + ' :input').map($.varClosure);
        ajax_prodQty('save', $.varClosure.params);
    });

    // ProdQt 更新按钮
    $('.prodQtyItem a[ptid]').click(function(){
        if(!confirm("确认更新吗?"))return false;
        var ptid = $(this).attr('ptid');
        $.varClosure.params = {};
        $('#prodQtyItem_' + ptid + ' :input').map($.varClosure);
        ajax_prodQty('edit', $.varClosure.params);
    });
});