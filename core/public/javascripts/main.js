$(function(){
    $("ul.dropdown li").hover(function(){//初始化 Menu
        $(this).addClass("hover");
        $('ul:first', this).css('visibility', 'visible');
    }, function(){
        $(this).removeClass("hover");
        $('ul:first', this).css('visibility', 'hidden');
    });
    $("ul.dropdown li ul li:has(ul)").find("a:first").append(" &raquo; ");
    /**
     * <pre>
     * 在获取到需要提交的 form 表单的节点后, 利用此函数获取节点中的所有 input 值.
     * 例子:
     * $.varClosure.params = {};
     * $(':input').map($.varClosure);
     * alert(JSON.stringify($.varClosure.params);
     * </pre>
     */
    $.varClosure = function(){
        var o = $(this);
        if(!o.attr('name')) return false;
        $.varClosure.params[o.attr("name")] = o.val();
    };
});