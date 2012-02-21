$(function(){
    $('#a_toolbar :input[type=date]').dateinput({format:'mm/dd/yyyy'});

    /* 查看 Sellings 的销售量的 HightChart Options 对象 */
    var sells = {
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
        plotOptions:{
            series:{
                cursor:'pointer',
                point:{
                    events:{
                        click:function(){
                            var msku = localStorage.getItem('msku');
                            window.open('/analyzes/pie?msku=' + msku + "&date=" + $.DateUtil.fmt1(new Date(this.x)),
                                    msku,
                                    'width=520,height=620,location=yes,status=yes');
                        }
                    }
                }
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
                        'Date:' + ($.DateUtil.fmt1(cur)) + '<br/>' +
                        'Sales: ' + this.y;
            }
        },
        series:[
        ]
    };

    /* 查看 Sellings 的销售额的 HightChart Options 对象 */
    var sales = {
        chart:{
            renderTo:'a_sales',
            defaultSeriesType:'line',
            marginBottom:50
        },
        title:{
            text:'Selling Price Sales'
        },
        xAxis:{
            type:'datetime',
            dateTimeLabelFormats:{
                day:'%y.%m.%d'
            }
        },
        plotOptions:{
            series:{
                cursor:'pointer',
                point:{
                    events:{
                        click:function(){
                            alert(this.series.name + ":::::" + this.x + ":::" + this.y);
                        }
                    }
                }
            }
        },
        yAxis:{
            title:{
                text:'Prices'
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
                        'Date:' + ($.DateUtil.fmt1(cur)) + '<br/>' +
                        'Sales: ' + this.y;
            }
        },
        series:[
        ]
    };


    /**
     * 加载并且绘制 Selling 的销售额与销售量
     * @param msku
     * @param params
     * @param type -1:销售量, 1:销售额
     */
    function ajax_line(msku, params, type){
        $('#selling_down').mask('加载中...');
        $.ajax({
            url:'/analyzes/' + (type < 0 ? 'ajaxSells' :'ajaxSales'),
            data:params,
            dataType:'json',
            success:function(data){
                var curtOpt;
                if(type < 0){
                    curtOpt = sells;
                    sells.title.text = 'Selling [' + msku + '] Sales';
                }else{
                    curtOpt = sales;
                    sales.title.text = 'Selling [' + msku + '] Prices';
                }
                var series = [];

                /**
                 *  处理一条一条的曲线
                 */
                function dealLine(lineName){
                    if(!data['series_' + lineName]) return false;
                    var line = {};
                    line.name = lineName.toUpperCase();
                    line.data = [];
                    for(var d = data['days']; d > 0; d--){
                        line.data.push([$.DateUtil.addDay(-d + 1, $('#a_to').data('dateinput').getValue()).getTime(), data['series_' + lineName].shift()]);
                    }
                    series.push(line);
                    return false;
                }

                dealLine('all');
                dealLine('auk');
                dealLine('ade');
                dealLine('afr');
                dealLine('ait');

                curtOpt.series = series;
                localStorage.setItem("msku", msku);
                new Highcharts.Chart(curtOpt);
                $('#selling_down').unmask();
            },
            error:function(xhr, state, err){
                alert(err);
                $('#selling_down').unmask();
            }
        });
    }

    // 访问页面, 利用 Ajax 加载所有订单的销量
    var preMonth = $.DateUtil.addDay(-30);
    var now = new Date();
    ajax_line('all', {from:$.DateUtil.fmt1(preMonth), to:$.DateUtil.fmt1(now), msku:'all'}, -1);
    $('#a_from').data("dateinput").setValue(preMonth);
    $('#a_to').data("dateinput").setValue(now);

    $('#a_search').click(function(){
        $.varClosure.params = {};
        $("#a_toolbar :input").map($.varClosure);
        if(!$.varClosure.params['msku']){
            alert('MerchantSKU 不允许为空! 或者可输入 ALL 进行所有订单查询!');
            return false;
        }
        var msku = $.varClosure.params['msku'];// 回掉函数会使用, 防止回掉函数使用的时候值变了
        if(!$.varClosure.params['from']){
            alert('使用默认的一个月时间间隔.');
            var now = new Date();
            var from = $.DateUtil.addDay(-30, now);
            $.varClosure.params['from'] = $.DateUtil.fmt1(from);
            $.varClosure.params['to'] = $.DateUtil.fmt1(now);
        }
        ajax_line(msku, $.varClosure.params, -1);
        ajax_line(msku, $.varClosure.params, 1);
    });


    // 最下方的 Selling Sale 列表信息
    $.get('/analyzes/index_sell', {'p.page':1, 'p.size':10}, function(html){
        $('#selling_down').html(html);
        $('.msku').dblclick(function(){
            $('#a_msku').val($(this).text());
        });
    });

    ajax_line('all', {from:$.DateUtil.fmt1(preMonth), to:$.DateUtil.fmt1(now), msku:'all'}, 1);
});