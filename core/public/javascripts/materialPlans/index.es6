$(() => {

  $("td[name='clickTd']").click(function () {
    let tr = $(this).parent("tr");
    let id = $(this).data("id");
    let format_id = id.replace(/\|/gi, '_');
    let type = $(this).data("type");
    if ($("#div" + format_id).html() != undefined) {
      tr.next("tr").toggle();
    } else {
      let html = "<tr style='background-color:#F2F2F2'><td colspan='13'><div id='div" + format_id + "'></div></td></tr>";
      tr.after(html);
      $("#data-table").mask();
      $("#div" + format_id).load($(this).data("url"), {id: id}, function () {
        $("#data-table").unmask();
        //点击明细修改按钮，显示弹出框,并初始化明细数据
        $("a[name='unitUpdateBtn']").click(function () {
          let id = $(this).attr("uid");
          //赋值
          $("#unit_id").val(id);
          $("#bom_modal").modal('show');
        });
      });
    }
  });

  //财务审核js处理
  $("#confirmMaterialPlanBtn").click(function (e) {
    let num = $("input[name='pids']:checked").length;
    if (num == 0) {
      noty({
        text: '请选择需要审核的数据!',
        type: 'error'
      });
    } else {
      let i = 0;
      let ids = [];
      $("input[name='pids']:checked").each(function () {
        console.log($(this).attr("financeState"));
        if ($(this).attr("financeState") != "PENDING_REVIEW") {
          i++;
          noty({
            text: $(this).val() + '已经审核，请选择【待审核】的出货单',
            type: 'error'
          });
          return false;
        }
        ids.push($(this).val());
      });
      if (i == 0) {
        if (confirm("点击审核后，即表示出货单据已审核通过！")) {
          return $('#deliverys_form').attr('action', $(this).data('url')).submit();
        }
      }
    }
  });

  //单个数据财务审核js处理
  $("#data-table a[name='approveBtn']").click(function (e) {
    e.preventDefault();
    $("#planId").val($(this).attr('uid'));
    if (confirm("点击审核后，即表示出货单据已审核通过！")) {
      return $('#deliverys_form').attr('action', $(this).data('url')).submit();
    }
  });

  //签收异常js处理
  $('#submitUpdateBtn').click(function (e) {
    e.stopPropagation();
    let $btn = $(this);
    let receiptQty = $("#unit_receiptQty").val();
    let $aobj = $("#qs_" + $("#unit_id").val());
    if (receiptQty == null || receiptQty == undefined || receiptQty == '' || isNaN(receiptQty)) {
      alert("请输入数字");
      $("#unit_receiptQty").focus();
      return false;
    } else {
      $.post('/MaterialPlans/updateMaterialPlanUnit', $("#updateUnit_form").serialize(), (r) => {
        if (r) {
          $aobj.parent("td").text(receiptQty);
          $aobj.remove();
          $('#bom_modal').modal('hide');
          noty({
            text: '更新成功!',
            type: 'success'
          });
          $("#unit_receiptQty").val("");
        } else {
          noty({
            text: r.message,
            type: 'error'
          });
        }
      });
    }
  });

  //请款js处理
  $('#goToApply').click(function (e) {
    e.stopPropagation();
    let firstCooper = $("input[name='pids']:checked").first().attr("cooperName");
    if ($("input[name='pids']:checked").length == 0) {
      noty({
        text: '请选择需纳入请款的出货单(相同供应商)',
        type: 'error'
      });
      return false;
    } else {

      let i = 0;
      let j = 0;
      let ids = [];
      $("input[name='pids']:checked").each(function () {
        if ($(this).attr("cooperName") != firstCooper) {
          j++;
        }
        ids.push($(this).val());
      });
      if (j > 0) {
        noty({
          text: '请选择[供应商]一致的出货单进行创建！',
          type: 'error'
        });
        return false;
      }

      if (i == 0 && j == 0) {
        $('#deliverys_form').attr('action', $(this).attr('url')).submit();
      }
    }
  });

  $("#exportExcel").click(function (e) {
    e.stopPropagation();
    let $form = $("#deliverys_form");
    window.open($(this).data("url") + "?" + $form.serialize(), "_blank");
  });

});