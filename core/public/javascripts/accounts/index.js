$(function(){
    $('a[rel=tooltip]').tooltip({placement:'top'});
    $('#addUser_btn').click(function(){
        $.varClosure.params = {};
        $('#addUser :input').map($.varClosure);
        $.varClosure.params['type'] = $.varClosure.params['a.type'];
        $('#addUser').mask("添加中...");
        $.post('/accounts/create', $.varClosure.params, function(r){
            try{
                if(r.flag) alert('添加成功.');
                else alert(r.message);
            }finally{
                $('#addUser').unmask();
            }
        }, 'json');
    });


    $('a[class=btn][aid]').click(function(){
        var aid = $(this).attr('aid');
        $.varClosure.params = {};
        $('#account_' + aid + " :input").map($.varClosure);
        $('#account_table').mask("更新中...");
        $.post('/accounts/update', $.varClosure.params, function(r){
            try{
                if(r.flag) alert('更新成功.');
                else alert(r.message);
            }finally{
                $('#account_table').unmask();
            }
        }, 'json');
    });
});