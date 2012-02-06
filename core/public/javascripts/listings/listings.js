$(function(){
    $.HASH = {sku:'', listing:'', selling:'',
        val:function(){
            return this.sku + "/" + this.listing + "/" + this.selling;
        },
        parse:function(){
            var splts = window.location.hash.split('/');
            this.sku = splts[0].substr(1);
            this.listing = splts[1] ? splts[1] :'';
            this.selling = splts[2] ? splts[2] :'';
            return this;
        }
    };

    $('.btn[rel="popover"]').popover({placement:'bottom'});

    //抓取并绑定 Listing 的按钮
    $('#craw_listing_btn').click(function(){
        if(!$.HASH.parse().sku){
            alert('请选择一个 Product!');
            return false;
        }
        var market = prompt('请输入一个 Market[uk,de,it,es,us]', 'uk');
        var asin = prompt('输入需要抓去的 Listing 的 ASIN.');
        if(!asin || asin.trim().length == 0){
            alert("请输入 ASIN.");
            return false;
        }
        alert("命令已提交...");
        $.ajax({
            url:'/listings/crawl',
            data:{market:market.trim(), asin:asin.trim(), sku:$.HASH.sku},
            dataType:'json',
            success:function(data){
                alert('成功添加并绑定 Listing:{' + data['listingId'] + "}\r\n[" + data['title'] + "]");
            },
            error:function(xhr, sta, err){
                alert(err);
            }
        });
        return false;
    });

    // 左侧 SKU 导航的点击事件
    $('#cat_prod_list a[sku]').click(function(){
        $('#pord_' + $.HASH.sku).parent().removeClass('active');
        $.HASH.sku = $(this).parent().addClass('active').children().text();
        window.location.hash = $.HASH.val();
        $('#l_CatAndProd').mask('加载中...');
        $.get('/listings/l_listing', {sku:$.HASH.sku}, function(data){
            $('#l_List').html(data);
        });
        $('.l_tabs a:eq(0)').click();
        $.get('/listings/l_prodDetail', {sku:$.HASH.sku}, function(data){
            $('#prod').html(data);
            $('.tabs a:first').tab('show');
            $('#l_CatAndProd').unmask();
        });
        return false;
    });
});