$(() => {

  $("#data_table").on("click", "[name='refundBtn']", function (e) {
    e.preventDefault();
    let memo = $(this).parent("td").parent("tr").find("input[name='memo']").val();
    let qty = $(this).parent("td").parent("tr").find("input[name$='attrs.qty']").val();
    if (memo && qty) {
      $("#id_input").val($(this).data("id"));
      $("#qty_input").val(qty);
      $("#memo_input").val(memo);
      $("#data_form").attr("action", $(this).data("url")).submit();
    } else {
      noty({
        text: '必须填写处理说明和数量!',
        type: 'warning'
      });
    }
  });

  $("#data_table").on("click", "[name='transferBtn']", function (e) {
    e.preventDefault();
    let $tr = $(this).parent("td").parent("tr");
    let type = $tr.find("select[name='type']").val();
    if (!type) {
      noty({
        text: '必须选择不良入库类型!',
        type: 'warning'
      });
      return;
    }
    let memo = $tr.find("input[name='memo']").val();
    let qty = $tr.find("input[name$='attrs.qty']").val();
    if (qty > $(this).data("unqualified")) {
      noty({
        text: '填写数量超过不良品数!',
        type: 'warning'
      });
    } else if (memo && qty) {
      $("#id_input").val($(this).data("id"));
      $("#qty_input").val(qty);
      $("#memo_input").val(memo);
      $("#type_input").val(type);
      $("#data_form").attr("action", $(this).data("url")).submit();
    } else {
      noty({
        text: '必须填写处理说明和数量!',
        type: 'warning'
      });
    }
  });

  $("input[name='qty']").change(function () {
    let origin = $(this).data("origin");
    if ($(this).val() < 0 || $(this).val() > origin) {
      noty({
        text: '数量不能小于0或初始大于不良品数!',
        type: 'warning'
      });
      $(this).val(origin);
    }
  });

  $("#batchBtn").click(function (e) {
    e.preventDefault();
    let checks = $("input[name$='id']:checked");
    if (checks.length == 0) {
      noty({
        text: '请先选择需要退货的采购计划！',
        type: 'warning'
      });
      return false;
    }
    let firstCooper = checks.first().data("cooper");
    let i = 0;
    checks.each(function () {
      if ($(this).data("cooper") != firstCooper) {
        i++;
      }
    });
    if (i > 0) {
      noty({
        text: '请选择相同供应商的采购计划！',
        type: 'error'
      });
      return false;
    }
    $("#batch_refund_modal").modal("show");
  });

  $("#submitBtn").click(function (e) {
    if ($("#refundMemo").val()) {
      $("#batchMemo").val($("#refundMemo").val());
      $("#commitForm").submit();
    } else {
      noty({
        text: '退货说明必填！',
        type: 'error'
      })
    }
  });

});