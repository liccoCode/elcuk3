$(function(){
    $.varClosure = function(){
        var o = $(this);
        if(!o.attr('name')) return false;
        if(o.val()){
            switch(o.attr('type')){
                case 'checkbox':
                    $.varClosure.params[o.attr("name")] = o.is(':checked');
                    break;
                case 'radio':
                    if(o.is(':checked')) $.varClosure.params[o.attr("name")] = o.val().trim();
                    break;
                default:
                    $.varClosure.params[o.attr("name")] = o.val().trim();
            }
        }
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
            var month = date.getMonth() + 1;
            var ddate = date.getDate();
            return (month < 10 ? '0' + month :month) + '/' + (ddate < 10 ? '0' + ddate :ddate) + '/' + date.getFullYear();
        },
        /**
         * yyyy-MM-dd 格式的年月日
         * @param date
         */
        fmt2:function(date){
            return date.getFullYear() + "-" + (date.getMonth() + 1) + '-' + date.getDate();
        },

        fmt3:function(date){
            return date.getFullYear() + "-" + (date.getMonth() + 1) + '-' + date.getDate() + " " + date.getHours() + ':' + date.getMinutes() + ":" + date.getSeconds();
        }

    };


    // ---- Key board shor
    $.keys = [
        ['g+h', '/'],
        ['g+s', '/analyzes/index'],
        ['g+o', '/orders/o_index?s=35&p=1'],
        ['g+l', '/listings/l_index'],
        ['g+p+w', '/procures/warn']
    ];

    var bindkey = function(k, url){
        key(k, function(){
            location.href = url;
            return false;
        })
    };

    for(var i = 0; i < $.keys.length; i++){
        var pair = $.keys[i];
        bindkey(pair[0], pair[1]);
    }
});