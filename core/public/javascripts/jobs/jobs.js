$(function(){
    $('.add_job').overlay();


    $('#add_job_btn').click(function(){
        $.varClosure.params = {};
        $('#job_add_form :input').map($.varClosure);
        // 自行修正 Checkbox 的值
        $.varClosure.params['j.close'] = $('#j_close').is(':checked');
        $.mask.load();
        $.ajax({
            url:'/jobs/c',
            data:$.varClosure.params,
            dataType:'json',
            success:function(data){
                alert('Job [' + data['className'] + '] 添加成功.');
                $.mask.close();
            },
            error:function(xhr, state, error){
                alert(error);
                $.mask.close();
            }
        });
        return false;
    });

    $('.runOnce').click(function(){
        var o = $(this);
        $.ajax({
            url:'/jobs/now',
            data:{id:o.attr('jid')},
            dataType:'json',
            success:function(data){
                if(data['flag']) alert('执行成功.')
            },
            error:function(xhr, state, error){
                alert(error);
            }
        });
    });
    $('.j_update').click(function(){
        var o = $(this);
        $.varClosure.params = {};
        o.parent().parent().find(":input").map($.varClosure);
        $.varClosure.params['j.close'] = o.find(':input[name="j.close"]').is(':checked');
        $.mask.load();
        $.ajax({
            url:'/jobs/u',
            data:$.varClosure.params,
            dataType:'json',
            success:function(data){
                if(data['className']) alert(data['className'] + '更新成功');
                else alert("更新失败!");
                $.mask.close();
            },
            error:function(xhr, state, error){
                alert(error);
                $.mask.close();
            }
        });
    });
});