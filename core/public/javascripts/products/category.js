$(function(){
    var triggers = $(".prodInput").overlay();
    $('.cat_info :input').map(function(){
        var t = $(this);
        if(!t.attr('readonly'))return false;
        t.toggle(function(){
            $(this).parent().parent().next().find('div').slideDown(400);
        }, function(){
            $(this).parent().parent().next().find('div').slideUp(400);
        });
    });
    var addCatForm = $('#add_cat_form');//缓存起来

    /**
     * 根据参数, 修改或者添加 Category
     * @param act
     */
    function ajax_cat(act, params){
        var save = true;
        if(act == 'save') save = true;else if(act == 'edit') save = false;
        $.ajax({
            url:'/products/c_create',
            data:params,
            dataType:'json',
            success:function(data){
                if(data.id && data['categoryId'] == params['c.categoryId']){ //成功
                    // 清零 Form 数据
                    alert('Category: [' + data['categoryId'] + ']' + (save ? '添加' :'修改') + '成功.');
                    if(save){
                        // 将数据按照格式添加到页面最上面
                    }
                }else{ //失败
                    alert("添加失败:\r\n " + JSON.stringify(data));
                }
            },
            error:function(xhr, ajaxstat, err){
                alert(err);
            }
        });
    }

    //添加 category
    $('#addCategoryBtn').click(function(){
        var params = {};
        addCatForm.find(':input').each(function(i, o){
            var ji = $(o);
            if(!ji.attr('name')) return;
            params[ji.attr("name")] = ji.val();
        });
        ajax_cat('save', params);
    });

    // 修改 category
    $(".cats_table td.action a").click(function(){
        var t = $(this);
        var catid = t.attr('catid');
        if(!catid)return false;
        var params = {};
        var valClosure = function(i, d){
            var o = $(d);
            if(!o.attr('name')) return;
            params[o.attr('name')] = o.val();
        };
        $('#cat_' + catid + " :input").map(valClosure);
        $('#cat_memo_' + catid + " :input").map(valClosure);
        ajax_cat('edit', params);
        return false;
    });

});