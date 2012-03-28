$(function(){
    $('a[rel=tooltip]').tooltip({placement:'top'});

    // 手动抓取 Feedback 的功能
    $('#s_feedback_btn').click(function(){
        var page = prompt('请输入抓取的页码.');
        if(page <= 0){
            alert("请输入大于 0 的页面.");
            return false;
        }
        var market = $('#h_market').val();
        var acc = $('#h_acc').val();
        $.post('/feedbacks/feedback', {market:market, 'acc.id':acc, page:page}, function(r){
            try{
                if(r.flag) alert('成功抓取了' + r.count + ' 条 FeedBack.');
                else throw 'No [flag] property!'
            }catch(e){
                alert(JSON.stringify(r));
            }
        });
    });

    // Guide to Google Translate Comment
    $('a[trans]').click(function(){
        var o = $(this);
        window.open('http://translate.google.com/?text=' + escape(o.text()), '_blank');
        return false;
    });

    // Reslove Feedback
    $('a[rsv]').click(function(){
        var o = $(this);
        $(o.parents('tr')).mask('更新中...');
        $.post('/feedbacks/update', {'f.orderId':o.attr('fid'), 'f.state':'SLOVED'}, function(r){
            try{
                if(r.flag) alert('更新成功.');
                else throw 'No [flag] property!';
            }catch(e){
                alert(JSON.stringify(r));
            }finally{
                $(o.parents('tr')).unmask();
            }
        });
    });
});