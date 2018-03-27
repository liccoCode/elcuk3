$(() => {

  $("a[name='copyBtn']").click(function () {
    let sku = $(this).data('sku');
    $("#target_choseid").val(sku);
    $('#copy_modal').modal('show');
  });

  $("a[name='backupBtn']").click(function () {
    let id = $(this).data('sku');
    let family = $(this).data('family');
    $("#backup_choseid").val(id);
    $("#back_sku").val(id);
    $("#back_families").val(family);
    $('#backup_modal').modal('show');
  });

  $(document).on("change", "select[name='pro.state'], select[name='pro.salesLevel']", function (r) {
    let $select = $(this);
    let sku = $(this).data("sku");
    LoadMask.mask();
    if ($select.val() != "" && sku != "") {
      if ($select.attr('name') == 'pro.state') {
        $.post('/products/updateState', {
          state: $select.val(),
          sku: sku
        }, function (r) {
          if (r.flag) {
            noty({
              text: '更新产品状态成功!',
              type: 'success'
            });
          } else {
            noty({
              text: '更新产品状态失败!',
              type: 'error'
            });
          }
        });
      } else {
        $.post('/products/updateSalesLevel', {
          salesLevel: $select.val(),
          sku: sku
        }, function (r) {
          if (r.flag) {
            noty({
              text: '更新销售级别成功!',
              type: 'success'
            });
          } else {
            noty({
              text: '更新销售级别失败!',
              type: 'error'
            });
          }
        });
      }
    }
    LoadMask.unmask();
  }).on("click", "a[name='deleteBtn']", function () {
    let $logForm = $("#logForm");
    let sku = $(this).data("sku");
    $($("#logForm input")[0]).val(sku);
    $logForm.modal();
  });

  $("#downloadBtn").click(function (e) {
    e.preventDefault();
    let $form = $("#search_Form");
    window.open($(this).data("url") + '?' + $form.serialize(), "_blank")
  });

  $("#categories").multiselect({
    buttonWidth: '120px',
    nonSelectedText: '品线',
    maxHeight: 200,
    includeSelectAllOption: true
  });

  $("input[name='p.search']").typeahead({
    source: (query, process) => {
      $.get('/products/source', {
        search: query
      }, function (c) {
        process(c)
      });
    }
  });

  $("a[name='improtProduct']").click(function (e) {
    e.preventDefault();
    $("#shipment_modal").modal('show');
    e.preventDefault();
  });

  $("#submitUpdateBtn").click(function () {
    $("#payment_form").submit();
  });

});