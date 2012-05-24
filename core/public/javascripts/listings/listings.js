$(function(){
    $('button[rel=tooltip]').tooltip();
    var allSkuLinks = $("#slider a[level=sku]");
    SELLINGS = $("#s_list");
    LISTINGS = $("#l_list");
    ACCOUNT = $("#account");
    MARKET = $("#market");

    function loadGlobalVar(){
        return {
            account:ACCOUNT.val(),
            market:MARKET.val()
        };
    }

    function listingRelateSelling(){
        LISTINGS.find('tr[lid]').dblclick(function(){
            var amVal = loadGlobalVar();
            SELLINGS.mask('加载中...');
            SELLINGS.load('/listings/listingSellings', {'l.listingId':$(this).attr('lid'), 'a.id':amVal['account']}, function(){
                SELLINGS.unmask();
            });
            return false;
        })
    }

    allSkuLinks.dblclick(function(){
        allSkuLinks.parent().removeClass("active");
        $(this).parent().addClass("active");
        LISTINGS.mask("加载中...");
        var amVal = loadGlobalVar();

        LISTINGS.load("/listings/prodListings", {'p.sku':$(this).attr('pid'), 'm':amVal['market']}, function(){
            LISTINGS.unmask();
            SELLINGS.html('');
            listingRelateSelling();
        });

    });

    $('button[lid]').click(function(){
        var o = $(this);
        var lid = o.attr('lid');
        o.button('loading').tooltip('hide');
        $.post('/listings/reCrawl', {'l.listingId':lid}, function(r){
            if(r.flag){
                o.after("<span><a style='background-color:#DFF0D8;margin-left:10px;padding:8px;' href='/listings/listing?lid=" + lid + "'>更新成功</a></span>");
            }else alert(r.message);
            o.button('reset');
        });
    });
});