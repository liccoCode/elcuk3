$(function(){
    $('a[class=btn][sid]').click(function(){
        var sid = $(this).attr('sid');
        $.varClosure.params = {};
        $('#server_' + sid + ' :input').map($.varClosure);
        $('#server_table').mask('更新中...');
        $.post('/servers/update', $.varClosure.params, function(e){
            try{
                if(e.flag) alert('更新成功');
                else alert(e.message);
            }finally{
                $('#server_table').unmask();
            }
        });
    });
});