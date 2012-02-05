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
            url:'/products/c_create',
            data:params,
            dataType:'json',
            success:function(data){
                if(data.id && data['categoryId'] == params['c.categoryId']){ //成功
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

    // 修改 category
    $("a[cid]").click(function(){
        var t = $(this);
        var catid = t.attr('cid');
        if(!catid)return false;
        $.varClosure.params = {};
        $('#cat_' + catid + " :input").map($.varClosure);
        $('#cat_memo_' + catid + " :input").map($.varClosure);
        ajax_cat('edit', $.varClosure.params);
        return false;
    });

});