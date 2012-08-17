$(function(){
    $('#cat_select').change(function(){ // category 选择的事件
        var o = $(this);
        if(o.val() == 0 || o.val() == '' || o.val() == undefined) return false;
        $.getJSON("/products/cat_brands", {'c.categoryId':o.val()}, function(r){
            var brands = ['<option value="0">请选择</option>'];

            // 1. 与 Category 关联的 Brand 加载
            $.each(r['brands'], function(i, brand){
                brands.push("<option value='" + brand['name'] + "'>" + brand['fullName'] + "</option>");
            });
            $('#cat_brand').html($("<select class='span2' size='1'/>").html(brands.join('')).change(function(){ // Brand 的级联事件
                var brd = $(this);
                if(brd.val() == 0 || brd.val() == '' || brd.val() == undefined) return false;
                $.getJSON('/products/brand_family', {'b.name':brd.val(), 'c.categoryId':o.val()}, function(f){
                    var familys = ['<option value="0">请选择</option>'];

                    $.each(f, function(i, fam){
                        familys.push("<option value='" + fam['family'] + "'>" + fam['family'] + "</option>")
                    });

                    $('#brand_family').html($("<select class='span2' name='p.family.family' size='1'/>").html(familys.join(''))).find('select').change(function(){
                            $("#p_sku").val($(this).val());
                        });
                });
                return false;
            }));

            // 2. 初始化一定需要填写的 Category 继承下来的字段
            var fixedAttr = r['cAttrs'];
            var fixedNames = {};
            var fixedAttrEl = []; // 存放单个 html 代码的数组

            // 2.1 从 Category 继承下来的固定的字段
            $.each(fixedAttr, function(i, att){
                if(!(att['name'] in fixedNames)) fixedNames[att['name']] = 1;
                var hidden = "<input type='hidden' name='p.attrs[" + i + "].attName.name' value='" + att['name'] + "'>";
                fixedAttrEl.push("<p>" + att['fullName'] + ":" + hidden + " <input type='text' name='p.attrs[" + i + "].value'></p>");
            });
            if(fixedAttrEl.length <= 0) $('#cat_attr_div').html('<p>此 Category 暂时没有绑定 AttrName.</p>');else $('#cat_attr_div').html(fixedAttrEl.join(''));

            // 2.2 生成手动绑定添加绑定的 AttrName
            var allAttr = r['attrs'];
            var avalibleAttr = allAttr.filter(function(att){
                return !(att['name'] in fixedNames);
            });
            //    <label class="checkbox inline"><input type="checkbox" ${cat.attrNames.contains(a) ? 'checked' : ''} value="${a.name}" lval="${a.fullName}">${a.fullName}
            var avalibleAttrEl = [];
            $.each(avalibleAttr, function(i, att){
                avalibleAttrEl.push("<label class='checkbox inline'><input type='checkbox' name='" + att['name'] + "' value='" + att['fullName'] + "'>" + att['fullName'] + "</label>");
            });
            $('#attr_div_slider').html(avalibleAttrEl.join('')).find(":checkbox").change(function(){
                // 导航栏目中的每一次变化, 都将 attr_div 内的 html 清空,重新绘制 input 元素
                var checkedAttEl = [];
                $('#attr_div_slider :checked').each(function(i, checked){
                    /*属性的索引,需要跟着 FixedAttr 的索引增加*/
                    var att = $(checked);
                    var index = (i + fixedAttr.length);
                    var hidden = "<input type='hidden' name='p.attrs[" + index + "].attName.name' value='" + att.attr('name') + "'>";
                    checkedAttEl.push("<p><span class='span1.5'>" + att.val() + "</span>:" + hidden + " <input type='text' name='p.attrs[" + index + "].value'></p>");
                });
                $('#attr_div').html(checkedAttEl.join(''));
            });

            // 3. 上一次的 html 代码清理
            $("#brand_family").html('');
            $("#attr_div").html('')

        });
        return false;
    });

    $('#addProduct').click(function(){
        var success_link = $("#success_link");
        success_link.css('visibility', 'hidden').css('height', '20px').find('a').attr('href', '#');
        $.post('/products/pCreate', $('#container :input').fieldSerialize(), function(r){
            if(r.flag){
                success_link.css('visibility', '').css('height', '50px').find('a').attr('href', r.message);
            }else{
                alert(r.message);
            }
        });
    });
});