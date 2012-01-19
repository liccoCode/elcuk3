$(function(){
    $('.order_info').map(function(){
        var o = $(this);
        o.toggle(function(){
            o.next().slideDown(0);
        }, function(){
            o.next().slideUp(0);
        });
    });

    $('.tabs').tabs('.panes > div');
});