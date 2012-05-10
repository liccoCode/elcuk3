$(function(){
    $.post('/listings/reload');
    var allSkuLinks = $("#slider a[level=sku]");
    allSkuLinks.dblclick(function(){
        allSkuLinks.parent().removeClass("active");
        $(this).parent().addClass("active");
        $("#listings").load("/listings/prodListings", {'p.sku':$(this).attr('pid')});
    });

});