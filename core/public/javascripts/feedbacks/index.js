$(function(){
    $('a[rel=tooltip]').tooltip({placement:'top'});
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
});