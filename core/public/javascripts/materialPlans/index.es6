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
        $('#create_deliveryment').attr('action', $(this).data('url')).submit();
      }
    }
  });


});