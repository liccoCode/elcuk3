$(function(){
    $.post('/listings/reload');
    var allSkuLinks = $("#slider a[level=sku]");

    function listingRelateSelling(){

    }

    allSkuLinks.dblclick(function(){
        allSkuLinks.parent().removeClass("active");
        $(this).parent().addClass("active");
        var listingEl = $("#listings");
        listingEl.mask("加载中...");
        listingEl.load("/listings/prodListings", {'p.sku':$(this).attr('pid')}, function(){
            listingEl.unmask();
        });
    });

});