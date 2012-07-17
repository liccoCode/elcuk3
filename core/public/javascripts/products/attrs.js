$(function(){
    // 添加 Att 按钮
    $('#attAddBtn').click(function(){
        $.varClosure.params = {};
        $('#add_att_form :input').map($.varClosure);
        var addAttr = $('#addAttr');
        addAttr.mask("添加中...");
        $.post('/attrs/create', $.varClosure.params, function(r){
            try{
                if(r.flag) alert("添加成功!");
                else alert(r.message);
            }finally{
                addAttr.unmask();
            }
        });
    });

    // 更新 Att 按钮
    $('table a[aid]').click(function(){
        var o = $(this);
        $.varClosure.params = {};
        var att = $('#aid_' + o.attr('aid'));
        att.find(":input").map($.varClosure);
        att.mask("更新中...");
        $.post('/attrs/update', $.varClosure.params, function(r){
            try{
                if(r.flag) alert("更新成功!");
                else alert(r.message);
            }finally{
                att.unmask();
            }
        });
    });
});