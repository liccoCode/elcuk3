$(function(){
    $.varClosure = function(){
        var o = $(this);
        if(!o.attr('name')) return false;
        if(o.val())
            $.varClosure.params[o.attr("name")] = o.val().trim();
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
        },
        /**
         * yyyy-MM-dd 格式的年月日
         * @param date
         */
        fmt2:function(date){
            return date.getFullYear() + "-" + (date.getMonth() + 1) + '-' + date.getDate();
        }
    }
});