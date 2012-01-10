$(function() {
    var triggers = $(".prodInput").overlay();
    var postForm = $('#add_form').validator();
    $('#addProdPostBtn').click(function() {
        var params = {};
        postForm.find(':input').each(function(i, o) {
            var ji = $(o);
            params[ji.attr("name")] = ji.val();
        });

        if('p.nocat' in params) {
            alert('请选择 Category!');
            //            return;
        }
        $.post('/products/c', params, function(data) {
            if(data.id && data['sku'] == params['p.sku']) { //成功
                // 清零 Form 数据
                alert('SKU: [' + data['sku'] + ']添加成功.');
            } else { //失败
                alert("添加失败:\r\n " + JSON.stringify(data));
            }
        }, 'json');
    });
});