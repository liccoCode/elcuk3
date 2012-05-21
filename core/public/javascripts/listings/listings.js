$(function(){
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
            listingRelateSelling();
        });

    });

});