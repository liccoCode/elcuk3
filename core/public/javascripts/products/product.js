$(function() {
    var triggers = $(".prodInput").overlay();
    var postForm = $('#add_prod_form'); //缓存

    // 产品详细信息的 tab
    $('tr.prodDetail ul.tabs').map(function() {
        $(this).tabs('div.panes > div');
    });
    $('.prod_table .prodItem').map(function(d) {
        var o = $(this);
        o.toggle(function() {
            o.next().fadeIn(500);
        }, function() {
            o.next().fadeOut(500);
        });
    });

    /**
     * 更新或者保存 Product 基本信息; 返回 false 防止冒泡
     * @param act
     * @param params
     */
    function ajax_prod(act, params) {
        var save = true;
        if(act == 'save') save = true; else if(act == 'edit') save = false; else return false;
        $.ajax({
            url:'/products/p_create',
            data:params,
            dataType:'json',
            success:function(data) {
                if(data.id && data['sku'] == params['p.sku']) { //成功
                    // 清零 Form 数据
                    alert('SKU: [' + data['sku'] + ']' + (save ? '添加' : '修改') + '成功.');
                    // 将数据按照格式添加到页面最上面
                } else { //失败
                    alert("添加失败:\r\n " + JSON.stringify(data));
                }
            },
            error:function(xhr, sta, err) {
                alert(err);
            }
        });
        return false;
    }

    /**
     * 添加按钮
     */
    $('#addProdPostBtn').click(function() {
        var params = {};
        postForm.find(':input').each(function(i, o) {
            var ji = $(o);
            if(!ji.attr('name')) return false;
            params[ji.attr("name")] = ji.val();
        });

        if('p.nocat' in params) {
            alert('请选择 Category!');
            //            return;
        }
        ajax_prod('save', params);
    });

    //更新按钮
    $('button.saveProdInfo').click(function() {
        var o = $(this);
        var id = o.attr('pid');
        var params = {};
        $('#prod_info_' + id + ' :input').map(function() {
            var ipt = $(this);
            if(!ipt.attr('name'))return false;
            params[ipt.attr('name')] = ipt.val();
        });
        delete params['relateSKU']; //这个参数不进行提交
        ajax_prod('edit', params);
    });
});