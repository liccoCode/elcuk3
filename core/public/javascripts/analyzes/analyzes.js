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
    function ajax_line(msku, params){
        $('#selling_down').mask('加载中...');
        $.ajax({
            url:'/analyzes/ajaxSells',
            data:params,
            dataType:'json',
            success:function(data){
                var display_sku = (data['type'] == 'sku' ? msku.split(',')[0] :msku);
                sells.title.text = 'Selling [' + display_sku + '] Sales';
                sales.title.text = 'Selling [' + display_sku + '] Prices';
                var sell_series = [];
                var sale_series = [];

                /**
                 *  处理一条一条的曲线
                 */
                function dealLine(lineName, series){
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

                dealLine('all', sell_series);
                dealLine('auk', sell_series);
                dealLine('ade', sell_series);
                dealLine('afr', sell_series);

                dealLine('allM', sale_series);
                dealLine('aukM', sale_series);
                dealLine('adeM', sale_series);
                dealLine('afrM', sale_series);

                sells.series = sell_series;
                sales.series = sale_series;
                localStorage.setItem("msku", msku);
                new Highcharts.Chart(sells);
                new Highcharts.Chart(sales);
                $('#selling_down').unmask();
            },
            error:function(xhr, state, err){
                alert(err);
                $('#selling_down').unmask();
            }
        });
    }


    /**
     * Ajax Load 页面下方的 MSKU 与 SKU 两个 Tab 的数据.
     * @param t
     * @param page
     * @param size
     */
    function sellRankLoad(t, page, size){
        var type = 'msku';
        if(!size) size = 10;
        if(t > 0) type = 'msku';
        else if(t < 0) type = 'sku';
        var tgt = $('#' + type);
        tgt.mask('加载中...');
        tgt.load('/analyzes/index_' + type, {'p.page':page, 'p.size':size}, function(){
            try{
                $('.msku').dblclick(function(){
                    $('#a_msku').val($(this).text());
                    $('#a_acc_id').val($(this).attr('aid'));
                    var display = {1:'easyacc.eu', 2:'easyacc.de'};
                    if(type == 'msku') $('#a_acc_id_label').html(display[$(this).attr('aid')]);
                });
                $('#pagefooter_' + type).keyup(function(e){
                    if(e.keyCode != 13) return false;
                    var o = $(this);
                    if(o.val() > new Number(o.attr('totalPage'))){
                        alert('不允许超过最大页码');
                        return false;
                    }
                    // Ajax 事件需要重新加载
                    sellRankLoad(t, o.val());
                });
            }finally{
                tgt.unmask();
            }
        });
    }

    // 访问页面, 利用 Ajax 加载所有订单的销量
    var preMonth = $.DateUtil.addDay(-30);
    var now = new Date();
    $('#a_from').data("dateinput").setValue(preMonth);
    $('#a_to').data("dateinput").setValue(now);

    // 最下方的 Selling[MerchantSKU, SKU] 列表信息
    sellRankLoad(1, 1);
    sellRankLoad(-1, 1);

    ajax_line('all', {from:$.DateUtil.fmt1(preMonth), to:$.DateUtil.fmt1(now), msku:'all', type:'msku'});


    // Search Btn
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
        ajax_line(msku, $.varClosure.params);
    });
});