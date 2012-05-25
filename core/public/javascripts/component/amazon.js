$(function(){
    $('input[bullet_point]').keyup(function(e){
        if(e.keyCode == 13) return false;
        var o = $(this);
        var length = encodeURI(o.val().trim()).length;
        o.find('~ span').html((2000 - length) + " bytes left");
        if(length > 2000) o.css('color', 'red');
        else o.css('color', '');
        return false;
    })/*直接计算一下*/.keyup();

    $('input[searchterms]').keyup(function(e){
        if(e.keyCode == 13) return false;
        var o = $(this);
        var length = o.val().trim().length;
        o.find('~ span').html((50 - length) + " bytes left");
        if(length > 50) o.css('color', 'red');
        else o.css('color', '');
        return false;
    })/*直接计算一下*/.keyup();

    /**
     * 预览 ProductDesc 的方法[在 Product 页面的上架的地方也使用到了]
     */
    function previewBtn(){
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
    }

    // ProductDESC 输入, 字数计算
    $("textarea[name=s\\.aps\\.productDesc]").blur(previewBtn).keyup(function(){
        var o = $(this);
        var length = o.css('color', 'black').val().length;
        if(length > 2000) o.css('color', 'red');
        o.siblings('span').html((2000 - length) + " bytes left");
    })/*自己按一下, 在页面开始的时候计算一次*/.keyup()./*预览按钮*/find('~ button').click(previewBtn).click();
});

