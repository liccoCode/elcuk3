$(function(){
    $('.middleFunc :date').dateinput({format:'mm/dd/yyyy'});
    var c1 = {
        chart:{
            renderTo:'a_chart',
            defaultSeriesType:'line',
            marginBottom:50
        },
        title:{
            text:'Selling Sales'
        },
        xAxis:{
            type:'datetime',
            dateTimeLabelFormats:{
                day:'%y.%m.%d'
            }
        },
        yAxis:{
            title:{
                text:'Sales'
            },
            min:0,
            plotLines:[
                {
                    value:0,
                    width:1,
                    color:'#808080'
                }
            ]
        },
        tooltip:{
            formatter:function(){
                var cur = new Date(this.x);
                return '<strong>' + this.series.name + '</strong><br/>' +
                        'Date:' + (cur.toLocaleDateString()) + '<br/>' +
                        'Sales: ' + this.y;
            }
        },
        series:[
        ]
    };

    function ajax_line(msku, params){
        $.ajax({
            url:'/analyzes/ajaxLine',
            data:params,
            dataType:'json',
            success:function(data){
                c1.title.text = 'Selling [' + msku + '] Sales';
                var series = [];

                /**
                 *  处理一条一条的曲线
                 */
                function dealLine(lineName){
                    if(!data['series_' + lineName]) return false;
                    var line = {};
                    line.name = lineName.toUpperCase();
                    line.data = [];
                    var now = new Date();
                    for(var d = data['days']; d > 0; d--){
                        var curt = new Date();
                        curt.setDate(now.getDate() - d);
                        line.data.push([curt.getTime(), data['series_' + lineName].shift()]);
                    }
                    series.push(line);
                    return false;
                }

                dealLine('all');
                dealLine('auk');
                dealLine('ade');
                dealLine('afr');
                dealLine('ait');

                c1.series = series;
                new Highcharts.Chart(c1);
            },
            error:function(xhr, state, err){
                alert(err);
            }
        });
    }

    // 访问页面, 利用 Ajax 加载所有订单的销量
    var preMonth = $.DateUtil.addDay(-30);
    var now = new Date();
    ajax_line('all', {from:preMonth.toLocaleDateString(), to:now.toLocaleDateString(), msku:'all'});
    $('#a_from').data("dateinput").setValue(preMonth);
    $('#a_to').data("dateinput").setValue(now);

    $('#a_search').click(function(){
        $.varClosure.params = {};
        $(".middleFunc :input").map($.varClosure);
        if(!$.varClosure.params['msku']){
            alert('MerchantSKU 不允许为空! 或者可输入 ALL 进行所有订单查询!');
            return false;
        }
        var msku = $.varClosure.params['msku'];// 回掉函数会使用, 防止回掉函数使用的时候值变了
        if(!$.varClosure.params['from']){
            alert('使用默认的一个月时间间隔.');
            var now = new Date();
            var from = $.DateUtil.addDay(-30, now);
            $.varClosure.params['from'] = from.getFullYear() + "-" + (from.getMonth() + 1) + "-" + from.getDay();
            $.varClosure.params['to'] = now.getFullYear() + "-" + (now.getMonth() + 1) + "-" + now.getDay();
        }
        ajax_line(msku, $.varClosure.params);
    });


    $('.msku').dblclick(function(){
        $('#a_msku').val($(this).text());
    });

});