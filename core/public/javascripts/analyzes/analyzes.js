$(function(){
    MSKU = 'msku';
    SKU = 'sku';

    // init page
    (function(){
        $('#a_toolbar :input[type=date]').dateinput({format:'mm/dd/yyyy'});
        $('a[rel=popover]').popover();

        // 访问页面, 利用 Ajax 加载所有订单的销量
        var preMonth = $.DateUtil.addDay(-30);
        var now = new Date();
        $('#a_from').data("dateinput").setValue(preMonth);
        $('#a_to').data("dateinput").setValue(now);

        // 最下方的 Selling[MerchantSKU, SKU] 列表信息
        sellRankLoad(MSKU, 1);
        sellRankLoad(SKU, 1);
        // 销量线
        sales_line({from:$.DateUtil.fmt1(preMonth), to:$.DateUtil.fmt1(now), msku:'All', type:'msku'});
        // PageView 线
        pageViewDefaultContent();
    }());

    /**
     * 用来构造给 HighChart 使用的默认 options 的方法
     * @param container
     * @param yName
     */
    function lineOp(container, yName){
        return {
            chart:{renderTo:container},
            title:{text:'Chart Title'},
            xAxis:{type:'datetime', dateTimeLabelFormats:{day:'%y.%m.%d'}},
            yAxis:{title:{text:yName}, min:0},
            plotOptions:{
                series:{
                    cursor:'pointer',
                    point:{events:{}}
                }
            },
            tooltip:{},
            series:[
            ],
            /**
             * 设置这条线的'标题'
             * @param title
             */
            head:function(title){
                this.title.text = title;
                return this;
            },
            click:function(func){
                this.plotOptions.series.point.events.click = func;
                return this;
            },
            mouseOver:function(func){
                this.plotOptions.series.point.events.mouseOver = func;
                return this;
            },
            mouseOut:function(func){
                this.plotOptions.series.point.events.mouseOut = func;
                return this;
            },
            formatter:function(func){
                this.tooltip.formatter = func;
                return this;
            },
            clearLines:function(){
                this.series = [];
            }
        }
    }

    /* 查看 Sellings 的销售量的 HightChart Options 对象 */
    var sellOp = lineOp('a_units', 'Units').click(
            function(){
                var msku = localStorage.getItem('msku');
                window.open('/analyzes/pie?msku=' + msku + "&date=" + $.DateUtil.fmt1(new Date(this.x)),
                        msku,
                        'width=520,height=620,location=yes,status=yes');
            }).formatter(function(){
                var cur = new Date(this.x);
                return '<strong>' + this.series.name + '</strong><br/>' +
                        'Date:' + ($.DateUtil.fmt1(cur)) + '<br/>' +
                        'Sales: ' + this.y;
            });

    /* 查看 Sellings 的销售额的 HightChart Options 对象 */
    var saleOp = lineOp('a_sales', 'Sales').click(
            function(){
                alert(this.series.name + ":::::" + this.x + ":::" + this.y);
            }).formatter(function(){
                var cur = new Date(this.x);
                return '<strong>' + this.series.name + '</strong><br/>' +
                        'Date:' + ($.DateUtil.fmt1(cur)) + '<br/>' +
                        'Sales: ' + this.y;
            });

    var pvOp = lineOp('a_pv', 'PageView').click(function(){
        alert("点击了这个按钮");
    });

    var ssOp = lineOp('a_ss', 'Session');

    /**
     * 加载并且绘制 Selling 的 PageView 与 Session 的线条
     * @params  msku, from, to
     */
    function pvSS_line(params){
        $.getJSON('/analyzes/ajaxSellingRecord', params, function(r){
            if(r.flag === false) alert(f.message);
            else{
                var lines = {
                    pv_uk:{name:'PageView(uk)', data:[]},
                    pv_de:{name:'PageView(de)', data:[]},
                    pv_fr:{name:'PageView(fr)', data:[]},
                    ss_uk:{name:'Session(uk)', data:[]},
                    ss_de:{name:'Session(de)', data:[]},
                    ss_fr:{name:'Session(fr)', data:[]}
                };
                pvOp.clearLines();
                ssOp.clearLines();
                for(var key in r){
                    r[key].forEach(function(o){
                        lines[key].data.push([o['_1'], o['_2']]);
                    });
                    if(key.indexOf('pv') >= 0) pvOp.series.push(lines[key]);
                    else if(key.indexOf('ss') >= 0) ssOp.series.push(lines[key]);
                }
                new Highcharts.Chart(pvOp);
                new Highcharts.Chart(ssOp);
            }
        });
    }

    /**
     * 加载并且绘制 Selling 的销售额与销售量的线条
     * @param msku
     * @param params
     * @param type -1:销售量, 1:销售额
     */
    function sales_line(params){
        $('#myTabContent').mask('加载中...');
        $.getJSON('/analyzes/ajaxSells', params, function(data){
            var display_sku = params['msku'];
            var prefix = 'Selling [<span style="color:orange">' + display_sku + '</span> | ' + params['type'].toUpperCase() + ']';
            sellOp.head(prefix + ' Sales');
            saleOp.head(prefix + ' Prices');
            sellOp.clearLines();
            saleOp.clearLines();
            /**
             *  处理一条一条的曲线
             */
            function dealLine(lineName, defOp){
                if(!data['series_' + lineName]) return false;
                var line = {};
                line.name = lineName.toUpperCase();
                line.data = [];
                for(var d = data['days']; d > 0; d--){
                    line.data.push([
                        $.DateUtil.addDay(-d + 1, $('#a_to').data('dateinput').getValue()).getTime(),
                        data['series_' + lineName].shift()
                    ]);
                }
                defOp.series.push(line);
                return false;
            }

            dealLine('all', sellOp);
            dealLine('auk', sellOp);
            dealLine('ade', sellOp);
            dealLine('afr', sellOp);

            dealLine('allM', saleOp);
            dealLine('aukM', saleOp);
            dealLine('adeM', saleOp);
            dealLine('afrM', saleOp);

            localStorage.setItem("msku", params['msku']);
            new Highcharts.Chart(sellOp);
            new Highcharts.Chart(saleOp);
            $('#myTabContent').unmask();
        });
    }

    /**
     * Ajax Load 页面下方的 MSKU 与 SKU 两个 Tab 的数据.
     * @param type
     * @param page
     */
    function sellRankLoad(type, page){
        if(type != MSKU && type != SKU){
            alert("只允许 msku 与 sku 两种类型!");
            return false;
        }
        var tgt = $('#' + type);
        tgt.mask('加载中...');
        tgt.load('/analyzes/index_' + type, {'p.page':page, 'p.size':10, 'p.param':$('#a_param').val()}, function(){
            try{
                //Selling 的(Ajax Line)双击事件
                $('.msku,.sku').unbind().dblclick(function(){
                    // 取到并设置: 时间, Type, Account
                    var o = $(this);
                    $.varClosure.params = {type:o.attr('class')}; // sku 类型不参加 sid 与 msku 的选择
                    var accId = o.attr('aid');
                    $('#a_acc_id').val(accId);
                    $('#a_msku').val(o.attr('title'));
                    $('#dbcick_param :input').map($.varClosure);

                    // 绘制销量线
                    sales_line($.varClosure.params);
                    // 绘制 PageView 线
                    if($.varClosure.params['type'] === 'msku') // 只有在查看 msku 类型的时候, 才会刷新 PageView
                        pvSS_line($.varClosure.params);
                    else
                        pageViewDefaultContent();//重置 PageView 内容

                    var display = {0:'EasyAcc', 1:'EasyAcc.EU', 2:'EasyAcc.DE'};
                    $('#a_acc_id_label').html(display[accId]);
                });
                //页脚的翻页事件
                $('div.pagination a').click(function(){
                    sellRankLoad(type, $(this).attr('page'));
                    return false;
                });

                $('#pagefooter_' + type).keyup(function(e){
                    if(e.keyCode != 13) return false;
                    var o = $(this);
                    if(o.val() > new Number(o.attr('totalPage'))){
                        alert('不允许超过最大页码');
                        return false;
                    }
                    // Ajax 事件需要重新加载
                    sellRankLoad(type, o.val());
                });
            }finally{
                tgt.unmask();
            }
        });
        return false;
    }

    // 给 搜索 按钮添加事件
    $('#a_search').click(function(){
        var tab_type = $('#tab li[class=active] a').attr('href').substring(1);
        var page = $('#pagefooter_sku').val() - 1;
        /*搜索框中保持当前页码不变*/
        sellRankLoad(tab_type, page <= 0 ? 1 :page);
        return false;
    });
    $('#a_param').keyup(function(e){
        if(e.keyCode == 13) $('#a_search').click();
        return false;
    });

    /**
     * PageView 与 Session 两条曲线在没有加载内容的时候的默认情况
     */
    function pageViewDefaultContent(){
        var template = '<div class="alert alert-success"><h3 style="text-align:center">请双击需要查看的 Selling 查看 PageView & Session</h3></div>';
        ['a_pv', 'a_ss'].forEach(function(id){
            $('#' + id).html(template);
        });
    }

});