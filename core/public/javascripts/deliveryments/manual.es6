$(() => {

  // 切换供应商, 自行寻找价格
  $("select[name='dmt.cooperator.id']").change(function() {
    let id = $(this).val();
    if (id) {
      LoadMask.mask();
      $.get('/Cooperators/price', {
        id: id,
        sku: $('#unit_sku').val()
      }, function(r) {
        if (!r.flag) {
          alert(r.message);
        } else {
          $("#unit_currency option:contains('#{r.currency}')").prop('selected', true);
          $("#unit_price").val(r.price);
        }
        LoadMask.unmask();
      });
    } else {
      $("#unit_currency option:contains('CNY')").prop('selected', true);
      $("#unit_price").val('');
    }
  });

  $('#box_num').change(function() {
    e.preventDefault()
    let cooper_id = $("select[name='dmt.cooperator.id']").val();
    if (cooper_id) {
      $.post('/cooperators/boxSize', {
        size: $('#box_num').val(),
        coperId: cooper_id,
        sku: $('#unit_sku').val()
      }, function(r) {
        if (!r.flag) {
          alert(r.message);
        } else {
          $("input[name='unit.attrs.planQty']").val(r['message']);
        }
      });
    } else {
      alert('请先选择 供应商');
    }
  });

  let $sku = $("input[name='unit.product.sku']");
  $sku.typeahead({
    source: (query, process) => {
      let sku = $sku.val();
      $.get('/products/sameSku', {
        sku: sku,
      }, function(c) {
        process(c);
      });
    },
    updater: (item) => {
      let $cooperators = $("select[name='dmt.cooperator.id']")
      $.get('/products/cooperators', {sku: item}, function(r) {
        LoadMask.mask();
        $cooperators.empty();
        $cooperators.append("<option value=''>请选择</option>");
        r.forEach(function(value) {
          $cooperators.append("<option value=" + value['id'] + ">" + value['name'] + "</option>");
        });
        LoadMask.unmask()
      });
      return item;
    }
  });

});