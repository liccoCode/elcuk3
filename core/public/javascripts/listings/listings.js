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

    $('.l_tabs').tabs('div.l_panels > div', {current:'l_current'});

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

    $('#cat_prod_list .l_cat').toggle(function(){
        $(this).next().css('display', 'block');
    }, function(){
        $(this).next().css('display', 'none');
    });

    $('#cat_prod_list .sub_prod a.l_prod').click(function(){
        $('#pord_' + $.HASH.sku).removeClass('l_checked');
        $.HASH.sku = $(this).addClass('l_checked').text();
        window.location.hash = $.HASH.val();
        var l_ListDiv = $('#l_List');
        $.mask.load();
        if(l_ListDiv.html().trim() == '请选择 Product' ||
                l_ListDiv.html().trim() == 'SKU 错误' ||
                l_ListDiv.html().trim() == '没有关联的 Listing'){
            $.get('/listings/l_listing', {sku:$.HASH.sku}, function(data){
                l_ListDiv.html(data);
            });
        }
        var prodDiv = $('#prod');
        if(prodDiv.html() != 'Product 信息'){
            $('.l_tabs a:eq(0)').click();
        }else{
            $.get('/listings/l_prodDetail', {sku:$.HASH.sku}, function(data){
                prodDiv.html(data);
                $('.l_tabs a:eq(0)').click();
            });
        }
        $.mask.close();
        return false;
    });
});