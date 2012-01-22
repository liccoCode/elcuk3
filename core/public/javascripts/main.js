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

    $.DateUtil = {
        /**
         * 按照给与的 date 基准时间, 然后进行 day 天数的添加减少
         * @param date
         * @param day
         */
        addDay:function(day, date){
            if(date){
                if($.type(date) != 'date'){
                    throw 'the date is not type of Date';
                }
            }else{
                date = new Date();
            }
            var newDate = new Date();
            newDate.setDate(date.getDate() + day);
            return newDate;
        },
        /**
         * mm/dd/yyyy 格式的年月日
         */
        fmt1:function(date){
            return (date.getMonth() + 1) + '/' + date.getDate() + '/' + date.getFullYear();
        }
    }
});