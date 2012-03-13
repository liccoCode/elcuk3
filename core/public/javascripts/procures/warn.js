$(function(){

    $('a[data-toggle="tab"][ct]').on('shown', function(e){
        $('#container').mask('加载中...');
        var o = $(e.target);
        var cat = o.text();
        var market = o.parents('div.tab-content div[id]').attr('id');
        $(o.attr('href')).load('/procures/warnItm', {cat:cat, market:market, page:1}, function(r){
            $('a[rel=tooltip]').tooltip({placement:'top'});
            $('#container').unmask();


            // ps_7
            $(':input[ps][sid]').keyup(function(e){
                if(e.keyCode != 13) return false;
                var o = $(this);
                $(o.parent()).mask('更新中...');
                $.post('/procures/ps', {'s.sellingId':o.attr('sid'), 's.ps':o.val()}, function(e){
                    try{
                        if(e.flag) alert('更新成功.');
                        else throw "no [flag] property."
                    }catch(e){
                        alert(JSON.stringify(e));
                    }
                    $(o.parent()).unmask();
                }, 'json');
            });

            // onWay, onWork, airPatch, airBuy, seaPatch, seaBuy
            $(':input[pid]').keyup(function(e){
                if(e.keyCode != 13) return false;
                var o = $(this);
                var params = {
                    id:o.attr('pid')
                };
                o.parent().parent().find(':input[pid]').map(function(){
                    var mo = $(this);
                    params['p.' + mo.attr('name')] = mo.val();
                });

                $(o.parent()).mask('更新中...');
                $.post('/procures/pitem', params, function(e){
                    try{
                        if(e.flag) alert('更新成功.');
                        else throw "no [flag] property."
                    }catch(e){
                        alert(JSON.stringify(e));
                    }
                    $(o.parent()).unmask();
                }, 'json');
            });

            $('ul a[invi]').click(function(e){
                if(!confirm('却要要隐藏? 隐藏后需要从 Listing 页面进行状态修改为非 DOWN 才重新可见.')) return false;
                $('#warnItm').mask('更新中...');
                $.post('/procures/invisible', {'s.sellingId':$(this).attr('sid'), 's.state':'DOWN'}, function(e){
                    try{
                        if(e.flag) alert('更新成功.');
                        else throw "no [flag] property."
                    }catch(e1){
                        alert(JSON.stringify(e));
                    }
                    $('#warnItm').unmask();
                }, 'json');
            });
        });
    });
});