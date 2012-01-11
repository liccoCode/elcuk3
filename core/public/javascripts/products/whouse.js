$(function(){
    $('.whsInput').overlay();
    var whsForm = $('#add_whs_form');

    // 仓库的 memo 点击事件
    $('.whs_table .cat_info').map(function(){
        var o = $(this);
        o.children('td:eq(0)').toggle(function(){
            o.next().fadeIn(400);
        }, function(){
            o.next().fadeOut(400);
        });
    });

    $('.whs_table .cat_info a[whid]').map(function(){
        var o = $(this);
        var whid = o.attr("whid");
        o.click(function(){
            var params = {};
            var varClosure = function(){
                var i = $(this);
                if(!i.attr('name'))return false;
                params[i.attr('name')] = i.val();
            };
            $('#wh_' + whid + ' :input').map(varClosure);
            $('#wh_memo_' + whid + " :input").map(varClosure);
            ajax_whs('edit', params);
        });
    });

    /**
     * Whouse 的添加与更新的 ajax 方法
     * @param act
     * @param params
     */
    function ajax_whs(act, params){
        var save = true;
        if(act == 'save') save = true;
        else if(act == 'edit') save = false;
        else return false;

        $.ajax({
            url:'/products/w_create',
            data:params,
            dataType:'json',
            success:function(data){
                if(data.name && data['name'] == params['w.name']){ //成功
                    // 清零 Form 数据
                    alert('SKU: [' + data['name'] + ']' + (save ? '添加' :'修改') + '成功.');
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

    $('#addWhBtn').click(function(){
        var params = {};
        whsForm.find(':input').map(function(){
            var o = $(this);
            if(!o.attr('name'))return false;
            params[o.attr('name')] = o.val();
        });
        ajax_whs('save', params);
    });
});