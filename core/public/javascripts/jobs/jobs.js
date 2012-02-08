$(function(){
    $('a[rel=tooltip]').tooltip();


    $('#add_job_btn').click(function(){
        $.varClosure.params = {};
        $('#job_add_form :input').map($.varClosure);
        // 自行修正 Checkbox 的值
        $.varClosure.params['j.close'] = $('#j_close').is(':checked');
        $.ajax({
            url:'/jobs/c',
            data:$.varClosure.params,
            dataType:'json',
            success:function(data){
                alert('Job [' + data['className'] + '] 添加成功.');
            },
            error:function(xhr, state, error){
                alert(error);
            }
        });
        return false;
    });

    $('.runOnce').click(function(){
        $.ajax({
            url:'/jobs/now',
            data:{id:$(this).attr('jid')},
            dataType:'json',
            success:function(data){
                if(data['flag']) alert('执行成功.')
            },
            error:function(xhr, state, error){
                alert(xhr.responseText);
            }
        });
    });

    $('.j_update').click(function(){
        var o = $(this);
        var jid = o.attr('jid');
        $.varClosure.params = {};
        $("#job_itm_" + jid + " :input").map($.varClosure);
        $.varClosure.params['j.close'] = $("#job_itm_" + jid + " :input[name='j.close']").is(':checked');
        $.ajax({
            url:'/jobs/u',
            data:$.varClosure.params,
            dataType:'json',
            success:function(data){
                if(data['className']) alert(data['className'] + '更新成功');
                else alert("更新失败!");
            },
            error:function(xhr, state, error){
                alert(xhr.responseText);
            }
        });
    });
});