$(function(){
    $('a[rel=tooltip]').tooltip({placement:'right'});

    $('#change_passwd_btn').click(function(){
        $.varClosure.params = {};
        $('#change_passwd :input').map($.varClosure);
        if($.varClosure.params['u.password'] != $.varClosure.params['c_password']){
            alert('两次密码不一致!');
            return false;
        }
        $('#change_passwd').mask('更新中...');
        $.post('/users/passwd', $.varClosure.params, function(data){
            if(data['username'] == $.varClosure.params['u.username']){
                alert('更新成功');
            }else{
                alert('更新失败;\r\n' + JSON.stringify(data));
            }
            $('#change_passwd').unmask();
        }, 'json');
        return false;
    });

    $('#cci').click(function(){
        $.post('/application/clearCache', {}, function(r){
            if(r.flag) alert('清理首页缓存成功');
            else alert('清理失败.')
        });
    });
});