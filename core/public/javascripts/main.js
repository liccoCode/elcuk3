/**
 * 对 simile Timeline 参数设置的初始化
 */
Timeline_ajax_url = "/js/timeline/timeline_ajax/simile-ajax-api.js";
Timeline_urlPrefix = '/js/timeline/timeline_js/';
Timeline_parameters = 'bundle=true';

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
$.varClosure2 = function(){
    var o = $(this);
    if(!o.attr('name')) return false;
    if(o.val() != undefined){
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
 * 对页面上的所有 Checbox 添加 check 事件
 */
$.checkBox = function(){
    $(':checkbox').change(function(e){
        var o = $(this);
        if(!o.attr('name')) e.preventDefault()
        o.val(o.is(":checked"));
        console.log(o.attr('name') + " TO " + o.val());
        e.preventDefault();
    })
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
        var month = date.getMonth() + 1;
        var ddate = date.getDate();
        return date.getFullYear() + "-" + (month < 10 ? '0' + month :month) + '-' + (ddate < 10 ? '0' + ddate :ddate)
    },

    /**
     * yyyy-MM-dd HH:mm:ss 格式
     * @param date
     */
    fmt3:function(date){
        return this.fmt2(date) + " " + date.getHours() + ':' + date.getMinutes() + ":" + date.getSeconds();
    }

};

/**
 * 利用 form 表单提交的时候, 添加一个 input hidden 来检查 checkbox 是否点击
 * @param box
 */
function checkbox(box){
    var o = $(box);
    o.next().val(o.val());
}

function toggle_init(){
    // 为页面添加 data-toggle=toggle 元素事件(类似 bootstrap 的 collapse)
    $('body').off('click', '[data-toggle=toggle]').on('click', '[data-toggle=toggle]', function(e){
        var target = $(this).attr('data-target');
        $(target).fadeToggle('fast');
        e.preventDefault();
    });
}

/**
 * 为 Form 里面的 button 添加 confirm 事件
 */
function submit_btn_init(){
    $('body').off('click', '[data-confirm=confirm]').on('click', '[data-confirm=confirm]', function(e){
        e.preventDefault();
        if(!confirm('确定提交?')) return false;
        $(this).parents('form').submit();
    });
}

function link_confirm_init(){
    $('body').off('click', 'a[data-confirm=link]').on('click', 'a[data-confirm=link]', function(e){
        if(!confirm("确认执行此操作?")) e.preventDefault()
    });
}

$(function(){
    toggle_init();
    submit_btn_init();
    link_confirm_init();
});

