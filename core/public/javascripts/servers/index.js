$(function(){
    $('a[class=btn][sid]').click(function(){
        var o = $(this);
        $.varClosure.params = {};
        o.parent().parent().find(':input').map($.varClosure);
        $.post('/servers/update', $.varClosure.params, function(e){
            try{
                if(e.flag) alert('更新成功');
                else throw 'No flag property!.'
            }catch(e1){
                alert(JSON.stringify(e));
            }
        }, 'json');
    });
});