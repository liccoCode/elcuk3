/**
 * 对 simile Timeline 参数设置的初始化
 */
Timeline_ajax_url = "/js/timeline/timeline_ajax/simile-ajax-api.js";
Timeline_urlPrefix = '/js/timeline/timeline_js/';
Timeline_parameters = 'bundle=true';

var LoadMask = {
    /**
     * 在页面进入的时候都需要清理带有 _times 的 sessionStorage
     */
    clear: function(){
        console.log('begin clean LoadMask...');
        for(var key in sessionStorage){
            if(!sessionStorage.hasOwnProperty(key)) continue;
            if(key.indexOf('_times') > 0){
                delete sessionStorage[key];
                console.log('delete key' + key)
            }
        }
        console.log('end of clean LoadMask.')
    },
    /**
     * 锁屏幕
     * @param selector
     */
    mask: function(selector){
        if(!selector) selector = "#container";
        var times = sessionStorage[selector + "_times"];
        if(!times){
            times = 0;
            $(selector).mask("处理中...");
        }
        sessionStorage[selector + "_times"] = ++times;
        console.log(selector + "_times:" + times);
    },
    unmask: function(selector){
        if(!selector) selector = "#container";
        var times = sessionStorage[selector + "_times"];
        if(--times <= 0){
            delete sessionStorage[selector + "_times"];
            $(selector).unmask();
        }else{
            sessionStorage[selector + "_times"] = times;
        }
        console.log(selector + "_times:" + times);
    }
};

/**
 * Effect, 包含几个页面效果
 */
var EF = {
    /**
     * 滚动到该元素
     * @param selector
     */
    scoll: function(selector){
        var mao = $(selector);
        return $('body').animate({scrollTop: mao.offset().top - mao.height() - 50}, 1000);
    },

    colorAnimate: function(selector, from, to){
        if(from == undefined) from = '#E35651';
        if(to == undefined) to = '#FFF';
        return $(selector).css('backgroundColor', from).animate({backgroundColor: to}, 3500);
    }
};

$.varClosure = function(){
    var o = $(this);
    if(!o.attr('name')) return false;
    if(o.val()){
        switch(o.attr('type')){
            case 'checkbox':
                $.params[o.attr("name")] = o.is(':checked');
                break;
            case 'radio':
                if(o.is(':checked')) $.params[o.attr("name")] = o.val().trim();
                break;
            default:
                $.params[o.attr("name")] = o.val().trim();
        }
    }
};

/**
 * 利用 jquery.form 简化的获取参数的方法, 将 ArrayObj 转换成为 param[obj]
 * @param formArr
 */
$.formArrayToObj = function(formArr){
    var param = {};
    for(var i = 0; i < formArr.length; i++){
        var el = formArr[i];
        param[el['name']] = el['value'];
    }
    return param;
};

/**
 * 这个是点 row 元素拥有自己的 click 事件的情况下使用.
 * rowsSelector: 需要绑定删除 active 的元素
 * activeObj: 需要选中 jQuery 元素
 */
$.tableRowClickActive = function(rowsSelector, activeObj){
    $(rowsSelector).removeClass('active');
    activeObj.addClass('active');
};

/**
 * 这个在 row 元素无自己的 click 的情况下使用
 * @param rowsSelector
 */
$.tableRowSelect = function(rowsSelector){
    $(rowsSelector).click(function(){
        $(rowsSelector).removeClass('active');
        $(this).addClass('active');
    });
};

$.DateUtil = {
    /**
     * 按照给与的 date 基准时间, 然后进行 day 天数的添加减少
     * @param date
     * @param day
     */
    addDay: function(day, date){
        if(date){
            if($.type(date) != 'date'){
                throw 'the date is not type of Date';
            }
        }else{
            date = new Date();
        }
        var newDate = new Date(date.getTime());
        newDate.setDate(newDate.getDate() + day);
        return newDate;
    },
    /**
     * mm/dd/yyyy 格式的年月日
     */
    fmt1: function(date){
        var month = date.getMonth() + 1;
        var ddate = date.getDate();
        return (month < 10 ? '0' + month :month) + '/' + (ddate < 10 ? '0' + ddate :ddate) + '/' + date.getFullYear();
    },
    /**
     * yyyy-MM-dd 格式的年月日
     * @param date
     */
    fmt2: function(date){
        var month = date.getMonth() + 1;
        var ddate = date.getDate();
        return date.getFullYear() + "-" + (month < 10 ? '0' + month :month) + '-' + (ddate < 10 ? '0' + ddate :ddate)
    },

    /**
     * yyyy-MM-dd HH:mm:ss 格式
     * @param date
     */
    fmt3: function(date){
        return this.fmt2(date) + " " + this.time(date);
    },

    time: function(date){
        var hour = date.getHours();
        var minu = date.getMinutes();
        var sencods = date.getSeconds();
        return (hour < 10 ? '0' + hour : hour) + ':' + (minu < 10 ? '0' + minu : minu) + ":" + (sencods < 10 ? '0' + sencods : sencods);
    }

};

/**
 * 利用 form 表单提交的时候, 添加一个 input hidden 来检查 checkbox 是否点击
 * @param box
 */
function checkbox(box){
    var o = $(box);
    o.next().val(o.is(':checked'));
}

function toggle_init(){
    // 为页面添加 data-toggle=toggle 元素事件(类似 bootstrap 的 collapse)
    $('body').off('click', '[data-toggle=toggle]').on('click', '[data-toggle=toggle]', function(e){
        var target = $(this).attr('data-target');
        $(target).fadeToggle('fast').css('cursor', 'pointer');
        e.preventDefault();
    });
}

// 为 .btn 添加上 loading , 防止多次提交
function btn_loading_init(){
    $('body').off('click', '.btn[data-loading]').on('click', '.btn[data-loading]', function(e){
        $(this).button('loading')
    })
}

/**
 * 对在 Table 中含有 checkbox.checkall 的元素进行全选处理
 */
function tableCheckBoxCheckAll(){
    $('table').on('change', ':checkbox.checkall', function(){
        var table = $(this).parents('table').find(':checkbox').prop('checked', $(this).prop('checked'));
    });
}

$(function(){
    toggle_init();
    btn_loading_init();
    tableCheckBoxCheckAll();

    LoadMask.clear();
});

$.noty.defaults = $.extend({}, $.noty.defaults, {layout: 'topCenter', timeout: '5000'});
