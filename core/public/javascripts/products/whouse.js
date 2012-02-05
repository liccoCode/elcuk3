$(function(){
    var whsForm = $('#add_whs_form');

    /**
     * Whouse 的添加与更新的 ajax 方法
     * @param act
     * @param params
     */
    function ajax_whs(act, params){
        var action = 0; // 0-save, 1-update, 2-remove
        if(act == 'save') action = 0;
        else if(act == 'edit') action = 1;
        else if(act == 'remove') action = 2;
        else return false;

        $.ajax({
            url:(action == 2 ? '/products/w_remove' :'/products/w_create'),
            data:params,
            dataType:'json',
            success:function(data){
                if((data.name && data['name'] == params['w.name']) || data['flag']){ //成功
                    // 清零 Form 数据
                    var text = '';
                    switch(action){
                        case 0:
                            text = '添加';
                            break;
                        case 1:
                            text = '修改';
                            break;
                        case 2:
                            text = '删除';
                    }
                    alert('SKU: [' + data['name'] + ']' + text + '成功.');
                    // 将数据按照格式添加到页面最上面
                }else{ //失败
                    alert("添加失败:\r\n " + JSON.stringify(data));
                }
            },
            error:function(xhr, sta, err){
                alert(xhr.responseText);
            }
        });
        return false;
    }

    $('#wh_list_table a[update]').click(function(){
        $.varClosure.params = {};
        var whid = $(this).attr('whid');
        $('#wh_' + whid + ' :input').map($.varClosure);
        $('#wh_memo_' + whid + " :input").map($.varClosure);
        ajax_whs('edit', $.varClosure.params);
    });

    $('#wh_list_table a[remove]').click(function(){
        var whname = $(this).attr('whname');
        if(!confirm('确认删除 ' + whname + ' 吗?'))return false;
        ajax_whs('remove', {id:$(this).attr('whid')});
    });

    $('#addWhBtn').click(function(){
        $.varClosure.params = {};
        whsForm.find(':input').map($.varClosure);
        ajax_whs('save', $.varClosure.params);
    });
});