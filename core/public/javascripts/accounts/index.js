$(function(){
    $('a[rel=tooltip]').tooltip({placement:'top'});
    $('#addUser_btn').click(function(){
        $.varClosure.params = {};
        $('#addUser :input').map($.varClosure);
        $.varClosure.params['type'] = $.varClosure.params['a.type'];
        $.post('/accounts/create', $.varClosure.params, function(r){
            try{
                if(r.flag) alert('添加成功.');
                else throw 'No [flag]  property!';
            }catch(e1){
                alert(JSON.stringify(r));
            }
        }, 'json');
    });


    $('a[class=btn][aid]').click(function(){
        var o = $(this);
        $.varClosure.params = {};
        o.parent().parent().find(":input").map($.varClosure);
        $.post('/accounts/update', $.varClosure.params, function(r){
            try{
                if(r.flag) alert('更新成功.');
                else throw 'No [flag] property!'
            }catch(e1){
                alert(JSON.stringify(r));
            }
        }, 'json');
    });
});