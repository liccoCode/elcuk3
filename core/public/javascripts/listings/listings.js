$(function(){
    var allSkuLinks = $("#slider a[level=sku]");
    SELLINGS = $("#s_list");
    LISTINGS = $("#l_list");
    ACCOUNT = $("#account");
    MARKET = $("#market");


    $("a[rel=popover]").popover({trigger:'focus'}).click(function(){
        $(this).focus();
        return false;
    });

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

    // -------------- 上架 Amazon 的相关功能

    // UPC 检查
    $('input[name=s\\.aps\\.upc] ~ button').click(function(){
        var upcEl = $(this).prev();
        var upc = upcEl.val();
        if(!$.isNumeric(upc)){
            alert("UPC 必须是数字!");
            return false;
        }
        $.getJSON('/listings/upcCheck', {upc:upc}, function(r){
            if(r.flag === false) alert(r.message);
            else{
                var upcAlertTemplate = "<div class='alert alert-info fade in'>" +
                        "<button class='close' data-dismiss='alert'>×</button>" +
                        "<strong>Sellings:</strong>" +
                        "</div>";
                var alertEl = $(upcAlertTemplate);
                if(r.length == 0)
                    alertEl.find('strong').after('<div>此 UPC 在系统中还没有 Selling</div>')
                else
                    $.each(r, function(i, s){
                        alertEl.find("strong").after('<div>' + s['merchantSKU'] + " | " + s['market'] + '</div>');
                    });
                alertEl.insertBefore("#btn_div");
                var mskuEl = $('input[name=s\\.merchantSKU]');
                mskuEl.val(mskuEl.val().split(',')[0] + ',' + upc);
            }
        });
    });

    // ProductDESC 输入, 字数计算
    $("textarea[name=s\\.aps\\.productDesc]").keyup(function(){
        var o = $(this);
        var length = o.css('color', 'black').val().length;
        if(length > 2000) o.css('color', 'red');
        o.siblings('span').html((2000 - length) + " bytes left");
    });

    // 预览按钮
    $("textarea[name=s\\.aps\\.productDesc] ~ button").click(function(){
        var ownerDiv = $(this).parent();
        var htmlPreview = ownerDiv.find(":input").val();
        var invalidTag = false;
        ownerDiv.siblings('div').html(htmlPreview).find('*').map(function(){
            var nodeName = this.nodeName.toString().toLowerCase();
            switch(nodeName){
                case 'br':
                case 'p':
                case 'b':
                case '#text':
                    break;
                default:
                    invalidTag = true;
                    $(this).css('background', 'yellow');
            }
        });
        if(invalidTag) alert("使用了 Amazon 不允许的 Tag, 请查看预览中的红色高亮部分!");
    });


    // Amazon 上架
    $('#s_sale').click(function(){
        $.varClosure.params = {'s.listing.listingId':$('#lid').text()};
        $("#amazon :input").map($.varClosure);
        $.post('/listings/saleAmazonListing', $.varClosure.params, function(r){
            if(r.flag) alert('更新成功.');
            else alert(r.message);
        });
    });

});