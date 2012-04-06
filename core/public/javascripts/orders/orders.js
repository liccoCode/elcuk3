$(function(){
    $('#search_form :input[type=date]').dateinput({format:'mm/dd/yyyy'});
    $('a[rel=tooltip]').tooltip({placement:'top'});
    $('a[rel=popover]').popover({placement:'bottom'});


    function do_search(o, page){
        $.varClosure.params = {};
        $('#search_form :input').map($.varClosure);
        var now = new Date();

        switch(o.attr('day')){
            case '7':
                $.varClosure.params['p.from'] = $.DateUtil.fmt1($.DateUtil.addDay(-7, now));
                $.varClosure.params['p.to'] = $.DateUtil.fmt1(now);
                break;
            case '30':
                $.varClosure.params['p.from'] = $.DateUtil.fmt1($.DateUtil.addDay(-30, now));
                $.varClosure.params['p.to'] = $.DateUtil.fmt1(now);
                break;
            case '-1':
                if(!$.varClosure.params['p.from'] || !$.varClosure.params['p.to']) alert('没有设置时间, 将搜索所有时间!');
                break;
            default:
                alert('输入的日期不合法!');
                return false;
        }
        if(page) $.varClosure.params['p.page'] = page;

        //虽然很多地方使用了 $.varClosure.params 这个动态参数, 但是由于每一个页面的 JS 的 Var Context 不同, 所以不会产生影响
        $.get('/Orders/o_search', $.varClosure.params, function(html){
            $("#order_list").html(html);
            $('a[rel=tooltip]').tooltip({placement:'top'});
            $('.pagination a[page]').click(function(){
                do_search(o, $(this).attr('page'));
                return false;
            })
        }, 'html');

        /*
         $("#order_list").load('/Orders/o_search', $.varClosure.params, function(r){
         $('a[rel=tooltip]').tooltip({placement:'top'});
         $('.pagination a[page]').click(function(){
         do_search(o, $(this).attr('page'));
         return false;
         })
         });
         */
        return false;
    }

    // 搜索按钮组
    $('#search_btns a[class]').click(function(){
        do_search($(this), 1);
    });
});